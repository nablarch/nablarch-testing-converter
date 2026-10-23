# task-13 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| `nablarch-core-dataformat` がcompileスコープになっている | OK | pom.xml から `<scope>test</scope>` を削除し、デフォルト compile スコープに変更した |
| 本体コード（src/main）が参照する依存がすべてcompileスコープになっている | OK | 全 src/main/java ファイルの import を精査した結果、外部ライブラリの依存先は以下の通りですべて compile スコープ: `com.networknt:json-schema-validator` (compile)、`org.snakeyaml:snakeyaml-engine` (compile)、`org.apache.poi:poi-ooxml` (compile)、`com.nablarch.framework:nablarch-testing` (compile/no scope = compile)、`com.nablarch.framework:nablarch-testing-yaml` (compile)、`com.nablarch.framework:nablarch-core-dataformat` (compile、今回修正)、Maven plugin API (`maven-plugin-api`, `maven-plugin-annotations`) は provided スコープで正しい。test スコープのまま残すべき依存 (`junit`, `mockito-core`, `nablarch-test-support`, `h2`, `nablarch-backward-compatibility`, `nablarch-fw-messaging`, `nablarch-fw-messaging-mom`) はすべて src/test からのみ参照されている |
| `mvn clean install` が通る | OK | BUILD SUCCESS (Total time: 29.352 s, Finished at: 2026-07-13T10:42:32+09:00) |

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Verification approach meaningful to the objective | OK | Root cause confirmed: `nablarch-testing` excludes `nablarch-core-dataformat` from transitive deps; MessagePool bytecode references `FixedLengthDataRecordFormatter` at runtime. Fix directly addresses this. |

## Expert Reviews

### Craft Expert (software engineering)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Medium-specific best practice | OK | compile scope is correct; plugin artifacts don't propagate to consumers' classpaths. `optional` not needed. |
| Consistency with existing style | OK | No change to style; single line removal. |

## Overall Verdict

- Self-check: OK
- QA: OK
- Design expert: N/A
- Craft expert: OK
- Verification expert: N/A
- Ready to check off: Yes
