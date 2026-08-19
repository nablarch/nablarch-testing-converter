package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.cell;
import static nablarch.test.tool.converter.xls.XlsFixture.line;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nablarch.test.core.reader.DataType;
import nablarch.test.tool.converter.model.FieldDef;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.MessageDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@link XlsFormatWriter} のテストクラス。
 *
 * <p>
 * 40 件の内訳は <b>{@code build}（メモリ上のブック）を見る 28 件</b>・
 * <b>{@code write} で実 {@code .xlsx} を書く 10 件</b>・
 * <b>SUT のブックを作らない 2 件</b>である（28 ＋ 10 ＋ 2 ＝ 40）。
 * </p>
 * <ul>
 *   <li><b>{@code build} 28 件</b> — メモリ上のブックからセル値・背景色・罫線・列幅を直接アサートする。</li>
 *   <li><b>{@code write} 10 件</b> — 往復テスト 8 件（{@code roundTrips*}。{@link #roundTrip} 経由で
 *       書き出し、実 {@link XlsFormatReader} でモデル → Excel → モデルの同値を検証する）と、
 *       {@link #writesWorkbookFileWithSheetPerSection}（ファイルとシートの生成を確かめる）・
 *       {@link #wrapsIoFailure}（書き出し失敗の例外）。
 *       {@code grep -n "new XlsFormatWriter()\.write(" <本ファイル>} は 3 か所を返し、うち 1 か所が
 *       {@link #roundTrip} ヘルパで 8 件が共有する。</li>
 *   <li><b>どちらも呼ばない 2 件</b> — {@link #eachGroupHasDistinctDefaultColor} と
 *       {@link #rejectsNegativeBlankRows}。{@link ExcelFormatConfig} だけを叩くため
 *       {@link XlsFormatWriter} のブックを作らない。</li>
 * </ul>
 *
 * <p>
 * 件数の導出コマンドと実測は {@code .rn/ntf-test-data-converter/coverage/inventory.md} §3.1 の末尾にある。
 * 本体パーサのキャッシュ衝突を避けるため、往復テストはテストごとに一意のブック名・
 * {@link TemporaryFolder} を使う。
 * </p>
 *
 * @author kiyobot
 */
public class XlsFormatWriterTest {

    /** 往復テストの出力先（テストごとに一意）。 */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // ------------------------------------------------------------------ helpers

    /**
     * null を含められるよう {@link Arrays#asList} で行を組み立てる。
     *
     * @param cells セル
     * @return 行
     */
    private static List<String> row(String... cells) {
        return Arrays.asList(cells);
    }

    /**
     * 1 セクション 1 コンテナを組み立てる。
     *
     * @param book   ブック名
     * @param sheet  シート名
     * @param blocks ブロック
     * @return コンテナ
     */
    private static TestDataContainer container(String book, String sheet, TestDataBlock... blocks) {
        TestDataSection section = new TestDataSection(sheet, Arrays.asList(blocks));
        return new TestDataContainer(book, Collections.singletonList(section));
    }

    /** キー順を保つマップを作る。 */
    private static Map<String, String> map(String... kv) {
        Map<String, String> m = new LinkedHashMap<String, String>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return m;
    }

    /** 既定設定のライタでブックを組み立てる。 */
    private static Workbook build(TestDataContainer container) {
        return new XlsFormatWriter().build(container);
    }

    /** 単一セクションのシートを取り出す。 */
    private static Sheet onlySheet(Workbook wb, String name) {
        return wb.getSheet(name);
    }

    // セル読み出し（cell / line）は XlsFixture の static メソッドを使う（本ファイル冒頭で static import）。

    // ------------------------------------------------------------------ table

    /**
     * Given: {@code ${...}}・空文字・null セルを含む SETUP_TABLE。
     * When : build。
     * Then : 識別行 → カラム名行 → データ行の版面。null はリテラル {@code null}・空文字は空セル。
     */
    @Test
    public void writesTableBlock() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "USERS",
                row("USER_NAME", "AGE"),
                Arrays.asList(row("${userName}", ""), row("literal", null)));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", table)), "sheet");

        // Then
        assertThat(cell(sheet, 0, 0), is("SETUP_TABLE=USERS"));
        assertThat(line(sheet, 1), is(Arrays.asList("USER_NAME", "AGE")));
        assertThat(line(sheet, 2), is(Arrays.asList("${userName}", "")));
        // null はリテラル null（空文字 "" と区別して書く）
        assertThat(line(sheet, 3), is(Arrays.asList("literal", "null")));
    }

    /**
     * Given: 1カラムテーブル（META行の値は1セル）。
     * When : build。
     * Then : META行（row 0）は値を持つセルのみ生成される（空の末尾セルは作られない）。
     */
    @Test
    public void metaRowContainsOnlyValueCells() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "ONE_COL",
                row("A"), Collections.singletonList(row("v")));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", table)), "sheet");

        // Then: META行（row 0）は1セルのみ（ブロック幅分に矩形整形されない）
        assertThat((int) sheet.getRow(0).getLastCellNum(), is(1));
    }

    /**
     * Given: グループ ID 付き EXPECTED_TABLE。
     * When : build。
     * Then : 識別セルが {@code TYPE[group]=id}。
     */
    @Test
    public void writesTableMarkerWithGroupId() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.EXPECTED_TABLE_DATA, "[g1]", "USERS",
                row("USER_NAME"), Collections.singletonList(row("${u}")));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", table)), "sheet");

        // Then
        assertThat(cell(sheet, 0, 0), is("EXPECTED_TABLE[g1]=USERS"));
    }

    /**
     * Given: EXPECTED_COMPLETE_TABLE。
     * When : build。
     * Then : 識別セルがデータタイプ名どおり。
     */
    @Test
    public void writesExpectedCompleteTableMarker() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.EXPECTED_COMPLETED, "", "USERS",
                row("C"), Collections.singletonList(row("v")));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", table)), "sheet");

        // Then
        assertThat(cell(sheet, 0, 0), is("EXPECTED_COMPLETE_TABLE=USERS"));
    }

    // ------------------------------------------------------------------ list_map

    /**
     * Given: LIST_MAP。
     * When : build。
     * Then : 識別行 → カラム名行 → データ行。
     */
    @Test
    public void writesListMapBlock() {
        // Given
        ListMapBlock listMap = new ListMapBlock("", "result",
                row("ID", "NAME"), Arrays.asList(row("${id}", ""), row("2", "bob")));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", listMap)), "sheet");

        // Then
        assertThat(cell(sheet, 0, 0), is("LIST_MAP=result"));
        assertThat(line(sheet, 1), is(Arrays.asList("ID", "NAME")));
        assertThat(line(sheet, 2), is(Arrays.asList("${id}", "")));
        assertThat(line(sheet, 3), is(Arrays.asList("2", "bob")));
    }

    /**
     * Given: マーカーカラム {@code [NOTE]} を含むテーブル。
     * When : build。
     * Then : マーカーカラムのセルにマーカー背景色が付く。
     */
    @Test
    public void tintsMarkerColumn() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                row("ID", "[NOTE]"), Collections.singletonList(row("1", "memo")));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", table)), "sheet");

        // Then
        short marker = ExcelFormatConfig.defaults().getMarkerColumnColorIndex();
        // データ行のマーカーカラム（列 1）はマーカー背景色
        assertThat(sheet.getRow(2).getCell(1).getCellStyle().getFillForegroundColor(), is(marker));
        assertThat(sheet.getRow(2).getCell(1).getCellStyle().getFillPattern(), is(CellStyle.SOLID_FOREGROUND));
        // 通常データセル（列 0）は背景色なし
        assertThat(sheet.getRow(2).getCell(0).getCellStyle().getFillPattern(), is(CellStyle.NO_FILL));
    }

    // ------------------------------------------------------------------ fixed file

    /**
     * Given: ディレクティブ＋固定長レコード（型・長さ・{@code -} 含む）の SETUP_FIXED。
     * When : build。
     * Then : 識別行 → ディレクティブ行 → 名前行 → 型行 → 長さ行 → データ行。データ行の列 0 は空。
     */
    @Test
    public void writesFixedFileBlock() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Arrays.asList(new FieldDef("f1", "半角英字", "-"), new FieldDef("f2", "半角英字", "5")),
                Collections.singletonList(row("abcd", "xy")));
        FileDataBlock file = new FileDataBlock(DataType.SETUP_FIXED, "", "test.dat",
                FileDataBlock.FileType.FIXED, map("text-encoding", "UTF-8"),
                Collections.singletonList(record));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", file)), "sheet");

        // Then
        assertThat(cell(sheet, 0, 0), is("SETUP_FIXED=test.dat"));
        assertThat(cell(sheet, 1, 0), is("text-encoding"));
        assertThat(cell(sheet, 1, 1), is("UTF-8"));
        assertThat(line(sheet, 2), is(Arrays.asList("data", "f1", "f2")));
        assertThat(line(sheet, 3), is(Arrays.asList("", "半角英字", "半角英字")));
        assertThat(line(sheet, 4), is(Arrays.asList("", "-", "5")));
        assertThat(line(sheet, 5), is(Arrays.asList("", "abcd", "xy")));
    }

    /**
     * Given: 可変長ファイル（長さなし）。
     * When : build。
     * Then : 長さ行を持たない（名前行 → 型行 → データ行）。
     *
     * <p>
     * フィールド長の番人の<b>範囲</b>も兼ねる。弾くのは固定長ファイルと電文だけであり、
     * <b>可変長ファイルでは {@code length} が {@code null} であることが正しい</b>
     * （{@code testdata_notation.rst:883}（{@code 30a8271} 時点）
     * 「可変長ファイルでは、フィールド名称・データ型の2リストが同サイズで必須であり、フィールド長は不要である」）。
     * 番人の範囲を可変長まで広げるとこのテストが落ちる。
     * </p>
     */
    @Test
    public void writesVariableFileWithoutLengthRow() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Collections.singletonList(new FieldDef("f1", "半角英字", null)),
                Collections.singletonList(row("${v}")));
        FileDataBlock file = new FileDataBlock(DataType.SETUP_VARIABLE, "", "in.csv",
                FileDataBlock.FileType.VARIABLE, map(), Collections.singletonList(record));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", file)), "sheet");

        // Then
        assertThat(cell(sheet, 0, 0), is("SETUP_VARIABLE=in.csv"));
        assertThat(line(sheet, 1), is(Arrays.asList("data", "f1")));
        assertThat(line(sheet, 2), is(Arrays.asList("", "半角英字")));
        // 長さ行は無く、3 行目がデータ行
        assertThat(line(sheet, 3), is(Arrays.asList("", "${v}")));
    }

    /**
     * Given: 2 レコードレイアウトを持つ固定長ファイル。
     * When : build。
     * Then : 各レコードの名前行（レコード種別が列 0）が独立に書かれる。
     */
    @Test
    public void writesMultipleRecordLayouts() {
        // Given
        RecordLayout header = new RecordLayout("header",
                Collections.singletonList(new FieldDef("h1", "半角英字", "5")),
                Collections.singletonList(row("AAAAA")));
        RecordLayout data = new RecordLayout("data",
                Collections.singletonList(new FieldDef("d1", "半角英字", "2")),
                Collections.singletonList(row("xy")));
        FileDataBlock file = new FileDataBlock(DataType.SETUP_FIXED, "", "multi.dat",
                FileDataBlock.FileType.FIXED, map(), Arrays.asList(header, data));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", file)), "sheet");

        // Then: header: 名前(1)/型(2)/長さ(3)/データ(4)。data: 名前(5)/型(6)/長さ(7)/データ(8)
        assertThat(line(sheet, 1), is(Arrays.asList("header", "h1")));
        assertThat(line(sheet, 5), is(Arrays.asList("data", "d1")));
    }

    /**
     * Given: 2 レコード目のレコード種別が null の固定長ファイル。
     * When : build。
     * Then : IllegalStateException（列 0 が空の名前行は本体パーサが直前レコードのデータ行と誤読し、
     *        読み戻せない版面になるため、黙って書かず早期に失敗する）。
     */
    @Test(expected = IllegalStateException.class)
    public void rejectsNullRecordTypeOnSecondRecord() {
        // Given
        RecordLayout first = new RecordLayout("header",
                Collections.singletonList(new FieldDef("h1", "半角英字", "5")),
                Collections.singletonList(row("AAAAA")));
        RecordLayout second = new RecordLayout(null,   // 2 レコード目はレコード種別必須
                Collections.singletonList(new FieldDef("d1", "半角英字", "2")),
                Collections.singletonList(row("xy")));
        FileDataBlock file = new FileDataBlock(DataType.SETUP_FIXED, "", "bad.dat",
                FileDataBlock.FileType.FIXED, map(), Arrays.asList(first, second));

        // When / Then
        build(container("book", "sheet", file));
    }

    /**
     * Given: フィールドを 1 件も持たないレコードレイアウトの固定長ファイル。
     * When : build。
     * Then : IllegalArgumentException（フィールド 0 件のレコードレイアウトは Excel 記法として存在しない形で
     *        あり、書き出しても本体パーサが読み戻せないため、黙って書かず早期に失敗する）。
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsRecordWithoutFieldsInFileBlock() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Collections.<FieldDef>emptyList(), Collections.singletonList(row("v")));
        FileDataBlock file = new FileDataBlock(DataType.SETUP_FIXED, "", "bad.dat",
                FileDataBlock.FileType.FIXED, map(), Collections.singletonList(record));

        // When / Then
        build(container("book", "sheet", file));
    }

    /**
     * Given: フィールドを 1 件も持たないレコードレイアウトのメッセージブロック。
     * When : build。
     * Then : IllegalArgumentException（番人はファイル系・メッセージ系の双方に効く）。
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsRecordWithoutFieldsInMessageBlock() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Collections.<FieldDef>emptyList(), Collections.singletonList(row("v")));
        MessageDataBlock message = new MessageDataBlock(DataType.MESSAGE, "", "msg1",
                map(), map(), Collections.singletonList(record));

        // When / Then
        build(container("book", "sheet", message));
    }

    /**
     * Given: フィールド長が {@code null} のフィールドを持つ固定長ファイル。
     * When : build。
     * Then : IllegalArgumentException（Excel 記法は固定長ファイルについて「フィールド名称・データ型・
     *        フィールド長の3リストが同サイズで必須」と定めており（{@code testdata_notation.rst:883}。
     *        {@code 30a8271} 時点）、長さを持たないフィールド定義は書き表せないため、
     *        空の長さセルを黙って書かず早期に失敗する）。
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsFieldWithoutLengthInFixedFileBlock() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Collections.singletonList(new FieldDef("f1", "半角英字", null)),
                Collections.singletonList(row("v")));
        FileDataBlock file = new FileDataBlock(DataType.SETUP_FIXED, "", "bad.dat",
                FileDataBlock.FileType.FIXED, map(), Collections.singletonList(record));

        // When / Then
        build(container("book", "sheet", file));
    }

    /**
     * Given: フィールド長が {@code null} のフィールドを持つメッセージブロック。
     * When : build。
     * Then : IllegalArgumentException（メッセージボディは「フィールド名称・データ型・フィールド長・データ
     *        という、前述のファイルデータと同じ構成」を持つ（{@code testdata_notation.rst:1158}。
     *        {@code 30a8271} 時点）ため、固定長ファイルと同じ制約に掛かる）。
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsFieldWithoutLengthInMessageBlock() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Collections.singletonList(new FieldDef("f1", "半角英字", null)),
                Collections.singletonList(row("v")));
        MessageDataBlock message = new MessageDataBlock(DataType.MESSAGE, "", "msg1",
                map(), map(), Collections.singletonList(record));

        // When / Then
        build(container("book", "sheet", message));
    }

    /**
     * Given: ファイル種別（{@link FileDataBlock.FileType}）が {@code null} の固定長ファイルブロック。
     * When : build。
     * Then : IllegalArgumentException（Excel 記法は固定長ファイルと可変長ファイルに固有の記法制約を
     *        置いており（{@code testdata_notation.rst:883}。{@code 30a8271} 時点）、どちらでもない
     *        ファイルデータブロックは書き表せないため、黙って可変長へ倒さず早期に失敗する）。
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsFileBlockWithoutFileType() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Collections.singletonList(new FieldDef("f1", "半角英字", "5")),
                Collections.singletonList(row("v")));
        FileDataBlock file = new FileDataBlock(DataType.SETUP_FIXED, "", "bad.dat",
                null, map(), Collections.singletonList(record));

        // When / Then
        build(container("book", "sheet", file));
    }

    /**
     * Given: カラム名を 1 件も持たないテーブルブロック（行も 0 件）。
     * When : build。
     * Then : IllegalArgumentException（Excel 記法はデータ行が無くてもカラム名の行を省略できない。
     *        {@code testdata_notation.rst:802}「データ行を書かない場合でも、カラム名の行は省略できない。
     *        識別子行の次の行がカラム名の行として読み込まれるため、カラム名の行を書かないと、
     *        その次に現れた行がカラム名の行になる」（{@code 30a8271} 時点）。
     *        カラム名行を書けないブロックを書き出すと<b>次のブロックを食う</b>ため、黙って書かず早期に失敗する。
     *        {@code coverage/issues.md} XLS-27）。
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsTableBlockWithoutColumnNames() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                Collections.<String>emptyList(), Collections.<List<String>>emptyList());

        // When / Then
        build(container("book", "sheet", table));
    }

    /**
     * Given: カラム名を 1 件も持たない {@code LIST_MAP} ブロック。
     * When : build。
     * Then : IllegalArgumentException（番人はテーブル系・{@code LIST_MAP} の双方に効く。
     *        {@code LIST_MAP} も {@code testdata_notation.rst:628}「1行目に {@code LIST_MAP=} に続けて
     *        シート内で一意になる ID を記載する。2行目を Map のキー、3行目以降を Map の値として
     *        読み込む」のとおりキー行が構成上必須である）。
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsListMapBlockWithoutColumnNames() {
        // Given
        ListMapBlock listMap = new ListMapBlock("", "lm",
                Collections.<String>emptyList(), Collections.<List<String>>emptyList());

        // When / Then
        build(container("book", "sheet", listMap));
    }

    /**
     * Given: 本文レコードを 1 件も持たないメッセージブロック。
     * When : build。
     * Then : IllegalArgumentException（レコード 0 件の電文は Excel 記法にも YAML 記法にも存在しない形で
     *        あるため、黙って書かず早期に失敗する）。
     *
     * <p>
     * 0 バイトの空ファイル特例は記法上あくまで<b>ファイル</b>に限った話であり
     * （{@code testdata_notation.rst:881}／{@code :1109}／{@code :1146}。{@code 30a8271} 時点）、
     * 電文についてレコード 0 件の記法は明文が無い。電文が存在しない場合は
     * {@code :1257} のとおり<b>データブロックごと省略する</b>のが記法である
     * （{@code coverage/issues.md} <b>YML-12</b> の 2 形目）。
     * ファイルブロックの 0 件は合法なので番人はここには置かない
     * （{@code XlsFormatWriterModelTest#writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty}）。
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsMessageBlockWithoutRecords() {
        // Given
        MessageDataBlock message = new MessageDataBlock(DataType.MESSAGE, "", "msg1",
                map(), map(), Collections.<RecordLayout>emptyList());

        // When / Then
        build(container("book", "sheet", message));
    }

    /**
     * Given: 本文レコードを 1 件も持たない送信系メッセージブロック。
     * When : build。
     * Then : IllegalArgumentException（番人は {@code MESSAGE} 経路・送信系 4 種の双方に効く。
     *        送信系のスキーマ定義 {@code $defs.expected_request_message_data} ／
     *        {@code $defs.group_message_data} も {@code records.minItems} ＝ 1 である）。
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsSendSyncMessageBlockWithoutRecords() {
        // Given
        MessageDataBlock message = new MessageDataBlock(DataType.RESPONSE_BODY_MESSAGES, "[g]", "msg1",
                map(), map(), Collections.<RecordLayout>emptyList());

        // When / Then
        build(container("book", "sheet", message));
    }

    /**
     * Given: 2 レコード目のレコード種別が空文字の固定長ファイル。
     * When : build。
     * Then : IllegalStateException（空文字も列 0 が空になるため null と同様に弾く）。
     */
    @Test(expected = IllegalStateException.class)
    public void rejectsEmptyRecordTypeOnSecondRecord() {
        // Given
        RecordLayout first = new RecordLayout("header",
                Collections.singletonList(new FieldDef("h1", "半角英字", "5")),
                Collections.singletonList(row("AAAAA")));
        RecordLayout second = new RecordLayout("",   // 空文字も列 0 空＝NG
                Collections.singletonList(new FieldDef("d1", "半角英字", "2")),
                Collections.singletonList(row("xy")));
        FileDataBlock file = new FileDataBlock(DataType.SETUP_FIXED, "", "bad.dat",
                FileDataBlock.FileType.FIXED, map(), Arrays.asList(first, second));

        // When / Then
        build(container("book", "sheet", file));
    }

    /**
     * Given: レコード種別が null の単一レコード固定長ファイル。
     * When : build。
     * Then : 例外にならない（1 レコード目の列 0 空は本体パーサが位置で名前行を特定でき、誤読しない）。
     */
    @Test
    public void allowsNullRecordTypeOnSingleRecord() {
        // Given
        RecordLayout only = new RecordLayout(null,
                Collections.singletonList(new FieldDef("f1", "半角英字", "2")),
                Collections.singletonList(row("xy")));
        FileDataBlock file = new FileDataBlock(DataType.SETUP_FIXED, "", "ok.dat",
                FileDataBlock.FileType.FIXED, map(), Collections.singletonList(only));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", file)), "sheet");

        // Then: 名前行の列 0 は空（レコード種別省略）
        assertThat(line(sheet, 1), is(Arrays.asList("", "f1")));
    }

    // ------------------------------------------------------------------ message

    /**
     * Given: FW 制御ヘッダ＋本文を持つ MESSAGE。
     * When : build。
     * Then : 識別行 → FW ヘッダ行 → 名前行 → 型行 → 長さ行 → データ行。データ行の列 0 は空。
     */
    @Test
    public void writesMessageBlock() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Arrays.asList(new FieldDef("body1", "半角英字", "10"), new FieldDef("body2", "半角英字", "5")),
                Collections.singletonList(row("${b}", "xyz")));
        MessageDataBlock message = new MessageDataBlock(DataType.MESSAGE, "", "msg1",
                map(), map("requestId", "${rid}"), Collections.singletonList(record));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", message)), "sheet");

        // Then
        assertThat(cell(sheet, 0, 0), is("MESSAGE=msg1"));
        assertThat(line(sheet, 1), is(Arrays.asList("requestId", "${rid}")));
        assertThat(line(sheet, 2), is(Arrays.asList("data", "body1", "body2")));
        assertThat(line(sheet, 3), is(Arrays.asList("", "半角英字", "半角英字")));
        assertThat(line(sheet, 4), is(Arrays.asList("", "10", "5")));
        // データ行の列 0 は空（本体は列 0 空をデータ行と判定する）
        assertThat(line(sheet, 5), is(Arrays.asList("", "${b}", "xyz")));
    }

    /**
     * Given: 送信系（EXPECTED_REQUEST_HEADER_MESSAGES・no 列）。
     * When : build。
     * Then : 本テストの入力が FW 制御ヘッダを持たない（空 Map）ため識別行の次は名前行になる。
     *        データ行の列 0 は送信系のため no（連番）になる。
     */
    @Test
    public void writesSendSyncMessageWithSequenceNo() {
        // Given
        RecordLayout record = new RecordLayout("no",
                Arrays.asList(new FieldDef("requestId", "半角", "20"), new FieldDef("resendFlag", "半角", "1")),
                Arrays.asList(row("RM21AA0104_01", "0"), row("RM21AA0104_02", "1")));
        MessageDataBlock message = new MessageDataBlock(DataType.EXPECTED_REQUEST_HEADER_MESSAGES,
                "[case1]", "RM21AA0104_01", map(), map(), Collections.singletonList(record));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", message)), "sheet");

        // Then
        assertThat(cell(sheet, 0, 0), is("EXPECTED_REQUEST_HEADER_MESSAGES[case1]=RM21AA0104_01"));
        assertThat(line(sheet, 1), is(Arrays.asList("no", "requestId", "resendFlag")));
        assertThat(line(sheet, 2), is(Arrays.asList("", "半角", "半角")));
        assertThat(line(sheet, 3), is(Arrays.asList("", "20", "1")));
        // データ行の列 0 は no（連番）
        assertThat(line(sheet, 4), is(Arrays.asList("1", "RM21AA0104_01", "0")));
        assertThat(line(sheet, 5), is(Arrays.asList("2", "RM21AA0104_02", "1")));
    }

    // ------------------------------------------------------------------ styling

    /**
     * Given: 既定設定。
     * When : build。
     * Then : 識別行は色なし、列名行に背景色、データ行は背景色なし。
     */
    @Test
    public void appliesHeaderBackgroundColor() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                row("C"), Collections.singletonList(row("v")));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", table)), "sheet");

        // Then
        // SETUP_TABLE_DATA は setupHeaderColorIndex を使う
        short header = ExcelFormatConfig.defaults().getSetupHeaderColorIndex();
        // 識別行（row 0）は色なし
        assertThat(sheet.getRow(0).getCell(0).getCellStyle().getFillPattern(), is(CellStyle.NO_FILL));
        // 列名行（row 1）に背景色
        assertThat(sheet.getRow(1).getCell(0).getCellStyle().getFillForegroundColor(), is(header));
        // データ行は背景色なし
        assertThat(sheet.getRow(2).getCell(0).getCellStyle().getFillPattern(), is(CellStyle.NO_FILL));
    }

    /**
     * Given: testShots 識別子の LIST_MAP。
     * When : build（既定設定）。
     * Then : 列名行に testShots グループの背景色（LIME）が付く。
     */
    @Test
    public void appliesTestShotsHeaderColor() {
        // Given
        ListMapBlock testShots = new ListMapBlock("", "testShots",
                row("no", "description"), Collections.singletonList(row("1", "test")));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", testShots)), "sheet");

        // Then
        short expected = ExcelFormatConfig.defaults().getTestShotsHeaderColorIndex();
        assertThat(sheet.getRow(1).getCell(0).getCellStyle().getFillForegroundColor(), is(expected));
    }

    /**
     * Given: SETUP_TABLE_DATA ブロック。
     * When : build（既定設定）。
     * Then : 列名行に SETUP 系グループの背景色（PALE_BLUE）が付く。
     */
    @Test
    public void appliesSetupHeaderColor() {
        // Given
        TableDataBlock setup = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                row("C"), Collections.singletonList(row("v")));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", setup)), "sheet");

        // Then
        short expected = ExcelFormatConfig.defaults().getSetupHeaderColorIndex();
        assertThat(sheet.getRow(1).getCell(0).getCellStyle().getFillForegroundColor(), is(expected));
    }

    /**
     * Given: EXPECTED_TABLE_DATA ブロック。
     * When : build（既定設定）。
     * Then : 列名行に EXPECTED 系グループの背景色（LIGHT_YELLOW）が付く。
     */
    @Test
    public void appliesExpectedHeaderColor() {
        // Given
        TableDataBlock expected = new TableDataBlock(DataType.EXPECTED_TABLE_DATA, "", "T",
                row("C"), Collections.singletonList(row("v")));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", expected)), "sheet");

        // Then
        short color = ExcelFormatConfig.defaults().getExpectedHeaderColorIndex();
        assertThat(sheet.getRow(1).getCell(0).getCellStyle().getFillForegroundColor(), is(color));
    }

    /**
     * Given: MESSAGE ブロック（その他グループ）。
     * When : build（既定設定）。
     * Then : FW ヘッダ行（ディレクティブ）の左列にその他グループの背景色（LAVENDER）が付く。
     */
    @Test
    public void appliesOtherHeaderColorForMessage() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Collections.singletonList(new FieldDef("f1", "半角英字", "5")),
                Collections.singletonList(row("hello")));
        MessageDataBlock message = new MessageDataBlock(DataType.MESSAGE, "", "msg",
                map(), map("requestId", "R01"), Collections.singletonList(record));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", message)), "sheet");

        // Then: FW ヘッダ行（row 1）の左列はその他グループ色
        short expected = ExcelFormatConfig.defaults().getOtherHeaderColorIndex();
        assertThat(sheet.getRow(1).getCell(0).getCellStyle().getFillForegroundColor(), is(expected));
    }

    /**
     * Given: 非 testShots 識別子の LIST_MAP（その他グループ）。
     * When : build（既定設定）。
     * Then : 列名行にその他グループの背景色（LAVENDER）が付き、testShots 色と異なる。
     */
    @Test
    public void appliesOtherHeaderColorForNonTestShotsListMap() {
        // Given
        ListMapBlock listMap = new ListMapBlock("", "result",
                row("ID"), Collections.singletonList(row("1")));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", listMap)), "sheet");

        // Then
        short expected = ExcelFormatConfig.defaults().getOtherHeaderColorIndex();
        short notExpected = ExcelFormatConfig.defaults().getTestShotsHeaderColorIndex();
        assertThat(sheet.getRow(1).getCell(0).getCellStyle().getFillForegroundColor(), is(expected));
        assertThat(sheet.getRow(1).getCell(0).getCellStyle().getFillForegroundColor(), is(not(notExpected)));
    }

    /**
     * Given: 4 グループのブロック。
     * When : build（既定設定）。
     * Then : 各ブロックの列名行に異なるグループ色が付く（4 色が相互に異なる）。
     */
    @Test
    public void eachGroupHasDistinctDefaultColor() {
        // Given: 既定値の 4 色を取得
        ExcelFormatConfig defaults = ExcelFormatConfig.defaults();
        short testShots = defaults.getTestShotsHeaderColorIndex();
        short setup = defaults.getSetupHeaderColorIndex();
        short exp = defaults.getExpectedHeaderColorIndex();
        short other = defaults.getOtherHeaderColorIndex();

        // Then: 4 色はすべて異なる
        assertThat(testShots, is(not(setup)));
        assertThat(testShots, is(not(exp)));
        assertThat(testShots, is(not(other)));
        assertThat(setup, is(not(exp)));
        assertThat(setup, is(not(other)));
        assertThat(exp, is(not(other)));
    }

    /**
     * Given: 既定設定（外枠罫線あり）。
     * When : build。
     * Then : ブロックの四隅セルに外枠の罫線が付く。
     */
    @Test
    public void drawsBlockOuterBorder() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                row("A", "B"), Collections.singletonList(row("1", "2")));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", table)), "sheet");

        // Then
        // row 0 は識別行（META）なので罫線なし
        assertThat(sheet.getRow(0).getCell(0).getCellStyle().getBorderTop(), is(CellStyle.BORDER_NONE));
        // 列名行（row 1）が外枠上辺の先頭：上辺・左辺に罫線
        CellStyle topLeft = sheet.getRow(1).getCell(0).getCellStyle();
        assertThat(topLeft.getBorderTop(), is(CellStyle.BORDER_THIN));
        assertThat(topLeft.getBorderLeft(), is(CellStyle.BORDER_THIN));
        // 右下セル（row 2 = データ行）：下辺・右辺に罫線
        CellStyle bottomRight = sheet.getRow(2).getCell(1).getCellStyle();
        assertThat(bottomRight.getBorderBottom(), is(CellStyle.BORDER_THIN));
        assertThat(bottomRight.getBorderRight(), is(CellStyle.BORDER_THIN));
        // 内側（データ行の上辺）にも内部グリッド線が引かれる（drawCellBorder=true がデフォルト）
        assertThat(sheet.getRow(2).getCell(0).getCellStyle().getBorderTop(), is(CellStyle.BORDER_THIN));
    }

    /**
     * Given: 2 ブロックを持つセクション（既定＝ブロック間 1 空行）。
     * When : build。
     * Then : 2 ブロック目の識別行が 1 行空けて始まる。
     */
    @Test
    public void insertsBlankRowBetweenBlocks() {
        // Given
        TableDataBlock t1 = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T1",
                row("C"), Collections.singletonList(row("v")));
        TableDataBlock t2 = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T2",
                row("C"), Collections.singletonList(row("v")));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", t1, t2)), "sheet");

        // Then: t1: 行 0,1,2。1 空行（行 3）。t2 の識別行は行 4。
        assertThat(cell(sheet, 0, 0), is("SETUP_TABLE=T1"));
        assertThat(sheet.getRow(3), is(nullValue()));
        assertThat(cell(sheet, 4, 0), is("SETUP_TABLE=T2"));
    }

    /**
     * Given: 自動列幅オン。
     * When : build。
     * Then : 列幅が最長値（＋余白）に応じて設定される。
     */
    @Test
    public void appliesAutoColumnWidth() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                row("C"), Collections.singletonList(row("abcdefghij")));  // 10 文字

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", table)), "sheet");

        // Then: 識別行 "SETUP_TABLE=T"（13 文字）が列 0 最長。(13+2)*256
        assertThat(sheet.getColumnWidth(0), is((13 + 2) * 256));
    }

    // ------------------------------------------------------------------ config override

    /**
     * Given: ブロック間空行 0・罫線なし・自動列幅なし・背景色変更の上書き設定。
     * When : build。
     * Then : 各上書きが反映される。
     */
    @Test
    public void honorsConfigOverrides() {
        // Given
        short customSetupHeader = org.apache.poi.ss.usermodel.IndexedColors.LIGHT_GREEN.getIndex();
        ExcelFormatConfig config = ExcelFormatConfig.defaults()
                .withBlankRowsBetweenBlocks(0)
                .withBlockBorder(false)
                .withCellBorder(false)
                .withAutoColumnWidth(false)
                .withSetupHeaderColor(customSetupHeader);
        TableDataBlock t1 = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T1",
                row("C"), Collections.singletonList(row("v")));
        TableDataBlock t2 = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T2",
                row("C"), Collections.singletonList(row("v")));

        // When
        Workbook wb = new XlsFormatWriter(config).build(container("book", "sheet", t1, t2));
        Sheet sheet = onlySheet(wb, "sheet");

        // Then
        // 空行 0 → t2 は行 3 から
        assertThat(cell(sheet, 3, 0), is("SETUP_TABLE=T2"));
        // 罫線なし
        assertThat(sheet.getRow(0).getCell(0).getCellStyle().getBorderTop(), is(CellStyle.BORDER_NONE));
        // 列名行（row 1）の背景色は上書き値（識別行 row 0 は色なし）
        assertThat(sheet.getRow(1).getCell(0).getCellStyle().getFillForegroundColor(), is(customSetupHeader));
        // 自動列幅なし → 自動調整時の幅（最長 "SETUP_TABLE=T2" 14 文字＝(14+2)*256）にはならない
        assertThat(sheet.getColumnWidth(0), is(not((14 + 2) * 256)));
    }

    /**
     * Given: 空行数に負数。
     * When : ExcelFormatConfig 構築。
     * Then : IllegalArgumentException。
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeBlankRows() {
        // When / Then
        ExcelFormatConfig.defaults().withBlankRowsBetweenBlocks(-1);
    }

    // ------------------------------------------------------------------ write (I/O) & multi-sheet

    /**
     * Given: 2 セクションのコンテナ。
     * When : write。
     * Then : {@code <basePath>/<コンテナ名>.xlsx} が生成され、各セクション＝シートになる。
     */
    @Test
    public void writesWorkbookFileWithSheetPerSection() throws Exception {
        // Given
        TestDataSection s1 = new TestDataSection("Sheet1", Collections.<TestDataBlock>singletonList(
                new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T", row("C"),
                        Collections.singletonList(row("v")))));
        TestDataSection s2 = new TestDataSection("Sheet2", Collections.<TestDataBlock>singletonList(
                new ListMapBlock("", "lm", row("K"), Collections.singletonList(row("1")))));
        TestDataContainer c = new TestDataContainer("MyBook", Arrays.asList(s1, s2));

        // When
        new XlsFormatWriter().write(c, folder.getRoot().getAbsolutePath());

        // Then
        File xlsx = new File(folder.getRoot(), "MyBook.xlsx");
        assertTrue(xlsx.exists());
        try (java.io.InputStream in = new java.io.FileInputStream(xlsx)) {
            Workbook wb = org.apache.poi.ss.usermodel.WorkbookFactory.create(in);
            assertThat(wb.getSheet("Sheet1"), is(notNullValue()));
            assertThat(wb.getSheet("Sheet2"), is(notNullValue()));
            assertThat(wb.getSheet("Sheet1").getRow(0).getCell(0).getStringCellValue(), is("SETUP_TABLE=T"));
        }
    }

    /**
     * Given: マーカーカラム背景色を上書きした設定。
     * When : build。
     * Then : マーカーカラムに上書き色が付く。
     */
    @Test
    public void honorsMarkerColumnColorOverride() {
        // Given
        short custom = org.apache.poi.ss.usermodel.IndexedColors.LIGHT_TURQUOISE.getIndex();
        ExcelFormatConfig config = ExcelFormatConfig.defaults().withMarkerColumnColor(custom);
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                row("ID", "[NOTE]"), Collections.singletonList(row("1", "memo")));

        // When
        Workbook wb = new XlsFormatWriter(config).build(container("book", "sheet", table));
        Sheet sheet = onlySheet(wb, "sheet");

        // Then
        assertThat(sheet.getRow(2).getCell(1).getCellStyle().getFillForegroundColor(), is(custom));
    }

    /**
     * Given: 閉じ括弧の無いカラム名 {@code [half}（マーカーカラムでない）。
     * When : build。
     * Then : マーカー背景色は付かない（{@code [...]} の両端一致のみがマーカー）。
     */
    @Test
    public void doesNotTintUnclosedBracketColumn() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                row("ID", "[half"), Collections.singletonList(row("1", "x")));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", table)), "sheet");

        // Then
        short marker = ExcelFormatConfig.defaults().getMarkerColumnColorIndex();
        assertThat(sheet.getRow(2).getCell(1).getCellStyle().getFillForegroundColor(), is(not(marker)));
    }

    /**
     * Given: 型・長さが空文字のフィールドと、値が空文字のディレクティブを持つ固定長ファイル。
     * When : build。
     * Then : 省略は空セルとして書かれる（データ行の null とは区別）。
     *
     * <p>
     * 番人の境界も兼ねる。弾くのはデータ型・フィールド長が {@code null} の場合だけであり、
     * <b>空文字は弾かない</b>（{@link FieldDef} の契約は「{@code type} は必須（{@code null} 不可）」
     * 「{@code length} は固定長ファイル・電文では必須（{@code null} 不可）」である）。
     * </p>
     *
     * <p>
     * <b>入力は #25.5 §1-C（2026-08-18）で長さを {@code null} から空文字へ書き直した。</b>
     * 固定長ファイルで長さを {@code null} にできた頃の版は「長さセルが空で書かれる」という
     * XLS-30 の不具合そのものを緑で固定していたため、番人と両立しない
     * （型を {@code null} から空文字へ書き直した YML-12 4 形目のときと同じ扱い）。
     * </p>
     *
     * <p>
     * <b>ディレクティブの値も #25.5 §6-H（2026-08-19。XLS-43）で {@code null} から空文字へ書き直した。</b>
     * {@link FileDataBlock} がディレクティブの値の {@code null} を生成時に拒否するようになったため、
     * この入力はもう作れない。<b>空文字は拒否しない</b>ので、空セルとして書かれることの担保は
     * 空文字入力で続けられる。
     * </p>
     */
    @Test
    public void writesOmittedMetaAndFieldAsEmpty() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Collections.singletonList(new FieldDef("f1", "", "")),
                Collections.singletonList(row("v")));
        Map<String, String> directives = new LinkedHashMap<String, String>();
        directives.put("text-encoding", "");
        FileDataBlock file = new FileDataBlock(DataType.SETUP_FIXED, "", "f.dat",
                FileDataBlock.FileType.FIXED, directives, Collections.singletonList(record));

        // When
        Sheet sheet = onlySheet(build(container("book", "sheet", file)), "sheet");

        // Then
        // ディレクティブ値 空文字 → 空セル
        assertThat(cell(sheet, 1, 0), is("text-encoding"));
        assertThat(cell(sheet, 1, 1), is(""));
        // 型・長さ 空文字 → 空セル
        assertThat(line(sheet, 3), is(Arrays.asList("", "")));   // 型行
        assertThat(line(sheet, 4), is(Arrays.asList("", "")));   // 長さ行
        // データ行の値は記法のまま
        assertThat(line(sheet, 5), is(Arrays.asList("", "v")));
    }

    /**
     * Given: 送信系 4 種すべて。
     * When : build。
     * Then : いずれもデータ行の列 0 に no（連番）が付く（送信系判定が全種別で成立）。
     */
    @Test
    public void writesSequenceNoForAllSendSyncTypes() {
        // Given / When / Then (パラメータ化ループ)
        DataType[] types = {
                DataType.EXPECTED_REQUEST_HEADER_MESSAGES,
                DataType.EXPECTED_REQUEST_BODY_MESSAGES,
                DataType.RESPONSE_HEADER_MESSAGES,
                DataType.RESPONSE_BODY_MESSAGES};
        for (DataType type : types) {
            RecordLayout record = new RecordLayout("no",
                    Collections.singletonList(new FieldDef("f", "半角", "5")),
                    Collections.singletonList(row("v")));
            MessageDataBlock message = new MessageDataBlock(type, "[g]", "id", map(), map(),
                    Collections.singletonList(record));
            Sheet sheet = onlySheet(build(container("book", "sheet", message)), "sheet");
            // 識別行 → 名前(1) → 型(2) → 長さ(3) → データ(4)。データ行の列 0 は連番 "1"
            assertThat("type=" + type, cell(sheet, 4, 0), is("1"));
        }
    }

    /**
     * Given: 親ディレクトリを作成できない出力先（既存の通常ファイルを basePath に与える）。
     * When : write。
     * Then : {@link java.io.UncheckedIOException} を送出する。
     */
    @Test(expected = java.io.UncheckedIOException.class)
    public void wrapsIoFailure() throws Exception {
        // Given
        File blocker = folder.newFile("blocker");  // 通常ファイル。配下にディレクトリは作れない
        TestDataContainer c = container("Book", "s",
                new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T", row("C"),
                        Collections.singletonList(row("v"))));

        // When / Then
        new XlsFormatWriter().write(c, blocker.getAbsolutePath());
    }

    // ------------------------------------------------------------------ round-trip

    /** モデルを書き出し、実 {@link XlsFormatReader} で読み戻す。 */
    private TestDataContainer roundTrip(String book, String sheet, TestDataBlock block) {
        TestDataContainer container = container(book, sheet, block);
        new XlsFormatWriter().write(container, folder.getRoot().getAbsolutePath());
        return new XlsFormatReader().read(folder.getRoot().getAbsolutePath(), book + "/" + sheet);
    }

    /**
     * Given: テーブルブロック。
     * When : 書き出し → 実 Reader で読み戻し。
     * Then : データタイプ・識別子・カラム名・行が一致。
     */
    @Test
    public void roundTripsTable() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "USERS",
                row("USER_NAME", "AGE"),
                Arrays.asList(row("${u}", "20"), row("alice", "")));

        // When
        TestDataBlock read = roundTrip("rt_table", "s", table).getSections().get(0).getBlocks().get(0);

        // Then
        TableDataBlock actual = (TableDataBlock) read;
        assertThat(actual.getDataType(), is(DataType.SETUP_TABLE_DATA));
        assertThat(actual.getIdentifier(), is("USERS"));
        assertThat(actual.getColumnNames(), is(Arrays.asList("USER_NAME", "AGE")));
        assertThat(actual.getRows().get(0), is(Arrays.asList("${u}", "20")));
        assertThat(actual.getRows().get(1), is(Arrays.asList("alice", "")));
    }

    /**
     * Given: null セルと空文字セルを持つテーブル。
     * When : 書き出し → 実 Reader で読み戻し。
     * Then : null は文字列 {@code "null"} として戻り（リテラル null を書くため、空インタープリタの読み戻しで
     *        文字列化される＝既知の非可逆。Writer Javadoc 記載）、空文字 {@code ""} は空文字のまま戻る。
     *        この非可逆挙動をテストで固定し将来のリグレッションを検知する。
     */
    @Test
    public void roundTripsNullCellAsLiteralNullString() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                row("A", "B"), Collections.singletonList(row(null, "")));

        // When
        TestDataBlock read = roundTrip("rt_null", "s", table).getSections().get(0).getBlocks().get(0);

        // Then
        TableDataBlock actual = (TableDataBlock) read;
        // null → リテラル "null" を書く → 読み戻しは文字列 "null"（null ↔ null は Excel 経路では復元不可）
        assertThat(actual.getRows().get(0).get(0), is("null"));
        // "" は空文字のまま
        assertThat(actual.getRows().get(0).get(1), is(""));
    }

    /**
     * Given: LIST_MAP ブロック。
     * When : 往復。
     * Then : 列順・値が一致。
     */
    @Test
    public void roundTripsListMap() {
        // Given
        ListMapBlock listMap = new ListMapBlock("", "result",
                row("ID", "NAME"), Arrays.asList(row("${id}", ""), row("2", "bob")));

        // When
        TestDataBlock read = roundTrip("rt_listmap", "s", listMap).getSections().get(0).getBlocks().get(0);

        // Then
        ListMapBlock actual = (ListMapBlock) read;
        assertThat(actual.getIdentifier(), is("result"));
        assertThat(actual.getColumnNames(), is(Arrays.asList("ID", "NAME")));
        assertThat(actual.getRows().get(0), is(Arrays.asList("${id}", "")));
        assertThat(actual.getRows().get(1), is(Arrays.asList("2", "bob")));
    }

    /**
     * Given: 固定長ファイル（型・長さ・{@code -} 含む）。
     * When : 往復。
     * Then : レコード種別・型記法・長さ（{@code -} 含む）・値が原文どおり。
     */
    @Test
    public void roundTripsFixedFile() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Arrays.asList(new FieldDef("f1", "半角英字", "-"), new FieldDef("f2", "半角英字", "5")),
                Collections.singletonList(row("abcd", "xy")));
        FileDataBlock file = new FileDataBlock(DataType.SETUP_FIXED, "", "test.dat",
                FileDataBlock.FileType.FIXED, map("text-encoding", "UTF-8"),
                Collections.singletonList(record));

        // When
        TestDataBlock read = roundTrip("rt_fixed", "s", file).getSections().get(0).getBlocks().get(0);

        // Then
        FileDataBlock actual = (FileDataBlock) read;
        assertThat(actual.getIdentifier(), is("test.dat"));
        assertThat(actual.getFileType(), is(FileDataBlock.FileType.FIXED));
        RecordLayout rec = actual.getRecords().get(0);
        assertThat(rec.getRecordType(), is("data"));
        assertThat(rec.getFields().get(0).getType(), is("半角英字"));
        assertThat(rec.getFields().get(0).getLength(), is("-"));
        assertThat(rec.getFields().get(1).getLength(), is("5"));
        assertThat(rec.getRows().get(0), is(Arrays.asList("abcd", "xy")));
    }

    /**
     * Given: 2 レコードレイアウト（種別非空）を持つ固定長ファイル。
     * When : 往復。
     * Then : 2 レコードが各レコード種別・フィールド・値のまま分割復元される
     *        （番人が弾かない正常側の境界＝複数レコード版面の対称性を実 Reader で固定）。
     */
    @Test
    public void roundTripsMultipleRecordLayouts() {
        // Given
        RecordLayout header = new RecordLayout("header",
                Collections.singletonList(new FieldDef("h1", "半角英字", "5")),
                Collections.singletonList(row("AAAAA")));
        RecordLayout data = new RecordLayout("data",
                Collections.singletonList(new FieldDef("d1", "半角英字", "2")),
                Collections.singletonList(row("xy")));
        FileDataBlock file = new FileDataBlock(DataType.SETUP_FIXED, "", "multi.dat",
                FileDataBlock.FileType.FIXED, map(), Arrays.asList(header, data));

        // When
        TestDataBlock read = roundTrip("rt_multi", "s", file).getSections().get(0).getBlocks().get(0);

        // Then
        FileDataBlock actual = (FileDataBlock) read;
        assertThat(actual.getRecords().size(), is(2));
        assertThat(actual.getRecords().get(0).getRecordType(), is("header"));
        assertThat(actual.getRecords().get(0).getRows().get(0), is(Arrays.asList("AAAAA")));
        assertThat(actual.getRecords().get(1).getRecordType(), is("data"));
        assertThat(actual.getRecords().get(1).getRows().get(0), is(Arrays.asList("xy")));
    }

    /**
     * Given: 可変長ファイル（長さなし）。
     * When : 往復。
     * Then : 長さは省略（null）。
     */
    @Test
    public void roundTripsVariableFile() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Collections.singletonList(new FieldDef("f1", "半角英字", null)),
                Collections.singletonList(row("${v}")));
        FileDataBlock file = new FileDataBlock(DataType.SETUP_VARIABLE, "", "in.csv",
                FileDataBlock.FileType.VARIABLE, map(), Collections.singletonList(record));

        // When
        TestDataBlock read = roundTrip("rt_var", "s", file).getSections().get(0).getBlocks().get(0);

        // Then
        FileDataBlock actual = (FileDataBlock) read;
        assertThat(actual.getFileType(), is(FileDataBlock.FileType.VARIABLE));
        RecordLayout rec = actual.getRecords().get(0);
        assertThat(rec.getFields().get(0).getLength(), is(nullValue()));
        assertThat(rec.getRows().get(0), is(Arrays.asList("${v}")));
    }

    /**
     * Given: FW 制御ヘッダ＋本文の MESSAGE。
     * When : 往復。
     * Then : FW ヘッダ・本文レコードが記法のまま一致。
     */
    @Test
    public void roundTripsMessage() {
        // Given
        RecordLayout record = new RecordLayout("data",
                Arrays.asList(new FieldDef("body1", "半角英字", "10"), new FieldDef("body2", "半角英字", "5")),
                Collections.singletonList(row("${b}", "xyz")));
        MessageDataBlock message = new MessageDataBlock(DataType.MESSAGE, "", "msg1",
                map(), map("requestId", "${rid}"), Collections.singletonList(record));

        // When
        TestDataBlock read = roundTrip("rt_msg", "s", message).getSections().get(0).getBlocks().get(0);

        // Then
        MessageDataBlock actual = (MessageDataBlock) read;
        assertThat(actual.getDataType(), is(DataType.MESSAGE));
        assertThat(actual.getIdentifier(), is("msg1"));
        assertThat(actual.getFwHeaderFields().get("requestId"), is("${rid}"));
        RecordLayout rec = actual.getRecords().get(0);
        assertThat(rec.getFields().get(0).getName(), is("body1"));
        assertThat(rec.getRows().get(0), is(Arrays.asList("${b}", "xyz")));
    }

    /**
     * Given: 送信系（no 列）。
     * When : 往復。
     * Then : データタイプ・グループ ID・本文値（no を除く）が一致。FW ヘッダは空。
     */
    @Test
    public void roundTripsSendSyncMessage() {
        // Given
        RecordLayout record = new RecordLayout("no",
                Arrays.asList(new FieldDef("requestId", "半角", "20"), new FieldDef("resendFlag", "半角", "1")),
                Collections.singletonList(row("RM21AA0104_01", "0")));
        MessageDataBlock message = new MessageDataBlock(DataType.EXPECTED_REQUEST_HEADER_MESSAGES,
                "[case1]", "RM21AA0104_01", map(), map(), Collections.singletonList(record));

        // When
        TestDataBlock read = roundTrip("rt_sendsync", "s", message).getSections().get(0).getBlocks().get(0);

        // Then
        MessageDataBlock actual = (MessageDataBlock) read;
        assertThat(actual.getDataType(), is(DataType.EXPECTED_REQUEST_HEADER_MESSAGES));
        assertThat(actual.getGroupId(), is("[case1]"));
        assertThat(actual.getIdentifier(), is("RM21AA0104_01"));
        assertTrue(actual.getFwHeaderFields().isEmpty());
        RecordLayout rec = actual.getRecords().get(0);
        List<String> fieldNames = new ArrayList<String>();
        for (FieldDef f : rec.getFields()) {
            fieldNames.add(f.getName());
        }
        assertThat(fieldNames, is(Arrays.asList("requestId", "resendFlag")));
        assertThat(rec.getRows().get(0), is(Arrays.asList("RM21AA0104_01", "0")));
    }
}
