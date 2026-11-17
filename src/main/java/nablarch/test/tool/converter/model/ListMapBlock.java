package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;

import java.util.List;

/**
 * LIST_MAP（{@code List<Map<String, String>>} 形式・SqlResultSet 比較等に用いる）のデータブロック。
 *
 * <p>
 * データ種別は常に {@link DataType#LIST_MAP}。識別子は LIST_MAP の ID を保持する。
 * 空マッピング由来の空行も空リストとして保持し、行の有無を後段が判別できるようにする。
 * </p>
 *
 * <p>
 * <b>グループ ID は持たない（空文字だけを許す）。</b>テスティングフレームワークは {@code LIST_MAP} を
 * ID だけで引き当て、Excel 形式の識別行にグループ ID が書かれていても使わない。YAML 形式のスキーマも
 * {@code list_maps} のエントリに {@code group_id} を定義していない。
 * </p>
 *
 * @author kiyotis
 */
public final class ListMapBlock extends ColumnRowDataBlock {

    /**
     * コンストラクタ。データ種別は {@link DataType#LIST_MAP} に固定される。
     *
     * @param groupId     グループ ID（空文字だけを許す）
     * @param identifier  LIST_MAP の ID
     * @param columnNames カラム名リスト（記述順）
     * @param rows        データ行のリスト（空行も保持）
     * @throws IllegalArgumentException グループ ID が空文字でない場合
     */
    public ListMapBlock(String groupId, String identifier,
                        List<String> columnNames, List<List<String>> rows) {
        super(DataType.LIST_MAP, groupId, identifier, columnNames, rows);
        if (!groupId.isEmpty()) {
            throw new IllegalArgumentException("LIST_MAP のデータブロックはグループ ID を持てません"
                    + "（テスティングフレームワークは LIST_MAP を ID だけで引き当てる）。"
                    + " groupId=[" + groupId + "] identifier=[" + identifier + "]");
        }
    }
}
