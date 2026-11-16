package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import nablarch.test.core.reader.FrameworkOracle;
import nablarch.test.core.reader.yaml.YamlLoader;
import nablarch.test.tool.converter.DataFormat;
import nablarch.test.tool.converter.TestDataConverter;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.yaml.YamlFormatReader;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 識別行にグループ ID を書いた {@code LIST_MAP} を、テスティングフレームワークと同じく読むことを確かめるテスト。
 *
 * <p>
 * フレームワークは {@code LIST_MAP} を ID だけで引き当て、識別行のグループ ID を使わない
 * （{@code LIST_MAP[g1]=lm} も {@code getListMap(..., "lm")} で読める）。同じ ID の {@code LIST_MAP} が
 * 複数あれば、最初のものだけを読む。中間モデルの {@code LIST_MAP} はグループ ID を持たないため、
 * 変換ツールはグループ ID を外し、警告を出して変換を続ける。
 * </p>
 */
public class XlsListMapGroupIdTest {

    /** フィクスチャのブック名。 */
    private static final String BOOK = "listMapGroup";

    /** フィクスチャのシート名。 */
    private static final String SHEET = "s";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** YAML の読み込みのキャッシュをテスト間で残さない。 */
    @After
    public void clearLoaderCache() {
        YamlLoader.clearCacheForTest();
    }

    /**
     * フィクスチャの出力先ディレクトリ。
     *
     * @return ディレクトリ
     */
    private Path dir() {
        return folder.getRoot().toPath();
    }

    /**
     * 変換ツールで読み、WARN 以上のログを集めながら中間モデルを返す。
     *
     * @param warnings 集めたメッセージの受け皿
     * @return 中間モデル
     */
    private TestDataContainer readCapturingWarnings(List<String> warnings) {
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                    warnings.add(record.getMessage());
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        Logger logger = Logger.getLogger(XlsFormatReader.class.getName());
        boolean useParent = logger.getUseParentHandlers();
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
        try {
            return new XlsFormatReader().read(dir().toString(), BOOK + "/" + SHEET);
        } finally {
            logger.removeHandler(handler);
            logger.setUseParentHandlers(useParent);
        }
    }

    /**
     * 中間モデルの唯一のブロックを {@code LIST_MAP} として取り出す。
     *
     * @param container 中間モデル
     * @return {@code LIST_MAP} のブロック
     */
    private static ListMapBlock onlyListMap(TestDataContainer container) {
        List<TestDataBlock> blocks = container.getSections().get(0).getBlocks();
        assertThat("ブロック数", blocks.size(), is(1));
        assertThat(blocks.get(0), is(instanceOf(ListMapBlock.class)));
        return (ListMapBlock) blocks.get(0);
    }

    /**
     * 中間モデルの {@code LIST_MAP} を、フレームワークが返す形（行ごとのマップ）へ写す。
     *
     * @param block {@code LIST_MAP} のブロック
     * @return 行ごとのマップ
     */
    private static List<Map<String, String>> asMaps(ListMapBlock block) {
        List<Map<String, String>> maps = new ArrayList<>();
        for (List<String> row : block.getRows()) {
            Map<String, String> map = new LinkedHashMap<>();
            for (int i = 0; i < block.getColumnNames().size(); i++) {
                map.put(block.getColumnNames().get(i), row.get(i));
            }
            maps.add(map);
        }
        return maps;
    }

    /**
     * Given: 識別行に {@code LIST_MAP[g1]=lm} と書いた実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : フレームワークと同じカラム名・行で読み、グループ ID は持たない。グループ ID を外したことを警告する。
     */
    @Test
    public void readsListMapWithGroupIdByIdentifierLikeFramework() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("LIST_MAP[g1]=lm"))
                .row(text("A"), text("B"))
                .row(text("1"), text("2"))
                .writeTo(dir());

        // When
        List<String> warnings = new ArrayList<>();
        ListMapBlock listMap = onlyListMap(readCapturingWarnings(warnings));

        // Then
        assertThat("グループ ID は持たない", listMap.getGroupId(), is(""));
        assertThat(listMap.getColumnNames(), is(Arrays.asList("A", "B")));
        assertThat(listMap.getRows(), is(Collections.singletonList(Arrays.asList("1", "2"))));
        assertThat("フレームワークと同じ", asMaps(listMap),
                is(FrameworkOracle.listMap(dir().toString(), BOOK + "/" + SHEET, "lm")));
        assertThat("警告の件数", warnings.size(), is(1));
        assertThat(warnings.get(0), containsString("LIST_MAP \"lm\" にグループID \"g1\""));
    }

    /**
     * Given: 同じ ID の {@code LIST_MAP} を、グループ ID を変えて 2 つ書いた実 {@code .xlsx}
     *        （{@code LIST_MAP[g1]=lm}・{@code LIST_MAP[g2]=lm}）。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : フレームワークと同じく最初のものだけを読み、2 つ目を出力しないことを警告する。
     */
    @Test
    public void readsOnlyFirstListMapOfSameIdentifierLikeFramework() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("LIST_MAP[g1]=lm"))
                .row(text("A"))
                .row(text("first"))
                .row()
                .row(text("LIST_MAP[g2]=lm"))
                .row(text("A"))
                .row(text("second"))
                .writeTo(dir());

        // When
        List<String> warnings = new ArrayList<>();
        ListMapBlock listMap = onlyListMap(readCapturingWarnings(warnings));

        // Then
        assertThat(listMap.getRows(), is(Collections.singletonList(Collections.singletonList("first"))));
        assertThat("フレームワークと同じ", asMaps(listMap),
                is(FrameworkOracle.listMap(dir().toString(), BOOK + "/" + SHEET, "lm")));
        assertThat("警告の件数", warnings.size(), is(2));
        assertThat(warnings.get(1), containsString("識別子 \"lm\" の LIST_MAP が複数あります"));
    }

    /**
     * Given: 識別行に {@code LIST_MAP[g1]=lm} と書いた実 {@code .xlsx}。
     * When : YAML 形式へ変換し、出力した YAML を読み戻す。
     * Then : スキーマの検証に通り（{@code group_id} を出力しない）、行も保たれる。
     */
    @Test
    public void convertsListMapWithGroupIdToReadableYaml() {
        // Given
        XlsFixture.book(BOOK).sheet(SHEET)
                .row(text("LIST_MAP[g1]=lm"))
                .row(text("A"))
                .row(text("1"))
                .writeTo(dir());
        Path out = dir().resolve("out");

        // When
        TestDataConverter.convert(DataFormat.XLS, DataFormat.YAML, dir(), out);

        // Then
        ListMapBlock back = onlyListMap(new YamlFormatReader().read(out.resolve(BOOK).toString(), SHEET));
        assertThat(back.getColumnNames(), is(Collections.singletonList("A")));
        assertThat(back.getRows(), is(Collections.singletonList(Collections.singletonList("1"))));
    }
}
