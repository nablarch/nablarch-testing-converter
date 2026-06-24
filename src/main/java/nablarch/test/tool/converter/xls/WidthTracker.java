package nablarch.test.tool.converter.xls;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * 列ごとの最大文字数を集計し、自動列幅をシートへ適用するトラッカ。
 */
final class WidthTracker {

    /** Excel の列幅上限（文字数）。 */
    private static final int MAX_CHARS = 255;

    /** 余白（文字数）。 */
    private static final int PADDING = 2;

    /** 1 文字あたりの列幅単位（POI）。 */
    private static final int WIDTH_UNIT = 256;

    /** 列番号 → 最大文字数。 */
    private final Map<Integer, Integer> maxChars = new HashMap<>();

    /**
     * 1 セルの文字数を観測する。
     *
     * @param column 列番号
     * @param length 文字数
     */
    void observe(int column, int length) {
        Integer current = maxChars.get(column);
        if (current == null || length > current) {
            maxChars.put(column, length);
        }
    }

    /**
     * 集計した最大文字数に基づき列幅を設定する。
     *
     * @param sheet 対象シート
     */
    void applyTo(Sheet sheet) {
        for (Map.Entry<Integer, Integer> entry : maxChars.entrySet()) {
            int chars = Math.min(entry.getValue() + PADDING, MAX_CHARS);
            sheet.setColumnWidth(entry.getKey(), chars * WIDTH_UNIT);
        }
    }
}
