package nablarch.test.tool.converter;

import java.io.File;
import java.io.UncheckedIOException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Maven plugin {@code convert} ゴール。テストデータを指定形式（XLS／YAML）間で変換する。
 *
 * <p>
 * {@link TestDataConverter#convert(ConversionRequest)} へ薄くラップするだけの委譲ゴール。
 * 変換ロジックは本クラスに持たず、{@link ConversionRequest.Builder} で組み立てて委譲する。
 * </p>
 *
 * @author kiyobot
 */
@Mojo(name = "convert", requiresProject = false)
public class ConverterMojo extends AbstractMojo {

    /** 変換元形式識別子（{@code "xls"} または {@code "yaml"}） */
    @Parameter(property = "nablarch-testing-converter.from", required = true)
    private String from;

    /** 変換先形式識別子（{@code "xls"} または {@code "yaml"}） */
    @Parameter(property = "nablarch-testing-converter.to", required = true)
    private String to;

    /** 入力ディレクトリ */
    @Parameter(property = "nablarch-testing-converter.input", required = true)
    private File input;

    /** 出力ディレクトリ */
    @Parameter(property = "nablarch-testing-converter.output", required = true)
    private File output;

    /** 出力先が既存の場合に上書きするか（省略時は {@code false}） */
    @Parameter(property = "nablarch-testing-converter.overwrite", defaultValue = "false")
    private boolean overwrite;

    /** 変換対象を絞る glob パターンのリスト（省略時は全件） */
    @Parameter
    private List<String> includes;

    /** 変換対象から除外する glob パターンのリスト（省略時は除外なし） */
    @Parameter
    private List<String> excludes;

    /** 変換対象から除外するシート名のリスト（省略時は除外なし） */
    @Parameter
    private List<String> excludeSheets;

    /**
     * 変換を実行する。
     *
     * @throws MojoExecutionException 形式識別子が未知の場合、またはConverter例外が発生した場合
     */
    @Override
    public void execute() throws MojoExecutionException {
        DataFormat sourceFormat;
        DataFormat targetFormat;
        try {
            sourceFormat = DataFormat.fromArgument(from);
            targetFormat = DataFormat.fromArgument(to);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        ConversionRequest.Builder builder = new ConversionRequest.Builder()
                .sourceFormat(sourceFormat)
                .targetFormat(targetFormat)
                .inputPath(input.toPath())
                .outputPath(output.toPath())
                .overwrite(overwrite);

        if (includes != null) {
            for (String pattern : includes) {
                builder.include(pattern);
            }
        }
        if (excludes != null) {
            for (String pattern : excludes) {
                builder.exclude(pattern);
            }
        }
        if (excludeSheets != null) {
            for (String sheet : excludeSheets) {
                builder.excludeSheet(sheet);
            }
        }

        try {
            int count = TestDataConverter.convert(builder.build());
            getLog().info("Converted " + count + " file(s).");
        } catch (ConverterException | UncheckedIOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
