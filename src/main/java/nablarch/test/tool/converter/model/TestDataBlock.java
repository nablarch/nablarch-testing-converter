package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;

import java.util.Set;

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
 * <b>{@code groupId} は {@code [ ]} を付けない値である</b>（{@link nablarch.test.tool.converter.reader.GroupIdNotation}）。
 * </p>
 *
 * <p>
 * 生成時に次の不変条件を検査する。いずれも、満たさないブロックは Excel・YAML のどちらかで書けないか、
 * 読み戻すと別のブロックになるためである。{@code null} が入るのは呼び出し側のバグであり、生成時に露見させる。
 * </p>
 * <ul>
 *   <li>{@code dataType} は {@code null} 不可。データタイプの無いブロックはどちらの形式でも書けない。</li>
 *   <li>{@code dataType} に {@link DataType#DEFAULT} は不可。YAML のトップレベルキーに対応が無く表現できない。</li>
 *   <li>データ種別はブロックのクラスの系統に属すること。データタイプとブロックの形は 1:1 に対応し、
 *       系統の違う組は YAML ではスキーマ違反、Excel では読み戻すと別種のブロックになる。</li>
 *   <li>{@code groupId} は {@code null} 不可。どちらの記法も {@code null} と空文字を区別できないため、省略は空文字で表す。</li>
 *   <li>{@code identifier} は {@code null} 不可（記法上の必須要素）。空文字は往復できるため通す。</li>
 * </ul>
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
     * @param dataType   データ種別（{@code null} 不可。{@link DataType#DEFAULT} 不可）
     * @param groupId    グループ ID（省略時は空文字。{@code null} 不可）
     * @param identifier ブロックの識別子（テーブル名／ファイルパス／LIST_MAP ID／メッセージ ID。
     *                   {@code null} 不可）
     * @throws IllegalArgumentException {@code dataType} が {@code null} または
     *                                  {@link DataType#DEFAULT} の場合、
     *                                  または {@code groupId} ／ {@code identifier} が
     *                                  {@code null} の場合
     */
    protected TestDataBlock(DataType dataType, String groupId, String identifier) {
        if (dataType == null) {
            throw new IllegalArgumentException(
                    "データ種別が null のデータブロックは作れません"
                            + "（データブロックは必ず 1 つのデータタイプを持ちます。Excel 形式は"
                            + "マーカー、YAML 形式はトップレベルキーがデータタイプから決まるため、"
                            + "データタイプの無いブロックはどちらの形式でも書けません）。"
                            + " グループ ID=[" + groupId + "] 識別子=[" + identifier + "]");
        }
        if (dataType == DataType.DEFAULT) {
            throw new IllegalArgumentException(
                    "データタイプ DEFAULT のデータブロックは作れません"
                            + "（DEFAULT はフレームワーク内部用であり、YAML のトップレベルキー対応表に"
                            + "対応する行が無いため YAML 形式では表現できません）。"
                            + " グループ ID=[" + groupId + "] 識別子=[" + identifier + "]");
        }
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "グループ ID が null のデータブロックは作れません"
                            + "（Excel 記法・YAML 記法とも null と空文字を区別できないため、"
                            + "省略は空文字で表します）。"
                            + " データ種別=[" + dataType + "] 識別子=[" + identifier + "]");
        }
        if (identifier == null) {
            throw new IllegalArgumentException(
                    "識別子が null のデータブロックは作れません"
                            + "（識別子は記法上の必須要素です。Excel 形式は「データタイプ=識別子の値」、"
                            + "YAML 形式は table ／ path ／ id といった必須キーで表します）。"
                            + " データ種別=[" + dataType + "] グループ ID=[" + groupId + "]");
        }
        this.dataType = dataType;
        this.groupId = groupId;
        this.identifier = identifier;
    }

    /** @return データ種別 */
    public DataType getDataType() {
        return dataType;
    }

    /** @return グループ ID（{@code [ ]} を付けない値。非 {@code null}。省略時は空文字） */
    public String getGroupId() {
        return groupId;
    }

    /** @return ブロックの識別子（テーブル名／ファイルパス／LIST_MAP ID／メッセージ ID） */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * データ種別が、そのブロックのクラスの系統に属することを確かめる。
     *
     * <p>
     * コンストラクタ以外からも直接呼ばれるため、{@code null} もここで拒否する
     * （{@link NullPointerException} にはしない）。
     * </p>
     *
     * @param blockClass 具体ブロックのクラス
     * @param permitted  そのクラスが取りうるデータ種別
     * @param dataType   検査するデータ種別（{@code null} 不可）
     * @throws IllegalArgumentException {@code dataType} が {@code null} または {@code permitted} に含まれない場合
     */
    static void requireDataTypeOf(Class<? extends TestDataBlock> blockClass,
                                  Set<DataType> permitted, DataType dataType) {
        if (dataType == null) {
            throw new IllegalArgumentException(
                    "データ種別が null のデータブロックを "
                            + blockClass.getSimpleName() + " として作ることはできません"
                            + "（データブロックは必ず 1 つのデータタイプを持ちます。Excel 形式は"
                            + "マーカー、YAML 形式はトップレベルキーがデータタイプから決まるため、"
                            + "データタイプの無いブロックはどちらの形式でも書けません）。");
        }
        if (!permitted.contains(dataType)) {
            throw new IllegalArgumentException(
                    "データ種別 " + dataType.getName() + " のデータブロックを "
                            + blockClass.getSimpleName() + " として作ることはできません"
                            + "（データタイプとブロックの形は 1:1 に対応します。YAML 形式は"
                            + "データタイプごとに専用のトップレベルキーを使い、キーごとにブロックの形が"
                            + "スキーマで決まっているため、系統の違う組はスキーマ違反の YAML にしか"
                            + "なりません。Excel 形式では書けてしまいますが、読み戻すと別種の"
                            + "ブロックになります）。"
                            + " " + blockClass.getSimpleName() + " が取りうるデータ種別=" + permitted);
        }
    }
}
