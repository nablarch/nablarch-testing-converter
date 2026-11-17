package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * メッセージ（電文）のデータブロック。
 *
 * <p>
 * データ種別は {@link DataType#MESSAGE}／{@link DataType#EXPECTED_REQUEST_HEADER_MESSAGES}／
 * {@link DataType#EXPECTED_REQUEST_BODY_MESSAGES}／{@link DataType#RESPONSE_HEADER_MESSAGES}／
 * {@link DataType#RESPONSE_BODY_MESSAGES} のいずれかで、それ以外は生成時に拒否する。
 * 識別子はメッセージ ID を保持する。ディレクティブ・FW 制御ヘッダフィールド・本文レコードレイアウト群を記述順で持つ。
 * FW 制御ヘッダを持つのは {@link DataType#MESSAGE} のみで、他は空 Map とする（仕様 MS-04）。
 * </p>
 *
 * <p>
 * 本文レコードは 1 件以上でなければならず、生成時に拒否する。どちらの記法にも 0 件の電文を表す書き方が無く
 * （スキーマも電文系は {@code records.minItems} ＝ 1）、電文が無い場合はデータブロックごと省略するためである。
 * 0 バイトの空ファイル特例（{@code records: []}）はファイルに限られ、電文には及ばない。
 * </p>
 *
 * <p>getter が返すコレクションは防御的コピーせず保持参照を返すため、呼び出し側は読み取り専用として扱うこと。</p>
 *
 * @author kiyotis
 */
public final class MessageDataBlock extends TestDataBlock {

    /**
     * 取りうるデータ種別。YAML のトップレベルキー {@code messages} ／
     * {@code expected_request_header_messages} ／ {@code expected_request_body_messages} ／
     * {@code response_header_messages} ／ {@code response_body_messages} に対応する 5 種。
     */
    private static final Set<DataType> PERMITTED_TYPES = EnumSet.of(
            DataType.MESSAGE,
            DataType.EXPECTED_REQUEST_HEADER_MESSAGES, DataType.EXPECTED_REQUEST_BODY_MESSAGES,
            DataType.RESPONSE_HEADER_MESSAGES, DataType.RESPONSE_BODY_MESSAGES);

    private final Map<String, String> directives;
    private final Map<String, String> fwHeaderFields;
    private final List<RecordLayout> records;

    /**
     * コンストラクタ。
     *
     * @param dataType       データ種別
     * @param groupId        グループ ID（省略時は空文字）
     * @param identifier     メッセージ ID
     * @param directives     ディレクティブ（記述順を保つため挿入順を維持する Map を渡すこと）
     * @param fwHeaderFields FW 制御ヘッダフィールド（記述順。FW ヘッダを読まない経路では空 Map）
     * @param records        本文レコードレイアウト群（記述順。1 件以上。0 件不可）
     * @throws IllegalArgumentException {@code dataType} が電文系の 5 種でない場合、または {@code records} が 0 件の場合
     */
    public MessageDataBlock(DataType dataType, String groupId, String identifier,
                            Map<String, String> directives,
                            Map<String, String> fwHeaderFields, List<RecordLayout> records) {
        super(dataType, groupId, identifier);
        requireDataTypeOf(MessageDataBlock.class, PERMITTED_TYPES, dataType);
        this.directives = ModelPreconditions.requireNoNulls("ディレクティブ", directives);
        this.fwHeaderFields = ModelPreconditions.requireNoNulls("FW 制御ヘッダフィールド", fwHeaderFields);
        this.records = ModelPreconditions.requireNoNulls("レコードレイアウトのリスト", records);
        if (this.records.isEmpty()) {
            throw new IllegalArgumentException(
                    "本文レコードを 1 件も持たない電文ブロックは作れません"
                            + "（レコード 0 件の電文を表す書き方が記法に無く、電文が存在しない場合は"
                            + "データブロックごと省略します。YAML スキーマの電文系 3 定義は"
                            + " records.minItems = 1 です。0 バイトの空ファイル特例はファイルに限られ、"
                            + "電文には及びません）。"
                            + " 識別子=[" + identifier + "]");
        }
        ModelPreconditions.requireLengths(this.records, identifier);
    }

    /** @return ディレクティブ（記述順） */
    public Map<String, String> getDirectives() {
        return directives;
    }

    /** @return FW 制御ヘッダフィールド（記述順。FW ヘッダを読まない経路では空 Map） */
    public Map<String, String> getFwHeaderFields() {
        return fwHeaderFields;
    }

    /** @return 本文レコードレイアウト群（記述順。1 件以上） */
    public List<RecordLayout> getRecords() {
        return records;
    }
}
