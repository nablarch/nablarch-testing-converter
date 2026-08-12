# task-20 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 軸A の 14 種すべてが、実 `.xlsx` を入力とするテストで 1 回以上中間モデルへ入っている | OK | 13 種を新規 `XlsFormatReaderRealFileTest`（**17 `@Test`**。修正ラウンドで 14 → 17）が実 `.xlsx` 経由で担保。A-01 `DEFAULT` のみ到達不能（`TestCoreReaderAdapter` L362）で理由付きの空欄（詳細は Evidence 1） | | |
| 軸B の 4 種すべてが実 `.xlsx` 経由で生成されアサートされている | OK | `readsFourBlockImplementationsFromOneRealSheet` が 1 シートから 4 種の具象クラスを生成し、各種別の個別テストが `instanceOf` でも確認（詳細は Evidence 2） | | |
| 軸C の全フィールドが非デフォルト値で 1 回以上アサートされ、省略可能なフィールドは省略時の挙動もアサートされている | OK | 21 フィールドすべてを実 `.xlsx` 経由でアサート。省略側は C-06(`""`)・C-21(`null`)・**C-08(空)** を実測固定、C-16 は `null` ではなく `""` になることを実測して固定（XLS-06）。**修正ラウンドで C-08 を #21 送りから本タスク担保へ、C-17 を #21 送りから到達不能へ変更**。到達不能 6 件・#21 送り 4 件は根拠付きで空欄（詳細は Evidence 3） | | |
| `fileType` の `FIXED` / `VARIABLE` 双方がアサートされている | OK | FIXED: `readsSetupFixedFileBlockFromRealBook`・`readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook`／VARIABLE: `readsSetupVariableFileBlockWithoutFieldLengthFromRealBook`・`readsExpectedVariableFileBlockWithGroupIdFromRealBook` | | |
| src/main への変更がゼロ | OK | `git diff --stat 9824064..HEAD -- src/main` → 出力なし。`git status --porcelain -- src/main` → 出力なし（詳細は Evidence 5） | | |
| `mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | 修正ラウンド後: `Tests run: 354, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`（作業前 337 → +17。リグレッションゼロ） | | |

### Evidence 1 — 軸A（`DataType` 14 種）× 担保テストメソッド

追加ファイルは 1 件のみ: `src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderRealFileTest.java`（**17 `@Test`**）。
全メソッドが `new XlsFormatReader()`（本番配線＝`PoiXlsReader`）に `XlsFixture` が POI で書き出した
実 `.xlsx` のパスを渡す。`FakeTestDataReader`（`XlsFormatReaderTest` の private 内部クラス）は経由しない。
Excel 上の記法と入力値は既存 33 件を参考にしており、混在シート・送信同期は入力ごと移植している。
組み直したのは入力経路（実 `.xlsx` → 本番配線）とアサーション対象である
（**修正ラウンドで「既存 33 件は流用せず組み直した」という当初の記述を実態に合わせて訂正した**）。

| 軸A | マーカー記法 | 担保テストメソッド（`XlsFormatReaderRealFileTest#`） |
|---|---|---|
| A-01 `DEFAULT` | — | **到達不能**（テストなし）。`TestCoreReaderAdapter` L362 の `HeaderCollector#parse` が `getDataType(first) == DataType.DEFAULT` の行を `continue` でスキップするため、ヘッダ一覧に `DEFAULT` のブロックが載らない。`XlsFormatReader#read` L105-131 は `headers` の各要素しか見ないため、リーダ経路では `DEFAULT` のブロックが生成されない |
| A-02 `SETUP_TABLE_DATA` | `SETUP_TABLE=USERS` | `readsSetupTableBlockFromRealBook`, `readsEmptyColumnNamesFromMarkerOnlyTableInRealBook`(新), `readsFourBlockImplementationsFromOneRealSheet` |
| A-03 `EXPECTED_TABLE_DATA` | `EXPECTED_TABLE[g1]=ROLES` | `readsExpectedTableBlockWithGroupIdFromRealBook` |
| A-04 `EXPECTED_COMPLETED` | `EXPECTED_COMPLETE_TABLE=DEPTS` | `readsExpectedCompletedTableBlockFromRealBook` |
| A-05 `LIST_MAP` | `LIST_MAP=testShots` | `readsListMapBlockFromRealBook`, `readsEmptyColumnNamesFromMarkerOnlyListMapInRealBook`(新), `readsFourBlockImplementationsFromOneRealSheet` |
| A-06 `SETUP_FIXED` | `SETUP_FIXED=test.dat` | `readsSetupFixedFileBlockFromRealBook`, `readsOmittedFieldLengthNotationFromRealBook`(新), `readsOmittedRecordTypeAsEmptyStringFromRealBook`, `readsFourBlockImplementationsFromOneRealSheet` |
| A-07 `EXPECTED_FIXED` | `EXPECTED_FIXED=expected.dat` | `readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook` |
| A-08 `SETUP_VARIABLE` | `SETUP_VARIABLE=in.csv` | `readsSetupVariableFileBlockWithoutFieldLengthFromRealBook` |
| A-09 `EXPECTED_VARIABLE` | `EXPECTED_VARIABLE[g2]=out.csv` | `readsExpectedVariableFileBlockWithGroupIdFromRealBook` |
| A-10 `MESSAGE` | `MESSAGE=msg1` | `readsMessageBlockFromRealBook`, `readsFourBlockImplementationsFromOneRealSheet` |
| A-11 `EXPECTED_REQUEST_HEADER_MESSAGES` | `EXPECTED_REQUEST_HEADER_MESSAGES[case1]=RM01` | `readsAllFourSendSyncMessageTypesFromRealBook`（識別子・ディレクティブ・レコード内容まで固定） |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | `EXPECTED_REQUEST_BODY_MESSAGES[case1]=RM02` | `readsAllFourSendSyncMessageTypesFromRealBook` |
| A-13 `RESPONSE_HEADER_MESSAGES` | `RESPONSE_HEADER_MESSAGES[res_case1]=RM03` | `readsAllFourSendSyncMessageTypesFromRealBook` |
| A-14 `RESPONSE_BODY_MESSAGES` | `RESPONSE_BODY_MESSAGES[res_case1]=RM04` | `readsAllFourSendSyncMessageTypesFromRealBook` |

- A-01 の根拠は自分でソースを読んで確認した（`TestCoreReaderAdapter.java` L353-372 の `HeaderCollector#parse`。
  L362 が `if (type == DataType.DEFAULT) { continue; }`）。
- #18 §0.8-8 のとおり A-04／A-07／A-09 は `RoundTripTest` が実ファイル経由で 🔺 担保していたが、
  往復ではなく**直接テスト**として本タスクで正式に固定した（steering Rules に従い往復テストは正式担保に数えない）。
  `RoundTripTest` には手を加えていない。

### Evidence 2 — 軸B（`TestDataBlock` 具象 4 種）× 担保テストメソッド

| 軸B | 担保テストメソッド | アサート内容 |
|---|---|---|
| B-1 `TableDataBlock` | `readsSetupTableBlockFromRealBook`（`instanceOf`）, `readsFourBlockImplementationsFromOneRealSheet` | 生成クラス ＋ 列名・行 |
| B-2 `ListMapBlock` | `readsListMapBlockFromRealBook`（`instanceOf`）, `readsFourBlockImplementationsFromOneRealSheet` | 生成クラス ＋ 列順・行 |
| B-3 `FileDataBlock` | `readsSetupFixedFileBlockFromRealBook`（`instanceOf`）, `readsFourBlockImplementationsFromOneRealSheet` | 生成クラス ＋ `fileType`・ディレクティブ・レコードレイアウト |
| B-4 `MessageDataBlock` | `readsMessageBlockFromRealBook`（`instanceOf`）, `readsAllFourSendSyncMessageTypesFromRealBook`, `readsFourBlockImplementationsFromOneRealSheet` | 生成クラス ＋ FW ヘッダ・レコードレイアウト |

`readsFourBlockImplementationsFromOneRealSheet` は 1 シートに 4 種のマーカーを置き、
生成されたブロックの `getClass()` に 4 種すべてが現れることを確認する。

### Evidence 3 — 軸C（中間モデル 21 フィールド）× 担保テストメソッド

「値あり／非空」「省略／空」を分けて評価する（#18 §0.4 の区分に従う）。
n/a は「省略」「空」という状態を持たない必須スカラー・2 値。

| 軸C | 値あり／非空 | 省略／空 | 担保テストメソッド（`XlsFormatReaderRealFileTest#`） |
|---|---|---|---|
| C-01 `TestDataContainer.name` | OK | n/a | `readsContainerAndSectionNamesFromRealBookAndSheetNames`（`MyBook`） |
| C-02 `TestDataContainer.sections` | OK(1 件) | **到達不能** | `readsContainerAndSectionNamesFromRealBookAndSheetNames`。「空」「複数」は `XlsFormatReader#read` L133 が `Collections.singletonList(section)` を返すため構造上生成されない |
| C-03 `TestDataSection.name` | OK | n/a | `readsContainerAndSectionNamesFromRealBookAndSheetNames`（`MySheet`） |
| C-04 `TestDataSection.blocks` | OK | OK(空) | 非空: `readsFourBlockImplementationsFromOneRealSheet` ほか 12 件／空: `readsEmptyBlockListFromRealSheetWithoutMarkers` |
| C-05 `TestDataBlock.dataType` | OK | n/a | 全ブロック系テスト 10 件が `getDataType()` を直接アサート |
| C-06 `TestDataBlock.groupId` | OK | OK(`""`) | 値あり: `readsExpectedTableBlockWithGroupIdFromRealBook`（`[g1]`）, `readsExpectedVariableFileBlockWithGroupIdFromRealBook`（`[g2]`）, `readsAllFourSendSyncMessageTypesFromRealBook`（`[case1]`/`[res_case1]`）／省略: `readsSetupTableBlockFromRealBook`, `readsListMapBlockFromRealBook`, `readsMessageBlockFromRealBook` が `""` をアサート |
| C-07 `TestDataBlock.identifier` | OK | n/a | `readsSetupTableBlockFromRealBook` ほか 8 件 |
| C-08 `ColumnRowDataBlock.columnNames` | OK | **OK(空)** | 非空: `readsSetupTableBlockFromRealBook`, `readsListMapBlockFromRealBook`／空: `readsEmptyColumnNamesFromMarkerOnlyTableInRealBook`, `readsEmptyColumnNamesFromMarkerOnlyListMapInRealBook`（マーカー列だけのブロックで到達。`issues.md` XLS-08）。**修正ラウンドで #21 送りから本タスク担保へ変更**（軸E の 4 観点に「列名 0 件」に対応する要素は無く、#21 のスコープからも脱落していた） |
| C-09 `ColumnRowDataBlock.rows` | OK | **#21 へ送る** | 非空: 同上（複数行も含む）／空は E-2(0 件) と同一の入力になるため #21 |
| C-10 `FileDataBlock.fileType` | OK(FIXED/VARIABLE 双方) | n/a | FIXED: `readsSetupFixedFileBlockFromRealBook`, `readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook`／VARIABLE: `readsSetupVariableFileBlockWithoutFieldLengthFromRealBook`, `readsExpectedVariableFileBlockWithGroupIdFromRealBook` |
| C-11 `FileDataBlock.directives` | OK | **到達不能** | 非空: `readsSetupFixedFileBlockFromRealBook`（`text-encoding`）, `readsExpectedVariableFileBlockWithGroupIdFromRealBook`（`record-separator`）／空: 本体 `DataFile` コンストラクタ L92 が `file-type` を必ず注入するため空 Map にならない（`issues.md` XLS-07）。`readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook` が「ディレクティブ行 0 行でも `{file-type=Fixed}` になる」ことを固定し、到達不能の根拠をテストで示す |
| C-12 `FileDataBlock.records` | OK | **#21 へ送る** | 非空: `readsSetupFixedFileBlockFromRealBook`／空は E-3(0 件) と同一のため #21 |
| C-13 `MessageDataBlock.directives` | OK | **到達不能** | 非空: `readsMessageBlockFromRealBook`（`text-encoding` ＋ 注入される `file-type` ＋ 件数 2）／空: C-11 と同じ理由（`issues.md` XLS-07）。**修正ラウンドで `readsAllFourSendSyncMessageTypesFromRealBook` のループ内に `getDirectives()` のアサートを追加し、到達不能の根拠をテストで示すようにした**（それまでは台帳が実在しないアサーションを指していた＝空証明） |
| C-14 `MessageDataBlock.fwHeaderFields` | OK | OK(空) | 非空: `readsMessageBlockFromRealBook`（`requestId=R1`）／空: `readsAllFourSendSyncMessageTypesFromRealBook`（送信系 4 種すべてで `isEmpty()`） |
| C-15 `MessageDataBlock.records` | OK | **#21 へ送る** | 非空: `readsMessageBlockFromRealBook`／空は E-3(0 件) と同一のため #21 |
| C-16 `RecordLayout.recordType` | OK | **到達不能（`null`）** | 値あり: `readsSetupFixedFileBlockFromRealBook`（`data`）, `readsMessageBlockFromRealBook`／省略: 実 `.xlsx` 経路では `null` にならず `""` になることを `readsOmittedRecordTypeAsEmptyStringFromRealBook` で固定した（`issues.md` XLS-06） |
| C-17 `RecordLayout.fields` | OK | **到達不能** | 非空: `readsSetupFixedFileBlockFromRealBook`, `readsMessageBlockFromRealBook`（フィールド名リストを順序込みでアサート）／空: 名前行が 2 列未満だと本体 `DataFileParser` L234 が `directive or data names row must have two columns at least.` で失敗する（`SETUP_FIXED`／`MESSAGE` 双方で実測）。**修正ラウンドで #21 送りから到達不能へ変更**（`issues.md`「到達不能」表） |
| C-18 `RecordLayout.rows` | OK | **#21 へ送る** | 非空: `readsSetupFixedFileBlockFromRealBook` ほか 5 件／空は #21 |
| C-19 `FieldDef.name` | OK | n/a | `readsSetupFixedFileBlockFromRealBook`, `readsMessageBlockFromRealBook`, `readsSetupVariableFileBlockWithoutFieldLengthFromRealBook` |
| C-20 `FieldDef.type` | OK | **到達不能（`null`）** | 値あり: `readsSetupFixedFileBlockFromRealBook`（`半角英字`）ほか 3 件／省略: 型行が名前行より短い・型セルが空のいずれでも本体パーサが失敗するためモデルに `null` が入らない（`issues.md` XLS-07 直下の「到達不能」表。例外自体は軸F F1-06 として #21） |
| C-21 `FieldDef.length` | OK | OK(`null`) | 値あり: `readsSetupFixedFileBlockFromRealBook`（`"10"`/`"5"`）, `readsMessageBlockFromRealBook`（`"10"`）, **`readsOmittedFieldLengthNotationFromRealBook`（省略記法 `"-"` の原文復元。修正ラウンドで追加）**／省略: `readsSetupVariableFileBlockWithoutFieldLengthFromRealBook`（可変長は長さ行を持たないため `null`） |

#### 3-1. 「#21 へ送る」4 件の根拠（修正ラウンドで 6 件 → 4 件）

C-09／C-12／C-15／C-18 の「空」は、いずれも**コレクションが 0 件**の状態であり、
軸E の「0 件」（E-2 ブロック内行数 0／E-3 ファイル内レコードレイアウト数 0）と同じ入力になる。
コーディネータ指示のとおり #21（軸E 多重度）の担当とし、本タスクでは無理に作り込まなかった。

当初は C-08／C-17 も同じ理由で #21 送りにしていたが、**どちらも根拠が誤っていた**（レビュー指摘）。

- **C-08 `columnNames` 空** — 軸E の 4 観点（E-1 セクション内ブロック数／E-2 ブロック内行数／
  E-3 ファイル内レコードレイアウト数／E-4 コンテナ内セクション数）に「列名 0 件」に対応する要素は無く、
  `inventory.md` §1.3 の #21 スコープにも入っていなかった（＝計画から脱落する）。
  実際にはマーカー列だけのテーブル／LIST_MAP で到達するため、**本タスクで担保した**。
- **C-17 `fields` 空** — 名前行が 2 列未満だと本体パーサが失敗するため**到達不能**であり、
  #21 の軸E（0 件）では到達できない。根拠付きで `issues.md` の「到達不能」表へ移した。

#### 3-2. `inventory.md` §1.3 からの分類変更（コーディネータ判断が要る点）

実 `.xlsx` を実際に読ませて判明した事実にもとづき、次の 3 要素を「要追加」から「到達不能」へ変更した。
`inventory.md` 本体は本タスクの担当外のため書き換えていない（#27 の対応表で反映するかはコーディネータ判断）。

| 軸要素 | #18 の分類 | #20 の分類 | 根拠 |
|---|---|---|---|
| C-11 `FileDataBlock.directives` 空 | 要追加 | 到達不能 | `DataFile` コンストラクタ L92 が `file-type` を必ず注入（`issues.md` XLS-07） |
| C-13 `MessageDataBlock.directives` 空 | 要追加 | 到達不能 | 同上 |
| C-16 `recordType` 省略（`null`） | 要追加 | 到達不能 | 実 `.xlsx` 経路では `""` になる（`issues.md` XLS-06） |
| C-17 `RecordLayout.fields` 空 | 要追加（#21 送り） | 到達不能 | 名前行が 2 列未満だと本体 `DataFileParser` L234 が失敗（`issues.md` の「到達不能」表。**修正ラウンドで追加**） |
| C-20 `FieldDef.type` 省略（`null`） | 要追加 | 到達不能 | 本体パーサが型の欠落を 2 通りの機構で弾く（`issues.md` の「到達不能」表。**修正ラウンドで機構の記述を訂正**） |
| C-08 `columnNames` 空 | 要追加（#21 送り） | **本タスクで担保** | マーカー列だけのブロックで到達（`issues.md` XLS-08。**修正ラウンドで変更**） |

**修正ラウンドで `inventory.md` §1.3 を実際に更新した**（当初は「本タスクの担当外」として書き換えていなかった）。
§1.2-2 を新設して #19／#20 の担保を軸要素別に記し、§1.3 の分類・件数を
「要追加 11 ／ 担保済み 22 ／ 到達不能 8」へ改めた。#18 時点の記述は消していない。

### Evidence 4 — 「期待値を先に決めていない」ことの根拠

1. まず使い捨てのプローブテスト 3 本（`ProbeTest` / `Probe2Test` / `Probe3Test`）を `src/test` に置き、
   14 種のマーカー・ディレクティブ有無・レコード種別省略・型行の欠落・マーカー無しシートについて
   `XlsFormatReader` の読み取り結果を標準出力へダンプした。
2. そのダンプを読んでから、観測値をテストのアサーションへ写した。
   `readsOmittedRecordTypeAsEmptyStringFromRealBook`（`""`）・
   `readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook`（`{file-type=Fixed}`）・
   可変長の `field-separator=,` は、いずれも**書く前には予想していなかった**観測結果である。
3. プローブは 3 本ともコミット前に削除した（`git status --porcelain` に残っていない）。
4. 仕様として妥当でないと判断した 2 件を `coverage/issues.md` に XLS-06／XLS-07 として記録した。
   **修正はしていない。**

| 実測 | 観測値 | 扱い |
|---|---|---|
| レコード種別を省略した固定長ファイル | `recordType == ""` | XLS-06（仕様として不適切・非対称） |
| ディレクティブ行 0 行の固定長ファイル | `{file-type=Fixed}` | XLS-07（記録のみ） |
| ディレクティブ行 1 行（`record-separator`）の可変長ファイル | `{file-type=Variable, record-separator=CRLF, field-separator=,}` | XLS-07（記録のみ） |
| 型行が名前行より短い固定長ファイル | `IllegalStateException: can't get data`（原因 `IllegalArgumentException: field name size is 2. but types size is 1.`） | C-20 到達不能の根拠。例外の固定は #21（軸F） |
| レコード種別省略の中間モデルを YAML へ書いた結果 | `record_type: ""`（キーが残る） | XLS-06 の影響の裏取り |

### Evidence 5 — src/main 無変更

```sh
git diff --stat ef80d30..HEAD -- src/main   # → 出力なし（ef80d30 が #20 の基点）
git status --porcelain -- src/main          # → 出力なし
```

コミットに含めた変更は次の 3 件のみ（修正ラウンド後）。

| パス | 変更 |
|---|---|
| `src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderRealFileTest.java` | 新規 → 修正ラウンドで 17 `@Test` へ |
| `.rn/ntf-test-data-converter/coverage/issues.md` | XLS-06〜XLS-09 と「到達不能」表。修正ラウンドで凡例・行番号・YAML スニペットを訂正 |
| `.rn/ntf-test-data-converter/coverage/inventory.md` | 修正ラウンドで §1.2-2 新設・§1.3 更新・冒頭注記 |

- `XlsFixture` の API は変更していない（#19 Design レビュー D-1 の判定どおり）。内部実装も無変更。
  本タスクで必要だったセル指定は `text()` / `blank()` の 2 つだけで、不足は無かった。
- 既存テストの削除・書き換えはしていない（追加のみ）。

### Evidence 6 — JaCoCo カバレッジ（作業前後）

手順は steering Decisions のとおり:

```sh
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean jacoco:instrument test jacoco:restore-instrumented-classes
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec
```

「作業前」は新規テストクラスを一時的に退避して同じ手順を実行したもの。

**注意（計測時に踏んだ落とし穴）**: `jacoco.exec` はプロジェクトルートに**追記**される。
消さずに 2 回計測すると前回の実行結果が混ざり、どの回も同じ数値になる（実際に一度そうなった）。
下表は各回の前に `rm -f jacoco.exec` してから計測し直したものである。

#### 6-1. `XlsFormatReader` の行・分岐カバレッジ

| 計測条件 | 行（カバー/全 188） | 行率 | 分岐（カバー/全 136） | 分岐率 |
|---|---|---|---|---|
| 作業前（全 337 件。新規クラスを退避） | 182 / 188（未到達 6） | 96.8% | 109 / 136（未到達 27） | 80.1% |
| 作業後（全 354 件・修正ラウンド後に再計測） | 182 / 188（未到達 6） | 96.8% | 109 / 136（未到達 27） | 80.1% |
| 参考: 新規クラス単独（14 件のみ） | 174 / 188（未到達 14） | 92.6% | 94 / 136（未到達 42） | 69.1% |

- **作業前後で数値は変わらない。** 本タスクが追加した 17 件は、`XlsFormatReader` の未到達行・未到達分岐を
  1 つも減らしていない。既存スイート（Fake リーダ経路 33 件 ＋ `RoundTripTest` の XLS 経路 13 件ほか）が
  同じ行・分岐を既に通しているためである。本タスクの価値はカバレッジの数値ではなく、
  **往復（🔺 弱い担保）や Fake 経路でしか触れていなかった軸要素を、実 `.xlsx` 入力の直接アサートに
  置き換えたこと**にある（steering Rules: 往復テストを正式担保に数えない）。
- 「新規クラス単独」の行は、計測が**テスト選択に反応している**ことの確認として載せた
  （同じ手順で数値が下がる＝上の「前後で同じ」は計測ミスではない）。

**修正ラウンド後に再計測した（2026-08-12）。** 追加した 3 件（`readsOmittedFieldLengthNotationFromRealBook` /
`readsEmptyColumnNamesFromMarkerOnlyTableInRealBook` / `readsEmptyColumnNamesFromMarkerOnlyListMapInRealBook`）を
含めても行 182/188・分岐 109/136 で変わらない。「参考: 新規クラス単独」の行は再計測していない
（14 件時点の値）。

#### 6-2. `XlsFormatReader` の未到達箇所（作業後・全 354 件時点）

未到達行 6:

| 行 | 内容 | 位置づけ |
|---|---|---|
| L231 | `readMessageBlock` の `return null`（MESSAGE マーカーはあるが本体パーサが空を返す） | 現行スイートでは未到達。#21 の軸F または #26 の判定対象 |
| L300 | `toRecordLayouts` の `fragments.isEmpty()` 時の `return records` | **#21 の軸E E-3(0 件)** で到達しうる |
| L344 | `verifyNameRow` の `throw new IllegalStateException` | 内部整合性ガード（#6/#8 で「維持が正しい」と判定済み） |
| L429 | `requireLine` の `throw new IllegalStateException` | 同上 |
| L499 | `isQuotationWrapped` の `return false`（`null` または 2 文字以下） | ディレクティブ値が 2 文字以下になる入力が現行スイートに無い。#26 の判定対象 |
| L533 | `stripQuotes` の `null` ガード（`return null`） | #6 で load-bearing と判定済み — **その判定根拠は誤り**（実 Excel 経路では `valueCells.get(i)` は `null` にならない）。修正ラウンドで裏を取り `issues.md` XLS-09 に記録した（`src/main` は無変更） |

未到達分岐 27（行ごとの内訳）:

| 行 | 未到達 | 内容 |
|---|---|---|
| L112 / L116 / L120 / L126 | 各 1/2 | 同一 (データタイプ, グループ) の 2 回目のヘッダを `processed` で弾く側（ファイル系／LIST_MAP／MESSAGE／送信系）。テーブル系（L108）と送信系（L127）は到達済み |
| L122 | 1/2 | `readMessageBlock` が `null` を返す側（L231 と同根） |
| L187 | 1/2 | LIST_MAP の値が `null` の側 |
| L228 | 1/2 | `message == null` の側（L231 と同根） |
| L299 | 1/2 | 断片 0 件の側（#21 E-3(0 件)） |
| L322 | 1/4 | `skipToFirstNameRow` のループ条件 |
| L343 | 2/4 | `verifyNameRow` の不整合側（L344 と同根） |
| L378 | 1/2 | `FieldDef.type` の `null` フォールバック（**到達不能**。`issues.md`「到達不能」表） |
| L379 | 1/4 | `FieldDef.length` の長さ行が短い側 |
| L406 | 1/2 | 値行が名前行より短いときの `""` フォールバック（軸F F1-06 相当。#21） |
| L428 | 1/2 | `requireLine` の不整合側（L429 と同根） |
| L474 | 1/2 | `field-separator` が実タブでない側 |
| L498 / L501 / L502 | 2/4・1/4・3/4 | `isQuotationWrapped` の `null`・2 文字以下・全角ダブルクォート記法（L499 と同根） |
| L513 | 1/2 | `tail` の空リスト側 |
| L532 | 1/2 | `stripQuotes` の `null` 側（L533 と同根） |
| L563 | 1/2 | 同じ重複カラム名で 2 回目以降の WARN を抑止する側 |
| L640 / L651 | 各 1/2 | リソース名に `/` が無い場合（`bookName` / `sheetName`）。`XlsFormatReader#read` の API 契約は `"ブック名/シート名"` であり、`/` 無しは防御的フォールバック |

- 未到達分岐の分類（テストを足すべき／不要）の最終判定は #26 の担当であるため、ここでは事実の列挙に留める。
- 本タスクの範囲で埋められる未到達は無かった（埋められるものはいずれも #21 の軸E・軸F の入力を要する）。

### Evidence 7 — 修正ラウンド（レビュー指摘対応・2026-08-12）

#### 7-1. 対応した指摘

| ID | 指摘 | 対応 |
|---|---|---|
| A-1 | XLS-07 の担保テストが実在しないアサーションを指していた（空証明） | `readsAllFourSendSyncMessageTypesFromRealBook` のループ内で `getDirectives()` の内容と件数をアサート |
| A-2 | MESSAGE の directives が非対称（`text-encoding` しか見ていない） | 注入される `file-type` と件数 2 を固定 |
| A-3 | 送信同期 4 種が `dataType`／`groupId` しか固定しておらず、識別子も全て `RM01` で取り違えを検出できない | 識別子を `RM01`〜`RM04` と別々にし、1 対 1 で突き合わせ。要求ヘッダ 1 種は `recordType`／`fields`／`rows` まで固定 |
| A-4 | 混在シートのアサーションが弱い | identifier（`T`/`lm`/`f.dat`/`m`）まで突き合わせ、`assertTrue` を `assertThat(..., hasItem(...))` へ |
| A-5 | C-08 `columnNames` 空が誤った根拠で #21 送りにされていた | マーカー列だけのテーブル／LIST_MAP で実測し、2 件のテストで担保。`rows=[[]]` は `issues.md` XLS-08 に記録 |
| A-6 | C-17 `fields` 空が未調査 | 実測して**到達不能**と判定し、根拠付きで `issues.md` の「到達不能」表へ記録 |
| A-7 | 長さ省略記法 `-` が実 `.xlsx` 経路で未担保 | `readsOmittedFieldLengthNotationFromRealBook` を追加 |
| B-1 | C-20 到達不能の機構の取り違え | 実測し直して 2 通りの機構へ書き換え（`assertSameSizeAsNames` 宣言 L339 も訂正） |
| B-2 | XLS-06 の YAML スニペットの真偽が割れていた | プローブを実行し直し、**切り詰められていた**ことを確認して全文へ差し替え |
| B-3 | `toRecordLayouts` L305 → L306 のズレ | 訂正。`issues.md` 中の引用行番号を全件突き合わせ、`PoiXlsReader#readLine`（L83-99→L83-98）と `isBlankLine`（L136-143→L140-147）も訂正 |
| B-4 | 凡例「影響度 高」の定義が旧いまま | 「影響度 中」と同じ言い回しへ揃え、訂正注記を追加 |
| B-5 | `XlsFormatReader` L531 のコメントが実挙動と矛盾 | 裏を取って**事実と確認**し、`issues.md` XLS-09 として記録（`src/main` は無変更） |
| C-1 | `inventory.md` §1.3 が #20 の実測結果と食い違う | §1.3 を更新（要追加 11 ／ 担保済み 22 ／ 到達不能 8）。§1.2-2 を新設。冒頭に更新注記 |
| D-1〜D-9 | コードの作法 | クラス Javadoc の到達不能一覧を唯一の索引として補完（D-1）／`onlyBlock(Class)` ヘルパで素キャストを一本化（D-2）／reason 文字列を付与（D-3）／Javadoc から `src/main` 行番号を除去（D-4）／`g1` → `f1` へ改名（D-5）／`dir()` に一本化（D-6）／型記法を `半角英字` に統一（D-7）／`assertDirectiveCount` に `SystemRepository` 依存を明記（D-8）／「既存 33 件は流用せず組み直している」を実態に合わせて訂正（D-9） |

却下された指摘（クラス名の改名）は対応していない。

#### 7-2. 実測で判明したこと（アサーションより先に観測した）

| 観測対象 | 実測結果 |
|---|---|
| 送信同期の `RecordLayout.recordType` | `"no"`（名前行の先頭セル＝メタ列ヘッダ）。往復で安定し load-bearing のため**課題としない**と判断（`issues.md`「課題としないと判断した観測結果（#20）」へ記録） |
| マーカー列だけのテーブル／LIST_MAP | `columnNames=[]`／`rows=[[]]`（データ行 2 件なら `[[], []]`）。書き戻すと行が消える（往復非安定）ため `issues.md` **XLS-08** に記録 |
| 名前行が 1 列だけの `SETUP_FIXED`／`MESSAGE` | `IllegalStateException: can't get data` ← `IllegalStateException: directive or data names row must have two columns at least. [data]` → C-17 到達不能 |
| 型行が名前行より短い／型セルが末尾で空 | `IllegalArgumentException: field name size is 2. but types size is 1.`（`assertSameSizeAsNames`） |
| 型セルが中間位置で空 | `IllegalArgumentException: can't convert value []. convert table ={...}`（`BasicDataTypeMapping`） |
| 長さ省略記法 `-` の実 `.xlsx` 経路 | 原文 `"-"` のまま（器の実バイト長 `4` ではない） |
| MESSAGE の directives | `{file-type=Fixed, text-encoding=UTF-8}`（2 件） |
| XLS-06 の変換後 YAML | `record_type: ""` の後に `fields`／`rows` が続く（掲載スニペットは切り詰められていた） |
| `stripQuotes` の `null` ガード | `PoiXlsReader#readOneLine` L123 が `""` を返すため実 Excel 経路で `null` は生じない。テーブル／LIST_MAP 経路の `null` も呼び出し側で先に判定済み。JaCoCo でも L533 は未到達 → コメントの根拠づけが誤り |

#### 7-3. ミューテーション試験（自分で実施）

`src/main` を一時的に壊してテストが落ちることを確認し、確認後に `git checkout -- src/main` で戻した。

| # | 加えた変更（`XlsFormatReader`） | 結果 |
|---|---|---|
| 1 | `readSendSyncBlocks` の directives を空 Map に | **FAIL**（`readsAllFourSendSyncMessageTypesFromRealBook:700` 「送信同期 EXPECTED_REQUEST_HEADER_MESSAGES の file-type」）。修正前は 14 件全 PASS だった＝空証明が解消 |
| 2 | `readSendSyncBlocks` の identifier を定数 `"RM01"` に | **FAIL**（`readsAllFourSendSyncMessageTypesFromRealBook:707 -> assertBlock:898` 「識別子」） |
| 3 | `readListMapBlock` の identifier を定数 `"CONST"` に | **FAIL 2 件**（`readsFourBlockImplementationsFromOneRealSheet:778` 「ListMapBlock の識別子」／`readsListMapBlockFromRealBook:328`） |

戻したあとの確認: `git status --porcelain`／`git diff -- src/main` とも `src/main` の出力なし。

## Expert Reviews

**ラウンド 1（成果物 `5509b09` に対して実施・2026-08-12）**。3 レビューとも独立サブエージェントとして起動し、
self-check ファイル・実装エキスパートのサマリ・期待する判定は渡していない（中立フレーミング）。
指摘は修正ラウンド（`0761b12`）で対応済み。**再レビューは未実施**（次セッションの最初の作業）。

### QA

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| 検証アプローチが目的に対して意味を持っているか（「通った」で終わっていないか） | **NG（ラウンド 1）** | Completion criteria 6 項目は字面上すべて充足し、実 `.xlsx` が本番配線（`XlsFormatReader` L80-82 → `PoiXlsReader`）を通っていること・Fake 経路と別物であることを独立に確認。ただし「到達不能／#21 送り」の根拠に実害 2 件。**F1**: C-08 `columnNames` 空を「軸E 0 件と重なるため #21」としたが、軸E に該当要素は存在せず #21 のスコープにも入っておらず（計画から脱落）、実 `.xlsx` で 3 行で到達可能（`rows=[[]]` という未記録の挙動付き）。**F3**: `issues.md` XLS-07 が担保テストとして挙げた `readsAllFourSendSyncMessageTypesFromRealBook` は `getDirectives()` を呼んでおらず、C-13 到達不能の宣言が空証明。他に F2（C-17 空は本来到達不能だが未調査）／F4（C-20 の根拠機構の取り違え）／F5（XLS-06 の実測 YAML 切り詰め）／F6・F7（混在シート・送信同期のアサーションが弱い）／F8（長さ `-` の原文復元が実経路で未担保）／F9（directives の size アサートが `SystemRepository` のグローバル状態に依存）。判定: **FAIL** |
| 再現性（実行順序・一時ファイル・ロケール/TZ/OS 依存） | OK | `TemporaryFolder` でメソッドごとに一意ディレクトリ。`PoiXlsReader.bookCache`／`TestDataParsingTemplate.TEST_DATA_CACHE`／`DataFileParser.cache` のいずれともキーが衝突しない。`TestCoreReaderAdapter` が全経路で `parse(..., false)` を渡すため変換器経路は静的キャッシュへ書き込まない。値はすべて文字列セルとシンボルでロケール・TZ・OS 非依存 |

### Craft (coding)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| 言語・フレームワークのベストプラクティス | **NG（ラウンド 1）** | `assertTrue(kinds.contains(...))` が既存 `XlsFormatReaderTest#readAssemblesMixedBlockTypesInOneSection` の `hasItem` から後退（失敗時にどの種別が欠けたか出ない）。`instanceOf` ガードと素キャストが混在（ガードあり 5 箇所／素キャスト 6 箇所）。全アサーションに reason 文字列なし（兄弟クラス `XlsFormatReaderCellTypeTest` は付けている）。`dir()` と `folder.getRoot().toString()` の二重導出。判定: **FAIL**（重い指摘は QA F3・F6/F7 と同一） |
| 重複（フィクスチャ／ヘルパ寄せすぎ） | OK | Given をインラインで組み立てており「何を入力にしたか」がメソッド内で読める。ヘルパは 5 個のみで寄せすぎていない |
| 既存スタイルとの一貫性 | 一部 NG（ラウンド 1） | Given/When/Then・`@author kiyobot`・セクション区切り・命名は兄弟クラスと揃う。アサーションスタイルのみ後退。クラス Javadoc の「扱わない軸要素と理由」一覧が不完全（C-11／C-13／C-16／C-20 が漏れ）。Javadoc から `src/main` の行番号を参照しており黙って腐る。`text("g1")` がフィールド名なのにグループ ID と紛らわしい |
| Javadoc の正確さ | OK | 参照先を全件実地照合（`TestCoreReaderAdapter` L362／`XlsFormatReader#read` L133／`DataFile` L92／`YamlFormatReaderTest#readFile_recordTypeOmitted_keepsNullRecordType`）。壊れた `{@link}` なし |

### Verification (test)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| 成果物が実際に検証されたか（テストが実行されたか） | OK | `mvn clean test -Djacoco.skip=true` → `Tests run: 351, Failures: 0, Errors: 0` を自ら実行。件数の主張も親コミット `ef80d30`（337 件）を worktree に展開して差分 14 件を実測確認 |
| カバレッジ（エッジケース・主張の裏取り） | 一部 NG（ラウンド 1） | ミューテーション 13 本中 11 本を本クラス単独で検出（`bookName`/`sheetName` 入れ替え・`fileType` 反転・`recordType`/`length`/`type` 定数化・LIST_MAP 列名ソート・`EXPECTED_COMPLETED` 除去・`fwHeaderFields` 空・`record-separator` 逆正規化無効・`groupId` 強制 `""`・`identifier` 定数化）。**SURVIVED: 送信同期の directives 空 Map 化**（＝ QA F3 と同じ空証明を独立に実証。全 351 件で見ても既存 Fake 経路 1 件しか落ちない）。`issues.md` の記述は自らプローブして実測照合し、XLS-07 の値・C-11/C-13/C-16/C-20 の到達不能根拠はいずれも**真**と確認。引用行番号 11 件中 10 件が正確、1 件（`toRecordLayouts` L305 → L306）が誤り。判定: **PASS**（マージ前に directives のアサート追加を推奨） |
| 将来の脆さ | OK | 日付・数値・数式セルを使わないためロケール／TZ 非依存。POI API は `XlsFixture` に封じ込め |

### 三者の指摘に対するコーディネータの triage（2026-08-12）

| 分類 | 件数 | 内訳 |
|---|---|---|
| Valid（修正指示） | 21 | テストの穴 7（A-1〜A-7）／台帳の正確性 5（B-1〜B-5）／棚卸し整合 1（C-1）／作法 8（D-1〜D-9 のうち採用分） |
| Invalid（却下） | 1 | テストクラス名の改名提案。「Fake 経路との対比で実ファイル経路を通す」ことが本タスクの主眼であり、名前はそれを表している。scope 外の taste |
| Escalation | 0 | 「軸A 14 種すべて」に対する 13/14（`DEFAULT` 到達不能）は steering #20 の Steps 自体が明記しており合意済み。C-20 到達不能も事実の記録で足りるため、ユーザー判断を要しない |

レビュー間で判定が割れた 1 点（XLS-06 の実測 YAML が切り詰められているか否か）は、修正ラウンドで実測し直して
**「切り詰められており実測と一致しない」が正しい**と決着した（`record_type: ""` の後に `fields` / `rows` が続く）。

## Overall Verdict

- Self-check: OK（修正ラウンド反映済み）
- QA: **NG（ラウンド 1）** — 再レビュー未実施
- Craft expert (coding): **NG（ラウンド 1）** — 再レビュー未実施
- Verification expert (test): **OK（ラウンド 1、条件付き）** — 修正が入ったため再レビュー未実施
- Ready to check off: **No** — 修正ラウンド（`0761b12`）に対する 3 レビューの再実行が未了。再実行で OK が揃った時点でチェックオフする
