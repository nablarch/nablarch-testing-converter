package nablarch.test.tool.converter.xls;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nablarch.test.core.reader.DataType;

/**
 * 1 ブロックの版面（行・行種別・マーカーカラム位置）。
 * <p>各行のセル数は不揃いでよく、描画時にブロック最大幅へ矩形整形される。</p>
 * <p>ブロックのデータタイプと識別子を保持し、{@link #headerFill()} でグループ別ヘッダ色を返す。</p>
 */
final class BlockLayout {

    /** 行（記述順）。 */
    private final List<List<String>> rows = new ArrayList<>();

    /** 各行の種別（{@link #rows} と同順）。 */
    private final List<RowKind> kinds = new ArrayList<>();

    /** マーカーカラムの列番号。 */
    private final Set<Integer> markerColumns = new HashSet<>();

    /** ブロック最大幅（列数）。 */
    private int width = 0;

    /** ブロックのデータタイプ。 */
    private final DataType dataType;

    /** ブロックの識別子。 */
    private final String identifier;

    /** {@link DataType#LIST_MAP} ブロックのうち testShots グループに属する識別子。 */
    static final String TEST_SHOTS_IDENTIFIER = "testShots";

    /**
     * コンストラクタ。
     *
     * @param dataType   ブロックのデータタイプ
     * @param identifier ブロックの識別子
     */
    BlockLayout(DataType dataType, String identifier) {
        this.dataType = dataType;
        this.identifier = identifier;
    }

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

    /**
     * このブロックのグループに対応するヘッダ背景色の {@link Fill} を返す。
     *
     * <ul>
     *   <li>testShots: {@code LIST_MAP} かつ identifier が {@code "testShots"} → {@link Fill#HEADER_TEST_SHOTS}</li>
     *   <li>SETUP 系: {@code SETUP_TABLE_DATA} / {@code SETUP_FIXED} / {@code SETUP_VARIABLE}
     *       → {@link Fill#HEADER_SETUP}</li>
     *   <li>EXPECTED 系: {@code EXPECTED_TABLE_DATA} / {@code EXPECTED_COMPLETED} / {@code EXPECTED_FIXED} /
     *       {@code EXPECTED_VARIABLE} / {@code EXPECTED_REQUEST_HEADER_MESSAGES} /
     *       {@code EXPECTED_REQUEST_BODY_MESSAGES} / {@code RESPONSE_HEADER_MESSAGES} /
     *       {@code RESPONSE_BODY_MESSAGES} → {@link Fill#HEADER_EXPECTED}</li>
     *   <li>それ以外（{@code MESSAGE} / {@code LIST_MAP}（非 testShots）/ {@code DEFAULT}）
     *       → {@link Fill#HEADER_OTHER}</li>
     * </ul>
     *
     * @return グループ別ヘッダ背景色
     */
    Fill headerFill() {
        if (dataType == DataType.LIST_MAP && TEST_SHOTS_IDENTIFIER.equals(identifier)) {
            return Fill.HEADER_TEST_SHOTS;
        }
        switch (dataType) {
            case SETUP_TABLE_DATA:
            case SETUP_FIXED:
            case SETUP_VARIABLE:
                return Fill.HEADER_SETUP;
            case EXPECTED_TABLE_DATA:
            case EXPECTED_COMPLETED:
            case EXPECTED_FIXED:
            case EXPECTED_VARIABLE:
            case EXPECTED_REQUEST_HEADER_MESSAGES:
            case EXPECTED_REQUEST_BODY_MESSAGES:
            case RESPONSE_HEADER_MESSAGES:
            case RESPONSE_BODY_MESSAGES:
                return Fill.HEADER_EXPECTED;
            default:
                return Fill.HEADER_OTHER;
        }
    }
}
