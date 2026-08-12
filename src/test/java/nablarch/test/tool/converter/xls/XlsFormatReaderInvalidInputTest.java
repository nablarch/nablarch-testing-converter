package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.blank;
import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import nablarch.test.core.reader.DataType;
import nablarch.test.tool.converter.model.FieldDef;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.MessageDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 辺①（Excel→中間モデル）の軸F（異常系）を、<b>実 {@code .xlsx} を入力に</b>固定するテスト。
 *
 * <p>
 * 対象は {@code .rn/ntf-test-data-converter/coverage/inventory.md} §0.7 の辺① 6 ケース
 * （F1-01 シート不在／F1-02 ブック破損／F1-03 未知のデータタイプ名／F1-04 マーカーカラム欠落／
 * F1-05 カラム名重複／F1-06 行と列の数の不一致）である。F1-05 はタスク #16 が
 * {@code XlsFormatReaderTest} の 4 件で担保しているが、それらは Fake リーダ経路であるため、
 * 本クラスで<b>実 {@code .xlsx} 経路の担保を別に置く</b>（軸E の E-3(複数) と同じ基準）。
 * </p>
 *
 * <p>
 * 軸F のほかに、次の 2 種類の「到達不能の根拠」も本クラスが持つ。いずれも「壊れた入力を流すと
 * 何が起きるか」を確かめるものであり、性質が軸F と同じためここに置く。
 * </p>
 * <ul>
 *   <li>軸C の C-17（{@code RecordLayout.fields} 空）・C-20（{@code FieldDef.type} 省略＝{@code null}）。
 *       #20 が実 {@code .xlsx} 経路で到達不能と判定したが根拠は散文の記述しか無く、パーサの挙動が
 *       変わって到達可能になっても検出できない状態だった。下記 4 メソッドが例外型とメッセージを固定する
 *       （{@code issues.md} の「到達不能」表から参照している）。
 *       <ul>
 *         <li>{@link #failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook} — C-17 の根拠（ファイル系）</li>
 *         <li>{@link #failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook} — C-17 の根拠（メッセージ系）</li>
 *         <li>{@link #failsWhenTypeRowIsShorterThanNameRowInRealBook} — C-20 の根拠①</li>
 *         <li>{@link #failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook} — C-20 の根拠②</li>
 *       </ul>
 *   </li>
 *   <li>軸E の E-3（ファイル内レコードレイアウト数）「複数」が<b>メッセージ経路では到達不能</b>である根拠
 *       （{@link #absorbsSecondNameRowAsDataRowInMessageBodyInRealBook}）。ファイル経路の担保は
 *       {@code XlsFormatReaderRealFileTest#readsMultipleRecordLayoutsFromOneFixedFileInRealBook} にある。</li>
 * </ul>
 *
 * <p>
 * 正常系（軸A・軸B・軸C・軸E）を扱う {@link XlsFormatReaderRealFileTest} と分けたのは、本クラスの
 * アサーション対象が<b>例外型・例外メッセージ・例外連鎖・警告ログの有無</b>という別種のものであり、
 * 入力も意図的に壊したものだけを置くためである。入力の組み立て（{@link XlsFixture}）と実 {@code .xlsx} を
 * 本番配線の {@link XlsFormatReader}（{@code PoiXlsReader}）へ渡す方針は同じである。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>期待される仕様ではない。
 * 妥当でないと判断した挙動は {@code .rn/ntf-test-data-converter/coverage/issues.md} に課題（XLS-10〜XLS-15）
 * として記録してあり、実装（src/main）は変更していない。異常系は<b>例外になるもの</b>
 * （F1-01・F1-02・F1-06 の一部）と<b>例外にならず変換が継続するもの</b>（F1-03・F1-04・F1-05・F1-06 の一部）に
 * 分かれ、後者は継続時の結果をそのまま固定している。
 * </p>
 *
 * <p>
 * <b>例外メッセージのアサートについて。</b>環境依存の要素（一時ディレクトリの絶対パス、{@code HashMap}
 * 由来で並び順が変わるマッピング表）を含むメッセージは変わらない部分文字列だけを {@code containsString} で
 * 突き合わせ、環境に依存しない固定文言は完全一致で突き合わせる。
 * </p>
 *
 * <p>
 * <b>警告ログのアサートについて。</b>「継続する異常系では警告すら出ない（検出できない）」ことが
 * {@code issues.md} XLS-10／XLS-12／XLS-13／XLS-15 の主張の核心であるため、{@link #readCapturingWarnings}
 * で {@code java.util.logging} のルートロガーに届く WARNING 以上のレコードを捕捉し、0 件であることを
 * アサートする。捕捉できるのは JUL 経路だけであり、{@code nablarch-testing} 自身のログ基盤
 * （{@code nablarch.core.log}）への出力は対象外である（変換ツール側で JUL を使うのは
 * {@link XlsFormatReader} だけで、そこが唯一の警告の出所である）。
 * </p>
 *
 * @author kiyobot
 */
public class XlsFormatReaderInvalidInputTest {

    /** フィクスチャの既定シート名。 */
    private static final String SHEET = "s";

    /**
     * フィクスチャ {@code .xlsx} の出力先。
     *
     * <p>
     * {@link TemporaryFolder} はテストメソッドごとに別ディレクトリを与えるため、複数のメソッドが同じ
     * ブック名を使ってもファイルパスは衝突しない。
     * </p>
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // ------------------------------------------------------------------ helpers

    /**
     * 既定のシート名でフィクスチャの組み立てを開始する。
     *
     * @param bookName ブック名
     * @return フィクスチャビルダ
     */
    private static XlsFixture book(String bookName) {
        return XlsFixture.book(bookName).sheet(SHEET);
    }

    /**
     * フィクスチャ {@code .xlsx} の出力先ディレクトリ。読み書きとも本メソッドだけを使う。
     *
     * @return ディレクトリ
     */
    private Path dir() {
        return folder.getRoot().toPath();
    }

    /**
     * 指定のブックの既定シートを実 {@link XlsFormatReader} で読む。
     *
     * @param bookName ブック名
     * @return 中間モデル
     */
    private TestDataContainer read(String bookName) {
        return read(bookName, SHEET);
    }

    /**
     * 指定のブック／シートを実 {@link XlsFormatReader} で読む。
     *
     * @param bookName  ブック名
     * @param sheetName シート名
     * @return 中間モデル
     */
    private TestDataContainer read(String bookName, String sheetName) {
        return new XlsFormatReader().read(dir().toString(), bookName + "/" + sheetName);
    }

    /**
     * 指定のブックを読み、読み取り中に出力された WARNING 以上のログと併せて返す。
     *
     * <p>
     * ハンドラは {@code java.util.logging} の<b>ルートロガー</b>へ取り付ける。{@link XlsFormatReader} の
     * ロガーは既定で親へ伝播するため、ルートで捕捉すれば変換ツールが出す警告はすべて拾える
     * （{@code XlsFormatReaderTest} の重複カラムテストは {@link XlsFormatReader} のロガーへ直接取り付けて
     * いるが、本クラスは「どこからも警告が出ない」ことを言うためルートで受ける）。
     * </p>
     *
     * @param bookName ブック名
     * @return 中間モデルと捕捉した警告
     */
    private Reading readCapturingWarnings(String bookName) {
        CapturingHandler handler = new CapturingHandler();
        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(handler);
        try {
            return new Reading(read(bookName), handler.messages);
        } finally {
            rootLogger.removeHandler(handler);
        }
    }

    /**
     * 1 回の読み取りの結果（中間モデルと、その間に出力された WARNING 以上のログ）。
     */
    private static final class Reading {

        /** 読み取った中間モデル。 */
        private final TestDataContainer container;

        /** 読み取り中に出力された WARNING 以上のログメッセージ。 */
        private final List<String> warnings;

        /**
         * コンストラクタ。
         *
         * @param container 読み取った中間モデル
         * @param warnings  捕捉した警告メッセージ
         */
        Reading(TestDataContainer container, List<String> warnings) {
            this.container = container;
            this.warnings = warnings;
        }
    }

    /**
     * WARNING 以上のログレコードを集めるハンドラ。
     */
    private static final class CapturingHandler extends Handler {

        /** 捕捉したメッセージ。 */
        private final List<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                messages.add(record.getMessage());
            }
        }

        @Override
        public void flush() {
            // 収集のみのため何もしない。
        }

        @Override
        public void close() {
            // 収集のみのため何もしない。
        }
    }

    /**
     * 読み取り結果から唯一のセクションのブロック一覧を取り出す。
     *
     * @param container 中間モデル
     * @return ブロック一覧
     */
    private static List<TestDataBlock> blocksOf(TestDataContainer container) {
        assertThat("セクション数", container.getSections().size(), is(1));
        return container.getSections().get(0).getBlocks();
    }

    /**
     * 指定のブックを読み、唯一のセクションのブロック一覧を返す。
     *
     * @param bookName ブック名
     * @return ブロック一覧
     */
    private List<TestDataBlock> blocks(String bookName) {
        return blocksOf(read(bookName));
    }

    /**
     * 読み取り結果の唯一のブロックが期待する実装クラスであることを確かめて返す。
     *
     * <p>
     * 素キャスト（失敗時に {@code ClassCastException} しか出ない）を避け、どのクラスが来たかが
     * 失敗メッセージに出るようにするためのヘルパ。
     * </p>
     *
     * @param <T>       期待する実装クラス
     * @param container 中間モデル
     * @param expected  期待する実装クラス
     * @return 唯一のブロック
     */
    private static <T extends TestDataBlock> T onlyBlock(TestDataContainer container, Class<T> expected) {
        List<TestDataBlock> blocks = blocksOf(container);
        assertThat("ブロック数", blocks.size(), is(1));
        TestDataBlock block = blocks.get(0);
        assertThat("唯一のブロックの実装クラス", block, is(instanceOf(expected)));
        return expected.cast(block);
    }

    /**
     * 指定のブックを読み、唯一のブロックが期待する実装クラスであることを確かめて返す。
     *
     * @param <T>      期待する実装クラス
     * @param bookName ブック名
     * @param expected 期待する実装クラス
     * @return 唯一のブロック
     */
    private <T extends TestDataBlock> T onlyBlock(String bookName, Class<T> expected) {
        return onlyBlock(read(bookName), expected);
    }

    /**
     * ブロック一覧から識別子で 1 件を引き、期待する実装クラスであることを確かめて返す。
     *
     * <p>
     * 素キャストだとブロックが生成されなかったときに無情報の {@code NullPointerException} に、
     * 型違いのときに {@code ClassCastException} だけになるため、{@link #onlyBlock} と同じく
     * 非 null と実装クラスをアサートしてから cast する。
     * </p>
     *
     * @param <T>        期待する実装クラス
     * @param blocks     ブロック一覧
     * @param identifier 識別子
     * @param expected   期待する実装クラス
     * @return 該当ブロック
     */
    private static <T extends TestDataBlock> T blockOf(List<TestDataBlock> blocks, String identifier,
            Class<T> expected) {
        Map<String, TestDataBlock> byIdentifier = new HashMap<>();
        for (TestDataBlock block : blocks) {
            byIdentifier.put(block.getIdentifier(), block);
        }
        TestDataBlock block = byIdentifier.get(identifier);
        assertThat("識別子 " + identifier + " のブロックが生成されていること（実際の識別子: "
                + byIdentifier.keySet() + "）", block, is(notNullValue()));
        assertThat("識別子 " + identifier + " のブロックの実装クラス", block, is(instanceOf(expected)));
        return expected.cast(block);
    }

    /**
     * レコードレイアウトのフィールド名を並び順に取り出す。
     *
     * @param record レコードレイアウト
     * @return フィールド名一覧
     */
    private static List<String> fieldNames(RecordLayout record) {
        List<String> names = new ArrayList<>();
        for (FieldDef field : record.getFields()) {
            names.add(field.getName());
        }
        return names;
    }

    /**
     * 例外連鎖の原因例外を 1 段たどる。
     *
     * @param thrown 例外
     * @return 原因例外
     */
    private static Throwable causeOf(Throwable thrown) {
        assertThat("原因例外が連鎖していること", thrown.getCause(), is(notNullValue()));
        return thrown.getCause();
    }

    /**
     * 例外連鎖に含まれる全メッセージを連結する（どこにも現れない情報を確かめるために使う）。
     *
     * @param thrown 例外
     * @return 連鎖する全例外のメッセージを連結した文字列
     */
    private static String allMessagesOf(Throwable thrown) {
        StringBuilder sb = new StringBuilder();
        for (Throwable e = thrown; e != null && e != e.getCause(); e = e.getCause()) {
            sb.append(e.getClass().getName()).append(": ").append(e.getMessage()).append('\n');
        }
        return sb.toString();
    }

    /**
     * 警告が 1 件も出ていないことをアサートする。
     *
     * @param reading 読み取り結果
     * @param issueId 根拠づける課題 ID（失敗時の手掛かり）
     */
    private static void assertNoWarning(Reading reading, String issueId) {
        assertThat("警告は 1 件も出ない（" + issueId + " の「検出できない」の根拠。実際: " + reading.warnings + "）",
                reading.warnings, is(Collections.<String>emptyList()));
    }

    /**
     * 本体 {@code TestDataParsingTemplate} が包む「{@code can't get data.}」例外であることを確かめ、
     * その原因例外を返す。
     *
     * @param thrown 送出された例外
     * @return 原因例外（本体パーサが実際に投げたもの）
     */
    private static Throwable assertWrappedByCannotGetData(IllegalStateException thrown) {
        assertThat("本体 TestDataParsingTemplate が包む診断メッセージ",
                thrown.getMessage(), containsString("can't get data."));
        return causeOf(thrown);
    }

    // ------------------------------------------------------------------ F1-01 シート不在

    /**
     * Given: {@code SETUP_TABLE} を 1 件持つ実 {@code .xlsx}。
     * When : 存在しないシート名を指定して {@code read}。
     * Then : {@code IllegalArgumentException}（本体 {@code PoiXlsReader#open}）が送出される。
     *        メッセージにはブックの絶対パスと指定したシート名が入る。
     *
     * <p>
     * 担保する軸要素: F1-01。ブックを開く段階の失敗であり、本体 {@code TestDataParsingTemplate} が
     * 解析中の例外に被せる「{@code can't get data.}」には包まれない（{@code reader.open} が
     * その {@code try} の外側で呼ばれるため）。原因例外を持たないことまで固定する。
     * </p>
     */
    @Test
    public void failsWithSheetNotFoundWhenSheetIsAbsentFromRealBook() {
        // Given
        book("absentSheet").row(text("SETUP_TABLE=T"))
                .row(text("A"))
                .row(text("a1"))
                .writeTo(dir());

        // When
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> read("absentSheet", "noSuchSheet"));

        // Then
        assertThat(thrown.getMessage(), containsString("sheet not found."));
        assertThat(thrown.getMessage(), containsString("sheet=[noSuchSheet]"));
        assertThat("どのブックかがメッセージから分かる", thrown.getMessage(), containsString("absentSheet.xlsx"));
        assertThat("can't get data. には包まれない", thrown.getCause(), is(nullValue()));
    }

    // ------------------------------------------------------------------ F1-02 ブック破損

    /**
     * Given: 拡張子だけが {@code .xlsx} で中身が Excel ではないファイル。
     * When : そのブックを {@code read}。
     * Then : 汎用の {@code java.lang.RuntimeException}（本体 {@code PoiXlsReader#getWorkbook}）が送出され、
     *        原因例外に POI の {@code IllegalArgumentException} が連鎖する。
     *        <b>例外連鎖のどのメッセージにもファイル名が現れない</b>。
     *
     * <p>
     * 担保する軸要素: F1-02。ファイル名が出ないことは、多数のブックを一括変換する経路
     * （{@code TestDataConverter#convert}）でどのブックが壊れているか分からないという課題であり、
     * {@code issues.md} の <b>XLS-14</b> に記録した（修正はしない）。本アサートはその根拠である。
     * </p>
     */
    @Test
    public void failsWithGenericRuntimeExceptionWhenWorkbookIsBroken() throws IOException {
        // Given: .xlsx という名前だが中身は Excel ではない
        Files.write(dir().resolve("broken.xlsx"), "this is not a workbook".getBytes(StandardCharsets.UTF_8));

        // When
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> read("broken"));

        // Then
        assertThat("専用の例外型ではなく汎用の RuntimeException が使われる",
                thrown.getClass().getName(), is("java.lang.RuntimeException"));
        assertThat(thrown.getMessage(), is("test data file open failed."));
        assertThat("原因は POI がストリームを判別できないこと",
                causeOf(thrown), is(instanceOf(IllegalArgumentException.class)));
        assertThat(causeOf(thrown).getMessage(), containsString("OOXML"));
        assertThat("どのブックが壊れているかは例外メッセージから分からない（issues.md XLS-14）",
                allMessagesOf(thrown), not(containsString("broken.xlsx")));
    }

    // ------------------------------------------------------------------ F1-03 未知のデータタイプ名

    /**
     * Given: 未知のデータタイプ名のマーカー（{@code UNKNOWN_TYPE=X}）で始まるブロックと、
     *        既知のマーカー（{@code SETUP_TABLE=T}）のブロックが同居する実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 例外にはならず変換は継続する。未知タイプのブロックは<b>マーカー行もデータ行も丸ごと
     *        中間モデルに現れない</b>。既知タイプのブロックだけが読まれる。警告も出ない。
     *
     * <p>
     * 担保する軸要素: F1-03。#18 の棚卸しでは {@code XlsFormatReaderTest#readIgnoresDataTypePrefixedLineWithoutMarker}
     * （データタイプ名で始まるが {@code =} を持たない行の無視）による 🔺 弱い担保しか無かった。
     * 本テストは「未知の名前そのもの」を実 {@code .xlsx} 経路で固定する。
     * </p>
     *
     * <p>
     * 変換対象のブロックが警告も無く消えるため、{@code issues.md} の <b>XLS-10</b> に課題として記録した
     * （修正はしない）。警告が出ないことまでアサートするのが「検出できない」という主張の根拠である。
     * </p>
     */
    @Test
    public void ignoresBlockWhoseMarkerHasUnknownDataTypeNameInRealBook() {
        // Given
        book("unknownType").row(text("UNKNOWN_TYPE=X"))
                .row(text("A"))
                .row(text("a1"))
                .row(text("SETUP_TABLE=T"))
                .row(text("B"))
                .row(text("b1"))
                .writeTo(dir());

        // When
        Reading reading = readCapturingWarnings("unknownType");

        // Then: 生成されたのは既知タイプのブロックだけ（未知タイプのブロックは 1 件も無い）
        TableDataBlock table = onlyBlock(reading.container, TableDataBlock.class);
        assertThat(table.getDataType(), is(DataType.SETUP_TABLE_DATA));
        assertThat(table.getIdentifier(), is("T"));
        assertThat("未知タイプ側のカラム A は取り込まれない",
                table.getColumnNames(), is(Arrays.asList("B")));
        assertThat("未知タイプ側のデータ行 a1 も取り込まれない（issues.md XLS-10）",
                table.getRows(), is(Arrays.asList(Arrays.asList("b1"))));
        assertNoWarning(reading, "issues.md XLS-10");
    }

    /**
     * Given: 既知のデータタイプ名で始まる未知の名前のマーカー（{@code SETUP_TABLEX=T}）を持つ実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 例外にはならず、{@code SETUP_TABLE} ブロックとして読まれる。データタイプ名の直後から
     *        {@code =} までの文字列（{@code X}）が<b>グループ ID として</b>切り出される。
     *
     * <p>
     * 担保する軸要素: F1-03（既知タイプ名を接頭辞に持つ未知の名前）。正しいグループ ID の記法は
     * {@code SETUP_TABLE[g1]=T} のように角括弧付きだが、切り出しを行う
     * {@code TestCoreReaderAdapter#markerGroupId}（<b>本リポジトリの
     * {@code src/main/java/nablarch/test/core/reader/TestCoreReaderAdapter.java}</b>。
     * {@code nablarch-testing} 側ではない）は角括弧の有無を検証しない。
     * {@code issues.md} の <b>XLS-11</b> に記録した（修正はしない）。
     * </p>
     */
    @Test
    public void readsSuffixAfterKnownDataTypeNameAsGroupIdInRealBook() {
        // Given: SETUP_TABLE の綴り誤り（末尾に X）
        book("typoType").row(text("SETUP_TABLEX=T"))
                .row(text("A"))
                .row(text("a1"))
                .writeTo(dir());

        // When
        TableDataBlock table = onlyBlock("typoType", TableDataBlock.class);

        // Then
        assertThat(table.getDataType(), is(DataType.SETUP_TABLE_DATA));
        assertThat("角括弧が無くてもグループ ID として扱われる（issues.md XLS-11）",
                table.getGroupId(), is("X"));
        assertThat(table.getIdentifier(), is("T"));
        assertThat(table.getColumnNames(), is(Arrays.asList("A")));
        assertThat(table.getRows(), is(Arrays.asList(Arrays.asList("a1"))));
    }

    // ------------------------------------------------------------------ F1-04 マーカーカラム欠落

    /**
     * Given: マーカーカラムの角括弧を欠いたカラム行（{@code [no]} ではなく {@code no}）を持つ
     *        {@code LIST_MAP} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 例外にはならず、当該カラムは<b>ふつうのデータカラムとして</b>中間モデルに入る。
     *
     * <p>
     * 担保する軸要素: F1-04。マーカーカラムの判定は本体 {@code HeaderLine} の
     * 「{@code [} で始まり {@code ]} で終わる」だけであり、角括弧を欠くと除外されない。
     * 角括弧付きのカラム行が除外される側の担保は
     * {@code XlsFormatReaderRealFileTest#readsEmptyColumnNamesFromMarkerOnlyListMapInRealBook} にある。
     * </p>
     */
    @Test
    public void readsMarkerColumnWithoutBracketsAsOrdinaryDataColumnInRealBook() {
        // Given
        book("noBrackets").row(text("LIST_MAP=lm"))
                .row(text("no"), text("A"))
                .row(text("1"), text("a1"))
                .writeTo(dir());

        // When
        ListMapBlock listMap = onlyBlock("noBrackets", ListMapBlock.class);

        // Then
        assertThat("角括弧が無いためマーカーカラムとして除外されない",
                listMap.getColumnNames(), is(Arrays.asList("no", "A")));
        assertThat(listMap.getRows(), is(Arrays.asList(Arrays.asList("1", "a1"))));
    }

    /**
     * Given: 送信同期メッセージのメタ列（{@code no} 列）を書き忘れた実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 例外にはならず、先頭列がメタ列として扱われる。すなわち<b>先頭フィールド名がレコード種別に、
     *        先頭列の値が捨てられる</b>。警告も出ない。
     *
     * <p>
     * 担保する軸要素: F1-04（メタ列の欠落）。正しい入力は名前行 {@code no, requestId, userId} である
     * （{@code XlsFormatReaderRealFileTest#readsAllFourSendSyncMessageTypesFromRealBook} 参照）。
     * 欠落してもフィールドと値が 1 つずつ静かに失われるだけで警告は出ないため、
     * {@code issues.md} の <b>XLS-13</b> に記録した（修正はしない）。
     * </p>
     */
    @Test
    public void dropsFirstFieldWhenSendSyncMetaColumnIsMissingInRealBook() {
        // Given: 名前行の先頭に本来置くべき no 列が無い
        book("noMetaColumn").row(text("EXPECTED_REQUEST_HEADER_MESSAGES[case1]=RM01"))
                .row(text("requestId"), text("userId"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("10"))
                .row(text("RM01"), text("user01"))
                .writeTo(dir());

        // When
        Reading reading = readCapturingWarnings("noMetaColumn");

        // Then
        MessageDataBlock message = onlyBlock(reading.container, MessageDataBlock.class);
        assertThat(message.getDataType(), is(DataType.EXPECTED_REQUEST_HEADER_MESSAGES));
        assertThat("レコードレイアウト数", message.getRecords().size(), is(1));
        RecordLayout record = message.getRecords().get(0);
        assertThat("先頭列はメタ列として扱われ、フィールド名がレコード種別になる",
                record.getRecordType(), is("requestId"));
        assertThat("先頭フィールドが脱落する（issues.md XLS-13）",
                fieldNames(record), is(Arrays.asList("userId")));
        assertThat("先頭列の値 RM01 も脱落する（issues.md XLS-13）",
                record.getRows(), is(Arrays.asList(Arrays.asList("user01"))));
        assertNoWarning(reading, "issues.md XLS-13");
    }

    // ------------------------------------------------------------------ F1-05 カラム名重複

    /**
     * Given: カラム行に同名カラム（{@code COL_A} が 2 回）を持つ {@code LIST_MAP} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 変換は継続し、重複カラムは<b>後勝ち</b>で 1 件に絞られる。WARN ログが 1 件出て、
     *        ブック名・シート名・ブロック識別子・重複カラム名・採用した列番号を含む。
     *
     * <p>
     * 担保する軸要素: F1-05（{@link ListMapBlock} 経路）。タスク #16 の
     * {@code XlsFormatReaderTest#readListMapWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins} は
     * Fake リーダ経路であり、実セル → 文字列行の区間を通らない。本テストは同じ挙動が実 {@code .xlsx}
     * 経路でも成り立つことを固定する。
     * </p>
     */
    @Test
    public void deduplicatesDuplicateColumnNamesWithWarningInListMapFromRealBook() {
        // Given: COL_A が 2 回（3 列目が後勝ち）
        book("dupListMap").row(text("LIST_MAP=dupMap"))
                .row(text("COL_A"), text("COL_B"), text("COL_A"))
                .row(text("first"), text("b1"), text("last"))
                .writeTo(dir());

        // When
        Reading reading = readCapturingWarnings("dupListMap");

        // Then
        ListMapBlock listMap = onlyBlock(reading.container, ListMapBlock.class);
        assertThat("重複は後勝ちで 1 件に絞られる", listMap.getColumnNames(), is(Arrays.asList("COL_B", "COL_A")));
        assertThat("採用されるのは後方の列の値", listMap.getRows(), is(Arrays.asList(Arrays.asList("b1", "last"))));
        assertThat("WARN ログの件数（実際: " + reading.warnings + "）", reading.warnings.size(), is(1));
        assertThat(reading.warnings.get(0), containsString("dupListMap"));
        assertThat(reading.warnings.get(0), containsString("\"s\""));
        assertThat(reading.warnings.get(0), containsString("dupMap"));
        assertThat(reading.warnings.get(0), containsString("COL_A"));
        assertThat(reading.warnings.get(0), containsString("3 列目"));
    }

    /**
     * Given: カラム行に同名カラム（{@code COL_X} が 2 回）を持つ {@code SETUP_TABLE} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : LIST_MAP 経路と同じく後勝ちで 1 件に絞られ、WARN ログが 1 件出る（経路が別なので個別に固定する）。
     *
     * <p>
     * 担保する軸要素: F1-05（{@link TableDataBlock} 経路）。#16 の
     * {@code XlsFormatReaderTest#readTableWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins} の
     * 実 {@code .xlsx} 版である。
     * </p>
     */
    @Test
    public void deduplicatesDuplicateColumnNamesWithWarningInTableFromRealBook() {
        // Given: COL_X が 2 回（3 列目が後勝ち）
        book("dupTable").row(text("SETUP_TABLE=MY_TABLE"))
                .row(text("COL_X"), text("COL_Y"), text("COL_X"))
                .row(text("first"), text("y1"), text("last"))
                .writeTo(dir());

        // When
        Reading reading = readCapturingWarnings("dupTable");

        // Then
        TableDataBlock table = onlyBlock(reading.container, TableDataBlock.class);
        assertThat("重複は後勝ちで 1 件に絞られる", table.getColumnNames(), is(Arrays.asList("COL_Y", "COL_X")));
        assertThat("採用されるのは後方の列の値", table.getRows(), is(Arrays.asList(Arrays.asList("y1", "last"))));
        assertThat("WARN ログの件数（実際: " + reading.warnings + "）", reading.warnings.size(), is(1));
        assertThat(reading.warnings.get(0), containsString("dupTable"));
        assertThat(reading.warnings.get(0), containsString("MY_TABLE"));
        assertThat(reading.warnings.get(0), containsString("COL_X"));
        assertThat(reading.warnings.get(0), containsString("3 列目"));
    }

    // ------------------------------------------------------------------ MESSAGE 本文の 2 つ目のレコードレイアウト

    /**
     * Given: 本文に 2 つ目のレコードレイアウト（名前行 {@code data}／型行／長さ行／値行）を書いた
     *        {@code MESSAGE} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 例外にはならないが、レコードレイアウトは<b>1 件のまま</b>で、2 つ目の名前行・型行・長さ行・値行が
     *        すべて 1 つ目の<b>値行として吸収される</b>。警告も出ない。
     *
     * <p>
     * 担保する軸要素: E-3（ファイル内レコードレイアウト数「複数」が<b>メッセージ経路では到達不能</b>で
     * あることの根拠）。ファイル経路の E-3(複数) の担保は
     * {@code XlsFormatReaderRealFileTest#readsMultipleRecordLayoutsFromOneFixedFileInRealBook} にある。
     * </p>
     *
     * <p>
     * 本体 {@code MessageParser} が生成する {@code FixedLengthFileParser} は {@code onReadingValues} を
     * 上書きし、先頭セルが非空でも新しい断片を作らず常に値行として足す（送信同期の {@code no} 列に
     * 合わせた仕様）。したがって {@code MESSAGE} 系では 1 ブロックにレコードレイアウトを 2 件以上作れない。
     * </p>
     *
     * <p>
     * 構造が黙って崩れる（フィールド名・型・長さがデータ値になる）ため、{@code issues.md} の <b>XLS-15</b> に
     * 記録した（修正はしない）。本テストはその根拠である。
     * </p>
     */
    @Test
    public void absorbsSecondNameRowAsDataRowInMessageBodyInRealBook() {
        // Given: 2 つ目のレコードレイアウトを書いた MESSAGE
        book("multiLayoutMessage").row(text("MESSAGE=m"))
                .row(text("requestId"), text("R1"))
                .row(text("header"), text("h1"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("2"))
                .row(blank(), text("HH"))
                .row(text("data"), text("d1"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("3"))
                .row(blank(), text("abc"))
                .writeTo(dir());

        // When
        Reading reading = readCapturingWarnings("multiLayoutMessage");

        // Then
        MessageDataBlock message = onlyBlock(reading.container, MessageDataBlock.class);
        assertThat("2 つ目のレコードレイアウトは作られない", message.getRecords().size(), is(1));
        RecordLayout record = message.getRecords().get(0);
        assertThat(record.getRecordType(), is("header"));
        assertThat(fieldNames(record), is(Arrays.asList("h1")));
        assertThat("2 つ目の名前行・型行・長さ行・値行がすべて値行になる（issues.md XLS-15）",
                record.getRows(), is(Arrays.asList(
                        Arrays.asList("HH"),
                        Arrays.asList("d1"),
                        Arrays.asList("半角英字"),
                        Arrays.asList("3"),
                        Arrays.asList("abc"))));
        assertNoWarning(reading, "issues.md XLS-15");
    }

    // ------------------------------------------------------------------ F1-06 行と列の数の不一致

    /**
     * Given: データ行がカラム行より短いテーブル（{@code SHORT}）と長いテーブル（{@code LONG}）を
     *        1 シートに持つ実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 例外にはならない。<b>足りないセルは空文字で埋められ、はみ出したセルは捨てられる</b>
     *        （非対称）。警告も出ない。
     *
     * <p>
     * 担保する軸要素: F1-06（テーブル経路）。はみ出したセルが警告も無く消えることは
     * {@code issues.md} の <b>XLS-12</b> に記録した（修正はしない）。
     * </p>
     */
    @Test
    public void padsShortDataRowAndDropsCellsBeyondColumnRowInRealBook() {
        // Given
        book("rowLength").row(text("SETUP_TABLE=SHORT"))
                .row(text("A"), text("B"))
                .row(text("a1"))
                .row(text("SETUP_TABLE=LONG"))
                .row(text("C"), text("D"))
                .row(text("c1"), text("d1"), text("e1"))
                .writeTo(dir());

        // When
        Reading reading = readCapturingWarnings("rowLength");

        // Then
        List<TestDataBlock> blocks = blocksOf(reading.container);
        assertThat("テーブル数", blocks.size(), is(2));

        TableDataBlock shortRow = blockOf(blocks, "SHORT", TableDataBlock.class);
        assertThat(shortRow.getColumnNames(), is(Arrays.asList("A", "B")));
        assertThat("足りないセルは空文字で埋められる",
                shortRow.getRows(), is(Arrays.asList(Arrays.asList("a1", ""))));

        TableDataBlock longRow = blockOf(blocks, "LONG", TableDataBlock.class);
        assertThat(longRow.getColumnNames(), is(Arrays.asList("C", "D")));
        assertThat("カラム行よりも右のセル e1 は黙って捨てられる（issues.md XLS-12）",
                longRow.getRows(), is(Arrays.asList(Arrays.asList("c1", "d1"))));
        assertNoWarning(reading, "issues.md XLS-12");
    }

    /**
     * Given: 値行が名前行より短い固定長ファイル（{@code short.dat}）と長い固定長ファイル
     *        （{@code long.dat}）を 1 シートに持つ実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 例外にはならない。テーブル経路と同じく<b>足りないセルは空文字で埋められ、
     *        はみ出したセルは捨てられる</b>。警告も出ない。
     *
     * <p>
     * 担保する軸要素: F1-06（ファイル経路の値行）。はみ出したセルの消失は {@code issues.md} の
     * <b>XLS-12</b> に記録した（修正はしない）。
     * </p>
     */
    @Test
    public void padsShortValueRowAndDropsCellsBeyondNameRowInFixedFileInRealBook() {
        // Given
        book("valueLength").row(text("SETUP_FIXED=short.dat"))
                .row(text("data"), text("f1"), text("f2"))
                .row(blank(), text("半角英字"), text("半角英字"))
                .row(blank(), text("3"), text("2"))
                .row(blank(), text("abc"))
                .row(text("SETUP_FIXED=long.dat"))
                .row(text("data"), text("g1"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("3"))
                .row(blank(), text("xyz"), text("extra"))
                .writeTo(dir());

        // When
        Reading reading = readCapturingWarnings("valueLength");

        // Then
        List<TestDataBlock> blocks = blocksOf(reading.container);
        assertThat("ファイルブロック数", blocks.size(), is(2));

        RecordLayout shortRow = blockOf(blocks, "short.dat", FileDataBlock.class).getRecords().get(0);
        assertThat(fieldNames(shortRow), is(Arrays.asList("f1", "f2")));
        assertThat("足りないセルは空文字で埋められる",
                shortRow.getRows(), is(Arrays.asList(Arrays.asList("abc", ""))));

        RecordLayout longRow = blockOf(blocks, "long.dat", FileDataBlock.class).getRecords().get(0);
        assertThat(fieldNames(longRow), is(Arrays.asList("g1")));
        assertThat("名前行よりも右のセル extra は黙って捨てられる（issues.md XLS-12）",
                longRow.getRows(), is(Arrays.asList(Arrays.asList("xyz"))));
        assertNoWarning(reading, "issues.md XLS-12");
    }

    /**
     * Given: 長さ行が名前行より短い {@code SETUP_FIXED} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 本体 {@code TestDataParsingTemplate} が包む {@code IllegalStateException}
     *        （{@code can't get data.}）が送出され、原因は本体 {@code DataFileFragment#assertSameSizeAsNames}
     *        の {@code IllegalArgumentException} である。
     *
     * <p>
     * 担保する軸要素: F1-06（長さ行の要素数不一致）。値行の不一致（空文字埋め・切り捨て）とは異なり、
     * <b>型行・長さ行の不一致は本体パーサが弾く</b>という非対称を固定する。
     * </p>
     */
    @Test
    public void failsWhenLengthRowIsShorterThanNameRowInRealBook() {
        // Given
        book("shortLengthRow").row(text("SETUP_FIXED=f.dat"))
                .row(text("data"), text("f1"), text("f2"))
                .row(blank(), text("半角英字"), text("半角英字"))
                .row(blank(), text("3"))
                .row(blank(), text("abc"), text("de"))
                .writeTo(dir());

        // When
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> read("shortLengthRow"));

        // Then
        Throwable cause = assertWrappedByCannotGetData(thrown);
        assertThat(cause, is(instanceOf(IllegalArgumentException.class)));
        assertThat(cause.getMessage(), containsString("field name size is 2. but lengths size is 1."));
    }

    // ------------------------------------------------------------------ C-17／C-20「到達不能」の根拠

    /**
     * Given: 名前行がレコード種別セル 1 列だけの {@code SETUP_FIXED} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code IllegalStateException}（{@code can't get data.}）が送出され、原因は本体
     *        {@code DataFileParser#processDirectives} の
     *        {@code IllegalStateException: directive or data names row must have two columns at least.} である。
     *
     * <p>
     * <b>C-17（{@code RecordLayout.fields} 空）が実 {@code .xlsx} 経路で到達不能であることの根拠（ファイル系）。</b>
     * フィールドを 0 件にするには名前行をレコード種別セルだけにするしかないが、本体パーサが
     * 名前行を 2 列以上と要求するため器が組み立たない。メッセージ系は
     * {@link #failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook} が別に確かめる
     * （{@code issues.md} の「到達不能」表から両メソッドを参照している）。
     * </p>
     */
    @Test
    public void failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook() {
        // Given
        book("singleNameFixed").row(text("SETUP_FIXED=f.dat"))
                .row(text("data"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("3"))
                .row(blank(), text("abc"))
                .writeTo(dir());

        // When
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> read("singleNameFixed"));

        // Then
        assertNameRowNeedsTwoColumns(thrown);
    }

    /**
     * Given: 本文の名前行がレコード種別セル 1 列だけの {@code MESSAGE} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : ファイル系と同じ例外・同じメッセージになる（経路が別なので個別に確かめる）。
     *
     * <p>
     * <b>C-17 が到達不能であることの根拠（メッセージ系）。</b>
     * </p>
     */
    @Test
    public void failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook() {
        // Given
        book("singleNameMessage").row(text("MESSAGE=m"))
                .row(text("requestId"), text("R1"))
                .row(text("data"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("3"))
                .row(blank(), text("abc"))
                .writeTo(dir());

        // When
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> read("singleNameMessage"));

        // Then
        assertNameRowNeedsTwoColumns(thrown);
    }

    /**
     * 名前行が 2 列未満で弾かれたことをアサートする（C-17 の根拠 2 件で共有する）。
     *
     * @param thrown 送出された例外
     */
    private static void assertNameRowNeedsTwoColumns(IllegalStateException thrown) {
        Throwable cause = assertWrappedByCannotGetData(thrown);
        assertThat(cause, is(instanceOf(IllegalStateException.class)));
        assertThat(cause.getMessage(),
                containsString("directive or data names row must have two columns at least."));
        assertThat("弾かれた行の内容がメッセージに入る", cause.getMessage(), containsString("[data]"));
    }

    /**
     * Given: 型行が名前行より短い（フィールド 2 件に対し型が 1 件）{@code SETUP_FIXED} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code IllegalStateException}（{@code can't get data.}）が送出され、原因は本体
     *        {@code DataFileFragment#assertSameSizeAsNames}（{@code setTypes} から呼ばれる）の
     *        {@code IllegalArgumentException} である。
     *
     * <p>
     * <b>C-20（{@code FieldDef.type} 省略＝{@code null}）が到達不能であることの根拠①。</b>
     * {@link XlsFormatReader} には型行が名前行より短いときに {@code null} を入れるフォールバックが
     * あるが、そこへ届く前に本体パーサが器の組み立てに失敗する。型セルが<b>末尾</b>で空の場合も、
     * 空セルは行の使用範囲から外れるため同じ経路になる。
     * </p>
     */
    @Test
    public void failsWhenTypeRowIsShorterThanNameRowInRealBook() {
        // Given
        book("shortTypeRow").row(text("SETUP_FIXED=f.dat"))
                .row(text("data"), text("f1"), text("f2"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("3"), text("2"))
                .row(blank(), text("abc"), text("de"))
                .writeTo(dir());

        // When
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> read("shortTypeRow"));

        // Then
        Throwable cause = assertWrappedByCannotGetData(thrown);
        assertThat(cause, is(instanceOf(IllegalArgumentException.class)));
        assertThat(cause.getMessage(), containsString("field name size is 2. but types size is 1."));
    }

    /**
     * Given: 型セルが中間位置で空（フィールド 2 件に対し 1 件目の型セルだけ空白セル）の
     *        {@code SETUP_FIXED} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code IllegalStateException}（{@code can't get data.}）が送出され、原因は本体
     *        {@code BasicDataTypeMapping#convertToFrameworkExpression} の
     *        {@code IllegalArgumentException: can't convert value []. convert table =...} である。
     *
     * <p>
     * <b>C-20（{@code FieldDef.type} 省略＝{@code null}）が到達不能であることの根拠②。</b>
     * 中間位置の空セルは要素数が一致するため {@code assertSameSizeAsNames} は通るが、空文字を
     * フレームワーク表記へ変換できずに弾かれる。すなわち型の欠落は<b>2 通りの機構</b>で弾かれる。
     * </p>
     *
     * <p>
     * 変換表（{@code convert table =}）は {@code HashMap} 由来で並び順が変わるため、メッセージは
     * 変換できなかった値までを突き合わせる。
     * </p>
     */
    @Test
    public void failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook() {
        // Given: f1 の型セルだけが空（f2 の型セルは埋まっているので行末の切り詰めは起きない）
        book("blankTypeCell").row(text("SETUP_FIXED=f.dat"))
                .row(text("data"), text("f1"), text("f2"))
                .row(blank(), blank(), text("半角英字"))
                .row(blank(), text("3"), text("2"))
                .row(blank(), text("abc"), text("de"))
                .writeTo(dir());

        // When
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> read("blankTypeCell"));

        // Then
        Throwable cause = assertWrappedByCannotGetData(thrown);
        assertThat(cause, is(instanceOf(IllegalArgumentException.class)));
        assertThat(cause.getMessage(), containsString("can't convert value []."));
        assertThat(cause.getMessage(), containsString("convert table ="));
    }
}
