# Step 4 第2回 報告 —— nablarch-testing-converter

指示書: `nablarch-document` `origin/ntf-yaml-support` の
`.rn/20260724-ntf-yaml-support/ntf-step4-07-nablarch-testing-converter-2.md`

参照点: 解説書 `a6da1f6` ／ 本モジュール `d611bec` ／ `nablarch-testing` `3c4bd2a` ／
`nablarch-testing-yaml` `3fecc4e`

---

## 0. 着手前の実測（完了条件8の着手時点）

`mvn -o clean test` → `Tests run: 656, Failures: 3, Errors: 1, Skipped: 0` ／ `BUILD FAILURE`。
赤は次の4件のみで、指示書 2-4 の表と全件一致する。指示書に載っていない赤は無い。

```
YamlFormatReaderInvalidInputTest.fillsMissingRecordFragmentValuesWithEmptyStringInsteadOfNull:763
YamlFormatReaderScalarTest.readsUnquotedNullAsJavaNullInRecordFragmentPath:650
YamlFormatReaderScalarTest.skipsRowWhoseValuesAreAllEmpty:596
YamlFormatReaderRealFileTest.keepsFwHeaderNamedRecordInSendSyncFromRealYaml:640
```

---

## 1. 2-1・2-3 の「着手前に特定すること」

### 測り方

(a)〜(d) は推測ではなく実測で決めた。2-1 の配線変更（本体パーサへ
`NullInterpreter`→`QuotationTrimmer`→`LineSeparatorInterpreter` を渡し、converter 側の
`interpretValue`／`interpretRows` を外す）を作業ツリーへ入れ、同じ入力を

- **変更前**（`d611bec` の `src/main`）
- **変更後**（配線変更後）
- **本体 `nablarch-testing` 直呼び**（`PoiXlsReader` ＋ 同じインタープリタ3本。正解）

の3者で読み比べた。使った実 `.xlsx` は POI で組み立てたもの。

### 2-1 (a) ディレクティブ値

入力（`SETUP_VARIABLE` の実 `.xlsx`）と3者の実測値:

| ディレクティブ | Excel セル | 本体（正解） | 変更前 | 変更後（配線のみ） | 判定 |
|---|---|---|---|---|---|
| `text-encoding` | `UTF-8` | `UTF-8` | `UTF-8` | `UTF-8` | 変化なし |
| `file-type` | `Variable` | `Variable` | `Variable` | `Variable` | 変化なし |
| `field-separator` | `\t`（2文字） | 実タブ | `\t` | `\t` | 変化なし（`normalizeSeparator` が記法へ戻す） |
| `record-separator` | `\r`（2文字） | `""`（空。`setDirective` の `trim()` で CR が落ちる） | `\r` | `NONE` | **変わる。変更後が本体と同義**（`normalizeSeparator("")` → `NONE`） |
| `quoting-delimiter` | `"""""`（引用符5個） | `"""`（1層だけ剥がれる） | `"""` | `"` | **変わる。二重適用**（本体が1層＋`normalizeDirectiveValue` がもう1層） |

**対処**

| # | 何が変わるか | どう対処するか |
|---|---|---|
| a-1 | `normalizeDirectiveValue:486`-`:500` の `QuotationTrimmer` 相当が二重適用になる（`"""""` → 本体 `"""` に対し converter `"`） | `isQuotationWrapped`／`stripQuotes` の呼び出しを外す。`normalizeSeparator` だけ残す。外したあと `quoting-delimiter` は `"""` になり本体と一致する |
| a-2 | `record-separator`・`field-separator` に `LineSeparatorInterpreter` が掛かる | 対処不要。器の値は本体と同じになり、`DirectiveUtil.normalizeSeparator` が記法（`NONE`／`CRLF`／`\t` 等）へ戻す。中間モデルの値は本体の意味と一致する |
| a-3 | 書き戻し（`XlsFormatWriter.appendKeyValueRows:469`）が値を素で書くため、解釈後の値になった中間モデルを読み戻すと意味がずれる（模型 `"""` → セル `"""` → 本体 `"`） | データ行と同じ `toCellNotation:685` を通す。`toCellNotation` 自体は変えない（指示書 2-1 の4） |

### 2-1 (b) FW 制御ヘッダ値

入力（`MESSAGE` の実 `.xlsx`。`requestId` に引用符記法、`userId` に 2 文字 `\r`）:

| キー | Excel セル | 本体（正解） | 変更前 | 変更後 | 判定 |
|---|---|---|---|---|---|
| `requestId` | `"R1"` | `R1` | `"R1"` | `R1` | **変わる。変更後が本体と一致** |
| `userId` | `\r`（2文字） | CR | `\r` | CR | **変わる。変更後が本体と一致** |
| `requestId` | `null` | `IllegalStateException`（`directive or data names row must have two columns at least. [requestId]`） | 文字列 `null` を保持 | 同じ例外 | **変わる。変更後が本体と一致**（仕様外入力。指示書 §5） |

**対処**

| # | 何が変わるか | どう対処するか |
|---|---|---|
| b-1 | `readMessageBlock:246`-`:248` のコメント「本体が生文字列として返すため `stripQuotes` は適用しない」が成り立たなくなる（本体が解釈済みの値を返すようになる） | コード変更は不要（元から `stripQuotes` を呼んでいない）。コメントを実態に合わせて書き直す。値は本体と一致する |
| b-2 | 書き戻しが素書きのため、解釈後の FW ヘッダ値を読み戻すと意味がずれる（`R1` は無害だが、値が `"R1"` の場合に崩れる） | a-3 と同じ経路（`appendKeyValueRows`）なので a-3 で同時に解決する |
| b-3 | FW ヘッダ値を `null` 記法で書くと例外になる | 対処しない。仕様外入力であり、変更後は本体と同じ例外になる（指示書 §5） |

### 2-1 (c) 空エントリ判定

**実測**: 本体に解釈させると `dropEmptyEntries:622` が解釈後の値で判定することになり、次の2つが壊れる。

- 全セルが `null` 記法の行 → 解釈後は全要素 Java `null` → `isEmptyCell` が真 → **行ごと消える**
  （現行 Javadoc `:612`-`:617` が「全セルが `null` 記法の行は読み飛ばさない」と明記している挙動と逆）
- 全セルが `""`（空文字記法）の行 → 解釈後は全要素空文字 → **行ごと消える**
  （`XlsEmptyEntryTest` の 5 件が落ちる）

一方、本体は `PoiXlsReader.readLine:93` が `isBlankLine:140`-`:147`（生セルの `isEmpty()`）で
**全セルが空の行を先に落とす**ため、converter へ届く時点で「本当に空の行」は存在しない。

| # | 何が変わるか | どう対処するか |
|---|---|---|
| c-1 | `dropEmptyEntries` が解釈後の値で判定するようになり、`null` 記法だけの行・`""` だけの行を落としてしまう | `dropEmptyEntries:622`・`isEmptyEntry:639`・`isEmptyCell:668` を**削除する**。全セル空の行は本体が先に落とし、マーカーカラムだけに値がある行は 2-2 のとおり残すのが正しいため、converter 側の判定は不要になる |

削除の実測結果（作業ツリーで確認済み）:
- `XlsEmptyEntryTest` 12 件すべて緑（削除前は 5 件赤）
- `RoundTripTest.nullCell_isPreservedInBothPaths`・`XlsFormatWriterTest.roundTripsNullCellAsJavaNull`・
  `XlsNotationSymmetryTest.readsNullNotationAsJavaNull` の 3 件も緑（削除前は `IndexOutOfBounds`）
- 代わりに 2-2 の期待値どおり赤くなるのが 3 件（下の (e) 表 3〜5）

### 2-1 (d) 生行と器の行数の対応

**実測**: 崩れない。2 断片・末尾 `null` 行・全 `null` 行を含む `SETUP_FIXED` を読ませ、
`records=2` ／ 断片1が 3 フィールド 2 行（`x,"",""` と `"","",""`）／ 断片2が 2 フィールド 1 行（`p,q`）で
生行と一致した。

理由（本体 `3c4bd2a`）:
- 行が消える判定は 2 か所とも**解釈前の生セル**で行われる ——
  `PoiXlsReader.readLine:93`（`isBlankLine`）と `TestDataParsingTemplate.readTestData:180`（`isBlankLine`）。
  解釈は `:183` で**そのあと**に来る。したがって解釈によって行数は変わらない
- 値行の判定 `DataFileParser.isDataRow:215`-`:221` は「行が空（要素数0）」または「先頭セルが空」を
  データ行とみなす。全セルが `null` 記法の行は解釈後 `trimTailCopy` で空リストになるが、
  `:216`-`:218` によりデータ行のまま扱われる。行は落ちない

| # | 何が変わるか | どう対処するか |
|---|---|---|
| d-1 | 行数の対応は変わらない | 対処不要。`requireLine:448` の位置決めはそのまま使える。値だけ `FragmentView.getValues` から取る |
| d-2 | データ行の先頭列（マーカーカラム）に `null` 記法を書いた場合、解釈後 Java `null` になり `isDataRow` が真になる（変更前は文字列 `null` のため新しい名前行と誤認していた） | 対処しない。仕様外入力であり、変更後は本体と同じ解析になる（指示書 §5） |

### 2-1 (e) 既存テストで生値（解釈前）を期待しているもの

指示書が挙げた 7 クラス（`@Test` 計 140 件）＋ 波及した `XlsReferenceFixtureTest` を
2-1・2-2 適用後の作業ツリーで全件実行した。**変える 5 件／変えない 135 件**（7 クラス内訳は
変える 4 件・変えない 136 件、加えてクラス外 1 件）。

| # | クラス | テスト | 何が起きるか | 変える理由 |
|---|---|---|---|---|
| 1 | `TestCoreReaderAdapterTest` | `readTablesReturnsRawTableData:127` | `InterpretationFailedException`（`QuotationTrimmer` に Java `null` が渡る） | フィクスチャ `FakeTestDataReader` がセルに Java `null` を入れている。実 `PoiXlsReader` は `readOneLine:123` で空セルを `""` にするため**この入力は実在しない**。セルを `""` に直し、期待値を解釈後（`${userName}` はそのまま・`""` はそのまま）へ改める |
| 2 | `XlsFormatReaderTest` | `readMapsTableBlockPreservingRawValues:144` | 同上 | 同上。テスト名・Javadoc の「記法のまま（未加工）」も実態と合わなくなるので書き直す |
| 3 | `XlsFormatReaderRealFileTest` | `dropsMarkerOnlyRowsAsEmptyEntriesInRealBook:384` | 期待 0 件に対し 2 件（`[]`,`[]`） | 2-2 の是正対象そのもの。マーカーカラムだけに値があるエントリは残すのが解説書 `tools/testdata_converter.rst:63` の定め。期待値を 2 件へ改める |
| 4 | `XlsFormatReaderRealFileTest` | `dropsMarkerOnlyRowsAsEmptyEntriesInListMapInRealBook:408` | 期待 0 件に対し 2 件 | 同上（`LIST_MAP` 経路） |
| 5 | `XlsReferenceFixtureTest` | `readsExcelSavedWorkbookIntoIntermediateModel:158`（`block[3].rows`） | 期待 `[]` に対し `[[]]` | 同上。参照フィクスチャにマーカーカラムだけのブロックが含まれるため |

**変えない**（主なもの）: `XlsFormatReaderCellTypeTest` 10 件・`XlsNotationSymmetryTest` 8 件・
`XlsEmptyEntryTest` 12 件・`RoundTripTest` 30 件・`XlsFormatReaderRealFileTest` の残り 21 件は
すべて緑のまま。これらは値の意味が本体と一致した結果であり、期待値を触る必要が無い。

**3〜5 に残る判断（調整側へ）**: 3〜5 はいずれも「**全カラムがマーカーカラム**」という退化形で、
マーカーカラムを除いたエントリが**セル 0 個**になる。解説書 `:63` は「マーカーカラムの値だけを除いた
エントリとして残す」と定めており、他のカラムが 1 つも無い場合の記述は無い。本報告では解説書どおり
「残す」（セル 0 個のエントリが本体の読む件数だけ並ぶ）を採る。**解説書を直す提案はしない。**

### 2-3 交互記述で (ii) が現状で成り立つか

**成り立つ。** 実 `.xlsx` を 2 形で組み、converter の出力を実測した。

| 入力（記述順） | converter の出力 | 判定 |
|---|---|---|
| `SETUP_TABLE=A` ／ `SETUP_TABLE[g1]=B` ／ `SETUP_TABLE=C`（グループIDが交互） | `A`（group `""`）と `B`（group `g1`）の 2 ブロック。**`C` は出力されない** | (ii) 成立 |
| `EXPECTED_TABLE=A` ／ `EXPECTED_COMPLETE_TABLE=B` ／ `EXPECTED_TABLE=C`（データタイプが交互） | `A`（`EXPECTED_TABLE_DATA`）と `B`（`EXPECTED_COMPLETED`）の 2 ブロック。**`C` は出力されない** | (ii) 成立 |

本体 `TestDataParsingTemplate.doParse:284`-`:310` が、対象ブロックを読み始めたあと（`nowReading`）に
別キーのマーカー行へ当たると `:303`-`:307` で `break` する。converter は
`XlsFormatReader.read:106`-`:137` が (データタイプ, グループID) ごとに 1 回だけ本体パーサを呼ぶため、
本体の `break` がそのまま効く。**converter 側で選別する必要は無く、足すのは警告だけ**（指示書 2-3 の2）。

### 2-6 取り除く行の件数（着手前）

抽出式と件数（`d611bec`。ディレクターの実測値と一致）:

```
git grep -nE '\.rst|nablarch-document|解説書|出典|根拠:' -- src/   → 167 行 / 43 ファイル
  うち src/main  71 行 / 19 ファイル
      src/test   96 行 / 24 ファイル
git grep -nE '[A-Za-z]+\.java:[0-9]+' -- src                      →  11 行
```

全件の `file:line` は 2-6 のコミットの差分で示す。
