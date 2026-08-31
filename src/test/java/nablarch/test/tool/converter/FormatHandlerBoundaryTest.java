package nablarch.test.tool.converter;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import nablarch.test.core.reader.DataType;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;
import nablarch.test.tool.converter.xls.ExcelFormatConfig;
import nablarch.test.tool.converter.xls.XlsFormatWriter;
import nablarch.test.tool.converter.yaml.YamlFormatWriter;

/**
 * 形式ハンドラ（{@link XlsFormatHandler}／{@link YamlFormatHandler}）と、その入出力に使う
 * 書き出し器の<b>パス境界</b>を担保するテストクラス。
 *
 * <p>
 * 各形式の変換ルールは辺ごとのテストクラスが担保する。本クラスが受け持つのは、ルールの外側にある
 * 2 つの境界だけである。
 * </p>
 * <ul>
 *   <li><b>親ディレクトリを持たない相対パス</b>——{@code basePath} が空文字のとき、
 *       {@code Paths.get("", "x.xlsx")} は親を持たない {@code "x.xlsx"} になる。書き出し器の
 *       {@code Files.createDirectories(parent)} と、{@link XlsFormatHandler#read} の
 *       「親が無ければカレント」がここで効く</li>
 *   <li><b>入力ディレクトリを開けないとき</b>——{@link YamlFormatHandler#read} が
 *       {@code DirectoryStream} を開けない場合に、チェック例外を握り潰さず
 *       {@link UncheckedIOException} で上げること</li>
 * </ul>
 *
 * @author kiyobot
 */
public class FormatHandlerBoundaryTest {

    /** 一時ディレクトリ */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Given: {@code basePath} が空文字（親ディレクトリを持たない相対パスになる）。
     * When : Excel を書き出し、同じ相対パスを {@link XlsFormatHandler#read} で読み戻す。
     * Then : プロセスのカレントディレクトリ直下に書かれ、読み戻した中間モデルが元と一致する。
     *
     * <p>担保：{@code XlsFormatWriter#write} の {@code parent == null} 側と、
     * {@code XlsFormatHandler#parentOf} の「親が無ければカレント」側。</p>
     */
    @Test
    public void xlsRoundTripsThroughPathWithoutParentDirectory() throws IOException {
        // Given
        String book = "nn-cov-xls-no-parent";
        Path written = Paths.get(book + ".xlsx");
        assertThat("前提：親を持たない相対パスであること", written.getParent(), is((Path) null));

        try {
            // When
            new XlsFormatWriter(ExcelFormatConfig.defaults()).write(container(book, "s1"), "");

            // Then
            assertThat("カレント直下へ書かれる", Files.isRegularFile(written), is(true));

            TestDataContainer read = new XlsFormatHandler().read(written, emptyList());
            assertThat(read.getName(), is(book));
            assertThat(read.getSections().size(), is(1));
            assertThat(read.getSections().get(0).getName(), is("s1"));
            TableDataBlock block = (TableDataBlock) read.getSections().get(0).getBlocks().get(0);
            assertThat(block.getColumnNames(), is(Arrays.asList("ZCOL", "ACOL")));
            assertThat(block.getRows(), is(Collections.singletonList(Arrays.asList("z1", "a1"))));
        } finally {
            Files.deleteIfExists(written);
        }
    }

    /**
     * Given: {@code basePath} が空文字（親ディレクトリを持たない相対パスになる）。
     * When : YAML を書き出す。
     * Then : プロセスのカレントディレクトリ直下に書かれる。
     *
     * <p>担保：{@code YamlFormatWriter#write} の {@code parent == null} 側。</p>
     */
    @Test
    public void yamlWritesThroughPathWithoutParentDirectory() throws IOException {
        // Given
        Path written = Paths.get("nn-cov-yaml-no-parent.yaml");
        assertThat("前提：親を持たない相対パスであること", written.getParent(), is((Path) null));

        try {
            // When
            new YamlFormatWriter().write(container("book", "nn-cov-yaml-no-parent"), "");

            // Then
            assertThat("カレント直下へ書かれる", Files.isRegularFile(written), is(true));
            String text = new String(Files.readAllBytes(written), "UTF-8");
            assertThat("中身が空でない", text.contains("ZCOL"), is(true));
        } finally {
            Files.deleteIfExists(written);
        }
    }

    /**
     * Given: 存在しない YAML コンテナディレクトリ。
     * When : {@link YamlFormatHandler#read} を呼ぶ。
     * Then : {@link UncheckedIOException} になり、原因がそのディレクトリの {@link IOException} である。
     *
     * <p>担保：{@code YamlFormatHandler#listYamlFiles} の {@code catch (IOException)} 側。</p>
     */
    @Test
    public void yamlReadWrapsDirectoryOpenFailure() {
        // Given
        Path missing = folder.getRoot().toPath().resolve("no-such-dir");

        // When / Then
        try {
            new YamlFormatHandler().read(missing, emptyList());
            fail("should throw UncheckedIOException");
        } catch (UncheckedIOException e) {
            assertThat(e.getMessage(), is("failed to list YAML files: " + missing));
            assertThat(e.getCause() instanceof IOException, is(true));
        }
    }

    /**
     * 1 テーブルブロックだけを持つコンテナを組む。
     *
     * <p>カラムは辞書順とずらして並べる（順序保持を主張するため）。</p>
     *
     * @param book  コンテナ名
     * @param sheet セクション名
     * @return コンテナ
     */
    private static TestDataContainer container(String book, String sheet) {
        List<TestDataBlock> blocks = Collections.<TestDataBlock>singletonList(
                new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "USERS",
                        Arrays.asList("ZCOL", "ACOL"),
                        Collections.singletonList(Arrays.asList("z1", "a1"))));
        return new TestDataContainer(book, Collections.singletonList(new TestDataSection(sheet, blocks)));
    }

}
