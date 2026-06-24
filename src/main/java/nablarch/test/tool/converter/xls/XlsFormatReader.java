package nablarch.test.tool.converter.xls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * 原文を復元する（設計書 §共通「器が正規化する値の原文復元」）。生行は器（{@link FragmentView}）を
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
        List<TestDataBlock> blocks = new ArrayList<TestDataBlock>();
        Set<String> processed = new HashSet<String>();
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
        List<TestDataBlock> result = new ArrayList<TestDataBlock>();
        for (TableData table : tables) {
            String[] columns = table.getColumnNames();
            List<String> columnNames = Arrays.asList(columns);
            List<List<String>> rows = new ArrayList<List<String>>();
            for (int r = 0; r < table.size(); r++) {
                List<String> row = new ArrayList<String>(columns.length);
                for (String column : columns) {
                    Object value = table.getValue(r, column);
                    row.add(value == null ? null : stripQuotes(value.toString()));
                }
                rows.add(row);
            }
            result.add(new TableDataBlock(type, groupId, table.getTableName(), columnNames, rows));
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
        List<Map<String, String>> mapRows = adapter.readListMap(basePath, resourceName, header.getIdentifier());
        List<String> columnNames = new ArrayList<String>();
        for (Map<String, String> mapRow : mapRows) {
            for (String key : mapRow.keySet()) {
                if (!columnNames.contains(key)) {
                    columnNames.add(key);
                }
            }
        }
        List<List<String>> rows = new ArrayList<List<String>>();
        for (Map<String, String> mapRow : mapRows) {
            List<String> row = new ArrayList<String>(columnNames.size());
            for (String column : columnNames) {
                String value = mapRow.get(column);
                row.add(value == null ? null : stripQuotes(value));
            }
            rows.add(row);
        }
        return new ListMapBlock(header.getGroupId(), header.getIdentifier(), columnNames, rows);
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
        List<TestDataBlock> result = new ArrayList<TestDataBlock>();
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
        Map<String, String> fwHeaderFields = new LinkedHashMap<String, String>(message.getFwHeader());
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
        List<TestDataBlock> result = new ArrayList<TestDataBlock>();
        for (FixedLengthFile body : bodies) {
            String identifier = body.getPath();
            FileView view = TestCoreFileAdapter.read(body);
            List<List<String>> bodyLines =
                    adapter.readBlockBodyLines(basePath, resourceName, groupId, identifier, type);
            result.add(new MessageDataBlock(type, groupId, identifier,
                    toStringDirectives(view.getDirectives()),
                    new LinkedHashMap<String, String>(),
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
        List<RecordLayout> records = new ArrayList<RecordLayout>();
        int idx = 0;
        boolean first = true;
        for (FragmentView fragment : view.getFragments()) {
            List<String> names = fragment.getNames();
            // 名前行を特定する。最初の断片はフィールド名一致まで読み飛ばし（ディレクティブ/FW ヘッダ行を除外）、
            // 以降は直前の値行の直後がそのまま次の名前行。
            if (first) {
                while (idx < bodyLines.size() && !tail(bodyLines.get(idx)).equals(names)) {
                    idx++;
                }
                first = false;
            }
            // 器（断片構造）を権威とし、生行がそれと一致することを各断片の名前行で検証する。
            // 一致しなければ器↔生行の対応が破綻している＝前提崩れ。誤った原文を静かに充填せず
            // 即座に失敗させる（StubDbInfo の番人コードと同じ思想。設計書 §共通）。
            // 内部整合性ガード。断片構造と生行の対応が壊れていれば二経路読み込みロジックのバグ。
            if (idx >= bodyLines.size() || !tail(bodyLines.get(idx)).equals(names)) {
                throw new IllegalStateException(
                        "器の断片構造と生行が不整合です。名前行 names=" + names + " が生行に見つかりません。");
            }
            String recordType = bodyLines.get(idx).get(0);
            idx++;
            List<String> originalTypes = tail(requireLine(bodyLines, idx, names, "型行"));
            idx++;
            List<String> originalLengths = null;
            if (fixed) {
                originalLengths = tail(requireLine(bodyLines, idx, names, "長さ行"));
                idx++;
            }
            List<FieldDef> fields = new ArrayList<FieldDef>(names.size());
            for (int i = 0; i < names.size(); i++) {
                String type = i < originalTypes.size() ? originalTypes.get(i) : null;
                String length = originalLengths != null && i < originalLengths.size() ? originalLengths.get(i) : null;
                fields.add(new FieldDef(names.get(i), type, length));
            }
            List<List<String>> rows = new ArrayList<List<String>>(fragment.getValues().size());
            for (int v = 0; v < fragment.getValues().size(); v++) {
                List<String> valueCells = tail(requireLine(bodyLines, idx, names, "値行"));
                idx++;
                List<String> row = new ArrayList<String>(names.size());
                for (int i = 0; i < names.size(); i++) {
                    String cellValue = i < valueCells.size() ? valueCells.get(i) : "";
                    row.add(stripQuotes(cellValue));
                }
                rows.add(row);
            }
            records.add(new RecordLayout(recordType, fields, rows));
        }
        return records;
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
     * {@code HashMap} 由来で記述順を保たない。Excel 経路（判断 A）はこの器固有挙動を受容し、
     * 値は {@link Object#toString()} で文字列化する。null 値は（テーブル/LIST_MAP 経路と対称に）
     * null のまま保持し、文字列 {@code "null"} へ化けさせない。
     * </p>
     *
     * @param directives 本体ディレクティブ
     * @return 文字列ディレクティブ
     */
    private Map<String, String> toStringDirectives(Map<String, Object> directives) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (Map.Entry<String, Object> entry : directives.entrySet()) {
            Object value = entry.getValue();
            result.put(entry.getKey(),
                       value == null ? null : normalizeDirectiveValue(entry.getKey(), value.toString()));
        }
        return result;
    }

    /**
     * 本体ディレクティブ値を、YAML へ書き出す表現（仕様 DR-09/DR-10 のシンボル）へ逆正規化する。
     * 本体は record-separator を LineSeparator.evaluate で実改行（\r\n 等）に、field-separator のタブを
     * 実タブに変換済みのため、そのまま toString() すると本体 setDirective の trim() で失われる。
     * 書き出し時にシンボル名（CRLF/LF/CR/NONE・\\t）へ戻すことで往復一致させる。
     */
    private static String normalizeDirectiveValue(String key, String value) {
        if ("record-separator".equals(key)) {
            if ("\r\n".equals(value)) return "CRLF";
            if ("\n".equals(value)) return "LF";
            if ("\r".equals(value)) return "CR";
            if (value.isEmpty()) return "NONE";
            return value;
        }
        if ("field-separator".equals(key)) {
            if ("\t".equals(value)) return "\\t";
            return value;
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
