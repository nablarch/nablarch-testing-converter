package nablarch.test.core.reader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * {@link GroupIdNotation} のテストクラス。
 * <p>
 * 生値のグループ ID を上流 API が要求する整形済み表記へ写す 1 メソッドだけを持つ。
 * 「グループ指定なし」を表す 2 つの生値（空文字と {@code null}）が、どちらも空文字のまま渡ること
 * ——角括弧を付けないこと——を担保する。
 * </p>
 *
 * @author kiyobot
 */
public class GroupIdNotationTest {

    /**
     * Given: グループ ID の生値 {@code "case01"}。
     * When : format を呼ぶ。
     * Then : 角括弧で囲んだ {@code "[case01]"} が返る。
     */
    @Test
    public void rawGroupId_isWrappedInBrackets() {
        assertThat(GroupIdNotation.format("case01"), is("[case01]"));
    }

    /**
     * Given: 「グループ指定なし」を表す生値の空文字。
     * When : format を呼ぶ。
     * Then : 空文字のまま返る（{@code "[]"} にしない）。
     */
    @Test
    public void emptyRawGroupId_staysEmpty() {
        assertThat(GroupIdNotation.format(""), is(""));
    }

    /**
     * Given: 「グループ指定なし」を表す {@code null}。
     * When : format を呼ぶ。
     * Then : 空文字が返る（{@code null} を素通しせず、{@code "[null]"} にもしない）。
     */
    @Test
    public void nullRawGroupId_becomesEmpty() {
        assertThat(GroupIdNotation.format(null), is(""));
    }
}
