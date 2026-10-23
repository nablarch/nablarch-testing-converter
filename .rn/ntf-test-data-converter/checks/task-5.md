# task-5 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| `src/main/java/nablarch/test/core/file/TestCoreFileAdapter.java` が存在する | OK | ファイル存在確認済み | OK | 配置・diff ゼロ確認済み |
| `src/main/java/nablarch/test/core/reader/` に YamlTestCoreAdapter, TestCoreReaderAdapter, StubDbInfo の3件が存在する | OK | 3件存在確認済み | OK | 配置・diff ゼロ確認済み |
| `src/test/java/nablarch/test/core/` 配下にテスト3件＋データが存在する | OK | TestCoreFileAdapterTest, TestCoreReaderAdapterTest, YamlTestCoreAdapterTest + YamlTestCoreAdapterTest/{files,messages,sendSync,tables}.yaml | OK | 配置・diff ゼロ確認済み |
| `mvn test` が全テスト PASS する | OK | Tests run: 277, Failures: 0, Errors: 0, Skipped: 0 | OK | 確認済み |
| 各追加ファイルが本体現ブランチと package/import を除いて完全一致する | OK | src/main/core (4件)・src/test/core (3件) 全件ゼロ diff | OK | 確認済み |

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| src/main 4件の配置・diff ゼロ | OK | 全件確認済み |
| src/test 3件＋データ4件の配置・diff ゼロ | OK | 全件確認済み |
| mvn test 全 PASS（Adapter テスト含む） | OK | YamlTestCoreAdapterTest(15), TestCoreReaderAdapterTest(22), TestCoreFileAdapterTest(7) 全 PASS |

## Overall Verdict

- Self-check: OK
- QA: OK
- Language expert: N/A
- Software-engineering expert: N/A
- Ready for user review: Yes
