package nablarch.test.tool.converter.xls;

/** 行の種別（背景色・罫線の付与判定に使う）。 */
enum RowKind {
    /** 列名行・レコード名行等のヘッダ行（背景色を付す）。 */
    HEADER,
    /** 識別行（SETUP_TABLE=xxx 等）。罫線なし・背景色なし。 */
    META,
    /** ディレクティブ・FW 制御ヘッダ等のキー値行。罫線あり・左列（キー）に背景色。 */
    DIRECTIVE,
    /** データ行（背景色なし）。 */
    DATA
}
