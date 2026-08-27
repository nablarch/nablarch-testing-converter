# Step 4 報告 — nablarch-testing-converter

指示書: `nablarch-document@origin/ntf-yaml-support` の
`.rn/20260724-ntf-yaml-support/ntf-step4-05-nablarch-testing-converter.md`

参照点: 解説書 `nablarch-document@5783b35` ／ 本モジュール `60d9a2d` ／
`nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@0b3015c`

構成は指示書「6. 報告」の 6 項に従う。**全 6 項を記入済み（#39・2026-08-28）。**

指示書は 2026-08-27 に `0d9a049` で 2-3 と表の行数が訂正された。第1節は
その訂正とユーザー判定（同日）を反映済みである。

---

## 0. 着手前の実測（2026-08-27）

`mvn -o clean test` = `Tests run: 605, Failures: 5, Errors: 0, Skipped: 2`。
赤 5 件は指示書「渡す前にやること」の記載と一致し、それ以外の赤は無い。

```
YamlTestCoreAdapterTest.isResourceExisting_reflectsFileExistence:370
YamlFormatReaderInvalidInputTest.dropsAllRowsWhenFirstRowOfTableIsEmptyObject:601
YamlFormatReaderInvalidInputTest.keepsRowCountButLosesValuesWhenFirstRowOfListMapIsEmptyObject:628
YamlFormatReaderScalarTest.readsEmptyStringAsIs:505->readValue:106->readValueLine:172
YamlFormatReaderScalarTest.readsEmptyStringAsIsInListMapPath:584->readListMapValue:192
```

---

## 1. 2-1・2-3 の「着手前に検証すること」の結果

### 1-1. 2-1「戻す条件」の反例検証 —— **反例なし（NG=0）**

`nablarch-testing@3c4bd2a` の実物のインタープリタ 3 本を
`src/test/resources/unit-test.xml:29`-`:40` と同じ順（`NullInterpreter` →
`QuotationTrimmer` → `LineSeparatorInterpreter`。`${...}` 系は掛けない）で組み、
`5783b35` の `implementation/testdata_notation.rst` の Excel 形式の表
（`:1353`-`:1391`）と YAML 形式の表（`:1405`-`:1443`）の**全データ行**について
「記法 → 値 → 戻し → 値'」を実行した。備考欄に現れる派生（`Null`／`"NULL"`／
`"ab"c"`／セル内 LF／`\n`／全角 `”…”`）も対象に含めた。

| 表 | ケース数 | 結果 |
|---|---|---|
| Excel 形式 データ行 12（`:1356`-`:1391`） | 18 | 全件 OK |
| YAML 形式 データ行 12（`:1408`-`:1443`） | 16 | 全件 OK |

表の行数は**実測 12 行**である（指示書の「13 行」は `0d9a049` で 12 行へ訂正済み）。
ケース数が行数を上回るのは、備考欄の派生（`Null`／`"NULL"`／`"ab"c"`／セル内 LF／
`\n`／全角 `”…”`）を別ケースとして数えたためである。

**ユーザー判定（2026-08-27）**: 反例なしとして受理。実装へ進む。

要点:

- `:1359 null` → Java null → `null` リテラル。`:1362 "null"` → 文字列 `null` →
  条件 ii で `"null"` へ復元。両者の区別が往復で保たれる
- `:1377 """` → `"` 1 文字 → 条件 iii で `"""` へ復元。再読込で
  `QuotationTrimmer.java:27` の `substring(1, 0)` に当たらない
- `:1389 \r` → CR → 条件 i で 2 文字の `\` ＋ `r` へ復元。`:1391 \n` は素通し、
  セル内 LF も素通し
- 条件 ii と iii が排他であること、条件 i が ii・iii の判定に影響しないことも全行で成立

**未確認**: `:1374 半角スペース1文字` の戻しは、囲まない裸のスペース 1 文字になる。
値としては往復するが、これは POI が前後スペースを保つことが前提である。
実ファイル起点の確認（完了条件3）で押さえる。

根拠（すべて `3c4bd2a`）: `NullInterpreter.java:15`、
`QuotationTrimmer.java:25`-`:27`、`LineSeparatorInterpreter.java:31`・`:34`・`:61`-`:64`。

**#32 で観測できる出力が変わるもの 1 件（追いかけない）**: YAML 形式にしか書けない
**2 文字の `\` ＋ `r`** は、#32 のあと **YAML→XLS→読み戻しで CR に変わる**
（`56ffbe6` では 2 文字のままだった。実測・2026-08-27）。
`XlsFormatWriter.toCellNotation:616` は CR を `\r` へ戻すだけで、値が既に 2 文字の
`\` ＋ `r` であればそのままセルへ書くため、読み戻しで `LineSeparatorInterpreter` が
CR へ解釈する。**Excel 記法にこの値を書く手段が無いことによる不可避の欠落**であり、
本体が読めば以前から CR である。`TABLE[]=x`（§1-2 (d)）と同じ扱いとし、追いかけない。

### 1-2. 2-3「`[ ]` に依存している箇所」の全走査

#### (a) `src/main`

| # | 箇所 | 現行の役割 | 2-3 後 |
|---|---|---|---|
| 1 | `YamlFormatReader.formatGroup:485`-`:488` | YAML 生値に `[ ]` を付けてモデルへ | 付与をやめる |
| 2 | `YamlFormatWriter.rawGroup:479`-`:487` | モデルから `[ ]` を推測剥がし | やめる |
| 3 | `TestCoreReaderAdapter.markerGroupId:282`-`:286` | `TYPE` と `=` の間を切り出す（`[g1]` がそのまま出る） | ここで外す |
| 4 | `XlsFormatWriter.marker:529`-`:531` | `TYPE + groupId + "=" + id` を連結 | ここで付ける |
| 5 | `XlsFormatReader:108`-`:128`・`:145`-`:146`・`:205`-`:206`・`:212`・`:240`・`:267`-`:274` | `header.getGroupId()` を上流 Excel パーサへ渡す | **変更不要**。生値をそのまま渡し、整形はアダプタ側で行う（(b)） |
| 6 | `YamlTestCoreAdapter.readTables:120`・`readFiles:149` | 上流 YAML ビルダへ渡す | **生値で受け取り、上流へ渡す直前に整形する**（(b)） |
| 7 | `TestCoreReaderAdapter.readBlockBodyLines:264`-`:266` ＋ `BodyLineCollector:453`-`:458` | `markerGroupId` の出力と equals 比較 | **整形しない**（両側とも生値になる） |
| 8 | `YamlTestCoreAdapter.readSendSyncMessages:179`-`:187` | 生値で比較（上流 `YamlMessageBuilder:156`-`:157`） | **整形しない**（上流が生値で比較。既に生値） |
| 9 | `YamlFormatReader.rawGroupsInOrder:427`・`entriesForRawGroup:466` | 送信系は既に生値で走査 | 変更不要。ただし `:299` のモデル格納は `formatGroup(entry)` なので生値へ |
| 10 | `XlsFormatReader.batchKey:637`-`:638` | 内部の重複判定キー | 表現は問わない |
| 11 | Javadoc: `XlsFormatWriter:524`／`TestDataBlock:77`／`YamlTestCoreAdapter:114`・`:143`／`TestCoreReaderAdapter:212`・`:230`-`:231`・`:259` | 整形済み前提の記述 | 書き直し対象 |

#### (b) 指示書の 1 文が上流の契約と両立しなかった —— ユーザーが指示書を訂正（`0d9a049`）

旧版の指示書 2-3 の「**`[ ]` を付けるのは `XlsFormatWriter.marker` の中だけにする**」は、
そのままでは実装できなかった。変更禁止の依存先 2 つが API 境界で `[case1]` 形式を要求するためである。

- **Excel 側** —— `nablarch-testing@3c4bd2a` の
  `GroupDataParsingTemplate.java:41`-`:42` が
  `getTargetType().getName() + groupId + '='` を組み立てて `first.startsWith(expected)`
  で前方一致する。Excel のマーカーセルは `SETUP_TABLE_DATA[g1]=USERS` なので、
  渡す `groupId` は `[g1]` でなければ一致しない。
  `TestCoreReaderAdapter.readTables:85`-`:87`・`readFiles:158`・
  `readSendSyncMessages:216`-`:220` はいずれもこの経路である
- **YAML 側** —— `nablarch-testing-yaml@0b3015c` の
  `YamlSection.groupMatches:281`-`:284` が
  `rawGroupId != null ? "[" + rawGroupId + "]" : ""` を作って `requestedFormatted`
  と比較する。`YamlTableDataBuilder:81`・`YamlFileBuilder:75`・
  `YamlMessageBuilder:115` が使う
- 解説書の裏付け —— `5783b35` の `implementation/testdata_notation.rst:268`
  「Excel 形式では ``データタイプ + グループID + '='`` による前方一致、YAML 形式では
  グループIDの完全一致で判定される」

**ユーザー判定（2026-08-27。指示書 `0d9a049` の 2-3）**: 指摘を受けて指示書を訂正。
`[ ]` を知ってよいのは次の **2 層**だけとする。

| 層 | 何を知るか | どこ |
|---|---|---|
| A Excel 版面の読み書き | `[ ]` は Excel 形式の書式である | 付ける＝`XlsFormatWriter.marker`／外す＝`TestCoreReaderAdapter.markerGroupId` |
| B 上流 API の境界 | 上流が整形済みグループ ID を要求する | `TestCoreReaderAdapter`・`YamlTestCoreAdapter` の各公開メソッドが**生値で受け取り、上流へ渡す直前に整形する** |

**当方の提案（3 境界方式）とは置き場所が違う。** 整形は `XlsFormatReader`・
`YamlFormatReader` ではなく**アダプタ 2 クラスの中**に置く。リーダー側に置くと
`YamlFormatReader` が `[ ]` を組み立てるままになり、2-3 が消そうとしている依存が残るためである。
`XlsFormatReader` は `[ ]` を扱わず、(a) の #5 に挙げた 7 つの行範囲は**変更不要**になる。

整形の式は `groupId == null || groupId.isEmpty() ? "" : "[" + groupId + "]"` の 1 つに揃える。

**整形しない例外 2 件**:

- `TestCoreReaderAdapter.readBlockBodyLines` —— `markerGroupId` の出力との内部比較であり、
  両側とも生値になるため
- `YamlTestCoreAdapter.readSendSyncMessages` —— 上流 `YamlMessageBuilder.buildSendSyncBodies`
  （`0b3015c:150`-`:163`）が**生値で**比較するため

#### (c) 既存テストの期待値に整形済みグループ ID が現れる箇所 —— 全 44 件

マーカーカラム（`[no]`・`[NOTE]`・`[MARK]`・`[EMPTY]`・`[COL]`・`[ignore]`。14 件）と
例外メッセージの `[data]`（1 件）は除外済み。

| ファイル | 行 | 件数 |
|---|---|---|
| `RoundTripTest` | 98, 269, 291, 313, 335, 376, 566, 588, 610, 632 | 10 |
| `YamlFormatReaderTest` | 91, 93, 271, 273, 275, 461, 467, 469, 516 | 9 |
| `YamlFormatWriterTest` | 89, 91, 256, 626, 633, 740 | 6 |
| `XlsFormatReaderRealFileTest` | 291, 591, 724, 725, 726, 727 | 6 |
| `XlsFormatReaderTest` | 280, 579, 644, 645, 646, 647 | 6 |
| `XlsFormatWriterTest` | 197, 530, 950, 1244, 1252 | 5 |
| `YamlFormatReaderRealFileTest` | 965, 1129, 1130, 1131 | 4（`:1129`-`:1131` は 9 値のリスト） |
| `XlsFormatWriterModelTest` | 304, 414 | 2 |
| `TestCoreReaderAdapterTest` | 602, 635, 664, 738 | 4 |
| `YamlTestCoreAdapterTest` | 74, 76 | 2 |
| `YamlFormatWriterModelTest` | 459 | 1 |
| `XlsReferenceFixtureTest` | 344 | 1 |

- `YamlTestCoreAdapterTest:74`・`:76` の 2 件は **生値へ変わる**。
  ユーザー判定（`0d9a049`）で `YamlTestCoreAdapter` の公開メソッドが生値で受け取る
  API になったためである。**「変えない 2 件」と書いた当初の判断は誤り**であった
- `TestCoreReaderAdapterTest:602`・`:635`・`:664` は `readBlockBodyLines` の引数、
  `:738` は `BlockHeader.getGroupId()` の期待値なので、いずれも生値へ変わる

#### (d) 生値化で観測できる出力の変化 1 件（追いかけない）

生値表現にすると、`TABLE[]=x`（空のグループ ID）とグループ ID 省略がどちらも
モデル上 `""` になる。**その結果、`TABLE[]=x` は往復後に `TABLE=x` になる。**
これは内部表現の区別が消えるという話ではなく、**観測できる出力の変化**である。

`5783b35` の `testdata_notation.rst:247`-`:269` に空文字グループ ID の記述はなく、
`src/test` にも `"[]"` を期待する箇所は 0 件（実測）。
**解説書に無い書き方**として追わない（ユーザー判定・2026-08-27。`0d9a049` の 2-3）。

---

## 2. 第2節 7 件の是正結果

**7 件すべて完了。**タスクごとの詳細（変更した箇所・赤→緑・変異・台帳の更新）は
`checks/task-32.md` 〜 `checks/task-36.md` にある。ここには結果と、指示書との食い違いだけを載せる。

| 項 | 内容 | タスク | 結果 |
|---|---|---|---|
| 2-1 | Excel 形式の読み書きを記法⇄値の対称な写像にする | #32 | ✅ 症状 4 件（`null` → 文字列 `null` ／ `"null"` → Java null ／ `"""` の再読込例外 ／ `\r` の 2 文字化）がいずれも実ファイル起点で再現しなくなった |
| 2-2 | 全フィールドが空文字のレコードを Excel 形式へ書き戻せるようにする | #33 | ✅ 読み（`isEmptyCell`）と書き（全要素が空文字のエントリ）を直した。既存テストの期待値変更は **0 件** |
| 2-3 | 中間モデルから Excel 形式の書式（`[ ]`）を外す | #34 | ✅ `[ ]` を知るのは層 A（`XlsFormatWriter#marker` ／ `TestCoreReaderAdapter#markerGroupId`）と層 B（`GroupIdNotation#format` を両アダプタが上流へ渡す直前に呼ぶ）の 2 層だけになった |
| 2-4 | 解説書に記述の無い「あるべき姿」を追う `@Ignore` 2 件を削除する | #35 | ✅ 2 件とも削除。**`@Ignore` は 0 件になり `Skipped` も 0 になった** |
| 2-5 | `YamlTestCoreAdapter#isResourceExisting` の Javadoc とテスト | #36 | ✅ 「入れ物ディレクトリの存在」を返す旨へ直した |
| 2-6 | `YamlFormatReaderScalarTest` のヘルパーに空でないカラムを足す | #36 | ✅ 併せて「すべての値が空文字の行は読み飛ばされる」正のテストを 1 件追加した |
| 2-7 | 空マッピングの行だけが読み飛ばされる形へ期待値を直す | #36 | ✅ メソッド名と Javadoc も欠陥の名前でないものへ改めた |

### 指示書との食い違い 3 件（いずれも実測で訂正した）

| # | 指示書の記述 | 実測 | 出典 |
|---|---|---|---|
| 1 | 2-2 の記載例（`testdata_examples.rst:2237`-`:2260`）のレコードは **3 件** | **2 件**である。当該 list-table は 6 行で、内訳は識別行・ディレクティブ行・名前行・型行・データ行 2 件。**「往復で 1 件消える」という症状の記述そのものは正しい** | `checks/task-33.md`「指示書との食い違い 1 件」 |
| 2 | 2-3 の既存テストの期待値は **44 件** | **62 箇所（56 行・12 ファイル）**である。指示書 §1-2 (c) の表の内訳を足すと 56 行になり、合計欄の数だけが違う。**対象の集合は同じ** | `checks/task-34.md`「既存テストの変更」 |
| 3 | 2-7 で「`YML-04` が**解消済み**」 | **一部だけ解消**である。解消したのは先頭行が空マッピング `{}` の形だけで、本題（カラムが先頭行のキー集合だけで決まり、後続行にしかないカラムが黙って消える）は残っている。根拠は該当 3 件のテストが期待値を変えずに緑のままであること | `checks/task-36.md`「指示書との食い違い 1 件」 |

### 観測できる出力の変化 2 件（どちらも追いかけない）

いずれも 2-3（#34）の生値化に伴うもので、**解説書に無い書き方**である。

1. **`TABLE[]=x`（空のグループ ID）は往復後に `TABLE=x` になる。** 指示書 2-3 が「追わない」と定めている
   （ユーザー判定・2026-08-27）。`testdata_notation.rst:247`-`:269` に空文字グループ ID の記述は無く、
   `src/test` に `"[]"` を期待する箇所も 0 件。
2. **角括弧の無いグループ ID を持つマーカー（`SETUP_TABLEX=T`）のブロックが黙って消える。**
   **指示書が挙げていない 2 件目の変化である。**#34 前は `groupId` ＝ `"X"` のテーブルブロックとして
   読まれていた。`issues.md` **XLS-11** に追記し、担保テストを新しい挙動へ書き直した。

### 付随して解消した課題 2 件（`src/main` を直した副産物）

| 課題 | 何が起きたか |
|---|---|
| **XLS-05**（全カラムが空のデータ行が黙って消える）の一部 | `""`（半角ダブルクォート 2 文字）の行を落としていたのは本体 `PoiXlsReader#isBlankLine` ではなく converter の `isEmptyCell` であった（**原因の帰属が誤っていた**）。#33 で解消。**残るのは「全セルが空セルの行」だけ**で、これは記法の明文どおりであり判定欄は対応不要のまま |
| **XLS-39**（辺③④の指すグループが食い違う） | #34 で推測剥がし（`YamlFormatWriter#rawGroup`）と角括弧付与（`YamlFormatReader#formatGroup`）が両方消え、**4 経路とも同じ値で往復する**ようになった（`groupId` ＝ `"[a]x[b]"` でプローブ実測）。判定欄（保留）は変えていない —— グループ ID に使える文字を定める明文が無いことは今も変わらないため。**申し送りの束に残すかは調整側の判断** |
| **COV-13**（`[` で始まり `]` で終わらないグループ ID の未到達分岐） | 未到達箇所とした `YamlFormatWriter#rawGroup` そのものが #34 で消えた |

## 3. 完了条件3（母集合の4経路）の結果

**母集合 20 件 × 4 経路 ＝ 80 経路すべて合格。`@Ignore` は 0 件。**
担保は新設の `xls/SpecialNotationRoundTripTest`（20 件）。表の行ごと・記載例ごとの合否は
`checks/task-37.md` の 2 つの表にある。

| 母集合 | 件数 | 4 経路の結果 |
|---|---|---|
| `implementation/testdata_notation.rst` の特殊記法の表（Excel `:1356`-`:1391` ／ YAML `:1408`-`:1443`） | 12 行 → **13 件**（改行文字の行を CR と LF の 2 形に分けた） | **52 経路すべて ✅** |
| `implementation/testdata_examples.rst`「null・空文字・改行など特殊な値を記述する」（`:2133`-`:2461`） | 6 小節 → **7 件**（「空文字・改行」の小節は YAML 側の LF 例と全フィールド空文字の例を分けた） | **28 経路すべて ✅** |

**起点はすべて実ファイル**（POI で組んだ `.xlsx` ／ `Files.write` で書いた `.yaml`）であり、
**比較は中間モデルへ入った解釈後の値**である（版面のセル文字列は 1 つもアサートしていない）。
加えて各件で **「Excel 記法の解釈後の値 ＝ YAML 記法の解釈後の値」**（`tools/testdata_converter.rst:14`）も
アサートし、**20 件とも一致した**。

**母集合に含めなかったもの 1 件（開示）**: `testdata_examples.rst` の
「アップロードファイルを指定する」（`:2423`-`:2461`）。対象が `${attach:ファイルパス}` であり、
特殊記法の表（12 行）に無く、`LIST_MAP` のリクエストパラメータの書き方の例であるため。

## 4. 第3節 11 件の結果

**11 件すべて緑。`@Ignore` は 0 件。**担保は新設 2 クラス
（`xls/ExcelOutputDocumentedBehaviorTest` 6 件／`ConverterDocumentedBehaviorTest` 5 件）。
1 件ごとの内容とテスト名は `checks/task-38.md` の表にある。

| 項 | 内容 | 結果 |
|---|---|:-:|
| 3-1 `:53`-`:55` | 検証が変換の処理経路に組み込まれていない（**負**） | ✅ |
| 3-2 `:59` | セルの色・書式・結合セルが往復で落ちる（**負**） | ✅ |
| 3-3 `:59` | YAML のコメントが往復で落ちる（**負**） | ✅ |
| 3-4 `:176` | 変換元が YAML のとき `excludeSheets` が無視される（**負**） | ✅ |
| 3-5 `:233` | `validate` は直下の `.yaml` だけを見る／上位ディレクトリは空リスト | ✅ |
| 3-6 `:251`-`:254` | `withTestShotsHeaderColor` が効く | ✅ |
| 3-7 `:259`-`:262` | `withExpectedHeaderColor` が `EXPECTED_` と `RESPONSE_` の**両方**に効く | ✅ |
| 3-8 `:263`-`:266` | `withOtherHeaderColor` が `MESSAGE` と非 `testShots` の `LIST_MAP` に効き、`testShots` は変わらない | ✅ |
| 3-9 `:275`-`:278` | `withMaxColumnWidthChars` が列幅を打ち切る | ✅ |
| 3-10 `:287`-`:290` | `withDisplayGridlines` が効く（既定はオフ） | ✅ |
| 3-11 `:239` | `ExcelFormatConfig` が YAML 出力に影響しない（**負**） | ✅ |

### 3-1 で分かったこと（指示書の想定より狭い）

`YamlTestDataValidator` の検査 7 項目のうち、**変換を通るのは一部**である。ただし止めているのは
いずれも `YamlTestDataValidator` ではなく上流であり、`:53`-`:55` と矛盾しない（2026-08-28 実測）。

| 検査項目 | 変換元にしたときの結果 | 止めているもの |
|---|---|---|
| **V-COL** フィールド数と要素数の不一致 | **変換できる**（余りは黙って捨てられる。`issues.md` **YML-14**） | —— |
| **V-DKEY** 未知のディレクティブ名 | 変換できない（`YamlSchemaValidationException`） | 上流ローダの**スキーマ検証**（未知のディレクティブ名はスキーマ非適合でもある） |
| **V-FNAME** フィールド名の重複 | 変換できない（`IllegalArgumentException: Duplicate field names are not permitted in a record.`） | 本体（`nablarch-testing`）の**レコード解析** |

そのため 3-1 のテストは **V-COL 1 項目**で押さえている。

## 5. 期待値をわざと崩す確認の結果

**Step 4 全体で 70 件の変異を確認し、すべて FAIL した。**1 件ごとの「崩した内容」は
各 `checks/task-NN.md` の「変異」表にある。

| タスク | 件数 | 対象 |
|---|---:|---|
| #32（2-1） | 15 | 記法⇄値の写像 8 メソッド分 |
| #33（2-2） | 12 | 読み 4・書き 4・往復 4 |
| #34（2-3） | 10 | 辺①②③④・層 A・層 B（Excel／YAML）・往復 2・XLS-11 |
| #36（2-5〜2-7） | 6 | 直した赤 5 件と、追加した正のテスト 1 件 |
| #37（完了条件3） | 9 | 4 経路それぞれ・解釈後の値 2・形式間の一致・記載例 2 |
| #38（第3節） | 18 | 11 件すべて。**負のテスト 4 件（3-2・3-3・3-4・3-11）は 2〜3 か所ずつ** |
| **合計** | **70** | すべて FAIL |

**#35（2-4）はテストを削除するだけのタスクのため変異を取っていない。**代わりに
`mvn -o clean test` の `Skipped` が **2 → 0** になったことで、削除したのがちょうどその 2 件であることを示した。

**変異が緑のままだった例が 1 件あり、それがテストの弱さを暴いた。** #37 の記載例の突き合わせで
`fromXls` と `fromYaml` を入れ替えても落ちなかった。**記載例の突き合わせが形式間の一致を見ていなかった**
ためであり、そのアサートを足したうえで崩し直した（`checks/task-37.md`）。

## 6. 既存テストの期待値を変えた箇所の全件／カバレッジ C0/C1

### 6-1. `src/main` の変更（9 ファイル・新規 1）

```
git diff --stat 60d9a2d..HEAD -- src/main
  core/reader/GroupIdNotation.java        |  54 +++++++   （新規。#34）
  core/reader/TestCoreReaderAdapter.java  |  41 ++++--    （#34）
  core/reader/YamlTestCoreAdapter.java    |  35 ++++-     （#34・#36）
  converter/model/ColumnRowDataBlock.java |   8 +-        （#35。Javadoc のみ）
  converter/model/TestDataBlock.java      |   9 ++        （#34。Javadoc のみ）
  converter/xls/XlsFormatReader.java      | 160 ++++----  （#32・#33）
  converter/xls/XlsFormatWriter.java      | 162 +++++---  （#32・#33・#34）
  converter/yaml/YamlFormatReader.java    |  56 +++---    （#34）
  converter/yaml/YamlFormatWriter.java    |  32 ++--      （#34）
  9 files changed, 439 insertions(+), 118 deletions(-)
```

### 6-2. 既存テストの期待値を変えた箇所（全件）

| タスク | 変えた | 変えなかった | 内訳 |
|---|---:|---|---|
| #32（2-1） | **6 件** | 上記以外の全既存テスト（着手前 605 件のうち 599 件） | `RoundTripTest#nullCell_…`（改称＋期待値）／同クラス Javadoc ／ `XlsFormatWriterTest#roundTripsNullCell…`（改称＋期待値）／`XlsFormatReaderCellTypeTest#readsLiteralNullStringAsString`（改称＋期待値）／`XlsFormatWriterCellTypeTest` の CR 3 件（入力をデータ行の値からカラム名へ移した） |
| #33（2-2） | **0 件** | `""` を含むデータ行を持つ既存テスト 3 件（`XlsFormatReaderTest#readTableNormalizesExcelQuotationNotation` ／ `#readListMapNormalizesExcelQuotationNotation` ／ `#readFixedFileNormalizesExcelQuotationNotation`） | いずれも同じ行に空でないセルがあり、変更前後とも空エントリではない |
| #34（2-3） | **62 箇所（56 行・12 ファイル）** ＋ 改称 4 件 | 0 件 | 導出は `grep -ro '"\[[A-Za-z0-9_]*\]"' src/test --include=*.java \| grep -v '\[no\]\|\[NOTE\]\|\[MARK\]\|\[EMPTY\]\|\[COL\]\|\[ignore\]\|\[data\]' \| wc -l` → 62。ファイル別の内訳は `checks/task-34.md` |
| #35（2-4） | **削除 2 件**（期待値の変更は 0 件） | 他の全テスト | 削除した 2 件と、その `{@link}` 参照 3 か所（うち 1 つは `src/main` の `ColumnRowDataBlock`） |
| #36（2-5〜2-7） | **5 件**（着手時点の赤 5 件） ＋ 改称 3 件 | ヘルパーの呼び出し元 25 か所 | 呼び出し元はいずれも戻り値だけを見ており、`columnNames` や取り出し位置に触れていない |
| #37 ／ #38 | **0 件** | 全既存テスト | どちらも新規クラスの追加だけで、既存ファイルを 1 つも変更していない |

**テストメソッド総数の推移**（`git grep -c '^    @Test' <rev> -- src/test` の合計）:

```
60d9a2d 605  → 1d07d00 614（#32 ＋9）→ 548ca59 626（#33 ＋12）→ 8774d0a 626（#34 ±0）
        → 8258390 624（#35 −2）→ 4ffdf81 625（#36 ＋1）→ 1ba9dfc 645（#37 ＋20）
        → b5f5063 656（#38 ＋11）
```

**新設したテストクラス 5 つ**（`coverage/inventory.md` §4.5 に開示）:
`xls/XlsNotationSymmetryTest` 8 ／ `xls/XlsEmptyEntryTest` 12 ／
`xls/SpecialNotationRoundTripTest` 20 ／ `xls/ExcelOutputDocumentedBehaviorTest` 6 ／
`ConverterDocumentedBehaviorTest` 5 ＝ **51 件**。

### 6-3. カバレッジ C0/C1

**Step 4 の完了時点（`b5f5063`。ワーキングツリーはクリーン）で計測した。**

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
rm -f jacoco.exec
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec
md5sum target/site/jacoco/jacoco.csv   # → 825ff458b5ebff0103030bc6f12bb07f
```

**`coverage/coverage-report.md` は書き換えていない。**同書の全数値は `da66425` の 1 回の実行
（`jacoco.csv` md5 `d28e374e9027ade63d7919f7a7b5826e`）に固定したままである
（`steering.md` Rules「JaCoCo の再計測はしない」の趣旨は、同書が引く行番号を自己無効化させないこと）。
本節の数値は**別の実行**であり、比較のためにここへ置く。

**全体**: 行 **1632/1706 ＝ 95.66%**（C0）／分岐 **761/818 ＝ 93.03%**（C1）。

`coverage-report.md` の 6 区分での比較（`da66425` → `b5f5063`）:

| 区分 | 行（#26） | 行（Step 4 後） | 分岐（#26） | 分岐（Step 4 後） | 分岐の増減 |
|---|---|---|---|---|---|
| ① `XlsFormatReader`（2 クラス） | 192/197 | **204/209** | 115/140 | **117/142** | 未到達 25 → **25**（横ばい） |
| ② `YamlFormatReader`（2 クラス） | 194/194 | **194/194** | 102/102 | **102/102** | 未到達 0 → **0** |
| ③ `XlsFormatWriter`（1 クラス） | 157/158 | **176/177** | 101/104 | **118/126** | 未到達 3 → **8**（＋5） |
| ④ `YamlFormatWriter`（1 クラス） | 157/159 | **150/152** | 86/90 | **81/84** | 未到達 4 → **3**（−1） |
| ⑤ `TestCoreReaderAdapter`（4 クラス） | 93/101 | **99/107** | 29/31 | **33/37** | 未到達 2 → **4**（＋2） |
| ⑥ 中間モデル（12 クラス） | 150/150 | **150/150** | 60/60 | **60/60** | 未到達 0 → **0** |

### 6-4. 下がった箇所（未到達分岐が増えた 8 件）

**いずれも Step 4 で新しく足した判定である。**

**分岐が増えた区分（③・⑤）については、#26 が挙げた未到達分岐が今もそのまま未到達であり、
増分はすべて新しく足した判定で説明がつく**（③ の #26 の 3 件 ＝ `write` の `parent != null` false 側／
`layout` の `instanceof MessageDataBlock` false 側／`isMarkerColumn` の `columnName != null` false 側は、
現在の `:125` ／ `:220` ／ `:597` に対応し、いずれも未到達のまま。⑤ の #26 の 2 件は
`BodyLineCollector` 側で、現在の `:475` ／ `:479` に対応する）。
**① `XlsFormatReader` は未到達 25 件のまま総分岐が 140 → 142 へ増えており、C1 は下がっていない。
ただし行番号が大きく動いたため、25 件が #26 と同一の分岐であることまでは確かめていない（未確認）。**

| 箇所 | 未到達の分岐 | 増えたタスク | 到達させる入力 | 判断 |
|---|---|---|---|---|
| `XlsFormatWriter#appendRecord`（`:451`） | `!valueCells.isEmpty()` の **false 側** | #33 | フィールドを 1 つも持たないレコード | **到達不能**。`RecordLayout` が生成時に `fields` 1 件以上を要求する（`ModelPreconditions#requireNotEmpty`） |
| `XlsFormatWriter#isQuotationWrapped`（`:710`-`:711`） | `:710` は 4 分岐中 1 件未到達（半角クォートで**始まるが終わらない**値）／`:711` は 4 分岐中 3 件未到達（**全角クォートの判定そのものが一度も真にならない**） | #32 | `"abc`（半角で始まり終わらない）、`”abc”`（全角で囲む） | **テストを足す余地がある**。全角で囲んだ値は `XlsNotationSymmetryTest#readsQuotedValueAsPlainValueAndWritesItWithoutQuotes` が**読み**では通しているが、読みで外側が外れるため**書き**（`toCellNotation`）へは届かない。**残課題として開示する** |
| `TestCoreReaderAdapter#markerGroupId`（`:303`） | `last > 0` の false 側／`charAt(last) == ']'` の false 側 | #34 | `TABLE[=x`（`[` で始まり `]` で終わらないグループ ID）、`TABLE[]=x` に相当する 1 文字マーカー | **テストを足す余地がある**。#26 の **COV-13** が `YamlFormatWriter#rawGroup` について挙げていたのと同じ形が、#34 で**この場所へ移った**。`TABLE[]=x` は指示書 2-3 が「追わない」と定めた形であり、**残課題として開示する** |
| `GroupIdNotation#format`（`:52`） | `rawGroupId == null` の **true 側** | #34 | `null` のグループ ID | **到達不能**。中間モデルの `groupId` は `TestDataBlock` が生成時に `null` を拒否し（`TestDataBlock.java:121`）、`BlockHeader.groupId` も非 null。**防御として残している** |

**④ `YamlFormatWriter` は未到達が 1 件減った**（4 → 3）。#34 で `rawGroup`（推測剥がし）ごと削除し、
**COV-13 が挙げていた未到達分岐が消えた**ためである。

**残課題として開示するもの 2 件**（テストを足す余地があり、足していない）:

1. `XlsFormatWriter#isQuotationWrapped` の全角クォート側 3 分岐（#32 由来）
2. `TestCoreReaderAdapter#markerGroupId` の角括弧が閉じていない側 2 分岐（#34 由来。旧 COV-13 の移動先）

どちらも Step 4 の完了条件に含まれておらず、**足すかどうかの判断は調整側に委ねる。**
