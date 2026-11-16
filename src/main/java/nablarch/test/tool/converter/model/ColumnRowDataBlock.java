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
 * {@code columnNames} ／ {@code rows} とその要素（行）は {@code null} を取らない（セルの {@code null} は通す）。
 * </p>
 *
 * <p>
 * カラム名 0 件で行を持つブロックは生成時に拒否する。カラム名の無い値はどちらの記法でも書けないためである
 * （YAML はカラム名を {@code rows:} の先頭要素のキーで決め、Excel ではデータ行がカラム名へ昇格して値が消える）。
 * セルを 1 つも持たない行も同じ理由で拒否する。
 * 「カラム名 0 件・行 0 件」は YAML の 0 件テーブルとして記法にあるため通す。
 * </p>
 *
 * <p>
 * {@code columnNames} の重複は拒否しない。本体 {@code TableData} がカラム名を大文字化するため、
 * {@code id} と {@code ID} を書いた仕様適合の YAML から重複したカラム名が実際に作られ、拒否すると変換できなくなるためである。
 * この挙動は converter の外（nablarch-testing）に帰属する。
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
     * @throws IllegalArgumentException {@code columnNames} ／ {@code rows} かその要素が {@code null} の場合、
     *                                  または {@code columnNames} が空で {@code rows} が空でない場合
     */
    protected ColumnRowDataBlock(DataType dataType, String groupId, String identifier,
                                 List<String> columnNames, List<List<String>> rows) {
        super(dataType, groupId, identifier);
        this.columnNames = ModelPreconditions.requireNoNulls("カラム名リスト", columnNames);
        this.rows = ModelPreconditions.requireNoNullRows("データ行のリスト", rows);
        if (this.columnNames.isEmpty() && !this.rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "カラム名を 1 件も持たないブロックはデータ行を持てません"
                            + "（記法のテーブル系データはデータタイプと識別子の値・カラム名・"
                            + "データ行という共通の構成を持ち、YAML ではカラム名が rows: の"
                            + "先頭要素のキーで決まるため、カラム名が無い行は書けません）。"
                            + " 識別子=[" + identifier + "] 行数=" + this.rows.size());
        }
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
