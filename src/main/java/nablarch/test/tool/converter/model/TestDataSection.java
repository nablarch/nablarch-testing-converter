package nablarch.test.tool.converter.model;

import java.util.List;

/**
 * NTF の読み込み単位 1 つ分を表す中間モデル。
 *
 * <p>
 * Excel では 1 シート、YAML では 1 ファイルに相当する。配下に同一読み込み単位内の
 * データブロック（{@link TestDataBlock}）を記述順で保持する。
 * </p>
 *
 * <p>
 * 名前は {@code null} 不可で、生成時に拒否する。データを引き当てるキーであり、{@code null} では引けないためである。
 * 空文字やシート名の文字数上限は Excel 形式固有の制約であり、中間モデルの不変条件にはしない。
 * </p>
 *
 * <p>getter が返すコレクションは防御的コピーせず保持参照を返すため、呼び出し側は読み取り専用として扱うこと。</p>
 *
 * @author kiyotis
 */
public final class TestDataSection {

    private final String name;
    private final List<TestDataBlock> blocks;

    /**
     * コンストラクタ。
     *
     * @param name   シート名／ファイル名（拡張子なし）。{@code null} は不可
     * @param blocks データブロックのリスト（記述順）
     * @throws IllegalArgumentException {@code name} が {@code null} の場合、または
     *                                  {@code blocks} かその要素が {@code null} の場合
     */
    public TestDataSection(String name, List<TestDataBlock> blocks) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "読み込み単位の名前を持たないセクションは作れません"
                            + "（名前は呼び出し側がデータを引き当てるためのキーであり、"
                            + "Excel 形式ではシート名、YAML 形式ではファイル名になります）。");
        }
        this.name = name;
        this.blocks = ModelPreconditions.requireNoNulls("データブロックのリスト", blocks);
    }

    /** @return シート名／ファイル名（拡張子なし） */
    public String getName() {
        return name;
    }

    /** @return データブロックのリスト（記述順） */
    public List<TestDataBlock> getBlocks() {
        return blocks;
    }
}
