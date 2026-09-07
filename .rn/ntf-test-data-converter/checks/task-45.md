# #45 Completion Check

指示書 第2回 2-6（`nablarch-document@a12fb67`）。**2-6 は単独のコミットにする。**

**レビュア subagent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| `git grep -nE '\.rst\|nablarch-document\|解説書\|出典\|根拠:' -- src/` が 0 件 | OK | 下記 |
| `git grep -nE '[A-Za-z]+\.java:[0-9]+' -- src` が 0 件 | OK | 下記 |
| テストの動作・期待値を変えていない | OK | 下記「コードは 1 文字も変わっていない」 |
| 2-6 が単独のコミットになっている | OK | 本タスクのコミットは `src/` のコメント・Javadoc だけを触る |

## ゲート（Rules「#30 以降」）

- `git diff --stat 1d572ef..HEAD -- src/`（実測値はコミット後の `git diff --stat` を正とする）:

  ```
  48 files changed, 361 insertions(+), 403 deletions(-)
    src/main  18 files changed, 150 insertions(+), 174 deletions(-)
    src/test  30 files changed, 211 insertions(+), 229 deletions(-)
  ```

- `mvn -o clean test` の最終行:

  ```
  Tests run: 687, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS
  ```

  実行件数は #44 完了時から変わっていない（テストの増減なし）。追跡対象は 678 件。

## 着手前の件数（本タスク着手時点。#44 完了後）

指示書 §2-6 が挙げた `d611bec` 時点の 167 行・43 ファイルは、#40〜#44 でテストを足したため増えている。
**着手時点の実測**は次のとおり。

```
$ git grep -nE '\.rst|nablarch-document|解説書|出典|根拠:' -- src/   → 157 行 / 42 ファイル
    src/main  64 行 / 18 ファイル
    src/test  93 行 / 24 ファイル
$ git grep -nE '[A-Za-z]+\.java:[0-9]+' -- src                      →   9 行
```

`d611bec` の 167 行 → 157 行に減っているのは、#40 で `XlsFormatReader` の死んだコード
（Javadoc ごと 194 行）を削除したためである。

## 作業後（完了条件11）

```
$ git grep -nE '\.rst|nablarch-document|解説書|出典|根拠:' -- src/   → 0
$ git grep -nE '[A-Za-z]+\.java:[0-9]+' -- src                      → 0
```

## 取り除き方

1. **引用だけで構成された括弧を落とす** —— `（{@code testdata_notation.rst:883}（{@code 30a8271} 時点））` の形。
2. **他リポジトリのソース参照はクラス名だけ残す**（指示書 2-6 の 3）——
   `{@code PoiXlsReader.java:62-65}` → `{@code PoiXlsReader}`、
   `{@code YamlSection.groupMatches:281}-{@code :284}` → `{@code YamlSection#groupMatches}`。
   コミット指定（`{@code nablarch-testing@3c4bd2a} の`）も落とした。
3. **解説書を指す語を、そのモジュール自身の言葉へ置き換える** ——
   `{@code testdata_notation.rst:NNN}` → 「記法」、
   `{@code tools/testdata_converter.rst:NNN}` → 「変換ツールの仕様」、
   `{@code testdata_examples.rst:NNN}` → 「記載例」、裸の行番号 `{@code :NNN}` → 「記法」。
4. **逐語引用の鉤括弧を外し、そのモジュール自身の記述に書き直した。**
   置換で文がつながらなくなった箇所（`ModelPreconditions` 9 か所・`XlsFormatWriter` 12 か所ほか）は
   段落ごと書き直した。
5. **「解説書」「出典」の語を使っていた文**（`ColumnRowDataBlock` ／ `FieldDef` ／
   `ConverterDocumentedBehaviorTest` ／ `ExcelOutputDocumentedBehaviorTest` ／
   `SpecialNotationRoundTripTest` ／ `XlsEmptyEntryTest` ／ `XlsFormatReaderCellTypeTest` ／
   `XlsFormatReaderInvalidInputTest` ／ `XlsNotationSymmetryTest` ／
   `YamlFormatReaderInvalidInputTest`）も書き直した。

## コードは 1 文字も変わっていない（指示書 2-6 の 4）

コメント（`//`・`/* */`）を機械的に取り除いたうえで空白を正規化し、本タスク着手前（`1d572ef`）と
作業ツリーを全 `.java` について突き合わせた。**差分のあるファイルは 0 件**である。

```
$ python3 <コメント除去して比較するスクリプト>
differ (code, ignoring comments): 0
```

途中、`ModelPreconditions` の例外メッセージ（文字列リテラル）を 1 か所だけ書き換えていたことが
この照合で分かったため、**元へ戻した**。指示書が触ってよいとしたのはコメント・Javadoc・
assert メッセージであり、例外メッセージは含まれない。

## 残したもの

- **本体スキーマ（`nablarch/test/ntf-testdata-yaml-schema.json`）の description の引用は残した。**
  取り除く対象は解説書への参照であり、スキーマは別物である。抽出式にも掛からない。
- **`{@code coverage/issues.md}` ／ `{@code steering.md}` への参照は残した。**`.rn/` の記録であり、
  指示書 2-6 が「根拠の追跡は `.rn/` の報告書・台帳で行う」と定めているため。
