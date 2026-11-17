package nablarch.test.tool.converter;

import nablarch.test.tool.converter.model.TestDataContainer;

/**
 * 形式（Excel／YAML）を読み込み、形式中立な中間モデル（{@link TestDataContainer}）へ写す
 * IN 側のインタフェース。
 *
 * <p>1 リソース（Excel は 1 シート、YAML は 1 ファイル）を読み、値を記法のまま保持した中間モデルを組み立てる。</p>
 *
 * @author kiyobot
 */
public interface TestDataFormatReader {

    /**
     * 1 リソースを読み込み、中間モデルへ写す。
     *
     * @param basePath     読み込み元のディレクトリ
     * @param resourceName リソース名（Excel は {@code "ブック名/シート名"}、YAML はファイル名）
     * @return 中間モデル（テストクラス 1 つ分。読み込み単位を 1 つ持つ）
     */
    TestDataContainer read(String basePath, String resourceName);
}
