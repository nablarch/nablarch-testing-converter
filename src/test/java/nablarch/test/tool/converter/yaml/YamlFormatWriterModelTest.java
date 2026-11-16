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
 * 中間モデル → YAML の書き出しについて、{@code YamlFormatWriter} のデータタイプ・中間モデルのフィールド・
 * 件数のうち {@code YamlFormatWriterTest} が通していない中間モデルの形を固定するテスト。
 *
 * <p>
 * 対象はデータタイプ 2 件（{@code EXPECTED_FIXED}／{@code SETUP_VARIABLE}）・中間モデルのフィールド 2 件
 * （{@code sections} 空・複数／{@code FileDataBlock.records} 空）・件数 2 件
 * （コンテナ内セクション数 1 件・複数）である。セクション数 1 件は、出力ファイルの件数を見るアサートが
 * YAML の書き出しのテスト全体で複数件側の 1 か所しか無かったため足した。
 * </p>
 *
 * <p>
 * <b>送信同期 3 種（{@code EXPECTED_REQUEST_BODY_MESSAGES}／{@code RESPONSE_HEADER_MESSAGES}／
 * {@code RESPONSE_BODY_MESSAGES}）のセクションキーは、既存テストが担保しているように見えて実際には
 * 固定されていなかったため、3 件を追加した。</b>
 * あわせて、YAML の特殊文字を含むキーが囲まれて書かれることを確かめる 1 件を、値 {@code "true"}／{@code "2026-08-07"} の記法を
 * {@code YamlFormatWriter#emitMap} 経路でも観測するため 1 件を追加した。経緯は各メソッドの Javadoc にある。
 * </p>
 *
 * <p>
 * <b>あわせて「書けるが読み戻せない YAML」を固定する。</b>{@link YamlFormatWriter} は本体スキーマ
 * （yaml jar 内 {@code nablarch/test/ntf-testdata-yaml-schema.json}）を参照しないため、
 * スキーマが禁じる形の中間モデルを渡すと、例外にならずスキーマ違反の {@code .yaml} を書き出す。
 * 該当する 4 つの形（{@code records} 空 2 種／{@code fields} 空／{@code FieldDef.type} 省略）は
 * いずれも YAML の読み取り（スキーマ検証）では<b>到達できず</b>、
 * 中間モデル → YAML の書き出しからだけ作れる。
 * <b>ただし 4 形すべて修正済み</b>であり、本クラスには 1 つも残っていない。
 * <b>2 形目（電文の {@code records} 空）は書き出し側の番人ではなく中間モデルの生成時拒否へ移した</b>
 * （{@code MessageDataBlockTest#本文レコードが0件の電文ブロックは生成できない} ／
 * {@code #本文レコードが0件の送信系電文ブロックも生成できない}。移設は 2026-08-19）。
 * <b>3 形目（{@code fields} 空）は書き出し側の番人ではなく中間モデルの生成時拒否へ移した</b>
 * （{@code RecordLayoutTest#フィールドを1件も持たないレコードは生成できない}）。
 * <b>4 形目（{@code FieldDef.type} 省略）だけは書き出し側の番人ではなく中間モデルの生成時拒否へ移した</b>
 * （{@code FieldDefTest#データ型がnullのフィールド定義は生成できない}。
 * 不正値は書き出し側でなく中間モデルの生成時に拒否する方針による）。
 * 1 形目（ファイルブロックの {@code records} 空）だけは記法・スキーマとも<b>合法な形が存在する</b>ため
 * 弾くのではなく {@code records: []} を書くように直してあり、本クラスの
 * {@link #writesEmptyRecordsListForFileBlockWithoutRecords()}と
 * {@link #readsBackFileBlockWithEmptyRecords()}（読み戻し）が固定する。
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
 * <b>本クラスのアサーションは、中間モデルを YAML 形式へ写す規則（スキーマどおりの構造）である。</b>
 * スキーマが禁じる形の中間モデルから読み戻せない YAML が書き出されていた 4 つの形は、いずれも修正し、
 * 中間モデルが生成時に拒否する（下記）。{@link #failsToReadBackLiteralTabFieldSeparator} だけは、
 * 本体がディレクティブの値を {@code trim()} するために往復できない既知の制限を固定する。
 * </p>
 *
 * <ul>
 *   <li>レコードが空のファイルブロック（{@code records:} キー自体が書かれず、読み戻しがスキーマの
 *       {@code required} 違反になる）は、記法どおり {@code records: []} を書くように直した。該当テストは
 *       <b>記法どおりの仕様</b>を書いている。</li>
 *   <li>本文レコードが 0 件の電文ブロック（{@code records} 欠落）、フィールドが 0 件のレコード定義
 *       （{@code fields} 欠落）、データ型が {@code null} のフィールド定義（{@code type} 欠落）は、
 *       中間モデルの生成時に拒否するようにした。これらの形は中間モデルとして作れないため本クラスでは扱わず、
 *       {@code MessageDataBlockTest#本文レコードが0件の電文ブロックは生成できない}・
 *       {@code RecordLayoutTest#フィールドを1件も持たないレコードは生成できない}・
 *       {@code FieldDefTest#データ型がnullのフィールド定義は生成できない} が固定する。</li>
 * </ul>
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
     * 別の呼び出し元である。したがって {@code "true"}／{@code "2026-08-07"} の記法が<b>この経路でも</b>
     * アサートされている状態を作るために、素の値（{@code "v"} など）でなくこの 2 値を置いてある。
     * <b>これが示すのは「その 2 値がこの経路でアサートされている」ことまでである</b> ——
     * 当初版（素の値）では「{@code rowFlow} がこの 2 値だけ非クォートで書く」変異が生存したが、
     * 条件を付けない「{@code rowFlow} からクォートを外す」変異なら当初版でも既存テストが落ちる。
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
     * {@link #record()} の<b>可変長ファイル用</b>。フィールド長だけを落とし、名前・型・値は同じである。
     *
     * <p>
     * 可変長ファイルではフィールド定義がフィールド長を持てない（NTF 仕様として可変長ファイルでは
     * {@code length} を書けない。{@code FileDataBlock} の生成時に拒否される）ため、可変長のブロックを組む
     * テストは {@link #record()} を使えない。<b>長さ以外を揃えてあるので、固定長側との差は
     * 長さ行／{@code length} キーの有無だけになる。</b>
     * </p>
     *
     * @return フィールド長を持たないレコードレイアウト
     */
    private static RecordLayout variableRecord() {
        return new RecordLayout(null,
                Arrays.asList(new FieldDef("flag", "半角英字", null), new FieldDef("date", "半角英字", null)),
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

    // ------------------------------------------------------------------ EXPECTED_FIXED / SETUP_VARIABLE

    /**
     * Given: {@code SETUP_VARIABLE}（可変長）と {@code EXPECTED_FIXED}（固定長）を<b>この順</b>で持つセクション。
     * When : {@code serialize}。
     * Then : {@code setup_files:} → {@code expected_files:} の順に、空行で区切って書かれる。
     *
     * <p>
     * 担保する観点: {@code EXPECTED_FIXED} → {@code expected_files}／
     * {@code SETUP_VARIABLE} → {@code setup_files}。
     * それまで 2 種とも {@code RoundTripTest} 経由の往復でしか担保されておらず、出力のキーを直接見ていなかった。
     * セクション内ブロック数 複数は既存の {@code YamlFormatWriterTest#serialize_multipleSections_separatedByBlankLineInEncounterOrder}
     * が通しており、本メソッドは<b>並びを辞書順・定義順とずらした版</b>として足しているだけである。
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
        FileDataBlock setupVariable = new FileDataBlock(DataType.SETUP_VARIABLE, "", "s.csv", map(),
                Collections.singletonList(variableRecord()));
        FileDataBlock expectedFixed = new FileDataBlock(DataType.EXPECTED_FIXED, "", "e.dat", map(),
                Collections.singletonList(record()));

        // When / Then
        assertThat(serialize(setupVariable, expectedFixed), is(""
                + "setup_files:\n"
                + "  - path: \"s.csv\"\n"
                + "    type: \"variable\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"flag\", type: \"半角英字\"}\n"
                + "          - {name: \"date\", type: \"半角英字\"}\n"
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
     * Given: ファイル系 4 種それぞれのファイルブロック。
     * When : 直列化する。
     * Then : {@code type:} が<b>データ種別から</b>出る
     *        （{@code SETUP_FIXED} ／ {@code EXPECTED_FIXED} → {@code "fixed"}、
     *        {@code SETUP_VARIABLE} ／ {@code EXPECTED_VARIABLE} → {@code "variable"}）。
     *
     * <p>
     * 担保する観点: <b>{@code SETUP_FIXED} ／ {@code EXPECTED_FIXED} ／ {@code SETUP_VARIABLE} ／
     * {@code EXPECTED_VARIABLE}</b>。YAML の書き出しのトップレベルキーは
     * SETUP 系 → {@code setup_files} ／ EXPECTED 系 → {@code expected_files} の <b>2 対 1</b> であり
     * 、固定長／可変長を分けるのは {@code type:} だけである。
     * 中間モデルはかつてファイル種別を {@code DataType} と {@code fileType} の 2 つで持ち、{@code type:} は
     * 引数の {@code fileType} から出ていた。食い違う組み合わせを検査しなかったため、
     * <b>4 種のデータ種別を区別する出力が YAML の書き出しに無かった</b>。現在は {@code fileType} を
     * {@code DataType} から導出する。
     * </p>
     *
     * <p>
     * 検査の並びは {@code DataType} の定義順（{@code SETUP_FIXED} → {@code EXPECTED_FIXED} →
     * {@code SETUP_VARIABLE} → {@code EXPECTED_VARIABLE}）とずらしてある。
     * </p>
     */
    @Test
    public void writesFileTypeKeyDerivedFromDataType() {
        // Given/When/Then
        assertThat(serialize(new FileDataBlock(DataType.EXPECTED_VARIABLE, "", "e.csv",
                map(), Collections.singletonList(variableRecord()))), containsString("type: \"variable\""));
        assertThat(serialize(new FileDataBlock(DataType.SETUP_FIXED, "", "s.dat",
                map(), Collections.singletonList(record()))), containsString("type: \"fixed\""));
        assertThat(serialize(new FileDataBlock(DataType.EXPECTED_FIXED, "", "e.dat",
                map(), Collections.singletonList(record()))), containsString("type: \"fixed\""));
        assertThat(serialize(new FileDataBlock(DataType.SETUP_VARIABLE, "", "s.csv",
                map(), Collections.singletonList(variableRecord()))), containsString("type: \"variable\""));
    }

    /**
     * Given: ファイル系 4 種それぞれのファイルブロック。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す（中間モデル → YAML → 中間モデル）。
     * Then : データ種別が 4 種とも元のまま復元される。
     *
     * <p>
     * 導出前は {@code SETUP_FIXED} のブロックから {@code type: "variable"} が出せたため、
     * 読み戻すと {@code YamlFormatReader#fileDataType} が {@code SETUP_VARIABLE} を作り、
     * <b>黙って別種のブロックへ化けた</b>。
     * 出力する YAML は本体スキーマに適合してしまうため、この経路は検出できなかった。
     * 導出後はファイル種別を外から渡せないため、この経路が塞がる。
     * </p>
     *
     * <p>検査の並びは {@code DataType} の定義順とずらしてある。</p>
     */
    @Test
    public void restoresAllFourFileDataTypesThroughRealReader() {
        for (DataType type : Arrays.asList(DataType.SETUP_VARIABLE, DataType.EXPECTED_FIXED,
                DataType.EXPECTED_VARIABLE, DataType.SETUP_FIXED)) {
            // Given: 同じファイル名 td.yaml へ 4 回書くため、読み戻す前に YamlLoader のキャッシュを落とす
            YamlLoader.clearCacheForTest();
            FileDataBlock block = new FileDataBlock(type, "", "f.dat", map(),
                    Collections.singletonList(
                            FileDataBlock.fileTypeOf(type) == FileDataBlock.FileType.FIXED
                                    ? record() : variableRecord()));

            // When
            FileDataBlock back = YamlFixture.onlyBlock(writeAndReadBack(block), FileDataBlock.class);

            // Then
            assertThat(back.getDataType(), is(type));
            assertThat(back.getFileType(), is(block.getFileType()));
        }
    }

    /**
     * Given: {@code EXPECTED_FIXED} のファイルブロック。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : {@code EXPECTED_FIXED} のまま復元される（{@code EXPECTED_VARIABLE} と混ざらない）。
     *
     * <p>担保する観点: {@code EXPECTED_FIXED}（往復）。同じ {@code expected_files} キーを共有する
     * {@code EXPECTED_VARIABLE} と区別されるのは {@code type: "fixed"} による。</p>
     */
    @Test
    public void restoresExpectedFixedDataTypeThroughRealReader() {
        // Given
        FileDataBlock block = new FileDataBlock(DataType.EXPECTED_FIXED, "", "e.dat", map(),
                Collections.singletonList(record()));

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
     * <p>担保する観点: {@code SETUP_VARIABLE}（往復）。</p>
     */
    @Test
    public void restoresSetupVariableDataTypeThroughRealReader() {
        // Given
        FileDataBlock block = new FileDataBlock(DataType.SETUP_VARIABLE, "", "s.csv", map(),
                Collections.singletonList(variableRecord()));

        // When
        FileDataBlock back = YamlFixture.onlyBlock(writeAndReadBack(block), FileDataBlock.class);

        // Then
        assertThat(back.getDataType(), is(DataType.SETUP_VARIABLE));
        assertThat(back.getFileType(), is(FileDataBlock.FileType.VARIABLE));
        assertThat(back.getIdentifier(), is("s.csv"));
    }

    // ------------------------------------------------------------------ 送信同期 3 種のセクションキー

    /**
     * 送信同期のメッセージブロックを 1 件組み立てる（4 種で {@code DataType} 以外は同一）。
     *
     * @param type データ種別
     * @return ブロック
     */
    private static MessageDataBlock sendSync(DataType type) {
        return new MessageDataBlock(type, "case1", "MSG1", map(), map(),
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
     * 担保する観点: データタイプ {@code EXPECTED_REQUEST_BODY_MESSAGES}。<b>既存テストで担保済みと
     * 見ていたが誤りだった</b>（下記）。
     * </p>
     *
     * <p>
     * 既存の {@code YamlFormatWriterTest#serializeSendSync_allFourSectionKeys} は送信同期 4 種を
     * <b>まとめて 1 つの出力に</b>直列化し、4 つのキー文字列が「どこかに現れる」ことを
     * {@code assertTrue(yaml.contains(...))} で見ているだけで、{@code DataType} → セクションキーの対応を
     * 1 つも固定していない。4 種の写像を入れ替えても 4 キーはすべて現れるため通ってしまう
     * （変異を入れて実測）。担保があったのは、単独ブロックの出力全文を完全一致で見ている
     * {@code #serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField} が通す
     * {@code EXPECTED_REQUEST_HEADER_MESSAGES} だけである。
     * <b>中間モデル → Excel の書き出しのテストにも、まったく同じ形の欠陥があった</b>。
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
     * <p>担保する観点: データタイプ {@code RESPONSE_HEADER_MESSAGES}（経緯は {@code #writesExpectedRequestBodyMessagesUnderItsOwnSectionKey}）。</p>
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
     * <p>担保する観点: データタイプ {@code RESPONSE_BODY_MESSAGES}（経緯は {@code #writesExpectedRequestBodyMessagesUnderItsOwnSectionKey}）。</p>
     */
    @Test
    public void writesResponseBodyMessagesUnderItsOwnSectionKey() {
        assertThat(serialize(sendSync(DataType.RESPONSE_BODY_MESSAGES)),
                is(sendSyncYaml("response_body_messages")));
    }

    // ------------------------------------------------------------------ sections の件数

    /**
     * Given: セクションを 1 件も持たないコンテナ。
     * When : {@code write}。
     * Then : 例外にならず、<b>ファイルも出力先ディレクトリも作られない</b>。
     *
     * <p>
     * 担保する観点: {@code sections} 空。{@link YamlFormatWriter#write} は
     * {@code container.getSections()} をループするだけで、ディレクトリの作成もループの中にあるためである。
     * 中間モデル → Excel は同じ入力から<b>シート 0 枚のブックを書き出す</b>ため非対称である。
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
     * 担保する観点: {@code sections} 複数（コンテナ内セクション数 複数）。
     * YAML の読み取りは 1 リソース＝1 セクションを返す API であるため到達不能だが、
     * YAML の書き出しは {@code write} がセクションをループするため到達できる。
     * </p>
     *
     * <p>
     * セクションごとにテーブル識別子を変えてあるため、ファイル名とセクションの対応が入れ替われば落ちる。
     * 期待する中身は<b>リテラルで置いている</b>（{@code writer.serialize(...)} と突き合わせると
     * 実装の出力同士を比べることになり、直列化そのものが壊れても気づけないためである）。
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
     * Given: セクションを 1 件だけ持つコンテナ（コンテナ名とセクション名は別にしてある）。
     * When : {@code write}。
     * Then : 出力先に書かれるファイルは<b>1 つだけ</b>で、名前は {@code <セクション名>.yaml}、
     *        中身はそのセクションを直列化したものになる。
     *
     * <p>
     * 担保する観点: コンテナ内セクション数 1 件。
     * <b>件数は出力側で固定する</b> —— {@code out.list().length} が出力先ディレクトリの実ファイル数を
     * 返すため、2 つ目が書き出されれば落ちる。{@code exists()} では件数を固定できない。
     * </p>
     *
     * <p>
     * 期待する中身は<b>リテラルで置いている</b>（{@code writer.serialize(...)} と突き合わせると
     * 実装の出力同士を比べることになり、直列化そのものが壊れても気づけない）。
     * コンテナ名を {@code "book"}、セクション名を {@code "solo"} と別にしてあるので、
     * ファイル名がコンテナ名から作られるようになれば落ちる。
     * </p>
     */
    @Test
    public void writesOneYamlFileWhenContainerHasSingleSection() throws IOException {
        // Given
        TestDataSection solo = section("solo", table("S"));
        File out = folder.getRoot();

        // When
        writer.write(new TestDataContainer("book", Collections.singletonList(solo)),
                out.getAbsolutePath());

        // Then
        assertThat("書かれたファイル数", out.list().length, is(1));
        assertThat(read(out, solo), is(oneRowTableYaml("S")));
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
                + "      - \"C\": \"1\"\n";
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

    // ------------------------------------------------------------------ FileDataBlock.records 空

    /**
     * Given: レコードレイアウトを 1 件も持たないファイルブロック（ディレクティブは 2 件）。
     * When : {@code serialize}。
     * Then : {@code records: []}（空配列）が書かれる。ディレクティブは記述順のまま、値はダブルクォート付きで出る。
     *
     * <p>
     * 記法は0バイトの空ファイルは、レコード定義を持たないファイルデータブロックとして表現する
     * 、「{@code setup_files}・{@code expected_files} の各エントリには
     * {@code path}・{@code type}・{@code records} の3キーが必須であり、いずれかを省略するとエラーになる。
     * 0バイトの空ファイルを表現するには、{@code records:} に空配列 {@code []} を記載する」
     * と定めている。
     * </p>
     *
     * <p>
     * 担保する観点: {@code FileDataBlock.records} 空。
     * ディレクティブの並び（{@code text-encoding} → {@code file-type}）は辞書順の逆である。
     * </p>
     */
    @Test
    public void writesEmptyRecordsListForFileBlockWithoutRecords() {
        // Given
        FileDataBlock block = new FileDataBlock(DataType.SETUP_FIXED, "", "n.dat",
                map("text-encoding", "UTF-8", "file-type", "Fixed"),
                Collections.<RecordLayout>emptyList());

        // When / Then
        assertThat(serialize(block), is(""
                + "setup_files:\n"
                + "  - path: \"n.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    directives:\n"
                + "      \"text-encoding\": \"UTF-8\"\n"
                + "      \"file-type\": \"Fixed\"\n"
                + "    records: []\n"));
    }

    /**
     * Given: 真偽値に見える値と日付に見える値を持つ {@code fw_header} のメッセージブロック。
     * When : {@code serialize}。
     * Then : どちらの値もダブルクォート付きで書かれる。
     *
     * <p>
     * 担保する観点: 値 {@code "true"}／{@code "2026-08-07"} の記法を
     * <b>{@code YamlFormatWriter#emitMap} 経路で</b>。同メソッドは {@code directives} と
     * {@code fw_header} の 2 箇所から呼ばれる唯一の出力経路であり、
     * {@code YamlFormatWriterScalarTest} が値の表現を測っている {@code setup_tables} の {@code rows} 経路
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
     * </p>
     */
    @Test
    public void quotesBooleanAndDateLookingValuesInFwHeader() {
        // Given
        // 本文レコードは電文の契約上 1 件以上が必要なため最小の 1 件を置く（検証対象は fw_header の値のみ）
        MessageDataBlock block = new MessageDataBlock(DataType.MESSAGE, "", "RM01",
                map("text-encoding", "UTF-8"),
                map("resendFlag", "true", "dateSent", "2026-08-07"),
                Collections.singletonList(new RecordLayout(null,
                        Collections.singletonList(new FieldDef("f", "半角英字", "1")),
                        Collections.singletonList(Collections.singletonList("v")))));

        // When / Then
        assertThat(serialize(block), is(""
                + "messages:\n"
                + "  - id: \"RM01\"\n"
                + "    directives:\n"
                + "      \"text-encoding\": \"UTF-8\"\n"
                + "    fw_header:\n"
                + "      \"resendFlag\": \"true\"\n"
                + "      \"dateSent\": \"2026-08-07\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"v\"]\n"));
    }

    // ------------------------------------------------------------------ キーのクォート

    /**
     * クォートを要するキーと、期待する出力上の表記。
     *
     * <p>
     * 1 列目は生のキー（{@code x} ＋ 対象文字）、2 列目は {@code directives:} 配下に現れるはずの表記である。
     * 対象文字は YAML の特殊文字 {@code "'#:,[]{}&*!|>%@`?}（<b>18 文字</b>）の全部と、
     * 制御文字（{@code < 0x20}）2 種である。
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
     * Given: YAML の特殊文字 18 文字と制御文字 2 種を
     *        1 文字ずつ含む {@code directives} のキー。
     * When : {@code serialize}。
     * Then : 20 通りすべてで、そのキーがダブルクォートで囲まれた出力全文になる。
     *
     * <p>
     * 担保する観点: 利用者が書くキーのクォート（{@code YamlFormatWriter#userKey}。値の表現ではなく<b>キー</b>）。
     * 利用者が書くキーは常に囲むので、これらの文字を含むキーも囲まれる。
     * </p>
     *
     * <p>
     * 囲まずに書くと実害は大きい —— たとえばカラム名 {@code #x} の行が {@code - #x: "v"} となって
     * <b>行全体が YAML コメント化し、データ行が黙って消える</b>。
     * </p>
     *
     * <p>
     * <b>本メソッドは「1 ケース 1 {@code @Test}」の規約から意図して逸脱している。</b>
     * 規約は {@code XlsFormatWriterInvalidOutputTest} のシート名の検査の節と
     * {@code XlsFormatWriterCellTypeTest} のコメントにあり、その理由は
     * 「ループで束ねると最初の 1 文字が落ちた時点で残りが実行されず、どの文字で挙動が違うのかが
     * 分からなくなるため」である。ここでループにしてよいと判断した根拠は 2 つある。
     * </p>
     *
     * <ol>
     *   <li><b>20 ケースは 1 つの振る舞いである。</b>いずれも
     *       {@code YamlFormatWriter#userKey} を通り、一律に {@code q(k)} でクォートされる。Xls 側の禁止文字 7 件のように
     *       <b>文字ごとに違うメッセージ・違う挙動</b>（例外になる／切り詰めで黙って書けてしまう）を
     *       持つわけではない。</li>
     *   <li><b>失敗を集約したので規約の理由が成立しない。</b>ループ内では判定するだけで、
     *       アサートはループ後に 1 回だけ行う。したがって 1 件目が落ちても残り 19 ケースは実行され、
     *       失敗メッセージには落ちた全ケースが出る。</li>
     * </ol>
     */
    @Test
    public void quotesDirectiveKeyContainingAnyYamlSpecialOrControlCharacter() {
        List<String> failures = new ArrayList<>();
        for (String[] pair : KEYS_REQUIRING_QUOTES) {
            // Given
            FileDataBlock block = new FileDataBlock(DataType.SETUP_FIXED, "", "k.dat", map(pair[0], "v"),
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

    // ------------------------------------------------------------------ 書けるが読み戻せない形

    /**
     * Given: レコードレイアウトを 1 件も持たないファイルブロック。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : スキーマ違反にならず、レコード 0 件のファイルブロックとして戻る。
     *
     * <p>
     * 本体スキーマの {@code $defs.file_data.required} は {@code records} を要求するが
     * {@code minItems} は 0 であり、<b>空配列なら通る</b>（キーごと省略すると通らない）。
     * 記法仕様も 0 バイトの空ファイルを {@code records: []} で表すと定めている
     * 。
     * </p>
     */
    @Test
    public void readsBackFileBlockWithEmptyRecords() {
        // Given / When
        FileDataBlock back = YamlFixture.onlyBlock(writeAndReadBack(
                new FileDataBlock(DataType.SETUP_FIXED, "", "n.dat",
                        map(), Collections.<RecordLayout>emptyList())), FileDataBlock.class);

        // Then
        assertThat(back.getIdentifier(), is("n.dat"));
        assertThat(back.getFileType(), is(FileDataBlock.FileType.FIXED));
        assertThat(back.getRecords().size(), is(0));
    }

    // ------------------------------------------------------------------ 往復で値が変わる形（既知の課題）

    /**
     * Given: 小文字のテーブル名とカラム名（カラムは辞書順の逆に並べてある）。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : テーブル名・カラム名とも<b>大文字化されて</b>戻る。値と並びはそのまま。
     *
     * <p>
     * 担保する観点: テーブル系のカラム名の大文字化が、YAML の書き出し → 読み戻しにどう現れるかの固定。
     * 大文字化するのは本体の器（{@code TableData}）であり converter ではない（大文字化を止める権限が
     * converter に無いため修正しない）。大小だけが違うカラム名が同居すると値が消えるところまでが問題の全体で、
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
     * 担保する観点: ディレクティブ値の {@code trim()} が、YAML の書き出し → 読み戻しにどう現れるかの固定。
     * 原因は読み取り側（本体 {@code DataFile#setDirective}）でディレクティブ値が {@code trim()} されることであり、
     * タブが空文字になるためである。
     * すなわち<b>リテラルのタブを区切り文字に持つ可変長ファイルは YAML の書き出し → 読み取りの往復ができない</b>。
     * </p>
     */
    @Test
    public void failsToReadBackLiteralTabFieldSeparator() {
        // Given
        FileDataBlock block = new FileDataBlock(DataType.SETUP_VARIABLE, "", "s.csv", map("field-separator", "\t"),
                Collections.singletonList(variableRecord()));
        assertThat("書き出しはタブをエスケープして忠実に書く",
                serialize(block), containsString("\"field-separator\": \"\\t\""));

        // When
        String base = write(block);

        // Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new YamlFormatReader().read(base, "td"));
        assertThat(thrown.getMessage(), containsString("field-separator must be one character"));
    }
}
