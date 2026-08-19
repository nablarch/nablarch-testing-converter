package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import nablarch.test.tool.converter.model.TestDataSection;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@link YamlFormatWriter} のテストクラス。
 *
 * <p>
 * 中間モデル → YAML テキストの直列化（{@code serialize}）を中心に検証する。全値ダブル
 * クォート・{@code null} はアンクォート・キーは原則無クォートを、種別ごと（テーブル／LIST_MAP／ファイル／
 * メッセージ／送信系）の出力で確認する。加えてクォート/エスケープのエッジ、{@code write} のファイル出力、
 * および実 {@link YamlFormatReader} で読み戻して同値になる往復（記法対称）を検証する。
 * </p>
 *
 * @author kiyobot
 */
public class YamlFormatWriterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final YamlFormatWriter writer = new YamlFormatWriter();

    /** 往復で実 {@link YamlFormatReader} を通すため、{@link YamlLoader} の LRU キャッシュをテスト間で残さない。 */
    @After
    public void clearLoaderCache() {
        YamlLoader.clearCacheForTest();
    }

    // ------------------------------------------------------------------------
    // テーブル系
    // ------------------------------------------------------------------------

    @Test
    public void serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation() {
        // Given: setup_tables・グループなし・${...}/null/"" を含む
        TableDataBlock block = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "USERS",
                list("ID", "NAME", "NOTE"),
                rows(row("1", "${u}", null), row("2", "", "x")));

        // When / Then
        assertThat(serialize(block), is(""
                + "setup_tables:\n"
                + "  - table: \"USERS\"\n"
                + "    rows:\n"
                + "      - ID: \"1\"\n"
                + "        NAME: \"${u}\"\n"
                + "        NOTE: null\n"
                + "      - ID: \"2\"\n"
                + "        NAME: \"\"\n"
                + "        NOTE: \"x\"\n"));
    }

    @Test
    public void serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId() {
        // Given: 同一 expected_tables に 2 グループ（整形済み [case01]/[case02]）
        TableDataBlock g1 = new TableDataBlock(DataType.EXPECTED_TABLE_DATA, "[case01]", "ORDERS",
                list("OID"), rows(row("10")));
        TableDataBlock g2 = new TableDataBlock(DataType.EXPECTED_TABLE_DATA, "[case02]", "ORDERS",
                list("OID"), rows(row("20")));

        // When / Then: 1 セクション配下に 2 エントリ・group_id は生値
        assertThat(serialize(g1, g2), is(""
                + "expected_tables:\n"
                + "  - group_id: \"case01\"\n"
                + "    table: \"ORDERS\"\n"
                + "    rows:\n"
                + "      - OID: \"10\"\n"
                + "  - group_id: \"case02\"\n"
                + "    table: \"ORDERS\"\n"
                + "    rows:\n"
                + "      - OID: \"20\"\n"));
    }

    @Test
    public void serializeTable_completed_usesExpectedCompleteTablesKey() {
        // Given
        TableDataBlock block = new TableDataBlock(DataType.EXPECTED_COMPLETED, "", "ITEMS",
                list("ITEM_ID"), rows(row("A")));

        // When / Then
        assertThat(serialize(block).startsWith("expected_complete_tables:\n"), is(true));
    }

    // ------------------------------------------------------------------------
    // LIST_MAP
    // ------------------------------------------------------------------------

    @Test
    public void serializeListMap_usesIdKeyAndColumnOrder() {
        // Given
        ListMapBlock block = new ListMapBlock("", "lm1",
                list("key", "val"), rows(row("k1", "${v1}"), row("k2", null)));

        // When / Then
        assertThat(serialize(block), is(""
                + "list_maps:\n"
                + "  - id: \"lm1\"\n"
                + "    rows:\n"
                + "      - key: \"k1\"\n"
                + "        val: \"${v1}\"\n"
                + "      - key: \"k2\"\n"
                + "        val: null\n"));
    }

    // ------------------------------------------------------------------------
    // ファイル系
    // ------------------------------------------------------------------------

    /**
     * <b>本メソッドは #25.5 §1-C（2026-08-18）で
     * {@code serializeFile_fixedWithDirectivesAndOmittedLength} から書き直した。</b>
     * 旧版は固定長ファイルの 2 レコード目に {@code length} を持たないフィールドを置き、
     * {@code - {name: "f2", type: "数値"}} と<b>長さを落として書かれる</b>ことを緑で固定していた。
     * これは XLS-30 の不具合そのものであり、番人（固定長ファイル・電文で {@code length} が
     * {@code null} なら送出）と両立しないため、長さを与えた形へ改めた。
     * {@code length} 省略の担保は可変長ファイルの
     * {@link #serializeFile_variableOmitsDirectivesAndRecordTypeAndLength} が持つ。
     */
    @Test
    public void serializeFile_fixedWithDirectivesAndMultipleRecords() {
        // Given: 固定長・directives・record_type あり・複数レコード・複数行
        RecordLayout head = new RecordLayout("head",
                list(field("f1", "半角英字", "5")), rows(row("${a}")));
        RecordLayout data = new RecordLayout("data",
                list(field("f2", "数値", "5")), rows(row("12"), row("")));
        FileDataBlock block = new FileDataBlock(DataType.SETUP_FIXED, "", "input.dat",
                FileDataBlock.FileType.FIXED, directives("file-type", "Fixed", "text-encoding", "UTF-8"),
                Arrays.asList(head, data));

        // When / Then
        assertThat(serialize(block), is(""
                + "setup_files:\n"
                + "  - path: \"input.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    directives:\n"
                + "      file-type: \"Fixed\"\n"
                + "      text-encoding: \"UTF-8\"\n"
                + "    records:\n"
                + "      - record_type: \"head\"\n"
                + "        fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"5\"}\n"
                + "        rows:\n"
                + "          - [\"${a}\"]\n"
                + "      - record_type: \"data\"\n"
                + "        fields:\n"
                + "          - {name: \"f2\", type: \"数値\", length: \"5\"}\n"
                + "        rows:\n"
                + "          - [\"12\"]\n"
                + "          - [\"\"]\n"));
    }

    /**
     * フィールド長の番人の<b>範囲</b>も兼ねる。弾くのは固定長ファイルと電文だけであり、
     * <b>可変長ファイルでは {@code length} が {@code null} であることが正しい</b>
     * （{@code testdata_notation.rst:883}（{@code 30a8271} 時点）
     * 「可変長ファイルでは、フィールド名称・データ型の2リストが同サイズで必須であり、フィールド長は不要である」）。
     * 番人の範囲を可変長まで広げるとこのテストが落ちる。
     */
    @Test
    public void serializeFile_variableOmitsDirectivesAndRecordTypeAndLength() {
        // Given: 可変長・directives なし・record_type 省略（null）・length なし
        RecordLayout record = new RecordLayout(null,
                list(field("c1", "半角英字", null), field("c2", "半角英字", null)),
                rows(row("x", "${b}")));
        FileDataBlock block = new FileDataBlock(DataType.EXPECTED_VARIABLE, "", "out.csv",
                FileDataBlock.FileType.VARIABLE, directives(), Collections.singletonList(record));

        // When / Then
        assertThat(serialize(block), is(""
                + "expected_files:\n"
                + "  - path: \"out.csv\"\n"
                + "    type: \"variable\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"c1\", type: \"半角英字\"}\n"
                + "          - {name: \"c2\", type: \"半角英字\"}\n"
                + "        rows:\n"
                + "          - [\"x\", \"${b}\"]\n"));
    }

    // ------------------------------------------------------------------------
    // MESSAGE
    // ------------------------------------------------------------------------

    @Test
    public void serializeMessage_withDirectivesAndFwHeader() {
        // Given
        RecordLayout body = new RecordLayout("body",
                list(field("m1", "半角英字", "3")), rows(row("abc")));
        MessageDataBlock block = new MessageDataBlock(DataType.MESSAGE, "", "RM01",
                directives("text-encoding", "Windows-31J"),
                fwHeader("requestId", "RM01", "userId", "${user}"),
                Collections.singletonList(body));

        // When / Then
        assertThat(serialize(block), is(""
                + "messages:\n"
                + "  - id: \"RM01\"\n"
                + "    directives:\n"
                + "      text-encoding: \"Windows-31J\"\n"
                + "    fw_header:\n"
                + "      requestId: \"RM01\"\n"
                + "      userId: \"${user}\"\n"
                + "    records:\n"
                + "      - record_type: \"body\"\n"
                + "        fields:\n"
                + "          - {name: \"m1\", type: \"半角英字\", length: \"3\"}\n"
                + "        rows:\n"
                + "          - [\"abc\"]\n"));
    }

    // ------------------------------------------------------------------------
    // 送信同期メッセージ（送信系 4 種）
    // ------------------------------------------------------------------------

    @Test
    public void serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField() {
        // Given
        RecordLayout record = new RecordLayout(null,
                list(field("no", "半角英字", "1"), field("s1", "半角英字", "2")),
                rows(row("1", "${z}")));
        MessageDataBlock block = new MessageDataBlock(DataType.EXPECTED_REQUEST_HEADER_MESSAGES,
                "[case1]", "MSG1", directives(), fwHeader(), Collections.singletonList(record));

        // When / Then
        assertThat(serialize(block), is(""
                + "expected_request_header_messages:\n"
                + "  - group_id: \"case1\"\n"
                + "    id: \"MSG1\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"no\", type: \"半角英字\", length: \"1\"}\n"
                + "          - {name: \"s1\", type: \"半角英字\", length: \"2\"}\n"
                + "        rows:\n"
                + "          - [\"1\", \"${z}\"]\n"));
    }

    @Test
    public void serializeSendSync_allFourSectionKeys() {
        // Given
        MessageDataBlock h = sendSync(DataType.EXPECTED_REQUEST_HEADER_MESSAGES);
        MessageDataBlock b = sendSync(DataType.EXPECTED_REQUEST_BODY_MESSAGES);
        MessageDataBlock rh = sendSync(DataType.RESPONSE_HEADER_MESSAGES);
        MessageDataBlock rb = sendSync(DataType.RESPONSE_BODY_MESSAGES);

        // When / Then
        String yaml = serialize(h, b, rh, rb);
        assertTrue(yaml.contains("expected_request_header_messages:\n"));
        assertTrue(yaml.contains("expected_request_body_messages:\n"));
        assertTrue(yaml.contains("response_header_messages:\n"));
        assertTrue(yaml.contains("response_body_messages:\n"));
    }

    // ------------------------------------------------------------------------
    // セクション構成
    // ------------------------------------------------------------------------

    @Test
    public void serialize_multipleSections_separatedByBlankLineInEncounterOrder() {
        // Given
        TableDataBlock table = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T1",
                list("C"), rows(row("1")));
        MessageDataBlock message = new MessageDataBlock(DataType.MESSAGE, "", "M",
                directives(), fwHeader(),
                Collections.singletonList(new RecordLayout(null, list(field("f", "半角英字", "1")), rows(row("v")))));

        // When / Then
        assertThat(serialize(table, message), is(""
                + "setup_tables:\n"
                + "  - table: \"T1\"\n"
                + "    rows:\n"
                + "      - C: \"1\"\n"
                + "\n"
                + "messages:\n"
                + "  - id: \"M\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"v\"]\n"));
    }

    @Test
    public void serialize_emptySection_isEmptyString() {
        // When / Then
        assertThat(writer.serialize(new TestDataSection("td", Collections.<TestDataBlock>emptyList())), is(""));
    }

    // ------------------------------------------------------------------------
    // クォート・エスケープ
    // ------------------------------------------------------------------------

    @Test
    public void serialize_escapesQuotesBackslashAndControlChars() {
        // Given: " \ 改行 復帰 タブ 制御文字(0x01) を含む値
        TableDataBlock block = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                list("V"), rows(row("a\"b\\c\n\r\t\u0001")));

        // When / Then
        assertThat(serialize(block), is(""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - V: \"a\\\"b\\\\c\\n\\r\\t\\x01\"\n"));
    }

    @Test
    public void serialize_surrogatePair_isOutputAsUtf8WithoutEscape() {
        // Given: BMP 外文字（U+1F600 😀）を含む値。SnakeYAML Engine がエスケープせず UTF-8 直書きすることを実証
        String emoji = new String(Character.toChars(0x1F600));
        TableDataBlock block = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                list("V"), rows(row(emoji), row("abc" + emoji + "xyz")));

        // When / Then
        assertThat(serialize(block), is(""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - V: \"" + emoji + "\"\n"
                + "      - V: \"abc" + emoji + "xyz\"\n"));
    }

    @Test
    public void serialize_quotesKeyContainingSpecialChars() {
        // Given: directives キーに YAML 特殊文字（コロン）／空白
        // （本文レコードは電文の契約上 1 件以上が必要なため最小の 1 件を置く。検証対象はキーの引用のみ）
        MessageDataBlock block = new MessageDataBlock(DataType.MESSAGE, "", "M",
                directives("a:b", "1", "c d", "2"), fwHeader(), Collections.singletonList(minimalBody()));

        // When / Then
        assertThat(serialize(block), is(""
                + "messages:\n"
                + "  - id: \"M\"\n"
                + "    directives:\n"
                + "      \"a:b\": \"1\"\n"
                + "      \"c d\": \"2\"\n"
                + MINIMAL_BODY_YAML));
    }

    @Test
    public void serialize_emptyKey_isQuoted() {
        // Given: 空文字キー（退化ケース）→ クォートされる
        // （本文レコードは電文の契約上 1 件以上が必要なため最小の 1 件を置く。検証対象はキーの引用のみ）
        MessageDataBlock block = new MessageDataBlock(DataType.MESSAGE, "", "M",
                directives("", "v"), fwHeader(), Collections.singletonList(minimalBody()));

        // When / Then
        assertThat(serialize(block), is(""
                + "messages:\n"
                + "  - id: \"M\"\n"
                + "    directives:\n"
                + "      \"\": \"v\"\n"
                + MINIMAL_BODY_YAML));
    }

    @Test
    public void serialize_distinguishesNullFromNullString() {
        // Given: 1 行目は明示 null、2 行目は文字列 "null"
        TableDataBlock block = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                list("V"), rows(row((String) null), row("null")));

        // When / Then
        assertThat(serialize(block), is(""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - V: null\n"
                + "      - V: \"null\"\n"));
    }

    // ------------------------------------------------------------------------
    // 空コレクション・退化ケース・異常系
    // ------------------------------------------------------------------------

    @Test
    public void serialize_emptyRows_emitsEmptyFlowList() {
        // Given
        TableDataBlock block = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                list("C"), rows());

        // When / Then
        assertThat(serialize(block), is(""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows: []\n"));
    }

    @Test
    public void serialize_emptyColumnsRow_emitsEmptyFlowMap() {
        // Given
        TableDataBlock block = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                new ArrayList<String>(), rows(row()));

        // When / Then
        assertThat(serialize(block), is(""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - {}\n"));
    }

    @Test
    public void serialize_recordWithEmptyRows_emitsEmptyFlowList() {
        // Given: フィールドは 1 件（0 件は書き出し時に弾かれる）、データ行だけが空
        RecordLayout empty = new RecordLayout(null,
                list(new FieldDef("c1", "半角英字", "5")), rows());
        FileDataBlock block = new FileDataBlock(DataType.SETUP_FIXED, "", "f.dat",
                FileDataBlock.FileType.FIXED, directives(), Collections.singletonList(empty));

        // When / Then
        assertThat(serialize(block), is(""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"c1\", type: \"半角英字\", length: \"5\"}\n"
                + "        rows: []\n"));
    }

    @Test
    public void serialize_rowShorterThanColumns_fillsMissingWithNull() {
        // Given: カラム 2 列に対し値 1 つだけの行（不足分は null 補完）
        TableDataBlock block = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                list("A", "B"), rows(row("x")));

        // When / Then
        assertThat(serialize(block), is(""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - A: \"x\"\n"
                + "        B: null\n"));
    }

    /**
     * Given: データ型が空文字のフィールド。
     * When : serialize。
     * Then : {@code type: ""} として書かれる（番人が弾くのは {@code null} だけであり、
     *        空文字は弾かない。{@link FieldDef} の契約は「{@code type} は必須（{@code null} 不可）」）。
     */
    @Test
    public void serialize_fieldWithEmptyType_emitsEmptyType() {
        // Given
        RecordLayout record = new RecordLayout(null,
                list(field("c1", "", null)), rows(row("v")));
        FileDataBlock block = new FileDataBlock(DataType.EXPECTED_VARIABLE, "", "out.csv",
                FileDataBlock.FileType.VARIABLE, directives(), Collections.singletonList(record));

        // When / Then
        assertTrue(serialize(block).contains("          - {name: \"c1\", type: \"\"}\n"));
    }

    /**
     * Given: フィールド長が {@code null} のフィールドを持つ固定長ファイルブロック。
     * When : serialize。
     * Then : IllegalArgumentException（記法は固定長ファイルについて「フィールド名称・データ型・
     *        フィールド長の3リストが同サイズで必須」と定めており（{@code testdata_notation.rst:883}。
     *        {@code 30a8271} 時点）、長さを落とした {@code fields:} は書き手の意図どおりには読み戻せないため、
     *        黙って書かず早期に失敗する）。
     */
    @Test(expected = IllegalArgumentException.class)
    public void serialize_fieldWithoutLengthInFixedFileBlock_rejected() {
        // Given
        RecordLayout record = new RecordLayout("data",
                list(field("c1", "半角英字", null)), rows(row("v")));
        FileDataBlock block = new FileDataBlock(DataType.SETUP_FIXED, "", "in.dat",
                FileDataBlock.FileType.FIXED, directives(), Collections.singletonList(record));

        // When / Then
        serialize(block);
    }

    /**
     * Given: フィールド長が {@code null} のフィールドを持つメッセージブロック。
     * When : serialize。
     * Then : IllegalArgumentException（メッセージボディは「フィールド名称・データ型・フィールド長・データ
     *        という、前述のファイルデータと同じ構成」を持つ（{@code testdata_notation.rst:1158}。
     *        {@code 30a8271} 時点）ため、固定長ファイルと同じ制約に掛かる）。
     */
    @Test(expected = IllegalArgumentException.class)
    public void serialize_fieldWithoutLengthInMessageBlock_rejected() {
        // Given
        RecordLayout record = new RecordLayout("data",
                list(field("c1", "半角英字", null)), rows(row("v")));
        MessageDataBlock block = new MessageDataBlock(DataType.MESSAGE, "", "msg1",
                directives(), directives(), Collections.singletonList(record));

        // When / Then
        serialize(block);
    }

    @Test
    public void serialize_keyStartingWithIndicator_isQuoted() {
        // Given: 先頭が YAML インジケータ（'-'）のキー
        // （本文レコードは電文の契約上 1 件以上が必要なため最小の 1 件を置く。検証対象はキーの引用のみ）
        MessageDataBlock block = new MessageDataBlock(DataType.MESSAGE, "", "M",
                directives("-x", "1"), fwHeader(), Collections.singletonList(minimalBody()));

        // When / Then
        assertTrue(serialize(block).contains("      \"-x\": \"1\"\n"));
    }

    @Test
    public void serialize_unbracketedGroupId_isUsedAsRawValue() {
        // Given: 整形されていない素のグループ ID（防御的経路）
        TableDataBlock block = new TableDataBlock(DataType.SETUP_TABLE_DATA, "raw", "T",
                list("C"), rows(row("1")));

        // When / Then
        assertTrue(serialize(block).contains("  - group_id: \"raw\"\n"));
    }

    @Test
    public void write_ioError_throwsUncheckedIOException() throws Exception {
        // Given: 親に通常ファイルが居座る出力先（ディレクトリ作成不可）
        File file = folder.newFile("blocker");
        TableDataBlock block = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T", list("C"), rows(row("1")));
        TestDataSection section = new TestDataSection("td", Collections.<TestDataBlock>singletonList(block));
        TestDataContainer container = new TestDataContainer("td", Collections.singletonList(section));

        // When / Then
        try {
            writer.write(container, file.getAbsolutePath());
            fail("should throw");
        } catch (UncheckedIOException e) {
            assertTrue(e.getMessage().contains("failed to write YAML"));
        }
    }

    // ------------------------------------------------------------------------
    // write（ファイル出力）
    // ------------------------------------------------------------------------

    @Test
    public void write_writesEachSectionAsYamlFileWithSerializedContent() throws Exception {
        // Given
        TableDataBlock block = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "USERS",
                list("ID"), rows(row("1")));
        TestDataSection section = new TestDataSection("td", Collections.<TestDataBlock>singletonList(block));
        TestDataContainer container = new TestDataContainer("td", Collections.singletonList(section));

        // When
        writer.write(container, folder.getRoot().getAbsolutePath());

        // Then: <basePath>/td.yaml に serialize と同一内容
        File out = new File(folder.getRoot(), "td.yaml");
        assertTrue(out.exists());
        String content = new String(Files.readAllBytes(out.toPath()), StandardCharsets.UTF_8);
        assertThat(content, is(writer.serialize(section)));
    }

    // ------------------------------------------------------------------------
    // 往復（モデル → write → 実 YamlFormatReader → モデル）
    // ------------------------------------------------------------------------

    @Test
    public void roundTrip_table_isPreservedThroughRealReader() {
        // Given
        TableDataBlock original = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "USERS",
                list("ID", "NAME", "NOTE"), rows(row("1", "${u}", null), row("2", "", "x")));

        // When
        TableDataBlock back = (TableDataBlock) roundTrip(original);

        // Then
        assertThat(back.getDataType(), is(DataType.SETUP_TABLE_DATA));
        assertThat(back.getIdentifier(), is("USERS"));
        assertThat(back.getColumnNames(), is(list("ID", "NAME", "NOTE")));
        assertThat(back.getRows().get(0), is(Arrays.asList("1", "${u}", null)));
        assertThat(back.getRows().get(1), is(Arrays.asList("2", "", "x")));
    }

    /**
     * <b>{@code f2} のフィールド長は #25.5 §1-C（2026-08-18）で {@code null} から {@code "5"} へ改めた。</b>
     * 旧版は固定長ファイルで {@code length} を持たないフィールドが往復しても {@code null} のまま残ることを
     * 緑で固定しており、XLS-30 の番人（固定長ファイル・電文で {@code length} が {@code null} なら送出）と
     * 両立しなかったためである。{@code length} 省略の担保は可変長ファイルの
     * {@link #serializeFile_variableOmitsDirectivesAndRecordTypeAndLength} が持つ。
     */
    @Test
    public void roundTrip_fixedFile_isPreservedThroughRealReader() {
        // Given
        RecordLayout head = new RecordLayout("head", list(field("f1", "半角英字", "5")), rows(row("${a}")));
        RecordLayout data = new RecordLayout("data", list(field("f2", "数値", "5")), rows(row("12"), row("")));
        FileDataBlock original = new FileDataBlock(DataType.SETUP_FIXED, "", "f.dat",
                FileDataBlock.FileType.FIXED, directives(), Arrays.asList(head, data));

        // When
        FileDataBlock back = (FileDataBlock) roundTrip(original);

        // Then
        assertThat(back.getDataType(), is(DataType.SETUP_FIXED));
        assertThat(back.getFileType(), is(FileDataBlock.FileType.FIXED));
        assertThat(back.getIdentifier(), is("f.dat"));
        assertThat(back.getRecords().size(), is(2));
        assertThat(back.getRecords().get(0).getRecordType(), is("head"));
        assertFieldDef(back.getRecords().get(0).getFields().get(0), "f1", "半角英字", "5");
        assertThat(back.getRecords().get(0).getRows().get(0), is(Arrays.asList("${a}")));
        assertThat(back.getRecords().get(1).getRecordType(), is("data"));
        assertFieldDef(back.getRecords().get(1).getFields().get(0), "f2", "数値", "5");
        assertThat(back.getRecords().get(1).getRows().get(0), is(Arrays.asList("12")));
        assertThat(back.getRecords().get(1).getRows().get(1), is(Arrays.asList("")));
    }

    @Test
    public void roundTrip_message_preservesFwHeaderAndBody() {
        // Given
        RecordLayout body = new RecordLayout("body", list(field("m1", "半角英字", "3")), rows(row("abc")));
        MessageDataBlock original = new MessageDataBlock(DataType.MESSAGE, "", "RM01",
                directives(), fwHeader("requestId", "RM01", "userId", "${user}"),
                Collections.singletonList(body));

        // When
        MessageDataBlock back = (MessageDataBlock) roundTrip(original);

        // Then
        assertThat(back.getDataType(), is(DataType.MESSAGE));
        assertThat(back.getIdentifier(), is("RM01"));
        assertThat(back.getFwHeaderFields().get("requestId"), is("RM01"));
        assertThat(back.getFwHeaderFields().get("userId"), is("${user}"));
        assertThat(back.getRecords().get(0).getRecordType(), is("body"));
        assertThat(back.getRecords().get(0).getRows().get(0), is(Arrays.asList("abc")));
    }

    @Test
    public void roundTrip_sendSync_preservesGroupIdAndNoField() {
        // Given
        RecordLayout record = new RecordLayout(null,
                list(field("no", "半角英字", "1"), field("s1", "半角英字", "2")), rows(row("1", "${z}")));
        MessageDataBlock original = new MessageDataBlock(DataType.EXPECTED_REQUEST_HEADER_MESSAGES,
                "[case1]", "MSG1", directives(), fwHeader(), Collections.singletonList(record));

        // When
        MessageDataBlock back = (MessageDataBlock) roundTrip(original);

        // Then
        assertThat(back.getDataType(), is(DataType.EXPECTED_REQUEST_HEADER_MESSAGES));
        assertThat(back.getGroupId(), is("[case1]"));
        assertThat(back.getIdentifier(), is("MSG1"));
        assertTrue(back.getFwHeaderFields().isEmpty());
        assertFieldDef(back.getRecords().get(0).getFields().get(0), "no", "半角英字", "1");
        assertThat(back.getRecords().get(0).getRows().get(0), is(Arrays.asList("1", "${z}")));
    }

    @Test
    public void roundTrip_leadingTrailingWhitespace_isPreservedThroughRealReader() {
        // Given: 前後・中間に半角/全角空白を持つ値（全値クォートが効かなければ脱落する）
        TableDataBlock original = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "WS",
                list("LEAD", "TRAIL", "MID", "FULL"),
                rows(row(" lead", "trail ", " mid ", "　全角　")));

        // When / Then: 読み戻しても前後空白が脱落しない（クォートが効いている証明）
        TableDataBlock back = (TableDataBlock) roundTrip(original);
        assertThat(back.getRows().get(0), is(Arrays.asList(" lead", "trail ", " mid ", "　全角　")));
    }

    @Test
    public void roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader() {
        // Given: 明示 null・文字列 "null"・数値文字列 "123"（全値クォート＋null例外規則の往復健全性）
        TableDataBlock original = new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                list("V"), rows(row((String) null), row("null"), row("123")));

        // When / Then: null は null、"null"/"123" は文字列として区別保持される
        TableDataBlock back = (TableDataBlock) roundTrip(original);
        assertThat(back.getRows().get(0), is(Arrays.asList((String) null)));
        assertThat(back.getRows().get(1), is(Arrays.asList("null")));
        assertThat(back.getRows().get(2), is(Arrays.asList("123")));
    }

    // ------------------------------------------------------------------------
    // ヘルパー
    // ------------------------------------------------------------------------

    /** ブロック群を 1 セクションに包んで直列化する。 */
    private String serialize(TestDataBlock... blocks) {
        return writer.serialize(new TestDataSection("td", Arrays.asList(blocks)));
    }

    /** ブロックを write→実 YamlFormatReader で読み戻し、唯一のブロックを返す。 */
    private TestDataBlock roundTrip(TestDataBlock block) {
        TestDataSection section = new TestDataSection("td", Collections.singletonList(block));
        writer.write(new TestDataContainer("td", Collections.singletonList(section)),
                folder.getRoot().getAbsolutePath());
        TestDataContainer back = new YamlFormatReader().read(folder.getRoot().getAbsolutePath(), "td");
        List<TestDataBlock> blocks = back.getSections().get(0).getBlocks();
        assertThat(blocks.size(), is(1));
        return blocks.get(0);
    }

    private static FieldDef field(String name, String type, String length) {
        return new FieldDef(name, type, length);
    }

    private static List<FieldDef> list(FieldDef... fields) {
        return new ArrayList<FieldDef>(Arrays.asList(fields));
    }

    private static List<String> list(String... values) {
        return new ArrayList<String>(Arrays.asList(values));
    }

    private static List<String> row(String... values) {
        return new ArrayList<String>(Arrays.asList(values));
    }

    private static List<List<String>> rows(List<String>... rows) {
        return new ArrayList<List<String>>(Arrays.asList(rows));
    }

    private static Map<String, String> directives(String... kv) {
        return strMap(kv);
    }

    private static Map<String, String> fwHeader(String... kv) {
        return strMap(kv);
    }

    private static Map<String, String> strMap(String... kv) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put(kv[i], kv[i + 1]);
        }
        return map;
    }

    /**
     * 電文の契約（本文レコードは 1 件以上）を満たすためだけの最小の本文レコードを作る。
     * キーの引用など本文と無関係な検証で使う。出力は {@link #MINIMAL_BODY_YAML} と対になる。
     *
     * @return フィールド 1 件・データ行 1 件のレコードレイアウト
     */
    private static RecordLayout minimalBody() {
        return new RecordLayout(null, list(field("f", "半角英字", "1")), rows(row("v")));
    }

    /** {@link #minimalBody()} が {@code messages} 経路で書き出される YAML。 */
    private static final String MINIMAL_BODY_YAML = ""
            + "    records:\n"
            + "      - fields:\n"
            + "          - {name: \"f\", type: \"半角英字\", length: \"1\"}\n"
            + "        rows:\n"
            + "          - [\"v\"]\n";

    private static MessageDataBlock sendSync(DataType type) {
        return new MessageDataBlock(type, "[g]", "ID", directives(), fwHeader(),
                Collections.singletonList(new RecordLayout(null, list(field("f", "半角英字", "1")), rows(row("v")))));
    }

    private static void assertFieldDef(FieldDef field, String name, String type, String length) {
        assertThat(field.getName(), is(name));
        assertThat(field.getType(), is(type));
        if (length == null) {
            assertThat(field.getLength(), is(nullValue()));
        } else {
            assertThat(field.getLength(), is(length));
        }
    }
}
