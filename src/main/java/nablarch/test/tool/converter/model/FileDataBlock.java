package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 固定長／可変長ファイルのデータブロック。
 *
 * <p>
 * データ種別は {@link DataType#SETUP_FIXED}／{@link DataType#EXPECTED_FIXED}／
 * {@link DataType#SETUP_VARIABLE}／{@link DataType#EXPECTED_VARIABLE} のいずれか。
 * 識別子はファイルパスを保持する。固定長か可変長かは SETUP／EXPECTED を問わず {@link FileType} で区別し、
 * その値は<b>データ種別から導出する</b>。
 * ディレクティブとレコードレイアウト群（FW_HEADER レコードもスキップせず保持）を記述順で持つ。
 * <b>この 4 種以外のデータ種別では生成できない。生成時点で拒否する</b>
 * （{@code coverage/issues.md} <b>XLS-36</b>。根拠は
 * {@link TestDataBlock#requireDataTypeOf(Class, Set, DataType)} の Javadoc）。
 * </p>
 *
 * <p>
 * <b>{@code fileType} は保持せず、{@code dataType} から導出する</b>
 * （{@code coverage/issues.md} <b>XLS-44</b>）。NTF 仕様はファイル種別をひとつの概念としてしか持たず、
 * 4 種のデータ種別それぞれに固定長／可変長のどちらか一方を一意に割り当てている —— 本体スキーマ
 * {@code nablarch/test/ntf-testdata-yaml-schema.json} の {@code $defs.file_data.properties.type} の
 * description が「fixed = 固定長（SETUP_FIXED / EXPECTED_FIXED）、variable = 可変長
 * （SETUP_VARIABLE / EXPECTED_VARIABLE）」と定め、記法
 * も固定長ファイル・可変長ファイルに対応するテストデータ（ファイルデータ）は、
 * SETUP_FIXED・EXPECTED_FIXED（固定長）、SETUP_VARIABLE・EXPECTED_VARIABLE（可変長）の
 * いずれかのデータタイプで記述する。と、データタイプそのものに固定長／可変長の別を割り当てている。
 * <b>したがってデータ種別が決まればファイル種別は決まる</b>（逆向きは SETUP／EXPECTED の情報が要るため
 * 定まらない。4 対 2 の写像であって全単射ではない）。
 * </p>
 *
 * <p>
 * <b>導出にしたことで、食い違う組（例: {@code SETUP_FIXED} かつ可変長）は型として表現できない。</b>
 * 以前は {@code dataType} と {@code fileType} を別々に引数で受け取り、食い違う組を検査せずに保持していた
 * ため、同じモデルから辺③と辺④が別のファイル種別を書いた（{@code coverage/issues.md} <b>XLS-44</b>）。
 * <b>これに伴い {@code fileType} ＝ {@code null} を拒否していた番人（XLS-29）は到達不能になり撤去した。</b>
 * 退行ではなく、不正な状態を型が表現できなくなったため番人が不要になったものである
 * （{@code steering.md} Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」の一段強い形）。
 * <b>{@link FileType} と {@link #getFileType()} は残す</b> —— 消えたのは概念ではなく二つ目の真実の
 * 置き場であり、NTF 仕様はファイル種別を名前つきの 2 値として持つ（YAML スキーマの {@code type} は
 * {@code required} かつ {@code enum} ＝ {@code ["fixed", "variable"]}、Excel は
 * 記法が記法を固定長ファイルと可変長ファイルの 2 種類に尽くしている）。
 * </p>
 *
 * <p>
 * <b>可変長ファイルではフィールド定義がフィールド長を持てない。生成時点で拒否する</b>
 * （{@code coverage/issues.md} <b>XLS-45</b>。ユーザー確定・2026-08-24）。
 * Excel 記法に可変長のフィールド長行が無いためである（固定長との違いは、可変長ファイルの場合はフィールド長行を記載しない
 * 点のみである。／可変長ファイルでは、フィールド名称・データ型の2リストが
 * 同サイズで必須であり、フィールド長は不要である。）。
 * <b>保持できると辺③（中間モデル → Excel）で例外にも警告にもならずに落ちる</b>ため、
 * 書き出し側に番人を置くのではなく、そもそも作れなくする
 * （{@code steering.md} Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」）。
 * 拒否の詳細（{@code "-"} も空文字も弾くこと、その根拠）は
 * {@link ModelPreconditions#requireNoLengths} の Javadoc にある。
 * </p>
 *
 * <p>getter が返すコレクションは防御的コピーせず保持参照を返すため、呼び出し側は読み取り専用として扱うこと。</p>
 *
 * @author kiyotis
 */
public final class FileDataBlock extends TestDataBlock {

    /** ファイルデータブロックの種別。SETUP／EXPECTED を問わず固定長か可変長かを区別する。 */
    public enum FileType { FIXED, VARIABLE }

    /**
     * 取りうるデータ種別。YAML のトップレベルキー {@code setup_files} ／ {@code expected_files}
     * （いずれも {@code $defs.file_data}）に対応する 4 種
     * 。
     */
    private static final Set<DataType> PERMITTED_TYPES = EnumSet.of(
            DataType.SETUP_FIXED, DataType.EXPECTED_FIXED,
            DataType.SETUP_VARIABLE, DataType.EXPECTED_VARIABLE);

    private final Map<String, String> directives;
    private final List<RecordLayout> records;

    /**
     * コンストラクタ。
     *
     * <p>固定長／可変長の区別は {@code dataType} から導出する（{@link #fileTypeOf(DataType)}）。</p>
     *
     * <p>
     * <b>フィールド長の不変条件は導出したファイル種別で分かれる。</b>固定長系ではすべてのフィールド定義が
     * フィールド長を持たなければならず（{@link ModelPreconditions#requireLengths}。
     * {@code coverage/issues.md} <b>XLS-30</b>）、<b>可変長系ではひとつも持ってはならない</b>
     * （{@link ModelPreconditions#requireNoLengths}。{@code coverage/issues.md} <b>XLS-45</b>）。
     * </p>
     *
     * @param dataType   データ種別
     * @param groupId    グループ ID（省略時は空文字）
     * @param identifier ファイルパス
     * @param directives ディレクティブ（キー → 値。記述順を保つため挿入順を維持する Map を渡すこと）
     * @param records    レコードレイアウト群（記述順。FW_HEADER もスキップせず保持）
     * @throws IllegalArgumentException {@code dataType} が SETUP_FIXED ／ EXPECTED_FIXED ／
     *                                  SETUP_VARIABLE ／ EXPECTED_VARIABLE のいずれでもない場合、
     *                                  固定長系でフィールド長を持たないフィールド定義がある場合、
     *                                  または可変長系でフィールド長を持つフィールド定義がある場合
     */
    public FileDataBlock(DataType dataType, String groupId, String identifier,
                         Map<String, String> directives, List<RecordLayout> records) {
        super(dataType, groupId, identifier);
        requireDataTypeOf(FileDataBlock.class, PERMITTED_TYPES, dataType);
        this.directives = ModelPreconditions.requireNoNulls("ディレクティブ", directives);
        this.records = ModelPreconditions.requireNoNulls("レコードレイアウトのリスト", records);
        if (getFileType() == FileType.FIXED) {
            ModelPreconditions.requireLengths(this.records, identifier);
        } else {
            ModelPreconditions.requireNoLengths(this.records, identifier);
        }
    }

    /**
     * ファイル系データ種別から固定長／可変長の区別を導出する。
     *
     * <p>
     * 対応は本体スキーマ {@code $defs.file_data.properties.type} の description
     * （fixed ＝ SETUP_FIXED ／ EXPECTED_FIXED、variable ＝ SETUP_VARIABLE ／ EXPECTED_VARIABLE）と
     * 記法による。
     * </p>
     *
     * <p>
     * <b>導出の対応が定まっているのはこの 4 種だけであり、それ以外のデータ種別は受け口で拒否する</b>
     * （{@code coverage/issues.md} <b>XLS-44</b>）。検査はコンストラクタと同じ
     * {@link TestDataBlock#requireDataTypeOf(Class, Set, DataType)} による
     * （コンストラクタ経由では二重に走るが、それでよい）。<b>{@code null} も同じ検査が
     * {@link IllegalArgumentException} で拒否する</b> —— データタイプの無いブロックはどちらの形式でも
     * 書けず（{@code coverage/issues.md} <b>XLS-34</b>）、{@link TestDataBlock} のコンストラクタが
     * 同じ入力を拒否しているため、<b>受け口によって例外の種類が分かれる状態は残さない</b>。
     * </p>
     *
     * @param dataType データ種別（{@code null} 不可）
     * @return 固定長系（SETUP_FIXED ／ EXPECTED_FIXED）なら {@link FileType#FIXED}、
     *         可変長系（SETUP_VARIABLE ／ EXPECTED_VARIABLE）なら {@link FileType#VARIABLE}
     * @throws IllegalArgumentException {@code dataType} が {@code null} の場合、または SETUP_FIXED ／
     *                                  EXPECTED_FIXED ／ SETUP_VARIABLE ／ EXPECTED_VARIABLE の
     *                                  いずれでもない場合
     */
    public static FileType fileTypeOf(DataType dataType) {
        requireDataTypeOf(FileDataBlock.class, PERMITTED_TYPES, dataType);
        return dataType == DataType.SETUP_FIXED || dataType == DataType.EXPECTED_FIXED
                ? FileType.FIXED : FileType.VARIABLE;
    }

    /** @return 固定長／可変長の区別（データ種別から導出した値） */
    public FileType getFileType() {
        return fileTypeOf(getDataType());
    }

    /** @return ディレクティブ（記述順） */
    public Map<String, String> getDirectives() {
        return directives;
    }

    /** @return レコードレイアウト群（記述順） */
    public List<RecordLayout> getRecords() {
        return records;
    }
}
