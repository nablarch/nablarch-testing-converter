package nablarch.test.tool.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * {@link DirectiveUtil#toStringDirectives} のテストクラス。
 * <p>
 * {@link DirectiveUtil#normalizeSeparator} は辺①・辺②の各リーダのテストが実入力で通しているため、
 * ここでは {@code toStringDirectives} の写しかた——キーの順序保持と、{@code null} 値を
 * {@code valueMapper} に渡さず {@code null} のまま通すこと——だけを担保する。
 * </p>
 *
 * @author kiyobot
 */
public class DirectiveUtilTest {

    /**
     * Given: 値が {@code null} のディレクティブと、非 {@code null} のディレクティブが混在する Map。
     * When : toStringDirectives を呼ぶ。
     * Then : {@code null} 値は {@code null} のまま残り（文字列 {@code "null"} へ化けない）、
     *        {@code valueMapper} はそのキーに対して呼ばれない。
     *
     * <p>担保：{@code value == null ? null : valueMapper.map(...)} の {@code null} 側。</p>
     */
    @Test
    public void nullValue_staysNullAndBypassesValueMapper() {
        // Given —— 定義順・辞書順とずらして並べる（順序保持を主張するため）
        Map<String, Object> directives = new LinkedHashMap<String, Object>();
        directives.put("zzz", "kept");
        directives.put("record-separator", null);
        directives.put("aaa", 1);

        final List<String> mapped = new ArrayList<String>();

        // When
        Map<String, String> result = DirectiveUtil.toStringDirectives(directives, new DirectiveUtil.ValueMapper() {
            @Override
            public String map(String key, String value) {
                mapped.add(key + "=" + value);
                return value + "!";
            }
        });

        // Then
        assertThat(result.size(), is(3));
        assertThat("null 値は null のまま", result.get("record-separator"), is(nullValue()));
        assertThat(result.get("zzz"), is("kept!"));
        assertThat(result.get("aaa"), is("1!"));
        assertThat("null のキーは valueMapper へ渡さない", mapped, is((List<String>) new ArrayList<String>(
                java.util.Arrays.asList("zzz=kept", "aaa=1"))));
        assertThat("キーの順序を保つ", new ArrayList<String>(result.keySet()),
                is((List<String>) new ArrayList<String>(
                        java.util.Arrays.asList("zzz", "record-separator", "aaa"))));
    }
}
