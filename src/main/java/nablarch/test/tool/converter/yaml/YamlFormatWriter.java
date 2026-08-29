package nablarch.test.tool.converter.yaml;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.ScalarStyle;

import nablarch.test.core.reader.DataType;
import nablarch.test.core.reader.yaml.YamlSection;
import nablarch.test.tool.converter.TestDataFormatWriter;
import nablarch.test.tool.converter.model.FieldDef;
import nablarch.test.tool.converter.model.FileDataBlock;
import nablarch.test.tool.converter.model.ListMapBlock;
import nablarch.test.tool.converter.model.MessageDataBlock;
import nablarch.test.tool.converter.model.RecordLayout;
import nablarch.test.tool.converter.model.TableDataBlock;
import nablarch.test.tool.converter.model.TestDataBlock;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;

/**
 * 中間モデル（{@link TestDataContainer}）を YAML へ書き出す OUT ライタ。
 *
 * <p>
 * {@link YamlFormatReader} と<b>記法対称</b>に直列化する。値はすべて
 * ダブルクォートで囲む（数値・null・前後空白を文字列として保つため）。ただし {@code null}（記述省略でなく
 * 明示的な NULL）はアンクォートの {@code null} とする。{@code ""}・{@code ${...}} は記法のまま保持する。
 * キーは原則クォートしない（YAML 特殊文字を含む場合のみクォートする）。
 * </p>
 *
 * <p>
 * セクションはブロックの {@link DataType} からセクションキーへ写し、初出順にグルーピングして出力する
 * （同一セクションキーの複数ブロックは 1 つのセクション配下へ記述順に並べる）。各ブロックの記法は
 * {@link YamlFormatReader} が読む形（テーブル＝{@code table:}＋行マップ／LIST_MAP＝{@code id:}＋列／
 * ファイル＝{@code path:}/{@code type:}/{@code directives:}/{@code records:}／メッセージ＝{@code id:}＋
 * {@code fw_header:}／送信系＝{@code group_id:}。デフォルトグループは省略）と一致させる。
 * </p>
 *
 * <p>
 * {@link DataType} からセクションキーへの対応は本ライタが保持する（本体の
 * {@link YamlSection#dataTypeToSectionKey(DataType)} は messaging 系専用で全種別を網羅しないため）。
 * </p>
 *
 * @author kiyobot
 */
public final class YamlFormatWriter implements TestDataFormatWriter {

    /** YAML 拡張子。 */
    private static final String YAML_EXTENSION = ".yaml";

    /** SnakeYAML Engine の Dump（スカラをダブルクォートスタイルで直列化）。スレッドセーフ。 */
    private static final Dump SCALAR_DUMP = new Dump(
            DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED).build());

    /**
     * {@inheritDoc}
     * <p>
     * コンテナの各セクションを {@code <basePath>/<セクション名>.yaml}（UTF-8）へ書き出す。
     * </p>
     * <p>
     * セクション名の {@code null} は検査しない。かつては {@code null.yaml} というファイルを黙って
     * 作っていたが（{@code issues.md} <b>XLS-33</b>）、{@link TestDataSection} が生成時に
     * {@code null} を拒否するようになったためここへは届かない。番人は置かない。
     * </p>
     */
    @Override
    public void write(TestDataContainer container, String basePath) {
        for (TestDataSection section : container.getSections()) {
            Path file = Paths.get(basePath, section.getName() + YAML_EXTENSION);
            try {
                Path parent = file.getParent();
                // basePath が空文字列の場合など、親ディレクトリを持たない相対パス（例: "foo.yaml"）が生成されると getParent() は null を返すため、null チェックが必須。
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.write(file, serialize(section).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException("failed to write YAML: " + file, e);
            }
        }
    }

    /**
     * 1 セクションを YAML テキストへ直列化する。
     *
     * @param section セクション
     * @return YAML テキスト（ブロックが無ければ空文字）
     */
    String serialize(TestDataSection section) {
        Map<String, List<TestDataBlock>> bySection = new LinkedHashMap<>();
        for (TestDataBlock block : section.getBlocks()) {
            String key = sectionKey(block.getDataType());
            bySection.computeIfAbsent(key, k -> new ArrayList<>()).add(block);
        }

        StringBuilder sb = new StringBuilder();
        boolean firstSection = true;
        for (Map.Entry<String, List<TestDataBlock>> entry : bySection.entrySet()) {
            if (!firstSection) {
                sb.append('\n');
            }
            firstSection = false;
            sb.append(entry.getKey()).append(":\n");
            for (TestDataBlock block : entry.getValue()) {
                emitBlock(sb, block);
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    // ブロック種別ごとの出力
    // ------------------------------------------------------------------------

    /**
     * ブロックを種別に応じて出力する。
     *
     * @param sb    出力先
     * @param block ブロック
     */
    private void emitBlock(StringBuilder sb, TestDataBlock block) {
        if (block instanceof TableDataBlock) {
            emitTable(sb, (TableDataBlock) block);
        } else if (block instanceof ListMapBlock) {
            emitListMap(sb, (ListMapBlock) block);
        } else if (block instanceof FileDataBlock) {
            emitFile(sb, (FileDataBlock) block);
        } else if (block instanceof MessageDataBlock) {
            emitMessage(sb, (MessageDataBlock) block);
        } else {
            // sealed 階層が将来変更された場合のランタイム安全網。instanceof チェーンにはコンパイル時の網羅性保証がない。
            throw new IllegalArgumentException("unsupported block: " + block.getClass().getName());
        }
    }

    /**
     * テーブル系ブロックを出力する（{@code table:}＋行マップ）。
     *
     * @param sb    出力先
     * @param block テーブルブロック
     */
    private void emitTable(StringBuilder sb, TableDataBlock block) {
        YamlSeq entry = new YamlSeq(sb, 1);
        emitGroupId(entry, block.getGroupId());
        entry.prop("table", block.getIdentifier());
        emitMapRows(sb, entry, block.getColumnNames(), block.getRows());
    }

    /**
     * LIST_MAP ブロックを出力する（{@code id:}＋行マップ）。
     *
     * @param sb    出力先
     * @param block LIST_MAP ブロック
     */
    private void emitListMap(StringBuilder sb, ListMapBlock block) {
        YamlSeq entry = new YamlSeq(sb, 1);
        emitGroupId(entry, block.getGroupId());
        entry.prop("id", block.getIdentifier());
        emitMapRows(sb, entry, block.getColumnNames(), block.getRows());
    }

    /**
     * ファイル系ブロックを出力する（{@code path:}/{@code type:}/{@code directives:}/{@code records:}）。
     * <p>
     * <b>ファイル種別 {@code null} はここでは検査しない。</b>{@code $defs.file_data} は {@code type} を
     * 必須とし、値を {@code enum} ＝ {@code ["fixed", "variable"]} に限るため、どちらとも決まっていない
     * ブロックは読み戻せる形に書けない。記法にも
     * 記法
     * {@code setup_files}・{@code expected_files} の各エントリには {@code path}・{@code type}・
     * {@code records} の3キーが必須であり、いずれかを省略するとエラーになるとある
     * （{@code coverage/issues.md} <b>XLS-29</b>）。{@link FileDataBlock} が<b>生成時点で拒否する</b>ため
     * ここへは届かない（番人の移設は 2026-08-19）。
     * </p>
     *
     * @param sb    出力先
     * @param block ファイルブロック
     */
    private void emitFile(StringBuilder sb, FileDataBlock block) {
        YamlSeq entry = new YamlSeq(sb, 1);
        emitGroupId(entry, block.getGroupId());
        entry.prop("path", block.getIdentifier());
        entry.prop("type", block.getFileType() == FileDataBlock.FileType.FIXED ? "fixed" : "variable");
        emitMap(sb, entry, "directives", block.getDirectives());
        emitRecords(sb, entry, block.getRecords());
    }

    /**
     * メッセージ系ブロック（MESSAGE／送信系 4 種）を出力する。
     * <p>
     * グループ ID が非空なら {@code group_id:}（送信系）、FW 制御ヘッダが非空なら {@code fw_header:}（MESSAGE）を
     * 出力する。いずれもブロックの内容に応じて自然に切り替わる。
     * </p>
     * <p>
     * <b>本文レコード 0 件はここでは検査しない。</b>{@code $defs.message_data} ／
     * {@code $defs.expected_request_message_data} ／ {@code $defs.group_message_data} はいずれも
     * {@code records} を必須かつ {@code minItems} ＝ 1 とするため、{@code records:} を省いても
     * {@code records: []} と書いても読み戻せない。記法にも電文のレコード 0 件を表す書き方の
     * 明文が無く、電文が存在しない場合は 記法の
     * とおり<b>データブロックごと省略する</b>。{@link MessageDataBlock} が<b>生成時点で拒否する</b>ため
     * ここへは届かない（{@code coverage/issues.md} <b>YML-12</b> の 2 形目。番人の移設は 2026-08-19）。
     * ファイルデータブロックのレコード 0 件は 0 バイトの空ファイルを表す<b>合法な形</b>であり、
     * {@link #emitFile} 経由で {@code records: []} を出す正当な経路である
     * （スキーマも
     * {@code $defs.file_data} だけが {@code records.minItems} ＝ 0）。
     * </p>
     *
     * @param sb    出力先
     * @param block メッセージブロック
     */
    private void emitMessage(StringBuilder sb, MessageDataBlock block) {
        YamlSeq entry = new YamlSeq(sb, 1);
        emitGroupId(entry, block.getGroupId());
        entry.prop("id", block.getIdentifier());
        emitMap(sb, entry, "directives", block.getDirectives());
        emitMap(sb, entry, "fw_header", block.getFwHeaderFields());
        emitRecords(sb, entry, block.getRecords());
    }

    // ------------------------------------------------------------------------
    // 構造要素の出力
    // ------------------------------------------------------------------------

    /**
     * グループ ID を出力する。
     * <p>
     * 中間モデルの {@code groupId} は<b>生値</b>（省略時は空文字）であり、そのまま書く。
     * 空文字（グループ省略）は本体スキーマが {@code group_id} に {@code minLength: 1} を課すため
     * キーごと出力しない（{@code TestDataBlock} のクラス Javadoc）。
     * </p>
     *
     * @param entry   エントリ
     * @param groupId 生値のグループ ID（非 null。省略時は空文字）
     */
    private static void emitGroupId(YamlSeq entry, String groupId) {
        if (!groupId.isEmpty()) {
            entry.prop("group_id", groupId);
        }
    }

    /**
     * テーブル／LIST_MAP の行を「カラム名→値」のマップ列として出力する。
     *
     * @param sb      出力先
     * @param parent  親エントリ
     * @param columns カラム名（記述順）
     * @param rows    行（カラムと同順）
     */
    private void emitMapRows(StringBuilder sb, YamlSeq parent, List<String> columns, List<List<String>> rows) {
        if (rows.isEmpty()) {
            parent.line(key("rows") + ": []");
            return;
        }
        parent.header("rows");
        int rowLevel = parent.childLevel();
        for (List<String> row : rows) {
            YamlSeq item = new YamlSeq(sb, rowLevel);
            if (columns.isEmpty()) {
                item.line("{}");
                continue;
            }
            for (int i = 0; i < columns.size(); i++) {
                item.prop(columns.get(i), i < row.size() ? row.get(i) : null);
            }
        }
    }

    /**
     * レコードレイアウト群を出力する。
     * <p>
     * 空なら {@code records: []}（空配列）を出力する。0 バイトの空ファイルはレコード定義を持たない
     * ファイルデータブロックとして表し、{@code records:} に空配列を記載すると記法仕様が定めているためである
     * （記法は／記法。本体スキーマも
     * {@code $defs.file_data.required} に {@code records} を含み {@code minItems} は 0）。
     * <b>空で本メソッドへ入るのはファイル系だけである</b>——メッセージ系は
     * {@link MessageDataBlock} が 0 件を<b>生成時点で拒否する</b>（{@code $defs.message_data} の
     * {@code records} は {@code minItems} ＝ 1 であり、空配列もスキーマ違反になるため。番人の移設は 2026-08-19）。
     * </p>
     * <p>
     * <b>フィールド 0 件のレコードレイアウトはここでは検査しない。</b>{@code $defs.record_fragment} は
     * {@code fields} を必須かつ {@code minItems} ＝ 1 とするため、どう書いても読み戻せない形だが
     * （{@code coverage/issues.md} <b>YML-12</b> の 3 形目。辺③の同じ形は <b>XLS-22</b>）、
     * {@link RecordLayout} が<b>生成時点で拒否する</b>ためここへは届かない（番人の移設は 2026-08-19）。
     * </p>
     * <p>
     * <b>データ型が {@code null} のフィールド定義もここでは検査しない。</b>{@code $defs.field_def} は
     * {@code required} ＝ {@code ["name", "type"]} であり {@code type} を省略した形は読み戻せないが
     * （{@code coverage/issues.md} <b>YML-12</b> の 4 形目）、{@link FieldDef} が<b>生成時点で拒否する</b>
     * ためここへは届かない（番人の移設は §1-D）。
     * </p>
     * <p>
     * <b>フィールド長が {@code null} のフィールド定義もここでは検査しない。</b>記法は固定長ファイルについて
     * 「フィールド名称・データ型・フィールド長の3リストが同サイズで必須」と定め
     * （「フィールド名称・データ型・フィールド長リストのサイズが一致していない」を記述時のエラーに挙げる）、
     * 電文も 記法はフレームワーク制御ヘッダ以降のメッセージボディは、フィールド名称・データ型・
     * フィールド長・データという、前述のファイルデータと同じ構成を持つで同じ制約に掛かる
     * （{@code coverage/issues.md} <b>XLS-30</b>）。文脈（固定長ファイルか・電文か・可変長ファイルか）は
     * ブロックが持っているため、{@link FileDataBlock}（{@code FIXED} のとき）と
     * {@link MessageDataBlock}（常に）が<b>生成時点で拒否する</b>。<b>可変長ファイルでは {@code null} が
     * 正しい</b>ため拒否しない（記法は可変長ファイルでは…フィールド長は不要である）。
     * 番人の移設は 2026-08-19。
     * </p>
     *
     * @param sb      出力先
     * @param parent  親エントリ
     * @param records レコードレイアウト群
     */
    private void emitRecords(StringBuilder sb, YamlSeq parent, List<RecordLayout> records) {
        if (records.isEmpty()) {
            parent.line(key("records") + ": []");
            return;
        }
        parent.header("records");
        int recordLevel = parent.childLevel();
        for (RecordLayout record : records) {
            YamlSeq item = new YamlSeq(sb, recordLevel);
            if (record.getRecordType() != null) {
                item.prop("record_type", record.getRecordType());
            }
            emitFlowList(sb, item, "fields", record.getFields(), YamlFormatWriter::fieldFlow);
            emitFlowList(sb, item, "rows", record.getRows(), YamlFormatWriter::rowFlow);
        }
    }

    /**
     * 文字列マップ（directives／fw_header）をブロックマップとして出力する（空なら出力しない）。
     *
     * @param sb      出力先
     * @param parent  親エントリ
     * @param keyName キー名
     * @param map     マップ（記述順）
     */
    private void emitMap(StringBuilder sb, YamlSeq parent, String keyName, Map<String, String> map) {
        if (map.isEmpty()) {
            return;
        }
        parent.header(keyName);
        String indent = ind(parent.childLevel());
        for (Map.Entry<String, String> e : map.entrySet()) {
            sb.append(indent).append(key(e.getKey())).append(": ").append(q(e.getValue())).append('\n');
        }
    }

    /**
     * フロー記法のリスト（{@code fields:}＝フロー map／{@code rows:}＝フロー list）を出力する。
     * 空なら {@code key: []} を 1 行で出力する。
     *
     * @param sb       出力先
     * @param parent   親エントリ
     * @param keyName  キー名
     * @param items    要素
     * @param renderer 各要素のフロー記法レンダラ
     * @param <T>      要素型
     */
    private <T> void emitFlowList(StringBuilder sb, YamlSeq parent, String keyName, List<T> items,
                                  Function<T, String> renderer) {
        if (items.isEmpty()) {
            parent.line(key(keyName) + ": []");
            return;
        }
        parent.header(keyName);
        int itemLevel = parent.childLevel();
        for (T item : items) {
            new YamlSeq(sb, itemLevel).line(renderer.apply(item));
        }
    }

    /**
     * フィールド定義をフロー map 記法へ整形する（{@code length} は null なら省略。
     * {@code type} は必須のため常に出力する。{@code null} は
     * {@link nablarch.test.tool.converter.model.FieldDef} が生成時に拒否している）。
     *
     * @param field フィールド定義
     * @return フロー map 文字列
     */
    private static String fieldFlow(FieldDef field) {
        StringBuilder b = new StringBuilder("{");
        b.append(key("name")).append(": ").append(q(field.getName()));
        b.append(", ").append(key("type")).append(": ").append(q(field.getType()));
        if (field.getLength() != null) {
            b.append(", ").append(key("length")).append(": ").append(q(field.getLength()));
        }
        return b.append('}').toString();
    }

    /**
     * データ行をフロー list 記法へ整形する。
     *
     * @param values 値（フィールド順）
     * @return フロー list 文字列
     */
    private static String rowFlow(List<String> values) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                b.append(", ");
            }
            b.append(q(values.get(i)));
        }
        return b.append(']').toString();
    }

    // ------------------------------------------------------------------------
    // スカラ・キー・インデント
    // ------------------------------------------------------------------------

    /**
     * 値をダブルクォートで囲んだ YAML スカラへ整形する。{@code null} はアンクォートの {@code null}。
     * クォートとエスケープは SnakeYAML Engine（YAML 1.2）に委譲する。
     *
     * @param value 値（{@code null} 可）
     * @return YAML スカラ文字列
     */
    static String q(String value) {
        if (value == null) {
            return "null";
        }
        // dumpToString は末尾に改行を付加するため除去する
        return SCALAR_DUMP.dumpToString(value).stripTrailing();
    }

    /**
     * キーを整形する。YAML 特殊文字・空白・空文字を含む場合のみダブルクォートで囲む。
     *
     * @param k キー
     * @return キー文字列
     */
    static String key(String k) {
        return isPlainSafeKey(k) ? k : q(k);
    }

    /**
     * キーをクォートなしで安全に書けるか判定する。
     *
     * @param k キー
     * @return クォート不要なら真
     */
    private static boolean isPlainSafeKey(String k) {
        if (k.isEmpty()) {
            return false;
        }
        for (int i = 0; i < k.length(); i++) {
            char c = k.charAt(i);
            if (c < 0x20 || c == ' ' || "\"'#:,[]{}&*!|>%@`?".indexOf(c) >= 0) {
                return false;
            }
        }
        return "-?:".indexOf(k.charAt(0)) < 0;
    }

    /**
     * インデント（1 レベル＝半角スペース 2 個）を返す。
     *
     * @param level レベル
     * @return インデント文字列
     */
    static String ind(int level) {
        StringBuilder b = new StringBuilder(level * 2);
        for (int i = 0; i < level; i++) {
            b.append("  ");
        }
        return b.toString();
    }

    /**
     * {@link DataType} を YAML セクションキーへ写す（全種別）。
     *
     * <p>
     * {@code default:} は switch の網羅のために残しているだけで<b>到達しない</b>。写せないのは
     * {@link DataType#DEFAULT} だけであり、{@code TestDataBlock} が生成時に拒否するようになったため
     * ここへは届かない（{@code issues.md} <b>XLS-20</b>）。番人としては置いていない。
     * </p>
     *
     * @param type データ種別
     * @return セクションキー
     */
    private static String sectionKey(DataType type) {
        switch (type) {
            case SETUP_TABLE_DATA:                 return YamlSection.KEY_SETUP_TABLES;
            case EXPECTED_TABLE_DATA:              return YamlSection.KEY_EXPECTED_TABLES;
            case EXPECTED_COMPLETED:               return YamlSection.KEY_EXPECTED_COMPLETE_TABLES;
            case LIST_MAP:                         return YamlSection.KEY_LIST_MAPS;
            case SETUP_FIXED:
            case SETUP_VARIABLE:                   return YamlSection.KEY_SETUP_FILES;
            case EXPECTED_FIXED:
            case EXPECTED_VARIABLE:                return YamlSection.KEY_EXPECTED_FILES;
            case MESSAGE:                          return YamlSection.KEY_MESSAGES;
            case EXPECTED_REQUEST_HEADER_MESSAGES: return YamlSection.KEY_EXPECTED_REQUEST_HEADER_MESSAGES;
            case EXPECTED_REQUEST_BODY_MESSAGES:   return YamlSection.KEY_EXPECTED_REQUEST_BODY_MESSAGES;
            case RESPONSE_HEADER_MESSAGES:         return YamlSection.KEY_RESPONSE_HEADER_MESSAGES;
            case RESPONSE_BODY_MESSAGES:           return YamlSection.KEY_RESPONSE_BODY_MESSAGES;
            default:
                // 到達不能。DEFAULT は TestDataBlock が生成時に拒否し（issues.md XLS-20）、
                // 残る 13 個の DataType は上ですべて分岐しているため。DataType が増えたときの安全網として残す。
                throw new IllegalArgumentException("unsupported DataType: " + type);
        }
    }

}
