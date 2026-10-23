# task-7 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| record-separator の CRLF/LF/CR 各シンボル変換パスをカバーするテストが存在する | OK | 3 メソッド追加。`FakeTestDataReader` が `"CRLF"` を渡す → `DataFile.setDirective` → `LineSeparator.evaluate("CRLF")` → `"\r\n"` として格納 → `normalizeDirectiveValue("\r\n")` → `"CRLF"` のラウンドトリップを正しく通過。SE expert が「pass-through になる」と指摘したが、`DataFile.convertDirectiveValue` の存在を確認し誤りと判定。 | OK | QA: 完了基準 3 件 OK。NONE シンボルが未テストの指摘あり（今回の criteria 外）。 |
| MESSAGE ブロック不在（`readMessageBlock` が null を返す）パスをカバーするテストが存在する | OK | `readMessageBlockAbsentReturnsNullAndIsSkipped` 追加。`openCount` で open 回数を切り替え null ブロックスキップを確認。 | OK | QA: path 正しく到達。openCount ポスト条件アサーションなしの軽微な指摘あり（影響低）。 |
| `mvn test` が全テスト PASS する | OK | `Tests run: 281, Failures: 0, Errors: 0, Skipped: 0`, BUILD SUCCESS | OK | - |

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Meaningful tests/verification | OK | 3 record-separator テストは実ラウンドトリップを通じて正しいパスを検証。MESSAGE テストは null ガードを正しく到達。 |
| Edge case coverage | NG（minor） | NONE シンボル（`"" → "NONE"`）と `field-separator` の `"\t" → "\\t"` ブランチが未カバー。今回の criteria 外だが次タスク以降の課題として記録。 |

## Expert Reviews (code changes only)

### Language Expert

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Best practices | OK | diff 表現の問題の指摘があったが、実際のコミット済みファイルは正しい形式。 |
| Codebase style consistency | OK | GWT マーカー・Javadoc・行スタイルともに既存テストと一致（実ファイル確認済み）。 |
| GWT test format | OK | 実ファイルに `// Given` / `// When` / `// Then` 記載あり。 |

### Software-engineering Expert

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Separation of concerns | OK | SE expert の「pass-through」指摘は `DataFile.convertDirectiveValue` の見落としによる誤り。テストは正しいレイヤーを通じて対象パスを検証。 |
| System integrity | OK | `normalizeDirectiveValue` の CRLF/LF/CR ブランチは `DataFile.setDirective` を経由して実際に到達。 |
| Maintainability | OK（minor） | `openCount` ポスト条件アサーションなし（低影響）。 |
