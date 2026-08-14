package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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
     * {@link #read} を実行し、その間に {@code java.util.logging} のルートロガーへ届いた
     * WARNING 以上のログとあわせて返す。
     *
     * <p>
     * <b>「例外にも警告にもならず値が消える」ことが `issues.md` の主張の核心である課題</b>
     * （YML-09 ／ YML-10 ／ YML-11）について、警告の不在をアサートするために用いる。
     * 兄弟の {@code XlsFormatReaderInvalidInputTest#readCapturingWarnings} と同じ形である。
     * </p>
     *
     * <p>
     * <b>捕捉できるのは JUL 経路だけである。</b>{@code nablarch-testing} 自身のログ基盤
     * （{@code nablarch.core.log}）への出力は捕捉できないため、これで示せるのは
     * 「JUL 経路には警告が出ない」までであって「どこにも警告が出ない」ことではない。
     * </p>
     *
     * <p>
     * さらに辺②の読み取り経路（{@link YamlFormatReader}）は JUL のロガーを 1 つも持たないため、
     * <b>現状の実装ではこのアサートは落ちようがない</b>。意味を持つのは
     * 「将来 JUL の警告を足したときに気づける」という回帰検知としてである
     * （辺①の {@code XlsFormatReader} は重複カラム名で実際に JUL の WARNING を出すため、
     * そちらでは現在の挙動そのものを固定している）。この非対称は
     * {@code coverage/inventory.md} §2.1-2 の「開示」に記した。
     * </p>
     *
     * @param dir      書き出し先ディレクトリ
     * @param yamlText YAML テキスト
     * @return 中間モデルと捕捉した警告
     */
    static Reading readCapturingWarnings(Path dir, String yamlText) {
        CapturingHandler handler = new CapturingHandler();
        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(handler);
        try {
            return new Reading(read(dir, yamlText), handler.messages);
        } finally {
            rootLogger.removeHandler(handler);
        }
    }

    /**
     * 1 回の読み取りの結果（中間モデルと、その間に出力された WARNING 以上のログ）。
     */
    static final class Reading {

        /** 読み取った中間モデル。 */
        private final TestDataContainer container;

        /** 読み取り中に出力された WARNING 以上のログメッセージ。 */
        private final List<String> warnings;

        /**
         * コンストラクタ。
         *
         * @param container 読み取った中間モデル
         * @param warnings  捕捉した警告メッセージ
         */
        Reading(TestDataContainer container, List<String> warnings) {
            this.container = container;
            this.warnings = warnings;
        }

        /**
         * 読み取った中間モデルを返す。
         *
         * @return 中間モデル
         */
        TestDataContainer container() {
            return container;
        }

        /**
         * 捕捉した WARNING 以上のログメッセージを返す。
         *
         * @return 警告メッセージ
         */
        List<String> warnings() {
            return warnings;
        }
    }

    /**
     * WARNING 以上のログレコードを集めるハンドラ。
     */
    private static final class CapturingHandler extends Handler {

        /** 捕捉したメッセージ。 */
        private final List<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                messages.add(record.getMessage());
            }
        }

        @Override
        public void flush() {
            // 集めるだけなのでフラッシュする対象を持たない。
        }

        @Override
        public void close() {
            // 集めるだけなので解放する資源を持たない。
        }
    }

    /**
     * 唯一のセクションのブロック群を返す。
     *
     * @param container 中間モデル
     * @return ブロック群
     */
    static List<TestDataBlock> blocks(TestDataContainer container) {
        assertThat("セクションが 1 件だけ生成されること", container.getSections().size(), is(1));
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
