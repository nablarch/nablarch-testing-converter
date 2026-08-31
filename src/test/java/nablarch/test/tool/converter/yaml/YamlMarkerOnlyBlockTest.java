package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import nablarch.test.core.db.TableData;
import nablarch.test.core.reader.YamlFrameworkOracle;
import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * <b>カラム名がマーカーカラムだけで構成されたエントリ</b>を、マーカーカラムの名前と各行の値を保ったまま
 * 中間モデルへ写すことを、実 {@code .yaml} を入力に固定する。辺①（{@code XlsMarkerOnlyBlockTest}）と
 * 対称の担保である。
 *
 * <p>
 * このブロックの各エントリはフィールドを持たない。それでもエントリの数と並びは意味を持つ——
 * テストショット一覧と行の順序で対応付ける用途があるためである。フレームワークの YAML 読み込みは、
 * 行を取り除くかどうかを「値を 1 つも持たない行（空マッピング {@code {} }）か」だけで決めるため、
 * マーカーカラムだけの行も落とさない。
 * </p>
 *
 * <p>
 * <b>値は YAML の原文そのものである。</b>YAML 読み込みはマーカーカラムを値加工より前に読み飛ばすうえ、
 * 変換ツールの読みはインタープリタを 1 つも積まない。辺①（Excel）が本体の解釈を通した値を持つのと
 * 同じく、辺②はその形式で「値」と決まっているものを持つ。
 * </p>
 *
 * <p>
 * <b>期待値の件数はテストが書かず、フレームワークの YAML 読み手（{@code YamlTestDataParser}）から取り出す。</b>
 * 変換ツール自身の読みを正解にしない。
 * </p>
 *
 * <p>
 * 実データカラムを 1 つでも持つエントリのマーカーカラムが従来どおり落ちること（非回帰）も本クラスで固定する。
 * </p>
 */
public class YamlMarkerOnlyBlockTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** 実 {@link YamlFormatReader} を通すため、{@link YamlLoader} の LRU キャッシュをテスト間で残さない。 */
    @After
    public void clearLoaderCache() {
        YamlLoader.clearCacheForTest();
    }

    /**
     * フィクスチャ {@code .yaml} の出力先ディレクトリ。
     *
     * @return ディレクトリ
     */
    private Path dir() {
        return folder.getRoot().toPath();
    }

    /**
     * フィクスチャの取得元パス（本体の YAML 読み手へ渡す形）。
     *
     * @return 取得元パス
     */
    private String oracleDir() {
        return dir().toAbsolutePath().toString();
    }

    // ------------------------------------------------------------------ LIST_MAP

    /**
     * Given: カラム名がマーカーカラム {@code [no]} だけの {@code list_maps} エントリ。
     *        値は定義順・辞書順とずらして並べる。
     * When : 実 {@code .yaml} を変換ツールと本体それぞれに読ませる。
     * Then : マーカーカラムの名前と各行の値が記述順で残り、エントリ数は本体の読みと一致する。
     */
    @Test
    public void keepsMarkerOnlyColumnsAndValuesInListMap() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(),
                "list_maps:\n"
                        + "  - id: \"requestParams\"\n"
                        + "    rows:\n"
                        + "      - \"[no]\": \"3\"\n"
                        + "      - \"[no]\": \"1\"\n"
                        + "      - \"[no]\": \"4\"\n"
                        + "      - \"[no]\": \"2\"\n");

        // Then
        ListMapBlock block = YamlFixture.onlyBlock(container, ListMapBlock.class);
        List<Map<String, String>> oracle = YamlFrameworkOracle.listMap(oracleDir(), YamlFixture.RESOURCE,
                                                                       "requestParams");
        assertThat("本体が読むエントリ数", oracle.size(), is(4));
        assertThat("本体はマーカーカラムを値に持たない", oracle.get(0).isEmpty(), is(true));
        assertThat("マーカーカラムの名前が残る", block.getColumnNames(), is(Arrays.asList("[no]")));
        assertThat("エントリ数が本体と一致する", block.getRows().size(), is(oracle.size()));
        assertThat("マーカーカラムの値が記述順で残る", block.getRows(), is(Arrays.asList(
                Arrays.asList("3"),
                Arrays.asList("1"),
                Arrays.asList("4"),
                Arrays.asList("2"))));
    }

    // ------------------------------------------------------------------ テーブル系

    /**
     * Given: カラム名がマーカーカラム {@code [no]}・{@code [memo]} だけの {@code setup_tables} エントリ。
     * When : 実 {@code .yaml} を変換ツールと本体それぞれに読ませる。
     * Then : マーカーカラムの名前が記述順で残り、各行の値も記述順で残る。エントリ数は本体の読みと一致する
     *        （テーブル系は経路が別なので個別に固定する）。
     *
     * <p>
     * <b>マーカーカラムの名前は大文字化されない。</b>テーブル系のカラム名を大文字へ揃えるのは
     * 本体の器（{@code TableData}）であり、マーカーカラムはその器に入らないためである。
     * Excel 形式でも同じ理由でマーカーカラム名はそのまま残る。
     * </p>
     */
    @Test
    public void keepsMarkerOnlyColumnsAndValuesInTable() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(),
                "setup_tables:\n"
                        + "  - table: \"T\"\n"
                        + "    rows:\n"
                        + "      - \"[no]\": \"3\"\n"
                        + "        \"[memo]\": \"c\"\n"
                        + "      - \"[no]\": \"1\"\n"
                        + "        \"[memo]\": \"a\"\n"
                        + "      - \"[no]\": \"2\"\n"
                        + "        \"[memo]\": \"b\"\n");

        // Then
        TableDataBlock block = YamlFixture.onlyBlock(container, TableDataBlock.class);
        List<TableData> oracle = YamlFrameworkOracle.setupTables(oracleDir(), YamlFixture.RESOURCE);
        assertThat("本体が読むテーブル数", oracle.size(), is(1));
        assertThat("本体はマーカーカラムをカラム名に持たない",
                   Arrays.asList(oracle.get(0).getColumnNames()), is(Collections.<String>emptyList()));
        assertThat("本体が読むエントリ数", oracle.get(0).size(), is(3));
        assertThat("マーカーカラムの名前が記述順で残る",
                   block.getColumnNames(), is(Arrays.asList("[no]", "[memo]")));
        assertThat("エントリ数が本体と一致する", block.getRows().size(), is(oracle.get(0).size()));
        assertThat("マーカーカラムの値が記述順で残る", block.getRows(), is(Arrays.asList(
                Arrays.asList("3", "c"),
                Arrays.asList("1", "a"),
                Arrays.asList("2", "b"))));
    }

    // ------------------------------------------------------------------ 非回帰

    /**
     * Given: 実データカラム {@code id} を持ち、あわせてマーカーカラム {@code [no]} を書いた
     *        {@code list_maps} エントリ。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : マーカーカラムはカラム名から落ち、実データカラムだけが残る（従来どおり）。
     *
     * <p>
     * 名前と値を保つのは<b>カラム名がマーカーカラムだけのブロックに限る</b>。実データカラムを持つ
     * ブロックの扱いは変えていないことを固定する。
     * </p>
     */
    @Test
    public void dropsMarkerColumnWhenListMapHasDataColumn() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(),
                "list_maps:\n"
                        + "  - id: \"lm\"\n"
                        + "    rows:\n"
                        + "      - \"[no]\": \"1\"\n"
                        + "        id: \"U0001\"\n"
                        + "      - \"[no]\": \"2\"\n"
                        + "        id: \"U0002\"\n");

        // Then
        ListMapBlock block = YamlFixture.onlyBlock(container, ListMapBlock.class);
        assertThat("マーカーカラムはカラム名から落ちる", block.getColumnNames(), is(Arrays.asList("id")));
        assertThat("実データカラムの値だけが残る", block.getRows(),
                   is(Arrays.asList(Arrays.asList("U0001"), Arrays.asList("U0002"))));
    }

    /**
     * Given: 実データカラム {@code C1} を持ち、あわせてマーカーカラム {@code [no]} を書いた
     *        {@code setup_tables} エントリ。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : マーカーカラムはカラム名から落ち、実データカラムだけが残る（テーブル系の経路も同じ）。
     */
    @Test
    public void dropsMarkerColumnWhenTableHasDataColumn() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(),
                "setup_tables:\n"
                        + "  - table: \"T\"\n"
                        + "    rows:\n"
                        + "      - \"[no]\": \"1\"\n"
                        + "        C1: \"v1\"\n"
                        + "      - \"[no]\": \"2\"\n"
                        + "        C1: \"v2\"\n");

        // Then
        TableDataBlock block = YamlFixture.onlyBlock(container, TableDataBlock.class);
        assertThat("マーカーカラムはカラム名から落ちる", block.getColumnNames(), is(Arrays.asList("C1")));
        assertThat("実データカラムの値だけが残る", block.getRows(),
                   is(Arrays.asList(Arrays.asList("v1"), Arrays.asList("v2"))));
    }
}
