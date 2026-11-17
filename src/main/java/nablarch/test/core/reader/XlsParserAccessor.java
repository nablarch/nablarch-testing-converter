package nablarch.test.core.reader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import nablarch.test.NablarchTestUtils;
import nablarch.test.core.db.BasicDefaultValues;
import nablarch.test.core.db.DbInfo;
import nablarch.test.core.db.DefaultValues;
import nablarch.test.core.db.TableData;
import nablarch.test.core.file.DataFile;
import nablarch.test.core.file.FixedLengthFile;
import nablarch.test.core.messaging.MessagePool;
import nablarch.test.core.util.interpreter.LineSeparatorInterpreter;
import nablarch.test.core.util.interpreter.NullInterpreter;
import nablarch.test.core.util.interpreter.QuotationTrimmer;
import nablarch.test.core.util.interpreter.TestDataInterpreter;
import nablarch.test.tool.converter.reader.GroupIdNotation;
import nablarch.test.tool.converter.reader.StubDbInfo;
import nablarch.test.tool.converter.xls.BlockHeader;
import nablarch.test.tool.converter.xls.MarkerOnlyBlock;
import nablarch.test.tool.converter.xls.MessageData;

/**
 * 本体の Excel 構造解析を再利用して、生の器を取り出すアクセサ。
 * <p>
 * 本体 Parser の取り出し口や一部コンストラクタがパッケージプライベートのため、本体と同じパッケージに置いている。
 * </p>
 * <p>
 * 値の意味を本体と揃えるため、本体の単体テストと同じインタープリタ列を使う。{@code ${...}} などの
 * 特殊記法を扱うインタープリタは渡さず、記法のまま運ぶ。デフォルト値補完などの後処理はしない。
 * </p>
 *
 * @author kiyobot
 */
public class XlsParserAccessor {

    /** 空の interpreters（生行を原文のまま取り出す用） */
    private static final List<TestDataInterpreter> EMPTY_INTERPRETERS = Collections.emptyList();

    /** テスティングフレームワークが単体テストで使うのと同順のインタープリタ列 */
    private static final List<TestDataInterpreter> INTERPRETERS = Collections.unmodifiableList(
            Arrays.<TestDataInterpreter>asList(new NullInterpreter(), new QuotationTrimmer(),
                                               new LineSeparatorInterpreter()));

    /** テストデータリーダ */
    private final TestDataReader reader;

    /** スタブの{@link DbInfo}（DB に接続しないため） */
    private final DbInfo dbInfo = new StubDbInfo();

    /** デフォルト値（{@link TableData}生成に必要なだけで、補完は実行しない） */
    private final DefaultValues defaultValues = new BasicDefaultValues();

    /**
     * コンストラクタ。
     *
     * @param reader テストデータリーダ
     */
    public XlsParserAccessor(TestDataReader reader) {
        this.reader = reader;
    }

    /**
     * テーブルデータを、デフォルト値補完などの後処理をせずに取り出す。
     *
     * @param path     取得元パス
     * @param resource 取得元リソース名
     * @param id       グループID（{@code [ ]} を付けない値。{@link GroupIdNotation}）
     * @param type     データタイプ（{@link DataType#SETUP_TABLE_DATA}／
     *                 {@link DataType#EXPECTED_TABLE_DATA}／{@link DataType#EXPECTED_COMPLETED}）
     * @return テーブルデータ一覧
     * @throws IllegalArgumentException データタイプがテーブル系でない場合
     */
    public List<TableData> readTables(String path, String resource, String id, DataType type) {
        switch (type) {
            case SETUP_TABLE_DATA:
            case EXPECTED_TABLE_DATA:
            case EXPECTED_COMPLETED:
                break;
            default:
                throw new IllegalArgumentException(
                        "unsupported data type for readTables. type=[" + type + "]");
        }
        TableDataParser parser = new TableDataParser(reader, INTERPRETERS, dbInfo, defaultValues, type);
        // 本体の静的キャッシュを汚染しない
        parser.parse(path, resource, GroupIdNotation.format(id), false);
        return parser.getResult();
    }

    /**
     * {@code List<Map<String, String>>}形式のデータを取り出す。
     *
     * @param path     取得元パス
     * @param resource 取得元リソース名
     * @param id       ID
     * @return 行データ一覧（キーはヘッダ行のカラム名）
     */
    public List<Map<String, String>> readListMap(String path, String resource, String id) {
        ListMapParser parser = new ListMapParser(reader, INTERPRETERS);
        parser.parse(path, resource, id, false); // 変換器経路: 本体静的キャッシュを汚染しない
        return parser.getResult();
    }

    /**
     * LIST_MAP ブロックのカラム名を記述順（マーカーカラム除外済み）で取得する。
     * {@link #readListMap} の結果はキーがアルファベット順になり、記述順が分からないため用いる。
     *
     * @param path     取得元パス
     * @param resource 取得元リソース名
     * @param id       識別子（{@code =}以降の識別子）
     * @return Excel 記述順のカラム名一覧（マーカーカラム除外済み）
     */
    public List<String> readListMapColumnNames(String path, String resource, String id) {
        // 1 行目がカラム名の行
        List<List<String>> bodyLines = readBlockBodyLines(path, resource, "", id, DataType.LIST_MAP);
        if (bodyLines.isEmpty()) {
            return Collections.emptyList();
        }
        HeaderLine header = new HeaderLine(bodyLines.get(0));
        return Arrays.asList(header.getEffectiveColumnNames());
    }

    /**
     * ファイル（固定長／可変長）を取り出す。
     *
     * @param path     取得元パス
     * @param resource 取得元リソース名
     * @param id       グループID（{@code [ ]} を付けない値）
     * @param type     データタイプ（{@link DataType#SETUP_FIXED}／{@link DataType#EXPECTED_FIXED}／
     *                 {@link DataType#SETUP_VARIABLE}／{@link DataType#EXPECTED_VARIABLE}）
     * @return ファイル一覧
     * @throws IllegalArgumentException データタイプがファイル系でない場合
     */
    public List<? extends DataFile> readFiles(String path, String resource, String id, DataType type) {
        DataFileParser<? extends DataFile> parser;
        switch (type) {
            case SETUP_FIXED:
            case EXPECTED_FIXED:
                parser = new FixedLengthFileParser(reader, INTERPRETERS, type);
                break;
            case SETUP_VARIABLE:
            case EXPECTED_VARIABLE:
                parser = new VariableLengthFileParser(reader, INTERPRETERS, type);
                break;
            default:
                throw new IllegalArgumentException(
                        "unsupported data type for readFiles. type=[" + type + "]");
        }
        // 本体の静的キャッシュを汚染しない
        parser.parse(path, resource, GroupIdNotation.format(id), false);
        return parser.getResult();
    }

    /**
     * メッセージ（{@link DataType#MESSAGE}）を取り出す。
     * 本文は {@link MessagePool#getSource()} が protected で読めないため、パーサから直接取り出す。
     *
     * @param path     取得元パス
     * @param resource 取得元リソース名
     * @param id       メッセージ ID（{@code =}以降の識別子）
     * @return メッセージ。対象が存在しない場合は{@code null}
     */
    public MessageData readMessage(String path, String resource, String id) {
        MessageParser parser = new MessageParser(reader, INTERPRETERS, DataType.MESSAGE);
        parser.parse(path, resource, id, false); // 変換器経路: 本体静的キャッシュを汚染しない
        List<FixedLengthFile> bodies = parser.getDelegate().getResult();
        if (bodies.isEmpty()) {
            return null;
        }
        return new MessageData(parser.getFwHeader(), bodies.get(0));
    }

    /**
     * 送信同期メッセージ（要求/応答電文 4 種）のうち、指定グループに属する全ブロックの本文を取り出す。
     *
     * @param path     取得元パス
     * @param resource 取得元リソース名
     * @param groupId  グループ ID（{@code [ ]} を付けない値）
     * @param type     データタイプ（送信系 4 種のいずれか）
     * @return 指定グループに属する本文（固定長ファイル）一覧（記述順。対象が無ければ空）
     */
    public List<FixedLengthFile> readSendSyncMessages(String path, String resource, String groupId, DataType type) {
        SendSyncBodyCollector collector = new SendSyncBodyCollector(reader, type);
        // 本体の静的キャッシュを汚染しない
        collector.parse(path, resource, GroupIdNotation.format(groupId), false);
        return collector.getResult();
    }

    /**
     * リソース内の全データブロックのヘッダ（データタイプ・グループ ID・識別子）を記述順に列挙する。
     * マーカー行の判定は本体の処理を使う。
     *
     * @param path     取得元パス
     * @param resource 取得元リソース名
     * @return ブロックヘッダ一覧（記述順。マーカー行が無ければ空）
     */
    public List<BlockHeader> readHeaders(String path, String resource) {
        HeaderCollector collector = new HeaderCollector(reader);
        collector.parse(path, resource, "", false); // 変換器経路: 本体静的キャッシュを汚染しない
        return collector.getResult();
    }

    /**
     * 指定したブロックの生のボディ行を、マーカー行を除いて記述順に取り出す。
     * <p>
     * 本体の器は長さ省略や型記法を正規化してしまうため、原文の復元に用いる。
     * 行末の空セルは本体と同じく除去する。
     * </p>
     *
     * @param path       取得元パス
     * @param resource   取得元リソース名
     * @param groupId    グループ ID（{@code [ ]} を付けない値。無指定は空文字）
     * @param identifier 識別子（ファイルパス／メッセージ ID 等）
     * @param type       データタイプ
     * @return 生のボディ行（記述順。マーカー行は含まない。対象ブロックが無ければ空）
     */
    public List<List<String>> readBlockBodyLines(String path, String resource, String groupId,
                                                 String identifier, DataType type) {
        BodyLineCollector collector =
                new BodyLineCollector(reader, EMPTY_INTERPRETERS, type, groupId, identifier, true);
        collector.parse(path, resource, "", false); // 変換器経路: 本体静的キャッシュを汚染しない
        return collector.getResult();
    }

    /**
     * カラムがマーカーカラムだけのブロックの版面（カラム名と各行の値）を取り出す。
     * <p>
     * マーカーカラムは本体の器に残らないため、行から直接取り出す。値は本体と同じインタープリタ列を通す
     * （本体はマーカーカラムにも同じ解釈を掛けるため）。
     * 行末の空セル除去は解釈後の {@code null} まで落とすため行わず、足りない位置を空文字で埋める。
     * </p>
     *
     * @param path       取得元パス
     * @param resource   取得元リソース名
     * @param groupId    グループ ID（{@code [ ]} を付けない値。無指定は空文字）
     * @param identifier 識別子（テーブル名／{@code LIST_MAP} の ID）
     * @param type       データタイプ（テーブル系／{@code LIST_MAP}）
     * @return 版面。対象ブロックが無い場合と、カラム名の行に有効なカラム名が 1 つでもある場合は {@code null}
     */
    public MarkerOnlyBlock readMarkerOnlyBlock(String path, String resource, String groupId,
                                               String identifier, DataType type) {
        BodyLineCollector collector =
                new BodyLineCollector(reader, INTERPRETERS, type, groupId, identifier, false);
        collector.parse(path, resource, "", false); // 変換器経路: 本体静的キャッシュを汚染しない
        List<List<String>> lines = collector.getResult();
        if (lines.isEmpty()) {
            return null;
        }
        List<String> columnNames = NablarchTestUtils.trimTailCopy(lines.get(0));
        if (columnNames.isEmpty() || new HeaderLine(columnNames).getEffectiveColumnNames().length > 0) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>(lines.size() - 1);
        for (int r = 1; r < lines.size(); r++) {
            List<String> line = lines.get(r);
            List<String> row = new ArrayList<>(columnNames.size());
            for (int c = 0; c < columnNames.size(); c++) {
                row.add(c < line.size() ? line.get(c) : "");
            }
            rows.add(row);
        }
        return new MarkerOnlyBlock(columnNames, rows);
    }

    /**
     * ブロックの識別子を、本体の器が持つ形へ揃える。
     * テーブル名は本体の {@link TableData} が trim して大文字化するため、同じ正規化を掛ける。
     *
     * @param type       データタイプ
     * @param identifier 識別子
     * @return 揃えた識別子
     */
    private static String normalizeIdentifier(DataType type, String identifier) {
        switch (type) {
            case SETUP_TABLE_DATA:
            case EXPECTED_TABLE_DATA:
            case EXPECTED_COMPLETED:
                return identifier.trim().toUpperCase();
            default:
                return identifier;
        }
    }

    /**
     * マーカー行の先頭セルから、{@code [ ]} を外したグループ ID（無指定は空文字）を切り出す
     * （{@link GroupIdNotation}）。
     * {@code =} を含まない場合はマーカー行でないとみなす。
     *
     * @param type      先頭セルから判定済みのデータタイプ（{@link DataType#DEFAULT} 以外）
     * @param firstCell マーカー行の先頭セル
     * @return グループ ID（無指定は空文字）。マーカー行でなければ {@code null}
     */
    private static String markerGroupId(DataType type, String firstCell) {
        String afterName = firstCell.substring(type.getName().length());
        int eq = afterName.indexOf('=');
        if (eq < 0) {
            return null;
        }
        String marker = afterName.substring(0, eq);
        int last = marker.length() - 1;
        if (last > 0 && marker.charAt(0) == '[' && marker.charAt(last) == ']') {
            return marker.substring(1, last);
        }
        return marker;
    }

    /**
     * 送信同期メッセージの本文（固定長ファイル）をグループ単位で収集する。
     * 本体の {@link GroupMessageParser} は結果を {@link MessagePool} に包み本文を取り出せないため、
     * 同じ構成で本文をそのまま返す。
     *
     * @see #readSendSyncMessages(String, String, String, DataType)
     */
    private static final class SendSyncBodyCollector extends GroupDataParsingTemplate<List<FixedLengthFile>> {

        /** 解析を委譲する送信同期メッセージパーサ */
        private final SendSyncMessageParser delegate;

        /**
         * コンストラクタ。
         *
         * @param reader     テストデータリーダ
         * @param targetType 対象データタイプ（送信系 4 種のいずれか）
         */
        SendSyncBodyCollector(TestDataReader reader, DataType targetType) {
            // セルを解釈するのはこのテンプレート側であり、委譲先ではない。
            super(reader, INTERPRETERS, targetType);
            delegate = new SendSyncMessageParser(reader, INTERPRETERS, targetType);
        }

        @Override
        void onReadLine(List<String> line) {
            delegate.onReadLine(line);
        }

        @Override
        void onTargetTypeFound(List<String> line) {
            delegate.onTargetTypeFound(line);
        }

        @Override
        List<FixedLengthFile> getResult() {
            return delegate.getDelegate().getResult();
        }
    }

    /**
     * リソース内のマーカー行を走査してブロックヘッダを収集する。ブロック本体は解析しない。
     */
    private static final class HeaderCollector extends TestDataParsingTemplate<List<BlockHeader>> {

        /** 収集したヘッダ（記述順） */
        private final List<BlockHeader> headers = new ArrayList<>();

        /**
         * コンストラクタ。
         *
         * @param reader テストデータリーダ
         */
        HeaderCollector(TestDataReader reader) {
            super(reader, EMPTY_INTERPRETERS, DataType.DEFAULT);
        }

        @Override
        void parse(String id) {
            List<String> line;
            while ((line = readLine()) != null) {
                String first = line.get(0);
                DataType type = getDataType(first);
                if (type == DataType.DEFAULT) {
                    continue;
                }
                String groupId = markerGroupId(type, first);
                if (groupId == null) {
                    // マーカー行でない
                    continue;
                }
                String identifier = getTypeValue(line);
                headers.add(new BlockHeader(type, groupId, identifier));
            }
        }

        // parse を上書きしているため、以下の抽象メソッドは使わない。
        @Override
        void onReadLine(List<String> line) {
            // ブロック本体は解析しない
        }

        @Override
        void onTargetTypeFound(List<String> line) {
            // 特定タイプを対象にしない
        }

        @Override
        boolean isTargetType(List<String> line, String id) {
            return false;
        }

        @Override
        boolean shouldStopOnNextOne() {
            return false;
        }

        @Override
        List<BlockHeader> getResult() {
            return headers;
        }
    }

    /**
     * 指定した 1 ブロックの生のボディ行を収集する。
     * <p>
     * {@link DataType#LIST_MAP} は本体 {@link ListMapParser} に合わせ、グループ ID を見ずに識別子だけで照合し、
     * 最初に見つけた 1 ブロックだけを読む。
     * </p>
     */
    private static final class BodyLineCollector extends TestDataParsingTemplate<List<List<String>>> {

        /** 対象データタイプ */
        private final DataType targetType;

        /** 対象グループ ID（{@code [ ]} を付けない値。無指定は空文字） */
        private final String targetGroupId;

        /** 対象識別子 */
        private final String targetIdentifier;

        /** 収集した生のボディ行（記述順） */
        private final List<List<String>> bodyLines = new ArrayList<>();

        /** 対象ブロックを読み終えたか（{@link DataType#LIST_MAP} は最初の 1 ブロックだけを読む） */
        private boolean finished = false;

        /** 対象ブロックを検出して収集中か */
        private boolean collecting = false;

        /** 行末の空セルを取り除くか */
        private final boolean trimTail;

        /**
         * コンストラクタ。
         *
         * @param reader           テストデータリーダ
         * @param interpreters     行のセルへ掛ける解釈クラス（原文で取り出すなら空）
         * @param targetType       対象データタイプ
         * @param targetGroupId    対象グループ ID（無指定は空文字）
         * @param targetIdentifier 対象識別子
         * @param trimTail         行末の空セルを取り除くなら真
         */
        BodyLineCollector(TestDataReader reader, List<TestDataInterpreter> interpreters, DataType targetType,
                          String targetGroupId, String targetIdentifier, boolean trimTail) {
            super(reader, interpreters, DataType.DEFAULT);
            this.targetType = targetType;
            this.targetGroupId = targetGroupId;
            this.targetIdentifier = normalizeIdentifier(targetType, targetIdentifier);
            this.trimTail = trimTail;
        }

        @Override
        void parse(String id) {
            List<String> line;
            while ((line = readLine()) != null) {
                String first = line.get(0);
                DataType type = getDataType(first);
                if (type != DataType.DEFAULT) {
                    String groupId = markerGroupId(type, first);
                    if (groupId != null) {
                        // 新しいブロックの開始
                        if (collecting && targetType == DataType.LIST_MAP) {
                            finished = true;
                        }
                        String identifier = normalizeIdentifier(targetType, getTypeValue(line));
                        collecting = !finished
                                && type == targetType
                                && (targetType == DataType.LIST_MAP || groupId.equals(targetGroupId))
                                && identifier.equals(targetIdentifier);
                        continue; // マーカー行自体はボディに含めない
                    }
                }
                if (collecting) {
                    bodyLines.add(trimTail ? NablarchTestUtils.trimTailCopy(line) : new ArrayList<>(line));
                }
            }
        }

        // parse を上書きしているため、以下の抽象メソッドは使わない。
        @Override
        void onReadLine(List<String> line) {
            // parse を上書きしているため未使用
        }

        @Override
        void onTargetTypeFound(List<String> line) {
            // 特定タイプを対象にしない
        }

        @Override
        boolean isTargetType(List<String> line, String id) {
            return false;
        }

        @Override
        boolean shouldStopOnNextOne() {
            return false;
        }

        @Override
        List<List<String>> getResult() {
            return bodyLines;
        }
    }
}
