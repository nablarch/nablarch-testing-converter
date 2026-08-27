# Step 4 報告 — nablarch-testing-converter

指示書: `nablarch-document@origin/ntf-yaml-support` の
`.rn/20260724-ntf-yaml-support/ntf-step4-05-nablarch-testing-converter.md`

参照点: 解説書 `nablarch-document@5783b35` ／ 本モジュール `60d9a2d` ／
`nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@0b3015c`

構成は指示書「6. 報告」の 6 項に従う。**第1節のみ記入済み。第2節以降は未着手。**

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
| Excel 形式 `:1353`-`:1391` | 18 | 全件 OK |
| YAML 形式 `:1405`-`:1443` | 16 | 全件 OK |

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

### 1-2. 2-3「`[ ]` に依存している箇所」の全走査

#### (a) `src/main`

| # | 箇所 | 現行の役割 | 2-3 後 |
|---|---|---|---|
| 1 | `YamlFormatReader.formatGroup:485`-`:488` | YAML 生値に `[ ]` を付けてモデルへ | 付与をやめる |
| 2 | `YamlFormatWriter.rawGroup:479`-`:487` | モデルから `[ ]` を推測剥がし | やめる |
| 3 | `TestCoreReaderAdapter.markerGroupId:282`-`:286` | `TYPE` と `=` の間を切り出す（`[g1]` がそのまま出る） | ここで外す |
| 4 | `XlsFormatWriter.marker:529`-`:531` | `TYPE + groupId + "=" + id` を連結 | ここで付ける |
| 5 | `XlsFormatReader:108`-`:128`・`:145`-`:146`・`:205`-`:206`・`:212`・`:240`・`:267`-`:274` | `header.getGroupId()` を上流 Excel パーサへ渡す | **整形済みが要る**（(b)） |
| 6 | `YamlTestCoreAdapter.readTables:120`・`readFiles:149` | 上流 YAML ビルダへ渡す | **整形済みが要る**（(b)） |
| 7 | `TestCoreReaderAdapter.readBlockBodyLines:264`-`:266` ＋ `BodyLineCollector:453`-`:458` | `markerGroupId` の出力と equals 比較 | 両側が同表現なら生値で可 |
| 8 | `YamlTestCoreAdapter.readSendSyncMessages:179`-`:187` | 生値で比較（上流 `YamlMessageBuilder:156`-`:157`） | 変更不要（既に生値） |
| 9 | `YamlFormatReader.rawGroupsInOrder:427`・`entriesForRawGroup:466` | 送信系は既に生値で走査 | 変更不要。ただし `:299` のモデル格納は `formatGroup(entry)` なので生値へ |
| 10 | `XlsFormatReader.batchKey:637`-`:638` | 内部の重複判定キー | 表現は問わない |
| 11 | Javadoc: `XlsFormatWriter:524`／`TestDataBlock:77`／`YamlTestCoreAdapter:114`・`:143`／`TestCoreReaderAdapter:212`・`:230`-`:231`・`:259` | 整形済み前提の記述 | 書き直し対象 |

#### (b) 指示書の 1 文が上流の契約と両立しない

指示書 2-3 の「**`[ ]` を付けるのは `XlsFormatWriter.marker` の中だけにする**」は、
そのままでは実装できない。変更禁止の依存先 2 つが API 境界で `[case1]` 形式を要求する。

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

**提案（ユーザー未判断）**: 中間モデルは生値で持つ（2-3 の目的はこれ）。
`[ ]` の付け外しは次の 3 境界だけに置き、モデルからは消す。

1. Excel 版面へ書く／から読む —— `XlsFormatWriter.marker`（付ける）・
   `TestCoreReaderAdapter.markerGroupId`（外す）。指示どおり
2. 上流 Excel パーサ呼び出し —— `XlsFormatReader` が生値→整形済みに変換して
   `TestCoreReaderAdapter` へ渡す（`readBlockBodyLines` は生値のまま）
3. 上流 YAML ビルダ呼び出し —— `YamlFormatReader` が生値→整形済みに変換して
   `YamlTestCoreAdapter` へ渡す（送信系は現行どおり生値）

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

- **変えない 2 件** —— `YamlTestCoreAdapterTest:74`・`:76`。
  `YamlTestCoreAdapter.readTables` は上流 `groupMatches` の契約どおり
  整形済みを受ける API のままである
- `TestCoreReaderAdapterTest:602`・`:635`・`:664` は `readBlockBodyLines` の引数、
  `:738` は `BlockHeader.getGroupId()` の期待値なので、いずれも生値へ変わる

#### (d) 付随して見つけた 1 点（追いかけない）

生値表現にすると、`group_id: ""`（Excel の `TABLE[]=x`）とグループ ID 省略が
どちらもモデル上 `""` になり区別できなくなる。現行の整形済み表現では `[]` と `""` で
区別できていた。`5783b35` の `testdata_notation.rst:247`-`:269` に空文字グループ ID の
記述はなく、`src/test` にも `"[]"` を期待する箇所は 0 件（実測）。
**解説書に無い書き方**として追わない。

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
