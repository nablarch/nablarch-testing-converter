package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.core.reader.yaml.YamlSchemaValidationException;
import nablarch.test.tool.converter.model.TestDataContainer;

import com.networknt.schema.ValidationMessage;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 辺②（YAML→中間モデル）の軸F — 異常系 5 ケース（F2-01〜F2-05）の挙動を固定するテスト。
 *
 * <p>
 * すべて {@link YamlFixture} が書き出した実 {@code .yaml} を入力とし、本番配線の
 * {@link YamlFormatReader} を通す。異常系はスキーマ検証・YAML パースの区間で起こるため、
 * {@code loadRawMap} を差し替える in-memory 経路では再現できない。
 * </p>
 *
 * <table border="1">
 * <caption>本クラスが固定する軸F の要素</caption>
 * <tr><th>要素</th><th>観測した挙動</th></tr>
 * <tr><td>F2-01 スキーマ違反</td><td>{@code YamlSchemaValidationException}</td></tr>
 * <tr><td>F2-02 YAML として不正</td><td>{@code IllegalStateException}（原因は {@code YamlEngineException}）</td></tr>
 * <tr><td>F2-03 未知のキー</td><td>{@code YamlSchemaValidationException}（トップレベルは {@code additionalProperties: false}）</td></tr>
 * <tr><td>F2-04 必須構造の欠落</td><td>{@code YamlSchemaValidationException}</td></tr>
 * <tr><td>F2-05 空ファイル</td><td>例外にならず、ブロック 0 件のコンテナになる</td></tr>
 * </table>
 *
 * <p>
 * <b>F2-01 の入力に、{@code rows} の値として仕様外とした引用符なしスカラー記法（{@code true} /
 * {@code 123} / {@code 1.50} / {@code .inf} / {@code .nan}）は使わない。</b>それらは
 * 「NTF が実行できるテストデータ」の外側にあり、例外の形を固定する対象にしないことが確定しているためである。
 * ここでは仕様内の記法（引用符付き文字列）で書きながらスキーマに反する入力 —— {@code type} の列挙違反と
 * {@code length} のパターン違反 —— を使う。
 * </p>
 *
 * <p>
 * この線引きは <b>{@code rows} の値</b>にのみ当てはまる。他のプロパティは別の型を課しており、たとえば
 * {@code $defs.field_def.properties.length} は integer 記法（{@code 10}）も文字列記法（{@code "10"}）も
 * 許す（{@link YamlFormatReaderRealFileTest#readsIntegerLengthNotationAsString} が担保する）。
 * </p>
 *
 * <p>
 * 例外メッセージ本文はロケール依存（日本語ロケールでは日本語）であるため、アサートは
 * {@link YamlSchemaValidationException#getErrors()} が返す {@link ValidationMessage} の
 * キーワード（{@code getType()}）と違反位置（{@code getInstanceLocation()}）で行う。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>実装（src/main）は変更していない。
 * </p>
 *
 * @author kiyobot
 */
public class YamlFormatReaderInvalidInputTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** 実 {@link YamlFormatReader} を通すため、{@link YamlLoader} の LRU キャッシュをテスト間で残さない。 */
    @After
    public void clearLoaderCache() {
        YamlLoader.clearCacheForTest();
    }

    // ------------------------------------------------------------------ F2-01 スキーマ違反

    /**
     * Given: {@code setup_files} の {@code type} に列挙外の値 {@code "text"} を書いた YAML
     *        （値そのものは引用符付き文字列であり、仕様外としたスカラー記法は使っていない）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code YamlSchemaValidationException} で止まり、違反は {@code enum} キーワード・
     *        位置 {@code $.setup_files[0].type} として報告される。
     *
     * <p>担保する軸要素: F2-01（スキーマ違反）。</p>
     */
    @Test
    public void failsWithSchemaValidationExceptionWhenFileTypeIsNotInEnum() {
        // Given / When / Then
        YamlSchemaValidationException e = assertSchemaViolation(""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"text\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");
        assertThat(types(e), is(Arrays.asList("enum")));
        assertThat(locations(e), is(Arrays.asList("$.setup_files[0].type")));
    }

    /**
     * Given: {@code field_def} の {@code length} に、スキーマの {@code ^([0-9]+|-)$} に反する
     *        引用符付き文字列 {@code "1a"} を書いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code YamlSchemaValidationException} で止まり、違反は同じ位置に対する
     *        {@code type} と {@code pattern} の 2 件として報告される
     *        （値が文字列でも記法が合わなければ通らない）。
     *
     * <p>
     * 2 件になるのは {@code length} が {@code anyOf} だからである。{@code "1a"} は
     * 第 1 枝（{@code {type: integer, minimum: 0}}）の {@code type} と
     * 第 2 枝（{@code {type: string, pattern: "^([0-9]+|-)$"}}）の {@code pattern} の<b>両方</b>を外し、
     * どちらの枝の違反も報告される。順序・件数とも実測（2 件・この順）どおりに固定する。
     * </p>
     *
     * <p>担保する軸要素: F2-01（スキーマ違反）。</p>
     */
    @Test
    public void failsWithSchemaValidationExceptionWhenFieldLengthDoesNotMatchPattern() {
        // Given / When / Then
        YamlSchemaValidationException e = assertSchemaViolation(""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1a\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");
        assertThat(types(e), is(Arrays.asList("type", "pattern")));
        assertThat(locations(e), is(Arrays.asList(
                "$.setup_files[0].records[0].fields[0].length",
                "$.setup_files[0].records[0].fields[0].length")));
    }

    // ------------------------------------------------------------------ F2-02 YAML として不正

    /**
     * Given: インデントが揃わずフローシーケンスも閉じていない、YAML として解析できないテキスト。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code IllegalStateException}（メッセージにファイルパスを含む）で止まり、
     *        原因例外は SnakeYAML Engine の {@code YamlEngineException} である。
     *        スキーマ検証はパースが通らないため実行されない（{@code YamlSchemaValidationException} ではない）。
     *
     * <p>担保する軸要素: F2-02（YAML として不正）。</p>
     */
    @Test
    public void failsWithParseErrorWhenYamlIsMalformed() {
        // Given / When
        try {
            YamlFixture.read(dir(), "setup_tables:\n  - table: \"T\"\n   rows: [\n");
            fail("should throw");
        } catch (IllegalStateException e) {
            // Then
            assertFalse("スキーマ検証まで到達しないこと", e instanceof YamlSchemaValidationException);
            assertTrue("メッセージにファイルパスを含むこと: " + e.getMessage(),
                    e.getMessage().startsWith("Failed to parse YAML file: "));
            assertThat(e.getCause(),
                    is(instanceOf(org.snakeyaml.engine.v2.exceptions.YamlEngineException.class)));
        }
    }

    // ------------------------------------------------------------------ F2-03 未知のキー

    /**
     * Given: 既知セクションに加えて未知のトップレベルキー {@code unknown_section} を書いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 無視されずに {@code YamlSchemaValidationException} で止まる
     *        （違反は {@code additionalProperties} キーワード・位置 {@code $}）。
     *
     * <p>
     * <b>in-memory 経路とは結果が異なる。</b>{@code YamlFormatReaderTest#read_mixedSections_keepsDescriptionOrderAndIgnoresUnknownKeys}
     * は未知キーが無視されることを固定しているが、それは {@code loadRawMap} を差し替えて
     * スキーマ検証を迂回しているためである。実ファイル経路ではスキーマのトップレベルが
     * {@code additionalProperties: false} であるため、未知キーはファイルごと読み込みに失敗する。
     * {@code YamlFormatReader#addBlocksForSection} の「未知キーは無視」は、
     * <b>スキーマが許す範囲（既知キーのうち本クラスが分岐を持たないもの）に対してのみ効く</b>。
     * </p>
     *
     * <p>担保する軸要素: F2-03（未知のキー）。</p>
     */
    @Test
    public void failsWithSchemaValidationExceptionWhenTopLevelKeyIsUnknown() {
        // Given / When / Then
        YamlSchemaValidationException e = assertSchemaViolation(""
                + "unknown_section:\n"
                + "  - x: \"y\"\n"
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - C: \"1\"\n");
        assertThat(types(e), is(Arrays.asList("additionalProperties")));
        assertThat(locations(e), is(Arrays.asList("$")));
    }

    // ------------------------------------------------------------------ F2-04 必須構造の欠落

    /**
     * Given: {@code setup_tables} のエントリに必須の {@code rows} を書かない YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code YamlSchemaValidationException} で止まり、違反は {@code required} キーワード・
     *        位置 {@code $.setup_tables[0]}・欠落プロパティ {@code rows} として報告される。
     *
     * <p>担保する軸要素: F2-04（必須構造の欠落）。</p>
     */
    @Test
    public void failsWithSchemaValidationExceptionWhenRequiredRowsIsMissing() {
        // Given / When / Then
        YamlSchemaValidationException e = assertSchemaViolation("setup_tables:\n  - table: \"T\"\n");
        assertThat(types(e), is(Arrays.asList("required")));
        assertThat(locations(e), is(Arrays.asList("$.setup_tables[0]")));
        assertThat(e.getErrors().get(0).getProperty(), is("rows"));
    }

    /**
     * Given: {@code records} の {@code fields} を空配列にした YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code YamlSchemaValidationException}（{@code minItems}）で止まる。
     *
     * <p>
     * 本テストは軸C の <b>C-17（{@code RecordLayout.fields} 空）が辺②で到達不能である根拠</b>でもある。
     * スキーマ {@code $defs.record_fragment.properties.fields.minItems} が 1 であるため、
     * フィールド 0 件のレコードは中間モデルへ到達しない。
     * </p>
     *
     * <p>担保する軸要素: F2-04（必須構造の欠落）／C-17（到達不能の根拠）。</p>
     */
    @Test
    public void failsWithSchemaValidationExceptionWhenFieldsIsEmpty() {
        // Given / When / Then
        YamlSchemaValidationException e = assertSchemaViolation(""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields: []\n"
                + "        rows: []\n");
        assertThat(types(e), is(Arrays.asList("minItems")));
        assertThat(locations(e), is(Arrays.asList("$.setup_files[0].records[0].fields")));
    }

    /**
     * Given: {@code fields} の要素から必須の {@code type} を落とした YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code YamlSchemaValidationException}（{@code required}）で止まる。
     *
     * <p>
     * 本テストは軸C の <b>C-20（{@code FieldDef.type} 省略）が辺②で到達不能である根拠</b>でもある。
     * スキーマ {@code $defs.field_def.required} が {@code type} を必須とするため、
     * 型を持たないフィールド定義は中間モデルへ到達しない。
     * </p>
     *
     * <p>担保する軸要素: F2-04（必須構造の欠落）／C-20（到達不能の根拠）。</p>
     */
    @Test
    public void failsWithSchemaValidationExceptionWhenFieldTypeIsMissing() {
        // Given / When / Then
        YamlSchemaValidationException e = assertSchemaViolation(""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");
        assertThat(types(e), is(Arrays.asList("required")));
        assertThat(locations(e), is(Arrays.asList("$.setup_files[0].records[0].fields[0]")));
        assertThat(e.getErrors().get(0).getProperty(), is("type"));
    }

    // ------------------------------------------------------------------ F2-05 空ファイル

    /**
     * Given: 中身が空の {@code .yaml}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 例外にならず、リソース名を持つコンテナ 1 件・セクション 1 件・ブロック 0 件になる。
     *
     * <p>
     * {@code YamlLoader#load} は読み込み結果が {@code null} のとき空 Map を返し、スキーマ検証も
     * 実行しない（トップレベルの必須キーは無いため空 Map はスキーマ上も適合する）。
     * <b>本テストはこの「スキーマ検証に到達しない」分岐そのものを担保する唯一のテストである。</b>
     * 同じ結果（ブロック 0 件）をスキーマ検証を通る入力で確かめるのは
     * {@link YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks}
     * （入力は {@code setup_tables: []}）の役目であり、両者は通る経路が違う。
     * </p>
     *
     * <p>担保する軸要素: F2-05（空ファイル）。</p>
     */
    @Test
    public void readsEmptyFileAsContainerWithoutBlocks() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), "");

        // Then
        assertThat(container.getName(), is(YamlFixture.RESOURCE));
        assertThat(container.getSections().size(), is(1));
        assertThat(container.getSections().get(0).getName(), is(YamlFixture.RESOURCE));
        assertThat(YamlFixture.blocks(container).size(), is(0));
    }

    // ------------------------------------------------------------------ helpers

    /**
     * フィクスチャ {@code .yaml} の出力先ディレクトリ。読み書きとも本メソッドだけを使う。
     *
     * @return ディレクトリ
     */
    private Path dir() {
        return folder.getRoot().toPath();
    }

    /**
     * YAML テキストを読み、{@link YamlSchemaValidationException} で失敗することを確かめる。
     *
     * @param yamlText YAML テキスト
     * @return 送出された例外
     */
    private YamlSchemaValidationException assertSchemaViolation(String yamlText) {
        try {
            YamlFixture.read(dir(), yamlText);
            fail("should throw YamlSchemaValidationException");
            return null;
        } catch (YamlSchemaValidationException e) {
            return e;
        }
    }

    /** 違反のキーワード（{@code enum} / {@code required} など）をロケール非依存に取り出す。 */
    private static List<String> types(YamlSchemaValidationException e) {
        List<String> types = new ArrayList<String>();
        for (ValidationMessage message : e.getErrors()) {
            types.add(message.getType());
        }
        return types;
    }

    /** 違反位置（{@code $.setup_files[0].type} など）をロケール非依存に取り出す。 */
    private static List<String> locations(YamlSchemaValidationException e) {
        List<String> locations = new ArrayList<String>();
        for (ValidationMessage message : e.getErrors()) {
            locations.add(message.getInstanceLocation().toString());
        }
        return locations;
    }
}
