package nablarch.test.tool.converter.model;

import java.util.List;
import java.util.Map;

/**
 * 中間モデルの不変条件のうち、コレクション・Map に共通するものをまとめた内部ユーティリティ。
 *
 * <p>
 * <b>中間モデルのコレクション・Map と、その要素は {@code null} を取らない。</b>
 * 記法は「空である」ことと「無い」ことを区別せず、カラム 0 個・行 0 件・ディレクティブなしは
 * すべて<b>要素 0 件</b>で表す。{@code null} に対応する書き方が記法に無いため、
 * {@code null} を保持できると 4 辺のどこにも書き出せない中間モデルができてしまう
 * （{@code coverage/issues.md} <b>XLS-38</b>）。生成時点で拒否する。
 * </p>
 *
 * <p>
 * <b>例外はデータ行の「セル」である。</b>セルの {@code null} は記法が明示的に定めている
 * （{@code testdata_notation.rst:767-772}（{@code 30a8271} 時点）Excel 形式は「セルに {@code null}
 * （大文字小文字不問）」／「セルを空にする（{@code null} 扱い）」、{@code :829-834} YAML 形式は
 * 「アンクォートの {@code null}」／「{@code ""}（{@code null} 扱い）」）。
 * したがってセルの {@code null} は通し、<b>行そのもの</b>の {@code null} は拒否する
 * （行が「無い」ことを表す書き方は記法に無い）。
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
     * Map が {@code null} でないことを検査する。
     *
     * <p>
     * <b>キー・値の {@code null} は検査しない。</b>ディレクティブ値が {@code null} のブロックは
     * 辺③が空セルとして書き出すため {@code NullPointerException} にならず、XLS-38 の観測
     * （{@code null} を入れると両辺が例外になる 10 箇所）に含まれていない。往復すると {@code ""}
     * になる点は未評価であり、台帳の判定が無い形をここで独断で拒否しない。
     * 担保テストは {@code XlsFormatWriterTest#writesOmittedMetaAndFieldAsEmpty}。
     * </p>
     *
     * @param label 呼び出し側が例外メッセージに出す項目名
     * @param map   検査対象
     * @return {@code map} をそのまま返す
     * @throws IllegalArgumentException {@code map} が {@code null} の場合
     */
    static Map<String, String> requireNoNulls(String label, Map<String, String> map) {
        requireNotNull(label, map);
        return map;
    }

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
