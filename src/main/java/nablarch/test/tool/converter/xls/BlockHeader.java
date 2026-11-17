package nablarch.test.tool.converter.xls;

import nablarch.test.core.reader.DataType;





/**
 * 1 データブロックのヘッダ（マーカー行から取り出したデータタイプ・グループ ID・識別子）。
 */
public final class BlockHeader {

    /** データタイプ */
    private final DataType type;

    /** グループ ID（{@code [ ]} を付けない値。無指定は空文字） */
    private final String groupId;

    /** 識別子（テーブル名／ファイルパス／LIST_MAP ID／メッセージ ID） */
    private final String identifier;

    /**
     * コンストラクタ。
     *
     * @param type       データタイプ
     * @param groupId    グループ ID（無指定は空文字）
     * @param identifier 識別子
     */
    public BlockHeader(DataType type, String groupId, String identifier) {
        this.type = type;
        this.groupId = groupId;
        this.identifier = identifier;
    }

    /** @return データタイプ */
    public DataType getType() {
        return type;
    }

    /** @return グループ ID（{@code [ ]} を付けない値。無指定は空文字） */
    public String getGroupId() {
        return groupId;
    }

    /** @return 識別子（テーブル名／ファイルパス／LIST_MAP ID／メッセージ ID） */
    public String getIdentifier() {
        return identifier;
    }
}
