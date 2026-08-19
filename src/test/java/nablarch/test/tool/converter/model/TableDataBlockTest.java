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

    /**
     * カラム名 0 件かつ行 0 件は<b>正当な形</b>である。YAML の 0 件テーブル
     * （{@code testdata_notation.rst:836}「0 件のデータは、{@code rows:} に空配列 {@code []} を記載する」。
     * {@code 30a8271} 時点）はカラム名を書く場所を持たないため（{@code :819}）、
     * 辺②が仕様適合入力からこの形を作る（{@code coverage/issues.md} <b>XLS-27</b>）。
     */
    @Test
    public void カラムなし行なしを保持する() {
        // Given/When: 空のテーブル（カラム行・データ行とも空）
        TableDataBlock sut = new TableDataBlock(
                DataType.EXPECTED_TABLE_DATA, "", "emp", List.of(), List.of());

        // Then
        assertThat(sut.getColumnNames().isEmpty(), is(true));
        assertThat(sut.getRows().isEmpty(), is(true));
    }

    /**
     * XLS-21。<b>カラム名 0 件で「セルを持つ行」を抱えるブロックは生成できない。</b>
     * {@code testdata_notation.rst:652}（{@code 30a8271} 時点）はテーブル系データを
     * 「データタイプと識別子の値・カラム名・データ行という共通の構成を持つ」と定め、
     * YAML はカラム名を {@code rows:} の先頭要素のキーで決める（{@code :819}）ため、
     * 行があってカラム名が無い形は書けない。Excel も {@code :802} によりカラム名の行を省略できず、
     * カラム名 0 件で行だけを書くと<b>データ行がカラム名へ昇格して値が消える</b>。
     */
    @Test
    public void カラムなしでセルを持つ行を抱えるブロックは生成できない() {
        // Given / When / Then
        try {
            new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                    List.of(), List.of(List.of("v1", "v2")));
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                    containsString("カラム名を 1 件も持たないブロックはセルを持つデータ行を持てません"));
        }
    }

    /**
     * XLS-08 ／ YML-04。<b>カラム名 0 件でセルを 1 つも持たない行は拒否しない。</b>
     * マーカーカラムだけのブロックが {@code testdata_notation.rst:1550}（{@code 30a8271} 時点）の
     * 除外を受けるとこの形になり、辺①・辺②が仕様適合入力から実際に作る。値を持たないため
     * 値の消失は起きない。
     */
    @Test
    public void カラムなしでセルを持たない行は保持する() {
        // Given / When
        TableDataBlock sut = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                List.of(), List.of(List.of(), List.of()));

        // Then
        assertThat(sut.getColumnNames().isEmpty(), is(true));
        assertThat(sut.getRows().size(), is(2));
    }
}
