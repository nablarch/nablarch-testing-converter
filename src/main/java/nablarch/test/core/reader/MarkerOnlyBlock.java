package nablarch.test.core.reader;

import java.util.List;

/**
 * カラム名の行が<b>マーカーカラムだけ</b>で構成されたデータブロックの版面（カラム名と各行の値）。
 *
 * <p>
 * マーカーカラム（{@code [COL]} のように半角角括弧で囲んだカラム）は、フレームワークが読み込み対象から
 * 除外する。カラム名の行がマーカーカラムだけのブロックでは、除外の結果としてフレームワークが持つ器
 * （{@code TableData}／{@code List<Map>}）にカラムも値も残らない。エントリの数と並びだけは器にも残るが、
 * 変換ツールは名前と値も要る——このブロックの各エントリはフィールドを持たないものの、テストショット一覧と
 * 行の順序で対応付ける用途ではエントリの数と並びが意味を持ち、往復して同じ版面へ戻せなければならないためである。
 * </p>
 *
 * <p>
 * 値は<b>フレームワークが解釈したあとの値</b>（Java {@code null} または {@link String}）である。
 * フレームワークは行の解釈をマーカーカラムの除外より前に行の全セルへ掛けるため、マーカーカラムの値も
 * 他のカラムと同じ解釈を受けている。
 * </p>
 *
 * <p>各行はカラム名と同じ要素数を持つ（足りない位置は空文字で埋めてある）。</p>
 *
 * @see TestCoreReaderAdapter#readMarkerOnlyBlock(String, String, String, String, DataType)
 * @author kiyobot
 */
public class MarkerOnlyBlock {

    /** マーカーカラムの名前（記述順） */
    private final List<String> columnNames;

    /** 各行の値（記述順。カラム名と同じ要素数） */
    private final List<List<String>> rows;

    /**
     * コンストラクタ。
     *
     * @param columnNames マーカーカラムの名前（記述順）
     * @param rows        各行の値（記述順。カラム名と同じ要素数）
     */
    MarkerOnlyBlock(List<String> columnNames, List<List<String>> rows) {
        this.columnNames = columnNames;
        this.rows = rows;
    }

    /** @return マーカーカラムの名前（記述順） */
    public List<String> getColumnNames() {
        return columnNames;
    }

    /** @return 各行の値（記述順。カラム名と同じ要素数） */
    public List<List<String>> getRows() {
        return rows;
    }
}
