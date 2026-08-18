package nablarch.test.tool.converter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * サンプルプロジェクトの YAML テストデータを Excel に変換するサンプル変換テスト。
 *
 * <p>
 * nablarch-system-development-guide のサンプルプロジェクトから抜粋した YAML ファイルを
 * テストリソースに配置し、実プロジェクトのテストデータに対する YAML→XLS 変換の挙動を確認する。
 * 現状は 0 件テーブル（{@code rows: []}）を含むため変換が中止される（{@code issues.md} <b>XLS-27</b>）。
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

    /**
     * climan サンプル: 0 件テーブル（{@code rows: []}）を含むため、現状は変換が中止される。
     *
     * <p>
     * サンプルの {@code ClientActionTest/testShowWithEmptyClientTable.yaml} ／
     * {@code testFindNoClients.yaml} ／ {@code ExportProjectsInPeriodActionRequestTest/testNormalEnd.yaml}
     * は {@code rows: []} のテーブルを持つ。YAML 記法では 0 件テーブルにカラム名を書く場所が無いため
     * （{@code testdata_notation.rst:819}／{@code :836}）、中間モデルはカラム名 0 件になる。
     * これを Excel へ書き出すと、次のブロックの識別子行がカラム名の行として読み込まれ、
     * そのブロックが丸ごと失われる（{@code issues.md} <b>XLS-27</b>）。
     * 無言で壊れた Excel を書かないため、辺③に番人を置いて変換を中止している。
     * </p>
     *
     * <p>
     * <b>これは当面の姿である。</b>本体（{@code nablarch-testing}）の {@code TableDataParser} が
     * 「識別子行の次の行が識別子行なら、カラム名 0 件の 0 件テーブルとみなす」と読めるようになったら、
     * 辺③は「識別子行だけを書く」実装へ切り替わり、本テストは<b>変換が成功して 2 冊出力される</b>
     * ことを確認するテストへ戻す（XLS-27 の 2 段構え）。
     * </p>
     */
    @Test
    public void stopsClimanSampleConversionBecauseOfZeroRowTable() throws IOException {
        Files.createDirectories(OUTPUT_BASE);

        try {
            TestDataConverter.convert(new ConversionRequest.Builder()
                    .sourceFormat(DataFormat.YAML)
                    .targetFormat(DataFormat.XLS)
                    .inputPath(RESOURCE_BASE)
                    .outputPath(OUTPUT_BASE)
                    .overwrite(true)
                    .build());
            fail("0 件テーブルを含むため変換は中止されるはず");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("カラム名を 1 件も持たないブロックは書き出せません"));
            assertThat(e.getMessage(), containsString("SETUP_TABLE=CLIENT"));
        }
    }
}
