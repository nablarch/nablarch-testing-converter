package nablarch.test.tool.converter.model;

/**
 * ファイル／メッセージのフィールド定義。不変オブジェクト。
 *
 * <p>
 * 名称・型・長さを、形式の記法を加工せずそのまま保持する。長さの省略（可変長ファイル）は {@code null}。
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
 * （{@code coverage/issues.md} <b>YML-12</b>）。
 * </p>
 *
 * <p>
 * <b>{@code length} は条件つきで必須である——固定長ファイル・電文のフィールド定義では {@code null} で
 * あってはならず、可変長ファイルのフィールド定義では省略可（{@code null} 可）である。</b>
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
 * 中間モデルの契約は 4 辺すべてが表現できる範囲で定める。本クラス自身は検査しないが、書き出し側
 * （{@code XlsFormatWriter} ／ {@code YamlFormatWriter}）が固定長ファイル・電文で {@code null} を
 * 受けたら送出で弾く（{@code coverage/issues.md} <b>XLS-30</b>）。
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
     * @param length フィールド長（記述のまま。{@code "-"} 等もリテラル保持。可変長ファイルでは省略可で
     *               {@code null}。固定長ファイル・電文では必須であり、{@code null} の検査は書き出し側が行う）
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

    /** @return フィールド長（可変長ファイルでは省略可で {@code null}。固定長ファイル・電文では必須） */
    public String getLength() {
        return length;
    }
}
