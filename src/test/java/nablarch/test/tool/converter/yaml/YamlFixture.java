package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
     * テストメソッドをまたいでキャッシュが衝突することはない。
     * </p>
     *
     * <p>
     * <b>1 つのテストメソッドの中で 2 回呼ぶと {@link IllegalStateException} で止まる。</b>
     * 書き出し先が固定名（{@link #RESOURCE}{@code .yaml}）であるため、2 回目は同じパスになり
     * ローダのキャッシュが<b>1 回目のパース結果を返す</b> —— すなわち 2 回目に書いた YAML は
     * 読まれないまま緑になる。黙って誤った観測をするより止めるほうがよいので、
     * 既に書き出し済みなら例外にする。2 パターン読みたい場合はテストメソッドを分けること。
     * </p>
     *
     * @param dir      書き出し先ディレクトリ
     * @param yamlText YAML テキスト
     * @return 中間モデル
     */
    static TestDataContainer read(Path dir, String yamlText) {
        Path file = dir.resolve(RESOURCE + ".yaml");
        if (Files.exists(file)) {
            throw new IllegalStateException("fixture already written in this test: " + file
                    + " — 同一テストメソッド内の 2 回目の read はローダのキャッシュが 1 回目の結果を返すため、"
                    + "テストメソッドを分けること");
        }
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

    /**
     * 唯一のセクションの唯一のブロックが、期待する実装クラスであることを確かめて返す。
     *
     * <p>
     * 素キャスト（失敗時に {@code ClassCastException} しか出ない）を避け、どのクラスが来たかが
     * 失敗メッセージに出るようにするためのヘルパ。兄弟の
     * {@code XlsFormatReaderRealFileTest#onlyBlock(Class)} と役割は同じだが、あちらは
     * 1 クラスでしか使わないため private なインスタンスメソッドである。
     * </p>
     *
     * <p>
     * こちらを {@code static} でフィクスチャ側に置いているのは、「唯一のブロック」の検査を辺②の
     * テストクラス 3 つ（{@code YamlFormatReaderInvalidInputTest} ／ {@code YamlFormatReaderRealFileTest} ／
     * {@code YamlFormatReaderScalarTest}）で共有し、同じ失敗がクラスごとに別のメッセージに
     * ならないようにするためである。{@code XlsFixture} が「中間モデルを組み立てるヘルパは引き受けない」と
     * 線を引いているのに対し、本クラスは<b>中間モデル側の取り出しヘルパも引き受ける</b>点で線引きが違う。
     * </p>
     *
     * @param <T>       期待する実装クラス
     * @param container 中間モデル
     * @param expected  期待する実装クラス
     * @return 唯一のブロック
     */
    static <T extends TestDataBlock> T onlyBlock(TestDataContainer container, Class<T> expected) {
        List<TestDataBlock> blocks = blocks(container);
        assertThat("ブロックが 1 件だけ生成されること", blocks.size(), is(1));
        TestDataBlock block = blocks.get(0);
        assertThat("唯一のブロックの実装クラス", block, is(instanceOf(expected)));
        return expected.cast(block);
    }
}
