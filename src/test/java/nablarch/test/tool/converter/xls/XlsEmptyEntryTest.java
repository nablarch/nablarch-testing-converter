package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.blank;
import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nablarch.test.core.db.TableData;
import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.PoiXlsReader;
import nablarch.test.core.reader.TestCoreReaderAdapter;
import nablarch.test.tool.converter.model.FieldDef;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;
import nablarch.test.tool.converter.yaml.YamlFormatReader;
import nablarch.test.tool.converter.yaml.YamlFormatWriter;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 「全要素が空文字のエントリ」を Excel 形式で失わないことを、実 {@code .xlsx} を起点に固定するテスト。
 *
 * <p>
 * 空エントリの読み飛ばしは<b>記法として空</b>のエントリだけを対象とする
 * （記法はExcel では行の全セルが空の場合）。
 * 半角 {@code ""}（2 文字）は<b>空文字を表す記法であって空セルではない</b>ため、読み飛ばしの対象ではない。
 * 本体も同じ扱いで、空エントリの判定を解釈前の生セルで行う
 * （{@code PoiXlsReader} が 記法の
 * {@code isBlankLine} で生セルの行を捨て、{@code TestDataParsingTemplate} の空行判定も
 * 記法の {@code interpret} より前にある）。
 * </p>
 *
 * <p>
 * 逆向き（書き）も同じ理由で対称になる。全要素が空文字のエントリを空セルだけの行として書くと、
 * 次に読むときに空エントリとして落ちる。全フィールドが空文字のレコードは、
 * いずれか 1 つのフィールドに {@code ""} と書いて表す。
 * </p>
 *
 * <p>
 * 本クラスは<b>件数の保存</b>を押さえる。記法⇄値の写像そのものは {@link XlsNotationSymmetryTest} の担当であり、
 * 特殊記法の表を全行・4 経路で押さえるのは別タスク（完了条件3）の担当である。
 * </p>
 *
 * @author kiyobot
 */
public class XlsEmptyEntryTest {

    /** フィクスチャの既定ブック名。 */
    private static final String BOOK = "emptyEntryBook";

    /** フィクスチャの既定シート名。 */
    private static final String SHEET = "sheet1";

    /** 空文字を表す Excel 記法（半角ダブルクォート 2 文字）。 */
    private static final String QUOTED_EMPTY = "\"\"";

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

    /** YAML 経由の往復で YAML を置く先。 */
    private Path via() {
        return folder.getRoot().toPath().resolve("via");
    }

    /** 既定ブックのリソース名（{@code ブック名/シート名}）。 */
    private static String resource() {
        return BOOK + "/" + SHEET;
    }

    /**
     * 指定ディレクトリの既定ブック／シートを読み、唯一のブロックを返す。
     *
     * @param dir ディレクトリ
     * @return ブロック
     */
    private static TestDataBlock block(Path dir) {
        TestDataContainer container = new XlsFormatReader().read(dir.toString(), resource());
        assertThat("セクション数", container.getSections().size(), is(1));
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        assertThat("ブロック数", blocks.size(), is(1));
        return blocks.get(0);
    }

    /**
     * 指定ディレクトリの既定ブックの指定行のセル文字列を返す。
     *
     * @param dir ディレクトリ
     * @param r   0 起算の行番号
     * @return セル文字列
     */
    private static List<String> cells(Path dir, int r) {
        Workbook workbook = XlsFixture.open(dir.resolve(BOOK + ".xlsx"));
        Sheet sheet = workbook.getSheet(SHEET);
        return XlsFixture.line(sheet, r);
    }

    /**
     * 本体（{@code PoiXlsReader}）がテーブルとして読むエントリ件数を返す。
     *
     * @param dir ディレクトリ
     * @param id  テーブル名
     * @return エントリ件数
     */
    private static int frameworkTableSize(Path dir, String id) {
        List<TableData> tables =
                new TestCoreReaderAdapter(new PoiXlsReader())
                        .readTables(dir.toString(), resource(), "", DataType.SETUP_TABLE_DATA);
        for (TableData table : tables) {
            if (id.equals(table.getTableName())) {
                return table.size();
            }
        }
        throw new IllegalStateException("テーブルが見つかりません: " + id);
    }

    /**
     * 本体（{@code PoiXlsReader}）が {@code LIST_MAP} として読むエントリ件数を返す。
     *
     * @param dir ディレクトリ
     * @param id  {@code LIST_MAP} の識別子
     * @return エントリ件数
     */
    private static int frameworkListMapSize(Path dir, String id) {
        return new TestCoreReaderAdapter(new PoiXlsReader())
                .readListMap(dir.toString(), resource(), id).size();
    }

    /** 中間モデルを 1 ブロックのコンテナへ包む。 */
    private static TestDataContainer container(TestDataBlock block) {
        return new TestDataContainer(BOOK, Collections.singletonList(
                new TestDataSection(SHEET, Collections.singletonList(block))));
    }

    // ------------------------------------------------------------------ 読み —— 記法として空でないエントリは残る

    /**
     * Given: テーブルの 2 行目の全セルに {@code ""}（2 文字）と書いた実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 2 行目が読み飛ばされず、全要素が空文字のエントリとして残る。
     */
    @Test
    public void keepsTableEntryWhoseCellsAreAllQuotedEmptyString() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("SETUP_TABLE=T"))
                .row(text("ID"), text("NAME"))
                .row(text("1"), text("山田"))
                .row(text(QUOTED_EMPTY), text(QUOTED_EMPTY))
                .writeTo(in());

        // When
        TableDataBlock table = (TableDataBlock) block(in());

        // Then
        assertThat("エントリ件数", table.getRows().size(), is(2));
        assertThat(table.getRows().get(1), is(Arrays.asList("", "")));
    }

    /**
     * Given: {@code LIST_MAP} の 2 行目の全セルに {@code ""} と書いた実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 2 行目が読み飛ばされず、全要素が空文字のエントリとして残る。
     */
    @Test
    public void keepsListMapEntryWhoseCellsAreAllQuotedEmptyString() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("LIST_MAP=L"))
                .row(text("ID"), text("NAME"))
                .row(text("1"), text("山田"))
                .row(text(QUOTED_EMPTY), text(QUOTED_EMPTY))
                .writeTo(in());

        // When
        ListMapBlock listMap = (ListMapBlock) block(in());

        // Then
        assertThat("エントリ件数", listMap.getRows().size(), is(2));
        assertThat(listMap.getRows().get(1), is(Arrays.asList("", "")));
    }

    /**
     * Given: 全セルが {@code ""} の行と、全セルが空セルの行の両方を持つ実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 読み込むエントリ件数が本体（{@code PoiXlsReader}）と一致する。
     *
     * <p>
     * 全セルが空セルの行だけが読み飛ばされる。
     * </p>
     */
    @Test
    public void readsAsManyTableEntriesAsTheFrameworkDoes() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("SETUP_TABLE=T"))
                .row(text("ID"), text("NAME"))
                .row(text("1"), text("山田"))
                .row(text(QUOTED_EMPTY), text(QUOTED_EMPTY))
                .row(blank(), blank())
                .writeTo(in());

        // When
        TableDataBlock table = (TableDataBlock) block(in());

        // Then
        assertThat("本体が読む件数", frameworkTableSize(in(), "T"), is(2));
        assertThat("converter が読む件数", table.getRows().size(), is(2));
    }

    /**
     * Given: 全セルが空セル（1 つは書式だけ、1 つはセルそのものが無い）の行を持つ実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : その行は読み飛ばされる（記法の規則そのもの）。
     */
    @Test
    public void dropsTableEntryWhoseCellsAreAllBlank() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("SETUP_TABLE=T"))
                .row(text("ID"), text("NAME"))
                .row(text("1"), text("山田"))
                .row(blank(), blank())
                .writeTo(in());

        // When
        TableDataBlock table = (TableDataBlock) block(in());

        // Then
        assertThat(table.getRows(), is(Collections.singletonList(Arrays.asList("1", "山田"))));
    }

    // ------------------------------------------------------------------ 書き —— 空文字だけのエントリを記法で書く

    /**
     * Given: 全要素が空文字のエントリを持つテーブルの中間モデル。
     * When : Excel 形式へ書き出す。
     * Then : そのエントリの<b>各セル</b>へ {@code ""} と書かれる。
     */
    @Test
    public void writesEveryCellOfAllEmptyTableEntryAsQuotedEmptyString() {
        // Given
        TableDataBlock block = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                Arrays.asList("ID", "NAME"),
                Arrays.asList(Arrays.asList("1", "山田"), Arrays.asList("", "")));

        // When
        new XlsFormatWriter().write(container(block), in().toString());

        // Then
        assertThat("通常の行はそのまま", cells(in(), 2), is(Arrays.asList("1", "山田")));
        assertThat(cells(in(), 3), is(Arrays.asList(QUOTED_EMPTY, QUOTED_EMPTY)));
    }

    /**
     * Given: 全要素が空文字のエントリを持つ {@code LIST_MAP} の中間モデル。
     * When : Excel 形式へ書き出す。
     * Then : そのエントリの<b>各セル</b>へ {@code ""} と書かれる。
     */
    @Test
    public void writesEveryCellOfAllEmptyListMapEntryAsQuotedEmptyString() {
        // Given
        ListMapBlock block = new ListMapBlock("", "L",
                Arrays.asList("ID", "NAME"),
                Arrays.asList(Arrays.asList("1", "山田"), Arrays.asList("", "")));

        // When
        new XlsFormatWriter().write(container(block), in().toString());

        // Then
        assertThat(cells(in(), 3), is(Arrays.asList(QUOTED_EMPTY, QUOTED_EMPTY)));
    }

    /**
     * Given: 一部の要素だけが空文字のテーブルエントリ。
     * When : Excel 形式へ書き出す。
     * Then : 空文字は空セルのまま書かれる（{@code ""} を付けるのは<b>全要素が空文字のとき</b>だけ）。
     */
    @Test
    public void writesPartiallyEmptyTableEntryWithBlankCells() {
        // Given
        TableDataBlock block = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                Arrays.asList("ID", "NAME"),
                Collections.singletonList(Arrays.asList("", "山田")));

        // When
        new XlsFormatWriter().write(container(block), in().toString());

        // Then
        assertThat(cells(in(), 2), is(Arrays.asList("", "山田")));
    }

    /**
     * Given: 全フィールドが空文字のデータ行を持つ可変長ファイルの中間モデル。
     * When : Excel 形式へ書き出す。
     * Then : そのデータ行の<b>先頭フィールド</b>へ {@code ""} と書かれる
     *        （記載例はいずれか1つのフィールドに {@code ""} と記述する）。
     */
    @Test
    public void writesFirstFieldOfAllEmptyFileDataRowAsQuotedEmptyString() {
        // Given
        FileDataBlock block = variableFile(Arrays.asList(
                Arrays.asList("001", "山田太郎", "5000"),
                Arrays.asList("", "", "")));

        // When
        new XlsFormatWriter().write(container(block), in().toString());

        // Then （0:識別行 1:ディレクティブ 2:名前行 3:型行 4,5:データ行。データ行の列 0 はレコード種別欄）
        assertThat("通常のデータ行", cells(in(), 4), is(Arrays.asList("", "001", "山田太郎", "5000")));
        assertThat(cells(in(), 5), is(Arrays.asList("", QUOTED_EMPTY, "", "")));
    }

    // ------------------------------------------------------------------ 往復

    /**
     * Given: 全セルが {@code ""} のエントリを持つ実 {@code .xlsx}。
     * When : 実 {@code .xlsx} → 中間モデル → 実 {@code .xlsx} と往復させる。
     * Then : エントリが保たれ、件数が本体（{@code PoiXlsReader}）と一致する。
     */
    @Test
    public void roundTripsAllEmptyTableEntryThroughXls() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("SETUP_TABLE=T"))
                .row(text("ID"), text("NAME"))
                .row(text("1"), text("山田"))
                .row(text(QUOTED_EMPTY), text(QUOTED_EMPTY))
                .writeTo(in());

        // When
        new XlsFormatWriter().write(container(block(in())), out().toString());

        // Then
        assertThat("本体が読む件数", frameworkTableSize(out(), "T"), is(2));
        assertThat(((TableDataBlock) block(out())).getRows(),
                is(Arrays.asList(Arrays.asList("1", "山田"), Arrays.asList("", ""))));
    }

    /**
     * Given: 全セルが {@code ""} のエントリを持つ実 {@code .xlsx}（{@code LIST_MAP}）。
     * When : 実 {@code .xlsx} → 中間モデル → 実 {@code .xlsx} と往復させる。
     * Then : エントリが保たれ、件数が本体（{@code PoiXlsReader}）と一致する。
     */
    @Test
    public void roundTripsAllEmptyListMapEntryThroughXls() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("LIST_MAP=L"))
                .row(text("ID"), text("NAME"))
                .row(text("1"), text("山田"))
                .row(text(QUOTED_EMPTY), text(QUOTED_EMPTY))
                .writeTo(in());

        // When
        new XlsFormatWriter().write(container(block(in())), out().toString());

        // Then
        assertThat("本体が読む件数", frameworkListMapSize(out(), "L"), is(2));
        assertThat(((ListMapBlock) block(out())).getRows(),
                is(Arrays.asList(Arrays.asList("1", "山田"), Arrays.asList("", ""))));
    }

    /**
     * Given: 記載例（可変長ファイル）そのままの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} → 中間モデル → 実 {@code .xlsx} と往復させる。
     * Then : レコード件数と値が保たれる。
     */
    @Test
    public void roundTripsDocumentedVariableFileExampleThroughXls() {
        // Given
        writeDocumentedExample();
        List<List<String>> expected = Arrays.asList(
                Arrays.asList("001", "山田太郎", "5000"),
                Arrays.asList("", "", ""));
        assertThat("原本", rowsOf(block(in())), is(expected));

        // When
        new XlsFormatWriter().write(container(block(in())), out().toString());

        // Then
        assertThat(rowsOf(block(out())), is(expected));
    }

    /**
     * Given: 記載例（可変長ファイル）そのままの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} → 中間モデル → YAML → 中間モデル → 実 {@code .xlsx} と往復させる。
     * Then : レコード件数と値が保たれる。
     */
    @Test
    public void roundTripsDocumentedVariableFileExampleThroughYaml() {
        // Given
        writeDocumentedExample();
        List<List<String>> expected = Arrays.asList(
                Arrays.asList("001", "山田太郎", "5000"),
                Arrays.asList("", "", ""));

        // When
        new YamlFormatWriter().write(container(block(in())), via().toString());
        TestDataContainer viaYaml = new YamlFormatReader().read(via().toString(), SHEET);
        assertThat("YAML 経由のブロック数", viaYaml.getSections().get(0).getBlocks().size(), is(1));
        // YAML 側はリソース名がブック名になるため、Excel へ戻す前に既定のブック名／シート名へ包み直す
        new XlsFormatWriter().write(
                container(viaYaml.getSections().get(0).getBlocks().get(0)), out().toString());

        // Then
        assertThat(rowsOf(block(out())), is(expected));
    }

    // ------------------------------------------------------------------ フィクスチャ

    /**
     * 記載例を実 {@code .xlsx} として書き出す。
     */
    private void writeDocumentedExample() {
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("SETUP_VARIABLE=input/data.csv"))
                .row(text("field-separator"), text(","))
                .row(text("DATA"), text("USER_ID"), text("USER_NAME"), text("AMOUNT"))
                .row(blank(), text("半角"), text("全角"), text("半角"))
                .row(blank(), text("001"), text("山田太郎"), text("5000"))
                .row(blank(), text(QUOTED_EMPTY), blank(), blank())
                .writeTo(in());
    }

    /**
     * ファイルブロックの唯一のレコードのデータ行を返す。
     *
     * @param block ブロック
     * @return データ行
     */
    private static List<List<String>> rowsOf(TestDataBlock block) {
        List<RecordLayout> records = ((FileDataBlock) block).getRecords();
        assertThat("レコードレイアウト数", records.size(), is(1));
        return records.get(0).getRows();
    }

    /**
     * 記載例と同じ形（フィールド 3 つ・{@code field-separator} 指定）の可変長ファイルブロックを組み立てる。
     *
     * @param rows データ行
     * @return 可変長ファイルブロック
     */
    private static FileDataBlock variableFile(List<List<String>> rows) {
        Map<String, String> directives = new LinkedHashMap<String, String>();
        directives.put("field-separator", ",");
        List<FieldDef> fields = Arrays.asList(
                new FieldDef("USER_ID", "半角", null),
                new FieldDef("USER_NAME", "全角", null),
                new FieldDef("AMOUNT", "半角", null));
        return new FileDataBlock(DataType.SETUP_VARIABLE, "", "input/data.csv", directives,
                Collections.singletonList(new RecordLayout("DATA", fields, rows)));
    }
}
