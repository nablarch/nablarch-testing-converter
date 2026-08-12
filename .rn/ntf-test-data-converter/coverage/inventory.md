# 既存テスト 4辺分の軸棚卸し（task #18）

4つの変換辺の既存テスト 126 件が、軸A〜F のどの要素を担保しているかを 1 件ずつ棚卸しした結果。
以降のタスク #19〜#25 は、本書「未担保一覧」に挙がった要素だけを埋める。

- 作成日: 2026-08-12
- 対象コミット: `c1d2d21`（棚卸し実施時 HEAD）
- 判定方法: 全テストメソッドのテスト本文を読み、実際にアサートしている対象のみを「担保」とした。
  推測で埋めていない。アサートが間接的・副次的なものは「弱」印で区別した。

## 凡例

| 印 | 意味 |
|---|---|
| ✅ | 担保あり（その軸要素を直接アサートしている） |
| 🔺 | 弱い担保（間接的にしか通っていない／副次的にしか現れない。詳細は各表の備考欄） |
| ❌ | 未担保 |

---

## 0. 前提の実測

### 0.1 テストメソッド件数（実測 vs steering 想定）

```
$ for f in XlsFormatReaderTest YamlFormatReaderTest XlsFormatWriterTest YamlFormatWriterTest; ... grep -c '@Test'
XlsFormatReaderTest.java  : 33 @Test  (1179 lines)
YamlFormatReaderTest.java : 20 @Test  ( 608 lines)
XlsFormatWriterTest.java  : 40 @Test  (1080 lines)
YamlFormatWriterTest.java : 33 @Test  ( 724 lines)
```

| 辺 | クラス | steering 想定 | 実測 | 差異 |
|---|---|---|---|---|
| 辺① Excel→中間モデル | `XlsFormatReaderTest` | 33 | **33** | なし |
| 辺② YAML→中間モデル | `YamlFormatReaderTest` | 20 | **20** | なし |
| 辺③ 中間モデル→Excel | `XlsFormatWriterTest` | 40 | **40** | なし |
| 辺④ 中間モデル→YAML | `YamlFormatWriterTest` | 33 | **33** | なし |
| 合計 | | 126 | **126** | なし |

### 0.2 軸A: `DataType` 実定義との突き合わせ

実定義: `/home/tie303177/work/nablarch/nablarch-testing/src/main/java/nablarch/test/core/reader/DataType.java`
（converter リポジトリには存在せず、依存の `nablarch-testing` 本体で定義されている）

| # | 定数名 | `getType()` | `getName()`（記法名） | 定義行 |
|---|---|---|---|---|
| A-01 | `DEFAULT` | 0 | `DEFAULT` | L11 |
| A-02 | `SETUP_TABLE_DATA` | 1 | `SETUP_TABLE` | L14 |
| A-03 | `EXPECTED_TABLE_DATA` | 2 | `EXPECTED_TABLE` | L17 |
| A-04 | `EXPECTED_COMPLETED` | 4 | `EXPECTED_COMPLETE_TABLE` | L23 |
| A-05 | `LIST_MAP` | 3 | `LIST_MAP` | L29 |
| A-06 | `SETUP_FIXED` | 5 | `SETUP_FIXED` | L32 |
| A-07 | `EXPECTED_FIXED` | 6 | `EXPECTED_FIXED` | L35 |
| A-08 | `SETUP_VARIABLE` | 7 | `SETUP_VARIABLE` | L38 |
| A-09 | `EXPECTED_VARIABLE` | 8 | `EXPECTED_VARIABLE` | L41 |
| A-10 | `MESSAGE` | 9 | `MESSAGE` | L44 |
| A-11 | `EXPECTED_REQUEST_HEADER_MESSAGES` | 10 | 同名 | L47 |
| A-12 | `EXPECTED_REQUEST_BODY_MESSAGES` | 11 | 同名 | L50 |
| A-13 | `RESPONSE_HEADER_MESSAGES` | 12 | 同名 | L53 |
| A-14 | `RESPONSE_BODY_MESSAGES` | 13 | 同名 | L56 |

**突き合わせ結果: 14 種（`DEFAULT` ＋ 13）で steering の記述と一致。差異なし。**

### 0.3 軸B: `TestDataBlock` sealed 階層

`TestDataBlock`（abstract sealed, L16-17）が permit するのは `ColumnRowDataBlock` / `FileDataBlock` / `MessageDataBlock`。
`ColumnRowDataBlock`（abstract sealed, L19-20）がさらに `TableDataBlock` / `ListMapBlock` を permit する。
したがって **具象（final）実装は 4 種**であり、steering の「4種」と一致する。

| # | 具象クラス | 直接の親 | 保持する `DataType` |
|---|---|---|---|
| B-1 | `TableDataBlock` | `ColumnRowDataBlock` | SETUP_TABLE_DATA / EXPECTED_TABLE_DATA / EXPECTED_COMPLETED |
| B-2 | `ListMapBlock` | `ColumnRowDataBlock` | LIST_MAP（コンストラクタで固定） |
| B-3 | `FileDataBlock` | `TestDataBlock` | SETUP_FIXED / EXPECTED_FIXED / SETUP_VARIABLE / EXPECTED_VARIABLE |
| B-4 | `MessageDataBlock` | `TestDataBlock` | MESSAGE / 送信系 4 種 |

### 0.4 軸C: 中間モデル全フィールド（実クラスから読み取り）

全 **21 フィールド**。`src/main/java/nablarch/test/tool/converter/model/` の各クラスの `private final` 宣言をすべて列挙した。

| # | クラス | フィールド | 型 | 省略区分 | 省略時の表現（Javadoc 根拠） |
|---|---|---|---|---|---|
| C-01 | `TestDataContainer` | `name` | String | 必須 | — |
| C-02 | `TestDataContainer` | `sections` | List | 空許容 | 空リスト |
| C-03 | `TestDataSection` | `name` | String | 必須 | — |
| C-04 | `TestDataSection` | `blocks` | List | 空許容 | 空リスト |
| C-05 | `TestDataBlock` | `dataType` | DataType | 必須 | — |
| C-06 | `TestDataBlock` | `groupId` | String | **省略可** | `""`（TestDataBlock L28/L41「省略時は空文字」） |
| C-07 | `TestDataBlock` | `identifier` | String | 必須 | — |
| C-08 | `ColumnRowDataBlock` | `columnNames` | List | 空許容 | 空リスト |
| C-09 | `ColumnRowDataBlock` | `rows` | List<List> | 空許容 | 空リスト |
| C-10 | `FileDataBlock` | `fileType` | FileType | 必須（2値） | FIXED / VARIABLE（FileDataBlock L25） |
| C-11 | `FileDataBlock` | `directives` | Map | 空許容 | 空 Map |
| C-12 | `FileDataBlock` | `records` | List | 空許容 | 空リスト |
| C-13 | `MessageDataBlock` | `directives` | Map | 空許容 | 空 Map |
| C-14 | `MessageDataBlock` | `fwHeaderFields` | Map | 空許容 | 空 Map（MessageDataBlock L40「FW ヘッダを読まない経路では空 Map」） |
| C-15 | `MessageDataBlock` | `records` | List | 空許容 | 空リスト |
| C-16 | `RecordLayout` | `recordType` | String | **省略可** | `null`（RecordLayout L26/L36「省略時は null」） |
| C-17 | `RecordLayout` | `fields` | List | 空許容 | 空リスト |
| C-18 | `RecordLayout` | `rows` | List<List> | 空許容 | 空リスト |
| C-19 | `FieldDef` | `name` | String | 必須 | — |
| C-20 | `FieldDef` | `type` | String | **省略可** | `null`（FieldDef L25/L38「省略時は null」） |
| C-21 | `FieldDef` | `length` | String | **省略可** | `null`（FieldDef L25/L43「省略時は null」） |

内訳: 必須スカラー 6 件（C-01, C-03, C-05, C-07, C-10, C-19）／
**省略可能フィールド 4 件**（C-06, C-16, C-20, C-21 — Javadoc に「省略時は…」と明記）／
空許容コレクション 11 件（C-02, C-04, C-08, C-09, C-11, C-12, C-13, C-14, C-15, C-17, C-18）。

**steering との差異（コーディネータ判断を仰ぐ点）**: steering #20 の Steps は
「`groupId` / `identifier` / `fileType` / `directives` / `fwHeaderFields` / `recordType` / `FieldDef.type` / `FieldDef.length` は
『値あり』『省略』の双方を通す」としているが、実定義上 `identifier`（C-07）と `fileType`（C-10）には
「省略」の表現が存在しない（`identifier` は必須スカラー、`fileType` は FIXED/VARIABLE の 2 値）。
本棚卸しでは実定義を正とし、`fileType` は「FIXED / VARIABLE 双方」、`identifier` は「値あり 1 通り」として扱う。
`directives` / `fwHeaderFields` は「非空 / 空 Map」の双方として扱う。

### 0.5 軸D の要素（辺ごとに定義が異なる。steering #19/#22/#24/#25 の記述を要素化）

- **辺① セル種別 17 ケース**: D1-01 文字列／D1-02 整数数値／D1-03 小数数値／D1-04 大きい数値／
  D1-05 先頭ゼロ文字列／D1-06 日付書式／D1-07 時刻書式／D1-08 日時書式／D1-09 数式／D1-10 真偽値／
  D1-11 エラー値／D1-12 セル不在／D1-13 空文字／D1-14 前後空白／D1-15 改行／D1-16 リテラル `null`／D1-17 表示形式付き数値
- **辺② YAML スカラー 10 ケース**: D2-01 引用符なし文字列／D2-02 引用符あり／D2-03 数値／D2-04 末尾ゼロ小数／
  D2-05 `true`・`TRUE`・`yes`／D2-06 `null`・`~`・値なし／D2-07 `"null"`／D2-08 日付風／D2-09 複数行 `|`・`>`／D2-10 先頭ゼロ
- **辺③ セル型 8 ケース（`getCellType()` をアサート）**: D3-01 `"100"`／D3-02 `"=1+1"`／D3-03 `"007"`／
  D3-04 `null`／D3-05 `""`／D3-06 改行含む文字列／D3-07 32767 文字超／D3-08 制御文字含む
- **辺④ YAML 表現 9 ケース**: D4-01 `"100"`／D4-02 `"true"`／D4-03 `"null"`／D4-04 `null`／D4-05 `""`／
  D4-06 `"007"`／D4-07 改行含む／D4-08 `"2026-08-07"`／D4-09 コロン・ハイフン・`#` 含む

### 0.6 軸E の要素（4 観点 × 0/1/複数。steering #21 より）

E-1 セクション内ブロック数（0／1／複数）／E-2 ブロック内行数（0／1／複数）／
E-3 ファイル内レコードレイアウト数（0／1／複数）／E-4 コンテナ内セクション数＝ブック内シート数（1／複数）

### 0.7 軸F の要素（辺ごと。steering #21/#22/#24/#25 より）

- **辺①（6）**: F1-01 シート不在／F1-02 ブック破損／F1-03 未知のデータタイプ名／
  F1-04 マーカーカラム欠落／F1-05 カラム名重複／F1-06 行と列の数の不一致
- **辺②（5）**: F2-01 スキーマ違反／F2-02 YAML として不正／F2-03 未知のキー／F2-04 必須構造の欠落／F2-05 空ファイル
- **辺③（4）**: F3-01 出力先不在／F3-02 同名ファイル既存かつ `overwrite=false`／F3-03 書き込み権限なし／
  F3-04 シート名が Excel 制約違反（31 文字超・禁止文字）
- **辺④（3）**: F4-01 出力先不在／F4-02 `overwrite=false` 衝突／F4-03 書き込み権限なし

### 0.8 棚卸しで判明した横断的な事実

1. **辺①の既存 33 件は 1 件も実 `.xlsx` を通っていない。** `XlsFormatReaderTest` は内部クラス
   `FakeTestDataReader`（L54-102）に `List<List<String>>` の canned 行を与えて `TestCoreReaderAdapter` を駆動する。
   実セル → 文字列行の区間（`PoiXlsReader`）は一度も実行されない。したがって **軸D 辺①（17 ケース）は全て未担保**。
2. **ただし辺③の往復テスト 8 件（`roundTrips*`）は実 `.xlsx` を経由して `XlsFormatReader` を駆動している。**
   `XlsFormatWriterTest#roundTrip`（L861-865）は `new XlsFormatWriter().write(...)` で実ファイルを書き、
   `new XlsFormatReader()`（本番配線＝`PoiXlsReader`）で読み戻す。よって steering #19 の
   「実 `.xlsx` を入力として `XlsFormatReader` を駆動するテストが存在し、`FakeTestDataReader` を経由していない」は
   **既に部分的に満たされている**（文字列セル・空セル・リテラル `null` の 3 ケース相当が通る）。#19 はこれを起点にできる。
3. **辺②の既存 20 件は 1 件も実 YAML テキストを通っていない。** `YamlFormatReaderTest#reader`（L538-545）は
   `YamlTestCoreAdapter#loadRawMap` を in-memory `LinkedHashMap` に差し替える。YAML パーサ（SnakeYAML Engine）は
   通らない。したがって **軸D 辺②（10 ケース）は全て未担保**。ただし辺④の往復テスト 6 件（`roundTrip_*`）は
   `writer.write(...)` で実 YAML ファイルを書き `new YamlFormatReader()` で読み戻すため、辺②を実ファイル経由で駆動している。
4. **`getCellType()` をアサートしているテストは src/test 全体でゼロ**（`grep -rn "getCellType" src/test/` → 0 件）。
   `XlsFormatWriterTest` のセル読み出しヘルパ `cell`（L100-107）／`line`（L110-121）は `getStringCellValue()` のみを使う。
   したがって **軸D 辺③（8 ケース）は `getCellType()` 観点では全て未担保**。
5. `overwrite` フラグは `ConversionRequest` / `TestDataConverter` / `ConverterMojo` が持ち、
   `XlsFormatWriter` / `YamlFormatWriter` は持たない（`grep -rln "overwrite" src/main/java`）。
   軸F の F3-02 / F4-02 は writer 単体では再現できない可能性がある（#22/#25 で要判断）。

---

## 1. 辺① Excel→中間モデル（`XlsFormatReaderTest` 33 件）

### 1.1 テストメソッド → 担保する軸要素

| # | テストメソッド | 軸A | 軸B | 軸C | 軸D | 軸E | 軸F |
|---|---|---|---|---|---|---|---|
| 1 | `readMapsTableBlockPreservingRawValues` | A-02 | B-1 | C-07, C-08, C-09 | — 🔺文字列レベルで `${}`／`""`／null セル区別 | E-1(1), E-2(複数) | — |
| 2 | `readTableNormalizesExcelQuotationNotation` | A-02 | B-1 | C-09 | — 🔺Excel 引用符記法 `""`→`""`・`"abc"`→`abc` | E-2(1) | — |
| 3 | `readListMapNormalizesExcelQuotationNotation` | A-05 | B-2 | C-09 | — 🔺同上 | E-2(1) | — |
| 4 | `readFixedFileNormalizesExcelQuotationNotation` | A-06 | B-3 | C-12, C-18 | — 🔺同上 | E-3(1) | — |
| 5 | `readMapsMultipleTablesWithoutDuplication` | A-02 | B-1 | C-07 | — | E-1(複数=2) | — |
| 6 | `readPreservesGroupIdAndDataType` | A-03 | B-1 | C-05, C-06(値あり), C-09 | — | E-1(1) | — |
| 7 | `readListMapPreservesColumnOrder` | A-05 | B-2 | C-08(記述順), C-09 | — | E-2(複数=2) | — |
| 8 | `readListMapExcludesMarkerColumns` | A-05 | B-2 | C-08, C-09 | — 🔺マーカーカラム `[no]` 除外 | E-2(複数=2) | — |
| 9 | `readMapsListMapBlock` | A-05 | B-2 | C-07, C-08, C-09 | — 🔺`${}`／`""` | E-2(複数=2) | — |
| 10 | `readMapsFixedLengthFileBlock` | A-06 | B-3 | C-07, C-10(FIXED), C-12, C-16(値あり), C-17, C-18, C-19, C-20(値あり), C-21(値あり) | — | E-3(1) | — |
| 11 | `readRestoresOriginalRecordTypeTypeAndOmittedLengthFromRawLines` | A-06 | B-3 | C-16, C-18, C-20, C-21(`"-"`) | — 🔺長さ記法 `-` の原文保持 | E-3(1) | — |
| 12 | `readRestoresMultipleRecordLayoutsInFixedFile` | A-06 | B-3 | C-12, C-16, C-17, C-18, C-19, C-20, C-21 | — | E-3(複数=2), E-2(1/複数) | — |
| 13 | `readMapsVariableLengthFileBlock` | A-08 | B-3 | C-10(VARIABLE), C-18, C-19, C-21(省略=null) | — | E-3(1) | — |
| 14 | `readMapsMessageBlock` | A-10 | B-4 | C-05, C-07, C-14(値あり), C-15, C-18, C-19 | — | E-3(1) | — |
| 15 | `readMapsExpectedRequestHeaderMessageBlock` | A-11 | B-4 | C-05, C-06(値あり), C-07, C-13(値あり), C-14(空), C-15, C-17, C-18, C-19, C-20, C-21 | — | E-3(1) | — |
| 16 | `readMapsAllFourSendSyncMessageTypes` | A-11, A-12, A-13, A-14 | B-4 | C-05, C-06 | — | E-1(複数=4) | — |
| 17 | `readMapsMultipleSendSyncBlocksInSameGroup` | A-11 | B-4 | C-07 | — | E-1(複数=2) | — |
| 18 | `readNormalizesRecordSeparatorCrlfSymbol` | A-08 | B-3 | C-11(値あり) | — 🔺`record-separator` CRLF シンボル逆正規化 | — | — |
| 19 | `readNormalizesRecordSeparatorLfSymbol` | A-08 | B-3 | C-11 | — 🔺LF シンボル | — | — |
| 20 | `readNormalizesRecordSeparatorCrSymbol` | A-08 | B-3 | C-11 | — 🔺CR シンボル | — | — |
| 21 | `defaultConstructorWiresProductionAdapter` | — | — | — | — | — | — （本番配線の生成可能性のみ） |
| 22 | `readIgnoresDataTypePrefixedLineWithoutMarker` | — | — | C-04(空) | — | E-1(0) | 🔺F1-03 に近い（データタイプ名で始まるが `=` なしの行を無視） |
| 23 | `readPreservesErrorModeRowInSendSyncMessage` | A-14 | B-4 | C-16, C-17, C-18, C-19 | — 🔺`errorMode:timeout` の原文保持 | E-3(1) | — |
| 24 | `readDerivesContainerAndSectionNamesFromResource` | A-02 | B-1 | C-01, C-02(1件), C-03 | — | E-4(1) | — |
| 25 | `readListMapWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins` | A-05 | B-2 | C-08, C-09 | — | — | **F1-05** ✅（WARN・後勝ち） |
| 26 | `readListMapWithMultipleDuplicateColumnsEmitsWarnPerName` | A-05 | B-2 | C-08, C-09 | — | — | **F1-05** ✅（複数名の重複） |
| 27 | `readTableWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins` | A-02 | B-1 | C-08, C-09 | — | — | **F1-05** ✅（TABLE 系） |
| 28 | `readListMapWithoutDuplicatesEmitsNoWarn` | A-05 | B-2 | — | — | — | F1-05 の非回帰（WARN なし） |
| 29 | `readReturnsEmptySectionWhenNoBlocks` | — | — | C-02(1件), C-04(空) | — | **E-1(0)** ✅ | — |
| 30 | `readAssemblesMixedBlockTypesInOneSection` | A-02, A-05, A-06, A-10 | **B-1, B-2, B-3, B-4** | C-04 | — | E-1(複数=4) | — |
| 31 | `readNormalizesRecordSeparatorEmptyValueToNoneSymbol` | A-08 | B-3 | C-11 | — 🔺NONE シンボル逆正規化 | — | — |
| 32 | `readPassesThroughUnknownRecordSeparatorValue` | A-08 | B-3 | C-11 | — 🔺未知値のパススルー | — | — |
| 33 | `readStripsQuotesFromQuotedGenericDirectiveValue` | A-08 | B-3 | C-11 | — 🔺ディレクティブ値の引用符除去 | — | — |

### 1.2 軸要素 → 担保テストメソッド

**軸A（10/14 担保）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| A-01 `DEFAULT` | ❌ | — |
| A-02 `SETUP_TABLE_DATA` | ✅ | `readMapsTableBlockPreservingRawValues`, `readTableNormalizesExcelQuotationNotation`, `readMapsMultipleTablesWithoutDuplication`, `readTableWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins`, `readDerivesContainerAndSectionNamesFromResource`, `readAssemblesMixedBlockTypesInOneSection` |
| A-03 `EXPECTED_TABLE_DATA` | ✅ | `readPreservesGroupIdAndDataType` |
| A-04 `EXPECTED_COMPLETED` | ❌ | — |
| A-05 `LIST_MAP` | ✅ | `readListMapNormalizesExcelQuotationNotation`, `readListMapPreservesColumnOrder`, `readListMapExcludesMarkerColumns`, `readMapsListMapBlock`, `readListMapWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins`, `readListMapWithMultipleDuplicateColumnsEmitsWarnPerName`, `readListMapWithoutDuplicatesEmitsNoWarn`, `readAssemblesMixedBlockTypesInOneSection` |
| A-06 `SETUP_FIXED` | ✅ | `readFixedFileNormalizesExcelQuotationNotation`, `readMapsFixedLengthFileBlock`, `readRestoresOriginalRecordTypeTypeAndOmittedLengthFromRawLines`, `readRestoresMultipleRecordLayoutsInFixedFile`, `readAssemblesMixedBlockTypesInOneSection` |
| A-07 `EXPECTED_FIXED` | ❌ | — |
| A-08 `SETUP_VARIABLE` | ✅ | `readMapsVariableLengthFileBlock`, `readNormalizesRecordSeparatorCrlfSymbol`, `readNormalizesRecordSeparatorLfSymbol`, `readNormalizesRecordSeparatorCrSymbol`, `readNormalizesRecordSeparatorEmptyValueToNoneSymbol`, `readPassesThroughUnknownRecordSeparatorValue`, `readStripsQuotesFromQuotedGenericDirectiveValue` |
| A-09 `EXPECTED_VARIABLE` | ❌ | — |
| A-10 `MESSAGE` | ✅ | `readMapsMessageBlock`, `readAssemblesMixedBlockTypesInOneSection` |
| A-11 `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `readMapsExpectedRequestHeaderMessageBlock`, `readMapsAllFourSendSyncMessageTypes`, `readMapsMultipleSendSyncBlocksInSameGroup` |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `readMapsAllFourSendSyncMessageTypes` |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅ | `readMapsAllFourSendSyncMessageTypes` |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅ | `readMapsAllFourSendSyncMessageTypes`, `readPreservesErrorModeRowInSendSyncMessage` |

**軸B（4/4 担保）**

| 要素 | 判定 | 担保テストメソッド（代表） |
|---|---|---|
| B-1 `TableDataBlock` | ✅ | `readMapsTableBlockPreservingRawValues`, `readAssemblesMixedBlockTypesInOneSection` ほか |
| B-2 `ListMapBlock` | ✅ | `readMapsListMapBlock`, `readAssemblesMixedBlockTypesInOneSection` ほか |
| B-3 `FileDataBlock` | ✅ | `readMapsFixedLengthFileBlock`, `readMapsVariableLengthFileBlock`, `readAssemblesMixedBlockTypesInOneSection` ほか |
| B-4 `MessageDataBlock` | ✅ | `readMapsMessageBlock`, `readMapsExpectedRequestHeaderMessageBlock`, `readAssemblesMixedBlockTypesInOneSection` ほか |

**軸C（21 フィールド／省略可能は「値あり」「省略」の双方を評価）**

| 要素 | 値あり／非空 | 省略／空 | 担保テストメソッド |
|---|---|---|---|
| C-01 `TestDataContainer.name` | ✅ | n/a | `readDerivesContainerAndSectionNamesFromResource` |
| C-02 `TestDataContainer.sections` | ✅(1件) | ❌ 空 | `readDerivesContainerAndSectionNamesFromResource`, `readReturnsEmptySectionWhenNoBlocks`（※辺①は 1 リソース＝1 セクション固定。空・複数は構造上発生しない可能性あり — 要判断） |
| C-03 `TestDataSection.name` | ✅ | n/a | `readDerivesContainerAndSectionNamesFromResource` |
| C-04 `TestDataSection.blocks` | ✅ | ✅ 空 | 非空: 多数／空: `readReturnsEmptySectionWhenNoBlocks`, `readIgnoresDataTypePrefixedLineWithoutMarker` |
| C-05 `TestDataBlock.dataType` | ✅ | n/a | `readPreservesGroupIdAndDataType`, `readMapsMessageBlock`, `readMapsExpectedRequestHeaderMessageBlock`, `readMapsAllFourSendSyncMessageTypes` |
| C-06 `TestDataBlock.groupId` | ✅ | ❌ 省略(`""`) | 値あり: `readPreservesGroupIdAndDataType`, `readMapsExpectedRequestHeaderMessageBlock`, `readMapsAllFourSendSyncMessageTypes`／`""` をアサートするテストは 0 件 |
| C-07 `TestDataBlock.identifier` | ✅ | n/a | `readMapsTableBlockPreservingRawValues`, `readMapsListMapBlock`, `readMapsFixedLengthFileBlock`, `readMapsMessageBlock` ほか |
| C-08 `ColumnRowDataBlock.columnNames` | ✅ | ❌ 空 | 非空: `readMapsTableBlockPreservingRawValues`, `readListMapPreservesColumnOrder`, `readListMapExcludesMarkerColumns` ほか |
| C-09 `ColumnRowDataBlock.rows` | ✅ | ❌ 空 | 非空: 多数 |
| C-10 `FileDataBlock.fileType` | ✅ FIXED / ✅ VARIABLE | n/a | FIXED: `readMapsFixedLengthFileBlock`／VARIABLE: `readMapsVariableLengthFileBlock` |
| C-11 `FileDataBlock.directives` | ✅ | ❌ 空 | 非空: `readNormalizesRecordSeparator*`（4件）, `readPassesThroughUnknownRecordSeparatorValue`, `readStripsQuotesFromQuotedGenericDirectiveValue` |
| C-12 `FileDataBlock.records` | ✅ | ❌ 空 | 非空: `readMapsFixedLengthFileBlock`, `readRestoresMultipleRecordLayoutsInFixedFile` |
| C-13 `MessageDataBlock.directives` | ✅ | ❌ 空 | 非空: `readMapsExpectedRequestHeaderMessageBlock` のみ |
| C-14 `MessageDataBlock.fwHeaderFields` | ✅ | ✅ 空 | 非空: `readMapsMessageBlock`／空: `readMapsExpectedRequestHeaderMessageBlock` |
| C-15 `MessageDataBlock.records` | ✅ | ❌ 空 | 非空: `readMapsMessageBlock`, `readMapsExpectedRequestHeaderMessageBlock` |
| C-16 `RecordLayout.recordType` | ✅ | ❌ 省略(null) | 値あり: `readMapsFixedLengthFileBlock`, `readRestoresOriginalRecordTypeTypeAndOmittedLengthFromRawLines`, `readRestoresMultipleRecordLayoutsInFixedFile`, `readPreservesErrorModeRowInSendSyncMessage` |
| C-17 `RecordLayout.fields` | ✅ | ❌ 空 | 非空: `readMapsFixedLengthFileBlock` ほか |
| C-18 `RecordLayout.rows` | ✅ | ❌ 空 | 非空: 多数 |
| C-19 `FieldDef.name` | ✅ | n/a | `readMapsFixedLengthFileBlock`, `readMapsExpectedRequestHeaderMessageBlock` ほか |
| C-20 `FieldDef.type` | ✅ | ❌ 省略(null) | 値あり: `readMapsFixedLengthFileBlock`, `readRestoresOriginalRecordTypeTypeAndOmittedLengthFromRawLines` |
| C-21 `FieldDef.length` | ✅ | ✅ 省略(null) | 値あり: `readMapsFixedLengthFileBlock`（`"10"`/`"5"`）, `readRestoresOriginalRecordTypeTypeAndOmittedLengthFromRawLines`（`"-"`）／省略: `readMapsVariableLengthFileBlock` |

**軸D（0/17 担保）**

D1-01〜D1-17 すべて ❌。理由: 既存 33 件は `FakeTestDataReader`（`XlsFormatReaderTest` L54-102）に文字列行を直接与えるため、
実セル → 文字列行の変換区間を一切通らない。セル種別という概念がテスト入力に存在しない。

🔺 弱い担保（辺③の往復テスト経由・実 `.xlsx` を通る）:
- D1-01 文字列: `XlsFormatWriterTest#roundTripsTable`, `#roundTripsListMap`, `#roundTripsFixedFile`, `#roundTripsMessage` ほか
- D1-13 空文字: `XlsFormatWriterTest#roundTripsTable`, `#roundTripsNullCellAsLiteralNullString`
- D1-16 リテラル `null`: `XlsFormatWriterTest#roundTripsNullCellAsLiteralNullString`

**軸E**

| 要素 | 0 件 | 1 件 | 複数件 | 担保テストメソッド |
|---|---|---|---|---|
| E-1 セクション内ブロック数 | ✅ | ✅ | ✅ | 0: `readReturnsEmptySectionWhenNoBlocks`, `readIgnoresDataTypePrefixedLineWithoutMarker`／1: 多数／複数: `readMapsMultipleTablesWithoutDuplication`, `readMapsAllFourSendSyncMessageTypes`, `readMapsMultipleSendSyncBlocksInSameGroup`, `readAssemblesMixedBlockTypesInOneSection` |
| E-2 ブロック内行数 | ❌ | ✅ | ✅ | 1: `readTableNormalizesExcelQuotationNotation` ほか／複数: `readMapsTableBlockPreservingRawValues`, `readListMapPreservesColumnOrder`, `readListMapExcludesMarkerColumns`, `readMapsListMapBlock` |
| E-3 ファイル内レコードレイアウト数 | ❌ | ✅ | ✅ | 1: `readMapsFixedLengthFileBlock`／複数: `readRestoresMultipleRecordLayoutsInFixedFile` |
| E-4 ブック内シート数 | n/a | ✅ | ❌ | 1: `readDerivesContainerAndSectionNamesFromResource`（※`XlsFormatReader#read` は「ブック名/シート名」1 リソース単位。複数シートは構造上到達不能の可能性 — 要判断） |

**軸F（1/6 担保）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| F1-01 シート不在 | ❌ | — |
| F1-02 ブック破損 | ❌ | — |
| F1-03 未知のデータタイプ名 | 🔺 | `readIgnoresDataTypePrefixedLineWithoutMarker`（データタイプ名で始まるが `=` を持たない行の無視。「未知の名前」そのものではない） |
| F1-04 マーカーカラム欠落 | ❌ | — |
| F1-05 カラム名重複 | ✅ | `readListMapWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins`, `readListMapWithMultipleDuplicateColumnsEmitsWarnPerName`, `readTableWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins`（非回帰: `readListMapWithoutDuplicatesEmitsNoWarn`） |
| F1-06 行と列の数の不一致 | ❌ | — |

### 1.3 辺① 未担保一覧（#19〜#21 が埋める対象）

| 軸 | 未担保要素 | 件数 |
|---|---|---|
| A | A-01 `DEFAULT`, A-04 `EXPECTED_COMPLETED`, A-07 `EXPECTED_FIXED`, A-09 `EXPECTED_VARIABLE` | 4 |
| B | （なし） | 0 |
| C | C-02 sections 空／C-06 groupId 省略(`""`)／C-08 columnNames 空／C-09 rows 空／C-11 FileDataBlock.directives 空／C-12 FileDataBlock.records 空／C-13 MessageDataBlock.directives 空／C-15 MessageDataBlock.records 空／C-16 recordType 省略／C-17 fields 空／C-18 RecordLayout.rows 空／C-20 FieldDef.type 省略 | 12 |
| D | D1-01〜D1-17 全 17 ケース（うち D1-01/D1-13/D1-16 は辺③往復経由の弱い担保あり） | 17 |
| E | E-2(0件)／E-3(0件)／E-4(複数シート) | 3 |
| F | F1-01 シート不在／F1-02 ブック破損／F1-03 未知データタイプ名（弱のみ）／F1-04 マーカーカラム欠落／F1-06 行列数不一致 | 5 |
| **合計** | | **41** |

**特に大きな空欄**: 軸D 17 ケース全滅（実 `.xlsx` を一度も通らないため）。#19 の主眼。

---

## 2. 辺② YAML→中間モデル（`YamlFormatReaderTest` 20 件）

### 2.1 テストメソッド → 担保する軸要素

| # | テストメソッド | 軸A | 軸B | 軸C | 軸D | 軸E | 軸F |
|---|---|---|---|---|---|---|---|
| 1 | `readTable_setup_mapsUppercaseNameAndColumnsWithRawValues` | A-02 | B-1 | C-05, C-06(省略=`""`), C-07, C-08, C-09 | — 🔺Map 値レベルで `${}`／null／`""` | E-1(1), E-2(複数=2) | — |
| 2 | `readTable_expectedWithGroup_formatsGroupIdAndCreatesBlockPerGroup` | A-03 | B-1 | C-05, C-06(値あり), C-09 | — | E-1(複数=2) | — |
| 3 | `readTable_completed_mapsExpectedCompletedType` | A-04 | B-1 | C-05, C-07 | — | E-1(1) | — |
| 4 | `readListMap_preservesYamlColumnOrderExcludesMarkersAndKeepsNull` | A-05 | B-2 | C-05, C-07, C-08(YAML 順), C-09 | — 🔺マーカーカラム `[ignore]` 除外・null 保持 | E-2(複数=2) | — |
| 5 | `readFile_fixed_mapsRawFieldDefsAndValues` | A-06 | B-3 | C-05, C-07, C-10(FIXED), C-11(値あり), C-12, C-16(値あり), C-17, C-18, C-19, C-20, C-21(値あり＋省略) | — 🔺`${}`／`""` | E-3(複数=2), E-2(複数=2) | — |
| 6 | `readFile_variable_mapsVariableTypeWithNullLengths` | A-09 | B-3 | C-05, C-10(VARIABLE), C-17, C-18, C-19, C-20, C-21(省略) | — | E-3(1) | — |
| 7 | `readFile_expectedFixedWithMultipleGroups_mapsExpectedFixedAndDedupesGroups` | A-07 | B-3 | C-05, C-06(値あり), C-07 | — | E-1(複数=3) | — |
| 8 | `readFile_setupVariable_mapsSetupVariableType` | A-08 | B-3 | C-05, C-10(VARIABLE) | — | E-1(1) | — |
| 9 | `readFile_recordTypeOmitted_keepsNullRecordType` | A-06 | B-3 | **C-16(省略=null)** ✅ | — | E-3(1) | — |
| 10 | `readFile_recordTypeDefault_normalizedToNull` | A-08 | B-3 | C-16(`"Default"`→null) | — 🔺特殊値の正規化 | E-3(1) | — |
| 11 | `readMessage_mapsRawFwHeaderAndExcludesFwHeaderRecord` | A-10 | B-4 | C-05, C-06(省略), C-07, C-14(値あり), C-15, C-16, C-17, C-18, C-19, C-20, C-21 | — 🔺`${}` | E-3(1) | — |
| 12 | `readMessage_emptyBody_isStillMapped` | A-10 | B-4 | C-07, **C-14(空)**, **C-15(空)** | — | **E-3(0)** ✅ | — |
| 13 | `readMessage_nullContent_isSkipped` | — | — | C-04(空) | — | E-1(0) | 🔺器が null を返す場合のスキップ |
| 14 | `readSendSync_groupsByRawValueFormatsGroupIdAndKeepsNoField` | A-11 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-17, C-18, C-19, C-20, C-21 | — 🔺`${}`・`no` フィールド保持 | E-1(複数=3) | — |
| 15 | `readSendSync_allFourTypesAreRecognized` | A-11, A-12, A-13, A-14 | B-4 | C-05 | — | E-1(複数=4) | — |
| 16 | `readSendSync_entryWithoutGroupId_isDropped` | A-14 | B-4 | C-07 | — | E-1(1) | 🔺**F2-04** に近い（送信系必須の `group_id` 欠落エントリを drop） |
| 17 | `read_mixedSections_keepsDescriptionOrderAndIgnoresUnknownKeys` | A-02, A-10 | B-1, B-4 | C-04 | — | E-1(複数=2) | **F2-03** ✅（未知キー無視） |
| 18 | `read_namesContainerAndSectionByResourceName` | — | — | C-01, C-02(1件), C-03, **C-04(空)** | — | E-1(0), E-4(1) | 🔺**F2-05** に近い（空 Map。実ファイルではない） |
| 19 | `read_containerCountMismatch_failsFast` | A-06 | B-3 | — | — | — | ✅ 器↔原文の件数不整合 → `IllegalStateException` |
| 20 | `read_fragmentRecordMismatch_failsFast` | A-06 | B-3 | — | — | — | ✅ 器の断片構造↔原文レコード不整合 → `IllegalStateException` |

### 2.2 軸要素 → 担保テストメソッド

**軸A（13/14 担保）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| A-01 `DEFAULT` | ❌ | — |
| A-02 `SETUP_TABLE_DATA` | ✅ | `readTable_setup_mapsUppercaseNameAndColumnsWithRawValues`, `read_mixedSections_keepsDescriptionOrderAndIgnoresUnknownKeys` |
| A-03 `EXPECTED_TABLE_DATA` | ✅ | `readTable_expectedWithGroup_formatsGroupIdAndCreatesBlockPerGroup` |
| A-04 `EXPECTED_COMPLETED` | ✅ | `readTable_completed_mapsExpectedCompletedType` |
| A-05 `LIST_MAP` | ✅ | `readListMap_preservesYamlColumnOrderExcludesMarkersAndKeepsNull` |
| A-06 `SETUP_FIXED` | ✅ | `readFile_fixed_mapsRawFieldDefsAndValues`, `readFile_recordTypeOmitted_keepsNullRecordType`, `read_containerCountMismatch_failsFast`, `read_fragmentRecordMismatch_failsFast` |
| A-07 `EXPECTED_FIXED` | ✅ | `readFile_expectedFixedWithMultipleGroups_mapsExpectedFixedAndDedupesGroups` |
| A-08 `SETUP_VARIABLE` | ✅ | `readFile_setupVariable_mapsSetupVariableType`, `readFile_recordTypeDefault_normalizedToNull` |
| A-09 `EXPECTED_VARIABLE` | ✅ | `readFile_variable_mapsVariableTypeWithNullLengths` |
| A-10 `MESSAGE` | ✅ | `readMessage_mapsRawFwHeaderAndExcludesFwHeaderRecord`, `readMessage_emptyBody_isStillMapped`, `read_mixedSections_keepsDescriptionOrderAndIgnoresUnknownKeys` |
| A-11 `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `readSendSync_groupsByRawValueFormatsGroupIdAndKeepsNoField`, `readSendSync_allFourTypesAreRecognized` |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `readSendSync_allFourTypesAreRecognized` |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅ | `readSendSync_allFourTypesAreRecognized` |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅ | `readSendSync_allFourTypesAreRecognized`, `readSendSync_entryWithoutGroupId_isDropped` |

**軸B（4/4 担保）**

| 要素 | 判定 | 担保テストメソッド（代表） |
|---|---|---|
| B-1 `TableDataBlock` | ✅ | `readTable_setup_mapsUppercaseNameAndColumnsWithRawValues` ほか |
| B-2 `ListMapBlock` | ✅ | `readListMap_preservesYamlColumnOrderExcludesMarkersAndKeepsNull` |
| B-3 `FileDataBlock` | ✅ | `readFile_fixed_mapsRawFieldDefsAndValues` ほか |
| B-4 `MessageDataBlock` | ✅ | `readMessage_mapsRawFwHeaderAndExcludesFwHeaderRecord` ほか |

**軸C**

| 要素 | 値あり／非空 | 省略／空 | 担保テストメソッド |
|---|---|---|---|
| C-01 `TestDataContainer.name` | ✅ | n/a | `read_namesContainerAndSectionByResourceName` |
| C-02 `TestDataContainer.sections` | ✅(1件) | ❌ 空 | `read_namesContainerAndSectionByResourceName`（※辺②は 1 YAML ファイル＝1 セクション固定） |
| C-03 `TestDataSection.name` | ✅ | n/a | `read_namesContainerAndSectionByResourceName` |
| C-04 `TestDataSection.blocks` | ✅ | ✅ 空 | 空: `read_namesContainerAndSectionByResourceName`, `readMessage_nullContent_isSkipped` |
| C-05 `TestDataBlock.dataType` | ✅ | n/a | 多数 |
| C-06 `TestDataBlock.groupId` | ✅ | ✅ 省略(`""`) | 値あり: `readTable_expectedWithGroup_...`, `readFile_expectedFixedWithMultipleGroups_...`, `readSendSync_groupsByRawValue...`／省略: `readTable_setup_...`, `readMessage_mapsRawFwHeader...` |
| C-07 `TestDataBlock.identifier` | ✅ | n/a | 多数 |
| C-08 `ColumnRowDataBlock.columnNames` | ✅ | ❌ 空 | 非空: `readTable_setup_...`, `readListMap_preservesYamlColumnOrder...` |
| C-09 `ColumnRowDataBlock.rows` | ✅ | ❌ 空 | 非空: `readTable_setup_...` ほか |
| C-10 `FileDataBlock.fileType` | ✅ FIXED / ✅ VARIABLE | n/a | FIXED: `readFile_fixed_...`／VARIABLE: `readFile_variable_...`, `readFile_setupVariable_...` |
| C-11 `FileDataBlock.directives` | ✅ | ❌ 空 | 非空: `readFile_fixed_mapsRawFieldDefsAndValues` のみ |
| C-12 `FileDataBlock.records` | ✅ | ❌ 空 | 非空: `readFile_fixed_...` |
| C-13 `MessageDataBlock.directives` | ❌ | ❌ | **アサートするテストが 0 件**（`getDirectives()` の呼び出しは `YamlFormatReaderTest` L164 の FileDataBlock のみ） |
| C-14 `MessageDataBlock.fwHeaderFields` | ✅ | ✅ 空 | 非空: `readMessage_mapsRawFwHeaderAndExcludesFwHeaderRecord`／空: `readMessage_emptyBody_isStillMapped`, `readSendSync_groupsByRawValue...` |
| C-15 `MessageDataBlock.records` | ✅ | ✅ 空 | 非空: `readMessage_mapsRawFwHeader...`／空: `readMessage_emptyBody_isStillMapped` |
| C-16 `RecordLayout.recordType` | ✅ | ✅ 省略(null) | 値あり: `readFile_fixed_...`, `readMessage_mapsRawFwHeader...`／省略: `readFile_recordTypeOmitted_keepsNullRecordType`, `readFile_recordTypeDefault_normalizedToNull` |
| C-17 `RecordLayout.fields` | ✅ | ❌ 空 | 非空: `readFile_fixed_...` ほか |
| C-18 `RecordLayout.rows` | ✅ | ❌ 空 | 非空: `readFile_fixed_...` ほか |
| C-19 `FieldDef.name` | ✅ | n/a | `readFile_fixed_...` ほか |
| C-20 `FieldDef.type` | ✅ | ❌ 省略(null) | 値あり: `readFile_fixed_...`（`半角英字`/`数値`） |
| C-21 `FieldDef.length` | ✅ | ✅ 省略(null) | 値あり: `readFile_fixed_...`（`"5"`）／省略: `readFile_fixed_...`（`f2`）, `readFile_variable_...` |

**軸D（0/10 担保）**

D2-01〜D2-10 すべて ❌。理由: 既存 20 件は `YamlFormatReaderTest#reader`（L538-545）で `loadRawMap` を
in-memory `LinkedHashMap` に差し替えるため、YAML テキストのパースを一切通らない。スカラー型の解釈が発生しない。

🔺 弱い担保（辺④の往復テスト経由・実 YAML ファイルを通る）:
- D2-02 引用符あり文字列: `YamlFormatWriterTest#roundTrip_table_isPreservedThroughRealReader` ほか往復 6 件
- D2-03 数値（`"123"`）: `YamlFormatWriterTest#roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader`
- D2-06 `null`: 同上（`~` と「値なし」は未担保）
- D2-07 `"null"`: 同上

**軸E**

| 要素 | 0 件 | 1 件 | 複数件 | 担保テストメソッド |
|---|---|---|---|---|
| E-1 セクション内ブロック数 | ✅ | ✅ | ✅ | 0: `read_namesContainerAndSectionByResourceName`, `readMessage_nullContent_isSkipped`／複数: `readTable_expectedWithGroup_...`, `readFile_expectedFixedWithMultipleGroups_...`, `readSendSync_groupsByRawValue...`, `readSendSync_allFourTypesAreRecognized`, `read_mixedSections_...` |
| E-2 ブロック内行数 | ❌ | ✅ | ✅ | 複数: `readTable_setup_...`, `readListMap_preservesYamlColumnOrder...`, `readFile_fixed_...` |
| E-3 ファイル内レコードレイアウト数 | ✅ | ✅ | ✅ | 0: `readMessage_emptyBody_isStillMapped`／複数: `readFile_fixed_mapsRawFieldDefsAndValues` |
| E-4 ディレクトリ内ファイル数（＝セクション数） | n/a | ✅ | ❌ | 1: `read_namesContainerAndSectionByResourceName`（※`YamlFormatReader#read` は 1 リソース単位） |

**軸F（1/5 担保）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| F2-01 スキーマ違反 | ❌ | —（`YamlTestDataValidatorTest` は別クラスであり本辺の対象外） |
| F2-02 YAML として不正 | ❌ | — |
| F2-03 未知のキー | ✅ | `read_mixedSections_keepsDescriptionOrderAndIgnoresUnknownKeys` |
| F2-04 必須構造の欠落 | 🔺 | `readSendSync_entryWithoutGroupId_isDropped`（送信系必須 `group_id` の欠落）、`readMessage_nullContent_isSkipped` |
| F2-05 空ファイル | 🔺 | `read_namesContainerAndSectionByResourceName`（空 Map。実ファイルではない） |
| （追加で担保済みの異常系） | ✅ | `read_containerCountMismatch_failsFast`, `read_fragmentRecordMismatch_failsFast`（器↔原文の不整合 fail-fast。steering の 5 ケースには含まれないが本辺固有の異常系として担保済み） |

### 2.3 辺② 未担保一覧（#24 が埋める対象）

| 軸 | 未担保要素 | 件数 |
|---|---|---|
| A | A-01 `DEFAULT` | 1 |
| B | （なし） | 0 |
| C | C-02 sections 空／C-08 columnNames 空／C-09 rows 空／C-11 FileDataBlock.directives 空／C-12 FileDataBlock.records 空／**C-13 MessageDataBlock.directives 値あり・空の双方**／C-17 fields 空／C-18 RecordLayout.rows 空／C-20 FieldDef.type 省略 | 9（うち C-13 は 2 状態とも欠） |
| D | D2-01〜D2-10 全 10 ケース（うち D2-02/D2-03/D2-06/D2-07 は辺④往復経由の弱い担保あり） | 10 |
| E | E-2(0件)／E-4(複数) | 2 |
| F | F2-01 スキーマ違反／F2-02 不正 YAML／F2-04 必須構造欠落（弱のみ）／F2-05 空ファイル（弱のみ） | 4 |
| **合計** | | **26** |

**特に大きな空欄**: 軸D 10 ケース全滅（実 YAML テキストを一度も通らないため）と、
`MessageDataBlock.directives`（C-13）が値あり・空の両方とも 0 件。

---

## 3. 辺③ 中間モデル→Excel（`XlsFormatWriterTest` 40 件）

### 3.1 テストメソッド → 担保する軸要素

| # | テストメソッド | 軸A | 軸B | 軸C | 軸D | 軸E | 軸F |
|---|---|---|---|---|---|---|---|
| 1 | `writesTableBlock` | A-02 | B-1 | C-06(省略→`[]` なし), C-07, C-08, C-09 | 🔺D3-04 null→リテラル `"null"`／🔺D3-05 `""`→空セル（いずれも `getStringCellValue` のみ） | E-2(複数=2) | — |
| 2 | `metaRowContainsOnlyValueCells` | A-02 | B-1 | — | — | E-2(1) | — |
| 3 | `writesTableMarkerWithGroupId` | A-03 | B-1 | C-06(値あり) | — | E-2(1) | — |
| 4 | `writesExpectedCompleteTableMarker` | A-04 | B-1 | C-05 | — | E-2(1) | — |
| 5 | `writesListMapBlock` | A-05 | B-2 | C-07, C-08, C-09 | 🔺D3-05 | E-2(複数=2) | — |
| 6 | `tintsMarkerColumn` | A-02 | B-1 | — | 🔺マーカーカラム `[NOTE]` 記法 | — | — |
| 7 | `writesFixedFileBlock` | A-06 | B-3 | C-07, C-11(値あり), C-12, C-16(値あり), C-17, C-18, C-19, C-20, C-21(`"-"`/`"5"`) | 🔺長さ記法 `-` | E-3(1) | — |
| 8 | `writesVariableFileWithoutLengthRow` | A-08 | B-3 | C-10(VARIABLE 版面), C-11(空), C-21(省略) | — | E-3(1) | — |
| 9 | `writesMultipleRecordLayouts` | A-06 | B-3 | C-12(2件), C-11(空) | — | **E-3(複数=2)** | — |
| 10 | `rejectsNullRecordTypeOnSecondRecord` | A-06 | B-3 | C-16(null) | — | E-3(複数) | ✅ 2 レコード目 recordType null → `IllegalStateException` |
| 11 | `rejectsEmptyRecordTypeOnSecondRecord` | A-06 | B-3 | C-16(`""`) | — | E-3(複数) | ✅ 2 レコード目 recordType 空文字 → `IllegalStateException` |
| 12 | `allowsNullRecordTypeOnSingleRecord` | A-06 | B-3 | **C-16(省略=null)** ✅ | — | E-3(1) | — |
| 13 | `writesMessageBlock` | A-10 | B-4 | C-07, C-13(空), C-14(値あり), C-15, C-17, C-18, C-19, C-20, C-21 | — | E-3(1) | — |
| 14 | `writesSendSyncMessageWithSequenceNo` | A-11 | B-4 | C-06(値あり), C-07, C-13(空), C-14(空), C-15, C-18 | — | E-2(複数=2) | — |
| 15 | `appliesHeaderBackgroundColor` | A-02 | B-1 | — | — | — | — |
| 16 | `appliesTestShotsHeaderColor` | A-05 | B-2 | — | — | — | — |
| 17 | `appliesSetupHeaderColor` | A-02 | B-1 | — | — | — | — |
| 18 | `appliesExpectedHeaderColor` | A-03 | B-1 | — | — | — | — |
| 19 | `appliesOtherHeaderColorForMessage` | A-10 | B-4 | C-14(値あり) | — | — | — |
| 20 | `appliesOtherHeaderColorForNonTestShotsListMap` | A-05 | B-2 | — | — | — | — |
| 21 | `eachGroupHasDistinctDefaultColor` | — | — | — | — | — | —（SUT は `ExcelFormatConfig`。Writer は駆動しない） |
| 22 | `drawsBlockOuterBorder` | A-02 | B-1 | — | — | E-2(1) | — |
| 23 | `insertsBlankRowBetweenBlocks` | A-02 | B-1 | — | — | **E-1(複数=2)** | — |
| 24 | `appliesAutoColumnWidth` | A-02 | B-1 | — | — | — | — |
| 25 | `honorsConfigOverrides` | A-02 | B-1 | — | — | E-1(複数=2) | — |
| 26 | `rejectsNegativeBlankRows` | — | — | — | — | — | ✅ 設定値負数 → `IllegalArgumentException`（steering の 4 ケース外） |
| 27 | `writesWorkbookFileWithSheetPerSection` | A-02, A-05 | B-1, B-2 | C-01, **C-02(複数=2)**, C-03 | — | **E-4(複数=2)** | — |
| 28 | `honorsMarkerColumnColorOverride` | A-02 | B-1 | — | 🔺マーカーカラム記法 | — | — |
| 29 | `doesNotTintUnclosedBracketColumn` | A-02 | B-1 | — | 🔺未閉じ括弧 `[half` はマーカーでない | — | — |
| 30 | `writesOmittedMetaAndFieldAsEmpty` | A-06 | B-3 | **C-20(省略)**, **C-21(省略)**, C-11(値 null) | 🔺null→空セル（メタ側） | E-3(1) | — |
| 31 | `writesSequenceNoForAllSendSyncTypes` | A-11, A-12, A-13, A-14 | B-4 | C-05, C-06(値あり) | — | E-3(1) | — |
| 32 | `wrapsIoFailure` | A-02 | B-1 | — | — | — | 🔺**F3-01**（親に通常ファイルが居座り出力先を作れない）→ `UncheckedIOException` |
| 33 | `roundTripsTable` | A-02 | B-1 | C-05, C-07, C-08, C-09 | 🔺実 `.xlsx` 往復（文字列・`${}`・空文字） | E-2(複数=2) | — |
| 34 | `roundTripsNullCellAsLiteralNullString` | A-02 | B-1 | C-09 | 🔺D3-04 null→`"null"`（非可逆を固定）／🔺D3-05 `""` | E-2(1) | — |
| 35 | `roundTripsListMap` | A-05 | B-2 | C-07, C-08, C-09 | 🔺実 `.xlsx` 往復 | E-2(複数=2) | — |
| 36 | `roundTripsFixedFile` | A-06 | B-3 | C-07, C-10(FIXED), C-16, C-18, C-20, C-21 | 🔺長さ記法 `-` の往復 | E-3(1) | — |
| 37 | `roundTripsMultipleRecordLayouts` | A-06 | B-3 | C-12(2件), C-16, C-18 | — | E-3(複数=2) | — |
| 38 | `roundTripsVariableFile` | A-08 | B-3 | **C-10(VARIABLE)** ✅, C-21(省略) | — | E-3(1) | — |
| 39 | `roundTripsMessage` | A-10 | B-4 | C-05, C-07, C-14(値あり), C-17, C-18, C-19 | — | E-3(1) | — |
| 40 | `roundTripsSendSyncMessage` | A-11 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-17, C-18, C-19 | — | E-3(1) | — |

### 3.2 軸要素 → 担保テストメソッド

**軸A（11/14 担保）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| A-01 `DEFAULT` | ❌ | — |
| A-02 `SETUP_TABLE_DATA` | ✅ | `writesTableBlock`, `metaRowContainsOnlyValueCells`, `appliesHeaderBackgroundColor`, `appliesSetupHeaderColor`, `drawsBlockOuterBorder`, `insertsBlankRowBetweenBlocks`, `appliesAutoColumnWidth`, `honorsConfigOverrides`, `writesWorkbookFileWithSheetPerSection`, `honorsMarkerColumnColorOverride`, `doesNotTintUnclosedBracketColumn`, `tintsMarkerColumn`, `wrapsIoFailure`, `roundTripsTable`, `roundTripsNullCellAsLiteralNullString` |
| A-03 `EXPECTED_TABLE_DATA` | ✅ | `writesTableMarkerWithGroupId`, `appliesExpectedHeaderColor` |
| A-04 `EXPECTED_COMPLETED` | ✅ | `writesExpectedCompleteTableMarker` |
| A-05 `LIST_MAP` | ✅ | `writesListMapBlock`, `appliesTestShotsHeaderColor`, `appliesOtherHeaderColorForNonTestShotsListMap`, `writesWorkbookFileWithSheetPerSection`, `roundTripsListMap` |
| A-06 `SETUP_FIXED` | ✅ | `writesFixedFileBlock`, `writesMultipleRecordLayouts`, `rejectsNullRecordTypeOnSecondRecord`, `rejectsEmptyRecordTypeOnSecondRecord`, `allowsNullRecordTypeOnSingleRecord`, `writesOmittedMetaAndFieldAsEmpty`, `roundTripsFixedFile`, `roundTripsMultipleRecordLayouts` |
| A-07 `EXPECTED_FIXED` | ❌ | — |
| A-08 `SETUP_VARIABLE` | ✅ | `writesVariableFileWithoutLengthRow`, `roundTripsVariableFile` |
| A-09 `EXPECTED_VARIABLE` | ❌ | — |
| A-10 `MESSAGE` | ✅ | `writesMessageBlock`, `appliesOtherHeaderColorForMessage`, `roundTripsMessage` |
| A-11 `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `writesSendSyncMessageWithSequenceNo`, `writesSequenceNoForAllSendSyncTypes`, `roundTripsSendSyncMessage` |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `writesSequenceNoForAllSendSyncTypes` |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅ | `writesSequenceNoForAllSendSyncTypes` |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅ | `writesSequenceNoForAllSendSyncTypes` |

**軸B（4/4 担保）**

| 要素 | 判定 | 担保テストメソッド（代表） |
|---|---|---|
| B-1 `TableDataBlock` | ✅ | `writesTableBlock`, `roundTripsTable` ほか |
| B-2 `ListMapBlock` | ✅ | `writesListMapBlock`, `roundTripsListMap` ほか |
| B-3 `FileDataBlock` | ✅ | `writesFixedFileBlock`, `writesVariableFileWithoutLengthRow`, `roundTripsFixedFile` ほか |
| B-4 `MessageDataBlock` | ✅ | `writesMessageBlock`, `writesSendSyncMessageWithSequenceNo`, `roundTripsMessage` ほか |

**軸C**

| 要素 | 値あり／非空 | 省略／空 | 担保テストメソッド |
|---|---|---|---|
| C-01 `TestDataContainer.name` | ✅ | n/a | `writesWorkbookFileWithSheetPerSection`（`MyBook.xlsx`） |
| C-02 `TestDataContainer.sections` | ✅(複数=2) | ❌ 空 | `writesWorkbookFileWithSheetPerSection` |
| C-03 `TestDataSection.name` | ✅ | n/a | `writesWorkbookFileWithSheetPerSection`（Sheet1/Sheet2） |
| C-04 `TestDataSection.blocks` | ✅ | ❌ 空 | 非空: 全テスト |
| C-05 `TestDataBlock.dataType` | ✅ | n/a | `writesExpectedCompleteTableMarker`, `writesSequenceNoForAllSendSyncTypes`, `roundTripsTable` ほか |
| C-06 `TestDataBlock.groupId` | ✅ | ✅ 省略(`""`) | 値あり: `writesTableMarkerWithGroupId`, `writesSendSyncMessageWithSequenceNo`, `roundTripsSendSyncMessage`／省略: `writesTableBlock`（`SETUP_TABLE=USERS` に `[]` が出ない） |
| C-07 `TestDataBlock.identifier` | ✅ | n/a | 多数 |
| C-08 `ColumnRowDataBlock.columnNames` | ✅ | ❌ 空 | 非空: `writesTableBlock`, `writesListMapBlock`, `roundTripsTable` |
| C-09 `ColumnRowDataBlock.rows` | ✅ | ❌ 空 | 非空: 多数 |
| C-10 `FileDataBlock.fileType` | ✅ FIXED / ✅ VARIABLE | n/a | FIXED: `roundTripsFixedFile`（明示アサート）, `writesFixedFileBlock`（長さ行の有無で暗黙）／VARIABLE: `roundTripsVariableFile`（明示）, `writesVariableFileWithoutLengthRow`（暗黙） |
| C-11 `FileDataBlock.directives` | ✅ | ✅ 空 | 非空: `writesFixedFileBlock`, `writesOmittedMetaAndFieldAsEmpty`／空: `writesVariableFileWithoutLengthRow`, `writesMultipleRecordLayouts`（ディレクティブ行が出ない版面をアサート） |
| C-12 `FileDataBlock.records` | ✅ | ❌ 空 | 非空: `writesFixedFileBlock`, `writesMultipleRecordLayouts` |
| C-13 `MessageDataBlock.directives` | ❌ | ✅ 空 | **値ありのテストが 0 件**（`XlsFormatWriterTest` の `new MessageDataBlock(...)` 6 箇所すべてで 4 引数目が空 `map()`: L405-406, L432-433, L543-544, L833, L1033-1034, L1060-1061） |
| C-14 `MessageDataBlock.fwHeaderFields` | ✅ | ✅ 空 | 非空: `writesMessageBlock`, `appliesOtherHeaderColorForMessage`, `roundTripsMessage`／空: `writesSendSyncMessageWithSequenceNo`, `roundTripsSendSyncMessage` |
| C-15 `MessageDataBlock.records` | ✅ | ❌ 空 | 非空: `writesMessageBlock` ほか |
| C-16 `RecordLayout.recordType` | ✅ | ✅ 省略(null) | 値あり: `writesFixedFileBlock`, `writesMultipleRecordLayouts`／省略: `allowsNullRecordTypeOnSingleRecord` |
| C-17 `RecordLayout.fields` | ✅ | ❌ 空 | 非空: `writesFixedFileBlock` ほか |
| C-18 `RecordLayout.rows` | ✅ | ❌ 空 | 非空: 多数 |
| C-19 `FieldDef.name` | ✅ | n/a | `writesFixedFileBlock` ほか |
| C-20 `FieldDef.type` | ✅ | ✅ 省略(null) | 値あり: `writesFixedFileBlock`／省略: `writesOmittedMetaAndFieldAsEmpty` |
| C-21 `FieldDef.length` | ✅ | ✅ 省略(null) | 値あり: `writesFixedFileBlock`（`"-"`/`"5"`）／省略: `writesVariableFileWithoutLengthRow`, `writesOmittedMetaAndFieldAsEmpty`, `roundTripsVariableFile` |

**軸D（0/8 担保 — `getCellType()` 観点）**

| 要素 | 判定 | 備考 |
|---|---|---|
| D3-01 `"100"` | ❌ | 数値セルにならないことのアサートなし |
| D3-02 `"=1+1"` | ❌ | 数式解釈されないことのアサートなし |
| D3-03 `"007"` | ❌ | — |
| D3-04 `null` | 🔺 | `writesTableBlock`, `roundTripsNullCellAsLiteralNullString` が値（`"null"` 文字列化）はアサートするが `getCellType()` はしない |
| D3-05 `""` | 🔺 | `writesTableBlock`, `writesListMapBlock`, `roundTripsNullCellAsLiteralNullString` が値のみアサート |
| D3-06 改行含む文字列 | ❌ | — |
| D3-07 32767 文字超 | ❌ | — |
| D3-08 制御文字含む | ❌ | — |

`grep -rn "getCellType" src/test/` → **0 件**。セル読み出しヘルパ `cell`（L100-107）/`line`（L110-121）は `getStringCellValue()` 固定。

**軸E**

| 要素 | 0 件 | 1 件 | 複数件 | 担保テストメソッド |
|---|---|---|---|---|
| E-1 セクション内ブロック数 | ❌ | ✅ | ✅ | 複数: `insertsBlankRowBetweenBlocks`, `honorsConfigOverrides` |
| E-2 ブロック内行数 | ❌ | ✅ | ✅ | 複数: `writesTableBlock`, `writesListMapBlock`, `writesSendSyncMessageWithSequenceNo`, `roundTripsTable` |
| E-3 ファイル内レコードレイアウト数 | ❌ | ✅ | ✅ | 複数: `writesMultipleRecordLayouts`, `roundTripsMultipleRecordLayouts` |
| E-4 ブック内シート数 | n/a | ✅ | ✅ | 1: 多数／複数: `writesWorkbookFileWithSheetPerSection` |

**軸F（0/4 担保、うち 1 件が弱い担保）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| F3-01 出力先不在 | 🔺 | `wrapsIoFailure`（正確には「親に通常ファイルが居座り親ディレクトリを作れない」ケース。「出力先不在」そのものではない） |
| F3-02 `overwrite=false` 衝突 | ❌ | — （`overwrite` は `ConversionRequest`/`TestDataConverter`/`ConverterMojo` 側の関心。`XlsFormatWriter` は保持しない） |
| F3-03 書き込み権限なし | ❌ | — |
| F3-04 シート名が Excel 制約違反 | ❌ | — |
| （steering 外で担保済みの異常系） | ✅ | `rejectsNullRecordTypeOnSecondRecord`, `rejectsEmptyRecordTypeOnSecondRecord`（2 レコード目 recordType 空 → `IllegalStateException`）、`rejectsNegativeBlankRows`（設定値負数 → `IllegalArgumentException`） |

### 3.3 辺③ 未担保一覧（#22〜#23 が埋める対象）

| 軸 | 未担保要素 | 件数 |
|---|---|---|
| A | A-01 `DEFAULT`, A-07 `EXPECTED_FIXED`, A-09 `EXPECTED_VARIABLE` | 3 |
| B | （なし） | 0 |
| C | C-02 sections 空／C-04 blocks 空／C-08 columnNames 空／C-09 rows 空／C-12 FileDataBlock.records 空／**C-13 MessageDataBlock.directives 値あり**／C-15 MessageDataBlock.records 空／C-17 fields 空／C-18 RecordLayout.rows 空 | 9 |
| D | D3-01〜D3-08 全 8 ケース（D3-04/D3-05 は値のみ弱い担保。`getCellType()` は全件ゼロ） | 8 |
| E | E-1(0件)／E-2(0件)／E-3(0件) | 3 |
| F | F3-01 出力先不在（弱のみ）／F3-02 `overwrite=false` 衝突／F3-03 書き込み権限なし／F3-04 シート名制約違反 | 4 |
| **合計** | | **27** |

**特に大きな空欄**: `getCellType()` を使ったテストが 1 件も存在しないため軸D 8 ケース全滅。#22 の主眼。
次いで `MessageDataBlock.directives` に値を入れて書き出すテストが 0 件（C-13）。

---

## 4. 辺④ 中間モデル→YAML（`YamlFormatWriterTest` 33 件）

### 4.1 テストメソッド → 担保する軸要素

| # | テストメソッド | 軸A | 軸B | 軸C | 軸D | 軸E | 軸F |
|---|---|---|---|---|---|---|---|
| 1 | `serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | A-02 | B-1 | C-06(省略→`group_id` キーなし), C-07, C-08, C-09 | **D4-04 `null`** ✅, **D4-05 `""`** ✅, 🔺`${}` の全値クォート | E-2(複数=2) | — |
| 2 | `serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId` | A-03 | B-1 | C-06(値あり `[case01]`→`case01`) | — | E-1(複数=2) | — |
| 3 | `serializeTable_completed_usesExpectedCompleteTablesKey` | A-04 | B-1 | C-05 | — | E-2(1) | — |
| 4 | `serializeListMap_usesIdKeyAndColumnOrder` | A-05 | B-2 | C-07, C-08, C-09 | D4-04 `null` | E-2(複数=2) | — |
| 5 | `serializeFile_fixedWithDirectivesAndOmittedLength` | A-06 | B-3 | C-07, C-10(FIXED), C-11(値あり), C-12(2件), C-16(値あり), C-17, C-18, C-19, C-20, C-21(値あり＋省略) | D4-05 `""` | **E-3(複数=2)**, E-2(複数=2) | — |
| 6 | `serializeFile_variableOmitsDirectivesAndRecordTypeAndLength` | A-09 | B-3 | C-10(VARIABLE), **C-11(空)**, **C-16(省略)**, **C-21(省略)** | — | E-3(1) | — |
| 7 | `serializeMessage_withDirectivesAndFwHeader` | A-10 | B-4 | C-07, **C-13(値あり)** ✅, C-14(値あり), C-15, C-16, C-17, C-18, C-19, C-20, C-21 | 🔺`${}` | E-3(1) | — |
| 8 | `serializeMessage_emptyBody_emitsIdOnly` | A-10 | B-4 | C-07, C-13(空), C-14(空), **C-15(空)** ✅ | — | **E-3(0)** ✅ | — |
| 9 | `serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField` | A-11 | B-4 | C-06(値あり), C-07, C-13(空), C-14(空), C-16(省略), C-17, C-18, C-19, C-20, C-21 | 🔺`${}` | E-3(1) | — |
| 10 | `serializeSendSync_allFourSectionKeys` | A-11, A-12, A-13, A-14 | B-4 | C-05 | — | E-1(複数=4) | — |
| 11 | `serialize_multipleSections_separatedByBlankLineInEncounterOrder` | A-02, A-10 | B-1, B-4 | C-05 | — | **E-1(複数=2)** | — |
| 12 | `serialize_emptySection_isEmptyString` | — | — | **C-04(空)** ✅ | — | **E-1(0)** ✅ | — |
| 13 | `serialize_escapesQuotesBackslashAndControlChars` | A-02 | B-1 | C-09 | **D4-07 改行含む** ✅（`\n`/`\r`/`\t`/`\x01`/`"`/`\` のエスケープ） | E-2(1) | — |
| 14 | `serialize_surrogatePair_isOutputAsUtf8WithoutEscape` | A-02 | B-1 | C-09 | 🔺BMP 外文字（U+1F600）の非エスケープ出力 | E-2(複数=2) | — |
| 15 | `serialize_quotesKeyContainingSpecialChars` | A-10 | B-4 | C-13(値あり) | 🔺**キー**中のコロン・空白のクォート（D4-09 の値側ではない） | — | — |
| 16 | `serialize_emptyKey_isQuoted` | A-10 | B-4 | C-13(値あり) | 🔺空キーのクォート | — | — |
| 17 | `serialize_distinguishesNullFromNullString` | A-02 | B-1 | C-09 | **D4-03 `"null"`** ✅, **D4-04 `null`** ✅ | E-2(複数=2) | — |
| 18 | `serialize_emptyRows_emitsEmptyFlowList` | A-02 | B-1 | **C-09(空)** ✅ | — | **E-2(0)** ✅ | — |
| 19 | `serialize_emptyColumnsRow_emitsEmptyFlowMap` | A-02 | B-1 | **C-08(空)** ✅ | — | E-2(1) | — |
| 20 | `serialize_recordWithEmptyFieldsAndRows_emitsEmptyFlowLists` | A-06 | B-3 | **C-17(空)** ✅, **C-18(空)** ✅, C-16(省略) | — | E-3(1) | — |
| 21 | `serialize_rowShorterThanColumns_fillsMissingWithNull` | A-02 | B-1 | C-08, C-09 | D4-04 `null`（補完） | — | ✅ 行と列の数の不一致（行が短い → null 補完） |
| 22 | `serialize_fieldWithNullType_omitsType` | A-09 | B-3 | **C-20(省略)** ✅ | — | E-3(1) | — |
| 23 | `serialize_keyStartingWithIndicator_isQuoted` | A-10 | B-4 | C-13(値あり) | 🔺**キー**先頭の YAML インジケータ `-`（D4-09 の値側ではない） | — | — |
| 24 | `serialize_unbracketedGroupId_isUsedAsRawValue` | A-02 | B-1 | C-06(非整形値 `raw`) | — | — | 🔺防御的経路（`[]` で囲まれていない groupId） |
| 25 | `serialize_unsupportedDataType_throws` | **A-01 `DEFAULT`** ✅ | B-1 | C-05 | — | — | ✅ 未サポート `DataType` → `IllegalArgumentException` |
| 26 | `write_ioError_throwsUncheckedIOException` | A-02 | B-1 | — | — | — | 🔺**F4-01**（親に通常ファイルが居座り出力先を作れない）→ `UncheckedIOException` |
| 27 | `write_writesEachSectionAsYamlFileWithSerializedContent` | A-02 | B-1 | C-01, C-02(1件), C-03 | — | E-4(1) | — |
| 28 | `roundTrip_table_isPreservedThroughRealReader` | A-02 | B-1 | C-05, C-07, C-08, C-09 | 🔺実 YAML 往復（`${}`/`null`/`""`） | E-2(複数=2) | — |
| 29 | `roundTrip_fixedFile_isPreservedThroughRealReader` | A-06 | B-3 | C-05, C-07, C-10(FIXED), C-12(2件), C-16, C-19, C-20, C-21(値あり＋省略), C-18 | 🔺実 YAML 往復 | E-3(複数=2) | — |
| 30 | `roundTrip_message_preservesFwHeaderAndBody` | A-10 | B-4 | C-05, C-07, C-14(値あり), C-16, C-18 | 🔺`${}` の往復 | E-3(1) | — |
| 31 | `roundTrip_sendSync_preservesGroupIdAndNoField` | A-11 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-17, C-18, C-19, C-20, C-21 | 🔺`${}` の往復 | E-3(1) | — |
| 32 | `roundTrip_leadingTrailingWhitespace_isPreservedThroughRealReader` | A-02 | B-1 | C-09 | 🔺前後・中間の半角/全角空白が往復で脱落しない | E-2(1) | — |
| 33 | `roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader` | A-02 | B-1 | C-09 | 🔺**D4-01 `"100"` 相当（`"123"`）**・D4-03 `"null"`・D4-04 `null` の往復区別（出力 YAML の記法アサートではない） | E-2(複数=3) | — |

### 4.2 軸要素 → 担保テストメソッド

**軸A（12/14 担保）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| A-01 `DEFAULT` | ✅ | `serialize_unsupportedDataType_throws`（**4 辺で唯一 `DEFAULT` を通すテスト**） |
| A-02 `SETUP_TABLE_DATA` | ✅ | `serializeTable_setupNoGroup_...`, `serialize_multipleSections_...`, `serialize_escapesQuotesBackslashAndControlChars`, `serialize_surrogatePair_...`, `serialize_distinguishesNullFromNullString`, `serialize_emptyRows_...`, `serialize_emptyColumnsRow_...`, `serialize_rowShorterThanColumns_...`, `serialize_unbracketedGroupId_...`, `write_ioError_...`, `write_writesEachSectionAsYamlFile...`, `roundTrip_table_...`, `roundTrip_leadingTrailingWhitespace_...`, `roundTrip_nullAndNullStringAndNumeric_...` |
| A-03 `EXPECTED_TABLE_DATA` | ✅ | `serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId` |
| A-04 `EXPECTED_COMPLETED` | ✅ | `serializeTable_completed_usesExpectedCompleteTablesKey` |
| A-05 `LIST_MAP` | ✅ | `serializeListMap_usesIdKeyAndColumnOrder` |
| A-06 `SETUP_FIXED` | ✅ | `serializeFile_fixedWithDirectivesAndOmittedLength`, `serialize_recordWithEmptyFieldsAndRows_...`, `roundTrip_fixedFile_...` |
| A-07 `EXPECTED_FIXED` | ❌ | — |
| A-08 `SETUP_VARIABLE` | ❌ | — |
| A-09 `EXPECTED_VARIABLE` | ✅ | `serializeFile_variableOmitsDirectivesAndRecordTypeAndLength`, `serialize_fieldWithNullType_omitsType` |
| A-10 `MESSAGE` | ✅ | `serializeMessage_withDirectivesAndFwHeader`, `serializeMessage_emptyBody_emitsIdOnly`, `serialize_multipleSections_...`, `serialize_quotesKeyContainingSpecialChars`, `serialize_emptyKey_isQuoted`, `serialize_keyStartingWithIndicator_isQuoted`, `roundTrip_message_...` |
| A-11 `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField`, `serializeSendSync_allFourSectionKeys`, `roundTrip_sendSync_...` |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `serializeSendSync_allFourSectionKeys` |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅ | `serializeSendSync_allFourSectionKeys` |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅ | `serializeSendSync_allFourSectionKeys` |

**軸B（4/4 担保）**

| 要素 | 判定 | 担保テストメソッド（代表） |
|---|---|---|
| B-1 `TableDataBlock` | ✅ | `serializeTable_setupNoGroup_...` ほか |
| B-2 `ListMapBlock` | ✅ | `serializeListMap_usesIdKeyAndColumnOrder` |
| B-3 `FileDataBlock` | ✅ | `serializeFile_fixedWithDirectivesAndOmittedLength` ほか |
| B-4 `MessageDataBlock` | ✅ | `serializeMessage_withDirectivesAndFwHeader` ほか |

**軸C（4 辺中もっとも充実。19/21 が両状態とも担保）**

| 要素 | 値あり／非空 | 省略／空 | 担保テストメソッド |
|---|---|---|---|
| C-01 `TestDataContainer.name` | ✅ | n/a | `write_writesEachSectionAsYamlFileWithSerializedContent`（`td.yaml`） |
| C-02 `TestDataContainer.sections` | ✅(1件) | ❌ 空／❌ 複数 | `write_writesEachSectionAsYamlFileWithSerializedContent` |
| C-03 `TestDataSection.name` | ✅ | n/a | `write_writesEachSectionAsYamlFileWithSerializedContent` |
| C-04 `TestDataSection.blocks` | ✅ | ✅ 空 | 空: `serialize_emptySection_isEmptyString` |
| C-05 `TestDataBlock.dataType` | ✅ | n/a | 多数 |
| C-06 `TestDataBlock.groupId` | ✅ | ✅ 省略(`""`) | 値あり: `serializeTable_withGroupsSameType_...`, `serializeSendSync_requiresGroupId...`, `serialize_unbracketedGroupId_...`／省略: `serializeTable_setupNoGroup_...` |
| C-07 `TestDataBlock.identifier` | ✅ | n/a | 多数 |
| C-08 `ColumnRowDataBlock.columnNames` | ✅ | ✅ 空 | 空: `serialize_emptyColumnsRow_emitsEmptyFlowMap` |
| C-09 `ColumnRowDataBlock.rows` | ✅ | ✅ 空 | 空: `serialize_emptyRows_emitsEmptyFlowList` |
| C-10 `FileDataBlock.fileType` | ✅ FIXED / ✅ VARIABLE | n/a | FIXED: `serializeFile_fixedWithDirectivesAndOmittedLength`（`type: "fixed"`）／VARIABLE: `serializeFile_variableOmits...`（`type: "variable"`） |
| C-11 `FileDataBlock.directives` | ✅ | ✅ 空 | 非空: `serializeFile_fixedWith...`／空: `serializeFile_variableOmits...`（`directives:` キーが出ない） |
| C-12 `FileDataBlock.records` | ✅ | ❌ 空 | 非空: `serializeFile_fixedWith...`（2 件） |
| C-13 `MessageDataBlock.directives` | ✅ | ✅ 空 | 非空: `serializeMessage_withDirectivesAndFwHeader`, `serialize_quotesKeyContainingSpecialChars`, `serialize_emptyKey_isQuoted`, `serialize_keyStartingWithIndicator_isQuoted`／空: `serializeMessage_emptyBody_emitsIdOnly` |
| C-14 `MessageDataBlock.fwHeaderFields` | ✅ | ✅ 空 | 非空: `serializeMessage_withDirectivesAndFwHeader`, `roundTrip_message_...`／空: `serializeMessage_emptyBody_...`, `serializeSendSync_requiresGroupId...` |
| C-15 `MessageDataBlock.records` | ✅ | ✅ 空 | 空: `serializeMessage_emptyBody_emitsIdOnly` |
| C-16 `RecordLayout.recordType` | ✅ | ✅ 省略(null) | 値あり: `serializeFile_fixedWith...`, `serializeMessage_withDirectives...`／省略: `serializeFile_variableOmits...`, `serializeSendSync_requiresGroupId...`, `serialize_recordWithEmptyFieldsAndRows_...` |
| C-17 `RecordLayout.fields` | ✅ | ✅ 空 | 空: `serialize_recordWithEmptyFieldsAndRows_emitsEmptyFlowLists` |
| C-18 `RecordLayout.rows` | ✅ | ✅ 空 | 空: `serialize_recordWithEmptyFieldsAndRows_emitsEmptyFlowLists` |
| C-19 `FieldDef.name` | ✅ | n/a | 多数 |
| C-20 `FieldDef.type` | ✅ | ✅ 省略(null) | 省略: `serialize_fieldWithNullType_omitsType` |
| C-21 `FieldDef.length` | ✅ | ✅ 省略(null) | 値あり: `serializeFile_fixedWith...`（`"5"`）／省略: `serializeFile_fixedWith...`（`f2`）, `serializeFile_variableOmits...` |

**軸D（4/9 担保）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| D4-01 `"100"` | 🔺 | `roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader`（`"123"` を往復で区別。**出力 YAML の記法（クォート有無）をアサートしていない**） |
| D4-02 `"true"` | ❌ | — |
| D4-03 `"null"` | ✅ | `serialize_distinguishesNullFromNullString`（`V: "null"`）, `roundTrip_nullAndNullStringAndNumeric_...` |
| D4-04 `null` | ✅ | `serializeTable_setupNoGroup_...`（`NOTE: null`）, `serializeListMap_usesIdKeyAndColumnOrder`, `serialize_distinguishesNullFromNullString`, `serialize_rowShorterThanColumns_fillsMissingWithNull` |
| D4-05 `""` | ✅ | `serializeTable_setupNoGroup_...`（`NAME: ""`）, `serializeFile_fixedWith...`（`[""]`） |
| D4-06 `"007"` | ❌ | — |
| D4-07 改行含む | ✅ | `serialize_escapesQuotesBackslashAndControlChars`（`\n`/`\r` を `\\n`/`\\r` へエスケープ） |
| D4-08 `"2026-08-07"` | ❌ | — |
| D4-09 コロン・ハイフン・`#` 含む | 🔺 | **キー側のみ**: `serialize_quotesKeyContainingSpecialChars`（`a:b`, `c d`）, `serialize_keyStartingWithIndicator_isQuoted`（`-x`）。**値側は未担保**。`#` はキー・値とも未担保 |

**軸E**

| 要素 | 0 件 | 1 件 | 複数件 | 担保テストメソッド |
|---|---|---|---|---|
| E-1 セクション内ブロック数 | ✅ | ✅ | ✅ | 0: `serialize_emptySection_isEmptyString`／複数: `serializeTable_withGroupsSameType_...`, `serializeSendSync_allFourSectionKeys`, `serialize_multipleSections_...` |
| E-2 ブロック内行数 | ✅ | ✅ | ✅ | 0: `serialize_emptyRows_emitsEmptyFlowList`／複数: `serializeTable_setupNoGroup_...`, `serializeFile_fixedWith...`, `roundTrip_nullAndNullStringAndNumeric_...` |
| E-3 ファイル内レコードレイアウト数 | ✅ | ✅ | ✅ | 0: `serializeMessage_emptyBody_emitsIdOnly`／複数: `serializeFile_fixedWith...`, `roundTrip_fixedFile_...` |
| E-4 コンテナ内セクション数 | n/a | ✅ | ❌ | 1: `write_writesEachSectionAsYamlFileWithSerializedContent` |

**軸F（0/3 担保、うち 1 件が弱い担保）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| F4-01 出力先不在 | 🔺 | `write_ioError_throwsUncheckedIOException`（正確には「親に通常ファイルが居座り親ディレクトリを作れない」ケース） |
| F4-02 `overwrite=false` 衝突 | ❌ | — （`overwrite` は `ConversionRequest`/`TestDataConverter`/`ConverterMojo` 側の関心） |
| F4-03 書き込み権限なし | ❌ | — |
| （steering 外で担保済みの異常系） | ✅ | `serialize_unsupportedDataType_throws`（`DataType.DEFAULT` → `IllegalArgumentException`）、`serialize_rowShorterThanColumns_fillsMissingWithNull`（行と列の数の不一致 → null 補完）、`serialize_unbracketedGroupId_isUsedAsRawValue`（非整形 groupId の防御的経路） |

### 4.3 辺④ 未担保一覧（#25 が埋める対象）

| 軸 | 未担保要素 | 件数 |
|---|---|---|
| A | A-07 `EXPECTED_FIXED`, A-08 `SETUP_VARIABLE` | 2 |
| B | （なし） | 0 |
| C | C-02 sections 空・複数／C-12 FileDataBlock.records 空 | 2 |
| D | D4-01 `"100"`（記法アサートなし・弱）／D4-02 `"true"`／D4-06 `"007"`／D4-08 `"2026-08-07"`／D4-09 値側のコロン・ハイフン・`#` | 5 |
| E | E-4(複数セクション) | 1 |
| F | F4-01 出力先不在（弱のみ）／F4-02 `overwrite=false` 衝突／F4-03 書き込み権限なし | 3 |
| **合計** | | **13** |

**特に大きな空欄**: 軸D の 5 ケース（特に `"true"`・`"007"`・日付風文字列は、辺②で読み戻したときに
型が変わりうる往復リスクの中心）。軸C は 4 辺中もっとも埋まっている。

---

## 5. 全体サマリ

### 5.1 未担保件数（辺 × 軸）

| 軸 | 辺① | 辺② | 辺③ | 辺④ | 合計 |
|---|---|---|---|---|---|
| A データタイプ | 4 | 1 | 3 | 2 | 10 |
| B ブロック実装 | 0 | 0 | 0 | 0 | 0 |
| C 中間モデル全フィールド | 12 | 9 | 9 | 2 | 32 |
| D 値の表現 | 17 | 10 | 8 | 5 | 40 |
| E 多重度 | 3 | 2 | 3 | 1 | 9 |
| F 異常系 | 5 | 4 | 4 | 3 | 16 |
| **合計** | **41** | **26** | **27** | **13** | **107** |

### 5.2 軸A の辺横断ビュー（`DataType` 14 種 × 4 辺）

| DataType | 辺① | 辺② | 辺③ | 辺④ |
|---|---|---|---|---|
| A-01 `DEFAULT` | ❌ | ❌ | ❌ | ✅ |
| A-02 `SETUP_TABLE_DATA` | ✅ | ✅ | ✅ | ✅ |
| A-03 `EXPECTED_TABLE_DATA` | ✅ | ✅ | ✅ | ✅ |
| A-04 `EXPECTED_COMPLETED` | ❌ | ✅ | ✅ | ✅ |
| A-05 `LIST_MAP` | ✅ | ✅ | ✅ | ✅ |
| A-06 `SETUP_FIXED` | ✅ | ✅ | ✅ | ✅ |
| A-07 `EXPECTED_FIXED` | ❌ | ✅ | ❌ | ❌ |
| A-08 `SETUP_VARIABLE` | ✅ | ✅ | ✅ | ❌ |
| A-09 `EXPECTED_VARIABLE` | ❌ | ✅ | ❌ | ✅ |
| A-10 `MESSAGE` | ✅ | ✅ | ✅ | ✅ |
| A-11 `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | ✅ | ✅ | ✅ |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | ✅ | ✅ | ✅ |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅ | ✅ | ✅ | ✅ |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅ | ✅ | ✅ | ✅ |
| **担保数** | 10/14 | 13/14 | 11/14 | 12/14 |

`EXPECTED_FIXED`（A-07）は 4 辺中 1 辺しか通っていない最弱の要素。

### 5.3 コーディネータに判断を仰ぎたい点

1. **軸C の「省略」定義**: steering #20 の Steps は `identifier` と `fileType` も「値あり」「省略」双方を通すとしているが、
   実定義には省略表現がない（0.4 参照）。実定義を正として `fileType` は FIXED/VARIABLE の 2 値、
   `identifier` は必須スカラーとして扱ってよいか。
2. **軸E の構造上到達不能な組み合わせ**: 辺①・辺②の `read` は 1 リソース（1 シート／1 YAML ファイル）単位のため、
   E-4「ブック内シート数 複数」と C-02「sections 複数」は API 上到達できない可能性が高い。
   #21/#24 で「到達不能」として理由付きで空欄のまま残す扱いでよいか。
   （`ConverterFileFilter`/`TestDataConverter` を経由すればブック単位の複数シート変換になるが、それは辺の担当クラス外）
3. **辺③ / 辺④ の `overwrite=false` 衝突（F3-02 / F4-02）**: `overwrite` を保持するのは
   `ConversionRequest` / `TestDataConverter` / `ConverterMojo` であり、`XlsFormatWriter` / `YamlFormatWriter` は保持しない
   （0.8-5 参照）。writer 単体では再現できないため、`TestDataConverter` 側の既存テストで担保済みか確認したうえで
   「本辺の対象外」とするか、writer のテストから `TestDataConverter` を呼んで担保するかの判断が要る。
4. **既存の往復テスト（`XlsFormatWriterTest#roundTrips*` 8 件、`YamlFormatWriterTest#roundTrip_*` 6 件）の扱い**:
   steering Rules（フェーズ2）に「各辺の担保を往復テスト（`RoundTripTest`）の追加で代替しない」とあるが、
   これらは `RoundTripTest` クラスとは別の、既存の辺③/辺④テスト内の往復である。
   本棚卸しでは辺①/辺②の担保としては「🔺弱い担保」に留め、正式な担保として数えていない。この扱いでよいか。
5. **`serialize_unsupportedDataType_throws` が `DataType.DEFAULT` を唯一通す**: 辺①〜③で `DEFAULT` を通すべきか。
   `DEFAULT` は「どのタイプにも属さない」ためリーダ側では生成されえない可能性がある。
   #20/#23/#24 で「到達不能」として理由付きで残す扱いが妥当か。
