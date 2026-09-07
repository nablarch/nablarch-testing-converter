# task-15 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| `ImportZipCodeFileActionRequestTest` Excel→YAML 変換で testShots 列順が元のExcelと一致する | OK | YAML の testShots: `no, description, expectedStatusCode, setUpTable, expectedTable, setUpFile, expectedLog, diConfig, requestPath, userId`（Excel 記述順と一致を確認） | OK | E2E 変換後 YAML を実際に確認。列順一致を verified |
| YAML→Excel で戻した testShots 列順が元のExcelと一致する | OK | RoundTripTest.xls_listMap_isPreserved() PASS + E2E ラウンドトリップ成功 | OK | yaml→xls ゴール実行成功、nablarch-example-batch 12 tests PASS |
| LIST_MAP 以外のブロックの列順が保持される（デグレなし） | OK | diff は readListMapBlock・readListMapColumnNames のみ変更。312 tests PASS | OK | テーブル系・ファイル系経路は変更なし |
| マーカーカラム（`[no]` 等）が変換後のYAML/Excelに含まれない | OK | readListMapExcludesMarkerColumns テストで除外確認。HeaderLine.MARKER_COLUMN_CONDITION を再利用 | OK | テストが `[no]` 除外を直接検証 |
| nablarch-example-batch の mvn test が全テスト PASS（テスト件数を報告） | OK | `mvn test -Pyaml-test`: **12 tests run, 0 Failures, 0 Errors** | OK | yaml-test プロファイルで BUILD SUCCESS |
| 本体（nablarch-testing）を変更した場合は本体のテストがすべて PASS する | OK | 本体変更なし（方針A採用） | OK | N/A（本体変更なし） |
| 採用方針と理由が checks/task-15.md に記録されている | OK | 本ファイルに記録 | OK | Javadoc・インラインコメントにも記録あり |
| `mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | **312 tests run, 0 Failures, 0 Errors** | OK | 310→312件（新規2テスト追加） |

## 採用方針と理由

**方針A（本体を変更しない）を採用。**

- `TestCoreReaderAdapter` は本体と同一パッケージ `nablarch.test.core.reader` に相乗りしているため、パッケージプライベートな `HeaderLine` に直接アクセスできる
- 本体の `getMapExcludingMarkerColumns()` の TreeMap は「目視による比較がしやすいのでTreeMapを使用」という意図的な選択であり、変更するとソート順に依存したアサート処理・ログ出力に影響を及ぼすリスクがある
- 方針Aなら本体テストへの影響がゼロで、変更範囲が converter リポジトリのみに限定できる
- `readListMapColumnNames()` で `readBlockBodyLines` + `HeaderLine` を再構築することで、マーカーカラム除外ロジックを再実装せずに本体の `getEffectiveColumnNames()` を利用できる

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Verification approach meaningful to the objective | OK | E2E 変換確認（ImportZipCodeFileActionRequestTest 実ファイル）、12 tests PASS、列順直接確認の3層で検証。「実行した」だけでなく「正しい順で出力された」ことを確認 |

## Expert Reviews (axes the task needs)

### Design Expert

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Approach/structure fits | OK | `HeaderLine` をアダプタ内で使う位置が正しい。YAML側の「値はMapから、列順は別途取得」設計と対称 |
| System-wide integrity | OK | groupId `""` ハードコードは LIST_MAP 仕様上正当。`getEffectiveColumnNames()` と `readListMap()` のキーセットが一致 |

### Craft Expert (coding)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Medium-specific best practice | OK | 命名・エラーハンドリング・null安全・スレッドセーフ問題なし。2パス読みのコメントを追加済み |
| Consistency with existing style | OK | Javadoc 構成・コメントスタイルが既存パターンと一致。`Arrays.asList()` 返却も既存パターンと一貫 |

### Verification Expert (test)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Artifact actually checked | OK | readListMapPreservesColumnOrder（列順保持）、readListMapExcludesMarkerColumns（マーカー除外）、readListMapColumnNames 直接単体テスト2件追加。E2E でも確認済み |
| Coverage (edge cases / claims / steps) | OK | 列順逆順・マーカー除外・ブロック不在・記述順一致の各ケースをカバー |

## Overall Verdict

- Self-check: OK
- QA: OK
- Design expert: OK
- Craft expert: OK
- Verification expert: OK
- Ready to check off: Yes
