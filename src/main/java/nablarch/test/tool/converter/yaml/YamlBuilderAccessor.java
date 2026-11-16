package nablarch.test.tool.converter.yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nablarch.test.core.db.BasicDefaultValues;
import nablarch.test.core.db.DefaultValues;
import nablarch.test.core.db.TableData;
import nablarch.test.core.file.DataFile;
import nablarch.test.core.file.FixedLengthFile;
import nablarch.test.core.messaging.MessagePool;
import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.yaml.InterpreterResolver;
import nablarch.test.core.reader.yaml.MessageContent;
import nablarch.test.core.reader.yaml.YamlFileBuilder;
import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.core.reader.yaml.YamlMessageBuilder;
import nablarch.test.core.reader.yaml.YamlSection;
import nablarch.test.core.reader.yaml.YamlTableDataBuilder;
import nablarch.test.tool.converter.reader.GroupIdNotation;
import nablarch.test.tool.converter.reader.StubDbInfo;

/**
 * 本体の YAML 読み込みを再利用して、生の器（{@link TableData}／{@link DataFile}／{@link MessagePool} 本文）を
 * 取り出すアクセサ。
 *
 * <p>
 * {@code ${...}} などの記法を加工せずに運ぶため、本体のビルダを値加工なし・デフォルト値補完なしで使う。
 * </p>
 *
 * @author kiyobot
 */
public class YamlBuilderAccessor {

    /**
     * デフォルトグループ（{@code group_id} 省略）を本体ビルダへ渡すための合成グループ ID。
     * 実在のグループ ID を含まない複製 Map の中だけで使うため、値は何でもよい。
     */
    private static final String DEFAULT_GROUP_MARKER = "default-group";

    /** テーブル系ビルダ（空インタープリタ・補完なしで配線） */
    private final YamlTableDataBuilder tableBuilder;

    /** ファイル系ビルダ（空インタープリタで配線） */
    private final YamlFileBuilder fileBuilder;

    /** メッセージ系ビルダ（空インタープリタで配線） */
    private final YamlMessageBuilder messageBuilder;

    /**
     * コンストラクタ。ビルダを値加工なし（{@link InterpreterResolver#raw()}）で構成する。
     * DB に接続しないよう {@link StubDbInfo} を用いる。
     */
    public YamlBuilderAccessor() {
        InterpreterResolver raw = InterpreterResolver.raw();
        DefaultValues defaultValues = new BasicDefaultValues();
        this.tableBuilder = new YamlTableDataBuilder(new StubDbInfo(), defaultValues, raw);
        this.fileBuilder = new YamlFileBuilder(raw);
        this.messageBuilder = new YamlMessageBuilder(raw);
    }

    /**
     * YAML ファイルのトップレベル Map（原文。記述順を保持）を返す。
     *
     * @param path     取得元パス
     * @param resource リソース名（拡張子なし）
     * @return YAML トップレベル Map（空ファイルの場合は空 Map）
     */
    public Map<String, Object> loadRawMap(String path, String resource) {
        return YamlLoader.load(path, resource);
    }

    /**
     * テストデータの入れ物（{@code <path>/<入れ物名>} ディレクトリ）が存在するかどうかを返す。
     * 読み込み単位の YAML ファイルの存在は見ない。
     *
     * @param path     取得元パス
     * @param resource リソース名
     * @return 入れ物ディレクトリが存在する場合 true
     */
    public boolean isResourceExisting(String path, String resource) {
        return YamlLoader.isResourceExisting(path, resource);
    }

    /**
     * テーブルデータを、デフォルト値を補完せずに取り出す。
     *
     * @param path     取得元パス
     * @param resource リソース名
     * @param groupId  グループ ID（{@code [ ]} を付けない値。{@link GroupIdNotation}）
     * @param type     データタイプ（{@link DataType#SETUP_TABLE_DATA}／
     *                 {@link DataType#EXPECTED_TABLE_DATA}／{@link DataType#EXPECTED_COMPLETED}）
     * @return テーブルデータ一覧
     * @throws IllegalArgumentException データタイプがテーブル系でない場合
     */
    public List<TableData> readTables(String path, String resource, String groupId, DataType type) {
        Map<String, Object> yaml = loadRawMap(path, resource);
        return tableBuilder.buildTableDataList(
                yaml, tableSectionKey(type), GroupIdNotation.format(groupId), false, path);
    }

    /**
     * {@code List<Map<String, String>>}形式（{@code list_maps}）のデータを取り出す。
     *
     * @param path     取得元パス
     * @param resource リソース名
     * @param id       list_maps エントリの id
     * @return 行データ一覧（見つからない場合は空）
     */
    public List<Map<String, String>> readListMap(String path, String resource, String id) {
        Map<String, Object> yaml = loadRawMap(path, resource);
        return tableBuilder.buildListMapRows(yaml, id, path);
    }

    /**
     * ファイル（固定長／可変長）を取り出す。
     *
     * @param path     取得元パス
     * @param resource リソース名
     * @param groupId  グループ ID（{@code [ ]} を付けない値。{@link GroupIdNotation}）
     * @param type     データタイプ（{@link DataType#SETUP_FIXED}／{@link DataType#EXPECTED_FIXED}／
     *                 {@link DataType#SETUP_VARIABLE}／{@link DataType#EXPECTED_VARIABLE}）
     * @return ファイル一覧
     * @throws IllegalArgumentException データタイプがファイル系でない場合
     */
    public List<DataFile> readFiles(String path, String resource, String groupId, DataType type) {
        Map<String, Object> yaml = loadRawMap(path, resource);
        return fileBuilder.buildDataFileList(
                yaml, fileSectionKey(type), GroupIdNotation.format(groupId), path);
    }

    /**
     * メッセージ（{@link DataType#MESSAGE}）を取り出す。
     *
     * @param path     取得元パス
     * @param resource リソース名
     * @param id       メッセージ ID
     * @return 本文と FW 制御ヘッダ。対象が存在しない場合は {@code null}
     */
    public MessageContent readMessage(String path, String resource, String id) {
        Map<String, Object> yaml = loadRawMap(path, resource);
        return messageBuilder.buildMessageContent(yaml, YamlSection.KEY_MESSAGES, id, true, path);
    }

    /**
     * 送信同期メッセージ（要求/応答電文 4 種）のうち、指定グループに属する本文を取り出す。
     *
     * @param path     取得元パス
     * @param resource リソース名
     * @param groupId  グループ ID（{@code [ ]} を付けない値）。{@code null} はデフォルトグループ
     * @param type     データタイプ（送信系 4 種のいずれか）
     * @return 本文（固定長ファイルの器）一覧（記述順。対象が無ければ空）
     * @throws IllegalArgumentException データタイプが送信系でない場合
     */
    public List<FixedLengthFile> readSendSyncMessages(String path, String resource, String groupId, DataType type) {
        Map<String, Object> yaml = loadRawMap(path, resource);
        String sectionKey = sendSyncSectionKey(type);
        if (groupId == null) {
            return messageBuilder.buildSendSyncBodies(
                    defaultGroupOnlyYaml(yaml, sectionKey), sectionKey, DEFAULT_GROUP_MARKER, path);
        }
        return messageBuilder.buildSendSyncBodies(yaml, sectionKey, groupId, path);
    }

    /**
     * {@code group_id} を省略したエントリだけを残し、合成グループ ID（{@link #DEFAULT_GROUP_MARKER}）を
     * 付けた複製 Map を組み立てる。
     * <p>
     * 本体は {@code group_id} を持つエントリしか返さないため、省略したエントリを記法どおり
     * デフォルトグループとして読むにはこの前処理が要る。
     * </p>
     *
     * @param yaml       トップレベル Map
     * @param sectionKey 対象セクションキー
     * @return 当該セクションだけを持つ複製 Map
     */
    private static Map<String, Object> defaultGroupOnlyYaml(Map<String, Object> yaml, String sectionKey) {
        List<Object> entries = new ArrayList<Object>();
        Object sectionObj = yaml.get(sectionKey);
        if (sectionObj instanceof List) {
            for (Object entryObj : (List<?>) sectionObj) {
                // 到達しない（スキーマ検証済み）。直後の無検査キャストを守るため残す。
                if (!(entryObj instanceof Map)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> entry = (Map<String, Object>) entryObj;
                if (entry.get(YamlSection.FIELD_GROUP_ID) == null) {
                    Map<String, Object> copy = new LinkedHashMap<String, Object>(entry);
                    copy.put(YamlSection.FIELD_GROUP_ID, DEFAULT_GROUP_MARKER);
                    entries.add(copy);
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put(sectionKey, entries);
        return result;
    }

    /**
     * テーブル系データタイプを YAML セクションキーへ変換する。
     *
     * @param type データタイプ
     * @return セクションキー
     * @throws IllegalArgumentException テーブル系でない場合
     */
    private static String tableSectionKey(DataType type) {
        switch (type) {
            case SETUP_TABLE_DATA:  return YamlSection.KEY_SETUP_TABLES;
            case EXPECTED_TABLE_DATA: return YamlSection.KEY_EXPECTED_TABLES;
            case EXPECTED_COMPLETED:  return YamlSection.KEY_EXPECTED_COMPLETE_TABLES;
            default:
                throw new IllegalArgumentException(
                        "unsupported data type for readTables. type=[" + type + "]");
        }
    }

    /**
     * ファイル系データタイプを YAML セクションキーへ変換する。
     *
     * @param type データタイプ
     * @return セクションキー
     * @throws IllegalArgumentException ファイル系でない場合
     */
    private static String fileSectionKey(DataType type) {
        switch (type) {
            case SETUP_FIXED:
            case SETUP_VARIABLE:
                return YamlSection.KEY_SETUP_FILES;
            case EXPECTED_FIXED:
            case EXPECTED_VARIABLE:
                return YamlSection.KEY_EXPECTED_FILES;
            default:
                throw new IllegalArgumentException(
                        "unsupported data type for readFiles. type=[" + type + "]");
        }
    }

    /**
     * 送信系データタイプを YAML セクションキーへ変換する。
     *
     * @param type データタイプ
     * @return セクションキー
     * @throws IllegalArgumentException 送信系 4 種でない場合
     */
    private static String sendSyncSectionKey(DataType type) {
        switch (type) {
            case EXPECTED_REQUEST_HEADER_MESSAGES:
            case EXPECTED_REQUEST_BODY_MESSAGES:
            case RESPONSE_HEADER_MESSAGES:
            case RESPONSE_BODY_MESSAGES:
                return YamlSection.dataTypeToSectionKey(type);
            default:
                throw new IllegalArgumentException(
                        "unsupported data type for readSendSyncMessages. type=[" + type + "]");
        }
    }
}
