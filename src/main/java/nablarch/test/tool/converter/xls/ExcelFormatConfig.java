package nablarch.test.tool.converter.xls;

import org.apache.poi.ss.usermodel.IndexedColors;

/**
 * {@link XlsFormatWriter} の整形設定。
 *
 * <p>
 * Excel OUT は人が見て編集する前提のため、行種別ごとの装飾やレイアウトで読みやすく整える
 * 整形は NTF 仕様上の意味を持たず、読み戻し（{@link XlsFormatReader}）は
 * 装飾を無視するため、整形の有無・配色は往復の可逆性に影響しない。
 * </p>
 *
 * <p>
 * 不変オブジェクトで、{@link #defaults()} が見やすい既定値を提供する。各 {@code with*} は当該項目だけを
 * 差し替えたコピーを返すため、{@code ExcelFormatConfig.defaults().withBlankRowsBetweenBlocks(0)} のように
 * 既定値を部分的に上書きできる。
 * </p>
 *
 * <p>
 * ヘッダ背景色はブロック種別グループごとに 4 種類を持つ（{@link Fill} の HEADER_TEST_SHOTS /
 * HEADER_SETUP / HEADER_EXPECTED / HEADER_OTHER に対応）。マーカーカラムは別途 1 色。
 * 色はいずれも POI のインデックスカラー（{@link IndexedColors#getIndex()}）で保持する。
 * </p>
 *
 * @author kiyobot
 */
public final class ExcelFormatConfig {

    /** testShots グループヘッダ行の背景色（POI インデックスカラー）。 */
    private final short testShotsHeaderColorIndex;

    /** SETUP 系グループヘッダ行の背景色（POI インデックスカラー）。 */
    private final short setupHeaderColorIndex;

    /** EXPECTED 系グループヘッダ行の背景色（POI インデックスカラー）。 */
    private final short expectedHeaderColorIndex;

    /** その他グループヘッダ行の背景色（POI インデックスカラー）。 */
    private final short otherHeaderColorIndex;

    /** マーカーカラムの背景色（POI インデックスカラー）。 */
    private final short markerColumnColorIndex;

    /** 列幅を各列の値の最大文字数に合わせて自動調整するか。 */
    private final boolean autoColumnWidth;

    /** 自動調整時の列幅上限（文字数）。列名が見える程度に抑えて全体俯瞰しやすくする。 */
    private final int maxColumnWidthChars;

    /** データブロックの外枠に細線の罫線を引くか。 */
    private final boolean drawBlockBorder;

    /** データブロック内のセル間にも罫線を引くか（内部グリッド線）。 */
    private final boolean drawCellBorder;

    /** シートの目盛り線（Excel デフォルトの薄いグリッド）を表示するか。 */
    private final boolean displayGridlines;

    /** データブロック間に挿入する空行数（0 以上）。 */
    private final int blankRowsBetweenBlocks;

    /**
     * コンストラクタ。
     *
     * @param testShotsHeaderColorIndex testShots グループヘッダ行の背景色（POI インデックスカラー）
     * @param setupHeaderColorIndex     SETUP 系グループヘッダ行の背景色（POI インデックスカラー）
     * @param expectedHeaderColorIndex  EXPECTED 系グループヘッダ行の背景色（POI インデックスカラー）
     * @param otherHeaderColorIndex     その他グループヘッダ行の背景色（POI インデックスカラー）
     * @param markerColumnColorIndex    マーカーカラムの背景色（POI インデックスカラー）
     * @param autoColumnWidth           列幅を自動調整するか
     * @param maxColumnWidthChars       自動調整時の列幅上限（文字数、1 以上）
     * @param drawBlockBorder           ブロック外枠に罫線を引くか
     * @param drawCellBorder            セル間の内部グリッド線を引くか
     * @param displayGridlines          シートの目盛り線を表示するか
     * @param blankRowsBetweenBlocks    ブロック間の空行数（0 以上）
     * @throws IllegalArgumentException 空行数が負、または列幅上限が 1 未満の場合
     */
    public ExcelFormatConfig(short testShotsHeaderColorIndex, short setupHeaderColorIndex,
                             short expectedHeaderColorIndex, short otherHeaderColorIndex,
                             short markerColumnColorIndex,
                             boolean autoColumnWidth, int maxColumnWidthChars,
                             boolean drawBlockBorder, boolean drawCellBorder,
                             boolean displayGridlines, int blankRowsBetweenBlocks) {
        if (blankRowsBetweenBlocks < 0) {
            throw new IllegalArgumentException(
                    "blankRowsBetweenBlocks must not be negative. but was [" + blankRowsBetweenBlocks + "]");
        }
        if (maxColumnWidthChars < 1) {
            throw new IllegalArgumentException(
                    "maxColumnWidthChars must be positive. but was [" + maxColumnWidthChars + "]");
        }
        this.testShotsHeaderColorIndex = testShotsHeaderColorIndex;
        this.setupHeaderColorIndex = setupHeaderColorIndex;
        this.expectedHeaderColorIndex = expectedHeaderColorIndex;
        this.otherHeaderColorIndex = otherHeaderColorIndex;
        this.markerColumnColorIndex = markerColumnColorIndex;
        this.autoColumnWidth = autoColumnWidth;
        this.maxColumnWidthChars = maxColumnWidthChars;
        this.drawBlockBorder = drawBlockBorder;
        this.drawCellBorder = drawCellBorder;
        this.displayGridlines = displayGridlines;
        this.blankRowsBetweenBlocks = blankRowsBetweenBlocks;
    }

    /**
     * 見やすい既定値の設定を返す。
     *
     * <ul>
     *   <li>testShots ヘッダ背景色: ライム（{@link IndexedColors#LIME}）</li>
     *   <li>SETUP 系ヘッダ背景色: 淡い青（{@link IndexedColors#PALE_BLUE}）</li>
     *   <li>EXPECTED 系ヘッダ背景色: 淡い黄（{@link IndexedColors#LIGHT_YELLOW}）</li>
     *   <li>その他ヘッダ背景色: ラベンダー（{@link IndexedColors#LAVENDER}）</li>
     *   <li>マーカー背景色: 淡い橙（{@link IndexedColors#LIGHT_ORANGE}）</li>
     *   <li>列幅自動調整: ON（上限 20 文字）</li>
     *   <li>外枠罫線: あり</li>
     *   <li>内部グリッド線: あり</li>
     *   <li>目盛り線: OFF</li>
     *   <li>ブロック間空行: 1</li>
     * </ul>
     *
     * @return 既定設定
     */
    public static ExcelFormatConfig defaults() {
        return new ExcelFormatConfig(
                IndexedColors.LIME.getIndex(),
                IndexedColors.PALE_BLUE.getIndex(),
                IndexedColors.LIGHT_YELLOW.getIndex(),
                IndexedColors.LAVENDER.getIndex(),
                IndexedColors.LIGHT_ORANGE.getIndex(),
                true, 20,
                true, true,
                false, 1);
    }

    /**
     * testShots グループヘッダ行の背景色を差し替えたコピーを返す。
     *
     * @param colorIndex 背景色（POI インデックスカラー）
     * @return 差し替え済みコピー
     */
    public ExcelFormatConfig withTestShotsHeaderColor(short colorIndex) {
        return new ExcelFormatConfig(colorIndex, setupHeaderColorIndex,
                expectedHeaderColorIndex, otherHeaderColorIndex,
                markerColumnColorIndex, autoColumnWidth, maxColumnWidthChars,
                drawBlockBorder, drawCellBorder, displayGridlines, blankRowsBetweenBlocks);
    }

    /**
     * SETUP 系グループヘッダ行の背景色を差し替えたコピーを返す。
     *
     * @param colorIndex 背景色（POI インデックスカラー）
     * @return 差し替え済みコピー
     */
    public ExcelFormatConfig withSetupHeaderColor(short colorIndex) {
        return new ExcelFormatConfig(testShotsHeaderColorIndex, colorIndex,
                expectedHeaderColorIndex, otherHeaderColorIndex,
                markerColumnColorIndex, autoColumnWidth, maxColumnWidthChars,
                drawBlockBorder, drawCellBorder, displayGridlines, blankRowsBetweenBlocks);
    }

    /**
     * EXPECTED 系グループヘッダ行の背景色を差し替えたコピーを返す。
     *
     * @param colorIndex 背景色（POI インデックスカラー）
     * @return 差し替え済みコピー
     */
    public ExcelFormatConfig withExpectedHeaderColor(short colorIndex) {
        return new ExcelFormatConfig(testShotsHeaderColorIndex, setupHeaderColorIndex,
                colorIndex, otherHeaderColorIndex,
                markerColumnColorIndex, autoColumnWidth, maxColumnWidthChars,
                drawBlockBorder, drawCellBorder, displayGridlines, blankRowsBetweenBlocks);
    }

    /**
     * その他グループヘッダ行の背景色を差し替えたコピーを返す。
     *
     * @param colorIndex 背景色（POI インデックスカラー）
     * @return 差し替え済みコピー
     */
    public ExcelFormatConfig withOtherHeaderColor(short colorIndex) {
        return new ExcelFormatConfig(testShotsHeaderColorIndex, setupHeaderColorIndex,
                expectedHeaderColorIndex, colorIndex,
                markerColumnColorIndex, autoColumnWidth, maxColumnWidthChars,
                drawBlockBorder, drawCellBorder, displayGridlines, blankRowsBetweenBlocks);
    }

    /**
     * マーカーカラムの背景色を差し替えたコピーを返す。
     *
     * @param colorIndex 背景色（POI インデックスカラー）
     * @return 差し替え済みコピー
     */
    public ExcelFormatConfig withMarkerColumnColor(short colorIndex) {
        return new ExcelFormatConfig(testShotsHeaderColorIndex, setupHeaderColorIndex,
                expectedHeaderColorIndex, otherHeaderColorIndex,
                colorIndex, autoColumnWidth, maxColumnWidthChars,
                drawBlockBorder, drawCellBorder, displayGridlines, blankRowsBetweenBlocks);
    }

    /**
     * 列幅自動調整の有無を差し替えたコピーを返す。
     *
     * @param autoColumnWidth 自動調整するか
     * @return 差し替え済みコピー
     */
    public ExcelFormatConfig withAutoColumnWidth(boolean autoColumnWidth) {
        return new ExcelFormatConfig(testShotsHeaderColorIndex, setupHeaderColorIndex,
                expectedHeaderColorIndex, otherHeaderColorIndex,
                markerColumnColorIndex, autoColumnWidth, maxColumnWidthChars,
                drawBlockBorder, drawCellBorder, displayGridlines, blankRowsBetweenBlocks);
    }

    /**
     * 列幅自動調整時の上限文字数を差し替えたコピーを返す。
     *
     * @param maxColumnWidthChars 上限文字数（1 以上）
     * @return 差し替え済みコピー
     */
    public ExcelFormatConfig withMaxColumnWidthChars(int maxColumnWidthChars) {
        return new ExcelFormatConfig(testShotsHeaderColorIndex, setupHeaderColorIndex,
                expectedHeaderColorIndex, otherHeaderColorIndex,
                markerColumnColorIndex, autoColumnWidth, maxColumnWidthChars,
                drawBlockBorder, drawCellBorder, displayGridlines, blankRowsBetweenBlocks);
    }

    /**
     * ブロック外枠罫線の有無を差し替えたコピーを返す。
     *
     * @param drawBlockBorder 罫線を引くか
     * @return 差し替え済みコピー
     */
    public ExcelFormatConfig withBlockBorder(boolean drawBlockBorder) {
        return new ExcelFormatConfig(testShotsHeaderColorIndex, setupHeaderColorIndex,
                expectedHeaderColorIndex, otherHeaderColorIndex,
                markerColumnColorIndex, autoColumnWidth, maxColumnWidthChars,
                drawBlockBorder, drawCellBorder, displayGridlines, blankRowsBetweenBlocks);
    }

    /**
     * セル間の内部グリッド線の有無を差し替えたコピーを返す。
     *
     * @param drawCellBorder 内部グリッド線を引くか
     * @return 差し替え済みコピー
     */
    public ExcelFormatConfig withCellBorder(boolean drawCellBorder) {
        return new ExcelFormatConfig(testShotsHeaderColorIndex, setupHeaderColorIndex,
                expectedHeaderColorIndex, otherHeaderColorIndex,
                markerColumnColorIndex, autoColumnWidth, maxColumnWidthChars,
                drawBlockBorder, drawCellBorder, displayGridlines, blankRowsBetweenBlocks);
    }

    /**
     * シートの目盛り線表示の有無を差し替えたコピーを返す。
     *
     * @param displayGridlines 目盛り線を表示するか
     * @return 差し替え済みコピー
     */
    public ExcelFormatConfig withDisplayGridlines(boolean displayGridlines) {
        return new ExcelFormatConfig(testShotsHeaderColorIndex, setupHeaderColorIndex,
                expectedHeaderColorIndex, otherHeaderColorIndex,
                markerColumnColorIndex, autoColumnWidth, maxColumnWidthChars,
                drawBlockBorder, drawCellBorder, displayGridlines, blankRowsBetweenBlocks);
    }

    /**
     * ブロック間空行数を差し替えたコピーを返す。
     *
     * @param blankRowsBetweenBlocks 空行数（0 以上）
     * @return 差し替え済みコピー
     */
    public ExcelFormatConfig withBlankRowsBetweenBlocks(int blankRowsBetweenBlocks) {
        return new ExcelFormatConfig(testShotsHeaderColorIndex, setupHeaderColorIndex,
                expectedHeaderColorIndex, otherHeaderColorIndex,
                markerColumnColorIndex, autoColumnWidth, maxColumnWidthChars,
                drawBlockBorder, drawCellBorder, displayGridlines, blankRowsBetweenBlocks);
    }

    /** @return testShots グループヘッダ行の背景色（POI インデックスカラー） */
    public short getTestShotsHeaderColorIndex() {
        return testShotsHeaderColorIndex;
    }

    /** @return SETUP 系グループヘッダ行の背景色（POI インデックスカラー） */
    public short getSetupHeaderColorIndex() {
        return setupHeaderColorIndex;
    }

    /** @return EXPECTED 系グループヘッダ行の背景色（POI インデックスカラー） */
    public short getExpectedHeaderColorIndex() {
        return expectedHeaderColorIndex;
    }

    /** @return その他グループヘッダ行の背景色（POI インデックスカラー） */
    public short getOtherHeaderColorIndex() {
        return otherHeaderColorIndex;
    }

    /** @return マーカーカラムの背景色（POI インデックスカラー） */
    public short getMarkerColumnColorIndex() {
        return markerColumnColorIndex;
    }

    /** @return 列幅を自動調整するか */
    public boolean isAutoColumnWidth() {
        return autoColumnWidth;
    }

    /** @return 列幅自動調整時の上限文字数 */
    public int getMaxColumnWidthChars() {
        return maxColumnWidthChars;
    }

    /** @return ブロック外枠に罫線を引くか */
    public boolean isDrawBlockBorder() {
        return drawBlockBorder;
    }

    /** @return セル間の内部グリッド線を引くか */
    public boolean isDrawCellBorder() {
        return drawCellBorder;
    }

    /** @return シートの目盛り線を表示するか */
    public boolean isDisplayGridlines() {
        return displayGridlines;
    }

    /** @return ブロック間の空行数 */
    public int getBlankRowsBetweenBlocks() {
        return blankRowsBetweenBlocks;
    }
}
