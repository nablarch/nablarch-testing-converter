package nablarch.test.tool.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.apache.poi.ss.usermodel.IndexedColors;
import org.junit.Test;

import nablarch.test.tool.converter.xls.ExcelFormatConfig;

/**
 * {@link XlsOutputConfig} のテストクラス。
 *
 * @author kiyobot
 */
public class XlsOutputConfigTest {

    /**
     * Given: すべてのフィールドを省略する（デフォルトコンストラクタ）。
     * When : toExcelFormatConfig() を呼ぶ。
     * Then : ExcelFormatConfig.defaults() と同一の設定が返る。
     */
    @Test
    public void allFieldsOmitted_returnsDefaults() {
        // Given
        XlsOutputConfig config = new XlsOutputConfig();

        // When
        ExcelFormatConfig result = config.toExcelFormatConfig();

        // Then
        ExcelFormatConfig defaults = ExcelFormatConfig.defaults();
        assertThat(result.getTestShotsHeaderColorIndex(), is(defaults.getTestShotsHeaderColorIndex()));
        assertThat(result.getSetupHeaderColorIndex(), is(defaults.getSetupHeaderColorIndex()));
        assertThat(result.getExpectedHeaderColorIndex(), is(defaults.getExpectedHeaderColorIndex()));
        assertThat(result.getOtherHeaderColorIndex(), is(defaults.getOtherHeaderColorIndex()));
        assertThat(result.getMarkerColumnColorIndex(), is(defaults.getMarkerColumnColorIndex()));
        assertThat(result.isAutoColumnWidth(), is(defaults.isAutoColumnWidth()));
        assertThat(result.getMaxColumnWidthChars(), is(defaults.getMaxColumnWidthChars()));
        assertThat(result.isDrawBlockBorder(), is(defaults.isDrawBlockBorder()));
        assertThat(result.isDrawCellBorder(), is(defaults.isDrawCellBorder()));
        assertThat(result.isDisplayGridlines(), is(defaults.isDisplayGridlines()));
        assertThat(result.getBlankRowsBetweenBlocks(), is(defaults.getBlankRowsBetweenBlocks()));
    }

    /**
     * Given: 有効な色名（AQUA）を setupHeaderColor に指定する。
     * When : toExcelFormatConfig() を呼ぶ。
     * Then : AQUA のインデックス値が setupHeaderColorIndex に反映される。
     */
    @Test
    public void validColorName_appliesColorIndex() {
        // Given
        XlsOutputConfig config = new XlsOutputConfig();
        config.setSetupHeaderColor("AQUA");

        // When
        ExcelFormatConfig result = config.toExcelFormatConfig();

        // Then
        assertThat(result.getSetupHeaderColorIndex(), is(IndexedColors.AQUA.getIndex()));
    }

    /**
     * Given: 無効な色名（"NOT_A_COLOR"）を setupHeaderColor に指定する。
     * When : toExcelFormatConfig() を呼ぶ。
     * Then : IllegalArgumentException がスローされる。
     */
    @Test
    public void invalidColorName_throwsIllegalArgumentException() {
        // Given
        XlsOutputConfig config = new XlsOutputConfig();
        config.setSetupHeaderColor("NOT_A_COLOR");

        // When / Then
        try {
            config.toExcelFormatConfig();
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage().contains("NOT_A_COLOR"), is(true));
            assertThat(e.getMessage().contains("IndexedColors"), is(true));
        }
    }
}
