package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 辺②（YAML→中間モデル）の軸D — YAML スカラー 12 ケース（D2-01〜D2-12）が中間モデルへどう入るかを
 * 固定するテスト。
 *
 * <p>
 * {@code YamlFormatReaderTest} が {@code loadRawMap} を in-memory {@code Map} に差し替えて駆動するのに対し、
 * 本クラスは {@link YamlFixture} が書き出した実 {@code .yaml} を入力にし、本番配線の
 * {@link YamlFormatReader} を通す。すなわち「YAML テキスト → スカラー解決 → スキーマ検証」の区間を実行する。
 * 記法そのものが対象である以上、この区間を通さなければ軸D は担保できない。
 * </p>
 *
 * <p>
 * <b>対象は「NTF が実行できるテストデータ」に限る。</b>本体スキーマ
 * （yaml jar 内 {@code nablarch/test/ntf-testdata-yaml-schema.json}）は
 * <b>{@code rows} の値</b>（{@code $defs.table_data} ／ {@code $defs.list_map_data} の
 * {@code properties.rows.items.additionalProperties.type}、および
 * {@code $defs.record_fragment.properties.rows.items.items.type}）を {@code ["string","null"]} に限るため、
 * <b>そこに</b>引用符なしで {@code true} / {@code 123} / {@code 1.50} / {@code .inf} / {@code .nan} と書くと
 * スキーマ違反で中間モデルへ到達しない。これらは仕様外の入力であり担保対象にしない
 * （例外で止まるため黙って壊れることもない）。本クラスが扱うのは
 * <b>{@code rows} の値としてスキーマを通るスカラー記法だけ</b>である。
 * </p>
 *
 * <p>
 * <b>この線引きは {@code rows} の値にのみ当てはまる。</b>スキーマの他のプロパティは別の型を課しており、
 * たとえば {@code $defs.field_def.properties.length} は
 * {@code anyOf: [{type: integer, minimum: 0}, {type: string, pattern: "^([0-9]+|-)$"}]} であって
 * description も「integer 記法（10）も文字列記法（"10"）もどちらも有効」と明記している。
 * すなわち引用符なしの {@code 123} が一律に仕様外なのではない
 * （{@code length} の integer 記法は {@link YamlFormatReaderRealFileTest#readsIntegerLengthNotationAsString} が担保する）。
 * </p>
 *
 * <p>
 * 各テストの Javadoc には、そのテストが担保する軸要素の ID（D2-01〜D2-12）を記す
 * （{@code YamlFormatReaderRealFileTest} ／ {@code YamlFormatReaderInvalidInputTest} と同じ書き方）。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>期待される仕様ではない。
 * 妥当でないと判断した挙動は {@code .rn/ntf-test-data-converter/coverage/issues.md} に課題として
 * 記録してあり、実装（src/main）は変更していない（{@code YML-01}）。
 * </p>
 *
 * @author kiyobot
 */
public class YamlFormatReaderScalarTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** 実 {@link YamlFormatReader} を通すため、{@link YamlLoader} の LRU キャッシュをテスト間で残さない。 */
    @After
    public void clearLoaderCache() {
        YamlLoader.clearCacheForTest();
    }

    // ------------------------------------------------------------------ helpers

    /**
     * フィクスチャ {@code .yaml} の出力先ディレクトリ。読み書きとも本メソッドだけを使う。
     *
     * @return ディレクトリ
     */
    private Path dir() {
        return folder.getRoot().toPath();
    }

    /**
     * 検証対象スカラー 1 個を {@code setup_tables} の 1 行 1 カラム（{@code V}）に置いた実 {@code .yaml} を
     * 書き出し、中間モデルへ入った値を返す。
     *
     * <p>
     * 値を書かない場合は本メソッドではなく {@link #readOmittedValue()} を、
     * ブロックスカラー（{@code |} ／ {@code >}）の場合は {@link #readBlockScalarValue} を使う。
     * </p>
     *
     * @param value 検証対象スカラー。{@code "        V: "} に続けて書かれる（行の先頭には
     *              空でない値を持つカラム {@code K} が置かれる。{@link #readValueLine} 参照）
     * @return 中間モデル（{@link TableDataBlock}）の 1 行目 {@code V} 列の値
     */
    private String readValue(String value) {
        return readValueLine(" " + value, Collections.<String>emptyList());
    }

    /**
     * ブロックスカラー（{@code |} ／ {@code >}）で書いた複数行の値を読み、中間モデルへ入った値を返す。
     *
     * <p>
     * <b>継続行のインデントは本メソッドが付ける</b>（半角空白 10 個）。呼び出し側は中身だけを渡す。
     * 必要なインデントは<b>半角空白 9 個以上</b>である（snakeyaml-engine 3.0.1 で実測。8 個は
     * {@code ScannerException}、7 個は {@code ParserException}、6 個は {@code ScannerException} になる）。
     * キー {@code V} が {@code "        V:"}（空白 8 個）の <b>9 桁目</b>から始まり、
     * YAML のブロックスカラーはそれより深いインデントを要求するためである。
     * </p>
     *
     * <p>
     * インデント量そのものは値に現れない。ブロックスカラーの content indentation は
     * <b>最初の非空行のインデント</b>で決まり（YAML 1.2 §8.1.1.1）、以降の行はそれ以上のインデントを要する。
     * </p>
     *
     * @param header       ブロックスカラーの記法（{@code "|"} ／ {@code ">"}）
     * @param contentLines 継続行の中身（インデントを含めない）
     * @return 中間モデル（{@link TableDataBlock}）の 1 行目 {@code V} 列の値
     */
    private String readBlockScalarValue(String header, String... contentLines) {
        List<String> indented = new ArrayList<>(contentLines.length);
        for (String line : contentLines) {
            indented.add("          " + line);
        }
        return readValueLine(" " + header, indented);
    }

    /**
     * 値を書かない行（{@code "      - V:"}）だけを持つ実 {@code .yaml} を書き出し、
     * 中間モデルへ入った値を返す。
     *
     * <p>
     * {@link #readValue} と分けているのは、「値なし」を空文字の引数で表すと呼び出し側から意図が読めないためである。
     * </p>
     *
     * @return 中間モデル（{@link TableDataBlock}）の 1 行目 {@code V} 列の値
     */
    private String readOmittedValue() {
        return readValueLine("", Collections.<String>emptyList());
    }

    /**
     * {@code "      - V:"} に続く文字列と後続行から実 {@code .yaml} を組み立てて読み、
     * 中間モデルへ入った値を返す。
     *
     * <p>
     * <b>行の先頭に空でない値を持つカラム {@code K} を必ず置く。</b>検証対象が空文字のとき、
     * 行の値がすべて空文字だと行ごと読み飛ばされてしまうためである
     * （記法。
     * この読み飛ばしそのものは {@link #skipsRowWhoseValuesAreAllEmpty} が押さえる）。
     * 取り出す値は従来どおり {@code V} 列である。
     * </p>
     *
     * @param firstLineTail  {@code "        V:"} の直後に書く文字列（値なしのときは空文字）
     * @param followingLines 後続行（ブロックスカラーの継続行。インデントも含む）
     * @return 中間モデル（{@link TableDataBlock}）の 1 行目 {@code V} 列の値
     */
    private String readValueLine(String firstLineTail, List<String> followingLines) {
        StringBuilder yaml = new StringBuilder()
                .append("setup_tables:\n")
                .append("  - table: \"T\"\n")
                .append("    rows:\n")
                .append("      - K: \"x\"\n")
                .append("        V:")
                .append(firstLineTail)
                .append('\n');
        for (String line : followingLines) {
            yaml.append(line).append('\n');
        }
        TestDataContainer container = YamlFixture.read(dir(), yaml.toString());
        TableDataBlock table = YamlFixture.onlyBlock(container, TableDataBlock.class);
        assertThat(table.getColumnNames(), is(Arrays.asList("K", "V")));
        assertThat(table.getRows().size(), is(1));
        return table.getRows().get(0).get(1);
    }

    /**
     * 検証対象スカラー 1 個を {@code list_maps} の 1 行 1 カラム（{@code V}）に置いた実 {@code .yaml} を
     * 書き出し、中間モデルへ入った値を返す。{@link #readValue} と同じケースを LIST_MAP 経路で通す。
     *
     * <p>
     * {@link #readValueLine} と同じ理由で、行の先頭に空でない値を持つカラム {@code K} を必ず置く。
     * </p>
     *
     * @param value 検証対象スカラー（YAML 記法のまま）
     * @return 中間モデル（{@link ListMapBlock}）の 1 行目 {@code V} 列の値
     */
    private String readListMapValue(String value) {
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "list_maps:\n"
                + "  - id: \"lm\"\n"
                + "    rows:\n"
                + "      - K: \"x\"\n"
                + "        V: " + value + "\n");
        ListMapBlock block = YamlFixture.onlyBlock(container, ListMapBlock.class);
        assertThat(block.getColumnNames(), is(Arrays.asList("K", "V")));
        assertThat(block.getRows().size(), is(1));
        return block.getRows().get(0).get(1);
    }

    /**
     * 検証対象スカラー 1 個をレコード断片（{@code setup_files} の {@code records[].rows}）の
     * 1 行 1 フィールドに置いた実 {@code .yaml} を書き出し、中間モデルへ入った値を返す。
     * {@link #readValue} と同じケースをレコード断片経路で通す。
     *
     * <p>
     * レコード断片の {@code rows} は<b>配列の配列</b>であり、スキーマも別パス
     * （{@code $defs.record_fragment.properties.rows.items.items.type}）で型を課す。
     * </p>
     *
     * @param value 検証対象スカラー（YAML 記法のまま）
     * @return 中間モデル（{@link RecordLayout}）の 1 行目 1 フィールド目の値
     */
    private String readRecordFragmentValue(String value) {
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"V\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [" + value + "]\n");
        FileDataBlock block = YamlFixture.onlyBlock(container, FileDataBlock.class);
        assertThat(block.getRecords().size(), is(1));
        RecordLayout record = block.getRecords().get(0);
        assertThat(record.getRows().size(), is(1));
        return record.getRows().get(0).get(0);
    }

    // ------------------------------------------------------------------ D2-01・D2-02

    /**
     * Given: 引用符なしの文字列 {@code abc}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code "abc"} が入る。
     *
     * <p>担保する軸要素: D2-01（引用符なし文字列）。</p>
     */
    @Test
    public void readsUnquotedStringAsIs() {
        assertThat(readValue("abc"), is("abc"));
    }

    /**
     * Given: 二重引用符付きの文字列 {@code "abc"}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 引用符は残らず {@code "abc"} が入る（引用符なしと同値）。
     *
     * <p>担保する軸要素: D2-02（引用符あり文字列・二重）。</p>
     */
    @Test
    public void readsDoubleQuotedStringWithoutQuotes() {
        assertThat(readValue("\"abc\""), is("abc"));
    }

    /**
     * Given: 一重引用符付きの文字列 {@code 'abc'}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 引用符は残らず {@code "abc"} が入る（二重引用符と同値）。
     *
     * <p>担保する軸要素: D2-02（引用符あり文字列・一重）。</p>
     */
    @Test
    public void readsSingleQuotedStringWithoutQuotes() {
        assertThat(readValue("'abc'"), is("abc"));
    }

    // ------------------------------------------------------------------ D2-03・D2-04

    /**
     * Given: 引用符付きの数値 {@code "123"}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 文字列 {@code "123"} が入る（数値へ解決されない）。
     *
     * <p>
     * 引用符<b>なし</b>の {@code 123} は整数へ解決され、<b>{@code rows} の値としては</b>スキーマ違反になるため
     * 対象外である（{@link YamlFormatReaderInvalidInputTest} が扱う軸F の範囲でもない ——
     * 仕様外の入力として固定しないことが確定している）。他のプロパティでは事情が異なり、
     * {@code field_def.length} は integer 記法も許す（クラス Javadoc 参照）。
     * </p>
     *
     * <p>担保する軸要素: D2-03（引用符付き数値）。</p>
     */
    @Test
    public void readsQuotedNumberAsString() {
        assertThat(readValue("\"123\""), is("123"));
    }

    /**
     * Given: 引用符付きの末尾ゼロ小数 {@code "1.50"}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 末尾ゼロを保った文字列 {@code "1.50"} が入る。
     *
     * <p>担保する軸要素: D2-04（引用符付き末尾ゼロ小数）。</p>
     */
    @Test
    public void readsQuotedTrailingZeroDecimalAsString() {
        assertThat(readValue("\"1.50\""), is("1.50"));
    }

    // ------------------------------------------------------------------ D2-05

    /**
     * Given: 引用符付きの {@code "true"}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 文字列 {@code "true"} が入る。
     *
     * <p>担保する軸要素: D2-05（真偽値に見える文字列・引用符あり）。</p>
     */
    @Test
    public void readsQuotedTrueAsString() {
        assertThat(readValue("\"true\""), is("true"));
    }

    /**
     * Given: 引用符なしの {@code TRUE}（大文字）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 文字列 {@code "TRUE"} が入る。
     *
     * <p>
     * 真偽値として解決されるのは小文字の {@code true} / {@code false} だけである
     * （{@code org.snakeyaml.engine.v2.resolver.JsonScalarResolver} の {@code BOOL} が
     * {@code ^(?:true|false)$}）。そのため {@code TRUE} は引用符なしでもスキーマを通る。
     * </p>
     *
     * <p>担保する軸要素: D2-05（真偽値に見える文字列・TRUE）。</p>
     */
    @Test
    public void readsUppercaseTrueAsString() {
        assertThat(readValue("TRUE"), is("TRUE"));
    }

    /**
     * Given: 引用符なしの {@code yes}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 文字列 {@code "yes"} が入る（YAML 1.1 の真偽値変換は行われない）。
     *
     * <p>担保する軸要素: D2-05（真偽値に見える文字列・yes）。</p>
     */
    @Test
    public void readsYesAsString() {
        assertThat(readValue("yes"), is("yes"));
    }

    // ------------------------------------------------------------------ D2-06（NULL）

    /**
     * Given: 引用符なしの {@code null}（小文字）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : Java の {@code null} が入る。
     *
     * <p>担保する軸要素: D2-06（NULL・引用符なし null）。</p>
     */
    @Test
    public void readsUnquotedNullAsJavaNull() {
        assertThat(readValue("null"), is(nullValue()));
    }

    /**
     * Given: 値を書かない行 {@code - V:}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : Java の {@code null} が入る（引用符なし {@code null} と同値）。
     *
     * <p>担保する軸要素: D2-06（NULL・値なし）。</p>
     */
    @Test
    public void readsOmittedValueAsJavaNull() {
        assertThat(readOmittedValue(), is(nullValue()));
    }

    // ------------------------------------------------------------------ D2-07（NULL に見える文字列）

    /**
     * Given: 引用符付きの {@code "null"}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 文字列 {@code "null"} が入る（Java の {@code null} にはならない）。
     *
     * <p>担保する軸要素: D2-07（NULL に見える文字列・"null"）。</p>
     */
    @Test
    public void readsQuotedNullAsString() {
        assertThat(readValue("\"null\""), is("null"));
    }

    /**
     * Given: 引用符なしの {@code ~}（YAML の標準的な null 記法）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>Java の {@code null} ではなく文字列 {@code "~"}</b> が入る。
     *
     * <p>
     * 引用符なし {@code null} が Java {@code null} になるのに {@code ~} は文字列になる、という非対称は
     * {@code coverage/issues.md} の <b>YML-01</b> に記録した（帰属は converter ではなく yaml 側）。
     * ここでは現状の挙動として固定するにとどめる。
     * </p>
     *
     * <p>担保する軸要素: D2-07（NULL に見える文字列・~）。</p>
     */
    @Test
    public void readsTildeAsString() {
        assertThat(readValue("~"), is("~"));
    }

    /**
     * Given: 引用符なしの {@code NULL}（大文字）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>Java の {@code null} ではなく文字列 {@code "NULL"}</b> が入る。
     *
     * <p>担保する軸要素: D2-07（NULL に見える文字列・NULL）。</p>
     */
    @Test
    public void readsUppercaseNullAsString() {
        assertThat(readValue("NULL"), is("NULL"));
    }

    // ------------------------------------------------------------------ D2-08

    /**
     * Given: 引用符なしの日付風文字列 {@code 2026-08-07}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 記法どおりの文字列が入る（日付型へ解決されない）。
     *
     * <p>担保する軸要素: D2-08（日付風文字列）。</p>
     */
    @Test
    public void readsDateLikeStringAsIs() {
        assertThat(readValue("2026-08-07"), is("2026-08-07"));
    }

    /**
     * Given: 引用符なしの日時風文字列 {@code 2026-08-07T12:34:56}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 記法どおりの文字列が入る（日時型へ解決されない）。
     *
     * <p>担保する軸要素: D2-08（日時風文字列）。</p>
     */
    @Test
    public void readsDateTimeLikeStringAsIs() {
        assertThat(readValue("2026-08-07T12:34:56"), is("2026-08-07T12:34:56"));
    }

    // ------------------------------------------------------------------ D2-09（複数行）

    /**
     * Given: リテラルブロックスカラー（{@code |}）で書いた 2 行。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 改行を保った 1 値になり、<b>末尾に改行が付く</b>（{@code "l1\nl2\n"}）。
     *
     * <p>担保する軸要素: D2-09（複数行・リテラル）。</p>
     */
    @Test
    public void readsLiteralBlockScalarKeepingNewlines() {
        assertThat(readBlockScalarValue("|", "l1", "l2"), is("l1\nl2\n"));
    }

    /**
     * Given: フォールドブロックスカラー（{@code >}）で書いた 2 行。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 行間の改行が半角空白へ畳まれ、<b>末尾に改行が付く</b>（{@code "l1 l2\n"}）。
     *
     * <p>担保する軸要素: D2-09（複数行・フォールド）。</p>
     */
    @Test
    public void readsFoldedBlockScalarFoldingNewlinesIntoSpaces() {
        assertThat(readBlockScalarValue(">", "l1", "l2"), is("l1 l2\n"));
    }

    // ------------------------------------------------------------------ D2-10

    /**
     * Given: 引用符なしの先頭ゼロ数値 {@code 007}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 先頭ゼロを保った文字列 {@code "007"} が入る。
     *
     * <p>
     * 整数へ解決されるのは {@code ^-?(0|[1-9][0-9]*)$}（{@code JsonScalarResolver} の {@code INT}）に
     * 一致する記法だけであり、先頭ゼロを持つ {@code 007} は一致しないため文字列のままスキーマを通る。
     * </p>
     *
     * <p>担保する軸要素: D2-10（先頭ゼロ）。</p>
     */
    @Test
    public void readsLeadingZeroNumberAsString() {
        assertThat(readValue("007"), is("007"));
    }

    /**
     * Given: 引用符なしの 16 進記法 {@code 0x1F}（JSON 数値記法ではない）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 記法どおりの文字列 {@code "0x1F"} が入る。
     *
     * <p>担保する軸要素: D2-10（非 JSON 数値記法）。</p>
     */
    @Test
    public void readsHexNotationAsString() {
        assertThat(readValue("0x1F"), is("0x1F"));
    }

    // ------------------------------------------------------------------ D2-11

    /**
     * Given: 引用符付きの空文字 {@code ""}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 空文字が入る（値なし＝Java {@code null} とは区別される）。
     *
     * <p>担保する軸要素: D2-11（空文字）。</p>
     */
    @Test
    public void readsEmptyStringAsIs() {
        assertThat(readValue("\"\""), is(""));
    }

    /**
     * Given: 引用符付きの前後空白を持つ文字列 {@code "  pad  "}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 前後空白が保たれる（トリムされない）。
     *
     * <p>担保する軸要素: D2-11（前後空白）。</p>
     */
    @Test
    public void readsSurroundingWhitespacePreserved() {
        assertThat(readValue("\"  pad  \""), is("  pad  "));
    }

    // ------------------------------------------------------------------ D2-12

    /**
     * Given: コロンと空白を含む文字列 {@code "a: b"}（引用符が無ければマッピングと解釈される記法）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code "a: b"} が 1 つの値として入る。
     *
     * <p>担保する軸要素: D2-12（コロンを含む文字列）。</p>
     */
    @Test
    public void readsColonContainingStringAsIs() {
        assertThat(readValue("\"a: b\""), is("a: b"));
    }

    /**
     * Given: {@code #} を含む文字列 {@code "a #b"}（引用符が無ければコメントとして落ちる記法）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code "a #b"} が 1 つの値として入る（コメントとして切り落とされない）。
     *
     * <p>担保する軸要素: D2-12（# を含む文字列）。</p>
     */
    @Test
    public void readsHashContainingStringAsIs() {
        assertThat(readValue("\"a #b\""), is("a #b"));
    }

    // ------------------------------------------------------------------ 空エントリの読み飛ばし

    /**
     * Given: 空マッピング {@code {}} の行と、すべての値が空文字 {@code ""} の行を含む {@code setup_tables}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>空マッピングの行だけ</b>が読み飛ばされ、すべての値が空文字の行は残る。
     *
     * <p>
     * 読み飛ばされる「記法として空のエントリ」は、YAML 形式では {@code rows:} 内の要素が
     * 空マッピング（{@code {}}）の場合だけである。{@code ""} と書いた空文字は値であり、
     * すべての値が {@code ""} のエントリは読み飛ばされず、全カラムが空文字のエントリとして読み込まれる。
     * </p>
     *
     * <p>
     * <b>この読み飛ばしがあるため、軸D の各ケースを測るヘルパ（{@link #readValueLine}・
     * {@link #readListMapValue}）は行の先頭に空でない値を持つカラム {@code K} を置いている。</b>
     * 本テストは、その回避が回避している当の規則そのものを押さえる。
     * </p>
     */
    @Test
    public void skipsRowWhoseValuesAreAllEmpty() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - K: \"x\"\n"
                + "        V: \"1\"\n"
                + "      - {}\n"
                + "      - K: \"\"\n"
                + "        V: \"\"\n");

        // Then
        TableDataBlock table = YamlFixture.onlyBlock(container, TableDataBlock.class);
        assertThat(table.getColumnNames(), is(Arrays.asList("K", "V")));
        assertThat("読み飛ばされるのは空マッピングの行だけ。全値が空文字の行は残る",
                table.getRows(), is(Arrays.asList(Arrays.asList("x", "1"), Arrays.asList("", ""))));
    }

    // ------------------------------------------------------------------ 経路差の確認（D2-06・D2-11 のみ）

    /*
     * 上の 12 ケースはすべて setup_tables の rows で測っている。YamlFormatReader の行値の取り出しは
     * テーブル／LIST_MAP／レコード断片の 3 系統があり、スキーマも別パスで型を課すため、
     * 「12 ケースの結果が他の 2 系統でも同じか」は上のテストだけでは分からない。
     * 以下は D2-06（null）と D2-11（空文字）の 2 ケースだけを別経路で測り、経路差が無いことを固定する。
     * 12 ケース × 3 経路には広げない（軸D の 12 ケース定義は setup_tables 経路のまま変えない）。
     * 実測の結果、3 経路とも同じ値が入った。
     *
     * ただしレコード断片経路の空文字だけは、固定できる性質が他より弱い。この経路では
     * 「書かれた空文字」と「要素数が足りず器が埋めた空文字」が中間モデル上で区別できないため
     * （issues.md YML-05）、書いた "" が保たれたことをテストで示すことはできない。
     * 示せるのは「"" は Java null にはならない」ことまでである。
     * 該当テスト readsEmptyStringAsIsInRecordFragmentPath の Javadoc に同じ但し書きを置いた。
     */

    /**
     * Given: 引用符なしの {@code null} を {@code list_maps} の行値に置いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : Java の {@code null} が入る（{@code setup_tables} 経路と同じ）。
     *
     * <p>担保する軸要素: D2-06 を LIST_MAP 経路で確認したもの（同一ケース・別経路）。</p>
     */
    @Test
    public void readsUnquotedNullAsJavaNullInListMapPath() {
        assertThat(readListMapValue("null"), is(nullValue()));
    }

    /**
     * Given: 引用符付きの空文字 {@code ""} を {@code list_maps} の行値に置いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 空文字が入る（{@code setup_tables} 経路と同じ。Java {@code null} とは区別される）。
     *
     * <p>担保する軸要素: D2-11 を LIST_MAP 経路で確認したもの（同一ケース・別経路）。</p>
     */
    @Test
    public void readsEmptyStringAsIsInListMapPath() {
        assertThat(readListMapValue("\"\""), is(""));
    }

    /**
     * Given: 引用符なしの {@code null} をレコード断片の唯一のフィールドの値に置いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>空文字</b>が入る。末尾のフィールドに {@code null} と書いた場合は、形式によらず空文字になる。
     *
     * <p>
     * {@code setup_tables} ／ {@code list_maps} 経路（{@link #readsUnquotedNullAsJavaNull} ／
     * {@link #readsUnquotedNullAsJavaNullInListMapPath}）とは結果が違う。レコード断片には
     * 「末尾のフィールド」という位置の概念があり、そこに書いた {@code null} は空文字になるためである。
     * 唯一のフィールドは常に末尾である。
     * </p>
     *
     * <p>
     * 後ろに空文字でも {@code null} でもないフィールドがあるときに {@code null} のまま残ることは
     * {@code YamlFrameworkAlignmentTest#keepsNonTrailingNullAsJavaNullInRecordFragment} が固定する。
     * </p>
     *
     * <p>担保する軸要素: D2-06 をレコード断片経路で確認したもの（同一ケース・別経路）。</p>
     */
    @Test
    public void readsTrailingUnquotedNullAsEmptyStringInRecordFragmentPath() {
        assertThat(readRecordFragmentValue("null"), is(""));
    }

    /**
     * Given: 引用符付きの空文字 {@code ""} をレコード断片の行値に置いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 空文字が入る（Java {@code null} とは区別される）。
     *
     * <p>
     * <b>本テストが固定できるのは「{@code ""} は Java {@code null} にならない」ことまでである。</b>
     * レコード断片経路では、行の要素数が {@code fields} の件数に足りないときに欠けた位置が
     * <b>空文字で埋められる</b>ため（{@code coverage/issues.md} <b>YML-05</b>。固定テストは
     * {@link YamlFormatReaderInvalidInputTest#fillsMissingRecordFragmentValuesWithEmptyStringInsteadOfNull}）、
     * 「書かれた {@code ""}」と「書かれなかった位置」が同じ {@code ""} になる。実測でも
     * {@code rows: - [""]} と {@code rows: - []} は同じ結果になった。
     * すなわち「書いた空文字が保たれた」ことは本テストでは示せない
     * （{@code setup_tables} ／ {@code list_maps} 経路では欠けたキーが {@code null} になるため示せる。
     * {@link YamlFormatReaderInvalidInputTest#padsColumnMissingFromSecondRowWithNullInTable}）。
     * </p>
     *
     * <p>担保する軸要素: D2-11 をレコード断片経路で確認したもの（同一ケース・別経路。上の但し書きの範囲で）。</p>
     */
    @Test
    public void readsEmptyStringAsIsInRecordFragmentPath() {
        assertThat(readRecordFragmentValue("\"\""), is(""));
    }
}
