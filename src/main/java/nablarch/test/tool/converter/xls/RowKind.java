package nablarch.test.tool.converter.xls;

/** 行の種別（背景色の付与判定に使う）。 */
enum RowKind {
    /** 識別行・各種ヘッダ行（背景色を付す）。 */
    HEADER,
    /** ディレクティブ・FW 制御ヘッダ等のメタ行（背景色なし）。 */
    META,
    /** データ行（背景色なし）。 */
    DATA
}
