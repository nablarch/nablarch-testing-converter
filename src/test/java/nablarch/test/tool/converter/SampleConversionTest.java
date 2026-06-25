package nablarch.test.tool.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
 * テストリソースに配置し、YAML→XLS 変換が正常終了して出力ファイルが生成されることを確認する。
 * アサートは「出力ファイルが存在するか」のみ。
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
     * climan サンプル: setup_tables のみの YAML 群を Excel に変換できる。
     */
    @Test
    public void convertsClimanSampleYamlToXls() throws IOException {
        Files.createDirectories(OUTPUT_BASE);

        int count = TestDataConverter.convert(new ConversionRequest.Builder()
                .sourceFormat(DataFormat.YAML)
                .targetFormat(DataFormat.XLS)
                .inputPath(RESOURCE_BASE)
                .outputPath(OUTPUT_BASE)
                .overwrite(true)
                .build());

        assertThat(count, is(2));
        assertThat(Files.exists(OUTPUT_BASE.resolve("ClientActionTest.xlsx")), is(true));
        assertThat(Files.exists(OUTPUT_BASE.resolve("ExportProjectsInPeriodActionRequestTest.xlsx")), is(true));
    }
}
