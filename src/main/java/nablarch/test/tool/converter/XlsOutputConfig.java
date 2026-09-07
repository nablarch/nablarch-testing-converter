package nablarch.test.tool.converter;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.poi.ss.usermodel.IndexedColors;

import nablarch.test.tool.converter.xls.ExcelFormatConfig;

/**
 * Maven plugin {@code <configuration>} で指定する Excel 出力整形設定の POJO。
 *
 * <p>
 * すべてのフィールドは省略可能（{@code null} なら {@link ExcelFormatConfig#defaults()} の値を使う）。
 * {@link #toExcelFormatConfig()} で {@link ExcelFormatConfig} に変換して利用する。
 * </p>
 *
 * <p>色名は Apache POI の {@link IndexedColors} の列挙定数名（例: {@code AQUA}、{@code YELLOW}）で指定する。</p>
 *
 * @author kiyobot
 */
public class XlsOutputConfig {

    /** testShots グループヘッダ行の背景色（IndexedColors 名。省略時: LIME） */
    @Parameter
    private String testShotsHeaderColor;

    /** SETUP 系グループヘッダ行の背景色（IndexedColors 名。省略時: PALE_BLUE） */
    @Parameter
    private String setupHeaderColor;

    /** EXPECTED 系グループヘッダ行の背景色（IndexedColors 名。省略時: LIGHT_YELLOW） */
    @Parameter
    private String expectedHeaderColor;

    /** その他グループヘッダ行の背景色（IndexedColors 名。省略時: LAVENDER） */
    @Parameter
    private String otherHeaderColor;

    /** マーカーカラムの背景色（IndexedColors 名。省略時: LIGHT_ORANGE） */
    @Parameter
    private String markerColumnColor;

    /** 列幅自動調整の有無（省略時: true） */
    @Parameter
    private Boolean autoColumnWidth;

    /** 列幅自動調整時の上限文字数（省略時: 20） */
    @Parameter
    private Integer maxColumnWidthChars;

    /** ブロック外枠罫線の有無（省略時: true） */
    @Parameter
    private Boolean drawBlockBorder;

    /** セル間内部グリッド線の有無（省略時: true） */
    @Parameter
    private Boolean drawCellBorder;

    /** シート目盛り線の表示（省略時: false） */
    @Parameter
    private Boolean displayGridlines;

    /** ブロック間空行数（省略時: 1） */
    @Parameter
    private Integer blankRowsBetweenBlocks;

    // ---- setters (Maven plugin の setter injection 用) ----

    /** @param testShotsHeaderColor testShots ヘッダ色の IndexedColors 名 */
    public void setTestShotsHeaderColor(String testShotsHeaderColor) {
        this.testShotsHeaderColor = testShotsHeaderColor;
    }

    /** @param setupHeaderColor SETUP 系ヘッダ色の IndexedColors 名 */
    public void setSetupHeaderColor(String setupHeaderColor) {
        this.setupHeaderColor = setupHeaderColor;
    }

    /** @param expectedHeaderColor EXPECTED 系ヘッダ色の IndexedColors 名 */
    public void setExpectedHeaderColor(String expectedHeaderColor) {
        this.expectedHeaderColor = expectedHeaderColor;
    }

    /** @param otherHeaderColor その他ヘッダ色の IndexedColors 名 */
    public void setOtherHeaderColor(String otherHeaderColor) {
        this.otherHeaderColor = otherHeaderColor;
    }

    /** @param markerColumnColor マーカーカラム色の IndexedColors 名 */
    public void setMarkerColumnColor(String markerColumnColor) {
        this.markerColumnColor = markerColumnColor;
    }

    /** @param autoColumnWidth 列幅自動調整の有無 */
    public void setAutoColumnWidth(Boolean autoColumnWidth) {
        this.autoColumnWidth = autoColumnWidth;
    }

    /** @param maxColumnWidthChars 列幅上限文字数 */
    public void setMaxColumnWidthChars(Integer maxColumnWidthChars) {
        this.maxColumnWidthChars = maxColumnWidthChars;
    }

    /** @param drawBlockBorder ブロック外枠罫線の有無 */
    public void setDrawBlockBorder(Boolean drawBlockBorder) {
        this.drawBlockBorder = drawBlockBorder;
    }

    /** @param drawCellBorder セル間内部グリッド線の有無 */
    public void setDrawCellBorder(Boolean drawCellBorder) {
        this.drawCellBorder = drawCellBorder;
    }

    /** @param displayGridlines シート目盛り線の表示 */
    public void setDisplayGridlines(Boolean displayGridlines) {
        this.displayGridlines = displayGridlines;
    }

    /** @param blankRowsBetweenBlocks ブロック間空行数 */
    public void setBlankRowsBetweenBlocks(Integer blankRowsBetweenBlocks) {
        this.blankRowsBetweenBlocks = blankRowsBetweenBlocks;
    }

    /**
     * 本 POJO を {@link ExcelFormatConfig} に変換する。
     *
     * <p>{@code null} のフィールドは {@link ExcelFormatConfig#defaults()} の値をそのまま使う。</p>
     *
     * @return 設定済み {@link ExcelFormatConfig}
     * @throws IllegalArgumentException 色名が {@link IndexedColors} に存在しない場合
     */
    public ExcelFormatConfig toExcelFormatConfig() {
        ExcelFormatConfig config = ExcelFormatConfig.defaults();

        if (testShotsHeaderColor != null) {
            config = config.withTestShotsHeaderColor(resolveColor(testShotsHeaderColor));
        }
        if (setupHeaderColor != null) {
            config = config.withSetupHeaderColor(resolveColor(setupHeaderColor));
        }
        if (expectedHeaderColor != null) {
            config = config.withExpectedHeaderColor(resolveColor(expectedHeaderColor));
        }
        if (otherHeaderColor != null) {
            config = config.withOtherHeaderColor(resolveColor(otherHeaderColor));
        }
        if (markerColumnColor != null) {
            config = config.withMarkerColumnColor(resolveColor(markerColumnColor));
        }
        if (autoColumnWidth != null) {
            config = config.withAutoColumnWidth(autoColumnWidth);
        }
        if (maxColumnWidthChars != null) {
            config = config.withMaxColumnWidthChars(maxColumnWidthChars);
        }
        if (drawBlockBorder != null) {
            config = config.withBlockBorder(drawBlockBorder);
        }
        if (drawCellBorder != null) {
            config = config.withCellBorder(drawCellBorder);
        }
        if (displayGridlines != null) {
            config = config.withDisplayGridlines(displayGridlines);
        }
        if (blankRowsBetweenBlocks != null) {
            config = config.withBlankRowsBetweenBlocks(blankRowsBetweenBlocks);
        }

        return config;
    }

    /**
     * IndexedColors 名からインデックスカラー値を解決する。
     *
     * @param colorName {@link IndexedColors} の列挙定数名（例: {@code "AQUA"}）
     * @return POI インデックスカラー値
     * @throws IllegalArgumentException 名前が無効な場合
     */
    private static short resolveColor(String colorName) {
        try {
            return IndexedColors.valueOf(colorName).getIndex();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid color name: \"" + colorName + "\". "
                    + "Use an IndexedColors enum name (e.g. AQUA, YELLOW, LIME, PALE_BLUE). "
                    + "See org.apache.poi.ss.usermodel.IndexedColors for valid values.",
                    e);
        }
    }
}
