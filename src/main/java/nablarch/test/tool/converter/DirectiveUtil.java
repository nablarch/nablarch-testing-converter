package nablarch.test.tool.converter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ディレクティブ変換に関するユーティリティ。
 *
 * @author kiyobot
 */
public final class DirectiveUtil {

    private DirectiveUtil() {
        // ユーティリティクラスのため、インスタンス化不可
    }

    /**
     * 本体ディレクティブ（{@code Map<String, Object>}）を文字列ディレクティブへ写す。
     * <p>
     * null 値は文字列 {@code "null"} へ化けさせないよう {@code null} のまま通す。
     * </p>
     *
     * @param directives  本体ディレクティブ
     * @param valueMapper キーと {@link Object#toString()} 済みの値文字列を最終的な文字列値へ変換する関数
     * @return 文字列ディレクティブ（{@link LinkedHashMap} 順）
     */
    public static Map<String, String> toStringDirectives(
            Map<String, Object> directives, ValueMapper valueMapper) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : directives.entrySet()) {
            Object value = entry.getValue();
            result.put(entry.getKey(),
                    value == null ? null : valueMapper.map(entry.getKey(), value.toString()));
        }
        return result;
    }

    /**
     * 区切り文字ディレクティブ（{@code record-separator}／{@code field-separator}）の値を、
     * 記法どおりの表現（シンボル／エスケープ 2 文字）へ逆正規化する。
     * <p>
     * NTF は読込時に実制御文字へ変換して保持するが、実制御文字のまま書き出すと NTF の {@code trim()} で
     * 失われ読み戻せないため、記法の表現へ戻す。区切り文字以外のキーは値をそのまま返す。
     * </p>
     *
     * @param key   ディレクティブキー
     * @param value 文字列化済みのディレクティブ値（non-null）
     * @return 逆正規化後の値
     */
    public static String normalizeSeparator(String key, String value) {
        if ("record-separator".equals(key)) {
            if ("\r\n".equals(value)) {
                return "CRLF";
            }
            if ("\n".equals(value)) {
                return "LF";
            }
            if ("\r".equals(value)) {
                return "CR";
            }
            if (value.isEmpty()) {
                return "NONE";
            }
            return value;
        }
        if ("field-separator".equals(key)) {
            return "\t".equals(value) ? "\\t" : value;
        }
        return value;
    }

    /**
     * キーと文字列値を受け取り、最終的な文字列値へ変換するインタフェース。
     */
    public interface ValueMapper {
        /**
         * @param key   ディレクティブキー
         * @param value {@link Object#toString()} で文字列化済みのディレクティブ値（non-null）
         * @return 変換後の文字列値
         */
        String map(String key, String value);
    }
}
