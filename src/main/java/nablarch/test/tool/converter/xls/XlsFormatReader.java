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
import nablarch.test.core.reader.MessageData;
import nablarch.test.core.reader.TestCoreReaderAdapter;
import nablarch.test.core.util.interpreter.InterpretationContext;
import nablarch.test.core.util.interpreter.QuotationTrimmer;
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
 * 器の中身は {@link TestCoreFileAdapter}（本体 {@code file} パッケージ相乗り）が読む。IN 値は
 * 記法のまま（未加工）で運ばれる（アダプタが空 interpreters で配線するため）。一方、本体の構造解析は
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
            List<List<String>> rows = new ArrayList<>();
            for (int r = 0; r < table.size(); r++) {
                List<String> row = new ArrayList<>(columnNames.size());
                for (String column : columnNames) {
                    Object value = table.getValue(r, column);
                    row.add(value == null ? null : stripQuotes(value.toString()));
                }
                rows.add(row);
            }
            result.add(new TableDataBlock(type, groupId, table.getTableName(), columnNames,
                                          dropEmptyEntries(rows)));
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
        List<Map<String, String>> mapRows = adapter.readListMap(basePath, resourceName, header.getIdentifier());
        List<List<String>> rows = new ArrayList<>();
        for (Map<String, String> mapRow : mapRows) {
            List<String> row = new ArrayList<>(columnNames.size());
            for (String column : columnNames) {
                String value = mapRow.get(column);
                row.add(value == null ? null : stripQuotes(value));
            }
            rows.add(row);
        }
        return new ListMapBlock(header.getGroupId(), header.getIdentifier(), columnNames,
                                dropEmptyEntries(rows));
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
        FileDataBlock.FileType fileType = isFixed(type) ? FileDataBlock.FileType.FIXED : FileDataBlock.FileType.VARIABLE;
        List<TestDataBlock> result = new ArrayList<>();
        for (DataFile file : files) {
            FileView view = TestCoreFileAdapter.read(file);
            List<List<String>> bodyLines =
                    adapter.readBlockBodyLines(basePath, resourceName, groupId, view.getPath(), type);
            result.add(new FileDataBlock(type, groupId, view.getPath(), fileType,
                    toStringDirectives(view.getDirectives()),
                    toRecordLayouts(view, bodyLines, isFixed(type))));
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
        // FW ヘッダ値は本体 MessageParser が生文字列として返すため、QuotationTrimmer 記法は使われない。
        // stripQuotes は適用しない。
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
            List<String> valueCells = tail(requireLine(bodyLines, idx, names, "値行"));
            idx++;
            List<String> row = new ArrayList<>(names.size());
            for (int i = 0; i < names.size(); i++) {
                String cellValue = i < valueCells.size() ? valueCells.get(i) : "";
                row.add(stripQuotes(cellValue));
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
     * {@link DirectiveUtil#normalizeSeparator} が行う。それ以外のキーは QuotationTrimmer 記法を剥がす。
     */
    private static String normalizeDirectiveValue(String key, String value) {
        if ("record-separator".equals(key) || "field-separator".equals(key)) {
            return DirectiveUtil.normalizeSeparator(key, value);
        }
        // ディレクティブ値も本体 Excel 実行経路では readTestData の interpret により QuotationTrimmer が
        // 適用される（例: quoting-delimiter のセル値 """ は " に解釈される）。変換器はインタープリタが
        // 空のため、ここで同等処理を行い Excel 実行経路と挙動を一致させる（YAML 経路は QuotationTrimmer
        // を持たないため必須）。ただし QuotationTrimmer 記法（前後がダブルクォートで囲まれた 2 文字超）の
        // ときのみ剥がす。デフォルトディレクティブとして本体器に注入される " 1 文字（可変長の
        // quoting-delimiter 既定値）等は記法ではなく生値であり、本体 Excel 経路でも QuotationTrimmer を
        // 通らないため、ここでも素通しする（1 文字を剥がそうとする QuotationTrimmer の例外も同時に回避）。
        if (isQuotationWrapped(value)) {
            return stripQuotes(value);
        }
        return value;
    }

    /**
     * 値が QuotationTrimmer 記法（前後が同一のダブルクォート（半角／全角）で囲まれ、剥がしても
     * 破綻しない 2 文字超）であるかを判定する。
     *
     * @param value 判定対象
     * @return QuotationTrimmer を安全に適用できる記法なら真
     */
    private static boolean isQuotationWrapped(String value) {
        if (value == null || value.length() <= 2) {
            return false;
        }
        return (value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("”") && value.endsWith("”"));
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

    /** Excel 引用符記法を取り除くために使い回す {@link QuotationTrimmer} インスタンス */
    private static final QuotationTrimmer QUOTATION_TRIMMER = new QuotationTrimmer();

    /**
     * Excel 引用符記法を取り除く（{@link QuotationTrimmer} に委譲）。
     * <p>
     * Excel のテストデータでは {@code “”} が空文字、{@code “値”} が {@code 値}（前後クォート除去）を
     * 表す慣習がある。本体の {@code QuotationTrimmer} は Excel 経路のテスト実行時に適用されるが、
     * Excel→YAML 変換経路ではインタープリタが空のため、変換器側で同等の処理を行う。
     * </p>
     *
     * @param value セル値
     * @return 前後ダブルクォートを除去した文字列（長さ1以下または非クォートはそのまま）
     */
    private static String stripQuotes(String value) {
        // toRecordLayouts の valueCells.get(i) は Excel の空白セルに対して null を返すため、このガードは必須。
        if (value == null) {
            return null;
        }
        return new InterpretationContext(value, QUOTATION_TRIMMER).invokeNext();
    }

    /**
     * マーカーカラムを除外したあとの行から、空エントリ（全要素が {@code null} または空文字の行）を除く。
     * <p>
     * {@code notation:1535}「全要素が null または空文字のエントリは読み飛ばされる」を、
     * <b>マーカーカラムの除外（{@code notation:1550}）のあとに</b>適用する。本体
     * {@code PoiXlsReader#readLine} は除外前の生の行で空エントリを判定するため、
     * マーカーカラムだけを持つ行は本体では空エントリにならず、除外後に「セルを 1 つも持たない行」として
     * 残ってしまう。これは記法に無い形（{@code notation:652} のとおり、テーブルデータは
     * カラム名とデータ行を持つ構成である）なので、ここで落とす。
     * </p>
     * <p>
     * 記法は 2 つの規則の前後関係を定めていない。「除外 → 空エントリ判定」を前提とする
     * （ユーザー確定・2026-08-18。解説書側へ明文化を申し送る）。課題は
     * {@code coverage/issues.md} の XLS-08 に記録している。
     * </p>
     *
     * @param rows マーカーカラム除外後の行
     * @return 空エントリを除いた行
     */
    private static List<List<String>> dropEmptyEntries(List<List<String>> rows) {
        List<List<String>> result = new ArrayList<>(rows.size());
        for (List<String> row : rows) {
            if (!isEmptyEntry(row)) {
                result.add(row);
            }
        }
        return result;
    }

    /**
     * 行が空エントリ（全要素が {@code null} または空文字）かを判定する。
     * <p>要素を 1 つも持たない行も空エントリとみなす。</p>
     *
     * @param row 判定対象の行
     * @return 空エントリなら {@code true}
     */
    private static boolean isEmptyEntry(List<String> row) {
        for (String value : row) {
            if (value != null && !value.isEmpty()) {
                return false;
            }
        }
        return true;
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
        return isFixed(type)
                || type == DataType.SETUP_VARIABLE
                || type == DataType.EXPECTED_VARIABLE;
    }

    /**
     * 固定長ファイルのデータタイプか判定する。
     *
     * @param type データタイプ
     * @return 固定長なら真
     */
    private static boolean isFixed(DataType type) {
        return type == DataType.SETUP_FIXED || type == DataType.EXPECTED_FIXED;
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
