package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.blank;
import static nablarch.test.tool.converter.xls.XlsFixture.number;
import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nablarch.test.core.reader.DataType;
import nablarch.test.tool.converter.model.ColumnRowDataBlock;
import nablarch.test.tool.converter.model.FieldDef;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 参照フィクスチャ（Excel が保存した実物 {@code .xlsx}）の読み取り結果を固定し、同じ論理内容を POI で
 * 組み立てた {@code .xlsx} が同じ読み取り結果になることを確認するテスト。
 *
 * <p>
 * 辺①のフィクスチャは原則 POI 生成方式（テスト実行時に組み立て、バイナリを静的同梱しない）だが、
 * POI 生成物と Excel 保存物の読み取り結果が同一である保証はない。そこで実物 {@code .xlsx} を 1 本だけ
 * 参照フィクスチャとして同梱し、同一性をこのテストで裏付ける。
 * </p>
 *
 * <p>
 * 参照フィクスチャ {@code reference/ProjectActionRequestTest.xlsx} は {@code nablarch-example-web}
 * （{@code origin/main} の {@code src/test/java/com/nablarch/example/app/web/action/ProjectActionRequestTest.xlsx}）
 * を無改変で取り込んだもの。{@code docProps/app.xml} に {@code <Application>Microsoft Excel</Application>}・
 * {@code AppVersion 16.0300} を持つ真正な Excel 保存物である。検証に使う {@code downloadNormal} シートは、
 * 実プロジェクトの Excel テストデータで唯一「データ列に置かれた数値セル」
 * （{@code COST_OF_GOODS_SOLD} 列。ブック XML 上 {@code <c r="M14" s="55"><v>2000</v>} ＝ {@code t} 属性を持たない
 * 数値セル）を含み、課題 XLS-01 が実データで再現する。同シートは表示形式 {@code @}（{@code numFmtId="49"}）
 * 付きの数値セル（{@code [no]} マーカー列の {@code <c r="A19" s="37"><v>1</v>}）も併せ持つ。
 * </p>
 *
 * <p>
 * <b>比較を循環させないための方式。</b>期待値は Excel 保存物から機械的に導出せず、
 * <b>テストソースに直書きした論理内容</b>を唯一の権威とする。
 * </p>
 * <ol>
 *   <li>{@link #readsExcelSavedWorkbookIntoIntermediateModel()} — 参照フィクスチャを読んだ中間モデルを、
 *       直書きした期待値（ブロック数・データタイプ・識別子・列名・全行の値・ディレクティブ・
 *       レコードレイアウト）と突き合わせる。</li>
 *   <li>{@link #poiGeneratedWorkbookReadsIdenticallyToExcelSavedWorkbook()} — 同じ論理内容を
 *       {@link XlsFixture} でセル種別を明示して組み立て（数値セルは {@code number(...)}、
 *       文字列セルは {@code text(...)}）、両ブックの中間モデルを突き合わせる。
 *       {@code copyOf} のように元セルを {@code Cell#toString()} で写し取る方式は、リーダ側も
 *       {@code cell.toString()} を使うため同じ関数を 2 回適用する恒等式になり、セル種別の違いを
 *       検出できない。本テストは POI 側のセル種別をテストソースで宣言することでその循環を断つ。
 *       たとえば {@code COST_OF_GOODS_SOLD} を {@code text("2000")} と宣言すると、Excel 側の
 *       {@code "2000.0"} と一致せず落ちる。</li>
 * </ol>
 *
 * <p>
 * 検証したのは {@code downloadNormal} シート 1 枚（LIST_MAP 3 ブロック・SETUP_TABLE 1 ブロック・
 * EXPECTED_VARIABLE 1 ブロック）であり、参照ブックの残り 25 シートは対象外である。実データに存在しない
 * セル種別（日付・時刻・日時・数式・真偽値・エラー）は参照フィクスチャに含まれないため、
 * それらの POI 生成物と Excel 保存物の同一性は未確認である（{@code coverage/issues.md}「未確認」参照）。
 * </p>
 *
 * @author kiyobot
 */
public class XlsReferenceFixtureTest {

    /** 参照フィクスチャのブック名。 */
    private static final String BOOK = "ProjectActionRequestTest";

    /** 検証対象シート名。 */
    private static final String SHEET = "downloadNormal";

    /** {@code EXPECTED_VARIABLE} ブロックの識別子（出力ファイルパス）。 */
    private static final String CSV_PATH =
            "./tmp/html_dump/ProjectActionRequestTest/downloadNormal_Shot1_プロジェクト一覧ダウンロード_プロジェクト一覧.csv";

    /** {@code SETUP_TABLE[1]=PROJECT} の列名。 */
    private static final List<String> PROJECT_COLUMNS = Arrays.asList(
            "PROJECT_ID", "PROJECT_NAME", "PROJECT_TYPE", "PROJECT_CLASS", "PROJECT_START_DATE",
            "PROJECT_END_DATE", "CLIENT_ID", "PROJECT_MANAGER", "PROJECT_LEADER", "USER_ID", "NOTE",
            "SALES", "COST_OF_GOODS_SOLD", "SGA", "ALLOCATION_OF_CORP_EXPENSES", "VERSION");

    /** {@code EXPECTED_VARIABLE} ブロックのフィールド名。 */
    private static final List<String> CSV_FIELDS = Arrays.asList(
            "projectName", "projectType", "projectClass", "projectManager", "projectLeader",
            "clientId", "clientName", "projectStartDate", "projectEndDate", "note",
            "sales", "costOfGoodsSold", "sga", "allocationOfCorpExpenses");

    /** POI 生成ブックの出力先。 */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * 参照フィクスチャが置かれたディレクトリを返す。
     *
     * <p>
     * {@code PoiXlsReader} はディレクトリパスとブック名でファイルを探すため、クラスパス上のリソースを
     * 実ファイルパスへ解決する必要がある。本プロジェクトのテストリソースは常に {@code target/test-classes}
     * 配下へ展開されるため {@link Paths#get(java.net.URI)} で解決できる。将来リソースが jar 由来になると
     * この解決は失敗するが、そのときはテンポラリへ展開する処理を足せばよく、現時点で先回りはしない。
     * </p>
     *
     * @return ディレクトリ
     */
    private static Path referenceDir() {
        URL url = XlsReferenceFixtureTest.class.getResource("reference/" + BOOK + ".xlsx");
        assertThat("参照フィクスチャがクラスパス上に存在すること", url, is(notNullValue()));
        try {
            return Paths.get(url.toURI()).getParent();
        } catch (Exception e) {
            throw new IllegalStateException("failed to resolve reference fixture: " + url, e);
        }
    }

    /**
     * Given: Excel が保存した実物 {@code .xlsx}（参照フィクスチャ）の {@code downloadNormal} シート。
     * When : 実 {@link XlsFormatReader} で読む。
     * Then : 直書きした期待値どおりの中間モデルになる。とくに
     *        {@code COST_OF_GOODS_SOLD} 列（Excel 上は数値セル）だけが {@code "2000.0"} になり、
     *        同じ行の他の金額列（文字列セル）は {@code "1000"} / {@code "3000"} / {@code "4000"} のままである
     *        （課題 XLS-01 が実データで再現することの記録）。
     */
    @Test
    public void readsExcelSavedWorkbookIntoIntermediateModel() {
        // When
        TestDataContainer container = new XlsFormatReader().read(referenceDir().toString(), BOOK + "/" + SHEET);

        // Then
        assertThat(container.getName(), is(BOOK));
        assertThat(container.getSections().size(), is(1));
        TestDataSection section = container.getSections().get(0);
        assertThat(section.getName(), is(SHEET));

        List<TestDataBlock> blocks = section.getBlocks();
        assertThat(blocks.size(), is(6));
        assertSameBlock("block[0]", expectedTestShots(), blocks.get(0));
        assertSameBlock("block[1]", expectedUser(), blocks.get(1));
        assertSameBlock("block[2]", expectedProject(), blocks.get(2));
        assertSameBlock("block[3]", expectedRequestParams(), blocks.get(3));
        assertSameBlock("block[4]", expectedSearchCondition(), blocks.get(4));
        assertSameBlock("block[5]", expectedCsv(), blocks.get(5));
    }

    /**
     * Given: 参照フィクスチャと、同じ論理内容を {@link XlsFixture} でセル種別を明示して組み立てた
     *        {@code .xlsx}（数値セルは {@code number(...)}・文字列セルは {@code text(...)} と宣言する）。
     * When : 双方を実 {@link XlsFormatReader} で読む。
     * Then : 中間モデルが完全一致する。すなわち Excel が保存した数値セルと POI が生成した数値セルは
     *        リーダを通したとき同じ値になり、POI 生成フィクスチャは Excel 保存物の代用として妥当である。
     *        期待値を Excel 側から導出していないため、Excel 側と POI 側でセル種別が食い違えば落ちる。
     */
    @Test
    public void poiGeneratedWorkbookReadsIdenticallyToExcelSavedWorkbook() {
        // Given
        Path poiDir = folder.getRoot().toPath();
        writePoiEquivalent(poiDir);

        // When
        TestDataContainer fromExcel = new XlsFormatReader().read(referenceDir().toString(), BOOK + "/" + SHEET);
        TestDataContainer fromPoi = new XlsFormatReader().read(poiDir.toString(), BOOK + "/" + SHEET);

        // Then
        assertSameContainer(fromExcel, fromPoi);
    }

    // ------------------------------------------------------------------ POI 生成ブック

    /**
     * 参照フィクスチャ {@code downloadNormal} シートと同じ論理内容のブックを POI で組み立てて書き出す。
     *
     * <p>
     * セル種別はここで宣言する。参照ブックが Excel の空白セル（値なし）を持つ位置は {@link XlsFixture#blank()}、
     * 数値セルの位置は {@link XlsFixture#number} を使う。参照ブックの各行にある末尾の空白セル
     * （書式だけが設定された最大 256 列ぶん）と、全セルが空の行は再現しない。前者は
     * {@code PoiXlsReader#readOneLine} が空文字として読むだけで列構造に影響せず、後者は
     * {@code PoiXlsReader#readLine} が空行として読み飛ばすためである。両者が結果に影響するなら
     * このテストは落ちる。
     * </p>
     *
     * @param dir 出力先ディレクトリ
     */
    private static void writePoiEquivalent(Path dir) {
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("// テストケース"))
                .row(text("LIST_MAP=testShots"))
                .row(texts("no", "description", "context", "isValidToken", "setUpTable", "queryParams",
                        "expectedStatusCode", "expectedMessageId", "expectedSearch", "forwardUri"))
                .row(text("1"), text("プロジェクト一覧ダウンロード"), text("user"), text("false"), text("1"),
                        text("searchCondition"), text("200"), blank(), blank(), blank())

                .row(text("// 事前準備データ"))
                .row(text("LIST_MAP=user"))
                .row(texts("REQUEST_ID", "USER_ID", "HTTP_METHOD"))
                .row(texts("download", "105", "GET"))

                .row(text("SETUP_TABLE[1]=PROJECT"))
                .row(texts(PROJECT_COLUMNS.toArray(new String[0])))
                // COST_OF_GOODS_SOLD（13 列目）だけが数値セル。他の金額列は文字列セルである。
                .row(text("10000"), text("プロジェクト００１"), text("development"), text("s"),
                        text("20150101"), text("20161231"), text("1"), text("テストマネージャー"),
                        text("テストリーダー"), text("105"), text("テスト備考"), text("1000"),
                        number(2000), text("3000"), text("4000"), text("0"))

                .row(text("//入力パラメータ"))
                .row(text("LIST_MAP=requestParams"))
                .row(text("[no]"))
                // マーカー列 [no] の値は表示形式 @ 付きの数値セル。
                .row(number(1, "@"))

                .row(text("LIST_MAP=searchCondition"))
                .row(texts("searchForm.projectName", "searchForm.pageNumber", "searchForm.sortKey"))
                .row(texts("プロジェクト００１", "1", "id"))

                .row(text("//期待値"))
                .row(text("EXPECTED_VARIABLE=" + CSV_PATH))
                .row(texts("text-encoding", "Shift_JIS"))
                .row(texts("record-separator", "CRLF"))
                .row(texts("field-separator", ","))
                .row(leading(text("header"), CSV_FIELDS))
                .row(leading(blank(), repeat("全角漢字", CSV_FIELDS.size())))
                .row(leading(blank(), quoted("プロジェクト名", "プロジェクト種別", "プロジェクト分類",
                        "プロジェクトマネージャー", "プロジェクトリーダー", "顧客ID", "顧客名",
                        "プロジェクト開始日", "プロジェクト終了日", "備考", "売上高", "売上原価",
                        "販管費", "本社配賦")))
                .row(leading(text("data"), CSV_FIELDS))
                .row(leading(blank(), Arrays.asList("全角漢字", "半角", "半角", "全角漢字", "全角漢字", "半角",
                        "全角漢字", "半角", "半角", "全角漢字", "半角", "半角", "半角", "半角")))
                .row(leading(blank(), quoted("プロジェクト００１", "development", "s", "テストマネージャー",
                        "テストリーダー", "1", "テスト顧客名", "2015/01/01", "2016/12/31", "テスト備考",
                        "1000", "2000", "3000", "4000")))
                .writeTo(dir);
    }

    /**
     * 文字列セルの並びを組み立てる。
     *
     * @param values 値
     * @return セル指定の配列
     */
    private static XlsFixture.CellSpec[] texts(String... values) {
        XlsFixture.CellSpec[] cells = new XlsFixture.CellSpec[values.length];
        for (int i = 0; i < values.length; i++) {
            cells[i] = text(values[i]);
        }
        return cells;
    }

    /**
     * 先頭セルの後ろに文字列セルの並びを続けた 1 行分のセル指定を組み立てる。
     *
     * @param first 先頭セル
     * @param rest  2 列目以降の値
     * @return セル指定の配列
     */
    private static XlsFixture.CellSpec[] leading(XlsFixture.CellSpec first, List<String> rest) {
        XlsFixture.CellSpec[] cells = new XlsFixture.CellSpec[rest.size() + 1];
        cells[0] = first;
        for (int i = 0; i < rest.size(); i++) {
            cells[i + 1] = text(rest.get(i));
        }
        return cells;
    }

    /**
     * 各値を Excel のダブルクォート記法（{@code QuotationTrimmer} が剥がす記法）で包む。
     *
     * @param values 値
     * @return 包んだ値
     */
    private static List<String> quoted(String... values) {
        List<String> result = new ArrayList<>(values.length);
        for (String value : values) {
            result.add('"' + value + '"');
        }
        return result;
    }

    /**
     * 同じ値を指定個数並べる。
     *
     * @param value 値
     * @param count 個数
     * @return 値のリスト
     */
    private static List<String> repeat(String value, int count) {
        List<String> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(value);
        }
        return result;
    }

    // ------------------------------------------------------------------ 期待値（直書き）

    /**
     * {@code LIST_MAP=testShots} の期待値。
     *
     * @return 期待ブロック
     */
    private static TestDataBlock expectedTestShots() {
        return new ListMapBlock("", "testShots",
                Arrays.asList("no", "description", "context", "isValidToken", "setUpTable", "queryParams",
                        "expectedStatusCode", "expectedMessageId", "expectedSearch", "forwardUri"),
                Arrays.asList(Arrays.asList("1", "プロジェクト一覧ダウンロード", "user", "false", "1",
                        "searchCondition", "200", "", "", "")));
    }

    /**
     * {@code LIST_MAP=user} の期待値。
     *
     * @return 期待ブロック
     */
    private static TestDataBlock expectedUser() {
        return new ListMapBlock("", "user",
                Arrays.asList("REQUEST_ID", "USER_ID", "HTTP_METHOD"),
                Arrays.asList(Arrays.asList("download", "105", "GET")));
    }

    /**
     * {@code SETUP_TABLE[1]=PROJECT} の期待値。{@code COST_OF_GOODS_SOLD} だけが {@code "2000.0"}。
     *
     * @return 期待ブロック
     */
    private static TestDataBlock expectedProject() {
        return new TableDataBlock(DataType.SETUP_TABLE_DATA, "1", "PROJECT",
                PROJECT_COLUMNS,
                Arrays.asList(Arrays.asList("10000", "プロジェクト００１", "development", "s", "20150101",
                        "20161231", "1", "テストマネージャー", "テストリーダー", "105", "テスト備考",
                        "1000", "2000.0", "3000", "4000", "0")));
    }

    /**
     * {@code LIST_MAP=requestParams} の期待値。マーカー列 {@code [no]} しか持たないため、
     * 中間モデルには列名 0 件・行 0 件が入る。
     * <p>
     * <b>以前は「列名 0 件・値 0 件の行が 1 行」を期待していた（XLS-08）。</b>
     * 空エントリ判定をマーカー列の除外より<b>あとに</b>行うよう辺①を直したため、期待値を置き換えた。
     * {@code nablarch-example-web} 由来の実フィクスチャにこの形が実在することを示す 1 例である。
     * </p>
     *
     * @return 期待ブロック
     */
    private static TestDataBlock expectedRequestParams() {
        return new ListMapBlock("", "requestParams",
                Collections.<String>emptyList(),
                Collections.<List<String>>emptyList());
    }

    /**
     * {@code LIST_MAP=searchCondition} の期待値。
     *
     * @return 期待ブロック
     */
    private static TestDataBlock expectedSearchCondition() {
        return new ListMapBlock("", "searchCondition",
                Arrays.asList("searchForm.projectName", "searchForm.pageNumber", "searchForm.sortKey"),
                Arrays.asList(Arrays.asList("プロジェクト００１", "1", "id")));
    }

    /**
     * {@code EXPECTED_VARIABLE} の期待値。
     *
     * @return 期待ブロック
     */
    private static TestDataBlock expectedCsv() {
        Map<String, String> directives = new LinkedHashMap<>();
        directives.put("file-type", "Variable");
        directives.put("text-encoding", "Shift_JIS");
        directives.put("record-separator", "CRLF");
        directives.put("field-separator", ",");

        List<RecordLayout> records = Arrays.asList(
                new RecordLayout("header", fields(repeat("全角漢字", CSV_FIELDS.size())),
                        Arrays.asList(Arrays.asList("プロジェクト名", "プロジェクト種別", "プロジェクト分類",
                                "プロジェクトマネージャー", "プロジェクトリーダー", "顧客ID", "顧客名",
                                "プロジェクト開始日", "プロジェクト終了日", "備考", "売上高", "売上原価",
                                "販管費", "本社配賦"))),
                new RecordLayout("data", fields(Arrays.asList("全角漢字", "半角", "半角", "全角漢字", "全角漢字",
                                "半角", "全角漢字", "半角", "半角", "全角漢字", "半角", "半角", "半角", "半角")),
                        Arrays.asList(Arrays.asList("プロジェクト００１", "development", "s",
                                "テストマネージャー", "テストリーダー", "1", "テスト顧客名", "2015/01/01",
                                "2016/12/31", "テスト備考", "1000", "2000", "3000", "4000"))));

        return new FileDataBlock(DataType.EXPECTED_VARIABLE, "", CSV_PATH, directives, records);
    }

    /**
     * {@link #CSV_FIELDS} の各フィールドに型記法を割り当てた {@link FieldDef} 群を作る（長さは可変長のため無し）。
     *
     * @param types 型記法（フィールドと同順）
     * @return フィールド定義
     */
    private static List<FieldDef> fields(List<String> types) {
        List<FieldDef> defs = new ArrayList<>(CSV_FIELDS.size());
        for (int i = 0; i < CSV_FIELDS.size(); i++) {
            defs.add(new FieldDef(CSV_FIELDS.get(i), types.get(i), null));
        }
        return defs;
    }

    // ------------------------------------------------------------------ 比較

    /**
     * 2 つの中間モデルが同一内容であることを検証する（中間モデルは {@code equals} を持たないため逐次比較）。
     *
     * @param expected 期待側
     * @param actual   実際側
     */
    private static void assertSameContainer(TestDataContainer expected, TestDataContainer actual) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getSections().size(), is(expected.getSections().size()));
        for (int s = 0; s < expected.getSections().size(); s++) {
            TestDataSection expectedSection = expected.getSections().get(s);
            TestDataSection actualSection = actual.getSections().get(s);
            assertThat(actualSection.getName(), is(expectedSection.getName()));
            assertThat(actualSection.getBlocks().size(), is(expectedSection.getBlocks().size()));
            for (int b = 0; b < expectedSection.getBlocks().size(); b++) {
                assertSameBlock("section[" + s + "].block[" + b + "]",
                        expectedSection.getBlocks().get(b), actualSection.getBlocks().get(b));
            }
        }
    }

    /**
     * 2 つのブロックが同一内容であることを検証する。
     *
     * @param at       診断用の位置
     * @param expected 期待側
     * @param actual   実際側
     */
    private static void assertSameBlock(String at, TestDataBlock expected, TestDataBlock actual) {
        assertThat(at + ".class", actual, instanceOf(expected.getClass()));
        assertThat(at + ".dataType", actual.getDataType(), is(expected.getDataType()));
        assertThat(at + ".groupId", actual.getGroupId(), is(expected.getGroupId()));
        assertThat(at + ".identifier", actual.getIdentifier(), is(expected.getIdentifier()));
        if (expected instanceof ColumnRowDataBlock) {
            ColumnRowDataBlock expectedBlock = (ColumnRowDataBlock) expected;
            ColumnRowDataBlock actualBlock = (ColumnRowDataBlock) actual;
            assertThat(at + ".columnNames", actualBlock.getColumnNames(), is(expectedBlock.getColumnNames()));
            assertThat(at + ".rows", actualBlock.getRows(), is(expectedBlock.getRows()));
        } else if (expected instanceof FileDataBlock) {
            FileDataBlock expectedBlock = (FileDataBlock) expected;
            FileDataBlock actualBlock = (FileDataBlock) actual;
            assertThat(at + ".fileType", actualBlock.getFileType(), is(expectedBlock.getFileType()));
            assertThat(at + ".directives", actualBlock.getDirectives(), is(expectedBlock.getDirectives()));
            assertSameRecords(at, expectedBlock.getRecords(), actualBlock.getRecords());
        }
    }

    /**
     * レコードレイアウト群が同一内容であることを検証する。
     *
     * @param at       診断用の位置
     * @param expected 期待側
     * @param actual   実際側
     */
    private static void assertSameRecords(String at, List<RecordLayout> expected, List<RecordLayout> actual) {
        assertThat(at + ".records.size", actual.size(), is(expected.size()));
        for (int r = 0; r < expected.size(); r++) {
            String recordAt = at + ".record[" + r + "]";
            RecordLayout expectedRecord = expected.get(r);
            RecordLayout actualRecord = actual.get(r);
            assertThat(recordAt + ".recordType", actualRecord.getRecordType(), is(expectedRecord.getRecordType()));
            assertThat(recordAt + ".fields.size", actualRecord.getFields().size(),
                    is(expectedRecord.getFields().size()));
            for (int f = 0; f < expectedRecord.getFields().size(); f++) {
                String fieldAt = recordAt + ".field[" + f + "]";
                FieldDef expectedField = expectedRecord.getFields().get(f);
                FieldDef actualField = actualRecord.getFields().get(f);
                assertThat(fieldAt + ".name", actualField.getName(), is(expectedField.getName()));
                assertThat(fieldAt + ".type", actualField.getType(), is(expectedField.getType()));
                assertThat(fieldAt + ".length", actualField.getLength(), is(expectedField.getLength()));
            }
            assertThat(recordAt + ".rows", actualRecord.getRows(), is(expectedRecord.getRows()));
        }
    }
}
