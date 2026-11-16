package nablarch.test.tool.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import nablarch.test.core.db.TableData;
import nablarch.test.core.file.DataFile;
import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.FrameworkOracle;
import nablarch.test.tool.converter.yaml.YamlFrameworkOracle;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * サンプルプロジェクトの YAML テストデータを Excel に変換するサンプル変換テスト。
 *
 * <p>
 * nablarch-system-development-guide のサンプルプロジェクトから抜粋した YAML ファイルを
 * テストリソースに配置し、実プロジェクトのテストデータに対する YAML→XLS 変換の挙動を確認する。
 * <b>0 件テーブル（{@code rows: []}）を含むファイルも変換できる</b>（カラム名の行へマーカーカラム
 * {@code [EMPTY]} を書く）。
 * </p>
 *
 * <p>
 * setup_files／expected_files を含む YAML（proman の testNormalEnd.yaml 等）は
 * nablarch-core-dataformat（scope=test）が実行時クラスパスに必要。本テストは
 * test スコープで実行されるため問題なく動作する（Maven プラグインとして実行する場合は
 * 利用 PJ 側が nablarch-core-dataformat への依存を持つ）。
 * </p>
 */
public class SampleConversionTest {

    private static final Path RESOURCE_BASE =
            Paths.get("src/test/java/nablarch/test/tool/converter/SampleConversionTest");

    private static final Path OUTPUT_BASE = Paths.get(".output/SampleConversionTest");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Given: climan／proman サンプルの YAML（0 件テーブルを含む。読み込み単位 8 件・テストクラス 2 件）。
     * When : YAML→XLS 変換。
     * Then : 変換は成功し、テストクラスごとに 1 冊、計 2 冊の {@code .xlsx} が出力される
     *        （{@code convert} の戻り値はブック数）。
     *
     * <p>
     * サンプル中の {@code rows: []} は 3 ファイル・4 箇所ある。うち
     * {@code ClientActionTest/testFindNoClients.yaml:3} と
     * {@code ClientActionTest/testShowWithEmptyClientTable.yaml:3} の 2 箇所が
     * {@code setup_tables} 配下の 0 件テーブルである。YAML 記法ではカラム名が {@code rows:} の
     * 先頭要素のキーで決まるため、
     * 0 件テーブルにはカラム名を書く場所が無く、中間モデルはカラム名 0 件になる。
     * Excel への書き出しは Excel の「データ行を書かない場合でも、カラム名の行は省略できない」制約を
     * 満たすため、カラム名の行へマーカーカラム {@code [EMPTY]} を 1 つ書く。
     * 残る {@code ExportProjectsInPeriodActionRequestTest/testNormalEnd.yaml:173}／記法の
     * {@code rows: []} は綴りは同じでも別物で、{@code expected_files} 配下のファイルデータの
     * 0 件レコードであり、カラム名は {@code fields:} に持つ。
     * </p>
     *
     */
    @Test
    public void convertsClimanSampleIncludingZeroRowTable() throws IOException {
        // Given
        Files.createDirectories(OUTPUT_BASE);

        // When
        int converted = TestDataConverter.convert(new ConversionRequest.Builder()
                .sourceFormat(DataFormat.YAML)
                .targetFormat(DataFormat.XLS)
                .inputPath(RESOURCE_BASE)
                .outputPath(OUTPUT_BASE)
                .overwrite(true)
                .build());

        // Then: テストクラスごとに 1 冊、計 2 冊
        assertThat("出力ブック数", converted, is(2));
        assertTrue("ClientActionTest.xlsx が出力されること",
                Files.exists(OUTPUT_BASE.resolve("ClientActionTest.xlsx")));
        assertTrue("ExportProjectsInPeriodActionRequestTest.xlsx が出力されること",
                Files.exists(OUTPUT_BASE.resolve("ExportProjectsInPeriodActionRequestTest.xlsx")));
    }

    /**
     * Given: climan／proman サンプルの YAML。
     * When : YAML→XLS 変換。
     * Then : 出力した Excel を本体の Excel リーダ（{@link FrameworkOracle}）で読んだ結果が、
     *        元の YAML を本体の YAML リーダ（{@link YamlFrameworkOracle}）で読んだ結果と一致する。
     *
     * <p>
     * 上の {@link #convertsClimanSampleIncludingZeroRowTable} は出力されたことしか確かめない。
     * 本テストは、実プロジェクトのテストデータを Excel にしたとき、テスティングフレームワークが同じ意味に
     * 読むことを確かめる（YAML→XLS 方向の品質担保）。変換ツールのリーダで読み戻して比べると、
     * 書き出しと読み戻しが同じ写し間違いをしたときに見逃すため、両側とも本体に読ませる。
     * </p>
     *
     * <p>
     * 比べるのはサンプルにあるすべてのブロックである。ファイルデータは値に加えて、パス・ディレクティブ・
     * レコード種別・型・長さも比べる（{@link FrameworkReadDump#fileLayouts}）。
     * Excel のグループ ID は本体の API が要求する整形済みの表記（{@code [1]}）で、YAML は生の値（{@code 1}）で渡す。
     * </p>
     */
    @Test
    public void convertedSampleIsReadByFrameworkAsOriginalYaml() throws IOException {
        // Given
        Path output = folder.getRoot().toPath();

        // When
        TestDataConverter.convert(new ConversionRequest.Builder()
                .sourceFormat(DataFormat.YAML)
                .targetFormat(DataFormat.XLS)
                .inputPath(RESOURCE_BASE)
                .outputPath(output)
                .build());

        // Then: climan（setup_tables だけ。0 件テーブルを含む）
        for (String sheet : new String[] {"setUpDb", "testFindCodeAndNameUpperLimit", "testFindIndustryCodeNoClients",
                "testFindNoClients", "testFindOverUpperLimit", "testShowWithEmptyClientTable"}) {
            assertSetupTables(output, "ClientActionTest", sheet, null);
        }

        // Then: proman
        String proman = "ExportProjectsInPeriodActionRequestTest";
        assertSetupTables(output, proman, "setUpDb", null);
        assertThat("LIST_MAP testShots",
                FrameworkReadDump.listMap(
                        FrameworkOracle.listMap(output.toString(), proman + "/testNormalEnd", "testShots")),
                is(FrameworkReadDump.listMap(
                        YamlFrameworkOracle.listMap(yamlDir(proman), "testNormalEnd", "testShots"))));
        for (int group = 1; group <= 6; group++) {
            String groupId = String.valueOf(group);
            assertSetupTables(output, proman, "testNormalEnd", groupId);
            List<? extends DataFile> fromXls = FrameworkOracle.files(output.toString(), proman + "/testNormalEnd",
                    "[" + groupId + "]", DataType.EXPECTED_VARIABLE);
            List<DataFile> fromYaml = YamlFrameworkOracle.expectedFiles(yamlDir(proman), "testNormalEnd", groupId);
            assertThat("expected_files group " + groupId + " が空でないこと", fromYaml.isEmpty(), is(false));
            assertThat("expected_files group " + groupId + " の値",
                    FrameworkReadDump.files(fromXls), is(FrameworkReadDump.files(fromYaml)));
            assertThat("expected_files group " + groupId + " のパス・ディレクティブ・レイアウト",
                    FrameworkReadDump.fileLayouts(fromXls), is(FrameworkReadDump.fileLayouts(fromYaml)));
        }
    }

    /**
     * 1 つの読み込み単位の {@code setup_tables} を、Excel と YAML の両方から本体に読ませて比べる。
     *
     * @param output    Excel の出力先
     * @param testClass テストクラス名（ブック名・YAML のディレクトリ名）
     * @param sheet     シート名（YAML のファイル名）
     * @param groupId   グループ ID の生の値（指定なしは {@code null}）
     */
    private static void assertSetupTables(Path output, String testClass, String sheet, String groupId) {
        List<TableData> fromXls = FrameworkOracle.tables(output.toString(), testClass + "/" + sheet,
                groupId == null ? "" : "[" + groupId + "]", DataType.SETUP_TABLE_DATA);
        List<TableData> fromYaml = groupId == null
                ? YamlFrameworkOracle.setupTables(yamlDir(testClass), sheet)
                : YamlFrameworkOracle.setupTables(yamlDir(testClass), sheet, groupId);
        String label = testClass + "/" + sheet + (groupId == null ? "" : " group " + groupId);
        assertThat(label + " の setup_tables が空でないこと", fromYaml.isEmpty(), is(false));
        assertThat(label + " の setup_tables",
                FrameworkReadDump.tables(fromXls), is(FrameworkReadDump.tables(fromYaml)));
    }

    /**
     * 元の YAML のディレクトリ（テストクラス 1 つ分）を返す。
     *
     * @param testClass テストクラス名
     * @return 絶対パス
     */
    private static String yamlDir(String testClass) {
        return RESOURCE_BASE.resolve(testClass).toAbsolutePath().toString();
    }
}
