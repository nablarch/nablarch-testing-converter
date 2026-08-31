package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import nablarch.test.core.db.TableData;
import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.FrameworkOracle;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * <b>カラム名の行がマーカーカラムだけで構成されたブロック</b>を、マーカーカラムの名前と各行の値を保ったまま
 * 中間モデルへ写すことを、実 {@code .xlsx} を入力に固定する。
 *
 * <p>
 * このブロックの各エントリはフィールドを持たない。それでもエントリの数と並びは意味を持つ——
 * テストショット一覧と行の順序で対応付ける用途があるためである。フレームワーク本体は、行を読み飛ばすかどうかを
 * マーカーカラムを除外する<b>前</b>の行で判定するため、マーカーカラムだけのブロックでも行を落とさない。
 * </p>
 *
 * <p>
 * <b>マーカーカラムの値も本体が解釈したあとの値である。</b>本体は行の解釈を、マーカーカラムを除外するより前に
 * 行の全セルへ掛ける。よって {@code ""}（空文字記法）は空文字に、{@code null} は Java {@code null} になる。
 * 中間モデルが解釈後の値を持つのは他のカラムと同じであり、{@link XlsFormatWriter} の記法への戻しも
 * そのまま働く。
 * </p>
 *
 * <p>
 * <b>期待値の件数はテストが書かず、フレームワークが実行時に使う読み手（{@code BasicTestDataParser}）から
 * 取り出す。</b>変換ツール自身の読みを正解にしない。値と並びはテストが明示し、本体側が黙って変わったときに
 * 気づけるようにする。
 * </p>
 *
 * <p>
 * 実データカラムを 1 つでも持つブロックのマーカーカラムが従来どおり落ちること（非回帰）は
 * {@code XlsMarkerOnlyEntryTest} が固定している。
 * </p>
 */
public class XlsMarkerOnlyBlockTest {

    /** フィクスチャの既定ブック名。 */
    private static final String BOOK = "markerOnlyBlockBook";

    /** フィクスチャの既定シート名。 */
    private static final String SHEET = "sheet1";

    /** テストごとに独立した出力先。 */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // ------------------------------------------------------------------ helpers

    /**
     * 既定のブック名・シート名でフィクスチャの組み立てを開始する。
     *
     * @return フィクスチャビルダ
     */
    private static XlsFixture book() {
        return XlsFixture.book(BOOK).sheet(SHEET);
    }

    /**
     * フィクスチャ {@code .xlsx} の出力先ディレクトリ。
     *
     * @return ディレクトリ
     */
    private Path dir() {
        return folder.getRoot().toPath();
    }

    /**
     * 既定のブック／シートのリソース名。
     *
     * @return リソース名
     */
    private static String resource() {
        return BOOK + "/" + SHEET;
    }

    /**
     * 既定のブック／シートを変換ツールで読み、唯一のブロックを返す。
     *
     * @param type 期待するブロックの実装クラス
     * @param <T>  ブロックの型
     * @return ブロック
     */
    private <T extends TestDataBlock> T onlyBlock(Class<T> type) {
        TestDataContainer container = new XlsFormatReader().read(dir().toString(), resource());
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        assertThat("ブロック数", blocks.size(), is(1));
        return type.cast(blocks.get(0));
    }

    // ------------------------------------------------------------------ テーブル

    /**
     * Given: カラム名の行がマーカーカラム {@code [no]}・{@code [memo]} だけの {@code SETUP_TABLE} の
     *        実 {@code .xlsx}。値は定義順・辞書順とずらして並べる。
     * When : 実 {@code .xlsx} を変換ツールと本体それぞれに読ませる。
     * Then : マーカーカラムの名前が記述順で残り、各行の値も記述順で残る。行数は本体の読みと一致する。
     */
    @Test
    public void keepsMarkerOnlyColumnsAndValuesInTable() {
        // Given
        book().row(text("SETUP_TABLE=T"))
                .row(text("[no]"), text("[memo]"))
                .row(text("3"), text("c"))
                .row(text("1"), text("a"))
                .row(text("4"), text("d"))
                .row(text("2"), text("b"))
                .writeTo(dir());

        // When
        TableDataBlock table = onlyBlock(TableDataBlock.class);
        List<TableData> oracle = FrameworkOracle.setupTablesViaTestDataParser(dir().toString(), resource());

        // Then
        assertThat("本体が読むテーブル数", oracle.size(), is(1));
        assertThat("本体はマーカーカラムをカラム名に持たない",
                   Arrays.asList(oracle.get(0).getColumnNames()), is(Collections.<String>emptyList()));
        assertThat("本体が読むエントリ数", oracle.get(0).size(), is(4));
        assertThat("マーカーカラムの名前が記述順で残る",
                   table.getColumnNames(), is(Arrays.asList("[no]", "[memo]")));
        assertThat("エントリ数が本体と一致する", table.getRows().size(), is(oracle.get(0).size()));
        assertThat("マーカーカラムの値が記述順で残る", table.getRows(), is(Arrays.asList(
                Arrays.asList("3", "c"),
                Arrays.asList("1", "a"),
                Arrays.asList("4", "d"),
                Arrays.asList("2", "b"))));
    }

    // ------------------------------------------------------------------ LIST_MAP

    /**
     * Given: カラム名の行がマーカーカラム {@code [no]} だけの {@code LIST_MAP} の実 {@code .xlsx}。
     *        値は定義順・辞書順とずらして並べる。
     * When : 実 {@code .xlsx} を変換ツールと本体それぞれに読ませる。
     * Then : マーカーカラムの名前と各行の値が記述順で残り、行数は本体の読みと一致する
     *        （{@code LIST_MAP} は経路が別なので個別に固定する）。
     */
    @Test
    public void keepsMarkerOnlyColumnsAndValuesInListMap() {
        // Given
        book().row(text("LIST_MAP=requestParams"))
                .row(text("[no]"))
                .row(text("3"))
                .row(text("1"))
                .row(text("4"))
                .row(text("2"))
                .writeTo(dir());

        // When
        ListMapBlock listMap = onlyBlock(ListMapBlock.class);
        List<Map<String, String>> oracle =
                FrameworkOracle.listMapViaTestDataParser(dir().toString(), resource(), "requestParams");

        // Then
        assertThat("本体が読むエントリ数", oracle.size(), is(4));
        assertThat("本体はマーカーカラムを値に持たない", oracle.get(0).isEmpty(), is(true));
        assertThat("マーカーカラムの名前が残る", listMap.getColumnNames(), is(Arrays.asList("[no]")));
        assertThat("エントリ数が本体と一致する", listMap.getRows().size(), is(oracle.size()));
        assertThat("マーカーカラムの値が記述順で残る", listMap.getRows(), is(Arrays.asList(
                Arrays.asList("3"),
                Arrays.asList("1"),
                Arrays.asList("4"),
                Arrays.asList("2"))));
    }

    // ------------------------------------------------------------------ 値の解釈

    /**
     * Given: マーカーカラム {@code [no]} だけの {@code SETUP_TABLE} で、値に空文字記法 {@code ""} と
     *        {@code null} 記法を書いた実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を変換ツールに読ませる。
     * Then : マーカーカラムの値も本体のインタープリタを通ったあとの値になる
     *        （{@code ""} は空文字、{@code null} は Java {@code null}）。
     *
     * <p>
     * 本体が行の解釈をマーカーカラムの除外より前に行うためである。中間モデルが解釈後の値を持つことで、
     * {@link XlsFormatWriter} の記法への戻し（{@code toCellNotation}）がマーカーカラムにもそのまま働く。
     * </p>
     */
    @Test
    public void interpretsMarkerOnlyCellValuesAsFrameworkDoes() {
        // Given
        book().row(text("SETUP_TABLE=T"))
                .row(text("[no]"))
                .row(text("3"))
                .row(text("\"\""))
                .row(text("null"))
                .row(text("2"))
                .writeTo(dir());

        // When
        TableDataBlock table = onlyBlock(TableDataBlock.class);
        List<TableData> oracle = FrameworkOracle.setupTablesViaTestDataParser(dir().toString(), resource());

        // Then
        assertThat("エントリ数が本体と一致する", table.getRows().size(), is(oracle.get(0).size()));
        assertThat("空文字記法・null 記法が解釈される", table.getRows(), is(Arrays.asList(
                Arrays.asList("3"),
                Arrays.asList(""),
                Arrays.<String>asList((String) null),
                Arrays.asList("2"))));
    }

    // ------------------------------------------------------------------ データタイプ

    /**
     * Given: マーカーカラム {@code [no]} だけの {@code EXPECTED_TABLE}（グループ ID 付き）の実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を変換ツールに読ませる。
     * Then : グループ付きのテーブル系ブロックでもマーカーカラムの名前と値が残る。
     *
     * <p>
     * テーブル系はグループ単位で収集するため、識別子（テーブル名）とグループ ID の両方で
     * 対象ブロックを引き当てる必要がある。本体 {@code TableData} はテーブル名を trim して大文字化するため、
     * 小文字のテーブル名で書いても引き当てられることをあわせて固定する。
     * </p>
     */
    @Test
    public void keepsMarkerOnlyColumnsInGroupedTableWithLowerCaseName() {
        // Given
        book().row(text("EXPECTED_TABLE[g1]=t_user"))
                .row(text("[no]"))
                .row(text("2"))
                .row(text("1"))
                .writeTo(dir());

        // When
        TableDataBlock table = onlyBlock(TableDataBlock.class);

        // Then
        assertThat(table.getDataType(), is(DataType.EXPECTED_TABLE_DATA));
        assertThat(table.getGroupId(), is("g1"));
        assertThat(table.getIdentifier(), is("T_USER"));
        assertThat("マーカーカラムの名前が残る", table.getColumnNames(), is(Arrays.asList("[no]")));
        assertThat("マーカーカラムの値が記述順で残る", table.getRows(), is(Arrays.asList(
                Arrays.asList("2"),
                Arrays.asList("1"))));
    }
}
