package nablarch.test.tool.converter.xls;

import java.util.List;

/**
 * カラム名の行が<b>マーカーカラムだけ</b>で構成されたデータブロックの版面（カラム名と各行の値）。
 *
 * <p>
 * フレームワークはマーカーカラムを除外するため器にはカラムも値も残らないが、往復して同じ版面へ戻すために
 * 名前と値を別途保持する。値はフレームワークが解釈したあとの値である。
 * </p>
 *
 * @see nablarch.test.core.reader.XlsParserAccessor#readMarkerOnlyBlock(String, String, String, String,
 *      nablarch.test.core.reader.DataType)
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
    public MarkerOnlyBlock(List<String> columnNames, List<List<String>> rows) {
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
