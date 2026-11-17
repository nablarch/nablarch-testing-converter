package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.core.reader.yaml.YamlSchemaValidationException;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 変換ツールの YAML 読み込みが、テスティングフレームワークの YAML 読み込みと同じ意味を持つことを
 * 実 {@code .yaml} を入力に固定する。
 *
 * <p>
 * 変換ツールの YAML 読み込みは器（{@code nablarch-testing-yaml} のビルダ）経由であり、実装は
 * 自動で追随する。本クラスはその追随を<b>テストで押さえる</b>ためのものである。押さえるのは次の 5 つ。
 * </p>
 * <ol>
 *   <li>末尾のフィールドに {@code null} と書いた場合は空文字になる（後ろに値があれば {@code null} のまま）</li>
 *   <li>電文のレコードレイアウトは 1 つである。2 つ以上書くとエラーになる</li>
 *   <li>{@code fw_header:} に書けるキーは決められた名前だけである。それ以外があるとエラーになる</li>
 *   <li>読み飛ばされる空エントリは空マッピング（{@code {}}）だけである。全値が空文字のエントリは残る</li>
 *   <li>バックスラッシュと {@code r} の 2 文字を含む値は書けない（エラーになる）</li>
 * </ol>
 */
public class YamlFrameworkAlignmentTest {

    /** テストごとに独立した出力先。 */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * ローダの LRU キャッシュを空にする。
     */
    @After
    public void clearLoaderCache() {
        YamlLoader.clearCacheForTest();
    }

    /**
     * フィクスチャ {@code .yaml} の出力先ディレクトリ。
     *
     * @return ディレクトリ
     */
    private Path dir() {
        return folder.getRoot().toPath();
    }

    /**
     * YAML を読み、投げられた例外を返す。例外にならなければテストを失敗させる。
     *
     * @param yamlText YAML テキスト
     * @return 投げられた例外
     */
    private RuntimeException readExpectingFailure(String yamlText) {
        try {
            YamlFixture.read(dir(), yamlText);
        } catch (RuntimeException e) {
            return e;
        }
        fail("エラーになるはずの YAML が読めてしまった");
        throw new IllegalStateException("unreachable");
    }

    /**
     * 例外の連鎖に、指定した文字列を含むメッセージがあるかを返す。
     *
     * @param thrown 投げられた例外
     * @param text   探す文字列
     * @return 含まれていれば真
     */
    private static boolean messageChainContains(Throwable thrown, String text) {
        for (Throwable t = thrown; t != null; t = t.getCause()) {
            if (t.getMessage() != null && t.getMessage().contains(text)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ 1. 末尾の null

    /**
     * Given: フィールド 3 件のレコード断片に、2 番目が {@code null}・3 番目に値がある行を書いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 2 番目は Java {@code null} のまま残る。末尾でない {@code null} は空文字にならない。
     *
     * <p>
     * 末尾に書いた {@code null} が空文字になることは
     * {@code YamlFormatReaderScalarTest#readsTrailingUnquotedNullAsEmptyStringInRecordFragmentPath} が
     * 固定している。本テストはその裏（後ろに値があれば {@code null} のまま）を押さえる。
     * </p>
     */
    @Test
    public void keepsNonTrailingNullAsJavaNullInRecordFragment() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_files:\n"
                + "  - path: \"f.dat\"\n"
                + "    type: \"fixed\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "          - {name: \"f2\", type: \"半角英字\", length: \"1\"}\n"
                + "          - {name: \"f3\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\", null, \"c\"]\n");

        // Then
        FileDataBlock block = YamlFixture.onlyBlock(container, FileDataBlock.class);
        List<RecordLayout> records = block.getRecords();
        assertThat(records.size(), is(1));
        assertThat("後ろに値があるフィールドの null は null のまま",
                records.get(0).getRows(), is(Arrays.asList(Arrays.asList("a", null, "c"))));
    }

    // ------------------------------------------------------------------ 2. 電文の records: は 1 つ

    /**
     * Given: {@code messages} の {@code records:} にレコードレイアウトを 2 つ書いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : エラーになる。電文のレコードレイアウトは 1 つである。
     *
     * <p>
     * この形は Excel 形式へは書けないため、変換ツール側の検査は要らない。YAML 側のスキーマ検証が弾く。
     * </p>
     */
    @Test
    public void rejectsMessageWithTwoRecords() {
        // Given / When
        RuntimeException thrown = readExpectingFailure(""
                + "messages:\n"
                + "  - id: \"M1\"\n"
                + "    directives:\n"
                + "      text-encoding: \"UTF-8\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n"
                + "      - fields:\n"
                + "          - {name: \"f2\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"b\"]\n");

        // Then
        assertThat("スキーマ検証で落ちること", thrown, is(instanceOf(YamlSchemaValidationException.class)));
        assertThat("records の件数について述べたエラーであること",
                messageChainContains(thrown, "$.messages[0].records"), is(true));
    }

    // ------------------------------------------------------------------ 3. fw_header: のキー

    /**
     * Given: {@code messages} の {@code fw_header:} に、決められた名前にないキーを書いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : エラーになる。
     *
     * <p>
     * {@code fw_header:} に書けるのはフレームワーク制御ヘッダとして扱う名前
     * （既定では {@code requestId} ／ {@code userId} ／ {@code resendFlag} ／ {@code resultCode}）だけである。
     * </p>
     */
    @Test
    public void rejectsFwHeaderWithUnknownKey() {
        // Given / When
        RuntimeException thrown = readExpectingFailure(""
                + "messages:\n"
                + "  - id: \"M1\"\n"
                + "    directives:\n"
                + "      text-encoding: \"UTF-8\"\n"
                + "    fw_header:\n"
                + "      notAFwHeaderField: \"x\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");

        // Then
        assertThat("知らないキーの名前がエラーに出ること",
                messageChainContains(thrown, "notAFwHeaderField"), is(true));
        assertThat("書けるキーの一覧がエラーに出ること",
                messageChainContains(thrown, "requestId"), is(true));
    }

    /**
     * Given: {@code messages} の {@code fw_header:} に、決められた名前のキーだけを書いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 読める。上のテストが「キーの名前」で落ちていることを示す対照である。
     */
    @Test
    public void acceptsFwHeaderWithKnownKey() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "messages:\n"
                + "  - id: \"M1\"\n"
                + "    directives:\n"
                + "      text-encoding: \"UTF-8\"\n"
                + "    fw_header:\n"
                + "      requestId: \"R1\"\n"
                + "    records:\n"
                + "      - fields:\n"
                + "          - {name: \"f1\", type: \"半角英字\", length: \"1\"}\n"
                + "        rows:\n"
                + "          - [\"a\"]\n");

        // Then
        assertThat(YamlFixture.onlyBlock(container, nablarch.test.tool.converter.model.MessageDataBlock.class)
                .getFwHeaderFields().get("requestId"), is("R1"));
    }

    // ------------------------------------------------------------------ 4. 空エントリは {} だけ

    /**
     * Given: すべての値が空文字 {@code ""} の行を含む {@code setup_files} のレコード断片。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : 行は残る。読み飛ばされる空エントリは空マッピング（{@code {}}）だけである。
     *
     * <p>
     * テーブル経路での同じ主張は
     * {@code YamlFormatReaderScalarTest#skipsRowWhoseValuesAreAllEmpty} が固定している。
     * </p>
     */
    @Test
    public void keepsRowWhoseValuesAreAllEmptyStringsInTable() {
        // Given / When
        TestDataContainer container = YamlFixture.read(dir(), ""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - K: \"\"\n"
                + "        V: \"\"\n");

        // Then
        TableDataBlock table = YamlFixture.onlyBlock(container, TableDataBlock.class);
        assertThat("全値が空文字のエントリは読み飛ばされない",
                table.getRows(), is(Arrays.asList(Arrays.asList("", ""))));
    }

    // ------------------------------------------------------------------ 5. 2 文字の \r

    /**
     * Given: バックスラッシュと {@code r} の 2 文字を含む値を書いた YAML。
     * When : 実 {@code .yaml} を {@code read}。
     * Then : エラーになる。
     *
     * <p>
     * Excel 形式ではこの 2 文字が必ず CR に変換されるため、この 2 文字を含む値は
     * テスティングフレームワークの仕様上存在しない。YAML 形式では入力の時点で弾かれる。
     * </p>
     */
    @Test
    public void rejectsTwoCharacterBackslashR() {
        // Given / When
        RuntimeException thrown = readExpectingFailure(""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - K: \"x\"\n"
                + "        V: \"a\\\\rb\"\n");

        // Then
        assertThat("書けない 2 文字を含む値がエラーに出ること",
                messageChainContains(thrown, "a\\rb"), is(true));
    }
}
