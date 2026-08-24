package nablarch.test.tool.converter.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 中間モデルの不変条件のうち、コレクション・Map に共通するものをまとめた内部ユーティリティ。
 *
 * <p>
 * <b>ここへ切り出す基準は「呼び出し元が何箇所あるか」ではなく「コレクション・Map に共通する検査か」である。</b>
 * 呼び出し元が 1 つしかない検査（{@link #requireNotEmpty} ／ {@link #requireNoDuplicates} ／
 * {@link #requireRowsNotLongerThan}。いずれも {@code RecordLayout} だけ）もここに置く。
 * 逆に、スカラ 1 つの検査（{@code name} ／ {@code groupId} ／ {@code identifier} ／ {@code dataType} の
 * {@code null}）は、たとえ複数のクラスで同じ形になっていても<b>各クラスの生成時に直接書く</b>——
 * 例外メッセージがクラスごとに違う説明（Excel 形式では何になり、YAML 形式では何になるか）を持つためである。
 * </p>
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
     * レコードレイアウト群のすべてのフィールド定義が<b>フィールド長を持つ</b>ことを検査する。
     *
     * <p>
     * 呼び出し元は<b>固定長</b>の {@link FileDataBlock} と {@link MessageDataBlock} である。
     * {@code testdata_notation.rst:883}（{@code 30a8271} 時点）は固定長ファイルについて
     * 「フィールド名称・データ型・フィールド長の3リストが同サイズで必須であり」と定め（逐語）、
     * {@code :889} は記述時エラーとして「フィールド名称・データ型・フィールド長リストのサイズが
     * 一致していない」を挙げる。電文も {@code :1158}「フレームワーク制御ヘッダ以降のメッセージボディは、
     * フィールド名称・データ型・フィールド長・データという、前述のファイルデータと同じ構成を持つ」により
     * 同じ制約に掛かる（{@code coverage/issues.md} <b>XLS-30</b>。もとは辺③④の書き出し側に置いていたが、
     * 2026-08-19 に生成時へ移した）。
     * </p>
     *
     * <p>
     * <b>可変長ファイルは呼び出さない。</b>{@code :883} は「可変長ファイルでは、フィールド名称・データ型の
     * 2リストが同サイズで必須であり、フィールド長は不要である」と定めており、{@code null} が正しい形である。
     * 電文にはこの逃げ道が無いため常に必須となる。<b>可変長ファイルは代わりに
     * {@link #requireNoLengths(List, String)} を呼ぶ</b>——「不要」ではなく<b>書けない</b>ためである
     * （{@code coverage/issues.md} <b>XLS-45</b>）。
     * </p>
     *
     * <p>
     * <b>弾くのは {@code null} だけで、空文字は弾かない。</b>空文字を禁じる明文が無いためである
     * （{@link FieldDef#getType()} の番人と同じ境界）。
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
     * 呼び出し元は<b>可変長</b>の {@link FileDataBlock} だけである。
     * <b>NTF 仕様として、可変長ファイルでは {@code length} を書けない</b>（ユーザー確定・2026-08-24。
     * {@code coverage/issues.md} <b>XLS-45</b>）。Excel 記法に可変長のフィールド長行が無いためである
     * （{@code testdata_notation.rst:1076}（{@code 30a8271} 時点）「固定長との違いは、可変長ファイルの
     * 場合はフィールド長行を記載しない点のみである。」／{@code :883}「可変長ファイルでは、
     * フィールド名称・データ型の2リストが同サイズで必須であり、フィールド長は不要である。」）。
     * 書ける先の無い値を中間モデルが保持できると、辺③（中間モデル → Excel）で例外にも警告にもならずに
     * 落ちる（{@code XlsFormatWriter#appendRecord} は長さ行を {@code if (fixed)} の中でしか作らない）。
     * <b>生成時点で拒否し、書き出し側には番人を置かない</b>
     * （{@code steering.md} Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」）。
     * </p>
     *
     * <p>
     * <b>弾くのは {@code null} 以外のすべてで、{@code "-"} も空文字も弾く。</b>
     * {@link #requireLengths(List, String)}（固定長側）が「弾くのは {@code null} だけ」であるのと
     * 境界が逆である。可変長ではフィールド長行そのものが記法に無く、どの表記であっても書き出す先が
     * 無いためである。<b>{@code "-"}（オンデマンド計算）も例外ではない</b>——本体
     * {@code nablarch/test/core/file/DataFileFragment.java} の {@code setLengths} が {@code "-"} を
     * {@code isOndemandCalcFieldSizeList} に立て、{@code addValue} が当該フィールドの値から改行と
     * 前後空白を除去するため、可変長でも NTF の格納値が変わる。しかしこれは<b>長さの指定ではなく
     * 値の整形であり、フィールド長の枠に相乗りしている</b>だけであって、追認すべき仕様ではない
     * （ユーザー確定・2026-08-24）。数値の {@code length} は
     * {@code VariableLengthFileFragment#createFieldDefinition} が {@code lengths} を一度も読まないため、
     * 可変長では NTF 実行時に使われない。
     * </p>
     *
     * <p>
     * <b>電文（{@link MessageDataBlock}）は呼び出さない。</b>常に固定長であり
     * （{@code :1158}「フレームワーク制御ヘッダ以降のメッセージボディは、フィールド名称・データ型・
     * フィールド長・データという、前述のファイルデータと同じ構成を持つ」）、
     * {@link #requireLengths(List, String)} の側に掛かる。
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
     * {@code testdata_notation.rst:891}（{@code 30a8271} 時点）はファイルデータの記述時エラーとして
     * 「データ要素数が不正である」を挙げる。一方 {@code :883} は<b>不足側だけを正常と定め</b>、
     * 「データ行のセル数（Excel形式）または ``rows:`` の各要素の長さ（YAML形式）がフィールド数より
     * 少ない場合、不足したフィールドは ``""`` として補完される」と書く（逐語）。
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
