package nablarch.test.tool.converter;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;
import nablarch.test.tool.converter.xls.ExcelFormatConfig;
import nablarch.test.tool.converter.xls.XlsFormatWriter;
import nablarch.test.tool.converter.yaml.YamlFormatReader;

import org.apache.poi.ss.usermodel.IndexedColors;

import org.apache.maven.plugin.MojoExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@link ConverterMojo} のテストクラス。
 *
 * <p>maven-plugin-testing-harness を使わず、リフレクションでフィールドを注入して {@code execute()} を呼ぶ方式を採る。</p>
 *
 * @author kiyobot
 */
public class ConverterMojoTest {

    /** 一時ディレクトリ */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** 入力ルート */
    private Path in;

    /** 出力ルート */
    private Path out;

    /**
     * 入出力ルートを初期化する。
     *
     * @throws IOException 入出力エラー
     */
    @Before
    public void setUp() throws IOException {
        in = folder.newFolder("in").toPath();
        out = folder.newFolder("out").toPath();
    }

    /**
     * YamlLoader のファイルキャッシュをクリアし、テスト間の干渉を防ぐ。
     */
    @After
    public void clearLoaderCache() {
        YamlLoader.clearCacheForTest();
    }

    // ---- ヘルパ ----

    /**
     * Mojo にフィールドをリフレクションで注入する。
     *
     * @param mojo  対象 Mojo
     * @param name  フィールド名
     * @param value 注入値
     * @throws ReflectiveOperationException リフレクションエラー
     */
    private static void inject(ConverterMojo mojo, String name, Object value)
            throws ReflectiveOperationException {
        Field field = ConverterMojo.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(mojo, value);
    }

    /**
     * カラム名・データ行から行リストを作る。
     *
     * @param cells セル値
     * @return 1 行
     */
    private static List<String> row(String... cells) {
        return Arrays.asList(cells);
    }

    /**
     * 検証用の SETUP_TABLE ブロックを作る。
     *
     * @param name テーブル名
     * @return テーブルブロック
     */
    private static TableDataBlock sampleTable(String name) {
        return new TableDataBlock(
                nablarch.test.core.reader.DataType.SETUP_TABLE_DATA, "", name,
                row("USER_NAME", "ROLE"),
                Arrays.asList(row("alice", "admin")));
    }

    /**
     * 1 セクション 1 テーブルのコンテナを作る。
     */
    private static TestDataContainer oneTable(String containerName, String sectionName,
            TableDataBlock table) {
        TestDataSection section = new TestDataSection(sectionName,
                Collections.<TestDataBlock>singletonList(table));
        return new TestDataContainer(containerName, Collections.singletonList(section));
    }

    /**
     * 入力 Excel ブックを生成する。
     */
    private static void writeXls(TestDataContainer container, Path baseDir) {
        new XlsFormatWriter().write(container, baseDir.toString());
    }

    // ---- テスト: 正常系・委譲 ----

    /**
     * Given: from=xls, to=yaml, 有効な input/output。
     * When : execute() を呼ぶ。
     * Then : XLS→YAML 変換が実行され、YAML ファイルが生成される。
     */
    @Test
    public void executesXlsToYamlConversionNormally() throws Exception {
        // Given
        TableDataBlock table = sampleTable("USERS");
        writeXls(oneTable("BookA", "data", table), in);

        ConverterMojo mojo = new ConverterMojo();
        inject(mojo, "from", "xls");
        inject(mojo, "to", "yaml");
        inject(mojo, "input", in.toFile());
        inject(mojo, "output", out.toFile());

        // When
        mojo.execute();

        // Then
        Path yaml = out.resolve("BookA/data.yaml");
        assertThat(Files.exists(yaml), is(true));
        TestDataContainer read = new YamlFormatReader().read(out.resolve("BookA").toString(), "data");
        List<TestDataBlock> blocks = read.getSections().get(0).getBlocks();
        assertThat(blocks.size(), is(1));
        TableDataBlock actual = (TableDataBlock) blocks.get(0);
        assertThat(actual.getIdentifier(), is("USERS"));
    }

    // ---- テスト: 全パラメータ反映 ----

    /**
     * Given: overwrite=true・includes・excludes・excludeSheets を指定。
     * When : execute() を呼ぶ。
     * Then : ConverterException なしで変換が完了する（パラメータが Builder に伝達される）。
     */
    @Test
    public void allParametersAreForwardedToBuilder() throws Exception {
        // Given: 2 シート (data/skip) を持つ Excel ブックと、既存の出力（overwrite=true で上書き）
        TableDataBlock table = sampleTable("USERS");
        TestDataSection dataSection = new TestDataSection("data",
                Collections.<TestDataBlock>singletonList(table));
        TestDataSection skipSection = new TestDataSection("skip",
                Collections.<TestDataBlock>singletonList(sampleTable("SKIP")));
        TestDataContainer container = new TestDataContainer("Book",
                Arrays.asList(dataSection, skipSection));
        writeXls(container, in);

        // 出力先に既存ファイルを作成（overwrite=true で上書きされるはず）
        Files.createDirectories(out.resolve("Book"));
        Files.write(out.resolve("Book/data.yaml"), "stale".getBytes());

        ConverterMojo mojo = new ConverterMojo();
        inject(mojo, "from", "xls");
        inject(mojo, "to", "yaml");
        inject(mojo, "input", in.toFile());
        inject(mojo, "output", out.toFile());
        inject(mojo, "overwrite", true);
        inject(mojo, "includes", Arrays.asList("Book.xlsx"));
        inject(mojo, "excludes", Collections.emptyList());
        inject(mojo, "excludeSheets", Arrays.asList("skip"));

        // When
        mojo.execute();

        // Then
        assertThat(Files.exists(out.resolve("Book/data.yaml")), is(true));
        assertThat(Files.exists(out.resolve("Book/skip.yaml")), is(false));
    }

    // ---- テスト: 不正形式 ----

    /**
     * Given: from="invalid"（未知の形式識別子）。
     * When : execute() を呼ぶ。
     * Then : MojoExecutionException がスローされる。
     * <p>
     * DataFormat.fromArgument は未知の識別子に対して {@code IllegalArgumentException} をスローする。
     * ConverterMojo はこれを MojoExecutionException に変換して伝達する。
     * </p>
     */
    @Test
    public void throwsMojoExecutionExceptionForInvalidFromFormat() throws Exception {
        // Given
        ConverterMojo mojo = new ConverterMojo();
        inject(mojo, "from", "invalid");
        inject(mojo, "to", "yaml");
        inject(mojo, "input", in.toFile());
        inject(mojo, "output", out.toFile());

        // When / Then
        try {
            mojo.execute();
            fail("should throw MojoExecutionException");
        } catch (MojoExecutionException e) {
            assertThat(e.getCause(), instanceOf(IllegalArgumentException.class));
            assertThat(e.getMessage(), is("unknown data format: invalid"));
        }
    }

    // ---- テスト: 入力ディレクトリ不在 ----

    /**
     * Given: 存在しない input ディレクトリ。
     * When : execute() を呼ぶ。
     * Then : ConverterException（入力不在）が MojoExecutionException に変換されてスローされる。
     */
    @Test
    public void throwsMojoExecutionExceptionWhenInputMissing() throws Exception {
        // Given
        ConverterMojo mojo = new ConverterMojo();
        inject(mojo, "from", "xls");
        inject(mojo, "to", "yaml");
        inject(mojo, "input", new File(in.toFile(), "nope"));
        inject(mojo, "output", out.toFile());

        // When / Then
        try {
            mojo.execute();
            fail("should throw MojoExecutionException");
        } catch (MojoExecutionException e) {
            assertThat(e.getCause(), instanceOf(ConverterException.class));
        }
    }

    // ---- テスト: 上書き衝突 ----

    /**
     * Given: 出力先が既存・overwrite=false（デフォルト）。
     * When : execute() を呼ぶ。
     * Then : ConverterException（上書き衝突）が MojoExecutionException に変換されてスローされる。
     */
    @Test
    public void throwsMojoExecutionExceptionOnOverwriteConflict() throws Exception {
        // Given: Excel ブックを配置し、出力先に既存ファイルを作成
        TableDataBlock table = sampleTable("USERS");
        writeXls(oneTable("BookA", "data", table), in);
        Files.createDirectories(out.resolve("BookA"));
        Files.createFile(out.resolve("BookA/data.yaml"));

        ConverterMojo mojo = new ConverterMojo();
        inject(mojo, "from", "xls");
        inject(mojo, "to", "yaml");
        inject(mojo, "input", in.toFile());
        inject(mojo, "output", out.toFile());
        // overwrite はデフォルト false

        // When / Then
        try {
            mojo.execute();
            fail("should throw MojoExecutionException");
        } catch (MojoExecutionException e) {
            assertThat(e.getCause(), instanceOf(ConverterException.class));
        }
    }

    // ---- テスト: xlsOutput 設定 ----

    /**
     * Given: xlsOutput を省略する（xls→yaml 変換）。
     * When : execute() を呼ぶ。
     * Then : ExcelFormatConfig.defaults() が使われ、変換が正常に完了する。
     */
    @Test
    public void xlsOutputOmitted_usesDefaultConfig() throws Exception {
        // Given: 入力 Excel を作成
        TableDataBlock table = sampleTable("USERS");
        writeXls(oneTable("BookD", "data", table), in);

        ConverterMojo mojo = new ConverterMojo();
        inject(mojo, "from", "xls");
        inject(mojo, "to", "yaml");
        inject(mojo, "input", in.toFile());
        inject(mojo, "output", out.toFile());
        // xlsOutput は注入しない（null のまま → defaults が使われる）

        // When
        mojo.execute();

        // Then
        assertThat(java.nio.file.Files.exists(out.resolve("BookD/data.yaml")), is(true));
    }

    /**
     * Given: xlsOutput に setupHeaderColor=AQUA を指定する。
     * When : execute() を呼ぶ（xls→yaml 変換。xlsOutput は xls 出力時に適用されるが変換自体は正常完了）。
     * Then : 変換が正常に完了し、xlsOutput.toExcelFormatConfig() が AQUA のインデックスを返す。
     */
    @Test
    public void xlsOutputWithColor_appliesColorToConfig() throws Exception {
        // Given: 入力 Excel を作成
        TableDataBlock table = sampleTable("USERS");
        writeXls(oneTable("BookE", "data", table), in);

        XlsOutputConfig xlsOutput = new XlsOutputConfig();
        xlsOutput.setSetupHeaderColor("AQUA");
        ExcelFormatConfig expectedConfig = xlsOutput.toExcelFormatConfig();

        ConverterMojo mojo = new ConverterMojo();
        inject(mojo, "from", "xls");
        inject(mojo, "to", "yaml");
        inject(mojo, "input", in.toFile());
        inject(mojo, "output", out.toFile());
        inject(mojo, "xlsOutput", xlsOutput);

        // When
        mojo.execute();

        // Then: 変換が成功し、XlsOutputConfig が AQUA を ExcelFormatConfig に反映する
        assertThat(java.nio.file.Files.exists(out.resolve("BookE/data.yaml")), is(true));
        assertThat(expectedConfig.getSetupHeaderColorIndex(), is(IndexedColors.AQUA.getIndex()));
    }

    /**
     * Given: xlsOutput に無効なカラー名（"NOT_A_COLOR"）を指定する。
     * When : execute() を呼ぶ。
     * Then : MojoExecutionException がスローされ、メッセージに "Invalid" が含まれ、
     *        原因が IllegalArgumentException である。
     */
    @Test
    public void xlsOutputWithInvalidColor_throwsMojoExecutionException() throws Exception {
        // Given: 無効なカラー名を持つ xlsOutput を設定
        XlsOutputConfig xlsOutput = new XlsOutputConfig();
        xlsOutput.setSetupHeaderColor("NOT_A_COLOR");

        ConverterMojo mojo = new ConverterMojo();
        inject(mojo, "from", "xls");
        inject(mojo, "to", "yaml");
        inject(mojo, "input", in.toFile());
        inject(mojo, "output", out.toFile());
        inject(mojo, "xlsOutput", xlsOutput);

        // When / Then
        try {
            mojo.execute();
            fail("should throw MojoExecutionException");
        } catch (MojoExecutionException e) {
            assertThat(e.getCause(), instanceOf(IllegalArgumentException.class));
            assertThat(e.getMessage().contains("Invalid"), is(true));
        }
    }
}
