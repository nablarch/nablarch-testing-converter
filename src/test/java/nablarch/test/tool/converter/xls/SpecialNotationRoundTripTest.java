package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
 * <b>母集合</b>（解説書は {@code nablarch-document@5783b35}）:
 * </p>
 * <ul>
 *   <li>{@code implementation/testdata_notation.rst} の特殊記法の表 ——
 *       Excel 形式 12 行（{@code :1356}-{@code :1391}）／YAML 形式 12 行（{@code :1408}-{@code :1443}）。
 *       <b>2 つの表は同じ 12 の「値の種類」を同じ順で並べており</b>、行ごとに Excel 記法と YAML 記法が
 *       対応する。改行文字の行だけは CR と LF の 2 形を含むため、本クラスでは 2 つに分けて測る</li>
 *   <li>{@code implementation/testdata_examples.rst} の「null・空文字・改行など特殊な値を記述する」
 *       （{@code :2133}-{@code :2461}）の各記載例 —— 6 つの小節が Excel 形式と YAML 形式の例を
 *       それぞれ 1 つずつ持つ</li>
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
 * （{@code implementation/testdata_notation.rst:1500}。空文字のケースで行ごと消えるため）。
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

    /** LF。 */
    private static final String LF = "\n";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

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

    /**
     * 指定ディレクトリの実 {@code .xlsx} を読み、{@code V} 列の解釈後の値を返す。
     *
     * @param dir ディレクトリ
     * @return 解釈後の値
     */
    private static String readXls(Path dir) {
        return valueOf(new XlsFormatReader().read(dir.toString(), BOOK + "/" + SHEET));
    }

    /**
     * 指定ディレクトリの実 {@code .yaml} を読み、{@code V} 列の解釈後の値を返す。
     *
     * @param dir ディレクトリ
     * @return 解釈後の値
     */
    private static String readYaml(Path dir) {
        return valueOf(new YamlFormatReader().read(dir.toAbsolutePath().toString(), SHEET));
    }

    /**
     * 中間モデルから {@code V} 列の値を取り出す。
     *
     * @param container 中間モデル
     * @return {@code V} 列の値
     */
    private static String valueOf(TestDataContainer container) {
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        assertThat("ブロック数", blocks.size(), is(1));
        TableDataBlock table = (TableDataBlock) blocks.get(0);
        assertThat("カラム名", table.getColumnNames(), is(Arrays.asList("K", "V")));
        assertThat("エントリ数", table.getRows().size(), is(1));
        return table.getRows().get(0).get(1);
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
     * @param expected   解説書が定める<b>解釈後の値</b>
     */
    private void assertFourRoutes(String excelCell, String yamlScalar, String expected) {
        // 経路 1: XLS → XLS
        writeXls(dir(1), excelCell);
        String fromXls = readXls(dir(1));
        assertThat("Excel 記法の解釈後の値", fromXls, is(expected));
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
        new YamlFormatWriter().write(rewrap(new YamlFormatReader().read(dir(5).toAbsolutePath().toString(), SHEET)),
                dir(6).toString());
        assertThat("YAML→YAML", readYaml(dir(6)), is(fromYaml));

        // 経路 4: YAML → XLS → YAML
        new XlsFormatWriter().write(rewrap(new YamlFormatReader().read(dir(5).toAbsolutePath().toString(), SHEET)),
                dir(7).toString());
        new YamlFormatWriter().write(rewrap(new XlsFormatReader().read(dir(7).toString(), BOOK + "/" + SHEET)),
                dir(8).toString());
        assertThat("YAML→XLS→YAML", readYaml(dir(8)), is(fromYaml));

        // 2 つの記法が同じ意味を表していること（tools/testdata_converter.rst:14）
        assertThat("Excel 記法と YAML 記法が同じ値を表す", fromXls, is(fromYaml));
    }

    // ------------------------------------------------------------------ notation.rst の表 12 行

    /** 表 1 行目 —— 通常の文字列（{@code :1356}-{@code :1358} ／ {@code :1408}-{@code :1410}）。 */
    @Test
    public void plainString() {
        assertFourRoutes("abc", "\"abc\"", "abc");
    }

    /** 表 2 行目 —— null（{@code :1359}-{@code :1361} ／ {@code :1411}-{@code :1413}）。 */
    @Test
    public void javaNull() {
        assertFourRoutes("null", "null", null);
    }

    /** 表 3 行目 —— 文字列の null（{@code :1362}-{@code :1364} ／ {@code :1414}-{@code :1416}）。 */
    @Test
    public void stringNull() {
        assertFourRoutes("\"null\"", "\"null\"", "null");
    }

    /** 表 4 行目 —— 空文字（{@code :1365}-{@code :1367} ／ {@code :1417}-{@code :1419}）。 */
    @Test
    public void emptyString() {
        assertFourRoutes("", "\"\"", "");
    }

    /** 表 5 行目 —— 先頭ゼロ付き数値（{@code :1368}-{@code :1370} ／ {@code :1420}-{@code :1422}）。 */
    @Test
    public void leadingZeroNumber() {
        assertFourRoutes("001", "\"001\"", "001");
    }

    /** 表 6 行目 —— {@code true}（文字列）（{@code :1371}-{@code :1373} ／ {@code :1423}-{@code :1425}）。 */
    @Test
    public void booleanLikeString() {
        assertFourRoutes("true", "\"true\"", "true");
    }

    /** 表 7 行目 —— 半角スペース 1 文字（{@code :1374}-{@code :1376} ／ {@code :1426}-{@code :1428}）。 */
    @Test
    public void singleHalfWidthSpace() {
        assertFourRoutes("\" \"", "\" \"", " ");
    }

    /** 表 8 行目 —— ダブルクォート 1 文字（{@code :1377}-{@code :1379} ／ {@code :1429}-{@code :1431}）。 */
    @Test
    public void singleDoubleQuote() {
        assertFourRoutes("\"\"\"", "'\"'", "\"");
    }

    /** 表 9 行目 —— {@code ${systemTime}}（{@code :1380}-{@code :1382} ／ {@code :1432}-{@code :1434}）。 */
    @Test
    public void systemTimeNotation() {
        assertFourRoutes("${systemTime}", "\"${systemTime}\"", "${systemTime}");
    }

    /** 表 10 行目 —— {@code ${binaryFile:パス}}（{@code :1383}-{@code :1385} ／ {@code :1435}-{@code :1437}）。 */
    @Test
    public void binaryFileNotation() {
        assertFourRoutes("${binaryFile:testdata.bin}", "\"${binaryFile:testdata.bin}\"", "${binaryFile:testdata.bin}");
    }

    /** 表 11 行目 —— {@code ${文字種,文字数}}（{@code :1386}-{@code :1388} ／ {@code :1438}-{@code :1440}）。 */
    @Test
    public void charTypeNotation() {
        assertFourRoutes("${半角英字,10}", "\"${半角英字,10}\"", "${半角英字,10}");
    }

    /**
     * 表 12 行目 —— 改行文字の CR（{@code :1389}-{@code :1391} ／ {@code :1441}-{@code :1443}）。
     * Excel は 2 文字の {@code \} ＋ {@code r}、YAML は {@code "\r"}。
     */
    @Test
    public void carriageReturn() {
        assertFourRoutes("a\\rb", "\"a\\rb\"", "a\rb");
    }

    /**
     * 表 12 行目 —— 改行文字の LF（同上）。Excel はセル内の改行（{@code Alt+Enter}）、YAML は {@code "\n"}。
     */
    @Test
    public void lineFeed() {
        assertFourRoutes("a" + LF + "b", "\"a\\nb\"", "a" + LF + "b");
    }

    // ------------------------------------------------------------------ examples.rst の記載例

    /**
     * 記載例 1 —— 日付・システム日時・NULL（{@code testdata_examples.rst:2149}-{@code :2176} ／
     * {@code :2184}-{@code :2202}）。4 行 4 カラムをそのまま置く。
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
        assertExampleFourRoutes();
    }

    /**
     * 記載例 2 —— 空文字・改行（{@code testdata_examples.rst:2212}-{@code :2227} ／
     * {@code :2270}-{@code :2278}）。Excel 形式の例に LF は含まれない（同 {@code :2229}）。
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
        assertExampleFourRoutes();
    }

    /**
     * 記載例 2b —— YAML 形式だけが持つ LF の例（{@code testdata_examples.rst:2284}-{@code :2289}）。
     * Excel 形式ではセル内改行で書く（同 {@code :2229}）ので、対応する Excel 側をセル内改行で組む。
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
        assertExampleFourRoutes();
    }

    /**
     * 記載例 3 —— スペース・ダブルクォート（{@code testdata_examples.rst:2320}-{@code :2332} ／
     * {@code :2340}-{@code :2345}）。
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
        assertExampleFourRoutes();
    }

    /**
     * 記載例 4 —— バイナリデータ（{@code testdata_examples.rst:2355}-{@code :2366} ／
     * {@code :2376}-{@code :2382}）。
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
        assertExampleFourRoutes();
    }

    /**
     * 記載例 5 —— 文字列の増幅（{@code testdata_examples.rst:2392}-{@code :2407} ／
     * {@code :2415}-{@code :2421}）。
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
        assertExampleFourRoutes();
    }

    /**
     * 記載例 6 —— 全フィールドが空文字のレコード（{@code testdata_examples.rst:2233}-{@code :2260} ／
     * {@code :2293}-{@code :2308}）。ファイルデータのため {@link #assertExampleFourRoutes} ではなく
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
        assertExampleFourRoutes();
    }

    /**
     * 記載例を 4 経路で往復させ、いずれも中間モデルのブロックが往復前と等しいことを確かめる。
     *
     * <p>
     * 呼び出す前に、Excel 形式の記載例を {@link #dir(int) dir(1)} へ、YAML 形式の記載例を
     * {@code dir(5)} へ書き出しておくこと。
     * </p>
     */
    private void assertExampleFourRoutes() {
        TestDataBlock fromXls = blockOf(new XlsFormatReader().read(dir(1).toString(), BOOK + "/" + SHEET));
        TestDataBlock fromYaml = blockOf(new YamlFormatReader().read(dir(5).toAbsolutePath().toString(), SHEET));

        // 2 つの記載例が同じ意味を表していること（tools/testdata_converter.rst:14）
        assertThat("Excel 形式の例と YAML 形式の例が同じ値を表す", describe(fromXls), is(describe(fromYaml)));

        // 経路 1: XLS → XLS
        new XlsFormatWriter().write(rewrap(new XlsFormatReader().read(dir(1).toString(), BOOK + "/" + SHEET)),
                dir(2).toString());
        assertBlock("XLS→XLS", blockOf(new XlsFormatReader().read(dir(2).toString(), BOOK + "/" + SHEET)), fromXls);

        // 経路 2: XLS → YAML → XLS
        new YamlFormatWriter().write(rewrap(new XlsFormatReader().read(dir(1).toString(), BOOK + "/" + SHEET)),
                dir(3).toString());
        new XlsFormatWriter().write(rewrap(new YamlFormatReader().read(dir(3).toAbsolutePath().toString(), SHEET)),
                dir(4).toString());
        assertBlock("XLS→YAML→XLS", blockOf(new XlsFormatReader().read(dir(4).toString(), BOOK + "/" + SHEET)),
                fromXls);

        // 経路 3: YAML → YAML
        new YamlFormatWriter().write(rewrap(new YamlFormatReader().read(dir(5).toAbsolutePath().toString(), SHEET)),
                dir(6).toString());
        assertBlock("YAML→YAML", blockOf(new YamlFormatReader().read(dir(6).toAbsolutePath().toString(), SHEET)),
                fromYaml);

        // 経路 4: YAML → XLS → YAML
        new XlsFormatWriter().write(rewrap(new YamlFormatReader().read(dir(5).toAbsolutePath().toString(), SHEET)),
                dir(7).toString());
        new YamlFormatWriter().write(rewrap(new XlsFormatReader().read(dir(7).toString(), BOOK + "/" + SHEET)),
                dir(8).toString());
        assertBlock("YAML→XLS→YAML", blockOf(new YamlFormatReader().read(dir(8).toAbsolutePath().toString(), SHEET)),
                fromYaml);
    }

    /**
     * コンテナから唯一のブロックを取り出す。
     *
     * @param container 中間モデル
     * @return 唯一のブロック
     */
    private static TestDataBlock blockOf(TestDataContainer container) {
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        assertThat("ブロック数", blocks.size(), is(1));
        return blocks.get(0);
    }

    /**
     * 2 つのブロックの<b>解釈後の値</b>が等しいことを確かめる。
     *
     * @param label    経路の名前（失敗メッセージ用）
     * @param actual   往復後のブロック
     * @param expected 往復前のブロック
     */
    private static void assertBlock(String label, TestDataBlock actual, TestDataBlock expected) {
        assertThat(label + ": データタイプ", actual.getDataType(), is(expected.getDataType()));
        assertThat(label + ": 識別子", actual.getIdentifier(), is(expected.getIdentifier()));
        assertThat(label + ": 値", describe(actual), is(describe(expected)));
    }

    /**
     * ブロックの値だけを文字列へ写す（失敗時に差分が読めるようにする）。
     *
     * @param block ブロック
     * @return 値の表現
     */
    private static String describe(TestDataBlock block) {
        if (block instanceof nablarch.test.tool.converter.model.ColumnRowDataBlock) {
            nablarch.test.tool.converter.model.ColumnRowDataBlock b =
                    (nablarch.test.tool.converter.model.ColumnRowDataBlock) block;
            return b.getColumnNames() + " " + b.getRows();
        }
        nablarch.test.tool.converter.model.FileDataBlock b =
                (nablarch.test.tool.converter.model.FileDataBlock) block;
        StringBuilder sb = new StringBuilder();
        for (nablarch.test.tool.converter.model.RecordLayout record : b.getRecords()) {
            sb.append(record.getRecordType()).append(' ');
            for (nablarch.test.tool.converter.model.FieldDef field : record.getFields()) {
                sb.append(field.getName()).append(':').append(field.getType()).append(' ');
            }
            sb.append(record.getRows()).append('\n');
        }
        return sb.toString();
    }
}
