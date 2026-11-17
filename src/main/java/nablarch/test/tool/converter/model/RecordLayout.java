package nablarch.test.tool.converter.model;

import java.util.ArrayList;
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
 * フィールド 0 件と、フィールド名称の重複は生成時に拒否する。どちらも記法が記述時のエラーとしている形であり、
 * 書き出すと読み戻せないかスキーマ違反になるためである。
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
     * @param fields     フィールド定義群（記述順。1 件以上）
     * @param rows       データ行のリスト（{@code null}・空文字・特殊記法を未加工で保持）
     * @throws IllegalArgumentException {@code fields} ／ {@code rows} かその要素が {@code null} の場合、
     *                                  {@code fields} が空の場合、またはフィールド名称に重複がある場合
     */
    public RecordLayout(String recordType, List<FieldDef> fields, List<List<String>> rows) {
        this.recordType = recordType;
        this.fields = ModelPreconditions.requireNoNulls("フィールド定義のリスト", fields);
        ModelPreconditions.requireNotEmpty("フィールド定義", this.fields);
        List<String> fieldNames = new ArrayList<>(fields.size());
        for (FieldDef field : fields) {
            fieldNames.add(field.getName());
        }
        ModelPreconditions.requireNoDuplicates("フィールド名称", fieldNames);
        this.rows = ModelPreconditions.requireNoNullRows("データ行のリスト", rows);
        ModelPreconditions.requireRowsNotLongerThan("データ行", this.rows, fields.size());
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
