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

    /**
     * XLS-22 ／ YML-12 の 3 形目。フィールドを 1 件も持たないレコードレイアウトは生成できない。
     * 記法はフィールド名称リストまたは
     * データ型リストが未指定または空であるを記述時のエラーに挙げ、本体スキーマ
     * {@code $defs.record_fragment} は {@code fields} を必須かつ {@code minItems} ＝ 1 とする。
     *
     * <p>
     * <b>データ行も 0 件で確かめる。</b>データ行が 1 件でもあると XLS-41 の番人
     * （行の要素数 ≦ フィールド定義の件数）が先に落とすため、フィールド 0 件そのものを
     * 突いたことにならない。
     * </p>
     *
     * <p>
     * <b>レコード種別の有無では分岐しない</b>ためレコード種別 {@code null} 版のテストは置かない
     * （{@code null} をそのまま保持することは {@code #レコード種別省略をnullで保持する} が担保する）。
     * </p>
     */
    @Test
    public void フィールドを1件も持たないレコードは生成できない() {
        // Given / When / Then: フィールド 0 件・データ行 0 件
        try {
            new RecordLayout("data", List.of(), List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("フィールド定義は 1 件以上必要です"));
        }
    }

    @Test
    public void フィールド名称が重複したレコードは生成できない() {
        // Given: 同一レコード種別内でフィールド名称が重複していることは記述時のエラー（XLS-40）
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
        // Given: データ要素数が不正であることはファイルデータの記述時のエラー（XLS-41 の「多い側」）
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
        // Given: 記法は少ない側を正常と定め、不足したフィールドは "" として補完されると書く。
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
        // Given: セルの null は記法にある（記法はセルに null
        //        「アンクォートの null」）。行そのものの null とは別である
        List<List<String>> rows = List.of(Arrays.asList("1", null));

        // When
        RecordLayout sut = new RecordLayout("data",
                List.of(new FieldDef("id", "数値", "5"), new FieldDef("name", "半角英字", "5")), rows);

        // Then
        assertThat(sut.getRows().get(0).get(1), is(nullValue()));
    }

    /**
     * レコード種別の省略は {@code null} のまま保持し、デフォルト補完しない。
     *
     * <p>
     * <b>入力のフィールド定義を 0 件から 1 件へ書き直した（XLS-22・2026-08-19）。</b>
     * フィールド 0 件のレコードレイアウトは記法に存在しない形であり生成時に拒否するようにしたため、
     * 0 件のままでは本テストの意図（レコード種別の保持）に届く前に落ちる。
     * 番人を緩めるのではなく入力を記法どおりの形へ直す扱いは、§1-C ／ YML-12 ／ XLS-43 と同じである。
     * </p>
     */
    @Test
    public void レコード種別省略をnullで保持する() {
        // Given: record_type 省略
        // When
        RecordLayout sut = new RecordLayout(null, List.of(new FieldDef("id", "数値", "5")), List.of());

        // Then: デフォルト補完せず null のまま
        assertThat(sut.getRecordType(), is(nullValue()));
        assertThat(sut.getFields().size(), is(1));
        assertThat(sut.getRows().isEmpty(), is(true));
    }
}
