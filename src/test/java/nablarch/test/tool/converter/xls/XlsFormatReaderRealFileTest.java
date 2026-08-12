package nablarch.test.tool.converter.xls;

import static nablarch.test.tool.converter.xls.XlsFixture.blank;
import static nablarch.test.tool.converter.xls.XlsFixture.text;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nablarch.test.core.reader.DataType;
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
 * 軸C（中間モデル 21 フィールド）を、<b>実 {@code .xlsx} を入力に</b>固定するテスト。
 *
 * <p>
 * {@code XlsFormatReaderTest}（既存 33 件）は内部 Fake リーダに {@code List<List<String>>} の canned 行を
 * 与えるため、実セル → 文字列行の区間を通らない。本クラスは {@link XlsFixture} が POI で組み立てた
 * 実 {@code .xlsx} を本番配線の {@link XlsFormatReader}（{@code PoiXlsReader}）へ食わせる。既存 33 件とは
 * 入力経路が異なるため流用せず組み直している。
 * </p>
 *
 * <p>
 * 各テストの Javadoc には、そのテストが担保する軸要素の ID
 * （{@code .rn/ntf-test-data-converter/coverage/inventory.md} の A-01〜A-14／B-1〜B-4／C-01〜C-21）を記す。
 * </p>
 *
 * <p>
 * <b>本クラスのアサーションはすべて「実行して観測した現状の挙動」である。</b>期待される仕様ではない。
 * 妥当でないと判断した挙動は {@code .rn/ntf-test-data-converter/coverage/issues.md} に課題として記録して
 * あり、実装（src/main）は変更していない。
 * </p>
 *
 * <p>
 * 本クラスが扱わない軸要素と理由:
 * </p>
 * <ul>
 *   <li>A-01 {@code DEFAULT} — {@code TestCoreReaderAdapter} L362 が {@code DEFAULT} と判定した行を
 *       {@code continue} でスキップするため、リーダ経路では {@code DEFAULT} のブロックが生成されない。</li>
 *   <li>C-02 {@code sections} の「空」「複数」— {@link XlsFormatReader#read} L133 が
 *       {@code Collections.singletonList(section)} を返すため常に 1 件。</li>
 *   <li>コレクションが 0 件になるケース（C-08／C-09／C-12／C-15／C-17／C-18 の「空」）— 軸E「0 件」と
 *       重なるためタスク #21 が扱う。</li>
 * </ul>
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
     * フィクスチャの出力先ディレクトリ。
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
        return new XlsFormatReader().read(folder.getRoot().toString(), bookName + "/" + sheetName);
    }

    /**
     * 既定のブック／シートを読み、唯一のセクションのブロック一覧を返す。
     *
     * @return ブロック一覧
     */
    private List<TestDataBlock> blocks() {
        TestDataContainer container = read();
        assertThat(container.getSections().size(), is(1));
        return container.getSections().get(0).getBlocks();
    }

    /**
     * 既定のブック／シートを読み、唯一のブロックを返す。
     *
     * @return ブロック
     */
    private TestDataBlock onlyBlock() {
        List<TestDataBlock> blocks = blocks();
        assertThat(blocks.size(), is(1));
        return blocks.get(0);
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
        TestDataBlock block = onlyBlock();

        // Then
        assertThat(block, is(instanceOf(TableDataBlock.class)));
        TableDataBlock table = (TableDataBlock) block;
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
        TableDataBlock table = (TableDataBlock) onlyBlock();

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
        TableDataBlock table = (TableDataBlock) onlyBlock();

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
        TestDataBlock block = onlyBlock();

        // Then
        assertThat(block, is(instanceOf(ListMapBlock.class)));
        ListMapBlock listMap = (ListMapBlock) block;
        assertThat(listMap.getDataType(), is(DataType.LIST_MAP));
        assertThat(listMap.getGroupId(), is(""));
        assertThat(listMap.getIdentifier(), is("testShots"));
        assertThat(listMap.getColumnNames(), is(Arrays.asList("Z", "A", "M")));
        assertThat(listMap.getRows(), is(Arrays.asList(Arrays.asList("z1", "a1", "m1"))));
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
        TestDataBlock block = onlyBlock();

        // Then
        assertThat(block, is(instanceOf(FileDataBlock.class)));
        FileDataBlock file = (FileDataBlock) block;
        assertThat(file.getDataType(), is(DataType.SETUP_FIXED));
        assertThat(file.getIdentifier(), is("test.dat"));
        assertThat(file.getFileType(), is(FileDataBlock.FileType.FIXED));
        assertThat(file.getDirectives().get("text-encoding"), is("UTF-8"));
        // Excel に書いていない file-type が器の既定値として現れる（issues.md XLS-07）。
        assertThat(file.getDirectives().get("file-type"), is("Fixed"));
        assertThat(file.getDirectives().size(), is(2));

        assertThat(file.getRecords().size(), is(1));
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
     * Given: ディレクティブ行を 1 行も持たない {@code EXPECTED_FIXED} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : データタイプ {@code EXPECTED_FIXED} の {@link FileDataBlock} が生成される。
     *        ディレクティブ行が無くてもディレクティブは空にならず、器が注入する {@code file-type} だけを持つ。
     *
     * <p>
     * 担保する軸要素: A-07／C-11（非空。「空 Map」が到達不能であることの根拠でもある。
     * 本体 {@code DataFile} のコンストラクタ L92 が {@code setDirective("file-type", getFileType())} を
     * 必ず実行するため、Excel 由来のディレクティブ行が 0 行でも空 Map にならない）。
     * </p>
     */
    @Test
    public void readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook() {
        // Given
        book().row(text("EXPECTED_FIXED=expected.dat"))
                .row(text("rec"), text("g1"))
                .row(blank(), text("半角英字"))
                .row(blank(), text("3"))
                .row(blank(), text("xyz"))
                .writeTo(dir());

        // When
        FileDataBlock file = (FileDataBlock) onlyBlock();

        // Then
        assertThat(file.getDataType(), is(DataType.EXPECTED_FIXED));
        assertThat(file.getIdentifier(), is("expected.dat"));
        assertThat(file.getFileType(), is(FileDataBlock.FileType.FIXED));
        assertThat(file.getDirectives().get("file-type"), is("Fixed"));
        assertThat(file.getDirectives().size(), is(1));
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
        FileDataBlock file = (FileDataBlock) onlyBlock();

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
        FileDataBlock file = (FileDataBlock) onlyBlock();

        // Then
        assertThat(file.getDataType(), is(DataType.EXPECTED_VARIABLE));
        assertThat(file.getGroupId(), is("[g2]"));
        assertThat(file.getIdentifier(), is("out.csv"));
        assertThat(file.getFileType(), is(FileDataBlock.FileType.VARIABLE));
        assertThat(file.getDirectives().get("record-separator"), is("CRLF"));
        // Excel に書いていない file-type / field-separator が器の既定値として現れる（issues.md XLS-07）。
        assertThat(file.getDirectives().get("file-type"), is("Variable"));
        assertThat(file.getDirectives().get("field-separator"), is(","));
        assertThat(file.getDirectives().size(), is(3));
    }

    // ------------------------------------------------------------------ 軸A メッセージ系

    /**
     * Given: ディレクティブ行・FW 制御ヘッダ行・本文を持つ {@code MESSAGE} ブロックの実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@link MessageDataBlock} が生成され、ディレクティブ・FW ヘッダ・本文レコードが入る。
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
        TestDataBlock block = onlyBlock();

        // Then
        assertThat(block, is(instanceOf(MessageDataBlock.class)));
        MessageDataBlock message = (MessageDataBlock) block;
        assertThat(message.getDataType(), is(DataType.MESSAGE));
        assertThat(message.getGroupId(), is(""));
        assertThat(message.getIdentifier(), is("msg1"));
        assertThat(message.getDirectives().get("text-encoding"), is("UTF-8"));
        assertThat(message.getFwHeaderFields().get("requestId"), is("R1"));
        assertThat(message.getFwHeaderFields().size(), is(1));

        assertThat(message.getRecords().size(), is(1));
        RecordLayout record = message.getRecords().get(0);
        assertThat(record.getRecordType(), is("data"));
        assertThat(fieldNames(record), is(Arrays.asList("body1", "body2")));
        assertThat(record.getFields().get(0).getType(), is("半角英字"));
        assertThat(record.getFields().get(0).getLength(), is("10"));
        assertThat(record.getRows(), is(Arrays.asList(Arrays.asList("x", "y"))));
    }

    /**
     * Given: 送信同期メッセージ 4 種（要求ヘッダ／要求本文／応答ヘッダ／応答本文）を持つ実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : 4 種すべてが {@link MessageDataBlock} として生成され、データタイプとグループ ID が入る。
     *        送信系に FW 制御ヘッダは無く、{@code fwHeaderFields} は空 Map になる。
     *
     * <p>担保する軸要素: A-11／A-12／A-13／A-14／C-14（空）。</p>
     */
    @Test
    public void readsAllFourSendSyncMessageTypesFromRealBook() {
        // Given
        book().row(text("EXPECTED_REQUEST_HEADER_MESSAGES[case1]=RM01"))
                .row(text("no"), text("requestId"))
                .row(blank(), text("半角"))
                .row(blank(), text("20"))
                .row(text("1"), text("RM01"))
                .row(text("EXPECTED_REQUEST_BODY_MESSAGES[case1]=RM01"))
                .row(text("no"), text("userId"))
                .row(blank(), text("半角"))
                .row(blank(), text("10"))
                .row(text("1"), text("user01"))
                .row(text("RESPONSE_HEADER_MESSAGES[res_case1]=RM01"))
                .row(text("no"), text("requestId"))
                .row(blank(), text("半角"))
                .row(blank(), text("20"))
                .row(text("1"), text("RM01"))
                .row(text("RESPONSE_BODY_MESSAGES[res_case1]=RM01"))
                .row(text("no"), text("failureCode"))
                .row(blank(), text("半角"))
                .row(blank(), text("20"))
                .row(text("1"), text("0"))
                .writeTo(dir());

        // When
        List<TestDataBlock> blocks = blocks();

        // Then
        assertThat(blocks.size(), is(4));
        Map<DataType, String> typeToGroup = new HashMap<>();
        for (TestDataBlock block : blocks) {
            assertThat(block, is(instanceOf(MessageDataBlock.class)));
            MessageDataBlock message = (MessageDataBlock) block;
            typeToGroup.put(message.getDataType(), message.getGroupId());
            // 送信系に FW 制御ヘッダは無い
            assertTrue(message.getFwHeaderFields().isEmpty());
        }
        assertThat(typeToGroup.get(DataType.EXPECTED_REQUEST_HEADER_MESSAGES), is("[case1]"));
        assertThat(typeToGroup.get(DataType.EXPECTED_REQUEST_BODY_MESSAGES), is("[case1]"));
        assertThat(typeToGroup.get(DataType.RESPONSE_HEADER_MESSAGES), is("[res_case1]"));
        assertThat(typeToGroup.get(DataType.RESPONSE_BODY_MESSAGES), is("[res_case1]"));
    }

    // ------------------------------------------------------------------ 軸B

    /**
     * Given: テーブル・LIST_MAP・固定長ファイル・MESSAGE が 1 シートに混在する実 {@code .xlsx}。
     * When : 実 {@code .xlsx} を {@code read}。
     * Then : {@code TestDataBlock} の具象 4 種が 1 セクションに揃う。
     *
     * <p>担保する軸要素: B-1／B-2／B-3／B-4（実ファイル経由での生成）／C-04（非空）。</p>
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
        for (TestDataBlock block : blocks) {
            kinds.add(block.getClass());
        }
        assertThat(kinds.size(), is(4));
        assertTrue(kinds.contains(TableDataBlock.class));
        assertTrue(kinds.contains(ListMapBlock.class));
        assertTrue(kinds.contains(FileDataBlock.class));
        assertTrue(kinds.contains(MessageDataBlock.class));
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
        assertThat(container.getSections().size(), is(1));
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
        assertThat(container.getSections().size(), is(1));
        assertTrue(container.getSections().get(0).getBlocks().isEmpty());
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
        FileDataBlock file = (FileDataBlock) onlyBlock();

        // Then
        RecordLayout record = file.getRecords().get(0);
        assertThat(record.getRecordType(), is(""));
        assertThat(record.getRows(), is(Arrays.asList(Arrays.asList("abc"))));
    }
}
