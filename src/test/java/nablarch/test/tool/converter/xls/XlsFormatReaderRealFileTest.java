package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.blank;
import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.TestCoreReaderAdapter;
import nablarch.test.tool.converter.model.FieldDef;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.MessageDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 辺①（Excel→中間モデル）の軸A（{@link DataType} 14 種）・軸B（{@code TestDataBlock} の具象 4 種）・
 * 軸C（中間モデル 21 フィールド）と、軸E の多重度のうち #21 が埋めたもの
 * （E-2 ブロック内行数 0 件／E-3 ファイル内レコードレイアウト数 0 件・複数件）を、
 * <b>実 {@code .xlsx} を入力に</b>固定するテスト。
 *
 * <p>
 * {@code XlsFormatReaderTest}（既存 33 件）は内部 Fake リーダに {@code List<List<String>>} の canned 行を
 * 与えるため、実セル → 文字列行の区間を通らない。本クラスは {@link XlsFixture} が POI で組み立てた
 * 実 {@code .xlsx} を本番配線の {@link XlsFormatReader}（{@code PoiXlsReader}）へ食わせる。
 * Excel 上の記法と入力値は既存 33 件（および {@code RoundTripTest}）を参考にしており、混在シート・送信同期の
 * 入力はほぼそのまま移植したものである。組み直したのは<b>入力経路</b>（実 {@code .xlsx} → 本番配線）と
 * アサーション対象であって、記法そのものではない。
 * </p>
 *
 * <p>
 * 各テストの Javadoc には、そのテストが担保する軸要素の ID
 * （{@code .rn/ntf-test-data-converter/coverage/inventory.md} の A-01〜A-14／B-1〜B-4／C-01〜C-21／E-1〜E-4）を記す。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>期待される仕様ではない。
 * 妥当でないと判断した挙動は {@code .rn/ntf-test-data-converter/coverage/issues.md} に課題として記録して
 * あり、実装（src/main）は変更していない。
 * </p>
 *
 * <p>
 * <b>本クラスが扱わない軸要素と理由（テストコード側の索引。根拠の本文は
 * {@code coverage/inventory.md} §1.3 と {@code coverage/issues.md} の「到達不能」表にあり、そちらが原本）:</b>
 * </p>
 * <table border="1">
 *   <caption>実 {@code .xlsx} 経路で到達不能な軸要素</caption>
 *   <tr><th>軸要素</th><th>扱い</th><th>理由</th></tr>
 *   <tr>
 *     <td>A-01 {@code DEFAULT}</td><td>到達不能</td>
 *     <td>{@link TestCoreReaderAdapter} の {@code HeaderCollector} が {@code DEFAULT} と判定した行を
 *         {@code continue} でスキップするため、ヘッダ一覧に {@code DEFAULT} のブロックが載らない。
 *         {@link XlsFormatReader#read} はヘッダ一覧の各要素しか見ないため、リーダ経路では
 *         {@code DEFAULT} のブロックが生成されない。</td>
 *   </tr>
 *   <tr>
 *     <td>C-02 {@code sections} の「空」「複数」</td><td>到達不能</td>
 *     <td>{@link XlsFormatReader#read} は必ず {@code Collections.singletonList(section)} を返すため
 *         常に 1 件。</td>
 *   </tr>
 *   <tr>
 *     <td>C-11 {@code FileDataBlock.directives} 空 ／ C-13 {@code MessageDataBlock.directives} 空</td>
 *     <td>到達不能</td>
 *     <td>本体 {@code DataFile} のコンストラクタが必ず {@code setDirective("file-type", getFileType())}
 *         を実行するため、Excel にディレクティブ行が 1 行も無くても空 Map にならない（{@code issues.md}
 *         XLS-07）。根拠は {@link #readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook}
 *         （C-11）と {@link #readsAllFourSendSyncMessageTypesFromRealBook}（C-13）がテストで示す。</td>
 *   </tr>
 *   <tr>
 *     <td>C-16 {@code RecordLayout.recordType} 省略（{@code null}）</td><td>到達不能</td>
 *     <td>空セルは {@code PoiXlsReader} が {@code ""} を返すため {@code null} にならない
 *         （{@code issues.md} XLS-06）。根拠は
 *         {@link #readsOmittedRecordTypeAsEmptyStringFromRealBook} がテストで示す。</td>
 *   </tr>
 *   <tr>
 *     <td>C-17 {@code RecordLayout.fields} 空</td><td>到達不能</td>
 *     <td>フィールドを 0 件にするには名前行を 1 列（レコード種別セルのみ）にするしかないが、本体
 *         {@code DataFileParser} が「{@code directive or data names row must have two columns at least.}」で
 *         弾く（{@code issues.md} の「到達不能」表）。根拠は
 *         {@code XlsFormatReaderInvalidInputTest#failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook}
 *         と {@code #failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook} がテストで示す
 *         （固定長とメッセージは経路が別のため 2 メソッドに分かれている）。</td>
 *   </tr>
 *   <tr>
 *     <td>C-20 {@code FieldDef.type} 省略（{@code null}）</td><td>到達不能</td>
 *     <td>型行が名前行より短いと {@code DataFileFragment#assertSameSizeAsNames} が、型セルが中間位置で
 *         空だと {@code BasicDataTypeMapping#convertToFrameworkExpression} が、それぞれ
 *         {@code IllegalArgumentException} を投げる（{@code issues.md} の「到達不能」表）。根拠は
 *         {@code XlsFormatReaderInvalidInputTest#failsWhenTypeRowIsShorterThanNameRowInRealBook} と
 *         {@code #failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook} がテストで示す。</td>
 *   </tr>
 *   <tr>
 *     <td>E-4 コンテナ内セクション数「複数」</td><td>到達不能</td>
 *     <td>{@link XlsFormatReader#read} は {@code "ブック名/シート名"} の 1 シート単位 API であり、
 *         必ず 1 セクションだけを返す（C-02 と同じ根拠）。</td>
 *   </tr>
 * </table>
 *
 * <p>
 * なお C-08 {@code columnNames} 空は #21 送りではなく<b>本クラスで担保する</b>
 * （{@link #readsEmptyColumnNamesFromMarkerOnlyTableInRealBook} ほか）。マーカー列だけのテーブルで
 * 到達でき、軸E の 4 観点（E-1〜E-4）に対応する要素を持たないためである。
 * </p>
 *
 * <p>
 * <b>軸F（異常系）は本クラスでは扱わない。</b>{@code XlsFormatReaderInvalidInputTest}（タスク #21）が、
 * 壊れた入力・欠けた入力に対する例外と継続時の結果を固定する。
 * </p>
 *
 * @author kiyobot
 */
public class XlsFormatReaderRealFileTest {

    /** フィクスチャの既定ブック名。 */
    private static final String BOOK = "realBook";

    /** フィクスチャの既定シート名。 */
    private static final String SHEET = "sheet1";

    /**
     * フィクスチャ {@code .xlsx} の出力先。
     *
     * <p>
     * {@link TemporaryFolder} はテストメソッドごとに別ディレクトリを与えるため、全メソッドが同じ
     * ブック名を使ってもファイルパスは衝突しない。
     * </p>
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // ------------------------------------------------------------------ helpers

    /**
     * 既定のブック名・シート名でフィクスチャの組み立てを開始する。
     *
     * @return フィクスチャビルダ
     */
    private static XlsFixture book() {
        return XlsFixture.book(BOOK).sheet(SHEET);
    }

    /**
     * フィクスチャ {@code .xlsx} の出力先ディレクトリ。読み書きとも本メソッドだけを使う。
     *
     * @return ディレクトリ
     */
    private Path dir() {
        return folder.getRoot().toPath();
    }

    /**
     * 既定のブック／シートを実 {@link XlsFormatReader} で読む。
     *
     * @return 中間モデル
     */
    private TestDataContainer read() {
        return read(BOOK, SHEET);
    }

    /**
     * 指定のブック／シートを実 {@link XlsFormatReader} で読む。
     *
     * @param bookName  ブック名
     * @param sheetName シート名
     * @return 中間モデル
     */
    private TestDataContainer read(String bookName, String sheetName) {
        return new XlsFormatReader().read(dir().toString(), bookName + "/" + sheetName);
    }

    /**
     * 既定のブック／シートを読み、唯一のセクションのブロック一覧を返す。
     *
     * @return ブロック一覧
     */
    private List<TestDataBlock> blocks() {
        TestDataContainer container = read();
        assertThat("セクション数", container.getSections().size(), is(1));
        return container.getSections().get(0).getBlocks();
    }

    /**
     * 既定のブック／シートを読み、唯一のブロックが期待する実装クラスであることを確かめて返す。
     *
     * <p>
     * 素キャスト（失敗時に {@code ClassCastException} しか出ない）を避け、どのクラスが来たかが
     * 失敗メッセージに出るようにするためのヘルパ。
     * </p>
     *
     * @param <T>      期待する実装クラス
     * @param expected 期待する実装クラス
     * @return 唯一のブロック
     */
    private <T extends TestDataBlock> T onlyBlock(Class<T> expected) {
        List<TestDataBlock> blocks = blocks();
        assertThat("ブロック数", blocks.size(), is(1));
        TestDataBlock block = blocks.get(0);
        assertThat("唯一のブロックの実装クラス", block, is(instanceOf(expected)));
        return expected.cast(block);
    }

    /**
     * レコードレイアウトのフィールド名を並び順に取り出す。
     *
     * @param record レコードレイアウト
     * @return フィールド名一覧
     */
    private static List<String> fieldNames(RecordLayout record) {
        List<String> names = new ArrayList<>();
        for (FieldDef field : record.getFields()) {
            names.add(field.getName());
        }
        return names;
    }

    // ------------------------------------------------------------------ 軸A テーブル系・LIST_MAP

    /**
     * Given: グループ ID を持たない {@code SETUP_TABLE} ブロック 1 件を含む実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@link TableDataBlock} が生成され、データタイプ・識別子・列名・行が入る。
     *        グループ ID は省略時の表現である空文字になる。
     *
     * <p>担保する軸要素: A-02／B-1／C-05／C-06（省略）／C-07／C-08（非空）／C-09（非空）。</p>
     */
    @Test
    public void readsSetupTableBlockFromRealBook() {
        // Given
        book().row(text("SETUP_TABLE=USERS"))
                .row(text("USER_ID"), text("AGE"))
                .row(text("U1"), text("20"))
                .row(text("U2"), text("30"))
                .writeTo(dir());

        // When
        TableDataBlock table = onlyBlock(TableDataBlock.class);

        // Then
        assertThat(table.getDataType(), is(DataType.SETUP_TABLE_DATA));
        assertThat(table.getGroupId(), is(""));
        assertThat(table.getIdentifier(), is("USERS"));
        assertThat(table.getColumnNames(), is(Arrays.asList("USER_ID", "AGE")));
        assertThat(table.getRows(), is(Arrays.asList(
                Arrays.asList("U1", "20"),
                Arrays.asList("U2", "30"))));
    }

    /**
     * Given: グループ ID {@code [g1]} を持つ {@code EXPECTED_TABLE} ブロックを含む実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : データタイプが {@code EXPECTED_TABLE_DATA}、グループ ID が角括弧付きのまま入る。
     *
     * <p>担保する軸要素: A-03／C-06（値あり）。</p>
     */
    @Test
    public void readsExpectedTableBlockWithGroupIdFromRealBook() {
        // Given
        book().row(text("EXPECTED_TABLE[g1]=ROLES"))
                .row(text("ROLE_NAME"))
                .row(text("admin"))
                .writeTo(dir());

        // When
        TableDataBlock table = onlyBlock(TableDataBlock.class);

        // Then
        assertThat(table.getDataType(), is(DataType.EXPECTED_TABLE_DATA));
        assertThat(table.getGroupId(), is("[g1]"));
        assertThat(table.getIdentifier(), is("ROLES"));
        assertThat(table.getRows(), is(Arrays.asList(Arrays.asList("admin"))));
    }

    /**
     * Given: {@code EXPECTED_COMPLETE_TABLE} マーカー（{@code DataType#EXPECTED_COMPLETED} の記法名）を
     *        持つ実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : データタイプ {@code EXPECTED_COMPLETED} の {@link TableDataBlock} が生成される。
     *
     * <p>担保する軸要素: A-04。</p>
     */
    @Test
    public void readsExpectedCompletedTableBlockFromRealBook() {
        // Given
        book().row(text("EXPECTED_COMPLETE_TABLE=DEPTS"))
                .row(text("DEPT_ID"))
                .row(text("D1"))
                .writeTo(dir());

        // When
        TableDataBlock table = onlyBlock(TableDataBlock.class);

        // Then
        assertThat(table.getDataType(), is(DataType.EXPECTED_COMPLETED));
        assertThat(table.getIdentifier(), is("DEPTS"));
        assertThat(table.getRows(), is(Arrays.asList(Arrays.asList("D1"))));
    }

    /**
     * Given: {@code LIST_MAP} ブロック 1 件を含む実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@link ListMapBlock} が生成され、列名が Excel の記述順（アルファベット順ではない）で入る。
     *
     * <p>担保する軸要素: A-05／B-2／C-05／C-07／C-08（非空）／C-09（非空）。</p>
     */
    @Test
    public void readsListMapBlockFromRealBook() {
        // Given: 列順は Z, A, M（アルファベット順なら A, M, Z になるはず）
        book().row(text("LIST_MAP=testShots"))
                .row(text("Z"), text("A"), text("M"))
                .row(text("z1"), text("a1"), text("m1"))
                .writeTo(dir());

        // When
        ListMapBlock listMap = onlyBlock(ListMapBlock.class);

        // Then
        assertThat(listMap.getDataType(), is(DataType.LIST_MAP));
        assertThat(listMap.getGroupId(), is(""));
        assertThat(listMap.getIdentifier(), is("testShots"));
        assertThat(listMap.getColumnNames(), is(Arrays.asList("Z", "A", "M")));
        assertThat(listMap.getRows(), is(Arrays.asList(Arrays.asList("z1", "a1", "m1"))));
    }

    // ------------------------------------------------------------------ 軸C columnNames 空

    /**
     * Given: マーカー列 {@code [no]} だけを持つ（データ列が 1 つも無い）{@code SETUP_TABLE} の実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : マーカー列は本体 {@code HeaderLine#getEffectiveColumnNames()} が除外するため列名は 0 件になる。
     *        行は 0 件にはならず、<b>セルを 1 つも持たない行</b>がデータ行の件数ぶん入る。
     *
     * <p>
     * 担保する軸要素: C-08（空）。本タスクの当初分類では「軸E の 0 件と重なる」として #21 送りにしていたが、
     * 軸E の 4 観点（E-1 セクション内ブロック数／E-2 ブロック内行数／E-3 ファイル内レコードレイアウト数／
     * E-4 コンテナ内セクション数）に「列名 0 件」に対応する要素は無い。本クラスで担保する。
     * </p>
     *
     * <p>
     * {@code rows} が空リストではなく「セル 0 個の行が 1 件」になる点は実測どおりに固定した。
     * この中間モデルを書き戻すと当該行は失われる（往復非安定）ため、課題として
     * {@code coverage/issues.md} の XLS-08 に記録した（修正はしない）。
     * </p>
     */
    @Test
    public void readsEmptyColumnNamesFromMarkerOnlyTableInRealBook() {
        // Given
        book().row(text("SETUP_TABLE=T"))
                .row(text("[no]"))
                .row(text("1"))
                .row(text("2"))
                .writeTo(dir());

        // When
        TableDataBlock table = onlyBlock(TableDataBlock.class);

        // Then
        assertThat("マーカー列は有効カラム名から除外される", table.getColumnNames(), is(Collections.<String>emptyList()));
        assertThat("データ行 2 件ぶん、セル 0 個の行が入る（XLS-08）",
                table.getRows(), is(Arrays.asList(Collections.<String>emptyList(), Collections.<String>emptyList())));
    }

    /**
     * Given: マーカー列 {@code [no]} だけを持つ {@code LIST_MAP} の実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : テーブル系と同じく列名 0 件・セル 0 個の行 1 件になる（経路が別なので個別に固定する）。
     *
     * <p>担保する軸要素: C-08（空。{@link ListMapBlock} 側の経路）。</p>
     */
    @Test
    public void readsEmptyColumnNamesFromMarkerOnlyListMapInRealBook() {
        // Given
        book().row(text("LIST_MAP=lm"))
                .row(text("[no]"))
                .row(text("1"))
                .writeTo(dir());

        // When
        ListMapBlock listMap = onlyBlock(ListMapBlock.class);

        // Then
        assertThat("マーカー列は有効カラム名から除外される", listMap.getColumnNames(), is(Collections.<String>emptyList()));
        assertThat("データ行 1 件ぶん、セル 0 個の行が入る（XLS-08）",
                listMap.getRows(), is(Arrays.asList(Collections.<String>emptyList())));
    }

    // ------------------------------------------------------------------ 軸A ファイル系

    /**
     * Given: ディレクティブ行・レコード種別・型行・長さ行を持つ {@code SETUP_FIXED} ブロックの
     *        実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@link FileDataBlock}（{@code FIXED}）が生成され、ディレクティブ・レコードレイアウト・
     *        フィールド定義・値行が入る。ディレクティブには Excel に書いた {@code text-encoding} に加え、
     *        本体器が既定で注入する {@code file-type} が現れる。
     *
     * <p>
     * 担保する軸要素: A-06／B-3／C-10（FIXED）／C-11（非空）／C-12（非空）／C-16（値あり）／
     * C-17（非空）／C-18（非空）／C-19／C-20（値あり）／C-21（値あり）。
     * </p>
     */
    @Test
    public void readsSetupFixedFileBlockFromRealBook() {
        // Given
        book().row(text("SETUP_FIXED=test.dat"))
                .row(text("text-encoding"), text("UTF-8"))
                .row(text("data"), text("field1"), text("field2"))
                .row(blank(), text("半角英字"), text("半角英字"))
                .row(blank(), text("10"), text("5"))
                .row(blank(), text("abc"), text("de"))
                .writeTo(dir());

        // When
        FileDataBlock file = onlyBlock(FileDataBlock.class);

        // Then
        assertThat(file.getDataType(), is(DataType.SETUP_FIXED));
        assertThat(file.getIdentifier(), is("test.dat"));
        assertThat(file.getFileType(), is(FileDataBlock.FileType.FIXED));
        assertThat(file.getDirectives().get("text-encoding"), is("UTF-8"));
        // Excel に書いていない file-type が器の既定値として現れる（issues.md XLS-07）。
        assertThat(file.getDirectives().get("file-type"), is("Fixed"));
        assertDirectiveCount(file.getDirectives(), 2);

        assertThat("レコードレイアウト数", file.getRecords().size(), is(1));
        RecordLayout record = file.getRecords().get(0);
        assertThat(record.getRecordType(), is("data"));
        assertThat(fieldNames(record), is(Arrays.asList("field1", "field2")));
        // 型・長さは生行の原文（器が正規化した FW シンボル・実バイト長ではない）
        assertThat(record.getFields().get(0).getType(), is("半角英字"));
        assertThat(record.getFields().get(0).getLength(), is("10"));
        assertThat(record.getFields().get(1).getLength(), is("5"));
        assertThat(record.getRows(), is(Arrays.asList(Arrays.asList("abc", "de"))));
    }

    /**
     * Given: 長さ行に省略記法 {@code -} を含む {@code SETUP_FIXED} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 器は {@code -} を実バイト長（この入力なら {@code 4}）へ正規化するが、中間モデルには
     *        <b>生行の原文 {@code "-"}</b> が入る。
     *
     * <p>
     * 担保する軸要素: C-21（値あり・省略記法 {@code -}）。原文復元ロジック
     * （{@link XlsFormatReader} が生行から型・長さ・レコード種別を取り直す経路）は
     * {@code -} のケースこそが要であり、それを実 {@code .xlsx} 入力で通す唯一のテストである
     * （Fake リーダ経路の {@code XlsFormatReaderTest#readRestoresOriginalRecordTypeTypeAndOmittedLengthFromRawLines}
     * と同じ入力を実ファイル経路で通し直したもの）。
     * </p>
     */
    @Test
    public void readsOmittedFieldLengthNotationFromRealBook() {
        // Given: f1 の長さは省略記法 "-"（値 "abcd" から器が 4 を導出する）
        book().row(text("SETUP_FIXED=om.dat"))
                .row(text("text-encoding"), text("UTF-8"))
                .row(text("rt"), text("f1"), text("f2"))
                .row(blank(), text("半角英字"), text("半角英字"))
                .row(blank(), text("-"), text("5"))
                .row(blank(), text("abcd"), text("xy"))
                .writeTo(dir());

        // When
        FileDataBlock file = onlyBlock(FileDataBlock.class);

        // Then
        RecordLayout record = file.getRecords().get(0);
        assertThat(record.getRecordType(), is("rt"));
        assertThat(fieldNames(record), is(Arrays.asList("f1", "f2")));
        assertThat("長さ省略記法は原文のまま（器が上書きする実バイト長 \"4\" ではない）",
                record.getFields().get(0).getLength(), is("-"));
        assertThat(record.getFields().get(1).getLength(), is("5"));
        assertThat(record.getFields().get(0).getType(), is("半角英字"));
        assertThat(record.getRows(), is(Arrays.asList(Arrays.asList("abcd", "xy"))));
    }

    /**
     * Given: ディレクティブ行を 1 行も持たない {@code EXPECTED_FIXED} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : データタイプ {@code EXPECTED_FIXED} の {@link FileDataBlock} が生成される。
     *        ディレクティブ行が無くてもディレクティブは空にならず、器が注入する {@code file-type} だけを持つ。
     *
     * <p>
     * 担保する軸要素: A-07／C-11（非空。「空 Map」が到達不能であることの根拠でもある。
     * 本体 {@code DataFile} のコンストラクタが必ず {@code setDirective("file-type", getFileType())} を
     * 実行するため、Excel 由来のディレクティブ行が 0 行でも空 Map にならない）。
     * </p>
     */
    @Test
    public void readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook() {
        // Given: 名前行は「レコード種別 rec ＋ フィールド名 f1」（[g1] 等のグループ ID とは無関係）
        book().row(text("EXPECTED_FIXED=expected.dat"))
                .row(text("rec"), text("f1"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("3"))
                .row(blank(), text("xyz"))
                .writeTo(dir());

        // When
        FileDataBlock file = onlyBlock(FileDataBlock.class);

        // Then
        assertThat(file.getDataType(), is(DataType.EXPECTED_FIXED));
        assertThat(file.getIdentifier(), is("expected.dat"));
        assertThat(file.getFileType(), is(FileDataBlock.FileType.FIXED));
        assertThat(file.getDirectives().get("file-type"), is("Fixed"));
        assertDirectiveCount(file.getDirectives(), 1);
        assertThat(file.getRecords().get(0).getRecordType(), is("rec"));
        assertThat(fieldNames(file.getRecords().get(0)), is(Arrays.asList("f1")));
        assertThat(file.getRecords().get(0).getRows(), is(Arrays.asList(Arrays.asList("xyz"))));
    }

    /**
     * Given: 長さ行を持たない {@code SETUP_VARIABLE} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@link FileDataBlock}（{@code VARIABLE}）が生成され、長さは省略時の表現である
     *        {@code null} になる。
     *
     * <p>担保する軸要素: A-08／C-10（VARIABLE）／C-21（省略＝{@code null}）。</p>
     */
    @Test
    public void readsSetupVariableFileBlockWithoutFieldLengthFromRealBook() {
        // Given
        book().row(text("SETUP_VARIABLE=in.csv"))
                .row(text("data"), text("f1"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("val"))
                .writeTo(dir());

        // When
        FileDataBlock file = onlyBlock(FileDataBlock.class);

        // Then
        assertThat(file.getDataType(), is(DataType.SETUP_VARIABLE));
        assertThat(file.getFileType(), is(FileDataBlock.FileType.VARIABLE));
        RecordLayout record = file.getRecords().get(0);
        assertThat(record.getFields().get(0).getName(), is("f1"));
        assertThat(record.getFields().get(0).getType(), is("半角英字"));
        assertThat(record.getFields().get(0).getLength(), is(nullValue()));
        assertThat(record.getRows(), is(Arrays.asList(Arrays.asList("val"))));
    }

    /**
     * Given: グループ ID {@code [g2]} と {@code record-separator} ディレクティブを持つ
     *        {@code EXPECTED_VARIABLE} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : データタイプ {@code EXPECTED_VARIABLE} の {@link FileDataBlock}（{@code VARIABLE}）が生成され、
     *        {@code record-separator} はシンボル {@code CRLF} へ逆正規化された値で入る。
     *        可変長では {@code field-separator} も器の既定値として現れる。
     *
     * <p>担保する軸要素: A-09／C-06（値あり）／C-11（非空）。</p>
     */
    @Test
    public void readsExpectedVariableFileBlockWithGroupIdFromRealBook() {
        // Given
        book().row(text("EXPECTED_VARIABLE[g2]=out.csv"))
                .row(text("record-separator"), text("CRLF"))
                .row(text("data"), text("f1"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("val"))
                .writeTo(dir());

        // When
        FileDataBlock file = onlyBlock(FileDataBlock.class);

        // Then
        assertThat(file.getDataType(), is(DataType.EXPECTED_VARIABLE));
        assertThat(file.getGroupId(), is("[g2]"));
        assertThat(file.getIdentifier(), is("out.csv"));
        assertThat(file.getFileType(), is(FileDataBlock.FileType.VARIABLE));
        assertThat(file.getDirectives().get("record-separator"), is("CRLF"));
        // Excel に書いていない file-type / field-separator が器の既定値として現れる（issues.md XLS-07）。
        assertThat(file.getDirectives().get("file-type"), is("Variable"));
        assertThat(file.getDirectives().get("field-separator"), is(","));
        assertDirectiveCount(file.getDirectives(), 3);
    }

    // ------------------------------------------------------------------ 軸A メッセージ系

    /**
     * Given: ディレクティブ行・FW 制御ヘッダ行・本文を持つ {@code MESSAGE} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@link MessageDataBlock} が生成され、ディレクティブ・FW ヘッダ・本文レコードが入る。
     *        ディレクティブには Excel に書いた {@code text-encoding} に加え、器が注入する
     *        {@code file-type} が現れる（本文は固定長ファイルとして読まれるため）。
     *
     * <p>
     * 担保する軸要素: A-10／B-4／C-13（非空）／C-14（非空）／C-15（非空）／C-16（値あり）／
     * C-17（非空）／C-18（非空）／C-19／C-20（値あり）／C-21（値あり）。
     * </p>
     */
    @Test
    public void readsMessageBlockFromRealBook() {
        // Given
        book().row(text("MESSAGE=msg1"))
                .row(text("text-encoding"), text("UTF-8"))
                .row(text("requestId"), text("R1"))
                .row(text("data"), text("body1"), text("body2"))
                .row(blank(), text("半角英字"), text("半角英字"))
                .row(blank(), text("10"), text("5"))
                .row(blank(), text("x"), text("y"))
                .writeTo(dir());

        // When
        MessageDataBlock message = onlyBlock(MessageDataBlock.class);

        // Then
        assertThat(message.getDataType(), is(DataType.MESSAGE));
        assertThat(message.getGroupId(), is(""));
        assertThat(message.getIdentifier(), is("msg1"));
        assertThat(message.getDirectives().get("text-encoding"), is("UTF-8"));
        // Excel に書いていない file-type が器の既定値として現れる（issues.md XLS-07）。
        assertThat(message.getDirectives().get("file-type"), is("Fixed"));
        assertDirectiveCount(message.getDirectives(), 2);
        assertThat(message.getFwHeaderFields().get("requestId"), is("R1"));
        assertThat("FW 制御ヘッダの件数", message.getFwHeaderFields().size(), is(1));

        assertThat("レコードレイアウト数", message.getRecords().size(), is(1));
        RecordLayout record = message.getRecords().get(0);
        assertThat(record.getRecordType(), is("data"));
        assertThat(fieldNames(record), is(Arrays.asList("body1", "body2")));
        assertThat(record.getFields().get(0).getType(), is("半角英字"));
        assertThat(record.getFields().get(0).getLength(), is("10"));
        assertThat(record.getRows(), is(Arrays.asList(Arrays.asList("x", "y"))));
    }

    /**
     * Given: 送信同期メッセージ 4 種（要求ヘッダ／要求本文／応答ヘッダ／応答本文）を持つ実 {@code .xlsx}。
     *        識別子は 4 種それぞれ別（{@code RM01}〜{@code RM04}）にしてブロックの取り違えを検出可能にする。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 4 種すべてが {@link MessageDataBlock} として生成され、データタイプ・グループ ID・識別子が
     *        1 対 1 で対応する。送信系に FW 制御ヘッダは無く {@code fwHeaderFields} は空 Map になるが、
     *        ディレクティブ行が 1 行も無くても {@code directives} は空にならず {@code {file-type=Fixed}} になる。
     *        要求ヘッダ 1 種については {@link #readsMessageBlockFromRealBook} と同水準まで内容を固定する。
     *
     * <p>担保する軸要素: A-11／A-12／A-13／A-14／C-06（値あり）／C-07／C-13（非空）／C-14（空）／
     * C-15（非空）／C-16（値あり）／C-17（非空）／C-18（非空）／C-19／C-20（値あり）／C-21（値あり）。</p>
     *
     * <p>
     * この経路（{@code XlsFormatReader#readSendSyncBlocks}）は識別子に本体器の {@code DataFile#getPath()}
     * を使う独自経路であり、テーブル系・ファイル系とは別に固定する必要がある。
     * </p>
     *
     * <p>
     * <b>レコード種別が {@code "no"} になる点について。</b>送信同期の名前行の先頭セルはレコード種別では
     * なくメタ列ヘッダ {@code no} だが、原文復元は名前行の先頭セルを一律にレコード種別として採るため
     * {@code "no"} が入る（実測）。これは課題として記録していない。この値は Excel へ書き戻す際に
     * メタ列ヘッダ {@code no} を再生するのに使われ、実 {@code .xlsx} → 中間モデル → 実 {@code .xlsx} →
     * 中間モデルで安定することをプローブで確認した（{@code null} だとヘッダが失われて往復が壊れる）。
     * すなわち本経路では load-bearing であり、既存テスト
     * {@code XlsFormatReaderTest#readPreservesErrorModeRowInSendSyncMessage} の判定（良性）と一致する。
     * </p>
     */
    @Test
    public void readsAllFourSendSyncMessageTypesFromRealBook() {
        // Given
        book().row(text("EXPECTED_REQUEST_HEADER_MESSAGES[case1]=RM01"))
                .row(text("no"), text("requestId"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("20"))
                .row(text("1"), text("RM01"))
                .row(text("EXPECTED_REQUEST_BODY_MESSAGES[case1]=RM02"))
                .row(text("no"), text("userId"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("10"))
                .row(text("1"), text("user01"))
                .row(text("RESPONSE_HEADER_MESSAGES[res_case1]=RM03"))
                .row(text("no"), text("requestId"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("20"))
                .row(text("1"), text("RM03"))
                .row(text("RESPONSE_BODY_MESSAGES[res_case1]=RM04"))
                .row(text("no"), text("failureCode"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("20"))
                .row(text("1"), text("0"))
                .writeTo(dir());

        // When
        List<TestDataBlock> blocks = blocks();

        // Then
        assertThat("送信同期ブロック数", blocks.size(), is(4));
        Map<DataType, MessageDataBlock> byType = new EnumMap<>(DataType.class);
        for (TestDataBlock block : blocks) {
            assertThat("送信同期ブロックの実装クラス", block, is(instanceOf(MessageDataBlock.class)));
            MessageDataBlock message = (MessageDataBlock) block;
            byType.put(message.getDataType(), message);
            // 送信系に FW 制御ヘッダは無い。これは Excel の入力内容ではなく経路の性質で、
            // XlsFormatReader が送信同期ブロックには常に空 Map を与えるため入力によらず空になる。
            assertThat("送信同期 " + message.getDataType() + " の FW 制御ヘッダ",
                    message.getFwHeaderFields(), is(Collections.<String, String>emptyMap()));
            // ディレクティブ行を 1 行も書いていないが、器が注入する file-type だけは必ず現れる
            // （issues.md XLS-07。C-13「MessageDataBlock.directives 空」が到達不能である根拠）。
            assertThat("送信同期 " + message.getDataType() + " の file-type",
                    message.getDirectives().get("file-type"), is("Fixed"));
            assertDirectiveCount(message.getDirectives(), 1);
        }
        assertThat("4 種すべてが揃うこと", byType.size(), is(4));

        assertGroupIdAndIdentifier(byType.get(DataType.EXPECTED_REQUEST_HEADER_MESSAGES), "[case1]", "RM01");
        assertGroupIdAndIdentifier(byType.get(DataType.EXPECTED_REQUEST_BODY_MESSAGES), "[case1]", "RM02");
        assertGroupIdAndIdentifier(byType.get(DataType.RESPONSE_HEADER_MESSAGES), "[res_case1]", "RM03");
        assertGroupIdAndIdentifier(byType.get(DataType.RESPONSE_BODY_MESSAGES), "[res_case1]", "RM04");

        // 要求ヘッダ 1 種は MESSAGE と同水準（レコード種別／フィールド／値行）まで固定する。
        MessageDataBlock requestHeader = byType.get(DataType.EXPECTED_REQUEST_HEADER_MESSAGES);
        assertThat("要求ヘッダのレコードレイアウト数", requestHeader.getRecords().size(), is(1));
        RecordLayout record = requestHeader.getRecords().get(0);
        // 名前行の先頭セル（メタ列ヘッダ no）がレコード種別として入る（上記 Javadoc 参照）
        assertThat(record.getRecordType(), is("no"));
        // no 列（メタ情報）はフィールドから脱落する
        assertThat(fieldNames(record), is(Arrays.asList("requestId")));
        assertThat(record.getFields().get(0).getType(), is("半角英字"));
        assertThat(record.getFields().get(0).getLength(), is("20"));
        // 値行も no 列の値（"1"）が脱落する
        assertThat(record.getRows(), is(Arrays.asList(Arrays.asList("RM01"))));
    }

    // ------------------------------------------------------------------ 軸B

    /**
     * Given: テーブル・LIST_MAP・固定長ファイル・MESSAGE が 1 シートに混在する実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code TestDataBlock} の具象 4 種が 1 セクションに揃い、各ブロックが自分の識別子を持つ。
     *
     * <p>担保する軸要素: B-1／B-2／B-3／B-4（実ファイル経由での生成）／C-04（非空）／C-07。</p>
     *
     * <p>
     * {@link XlsFormatReader#read} は (データタイプ, グループ) 単位で本体 API を一括呼び出しし、
     * 同じキーの 2 回目以降を重複排除する非自明な処理をしている。識別子まで突き合わせることで、
     * ブロックの取り違え・取りこぼしを検出できるようにする。
     * </p>
     */
    @Test
    public void readsFourBlockImplementationsFromOneRealSheet() {
        // Given
        book().row(text("SETUP_TABLE=T"))
                .row(text("C"))
                .row(text("v"))
                .row(text("LIST_MAP=lm"))
                .row(text("K"))
                .row(text("1"))
                .row(text("SETUP_FIXED=f.dat"))
                .row(text("data"), text("f1"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("5"))
                .row(blank(), text("x"))
                .row(text("MESSAGE=m"))
                .row(text("requestId"), text("R"))
                .row(text("data"), text("b1"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("3"))
                .row(blank(), text("y"))
                .writeTo(dir());

        // When
        List<TestDataBlock> blocks = blocks();

        // Then
        List<Class<?>> kinds = new ArrayList<>();
        Map<Class<?>, String> identifiers = new HashMap<>();
        for (TestDataBlock block : blocks) {
            kinds.add(block.getClass());
            identifiers.put(block.getClass(), block.getIdentifier());
        }
        assertThat("1 シートから生成されたブロック数", kinds.size(), is(4));
        assertThat(kinds, hasItem((Class<?>) TableDataBlock.class));
        assertThat(kinds, hasItem((Class<?>) ListMapBlock.class));
        assertThat(kinds, hasItem((Class<?>) FileDataBlock.class));
        assertThat(kinds, hasItem((Class<?>) MessageDataBlock.class));
        assertThat("TableDataBlock の識別子", identifiers.get(TableDataBlock.class), is("T"));
        assertThat("ListMapBlock の識別子", identifiers.get(ListMapBlock.class), is("lm"));
        assertThat("FileDataBlock の識別子", identifiers.get(FileDataBlock.class), is("f.dat"));
        assertThat("MessageDataBlock の識別子", identifiers.get(MessageDataBlock.class), is("m"));
    }

    // ------------------------------------------------------------------ 軸C コンテナ・セクション

    /**
     * Given: ブック名 {@code MyBook}・シート名 {@code MySheet} の実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : コンテナ名がブック名、セクション名がシート名になり、セクションは 1 件になる。
     *
     * <p>担保する軸要素: C-01／C-02（1 件）／C-03。</p>
     */
    @Test
    public void readsContainerAndSectionNamesFromRealBookAndSheetNames() {
        // Given
        XlsFixture.book("MyBook").sheet("MySheet")
                .row(text("SETUP_TABLE=T"))
                .row(text("C"))
                .row(text("v"))
                .writeTo(dir());

        // When
        TestDataContainer container = read("MyBook", "MySheet");

        // Then
        assertThat(container.getName(), is("MyBook"));
        assertThat("セクション数", container.getSections().size(), is(1));
        assertThat(container.getSections().get(0).getName(), is("MySheet"));
    }

    /**
     * Given: マーカー行を 1 行も持たない実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : ブロックが空のセクションを 1 件持つコンテナになる。
     *
     * <p>担保する軸要素: C-04（空）。</p>
     */
    @Test
    public void readsEmptyBlockListFromRealSheetWithoutMarkers() {
        // Given
        book().row(text("just"), text("data")).writeTo(dir());

        // When
        TestDataContainer container = read();

        // Then
        assertThat("セクション数", container.getSections().size(), is(1));
        assertThat("マーカー行が無いシートのブロック一覧",
                container.getSections().get(0).getBlocks(), is(Collections.<TestDataBlock>emptyList()));
    }

    // ------------------------------------------------------------------ 軸C レコード種別の省略

    /**
     * Given: 名前行の先頭セル（レコード種別）が空白セルの {@code SETUP_FIXED} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : レコード種別は {@code null} ではなく空文字になる。
     *
     * <p>
     * 担保する軸要素: C-16（省略）。中間モデル {@code RecordLayout} の Javadoc は省略時の表現を
     * {@code null} と定めており、YAML 経路（辺②）は実際に {@code null} を入れる
     * （{@code YamlFormatReaderTest#readFile_recordTypeOmitted_keepsNullRecordType}）。
     * 実 {@code .xlsx} 経路ではセル値が空文字として読まれるため {@code null} にならない。
     * この非対称は課題として {@code coverage/issues.md} の XLS-06 に記録した（修正はしない）。
     * </p>
     */
    @Test
    public void readsOmittedRecordTypeAsEmptyStringFromRealBook() {
        // Given
        book().row(text("SETUP_FIXED=f.dat"))
                .row(blank(), text("f1"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("3"))
                .row(blank(), text("abc"))
                .writeTo(dir());

        // When
        FileDataBlock file = onlyBlock(FileDataBlock.class);

        // Then
        RecordLayout record = file.getRecords().get(0);
        assertThat(record.getRecordType(), is(""));
        assertThat(record.getRows(), is(Arrays.asList(Arrays.asList("abc"))));
    }

    // ------------------------------------------------------------------ 軸C 空コレクション／軸E 0 件

    /**
     * Given: カラム行だけを持ち、データ行を 1 行も持たない {@code SETUP_TABLE} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 列名は入るが行は 0 件になる。例外にはならない。
     *
     * <p>担保する軸要素: C-09（空。テーブル経路）／E-2（ブロック内行数 0 件）。</p>
     */
    @Test
    public void readsEmptyRowsFromTableWithoutDataRowsInRealBook() {
        // Given
        book().row(text("SETUP_TABLE=T"))
                .row(text("A"), text("B"))
                .writeTo(dir());

        // When
        TableDataBlock table = onlyBlock(TableDataBlock.class);

        // Then
        assertThat(table.getColumnNames(), is(Arrays.asList("A", "B")));
        assertThat("データ行を 1 行も持たないテーブルの行",
                table.getRows(), is(Collections.<List<String>>emptyList()));
    }

    /**
     * Given: カラム行だけを持ち、データ行を 1 行も持たない {@code LIST_MAP} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : テーブル経路と同じく列名は入り行は 0 件になる（経路が別なので個別に固定する）。
     *
     * <p>担保する軸要素: C-09（空。{@link ListMapBlock} 経路）／E-2（ブロック内行数 0 件）。</p>
     */
    @Test
    public void readsEmptyRowsFromListMapWithoutDataRowsInRealBook() {
        // Given
        book().row(text("LIST_MAP=lm"))
                .row(text("A"), text("B"))
                .writeTo(dir());

        // When
        ListMapBlock listMap = onlyBlock(ListMapBlock.class);

        // Then
        assertThat(listMap.getColumnNames(), is(Arrays.asList("A", "B")));
        assertThat("データ行を 1 行も持たない LIST_MAP の行",
                listMap.getRows(), is(Collections.<List<String>>emptyList()));
    }

    /**
     * Given: ディレクティブ行だけを持ち、名前行以降（レコードレイアウト）を持たない {@code SETUP_FIXED}
     *        ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : ブロックは生成され、ディレクティブは入るがレコードレイアウトは 0 件になる。例外にはならない。
     *
     * <p>担保する軸要素: C-12（空）／E-3（ファイル内レコードレイアウト数 0 件）。</p>
     */
    @Test
    public void readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook() {
        // Given
        book().row(text("SETUP_FIXED=f.dat"))
                .row(text("text-encoding"), text("UTF-8"))
                .writeTo(dir());

        // When
        FileDataBlock file = onlyBlock(FileDataBlock.class);

        // Then
        assertThat(file.getDataType(), is(DataType.SETUP_FIXED));
        assertThat(file.getIdentifier(), is("f.dat"));
        assertThat(file.getDirectives().get("text-encoding"), is("UTF-8"));
        assertThat(file.getDirectives().get("file-type"), is("Fixed"));
        assertDirectiveCount(file.getDirectives(), 2);
        assertThat("レコードレイアウトを 1 件も持たないファイルブロックのレコード",
                file.getRecords(), is(Collections.<RecordLayout>emptyList()));
    }

    /**
     * Given: FW 制御ヘッダ行だけを持ち、本文（名前行以降）を持たない {@code MESSAGE} ブロックの
     *        実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : ブロックは生成され、FW 制御ヘッダは入るがレコードレイアウトは 0 件になる。
     *
     * <p>担保する軸要素: C-15（空）／E-3（ファイル内レコードレイアウト数 0 件。{@link MessageDataBlock} 経路）。</p>
     *
     * <p>
     * {@link XlsFormatReader#read} は本体パーサが同 ID のデータを見つけられない場合に {@code MESSAGE}
     * ブロックを丸ごと落とす（{@code readMessageBlock} が {@code null} を返す）が、本入力では
     * <b>ブロックは生成されたうえでレコードが 0 件になる</b>ことを実測した。YAML 経路
     * （{@code YamlFormatReaderTest#readMessage_emptyBody_isStillMapped}）と同じ扱いである。
     * </p>
     */
    @Test
    public void readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook() {
        // Given
        book().row(text("MESSAGE=m"))
                .row(text("requestId"), text("R1"))
                .writeTo(dir());

        // When
        MessageDataBlock message = onlyBlock(MessageDataBlock.class);

        // Then
        assertThat(message.getDataType(), is(DataType.MESSAGE));
        assertThat(message.getIdentifier(), is("m"));
        assertThat(message.getFwHeaderFields().get("requestId"), is("R1"));
        assertThat("FW 制御ヘッダの件数", message.getFwHeaderFields().size(), is(1));
        assertThat(message.getDirectives().get("file-type"), is("Fixed"));
        assertDirectiveCount(message.getDirectives(), 1);
        assertThat("本文を持たないメッセージブロックのレコード",
                message.getRecords(), is(Collections.<RecordLayout>emptyList()));
    }

    /**
     * Given: 名前行・型行・長さ行を持つが値行を 1 行も持たない {@code SETUP_FIXED} ブロックの
     *        実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : レコードレイアウトは 1 件生成され、フィールド定義は入るが値行は 0 件になる。
     *
     * <p>担保する軸要素: C-18（空）。</p>
     */
    @Test
    public void readsEmptyRowsFromRecordLayoutWithoutValueRowsInRealBook() {
        // Given
        book().row(text("SETUP_FIXED=f.dat"))
                .row(text("data"), text("f1"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("3"))
                .writeTo(dir());

        // When
        FileDataBlock file = onlyBlock(FileDataBlock.class);

        // Then
        assertThat("レコードレイアウト数", file.getRecords().size(), is(1));
        RecordLayout record = file.getRecords().get(0);
        assertThat(record.getRecordType(), is("data"));
        assertThat(fieldNames(record), is(Arrays.asList("f1")));
        assertThat(record.getFields().get(0).getType(), is("半角英字"));
        assertThat(record.getFields().get(0).getLength(), is("3"));
        assertThat("値行を 1 行も持たない断片の行",
                record.getRows(), is(Collections.<List<String>>emptyList()));
    }

    // ------------------------------------------------------------------ 軸E 複数（1 ファイルに複数レコードレイアウト）

    /**
     * Given: 断片（レコードレイアウト）を 2 つ持つ {@code SETUP_FIXED} ブロックの実 {@code .xlsx}。
     *        1 つ目は値行 1 行、2 つ目は値行 2 行とし、2 つ目の長さ行には省略記法 {@code -} を含める。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : レコードレイアウトが 2 件に分かれ、レコード種別・フィールド定義・値行が断片ごとに独立して入る。
     *
     * <p>
     * 担保する軸要素: E-3（ファイル内レコードレイアウト数 複数）／C-12（非空・2 件）／C-18（非空・断片ごとに別）／
     * C-21（値あり。2 断片目の省略記法 {@code -} も原文のまま）。
     * </p>
     *
     * <p>
     * {@link XlsFormatReader#toRecordLayouts} は器の断片を権威に生行を走査してインデックスを進めるため、
     * <b>断片が複数あるときだけ通る経路</b>（断片ループと {@code verifyNameRow} による位置合わせ）を持つ。
     * この経路を実 {@code .xlsx} で通すのは本テストだけである（Fake リーダ経路には
     * {@code XlsFormatReaderTest#readRestoresMultipleRecordLayoutsInFixedFile} がある。
     * <b>入力の論理内容は同テストを参考にしたが、期待値は流用せず実行して観測した結果を固定した</b>）。
     * </p>
     *
     * <p>
     * なお {@code MESSAGE} 本文では断片を 2 つ以上作れない（2 つ目の名前行が値行として吸収される）。
     * 根拠と課題は {@code coverage/issues.md} の <b>XLS-15</b> と
     * {@code XlsFormatReaderInvalidInputTest#absorbsSecondNameRowAsDataRowInMessageBodyInRealBook} にある。
     * </p>
     */
    @Test
    public void readsMultipleRecordLayoutsFromOneFixedFileInRealBook() {
        // Given: 断片1（header, 値行 1 行）＋ 断片2（data, 値行 2 行。長さは省略記法 "-" を含む）
        book().row(text("SETUP_FIXED=multi.dat"))
                .row(text("text-encoding"), text("UTF-8"))
                .row(text("header"), text("h1"), text("h2"))
                .row(blank(), text("半角英字"), text("半角英字"))
                .row(blank(), text("5"), text("3"))
                .row(blank(), text("AAAAA"), text("BBB"))
                .row(text("data"), text("d1"), text("d2"))
                .row(blank(), text("半角英字"), text("半角"))
                .row(blank(), text("-"), text("2"))
                .row(blank(), text("1"), text("xy"))
                .row(blank(), text("2"), text("zw"))
                .writeTo(dir());

        // When
        FileDataBlock file = onlyBlock(FileDataBlock.class);

        // Then
        assertThat("レコードレイアウト数", file.getRecords().size(), is(2));

        RecordLayout header = file.getRecords().get(0);
        assertThat(header.getRecordType(), is("header"));
        assertThat(fieldNames(header), is(Arrays.asList("h1", "h2")));
        assertThat(header.getFields().get(0).getType(), is("半角英字"));
        assertThat(header.getFields().get(0).getLength(), is("5"));
        assertThat(header.getFields().get(1).getLength(), is("3"));
        assertThat(header.getRows(), is(Arrays.asList(Arrays.asList("AAAAA", "BBB"))));

        RecordLayout data = file.getRecords().get(1);
        assertThat(data.getRecordType(), is("data"));
        assertThat(fieldNames(data), is(Arrays.asList("d1", "d2")));
        assertThat("2 断片目の型も独立に原文復元される", data.getFields().get(1).getType(), is("半角"));
        assertThat("2 断片目の長さ省略記法も原文のまま", data.getFields().get(0).getLength(), is("-"));
        assertThat(data.getFields().get(1).getLength(), is("2"));
        assertThat(data.getRows(), is(Arrays.asList(
                Arrays.asList("1", "xy"),
                Arrays.asList("2", "zw"))));
    }

    // ------------------------------------------------------------------ assertion helpers

    /**
     * ディレクティブの件数を完全一致でアサートする（余計なディレクティブが混入しないことの担保）。
     *
     * <p>
     * <b>グローバル状態への依存に注意。</b>本体 {@code DataFile} のコンストラクタは
     * {@code prepareDefaultDirectives} で {@code SystemRepository} から既定ディレクティブを取り込む。
     * 本リポジトリの {@code src/test/resources/unit-test.xml} には {@code defaultDirectives}
     * （{@code text-encoding=Windows-31J}）・{@code fixedLengthDirectives}・{@code variableLengthDirectives}
     * が定義済みであり、将来どれかのテストが {@code SystemRepository} をこの設定で初期化したまま
     * 本クラスを実行すると、注入されるディレクティブが増えて<b>本クラスの件数アサートが一斉に落ちる</b>。
     * その場合は本メソッドが原因の入口である（本クラスのバグでも {@link XlsFormatReader} の退行でもない）。
     * 件数アサート自体は「Excel に書いていないディレクティブが混入しないこと」の担保なので維持する。
     * </p>
     *
     * @param directives 実際のディレクティブ
     * @param expected   期待件数
     */
    private static void assertDirectiveCount(Map<String, String> directives, int expected) {
        assertThat("ディレクティブの件数（実際の内容: " + directives + "）", directives.size(), is(expected));
    }

    /**
     * ブロックのグループ ID と識別子をアサートする。
     *
     * @param block      対象ブロック（{@code null} なら該当データタイプのブロックが無い）
     * @param groupId    期待するグループ ID
     * @param identifier 期待する識別子
     */
    private static void assertGroupIdAndIdentifier(MessageDataBlock block, String groupId, String identifier) {
        assertThat("識別子 " + identifier + " のブロックが生成されていること", block, is(notNullValue()));
        assertThat("グループ ID", block.getGroupId(), is(groupId));
        assertThat("識別子", block.getIdentifier(), is(identifier));
    }
}
