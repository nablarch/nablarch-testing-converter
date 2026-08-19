package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link TestDataBlock} の共通契約のテスト。
 *
 * <p>
 * {@code groupId} が {@code null} のブロックを生成できないことを、sealed 階層の 3 つの直接サブクラス
 * （{@link ColumnRowDataBlock} ／ {@link FileDataBlock} ／ {@link MessageDataBlock}）それぞれについて
 * 検証する。契約は「グループ ID は省略時は空文字」であり、{@code null} は呼び出し側のバグである。
 * </p>
 *
 * <p>
 * 方針は {@code steering.md} Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」。
 * 番人は辺③④（書き出し）には置かない。
 * </p>
 */
public class TestDataBlockTest {

    @Test
    public void グループIDがnullのテーブル系ブロックは生成できない() {
        // Given / When
        try {
            new TableDataBlock(DataType.SETUP_TABLE_DATA, null, "emp",
                    List.of("id"), List.of(List.of("1")));
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            // Then
            assertThat(e.getMessage(), containsString("グループ ID"));
        }
    }

    @Test
    public void グループIDがnullのLISTMAPブロックは生成できない() {
        // Given / When
        try {
            new ListMapBlock(null, "listMapId", List.of("id"), List.of(List.of("1")));
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            // Then
            assertThat(e.getMessage(), containsString("グループ ID"));
        }
    }

    @Test
    public void グループIDがnullのファイルブロックは生成できない() {
        // Given / When
        try {
            new FileDataBlock(DataType.SETUP_FIXED, null, "in.dat",
                    FileDataBlock.FileType.FIXED, Map.of(), List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            // Then
            assertThat(e.getMessage(), containsString("グループ ID"));
        }
    }

    @Test
    public void グループIDがnullの電文ブロックは生成できない() {
        // Given / When
        try {
            new MessageDataBlock(DataType.MESSAGE, null, "msg1",
                    Map.of(), Map.of(), List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            // Then
            assertThat(e.getMessage(), containsString("グループ ID"));
        }
    }

    @Test
    public void データタイプDEFAULTのブロックは生成できない() {
        // Given: DEFAULT は記法のデータタイプ表に載っているが（notation:188-190「フレームワーク内部用
        //        （通常は使用しない）」）、YAML のトップレベルキー対応表（notation:206-241）に行が無く、
        //        YAML では表現できない。中間モデルの契約は 4 辺すべてが表現できる範囲で定める
        // When
        try {
            new TableDataBlock(DataType.DEFAULT, "", "T", List.of("C1"), List.of(List.of("v1")));
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            // Then
            assertThat(e.getMessage(), containsString("DEFAULT"));
        }
    }

    @Test
    public void データ種別がnullのデータブロックは生成できない() {
        // Given: データブロックは必ず 1 つのデータタイプを持つ。Excel はマーカー（notation:198
        //        「データタイプ=識別子の値」）、YAML はトップレベルキー（notation:206-241）が
        //        データタイプから決まるため、データタイプの無いブロックはどちらの形式でも書けない
        // When
        try {
            new TableDataBlock(null, "", "emp", List.of("id"), List.of(List.of("1")));
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            // Then
            assertThat(e.getMessage(), containsString("データ種別"));
        }
    }

    @Test
    public void グループID省略は空文字で表しデフォルトグループとする() {
        // Given: 省略は空文字（notation:254「グループIDを省略した場合は、グループIDを持たない
        //        データブロック（デフォルトグループ）が対象になる」）
        // When
        TableDataBlock sut = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "emp",
                List.of("id"), List.of(List.of("1")));

        // Then: 空文字は拒否せず、そのまま保持する
        assertThat(sut.getGroupId(), is(""));
    }
}
