package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;

import java.util.List;
import java.util.Map;

/**
 * 固定長／可変長ファイルのデータブロック。
 *
 * <p>
 * データ種別は {@link DataType#SETUP_FIXED}／{@link DataType#EXPECTED_FIXED}／
 * {@link DataType#SETUP_VARIABLE}／{@link DataType#EXPECTED_VARIABLE} のいずれか。
 * 識別子はファイルパスを保持する。固定長か可変長かは SETUP／EXPECTED を問わず {@link FileType} で区別する。
 * ディレクティブとレコードレイアウト群（FW_HEADER レコードもスキップせず保持）を記述順で持つ。
 * </p>
 *
 * <p>
 * <b>{@code fileType} は必須であり {@code null} であってはならない。</b>Excel 記法・YAML 記法のいずれも
 * 固定長でも可変長でもないファイルデータブロックを認めていないためである（Excel は
 * {@code testdata_notation.rst:883}（{@code 30a8271} 時点）が「固定長ファイルと可変長ファイルには、
 * それぞれ固有の記法制約がある」と定め、固定長では「フィールド名称・データ型・フィールド長の3リストが
 * 同サイズで必須」・可変長では「フィールド名称・データ型の2リストが同サイズで必須であり、フィールド長は
 * 不要」と、記法をこの 2 種類に尽くしている。YAML は {@code :1146} が
 * 「{@code setup_files}・{@code expected_files} の各エントリには {@code path}・{@code type}・
 * {@code records} の3キーが必須であり、いずれかを省略するとエラーになる」と定め、本体スキーマ
 * {@code nablarch/test/ntf-testdata-yaml-schema.json} の {@code $defs.file_data} も {@code type} を
 * {@code required} に含めたうえで {@code enum} ＝ {@code ["fixed", "variable"]} に限る）。
 * 中間モデルの契約は 4 辺すべてが表現できる範囲で定める。本クラス自身は検査しないが、書き出し側
 * （{@code XlsFormatWriter} ／ {@code YamlFormatWriter}）が {@code null} を受けたら送出で弾く
 * （{@code coverage/issues.md} <b>XLS-29</b>）。
 * </p>
 *
 * <p>getter が返すコレクションは防御的コピーせず保持参照を返すため、呼び出し側は読み取り専用として扱うこと。</p>
 *
 * @author kiyotis
 */
public final class FileDataBlock extends TestDataBlock {

    /** ファイルデータブロックの種別。SETUP／EXPECTED を問わず固定長か可変長かを区別する。 */
    public enum FileType { FIXED, VARIABLE }

    private final FileType fileType;
    private final Map<String, String> directives;
    private final List<RecordLayout> records;

    /**
     * コンストラクタ。
     *
     * @param dataType   データ種別
     * @param groupId    グループ ID（省略時は空文字）
     * @param identifier ファイルパス
     * @param fileType   固定長／可変長の区別（必須（{@code null} 不可）。{@code null} の検査は書き出し側が行う）
     * @param directives ディレクティブ（キー → 値。記述順を保つため挿入順を維持する Map を渡すこと）
     * @param records    レコードレイアウト群（記述順。FW_HEADER もスキップせず保持）
     */
    public FileDataBlock(DataType dataType, String groupId, String identifier,
                         FileType fileType, Map<String, String> directives, List<RecordLayout> records) {
        super(dataType, groupId, identifier);
        this.fileType = fileType;
        this.directives = directives;
        this.records = records;
    }

    /** @return 固定長／可変長の区別（必須。{@code null} 不可） */
    public FileType getFileType() {
        return fileType;
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
