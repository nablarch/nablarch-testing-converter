package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.yaml.YamlLoader;
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
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>実装（src/main）は変更していない。
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
     */
    @Test
    public void readsAllThirteenDataTypesFromRealYaml() {
        // Given / When
        TestDataContainer container = YamlFixture.read(folder.getRoot(), allSectionsYaml());

        // Then
        List<DataType> actual = new ArrayList<DataType>();
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
     */
    @Test
    public void readsFourBlockImplementationsFromOneRealYaml() {
        // Given / When
        TestDataContainer container = YamlFixture.read(folder.getRoot(), ""
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
        assertThat(blocks.get(1), is(instanceOf(ListMapBlock.class)));
        assertThat(blocks.get(1).getIdentifier(), is("lm"));
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
     */
    @Test
    public void readsEmptyColumnNamesAndRowsFromTableWithoutRows() {
        // Given / When
        TestDataContainer container = YamlFixture.read(folder.getRoot(),
                "setup_tables:\n  - table: \"T\"\n    rows: []\n");

        // Then
        TableDataBlock block = (TableDataBlock) onlyBlock(container);
        assertThat(block.getDataType(), is(DataType.SETUP_TABLE_DATA));
        assertThat(block.getIdentifier(), is("T"));
        assertTrue("columnNames が空であること", block.getColumnNames().isEmpty());
        assertTrue("rows が空であること", block.getRows().isEmpty());
    }

    /**
     * Given: {@code rows} を空配列にした LIST_MAP エントリ。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : ブロックは生成され、{@code columnNames} も {@code rows} も空になる（テーブル経路と同じ）。
     */
    @Test
    public void readsEmptyColumnNamesAndRowsFromListMapWithoutRows() {
        // Given / When
        TestDataContainer container = YamlFixture.read(folder.getRoot(),
                "list_maps:\n  - id: \"lm\"\n    rows: []\n");

        // Then
        ListMapBlock block = (ListMapBlock) onlyBlock(container);
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
     */
    @Test
    public void readsEmptyRecordsFromFixedFileWithoutRecords() {
        // Given / When
        TestDataContainer container = YamlFixture.read(folder.getRoot(),
                "setup_files:\n  - path: \"f.dat\"\n    type: \"fixed\"\n    records: []\n");

        // Then
        FileDataBlock block = (FileDataBlock) onlyBlock(container);
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
     */
    @Test
    public void readsEmptyRowsFromRecordLayoutWithoutRows() {
        // Given / When
        TestDataContainer container = YamlFixture.read(folder.getRoot(), ""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows: []\n");

        // Then
        FileDataBlock block = (FileDataBlock) onlyBlock(container);
        assertThat(block.getRecords().size(), is(1));
        RecordLayout record = block.getRecords().get(0);
        assertThat(record.getRecordType(), is(nullValue()));
        assertThat(record.getFields().size(), is(1));
        assertTrue("RecordLayout.rows が空であること", record.getRows().isEmpty());
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
     */
    @Test
    public void readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInFile() {
        // Given / When
        TestDataContainer container = YamlFixture.read(folder.getRoot(), ""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");

        // Then
        FileDataBlock block = (FileDataBlock) onlyBlock(container);
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
     * </p>
     */
    @Test
    public void readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInMessage() {
        // Given / When
        TestDataContainer container = YamlFixture.read(folder.getRoot(), ""
                + "messages:\n"
                + "  - id: \"RM01\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"m1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");

        // Then
        MessageDataBlock block = (MessageDataBlock) onlyBlock(container);
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
     */
    @Test
    public void readsMessageDirectivesFromRealYaml() {
        // Given / When
        TestDataContainer container = YamlFixture.read(folder.getRoot(), ""
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
        MessageDataBlock block = (MessageDataBlock) onlyBlock(container);
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
     */
    @Test
    public void namesContainerAndSectionByResourceNameWithoutBlocks() {
        // Given / When: コメントだけの YAML（トップレベルは空 Map になる）
        TestDataContainer container = YamlFixture.read(folder.getRoot(), "# no sections\n");

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
     */
    @Test
    public void readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml() {
        // Given / When
        TestDataContainer container = YamlFixture.read(folder.getRoot(), ""
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

    // ------------------------------------------------------------------ helpers

    /**
     * 唯一のブロックを返す。
     *
     * @param container 中間モデル
     * @return ブロック
     */
    private static TestDataBlock onlyBlock(TestDataContainer container) {
        List<TestDataBlock> blocks = YamlFixture.blocks(container);
        assertThat(blocks.size(), is(1));
        return blocks.get(0);
    }

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
