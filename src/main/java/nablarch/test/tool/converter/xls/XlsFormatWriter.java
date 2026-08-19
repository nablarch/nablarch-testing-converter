package nablarch.test.tool.converter.xls;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nablarch.test.core.reader.DataType;
import nablarch.test.tool.converter.TestDataFormatWriter;
import nablarch.test.tool.converter.model.ColumnRowDataBlock;
import nablarch.test.tool.converter.model.FieldDef;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.MessageDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 中間モデル（{@link TestDataContainer}）を Excel（{@code .xlsx}）へ書き出す OUT ライタ。
 *
 * <p>
 * {@link XlsFormatReader} と<b>版面対称</b>に直列化する。Reader が委譲する本体パーサ
 * （{@code nablarch.test.core.reader}）が読み戻せる版面で書くため、書いた Excel を Reader で読み直すと
 * 同じ中間モデルへ戻る（往復。色・書式・列幅といった整形は NTF 仕様上の意味を持たないため可逆性の対象外）。
 * </p>
 *
 * <p>各データタイプの版面（本体パーサが要求する行/列の配置）は次のとおり。</p>
 * <ul>
 *   <li><b>TABLE／LIST_MAP</b>: 識別行（{@code TYPE[group]=id}）→ カラム名行 → データ行。
 *       いずれも列 0 始まり。マーカーカラム（{@code [...]}）は読み戻し時に本体が除外する。</li>
 *   <li><b>FILE</b>: 識別行 → ディレクティブ行（{@code key, value}）→ レコードごとに
 *       名前行（{@code レコード種別, フィールド名…}）・型行（{@code "", 型…}）・
 *       長さ行（固定長のみ {@code "", 長さ…}）・データ行（{@code "", 値…}）。
 *       本体は「列 0 が空＝データ行 / 非空＝新レコードの名前行」で判別するため、2 レコード目以降の
 *       名前行（レコード種別）は非空である必要がある。</li>
 *   <li><b>MESSAGE</b>: 識別行 → ディレクティブ行 → FW 制御ヘッダ行（{@code key, value}）→ 本文レコード
 *       （FILE と同型・本文は固定長）。本文データ行の列 0 は空。</li>
 *   <li><b>送信系 4 種</b>: MESSAGE と同型だが FW 制御ヘッダは無く、データ行の列 0 は {@code no}（連番）。
 *       本体は列 0 を {@code no} 値として取り除く。</li>
 * </ul>
 *
 * <p>
 * 値は記法のまま書く（クォート付与・トリムはしない。本体読み込みは空インタープリタで原文を保つ）。
 * 明示的な {@code null} 値はセルに {@code null} リテラルを書く（NTF のテストデータ慣習。空インタープリタの
 * 読み戻しでは文字列 {@code "null"} として戻るため、{@code null}↔{@code null} は Excel 経路では復元されない）。
 * </p>
 *
 * <p>
 * 1 コンテナ＝1 ブック（{@code <basePath>/<コンテナ名>.xlsx}）、1 セクション＝1 シートへ出力する。
 * 整形は {@link ExcelFormatConfig}（既定を備え上書き可能）に従う。
 * </p>
 *
 * @author kiyobot
 */
public final class XlsFormatWriter implements TestDataFormatWriter {

    /** 出力拡張子。 */
    private static final String EXTENSION = ".xlsx";

    /** Excel がシート名に許す最大文字数。 */
    private static final int MAX_SHEET_NAME_LENGTH = 31;

    /** 整形設定。 */
    private final ExcelFormatConfig config;

    /**
     * 既定の整形設定（{@link ExcelFormatConfig#defaults()}）で構成する。
     */
    public XlsFormatWriter() {
        this(ExcelFormatConfig.defaults());
    }

    /**
     * 整形設定を指定して構成する。
     *
     * @param config 整形設定
     */
    public XlsFormatWriter(ExcelFormatConfig config) {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     * <p>
     * コンテナを {@code <basePath>/<コンテナ名>.xlsx}（各セクション＝1 シート）へ書き出す。
     * </p>
     */
    @Override
    public void write(TestDataContainer container, String basePath) {
        Path file = Paths.get(basePath, container.getName() + EXTENSION);
        try {
            Path parent = file.getParent();
            // basePath が空文字列の場合など、親ディレクトリを持たない相対パス（例: "foo.xlsx"）が生成されると getParent() は null を返すため、null チェックが必須。
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Workbook workbook = build(container);
            try (OutputStream out = Files.newOutputStream(file)) {
                workbook.write(out);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("failed to write Excel: " + file, e);
        }
    }

    /**
     * コンテナを Excel ブック（メモリ上）へ組み立てる。テスト容易化のためファイル出力と分離する。
     *
     * @param container 中間モデル
     * @return 組み立て済みブック
     */
    Workbook build(TestDataContainer container) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Styles styles = new Styles(workbook, config);
        for (TestDataSection section : container.getSections()) {
            requireValidSheetNameLength(section.getName());
            Sheet sheet = workbook.createSheet(section.getName());
            sheet.setDisplayGridlines(config.isDisplayGridlines());
            writeSection(sheet, section, styles);
        }
        return workbook;
    }

    /**
     * シート名の文字数が Excel の上限（31 文字）以内であることを検査する。
     * <p>
     * POI の {@code XSSFWorkbook#createSheet(String)} は 31 文字を超える名前を
     * {@code substring(0, 31)} で黙って切り詰めてから禁止文字を検査する。シート名は呼び出し側が
     * データを引き当てるためのキーであり（{@code testdata_notation.rst:590}）、別名へ変わると引けなくなる。
     * また切り詰めが先に走ることで、禁止文字が 32 文字目以降にある名前は禁止文字検査に到達しない。
     * どちらも {@code createSheet} の前に文字数を検査すれば閉じる（{@code issues.md} XLS-16）。
     * </p>
     * <p>
     * null は検査しない。{@link TestDataSection} が生成時に拒否するため、ここへは届かない
     * （{@code issues.md} XLS-33）。<b>31 文字上限は Excel 形式固有の制約であり中間モデルの
     * 不変条件ではないため、この番人は書き出し側に残す。</b>
     * </p>
     *
     * @param sheetName シート名（＝セクション名）
     * @throws IllegalArgumentException 31 文字を超える場合
     */
    private static void requireValidSheetNameLength(String sheetName) {
        if (sheetName.length() > MAX_SHEET_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "シート名が Excel の上限 " + MAX_SHEET_NAME_LENGTH + " 文字を超えています。"
                            + "切り詰めると別名になり読み込み単位を引き当てられなくなるため、変換を中止しました。"
                            + " sheetName='" + sheetName + "', length=" + sheetName.length());
        }
    }

    /**
     * 1 セクションを 1 シートへ書き出す。
     *
     * @param sheet   出力先シート
     * @param section セクション
     * @param styles  セルスタイルキャッシュ
     */
    private void writeSection(Sheet sheet, TestDataSection section, Styles styles) {
        WidthTracker widths = new WidthTracker(config.getMaxColumnWidthChars());
        int rowNum = 0;
        boolean first = true;
        for (TestDataBlock block : section.getBlocks()) {
            if (!first) {
                rowNum += config.getBlankRowsBetweenBlocks();
            }
            first = false;
            rowNum = render(sheet, rowNum, layout(block), styles, widths);
        }
        if (config.isAutoColumnWidth()) {
            widths.applyTo(sheet);
        }
    }

    // ------------------------------------------------------------------------
    // 版面（BlockLayout）の組み立て
    // ------------------------------------------------------------------------

    /**
     * ブロックを版面（行と種別の並び）へ写す。
     *
     * @param block ブロック
     * @return 版面
     */
    private BlockLayout layout(TestDataBlock block) {
        if (block instanceof ColumnRowDataBlock) {
            return layoutColumnRow((ColumnRowDataBlock) block);
        } else if (block instanceof FileDataBlock) {
            return layoutFile((FileDataBlock) block);
        } else if (block instanceof MessageDataBlock) {
            return layoutMessage((MessageDataBlock) block);
        }
        throw new IllegalArgumentException("unsupported block: " + block.getClass().getName());
    }

    /**
     * テーブル／LIST_MAP の版面を組み立てる（識別行 → カラム名行 → データ行）。
     * <p>
     * カラム名を 1 件も持たないブロックは書き出さずに弾く。Excel 記法はデータ行が無くても
     * カラム名の行を省略できないためである（{@code testdata_notation.rst:802}
     * 「データ行を書かない場合でも、カラム名の行は省略できない。識別子行の次の行がカラム名の行として
     * 読み込まれるため、カラム名の行を書かないと、その次に現れた行がカラム名の行になる」。
     * {@code LIST_MAP} も {@code :628}「2行目を Map のキー、3行目以降を Map の値として読み込む」の
     * とおりキー行が構成上必須である。いずれも {@code 30a8271} 時点）。
     * </p>
     * <p>
     * <b>黙って書くと次のブロックを食う。</b>カラム名行を持たない版面を本体が読み戻すと、次のブロックの
     * 識別子行がカラム名の行として吸収され、そのブロックが丸ごと消える（実測。
     * {@code coverage/issues.md} <b>XLS-27</b>）。
     * </p>
     * <p>
     * <b>これは当面の対応である。</b>本体（{@code nablarch-testing}）の {@code TableDataParser} が
     * 「識別子行の次の行が識別子行なら、カラム名 0 個の 0 件テーブルとみなす」と読めるようになったら、
     * ここは「識別子行だけを書く」実装へ切り替える（XLS-27 の 2 段構え）。
     * </p>
     *
     * @param block カラム・行ブロック
     * @return 版面
     * @throws IllegalArgumentException カラム名が 0 件の場合
     */
    private BlockLayout layoutColumnRow(ColumnRowDataBlock block) {
        if (block.getColumnNames().isEmpty()) {
            throw new IllegalArgumentException(
                    "カラム名を 1 件も持たないブロックは書き出せません。Excel 形式ではデータ行が無くても"
                    + "カラム名の行を省略できず、省略すると次のブロックの識別子行がカラム名の行として"
                    + "読み込まれてそのブロックが消えるため、変換を中止しました。"
                    + "ブロック=[" + marker(block) + "]");
        }
        BlockLayout l = new BlockLayout(block.getDataType(), block.getIdentifier());
        l.add(RowKind.META, Arrays.asList(marker(block)));
        List<String> columns = block.getColumnNames();
        l.add(RowKind.HEADER, new ArrayList<>(columns));
        for (int c = 0; c < columns.size(); c++) {
            // カラム名が null の場合は isMarkerColumn 内で null チェックして false を返す。null カラムは非マーカーとして扱う。
            if (isMarkerColumn(columns.get(c))) {
                l.markMarkerColumn(c);
            }
        }
        for (List<String> row : block.getRows()) {
            l.add(RowKind.DATA, literals(row));
        }
        return l;
    }

    /**
     * ファイルの版面を組み立てる（識別行 → ディレクティブ → レコード群）。
     * <p>
     * <b>ファイル種別 {@code null} はここでは検査しない。</b>Excel 記法はファイルデータを固定長と
     * 可変長の 2 種類に尽くしており（{@code testdata_notation.rst:883}（{@code 30a8271} 時点）
     * 「固定長ファイルと可変長ファイルには、それぞれ固有の記法制約がある」。固定長は長さ行を持ち、
     * 可変長は持たない）、どちらとも決まっていないファイルデータブロックは Excel 記法に存在しない形である
     * （{@code coverage/issues.md} <b>XLS-29</b>）。{@link FileDataBlock} が<b>生成時点で拒否する</b>ため
     * ここへは届かない（番人の移設は 2026-08-19）。
     * </p>
     *
     * @param block ファイルブロック
     * @return 版面
     */
    private BlockLayout layoutFile(FileDataBlock block) {
        BlockLayout l = new BlockLayout(block.getDataType(), block.getIdentifier());
        l.add(RowKind.META, Arrays.asList(marker(block)));
        appendKeyValueRows(l, block.getDirectives());
        boolean fixed = block.getFileType() == FileDataBlock.FileType.FIXED;
        appendRecords(l, block.getRecords(), fixed, false, block.getIdentifier());
        return l;
    }

    /**
     * メッセージ（MESSAGE／送信系）の版面を組み立てる
     * （識別行 → ディレクティブ → FW 制御ヘッダ → 本文レコード群）。
     * <p>
     * <b>本文レコード 0 件はここでは検査しない。</b>Excel 記法・YAML 記法のいずれも本文レコード 0 件の電文を
     * 認めていない（電文が存在しない場合は {@code testdata_notation.rst:1257}
     * （{@code 30a8271} 時点）のとおり<b>データブロックごと省略する</b>のが記法であり、レコード 0 件の
     * 電文を表す書き方は明文が無い。本体スキーマも {@code $defs.message_data} ／
     * {@code $defs.expected_request_message_data} ／ {@code $defs.group_message_data} が
     * {@code records.minItems} ＝ 1 とする）が、{@link MessageDataBlock} が<b>生成時点で拒否する</b>ため
     * ここへは届かない（{@code coverage/issues.md} <b>YML-12</b> の 2 形目。番人の移設は 2026-08-19）。
     * ファイルデータブロックのレコード 0 件は 0 バイトの空ファイルを表す<b>合法な形</b>であり
     * （{@code testdata_notation.rst:881}／{@code :1109}／{@code :1146}。スキーマも
     * {@code $defs.file_data} だけが {@code records.minItems} ＝ 0）、こちらは拒否しない。
     * </p>
     *
     * @param block メッセージブロック
     * @return 版面
     */
    private BlockLayout layoutMessage(MessageDataBlock block) {
        BlockLayout l = new BlockLayout(block.getDataType(), block.getIdentifier());
        l.add(RowKind.META, Arrays.asList(marker(block)));
        appendKeyValueRows(l, block.getDirectives());
        appendKeyValueRows(l, block.getFwHeaderFields());
        boolean sendSync = XlsDataTypeUtil.isSendSyncType(block.getDataType());
        // 本文は固定長（長さ行を持つ）。送信系はデータ行の列 0 に no（連番）を置く。
        appendRecords(l, block.getRecords(), true, sendSync, block.getIdentifier());
        return l;
    }

    /**
     * 複数レコードレイアウトを版面へ追加する。
     * <p>
     * 本体パーサは「名前行の列 0（レコード種別）が空＝直前レコードのデータ行 / 非空＝新レコードの名前行」で
     * 判別するため、2 レコード目以降でレコード種別が空（{@code null}／空文字）だと、その名前行が直前レコードの
     * データ行と誤読され版面対称性が崩れる（書いたものを読み戻せない）。読み戻せない版面を黙って書かず、
     * 前提崩れとして即座に失敗させる（{@link XlsFormatReader} の番人と同じ思想）。
     * </p>
     * <p>
     * <b>フィールド 0 件のレコードレイアウトはここでは検査しない。</b>Excel 記法に存在しない形であり
     * （{@code testdata_notation.rst:888}（{@code 30a8271} 時点）。フィールドが無いと名前行がレコード種別
     * セル 1 個だけになり、本体 {@code DataFileParser} が名前行に 2 列以上を要求するため読み戻せない。
     * {@code coverage/issues.md} <b>XLS-22</b>）、{@link RecordLayout} が<b>生成時点で拒否する</b>ため
     * ここへは届かない（番人の移設は 2026-08-19）。
     * </p>
     * <p>
     * データ型が {@code null} のフィールド定義も同じ思想で弾く。Excel 記法はフィールド名称・データ型の
     * リストを必須としており（{@code testdata_notation.rst:883}（{@code 30a8271} 時点）。
     * {@code :888} は「フィールド名称リストまたはデータ型リストが未指定または空である」を記述時のエラーに
     * 挙げる）、データ型を持たないフィールド定義は Excel 記法に存在しない形だからである
     * （{@code coverage/issues.md} <b>YML-12</b> の 4 形目）。{@link FieldDef} の契約としても
     * {@code type} は必須である。弾くのは {@code null} だけで、空文字は弾かない。
     * </p>
     * <p>
     * フィールド長が {@code null} のフィールド定義は、<b>長さ行を持つ版面（{@code fixed} が真）に限り</b>弾く。
     * Excel 記法は固定長ファイルについて「フィールド名称・データ型・フィールド長の3リストが同サイズで必須」と
     * 定めており（{@code testdata_notation.rst:883}（{@code 30a8271} 時点）。{@code :889} は
     * 「フィールド名称・データ型・フィールド長リストのサイズが一致していない」を記述時のエラーに挙げる）、
     * 長さを持たないフィールド定義は Excel 記法に存在しない形だからである
     * （{@code coverage/issues.md} <b>XLS-30</b>）。判定せずに書くと長さセルだけが空の長さ行になる。
     * 電文も同じ制約に掛かる——{@code :1158}「フレームワーク制御ヘッダ以降のメッセージボディは、
     * フィールド名称・データ型・フィールド長・データという、前述のファイルデータと同じ構成を持つ」であり、
     * {@link #layoutMessage} は {@code fixed} に常に真を渡す。
     * <b>可変長ファイルでは {@code null} が正しい</b>ため弾かない（{@code :883}「可変長ファイルでは、
     * フィールド名称・データ型の2リストが同サイズで必須であり、フィールド長は不要である」）。
     * {@link FieldDef} の契約も「固定長ファイル・電文では必須、可変長ファイルでは省略可」という条件つきである。
     * 弾くのは {@code null} だけで、空文字は弾かない（{@code type} の番人と同じ境界）。
     * </p>
     *
     * @param l          版面
     * @param records    レコードレイアウト群
     * @param fixed      固定長（長さ行を持つ）なら真
     * @param sendSync   送信系（データ行の列 0 に no を置く）なら真
     * @param identifier 識別子（診断メッセージ用）
     * @throws IllegalArgumentException {@code fixed} が真でフィールド長が {@code null} の
     *                                  フィールド定義が含まれる場合
     * @throws IllegalStateException    2 レコード目以降のレコード種別が空の場合
     */
    private void appendRecords(BlockLayout l, List<RecordLayout> records,
                               boolean fixed, boolean sendSync, String identifier) {
        for (int i = 0; i < records.size(); i++) {
            RecordLayout record = records.get(i);
            for (FieldDef field : record.getFields()) {
                // 名称・データ型の null は FieldDef の生成時に拒否済みのため、ここでは検査しない
                if (fixed && field.getLength() == null) {
                    throw new IllegalArgumentException(
                            "固定長ファイル・電文でフィールド長を持たないフィールド定義は書き出せません"
                                    + "（Excel 記法はフィールド名称・データ型・フィールド長の 3 リストが"
                                    + "同サイズであることを求めており、長さセルだけが空の長さ行になります）。"
                                    + " identifier=[" + identifier + "] レコード番号=" + i
                                    + " フィールド名=[" + field.getName() + "]");
                }
            }
            String recordType = record.getRecordType();
            if (i > 0 && (recordType == null || recordType.isEmpty())) {
                throw new IllegalStateException(
                        "2 レコード目以降のレコード種別は省略できません"
                                + "（列 0 が空だと本体パーサが直前レコードのデータ行と誤読します）。"
                                + " identifier=[" + identifier + "] レコード番号=" + i);
            }
            appendRecord(l, record, fixed, sendSync);
        }
    }

    /**
     * 1 レコードレイアウト（名前行・型行・長さ行・データ行）を版面へ追加する。
     *
     * @param l        版面
     * @param record   レコードレイアウト
     * @param fixed    固定長（長さ行を持つ）なら真
     * @param sendSync 送信系（データ行の列 0 に no を置く）なら真
     */
    private void appendRecord(BlockLayout l, RecordLayout record, boolean fixed, boolean sendSync) {
        List<FieldDef> fields = record.getFields();

        List<String> nameRow = new ArrayList<>();
        nameRow.add(nullToEmpty(record.getRecordType()));
        for (FieldDef field : fields) {
            nameRow.add(field.getName());
        }
        l.add(RowKind.HEADER, nameRow);

        List<String> typeRow = new ArrayList<>();
        typeRow.add("");
        for (FieldDef field : fields) {
            typeRow.add(nullToEmpty(field.getType()));
        }
        l.add(RowKind.HEADER, typeRow);

        if (fixed) {
            List<String> lengthRow = new ArrayList<>();
            lengthRow.add("");
            for (FieldDef field : fields) {
                lengthRow.add(nullToEmpty(field.getLength()));
            }
            l.add(RowKind.HEADER, lengthRow);
        }

        int seq = 1;
        for (List<String> values : record.getRows()) {
            List<String> dataRow = new ArrayList<>();
            dataRow.add(sendSync ? String.valueOf(seq++) : "");
            for (String value : values) {
                dataRow.add(nullToLiteral(value));
            }
            l.add(RowKind.DATA, dataRow);
        }
    }

    /**
     * {@code key, value} 形式のメタ行（ディレクティブ／FW 制御ヘッダ）を版面へ追加する。
     *
     * @param l   版面
     * @param map キー → 値（記述順）
     */
    private void appendKeyValueRows(BlockLayout l, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            l.add(RowKind.DIRECTIVE, Arrays.asList(entry.getKey(), nullToEmpty(entry.getValue())));
        }
    }

    // ------------------------------------------------------------------------
    // 版面 → シート（セル生成・スタイル・列幅）
    // ------------------------------------------------------------------------

    /**
     * 版面をシートへ描画する。ブロックを矩形に整え（不足セルは空文字で補填）、外枠罫線・背景色・列幅を付す。
     *
     * @param sheet    出力先シート
     * @param startRow 開始行
     * @param layout   版面
     * @param styles   セルスタイルキャッシュ
     * @param widths   列幅トラッカ
     * @return 次に書き込むべき行番号
     */
    private int render(Sheet sheet, int startRow, BlockLayout layout, Styles styles, WidthTracker widths) {
        int width = layout.width();
        int rowCount = layout.size();
        // 外枠罫線の上辺・下辺は HEADER/DIRECTIVE/DATA 行の中での最初・最後で引く
        int firstBorderRow = -1;
        int lastBorderRow = -1;
        for (int r = 0; r < rowCount; r++) {
            if (layout.kind(r) != RowKind.META) {
                if (firstBorderRow < 0) {
                    firstBorderRow = r;
                }
                lastBorderRow = r;
            }
        }
        for (int r = 0; r < rowCount; r++) {
            Row row = sheet.createRow(startRow + r);
            List<String> cells = layout.row(r);
            RowKind kind = layout.kind(r);
            // META 行（識別行）は罫線なし・背景色なし。空セルを作ると隣列へのテキストオーバーフローが
            // Excel に塞がれるため、値を持つセルのみ作成する。
            if (kind == RowKind.META) {
                for (int c = 0; c < cells.size(); c++) {
                    String value = cells.get(c);
                    row.createCell(c).setCellValue(value);
                    widths.observe(c, value.length());
                }
                continue;
            }
            boolean header = kind == RowKind.HEADER;
            boolean directive = kind == RowKind.DIRECTIVE;
            for (int c = 0; c < width; c++) {
                // ディレクティブ行の3列目以降（キー・値以外）はセル不要
                if (directive && c >= 2) {
                    continue;
                }
                String value = c < cells.size() ? cells.get(c) : "";
                Cell cell = row.createCell(c);
                cell.setCellValue(value);
                Fill fill;
                if (directive) {
                    // ディレクティブ行：左列（キー）にヘッダ色、右列（値）は背景なし
                    fill = c == 0 ? layout.headerFill() : Fill.NONE;
                } else {
                    fill = layout.isMarkerColumn(c) ? Fill.MARKER
                            : (header ? layout.headerFill() : Fill.NONE);
                }
                cell.setCellStyle(styles.get(r == firstBorderRow, r == lastBorderRow,
                        c == 0, c == width - 1, fill));
                widths.observe(c, value.length());
            }
        }
        return startRow + rowCount;
    }

    // ------------------------------------------------------------------------
    // 値・カラムのヘルパ
    // ------------------------------------------------------------------------

    /**
     * ブロックの識別セル文字列（{@code TYPE[group]=identifier}）を生成する。
     * グループ ID は中間モデルが整形済み（{@code [g1]} もしくは空文字）で保持するためそのまま連結する。
     *
     * @param block ブロック
     * @return 識別セル文字列
     */
    private static String marker(TestDataBlock block) {
        return block.getDataType().getName() + block.getGroupId() + "=" + block.getIdentifier();
    }

    /**
     * マーカーカラム（{@code [...]} 形式）か判定する。
     *
     * @param columnName カラム名
     * @return マーカーカラムなら真
     */
    private static boolean isMarkerColumn(String columnName) {
        return columnName != null && columnName.startsWith("[") && columnName.endsWith("]");
    }

    /**
     * データ行の値を版面用に写す（{@code null} はリテラル {@code null}、それ以外は記法のまま）。
     *
     * @param row 値（{@code null} セルを含みうる）
     * @return 版面用の文字列リスト
     */
    private static List<String> literals(List<String> row) {
        List<String> result = new ArrayList<>(row.size());
        for (String value : row) {
            result.add(nullToLiteral(value));
        }
        return result;
    }

    /**
     * {@code null} をリテラル {@code null} へ、それ以外は記法のまま返す（データ行の値用）。
     *
     * @param value 値
     * @return セル文字列
     */
    private static String nullToLiteral(String value) {
        return value == null ? "null" : value;
    }

    /**
     * {@code null} を空文字へ、それ以外は記法のまま返す（メタ・型・長さ等、省略を空セルで表す箇所用）。
     *
     * @param value 値
     * @return セル文字列
     */
    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

}
