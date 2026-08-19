package nablarch.test.tool.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
 * <b>0 件テーブル（{@code rows: []}）を含むファイルも変換できる</b>（{@code issues.md} <b>XLS-27</b>。
 * マーカーカラム案の採用は 2026-08-19）。
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
     * 先頭要素のキーで決まるため（{@code testdata_notation.rst:819}。{@code 30a8271} 時点）、
     * 0 件テーブル（同 {@code :836}）にはカラム名を書く場所が無く、中間モデルはカラム名 0 件になる。
     * 辺③は Excel の「データ行を書かない場合でも、カラム名の行は省略できない」制約（{@code :802}）を
     * 満たすため、カラム名の行へマーカーカラム {@code [空]} を 1 つ書く（{@code :1515}）。
     * 残る {@code ExportProjectsInPeriodActionRequestTest/testNormalEnd.yaml:173}／{@code :199} の
     * {@code rows: []} は綴りは同じでも別物で、{@code expected_files} 配下のファイルデータの
     * 0 件レコードであり、カラム名は {@code fields:} に持つ。
     * </p>
     *
     * <p>
     * <b>本テストは 2026-08-18〜19 の間、変換が中止されることを固定していた</b>
     * （{@code stopsClimanSampleConversionBecauseOfZeroRowTable}）。当時の辺③は
     * カラム名 0 件のブロックを {@code IllegalArgumentException} で弾いており、
     * 0 件テーブルを含む YAML を Excel へ変換できないという実運用上の制約があった
     * （{@code issues.md} <b>XLS-27</b>）。
     * </p>
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
}
