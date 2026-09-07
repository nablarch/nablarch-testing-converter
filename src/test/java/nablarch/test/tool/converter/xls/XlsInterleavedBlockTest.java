package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import nablarch.test.core.db.TableData;
import nablarch.test.core.file.DataFile;
import nablarch.test.core.file.DataFileInspector;
import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.FrameworkOracle;
import nablarch.test.core.reader.YamlFrameworkOracle;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;
import nablarch.test.tool.converter.yaml.YamlFormatWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 同じデータタイプ・同じグループ ID のデータブロックの間に別のデータブロックを挟んだシート
 * （交互記述）を変換したときの振る舞いを固定する。
 *
 * <p>
 * 収集方式が「グループ」のデータタイプ（テーブル・ファイル・グループ ID 付きの電文）では、
 * フレームワークは Excel 形式で収集を途中で終え、後ろのデータブロックを読まない。YAML 形式には
 * これに相当する記法が無いため、そのまま変換すると読まれなかったデータブロックが有効になって意味が変わる。
 * 変換ツールはこのシートを検出すると、読まれなかったデータブロックを出力せずに変換を続け、警告を出す。
 * </p>
 *
 * <p>
 * 本クラスが確かめるのは 3 つである。
 * </p>
 * <ol>
 *   <li>警告が 1 件出ること（ブック名・シート名・データタイプ・グループ ID・読まれなかったブロックの識別子を含む）</li>
 *   <li>出力に読まれなかったブロックが無いこと</li>
 *   <li>出力をフレームワークが読んだ結果が、元の {@code .xlsx} をフレームワークが読んだ結果と一致すること</li>
 * </ol>
 *
 * <p>
 * 読まれなかったブロックを出力から外しているのは変換ツールではなく<b>フレームワーク本体</b>である。
 * 変換ツールが足したのは警告だけで、自前の選別は行っていない。
 * </p>
 */
public class XlsInterleavedBlockTest {

    /** フィクスチャの既定ブック名。 */
    private static final String BOOK = "interleavedBook";

    /** フィクスチャの既定シート名。 */
    private static final String SHEET = "sheet1";

    /** テストごとに独立した出力先。 */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // ------------------------------------------------------------------ helpers

    /**
     * WARN 以上のログを収集するハンドラ。
     */
    private static final class CapturingHandler extends Handler {

        /** 収集したメッセージ */
        private final List<String> messages = new ArrayList<String>();

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                messages.add(record.getMessage());
            }
        }

        @Override
        public void flush() { /* no-op */ }

        @Override
        public void close() { /* no-op */ }
    }

    /**
     * 既定のブック名・シート名でフィクスチャの組み立てを開始する。
     *
     * @return フィクスチャビルダ
     */
    private static XlsFixture book() {
        return XlsFixture.book(BOOK).sheet(SHEET);
    }

    /**
     * フィクスチャ {@code .xlsx} の出力先ディレクトリ。
     *
     * @return ディレクトリ
     */
    private Path dir() {
        return folder.getRoot().toPath();
    }

    /**
     * 変換後の YAML の出力先ディレクトリ。
     *
     * @return ディレクトリ
     */
    private Path outDir() {
        return folder.getRoot().toPath().resolve("out");
    }

    /**
     * 既定のブック／シートのリソース名。
     *
     * @return リソース名
     */
    private static String resource() {
        return BOOK + "/" + SHEET;
    }

    /**
     * 既定のブック／シートを変換ツールで読み、WARN ログを集めながら中間モデルを返す。
     *
     * @param captured 収集した WARN メッセージの受け皿
     * @return 中間モデル
     */
    private TestDataContainer readCapturingWarnings(List<String> captured) {
        CapturingHandler handler = new CapturingHandler();
        Logger logger = Logger.getLogger(XlsFormatReader.class.getName());
        logger.addHandler(handler);
        boolean useParent = logger.getUseParentHandlers();
        Level level = logger.getLevel();
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.WARNING);
        try {
            return new XlsFormatReader().read(dir().toString(), resource());
        } finally {
            logger.removeHandler(handler);
            logger.setUseParentHandlers(useParent);
            logger.setLevel(level);
            captured.addAll(handler.messages);
        }
    }

    /**
     * 中間モデルのテーブルブロックを、識別子とグループ ID の対で列挙する。
     *
     * @param container 中間モデル
     * @return {@code "グループID/識別子"} の一覧（記述順）
     */
    private static List<String> tableKeys(TestDataContainer container) {
        List<String> keys = new ArrayList<String>();
        for (TestDataBlock block : container.getSections().get(0).getBlocks()) {
            keys.add(block.getGroupId() + "/" + block.getIdentifier());
        }
        return keys;
    }

    /**
     * 中間モデルを YAML へ書き出す。
     *
     * @param container 中間モデル
     */
    private void writeYaml(TestDataContainer container) {
        TestDataContainer rewrapped = new TestDataContainer(BOOK,
                Collections.singletonList(
                        new TestDataSection(SHEET, container.getSections().get(0).getBlocks())));
        new YamlFormatWriter().write(rewrapped, outDir().toString());
    }

    /**
     * フレームワークが読んだテーブルを、テーブル名と行の対で列挙する。
     *
     * @param tables テーブル一覧
     * @return テーブル名 → 行 の文字列表現（記述順）
     */
    private static List<String> tableDump(List<TableData> tables) {
        List<String> dump = new ArrayList<String>();
        for (TableData table : tables) {
            dump.add(table.getTableName() + "=" + YamlFrameworkOracle.rowsOf(table));
        }
        return dump;
    }

    /**
     * フレームワークが読んだファイルを、パスとフィールド名・値の対で列挙する。
     *
     * @param files ファイル一覧
     * @return パス → フィールド名 → 値 の文字列表現（記述順）
     */
    private static List<String> fileDump(List<? extends DataFile> files) {
        List<String> dump = new ArrayList<String>();
        for (DataFile file : files) {
            dump.add(file.getPath() + "=" + DataFileInspector.fieldNames(file)
                    + DataFileInspector.values(file));
        }
        return dump;
    }

    // ------------------------------------------------------------------ グループ ID が交互

    /**
     * Given: {@code SETUP_TABLE=A} ／ {@code SETUP_TABLE[g1]=B} ／ {@code SETUP_TABLE=C} の順に並んだ実 {@code .xlsx}。
     *        グループ ID 無しのブロックの間に、グループ ID 付きのブロックが挟まっている。
     * When : 実 {@code .xlsx} を変換ツールで読み、YAML へ書き出す。
     * Then : 警告が 1 件出る。出力に {@code C} は無い。出力をフレームワークが読んだ結果が、
     *        元の {@code .xlsx} をフレームワークが読んだ結果と一致する。
     */
    @Test
    public void warnsAndDropsBlockAfterInterleavedGroupId() {
        // Given
        book().row(text("SETUP_TABLE=A"))
                .row(text("id"))
                .row(text("a1"))
                .row(text("SETUP_TABLE[g1]=B"))
                .row(text("id"))
                .row(text("b1"))
                .row(text("SETUP_TABLE=C"))
                .row(text("id"))
                .row(text("c1"))
                .writeTo(dir());

        // When
        List<String> warnings = new ArrayList<String>();
        TestDataContainer container = readCapturingWarnings(warnings);
        writeYaml(container);

        // Then (i) 警告が 1 件
        assertThat("警告の件数", warnings.size(), is(1));
        String warning = warnings.get(0);
        assertThat(warning, containsString(BOOK));
        assertThat(warning, containsString(SHEET));
        assertThat(warning, containsString("SETUP_TABLE"));
        assertThat(warning, containsString("C"));

        // Then (ii) 出力に C が無い
        assertThat(tableKeys(container), is(java.util.Arrays.asList("/A", "g1/B")));

        // Then (iii) 出力をフレームワークが読んだ結果が、元の .xlsx を読んだ結果と一致する
        assertThat("グループ指定なし",
                tableDump(YamlFrameworkOracle.setupTables(outDir().toString(), SHEET)),
                is(tableDump(FrameworkOracle.tables(dir().toString(), resource(), "",
                        DataType.SETUP_TABLE_DATA))));
        assertThat("グループ g1",
                tableDump(YamlFrameworkOracle.setupTables(outDir().toString(), SHEET, "g1")),
                is(tableDump(FrameworkOracle.tables(dir().toString(), resource(), "[g1]",
                        DataType.SETUP_TABLE_DATA))));
    }

    /**
     * Given: {@code SETUP_TABLE=A} ／ {@code SETUP_TABLE[g1]=B} ／ {@code SETUP_TABLE=C} ／
     *        {@code SETUP_TABLE=D} の順に並んだ実 {@code .xlsx}。挟まれたあとに同じキーのブロックが
     *        <b>2 件</b>続く。
     * When : 実 {@code .xlsx} を変換ツールで読む。
     * Then : 警告は 1 件で、読まれなかった {@code C} と {@code D} の<b>両方</b>を含む。出力にも両方が無い。
     *
     * <p>
     * 読まれないブロックは打ち切り位置より後ろの<b>すべて</b>である。1 件目で数え終えると、
     * 2 件目以降が警告にも現れないまま出力からも消える。
     * </p>
     */
    @Test
    public void warnsAboutEveryUnreadBlockAfterInterleavedGroupId() {
        // Given
        book().row(text("SETUP_TABLE=A"))
                .row(text("id"))
                .row(text("a1"))
                .row(text("SETUP_TABLE[g1]=B"))
                .row(text("id"))
                .row(text("b1"))
                .row(text("SETUP_TABLE=C"))
                .row(text("id"))
                .row(text("c1"))
                .row(text("SETUP_TABLE=D"))
                .row(text("id"))
                .row(text("d1"))
                .writeTo(dir());

        // When
        List<String> warnings = new ArrayList<String>();
        TestDataContainer container = readCapturingWarnings(warnings);

        // Then (i) 警告 1 件に C と D の両方が載る。
        // 識別子 1 文字を containsString で探すと本文の別語に当たるため（"D" は "グループID" に含まれる）、
        // 読まれなかったブロックの一覧そのものを突き合わせる。
        assertThat("警告の件数", warnings.size(), is(1));
        String warning = warnings.get(0);
        assertThat(warning, containsString("[C, D]"));

        // Then (ii) 出力に C も D も無い
        assertThat(tableKeys(container), is(java.util.Arrays.asList("/A", "g1/B")));
    }

    // ------------------------------------------------------------------ ファイル系

    /**
     * Given: {@code SETUP_FIXED=a.dat} ／ {@code SETUP_FIXED[g1]=b.dat} ／ {@code SETUP_FIXED=c.dat} の順に
     *        並んだ実 {@code .xlsx}。テーブルと同じ形の交互記述をファイル系で組んだもの。
     * When : 実 {@code .xlsx} を変換ツールで読み、YAML へ書き出す。
     * Then : 警告が 1 件出る。出力に {@code c.dat} は無い。出力をフレームワークが読んだ結果が、
     *        元の {@code .xlsx} をフレームワークが読んだ結果と一致する。
     *
     * <p>
     * ファイル系はテーブルと同じく収集方式が「グループ」であり、交互記述の対象である。
     * </p>
     */
    @Test
    public void warnsAndDropsFileBlockAfterInterleavedGroupId() {
        // Given
        book().row(text("SETUP_FIXED=a.dat"))
                .row(text("data"), text("f1"))
                .row(XlsFixture.blank(), text("半角英字"))
                .row(XlsFixture.blank(), text("5"))
                .row(XlsFixture.blank(), text("a"))
                .row(text("SETUP_FIXED[g1]=b.dat"))
                .row(text("data"), text("f1"))
                .row(XlsFixture.blank(), text("半角英字"))
                .row(XlsFixture.blank(), text("5"))
                .row(XlsFixture.blank(), text("b"))
                .row(text("SETUP_FIXED=c.dat"))
                .row(text("data"), text("f1"))
                .row(XlsFixture.blank(), text("半角英字"))
                .row(XlsFixture.blank(), text("5"))
                .row(XlsFixture.blank(), text("c"))
                .writeTo(dir());

        // When
        List<String> warnings = new ArrayList<String>();
        TestDataContainer container = readCapturingWarnings(warnings);
        writeYaml(container);

        // Then (i) 警告が 1 件
        assertThat("警告の件数", warnings.size(), is(1));
        String warning = warnings.get(0);
        assertThat(warning, containsString(BOOK));
        assertThat(warning, containsString(SHEET));
        assertThat(warning, containsString("SETUP_FIXED"));
        assertThat(warning, containsString("c.dat"));

        // Then (ii) 出力に c.dat が無い
        assertThat(tableKeys(container), is(java.util.Arrays.asList("/a.dat", "g1/b.dat")));

        // Then (iii) 出力をフレームワークが読んだ結果が、元の .xlsx を読んだ結果と一致する
        assertThat("グループ指定なし",
                fileDump(YamlFrameworkOracle.setupFiles(outDir().toString(), SHEET)),
                is(fileDump(FrameworkOracle.files(dir().toString(), resource(), "", DataType.SETUP_FIXED))));
        assertThat("グループ g1",
                fileDump(YamlFrameworkOracle.setupFiles(outDir().toString(), SHEET, "g1")),
                is(fileDump(FrameworkOracle.files(dir().toString(), resource(), "[g1]", DataType.SETUP_FIXED))));
    }

    // ------------------------------------------------------------------ データタイプが交互

    /**
     * Given: {@code EXPECTED_TABLE=A} ／ {@code EXPECTED_COMPLETE_TABLE=B} ／ {@code EXPECTED_TABLE=C} の順に
     *        並んだ実 {@code .xlsx}。同じデータタイプのブロックの間に、別のデータタイプのブロックが挟まっている。
     * When : 実 {@code .xlsx} を変換ツールで読む。
     * Then : 警告が 1 件出て、出力に {@code C} が無い。
     */
    @Test
    public void warnsAndDropsBlockAfterInterleavedDataType() {
        // Given
        book().row(text("EXPECTED_TABLE=A"))
                .row(text("id"))
                .row(text("a1"))
                .row(text("EXPECTED_COMPLETE_TABLE=B"))
                .row(text("id"))
                .row(text("b1"))
                .row(text("EXPECTED_TABLE=C"))
                .row(text("id"))
                .row(text("c1"))
                .writeTo(dir());

        // When
        List<String> warnings = new ArrayList<String>();
        TestDataContainer container = readCapturingWarnings(warnings);

        // Then (i)
        assertThat("警告の件数", warnings.size(), is(1));
        String warning = warnings.get(0);
        assertThat(warning, containsString(BOOK));
        assertThat(warning, containsString(SHEET));
        assertThat(warning, containsString("EXPECTED_TABLE"));
        assertThat(warning, containsString("C"));

        // Then (ii)
        assertThat(tableKeys(container), is(java.util.Arrays.asList("/A", "/B")));
        for (TestDataBlock block : container.getSections().get(0).getBlocks()) {
            assertThat("読まれた行", ((TableDataBlock) block).getRows().size(), is(1));
        }
    }

    // ------------------------------------------------------------------ 対象外のデータタイプ

    /**
     * Given: {@code MESSAGE=m1} ／ {@code SETUP_TABLE=A} ／ {@code MESSAGE=m1} の順に並んだ実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を変換ツールで読む。
     * Then : 警告は出ない。識別子で 1 件を引くデータタイプ（{@code MESSAGE} ／ {@code LIST_MAP}）は
     *        収集方式が「グループ」ではないため、交互記述の対象外である。
     */
    @Test
    public void doesNotWarnForDataTypesFetchedByIdentifier() {
        // Given
        book().row(text("MESSAGE=m1"))
                .row(text("text-encoding"), text("UTF-8"))
                .row(text("data"), text("f1"))
                .row(XlsFixture.blank(), text("半角英字"))
                .row(XlsFixture.blank(), text("5"))
                .row(XlsFixture.blank(), text("x"))
                .row(text("SETUP_TABLE=A"))
                .row(text("id"))
                .row(text("a1"))
                .row(text("MESSAGE=m1"))
                .row(text("text-encoding"), text("UTF-8"))
                .row(text("data"), text("f1"))
                .row(XlsFixture.blank(), text("半角英字"))
                .row(XlsFixture.blank(), text("5"))
                .row(XlsFixture.blank(), text("y"))
                .writeTo(dir());

        // When
        List<String> warnings = new ArrayList<String>();
        readCapturingWarnings(warnings);

        // Then
        assertThat("警告の件数", warnings.size(), is(0));
    }
}
