package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import nablarch.test.core.db.TableData;
import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.FrameworkOracle;
import nablarch.test.core.reader.YamlFrameworkOracle;
import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;
import nablarch.test.tool.converter.yaml.YamlFormatReader;
import nablarch.test.tool.converter.yaml.YamlFormatWriter;
import nablarch.test.tool.converter.yaml.YamlTestDataValidator;
import nablarch.test.tool.converter.yaml.ValidationError;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * カラム名の行がマーカーカラムだけのデータブロックを、<b>実ファイルを起点に形式をまたいで</b>変換し、
 * エントリの数と並び・マーカーカラムの名前と値が保たれることを固定する。
 *
 * <p>
 * <b>正解は変換ツールのリーダではない。</b>エントリの数と並びはフレームワークの読み手
 * （Excel は {@code BasicTestDataParser}、YAML は {@code YamlTestDataParser}）から取り、
 * マーカーカラムの名前と値は成果物そのもの（{@code .xlsx} のセル・{@code .yaml} のロード結果）から取る。
 * 変換ツールの 2 つのリーダを突き合わせると、両方が同じ写し間違いを持つ欠陥を検知できない。
 * </p>
 *
 * <p>
 * <b>フレームワークはマーカーカラムの値を見ない。</b>マーカーカラムは読み込み対象から除外されるため、
 * フレームワークから取れる正解はエントリの数と並びだけである。名前と値はそれとは別に、成果物から確かめる。
 * </p>
 *
 * <p>
 * 各辺の単体の担保は {@code XlsMarkerOnlyBlockTest}（辺①）と {@code YamlMarkerOnlyBlockTest}（辺②）にある。
 * 本クラスは<b>形式をまたいだ通し</b>だけを扱う。
 * </p>
 */
public class MarkerOnlyBlockConversionTest {

    /** フィクスチャの既定ブック名。 */
    private static final String BOOK = "markerOnlyConvBook";

    /** シート名（YAML 側の読み込み単位名も兼ねる）。 */
    private static final String SHEET = "td";

    /** {@code LIST_MAP} の識別子。 */
    private static final String LIST_MAP_ID = "requestParams";

    /** テーブル名。 */
    private static final String TABLE = "T";

    /** マーカーカラム名。 */
    private static final String MARKER = "[no]";

    /** マーカーカラムの値（定義順・辞書順とずらしてある）。 */
    private static final List<String> VALUES = Arrays.asList("3", "1", "4", "2");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** 実ファイル経路で {@link YamlLoader} の LRU キャッシュをテスト間へ漏らさない。 */
    @After
    public void clearLoaderCache() {
        YamlLoader.clearCacheForTest();
    }

    // ------------------------------------------------------------------ helpers

    /**
     * 変換の段ごとのディレクトリ。
     *
     * @param step 段（1 始まり）
     * @return ディレクトリ
     */
    private Path dir(int step) {
        Path path = folder.getRoot().toPath().resolve("step" + step);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new UncheckedIOException("failed to create fixture dir: " + path, e);
        }
        return path;
    }

    /** Excel 側のリソース名。 */
    private static String xlsResource() {
        return BOOK + "/" + SHEET;
    }

    /**
     * マーカーカラムだけのブロック 1 件を持つ実 {@code .xlsx} を書き出す。
     *
     * @param dir    書き出し先
     * @param marker ブロックのマーカー行
     */
    private static void writeXls(Path dir, String marker) {
        XlsFixture fixture = XlsFixture.book(BOOK).sheet(SHEET)
                .row(text(marker))
                .row(text(MARKER));
        for (String value : VALUES) {
            fixture = fixture.row(text(value));
        }
        fixture.writeTo(dir);
    }

    /**
     * マーカーカラムだけの {@code list_maps} エントリ 1 件を持つ実 {@code .yaml} を書き出す。
     *
     * @param dir 書き出し先
     */
    private static void writeYaml(Path dir) {
        StringBuilder sb = new StringBuilder("list_maps:\n  - id: \"" + LIST_MAP_ID + "\"\n    rows:\n");
        for (String value : VALUES) {
            sb.append("      - \"").append(MARKER).append("\": \"").append(value).append("\"\n");
        }
        try {
            Files.write(dir.resolve(SHEET + ".yaml"), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("failed to write fixture: " + dir, e);
        }
    }

    /**
     * 中間モデルを、既定のブック名／シート名を持つコンテナへ包み直す。
     *
     * @param container 読み込んだコンテナ
     * @return 包み直したコンテナ
     */
    private static TestDataContainer rewrap(TestDataContainer container) {
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        return new TestDataContainer(BOOK, Collections.singletonList(new TestDataSection(SHEET, blocks)));
    }

    /**
     * 実 {@code .xlsx} を YAML 形式へ変換する。
     *
     * @param from 変換元ディレクトリ
     * @param to   変換先ディレクトリ
     */
    private static void xlsToYaml(Path from, Path to) {
        new YamlFormatWriter().write(rewrap(new XlsFormatReader().read(from.toString(), xlsResource())),
                to.toString());
    }

    /**
     * 実 {@code .yaml} を Excel 形式へ変換する。
     *
     * @param from 変換元ディレクトリ
     * @param to   変換先ディレクトリ
     */
    private static void yamlToXls(Path from, Path to) {
        new XlsFormatWriter().write(
                rewrap(new YamlFormatReader().read(from.toAbsolutePath().toString(), SHEET)), to.toString());
    }

    /**
     * 実 {@code .yaml} をフレームワークの YAML ローダで読み、{@code list_maps} の行を原文のまま取り出す。
     *
     * <p>変換ツールのリーダを通さずに、書き出された YAML の構造そのものを見るための口である。</p>
     *
     * @param dir ディレクトリ
     * @return 行（キー → 値）の一覧
     */
    private static List<Map<String, Object>> loadListMapRows(Path dir) {
        Map<String, Object> yaml = YamlLoader.load(dir.toAbsolutePath().toString(), SHEET);
        List<?> entries = (List<?>) yaml.get("list_maps");
        assertThat("list_maps のエントリ数", entries.size(), is(1));
        @SuppressWarnings("unchecked")
        Map<String, Object> entry = (Map<String, Object>) entries.get(0);
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (Object row : (List<?>) entry.get("rows")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) row;
            rows.add(map);
        }
        return rows;
    }

    /**
     * {@link #VALUES} を「1 マーカーカラムだけを持つ行」の一覧として並べたもの。
     *
     * @return 期待する行の一覧
     */
    private static List<Map<String, Object>> expectedRows() {
        List<Map<String, Object>> expected = new ArrayList<Map<String, Object>>();
        for (String value : VALUES) {
            expected.add(Collections.<String, Object>singletonMap(MARKER, value));
        }
        return expected;
    }

    // ------------------------------------------------------------------ テスト 1: oracle（Excel→YAML）

    /**
     * Given: マーカーカラム {@code [no]} だけの {@code LIST_MAP} を持つ実 {@code .xlsx}。
     * When : Excel → YAML へ変換し、変換前を本体 {@code BasicTestDataParser}、変換後を
     *        {@code YamlTestDataParser} に読ませる。
     * Then : エントリ数と並びが一致する。
     *
     * <p>
     * <b>フレームワークから見るとエントリはいずれも空のマップである</b>——マーカーカラムは読み込み対象から
     * 除外されるためで、意味を持つのはエントリの数と並びだけである。テストショット一覧と行の順序で
     * 対応付ける用途がそこに掛かっている。
     * </p>
     */
    @Test
    public void keepsListMapEntryCountAndOrderThroughXlsToYaml() {
        // Given
        writeXls(dir(1), "LIST_MAP=" + LIST_MAP_ID);
        List<Map<String, String>> before =
                FrameworkOracle.listMapViaTestDataParser(dir(1).toString(), xlsResource(), LIST_MAP_ID);

        // When
        xlsToYaml(dir(1), dir(2));
        List<Map<String, String>> after =
                YamlFrameworkOracle.listMap(dir(2).toAbsolutePath().toString(), SHEET, LIST_MAP_ID);

        // Then
        assertThat("変換前のエントリ数", before.size(), is(VALUES.size()));
        assertThat("変換後のエントリ数・並びが変換前と一致する", after, is(before));
    }

    /**
     * Given: マーカーカラム {@code [no]} だけの {@code SETUP_TABLE} を持つ実 {@code .xlsx}。
     * When : Excel → YAML へ変換し、変換前後をそれぞれの本体の読み手に読ませる。
     * Then : テーブルのエントリ数が一致する（テーブル系は経路が別なので個別に固定する）。
     */
    @Test
    public void keepsTableEntryCountThroughXlsToYaml() {
        // Given
        writeXls(dir(1), "SETUP_TABLE=" + TABLE);
        List<TableData> before =
                FrameworkOracle.setupTablesViaTestDataParser(dir(1).toString(), xlsResource());

        // When
        xlsToYaml(dir(1), dir(2));
        List<TableData> after = YamlFrameworkOracle.setupTables(dir(2).toAbsolutePath().toString(), SHEET);

        // Then
        assertThat("変換前のテーブル数", before.size(), is(1));
        assertThat("変換前のエントリ数", before.get(0).size(), is(VALUES.size()));
        assertThat("変換後のテーブル数", after.size(), is(1));
        assertThat("変換後のエントリ数が変換前と一致する", after.get(0).size(), is(before.get(0).size()));
        assertThat("変換後もマーカーカラムはカラム名に入らない",
                   Arrays.asList(after.get(0).getColumnNames()), is(Collections.<String>emptyList()));
    }

    // ------------------------------------------------------------------ テスト 2: 往復

    /**
     * Given: マーカーカラム {@code [no]} だけの {@code LIST_MAP} を持つ実 {@code .xlsx}。
     * When : Excel → YAML → Excel と往復させる。
     * Then : 最後の {@code .xlsx} のカラム名の行・データ行が最初の {@code .xlsx} と同じで、
     *        本体が読むエントリ数も変わらない。
     */
    @Test
    public void roundTripsMarkerOnlyListMapFromXls() {
        // Given
        writeXls(dir(1), "LIST_MAP=" + LIST_MAP_ID);

        // When
        xlsToYaml(dir(1), dir(2));
        yamlToXls(dir(2), dir(3));

        // Then
        Workbook workbook = XlsFixture.open(dir(3).resolve(BOOK + ".xlsx"));
        Sheet sheet = workbook.getSheetAt(0);
        assertThat("識別行", XlsFixture.cell(sheet, 0, 0), is("LIST_MAP=" + LIST_MAP_ID));
        assertThat("カラム名の行はマーカーカラム 1 件", XlsFixture.line(sheet, 1), is(Arrays.asList(MARKER)));
        for (int i = 0; i < VALUES.size(); i++) {
            assertThat("データ行 " + (i + 1), XlsFixture.line(sheet, 2 + i),
                       is(Arrays.asList(VALUES.get(i))));
        }
        assertThat("本体が読むエントリ数",
                   FrameworkOracle.listMapViaTestDataParser(dir(3).toString(), xlsResource(), LIST_MAP_ID).size(),
                   is(VALUES.size()));
    }

    /**
     * Given: マーカーカラム {@code [no]} だけの {@code list_maps} エントリを持つ実 {@code .yaml}。
     * When : YAML → Excel → YAML と往復させる。
     * Then : 最後の {@code .yaml} の行（キーと値・並び）が最初の {@code .yaml} と同じで、
     *        本体が読むエントリ数も変わらない。
     */
    @Test
    public void roundTripsMarkerOnlyListMapFromYaml() {
        // Given
        writeYaml(dir(1));
        assertThat("起点の YAML の行", loadListMapRows(dir(1)), is(expectedRows()));

        // When
        yamlToXls(dir(1), dir(2));
        xlsToYaml(dir(2), dir(3));

        // Then
        assertThat("往復後の YAML の行（キー・値・並び）が起点と一致する",
                   loadListMapRows(dir(3)), is(expectedRows()));
        assertThat("本体が読むエントリ数",
                   YamlFrameworkOracle.listMap(dir(3).toAbsolutePath().toString(), SHEET, LIST_MAP_ID).size(),
                   is(VALUES.size()));
    }

    // ------------------------------------------------------------------ テスト 3: スキーマ検証

    /**
     * Given: マーカーカラム {@code [no]} だけの {@code LIST_MAP} を持つ実 {@code .xlsx}。
     * When : Excel → YAML へ変換し、変換後のディレクトリをスキーマ検証に掛ける。
     * Then : 検証エラーが 1 件も出ない。
     *
     * <p>
     * {@code rows} の要素はキー名を制約しないオブジェクトであるため、マーカーカラムをキーにした行も
     * スキーマに適合する。
     * </p>
     */
    @Test
    public void convertedYamlPassesSchemaValidation() {
        // Given
        writeXls(dir(1), "LIST_MAP=" + LIST_MAP_ID);

        // When
        xlsToYaml(dir(1), dir(2));
        List<ValidationError> errors = new YamlTestDataValidator().validate(dir(2));

        // Then
        assertThat("スキーマ検証のエラー件数（内容: " + errors + "）", errors.size(), is(0));
    }

    /**
     * Given: マーカーカラム {@code [no]} だけの {@code SETUP_TABLE} を持つ実 {@code .xlsx}。
     * When : Excel → YAML へ変換し、変換後のディレクトリをスキーマ検証に掛ける。
     * Then : 検証エラーが 1 件も出ない（テーブル系のセクションも同じ）。
     */
    @Test
    public void convertedYamlTablePassesSchemaValidation() {
        // Given
        writeXls(dir(1), "SETUP_TABLE=" + TABLE);

        // When
        xlsToYaml(dir(1), dir(2));
        List<ValidationError> errors = new YamlTestDataValidator().validate(dir(2));

        // Then
        assertThat("スキーマ検証のエラー件数（内容: " + errors + "）", errors.size(), is(0));
    }

    // ------------------------------------------------------------------ テスト 4: 非回帰

    /**
     * Given: 実データカラム {@code id} とマーカーカラム {@code [no]} を持つ {@code LIST_MAP} の実 {@code .xlsx}。
     * When : Excel → YAML へ変換する。
     * Then : 変換後の YAML にマーカーカラムは残らない（従来どおり消える）。
     *
     * <p>
     * 名前と値を保つのは<b>カラム名の行がマーカーカラムだけのブロックに限る</b>。
     * </p>
     */
    @Test
    public void dropsMarkerColumnThroughConversionWhenBlockHasDataColumn() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("LIST_MAP=" + LIST_MAP_ID))
                .row(text(MARKER), text("id"))
                .row(text("1"), text("U0001"))
                .row(text("2"), text("U0002"))
                .writeTo(dir(1));

        // When
        xlsToYaml(dir(1), dir(2));

        // Then
        List<Map<String, Object>> rows = loadListMapRows(dir(2));
        assertThat("変換後の行数", rows.size(), is(2));
        assertThat("マーカーカラムは変換後に残らない", rows.get(0).containsKey(MARKER), is(false));
        assertThat("実データカラムだけが残る", rows.get(0).get("id"), is((Object) "U0001"));
        assertThat("実データカラムだけが残る", rows.get(1).get("id"), is((Object) "U0002"));
    }
}
