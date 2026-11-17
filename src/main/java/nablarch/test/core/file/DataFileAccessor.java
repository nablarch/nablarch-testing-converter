package nablarch.test.core.file;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nablarch.test.tool.converter.reader.FileView;
import nablarch.test.tool.converter.reader.FragmentView;

/**
 * 本体の {@link DataFile}／{@link DataFileFragment} の中身を、読み取り専用のスナップショット
 * （{@link FileView}）へ写すアクセサ。
 * <p>
 * 読み取る項目が protected のため、本体と同じパッケージに置いている。
 * レコード種別は本体で private のため読めず、{@link FragmentView} は保持しない。
 * </p>
 *
 * @author kiyobot
 */
public final class DataFileAccessor {

    /** インスタンス化させない。 */
    private DataFileAccessor() {
    }

    /**
     * 本体ファイル器の中身を読み取り専用のスナップショットへ写す。
     *
     * @param file 本体ファイル器
     * @return ファイルビュー
     */
    public static FileView read(DataFile file) {
        List<FragmentView> fragments = new ArrayList<>(file.all.size());
        for (DataFileFragment fragment : file.all) {
            fragments.add(new FragmentView(
                    copyOrNull(fragment.names),
                    copyOrNull(fragment.types),
                    copyOrNull(fragment.lengths),
                    copyValues(fragment.values)));
        }
        return new FileView(file.getPath(),
                new LinkedHashMap<>(file.directives),
                fragments);
    }

    /**
     * リストを防御的にコピーする（{@code null} はそのまま {@code null}）。
     *
     * @param list 対象（{@code null} 可）
     * @return コピー（{@code null} なら {@code null}）
     */
    private static List<String> copyOrNull(List<String> list) {
        return list == null ? null : new ArrayList<>(list);
    }

    /**
     * データ行（フィールド名→値の Map のリスト）を防御的にコピーする。
     *
     * @param values 対象
     * @return コピー
     */
    private static List<Map<String, String>> copyValues(List<Map<String, String>> values) {
        List<Map<String, String>> copy = new ArrayList<>(values.size());
        for (Map<String, String> row : values) {
            copy.add(new LinkedHashMap<>(row));
        }
        return copy;
    }

}
