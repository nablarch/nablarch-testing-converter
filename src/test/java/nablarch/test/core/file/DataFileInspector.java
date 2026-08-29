package nablarch.test.core.file;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link DataFile} が保持するフィールド名と値へ、テストから到達するための相乗りヘルパ。
 *
 * <p>
 * ファイル・電文の値をフレームワーク本体に読ませて「正解」として取り出すとき、本体が実際に保持している
 * 値（末尾の空要素を取り除いてフィールド名の数まで空文字で埋め直したあとの値）を見る必要がある。
 * {@code DataFile#toDataRecords()} は固定長レコードとしての型変換を伴い、空文字が {@code null} へ
 * 変わるため、値そのものの突き合わせには使えない。
 * </p>
 *
 * <p>
 * 保持している値は {@code DataFileFragment} の protected フィールドの向こうにある。protected は
 * 同一パッケージからは見えるため、本クラスを {@link DataFile} と同じパッケージへ 1 枚だけ置いて壁を越える。
 * 相乗りの影響は本クラスに閉じる。テスト専用であり、{@code src/main} からは使わない。
 * </p>
 */
public final class DataFileInspector {

    /** ユーティリティクラスのため生成させない */
    private DataFileInspector() {
    }

    /**
     * ファイルが保持する値を、断片（レコードレイアウト）をまたいで記述順に並べて返す。
     *
     * @param file 本体が読んだファイル
     * @return 行ごとの値（フィールド名の定義順）
     */
    public static List<List<String>> values(DataFile file) {
        List<List<String>> rows = new ArrayList<List<String>>();
        for (DataFileFragment fragment : file.all) {
            for (Map<String, String> row : fragment.values) {
                List<String> values = new ArrayList<String>();
                for (String name : fragment.names) {
                    values.add(row.get(name));
                }
                rows.add(values);
            }
        }
        return rows;
    }

    /**
     * ファイルが保持するフィールド名を、断片をまたいで記述順に並べて返す。
     *
     * @param file 本体が読んだファイル
     * @return 断片ごとのフィールド名
     */
    public static List<List<String>> fieldNames(DataFile file) {
        List<List<String>> names = new ArrayList<List<String>>();
        for (DataFileFragment fragment : file.all) {
            names.add(new ArrayList<String>(fragment.names));
        }
        return names;
    }
}
