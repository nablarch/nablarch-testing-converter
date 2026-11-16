package nablarch.test.tool.converter.yaml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import nablarch.test.core.db.TableData;
import nablarch.test.core.file.DataFile;
import nablarch.test.core.file.FixedLengthFile;
import nablarch.test.core.file.DataFileAccessor;
import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.yaml.MessageContent;
import nablarch.test.core.reader.yaml.YamlSection;
import nablarch.test.tool.converter.DirectiveUtil;
import nablarch.test.tool.converter.TestDataFormatReader;
import nablarch.test.tool.converter.model.FieldDef;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.MessageDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;
import nablarch.test.tool.converter.reader.FileView;
import nablarch.test.tool.converter.reader.FragmentView;

/**
 * YAML（1 ファイル）を読み込み、中間モデル（{@link TestDataContainer}）へ写す IN リーダ。
 *
 * <p>
 * YAML の構造解釈は本体に任せ（{@link YamlBuilderAccessor} 経由）、本体が正規化してしまう値
 * （カラム名の大文字化・長さ省略・型表記など）だけを YAML の原文から復元する。
 * </p>
 *
 * @author kiyobot
 */
public class YamlFormatReader implements TestDataFormatReader {

    /** 本体再利用のためのアクセサ */
    private final YamlBuilderAccessor accessor;

    /**
     * 本番用コンストラクタ。実 YAML を読む {@link YamlBuilderAccessor} を構成する。
     */
    public YamlFormatReader() {
        this(new YamlBuilderAccessor());
    }

    /**
     * アクセサを注入するコンストラクタ（主にテスト用）。
     *
     * @param accessor 本体再利用のためのアクセサ
     */
    YamlFormatReader(YamlBuilderAccessor accessor) {
        this.accessor = accessor;
    }

    /**
     * {@inheritDoc}
     * <p>
     * 1 ファイルを 1 つの {@link TestDataSection} として返す。未知のトップレベルキーは無視する。
     * </p>
     */
    @Override
    public TestDataContainer read(String basePath, String resourceName) {
        Map<String, Object> yaml = accessor.loadRawMap(basePath, resourceName);
        List<TestDataBlock> blocks = new ArrayList<>();
        for (String key : yaml.keySet()) {
            addBlocksForSection(basePath, resourceName, yaml, key, blocks);
        }
        TestDataSection section = new TestDataSection(resourceName, blocks);
        return new TestDataContainer(resourceName, Collections.singletonList(section));
    }

    /**
     * 1 セクションキーに対応するブロック群を {@code blocks} へ追加する。未知キーは何もしない。
     *
     * @param basePath     ディレクトリ
     * @param resourceName リソース名
     * @param yaml         トップレベル Map
     * @param sectionKey   セクションキー
     * @param blocks       追加先
     */
    private void addBlocksForSection(String basePath, String resourceName, Map<String, Object> yaml,
                                     String sectionKey, List<TestDataBlock> blocks) {
        if (YamlSection.KEY_SETUP_TABLES.equals(sectionKey)) {
            addTableBlocks(basePath, resourceName, yaml, sectionKey, DataType.SETUP_TABLE_DATA, blocks);
        } else if (YamlSection.KEY_EXPECTED_TABLES.equals(sectionKey)) {
            addTableBlocks(basePath, resourceName, yaml, sectionKey, DataType.EXPECTED_TABLE_DATA, blocks);
        } else if (YamlSection.KEY_EXPECTED_COMPLETE_TABLES.equals(sectionKey)) {
            addTableBlocks(basePath, resourceName, yaml, sectionKey, DataType.EXPECTED_COMPLETED, blocks);
        } else if (YamlSection.KEY_LIST_MAPS.equals(sectionKey)) {
            addListMapBlocks(basePath, resourceName, yaml, blocks);
        } else if (YamlSection.KEY_SETUP_FILES.equals(sectionKey)) {
            addFileBlocks(basePath, resourceName, yaml, sectionKey, true, blocks);
        } else if (YamlSection.KEY_EXPECTED_FILES.equals(sectionKey)) {
            addFileBlocks(basePath, resourceName, yaml, sectionKey, false, blocks);
        } else if (YamlSection.KEY_MESSAGES.equals(sectionKey)) {
            addMessageBlocks(basePath, resourceName, yaml, blocks);
        } else if (YamlSection.KEY_EXPECTED_REQUEST_HEADER_MESSAGES.equals(sectionKey)) {
            addSendSyncBlocks(basePath, resourceName, yaml, sectionKey,
                    DataType.EXPECTED_REQUEST_HEADER_MESSAGES, blocks);
        } else if (YamlSection.KEY_EXPECTED_REQUEST_BODY_MESSAGES.equals(sectionKey)) {
            addSendSyncBlocks(basePath, resourceName, yaml, sectionKey,
                    DataType.EXPECTED_REQUEST_BODY_MESSAGES, blocks);
        } else if (YamlSection.KEY_RESPONSE_HEADER_MESSAGES.equals(sectionKey)) {
            addSendSyncBlocks(basePath, resourceName, yaml, sectionKey,
                    DataType.RESPONSE_HEADER_MESSAGES, blocks);
        } else if (YamlSection.KEY_RESPONSE_BODY_MESSAGES.equals(sectionKey)) {
            addSendSyncBlocks(basePath, resourceName, yaml, sectionKey,
                    DataType.RESPONSE_BODY_MESSAGES, blocks);
        }
    }

    // ------------------------------------------------------------------------
    // テーブル系（SETUP_TABLE_DATA／EXPECTED_TABLE_DATA／EXPECTED_COMPLETED）
    // ------------------------------------------------------------------------

    /**
     * テーブル系ブロックを写す。マーカーカラムだけのエントリを除き、本体の読み込み結果をそのまま用いる。
     *
     * @param basePath     ディレクトリ
     * @param resourceName リソース名
     * @param yaml         トップレベル Map
     * @param sectionKey   セクションキー
     * @param type         データタイプ（テーブル系）
     * @param blocks       追加先
     */
    private void addTableBlocks(String basePath, String resourceName, Map<String, Object> yaml,
                               String sectionKey, DataType type, List<TestDataBlock> blocks) {
        for (String groupId : groupIdsInOrder(yaml, sectionKey)) {
            List<TableData> tables = accessor.readTables(basePath, resourceName, groupId, type);
            List<Map<String, Object>> entries = entriesForGroupId(yaml, sectionKey, groupId);
            for (int t = 0; t < tables.size(); t++) {
                TableData table = tables.get(t);
                String[] columns = table.getColumnNames();
                if (columns.length == 0) {
                    // 本体はマーカーカラムを落とすため、マーカーカラムだけのエントリは原文から写す。
                    requireSameSize(tables.size(), entries.size(), sectionKey, groupId);
                    List<String> markerColumns = markerOnlyColumns(entries.get(t));
                    if (!markerColumns.isEmpty()) {
                        blocks.add(new TableDataBlock(type, groupId, table.getTableName(), markerColumns,
                                rawRows(entries.get(t), markerColumns)));
                        continue;
                    }
                }
                List<List<String>> rows = new ArrayList<>(table.size());
                for (int r = 0; r < table.size(); r++) {
                    List<String> row = new ArrayList<>(columns.length);
                    for (String column : columns) {
                        Object value = table.getValue(r, column);
                        row.add(value == null ? null : value.toString());
                    }
                    rows.add(row);
                }
                blocks.add(new TableDataBlock(type, groupId, table.getTableName(),
                        Arrays.asList(columns), rows));
            }
        }
    }

    // ------------------------------------------------------------------------
    // LIST_MAP
    // ------------------------------------------------------------------------

    /**
     * LIST_MAP ブロックを写す。カラムの並びだけ原文（YAML 記述順）から復元する。
     *
     * @param basePath     ディレクトリ
     * @param resourceName リソース名
     * @param yaml         トップレベル Map
     * @param blocks       追加先
     */
    private void addListMapBlocks(String basePath, String resourceName, Map<String, Object> yaml,
                                  List<TestDataBlock> blocks) {
        for (Object entryObj : YamlSection.getList(yaml, YamlSection.KEY_LIST_MAPS)) {
            Map<String, Object> entry = YamlSection.castMap(entryObj);
            String id = YamlSection.toStr(entry.get(YamlSection.FIELD_ID));
            List<String> markerColumns = markerOnlyColumns(entry);
            if (!markerColumns.isEmpty()) {
                blocks.add(new ListMapBlock(rawGroupId(entry), id, markerColumns,
                        rawRows(entry, markerColumns)));
                continue;
            }
            List<Map<String, String>> mapRows = accessor.readListMap(basePath, resourceName, id);
            List<String> orderedColumns = nonMarkerColumns(entry);
            List<List<String>> rows = new ArrayList<>(mapRows.size());
            for (Map<String, String> mapRow : mapRows) {
                List<String> row = new ArrayList<>(orderedColumns.size());
                for (String column : orderedColumns) {
                    row.add(mapRow.get(column));
                }
                rows.add(row);
            }
            blocks.add(new ListMapBlock(rawGroupId(entry), id, orderedColumns, rows));
        }
    }

    // ------------------------------------------------------------------------
    // ファイル系（SETUP_FIXED／EXPECTED_FIXED／SETUP_VARIABLE／EXPECTED_VARIABLE）
    // ------------------------------------------------------------------------

    /**
     * ファイル系ブロックを写す。本体の {@link DataFile} と同じグループの原文エントリは 1:1 同順で対応する。
     *
     * @param basePath     ディレクトリ
     * @param resourceName リソース名
     * @param yaml         トップレベル Map
     * @param sectionKey   セクションキー（{@code setup_files}／{@code expected_files}）
     * @param setup        セットアップ系なら真、期待値系なら偽
     * @param blocks       追加先
     */
    private void addFileBlocks(String basePath, String resourceName, Map<String, Object> yaml,
                              String sectionKey, boolean setup, List<TestDataBlock> blocks) {
        // 固定長／可変長は同一セクションのため、セクション解決用の代表値として使う。
        DataType representativeType = setup ? DataType.SETUP_FIXED : DataType.EXPECTED_FIXED;
        for (String groupId : groupIdsInOrder(yaml, sectionKey)) {
            List<DataFile> files = accessor.readFiles(basePath, resourceName, groupId, representativeType);
            List<Map<String, Object>> entries = entriesForGroupId(yaml, sectionKey, groupId);
            requireSameSize(files.size(), entries.size(), sectionKey, groupId);
            for (int i = 0; i < files.size(); i++) {
                DataFile file = files.get(i);
                Map<String, Object> entry = entries.get(i);
                FileView view = DataFileAccessor.read(file);
                FileDataBlock.FileType fileType = file instanceof FixedLengthFile
                        ? FileDataBlock.FileType.FIXED : FileDataBlock.FileType.VARIABLE;
                blocks.add(new FileDataBlock(fileDataType(setup, fileType), groupId, view.getPath(),
                        toStringDirectives(view.getDirectives()),
                        toRecordLayouts(view, records(entry))));
            }
        }
    }

    // ------------------------------------------------------------------------
    // MESSAGE
    // ------------------------------------------------------------------------

    /**
     * MESSAGE ブロックを写す。
     *
     * @param basePath     ディレクトリ
     * @param resourceName リソース名
     * @param yaml         トップレベル Map
     * @param blocks       追加先
     */
    private void addMessageBlocks(String basePath, String resourceName, Map<String, Object> yaml,
                                  List<TestDataBlock> blocks) {
        for (Object entryObj : YamlSection.getList(yaml, YamlSection.KEY_MESSAGES)) {
            Map<String, Object> entry = YamlSection.castMap(entryObj);
            String id = YamlSection.toStr(entry.get(YamlSection.FIELD_ID));
            MessageContent content = accessor.readMessage(basePath, resourceName, id);
            if (content == null) {
                continue;
            }
            FileView view = DataFileAccessor.read(content.getBody());
            blocks.add(new MessageDataBlock(DataType.MESSAGE, "", id,
                    toStringDirectives(view.getDirectives()),
                    new LinkedHashMap<>(content.getFwHeader()),
                    toRecordLayouts(view, records(entry))));
        }
    }

    // ------------------------------------------------------------------------
    // 送信同期メッセージ（要求/応答電文 4 種）
    // ------------------------------------------------------------------------

    /**
     * 送信系ブロックを写す。本体には {@code group_id} 省略（デフォルトグループ）を {@code null} で渡す。
     *
     * @param basePath     ディレクトリ
     * @param resourceName リソース名
     * @param yaml         トップレベル Map
     * @param sectionKey   セクションキー（送信系 4 種）
     * @param type         データタイプ（送信系 4 種）
     * @param blocks       追加先
     */
    private void addSendSyncBlocks(String basePath, String resourceName, Map<String, Object> yaml,
                                  String sectionKey, DataType type, List<TestDataBlock> blocks) {
        for (String rawGroup : rawGroupsInOrder(yaml, sectionKey)) {
            List<FixedLengthFile> bodies = accessor.readSendSyncMessages(basePath, resourceName, rawGroup, type);
            List<Map<String, Object>> entries = entriesForRawGroup(yaml, sectionKey, rawGroup);
            requireSameSize(bodies.size(), entries.size(), sectionKey, rawGroup);
            for (int i = 0; i < bodies.size(); i++) {
                FixedLengthFile body = bodies.get(i);
                Map<String, Object> entry = entries.get(i);
                FileView view = DataFileAccessor.read(body);
                blocks.add(new MessageDataBlock(type, rawGroupId(entry), body.getPath(),
                        toStringDirectives(view.getDirectives()),
                        new LinkedHashMap<>(),
                        toRecordLayouts(view, records(entry))));
            }
        }
    }

    // ------------------------------------------------------------------------
    // 原文復元の共通処理
    // ------------------------------------------------------------------------

    /**
     * 器のビューと原文レコードから、レコードレイアウト群を組み立てる。
     * <p>
     * フィールド定義とレコード種別は原文を正とする（本体は型表記と長さ省略を正規化してしまうため）。
     * </p>
     *
     * @param view           器のビュー
     * @param alignedRecords 器の断片と同順の原文レコード
     * @return レコードレイアウト群
     */
    private List<RecordLayout> toRecordLayouts(FileView view, List<Map<String, Object>> alignedRecords) {
        List<FragmentView> fragments = view.getFragments();
        if (fragments.size() != alignedRecords.size()) {
            throw new IllegalStateException(
                    "器の断片構造と原文レコードが不整合です。fragments=" + fragments.size()
                            + ", records=" + alignedRecords.size());
        }
        List<RecordLayout> records = new ArrayList<>(fragments.size());
        for (int i = 0; i < fragments.size(); i++) {
            FragmentView fragment = fragments.get(i);
            Map<String, Object> record = alignedRecords.get(i);
            String recordType = normalizeRecordType(YamlSection.toStr(record.get(YamlSection.FIELD_RECORD_TYPE)));
            List<FieldDef> fields = toFieldDefs(record);
            List<String> names = fragment.getNames();
            List<List<String>> rows = new ArrayList<>(fragment.getValues().size());
            for (Map<String, String> valueMap : fragment.getValues()) {
                List<String> row = new ArrayList<>(names.size());
                for (String name : names) {
                    row.add(valueMap.get(name));
                }
                rows.add(row);
            }
            records.add(new RecordLayout(recordType, fields, rows));
        }
        return records;
    }

    /** "Default" は NTF 仕様に存在しない値（Excelでは空欄）のため null に正規化する。 */
    private static String normalizeRecordType(String recordType) {
        if ("Default".equals(recordType) || "default".equals(recordType)) {
            return null;
        }
        return recordType;
    }

    /**
     * 原文レコードの {@code fields} から、記法を加工しないフィールド定義群を組み立てる。
     *
     * @param record 原文レコード Map
     * @return フィールド定義群（記述順）
     */
    private static List<FieldDef> toFieldDefs(Map<String, Object> record) {
        List<Object> fieldList = YamlSection.getList(record, YamlSection.FIELD_FIELDS);
        List<FieldDef> fields = new ArrayList<>(fieldList.size());
        for (Object fieldObj : fieldList) {
            Map<String, Object> field = YamlSection.castMap(fieldObj);
            fields.add(new FieldDef(
                    YamlSection.toStr(field.get(YamlSection.FIELD_NAME)),
                    YamlSection.toStr(field.get(YamlSection.FIELD_TYPE)),
                    YamlSection.toStr(field.get(YamlSection.FIELD_LENGTH))));
        }
        return fields;
    }

    /**
     * エントリの {@code records} を Map のリストとして全件取り出す。
     *
     * @param entry エントリ Map
     * @return レコード Map のリスト
     */
    private static List<Map<String, Object>> records(Map<String, Object> entry) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object recordObj : YamlSection.getList(entry, YamlSection.FIELD_RECORDS)) {
            result.add(YamlSection.castMap(recordObj));
        }
        return result;
    }

    // ------------------------------------------------------------------------
    // グループ列挙・整列
    // ------------------------------------------------------------------------

    /**
     * セクション内エントリのグループ ID を初出順で重複なく列挙する。
     *
     * @param yaml       トップレベル Map
     * @param sectionKey セクションキー
     * @return グループ ID のリスト（省略時は空文字）
     */
    private static List<String> groupIdsInOrder(Map<String, Object> yaml, String sectionKey) {
        List<String> groups = new ArrayList<>();
        for (Object entryObj : YamlSection.getList(yaml, sectionKey)) {
            String group = rawGroupId(YamlSection.castMap(entryObj));
            if (!groups.contains(group)) {
                groups.add(group);
            }
        }
        return groups;
    }

    /**
     * 送信系セクション内エントリのグループ ID を初出順で重複なく列挙する。
     *
     * @param yaml       トップレベル Map
     * @param sectionKey セクションキー
     * @return グループ ID のリスト（{@code group_id} 省略時は {@code null}）
     */
    private static List<String> rawGroupsInOrder(Map<String, Object> yaml, String sectionKey) {
        List<String> groups = new ArrayList<>();
        for (Object entryObj : YamlSection.getList(yaml, sectionKey)) {
            String group = YamlSection.toStr(YamlSection.castMap(entryObj).get(YamlSection.FIELD_GROUP_ID));
            if (!groups.contains(group)) {
                groups.add(group);
            }
        }
        return groups;
    }

    /**
     * 指定のグループに属するエントリを記述順で集める。
     *
     * @param yaml       トップレベル Map
     * @param sectionKey セクションキー
     * @param groupId    グループ ID（省略時は空文字）
     * @return エントリ Map のリスト
     */
    private static List<Map<String, Object>> entriesForGroupId(Map<String, Object> yaml, String sectionKey,
                                                               String groupId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object entryObj : YamlSection.getList(yaml, sectionKey)) {
            Map<String, Object> entry = YamlSection.castMap(entryObj);
            if (rawGroupId(entry).equals(groupId)) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * 送信系で、指定のグループに属するエントリを記述順で集める。
     *
     * @param yaml       トップレベル Map
     * @param sectionKey セクションキー
     * @param rawGroup   グループ ID（{@code group_id} 省略時は {@code null}）
     * @return エントリ Map のリスト
     */
    private static List<Map<String, Object>> entriesForRawGroup(Map<String, Object> yaml, String sectionKey,
                                                                String rawGroup) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object entryObj : YamlSection.getList(yaml, sectionKey)) {
            Map<String, Object> entry = YamlSection.castMap(entryObj);
            if (Objects.equals(rawGroup, YamlSection.toStr(entry.get(YamlSection.FIELD_GROUP_ID)))) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * エントリの {@code group_id} をグループ ID（{@code [ ]} を付けない値。省略時は空文字）として取り出す。
     *
     * @param entry エントリ Map
     * @return グループ ID（省略時は空文字）
     */
    private static String rawGroupId(Map<String, Object> entry) {
        String groupId = YamlSection.toStr(entry.get(YamlSection.FIELD_GROUP_ID));
        return groupId != null ? groupId : "";
    }

    /**
     * カラムがマーカーカラムだけのエントリについて、そのカラム名（記述順）を返す。
     * <p>
     * テストショット一覧と行の順序で対応付ける用途があり、エントリの数と並びに意味があるため、
     * このエントリもマーカーカラムと値を保って変換する。
     * </p>
     *
     * @param entry エントリ Map（{@code rows} を持つ）
     * @return マーカーカラム名（記述順）。マーカーカラムだけのエントリでなければ空
     */
    private static List<String> markerOnlyColumns(Map<String, Object> entry) {
        List<String> columns = YamlSection.resolveColumns(
                YamlSection.getList(entry, YamlSection.FIELD_ROWS));
        for (String column : columns) {
            if (!YamlSection.isMarker(column)) {
                return Collections.emptyList();
            }
        }
        return columns;
    }

    /**
     * エントリの行を、指定のカラム名の並びで原文のまま取り出す。
     * <p>
     * 空行の除去は本体と同じ条件にするため {@link YamlSection#dropBlankRows} を使う。
     * </p>
     *
     * @param entry   エントリ Map（{@code rows} を持つ）
     * @param columns 取り出すカラム名（記述順）
     * @return 行ごとの値（カラム名と同順。キーが無い位置は {@code null}）
     */
    private static List<List<String>> rawRows(Map<String, Object> entry, List<String> columns) {
        List<Object> rawRows = YamlSection.dropBlankRows(
                YamlSection.getList(entry, YamlSection.FIELD_ROWS));
        List<List<String>> rows = new ArrayList<>(rawRows.size());
        for (Object rawRow : rawRows) {
            Map<String, Object> map = YamlSection.castMap(rawRow);
            List<String> row = new ArrayList<>(columns.size());
            for (String column : columns) {
                row.add(YamlSection.objectToString(map.get(column)));
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * エントリのカラム名（記述順）からマーカーカラムを除いたものを返す。
     *
     * @param entry エントリ Map（{@code rows} を持つ）
     * @return 非マーカーカラム名（記述順）
     */
    private static List<String> nonMarkerColumns(Map<String, Object> entry) {
        List<String> columns = new ArrayList<>();
        for (String column : YamlSection.resolveColumns(YamlSection.getList(entry, YamlSection.FIELD_ROWS))) {
            if (!YamlSection.isMarker(column)) {
                columns.add(column);
            }
        }
        return columns;
    }

    // ------------------------------------------------------------------------
    // その他ヘルパー
    // ------------------------------------------------------------------------

    /**
     * セットアップ／期待値と固定長／可変長から、ファイル系データタイプを決定する。
     *
     * @param setup    セットアップ系なら真
     * @param fileType 固定長／可変長
     * @return データタイプ
     */
    private static DataType fileDataType(boolean setup, FileDataBlock.FileType fileType) {
        if (fileType == FileDataBlock.FileType.FIXED) {
            return setup ? DataType.SETUP_FIXED : DataType.EXPECTED_FIXED;
        }
        return setup ? DataType.SETUP_VARIABLE : DataType.EXPECTED_VARIABLE;
    }

    /**
     * 本体ディレクティブを文字列ディレクティブへ写す。
     * <p>
     * 区切り文字は本体が実制御文字へ変換して保持しており、そのまま書き出すと本体の読み込みで
     * {@code trim()} され失われるため、記法どおりの表現へ戻す。
     * </p>
     *
     * @param directives 本体ディレクティブ
     * @return 文字列ディレクティブ
     */
    private static Map<String, String> toStringDirectives(Map<String, Object> directives) {
        return DirectiveUtil.toStringDirectives(directives,
                new DirectiveUtil.ValueMapper() {
                    @Override
                    public String map(String key, String value) {
                        return DirectiveUtil.normalizeSeparator(key, value);
                    }
                });
    }

    /**
     * 器の要素数と原文エントリ数が一致することを検証する。
     *
     * @param containerSize 器の要素数
     * @param entrySize     原文エントリ数
     * @param sectionKey    診断用：セクションキー
     * @param group         診断用：グループ ID
     */
    private static void requireSameSize(int containerSize, int entrySize, String sectionKey, String group) {
        if (containerSize != entrySize) {
            throw new IllegalStateException(
                    "器と原文エントリが不整合です。section=" + sectionKey + ", group=" + group
                            + ", containers=" + containerSize + ", entries=" + entrySize);
        }
    }
}
