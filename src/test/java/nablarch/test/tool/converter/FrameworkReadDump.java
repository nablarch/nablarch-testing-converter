package nablarch.test.tool.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nablarch.test.core.db.TableData;
import nablarch.test.core.file.DataFile;
import nablarch.test.core.file.DataFileInspector;
import nablarch.test.core.reader.YamlFrameworkOracle;

/**
 * テスティングフレームワークが読んだ結果（{@code FrameworkOracle}・{@code YamlFrameworkOracle} の戻り値）を、
 * 突き合わせ用の文字列へ写すヘルパ。
 *
 * <p>
 * Excel 形式と YAML 形式を本体に読ませた結果を、同じ並べ方の文字列にして比べるために使う。
 * </p>
 *
 * <p>
 * ファイル・電文は、値を写す {@link #files(List)} と、値以外（ディレクティブ・レコード種別・型・長さ）を
 * 写す {@link #fileLayouts(List)} に分けている。変換ツールは値以外を生の行や原文から取り直して書き出すため、
 * 値だけを比べると、書き出しと読み戻しが同じ間違いをしたときに見逃す。値以外も比べたいときは両方を使う。
 * </p>
 */
public final class FrameworkReadDump {

    /** ユーティリティクラスのため生成させない */
    private FrameworkReadDump() {
    }

    /**
     * テーブルの一覧を、テーブル名・カラム名・行の値の文字列へ写す。
     *
     * @param tables 本体が読んだテーブルの一覧
     * @return 表現
     */
    public static String tables(List<TableData> tables) {
        StringBuilder sb = new StringBuilder();
        for (TableData table : tables) {
            sb.append(table.getTableName())
                    .append(' ').append(Arrays.asList(table.getColumnNames()))
                    .append(' ').append(YamlFrameworkOracle.rowsOf(table)).append('\n');
        }
        return sb.toString();
    }

    /**
     * ファイルの一覧を、パス・フィールド名・値の文字列へ写す。
     *
     * @param files 本体が読んだファイルの一覧
     * @return 表現
     */
    public static String files(List<? extends DataFile> files) {
        StringBuilder sb = new StringBuilder();
        for (DataFile file : files) {
            sb.append(file.getPath())
                    .append(' ').append(DataFileInspector.fieldNames(file))
                    .append(' ').append(DataFileInspector.values(file)).append('\n');
        }
        return sb.toString();
    }

    /**
     * ファイルの一覧を、パス・ディレクティブ・レコード種別・型・長さの文字列へ写す。
     *
     * <p>
     * レコード種別を省略したことを表す 3 つの表記（Excel の空のセルを本体が読んだ空文字、YAML の
     * {@code record_type} を省略したときの {@code default}、YAML に明示した {@code Default}）は、
     * 同じ {@value #OMITTED_RECORD_TYPE} に写す。変換ツールはレコード種別の省略を {@code null} で持ち、
     * Excel には空のセル、YAML には {@code record_type} の省略で書く仕様であり、形式をまたぐと表記が変わるためである。
     * 省略できるのはレコード定義が 1 つのブロックだけで（2 件目以降の省略は Excel へ書くときに止める）、
     * レコード種別の名前がファイルの読み書きの結果を変えないため、同じ意味として扱う。
     * それ以外のレコード種別は、書いたとおりの文字列で比べる。
     * </p>
     *
     * @param files 本体が読んだファイルの一覧
     * @return 表現
     */
    public static String fileLayouts(List<? extends DataFile> files) {
        StringBuilder sb = new StringBuilder();
        for (DataFile file : files) {
            List<String> recordTypes = new ArrayList<>();
            for (String recordType : DataFileInspector.recordTypes(file)) {
                recordTypes.add(isOmittedRecordType(recordType) ? OMITTED_RECORD_TYPE : recordType);
            }
            sb.append(file.getPath())
                    .append(' ').append(DataFileInspector.directives(file))
                    .append(" recordTypes=").append(recordTypes)
                    .append(" types=").append(DataFileInspector.types(file))
                    .append(" lengths=").append(DataFileInspector.lengths(file)).append('\n');
        }
        return sb.toString();
    }

    /** レコード種別を省略したことを表す、比較用の表記。 */
    private static final String OMITTED_RECORD_TYPE = "(omitted)";

    /**
     * レコード種別を省略したことを表す表記かを判定する。
     *
     * @param recordType 本体が読んだレコード種別
     * @return {@code null}・空文字・{@code default}（大文字と小文字を区別しない）なら真
     */
    private static boolean isOmittedRecordType(String recordType) {
        return recordType == null || recordType.isEmpty() || "default".equalsIgnoreCase(recordType);
    }

    /**
     * {@code LIST_MAP} の行の一覧を、キーの辞書順に並べた文字列へ写す。
     *
     * @param rows 本体が読んだ行の一覧
     * @return 表現
     */
    public static String listMap(List<Map<String, String>> rows) {
        List<String> dump = new ArrayList<>();
        for (Map<String, String> row : rows) {
            dump.add(new TreeMap<>(row).toString());
        }
        return dump.toString();
    }
}
