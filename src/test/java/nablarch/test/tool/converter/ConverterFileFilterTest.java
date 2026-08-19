package nablarch.test.tool.converter;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@link ConverterFileFilter} のテストクラス。
 *
 * <p>
 * 実ディレクトリ（{@link TemporaryFolder}）に対し、Excel ブック／YAML コンテナディレクトリの列挙、
 * 一時ロックファイル・非対象拡張子の除外、include／exclude グロブ、再現性のためのソート、
 * 入力ディレクトリ不在時の例外、拡張子違いの同名ブック同居の検出（XLS-28）を検証する。
 * </p>
 *
 * @author kiyobot
 */
public class ConverterFileFilterTest {

    /** 一時ディレクトリ */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** 入力ルート */
    private Path root;

    /**
     * 入力ルートを初期化する。
     */
    @Before
    public void setUp() {
        root = folder.getRoot().toPath();
    }

    /**
     * 指定相対パスに空ファイルを作成する（親ディレクトリは自動生成）。
     *
     * @param relative ルートからの相対パス
     * @return 作成したファイルの絶対パス
     * @throws IOException 入出力エラー
     */
    private Path touch(String relative) throws IOException {
        Path file = root.resolve(relative);
        Files.createDirectories(file.getParent());
        Files.createFile(file);
        return file;
    }

    // ---- findXlsFiles ----

    /**
     * Given: .xls/.xlsx・非対象・一時ロック・ネストを含むディレクトリ。
     * When : findXlsFiles（フィルタなし）。
     * Then : ブックのみを相対パス辞書順で返す（非対象・{@code ~$} 一時ファイルは除外）。
     */
    @Test
    public void findsXlsBooksSortedIgnoringNonBooks() throws IOException {
        // Given
        Path a = touch("A.xls");
        Path b = touch("B.xlsx");
        touch("notes.txt");
        touch("~$lock.xlsx");
        Path c = touch("sub/C.xls");

        // When
        List<Path> found = ConverterFileFilter.findXlsFiles(root, emptyList(), emptyList());

        // Then
        assertThat(found, is(Arrays.asList(a, b, c)));
    }

    /**
     * Given: include グロブ。
     * When : findXlsFiles。
     * Then : 合致したブックのみ返す。
     */
    @Test
    public void findXlsFilesAppliesInclude() throws IOException {
        // Given
        touch("A.xls");
        Path c = touch("sub/C.xls");

        // When
        List<Path> found = ConverterFileFilter.findXlsFiles(root, Arrays.asList("sub/**"), emptyList());

        // Then
        assertThat(found, is(Arrays.asList(c)));
    }

    /**
     * Given: exclude グロブ。
     * When : findXlsFiles。
     * Then : 合致したブックを除外する。
     */
    @Test
    public void findXlsFilesAppliesExclude() throws IOException {
        // Given
        Path a = touch("A.xls");
        Path b = touch("B.xlsx");
        touch("sub/C.xls");

        // When
        List<Path> found = ConverterFileFilter.findXlsFiles(root, emptyList(), Arrays.asList("**/C.xls"));

        // Then
        assertThat(found, is(Arrays.asList(a, b)));
    }

    /**
     * Given: 入力ディレクトリが存在しない。
     * When : findXlsFiles。
     * Then : ConverterException。
     */
    @Test
    public void findXlsFilesRejectsMissingDirectory() {
        // When / Then
        try {
            ConverterFileFilter.findXlsFiles(root.resolve("nope"), emptyList(), emptyList());
            fail("should throw");
        } catch (ConverterException e) {
            assertThat(e.getMessage().startsWith("input directory not found:"), is(true));
        }
    }

    /**
     * Given: 同一ディレクトリに拡張子違いの同名ブック（{@code Foo.xls} と {@code Foo.xlsx}）。
     * When : findXlsFiles。
     * Then : ConverterException で止まり、メッセージに両方のファイル名が出る。
     *
     * <p>
     * 記法は「同名の 1 つの Excel ファイル（{@code .xls} または {@code .xlsx}）がテストクラスに対応」と
     * 定めている（{@code notation:44}）。同居を通すと本体 {@code PoiXlsReader#open}
     * （{@code PoiXlsReader.java:62-65}。{@code .xls} を先に解決する）により
     * {@code .xlsx} の中身が読まれないまま {@code .xls} の中身が書き出される。
     * </p>
     */
    @Test
    public void findXlsFilesRejectsSameNameBooks() throws IOException {
        // Given
        touch("Foo.xls");
        touch("Foo.xlsx");

        // When / Then
        try {
            ConverterFileFilter.findXlsFiles(root, emptyList(), emptyList());
            fail("should throw");
        } catch (ConverterException e) {
            assertThat(e.getMessage().startsWith("same-name Excel books coexist:"), is(true));
            assertThat(e.getMessage().contains("Foo.xls,"), is(true));
            assertThat(e.getMessage().contains("Foo.xlsx"), is(true));
        }
    }

    /**
     * Given: 同名ブックの片方だけを exclude で外した。
     * When : findXlsFiles。
     * Then : それでも ConverterException で止まる。
     *
     * <p>
     * 本体 {@code PoiXlsReader#open} は実ディスクを見て {@code .xls} を先に解決するため、
     * exclude で列挙から外しても読み違いは起きる。したがって検出は列挙結果ではなく
     * 「変換対象になったブックの隣にいる同名ブック」で行う。
     * </p>
     */
    @Test
    public void findXlsFilesRejectsSameNameBooksEvenWhenOneIsExcluded() throws IOException {
        // Given
        touch("Foo.xls");
        touch("Foo.xlsx");

        // When / Then
        try {
            ConverterFileFilter.findXlsFiles(root, emptyList(), Arrays.asList("Foo.xlsx"));
            fail("should throw");
        } catch (ConverterException e) {
            assertThat(e.getMessage().startsWith("same-name Excel books coexist:"), is(true));
        }
    }

    /**
     * Given: 同名だがディレクトリが異なるブック。
     * When : findXlsFiles。
     * Then : 例外にならず両方を返す（{@code PoiXlsReader} の解決はディレクトリ単位のため衝突しない）。
     */
    @Test
    public void findXlsFilesAllowsSameNameBooksInDifferentDirectories() throws IOException {
        // Given
        Path a = touch("a/Foo.xls");
        Path b = touch("b/Foo.xlsx");

        // When
        List<Path> found = ConverterFileFilter.findXlsFiles(root, emptyList(), emptyList());

        // Then
        assertThat(found, is(Arrays.asList(a, b)));
    }

    /**
     * Given: 同名ブックの組が丸ごと変換対象外（include で別ディレクトリだけを対象にした）。
     * When : findXlsFiles。
     * Then : 例外にならない（読まれないブックの同居は読み違いを起こさない）。
     */
    @Test
    public void findXlsFilesAllowsSameNameBooksOutsideTargets() throws IOException {
        // Given
        touch("legacy/Foo.xls");
        touch("legacy/Foo.xlsx");
        Path current = touch("current/A.xls");

        // When
        List<Path> found = ConverterFileFilter.findXlsFiles(root, Arrays.asList("current/**"), emptyList());

        // Then
        assertThat(found, is(Arrays.asList(current)));
    }

    // ---- findYamlDirs ----

    /**
     * Given: 同一ディレクトリ内複数 YAML・ネスト・ルート直下 YAML・YAML 無しディレクトリ。
     * When : findYamlDirs（フィルタなし）。
     * Then : YAML を直下に持つディレクトリのみを重複なく辞書順で返す。
     */
    @Test
    public void findsYamlDirsDedupedAndSorted() throws IOException {
        // Given
        touch("d1/a.yaml");
        touch("d1/b.yaml");
        touch("d1/readme.txt");
        touch("d2/c.yaml");
        touch("d2/sub/e.yaml");
        touch("top.yaml");
        Files.createDirectories(root.resolve("empty"));

        // When
        List<Path> found = ConverterFileFilter.findYamlDirs(root, emptyList(), emptyList());

        // Then
        assertThat(found, is(Arrays.asList(
                root,
                root.resolve("d1"),
                root.resolve("d2"),
                root.resolve("d2/sub"))));
    }

    /**
     * Given: 複数の include グロブ。
     * When : findYamlDirs。
     * Then : いずれかに合致したディレクトリのみ返す。
     */
    @Test
    public void findYamlDirsAppliesInclude() throws IOException {
        // Given
        touch("d1/a.yaml");
        touch("d2/c.yaml");
        touch("d2/sub/e.yaml");

        // When
        List<Path> found = ConverterFileFilter.findYamlDirs(root, Arrays.asList("d2", "d2/**"), emptyList());

        // Then
        assertThat(found, is(Arrays.asList(root.resolve("d2"), root.resolve("d2/sub"))));
    }

    /**
     * Given: exclude グロブ。
     * When : findYamlDirs。
     * Then : 合致したディレクトリを除外する。
     */
    @Test
    public void findYamlDirsAppliesExclude() throws IOException {
        // Given
        touch("d1/a.yaml");
        touch("d2/c.yaml");

        // When
        List<Path> found = ConverterFileFilter.findYamlDirs(root, emptyList(), Arrays.asList("d1"));

        // Then
        assertThat(found, is(Arrays.asList(root.resolve("d2"))));
    }

    /**
     * Given: 入力ディレクトリが存在しない。
     * When : findYamlDirs。
     * Then : ConverterException。
     */
    @Test
    public void findYamlDirsRejectsMissingDirectory() {
        // When / Then
        try {
            ConverterFileFilter.findYamlDirs(root.resolve("nope"), emptyList(), emptyList());
            fail("should throw");
        } catch (ConverterException e) {
            assertThat(e.getMessage().startsWith("input directory not found:"), is(true));
        }
    }
}
