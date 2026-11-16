package nablarch.test.tool.converter.xls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import nablarch.test.core.db.TableData;
import nablarch.test.core.file.DataFile;
import nablarch.test.core.file.FixedLengthFile;
import nablarch.test.core.file.DataFileAccessor;
import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.PoiXlsReader;
import nablarch.test.core.reader.XlsParserAccessor;
import nablarch.test.tool.converter.DirectiveUtil;
import nablarch.test.tool.converter.TestDataFormatReader;
import nablarch.test.tool.converter.model.ColumnRowDataBlock;
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
 * Excel（1 シート）を読み込み、中間モデル（{@link TestDataContainer}）へ写す IN リーダ。
 *
 * <p>
 * POI パース・構造解析は本体（{@link XlsParserAccessor}）へ委譲する。値は本体が解釈したあとの値を使うが、
 * 本体が正規化してしまう箇所（長さ・型記法・レコード種別）だけは作成者の原文が要るため生行から復元する。
 * </p>
 *
 * <p>
 * 同一データタイプ・同一グループの複数ブロックは本体 API が一括取得するため、最初に検出した位置へ
 * まとめて展開する。NTF はデータを (データタイプ, ID) で取得するため並び順は意味を持たない。
 * </p>
 *
 * @author kiyobot
 */
public class XlsFormatReader implements TestDataFormatReader {

    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(XlsFormatReader.class.getName());

    /** 本体再利用のためのアクセサ */
    private final XlsParserAccessor accessor;

    /**
     * 本番用コンストラクタ。{@link PoiXlsReader} で実 Excel を読む。
     */
    public XlsFormatReader() {
        this(new XlsParserAccessor(new PoiXlsReader()));
    }

    /**
     * アクセサを注入するコンストラクタ（主にテスト用）。
     *
     * @param accessor 本体再利用のためのアクセサ
     */
    XlsFormatReader(XlsParserAccessor accessor) {
        this.accessor = accessor;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Excel の 1 シートを読み、1 つの {@link TestDataSection} を持つ {@link TestDataContainer} を返す。
     * {@code resourceName} は {@code "ブック名/シート名"}（ブック名 → コンテナ名、シート名 → セクション名）。
     * </p>
     */
    @Override
    public TestDataContainer read(String basePath, String resourceName) {
        List<BlockHeader> headers = accessor.readHeaders(basePath, resourceName);
        warnInterleavedBlocks(headers, resourceName);
        List<TestDataBlock> blocks = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        for (BlockHeader header : headers) {
            DataType type = header.getType();
            if (isTableType(type)) {
                if (processed.add(batchKey(type, header.getGroupId()))) {
                    blocks.addAll(readTableBlocks(basePath, resourceName, header.getGroupId(), type));
                }
            } else if (isFileType(type)) {
                if (processed.add(batchKey(type, header.getGroupId()))) {
                    blocks.addAll(readFileBlocks(basePath, resourceName, header.getGroupId(), type));
                }
            } else if (type == DataType.LIST_MAP) {
                if (processed.add(singleKey(type, header.getIdentifier()))) {
                    blocks.add(readListMapBlock(basePath, resourceName, header));
                } else {
                    LOGGER.warning("[" + bookName(resourceName) + "] シート \"" + sheetName(resourceName)
                            + "\" に、識別子 \"" + header.getIdentifier() + "\" の LIST_MAP が複数あります。"
                            + "テスティングフレームワークは最初のものだけを読むため、2 つ目以降を出力しません。");
                }
            } else if (type == DataType.MESSAGE) {
                if (processed.add(singleKey(type, header.getIdentifier()))) {
                    TestDataBlock block = readMessageBlock(basePath, resourceName, header);
                    if (block != null) {
                        blocks.add(block);
                    }
                }
            } else if (XlsDataTypeUtil.isSendSyncType(type)) {
                if (processed.add(batchKey(type, header.getGroupId()))) {
                    blocks.addAll(readSendSyncBlocks(basePath, resourceName, header.getGroupId(), type));
                }
            } else {
                // 到達しない。DataType が増えたときにブロックが黙って欠落するのを防ぐため残す。
                throw new IllegalStateException("unhandled DataType: " + type);
            }
        }
        TestDataSection section = new TestDataSection(sheetName(resourceName), blocks);
        return new TestDataContainer(bookName(resourceName), Collections.singletonList(section));
    }

    /**
     * テーブル系ブロック（指定の (データタイプ, グループ) に属する全テーブル）を写す。
     *
     * @param basePath     ディレクトリ
     * @param resourceName リソース名
     * @param groupId      グループ ID
     * @param type         データタイプ（テーブル系）
     * @return テーブルデータブロック一覧
     */
    private List<TestDataBlock> readTableBlocks(String basePath, String resourceName, String groupId, DataType type) {
        List<TableData> tables = accessor.readTables(basePath, resourceName, groupId, type);
        List<TestDataBlock> result = new ArrayList<>();
        for (TableData table : tables) {
            String[] columns = table.getColumnNames();
            List<String> columnNames = deduplicateColumnNames(
                    Arrays.asList(columns), resourceName, table.getTableName());
            MarkerOnlyBlock markerOnly = markerOnlyBlock(basePath, resourceName, groupId,
                                                         table.getTableName(), type, columnNames);
            if (markerOnly != null) {
                result.add(new TableDataBlock(type, groupId, table.getTableName(),
                                              markerOnly.getColumnNames(), markerOnly.getRows()));
                continue;
            }
            List<List<String>> cellRows = new ArrayList<>();
            for (int r = 0; r < rowCount(columnNames, table.size()); r++) {
                List<String> cells = new ArrayList<>(columnNames.size());
                for (String column : columnNames) {
                    Object value = table.getValue(r, column);
                    cells.add(value == null ? null : value.toString());
                }
                cellRows.add(cells);
            }
            result.add(new TableDataBlock(type, groupId, table.getTableName(), columnNames,
                                          cellRows));
        }
        return result;
    }

    /**
     * LIST_MAP ブロックを写す。
     *
     * @param basePath     ディレクトリ
     * @param resourceName リソース名
     * @param header       ブロックヘッダ
     * @return LIST_MAP ブロック
     */
    private TestDataBlock readListMapBlock(String basePath, String resourceName, BlockHeader header) {
        // 本体は LIST_MAP のグループ ID を使わないため、書かれていれば外して警告する。
        if (!header.getGroupId().isEmpty()) {
            LOGGER.warning("[" + bookName(resourceName) + "] シート \"" + sheetName(resourceName)
                    + "\" の LIST_MAP \"" + header.getIdentifier() + "\" にグループID \"" + header.getGroupId()
                    + "\" が書かれています。テスティングフレームワークは LIST_MAP のグループIDを使わないため、"
                    + "グループIDを除いて出力します。");
        }
        // readListMap() の Map は記述順を保たないため、列順は別途取得する。
        List<String> rawColumnNames = accessor.readListMapColumnNames(basePath, resourceName, header.getIdentifier());
        List<String> columnNames = deduplicateColumnNames(rawColumnNames, resourceName, header.getIdentifier());
        MarkerOnlyBlock markerOnly = markerOnlyBlock(basePath, resourceName, header.getGroupId(),
                                                     header.getIdentifier(), DataType.LIST_MAP, columnNames);
        if (markerOnly != null) {
            return new ListMapBlock("", header.getIdentifier(),
                                    markerOnly.getColumnNames(), markerOnly.getRows());
        }
        List<Map<String, String>> mapRows = accessor.readListMap(basePath, resourceName, header.getIdentifier());
        List<List<String>> cellRows = new ArrayList<>();
        for (int r = 0; r < rowCount(columnNames, mapRows.size()); r++) {
            Map<String, String> mapRow = mapRows.get(r);
            List<String> cells = new ArrayList<>(columnNames.size());
            for (String column : columnNames) {
                cells.add(mapRow.get(column));
            }
            cellRows.add(cells);
        }
        return new ListMapBlock("", header.getIdentifier(), columnNames,
                                cellRows);
    }

    /**
     * ファイル系ブロック（指定の (データタイプ, グループ) に属する全ファイル）を写す。
     *
     * @param basePath     ディレクトリ
     * @param resourceName リソース名
     * @param groupId      グループ ID
     * @param type         データタイプ（ファイル系）
     * @return ファイルデータブロック一覧
     */
    private List<TestDataBlock> readFileBlocks(String basePath, String resourceName, String groupId, DataType type) {
        List<? extends DataFile> files = accessor.readFiles(basePath, resourceName, groupId, type);
        boolean fixed = FileDataBlock.fileTypeOf(type) == FileDataBlock.FileType.FIXED;
        List<TestDataBlock> result = new ArrayList<>();
        for (DataFile file : files) {
            FileView view = DataFileAccessor.read(file);
            List<List<String>> bodyLines =
                    accessor.readBlockBodyLines(basePath, resourceName, groupId, view.getPath(), type);
            result.add(new FileDataBlock(type, groupId, view.getPath(),
                    toStringDirectives(view.getDirectives()),
                    toRecordLayouts(view, bodyLines, fixed, view.getDirectives().keySet())));
        }
        return result;
    }

    /**
     * MESSAGE ブロックを写す。本文（固定長ファイル）のレコードレイアウトと FW 制御ヘッダを持つ。
     *
     * @param basePath     ディレクトリ
     * @param resourceName リソース名
     * @param header       ブロックヘッダ
     * @return MESSAGE ブロック。対象が存在しない場合は {@code null}
     */
    private TestDataBlock readMessageBlock(String basePath, String resourceName, BlockHeader header) {
        MessageData message = accessor.readMessage(basePath, resourceName, header.getIdentifier());
        if (message == null) {
            // マーカーはあるが本体が同 ID のデータを見つけられない場合（正常系）。
            return null;
        }
        FixedLengthFile body = message.getBody();
        FileView view = DataFileAccessor.read(body);
        List<List<String>> bodyLines =
                accessor.readBlockBodyLines(basePath, resourceName, header.getGroupId(), header.getIdentifier(),
                        DataType.MESSAGE);
        Map<String, String> fwHeaderFields = new LinkedHashMap<>(message.getFwHeader());
        // 本体は FW ヘッダ行も設定行として読むため、名前行の候補から除く。
        Set<String> settingRowNames = new HashSet<>(view.getDirectives().keySet());
        settingRowNames.addAll(fwHeaderFields.keySet());
        return new MessageDataBlock(DataType.MESSAGE, header.getGroupId(), header.getIdentifier(),
                toStringDirectives(view.getDirectives()), fwHeaderFields,
                toRecordLayouts(view, bodyLines, true, settingRowNames));
    }

    /**
     * 送信同期メッセージ（要求/応答電文 4 種）の全ブロック（指定の (データタイプ, グループ) に属する）を
     * {@link MessageDataBlock} 群へ写す。構造は MESSAGE と同じだが FW 制御ヘッダは持たない。
     *
     * @param basePath     ディレクトリ
     * @param resourceName リソース名
     * @param groupId      グループ ID（{@code [ ]} を付けない値）
     * @param type         データタイプ（送信系 4 種）
     * @return メッセージデータブロック一覧
     */
    private List<TestDataBlock> readSendSyncBlocks(String basePath, String resourceName, String groupId, DataType type) {
        List<FixedLengthFile> bodies = accessor.readSendSyncMessages(basePath, resourceName, groupId, type);
        List<TestDataBlock> result = new ArrayList<>();
        for (FixedLengthFile body : bodies) {
            String identifier = body.getPath();
            FileView view = DataFileAccessor.read(body);
            List<List<String>> bodyLines =
                    accessor.readBlockBodyLines(basePath, resourceName, groupId, identifier, type);
            result.add(new MessageDataBlock(type, groupId, identifier,
                    toStringDirectives(view.getDirectives()),
                    new LinkedHashMap<>(),
                    toRecordLayouts(view, bodyLines, true, view.getDirectives().keySet())));
        }
        return result;
    }

    /**
     * 器のビュー（断片構造）と生行から、原文を復元したレコードレイアウト群を組み立てる。
     * 断片数・フィールド名・値行数は器に従い、レコード種別・型記法・長さは生行から取る。
     *
     * @param view            器のビュー
     * @param bodyLines       生のボディ行（マーカー行除去済み）
     * @param fixed           固定長（長さ行を持つ）なら真、可変長なら偽
     * @param settingRowNames 本体パーサが設定行として読んだ行の先頭セルの集合
     * @return レコードレイアウト群
     */
    private List<RecordLayout> toRecordLayouts(FileView view, List<List<String>> bodyLines, boolean fixed,
                                               Set<String> settingRowNames) {
        List<RecordLayout> records = new ArrayList<>();
        List<FragmentView> fragments = view.getFragments();
        if (fragments.isEmpty()) {
            return records;
        }
        int idx = skipToFirstNameRow(bodyLines, fragments.get(0).getNames(), settingRowNames);
        for (FragmentView fragment : fragments) {
            List<String> names = fragment.getNames();
            idx = verifyNameRow(bodyLines, idx, names);
            String recordType = emptyToNull(bodyLines.get(idx).get(0));
            idx++;
            idx = readFieldDefs(bodyLines, idx, names, fixed, records, recordType, fragment);
        }
        return records;
    }

    /**
     * レコード種別セルの値を中間モデルの表現へ直す。
     * 中間モデルは省略を {@code null} で表すため、空セルの {@code ""} を {@code null} にそろえる。
     *
     * @param recordType レコード種別セルの値
     * @return 空文字なら {@code null}、それ以外はそのまま
     */
    private static String emptyToNull(String recordType) {
        return recordType == null || recordType.isEmpty() ? null : recordType;
    }

    /**
     * 最初の断片の名前行が現れる位置まで生行を読み飛ばす（ディレクティブ行・FW ヘッダ行を除外）。
     *
     * @param bodyLines 生のボディ行
     * @param firstNames 最初の断片のフィールド名リスト
     * @param settingRowNames 当該ブロックで設定行として読まれた行の先頭セルの集合
     * @return 名前行のインデックス（一致する行の位置）
     */
    private int skipToFirstNameRow(List<List<String>> bodyLines, List<String> firstNames, Set<String> settingRowNames) {
        int idx = 0;
        while (idx < bodyLines.size() && !isNameRow(bodyLines.get(idx), firstNames, settingRowNames)) {
            idx++;
        }
        return idx;
    }

    /**
     * 生行が名前行かどうかを判定する。
     * <p>
     * 単一フィールドの電文ではディレクティブ行（例: {@code [file-type, JSON]}）もフィールド名と一致しうるため、
     * NTF 本体と同じく、先頭セルが設定行の名前である行は名前行とみなさない。
     * </p>
     *
     * @param line            生行
     * @param names           フィールド名リスト
     * @param settingRowNames 設定行として読まれた行の先頭セルの集合
     * @return 名前行なら真
     */
    private static boolean isNameRow(List<String> line, List<String> names, Set<String> settingRowNames) {
        if (!line.isEmpty() && settingRowNames.contains(line.get(0))) {
            return false;
        }
        return tail(line).equals(names);
    }

    /**
     * 器（断片構造）の名前行が生行の {@code idx} 位置に一致することを検証し、{@code idx} をそのまま返す。
     *
     * @param bodyLines 生のボディ行
     * @param idx       検証する位置
     * @param names     対象断片のフィールド名（診断・一致判定に使用）
     * @return 検証済みの {@code idx}（名前行位置）
     * @throws IllegalStateException 名前行が生行に見つからない場合
     */
    private int verifyNameRow(List<List<String>> bodyLines, int idx, List<String> names) {
        if (idx >= bodyLines.size() || !tail(bodyLines.get(idx)).equals(names)) {
            throw new IllegalStateException(
                    "器の断片構造と生行が不整合です。名前行 names=" + names + " が生行に見つかりません。");
        }
        return idx;
    }

    /**
     * 型行・長さ行（固定長のみ）・値行を読んで {@link RecordLayout} を {@code records} に追加する。
     *
     * @param bodyLines  生のボディ行
     * @param idx        型行の開始インデックス（名前行の次）
     * @param names      対象断片のフィールド名
     * @param fixed      固定長（長さ行を持つ）なら真
     * @param records    組み立て結果の追加先
     * @param recordType レコード種別（名前行の先頭セル）
     * @param fragment   対象断片（値行数の取得に使用）
     * @return 次の断片の名前行インデックス
     */
    private int readFieldDefs(List<List<String>> bodyLines, int idx, List<String> names,
            boolean fixed, List<RecordLayout> records, String recordType, FragmentView fragment) {
        List<String> originalTypes = tail(requireLine(bodyLines, idx, names, "型行"));
        idx++;
        List<String> originalLengths = null;
        if (fixed) {
            originalLengths = tail(requireLine(bodyLines, idx, names, "長さ行"));
            idx++;
        }
        List<FieldDef> fields = new ArrayList<>(names.size());
        for (int i = 0; i < names.size(); i++) {
            String type = i < originalTypes.size() ? originalTypes.get(i) : null;
            String length = originalLengths != null && i < originalLengths.size() ? originalLengths.get(i) : null;
            fields.add(new FieldDef(names.get(i), type, length));
        }
        idx = readDataRows(bodyLines, idx, names, fragment, records, recordType, fields);
        return idx;
    }

    /**
     * 断片の値行をすべて読み込んで {@link RecordLayout} を {@code records} に追加する。
     *
     * @param bodyLines  生のボディ行
     * @param idx        最初の値行のインデックス
     * @param names      対象断片のフィールド名（診断用）
     * @param fragment   対象断片（値行数の取得に使用）
     * @param records    組み立て結果の追加先
     * @param recordType レコード種別
     * @param fields     フィールド定義リスト
     * @return 全値行を消費した後の次インデックス
     */
    private int readDataRows(List<List<String>> bodyLines, int idx, List<String> names,
            FragmentView fragment, List<RecordLayout> records, String recordType, List<FieldDef> fields) {
        List<List<String>> rows = new ArrayList<>(fragment.getValues().size());
        for (int v = 0; v < fragment.getValues().size(); v++) {
            requireLine(bodyLines, idx, names, "値行");
            idx++;
            Map<String, String> valueMap = fragment.getValues().get(v);
            List<String> row = new ArrayList<>(names.size());
            for (int i = 0; i < names.size(); i++) {
                row.add(valueMap.get(names.get(i)));
            }
            rows.add(row);
        }
        records.add(new RecordLayout(recordType, fields, rows));
        return idx;
    }

    /**
     * 器が期待する位置の生行を取り出す。
     *
     * @param bodyLines 生のボディ行
     * @param idx       取り出す位置
     * @param names     診断用：対象断片のフィールド名
     * @param rowKind   診断用：行種別（型行／長さ行／値行）
     * @return 当該位置の生行
     * @throws IllegalStateException 当該位置に生行が存在しない場合
     */
    private static List<String> requireLine(List<List<String>> bodyLines, int idx, List<String> names, String rowKind) {
        if (idx >= bodyLines.size()) {
            throw new IllegalStateException(
                    "器の断片構造と生行が不整合です。断片 names=" + names + " の" + rowKind + "が生行に存在しません。");
        }
        return bodyLines.get(idx);
    }

    /**
     * 本体ディレクティブ（{@code Map<String, Object>}）を文字列ディレクティブへ写す。
     *
     * @param directives 本体ディレクティブ
     * @return 文字列ディレクティブ
     */
    private Map<String, String> toStringDirectives(Map<String, Object> directives) {
        return DirectiveUtil.toStringDirectives(directives,
                new DirectiveUtil.ValueMapper() {
                    @Override
                    public String map(String key, String value) {
                        return normalizeDirectiveValue(key, value);
                    }
                });
    }

    /**
     * 本体ディレクティブ値を、YAML へ書き出す表現へ逆正規化する。区切り文字以外はそのまま返す。
     */
    private static String normalizeDirectiveValue(String key, String value) {
        if ("record-separator".equals(key) || "field-separator".equals(key)) {
            return DirectiveUtil.normalizeSeparator(key, value);
        }
        // 値は本体が解釈済みのため、ここで再度加工すると二重適用になる。
        return value;
    }

    /**
     * 先頭要素を除いたリストを返す。空リストはそのまま返す。
     *
     * @param list 対象
     * @return 先頭要素を除いたリスト
     */
    private static List<String> tail(List<String> list) {
        return list.isEmpty() ? list : list.subList(1, list.size());
    }

    /**
     * 同じキー（データタイプ ＋ グループ ID）のデータブロックの間に別のデータブロックが挟まっているシートを
     * 検出し、フレームワークが読まないデータブロックについて警告を出す。
     *
     * <p>
     * フレームワークは別のキーのマーカー行に当たった時点で収集を打ち切るため、後ろのブロックは読まれず
     * 器にも入らない。YAML にはこれに相当する記法が無く、黙って落ちることになるため警告する。
     * </p>
     *
     * @param headers      記述順のブロックヘッダ一覧
     * @param resourceName リソース名（{@code "ブック名/シート名"} 形式）
     */
    private static void warnInterleavedBlocks(List<BlockHeader> headers, String resourceName) {
        Set<String> warned = new HashSet<>();
        for (int start = 0; start < headers.size(); start++) {
            BlockHeader header = headers.get(start);
            if (!isGroupCollected(header.getType())) {
                continue;
            }
            String key = batchKey(header.getType(), header.getGroupId());
            if (!warned.add(key)) {
                continue;
            }
            List<String> unread = unreadIdentifiersAfter(headers, start, key);
            if (unread.isEmpty()) {
                continue;
            }
            LOGGER.warning("[" + bookName(resourceName) + "] シート \"" + sheetName(resourceName)
                    + "\" では、データタイプ \"" + header.getType().getName()
                    + "\"・グループID \"" + header.getGroupId()
                    + "\" のデータブロックの間に別のデータブロックが挟まっています。"
                    + "テスティングフレームワークは Excel 形式では後ろのデータブロックを読まないため、"
                    + "次のデータブロックを出力しません: " + unread);
        }
    }

    /**
     * 指定位置から始まるブロック群のうち、フレームワークが読まないブロックの識別子を返す。
     *
     * @param headers 記述順のブロックヘッダ一覧
     * @param start   対象キーが最初に現れた位置
     * @param key     対象キー
     * @return 読まれないブロックの識別子（記述順。無ければ空）
     */
    private static List<String> unreadIdentifiersAfter(List<BlockHeader> headers, int start, String key) {
        int broken = -1;
        for (int i = start + 1; i < headers.size(); i++) {
            BlockHeader header = headers.get(i);
            if (!batchKey(header.getType(), header.getGroupId()).equals(key)) {
                broken = i;
                break;
            }
        }
        List<String> unread = new ArrayList<>();
        if (broken < 0) {
            return unread;
        }
        for (int i = broken + 1; i < headers.size(); i++) {
            BlockHeader header = headers.get(i);
            if (batchKey(header.getType(), header.getGroupId()).equals(key)) {
                unread.add(header.getIdentifier());
            }
        }
        return unread;
    }

    /**
     * データタイプの収集方式が「グループ」であるかを返す。
     *
     * @param type データタイプ
     * @return グループ単位で収集するデータタイプなら真
     */
    private static boolean isGroupCollected(DataType type) {
        return isTableType(type) || isFileType(type) || XlsDataTypeUtil.isSendSyncType(type);
    }

    /**
     * ブロックが持つデータ行の数を返す。カラム名を 1 件も持たないブロックは 0 を返す。
     *
     * <p>
     * カラム名の無いブロックは行を持てない（{@link ColumnRowDataBlock} の不変条件）。
     * どちらの記法にも「値があってカラム名が無い」形は書けないためである。
     * </p>
     *
     * @param columnNames ブロックのカラム名
     * @param rowCount    フレームワークが返した行数
     * @return データ行の数（カラム名が空なら 0）
     */
    private static int rowCount(List<String> columnNames, int rowCount) {
        return columnNames.isEmpty() ? 0 : rowCount;
    }

    /**
     * カラム名の行が<b>マーカーカラムだけ</b>で構成されたブロックの版面を返す。そうでなければ {@code null}。
     *
     * <p>
     * テストショット一覧と行の順序で対応付ける用途ではエントリの数と並びが意味を持つため、
     * フレームワークが除外するマーカーカラムをこの形に限り名前と値ごと保つ。
     * </p>
     *
     * @param basePath     ディレクトリ
     * @param resourceName リソース名
     * @param groupId      グループ ID（{@code [ ]} を付けない値）
     * @param identifier   識別子（テーブル名／{@code LIST_MAP} の ID）
     * @param type         データタイプ
     * @param columnNames  マーカーカラムを除いたカラム名
     * @return 版面。マーカーカラムだけのブロックでなければ {@code null}
     */
    private MarkerOnlyBlock markerOnlyBlock(String basePath, String resourceName, String groupId,
                                            String identifier, DataType type, List<String> columnNames) {
        if (!columnNames.isEmpty()) {
            return null;
        }
        return accessor.readMarkerOnlyBlock(basePath, resourceName, groupId, identifier, type);
    }

    /**
     * カラム名リストから重複を除去し（後勝ち）、重複があれば WARN ログを出力する。
     *
     * @param columnNames  元のカラム名リスト（重複を含む可能性あり）
     * @param resourceName リソース名（{@code "ブック名/シート名"} 形式）
     * @param blockId      ブロック識別子（テーブル名・LIST_MAP 識別子等）
     * @return 重複を除去したカラム名リスト（後勝ち・元の順序を保持）
     */
    private static List<String> deduplicateColumnNames(List<String> columnNames, String resourceName, String blockId) {
        Map<String, Integer> lastIndex = new HashMap<>();
        for (int i = 0; i < columnNames.size(); i++) {
            lastIndex.put(columnNames.get(i), i);
        }
        Set<String> warned = new HashSet<>();
        List<String> result = new ArrayList<>(lastIndex.size());
        for (int i = 0; i < columnNames.size(); i++) {
            String name = columnNames.get(i);
            if (lastIndex.get(name) != i) {
                if (warned.add(name)) {
                    LOGGER.warning("[" + bookName(resourceName) + "] シート \"" + sheetName(resourceName)
                            + "\" のブロック \"" + blockId
                            + "\" に重複カラム名 \"" + name + "\" があります。"
                            + (lastIndex.get(name) + 1) + " 列目の値を採用します。");
                }
            } else {
                result.add(name);
            }
        }
        return result;
    }

    /**
     * 一括取得型（テーブル・ファイル）の重複排除キー。
     *
     * @param type    データタイプ
     * @param groupId グループ ID
     * @return キー
     */
    private static String batchKey(DataType type, String groupId) {
        return type.name() + ' ' + groupId;
    }

    /**
     * 単体取得型（LIST_MAP・MESSAGE）の重複排除キー。
     *
     * @param type       データタイプ
     * @param identifier 識別子
     * @return キー
     */
    private static String singleKey(DataType type, String identifier) {
        return type.name() + ' ' + identifier;
    }

    /**
     * テーブル系データタイプか判定する。
     *
     * @param type データタイプ
     * @return テーブル系なら真
     */
    private static boolean isTableType(DataType type) {
        return type == DataType.SETUP_TABLE_DATA
                || type == DataType.EXPECTED_TABLE_DATA
                || type == DataType.EXPECTED_COMPLETED;
    }

    /**
     * ファイル系データタイプか判定する。
     *
     * @param type データタイプ
     * @return ファイル系なら真
     */
    private static boolean isFileType(DataType type) {
        return type == DataType.SETUP_FIXED
                || type == DataType.EXPECTED_FIXED
                || type == DataType.SETUP_VARIABLE
                || type == DataType.EXPECTED_VARIABLE;
    }

    /**
     * リソース名（{@code "ブック名/シート名"}）からブック名を取り出す。
     *
     * @param resourceName リソース名
     * @return ブック名（{@code '/'} が無ければリソース名全体）
     */
    private static String bookName(String resourceName) {
        int slash = resourceName.indexOf('/');
        return slash < 0 ? resourceName : resourceName.substring(0, slash);
    }

    /**
     * リソース名（{@code "ブック名/シート名"}）からシート名を取り出す。
     *
     * @param resourceName リソース名
     * @return シート名（{@code '/'} が無ければリソース名全体）
     */
    private static String sheetName(String resourceName) {
        int slash = resourceName.indexOf('/');
        return slash < 0 ? resourceName : resourceName.substring(slash + 1);
    }
}
