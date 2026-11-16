package nablarch.test.tool.converter.model;

import java.util.List;

/**
 * テストクラス 1 つ分のテストデータを表す中間モデルの最上位コンテナ。
 *
 * <p>
 * Excel では 1 ブック、YAML では 1 ディレクトリに相当し、テストクラスと 1 対 1 に対応する。
 * 配下に読み込み単位（{@link TestDataSection}）を保持する。
 * </p>
 *
 * <p>
 * 名前は {@code null} 不可で、生成時に拒否する。テストクラスと 1 対 1 に対応する引き当てキーであり、
 * Excel 形式ではブック名、YAML 形式ではディレクトリ名になる。
 * </p>
 *
 * <p>
 * 中間モデルは変換ツール内部の形式中立な表現である。getter が返すコレクションは防御的コピーせず保持参照を返すため、
 * 呼び出し側は読み取り専用として扱うこと。
 * </p>
 *
 * @author kiyotis
 */
public final class TestDataContainer {

    private final String name;
    private final List<TestDataSection> sections;

    /**
     * コンストラクタ。
     *
     * @param name     ブック名／ディレクトリ名（拡張子なし）
     * @param sections 読み込み単位のリスト（記述順）
     * @throws IllegalArgumentException {@code name} が {@code null} の場合、または
     *                                  {@code sections} かその要素が {@code null} の場合
     */
    public TestDataContainer(String name, List<TestDataSection> sections) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "器の名前を持たないコンテナは作れません"
                            + "（名前はテストクラスと 1 対 1 に対応する引き当てキーであり、"
                            + "Excel 形式ではブック名、YAML 形式ではディレクトリ名になります）。");
        }
        this.name = name;
        this.sections = ModelPreconditions.requireNoNulls("読み込み単位のリスト", sections);
    }

    /** @return ブック名／ディレクトリ名（拡張子なし） */
    public String getName() {
        return name;
    }

    /** @return 読み込み単位のリスト（記述順） */
    public List<TestDataSection> getSections() {
        return sections;
    }
}
