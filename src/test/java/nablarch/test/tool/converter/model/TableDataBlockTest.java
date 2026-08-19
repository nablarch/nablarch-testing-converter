package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link TableDataBlock} のテスト。
 *
 * <p>テーブルデータが、マーカーカラム・特殊記法・空文字・null セルを未加工で保持することを検証する。</p>
 */
public class TableDataBlockTest {

    @Test
    public void テーブル名とカラムと行を未加工で保持する() {
        // Given: マーカーカラム [COL] を含み、テーブル名は小文字のまま
        List<String> columns = List.of("[COL]", "id", "emp_name");
        // 行: null セル・空文字・特殊記法 ${...} をそれぞれ含む
        List<List<String>> rows = List.of(
                Arrays.asList(null, "1", "${systemTime}"),
                Arrays.asList("=", "2", ""));

        // When
        TableDataBlock sut = new TableDataBlock(
                DataType.SETUP_TABLE_DATA, "g1", "emp", columns, rows);

        // Then: dataType/groupId/識別子（テーブル名）は親から取得でき、値は加工されない
        assertThat(sut.getDataType(), is(DataType.SETUP_TABLE_DATA));
        assertThat(sut.getGroupId(), is("g1"));
        assertThat(sut.getIdentifier(), is("emp"));
        assertThat(sut.getColumnNames(), is(columns));
        assertThat(sut.getRows().get(0).get(0), is((String) null));
        assertThat(sut.getRows().get(0).get(2), is("${systemTime}"));
        assertThat(sut.getRows().get(1).get(2), is(""));
    }

    @Test
    public void カラム名リストがnullのブロックは生成できない() {
        // Given: カラム 0 個は空リストで表す（XLS-38）
        try {
            new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "emp", null, List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("カラム名リスト"));
        }
    }

    @Test
    public void カラム名にnullを含むブロックは生成できない() {
        try {
            new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "emp",
                    Arrays.asList("id", null), List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("カラム名リスト"));
        }
    }

    @Test
    public void データ行リストがnullのブロックは生成できない() {
        try {
            new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "emp", List.of("id"), null);
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("データ行のリスト"));
        }
    }

    @Test
    public void 行そのものがnullのブロックは生成できない() {
        // Given: 行の「セル」の null は記法にある（notation:767-772・:829-834）が、
        //        行そのものの null に当たる書き方は無い
        try {
            new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "emp", List.of("id"),
                    Arrays.asList(List.of("1"), null));
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("データ行のリスト"));
        }
    }

    @Test
    public void テーブル系の全データ種別を保持する() {
        // Given: テーブルが取りうる 3 種別（COMPLETE は省略カラムのデフォルト補完を本体側に委ね中間モデルでは補完しない）
        for (DataType dt : List.of(
                DataType.SETUP_TABLE_DATA, DataType.EXPECTED_TABLE_DATA, DataType.EXPECTED_COMPLETED)) {
            // When
            TableDataBlock sut = new TableDataBlock(dt, "", "emp", List.of("id"), List.of(List.of("1")));
            // Then
            assertThat(sut.getDataType(), is(dt));
        }
    }

    @Test
    public void 大文字混在のテーブル名とカラム名を大文字化せず保持する() {
        // Given: 本体器は大文字化するが中間モデルは記法のまま保持すべき
        // When
        TableDataBlock sut = new TableDataBlock(
                DataType.SETUP_TABLE_DATA, "", "EmpData",
                List.of("EmpName", "birth_Day"), List.of(List.of("a", "b")));

        // Then: 大文字小文字そのまま
        assertThat(sut.getIdentifier(), is("EmpData"));
        assertThat(sut.getColumnNames(), is(List.of("EmpName", "birth_Day")));
    }

    @Test
    public void カラムなし行なしを保持する() {
        // Given/When: 空のテーブル（カラム行・データ行とも空）
        TableDataBlock sut = new TableDataBlock(
                DataType.EXPECTED_TABLE_DATA, "", "emp", List.of(), List.of());

        // Then
        assertThat(sut.getColumnNames().isEmpty(), is(true));
        assertThat(sut.getRows().isEmpty(), is(true));
    }
}
