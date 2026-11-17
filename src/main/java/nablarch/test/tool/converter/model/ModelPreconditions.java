package nablarch.test.tool.converter.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 中間モデルの不変条件のうち、コレクション・Map に共通するものをまとめた内部ユーティリティ。
 *
 * <p>
 * 中間モデルのコレクション・Map とその要素は {@code null} を取らない。
 * 記法は空であることと無いことを区別せず、{@code null} に当たる書き方が無いためである（無いことは要素 0 件で表す）。
 * ただしデータ行のセルの {@code null} は記法が書き方を定めているため通す。
 * </p>
 *
 * <p>
 * スカラ値の {@code null} 検査はここに置かず、各クラスに直接書く。例外メッセージがクラスごとに異なるためである。
 * </p>
 *
 * @author kiyotis
 */
final class ModelPreconditions {

    /** インスタンス化させない。 */
    private ModelPreconditions() {
    }

    /**
     * リストとその要素が {@code null} でないことを検査する。
     *
     * @param label 呼び出し側が例外メッセージに出す項目名
     * @param list  検査対象
     * @param <T>   要素の型
     * @return {@code list} をそのまま返す
     * @throws IllegalArgumentException {@code list} またはその要素が {@code null} の場合
     */
    static <T> List<T> requireNoNulls(String label, List<T> list) {
        requireNotNull(label, list);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == null) {
                throw new IllegalArgumentException(
                        label + "の " + (i + 1) + " 件目が null です。" + REASON);
            }
        }
        return list;
    }

    /**
     * データ行のリストと各行が {@code null} でないことを検査する。<b>行の中のセルは {@code null} を通す。</b>
     *
     * @param label 呼び出し側が例外メッセージに出す項目名
     * @param rows  検査対象
     * @return {@code rows} をそのまま返す
     * @throws IllegalArgumentException {@code rows} またはその要素（行）が {@code null} の場合
     */
    static List<List<String>> requireNoNullRows(String label, List<List<String>> rows) {
        return requireNoNulls(label, rows);
    }

    /**
     * Map とその<b>キー・値</b>が {@code null} でないことを検査する。
     *
     * <p>
     * 記法はディレクティブ等をキー名と値の 2 要素で書くと定めており、{@code null} に当たる書き方が無い。
     * 空文字は禁じる明文が無いため拒否しない。
     * </p>
     *
     * @param label 呼び出し側が例外メッセージに出す項目名
     * @param map   検査対象
     * @return {@code map} をそのまま返す
     * @throws IllegalArgumentException {@code map} またはそのキー・値が {@code null} の場合
     */
    static Map<String, String> requireNoNulls(String label, Map<String, String> map) {
        requireNotNull(label, map);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException(label + "のキーに null は指定できません。" + MAP_REASON);
            }
            if (entry.getValue() == null) {
                throw new IllegalArgumentException(
                        label + " \"" + entry.getKey() + "\" の値が null です。" + MAP_REASON);
            }
        }
        return map;
    }

    /**
     * リストが<b>1 件以上</b>であることを検査する。
     *
     * <p>
     * フィールド 0 件のレコードレイアウトを拒否するために使う（理由は {@link RecordLayout} を参照）。
     * </p>
     *
     * @param label 呼び出し側が例外メッセージに出す項目名
     * @param list  検査対象
     * @throws IllegalArgumentException {@code list} が空の場合
     */
    static void requireNotEmpty(String label, List<?> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException(
                    label + "は 1 件以上必要です"
                            + "（記法はフィールドを持たないレコードレイアウトを認めていません）。");
        }
    }

    /**
     * レコードレイアウト群のすべてのフィールド定義が<b>フィールド長を持つ</b>ことを検査する。
     *
     * <p>
     * 固定長ファイルと電文が対象である。記法がフィールド名称・データ型・フィールド長の 3 リストを
     * 同サイズで必須とするためである。弾くのは {@code null} だけで、空文字は禁じる明文が無いため通す。
     * </p>
     *
     * @param records    検査対象
     * @param identifier 識別子（診断メッセージ用）
     * @throws IllegalArgumentException フィールド長が {@code null} のフィールド定義が含まれる場合
     */
    static void requireLengths(List<RecordLayout> records, String identifier) {
        for (int i = 0; i < records.size(); i++) {
            RecordLayout record = records.get(i);
            for (FieldDef field : record.getFields()) {
                if (field.getLength() == null) {
                    throw new IllegalArgumentException(
                            "固定長ファイル・電文でフィールド長を持たないフィールド定義は保持できません"
                                    + "（記法はフィールド名称・データ型・フィールド長の 3 リストが"
                                    + "同サイズであることを求めており、長さを落とすと記法どおりには"
                                    + "書き出せません）。"
                                    + " 識別子=[" + identifier + "] レコード番号=" + i
                                    + " フィールド名=[" + field.getName() + "]");
                }
            }
        }
    }

    /**
     * レコードレイアウト群のすべてのフィールド定義が<b>フィールド長を持たない</b>ことを検査する。
     *
     * <p>
     * 可変長ファイルが対象である。Excel 記法に可変長のフィールド長行が無く、書き出す先が無いため、
     * 生成時に拒否する。{@code "-"} や空文字も含め、{@code null} 以外はすべて弾く
     * （{@code "-"} は本体では値の整形に作用するが、長さの指定ではないため追認しない）。
     * </p>
     *
     * @param records    検査対象
     * @param identifier 識別子（診断メッセージ用）
     * @throws IllegalArgumentException フィールド長が {@code null} でないフィールド定義が含まれる場合
     */
    static void requireNoLengths(List<RecordLayout> records, String identifier) {
        for (int i = 0; i < records.size(); i++) {
            RecordLayout record = records.get(i);
            for (FieldDef field : record.getFields()) {
                if (field.getLength() != null) {
                    throw new IllegalArgumentException(
                            "可変長ファイルでフィールド長を持つフィールド定義は保持できません"
                                    + "（記法は可変長ファイルにフィールド長行を持たないため、"
                                    + "書き出す先がありません）。"
                                    + " 識別子=[" + identifier + "] レコード番号=" + i
                                    + " フィールド名=[" + field.getName() + "]"
                                    + " フィールド長=[" + field.getLength() + "]");
                }
            }
        }
    }

    /**
     * データ行の要素数が<b>上限件数以下</b>であることを検査する。<b>不足は通す。</b>
     *
     * <p>
     * 記法は不足したフィールドを {@code ""} で補完すると定めているため、一致は強制しない。
     * </p>
     *
     * @param label 呼び出し側が例外メッセージに出す項目名
     * @param rows  検査対象
     * @param limit 1 行あたりの要素数の上限（フィールド定義の件数）
     * @throws IllegalArgumentException いずれかの行の要素数が {@code limit} を超える場合
     */
    static void requireRowsNotLongerThan(String label, List<List<String>> rows, int limit) {
        for (int i = 0; i < rows.size(); i++) {
            int size = rows.get(i).size();
            if (size > limit) {
                throw new IllegalArgumentException(
                        label + "の " + (i + 1) + " 件目の要素数 " + size
                                + " がフィールド定義の件数 " + limit + " を超えています"
                                + "（記法は値の不足だけを認め、余りの値を書く場所がありません）。");
            }
        }
    }

    /**
     * 名前のリストに重複が無いことを検査する。判定は<b>完全一致</b>で行う。
     *
     * <p>
     * 大文字小文字の違いは、同一視する明文が無いため重複としない。
     * テーブル系のカラム名には使わない（理由は {@link ColumnRowDataBlock} を参照）。
     * </p>
     *
     * @param label 呼び出し側が例外メッセージに出す項目名
     * @param names 検査対象
     * @throws IllegalArgumentException {@code names} に同じ値が 2 つ以上ある場合
     */
    static void requireNoDuplicates(String label, List<String> names) {
        Set<String> seen = new HashSet<>();
        for (String name : names) {
            if (!seen.add(name)) {
                throw new IllegalArgumentException(
                        label + " \"" + name + "\" が重複しています"
                                + "（同一レコード種別内で名前は重複できません）。");
            }
        }
    }

    /** Map のキー・値の {@code null} を拒む理由。例外メッセージの共通部分。 */
    private static final String MAP_REASON =
            "（記法はキー名と値の 2 要素で記述することを定めており、null に当たる書き方がありません）。";

    /** {@code null} を拒む理由。例外メッセージの共通部分。 */
    private static final String REASON =
            "（記法は「空である」ことと「無い」ことを区別せず、null に当たる書き方がありません。"
                    + "「無い」ことは要素 0 件で表してください）。";

    /**
     * {@code null} でないことを検査する。
     *
     * @param label 呼び出し側が例外メッセージに出す項目名
     * @param value 検査対象
     * @throws IllegalArgumentException {@code value} が {@code null} の場合
     */
    private static void requireNotNull(String label, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(label + "に null は指定できません。" + REASON);
        }
    }
}
