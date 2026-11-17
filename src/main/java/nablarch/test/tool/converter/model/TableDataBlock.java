package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * テーブルデータのデータブロック。
 *
 * <p>
 * データ種別は {@link DataType#SETUP_TABLE_DATA}／{@link DataType#EXPECTED_TABLE_DATA}／
 * {@link DataType#EXPECTED_COMPLETED} のいずれかで、それ以外は生成時に拒否する。
 * 識別子はテーブル名を保持する。省略カラムへのデフォルト補完は本体の責務であり、中間モデルでは行わない。
 * </p>
 *
 * @author kiyotis
 */
public final class TableDataBlock extends ColumnRowDataBlock {

    /**
     * 取りうるデータ種別。YAML のトップレベルキー {@code setup_tables} ／ {@code expected_tables} ／
     * {@code expected_complete_tables}（いずれも {@code $defs.table_data}）に対応する 3 種。
     */
    private static final Set<DataType> PERMITTED_TYPES = EnumSet.of(
            DataType.SETUP_TABLE_DATA, DataType.EXPECTED_TABLE_DATA, DataType.EXPECTED_COMPLETED);

    /**
     * コンストラクタ。
     *
     * @param dataType    データ種別（SETUP_TABLE_DATA／EXPECTED_TABLE_DATA／EXPECTED_COMPLETED）
     * @param groupId     グループ ID（省略時は空文字）
     * @param identifier  テーブル名
     * @param columnNames カラム名リスト（マーカーカラムを含む）
     * @param rows        データ行のリスト
     * @throws IllegalArgumentException {@code dataType} がテーブル系の 3 種でない場合
     */
    public TableDataBlock(DataType dataType, String groupId, String identifier,
                          List<String> columnNames, List<List<String>> rows) {
        super(dataType, groupId, identifier, columnNames, rows);
        requireDataTypeOf(TableDataBlock.class, PERMITTED_TYPES, dataType);
    }
}
