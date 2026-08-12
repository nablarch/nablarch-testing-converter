# task-18 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| `coverage/inventory.md` に4辺ぶんの棚卸し表があり、既存126件（33+20+40+33）の全テストメソッド名が漏れなく載っている | OK | 実測件数 `grep -c '@Test'` → XlsFormatReaderTest 33 / YamlFormatReaderTest 20 / XlsFormatWriterTest 40 / YamlFormatWriterTest 33 ＝ 126（steering 想定値と完全一致、差異なし）。全メソッド名の突合スクリプトを実行し **missing count = 0**（4クラスの `public void <name>` 全件が inventory.md 中に `` `<name>` `` として出現）。逆向き検査（inventory 中の関数名が実在するか）も実施し、非実在は 0 件（唯一の検出 `read` は SUT メソッド名の散文言及・L779） | | |
| 軸A の要素が `DataType` の実定義と一致している（14種でない場合はその旨と実際の要素が記録されている） | OK | 実定義 `/home/tie303177/work/nablarch/nablarch-testing/src/main/java/nablarch/test/core/reader/DataType.java` L8-56 を読み、全 enum 定数を列挙 → `DEFAULT`(L11), `SETUP_TABLE_DATA`(L14), `EXPECTED_TABLE_DATA`(L17), `EXPECTED_COMPLETED`(L23), `LIST_MAP`(L29), `SETUP_FIXED`(L32), `EXPECTED_FIXED`(L35), `SETUP_VARIABLE`(L38), `EXPECTED_VARIABLE`(L41), `MESSAGE`(L44), `EXPECTED_REQUEST_HEADER_MESSAGES`(L47), `EXPECTED_REQUEST_BODY_MESSAGES`(L50), `RESPONSE_HEADER_MESSAGES`(L53), `RESPONSE_BODY_MESSAGES`(L56) の **14 種（DEFAULT ＋13）**。steering 記述と一致・差異なし。inventory.md §0.2 に定義行番号・`getType()`・`getName()` つきで記録 | | |
| 軸C の対象フィールドが実クラスの定義と一致し、省略可能なフィールドが識別されている | OK | `src/main/java/nablarch/test/tool/converter/model/` の8クラスを全文 Read し `private final` 宣言を列挙 → 全 **21 フィールド**（TestDataContainer 2 / TestDataSection 2 / TestDataBlock 3 / ColumnRowDataBlock 2 / FileDataBlock 3 / MessageDataBlock 3 / RecordLayout 3 / FieldDef 3）。省略区分は Javadoc 記述を根拠に3分類 — **省略可能 4 件**（`TestDataBlock.groupId` L28/L41「省略時は空文字」、`RecordLayout.recordType` L26/L36「省略時は null」、`FieldDef.type` L25/L38、`FieldDef.length` L25/L43）／空許容コレクション 11 件／必須スカラー 6 件。inventory.md §0.4 に表として記録。steering #20 が挙げる `identifier`・`fileType` には実定義上「省略」表現がない点を差異として明記し、判断依頼として §5.3-1 に記載 | | |
| 各辺について未担保の軸要素が一覧化されており、以降のタスクが埋めるべき対象が確定している | OK | 辺ごとに「未担保一覧」節を設置 — §1.3 辺①（41件）／§2.3 辺②（26件）／§3.3 辺③（27件）／§4.3 辺④（13件）、合計 107 件。§5.1 に辺×軸のクロス表を掲載。軸D は steering #19/#22/#24/#25 の記述を D1-01〜17 / D2-01〜10 / D3-01〜08 / D4-01〜09 として要素 ID 化し、軸E は E-1〜E-4 × 0/1/複数、軸F は F1-01〜06 / F2-01〜05 / F3-01〜04 / F4-01〜03 として ID 化。各未担保要素に担当タスク（#19〜#25）を明示。判断が割れる要素は「🔺弱い担保」として区別し理由を併記、コーディネータ判断が要る 5 点を §5.3 に列挙 | | |
| src/main・src/test への変更がゼロ（棚卸しのみ） | OK | `git status --porcelain -- src` → 出力なし。`git diff --stat -- src` / `git diff --cached --stat -- src` → 出力なし。`git status --porcelain` の全出力は `?? .rn/ntf-test-data-converter/checks/task-18.md` と `?? .rn/ntf-test-data-converter/coverage/` の2行のみ（いずれも本タスクで新規作成した成果物）。steering.md も未編集 | | |

## 棚卸しで判明した主な事実（後続タスクへの申し送り）

| # | 事実 | 影響するタスク |
|---|---|---|
| 1 | 辺①既存33件は全て `FakeTestDataReader`（`XlsFormatReaderTest` L54-102）経由で、実セル→文字列行の区間を通らない。軸D 17ケースが全滅 | #19 |
| 2 | ただし `XlsFormatWriterTest#roundTrips*` 8件は実 `.xlsx` を書いて `new XlsFormatReader()`（本番配線）で読み戻している（L861-865）。#19 の「実 `.xlsx` 入力・`FakeTestDataReader` 非経由」は既に部分的に成立 | #19 |
| 3 | 辺②既存20件は `loadRawMap` を in-memory Map に差し替え（`YamlFormatReaderTest` L538-545）、YAML パーサを通らない。軸D 10ケースが全滅 | #24 |
| 4 | `getCellType()` を使うテストは src/test 全体で 0 件（`grep -rn "getCellType" src/test/`）。軸D 辺③8ケースが全滅 | #22 |
| 5 | `MessageDataBlock.directives`（C-13）は辺②で値あり・空とも 0 件、辺③で値あり 0 件（`new MessageDataBlock` 6箇所すべて 4引数目が空 `map()`: XlsFormatWriterTest L405,432,543,833,1033,1060） | #23, #24 |
| 6 | `overwrite` を保持するのは `ConversionRequest`/`TestDataConverter`/`ConverterMojo` のみで、`XlsFormatWriter`/`YamlFormatWriter` は保持しない（`grep -rln "overwrite" src/main/java`）。F3-02/F4-02 は writer 単体では再現不能の可能性 | #22, #25 |
| 7 | `EXPECTED_FIXED`（A-07）は4辺中 辺②のみ担保。4辺横断で最弱の軸A要素 | #20, #23, #25 |

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|

## Expert Reviews

### Craft Expert (writing)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|

### Verification Expert (fact-check)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|

## Overall Verdict

- Self-check: OK
- QA: 未実施
- Craft expert: 未実施
- Verification expert: 未実施
- Ready to check off: QA / Craft / Verification レビュー後に判定
