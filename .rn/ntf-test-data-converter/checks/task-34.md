# #34 Completion Check

指示書 2-3（`nablarch-document@0d9a049` の `.rn/20260724-ntf-yaml-support/ntf-step4-05-nablarch-testing-converter.md`）。
参照点: 解説書 `nablarch-document@5783b35` ／ `nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@0b3015c`。

**レビュア subagent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| 中間モデルの `groupId` が生値である（`[ ]` を持たない） | OK | 辺①は `TestCoreReaderAdapter#markerGroupId` が角括弧を外す（`TestCoreReaderAdapterTest#readHeadersExtractsGroupId` → `g1`）。辺②は `YamlFormatReader#rawGroupId`（旧 `formatGroup`）が生値をそのまま入れる（`YamlFormatReaderTest#readTable_expectedWithGroup_keepsRawGroupIdAndCreatesBlockPerGroup` → `case01`）。中間モデルの期待値 62 箇所を生値へ書き換えて全件緑 |
| 4 種のグループ ID（省略・単一・複数・送信系）で 4 経路の往復が壊れていない | OK | 既存の往復テストが全件緑 —— 省略: `RoundTripTest#xls_setupTable_isPreserved` ほか（`groupId` ＝ `""`）／単一: `#xls_expectedTable_withGroupId_isPreserved`・`#yaml_expectedTable_withGroupId_isPreserved`／複数: `YamlFormatWriterTest#serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId`・`YamlFormatReaderTest#readFile_expectedFixedWithMultipleGroups_mapsExpectedFixedAndDedupesGroups`／送信系: `RoundTripTest#xls_expectedRequestHeaderMessages_isPreserved` ほか 8 件・`XlsFormatWriterTest#roundTripsSendSyncMessage`・`YamlFormatWriterTest#roundTrip_sendSync_preservesGroupIdAndNoField` |
| `[ ]` を知るのが層 A・層 B の 2 層だけに閉じており、`XlsFormatReader`・`YamlFormatReader` が `[ ]` を扱わない | OK | `src/main` 全体で角括弧のリテラルを扱うグループ ID の箇所は 3 か所だけ —— `XlsFormatWriter#markerGroupId`（付ける・層 A）／`TestCoreReaderAdapter#markerGroupId`（外す・層 A）／`GroupIdNotation#format`（層 B。両アダプタが上流へ渡す直前に呼ぶ）。`XlsFormatReader`・`YamlFormatReader` からは消えた（`YamlFormatReader#formatGroup` は `#rawGroupId` へ、`YamlFormatWriter#rawGroup` は削除） |
| 既存テストの期待値を変えた箇所・変えなかった箇所が全件・件数つきで挙がっている | OK | **変えた 62 箇所（56 行・12 ファイル）／変えなかった 0 箇所**。加えて改称 3 件・書き直し 1 件。詳細は下記 |

## ゲート（Rules「#30 以降」）

- `git diff --stat 548ca59..作業ツリー -- src/`:

  ```
  src/main/.../core/reader/TestCoreReaderAdapter.java        | 41 ++++++++----
  src/main/.../core/reader/YamlTestCoreAdapter.java          | 18 ++++---
  src/main/.../converter/model/TestDataBlock.java            |  9 ++++
  src/main/.../converter/xls/XlsFormatWriter.java            | 21 +++++++-
  src/main/.../converter/yaml/YamlFormatReader.java          | 56 ++++++++++------
  src/main/.../converter/yaml/YamlFormatWriter.java          | 32 ++++-------
  src/test/.../core/reader/TestCoreReaderAdapterTest.java    | 11 +++--
  src/test/.../core/reader/YamlTestCoreAdapterTest.java      |  6 +--
  src/test/.../converter/RoundTripTest.java                  | 20 ++++----
  src/test/.../xls/XlsFormatReaderInvalidInputTest.java      | 30 ++++++------
  src/test/.../xls/XlsFormatReaderRealFileTest.java          | 16 +++----
  src/test/.../xls/XlsFormatReaderTest.java                  | 14 +++---
  src/test/.../xls/XlsFormatWriterModelTest.java             |  4 +-
  src/test/.../xls/XlsFormatWriterTest.java                  | 10 ++--
  src/test/.../xls/XlsReferenceFixtureTest.java              |  2 +-
  src/test/.../yaml/YamlFormatReaderRealFileTest.java        | 12 ++---
  src/test/.../yaml/YamlFormatReaderTest.java                | 28 +++++-----
  src/test/.../yaml/YamlFormatWriterModelTest.java           |  2 +-
  src/test/.../yaml/YamlFormatWriterTest.java                | 18 +++----
  19 files changed, 198 insertions(+), 152 deletions(-)
  ```

  加えて新規 1 ファイル `src/main/.../core/reader/GroupIdNotation.java`（層 B の整形 1 メソッド）。

- `mvn -o clean test` の最終行:

  ```
  Tests run: 626, Failures: 5, Errors: 0, Skipped: 2
  BUILD FAILURE
  ```

  **緑ではない。** 落ちている 5 件は**着手前から赤い 5 件と同一**であり（`checks/step4-report.md` §0）、
  **#36（2-5〜2-7）の担当**である。#34 が新たに落としたものは 0 件。**テストメソッド数は 626 のまま変わらない**
  （追加 0・削除 0・改称 3）。

## 是正の内訳

| 層 | 箇所 | 変更 |
|---|---|---|
| A Excel 版面 | `XlsFormatWriter#marker` ／ `#markerGroupId`（新設） | 生値へ角括弧を**付ける**。付ける場所をこの 1 メソッドへ寄せた |
| A Excel 版面 | `TestCoreReaderAdapter#markerGroupId` | マーカーセルから切り出した文字列の角括弧を**外して**生値を返す |
| B 上流 API 境界 | `GroupIdNotation#format`（新設・package private） | `groupId == null \|\| groupId.isEmpty() ? "" : "[" + groupId + "]"`。整形の式をここ 1 つに揃えた |
| B 上流 API 境界 | `TestCoreReaderAdapter#readTables` ／ `#readFiles` ／ `#readSendSyncMessages` | 生値で受け取り、上流へ渡す直前に `GroupIdNotation#format` を掛ける |
| B 上流 API 境界 | `YamlTestCoreAdapter#readTables` ／ `#readFiles` | 同上 |
| 整形しない例外 | `TestCoreReaderAdapter#readBlockBodyLines` | `markerGroupId` の出力との内部比較であり、両側とも生値になるため |
| 整形しない例外 | `YamlTestCoreAdapter#readSendSyncMessages` | 上流 `YamlMessageBuilder#buildSendSyncBodies`（`0b3015c:150`-`:163`）が生値で比較するため（従来どおり） |
| リーダー | `YamlFormatReader#formatGroup` → `#rawGroupId` ／ `#formattedGroupsInOrder` → `#groupIdsInOrder` ／ `#entriesForFormattedGroup` → `#entriesForGroupId` | 角括弧の付与をやめ、生値をモデルへ入れる。上流呼び出しのためにも組み立てない |
| リーダー | `XlsFormatReader` | **変更なし**（`header.getGroupId()` を生値のまま各アダプタへ渡す。#32 の着手前検証 (a) の 7 行範囲は変更不要のとおり） |
| ライター | `YamlFormatWriter#rawGroup` | **削除**。`#emitGroupId` は生値をそのまま書き、空文字のときだけキーごと落とす |
| Javadoc | `TestDataBlock`（クラス）／`XlsFormatWriter#marker`／`TestCoreReaderAdapter#markerGroupId`・`#readTables`・`#readFiles`・`#readSendSyncMessages`・`#readBlockBodyLines`／`YamlTestCoreAdapter#readTables`・`#readFiles`・`#readSendSyncMessages`／`YamlFormatReader#rawGroupId`・`#groupIdsInOrder`・`#entriesForGroupId`／`YamlFormatWriter#emitGroupId` | 生値で持つ・受ける旨と、書式を知ってよい 2 層を書いた |

## 既存テストの変更

**期待値を変えた 62 箇所（56 行・12 ファイル）。変えなかった 0 箇所。**

導出コマンド（変更前の作業ツリーに対して実行した実測）:

```
grep -ro '"\[[A-Za-z0-9_]*\]"' src/test --include=*.java \
  | grep -v '\[no\]\|\[NOTE\]\|\[MARK\]\|\[EMPTY\]\|\[COL\]\|\[ignore\]\|\[data\]' | wc -l   → 62
```

（マーカーカラム名 `[no]`・`[NOTE]`・`[MARK]`・`[EMPTY]`・`[COL]`・`[ignore]` と例外メッセージの `[data]` は
グループ ID ではないため除外。Excel マーカーセルの文字列（`"EXPECTED_TABLE[g1]=USERS"` など）は
`"[…]"` の形をしていないためこの grep に掛からず、**版面の書式として変えていない**。）

| ファイル | 行数 |
|---|---|
| `RoundTripTest` | 10 |
| `YamlFormatReaderTest` | 9 |
| `YamlFormatWriterTest` | 6 |
| `XlsFormatReaderRealFileTest` | 6 |
| `XlsFormatReaderTest` | 6 |
| `XlsFormatWriterTest` | 5 |
| `YamlFormatReaderRealFileTest` | 4（`:1129`-`:1131` は 9 値のリスト。行数 4・箇所 10） |
| `TestCoreReaderAdapterTest` | 4 |
| `XlsFormatWriterModelTest` | 2 |
| `YamlTestCoreAdapterTest` | 2 |
| `YamlFormatWriterModelTest` | 1 |
| `XlsReferenceFixtureTest` | 1 |
| **計** | **56 行 / 62 箇所** |

**指示書 §1-2 (c) の「全 44 件」は誤りである。** 同じ表の内訳は 56 行あり（`10+9+6+6+6+5+4+4+2+2+1+1`）、
上の grep の実測とも一致する。**対象の集合は同じで、合計欄の数だけが違う。**

### 期待値の書き換え以外の変更（4 件）

| テスト | 変更 | 理由 |
|---|---|---|
| `XlsFormatReaderInvalidInputTest#readsSuffixAfterKnownDataTypeNameAsGroupIdInRealBook` → `#dropsMarkerWhoseGroupIdIsNotBracketedInRealBook` | **観測できる挙動が変わったため書き直した。** 下記「観測できる出力の変化」参照 | 角括弧の無いグループ ID は上流と前方一致せず、ブロックが作られなくなった |
| `YamlFormatReaderTest#readTable_expectedWithGroup_formatsGroupIdAndCreatesBlockPerGroup` → `#readTable_expectedWithGroup_keepsRawGroupIdAndCreatesBlockPerGroup` | 改称のみ | 名前が「整形する」と主張していた |
| `YamlFormatReaderTest#readSendSync_groupsByRawValueFormatsGroupIdAndKeepsNoField` → `#readSendSync_groupsByRawValueKeepsRawGroupIdAndNoField` | 改称のみ | 同上 |
| `YamlFormatWriterTest#serialize_unbracketedGroupId_isUsedAsRawValue` → `#serialize_groupId_isWrittenVerbatim` | 改称＋コメント | 「整形されていない素のグループ ID（防御的経路）」という前提が消えた（`rawGroup` の削除により、すべてのグループ ID が生値） |

加えて、期待値ではないコメント・Javadoc の文言 8 か所（「整形済みグループ ID」「角括弧付きのまま」など）を
現行へ揃えた。

## 変異（期待値をわざと崩すと落ちること）

10 件すべて FAIL を確認した（`mvn -o clean test -Dtest=<クラス>#<メソッド>`）。

| 対象 | 崩した内容 | 結果 |
|---|---|---|
| `XlsFormatReaderTest#readPreservesGroupIdAndDataType` | 辺①の期待を `g1` → `[g1]` | FAIL |
| `YamlFormatReaderTest#readTable_expectedWithGroup_keepsRawGroupIdAndCreatesBlockPerGroup` | 辺②の期待を `case01` → `[case01]` | FAIL |
| `XlsFormatWriterTest#writesTableMarkerWithGroupId` | 版面の期待を `EXPECTED_TABLE[g1]=USERS` → `EXPECTED_TABLEg1=USERS` | FAIL |
| `YamlFormatWriterTest#serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId` | 辺④の期待を `group_id: "case01"` → `"[case01]"` | FAIL |
| `TestCoreReaderAdapterTest#readHeadersExtractsGroupId` | 層 A（外す）の期待を `g1` → `[g1]` | FAIL |
| `YamlTestCoreAdapterTest#readTables_expectedWithGroup_filtersByGroupId` | 層 B（YAML）の引数を `case01` → `[case01]` | FAIL |
| `TestCoreReaderAdapterTest#readSendSyncMessagesReturnsAllBlocksInGroup` | 層 B（Excel 送信系）の引数を `case1` → `[case1]` | FAIL |
| `XlsFormatReaderInvalidInputTest#dropsMarkerWhoseGroupIdIsNotBracketedInRealBook` | ブロック 0 件の期待を 1 件へ | FAIL |
| `XlsFormatWriterTest#roundTripsSendSyncMessage` | 往復後の期待を `case1` → `[case1]` | FAIL |
| `YamlFormatWriterTest#roundTrip_sendSync_preservesGroupIdAndNoField` | 同上 | FAIL |

## 観測できる出力の変化（2 件。どちらも追いかけない）

1. **`TABLE[]=x`（空のグループ ID）は往復後に `TABLE=x` になる。** 生値化すると空のグループ ID と
   グループ ID 省略がどちらもモデル上 `""` になるためである。指示書 2-3 が「追わない」と定めている
   （ユーザー判定・2026-08-27。`5783b35` の `testdata_notation.rst:247`-`:269` に空文字グループ ID の記述は無く、
   `src/test` に `"[]"` を期待する箇所も 0 件 —— `grep -rn '"\[\]"' src/test --include=*.java` → 0）。
2. **角括弧の無いグループ ID を持つマーカー（`SETUP_TABLEX=T`）のブロックが黙って消える。**
   `markerGroupId` が生値 `X` を返し、層 B が `[X]` へ整形するため、版面の `SETUP_TABLEX=` とは
   前方一致しなくなった。#34 前は `groupId` ＝ `"X"` のテーブルブロックとして読まれていた。
   **これは指示書が挙げていない 2 件目の変化である。**正しい記法は `SETUP_TABLE[g1]=T` であり
   角括弧の無い形は解説書に無いため、`TABLE[]=x` と同じく追いかけない。
   `issues.md` **XLS-11** に追記し、担保テストを新しい挙動へ書き直した。

## 付随して見つけたこと

- **XLS-39（辺③④の指すグループが食い違う）は #34 で解消した。**`groupId` ＝ `"[a]x[b]"` の中間モデルで
  プローブ実測（2026-08-28）—— 辺③が `SETUP_TABLE[[a]x[b]]=T` を書き辺①が `"[a]x[b]"` に戻す／
  辺④が `group_id: "[a]x[b]"` を書き辺②が `"[a]x[b]"` に戻す。**4 経路とも同じ値で往復する。**
  推測剥がし（`YamlFormatWriter#rawGroup`）と角括弧付与（`YamlFormatReader#formatGroup`）が
  どちらも消えたためである。`issues.md` XLS-39 に追記した（**判定「保留」は変えていない**。
  グループ ID に使える文字を定める明文が無いことは今も変わらないため）。
  プローブは実行後に削除し、恒久テストは足していない（完了条件が求めるグループ ID 4 種に
  区切り文字入りは含まれないため）。**申し送りの束に XLS-39 を残すかは調整側の判断**（Rules）。
- **COV-13（`[` で始まり `]` で終わらないグループ ID の未到達分岐）も消えた。**未到達箇所とした
  `YamlFormatWriter#rawGroup` そのものを削除したためである。JaCoCo は再計測していない
  （Rules「JaCoCo の再計測はしない」）。`issues.md` COV-13 と `coverage-report.md` の該当行に追記した。

## 台帳の更新

| 文書 | 更新 |
|---|---|
| `coverage/issues.md` | **XLS-11** に #34 前後の挙動と担保テストの改称を追記／**XLS-39** に #34 で解消した旨とプローブ実測を追記／**COV-13** に未到達分岐が消えた旨を追記 |
| `coverage/axis-matrix.md` | 辺①②③④の C-06(値あり) 4 行を生値の説明へ。F1-03 の担保テスト名を改称後へ |
| `coverage/inventory.md` | 改称した 4 件の行（F1-03 の 1 件・辺② 2 件・辺④ 1 件）を改称後の名前と現行の説明へ |
| `coverage/coverage-report.md` | COV-13 の行に「#34 で `rawGroup` ごと削除され未到達分岐が消えた／再計測はしていない」を追記（数値・行番号は `da66425` 固定のまま） |

## Overall Verdict

- Self-check: OK
- QA: N/A（Rules「#30 以降、レビュア subagent は回さない」）
- Design expert: N/A（同上）
- Craft expert: N/A（同上）
- Verification expert: N/A（同上）
- Ready to check off: Yes（`mvn` の赤 5 件は #36 の担当であり、#34 が落としたものは 0 件）
