package nablarch.test.tool.converter.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link RecordLayout} のテスト。
 *
 * <p>レコード種別・フィールド定義・データ行を、加工・補完せず保持することを検証する。</p>
 */
public class RecordLayoutTest {

    @Test
    public void レコード種別とフィールドと行を保持する() {
        // Given
        List<FieldDef> fields = List.of(new FieldDef("id", "数値", "5"));
        List<List<String>> rows = List.of(List.of("00001"));

        // When
        RecordLayout sut = new RecordLayout("data", fields, rows);

        // Then: 参照そのまま保持（防御コピーしない＝読み取り専用契約）
        assertThat(sut.getRecordType(), is("data"));
        assertThat(sut.getFields(), is(sameInstance(fields)));
        assertThat(sut.getRows(), is(sameInstance(rows)));
    }

    @Test
    public void フィールド名称が重複したレコードは生成できない() {
        // Given: notation:887「同一レコード種別内でフィールド名称が重複している」は記述時のエラー（XLS-40）
        List<FieldDef> fields = List.of(
                new FieldDef("f1", "数値", "5"), new FieldDef("f1", "半角英字", "3"));
        try {
            new RecordLayout("data", fields, List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("フィールド名称"));
            assertThat(e.getMessage(), containsString("f1"));
        }
    }

    @Test
    public void 大文字小文字だけが違うフィールド名称は重複ではない() {
        // Given: 記法に大文字小文字を同一視する明文が無く、中間モデルは名前を大文字化せず保持する
        // When
        RecordLayout sut = new RecordLayout("data",
                List.of(new FieldDef("f1", "数値", "5"), new FieldDef("F1", "数値", "5")), List.of());

        // Then
        assertThat(sut.getFields().size(), is(2));
    }

    @Test
    public void フィールド定義の件数より要素数が多いデータ行を持つレコードは生成できない() {
        // Given: notation:891「データ要素数が不正である」はファイルデータの記述時エラー（XLS-41 の「多い側」）
        List<FieldDef> fields = List.of(
                new FieldDef("f1", "数値", "5"), new FieldDef("f2", "半角英字", "3"));
        List<List<String>> rows = List.of(Arrays.asList("1", "a", "余り"));
        try {
            new RecordLayout("data", fields, rows);
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("データ行"));
            assertThat(e.getMessage(), containsString("1 件目"));
            assertThat(e.getMessage(), containsString("3"));
            assertThat(e.getMessage(), containsString("2"));
        }
    }

    @Test
    public void フィールド定義の件数より要素数が少ないデータ行は保持できる() {
        // Given: notation:883 は「少ない側」を正常と定め、不足したフィールドは "" として補完されると書く。
        //        全フィールドを省略した行（YAML では rows: に空配列）も記法として明示的に案内している。
        //        したがって不変条件は「行の要素数 ≦ fields の件数」であり、一致の強制ではない（XLS-42）。
        List<FieldDef> fields = List.of(
                new FieldDef("f1", "数値", "5"), new FieldDef("f2", "半角英字", "3"));
        List<List<String>> rows = List.of(Arrays.asList("1"), Arrays.<String>asList());

        // When
        RecordLayout sut = new RecordLayout("data", fields, rows);

        // Then
        assertThat(sut.getRows(), is(rows));
    }

    @Test
    public void 何件目のデータ行が多すぎるかを例外メッセージに出す() {
        // Given: 2 件目だけが多い（先頭行だけ見て通す実装を落とす）
        List<FieldDef> fields = List.of(new FieldDef("f1", "数値", "5"));
        List<List<String>> rows = List.of(Arrays.asList("1"), Arrays.asList("2", "余り"));
        try {
            new RecordLayout("data", fields, rows);
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("2 件目"));
        }
    }

    @Test
    public void フィールド定義群がnullのレコードは生成できない() {
        // Given: 「無い」ことは 0 件のリストで表す（XLS-38）
        try {
            new RecordLayout("data", null, List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("フィールド定義のリスト"));
        }
    }

    @Test
    public void フィールド定義群にnullの要素を含むレコードは生成できない() {
        // Given
        List<FieldDef> fields = Arrays.asList(new FieldDef("id", "数値", "5"), null);
        try {
            new RecordLayout("data", fields, List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("フィールド定義のリスト"));
        }
    }

    @Test
    public void データ行リストがnullのレコードは生成できない() {
        try {
            new RecordLayout("data", List.of(new FieldDef("id", "数値", "5")), null);
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("データ行のリスト"));
        }
    }

    @Test
    public void セルがnullのデータ行は保持できる() {
        // Given: セルの null は記法にある（notation:767-772「セルに null」・:829-834
        //        「アンクォートの null」）。行そのものの null とは別である
        List<List<String>> rows = List.of(Arrays.asList("1", null));

        // When
        RecordLayout sut = new RecordLayout("data",
                List.of(new FieldDef("id", "数値", "5"), new FieldDef("name", "半角英字", "5")), rows);

        // Then
        assertThat(sut.getRows().get(0).get(1), is(nullValue()));
    }

    @Test
    public void レコード種別省略をnullで保持する() {
        // Given: record_type 省略
        // When
        RecordLayout sut = new RecordLayout(null, List.of(), List.of());

        // Then: デフォルト補完せず null のまま
        assertThat(sut.getRecordType(), is(nullValue()));
        assertThat(sut.getFields().isEmpty(), is(true));
        assertThat(sut.getRows().isEmpty(), is(true));
    }
}
