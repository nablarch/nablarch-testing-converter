# #47 Completion Check

指示書 第2回 §9「#47 でやること」4 点（`nablarch-document@origin/ntf-yaml-support`
`.rn/20260724-ntf-yaml-support/ntf-step4-07-nablarch-testing-converter-2.md`）。

**レビュア subagent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| 1・2 の各テストが、指定した変異で落ち HEAD で通る | OK | 下の「変異試験」の表（4 件・変異 3 種） |
| `mvn -o clean test` 緑（`Tests run: 682`） | OK | `Tests run: 682, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS` |
| `git grep` 2 式が 0 件のまま | OK | 下の「ゲート」 |
| `git status --short` が空 | OK | 本コミット後に無出力 |
| push 済み | OK | 本コミットを `origin/ntf-test-data-converter` へ push |

## ゲート（Rules「#30 以降」）

- `git diff --stat 77e4a22..HEAD -- src/`:

  ```
   .../test/tool/converter/xls/XlsFormatReader.java   |  14 +-
   .../nablarch/test/core/file/DataFileInspector.java |  23 ++-
   .../converter/xls/XlsInterleavedBlockTest.java     | 119 ++++++++++++++
   .../converter/xls/XlsKeyValueNotationTest.java     | 178 +++++++++++++++++++++
   4 files changed, 324 insertions(+), 10 deletions(-)
  ```

  **`src/main` の変更は `XlsFormatReader#normalizeDirectiveValue` のコメントだけ**（指示書 §9 の 3）。
  コード行は 1 行も変えていない。残る 3 ファイルは `src/test` である。

- `mvn -o clean test` の最終行:

  ```
  Tests run: 682, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS
  ```

- 2-6 の 2 式（`git add` 後に実行）:

  ```sh
  git grep -nE '\.rst|nablarch-document|解説書|出典|根拠:' -- src/   # → 0 件
  git grep -nE '[A-Za-z]+\.java:[0-9]+' -- src                        # → 0 件
  ```

- `grep -rn '^    @Ignore' src/test --include=*.java` → **0 件**

## 変異試験（`src/main` を 1 箇所ずつ壊して対象テストを実行。実行後は `git checkout -- src/` で復元）

| # | 壊した箇所 | 対象テスト | 変異あり | HEAD |
|---|---|---|---|---|
| M6 | `XlsFormatWriter#appendKeyValueRows` の `toCellNotation(nullToEmpty(v))` → `nullToEmpty(v)` | `XlsKeyValueNotationTest` 2 件 | **2 件とも赤** | 緑 |
| M7 | `XlsFormatReader#isGroupCollected` を `isTableType(type)` だけに | `XlsInterleavedBlockTest#warnsAndDropsFileBlockAfterInterleavedGroupId` | **赤** | 緑 |
| M8 | `XlsFormatReader#unreadIdentifiersAfter` の `unread.add(...)` の直後に `break` | `XlsInterleavedBlockTest#warnsAboutEveryUnreadBlockAfterInterleavedGroupId` | **赤** | 緑 |

**M6 の実測（変異あり）**

```
XlsKeyValueNotationTest.keepsQuotationNotationOfDirectiveValueThroughXlsRoundTrip
  but: was <{field-separator=,, file-type=Variable, quoting-delimiter="}>      （期待は """）
XlsKeyValueNotationTest.keepsQuotationNotationOfFwHeaderValueThroughXlsRoundTrip
  but: was <{requestId="R1", userId=U1}>                                       （期待は ""R1""）
Tests run: 2, Failures: 2, Errors: 0, Skipped: 0
```

往復のたびに引用符が外側 1 層ずつ減っていく、というのが M6 の症状である。

**M7 の実測（変異あり）**

```
XlsInterleavedBlockTest.warnsAndDropsFileBlockAfterInterleavedGroupId:341 警告の件数
  Expected: is <1>  but: was <0>
Tests run: 5, Failures: 1, Errors: 0, Skipped: 0
```

同クラスの残る 4 件（テーブル系 3 件・#47 で足した多重ブロック 1 件）は緑のままであり、
**ファイル系だけを落とす変異をファイル系のテストだけが検知している。**

**M8 の実測（変異あり）**

```
XlsInterleavedBlockTest.warnsAboutEveryUnreadBlockAfterInterleavedGroupId:297
  Expected: a string containing "[C, D]"
  but: was "[interleavedBook] シート "sheet1" では、…次のデータブロックを出力しません: [C]"
Tests run: 5, Failures: 1, Errors: 0, Skipped: 0
```

## 足したテスト（4 件）

| クラス | メソッド | 入力（実 `.xlsx`） | 正解の取り方 |
|---|---|---|---|
| `xls/XlsKeyValueNotationTest` | `#keepsQuotationNotationOfDirectiveValueThroughXlsRoundTrip` | `SETUP_VARIABLE` の `quoting-delimiter` セルにダブルクォート 5 個 | `FrameworkOracle.files` ＋ `DataFileInspector.directives` |
| 同 | `#keepsQuotationNotationOfFwHeaderValueThroughXlsRoundTrip` | `MESSAGE` の FW ヘッダ `requestId` セルに `"""R1"""` | `FrameworkOracle.messageFwHeader` |
| `xls/XlsInterleavedBlockTest` | `#warnsAboutEveryUnreadBlockAfterInterleavedGroupId` | `SETUP_TABLE=A` ／ `[g1]=B` ／ `=C` ／ `=D` | 警告本文の識別子一覧 |
| 同 | `#warnsAndDropsFileBlockAfterInterleavedGroupId` | `SETUP_FIXED=a.dat` ／ `[g1]=b.dat` ／ `=c.dat` | (iii) は `FrameworkOracle.files` と `YamlFrameworkOracle.setupFiles` の突き合わせ |

いずれも期待値を自分で書かず、**元の `.xlsx` をフレームワーク本体が読んだ値**を正解にしている。
本体が返す値そのもの（`"""` ／ `""R1""`）も各テストで明示し、本体側が黙って変わったときに気づけるようにした。

## 指示書との食い違い 1 件（実測で訂正した）

**指示書 §9 の 1（2 つ目のケース）は「`MESSAGE` の FW ヘッダ `requestId` セルに `"""R1"""`。
本体は `"R1"` に読む」とするが、実測では本体は `""R1""` に読む。**
`QuotationTrimmer` が外すのは外側 1 層だけであり、ダブルクォート 3 個ずつで囲んだ値からは 1 個ずつしか減らない
（`"""R1"""` → `""R1""`）。1 つ目のケース（`"""""` → `"""`）は指示書のとおりである。

**入力は指示書の指定どおり `"""R1"""` のままにし、期待値だけを実測へ合わせた。**
M6 を検知するという目的（素で書くと 1 層減る）はどちらの値でも成り立つ。

## 台帳の更新

- `steering.md` #40 の完了条件から `tail` を外し、残した理由を 1 行足した（指示書 §9 の 4）。
  呼び出しは `XlsFormatReader` の `skipToFirstNameRow` 1 か所・`verifyNameRow` 1 か所・`readFieldDefs` 2 か所の計 4 か所
  （`grep -n 'tail(' src/main/java/nablarch/test/tool/converter/xls/XlsFormatReader.java` の実測）。
  Steps の `【§8-5】` 行は [x] のまま動かしていない
- `coverage/inventory.md` §0.1-2 に**追補その 19**（追跡対象 678 → 682）。§4.6 の
  `XlsInterleavedBlockTest` を 3 → 5 件へ導き直し、`XlsKeyValueNotationTest` の行を足した。
  相乗りクラスの表に `DataFileInspector` の `directives` を追記

## 調整側へ開示するもの（マージ可否の判断は出さない）

Rules「マージ可否の判断は出さない。完了報告に『要対応 0 件』と書かない」に従う。開示は次の 3 件。

| # | 内容 | 置き場所 |
|---|---|---|
| 1 | 指示書 §9 の期待値 1 件が実測と違った（`"""R1"""` は `"R1"` ではなく `""R1""` に読まれる）。入力は指定どおりにし、期待値を実測へ合わせた | 上節 |
| 2 | M8 のテストは最初 `containsString("D")` で書いたが、警告本文の「グループID」に `D` が含まれるため変異があっても緑になった。読まれなかったブロックの一覧そのもの（`[C, D]`）を突き合わせる形へ直した。同クラスの既存 3 件が使う `containsString("C")` は本文の別語に当たらないため、そのままにした | —— |
| 3 | `DataFileInspector` にディレクティブを取り出す口を足した（`src/test` のみ。`src/main` からは使わない） | `coverage/inventory.md` §4.6 |
