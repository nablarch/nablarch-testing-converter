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
 * データ行の値は<b>解釈後の値から Excel 記法へ戻して</b>書く（{@link #toCellNotation}）。中間モデルが持つのは
 * テスティングフレームワークが解釈したあとの値（Java {@code null} または {@link String}）であり、
 * 読み（{@code XlsFormatReader}）で外した記法をここで戻すことで記法⇄値の写像を対称にする。
 * 戻すのは {@code null} リテラル・{@code \r}（CR）・引用符記法の 3 つで、
 * 値の途中のダブルクォート／2 文字の {@code \} ＋ {@code n}／LF は戻さない（記法の側でも素通しのため）。
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

    /** {@code null} を表す Excel 記法（本体 {@code NullInterpreter} が Java {@code null} へ解釈する表記）。 */
    private static final String NULL_LITERAL = "null";

    /**
     * 空文字を表す Excel 記法（半角ダブルクォート 2 文字。本体 {@code QuotationTrimmer} が空文字へ解釈する表記）。
     *
     * <p>
     * 全要素が空文字のエントリを空セルだけの行として書くと、読み戻しで空エントリとして読み飛ばされる。
     * 全フィールドが空文字のレコードは、いずれか 1 つのフィールドにこの記法を書いて表す。
     * </p>
     */
    private static final String EMPTY_STRING_NOTATION = "\"\"";

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
     * データを引き当てるためのキーであり、別名へ変わると引けなくなる。
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
        // sealed 階層が将来変更された場合のランタイム安全網。instanceof チェーンにはコンパイル時の網羅性保証がない。
        throw new IllegalArgumentException("unsupported block: " + block.getClass().getName());
    }

    /**
     * テーブル／LIST_MAP の版面を組み立てる（識別行 → カラム名行 → データ行）。
     * <p>
     * <b>カラム名を 1 件も持たない 0 件テーブルには、カラム名の行へマーカーカラム
     * {@value #EMPTY_BLOCK_MARKER_COLUMN} を 1 つだけ書く。</b>3 つの明文を同時に満たす唯一の書き方だからである
     * （{@code coverage/issues.md} <b>XLS-27</b>。採用は 2026-08-19。いずれも {@code 30a8271} 時点）。
     * </p>
     * <ul>
     *   <li>Excel 記法では、データ行を書かない場合でもカラム名の行を省略できない。
     *       識別子行の次の行がカラム名の行として読み込まれるため、カラム名の行を書かないと
     *       その次に現れた行がカラム名の行になる——<b>識別子行だけを書くことはできない。</b>
     *       {@code LIST_MAP} も 2 行目を Map のキー、3 行目以降を Map の値として読み込むため、
     *       キー行が構成上必須である</li>
     *   <li>YAML 記法では、カラム名は最初の行（{@code rows:} の先頭要素）のキーで決まり、
     *       0 件のデータは {@code rows:} に空配列 {@code []} を書く——
     *       <b>YAML の 0 件テーブルにはカラム名を書く場所が無い</b>ため、辺②は
     *       仕様適合入力からカラム名 0 件のブロックを正当に作る</li>
     *   <li>カラム名を半角角括弧 {@code [ ]} で囲むと、そのカラムはマーカーカラムとして
     *       読み込み対象から除外される…Excel 形式の {@code SETUP_TABLE}・{@code EXPECTED_TABLE}・
     *       {@code LIST_MAP}、YAML 形式の {@code setup_tables}・{@code expected_tables}・{@code list_maps}、
     *       いずれのデータタイプでも使える」——<b>意味を持たないカラムを 1 つ置く正当な書き方がある</b></li>
     * </ul>
     * <p>
     * <b>黙ってカラム名行を落とすと次のブロックを食う。</b>カラム名行を持たない版面を本体が読み戻すと、
     * 次のブロックの識別子行がカラム名の行として吸収され、そのブロックが丸ごと消える（実測。
     * {@code coverage/issues.md} <b>XLS-27</b>）。マーカーカラムは読み込み対象から除外されるため、
     * 読み戻すと {@code columnNames} ／ {@code rows} とも 0 件に戻る
     * （{@link XlsFormatReader} の XLS-08 の正規化。担保は
     * {@code XlsFormatWriterTest#roundTripsZeroRowTableWithoutEatingNextBlock}）。
     * </p>
     * <p>
     * <b>カラム名 0 件で行を持つブロックはここへ届かない。</b>どちらの記法にも書けない形であり、
     * {@link ColumnRowDataBlock} が<b>生成時点で拒否する</b>（{@code coverage/issues.md} <b>XLS-21</b>。
     * 番人の移設は 2026-08-19）。
     * </p>
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
            // カラム名の null は ColumnRowDataBlock が生成時に拒否するため、ここへは届かない
            // （ModelPreconditions#requireNoNulls）。isMarkerColumn の null 判定は防御として残してある。
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
     * <p>
     * <b>ファイル種別 {@code null} はここでは検査しない。</b>Excel 記法はファイルデータを固定長と
     * 可変長の 2 種類に尽くしており（固定長ファイルと可変長ファイルにはそれぞれ固有の制約があり、
     * 固定長は長さ行を持ち可変長は持たない）、
     * どちらとも決まっていないファイルデータブロックは Excel 記法に存在しない形である
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
     * 認めていない（電文が存在しない場合は<b>データブロックごと省略する</b>のが記法であり、レコード 0 件の
     * 電文を表す書き方は明文が無い。本体スキーマも {@code $defs.message_data} ／
     * {@code $defs.expected_request_message_data} ／ {@code $defs.group_message_data} が
     * {@code records.minItems} ＝ 1 とする）が、{@link MessageDataBlock} が<b>生成時点で拒否する</b>ため
     * ここへは届かない（{@code coverage/issues.md} <b>YML-12</b> の 2 形目。番人の移設は 2026-08-19）。
     * ファイルデータブロックのレコード 0 件は 0 バイトの空ファイルを表す<b>合法な形</b>であり
     * （スキーマも {@code $defs.file_data} だけが {@code records.minItems} ＝ 0）、こちらは拒否しない。
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
     * Excel 記法は、1 つのファイルデータブロック内に複数のレコードレイアウトを連続して書くと、
     * データの後ろに<b>新たなレコード種別とフィールド名称を書いた時点で</b>新しいレコードレイアウトとして
     * 扱われると定めている（複数レコードレイアウトの場合はレコード種別の記述を連続して書く）。
     * レコード種別を書かなければ新しいレコードレイアウトにならない。したがって 2 レコード目以降で
     * レコード種別が空（{@code null}／空文字）のレコードレイアウトは、<b>Excel 記法では書き表せない</b>。
     * 読み戻せない版面を黙って書かず、前提崩れとして即座に失敗させる
     * （{@link XlsFormatReader} の番人と同じ思想。{@code coverage/issues.md} <b>XLS-06</b>）。
     * </p>
     * <p>
     * <b>この番人は中間モデルへ寄せない。</b>YAML 記法にはこの制約が無く（{@code rows:} の
     * 各行は配列形式で、{@code fields:} と同じ順序・同じ件数で値を並べる。<b>先頭を空にするという Excel 形式の
     * 制約はない</b>）、本体スキーマ {@code $defs.record_fragment} の {@code required} も
     * {@code ["fields", "rows"]} だけで {@code record_type} を含まない。レコード種別を持たない 2 件目以降の
     * レコードレイアウトは辺④（YAML 書き出し）では正しく書けるため、中間モデルが保持できてよい形である。
     * 31 文字上限（{@link #requireValidSheetNameLength}）と同じく、Excel 形式固有の書き出し不能として
     * 書き出し側に残す。
     * </p>
     * <p>
     * <b>フィールド 0 件のレコードレイアウトはここでは検査しない。</b>Excel 記法に存在しない形であり
     * （フィールドが無いと名前行がレコード種別セル 1 個だけになり、
     * 本体 {@code DataFileParser} が名前行に 2 列以上を要求するため読み戻せない。
     * {@code coverage/issues.md} <b>XLS-22</b>）、{@link RecordLayout} が<b>生成時点で拒否する</b>ため
     * ここへは届かない（番人の移設は 2026-08-19）。
     * </p>
     * <p>
     * <b>データ型が {@code null} のフィールド定義もここでは検査しない。</b>Excel 記法はフィールド名称・
     * データ型のリストを必須としており（フィールド名称リストまたはデータ型リストが未指定または空であることを
     * 記述時のエラーに挙げる）、データ型を持たないフィールド定義は Excel 記法に存在しない形だが
     * （{@code coverage/issues.md} <b>YML-12</b> の 4 形目）、{@link FieldDef} が<b>生成時点で拒否する</b>
     * ためここへは届かない（番人の移設は §1-D）。
     * </p>
     * <p>
     * <b>フィールド長が {@code null} のフィールド定義もここでは検査しない。</b>Excel 記法は固定長ファイルに
     * ついてフィールド名称・データ型・フィールド長の 3 リストが同サイズで必須と定めており
     * （3 リストのサイズが一致していないことを記述時のエラーに挙げる）、
     * 電文のメッセージボディもフィールド名称・データ型・フィールド長・データという
     * ファイルデータと同じ構成を持つため、同じ制約に掛かる
     * （{@code coverage/issues.md} <b>XLS-30</b>）。文脈（固定長ファイルか・電文か・可変長ファイルか）は
     * ブロックが持っているため、{@link FileDataBlock}（{@code FIXED} のとき）と
     * {@link MessageDataBlock}（常に）が<b>生成時点で拒否する</b>。<b>可変長ファイルでは {@code null} が
     * 正しい</b>ため拒否しない（可変長ファイルではフィールド名称・データ型の 2 リストが
     * 同サイズで必須であり、フィールド長は不要である）。番人の移設は 2026-08-19。
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
            // 全フィールドが空文字のデータ行は、先頭フィールドへ空文字記法を書く
            // （いずれか 1 つのフィールドに "" と書く）。
            // 空セルだけの行にすると本体 PoiXlsReader#isBlankLine が行ごと捨てる。
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
     *
     * <p>
     * 中間モデルの {@code groupId} は<b>生値</b>（{@code g1}、省略時は空文字）である。半角角括弧は
     * Excel 形式の書式であって値ではないため、ここで付ける。外すのは
     * {@code TestCoreReaderAdapter#markerGroupId} であり、この 2 か所が Excel 版面の書式を知る層である。
     * </p>
     *
     * @param block ブロック
     * @return 識別セル文字列
     */
    private static String marker(TestDataBlock block) {
        return block.getDataType().getName() + markerGroupId(block.getGroupId())
                + "=" + block.getIdentifier();
    }

    /**
     * 生値のグループ ID を Excel 版面の書式（半角角括弧で囲む）へ写す。
     *
     * @param groupId 生値のグループ ID（非 null。省略時は空文字）
     * @return 版面用のグループ ID（{@code [g1]}／省略時は空文字）
     */
    private static String markerGroupId(String groupId) {
        return groupId.isEmpty() ? "" : "[" + groupId + "]";
    }

    /**
     * カラム名を 1 件も持たない 0 件テーブルのカラム名行へ書くマーカーカラム。
     *
     * <p>
     * 半角角括弧で囲んであるため、本体は読み込み対象から除外する。
     * カラム名の行そのものは記法により省略できないため、意味を持たないカラムを 1 つ置いて
     * 行の存在だけを満たす（{@code coverage/issues.md} <b>XLS-27</b>）。
     * </p>
     */
    static final String EMPTY_BLOCK_MARKER_COLUMN = "[EMPTY]";

    /**
     * マーカーカラム（{@code [...]} 形式）か判定する。
     *
     * @param columnName カラム名（{@code null} 不可。カラム名の {@code null} は
     *                   {@code ColumnRowDataBlock} の生成時に拒否されるため、ここへは届かない）
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
     * テーブル・{@code LIST_MAP} のエントリを版面用に写す。
     *
     * <p>
     * <b>全要素が空文字のエントリは各セルへ空文字記法（{@code ""}）を書く。</b>空セルだけの行にすると、
     * 読み戻しで空エントリとして読み飛ばされてエントリが 1 件消える
     * 。
     * 一部の要素だけが空文字のエントリは空セルのまま書く（行として空にならないため）。
     * </p>
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
     * 版面用のセル文字列がすべて空文字かを判定する。
     *
     * <p>要素を 1 つも持たないリストは真を返す。</p>
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
     * {@code XlsFormatReader} がデータ行のセルへ掛ける 3 つのインタープリタ（{@code NullInterpreter} →
     * {@code QuotationTrimmer} → {@code LineSeparatorInterpreter}）の逆写像である。値ごとに次の順で判定する。
     * </p>
     * <ul>
     *   <li>Java {@code null} —— {@code null} リテラルを書く（{@code NullInterpreter} が戻す）</li>
     *   <li>i 値の中の CR —— 2 文字の {@code \} ＋ {@code r} へ戻す
     *       （{@code LineSeparatorInterpreter} が戻す）</li>
     *   <li>ii 半角 {@code null}（大文字小文字不問） —— 半角ダブルクォートで囲む。囲まないと読み戻しで
     *       {@code NullInterpreter} が Java {@code null} にしてしまう
     *       </li>
     *   <li>iii 前後が同じダブルクォート（半角 {@code "} または全角 {@code ”}）で囲まれた値 ——
     *       半角ダブルクォートでさらに囲む。囲まないと読み戻しで {@code QuotationTrimmer} が外側を外して
     *       しまう</li>
     * </ul>
     *
     * <p>
     * ii と iii は排他である（{@code null} は引用符で始まらない）。i は ii・iii の判定に影響しない
     * （CR を {@code \} ＋ {@code r} へ置換しても値の先頭・末尾に引用符は生まれない）。
     * </p>
     *
     * <p>
     * <b>戻さないもの</b>: 値の途中のダブルクォート（{@code QuotationTrimmer} は外側 1 層しか外さない）／
     * 2 文字の {@code \} ＋ {@code n}／LF（どちらも {@code LineSeparatorInterpreter} の既定パターンの
     * 対象外）。いずれも記法の側で素通しされるため、戻す必要が無い。
     * </p>
     *
     * @param value 解釈後の値
     * @return セル文字列
     */
    private static String toCellNotation(String value) {
        if (value == null) {
            return NULL_LITERAL;
        }
        String notation = value.replace("\r", "\\r");
        if (NULL_LITERAL.equalsIgnoreCase(notation) || isQuotationWrapped(notation)) {
            return '"' + notation + '"';
        }
        return notation;
    }

    /**
     * 値の前後が同じダブルクォート（半角 {@code "} または全角 {@code ”}）で囲まれているかを判定する。
     *
     * <p>
     * 本体の {@code QuotationTrimmer} が外側 1 層を外す条件そのものである
     * （{@code nablarch-testing} の {@code QuotationTrimmer}）。1 文字の {@code "} も
     * 「前後が {@code "}」に当たるため真になる（この 1 文字を囲まずに書くと、読み戻しで
     * {@code substring(1, 0)} により例外になる）。
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
     * <p>
     * <b>実際に {@code null} が来るのはレコード種別だけである</b>（{@code RecordLayout} が
     * 「省略時は {@code null}」と定めているため）。データ型・フィールド長・ディレクティブ値の呼び出しは
     * 中間モデルの生成時に {@code null} が拒否済みで到達しない（{@code FieldDef} ／
     * {@code FileDataBlock} ／ {@code MessageDataBlock}）。防御として残してある。
     * </p>
     *
     * @param value 値
     * @return セル文字列
     */
    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

}
