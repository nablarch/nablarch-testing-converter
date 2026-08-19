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
 * <b>{@code identifier} は {@code null} であってはならない。生成時点で拒否する。</b>
 * 識別子は記法上の必須要素である（{@code testdata_notation.rst:198}（{@code 30a8271} 時点）
 * {@code データタイプ=識別子の値}）。本体スキーマ
 * {@code nablarch/test/ntf-testdata-yaml-schema.json} も {@code $defs.table_data} の
 * {@code required} に {@code table} を含め、{@code {"type": "string"}} としている。
 * <b>空文字は拒否しない</b>（Excel は {@code id=[]} として往復し、YAML の {@code table: ""} は
 * スキーマに適合する）。{@code coverage/issues.md} <b>XLS-35</b>。
 * </p>
 *
 * <p>
 * <b>{@code dataType} は {@code null} であってはならない。生成時点で拒否する。</b>
 * データブロックは必ず 1 つのデータタイプを持つ。Excel 形式はマーカー
 * （{@code testdata_notation.rst:198}（{@code 30a8271} 時点）の {@code データタイプ=識別子の値}）、
 * YAML 形式はトップレベルキー（{@code :206-241}）がデータタイプから決まるため、
 * <b>データタイプの無いブロックはどちらの形式でも書けない</b>
 * （{@code coverage/issues.md} <b>XLS-34</b>）。
 * </p>
 *
 * <p>
 * <b>{@code dataType} に {@link DataType#DEFAULT} を持たせてはならない。生成時点で拒否する。</b>
 * {@code DEFAULT} は記法のデータタイプ表に載っている予約語だが（{@code testdata_notation.rst:188-190}
 * （{@code 30a8271} 時点）「フレームワーク内部用（通常は使用しない）」）、YAML のトップレベルキー
 * 対応表（{@code :206-241}「データタイプごとに専用のトップレベルキーを使う…対応は、以下のとおりである」）
 * に {@code DEFAULT} の行が無く、<b>YAML 形式では表現できない</b>。中間モデルの契約は 4 辺すべてが
 * 表現できる範囲で定めるため、{@code DEFAULT} は契約の外である
 * （{@code coverage/issues.md} <b>XLS-20</b>）。**根拠が「対応表に行が無い」という不在である点に注意。**
 * </p>
 *
 * <p>
 * <b>データ種別は、そのブロックのクラスの系統に属していなければならない。生成時点で拒否する。</b>
 * データタイプとブロックの形は 1:1 に対応するためである。YAML 形式は
 * <b>データタイプごとに専用のトップレベルキー</b>を使い（{@code testdata_notation.rst:206}
 * （{@code 30a8271} 時点）「データタイプごとに専用のトップレベルキーを使う…対応は、以下のとおりである」と
 * {@code :212-235} の対応表）、本体スキーマ {@code nablarch/test/ntf-testdata-yaml-schema.json} は
 * キーごとに別の {@code $defs} を割り当てている（{@code setup_tables} ／ {@code expected_tables} ／
 * {@code expected_complete_tables} → {@code $defs.table_data}（{@code required} ＝
 * {@code ["table","rows"]}）、{@code list_maps} → {@code $defs.list_map_data}（{@code ["id","rows"]}）、
 * {@code setup_files} ／ {@code expected_files} → {@code $defs.file_data}（{@code ["path","type","records"]}）、
 * {@code messages} → {@code $defs.message_data}、{@code expected_request_*} ／ {@code response_*} →
 * {@code $defs.expected_request_message_data} ／ {@code $defs.group_message_data}）。
 * <b>いずれも {@code additionalProperties} ＝ {@code false}</b> であり、系統の違うデータ種別を持つ
 * ブロックはスキーマ違反の YAML にしかならない。Excel 形式も同じで、系統の違う組は書けてしまうが
 * 読み戻すと別種のブロックになる（{@code coverage/issues.md} <b>XLS-36</b>）。
 * {@link ListMapBlock} は {@link DataType#LIST_MAP} をコンストラクタで固定するため検査の対象外である。
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

    /** @return グループ ID（非 {@code null}。省略時は空文字） */
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
     * 具体ブロックのコンストラクタが {@code super(...)} の直後に呼ぶ。{@code dataType} が {@code null}
     * ／ {@link DataType#DEFAULT} の場合は<b>ここへ届かない</b> —— どちらも
     * {@link #TestDataBlock(DataType, String, String)} が先に拒否するため、その専用のメッセージが
     * そのまま残る（{@code coverage/issues.md} <b>XLS-34</b>／<b>XLS-20</b>）。
     * </p>
     *
     * @param blockClass 具体ブロックのクラス
     * @param permitted  そのクラスが取りうるデータ種別
     * @param dataType   検査するデータ種別
     * @throws IllegalArgumentException {@code dataType} が {@code permitted} に含まれない場合
     */
    static void requireDataTypeOf(Class<? extends TestDataBlock> blockClass,
                                  Set<DataType> permitted, DataType dataType) {
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
