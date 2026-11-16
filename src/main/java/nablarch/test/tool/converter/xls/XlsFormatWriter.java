package nablarch.test.tool.converter.xls;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import nablarch.test.tool.converter.reader.GroupIdNotation;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 中間モデル（{@link TestDataContainer}）を Excel（{@code .xlsx}）へ書き出す OUT ライタ。
 *
 * <p>
 * 本体パーサが読み戻せる版面で書くため、書いた Excel を {@link XlsFormatReader} で読み直すと
 * 同じ中間モデルへ戻る（色・書式・列幅は NTF 上の意味を持たないため対象外）。
 * 値は解釈後の値から Excel 記法へ戻して書く（{@link #toCellNotation}）。
 * </p>
 *
 * <p>
 * 1 コンテナ＝1 ブック（{@code <basePath>/<コンテナ名>.xlsx}）、1 セクション＝1 シートへ出力する。
 * 整形は {@link ExcelFormatConfig} に従う。
 * </p>
 *
 * @author kiyobot
 */
public final class XlsFormatWriter implements TestDataFormatWriter {

    /** 出力拡張子。 */
    private static final String EXTENSION = ".xlsx";

    /** {@code null} を表す Excel 記法。 */
    private static final String NULL_LITERAL = "null";

    /**
     * 空文字を表す Excel 記法。
     * 空セルだけの行は空行として読み飛ばされるため、全要素が空文字の行はこの記法で書く。
     */
    private static final String EMPTY_STRING_NOTATION = "\"\"";

    /** 本体がコメントとみなすセルの接頭辞。 */
    static final String COMMENT_PREFIX = "//";

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
            // basePath が空文字列だと親ディレクトリを持たず null になる。
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
        CellStyleCache styles = new CellStyleCache(workbook, config);
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
     * POI は超過分を黙って切り詰めるが、シート名はデータを引き当てるキーであり別名になると引けなくなる。
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
    private void writeSection(Sheet sheet, TestDataSection section, CellStyleCache styles) {
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
        // 到達しない。戻り値を返すため、および派生型が増えたときの検出のため残す。
        throw new IllegalArgumentException("unsupported block: " + block.getClass().getName());
    }

    /**
     * テーブル／LIST_MAP の版面を組み立てる（識別行 → カラム名行 → データ行）。
     * カラム名を持たないブロックには {@link #EMPTY_BLOCK_MARKER_COLUMN} を 1 つ書く。
     *
     * @param block カラム・行ブロック
     * @return 版面
     */
    private BlockLayout layoutColumnRow(ColumnRowDataBlock block) {
        BlockLayout l = new BlockLayout(block.getDataType(), block.getIdentifier());
        l.add(RowKind.META, Arrays.asList(marker(block)));
        List<String> columns = block.getColumnNames().isEmpty()
                ? Arrays.asList(EMPTY_BLOCK_MARKER_COLUMN)
                : block.getColumnNames();
        l.add(RowKind.HEADER, new ArrayList<>(columns));
        for (int c = 0; c < columns.size(); c++) {
            if (isMarkerColumn(columns.get(c))) {
                l.markMarkerColumn(c);
            }
        }
        for (List<String> row : block.getRows()) {
            l.add(RowKind.DATA, entryCells(row));
        }
        return l;
    }

    /**
     * ファイルの版面を組み立てる（識別行 → ディレクティブ → レコード群）。
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
     * Excel 記法ではレコード種別を書いた行から新しいレコードレイアウトになるため、2 レコード目以降で
     * レコード種別が空のものは書き表せない。YAML では書けるため、中間モデルではなくここで検査する。
     * </p>
     *
     * @param l          版面
     * @param records    レコードレイアウト群
     * @param fixed      固定長（長さ行を持つ）なら真
     * @param sendSync   送信系（データ行の列 0 に no を置く）なら真
     * @param identifier 識別子（診断メッセージ用）
     * @throws IllegalStateException 2 レコード目以降のレコード種別が空の場合
     */
    private void appendRecords(BlockLayout l, List<RecordLayout> records,
                               boolean fixed, boolean sendSync, String identifier) {
        for (int i = 0; i < records.size(); i++) {
            RecordLayout record = records.get(i);
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
            List<String> valueCells = literals(values);
            // 空セルだけの行は本体が捨てるため、先頭フィールドへ空文字記法を書く。
            if (!valueCells.isEmpty() && isAllBlank(valueCells)) {
                valueCells.set(0, EMPTY_STRING_NOTATION);
            }
            List<String> dataRow = new ArrayList<>(valueCells.size() + 1);
            dataRow.add(sendSync ? String.valueOf(seq++) : "");
            dataRow.addAll(valueCells);
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
            l.add(RowKind.DIRECTIVE,
                  Arrays.asList(entry.getKey(), toCellNotation(nullToEmpty(entry.getValue()))));
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
    private int render(Sheet sheet, int startRow, BlockLayout layout, CellStyleCache styles, WidthTracker widths) {
        int width = layout.width();
        int rowCount = layout.size();
        // 外枠罫線は META 行を除いた範囲に引く
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
            // 空セルを作ると隣列へのはみ出し表示が Excel に塞がれるため、値を持つセルのみ作成する。
            if (kind == RowKind.META) {
                for (int c = 0; c < cells.size(); c++) {
                    String value = cells.get(c);
                    Cell cell = row.createCell(c);
                    cell.setCellValue(value);
                    cell.setCellStyle(styles.getOrCreatePlain());
                    widths.observe(c, value.length());
                }
                continue;
            }
            boolean header = kind == RowKind.HEADER;
            boolean directive = kind == RowKind.DIRECTIVE;
            for (int c = 0; c < width; c++) {
                if (directive && c >= 2) {
                    continue;
                }
                String value = c < cells.size() ? cells.get(c) : "";
                Cell cell = row.createCell(c);
                cell.setCellValue(value);
                Fill fill;
                if (directive) {
                    fill = c == 0 ? layout.headerFill() : Fill.NONE;
                } else {
                    fill = layout.isMarkerColumn(c) ? Fill.MARKER
                            : (header ? layout.headerFill() : Fill.NONE);
                }
                cell.setCellStyle(styles.getOrCreate(r == firstBorderRow, r == lastBorderRow,
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
     *
     * @param block ブロック
     * @return 識別セル文字列
     */
    private static String marker(TestDataBlock block) {
        return block.getDataType().getName() + GroupIdNotation.format(block.getGroupId())
                + "=" + block.getIdentifier();
    }

    /**
     * カラム名を 1 件も持たないブロックのカラム名行へ書くマーカーカラム。
     * <p>
     * カラム名行は省略できない（省略すると次のブロックの識別子行がカラム名行として読まれる）ため、
     * 本体が除外するマーカーカラムを 1 つ置いて行だけを満たす。
     * </p>
     */
    static final String EMPTY_BLOCK_MARKER_COLUMN = "[EMPTY]";

    /**
     * マーカーカラム（{@code [...]} 形式）か判定する。
     *
     * @param columnName カラム名（{@code null} 不可）
     * @return マーカーカラムなら真
     */
    private static boolean isMarkerColumn(String columnName) {
        return columnName.startsWith("[") && columnName.endsWith("]");
    }

    /**
     * データ行の値を版面用に写す（{@link #toCellNotation} を各要素へ適用する）。
     *
     * @param row 値（{@code null} セルを含みうる）
     * @return 版面用の文字列リスト
     */
    private static List<String> literals(List<String> row) {
        List<String> result = new ArrayList<>(row.size());
        for (String value : row) {
            result.add(toCellNotation(value));
        }
        return result;
    }

    /**
     * テーブル・{@code LIST_MAP} のエントリを版面用に写す。全要素が空文字なら各セルへ空文字記法を書く。
     *
     * @param row エントリの値（{@code null} を含みうる）
     * @return 版面用の文字列リスト
     */
    private static List<String> entryCells(List<String> row) {
        List<String> cells = literals(row);
        if (isAllBlank(cells)) {
            Collections.fill(cells, EMPTY_STRING_NOTATION);
        }
        return cells;
    }

    /**
     * 版面用のセル文字列がすべて空文字かを判定する（空リストは真）。
     *
     * @param cells 版面用のセル文字列
     * @return すべて空文字なら真
     */
    private static boolean isAllBlank(List<String> cells) {
        for (String cell : cells) {
            if (!cell.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 解釈後の値（Java {@code null} または {@link String}）を Excel 記法のセル文字列へ戻す。
     *
     * <p>
     * 本体の解釈（{@code null} 記法・引用符の除去・CR 記法）の逆写像である。
     * 次の値は読み戻しで別の値に化けるため、半角ダブルクォートで囲む。
     * </p>
     * <ul>
     *   <li>{@code null}（大文字小文字不問）—— 囲まないと Java {@code null} になる</li>
     *   <li>前後が同じダブルクォートで囲まれた値 —— 囲まないと外側が外される</li>
     *   <li>{@code //} で始まる値 —— 囲まないとコメントとして読み飛ばされる</li>
     * </ul>
     *
     * @param value 解釈後の値
     * @return セル文字列
     */
    private static String toCellNotation(String value) {
        if (value == null) {
            return NULL_LITERAL;
        }
        String notation = value.replace("\r", "\\r");
        if (NULL_LITERAL.equalsIgnoreCase(notation) || isQuotationWrapped(notation)
                || notation.startsWith(COMMENT_PREFIX)) {
            return '"' + notation + '"';
        }
        return notation;
    }

    /**
     * 値の前後が同じダブルクォート（半角 {@code "} または全角 {@code ”}）で囲まれているかを判定する。
     *
     * <p>
     * 本体が引用符を外す条件と同じにしてある。1 文字の {@code "} も真になる（囲まずに書くと読み戻しで例外になる）。
     * </p>
     *
     * @param value 判定対象（{@code null} 不可）
     * @return 囲まれていれば真
     */
    private static boolean isQuotationWrapped(String value) {
        return (value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("”") && value.endsWith("”"));
    }

    /**
     * {@code null} を空文字へ写す。
     *
     * @param value 値
     * @return セル文字列
     */
    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

}
