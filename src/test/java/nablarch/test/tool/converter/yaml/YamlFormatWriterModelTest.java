package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
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
 * <b>#25 のレビューで、送信同期 3 種（A-12 {@code EXPECTED_REQUEST_BODY_MESSAGES}／
 * A-13 {@code RESPONSE_HEADER_MESSAGES}／A-14 {@code RESPONSE_BODY_MESSAGES}）が
 * #18 以来 ✅ と判定されていながら実際は 🔺 だったことが判明したため、3 件を追加した。</b>
 * あわせて、キーのクォート判定（{@code YamlFormatWriter#isPlainSafeKey}）が特殊文字集合の
 * ほとんどについて未固定だったため 1 件を、軸D の D4-02／D4-08 を {@code YamlFormatWriter#emitMap}
 * 経路でも観測するため 1 件を追加した。経緯は各メソッドの Javadoc と
 * {@code inventory.md} §4.1-2 の軸A 表・「軸D の測定経路」にある。
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
 * 順序を主張するフィクスチャは、<b>定義順・辞書順のいずれとも違う並び</b>で組み立てている。
 * 並びが辞書順・定義順と一致していると、順序を壊す変更を入れてもテストが通ってしまうためである。
 * <b>本クラスのフィクスチャは 5 つとも下記のとおりずらしてある（新しく足すときはここへ加えること）。</b>
 * </p>
 *
 * <ul>
 *   <li>セクションキー {@code setup_files} → {@code expected_files}
 *       ＝ {@code DataType} の定義順とも辞書順とも逆</li>
 *   <li>ディレクティブ {@code text-encoding} → {@code file-type} ＝辞書順の逆</li>
 *   <li>{@code fw_header} のフィールド {@code resendFlag} → {@code dateSent} ＝辞書順の逆</li>
 *   <li>レコード断片のフィールド {@code flag} → {@code date} ＝辞書順の逆（{@link #record()}）</li>
 *   <li>カラム {@code zip} → {@code name} ＝辞書順の逆</li>
 * </ul>
 *
 * <p>
 * <b>本クラスのアサーションは原則として「実行して観測した現状の挙動」である。</b>期待される仕様ではない。
 * 妥当でないと判断した挙動は {@code .rn/ntf-test-data-converter/coverage/issues.md} に課題
 * （{@code YML-12}）として記録した。
 * このうち <b>{@code YML-12} の 1 形目</b>（レコードが空のファイルブロックで {@code records:} キー自体が
 * 書かれず、読み戻しがスキーマの {@code required} 違反になる）は <b>#25.5 で修正済み</b>で、該当テストは
 * 現状の固定ではなく<b>記法どおりの仕様</b>（{@code records: []} を書く）を書いている。
 * 残る 2〜4 形目（メッセージブロックの {@code records} 欠落・{@code fields} 欠落・{@code type} 欠落）は
 * 今回の対象外のため、現状の固定のままである。
 * </p>
 *
 * @author kiyobot
 */
public class YamlFormatWriterModelTest {

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
     * ブロック 1 件を実ファイルへ書き出し、本番配線の {@link YamlFormatReader} で読み戻す。
     *
     * <p>
     * 唯一のブロックの取り出しは呼び出し側が {@link YamlFixture#onlyBlock(TestDataContainer, Class)} で行う
     * （素キャストを避け、想定外のクラスが来たときに失敗メッセージへ実クラス名が出るようにするため。
     * 同ヘルパの Javadoc に共有意図が書いてある）。
     * </p>
     *
     * @param block ブロック
     * @return 読み戻した中間モデル
     */
    private TestDataContainer writeAndReadBack(TestDataBlock block) {
        return new YamlFormatReader().read(write(block), "td");
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
     * 値 1 行 2 フィールドのレコードレイアウトを組み立てる（{@code record_type} は省略）。
     *
     * <p>
     * <b>値は {@code "true"}（真偽値に見える文字列）と {@code "2026-08-07"}（日付に見える文字列）である。</b>
     * レコード断片（{@code records[].rows}）経路は {@code YamlFormatWriter#rowFlow} を通り、
     * {@code setup_tables} の {@code rows} 経路（{@code YamlFormatWriterScalarTest} が測っている経路）とは
     * 別の呼び出し元である。したがって軸D の D4-02／D4-08 の記法が<b>この経路でも</b>
     * アサートされている状態を作るために、素の値（{@code "v"} など）でなくこの 2 値を置いてある。
     * <b>これが示すのは「その 2 値がこの経路でアサートされている」ことまでである</b> ——
     * 当初版（素の値）では「{@code rowFlow} がこの 2 値だけ非クォートで書く」変異が生存したが、
     * 条件を付けない「{@code rowFlow} からクォートを外す」変異なら当初版でも既存テストが落ちる。
     * 詳細は {@code inventory.md} §4.1-2 の「軸D の測定経路」にある。
     * フィールド名の並び {@code flag} → {@code date} は辞書順の逆である。
     * </p>
     *
     * @return レコードレイアウト
     */
    private static RecordLayout record() {
        return new RecordLayout(null,
                Arrays.asList(new FieldDef("flag", "半角英字", "5"), new FieldDef("date", "半角英字", "10")),
                Collections.singletonList(Arrays.asList("true", "2026-08-07")));
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
     * A-08（{@code SETUP_VARIABLE} → {@code setup_files}）。
     * §4.1 では 2 種とも {@code RoundTripTest} 経由の 🔺 だけだった。
     * E-1(複数) は既存の {@code YamlFormatWriterTest#serialize_multipleSections_separatedByBlankLineInEncounterOrder}
     * が通しており、本メソッドは<b>並びを辞書順・定義順とずらした版</b>として足しているだけである
     * （台帳 §4.1-2 は本クラスの担う軸に E-1 を挙げていない）。
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
                + "          - {name: \"flag\", type: \"半角英字\", length: \"5\"}\n"
                + "          - {name: \"date\", type: \"半角英字\", length: \"10\"}\n"
                + "        rows:\n"
                + "          - [\"true\", \"2026-08-07\"]\n"
                + "\n"
                + "expected_files:\n"
                + "  - path: \"e.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"flag\", type: \"半角英字\", length: \"5\"}\n"
                + "          - {name: \"date\", type: \"半角英字\", length: \"10\"}\n"
                + "        rows:\n"
                + "          - [\"true\", \"2026-08-07\"]\n"));
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
        FileDataBlock back = YamlFixture.onlyBlock(writeAndReadBack(block), FileDataBlock.class);

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
        FileDataBlock back = YamlFixture.onlyBlock(writeAndReadBack(block), FileDataBlock.class);

        // Then
        assertThat(back.getDataType(), is(DataType.SETUP_VARIABLE));
        assertThat(back.getFileType(), is(FileDataBlock.FileType.VARIABLE));
        assertThat(back.getIdentifier(), is("s.csv"));
    }

    // ------------------------------------------------------------------ A-12 / A-13 / A-14

    /**
     * 送信同期のメッセージブロックを 1 件組み立てる（4 種で {@code DataType} 以外は同一）。
     *
     * @param type データ種別
     * @return ブロック
     */
    private static MessageDataBlock sendSync(DataType type) {
        return new MessageDataBlock(type, "[case1]", "MSG1", map(), map(),
                Collections.singletonList(record()));
    }

    /**
     * {@link #sendSync(DataType)} 1 件だけを持つセクションの、期待する出力 YAML を組み立てる。
     *
     * <p>
     * セクションキー以外は 4 種で完全に同一である。すなわち<b>この文字列全体との一致を見れば、
     * 写像が入れ替わったときに必ず落ちる</b>。
     * </p>
     *
     * @param sectionKey 期待するセクションキー
     * @return YAML テキスト
     */
    private static String sendSyncYaml(String sectionKey) {
        return ""
                + sectionKey + ":\n"
                + "  - group_id: \"case1\"\n"
                + "    id: \"MSG1\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"flag\", type: \"半角英字\", length: \"5\"}\n"
                + "          - {name: \"date\", type: \"半角英字\", length: \"10\"}\n"
                + "        rows:\n"
                + "          - [\"true\", \"2026-08-07\"]\n";
    }

    /**
     * Given: {@code EXPECTED_REQUEST_BODY_MESSAGES} のブロック 1 件だけを持つセクション。
     * When : {@code serialize}。
     * Then : 出力全文が {@code expected_request_body_messages:} 配下の 1 ブロックと一致する。
     *
     * <p>
     * 担保する軸要素: A-12。<b>#18 は ✅ と判定していたが誤りだった</b>（下記）。
     * </p>
     *
     * <p>
     * 既存の {@code YamlFormatWriterTest#serializeSendSync_allFourSectionKeys} は送信同期 4 種を
     * <b>まとめて 1 つの出力に</b>直列化し、4 つのキー文字列が「どこかに現れる」ことを
     * {@code assertTrue(yaml.contains(...))} で見ているだけで、{@code DataType} → セクションキーの対応を
     * 1 つも固定していない。4 種の写像を入れ替えても 4 キーはすべて現れるため通ってしまう
     * （#25 レビューの変異で実測）。担保があったのは、単独ブロックの出力全文を完全一致で見ている
     * {@code #serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField} が通す A-11 だけである。
     * <b>辺③がまったく同じ形の欠陥を #23 のレビューで見つけている</b>（{@code inventory.md} §3.1-3）。
     * </p>
     */
    @Test
    public void writesExpectedRequestBodyMessagesUnderItsOwnSectionKey() {
        assertThat(serialize(sendSync(DataType.EXPECTED_REQUEST_BODY_MESSAGES)),
                is(sendSyncYaml("expected_request_body_messages")));
    }

    /**
     * Given: {@code RESPONSE_HEADER_MESSAGES} のブロック 1 件だけを持つセクション。
     * When : {@code serialize}。
     * Then : 出力全文が {@code response_header_messages:} 配下の 1 ブロックと一致する。
     *
     * <p>担保する軸要素: A-13（経緯は {@code #writesExpectedRequestBodyMessagesUnderItsOwnSectionKey}）。</p>
     */
    @Test
    public void writesResponseHeaderMessagesUnderItsOwnSectionKey() {
        assertThat(serialize(sendSync(DataType.RESPONSE_HEADER_MESSAGES)),
                is(sendSyncYaml("response_header_messages")));
    }

    /**
     * Given: {@code RESPONSE_BODY_MESSAGES} のブロック 1 件だけを持つセクション。
     * When : {@code serialize}。
     * Then : 出力全文が {@code response_body_messages:} 配下の 1 ブロックと一致する。
     *
     * <p>担保する軸要素: A-14（経緯は {@code #writesExpectedRequestBodyMessagesUnderItsOwnSectionKey}）。</p>
     */
    @Test
    public void writesResponseBodyMessagesUnderItsOwnSectionKey() {
        assertThat(serialize(sendSync(DataType.RESPONSE_BODY_MESSAGES)),
                is(sendSyncYaml("response_body_messages")));
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
     * 期待する中身は<b>リテラルで置いている</b>（{@code writer.serialize(...)} と突き合わせると
     * 実装の出力同士を比べることになり、直列化そのものが壊れても気づけないためである。#25 レビュー指摘）。
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
        assertThat(read(out, zebra), is(oneRowTableYaml("Z")));
        assertThat(read(out, alpha), is(oneRowTableYaml("A")));
        assertThat(read(out, mango), is(oneRowTableYaml("M")));
    }

    /**
     * {@link #table(String)} 1 件だけを持つセクションの、期待する出力 YAML を組み立てる。
     *
     * @param name テーブル名
     * @return YAML テキスト
     */
    private static String oneRowTableYaml(String name) {
        return ""
                + "setup_tables:\n"
                + "  - table: \"" + name + "\"\n"
                + "    rows:\n"
                + "      - C: \"1\"\n";
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
        Path file = new File(dir, section.getName() + YamlFixture.EXTENSION).toPath();
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
     * Then : {@code records: []}（空配列）が書かれる。ディレクティブは記述順のまま、値はダブルクォート付きで出る。
     *
     * <p>
     * 記法仕様は「0バイトの空ファイルは、レコード定義を持たないファイルデータブロックとして表現する」
     * （{@code testdata_notation.rst:879}）、「{@code setup_files}・{@code expected_files} の各エントリには
     * {@code path}・{@code type}・{@code records} の3キーが必須であり、いずれかを省略するとエラーになる。
     * 0バイトの空ファイルを表現するには、{@code records:} に空配列 {@code []} を記載する」
     * （{@code testdata_notation.rst:1144}）と定めている。
     * </p>
     *
     * <p>
     * 担保する軸要素: C-12（{@code FileDataBlock.records} 空）。
     * ディレクティブの並び（{@code text-encoding} → {@code file-type}）は辞書順の逆である。
     * </p>
     */
    @Test
    public void writesEmptyRecordsListForFileBlockWithoutRecords() {
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
                + "      file-type: \"Fixed\"\n"
                + "    records: []\n"));
    }

    /**
     * Given: 真偽値に見える値と日付に見える値を持つ {@code fw_header} のメッセージブロック。
     * When : {@code serialize}。
     * Then : どちらの値もダブルクォート付きで書かれる。
     *
     * <p>
     * 担保する軸要素: D4-02（{@code "true"}）／D4-08（{@code "2026-08-07"}）を
     * <b>{@code YamlFormatWriter#emitMap} 経路で</b>。同メソッドは {@code directives} と
     * {@code fw_header} の 2 箇所から呼ばれる唯一の出力経路であり、
     * {@code YamlFormatWriterScalarTest} が軸D を測っている {@code setup_tables} の {@code rows} 経路
     * （{@code YamlSeq#prop}）とは別の呼び出し元である。
     * </p>
     *
     * <p>
     * {@code fw_header} を選んだのは、フィールド名が利用者定義であり
     * <b>再送フラグ・送信日付という実在しうる組み合わせで真偽値風・日付風の値を置ける</b>ためである
     * （{@code directives} のキーは {@code file-type} ／ {@code text-encoding} など語彙が決まっており、
     * 日付を取るものが無い）。フィールド名の並び {@code resendFlag} → {@code dateSent} は<b>辞書順の逆</b>
     * （{@code LC_ALL=C sort} は {@code dateSent} → {@code resendFlag} に並べる）であり、
     * 値の並び {@code "true"} → {@code "2026-08-07"} も辞書順の逆である。
     * したがって {@code emitMap} がキーを辞書順へ並べ替える変異を入れると本メソッドが落ちる。
     * </p>
     *
     * <p>
     * <b>本メソッドが示すのは「この 2 値がこの経路でアサートされている」ことまでである。</b>
     * {@code emitMap} からクォートを外す変異（条件なし）は本メソッドが無くても既存テストが捉える。
     * 粒度の議論は {@code inventory.md} §4.1-2 の「軸D の測定経路」にある。
     * </p>
     */
    @Test
    public void quotesBooleanAndDateLookingValuesInFwHeader() {
        // Given
        MessageDataBlock block = new MessageDataBlock(DataType.MESSAGE, "", "RM01",
                map("text-encoding", "UTF-8"),
                map("resendFlag", "true", "dateSent", "2026-08-07"),
                Collections.<RecordLayout>emptyList());

        // When / Then
        assertThat(serialize(block), is(""
                + "messages:\n"
                + "  - id: \"RM01\"\n"
                + "    directives:\n"
                + "      text-encoding: \"UTF-8\"\n"
                + "    fw_header:\n"
                + "      resendFlag: \"true\"\n"
                + "      dateSent: \"2026-08-07\"\n"));
    }

    // ------------------------------------------------------------------ キーのクォート

    /**
     * クォートを要するキーと、期待する出力上の表記。
     *
     * <p>
     * 1 列目は生のキー（{@code x} ＋ 対象文字）、2 列目は {@code directives:} 配下に現れるはずの表記である。
     * 対象文字は {@code YamlFormatWriter#isPlainSafeKey} の特殊文字集合
     * {@code "'#:,[]{}&*!|>%@`?}（<b>18 文字</b>）の全部と、制御文字（{@code < 0x20}）2 種である。
     * 期待表記は<b>実行して観測した結果</b>であり、実装から導出していない。
     * </p>
     */
    private static final String[][] KEYS_REQUIRING_QUOTES = {
            {"x\"", "\"x\\\"\""},
            {"x'", "\"x'\""},
            {"x#", "\"x#\""},
            {"x:", "\"x:\""},
            {"x,", "\"x,\""},
            {"x[", "\"x[\""},
            {"x]", "\"x]\""},
            {"x{", "\"x{\""},
            {"x}", "\"x}\""},
            {"x&", "\"x&\""},
            {"x*", "\"x*\""},
            {"x!", "\"x!\""},
            {"x|", "\"x|\""},
            {"x>", "\"x>\""},
            {"x%", "\"x%\""},
            {"x@", "\"x@\""},
            {"x`", "\"x`\""},
            {"x?", "\"x?\""},
            {"x" + (char) 0x01, "\"x\\x01\""},
            {"x" + (char) 0x1f, "\"x\\x1f\""},
    };

    /**
     * Given: {@code YamlFormatWriter#isPlainSafeKey} の特殊文字集合 18 文字と制御文字 2 種を
     *        1 文字ずつ含む {@code directives} のキー。
     * When : {@code serialize}。
     * Then : 20 通りすべてで、そのキーがダブルクォートで囲まれた出力全文になる。
     *
     * <p>
     * 担保する軸要素: なし（{@code isPlainSafeKey} の判定そのもの。軸D はキーではなく<b>値</b>の表現である）。
     * </p>
     *
     * <p>
     * <b>既存の担保はコロン・空白・空文字・先頭 {@code -} の 4 つだけだった</b>
     * （{@code YamlFormatWriterTest#serialize_quotesKeyContainingSpecialChars} ／
     * {@code #serialize_emptyKey_isQuoted} ／ {@code #serialize_keyStartingWithIndicator_isQuoted}）。
     * 集合から {@code #} を 1 文字外すだけで全件が通ってしまうこと（生存変異）を #25 レビューが実測しており、
     * 実害は大きい —— {@code #} が外れるとカラム名 {@code #x} の行が {@code - #x: "v"} となって
     * <b>行全体が YAML コメント化し、データ行が黙って消える</b>。
     * あわせて JaCoCo で未到達だった「キーに制御文字を含む」枝もここで閉じる。
     * </p>
     *
     * <p>
     * <b>本メソッドは「1 ケース 1 {@code @Test}」の規約から意図して逸脱している。</b>
     * 規約は {@code XlsFormatWriterInvalidOutputTest} の F3-04 節と
     * {@code XlsFormatWriterCellTypeTest} のコメントにあり、その理由は
     * 「ループで束ねると最初の 1 文字が落ちた時点で残りが実行されず、どの文字で挙動が違うのかが
     * 分からなくなるため」である。ここでループにしてよいと判断した根拠は 2 つある。
     * </p>
     *
     * <ol>
     *   <li><b>20 ケースは 1 つの振る舞いである。</b>いずれも
     *       {@code YamlFormatWriter#isPlainSafeKey} の同一の判定 1 つ
     *       （特殊文字集合の {@code indexOf} と {@code c < 0x20} の制御文字ガード）を通り、
     *       通れば一律に {@code q(k)} でクォートされる。Xls 側の禁止文字 7 件のように
     *       <b>文字ごとに違うメッセージ・違う挙動</b>（例外になる／切り詰めで黙って書けてしまう）を
     *       持つわけではない。</li>
     *   <li><b>失敗を集約したので規約の理由が成立しない。</b>ループ内では判定するだけで、
     *       アサートはループ後に 1 回だけ行う。したがって 1 件目が落ちても残り 19 ケースは実行され、
     *       失敗メッセージには落ちた全ケースが出る。</li>
     * </ol>
     *
     * <p>この逸脱は {@code inventory.md} §4.1-2 にも記録してある。</p>
     */
    @Test
    public void quotesDirectiveKeyContainingAnyYamlSpecialOrControlCharacter() {
        List<String> failures = new ArrayList<>();
        for (String[] pair : KEYS_REQUIRING_QUOTES) {
            // Given
            FileDataBlock block = new FileDataBlock(DataType.SETUP_FIXED, "", "k.dat",
                    FileDataBlock.FileType.FIXED, map(pair[0], "v"),
                    Collections.<RecordLayout>emptyList());
            String expected = ""
                    + "setup_files:\n"
                    + "  - path: \"k.dat\"\n"
                    + "    type: \"fixed\"\n"
                    + "    directives:\n"
                    + "      " + pair[1] + ": \"v\"\n"
                    + "    records: []\n";

            // When（ここでは判定だけ。アサートはループ後に 1 回）
            String actual = serialize(block);
            if (!expected.equals(actual)) {
                failures.add("キー " + pair[1] + " は " + escapeNewlines(expected)
                        + " になるはずが " + escapeNewlines(actual) + " になった");
            }
        }

        // Then
        assertThat(KEYS_REQUIRING_QUOTES.length + " ケース中 " + failures.size() + " 件が期待どおりに"
                        + "クォートされなかった", failures, is(Collections.<String>emptyList()));
    }

    /**
     * 失敗メッセージを 1 行に収めるため、改行をエスケープ表記へ置き換える。
     *
     * @param yaml YAML テキスト
     * @return 改行を {@code \n} の 2 文字へ置き換えた文字列
     */
    private static String escapeNewlines(String yaml) {
        return yaml.replace("\n", "\\n");
    }

    // ------------------------------------------------------------------ 書けるが読み戻せない形（YML-12）

    /**
     * Given: レコードレイアウトを 1 件も持たないファイルブロック。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : スキーマ違反にならず、レコード 0 件のファイルブロックとして戻る。
     *
     * <p>
     * 本体スキーマの {@code $defs.file_data.required} は {@code records} を要求するが
     * {@code minItems} は 0 であり、<b>空配列なら通る</b>（キーごと省略すると通らない）。
     * 記法仕様も 0 バイトの空ファイルを {@code records: []} で表すと定めている
     * （{@code testdata_notation.rst:1144}）。
     * </p>
     */
    @Test
    public void readsBackFileBlockWithEmptyRecords() {
        // Given / When
        FileDataBlock back = YamlFixture.onlyBlock(writeAndReadBack(
                new FileDataBlock(DataType.SETUP_FIXED, "", "n.dat", FileDataBlock.FileType.FIXED,
                        map(), Collections.<RecordLayout>emptyList())), FileDataBlock.class);

        // Then
        assertThat(back.getIdentifier(), is("n.dat"));
        assertThat(back.getFileType(), is(FileDataBlock.FileType.FIXED));
        assertThat(back.getRecords().size(), is(0));
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
     *
     * <p>
     * <b>本メソッドが固定しているのは「スキーマが認めない形を書けてしまう」という現状の記録であって、
     * NTF の仕様ではない。</b>緑であることは「仕様どおり」を意味しない。#25.5 で修正したのは YML-12 の
     * 1 形目（ファイルブロックの {@code records} 欠落）だけで、本形はユーザ確定のスコープ外として
     * 残置している。残置の一覧は {@code coverage/issues.md} の「残置している『緑の嘘』」にまとめた。
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
     *
     * <p>
     * <b>本メソッドが固定しているのは「スキーマが認めない形を書けてしまう」という現状の記録であって、
     * NTF の仕様ではない。</b>緑であることは「仕様どおり」を意味しない。#25.5 で修正したのは YML-12 の
     * 1 形目（ファイルブロックの {@code records} 欠落）だけで、本形はユーザ確定のスコープ外として
     * 残置している。残置の一覧は {@code coverage/issues.md} の「残置している『緑の嘘』」にまとめた。
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
     *
     * <p>
     * <b>本メソッドが固定しているのは「スキーマが認めない形を書けてしまう」という現状の記録であって、
     * NTF の仕様ではない。</b>緑であることは「仕様どおり」を意味しない。#25.5 で修正したのは YML-12 の
     * 1 形目（ファイルブロックの {@code records} 欠落）だけで、本形はユーザ確定のスコープ外として
     * 残置している。残置の一覧は {@code coverage/issues.md} の「残置している『緑の嘘』」にまとめた。
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
        TableDataBlock back = YamlFixture.onlyBlock(writeAndReadBack(block), TableDataBlock.class);

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
