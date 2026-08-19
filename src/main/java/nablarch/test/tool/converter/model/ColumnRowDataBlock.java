package nablarch.test.tool.converter.model;

import nablarch.test.core.reader.DataType;

import java.util.List;

/**
 * カラム名行とデータ行で表現されるデータブロックの抽象基底。
 *
 * <p>
 * テーブルデータ（{@link TableDataBlock}）と LIST_MAP（{@link ListMapBlock}）の共通基底（sealed）。
 * カラム名リスト（マーカーカラムを含む）とデータ行リストを、加工せず記述順・記法のまま保持する。
 * </p>
 *
 * <p>
 * <b>{@code columnNames} ／ {@code rows} と、その要素（行）は {@code null} を取らない。生成時点で拒否する</b>
 * （{@code coverage/issues.md} <b>XLS-38</b>。根拠は {@link ModelPreconditions} の Javadoc）。
 * <b>行の中のセルの {@code null} は記法にあるため通す。</b>
 * </p>
 *
 * <p>
 * <b>{@code columnNames} の重複は拒否しない。番人も WARN も置かない</b>
 * （ユーザー確定・2026-08-19。{@code coverage/issues.md} <b>XLS-40</b> のカラム名側）。
 * 辺②が重複したカラム名を持つブロックを実際に作るためである —— 本体 {@code TableData} が
 * テーブル系のカラム名を大文字化するので、{@code id} と {@code ID} を書いたスキーマ適合の YAML が
 * {@code columnNames} ＝ {@code [ID, ID]} になる（<b>YML-10</b>。担保テストは
 * {@code YamlFormatReaderInvalidInputTest#dropsValueWhenTableColumnNamesDifferOnlyByCase}）。
 * 生成時に拒否すると仕様適合入力を変換できなくなるため、番人は置けない。
 * </p>
 *
 * <p>
 * <b>帰属は converter の外（nablarch-testing の {@code TableData}）である。</b>
 * あるべき姿は「値が黙って消えないこと」であり、それを主張するテストを
 * {@code YamlFormatReaderInvalidInputTest#keepsOriginalColumnCaseInTable} に
 * {@code @Ignore} 付きで置いてある。<b>フィールド名称側（{@link RecordLayout}）は明文があるため
 * 拒否している</b>（{@code testdata_notation.rst:887}（{@code 30a8271} 時点））。
 * </p>
 *
 * <p>getter が返すコレクションは防御的コピーせず保持参照を返すため、呼び出し側は読み取り専用として扱うこと。</p>
 *
 * @author kiyotis
 */
public abstract sealed class ColumnRowDataBlock extends TestDataBlock
        permits TableDataBlock, ListMapBlock {

    private final List<String> columnNames;
    private final List<List<String>> rows;

    /**
     * コンストラクタ。
     *
     * @param dataType    データ種別
     * @param groupId     グループ ID（省略時は空文字）
     * @param identifier  識別子（テーブル名／LIST_MAP ID）
     * @param columnNames カラム名リスト（マーカーカラムを含む。記述順・大文字化なし）
     * @param rows        データ行のリスト（{@code null} セルと空文字 {@code ""} を区別し、特殊記法を記法のまま保持）
     * @throws IllegalArgumentException {@code columnNames} かその要素、{@code rows} かその要素（行）が
     *                                  {@code null} の場合（セルの {@code null} は通す）
     */
    protected ColumnRowDataBlock(DataType dataType, String groupId, String identifier,
                                 List<String> columnNames, List<List<String>> rows) {
        super(dataType, groupId, identifier);
        this.columnNames = ModelPreconditions.requireNoNulls("カラム名リスト", columnNames);
        this.rows = ModelPreconditions.requireNoNullRows("データ行のリスト", rows);
    }

    /** @return カラム名リスト（マーカーカラムを含む。記述順・大文字化なし） */
    public List<String> getColumnNames() {
        return columnNames;
    }

    /** @return データ行のリスト（{@code null}・空文字・特殊記法を未加工で保持） */
    public List<List<String>> getRows() {
        return rows;
    }
}
