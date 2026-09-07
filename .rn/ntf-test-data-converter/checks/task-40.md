# #40 Completion Check

指示書 第2回 2-1（`nablarch-document@a12fb67` の
`.rn/20260724-ntf-yaml-support/ntf-step4-07-nablarch-testing-converter-2.md`）と、同 §8「#40 で直すこと」の 5 件。
参照点（ピン）: 解説書 `nablarch-document@a6da1f6` ／ `nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@3fecc4e`。

**レビュア subagent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| 直す前は落ちて直したあとは通るテストがある | OK | `XlsTrailingNullTest` 5 件。うち送信同期の 1 件は本タスクの `src/main` 修正（下記「配線の取り残し」）まで赤だった |
| (a)〜(e) の表が報告に載っている | OK | `checks/step4-2-report.md` §1（`c10de5e`）。承認は指示書 §8 |
| 末尾 `null` の値が本体と一致する | OK | 5 形すべてで `FrameworkOracle`（本体）の値と一致 |
| `dropEmptyEntries` 系の**定義**が `src/main` に残っていない | OK | 下記「§8-5 の削除」 |

## ゲート（Rules「#30 以降」）

- `git diff --stat f81fa66..HEAD -- src/`:

  ```
  src/main/java/nablarch/test/core/reader/TestCoreReaderAdapter.java   |  19 +-
  src/main/java/nablarch/test/tool/converter/xls/XlsFormatReader.java  | 194 +---------
  src/test/java/nablarch/test/core/file/DataFileInspector.java         |  新規
  src/test/java/nablarch/test/core/messaging/MessagePoolInspector.java |  新規
  src/test/java/nablarch/test/core/reader/FrameworkOracle.java         |  新規
  src/test/java/nablarch/test/core/reader/TestCoreReaderAdapterTest.java   |  18 +-
  src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderTest.java  |  13 +-
  src/test/java/nablarch/test/tool/converter/xls/XlsTrailingNullTest.java  |  新規
  ```

  （実測値はコミット後の `git diff --stat` を正とする）

- `mvn -o clean test` の最終行:

  ```
  Tests run: 670, Failures: 6, Errors: 1, Skipped: 0
  BUILD FAILURE
  ```

  **緑ではない。** 残る赤 7 件はいずれも後続タスクが直す対象で、内訳は次のとおり。
  着手時の赤 9 件から、本タスクで (e) 1・2 の 2 件が緑になった。

  | 赤 | 直すタスク |
  |---|---|
  | `XlsFormatReaderRealFileTest#dropsMarkerOnlyRowsAsEmptyEntriesInRealBook` | #41（2-2） |
  | `XlsFormatReaderRealFileTest#dropsMarkerOnlyRowsAsEmptyEntriesInListMapInRealBook` | #41（2-2） |
  | `XlsReferenceFixtureTest#readsExcelSavedWorkbookIntoIntermediateModel` | #41（2-2） |
  | `YamlFormatReaderInvalidInputTest#fillsMissingRecordFragmentValuesWithEmptyStringInsteadOfNull` | #43（2-4） |
  | `YamlFormatReaderScalarTest#readsUnquotedNullAsJavaNullInRecordFragmentPath` | #43（2-4） |
  | `YamlFormatReaderScalarTest#skipsRowWhoseValuesAreAllEmpty` | #43（2-4） |
  | `YamlFormatReaderRealFileTest#keepsFwHeaderNamedRecordInSendSyncFromRealYaml` | #43（2-4） |

  実行件数 670 には、追跡していない測定用の一時テスト 9 件（`ZzOracleProbeTest` ／ `ZzProbeTest`）を含む。
  追跡対象は 661 件。

## §8「#40 で直すこと」のうち本タスクで実施した分

### §8-5 死んだコードの削除

指示書が挙げた 8 か所のうち、**7 か所を削除した。`tail` は削除していない —— まだ使われているためである。**

```
$ grep -n 'tail(' src/main/java/nablarch/test/tool/converter/xls/XlsFormatReader.java
344:        while (idx < bodyLines.size() && !tail(bodyLines.get(idx)).equals(firstNames)) {
365:        if (idx >= bodyLines.size() || !tail(bodyLines.get(idx)).equals(names)) {
391:        List<String> originalTypes = tail(requireLine(bodyLines, idx, names, "型行"));
395:            originalLengths = tail(requireLine(bodyLines, idx, names, "長さ行"));
```

生行から原文（型行・長さ行・名前行）を復元する経路が先頭セルを落とすのに使っている。
指示書が根拠にした `git grep -nE 'private .*\(...\)\('` は**定義だけ**を探す式で、呼び出しの有無は見ていない。

| 対象 | 扱い |
|---|---|
| `XlsFormatReader#interpretValue` | 削除 |
| `XlsFormatReader#interpretRows` | 削除 |
| `XlsFormatReader#stripQuotes` | 削除 |
| `XlsFormatReader#dropEmptyEntries` | 削除 |
| `XlsFormatReader#isEmptyEntry` | 削除 |
| `XlsFormatReader#isEmptyCell` | 削除 |
| `XlsFormatReader#isQuotationWrapped` | 削除（参照が無くなっていた） |
| `XlsFormatReader` の `QUOTATION_TRIMMER` ／ `NULL_INTERPRETER` ／ `LINE_SEPARATOR_INTERPRETER` と 4 つの import | 削除（上記の削除で参照が無くなった） |
| `XlsFormatReader#tail` | **残す**（上記のとおり 4 か所から呼ばれている） |
| `TestCoreReaderAdapter` の `EXPERIMENT:` コメント | 書き直し（実験ではなくなったため、何を配線しているかを述べる文へ） |

削除に伴い、成り立たなくなったコメント 2 か所も書き直した。

- `XlsFormatReader` クラス Javadoc「本クラスは中間モデルへ入れる前に `interpretValue` で……解釈する」
  → 解釈するのは本体であり、器から取り出した時点で解釈後の値になっている、という記述へ
- `readMessageBlock` の「本体が生文字列として返すため `stripQuotes` は適用しない」
  → 本体が解釈後の値を返すため変換器側の加工は要らない、という記述へ（報告 §1 (b) の b-1）

### 配線の取り残し（本タスクで見つけて直した）

**`TestCoreReaderAdapter.SendSyncBodyCollector` が `super(reader, EMPTY_INTERPRETERS, targetType)` のままだった。**
送信同期電文だけがセルを解釈しない経路に残り、`XlsTrailingNullTest#readsTrailingNullsAsEmptyStringInSendSyncMessage`
が本体（`x`,`""`,`""`）に対し `x`,null,null を返して落ちた。

セルを解釈するのは**読み手であるテンプレート側**（`TestDataParsingTemplate#readTestData` の `interpret`）であって、
`onReadLine` 以降しか呼ばれない委譲先ではない。委譲先に `INTERPRETERS` を渡しても効かない。
`super` を `INTERPRETERS` に改めた。

### (e) 1・2 の期待値の直し

| # | テスト | 直した内容 |
|---|---|---|
| 1 | `TestCoreReaderAdapterTest#readTablesReturnsRawTableData` → `#readTablesReturnsValuesInterpretedByFramework` | フィクスチャのセル Java `null` を `null` 記法・引用符記法へ差し替え、期待値を解釈後の値（引用符記法 → 外側 1 層が外れる／`null` 記法 → Java `null`／`${...}` は記法のまま）へ改めた。実 `PoiXlsReader` は空セルを空文字で返すため、Java `null` のセルは実在しない |
| 2 | `XlsFormatReaderTest#readMapsTableBlockPreservingRawValues` → `#readMapsTableBlockWithFrameworkInterpretedValues` | 同上。テスト名・Javadoc の「記法のまま（未加工）」も実態に合わせた |

**(e) 3〜5 は指示書 §8 のとおり期待値を変えていない。**#41 で直す。

## 完了条件4（期待値をわざと崩すと落ちること）

足した 5 件・直した 2 件のそれぞれについて 1 回ずつ確認した（計 7 件）。
崩した状態でそのテストだけを実行し、`Failures: 1` になることを確認してから元へ戻した。

| # | テスト | 崩した内容 | 結果 |
|---|---|---|---|
| 1 | `XlsTrailingNullTest#readsTrailingNullsAsEmptyStringInFixedFile` | 本体の値の期待を `[x, "", ""]` → `[x, "MUT", ""]` | Failures: 1 |
| 2 | `XlsTrailingNullTest#readsAllNullRowAsAllEmptyStringsInFixedFile` | 同 `["", "", ""]` → `["MUT", "", ""]` | Failures: 1 |
| 3 | `XlsTrailingNullTest#readsTrailingNullAfterQuotedEmptyAsEmptyStringInFixedFile` | 同 `[x, "", ""]` → `[x, "", "MUT"]` | Failures: 1 |
| 4 | `XlsTrailingNullTest#readsTrailingNullsAsEmptyStringInMessage` | 同 `[x, "", ""]` → `[x, "", "MUT"]` | Failures: 1 |
| 5 | `XlsTrailingNullTest#readsTrailingNullsAsEmptyStringInSendSyncMessage` | 同 `[x, "", ""]` → `[x, "MUT", ""]` | Failures: 1 |
| 6 | `TestCoreReaderAdapterTest#readTablesReturnsValuesInterpretedByFramework` | `quoted` → `MUT` | Failures: 1 |
| 7 | `XlsFormatReaderTest#readMapsTableBlockWithFrameworkInterpretedValues` | `nullValue()` → `"MUT"` | Failures: 1 |

## 台帳の更新

- `coverage/inventory.md` §0.1-2 に**追補その 13**（件数の導き直し。605 → 661）
- 同 **§4.6**（第2回が新設したテストクラスと、テスト専用の相乗りクラス 3 本）
