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
import nablarch.test.core.file.FileView;
import nablarch.test.core.file.FragmentView;
import nablarch.test.core.file.TestCoreFileAdapter;
import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.PoiXlsReader;
import nablarch.test.core.reader.BlockHeader;
import nablarch.test.core.reader.MarkerOnlyBlock;
import nablarch.test.core.reader.MessageData;
import nablarch.test.core.reader.TestCoreReaderAdapter;
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

/**
 * Excel（1 シート）を読み込み、中間モデル（{@link TestDataContainer}）へ写す IN リーダ。
 *
 * <p>
 * 独自の POI パース・構造解析は持たない。本体の読み込みは {@link TestCoreReaderAdapter}
 * （本体 {@code reader} パッケージ相乗りのアダプタ）へ委譲し、本クラスは「どのブロックが存在するか」を
 * {@link TestCoreReaderAdapter#readHeaders(String, String)} で得て、各ブロックをデータタイプ別の
 * {@code read*} で取り出し、本体の器を中間モデルへ写すオーケストレーションに徹する。
 * 行のデータタイプ判定・マーカー解釈はすべてアダプタ（本体）側が担う。
 * </p>
 *
 * <p>
 * 器の中身は {@link TestCoreFileAdapter}（本体 {@code file} パッケージ相乗り）が読む。データ行の値は
 * 本クラスでは解釈しない。{@link TestCoreReaderAdapter} が本体パーサへインタープリタ列を渡し、
 * 本体が全セルを解釈してから構造解析するため、器から取り出した時点で既に
 * <b>テスティングフレームワークが解釈したあとの値</b>（Java {@code null} または {@link String}）に
 * なっている。一方、本体の構造解析は
 * テスト実行に必要な正規化を器に施す（長さ省略 {@code -} の実バイト長化・型記法のフレームワーク表記化・
 * レコード種別の private 化）。作成者が記述した原文が要るため、これら正規化される箇所だけ
 * {@link TestCoreReaderAdapter#readBlockBodyLines(String, String, String, String, DataType) 生行}から
 * 原文を復元する。生行は器（{@link FragmentView}）を
 * 権威に断片数・フィールド数・値行数を決め、本体パーサと同形に走査して原文を充填する。
 * </p>
 *
 * <p>
 * 同一データタイプ・同一グループの複数ブロックは本体 API が一括取得するため、ブロックの並びは
 * 「各 (データタイプ, グループ) を最初に検出した位置」にまとめて展開する。データタイプをまたぐ
 * 厳密な記述順は保たれない場合があるが、NTF はデータを (データタイプ, ID) で取得するため
 * ブロックの並び順は意味を持たない。
 * </p>
 *
 * @author kiyobot
 */
public class XlsFormatReader implements TestDataFormatReader {

    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(XlsFormatReader.class.getName());

    /** 本体再利用のためのアダプタ */
    private final TestCoreReaderAdapter adapter;

    /**
     * 本番用コンストラクタ。実 Excel を読む {@link PoiXlsReader} を注入したアダプタを構成する。
     */
    public XlsFormatReader() {
        this(new TestCoreReaderAdapter(new PoiXlsReader()));
    }

    /**
     * アダプタを注入するコンストラクタ（主にテスト用）。
     *
     * @param adapter 本体再利用のためのアダプタ
     */
    XlsFormatReader(TestCoreReaderAdapter adapter) {
        this.adapter = adapter;
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
        List<BlockHeader> headers = adapter.readHeaders(basePath, resourceName);
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
                // 到達しない。readHeaders はマーカー行から取れた DataType だけを返し DataType.DEFAULT は
                // 返さないため、ここへ届くのは残る 13 種であり、上の 5 分岐がその 13 種を尽くしている。
                // それでも残すのは、DataType に値が増えたとき、その種別のブロックが黙って変換結果から
                // 欠落するのを防ぎ、その場で検出するための安全網としてである。
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
        List<TableData> tables = adapter.readTables(basePath, resourceName, groupId, type);
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
        // 列順は adapter.readListMapColumnNames() から取得する。
        // adapter.readListMap() が返す Map は TreeMap 由来のためアルファベット順になるが、
        // readListMapColumnNames() は HeaderLine の effectiveColumnNames（Excel 記述順）を返す。
        List<String> rawColumnNames = adapter.readListMapColumnNames(basePath, resourceName, header.getIdentifier());
        List<String> columnNames = deduplicateColumnNames(rawColumnNames, resourceName, header.getIdentifier());
        // 同一ブロックを2回読む（readListMapColumnNames と readListMap の二重パース）のは、readListMap が TreeMap を返す設計を本体側で変えずに済ませるためである。
        MarkerOnlyBlock markerOnly = markerOnlyBlock(basePath, resourceName, header.getGroupId(),
                                                     header.getIdentifier(), DataType.LIST_MAP, columnNames);
        if (markerOnly != null) {
            return new ListMapBlock(header.getGroupId(), header.getIdentifier(),
                                    markerOnly.getColumnNames(), markerOnly.getRows());
        }
        List<Map<String, String>> mapRows = adapter.readListMap(basePath, resourceName, header.getIdentifier());
        List<List<String>> cellRows = new ArrayList<>();
        for (int r = 0; r < rowCount(columnNames, mapRows.size()); r++) {
            Map<String, String> mapRow = mapRows.get(r);
            List<String> cells = new ArrayList<>(columnNames.size());
            for (String column : columnNames) {
                cells.add(mapRow.get(column));
            }
            cellRows.add(cells);
        }
        return new ListMapBlock(header.getGroupId(), header.getIdentifier(), columnNames,
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
        List<? extends DataFile> files = adapter.readFiles(basePath, resourceName, groupId, type);
        boolean fixed = FileDataBlock.fileTypeOf(type) == FileDataBlock.FileType.FIXED;
        List<TestDataBlock> result = new ArrayList<>();
        for (DataFile file : files) {
            FileView view = TestCoreFileAdapter.read(file);
            List<List<String>> bodyLines =
                    adapter.readBlockBodyLines(basePath, resourceName, groupId, view.getPath(), type);
            result.add(new FileDataBlock(type, groupId, view.getPath(),
                    toStringDirectives(view.getDirectives()),
                    toRecordLayouts(view, bodyLines, fixed)));
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
        MessageData message = adapter.readMessage(basePath, resourceName, header.getIdentifier());
        if (message == null) {
            // ヘッダスキャンで MESSAGE= マーカーを検出したが、本体パーサが同 ID のデータを見つけられない場合。
            // 本体の MessageParser が空結果を返したとき adapter.readMessage は null を返す（正常系）。
            return null;
        }
        FixedLengthFile body = message.getBody();
        FileView view = TestCoreFileAdapter.read(body);
        // MESSAGE 本文は固定長。生行には FW ヘッダ行が先頭に含まれるが、原文復元は名前行を
        // 起点に走査するため FW ヘッダ行は読み飛ばされる。
        List<List<String>> bodyLines =
                adapter.readBlockBodyLines(basePath, resourceName, header.getGroupId(), header.getIdentifier(),
                        DataType.MESSAGE);
        // FW ヘッダ値は本体 MessageParser が解釈後の値として返すため、変換器側の加工は要らない。
        Map<String, String> fwHeaderFields = new LinkedHashMap<>(message.getFwHeader());
        return new MessageDataBlock(DataType.MESSAGE, header.getGroupId(), header.getIdentifier(),
                toStringDirectives(view.getDirectives()), fwHeaderFields,
                toRecordLayouts(view, bodyLines, true));
    }

    /**
     * 送信同期メッセージ（要求/応答電文 4 種）の全ブロック（指定の (データタイプ, グループ) に属する）を
     * {@link MessageDataBlock} 群へ写す。
     * <p>
     * 構造は MESSAGE と同型（名前行の先頭セルがレコード種別、{@code no} 列＝メタ情報は器が
     * 値から除いて {@code FIRST_FIELD_NO} に隔離する）だが、送信系に FW 制御ヘッダは無い（常に空）。
     * 本文の固定長ファイルは {@link TestCoreReaderAdapter#readSendSyncMessages} がグループ単位で
     * まとめて返し、各ファイルの {@link DataFile#getPath()} がマーカー {@code =} 以降の識別子に一致する。
     * 原文（レコード種別・型記法・長さ・値）は MESSAGE と同じく生行から復元する。
     * </p>
     *
     * @param basePath     ディレクトリ
     * @param resourceName リソース名
     * @param groupId      グループ ID（{@code [case1]} 等）
     * @param type         データタイプ（送信系 4 種）
     * @return メッセージデータブロック一覧
     */
    private List<TestDataBlock> readSendSyncBlocks(String basePath, String resourceName, String groupId, DataType type) {
        List<FixedLengthFile> bodies = adapter.readSendSyncMessages(basePath, resourceName, groupId, type);
        List<TestDataBlock> result = new ArrayList<>();
        for (FixedLengthFile body : bodies) {
            String identifier = body.getPath();
            FileView view = TestCoreFileAdapter.read(body);
            List<List<String>> bodyLines =
                    adapter.readBlockBodyLines(basePath, resourceName, groupId, identifier, type);
            result.add(new MessageDataBlock(type, groupId, identifier,
                    toStringDirectives(view.getDirectives()),
                    new LinkedHashMap<>(),
                    toRecordLayouts(view, bodyLines, true)));
        }
        return result;
    }

    /**
     * 器のビュー（断片構造）と生行から、原文を復元したレコードレイアウト群を組み立てる。
     * <p>
     * 器（{@link FragmentView}）を権威に断片数・フィールド名・値行数を決め、生行を本体パーサと同形に
     * 走査して原文（レコード種別＝名前行の先頭セル／型記法＝型行／長さ＝長さ行／値＝値行）を取る。
     * 生行のうちマーカー行は既に除かれており、各行の先頭セルを落とした残り（{@link #tail(List)}）が
     * 器のフィールドと同順・同数で並ぶ。最初の断片の名前行はフィールド名一致で特定し、それより前の
     * ディレクティブ行・FW ヘッダ行を読み飛ばす。
     * </p>
     *
     * @param view      器のビュー
     * @param bodyLines 生のボディ行（マーカー行除去済み）
     * @param fixed     固定長（長さ行を持つ）なら真、可変長なら偽
     * @return レコードレイアウト群
     */
    private List<RecordLayout> toRecordLayouts(FileView view, List<List<String>> bodyLines, boolean fixed) {
        List<RecordLayout> records = new ArrayList<>();
        List<FragmentView> fragments = view.getFragments();
        if (fragments.isEmpty()) {
            return records;
        }
        int idx = skipToFirstNameRow(bodyLines, fragments.get(0).getNames());
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
     * <p>
     * 空セルは {@code PoiXlsReader} が {@code ""} を返すが、中間モデルの
     * {@link RecordLayout}（同クラスのコンストラクタ Javadoc「レコード種別（省略時は {@code null}）」）は
     * 省略を {@code null} で表す。辺②（YAML）も省略時は {@code null} を入れる。
     * 辺①だけ {@code ""} になる非対称を無くす（{@code issues.md} XLS-06）。
     * </p>
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
     * @return 名前行のインデックス（一致する行の位置）
     */
    private int skipToFirstNameRow(List<List<String>> bodyLines, List<String> firstNames) {
        int idx = 0;
        while (idx < bodyLines.size() && !tail(bodyLines.get(idx)).equals(firstNames)) {
            idx++;
        }
        return idx;
    }

    /**
     * 器（断片構造）の名前行が生行の {@code idx} 位置に一致することを検証し、{@code idx} をそのまま返す。
     * <p>
     * 一致しなければ器↔生行の対応が破綻している＝前提崩れ。誤った原文を静かに充填せず
     * 即座に失敗させる。
     * </p>
     *
     * @param bodyLines 生のボディ行
     * @param idx       検証する位置
     * @param names     対象断片のフィールド名（診断・一致判定に使用）
     * @return 検証済みの {@code idx}（名前行位置）
     * @throws IllegalStateException 名前行が生行に見つからない場合
     */
    private int verifyNameRow(List<List<String>> bodyLines, int idx, List<String> names) {
        // 内部整合性ガード。断片構造と生行の対応が壊れていれば二経路読み込みロジックのバグ。
        if (idx >= bodyLines.size() || !tail(bodyLines.get(idx)).equals(names)) {
            throw new IllegalStateException(
                    "器の断片構造と生行が不整合です。名前行 names=" + names + " が生行に見つかりません。");
        }
        return idx;
    }

    /**
     * 型行・長さ行（固定長のみ）を読んで {@link FieldDef} リストを組み立て、続けてデータ行も読んで
     * {@link RecordLayout} を {@code records} に追加する。
     * <p>
     * このメソッドは名前行の次の行（{@code idx}）から始まり、型行・長さ行・全値行を消費した後の
     * 次インデックスを返す。
     * </p>
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
     * 器が期待する位置の生行を取り出す。生行が器の断片構造より短い（対応が破綻している）場合は
     * 前提崩れとして即座に失敗させる。
     *
     * @param bodyLines 生のボディ行
     * @param idx       取り出す位置
     * @param names     診断用：対象断片のフィールド名
     * @param rowKind   診断用：行種別（型行／長さ行／値行）
     * @return 当該位置の生行
     * @throws IllegalStateException 当該位置に生行が存在しない場合
     */
    private static List<String> requireLine(List<List<String>> bodyLines, int idx, List<String> names, String rowKind) {
        // 内部整合性ガード。断片構造と生行の対応が壊れていれば二経路読み込みロジックのバグ。
        if (idx >= bodyLines.size()) {
            throw new IllegalStateException(
                    "器の断片構造と生行が不整合です。断片 names=" + names + " の" + rowKind + "が生行に存在しません。");
        }
        return bodyLines.get(idx);
    }

    /**
     * 本体ディレクティブ（{@code Map<String, Object>}）を文字列ディレクティブへ写す。
     * <p>
     * 本体器のディレクティブ値は型変換済み（{@code Charset}・enum・整数等）で、順序も
     * {@code HashMap} 由来で記述順を保たない。Excel 経路はこの器固有挙動を受容し、
     * 値は {@link Object#toString()} で文字列化する。null 値は（テーブル/LIST_MAP 経路と対称に）
     * null のまま保持し、文字列 {@code "null"} へ化けさせない。
     * Excel 経路固有の逆正規化（{@link #normalizeDirectiveValue}）を {@link DirectiveUtil#toStringDirectives}
     * の {@code valueMapper} として差し込む。
     * </p>
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
     * 本体ディレクティブ値を、YAML へ書き出す表現（仕様 DR-09/DR-10 のシンボル）へ逆正規化する。
     * 区切り文字（record-separator／field-separator）の逆正規化は辺②（YamlFormatReader）と共有する
     * {@link DirectiveUtil#normalizeSeparator} が行う。それ以外のキーは値をそのまま返す。
     */
    private static String normalizeDirectiveValue(String key, String value) {
        if ("record-separator".equals(key) || "field-separator".equals(key)) {
            return DirectiveUtil.normalizeSeparator(key, value);
        }
        // 区切り文字以外は加工しない。本体パーサへ NullInterpreter → QuotationTrimmer →
        // LineSeparatorInterpreter を渡しているため、ここへ届く値は本体が解釈し終えたあとの値である
        // （例: quoting-delimiter のセル値がダブルクォート 5 個なら、本体が外側 1 層を外した 3 個が届く）。
        // 変換器がここでもう一度剥がすと二重適用になり、本体が読む値とずれる。
        // 記法へ戻すのは書き出し側（XlsFormatWriter の toCellNotation）の役目である。
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
     * 収集方式が「グループ」のデータタイプ（テーブル・ファイル・グループ ID 付きの電文）では、
     * フレームワークは対象のブロックを読み始めたあと、別のキーのマーカー行に当たった時点で収集を打ち切る。
     * そのため、間に別のブロックを挟んだあとに再び現れた同じキーのブロックは読まれない。
     * YAML 形式にはこれに相当する記法が無く、そのまま変換すると読まれなかったブロックが有効になって
     * 意味が変わってしまうため、警告を出す。
     * </p>
     *
     * <p>
     * <b>出力から外すのは本クラスではない。</b>フレームワークが打ち切った結果、
     * 読まれなかったブロックはそもそも器に入ってこない。本メソッドは検出と警告だけを行う。
     * </p>
     *
     * <p>
     * 識別子で 1 件を引くデータタイプ（{@code LIST_MAP} ／ {@code MESSAGE}）は収集方式が
     * 「グループ」ではないため対象外である。
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
     * <p>
     * 打ち切りが起きるのは、開始位置より後ろで最初に別のキーのヘッダが現れた位置である。
     * それより後ろに同じキーのヘッダがあれば、それらは読まれない。
     * </p>
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
     * カラム名の行が<b>マーカーカラムだけ</b>で構成されたブロックの版面を返す。そうでなければ {@code null}。
     *
     * <p>
     * マーカーカラムはフレームワークが読み込み対象から除外するため、除外後のカラム名が 0 件になったときだけ
     * この形の候補になる。除外前の版面（マーカーカラムの名前と各行の値）は器に残っていないため、
     * アダプタが本体の行読み込みを再利用して取り直す。
     * </p>
     *
     * <p>
     * <b>このブロックは名前と値を保ったまま変換する。</b>各エントリはフィールドを持たないが、
     * テストショット一覧と行の順序で対応付ける用途ではエントリの数と並びが意味を持つためである。
     * 実データカラムを 1 つでも持つブロックのマーカーカラムは、従来どおりフレームワークの除外に従って落とす。
     * </p>
     *
     * @param basePath     ディレクトリ
     * @param resourceName リソース名
     * @param groupId      生値のグループ ID
     * @param identifier   識別子（テーブル名／{@code LIST_MAP} の ID）
     * @param type         データタイプ
     * @param columnNames  マーカーカラムを除いたカラム名
     * @return 版面。マーカーカラムだけのブロックでなければ {@code null}
     */
    /**
     * ブロックが持つデータ行の数を返す。カラム名を 1 件も持たないブロックは 0 を返す。
     *
     * <p>
     * <b>カラム名を 1 件も持たないブロックは行を持てない</b>（{@link ColumnRowDataBlock} の不変条件）。
     * どちらの記法にも「値があってカラム名が無い」形は書けないためである。
     * </p>
     *
     * <p>
     * <b>カラム名の行がマーカーカラムだけのブロックはここへ来ない。</b>そちらは
     * {@link #markerOnlyBlock} がマーカーカラムの名前と値ごと保つ。ここへ来るのは、カラム名の行が
     * <b>マーカーカラムでもないのに 1 件も残らなかった</b>ブロックだけである。実際に起こるのは次の 2 つで、
     * どちらも版面に書き戻せる形が無いため行を落とす。
     * </p>
     * <ul>
     *   <li>カラム名の行のセルが {@code null} 記法だけ —— フレームワークは解釈のあとに行末の空要素を
     *       取り除くため、カラム名が 1 件も残らない（テーブル系の経路）</li>
     *   <li>{@code LIST_MAP} の識別行にグループ ID を書いた —— カラム名の取り出しがグループ指定なしの
     *       ブロックだけを探すため、対象ブロックが見つからず 0 件になる（{@code LIST_MAP} の経路）</li>
     * </ul>
     *
     * @param columnNames ブロックのカラム名
     * @param rowCount    フレームワークが返した行数
     * @return データ行の数（カラム名が空なら 0）
     */
    private static int rowCount(List<String> columnNames, int rowCount) {
        return columnNames.isEmpty() ? 0 : rowCount;
    }

    private MarkerOnlyBlock markerOnlyBlock(String basePath, String resourceName, String groupId,
                                            String identifier, DataType type, List<String> columnNames) {
        if (!columnNames.isEmpty()) {
            return null;
        }
        return adapter.readMarkerOnlyBlock(basePath, resourceName, groupId, identifier, type);
    }

    /**
     * カラム名リストから重複を除去し（後勝ち）、重複があれば WARN ログを出力する。
     * <p>
     * ヘッダ行に同名カラムが複数存在する場合、最後の出現位置のみを有効とする（後勝ち）。
     * 重複が検出されると、ファイル名・シート名・ブロック識別子・重複カラム名を含む WARN ログを出力する。
     * </p>
     *
     * @param columnNames  元のカラム名リスト（重複を含む可能性あり）
     * @param resourceName リソース名（{@code "ブック名/シート名"} 形式）
     * @param blockId      ブロック識別子（テーブル名・LIST_MAP 識別子等）
     * @return 重複を除去したカラム名リスト（後勝ち・元の順序を保持）
     */
    private static List<String> deduplicateColumnNames(List<String> columnNames, String resourceName, String blockId) {
        // 各カラム名の最後の出現インデックスを記録
        Map<String, Integer> lastIndex = new HashMap<>();
        for (int i = 0; i < columnNames.size(); i++) {
            lastIndex.put(columnNames.get(i), i);
        }
        // 重複チェック：最後の出現でない位置は除外、初回重複検出時に WARN を出す
        Set<String> warned = new HashSet<>();
        List<String> result = new ArrayList<>(lastIndex.size());
        for (int i = 0; i < columnNames.size(); i++) {
            String name = columnNames.get(i);
            if (lastIndex.get(name) != i) {
                // この位置は後方に同名カラムが存在するため除外（後勝ち）
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
