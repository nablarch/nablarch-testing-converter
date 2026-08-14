package nablarch.test.tool.converter.yaml;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
 * <p>
 * パスの型は兄弟の {@code XlsFixture}（{@code open(Path)} ほか）と内部実装（{@link Files}）に揃えて
 * {@link Path} を用いる。
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
     * <p>
     * <b>副作用</b>: 読み込み前に {@link YamlLoader#clearCacheForTest()} を呼び、静的グローバルの
     * LRU キャッシュ（{@code YamlLoader.YAML_CACHE}）を空にする。同一パスへ内容の違う YAML を書き直して
     * 読むテストが、直前のテストのキャッシュ結果を受け取らないようにするためである。
     * </p>
     *
     * @param dir      書き出し先ディレクトリ
     * @param yamlText YAML テキスト
     * @return 中間モデル
     */
    static TestDataContainer read(Path dir, String yamlText) {
        try {
            Files.write(dir.resolve(RESOURCE + ".yaml"), yamlText.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        YamlLoader.clearCacheForTest();
        return new YamlFormatReader().read(dir.toAbsolutePath().toString(), RESOURCE);
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
