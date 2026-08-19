package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link MessageDataBlock} のテスト。
 *
 * <p>ディレクティブ・FW 制御ヘッダ・本文レコードを順序保持し、FW ヘッダ無し経路を空 Map で表せることを検証する。</p>
 */
public class MessageDataBlockTest {

    @Test
    public void メッセージのディレクティブとFWヘッダと本文を順序保持する() {
        // Given: messages 経路。fw_header マップと本文レコード
        Map<String, String> directives = new LinkedHashMap<>();
        directives.put("text-encoding", "UTF-8");
        Map<String, String> fwHeader = new LinkedHashMap<>();
        fwHeader.put("requestId", "RM11AC0101");
        fwHeader.put("userId", "${userId}");
        List<RecordLayout> records = List.of(
                new RecordLayout(null, List.of(new FieldDef("data", "半角英字", "-")), List.of(List.of("X"))));

        // When
        MessageDataBlock sut = new MessageDataBlock(
                DataType.MESSAGE, "g1", "RM11AC0101", directives, fwHeader, records);

        // Then: 値は未加工（${userId} もそのまま）、順序保持
        assertThat(sut.getDataType(), is(DataType.MESSAGE));
        assertThat(sut.getGroupId(), is("g1"));
        assertThat(sut.getIdentifier(), is("RM11AC0101"));
        assertThat(new ArrayList<>(sut.getDirectives().keySet()), is(List.of("text-encoding")));
        assertThat(new ArrayList<>(sut.getFwHeaderFields().keySet()), is(List.of("requestId", "userId")));
        assertThat(sut.getFwHeaderFields().get("userId"), is("${userId}"));
        assertThat(sut.getRecords(), is(records));
    }

    @Test
    public void ディレクティブがnullの電文ブロックは生成できない() {
        // Given: 「無い」ことは空 Map で表す（XLS-38）
        try {
            new MessageDataBlock(DataType.MESSAGE, "", "RM11AC0101",
                    null, new LinkedHashMap<>(), List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("ディレクティブ"));
        }
    }

    @Test
    public void FW制御ヘッダがnullの電文ブロックは生成できない() {
        try {
            new MessageDataBlock(DataType.MESSAGE, "", "RM11AC0101",
                    new LinkedHashMap<>(), null, List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("FW 制御ヘッダフィールド"));
        }
    }

    /**
     * XLS-43。ディレクティブのキー・値が {@code null} の電文ブロックは生成できない。
     * 根拠は {@code testdata_notation.rst:906}・{@code :892} と本体スキーマ {@code $defs.directives}。
     */
    @Test
    public void ディレクティブのキーまたは値がnullの電文ブロックは生成できない() {
        // Given: キーが null
        Map<String, String> nullKey = new LinkedHashMap<>();
        nullKey.put(null, "UTF-8");
        try {
            new MessageDataBlock(DataType.MESSAGE, "", "RM11AC0101",
                    nullKey, new LinkedHashMap<>(), List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("ディレクティブのキーに null は指定できません"));
        }

        // Given: 値が null
        Map<String, String> nullValue = new LinkedHashMap<>();
        nullValue.put("text-encoding", null);
        try {
            new MessageDataBlock(DataType.MESSAGE, "", "RM11AC0101",
                    nullValue, new LinkedHashMap<>(), List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("ディレクティブ \"text-encoding\" の値が null です"));
        }
    }

    /**
     * XLS-43。FW 制御ヘッダも同じ「名前・値」形式で記述するため（{@code testdata_notation.rst:1267}）、
     * キー・値の {@code null} を同様に拒否する。
     */
    @Test
    public void FW制御ヘッダのキーまたは値がnullの電文ブロックは生成できない() {
        // Given: キーが null
        Map<String, String> nullKey = new LinkedHashMap<>();
        nullKey.put(null, "RM11AC0101");
        try {
            new MessageDataBlock(DataType.MESSAGE, "", "RM11AC0101",
                    new LinkedHashMap<>(), nullKey, List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("FW 制御ヘッダフィールドのキーに null は指定できません"));
        }

        // Given: 値が null
        Map<String, String> nullValue = new LinkedHashMap<>();
        nullValue.put("requestId", null);
        try {
            new MessageDataBlock(DataType.MESSAGE, "", "RM11AC0101",
                    new LinkedHashMap<>(), nullValue, List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("FW 制御ヘッダフィールド \"requestId\" の値が null です"));
        }
    }

    @Test
    public void レコード群がnullの電文ブロックは生成できない() {
        try {
            new MessageDataBlock(DataType.MESSAGE, "", "RM11AC0101",
                    new LinkedHashMap<>(), new LinkedHashMap<>(), null);
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("レコードレイアウトのリスト"));
        }
    }

    @Test
    public void FWヘッダ非使用経路は空Mapで表しディレクティブも空で保持する() {
        // Given/When: expected_request_body_messages 等は fw_header を読まない＝空 Map
        MessageDataBlock sut = new MessageDataBlock(
                DataType.EXPECTED_REQUEST_BODY_MESSAGES, "", "RM11AC0101",
                new LinkedHashMap<>(), new LinkedHashMap<>(),
                List.of(new RecordLayout(null, List.of(new FieldDef("f", "半角英字", "1")), List.of(List.of("v")))));

        // Then: 各コレクションが空であること
        assertThat(sut.getFwHeaderFields().isEmpty(), is(true));
        assertThat(sut.getDirectives().isEmpty(), is(true));
        assertThat(sut.getDataType(), is(DataType.EXPECTED_REQUEST_BODY_MESSAGES));
    }

    @Test
    public void 契約違反のレコード0件もモデル自身は検査せず保持する() {
        // Given: records は 1 件以上が契約だが、番人は書き出し側（XlsFormatWriter／YamlFormatWriter）に置く
        // When
        MessageDataBlock sut = new MessageDataBlock(
                DataType.MESSAGE, "", "RM11AC0101",
                new LinkedHashMap<>(), new LinkedHashMap<>(), List.of());

        // Then: 中間モデルは受けた値をそのまま保持する（送出はしない）
        assertThat(sut.getRecords().isEmpty(), is(true));
    }

    @Test
    public void メッセージ系の全データ種別を保持する() {
        // Given: 電文が取りうる 5 種別すべて
        for (DataType dt : List.of(
                DataType.MESSAGE,
                DataType.EXPECTED_REQUEST_HEADER_MESSAGES,
                DataType.EXPECTED_REQUEST_BODY_MESSAGES,
                DataType.RESPONSE_HEADER_MESSAGES,
                DataType.RESPONSE_BODY_MESSAGES)) {
            // When
            MessageDataBlock sut = new MessageDataBlock(
                    dt, "", "RM11", new LinkedHashMap<>(), new LinkedHashMap<>(), List.of());
            // Then
            assertThat(sut.getDataType(), is(dt));
        }
    }

    @Test
    public void 複数の本文レコードを記述順で保持する() {
        // Given: ヘッダレコードと本文レコードの 2 つ
        RecordLayout header = new RecordLayout(
                "FW_HEADER", List.of(new FieldDef("requestId", "半角英字", "10")), List.of(List.of("RM11")));
        RecordLayout body = new RecordLayout(
                "data", List.of(new FieldDef("amount", "数値", "5")), List.of(List.of("100")));
        List<RecordLayout> records = List.of(header, body);

        // When
        MessageDataBlock sut = new MessageDataBlock(
                DataType.MESSAGE, "", "RM11", new LinkedHashMap<>(), new LinkedHashMap<>(), records);

        // Then: レコード順は崩れない
        assertThat(sut.getRecords().size(), is(2));
        assertThat(sut.getRecords().get(0).getRecordType(), is("FW_HEADER"));
        assertThat(sut.getRecords().get(1).getRecordType(), is("data"));
    }
}
