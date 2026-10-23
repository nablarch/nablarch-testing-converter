package nablarch.test.core.reader;

/**
 * 1 データブロックのヘッダ（マーカー行から取り出した属性）。
 * <p>
 * {@code SETUP_TABLE[g1]=USERS} のようなマーカー行を、データタイプ
 * （{@code SETUP_TABLE}）・グループ ID（{@code [g1]}、無指定は空文字）・
 * 識別子（{@code USERS}）へ分解して保持する。
 * </p>
 */
public final class BlockHeader {

    /** データタイプ */
    private final DataType type;

    /** グループ ID（{@code [g1]} 等。無指定は空文字） */
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
    BlockHeader(DataType type, String groupId, String identifier) {
        this.type = type;
        this.groupId = groupId;
        this.identifier = identifier;
    }

    /** @return データタイプ */
    public DataType getType() {
        return type;
    }

    /** @return グループ ID（{@code [g1]} 等。無指定は空文字） */
    public String getGroupId() {
        return groupId;
    }

    /** @return 識別子（テーブル名／ファイルパス／LIST_MAP ID／メッセージ ID） */
    public String getIdentifier() {
        return identifier;
    }
}
