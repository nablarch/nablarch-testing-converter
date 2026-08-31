# task-50 Completion Check

辺① —— カラム名の行がマーカーカラムだけのブロックを、名前と値を保って読む。

## 1. Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| マーカーカラムだけのテーブル・`LIST_MAP` について、中間モデルのカラム名がマーカーカラム名と一致し、行数・値が本体 `BasicTestDataParser` の読みと一致する | OK | `XlsMarkerOnlyBlockTest#keepsMarkerOnlyColumnsAndValuesInTable`／`#keepsMarkerOnlyColumnsAndValuesInListMap`。件数の正解は `FrameworkOracle.setupTablesViaTestDataParser`／`#listMapViaTestDataParser`（`BasicTestDataParser` 経由）から取る |
| 実データカラムを持つブロックのマーカーカラムは従来どおりカラム名から落ちる | OK | `XlsMarkerOnlyEntryTest` 2 件が変更なしで緑（期待値も本体から取っている） |
| 直した既存テストが全件列挙されている | OK | 下の §3 |
| `mvn -o clean test` 全件緑・`@Ignore` 0 件・`git status --short` 空 | OK | `Tests run: 714, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS`。`@Ignore` は Javadoc 中の 4 か所だけでアノテーションは 0 件（`git grep -n "@Ignore" -- src/`） |

## 2. 変更の中身

| ファイル | 変更 |
|---|---|
| `src/main/.../core/reader/MarkerOnlyBlock.java`（新設） | マーカーカラムだけのブロックの版面（カラム名と各行の値）を運ぶ値クラス |
| `src/main/.../core/reader/TestCoreReaderAdapter.java` | `readMarkerOnlyBlock` を追加。`BodyLineCollector` にインタープリタ列・行末トリムの有無を渡せるようにし、識別子の比較を本体の器に合わせて正規化する（テーブル系だけ trim ＋ 大文字化） |
| `src/main/.../tool/converter/xls/XlsFormatReader.java` | 行を落としていた `rowCount` を削除し、マーカー除外後のカラム名が 0 件のときだけ版面を使う `markerOnlyBlock` を追加 |
| `src/test/.../core/reader/FrameworkOracle.java` | `BasicTestDataParser`（実行時と同じ公開 API）経由の正解取り出しを 2 つ追加 |
| `src/test/.../xls/XlsMarkerOnlyBlockTest.java`（新設） | 本タスクの担保 4 件 |

### 設計上の要点

**マーカーカラムの値も「本体が解釈したあとの値」である。** 本体は行の解釈を、マーカーカラムを除外するより
**前**に行の全セルへ掛ける（`nablarch-testing@dcaed44` の
`src/main/java/nablarch/test/core/reader/TestDataParsingTemplate.java:183` が `readTestData` の中で
`interpret(line)` を呼び、`HeaderLine` による除外は `onReadLine` 以降）。したがってマーカーカラムの値も
他のカラムと同じ解釈を受けており、中間モデルが解釈後の値を持つという既存の取り決めがそのまま当てはまる。
**この結果、辺③（`XlsFormatWriter#toCellNotation`／`entryCells`）は 1 行も変えずに逆写像として働く。**
担保は `XlsMarkerOnlyBlockTest#interpretsMarkerOnlyCellValuesAsFrameworkDoes`。

**行末の空セルは詰めない。** `null` 記法は解釈の結果 Java `null` になり、本体の
`NablarchTestUtils#trimTail` はこれを空要素として落とす。代わりに本体 `HeaderLine#excludeMarkerColumns`
と同じく、足りない位置を空文字で埋めてカラム名と同じ要素数へ揃えている。

**識別子の正規化。** 本体 `TableData` はテーブル名を trim して大文字化する
（`nablarch-testing@dcaed44` の `src/main/java/nablarch/test/core/db/TableData.java:97`）。
器から取った識別子でシート上のマーカー行を引き当てるため、テーブル系だけ同じ正規化を掛ける。
担保は `XlsMarkerOnlyBlockTest#keepsMarkerOnlyColumnsInGroupedTableWithLowerCaseName`。

## 3. 旧仕様を期待していた既存テストの変更（全件）

| # | テスト | 変更 |
|---|---|---|
| 1 | `XlsFormatReaderRealFileTest#dropsMarkerOnlyRowsAsEmptyEntriesInRealBook` | `#keepsMarkerOnlyColumnAndValuesInRealBook` へ改名。期待を「列名 0 件・行 0 件」→「`[no]` 1 列・行 2 件」へ。Javadoc も新仕様へ |
| 2 | `XlsFormatReaderRealFileTest#dropsMarkerOnlyRowsAsEmptyEntriesInListMapInRealBook` | `#keepsMarkerOnlyColumnAndValuesInListMapInRealBook` へ改名。同上（行 1 件） |
| 3 | `XlsFormatWriterTest#roundTripsZeroRowTableWithoutEatingNextBlock` | 読み戻したカラム名の期待を「0 件」→「`[EMPTY]` 1 件」へ。行 0 件は変えていない |
| 4 | `XlsFormatWriterTest#roundTripsZeroRowListMapWithoutEatingNextBlock` | 同上 |
| 5 | `XlsReferenceFixtureTest#readsExcelSavedWorkbookIntoIntermediateModel` | `expectedRequestParams()` を「列名 0 件・行 0 件」→「`[no]` 1 列・行 `["1.0"]`」へ。値が `"1.0"` なのは当該セルが表示形式 `@` 付きの数値セルであるため（`issues.md` XLS-01。実挙動の記録は `XlsFormatReaderCellTypeTest#readsTextFormattedNumericCellAsDoubleString`） |

**Javadoc だけを直したもの（期待値は変えていない）**

| # | 箇所 | 変更 |
|---|---|---|
| 6 | `XlsMarkerOnlyEntryTest` クラス Javadoc | 「マーカーカラムだけのブロックはデータ行を持たず、別の定めが当たる」→ 新仕様の説明と `XlsMarkerOnlyBlockTest` への参照へ |
| 7 | `XlsFormatReaderInvalidInputTest#readsMarkerColumnWithoutBracketsAsOrdinaryDataColumnInRealBook` | 参照先を改名後のメソッドではなく `XlsMarkerOnlyEntryTest#keepsMarkerOnlyEntryInListMapAsFrameworkDoes` へ |
| 8 | `XlsFormatReaderRealFileTest` クラス Javadoc（C-08 の段落） | 参照メソッド名を改名後へ |

## 4. 台帳の更新

| ファイル | 変更 |
|---|---|
| `coverage/issues.md` | XLS-08 に【2026-08-31・#50】を追記して決着させた。旧表は時点の記録として残し、現在の値は追記側が正であることを冒頭で示す |
| `coverage/axis-matrix.md` | 辺① C-08(空) を ✅ → `—`（到達不能）へ。§5.2 の辺① ✅ 70→69／`—` 8→9・合計 284→283／27→28、§5.3 を 28 件へ（分類 1 行追加）、§7 柱書の「4 行とも ✅」を訂正、§0.1・§5.2 の「🔺 往復欄が `—` でない行」96→95 |

**§0.6 の導出コマンドを流し直した結果**（リポジトリルートから実行）:

```
辺1: 78  辺2: 81  辺3: 76  辺4: 76
辺1 ✅=69 —=9 ／ 辺2 ✅=73 —=8 ／ 辺3 ✅=71 —=5 ／ 辺4 ✅=70 —=6
③ 🔺往復欄が「該当なし」でない行数: 95
⑤ 全行数: 311
⑥ 太字の残り: C-06(値あり)×4 ／ D1-16 ／ F1-03 ／ D3-04（**HEAD と同じ 7 件。本タスクでは増やしていない**）
担保テストメソッドの実在確認: 出力なし（全件実在）
```

`inventory.md` のテストメソッド件数（追補）は、#54（締め）でまとめて導き直す。

## 5. 指示書の禁止事項

| 事項 | 結果 |
|---|---|
| 解説書・本体・yaml・integration を変更しない | OK（`git status --short` は本リポジトリのみ。他リポジトリは読み取りだけ） |
| ソース・記録に解説書への参照を書かない | OK。`git grep -nE '\.rst\|nablarch-document\|解説書' -- src/` → 0 件、`git grep -nE '[A-Za-z]+\.java:[0-9]+' -- src` → 0 件 |
| force push・`--amend` をしない | OK |

## Overall Verdict

- Self-check: OK
- Ready to check off: Yes
