package nablarch.test.tool.converter.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import nablarch.test.core.reader.yaml.YamlLoader;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * YAML テストデータディレクトリを検証し、{@link ValidationError} のリストを返すリンタ。
 *
 * <p>人手で記述された YAML の記述ミスを早期検出する。入力が壊れていても例外で停止せず、
 * {@link ValidationError} として報告する。</p>
 *
 * <p>検証ルール:</p>
 * <ul>
 *   <li><b>V-COL</b>：file 系・message 系の record_fragment で fields 件数と各 rows 配列長が一致すること</li>
 *   <li><b>V-DIR</b>：構造境界 — {@code fw_header} を持ちうるセクション（{@code messages} /
 *       {@code expected_request_*_messages}）の {@code fw_header} にディレクティブ名が混入していないこと</li>
 *   <li><b>V-SCH</b>：{@code ntf-testdata-yaml-schema.json} に適合していること</li>
 *   <li><b>V-FNAME</b>：同一 record_fragment 内のフィールド名が重複していないこと</li>
 *   <li><b>V-DKEY</b>：{@code directives} のキーが既知のディレクティブ名であること</li>
 *   <li><b>V-YAML</b>：YAML として解析可能であること（不正構文・キー重複を検出）</li>
 *   <li><b>V-COLCASE</b>：テーブル（{@code setup_tables} など）のカラム名が、大文字・小文字の違いだけで
 *       重複していないこと。本体はテーブルのカラム名を大文字にするため、重複すると後の値だけが使われる</li>
 * </ul>
 */
public class YamlTestDataValidator {

    private static final String SCHEMA_RESOURCE = "/nablarch/test/ntf-testdata-yaml-schema.json";

    private static final JsonSchema JSON_SCHEMA;

    /** 解析済みの YAML（Map）を JSON スキーマ検証に渡す {@link JsonNode} へ変換するための ObjectMapper。 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 失敗経路は到達しない（スキーマは依存 jar に同梱）。原因の分かる例外にするため残す。
        try (InputStream in = YamlTestDataValidator.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("スキーマリソースが見つかりません: " + SCHEMA_RESOURCE);
            }
            JSON_SCHEMA = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(in);
        } catch (IOException e) {
            throw new IllegalStateException("スキーマのロードに失敗しました: " + SCHEMA_RESOURCE, e);
        }
    }

    /**
     * 既知のディレクティブ名。{@code directives:} のキー検証（V-DKEY）と、{@code fw_header:} への
     * ディレクティブ名混入検出（V-DIR）に用いる。
     * NTF 本体のディレクティブ名を網羅する。
     */
    static final Set<String> KNOWN_DIRECTIVE_NAMES = Set.of(
            "file-type", "text-encoding", "record-separator",
            "record-length",
            "positive-zone-sign-nibble", "negative-zone-sign-nibble",
            "positive-pack-sign-nibble", "negative-pack-sign-nibble",
            "required-decimal-point", "fixed-sign-position", "required-plus-sign",
            "field-separator", "quoting-delimiter", "ignore-blank-lines",
            "requires-title", "max-record-length", "title-record-type-name"
    );

    /** record_fragment（fields/rows）と directives を持つ file 系・message 系セクションキー。 */
    private static final Set<String> RECORD_FRAGMENT_SECTION_KEYS = Set.of(
            "setup_files", "expected_files",
            "messages",
            "expected_request_header_messages", "expected_request_body_messages",
            "response_header_messages", "response_body_messages"
    );

    /** テーブルのセクションキー（V-COLCASE の対象）。 */
    private static final Set<String> TABLE_SECTION_KEYS = Set.of(
            "setup_tables", "expected_tables", "expected_complete_tables"
    );

    /**
     * {@code fw_header} を持ちうるセクションキー（V-DIR の対象）。
     */
    private static final Set<String> FW_HEADER_SECTION_KEYS = Set.of(
            "messages",
            "expected_request_header_messages", "expected_request_body_messages"
    );

    /**
     * 指定ディレクトリ内の全 {@code *.yaml} を名前昇順で検証する。
     *
     * @param dirPath 検証対象の YAML ディレクトリ
     * @return 検証エラーのリスト（エラーなしは空リスト）
     */
    public List<ValidationError> validate(Path dirPath) {
        File[] yamlFiles = dirPath.toFile().listFiles(f -> f.getName().endsWith(".yaml"));
        if (yamlFiles == null || yamlFiles.length == 0) {
            return Collections.emptyList();
        }
        Arrays.sort(yamlFiles, Comparator.comparing(File::getName));

        List<ValidationError> errors = new ArrayList<>();
        for (File yamlFile : yamlFiles) {
            errors.addAll(validateFile(yamlFile, JSON_SCHEMA));
        }
        return errors;
    }

    private List<ValidationError> validateFile(File yamlFile, JsonSchema schema) {
        String filePath = yamlFile.getAbsolutePath();
        List<ValidationError> errors = new ArrayList<>();

        String yamlText;
        try {
            yamlText = new String(Files.readAllBytes(yamlFile.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            errors.add(new ValidationError(filePath, "", "[V-IO] ファイル読み込みエラー: " + e.getMessage()));
            return errors;
        }

        // V-YAML: 不正構文・キー重複
        Map<String, Object> yaml;
        try {
            yaml = parseYaml(yamlText);
        } catch (YamlEngineException e) {
            errors.add(new ValidationError(filePath, "", "[V-YAML] YAML 解析エラー: " + e.getMessage()));
            return errors;
        }

        // V-SCH: スキーマ適合
        try {
            JsonNode jsonNode = (yaml == null) ? NullNode.getInstance() : OBJECT_MAPPER.valueToTree(yaml);
            for (ValidationMessage schemaError : schema.validate(jsonNode)) {
                errors.add(new ValidationError(filePath,
                        schemaError.getInstanceLocation().toString(),
                        "[V-SCH] スキーマ非適合: " + schemaError.getMessage()));
            }
        } catch (RuntimeException e) {
            // 到達しない。検証器の例外で残りのファイルの検査が止まらないよう残す。
            errors.add(new ValidationError(filePath, "", "[V-SCH] スキーマ検証エラー: " + e.getMessage()));
        }

        // 構造検証（V-COL / V-DIR / V-FNAME / V-DKEY / V-COLCASE）は Map トップレベルのみ
        if (yaml != null) {
            errors.addAll(validateStructure(filePath, yaml));
        }
        return errors;
    }

    private List<ValidationError> validateStructure(String filePath, Map<String, Object> yaml) {
        List<ValidationError> errors = new ArrayList<>();

        for (Map.Entry<String, Object> entry : yaml.entrySet()) {
            String sectionKey = entry.getKey();
            List<Object> blocks = castList(entry.getValue());

            for (int blockIdx = 0; blockIdx < blocks.size(); blockIdx++) {
                Map<String, Object> block = castMap(blocks.get(blockIdx));
                String blockLocation = sectionKey + "[" + blockIdx + "]";

                if (RECORD_FRAGMENT_SECTION_KEYS.contains(sectionKey)) {
                    List<Object> records = castList(block.get("records"));
                    for (int recIdx = 0; recIdx < records.size(); recIdx++) {
                        Map<String, Object> rec = castMap(records.get(recIdx));
                        String recLocation = blockLocation + ".records[" + recIdx + "]";
                        errors.addAll(validateColumnCount(filePath, recLocation, rec));
                        errors.addAll(validateFieldNames(filePath, recLocation, rec));
                    }
                    errors.addAll(validateDirectiveKeys(filePath, blockLocation, block));
                }

                if (FW_HEADER_SECTION_KEYS.contains(sectionKey)) {
                    errors.addAll(validateFwHeader(filePath, blockLocation, block));
                }

                if (TABLE_SECTION_KEYS.contains(sectionKey)) {
                    errors.addAll(validateColumnNameCase(filePath, blockLocation, block));
                }
            }
        }
        return errors;
    }

    /** V-COL: fields 件数と各 rows 配列長の一致を検証する。 */
    private List<ValidationError> validateColumnCount(String filePath, String recLocation, Map<String, Object> rec) {
        List<ValidationError> errors = new ArrayList<>();
        int fieldCount = castList(rec.get("fields")).size();
        List<Object> rows = castList(rec.get("rows"));

        for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
            int rowSize = castList(rows.get(rowIdx)).size();
            if (rowSize != fieldCount) {
                errors.add(new ValidationError(filePath,
                        recLocation + ".rows[" + rowIdx + "]",
                        "[V-COL] 列数不一致: fields=" + fieldCount + " 件に対して rows=" + rowSize + " 要素"));
            }
        }
        return errors;
    }

    /**
     * V-FNAME: 同一 record_fragment 内のフィールド名重複を検証する。
     * 同一名が N 回出現した場合は (N-1) 件のエラーを報告する。
     */
    private List<ValidationError> validateFieldNames(String filePath, String recLocation, Map<String, Object> rec) {
        List<ValidationError> errors = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Object fieldObj : castList(rec.get("fields"))) {
            Object nameObj = castMap(fieldObj).get("name");
            if (nameObj == null) {
                continue;
            }
            if (!seen.add(nameObj.toString())) {
                errors.add(new ValidationError(filePath,
                        recLocation + ".fields",
                        "[V-FNAME] フィールド名重複: \"" + nameObj + "\" が同一 record_fragment 内で重複しています"));
            }
        }
        return errors;
    }

    /**
     * V-COLCASE: テーブルのカラム名が、大文字・小文字の違いだけで重複していないことを検証する。
     * 全行のキーを対象にし、重複する組ごとに 1 件を報告する。
     */
    private List<ValidationError> validateColumnNameCase(String filePath, String blockLocation,
                                                         Map<String, Object> block) {
        Map<String, Set<String>> byUpperCase = new LinkedHashMap<>();
        for (Object row : castList(block.get("rows"))) {
            for (String key : castMap(row).keySet()) {
                byUpperCase.computeIfAbsent(key.toUpperCase(Locale.ROOT), k -> new LinkedHashSet<>()).add(key);
            }
        }
        List<ValidationError> errors = new ArrayList<>();
        for (Set<String> keys : byUpperCase.values()) {
            if (keys.size() > 1) {
                errors.add(new ValidationError(filePath,
                        blockLocation + ".rows",
                        "[V-COLCASE] カラム名が大文字・小文字の違いだけで重複しています: " + keys
                                + "（テスティングフレームワークはカラム名を大文字にするため、後の値だけが使われます）"));
            }
        }
        return errors;
    }

    /** V-DKEY: directives のキーが {@link #KNOWN_DIRECTIVE_NAMES} に含まれることを検証する。 */
    private List<ValidationError> validateDirectiveKeys(String filePath, String blockLocation, Map<String, Object> block) {
        List<ValidationError> errors = new ArrayList<>();
        for (String key : castMap(block.get("directives")).keySet()) {
            if (!KNOWN_DIRECTIVE_NAMES.contains(key)) {
                errors.add(new ValidationError(filePath,
                        blockLocation + ".directives",
                        "[V-DKEY] 不正なディレクティブキー: \"" + key + "\" は既知のディレクティブ名ではありません"));
            }
        }
        return errors;
    }

    /** V-DIR: 構造境界 — fw_header にディレクティブ名が含まれていないことを検証する。 */
    private List<ValidationError> validateFwHeader(String filePath, String blockLocation, Map<String, Object> block) {
        List<ValidationError> errors = new ArrayList<>();
        for (String key : castMap(block.get("fw_header")).keySet()) {
            if (KNOWN_DIRECTIVE_NAMES.contains(key)) {
                errors.add(new ValidationError(filePath,
                        blockLocation + ".fw_header",
                        "[V-DIR] 構造境界違反: fw_header にディレクティブ名 \"" + key
                                + "\" が含まれています。directives: に移動してください"));
            }
        }
        return errors;
    }

    /**
     * YAML テキストを解析しトップレベル Map を返す。Map でない場合は {@code null}。
     * 本体と同じ読み込み設定（{@link YamlLoader#loadSettings()}）を使う。
     *
     * @throws YamlEngineException 不正構文・キー重複
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseYaml(String yamlText) {
        Object loaded = new Load(YamlLoader.loadSettings()).loadFromString(yamlText);
        return (loaded instanceof Map) ? (Map<String, Object>) loaded : null;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> castList(Object obj) {
        return (obj instanceof List) ? (List<Object>) obj : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object obj) {
        return (obj instanceof Map) ? (Map<String, Object>) obj : Collections.emptyMap();
    }
}
