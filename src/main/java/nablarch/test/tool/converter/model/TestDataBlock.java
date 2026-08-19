package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;

/**
 * NTF の 1 データブロックを表す中間モデルの抽象基底。
 *
 * <p>
 * データ種別（{@link DataType}）・グループ ID・識別子という全ブロック共通の属性を持つ。
 * 具体ブロックは、カラム／行形式（{@link ColumnRowDataBlock}）・ファイル（{@link FileDataBlock}）・
 * メッセージ（{@link MessageDataBlock}）の 3 系統に限定される（sealed）。
 * </p>
 *
 * <p>
 * <b>{@code groupId} は {@code null} であってはならない。省略は空文字で表す。生成時点で拒否する。</b>
 * Excel 記法・YAML 記法のいずれも {@code null} と空文字を区別できないためである
 * （Excel は {@code testdata_notation.rst:198}（{@code 30a8271} 時点）の {@code データタイプ=識別子の値} と
 * {@code :278} の {@code データタイプ名[グループID]=} が書ける形を定めるが、どちらも「何も書かない」という
 * 同じ 1 つの出力にしかならない。YAML 本体スキーマ
 * {@code nablarch/test/ntf-testdata-yaml-schema.json} は {@code group_id} を
 * {@code {"type": "string", "minLength": 1}} の任意キーとしており、空文字を禁じ、省略はキーを置かない形で表す）。
 * 省略が「グループ ID を持たないデータブロック（デフォルトグループ）」を指すことは {@code :254} が定める。
 * </p>
 *
 * <p>
 * <b>この拒否は入力の検証ではなく不変条件の保証である。</b>両リーダーは {@code null} を作らない
 * （辺①は {@code TestCoreReaderAdapter} がマーカー行でない行をブロックごと読み飛ばすため
 * {@code BlockHeader.groupId} が必ず非 null、辺②は {@code YamlFormatReader} が省略時に空文字を返す）。
 * したがって {@code null} が入るのは呼び出し側のバグであり、生成時点で露見させる。書き出し側
 * （{@code XlsFormatWriter} ／ {@code YamlFormatWriter}）には番人を置かない
 * （{@code coverage/issues.md} <b>XLS-32</b>。方針は {@code steering.md} Decisions
 * 「不正値は書き出し側でなく中間モデルの生成時に拒否する」）。
 * </p>
 *
 * @author kiyotis
 */
public abstract sealed class TestDataBlock
        permits ColumnRowDataBlock, FileDataBlock, MessageDataBlock {

    private final DataType dataType;
    private final String groupId;
    private final String identifier;

    /**
     * コンストラクタ。
     *
     * @param dataType   データ種別
     * @param groupId    グループ ID（省略時は空文字。{@code null} 不可）
     * @param identifier ブロックの識別子（テーブル名／ファイルパス／LIST_MAP ID／メッセージ ID）
     * @throws IllegalArgumentException {@code groupId} が {@code null} の場合
     */
    protected TestDataBlock(DataType dataType, String groupId, String identifier) {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "グループ ID が null のデータブロックは作れません"
                            + "（Excel 記法・YAML 記法とも null と空文字を区別できないため、"
                            + "省略は空文字で表します）。"
                            + " データ種別=[" + dataType + "] 識別子=[" + identifier + "]");
        }
        this.dataType = dataType;
        this.groupId = groupId;
        this.identifier = identifier;
    }

    /** @return データ種別 */
    public DataType getDataType() {
        return dataType;
    }

    /** @return グループ ID（非 {@code null}。省略時は空文字） */
    public String getGroupId() {
        return groupId;
    }

    /** @return ブロックの識別子（テーブル名／ファイルパス／LIST_MAP ID／メッセージ ID） */
    public String getIdentifier() {
        return identifier;
    }
}
