package nablarch.test.tool.converter.xls;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import nablarch.test.core.reader.DataType;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 辺③（中間モデル→Excel）の軸D — 値がどの<b>セル型</b>で書き出されるかを固定するテスト。
 *
 * <p>
 * 対象は {@code .rn/ntf-test-data-converter/coverage/inventory.md} §0.5 の辺③ 8 ケース
 * （D3-01 {@code "100"}／D3-02 {@code "=1+1"}／D3-03 {@code "007"}／D3-04 {@code null}／
 * D3-05 {@code ""}／D3-06 改行含む文字列／D3-07 32767 文字超／D3-08 制御文字含む）である。
 * </p>
 *
 * <p>
 * {@code XlsFormatWriterTest} が {@code build}（メモリ上のブック）のセル<b>値</b>を
 * {@code getStringCellValue()} で見るのに対し、本クラスは {@link XlsFormatWriter#write} で実
 * {@code .xlsx} を書き、それを POI で開き直して {@link Cell#getCellType()} を突き合わせる。
 * すなわち「値 → セル → ファイル」の直列化区間を実行し、<b>数値・数式として解釈されないこと</b>を
 * セル型で固定する。ファイルを経由するのは、直列化（XML 書き出し）で値が変わるケースが実在するためである
 * （下記 D3-06・D3-08。メモリ上のブックだけを見ていると気づけない）。
 * </p>
 *
 * <p>
 * 各ケースは {@code SETUP_TABLE} の 1 データ行（{@code KEY} 列＝ケース識別・{@code V} 列＝検証対象セル）
 * として与え、{@code V} 列のセルを検証する。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>期待される仕様ではない。
 * 妥当でないと判断した挙動は {@code .rn/ntf-test-data-converter/coverage/issues.md} に課題
 * （XLS-17〜XLS-19）として記録してあり、実装（src/main）は変更していない。
 * </p>
 *
 * @author kiyobot
 */
public class XlsFormatWriterCellTypeTest {

    /** 出力ブック名。 */
    private static final String BOOK = "cellTypes";

    /** 出力シート名。 */
    private static final String SHEET = "s";

    /** 検証対象セルの行番号（識別行 0 → カラム名行 1 → データ行 2）。 */
    private static final int DATA_ROW = 2;

    /** 検証対象セルの列番号（{@code KEY} 列 0 → {@code V} 列 1）。 */
    private static final int VALUE_COLUMN = 1;

    /** Excel（{@code .xlsx}）が 1 セルに保持できる文字数の上限。 */
    private static final int EXCEL_MAX_CELL_LENGTH = 32767;

    /**
     * 出力先。
     *
     * <p>
     * {@link TemporaryFolder} はテストメソッドごとに別ディレクトリを与えるため、全メソッドが同じ
     * ブック名を使ってもファイルパスは衝突しない。
     * </p>
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // ------------------------------------------------------------------ helpers

    /**
     * 検証対象の値 1 個を {@code V} 列に持つ {@code SETUP_TABLE} のコンテナを組み立てる。
     *
     * @param value 検証対象の値
     * @return コンテナ
     */
    private static TestDataContainer container(String value) {
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                Arrays.asList("KEY", "V"),
                Collections.singletonList(Arrays.asList("k", value)));
        TestDataSection section = new TestDataSection(SHEET,
                Collections.<TestDataBlock>singletonList(table));
        return new TestDataContainer(BOOK, Collections.singletonList(section));
    }

    /**
     * 検証対象の値 1 個を {@code SETUP_TABLE} のデータ行に置いてメモリ上のブックを組み立て、
     * 検証対象セルの値を返す（ファイルへ直列化する前の値）。
     *
     * @param value 検証対象の値
     * @return メモリ上のブックのセル値
     */
    private static String buildInMemory(String value) {
        return new XlsFormatWriter().build(container(value))
                .getSheet(SHEET).getRow(DATA_ROW).getCell(VALUE_COLUMN).getStringCellValue();
    }

    /**
     * 検証対象の値 1 個を {@code SETUP_TABLE} のデータ行に置いて実 {@code .xlsx} を書き出し、
     * POI で開き直して検証対象セルを返す。
     *
     * @param value 検証対象の値（{@code V} 列に置く）
     * @return 書き出したファイルから読み直したセル
     */
    private Cell writeAndReopen(String value) {
        new XlsFormatWriter().write(container(value), folder.getRoot().getAbsolutePath());

        Path file = folder.getRoot().toPath().resolve(BOOK + ".xlsx");
        Sheet sheet = XlsFixture.open(file).getSheet(SHEET);
        assertThat("書き出したブックに出力先シートがあること", sheet, is(notNullValue()));
        Row row = sheet.getRow(DATA_ROW);
        assertThat("データ行が書かれていること", row, is(notNullValue()));
        Cell cell = row.getCell(VALUE_COLUMN);
        assertThat("検証対象セルが書かれていること", cell, is(notNullValue()));
        return cell;
    }

    /**
     * 同じ文字を繰り返した文字列を作る。
     *
     * @param c     文字
     * @param count 繰り返し回数
     * @return 文字列
     */
    private static String repeat(char c, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(c);
        }
        return builder.toString();
    }

    // ------------------------------------------------------------------ D3-01〜D3-03（型として解釈されうる記法）

    /**
     * Given: 数値に見える文字列 {@code "100"} を持つデータ行。
     * When : 実 {@code .xlsx} へ {@code write} し、POI で開き直す。
     * Then : <b>文字列セル</b>（{@code CELL_TYPE_STRING}）になり、数値セルにはならない。
     *        値は {@code "100"} のまま。
     *
     * <p>担保する軸要素: D3-01。</p>
     */
    @Test
    public void writesNumericLookingStringAsStringCell() {
        // When
        Cell cell = writeAndReopen("100");

        // Then
        assertThat(cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat("数値セルにならない", cell.getCellType(), is(not(Cell.CELL_TYPE_NUMERIC)));
        assertThat(cell.getStringCellValue(), is("100"));
        // 数値としては取り出せない（＝数値セルでないことのもう一つの証拠）
        assertThrows(IllegalStateException.class, () -> cell.getNumericCellValue());
    }

    /**
     * Given: 数式に見える文字列 {@code "=1+1"} を持つデータ行。
     * When : 実 {@code .xlsx} へ {@code write} し、POI で開き直す。
     * Then : <b>文字列セル</b>になり、数式セルとして解釈されない。値は {@code "=1+1"} のまま
     *        （計算結果 {@code 2} にはならない）。
     *
     * <p>担保する軸要素: D3-02。</p>
     */
    @Test
    public void writesFormulaLookingStringAsStringCell() {
        // When
        Cell cell = writeAndReopen("=1+1");

        // Then
        assertThat(cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat("数式セルにならない", cell.getCellType(), is(not(Cell.CELL_TYPE_FORMULA)));
        assertThat("数式は評価されず記法のまま残る", cell.getStringCellValue(), is("=1+1"));
        // 数式としては取り出せない（＝数式セルでないことのもう一つの証拠）
        assertThrows(IllegalStateException.class, () -> cell.getCellFormula());
    }

    /**
     * Given: 先頭ゼロの文字列 {@code "007"} を持つデータ行。
     * When : 実 {@code .xlsx} へ {@code write} し、POI で開き直す。
     * Then : <b>文字列セル</b>になり、先頭ゼロが落ちない（{@code "7"} にならない）。
     *
     * <p>担保する軸要素: D3-03。</p>
     */
    @Test
    public void writesLeadingZeroStringAsStringCell() {
        // When
        Cell cell = writeAndReopen("007");

        // Then
        assertThat(cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat("数値セルにならない（先頭ゼロが落ちない）", cell.getCellType(), is(not(Cell.CELL_TYPE_NUMERIC)));
        assertThat(cell.getStringCellValue(), is("007"));
    }

    // ------------------------------------------------------------------ D3-04・D3-05（値なし）

    /**
     * Given: {@code null} 値を持つデータ行。
     * When : 実 {@code .xlsx} へ {@code write} し、POI で開き直す。
     * Then : <b>文字列セル</b>になり、値はリテラル {@code "null"}（4 文字の文字列）。
     *        空白セル（{@code CELL_TYPE_BLANK}）にはならない。
     *
     * <p>
     * 担保する軸要素: D3-04。値の側（{@code "null"} と書かれること・読み戻すと文字列 {@code "null"} に
     * なること）は {@code XlsFormatWriterTest#writesTableBlock} ／
     * {@code #roundTripsNullCellAsLiteralNullString} が既に固定しており、本テストはセル型を足す。
     * </p>
     */
    @Test
    public void writesNullValueAsLiteralNullStringCell() {
        // When
        Cell cell = writeAndReopen(null);

        // Then
        assertThat(cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat("空白セルにはならない", cell.getCellType(), is(not(Cell.CELL_TYPE_BLANK)));
        assertThat(cell.getStringCellValue(), is("null"));
    }

    /**
     * Given: 空文字 {@code ""} を持つデータ行。
     * When : 実 {@code .xlsx} へ {@code write} し、POI で開き直す。
     * Then : <b>文字列セル</b>のまま（長さ 0 の文字列）で、空白セル（{@code CELL_TYPE_BLANK}）には
     *        ならない。すなわちファイルへ直列化しても「値なしのセル」に退化しない。
     *
     * <p>担保する軸要素: D3-05。</p>
     */
    @Test
    public void writesEmptyValueAsEmptyStringCell() {
        // When
        Cell cell = writeAndReopen("");

        // Then
        assertThat(cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat("空白セルにはならない", cell.getCellType(), is(not(Cell.CELL_TYPE_BLANK)));
        assertThat(cell.getStringCellValue(), is(""));
    }

    // ------------------------------------------------------------------ D3-06 改行

    /**
     * Given: 改行（{@code LF}）を含む文字列を持つデータ行。
     * When : 実 {@code .xlsx} へ {@code write} し、POI で開き直す。
     * Then : <b>文字列セル</b>になり、改行がそのまま保たれる。
     *
     * <p>担保する軸要素: D3-06。</p>
     */
    @Test
    public void writesLineFeedStringAsStringCell() {
        // When
        Cell cell = writeAndReopen("a\nb");

        // Then
        assertThat(cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat(cell.getStringCellValue(), is("a\nb"));
    }

    /**
     * Given: {@code CRLF} 改行を含む文字列を持つデータ行。
     * When : 実 {@code .xlsx} へ {@code write} し、POI で開き直す。
     * Then : <b>文字列セル</b>になるが、{@code CR}（{@code U+000D}）が黙って落ち {@code LF} だけが残る。
     *
     * <p>
     * 担保する軸要素: D3-06（改行の異表記）。メモリ上のブックでは {@code CRLF} が保たれており
     * （{@code XlsFormatWriter#build} 直後の値は 4 文字）、失われるのはファイルへ直列化する区間である。
     * 変換前後で値が変わるため {@code issues.md} の <b>XLS-18</b> に課題として記録した（修正はしない）。
     * </p>
     */
    @Test
    public void dropsCarriageReturnFromCrLfStringCell() {
        // When
        Cell cell = writeAndReopen("a\r\nb");

        // Then
        assertThat("メモリ上のブックでは CRLF が保たれている（失われるのは直列化区間）",
                buildInMemory("a\r\nb"), is("a\r\nb"));
        assertThat(cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat("CR が落ちて LF だけが残る（issues.md XLS-18）", cell.getStringCellValue(), is("a\nb"));
        assertThat(cell.getStringCellValue().length(), is(3));
    }

    // ------------------------------------------------------------------ D3-07 32767 文字超

    /**
     * Given: Excel のセル文字数上限（32767）を 1 文字超える 32768 文字の値を持つデータ行。
     * When : 実 {@code .xlsx} へ {@code write} し、POI で開き直す。
     * Then : 例外にならず<b>文字列セル</b>として書かれ、32768 文字がそのまま読み戻せる
     *        （切り詰めも例外も起きない）。
     *
     * <p>
     * 担保する軸要素: D3-07。上限超過を誰も検査しないため、Excel の仕様上不正な長さのセルを持つ
     * ブックが黙って生成される。{@code issues.md} の <b>XLS-19</b> に記録した（修正はしない）。
     * </p>
     */
    @Test
    public void writesStringLongerThanExcelCellLimitAsStringCell() {
        // Given
        String tooLong = repeat('x', EXCEL_MAX_CELL_LENGTH + 1);

        // When
        Cell cell = writeAndReopen(tooLong);

        // Then
        assertThat(cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat("切り詰められない（issues.md XLS-19）",
                cell.getStringCellValue().length(), is(EXCEL_MAX_CELL_LENGTH + 1));
        assertThat(cell.getStringCellValue(), is(tooLong));
    }

    /**
     * Given: Excel のセル文字数上限ちょうど（32767 文字）の値を持つデータ行。
     * When : 実 {@code .xlsx} へ {@code write} し、POI で開き直す。
     * Then : <b>文字列セル</b>としてそのまま書かれる（上限内の正常側の境界）。
     *
     * <p>担保する軸要素: D3-07 の境界（上限ちょうど）。</p>
     */
    @Test
    public void writesStringOfExcelCellLimitLengthAsStringCell() {
        // Given
        String atLimit = repeat('x', EXCEL_MAX_CELL_LENGTH);

        // When
        Cell cell = writeAndReopen(atLimit);

        // Then
        assertThat(cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat(cell.getStringCellValue().length(), is(EXCEL_MAX_CELL_LENGTH));
    }

    // ------------------------------------------------------------------ D3-08 制御文字

    /**
     * Given: XML 1.0 で使えない制御文字（{@code NUL}／{@code BEL}／{@code VT}／{@code US}）を含む値。
     * When : 実 {@code .xlsx} へ {@code write} し、POI で開き直す。
     * Then : <b>文字列セル</b>になるが、当該文字が黙って {@code ?}（{@code U+003F}）へ置き換わる。
     *        文字数は変わらない。
     *
     * <p>
     * 担保する軸要素: D3-08。メモリ上のブックでは制御文字が保たれており
     * （{@code XlsFormatWriter#build} 直後は入力と同一）、置換されるのはファイルへ直列化する区間である。
     * 変換前後で値が変わるため {@code issues.md} の <b>XLS-17</b> に課題として記録した（修正はしない）。
     * 制御文字はソースに直接書くと編集経路で失われるため、{@code (char)} キャストで組み立てる。
     * </p>
     */
    @Test
    public void replacesXmlIllegalControlCharactersWithQuestionMark() {
        // Given / When / Then（4 文字それぞれで同じ挙動になることを確かめる）
        char[] illegals = {(char) 0x00, (char) 0x07, (char) 0x0B, (char) 0x1F};
        for (char illegal : illegals) {
            String label = String.format("U+%04X", (int) illegal);
            String value = "a" + illegal + "b";
            Cell cell = writeAndReopen(value);
            assertThat(label + " はメモリ上のブックでは保たれている（失われるのは直列化区間）",
                    buildInMemory(value), is(value));
            assertThat(label, cell.getCellType(), is(Cell.CELL_TYPE_STRING));
            assertThat(label + " は ? へ置き換わる（issues.md XLS-17）",
                    cell.getStringCellValue(), is("a?b"));
        }
    }

    /**
     * Given: XML 1.0 で使える制御文字（{@code TAB}／{@code DEL}）を含む値。
     * When : 実 {@code .xlsx} へ {@code write} し、POI で開き直す。
     * Then : <b>文字列セル</b>になり、当該文字がそのまま保たれる（置換されない）。
     *
     * <p>
     * 担保する軸要素: D3-08 の対照。置換されるのは「制御文字だから」ではなく
     * 「XML 1.0 で表現できない文字だから」であることを、置換されない制御文字で示す
     * （{@code issues.md} XLS-17 の原因の裏付け）。
     * </p>
     */
    @Test
    public void writesXmlLegalControlCharactersAsIs() {
        // Given / When / Then
        char[] legals = {(char) 0x09, (char) 0x7F};
        for (char legal : legals) {
            String label = String.format("U+%04X", (int) legal);
            Cell cell = writeAndReopen("a" + legal + "b");
            assertThat(label, cell.getCellType(), is(Cell.CELL_TYPE_STRING));
            assertThat(label + " は保たれる", cell.getStringCellValue(), is("a" + legal + "b"));
        }
    }
}
