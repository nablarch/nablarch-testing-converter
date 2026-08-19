package nablarch.test.tool.converter.model;

import java.util.List;

/**
 * ファイル／メッセージのレコードレイアウト。
 *
 * <p>
 * レコード種別・フィールド定義群・データ行を保持する。レコード種別の省略は {@code null} のまま保持し、
 * デフォルト補完は行わない（本体読み込み側の責務）。フィールド定義・データ行は記述順・記法のまま保持する。
 * </p>
 *
 * <p>
 * <b>{@code fields} は 1 件以上でなければならない。</b>Excel 記法・YAML 記法のいずれもフィールドを
 * 持たないレコードレイアウトを認めていないためである（Excel は
 * {@code testdata_notation.rst:888} が「フィールド名称リストまたはデータ型リストが未指定または空である」を
 * 記述時のエラーに挙げる。YAML は本体スキーマ {@code nablarch/test/ntf-testdata-yaml-schema.json} の
 * {@code $defs.record_fragment} が {@code fields} を必須かつ {@code minItems} ＝ 1 とする）。
 * 中間モデルの契約は 4 辺すべてが表現できる範囲で定める。本クラス自身は検査しないが、書き出し側
 * （{@code XlsFormatWriter} ／ {@code YamlFormatWriter}）が空を受けたら送出で弾く
 * （{@code coverage/issues.md} <b>XLS-22</b> ／ <b>YML-12</b>）。
 * </p>
 *
 * <p>getter が返すコレクションは防御的コピーせず保持参照を返すため、呼び出し側は読み取り専用として扱うこと。</p>
 *
 * @author kiyotis
 */
public final class RecordLayout {

    private final String recordType;
    private final List<FieldDef> fields;
    private final List<List<String>> rows;

    /**
     * コンストラクタ。
     *
     * @param recordType レコード種別（省略時は {@code null}。FW_HEADER 等もそのまま保持）
     * @param fields     フィールド定義群（記述順。1 件以上。空の検査は書き出し側が行う）
     * @param rows       データ行のリスト（{@code null}・空文字・特殊記法を未加工で保持）
     * @throws IllegalArgumentException {@code fields} かその要素、{@code rows} かその要素（行）が
     *                                  {@code null} の場合（セルの {@code null} は通す）
     */
    public RecordLayout(String recordType, List<FieldDef> fields, List<List<String>> rows) {
        this.recordType = recordType;
        this.fields = ModelPreconditions.requireNoNulls("フィールド定義のリスト", fields);
        this.rows = ModelPreconditions.requireNoNullRows("データ行のリスト", rows);
    }

    /** @return レコード種別（省略時は {@code null}） */
    public String getRecordType() {
        return recordType;
    }

    /** @return フィールド定義群（記述順。1 件以上） */
    public List<FieldDef> getFields() {
        return fields;
    }

    /** @return データ行のリスト（未加工） */
    public List<List<String>> getRows() {
        return rows;
    }
}
