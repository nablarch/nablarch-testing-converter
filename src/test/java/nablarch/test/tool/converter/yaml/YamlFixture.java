package nablarch.test.tool.converter.yaml;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
     * <b>{@link nablarch.test.core.reader.yaml.YamlLoader} の LRU キャッシュはここでは触らない。</b>
     * キャッシュを空にする責務は利用側テストクラスの {@code @After clearLoaderCache} に一本化してある
     * （既存の {@code RoundTripTest} ほかと同じ形）。本フィクスチャを使うテストはすべて
     * {@link org.junit.rules.TemporaryFolder} が用意するテストごとに別のディレクトリへ書くため、
     * 1 つのテストメソッドの中で同一パスを書き直さない限りキャッシュは衝突しない。
     * </p>
     *
     * @param dir      書き出し先ディレクトリ
     * @param yamlText YAML テキスト
     * @return 中間モデル
     */
    static TestDataContainer read(Path dir, String yamlText) {
        Path file = dir.resolve(RESOURCE + ".yaml");
        try {
            Files.createDirectories(dir);
            Files.write(file, yamlText.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("failed to write fixture: " + file, e);
        }
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
