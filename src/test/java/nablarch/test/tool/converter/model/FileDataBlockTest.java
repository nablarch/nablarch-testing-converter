package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;
import nablarch.test.tool.converter.model.FileDataBlock.FileType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                DataType.SETUP_FIXED, "g1", "test.dat", directives, records);

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
                DataType.EXPECTED_VARIABLE, "", "out.csv",
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
            new FileDataBlock(DataType.SETUP_FIXED, "", "t.dat", null, List.of());
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
            new FileDataBlock(DataType.SETUP_FIXED, "", "t.dat", directives, List.of());
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
            new FileDataBlock(DataType.SETUP_FIXED, "", "t.dat", directives, List.of());
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
                DataType.SETUP_FIXED, "", "t.dat", directives, List.of());

        // Then
        assertThat(sut.getDirectives().get(""), is(""));
    }

    @Test
    public void レコード群がnullのファイルブロックは生成できない() {
        try {
            new FileDataBlock(DataType.SETUP_FIXED, "", "t.dat",
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
            new FileDataBlock(DataType.SETUP_FIXED, "", "t.dat",
                    new LinkedHashMap<>(), records);
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("レコードレイアウトのリスト"));
        }
    }

    /**
     * XLS-44。ファイル種別は<b>データ種別から導出される</b>。
     * <p>
     * 本体スキーマ {@code nablarch/test/ntf-testdata-yaml-schema.json} の
     * {@code $defs.file_data.properties.type} の description が「fixed = 固定長
     * （SETUP_FIXED / EXPECTED_FIXED）、variable = 可変長（SETUP_VARIABLE / EXPECTED_VARIABLE）」と、
     * 4 種のデータ種別それぞれに {@code fixed} ／ {@code variable} のどちらか一方を一意に割り当てている。
     * 解説書も同じで、{@code testdata_notation.rst:850}（{@code 30a8271} 時点）が
     * データタイプそのものに固定長／可変長の別を割り当てている。
     * したがって<b>データ種別が決まればファイル種別は決まる</b>（逆向きは SETUP／EXPECTED の情報が要るため
     * 定まらない。4 対 2 の写像であって全単射ではない）。
     * </p>
     * <p>
     * 導出にしたことで、食い違う組（例: {@code SETUP_FIXED} かつ可変長）は<b>型として表現できない</b>。
     * 以前はコンストラクタが両方を引数で受け取り、食い違う組を検査せずに保持していた
     * （{@code coverage/issues.md} <b>XLS-44</b>）。
     * </p>
     */
    @Test
    public void ファイル種別を4種のデータ種別から導出する() {
        // Given/When/Then: 固定長系 2 種 → FIXED、可変長系 2 種 → VARIABLE
        assertDerivedFileType(DataType.SETUP_FIXED, FileType.FIXED);
        assertDerivedFileType(DataType.EXPECTED_FIXED, FileType.FIXED);
        assertDerivedFileType(DataType.SETUP_VARIABLE, FileType.VARIABLE);
        assertDerivedFileType(DataType.EXPECTED_VARIABLE, FileType.VARIABLE);
    }

    /**
     * XLS-44。導出の受け口である {@link FileDataBlock#fileTypeOf(DataType)} は、
     * <b>ファイル系 4 種以外のデータ種別を拒否する</b>。
     * <p>
     * 導出の対応が定まっているのは {@code SETUP_FIXED} ／ {@code EXPECTED_FIXED}（固定長）と
     * {@code SETUP_VARIABLE} ／ {@code EXPECTED_VARIABLE}（可変長）の 4 種だけであり
     * （本体スキーマ {@code $defs.file_data.properties.type} の description ／
     * {@code testdata_notation.rst:850}（{@code 30a8271} 時点））、
     * それ以外のデータ種別に対する固定長／可変長の別は<b>NTF 仕様に無い</b>。
     * {@code public static} の受け口が黙って {@link FileType#VARIABLE} を返すと、
     * 「不正な状態を型で表現できなくする」という XLS-44 の修正の趣旨が受け口の側で崩れる。
     * 拒否は生成時と同じ検査（{@link TestDataBlock#requireDataTypeOf(Class, Set, DataType)}）による。
     * </p>
     * <p>
     * 主張は<b>ファイル系 4 種の補集合を回す</b>（{@code EnumSet.complementOf}）。1 種だけを代表として
     * 渡すと、通してよい種別が増える向きの誤りを取り逃がす。補集合で回せば
     * {@code DataType} に定数が増えても自動で追随する。ただし補集合が空になるとループが 0 回になり
     * 無条件に緑になるため、<b>要素数そのものも主張する</b>。
     * メッセージは XLS-36 の {@code requireDataTypeOf} のものであることまで主張する
     * （{@code "が取りうるデータ種別="} は同メソッド固有の断片で、{@code fileTypeOf} が独自の
     * メッセージで投げる実装に差し替わると落ちる）。
     * </p>
     */
    @Test
    public void ファイル系でないデータ種別からはファイル種別を導出できない() {
        // Given: ファイル系 4 種を除いた残り全部（DataType 全 14 定数の補集合 ＝ 10 種）
        Set<DataType> notFileTypes = EnumSet.complementOf(EnumSet.of(
                DataType.SETUP_FIXED, DataType.EXPECTED_FIXED,
                DataType.SETUP_VARIABLE, DataType.EXPECTED_VARIABLE));

        // Then: 補集合が空だとループが 0 回になり無条件に緑になるため、要素数そのものを固定する
        assertThat("ファイル系 4 種以外は 10 種であるべき", notFileTypes.size(), is(10));

        // When / Then: どれを渡しても拒否され、メッセージにデータ種別名とブロック名が出る
        for (DataType notFileType : notFileTypes) {
            try {
                FileDataBlock.fileTypeOf(notFileType);
                fail("IllegalArgumentException が送出されるべき: " + notFileType);
            } catch (IllegalArgumentException e) {
                assertThat("データ種別名がメッセージに出るべき: " + notFileType,
                        e.getMessage(), containsString(notFileType.getName()));
                assertThat("ブロック名がメッセージに出るべき: " + notFileType,
                        e.getMessage(), containsString("FileDataBlock"));
                // 生成時と同じ検査（TestDataBlock#requireDataTypeOf）による拒否であること
                assertThat("XLS-36 の検査によるメッセージであるべき: " + notFileType,
                        e.getMessage(), containsString("が取りうるデータ種別="));
            }
        }
    }

    /**
     * XLS-44 ／ XLS-34。導出の受け口である {@link FileDataBlock#fileTypeOf(DataType)} は、
     * <b>{@code null} を {@link IllegalArgumentException} で拒否する</b>。
     * <p>
     * データブロックは必ず 1 つのデータタイプを持つ（Excel 形式はマーカー、YAML 形式は
     * トップレベルキーがデータタイプから決まるため、<b>データタイプの無いブロックはどちらの形式でも
     * 書けない</b> —— {@code coverage/issues.md} <b>XLS-34</b>）。{@code null} に対する答えは
     * そこで確定しており、{@link TestDataBlock} のコンストラクタは同じ入力を
     * {@link IllegalArgumentException} で拒否している。<b>受け口によって
     * {@link NullPointerException} と {@link IllegalArgumentException} に分かれてよい理由は無い。</b>
     * </p>
     * <p>
     * 主張は例外の種類とメッセージの趣旨の両方に掛ける。種類だけを主張すると、メッセージ組み立ての
     * {@code dataType.getName()} が投げる {@link NullPointerException} を
     * {@link IllegalArgumentException} で包み直す実装（趣旨の異なるメッセージになる）が通ってしまう。
     * </p>
     */
    @Test
    public void データ種別がnullではファイル種別を導出できない() {
        // When / Then: NullPointerException ではなく IllegalArgumentException で落ちる
        try {
            FileDataBlock.fileTypeOf(null);
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat("データ種別が null である旨がメッセージに出るべき",
                    e.getMessage(), containsString("データ種別が null"));
            // XLS-34 の趣旨（データタイプの無いブロックはどちらの形式でも書けない）であること
            assertThat("XLS-34 の趣旨がメッセージに出るべき",
                    e.getMessage(), containsString("どちらの形式でも書けません"));
            assertThat("ブロック名がメッセージに出るべき",
                    e.getMessage(), containsString("FileDataBlock"));
        }
    }

    private static void assertDerivedFileType(DataType dataType, FileType expected) {
        // 静的受け口の受理側を直接主張する（コンストラクタ経由の間接担保に頼らない）
        assertThat(FileDataBlock.fileTypeOf(dataType), is(expected));
        FileDataBlock sut = new FileDataBlock(
                dataType, "", "f", new LinkedHashMap<>(), List.of());
        assertThat(sut.getDataType(), is(dataType));
        assertThat(sut.getFileType(), is(expected));
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
                DataType.SETUP_FIXED, "", "msg.dat", new LinkedHashMap<>(), records);

        // Then: FW_HEADER も保持され、レコード順は崩れない
        assertThat(sut.getRecords().size(), is(2));
        assertThat(sut.getRecords().get(0).getRecordType(), is("FW_HEADER"));
        assertThat(sut.getRecords().get(1).getRecordType(), is("data"));
    }

    /**
     * XLS-30。固定長ファイルでフィールド長が {@code null} のブロックは生成できない。
     * {@code testdata_notation.rst:883}（{@code 30a8271} 時点）は固定長ファイルについて
     * 「フィールド名称・データ型・フィールド長の3リストが同サイズで必須であり」と定め（逐語）、
     * {@code :889} は記述時エラーとして「フィールド名称・データ型・フィールド長リストのサイズが
     * 一致していない」を挙げる。長さを持たないフィールドは 4 辺のどこにも記法どおりには書き出せない。
     */
    @Test
    public void 固定長ファイルでフィールド長がnullのフィールド定義は保持できない() {
        // Given: 長さの無いフィールドを 1 件持つレコード
        List<RecordLayout> records = List.of(
                new RecordLayout("data", List.of(new FieldDef("id", "数値", null)), List.of()));

        // When / Then
        try {
            new FileDataBlock(DataType.SETUP_FIXED, "", "t.dat", new LinkedHashMap<>(), records);
            fail("IllegalArgumentException が送出されるべき");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("フィールド長を持たないフィールド定義は保持できません"));
        }
    }

    /**
     * XLS-44 ／ XLS-30。フィールド長の検査は<b>データ種別を起点に</b>走る。
     * <p>
     * 導出前は末尾の検査が {@code fileType} だけを見ていたため、データ種別 ＝ {@code SETUP_FIXED} でも
     * 引数の {@code fileType} に {@code VARIABLE} を渡せば XLS-30 の番人が走らなかった
     * （{@code coverage/issues.md} <b>XLS-44</b>「既存の不変条件も素通りする」。実測 2026-08-21）。
     * 導出後はファイル種別を外から渡せないため、固定長系のデータ種別なら必ず走る。
     * </p>
     */
    @Test
    public void フィールド長がnullのフィールド定義は固定長系のデータ種別すべてで拒否される() {
        // Given: 長さの無いフィールドを 1 件持つレコード
        List<RecordLayout> records = List.of(
                new RecordLayout("data", List.of(new FieldDef("id", "数値", null)), List.of()));

        // When / Then: 固定長系 2 種はいずれも拒否される
        for (DataType fixedType : List.of(DataType.SETUP_FIXED, DataType.EXPECTED_FIXED)) {
            try {
                new FileDataBlock(fixedType, "", "t.dat", new LinkedHashMap<>(), records);
                fail("IllegalArgumentException が送出されるべき: " + fixedType);
            } catch (IllegalArgumentException e) {
                assertThat(e.getMessage(), containsString("フィールド長を持たないフィールド定義は保持できません"));
            }
        }

        // Then: 可変長系はフィールド長を要求しないため保持できる（記法どおり）
        for (DataType variableType : List.of(DataType.SETUP_VARIABLE, DataType.EXPECTED_VARIABLE)) {
            FileDataBlock sut = new FileDataBlock(variableType, "", "t.csv", new LinkedHashMap<>(), records);
            assertThat(sut.getFileType(), is(FileType.VARIABLE));
            assertThat(sut.getRecords(), is(records));
        }
    }

    /**
     * XLS-30。<b>可変長ファイルではフィールド長 {@code null} が正しい</b>ため拒否しない
     * （{@code testdata_notation.rst:883}「可変長ファイルでは、フィールド名称・データ型の2リストが
     * 同サイズで必須であり、フィールド長は不要である」）。
     */
    @Test
    public void 可変長ファイルはフィールド長がnullでも生成できる() {
        // Given / When
        FileDataBlock sut = new FileDataBlock(
                DataType.SETUP_VARIABLE, "", "t.csv", new LinkedHashMap<>(),
                List.of(new RecordLayout(null, List.of(new FieldDef("id", "数値", null)), List.of())));

        // Then
        assertThat(sut.getRecords().get(0).getFields().get(0).getLength(), is(nullValue()));
    }

    @Test
    public void FileType列挙は固定長と可変長の2種() {
        // When / Then: FileType 列挙値は FIXED / VARIABLE のみ
        assertThat(FileType.values().length, is(2));
        assertThat(FileType.valueOf("FIXED"), is(FileType.FIXED));
        assertThat(FileType.valueOf("VARIABLE"), is(FileType.VARIABLE));
    }
}
