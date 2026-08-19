package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link TestDataContainer} と {@link TestDataSection} のテスト。
 *
 * <p>テストクラス 1 つ分（ブック／ディレクトリ）と読み込み単位（シート／ファイル）の階層を保持することを検証する。</p>
 */
public class TestDataContainerTest {

    @Test
    public void コンテナは名前とセクション群を保持する() {
        // Given
        TestDataSection section = new TestDataSection("sheet1", List.of());
        List<TestDataSection> sections = List.of(section);

        // When
        TestDataContainer sut = new TestDataContainer("BookA", sections);

        // Then
        assertThat(sut.getName(), is("BookA"));
        assertThat(sut.getSections(), is(sameInstance(sections)));
    }

    @Test
    public void セクションは名前とブロック群を保持する() {
        // Given: セクション配下に複数データ種別のブロックが並ぶ
        TableDataBlock table = new TableDataBlock(
                DataType.SETUP_TABLE_DATA, "", "emp", List.of("id"), List.of(List.of("1")));
        List<TestDataBlock> blocks = List.of(table);

        // When
        TestDataSection sut = new TestDataSection("sheet1", blocks);

        // Then
        assertThat(sut.getName(), is("sheet1"));
        assertThat(sut.getBlocks(), is(sameInstance(blocks)));
        assertThat(sut.getBlocks().get(0), is(sameInstance(table)));
    }

    @Test
    public void 名前がnullの器は生成できない() {
        // Given: 器の名前はテストクラスと 1 対 1 に対応する引き当てキーであり
        //        （notation:44「同名の1つのExcelファイル（.xls または .xlsx）がテストクラスに対応し、
        //        1シートが読み込み単位に対応する」）、辺③はこれをそのままブック名にする。
        //        null では null.xlsx というファイルが黙って作られる（XLS-37）
        // When
        try {
            new TestDataContainer(null, List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            // Then
            assertThat(e.getMessage(), containsString("器の名前"));
        }
    }

    @Test
    public void 名前が空文字の器は生成できる() {
        // Given: 空文字は拒否しない。ブック名の書式は Excel 形式固有の制約であり
        //        中間モデルの不変条件ではない（TestDataSection と同じ扱い）
        // When
        TestDataContainer sut = new TestDataContainer("", List.of());

        // Then
        assertThat(sut.getName(), is(""));
    }

    @Test
    public void 名前がnullの読み込み単位は生成できない() {
        // Given: 読み込み単位の名前は呼び出し側がデータを引き当てるキーであり
        //        （notation:590「読み込み単位の名前（Excel 形式ではシート名、YAML 形式ではファイル名）と
        //        ID を指定して…取得できる」）、null では引けない。生成時点で拒否する
        // When
        try {
            new TestDataSection(null, List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            // Then
            assertThat(e.getMessage(), containsString("読み込み単位の名前"));
        }
    }
}
