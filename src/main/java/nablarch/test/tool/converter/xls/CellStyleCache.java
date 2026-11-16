package nablarch.test.tool.converter.xls;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * セルスタイルのキャッシュ。同一構成のスタイルを使い回して POI のスタイル数上限を回避する。
 *
 * <p>
 * どのスタイルも書式を文字列（{@code @}）にする。書式が「標準」のままだと、人が {@code 001} や {@code 1} と
 * 入力し直したときに Excel が数値のセルにし、本体はそのセルを {@code 1.0} と読むためである。
 * </p>
 */
final class CellStyleCache {

    /** 文字列の書式。 */
    private static final String TEXT_FORMAT = "@";

    /** 罫線・背景色を持たないスタイルのキャッシュキー。 */
    private static final String PLAIN_KEY = "plain";

    /** ブック。 */
    private final Workbook workbook;

    /** 整形設定。 */
    private final ExcelFormatConfig config;

    /** 構成キー → スタイル。 */
    private final Map<String, CellStyle> cache = new HashMap<>();

    /**
     * コンストラクタ。
     *
     * @param workbook ブック
     * @param config   整形設定
     */
    CellStyleCache(Workbook workbook, ExcelFormatConfig config) {
        this.workbook = workbook;
        this.config = config;
    }

    /**
     * 罫線・背景色を持たない、書式だけを文字列にしたセルスタイルを返す（識別行のセル用）。
     *
     * @return セルスタイル
     */
    CellStyle getOrCreatePlain() {
        CellStyle style = cache.get(PLAIN_KEY);
        if (style == null) {
            style = createTextStyle();
            cache.put(PLAIN_KEY, style);
        }
        return style;
    }

    /**
     * 指定構成のセルスタイルを返す。まだ無ければ作ってキャッシュする。
     *
     * @param top    上辺に罫線を引くか
     * @param bottom 下辺に罫線を引くか
     * @param left   左辺に罫線を引くか
     * @param right  右辺に罫線を引くか
     * @param fill   背景色
     * @return セルスタイル
     */
    CellStyle getOrCreate(boolean top, boolean bottom, boolean left, boolean right, Fill fill) {
        String key = (top ? 1 : 0) + "" + (bottom ? 1 : 0) + (left ? 1 : 0) + (right ? 1 : 0) + "|" + fill;
        CellStyle style = cache.get(key);
        if (style == null) {
            style = createTextStyle();
            // 内部グリッド線（全辺）
            if (config.isDrawCellBorder()) {
                style.setBorderTop(CellStyle.BORDER_THIN);
                style.setBorderBottom(CellStyle.BORDER_THIN);
                style.setBorderLeft(CellStyle.BORDER_THIN);
                style.setBorderRight(CellStyle.BORDER_THIN);
            }
            // 外枠罫線（ブロック外周の辺のみ）
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
                short color;
                switch (fill) {
                    case HEADER_TEST_SHOTS:
                        color = config.getTestShotsHeaderColorIndex();
                        break;
                    case HEADER_SETUP:
                        color = config.getSetupHeaderColorIndex();
                        break;
                    case HEADER_EXPECTED:
                        color = config.getExpectedHeaderColorIndex();
                        break;
                    case HEADER_OTHER:
                        color = config.getOtherHeaderColorIndex();
                        break;
                    case MARKER:
                        color = config.getMarkerColumnColorIndex();
                        break;
                    default:
                        // 到達しない。color を確定させるため、および Fill が増えたときの検出のため残す。
                        throw new IllegalStateException("Unhandled Fill: " + fill);
                }
                style.setFillForegroundColor(color);
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            }
            cache.put(key, style);
        }
        return style;
    }

    /**
     * 書式を文字列にしたセルスタイルを新しく作る。
     *
     * @return セルスタイル
     */
    private CellStyle createTextStyle() {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat(TEXT_FORMAT));
        return style;
    }
}
