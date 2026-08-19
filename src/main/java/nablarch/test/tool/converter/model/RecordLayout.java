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
 * <b>フィールドを 1 件も持たないレコードレイアウトは作れない。生成時点で拒否する。</b>
 * Excel 記法・YAML 記法のいずれもフィールドを持たないレコードレイアウトを認めていないためである
 * （Excel は {@code testdata_notation.rst:888}（{@code 30a8271} 時点）が「フィールド名称リストまたは
 * データ型リストが未指定または空である」を記述時のエラーに挙げる。YAML は本体スキーマ
 * {@code nablarch/test/ntf-testdata-yaml-schema.json} の {@code $defs.record_fragment} が
 * {@code fields} を必須かつ {@code minItems} ＝ 1 とする）。
 * <b>番人はもとは辺③④の書き出し側にあった</b>（{@code coverage/issues.md} <b>XLS-22</b> ／
 * <b>YML-12</b> の 3 形目）が、不正値は書き出し側でなく生成時に拒否する方針に従って本クラスへ移した
 * （{@code steering.md} Decisions・2026-08-19）。
 * </p>
 *
 * <p>
 * <b>フィールド名称が重複したレコードレイアウトは作れない。生成時点で拒否する</b>
 * （{@code testdata_notation.rst:887}（{@code 30a8271} 時点）「同一レコード種別内でフィールド名称が
 * 重複している」は記述時のエラーである。{@code coverage/issues.md} <b>XLS-40</b>）。
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
     * @throws IllegalArgumentException {@code fields} かその要素、{@code rows} かその要素（行）が
     *                                  {@code null} の場合（セルの {@code null} は通す）、
     *                                  {@code fields} が空の場合、
     *                                  または {@code fields} のフィールド名称に重複がある場合
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
