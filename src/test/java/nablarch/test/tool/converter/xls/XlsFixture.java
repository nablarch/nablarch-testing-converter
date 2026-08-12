package nablarch.test.tool.converter.xls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 実 {@code .xlsx} を POI で組み立てるテストフィクスチャビルダ。
 *
 * <p>
 * 既存のテストヘルパ（{@code XlsFormatWriterTest} ／ {@code RoundTripTest} が持つ private static の
 * {@code container} / {@code table} / {@code row} など）は<b>中間モデル</b>を組み立て、Excel が要るときは
 * {@link XlsFormatWriter} に書かせる。{@link XlsFormatWriter} はすべての値を文字列セルとして書くため、
 * 数値セル・日付書式セル・数式セル・真偽値セル・エラーセル・表示形式といった<b>セル種別</b>を表現できない。
 * 本クラスはその表現できない軸だけを受け持つ。中間モデル組み立てヘルパとは対象レイヤが異なるため
 * 重複しない。
 * </p>
 *
 * <p>
 * 使い方（{@link #sheet} でシートを開き、{@link #row} で行を上から順に追加する。{@link #sheet} は
 * 続けて呼べ、呼ぶたびにシートが末尾に追加されて以降の {@link #row} の出力先が切り替わる）:
 * </p>
 * <pre>{@code
 * XlsFixture.book("myBook").sheet("mySheet")
 *         .row(text("SETUP_TABLE=USERS"))
 *         .row(text("USER_ID"), text("AGE"))
 *         .row(text("U1"), number(1, "@"))
 *         .writeTo(dir);
 * }</pre>
 *
 * <p>
 * セルは {@link CellSpec} で指定する。ファクトリは本クラスの static メソッド
 * （{@link #text} / {@link #number} / {@link #date} / {@link #formula} / {@link #bool} /
 * {@link #error} / {@link #blank} / {@link #absent}）で、テスト側から static import して使う。
 * </p>
 *
 * <p>
 * POI は 3.8 を使う。{@code CellType} enum は POI 4 以降のため、本クラスは 3.8 で利用できる API のみを使う。
 * </p>
 *
 * @author kiyobot
 */
final class XlsFixture {

    /** 出力拡張子。{@code PoiXlsReader} は {@code .xls} → {@code .xlsx} の順に探す。 */
    private static final String EXTENSION = ".xlsx";

    /** ブック名（出力ファイル名から拡張子を除いたもの）。 */
    private final String bookName;

    /** 組み立て中のブック。 */
    private final Workbook workbook = new XSSFWorkbook();

    /** 表示形式文字列 → セルスタイルのキャッシュ（同じ書式で毎回スタイルを作らない）。 */
    private final Map<String, CellStyle> styles = new HashMap<>();

    /** 現在の出力先シート。 */
    private Sheet sheet;

    /** 次に {@link #row} が作る行のインデックス。 */
    private int nextRow;

    /**
     * コンストラクタ。
     *
     * @param bookName ブック名
     */
    private XlsFixture(String bookName) {
        this.bookName = bookName;
    }

    // ------------------------------------------------------------------ 構築

    /**
     * ブックの組み立てを開始する。
     *
     * @param bookName ブック名（出力は {@code <bookName>.xlsx}）
     * @return 自身
     */
    static XlsFixture book(String bookName) {
        return new XlsFixture(bookName);
    }

    /**
     * シートを追加し、以降の {@link #row} の出力先にする。
     *
     * @param sheetName シート名
     * @return 自身
     */
    XlsFixture sheet(String sheetName) {
        sheet = workbook.createSheet(sheetName);
        nextRow = 0;
        return this;
    }

    /**
     * 現在のシートへ 1 行追加する。
     *
     * @param cells 左から順のセル指定（{@link #absent()} の位置にはセルを作らない）
     * @return 自身
     * @throws IllegalStateException {@link #sheet} を呼ぶ前に呼び出した場合
     */
    XlsFixture row(CellSpec... cells) {
        if (sheet == null) {
            throw new IllegalStateException("call sheet() first");
        }
        Row row = sheet.createRow(nextRow++);
        for (int i = 0; i < cells.length; i++) {
            CellSpec spec = cells[i];
            if (spec.isAbsent()) {
                continue;
            }
            spec.write(this, row.createCell(i));
        }
        return this;
    }

    // ------------------------------------------------------------------ 出力

    /**
     * ブックを {@code <dir>/<bookName>.xlsx} へ書き出す。
     *
     * @param dir 出力先ディレクトリ
     * @return 書き出したファイルのパス
     */
    Path writeTo(Path dir) {
        Path file = dir.resolve(bookName + EXTENSION);
        try {
            Files.createDirectories(dir);
            try (OutputStream out = Files.newOutputStream(file)) {
                workbook.write(out);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("failed to write fixture: " + file, e);
        }
        return file;
    }

    /**
     * {@code .xlsx} を POI で開く。
     *
     * @param file ファイル
     * @return ブック
     */
    static Workbook open(Path file) {
        // POI 3.8 の WorkbookFactory.create(InputStream) が投げる検査例外は
        // IOException と InvalidFormatException の 2 つだけ（javap で確認済み）。
        try (InputStream in = Files.newInputStream(file)) {
            return WorkbookFactory.create(in);
        } catch (IOException | InvalidFormatException e) {
            throw new IllegalStateException("failed to open workbook: " + file, e);
        }
    }

    // ------------------------------------------------------------------ セル指定

    /**
     * 表示形式（{@code @}・{@code yyyy/mm/dd} 等）に対応するセルスタイルを返す。同じ書式は使い回す。
     *
     * @param displayFormat 表示形式
     * @return セルスタイル
     */
    private CellStyle styleFor(String displayFormat) {
        CellStyle style = styles.get(displayFormat);
        if (style == null) {
            style = workbook.createCellStyle();
            style.setDataFormat(workbook.createDataFormat().getFormat(displayFormat));
            styles.put(displayFormat, style);
        }
        return style;
    }

    /**
     * 1 セルの内容（セル種別・値・表示形式）の指定。
     */
    abstract static class CellSpec {

        /**
         * セルを作らない（セル不在）指定なら真。
         *
         * @return セル不在なら真
         */
        boolean isAbsent() {
            return false;
        }

        /**
         * セルへ値と表示形式を書き込む。
         *
         * @param fixture 所属フィクスチャ（表示形式スタイルの生成に使う）
         * @param cell    書き込み先セル
         */
        abstract void write(XlsFixture fixture, Cell cell);
    }

    /**
     * 文字列セル。
     *
     * @param value 値
     * @return セル指定
     */
    static CellSpec text(final String value) {
        return new CellSpec() {
            @Override
            void write(XlsFixture fixture, Cell cell) {
                cell.setCellValue(value);
            }
        };
    }

    /**
     * 数値セル（表示形式なし＝標準）。
     *
     * @param value 値
     * @return セル指定
     */
    static CellSpec number(final double value) {
        return new CellSpec() {
            @Override
            void write(XlsFixture fixture, Cell cell) {
                cell.setCellValue(value);
            }
        };
    }

    /**
     * 表示形式付きの数値セル。
     *
     * @param value         値
     * @param displayFormat 表示形式（例: {@code "@"}（テキスト）・{@code "0.00"}）
     * @return セル指定
     */
    static CellSpec number(final double value, final String displayFormat) {
        return new CellSpec() {
            @Override
            void write(XlsFixture fixture, Cell cell) {
                cell.setCellValue(value);
                cell.setCellStyle(fixture.styleFor(displayFormat));
            }
        };
    }

    /**
     * 日付・時刻・日時セル（Excel では表示形式付きの数値セル）。
     *
     * @param value         値
     * @param displayFormat 表示形式（例: {@code "yyyy/mm/dd"}・{@code "hh:mm:ss"}）
     * @return セル指定
     */
    static CellSpec date(final Date value, final String displayFormat) {
        return new CellSpec() {
            @Override
            void write(XlsFixture fixture, Cell cell) {
                cell.setCellValue(value);
                cell.setCellStyle(fixture.styleFor(displayFormat));
            }
        };
    }

    /**
     * 数式セル。
     *
     * @param expression 数式（先頭の {@code =} は付けない。例: {@code "1+1"}）
     * @return セル指定
     */
    static CellSpec formula(final String expression) {
        return new CellSpec() {
            @Override
            void write(XlsFixture fixture, Cell cell) {
                cell.setCellFormula(expression);
            }
        };
    }

    /**
     * 真偽値セル。
     *
     * @param value 値
     * @return セル指定
     */
    static CellSpec bool(final boolean value) {
        return new CellSpec() {
            @Override
            void write(XlsFixture fixture, Cell cell) {
                cell.setCellValue(value);
            }
        };
    }

    /**
     * エラー値セル（{@code #DIV/0!} 等）。
     *
     * @param value エラー種別
     * @return セル指定
     */
    static CellSpec error(final FormulaError value) {
        return new CellSpec() {
            @Override
            void write(XlsFixture fixture, Cell cell) {
                cell.setCellErrorValue(value.getCode());
            }
        };
    }

    /**
     * 空白セル（セルは存在するが値を持たない）。
     *
     * @return セル指定
     */
    static CellSpec blank() {
        return new CellSpec() {
            @Override
            void write(XlsFixture fixture, Cell cell) {
                // createCell した直後のセルは空白セル。値を書かないことが空白セルの指定そのもの。
            }
        };
    }

    /**
     * セル不在（その位置にセルを作らない）。
     *
     * @return セル指定
     */
    static CellSpec absent() {
        return new CellSpec() {
            @Override
            boolean isAbsent() {
                return true;
            }

            @Override
            void write(XlsFixture fixture, Cell cell) {
                // isAbsent() が真のときセルは作られないため呼ばれない。
                throw new UnsupportedOperationException("absent cell must not be written");
            }
        };
    }
}
