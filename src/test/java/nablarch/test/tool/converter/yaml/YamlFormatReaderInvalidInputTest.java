package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.core.reader.yaml.YamlSchemaValidationException;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.MessageDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;

import com.networknt.schema.ValidationMessage;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

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
 * <b>例外メッセージのアサートについて。</b>次の 3 類型で使い分ける（辺①の
 * {@code XlsFormatReaderInvalidInputTest} と同じ考え方）。
 * </p>
 * <ul>
 *   <li>スキーマ検証の違反 — メッセージ本文はロケール依存（日本語ロケールでは日本語）であるため
 *       本文を突き合わせず、{@link YamlSchemaValidationException#getErrors()} が返す
 *       {@link ValidationMessage} のキーワード（{@code getType()}）と違反位置
 *       （{@code getInstanceLocation()}）で行う。<b>報告順は突き合わせない</b> ——
 *       {@code JsonSchema#validate} が返すのは {@code Set} であり反復順は契約されていないため、
 *       2 件以上報告されるケースは件数と集合で突き合わせる。</li>
 *   <li>器（{@code nablarch-testing}）や外部ライブラリが生成する文言 — 版差に追随しないため、
 *       特徴的な部分文字列だけを {@code containsString} で突き合わせる。</li>
 *   <li>変換ツール自身が組み立てる文言（一時ディレクトリの絶対パスを含むもの） —
 *       変わらない先頭部分を {@code startsWith} で突き合わせる。</li>
 * </ul>
 *
 * <p>
 * <b>本クラスは軸F の 5 ケースに加えて、「スキーマが構造を縛っていない箇所を突いた入力」の現状挙動も固定する</b>
 * （後半の「掃引で見つけた現状挙動の固定」節。{@code coverage/issues.md} の
 * <b>YML-04</b>〜<b>YML-08</b>）。それらの入力は<b>スキーマ検証を通る仕様内の入力</b>であって異常系ではないが、
 * 例外にならず値や行が消える／中間モデルが原文と食い違うため、辺①の
 * {@code XlsFormatReaderInvalidInputTest}（XLS-10〜XLS-15 を同居させている）と同じ置き方に揃えた。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションは原則として「実行して観測した現状の挙動」である。</b>
 * ただし <b>YML-08</b>（区切り文字ディレクティブの値が辺①と非対称になり、実制御文字のまま中間モデルへ
 * 入って本体が読み戻せない）は <b>#25.5 で修正済み</b>で、該当テストは現状の固定ではなく
 * <b>記法どおりの仕様</b>を書いている。<b>YML-04 は #36 で一部だけ解消した</b> —— 依存先
 * {@code nablarch-testing-yaml} の Step 4 是正で空マッピング {@code {}} の行がカラム解決より前に
 * 読み飛ばされるようになり、{@link #skipsEmptyObjectRowAndKeepsFollowingRowInTable} ／
 * {@link #skipsEmptyObjectRowAndKeepsFollowingRowInListMap} の 2 件は記法どおりの仕様を書いている。
 * <b>YML-04 の中心（カラムが先頭行のキー集合だけで決まる）は残っており</b>、
 * YML-05〜YML-07・YML-10・YML-11 とともに現状の固定のままである。
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
        return assertThrows(YamlSchemaValidationException.class, () -> YamlFixture.read(dir(), yamlText));
    }

    /**
     * 違反のキーワード（{@code enum} / {@code required} など）をロケール非依存に取り出す。
     *
     * @param e 送出された例外
     * @return 違反のキーワード（例外が報告した順）
     */
    private static List<String> types(YamlSchemaValidationException e) {
        List<String> types = new ArrayList<>();
        for (ValidationMessage message : e.getErrors()) {
            types.add(message.getType());
        }
        return types;
    }

    /**
     * 違反位置（{@code $.setup_files[0].type} など）をロケール非依存に取り出す。
     *
     * @param e 送出された例外
     * @return 違反位置（例外が報告した順）
     */
    private static List<String> locations(YamlSchemaValidationException e) {
        List<String> locations = new ArrayList<>();
        for (ValidationMessage message : e.getErrors()) {
            locations.add(message.getInstanceLocation().toString());
        }
        return locations;
    }

    /**
     * 指定のクラス・メソッドの段がスタックトレースに含まれるかを返す。
     *
     * <p>
     * 段の位置（{@code [0]} / {@code [1]}）で見ないのは、最内段が JDK の実装で動くためである。
     * </p>
     *
     * @param thrown     送出された例外
     * @param className  探すクラスの完全修飾名
     * @param methodName 探すメソッド名
     * @return 含まれるなら {@code true}
     */
    private static boolean hasFrame(Throwable thrown, String className, String methodName) {
        for (StackTraceElement frame : thrown.getStackTrace()) {
            if (className.equals(frame.getClassName()) && methodName.equals(frame.getMethodName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 唯一のブロックの唯一のレコードレイアウトを返す。
     *
     * @param container 中間モデル
     * @return レコードレイアウト
     */
    private static RecordLayout onlyRecord(TestDataContainer container) {
        FileDataBlock block = YamlFixture.onlyBlock(container, FileDataBlock.class);
        assertThat("レコードレイアウトが 1 件だけ生成されること", block.getRecords().size(), is(1));
        return block.getRecords().get(0);
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
     * どちらの枝の違反も報告される。<b>固定するのは件数（2 件）とキーワードの集合だけで、
     * 報告順は突き合わせない</b>（クラス Javadoc の方針どおり。{@code JsonSchema#validate} が返すのは
     * {@code Set} であり反復順は契約されていないため）。
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
        String location = "$.setup_files[0].records[0].fields[0].length";
        assertThat("2 件報告されること", types(e).size(), is(2));
        assertThat("報告順は Set のため突き合わせない", types(e), containsInAnyOrder("type", "pattern"));
        assertThat("2 件とも同じ位置に対する違反であること", locations(e), is(Arrays.asList(location, location)));
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
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> YamlFixture.read(dir(), "setup_tables:\n  - table: \"T\"\n   rows: [\n"));

        // Then
        assertFalse("スキーマ検証まで到達しないこと", thrown instanceof YamlSchemaValidationException);
        assertThat("メッセージにファイルパスを含むこと",
                thrown.getMessage(), startsWith("Failed to parse YAML file: "));
        assertThat(thrown.getCause(),
                is(instanceOf(YamlEngineException.class)));
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
     * すなわち {@code YamlFormatReader#addBlocksForSection} の「未知キーは無視」は
     * <b>実ファイル経路では到達不能</b>である —— スキーマのトップレベル {@code properties} 11 キーと
     * 同メソッドの分岐 11 本が完全に一致しており、分岐に落ちない既知キーは存在しない。
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
     * 実行しない。<b>ただし本テストはその早期 return を検知できない。</b>スキーマのトップレベルには
     * {@code required} も {@code minProperties} も無く、空 Map は検証を通しても適合するため、
     * 早期 return を外しても結果は変わらないからである。本テストが固定しているのは
     * 「空ファイルを読んでも例外にならず、リソース名を持つブロック 0 件のコンテナになる」ことである。
     * 同じ結果（ブロック 0 件）をスキーマ検証が実際に走る入力で確かめるのは
     * {@link YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks}
     * （入力は {@code setup_tables: []}）の役目である。
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

    // ==================================================================
    // 掃引で見つけた現状挙動の固定
    //
    // 以下はいずれも「本体スキーマが構造を縛っていない箇所」を突いた入力であり、
    // スキーマ検証を通る（＝仕様内の）入力である。軸F の 5 ケースには属さない。
    // 課題は coverage/issues.md の YML-04〜YML-08・YML-10 に記録した。
    // このうち YML-08 は #25.5 で修正済みで、該当テストは現状の固定ではなく記法どおりの仕様を書いている。
    // YML-04 は #36 で一部だけ解消した（空マッピング {} の行の読み飛ばし。中心は残っている）。
    // 残る YML-05〜YML-07・YML-10 は未修正のため現状の固定のままである。
    // ==================================================================

    // ------------------------------------------------------------------ ローダの他の失敗経路（軸F の 5 ケース外）

    /**
     * Given: ルートがマッピングでない YAML（シーケンス）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code IllegalStateException}（メッセージは {@code "YAML root must be a mapping, but was "} で始まる）。
     *
     * <p>
     * <b>F2-02（YAML として不正）とは別の分岐である。</b>F2-02 はパース自体が失敗する経路
     * （{@link #failsWithParseErrorWhenYamlIsMalformed}。原因例外は {@code YamlEngineException}）だが、
     * こちらはパースには成功したうえで型を弾く経路であり、原因例外を持たない。
     * どちらも {@code IllegalStateException} であるため、<b>メッセージの先頭で分岐を区別する</b>。
     * </p>
     *
     * <p>担保する軸要素: なし（軸F の 5 ケースには属さない。ローダの分岐の固定）。</p>
     */
    @Test
    public void failsWhenYamlRootIsNotMapping() {
        // Given / When
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> YamlFixture.read(dir(), "- a\n- b\n"));

        // Then
        assertThat(thrown.getMessage(), startsWith("YAML root must be a mapping, but was "));
        assertThat("パースは成功しているため原因例外を持たない", thrown.getCause(), is(nullValue()));
    }

    /**
     * Given: 同一マッピング内に同じキーを 2 回書いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code IllegalStateException}（原因は {@code YamlEngineException}）で止まる。
     *
     * <p>
     * ローダが {@code LoadSettings} に {@code setAllowDuplicateKeys(false)} を設定しているため、
     * パース段階で弾かれる。<b>辺①とは結果が正反対である</b> —— 辺①の実 {@code .xlsx} 経路では
     * カラム名の重複は WARN ＋ 後勝ちで<b>変換が継続する</b>（F1-05。steering #16）。
     * </p>
     *
     * <p>
     * なお辺②で「大小だけが違うキー」（`id` と `ID`）は YAML としては別キーであるため
     * この検査には掛からず、器の大文字化で衝突して値が消える（<b>YML-10</b>）。
     * </p>
     *
     * <p>担保する軸要素: なし（軸F の 5 ケースには属さない。ローダの分岐の固定）。</p>
     */
    @Test
    public void failsWhenSameKeyAppearsTwiceInOneMapping() {
        // Given / When
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> YamlFixture.read(dir(), ""
                        + "setup_tables:\n"
                        + "  - table: \"T\"\n"
                        + "    rows:\n"
                        + "      - V: \"1\"\n"
                        + "        V: \"2\"\n"));

        // Then
        assertThat(thrown.getMessage(), startsWith("Failed to parse YAML file: "));
        assertThat(thrown.getCause(), is(instanceOf(YamlEngineException.class)));
    }

    // ------------------------------------------------------------------ YML-04 先頭行のキー集合だけがカラムになる

    /**
     * Given: {@code setup_tables} の 2 行目にだけ現れるカラム {@code B} を持つ YAML
     *        （{@code rows.items.additionalProperties} は任意キーを許すためスキーマを通る）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>例外にならず</b>、カラムは先頭行のキー集合 {@code [A]} だけになり、
     *        2 行目の {@code B: "x"} が黙って消える。
     *
     * <p>{@code coverage/issues.md} <b>YML-04</b> の根拠テスト。</p>
     */
    @Test
    public void dropsColumnThatAppearsOnlyInSecondRowOfTable() {
        // Given / When
        YamlFixture.Reading reading = YamlFixture.readCapturingWarnings(dir(), ""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - {A: \"1\"}\n"
                + "      - {A: \"2\", B: \"x\"}\n");

        // Then
        TableDataBlock block = YamlFixture.onlyBlock(reading.container(), TableDataBlock.class);
        assertThat(block.getColumnNames(), is(Arrays.asList("A")));
        assertThat("2 行目の B が消える", block.getRows(),
                is(Arrays.asList(Arrays.asList("1"), Arrays.asList("2"))));
        assertThat("JUL 経路にも警告が出ない", reading.warnings(), is(Collections.<String>emptyList()));
    }

    /**
     * Given: {@code list_maps} の 2 行目にだけ現れるカラム {@code B} を持つ YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : テーブル経路と同じく、2 行目の {@code B: "x"} が黙って消える。
     *
     * <p>{@code coverage/issues.md} <b>YML-04</b> の根拠テスト（LIST_MAP 経路）。</p>
     */
    @Test
    public void dropsColumnThatAppearsOnlyInSecondRowOfListMap() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "list_maps:\n"
                + "  - id: \"lm\"\n"
                + "    rows:\n"
                + "      - {A: \"1\"}\n"
                + "      - {A: \"2\", B: \"x\"}\n");

        // Then
        ListMapBlock block = YamlFixture.onlyBlock(container, ListMapBlock.class);
        assertThat(block.getColumnNames(), is(Arrays.asList("A")));
        assertThat("2 行目の B が消える", block.getRows(),
                is(Arrays.asList(Arrays.asList("1"), Arrays.asList("2"))));
    }

    /**
     * Given: 先頭行にだけ現れるカラム {@code B} を持つ（2 行目でキーが欠ける）YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 逆向き（キー不足）は値が消えず、Java {@code null} で埋められる。
     *
     * <p>
     * YML-04 が<b>非対称</b>であることの根拠。カラム集合は先頭行だけで決まるため、
     * 「先頭行に無いキー」は消え、「先頭行にあって後続行に無いキー」は {@code null} で救われる。
     * </p>
     */
    @Test
    public void padsColumnMissingFromSecondRowWithNullInTable() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - {B: \"x\", A: \"1\"}\n"
                + "      - {B: \"y\"}\n");

        // Then
        TableDataBlock block = YamlFixture.onlyBlock(container, TableDataBlock.class);
        assertThat("辞書順（[A, B]）ではなく原文の記述順であること",
                block.getColumnNames(), is(Arrays.asList("B", "A")));
        assertThat("欠けたキーは null で埋まる（カラム順は原文どおり）", block.getRows(),
                is(Arrays.asList(Arrays.asList("x", "1"), Arrays.asList("y", null))));
    }

    /**
     * Given: 先頭行が空マッピング {@code {}} で、2 行目にデータを書いた {@code setup_tables}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 空マッピングの行<b>だけ</b>が読み飛ばされ、2 行目はカラム名も値も保たれる。
     *
     * <p>
     * 記法
     * 全要素が空のエントリは読み飛ばされる。……YAML では {@code rows:} 内の要素が空マッピング
     * （{@code {}}）……の場合にスキップされる。のとおりであり、カラム名は<b>読み飛ばしたあとの
     * 先頭行</b>で決まる。実測値は {@code columnNames} ＝ {@code [A]}・{@code rows} ＝ {@code [[1]]}
     * （2026-08-28）。
     * </p>
     *
     * <p>
     * <b>{@code coverage/issues.md} の YML-04 は解消済みである。</b>以前は先頭の {@code {}} が
     * カラム名を空にしてしまい 2 行目のデータごと消えていた。依存先 {@code nablarch-testing-yaml} の
     * Step 4 是正で直った（#36）。
     * </p>
     */
    @Test
    public void skipsEmptyObjectRowAndKeepsFollowingRowInTable() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - {}\n"
                + "      - {A: \"1\"}\n");

        // Then
        TableDataBlock block = YamlFixture.onlyBlock(container, TableDataBlock.class);
        assertThat(block.getColumnNames(), is(Arrays.asList("A")));
        assertThat(block.getRows(), is(Arrays.asList(Arrays.asList("1"))));
    }

    /**
     * Given: 先頭行が空マッピング {@code {}} で、2 行目にデータを書いた {@code list_maps}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 空マッピングの行<b>だけ</b>が読み飛ばされ、2 行目はカラム名も値も保たれる。
     *
     * <p>
     * <b>テーブル経路（{@link #skipsEmptyObjectRowAndKeepsFollowingRowInTable}）と同じ結果になる。</b>
     * 以前は同じ入力でも経路によって結果が違い（テーブルは行ごと消える／LIST_MAP は値を持たない行が残る）、
     * それが {@code coverage/issues.md} <b>YML-04</b> であった。依存先 {@code nablarch-testing-yaml} の
     * Step 4 是正で両経路とも 記法どおりになった（#36）。
     * 実測値は {@code columnNames} ＝ {@code [A]}・{@code rows} ＝ {@code [[1]]}（2026-08-28）。
     * </p>
     */
    @Test
    public void skipsEmptyObjectRowAndKeepsFollowingRowInListMap() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "list_maps:\n"
                + "  - id: \"lm\"\n"
                + "    rows:\n"
                + "      - {}\n"
                + "      - {A: \"1\"}\n");

        // Then
        ListMapBlock block = YamlFixture.onlyBlock(container, ListMapBlock.class);
        assertThat(block.getColumnNames(), is(Arrays.asList("A")));
        assertThat(block.getRows(), is(Arrays.asList(Arrays.asList("1"))));
    }

    /**
     * Given: {@code rows} のキーがマーカーカラム {@code [no]} だけの {@code setup_tables}。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : マーカーカラムの名前と各行の値が保たれる。
     *
     * <p>
     * カラム名の行がマーカーカラムだけのデータブロックは例外として、マーカーカラムとその値を保ったまま
     * 変換する。各エントリはフィールドを持たないが、テストショット一覧と行の順序で対応付ける用途では
     * エントリの数と並びが意味を持つためである。<b>辺①（{@code coverage/issues.md} <b>XLS-08</b>）と
     * 同じ扱いであり、2 辺は対称である。</b>
     * </p>
     *
     * <p>
     * <b>本クラスに残しているのは、この入力が「マーカーの除外でカラムが 0 件になる」という
     * 異常系の入り口として書かれていたためである。</b>本体の読みを正解にした担保と、
     * 実データカラムを持つエントリでマーカーが落ちる非回帰は {@code YamlMarkerOnlyBlockTest} にある。
     * </p>
     */
    @Test
    public void keepsMarkerOnlyTableColumnsAndValues() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - {\"[no]\": \"1\"}\n"
                + "      - {\"[no]\": \"2\"}\n");

        // Then
        TableDataBlock block = YamlFixture.onlyBlock(container, TableDataBlock.class);
        assertThat("マーカーカラムの名前が残る", block.getColumnNames(), is(Arrays.asList("[no]")));
        assertThat("各行の値も残る", block.getRows(),
                is(Arrays.asList(Arrays.asList("1"), Arrays.asList("2"))));
    }

    // ------------------------------------------------------------------ YML-14 フィールド数より値が多い行（余りが黙って消える）

    /**
     * Given: フィールド 1 件に対して値を 3 個書いたレコード断片
     *        （{@code rows.items} の要素数は {@code fields} の件数と紐づいていないためスキーマを通る）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>例外にならず</b>、フィールド数を超える値が黙って捨てられる。
     *
     * <p>
     * <b>本テストは他責の現状を記録したものである。</b>YAML 形式には記述時エラーの一覧が無く、
     * <b>読み込みでエラーにすることは定められていない</b>。フィールド数と要素数の一致に触れるのは
     * {@code YamlTestDataValidator} の検査だけであり、その検証は変換の処理経路に組み込まれておらず、
     * 変換の実行時に自動では呼び出されない。
     * <b>「あるべき姿」を主張していた {@code @Ignore} つきテストは #35 で削除した</b>
     * （記法に無い書き方は追わない）。
     * </p>
     *
     * <p>
     * <b>帰属は converter の外（本体パーサ）である。</b>nablarch-testing の
     * {@code nablarch/test/core/file/DataFileFragment.java} の {@code addValue} が
     * {@code String value = i < line.size() ? line.get(i) : "";} としてフィールド名の件数ぶんだけ値を取り出すため、
     * <b>余った要素は converter に届く前に器が捨てている</b>。{@code YamlFormatReader#toRecordLayouts} は
     * 器が持つ値 Map を並べ直すだけで原文の要素数を見ない。したがって converter 側に番人も WARN も置かない
     * （ユーザー確定・2026-08-19。{@code coverage/issues.md} <b>YML-14</b>）。
     * </p>
     *
     * <p>担保する軸要素: なし（YML-14 の根拠テスト）。</p>
     */
    @Test
    public void dropsRecordFragmentValuesBeyondFieldCount() {
        // Given / When
        YamlFixture.Reading reading = YamlFixture.readCapturingWarnings(dir(), ""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\", \"b\", \"c\"]\n");

        // Then
        RecordLayout record = onlyRecord(reading.container());
        assertThat(record.getFields().size(), is(1));
        assertThat("2 個目以降の値が消える", record.getRows(),
                is(Arrays.asList(Arrays.asList("a"))));
        assertThat("JUL 経路にも警告が出ない", reading.warnings(), is(Collections.<String>emptyList()));
    }

    // ------------------------------------------------------------------ YML-05 フィールド数より値が少ない行（不足が空文字で埋まる）

    /**
     * Given: フィールド 3 件に対して値が足りない行（1 個だけの行と、2 個目に明示 {@code null} を書いた行）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>例外にならず</b>、欠けた位置は Java {@code null} ではなく<b>空文字</b>で埋められる。
     *        末尾に書いた {@code null} も空文字になる（形式によらず、末尾のフィールドの {@code null} は空文字である）。
     *
     * <p>
     * すなわち<b>「書かれた空文字」と「要素数不足で埋められた欠損」が中間モデル上で区別できない</b>。
     * 軸D の D2-11 で固定した「空文字と Java {@code null} は区別される」は<b>書かれた値についてのみ</b>成り立つ。
     * {@code coverage/issues.md} <b>YML-05</b> の根拠テスト。
     * </p>
     *
     * <p>
     * 2 行目の {@code null} は<b>末尾側</b>にあるため空文字になる。後ろに空文字でも {@code null} でもない
     * フィールドがあるときに {@code null} のまま残ることは
     * {@code YamlFrameworkAlignmentTest#keepsNonTrailingNullAsJavaNullInRecordFragment} が固定する。
     * </p>
     */
    @Test
    public void fillsMissingRecordFragmentValuesWithEmptyStringInsteadOfNull() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "          - {name: \"f2\", type: \"半角英字\", length: \"1\"}\n"
                + "          - {name: \"f3\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n"
                + "          - [\"a\", null]\n");

        // Then
        RecordLayout record = onlyRecord(container);
        assertThat(record.getFields().size(), is(3));
        assertThat("欠損は空文字で埋められる（null ではない）", record.getRows().get(0),
                is(Arrays.asList("a", "", "")));
        assertThat("末尾側に並んだ null と欠損はまとめて空文字になる", record.getRows().get(1),
                is(Arrays.asList("a", "", "")));
    }

    /**
     * Given: フィールド 1 件のレコード断片に、要素 0 個の行 {@code rows: - []} を書いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 行は消えず、値が<b>空文字</b>の 1 要素になる。
     *
     * <p>
     * <b>「書かれた空文字」と見分けが付かないことを実行可能な形にするテストである。</b>
     * {@link YamlFormatReaderScalarTest#readsEmptyStringAsIsInRecordFragmentPath} は
     * {@code rows: - [""]} を読んで同じ {@code [""]} を得る。本テストは {@code rows: - []}
     * ——すなわち<b>何も書かなかった行</b>——が同じ結果になることを示す。
     * したがってレコード断片経路では「書いた空文字が保たれた」ことをテストで示せない（<b>YML-05</b>）。
     * </p>
     *
     * <p>担保する軸要素: なし（YML-05 の根拠テスト。D2-11 の担保の限界を示す）。</p>
     */
    @Test
    public void fillsEmptyRecordFragmentRowWithEmptyStringIndistinguishableFromWrittenOne() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - []\n");

        // Then
        assertThat("何も書かなかった行も空文字 1 要素になる（書いた \"\" と同じ）",
                onlyRecord(container).getRows(), is(Arrays.asList(Arrays.asList(""))));
    }

    // ------------------------------------------------------------------ YML-06 id 重複

    /**
     * Given: 同じ {@code id} を持つ {@code list_maps} エントリ 2 件（値は別）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>例外にならず</b>ブロックは 2 件生成されるが、<b>どちらも 1 件目の値</b>を持つ。
     *        2 件目に書いた値 {@code "second"} は中間モデルに現れない。
     *
     * <p>
     * スキーマは {@code id} に一意制約を持たず、{@code $defs.list_map_data} の description も
     * 「id が重複した場合は最初の1件のみ有効（2件目以降は無視）」と重複を仕様内として扱っている。
     * {@code coverage/issues.md} <b>YML-06</b> の根拠テスト。
     * </p>
     */
    @Test
    public void reusesFirstEntryRowsForDuplicateListMapId() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "list_maps:\n"
                + "  - id: \"lm\"\n"
                + "    rows:\n"
                + "      - {A: \"first\"}\n"
                + "  - id: \"lm\"\n"
                + "    rows:\n"
                + "      - {A: \"second\"}\n");

        // Then
        List<TestDataBlock> blocks = YamlFixture.blocks(container);
        assertThat(blocks.size(), is(2));
        assertThat(((ListMapBlock) blocks.get(0)).getRows(), is(Arrays.asList(Arrays.asList("first"))));
        assertThat("2 件目のブロックにも 1 件目の値が入る",
                ((ListMapBlock) blocks.get(1)).getRows(), is(Arrays.asList(Arrays.asList("first"))));
    }

    /**
     * Given: 同じ {@code id} を持つ {@code messages} エントリ 2 件（フィールド名も値も別）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>例外にならず</b>ブロックは 2 件生成されるが、2 件目はフィールド定義だけが自分のもので、
     *        データ行は<b>1 件目の本文</b>になる。2 件目に書いた値 {@code "b"} は中間モデルに現れない。
     *
     * <p>{@code coverage/issues.md} <b>YML-06</b> の根拠テスト（メッセージ経路）。</p>
     */
    @Test
    public void reusesFirstEntryBodyForDuplicateMessageId() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "messages:\n"
                + "  - id: \"RM01\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"m1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n"
                + "  - id: \"RM01\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"m2\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"b\"]\n");

        // Then
        List<TestDataBlock> blocks = YamlFixture.blocks(container);
        assertThat(blocks.size(), is(2));
        MessageDataBlock second = (MessageDataBlock) blocks.get(1);
        assertThat("フィールド定義は 2 件目の原文",
                second.getRecords().get(0).getFields().get(0).getName(), is("m2"));
        assertThat("値は 1 件目の本文", second.getRecords().get(0).getRows(),
                is(Arrays.asList(Arrays.asList("a"))));
    }

    // ------------------------------------------------------------------ YML-07 長さ省略記法

    /**
     * Given: 長さ省略記法 {@code "-"} を使い、{@code text-encoding} を書かないファイルエントリ
     *        （スキーマ {@code $defs.field_def.properties.length} は {@code "-"} を許し、
     *        description もオンデマンド計算として意味を定めている）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code NullPointerException}（メッセージ無し）で止まる。
     *
     * <p>
     * 器がオンデマンド長を求めるために値のバイト長を計算する際、ディレクティブ由来の文字セットが
     * {@code null} のままだからである。{@code coverage/issues.md} <b>YML-07</b> の根拠テスト。
     * </p>
     *
     * <p>
     * <b>例外の型とメッセージだけでなく、発生箇所もアサートする。</b>型とメッセージ {@code null} だけでは
     * フィクスチャ側の不備で出た {@code NullPointerException} でも緑になり、
     * 「長さ計算の場所で・診断情報なしに落ちる」という本課題の主張を固定できないためである。
     * </p>
     *
     * <p>
     * 見るのは<b>器の呼び出し段が居ること</b>だけで、段の位置も行番号も見ない。
     * NPE を投げる最内段は JDK の実装次第で動く（JDK 17 では
     * {@code String#getBytes(Charset)} の {@code null} 判定だが、{@code Objects#requireNonNull} へ
     * 変われば別クラスになる）。本課題の主張は最内段ではなく<b>呼び出し元</b>にあるためである。
     * </p>
     */
    @Test
    public void failsWithNullPointerExceptionWhenOndemandLengthIsUsedWithoutTextEncoding() {
        // Given / When
        NullPointerException thrown = assertThrows(NullPointerException.class,
                () -> YamlFixture.read(dir(), ""
                        + "setup_files:\n"
                        + "  - path: \"f.dat\"\n"
                        + "    type: \"fixed\"\n"
                        + "    records:\n"
                        + "      - fields:\n"
                        + "          - {name: \"f1\", type: \"半角英字\", length: \"-\"}\n"
                        + "        rows:\n"
                        + "          - [\"abcd\"]\n"));

        // Then
        assertThat("どのファイルのどのフィールドかを示す手掛かりが無い",
                thrown.getMessage(), is(nullValue()));
        assertTrue("器のオンデマンド長計算から出た NPE であること: " + Arrays.toString(thrown.getStackTrace()),
                hasFrame(thrown, "nablarch.test.core.file.DataFileFragment", "replaceFieldSize"));
    }

    /**
     * Given: 同じ長さ省略記法 {@code "-"} に {@code text-encoding} を添えたファイルエントリ。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 例外にならず、{@code FieldDef.length} には原文 {@code "-"} が入る
     *        （器が導出する実バイト長 {@code "4"} ではない）。
     *
     * <p>
     * {@link #failsWithNullPointerExceptionWhenOndemandLengthIsUsedWithoutTextEncoding} との差は
     * {@code text-encoding} の有無だけである（YML-07 の切り分け）。
     * </p>
     */
    @Test
    public void readsOndemandLengthNotationWhenTextEncodingIsSpecified() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    directives:\n"
                + "      text-encoding: \"UTF-8\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"-\"}\n"
                + "        rows:\n"
                + "          - [\"abcd\"]\n");

        // Then
        RecordLayout record = onlyRecord(container);
        assertThat(record.getFields().get(0).getLength(), is("-"));
        assertThat(record.getRows(), is(Arrays.asList(Arrays.asList("abcd"))));
    }

    // ------------------------------------------------------------------ YML-08 ディレクティブ値

    /**
     * Given: {@code record-separator} をスキーマ description が推奨するリテラル記法
     *        （ダブルクォート文字列内のエスケープシーケンス {@code "\r\n"}）で書いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>例外にならず</b>、中間モデルの {@code record-separator} はシンボル {@code "NONE"} になる。
     *
     * <p>
     * 記法が定めるのはシンボル（{@code NONE}／{@code CR}／{@code LF}／{@code CRLF}）または任意の
     * リテラル文字列である。中間モデルには辺①
     * （{@code XlsFormatReader#normalizeDirectiveValue}）と同じ逆正規化を通した値を入れる。
     * </p>
     *
     * <p>
     * <b>書いた改行そのものは本体で失われる。</b>本体 {@code DataFile#setDirective} が
     * 値へ {@code String#trim()} を掛けるため、制御文字だけの値は converter へ届く前に空になる。
     * 空を {@code NONE} と読むのは辺①と同じ規則である。改行が消えること自体は本リポジトリの外
     * （nablarch-testing）に原因があり、ここでは直せない。{@code coverage/issues.md} <b>YML-08</b>。
     * </p>
     */
    @Test
    public void readsRecordSeparatorWrittenAsLiteralNewlineAsNoneSymbol() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_files:\n"
                + "  - path: \"f.csv\"\n"
                + "    type: \"variable\"\n"
                + "    directives:\n"
                + "      record-separator: \"\\r\\n\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"c1\", type: \"半角英字\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");

        // Then
        FileDataBlock block = YamlFixture.onlyBlock(container, FileDataBlock.class);
        assertThat("本体の trim() で空になった値を、辺①と同じ規則でシンボルへ戻す",
                block.getDirectives().get("record-separator"), is("NONE"));
    }

    /**
     * Given: {@code record-separator} をシンボル記法 {@code "CRLF"} で書いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 中間モデルにも<b>シンボルのまま</b>（{@code "CRLF"}）入る。
     *
     * <p>
     * 本体の器は {@code LineSeparator.evaluate} でシンボルを実改行へ変換するが、
     * 辺①（{@code XlsFormatReader#normalizeDirectiveValue}）も辺②
     * （{@code YamlFormatReader#toStringDirectives}）も同じ逆正規化でシンボルへ戻すため、
     * 同じ入力表記が同じ中間モデル値になる。記法どおりである。
     * （{@code record-separator CRLF}）。{@code coverage/issues.md} <b>YML-08</b>。
     * </p>
     */
    @Test
    public void readsRecordSeparatorSymbolAsSymbol() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_files:\n"
                + "  - path: \"f.csv\"\n"
                + "    type: \"variable\"\n"
                + "    directives:\n"
                + "      record-separator: \"CRLF\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"c1\", type: \"半角英字\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");

        // Then
        FileDataBlock block = YamlFixture.onlyBlock(container, FileDataBlock.class);
        assertThat(block.getDirectives().get("record-separator"), is("CRLF"));
    }

    /**
     * Given: {@code field-separator} を {@code "\t"} と書いた YAML
     *        （ダブルクォート文字列のためタブ文字 1 個に解決される）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code IllegalArgumentException} で止まる。{@code trim()} でタブが失われ、
     *        「1 文字でなければならない」という検査に引っ掛かるためである。
     *
     * <p>
     * 実際にタブへ変換されるのは<b>バックスラッシュと {@code t} の 2 文字</b>を渡した場合
     * （YAML では {@code '\t'} などシングルクォート記法）である。本体スキーマ
     * {@code $defs.directives.properties.field-separator.description} も
     * 「タブは {@code field-separator: "\\t"} と記述する」と書いており、**この挙動と一致している**
     * （2026-08-19 実測。以前この Javadoc が引いていた「{@code "\t"} と記述するとタブ文字に変換される」は
     * 現行スキーマに存在しない）。
     * {@code coverage/issues.md} <b>YML-08</b> の根拠テスト。
     * </p>
     */
    @Test
    public void failsWhenFieldSeparatorIsWrittenAsActualTab() {
        // Given / When
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> YamlFixture.read(dir(), ""
                        + "setup_files:\n"
                        + "  - path: \"f.csv\"\n"
                        + "    type: \"variable\"\n"
                        + "    directives:\n"
                        + "      field-separator: \"\\t\"\n"
                        + "    records:\n"
                        + "      - fields:\n"
                        + "          - {name: \"c1\", type: \"半角英字\"}\n"
                        + "        rows:\n"
                        + "          - [\"a\"]\n"));

        // Then
        assertThat("trim() でタブが失われ、空文字として報告される",
                thrown.getMessage(), containsString("field-separator must be one character"));
    }

    // ------------------------------------------------------------------ YML-11 引用符なしスカラーの値落ち

    /**
     * Given: 前後に空白を付けた値を<b>引用符なし</b>で書いた YAML（{@code - V:   pad  }）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>例外にも警告にもならず</b>前後の空白が消え、{@code "pad"} が入る。
     *
     * <p>
     * <b>この入力はスキーマ上の仕様内である。</b>{@code rows} の値の型は {@code ["string","null"]} であり、
     * 引用符なしのプレーンスカラーも文字列として通る。空白が消えるのは YAML の仕様
     * （プレーンスカラーは前後の空白を含まない）だが、変換ツールから見ると
     * <b>作成者が書いた値と中間モデルの値が食い違う</b>。
     * {@code coverage/issues.md} に <b>YML-11</b> として記録した（{@code src/main} は無変更）。
     * </p>
     *
     * <p>
     * 引用符付きの {@code "  pad  "}（D2-11）は空白が保たれる
     * （{@link YamlFormatReaderScalarTest#readsSurroundingWhitespacePreserved}）。
     * </p>
     *
     * <p>担保する軸要素: なし（軸A〜F のどの要素にも新しい担保を与えない。YML-11 の根拠テスト）。</p>
     */
    @Test
    public void dropsSurroundingSpacesFromUnquotedScalar() {
        // Given / When
        YamlFixture.Reading reading = YamlFixture.readCapturingWarnings(dir(), ""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - V:   pad  \n");

        // Then
        TableDataBlock block = YamlFixture.onlyBlock(reading.container(), TableDataBlock.class);
        assertThat("前後の空白が消える", block.getRows(), is(Arrays.asList(Arrays.asList("pad"))));
        assertThat("JUL 経路にも警告が出ない", reading.warnings(), is(Collections.<String>emptyList()));
    }

    /**
     * Given: {@code #} を含む値を<b>引用符なし</b>で書いた YAML（{@code - V: a #b}）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>例外にも警告にもならず</b> {@code #} 以降がコメントとして落ち、{@code "a"} が入る。
     *
     * <p>
     * {@link #dropsSurroundingSpacesFromUnquotedScalar} と同じく YAML の仕様どおりだが、
     * 書いた値の一部が黙って消える点は同じである（{@code coverage/issues.md} <b>YML-11</b>）。
     * 引用符付きの {@code "a #b"}（D2-12）はそのまま入る
     * （{@link YamlFormatReaderScalarTest#readsHashContainingStringAsIs}）。
     * </p>
     *
     * <p>担保する軸要素: なし（YML-11 の根拠テスト）。</p>
     */
    @Test
    public void dropsCommentPartFromUnquotedScalarContainingHash() {
        // Given / When
        YamlFixture.Reading reading = YamlFixture.readCapturingWarnings(dir(), ""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - V: a #b\n");

        // Then
        TableDataBlock block = YamlFixture.onlyBlock(reading.container(), TableDataBlock.class);
        assertThat("# 以降が消える", block.getRows(), is(Arrays.asList(Arrays.asList("a"))));
        assertThat("JUL 経路にも警告が出ない", reading.warnings(), is(Collections.<String>emptyList()));
    }

    /**
     * Given: {@code field-separator} を<b>バックスラッシュと {@code t} の 2 文字</b>で書いた可変長ファイル
     *        （YAML のシングルクォート記法 {@code '\t'}。エスケープが働かないためリテラル 2 文字になる）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 器がタブ 1 文字へ変換したうえで、中間モデルには辺①と同じ 2 文字記法（{@code \t}）が入る。
     *
     * <p>
     * <b>{@link #failsWhenFieldSeparatorIsWrittenAsActualTab} との切り分けである。</b>
     * 実タブ 1 文字を渡すと器の {@code trim()} で失われて例外になるのに対し、
     * 2 文字記法（スキーマの description が推奨する形）は通る。
     * YML-08 の record-separator 側が {@code readsRecordSeparatorWrittenAsLiteralNewlineAsNoneSymbol} と
     * {@code readsRecordSeparatorSymbolAsSymbol} の対で書かれているのに合わせ、
     * field-separator 側にも「通る側」を置く。
     * </p>
     *
     * <p>担保する軸要素: なし（YML-08 の切り分け）。</p>
     */
    @Test
    public void readsFieldSeparatorWrittenAsEscapedTabNotation() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_files:\n"
                + "  - path: \"f.csv\"\n"
                + "    type: \"variable\"\n"
                + "    directives:\n"
                + "      field-separator: '\\t'\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"c1\", type: \"半角英字\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");

        // Then
        FileDataBlock block = YamlFixture.onlyBlock(container, FileDataBlock.class);
        assertThat("器はタブ 1 文字へ変換するが、中間モデルへは 2 文字記法へ戻して入れる",
                block.getDirectives().get("field-separator"), is("\\t"));
    }

    // ------------------------------------------------------------------ YML-10 カラム名の大小衝突

    /**
     * Given: テーブル系の {@code rows} に、大小だけが違う 2 つのキー {@code id} と {@code ID} を書いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>例外にも警告にもならず</b>、カラム名は大文字化されて {@code [ID, ID]} と重複し、
     *        {@code id} に書いた値（{@code "1"} ／ {@code "3"}）は中間モデルのどこにも残らない。
     *
     * <p>
     * <b>この入力はスキーマ上の仕様内である。</b>{@code $defs.table_data.properties.rows.items} は
     * {@code {"type": "object", "additionalProperties": {"type": ["string", "null"]}}} で、
     * キーの大小にも一意性にも制約が無い。YAML としても {@code id} と {@code ID} は別キーである。
     * </p>
     *
     * <p>
     * 原因は nablarch-testing の {@code TableData} が、カラム名（コンストラクタ）と行 Map のキー
     * （値の格納時）をそれぞれ {@code toUpperCase()} することである。大文字化後に同名となった
     * 2 カラムは 1 つの Map エントリへ潰れ、後勝ちの値だけが残る。
     * {@code YamlFormatReader#addTableBlocks} は器が返した列名でそのまま値を引くため、
     * 同じ値が 2 回並ぶ。{@code coverage/issues.md} に <b>YML-10</b> として記録した
     * （{@code src/main} は無変更）。
     * </p>
     *
     * <p>
     * <b>本テストは他責の現状を固定したものである。</b>記法はテーブル系のカラム名の大小の扱いに
     * いっさい触れていない。converter に大文字化を止める権限も無いため、現状を記録に留めている
     * （{@code coverage/issues.md} <b>XLS-40</b> のカラム名側）。
     * <b>「あるべき姿」を主張していた {@code @Ignore} つきテストは #35 で削除した</b>
     * （記法に無い書き方は追わない）。
     * </p>
     *
     * <p>担保する軸要素: なし（軸A〜F のどの要素にも新しい担保を与えない。YML-10 の根拠テスト）。</p>
     */
    @Test
    public void dropsValueWhenTableColumnNamesDifferOnlyByCase() {
        // Given / When
        YamlFixture.Reading reading = YamlFixture.readCapturingWarnings(dir(), ""
                + "setup_tables:\n"
                + "  - table: \"my_table\"\n"
                + "    rows:\n"
                + "      - id: \"1\"\n"
                + "        ID: \"2\"\n"
                + "      - id: \"3\"\n"
                + "        ID: \"4\"\n");

        // Then
        assertThat("JUL 経路にも警告が出ない（辺①は同じ重複で WARNING を出す）",
                reading.warnings(), is(Collections.<String>emptyList()));
        TableDataBlock block = YamlFixture.onlyBlock(reading.container(), TableDataBlock.class);
        assertThat("テーブル名も大文字化される", block.getIdentifier(), is("MY_TABLE"));
        assertThat("大文字化により列名が重複する", block.getColumnNames(), is(Arrays.asList("ID", "ID")));
        assertThat("後勝ちの値だけが残り、同じ値が 2 回並ぶ", block.getRows(),
                is(Arrays.asList(Arrays.asList("2", "2"), Arrays.asList("4", "4"))));
    }

    /**
     * Given: LIST_MAP の {@code rows} に、大小だけが違う 2 つのキー {@code id} と {@code ID} を書いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : カラム名は<b>原文の大小のまま</b> {@code [id, ID]} で保たれ、両方の値が残る。
     *
     * <p>
     * テーブル系（{@link #dropsValueWhenTableColumnNamesDifferOnlyByCase}）との<b>非対称</b>を固定する。
     * LIST_MAP は {@code TableData} を経由せず、{@code YamlFormatReader#nonMarkerColumns} が
     * {@code YamlSection#resolveColumns} の生キーをそのまま使うため大文字化されない。
     * </p>
     *
     * <p>担保する軸要素: なし（YML-10 の対比。テーブル系との非対称を示す）。</p>
     */
    @Test
    public void keepsOriginalColumnCaseInListMap() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "list_maps:\n"
                + "  - id: \"lm\"\n"
                + "    rows:\n"
                + "      - id: \"1\"\n"
                + "        ID: \"2\"\n");

        // Then
        ListMapBlock block = YamlFixture.onlyBlock(container, ListMapBlock.class);
        assertThat("原文の大小がそのまま残る", block.getColumnNames(), is(Arrays.asList("id", "ID")));
        assertThat("どちらの値も失われない", block.getRows(), is(Arrays.asList(Arrays.asList("1", "2"))));
    }
    // ------------------------------------------------------------------ XLS-45 可変長ファイルの length

    /**
     * Given: 可変長ファイルのフィールド定義に {@code length} を書いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code IllegalArgumentException} で止まる。
     *
     * <p>
     * NTF 仕様として、可変長ファイルでは {@code length} を書けない（ユーザー確定・2026-08-24。
     * {@code coverage/issues.md} <b>XLS-45</b>）。Excel 記法に可変長のフィールド長行が無く
     * （記法は固定長との違いは、可変長ファイルの
     * 場合はフィールド長行を記載しない点のみである。）、書ける先の無い値を中間モデルが保持できると
     * 辺③で黙って落ちるためである。拒否するのは<b>中間モデルの生成時</b>であって書き出し側ではない。
     * </p>
     *
     * <p>
     * <b>本テストは実ファイル経路である。</b>{@code loadRawMap} を差し替える in-memory 経路
     * （{@code YamlFormatReaderTest}）では YAML のパースもスキーマ検証も通らないため、
     * 「この YAML が落ちる」ことの担保にならない。
     * </p>
     *
     * <p>
     * <b>スキーマ検証は通る。</b>本体スキーマ {@code $defs.field_def.properties.length} は可変長で
     * {@code length} を禁じておらず（「可変長ファイルでは不要（省略可）」とだけ書く）、
     * {@code YamlTestDataValidator} はそのスキーマをクラスパスから読むだけである。
     * <b>そのため今は、converter 側の中間モデルの生成時に {@code IllegalArgumentException} で落ちる。</b>
     * 本テストはその型とメッセージの両方を主張している。
     * </p>
     *
     * <p>
     * <b>スキーマ側の対応が入れば、本テストは落ちる。</b>落ちる段がスキーマ検証まで前へ動き、
     * 例外が {@code YamlSchemaValidationException} へ変わるためである。同クラスは
     * {@code IllegalStateException} を継承しており（nablarch-testing-yaml の
     * {@code YamlSchemaValidationException}
     * 「{@code public class YamlSchemaValidationException extends IllegalStateException}」）、
     * {@code IllegalArgumentException} のサブクラスではない。{@code YamlFixture.read} が包み直さないことは、
     * 同じ経路でこの例外をそのまま受ける {@link #failsWithSchemaValidationExceptionWhenFieldTypeIsMissing}
     * から分かる。<b>そのときは本テストを、スキーマ違反を主張する形（{@code assertSchemaViolation}）へ
     * 書き替える。</b>
     * </p>
     *
     * <p>担保する軸要素: なし（XLS-45 の根拠テスト。辺②の実ファイル経路で落ちることの担保）。</p>
     */
    @Test
    public void rejectsVariableFileFieldWithLengthFromRealYaml() {
        // Given / When
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> YamlFixture.read(dir(), ""
                        + "setup_files:\n"
                        + "  - path: \"v.csv\"\n"
                        + "    type: \"variable\"\n"
                        + "    records:\n"
                        + "      - fields:\n"
                        + "          - {name: \"c1\", type: \"半角英字\", length: \"10\"}\n"
                        + "        rows:\n"
                        + "          - [\"a\"]\n"));

        // Then
        assertThat(thrown.getMessage(),
                containsString("可変長ファイルでフィールド長を持つフィールド定義は保持できません"));
        assertThat("どのファイルのどのフィールドかが分かること",
                thrown.getMessage(), containsString("フィールド名=[c1]"));
    }
}
