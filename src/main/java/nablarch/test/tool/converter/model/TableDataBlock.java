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
 * {@link DataType#EXPECTED_COMPLETED} のいずれか。識別子はテーブル名を保持する。
 * 省略カラムへのデフォルト補完（{@code EXPECTED_COMPLETE_TABLE}）は本体読み込み側の責務であり、
 * 中間モデルでは補完しない。
 * </p>
 *
 * <p>
 * <b>この 3 種以外のデータ種別では生成できない。生成時点で拒否する</b>
 * （{@code coverage/issues.md} <b>XLS-36</b>。根拠は
 * {@link TestDataBlock#requireDataTypeOf(Class, Set, DataType)} の Javadoc）。
 * </p>
 *
 * @author kiyotis
 */
public final class TableDataBlock extends ColumnRowDataBlock {

    /**
     * 取りうるデータ種別。YAML のトップレベルキー {@code setup_tables} ／ {@code expected_tables} ／
     * {@code expected_complete_tables}（いずれも {@code $defs.table_data}）に対応する 3 種
     * 。
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
     * @throws IllegalArgumentException {@code dataType} が SETUP_TABLE_DATA ／ EXPECTED_TABLE_DATA ／
     *                                  EXPECTED_COMPLETED のいずれでもない場合
     */
    public TableDataBlock(DataType dataType, String groupId, String identifier,
                          List<String> columnNames, List<List<String>> rows) {
        super(dataType, groupId, identifier, columnNames, rows);
        requireDataTypeOf(TableDataBlock.class, PERMITTED_TYPES, dataType);
    }
}
