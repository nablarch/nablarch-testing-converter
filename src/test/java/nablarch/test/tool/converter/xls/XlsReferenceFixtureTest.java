package nablarch.test.tool.converter.xls;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import nablarch.test.tool.converter.model.ColumnRowDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 参照フィクスチャ（Excel が保存した実物 {@code .xlsx}）と、同じシート内容を POI で生成した
 * {@code .xlsx} とで、{@link XlsFormatReader} の読み取り結果が一致することを確認するテスト。
 *
 * <p>
 * 辺①のフィクスチャは原則 POI 生成方式（テスト実行時に組み立て、バイナリを静的同梱しない）だが、
 * POI 生成物と Excel 保存物の読み取り結果が同一である保証はない。そこで実物 {@code .xlsx} を 1 本だけ
 * 参照フィクスチャとして同梱し、同一性をこのテストで裏付ける。
 * </p>
 *
 * <p>
 * 参照フィクスチャ {@code reference/ClientActionTest.xlsx} は {@code nablarch-example-web}
 * （{@code origin/main} の {@code src/test/java/com/nablarch/example/app/web/action/ClientActionTest.xlsx}）
 * を無改変で取り込んだもの。{@code docProps/app.xml} に {@code <Application>Microsoft Excel</Application>}・
 * {@code AppVersion 16.0300} を持つ真正な Excel 保存物である。
 * </p>
 *
 * <p>
 * 比較は「同じ表示内容を持つ POI 生成ブック」を {@link XlsFixture#copyOf} で作り、両方を
 * {@link XlsFormatReader} に通して中間モデルを突き合わせる形で行う。両ブックはブック名・シート名も
 * 同一にするため、コンテナ名・セクション名まで含めて完全一致を要求できる。
 * </p>
 *
 * @author kiyobot
 */
public class XlsReferenceFixtureTest {

    /** 参照フィクスチャのブック名（＝シート名を除いたリソース名）。 */
    private static final String BOOK = "ClientActionTest";

    /** 参照フィクスチャのシート名。 */
    private static final String SHEET = "setUpDb";

    /** POI 生成ブックの出力先。 */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * 参照フィクスチャが置かれたディレクトリを返す。
     *
     * @return ディレクトリ
     */
    private static Path referenceDir() {
        URL url = XlsReferenceFixtureTest.class.getResource("reference/" + BOOK + ".xlsx");
        assertThat("参照フィクスチャがクラスパス上に存在すること", url, is(notNullValue()));
        try {
            return Paths.get(url.toURI()).getParent();
        } catch (Exception e) {
            throw new IllegalStateException("failed to resolve reference fixture: " + url, e);
        }
    }

    /**
     * Given: Excel が保存した実物 {@code .xlsx}（参照フィクスチャ）。
     * When : 実 {@link XlsFormatReader} で読む。
     * Then : シート上の 3 ブロック（{@code PROJECT}／{@code INDUSTRY}／{@code CLIENT}）が中間モデルへ入る。
     *        Fake リーダを経由せず実ファイルから読めていることの確認を兼ねる。
     */
    @Test
    public void readsRealExcelSavedWorkbook() {
        // When
        TestDataContainer container = new XlsFormatReader().read(referenceDir().toString(), BOOK + "/" + SHEET);

        // Then
        assertThat(container.getName(), is(BOOK));
        assertThat(container.getSections().size(), is(1));
        TestDataSection section = container.getSections().get(0);
        assertThat(section.getName(), is(SHEET));

        List<TestDataBlock> blocks = section.getBlocks();
        assertThat(blocks.size(), is(3));

        ColumnRowDataBlock project = columnRow(blocks.get(0));
        assertThat(project.getIdentifier(), is("PROJECT"));
        assertThat(project.getColumnNames().size(), is(16));
        // カラム名行の直後が空行のため、データ行を持たないブロックとして読まれる。
        assertThat(project.getRows().size(), is(0));

        ColumnRowDataBlock industry = columnRow(blocks.get(1));
        assertThat(industry.getIdentifier(), is("INDUSTRY"));
        assertThat(industry.getColumnNames(), is(Arrays.asList("INDUSTRY_CODE", "INDUSTRY_NAME")));
        assertThat(industry.getRows().size(), is(93));
        // 先頭ゼロを保った文字列セルがそのまま入る。
        assertThat(industry.getRows().get(0), is(Arrays.asList("01", "農業")));

        ColumnRowDataBlock client = columnRow(blocks.get(2));
        assertThat(client.getIdentifier(), is("CLIENT"));
        assertThat(client.getColumnNames(),
                is(Arrays.asList("CLIENT_ID", "CLIENT_NAME", "INDUSTRY_CODE")));
        assertThat(client.getRows().size(), is(120));
        assertThat(client.getRows().get(0), is(Arrays.asList("1", "１株式会社", "01")));
    }

    /**
     * Given: 参照フィクスチャと、同じシート内容を POI で生成した {@code .xlsx}。
     * When : 双方を実 {@link XlsFormatReader} で読む。
     * Then : 中間モデルが完全一致する（POI 生成フィクスチャは Excel 保存物の代用として妥当）。
     */
    @Test
    public void poiGeneratedWorkbookReadsIdenticallyToExcelSavedWorkbook() {
        // Given
        Path referenceDir = referenceDir();
        Path poiDir = folder.getRoot().toPath();
        Workbook reference = XlsFixture.open(referenceDir.resolve(BOOK + ".xlsx"));
        Sheet sheet = reference.getSheet(SHEET);
        XlsFixture.copyOf(BOOK, sheet).writeTo(poiDir);

        // When
        TestDataContainer fromExcel = new XlsFormatReader().read(referenceDir.toString(), BOOK + "/" + SHEET);
        TestDataContainer fromPoi = new XlsFormatReader().read(poiDir.toString(), BOOK + "/" + SHEET);

        // Then
        assertSameContainer(fromExcel, fromPoi);
    }

    // ------------------------------------------------------------------ helpers

    /**
     * ブロックを {@link ColumnRowDataBlock} として取り出す。
     *
     * @param block ブロック
     * @return カラム・行ブロック
     */
    private static ColumnRowDataBlock columnRow(TestDataBlock block) {
        assertThat(block, instanceOf(ColumnRowDataBlock.class));
        return (ColumnRowDataBlock) block;
    }

    /**
     * 2 つの中間モデルが同一内容であることを検証する（中間モデルは {@code equals} を持たないため逐次比較）。
     *
     * @param expected 期待側
     * @param actual   実際側
     */
    private static void assertSameContainer(TestDataContainer expected, TestDataContainer actual) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getSections().size(), is(expected.getSections().size()));
        for (int s = 0; s < expected.getSections().size(); s++) {
            TestDataSection expectedSection = expected.getSections().get(s);
            TestDataSection actualSection = actual.getSections().get(s);
            assertThat(actualSection.getName(), is(expectedSection.getName()));
            assertThat(actualSection.getBlocks().size(), is(expectedSection.getBlocks().size()));
            for (int b = 0; b < expectedSection.getBlocks().size(); b++) {
                ColumnRowDataBlock expectedBlock = columnRow(expectedSection.getBlocks().get(b));
                ColumnRowDataBlock actualBlock = columnRow(actualSection.getBlocks().get(b));
                String at = "section[" + s + "].block[" + b + "]";
                assertThat(at + ".dataType", actualBlock.getDataType(), is(expectedBlock.getDataType()));
                assertThat(at + ".groupId", actualBlock.getGroupId(), is(expectedBlock.getGroupId()));
                assertThat(at + ".identifier", actualBlock.getIdentifier(), is(expectedBlock.getIdentifier()));
                assertThat(at + ".columnNames", actualBlock.getColumnNames(), is(expectedBlock.getColumnNames()));
                assertThat(at + ".rows", actualBlock.getRows(), is(expectedBlock.getRows()));
            }
        }
    }
}
