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
 * <b>名前を持たないコンテナは作れない。生成時点で拒否する</b>
 * （{@code coverage/issues.md} <b>XLS-37</b>）。名前はテストクラスと 1 対 1 に対応する引き当てキーであり、
 * {@code testdata_notation.rst:44}（{@code 30a8271} 時点）「同名の1つのExcelファイル（{@code .xls}
 * または {@code .xlsx}）がテストクラスに対応し、1シートが読み込み単位に対応する」のとおり、
 * Excel 形式ではブック名、YAML 形式ではディレクトリ名になる。{@code null} を持てると
 * 書き出し側が {@code null.xlsx} というファイルを黙って作ってしまう。
 * </p>
 *
 * <p>
 * 中間モデルは変換ツール内部の形式中立な表現であり、IN（形式 → 中間モデル）が組み立て、
 * OUT（中間モデル → 形式）が消費する。getter が返すコレクションは防御的コピーせず保持参照を返すため、
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
     * @throws IllegalArgumentException {@code name} が {@code null} の場合
     */
    public TestDataContainer(String name, List<TestDataSection> sections) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "器の名前を持たないコンテナは作れません"
                            + "（名前はテストクラスと 1 対 1 に対応する引き当てキーであり、"
                            + "Excel 形式ではブック名、YAML 形式ではディレクトリ名になります）。");
        }
        this.name = name;
        this.sections = sections;
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
