package nablarch.test.tool.converter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.tool.converter.xls.ExcelFormatConfig;
import nablarch.test.tool.converter.yaml.ValidationError;
import nablarch.test.tool.converter.yaml.YamlTestDataValidator;

import org.apache.poi.ss.usermodel.IndexedColors;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 解説書 {@code tools/testdata_converter.rst}（{@code 5783b35}）が述べている
 * <b>変換ツールの入口の振る舞い</b>を押さえるテスト（指示書 第3節のうち 3-1・3-3・3-4・3-5・3-11）。
 *
 * <p>
 * 4 件（3-1・3-3・3-4・3-11）は<b>負のテスト</b>である。すなわち「起こらないこと」「効かないこと」を
 * 押さえる。負のテストは崩しても落ちないことが起こりやすいため、期待値をわざと崩して落ちることを
 * 特に念入りに確認した（記録は {@code checks/task-38.md}）。
 * </p>
 *
 * @author kiyobot
 */
public class ConverterDocumentedBehaviorTest {

    /** コンテナ名（Excel ではブック名、YAML ではディレクトリ名）。 */
    private static final String CONTAINER = "SampleTest";

    /** 読み込み単位名（Excel ではシート名、YAML ではファイル名）。 */
    private static final String UNIT = "td";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** 入力ルート。 */
    private Path in;

    /** 出力ルート。 */
    private Path out;

    @Before
    public void setUp() throws IOException {
        in = folder.newFolder("in").toPath();
        out = folder.newFolder("out").toPath();
    }

    /** YAML ローダのファイルキャッシュをクリアし、テスト間の干渉を防ぐ。 */
    @After
    public void clearLoaderCache() {
        YamlLoader.clearCacheForTest();
    }

    // ------------------------------------------------------------------ helpers

    /**
     * YAML の読み込み単位を書き出す。
     *
     * @param containerDir コンテナディレクトリ
     * @param unitName     読み込み単位名（拡張子なし）
     * @param yamlText     内容
     */
    private static void writeYaml(Path containerDir, String unitName, String yamlText) {
        try {
            Files.createDirectories(containerDir);
            Files.write(containerDir.resolve(unitName + ".yaml"), yamlText.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("failed to write fixture: " + containerDir, e);
        }
    }

    /**
     * 書き出された YAML の全文を返す。
     *
     * @param containerDir コンテナディレクトリ
     * @param unitName     読み込み単位名（拡張子なし）
     * @return 全文
     */
    private static String readText(Path containerDir, String unitName) {
        try {
            return new String(Files.readAllBytes(containerDir.resolve(unitName + ".yaml")),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("failed to read: " + containerDir, e);
        }
    }

    /** テーブル 1 つを持つ最小の YAML。 */
    private static String minimalTableYaml() {
        return ""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - A: \"1\"\n"
                + "        B: \"2\"\n";
    }

    // ------------------------------------------------------------------ 3-1

    /**
     * Given: {@code YamlTestDataValidator} が問題として報告する YAML
     *        （フィールド 2 件に対しデータ行が 3 要素 ＝ V-COL）。
     * When : その YAML を変換元にして {@code TestDataConverter.convert} を実行する。
     * Then : <b>検証を理由に失敗しない</b>（例外にならず、コンテナ 1 件を変換する）（<b>負のテスト</b>）。
     *
     * <p>
     * {@code tools/testdata_converter.rst:53}-{@code :55}
     * 「検証は変換の処理経路には組み込まれておらず、変換の実行時に自動では呼び出されない。
     * 検査したい場合は明示的に呼び出す。」
     * </p>
     *
     * <p>
     * <b>「検証が報告する種類の YAML である」ことも同じ入力で実測する。</b>そうしないと、
     * ただ変換できる YAML を渡しただけのテストになるためである。
     * </p>
     *
     * <p>
     * <b>検証の 7 項目すべてが変換を通るわけではない（2026-08-28 実測）。</b>ただし止めているのは
     * いずれも {@code YamlTestDataValidator} ではなく<b>上流</b>であり、{@code :53}-{@code :55} と
     * 矛盾しない。
     * </p>
     * <ul>
     *   <li><b>未知のディレクティブ名（V-DKEY）</b> —— スキーマ非適合でもあるため、上流ローダの
     *       スキーマ検証が {@code YamlSchemaValidationException} を投げる
     *       （{@code $.setup_files[0].directives: プロパティ 'no-such-directive' がスキーマで
     *       定義されておらず……}）</li>
     *   <li><b>フィールド名の重複（V-FNAME）</b> —— 本体のレコード解析が
     *       {@code IllegalArgumentException: Duplicate field names are not permitted in a record.} を投げる</li>
     * </ul>
     * <p>
     * 本テストは<b>スキーマも本体の解析も通る V-COL</b> で押さえる。V-COL の余りの値が黙って捨てられる
     * ことは {@code coverage/issues.md} <b>YML-14</b> に記録済みである。
     * </p>
     */
    @Test
    public void convertDoesNotRunValidation() {
        // Given
        Path container = in.resolve(CONTAINER);
        writeYaml(container, UNIT, ""
                + "setup_files:\n"
                + "  - path: \"test.dat\"\n"
                + "    type: fixed\n"
                + "    records:\n"
                + "      - record_type: \"\"\n"
                + "        fields:\n"
                + "          - {name: col1, type: 半角英字, length: \"3\"}\n"
                + "          - {name: col2, type: 半角英字, length: \"3\"}\n"
                + "        rows:\n"
                + "          - [\"a\", \"b\", \"c\"]\n");

        // When / Then: 検証は問題として報告する
        List<ValidationError> errors = new YamlTestDataValidator().validate(container);
        assertThat("検証は問題を報告する", errors.size(), is(1));
        assertThat(messagesOf(errors), containsString("[V-COL]"));

        // When / Then: それでも変換は失敗しない
        int count = TestDataConverter.convert(DataFormat.YAML, DataFormat.XLS, in, out);
        assertThat("変換したコンテナ件数", count, is(1));
        assertTrue("変換結果が出力されている", Files.exists(out.resolve(CONTAINER + ".xlsx")));
    }

    /**
     * 検証エラーのメッセージを連結する。
     *
     * @param errors 検証エラー
     * @return 連結したメッセージ
     */
    private static String messagesOf(List<ValidationError> errors) {
        StringBuilder sb = new StringBuilder();
        for (ValidationError error : errors) {
            sb.append(error.getMessage()).append('\n');
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ 3-3

    /**
     * Given: コメント行を含む YAML。
     * When : YAML→YAML で変換する。
     * Then : 往復後の YAML に<b>コメントが無い</b>（<b>負のテスト</b>）。
     *
     * <p>
     * {@code tools/testdata_converter.rst:59}「形式に固有で、テスティングフレームワークの仕様上の意味を
     * 持たない情報は中間モデルに保持されないため、往復しても再現されない。……YAML 形式のコメントが
     * これにあたる」。
     * </p>
     */
    @Test
    public void dropsYamlCommentsOnRoundTrip() {
        // Given
        Path container = in.resolve(CONTAINER);
        writeYaml(container, UNIT, ""
                + "# 先頭のコメント\n"
                + "setup_tables:\n"
                + "  - table: \"T\"   # 行末のコメント\n"
                + "    rows:\n"
                + "      # 行の間のコメント\n"
                + "      - A: \"1\"\n"
                + "        B: \"2\"\n");

        // When
        int count = TestDataConverter.convert(DataFormat.YAML, DataFormat.YAML, in, out);

        // Then
        assertThat(count, is(1));
        String written = readText(out.resolve(CONTAINER), UNIT);
        assertThat("コメント記号が残らない", written, not(containsString("#")));
        assertThat("先頭のコメント", written, not(containsString("先頭のコメント")));
        assertThat("行末のコメント", written, not(containsString("行末のコメント")));
        assertThat("行の間のコメント", written, not(containsString("行の間のコメント")));
        assertThat("値は保たれる", written, containsString("A: \"1\""));
    }

    // ------------------------------------------------------------------ 3-4

    /**
     * Given: 変換元が YAML 形式のテストデータ。
     * When : {@code excludeSheet} を指定した場合としない場合で変換する。
     * Then : 変換件数と出力内容が一致し、エラーにもならない（<b>負のテスト</b>）。
     *
     * <p>
     * {@code tools/testdata_converter.rst:176}
     * 「変換元が YAML 形式のときはシートの概念がないため無視される」。
     * </p>
     */
    @Test
    public void ignoresExcludeSheetsWhenSourceIsYaml() throws IOException {
        // Given
        writeYaml(in.resolve(CONTAINER), UNIT, minimalTableYaml());
        Path withoutOption = folder.newFolder("out-plain").toPath();
        Path withOption = folder.newFolder("out-excluded").toPath();

        // When
        int plain = TestDataConverter.convert(new ConversionRequest.Builder()
                .sourceFormat(DataFormat.YAML).targetFormat(DataFormat.YAML)
                .inputPath(in).outputPath(withoutOption).build());
        YamlLoader.clearCacheForTest();
        int excluded = TestDataConverter.convert(new ConversionRequest.Builder()
                .sourceFormat(DataFormat.YAML).targetFormat(DataFormat.YAML)
                .inputPath(in).outputPath(withOption)
                // 読み込み単位名そのものを除外指定しても無視される
                .excludeSheet(UNIT).excludeSheet("abnormalCase").build());

        // Then
        assertThat("変換件数が一致する", excluded, is(plain));
        assertThat("変換件数は 1", plain, is(1));
        assertThat("出力内容が一致する",
                readText(withOption.resolve(CONTAINER), UNIT),
                is(readText(withoutOption.resolve(CONTAINER), UNIT)));
    }

    // ------------------------------------------------------------------ 3-5

    /**
     * Given: 直下とサブディレクトリの両方に不正な YAML を置いたディレクトリ構成。
     * When : {@code YamlTestDataValidator#validate} を、直下に {@code .yaml} を持つディレクトリと
     *        持たない上位ディレクトリのそれぞれで呼ぶ。
     * Then : 前者は<b>直下のぶんだけ</b>を返し、後者は空リストを返す。
     *
     * <p>
     * {@code tools/testdata_converter.rst:233}
     * 「指定したディレクトリの直下にある {@code .yaml} ファイルだけを検査し、サブディレクトリはたどらない。
     * ……上位のディレクトリを指定すると、1 件も検査しないまま空のリストが返る。」
     * </p>
     */
    @Test
    public void validateLooksOnlyAtDirectChildren() {
        // Given
        Path container = in.resolve(CONTAINER);
        String vcol = ""
                + "setup_files:\n"
                + "  - path: \"test.dat\"\n"
                + "    type: fixed\n"
                + "    records:\n"
                + "      - record_type: \"\"\n"
                + "        fields:\n"
                + "          - {name: col1, type: 半角英字, length: \"3\"}\n"
                + "        rows:\n"
                + "          - [\"a\", \"b\"]\n";
        writeYaml(container, "direct", vcol);
        writeYaml(container.resolve("nested"), "nested", vcol);

        // When
        List<ValidationError> direct = new YamlTestDataValidator().validate(container);
        List<ValidationError> upper = new YamlTestDataValidator().validate(in);

        // Then
        assertThat("直下のぶんだけを検査する", direct.size(), is(1));
        assertThat("サブディレクトリのファイルは検査しない",
                direct.get(0).getFilePath(), containsString("direct.yaml"));
        assertThat("直下に .yaml を持たない上位ディレクトリは空リスト",
                upper, is(Collections.<ValidationError>emptyList()));
    }

    // ------------------------------------------------------------------ 3-11

    /**
     * Given: {@code ExcelFormatConfig} を設定した {@code ConversionRequest}。
     * When : {@code to=yaml} の変換を実行する。
     * Then : 出力 YAML の中身が設定なしの場合と一致する（<b>負のテスト</b>）。
     *
     * <p>
     * {@code tools/testdata_converter.rst:239}
     * 「YAML 形式へ書き出す場合、本ツールは記法どおりに書くだけで、整形の設定を持たない」。
     * </p>
     */
    @Test
    public void excelFormatConfigDoesNotAffectYamlOutput() throws IOException {
        // Given
        writeYaml(in.resolve(CONTAINER), UNIT, minimalTableYaml());
        Path withoutConfig = folder.newFolder("out-noconfig").toPath();
        Path withConfig = folder.newFolder("out-config").toPath();
        ExcelFormatConfig config = ExcelFormatConfig.defaults()
                .withTestShotsHeaderColor(IndexedColors.RED.getIndex())
                .withSetupHeaderColor(IndexedColors.RED.getIndex())
                .withExpectedHeaderColor(IndexedColors.RED.getIndex())
                .withOtherHeaderColor(IndexedColors.RED.getIndex())
                .withMarkerColumnColor(IndexedColors.RED.getIndex())
                .withAutoColumnWidth(false)
                .withMaxColumnWidthChars(3)
                .withBlockBorder(false)
                .withCellBorder(false)
                .withDisplayGridlines(true)
                .withBlankRowsBetweenBlocks(5);

        // When
        TestDataConverter.convert(new ConversionRequest.Builder()
                .sourceFormat(DataFormat.YAML).targetFormat(DataFormat.YAML)
                .inputPath(in).outputPath(withoutConfig).build());
        YamlLoader.clearCacheForTest();
        TestDataConverter.convert(new ConversionRequest.Builder()
                .sourceFormat(DataFormat.YAML).targetFormat(DataFormat.YAML)
                .inputPath(in).outputPath(withConfig)
                .excelFormatConfig(config).build());

        // Then
        assertThat("出力 YAML の中身が一致する",
                readText(withConfig.resolve(CONTAINER), UNIT),
                is(readText(withoutConfig.resolve(CONTAINER), UNIT)));
        assertThat("出力されたファイルはこの 1 つだけ",
                Arrays.asList(withConfig.resolve(CONTAINER).toFile().list()),
                is(Collections.singletonList(UNIT + ".yaml")));
    }
}
