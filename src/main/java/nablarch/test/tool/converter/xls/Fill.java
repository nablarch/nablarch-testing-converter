package nablarch.test.tool.converter.xls;

/** セル背景色の種類。 */
enum Fill {
    /** 背景色なし。 */
    NONE,
    /** ヘッダ背景色（testShots グループ：LIST_MAP で identifier == "testShots"）。 */
    HEADER_TEST_SHOTS,
    /** ヘッダ背景色（SETUP 系グループ：SETUP_TABLE_DATA / SETUP_FIXED / SETUP_VARIABLE）。 */
    HEADER_SETUP,
    /**
     * ヘッダ背景色（EXPECTED 系グループ：EXPECTED_TABLE_DATA / EXPECTED_COMPLETED / EXPECTED_FIXED /
     * EXPECTED_VARIABLE / EXPECTED_REQUEST_HEADER_MESSAGES / EXPECTED_REQUEST_BODY_MESSAGES /
     * RESPONSE_HEADER_MESSAGES / RESPONSE_BODY_MESSAGES）。
     */
    HEADER_EXPECTED,
    /** ヘッダ背景色（その他グループ：MESSAGE / LIST_MAP（非 testShots））。 */
    HEADER_OTHER,
    /** マーカーカラム背景色。 */
    MARKER
}
