package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;

import java.util.List;

/**
 * カラム名行とデータ行で表現されるデータブロックの抽象基底。
 *
 * <p>
 * テーブルデータ（{@link TableDataBlock}）と LIST_MAP（{@link ListMapBlock}）の共通基底（sealed）。
 * カラム名リスト（マーカーカラムを含む）とデータ行リストを、加工せず記述順・記法のまま保持する。
 * </p>
 *
 * <p>
 * <b>{@code columnNames} ／ {@code rows} と、その要素（行）は {@code null} を取らない。生成時点で拒否する</b>
 * （{@code coverage/issues.md} <b>XLS-38</b>。根拠は {@link ModelPreconditions} の Javadoc）。
 * <b>行の中のセルの {@code null} は記法にあるため通す。</b>
 * </p>
 *
 * <p>getter が返すコレクションは防御的コピーせず保持参照を返すため、呼び出し側は読み取り専用として扱うこと。</p>
 *
 * @author kiyotis
 */
public abstract sealed class ColumnRowDataBlock extends TestDataBlock
        permits TableDataBlock, ListMapBlock {

    private final List<String> columnNames;
    private final List<List<String>> rows;

    /**
     * コンストラクタ。
     *
     * @param dataType    データ種別
     * @param groupId     グループ ID（省略時は空文字）
     * @param identifier  識別子（テーブル名／LIST_MAP ID）
     * @param columnNames カラム名リスト（マーカーカラムを含む。記述順・大文字化なし）
     * @param rows        データ行のリスト（{@code null} セルと空文字 {@code ""} を区別し、特殊記法を記法のまま保持）
     * @throws IllegalArgumentException {@code columnNames} かその要素、{@code rows} かその要素（行）が
     *                                  {@code null} の場合（セルの {@code null} は通す）
     */
    protected ColumnRowDataBlock(DataType dataType, String groupId, String identifier,
                                 List<String> columnNames, List<List<String>> rows) {
        super(dataType, groupId, identifier);
        this.columnNames = ModelPreconditions.requireNoNulls("カラム名リスト", columnNames);
        this.rows = ModelPreconditions.requireNoNullRows("データ行のリスト", rows);
    }

    /** @return カラム名リスト（マーカーカラムを含む。記述順・大文字化なし） */
    public List<String> getColumnNames() {
        return columnNames;
    }

    /** @return データ行のリスト（{@code null}・空文字・特殊記法を未加工で保持） */
    public List<List<String>> getRows() {
        return rows;
    }
}
