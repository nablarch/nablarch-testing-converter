# Step 4 報告 — nablarch-testing-converter

指示書: `nablarch-document@origin/ntf-yaml-support` の
`.rn/20260724-ntf-yaml-support/ntf-step4-05-nablarch-testing-converter.md`

参照点: 解説書 `nablarch-document@5783b35` ／ 本モジュール `60d9a2d` ／
`nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@0b3015c`

構成は指示書「6. 報告」の 6 項に従う。**第1節のみ記入済み。第2節以降は未着手。**

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

（未着手）

## 3. 完了条件3（母集合の4経路）の結果

（未着手）

## 4. 第3節 11 件の結果

（未着手）

## 5. 期待値をわざと崩す確認の結果

（未着手）

## 6. 既存テストの期待値を変えた箇所の全件／カバレッジ C0/C1

（未着手）
