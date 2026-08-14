package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.absent;
import static nablarch.test.tool.converter.xls.XlsFixture.blank;
import static nablarch.test.tool.converter.xls.XlsFixture.number;
import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 辺①（Excel→中間モデル）の軸D — セル種別 8 ケースが中間モデルへどう入るかを固定するテスト。
 *
 * <p>
 * {@code XlsFormatReaderTest} が Fake リーダ（{@code List<List<String>>} の canned 行）で駆動するのに対し、
 * 本クラスは {@link XlsFixture} が POI で組み立てた実 {@code .xlsx} を入力にし、本番配線の
 * {@link XlsFormatReader}（{@code PoiXlsReader}）を通す。すなわち「実セル → 文字列行」の区間を実行する。
 * </p>
 *
 * <p>
 * <b>対象は「NTF が実行できるテストデータ」に限る。</b>{@code PoiXlsReader} のクラス Javadoc が
 * 「全セルが文字列書式であること」を前提として明示しているため、それを外れる入力
 * （表示形式を持たない数値セル・日付／時刻／日時書式セル・数式セル・真偽値セル・エラー値セル）は
 * 担保対象にしない。<b>値が数値で表示形式が {@code @}（文字列書式）のセル</b>も同じく仕様外である
 * （解説書 {@code testdata_notation.rst:75} と直後の {@code important}。
 * {@code coverage/issues.md} <b>XLS-01</b>）。ただし作成者から見れば文字列書式であり
 * 参照フィクスチャの実物にも存在する形であるため、
 * {@link #readsTextFormattedNumericCellAsDoubleString()} を<b>実挙動の記録として</b>残す
 * （担保ではない。同テストの Javadoc を参照）。
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

    // ------------------------------------------------------------------ D1-01・D1-05

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
     * Given: 先頭ゼロを保った文字列セル {@code 007}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code 007} のまま入る（先頭ゼロが落ちない）。
     */
    @Test
    public void readsLeadingZeroStringCellAsIs() {
        assertThat(readValue(text("007")), is("007"));
    }

    // ------------------------------------------------------------------ D1-12〜D1-13（空セル）

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

    // ------------------------------------------------------------------ D1-17

    /**
     * Given: 値が数値 {@code 1}・表示形式が {@code @}（文字列書式）のセル。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : <b>値は保証しない。</b>実測では表示形式 {@code @} は考慮されず {@code "1.0"} が入る
     *        （画面表示 {@code 1} と一致しない）。
     *
     * <p>
     * <b>これは仕様外入力であり、converter は対応しない（{@code coverage/issues.md} <b>XLS-01</b>）。</b>
     * 解説書 {@code testdata_notation.rst:75} は「Excelのセルの書式は、必ず文字列書式に統一して
     * おく必要がある」と定め、直後の {@code important}（{@code :79}）は
     * 「Excelファイルに文字列以外の書式でデータを記述すると、Excelがセルの値を自動的に変換して
     * しまう（例えば数値書式では先頭の {@code 0} が消えて {@code 0001} が {@code 1} になる、
     * 日付書式では表示形式が変わるなど）ため、正しくデータを読み取れなくなる」と明言している。
     * セル種別が数値である本ケースは「文字列以外の書式でデータを記述した」状態であり、
     * 解説書自身が読み取れないと言っている入力である。converter の入出力は
     * NTF が実行できるテストデータに限るため、正しい値を決めることができない。
     * </p>
     *
     * <p>
     * <b>したがって本テストは要件ではなく実挙動の記録である。</b>下のアサートが将来 {@code "1.0"} 以外へ
     * 変わっても不具合ではない。値を仕様どおりに直す修正を入れたときは、期待値を書き換えて
     * 記録を更新すればよい（表示形式 {@code @} が文字列書式である以上、
     * 作成者が仕様どおりのつもりで書ける形であるため、記録そのものは残す価値がある）。
     * 参照フィクスチャ {@code ProjectActionRequestTest.xlsx} の {@code downloadNormal} シート
     * {@code A19} がこの形（{@code t} 属性なしの数値セルで、適用スタイルの {@code numFmtId} が
     * 49 ＝ {@code @}）であり、Excel が実際に保存した版面に存在するパターンである。
     * </p>
     *
     * <p>
     * 「数値セルである」という前提を立てるには、そのセルが実際に数値セルかつ表示形式 {@code @} を
     * 持っていなければならない。読み取り値だけでは区別できないため、書き出した {@code .xlsx} を
     * 読み戻して当該セルの種別と {@code getDataFormatString()} も確認する（こちらは前提の確認であり、
     * 記録ではなく担保である）。
     * </p>
     */
    @Test
    public void readsTextFormattedNumericCellAsDoubleString() {
        // When
        String value = readValue(number(1, "@"));

        // Then
        assertThat("仕様外入力（セル種別が数値）のため値は保証しない。これは要件ではなく実挙動の記録である",
                value, is("1.0"));

        Cell cell = writtenSheet().getRow(2).getCell(1);
        assertThat("検証対象セルが数値セルであること", cell.getCellType(), is(Cell.CELL_TYPE_NUMERIC));
        assertThat("検証対象セルが表示形式 @ を持つこと", cell.getCellStyle().getDataFormatString(), is("@"));
    }
}
