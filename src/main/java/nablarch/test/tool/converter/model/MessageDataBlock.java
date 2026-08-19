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
 * {@link DataType#RESPONSE_BODY_MESSAGES} のいずれか。識別子はメッセージ ID を保持する。
 * ディレクティブ・FW 制御ヘッダフィールド・本文レコードレイアウト群を記述順で持つ。
 * <b>この 5 種以外のデータ種別では生成できない。生成時点で拒否する</b>
 * （{@code coverage/issues.md} <b>XLS-36</b>。根拠は
 * {@link TestDataBlock#requireDataTypeOf(Class, Set, DataType)} の Javadoc）。
 * </p>
 *
 * <p>
 * FW 制御ヘッダ（{@code requestId}／{@code userId} 等）を読むのは {@code messages} 経路（{@link DataType#MESSAGE}）
 * のみで、{@code expected_request_*}／{@code response_*} 経路は空 Map とする（仕様 MS-04）。
 * </p>
 *
 * <p>
 * <b>{@code records}（本文レコード）は 1 件以上でなければならない（0 件不可）。生成時点で拒否する。</b>
 * Excel 記法・YAML 記法のいずれも本文レコード 0 件の電文を認めていないためである。
 * YAML は本体スキーマ {@code nablarch/test/ntf-testdata-yaml-schema.json} の
 * {@code $defs.message_data} ／ {@code $defs.expected_request_message_data} ／
 * {@code $defs.group_message_data} がいずれも {@code required} ＝ {@code ["id", "records"]} かつ
 * {@code records.minItems} ＝ 1 であり、{@code records:} を省いても {@code records: []} と書いても通らない。
 * Excel も同じで、記法に電文のレコード 0 件を表す書き方の明文が無い。電文が存在しない場合は
 * {@code testdata_notation.rst:1257}（{@code 30a8271} 時点）
 * 「応答不要メッセージ受信では…{@code expectedMessages} のデータブロックを記述する必要はない」のとおり、
 * <b>データブロックごと省略する</b>のが記法である。
 * </p>
 *
 * <p>
 * <b>0 バイトの空ファイル特例（{@code records: []}）は電文には及ばない。</b>同特例は
 * {@code testdata_notation.rst:881}（「0バイトの空ファイルは、レコード定義を持たないファイルデータブロックとして
 * 表現する」）／{@code :1109}／{@code :1146}（「…0バイトの空ファイルを表現するには、{@code records:} に
 * 空配列 {@code []} を記載する」）と、いずれも<b>ファイル</b>に限って書かれている
 * （{@code 30a8271} 時点）。{@code :1158}「フレームワーク制御ヘッダ以降のメッセージボディは、
 * フィールド名称・データ型・フィールド長・データという、前述のファイルデータと同じ構成を持つ」は
 * <b>カラム構成が同じ</b>と言っているだけで、空ファイル特例まで及ぶ読み方はしない。
 * スキーマ側も {@code $defs.file_data} だけが {@code records.minItems} ＝ 0 であり、電文系 3 定義と
 * 非対称である。{@link FileDataBlock#getRecords()} の 0 件は合法だが、本クラスの 0 件は違う。
 * </p>
 *
 * <p>
 * 中間モデルの契約は 4 辺すべてが表現できる範囲で定める（{@code coverage/issues.md} <b>YML-12</b> の
 * 2 形目）。<b>番人はもとは辺③④の書き出し側（{@code XlsFormatWriter#layoutMessage} ／
 * {@code YamlFormatWriter#emitMessage}）にあったが、2026-08-19 に生成時へ移した</b>
 * （{@code steering.md} Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」）。
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
     * {@code response_header_messages} ／ {@code response_body_messages} に対応する 5 種
     * （{@code testdata_notation.rst:226-235}（{@code 30a8271} 時点））。
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
     * @throws IllegalArgumentException {@code dataType} が MESSAGE ／
     *                                  EXPECTED_REQUEST_HEADER_MESSAGES ／
     *                                  EXPECTED_REQUEST_BODY_MESSAGES ／
     *                                  RESPONSE_HEADER_MESSAGES ／ RESPONSE_BODY_MESSAGES の
     *                                  いずれでもない場合、または {@code records} が 0 件の場合
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
