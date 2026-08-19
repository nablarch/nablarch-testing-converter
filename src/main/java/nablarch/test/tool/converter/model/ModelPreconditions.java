package nablarch.test.tool.converter.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * Map とその<b>キー・値</b>が {@code null} でないことを検査する。
     *
     * <p>
     * 呼び出し元はディレクティブの Map（{@link FileDataBlock#getDirectives()} ／
     * {@link MessageDataBlock#getDirectives()}）と、同じ「名前・値」形式で記述する
     * フレームワーク制御ヘッダ（{@link MessageDataBlock#getFwHeaderFields()}。
     * {@code testdata_notation.rst:1267}）である。
     * </p>
     *
     * <p>
     * <b>キーが {@code null} でも値が {@code null} でも、記法どおりには書けない。</b>
     * {@code testdata_notation.rst:906}（{@code 30a8271} 時点）は「ディレクティブは、ファイル・電文の
     * フォーマットに関する属性を、<b>キー名と値の 2 要素</b>で記述するものである（最低 2 要素が必要）」と定め、
     * {@code :892} は記述時エラーとして「ディレクティブまたはレコード種別・フィールド名称定義の
     * <b>要素数が 2 未満である</b>」を挙げる。本体スキーマ {@code $defs.directives} も
     * 各キーの値を {@code string} ／ {@code boolean} ／ {@code integer} と定めるだけで
     * {@code null} を許す定義を持たず、{@code additionalProperties} が {@code false} であるため
     * {@code null} のキーはそもそも書けない（{@code coverage/issues.md} <b>XLS-43</b>）。
     * </p>
     *
     * <p>
     * <b>空文字は拒否しない。</b>空文字を禁じる明文が無いためである（ユーザー確定・2026-08-19）。
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
     * 現在の呼び出し元は {@link RecordLayout} のフィールド定義群だけである。
     * {@code testdata_notation.rst:888}（{@code 30a8271} 時点）はファイルデータの記述時エラーとして
     * 「フィールド名称リストまたはデータ型リストが未指定または<b>空である</b>」を挙げ、
     * 本体スキーマ {@code $defs.record_fragment} は {@code fields} を必須かつ {@code minItems} ＝ 1 とする。
     * したがってフィールド 0 件のレコードレイアウトは<b>どちらの記法にも存在しない形</b>であり、
     * 生成時点で拒否する（{@code coverage/issues.md} <b>XLS-22</b> ／ <b>YML-12</b> の 3 形目。
     * もとは辺③④の書き出し側に置いていたが、2026-08-19 に生成時へ移した）。
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
     * データ行の要素数が<b>上限件数以下</b>であることを検査する。<b>不足は通す。</b>
     *
     * <p>
     * {@code testdata_notation.rst:891}（{@code 30a8271} 時点）はファイルデータの記述時エラーとして
     * 「データ要素数が不正である」を挙げる。一方 {@code :883} は<b>不足側だけを正常と定め</b>、
     * 「フィールドの数だけ値を記述しなかった場合、記述しなかったフィールドの値は空文字となる」と書く。
     * したがって不変条件は<b>「行の要素数 ≦ フィールド定義の件数」</b>であって、
     * <b>一致の強制ではない</b>（{@code coverage/issues.md} <b>XLS-41</b>）。
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
     * 現在の呼び出し元は {@link RecordLayout} のフィールド名称だけである。
     * {@code testdata_notation.rst:887}（{@code 30a8271} 時点）「同一レコード種別内でフィールド名称が
     * 重複している」が記述時のエラーとして明文で禁じている（{@code coverage/issues.md} <b>XLS-40</b>）。
     * </p>
     *
     * <p>
     * <b>テーブル系のカラム名には使っていない。</b>辺②が重複したカラム名を持つブロックを実際に作る
     * ためである（<b>YML-10</b>。本体 {@code TableData} の大文字化により {@code id} と {@code ID} が
     * {@code [ID, ID]} になる）。番人を置くと仕様適合入力を変換できなくなるため、
     * <b>カラム名側は他責として扱い、番人も WARN も置かない</b>（ユーザー確定・2026-08-19。
     * 根拠は {@link ColumnRowDataBlock} の Javadoc）。
     * </p>
     *
     * <p>
     * <b>大文字小文字の違いは重複としない。</b>記法に大文字小文字を同一視する明文が無く、
     * 中間モデルは名前を大文字化せず記法のまま保持するためである。
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
