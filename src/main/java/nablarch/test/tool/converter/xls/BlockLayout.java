package nablarch.test.tool.converter.xls;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 1 ブロックの版面（行・行種別・マーカーカラム位置）。
 * <p>各行のセル数は不揃いでよく、描画時にブロック最大幅へ矩形整形される。</p>
 */
final class BlockLayout {

    /** 行（記述順）。 */
    private final List<List<String>> rows = new ArrayList<List<String>>();

    /** 各行の種別（{@link #rows} と同順）。 */
    private final List<RowKind> kinds = new ArrayList<RowKind>();

    /** マーカーカラムの列番号。 */
    private final Set<Integer> markerColumns = new HashSet<Integer>();

    /** ブロック最大幅（列数）。 */
    private int width = 0;

    /**
     * 1 行を追加する。
     *
     * @param kind  行種別
     * @param cells セル
     */
    void add(RowKind kind, List<String> cells) {
        rows.add(cells);
        kinds.add(kind);
        if (cells.size() > width) {
            width = cells.size();
        }
    }

    /**
     * マーカーカラムを登録する。
     *
     * @param column 列番号
     */
    void markMarkerColumn(int column) {
        markerColumns.add(column);
    }

    /** @return 行数 */
    int size() {
        return rows.size();
    }

    /** @return ブロック最大幅 */
    int width() {
        return width;
    }

    /**
     * @param r 行番号
     * @return 当該行のセル
     */
    List<String> row(int r) {
        return rows.get(r);
    }

    /**
     * @param r 行番号
     * @return 当該行の種別
     */
    RowKind kind(int r) {
        return kinds.get(r);
    }

    /**
     * @param column 列番号
     * @return マーカーカラムなら真
     */
    boolean isMarkerColumn(int column) {
        return markerColumns.contains(column);
    }
}
