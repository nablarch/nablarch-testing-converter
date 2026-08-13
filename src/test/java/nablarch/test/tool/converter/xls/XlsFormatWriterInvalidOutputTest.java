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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nablarch.test.core.reader.DataType;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
 * {@code TestDataConverter} / {@code ConverterMojo}）、衝突検査は {@code XlsFormatWriter} を呼ぶ前に
 * 上位層で完結するためである（棚卸し §0.8-5）。
 * </p>
 *
 * <p>
 * <b>ただし上位層の既存テストが担保しているのは、{@code .yaml} を<i>出力</i>側とする衝突だけである。</b>
 * {@code TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse} ／
 * {@code ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict} はどちらも XLS→YAML であり、
 * {@code TestDataConverter#checkOverwrite} が多態で呼ぶ {@code FormatHandler#outputPaths} の実体は
 * {@code YamlFormatHandler#outputPaths} である。
 * </p>
 *
 * <p>
 * <b>{@code XlsFormatHandler#outputPaths} 自体は {@code overwrite=false} 下で実行されている。</b>
 * {@code TestDataConverter.convert(DataFormat, DataFormat, Path, Path)} は {@code overwrite} を
 * 既定値 {@code false} のままリクエストを組むため（{@code ConversionRequest.Builder#overwrite} を呼ばない）、
 * {@code checkOverwrite} は早期 return せず {@code outputPaths} を呼ぶ。YAML→XLS ／ XLS→XLS を通す
 * {@code TestDataConverterTest#convertsYamlToXls} ／ {@code #convertsXlsToXls} ／
 * {@code #convertsYamlWithFilesToXls} の 3 件がこれに当たる
 * （実測 2026-08-13: {@code XlsFormatHandler#outputPaths} を送出に変異させると、落ちるのはこの 3 件だけ）。
 * 1 件も通っていないのは <b>{@code .xlsx} が既存で衝突する分岐</b>
 * （{@code checkOverwrite} の {@code Files.exists(output)} が真 → {@code ConverterException}）のほうである
 * （実測 2026-08-13: この分岐を {@code .xlsx} のときだけ {@code AssertionError} に変異させても 0 failures）。
 * すなわち共通処理 {@code checkOverwrite} の分岐自体は通っているが、
 * <b>{@code .xlsx} を出力側とする衝突は未担保</b>である。
 * これは辺③（{@code XlsFormatWriter} 単体）の責務ではなく上位層側の穴であり、本クラスでは埋めない。
 * </p>
 *
 * <p>
 * 異常系は<b>例外になるもの</b>（F3-03・F3-04 の禁止文字・空文字）と<b>例外にならず書けてしまうもの</b>
 * （F3-01・F3-04 の 31 文字超）に分かれる。後者は「書けてしまった結果」をそのまま固定する。
 * ただしこの区別は無条件ではない。POI が切り詰めを禁止文字検査より先に適用するため、
 * <b>禁止文字が index 31 以降にある 32 文字以上のシート名は例外にならず黙って書き出される</b>
 * （{@code #writesSheetNameWhoseForbiddenCharacterIsRemovedByTruncation}）。
 * </p>
 *
 * <p>
 * <b>F3-04 で本クラスが担保する範囲</b>は、31 文字超・禁止文字（{@code / \ ? * [ ] :}）・空文字・
 * 31 文字ちょうど（正常側の境界）・重複判定（切り詰め後の衝突／大文字小文字だけが違う名前）である。
 * シート名のアポストロフィ（先頭／末尾）と {@code null} は
 * タスク #22 のスコープ外であり、<b>未担保</b>である。
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

    /**
     * 出力されるブックの拡張子（{@link XlsFormatWriter} が付ける）。値は {@link XlsFixture#EXTENSION} から取る。
     *
     * <p>
     * 本クラスは期待パスを 2 か所（{@link #writtenBook} と例外メッセージの照合）で組むためこの定数を置くが、
     * <b>拡張子の文字列リテラルを {@code xls} パッケージから一掃したわけではない。</b>
     * {@code grep -rn '"\.xlsx"' src/test/java/nablarch/test/tool/converter/xls/ --include=*.java} は
     * {@link XlsFixture} の定義（1 か所）に加えて<b>使用側 4 か所</b>を返す ─
     * {@code XlsFormatWriterCellTypeTest} L181・L207 ／ {@code XlsFormatReaderCellTypeTest} L200 ／
     * {@code XlsReferenceFixtureTest} L125（この Javadoc 自身は上のコマンドにヒットしない）。
     * さらに {@code XlsFormatWriterTest} L732 は {@code "MyBook.xlsx"} という 1 個のファイル名リテラルとして
     * 拡張子を含んでいる。いずれもブック名と連結して 1 個のパスを作るだけの局所的な用法で、
     * 定数化しても読み手の得が無いため置き換えていない。
     * <b>したがって「定義が 1 か所」ではなく「本クラスの中では 1 か所」である。</b>
     * </p>
     */
    private static final String EXTENSION = XlsFixture.EXTENSION;

    /** Excel がシート名に許す最大文字数。 */
    private static final int EXCEL_MAX_SHEET_NAME_LENGTH = 31;

    /** 出力先。 */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** 権限を落としたディレクトリ（{@link TemporaryFolder} が後片付けできるよう復元する）。 */
    private File permissionDroppedDir;

    /**
     * 権限を落としたディレクトリがあれば書き込み可能へ戻す。
     *
     * <p>
     * 落とすときは {@code setWritable(false, false)}（所有者・グループ・その他すべてから剥奪）だが、
     * 戻すのは所有者ぶんだけでよい。{@link TemporaryFolder} の後片付けはテストプロセス自身
     * （＝所有者）が行うため、所有者に書き込み権限が戻れば削除できる。
     * </p>
     */
    @After
    public void restorePermission() {
        if (permissionDroppedDir != null) {
            permissionDroppedDir.setWritable(true, true);
        }
    }

    // ------------------------------------------------------------------ helpers

    /**
     * セクションごとに 1 ブロックを持つコンテナを組み立てる。
     *
     * @param book       ブック名
     * @param sheetNames シート名（渡した順に 1 セクションずつ）
     * @return コンテナ
     */
    private static TestDataContainer container(String book, String... sheetNames) {
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                Collections.singletonList("C"),
                Collections.singletonList(Collections.singletonList("v")));
        List<TestDataSection> sections = new ArrayList<>();
        for (String sheetName : sheetNames) {
            sections.add(new TestDataSection(sheetName,
                    Collections.<TestDataBlock>singletonList(table)));
        }
        return new TestDataContainer(book, sections);
    }

    /**
     * ブック名から、書き出されるはずのファイルを求める。
     *
     * <p>
     * ブック名を {@code container(...)} 側と {@code new File(...)} 側に別々のリテラルで書くと、
     * 片方だけを書き換えたときに「存在しないファイルを見て {@code assertFalse} が無条件に通る」ため、
     * 名前を 1 箇所に束ねるためのヘルパである。
     * </p>
     *
     * @param dir  出力先ディレクトリ
     * @param book ブック名
     * @return 書き出されるはずのファイル
     */
    private static File writtenBook(File dir, String book) {
        return new File(dir, book + EXTENSION);
    }

    /**
     * ブック名から、既定の出力先（{@link #folder} 直下）に書き出されるはずのファイルを求める。
     *
     * @param book ブック名
     * @return 書き出されるはずのファイル
     */
    private File writtenBook(String book) {
        return writtenBook(folder.getRoot(), book);
    }

    /**
     * Excel が禁じる文字を含むシート名が拒否されることを、1 つのシート名ぶん確かめる。
     *
     * <p>
     * 組み立てるシート名は {@code "a" + 禁止文字 + "b"} の 3 文字である。31 文字以下なので切り詰めは走らず、
     * 禁止文字は必ず index 1 で検査に掛かる（32 文字以上だと切り詰めが先に走り、禁止文字の位置に
     * よっては検査に到達しない。{@code #writesSheetNameWhoseForbiddenCharacterIsRemovedByTruncation}）。
     * </p>
     *
     * <p>
     * メッセージは文字と index まで固定する。対照の
     * {@code #rejectsSheetNameWhoseForbiddenCharacterSurvivesTruncation} が
     * {@code Invalid char (/) found at index (30)} まで固定しているのに合わせ、
     * 「どの文字がどの位置で不正か」をメッセージが示すことを同じ粒度で担保するためである。
     * </p>
     *
     * @param forbidden Excel がシート名に禁じる文字
     */
    private void assertRejectsSheetName(char forbidden) {
        // Given
        String book = "Forbidden";
        String sheetName = "a" + forbidden + "b";

        // When
        IllegalArgumentException thrown = assertThrows(sheetName, IllegalArgumentException.class,
                () -> new XlsFormatWriter().write(container(book, sheetName),
                        folder.getRoot().getAbsolutePath()));

        // Then
        assertThat(sheetName, thrown.getMessage(),
                containsString("Invalid char (" + forbidden + ") found at index (1)"));
        assertThat(sheetName, thrown.getMessage(), containsString("in sheet name '" + sheetName + "'"));
        assertFalse("ブックは作られない: " + sheetName, writtenBook(book).exists());
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
     * <p>
     * <b>拒否のされ方が {@link AccessDeniedException} 以外だった場合もスキップする。</b>
     * 本テストが固定するのは「書き込みを拒否されたときに {@code UncheckedIOException} が
     * {@link AccessDeniedException} を包んで送出される」ことであり、書き込み拒否を汎用の
     * {@code FileSystemException} で返すファイルシステムではその形にならない。
     * そういう環境で ERROR にすると「権限が効かない環境ではスキップする」という約束に反するため、
     * {@link Assume#assumeNoException} で逃がす（緑にごまかすのではなく、実行しなかったことを残す）。
     * </p>
     *
     * @param dir 対象ディレクトリ
     * @return 権限が効いていて {@link AccessDeniedException} で拒否されるなら真。
     *         権限が効いていない（書けてしまう）なら偽。
     *         それ以外の {@link IOException} で拒否された場合は {@link Assume#assumeNoException} により
     *         テストをスキップさせるため、値は返らない
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
        String book = "Missing";
        File missing = new File(folder.getRoot(), "no/such/dir");
        assertFalse("前提: 出力先はまだ存在しない", missing.exists());

        // When
        new XlsFormatWriter().write(container(book, SHEET), missing.getAbsolutePath());

        // Then
        assertTrue("出力先ディレクトリが作られる", missing.isDirectory());
        File written = writtenBook(missing, book);
        assertTrue("ブックが書き出される", written.exists());
        Workbook workbook = XlsFixture.open(written.toPath());
        Sheet sheet = workbook.getSheet(SHEET);
        assertThat("出力先シートがあること", sheet, is(notNullValue()));
        Row row = sheet.getRow(0);
        assertThat("識別行が書かれていること", row, is(notNullValue()));
        Cell cell = row.getCell(0);
        assertThat("識別セルが書かれていること", cell, is(notNullValue()));
        assertThat(cell.getStringCellValue(), is("SETUP_TABLE=T"));
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
        String book = "Denied";
        File readOnly = folder.newFolder("readonly");
        Assume.assumeTrue("書き込み権限が効かない環境ではスキップする（root 実行・権限を無視する FS など）",
                dropWritePermission(readOnly));

        // When
        UncheckedIOException thrown = assertThrows(UncheckedIOException.class,
                () -> new XlsFormatWriter().write(container(book, SHEET), readOnly.getAbsolutePath()));

        // Then
        assertThat(thrown.getMessage(), containsString("failed to write Excel:"));
        assertThat("どのファイルを書けなかったかが分かる",
                thrown.getMessage(), containsString(book + EXTENSION));
        assertThat(thrown.getCause(), is(instanceOf(AccessDeniedException.class)));
        assertFalse("ファイルは作られない", writtenBook(readOnly, book).exists());
    }

    // ------------------------------------------------------------------ F3-04 シート名が Excel 制約違反

    /*
     * 禁止文字は文字ごとに 1 メソッドへ分けてある（姉妹クラス XlsFormatReaderCellTypeTest が
     * 1 ケース 1 @Test で展開しているのに合わせた）。ループで束ねると最初の 1 文字が落ちた時点で
     * 残りが実行されず、どの文字で挙動が違うのかが分からなくなるためである。
     *
     * 以下 7 件（/ \ ? * [ ] :）はいずれも Excel がシート名に禁じる文字である。
     * write すると POI の IllegalArgumentException が送出され、どの文字がどの位置で不正かが
     * メッセージに入る。ブックは作られない。検査は POI の WorkbookUtil が行う。
     * 担保する軸要素: F3-04（禁止文字）。
     */

    /**
     * Given: 禁止文字 {@code /} を index 1 に持つ 3 文字のシート名 {@code a/b}。
     * When : {@code write}。
     * Then : POI の {@code IllegalArgumentException} が
     *        {@code Invalid char (/) found at index (1)} と {@code in sheet name 'a/b'} を伴って送出され、
     *        ブックは作られない。
     *
     * <p>担保する軸要素: F3-04（禁止文字）。</p>
     */
    @Test
    public void rejectsSheetNameContainingSlash() {
        assertRejectsSheetName('/');
    }

    /**
     * Given: 禁止文字 {@code \} を index 1 に持つ 3 文字のシート名 {@code a\b}。
     * When : {@code write}。
     * Then : POI の {@code IllegalArgumentException} が
     *        {@code Invalid char (\) found at index (1)} と {@code in sheet name 'a\b'} を伴って送出され、
     *        ブックは作られない。
     *
     * <p>担保する軸要素: F3-04（禁止文字）。</p>
     */
    @Test
    public void rejectsSheetNameContainingBackslash() {
        assertRejectsSheetName('\\');
    }

    /**
     * Given: 禁止文字 {@code ?} を index 1 に持つ 3 文字のシート名 {@code a?b}。
     * When : {@code write}。
     * Then : POI の {@code IllegalArgumentException} が
     *        {@code Invalid char (?) found at index (1)} と {@code in sheet name 'a?b'} を伴って送出され、
     *        ブックは作られない。
     *
     * <p>担保する軸要素: F3-04（禁止文字）。</p>
     */
    @Test
    public void rejectsSheetNameContainingQuestionMark() {
        assertRejectsSheetName('?');
    }

    /**
     * Given: 禁止文字 {@code *} を index 1 に持つ 3 文字のシート名 {@code a*b}。
     * When : {@code write}。
     * Then : POI の {@code IllegalArgumentException} が
     *        {@code Invalid char (*) found at index (1)} と {@code in sheet name 'a*b'} を伴って送出され、
     *        ブックは作られない。
     *
     * <p>担保する軸要素: F3-04（禁止文字）。</p>
     */
    @Test
    public void rejectsSheetNameContainingAsterisk() {
        assertRejectsSheetName('*');
    }

    /**
     * Given: 禁止文字 {@code [} を index 1 に持つ 3 文字のシート名 {@code a[b}。
     * When : {@code write}。
     * Then : POI の {@code IllegalArgumentException} が
     *        {@code Invalid char ([) found at index (1)} と {@code in sheet name 'a[b'} を伴って送出され、
     *        ブックは作られない。
     *
     * <p>担保する軸要素: F3-04（禁止文字）。</p>
     */
    @Test
    public void rejectsSheetNameContainingOpeningBracket() {
        assertRejectsSheetName('[');
    }

    /**
     * Given: 禁止文字 {@code ]} を index 1 に持つ 3 文字のシート名 {@code a]b}。
     * When : {@code write}。
     * Then : POI の {@code IllegalArgumentException} が
     *        {@code Invalid char (]) found at index (1)} と {@code in sheet name 'a]b'} を伴って送出され、
     *        ブックは作られない。
     *
     * <p>担保する軸要素: F3-04（禁止文字）。</p>
     */
    @Test
    public void rejectsSheetNameContainingClosingBracket() {
        assertRejectsSheetName(']');
    }

    /**
     * Given: 禁止文字 {@code :} を index 1 に持つ 3 文字のシート名 {@code a:b}。
     * When : {@code write}。
     * Then : POI の {@code IllegalArgumentException} が
     *        {@code Invalid char (:) found at index (1)} と {@code in sheet name 'a:b'} を伴って送出され、
     *        ブックは作られない。
     *
     * <p>担保する軸要素: F3-04（禁止文字）。</p>
     */
    @Test
    public void rejectsSheetNameContainingColon() {
        assertRejectsSheetName(':');
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
        // Given
        String book = "Empty";

        // When
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new XlsFormatWriter().write(container(book, ""),
                        folder.getRoot().getAbsolutePath()));

        // Then
        assertThat(thrown.getMessage(), containsString("sheetName '' is invalid"));
        assertFalse("ブックは作られない", writtenBook(book).exists());
    }

    /**
     * Given: Excel の上限ちょうど（31 文字）のセクション名。
     * When : {@code write}。
     * Then : 例外にならず、シート名が<b>そのまま</b>書き出される（切り詰められない）。
     *        元の名前でシートを引ける。
     *
     * <p>
     * 担保する軸要素: F3-04（文字数の上限の正常側の境界）。超過側
     * （{@code #truncatesSheetNameLongerThanExcelLimitSilently}）と対にして、
     * 切り詰めが起きる境界が 31／32 のどちらであるかを固定する。
     * </p>
     */
    @Test
    public void writesSheetNameOfExcelLimitLengthAsIs() {
        // Given
        String book = "AtLimit";
        String atLimit = "a".repeat(EXCEL_MAX_SHEET_NAME_LENGTH);

        // When
        new XlsFormatWriter().write(container(book, atLimit), folder.getRoot().getAbsolutePath());

        // Then
        Workbook workbook = XlsFixture.open(writtenBook(book).toPath());
        assertThat("シートは 1 枚だけ", workbook.getNumberOfSheets(), is(1));
        assertThat("31 文字はそのまま保たれる", workbook.getSheetName(0), is(atLimit));
        assertThat("元のセクション名でシートを引ける", workbook.getSheet(atLimit), is(notNullValue()));
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
        String book = "TooLong";
        String tooLong = "a".repeat(EXCEL_MAX_SHEET_NAME_LENGTH + 1);

        // When
        String inMemorySheetName = new XlsFormatWriter().build(container(book, tooLong)).getSheetName(0);
        new XlsFormatWriter().write(container(book, tooLong), folder.getRoot().getAbsolutePath());

        // Then
        assertThat("切り詰めはメモリ上のブックの時点で起きている",
                inMemorySheetName.length(), is(EXCEL_MAX_SHEET_NAME_LENGTH));
        Workbook workbook = XlsFixture.open(writtenBook(book).toPath());
        assertThat("シートは 1 枚だけ", workbook.getNumberOfSheets(), is(1));
        assertThat("31 文字へ切り詰められる（issues.md XLS-16）",
                workbook.getSheetName(0), is("a".repeat(EXCEL_MAX_SHEET_NAME_LENGTH)));
        assertThat("元のセクション名ではシートを引けない", workbook.getSheet(tooLong), is(nullValue()));
        // 変換ツール自身の読み戻しも元の名前では失敗する
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new XlsFormatReader().read(folder.getRoot().getAbsolutePath(),
                        book + "/" + tooLong));
        assertThat(thrown.getMessage(), containsString("sheet not found."));
    }

    /**
     * Given: 32 文字目（index 31）に Excel の禁止文字 {@code /} を置いた 32 文字のセクション名。
     * When : {@code write}。
     * Then : <b>例外にならず</b>、禁止文字が切り詰めで消えたシート名（{@code a} × 31）のブックが
     *        黙って書き出される。
     *
     * <p>
     * 担保する軸要素: F3-04（切り詰めと禁止文字検査の順序）。POI 3.8 の
     * {@code XSSFWorkbook#createSheet(String)} は {@code substring(0, 31)} による切り詰めを
     * {@code WorkbookUtil.validateSheetName} <b>より先に</b>適用する。したがって
     * 「禁止文字を含むシート名は必ず例外になる」とは言えず、<b>禁止文字が index 31 以降にある
     * 32 文字以上の名前では検査に到達せず黙って書き出される</b>。
     * 検査そのものは効いていること（切り詰め後の名前に禁止文字が残れば失敗すること）は
     * {@code #rejectsSheetNameWhoseForbiddenCharacterSurvivesTruncation} が示す。
     * {@code issues.md} の <b>XLS-16</b> の帰結の一つである。
     * </p>
     */
    @Test
    public void writesSheetNameWhoseForbiddenCharacterIsRemovedByTruncation() {
        // Given
        String book = "Hidden";
        String hidden = "a".repeat(EXCEL_MAX_SHEET_NAME_LENGTH) + "/";

        // When
        new XlsFormatWriter().write(container(book, hidden), folder.getRoot().getAbsolutePath());

        // Then
        File written = writtenBook(book);
        assertTrue("禁止文字を含む名前なのにブックが書かれる（issues.md XLS-16）", written.exists());
        Workbook workbook = XlsFixture.open(written.toPath());
        assertThat("シートは 1 枚だけ", workbook.getNumberOfSheets(), is(1));
        assertThat("禁止文字は切り詰めで消え、検査に到達しない",
                workbook.getSheetName(0), is("a".repeat(EXCEL_MAX_SHEET_NAME_LENGTH)));
    }

    /**
     * Given: 31 文字目（index 30）に Excel の禁止文字 {@code /} を置いた 32 文字のセクション名。
     * When : {@code write}。
     * Then : {@code IllegalArgumentException} で失敗する。メッセージが示すシート名は
     *        <b>切り詰め後の 31 文字</b>であり、渡した 32 文字ではない。
     *
     * <p>
     * 担保する軸要素: F3-04（切り詰めと禁止文字検査の順序）。
     * {@code #writesSheetNameWhoseForbiddenCharacterIsRemovedByTruncation} の対照であり、
     * 「切り詰めてから検査する」という順序を、例外メッセージに現れる名前で裏づける。
     * </p>
     */
    @Test
    public void rejectsSheetNameWhoseForbiddenCharacterSurvivesTruncation() {
        // Given
        String book = "Surviving";
        String surviving = "a".repeat(EXCEL_MAX_SHEET_NAME_LENGTH - 1) + "/a";

        // When
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new XlsFormatWriter().write(container(book, surviving),
                        folder.getRoot().getAbsolutePath()));

        // Then
        assertThat(thrown.getMessage(), containsString("Invalid char (/) found at index (30)"));
        assertThat("メッセージのシート名は切り詰め後の 31 文字（＝検査は切り詰めの後に走る）",
                thrown.getMessage(),
                containsString("in sheet name '" + "a".repeat(EXCEL_MAX_SHEET_NAME_LENGTH - 1) + "/'"));
        assertFalse("ブックは作られない", writtenBook(book).exists());
    }

    /**
     * Given: 先頭 31 文字が同じで 32 文字目だけが違う 2 つのセクション名。
     * When : {@code write}。
     * Then : 切り詰めた結果が衝突し、{@code IllegalArgumentException} で失敗する。
     *        <b>ブックは作られない。</b>
     *
     * <p>
     * 担保する軸要素: F3-04（切り詰めの帰結）。切り詰めそのものは黙って起こるが、
     * 衝突した場合だけは失敗する（{@code issues.md} XLS-16 の境界）。
     * </p>
     *
     * <p>
     * 1 枚目のシートは作成済みで例外は 2 枚目で出るため、ファイルが書かれないことは自明ではない。
     * 実行して観測した結果、ファイルは作られなかった。{@link XlsFormatWriter#write} が
     * ブックをすべてメモリ上に組み立ててから出力ストリームを開くため、途中で失敗すると
     * 半端なブックが残らない（出力先ディレクトリは {@code Files.createDirectories} で先に作られる）。
     * </p>
     */
    @Test
    public void failsWhenTruncatedSheetNamesCollide() {
        // Given
        String book = "Collide";
        String base = "a".repeat(EXCEL_MAX_SHEET_NAME_LENGTH);

        // When
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new XlsFormatWriter().write(container(book, base + "1", base + "2"),
                        folder.getRoot().getAbsolutePath()));

        // Then
        assertThat(thrown.getMessage(), containsString("already contains a sheet of this name"));
        assertFalse("ブックは作られない（1 枚目のシートは作成済みだがファイルは残らない）",
                writtenBook(book).exists());
    }

    /**
     * Given: 大文字小文字だけが違う 2 つのセクション名（{@code abc} と {@code ABC}）。どちらも 31 文字以下。
     * When : {@code write}。
     * Then : 重複と判定され、{@code IllegalArgumentException} で失敗する。<b>ブックは作られない。</b>
     *
     * <p>
     * 担保する軸要素: F3-04（シート名の重複判定）。{@code issues.md} <b>XLS-16</b> は原因として
     * POI 3.8 の {@code Workbook#containsSheet} が「両辺を 31 文字へ切り詰めてから
     * {@code equalsIgnoreCase} で比べる」ことを挙げている。
     * {@code #failsWhenTruncatedSheetNamesCollide} が担保するのは<b>切り詰め</b>側だけなので、
     * 本メソッドが<b>大文字小文字を区別しない</b>側を担保する。
     * シート名は 3 文字で切り詰めが走らないため、衝突の理由が {@code equalsIgnoreCase} だけに絞られる。
     * </p>
     *
     * <p>
     * これは Excel 自身の制約（Excel もシート名の大文字小文字を区別しない）と一致するため、
     * <b>課題ではなく妥当な挙動</b>として記録している（{@code issues.md} の「課題としないと判断した観測結果」）。
     * 固定する意味は、XLS-16 が原因として引用している機構の両輪を実測で押さえておくことにある。
     * </p>
     */
    @Test
    public void failsWhenSheetNamesDifferOnlyInCase() {
        // Given
        String book = "CaseCollide";

        // When
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new XlsFormatWriter().write(container(book, "abc", "ABC"),
                        folder.getRoot().getAbsolutePath()));

        // Then
        assertThat("大文字小文字だけが違う名前も同名と判定される",
                thrown.getMessage(), containsString("already contains a sheet of this name"));
        assertFalse("ブックは作られない", writtenBook(book).exists());
    }
}
