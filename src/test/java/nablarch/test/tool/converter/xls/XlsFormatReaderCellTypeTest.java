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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

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
 * {@code PoiXlsReader} に読み飛ばされてしまうため、行を空にしない {@code KEY} 列を必ず置く
 * （全カラムが空のデータ行が黙って消えること自体は課題として
 * {@code .rn/ntf-test-data-converter/coverage/issues.md} の XLS-05 に記録した）。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>期待される仕様ではない。
 * 妥当でないと判断した挙動は {@code .rn/ntf-test-data-converter/coverage/issues.md} に課題として
 * 記録してあり、実装（src/main）は変更していない。
 * </p>
 *
 * @author kiyobot
 */
public class XlsFormatReaderCellTypeTest {

    /** フィクスチャのブック名。 */
    private static final String BOOK = "cellTypes";

    /** フィクスチャのシート名。 */
    private static final String SHEET = "s";

    /**
     * フィクスチャ {@code .xlsx} の出力先。
     *
     * <p>
     * {@link TemporaryFolder} はテストメソッドごとに別ディレクトリを与えるため、全メソッドが同じ
     * ブック名を使ってもファイルパスは衝突しない。加えて converter 経路は
     * {@code TestCoreReaderAdapter} の全パーサが {@code parse(..., false)} を渡し
     * {@code TestDataParsingTemplate} が {@code PoiXlsReader#setUseCache(false)} を呼ぶため、
     * {@code PoiXlsReader} のブックキャッシュは使われない（同一パスを書き換えて同一 JVM 内で
     * 2 回読むと新しい内容が読めることをプローブで実測して確認済み）。ブック名を一意化する必要はない。
     * </p>
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * {@link EnglishLocale} を付けたテストの実行中だけ、プラットフォーム既定ロケールを
     * {@link Locale#ENGLISH} に固定する（終了時に元へ戻す）。
     *
     * <p>
     * 日付・時刻・日時セルの文字列化は POI の {@code XSSFCell#toString()} が
     * {@code new SimpleDateFormat("dd-MMM-yyyy")} を使うため既定ロケールに依存する
     * （{@code -Duser.language=ja -Duser.country=JP} で実行すると {@code 07-8-2026} になることを実測）。
     * {@link Locale#setDefault} は JVM グローバルな変更のため、必要な 3 件（D1-06〜D1-08）だけに
     * 掛かるようこのルールで絞る。
     * </p>
     *
     * <p>
     * <b>タイムゾーンは固定しない。</b>POI は日付セルの往復（{@code DateUtil#getExcelDate} ↔
     * {@code DateUtil#getJavaDate}）に同一の既定タイムゾーンを使い、書き込み時と読み取り時のずれが
     * 相殺されるため、既定タイムゾーンが何であっても結果は変わらない
     * （{@code UTC}／{@code America/Los_Angeles}／{@code Pacific/Kiritimati}／{@code Europe/Istanbul}
     * で本クラス 19 件が全 PASS することを実測して確認済み）。将来この固定を足す必要はない。
     * </p>
     */
    @Rule
    public final TestRule englishLocale = new TestRule() {

        /** 既定ロケールの退避・固定・復元。 */
        private final ExternalResource fixLocale = new ExternalResource() {

            /** 退避した既定ロケール。 */
            private Locale original;

            @Override
            protected void before() {
                original = Locale.getDefault();
                Locale.setDefault(Locale.ENGLISH);
            }

            @Override
            protected void after() {
                Locale.setDefault(original);
            }
        };

        @Override
        public Statement apply(Statement base, Description description) {
            return description.getAnnotation(EnglishLocale.class) == null
                    ? base
                    : fixLocale.apply(base, description);
        }
    };

    /**
     * 既定ロケールを {@link Locale#ENGLISH} に固定して実行すべきテストに付ける印。
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface EnglishLocale {
    }

    // ------------------------------------------------------------------ helpers

    /**
     * 検証対象セル 1 個を {@code SETUP_TABLE} のデータ行に置いた {@code .xlsx} を組み立てて書き出す。
     *
     * @param value 検証対象セル（{@code V} 列に置く）
     * @return 出力先ディレクトリ
     */
    private Path writeSingleValueBook(XlsFixture.CellSpec value) {
        Path dir = folder.getRoot().toPath();
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("SETUP_TABLE=T"))
                .row(text("KEY"), text("V"))
                .row(text("k"), value)
                .writeTo(dir);
        return dir;
    }

    /**
     * 実 {@link XlsFormatReader} で読み、唯一の {@code SETUP_TABLE} ブロックを返す。
     *
     * @param dir フィクスチャの置かれたディレクトリ
     * @return テーブルデータブロック
     */
    private static TableDataBlock readTable(Path dir) {
        TestDataContainer container = new XlsFormatReader().read(dir.toString(), BOOK + "/" + SHEET);
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        assertThat(blocks.size(), is(1));
        return (TableDataBlock) blocks.get(0);
    }

    /**
     * 検証対象セル 1 個の {@code .xlsx} を組み立て、実 {@link XlsFormatReader} で読んだ中間モデルの
     * {@code V} 列の値を返す。
     *
     * @param value 検証対象セル
     * @return 中間モデル（{@link TableDataBlock}）の 1 行目 {@code V} 列の値
     */
    private String readValue(XlsFixture.CellSpec value) {
        TableDataBlock table = readTable(writeSingleValueBook(value));
        assertThat(table.getColumnNames(), is(Arrays.asList("KEY", "V")));
        assertThat(table.getRows().size(), is(1));
        return table.getRows().get(0).get(1);
    }

    /**
     * 直前に書き出したフィクスチャ {@code .xlsx} を POI で開き直し、シートを返す。
     * 「リーダに何を食わせたか」をブック側で確認するために使う。
     *
     * @return シート
     */
    private Sheet writtenSheet() {
        Sheet sheet = XlsFixture.open(folder.getRoot().toPath().resolve(BOOK + ".xlsx")).getSheet(SHEET);
        assertThat("書き出したフィクスチャに検証対象シートがあること", sheet, is(notNullValue()));
        return sheet;
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
        assertThat(readValue(text("abc")), is("abc"));
    }

    /**
     * Given: 整数値 {@code 1} の数値セル（表示形式なし）。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code "1.0"} が入る（POI の {@code toString()} が {@code double} を文字列化するため）。
     *        整数として記述した値が {@code .0} 付きに変わる。
     */
    @Test
    public void readsIntegerNumericCellAsDoubleString() {
        assertThat(readValue(number(1)), is("1.0"));
    }

    /**
     * Given: 小数値 {@code 1.5} の数値セル。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code "1.5"} が入る。
     */
    @Test
    public void readsDecimalNumericCellAsDoubleString() {
        assertThat(readValue(number(1.5)), is("1.5"));
    }

    /**
     * Given: {@code double} の有効桁を超える大きい数値セル。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 指数表記の文字列が入る（元の桁は復元できない）。
     */
    @Test
    public void readsLargeNumericCellAsScientificNotation() {
        assertThat(readValue(number(12345678901234567890d)), is("1.2345678901234567E19"));
    }

    /**
     * Given: 先頭ゼロを保った文字列セル {@code 007}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code 007} のまま入る（先頭ゼロが落ちない）。
     */
    @Test
    public void readsLeadingZeroStringCellAsIs() {
        assertThat(readValue(text("007")), is("007"));
    }

    // ------------------------------------------------------------------ D1-06〜D1-08（日付・時刻・日時）

    /**
     * Given: 表示形式 {@code yyyy/mm/dd} を持つ日付セル（2026-08-07）。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : セルの表示形式ではなく POI 既定の {@code dd-MMM-yyyy} 表記になる（既定ロケール依存）。
     */
    @Test
    @EnglishLocale
    public void readsDateFormattedCellAsPoiDefaultDatePattern() {
        assertThat(readValue(date(at(2026, 8, 7, 0, 0, 0), "yyyy/mm/dd")), is("07-Aug-2026"));
    }

    /**
     * Given: 表示形式 {@code hh:mm:ss} を持つ時刻セル（シリアル値 0.5 ＝ 12:00:00）。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 時刻成分が捨てられ、Excel シリアル値の日付部（1899-12-31）だけが残る。
     */
    @Test
    @EnglishLocale
    public void readsTimeFormattedCellLosingTimeComponent() {
        assertThat(readValue(number(0.5, "hh:mm:ss")), is("31-Dec-1899"));
    }

    /**
     * Given: 表示形式 {@code yyyy/mm/dd hh:mm:ss} を持つ日時セル（2026-08-07 12:34:56）。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 日付部だけが残り、時刻部は失われる。
     */
    @Test
    @EnglishLocale
    public void readsDateTimeFormattedCellLosingTimeComponent() {
        assertThat(readValue(date(at(2026, 8, 7, 12, 34, 56), "yyyy/mm/dd hh:mm:ss")), is("07-Aug-2026"));
    }

    // ------------------------------------------------------------------ D1-09〜D1-11（数式・真偽値・エラー）

    /**
     * Given: 数式セル {@code =1+1}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 計算結果 {@code 2} ではなく数式文字列 {@code 1+1} が入る。
     */
    @Test
    public void readsFormulaCellAsFormulaText() {
        assertThat(readValue(formula("1+1")), is("1+1"));
    }

    /**
     * Given: 真偽値セル {@code TRUE}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 大文字の {@code TRUE} が入る。
     */
    @Test
    public void readsBooleanCellAsUpperCaseLiteral() {
        assertThat(readValue(bool(true)), is("TRUE"));
    }

    /**
     * Given: エラー値セル {@code #DIV/0!}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 例外にはならず、エラー表示文字列がそのまま入る。
     */
    @Test
    public void readsErrorCellAsErrorText() {
        assertThat(readValue(error(FormulaError.DIV0)), is("#DIV/0!"));
    }

    // ------------------------------------------------------------------ D1-12〜D1-13（空セル・最優先）

    /**
     * Given: 行末（最終列）にセルが存在しない行。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code null} ではなく空文字が入る（Fake リーダ経路で {@code null} 行要素を与えた場合と異なる）。
     *
     * <p>
     * 不在セルが行末にあると行の使用範囲（{@code Row#getLastCellNum()}）自体が縮むため、
     * {@code PoiXlsReader#readOneLine} の {@code i < lastCellNum} ループは {@code V} 列に到達せず、
     * {@code cell == null ? ""} の分岐は<b>通らない</b>。ここでの空文字は、行の長さがヘッダより短いときに
     * 下流（本体パーサ）が行を埋める結果である。この分岐自体は
     * {@link #readsAbsentCellInMiddleOfRowAsEmptyString()} が別経路として通す。
     * </p>
     */
    @Test
    public void readsAbsentCellAsEmptyString() {
        assertThat(readValue(absent()), is(""));

        // 行の使用範囲が KEY 列までしかない（＝リーダのループが V 列に到達しない）ことの確認。
        Row row = writtenSheet().getRow(2);
        assertThat(row.getLastCellNum(), is((short) 1));
    }

    /**
     * Given: 行の途中（{@code V} 列）にセルが存在せず、その右（{@code W} 列）にはセルがある行。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 穴の位置に空文字が入り、右隣の値はそのまま入る。
     *
     * <p>
     * この形だけが {@code PoiXlsReader#readOneLine} の {@code cell == null ? ""}
     * 分岐を実際に通す（行の使用範囲が {@code W} 列まで届いているため、ループが穴の位置を走査する）。
     * 行末の不在セルを与える {@link #readsAbsentCellAsEmptyString()} とは経路が異なる。
     * </p>
     */
    @Test
    public void readsAbsentCellInMiddleOfRowAsEmptyString() {
        // Given
        Path dir = folder.getRoot().toPath();
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("SETUP_TABLE=T"))
                .row(text("KEY"), text("V"), text("W"))
                .row(text("k"), absent(), text("z"))
                .writeTo(dir);

        // 行の使用範囲が W 列まで届き、その内側の V 列が不在であることの確認
        // （＝リーダのループが cell == null の位置を走査する）。
        Row row = writtenSheet().getRow(2);
        assertThat(row.getLastCellNum(), is((short) 3));
        assertThat(row.getCell(1), is(nullValue()));

        // When
        TableDataBlock table = readTable(dir);

        // Then
        assertThat(table.getColumnNames(), is(Arrays.asList("KEY", "V", "W")));
        assertThat(table.getRows(), is(Arrays.<List<String>>asList(Arrays.asList("k", "", "z"))));
    }

    /**
     * Given: 空文字を持つ文字列セル。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 空文字が入る。
     */
    @Test
    public void readsEmptyStringCellAsEmptyString() {
        assertThat(readValue(text("")), is(""));
    }

    /**
     * Given: セルは存在するが値を持たない空白セル（書式のみ設定された Excel の空白セルと同じ状態）。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 空文字が入る。セル不在・空文字セル・空白セルの 3 者は中間モデル上で区別されない。
     */
    @Test
    public void readsBlankCellAsEmptyString() {
        assertThat(readValue(blank()), is(""));
    }

    // ------------------------------------------------------------------ D1-14〜D1-16

    /**
     * Given: 前後に半角空白を持つ文字列セル。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 前後空白が保たれる（トリムされない）。
     */
    @Test
    public void readsSurroundingWhitespacePreserved() {
        assertThat(readValue(text("  pad  ")), is("  pad  "));
    }

    /**
     * Given: 改行を含む文字列セル。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 改行を含んだまま 1 つの値として入る。
     */
    @Test
    public void readsEmbeddedNewlinePreserved() {
        assertThat(readValue(text("line1\nline2")), is("line1\nline2"));
    }

    /**
     * Given: リテラル文字列 {@code null} を持つ文字列セル。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 文字列 {@code "null"} として入る（Java の {@code null} にはならない）。
     */
    @Test
    public void readsLiteralNullStringAsString() {
        assertThat(readValue(text("null")), is("null"));
    }

    // ------------------------------------------------------------------ D1-17（最優先）

    /**
     * Given: 値が数値 {@code 1}・表示形式が {@code @}（テキスト）のセル。
     *        実プロジェクトの Excel テストデータに実在するパターン。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 表示形式 {@code @} は考慮されず {@code "1.0"} が入る（画面表示 {@code 1} と一致しない）。
     *
     * <p>
     * 「表示形式が無視される」という主張を立てるには、そのセルが実際に表示形式 {@code @} を
     * 持っていなければならない。読み取り値だけでは表示形式なしの数値セル（D1-02）と区別できないため、
     * 書き出した {@code .xlsx} を読み戻して当該セルが数値セルかつ {@code getDataFormatString()} が
     * {@code "@"} であることも確認する。
     * </p>
     */
    @Test
    public void readsTextFormattedNumericCellAsDoubleString() {
        // When
        String value = readValue(number(1, "@"));

        // Then
        assertThat(value, is("1.0"));

        Cell cell = writtenSheet().getRow(2).getCell(1);
        assertThat("検証対象セルが数値セルであること", cell.getCellType(), is(Cell.CELL_TYPE_NUMERIC));
        assertThat("検証対象セルが表示形式 @ を持つこと", cell.getCellStyle().getDataFormatString(), is("@"));
    }
}
