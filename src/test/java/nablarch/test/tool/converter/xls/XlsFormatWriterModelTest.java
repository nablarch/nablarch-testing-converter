package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.line;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nablarch.test.core.reader.DataType;
import nablarch.test.tool.converter.model.ColumnRowDataBlock;
import nablarch.test.tool.converter.model.FieldDef;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.MessageDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 辺③（中間モデル→Excel）の軸A・軸C・軸E の欠けを埋めるテスト。
 *
 * <p>
 * 対象は {@code .rn/ntf-test-data-converter/coverage/inventory.md} §3.3 が
 * 「#23 の対象」として残していた 15 要素と、#23 のレビューで<b>担保の穴</b>として判明した軸A 3 要素である。
 * </p>
 * <ul>
 *   <li><b>軸A 3 件</b>: A-01 {@code DEFAULT}（辺③では到達可能）／A-07 {@code EXPECTED_FIXED}／
 *       A-09 {@code EXPECTED_VARIABLE}（後 2 者は {@code RoundTripTest} 経由の 🔺 しか無かった）</li>
 *   <li><b>軸A 送信同期 3 件（レビュー指摘で追加）</b>: A-12 {@code EXPECTED_REQUEST_BODY_MESSAGES}／
 *       A-13 {@code RESPONSE_HEADER_MESSAGES}／A-14 {@code RESPONSE_BODY_MESSAGES}。
 *       識別セルを固定するテストが辺③に 1 件も無かった（詳細は該当セクションのコメント）</li>
 *   <li><b>軸C 8 件</b>: C-02 {@code sections} 空／C-04 {@code blocks} 空／C-08 {@code columnNames} 空／
 *       C-09 {@code rows} 空／C-12 {@code FileDataBlock.records} 空／
 *       C-13 {@code MessageDataBlock.directives} 値あり／C-15 {@code MessageDataBlock.records} 空／
 *       C-18 {@code RecordLayout.rows} 空。
 *       <b>C-17（{@code RecordLayout.fields} 空）は本クラスの対象から外した。</b>
 *       {@link RecordLayout} が<b>生成時点で</b>フィールド 0 件を拒否するため、辺③へ届かない
 *       （番人は {@code RecordLayoutTest#フィールドを1件も持たないレコードは生成できない} が担保する。
 *       {@code issues.md} XLS-22）</li>
 *   <li><b>軸E 3 件</b>: E-1(0 件)＝セクション内ブロック数 0／E-2(0 件)＝ブロック内行数 0／
 *       E-3(0 件)＝ファイル内レコードレイアウト数 0</li>
 *   <li><b>軸E 1 件（#27 で追加）</b>: E-1(1 件)＝セクション内ブロック数 1。
 *       {@code axis-matrix.md} §3.5 の総点検で、シート全体の行数を見るアサートが {@code src/test} 全体で
 *       0 件側の 1 か所しか無く 1 件側が空いていることが判明したため足した</li>
 * </ul>
 *
 * <p>
 * <b>本クラスは全件が実 {@code .xlsx} を書いて開き直す。</b>{@code XlsFormatWriterTest} は
 * {@code build}（メモリ上のブック）を見るものと実ファイルを書くものが混在するが、本クラスは
 * <b>全件が実ファイル経路</b>である点が違う（同クラスの内訳と導出コマンドは
 * {@code .rn/ntf-test-data-converter/coverage/inventory.md} §3.1 の末尾にある）。
 * 本クラスが扱うのは「空のコレクションが版面のどこに現れる／現れないか」であり、行やセルが直列化で
 * 落ちないことまで含めて確かめたいので、{@code write} が実際に作ったファイルを検証対象にする
 * （{@code XlsFormatWriterCellTypeTest} と同じ方針）。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>期待される仕様ではない。
 * 妥当でないと判断した挙動は {@code .rn/ntf-test-data-converter/coverage/issues.md} に課題
 * （XLS-20〜XLS-23）として記録してある。<b>このうち XLS-20・XLS-22 は #25.5 で修正済み</b>であり、
 * フィールド 0 件のレコードレイアウトと {@code DataType.DEFAULT} のブロックは中間モデルが生成時に
 * 拒否するようになった。残る XLS-21／XLS-23 の実装（src/main）は変更していない。
 * </p>
 *
 * <p>
 * <b>読み戻しを実検査する末尾の 2 件は無くなった。</b>XLS-21 の 1 件は XLS-27 の番人で到達不能に
 * なったため削除済み（{@code 57c1b0d}）、XLS-20 の 1 件は #25.5 §1-G で {@code DEFAULT} のブロックが
 * 作れなくなったため削除した。
 * </p>
 *
 * @author kiyobot
 */
public class XlsFormatWriterModelTest {

    /** 出力シート名。 */
    private static final String SHEET = "s";

    /** {@link XlsFormatWriter} が付ける拡張子。 */
    private static final String EXTENSION = ".xlsx";

    /**
     * 出力先。
     *
     * <p>
     * {@link TemporaryFolder} はテストメソッドごとに別ディレクトリを与えるため、全メソッドが同じ
     * ブック名を使ってもファイルパスは衝突しない。
     * </p>
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // ------------------------------------------------------------------ helpers

    /**
     * 行を組み立てる（{@link Arrays#asList}）。
     *
     * @param cells セル
     * @return 行
     */
    private static List<String> row(String... cells) {
        return Arrays.asList(cells);
    }

    /**
     * キー順を保つマップを作る。
     *
     * @param kv キー・値の並び
     * @return マップ
     */
    private static Map<String, String> map(String... kv) {
        Map<String, String> m = new LinkedHashMap<String, String>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return m;
    }

    /**
     * 1 セクション 1 コンテナを組み立てる。
     *
     * @param book   ブック名
     * @param blocks ブロック
     * @return コンテナ
     */
    private static TestDataContainer container(String book, TestDataBlock... blocks) {
        TestDataSection section = new TestDataSection(SHEET, Arrays.asList(blocks));
        return new TestDataContainer(book, Collections.singletonList(section));
    }

    /**
     * コンテナを実 {@code .xlsx} へ書き出し、書き出したファイルを返す。
     *
     * @param container コンテナ
     * @return 書き出したファイル
     */
    private File write(TestDataContainer container) {
        new XlsFormatWriter().write(container, folder.getRoot().getAbsolutePath());
        return new File(folder.getRoot(), container.getName() + EXTENSION);
    }

    /**
     * コンテナを実 {@code .xlsx} へ書き出し、POI で開き直したブックを返す。
     *
     * @param container コンテナ
     * @return 書き出したファイルから読み直したブック
     */
    private Workbook writeAndReopen(TestDataContainer container) {
        File file = write(container);
        assertTrue("ブックが書き出されていること", file.exists());
        return XlsFixture.open(file.toPath());
    }

    /**
     * コンテナを実 {@code .xlsx} へ書き出し、POI で開き直した唯一のシートを返す。
     *
     * @param container コンテナ
     * @return 書き出したファイルから読み直したシート
     */
    private Sheet writeAndReopenSheet(TestDataContainer container) {
        Sheet sheet = writeAndReopen(container).getSheet(SHEET);
        assertThat("書き出したブックに出力先シートがあること", sheet, is(notNullValue()));
        return sheet;
    }

    /**
     * コンテナを実 {@code .xlsx} へ書き出し、{@link XlsFormatReader} で読み戻す。
     *
     * @param container コンテナ
     * @return 読み戻した中間モデル
     */
    private TestDataContainer writeAndReadBack(TestDataContainer container) {
        write(container);
        return new XlsFormatReader().read(folder.getRoot().getAbsolutePath(),
                container.getName() + "/" + SHEET);
    }

    // 1 行の取り出し（line）は XlsFixture の static メソッドを使う（本ファイル冒頭で static import）。

    // ------------------------------------------------------------------ 軸A の欠け 2 件
    // （3 件目の DataType.DEFAULT は #25.5 §1-G で中間モデルが生成時に拒否するようになり削除した）

    /**
     * Given: ディレクティブと 1 レコード（型・長さあり）を持つ {@code EXPECTED_FIXED} のファイルブロック。
     * When : 実 {@code .xlsx} へ書き出し、POI で開き直す。
     * Then : 識別セルが {@code EXPECTED_FIXED=exp.dat} になり、
     *        識別行 → ディレクティブ行 → 名前行 → 型行 → <b>長さ行</b> → データ行の版面になる。
     *
     * <p>
     * 担保する軸要素: A-07。{@code XlsFormatWriterTest} には {@code EXPECTED_FIXED} を書くテストが
     * 1 件も無く、{@code RoundTripTest#xls_expectedFixed_isPreserved} 経由の 🔺 だけだった。
     * </p>
     */
    @Test
    public void writesExpectedFixedFileBlockWithLengthRow() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Collections.singletonList(new FieldDef("f1", "半角英字", "5")),
                Collections.singletonList(row("abcde")));
        FileDataBlock file = new FileDataBlock(DataType.EXPECTED_FIXED, "", "exp.dat", map("text-encoding", "UTF-8"),
                Collections.singletonList(record));

        // When
        Sheet sheet = writeAndReopenSheet(container("expectedFixed", file));

        // Then
        assertThat(line(sheet, 0), is(Arrays.asList("EXPECTED_FIXED=exp.dat")));
        assertThat(line(sheet, 1), is(Arrays.asList("text-encoding", "UTF-8")));
        assertThat(line(sheet, 2), is(Arrays.asList("data", "f1")));
        assertThat(line(sheet, 3), is(Arrays.asList("", "半角英字")));
        assertThat("固定長なので長さ行が出る", line(sheet, 4), is(Arrays.asList("", "5")));
        assertThat(line(sheet, 5), is(Arrays.asList("", "abcde")));
    }

    /**
     * XLS-44。Given: 名前・型・データ行が同じレコードを {@code SETUP_FIXED}（長さあり）／
     *        {@code SETUP_VARIABLE}（長さなし）で持つ 2 ブロック。
     * When : それぞれ実 {@code .xlsx} へ書き出し、POI で開き直す。
     * Then : 長さ行の有無が<b>データ種別だけで決まる</b>（固定長系は出る／可変長系は出ない）。
     *
     * <p>
     * 導出前は長さ行の有無が引数の {@code fileType} で決まっていたため、識別セルに
     * {@code SETUP_FIXED=…} と書きながら長さ行を書かない Excel を出せた
     * （{@code coverage/issues.md} <b>XLS-44</b>。実測 2026-08-21）。これは
     * {@code testdata_notation.rst:883}（{@code 30a8271} 時点）「固定長ファイルでは、フィールド名称・
     * データ型・フィールド長の3リストが同サイズで必須」に反する。導出後はファイル種別を外から渡せない。
     * </p>
     *
     * <p>
     * <b>2 ブロックのレコードは長さだけが違う。</b>#31（{@code coverage/issues.md} <b>XLS-45</b>）で
     * 可変長系のブロックが長さを持つフィールド定義を保持できなくなったため、
     * <b>「長さを持つ同一のレコードを両方へ渡す」形はもはや組めない</b>
     * （組めば {@code FileDataBlock} の生成が {@code IllegalArgumentException} で落ちる。
     * {@code FileDataBlockTest#フィールド長を持つフィールド定義は可変長系のデータ種別すべてで拒否される}）。
     * XLS-44 が主張したいのは<b>識別セルと長さ行が食い違わないこと</b>であり、それは長さの値ではなく
     * データ種別で決まる。ここで固定するのはその主張である。
     * </p>
     */
    @Test
    public void writesLengthRowDecidedSolelyByDataType() {
        // Given: 名前・型・データ行が同じで、長さだけが違う 2 つのレコード
        RecordLayout fixedRecord = new RecordLayout("data",
                Collections.singletonList(new FieldDef("f1", "半角英字", "5")),
                Collections.singletonList(row("abcde")));
        RecordLayout variableRecord = new RecordLayout("data",
                Collections.singletonList(new FieldDef("f1", "半角英字", null)),
                Collections.singletonList(row("abcde")));

        // When: 固定長系
        Sheet fixed = writeAndReopenSheet(container("derivedFixed",
                new FileDataBlock(DataType.SETUP_FIXED, "", "in.dat", map(),
                        Collections.singletonList(fixedRecord))));

        // Then: 識別セルと長さ行が食い違わない
        assertThat(line(fixed, 0), is(Arrays.asList("SETUP_FIXED=in.dat")));
        assertThat(line(fixed, 1), is(Arrays.asList("data", "f1")));
        assertThat(line(fixed, 2), is(Arrays.asList("", "半角英字")));
        assertThat("SETUP_FIXED なので長さ行が出る", line(fixed, 3), is(Arrays.asList("", "5")));
        assertThat(line(fixed, 4), is(Arrays.asList("", "abcde")));

        // When: 可変長系
        Sheet variable = writeAndReopenSheet(container("derivedVariable",
                new FileDataBlock(DataType.SETUP_VARIABLE, "", "in.csv", map(),
                        Collections.singletonList(variableRecord))));

        // Then: 長さ行が無く、データ行が 1 行上がる
        assertThat(line(variable, 0), is(Arrays.asList("SETUP_VARIABLE=in.csv")));
        assertThat(line(variable, 1), is(Arrays.asList("data", "f1")));
        assertThat(line(variable, 2), is(Arrays.asList("", "半角英字")));
        assertThat("SETUP_VARIABLE なので長さ行は出ない", line(variable, 3), is(Arrays.asList("", "abcde")));
    }

    /**
     * Given: グループ ID 付き {@code EXPECTED_VARIABLE} の可変長ファイルブロック。
     * When : 実 {@code .xlsx} へ書き出し、POI で開き直す。
     * Then : 識別セルが {@code EXPECTED_VARIABLE[g2]=exp.csv} になり、
     *        可変長なので<b>長さ行を持たない</b>版面になる。
     *
     * <p>
     * 担保する軸要素: A-09。{@code XlsFormatWriterTest} には {@code EXPECTED_VARIABLE} を書くテストが
     * 1 件も無く、{@code RoundTripTest#xls_expectedVariable_isPreserved} 経由の 🔺 だけだった。
     * </p>
     */
    @Test
    public void writesExpectedVariableFileBlockWithoutLengthRow() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Collections.singletonList(new FieldDef("f1", "半角英字", null)),
                Collections.singletonList(row("abcde")));
        FileDataBlock file = new FileDataBlock(DataType.EXPECTED_VARIABLE, "g2", "exp.csv", map(),
                Collections.singletonList(record));

        // When
        Sheet sheet = writeAndReopenSheet(container("expectedVariable", file));

        // Then
        assertThat(line(sheet, 0), is(Arrays.asList("EXPECTED_VARIABLE[g2]=exp.csv")));
        assertThat(line(sheet, 1), is(Arrays.asList("data", "f1")));
        assertThat(line(sheet, 2), is(Arrays.asList("", "半角英字")));
        assertThat("可変長なので長さ行は出ず、3 行目がデータ行", line(sheet, 3), is(Arrays.asList("", "abcde")));
    }

    // ------------------------------------------------- 軸A 送信同期 3 種の識別セル（#23 レビュー指摘で追加）

    /*
     * 以下 3 件は A-12／A-13／A-14 の識別セル（マーカー文字列）を 1 タイプ 1 メソッドで固定する。
     *
     * 追加前、この 3 タイプを辺③で通していたのは XlsFormatWriterTest#writesSequenceNoForAllSendSyncTypes
     * だけで、同メソッドがアサートするのはデータ行の列 0 の連番 "1"（4 タイプ共通の値）であり、
     * タイプを区別する出力は 1 つも固定していなかった。実測（2026-08-13）: XlsFormatWriter#marker が
     * この 3 タイプにだけ別文字列 "MUTATED" を返すよう src/main を一時的に変異させて全件実行したところ、
     * 落ちたのは RoundTripTest の 3 件（xls_expectedRequestBodyMessages_isPreserved／
     * xls_responseHeaderMessages_isPreserved／xls_responseBodyMessages_isPreserved）だけで、
     * XlsFormatWriterTest 40 件は全緑だった（変異は確認後に戻し、src/main は無変更）。
     * 往復テストは steering Rules フェーズ2 により正式な担保に数えないため、テストを足して埋める。
     *
     * 粒度は XlsFormatWriterTest#writesSendSyncMessageWithSequenceNo（A-11）に揃え、
     * グループ ID と識別子を含むマーカー全体と、データ行の列 0 に連番が入る版面を固定する。
     *
     * 版面に FW 制御ヘッダ行が出ないのは「送信系だから」ではなく、下の sendSyncMessage が渡す
     * fwHeaderFields が空 Map だからである。XlsFormatWriter#layoutMessage は fwHeaderFields を
     * データタイプで分岐せず無条件に出力する（同メソッドを読んで確認）。
     * 「送信系は FW 制御ヘッダを書かない」という性質は本クラスでも src/test 全体でも未担保であり、
     * issues.md の XLS-24 と inventory.md §3.1-3 に担保の穴として開示した。
     */

    /**
     * Given: グループ ID 付き {@code EXPECTED_REQUEST_BODY_MESSAGES}（送信系・no 列）のメッセージブロック。
     * When : 実 {@code .xlsx} へ書き出し、POI で開き直す。
     * Then : 識別セルが {@code EXPECTED_REQUEST_BODY_MESSAGES[case1]=RM21AA0104_01}。
     *        <b>本テストの入力が FW 制御ヘッダを持たない（空 Map）ため</b>次は名前行になる。
     *        データ行の列 0 は送信系のため連番になる。
     *
     * <p>担保する軸要素: A-12。</p>
     */
    @Test
    public void writesExpectedRequestBodyMessagesMarker() {
        // Given
        MessageDataBlock message = sendSyncMessage(DataType.EXPECTED_REQUEST_BODY_MESSAGES);

        // When
        Sheet sheet = writeAndReopenSheet(container("expectedRequestBody", message));

        // Then
        assertSendSyncLayout(sheet, "EXPECTED_REQUEST_BODY_MESSAGES[case1]=RM21AA0104_01");
    }

    /**
     * Given: グループ ID 付き {@code RESPONSE_HEADER_MESSAGES}（送信系・no 列）のメッセージブロック。
     * When : 実 {@code .xlsx} へ書き出し、POI で開き直す。
     * Then : 識別セルが {@code RESPONSE_HEADER_MESSAGES[case1]=RM21AA0104_01}。
     *        <b>本テストの入力が FW 制御ヘッダを持たない（空 Map）ため</b>次は名前行になる。
     *        データ行の列 0 は送信系のため連番になる。
     *
     * <p>担保する軸要素: A-13。</p>
     */
    @Test
    public void writesResponseHeaderMessagesMarker() {
        // Given
        MessageDataBlock message = sendSyncMessage(DataType.RESPONSE_HEADER_MESSAGES);

        // When
        Sheet sheet = writeAndReopenSheet(container("responseHeader", message));

        // Then
        assertSendSyncLayout(sheet, "RESPONSE_HEADER_MESSAGES[case1]=RM21AA0104_01");
    }

    /**
     * Given: グループ ID 付き {@code RESPONSE_BODY_MESSAGES}（送信系・no 列）のメッセージブロック。
     * When : 実 {@code .xlsx} へ書き出し、POI で開き直す。
     * Then : 識別セルが {@code RESPONSE_BODY_MESSAGES[case1]=RM21AA0104_01}。
     *        <b>本テストの入力が FW 制御ヘッダを持たない（空 Map）ため</b>次は名前行になる。
     *        データ行の列 0 は送信系のため連番になる。
     *
     * <p>担保する軸要素: A-14。</p>
     */
    @Test
    public void writesResponseBodyMessagesMarker() {
        // Given
        MessageDataBlock message = sendSyncMessage(DataType.RESPONSE_BODY_MESSAGES);

        // When
        Sheet sheet = writeAndReopenSheet(container("responseBody", message));

        // Then
        assertSendSyncLayout(sheet, "RESPONSE_BODY_MESSAGES[case1]=RM21AA0104_01");
    }

    /**
     * 送信同期の 3 テストが共有する入力（データタイプだけが違う）。
     *
     * @param type データタイプ（送信系）
     * @return メッセージブロック
     */
    private static MessageDataBlock sendSyncMessage(DataType type) {
        RecordLayout record = new RecordLayout("no",
                Arrays.asList(new FieldDef("requestId", "半角", "20"), new FieldDef("resendFlag", "半角", "1")),
                Arrays.asList(row("RM21AA0104_01", "0"), row("RM21AA0104_02", "1")));
        return new MessageDataBlock(type, "case1", "RM21AA0104_01",
                map(), map(), Collections.singletonList(record));
    }

    /**
     * 送信系の版面をアサートする（識別セルだけがタイプごとに違う）。
     *
     * @param sheet  読み直したシート
     * @param marker 期待する識別セル文字列
     */
    private static void assertSendSyncLayout(Sheet sheet, String marker) {
        assertThat("識別セルはタイプ名＋グループ ID＋識別子", line(sheet, 0), is(Arrays.asList(marker)));
        assertThat("入力の FW 制御ヘッダが空 Map なので次は名前行",
                line(sheet, 1), is(Arrays.asList("no", "requestId", "resendFlag")));
        assertThat(line(sheet, 2), is(Arrays.asList("", "半角", "半角")));
        assertThat(line(sheet, 3), is(Arrays.asList("", "20", "1")));
        assertThat("送信系なのでデータ行の列 0 は連番", line(sheet, 4), is(Arrays.asList("1", "RM21AA0104_01", "0")));
        assertThat(line(sheet, 5), is(Arrays.asList("2", "RM21AA0104_02", "1")));
    }

    // ------------------------------------------------------------------ 軸C・軸E の「空」

    /**
     * Given: セクションを 1 件も持たないコンテナ。
     * When : 実 {@code .xlsx} へ書き出し、POI で開き直す。
     * Then : 例外にならずファイルが作られ、<b>シートを 1 枚も持たない</b>ブックになる。
     *
     * <p>
     * 担保する軸要素: C-02（{@code sections} 空）。{@code XlsFormatWriter#build} は
     * {@code container.getSections()} をループするだけで空を弾かない。Excel の仕様上シート 0 枚の
     * ブックが妥当かは別問題であり、{@code issues.md} の <b>XLS-23</b> に記録した（修正はしない）。
     * </p>
     */
    @Test
    public void writesWorkbookWithoutSheetsWhenContainerHasNoSections() {
        // Given
        TestDataContainer container = new TestDataContainer("noSections",
                Collections.<TestDataSection>emptyList());

        // When
        Workbook workbook = writeAndReopen(container);

        // Then
        assertThat("シート 0 枚のブックが黙って書き出される（issues.md XLS-23）",
                workbook.getNumberOfSheets(), is(0));
    }

    /**
     * Given: ブロックを 1 件も持たないセクション。
     * When : 実 {@code .xlsx} へ書き出し、POI で開き直す。
     * Then : シートは作られるが行が 1 行も無い。
     *
     * <p>担保する軸要素: C-04（{@code blocks} 空）／E-1(0 件)（セクション内ブロック数 0）。</p>
     */
    @Test
    public void writesEmptySheetWhenSectionHasNoBlocks() {
        // Given（container の可変長引数にブロックを 1 件も渡さない＝ブロック 0 件のセクション）
        TestDataContainer container = container("noBlocks");

        // When
        Sheet sheet = writeAndReopenSheet(container);

        // Then
        assertThat("シートは作られる（名前だけのシートになる）", sheet.getSheetName(), is(SHEET));
        assertThat("行は 1 行も無い", sheet.getPhysicalNumberOfRows(), is(0));
        assertThat(sheet.getRow(0), is(nullValue()));
    }

    /**
     * Given: カラム名 2 件・データ行 0 件のテーブルブロック。
     * When : 実 {@code .xlsx} へ書き出し、POI で開き直す。
     * Then : 識別行とカラム名行だけが書かれ、データ行は 1 行も無い。
     *
     * <p>担保する軸要素: C-09（{@code rows} 空）／E-2(0 件)（ブロック内行数 0）。</p>
     */
    @Test
    public void writesTableWithoutDataRowsWhenRowsAreEmpty() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                row("A", "B"), Collections.<List<String>>emptyList());

        // When
        Sheet sheet = writeAndReopenSheet(container("noRows", table));

        // Then
        assertThat(line(sheet, 0), is(Arrays.asList("SETUP_TABLE=T")));
        assertThat(line(sheet, 1), is(Arrays.asList("A", "B")));
        assertThat("データ行は書かれない", sheet.getRow(2), is(nullValue()));
    }

    /**
     * Given: ディレクティブあり・レコードレイアウト 0 件の固定長ファイルブロック。
     * When : 実 {@code .xlsx} へ書き出し、POI で開き直す。
     * Then : 識別行とディレクティブ行だけが書かれ、名前行・型行・長さ行・データ行はいずれも無い。
     *
     * <p>担保する軸要素: C-12（{@code FileDataBlock.records} 空）／E-3(0 件)（ファイル内レコードレイアウト数 0）。</p>
     */
    @Test
    public void writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty() {
        // Given
        FileDataBlock file = new FileDataBlock(DataType.SETUP_FIXED, "", "f.dat", map("text-encoding", "UTF-8"),
                Collections.<RecordLayout>emptyList());

        // When
        Sheet sheet = writeAndReopenSheet(container("noRecords", file));

        // Then
        assertThat(line(sheet, 0), is(Arrays.asList("SETUP_FIXED=f.dat")));
        assertThat(line(sheet, 1), is(Arrays.asList("text-encoding", "UTF-8")));
        assertThat("レコードの行は 1 行も書かれない", sheet.getRow(2), is(nullValue()));
    }

    /**
     * Given: ディレクティブ 2 件・FW 制御ヘッダ 1 件・本文 1 レコードを持つ {@code MESSAGE} ブロック。
     * When : 実 {@code .xlsx} へ書き出し、POI で開き直す。
     * Then : 識別行 → <b>ディレクティブ行（記述順）</b> → FW 制御ヘッダ行 → 名前行 → 型行 → 長さ行 →
     *        データ行の順に書かれる。
     *
     * <p>
     * 担保する軸要素: C-13（{@code MessageDataBlock.directives} 値あり）。
     * {@code XlsFormatWriterTest} の {@code new MessageDataBlock(...)} は 6 箇所すべてで
     * ディレクティブが空 Map であり、値ありの版面（ディレクティブ行が FW 制御ヘッダ行より<b>上</b>に
     * 出ること）は 1 件も通っていなかった。
     * </p>
     */
    @Test
    public void writesDirectiveRowsBeforeFwHeaderRowsInMessage() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Collections.singletonList(new FieldDef("body1", "半角英字", "10")),
                Collections.singletonList(row("hello")));
        MessageDataBlock message = new MessageDataBlock(DataType.MESSAGE, "", "msg1",
                map("file-type", "Fixed", "text-encoding", "UTF-8"),
                map("requestId", "R01"), Collections.singletonList(record));

        // When
        Sheet sheet = writeAndReopenSheet(container("messageDirectives", message));

        // Then
        assertThat(line(sheet, 0), is(Arrays.asList("MESSAGE=msg1")));
        assertThat("ディレクティブ行が記述順に並ぶ", line(sheet, 1), is(Arrays.asList("file-type", "Fixed")));
        assertThat(line(sheet, 2), is(Arrays.asList("text-encoding", "UTF-8")));
        assertThat("FW 制御ヘッダ行はディレクティブ行の後", line(sheet, 3), is(Arrays.asList("requestId", "R01")));
        assertThat(line(sheet, 4), is(Arrays.asList("data", "body1")));
        assertThat(line(sheet, 5), is(Arrays.asList("", "半角英字")));
        assertThat(line(sheet, 6), is(Arrays.asList("", "10")));
        assertThat(line(sheet, 7), is(Arrays.asList("", "hello")));
    }

    /**
     * Given: フィールド 1 件・データ行 0 件のレコードレイアウトを持つ固定長ファイルブロック。
     * When : 実 {@code .xlsx} へ書き出し、POI で開き直す。
     * Then : 名前行・型行・長さ行までが書かれ、データ行は 1 行も無い。
     *
     * <p>担保する軸要素: C-18（{@code RecordLayout.rows} 空）／E-2(0 件)（ファイル経路のブロック内行数 0）。</p>
     */
    @Test
    public void writesRecordWithoutDataRowsWhenRecordRowsAreEmpty() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Collections.singletonList(new FieldDef("f1", "半角英字", "5")),
                Collections.<List<String>>emptyList());
        FileDataBlock file = new FileDataBlock(DataType.SETUP_FIXED, "", "f.dat", map(),
                Collections.singletonList(record));

        // When
        Sheet sheet = writeAndReopenSheet(container("noRecordRows", file));

        // Then
        assertThat(line(sheet, 0), is(Arrays.asList("SETUP_FIXED=f.dat")));
        assertThat(line(sheet, 1), is(Arrays.asList("data", "f1")));
        assertThat(line(sheet, 2), is(Arrays.asList("", "半角英字")));
        assertThat(line(sheet, 3), is(Arrays.asList("", "5")));
        assertThat("データ行は書かれない", sheet.getRow(4), is(nullValue()));
    }

    // ------------------------------------------------------------------ E-1(1 件)

    /**
     * Given: ブロックを 1 件だけ持つセクション（カラム名 2 件・データ行 1 行のテーブル）。
     * When : 実 {@code .xlsx} へ書き出し、POI で開き直す。
     * Then : シートの行は<b>全部で 3 行</b>（識別行・カラム名行・データ行 1 行）であり、
     *        2 ブロック目は書かれない。
     *
     * <p>
     * 担保する軸要素: E-1(1 件)（セクション内ブロック数 1）。
     * <b>件数は出力側で固定する</b> —— {@code getPhysicalNumberOfRows()} がシート全体の行数を返すため、
     * 2 ブロック目が書かれれば（ブロック間の空行は行を作らないので）6 になって落ちる。
     * 「次の行が {@code null}」では 2 ブロック目の不在を示せない。
     * {@code XlsFormatWriter#writeSection} はブロック間の空行を行を生成せずに {@code rowNum} を
     * 進めるだけなので、2 ブロック目があっても直後の行は {@code null} のままだからである。
     * </p>
     *
     * <p>
     * カラム名は辞書順の逆（{@code B} → {@code A}）に並べてある。定義順と辞書順が一致していると、
     * 並びを崩す変異を入れても落ちないためである（Rules「順序を主張するテストは、フィクスチャを
     * 最初から定義順・辞書順とずらして作る」）。
     * </p>
     */
    @Test
    public void writesOnlyOneBlockWhenSectionHasSingleBlock() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                row("B", "A"), Collections.singletonList(row("b1", "a1")));

        // When
        Sheet sheet = writeAndReopenSheet(container("oneBlock", table));

        // Then
        assertThat("シート全体で 3 行（識別行・カラム名行・データ行 1 行）",
                sheet.getPhysicalNumberOfRows(), is(3));
        assertThat(line(sheet, 0), is(Arrays.asList("SETUP_TABLE=T")));
        assertThat(line(sheet, 1), is(Arrays.asList("B", "A")));
        assertThat(line(sheet, 2), is(Arrays.asList("b1", "a1")));
    }
}
