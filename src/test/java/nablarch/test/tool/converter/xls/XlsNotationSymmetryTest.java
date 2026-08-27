package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Excel 形式の読み書きが<b>記法⇄値の対称な写像</b>であることを、実 {@code .xlsx} を起点に固定するテスト。
 *
 * <p>
 * 中間モデルが持つのは「テスティングフレームワークが解釈したあとの値」（Java {@code null} または
 * {@link String}）であって Excel 形式固有の記法ではない（{@code tools/testdata_converter.rst:14}・
 * {@code :22}・{@code :34}-{@code :35}）。読み（{@link XlsFormatReader}）で外した記法を
 * 書き（{@link XlsFormatWriter}）で戻すことで写像が対称になる。
 * </p>
 *
 * <p>
 * 本クラスは対称性の<b>原因側</b>を固定する。すなわち、
 * </p>
 * <ul>
 *   <li>記法 → 値（読み）: {@code null} リテラル・引用符記法・{@code \r} が解釈されること</li>
 *   <li>値 → 記法（書き）: その逆写像がセルへ書かれること</li>
 *   <li>実 {@code .xlsx} 起点の往復で、値も版面の記法も保たれること</li>
 * </ul>
 *
 * <p>
 * {@code implementation/testdata_notation.rst} の特殊記法の表を<b>全行・4 経路</b>で押さえるのは
 * 別タスク（完了条件3）の担当であり、本クラスは重複しない。
 * </p>
 *
 * <p>
 * 版面の見た目（セル文字列）を突き合わせるため、入力・出力とも実 {@code .xlsx} を POI で開いて確かめる。
 * </p>
 *
 * @author kiyobot
 */
public class XlsNotationSymmetryTest {

    /** フィクスチャの既定ブック名。 */
    private static final String BOOK = "notationBook";

    /** フィクスチャの既定シート名。 */
    private static final String SHEET = "sheet1";

    /** CR（{@code \r} 記法が解釈された結果）。 */
    private static final String CR = "\r";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // ------------------------------------------------------------------ helpers

    /** フィクスチャ {@code .xlsx} の入力ディレクトリ。 */
    private Path in() {
        return folder.getRoot().toPath();
    }

    /** 往復の 2 周目を書き出す先（入力ブックを上書きしないよう分ける）。 */
    private Path out() {
        return folder.getRoot().toPath().resolve("out");
    }

    /**
     * 1 行のデータ行を持つ {@code SETUP_TABLE} を実 {@code .xlsx} として書き出す。
     *
     * @param columns カラム名
     * @param values  データ行の値（記法のまま）
     */
    private void writeBook(List<String> columns, List<String> values) {
        XlsFixture fixture = XlsFixture.book(BOOK).sheet(SHEET).row(text("SETUP_TABLE=T"));
        fixture.row(cells(columns)).row(cells(values)).writeTo(in());
    }

    /**
     * 文字列リストを文字列セル指定の配列へ写す。
     *
     * @param values 値
     * @return セル指定
     */
    private static XlsFixture.CellSpec[] cells(List<String> values) {
        XlsFixture.CellSpec[] specs = new XlsFixture.CellSpec[values.size()];
        for (int i = 0; i < values.size(); i++) {
            specs[i] = text(values.get(i));
        }
        return specs;
    }

    /**
     * 指定ディレクトリの既定ブック／シートを読み、唯一のテーブルブロックを返す。
     *
     * @param dir ディレクトリ
     * @return テーブルブロック
     */
    private static TableDataBlock table(Path dir) {
        TestDataContainer container = new XlsFormatReader().read(dir.toString(), BOOK + "/" + SHEET);
        assertThat("セクション数", container.getSections().size(), is(1));
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        assertThat("ブロック数", blocks.size(), is(1));
        return (TableDataBlock) blocks.get(0);
    }

    /**
     * 指定ディレクトリの既定ブックのデータ行（3 行目）のセル文字列を返す。
     *
     * @param dir ディレクトリ
     * @return データ行のセル文字列
     */
    private static List<String> dataRowCells(Path dir) {
        Workbook workbook = XlsFixture.open(dir.resolve(BOOK + ".xlsx"));
        Sheet sheet = workbook.getSheet(SHEET);
        return XlsFixture.line(sheet, 2);
    }

    /**
     * 入力ブックを中間モデルとして読む。
     *
     * @return 中間モデル
     */
    private TestDataContainer read() {
        return new XlsFormatReader().read(in().toString(), BOOK + "/" + SHEET);
    }

    // ------------------------------------------------------------------ 記法 → 値（読み）

    /**
     * Given: セルに {@code null}／{@code Null}／{@code NULL} と書いた実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : いずれも Java {@code null} になる（大文字小文字を区別しない）。
     */
    @Test
    public void readsNullNotationAsJavaNull() {
        // Given
        writeBook(Arrays.asList("A", "B", "C"), Arrays.asList("null", "Null", "NULL"));

        // When
        List<String> row = table(in()).getRows().get(0);

        // Then
        assertThat("null", row.get(0), is(nullValue()));
        assertThat("Null", row.get(1), is(nullValue()));
        assertThat("NULL", row.get(2), is(nullValue()));
    }

    /**
     * Given: セルに {@code "null"}／{@code "NULL"} と書いた実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 引用符が外れた<b>文字列</b> {@code null}／{@code NULL} になる（Java {@code null} にならない）。
     */
    @Test
    public void readsQuotedNullNotationAsStringNull() {
        // Given
        writeBook(Arrays.asList("A", "B"), Arrays.asList("\"null\"", "\"NULL\""));

        // When
        List<String> row = table(in()).getRows().get(0);

        // Then
        assertThat(row.get(0), is("null"));
        assertThat(row.get(1), is("NULL"));
    }

    /**
     * Given: セルに {@code """}（ダブルクォート 3 つ）と書いた実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : ダブルクォート 1 文字になる。
     */
    @Test
    public void readsTripleQuoteNotationAsSingleDoubleQuote() {
        // Given
        writeBook(Arrays.asList("A"), Arrays.asList("\"\"\""));

        // When / Then
        assertThat(table(in()).getRows().get(0).get(0), is("\""));
    }

    /**
     * Given: セルに {@code \r}／{@code \n}／セル内 LF と書いた実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code \r} だけが CR になり、{@code \n} は 2 文字のまま・LF は LF のまま素通しになる
     *        （{@code implementation/testdata_notation.rst:1391}）。
     */
    @Test
    public void readsBackslashRNotationAsCarriageReturn() {
        // Given
        writeBook(Arrays.asList("A", "B", "C"), Arrays.asList("x\\ry", "x\\ny", "x\ny"));

        // When
        List<String> row = table(in()).getRows().get(0);

        // Then
        assertThat("\\r は CR へ", row.get(0), is("x" + CR + "y"));
        assertThat("\\n は 2 文字のまま", row.get(1), is("x\\ny"));
        assertThat("セル内 LF は LF のまま", row.get(2), is("x\ny"));
    }

    /**
     * Given: セルに {@code ${systemTime}} と書いた実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 記法のまま保たれる（{@code ${...}} 系は解決しない。{@code tools/testdata_converter.rst:61}）。
     */
    @Test
    public void keepsDollarNotationUninterpreted() {
        // Given
        writeBook(Arrays.asList("A", "B"), Arrays.asList("${systemTime}", "${半角英字,10}"));

        // When
        List<String> row = table(in()).getRows().get(0);

        // Then
        assertThat(row.get(0), is("${systemTime}"));
        assertThat(row.get(1), is("${半角英字,10}"));
    }

    // ------------------------------------------------------------------ 値 → 記法（書き）

    /**
     * Given: 実 {@code .xlsx} 起点で読んだ、記法 6 種を含む 1 行。
     * When : 中間モデルを Excel 形式へ書き戻す。
     * Then : 版面のデータ行のセル文字列が<b>入力と同じ記法</b>に戻る（値 → 記法の逆写像）。
     */
    @Test
    public void writesBackTheSameNotationAsTheSourceBook() {
        // Given
        List<String> notations = Arrays.asList("null", "\"null\"", "\"\"\"", "x\\ry", "x\\ny", "${systemTime}");
        writeBook(Arrays.asList("A", "B", "C", "D", "E", "F"), notations);

        // When
        new XlsFormatWriter().write(read(), out().toString());

        // Then
        assertThat(dataRowCells(out()), is(notations));
    }

    /**
     * Given: 前後を全角ダブルクォートで囲んだ値（{@code ”ab”}）と、半角で囲んだ値（{@code "ab"}）。
     * When : 実 {@code .xlsx} 起点で読み、書き戻す。
     * Then : 読みでは外側 1 層が外れて {@code ab} になり、書きでは囲まずにそのまま書く
     *        （{@code ab} は引用符記法ではないため戻す必要が無い）。
     *
     * <p>
     * 全角で囲んだ記法は<b>半角で囲んだ記法へ写る</b>（値としては等価。往復で値は保たれるが、
     * 版面の全角／半角の別は保たれない）。
     * </p>
     */
    @Test
    public void readsQuotedValueAsPlainValueAndWritesItWithoutQuotes() {
        // Given
        writeBook(Arrays.asList("A", "B"), Arrays.asList("”ab”", "\"ab\""));

        // When
        List<String> read = table(in()).getRows().get(0);
        new XlsFormatWriter().write(read(), out().toString());

        // Then
        assertThat("読み: 外側 1 層が外れる", read, is(Arrays.asList("ab", "ab")));
        assertThat("書き: どちらも半角で囲まずそのまま（値 ab は引用符記法ではない）",
                dataRowCells(out()), is(Arrays.asList("ab", "ab")));
        assertThat("往復後も値は ab のまま", table(out()).getRows().get(0), is(Arrays.asList("ab", "ab")));
    }

    // ------------------------------------------------------------------ 実ファイル起点の往復

    /**
     * Given: 症状 4 件の記法（{@code null}／{@code "null"}／{@code """}／{@code \r}）を含む実 {@code .xlsx}。
     * When : 実 {@code .xlsx} → 中間モデル → 実 {@code .xlsx} → 中間モデル と往復させる。
     * Then : 値が 1 周目と一致する。
     *
     * <p>
     * 4 件の症状はいずれもここで再現しなくなる —— {@code null} が文字列 {@code null} に化けない／
     * {@code "null"} が Java {@code null} に化けない／{@code """} の再読込が例外にならない／
     * {@code \r} が 2 文字のまま残らない。
     * </p>
     */
    @Test
    public void roundTripsSpecialNotationsFromRealBookWithoutLoss() {
        // Given
        writeBook(Arrays.asList("A", "B", "C", "D"),
                Arrays.asList("null", "\"null\"", "\"\"\"", "x\\ry"));

        // When
        List<String> first = table(in()).getRows().get(0);
        new XlsFormatWriter().write(read(), out().toString());
        List<String> second = table(out()).getRows().get(0);

        // Then
        assertThat("1 周目の値", first, is(Arrays.asList(null, "null", "\"", "x" + CR + "y")));
        assertThat("往復後も同じ値", second, is(first));
    }
}
