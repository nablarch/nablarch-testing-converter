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
 * {@link DataType#SETUP_VARIABLE}／{@link DataType#EXPECTED_VARIABLE} のいずれかで、それ以外は生成時に拒否する。
 * 識別子はファイルパスを保持する。ディレクティブとレコードレイアウト群（FW_HEADER レコードも含む）を記述順で持つ。
 * </p>
 *
 * <p>
 * 固定長か可変長か（{@link FileType}）は保持せず、データ種別から導出する。NTF 仕様がデータ種別ごとに
 * 固定長／可変長を一意に割り当てているためであり、食い違う組を表現できなくする。
 * </p>
 *
 * <p>
 * 可変長ファイルのフィールド定義はフィールド長を持てない。Excel 記法に可変長のフィールド長行が無く、
 * 書き出す先が無いため、生成時に拒否する。
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
     * （いずれも {@code $defs.file_data}）に対応する 4 種。
     */
    private static final Set<DataType> PERMITTED_TYPES = EnumSet.of(
            DataType.SETUP_FIXED, DataType.EXPECTED_FIXED,
            DataType.SETUP_VARIABLE, DataType.EXPECTED_VARIABLE);

    private final Map<String, String> directives;
    private final List<RecordLayout> records;

    /**
     * コンストラクタ。
     *
     * <p>
     * 固定長系ではすべてのフィールド定義がフィールド長を持たなければならず、可変長系ではひとつも持ってはならない。
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
     * 対応が定まっているのはファイル系の 4 種だけであり、それ以外と {@code null} は拒否する。
     * </p>
     *
     * @param dataType データ種別（{@code null} 不可）
     * @return 固定長系（SETUP_FIXED ／ EXPECTED_FIXED）なら {@link FileType#FIXED}、
     *         可変長系（SETUP_VARIABLE ／ EXPECTED_VARIABLE）なら {@link FileType#VARIABLE}
     * @throws IllegalArgumentException {@code dataType} が {@code null} またはファイル系の 4 種でない場合
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
