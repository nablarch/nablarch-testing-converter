# #36 Completion Check

指示書 2-5〜2-7（`nablarch-document@a16be0a` の `.rn/20260724-ntf-yaml-support/ntf-step4-05-nablarch-testing-converter.md`）。
参照点: 解説書 `nablarch-document@5783b35` ／ `nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@0b3015c`。

**レビュア subagent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| 着手時点の赤 5 件がすべて通る | OK | `mvn -o clean test` → `Tests run: 625, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS`。**Step 4 で初めて全件緑になった** |
| 5 件それぞれの新しい期待値に、解説書の `file:line` と実測値の根拠が付いている | OK | 下表「赤 5 件の是正」 |
| 2-6 のヘルパー修正で期待値が動いた呼び出し元が、件数つきで全件挙がっている | OK | **呼び出し元 25 か所すべてで期待値は動かない。**ヘルパーの中（`readValueLine` の `columnNames`・取り出し位置、`readListMapValue` の同 2 点）だけが動いた。理由と件数の内訳は下記 |
| 「すべての値が空文字の行は読み飛ばされる」正のテストが 1 件ある | OK | `YamlFormatReaderScalarTest#skipsRowWhoseValuesAreAllEmpty`（`{}` の行と、値が `""` だけの行の両方を含む） |
| `coverage/issues.md` の YML-04 に解消済みの 1 行が入っている（書き換えていない） | OK（**ただし「解消済み」ではなく「一部だけ解消」と書いた。**下記「指示書との食い違い 1 件」） | `issues.md` YML-04 の表 2 行を #36 前後の併記へ改め、直後に追記した |

## ゲート（Rules「#30 以降」）

- `git diff --stat 8258390..作業ツリー -- src/`:

  ```
  src/main/.../core/reader/YamlTestCoreAdapter.java              | 17 ++++-
  src/test/.../core/reader/YamlTestCoreAdapterTest.java          | 30 +++++++--
  src/test/.../yaml/YamlFormatReaderInvalidInputTest.java        | 49 ++++++++++-----
  src/test/.../yaml/YamlFormatReaderScalarTest.java              | 72 +++++++++++++++---
  4 files changed, 136 insertions(+), 32 deletions(-)
  ```

  **`src/main` の変更は `YamlTestCoreAdapter#isResourceExisting` の Javadoc だけである**（コードは無変更）。

- `mvn -o clean test` の最終行:

  ```
  Tests run: 625, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS
  ```

  624 件 → **625 件**（2-6 の正のテストを 1 件追加）。

## 赤 5 件の是正

| # | テスト | 新しい期待値 | 根拠（解説書・上流の `file:line` と実測値） |
|---|---|---|---|
| 2-5 | `YamlTestCoreAdapterTest#isResourceExisting_reflectsFileExistence` → `#isResourceExisting_reflectsContainerDirectoryExistence` | 入れ物ディレクトリの有無だけを映す。**読み込み単位名が実在しない `noSuchFile` でも `true`** | 委譲先 `nablarch-testing-yaml@0b3015c` の `YamlLoader.java:184`-`:186`（`new File(buildContainerPath(...)).isDirectory()`）。入れ物の定義は同 `:165`-`:178` の Javadoc、入れ物名の決め方は同 `:97`-`:100`（最後の `/` より前。`/` が無ければ全体）。読み込み単位の存在は同 `:200`-`:202` の `isDataExisting` |
| 2-6 | `YamlFormatReaderScalarTest#readsEmptyStringAsIs` | `""` が空文字として入る（従来どおり）。**ヘルパーが行の先頭に空でないカラム `K: "x"` を置くようにした** | `implementation/testdata_notation.rst:1500`（`5783b35`）「全要素が空のエントリは読み飛ばされる。……YAML では `rows:` 内の要素が空マッピング（`{}`）またはすべての値が空文字の場合にスキップされる。」——**検証対象 1 カラムだけの行に `""` を書くと行ごと読み飛ばされる**ため、`V` 列を測るには空でない `K` 列が要る |
| 2-6 | `YamlFormatReaderScalarTest#readsEmptyStringAsIsInListMapPath` | 同上（LIST_MAP 経路） | 同上 |
| 2-7 | `YamlFormatReaderInvalidInputTest#dropsAllRowsWhenFirstRowOfTableIsEmptyObject` → `#skipsEmptyObjectRowAndKeepsFollowingRowInTable` | `columnNames` ＝ `[A]`、`rows` ＝ `[[1]]`（**実測。2026-08-28**） | 同 `:1500`。空マッピングの行がカラム解決より前に読み飛ばされるため、カラム名は読み飛ばしたあとの先頭行で決まる |
| 2-7 | `YamlFormatReaderInvalidInputTest#keepsRowCountButLosesValuesWhenFirstRowOfListMapIsEmptyObject` → `#skipsEmptyObjectRowAndKeepsFollowingRowInListMap` | `columnNames` ＝ `[A]`、`rows` ＝ `[[1]]`（**実測。テーブル経路と同じ結果**） | 同上。**経路差（テーブルは行ごと消える／LIST_MAP は値を持たない行が残る）が消えた** |

メソッド名と Javadoc は「欠陥の名前」（`dropsAllRows…` ／ `keepsRowCountButLosesValues…`）をやめ、
記法どおりの挙動を述べる名前（`skipsEmptyObjectRowAndKeepsFollowingRow…`）にした。
Javadoc の「最も損失が大きい形」「2 行目に書いたデータも消える」も落とした。

## 2-6 のヘルパー修正と、その呼び出し元

| ヘルパー | 変更 |
|---|---|
| `readValueLine`（`readValue` ／ `readOmittedValue` ／ `readBlockScalarValue` が経由） | 組み立てる YAML の行に `K: "x"` を先に置いた。`columnNames` の期待を `[V]` → `[K, V]`、取り出す位置を `get(0)` → `get(1)` |
| `readListMapValue` | 同上 |

**呼び出し元 25 か所は 1 つも直していない。**いずれも戻り値（`V` 列の値）だけを見ており、
`columnNames` や取り出し位置に触れていないためである。内訳は
`readValue` 20 件（`:237`〜`:543`）／`readOmittedValue` 1 件／`readBlockScalarValue` 2 件／
`readListMapValue` 2 件で、導出は次のとおり。

```sh
grep -n 'readValue(\|readListMapValue(\|readBlockScalarValue(\|readOmittedValue(' \
    src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderScalarTest.java \
  | grep -v 'private String read' | grep -c 'assertThat'
```

→ **25**。**他クラスからの呼び出しは 0 件**（`grep -rn 'readValueLine\|readListMapValue\|readBlockScalarValue' src/test --include=*.java` は同クラス以外にヒットしない）。

**ブロックスカラーのインデントは変わっていない。** キー `V` の開始桁は
`      - V:`（空白 6 個＋`- `）でも `        V:`（空白 8 個）でも **9 桁目**で同じであり、
`readBlockScalarValue` が付ける継続行のインデント（半角空白 10 個）はそのままで通る（実測で緑）。

## 変異（期待値をわざと崩すと落ちること）

6 件すべて FAIL を確認した（`mvn -o clean test -Dtest=<クラス>#<メソッド>`）。

| テスト | 崩した内容 | 結果 |
|---|---|---|
| `YamlTestCoreAdapterTest#isResourceExisting_reflectsContainerDirectoryExistence` | 実在しない読み込み単位名の期待を `true` → `false` | FAIL |
| `YamlFormatReaderInvalidInputTest#skipsEmptyObjectRowAndKeepsFollowingRowInTable` | 期待を #36 前の `columnNames=[]`・`rows=[]` へ戻す | FAIL |
| `YamlFormatReaderInvalidInputTest#skipsEmptyObjectRowAndKeepsFollowingRowInListMap` | 期待を #36 前の `columnNames=[]`・`rows=[[], []]` へ戻す | FAIL |
| `YamlFormatReaderScalarTest#readsEmptyStringAsIs` | 期待を `""` → Java `null` | FAIL |
| `YamlFormatReaderScalarTest#readsEmptyStringAsIsInListMapPath` | 同上 | FAIL |
| `YamlFormatReaderScalarTest#skipsRowWhoseValuesAreAllEmpty` | 期待に空文字だけの行 `["", ""]` を足す | FAIL |

## 指示書との食い違い 1 件（実測で訂正）

**YML-04 は「解消済み」ではなく「一部だけ解消」である。**

指示書 2-7 は「`coverage/issues.md` は YML-04 が解消済みであることを 1 行追記するに留める」としているが、
**YML-04 の中心は残っている。**解消したのは表の下 2 行（先頭行が空マッピング `{}` の形）だけであり、
本題である「カラムは先頭行のキー集合だけで決まり、後続行にしかないカラムが黙って消える」は変わっていない。
根拠は、その 3 件のテストが**期待値を変えずに緑のまま**であることである ——
`dropsColumnThatAppearsOnlyInSecondRowOfTable`（`[{A: "1"}, {A: "2", B: "x"}]` → `columnNames=[A]`・`B` が消える）／
`dropsColumnThatAppearsOnlyInSecondRowOfListMap`（同上）／
`padsColumnMissingFromSecondRowWithNullInTable`（逆向きは `null` で救われる）。

`issues.md` には「一部だけ解消した」として、解消した形・残っている形・その根拠テスト名を追記した
（**記録の書き換えはしていない**。表の 2 行は #36 前後の併記に改めた）。**判定欄（対応不要）は変えていない。**

## 台帳の更新

| 文書 | 更新 |
|---|---|
| `coverage/issues.md` | **YML-04** —— 表の 2 行を #36 前後の併記へ、直後に「一部だけ解消した」の追記（上記） |
| `coverage/inventory.md` | `YamlFormatReaderScalarTest` の宣言値 27 → **28**（`skipsRowWhoseValuesAreAllEmpty` の追加ぶん。コマンドから導き直した）／§2.1-2 の `YamlFormatReaderInvalidInputTest` の 3 区間を **8 ／ 2 ／ 22** へ（#35 の削除ぶんを導き直した。awk の 3 コマンドを実行した実測）／改称した 3 件の参照 |
| `coverage/axis-matrix.md` | 改称した 3 件の参照 |

`YamlFormatReaderInvalidInputTest` の Javadoc とコメント 2 か所（「YML-04〜YML-07 は未修正」）も
「YML-04 は #36 で一部だけ解消した」へ揃えた。

## Overall Verdict

- Self-check: OK
- QA: N/A（Rules「#30 以降、レビュア subagent は回さない」）
- Design expert: N/A（同上）
- Craft expert: N/A（同上）
- Verification expert: N/A（同上）
- Ready to check off: Yes（`mvn -o clean test` が全件緑・`BUILD SUCCESS`）
