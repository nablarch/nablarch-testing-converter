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
 * （根拠は {@link ModelPreconditions} の Javadoc）。
 * <b>行の中のセルの {@code null} は記法にあるため通す。</b>
 * </p>
 *
 * <p>
 * <b>カラム名 0 件で「行」を抱えるブロックは作れない。生成時点で拒否する。</b>
 * 記法はテーブル系データを
 * 「データタイプと識別子の値・カラム名・データ行という共通の構成を持つ」と定め、YAML はカラム名を
 * {@code rows:} の先頭要素のキーで決めるため、値があってカラム名が無い形は書けない。
 * Excel も記法によりカラム名の行を省略できず、この形を書き出すと<b>データ行がカラム名へ
 * 昇格して値が消える</b>（先頭のデータ行がカラム名の行として読まれる）。
 * </p>
 *
 * <p>
 * <b>セルを 1 つも持たない行も拒否する。</b>かつてはこの形を通していた ——
 * カラム名の行がマーカーカラムだけのブロックがマーカーの除外を受けると
 * 「カラム名 0 件・セルを持たない行が n 件」になり、Excel の読み込みと YAML の読み込みが
 * 仕様適合入力から実際に作っていたためである。<b>どちらの読み込みもマーカーカラムの名前と値を
 * 保つようになり、この形はどちらの読みからも作られなくなった。</b>どちらの記法にも書けない形でもある ——
 * YAML へ書くと {@code - &#123;&#125;} になり、読み戻すと行として存在しないものとして取り除かれる。
 * </p>
 *
 * <p>
 * <b>拒否しないのは「カラム名 0 件・行 0 件」だけである。</b>YAML の 0 件テーブル（記法は「0 件のデータは、
 * {@code rows:} に空配列 {@code []} を記載する」と定める）はカラム名を書く場所を持たず、YAML の読み込みが
 * 仕様適合入力から実際に作る。Excel への書き出しは、読み込み時に除外されるマーカーカラムを 1 つだけ
 * 書いて Excel の「カラム名の行は省略できない」制約を満たす（カラム名の行を書かないと、読み戻したとき
 * 直後のブロックのマーカー行がカラム名の行として読まれ、次のブロックが消える）。
 * </p>
 *
 * <p>
 * <b>{@code columnNames} の重複は拒否しない。番人も WARN も置かない。</b>
 * YAML の読み込みが重複したカラム名を持つブロックを実際に作るためである —— 本体 {@code TableData} が
 * テーブル系のカラム名を大文字化するので、{@code id} と {@code ID} を書いたスキーマ適合の YAML が
 * {@code columnNames} ＝ {@code [ID, ID]} になり、後のカラムの値で前のカラムの値が上書きされる（担保テストは
 * {@code YamlFormatReaderInvalidInputTest#dropsValueWhenTableColumnNamesDifferOnlyByCase}）。
 * 生成時に拒否すると仕様適合入力を変換できなくなるため、番人は置けない。
 * </p>
 *
 * <p>
 * <b>帰属は converter の外（nablarch-testing の {@code TableData}）である。</b>
 * 記法はテーブル系のカラム名の大小の扱いにいっさい触れていないため、記法に無い書き方を前提とした
 * あるべき姿のテストは置いていない。<b>フィールド名称側（{@link RecordLayout}）は、記法が同一レコード種別内での
 * フィールド名称の重複を記述時のエラーとして明文で禁じているため拒否している</b>。
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
     *                                  {@code null} の場合（セルの {@code null} は通す）、
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
