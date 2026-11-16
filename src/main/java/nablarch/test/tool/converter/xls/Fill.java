package nablarch.test.tool.converter.xls;

/** セル背景色の種類。 */
enum Fill {
    /** 背景色なし。 */
    NONE,
    /** ヘッダ背景色（testShots グループ）。 */
    HEADER_TEST_SHOTS,
    /** ヘッダ背景色（SETUP 系グループ）。 */
    HEADER_SETUP,
    /** ヘッダ背景色（EXPECTED 系グループ）。 */
    HEADER_EXPECTED,
    /** ヘッダ背景色（その他グループ）。 */
    HEADER_OTHER,
    /** マーカーカラム背景色。 */
    MARKER
}
