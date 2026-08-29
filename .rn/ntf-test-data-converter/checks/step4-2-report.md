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


---

## 2. 第2節 6 件の是正結果

コミットは 6 つ（`d611bec` の次から）。`src/` の差分は各コミットの `git diff --stat` が正である。

| 是正 | タスク | コミット | 変更したファイル | 直す前に落ちたテスト |
|---|---|---|---|---|
| 2-1 Excel 読みの値処理を本体と同じ順序にする | #40 | `4418726`（`b7d2320` の `src/main` を含む） | `core/reader/TestCoreReaderAdapter`（本体パーサ 6 種へインタープリタ列を渡す／`SendSyncBodyCollector` の `super` を `INTERPRETERS` へ）、`xls/XlsFormatReader`（値を器から取り、自前の解釈と死んだコードを削除）、`xls/XlsFormatWriter`（`appendKeyValueRows` を `toCellNotation` 経由へ） | `XlsTrailingNullTest` 5 件（`d611bec` で全件赤）／`TestCoreReaderAdapterTest#readTablesReturnsRawTableData`／`XlsFormatReaderTest#readMapsTableBlockPreservingRawValues` |
| 2-2 マーカーカラムだけに値があるエントリを残す | #41 | `1915207` | `xls/XlsFormatReader#rowCount`（カラム名 0 件のブロックは行を持たない。値は見ない） | `XlsMarkerOnlyEntryTest` 2 件（`d611bec` で赤）／`XlsFormatReaderRealFileTest` 2 件・`XlsReferenceFixtureTest` 1 件（`b7d2320` で赤。**期待値は変えていない**） |
| 2-3 交互記述のシートで警告を出す | #42 | `729347b` | `xls/XlsFormatReader#warnInterleavedBlocks`・`#unreadIdentifiersAfter`・`#isGroupCollected` | `XlsInterleavedBlockTest` 2 件が (i) 警告の件数で赤（実装前に確認） |
| 2-4 `nablarch-testing-yaml` 第2回への追随 | #43 | `f858ae5` | **`src/main` は無変更**（器経由で自動追随する） | 着手時点の赤 4 件（指示書 2-4 の表と全件一致） |
| 2-5 4 経路テストの正解を本体にし、母集合を足す | #44 | `1d572ef` | **`src/main` は無変更** | `SpecialNotationRoundTripTest` の新規 3 件（`d611bec` で赤。§3） |
| 2-6 ソースから解説書への参照をすべて取り除く | #45 | `26701b7` | `src/` 48 ファイルのコメント・Javadoc のみ（コードは 1 文字も変わっていない） | —— |

**`src/main` の変更は 3 ファイルに閉じている**（`TestCoreReaderAdapter` ／ `XlsFormatReader` ／ `XlsFormatWriter`）。
`d611bec..HEAD` の `src/main` 差分に 19 ファイルが並ぶが、そのうち 16 ファイルは #45 のコメント変更だけである。

### 指示書との食い違い 2 件（いずれも実測で訂正した）

| # | 指示書 | 実測 | どうしたか |
|---|---|---|---|
| 1 | §8-5「`b7d2320` に残った死んだコードを消す —— `XlsFormatReader` の `tail`・…」 | **`tail` は死んでいない。**生行から原文（型行・長さ行・名前行）を復元する経路が 4 か所から呼んでいる | `tail` は残し、他の 7 か所（＋インタープリタ定数 3 と import 4）を削除した。指示書が根拠にした `git grep 'private .*(…)\('` は**定義だけ**を探す式で、呼び出しの有無を見ていない |
| 2 | 2-1 の対処 1「本体パーサ 6 種へインタープリタ列を渡す」 | `b7d2320` の時点で**送信同期系だけが渡せていなかった**。`SendSyncBodyCollector` の `super(reader, EMPTY_INTERPRETERS, targetType)` が残っており、S2（送信同期の末尾 `null`）が本体と食い違ったまま | `super` を `INTERPRETERS` へ改めた。セルを解釈するのは読み手であるテンプレート側であって、`onReadLine` 以降しか呼ばれない委譲先ではない |

---

## 3. 2-5 の結果（母集合と 4 経路）

`SpecialNotationRoundTripTest` **26 件**（第1回の 20 件 ＋ #44 で足した 6 件）。**全件緑。**

**正解は変換ツールのリーダではない。** Excel 形式はフレームワーク本体（`PoiXlsReader` ＋ パーサ ＋
インタープリタ列）、YAML 形式は `nablarch-testing-yaml` の `YamlTestDataParser` に読ませた値を正解とする
（テスト専用の `FrameworkOracle` ／ `YamlFrameworkOracle`）。各テストは 4 経路の往復に先立って
**変換ツールが読んだ中間モデルの値を正解と突き合わせる**。

**往復だけでは末尾 `null` の欠陥を検知できない。** 変換ツールが `x,null,null` を `x,null,null` と読み、
書き戻すときも `null` 記法へ戻すため、往復の前後でフレームワークが読む値は変わらないからである。
この突き合わせを入れる前は、`d611bec` でも 26 件のうち落ちたのは 1 件だけだった。

| 母集合 | 件数 | 4 経路 | 備考 |
|---|---:|---|---|
| 特殊記法 12 種（Excel 記法 ⇄ YAML 記法の対） | 13 | 全経路 緑 | 改行は CR と LF に分けて測るため 13 件 |
| 特殊な値の記述例 6 例 | 7 | 全経路 緑 | LF の例は YAML 側だけが持つため別建て |
| **#44 追加**: 末尾 `null`（ファイル F1・F4・F6 ／ 電文 M1） | 2 | 全経路 緑 | `d611bec` では**変換ツールの Excel 読みが本体と一致する**で赤 |
| **#44 追加**: 全カラムの値が空文字のエントリ（テーブル／`LIST_MAP`） | 2 | 全経路 緑 | 第1回はガードのカラムを置いていたため母集合に無かった |
| **#44 追加**: マーカーカラムだけに値があるエントリ | 1 | 全経路 緑 | `d611bec` では赤 |
| **#44 追加**: アップロードファイルの記述例（`LIST_MAP` ＋ `[no]` ＋ `${attach:…}`） | 1 | 全経路 緑 | 第1回は母集合から外していた |

**`d611bec` で落ちる 3 件**（完了条件1。worktree に `d611bec` を出し、新しいテストだけを持ち込んで実測）:

```
Tests run: 26, Failures: 3, Errors: 0, Skipped: 0
  markerOnlyEntryInListMap   変換ツールの Excel 読みが本体と一致する
  trailingNullInFixedFile    変換ツールの Excel 読みが本体と一致する
  trailingNullInMessage      変換ツールの Excel 読みが本体と一致する
```

**送信同期電文（2-1 実測表の S2）は 4 経路の母集合へは入れていない。** 4 経路のうち YAML→XLS→YAML は
送信同期の識別子とグループ ID の対応を要し、母集合の 1 件として組むより直接の突き合わせのほうが
確かめたいことに近いためである。S2 の担保は
`XlsTrailingNullTest#readsTrailingNullsAsEmptyStringInSendSyncMessage`（#40）にある。

---

## 4. 期待値をわざと崩す確認の結果（完了条件4）

**足したテスト 22 件・直したテスト 7 件のすべてについて確認した。変異は計 34 件。**
崩した状態でそのテストだけを実行し `Failures: 1` になることを確認してから元へ戻している。
全件の表は各タスクの `checks/task-4N.md`「完了条件4」にある。

| タスク | 変異 | 内訳 |
|---|---:|---|
| #40 | 7 | `XlsTrailingNullTest` 5 件（本体の値の期待を 1 要素ずつ崩す）／(e) 1・2 の直した期待値 2 件 |
| #41 | 5 | `XlsMarkerOnlyEntryTest` 2 件（本体が読む件数 3 → 2）／文言を書き直した 3 件（行 0 件 → 1 件） |
| #42 | 5 | 警告の件数 1 → 2 ／出力の期待に落ちたブロックを足す ／(iii) の突き合わせ相手を別グループへ ／文言 ／負のテストの 0 → 1 |
| #43 | 10 | 足した 6 件（値・エラー文言）／直した 4 件（いずれも直す前の値へ戻す） |
| #44 | 7 | 足した 6 件（YAML 側の値・パスを変える）／既存 1 件（`javaNull` の期待を `null` → `""`） |

---

## 5. 既存テストの期待値を変えた箇所の全件（完了条件5）

**変えたのは 7 件。変えなかったが結果が緑へ変わったものが 3 件。** テストの総数は 656 → 678（＋22。すべて新規）。

### 変えた 7 件

| # | テスト | 変更前 → 変更後 | 理由 | タスク |
|---|---|---|---|---|
| 1 | `core/reader/TestCoreReaderAdapterTest#readTablesReturnsRawTableData` → `#readTablesReturnsValuesInterpretedByFramework` | 「器から出る値は記法のまま（未加工）」→ 解釈後の値（引用符記法は外側 1 層が外れ、`null` 記法は Java `null`。`${…}` は記法のまま） | 本体にセルを解釈させる配線へ変えたため主張が成り立たない。フィクスチャがセルに Java `null` を入れていた点も直した（実 `PoiXlsReader` は空セルを空文字で返すため、その入力は実在しない） | #40 |
| 2 | `xls/XlsFormatReaderTest#readMapsTableBlockPreservingRawValues` → `#readMapsTableBlockWithFrameworkInterpretedValues` | 同上 | 同上 | #40 |
| 3 | `yaml/YamlFormatReaderInvalidInputTest#fillsMissingRecordFragmentValuesWithEmptyStringInsteadOfNull` | 2 行目の期待 `[a, null, ""]` → `[a, "", ""]` | 末尾側に並んだ `null` と欠損はまとめて空文字になる（yaml 第2回 2-1）。テストの主題（書かれた空文字と欠損が区別できない＝YML-05）は 1 行目が担っており変わっていない | #43 |
| 4 | `yaml/YamlFormatReaderScalarTest#readsUnquotedNullAsJavaNullInRecordFragmentPath` → `#readsTrailingUnquotedNullAsEmptyStringInRecordFragmentPath` | 期待 `null` → `""`。改名 | ヘルパのフィクスチャはフィールド 1 件で、**唯一のフィールドは常に末尾**である | #43 |
| 5 | `yaml/YamlFormatReaderScalarTest#skipsRowWhoseValuesAreAllEmpty` | 期待に全値が空文字の行を戻した | 読み飛ばされるのは空マッピング `{}` の行だけである（yaml 第2回 2-4） | #43 |
| 6 | `yaml/YamlFormatReaderRealFileTest#keepsFwHeaderNamedRecordInSendSyncFromRealYaml` | フィクスチャの `records:` を 2 件 → 1 件。期待するレコード数も 2 → 1 | 電文のレコードレイアウトは 1 つであり、2 件はスキーマ検証で落ちる（yaml 第2回 2-2）。テストの主題（`FW_HEADER` という名前のレコードが落とされないこと）は変えていない | #43 |
| 7 | `xls/SpecialNotationRoundTripTest#binaryFileNotation` | 期待 `"${binaryFile:testdata.bin}"` → `"010203"` | 正解の読み手をフレームワークにしたため、この記法は取得元パス起点で解決されファイル内容の 16 進文字列になる | #44 |

### 変えなかったが結果が緑へ変わった 3 件（指示書 §8-2）

| テスト | 何を変えたか |
|---|---|
| `xls/XlsFormatReaderRealFileTest#dropsMarkerOnlyRowsAsEmptyEntriesInRealBook` | **期待値（行 0 件）は変えていない。** assert メッセージと Javadoc の理由の説明だけを「カラム名を 1 つも持たないブロックはデータ行を持たない」へ書き直した |
| `xls/XlsFormatReaderRealFileTest#dropsMarkerOnlyRowsAsEmptyEntriesInListMapInRealBook` | 同上 |
| `xls/XlsReferenceFixtureTest#readsExcelSavedWorkbookIntoIntermediateModel` | 同上（`expectedRequestParams` の Javadoc） |

### 変えなかったもの（主なもの）

`XlsFormatReaderCellTypeTest` 10 件・`XlsNotationSymmetryTest` 8 件・`XlsEmptyEntryTest` 12 件・
`RoundTripTest` 30 件・`XlsFormatReaderRealFileTest` の残り 21 件・`SpecialNotationRoundTripTest` の
第1回 19 件は、いずれも期待値を触っていない。値の意味が本体と一致した結果として緑のままである。

---

## 6. カバレッジ C0/C1（完了条件7）

**着手前（`d611bec`）と完了時（`26701b7`）の 2 回を、同じ手順で計測して突き合わせた。**

```sh
rm -f jacoco.exec
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec
md5sum target/site/jacoco/jacoco.csv
```

- 着手前（`d611bec`。worktree で計測。赤 4 件を含む実行）: 行 **1632/1706 ＝ 95.66%** ／ 分岐 **761/818 ＝ 93.03%**
  —— これは第1回の報告 §6-3 が記録した値と一致する（同じ手順・同じ対象であることの確認）
- 完了時（`26701b7`。`jacoco.csv` md5 `0ea76427cb5afd322a2c3804ea30bf13`）:
  行 **1632/1704 ＝ 95.77%** ／ 分岐 **763/810 ＝ 94.20%**

**`coverage/coverage-report.md` は書き換えていない**（`steering.md` Rules「JaCoCo の再計測はしない」の趣旨は
同書が引く行番号を自己無効化させないことであり、本節の数値は別の実行である。`checks/` は時点の証拠記録）。

区分別（着手前 → 完了時）:

| 区分 | 行（前） | 行（後） | 分岐（前） | 分岐（後） | 分岐の未到達 |
|---|---|---|---|---|---|
| ① `XlsFormatReader`（2 クラス） | 204/209 | **201/204** | 117/142 | **119/134** | 25 → **15** |
| ② `YamlFormatReader`（2 クラス） | 194/194 | **194/194** | 102/102 | **102/102** | 0 → **0** |
| ③ `XlsFormatWriter`（1 クラス） | 176/177 | **177/178** | 118/126 | **118/126** | 8 → **8** |
| ④ `YamlFormatWriter`（1 クラス） | 150/152 | **150/152** | 81/84 | **81/84** | 3 → **3** |
| ⑤ `TestCoreReaderAdapter`（4 クラス） | 99/107 | **101/109** | 33/37 | **33/37** | 4 → **4** |
| ⑥ 中間モデル（12 クラス） | 163/163 | **163/163** | 70/70 | **70/70** | 0 → **0** |

（⑥ の値が第1回の報告 §6-3 の `150/150` と違うのは区分に入れたクラスの数え方の差である。
本節は**前後とも同じ数え方**で集計しているので、増減の比較には影響しない。）

### 下がった箇所

**無い。全 53 クラスについて、未到達の行も未到達の分岐も 1 件も増えていない**（CSV を 1 行ずつ突き合わせた実測）。

**下がるどころか `XlsFormatReader` が改善している** —— 未到達分岐 **25 → 15**、未到達行 **5 → 3**。
自前の解釈（`interpretValue` ／ `interpretRows` ／ `dropEmptyEntries` 系）を削除して総分岐が 142 → 134 へ減り、
かつ #41・#42 で足した判定にテストが届いているためである。

**残っている未到達 15 分岐は `XlsFormatReader` に集中している。** 第1回の報告 §6-4 が残課題として
開示した 2 件（`XlsFormatWriter#isQuotationWrapped` の全角クォート側 3 分岐／
`TestCoreReaderAdapter#markerGroupId` の角括弧が閉じていない側 2 分岐）は**今回も未到達のまま**であり、
どちらも第2回の完了条件に含まれていない。**足すかどうかの判断は調整側に委ねる。**

---

## 7. 2-6 の件数と抽出方法（完了条件11）

### 着手前（#45 着手時点。#44 完了後）

```
git grep -nE '\.rst|nablarch-document|解説書|出典|根拠:' -- src/   → 157 行 / 42 ファイル
    src/main  64 行 / 18 ファイル
    src/test  93 行 / 24 ファイル
git grep -nE '[A-Za-z]+\.java:[0-9]+' -- src                      →   9 行
```

指示書 §2-6 が挙げた `d611bec` 時点の 167 行・43 ファイルより減っているのは、#40 で
`XlsFormatReader` の死んだコードを Javadoc ごと削除したためである。
**全件の `file:line` は 2-6 のコミット `26701b7` の差分が示す。**

### 作業後

```
git grep -nE '\.rst|nablarch-document|解説書|出典|根拠:' -- src/   → 0
git grep -nE '[A-Za-z]+\.java:[0-9]+' -- src                      → 0
```

### コミット

**`26701b7`（単独）。** 変更は `src/` 48 ファイル・+361／-403 行で、すべてコメントと Javadoc である。
コメント（`//`・`/* */`）を機械的に取り除いて空白を正規化したうえで着手前（`1d572ef`）と突き合わせ、
**コードに差分のあるファイルが 0 件**であることを確認した（手順は `checks/task-45.md`）。

### 残したもの

- 本体スキーマ（`nablarch/test/ntf-testdata-yaml-schema.json`）の description の引用 —— 解説書ではない
- `{@code coverage/issues.md}` ／ `{@code steering.md}` への参照 —— 指示書 2-6 が「根拠の追跡は `.rn/` の
  報告書・台帳で行う」と定めているため
