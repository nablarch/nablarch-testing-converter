package nablarch.test.tool.converter;

import nablarch.test.tool.converter.model.TestDataContainer;

/**
 * 形式中立な中間モデル（{@link TestDataContainer}）を形式（Excel／YAML）へ書き出す OUT 側のインタフェース。
 *
 * <p>リソース名（ファイル名・シート名）は中間モデルのセクション名から決まる。</p>
 *
 * @author kiyobot
 */
public interface TestDataFormatWriter {

    /**
     * 中間モデルを形式へ書き出す。
     *
     * @param container 中間モデル（テストクラス 1 つ分）
     * @param basePath  書き出し先のディレクトリ
     */
    void write(TestDataContainer container, String basePath);
}
