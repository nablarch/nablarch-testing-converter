# #41 Completion Check

指示書 第2回 2-2（`nablarch-document@a12fb67`）と、同 §8「#40 で直すこと」の 1・2・3・4。
参照点（ピン）: 解説書 `nablarch-document@a6da1f6` ／ `nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@3fecc4e`。

**レビュア subagent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| マーカーカラムだけに値があるエントリが本体と同じ件数で残る（他のカラムを持つブロック） | OK | `XlsMarkerOnlyEntryTest` 2 件。本体の 3 件と一致 |
| カラム名がマーカーカラムだけのブロックは行 0 件のまま（(e) 3〜5 の期待値が変わっていない） | OK | 3 件とも期待値を触っていない。書き直したのは assert メッセージ・Javadoc の文言だけ |
| 直す前は落ちて直したあとは通るテストがある | OK | 下記「直す前は落ちること」 |

## ゲート（Rules「#30 以降」）

- `git diff --stat 4418726..HEAD -- src/`（実測値はコミット後の `git diff --stat` を正とする）:

  ```
  src/main/java/nablarch/test/tool/converter/xls/XlsFormatReader.java        |  +29 -3
  src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderRealFileTest.java |  +11 -13
  src/test/java/nablarch/test/tool/converter/xls/XlsMarkerOnlyEntryTest.java |  新規
  src/test/java/nablarch/test/tool/converter/xls/XlsReferenceFixtureTest.java |  +2 -2
  ```

- `mvn -o clean test` の最終行:

  ```
  Tests run: 672, Failures: 3, Errors: 1, Skipped: 0
  BUILD FAILURE
  ```

  **緑ではない。** 残る赤 4 件はすべて #43（2-4。yaml 第2回への追随）が直す対象である。
  #40 完了時の赤 7 件から、本タスクで (e) 3〜5 の 3 件が緑になった。

  | 赤 | 直すタスク |
  |---|---|
  | `YamlFormatReaderInvalidInputTest#fillsMissingRecordFragmentValuesWithEmptyStringInsteadOfNull` | #43 |
  | `YamlFormatReaderScalarTest#readsUnquotedNullAsJavaNullInRecordFragmentPath` | #43 |
  | `YamlFormatReaderScalarTest#skipsRowWhoseValuesAreAllEmpty` | #43 |
  | `YamlFormatReaderRealFileTest#keepsFwHeaderNamedRecordInSendSyncFromRealYaml` | #43 |

  実行件数 672 には、追跡していない測定用の一時テスト 9 件を含む。追跡対象は 663 件。

## §8「#40 で直すこと」のうち本タスクで実施した分

### §8-1 カラム名 0 件のブロックは行を持たせない（値は見ない）

`XlsFormatReader#rowCount(columnNames, rowCount)` を足し、テーブル系・`LIST_MAP` の両経路で
行数の上限に使う。カラム名が空なら 0 を返す。

**値を見る判定は入れていない。**（#40 で削除した `dropEmptyEntries` 系は復活させていない。）
値で落とすと、本体には届く行（全セルが `null` 記法の行・全セルが空文字記法の行）まで消える。

### §8-2 (e) 3〜5 の期待値は変えない

| # | テスト | 変えたもの |
|---|---|---|
| 3 | `XlsFormatReaderRealFileTest#dropsMarkerOnlyRowsAsEmptyEntriesInRealBook` | assert メッセージ「除外後は全要素が空のエントリになるため読み飛ばされる（XLS-08）」→「カラム名を 1 つも持たないブロックはデータ行を持たない」。Javadoc の仕組みの説明も同様に。**期待値（行 0 件）は変えていない** |
| 4 | `#dropsMarkerOnlyRowsAsEmptyEntriesInListMapInRealBook` | 同上 |
| 5 | `XlsReferenceFixtureTest#readsExcelSavedWorkbookIntoIntermediateModel`（`expectedRequestParams` の Javadoc） | 同上。**期待ブロック（列名 0 件・行 0 件）は変えていない** |

### §8-3 本体が読む件数と一致するテスト（他のカラムを持つブロックで組む）

`XlsMarkerOnlyEntryTest` を新設。入力はカラム行 `[no]`, `id`, `name`、データ行
`1,U0001,yamada` ／ `(空),"",""` ／ `3,(空),(空)` の 3 行。テーブル系と `LIST_MAP` の 2 経路。

期待値はテストが書かず `FrameworkOracle` が本体から取る。本体が読む件数（3）と値も明示している。

**テーブル系のカラム名は `ID` ／ `NAME` と大文字になる。** フレームワークがテーブルのカラム名を
大文字へ揃えるためで、本テストの主題ではない（既知の課題 XLS-40）。

### §8-4 台帳への明記

`coverage/issues.md` の XLS-08 に【2026-08-29・#41】として追記した ——
「XLS-08 の仕組みを上書きした。結果が変わるのは他のカラムを持つブロックだけで、
カラム名 0 件のブロックの結果（行 0 件）は変わらない」。上書きした理由 2 点も併記した。

## 直す前は落ちること（完了条件1）

指示書のピン **`d611bec`** を worktree に出し、#40・#41 で足したテスト 3 クラス（と、それが使う
テスト専用の相乗りクラス 3 本）だけを持ち込んで実行した。**7 件すべてが落ちた。**

```
$ git worktree add --detach <dir> d611bec
$ cp <新テスト 5 ファイル> <dir>/...
$ cd <dir> && JAVA_HOME=... mvn -o test -Djacoco.skip=true \
      -Dtest='XlsTrailingNullTest,XlsMarkerOnlyEntryTest'

Tests run: 7, Failures: 7, Errors: 0, Skipped: 0
  XlsMarkerOnlyEntryTest.keepsMarkerOnlyEntryInListMapAsFrameworkDoes:178   変換ツールの行が本体と一致する
  XlsMarkerOnlyEntryTest.keepsMarkerOnlyEntryInTableAsFrameworkDoes:147     変換ツールの行が本体と一致する
  XlsTrailingNullTest.readsAllNullRowAsAllEmptyStringsInFixedFile:178       変換ツールの値が本体と一致する
  XlsTrailingNullTest.readsTrailingNullAfterQuotedEmptyAsEmptyStringInFixedFile:199 同
  XlsTrailingNullTest.readsTrailingNullsAsEmptyStringInFixedFile:158        同
  XlsTrailingNullTest.readsTrailingNullsAsEmptyStringInMessage:228          同
  XlsTrailingNullTest.readsTrailingNullsAsEmptyStringInSendSyncMessage:256  同
```

落ちたのはいずれも「変換ツールの値が本体と一致する」の assert であり、
その直前の「本体の値」の assert は 7 件とも通っている。**本体の値は `d611bec` でも同じ**であり、
食い違っていたのは変換ツール側だけである、ということがこの実行から読める。

(e) 3〜5 の 3 件は、`b7d2320`（#40・#41 の `src/main` 変更が入り、§8-1 がまだ無い状態）で落ち、
§8-1 を入れると通る。#41 着手時点の `mvn -o clean test` がその状態であった（`checks/task-40.md` の赤 7 件）。

## 完了条件4（期待値をわざと崩すと落ちること）

足した 2 件と、文言を書き直した 3 件のそれぞれについて 1 回ずつ確認した（計 5 件）。

| # | テスト | 崩した内容 | 結果 |
|---|---|---|---|
| 1 | `XlsMarkerOnlyEntryTest#keepsMarkerOnlyEntryInTableAsFrameworkDoes` | 本体が読む行数の期待を 3 → 2 | Failures: 1 |
| 2 | `XlsMarkerOnlyEntryTest#keepsMarkerOnlyEntryInListMapAsFrameworkDoes` | 同 3 → 2 | Failures: 1 |
| 3 | `XlsFormatReaderRealFileTest#dropsMarkerOnlyRowsAsEmptyEntriesInRealBook` | 行の期待を 0 件 → 1 件（`[[]]`） | Failures: 1 |
| 4 | `#dropsMarkerOnlyRowsAsEmptyEntriesInListMapInRealBook` | 同 0 件 → 1 件 | Failures: 1 |
| 5 | `XlsReferenceFixtureTest#readsExcelSavedWorkbookIntoIntermediateModel` | `expectedRequestParams` の行を 0 件 → 1 件 | Failures: 1 |

## 台帳の更新

- `coverage/inventory.md` §0.1-2 に**追補その 14**（661 → 663）
- 同 §4.6 に `xls/XlsMarkerOnlyEntryTest` の行
- `coverage/issues.md` XLS-08 に【2026-08-29・#41】
