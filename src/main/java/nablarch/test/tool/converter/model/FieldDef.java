package nablarch.test.tool.converter.model;

/**
 * ファイル／メッセージのフィールド定義。不変オブジェクト。
 *
 * <p>
 * 名称・型・長さを、形式の記法を加工せずそのまま保持する。長さの省略（可変長ファイル）は {@code null}
 * （可変長ファイルでは {@code null} 以外を取れない。下記）。
 * 名称は大文字化しない。長さは {@code "-"}（自動拡張指示）等の記法もありうるためリテラルとして
 * {@link String} で保持する（数値化しない）。
 * </p>
 *
 * <p>
 * <b>{@code name} と {@code type} は必須であり {@code null} であってはならない。生成時点で拒否する。</b>
 * Excel 記法・YAML 記法のいずれも、名称またはデータ型を持たないフィールド定義を認めていないためである
 * （Excel は {@code testdata_notation.rst:883}（{@code 30a8271} 時点）が固定長ファイルで「フィールド名称・
 * データ型・フィールド長の3リストが同サイズで必須」・可変長ファイルで「フィールド名称・データ型の2リストが
 * 同サイズで必須」と定め、{@code :888} が「フィールド名称リストまたはデータ型リストが未指定または空である」を
 * 記述時のエラーに挙げる。YAML は本体スキーマ {@code nablarch/test/ntf-testdata-yaml-schema.json} の
 * {@code $defs.field_def} が {@code required} ＝ {@code ["name", "type"]} とする）。
 * <b>空文字は拒否しない</b>——{@code $defs.field_def.name} に {@code minLength} が無いため
 * {@code name: ""} はスキーマに適合する。
 * 中間モデルの契約は 4 辺すべてが表現できる範囲で定める。
 * </p>
 *
 * <p>
 * <b>この拒否は入力の検証ではなく不変条件の保証である。</b>NTF 仕様に合わない Excel ／ YAML が
 * 読み取りで落ちるのは従来どおり正しい。それとは別に、中間モデルへ {@code null} が入ることは呼び出し側の
 * バグであり、生成時点で露見させる。したがって書き出し側（{@code XlsFormatWriter} ／
 * {@code YamlFormatWriter}）には番人を置かない（{@code coverage/issues.md} <b>XLS-31</b> ／ <b>YML-12</b>。
 * 方針は {@code steering.md} Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」）。
 * </p>
 *
 * <p>
 * <b>{@code length} は条件つきで必須である——固定長ファイル・電文のフィールド定義では {@code null} で
 * あってはならず、可変長ファイルのフィールド定義では {@code null} でなければならない。</b>
 * 記法が固定長と可変長で異なる制約を置いているためである（{@code testdata_notation.rst:883}
 * （{@code 30a8271} 時点）が固定長ファイルで「フィールド名称・データ型・フィールド長の3リストが同サイズで
 * 必須」・可変長ファイルで「フィールド名称・データ型の2リストが同サイズで必須であり、フィールド長は不要である」と
 * 定め、{@code :889} が「フィールド名称・データ型・フィールド長リストのサイズが一致していない」を記述時の
 * エラーに挙げる。電文も {@code :1158}「フレームワーク制御ヘッダ以降のメッセージボディは、フィールド名称・
 * データ型・フィールド長・データという、前述のファイルデータと同じ構成を持つ」により同じ制約に掛かる）。
 * <b>本体スキーマ {@code $defs.field_def} の {@code required} は {@code ["name", "type"]} であり
 * {@code length} を含まない</b>——{@code $defs.record_fragment} が固定長ファイル・可変長ファイル・電文で
 * 共用されるため、必須か否かを {@code required} では表せないからである。ただし同スキーマ自身が
 * {@code length} の説明に「固定長ファイルでは実質必須（省略すると NTF が record-length を計算できない）。
 * 可変長ファイルでは不要（省略可）」と書いており、記法の明文と食い違ってはいない。
 * 中間モデルの契約は 4 辺すべてが表現できる範囲で定める。<b>ただし固定長か可変長かは本クラス単体では
 * 判らないため、{@code length} は本クラスの生成時には拒否できない。</b>文脈を持つ側が生成時に拒否する——
 * {@link nablarch.test.tool.converter.model.FileDataBlock}（固定長系のデータ種別のとき）と
 * {@link nablarch.test.tool.converter.model.MessageDataBlock}（常に）が
 * {@code ModelPreconditions#requireLengths} を呼ぶ（{@code coverage/issues.md} <b>XLS-30</b>。
 * <b>2026-08-19 の §6-J-3（{@code b762438}）で書き出し側から移設した</b>ので、
 * {@code XlsFormatWriter} ／ {@code YamlFormatWriter} に {@code length} の番人は無い。
 * 上の段落「書き出し側には番人を置かない」と揃っている）。
 * </p>
 *
 * <p>
 * <b>可変長ファイルでは逆向きの検査が掛かる——{@code length} を持ってはならない</b>
 * （{@code coverage/issues.md} <b>XLS-45</b>。ユーザー確定・2026-08-24）。
 * 上に引いた {@code :883} の「フィールド長は不要である」と {@code :1076}「固定長との違いは、
 * 可変長ファイルの場合はフィールド長行を記載しない点のみである。」のとおり、
 * <b>Excel 記法に可変長のフィールド長行が無く、書き出す先が無い</b>ためである
 * （本体スキーマ {@code $defs.field_def.properties.length} は可変長で {@code length} を禁じていないが、
 * NTF 仕様として禁止で確定した。スキーマと解説書の対応は別途依頼済みである）。
 * これも文脈を持つ側が拒否する——{@link nablarch.test.tool.converter.model.FileDataBlock} が
 * 可変長系のデータ種別のとき {@code ModelPreconditions#requireNoLengths} を呼ぶ。
 * <b>弾くのは {@code null} 以外のすべてであり、{@code "-"} も空文字も弾く</b>（固定長側と境界が逆である）。
 * </p>
 *
 * @author kiyotis
 */
public final class FieldDef {

    private final String name;
    private final String type;
    private final String length;

    /**
     * コンストラクタ。
     *
     * @param name   フィールド名称（記述のまま。大文字化なし。必須（{@code null} 不可）。空文字は可）
     * @param type   データ型（記述のまま。必須（{@code null} 不可）。空文字は可）
     * @param length フィールド長（記述のまま。{@code "-"} 等もリテラル保持。可変長ファイルでは
     *               {@code null} でなければならず、固定長ファイル・電文では必須である。どちらの検査も
     *               {@code FileDataBlock} ／ {@code MessageDataBlock} が生成時に行う）
     * @throws IllegalArgumentException {@code name} または {@code type} が {@code null} の場合
     */
    public FieldDef(String name, String type, String length) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "フィールド名称を持たないフィールド定義は作れません"
                            + "（Excel 記法はフィールド名称・データ型のリストを必須としており、"
                            + "YAML 本体スキーマ $defs.field_def の required は name を含みます）。"
                            + " データ型=[" + type + "] フィールド長=[" + length + "]");
        }
        if (type == null) {
            throw new IllegalArgumentException(
                    "データ型を持たないフィールド定義は作れません"
                            + "（Excel 記法はフィールド名称・データ型のリストを必須としており、"
                            + "YAML 本体スキーマ $defs.field_def の required は type を含みます）。"
                            + " フィールド名=[" + name + "] フィールド長=[" + length + "]");
        }
        this.name = name;
        this.type = type;
        this.length = length;
    }

    /** @return フィールド名称（非 {@code null}） */
    public String getName() {
        return name;
    }

    /** @return データ型（非 {@code null}） */
    public String getType() {
        return type;
    }

    /** @return フィールド長（可変長ファイルでは常に {@code null}。固定長ファイル・電文では非 {@code null}） */
    public String getLength() {
        return length;
    }
}
