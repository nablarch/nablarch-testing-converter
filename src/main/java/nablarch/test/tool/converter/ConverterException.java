package nablarch.test.tool.converter;

/**
 * 変換処理で回復不能な状況（入力先が存在しない・出力衝突など）を表す非検査例外。
 *
 * @author kiyobot
 */
public final class ConverterException extends RuntimeException {

    /**
     * メッセージを指定して例外を生成する。
     *
     * @param message 詳細メッセージ
     */
    public ConverterException(String message) {
        super(message);
    }

    /**
     * メッセージと原因を指定して例外を生成する。
     *
     * @param message 詳細メッセージ
     * @param cause   原因
     */
    public ConverterException(String message, Throwable cause) {
        super(message, cause);
    }
}
