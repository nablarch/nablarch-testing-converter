package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.absent;
import static nablarch.test.tool.converter.xls.XlsFixture.blank;
import static nablarch.test.tool.converter.xls.XlsFixture.bool;
import static nablarch.test.tool.converter.xls.XlsFixture.date;
import static nablarch.test.tool.converter.xls.XlsFixture.error;
import static nablarch.test.tool.converter.xls.XlsFixture.formula;
import static nablarch.test.tool.converter.xls.XlsFixture.number;
import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;

import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 辺①（Excel→中間モデル）の軸D — セル種別 17 ケースが中間モデルへどう入るかを固定するテスト。
 *
 * <p>
 * {@code XlsFormatReaderTest} が Fake リーダ（{@code List<List<String>>} の canned 行）で駆動するのに対し、
 * 本クラスは {@link XlsFixture} が POI で組み立てた実 {@code .xlsx} を入力にし、本番配線の
 * {@link XlsFormatReader}（{@code PoiXlsReader}）を通す。すなわち「実セル → 文字列行」の区間を実行する。
 * </p>
 *
 * <p>
 * 各ケースは {@code SETUP_TABLE} の 1 データ行（{@code KEY} 列＝ケース識別・{@code V} 列＝検証対象セル）
 * として与える。{@code V} 列だけのシートにすると、空セルのケースで行全体が空行となり
 * {@code PoiXlsReader} に読み飛ばされてしまうため、行を空にしない {@code KEY} 列を必ず置く。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>期待される仕様ではない。
 * 妥当でないと判断した挙動は {@code .rn/ntf-test-data-converter/coverage/issues.md} に課題として
 * 記録してあり、実装（src/main）は変更していない。
 * </p>
 *
 * <p>
 * 日付・時刻・日時セルの文字列化はプラットフォーム既定ロケールに依存する（POI の
 * {@code XSSFCell#toString()} が {@code new SimpleDateFormat("dd-MMM-yyyy")} を使うため）。
 * 値を固定できるようにテスト実行中だけ既定ロケールを {@link Locale#ENGLISH} に固定し、終了時に戻す。
 * ロケール依存であること自体は課題として記録済み。
 * </p>
 *
 * @author kiyobot
 */
public class XlsFormatReaderCellTypeTest {

    /** フィクスチャ {@code .xlsx} の出力先（テストごとに一意）。 */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** 退避した既定ロケール。 */
    private Locale originalLocale;

    /** 既定ロケールを固定する（日付・時刻・日時セルの文字列化がロケール依存のため）。 */
    @Before
    public void fixLocale() {
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    /** 既定ロケールを元に戻す。 */
    @After
    public void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    // ------------------------------------------------------------------ helpers

    /**
     * 検証対象セル 1 個を {@code SETUP_TABLE} のデータ行に置いた {@code .xlsx} を組み立て、
     * 実 {@link XlsFormatReader} で読んだ中間モデルの当該セル値を返す。
     *
     * @param book  ブック名（{@code PoiXlsReader} のブックキャッシュ衝突を避けるためテストごとに一意）
     * @param value 検証対象セル
     * @return 中間モデル（{@link TableDataBlock}）の 1 行目 {@code V} 列の値
     */
    private String readValue(String book, XlsFixture.CellSpec value) {
        Path dir = folder.getRoot().toPath();
        XlsFixture.book(book).sheet("s")
                .row(text("SETUP_TABLE=T"))
                .row(text("KEY"), text("V"))
                .row(text("k"), value)
                .writeTo(dir);

        TestDataContainer container = new XlsFormatReader().read(dir.toString(), book + "/s");
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        assertThat(blocks.size(), is(1));
        TableDataBlock table = (TableDataBlock) blocks.get(0);
        assertThat(table.getColumnNames(), is(Arrays.asList("KEY", "V")));
        assertThat(table.getRows().size(), is(1));
        return table.getRows().get(0).get(1);
    }

    /**
     * 日時を組み立てる。
     *
     * @param year   年
     * @param month  月（1 始まり）
     * @param day    日
     * @param hour   時
     * @param minute 分
     * @param second 秒
     * @return 日時
     */
    private static Date at(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day, hour, minute, second);
        return calendar.getTime();
    }

    // ------------------------------------------------------------------ D1-01〜D1-05（最優先）

    /**
     * Given: 文字列セル {@code abc}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : そのまま {@code abc} が入る。
     */
    @Test
    public void readsStringCellAsIs() {
        assertThat(readValue("cellString", text("abc")), is("abc"));
    }

    /**
     * Given: 整数値 {@code 1} の数値セル（表示形式なし）。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code "1.0"} が入る（POI の {@code toString()} が {@code double} を文字列化するため）。
     *        整数として記述した値が {@code .0} 付きに変わる。
     */
    @Test
    public void readsIntegerNumericCellAsDoubleString() {
        assertThat(readValue("cellInteger", number(1)), is("1.0"));
    }

    /**
     * Given: 小数値 {@code 1.5} の数値セル。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code "1.5"} が入る。
     */
    @Test
    public void readsDecimalNumericCellAsDoubleString() {
        assertThat(readValue("cellDecimal", number(1.5)), is("1.5"));
    }

    /**
     * Given: {@code double} の有効桁を超える大きい数値セル。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 指数表記の文字列が入る（元の桁は復元できない）。
     */
    @Test
    public void readsLargeNumericCellAsScientificNotation() {
        assertThat(readValue("cellLargeNumber", number(12345678901234567890d)), is("1.2345678901234567E19"));
    }

    /**
     * Given: 先頭ゼロを保った文字列セル {@code 007}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code 007} のまま入る（先頭ゼロが落ちない）。
     */
    @Test
    public void readsLeadingZeroStringCellAsIs() {
        assertThat(readValue("cellLeadingZero", text("007")), is("007"));
    }

    // ------------------------------------------------------------------ D1-06〜D1-08（日付・時刻・日時）

    /**
     * Given: 表示形式 {@code yyyy/mm/dd} を持つ日付セル（2026-08-07）。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : セルの表示形式ではなく POI 既定の {@code dd-MMM-yyyy} 表記になる（既定ロケール依存）。
     */
    @Test
    public void readsDateFormattedCellAsPoiDefaultDatePattern() {
        assertThat(readValue("cellDate", date(at(2026, 8, 7, 0, 0, 0), "yyyy/mm/dd")), is("07-Aug-2026"));
    }

    /**
     * Given: 表示形式 {@code hh:mm:ss} を持つ時刻セル（シリアル値 0.5 ＝ 12:00:00）。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 時刻成分が捨てられ、Excel シリアル値の日付部（1899-12-31）だけが残る。
     */
    @Test
    public void readsTimeFormattedCellLosingTimeComponent() {
        assertThat(readValue("cellTime", number(0.5, "hh:mm:ss")), is("31-Dec-1899"));
    }

    /**
     * Given: 表示形式 {@code yyyy/mm/dd hh:mm:ss} を持つ日時セル（2026-08-07 12:34:56）。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 日付部だけが残り、時刻部は失われる。
     */
    @Test
    public void readsDateTimeFormattedCellLosingTimeComponent() {
        assertThat(readValue("cellDateTime", date(at(2026, 8, 7, 12, 34, 56), "yyyy/mm/dd hh:mm:ss")),
                is("07-Aug-2026"));
    }

    // ------------------------------------------------------------------ D1-09〜D1-11（数式・真偽値・エラー）

    /**
     * Given: 数式セル {@code =1+1}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 計算結果 {@code 2} ではなく数式文字列 {@code 1+1} が入る。
     */
    @Test
    public void readsFormulaCellAsFormulaText() {
        assertThat(readValue("cellFormula", formula("1+1")), is("1+1"));
    }

    /**
     * Given: 真偽値セル {@code TRUE}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 大文字の {@code TRUE} が入る。
     */
    @Test
    public void readsBooleanCellAsUpperCaseLiteral() {
        assertThat(readValue("cellBoolean", bool(true)), is("TRUE"));
    }

    /**
     * Given: エラー値セル {@code #DIV/0!}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 例外にはならず、エラー表示文字列がそのまま入る。
     */
    @Test
    public void readsErrorCellAsErrorText() {
        assertThat(readValue("cellError", error(FormulaError.DIV0)), is("#DIV/0!"));
    }

    // ------------------------------------------------------------------ D1-12〜D1-13（空セル・最優先）

    /**
     * Given: {@code V} 列にセルが存在しない行。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code null} ではなく空文字が入る（Fake リーダ経路で {@code null} 行要素を与えた場合と異なる）。
     */
    @Test
    public void readsAbsentCellAsEmptyString() {
        assertThat(readValue("cellAbsent", absent()), is(""));
    }

    /**
     * Given: 空文字を持つ文字列セル。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 空文字が入る。
     */
    @Test
    public void readsEmptyStringCellAsEmptyString() {
        assertThat(readValue("cellEmptyString", text("")), is(""));
    }

    /**
     * Given: セルは存在するが値を持たない空白セル（書式のみ設定された Excel の空白セルと同じ状態）。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 空文字が入る。セル不在・空文字セル・空白セルの 3 者は中間モデル上で区別されない。
     */
    @Test
    public void readsBlankCellAsEmptyString() {
        assertThat(readValue("cellBlank", blank()), is(""));
    }

    // ------------------------------------------------------------------ D1-14〜D1-16

    /**
     * Given: 前後に半角空白を持つ文字列セル。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 前後空白が保たれる（トリムされない）。
     */
    @Test
    public void readsSurroundingWhitespacePreserved() {
        assertThat(readValue("cellWhitespace", text("  pad  ")), is("  pad  "));
    }

    /**
     * Given: 改行を含む文字列セル。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 改行を含んだまま 1 つの値として入る。
     */
    @Test
    public void readsEmbeddedNewlinePreserved() {
        assertThat(readValue("cellNewline", text("line1\nline2")), is("line1\nline2"));
    }

    /**
     * Given: リテラル文字列 {@code null} を持つ文字列セル。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 文字列 {@code "null"} として入る（Java の {@code null} にはならない）。
     */
    @Test
    public void readsLiteralNullStringAsString() {
        assertThat(readValue("cellLiteralNull", text("null")), is("null"));
    }

    // ------------------------------------------------------------------ D1-17（最優先）

    /**
     * Given: 値が数値 {@code 1}・表示形式が {@code @}（テキスト）のセル。
     *        実プロジェクトの Excel テストデータに実在するパターン。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 表示形式 {@code @} は考慮されず {@code "1.0"} が入る（画面表示 {@code 1} と一致しない）。
     */
    @Test
    public void readsTextFormattedNumericCellAsDoubleString() {
        assertThat(readValue("cellTextFormattedNumber", number(1, "@")), is("1.0"));
    }
}
