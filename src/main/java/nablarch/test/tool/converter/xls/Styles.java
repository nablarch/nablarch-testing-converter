package nablarch.test.tool.converter.xls;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * セルスタイルのキャッシュ。外枠罫線（細線）と背景色の組み合わせは有限（外枠 4 辺 × 背景 3 種）のため、
 * 同一構成のスタイルを使い回して POI のスタイル数上限を回避する。
 */
final class Styles {

    /** ブック。 */
    private final Workbook workbook;

    /** 整形設定。 */
    private final ExcelFormatConfig config;

    /** 構成キー → スタイル。 */
    private final Map<String, CellStyle> cache = new HashMap<String, CellStyle>();

    /**
     * コンストラクタ。
     *
     * @param workbook ブック
     * @param config   整形設定
     */
    Styles(Workbook workbook, ExcelFormatConfig config) {
        this.workbook = workbook;
        this.config = config;
    }

    /**
     * 指定構成のセルスタイルを返す（キャッシュ）。
     *
     * @param top    上辺に罫線を引くか
     * @param bottom 下辺に罫線を引くか
     * @param left   左辺に罫線を引くか
     * @param right  右辺に罫線を引くか
     * @param fill   背景色
     * @return セルスタイル
     */
    CellStyle get(boolean top, boolean bottom, boolean left, boolean right, Fill fill) {
        String key = (top ? 1 : 0) + "" + (bottom ? 1 : 0) + (left ? 1 : 0) + (right ? 1 : 0) + fill;
        CellStyle style = cache.get(key);
        if (style == null) {
            style = workbook.createCellStyle();
            if (config.isDrawBlockBorder()) {
                if (top) {
                    style.setBorderTop(CellStyle.BORDER_THIN);
                }
                if (bottom) {
                    style.setBorderBottom(CellStyle.BORDER_THIN);
                }
                if (left) {
                    style.setBorderLeft(CellStyle.BORDER_THIN);
                }
                if (right) {
                    style.setBorderRight(CellStyle.BORDER_THIN);
                }
            }
            if (fill != Fill.NONE) {
                short color = fill == Fill.HEADER
                        ? config.getHeaderColorIndex()
                        : config.getMarkerColumnColorIndex();
                style.setFillForegroundColor(color);
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            }
            cache.put(key, style);
        }
        return style;
    }
}
