# #35 Completion Check

指示書 2-4（`nablarch-document@a16be0a` の `.rn/20260724-ntf-yaml-support/ntf-step4-05-nablarch-testing-converter.md`）。
参照点: 解説書 `nablarch-document@5783b35` ／ `nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@0b3015c`。

**レビュア subagent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| 2 件のテストメソッドと、その `{@link}` 参照が残っていない | OK | `grep -rn 'failsToReadRecordFragmentRowWithMoreValuesThanFields\|keepsOriginalColumnCaseInTable' src/` → 0 件。参照は `YamlFormatReaderInvalidInputTest:670`・`:1220`（現状テストの Javadoc）と `ColumnRowDataBlock.java:58`（`src/main`）の 3 か所にあり、すべて外した。`import org.junit.Ignore;` も未使用になったため外した |
| 他の `@Ignore` を消していない | OK | 着手前に `@Ignore` は 2 件しか無く、削除後は 0 件（`grep -rn '@Ignore' src/test --include=*.java` の残り 3 件はいずれも Javadoc 中の `{@code @Ignore}` という文字列）。`mvn -o clean test` の `Skipped` が **2 → 0** になったことが実測である |

## ゲート（Rules「#30 以降」）

- `git diff --stat 8774d0a..作業ツリー -- src/`:

  ```
  src/main/.../converter/model/ColumnRowDataBlock.java             |   8 +-
  src/test/.../yaml/YamlFormatReaderInvalidInputTest.java          | 121 +++-------------
  2 files changed, 17 insertions(+), 112 deletions(-)
  ```

- `mvn -o clean test` の最終行:

  ```
  Tests run: 624, Failures: 5, Errors: 0, Skipped: 0
  BUILD FAILURE
  ```

  **緑ではない。** 落ちている 5 件は**着手前から赤い 5 件と同一**であり（`checks/step4-report.md` §0）、
  **#36（2-5〜2-7）の担当**である。#35 が新たに落としたものは 0 件。
  626 件 → **624 件**（削除 2）、`Skipped` 2 → **0**。

## 削除した 2 件と、削除してよいと判断した根拠（実物で確認）

| テスト | 主張していた「あるべき姿」 | ピン `5783b35` の解説書に根拠が無いことの確認 |
|---|---|---|
| `YamlFormatReaderInvalidInputTest#failsToReadRecordFragmentRowWithMoreValuesThanFields` | フィールド数を超える値がある入力は読み込みが**エラーになる**（根拠にしていたのは `testdata_notation.rst:891`（`30a8271` 時点）の記述時エラー一覧「データ要素数が不正である」） | **記述時エラー一覧そのものが `5783b35` に存在しない。**同 `:891` はパディングとバイナリデータの記述である。`git grep -n '要素数' 5783b35 -- 'ja/development_tools/testing_framework'` → **1 件のみ**（`tools/testdata_converter.rst:47`「レコード定義のフィールド数と、各データ行の要素数が一致すること」）。ただしこれは `YamlTestDataValidator` の検査 7 項目の 1 つで、**同 `:55` が「検証は変換の処理経路には組み込まれておらず、変換の実行時に自動では呼び出されない」と明記している**。すなわち**読み込みでエラーにすることを定めた明文は無い** |
| `YamlFormatReaderInvalidInputTest#keepsOriginalColumnCaseInTable` | テーブル系のカラム名は原文の大小のまま残る | `git grep -n '大文字' 5783b35 -- 'ja/development_tools/testing_framework'` → **14 箇所**。うち `testdata_notation.rst:767`・`:1360` は値の `null` 表記が大文字小文字不問である話、`setup/class_unit_test.rst` の 7 件と `implementation/class_unit_test/entity.rst` の 2 件は Bean Validation の文字列長、`tools/html_check_tool.rst` の 2 件はタグ名・属性名、`tools/testdata_converter.rst:274` は列幅の最大文字数。**テーブルのカラム名の大小に触れるものは 0 件** |

**指示書の 2 件目の理由づけ（「全走査で記述は 0 件」）は実測と一致した。1 件目の理由づけ
（「`5783b35` の `:891` はパディングとバイナリデータの記述」）も一致したが、
それだけでは「行が移動しただけ」の可能性を排除できないため、`要素数` の全文検索まで行った。**
残った唯一の明文（`testdata_converter.rst:47`）は**変換の処理経路の外**であり、削除の判断は変わらない。

## 変えたもの・変えなかったもの

| 対象 | 変更 |
|---|---|
| `YamlFormatReaderInvalidInputTest#dropsRecordFragmentValuesBeyondFieldCount` の Javadoc | 「あるべき姿は `@Ignore` 側」と `notation:891` の引用を、現行の解説書に照らした記述（`testdata_converter.rst:47`・`:55`）へ書き直した。**テストの入力・期待値は変えていない** |
| `YamlFormatReaderInvalidInputTest#dropsValueWhenTableColumnNamesDifferOnlyByCase` の Javadoc | 同上（大小の記述が 0 件であることへ書き直した）。**入力・期待値は変えていない** |
| `ColumnRowDataBlock`（`src/main`）のクラス Javadoc | `#keepsOriginalColumnCaseInTable` への参照を外し、#35 で削除した旨へ書き直した。**コードは無変更** |
| `import org.junit.Ignore;` | 未使用になったため削除 |
| 上記以外の全テスト | 変えていない |

## 台帳の更新

| 文書 | 更新 |
|---|---|
| `coverage/issues.md` | **XLS-40**（カラム名側）と **YML-14** に、`@Ignore` つきテストを #35 で削除した旨と、その根拠（全走査の結果と出典）を追記。**どちらも判定欄は変えていない**（XLS-40 は保留、YML-14 は対応不要）。`@Ignore` を外したときの FAIL 記録はそのまま残した |
| `coverage/coverage-report.md` | 「待機テスト（`@Ignore`）2 件」の表の直後に、#35 で 2 件とも削除し `Skipped` が 0 になった旨を引用ブロックで追記。**数値・行番号は `da66425` 時点のまま**（Rules「JaCoCo の再計測はしない」） |
| `handover.md` | 3（カラム名の大小）と 4（YML-14）の「converter側の現状」から `@Ignore` の記述を外し、削除した旨と、代わりに現状固定テストが残っていることを書いた。**申し送りの本文（依頼内容）は変えていない** |
| `coverage/inventory.md` | §2.1-2 の `YamlFormatReaderInvalidInputTest` の宣言値を導き直した（下記） |

### 宣言値の導き直し（Rules「テストメソッドを増減させたら…コマンドから導き直す」）

`inventory.md` のすべての `grep -c '^    @Test' <ファイル>` 形式の宣言値を実行して突き合わせた結果、
**現行の宣言と食い違っていたのは 1 か所だけ**であった。

| 場所 | 旧宣言値 | 実測値 | 出所 |
|---|---:|---:|---|
| `inventory.md` §2.1-2 の `YamlFormatReaderInvalidInputTest` | 31 | **32** | 31 は `226d0f8` 時点。以後 `d737815` ＋1（XLS-40 の `@Ignore`）／`b19a236` ＋1（YML-14 の `@Ignore`）／`96b5aea`（#31）＋1 で 34 になり、**#35 の 2 件削除で 32** |

区間別も導き直して **8 ／ 2 ／ 22** とした（軸F 8 件／ローダの他の失敗経路 2 件／掃引 22 件）。
#31 の実測 8 ／ 2 ／ 24 から掃引側が 2 件減ったものである。

**`XlsFormatWriterModelTest` の 15 は食い違いではない。**同じ節の追補が
`13 → 11 → 12 → 13` と続いており、末尾の宣言（13）は実測と一致している（15 は `04873de` 時点の記録）。

## 持ち越しの未決 2 件のうち 1 件が解けた

`steering.md` Rules の持ち越し (2)「台帳の宣言値のずれ 2 件の出所が未確認」（`checks/task-31.md`）のうち、
**`inventory.md` §2.1-2 の `YamlFormatReaderInvalidInputTest` 区間別の「それ以前の 2 件」が特定できた** ——
`d737815`（XLS-40 のあるべき姿の `@Ignore`）と `b19a236`（YML-14 のあるべき姿の `@Ignore`）であり、
**#35 が削除した 2 件そのもの**である。各コミットで `grep -c '^    @Test'` を取って追った実測である。

**残る 1 件（`inventory.md` §3.1 の `XlsFormatWriterTest` 内訳の `build` ＋3 の出所）は未確認のまま**である。
Step 4 の作業範囲外であり、完了条件にも入らない。

## Overall Verdict

- Self-check: OK
- QA: N/A（Rules「#30 以降、レビュア subagent は回さない」）
- Design expert: N/A（同上）
- Craft expert: N/A（同上）
- Verification expert: N/A（同上）
- Ready to check off: Yes（`mvn` の赤 5 件は #36 の担当であり、#35 が落としたものは 0 件）
