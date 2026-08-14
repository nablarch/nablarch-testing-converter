package nablarch.test.core.reader;

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
import nablarch.test.core.reader.yaml.InterpreterResolver;
import nablarch.test.core.reader.yaml.YamlFileBuilder;
import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.core.reader.yaml.YamlMessageBuilder;
import nablarch.test.core.reader.yaml.MessageContent;
import nablarch.test.core.reader.yaml.YamlSection;
import nablarch.test.core.reader.yaml.YamlTableDataBuilder;

/**
 * テストデータ変換ツール（{@code nablarch.test.tool.converter}）が、本体の YAML 読み込みを
 * 再利用して生の器を取り出すための薄いアダプタ。Excel 経路の {@link TestCoreReaderAdapter} と対称。
 *
 * <p>
 * 本体の YAML 読み込みは {@code reader.yaml} パッケージのビルダ
 * （{@link YamlTableDataBuilder}／{@link YamlFileBuilder}／{@link YamlMessageBuilder}）が
 * {@link YamlLoader#load} の返す順序保持 Map を走査して器を組み立てる。本アダプタはそれらビルダを
 * <b>{@link InterpreterResolver#raw() 値加工なし（空インタープリタ）}・デフォルト値補完なし</b>で配線し、
 * {@code ${...}}・{@code ${binaryFile:...}}・{@code null}・{@code ""} を記法のまま（未加工）保った
 * 生の本体器（{@link TableData}／{@link DataFile}／{@link MessagePool} 本文）を返す。
 * </p>
 * <p>
 * あわせて {@link #loadRawMap}（{@link YamlLoader#load} の透過）を備える。Excel が器の正規化を生行から
 * 復元するのに対し、YAML は器が正規化する値（カラム名の大文字化・長さ省略 {@code -}・型表記）の原文を
 * この順序保持 Map から復元する。
 * </p>
 *
 * @author kiyobot
 */
public class YamlTestCoreAdapter {

    /**
     * デフォルトグループ（{@code group_id} 省略）を本体ビルダへ引き渡すための合成グループ ID。
     * <p>
     * 値そのものに衝突回避の役割は無い。{@link #defaultGroupOnlyYaml} が {@code group_id} を持つ
     * エントリを 1 件も残さないため、この ID と一致しうる実在のグループ ID がそもそも同じ Map に
     * 存在しない。上流の照合は {@code YamlMessageBuilder} の
     * {@code rawGroupId != null && rawGroupId.equals(groupId)} で、非 null の文字列なら何でもよい。
     * 複製した Map の中だけで使い、元の YAML は書き換えない。
     * </p>
     */
    private static final String DEFAULT_GROUP_MARKER = "default-group";

    /** テーブル系ビルダ（空インタープリタ・補完なしで配線） */
    private final YamlTableDataBuilder tableBuilder;

    /** ファイル系ビルダ（空インタープリタで配線） */
    private final YamlFileBuilder fileBuilder;

    /** メッセージ系ビルダ（空インタープリタで配線） */
    private final YamlMessageBuilder messageBuilder;

    /**
     * コンストラクタ。ビルダを値加工なし（{@link InterpreterResolver#raw()}）で配線する。
     * <p>
     * テーブル構築に必要な {@link nablarch.test.core.db.DbInfo} はスタブ（{@link StubDbInfo}）を用いる。
     * 読み込み経路で実際に参照されるのはカラム型（一律 VARCHAR）のみで、値は型に依存せず生のまま格納される。
     * </p>
     */
    public YamlTestCoreAdapter() {
        InterpreterResolver raw = InterpreterResolver.raw();
        DefaultValues defaultValues = new BasicDefaultValues();
        this.tableBuilder = new YamlTableDataBuilder(new StubDbInfo(), defaultValues, raw);
        this.fileBuilder = new YamlFileBuilder(raw);
        this.messageBuilder = new YamlMessageBuilder(raw);
    }

    /**
     * YAML ファイルのトップレベル Map（原文。順序保持）をそのまま返す。
     * <p>
     * 変換ツールは器（構造）と本 Map（原文）を突き合わせて中間モデルを組み立てる。
     * </p>
     *
     * @param path     取得元パス
     * @param resource リソース名（拡張子なし）
     * @return YAML トップレベル Map（空ファイルの場合は空 Map）
     */
    public Map<String, Object> loadRawMap(String path, String resource) {
        return YamlLoader.load(path, resource);
    }

    /**
     * YAML ファイルが存在するかどうかを返す。
     *
     * @param path     取得元パス
     * @param resource リソース名
     * @return 存在する場合 true
     */
    public boolean isResourceExisting(String path, String resource) {
        return YamlLoader.isResourceExisting(path, resource);
    }

    /**
     * テーブルデータを取り出す。
     * <p>
     * デフォルト値補完（{@code fillDefaultValues}）は行わず、指定データタイプ・グループの生の
     * {@link TableData} 一覧を返す。
     * </p>
     *
     * @param path     取得元パス
     * @param resource リソース名
     * @param groupId  整形済みグループ ID（例: {@code "[case01]"} または {@code ""}）
     * @param type     データタイプ（{@link DataType#SETUP_TABLE_DATA}／
     *                 {@link DataType#EXPECTED_TABLE_DATA}／{@link DataType#EXPECTED_COMPLETED}）
     * @return テーブルデータ一覧
     * @throws IllegalArgumentException データタイプがテーブル系でない場合
     */
    public List<TableData> readTables(String path, String resource, String groupId, DataType type) {
        Map<String, Object> yaml = loadRawMap(path, resource);
        return tableBuilder.buildTableDataList(yaml, tableSectionKey(type), groupId, false, path);
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
     * ファイル（固定長／可変長）を取り出す。固定長・可変長の区別はエントリの {@code type} で決まる。
     *
     * @param path     取得元パス
     * @param resource リソース名
     * @param groupId  整形済みグループ ID（例: {@code "[case01]"} または {@code ""}）
     * @param type     データタイプ（{@link DataType#SETUP_FIXED}／{@link DataType#EXPECTED_FIXED}／
     *                 {@link DataType#SETUP_VARIABLE}／{@link DataType#EXPECTED_VARIABLE}）
     * @return ファイル一覧
     * @throws IllegalArgumentException データタイプがファイル系でない場合
     */
    public List<DataFile> readFiles(String path, String resource, String groupId, DataType type) {
        Map<String, Object> yaml = loadRawMap(path, resource);
        return fileBuilder.buildDataFileList(yaml, fileSectionKey(type), groupId, path);
    }

    /**
     * メッセージ（{@link DataType#MESSAGE}）を取り出す。本文（固定長ファイルの器）と FW 制御ヘッダを併せ持つ。
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
     * 送信同期メッセージ（要求/応答電文 4 種）のうち、指定グループに属する全ブロックの本文
     * （固定長ファイルの器）を取り出す。FW 制御ヘッダは送信系では常に空のため返さない。
     *
     * @param path     取得元パス
     * @param resource リソース名
     * @param groupId  グループ ID（{@code group_id} と生値で一致比較する）。{@code null} を渡すと
     *                 {@code group_id} を省略したエントリ（デフォルトグループ）を対象にする
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
     * デフォルトグループ（{@code group_id} 省略）のエントリだけを残し、それぞれに合成グループ ID
     * （{@link #DEFAULT_GROUP_MARKER}）を付けた 1 セクションだけの Map を組み立てる。
     * <p>
     * 本体の {@code YamlMessageBuilder#buildSendSyncBodies} は {@code group_id} が非 null のエントリしか
     * 返さないため（{@code rawGroupId != null && rawGroupId.equals(groupId)}）、記法仕様
     * （{@code testdata_notation.rst:254}「グループIDを省略した場合は…デフォルトグループが対象になる」）
     * どおりに読むにはこの前処理が要る。元の Map・エントリ Map は書き換えず複製する。
     * 合成 ID を付けるエントリは「{@code group_id} を持たないもの」だけに限り、かつ他のエントリを含めない
     * ため、実在のグループ ID と衝突しない。
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
     * ファイル系データタイプを YAML セクションキーへ変換する。固定長／可変長はセクションを分けない。
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
