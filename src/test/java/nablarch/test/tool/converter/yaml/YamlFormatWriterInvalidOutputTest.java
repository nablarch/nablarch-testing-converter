package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import nablarch.test.core.reader.DataType;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import org.junit.After;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 中間モデル → YAML の書き出しについて、出力先の異常系を固定するテスト。
 *
 * <p>
 * 対象は出力先の異常系 3 ケースのうち 2 件
 * （出力先不在／書き込み権限なし）である。残る <b>同名ファイル既存かつ
 * {@code overwrite=false} は本クラスの対象外</b>とする。{@link YamlFormatWriter} は {@code overwrite} を
 * 保持しないためである（{@code overwrite} を持つのは {@code ConversionRequest} ／ {@code TestDataConverter} ／
 * {@code ConverterMojo} だけである）。
 * </p>
 *
 * <p>
 * <b>YAML の書き出しについては上位層の既存テストが衝突検査を通している。</b>衝突を検査するのは
 * {@code TestDataConverter#checkOverwrite} で、{@code request.isOverwrite()} が偽のとき
 * {@code target.outputPaths(...)} が返すパスの存在を見て {@code ConverterException} を送出する。
 * {@code TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse} と
 * {@code ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict} はどちらも
 * <b>XLS→YAML</b>（前者は {@code TestDataConverter.convert(DataFormat.XLS, DataFormat.YAML, in, out)}、
 * 後者は Mojo の {@code from=xls} / {@code to=yaml}）で、出力先に {@code BookA/data.yaml} を
 * 置いた状態から {@code ConverterException} ／ {@code MojoExecutionException} をアサートしている。
 * すなわち {@code checkOverwrite} が呼ぶ {@code FormatHandler#outputPaths} の実体は
 * {@code YamlFormatHandler#outputPaths}（セクション名 ＋ {@code .yaml} を並べる）であり、
 * <b>{@code .yaml} を出力側とする衝突は担保されている</b>。
 * {@code .xlsx} を出力側とする衝突（{@code checkOverwrite} の {@code Files.exists(output)} から
 * {@code ConverterException} へ進む分岐）を通すテストが 1 件も無いのとは対照的である。
 * </p>
 *
 * <p>
 * 出力先不在は<b>例外にならない</b>。{@link YamlFormatWriter#write} が
 * {@code Files.createDirectories} でディレクトリを作るためである。既存の
 * {@code YamlFormatWriterTest#write_ioError_throwsUncheckedIOException} が扱うのは
 * 「親に通常ファイルが居座り<b>ディレクトリを作れない</b>」ケースであり、別の入力である。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションは、YAML 形式の出力先の扱いの仕様である</b>（存在しない出力先はディレクトリを作って書き出し、
 * 書き込めない出力先は {@code UncheckedIOException} で止める）。
 * </p>
 *
 * @author kiyobot
 */
public class YamlFormatWriterInvalidOutputTest {

    /** セクション名（＝出力ファイル名の幹）。 */
    private static final String SECTION = "td";

    /** 出力先。 */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** 権限を落としたディレクトリ（{@link TemporaryFolder} が後片付けできるよう復元する）。 */
    private File permissionDroppedDir;

    /**
     * 権限を落としたディレクトリがあれば書き込み可能へ戻す。
     *
     * <p>兄弟の {@code XlsFormatWriterInvalidOutputTest#restorePermission} と同じ理由・同じ形である。</p>
     */
    @After
    public void restorePermission() {
        if (permissionDroppedDir != null) {
            permissionDroppedDir.setWritable(true, true);
        }
    }

    // ------------------------------------------------------------------ helpers

    /**
     * セクション 1 件・ブロック 1 件のコンテナを組み立てる。
     *
     * @return コンテナ
     */
    private static TestDataContainer container() {
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                Collections.singletonList("C"),
                Collections.singletonList(Collections.singletonList("v")));
        return new TestDataContainer(SECTION, Collections.singletonList(
                new TestDataSection(SECTION, Collections.<TestDataBlock>singletonList(table))));
    }

    /**
     * 出力先ディレクトリから、書き出されるはずのファイルを求める。
     *
     * <p>
     * セクション名を組み立て側と検査側に別々のリテラルで書くと、片方だけを書き換えたときに
     * 「存在しないファイルを見て {@code assertFalse} が無条件に通る」ため、名前を 1 箇所に束ねる
     * （兄弟の {@code XlsFormatWriterInvalidOutputTest#writtenBook} と同じ理由）。
     * </p>
     *
     * @param dir 出力先ディレクトリ
     * @return 書き出されるはずのファイル
     */
    private static File writtenYaml(File dir) {
        return new File(dir, SECTION + YamlFixture.EXTENSION);
    }

    /**
     * ディレクトリの書き込み権限を落とし、それが実際に効いているかを返す。
     *
     * <p>
     * root 実行や権限を無視するファイルシステムでは権限を落としても書き込めてしまい、
     * 「権限が無いから失敗した」というテストが偽 PASS になる。実際にファイルを作ってみて
     * 拒否されることを確かめ、拒否されない環境ではテストをスキップさせる
     * （兄弟の {@code XlsFormatWriterInvalidOutputTest#dropWritePermission} と同じ形）。
     * </p>
     *
     * @param dir 対象ディレクトリ
     * @return 権限が効いていて {@link AccessDeniedException} で拒否されるなら真。
     *         権限が効いていない（書けてしまう）なら偽
     */
    private boolean dropWritePermission(File dir) {
        permissionDroppedDir = dir;
        dir.setWritable(false, false);
        Path canary = dir.toPath().resolve("canary.txt");
        try {
            Files.createFile(canary);
        } catch (AccessDeniedException expected) {
            return true;
        } catch (IOException e) {
            // 書き込みは拒否されたが AccessDeniedException ではない環境。ERROR にせずスキップする。
            Assume.assumeNoException(
                    "書き込み拒否が AccessDeniedException にならない環境ではスキップする: " + dir, e);
            return false;
        }
        // 作れてしまった＝権限が効いていない。後片付けだけして偽 PASS を避ける。
        try {
            Files.delete(canary);
        } catch (IOException e) {
            throw new IllegalStateException("確認用ファイルを削除できなかった: " + canary, e);
        }
        return false;
    }

    // ------------------------------------------------------------------ 出力先不在

    /**
     * Given: 存在しない多階層の出力先ディレクトリ。
     * When : {@code write}。
     * Then : 例外にならず、ディレクトリが作られて YAML が書き出される。
     *
     * <p>担保する観点: 出力先不在。作成は {@link YamlFormatWriter#write} の {@code Files.createDirectories} による。</p>
     */
    @Test
    public void createsMissingOutputDirectoriesAndWritesYaml() throws IOException {
        // Given
        File missing = new File(folder.getRoot(), "no/such/dir");
        assertFalse("前提: 出力先はまだ存在しない", missing.exists());

        // When
        new YamlFormatWriter().write(container(), missing.getAbsolutePath());

        // Then
        assertTrue("出力先ディレクトリが作られる", missing.isDirectory());
        File written = writtenYaml(missing);
        assertTrue("YAML が書き出される", written.exists());
        assertThat(new String(Files.readAllBytes(written.toPath()), StandardCharsets.UTF_8), is(""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - \"C\": \"v\"\n"));
    }

    // ------------------------------------------------------------------ 書き込み権限なし

    /**
     * Given: 書き込み権限を落とした出力先ディレクトリ。
     * When : {@code write}。
     * Then : {@code UncheckedIOException} を送出し、メッセージに出力先ファイルのパスが入る。
     *        原因は {@code java.nio.file.AccessDeniedException}。ファイルは作られない。
     *
     * <p>
     * 担保する観点: 書き込み権限なし。権限が効かない環境（root 実行など）では {@link Assume} でスキップする
     * （緑にごまかさない）。
     * </p>
     */
    @Test
    public void wrapsAccessDeniedExceptionWhenOutputDirectoryIsNotWritable() throws IOException {
        // Given
        File readOnly = folder.newFolder("readonly");
        Assume.assumeTrue("書き込み権限が効かない環境ではスキップする（root 実行・権限を無視する FS など）",
                dropWritePermission(readOnly));

        // When
        UncheckedIOException thrown = assertThrows(UncheckedIOException.class,
                () -> new YamlFormatWriter().write(container(), readOnly.getAbsolutePath()));

        // Then
        assertThat(thrown.getMessage(), containsString("failed to write YAML:"));
        assertThat("どのファイルを書けなかったかが分かる",
                thrown.getMessage(), containsString(SECTION + YamlFixture.EXTENSION));
        assertThat(thrown.getCause(), is(instanceOf(AccessDeniedException.class)));
        assertFalse("ファイルは作られない", writtenYaml(readOnly).exists());
    }
}
