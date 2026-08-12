# 現状挙動の課題一覧

フェーズ2（タスク #19〜#26）で「実行して記録した現状の挙動」のうち、仕様として妥当でないと判断したもの、
または挙動を固定できなかったものを記録する。

**本ファイルに記録した課題は、この作業の中では修正しない**（steering Rules フェーズ2）。
`src/main` への変更はゼロである。

## 凡例

| 記号 | 意味 |
|---|---|
| 影響度 高 | 実プロジェクトの Excel テストデータに実在するパターンで、変換結果が黙って変わる |
| 影響度 中 | 実データには現れていないが、変換結果が入力と一致しない |
| 影響度 低 | 記録のみ。仕様として受容できると判断した |
| 未確認 | 挙動を確認・固定できなかったもの |

---

## #19 辺① 軸D（セル種別 17 ケース）で記録した課題

### 前提: 本体リーダの但し書き

`nablarch-testing` の `PoiXlsReader`（クラス Javadoc）は次のとおり明記している。

> EXCELに記述されたテストデータは、すべて文字列書式となっている必要がある。
> 文字列書式以外のデータ書式が存在した場合の動作は保証しない。

`PoiXlsReader#readOneLine` はセル値を `cell.toString()` で文字列化するだけで、セル種別・表示形式による
分岐を持たない。以下 XLS-01〜XLS-03 はいずれもこの「保証しない」範囲の挙動である。
ただし変換ツール（Excel→YAML）は**テストデータを恒久的に別形式へ書き換える**ため、
「保証外だから何が起きてもよい」では済まず、少なくとも検知（WARN）が要るかを判断する必要がある。

### XLS-01 数値セルが `double` の文字列表現になる（影響度 高）

| 入力セル | 中間モデルへ入る値 | 担保テスト |
|---|---|---|
| 数値 `1`（表示形式なし） | `"1.0"` | `XlsFormatReaderCellTypeTest#readsIntegerNumericCellAsDoubleString` |
| 数値 `1.5` | `"1.5"` | `#readsDecimalNumericCellAsDoubleString` |
| 数値 `12345678901234567890` | `"1.2345678901234567E19"` | `#readsLargeNumericCellAsScientificNotation` |
| 数値 `1`・**表示形式 `@`（テキスト）** | `"1.0"` | `#readsTextFormattedNumericCellAsDoubleString` |

- 原因: `PoiXlsReader#readOneLine` の `cell.toString()` が、数値セルに対して
  POI の `XSSFCell#toString()` → `getNumericCellValue() + ""` を返す。表示形式は参照されない。
- 桁数の大きい数値は指数表記になり、元の桁を復元できない。
- 判断: **仕様として不適切**。少なくとも変換時に「文字列書式でないセルがある」ことを検知して WARN を出すべき。
  修正はこの作業では行わない。

#### 実データでの再現（参照フィクスチャによる実測・2026-08-12）

同梱した参照フィクスチャ `ProjectActionRequestTest.xlsx`（`nablarch-example-web` `origin/main` の
Excel 保存物）を含む同ブランチの `.xlsx` 6 ファイルを POI で全走査した結果は次のとおり。

| ファイル | 数値セル | うち表示形式 `@` |
|---|---|---|
| `AuthenticationActionRequestTest.xlsx` | 5 | 5 |
| `ClientActionTest.xlsx` | 0 | 0 |
| `IndustryActionTest.xlsx` | 0 | 0 |
| `ProjectActionRequestTest.xlsx` | 26 | 25 |
| `ProjectBulkActionRequestTest.xlsx` | 3 | 3 |
| `ProjectUploadActionRequestTest.xlsx` | 5 | 5 |
| 合計 | 39 | 38 |

- **表示形式 `@` 付きの数値セル 38 件はすべて `LIST_MAP` のマーカー列 `[no]` の値**である
  （例: `ProjectActionRequestTest.xlsx` の `downloadNormal` シート `A19` ＝
  `<c r="A19" s="37"><v>1</v>`、`cellXfs[37]` の `numFmtId="49"` ＝ builtin `@`）。
  マーカー列は `HeaderLine#getEffectiveColumnNames()` が除外するため中間モデルには入らない
  （steering #15 の完了条件どおりの意図した除外）。したがって **この 38 件は変換結果を壊さない**。
- **データ列に置かれた数値セルは 39 件中 1 件だけ**存在する。
  `ProjectActionRequestTest.xlsx` の `downloadNormal` シート `M14` ＝
  `<c r="M14" s="55"><v>2000</v>`（`t` 属性なし＝数値セル、`cellXfs[55]` は `numFmtId="0"` ＝ General）。
  `SETUP_TABLE[1]=PROJECT` の `COST_OF_GOODS_SOLD` 列に当たる。
  同じ行の `SALES`／`SGA`／`ALLOCATION_OF_CORP_EXPENSES` は文字列セルで `"1000"`／`"3000"`／`"4000"`
  のまま入るのに対し、**`COST_OF_GOODS_SOLD` だけが `"2000.0"` になる**。
  Excel の画面表示は `2000`。DB 投入値が `2000.0` になり数値カラムでなければ壊れる。
  担保テスト: `XlsReferenceFixtureTest#readsExcelSavedWorkbookIntoIntermediateModel`。
- したがって影響度「高」は維持する。ただし**実データでの発現箇所は 1 件**であり、
  「表示形式 `@` 付き数値セルが 38 件ある」ことをもって影響が広いとは言えない
  （マーカー列に集中しているため）。この訂正はコーディネータの判断材料として記録する。

### XLS-02 日付・時刻・日時セルがセルの表示形式を無視し、ロケール依存の表記になる（影響度 中）

| 入力セル | 中間モデルへ入る値（既定ロケール `en`） | 担保テスト |
|---|---|---|
| 日付 2026-08-07・表示形式 `yyyy/mm/dd` | `"07-Aug-2026"` | `XlsFormatReaderCellTypeTest#readsDateFormattedCellAsPoiDefaultDatePattern` |
| 時刻 シリアル値 `0.5`（12:00:00）・表示形式 `hh:mm:ss` | `"31-Dec-1899"` | `#readsTimeFormattedCellLosingTimeComponent` |
| 日時 2026-08-07 12:34:56・表示形式 `yyyy/mm/dd hh:mm:ss` | `"07-Aug-2026"` | `#readsDateTimeFormattedCellLosingTimeComponent` |

- 原因: POI の `XSSFCell#toString()` が、日付書式付き数値セルに対して
  `new SimpleDateFormat("dd-MMM-yyyy")` で文字列化する。セルの表示形式は使われない。
- **ロケール依存**: `SimpleDateFormat` の既定コンストラクタは既定ロケールを使うため、
  同じセルが実行環境のロケールで変わる（`-Duser.language=ja -Duser.country=JP` では
  `07-8-2026` になることを JDK 17 で実測）。
- **タイムゾーンには依存しない**: POI は日付セルの往復（`DateUtil#getExcelDate` ↔ `getJavaDate`）に
  同一の既定タイムゾーンを使い、書き込み時と読み取り時のずれが相殺される。
  `UTC`／`America/Los_Angeles`／`Pacific/Kiritimati`／`Europe/Istanbul` で実測して確認した。
- **時刻成分が失われる**: 時刻セルは日付部（Excel シリアル値 0.5 → 1899-12-31）だけが残り、
  時刻情報が完全に消える。日時セルも時刻部が消える。
- 担保テストは既定ロケールを `Locale.ENGLISH` に固定して値を確定させている。
  **ロケールを固定しなければ値は固定できない**（この点自体が課題である）。
- 判断: **仕様として不適切**。修正はこの作業では行わない。

### XLS-03 数式セルが計算結果ではなく数式文字列になる（影響度 中）

| 入力セル | 中間モデルへ入る値 | 担保テスト |
|---|---|---|
| 数式 `=1+1` | `"1+1"` | `XlsFormatReaderCellTypeTest#readsFormulaCellAsFormulaText` |

- 原因: POI の `XSSFCell#toString()` が数式セルに対して `getCellFormula()`（先頭 `=` なし）を返す。
- Excel の画面表示は計算結果 `2` だが、変換後は `1+1` という文字列になる。
- 実データに数式セルは 0 件（steering Decisions）。
- 判断: **仕様として不適切**（画面表示と一致しない）。実データに存在しないため優先度は低い。
  修正はこの作業では行わない。

### XLS-04 セル不在・空白セル・空文字セルが中間モデル上で区別されない（影響度 低・記録のみ）

| 入力セル | 中間モデルへ入る値 | 担保テスト |
|---|---|---|
| セル不在・行末（その位置から右にセルがない） | `""` | `XlsFormatReaderCellTypeTest#readsAbsentCellAsEmptyString` |
| セル不在・行の途中（右隣にセルがある＝穴） | `""` | `#readsAbsentCellInMiddleOfRowAsEmptyString` |
| 空白セル（セルはあるが値なし） | `""` | `#readsBlankCellAsEmptyString` |
| 空文字の文字列セル | `""` | `#readsEmptyStringCellAsEmptyString` |

- 4 者はいずれも `""` になる。Excel 上で区別できない以上、これは受容できる挙動である。
- ただし**リーダ内の経路は 2 つに分かれる**。行の途中の不在セルは
  `PoiXlsReader#readOneLine`（L123）の `cell == null ? ""` を通る（読み取った行が `[k, "", z]`）。
  行末の不在セルは行の使用範囲（`Row#getLastCellNum()`）自体が縮むためこの分岐に到達せず
  （読み取った行は `[k]`）、`""` は下流の行パディング由来である。実測で両者を確認した。
- ただし **Fake リーダ経路（`XlsFormatReaderTest` の既存 33 件）とは挙動が異なる**。
  Fake リーダは `List<List<String>>` の要素に Java の `null` を直接置けるため、
  `XlsFormatReaderTest#readMapsTableBlockPreservingRawValues` は `null` が `null` のまま入ることを
  アサートしている。実 `.xlsx` 経路では `null` は生じない。
- 判断: 実挙動として妥当。**ただし「実ファイル経路では中間モデルに `null` セルは現れない」ことを
  前提にしてよいかはコーディネータの判断が要る**（辺③④の `null` 表現・軸C の扱いに影響する）。
  #20 以降で参照すること。

### XLS-05 全カラムが空のデータ行が黙って消える（影響度 高）

| 入力 | 中間モデルへ入る結果 |
|---|---|
| `SETUP_TABLE=T`／カラム行 `A`,`B`／データ行 `x1`,`y1`／データ行 `""`,`""`／データ行 `x3`,`y3` | 行が **2 件**（`[x1, y1]`, `[x3, y3]`）。空のデータ行は消える |

- 原因: `PoiXlsReader#readLine`（L83-99）が `isBlankLine`（L136-143）で
  「全要素が空文字の行」を読み飛ばす。ブロック区切りとしての空行と、
  **全カラムが空のデータ行**が区別されない。
- 実測（プローブ実行・2026-08-12）: 上表のとおりデータ行 3 件のうち 1 件が消えた。
  警告は一切出ない。
- 影響: NULL 許容カラムだけからなる行、あるいは全カラムが空文字のレコードを持つテストデータは、
  Excel→YAML 変換で**黙って 1 行減る**。変換後にテストが通ってしまえば発見できない。
- 本タスクへの現れ方: `XlsFormatReaderCellTypeTest` は検証対象セル（`V` 列）が空になるケース
  （D1-12・D1-13・空白セル）で行全体が空になり読み飛ばされるため、行を空にしない `KEY` 列を
  必ず置いて回避している。**この回避の必要性そのものが本課題の現れである。**
- 判断: **仕様として不適切**（データ損失）。少なくとも WARN が要る。
  修正はこの作業では行わない（`src/main` 無変更）。

### 課題としないと判断した観測結果

| 入力セル | 中間モデルへ入る値 | 判断 |
|---|---|---|
| 文字列 `abc` | `"abc"` | 妥当 |
| 文字列 `007`（先頭ゼロ） | `"007"` | 妥当（文字列セルなら先頭ゼロは保たれる） |
| 真偽値 `TRUE` | `"TRUE"` | 保証外だが表示と一致する。実データ 0 件。妥当 |
| エラー値 `#DIV/0!` | `"#DIV/0!"` | 例外にならず表示と一致する。実データ 0 件。妥当 |
| 前後に空白を持つ文字列 `␣␣pad␣␣` | `"  pad  "` | 妥当（トリムされない） |
| 改行を含む文字列 | 改行を含んだまま 1 値 | 妥当 |
| リテラル文字列 `null` | `"null"` | 妥当（Excel 経路で `null` へ戻せないことは `XlsFormatWriter` の Javadoc に既記） |

### 未確認

**「未確認なし」は撤回する（2026-08-12・レビュー指摘による訂正）。**

軸D の 17 ケースそのものは、POI 生成フィクスチャを入力にすべて挙動を確認しテストで固定した
（`XlsFormatReaderCellTypeTest` 19 件＝17 ケース＋空白セル 1 件＋行途中の不在セル 1 件）。
未確認なのは **「POI 生成物と Excel 保存物の読み取り結果が同一か」の射程**である。

#### 確認できた範囲

参照フィクスチャ `ProjectActionRequestTest.xlsx`（Excel 保存物）の `downloadNormal` シートについて、
テストソースに直書きした論理内容と読み取り結果が一致することを確認した
（`XlsReferenceFixtureTest#readsExcelSavedWorkbookIntoIntermediateModel`）。
さらに同じ論理内容を `XlsFixture` でセル種別を明示して組み立てた POI 生成ブックが、
同じ中間モデルになることを確認した
（`XlsReferenceFixtureTest#poiGeneratedWorkbookReadsIdenticallyToExcelSavedWorkbook`）。
確認できたセル種別は次の 3 つだけである。

| セル種別 | Excel 保存物側の実体 | POI 生成側の宣言 |
|---|---|---|
| 文字列セル | 共有文字列 `t="s"` | `text(...)` |
| 数値セル（表示形式 General） | `<c r="M14" s="55"><v>2000</v>` | `number(2000)` |
| 数値セル（表示形式 `@`） | `<c r="A19" s="37"><v>1</v>` | `number(1, "@")` |
| 空白セル（値なし・書式のみ） | `<c>` に `<v>` なし | `blank()` |

#### 確認できなかった範囲（未確認）

以下のセル種別は**参照フィクスチャ（およびユーザーが実測した `nablarch-example-web` の Excel 6 ファイル
すべて）に 1 件も存在しない**ため、Excel 保存物と POI 生成物の読み取り結果が同一であることを
突き合わせられなかった。これらのケース（D1-06〜D1-11）の挙動は **POI 生成物でのみ**確認したものであり、
Excel が保存した同種のセルで同じ結果になる保証はない。

| 未確認のセル種別 | 該当ケース |
|---|---|
| 日付書式付き数値セル | D1-06 |
| 時刻書式付き数値セル | D1-07 |
| 日時書式付き数値セル | D1-08 |
| 数式セル | D1-09 |
| 真偽値セル | D1-10 |
| エラーセル | D1-11 |

- 参照フィクスチャは 26 シートのうち `downloadNormal` 1 シートのみを検証対象とした。
  残り 25 シートの読み取り結果は未確認である（ブロック構成は同型であり、
  数値セルを含むのは検証対象シートだけであることは全ブック走査で確認済み）。
- steering Assumptions の例外は「実物 `.xlsx` 1 本のみ」であり、上記の未確認範囲を埋めるには
  日付・数式・真偽値・エラーセルを含む Excel 保存物を追加同梱する必要がある。
  同梱本数を増やすかどうかは**コーディネータの判断が要る**。
