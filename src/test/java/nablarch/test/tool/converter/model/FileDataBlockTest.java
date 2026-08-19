package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;
import nablarch.test.tool.converter.model.FileDataBlock.FileType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link FileDataBlock} のテスト。
 *
 * <p>固定長／可変長の区別・ディレクティブ順序・レコードレイアウト群の保持を検証する。</p>
 */
public class FileDataBlockTest {

    @Test
    public void 固定長ファイルのディレクティブとレコードを順序保持する() {
        // Given: ディレクティブは記述順を保つ（LinkedHashMap）
        Map<String, String> directives = new LinkedHashMap<>();
        directives.put("file-type", "Fixed");
        directives.put("text-encoding", "MS932");
        List<RecordLayout> records = List.of(
                new RecordLayout("data", List.of(new FieldDef("id", "数値", "5")), List.of(List.of("1"))));

        // When
        FileDataBlock sut = new FileDataBlock(
                DataType.SETUP_FIXED, "g1", "test.dat", FileType.FIXED, directives, records);

        // Then
        assertThat(sut.getDataType(), is(DataType.SETUP_FIXED));
        assertThat(sut.getGroupId(), is("g1"));
        assertThat(sut.getIdentifier(), is("test.dat"));
        assertThat(sut.getFileType(), is(FileType.FIXED));
        assertThat(new ArrayList<>(sut.getDirectives().keySet()), is(List.of("file-type", "text-encoding")));
        assertThat(sut.getRecords(), is(records));
    }

    @Test
    public void 可変長ファイルの種別を区別し空のディレクティブとレコードを保持する() {
        // Given/When: 可変長・EXPECTED。ディレクティブもレコードも空
        FileDataBlock sut = new FileDataBlock(
                DataType.EXPECTED_VARIABLE, "", "out.csv", FileType.VARIABLE,
                new LinkedHashMap<>(), List.of());

        // Then
        assertThat(sut.getFileType(), is(FileType.VARIABLE));
        assertThat(sut.getDataType(), is(DataType.EXPECTED_VARIABLE));
        assertThat(sut.getDirectives().isEmpty(), is(true));
        assertThat(sut.getRecords().isEmpty(), is(true));
    }

    @Test
    public void ディレクティブがnullのファイルブロックは生成できない() {
        // Given: ディレクティブなしは空 Map で表す（XLS-38）
        try {
            new FileDataBlock(DataType.SETUP_FIXED, "", "t.dat", FileType.FIXED, null, List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("ディレクティブ"));
        }
    }

    /**
     * XLS-43。ディレクティブのキーが {@code null} のブロックは生成できない。
     * 記法は「キー名と値の 2 要素」で記述することを定めており（{@code testdata_notation.rst:906}・{@code :892}）、
     * 本体スキーマ {@code $defs.directives} は {@code additionalProperties: false} でキーを列挙しているため、
     * {@code null} のキーはそもそも書けない。
     */
    @Test
    public void ディレクティブのキーがnullのファイルブロックは生成できない() {
        // Given: null キーを入れられる Map（Map.of は null キーを許さない）
        Map<String, String> directives = new LinkedHashMap<>();
        directives.put(null, "MS932");

        // When / Then
        try {
            new FileDataBlock(DataType.SETUP_FIXED, "", "t.dat", FileType.FIXED, directives, List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("ディレクティブのキーに null は指定できません"));
        }
    }

    /**
     * XLS-43。ディレクティブの値が {@code null} のブロックは生成できない。
     * 本体スキーマ {@code $defs.directives} の値型は string ／ boolean ／ integer だけで、
     * {@code null} を許す定義が 1 つも無い。
     */
    @Test
    public void ディレクティブの値がnullのファイルブロックは生成できない() {
        // Given
        Map<String, String> directives = new LinkedHashMap<>();
        directives.put("text-encoding", null);

        // When / Then
        try {
            new FileDataBlock(DataType.SETUP_FIXED, "", "t.dat", FileType.FIXED, directives, List.of());
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("ディレクティブ \"text-encoding\" の値が null です"));
        }
    }

    /**
     * XLS-43。<b>空文字は拒否しない</b>（空文字を禁じる明文が無いため。ユーザー確定・2026-08-19）。
     */
    @Test
    public void ディレクティブのキーと値が空文字のファイルブロックは生成できる() {
        // Given
        Map<String, String> directives = new LinkedHashMap<>();
        directives.put("", "");

        // When
        FileDataBlock sut = new FileDataBlock(
                DataType.SETUP_FIXED, "", "t.dat", FileType.FIXED, directives, List.of());

        // Then
        assertThat(sut.getDirectives().get(""), is(""));
    }

    @Test
    public void レコード群がnullのファイルブロックは生成できない() {
        try {
            new FileDataBlock(DataType.SETUP_FIXED, "", "t.dat", FileType.FIXED,
                    new LinkedHashMap<>(), null);
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("レコードレイアウトのリスト"));
        }
    }

    @Test
    public void レコード群にnullの要素を含むファイルブロックは生成できない() {
        // Given
        List<RecordLayout> records = Arrays.asList(
                new RecordLayout("data", List.of(new FieldDef("id", "数値", "5")), List.of()), null);
        try {
            new FileDataBlock(DataType.SETUP_FIXED, "", "t.dat", FileType.FIXED,
                    new LinkedHashMap<>(), records);
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("レコードレイアウトのリスト"));
        }
    }

    @Test
    public void 固定可変とSETUP_EXPECTEDの全組合せを保持する() {
        // Given: 4 つのファイル系データ種別と FileType の対角を網羅
        // When/Then
        assertFileBlock(DataType.SETUP_FIXED, FileType.FIXED);
        assertFileBlock(DataType.EXPECTED_FIXED, FileType.FIXED);
        assertFileBlock(DataType.SETUP_VARIABLE, FileType.VARIABLE);
        assertFileBlock(DataType.EXPECTED_VARIABLE, FileType.VARIABLE);
    }

    private static void assertFileBlock(DataType dataType, FileType fileType) {
        FileDataBlock sut = new FileDataBlock(
                dataType, "", "f", fileType, new LinkedHashMap<>(), List.of());
        assertThat(sut.getDataType(), is(dataType));
        assertThat(sut.getFileType(), is(fileType));
    }

    @Test
    public void FWヘッダレコードをスキップせず複数レコードを記述順で保持する() {
        // Given: FW_HEADER レコード + データレコードの 2 つを記述順で保持する
        RecordLayout fwHeader = new RecordLayout(
                "FW_HEADER", List.of(new FieldDef("requestId", "半角英字", "10")), List.of(List.of("RM11")));
        RecordLayout data = new RecordLayout(
                "data", List.of(new FieldDef("body", "全角", "-")), List.of(List.of("あ")));
        List<RecordLayout> records = List.of(fwHeader, data);

        // When
        FileDataBlock sut = new FileDataBlock(
                DataType.SETUP_FIXED, "", "msg.dat", FileType.FIXED, new LinkedHashMap<>(), records);

        // Then: FW_HEADER も保持され、レコード順は崩れない
        assertThat(sut.getRecords().size(), is(2));
        assertThat(sut.getRecords().get(0).getRecordType(), is("FW_HEADER"));
        assertThat(sut.getRecords().get(1).getRecordType(), is("data"));
    }

    @Test
    public void 契約違反のnullファイル種別もモデル自身は検査せず保持する() {
        // Given: fileType は必須（null 不可）だが、番人は書き出し側（XlsFormatWriter／YamlFormatWriter）に置く
        // When
        FileDataBlock sut = new FileDataBlock(
                DataType.SETUP_FIXED, "", "f.dat", null, new LinkedHashMap<>(), List.of());

        // Then: 中間モデルは受けた値をそのまま保持する（送出はしない）
        assertThat(sut.getFileType(), is(nullValue()));
    }

    @Test
    public void FileType列挙は固定長と可変長の2種() {
        // When / Then: FileType 列挙値は FIXED / VARIABLE のみ
        assertThat(FileType.values().length, is(2));
        assertThat(FileType.valueOf("FIXED"), is(FileType.FIXED));
        assertThat(FileType.valueOf("VARIABLE"), is(FileType.VARIABLE));
    }
}
