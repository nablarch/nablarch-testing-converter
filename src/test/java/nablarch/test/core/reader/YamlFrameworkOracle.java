package nablarch.test.core.reader;

import java.util.ArrayList;
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
import nablarch.test.core.util.interpreter.TestDataInterpreter;

/**
 * YAML 形式のテストデータを、テスティングフレームワークの YAML 読み込み
 * （{@code nablarch-testing-yaml} の {@link YamlTestDataParser}）に読ませて「正解」として取り出すヘルパ。
 *
 * <p>
 * {@link FrameworkOracle} の YAML 版である。変換ツールのリーダを通さず、フレームワークが実行時に使うのと
 * 同じ経路で読む。<b>Excel 形式の記法を解釈するインタープリタは渡さない</b>（下の {@code INTERPRETERS} 参照）。
 * </p>
 *
 * <p>
 * 取り出すのは<b>加工しない値</b>である。YAML 側の {@code getExpectedTableData} は
 * {@code expected_tables} と {@code expected_complete_tables} を並べて返すだけで、
 * デフォルト値の補完や期待値のマージは行わない（フレームワーク本体の既定実装とはここが違う）。
 * </p>
 */
public final class YamlFrameworkOracle {

    /**
     * YAML 形式で使うインタープリタ列（空）。
     *
     * <p>
     * Excel 形式の記法を解釈する 3 つ（{@code null} 記法・引用符記法・改行記法）は YAML 形式では使わない。
     * YAML では {@code null} と {@code "null"} の区別も、値を囲むダブルクォートの除去も、
     * {@code "\r"} ／ {@code "\n"} の制御文字への変換も、YAML のパーサ自身が行うためである。
     * 前後のダブルクォート 1 層の除去は YAML 形式では行われず、値の中のダブルクォートはそのまま値になる。
     * </p>
     *
     * <p>
     * {@code ${binaryFile:相対パス}} を取得元パス起点で解決するインタープリタは、
     * {@link YamlTestDataParser} が読み込みのたびに自動で先頭へ積む。
     * </p>
     */
    private static final List<TestDataInterpreter> INTERPRETERS = Collections.emptyList();

    /** ユーティリティクラスのため生成させない */
    private YamlFrameworkOracle() {
    }

    /**
     * YAML 読み込み用のパーサを組み立てる。
     *
     * @return パーサ
     */
    private static YamlTestDataParser parser() {
        YamlTestDataParser parser = new YamlTestDataParser();
        parser.setDbInfo(new StubDbInfo());
        parser.setDefaultValues(new BasicDefaultValues());
        parser.setInterpreters(INTERPRETERS);
        return parser;
    }

    /**
     * セットアップ用テーブルを YAML から読む。
     *
     * @param dir      ディレクトリ
     * @param resource リソース名
     * @param groupId  生値のグループ ID（指定なしは何も渡さない）
     * @return テーブル一覧
     */
    public static List<TableData> setupTables(String dir, String resource, String... groupId) {
        return parser().getSetupTableData(dir, resource, groupId);
    }

    /**
     * テーブルの値を、カラム名の定義順に並べた行のリストとして YAML から読む。
     *
     * @param dir       ディレクトリ
     * @param resource  リソース名
     * @param tableName 対象テーブル名
     * @param groupId   生値のグループ ID
     * @return 行ごとの値（カラム名の定義順）
     */
    public static List<List<String>> setupTableRows(String dir, String resource, String tableName,
                                                    String... groupId) {
        for (TableData table : setupTables(dir, resource, groupId)) {
            if (table.getTableName().equals(tableName)) {
                return rowsOf(table);
            }
        }
        throw new IllegalArgumentException("table not found. name=[" + tableName + "]");
    }

    /**
     * テーブル器の値を、カラム名の定義順に並べた行のリストへ写す。
     *
     * @param table テーブル器
     * @return 行ごとの値
     */
    public static List<List<String>> rowsOf(TableData table) {
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

    /**
     * 期待値のテーブルを YAML から読む。
     *
     * <p>
     * YAML 側の実装は {@code expected_tables} と {@code expected_complete_tables} を並べて返すだけで、
     * デフォルト値の補完や期待値のマージは行わない。
     * </p>
     *
     * @param dir      ディレクトリ
     * @param resource リソース名
     * @param groupId  生値のグループ ID
     * @return テーブル一覧
     */
    public static List<TableData> expectedTables(String dir, String resource, String... groupId) {
        return parser().getExpectedTableData(dir, resource, groupId);
    }

    /**
     * {@code LIST_MAP} を YAML から読む。
     *
     * @param dir      ディレクトリ
     * @param resource リソース名
     * @param id       識別子
     * @return 行ごとのマップ
     */
    public static List<Map<String, String>> listMap(String dir, String resource, String id) {
        return parser().getListMap(dir, resource, id);
    }

    /**
     * セットアップ用ファイルを YAML から読む。
     *
     * @param dir      ディレクトリ
     * @param resource リソース名
     * @param groupId  生値のグループ ID
     * @return ファイル一覧
     */
    public static List<DataFile> setupFiles(String dir, String resource, String... groupId) {
        return parser().getSetupFile(dir, resource, groupId);
    }

    /**
     * 期待値のファイルを YAML から読む。
     *
     * @param dir      ディレクトリ
     * @param resource リソース名
     * @param groupId  生値のグループ ID
     * @return ファイル一覧
     */
    public static List<DataFile> expectedFiles(String dir, String resource, String... groupId) {
        return parser().getExpectedFile(dir, resource, groupId);
    }

    /**
     * 電文の本文を YAML から読む。
     *
     * @param dir      ディレクトリ
     * @param resource リソース名
     * @param id       電文 ID
     * @return 本文の固定長ファイル
     */
    public static FixedLengthFile messageBody(String dir, String resource, String id) {
        MessagePool pool = parser().getMessage(dir, resource, id);
        return MessagePoolInspector.sourceOf(pool);
    }

    /**
     * 送信同期電文の本文を YAML から読む。
     *
     * @param dir      ディレクトリ
     * @param resource リソース名
     * @param id       生値のグループ ID
     * @param type     データタイプ（送信同期系）
     * @return 本文の固定長ファイル一覧
     */
    public static List<FixedLengthFile> sendSyncBodies(String dir, String resource, String id, DataType type) {
        List<FixedLengthFile> bodies = new ArrayList<FixedLengthFile>();
        for (MessagePool pool : parser().getSendSyncMessage(dir, resource, id, type)) {
            bodies.add(MessagePoolInspector.sourceOf(pool));
        }
        return bodies;
    }

    /**
     * ファイル・電文の値を、行ごとの値のリストとして取り出す。
     *
     * @param file 読んだファイル
     * @return 行ごとの値（フィールド名の定義順）
     */
    public static List<List<String>> recordValues(DataFile file) {
        return DataFileInspector.values(file);
    }
}
