# #44 Completion Check

指示書 第2回 2-5（`nablarch-document@a12fb67`）。
参照点（ピン）: 解説書 `nablarch-document@a6da1f6` ／ `nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@3fecc4e`。

**レビュア subagent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| 正解が本体であることがテストから分かる | OK | `readXls` ／ `readYaml` と 11 個の「正解の読み手」がいずれも `FrameworkOracle`（本体）／`YamlFrameworkOracle`（yaml）だけを使う。変換ツールのリーダは<b>突き合わせられる側</b>としてのみ呼ぶ |
| 母集合に 4 種が入っている | OK | 下表（6 テスト） |
| 末尾 `null` の 1 件以上が #40 の前は落ちる | OK | 下記「#40 の前は落ちること」。**3 件が落ちた** |
| 全経路が緑 | OK | `SpecialNotationRoundTripTest` 26 件が緑 |

## ゲート（Rules「#30 以降」）

- `git diff --stat f858ae5..HEAD -- src/`（実測値はコミット後の `git diff --stat` を正とする）:

  ```
  src/test/java/nablarch/test/core/reader/FrameworkOracle.java                    | 変更（BinaryFileInterpreter を積む）
  src/test/java/nablarch/test/core/reader/YamlFrameworkOracle.java                | 変更（インタープリタ列を空へ・expectedTables を追加）
  src/test/java/nablarch/test/tool/converter/xls/SpecialNotationRoundTripTest.java | 変更（正解の差し替え・母集合 ＋6）
  ```

  **`src/main` は 1 行も触っていない。**

- `mvn -o clean test` の最終行:

  ```
  Tests run: 687, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS
  ```

  実行件数 687 には、追跡していない測定用の一時テスト 9 件を含む。追跡対象は 678 件。

## 正解を本体にする

| 形式 | 正解の読み手 | インタープリタ列 |
|---|---|---|
| Excel | `FrameworkOracle`（`PoiXlsReader` ＋ 本体パーサ） | `BinaryFileInterpreter`（取得元パス起点）→ `null` 記法 → 引用符記法 → 改行記法 |
| YAML | `YamlFrameworkOracle`（`nablarch-testing-yaml` の `YamlTestDataParser`） | `BinaryFileInterpreter` のみ（パーサが自動で積む） |

**YAML 側に Excel 形式の記法を解釈するインタープリタを渡さないのは、解説書の定めによる。**
YAML 形式では、`null` と `"null"` の区別も、値を囲むダブルクォートの除去も、`"\r"` ／ `"\n"` の
制御文字への変換も YAML のパーサ自身が行う。前後のダブルクォート 1 層の除去は YAML 形式では行われない。
渡してしまうと `"null"` が Java `null` になり、`'"'`（ダブルクォート 1 文字）は引用符記法の解釈で例外になる
（実測。渡した状態で `stringNull` ／ `singleDoubleQuote` ／ `binaryFileNotation` ／
`exampleSpaceAndDoubleQuote` ／ `exampleBinaryData` が落ちた）。

**`BinaryFileInterpreter` は両側に積む。** YAML 側はパーサが自動で積むため外せない。Excel 側だけ積まないと
「Excel 形式の例と YAML 形式の例が同じ値を表す」が成り立たなくなる。両側に積んだうえで、
各段のディレクトリへ同じ内容の `testdata.bin`（3 バイト）を置いた（`placeBinaryFixture`）。

### 変換ツールの読みを正解と突き合わせる

**往復だけでは末尾 `null` の欠陥を検知できない。** 変換ツールが `x,null,null` を `x,null,null` と読み、
書き戻すときも `null` 記法へ戻すため、往復の前後でフレームワークが読む値は変わらないからである
（実測。この突き合わせを入れる前は、`d611bec` でも往復 26 件のうち落ちたのは
`markerOnlyEntryInListMap` 1 件だけだった）。

そこで、往復に先立って**変換ツールが読んだ中間モデルの値をフレームワークが読んだ値と突き合わせる**
（`converterReadXls` ／ `converterReadYaml` ／ `converterValue`）。これが「正解を本体にする」の実体である。

**`${binaryFile:パス}` を含む 2 件（`binaryFileNotation` ／ `exampleBinaryData`）はこの突き合わせを行わない。**
変換ツールはこの記法を解決せず記法のまま運ぶ仕様であり（`${...}` 系のインタープリタを渡さない）、
フレームワークが解決した値とは一致しないためである。往復の担保は残っている。

## 母集合に足した 4 種

| 種 | テスト | 内容 |
|---|---|---|
| 末尾 `null` | `#trailingNullInFixedFile` | 固定長ファイルの 3 行 —— `x,null,null` ／ `null,null,null` ／ `x,"",null`（2-1 実測表の F1・F4・F6） |
| 末尾 `null` | `#trailingNullInMessage` | 電文の 1 行 —— `x,null,null`（同 M1） |
| 全値 `""` | `#allEmptyStringEntryInTable` ／ `#allEmptyStringEntryInListMap` | 全カラムの値が空文字のエントリ。第1回はガードのカラムを置いていたため母集合に無かった |
| マーカーカラムだけに値がある | `#markerOnlyEntryInListMap` | `[no]` ＋ 2 カラム。2 行目はマーカーカラムだけに値がある |
| アップロードファイルの記載例 | `#exampleUploadFile` | `LIST_MAP` ＋ `[no]` ＋ `${attach:パス}` |

**送信同期電文（2-1 実測表の S2）は 4 経路の母集合へは入れていない。** 4 経路のうち YAML→XLS→YAML は
送信同期の識別子とグループ ID の対応を要し、母集合の 1 件として組むより、直接の突き合わせ
（`XlsTrailingNullTest#readsTrailingNullsAsEmptyStringInSendSyncMessage`。#40 で追加）で押さえるほうが
確かめたいこと（本体と値が一致すること）に近い。**S2 の担保はそちらにある。**

## #40 の前は落ちること（完了条件1）

指示書のピン **`d611bec`** を worktree に出し、本タスク後の `SpecialNotationRoundTripTest` と
正解の読み手 2 本を持ち込んで実行した。

```
$ git worktree add --detach <dir> d611bec
$ cp <FrameworkOracle / YamlFrameworkOracle / DataFileInspector / MessagePoolInspector /
      SpecialNotationRoundTripTest> <dir>/...
$ cd <dir> && JAVA_HOME=... mvn -o test -Djacoco.skip=true -Dtest=SpecialNotationRoundTripTest

Tests run: 26, Failures: 3, Errors: 0, Skipped: 0
  SpecialNotationRoundTripTest.markerOnlyEntryInListMap   変換ツールの Excel 読みが本体と一致する
  SpecialNotationRoundTripTest.trailingNullInFixedFile    変換ツールの Excel 読みが本体と一致する
  SpecialNotationRoundTripTest.trailingNullInMessage      変換ツールの Excel 読みが本体と一致する
```

**末尾 `null` の 2 件が落ちた**（要件は 1 件以上）。3 件目はマーカーカラムだけに値があるエントリ（2-2）である。

## 完了条件4（期待値をわざと崩すと落ちること）

足した 6 件それぞれと、期待値を変えた既存 1 件の裏づけとして既存 1 件を、計 7 件確認した。

| # | テスト | 崩した内容 | 結果 |
|---|---|---|---|
| 1 | `#trailingNullInFixedFile` | YAML 側の 2 行目を `[null, null, null]` → `[null, null, "z"]` | Failures: 1 |
| 2 | `#trailingNullInMessage` | YAML 側の値を `["x", null, null]` → `["x", null, "z"]` | Failures: 1 |
| 3 | `#allEmptyStringEntryInTable` | YAML 側の `K: ""` → `K: "z"` | Failures: 1 |
| 4 | `#allEmptyStringEntryInListMap` | 同上 | Failures: 1 |
| 5 | `#markerOnlyEntryInListMap` | YAML 側の 2 行目 `K: ""` → `K: "z"` | Failures: 1 |
| 6 | `#exampleUploadFile` | YAML 側の `${attach:...}` のパスを変える | Failures: 1 |
| 7 | `#javaNull`（既存 12 件の 1 つ） | 期待を `null` → `""` | Failures: 1 |

## 台帳の更新

- `coverage/inventory.md` §0.1-2 に**追補その 17**（672 → 678）
- 同 §4.5 の `SpecialNotationRoundTripTest` の件数（20 → 26）と、正解を差し替えたこと
- 同 §4.6 の「期待値の出どころを本体に移した」の対象に #44 を追加
