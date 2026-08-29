package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.blank;
import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import nablarch.test.core.file.DataFile;
import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.FrameworkOracle;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.MessageDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * ファイル・電文の末尾に連続して {@code null} 記法を書いたとき、中間モデルへ入る値が
 * <b>テスティングフレームワーク本体が読む値と一致する</b>ことを、実 {@code .xlsx} を入力に固定する。
 *
 * <p>
 * 末尾のフィールドに {@code null} と書いた場合、値は形式によらず空文字になる。フレームワークは
 * 全セルを先に解釈してから構造解析し、解釈後に空（Java {@code null} または空文字）になった値を
 * レコードの末尾から連続して取り除いたうえで、フィールド名の数まで空文字で埋め直すためである。
 * </p>
 *
 * <p>
 * <b>期待値は本テストが書くのではなく、{@link FrameworkOracle} が本体に同じ {@code .xlsx} を読ませて
 * 取り出す。</b>変換ツールと期待値が同じ写し間違いを持つと検知できないため
 * （変換ツール自身の読みどうしを突き合わせていた 4 経路テストで実際に起きた）。
 * 本体の値そのものも各テストで明示し、本体側が黙って変わったときに気づけるようにする。
 * </p>
 *
 * <p>
 * 入力の 5 形（ファイル 3 形・電文 1 形・送信同期電文 1 形）は、末尾の {@code null} が
 * 空文字にならないという不具合を実測で特定したときの入力と同じものである。
 * </p>
 */
public class XlsTrailingNullTest {

    /** フィクスチャの既定ブック名。 */
    private static final String BOOK = "trailingNullBook";

    /** フィクスチャの既定シート名。 */
    private static final String SHEET = "sheet1";

    /** テストごとに独立した出力先。 */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // ------------------------------------------------------------------ helpers

    /**
     * 既定のブック名・シート名でフィクスチャの組み立てを開始する。
     *
     * @return フィクスチャビルダ
     */
    private static XlsFixture book() {
        return XlsFixture.book(BOOK).sheet(SHEET);
    }

    /**
     * フィクスチャ {@code .xlsx} の出力先ディレクトリ。
     *
     * @return ディレクトリ
     */
    private Path dir() {
        return folder.getRoot().toPath();
    }

    /**
     * 既定のブック／シートのリソース名。
     *
     * @return リソース名
     */
    private static String resource() {
        return BOOK + "/" + SHEET;
    }

    /**
     * 既定のブック／シートを変換ツールで読み、唯一のブロックを返す。
     *
     * @param type 期待するブロックの実装クラス
     * @param <T>  ブロックの型
     * @return ブロック
     */
    private <T extends TestDataBlock> T onlyBlock(Class<T> type) {
        TestDataContainer container = new XlsFormatReader().read(dir().toString(), resource());
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        assertThat("ブロック数", blocks.size(), is(1));
        return type.cast(blocks.get(0));
    }

    /**
     * ブロックの唯一のレコードレイアウトの値行を返す。
     *
     * @param records レコードレイアウト一覧
     * @return 値行
     */
    private static List<List<String>> onlyRecordRows(List<RecordLayout> records) {
        assertThat("レコードレイアウト数", records.size(), is(1));
        return records.get(0).getRows();
    }

    /**
     * 本体が読んだ唯一のファイルの値行を返す。
     *
     * @param files 本体が読んだファイル一覧
     * @return 値行
     */
    private static List<List<String>> onlyFileRows(List<? extends DataFile> files) {
        assertThat("本体が読んだファイル数", files.size(), is(1));
        return FrameworkOracle.recordValues(files.get(0));
    }

    /**
     * 3 フィールドの固定長ファイル 1 ブロックを、値行 1 行だけ差し替えて組み立てる。
     *
     * @param values 値行の 3 セル（{@code null} はセルを置かない）
     */
    private void fixedFileWith(String... values) {
        book().row(text("SETUP_FIXED=f.dat"))
                .row(text("text-encoding"), text("UTF-8"))
                .row(text("data"), text("f1"), text("f2"), text("f3"))
                .row(blank(), text("半角英字"), text("半角英字"), text("半角英字"))
                .row(blank(), text("5"), text("5"), text("5"))
                .row(blank(), text(values[0]), text(values[1]), text(values[2]))
                .writeTo(dir());
    }

    // ------------------------------------------------------------------ ファイル

    /**
     * Given: 固定長ファイルの値行が {@code x}／{@code null}／{@code null}（末尾 2 つが {@code null} 記法）。
     * When : 実 {@code .xlsx} を変換ツールと本体それぞれに読ませる。
     * Then : 変換ツールの値が本体の値と一致し、末尾の {@code null} は空文字になる。
     */
    @Test
    public void readsTrailingNullsAsEmptyStringInFixedFile() {
        // Given
        fixedFileWith("x", "null", "null");

        // When
        List<List<String>> actual = onlyRecordRows(onlyBlock(FileDataBlock.class).getRecords());
        List<List<String>> oracle =
                onlyFileRows(FrameworkOracle.files(dir().toString(), resource(), "", DataType.SETUP_FIXED));

        // Then
        assertThat("本体の値", oracle, is(Arrays.asList(Arrays.asList("x", "", ""))));
        assertThat("変換ツールの値が本体と一致する", actual, is(oracle));
    }

    /**
     * Given: 固定長ファイルの値行が全て {@code null} 記法。
     * When : 実 {@code .xlsx} を変換ツールと本体それぞれに読ませる。
     * Then : 変換ツールの値が本体の値と一致し、全フィールドが空文字になる。
     */
    @Test
    public void readsAllNullRowAsAllEmptyStringsInFixedFile() {
        // Given
        fixedFileWith("null", "null", "null");

        // When
        List<List<String>> actual = onlyRecordRows(onlyBlock(FileDataBlock.class).getRecords());
        List<List<String>> oracle =
                onlyFileRows(FrameworkOracle.files(dir().toString(), resource(), "", DataType.SETUP_FIXED));

        // Then
        assertThat("本体の値", oracle, is(Arrays.asList(Arrays.asList("", "", ""))));
        assertThat("変換ツールの値が本体と一致する", actual, is(oracle));
    }

    /**
     * Given: 固定長ファイルの値行が {@code x}／{@code ""}（空文字記法）／{@code null}。
     * When : 実 {@code .xlsx} を変換ツールと本体それぞれに読ませる。
     * Then : 変換ツールの値が本体の値と一致し、末尾の {@code null} は空文字になる。
     *        空文字記法が末尾寄りにあっても値の並びは変わらない。
     */
    @Test
    public void readsTrailingNullAfterQuotedEmptyAsEmptyStringInFixedFile() {
        // Given
        fixedFileWith("x", "\"\"", "null");

        // When
        List<List<String>> actual = onlyRecordRows(onlyBlock(FileDataBlock.class).getRecords());
        List<List<String>> oracle =
                onlyFileRows(FrameworkOracle.files(dir().toString(), resource(), "", DataType.SETUP_FIXED));

        // Then
        assertThat("本体の値", oracle, is(Arrays.asList(Arrays.asList("x", "", ""))));
        assertThat("変換ツールの値が本体と一致する", actual, is(oracle));
    }

    // ------------------------------------------------------------------ 電文

    /**
     * Given: 電文（{@code MESSAGE}）の値行が {@code 1}（{@code no} 列）／{@code x}／{@code null}／{@code null}。
     * When : 実 {@code .xlsx} を変換ツールと本体それぞれに読ませる。
     * Then : 変換ツールの値が本体の値と一致し、末尾の {@code null} は空文字になる。
     */
    @Test
    public void readsTrailingNullsAsEmptyStringInMessage() {
        // Given
        book().row(text("MESSAGE=m1"))
                .row(text("text-encoding"), text("UTF-8"))
                .row(text("requestId"), text("R1"))
                .row(text("no"), text("f1"), text("f2"), text("f3"))
                .row(blank(), text("半角英字"), text("半角英字"), text("半角英字"))
                .row(blank(), text("5"), text("5"), text("5"))
                .row(text("1"), text("x"), text("null"), text("null"))
                .writeTo(dir());

        // When
        List<List<String>> actual = onlyRecordRows(onlyBlock(MessageDataBlock.class).getRecords());
        List<List<String>> oracle =
                onlyFileRows(FrameworkOracle.messageBodies(dir().toString(), resource(), "m1"));

        // Then
        assertThat("本体の値", oracle, is(Arrays.asList(Arrays.asList("x", "", ""))));
        assertThat("変換ツールの値が本体と一致する", actual, is(oracle));
    }

    // ------------------------------------------------------------------ 送信同期電文

    /**
     * Given: 送信同期電文（要求本文）の値行が {@code 2}（{@code no} 列）／{@code x}／{@code null}／{@code null}。
     * When : 実 {@code .xlsx} を変換ツールと本体それぞれに読ませる。
     * Then : 変換ツールの値が本体の値と一致し、末尾の {@code null} は空文字になる。
     */
    @Test
    public void readsTrailingNullsAsEmptyStringInSendSyncMessage() {
        // Given
        book().row(text("EXPECTED_REQUEST_BODY_MESSAGES[case1]=RM01"))
                .row(text("text-encoding"), text("UTF-8"))
                .row(text("no"), text("f1"), text("f2"), text("f3"))
                .row(blank(), text("半角英字"), text("半角英字"), text("半角英字"))
                .row(blank(), text("5"), text("5"), text("5"))
                .row(text("2"), text("x"), text("null"), text("null"))
                .writeTo(dir());

        // When
        List<List<String>> actual = onlyRecordRows(onlyBlock(MessageDataBlock.class).getRecords());
        List<List<String>> oracle = onlyFileRows(FrameworkOracle.sendSyncBodies(
                dir().toString(), resource(), "[case1]", DataType.EXPECTED_REQUEST_BODY_MESSAGES));

        // Then
        assertThat("本体の値", oracle, is(Arrays.asList(Arrays.asList("x", "", ""))));
        assertThat("変換ツールの値が本体と一致する", actual, is(oracle));
    }
}
