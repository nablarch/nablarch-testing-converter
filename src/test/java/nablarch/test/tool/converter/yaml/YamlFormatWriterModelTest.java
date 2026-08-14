package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.core.reader.yaml.YamlSchemaValidationException;
import nablarch.test.tool.converter.model.FieldDef;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.MessageDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import com.networknt.schema.ValidationMessage;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 辺④（中間モデル→YAML）の軸A・C・E — {@code YamlFormatWriterTest} が通していない中間モデルの形を
 * 固定するテスト。
 *
 * <p>
 * 対象は {@code .rn/ntf-test-data-converter/coverage/inventory.md} §4.3 が「要追加」としていた
 * 軸A 2 件（A-07 {@code EXPECTED_FIXED}／A-08 {@code SETUP_VARIABLE}）・軸C 2 件
 * （C-02 {@code sections} 空・複数／C-12 {@code FileDataBlock.records} 空）・軸E 1 件
 * （E-4 コンテナ内セクション数 複数）である。
 * </p>
 *
 * <p>
 * <b>あわせて「書けるが読み戻せない YAML」を固定する。</b>{@link YamlFormatWriter} は本体スキーマ
 * （yaml jar 内 {@code nablarch/test/ntf-testdata-yaml-schema.json}）を参照しないため、
 * スキーマが禁じる形の中間モデルを渡すと、例外にならずスキーマ違反の {@code .yaml} を書き出す。
 * 該当する 4 つの形（{@code records} 空 2 種／{@code fields} 空／{@code FieldDef.type} 省略）は
 * いずれも辺②では<b>到達不能</b>と判定済みであり（{@code inventory.md} §2.3）、
 * 辺④からだけ作れる。{@code issues.md} <b>YML-12</b> に記録した。
 * </p>
 *
 * <p>
 * 順序を主張するフィクスチャは、<b>定義順・辞書順のいずれとも違う並び</b>で組み立てている
 * （セクションキーは {@code setup_files} → {@code expected_files} の順＝ {@code DataType} の定義順とも
 * 辞書順とも逆、ディレクティブは {@code text-encoding} → {@code file-type} の順＝辞書順の逆、
 * カラムは {@code zip} → {@code name} の順＝辞書順の逆）。並びが辞書順・定義順と一致していると、
 * 順序を壊す変更を入れてもテストが通ってしまうためである。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>期待される仕様ではない。
 * 妥当でないと判断した挙動は {@code .rn/ntf-test-data-converter/coverage/issues.md} に課題
 * （{@code YML-12}）として記録してあり、実装（src/main）は変更していない。
 * </p>
 *
 * @author kiyobot
 */
public class YamlFormatWriterModelTest {

    /** {@link YamlFormatWriter} が付ける拡張子。 */
    private static final String EXTENSION = ".yaml";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final YamlFormatWriter writer = new YamlFormatWriter();

    /** 読み戻しで実 {@link YamlFormatReader} を通すため、{@link YamlLoader} の LRU キャッシュをテスト間で残さない。 */
    @After
    public void clearLoaderCache() {
        YamlLoader.clearCacheForTest();
    }

    // ------------------------------------------------------------------ helpers

    /**
     * ブロック群を 1 セクション（名前 {@code td}）に包んで直列化する。
     *
     * @param blocks ブロック（記述順）
     * @return YAML テキスト
     */
    private String serialize(TestDataBlock... blocks) {
        return writer.serialize(section("td", blocks));
    }

    /**
     * ブロック群を 1 セクションに包む。
     *
     * @param name   セクション名（＝出力ファイル名）
     * @param blocks ブロック（記述順）
     * @return セクション
     */
    private static TestDataSection section(String name, TestDataBlock... blocks) {
        return new TestDataSection(name, Arrays.asList(blocks));
    }

    /**
     * ブロック 1 件を実ファイルへ書き出し、本番配線の {@link YamlFormatReader} で読み戻して
     * 唯一のブロックを返す。
     *
     * @param block ブロック
     * @return 読み戻したブロック
     */
    private TestDataBlock writeAndReadBack(TestDataBlock block) {
        String base = write(block);
        List<TestDataBlock> blocks = new YamlFormatReader().read(base, "td").getSections().get(0).getBlocks();
        assertThat("読み戻したブロック数", blocks.size(), is(1));
        return blocks.get(0);
    }

    /**
     * ブロック 1 件を実ファイル（{@code <一時フォルダ>/td.yaml}）へ書き出す。
     *
     * @param block ブロック
     * @return 出力先ディレクトリ（絶対パス）
     */
    private String write(TestDataBlock block) {
        String base = folder.getRoot().getAbsolutePath();
        writer.write(new TestDataContainer("td",
                Collections.singletonList(section("td", block))), base);
        return base;
    }

    /**
     * ブロックを書き出し、読み戻しがスキーマ違反で失敗することを確かめる。
     *
     * @param block ブロック
     * @return 送出された例外
     */
    private YamlSchemaValidationException assertFailsToReadBack(TestDataBlock block) {
        String base = write(block);
        return assertThrows(YamlSchemaValidationException.class,
                () -> new YamlFormatReader().read(base, "td"));
    }

    /**
     * 違反のキーワード（{@code required} / {@code minItems} など）をロケール非依存に取り出す。
     *
     * <p>{@code YamlFormatReaderInvalidInputTest#types} と同じ理由（メッセージ本文は言語設定に依存する）。</p>
     *
     * @param e 送出された例外
     * @return 違反のキーワード
     */
    private static List<String> types(YamlSchemaValidationException e) {
        List<String> types = new ArrayList<>();
        for (ValidationMessage message : e.getErrors()) {
            types.add(message.getType());
        }
        return types;
    }

    /**
     * 違反位置（{@code $.setup_files[0]} など）をロケール非依存に取り出す。
     *
     * @param e 送出された例外
     * @return 違反位置
     */
    private static List<String> locations(YamlSchemaValidationException e) {
        List<String> locations = new ArrayList<>();
        for (ValidationMessage message : e.getErrors()) {
            locations.add(message.getInstanceLocation().toString());
        }
        return locations;
    }

    /**
     * 値 1 行 1 フィールドのレコードレイアウトを組み立てる（{@code record_type} は省略）。
     *
     * @return レコードレイアウト
     */
    private static RecordLayout record() {
        return new RecordLayout(null,
                Collections.singletonList(new FieldDef("f1", "半角英字", "5")),
                Collections.singletonList(Collections.singletonList("v")));
    }

    /**
     * 記述順を保つ文字列マップを組み立てる。
     *
     * @param kv キーと値を交互に並べたもの
     * @return マップ
     */
    private static Map<String, String> map(String... kv) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return m;
    }

    // ------------------------------------------------------------------ A-07 / A-08

    /**
     * Given: {@code SETUP_VARIABLE}（可変長）と {@code EXPECTED_FIXED}（固定長）を<b>この順</b>で持つセクション。
     * When : {@code serialize}。
     * Then : {@code setup_files:} → {@code expected_files:} の順に、空行で区切って書かれる。
     *
     * <p>
     * 担保する軸要素: A-07（{@code EXPECTED_FIXED} → {@code expected_files}）／
     * A-08（{@code SETUP_VARIABLE} → {@code setup_files}）／E-1(複数)。
     * §4.1 では 2 種とも {@code RoundTripTest} 経由の 🔺 だけだった。
     * </p>
     *
     * <p>
     * 入力の並びは {@code DataType} の定義順（{@code EXPECTED_FIXED} が {@code SETUP_VARIABLE} より前）とも、
     * セクションキーの辞書順（{@code expected_files} が {@code setup_files} より前）とも<b>逆</b>である。
     * 初出順で並べる実装をソートへ変えれば落ちる。
     * </p>
     */
    @Test
    public void writesSetupVariableAndExpectedFixedUnderTheirSectionKeysInEncounterOrder() {
        // Given
        FileDataBlock setupVariable = new FileDataBlock(DataType.SETUP_VARIABLE, "", "s.csv",
                FileDataBlock.FileType.VARIABLE, map(), Collections.singletonList(record()));
        FileDataBlock expectedFixed = new FileDataBlock(DataType.EXPECTED_FIXED, "", "e.dat",
                FileDataBlock.FileType.FIXED, map(), Collections.singletonList(record()));

        // When / Then
        assertThat(serialize(setupVariable, expectedFixed), is(""
                + "setup_files:\n"
                + "  - path: \"s.csv\"\n"
                + "    type: \"variable\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"5\"}\n"
                + "        rows:\n"
                + "          - [\"v\"]\n"
                + "\n"
                + "expected_files:\n"
                + "  - path: \"e.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"5\"}\n"
                + "        rows:\n"
                + "          - [\"v\"]\n"));
    }

    /**
     * Given: {@code EXPECTED_FIXED} のファイルブロック。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : {@code EXPECTED_FIXED} のまま復元される（{@code EXPECTED_VARIABLE} と混ざらない）。
     *
     * <p>担保する軸要素: A-07（往復）。同じ {@code expected_files} キーを共有する
     * {@code EXPECTED_VARIABLE} と区別されるのは {@code type: "fixed"} による。</p>
     */
    @Test
    public void restoresExpectedFixedDataTypeThroughRealReader() {
        // Given
        FileDataBlock block = new FileDataBlock(DataType.EXPECTED_FIXED, "", "e.dat",
                FileDataBlock.FileType.FIXED, map(), Collections.singletonList(record()));

        // When
        FileDataBlock back = (FileDataBlock) writeAndReadBack(block);

        // Then
        assertThat(back.getDataType(), is(DataType.EXPECTED_FIXED));
        assertThat(back.getFileType(), is(FileDataBlock.FileType.FIXED));
        assertThat(back.getIdentifier(), is("e.dat"));
    }

    /**
     * Given: {@code SETUP_VARIABLE} のファイルブロック。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : {@code SETUP_VARIABLE} のまま復元される（{@code SETUP_FIXED} と混ざらない）。
     *
     * <p>担保する軸要素: A-08（往復）。</p>
     */
    @Test
    public void restoresSetupVariableDataTypeThroughRealReader() {
        // Given
        FileDataBlock block = new FileDataBlock(DataType.SETUP_VARIABLE, "", "s.csv",
                FileDataBlock.FileType.VARIABLE, map(), Collections.singletonList(record()));

        // When
        FileDataBlock back = (FileDataBlock) writeAndReadBack(block);

        // Then
        assertThat(back.getDataType(), is(DataType.SETUP_VARIABLE));
        assertThat(back.getFileType(), is(FileDataBlock.FileType.VARIABLE));
        assertThat(back.getIdentifier(), is("s.csv"));
    }

    // ------------------------------------------------------------------ C-02 / E-4

    /**
     * Given: セクションを 1 件も持たないコンテナ。
     * When : {@code write}。
     * Then : 例外にならず、<b>ファイルも出力先ディレクトリも作られない</b>。
     *
     * <p>
     * 担保する軸要素: C-02（{@code sections} 空）。{@link YamlFormatWriter#write} は
     * {@code container.getSections()} をループするだけで、ディレクトリの作成もループの中にあるためである。
     * 辺③は同じ入力から<b>シート 0 枚のブックを書き出す</b>（{@code issues.md} <b>XLS-23</b>）ため非対称である。
     * </p>
     */
    @Test
    public void writesNothingWhenContainerHasNoSections() {
        // Given
        File out = new File(folder.getRoot(), "out");
        assertFalse("前提: 出力先はまだ存在しない", out.exists());

        // When
        writer.write(new TestDataContainer("td", Collections.<TestDataSection>emptyList()),
                out.getAbsolutePath());

        // Then
        assertFalse("出力先ディレクトリすら作られない", out.exists());
    }

    /**
     * Given: セクションを 3 件持つコンテナ（名前は辞書順に並べていない）。
     * When : {@code write}。
     * Then : セクションごとに {@code <セクション名>.yaml} が 1 つずつ、計 3 ファイル書かれ、
     *        各ファイルの中身はそのセクションを直列化したものになる。
     *
     * <p>
     * 担保する軸要素: C-02（{@code sections} 複数）／E-4（コンテナ内セクション数 複数）。
     * 辺②は 1 リソース＝1 セクションであるため到達不能だが（{@code inventory.md} §0.8-6）、
     * 辺④は {@code write} がセクションをループするため到達できる。
     * </p>
     *
     * <p>
     * セクションごとにテーブル識別子を変えてあるため、ファイル名とセクションの対応が入れ替われば落ちる。
     * </p>
     */
    @Test
    public void writesOneYamlFilePerSectionWhenContainerHasMultipleSections() throws IOException {
        // Given
        TestDataSection zebra = section("zebra", table("Z"));
        TestDataSection alpha = section("alpha", table("A"));
        TestDataSection mango = section("mango", table("M"));
        File out = folder.getRoot();

        // When
        writer.write(new TestDataContainer("td", Arrays.asList(zebra, alpha, mango)),
                out.getAbsolutePath());

        // Then
        assertThat("書かれたファイル数", out.list().length, is(3));
        assertThat(read(out, zebra), is(writer.serialize(zebra)));
        assertThat(read(out, alpha), is(writer.serialize(alpha)));
        assertThat(read(out, mango), is(writer.serialize(mango)));
        assertThat("セクションごとに中身が違うこと", read(out, zebra),
                containsString("table: \"Z\""));
    }

    /**
     * セクション名から書き出されたファイルを読む。
     *
     * @param dir     出力先ディレクトリ
     * @param section セクション
     * @return ファイルの中身（UTF-8）
     * @throws IOException 読めなかった場合
     */
    private static String read(File dir, TestDataSection section) throws IOException {
        Path file = new File(dir, section.getName() + EXTENSION).toPath();
        assertTrue("書き出されていること: " + file, Files.exists(file));
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    /**
     * 1 行 1 カラムのテーブルブロックを組み立てる。
     *
     * @param name テーブル名
     * @return ブロック
     */
    private static TableDataBlock table(String name) {
        return new TableDataBlock(DataType.SETUP_TABLE_DATA, "", name,
                Collections.singletonList("C"),
                Collections.singletonList(Collections.singletonList("1")));
    }

    // ------------------------------------------------------------------ C-12

    /**
     * Given: レコードレイアウトを 1 件も持たないファイルブロック（ディレクティブは 2 件）。
     * When : {@code serialize}。
     * Then : {@code records:} キー自体が書かれない。ディレクティブは記述順のまま出る。
     *
     * <p>
     * 担保する軸要素: C-12（{@code FileDataBlock.records} 空）／E-3(0)。
     * ディレクティブの並び（{@code text-encoding} → {@code file-type}）は辞書順の逆である。
     * </p>
     */
    @Test
    public void writesFileBlockWithoutRecordsKeyWhenRecordsAreEmpty() {
        // Given
        FileDataBlock block = new FileDataBlock(DataType.SETUP_FIXED, "", "n.dat",
                FileDataBlock.FileType.FIXED, map("text-encoding", "UTF-8", "file-type", "Fixed"),
                Collections.<RecordLayout>emptyList());

        // When / Then
        assertThat(serialize(block), is(""
                + "setup_files:\n"
                + "  - path: \"n.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    directives:\n"
                + "      text-encoding: \"UTF-8\"\n"
                + "      file-type: \"Fixed\"\n"));
    }

    // ------------------------------------------------------------------ 書けるが読み戻せない形（YML-12）

    /**
     * Given: レコードレイアウトを 1 件も持たないファイルブロック。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 書き出しは成功するが、読み戻しは {@code required}（{@code records}）違反で失敗する。
     *
     * <p>
     * 担保する軸要素: なし（{@code issues.md} <b>YML-12</b> の現状挙動の固定）。
     * 本体スキーマの {@code $defs.file_data.required} が {@code records} を要求するためである
     * （{@code minItems: 0} なので<b>空配列なら通る</b>が、キーごと省略されると通らない）。
     * </p>
     */
    @Test
    public void failsToReadBackFileBlockWithoutRecords() {
        // Given / When
        YamlSchemaValidationException e = assertFailsToReadBack(
                new FileDataBlock(DataType.SETUP_FIXED, "", "n.dat", FileDataBlock.FileType.FIXED,
                        map(), Collections.<RecordLayout>emptyList()));

        // Then
        assertThat(types(e), is(Arrays.asList("required")));
        assertThat(locations(e), is(Arrays.asList("$.setup_files[0]")));
    }

    /**
     * Given: レコードレイアウトを 1 件も持たないメッセージブロック。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 書き出しは成功するが、読み戻しは {@code required}（{@code records}）違反で失敗する。
     *
     * <p>
     * 担保する軸要素: なし（{@code issues.md} <b>YML-12</b>）。
     * この形は既存の {@code YamlFormatWriterTest#serializeMessage_emptyBody_emitsIdOnly} が
     * <b>書き出し側だけ</b>固定していた（C-15 空）。読み戻せないことは誰も通していなかった。
     * </p>
     */
    @Test
    public void failsToReadBackMessageBlockWithoutRecords() {
        // Given / When
        YamlSchemaValidationException e = assertFailsToReadBack(
                new MessageDataBlock(DataType.MESSAGE, "", "EMPTY", map(), map(),
                        Collections.<RecordLayout>emptyList()));

        // Then
        assertThat(types(e), is(Arrays.asList("required")));
        assertThat(locations(e), is(Arrays.asList("$.messages[0]")));
    }

    /**
     * Given: フィールドを 1 件も持たないレコードレイアウト。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 書き出しは成功するが、読み戻しは {@code minItems} 違反で失敗する。
     *
     * <p>
     * 担保する軸要素: なし（{@code issues.md} <b>YML-12</b>）。
     * 既存の {@code YamlFormatWriterTest#serialize_recordWithEmptyFieldsAndRows_emitsEmptyFlowLists} が
     * {@code fields: []} を書くことを固定している。辺①・辺②のいずれもこの中間モデルを生成できない
     * （辺③では {@code issues.md} <b>XLS-22</b> が同じ形を扱う）。
     * </p>
     */
    @Test
    public void failsToReadBackRecordWithoutFields() {
        // Given / When
        YamlSchemaValidationException e = assertFailsToReadBack(
                new FileDataBlock(DataType.SETUP_FIXED, "", "f.dat", FileDataBlock.FileType.FIXED, map(),
                        Collections.singletonList(new RecordLayout(null,
                                Collections.<FieldDef>emptyList(),
                                Collections.<List<String>>emptyList()))));

        // Then
        assertThat(types(e), is(Arrays.asList("minItems")));
        assertThat(locations(e), is(Arrays.asList("$.setup_files[0].records[0].fields")));
    }

    /**
     * Given: {@code type} を省略したフィールド定義。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 書き出しは成功するが、読み戻しは {@code required}（{@code type}）違反で失敗する。
     *
     * <p>
     * 担保する軸要素: なし（{@code issues.md} <b>YML-12</b>）。
     * 既存の {@code YamlFormatWriterTest#serialize_fieldWithNullType_omitsType} が
     * {@code {name: "c1"}} を書くことを固定している（C-20 省略）。
     * </p>
     */
    @Test
    public void failsToReadBackFieldWithoutType() {
        // Given / When
        YamlSchemaValidationException e = assertFailsToReadBack(
                new FileDataBlock(DataType.EXPECTED_VARIABLE, "", "out.csv",
                        FileDataBlock.FileType.VARIABLE, map(),
                        Collections.singletonList(new RecordLayout(null,
                                Collections.singletonList(new FieldDef("c1", null, null)),
                                Collections.singletonList(Collections.singletonList("v"))))));

        // Then
        assertThat(types(e), is(Arrays.asList("required")));
        assertThat(locations(e), is(Arrays.asList("$.expected_files[0].records[0].fields[0]")));
    }

    // ------------------------------------------------------------------ 往復で値が変わる形（既知の課題）

    /**
     * Given: 小文字のテーブル名とカラム名（カラムは辞書順の逆に並べてある）。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : テーブル名・カラム名とも<b>大文字化されて</b>戻る。値と並びはそのまま。
     *
     * <p>
     * 担保する軸要素: なし（{@code issues.md} <b>YML-10</b> の辺④側の現れ方の固定）。
     * 大文字化するのは本体の器（{@code TableData}）であり converter ではない。
     * 大小だけが違うカラム名が同居すると値が消えるところまでが YML-10 の課題で、
     * ここで固定するのは<b>往復でカラム名の大小が保たれない</b>ことである。
     * </p>
     */
    @Test
    public void uppercasesTableAndColumnNamesWhenReadBack() {
        // Given
        TableDataBlock block = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "users",
                Arrays.asList("zip", "name"),
                Collections.singletonList(Arrays.asList("100", "x")));

        // When
        TableDataBlock back = (TableDataBlock) writeAndReadBack(block);

        // Then
        assertThat("テーブル名が大文字化される", back.getIdentifier(), is("USERS"));
        assertThat("カラム名が大文字化され、並びは保たれる",
                back.getColumnNames(), is(Arrays.asList("ZIP", "NAME")));
        assertThat("値は変わらない", back.getRows().get(0), is(Arrays.asList("100", "x")));
    }

    /**
     * Given: {@code field-separator} にリテラルのタブ文字を持つ可変長ファイルブロック。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 書き出しは {@code "\t"} と忠実だが、読み戻しは {@code IllegalArgumentException} で止まる。
     *
     * <p>
     * 担保する軸要素: なし（{@code issues.md} <b>YML-08</b> の辺④側の現れ方の固定）。
     * 原因は辺②側でディレクティブ値が {@code trim()} されることであり、タブが空文字になるためである。
     * すなわち<b>タブ区切りの可変長ファイルは辺④→辺②の往復ができない</b>。
     * </p>
     */
    @Test
    public void failsToReadBackLiteralTabFieldSeparator() {
        // Given
        FileDataBlock block = new FileDataBlock(DataType.SETUP_VARIABLE, "", "s.csv",
                FileDataBlock.FileType.VARIABLE, map("field-separator", "\t"),
                Collections.singletonList(record()));
        assertThat("書き出しはタブをエスケープして忠実に書く",
                serialize(block), containsString("field-separator: \"\\t\""));

        // When
        String base = write(block);

        // Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new YamlFormatReader().read(base, "td"));
        assertThat(thrown.getMessage(), containsString("field-separator must be one character"));
    }
}
