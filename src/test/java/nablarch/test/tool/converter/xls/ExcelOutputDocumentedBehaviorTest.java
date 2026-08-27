package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nablarch.test.core.reader.DataType;
import nablarch.test.tool.converter.model.FieldDef;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.MessageDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 解説書 {@code tools/testdata_converter.rst}（{@code 5783b35}）が述べている
 * <b>Excel 形式の出力についての振る舞い</b>を押さえるテスト（指示書 第3節のうち 3-2・3-6〜3-10）。
 *
 * <p>
 * 既存の {@code XlsFormatWriterTest} が押さえているのは<b>既定値</b>の色である。本クラスが押さえるのは
 * <b>設定した値が実際に効くこと</b>と、<b>形式に固有で意味を持たない情報が往復で落ちること</b>である。
 * </p>
 *
 * @author kiyobot
 */
public class ExcelOutputDocumentedBehaviorTest {

    /** フィクスチャの既定ブック名。 */
    private static final String BOOK = "docBook";

    /** フィクスチャの既定シート名。 */
    private static final String SHEET = "s";

    /** 設定した値が効いたことが分かるよう、既定値と重ならない色を使う。 */
    private static final short CUSTOM = IndexedColors.RED.getIndex();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // ------------------------------------------------------------------ helpers

    /** 段ごとに別ディレクトリを使う。 */
    private Path dir(int step) {
        return folder.getRoot().toPath().resolve("s" + step);
    }

    /** 1 ブロックのコンテナを組み立てる。 */
    private static TestDataContainer container(TestDataBlock... blocks) {
        return new TestDataContainer(BOOK, Collections.singletonList(
                new TestDataSection(SHEET, Arrays.asList(blocks))));
    }

    /** 設定を与えて版面を組み立て、唯一のシートを返す。 */
    private static Sheet build(ExcelFormatConfig config, TestDataBlock... blocks) {
        return new XlsFormatWriter(config).build(container(blocks)).getSheet(SHEET);
    }

    /** 文字列リスト。 */
    private static List<String> row(String... values) {
        return Arrays.asList(values);
    }

    /** 指定行・指定列のセルの背景色インデックスを返す。 */
    private static short fill(Sheet sheet, int r, int c) {
        return sheet.getRow(r).getCell(c).getCellStyle().getFillForegroundColor();
    }

    /**
     * 組み立てたブックを {@code <dir>/<BOOK>.xlsx} へ書き出す。
     *
     * <p>
     * {@link XlsFixture} のビルダはセルの色・表示形式・結合セルを表現できないため、本テストだけは
     * POI のブックを直に組み立てて書き出す。
     * </p>
     *
     * @param workbook 書き出すブック
     * @param dir      書き出し先ディレクトリ
     */
    private static void writeWorkbook(Workbook workbook, Path dir) {
        try {
            Files.createDirectories(dir);
            try (OutputStream out = Files.newOutputStream(dir.resolve(BOOK + ".xlsx"))) {
                workbook.write(out);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("failed to write workbook: " + dir, e);
        }
    }

    /** 送信系・MESSAGE 用の最小レコード。 */
    private static RecordLayout minimalRecord() {
        return new RecordLayout("rec",
                Collections.singletonList(new FieldDef("f1", "半角英字", "1")),
                Collections.singletonList(row("a")));
    }

    // ------------------------------------------------------------------ 3-2

    /**
     * Given: セルの背景色・表示形式・結合セルを設定した実 {@code .xlsx}（{@code SETUP_TABLE}）。
     * When : XLS→XLS で往復させる。
     * Then : 往復後のセルにその色・表示形式・結合が<b>残っていない</b>（<b>負のテスト</b>）。
     *
     * <p>
     * {@code tools/testdata_converter.rst:59}「形式に固有で、テスティングフレームワークの仕様上の意味を
     * 持たない情報は中間モデルに保持されないため、往復しても再現されない。Excel 形式のセルの色・書式・
     * 結合セル……がこれにあたる。Excel 形式へ書き出す際は、元の色や書式ではなく……設定に従った整形が
     * 新たに付与される」。
     * </p>
     */
    @Test
    public void dropsCellColorFormatAndMergeOnRoundTrip() {
        // Given: POI で色・表示形式・結合セルを直に付けたブック
        Workbook source = new XSSFWorkbook();
        Sheet sheet = source.createSheet(SHEET);
        sheet.createRow(0).createCell(0).setCellValue("SETUP_TABLE=T");
        sheet.createRow(1);
        sheet.getRow(1).createCell(0).setCellValue("A");
        sheet.getRow(1).createCell(1).setCellValue("B");
        sheet.createRow(2);
        sheet.getRow(2).createCell(0).setCellValue("1");
        sheet.getRow(2).createCell(1).setCellValue("2");
        CellStyle decorated = source.createCellStyle();
        decorated.setFillForegroundColor(IndexedColors.RED.getIndex());
        decorated.setFillPattern(CellStyle.SOLID_FOREGROUND);
        decorated.setDataFormat(source.createDataFormat().getFormat("0.00"));
        sheet.getRow(2).getCell(0).setCellStyle(decorated);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 1));
        writeWorkbook(source, dir(1));

        // When
        new XlsFormatWriter().write(
                new XlsFormatReader().read(dir(1).toString(), BOOK + "/" + SHEET), dir(2).toString());

        // Then
        Sheet back = XlsFixture.open(dir(2).resolve(BOOK + ".xlsx")).getSheet(SHEET);
        assertThat("値は保たれる", XlsFixture.line(back, 2), is(row("1", "2")));
        assertThat("元の背景色は残らない", fill(back, 2, 0), is(not(IndexedColors.RED.getIndex())));
        assertThat("元の表示形式は残らない",
                back.getRow(2).getCell(0).getCellStyle().getDataFormatString(), is("General"));
        assertThat("結合セルは残らない", back.getNumMergedRegions(), is(0));
    }

    // ------------------------------------------------------------------ 3-6

    /**
     * Given: 識別子 {@code testShots} の {@code LIST_MAP}。
     * When : {@code withTestShotsHeaderColor(x)} を設定して書き出す。
     * Then : ヘッダ行の背景色が {@code x} になる。
     *
     * <p>{@code tools/testdata_converter.rst:251}-{@code :254}。</p>
     */
    @Test
    public void appliesConfiguredTestShotsHeaderColor() {
        // Given
        ListMapBlock testShots = new ListMapBlock("", "testShots",
                row("no", "description"), Collections.singletonList(row("1", "t")));

        // When
        Sheet sheet = build(ExcelFormatConfig.defaults().withTestShotsHeaderColor(CUSTOM), testShots);

        // Then
        assertThat("設定した色が効く", fill(sheet, 1, 0), is(CUSTOM));
        assertThat("既定値のままではない",
                CUSTOM, is(not(ExcelFormatConfig.defaults().getTestShotsHeaderColorIndex())));
    }

    // ------------------------------------------------------------------ 3-7

    /**
     * Given: {@code EXPECTED_} で始まるブロックと {@code RESPONSE_} で始まるブロック。
     * When : {@code withExpectedHeaderColor(x)} を設定して書き出す。
     * Then : <b>どちらも</b>ヘッダ行の背景色が {@code x} になる。
     *
     * <p>
     * {@code tools/testdata_converter.rst:259}-{@code :262}
     * 「{@code EXPECTED_} で始まるデータブロックと {@code RESPONSE_} で始まるデータブロックの
     * ヘッダ行の背景色」。
     * </p>
     */
    @Test
    public void appliesConfiguredExpectedHeaderColorToBothExpectedAndResponse() {
        // Given
        TableDataBlock expected = new TableDataBlock(DataType.EXPECTED_TABLE_DATA, "", "T",
                row("C"), Collections.singletonList(row("v")));
        MessageDataBlock response = new MessageDataBlock(DataType.RESPONSE_BODY_MESSAGES, "g", "RM01",
                Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(),
                Collections.singletonList(minimalRecord()));

        // When
        Sheet sheet = build(ExcelFormatConfig.defaults().withExpectedHeaderColor(CUSTOM), expected, response);

        // Then（0:識別行 1:カラム名行 2:データ行 3:空行 4:識別行 5:名前行 …）
        assertThat("EXPECTED_TABLE のヘッダ行", fill(sheet, 1, 0), is(CUSTOM));
        assertThat("RESPONSE_BODY_MESSAGES のヘッダ行", fill(sheet, 5, 0), is(CUSTOM));
    }

    // ------------------------------------------------------------------ 3-8

    /**
     * Given: {@code MESSAGE}・{@code testShots} 以外の {@code LIST_MAP}・{@code testShots} の {@code LIST_MAP}。
     * When : {@code withOtherHeaderColor(x)} を設定して書き出す。
     * Then : 前 2 つのヘッダ行が {@code x} になり、{@code testShots} は変わらない。
     *
     * <p>
     * {@code tools/testdata_converter.rst:263}-{@code :266}
     * 「{@code MESSAGE}・{@code LIST_MAP}（識別子が {@code testShots} 以外）のヘッダ行の背景色」。
     * </p>
     */
    @Test
    public void appliesConfiguredOtherHeaderColorExceptTestShots() {
        // Given
        ListMapBlock other = new ListMapBlock("", "params",
                row("K"), Collections.singletonList(row("v")));
        ListMapBlock testShots = new ListMapBlock("", "testShots",
                row("no"), Collections.singletonList(row("1")));
        MessageDataBlock message = new MessageDataBlock(DataType.MESSAGE, "", "M1",
                Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(),
                Collections.singletonList(minimalRecord()));

        // When
        Sheet sheet = build(ExcelFormatConfig.defaults().withOtherHeaderColor(CUSTOM),
                other, testShots, message);

        // Then（各ブロックは 識別行・ヘッダ行・… ＋ 空行 1 で並ぶ）
        assertThat("testShots 以外の LIST_MAP", fill(sheet, 1, 0), is(CUSTOM));
        assertThat("testShots の LIST_MAP は変わらない",
                fill(sheet, 5, 0), is(ExcelFormatConfig.defaults().getTestShotsHeaderColorIndex()));
        assertThat("MESSAGE", fill(sheet, 9, 0), is(CUSTOM));
    }

    // ------------------------------------------------------------------ 3-9

    /**
     * Given: 30 文字の値を持つ列。
     * When : 既定（上限 20 文字）と {@code withMaxColumnWidthChars(5)} で書き出す。
     * Then : 既定では 20 文字相当で頭打ちになり、5 を指定すると 5 文字相当になる。
     *
     * <p>
     * {@code tools/testdata_converter.rst:275}-{@code :278}
     * 「列幅を自動調整する場合の上限文字数」。POI の列幅は 1 文字あたり 256 単位である。
     * </p>
     */
    @Test
    public void limitsColumnWidthByMaxColumnWidthChars() {
        // Given
        String thirtyChars = "012345678901234567890123456789";
        assertThat("入力は 30 文字", thirtyChars.length(), is(30));
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                row("C"), Collections.singletonList(row(thirtyChars)));

        // When
        Sheet byDefault = build(ExcelFormatConfig.defaults(), table);
        Sheet narrowed = build(ExcelFormatConfig.defaults().withMaxColumnWidthChars(5), table);

        // Then
        assertThat("既定の上限 20 で頭打ちになる（30 ＋ 余白 2 にならない）",
                byDefault.getColumnWidth(0), is(20 * 256));
        assertThat("上限 5 を指定すると 5 文字相当になる", narrowed.getColumnWidth(0), is(5 * 256));
    }

    // ------------------------------------------------------------------ 3-10

    /**
     * Given: 任意のブロック。
     * When : 既定と {@code withDisplayGridlines(true)} で書き出す。
     * Then : 既定ではグリッド線表示がオフ、{@code true} ではオンになる。
     *
     * <p>{@code tools/testdata_converter.rst:287}-{@code :290}（既定は {@code false}）。</p>
     */
    @Test
    public void togglesDisplayGridlines() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                row("C"), Collections.singletonList(row("v")));

        // When / Then
        assertThat("既定はオフ", build(ExcelFormatConfig.defaults(), table).isDisplayGridlines(), is(false));
        assertThat("true を設定するとオン",
                build(ExcelFormatConfig.defaults().withDisplayGridlines(true), table).isDisplayGridlines(),
                is(true));
    }
}
