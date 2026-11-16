package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

/**
 * {@link ListMapBlock} のテスト。
 *
 * <p>LIST_MAP が常に {@link DataType#LIST_MAP} を持ち、YAML 記述順のカラム・空行を保持することを検証する。</p>
 */
public class ListMapBlockTest {

    @Test
    public void データ種別は常にLIST_MAPで識別子とカラムと行を保持する() {
        // Given: LIST_MAP は groupId を持たない（空文字）。ID は識別子に保持
        List<String> columns = List.of("key", "value");
        List<List<String>> rows = List.of(List.of("a", "1"));

        // When
        ListMapBlock sut = new ListMapBlock("", "result1", columns, rows);

        // Then: コンストラクタで dataType を渡さずとも LIST_MAP 固定
        assertThat(sut.getDataType(), is(DataType.LIST_MAP));
        assertThat(sut.getGroupId(), is(""));
        assertThat(sut.getIdentifier(), is("result1"));
        assertThat(sut.getColumnNames(), is(columns));
        assertThat(sut.getRows(), is(rows));
    }

    @Test
    public void 空行を空リストとして保持する() {
        // Given: 空マッピング {} 由来の空行（構造層が [] として残す挙動の保持）
        List<List<String>> rows = List.of(List.of());

        // When
        ListMapBlock sut = new ListMapBlock("", "empty", List.of("k"), rows);

        // Then: 行は存在し中身は空
        assertThat(sut.getRows().size(), is(1));
        assertThat(sut.getRows().get(0).isEmpty(), is(true));
    }

    @Test
    public void グループIDを持つLISTMAPブロックは生成できない() {
        // Given / When: フレームワークは LIST_MAP を ID だけで引き当てるため、中間モデルはグループ ID を持たない
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new ListMapBlock("g1", "lm", List.of("k"), List.of(List.of("v"))));

        // Then
        assertThat(e.getMessage(), containsString("グループ ID を持てません"));
        assertThat(e.getMessage(), containsString("groupId=[g1]"));
    }
}
