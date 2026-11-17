package nablarch.test.tool.converter;

/**
 * 変換ツールが扱うテストデータ形式。
 *
 * @author kiyobot
 */
public enum DataFormat {

    /** Excel（{@code .xlsx}） */
    XLS("xls"),

    /** YAML（{@code .yaml}） */
    YAML("yaml");

    /** 引数・設定値で用いる識別子 */
    private final String argument;

    /**
     * コンストラクタ。
     *
     * @param argument 引数・設定値で用いる識別子
     */
    DataFormat(String argument) {
        this.argument = argument;
    }

    /**
     * 本形式の入出力の振る舞いを担うハンドラを返す。
     *
     * @return 形式ハンドラ
     */
    FormatHandler handler() {
        switch (this) {
            case XLS:
                return new XlsFormatHandler();
            case YAML:
                return new YamlFormatHandler();
            default:
                // enum 値追加時のハンドラ未実装を検出するための番人
                throw new IllegalStateException("no handler for format: " + this);
        }
    }

    /**
     * 引数・設定値で用いる識別子を返す。
     *
     * @return 識別子（{@code "xls"} または {@code "yaml"}）
     */
    public String getArgument() {
        return argument;
    }

    /**
     * 識別子（{@code "xls"}／{@code "yaml"}）から形式を解決する。
     *
     * @param argument 識別子（前後空白は許容せず厳密一致）
     * @return 対応する形式
     * @throws IllegalArgumentException 識別子が未知の場合
     */
    public static DataFormat fromArgument(String argument) {
        for (DataFormat format : values()) {
            if (format.argument.equals(argument)) {
                return format;
            }
        }
        throw new IllegalArgumentException("unknown data format: " + argument);
    }
}
