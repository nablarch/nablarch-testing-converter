package nablarch.test.tool.converter.xls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
 * {@code xls} パッケージのテストが共有する POI ユーティリティ。実 {@code .xlsx} を組み立てる
 * フィクスチャビルダ（{@link #book}／{@link #sheet}／{@link #row}／{@link #writeTo}）と、
 * 書き出したブックを読む側のヘルパ（{@link #open}／{@link #cell}／{@link #line}／{@link #EXTENSION}）から成る。
 *
 * <p>
 * <b>ビルダ側の存在理由。</b>既存のテストヘルパ（{@code XlsFormatWriterTest} ／ {@code RoundTripTest} が
 * 持つ private static の {@code container} / {@code table} / {@code row} など）は<b>中間モデル</b>を
 * 組み立て、Excel が要るときは {@link XlsFormatWriter} に書かせる。{@link XlsFormatWriter} はすべての値を
 * 文字列セルとして書くため、数値セル・日付書式セル・数式セル・真偽値セル・エラーセル・表示形式といった
 * <b>セル種別</b>を表現できない。ビルダはその表現できない軸を受け持つ。
 * </p>
 *
 * <p>
 * <b>読み出し側の存在理由。</b>{@link #open}／{@link #cell}／{@link #line} は、書き出した実 {@code .xlsx} を
 * 開いて版面を突き合わせる操作を 1 か所へ集めたものである。ビルダを使わないテストクラスからも使う
 * （下の「本クラスが引き受けるヘルパの範囲」）。
 * </p>
 *
 * <p>
 * どちらも<b>中間モデル組み立てヘルパとは対象レイヤが異なる</b>ため重複しない。
 * </p>
 *
 * <p>
 * ビルダの使い方（{@link #sheet} でシートを開き、{@link #row} で行を上から順に追加する。{@link #sheet} は
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
 * <p>
 * <b>本クラスが引き受けるヘルパの範囲（#22 から持ち越した判断を #23 で確定。2026-08-13）。</b>
 * 引き受けるのは<b>POI のブック・シートを直接触る</b>ユーティリティだけである
 * （{@link #open}／{@link #cell}／{@link #line}／{@link #EXTENSION}）。
 * <b>中間モデルを組み立てるヘルパ（{@code row} / {@code map} / {@code container}）は引き受けない。</b>
 * 上段の「本クラスは中間モデル組み立てヘルパとは対象レイヤが異なる」という線引きをそのまま境界に使う。
 * </p>
 * <ul>
 *   <li><b>移した</b>: {@code line} は {@code XlsFormatWriterTest} と {@code XlsFormatWriterModelTest} に
 *       本体が完全一致した写しとして存在していた。Writer 系のテストクラスは 4 本あり、次の 1 本も同じ写しを
 *       持つ見込みが高い。{@code cell} は写しこそ 1 件だったが {@code line} と対になるシート読み出しであり、
 *       離すと次の写しが {@code cell} 側に生まれるため同時に移した。</li>
 *   <li><b>移さない</b>: {@code row} はリポジトリの複数パッケージ（{@code xls} / {@code yaml} /
 *       {@code converter} / {@code core.reader}）に定着したイディオムで、集約するとパッケージをまたぐ依存が
 *       増えるだけである（本体は {@code Arrays.asList} 1 行）。{@code map} は写しが 2 件あるが中間モデル
 *       組み立て側で境界の向こうにある。{@code container} は 5 定義とも<b>各クラスのシート名・ブック名の
 *       決め方に合わせた局所版</b>であり、共有すると各クラスがその決め方を引数へ戻すことになる
 *       （例: {@code XlsFormatWriterModelTest#container} はシート名を定数 {@code SHEET} に固定した 2 引数版で、
 *       共有版に合わせると呼び出し 17 か所すべてがシート名を書くことになる）。</li>
 * </ul>
 *
 * <p>
 * 判断の根拠になった実測値（写しの数・定義の位置・引数の形）と、それを導いたコマンドは
 * {@code .rn/ntf-test-data-converter/coverage/inventory.md} §3.1-5 に記録した。
 * </p>
 *
 * @author kiyobot
 */
final class XlsFixture {

    /**
     * {@link #writeTo} が付ける出力拡張子。{@code PoiXlsReader} は {@code .xls} → {@code .xlsx} の順に探す。
     *
     * <p>
     * {@link XlsFormatWriter} が付ける拡張子と同じ値であり、SUT の出力ファイル名を組み立てる用途にも
     * 使える（{@code XlsFormatWriterModelTest#write} ／ {@code XlsFormatWriterInvalidOutputTest#EXTENSION}）。
     * ただし SUT 側の定義は {@code XlsFormatWriter} の {@code private static final String EXTENSION}
     * （src/main L71）であってテストからは参照できないため、<b>本定数は同じ値を独立に持っているだけ</b>で、
     * 両者が一致していることを担保するものではない。
     * </p>
     */
    static final String EXTENSION = ".xlsx";

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

    /**
     * 1 セルの文字列値を取り出す。
     *
     * <p>
     * <b>文字列セル専用である。</b>{@code getStringCellValue()} 固定なので、本クラスのビルダが作る
     * 非文字列セル（{@link #number} / {@link #date} / {@link #formula} / {@link #bool} / {@link #error}）に
     * 当てると例外になる。空白セル（{@link #blank}）は空文字を返すため例外にならない。
     * セル種別そのものを見たいときは {@link #open} で得たブックから素の {@code getRow} / {@code getCell} を
     * たどる（{@code XlsFormatReaderCellTypeTest#writtenSheet} の 3 か所の呼び出しがその形。
     * うち {@code readsTextFormattedNumericCellAsDoubleString} は数値セルの {@code getCellType()} を
     * アサートするため本メソッドでは書けない）。
     * </p>
     *
     * @param sheet シート
     * @param r     行番号（0 始まり）
     * @param c     列番号（0 始まり）
     * @return セルの文字列値。行またはセルが存在しなければ {@code null}
     * @throws IllegalStateException 対象セルが文字列セルでない場合
     *                               （POI 3.8 は {@code Cannot get a text value from a numeric cell} 等を投げる）
     */
    static String cell(Sheet sheet, int r, int c) {
        Row row = sheet.getRow(r);
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(c);
        return cell == null ? null : cell.getStringCellValue();
    }

    /**
     * 1 行を文字列リストとして取り出す（末尾の空セルも含む）。
     *
     * <p>
     * <b>文字列セル専用である。</b>前提は {@link #cell} と同じ（行内に非文字列セルが 1 つでもあると
     * そこで例外になる）。
     * </p>
     *
     * @param sheet シート
     * @param r     行番号（0 始まり）
     * @return セル値のリスト。セルが存在しない位置は {@code null}。行が存在しなければ {@code null}
     * @throws IllegalStateException 行内に文字列セルでないセルがある場合
     *                               （POI 3.8 は {@code Cannot get a text value from a numeric cell} 等を投げる）
     */
    static List<String> line(Sheet sheet, int r) {
        Row row = sheet.getRow(r);
        if (row == null) {
            return null;
        }
        List<String> cells = new ArrayList<>();
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            cells.add(cell == null ? null : cell.getStringCellValue());
        }
        return cells;
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
