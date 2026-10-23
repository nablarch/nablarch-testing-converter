# #33 Completion Check

指示書 2-2（`nablarch-document@a16be0a` の `.rn/20260724-ntf-yaml-support/ntf-step4-05-nablarch-testing-converter.md`）。
参照点: 解説書 `nablarch-document@5783b35` ／ `nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@0b3015c`。

**レビュア subagent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| `testdata_examples.rst:2237`-`:2260` の記載例を XLS→XLS・XLS→YAML→XLS で往復させて、テスティングフレームワークが読むレコードが保たれる | OK（**件数は 3 件でなく 2 件。下記「指示書との食い違い 1 件」参照**） | `XlsEmptyEntryTest#roundTripsDocumentedVariableFileExampleThroughXls`／`#roundTripsDocumentedVariableFileExampleThroughYaml`。記載例そのままの実 `.xlsx`（`SETUP_VARIABLE=input/data.csv`／`field-separator`,`,`／名前行 `DATA`,`USER_ID`,`USER_NAME`,`AMOUNT`／型行 `半角`,`全角`,`半角`／データ行 `001`,`山田太郎`,`5000` と `""`,(空),(空)）を起点に、両経路とも `[[001, 山田太郎, 5000], [, , ]]` が保たれる |
| `""` だけからなるテーブル・`LIST_MAP` のエントリが XLS→XLS で保たれる（本体 `PoiXlsReader` が読む件数と一致する） | OK | `#roundTripsAllEmptyTableEntryThroughXls`（往復後に本体 `TestCoreReaderAdapter#readTables` が 2 件・converter も 2 件）／`#roundTripsAllEmptyListMapEntryThroughXls`（同 `#readListMap` が 2 件・converter も 2 件） |
| `isEmptyCell` の変更で期待値が動いた既存テストが、変えた／変えなかったの判断つきで全件・件数つきで挙がっている | OK | **変えた 0 件 ／ 変えなかった 3 件**。詳細は下表。着手前 614 件のうち既存 614 件はすべて無変更で、`mvn` の結果（赤 5 件）も着手前と同一 |
| 直す前は落ちて直したあとは通るテストが挙がっている | OK | 下表「赤→緑」。是正前は新規 12 件中 10 件が赤（9 Failures・1 Error）。是正後は 12 件緑 |

## ゲート（Rules「#30 以降」）

- `git diff --stat 0ff8462..作業ツリー -- src/`:

  ```
  src/main/.../xls/XlsFormatReader.java | 23 +++++---
  src/main/.../xls/XlsFormatWriter.java | 66 ++++++++++++++++++++--
  2 files changed, 75 insertions(+), 14 deletions(-)
  ```

  加えて新規 1 ファイル `src/test/.../xls/XlsEmptyEntryTest.java`（12 メソッド）。
  **既存テストファイルは 1 つも変更していない。**

- `mvn -o clean test` の最終行:

  ```
  Tests run: 626, Failures: 5, Errors: 0, Skipped: 2
  BUILD FAILURE
  ```

  **緑ではない。** 落ちている 5 件は**着手前から赤い 5 件と同一**であり（`checks/step4-report.md` §0。
  `YamlTestCoreAdapterTest#isResourceExisting_reflectsFileExistence`／
  `YamlFormatReaderInvalidInputTest#dropsAllRowsWhenFirstRowOfTableIsEmptyObject`／
  `#keepsRowCountButLosesValuesWhenFirstRowOfListMapIsEmptyObject`／
  `YamlFormatReaderScalarTest#readsEmptyStringAsIs`／`#readsEmptyStringAsIsInListMapPath`）、
  **#36（2-5〜2-7）の担当**である。#33 が新たに落としたものは 0 件。着手前 614 件 → 626 件（+12。すべて新規）。

## 是正の内訳（2 点）

| 箇所 | 変更 |
|---|---|
| 読み —— `XlsFormatReader#isEmptyCell` | `cell == null \|\| cell.isEmpty()` だけを空とする。**引用符だけの記法（半角 `""`／全角 `””`）を空セル扱いするのをやめた。** 読み飛ばしの対象は `implementation/testdata_notation.rst:1500`（`5783b35` 時点）「Excel では行の全セルが空の場合」であり、`""` は空文字を表す**記法**であって空セルではない。テーブル・`LIST_MAP` の両経路（`dropEmptyEntries` → `isEmptyEntry` → `isEmptyCell`）に効く |
| 書き —— `XlsFormatWriter#entryCells`（新設） | テーブル・`LIST_MAP` のエントリを版面へ写す際、**全要素が空文字のエントリは各セルへ `""` を書く**。一部だけ空文字のエントリは空セルのまま |
| 書き —— `XlsFormatWriter#appendRecord` | ファイル・メッセージのデータ行で、**全フィールドが空文字の行は先頭フィールドへ `""` を書く**（`implementation/testdata_examples.rst:2231`「いずれか1つのフィールドに `""` と記述する」）。送信系の列 0（連番）は従来どおり |
| 補助 —— `XlsFormatWriter#isAllBlank`（新設）／定数 `EMPTY_STRING_NOTATION` | 上 2 つの判定と記法の定数 |

## 赤→緑（是正前に落ち、是正後に通る）

新規 `XlsEmptyEntryTest` を先に書いて `src/main` 未変更の状態で実行した実測（`Tests run: 12, Failures: 9, Errors: 1`）。

| テスト | 是正前 | 是正後 |
|---|---|---|
| `#keepsTableEntryWhoseCellsAreAllQuotedEmptyString` | FAIL（件数 2 に対し 1） | PASS |
| `#keepsListMapEntryWhoseCellsAreAllQuotedEmptyString` | FAIL（同上） | PASS |
| `#readsAsManyTableEntriesAsTheFrameworkDoes` | FAIL（本体 2 件・converter 1 件） | PASS |
| `#roundTripsAllEmptyTableEntryThroughXls` | FAIL（往復後に本体が読む件数が 1） | PASS |
| `#roundTripsAllEmptyListMapEntryThroughXls` | FAIL（同上） | PASS |
| `#writesEveryCellOfAllEmptyTableEntryAsQuotedEmptyString` | FAIL（`["", ""]` に対し `[, ]`） | PASS |
| `#writesEveryCellOfAllEmptyListMapEntryAsQuotedEmptyString` | FAIL（同上） | PASS |
| `#writesFirstFieldOfAllEmptyFileDataRowAsQuotedEmptyString` | FAIL（`[, "", , ]` に対し `[, , , ]`） | PASS |
| `#roundTripsDocumentedVariableFileExampleThroughXls` | FAIL（往復後にレコードが 1 件へ減る） | PASS |
| `#roundTripsDocumentedVariableFileExampleThroughYaml` | ERROR（往復後のブックが出力されない） | PASS |
| `#dropsTableEntryWhoseCellsAreAllBlank` | PASS | PASS（空セルの行は引き続き読み飛ばす番人） |
| `#writesPartiallyEmptyTableEntryWithBlankCells` | PASS | PASS（`""` を付けるのは全要素が空文字のときだけ、の番人） |

## 変異（期待値をわざと崩すと落ちること）

12 件すべて FAIL を確認した（`mvn -o clean test -Dtest=XlsEmptyEntryTest#<メソッド>`）。

| テスト | 崩した内容 | 結果 |
|---|---|---|
| `#keepsTableEntryWhoseCellsAreAllQuotedEmptyString` | 件数 2 → 1 | FAIL |
| `#keepsListMapEntryWhoseCellsAreAllQuotedEmptyString` | 件数 2 → 1 | FAIL |
| `#readsAsManyTableEntriesAsTheFrameworkDoes` | converter の件数 2 → 1 | FAIL |
| `#dropsTableEntryWhoseCellsAreAllBlank` | 期待に空セル行 `["", ""]` を足す | FAIL |
| `#writesEveryCellOfAllEmptyTableEntryAsQuotedEmptyString` | 2 セル目の期待を `""` → 空文字 | FAIL |
| `#writesEveryCellOfAllEmptyListMapEntryAsQuotedEmptyString` | 両セルの期待を `""` → 空文字 | FAIL |
| `#writesPartiallyEmptyTableEntryWithBlankCells` | 空文字セルの期待を `""` へ | FAIL |
| `#writesFirstFieldOfAllEmptyFileDataRowAsQuotedEmptyString` | 2・3 フィールド目の期待を空文字 → `""` | FAIL |
| `#roundTripsAllEmptyTableEntryThroughXls` | 本体件数 2 → 1 | FAIL |
| `#roundTripsAllEmptyListMapEntryThroughXls` | 本体件数 2 → 1 | FAIL |
| `#roundTripsDocumentedVariableFileExampleThroughXls` | 期待レコードを 1 件へ | FAIL |
| `#roundTripsDocumentedVariableFileExampleThroughYaml` | 期待レコードを 1 件へ | FAIL |

## 既存テストの変更（0 件）

**期待値を変えた既存テスト: 0 件。**着手前特定（`steering.md` #33「着手前特定の結果」）のとおりであった。

| 変えなかった 3 件 | 変えなかった理由 |
|---|---|
| `XlsFormatReaderTest#readTableNormalizesExcelQuotationNotation` | 同じ行に空でないセル（`"abc"`・`${expr}`）があり、変更前後とも空エントリではない |
| `XlsFormatReaderTest#readListMapNormalizesExcelQuotationNotation` | 同じ行に `"val"` があり、同上 |
| `XlsFormatReaderTest#readFixedFileNormalizesExcelQuotationNotation` | ファイルのデータ行は空エントリ判定を通らない（`readDataRows` は器の値行数ぶんを位置で読む）。加えて同じ行に `"x"` がある |

`XlsFormatReaderCellTypeTest` の `KEY` 列は**残した**（ユーザー確定・2026-08-27）。回避の理由は
converter の `isEmptyCell` ではなく本体側にあり、`PoiXlsReader.java:93` が `:140`-`:147` の
`isBlankLine` で**生セルが空の行**を捨てるため、`isEmptyCell` を直しても不要にならない。
同クラスに `""` を使うケースは 0 件。

## 台帳の更新

| 文書 | 更新 |
|---|---|
| `coverage/issues.md` | **XLS-05** に追記 —— 表の入力（`""`,`""`）についての原因の帰属が誤りであったこと（落としていたのは本体 `PoiXlsReader#isBlankLine` ではなく converter の `isEmptyCell`）と、#33 で解消したこと。**本課題として残るのは「全セルが空セルの行」だけ**であり判定欄は**対応不要**のまま。`KEY` 列の回避が引き続き必要であることも明記した。記録の書き換えはしていない（追記のみ） |

**未実施（範囲外として上げる）**: 本タスクで新設した `XlsEmptyEntryTest`（12 件）は
`inventory.md` の軸要素対応表へ載せていない。#32 の `XlsNotationSymmetryTest`（8 件）と同じ扱いで、
軸要素の割り当ては #37（完了条件3）が扱う母集合と重なるためである。#37 の Steps へ両クラスを書いた。

## 指示書との食い違い 1 件（実測で訂正）

**記載例（`testdata_examples.rst:2237`-`:2260`）のレコードは 3 件でなく 2 件である。**

指示書 2-2 および `steering.md` #33 の Purpose は「原本 3 件・XLS→XLS 後 2 件・XLS→YAML→XLS 後 2 件」と
述べているが、`5783b35` の当該 list-table は 6 行から成り、内訳は識別行（`:2237`-`:2240`）／
ディレクティブ行（`:2241`-`:2244`）／名前行（`:2245`-`:2248`）／型行（`:2249`-`:2252`）／
データ行 2 件（`:2253`-`:2256` の `001`,`山田太郎`,`5000` と `:2257`-`:2260` の `""`,(空),(空)）である。
記載例そのままの実 `.xlsx` を組んで読むと**原本 2 件**、是正前の XLS→XLS 後は **1 件**（1 件消える）、
是正後は両経路とも **2 件**になる（`#roundTripsDocumentedVariableFileExampleThroughXls` の
「原本」アサートが是正前から緑であったことが、原本 2 件の実測である）。
**「往復させるとレコードが 1 件消える」という症状の記述そのものは正しい。**件数だけが違う。

## 付随して見つけたこと（直していない）

- **XLS→YAML→XLS でも記載例のレコードは失われない。** `testdata_notation.rst:1500` の
  「すべての値が空文字の要素はスキップ」は**テーブルデータと `LIST_MAP` のエントリ**を対象とし、
  ファイルデータのレコードは対象外であるため（`#roundTripsDocumentedVariableFileExampleThroughYaml` で実測）。
  #37（完了条件3）で `@Ignore` にする対象は、**テーブル・`LIST_MAP` のエントリが XLS→YAML で落ちる件**である。
- **`XlsFormatReader#dropEmptyEntries` の Javadoc（`:607`）と `XlsFormatReaderRealFileTest:365` の
  「記法は 2 つの規則の前後関係を定めていない」は、現行の解説書に照らすと成り立たない。**
  `5783b35` の `implementation/testdata_notation.rst:1500` は「この判定はマーカーカラムを除外する
  **前**に行われる」と明記している。この食い違いは XLS-08 として `issues.md` に決着済みであり
  （往復の結果としては解説書と食い違わない。ユーザー確定・2026-08-27）、**振る舞いは直さない**。
  残っているのは Javadoc・テスト Javadoc の文言 2 か所だけである。
  **#33 の作業内容を 2 点に限る指示（ユーザー・2026-08-27）に従い、本タスクでは直していない。**

## Overall Verdict

- Self-check: OK
- QA: N/A（Rules「#30 以降、レビュア subagent は回さない」）
- Design expert: N/A（同上）
- Craft expert: N/A（同上）
- Verification expert: N/A（同上）
- Ready to check off: Yes（`mvn` の赤 5 件は #36 の担当であり、#33 が落としたものは 0 件）
