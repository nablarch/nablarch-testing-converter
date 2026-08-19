package nablarch.test.tool.converter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ディレクティブ変換に関するユーティリティ。
 *
 * <p>
 * {@link nablarch.test.tool.converter.xls.XlsFormatReader} および
 * {@link nablarch.test.tool.converter.yaml.YamlFormatReader} で共通して使われる
 * {@code Map<String, Object>} → {@code Map<String, String>} 変換（{@link #toStringDirectives}）と、
 * 区切り文字ディレクティブの値の逆正規化（{@link #normalizeSeparator}）を提供する。
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
     * null 値はここでは変換せず {@code null} のまま通す（文字列 {@code "null"} へ化けさせない）。
     * ただし<b>保持はされない</b>——戻り値を受け取る {@code FileDataBlock} ／ {@code MessageDataBlock} が
     * 生成時に {@code ModelPreconditions#requireNoNulls(String, Map)} で拒否するため、
     * 最終的には {@code IllegalArgumentException} になる（本体スキーマ {@code $defs.directives} の値型は
     * string ／ boolean ／ integer だけで {@code null} を許す定義が無い。{@code coverage/issues.md}
     * <b>XLS-43</b>）。
     * 非 null 値は {@code valueMapper} を介して文字列へ変換する。
     * </p>
     *
     * @param directives  本体ディレクティブ
     * @param valueMapper キーと {@link Object#toString()} 済みの値文字列を受け取り、
     *                    最終的な文字列値へ変換する関数。区切り文字ディレクティブについては
     *                    辺①（Excel）・辺②（YAML）とも {@link #normalizeSeparator} による逆正規化を
     *                    行う。辺①はそれ以外のキーで {@code QuotationTrimmer} 相当のクォート剥がしも
     *                    行うため、両者は同一ではない
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
     * 本体の器は {@code record-separator} を {@code LineSeparator.evaluate} で実改行（{@code \r\n} 等）へ、
     * {@code field-separator} の 2 文字記法を実タブへ変換して保持する。一方、記法が定めるのは
     * シンボル（{@code NONE}／{@code CR}／{@code LF}／{@code CRLF}）または任意のリテラル文字列
     * （{@code testdata_notation.rst:947-948}）と、タブを表す {@code field-separator=\t}
     * （{@code testdata_notation.rst:1080}）である。実制御文字のまま中間モデルへ入れると、
     * 書き出した値を本体が読み戻せない（{@code DataFile#setDirective} の {@code trim()} で失われる）。
     * </p>
     * <p>
     * 辺①（Excel）と辺②（YAML）で同じ入力表記が同じ中間モデル値になるよう、両者から呼ぶ
     * （{@code issues.md} YML-08）。区切り文字以外のキーは値をそのまま返す。
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
