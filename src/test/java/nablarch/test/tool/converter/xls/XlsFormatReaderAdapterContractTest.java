package nablarch.test.tool.converter.xls;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nablarch.test.core.file.DataFile;
import nablarch.test.core.file.DataFileFragment;
import nablarch.test.core.file.FixedLengthFile;
import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.MessageData;
import nablarch.test.core.reader.TestCoreReaderAdapter;
import nablarch.test.core.reader.TestDataReader;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TestDataContainer;

import org.junit.Test;

/**
 * {@link XlsFormatReader} が<b>アダプタとの契約</b>をどう扱うかのテストクラス。
 *
 * <p>
 * {@link XlsFormatReader} はファイル系・電文系のブロックを 2 経路から組み立てる。器
 * （{@link TestCoreReaderAdapter#readFiles} 等が返す本体器）が断片構造・値を決め、生行
 * （{@link TestCoreReaderAdapter#readBlockBodyLines}）が原文（レコード種別・型記法・長さ）を決める。
 * 実 Excel を入力にすると両経路は同じシートを読むため必ず一致し、<b>一致しないときの振る舞いは
 * 実 Excel からは動かせない</b>。
 * </p>
 *
 * <p>
 * 本クラスはそこを担う。{@link XlsFormatReader} が受け取るアダプタを差し替え、器と生行を
 * 意図的にずらして、次の 3 つが仕様どおりであることを確かめる。
 * </p>
 * <ul>
 *   <li>器が電文を返さないとき、ブロックを作らずに読み飛ばす</li>
 *   <li>生行が器の断片構造と対応しないとき、静かに歪めず {@link IllegalStateException} で即座に失敗する
 *       （名前行が見つからない場合・生行が足りない場合）</li>
 *   <li>型行・長さ行が名前行より短いとき、型・長さを空文字などで補わず {@code null} のまま中間モデルへ
 *       渡し、中間モデルの生成時に弾かれるに任せる（番人は書き出し側でなく中間モデルの生成時に置く）</li>
 * </ul>
 *
 * <p>
 * 差し替えるのは {@link TestCoreReaderAdapter} の値を返すメソッドだけで、マーカー行の走査
 * （{@code readHeaders}）は本体の実装をそのまま使う。
 * </p>
 *
 * @author kiyobot
 */
public class XlsFormatReaderAdapterContractTest {

    /** ディレクトリ（ダミー） */
    private static final String DIR = "dummy-dir";

    /** ファイル識別子 */
    private static final String FILE_ID = "f.dat";

    /**
     * マーカー行だけを返す {@link TestDataReader}。{@code readHeaders} を本体実装で動かすために使う。
     */
    private static final class MarkerOnlyReader implements TestDataReader {

        /** リソース名 → 行データ */
        private final Map<String, List<List<String>>> data = new HashMap<String, List<List<String>>>();

        /** 現在オープン中のイテレータ */
        private java.util.Iterator<List<String>> current;

        /**
         * canned データを登録する。
         *
         * @param resource リソース名
         * @param lines    行データ
         * @return 自身
         */
        MarkerOnlyReader put(String resource, List<List<String>> lines) {
            data.put(resource, lines);
            return this;
        }

        @Override
        public void open(String path, String dataName) {
            List<List<String>> lines = data.get(dataName);
            current = (lines == null ? new ArrayList<List<String>>() : lines).iterator();
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
            return data.containsKey(resourceName);
        }

        @Override
        public boolean isDataExisting(String basePath, String resourceName) {
            return data.containsKey(resourceName);
        }
    }

    /**
     * 器と生行を独立に差し替えられるアダプタ。{@code readHeaders} は継承した本体実装を使う。
     */
    private static final class StubAdapter extends TestCoreReaderAdapter {

        /** {@code readFiles} が返す器（空なら継承実装を使わずそのまま空） */
        private final List<DataFile> files = new ArrayList<DataFile>();

        /** {@code readBlockBodyLines} が返す生行 */
        private List<List<String>> bodyLines = new ArrayList<List<String>>();

        /** {@code readMessage} が返す電文（{@code null} なら「対象が存在しない」） */
        private MessageData message;

        /**
         * コンストラクタ。
         *
         * @param reader マーカー行だけを持つリーダ
         */
        StubAdapter(TestDataReader reader) {
            super(reader);
        }

        @Override
        public List<? extends DataFile> readFiles(String path, String resource, String id, DataType type) {
            return files;
        }

        @Override
        public MessageData readMessage(String path, String resource, String id) {
            return message;
        }

        @Override
        public List<List<String>> readBlockBodyLines(String path, String resource, String groupId,
                                                     String identifier, DataType type) {
            return bodyLines;
        }
    }

    /**
     * Given: {@code MESSAGE=} マーカーは在るが、器が電文を返さない（対象が存在しない）。
     * When : {@code read}。
     * Then : 例外にせず、ブロックを 1 件も作らない。
     *
     * <p>担保：{@code readMessageBlock} の {@code message == null} 側と、その戻り値を捨てる側。</p>
     */
    @Test
    public void skipsMessageBlockWhenAdapterHasNoMessage() {
        // Given
        String resource = "skipsMessageBlockWhenAdapterHasNoMessage";
        MarkerOnlyReader reader = new MarkerOnlyReader().put(resource,
                Arrays.asList(Arrays.asList("MESSAGE=M1")));
        StubAdapter adapter = new StubAdapter(reader);
        adapter.message = null;

        // When
        TestDataContainer container = new XlsFormatReader(adapter).read(DIR, resource);

        // Then
        assertThat(container.getSections().size(), is(1));
        assertThat(container.getSections().get(0).getBlocks().isEmpty(), is(true));
    }

    /**
     * Given: 器は断片（フィールド {@code ZF}／{@code AF}）を持つが、生行にその名前行が無い。
     * When : {@code read}。
     * Then : 生行を先頭から順に当てはめて歪めるのではなく、{@link IllegalStateException} で即座に失敗する。
     *
     * <p>担保：{@code skipToFirstNameRow} が末尾まで走り切る側と、{@code verifyNameRow} の
     * {@code idx >= bodyLines.size()} 側。</p>
     */
    @Test
    public void failsWhenNameRowIsAbsentFromBodyLines() {
        // Given
        String resource = "failsWhenNameRowIsAbsentFromBodyLines";
        StubAdapter adapter = fileAdapter(resource);
        adapter.bodyLines = Arrays.asList(
                Arrays.asList("", "OTHER1"),
                Arrays.asList("", "OTHER2"));

        // When / Then
        try {
            new XlsFormatReader(adapter).read(DIR, resource);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("名前行"));
            assertThat(e.getMessage(), containsString("ZF"));
        }
    }

    /**
     * Given: 器は断片（フィールド {@code ZF}／{@code AF}）を持つが、生行が名前行までしか無い。
     * When : {@code read}。
     * Then : 足りない行を空として補うのではなく、{@link IllegalStateException} で即座に失敗する。
     *
     * <p>担保：{@code requireLine} の {@code idx >= bodyLines.size()} 側。</p>
     */
    @Test
    public void failsWhenBodyLinesAreShorterThanFragment() {
        // Given
        String resource = "failsWhenBodyLinesAreShorterThanFragment";
        StubAdapter adapter = fileAdapter(resource);
        adapter.bodyLines = Arrays.asList(Arrays.asList("", "ZF", "AF"));

        // When / Then
        try {
            new XlsFormatReader(adapter).read(DIR, resource);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("型行"));
            assertThat(e.getMessage(), containsString("ZF"));
        }
    }

    /**
     * Given: 型行が名前行より 1 列短い生行。
     * When : {@code read}。
     * Then : 型を空文字などで補わず {@code null} のまま中間モデルへ渡し、中間モデルの生成時に
     *        {@link IllegalArgumentException} で弾かれる。
     *
     * <p>
     * 担保：{@code readFieldDefs} の「型行が名前行より短い」側。番人は書き出し側でなく中間モデルの
     * 生成時（{@code FieldDef}）に置く、という方針どおりに弾かれることまでを見る。
     * </p>
     */
    @Test
    public void passesNullTypeToModelWhenTypeRowIsShorterThanNames() {
        // Given —— 名前は辞書順とずらす
        String resource = "passesNullTypeToModelWhenTypeRowIsShorterThanNames";
        StubAdapter adapter = fileAdapter(resource);
        adapter.bodyLines = Arrays.asList(
                Arrays.asList("", "ZF", "AF"),
                Arrays.asList("", "半角英字"),
                Arrays.asList("", "2", "2"),
                Arrays.asList("", "ab", "cd"));

        // When / Then
        try {
            new XlsFormatReader(adapter).read(DIR, resource);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("データ型を持たないフィールド定義は作れません"));
            assertThat(e.getMessage(), containsString("AF"));
        }
    }

    /**
     * Given: 名前行の先頭セルが {@code null}、長さ行が名前行より 1 列短い生行。
     * When : {@code read}。
     * Then : レコード種別は {@code null}（省略）として扱い、長さは空文字などで補わず {@code null} の
     *        まま中間モデルへ渡して、中間モデルの生成時に {@link IllegalArgumentException} で弾かれる。
     *
     * <p>
     * 担保：{@code emptyToNull} の {@code null} 側と、{@code readFieldDefs} の
     * 「長さ行が名前行より短い」側。番人は書き出し側でなく中間モデルの生成時
     * （{@code FileDataBlock}）に置く、という方針どおりに弾かれることまでを見る。
     * </p>
     */
    @Test
    public void passesNullLengthToModelWhenLengthRowIsShorterThanNames() {
        // Given —— 名前は辞書順とずらす。先頭セルの null はレコード種別の省略にあたる
        String resource = "passesNullLengthToModelWhenLengthRowIsShorterThanNames";
        StubAdapter adapter = fileAdapter(resource);
        adapter.bodyLines = Arrays.asList(
                Arrays.asList(null, "ZF", "AF"),
                Arrays.asList("", "半角英字", "半角英字"),
                Arrays.asList("", "2"),
                Arrays.asList("", "ab", "cd"));

        // When / Then
        try {
            new XlsFormatReader(adapter).read(DIR, resource);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                    containsString("固定長ファイル・電文でフィールド長を持たないフィールド定義は保持できません"));
            assertThat(e.getMessage(), containsString("AF"));
        }
    }

    /**
     * {@code SETUP_FIXED} マーカー 1 件と、フィールド {@code ZF}／{@code AF}・値 1 行の器を持つ
     * アダプタを組み立てる。生行は呼び出し側が差し替える。
     *
     * @param resource リソース名
     * @return アダプタ
     */
    private static StubAdapter fileAdapter(String resource) {
        MarkerOnlyReader reader = new MarkerOnlyReader().put(resource,
                Arrays.asList(Arrays.asList("SETUP_FIXED=" + FILE_ID)));
        StubAdapter adapter = new StubAdapter(reader);

        FixedLengthFile file = new FixedLengthFile(FILE_ID);
        file.setDirective("text-encoding", "UTF-8");
        DataFileFragment fragment = file.getNewFragment();
        fragment.setNames(Arrays.asList("ZF", "AF"));
        fragment.setTypes(Arrays.asList("半角英字", "半角英字"));
        fragment.setLengths(Arrays.asList("2", "2"));
        fragment.addValue(Arrays.asList("ab", "cd"));
        adapter.files.add(file);
        return adapter;
    }
    /**
     * Given: 名前行の手前に、セルを 1 つも持たない生行が挟まっている。
     * When : {@code read}。
     * Then : その行を読み飛ばして名前行を見つけ、原文の復元が正しく行われる。
     *
     * <p>
     * 担保：先頭セルを落とす操作の「リストが空」側。本体の走査は全セルが空の行を捨てるため
     * （{@code TestDataParsingTemplate} の {@code isBlankLine}）、実 Excel からは空の生行が届かない。
     * </p>
     */
    @Test
    public void skipsRawLineWithNoCellsWhenLocatingNameRow() {
        // Given
        String resource = "skipsRawLineWithNoCellsWhenLocatingNameRow";
        StubAdapter adapter = fileAdapter(resource);
        adapter.bodyLines = Arrays.asList(
                Arrays.<String>asList(),
                Arrays.asList("", "ZF", "AF"),
                Arrays.asList("", "半角英字", "半角英字"),
                Arrays.asList("", "2", "2"),
                Arrays.asList("", "ab", "cd"));

        // When
        TestDataContainer container = new XlsFormatReader(adapter).read(DIR, resource);

        // Then
        FileDataBlock block = (FileDataBlock) container.getSections().get(0).getBlocks().get(0);
        RecordLayout record = block.getRecords().get(0);
        assertThat(record.getFields().size(), is(2));
        assertThat(record.getFields().get(0).getName(), is("ZF"));
        assertThat(record.getFields().get(1).getName(), is("AF"));
        assertThat(record.getRows().get(0), is(Arrays.asList("ab", "cd")));
    }

    /**
     * Given: 器は 2 断片（{@code ZF}／{@code AF} と {@code ZS}）を持つが、生行の 2 断片目にあたる
     *        位置の行が名前行になっていない。
     * When : {@code read}。
     * Then : 位置がずれたまま読み進めるのではなく、{@link IllegalStateException} で即座に失敗する。
     *
     * <p>担保：{@code verifyNameRow} の「その位置の行が名前行と一致しない」側。</p>
     */
    @Test
    public void failsWhenLaterFragmentNameRowDoesNotMatch() {
        // Given
        String resource = "failsWhenLaterFragmentNameRowDoesNotMatch";
        StubAdapter adapter = twoFragmentFileAdapter(resource);
        adapter.bodyLines = Arrays.asList(
                Arrays.asList("", "ZF", "AF"),
                Arrays.asList("", "半角英字", "半角英字"),
                Arrays.asList("", "2", "2"),
                Arrays.asList("", "ab", "cd"),
                Arrays.asList("", "NOT_A_NAME_ROW"),
                Arrays.asList("", "半角英字"),
                Arrays.asList("", "2"),
                Arrays.asList("", "ef"));

        // When / Then
        try {
            new XlsFormatReader(adapter).read(DIR, resource);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("名前行"));
            assertThat(e.getMessage(), containsString("ZS"));
        }
    }

    /**
     * 2 断片（{@code ZF}／{@code AF} と {@code ZS}）を持つ器のアダプタを組み立てる。
     *
     * @param resource リソース名
     * @return アダプタ
     */
    private static StubAdapter twoFragmentFileAdapter(String resource) {
        MarkerOnlyReader reader = new MarkerOnlyReader().put(resource,
                Arrays.asList(Arrays.asList("SETUP_FIXED=" + FILE_ID)));
        StubAdapter adapter = new StubAdapter(reader);

        FixedLengthFile file = new FixedLengthFile(FILE_ID);
        file.setDirective("text-encoding", "UTF-8");
        DataFileFragment first = file.getNewFragment();
        first.setNames(Arrays.asList("ZF", "AF"));
        first.setTypes(Arrays.asList("半角英字", "半角英字"));
        first.setLengths(Arrays.asList("2", "2"));
        first.addValue(Arrays.asList("ab", "cd"));
        DataFileFragment second = file.getNewFragment();
        second.setNames(Arrays.asList("ZS"));
        second.setTypes(Arrays.asList("半角英字"));
        second.setLengths(Arrays.asList("2"));
        second.addValue(Arrays.asList("ef"));
        adapter.files.add(file);
        return adapter;
    }
}
