package nablarch.test.core.reader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import nablarch.test.core.db.BasicDefaultValues;
import nablarch.test.core.db.TableData;
import nablarch.test.core.file.DataFile;
import nablarch.test.core.file.DataFileInspector;
import nablarch.test.core.file.FixedLengthFile;
import nablarch.test.core.messaging.MessagePool;
import nablarch.test.core.messaging.MessagePoolInspector;
import nablarch.test.core.util.interpreter.BinaryFileInterpreter;
import nablarch.test.core.util.interpreter.LineSeparatorInterpreter;
import nablarch.test.core.util.interpreter.NullInterpreter;
import nablarch.test.core.util.interpreter.QuotationTrimmer;
import nablarch.test.core.util.interpreter.TestDataInterpreter;

/**
 * テスティングフレームワーク本体に {@code .xlsx} を読ませ、その値を「正解」として取り出すヘルパ。
 *
 * <p>
 * 変換ツールのテストが期待値を自分で書くと、変換ツールの読みと期待値が同じ写し間違いを持ったときに
 * 検知できない。本クラスは<b>変換ツールのコードを一切通さず</b>、フレームワーク本体のパーサ
 * （{@code TableDataParser}／{@code ListMapParser}／{@code FixedLengthFileParser}／
 * {@code VariableLengthFileParser}／{@code MessageParser}／{@code GroupMessageParser}）を
 * {@code PoiXlsReader} と組み合わせて直接呼び、フレームワークが単体テストで使うのと同じ順の
 * インタープリタ列（{@code null} 記法 → 引用符記法 → 改行記法）を渡す。
 * </p>
 *
 * <p>
 * 本体パーサの取り出し口（{@code getResult}）と {@code MessageParser#getDelegate} は
 * パッケージプライベートのため、本クラスは本体パーサと同じパッケージに置く。
 * </p>
 *
 * <p>
 * グループ ID は本体の API がそのまま要求する<b>整形済み</b>の表記（{@code [case01]}。指定なしは空文字）で
 * 渡す。変換ツールの表記変換（生値 ⇄ 角括弧）を通さないためである。
 * </p>
 *
 * <p>
 * テーブルの読みには {@code DbInfo} が要る。カラム型の取得（一律 {@code VARCHAR}）にしか使われず、
 * 値の意味には影響しないため、変換ツールが持つスタブを共有する。
 * </p>
 */
public final class FrameworkOracle {

    /** フレームワークが単体テストで使うのと同順のインタープリタ列 */
    private static final List<TestDataInterpreter> INTERPRETERS = Collections.unmodifiableList(
            Arrays.<TestDataInterpreter>asList(new NullInterpreter(), new QuotationTrimmer(),
                                               new LineSeparatorInterpreter()));

    /** ユーティリティクラスのため生成させない */
    private FrameworkOracle() {
    }

    /**
     * 取得元パスに対するインタープリタ列を組み立てる。
     *
     * <p>
     * フレームワークは読み込みのたびに、取得元パスを起点に {@code ${binaryFile:相対パス}} を解決する
     * {@link BinaryFileInterpreter} を先頭へ積む。正解の読み手として同じ形にする。
     * </p>
     *
     * @param dir 取得元パス
     * @return インタープリタ列
     */
    private static List<TestDataInterpreter> interpreters(String dir) {
        List<TestDataInterpreter> result = new ArrayList<TestDataInterpreter>();
        result.add(new BinaryFileInterpreter(dir));
        result.addAll(INTERPRETERS);
        return result;
    }

    /**
     * フレームワークが<b>実行時に使う読み手</b>（{@link BasicTestDataParser}）を組み立てる。
     *
     * <p>
     * 本クラスの他のメソッドが本体パーサを直接呼ぶのに対し、こちらは実行時と同じ公開 API を通す。
     * ブロックの読み飛ばし・エントリ件数のように「フレームワークがいくつのエントリを受け取るか」を
     * 正解にしたいテストで使う。
     * </p>
     *
     * @param dir 取得元パス
     * @return パーサ
     */
    private static BasicTestDataParser testDataParser(String dir) {
        BasicTestDataParser parser = new BasicTestDataParser();
        parser.setTestDataReader(new PoiXlsReader());
        parser.setDbInfo(new StubDbInfo());
        parser.setDefaultValues(new BasicDefaultValues());
        parser.setInterpreters(interpreters(dir));
        return parser;
    }

    /**
     * セットアップ用テーブルを、実行時と同じ読み手（{@link BasicTestDataParser}）に読ませる。
     *
     * @param dir      ディレクトリ
     * @param resource リソース名（{@code "ブック名/シート名"}）
     * @param groupId  整形済みグループ ID（指定なしは何も渡さない）
     * @return 本体が読んだテーブル一覧
     */
    public static List<TableData> setupTablesViaTestDataParser(String dir, String resource, String... groupId) {
        return testDataParser(dir).getSetupTableData(dir, resource, groupId);
    }

    /**
     * {@code LIST_MAP} を、実行時と同じ読み手（{@link BasicTestDataParser}）に読ませる。
     *
     * @param dir      ディレクトリ
     * @param resource リソース名
     * @param id       識別子
     * @return 本体が読んだ行ごとのマップ
     */
    public static List<Map<String, String>> listMapViaTestDataParser(String dir, String resource, String id) {
        return testDataParser(dir).getListMap(dir, resource, id);
    }

    /**
     * テーブル系ブロックを本体に読ませる。
     *
     * @param dir      ディレクトリ
     * @param resource リソース名（{@code "ブック名/シート名"}）
     * @param groupId  整形済みグループ ID（指定なしは空文字）
     * @param type     データタイプ（テーブル系）
     * @return 本体が読んだテーブル一覧
     */
    public static List<TableData> tables(String dir, String resource, String groupId, DataType type) {
        TableDataParser parser = new TableDataParser(
                new PoiXlsReader(), interpreters(dir), new StubDbInfo(), new BasicDefaultValues(), type);
        parser.parse(dir, resource, groupId, false);
        return parser.getResult();
    }

    /**
     * テーブルの値を、カラム名の定義順に並べた行のリストとして本体に読ませる。
     *
     * @param dir       ディレクトリ
     * @param resource  リソース名
     * @param groupId   整形済みグループ ID
     * @param type      データタイプ（テーブル系）
     * @param tableName 対象テーブル名
     * @return 行ごとの値（カラム名の定義順）
     */
    public static List<List<String>> tableRows(String dir, String resource, String groupId, DataType type,
                                               String tableName) {
        for (TableData table : tables(dir, resource, groupId, type)) {
            if (table.getTableName().equals(tableName)) {
                List<List<String>> rows = new ArrayList<List<String>>();
                for (int r = 0; r < table.size(); r++) {
                    List<String> row = new ArrayList<String>();
                    for (String column : table.getColumnNames()) {
                        Object value = table.getValue(r, column);
                        row.add(value == null ? null : value.toString());
                    }
                    rows.add(row);
                }
                return rows;
            }
        }
        throw new IllegalArgumentException("table not found. name=[" + tableName + "]");
    }

    /**
     * {@code LIST_MAP} ブロックを本体に読ませる。
     *
     * @param dir      ディレクトリ
     * @param resource リソース名
     * @param id       識別子
     * @return 行ごとのマップ（キーはカラム名）
     */
    public static List<Map<String, String>> listMap(String dir, String resource, String id) {
        ListMapParser parser = new ListMapParser(new PoiXlsReader(), interpreters(dir));
        parser.parse(dir, resource, id, false);
        return parser.getResult();
    }

    /**
     * ファイル系ブロックを本体に読ませる。
     *
     * @param dir      ディレクトリ
     * @param resource リソース名
     * @param groupId  整形済みグループ ID
     * @param type     データタイプ（ファイル系）
     * @return 本体が読んだファイル一覧
     */
    public static List<? extends DataFile> files(String dir, String resource, String groupId, DataType type) {
        DataFileParser<? extends DataFile> parser;
        switch (type) {
            case SETUP_FIXED:
            case EXPECTED_FIXED:
                parser = new FixedLengthFileParser(new PoiXlsReader(), interpreters(dir), type);
                break;
            case SETUP_VARIABLE:
            case EXPECTED_VARIABLE:
                parser = new VariableLengthFileParser(new PoiXlsReader(), interpreters(dir), type);
                break;
            default:
                throw new IllegalArgumentException("unsupported data type. type=[" + type + "]");
        }
        parser.parse(dir, resource, groupId, false);
        return parser.getResult();
    }

    /**
     * 電文（{@code MESSAGE}）の本文を本体に読ませる。
     *
     * @param dir      ディレクトリ
     * @param resource リソース名
     * @param id       電文 ID
     * @return 本文の固定長ファイル一覧
     */
    public static List<FixedLengthFile> messageBodies(String dir, String resource, String id) {
        MessageParser parser = new MessageParser(new PoiXlsReader(), interpreters(dir), DataType.MESSAGE);
        parser.parse(dir, resource, id, false);
        return parser.getDelegate().getResult();
    }

    /**
     * 電文（{@code MESSAGE}）の FW 制御ヘッダを本体に読ませる。
     *
     * @param dir      ディレクトリ
     * @param resource リソース名
     * @param id       電文 ID
     * @return FW 制御ヘッダ
     */
    public static Map<String, String> messageFwHeader(String dir, String resource, String id) {
        MessageParser parser = new MessageParser(new PoiXlsReader(), interpreters(dir), DataType.MESSAGE);
        parser.parse(dir, resource, id, false);
        return parser.getFwHeader();
    }

    /**
     * 送信同期電文（要求／応答電文 4 種）を本体に読ませる。
     *
     * @param dir      ディレクトリ
     * @param resource リソース名
     * @param groupId  整形済みグループ ID
     * @param type     データタイプ（送信同期系）
     * @return 本文の固定長ファイル一覧（記述順）
     */
    public static List<FixedLengthFile> sendSyncBodies(String dir, String resource, String groupId, DataType type) {
        GroupMessageParser parser = new GroupMessageParser(new PoiXlsReader(), interpreters(dir), type);
        parser.parse(dir, resource, groupId, false);
        List<FixedLengthFile> bodies = new ArrayList<FixedLengthFile>();
        for (MessagePool pool : parser.getResult()) {
            bodies.add(MessagePoolInspector.sourceOf(pool));
        }
        return bodies;
    }

    /**
     * ファイル・電文の値を、行ごとの値のリストとして取り出す。
     *
     * <p>
     * 本体が保持している値（末尾の空要素を取り除いたうえでフィールド名の数まで空文字で埋め直したあとの値）を
     * そのまま並べる。{@code DataFile#toDataRecords()} は固定長レコードとしての型変換を伴い、空文字が
     * {@code null} へ変わるため使わない。
     * </p>
     *
     * @param file 本体が読んだファイル
     * @return 行ごとの値（フィールド名の定義順）
     */
    public static List<List<String>> recordValues(DataFile file) {
        return DataFileInspector.values(file);
    }
}
