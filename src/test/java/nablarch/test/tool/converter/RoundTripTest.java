package nablarch.test.tool.converter;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

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
import nablarch.test.tool.converter.xls.XlsFormatReader;
import nablarch.test.tool.converter.xls.XlsFormatWriter;
import nablarch.test.tool.converter.yaml.YamlFormatReader;
import nablarch.test.tool.converter.yaml.YamlFormatWriter;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 往復変換の可逆性確認（品質担保 Level2）。
 *
 * <p>
 * 全データブロック種別（SETUP_TABLE / EXPECTED_TABLE / EXPECTED_COMPLETE_TABLE / LIST_MAP /
 * SETUP_FIXED / EXPECTED_FIXED / SETUP_VARIABLE / EXPECTED_VARIABLE / MESSAGE /
 * EXPECTED_REQUEST_HEADER_MESSAGES / EXPECTED_REQUEST_BODY_MESSAGES /
 * RESPONSE_HEADER_MESSAGES / RESPONSE_BODY_MESSAGES）に対して、
 * 中間モデル → 同一形式 → 中間モデル の往復（XLS 経路・YAML 経路それぞれ）で
 * NTF 仕様上の意味が不変であることを検証する。
 * </p>
 *
 * <p><b>可逆性の対象外:</b>
 * Excel の色・書式・結合セル・コメント、YAML のコメントはいずれも中間モデルに乗らない。
 * 往復後の Excel は {@link nablarch.test.tool.converter.xls.ExcelFormatConfig} デフォルト整形が付く。
 * </p>
 *
 * @author kiyobot
 */
public class RoundTripTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** YAML 経路の往復で YamlLoader の LRU キャッシュがテスト間へ漏れないようクリアする。 */
    @After
    public void clearLoaderCache() {
        YamlLoader.clearCacheForTest();
    }

    // ======================================================================
    // XLS 経路（中間モデル → Excel → 中間モデル）
    // ======================================================================

    /**
     * Given: SETUP_TABLE ブロック（特殊記法・空文字含む）。
     * When : XLS 経路往復。
     * Then : データタイプ・識別子・カラム名・行が一致。
     */
    @Test
    public void xls_setupTable_isPreserved() {
        // Given
        TableDataBlock original = table(DataType.SETUP_TABLE_DATA, "", "USERS",
                cols("ID", "NAME"), rows(row("${id}", "alice"), row("2", "")));

        // When
        TableDataBlock back = (TableDataBlock) xlsRoundTrip("xls_setup", "s", original);

        // Then
        assertTableBlock(original, back);
    }

    /**
     * Given: EXPECTED_TABLE ブロック（グループ ID 付き）。
     * When : XLS 経路往復。
     * Then : グループ ID・識別子・カラム名・行が一致。
     */
    @Test
    public void xls_expectedTable_withGroupId_isPreserved() {
        // Given
        TableDataBlock original = table(DataType.EXPECTED_TABLE_DATA, "case1", "ORDERS",
                cols("OID", "STATUS"), rows(row("10", "OK")));

        // When
        TableDataBlock back = (TableDataBlock) xlsRoundTrip("xls_expected", "s", original);

        // Then
        assertTableBlock(original, back);
    }

    /**
     * Given: EXPECTED_COMPLETE_TABLE ブロック。
     * When : XLS 経路往復。
     * Then : データタイプ・識別子・カラム名・行が一致。
     */
    @Test
    public void xls_expectedCompleteTable_isPreserved() {
        // Given
        TableDataBlock original = table(DataType.EXPECTED_COMPLETED, "", "ITEMS",
                cols("CODE", "QTY"), rows(row("A01", "5")));

        // When
        TableDataBlock back = (TableDataBlock) xlsRoundTrip("xls_complete", "s", original);

        // Then
        assertTableBlock(original, back);
    }

    /**
     * Given: LIST_MAP ブロック（列順・特殊記法含む）。
     * When : XLS 経路往復。
     * Then : 識別子・カラム名（列順）・行が一致。
     */
    @Test
    public void xls_listMap_isPreserved() {
        // Given
        ListMapBlock original = listMap("", "result",
                cols("ID", "NAME"), rows(row("${id}", ""), row("2", "bob")));

        // When
        ListMapBlock back = (ListMapBlock) xlsRoundTrip("xls_listmap", "s", original);

        // Then
        assertThat(back.getDataType(), is(DataType.LIST_MAP));
        assertThat(back.getIdentifier(), is("result"));
        assertThat(back.getColumnNames(), is(cols("ID", "NAME")));
        assertThat(back.getRows(), is(original.getRows()));
    }

    /**
     * Given: SETUP_FIXED ブロック（型・長さ省略 {@code -}・ディレクティブ含む）。
     * When : XLS 経路往復。
     * Then : ファイルタイプ・識別子・ディレクティブ・レコード種別・型・長さ・行が一致。
     */
    @Test
    public void xls_setupFixed_isPreserved() {
        // Given
        RecordLayout rec = record("data",
                fields(field("f1", "半角英字", "-"), field("f2", "数値", "5")),
                rows(row("${v}", "12")));
        FileDataBlock original = file(DataType.SETUP_FIXED, "", "t.dat",
                directives("text-encoding", "UTF-8"),
                Collections.singletonList(rec));

        // When
        FileDataBlock back = (FileDataBlock) xlsRoundTrip("xls_sfixed", "s", original);

        // Then
        assertFileBlock(original, back);
    }

    /**
     * Given: EXPECTED_FIXED ブロック。
     * When : XLS 経路往復。
     * Then : ファイルタイプ・識別子・レコード・行が一致。
     */
    @Test
    public void xls_expectedFixed_isPreserved() {
        // Given
        RecordLayout rec = record("data",
                fields(field("f1", "半角英字", "3")),
                rows(row("abc")));
        FileDataBlock original = file(DataType.EXPECTED_FIXED, "", "e.dat",
                directives(),
                Collections.singletonList(rec));

        // When
        FileDataBlock back = (FileDataBlock) xlsRoundTrip("xls_efixed", "s", original);

        // Then
        assertFileBlock(original, back);
    }

    /**
     * Given: SETUP_VARIABLE ブロック（長さ null）。
     * When : XLS 経路往復。
     * Then : ファイルタイプ・フィールド長（null）・行が一致。
     */
    @Test
    public void xls_setupVariable_isPreserved() {
        // Given
        RecordLayout rec = record("data",
                fields(field("f1", "半角英字", null)),
                rows(row("${v}")));
        FileDataBlock original = file(DataType.SETUP_VARIABLE, "", "in.csv",
                directives(),
                Collections.singletonList(rec));

        // When
        FileDataBlock back = (FileDataBlock) xlsRoundTrip("xls_svar", "s", original);

        // Then
        assertFileBlock(original, back);
    }

    /**
     * Given: EXPECTED_VARIABLE ブロック。
     * When : XLS 経路往復。
     * Then : ファイルタイプ・識別子・行が一致。
     */
    @Test
    public void xls_expectedVariable_isPreserved() {
        // Given
        RecordLayout rec = record("data",
                fields(field("f1", "半角英字", null)),
                rows(row("ok")));
        FileDataBlock original = file(DataType.EXPECTED_VARIABLE, "", "out.csv",
                directives(),
                Collections.singletonList(rec));

        // When
        FileDataBlock back = (FileDataBlock) xlsRoundTrip("xls_evar", "s", original);

        // Then
        assertFileBlock(original, back);
    }

    /**
     * Given: MESSAGE ブロック（FW ヘッダ・本文含む）。
     * When : XLS 経路往復。
     * Then : 識別子・FW ヘッダ・本文レコードが一致。
     */
    @Test
    public void xls_message_isPreserved() {
        // Given
        RecordLayout body = record("body",
                fields(field("m1", "半角英字", "5")),
                rows(row("${b}")));
        MessageDataBlock original = message(DataType.MESSAGE, "", "RM01",
                directives(), fwHeader("requestId", "RM01", "userId", "${u}"),
                Collections.singletonList(body));

        // When
        MessageDataBlock back = (MessageDataBlock) xlsRoundTrip("xls_msg", "s", original);

        // Then
        assertMessageBlock(original, back);
    }

    /**
     * Given: EXPECTED_REQUEST_HEADER_MESSAGES ブロック（送信系・グループ ID 付き）。
     * When : XLS 経路往復。
     * Then : データタイプ・グループ ID・識別子・フィールド・行が一致（FW ヘッダは空）。
     */
    @Test
    public void xls_expectedRequestHeaderMessages_isPreserved() {
        // Given
        RecordLayout rec = record("no",
                fields(field("requestId", "半角英字", "20")),
                rows(row("RM01")));
        MessageDataBlock original = message(DataType.EXPECTED_REQUEST_HEADER_MESSAGES,
                "case1", "RM01", directives(), fwHeader(),
                Collections.singletonList(rec));

        // When
        MessageDataBlock back = (MessageDataBlock) xlsRoundTrip("xls_erh", "s", original);

        // Then
        assertMessageBlock(original, back);
    }

    /**
     * Given: EXPECTED_REQUEST_BODY_MESSAGES ブロック。
     * When : XLS 経路往復。
     * Then : データタイプ・グループ ID・識別子・行が一致。
     */
    @Test
    public void xls_expectedRequestBodyMessages_isPreserved() {
        // Given
        RecordLayout rec = record("no",
                fields(field("body", "半角英字", "10")),
                rows(row("${body}")));
        MessageDataBlock original = message(DataType.EXPECTED_REQUEST_BODY_MESSAGES,
                "case1", "RM01", directives(), fwHeader(),
                Collections.singletonList(rec));

        // When
        MessageDataBlock back = (MessageDataBlock) xlsRoundTrip("xls_erb", "s", original);

        // Then
        assertMessageBlock(original, back);
    }

    /**
     * Given: RESPONSE_HEADER_MESSAGES ブロック。
     * When : XLS 経路往復。
     * Then : データタイプ・グループ ID・識別子・行が一致。
     */
    @Test
    public void xls_responseHeaderMessages_isPreserved() {
        // Given
        RecordLayout rec = record("no",
                fields(field("responseId", "半角英字", "20")),
                rows(row("RES01")));
        MessageDataBlock original = message(DataType.RESPONSE_HEADER_MESSAGES,
                "case1", "RES01", directives(), fwHeader(),
                Collections.singletonList(rec));

        // When
        MessageDataBlock back = (MessageDataBlock) xlsRoundTrip("xls_rh", "s", original);

        // Then
        assertMessageBlock(original, back);
    }

    /**
     * Given: RESPONSE_BODY_MESSAGES ブロック。
     * When : XLS 経路往復。
     * Then : データタイプ・グループ ID・識別子・行が一致。
     */
    @Test
    public void xls_responseBodyMessages_isPreserved() {
        // Given
        RecordLayout rec = record("no",
                fields(field("resBody", "半角英字", "5")),
                rows(row("${rb}")));
        MessageDataBlock original = message(DataType.RESPONSE_BODY_MESSAGES,
                "case1", "RES01", directives(), fwHeader(),
                Collections.singletonList(rec));

        // When
        MessageDataBlock back = (MessageDataBlock) xlsRoundTrip("xls_rb", "s", original);

        // Then
        assertMessageBlock(original, back);
    }

    // ======================================================================
    // YAML 経路（中間モデル → YAML → 中間モデル）
    // ======================================================================

    /**
     * Given: SETUP_TABLE ブロック（特殊記法・空文字・null 含む）。
     * When : YAML 経路往復。
     * Then : データタイプ・識別子・カラム名・行が一致（null は null のまま保持）。
     */
    @Test
    public void yaml_setupTable_isPreserved() {
        // Given
        TableDataBlock original = table(DataType.SETUP_TABLE_DATA, "", "USERS",
                cols("ID", "NAME", "NOTE"),
                rows(row("${id}", "alice", null), row("2", "", "x")));

        // When
        TableDataBlock back = (TableDataBlock) yamlRoundTrip("yaml_setup", original);

        // Then
        assertTableBlock(original, back);
    }

    /**
     * Given: EXPECTED_TABLE ブロック（グループ ID 付き）。
     * When : YAML 経路往復。
     * Then : グループ ID・識別子・カラム名・行が一致。
     */
    @Test
    public void yaml_expectedTable_withGroupId_isPreserved() {
        // Given
        TableDataBlock original = table(DataType.EXPECTED_TABLE_DATA, "case1", "ORDERS",
                cols("OID", "STATUS"), rows(row("10", "OK")));

        // When
        TableDataBlock back = (TableDataBlock) yamlRoundTrip("yaml_expected", original);

        // Then
        assertTableBlock(original, back);
    }

    /**
     * Given: EXPECTED_COMPLETE_TABLE ブロック。
     * When : YAML 経路往復。
     * Then : データタイプ・識別子・カラム名・行が一致。
     */
    @Test
    public void yaml_expectedCompleteTable_isPreserved() {
        // Given
        TableDataBlock original = table(DataType.EXPECTED_COMPLETED, "", "ITEMS",
                cols("CODE", "QTY"), rows(row("A01", "5")));

        // When
        TableDataBlock back = (TableDataBlock) yamlRoundTrip("yaml_complete", original);

        // Then
        assertTableBlock(original, back);
    }

    /**
     * Given: LIST_MAP ブロック（列順・マーカー除外後・特殊記法含む）。
     * When : YAML 経路往復。
     * Then : 識別子・カラム名（YAML 列順）・行が一致。
     */
    @Test
    public void yaml_listMap_isPreserved() {
        // Given
        ListMapBlock original = listMap("", "result",
                cols("ID", "NAME"), rows(row("${id}", ""), row("2", "bob")));

        // When
        ListMapBlock back = (ListMapBlock) yamlRoundTrip("yaml_listmap", original);

        // Then
        assertThat(back.getDataType(), is(DataType.LIST_MAP));
        assertThat(back.getIdentifier(), is("result"));
        assertThat(back.getColumnNames(), is(cols("ID", "NAME")));
        assertThat(back.getRows(), is(original.getRows()));
    }

    /**
     * Given: LIST_MAP ブロック（null 値を含む行）。
     * When : YAML 経路往復。
     * Then : null セルが null のまま保持される（TABLE と同じ YAML 経路の null 保持を LIST_MAP で実証）。
     */
    @Test
    public void yaml_listMap_withNullValue_isPreserved() {
        // Given
        ListMapBlock original = listMap("", "result",
                cols("ID", "NOTE"), rows(row("1", null), row("2", "x")));

        // When
        ListMapBlock back = (ListMapBlock) yamlRoundTrip("yaml_listmap_null", original);

        // Then
        assertThat(back.getRows().get(0).get(1), nullValue());
        assertThat(back.getRows().get(1).get(1), is("x"));
    }

    /**
     * Given: SETUP_FIXED ブロック（型・長さ省略 {@code -}・ディレクティブ含む）。
     * When : YAML 経路往復。
     * Then : ファイルタイプ・識別子・ディレクティブ・レコード種別・型・長さ・行が一致。
     */
    @Test
    public void yaml_setupFixed_isPreserved() {
        // Given
        RecordLayout rec = record("data",
                fields(field("f1", "半角英字", "-"), field("f2", "数値", "5")),
                rows(row("${v}", "12")));
        FileDataBlock original = file(DataType.SETUP_FIXED, "", "t.dat",
                directives("text-encoding", "UTF-8"),
                Collections.singletonList(rec));

        // When
        FileDataBlock back = (FileDataBlock) yamlRoundTrip("yaml_sfixed", original);

        // Then
        assertFileBlock(original, back);
    }

    /**
     * Given: EXPECTED_FIXED ブロック。
     * When : YAML 経路往復。
     * Then : ファイルタイプ・識別子・レコード・行が一致。
     */
    @Test
    public void yaml_expectedFixed_isPreserved() {
        // Given
        RecordLayout rec = record("data",
                fields(field("f1", "半角英字", "3")),
                rows(row("abc")));
        FileDataBlock original = file(DataType.EXPECTED_FIXED, "", "e.dat",
                directives(),
                Collections.singletonList(rec));

        // When
        FileDataBlock back = (FileDataBlock) yamlRoundTrip("yaml_efixed", original);

        // Then
        assertFileBlock(original, back);
    }

    /**
     * Given: SETUP_VARIABLE ブロック（長さ null）。
     * When : YAML 経路往復。
     * Then : ファイルタイプ・フィールド長（null）・行が一致。
     */
    @Test
    public void yaml_setupVariable_isPreserved() {
        // Given
        RecordLayout rec = record("data",
                fields(field("f1", "半角英字", null)),
                rows(row("${v}")));
        FileDataBlock original = file(DataType.SETUP_VARIABLE, "", "in.csv",
                directives(),
                Collections.singletonList(rec));

        // When
        FileDataBlock back = (FileDataBlock) yamlRoundTrip("yaml_svar", original);

        // Then
        assertFileBlock(original, back);
    }

    /**
     * Given: EXPECTED_VARIABLE ブロック。
     * When : YAML 経路往復。
     * Then : ファイルタイプ・識別子・行が一致。
     */
    @Test
    public void yaml_expectedVariable_isPreserved() {
        // Given
        RecordLayout rec = record("data",
                fields(field("f1", "半角英字", null)),
                rows(row("ok")));
        FileDataBlock original = file(DataType.EXPECTED_VARIABLE, "", "out.csv",
                directives(),
                Collections.singletonList(rec));

        // When
        FileDataBlock back = (FileDataBlock) yamlRoundTrip("yaml_evar", original);

        // Then
        assertFileBlock(original, back);
    }

    /**
     * Given: MESSAGE ブロック（FW ヘッダ・本文含む）。
     * When : YAML 経路往復。
     * Then : 識別子・FW ヘッダ・本文レコードが一致。
     */
    @Test
    public void yaml_message_isPreserved() {
        // Given
        RecordLayout body = record("body",
                fields(field("m1", "半角英字", "5")),
                rows(row("${b}")));
        MessageDataBlock original = message(DataType.MESSAGE, "", "RM01",
                directives(), fwHeader("requestId", "RM01", "userId", "${u}"),
                Collections.singletonList(body));

        // When
        MessageDataBlock back = (MessageDataBlock) yamlRoundTrip("yaml_msg", original);

        // Then
        assertMessageBlock(original, back);
    }

    /**
     * Given: EXPECTED_REQUEST_HEADER_MESSAGES ブロック（送信系・グループ ID 付き）。
     * When : YAML 経路往復。
     * Then : データタイプ・グループ ID・識別子・フィールド・行が一致（FW ヘッダは空）。
     */
    @Test
    public void yaml_expectedRequestHeaderMessages_isPreserved() {
        // Given
        RecordLayout rec = record(null,
                fields(field("requestId", "半角英字", "20")),
                rows(row("RM01")));
        MessageDataBlock original = message(DataType.EXPECTED_REQUEST_HEADER_MESSAGES,
                "case1", "RM01", directives(), fwHeader(),
                Collections.singletonList(rec));

        // When
        MessageDataBlock back = (MessageDataBlock) yamlRoundTrip("yaml_erh", original);

        // Then
        assertMessageBlock(original, back);
    }

    /**
     * Given: EXPECTED_REQUEST_BODY_MESSAGES ブロック。
     * When : YAML 経路往復。
     * Then : データタイプ・グループ ID・識別子・行が一致。
     */
    @Test
    public void yaml_expectedRequestBodyMessages_isPreserved() {
        // Given
        RecordLayout rec = record(null,
                fields(field("body", "半角英字", "10")),
                rows(row("${body}")));
        MessageDataBlock original = message(DataType.EXPECTED_REQUEST_BODY_MESSAGES,
                "case1", "RM01", directives(), fwHeader(),
                Collections.singletonList(rec));

        // When
        MessageDataBlock back = (MessageDataBlock) yamlRoundTrip("yaml_erb", original);

        // Then
        assertMessageBlock(original, back);
    }

    /**
     * Given: RESPONSE_HEADER_MESSAGES ブロック。
     * When : YAML 経路往復。
     * Then : データタイプ・グループ ID・識別子・行が一致。
     */
    @Test
    public void yaml_responseHeaderMessages_isPreserved() {
        // Given
        RecordLayout rec = record(null,
                fields(field("responseId", "半角英字", "20")),
                rows(row("RES01")));
        MessageDataBlock original = message(DataType.RESPONSE_HEADER_MESSAGES,
                "case1", "RES01", directives(), fwHeader(),
                Collections.singletonList(rec));

        // When
        MessageDataBlock back = (MessageDataBlock) yamlRoundTrip("yaml_rh", original);

        // Then
        assertMessageBlock(original, back);
    }

    /**
     * Given: RESPONSE_BODY_MESSAGES ブロック。
     * When : YAML 経路往復。
     * Then : データタイプ・グループ ID・識別子・行が一致。
     */
    @Test
    public void yaml_responseBodyMessages_isPreserved() {
        // Given
        RecordLayout rec = record(null,
                fields(field("resBody", "半角英字", "5")),
                rows(row("${rb}")));
        MessageDataBlock original = message(DataType.RESPONSE_BODY_MESSAGES,
                "case1", "RES01", directives(), fwHeader(),
                Collections.singletonList(rec));

        // When
        MessageDataBlock back = (MessageDataBlock) yamlRoundTrip("yaml_rb", original);

        // Then
        assertMessageBlock(original, back);
    }

    // ======================================================================
    // 既知の非可逆挙動を固定するテスト（リグレッション検知）
    // ======================================================================

    /**
     * Given: null セルを含むテーブル（XLS 経路・YAML 経路）。
     * When : それぞれの経路で往復。
     * Then : どちらの経路でも null が保持される。
     *
     * <p>
     * XLS 経路は書きで {@code null} 記法のセルへ写し、読みで Java の {@code null} へ戻す
     * （記法⇄値の対称な写像）。かつては読み戻しが文字列 {@code "null"} になり YAML 経路と非対称だったが、
     * 読みに {@code NullInterpreter} を掛けたことで解消している。
     * </p>
     */
    @Test
    public void nullCell_isPreservedInBothPaths() {
        // Given
        TableDataBlock original = table(DataType.SETUP_TABLE_DATA, "", "T",
                cols("V"), rows(row((String) null)));

        // When / Then: XLS: null → null 記法 → null
        TableDataBlock xlsBack = (TableDataBlock) xlsRoundTrip("xls_null", "s", original);
        assertThat(xlsBack.getRows().get(0).get(0), is(nullValue()));

        // When / Then: YAML: null は保持される
        TableDataBlock yamlBack = (TableDataBlock) yamlRoundTrip("yaml_null", original);
        assertThat(yamlBack.getRows().get(0).get(0), is(nullValue()));
    }

    /**
     * Given: 前後空白を含む値（XLS・YAML 両経路）。
     * When : 往復。
     * Then : 前後空白が脱落しない（両経路ともクォート処理が正しく機能している）。
     */
    @Test
    public void leadingTrailingWhitespace_isPreservedInBothPaths() {
        // Given
        TableDataBlock original = table(DataType.SETUP_TABLE_DATA, "", "WS",
                cols("V"), rows(row(" lead"), row("trail "), row(" both ")));

        // When / Then: XLS 経路
        TableDataBlock xlsBack = (TableDataBlock) xlsRoundTrip("xls_ws", "s", original);
        assertThat(xlsBack.getRows().get(0), is(row(" lead")));
        assertThat(xlsBack.getRows().get(1), is(row("trail ")));
        assertThat(xlsBack.getRows().get(2), is(row(" both ")));

        // When / Then: YAML 経路
        TableDataBlock yamlBack = (TableDataBlock) yamlRoundTrip("yaml_ws", original);
        assertThat(yamlBack.getRows().get(0), is(row(" lead")));
        assertThat(yamlBack.getRows().get(1), is(row("trail ")));
        assertThat(yamlBack.getRows().get(2), is(row(" both ")));
    }

    /**
     * Given: 特殊記法 {@code ${...}} を含む値（XLS・YAML 両経路）。
     * When : 往復。
     * Then : 記法が展開されずそのまま保持される（IN 値未加工の実証）。
     */
    @Test
    public void specialNotation_isPreservedInBothPaths() {
        // Given
        TableDataBlock original = table(DataType.SETUP_TABLE_DATA, "", "T",
                cols("V"), rows(row("${user.id}"), row("${config.value}")));

        // When / Then: XLS 経路
        TableDataBlock xlsBack = (TableDataBlock) xlsRoundTrip("xls_special", "s", original);
        assertThat(xlsBack.getRows().get(0), is(row("${user.id}")));
        assertThat(xlsBack.getRows().get(1), is(row("${config.value}")));

        // When / Then: YAML 経路
        TableDataBlock yamlBack = (TableDataBlock) yamlRoundTrip("yaml_special", original);
        assertThat(yamlBack.getRows().get(0), is(row("${user.id}")));
        assertThat(yamlBack.getRows().get(1), is(row("${config.value}")));
    }

    // ======================================================================
    // ヘルパー — 往復実行
    // ======================================================================

    /**
     * XLS 往復: 中間モデル → Excel → 中間モデル。各往復テストにはブック名が一意（本体パーサのキャッシュ衝突回避）。
     */
    private TestDataBlock xlsRoundTrip(String book, String sheet, TestDataBlock block) {
        TestDataContainer container = container(book, sheet, block);
        new XlsFormatWriter().write(container, folder.getRoot().getAbsolutePath());
        TestDataContainer back = new XlsFormatReader()
                .read(folder.getRoot().getAbsolutePath(), book + "/" + sheet);
        assertThat(back.getSections().size(), is(1));
        assertThat(back.getSections().get(0).getBlocks().size(), is(1));
        return back.getSections().get(0).getBlocks().get(0);
    }

    /**
     * YAML 往復: 中間モデル → YAML → 中間モデル。セクション名を resource 名として使う。
     */
    private TestDataBlock yamlRoundTrip(String resource, TestDataBlock block) {
        TestDataContainer container = container(resource, resource, block);
        new YamlFormatWriter().write(container, folder.getRoot().getAbsolutePath());
        TestDataContainer back = new YamlFormatReader()
                .read(folder.getRoot().getAbsolutePath(), resource);
        assertThat(back.getSections().size(), is(1));
        assertThat(back.getSections().get(0).getBlocks().size(), is(1));
        return back.getSections().get(0).getBlocks().get(0);
    }

    // ======================================================================
    // ヘルパー — アサーション
    // ======================================================================

    private static void assertTableBlock(TableDataBlock expected, TestDataBlock actual) {
        assertThat(actual, instanceOf(TableDataBlock.class));
        TableDataBlock t = (TableDataBlock) actual;
        assertThat(t.getDataType(), is(expected.getDataType()));
        assertThat(t.getGroupId(), is(expected.getGroupId()));
        assertThat(t.getIdentifier(), is(expected.getIdentifier()));
        assertThat(t.getColumnNames(), is(expected.getColumnNames()));
        assertThat(t.getRows(), is(expected.getRows()));
    }

    private static void assertFileBlock(FileDataBlock expected, TestDataBlock actual) {
        assertThat(actual, instanceOf(FileDataBlock.class));
        FileDataBlock f = (FileDataBlock) actual;
        assertThat(f.getDataType(), is(expected.getDataType()));
        assertThat(f.getGroupId(), is(expected.getGroupId()));
        assertThat(f.getIdentifier(), is(expected.getIdentifier()));
        assertThat(f.getFileType(), is(expected.getFileType()));
        // NTF ボディは file-type・field-separator 等を内部ディレクティブとして付与するため
        // 比較は「期待値に明示したキー」のみ（NTF 内部ディレクティブは意味不変の対象外）。
        for (Map.Entry<String, String> e : expected.getDirectives().entrySet()) {
            assertThat("directive: " + e.getKey(), f.getDirectives().get(e.getKey()), is(e.getValue()));
        }
        assertThat(f.getRecords().size(), is(expected.getRecords().size()));
        for (int i = 0; i < expected.getRecords().size(); i++) {
            RecordLayout expRec = expected.getRecords().get(i);
            RecordLayout actRec = f.getRecords().get(i);
            assertThat(actRec.getRecordType(), is(expRec.getRecordType()));
            assertThat(actRec.getFields().size(), is(expRec.getFields().size()));
            for (int j = 0; j < expRec.getFields().size(); j++) {
                FieldDef ef = expRec.getFields().get(j);
                FieldDef af = actRec.getFields().get(j);
                assertThat(af.getName(), is(ef.getName()));
                assertThat(af.getType(), is(ef.getType()));
                assertThat(af.getLength(), is(ef.getLength()));
            }
            assertThat(actRec.getRows(), is(expRec.getRows()));
        }
    }

    private static void assertMessageBlock(MessageDataBlock expected, TestDataBlock actual) {
        assertThat(actual, instanceOf(MessageDataBlock.class));
        MessageDataBlock m = (MessageDataBlock) actual;
        assertThat(m.getDataType(), is(expected.getDataType()));
        assertThat(m.getGroupId(), is(expected.getGroupId()));
        assertThat(m.getIdentifier(), is(expected.getIdentifier()));
        assertThat(m.getFwHeaderFields(), is(expected.getFwHeaderFields()));
        assertThat(m.getRecords().size(), is(expected.getRecords().size()));
        for (int i = 0; i < expected.getRecords().size(); i++) {
            RecordLayout expRec = expected.getRecords().get(i);
            RecordLayout actRec = m.getRecords().get(i);
            assertThat(actRec.getRecordType(), is(expRec.getRecordType()));
            assertThat(actRec.getFields().size(), is(expRec.getFields().size()));
            for (int j = 0; j < expRec.getFields().size(); j++) {
                FieldDef ef = expRec.getFields().get(j);
                FieldDef af = actRec.getFields().get(j);
                assertThat(af.getName(), is(ef.getName()));
                assertThat(af.getType(), is(ef.getType()));
                assertThat(af.getLength(), is(ef.getLength()));
            }
            assertThat(actRec.getRows(), is(expRec.getRows()));
        }
    }

    // ======================================================================
    // ヘルパー — 構築
    // ======================================================================

    private static TestDataContainer container(String book, String sheet, TestDataBlock block) {
        return new TestDataContainer(book,
                Collections.singletonList(new TestDataSection(sheet,
                        Collections.<TestDataBlock>singletonList(block))));
    }

    private static List<String> cols(String... names) {
        return Arrays.asList(names);
    }

    private static List<List<String>> rows(List<String>... rows) {
        return Arrays.asList(rows);
    }

    private static List<String> row(String... cells) {
        return Arrays.asList(cells);
    }

    private static TableDataBlock table(DataType type, String groupId, String id,
            List<String> cols, List<List<String>> rows) {
        return new TableDataBlock(type, groupId, id, cols, rows);
    }

    private static ListMapBlock listMap(String groupId, String id,
            List<String> cols, List<List<String>> rows) {
        return new ListMapBlock(groupId, id, cols, rows);
    }

    private static FileDataBlock file(DataType type, String groupId, String id,
            Map<String, String> directives, List<RecordLayout> records) {
        return new FileDataBlock(type, groupId, id, directives, records);
    }

    private static MessageDataBlock message(DataType type, String groupId, String id,
            Map<String, String> directives, Map<String, String> fwHeader,
            List<RecordLayout> records) {
        return new MessageDataBlock(type, groupId, id, directives, fwHeader, records);
    }

    private static RecordLayout record(String recordType, List<FieldDef> fields,
            List<List<String>> rows) {
        return new RecordLayout(recordType, fields, rows);
    }

    private static List<FieldDef> fields(FieldDef... defs) {
        return Arrays.asList(defs);
    }

    private static FieldDef field(String name, String type, String length) {
        return new FieldDef(name, type, length);
    }

    private static Map<String, String> kvMap(String... kvPairs) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (int i = 0; i < kvPairs.length; i += 2) {
            map.put(kvPairs[i], kvPairs[i + 1]);
        }
        return map;
    }

    private static Map<String, String> directives(String... kvPairs) {
        return kvMap(kvPairs);
    }

    private static Map<String, String> fwHeader(String... kvPairs) {
        return kvMap(kvPairs);
    }
}
