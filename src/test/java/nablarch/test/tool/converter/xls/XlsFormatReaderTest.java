package nablarch.test.tool.converter.xls;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import nablarch.test.core.reader.TestCoreReaderAdapter;
import nablarch.test.core.reader.TestDataReader;
import nablarch.test.tool.converter.model.FieldDef;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.MessageDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import org.junit.Test;

/**
 * {@link XlsFormatReader}のテストクラス。
 * <p>
 * 実 Excel を使わず、{@link FakeTestDataReader}に canned な行データを与えて
 * {@link TestCoreReaderAdapter}を駆動し、本体器が中間モデルへ無損失（IN 値が記法のまま）に
 * 写されることを検証する。{@link TestCoreReaderAdapter}内部の静的キャッシュ衝突を避けるため、
 * テストメソッドごとにリソース名を一意にする。
 * </p>
 *
 * @author kiyobot
 */
public class XlsFormatReaderTest {

    /** ディレクトリ（ダミー） */
    private static final String DIR = "dummy-dir";

    /**
     * テスト用の{@link TestDataReader}実装。リソース名をキーに canned データを返す。
     */
    private static final class FakeTestDataReader implements TestDataReader {

        /** リソース名 → 行データ */
        private final Map<String, List<List<String>>> dataByResource = new HashMap<String, List<List<String>>>();

        /** 現在オープン中のイテレータ */
        private java.util.Iterator<List<String>> current;

        /**
         * canned データを登録する。
         *
         * @param resource リソース名
         * @param lines    行データ
         * @return 自身
         */
        FakeTestDataReader put(String resource, List<List<String>> lines) {
            dataByResource.put(resource, lines);
            return this;
        }

        @Override
        public void open(String path, String dataName) {
            List<List<String>> lines = dataByResource.get(dataName);
            if (lines == null) {
                lines = new ArrayList<List<String>>();
            }
            current = lines.iterator();
        }

        @Override
        public void close() {
            current = null;
        }

        @Override
        public List<String> readLine() {
            return (current != null && current.hasNext()) ? current.next() : null;
        }

        @Override
        public boolean isResourceExisting(String basePath, String resourceName) {
            return dataByResource.containsKey(resourceName);
        }

        @Override
        public boolean isDataExisting(String basePath, String resourceName) {
            return dataByResource.containsKey(resourceName);
        }
    }

    /**
     * null セルを含められるよう{@link Arrays#asList}で行を組み立てる。
     *
     * @param cells セル
     * @return 行データ
     */
    private static List<String> row(String... cells) {
        return Arrays.asList(cells);
    }

    /**
     * Fake リーダに 1 リソース分の行を登録した{@link XlsFormatReader}を生成する。
     *
     * @param resource リソース名
     * @param lines    行データ
     * @return リーダ
     */
    private static XlsFormatReader readerOf(String resource, List<List<String>> lines) {
        FakeTestDataReader fake = new FakeTestDataReader().put(resource, lines);
        return new XlsFormatReader(new TestCoreReaderAdapter(fake));
    }

    // ------------------------------------------------------------------ table

    /**
     * Given: {@code ${...}}・空セル・{@code null} 記法を含む SETUP_TABLE ブロック 1 件。
     * When : {@code read}。
     * Then : TableDataBlock に写され、値はテスティングフレームワークが解釈したあとの値になる
     *        （{@code null} 記法は Java {@code null}、空セルは空文字。{@code ${...}} は記法のまま）。
     */
    @Test
    public void readMapsTableBlockWithFrameworkInterpretedValues() {
        // Given
        String resource = "book/readMapsTableBlockWithFrameworkInterpretedValues";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_TABLE=USERS"));
        lines.add(row("USER_NAME", "AGE"));
        lines.add(row("${userName}", ""));
        lines.add(row("literal", "null"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        assertThat(blocks.size(), is(1));
        TableDataBlock table = (TableDataBlock) blocks.get(0);
        assertThat(table.getIdentifier(), is("USERS"));
        assertThat(table.getColumnNames(), is(Arrays.asList("USER_NAME", "AGE")));
        assertThat(table.getRows().get(0), is(Arrays.asList("${userName}", "")));
        // null 記法は Java null（空文字と区別）
        assertThat(table.getRows().get(1).get(0), is("literal"));
        assertThat(table.getRows().get(1).get(1), is(nullValue()));
    }

    /**
     * Given: テーブルセルに Excel 引用符記法 {@code ""} (空文字) と {@code "abc"} (値) を含む行。
     * When : {@code read}。
     * Then : {@code ""} は空文字に、{@code "abc"} は {@code abc} に変換される（stripQuotes）。
     *        {@code ${...}} などの実行時記法はそのまま保持される。
     */
    @Test
    public void readTableNormalizesExcelQuotationNotation() {
        // Given
        String resource = "book/readTableNormalizesExcelQuotationNotation";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_TABLE=T"));
        lines.add(row("A", "B", "C"));
        lines.add(row("\"\"", "\"abc\"", "${expr}"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        TableDataBlock table = (TableDataBlock) container.getSections().get(0).getBlocks().get(0);
        List<String> row = table.getRows().get(0);
        assertThat(row.get(0), is(""));
        assertThat(row.get(1), is("abc"));
        assertThat(row.get(2), is("${expr}"));
    }

    /**
     * Given: LIST_MAP セルに Excel 引用符記法 {@code ""} と {@code "val"} を含む行。
     * When : {@code read}。
     * Then : {@code ""} は空文字に、{@code "val"} は {@code val} に変換される（stripQuotes）。
     */
    @Test
    public void readListMapNormalizesExcelQuotationNotation() {
        // Given
        String resource = "book/readListMapNormalizesExcelQuotationNotation";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("LIST_MAP=lm"));
        lines.add(row("K1", "K2"));
        lines.add(row("\"\"", "\"val\""));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        ListMapBlock block = (ListMapBlock) container.getSections().get(0).getBlocks().get(0);
        List<String> row = block.getRows().get(0);
        assertThat(row.get(0), is(""));
        assertThat(row.get(1), is("val"));
    }

    /**
     * Given: 固定長ファイル値行に Excel 引用符記法 {@code ""} と {@code "x"} を含む行。
     * When : {@code read}。
     * Then : {@code ""} は空文字に、{@code "x"} は {@code x} に変換される（stripQuotes）。
     */
    @Test
    public void readFixedFileNormalizesExcelQuotationNotation() {
        // Given
        String resource = "book/readFixedFileNormalizesExcelQuotationNotation";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_FIXED=f.dat"));
        lines.add(row("data", "F1", "F2"));
        lines.add(row("", "半角英字", "半角英字"));
        lines.add(row("", "3", "3"));
        lines.add(row("", "\"\"", "\"x\""));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        FileDataBlock file = (FileDataBlock) container.getSections().get(0).getBlocks().get(0);
        List<String> row = file.getRecords().get(0).getRows().get(0);
        assertThat(row.get(0), is(""));
        assertThat(row.get(1), is("x"));
    }

    /**
     * Given: 同一タイプ・同一グループ（無指定）の SETUP_TABLE が 2 件。
     * When : {@code read}。
     * Then : 2 ブロックに展開され、一括取得の重複読みで増殖しない。
     */
    @Test
    public void readMapsMultipleTablesWithoutDuplication() {
        // Given
        String resource = "book/readMapsMultipleTablesWithoutDuplication";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_TABLE=USERS"));
        lines.add(row("USER_NAME"));
        lines.add(row("alice"));
        lines.add(row("SETUP_TABLE=ROLES"));
        lines.add(row("ROLE_NAME"));
        lines.add(row("admin"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        assertThat(blocks.size(), is(2));
        assertThat(blocks.get(0).getIdentifier(), is("USERS"));
        assertThat(blocks.get(1).getIdentifier(), is("ROLES"));
    }

    /**
     * Given: グループ ID 付きの EXPECTED_TABLE ブロック。
     * When : {@code read}。
     * Then : ブロックにグループ ID とデータタイプが保持される。
     */
    @Test
    public void readPreservesGroupIdAndDataType() {
        // Given
        String resource = "book/readPreservesGroupIdAndDataType";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("EXPECTED_TABLE[g1]=USERS"));
        lines.add(row("USER_NAME"));
        lines.add(row("${u}"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        TableDataBlock table = (TableDataBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(table.getGroupId(), is("g1"));
        assertThat(table.getDataType(), is(nablarch.test.core.reader.DataType.EXPECTED_TABLE_DATA));
        assertThat(table.getRows().get(0).get(0), is("${u}"));
    }

    // ------------------------------------------------------------------ list_map

    /**
     * Given: アルファベット逆順（Z, A, M）の列名を持つ LIST_MAP ブロック。
     * When : {@code read}。
     * Then : ListMapBlock の列順が Excel 記述順（Z, A, M）のまま保持される（アルファベット順にならない）。
     */
    @Test
    public void readListMapPreservesColumnOrder() {
        // Given: 列順は Z, A, M（アルファベット順なら A, M, Z になるはず）
        String resource = "book/readListMapPreservesColumnOrder";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("LIST_MAP=ordered"));
        lines.add(row("Z", "A", "M"));
        lines.add(row("z1", "a1", "m1"));
        lines.add(row("z2", "a2", "m2"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then: 列順が Excel 記述順（Z, A, M）のまま保持される
        ListMapBlock listMap = (ListMapBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(listMap.getColumnNames(), is(Arrays.asList("Z", "A", "M")));
        // 各行の値も Z→A→M 順に並ぶ
        assertThat(listMap.getRows().get(0), is(Arrays.asList("z1", "a1", "m1")));
        assertThat(listMap.getRows().get(1), is(Arrays.asList("z2", "a2", "m2")));
    }

    /**
     * Given: マーカーカラム（{@code [no]}）を含む LIST_MAP ブロック。
     * When : {@code read}。
     * Then : マーカーカラムが列名・行データから除外され、通常列のみが記述順で保持される。
     */
    @Test
    public void readListMapExcludesMarkerColumns() {
        // Given: [no] はマーカーカラム、description/status は通常列
        String resource = "book/readListMapExcludesMarkerColumns";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("LIST_MAP=testShots"));
        lines.add(row("[no]", "description", "status"));
        lines.add(row("1", "first case", "200"));
        lines.add(row("2", "second case", "400"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then: マーカーカラム [no] は除外され、通常列のみ記述順で保持される
        ListMapBlock listMap = (ListMapBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(listMap.getColumnNames(), is(Arrays.asList("description", "status")));
        assertThat(listMap.getRows().get(0), is(Arrays.asList("first case", "200")));
        assertThat(listMap.getRows().get(1), is(Arrays.asList("second case", "400")));
    }

    /**
     * Given: {@code ${...}}・空文字を含む LIST_MAP ブロック。
     * When : {@code read}。
     * Then : ListMapBlock に写され、IN 値が記法のまま、列順が保たれる。
     */
    @Test
    public void readMapsListMapBlock() {
        // Given
        String resource = "book/readMapsListMapBlock";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("LIST_MAP=result"));
        lines.add(row("ID", "NAME"));
        lines.add(row("${id}", ""));
        lines.add(row("2", "bob"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        ListMapBlock listMap = (ListMapBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(listMap.getIdentifier(), is("result"));
        assertThat(listMap.getColumnNames(), is(Arrays.asList("ID", "NAME")));
        assertThat(listMap.getRows().get(0), is(Arrays.asList("${id}", "")));
        assertThat(listMap.getRows().get(1), is(Arrays.asList("2", "bob")));
    }

    // ------------------------------------------------------------------ fixed file

    /**
     * Given: SETUP_FIXED の固定長ファイル（型・長さ・{@code ${...}}を含む）。
     * When : {@code read}。
     * Then : FileDataBlock（FIXED）に写され、レコードレイアウト・フィールド定義・行が無損失。
     */
    @Test
    public void readMapsFixedLengthFileBlock() {
        // Given
        String resource = "book/readMapsFixedLengthFileBlock";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_FIXED=test.dat"));
        lines.add(row("data", "field1", "field2"));
        lines.add(row("", "半角英字", "半角英字"));
        lines.add(row("", "10", "5"));
        lines.add(row("", "${value}", "abc"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        FileDataBlock file = (FileDataBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(file.getIdentifier(), is("test.dat"));
        assertThat(file.getFileType(), is(FileDataBlock.FileType.FIXED));
        assertThat(file.getRecords().size(), is(1));
        RecordLayout record = file.getRecords().get(0);
        assertThat(record.getRecordType(), is("data"));
        assertThat(record.getFields().size(), is(2));
        FieldDef f1 = record.getFields().get(0);
        assertThat(f1.getName(), is("field1"));
        // 型は原文（設計記法）が生行から復元される。器が FW シンボル（X/N/Z）へ正規化した値ではない。
        assertThat(f1.getType(), is("半角英字"));
        assertThat(f1.getLength(), is("10"));
        FieldDef f2 = record.getFields().get(1);
        assertThat(f2.getName(), is("field2"));
        assertThat(f2.getLength(), is("5"));
        // IN 値は記法のまま
        assertThat(record.getRows().get(0), is(Arrays.asList("${value}", "abc")));
    }

    /**
     * Given: 長さ省略（{@code -}）フィールドと明示レコード種別を含む SETUP_FIXED ブロック。
     * When : {@code read}。
     * Then : レコード種別・型記法・長さ（{@code -} 含む）が生行の原文どおり復元される
     *        （器が正規化した値＝実バイト長・FW シンボルは現れない）。
     */
    @Test
    public void readRestoresOriginalRecordTypeTypeAndOmittedLengthFromRawLines() {
        // Given
        String resource = "book/readRestoresOriginalRecordTypeTypeAndOmittedLength";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_FIXED=om.dat"));
        lines.add(row("text-encoding", "UTF-8"));        // ディレクティブ行（名前行より前）
        lines.add(row("rt", "f1", "f2"));                // 名前行（列0=レコード種別）
        lines.add(row("", "半角英字", "半角英字"));        // 型（設計記法）
        lines.add(row("", "-", "5"));                    // 長さ（f1 は省略）
        lines.add(row("", "abcd", "xy"));                // データ行

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        FileDataBlock file = (FileDataBlock) container.getSections().get(0).getBlocks().get(0);
        RecordLayout record = file.getRecords().get(0);
        // レコード種別は生行の名前行 列0 から（器では private で読めない）
        assertThat(record.getRecordType(), is("rt"));
        FieldDef f1 = record.getFields().get(0);
        // 型は原文記法（器の FW シンボル X ではない）
        assertThat(f1.getType(), is("半角英字"));
        // 長さ省略 "-" は原文どおり（器が上書きする実バイト長 "4" ではない）
        assertThat(f1.getLength(), is("-"));
        assertThat(record.getFields().get(1).getLength(), is("5"));
        // 値は記法のまま
        assertThat(record.getRows().get(0), is(Arrays.asList("abcd", "xy")));
    }

    /**
     * Given: 複数レコードレイアウト（header + data の 2 断片）を持つ SETUP_FIXED ブロック。
     * When : {@code read}。
     * Then : 各断片の レコード種別・型記法・長さ・値 が独立に原文復元される
     *        （2 断片目以降の名前行同期と原文充填が正しく働く）。
     */
    @Test
    public void readRestoresMultipleRecordLayoutsInFixedFile() {
        // Given
        String resource = "book/readRestoresMultipleRecordLayoutsInFixedFile";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_FIXED=multi.dat"));
        lines.add(row("text-encoding", "UTF-8"));  // 長さ省略 "-" の動的バイト長計算に必要
        lines.add(row("header", "h1", "h2"));     // 断片1 名前行
        lines.add(row("", "半角英字", "半角英字"));
        lines.add(row("", "5", "3"));
        lines.add(row("", "AAAAA", "BBB"));       // 断片1 値行（1 行）
        lines.add(row("data", "d1", "d2"));       // 断片2 名前行（列0 非空＝新レコード）
        lines.add(row("", "半角英字", "半角"));
        lines.add(row("", "-", "2"));             // 断片2 は長さ省略 "-" を含む
        lines.add(row("", "1", "xy"));            // 断片2 値行（2 行）
        lines.add(row("", "2", "zw"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        FileDataBlock file = (FileDataBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(file.getRecords().size(), is(2));

        RecordLayout header = file.getRecords().get(0);
        assertThat(header.getRecordType(), is("header"));
        assertThat(header.getFields().get(0).getName(), is("h1"));
        assertThat(header.getFields().get(0).getType(), is("半角英字"));
        assertThat(header.getFields().get(0).getLength(), is("5"));
        assertThat(header.getRows().size(), is(1));
        assertThat(header.getRows().get(0), is(Arrays.asList("AAAAA", "BBB")));

        RecordLayout data = file.getRecords().get(1);
        assertThat(data.getRecordType(), is("data"));
        assertThat(data.getFields().get(0).getName(), is("d1"));
        // 2 断片目の型・長さも独立に原文復元される
        assertThat(data.getFields().get(1).getType(), is("半角"));
        assertThat(data.getFields().get(0).getLength(), is("-"));
        assertThat(data.getRows().size(), is(2));
        assertThat(data.getRows().get(0), is(Arrays.asList("1", "xy")));
        assertThat(data.getRows().get(1), is(Arrays.asList("2", "zw")));
    }

    /**
     * Given: SETUP_VARIABLE の可変長ファイル（長さなし）。
     * When : {@code read}。
     * Then : FileDataBlock（VARIABLE）に写され、長さは省略（{@code null}）。
     */
    @Test
    public void readMapsVariableLengthFileBlock() {
        // Given
        String resource = "book/readMapsVariableLengthFileBlock";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_VARIABLE=in.csv"));
        lines.add(row("data", "f1"));
        lines.add(row("", "半角英字"));
        lines.add(row("", "${v}"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        FileDataBlock file = (FileDataBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(file.getFileType(), is(FileDataBlock.FileType.VARIABLE));
        RecordLayout record = file.getRecords().get(0);
        assertThat(record.getFields().get(0).getName(), is("f1"));
        assertThat(record.getFields().get(0).getLength(), is(nullValue()));
        assertThat(record.getRows().get(0), is(Arrays.asList("${v}")));
    }

    // ------------------------------------------------------------------ message

    /**
     * Given: FW 制御ヘッダ＋本文を持つ MESSAGE ブロック。
     * When : {@code read}。
     * Then : MessageDataBlock に写され、FW ヘッダ・本文レコードが記法のまま。
     */
    @Test
    public void readMapsMessageBlock() {
        // Given
        String resource = "book/readMapsMessageBlock";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("MESSAGE=msg1"));
        lines.add(row("requestId", "${rid}"));
        lines.add(row("data", "body1", "body2"));
        lines.add(row("", "半角英字", "半角英字"));
        lines.add(row("", "10", "5"));
        lines.add(row("", "${b}", "xyz"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        MessageDataBlock message = (MessageDataBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(message.getIdentifier(), is("msg1"));
        assertThat(message.getDataType(), is(nablarch.test.core.reader.DataType.MESSAGE));
        // FW ヘッダは記法のまま
        assertThat(message.getFwHeaderFields().get("requestId"), is("${rid}"));
        // 本文レコード
        assertThat(message.getRecords().size(), is(1));
        RecordLayout record = message.getRecords().get(0);
        assertThat(record.getFields().get(0).getName(), is("body1"));
        assertThat(record.getRows().get(0), is(Arrays.asList("${b}", "xyz")));
    }

    // ------------------------------------------------------------- send-sync messages

    /**
     * Given: {@code no} 列＋本文フィールドを持つ EXPECTED_REQUEST_HEADER_MESSAGES ブロック
     *        （{@code TYPE[group]=id} 形式マーカー）。
     * When : {@code read}。
     * Then : MessageDataBlock に写される。グループ ID は生値の {@code case1}、識別子は {@code =} 以降。
     *        {@code no} 列はメタ情報のため脱落し、本文フィールド・値は記法のまま。FW ヘッダは空。
     */
    @Test
    public void readMapsExpectedRequestHeaderMessageBlock() {
        // Given
        String resource = "book/readMapsExpectedRequestHeaderMessageBlock";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("EXPECTED_REQUEST_HEADER_MESSAGES[case1]=RM21AA0104_01"));
        lines.add(row("text-encoding", "ms932"));
        lines.add(row("no", "requestId", "resendFlag"));
        lines.add(row("", "半角", "半角"));
        lines.add(row("", "20", "1"));
        lines.add(row("1", "RM21AA0104_01", "0"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        MessageDataBlock message = (MessageDataBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(message.getDataType(), is(nablarch.test.core.reader.DataType.EXPECTED_REQUEST_HEADER_MESSAGES));
        assertThat(message.getGroupId(), is("case1"));
        assertThat(message.getIdentifier(), is("RM21AA0104_01"));
        // 送信系に FW 制御ヘッダは無い（常に空）
        assertTrue(message.getFwHeaderFields().isEmpty());
        // ディレクティブは記法のまま
        assertThat(message.getDirectives().get("text-encoding"), is("ms932"));
        // 本文レコード: no 列は脱落し、フィールドは requestId/resendFlag のみ
        assertThat(message.getRecords().size(), is(1));
        RecordLayout record = message.getRecords().get(0);
        List<String> fieldNames = new ArrayList<String>();
        for (FieldDef field : record.getFields()) {
            fieldNames.add(field.getName());
        }
        assertThat(fieldNames, is(Arrays.asList("requestId", "resendFlag")));
        assertThat(record.getFields().get(0).getType(), is("半角"));
        assertThat(record.getFields().get(0).getLength(), is("20"));
        // 値行も no（NO 値）が脱落し、本文値のみ
        assertThat(record.getRows().get(0), is(Arrays.asList("RM21AA0104_01", "0")));
    }

    /**
     * Given: 同一グループ {@code [case1]} に EXPECTED_REQUEST_HEADER/BODY、
     *        グループ {@code [res_case1]} に RESPONSE_HEADER/BODY を持つシート。
     * When : {@code read}。
     * Then : 4 種すべてが MessageDataBlock として写り、データタイプ・グループ ID が保たれる。
     */
    @Test
    public void readMapsAllFourSendSyncMessageTypes() {
        // Given
        String resource = "book/readMapsAllFourSendSyncMessageTypes";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("EXPECTED_REQUEST_HEADER_MESSAGES[case1]=RM21AA0104_01"));
        lines.add(row("no", "requestId"));
        lines.add(row("", "半角"));
        lines.add(row("", "20"));
        lines.add(row("1", "RM21AA0104_01"));
        lines.add(row("EXPECTED_REQUEST_BODY_MESSAGES[case1]=RM21AA0104_01"));
        lines.add(row("no", "userId"));
        lines.add(row("", "半角"));
        lines.add(row("", "10"));
        lines.add(row("1", "user01"));
        lines.add(row("RESPONSE_HEADER_MESSAGES[res_case1]=RM21AA0104_01"));
        lines.add(row("no", "requestId"));
        lines.add(row("", "半角"));
        lines.add(row("", "20"));
        lines.add(row("1", "RM21AA0101"));
        lines.add(row("RESPONSE_BODY_MESSAGES[res_case1]=RM21AA0104_01"));
        lines.add(row("no", "failureCode"));
        lines.add(row("", "半角"));
        lines.add(row("", "20"));
        lines.add(row("1", "0"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        Map<nablarch.test.core.reader.DataType, String> typeToGroup =
                new HashMap<nablarch.test.core.reader.DataType, String>();
        for (TestDataBlock block : blocks) {
            assertTrue(block instanceof MessageDataBlock);
            MessageDataBlock m = (MessageDataBlock) block;
            typeToGroup.put(m.getDataType(), m.getGroupId());
        }
        assertThat(blocks.size(), is(4));
        assertThat(typeToGroup.get(nablarch.test.core.reader.DataType.EXPECTED_REQUEST_HEADER_MESSAGES), is("case1"));
        assertThat(typeToGroup.get(nablarch.test.core.reader.DataType.EXPECTED_REQUEST_BODY_MESSAGES), is("case1"));
        assertThat(typeToGroup.get(nablarch.test.core.reader.DataType.RESPONSE_HEADER_MESSAGES), is("res_case1"));
        assertThat(typeToGroup.get(nablarch.test.core.reader.DataType.RESPONSE_BODY_MESSAGES), is("res_case1"));
    }

    /**
     * Given: 同一データタイプ・同一グループに識別子の異なる 2 ブロック。
     * When : {@code read}。
     * Then : 各識別子ごとに MessageDataBlock が 1 件ずつ写る。
     */
    @Test
    public void readMapsMultipleSendSyncBlocksInSameGroup() {
        // Given
        String resource = "book/readMapsMultipleSendSyncBlocksInSameGroup";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("EXPECTED_REQUEST_HEADER_MESSAGES[case1]=RM21AA0104_01"));
        lines.add(row("no", "requestId"));
        lines.add(row("", "半角"));
        lines.add(row("", "20"));
        lines.add(row("1", "RM21AA0104_01"));
        lines.add(row("EXPECTED_REQUEST_HEADER_MESSAGES[case1]=RM21AA0104_02"));
        lines.add(row("no", "requestId"));
        lines.add(row("", "半角"));
        lines.add(row("", "20"));
        lines.add(row("1", "RM21AA0104_02"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        assertThat(blocks.size(), is(2));
        List<String> identifiers = new ArrayList<String>();
        for (TestDataBlock block : blocks) {
            identifiers.add(((MessageDataBlock) block).getIdentifier());
        }
        assertThat(identifiers, hasItem("RM21AA0104_01"));
        assertThat(identifiers, hasItem("RM21AA0104_02"));
    }

    // ---------------------------------------------- record-separator symbol conversion

    /**
     * Given: SETUP_VARIABLE ブロックに {@code record-separator} ディレクティブ値 {@code CRLF} を設定した行。
     * When : {@code read}。
     * Then : 本体が {@code CRLF} を実改行文字（{@code \r\n}）へ変換し、{@code normalizeDirectiveValue} が
     *        逆正規化して {@code "CRLF"} シンボルを FileDataBlock のディレクティブへ返す。
     */
    @Test
    public void readNormalizesRecordSeparatorCrlfSymbol() {
        // Given
        String resource = "book/readNormalizesRecordSeparatorCrlfSymbol";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_VARIABLE=out.csv"));
        lines.add(row("record-separator", "CRLF"));
        lines.add(row("data", "f1"));
        lines.add(row("", "半角英字"));
        lines.add(row("", "val"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        FileDataBlock file = (FileDataBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(file.getDirectives().get("record-separator"), is("CRLF"));
    }

    /**
     * Given: SETUP_VARIABLE ブロックに {@code record-separator} ディレクティブ値 {@code LF} を設定した行。
     * When : {@code read}。
     * Then : 本体が {@code LF} を実改行文字（{@code \n}）へ変換し、{@code normalizeDirectiveValue} が
     *        逆正規化して {@code "LF"} シンボルを FileDataBlock のディレクティブへ返す。
     */
    @Test
    public void readNormalizesRecordSeparatorLfSymbol() {
        // Given
        String resource = "book/readNormalizesRecordSeparatorLfSymbol";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_VARIABLE=out.csv"));
        lines.add(row("record-separator", "LF"));
        lines.add(row("data", "f1"));
        lines.add(row("", "半角英字"));
        lines.add(row("", "val"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        FileDataBlock file = (FileDataBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(file.getDirectives().get("record-separator"), is("LF"));
    }

    /**
     * Given: SETUP_VARIABLE ブロックに {@code record-separator} ディレクティブ値 {@code CR} を設定した行。
     * When : {@code read}。
     * Then : 本体が {@code CR} を実改行文字（{@code \r}）へ変換し、{@code normalizeDirectiveValue} が
     *        逆正規化して {@code "CR"} シンボルを FileDataBlock のディレクティブへ返す。
     */
    @Test
    public void readNormalizesRecordSeparatorCrSymbol() {
        // Given
        String resource = "book/readNormalizesRecordSeparatorCrSymbol";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_VARIABLE=out.csv"));
        lines.add(row("record-separator", "CR"));
        lines.add(row("data", "f1"));
        lines.add(row("", "半角英字"));
        lines.add(row("", "val"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        FileDataBlock file = (FileDataBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(file.getDirectives().get("record-separator"), is("CR"));
    }

    // ------------------------------------------------------------------ wiring / robustness

    /**
     * Given: 引数なしコンストラクタ（本番配線＝実 Excel を読む {@link nablarch.test.core.reader.PoiXlsReader}
     *        を注入したアダプタを構成）。
     * When : インスタンス化。
     * Then : 例外なく生成される（本番配線の健全性）。
     */
    @Test
    public void defaultConstructorWiresProductionAdapter() {
        // When / Then
        assertNotNull(new XlsFormatReader());
    }

    /**
     * Given: 先頭セルがデータタイプ名で始まるが {@code =} を持たない行（不完全マーカー／
     *        偶然データタイプ名で始まるデータ行）。
     * When : {@code read}。
     * Then : マーカーとして扱われず無視され、ブロックは生成されない。
     */
    @Test
    public void readIgnoresDataTypePrefixedLineWithoutMarker() {
        // Given
        String resource = "book/readIgnoresDataTypePrefixedLineWithoutMarker";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("MESSAGE"));   // データタイプ名で始まるが '=' なし＝マーカーでない
        lines.add(row("x"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        assertTrue(container.getSections().get(0).getBlocks().isEmpty());
    }

    /**
     * Given: errorMode 行（{@code errorMode:timeout}）を含む RESPONSE_BODY_MESSAGES ブロック。
     *        本体パーサは errorMode 行を NO 列除去後の本文値として扱う。
     * When : {@code read}。
     * Then : errorMode 文字列が本文値として原文のまま保持され、NO（caseNo）は脱落する。
     *        レコード種別は名前行先頭の {@code "no"}（FW_HEADER 扱いされず良性）。
     *        ＜#6 自身の契約＝原文保持を固定する。runtime 同値の検証は #14 で別途行う＞
     */
    @Test
    public void readPreservesErrorModeRowInSendSyncMessage() {
        // Given
        String resource = "book/readPreservesErrorModeRowInSendSyncMessage";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("RESPONSE_BODY_MESSAGES[res_case1]=RM21AA0104_01"));
        lines.add(row("no", "failureCode"));
        lines.add(row("", "半角"));
        lines.add(row("", "20"));
        lines.add(row("1", "errorMode:timeout"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        MessageDataBlock message = (MessageDataBlock) container.getSections().get(0).getBlocks().get(0);
        RecordLayout record = message.getRecords().get(0);
        // レコード種別は名前行先頭の "no"（送信系。steering R3 裏取り結論で良性と確定）
        assertThat(record.getRecordType(), is("no"));
        // no 列は脱落し、本文フィールドは failureCode のみ
        assertThat(record.getFields().size(), is(1));
        assertThat(record.getFields().get(0).getName(), is("failureCode"));
        // errorMode 文字列は本文値として原文のまま保持される
        assertThat(record.getRows().get(0), is(Arrays.asList("errorMode:timeout")));
    }

    // ------------------------------------------------------------------ container / section

    /**
     * Given: {@code "ブック名/シート名"} 形式のリソース名。
     * When : {@code read}。
     * Then : コンテナ名＝ブック名、セクション名＝シート名。
     */
    @Test
    public void readDerivesContainerAndSectionNamesFromResource() {
        // Given
        String resource = "MyBook/MySheet";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_TABLE=T"));
        lines.add(row("C"));
        lines.add(row("v"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        assertThat(container.getName(), is("MyBook"));
        assertThat(container.getSections().size(), is(1));
        assertThat(container.getSections().get(0).getName(), is("MySheet"));
    }

    // ------------------------------------------------------------------ duplicate column names

    /**
     * WARN ログを収集するためのハンドラ。
     */
    private static final class CapturingHandler extends Handler {
        final List<String> messages = new ArrayList<String>();

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                messages.add(record.getMessage());
            }
        }

        @Override
        public void flush() { /* no-op */ }

        @Override
        public void close() { /* no-op */ }
    }

    /**
     * Given: LIST_MAP ブロックのヘッダ行に重複カラム名（COL_A が 2 回）が存在する。
     * When : {@code read}。
     * Then : 変換が続行され、重複カラムは後勝ちで 1 件のみに絞られ、WARN ログが出力される。
     *        WARN ログにはブック名・シート名・ブロック識別子・重複カラム名が含まれる。
     */
    @Test
    public void readListMapWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins() {
        // Given
        String resource = "dupBook/dupSheet";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("LIST_MAP=dupMap"));
        lines.add(row("COL_A", "COL_B", "COL_A"));  // COL_A が 2 回（重複）
        lines.add(row("first", "b1", "last"));        // 後勝ち: COL_A = "last"

        CapturingHandler handler = new CapturingHandler();
        Logger logger = Logger.getLogger(XlsFormatReader.class.getName());
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.WARNING);
        try {
            // When
            TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

            // Then: 変換が続行される
            ListMapBlock listMap = (ListMapBlock) container.getSections().get(0).getBlocks().get(0);
            // 重複が除去され COL_A は 1 件のみ（後勝ち = 後方の列）
            assertThat(listMap.getColumnNames(), is(Arrays.asList("COL_B", "COL_A")));
            // 後勝ちの値（"last"）が採用される
            assertThat(listMap.getRows().get(0), is(Arrays.asList("b1", "last")));
            // WARN ログが出ること
            assertThat(handler.messages.size(), is(1));
            assertThat(handler.messages.get(0), containsString("dupBook"));
            assertThat(handler.messages.get(0), containsString("dupSheet"));
            assertThat(handler.messages.get(0), containsString("dupMap"));
            assertThat(handler.messages.get(0), containsString("COL_A"));
        } finally {
            logger.setUseParentHandlers(true);
            logger.removeHandler(handler);
        }
    }

    /**
     * Given: LIST_MAP ブロックのヘッダ行に複数の異なる重複カラム名が存在する。
     * When : {@code read}。
     * Then : 各重複カラム名につき 1 件の WARN ログが出力され、後勝ちで絞られる。
     */
    @Test
    public void readListMapWithMultipleDuplicateColumnsEmitsWarnPerName() {
        // Given
        String resource = "book/readListMapWithMultipleDuplicateColumnsEmitsWarnPerName";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("LIST_MAP=mDup"));
        lines.add(row("A", "B", "A", "C", "B"));  // A が 2 回、B が 2 回
        lines.add(row("a1", "b1", "a2", "c1", "b2"));

        CapturingHandler handler = new CapturingHandler();
        Logger logger = Logger.getLogger(XlsFormatReader.class.getName());
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.WARNING);
        try {
            // When
            TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

            // Then: A(index2), C(index3), B(index4) の順（各名前の最後の出現位置を保持）
            ListMapBlock listMap = (ListMapBlock) container.getSections().get(0).getBlocks().get(0);
            assertThat(listMap.getColumnNames(), is(Arrays.asList("A", "C", "B")));
            // 後勝ちの値（A=a2, C=c1, B=b2）
            assertThat(listMap.getRows().get(0), is(Arrays.asList("a2", "c1", "b2")));
            // A と B それぞれ 1 件ずつ = 合計 2 件の WARN
            assertThat(handler.messages.size(), is(2));
            // Both A and B must appear across the warnings (order may vary)
            String allWarnings = handler.messages.get(0) + handler.messages.get(1);
            assertThat(allWarnings, containsString("\"A\""));
            assertThat(allWarnings, containsString("\"B\""));
        } finally {
            logger.setUseParentHandlers(true);
            logger.removeHandler(handler);
        }
    }

    /**
     * Given: SETUP_TABLE ブロックのヘッダ行に重複カラム名（COL_X が 2 回）が存在する。
     * When : {@code read}。
     * Then : 変換が続行され、WARN ログが出力され、後勝ちで 1 件に絞られる。
     */
    @Test
    public void readTableWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins() {
        // Given
        String resource = "book/readTableWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_TABLE=MY_TABLE"));
        lines.add(row("COL_X", "COL_Y", "COL_X"));  // COL_X が 2 回
        lines.add(row("first", "y1", "last"));

        CapturingHandler handler = new CapturingHandler();
        Logger logger = Logger.getLogger(XlsFormatReader.class.getName());
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.WARNING);
        try {
            // When
            TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

            // Then: 変換が続行される
            TableDataBlock table = (TableDataBlock) container.getSections().get(0).getBlocks().get(0);
            // 重複が除去され COL_X は 1 件のみ（後勝ち）
            assertThat(table.getColumnNames(), is(Arrays.asList("COL_Y", "COL_X")));
            // 後勝ちの値（"last"）が採用される
            assertThat(table.getRows().get(0), is(Arrays.asList("y1", "last")));
            // WARN ログが出ること
            assertThat(handler.messages.size(), is(1));
            assertThat(handler.messages.get(0), containsString("book"));
            assertThat(handler.messages.get(0), containsString("readTableWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins"));
            assertThat(handler.messages.get(0), containsString("MY_TABLE"));
            assertThat(handler.messages.get(0), containsString("COL_X"));
            assertThat(handler.messages.get(0), containsString("3 列目"));
        } finally {
            logger.setUseParentHandlers(true);
            logger.removeHandler(handler);
        }
    }

    /**
     * Given: 重複なしの LIST_MAP ブロック。
     * When : {@code read}。
     * Then : WARN ログは出力されない（正常系の非回帰）。
     */
    @Test
    public void readListMapWithoutDuplicatesEmitsNoWarn() {
        // Given
        String resource = "book/readListMapWithoutDuplicatesEmitsNoWarn";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("LIST_MAP=noDup"));
        lines.add(row("A", "B", "C"));
        lines.add(row("a", "b", "c"));

        CapturingHandler handler = new CapturingHandler();
        Logger logger = Logger.getLogger(XlsFormatReader.class.getName());
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.WARNING);
        try {
            // When
            readerOf(resource, lines).read(DIR, resource);

            // Then: WARN ログは出力されない
            assertThat(handler.messages.size(), is(0));
        } finally {
            logger.setUseParentHandlers(true);
            logger.removeHandler(handler);
        }
    }

    /**
     * Given: マーカー行が存在しないリソース。
     * When : {@code read}。
     * Then : ブロックが空のセクションを 1 つ持つコンテナを返す。
     */
    @Test
    public void readReturnsEmptySectionWhenNoBlocks() {
        // Given
        String resource = "book/readReturnsEmptySectionWhenNoBlocks";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("just", "data"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        assertThat(container.getSections().size(), is(1));
        assertTrue(container.getSections().get(0).getBlocks().isEmpty());
    }

    /**
     * Given: テーブル・固定長ファイル・LIST_MAP・MESSAGE が混在するリソース。
     * When : {@code read}。
     * Then : 1 セクションに 4 種のブロックが揃う（全種別を 1 シートから組み立てられる）。
     */
    @Test
    public void readAssemblesMixedBlockTypesInOneSection() {
        // Given
        String resource = "book/readAssemblesMixedBlockTypesInOneSection";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_TABLE=T"));
        lines.add(row("C"));
        lines.add(row("v"));
        lines.add(row("SETUP_FIXED=f.dat"));
        lines.add(row("data", "f1"));
        lines.add(row("", "半角英字"));
        lines.add(row("", "5"));
        lines.add(row("", "x"));
        lines.add(row("LIST_MAP=lm"));
        lines.add(row("K"));
        lines.add(row("1"));
        lines.add(row("MESSAGE=m"));
        lines.add(row("requestId", "R"));
        lines.add(row("data", "b1"));
        lines.add(row("", "半角英字"));
        lines.add(row("", "3"));
        lines.add(row("", "y"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then
        TestDataSection section = container.getSections().get(0);
        List<Class<?>> kinds = new ArrayList<Class<?>>();
        for (TestDataBlock block : section.getBlocks()) {
            kinds.add(block.getClass());
        }
        assertThat(kinds.size(), is(4));
        assertThat(kinds, hasItem((Class<?>) TableDataBlock.class));
        assertThat(kinds, hasItem((Class<?>) FileDataBlock.class));
        assertThat(kinds, hasItem((Class<?>) ListMapBlock.class));
        assertThat(kinds, hasItem((Class<?>) MessageDataBlock.class));
    }

    // ------------------------------------------------------------------ normalizeDirectiveValue / record-separator

    /**
     * Given: SETUP_VARIABLE ブロックに {@code record-separator} ディレクティブ値として {@code "NONE"} シンボルを設定した行
     *        （本体の {@code LineSeparator.evaluate} が空文字列へ変換する）。
     * When : {@code read}。
     * Then : 本体が空文字をそのまま保持し、{@code normalizeDirectiveValue} が {@code "NONE"} シンボルへ
     *        変換して FileDataBlock のディレクティブへ返す（仕様 DR-09 の NONE シンボル逆正規化）。
     */
    @Test
    public void readNormalizesRecordSeparatorEmptyValueToNoneSymbol() {
        // Given
        String resource = "book/readNormalizesRecordSeparatorEmptyValueToNoneSymbol";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_VARIABLE=out.csv"));
        lines.add(row("record-separator", "NONE"));
        lines.add(row("data", "f1"));
        lines.add(row("", "半角英字"));
        lines.add(row("", "val"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then: 本体は "NONE" を空文字列に変換し、normalizeDirectiveValue が "NONE" へ逆正規化する
        FileDataBlock file = (FileDataBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(file.getDirectives().get("record-separator"), is("NONE"));
    }

    /**
     * Given: SETUP_VARIABLE ブロックに {@code record-separator} ディレクティブ値として
     *        CRLF/LF/CR/NONE のいずれでもないカスタム文字列を設定した行。
     * When : {@code read}。
     * Then : {@code normalizeDirectiveValue} がシンボル変換せずその値をそのまま返す
     *        （record-separator ブロックのフォールスルー経路）。
     */
    @Test
    public void readPassesThroughUnknownRecordSeparatorValue() {
        // Given: "|" は CRLF/LF/CR/NONE のいずれでもないカスタム区切り文字
        String resource = "book/readPassesThroughUnknownRecordSeparatorValue";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_VARIABLE=out.csv"));
        // 本体の LineSeparator.evaluate がシンボル変換しない値を渡す。
        // "|" は本体器がそのまま保持するため normalizeDirectiveValue のフォールスルーに到達する。
        lines.add(row("record-separator", "|"));
        lines.add(row("data", "f1"));
        lines.add(row("", "半角英字"));
        lines.add(row("", "val"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then: 変換されずそのまま保持される
        FileDataBlock file = (FileDataBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(file.getDirectives().get("record-separator"), is("|"));
    }

    /**
     * Given: SETUP_VARIABLE ブロックに非 record-separator・非 field-separator のディレクティブとして
     *        ダブルクォートで囲まれた値（{@code "foo"}）を設定した行。
     * When : {@code read}。
     * Then : {@code normalizeDirectiveValue} が QuotationTrimmer 記法と判定し、
     *        {@code stripQuotes} を適用してクォートを除去した {@code foo} を返す。
     */
    @Test
    public void readStripsQuotesFromQuotedGenericDirectiveValue() {
        // Given: quoting-delimiter ディレクティブの値が Excel 引用符記法 '"foo"'（長さ5 > 2）
        String resource = "book/readStripsQuotesFromQuotedGenericDirectiveValue";
        List<List<String>> lines = new ArrayList<List<String>>();
        lines.add(row("SETUP_VARIABLE=out.csv"));
        // quoting-delimiter の値を '"foo"'（前後 " で囲まれた長さ5の文字列）とする
        lines.add(row("quoting-delimiter", "\"foo\""));
        lines.add(row("data", "f1"));
        lines.add(row("", "半角英字"));
        lines.add(row("", "val"));

        // When
        TestDataContainer container = readerOf(resource, lines).read(DIR, resource);

        // Then: '"foo"' から前後クォートが除去されて 'foo' になる
        FileDataBlock file = (FileDataBlock) container.getSections().get(0).getBlocks().get(0);
        assertThat(file.getDirectives().get("quoting-delimiter"), is("foo"));
    }
}
