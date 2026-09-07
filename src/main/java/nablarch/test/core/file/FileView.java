package nablarch.test.core.file;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 本体ファイル器の読み取り専用スナップショット。
 */
public final class FileView {

    /** ファイルパス */
    private final String path;

    /** ディレクティブ（型変換済み・記述順は保たない＝器固有挙動） */
    private final Map<String, Object> directives;

    /** 断片（レコードレイアウト単位） */
    private final List<FragmentView> fragments;

    /**
     * コンストラクタ。
     *
     * @param path       ファイルパス
     * @param directives ディレクティブ
     * @param fragments  断片
     */
    FileView(String path, Map<String, Object> directives, List<FragmentView> fragments) {
        this.path = path;
        this.directives = directives;
        this.fragments = fragments;
    }

    /** @return ファイルパス */
    public String getPath() {
        return path;
    }

    /** @return ディレクティブ（読み取り専用） */
    public Map<String, Object> getDirectives() {
        return Collections.unmodifiableMap(directives);
    }

    /** @return 断片一覧（読み取り専用） */
    public List<FragmentView> getFragments() {
        return Collections.unmodifiableList(fragments);
    }
}
