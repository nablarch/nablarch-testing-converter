package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import nablarch.test.core.db.TableData;
import nablarch.test.core.file.DataFile;
import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.FrameworkOracle;
import nablarch.test.core.reader.YamlFrameworkOracle;
import nablarch.test.tool.converter.model.FieldDef;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.MessageDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;
import nablarch.test.tool.converter.yaml.YamlFormatReader;
import nablarch.test.tool.converter.yaml.YamlFormatWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 特殊記法の母集合を<b>実ファイル起点・4 経路</b>で往復させ、テスティングフレームワークが解釈したあとの
 * 値が保たれることを固定するテスト（指示書「完了条件3」）。
 *
 * <p>
 * <b>{@code RoundTripTest} の代わりにならない理由。</b>{@code RoundTripTest} は中間モデルを起点に
 * 中間モデルへ戻すため（同クラスの Javadoc）、記法⇄値の写像が非対称でも書きと読みが同じ非対称なら
 * 緑になる。本クラスは<b>実ファイル（{@code .xlsx} ／ {@code .yaml}）を起点</b>にし、
 * <b>解釈後の値</b>（セルの見た目ではない）で比べる。
 * </p>
 *
 * <p>
 * <b>母集合</b>:
 * </p>
 * <ul>
 *   <li>特殊記法の一覧 —— Excel 形式 12 種／YAML 形式 12 種。
 *       <b>2 つの一覧は同じ 12 の値の種類を同じ順で並べており</b>、種類ごとに Excel 記法と YAML 記法が
 *       対応する。改行文字だけは CR と LF の 2 形を含むため、本クラスでは 2 つに分けて測る</li>
 *   <li>{@code null}・空文字・改行など特殊な値の記述例 —— 6 つの例が Excel 形式と YAML 形式の
 *       書き方をそれぞれ 1 つずつ持つ</li>
 *   <li>#44 で足した 4 種（末尾の {@code null}／全カラムの値が空文字のエントリ／
 *       マーカーカラムだけに値があるエントリ／アップロードファイルの記述例）</li>
 * </ul>
 *
 * <p>
 * <b>4 経路</b>: XLS→XLS ／ XLS→YAML→XLS ／ YAML→YAML ／ YAML→XLS→YAML。
 * 表の 1 行は Excel 記法と YAML 記法の対で与えられるため、Excel 記法を経路 1・2 へ、
 * YAML 記法を経路 3・4 へ通す。<b>比較の基準は「その形式の実ファイルを最初に読んだときの値」</b>であり、
 * 往復後にその値と一致することを求める。
 * </p>
 *
 * <p>
 * <b>本クラスが置かれている場所について。</b>4 形式にまたがるテストだが、実 {@code .xlsx} を
 * 組み立てる {@link XlsFixture} が {@code xls} パッケージの package private であるため同パッケージに置く。
 * YAML 側は素のテキストなので {@link Files} で書き出している。
 * </p>
 *
 * <p>
 * <b>行が読み飛ばされないよう、検証対象カラム {@code V} の前に空でないカラム {@code K} を必ず置く</b>
 * （記法。空文字のケースで行ごと消えるため）。
 * </p>
 *
 * @author kiyobot
 */
public class SpecialNotationRoundTripTest {

    /** フィクスチャの既定ブック名／シート名／リソース名。 */
    private static final String BOOK = "notationBook";

    /** シート名（YAML 側の読み込み単位名も兼ねる）。 */
    private static final String SHEET = "td";

    /** テーブル名。 */
    private static final String TABLE = "T";

    /** 行が空エントリにならないよう先頭へ置く、空でない値を持つカラムの値。 */
    private static final String GUARD = "g";

    /** 電文の識別子。 */
    private static final String MESSAGE_ID = "M1";

    /** {@code LIST_MAP} の識別子。 */
    private static final String LIST_MAP_ID = "requestParams";

    /** LF。 */
    private static final String LF = "\n";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * 各段のディレクトリへ、{@code ${binaryFile:testdata.bin}} が指すファイルを同じ内容で置く。
     *
     * <p>
     * フレームワークは読み込みのたびに取得元パス起点で {@code ${binaryFile:相対パス}} を解決し、
     * ファイルの内容を 16 進文字列へ写す。往復の前後で同じ値になるためには、どの段のディレクトリにも
     * 同じ内容のファイルが要る。
     * </p>
     */
    @org.junit.Before
    public void placeBinaryFixture() {
        for (int step = 1; step <= 8; step++) {
            try {
                Files.createDirectories(dir(step));
                Files.write(dir(step).resolve("testdata.bin"), new byte[] {0x01, 0x02, 0x03});
            } catch (IOException e) {
                throw new UncheckedIOException("failed to write binary fixture", e);
            }
        }
    }

    // ------------------------------------------------------------------ 経路の実行

    /**
     * 段ごとに別ディレクトリを使う（本体ローダのキャッシュが同一リソース名の 2 回目の読みで
     * 1 回目の結果を返すため）。
     *
     * @param step 段番号
     * @return ディレクトリ
     */
    private Path dir(int step) {
        return folder.getRoot().toPath().resolve("s" + step);
    }

    /**
     * Excel 記法 1 つを {@code SETUP_TABLE} の {@code V} 列に置いた実 {@code .xlsx} を書き出す。
     *
     * @param dir       書き出し先
     * @param excelCell {@code V} 列のセル文字列（{@code null} は空セル）
     */
    private static void writeXls(Path dir, String excelCell) {
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("SETUP_TABLE=" + TABLE))
                .row(text("K"), text("V"))
                .row(text(GUARD), text(excelCell))
                .writeTo(dir);
    }

    /**
     * YAML 記法 1 つを {@code setup_tables} の {@code V} 列に置いた実 {@code .yaml} を書き出す。
     *
     * @param dir        書き出し先
     * @param yamlScalar {@code "        V: "} に続けて書くスカラー（YAML 記法のまま）
     */
    private static void writeYaml(Path dir, String yamlScalar) {
        write(dir, ""
                + "setup_tables:\n"
                + "  - table: \"" + TABLE + "\"\n"
                + "    rows:\n"
                + "      - K: \"" + GUARD + "\"\n"
                + "        V: " + yamlScalar + "\n");
    }

    /**
     * 実 {@code .yaml} を書き出す。
     *
     * @param dir      書き出し先
     * @param yamlText 内容
     */
    private static void write(Path dir, String yamlText) {
        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve(SHEET + ".yaml"), yamlText.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("failed to write fixture: " + dir, e);
        }
    }

    // ------------------------------------------------------------------ 正解の読み手（フレームワーク本体）

    /*
     * 往復の正解は変換ツールのリーダではなく、テスティングフレームワークが読んだ値である。
     * Excel 形式はフレームワーク本体（PoiXlsReader ＋ 単体テストと同順のインタープリタ 3 本）、
     * YAML 形式は nablarch-testing-yaml の YamlTestDataParser に読ませる。
     * 変換ツール自身の 2 つのリーダを突き合わせていると、両方が同じ写し間違いを持つ欠陥
     * （末尾の null が空文字にならない等）を検知できない。
     */

    /** {@code SETUP_TABLE} を本体に読ませる。 */
    private static final Function<Path, String> XLS_SETUP_TABLES =
            d -> dumpTables(FrameworkOracle.tables(d.toString(), BOOK + "/" + SHEET, "", DataType.SETUP_TABLE_DATA));

    /** {@code setup_tables} を YAML 読み込みに読ませる。 */
    private static final Function<Path, String> YAML_SETUP_TABLES =
            d -> dumpTables(YamlFrameworkOracle.setupTables(d.toAbsolutePath().toString(), SHEET));

    /** {@code EXPECTED_TABLE} を本体に読ませる。 */
    private static final Function<Path, String> XLS_EXPECTED_TABLES =
            d -> dumpTables(FrameworkOracle.tables(d.toString(), BOOK + "/" + SHEET, "",
                    DataType.EXPECTED_TABLE_DATA));

    /** {@code expected_tables} を YAML 読み込みに読ませる。 */
    private static final Function<Path, String> YAML_EXPECTED_TABLES =
            d -> dumpTables(YamlFrameworkOracle.expectedTables(d.toAbsolutePath().toString(), SHEET));

    /** {@code SETUP_VARIABLE} を本体に読ませる。 */
    private static final Function<Path, String> XLS_SETUP_VARIABLE =
            d -> dumpFiles(FrameworkOracle.files(d.toString(), BOOK + "/" + SHEET, "", DataType.SETUP_VARIABLE));

    /** {@code setup_files}（可変長）を YAML 読み込みに読ませる。 */
    private static final Function<Path, String> YAML_SETUP_FILES =
            d -> dumpFiles(YamlFrameworkOracle.setupFiles(d.toAbsolutePath().toString(), SHEET));

    /** {@code SETUP_FIXED} を本体に読ませる。 */
    private static final Function<Path, String> XLS_SETUP_FIXED =
            d -> dumpFiles(FrameworkOracle.files(d.toString(), BOOK + "/" + SHEET, "", DataType.SETUP_FIXED));

    /** {@code MESSAGE} を本体に読ませる。 */
    private static final Function<Path, String> XLS_MESSAGE =
            d -> dumpFiles(FrameworkOracle.messageBodies(d.toString(), BOOK + "/" + SHEET, MESSAGE_ID));

    /** {@code messages} を YAML 読み込みに読ませる。 */
    private static final Function<Path, String> YAML_MESSAGE =
            d -> dumpFiles(Collections.singletonList(
                    YamlFrameworkOracle.messageBody(d.toAbsolutePath().toString(), SHEET, MESSAGE_ID)));

    /** {@code LIST_MAP} を本体に読ませる。 */
    private static final Function<Path, String> XLS_LIST_MAP =
            d -> dumpListMap(FrameworkOracle.listMap(d.toString(), BOOK + "/" + SHEET, LIST_MAP_ID));

    /** {@code list_maps} を YAML 読み込みに読ませる。 */
    private static final Function<Path, String> YAML_LIST_MAP =
            d -> dumpListMap(YamlFrameworkOracle.listMap(d.toAbsolutePath().toString(), SHEET, LIST_MAP_ID));

    /**
     * テーブル器の一覧を、突き合わせ用の文字列へ写す。
     *
     * @param tables テーブル器の一覧
     * @return 表現
     */
    private static String dumpTables(List<TableData> tables) {
        StringBuilder sb = new StringBuilder();
        for (TableData table : tables) {
            sb.append(table.getTableName())
                    .append(' ').append(java.util.Arrays.asList(table.getColumnNames()))
                    .append(' ').append(YamlFrameworkOracle.rowsOf(table)).append('\n');
        }
        return sb.toString();
    }

    /**
     * ファイル器の一覧を、突き合わせ用の文字列へ写す。
     *
     * @param files ファイル器の一覧
     * @return 表現
     */
    private static String dumpFiles(List<? extends DataFile> files) {
        StringBuilder sb = new StringBuilder();
        for (DataFile file : files) {
            sb.append(file.getPath())
                    .append(' ').append(nablarch.test.core.file.DataFileInspector.fieldNames(file))
                    .append(' ').append(nablarch.test.core.file.DataFileInspector.values(file)).append('\n');
        }
        return sb.toString();
    }

    /**
     * {@code LIST_MAP} の行一覧を、突き合わせ用の文字列へ写す。
     *
     * @param rows 行一覧
     * @return 表現
     */
    private static String dumpListMap(List<Map<String, String>> rows) {
        List<String> dump = new ArrayList<>();
        for (Map<String, String> row : rows) {
            dump.add(new java.util.TreeMap<>(row).toString());
        }
        return dump.toString();
    }

    // ------------------------------------------------------------------ 変換ツールが読んだ値（突き合わせ対象）

    /**
     * 変換ツールが読んだ中間モデルを、正解の読み手と同じ形の文字列へ写す。
     *
     * <p>
     * 正解（フレームワークが読んだ値）とそのまま突き合わせるためのものである。
     * ブロックの種別ごとに、上の {@code dumpTables} ／ {@code dumpListMap} ／ {@code dumpFiles} と
     * 同じ並べ方をする。
     * </p>
     *
     * @param container 中間モデル
     * @return 表現
     */
    private static String converterDump(TestDataContainer container) {
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        assertThat("ブロック数", blocks.size(), is(1));
        TestDataBlock block = blocks.get(0);
        if (block instanceof ListMapBlock) {
            ListMapBlock listMap =
                    (ListMapBlock) block;
            List<Map<String, String>> rows = new ArrayList<>();
            for (List<String> row : listMap.getRows()) {
                Map<String, String> map = new java.util.LinkedHashMap<>();
                for (int i = 0; i < listMap.getColumnNames().size(); i++) {
                    map.put(listMap.getColumnNames().get(i), row.get(i));
                }
                rows.add(map);
            }
            return dumpListMap(rows);
        }
        if (block instanceof TableDataBlock) {
            TableDataBlock table = (TableDataBlock) block;
            return table.getIdentifier() + ' ' + table.getColumnNames() + ' ' + table.getRows() + '\n';
        }
        List<List<String>> fieldNames = new ArrayList<>();
        List<List<String>> values = new ArrayList<>();
        for (RecordLayout record : recordsOf(block)) {
            List<String> names = new ArrayList<>();
            for (FieldDef field : record.getFields()) {
                names.add(field.getName());
            }
            fieldNames.add(names);
            values.addAll(record.getRows());
        }
        return block.getIdentifier() + ' ' + fieldNames + ' ' + values + '\n';
    }

    /**
     * ファイル系・電文系ブロックのレコードレイアウトを取り出す。
     *
     * @param block ブロック
     * @return レコードレイアウト一覧
     */
    private static List<RecordLayout> recordsOf(TestDataBlock block) {
        if (block instanceof FileDataBlock) {
            return ((FileDataBlock) block).getRecords();
        }
        return ((MessageDataBlock) block).getRecords();
    }

    /**
     * 変換ツールが読んだ中間モデルから、{@code V} 列の値を取り出す。
     *
     * @param dir ディレクトリ
     * @param xls Excel 形式なら真、YAML 形式なら偽
     * @return {@code V} 列の値
     */
    private static String converterValue(Path dir, boolean xls) {
        TestDataContainer container = xls
                ? new XlsFormatReader().read(dir.toString(), BOOK + "/" + SHEET)
                : new YamlFormatReader().read(dir.toAbsolutePath().toString(), SHEET);
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        assertThat("ブロック数", blocks.size(), is(1));
        TableDataBlock table = (TableDataBlock) blocks.get(0);
        int index = table.getColumnNames().indexOf("V");
        assertThat("V 列があること", index >= 0, is(true));
        assertThat("エントリ数", table.getRows().size(), is(1));
        return table.getRows().get(0).get(index);
    }

    /**
     * 変換ツールが実 {@code .xlsx} を読んだ結果を、正解と同じ形の文字列で返す。
     *
     * @param dir ディレクトリ
     * @return 表現
     */
    private static String converterReadXls(Path dir) {
        return converterDump(new XlsFormatReader().read(dir.toString(), BOOK + "/" + SHEET));
    }

    /**
     * 変換ツールが実 {@code .yaml} を読んだ結果を、正解と同じ形の文字列で返す。
     *
     * @param dir ディレクトリ
     * @return 表現
     */
    private static String converterReadYaml(Path dir) {
        return converterDump(new YamlFormatReader().read(dir.toAbsolutePath().toString(), SHEET));
    }

    /**
     * 指定ディレクトリの実 {@code .xlsx} を本体に読ませ、{@code V} 列の解釈後の値を返す。
     *
     * @param dir ディレクトリ
     * @return 解釈後の値
     */
    private static String readXls(Path dir) {
        return onlyValue(FrameworkOracle.tables(dir.toString(), BOOK + "/" + SHEET, "",
                DataType.SETUP_TABLE_DATA));
    }

    /**
     * 指定ディレクトリの実 {@code .yaml} を YAML 読み込みに読ませ、{@code V} 列の解釈後の値を返す。
     *
     * @param dir ディレクトリ
     * @return 解釈後の値
     */
    private static String readYaml(Path dir) {
        return onlyValue(YamlFrameworkOracle.setupTables(dir.toAbsolutePath().toString(), SHEET));
    }

    /**
     * 唯一のテーブルの唯一の行から {@code V} 列の値を取り出す。
     *
     * @param tables テーブル器の一覧
     * @return {@code V} 列の値
     */
    private static String onlyValue(List<TableData> tables) {
        assertThat("テーブル数", tables.size(), is(1));
        TableData table = tables.get(0);
        assertThat("エントリ数", table.size(), is(1));
        Object value = table.getValue(0, "V");
        return value == null ? null : value.toString();
    }

    /**
     * 中間モデルを、既定のブック名／シート名を持つ 1 ブロックのコンテナへ包み直す。
     *
     * @param container 読み込んだコンテナ
     * @return 包み直したコンテナ
     */
    private static TestDataContainer rewrap(TestDataContainer container) {
        TestDataBlock block = container.getSections().get(0).getBlocks().get(0);
        return new TestDataContainer(BOOK,
                Collections.singletonList(new TestDataSection(SHEET, Collections.singletonList(block))));
    }

    /**
     * 表の 1 行（Excel 記法と YAML 記法の対）を 4 経路で往復させ、いずれも解釈後の値が保たれることを
     * 確かめる。
     *
     * @param excelCell  Excel 形式の記法（{@code V} 列のセル文字列）
     * @param yamlScalar YAML 形式の記法（{@code V:} に続けて書くスカラー）
     * @param expected   フレームワークが読む<b>解釈後の値</b>
     */
    private void assertFourRoutes(String excelCell, String yamlScalar, String expected) {
        assertFourRoutes(excelCell, yamlScalar, expected, true);
    }

    /**
     * 表の 1 行を 4 経路で往復させる。
     *
     * @param excelCell            Excel 形式の記法
     * @param yamlScalar           YAML 形式の記法
     * @param expected             フレームワークが読む値
     * @param compareConverterRead 変換ツールが読んだ値を正解と突き合わせるか
     *                             （{@code ${binaryFile:パス}} の行では偽にする）
     */
    private void assertFourRoutes(String excelCell, String yamlScalar, String expected,
                                  boolean compareConverterRead) {
        // 経路 1: XLS → XLS
        writeXls(dir(1), excelCell);
        String fromXls = readXls(dir(1));
        assertThat("Excel 記法の解釈後の値", fromXls, is(expected));
        if (compareConverterRead) {
            assertThat("変換ツールの Excel 読みが本体と一致する", converterValue(dir(1), true), is(fromXls));
        }
        new XlsFormatWriter().write(rewrap(new XlsFormatReader().read(dir(1).toString(), BOOK + "/" + SHEET)),
                dir(2).toString());
        assertThat("XLS→XLS", readXls(dir(2)), is(fromXls));

        // 経路 2: XLS → YAML → XLS
        new YamlFormatWriter().write(rewrap(new XlsFormatReader().read(dir(1).toString(), BOOK + "/" + SHEET)),
                dir(3).toString());
        new XlsFormatWriter().write(rewrap(new YamlFormatReader().read(dir(3).toAbsolutePath().toString(), SHEET)),
                dir(4).toString());
        assertThat("XLS→YAML→XLS", readXls(dir(4)), is(fromXls));

        // 経路 3: YAML → YAML
        writeYaml(dir(5), yamlScalar);
        String fromYaml = readYaml(dir(5));
        assertThat("YAML 記法の解釈後の値", fromYaml, is(expected));
        if (compareConverterRead) {
            assertThat("変換ツールの YAML 読みが本体と一致する", converterValue(dir(5), false), is(fromYaml));
        }
        new YamlFormatWriter().write(rewrap(new YamlFormatReader().read(dir(5).toAbsolutePath().toString(), SHEET)),
                dir(6).toString());
        assertThat("YAML→YAML", readYaml(dir(6)), is(fromYaml));

        // 経路 4: YAML → XLS → YAML
        new XlsFormatWriter().write(rewrap(new YamlFormatReader().read(dir(5).toAbsolutePath().toString(), SHEET)),
                dir(7).toString());
        new YamlFormatWriter().write(rewrap(new XlsFormatReader().read(dir(7).toString(), BOOK + "/" + SHEET)),
                dir(8).toString());
        assertThat("YAML→XLS→YAML", readYaml(dir(8)), is(fromYaml));

        // 2 つの記法が同じ意味を表していること
        assertThat("Excel 記法と YAML 記法が同じ値を表す", fromXls, is(fromYaml));
    }

    // ------------------------------------------------------------------ 特殊記法の 12 種

    /** 種類 1 —— 通常の文字列。 */
    @Test
    public void plainString() {
        assertFourRoutes("abc", "\"abc\"", "abc");
    }

    /** 種類 2 —— null。 */
    @Test
    public void javaNull() {
        assertFourRoutes("null", "null", null);
    }

    /** 種類 3 —— 文字列の null。 */
    @Test
    public void stringNull() {
        assertFourRoutes("\"null\"", "\"null\"", "null");
    }

    /** 種類 4 —— 空文字。 */
    @Test
    public void emptyString() {
        assertFourRoutes("", "\"\"", "");
    }

    /** 種類 5 —— 先頭ゼロ付き数値。 */
    @Test
    public void leadingZeroNumber() {
        assertFourRoutes("001", "\"001\"", "001");
    }

    /** 種類 6 —— {@code true}（文字列）。 */
    @Test
    public void booleanLikeString() {
        assertFourRoutes("true", "\"true\"", "true");
    }

    /** 種類 7 —— 半角スペース 1 文字。 */
    @Test
    public void singleHalfWidthSpace() {
        assertFourRoutes("\" \"", "\" \"", " ");
    }

    /** 種類 8 —— ダブルクォート 1 文字。 */
    @Test
    public void singleDoubleQuote() {
        assertFourRoutes("\"\"\"", "'\"'", "\"");
    }

    /** 種類 9 —— {@code ${systemTime}}。 */
    @Test
    public void systemTimeNotation() {
        assertFourRoutes("${systemTime}", "\"${systemTime}\"", "${systemTime}");
    }

    /**
     * 種類 10 —— {@code ${binaryFile:パス}}。
     *
     * <p>
     * 正解の読み手はフレームワークであり、フレームワークはこの記法を取得元パス起点で解決して
     * ファイルの内容の 16 進文字列にする。{@link #placeBinaryFixture} が置いた 3 バイトが
     * {@code "010203"} になる。変換ツールが記法をそのまま運べていれば、往復の前後で同じ値になる。
     * </p>
     */
    @Test
    public void binaryFileNotation() {
        // 変換ツールはこの記法を解決せず記法のまま運ぶため、読んだ値の突き合わせは行わない。
        assertFourRoutes("${binaryFile:testdata.bin}", "\"${binaryFile:testdata.bin}\"", "010203", false);
    }

    /** 種類 11 —— {@code ${文字種,文字数}}。 */
    @Test
    public void charTypeNotation() {
        assertFourRoutes("${半角英字,10}", "\"${半角英字,10}\"", "${半角英字,10}");
    }

    /**
     * 種類 12 —— 改行文字の CR。
     * Excel は 2 文字の {@code \} ＋ {@code r}、YAML は {@code "\r"}。
     */
    @Test
    public void carriageReturn() {
        assertFourRoutes("a\\rb", "\"a\\rb\"", "a\rb");
    }

    /**
     * 種類 12 —— 改行文字の LF（同上）。Excel はセル内の改行（{@code Alt+Enter}）、YAML は {@code "\n"}。
     */
    @Test
    public void lineFeed() {
        assertFourRoutes("a" + LF + "b", "\"a\\nb\"", "a" + LF + "b");
    }

    // ------------------------------------------------------------------ 特殊な値の記述例

    /**
     * 記載例 1 —— 日付・システム日時・NULL。4 行 4 カラムをそのまま置く。
     */
    @Test
    public void exampleDateSystemTimeAndNull() {
        // Given: Excel 形式の記載例そのまま
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("EXPECTED_TABLE=SCHEDULE"))
                .row(text("ID"), text("EVENT_NAME"), text("START_DATE"), text("CREATED_AT"))
                .row(text("1"), text("会議"), text("2024-01-15"), text("2024-01-01 09:00:00.0"))
                .row(text("2"), text("NULLテスト"), text("NULL"), text("NULL"))
                .row(text("3"), text("システム時刻"), text("${systemTime}"), text("${systemTime}"))
                .row(text("4"), text("更新時刻"), text("${updateTime}"), text("${setUpTime}"))
                .writeTo(dir(1));
        // Given: YAML 形式の記載例そのまま
        write(dir(5), ""
                + "expected_tables:\n"
                + "  - table: \"SCHEDULE\"\n"
                + "    rows:\n"
                + "      - ID: \"1\"\n"
                + "        EVENT_NAME: \"会議\"\n"
                + "        START_DATE: \"2024-01-15\"\n"
                + "        CREATED_AT: \"2024-01-01 09:00:00.0\"\n"
                + "      - ID: \"2\"\n"
                + "        EVENT_NAME: \"NULLテスト\"\n"
                + "        START_DATE: null\n"
                + "        CREATED_AT: null\n"
                + "      - ID: \"3\"\n"
                + "        EVENT_NAME: \"システム時刻\"\n"
                + "        START_DATE: \"${systemTime}\"\n"
                + "        CREATED_AT: \"${systemTime}\"\n"
                + "      - ID: \"4\"\n"
                + "        EVENT_NAME: \"更新時刻\"\n"
                + "        START_DATE: \"${updateTime}\"\n"
                + "        CREATED_AT: \"${setUpTime}\"\n");

        // When / Then
        assertExampleFourRoutes(XLS_EXPECTED_TABLES, YAML_EXPECTED_TABLES);
    }

    /**
     * 記載例 2 —— 空文字・改行。Excel 形式の例に LF は含まれない。
     */
    @Test
    public void exampleEmptyStringAndLineBreak() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("EXPECTED_TABLE=NOTE"))
                .row(text("ID"), text("TITLE"), text("BODY"))
                .row(text("1"), text(""), text("1行目\\r2行目"))
                .row(text("2"), text("補足なし"), text(""))
                .writeTo(dir(1));
        write(dir(5), ""
                + "expected_tables:\n"
                + "  - table: \"NOTE\"\n"
                + "    rows:\n"
                + "      - ID: \"1\"\n"
                + "        TITLE: \"\"\n"
                + "        BODY: \"1行目\\r2行目\"\n"
                + "      - ID: \"2\"\n"
                + "        TITLE: \"補足なし\"\n"
                + "        BODY: \"\"\n");

        // When / Then
        assertExampleFourRoutes(XLS_EXPECTED_TABLES, YAML_EXPECTED_TABLES);
    }

    /**
     * 記載例 2b —— YAML 形式だけが持つ LF の例。
     * Excel 形式ではセル内改行で書くので、対応する Excel 側をセル内改行で組む。
     */
    @Test
    public void exampleLineFeedInBody() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("EXPECTED_TABLE=NOTE"))
                .row(text("ID"), text("TITLE"), text("BODY"))
                .row(text("3"), text(""), text("1行目" + LF + "2行目"))
                .writeTo(dir(1));
        write(dir(5), ""
                + "expected_tables:\n"
                + "  - table: \"NOTE\"\n"
                + "    rows:\n"
                + "      - ID: \"3\"\n"
                + "        TITLE: \"\"\n"
                + "        BODY: \"1行目\\n2行目\"\n");

        // When / Then
        assertExampleFourRoutes(XLS_EXPECTED_TABLES, YAML_EXPECTED_TABLES);
    }

    /**
     * 記載例 3 —— スペース・ダブルクォート。
     */
    @Test
    public void exampleSpaceAndDoubleQuote() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("EXPECTED_TABLE=ITEM"))
                .row(text("ID"), text("NAME"), text("MEMO"))
                .row(text("1"), text("\" \""), text("\"\"\""))
                .writeTo(dir(1));
        write(dir(5), ""
                + "expected_tables:\n"
                + "  - table: \"ITEM\"\n"
                + "    rows:\n"
                + "      - ID: \"1\"\n"
                + "        NAME: \" \"\n"
                + "        MEMO: \"\\\"\"\n");

        // When / Then
        assertExampleFourRoutes(XLS_EXPECTED_TABLES, YAML_EXPECTED_TABLES);
    }

    /**
     * 記載例 4 —— バイナリデータ。
     */
    @Test
    public void exampleBinaryData() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("SETUP_TABLE=FILE_TABLE"))
                .row(text("FILE_ID"), text("FILE_DATA"))
                .row(text("001"), text("0xCAFEBABE"))
                .row(text("002"), text("${binaryFile:testdata.bin}"))
                .writeTo(dir(1));
        write(dir(5), ""
                + "setup_tables:\n"
                + "  - table: \"FILE_TABLE\"\n"
                + "    rows:\n"
                + "      - FILE_ID: \"001\"\n"
                + "        FILE_DATA: \"0xCAFEBABE\"\n"
                + "      - FILE_ID: \"002\"\n"
                + "        FILE_DATA: \"${binaryFile:testdata.bin}\"\n");

        // When / Then
        // ${binaryFile:パス} を含むため、変換ツールが読んだ値との突き合わせは行わない（上の Javadoc 参照）。
        assertExampleFourRoutes(XLS_SETUP_TABLES, YAML_SETUP_TABLES, false);
    }

    /**
     * 記載例 5 —— 文字列の増幅。
     */
    @Test
    public void exampleAmplifiedString() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("SETUP_TABLE=MEMBER"))
                .row(text("MEMBER_ID"), text("NAME"), text("PROFILE"), text("ZIP_CODE"))
                .row(text("0000000101"), text("${全角漢字,5}"), text("${半角英字,10}"),
                        text("${半角数字,3}-${半角数字,4}"))
                .writeTo(dir(1));
        write(dir(5), ""
                + "setup_tables:\n"
                + "  - table: \"MEMBER\"\n"
                + "    rows:\n"
                + "      - MEMBER_ID: \"0000000101\"\n"
                + "        NAME: \"${全角漢字,5}\"\n"
                + "        PROFILE: \"${半角英字,10}\"\n"
                + "        ZIP_CODE: \"${半角数字,3}-${半角数字,4}\"\n");

        // When / Then
        assertExampleFourRoutes(XLS_SETUP_TABLES, YAML_SETUP_TABLES);
    }

    /**
     * 記載例 6 —— 全フィールドが空文字のレコード。ファイルデータのため {@link #assertExampleFourRoutes} ではなく
     * ファイル用の突き合わせを使う。
     */
    @Test
    public void exampleAllEmptyRecordInVariableFile() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("SETUP_VARIABLE=input/data.csv"))
                .row(text("field-separator"), text(","))
                .row(text("DATA"), text("USER_ID"), text("USER_NAME"), text("AMOUNT"))
                .row(text(""), text("半角"), text("全角"), text("半角"))
                .row(text(""), text("001"), text("山田太郎"), text("5000"))
                .row(text(""), text("\"\""), text(""), text(""))
                .writeTo(dir(1));
        write(dir(5), ""
                + "setup_files:\n"
                + "  - path: \"input/data.csv\"\n"
                + "    type: variable\n"
                + "    directives:\n"
                + "      field-separator: \",\"\n"
                + "    records:\n"
                + "      - record_type: DATA\n"
                + "        fields:\n"
                + "          - {name: USER_ID,   type: 半角}\n"
                + "          - {name: USER_NAME, type: 全角}\n"
                + "          - {name: AMOUNT,    type: 半角}\n"
                + "        rows:\n"
                + "          - [\"001\", \"山田太郎\", \"5000\"]\n"
                + "          - [\"\", \"\", \"\"]\n");

        // When / Then
        assertExampleFourRoutes(XLS_SETUP_VARIABLE, YAML_SETUP_FILES);
    }

    // ------------------------------------------------------------------ #44 で足した母集合（4 種）

    /**
     * 母集合の追加 1 —— ファイルの末尾に連続して {@code null} を書いた形。
     *
     * <p>
     * 3 行はそれぞれ {@code x,null,null} ／ {@code null,null,null} ／ {@code x,"",null} で、
     * 2-1 の実測表の F1・F4・F6 に対応する。末尾のフィールドの {@code null} は形式によらず空文字になる。
     * </p>
     */
    @Test
    public void trailingNullInFixedFile() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("SETUP_FIXED=f.dat"))
                .row(text("text-encoding"), text("UTF-8"))
                .row(text("data"), text("f1"), text("f2"), text("f3"))
                .row(text(""), text("半角英字"), text("半角英字"), text("半角英字"))
                .row(text(""), text("5"), text("5"), text("5"))
                .row(text(""), text("x"), text("null"), text("null"))
                .row(text(""), text("null"), text("null"), text("null"))
                .row(text(""), text("x"), text("\"\""), text("null"))
                .writeTo(dir(1));
        write(dir(5), ""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    directives:\n"
                + "      text-encoding: \"UTF-8\"\n"
                + "    records:\n"
                + "      - record_type: data\n"
                + "        fields:\n"
                + "          - {name: f1, type: 半角英字, length: \"5\"}\n"
                + "          - {name: f2, type: 半角英字, length: \"5\"}\n"
                + "          - {name: f3, type: 半角英字, length: \"5\"}\n"
                + "        rows:\n"
                + "          - [\"x\", null, null]\n"
                + "          - [null, null, null]\n"
                + "          - [\"x\", \"\", null]\n");

        // When / Then
        assertExampleFourRoutes(XLS_SETUP_FIXED, YAML_SETUP_FILES);
    }

    /**
     * 母集合の追加 1（続き） —— 電文の末尾に連続して {@code null} を書いた形（2-1 の実測表 M1）。
     */
    @Test
    public void trailingNullInMessage() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("MESSAGE=" + MESSAGE_ID))
                .row(text("text-encoding"), text("UTF-8"))
                .row(text("requestId"), text("R1"))
                .row(text("no"), text("f1"), text("f2"), text("f3"))
                .row(text(""), text("半角英字"), text("半角英字"), text("半角英字"))
                .row(text(""), text("5"), text("5"), text("5"))
                .row(text("1"), text("x"), text("null"), text("null"))
                .writeTo(dir(1));
        write(dir(5), ""
                + "messages:\n"
                + "  - id: \"" + MESSAGE_ID + "\"\n"
                + "    directives:\n"
                + "      text-encoding: \"UTF-8\"\n"
                + "    fw_header:\n"
                + "      requestId: \"R1\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: f1, type: 半角英字, length: \"5\"}\n"
                + "          - {name: f2, type: 半角英字, length: \"5\"}\n"
                + "          - {name: f3, type: 半角英字, length: \"5\"}\n"
                + "        rows:\n"
                + "          - [\"x\", null, null]\n");

        // When / Then
        assertExampleFourRoutes(XLS_MESSAGE, YAML_MESSAGE);
    }

    /**
     * 母集合の追加 2 —— 全カラムの値が空文字のテーブルエントリ。
     *
     * <p>
     * 第1回の 20 件は行が読み飛ばされないよう先頭にガードのカラムを置いていたため、この形が
     * 母集合に無かった。読み飛ばされる「記法として空のエントリ」は Excel 形式では全セルが空セルの行、
     * YAML 形式では空マッピングの行だけであり、{@code ""} と書いた空文字は値である。
     * </p>
     */
    @Test
    public void allEmptyStringEntryInTable() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("SETUP_TABLE=" + TABLE))
                .row(text("K"), text("V"))
                .row(text("x"), text("1"))
                .row(text("\"\""), text("\"\""))
                .writeTo(dir(1));
        write(dir(5), ""
                + "setup_tables:\n"
                + "  - table: \"" + TABLE + "\"\n"
                + "    rows:\n"
                + "      - K: \"x\"\n"
                + "        V: \"1\"\n"
                + "      - K: \"\"\n"
                + "        V: \"\"\n");

        // When / Then
        assertExampleFourRoutes(XLS_SETUP_TABLES, YAML_SETUP_TABLES);
    }

    /**
     * 母集合の追加 2（続き） —— 全カラムの値が空文字の {@code LIST_MAP} エントリ。
     */
    @Test
    public void allEmptyStringEntryInListMap() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("LIST_MAP=" + LIST_MAP_ID))
                .row(text("K"), text("V"))
                .row(text("x"), text("1"))
                .row(text("\"\""), text("\"\""))
                .writeTo(dir(1));
        write(dir(5), ""
                + "list_maps:\n"
                + "  - id: \"" + LIST_MAP_ID + "\"\n"
                + "    rows:\n"
                + "      - K: \"x\"\n"
                + "        V: \"1\"\n"
                + "      - K: \"\"\n"
                + "        V: \"\"\n");

        // When / Then
        assertExampleFourRoutes(XLS_LIST_MAP, YAML_LIST_MAP);
    }

    /**
     * 母集合の追加 3 —— マーカーカラムだけに値があるエントリ。
     *
     * <p>
     * マーカーカラムはエントリを読み飛ばす判断に使われないため、この形のエントリは残り、
     * 他のカラムは空文字として読み込まれる。消えるのはマーカーカラムの値だけである。
     * </p>
     */
    @Test
    public void markerOnlyEntryInListMap() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("LIST_MAP=" + LIST_MAP_ID))
                .row(text("[no]"), text("K"), text("V"))
                .row(text("1"), text("x"), text("1"))
                .row(text("2"), text(""), text(""))
                .writeTo(dir(1));
        write(dir(5), ""
                + "list_maps:\n"
                + "  - id: \"" + LIST_MAP_ID + "\"\n"
                + "    rows:\n"
                + "      - \"[no]\": \"1\"\n"
                + "        K: \"x\"\n"
                + "        V: \"1\"\n"
                + "      - \"[no]\": \"2\"\n"
                + "        K: \"\"\n"
                + "        V: \"\"\n");

        // When / Then
        assertExampleFourRoutes(XLS_LIST_MAP, YAML_LIST_MAP);
    }

    /**
     * 母集合の追加 4 —— アップロードファイルを指定する記載例
     * （{@code LIST_MAP} ＋ マーカーカラム {@code [no]} ＋ {@code ${attach:パス}}）。
     *
     * <p>
     * 第1回は特殊記法の記載例の節に含まれていたが母集合から外していた。
     * </p>
     */
    @Test
    public void exampleUploadFile() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("LIST_MAP=" + LIST_MAP_ID))
                .row(text("[no]"), text("memberId"), text("uploadFile"))
                .row(text("1"), text("0000000101"),
                        text("${attach:src/test/resources/upload/member_photo.png}"))
                .writeTo(dir(1));
        write(dir(5), ""
                + "list_maps:\n"
                + "  - id: \"" + LIST_MAP_ID + "\"\n"
                + "    rows:\n"
                + "      - \"[no]\": \"1\"\n"
                + "        memberId: \"0000000101\"\n"
                + "        uploadFile: \"${attach:src/test/resources/upload/member_photo.png}\"\n");

        // When / Then
        assertExampleFourRoutes(XLS_LIST_MAP, YAML_LIST_MAP);
    }

    /**
     * 記載例を 4 経路で往復させ、いずれも中間モデルのブロックが往復前と等しいことを確かめる。
     *
     * <p>
     * 呼び出す前に、Excel 形式の記載例を {@link #dir(int) dir(1)} へ、YAML 形式の記載例を
     * {@code dir(5)} へ書き出しておくこと。
     * </p>
     */
    private void assertExampleFourRoutes(Function<Path, String> xlsOracle, Function<Path, String> yamlOracle) {
        assertExampleFourRoutes(xlsOracle, yamlOracle, true);
    }

    /**
     * 記載例を 4 経路で往復させる。
     *
     * @param xlsOracle            Excel 形式を本体に読ませる関数
     * @param yamlOracle           YAML 形式を YAML 読み込みに読ませる関数
     * @param compareConverterRead 変換ツールが読んだ値を正解と突き合わせるか。
     *                             {@code ${binaryFile:パス}} を含む母集合では偽にする ——
     *                             変換ツールはこの記法を解決せず記法のまま運ぶ仕様であり、
     *                             フレームワークが解決した値とは一致しないためである（往復の担保は残る）
     */
    private void assertExampleFourRoutes(Function<Path, String> xlsOracle, Function<Path, String> yamlOracle,
                                         boolean compareConverterRead) {
        String fromXls = xlsOracle.apply(dir(1));
        String fromYaml = yamlOracle.apply(dir(5));

        // 2 つの記載例が同じ意味を表していること
        assertThat("Excel 形式の例と YAML 形式の例が同じ値を表す", fromXls, is(fromYaml));

        // 変換ツールが読んだ値が、フレームワークが読んだ値と一致すること（正解は本体である）
        if (compareConverterRead) {
            assertThat("変換ツールの Excel 読みが本体と一致する", converterReadXls(dir(1)), is(fromXls));
            assertThat("変換ツールの YAML 読みが本体と一致する", converterReadYaml(dir(5)), is(fromYaml));
        }

        // 経路 1: XLS → XLS
        new XlsFormatWriter().write(rewrap(new XlsFormatReader().read(dir(1).toString(), BOOK + "/" + SHEET)),
                dir(2).toString());
        assertThat("XLS→XLS", xlsOracle.apply(dir(2)), is(fromXls));

        // 経路 2: XLS → YAML → XLS
        new YamlFormatWriter().write(rewrap(new XlsFormatReader().read(dir(1).toString(), BOOK + "/" + SHEET)),
                dir(3).toString());
        new XlsFormatWriter().write(rewrap(new YamlFormatReader().read(dir(3).toAbsolutePath().toString(), SHEET)),
                dir(4).toString());
        assertThat("XLS→YAML→XLS", xlsOracle.apply(dir(4)), is(fromXls));

        // 経路 3: YAML → YAML
        new YamlFormatWriter().write(rewrap(new YamlFormatReader().read(dir(5).toAbsolutePath().toString(), SHEET)),
                dir(6).toString());
        assertThat("YAML→YAML", yamlOracle.apply(dir(6)), is(fromYaml));

        // 経路 4: YAML → XLS → YAML
        new XlsFormatWriter().write(rewrap(new YamlFormatReader().read(dir(5).toAbsolutePath().toString(), SHEET)),
                dir(7).toString());
        new YamlFormatWriter().write(rewrap(new XlsFormatReader().read(dir(7).toString(), BOOK + "/" + SHEET)),
                dir(8).toString());
        assertThat("YAML→XLS→YAML", yamlOracle.apply(dir(8)), is(fromYaml));
    }
}
