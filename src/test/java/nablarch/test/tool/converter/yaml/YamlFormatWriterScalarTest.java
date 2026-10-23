package nablarch.test.tool.converter.yaml;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.Collections;

import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 辺④（中間モデル→YAML）の軸D — 中間モデルの値が YAML へどの記法で書かれるかと、それを辺②で
 * 読み戻したときに元の文字列へ戻るかを固定するテスト。
 *
 * <p>
 * 対象は {@code .rn/ntf-test-data-converter/coverage/inventory.md} §0.5 の辺④ 9 ケース
 * （D4-01 {@code "100"}／D4-02 {@code "true"}／D4-03 {@code "null"}／D4-04 {@code null}／
 * D4-05 {@code ""}／D4-06 {@code "007"}／D4-07 改行含む／D4-08 {@code "2026-08-07"}／
 * D4-09 コロン・ハイフン・{@code #} 含む）である。
 * </p>
 *
 * <p>
 * <b>本クラスが担うのは、既存の {@code YamlFormatWriterTest} が通していない分だけである。</b>
 * D4-03／D4-04（{@code YamlFormatWriterTest#serialize_distinguishesNullFromNullString} が記法を、
 * {@code #roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader} が往復を通す）と
 * D4-05（{@code #serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation} が記法を、
 * {@code #roundTrip_table_isPreservedThroughRealReader} が往復を通す）、および D4-07 の記法
 * （{@code #serialize_escapesQuotesBackslashAndControlChars}）は既に担保があるため重複させない。
 * 対応は {@code inventory.md} §4.1-2 の表に全 9 ケースぶん載せてある。
 * </p>
 *
 * <p>
 * <b>アサートは「値が取れる」ではなく出力 YAML の記法そのもの</b>（引用符の有無・折り返し・
 * NULL 表現）に対して行う。{@link YamlFormatWriter} は全値をダブルクォートで囲む方針であり、
 * その方針が効いていることは記法を見なければ確かめられないためである。
 * </p>
 *
 * <p>
 * 往復（{@code write} → 実 {@link YamlFormatReader}）は<b>実ファイルを経由する</b>。
 * すなわち辺④の書き出しと辺②の読み取り（スカラー解決・スキーマ検証）の双方を実行する。
 * 辺②側の単独の担保は {@code YamlFormatReaderScalarTest}（D2-01〜D2-12）にある。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>期待される仕様ではない。
 * 妥当でないと判断した挙動は {@code .rn/ntf-test-data-converter/coverage/issues.md} に課題
 * （{@code YML-13}）として記録してあり、実装（src/main）は変更していない。
 * </p>
 *
 * @author kiyobot
 */
public class YamlFormatWriterScalarTest {

    /**
     * 折り返しの起きる長さの、改行を含む値。
     *
     * <p>
     * snakeyaml-engine の出力幅（既定 80 桁）を超え、かつエスケープを要する文字（改行）を含むと、
     * ダブルクォートスカラーが行末の {@code \} で折り返される（実測）。長さだけでは折り返されない
     * （エスケープを要する文字を 1 つも含まない 300 文字の値は 1 行のまま。実測）。
     * </p>
     */
    private static final String FOLDED_VALUE =
            "aaaa bbbb cccc dddd eeee ffff gggg hhhh\niiii jjjj kkkk llll mmmm nnnn oooo pppp qqqq";

    /** {@link #FOLDED_VALUE} と同じ形をカラム名（＝キー）に置いたもの。 */
    private static final String FOLDED_KEY =
            "AAAA BBBB CCCC DDDD EEEE FFFF GGGG HHHH\nIIII JJJJ KKKK LLLL MMMM NNNN OOOO PPPP QQQQ";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final YamlFormatWriter writer = new YamlFormatWriter();

    /** 往復で実 {@link YamlFormatReader} を通すため、{@link YamlLoader} の LRU キャッシュをテスト間で残さない。 */
    @After
    public void clearLoaderCache() {
        YamlLoader.clearCacheForTest();
    }

    // ------------------------------------------------------------------ helpers

    /**
     * 検証対象の値 1 個を {@code setup_tables} の 1 行 1 カラム（{@code V}）に置いたブロックを組み立てる。
     *
     * <p>
     * カラム名を大文字 {@code V} にしているのは、テーブル系のカラム名が読み戻しで大文字化されるため
     * （{@code issues.md} <b>YML-10</b>）。小文字にすると往復判定が「値が戻るか」ではなく
     * 「カラム名が戻るか」に引きずられる。カラム名側の挙動は
     * {@code YamlFormatWriterModelTest#uppercasesTableAndColumnNamesWhenReadBack} が固定する。
     * </p>
     *
     * @param value 値（{@code null} 可）
     * @return ブロック
     */
    private static TableDataBlock block(String value) {
        return block("V", value);
    }

    /**
     * 検証対象の値 1 個を {@code setup_tables} の 1 行 1 カラムに置いたブロックを組み立てる。
     *
     * @param column カラム名
     * @param value  値（{@code null} 可）
     * @return ブロック
     */
    private static TableDataBlock block(String column, String value) {
        return new TableDataBlock(DataType.SETUP_TABLE_DATA, "", "T",
                Collections.singletonList(column),
                Collections.singletonList(Collections.singletonList(value)));
    }

    /**
     * 値 1 個を持つセクションを直列化する。
     *
     * @param value 値（{@code null} 可）
     * @return YAML テキスト
     */
    private String serializeValue(String value) {
        return writer.serialize(section(block(value)));
    }

    /**
     * ブロックを 1 セクションに包む。
     *
     * @param block ブロック
     * @return セクション
     */
    private static TestDataSection section(TestDataBlock block) {
        return new TestDataSection("td", Collections.singletonList(block));
    }

    /**
     * 期待する出力 YAML を組み立てる（{@code setup_tables} 1 ブロック 1 行 1 カラム）。
     *
     * @param scalar {@code "      - V: "} に続けて書かれるはずのスカラー記法
     * @return YAML テキスト
     */
    private static String expected(String scalar) {
        return ""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - V: " + scalar + "\n";
    }

    /**
     * 値 1 個を実ファイルへ書き出し、本番配線の {@link YamlFormatReader} で読み戻した値を返す。
     *
     * @param value 値（{@code null} 可）
     * @return 読み戻した値
     */
    private String writeAndReadBack(String value) {
        String base = folder.getRoot().getAbsolutePath();
        writer.write(new TestDataContainer("td", Collections.singletonList(section(block(value)))), base);
        TableDataBlock table = YamlFixture.onlyBlock(
                new YamlFormatReader().read(base, "td"), TableDataBlock.class);
        assertThat("読み戻したカラム名", table.getColumnNames(), is(Arrays.asList("V")));
        assertThat("読み戻した行数", table.getRows().size(), is(1));
        return table.getRows().get(0).get(0);
    }

    // ------------------------------------------------------------------ D4-01 "100"

    /**
     * Given: 数値に見える文字列 {@code 100}。
     * When : {@code serialize}。
     * Then : ダブルクォート付きの {@code "100"} として書かれる。
     *
     * <p>
     * 担保する軸要素: D4-01。既存の
     * {@code YamlFormatWriterTest#roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader} は
     * 往復で文字列として戻ることだけを見ており、<b>出力記法をアサートしていない</b>
     * （{@code inventory.md} §4.1 でも 🔺 としている）。
     * </p>
     */
    @Test
    public void writesNumberLookingStringAsDoubleQuotedScalar() {
        assertThat(serializeValue("100"), is(expected("\"100\"")));
    }

    /**
     * Given: 数値に見える文字列 {@code 100}。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 文字列 {@code "100"} として復元される（数値にならない）。
     *
     * <p>担保する軸要素: D4-01（往復）。辺②側は {@code YamlFormatReaderScalarTest#readsQuotedNumberAsString}。</p>
     */
    @Test
    public void restoresNumberLookingStringThroughRealReader() {
        assertThat(writeAndReadBack("100"), is("100"));
    }

    // ------------------------------------------------------------------ D4-02 "true"

    /**
     * Given: 真偽値に見える文字列 {@code true}。
     * When : {@code serialize}。
     * Then : ダブルクォート付きの {@code "true"} として書かれる。
     *
     * <p>担保する軸要素: D4-02。</p>
     */
    @Test
    public void writesBooleanLookingStringAsDoubleQuotedScalar() {
        assertThat(serializeValue("true"), is(expected("\"true\"")));
    }

    /**
     * Given: 真偽値に見える文字列 {@code true}。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 文字列 {@code "true"} として復元される（真偽値にならない）。
     *
     * <p>
     * 担保する軸要素: D4-02（往復）。辺②側は {@code YamlFormatReaderScalarTest#readsQuotedTrueAsString}。
     * 引用符を落とすと本体スキーマが {@code rows} の値を {@code ["string","null"]} に限るため
     * 読み戻しがスキーマ違反で止まる（{@code YamlFormatReaderScalarTest} のクラス Javadoc）。
     * すなわちここで確かめているのは「全値クォート方針が往復を成立させている」ことである。
     * </p>
     */
    @Test
    public void restoresBooleanLookingStringThroughRealReader() {
        assertThat(writeAndReadBack("true"), is("true"));
    }

    // ------------------------------------------------------------------ D4-06 "007"

    /**
     * Given: 先頭ゼロの数値に見える文字列 {@code 007}。
     * When : {@code serialize}。
     * Then : ダブルクォート付きの {@code "007"} として書かれる（先頭ゼロが落ちない）。
     *
     * <p>担保する軸要素: D4-06。</p>
     */
    @Test
    public void writesLeadingZeroNumberAsDoubleQuotedScalar() {
        assertThat(serializeValue("007"), is(expected("\"007\"")));
    }

    /**
     * Given: 先頭ゼロの数値に見える文字列 {@code 007}。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 文字列 {@code "007"} として復元される。
     *
     * <p>担保する軸要素: D4-06（往復）。辺②側は {@code YamlFormatReaderScalarTest#readsLeadingZeroNumberAsString}。</p>
     */
    @Test
    public void restoresLeadingZeroNumberThroughRealReader() {
        assertThat(writeAndReadBack("007"), is("007"));
    }

    // ------------------------------------------------------------------ D4-07 改行含む

    /**
     * Given: 改行を含む短い値 {@code "l1\nl2"}。
     * When : {@code serialize}。
     * Then : ブロックスカラー（{@code |} ／ {@code >}）ではなく、<b>1 行のダブルクォートスカラーの中で
     *        {@code \n} へエスケープ</b>して書かれる。
     *
     * <p>
     * 担保する軸要素: D4-07。既存の
     * {@code YamlFormatWriterTest#serialize_escapesQuotesBackslashAndControlChars} は
     * {@code \n}／{@code \r}／{@code \t}／{@code \x01}／{@code "}／{@code \} をまとめた 1 つの値で
     * エスケープを固定している。本メソッドは<b>改行だけを含む値でも複数行記法にならない</b>ことを
     * 分けて示す（下の {@code #foldsLongEscapedValueWithBackslashContinuation} が示す折り返しとの対比）。
     * </p>
     */
    @Test
    public void writesNewlineContainingStringAsEscapedSingleLineScalar() {
        assertThat(serializeValue("l1\nl2"), is(expected("\"l1\\nl2\"")));
    }

    /**
     * Given: 改行を含む短い値 {@code "l1\nl2"}。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 改行を含んだまま復元される。
     *
     * <p>
     * 担保する軸要素: D4-07（往復）。辺②側でブロックスカラーを読む担保は
     * {@code YamlFormatReaderScalarTest#readsLiteralBlockScalarKeepingNewlines} にあるが、
     * <b>辺④が書くのはブロックスカラーではない</b>ため往復の経路が違う。
     * </p>
     */
    @Test
    public void restoresNewlineContainingStringThroughRealReader() {
        assertThat(writeAndReadBack("l1\nl2"), is("l1\nl2"));
    }

    /**
     * Given: 出力幅（80 桁）を超え、かつ改行を含む値。
     * When : {@code serialize}。
     * Then : ダブルクォートスカラーが行末の {@code \} で折り返され、継続行が現れる。
     *        <b>継続行の字下げは半角空白 2 個で、周囲のインデント（{@code V} は 9 桁目）に揃わない。</b>
     *
     * <p>
     * 担保する軸要素: D4-07（複数行記法）。{@link YamlFormatWriter#q} は値を単独で
     * {@code Dump#dumpToString} し、その結果を行へ差し込むだけであるため、折り返しの字下げは
     * 差し込み先の深さを知らない。
     * </p>
     */
    @Test
    public void foldsLongEscapedValueWithBackslashContinuation() {
        assertThat(serializeValue(FOLDED_VALUE), is(""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - V: \"aaaa bbbb cccc dddd eeee ffff gggg hhhh"
                + "\\niiii jjjj kkkk llll mmmm nnnn oooo pppp\\\n"
                + "  \\ qqqq\"\n"));
    }

    /**
     * Given: 出力幅を超え、かつ改行を含む値。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 折り返されていても元の文字列へ復元される。
     *
     * <p>
     * 担保する軸要素: D4-07（折り返しの往復）。<b>値側の折り返しは往復を壊さない。</b>
     * 壊れるのはキー側である（{@code #failsToReadBackFoldedKey}／{@code issues.md} <b>YML-13</b>）。
     * </p>
     */
    @Test
    public void restoresFoldedLongEscapedValueThroughRealReader() {
        assertThat(writeAndReadBack(FOLDED_VALUE), is(FOLDED_VALUE));
    }

    // ------------------------------------------------------------------ D4-08 "2026-08-07"

    /**
     * Given: 日付に見える文字列 {@code 2026-08-07}。
     * When : {@code serialize}。
     * Then : ダブルクォート付きの {@code "2026-08-07"} として書かれる。
     *
     * <p>担保する軸要素: D4-08。</p>
     */
    @Test
    public void writesDateLookingStringAsDoubleQuotedScalar() {
        assertThat(serializeValue("2026-08-07"), is(expected("\"2026-08-07\"")));
    }

    /**
     * Given: 日付に見える文字列 {@code 2026-08-07}。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : 文字列 {@code "2026-08-07"} として復元される（日付にならない）。
     *
     * <p>担保する軸要素: D4-08（往復）。辺②側は {@code YamlFormatReaderScalarTest#readsDateLikeStringAsIs}。</p>
     */
    @Test
    public void restoresDateLookingStringThroughRealReader() {
        assertThat(writeAndReadBack("2026-08-07"), is("2026-08-07"));
    }

    // ------------------------------------------------------------------ D4-09 コロン・ハイフン・#

    /**
     * Given: コロン・ハイフン・{@code #} を含む値 {@code "a: b - c #d"}。
     * When : {@code serialize}。
     * Then : ダブルクォート付きで 1 値として書かれる（コロンでキーに割れず、{@code #} 以降も残る）。
     *
     * <p>
     * 担保する軸要素: D4-09（<b>値側</b>）。キー側のクォートは別の要素であり、本メソッドは見ていない。
     * </p>
     *
     * <p>
     * <b>キー側の担保は、既存テストだけではコロン・空白・空文字・先頭 {@code -} の 4 つに限られていた</b>
     * （{@code YamlFormatWriterTest#serialize_quotesKeyContainingSpecialChars} ／
     * {@code #serialize_emptyKey_isQuoted} ／ {@code #serialize_keyStartingWithIndicator_isQuoted}。
     * {@code inventory.md} §4.1 の 15・16・23 行目）。
     * {@code YamlFormatWriter#isPlainSafeKey} が持つ特殊文字集合 18 文字のうち残る 17 文字と制御文字は
     * #25 のレビューまで未固定であり（{@code #} を集合から外しても全件が通る生存変異として実測された）、
     * {@code YamlFormatWriterModelTest#quotesDirectiveKeyContainingAnyYamlSpecialOrControlCharacter} で閉じた。
     * </p>
     */
    @Test
    public void writesColonHyphenAndHashContainingStringAsDoubleQuotedScalar() {
        assertThat(serializeValue("a: b - c #d"), is(expected("\"a: b - c #d\"")));
    }

    /**
     * Given: コロン・ハイフン・{@code #} を含む値 {@code "a: b - c #d"}。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : そのまま復元される（{@code #} 以降がコメントとして落ちない）。
     *
     * <p>
     * 担保する軸要素: D4-09（往復）。辺②側は
     * {@code YamlFormatReaderScalarTest#readsColonContainingStringAsIs} ／ {@code #readsHashContainingStringAsIs}。
     * 引用符が落ちれば {@code issues.md} <b>YML-11</b> のとおり値が黙って変わる。
     * </p>
     */
    @Test
    public void restoresColonHyphenAndHashContainingStringThroughRealReader() {
        assertThat(writeAndReadBack("a: b - c #d"), is("a: b - c #d"));
    }

    // ------------------------------------------------------------------ 折り返したキー（YML-13）

    /**
     * Given: 出力幅を超え、かつ改行を含むカラム名（＝キー）。
     * When : {@code serialize}。
     * Then : 値と同じく行末の {@code \} で折り返され、<b>キーが 2 行にまたがる</b>。
     *
     * <p>
     * 担保する軸要素: なし（{@code issues.md} <b>YML-13</b> の現状挙動の固定）。
     * YAML の simple key は 1 行に収まらなければならないため、この出力は読み戻せない
     * （{@code #failsToReadBackFoldedKey}）。
     * </p>
     */
    @Test
    public void foldsLongEscapedKeyWithBackslashContinuation() {
        TableDataBlock block = block(FOLDED_KEY, "v");

        assertThat(writer.serialize(section(block)), is(""
                + "setup_tables:\n"
                + "  - table: \"T\"\n"
                + "    rows:\n"
                + "      - \"AAAA BBBB CCCC DDDD EEEE FFFF GGGG HHHH"
                + "\\nIIII JJJJ KKKK LLLL MMMM NNNN OOOO PPPP\\\n"
                + "  \\ QQQQ\": \"v\"\n"));
    }

    /**
     * Given: 折り返されたキーを含む出力 YAML。
     * When : {@code write} → 実 {@link YamlFormatReader} で読み戻す。
     * Then : パースの時点で {@code IllegalStateException}（{@code Failed to parse YAML file: ...}）になる。
     *        スキーマ検証には到達しない。
     *
     * <p>
     * 担保する軸要素: なし（{@code issues.md} <b>YML-13</b>）。書き出しは成功するため、
     * <b>壊れていることが分かるのは読み戻しのときである</b>。
     * </p>
     */
    @Test
    public void failsToReadBackFoldedKey() {
        // Given
        TableDataBlock block = block(FOLDED_KEY, "v");
        String base = folder.getRoot().getAbsolutePath();
        writer.write(new TestDataContainer("td", Collections.singletonList(section(block))), base);

        // When / Then
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> new YamlFormatReader().read(base, "td"));
        assertThat(thrown.getMessage(), startsWith("Failed to parse YAML file: "));
    }
}
