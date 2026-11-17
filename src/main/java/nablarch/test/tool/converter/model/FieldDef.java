package nablarch.test.tool.converter.model;

/**
 * ファイル／メッセージのフィールド定義。不変オブジェクト。
 *
 * <p>
 * 名称・型・長さを、形式の記法を加工せずそのまま保持する（名称は大文字化しない）。
 * 長さは {@code "-"} 等の記法もありうるため、数値化せず {@link String} で保持する。
 * </p>
 *
 * <p>
 * {@code name} と {@code type} は {@code null} 不可で、生成時に拒否する。どちらの記法も名称・データ型を
 * 持たないフィールド定義を認めないためである。空文字は通す（スキーマも {@code minLength} を持たない）。
 * </p>
 *
 * <p>
 * {@code length} は固定長ファイル・電文では必須、可変長ファイルでは持ってはならない。
 * 固定長か可変長かは本クラス単体では判らないため、検査は {@link FileDataBlock} ／
 * {@link MessageDataBlock} が生成時に行う。
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
     * @param length フィールド長（記述のまま。可変長ファイルでは {@code null}、固定長ファイル・電文では必須）
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
