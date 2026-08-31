package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.blank;
import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
 * マーカーカラムだけに値があるエントリが、<b>フレームワーク本体が読むのと同じ件数だけ</b>
 * 中間モデルに残ることを、実 {@code .xlsx} を入力に固定する。
 *
 * <p>
 * マーカーカラムはエントリを読み飛ばす判断に使われない。読み飛ばしの判定はマーカーカラムを除外する
 * <b>前</b>の行に対して行われるため、マーカーカラムだけに値があるエントリは読み飛ばされず、
 * 他のカラムの値は通常どおり読み込まれる。消えるのはマーカーカラムの値だけである。
 * </p>
 *
 * <p>
 * 入力は<b>他のカラムを持つブロック</b>で組む。カラム名の行がマーカーカラムだけのブロックには別の定めが
 * 当たり、マーカーカラムの名前と値を保ったまま変換する。そちらは {@code XlsMarkerOnlyBlockTest} が
 * 固定している。<b>本クラスが固定するのは、実データカラムを持つブロックではマーカーカラムが従来どおり
 * 落ちること</b>（非回帰）である。
 * </p>
 *
 * <p>
 * <b>期待値はテストが書かず、{@link FrameworkOracle} が本体に同じ {@code .xlsx} を読ませて取り出す。</b>
 * 件数そのものも各テストで明示し、本体側が黙って変わったときに気づけるようにする。
 * </p>
 */
public class XlsMarkerOnlyEntryTest {

    /** フィクスチャの既定ブック名。 */
    private static final String BOOK = "markerOnlyBook";

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

    /**
     * マーカーカラム {@code [no]} と 2 つのデータカラムを持つブロックの行を組み立てる。
     *
     * <p>
     * 3 行目はマーカーカラムだけに値があるエントリである。2 行目は他のカラムだけに値があり、
     * その値が空文字記法であるエントリで、マーカーカラムの有無で扱いが変わらないことを示す。
     * </p>
     *
     * @param marker ブロックのマーカー行
     * @return 組み立てたフィクスチャ
     */
    private XlsFixture blockWithMarkerColumn(String marker) {
        return book().row(text(marker))
                .row(text("[no]"), text("id"), text("name"))
                .row(text("1"), text("U0001"), text("yamada"))
                .row(blank(), text("\"\""), text("\"\""))
                .row(text("3"), blank(), blank());
    }

    // ------------------------------------------------------------------ テーブル

    /**
     * Given: マーカーカラム {@code [no]} と 2 つのデータカラムを持つ {@code SETUP_TABLE} の実 {@code .xlsx}。
     *        3 行目はマーカーカラムだけに値がある。
     * When : 実 {@code .xlsx} を変換ツールと本体それぞれに読ませる。
     * Then : 変換ツールの行数・値が本体と一致する。マーカーカラムだけに値がある行も残り、
     *        カラム名からはマーカーカラムが落ちる。
     */
    @Test
    public void keepsMarkerOnlyEntryInTableAsFrameworkDoes() {
        // Given
        blockWithMarkerColumn("SETUP_TABLE=T").writeTo(dir());

        // When
        TableDataBlock table = onlyBlock(TableDataBlock.class);
        List<List<String>> oracle = FrameworkOracle.tableRows(
                dir().toString(), resource(), "", DataType.SETUP_TABLE_DATA, "T");

        // Then
        // テーブル系はフレームワークがカラム名を大文字へ揃える（本テストの主題ではない）。
        assertThat("マーカーカラムはカラム名から落ちる", table.getColumnNames(), is(Arrays.asList("ID", "NAME")));
        assertThat("本体が読む行数", oracle.size(), is(3));
        assertThat("本体が読む値", oracle, is(Arrays.asList(
                Arrays.asList("U0001", "yamada"),
                Arrays.asList("", ""),
                Arrays.asList("", ""))));
        assertThat("変換ツールの行が本体と一致する", table.getRows(), is(oracle));
    }

    // ------------------------------------------------------------------ LIST_MAP

    /**
     * Given: マーカーカラム {@code [no]} と 2 つのデータカラムを持つ {@code LIST_MAP} の実 {@code .xlsx}。
     *        3 行目はマーカーカラムだけに値がある。
     * When : 実 {@code .xlsx} を変換ツールと本体それぞれに読ませる。
     * Then : 変換ツールの行数・値が本体と一致する（{@code LIST_MAP} は経路が別なので個別に固定する）。
     */
    @Test
    public void keepsMarkerOnlyEntryInListMapAsFrameworkDoes() {
        // Given
        blockWithMarkerColumn("LIST_MAP=lm").writeTo(dir());

        // When
        ListMapBlock listMap = onlyBlock(ListMapBlock.class);
        List<Map<String, String>> oracleRows = FrameworkOracle.listMap(dir().toString(), resource(), "lm");

        // Then
        assertThat("マーカーカラムはカラム名から落ちる", listMap.getColumnNames(), is(Arrays.asList("id", "name")));
        assertThat("本体が読む行数", oracleRows.size(), is(3));
        List<List<String>> oracle = new ArrayList<List<String>>();
        for (Map<String, String> row : oracleRows) {
            oracle.add(Arrays.asList(row.get("id"), row.get("name")));
        }
        assertThat("本体が読む値", oracle, is(Arrays.asList(
                Arrays.asList("U0001", "yamada"),
                Arrays.asList("", ""),
                Arrays.asList("", ""))));
        assertThat("変換ツールの行が本体と一致する", listMap.getRows(), is(oracle));
    }
}
