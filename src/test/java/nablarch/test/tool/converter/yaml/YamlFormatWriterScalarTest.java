package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 中間モデル → YAML の書き出しについて、値の表現 — 中間モデルの値が YAML へどの記法で書かれるかと、
 * それを YAML → 中間モデルで読み戻したときに元の文字列へ戻るかを固定するテスト。
 *
 * <p>
 * 対象は値の表現 9 ケース
 * （{@code "100"}／{@code "true"}／{@code "null"}／{@code null}／
 * {@code ""}／{@code "007"}／改行含む／{@code "2026-08-07"}／
 * コロン・ハイフン・{@code #} 含む）である。
 * </p>
 *
 * <p>
 * <b>本クラスが担うのは、既存の {@code YamlFormatWriterTest} が通していない分だけである。</b>
 * {@code "null"}／{@code null}（{@code YamlFormatWriterTest#serialize_distinguishesNullFromNullString} が記法を、
 * {@code #roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader} が往復を通す）と
 * {@code ""}（{@code #serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation} が記法を、
 * {@code #roundTrip_table_isPreservedThroughRealReader} が往復を通す）、および改行含むの記法
 * （{@code #serialize_escapesQuotesBackslashAndControlChars}）は既に担保があるため重複させない。
 * </p>
 *
 * <p>
 * <b>アサートは「値が取れる」ではなく出力 YAML の記法そのもの</b>（引用符の有無・折り返し・
 * NULL 表現）に対して行う。{@link YamlFormatWriter} は全値をダブルクォートで囲む方針であり、
 * その方針が効いていることは記法を見なければ確かめられないためである。
 * </p>
 *
 * <p>
 * 往復（{@code write} → 実 {@link YamlFormatReader}）は<b>実ファイルを経由する</b>。
 * すなわち YAML の書き出しと読み取り（スカラー解決・スキーマ検証）の双方を実行する。
 * 読み取り側の単独の担保は {@code YamlFormatReaderScalarTest}（YAML スカラー 12 ケース）にある。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>期待される仕様ではない。
 * 妥当でないと判断した挙動（折り返しの起きるキーを書き出すと YAML として読めなくなる）もあるが、
 * 実装（src/main）は変更していない。理由は {@link #foldsLongEscapedKeyWithBackslashContinuation} に記す。
 * </p>
 *
 * @author kiyobot
 */
public class YamlFormatWriterScalarTest {

    /**
     * 折り返しの起きる長さの、改行を含む値。
     *
     * <p>
     * snakeyaml-engine の出力幅（既定 80 桁）を超え、かつエスケープを要する文字（改行）を含むと、
     * ダブルクォートスカラーが行末の {@code \} で折り返される（実測）。長さだけでは折り返されない
     * （エスケープを要する文字を 1 つも含まない 300 文字の値は 1 行のまま。実測）。
     * </p>
     */
    private static final String FOLDED_VALUE =
            "aaaa bbbb cccc dddd eeee ffff gggg hhhh\niiii jjjj kkkk llll mmmm nnnn oooo pppp qqqq";

    /** {@link #FOLDED_VALUE} と同じ形をカラム名（＝キー）に置いたもの。 */
    private static final String FOLDED_KEY =
            "AAAA BBBB CCCC DDDD EEEE FFFF GGGG HHHH\nIIII JJJJ KKKK LLLL MMMM NNNN OOOO PPPP QQQQ";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final YamlFormatWriter writer = new YamlFormatWriter();

    /** 往復で実 {@link YamlFormatReader} を通すため、{@link YamlLoader} の LRU キャッシュをテスト間で残さない。 */
    @After
    public void clearLoaderCache() {
        YamlLoader.clearCacheForTest();
    }

    // ------------------------------------------------------------------ helpers

    /**
     * 検証対象の値 1 個を {@code setup_tables} の 1 行 1 カラム（{@code V}）に置いたブロックを組み立てる。
     *
     * <p>
     * カラム名を大文字 {@code V} にしているのは、テーブル系のカラム名が読み戻しで大文字化されるため
     * （大文字化するのは本体の {@code TableData}）。小文字にすると往復判定が「値が戻るか」ではなく
     * 「カラム名が戻るか」に引きずられる。カラム名側の挙動は
     * {@code YamlFormatWriterModelTest#uppercasesTableAndColumnNamesWhenReadBack} が固定する。
     * </p>
     *
     * @param value 値（{@code null} 可）
     * @return ブロック
     */
    private static TableDataBlock block(String value) {
        return block("V", value);
    }

    /**
     * 検証対象の値 1 個を {@code setup_tables} の 1 行 1 カラムに置いたブロックを組み立てる。
     *
     * @param column カラム名
     * @param value  値（{@code null} 可）
     * @return ブロック
     */
    private static TableDataBlock block(String column, String value) {
        return new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                Collections.singletonList(column),
                Collections.singletonList(Collections.singletonList(value)));
    }

    /**
     * 値 1 個を持つセクションを直列化する。
     *
     * @param value 値（{@code null} 可）
     * @return YAML テキスト
     */
    private String serializeValue(String value) {
        return writer.serialize(section(block(value)));
    }

    /**
     * ブロックを 1 セクションに包む。
     *
     * @param block ブロック
     * @return セクション
     */
    private static TestDataSection section(TestDataBlock block) {
        return new TestDataSection("td", Collections.singletonList(block));
    }

    /**
     * 期待する出力 YAML を組み立てる（{@code setup_tables} 1 ブロック 1 行 1 カラム）。
     *
     * @param scalar {@code "      - V: "} に続けて書かれるはずのスカラー記法
     * @return YAML テキスト
     */
    private static String expected(String scalar) {
        return ""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - \"V\": " + scalar + "\n";
    }

    /**
     * 値 1 個を実ファイルへ書き出し、本番配線の {@link YamlFormatReader} で読み戻した値を返す。
     *
     * @param value 値（{@code null} 可）
     * @return 読み戻した値
     */
    private String writeAndReadBack(String value) {
        String base = folder.getRoot().getAbsolutePath();
        writer.write(new TestDataContainer("td", Collections.singletonList(section(block(value)))), base);
        TableDataBlock table = YamlFixture.onlyBlock(
                new YamlFormatReader().read(base, "td"), TableDataBlock.class);
        assertThat("読み戻したカラム名", table.getColumnNames(), is(Arrays.asList("V")));
        assertThat("読み戻した行数", table.getRows().size(), is(1));
        return table.getRows().get(0).get(0);
    }

    // ------------------------------------------------------------------ キー

    /** 引用符で囲まないと文字列以外（null・真偽値・数値）として読まれるキーと、文字列のままのキー。 */
    private static final List<String> NON_STRING_LOOKING_KEYS =
            Arrays.asList("null", "true", "false", "1", "1.5", "1e3", "001");

    /**
     * {@link #NON_STRING_LOOKING_KEYS} をキーに持つ {@code LIST_MAP} を組み立てる。値はキーごとに変える。
     *
     * @return ブロック
     */
    private static ListMapBlock nonStringLookingKeyBlock() {
        List<String> values = Arrays.asList("a", "b", "c", "d", "e", "f", "g");
        return new ListMapBlock("", "lm", NON_STRING_LOOKING_KEYS, Collections.singletonList(values));
    }

    /**
     * Given: キーが {@code null}・{@code true}・{@code false}・{@code 1}・{@code 1.5}・{@code 1e3}・
     *        {@code 001} の {@code LIST_MAP}。
     * When : {@code serialize}。
     * Then : どのキーもダブルクォートで囲んで書く。
     *
     * <p>
     * 担保する観点: 利用者が書くキーは、値と同じく常に囲むこと。囲まないと、{@code null} は null、
     * {@code true}・{@code false} は真偽値、{@code 1}・{@code 1.5}・{@code 1e3} は数値として読まれる。
     * </p>
     */
    @Test
    public void writesNonStringLookingKeysAsDoubleQuotedScalars() {
        assertThat(writer.serialize(section(nonStringLookingKeyBlock())), is(""
                + "list_maps:\n"
                + "  - id: \"lm\"\n"
                + "    rows:\n"
                + "      - \"null\": \"a\"\n"
                + "        \"true\": \"b\"\n"
                + "        \"false\": \"c\"\n"
                + "        \"1\": \"d\"\n"
                + "        \"1.5\": \"e\"\n"
                + "        \"1e3\": \"f\"\n"
                + "        \"001\": \"g\"\n"));
    }

    /**
     * Given: {@link #writesNonStringLookingKeysAsDoubleQuotedScalars} と同じ {@code LIST_MAP}。
     * When : {@code write} → 実 {@link YamlFormatReader} と、本体の YAML の読み込みで読み戻す。
     * Then : どちらでも元のキーと値に戻る。
     *
     * <p>
     * 囲まずに書いていたときは、{@code null} がスキーマの検証で、{@code true}・{@code false}・{@code 1}・
     * {@code 1.5}・{@code 1e3} が {@code ClassCastException} で、読み込みに失敗していた。
     * </p>
     */
    @Test
    public void restoresNonStringLookingKeysThroughRealReaders() {
        // Given
        String base = folder.getRoot().getAbsolutePath();
        ListMapBlock block = nonStringLookingKeyBlock();

        // When
        writer.write(new TestDataContainer("td", Collections.singletonList(section(block))), base);

        // Then
        ListMapBlock back = YamlFixture.onlyBlock(new YamlFormatReader().read(base, "td"), ListMapBlock.class);
        assertThat("変換ツール: キー", back.getColumnNames(), is(NON_STRING_LOOKING_KEYS));
        assertThat("変換ツール: 値", back.getRows(), is(block.getRows()));
        Map<String, String> expected = new LinkedHashMap<>();
        for (int i = 0; i < NON_STRING_LOOKING_KEYS.size(); i++) {
            expected.put(NON_STRING_LOOKING_KEYS.get(i), block.getRows().get(0).get(i));
        }
        assertThat("本体", YamlFrameworkOracle.listMap(base, "td", "lm"),
                is(Collections.singletonList(expected)));
    }

    // ------------------------------------------------------------------ "100"

    /**
     * Given: 数値に見える文字列 {@code 100}。
     * When : {@code serialize}。
     * Then : ダブルクォート付きの {@code "100"} として書かれる。
     *
     * <p>
     * 担保する観点: {@code "100"}。既存の
     * {@code YamlFormatWriterTest#roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader} は
     * 往復で文字列として戻ることだけを見ており、<b>出力記法をアサートしていない</b>。
     * </p>
     */
    @Test
    public void writesNumberLookingStringAsDoubleQuotedScalar() {
        assertThat(serializeValue("100"), is(expected("\"100\"")));
    }

    /**
     * Given: 数値に見える文字列 {@code 100}。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 文字列 {@code "100"} として復元される（数値にならない）。
     *
     * <p>担保する観点: {@code "100"}（往復）。読み取り側は {@code YamlFormatReaderScalarTest#readsQuotedNumberAsString}。</p>
     */
    @Test
    public void restoresNumberLookingStringThroughRealReader() {
        assertThat(writeAndReadBack("100"), is("100"));
    }

    // ------------------------------------------------------------------ "true"

    /**
     * Given: 真偽値に見える文字列 {@code true}。
     * When : {@code serialize}。
     * Then : ダブルクォート付きの {@code "true"} として書かれる。
     *
     * <p>担保する観点: {@code "true"}。</p>
     */
    @Test
    public void writesBooleanLookingStringAsDoubleQuotedScalar() {
        assertThat(serializeValue("true"), is(expected("\"true\"")));
    }

    /**
     * Given: 真偽値に見える文字列 {@code true}。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 文字列 {@code "true"} として復元される（真偽値にならない）。
     *
     * <p>
     * 担保する観点: {@code "true"}（往復）。読み取り側は {@code YamlFormatReaderScalarTest#readsQuotedTrueAsString}。
     * 引用符を落とすと本体スキーマが {@code rows} の値を {@code ["string","null"]} に限るため
     * 読み戻しがスキーマ違反で止まる（{@code YamlFormatReaderScalarTest} のクラス Javadoc）。
     * すなわちここで確かめているのは「全値クォート方針が往復を成立させている」ことである。
     * </p>
     */
    @Test
    public void restoresBooleanLookingStringThroughRealReader() {
        assertThat(writeAndReadBack("true"), is("true"));
    }

    // ------------------------------------------------------------------ "007"

    /**
     * Given: 先頭ゼロの数値に見える文字列 {@code 007}。
     * When : {@code serialize}。
     * Then : ダブルクォート付きの {@code "007"} として書かれる（先頭ゼロが落ちない）。
     *
     * <p>担保する観点: {@code "007"}。</p>
     */
    @Test
    public void writesLeadingZeroNumberAsDoubleQuotedScalar() {
        assertThat(serializeValue("007"), is(expected("\"007\"")));
    }

    /**
     * Given: 先頭ゼロの数値に見える文字列 {@code 007}。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 文字列 {@code "007"} として復元される。
     *
     * <p>担保する観点: {@code "007"}（往復）。読み取り側は {@code YamlFormatReaderScalarTest#readsLeadingZeroNumberAsString}。</p>
     */
    @Test
    public void restoresLeadingZeroNumberThroughRealReader() {
        assertThat(writeAndReadBack("007"), is("007"));
    }

    // ------------------------------------------------------------------ 改行含む

    /**
     * Given: 改行を含む短い値 {@code "l1\nl2"}。
     * When : {@code serialize}。
     * Then : ブロックスカラー（{@code |} ／ {@code >}）ではなく、<b>1 行のダブルクォートスカラーの中で
     *        {@code \n} へエスケープ</b>して書かれる。
     *
     * <p>
     * 担保する観点: 改行含む。既存の
     * {@code YamlFormatWriterTest#serialize_escapesQuotesBackslashAndControlChars} は
     * {@code \n}／{@code \r}／{@code \t}／{@code \x01}／{@code "}／{@code \} をまとめた 1 つの値で
     * エスケープを固定している。本メソッドは<b>改行だけを含む値でも複数行記法にならない</b>ことを
     * 分けて示す（下の {@code #foldsLongEscapedValueWithBackslashContinuation} が示す折り返しとの対比）。
     * </p>
     */
    @Test
    public void writesNewlineContainingStringAsEscapedSingleLineScalar() {
        assertThat(serializeValue("l1\nl2"), is(expected("\"l1\\nl2\"")));
    }

    /**
     * Given: 改行を含む短い値 {@code "l1\nl2"}。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 改行を含んだまま復元される。
     *
     * <p>
     * 担保する観点: 改行含む（往復）。読み取り側でブロックスカラーを読む担保は
     * {@code YamlFormatReaderScalarTest#readsLiteralBlockScalarKeepingNewlines} にあるが、
     * <b>YAML の書き出しが書くのはブロックスカラーではない</b>ため往復の経路が違う。
     * </p>
     */
    @Test
    public void restoresNewlineContainingStringThroughRealReader() {
        assertThat(writeAndReadBack("l1\nl2"), is("l1\nl2"));
    }

    /**
     * Given: 出力幅（80 桁）を超え、かつ改行を含む値。
     * When : {@code serialize}。
     * Then : ダブルクォートスカラーが行末の {@code \} で折り返され、継続行が現れる。
     *        <b>継続行の字下げは半角空白 2 個で、周囲のインデント（{@code V} は 9 桁目）に揃わない。</b>
     *
     * <p>
     * 担保する観点: 改行含む（複数行記法）。{@link YamlFormatWriter#q} は値を単独で
     * {@code Dump#dumpToString} し、その結果を行へ差し込むだけであるため、折り返しの字下げは
     * 差し込み先の深さを知らない。
     * </p>
     */
    @Test
    public void foldsLongEscapedValueWithBackslashContinuation() {
        assertThat(serializeValue(FOLDED_VALUE), is(""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - \"V\": \"aaaa bbbb cccc dddd eeee ffff gggg hhhh"
                + "\\niiii jjjj kkkk llll mmmm nnnn oooo pppp\\\n"
                + "  \\ qqqq\"\n"));
    }

    /**
     * Given: 出力幅を超え、かつ改行を含む値。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 折り返されていても元の文字列へ復元される。
     *
     * <p>
     * 担保する観点: 改行含む（折り返しの往復）。<b>値側の折り返しは往復を壊さない。</b>
     * 壊れるのはキー側である（{@code #failsToReadBackFoldedKey}）。
     * </p>
     */
    @Test
    public void restoresFoldedLongEscapedValueThroughRealReader() {
        assertThat(writeAndReadBack(FOLDED_VALUE), is(FOLDED_VALUE));
    }

    // ------------------------------------------------------------------ "2026-08-07"

    /**
     * Given: 日付に見える文字列 {@code 2026-08-07}。
     * When : {@code serialize}。
     * Then : ダブルクォート付きの {@code "2026-08-07"} として書かれる。
     *
     * <p>担保する観点: {@code "2026-08-07"}。</p>
     */
    @Test
    public void writesDateLookingStringAsDoubleQuotedScalar() {
        assertThat(serializeValue("2026-08-07"), is(expected("\"2026-08-07\"")));
    }

    /**
     * Given: 日付に見える文字列 {@code 2026-08-07}。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 文字列 {@code "2026-08-07"} として復元される（日付にならない）。
     *
     * <p>担保する観点: {@code "2026-08-07"}（往復）。読み取り側は {@code YamlFormatReaderScalarTest#readsDateLikeStringAsIs}。</p>
     */
    @Test
    public void restoresDateLookingStringThroughRealReader() {
        assertThat(writeAndReadBack("2026-08-07"), is("2026-08-07"));
    }

    // ------------------------------------------------------------------ コロン・ハイフン・#

    /**
     * Given: コロン・ハイフン・{@code #} を含む値 {@code "a: b - c #d"}。
     * When : {@code serialize}。
     * Then : ダブルクォート付きで 1 値として書かれる（コロンでキーに割れず、{@code #} 以降も残る）。
     *
     * <p>
     * 担保する観点: コロン・ハイフン・{@code #} 含む（<b>値側</b>）。キー側のクォートは別の要素であり、本メソッドは見ていない。
     * </p>
     *
     * <p>
     * キー側は {@code #writesNonStringLookingKeysAsDoubleQuotedScalars} と
     * {@code YamlFormatWriterModelTest#quotesDirectiveKeyContainingAnyYamlSpecialOrControlCharacter} が担保する。
     * </p>
     */
    @Test
    public void writesColonHyphenAndHashContainingStringAsDoubleQuotedScalar() {
        assertThat(serializeValue("a: b - c #d"), is(expected("\"a: b - c #d\"")));
    }

    /**
     * Given: コロン・ハイフン・{@code #} を含む値 {@code "a: b - c #d"}。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : そのまま復元される（{@code #} 以降がコメントとして落ちない）。
     *
     * <p>
     * 担保する観点: コロン・ハイフン・{@code #} 含む（往復）。読み取り側は
     * {@code YamlFormatReaderScalarTest#readsColonContainingStringAsIs} ／ {@code #readsHashContainingStringAsIs}。
     * 引用符が落ちれば、引用符なしスカラーの {@code #} 以降がコメントとして落ちるため値が黙って変わる。
     * </p>
     */
    @Test
    public void restoresColonHyphenAndHashContainingStringThroughRealReader() {
        assertThat(writeAndReadBack("a: b - c #d"), is("a: b - c #d"));
    }

    // ------------------------------------------------------------------ 折り返したキー

    /**
     * Given: 出力幅を超え、かつ改行を含むカラム名（＝キー）。
     * When : {@code serialize}。
     * Then : 値と同じく行末の {@code \} で折り返され、<b>キーが 2 行にまたがる</b>。
     *
     * <p>
     * 担保する観点: 折り返したキーの現状挙動の固定。
     * YAML の simple key は 1 行に収まらなければならないため、この出力は読み戻せない
     * （{@code #failsToReadBackFoldedKey}）。修正しない。NTF の記法はキーに使える文字種・長さを定めておらず、
     * 到達には「キーにエスケープ対象の文字を含む」かつ「出力幅（80 桁）を超える」の両方が要り、
     * 踏んだ場合も読み戻しが例外で止まる（黙ってデータが変わることはない）ためである。
     * </p>
     */
    @Test
    public void foldsLongEscapedKeyWithBackslashContinuation() {
        TableDataBlock block = block(FOLDED_KEY, "v");

        assertThat(writer.serialize(section(block)), is(""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - \"AAAA BBBB CCCC DDDD EEEE FFFF GGGG HHHH"
                + "\\nIIII JJJJ KKKK LLLL MMMM NNNN OOOO PPPP\\\n"
                + "  \\ QQQQ\": \"v\"\n"));
    }

    /**
     * Given: 折り返されたキーを含む出力 YAML。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : パースの時点で {@code IllegalStateException}（{@code Failed to parse YAML file: ...}）になる。
     *        スキーマ検証には到達しない。
     *
     * <p>
     * 担保する観点: 折り返したキーの読み戻し。書き出しは成功するため、
     * <b>壊れていることが分かるのは読み戻しのときである</b>。
     * </p>
     */
    @Test
    public void failsToReadBackFoldedKey() {
        // Given
        TableDataBlock block = block(FOLDED_KEY, "v");
        String base = folder.getRoot().getAbsolutePath();
        writer.write(new TestDataContainer("td", Collections.singletonList(section(block))), base);

        // When / Then
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> new YamlFormatReader().read(base, "td"));
        assertThat(thrown.getMessage(), startsWith("Failed to parse YAML file: "));
    }
}
