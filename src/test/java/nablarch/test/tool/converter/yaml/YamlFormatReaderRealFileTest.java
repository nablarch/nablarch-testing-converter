package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.tool.converter.model.FieldDef;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.MessageDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 辺②（YAML→中間モデル）を実 {@code .yaml} で駆動し、軸A（データタイプ）・軸B（ブロック実装）・
 * 軸C（中間モデルのフィールド）・軸E（多重度）の空欄を埋めるテスト。
 *
 * <p>
 * {@code YamlFormatReaderTest} 20 件は {@code loadRawMap} を in-memory {@code Map} へ差し替える経路であり、
 * YAML テキストのパースとスキーマ検証を通らない。本クラスは {@link YamlFixture} が書き出した
 * 実 {@code .yaml} を入力とし、本番配線の {@link YamlFormatReader} を通す。
 * </p>
 *
 * <p>
 * 空コレクション（{@code columnNames} / {@code rows} / {@code records}）は
 * <b>スキーマが空配列を許す形でのみ到達できる</b>。到達できないもの
 * （{@code FileDataBlock.directives} 空・{@code MessageDataBlock.directives} 空・
 * {@code RecordLayout.fields} 空・{@code FieldDef.type} 省略）は、その根拠を本クラスまたは
 * {@link YamlFormatReaderInvalidInputTest} が実行可能な形で示す。
 * </p>
 *
 * <p>
 * 各テストの Javadoc には、そのテストが担保する軸要素の ID
 * （{@code .rn/ntf-test-data-converter/coverage/inventory.md} の A-01〜A-14／B-1〜B-4／C-01〜C-21／E-1〜E-4）を記す。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>実装（src/main）は変更していない。
 * 妥当でないと判断した挙動は {@code coverage/issues.md} に課題として記録した（{@code YML-02} ／ {@code YML-03}）。
 * </p>
 *
 * @author kiyobot
 */
public class YamlFormatReaderRealFileTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** 実 {@link YamlFormatReader} を通すため、{@link YamlLoader} の LRU キャッシュをテスト間で残さない。 */
    @After
    public void clearLoaderCache() {
        YamlLoader.clearCacheForTest();
    }

    /**
     * フィクスチャ {@code .yaml} の出力先ディレクトリ。読み書きとも本メソッドだけを使う。
     *
     * @return ディレクトリ
     */
    private Path dir() {
        return folder.getRoot().toPath();
    }

    // ------------------------------------------------------------------ 軸A

    /**
     * Given: 既知セクション 11 キーに 13 エントリを書いた 1 ファイル
     *        （{@code setup_files} / {@code expected_files} は固定長・可変長を 1 件ずつ持つ）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code DataType.DEFAULT} を除く 13 種すべてが、YAML の記述順どおりに生成される。
     *
     * <p>
     * {@code DEFAULT} は {@code YamlFormatReader#addBlocksForSection} が既知セクションキーのみを
     * 分岐に持ち、いずれの分岐も {@code DEFAULT} を渡さないため辺②では到達不能である。
     * </p>
     *
     * <p>担保する軸要素: A-02〜A-14（{@code DEFAULT} を除く 13 種）／E-1（複数＝13）。</p>
     */
    @Test
    public void readsAllThirteenDataTypesFromRealYaml() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), allSectionsYaml());

        // Then
        List<DataType> actual = new ArrayList<>();
        for (TestDataBlock block : YamlFixture.blocks(container)) {
            actual.add(block.getDataType());
        }
        assertThat(actual, is(Arrays.asList(
                DataType.SETUP_TABLE_DATA,
                DataType.EXPECTED_TABLE_DATA,
                DataType.EXPECTED_COMPLETED,
                DataType.LIST_MAP,
                DataType.SETUP_FIXED,
                DataType.SETUP_VARIABLE,
                DataType.EXPECTED_FIXED,
                DataType.EXPECTED_VARIABLE,
                DataType.MESSAGE,
                DataType.EXPECTED_REQUEST_HEADER_MESSAGES,
                DataType.EXPECTED_REQUEST_BODY_MESSAGES,
                DataType.RESPONSE_HEADER_MESSAGES,
                DataType.RESPONSE_BODY_MESSAGES)));
    }

    // ------------------------------------------------------------------ 軸B

    /**
     * Given: テーブル・LIST_MAP・ファイル・メッセージを 1 件ずつ書いた 1 ファイル。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code TestDataBlock} の具象 4 種がそれぞれ生成され、識別子も原文どおりになる。
     *        テーブルと LIST_MAP はいずれも行 1 件を持つ。
     *
     * <p>担保する軸要素: B-1〜B-4／C-07（identifier）／E-1（複数＝4）／<b>E-2(1 件)</b>。</p>
     */
    @Test
    public void readsFourBlockImplementationsFromOneRealYaml() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - C: \"1\"\n"
                + "list_maps:\n"
                + "  - id: \"lm\"\n"
                + "    rows:\n"
                + "      - K: \"v\"\n"
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n"
                + "messages:\n"
                + "  - id: \"RM01\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"m1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"b\"]\n");

        // Then
        List<TestDataBlock> blocks = YamlFixture.blocks(container);
        assertThat(blocks.size(), is(4));
        assertThat(blocks.get(0), is(instanceOf(TableDataBlock.class)));
        assertThat(blocks.get(0).getIdentifier(), is("T"));
        assertThat("E-2 ブロック内行数（1 件）", ((TableDataBlock) blocks.get(0)).getRows().size(), is(1));
        assertThat(blocks.get(1), is(instanceOf(ListMapBlock.class)));
        assertThat(blocks.get(1).getIdentifier(), is("lm"));
        assertThat("E-2 ブロック内行数（1 件。LIST_MAP 経路）",
                ((ListMapBlock) blocks.get(1)).getRows().size(), is(1));
        assertThat(blocks.get(2), is(instanceOf(FileDataBlock.class)));
        assertThat(blocks.get(2).getIdentifier(), is("f.dat"));
        assertThat(blocks.get(3), is(instanceOf(MessageDataBlock.class)));
        assertThat(blocks.get(3).getIdentifier(), is("RM01"));
    }

    // ------------------------------------------------------------------ 軸C・軸E（空コレクション）

    /**
     * Given: {@code rows} を空配列にしたテーブルエントリ。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : ブロックは生成され、{@code columnNames} も {@code rows} も空になる。
     *
     * <p>
     * 軸C の C-08（{@code columnNames} 空）・C-09（{@code rows} 空）と軸E の E-2(0 件) を同じ入力で通す。
     * カラム名は行から導出されるため、行が無ければカラム名も 0 件になる。
     * </p>
     *
     * <p>担保する軸要素: A-02／B-1／C-07／C-08(空)／C-09(空)／E-1(1 件)／E-2(0 件)。</p>
     */
    @Test
    public void readsEmptyColumnNamesAndRowsFromTableWithoutRows() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(),
                "setup_tables:\n  - table: \"T\"\n    rows: []\n");

        // Then
        TableDataBlock block = YamlFixture.onlyBlock(container, TableDataBlock.class);
        assertThat(block.getDataType(), is(DataType.SETUP_TABLE_DATA));
        assertThat(block.getIdentifier(), is("T"));
        assertTrue("columnNames が空であること", block.getColumnNames().isEmpty());
        assertTrue("rows が空であること", block.getRows().isEmpty());
    }

    /**
     * Given: {@code rows} を空配列にした LIST_MAP エントリ。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : ブロックは生成され、{@code columnNames} も {@code rows} も空になる（テーブル経路と同じ）。
     *
     * <p>担保する軸要素: A-05／B-2／C-07／C-08(空)／C-09(空)／E-2(0 件)。</p>
     */
    @Test
    public void readsEmptyColumnNamesAndRowsFromListMapWithoutRows() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(),
                "list_maps:\n  - id: \"lm\"\n    rows: []\n");

        // Then
        ListMapBlock block = YamlFixture.onlyBlock(container, ListMapBlock.class);
        assertThat(block.getDataType(), is(DataType.LIST_MAP));
        assertThat(block.getIdentifier(), is("lm"));
        assertTrue("columnNames が空であること", block.getColumnNames().isEmpty());
        assertTrue("rows が空であること", block.getRows().isEmpty());
    }

    /**
     * Given: {@code records} を空配列にした固定長ファイルエントリ。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : ブロックは生成され、{@code records} が空になる。
     *
     * <p>
     * 軸C の C-12（{@code FileDataBlock.records} 空）と軸E の E-3(0 件) を同じ入力で通す。
     * スキーマ {@code $defs.file_data.properties.records.minItems} が 0 であるため到達できる
     * （{@code message_data} / {@code expected_request_message_data} は {@code minItems: 1} のため
     * メッセージ系では到達できない）。
     * </p>
     *
     * <p>担保する軸要素: A-06／B-3／C-10(FIXED)／C-12(空)／E-3(0 件)。</p>
     */
    @Test
    public void readsEmptyRecordsFromFixedFileWithoutRecords() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(),
                "setup_files:\n  - path: \"f.dat\"\n    type: \"fixed\"\n    records: []\n");

        // Then
        FileDataBlock block = YamlFixture.onlyBlock(container, FileDataBlock.class);
        assertThat(block.getDataType(), is(DataType.SETUP_FIXED));
        assertThat(block.getFileType(), is(FileDataBlock.FileType.FIXED));
        assertTrue("records が空であること", block.getRecords().isEmpty());
    }

    /**
     * Given: フィールド定義は持つが {@code rows} を空配列にしたレコード断片。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : レコードレイアウトは生成され、{@code rows} が空になる（{@code fields} は 1 件のまま）。
     *
     * <p>
     * 軸C の C-18（{@code RecordLayout.rows} 空）を通す。あわせて {@code record_type} を書かない場合に
     * {@code null} になること（C-16 省略）も実ファイル経路で確かめる。
     * </p>
     *
     * <p>担保する軸要素: A-06／B-3／C-16(省略＝null)／C-18(空)／C-19／C-21／E-3(1 件)。</p>
     */
    @Test
    public void readsEmptyRowsFromRecordLayoutWithoutRows() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows: []\n");

        // Then
        FileDataBlock block = YamlFixture.onlyBlock(container, FileDataBlock.class);
        assertThat(block.getRecords().size(), is(1));
        RecordLayout record = block.getRecords().get(0);
        assertThat(record.getRecordType(), is(nullValue()));
        assertThat(record.getFields().size(), is(1));
        assertTrue("RecordLayout.rows が空であること", record.getRows().isEmpty());
    }

    /**
     * Given: {@code record_type} に小文字の {@code "default"} を書いたレコード断片。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code RecordLayout.recordType} は {@code null} になる
     *        （作成者が書いた {@code "default"} は中間モデルに残らない）。
     *
     * <p>
     * {@code YamlFormatReader#normalizeRecordType} は {@code "Default"} と {@code "default"} の
     * 2 つを {@code null} へ正規化する。{@code "Default"} 側は in-memory 経路の
     * {@code YamlFormatReaderTest#readFile_recordTypeDefault_normalizedToNull} が通しているが、
     * <b>小文字側は #24 の JaCoCo 実測時点で未到達だった</b>。本テストはそれを実 {@code .yaml} で閉じる。
     * スキーマ {@code $defs.record_fragment.properties.record_type} に {@code enum} は無く、
     * その description も「可読性のために任意の名前を記述してよい」と書いているため、
     * {@code "default"} は<b>スキーマを通る仕様内の入力</b>である。
     * </p>
     *
     * <p>担保する軸要素: A-06／B-3／C-16（{@code "default"}→null の正規化）。</p>
     */
    @Test
    public void normalizesLowercaseDefaultRecordTypeToNull() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - record_type: \"default\"\n"
                + "        fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");

        // Then
        FileDataBlock block = YamlFixture.onlyBlock(container, FileDataBlock.class);
        assertThat(block.getRecords().size(), is(1));
        RecordLayout record = block.getRecords().get(0);
        assertThat("小文字の \"default\" も null へ正規化される", record.getRecordType(), is(nullValue()));
        assertThat(record.getFields().get(0).getName(), is("f1"));
        assertThat(record.getRows(), is(Arrays.asList(Arrays.asList("a"))));
    }

    // ------------------------------------------------------------------ 軸C（directives）

    /**
     * Given: {@code directives} を書かない固定長ファイルエントリ。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code directives} は空にならず、器が注入する {@code file-type} だけを持つ。
     *
     * <p>
     * 軸C の <b>C-11（{@code FileDataBlock.directives} 空）が辺②で到達不能である根拠</b>である。
     * 辺①でも同じ理由（本体 {@code DataFile} が {@code file-type} を必ず持つ）で到達不能と判定しており
     * （{@code coverage/issues.md} XLS-07）、YAML 経路でも同じ器を使うため結果は変わらない。
     * </p>
     *
     * <p>担保する軸要素: A-06／B-3／C-11(空が到達不能である根拠)。</p>
     */
    @Test
    public void readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInFile() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");

        // Then
        FileDataBlock block = YamlFixture.onlyBlock(container, FileDataBlock.class);
        assertThat(block.getDirectives().size(), is(1));
        assertThat(block.getDirectives().get("file-type"), is("Fixed"));
    }

    /**
     * Given: {@code directives} を書かないメッセージエントリ。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code directives} は空にならず、器が注入する {@code file-type} だけを持つ。
     *
     * <p>
     * 軸C の <b>C-13（{@code MessageDataBlock.directives} 空）が辺②で到達不能である根拠</b>である。
     * ただし {@code MessageDataBlock} は {@code addMessageBlocks}（本テスト）と {@code addSendSyncBlocks}
     * （{@link #readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInSendSync}）の 2 か所で生成される。
     * 根拠は両方で示す。
     * </p>
     *
     * <p>担保する軸要素: A-10／B-4／C-13(空が到達不能である根拠。受信メッセージ経路)。</p>
     */
    @Test
    public void readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInMessage() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "messages:\n"
                + "  - id: \"RM01\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"m1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");

        // Then
        MessageDataBlock block = YamlFixture.onlyBlock(container, MessageDataBlock.class);
        assertThat(block.getDirectives().size(), is(1));
        assertThat(block.getDirectives().get("file-type"), is("Fixed"));
    }

    /**
     * Given: {@code directives} に {@code text-encoding} を書いたメッセージエントリ。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 書いたディレクティブが器の注入する {@code file-type} とともに中間モデルへ入る。
     *
     * <p>
     * 軸C の C-13（{@code MessageDataBlock.directives} 値あり）を通す。
     * </p>
     *
     * <p>担保する軸要素: A-10／B-4／C-13(値あり)。</p>
     */
    @Test
    public void readsMessageDirectivesFromRealYaml() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "messages:\n"
                + "  - id: \"RM01\"\n"
                + "    directives:\n"
                + "      text-encoding: \"Windows-31J\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"m1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");

        // Then
        MessageDataBlock block = YamlFixture.onlyBlock(container, MessageDataBlock.class);
        assertThat(block.getDirectives().get("text-encoding"), is("Windows-31J"));
        assertThat(block.getDirectives().get("file-type"), is("Fixed"));
    }

    // ------------------------------------------------------------------ 軸E

    /**
     * Given: ブロックを 1 件も持たない（既知セクションを書かない）YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : コンテナ名・セクション名はともにリソース名になり、セクションは 1 件・ブロックは 0 件になる。
     *
     * <p>
     * 軸C の C-01 / C-02(1 件) / C-03 / C-04(空) と軸E の E-1(0 件) / E-4(1 件) を実ファイル経路で通す。
     * E-4(複数) は {@code YamlFormatReader#read} が {@code Collections.singletonList(section)} を返す
     * 1 リソース単位 API のため到達不能である。
     * </p>
     *
     * <p>
     * <b>入力は {@code setup_tables: []}（既知セクションを空配列で書く）である。</b>
     * 空ファイルやコメントだけのファイルにすると {@code YamlLoader#load} が
     * {@code loadFromInputStream} の {@code null} を受けて空 Map を早期 return し、
     * <b>{@code JSON_SCHEMA.validate} に到達しない</b>。本テストはスキーマ検証を通る入力で
     * 「ブロック 0 件」を担保するために、スキーマ上有効な空配列を用いる。
     * 空ファイルそのもの（＝検証を迂回する分岐）は
     * {@link YamlFormatReaderInvalidInputTest#readsEmptyFileAsContainerWithoutBlocks}（軸F の F2-05）が担保する。
     * </p>
     *
     * <p>担保する軸要素: C-01／C-02(1 件)／C-03／C-04(空)／E-1(0 件)／E-4(1 件)。</p>
     */
    @Test
    public void namesContainerAndSectionByResourceNameWithoutBlocks() {
        // Given / When: 既知セクションを空配列で書いた YAML（スキーマ検証を通る）
        TestDataContainer container = YamlFixture.read(dir(), "setup_tables: []\n");

        // Then
        assertThat(container.getName(), is(YamlFixture.RESOURCE));
        assertThat(container.getSections().size(), is(1));
        assertThat(container.getSections().get(0).getName(), is(YamlFixture.RESOURCE));
        assertTrue("ブロックが 0 件であること", YamlFixture.blocks(container).isEmpty());
    }

    /**
     * Given: 1 セクションに 3 ブロック・1 ブロックに 2 行・1 ファイルに 2 レコードレイアウトを持つ YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 多重度がそのまま中間モデルへ入る。
     *
     * <p>
     * 軸E の E-1(複数) / E-2(複数) / E-3(複数) を実ファイル経路で通す
     * （1 件のケースは他のテストが通している）。
     * </p>
     *
     * <p>担保する軸要素: A-02／A-06／B-1／B-3／C-16(値あり)／E-1(複数)／E-2(複数)／E-3(複数)。</p>
     */
    @Test
    public void readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_tables:\n"
                + "  - table: \"T1\"\n"
                + "    rows:\n"
                + "      - C: \"1\"\n"
                + "      - C: \"2\"\n"
                + "  - table: \"T2\"\n"
                + "    rows:\n"
                + "      - C: \"3\"\n"
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - record_type: \"head\"\n"
                + "        fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n"
                + "      - record_type: \"data\"\n"
                + "        fields:\n"
                + "          - {name: \"f2\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"b\"]\n"
                + "          - [\"c\"]\n");

        // Then
        List<TestDataBlock> blocks = YamlFixture.blocks(container);
        assertThat("E-1 セクション内ブロック数（複数）", blocks.size(), is(3));
        assertThat("E-2 ブロック内行数（複数）", ((TableDataBlock) blocks.get(0)).getRows().size(), is(2));
        FileDataBlock file = (FileDataBlock) blocks.get(2);
        assertThat("E-3 ファイル内レコードレイアウト数（複数）", file.getRecords().size(), is(2));
        assertThat(file.getRecords().get(0).getRecordType(), is("head"));
        assertThat(file.getRecords().get(1).getRows().size(), is(2));
    }

    // ------------------------------------------------------------------ 軸C（FW 制御ヘッダ・フィールド長）

    /**
     * Given: {@code fw_header:} に 2 キーを書いた {@code messages} エントリ。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 書いたキーと値がそのまま {@code MessageDataBlock.fwHeaderFields} へ記述順で入る。
     *
     * <p>
     * 軸C の C-14（{@code MessageDataBlock.fwHeaderFields} 値あり）を<b>実ファイル経路で</b>通す。
     * これまでは in-memory 経路（{@code YamlFormatReaderTest#readMessage_mapsRawFwHeaderAndExcludesFwHeaderRecord}）
     * の担保しか無かった。スキーマ上 {@code fw_header} は {@code messages} 専用であり
     * （{@code $defs.message_data.properties.fw_header} の description）、値の型は
     * {@code $defs.fw_header.additionalProperties.type} ＝ {@code string} である。
     * </p>
     *
     * <p>担保する軸要素: A-10／B-4／C-14(値あり)。</p>
     */
    @Test
    public void readsFwHeaderFieldsFromRealYaml() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "messages:\n"
                + "  - id: \"RM01\"\n"
                + "    fw_header:\n"
                + "      requestId: \"RM01\"\n"
                + "      userId: \"u1\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"b1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");

        // Then
        MessageDataBlock block = YamlFixture.onlyBlock(container, MessageDataBlock.class);
        assertThat(new ArrayList<>(block.getFwHeaderFields().keySet()),
                is(Arrays.asList("requestId", "userId")));
        assertThat(block.getFwHeaderFields().get("requestId"), is("RM01"));
        assertThat(block.getFwHeaderFields().get("userId"), is("u1"));
        // 本文レコードは fw_header の影響を受けない
        assertThat(block.getRecords().size(), is(1));
        assertThat(block.getRecords().get(0).getFields().get(0).getName(), is("b1"));
    }

    /**
     * Given: {@code length} を<b>引用符なしの整数</b>（{@code 10}）で書いたフィールド定義。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : スキーマを通り、{@code FieldDef.length} には文字列 {@code "10"} が入る。
     *
     * <p>
     * スキーマ {@code $defs.field_def.properties.length} は
     * {@code anyOf: [{type: integer, minimum: 0}, {type: string, pattern: "^([0-9]+|-)$"}]} であり、
     * description も「integer 記法（10）も文字列記法（"10"）もどちらも有効」と明記している。
     * すなわち<b>これは仕様内の入力である</b>（{@code rows} の値として引用符なし {@code 123} を書くと
     * スキーマ違反になるのとは別の話。{@code coverage/issues.md}「対象としない入力（辺②）」）。
     * 中間モデルで文字列になるのは {@code YamlSection#toStr} が {@code Object#toString()} で
     * 文字列化するためであり、引用符の有無は中間モデルに残らない。
     * </p>
     *
     * <p>担保する軸要素: A-06／B-3／C-21（{@code FieldDef.length} 値あり・integer 記法）。</p>
     */
    @Test
    public void readsIntegerLengthNotationAsString() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: 10}\n"
                + "        rows:\n"
                + "          - [\"abcdefghij\"]\n");

        // Then
        FileDataBlock block = YamlFixture.onlyBlock(container, FileDataBlock.class);
        FieldDef field = block.getRecords().get(0).getFields().get(0);
        assertThat(field.getName(), is("f1"));
        assertThat(field.getType(), is("半角英字"));
        assertThat(field.getLength(), is("10"));
    }

    // ------------------------------------------------------------------ 送信系（YML-02）・FW_HEADER（YML-03）

    /**
     * Given: {@code directives} を書かない送信系（{@code response_header_messages}）エントリ。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : {@code directives} は空にならず、器が注入する {@code file-type} だけを持つ。
     *
     * <p>
     * {@code MessageDataBlock} は {@code YamlFormatReader#addMessageBlocks} と
     * {@code #addSendSyncBlocks} の 2 か所で生成される。
     * {@link #readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInMessage} が前者を通すのに対し、
     * 本テストは<b>後者</b>を通す。したがって軸C の
     * <b>C-13（{@code MessageDataBlock.directives} 空）が辺②で到達不能である根拠</b>は
     * 2 つの生成経路の両方で示されている。
     * </p>
     *
     * <p>担保する軸要素: A-13／B-4／C-13(空が到達不能である根拠。送信系経路)。</p>
     */
    @Test
    public void readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInSendSync() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "response_header_messages:\n"
                + "  - group_id: \"g\"\n"
                + "    id: \"MSG1\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"h1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");

        // Then
        MessageDataBlock block = YamlFixture.onlyBlock(container, MessageDataBlock.class);
        assertThat(block.getDataType(), is(DataType.RESPONSE_HEADER_MESSAGES));
        assertThat(block.getDirectives().size(), is(1));
        assertThat(block.getDirectives().get("file-type"), is("Fixed"));
    }

    /**
     * Given: {@code group_id} を書かない送信系エントリ 1 件と、書いたエントリ 1 件。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>例外にならず</b>、{@code group_id} 無しのエントリだけがブロックごと消える。
     *
     * <p>
     * <b>この入力はスキーマ上の仕様内である。</b>{@code $defs.group_message_data.required} は
     * {@code ["id","records"]} だけで {@code group_id} を要求しない。加えて<b>定義レベルの</b> description
     * （{@code $defs.group_message_data.description}）は「{@code group_id} を省略した場合は経路 B として動作する」、
     * <b>プロパティの</b> description（{@code $defs.group_message_data.properties.group_id.description}）は
     * 「{@code MockMessagingContext} / {@code MockMessagingClient} 経路では参照されないため省略可」と書いている
     * （2 文は別の JSON パスにある。引用元の取り違えを 2026-08-14 のレビュー指摘で訂正した）。
     * それでも {@code YamlFormatReader#addSendSyncBlocks} は
     * {@code rawGroupsInOrder}（{@code group_id} が非 null のエントリのみ列挙）を回すため、
     * {@code group_id} の無いエントリはブロックを生成しない。
     * {@code coverage/issues.md} に <b>YML-02</b> として記録した（{@code src/main} は無変更）。
     * </p>
     *
     * <p>担保する軸要素: A-14／B-4（現状挙動の固定。YML-02 の根拠テスト）。</p>
     */
    @Test
    public void dropsSendSyncEntryWithoutGroupIdFromRealYaml() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "response_body_messages:\n"
                + "  - id: \"DROP\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"d1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"x\"]\n"
                + "  - group_id: \"g\"\n"
                + "    id: \"KEEP\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"k1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"y\"]\n");

        // Then: 例外にならず、group_id 付きの 1 件だけが残る
        MessageDataBlock block = YamlFixture.onlyBlock(container, MessageDataBlock.class);
        assertThat(block.getDataType(), is(DataType.RESPONSE_BODY_MESSAGES));
        assertThat(block.getIdentifier(), is("KEEP"));
        assertThat(block.getGroupId(), is("[g]"));
    }

    /**
     * Given: {@code record_type: "FW_HEADER"} のレコードだけを持ち {@code fw_header:} を書かない
     *        {@code messages} エントリ。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>例外にならず</b>、ブロックは生成されるがレコードも FW 制御ヘッダも 0 件になる
     *        （書いたフィールド定義とデータ行が黙って消える）。
     *
     * <p>
     * <b>この入力はスキーマ上の仕様内である。</b>{@code $defs.record_fragment.properties.record_type} に
     * {@code enum} は無く、その description は
     * 「{@code FW_HEADER} のような予約値はない」「可読性のために任意の名前を記述してよい」と明記し、
     * {@code $defs.message_data.properties.records} の description も
     * 「旧形式の {@code record_type: FW_HEADER} は廃止」と書いている。
     * にもかかわらず本体器（yaml jar の {@code YamlFileBuilder#buildFragmentsInternal} が
     * {@code skipFwHeader} 時に {@code FW_HEADER} を {@code continue} でスキップする）と
     * converter（{@code YamlFormatReader#recordsWithoutFwHeader}）の双方が、この名前のレコードを落とす。
     * {@code coverage/issues.md} に <b>YML-03</b> として記録した（{@code src/main} は無変更）。
     * </p>
     *
     * <p>担保する軸要素: A-10／B-4／C-14(空)／C-15(空)（現状挙動の固定。YML-03 の根拠テスト）。</p>
     */
    @Test
    public void dropsFwHeaderNamedRecordFromRealYaml() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "messages:\n"
                + "  - id: \"RM01\"\n"
                + "    records:\n"
                + "      - record_type: \"FW_HEADER\"\n"
                + "        fields:\n"
                + "          - {name: \"requestId\", type: \"半角英字\", length: \"4\"}\n"
                + "        rows:\n"
                + "          - [\"RM01\"]\n");

        // Then: 例外にならず、レコードも FW 制御ヘッダも残らない
        MessageDataBlock block = YamlFixture.onlyBlock(container, MessageDataBlock.class);
        assertThat(block.getDataType(), is(DataType.MESSAGE));
        assertThat(block.getIdentifier(), is("RM01"));
        assertTrue("records が空であること", block.getRecords().isEmpty());
        assertTrue("fwHeaderFields が空であること", block.getFwHeaderFields().isEmpty());
    }

    // ------------------------------------------------------------------ グループの並び替え（YML-09）

    /**
     * Given: 同じセクション配列の中で {@code group_id} を {@code g1} → {@code g2} → {@code g1} と
     *        交互に書いたエントリ列（テーブル系・ファイル系・送信系の 3 セクション）。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : <b>例外にも警告にもならず</b>、ブロックはグループの初出順にまとめ直されて
     *        {@code T1, T3, T2} ／ {@code a.dat, c.dat, b.dat} ／ {@code M1, M3, M2} の順になる
     *        （原文の記述順ではない）。値そのものは失われない。
     *
     * <p>
     * <b>この入力はスキーマ上の仕様内である。</b>{@code $defs.table_data} ／ {@code $defs.file_data} ／
     * {@code $defs.group_message_data} のいずれも、同じ {@code group_id} のエントリが配列内で
     * 連続することを要求していない。
     * </p>
     *
     * <p>
     * 原因は、テーブル系・ファイル系は {@code YamlFormatReader#formattedGroupsInOrder} が、
     * 送信系は {@code #rawGroupsInOrder} が、それぞれグループを初出順に重複排除し、
     * {@code #addTableBlocks} ／ {@code #addFileBlocks} ／ {@code #addSendSyncBlocks} が
     * <b>グループ単位で</b>ブロックを作ることである。
     * {@code coverage/issues.md} に <b>YML-09</b> として記録した（{@code src/main} は無変更）。
     * </p>
     *
     * <p>担保する軸要素: なし（軸A〜F のどの要素にも新しい担保を与えない。YML-09 の根拠テスト）。</p>
     */
    @Test
    public void reordersBlocksByFirstAppearanceOfGroupIdFromRealYaml() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_tables:\n"
                + "  - group_id: \"g1\"\n"
                + "    table: \"T1\"\n"
                + "    rows:\n"
                + "      - C: \"1\"\n"
                + "  - group_id: \"g2\"\n"
                + "    table: \"T2\"\n"
                + "    rows:\n"
                + "      - C: \"2\"\n"
                + "  - group_id: \"g1\"\n"
                + "    table: \"T3\"\n"
                + "    rows:\n"
                + "      - C: \"3\"\n"
                + "setup_files:\n"
                + "  - group_id: \"g1\"\n"
                + "    path: \"a.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n"
                + "  - group_id: \"g2\"\n"
                + "    path: \"b.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"b\"]\n"
                + "  - group_id: \"g1\"\n"
                + "    path: \"c.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"c\"]\n"
                + "response_body_messages:\n"
                + "  - group_id: \"g1\"\n"
                + "    id: \"M1\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n"
                + "  - group_id: \"g2\"\n"
                + "    id: \"M2\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"b\"]\n"
                + "  - group_id: \"g1\"\n"
                + "    id: \"M3\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"c\"]\n");

        // Then: グループの初出順にまとめ直され、原文の記述順（T1, T2, T3 ほか）ではなくなる
        List<String> identifiers = new ArrayList<>();
        List<String> groupIds = new ArrayList<>();
        for (TestDataBlock block : YamlFixture.blocks(container)) {
            identifiers.add(block.getIdentifier());
            groupIds.add(block.getGroupId());
        }
        assertThat("原文の記述順ではなくグループの初出順に並ぶ", identifiers, is(Arrays.asList(
                "T1", "T3", "T2",
                "a.dat", "c.dat", "b.dat",
                "M1", "M3", "M2")));
        assertThat(groupIds, is(Arrays.asList(
                "[g1]", "[g1]", "[g2]",
                "[g1]", "[g1]", "[g2]",
                "[g1]", "[g1]", "[g2]")));
        // 値そのものは失われない（入れ替わるのは並びだけ）
        List<TestDataBlock> blocks = YamlFixture.blocks(container);
        assertThat("2 番目に来た T3 の値", ((TableDataBlock) blocks.get(1)).getRows(),
                is(Arrays.asList(Arrays.asList("3"))));
        assertThat("3 番目に来た T2 の値", ((TableDataBlock) blocks.get(2)).getRows(),
                is(Arrays.asList(Arrays.asList("2"))));
    }

    // ------------------------------------------------------------------ helpers

    /**
     * {@code DEFAULT} を除く 13 データタイプすべてを 1 ファイルに書いた YAML を返す。
     *
     * @return YAML テキスト
     */
    private static String allSectionsYaml() {
        return ""
                + "setup_tables:\n"
                + "  - table: \"T1\"\n"
                + "    rows:\n"
                + "      - C: \"1\"\n"
                + "expected_tables:\n"
                + "  - table: \"T2\"\n"
                + "    rows:\n"
                + "      - C: \"2\"\n"
                + "expected_complete_tables:\n"
                + "  - table: \"T3\"\n"
                + "    rows:\n"
                + "      - C: \"3\"\n"
                + "list_maps:\n"
                + "  - id: \"lm\"\n"
                + "    rows:\n"
                + "      - K: \"v\"\n"
                + "setup_files:\n"
                + "  - path: \"sf.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n"
                + "  - path: \"sv.csv\"\n"
                + "    type: \"variable\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"c1\", type: \"半角英字\"}\n"
                + "        rows:\n"
                + "          - [\"b\"]\n"
                + "expected_files:\n"
                + "  - path: \"ef.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f2\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"c\"]\n"
                + "  - path: \"ev.csv\"\n"
                + "    type: \"variable\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"c2\", type: \"半角英字\"}\n"
                + "        rows:\n"
                + "          - [\"d\"]\n"
                + "messages:\n"
                + "  - id: \"RM01\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"m1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"e\"]\n"
                + "expected_request_header_messages:\n"
                + "  - group_id: \"g\"\n"
                + "    id: \"MSG1\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"h1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"f\"]\n"
                + "expected_request_body_messages:\n"
                + "  - group_id: \"g\"\n"
                + "    id: \"MSG2\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"b1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"g\"]\n"
                + "response_header_messages:\n"
                + "  - group_id: \"g\"\n"
                + "    id: \"MSG3\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"h2\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"h\"]\n"
                + "response_body_messages:\n"
                + "  - group_id: \"g\"\n"
                + "    id: \"MSG4\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"b2\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"i\"]\n";
    }
}
