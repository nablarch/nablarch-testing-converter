package nablarch.test.tool.converter.xls;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nablarch.test.core.reader.DataType;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 辺③（中間モデル→Excel）の軸F — 出力先とシート名の異常系を固定するテスト。
 *
 * <p>
 * 対象は {@code .rn/ntf-test-data-converter/coverage/inventory.md} §0.7 の辺③ 4 ケースのうち 3 件
 * （F3-01 出力先不在／F3-03 書き込み権限なし／F3-04 シート名が Excel 制約違反）である。
 * 残る <b>F3-02（同名ファイル既存かつ {@code overwrite=false}）は辺③の対象外</b>とする。
 * {@link XlsFormatWriter} は {@code overwrite} を保持せず（保持するのは {@code ConversionRequest} /
 * {@code TestDataConverter} / {@code ConverterMojo}）、衝突時の振る舞いは上位層の既存テスト
 * {@code TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse} ／
 * {@code ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict} が担保しているためである
 * （棚卸し §0.8-5）。
 * </p>
 *
 * <p>
 * 異常系は<b>例外になるもの</b>（F3-03・F3-04 の禁止文字）と<b>例外にならず書けてしまうもの</b>
 * （F3-01・F3-04 の 31 文字超）に分かれる。後者は「書けてしまった結果」をそのまま固定する。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>期待される仕様ではない。
 * 妥当でないと判断した挙動は {@code .rn/ntf-test-data-converter/coverage/issues.md} に課題（XLS-16）
 * として記録してあり、実装（src/main）は変更していない。
 * </p>
 *
 * @author kiyobot
 */
public class XlsFormatWriterInvalidOutputTest {

    /** 既定のシート名。 */
    private static final String SHEET = "s";

    /** Excel がシート名に許す最大文字数。 */
    private static final int EXCEL_MAX_SHEET_NAME_LENGTH = 31;

    /** 出力先。 */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** 権限を落としたディレクトリ（{@link TemporaryFolder} が後片付けできるよう復元する）。 */
    private File permissionDroppedDir;

    /**
     * 権限を落としたディレクトリがあれば書き込み可能へ戻す。
     */
    @After
    public void restorePermission() {
        if (permissionDroppedDir != null) {
            permissionDroppedDir.setWritable(true, true);
        }
    }

    // ------------------------------------------------------------------ helpers

    /**
     * 1 ブロック 1 セクションのコンテナを組み立てる。
     *
     * @param book      ブック名
     * @param sheetName シート名
     * @return コンテナ
     */
    private static TestDataContainer container(String book, String sheetName) {
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                Collections.singletonList("C"),
                Collections.singletonList(Collections.singletonList("v")));
        TestDataSection section = new TestDataSection(sheetName,
                Collections.<TestDataBlock>singletonList(table));
        return new TestDataContainer(book, Collections.singletonList(section));
    }

    /**
     * 同じ文字を繰り返した文字列を作る。
     *
     * @param c     文字
     * @param count 繰り返し回数
     * @return 文字列
     */
    private static String repeat(char c, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(c);
        }
        return builder.toString();
    }

    /**
     * ディレクトリの書き込み権限を落とし、それが実際に効いているかを返す。
     *
     * <p>
     * root 実行や権限を無視するファイルシステムでは権限を落としても書き込めてしまい、
     * 「権限が無いから失敗した」というテストが偽 PASS になる。実際にファイルを作ってみて
     * 拒否されることを確かめ、拒否されない環境ではテストをスキップさせる。
     * </p>
     *
     * @param dir 対象ディレクトリ
     * @return 権限が効いている（書き込みが拒否される）なら真
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
            throw new IllegalStateException("権限の実効性を確認できなかった: " + dir, e);
        }
        // 作れてしまった＝権限が効いていない。後片付けだけして偽 PASS を避ける。
        try {
            Files.delete(canary);
        } catch (IOException e) {
            throw new IllegalStateException("確認用ファイルを削除できなかった: " + canary, e);
        }
        return false;
    }

    // ------------------------------------------------------------------ F3-01 出力先不在

    /**
     * Given: 存在しない多階層の出力先ディレクトリ。
     * When : {@code write}。
     * Then : 例外にならず、ディレクトリが作られてブックが書き出される。
     *
     * <p>
     * 担保する軸要素: F3-01。既存の {@code XlsFormatWriterTest#wrapsIoFailure} は
     * 「親に通常ファイルが居座り<b>ディレクトリを作れない</b>」ケース（{@code UncheckedIOException}）であり、
     * 「出力先が単に無い」ケースはこちらが担う。作成は {@link XlsFormatWriter#write} の
     * {@code Files.createDirectories} による。
     * </p>
     */
    @Test
    public void createsMissingOutputDirectoriesAndWritesWorkbook() {
        // Given
        File missing = new File(folder.getRoot(), "no/such/dir");
        assertFalse("前提: 出力先はまだ存在しない", missing.exists());

        // When
        new XlsFormatWriter().write(container("Missing", SHEET), missing.getAbsolutePath());

        // Then
        assertTrue("出力先ディレクトリが作られる", missing.isDirectory());
        File written = new File(missing, "Missing.xlsx");
        assertTrue("ブックが書き出される", written.exists());
        Workbook workbook = XlsFixture.open(written.toPath());
        assertThat(workbook.getSheet(SHEET), is(notNullValue()));
        assertThat(workbook.getSheet(SHEET).getRow(0).getCell(0).getStringCellValue(), is("SETUP_TABLE=T"));
    }

    // ------------------------------------------------------------------ F3-03 書き込み権限なし

    /**
     * Given: 書き込み権限を落とした出力先ディレクトリ。
     * When : {@code write}。
     * Then : {@code UncheckedIOException} を送出し、メッセージに出力先ファイルのパスが入る。
     *        原因は {@code java.nio.file.AccessDeniedException}。ファイルは作られない。
     *
     * <p>
     * 担保する軸要素: F3-03。権限が効かない環境（root 実行など）では
     * {@link Assume} でスキップする（緑にごまかさない）。
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
                () -> new XlsFormatWriter().write(container("Denied", SHEET), readOnly.getAbsolutePath()));

        // Then
        assertThat(thrown.getMessage(), containsString("failed to write Excel:"));
        assertThat("どのファイルを書けなかったかが分かる", thrown.getMessage(), containsString("Denied.xlsx"));
        assertThat(thrown.getCause(), is(instanceOf(AccessDeniedException.class)));
        assertFalse("ファイルは作られない", new File(readOnly, "Denied.xlsx").exists());
    }

    // ------------------------------------------------------------------ F3-04 シート名が Excel 制約違反

    /**
     * Given: Excel がシート名に禁じる文字（{@code / \ ? * [ ] :}）を含むセクション名。
     * When : {@code write}。
     * Then : POI の {@code IllegalArgumentException} が送出され、どの文字がどの位置で不正かが
     *        メッセージに入る。ブックは作られない。
     *
     * <p>担保する軸要素: F3-04（禁止文字）。検査は POI の {@code WorkbookUtil} が行う。</p>
     */
    @Test
    public void rejectsSheetNameContainingCharacterForbiddenByExcel() {
        // Given
        List<String> forbidden = Arrays.asList("a/b", "a\\b", "a?b", "a*b", "a[b", "a]b", "a:b");

        for (String sheetName : forbidden) {
            // When
            IllegalArgumentException thrown = assertThrows(sheetName, IllegalArgumentException.class,
                    () -> new XlsFormatWriter().write(container("Forbidden", sheetName),
                            folder.getRoot().getAbsolutePath()));

            // Then
            assertThat(sheetName, thrown.getMessage(), containsString("Invalid char"));
            assertThat(sheetName, thrown.getMessage(), containsString("in sheet name '" + sheetName + "'"));
            assertFalse("ブックは作られない: " + sheetName,
                    new File(folder.getRoot(), "Forbidden.xlsx").exists());
        }
    }

    /**
     * Given: 空文字のセクション名。
     * When : {@code write}。
     * Then : POI の {@code IllegalArgumentException}（文字数が 1 以上 31 以下でない）が送出される。
     *
     * <p>担保する軸要素: F3-04（文字数の下限）。</p>
     */
    @Test
    public void rejectsEmptySheetName() {
        // When
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new XlsFormatWriter().write(container("Empty", ""),
                        folder.getRoot().getAbsolutePath()));

        // Then
        assertThat(thrown.getMessage(), containsString("sheetName '' is invalid"));
        assertFalse("ブックは作られない", new File(folder.getRoot(), "Empty.xlsx").exists());
    }

    /**
     * Given: Excel の上限（31 文字）を 1 文字超える 32 文字のセクション名。
     * When : {@code write}。
     * Then : 例外にならず、シート名が<b>黙って 31 文字へ切り詰められて</b>書き出される。
     *        元の名前ではシートを引けず、変換ツール自身の読み戻し（{@link XlsFormatReader}）は
     *        {@code sheet not found} で失敗する。
     *
     * <p>
     * 担保する軸要素: F3-04（文字数の上限）。切り詰めは POI の {@code Workbook#createSheet} が
     * メモリ上のブックの時点で行う。セクション名が変換後のブックで変わってしまうため、
     * {@code issues.md} の <b>XLS-16</b> に課題として記録した（修正はしない）。
     * </p>
     */
    @Test
    public void truncatesSheetNameLongerThanExcelLimitSilently() {
        // Given
        String tooLong = repeat('a', EXCEL_MAX_SHEET_NAME_LENGTH + 1);

        // When
        new XlsFormatWriter().write(container("TooLong", tooLong), folder.getRoot().getAbsolutePath());

        // Then
        assertThat("切り詰めはメモリ上のブックの時点で起きている",
                new XlsFormatWriter().build(container("TooLong", tooLong)).getSheetName(0).length(),
                is(EXCEL_MAX_SHEET_NAME_LENGTH));
        Workbook workbook = XlsFixture.open(folder.getRoot().toPath().resolve("TooLong.xlsx"));
        assertThat("シートは 1 枚だけ", workbook.getNumberOfSheets(), is(1));
        assertThat("31 文字へ切り詰められる（issues.md XLS-16）",
                workbook.getSheetName(0), is(repeat('a', EXCEL_MAX_SHEET_NAME_LENGTH)));
        assertThat("元のセクション名ではシートを引けない", workbook.getSheet(tooLong), is(nullValue()));
        // 変換ツール自身の読み戻しも元の名前では失敗する
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new XlsFormatReader().read(folder.getRoot().getAbsolutePath(),
                        "TooLong/" + tooLong));
        assertThat(thrown.getMessage(), containsString("sheet not found."));
    }

    /**
     * Given: 先頭 31 文字が同じで 32 文字目だけが違う 2 つのセクション名。
     * When : {@code write}。
     * Then : 切り詰めた結果が衝突し、{@code IllegalArgumentException} で失敗する。
     *
     * <p>
     * 担保する軸要素: F3-04（切り詰めの帰結）。切り詰めそのものは黙って起こるが、
     * 衝突した場合だけは失敗する（{@code issues.md} XLS-16 の境界）。
     * </p>
     */
    @Test
    public void failsWhenTruncatedSheetNamesCollide() {
        // Given
        String base = repeat('a', EXCEL_MAX_SHEET_NAME_LENGTH);
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                Collections.singletonList("C"),
                Collections.singletonList(Collections.singletonList("v")));
        TestDataSection first = new TestDataSection(base + "1",
                Collections.<TestDataBlock>singletonList(table));
        TestDataSection second = new TestDataSection(base + "2",
                Collections.<TestDataBlock>singletonList(table));
        TestDataContainer container = new TestDataContainer("Collide", Arrays.asList(first, second));

        // When
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new XlsFormatWriter().write(container, folder.getRoot().getAbsolutePath()));

        // Then
        assertThat(thrown.getMessage(), containsString("already contains a sheet of this name"));
    }
}
