package nablarch.test.tool.converter.yaml;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;

/**
 * 辺②（YAML→中間モデル）の実ファイル経路テスト用フィクスチャ。
 *
 * <p>
 * YAML テキストを実ファイル（{@code <dir>/testdata.yaml}）へ書き出し、本番配線の
 * {@link YamlFormatReader}（{@code YamlTestCoreAdapter} → {@code YamlLoader} → SnakeYAML Engine ＋
 * JSON スキーマ検証）で読む。{@code YamlFormatReaderTest} が {@code loadRawMap} を in-memory
 * {@code Map} に差し替えて駆動するのに対し、本フィクスチャを使うテストは
 * <b>YAML テキストのパースとスキーマ検証を実際に通す</b>。
 * </p>
 *
 * @author kiyobot
 */
final class YamlFixture {

    /** フィクスチャのリソース名（＝コンテナ名・セクション名になる）。 */
    static final String RESOURCE = "testdata";

    private YamlFixture() {
    }

    /**
     * YAML テキストを実ファイルへ書き出し、本番配線の {@link YamlFormatReader} で読む。
     *
     * @param dir      書き出し先ディレクトリ
     * @param yamlText YAML テキスト
     * @return 中間モデル
     */
    static TestDataContainer read(File dir, String yamlText) {
        write(dir, yamlText);
        YamlLoader.clearCacheForTest();
        return new YamlFormatReader().read(dir.getAbsolutePath(), RESOURCE);
    }

    /**
     * YAML テキストを実ファイルへ書き出す。
     *
     * @param dir      書き出し先ディレクトリ
     * @param yamlText YAML テキスト
     */
    static void write(File dir, String yamlText) {
        try {
            Files.write(new File(dir, RESOURCE + ".yaml").toPath(), yamlText.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 唯一のセクションのブロック群を返す。
     *
     * @param container 中間モデル
     * @return ブロック群
     */
    static List<TestDataBlock> blocks(TestDataContainer container) {
        return container.getSections().get(0).getBlocks();
    }
}
