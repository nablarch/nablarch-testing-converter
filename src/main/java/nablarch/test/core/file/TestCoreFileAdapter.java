package nablarch.test.core.file;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * テストデータ変換ツール（{@code nablarch.test.tool.converter}）が、本体の構造解析結果である
 * {@link DataFile}／{@link DataFileFragment} の<b>器の中身</b>を読み取るための、{@code file}
 * パッケージ相乗りのアダプタ。
 * <p>
 * {@link DataFile#all}・{@link DataFile#directives}・{@link DataFileFragment#names}／
 * {@code types}／{@code lengths}／{@code values} は protected で、変換ツールのパッケージから
 * 直接読めない。本クラスを本体と同一パッケージ（{@code nablarch.test.core.file}）に 1 枚だけ
 * 相乗りさせ、この可視性の壁を越えて読み取り専用のスナップショット（{@link FileView}）へ写す。
 * 相乗りの影響は本クラスに局所化される（設計書 §共通）。
 * </p>
 * <p>
 * 本クラスは器が保持する値を<b>そのまま</b>写すだけで、原文復元（長さ省略・型表記の生行からの復元）は
 * 行わない。器が正規化した値（型記法のフレームワーク表記化など）はそのまま現れる。原文の復元は
 * 呼び出し側（Reader）が生行から行う（設計書 §共通「器が正規化する値の原文復元」）。なお
 * レコード種別（{@link DataFileFragment#recordType}）は本体で private のため読めず、{@link FragmentView}
 * は保持しない。レコード種別も呼び出し側が生行（名前行の先頭セル）から取る。
 * </p>
 *
 * @author kiyobot
 */
public final class TestCoreFileAdapter {

    /** インスタンス化させない。 */
    private TestCoreFileAdapter() {
    }

    /**
     * 本体ファイル器の中身を読み取り専用のスナップショットへ写す。
     *
     * @param file 本体ファイル器
     * @return ファイルビュー
     */
    public static FileView read(DataFile file) {
        List<FragmentView> fragments = new ArrayList<FragmentView>(file.all.size());
        for (DataFileFragment fragment : file.all) {
            fragments.add(new FragmentView(
                    copyOrNull(fragment.names),
                    copyOrNull(fragment.types),
                    copyOrNull(fragment.lengths),
                    copyValues(fragment.values)));
        }
        return new FileView(file.getPath(),
                new LinkedHashMap<String, Object>(file.directives),
                fragments);
    }

    /**
     * リストを防御的にコピーする（{@code null} はそのまま {@code null}）。
     *
     * @param list 対象（{@code null} 可）
     * @return コピー（{@code null} なら {@code null}）
     */
    private static List<String> copyOrNull(List<String> list) {
        return list == null ? null : new ArrayList<String>(list);
    }

    /**
     * データ行（フィールド名→値の Map のリスト）を防御的にコピーする。
     *
     * @param values 対象
     * @return コピー
     */
    private static List<Map<String, String>> copyValues(List<Map<String, String>> values) {
        List<Map<String, String>> copy = new ArrayList<Map<String, String>>(values.size());
        for (Map<String, String> row : values) {
            copy.add(new LinkedHashMap<String, String>(row));
        }
        return copy;
    }

}
