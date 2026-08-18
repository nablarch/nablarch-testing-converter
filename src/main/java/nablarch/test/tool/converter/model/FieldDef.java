package nablarch.test.tool.converter.model;

/**
 * ファイル／メッセージのフィールド定義。不変オブジェクト。
 *
 * <p>
 * 名称・型・長さを、形式の記法を加工せずそのまま保持する。長さの省略は {@code null}。
 * 名称は大文字化しない。長さは {@code "-"}（自動拡張指示）等の記法もありうるためリテラルとして
 * {@link String} で保持する（数値化しない）。
 * </p>
 *
 * <p>
 * <b>{@code type} は必須であり {@code null} であってはならない。</b>Excel 記法・YAML 記法のいずれも
 * データ型を持たないフィールド定義を認めていないためである（Excel は
 * {@code testdata_notation.rst:883}（{@code 30a8271} 時点）が固定長ファイルで「フィールド名称・データ型・
 * フィールド長の3リストが同サイズで必須」・可変長ファイルで「フィールド名称・データ型の2リストが同サイズで
 * 必須」と定め、{@code :888} が「フィールド名称リストまたはデータ型リストが未指定または空である」を
 * 記述時のエラーに挙げる。YAML は本体スキーマ {@code nablarch/test/ntf-testdata-yaml-schema.json} の
 * {@code $defs.field_def} が {@code required} ＝ {@code ["name", "type"]} とする）。
 * 中間モデルの契約は 4 辺すべてが表現できる範囲で定める。本クラス自身は検査しないが、書き出し側
 * （{@code XlsFormatWriter} ／ {@code YamlFormatWriter}）が {@code null} を受けたら送出で弾く
 * （{@code coverage/issues.md} <b>YML-12</b>）。{@code length} は従来どおり省略可（{@code null} 可）である。
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
     * @param name   フィールド名称（記述のまま。大文字化なし）
     * @param type   データ型（記述のまま。必須（{@code null} 不可）。{@code null} の検査は書き出し側が行う）
     * @param length フィールド長（記述のまま。{@code "-"} 等もリテラル保持。省略時は {@code null}）
     */
    public FieldDef(String name, String type, String length) {
        this.name = name;
        this.type = type;
        this.length = length;
    }

    /** @return フィールド名称 */
    public String getName() {
        return name;
    }

    /** @return データ型（必須。{@code null} 不可） */
    public String getType() {
        return type;
    }

    /** @return フィールド長（省略時は {@code null}） */
    public String getLength() {
        return length;
    }
}
