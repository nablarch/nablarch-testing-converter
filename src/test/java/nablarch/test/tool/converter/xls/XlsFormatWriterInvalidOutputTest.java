package nablarch.test.tool.converter.xls;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
 * 異常系は<b>例外になるもの</b>（F3-03・F3-04 の禁止文字・空文字・31 文字超）と
 * <b>例外にならず書けてしまうもの</b>（F3-01）に分かれる。後者は「書けてしまった結果」をそのまま固定する。
 * シート名の 31 文字超は、かつて POI が黙って切り詰めていた（切り詰めを禁止文字検査より先に適用するため、
 * 禁止文字が index 31 以降にある 32 文字以上の名前は検査に到達しなかった）が、
 * {@code issues.md} <b>XLS-16</b> の修正で {@link XlsFormatWriter} が {@code createSheet} の前に
 * 文字数を検査するようになり、いずれも例外になる
 * （{@code #rejectsSheetNameLongerThanExcelLimit}／
 * {@code #rejectsSheetNameWhoseForbiddenCharacterWouldBeRemovedByTruncation}）。
 * </p>
 *
 * <p>
 * <b>F3-04 で本クラスが担保する範囲</b>は、31 文字超・禁止文字（{@code / \ ? * [ ] :}）・空文字・
 * 31 文字ちょうど（正常側の境界）・重複判定（同名／大文字小文字だけが違う名前）である。
 * シート名のアポストロフィ（先頭／末尾）はタスク #22 のスコープ外であり、<b>未担保</b>である。
 * {@code null} は<b>本クラスの担保対象ではない</b> —— {@link nablarch.test.tool.converter.model.TestDataSection}
 * が生成時に拒否するため辺③へ届かず、担保は
 * {@code TestDataContainerTest#名前がnullの読み込み単位は生成できない} にある
 * （{@code issues.md} <b>XLS-33</b>）。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>
 * ただしシート名の文字数上限だけは例外で、{@code issues.md} の課題（XLS-16）として
 * {@link XlsFormatWriter} を修正したうえで、仕様どおりの期待値を置いている。
 * </p>
 *
 * @author kiyobot
 */
public class XlsFormatWriterInvalidOutputTest {

    /** 既定のシート名。 */
    private static final String SHEET = "s";

    /** {@link XlsFormatWriter} が付ける拡張子。 */
    private static final String EXTENSION = ".xlsx";

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
     * よっては検査に到達しない。{@code #rejectsSheetNameWhoseForbiddenCharacterWouldBeRemovedByTruncation}）。
     * </p>
     *
     * <p>
     * メッセージは文字と index まで固定する。対照の
     * {@code #rejectsSheetNameWhoseForbiddenCharacterIsAtTheLastPosition} が
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
     * （{@code #rejectsSheetNameLongerThanExcelLimit}）と対にして、
     * 許容と拒否の境界が 31／32 のどちらであるかを固定する。
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
     * Then : {@code IllegalArgumentException} で失敗する。メッセージは渡された名前と文字数を示す。
     *        <b>ブックは作られない。</b>
     *
     * <p>
     * シート名は呼び出し側が渡す引き当てキーである（{@code testdata_notation.rst:590}
     * 「読み込み単位の名前（Excel 形式ではシート名、YAML 形式ではファイル名）と ID を指定して
     * List 形式または Map 形式でデータを取得できる」、
     * 続く {@code TestSupport#getListMap(String sheetName, String id)}）。
     * 黙って別名へ切り詰めると呼び出し側から引けなくなるため、切り詰めずに落とす
     * （{@code issues.md} <b>XLS-16</b>）。
     * </p>
     *
     * <p>担保する軸要素: F3-04（文字数の上限）。正常側の境界は
     * {@code #writesSheetNameOfExcelLimitLengthAsIs} が担保する。</p>
     */
    @Test
    public void rejectsSheetNameLongerThanExcelLimit() {
        // Given
        String book = "TooLong";
        String tooLong = "a".repeat(EXCEL_MAX_SHEET_NAME_LENGTH + 1);

        // When
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new XlsFormatWriter().write(container(book, tooLong),
                        folder.getRoot().getAbsolutePath()));

        // Then
        assertThat(thrown.getMessage(),
                containsString("シート名が Excel の上限 31 文字を超えています"));
        assertThat(thrown.getMessage(), containsString("sheetName='" + tooLong + "'"));
        assertThat(thrown.getMessage(), containsString("length=32"));
        assertFalse("ブックは作られない", writtenBook(book).exists());
    }

    /**
     * Given: 32 文字目（index 31）に Excel の禁止文字 {@code /} を置いた 32 文字のセクション名。
     * When : {@code write}。
     * Then : {@code IllegalArgumentException}（文字数超過）で失敗する。<b>ブックは作られない。</b>
     *
     * <p>
     * 担保する軸要素: F3-04（切り詰めが禁止文字検査を無効化する抜けが閉じていること）。
     * POI 3.8 の {@code XSSFWorkbook#createSheet(String)} は {@code substring(0, 31)} による
     * 切り詰めを {@code WorkbookUtil.validateSheetName} <b>より先に</b>適用するため、
     * 禁止文字が index 31 以降にある 32 文字以上の名前は検査に到達せず黙って書き出されていた
     * （{@code issues.md} <b>XLS-16</b>）。{@link XlsFormatWriter} が {@code createSheet} の前に
     * 文字数を検査するようにしたため、この形は {@code createSheet} へ渡る前に落ちる。
     * </p>
     */
    @Test
    public void rejectsSheetNameWhoseForbiddenCharacterWouldBeRemovedByTruncation() {
        // Given
        String book = "Hidden";
        String hidden = "a".repeat(EXCEL_MAX_SHEET_NAME_LENGTH) + "/";

        // When
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new XlsFormatWriter().write(container(book, hidden),
                        folder.getRoot().getAbsolutePath()));

        // Then
        assertThat("禁止文字が切り詰めで消える前に、文字数で落ちる",
                thrown.getMessage(), containsString("シート名が Excel の上限 31 文字を超えています"));
        assertThat(thrown.getMessage(), containsString("sheetName='" + hidden + "'"));
        assertFalse("ブックは作られない", writtenBook(book).exists());
    }

    /**
     * Given: 31 文字目（index 30）に Excel の禁止文字 {@code /} を置いた<b>31 文字</b>のセクション名。
     * When : {@code write}。
     * Then : {@code IllegalArgumentException} で失敗する。メッセージは文字と位置を示す。
     *
     * <p>
     * 担保する軸要素: F3-04（禁止文字の検査が上限ちょうどの長さでも効くこと）。
     * {@code #rejectsSheetNameWhoseForbiddenCharacterWouldBeRemovedByTruncation} の対照であり、
     * 文字数検査を通る名前では POI の禁止文字検査に到達することを示す。
     * 文字数が 31 なので切り詰めは起こらず、メッセージのシート名は渡した名前そのものである。
     * </p>
     */
    @Test
    public void rejectsSheetNameWhoseForbiddenCharacterIsAtTheLastPosition() {
        // Given
        String book = "Surviving";
        String surviving = "a".repeat(EXCEL_MAX_SHEET_NAME_LENGTH - 1) + "/";

        // When
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new XlsFormatWriter().write(container(book, surviving),
                        folder.getRoot().getAbsolutePath()));

        // Then
        assertThat(thrown.getMessage(), containsString("Invalid char (/) found at index (30)"));
        assertThat("メッセージのシート名は渡した名前そのもの（切り詰めは起きない）",
                thrown.getMessage(), containsString("in sheet name '" + surviving + "'"));
        assertFalse("ブックは作られない", writtenBook(book).exists());
    }

    /**
     * Given: まったく同じ 31 文字のセクション名 2 つ。
     * When : {@code write}。
     * Then : 重複と判定され、{@code IllegalArgumentException} で失敗する。
     *        <b>ブックは作られない。</b>
     *
     * <p>
     * 担保する軸要素: F3-04（シート名の重複判定）。
     * 元は「先頭 31 文字が同じで 32 文字目だけが違う 2 名が切り詰めで衝突する」ことを固定していたが、
     * 31 文字超を {@link XlsFormatWriter} が拒否するようになった（{@code issues.md} <b>XLS-16</b>）ため
     * その前提が無くなった。上限ちょうどの長さでの重複検出として書き直してある。
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
    public void failsWhenSameSheetNameOfLimitLengthIsUsedTwice() {
        // Given
        String book = "Collide";
        String atLimit = "a".repeat(EXCEL_MAX_SHEET_NAME_LENGTH);

        // When
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new XlsFormatWriter().write(container(book, atLimit, atLimit),
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
     * {@code #failsWhenSameSheetNameOfLimitLengthIsUsedTwice} が担保するのは<b>完全同名</b>だけなので、
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
