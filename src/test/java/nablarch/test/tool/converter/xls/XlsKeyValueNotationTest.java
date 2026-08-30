package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import nablarch.test.core.file.DataFile;
import nablarch.test.core.file.DataFileInspector;
import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.FrameworkOracle;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@code key, value} 形式のメタ行（ディレクティブ行・FW 制御ヘッダ行）が、Excel 形式の往復で
 * 意味を保つことを、実 {@code .xlsx} を起点に固定するテスト。
 *
 * <p>
 * <b>データ行と同じ問題がメタ行にもある。</b>中間モデルが持つのはテスティングフレームワークが
 * 解釈したあとの値であり、Excel 形式の記法ではない。読みで外れた記法を書きで戻さないと、
 * 書き出した {@code .xlsx} をフレームワークが読んだときに意味がずれる
 * （引用符記法は外側 1 層が外れるため、1 往復ごとに 1 層ずつ減っていく）。
 * </p>
 *
 * <p>
 * <b>正解はフレームワーク本体である。</b>期待値を自分で書かず、元の {@code .xlsx} を本体が読んだ値と、
 * 変換ツールで XLS→XLS した {@code .xlsx} を本体が読んだ値が一致することを確かめる。
 * </p>
 *
 * <p>
 * データ行側の対称性は {@code XlsNotationSymmetryTest} ／ {@code SpecialNotationRoundTripTest} の
 * 担当であり、本クラスは重複しない。本クラスが受け持つのはメタ行だけである。
 * </p>
 *
 * @author kiyobot
 */
public class XlsKeyValueNotationTest {

    /** フィクスチャの既定ブック名。 */
    private static final String BOOK = "keyValueBook";

    /** フィクスチャの既定シート名。 */
    private static final String SHEET = "sheet1";

    /** 電文の識別子。 */
    private static final String MESSAGE_ID = "M1";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // ------------------------------------------------------------------ helpers

    /**
     * 段ごとに別ディレクトリを使う（本体ローダのキャッシュが同一リソース名の 2 回目の読みで
     * 1 回目の結果を返すため）。
     *
     * @param step 段番号
     * @return ディレクトリ
     */
    private Path dir(int step) {
        return folder.getRoot().toPath().resolve("s" + step);
    }

    /** 既定のブック／シートのリソース名。 */
    private static String resource() {
        return BOOK + "/" + SHEET;
    }

    /**
     * 変換ツールで {@code from} の実 {@code .xlsx} を読み、{@code to} へ実 {@code .xlsx} として書き出す。
     *
     * @param from 入力ディレクトリ
     * @param to   出力ディレクトリ
     */
    private static void convertXlsToXls(Path from, Path to) {
        TestDataContainer read = new XlsFormatReader().read(from.toString(), resource());
        TestDataBlock block = read.getSections().get(0).getBlocks().get(0);
        TestDataContainer rewrapped = new TestDataContainer(BOOK,
                Collections.singletonList(new TestDataSection(SHEET, Collections.singletonList(block))));
        new XlsFormatWriter().write(rewrapped, to.toString());
    }

    /**
     * 指定ディレクトリの {@code SETUP_VARIABLE} を本体に読ませ、ディレクティブを返す。
     *
     * @param dir ディレクトリ
     * @return 本体が保持しているディレクティブ
     */
    private static Map<String, Object> frameworkDirectives(Path dir) {
        List<? extends DataFile> files =
                FrameworkOracle.files(dir.toString(), resource(), "", DataType.SETUP_VARIABLE);
        assertThat("ファイル数", files.size(), is(1));
        return DataFileInspector.directives(files.get(0));
    }

    // ------------------------------------------------------------------ ディレクティブ行

    /**
     * Given: {@code SETUP_VARIABLE} の {@code quoting-delimiter} セルにダブルクォート 5 個
     *        （{@code """""}）を書いた実 {@code .xlsx}。本体は引用符記法の外側 1 層だけを外し、
     *        ダブルクォート 3 個（{@code """}）として保持する。
     * When : 変換ツールで XLS→XLS する。
     * Then : 書き出した {@code .xlsx} を本体が読んだディレクティブが、元の {@code .xlsx} を本体が
     *        読んだものと一致する。
     *
     * <p>
     * 書きが値を素で書くと、セルはダブルクォート 3 個になり、読み戻しで本体がもう 1 層外して
     * ダブルクォート 1 個になる。往復のたびに 1 層ずつ減る。
     * </p>
     */
    @Test
    public void keepsQuotationNotationOfDirectiveValueThroughXlsRoundTrip() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("SETUP_VARIABLE=input/data.csv"))
                .row(text("field-separator"), text(","))
                .row(text("quoting-delimiter"), text("\"\"\"\"\""))
                .row(text("DATA"), text("USER_ID"), text("USER_NAME"))
                .row(text(""), text("半角"), text("全角"))
                .row(text(""), text("001"), text("山田太郎"))
                .writeTo(dir(1));

        Map<String, Object> expected = frameworkDirectives(dir(1));
        // 本体が外すのは外側 1 層だけである（正解の値そのものを固定しておく）
        assertThat("本体が読んだ quoting-delimiter", expected.get("quoting-delimiter"), is((Object) "\"\"\""));

        // When
        convertXlsToXls(dir(1), dir(2));

        // Then
        assertThat("XLS→XLS 後のディレクティブ", frameworkDirectives(dir(2)), is(expected));
    }

    // ------------------------------------------------------------------ FW 制御ヘッダ行

    /**
     * Given: {@code MESSAGE} の FW 制御ヘッダ {@code requestId} セルに、{@code R1} をダブルクォート
     *        3 個ずつで囲んだ値（{@code """R1"""}）を書いた実 {@code .xlsx}。本体は外側 1 層だけを外し、
     *        {@code ""R1""} として保持する。
     * When : 変換ツールで XLS→XLS する。
     * Then : 書き出した {@code .xlsx} を本体が読んだ FW 制御ヘッダが、元の {@code .xlsx} を本体が
     *        読んだものと一致する。
     */
    @Test
    public void keepsQuotationNotationOfFwHeaderValueThroughXlsRoundTrip() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("MESSAGE=" + MESSAGE_ID))
                .row(text("requestId"), text("\"\"\"R1\"\"\""))
                .row(text("userId"), text("U1"))
                .row(text("data"), text("f1"))
                .row(XlsFixture.blank(), text("半角英字"))
                .row(XlsFixture.blank(), text("5"))
                .row(XlsFixture.blank(), text("x"))
                .writeTo(dir(1));

        Map<String, String> expected = FrameworkOracle.messageFwHeader(dir(1).toString(), resource(), MESSAGE_ID);
        // 本体が外すのは外側 1 層だけである（正解の値そのものを固定しておく）
        assertThat("本体が読んだ requestId", expected.get("requestId"), is("\"\"R1\"\""));

        // When
        convertXlsToXls(dir(1), dir(2));

        // Then
        assertThat("XLS→XLS 後の FW 制御ヘッダ",
                FrameworkOracle.messageFwHeader(dir(2).toString(), resource(), MESSAGE_ID), is(expected));
    }
}
