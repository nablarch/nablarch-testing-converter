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
 * {@code XlsFormatWriterTest} はセルの<b>値</b>だけを {@code getStringCellValue()} で見る。
 * 実 {@code .xlsx} を書いて開き直すこと自体は同クラスの
 * {@code #writesWorkbookFileWithSheetPerSection} が既に行っているため、本クラスの新規性は
 * ファイルを経由すること<b>ではなく</b>、{@link Cell#getCellType()} を突き合わせることである。
 * すなわち「値 → セル → ファイル → 読み戻し」を通したうえで、
 * <b>数値・数式として解釈されないこと</b>をセル型で固定する。
 * </p>
 *
 * <p>
 * <b>ファイルを経由する理由。</b>メモリ上のブックだけを見ていると値が変わることに気づけないケースが
 * 実在する（下記 D3-06・D3-08）。ただし<b>変化の起きる区間は 2 つあり、両者は違う</b>。
 * </p>
 * <ul>
 *   <li><b>D3-08 制御文字（{@code issues.md} XLS-17）— 直列化区間。</b>
 *       書き出した {@code .xlsx} の {@code xl/sharedStrings.xml} を展開すると
 *       {@code <t>a?b</t>} となっており、{@code ?}（{@code U+003F}）が<b>ファイルに焼き込まれている</b>。</li>
 *   <li><b>D3-06 の {@code CR}（{@code issues.md} XLS-18）— 読み戻し（XML パース）区間。</b>
 *       同じダンプで {@code CR} は {@code <t>a[CR]b</t>} と<b>生のまま保存されており</b>
 *       （数値文字参照 {@code &#13;} への退避も無い）、{@code LF} へ変わるのは XML を読み直すときである。
 *       したがって<b>{@code .xlsx} をバイトで比較しても {@code CR} は残って見え</b>、
 *       ファイルを探しても変化の原因は見つからない。</li>
 * </ul>
 *
 * <p>
 * この違いにより、本クラスの CR 系メソッドが置く「メモリ上のブックでは保たれている」というアサートは、
 * 「直列化で失われた」ことの証明<b>ではない</b>（ファイルにも残っているため）。
 * 証明しているのは「{@code XlsFormatWriter} 自身は値を変えていない」ことだけである。
 * </p>
 *
 * <p>
 * 各ケースは {@code SETUP_TABLE} の 1 データ行（{@code KEY} 列＝ケース識別・{@code V} 列＝検証対象セル）
 * として与え、{@code V} 列のセルを検証する。
 * </p>
 *
 * <p>
 * <b>アサートの読み方。</b>{@code getCellType()} が返すのは排他的な {@code int} 定数であり、
 * 本クラスで現れうるのは次の 4 つである（POI 3.8 はほかに {@code CELL_TYPE_BOOLEAN=4}／
 * {@code CELL_TYPE_ERROR=5} も定義するが、{@link XlsFormatWriter} は
 * {@code Cell#setCellValue(String)} しか呼ばないため本クラスでは現れない）:
 * {@code CELL_TYPE_NUMERIC=0}／{@code CELL_TYPE_STRING=1}／{@code CELL_TYPE_FORMULA=2}／
 * {@code CELL_TYPE_BLANK=3}。したがって {@code is(CELL_TYPE_STRING)} が通れば、
 * 同じセルに対する {@code is(not(CELL_TYPE_NUMERIC))} などの否定形も、
 * {@code getNumericCellValue()} ／ {@code getCellFormula()} が {@code IllegalStateException} に
 * なることも<b>必ず成り立つ</b>。本クラスの否定形アサートと {@code assertThrows} は、
 * 完了条件の文言（「数値セルにならない」「数式として解釈されない」）を字面どおり残すために置いてあるだけで、
 * <b>型アサートとは独立した担保ではない</b>。実装が壊れたときに落ちるのは型アサートの行である。
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
        // D3-04（value == null）を通すため Arrays.asList を使う。List.of は null 要素を拒否するので
        // ここを List.of へ置き換えると writesNullValueAsLiteralNullStringCell が壊れる
        // （同趣旨の注意書きが XlsFormatWriterTest#row の Javadoc にもある）。
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
     * XML 1.0 で表現できない制御文字が {@code ?} へ置換されることを、1 文字ぶん確かめる。
     *
     * <p>
     * 制御文字はソースに直接書くと編集経路で失われるため、呼び出し側は {@code (char)} キャストで渡す。
     * </p>
     *
     * @param illegal XML 1.0 で表現できない制御文字
     */
    private void assertReplacedWithQuestionMark(char illegal) {
        // Given
        String value = "a" + illegal + "b";
        String label = String.format("U+%04X", (int) illegal);

        // When
        String inMemory = buildInMemory(value);
        Cell cell = writeAndReopen(value);

        // Then
        assertThat(label + " はメモリ上のブックでは保たれている（失われるのは直列化区間）", inMemory, is(value));
        assertThat(label, cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat(label + " は ? へ置き換わる（issues.md XLS-17）", cell.getStringCellValue(), is("a?b"));
    }

    /**
     * XML 1.0 で表現できる制御文字がそのまま保たれることを、1 文字ぶん確かめる。
     *
     * @param legal XML 1.0 で表現できる制御文字
     */
    private void assertWrittenAsIs(char legal) {
        // Given
        String value = "a" + legal + "b";
        String label = String.format("U+%04X", (int) legal);

        // When
        Cell cell = writeAndReopen(value);

        // Then
        assertThat(label, cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat(label + " は保たれる", cell.getStringCellValue(), is(value));
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
        assertThat(cell.getStringCellValue(), is("100"));
        // 以下 2 行は上の型アサートの帰結を字面どおり書き下したものであり、独立した担保ではない
        // （クラス Javadoc「アサートの読み方」参照）。
        assertThat("数値セルにならない", cell.getCellType(), is(not(Cell.CELL_TYPE_NUMERIC)));
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
        assertThat("数式は評価されず記法のまま残る", cell.getStringCellValue(), is("=1+1"));
        // 以下 2 行は上の型アサートの帰結を字面どおり書き下したものであり、独立した担保ではない
        // （クラス Javadoc「アサートの読み方」参照）。
        assertThat("数式セルにならない", cell.getCellType(), is(not(Cell.CELL_TYPE_FORMULA)));
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
        assertThat(cell.getStringCellValue(), is("007"));
        // 型アサートの帰結（独立した担保ではない。クラス Javadoc「アサートの読み方」参照）。
        assertThat("数値セルにならない（先頭ゼロが落ちない）", cell.getCellType(), is(not(Cell.CELL_TYPE_NUMERIC)));
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
        assertThat(cell.getStringCellValue(), is("null"));
        // 型アサートの帰結（独立した担保ではない。クラス Javadoc「アサートの読み方」参照）。
        assertThat("空白セルにはならない", cell.getCellType(), is(not(Cell.CELL_TYPE_BLANK)));
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
        assertThat(cell.getStringCellValue(), is(""));
        // 型アサートの帰結（独立した担保ではない。クラス Javadoc「アサートの読み方」参照）。
        assertThat("空白セルにはならない", cell.getCellType(), is(not(Cell.CELL_TYPE_BLANK)));
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
     * Then : <b>文字列セル</b>になるが、{@code CRLF}（2 文字）が黙って {@code LF} 1 文字へ正規化される。
     *
     * <p>
     * 担保する軸要素: D3-06（改行の異表記）。メモリ上のブックでは {@code CRLF} が保たれており
     * （{@code XlsFormatWriter#build} 直後の値は 4 文字）、書き出した {@code .xlsx} の
     * {@code xl/sharedStrings.xml} にも {@code CR} は<b>生のまま保存されている</b>（実測）。
     * したがって<b>変わるのは読み戻し（XML パース）区間</b>であって直列化区間ではない。
     * XML の行末正規化は「{@code CR} を捨てる」のではなく「{@code CR} を {@code LF} へ置き換える」
     * 規則であり、{@code CRLF} の場合だけ 2 文字が 1 文字にまとまるため長さが減る
     * （{@code CR} 単独では長さが変わらない。{@code #replacesLoneCarriageReturnWithLineFeedInStringCell}）。
     * 変換前後で値が変わるため {@code issues.md} の <b>XLS-18</b> に課題として記録した（修正はしない）。
     * </p>
     */
    @Test
    public void replacesCrLfWithSingleLineFeedInStringCell() {
        // When
        String inMemory = buildInMemory("a\r\nb");
        Cell cell = writeAndReopen("a\r\nb");

        // Then
        assertThat("メモリ上のブックでは CRLF が保たれている（CR はファイルにも残る。変わるのは読み戻し区間）",
                inMemory, is("a\r\nb"));
        assertThat(cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat("CRLF が LF 1 文字へまとまる（issues.md XLS-18）",
                cell.getStringCellValue(), is("a\nb"));
    }

    /**
     * Given: {@code LF} を伴わない単独の {@code CR} を含む文字列を持つデータ行。
     * When : 実 {@code .xlsx} へ {@code write} し、POI で開き直す。
     * Then : <b>文字列セル</b>になるが、{@code CR} が黙って {@code LF} へ置き換わる。
     *        <b>文字数は 3 文字のまま変わらない。</b>
     *
     * <p>
     * 担保する軸要素: D3-06（改行の異表記）。{@code CRLF}（{@code #replacesCrLfWithSingleLineFeedInStringCell}）
     * では 4 文字が 3 文字になるため長さの差で気づけるが、単独 {@code CR} では長さが変わらないため
     * 差分の長さでは気づけない。すなわち XML の行末正規化は削除ではなく<b>置換</b>である
     * （{@code issues.md} <b>XLS-18</b>）。書き出した {@code .xlsx} の {@code xl/sharedStrings.xml} には
     * {@code CR} が生のまま入っており（実測）、<b>変わるのは読み戻し（XML パース）区間</b>である。
     * </p>
     */
    @Test
    public void replacesLoneCarriageReturnWithLineFeedInStringCell() {
        // When
        String inMemory = buildInMemory("a\rb");
        Cell cell = writeAndReopen("a\rb");

        // Then
        assertThat("メモリ上のブックでは CR が保たれている（CR はファイルにも残る。変わるのは読み戻し区間）",
                inMemory, is("a\rb"));
        assertThat(cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat("CR が LF へ置き換わる（長さは変わらないため差分の長さでは気づけない。issues.md XLS-18）",
                cell.getStringCellValue(), is("a\nb"));
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
        String tooLong = "x".repeat(EXCEL_MAX_CELL_LENGTH + 1);

        // When
        Cell cell = writeAndReopen(tooLong);

        // Then
        assertThat(cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat("切り詰められず 32768 文字がそのまま読み戻せる（issues.md XLS-19）",
                cell.getStringCellValue(), is(tooLong));
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
        String atLimit = "x".repeat(EXCEL_MAX_CELL_LENGTH);

        // When
        Cell cell = writeAndReopen(atLimit);

        // Then
        assertThat(cell.getCellType(), is(Cell.CELL_TYPE_STRING));
        assertThat("32767 文字が内容ごとそのまま読み戻せる", cell.getStringCellValue(), is(atLimit));
    }

    // ------------------------------------------------------------------ D3-08 制御文字

    /*
     * D3-08 は文字ごとに 1 メソッドへ分けてある（姉妹クラス XlsFormatReaderCellTypeTest が
     * 1 ケース 1 @Test で展開しているのに合わせた。同クラスの @Test は 19 個）。ループで束ねると
     * 最初の 1 文字が落ちた時点で残りが実行されず、どの文字で挙動が違うのかが分からなくなるためである。
     *
     * 以下 4 件（NUL／BEL／VT／US）は XML 1.0 で表現できない制御文字。
     * 実 .xlsx へ書き出すと当該文字が黙って ?（U+003F）へ置き換わる（文字数は変わらない）。
     * メモリ上のブックでは保たれており、置換されるのはファイルへ直列化する区間である
     * （xl/sharedStrings.xml に <t>a?b</t> と ? が焼き込まれていることを実測。CR とは違い、
     *   ファイルを見れば変化が分かる。クラス Javadoc「ファイルを経由する理由」参照）。
     * 変換前後で値が変わるため issues.md の XLS-17 に課題として記録した（修正はしない）。
     * 担保する軸要素: D3-08。
     */

    /**
     * {@code NUL}（{@code U+0000}）を含む値が {@code ?} へ置換されて書かれる。
     */
    @Test
    public void replacesNulCharacterWithQuestionMark() {
        assertReplacedWithQuestionMark((char) 0x00);
    }

    /**
     * {@code BEL}（{@code U+0007}）を含む値が {@code ?} へ置換されて書かれる。
     */
    @Test
    public void replacesBellCharacterWithQuestionMark() {
        assertReplacedWithQuestionMark((char) 0x07);
    }

    /**
     * {@code VT}（{@code U+000B}）を含む値が {@code ?} へ置換されて書かれる。
     */
    @Test
    public void replacesVerticalTabCharacterWithQuestionMark() {
        assertReplacedWithQuestionMark((char) 0x0B);
    }

    /**
     * {@code US}（{@code U+001F}）を含む値が {@code ?} へ置換されて書かれる。
     */
    @Test
    public void replacesUnitSeparatorCharacterWithQuestionMark() {
        assertReplacedWithQuestionMark((char) 0x1F);
    }

    /*
     * 以下 2 件（TAB／DEL）は D3-08 の対照。置換されるのは「制御文字だから」ではなく
     * 「XML 1.0 で表現できない文字だから」であることを、置換されない制御文字で示す
     * （issues.md XLS-17 の原因の裏付け）。
     */

    /**
     * {@code TAB}（{@code U+0009}）を含む値はそのまま書かれる（置換されない）。
     */
    @Test
    public void writesTabCharacterAsIs() {
        assertWrittenAsIs((char) 0x09);
    }

    /**
     * {@code DEL}（{@code U+007F}）を含む値はそのまま書かれる（置換されない）。
     */
    @Test
    public void writesDeleteCharacterAsIs() {
        assertWrittenAsIs((char) 0x7F);
    }
}
