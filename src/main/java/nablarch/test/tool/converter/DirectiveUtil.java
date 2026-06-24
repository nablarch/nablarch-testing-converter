package nablarch.test.tool.converter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ディレクティブ変換に関するユーティリティ。
 *
 * <p>
 * {@link nablarch.test.tool.converter.xls.XlsFormatReader} および
 * {@link nablarch.test.tool.converter.yaml.YamlFormatReader} で共通して使われる
 * {@code Map<String, Object>} → {@code Map<String, String>} 変換のロジックを提供する。
 * </p>
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
     * null 値は {@code null} のまま保持する（文字列 {@code "null"} へ化けさせない）。
     * 非 null 値は {@code valueMapper} を介して文字列へ変換する。
     * </p>
     *
     * @param directives  本体ディレクティブ
     * @param valueMapper キーと {@link Object#toString()} 済みの値文字列を受け取り、
     *                    最終的な文字列値へ変換する関数（XLS 版では逆正規化を行う）
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
