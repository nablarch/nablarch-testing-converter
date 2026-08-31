package nablarch.test.core.file;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 本体ファイル断片（レコードレイアウト）の読み取り専用スナップショット。
 * <p>
 * レコード種別・長さ省略判定は本体で private のため保持しない。これらは呼び出し側が
 * 生行から復元する。
 * </p>
 */
public final class FragmentView {

    /** フィールド名称 */
    private final List<String> names;

    /** データ型シンボル（器がフレームワーク表記へ正規化済み。{@code null} 可） */
    private final List<String> types;

    /** フィールド長（器が省略長を実バイト長へ上書き済み。可変長では {@code null}） */
    private final List<String> lengths;

    /** データ行（フィールド名→値） */
    private final List<Map<String, String>> values;

    /**
     * コンストラクタ。
     *
     * @param names   フィールド名称
     * @param types   データ型シンボル（{@code null} 可）
     * @param lengths フィールド長（{@code null} 可）
     * @param values  データ行
     */
    FragmentView(List<String> names, List<String> types, List<String> lengths,
                 List<Map<String, String>> values) {
        this.names = names;
        this.types = types;
        this.lengths = lengths;
        this.values = values;
    }

    /** @return フィールド名称（読み取り専用） */
    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }

    /** @return データ型シンボル（器が正規化済み。{@code null} 可） */
    public List<String> getTypes() {
        return types == null ? null : Collections.unmodifiableList(types);
    }

    /** @return フィールド長（器が省略長を上書き済み。{@code null} 可） */
    public List<String> getLengths() {
        return lengths == null ? null : Collections.unmodifiableList(lengths);
    }

    /** @return データ行（読み取り専用） */
    public List<Map<String, String>> getValues() {
        return Collections.unmodifiableList(values);
    }
}
