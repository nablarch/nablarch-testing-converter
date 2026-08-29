# #43 Completion Check

指示書 第2回 2-4（`nablarch-document@a12fb67`）。
参照点（ピン）: 解説書 `nablarch-document@a6da1f6` ／ `nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@3fecc4e`。

**レビュア subagent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| 着手時点の赤 4 件が緑 | OK | `mvn -o clean test` が `Failures: 0, Errors: 0` |
| 5 項目それぞれにテストがある | OK | 下表 |

## ゲート（Rules「#30 以降」）

- `git diff --stat 729347b..HEAD -- src/`（実測値はコミット後の `git diff --stat` を正とする）:

  ```
  src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java |  +11 -4
  src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderRealFileTest.java     |  +14 -14
  src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderScalarTest.java       |  +25 -11
  src/test/java/nablarch/test/tool/converter/yaml/YamlFrameworkAlignmentTest.java       |  新規
  ```

  **`src/main` は 1 行も触っていない。** 変換ツールの YAML 読み込みは器（yaml jar のビルダ）経由であり、
  実装は自動で追随する。本タスクはその追随をテストで押さえるものである。

- `mvn -o clean test` の最終行:

  ```
  Tests run: 681, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS
  ```

  **着手時点の赤 4 件がすべて緑になり、全件緑になった。** 実行件数 681 には、追跡していない
  測定用の一時テスト 9 件（`ZzOracleProbeTest` ／ `ZzProbeTest`。#46 で削除）を含む。追跡対象は 672 件。

## 5 項目のテスト

| yaml 第2回 | 押さえたこと | テスト |
|---|---|---|
| 2-1 末尾 `null` → `""` | 末尾のフィールドの `null` が空文字になる | `YamlFormatReaderScalarTest#readsTrailingUnquotedNullAsEmptyStringInRecordFragmentPath`（既存を直したもの） |
| 2-1（裏） | 末尾でない `null` は Java `null` のまま残る | `YamlFrameworkAlignmentTest#keepsNonTrailingNullAsJavaNullInRecordFragment`（**新規**） |
| 2-2 電文 `records:` は 1 つ | 2 つ書くとスキーマ検証（`YamlSchemaValidationException`）で落ちる。メッセージに `$.messages[0].records` が出る | `YamlFrameworkAlignmentTest#rejectsMessageWithTwoRecords`（**新規**） |
| 2-3 `fw_header:` のキー | 決められた名前にないキーがあるとエラー。メッセージに書いたキー名と、書けるキーの一覧が出る | `YamlFrameworkAlignmentTest#rejectsFwHeaderWithUnknownKey`（**新規**）／対照 `#acceptsFwHeaderWithKnownKey`（**新規**） |
| 2-4 空エントリは `{}` だけ | 全値が空文字のエントリは残る（テーブル経路・レコード断片経路） | `YamlFormatReaderScalarTest#skipsRowWhoseValuesAreAllEmpty`（既存を直したもの）／`YamlFrameworkAlignmentTest#keepsRowWhoseValuesAreAllEmptyStringsInTable`（**新規**） |
| 2-5 2 文字の `\` ＋ `r` | エラーになる。メッセージに書いた値が出る | `YamlFrameworkAlignmentTest#rejectsTwoCharacterBackslashR`（**新規**） |

**新規は 6 件**（`YamlFrameworkAlignmentTest`）。

## 着手時点の赤 4 件をどう直したか

いずれも変換ツール側のテストが yaml 第2回より前の挙動を期待値に書いていたもので、`src/main` の欠陥ではない。

| # | テスト | 直した内容 |
|---|---|---|
| 1 | `YamlFormatReaderInvalidInputTest#fillsMissingRecordFragmentValuesWithEmptyStringInsteadOfNull` | 2 行目の期待を `[a, null, ""]` → `[a, "", ""]`。**末尾側に並んだ `null` と欠損はまとめて空文字になる。** テストの主題（書かれた空文字と欠損が区別できない＝YML-05）は 1 行目が担っており、変わっていない |
| 2 | `YamlFormatReaderScalarTest#readsUnquotedNullAsJavaNullInRecordFragmentPath` → `#readsTrailingUnquotedNullAsEmptyStringInRecordFragmentPath` | 期待を `null` → `""` へ。ヘルパのフィクスチャはフィールド 1 件で、**唯一のフィールドは常に末尾**である。名前が主張と食い違うため改名した |
| 3 | `YamlFormatReaderScalarTest#skipsRowWhoseValuesAreAllEmpty` | 期待に全値が空文字の行を戻した。**読み飛ばされるのは空マッピング `{}` の行だけ**である |
| 4 | `YamlFormatReaderRealFileTest#keepsFwHeaderNamedRecordInSendSyncFromRealYaml` | フィクスチャの `records:` を 2 件 → 1 件へ。**電文のレコードレイアウトは 1 つ**であり、2 件はスキーマ検証で落ちる。テストの主題（`FW_HEADER` という名前のレコードが落とされないこと）は変えていない |

## 完了条件4（期待値をわざと崩すと落ちること）

足した 6 件・直した 4 件のそれぞれについて 1 回ずつ確認した（計 10 件）。

| # | テスト | 崩した内容 | 結果 |
|---|---|---|---|
| 1 | `#keepsNonTrailingNullAsJavaNullInRecordFragment` | 期待を `[a, null, c]` → `[a, "", c]` | Failures: 1 |
| 2 | `#rejectsMessageWithTwoRecords` | 文言の期待 `$.messages[0].records` → `$.messages[0].ZZZ` | Failures: 1 |
| 3 | `#rejectsFwHeaderWithUnknownKey` | 文言の期待 `notAFwHeaderField` → `ZZZ` | Failures: 1 |
| 4 | `#acceptsFwHeaderWithKnownKey` | 期待 `R1` → `ZZZ` | Failures: 1 |
| 5 | `#keepsRowWhoseValuesAreAllEmptyStringsInTable` | 期待の要素数を 2 → 3 へ | Failures: 1 |
| 6 | `#rejectsTwoCharacterBackslashR` | 文言の期待 `a\rb` → `zzz` | Failures: 1 |
| 7 | `#fillsMissingRecordFragmentValuesWithEmptyStringInsteadOfNull` | 2 行目の期待 `""` → `null`（直す前の値へ戻す） | Failures: 1 |
| 8 | `#readsTrailingUnquotedNullAsEmptyStringInRecordFragmentPath` | 期待 `""` → `ZZZ` | Failures: 1 |
| 9 | `#skipsRowWhoseValuesAreAllEmpty` | 期待から全値が空文字の行を落とす（直す前の値へ戻す） | Failures: 1 |
| 10 | `#keepsFwHeaderNamedRecordInSendSyncFromRealYaml` | レコード種別の期待 `FW_HEADER` → `ZZZ` | Failures: 1 |

## 台帳の更新

- `coverage/inventory.md` §0.1-2 に**追補その 16**（666 → 672）
- 同 §4.6 に `yaml/YamlFrameworkAlignmentTest` の行
- 同 §2.1-2 の経路差の表（D2-06 のレコード断片経路が空文字になった）と、変異の表の参照名
- `coverage/issues.md` YML-03 の記述（送信系のフィクスチャを 1 件へ改めたこと）
- `coverage/axis-matrix.md` の D2-06 の参照名と、経路差が生じたこと
