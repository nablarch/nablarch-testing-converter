# 現状挙動の課題一覧

フェーズ2（タスク #19〜#26）で「実行して記録した現状の挙動」のうち、仕様として妥当でないと判断したもの、
または挙動を固定できなかったものを記録する。

**本ファイルに記録した課題は、この作業の中では修正しない**（steering Rules フェーズ2）。
`src/main` への変更はゼロである。

## 凡例

| 記号 | 意味 |
|---|---|
| 影響度 高 | 変換結果が入力と一致せず、値が化ける・行が消えるなど**データが黙って変わる**。`nablarch-example-web`（サンプルアプリ）の 6 ファイルでの発現有無は影響度の根拠にしない（対象PJの実データでの発現は未知） |
| 影響度 中 | 変換結果が入力と一致しないが、`nablarch-example-web`（サンプルアプリ）の 6 ファイルでは発現を確認していない（対象PJの実データに無いことは意味しない） |
| 影響度 低 | 記録のみ。仕様として受容できると判断した |
| 未確認 | 挙動を確認・固定できなかったもの |

**「影響度 高」の定義を訂正した（2026-08-12・ユーザー指摘による訂正）。** 当初は「実プロジェクトの Excel
テストデータに実在するパターンで」としていたが、`nablarch-example-web` は**サンプルアプリであって対象PJの
実データではない**（同じ誤りを「影響度 中」の定義では既に訂正済み）。実在するかどうかは影響度の根拠に
できないため、定義から外して「影響度 中」と同じ言い回しに揃えた。XLS-05 は「高」だが `nablarch-example-web`
での発現は確認していない。

### 並び順の原則（2026-08-12・ユーザー指摘による訂正）

**この一覧は影響度の大小ではなく「検出できるか（変換ミスが後段のテスト失敗として現れるか）」を優先して並べる。
検出できないものを上に置く。**

変換結果の値が入力と違えば、その値を使う後段のテストが FAIL するので気づける。
一方、行そのものが消える・要素が黙って落ちる類の欠落は、変換後にテストが通ってしまえば誰も気づかない。
後者のほうが先に読まれるべきである。

例: XLS-01（数値セルが `"1.0"` になる）は値が違えばテストが FAIL して気づけるが、
XLS-05（全カラムが空のデータ行が消える）は行が消えても誰も気づかない。
影響度はどちらも「高」だが、**検出できないこと**を理由に XLS-05 を先に置く。

**課題 ID は発見順のまま振り直さない**ため、以下では ID の昇順と掲載順は一致しない。

---

## #19 辺① 軸D（セル種別 17 ケース）で記録した課題

**掲載順を訂正した（2026-08-12・ユーザー指摘による訂正）。** 当初は ID 昇順（XLS-01 → XLS-05）で並べていたが、
「凡例 → 並び順の原則」のとおり**検出できないものを上に置く**方針に改め、XLS-05 を先頭に移した。
課題 ID は振り直していない。

### XLS-05 全カラムが空のデータ行が黙って消える（影響度 高・**検出できない**）

| 入力 | 中間モデルへ入る結果 |
|---|---|
| `SETUP_TABLE=T`／カラム行 `A`,`B`／データ行 `x1`,`y1`／データ行 `""`,`""`／データ行 `x3`,`y3` | 行が **2 件**（`[x1, y1]`, `[x3, y3]`）。空のデータ行は消える |

- 原因: `PoiXlsReader#readLine`（L83-98）が `isBlankLine`（L140-147）で
  「全要素が空文字の行」を読み飛ばす。ブロック区切りとしての空行と、
  **全カラムが空のデータ行**が区別されない。
  **後続の XLS-01〜XLS-03 とは別件である**（セル書式の但し書きとは無関係で、行の読み飛ばしが原因）。
- 実測（プローブ実行・2026-08-12）: 上表のとおりデータ行 3 件のうち 1 件が消えた。
  警告は一切出ない。
- 影響: NULL 許容カラムだけからなる行、あるいは全カラムが空文字のレコードを持つテストデータは、
  Excel→YAML 変換で**黙って 1 行減る**。変換後にテストが通ってしまえば発見できない。
- **本課題を先頭に置く理由**: 値が変わる課題（XLS-01 など）は値が違えばテストが FAIL して気づけるが、
  本課題は行が消えても誰も気づかない。影響の大きさではなく**検出できないこと**が理由である。
- 本タスクへの現れ方: `XlsFormatReaderCellTypeTest` は検証対象セル（`V` 列）が空になるケース
  （D1-12・D1-13・空白セル）で行全体が空になり読み飛ばされるため、行を空にしない `KEY` 列を
  必ず置いて回避している。**この回避の必要性そのものが本課題の現れである。**
- 判断: **仕様として不適切**（データ損失）。少なくとも WARN が要る。
  修正はこの作業では行わない（`src/main` 無変更）。

### 前提: 本体リーダの但し書き（以下 XLS-01〜XLS-03 の共通前提）

ここから先の XLS-01〜XLS-03 は、いずれもセル種別・表示形式に起因する課題であり、共通の前提を持つ
（上の XLS-05 はこの前提とは無関係である）。

`nablarch-testing` の `PoiXlsReader`（クラス Javadoc）は次のとおり明記している。

> EXCELに記述されたテストデータは、すべて文字列書式となっている必要がある。
> 文字列書式以外のデータ書式が存在した場合の動作は保証しない。

`PoiXlsReader#readOneLine` はセル値を `cell.toString()` で文字列化するだけで、セル種別・表示形式による
分岐を持たない。以下 XLS-01〜XLS-03 はいずれもこの「保証しない」範囲の挙動である。
ただし変換ツール（Excel→YAML）は**テストデータを恒久的に別形式へ書き換える**ため、
「保証外だから何が起きてもよい」では済まず、少なくとも検知（WARN）が要るかを判断する必要がある。

### XLS-01 数値セルが `double` の文字列表現になる（影響度 高・値が変わるため後段のテスト失敗で検出できる）

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

#### `nablarch-example-web`（サンプルアプリ）での再現（参照フィクスチャによる実測・2026-08-12）

**以下の件数は `nablarch-example-web`（サンプルアプリ）の Excel 6 ファイルの実測値であり、対象PJの実データではない**
（2026-08-12・ユーザー指摘による訂正）。

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
- **データ列に置かれた数値セルは、この 6 ファイルの 39 件中 1 件だけ**存在する。
  `ProjectActionRequestTest.xlsx` の `downloadNormal` シート `M14` ＝
  `<c r="M14" s="55"><v>2000</v>`（`t` 属性なし＝数値セル、`cellXfs[55]` は `numFmtId="0"` ＝ General）。
  `SETUP_TABLE[1]=PROJECT` の `COST_OF_GOODS_SOLD` 列に当たる。
  同じ行の `SALES`／`SGA`／`ALLOCATION_OF_CORP_EXPENSES` は文字列セルで `"1000"`／`"3000"`／`"4000"`
  のまま入るのに対し、**`COST_OF_GOODS_SOLD` だけが `"2000.0"` になる**。
  Excel の画面表示は `2000`。DB 投入値が `2000.0` になり数値カラムでなければ壊れる。
  担保テスト: `XlsReferenceFixtureTest#readsExcelSavedWorkbookIntoIntermediateModel`。
- したがって影響度「高」は維持する。ただし**`nablarch-example-web` の 6 ファイルでの発現箇所は 1 件**であり、
  「表示形式 `@` 付き数値セルが 38 件ある」ことをもって影響が広いとは言えない
  （マーカー列に集中しているため）。この訂正はコーディネータの判断材料として記録する。
  なお 6 ファイルはサンプルアプリの内容であり、**対象PJの実データでの発現件数は未知である**
  （2026-08-12・ユーザー指摘による訂正）。

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
- 影響度を「高」ではなく「中」とした根拠は、`nablarch-example-web`（サンプルアプリ）の 6 ファイルに
  日付・時刻・日時セルが 0 件で、Excel 保存物での発現を確認できていないことに限る。
  **対象PJの実データにこれらのセル型が無いことを意味しない**（2026-08-12・ユーザー指摘による訂正）。
- 判断: **仕様として不適切**。修正はこの作業では行わない。

### XLS-03 数式セルが計算結果ではなく数式文字列になる（影響度 中）

| 入力セル | 中間モデルへ入る値 | 担保テスト |
|---|---|---|
| 数式 `=1+1` | `"1+1"` | `XlsFormatReaderCellTypeTest#readsFormulaCellAsFormulaText` |

- 原因: POI の `XSSFCell#toString()` が数式セルに対して `getCellFormula()`（先頭 `=` なし）を返す。
- Excel の画面表示は計算結果 `2` だが、変換後は `1+1` という文字列になる。
- `nablarch-example-web`（サンプルアプリ）の 6 ファイルに数式セルは 0 件（steering Decisions）。
  これは**参照フィクスチャに使える実物が無い**ということであり、
  **対象PJの実データに数式セルが無いことを意味しない**。
- 判断: **仕様として不適切**（画面表示と一致しない）。
  **「実データに存在しないため優先度は低い」という当初の理由づけは撤回する
  （2026-08-12・ユーザー指摘による訂正）。** 優先度が低いのではなく、Excel 保存物との突き合わせが
  後回しになるという意味に限る（挙動の確認・固定そのものは他ケースと同じ重みで行った）。
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

### 課題としないと判断した観測結果

| 入力セル | 中間モデルへ入る値 | 判断 |
|---|---|---|
| 文字列 `abc` | `"abc"` | 妥当 |
| 文字列 `007`（先頭ゼロ） | `"007"` | 妥当（文字列セルなら先頭ゼロは保たれる） |
| 真偽値 `TRUE` | `"TRUE"` | 保証外だが表示と一致する。妥当（`nablarch-example-web` の 6 ファイルに 0 件だが、それは判断根拠ではない） |
| エラー値 `#DIV/0!` | `"#DIV/0!"` | 例外にならず表示と一致する。妥当（`nablarch-example-web` の 6 ファイルに 0 件だが、それは判断根拠ではない） |
| 前後に空白を持つ文字列 `␣␣pad␣␣` | `"  pad  "` | 妥当（トリムされない） |
| 改行を含む文字列 | 改行を含んだまま 1 値 | 妥当 |
| リテラル文字列 `null` | `"null"` | 妥当（Excel 経路で `null` へ戻せないことは `XlsFormatWriter` の Javadoc に既記） |

- **真偽値セル・エラー値セルの「実データ 0 件。妥当」という理由づけは撤回する
  （2026-08-12・ユーザー指摘による訂正）。** 課題としない理由は「読み取り結果が Excel の画面表示と一致し、
  値が失われないから」であって、件数が 0 だからではない。件数 0 は `nablarch-example-web`（サンプルアプリ）の
  6 ファイルでの実測値であり、**対象PJの実データにこれらのセル型が無いことを意味しない**。

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

**「実データに 0 件のため突き合わせ不能」という理由づけは撤回する（2026-08-12・ユーザー指摘による訂正）。**
`nablarch-example-web` は**サンプルアプリであって対象PJの実データではない**。
正しくは「**`nablarch-example-web` の 6 ファイルに 0 件のため、参照フィクスチャに使える実物が無い。
対象PJの実データにこれらのセル型が無いことを意味しない**」である。
「起こらないから確認不要」と読んではならない。

以下のセル種別は**参照フィクスチャ（およびユーザーが実測した `nablarch-example-web` の Excel 6 ファイル
すべて）に 1 件も存在せず、突き合わせに使える実物の Excel 保存物が無い**ため、
Excel 保存物と POI 生成物の読み取り結果が同一であることを確認できなかった。
これらのケース（D1-06〜D1-11）の挙動は **POI 生成物でのみ**確認したものであり、
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

---

## #20 辺① 軸A・B・C（実ファイル経由）で記録した課題

**掲載順**: 「凡例 → 並び順の原則」に従い、**検出できない** XLS-08 を本節の先頭に置く。課題 ID は発見順のまま
振り直していない（XLS-08 は 2026-08-12 の修正ラウンドで追加したもの）。

### XLS-08 マーカー列だけのブロックは「セル 0 個の行」になり、書き戻すと行が消える（影響度 低・**検出できない**）

| 入力 | 中間モデルへ入る結果 | 担保テスト |
|---|---|---|
| `SETUP_TABLE=T`／カラム行 `[no]` のみ／データ行 `1`, `2` | `columnNames=[]`、`rows=[[], []]`（セルを 1 つも持たない行が 2 件） | `XlsFormatReaderRealFileTest#readsEmptyColumnNamesFromMarkerOnlyTableInRealBook` |
| `LIST_MAP=lm`／カラム行 `[no]` のみ／データ行 `1` | `columnNames=[]`、`rows=[[]]` | `#readsEmptyColumnNamesFromMarkerOnlyListMapInRealBook` |

- 原因: マーカー列（`[no]`）は本体 `HeaderLine#getEffectiveColumnNames()` が有効カラム名から除外する
  （steering #15 の意図した除外）。一方、データ行の件数はそのまま数えられるため、
  「列 0 個 × 行 N 件」という中間モデルになる。
- 実測（プローブ実行・2026-08-12）: 上表のとおり。テーブル系・LIST_MAP の両経路で同じ結果になる。
- **往復が安定しない**: この中間モデルを `XlsFormatWriter` で Excel へ書き戻し、もう一度読むと
  `rows=[]`（0 件）になる。書き出された行が全カラム空になり、XLS-05 のとおり `PoiXlsReader` に
  読み飛ばされるためである（プローブで実測）。YAML へ書くと `rows: [- {}]`（キーを持たない行）になる。
- 影響: 失われるのは「セルを 1 つも持たない行」であり、**値としての情報は失われない**。
  行数だけが変わる。したがって影響度は「低」とする。ただし変換前後でモデルが変わることに
  警告は一切出ないため、**検出はできない**（本節の先頭に置く理由）。
- 判断: XLS-05（全カラムが空のデータ行が黙って消える）の派生であり、単独で修正すべきものではない。
  XLS-05 の対応を検討する際に、この派生ケースも併せて判断すること。修正はこの作業では行わない。

### XLS-06 レコード種別の省略が実 `.xlsx` 経路では `null` にならず空文字になる（影響度 中）

| 入力 | 中間モデルへ入る値 | 担保テスト |
|---|---|---|
| `SETUP_FIXED=f.dat` の名前行 先頭セル（レコード種別）が空白セル | `RecordLayout.recordType` ＝ `""` | `XlsFormatReaderRealFileTest#readsOmittedRecordTypeAsEmptyStringFromRealBook` |

- 中間モデル `RecordLayout` の Javadoc（L26/L36）は省略時の表現を **`null`** と定めており、
  YAML 経路（辺②）は実際に `null` を入れる
  （`YamlFormatReaderTest#readFile_recordTypeOmitted_keepsNullRecordType`）。
- Excel 経路は `XlsFormatReader#toRecordLayouts` L306（`String recordType = bodyLines.get(idx).get(0);`）が
  生行の先頭セルをそのまま採る。空セルは `PoiXlsReader` が `""` を返すため、`null` にはならない。
  **当初 L305 と記載していたのは誤り（L305 は `verifyNameRow` の呼び出し行）。2026-08-12・レビュー指摘により訂正。**
- 影響: `YamlFormatWriter` L261-263 は `recordType != null` のときだけ `record_type` を書き出すため、
  **Excel 由来の「レコード種別省略」は `record_type: ""` として YAML に現れる**（YAML 由来の省略はキー自体が無い）。
  プローブで実測した変換後 YAML（`s.yaml` の**全文**。当初この節に載せていたスニペットは
  `record_type: ""` の行で切り詰められており実測と一致しなかった。2026-08-12・レビュー指摘により
  プローブを実行し直して差し替えた）:

  ```yaml
  setup_files:
    - path: "f.dat"
      type: "fixed"
      directives:
        file-type: "Fixed"
      records:
        - record_type: ""
          fields:
            - {name: "f1", type: "半角英字", length: "3"}
          rows:
            - ["abc"]
  ```

  入力は `SETUP_FIXED=f.dat` ／ 名前行 `[空白セル], f1` ／ 型行 `[空白], 半角英字` ／ 長さ行 `[空白], 3` ／
  値行 `[空白], abc`。

- Excel へ書き戻す際は `XlsFormatWriter` L275 の `nullToEmpty` により元と同じ空セルへ戻るため、
  Excel→YAML→Excel の往復自体は安定する。しかし同じ「省略」が入力形式によって 2 通りに表現され、
  YAML 同士の比較・スキーマ上の扱いが揃わない。
- 判断: **仕様として不適切**（省略の表現が経路間で非対称）。修正はこの作業では行わない。
- 帰結: 辺①では軸C の C-16「省略（`null`）」は**到達不能**である。

### XLS-07 器が注入する既定ディレクティブが、Excel に書かれていなくても中間モデルに現れる（影響度 低・記録のみ）

| 入力 | 中間モデルへ入る `directives` | 担保テスト |
|---|---|---|
| ディレクティブ行を持たない `EXPECTED_FIXED` | `{file-type=Fixed}` | `XlsFormatReaderRealFileTest#readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook` |
| `record-separator` だけを書いた `EXPECTED_VARIABLE` | `{file-type=Variable, record-separator=CRLF, field-separator=,}` | `#readsExpectedVariableFileBlockWithGroupIdFromRealBook` |
| `text-encoding` だけを書いた `MESSAGE`（本文は固定長） | `{file-type=Fixed, text-encoding=UTF-8}` | `#readsMessageBlockFromRealBook` |
| ディレクティブ行を持たない送信同期メッセージ 4 種 | `{file-type=Fixed}` | `#readsAllFourSendSyncMessageTypesFromRealBook`（4 種すべてでループ内アサート） |

- 原因: 本体 `DataFile` のコンストラクタ（L92）が `setDirective("file-type", getFileType())` を必ず実行する。
  可変長は `VariableLengthFile` L29 がさらに `field-separator` の既定値 `,` を設定する。
- 変換後 YAML には作成者が書いていない `file-type` / `field-separator` が出力される。
  ただし値は器の既定値そのものであり、テスト実行時の解釈は変わらない。
- 判断: 受容できる（記録のみ）。
- 帰結: 辺①では軸C の C-11 `FileDataBlock.directives` 空 と C-13 `MessageDataBlock.directives` 空 は
  **到達不能**である（Excel にディレクティブ行が 1 行も無くても空 Map にならない）。

### XLS-09 `XlsFormatReader#stripQuotes` の `null` ガードのコメントが実挙動と食い違う（影響度 低・記録のみ／`src/main` は無変更）

| 対象 | 内容 |
|---|---|
| `XlsFormatReader` L531（`stripQuotes` の直前のコメント） | 「`toRecordLayouts` の `valueCells.get(i)` は Excel の空白セルに対して `null` を返すため、このガードは必須。」 |

- **この記述は誤りである**（2026-08-12・レビュー指摘を受けて該当箇所を読み直して裏を取った）。
  `PoiXlsReader#readOneLine` L123 は `String cellValue = cell == null ? "" : cell.toString();` であり、
  Excel の空白セル・不在セルに対して **`""` を返す。`null` は返さない**（XLS-04 で実測済み）。
  したがって実 `.xlsx` 経路では `valueCells.get(i)` が `null` になることはない。
- ではどこで `null` が生じるか: テーブル経路（`readTableBlocks`、`table.getValue()` が `null`）と
  LIST_MAP 経路（`readListMapBlock`、`mapRow.get(column)` が `null`）である。ただし**どちらも
  呼び出し側で `value == null ? null : stripQuotes(...)` と先に判定している**ため、`null` が
  `stripQuotes` に渡ることはない。
- 残る到達経路は、Fake リーダ（`XlsFormatReaderTest` の `FakeTestDataReader`）が canned 行の要素に
  Java の `null` を直接置いた場合の `toRecordLayouts` 経路だけであり、現行スイートにその入力は無い。
  実際 #20 の JaCoCo 計測でも `stripQuotes` の `null` ガード（L533）は**未到達**である。
- 判断: 実挙動としてのガード自体は防御的で害が無い（残してよい）。**コメントの根拠づけだけが誤っている。**
  `src/main` は本作業では変更しないため記録に留める。修正する場合はコメントを
  「テーブル／LIST_MAP 経路と対称に `null` を通すための防御的ガード。実 Excel 経路では `null` は生じない」
  程度に直すのが正しい。

### 課題としないと判断した観測結果（#20）

| 観測 | 判断 |
|---|---|
| グループ ID を持たないマーカー（`SETUP_TABLE=T`）の `groupId` が `""` | 妥当（`TestDataBlock` Javadoc L27/L41「省略時は空文字」どおり） |
| 可変長ファイルの `FieldDef.length` が `null` | 妥当（長さ行を持たないため。`FieldDef` Javadoc L25/L43 どおり） |
| マーカー行の無いシートがブロック 0 件のセクションになる | 妥当 |
| Excel 記述順の列名が LIST_MAP でアルファベット順にならない | 妥当（steering #15 の修正どおり） |
| 長さ省略記法 `-` が実 `.xlsx` 経路でも原文 `"-"` のまま入る（器は実バイト長 `4` へ正規化している） | 妥当（原文復元ロジックの意図どおり。`XlsFormatReaderRealFileTest#readsOmittedFieldLengthNotationFromRealBook` で固定。2026-08-12 の修正ラウンドで追加） |
| 送信同期メッセージの `RecordLayout.recordType` が名前行の先頭セル＝メタ列ヘッダ `no` になる | 妥当（2026-08-12 の修正ラウンドで実測して判断）。送信同期の名前行の先頭セルは本来レコード種別ではないが、`toRecordLayouts` L306 が一律に先頭セルを採るため `"no"` が入る。この値は `XlsFormatWriter` L275 が名前行の先頭セルへ書き戻すのに使われ、実 `.xlsx` → 中間モデル → 実 `.xlsx` → 中間モデルで `"no"` のまま安定することをプローブで実測した（`null` だとメタ列ヘッダが失われて往復が壊れる）。すなわち本経路では load-bearing である。既存テスト `XlsFormatReaderTest#readPreservesErrorModeRowInSendSyncMessage` の判定（良性）と一致する。YAML には `record_type: "no"` が出る |

### 到達不能と判定した軸要素（#20 で新たに判明したもの）

`inventory.md` §1.3 では「要追加」に分類されていたが、実 `.xlsx` 経路では生成できないことが判明したもの。

| 軸要素 | 根拠 |
|---|---|
| C-11 `FileDataBlock.directives` 空 ／ C-13 `MessageDataBlock.directives` 空 | XLS-07（器が `file-type` を必ず注入する）。担保テストは C-11 が `XlsFormatReaderRealFileTest#readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook`、C-13 が `#readsAllFourSendSyncMessageTypesFromRealBook`（ディレクティブ行を 1 行も持たない送信同期 4 種すべてについて、ループ内で `getDirectives()` の内容と件数をアサートする） |
| C-16 `RecordLayout.recordType` 省略（`null`） | XLS-06（実 `.xlsx` 経路では `""` になる） |
| C-17 `RecordLayout.fields` 空 | **2026-08-12・レビュー指摘により調査して追加**（当初は「軸E の 0 件と重なる」として #21 送りに分類していたが、実測すると到達不能だった）。フィールドを 0 件にするには名前行をレコード種別セル 1 列だけにするしかないが、本体 `DataFileParser` L234 が `IllegalStateException: directive or data names row must have two columns at least. [data]` で弾く。仮に名前行を空にできたとしても `DataFileFragment#setNames`（L190）の `assertNotNullOrEmpty`（L326）が `names must not be null or empty.` で弾く。いずれも `TestDataParsingTemplate#parse` L160 が `IllegalStateException("can't get data")` に包んで失敗する。`SETUP_FIXED`／`MESSAGE` の双方で実測した。**根拠テスト（#21 で追加）: `XlsFormatReaderInvalidInputTest#failsWhenNameRowHasOnlyRecordTypeCellInRealBook`**（`SETUP_FIXED`／`MESSAGE` の 2 ブックで例外型とメッセージをアサートする）。例外そのものは軸F の F1-06 としてタスク #21 が扱う |
| C-20 `FieldDef.type` 省略（`null`） | 型が欠ける入力は本体パーサが弾く。**機構は欠け方で 2 通りに分かれる（2026-08-12・レビュー指摘によりプローブを実行し直して訂正。当初は両方を `assertSameSizeAsNames` 由来と書いていたが誤り）**。<br>① 型行が名前行より短い（型セルが**末尾**で空の場合も、空白セルは行の使用範囲から外れるため同じ経路になる）→ `DataFileFragment#assertSameSizeAsNames`（宣言 L339。`throw` は L342。呼び出しは `setTypes` L203）が `IllegalArgumentException: field name size is 2. but types size is 1. FixedLengthFileFragment{...}`。**根拠テスト（#21 で追加）: `XlsFormatReaderInvalidInputTest#failsWhenTypeRowIsShorterThanNameRowInRealBook`**。<br>② 型セルが**中間位置**で空 → 要素数は一致するので `assertSameSizeAsNames` は通り、`setTypes` L206 の `convertToFrameworkExpression` → `BasicDataTypeMapping` L69 が `IllegalArgumentException: can't convert value []. convert table ={半角カナ=X, ...}`（変換表は `HashMap` 由来で並び順が変わる）。**根拠テスト（#21 で追加）: `XlsFormatReaderInvalidInputTest#failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook`**。<br>いずれも `TestDataParsingTemplate#parse` L160 が `IllegalStateException("can't get data")` に包んで失敗する。器が成立する入力では型が常に全フィールドぶん揃うため、`XlsFormatReader#readFieldDefs` L378 の `null` フォールバックには到達しない。例外そのものは軸F の F1-06（行と列の数の不一致）としてタスク #21 が扱う |

---

## #21 辺① 軸E（多重度）・軸F（異常系）で記録した課題

**掲載順**: 「凡例 → 並び順の原則」に従い、**検出できない**もの（XLS-10・XLS-13・XLS-12・XLS-15）を先に置き、
loud に失敗する／記録のみのもの（XLS-11・XLS-14）を後に置く。課題 ID は発見順のまま振り直していない
（XLS-15 は E-3(複数) の追加担保を実装する過程で見つけたため最後の ID になっている）。

以下はすべて `XlsFixture` が POI で組み立てた実 `.xlsx` を `new XlsFormatReader().read(...)` に渡して
実測したものである（プローブ実行 2026-08-12 ＋ 担保テストの実行）。

### XLS-10 未知のデータタイプ名のマーカーは行ごと黙って無視され、ブロックが変換結果から消える（影響度 中・**検出できない**）

| 入力 | 中間モデルへ入る結果 | 担保テスト |
|---|---|---|
| `UNKNOWN_TYPE=X`／カラム行 `A`／データ行 `a1`／`SETUP_TABLE=T`／カラム行 `B`／データ行 `b1` | ブロックは **1 件**（`SETUP_TABLE=T` だけ）。未知タイプ側はマーカー行もカラム行もデータ行も中間モデルに現れない | `XlsFormatReaderInvalidInputTest#ignoresBlockWhoseMarkerHasUnknownDataTypeNameInRealBook` |

- 原因: `TestCoreReaderAdapter` の `HeaderCollector#parse`（L361-364）が、先頭セルから判定したデータタイプが
  `DEFAULT`（＝既知のどの名前にも一致しない）の行を `continue` でスキップする。マーカー行として認識されない
  以上ブロックは 1 件も生成されず、後続行も `BodyLineCollector`（L457-463）が `collecting == false` のまま
  読み飛ばす。
- 実測: 上表のとおり。**警告・ログ出力は一切ない**（プローブ実行・テスト実行とも WARN の出力なし）。
  小文字表記（`setup_table=T`）でも同じくブロック 0 件になることをプローブで確認した
  （`DataType` の名前照合は大文字完全一致のため）。
- 影響: マーカーの綴り誤り・大文字小文字の誤りがあると、そのブロックは変換後の YAML に 1 行も出ない。
  変換結果を見比べない限り気づけない。
- 判断: **仕様として不適切**（少なくとも「先頭セルが `=` を含みデータタイプ名らしいのに未知」を WARN で
  報せるべき）。ただし NTF の Excel 形式では任意文字列の先頭セルは正当なデータ行でもあり得るため、
  検知は発見的（ヒューリスティック）にならざるを得ない。修正はこの作業では行わない。

### XLS-13 送信同期メッセージのメタ列（`no`）欠落で先頭フィールドと値が黙って失われる（影響度 低・**検出できない**）

| 入力 | 中間モデルへ入る結果 | 担保テスト |
|---|---|---|
| `EXPECTED_REQUEST_HEADER_MESSAGES[case1]=RM01`／名前行 `requestId`, `userId`（本来先頭に置くべき `no` 列が無い）／型行 `[空白]`, `半角英字`／長さ行 `[空白]`, `10`／値行 `RM01`, `user01` | `recordType` ＝ `"requestId"`、`fields` ＝ `[userId]`、`rows` ＝ `[[user01]]`。**先頭フィールド `requestId` と値 `RM01` が消える** | `XlsFormatReaderInvalidInputTest#dropsFirstFieldWhenSendSyncMetaColumnIsMissingInRealBook` |

- 原因: 送信同期・MESSAGE 経路は名前行の先頭セルを一律にレコード種別として扱い（`XlsFormatReader#toRecordLayouts` L306）、
  値行の先頭セルは本体 `SendSyncMessageParser` L134 の
  `currentFragment.addValueWithId(temp, temp.remove(NO_COLUMN_NUMBER))`（`NO_COLUMN_NUMBER` は L99 で `0`）が
  ID として取り除く。メタ列が無い入力でも「先頭列＝メタ列」という前提が変わらないため、実データが 1 列ぶんずれる。
- 実測: 上表のとおり。例外にならず警告も出ない。
- 影響: 失われるのはフィールド 1 件とその値であり、変換後の YAML を元の Excel と突き合わせない限り気づけない。
  ただしメタ列の欠落は NTF の記法違反であり、正しく書かれた入力では起こらない。よって影響度は「低」とする。
- 判断: 入力が記法違反である以上パーサが救えないことは受け入れるが、**黙って落ちる**点は記録に値する。
  修正はこの作業では行わない。

### XLS-12 カラム行・名前行より右にはみ出したデータセルが黙って捨てられる（影響度 低・**検出できない**）

| 入力 | 中間モデルへ入る結果 | 担保テスト |
|---|---|---|
| `SETUP_TABLE=LONG`／カラム行 `C`, `D`／データ行 `c1`, `d1`, `e1` | `rows` ＝ `[[c1, d1]]`。**3 セル目 `e1` は消える** | `XlsFormatReaderInvalidInputTest#padsShortDataRowAndDropsCellsBeyondColumnRowInRealBook` |
| `SETUP_FIXED=long.dat`／名前行 `data`, `g1`／値行 `[空白]`, `xyz`, `extra` | `rows` ＝ `[[xyz]]`。**3 セル目 `extra` は消える** | `#padsShortValueRowAndDropsCellsBeyondNameRowInFixedFileInRealBook` |

- 原因: `XlsFormatReader#readTableBlocks`（L154-158）・`#readDataRows`（L404-408）は、いずれも**カラム名／
  フィールド名の件数ぶんだけ**値を取り出す。行の側が長くても余りは参照されない。
- **反対向き（行が短い）とは非対称である**: 足りないセルは空文字で埋められる
  （ファイル経路は `XlsFormatReader#readDataRows` L406 の `i < valueCells.size() ? ... : ""`。
  テーブル経路・LIST_MAP 経路は本体 `HeaderLine#excludeMarkerColumns` L81 の
  `(i >= line.size()) ? "" : line.get(i)`。`TableDataParser#onReadLine` L98 がこれを呼ぶ）。埋める側は
  XLS-04（セル不在と空文字が区別されない）と同じ扱いであり受容できるが、**捨てる側は情報が失われる**。
- 実測: 上表のとおり。例外にならず警告も出ない。
- 影響: カラム行の書き忘れ（値だけ足した列）があると、その列の値は変換後に存在しない。
  ただしカラム名が無い値は中間モデルに置き場所が無く、変換ツール単独では救えない。よって影響度は「低」とする。
- 判断: 少なくとも WARN が要る（カラム行より右に非空セルがある、という検知は容易である）。
  修正はこの作業では行わない。

### XLS-15 `MESSAGE` 本文の 2 つ目のレコードレイアウトが値行として吸収される（影響度 低・**検出できない**）

| 入力 | 中間モデルへ入る結果 | 担保テスト |
|---|---|---|
| `MESSAGE=m`／FW ヘッダ行／断片1 名前行 `header`,`h1`・型行・長さ行・値行 `[空白]`,`HH`／断片2 名前行 `data`,`d1`・型行 `[空白]`,`半角英字`・長さ行 `[空白]`,`3`・値行 `[空白]`,`abc` | `records` は **1 件**（`recordType="header"`, `fields=[h1]`）。`rows` ＝ `[[HH], [d1], [半角英字], [3], [abc]]` — **2 つ目の名前行・型行・長さ行がデータ値になる** | `XlsFormatReaderInvalidInputTest#absorbsSecondNameRowAsDataRowInMessageBodyInRealBook` |

- 原因: 本体 `DataFileParser#onReadingValues`（L193-202）は先頭セルが非空の行を「新しい断片の名前行」として
  扱うが、`MessageParser` が生成する匿名 `FixedLengthFileParser` はこれを上書きし、
  空行以外は常に `currentFragment.addValue(tail(line))` とする（`MessageParser` の
  `createFixedLengthFileParser` 内。送信同期の `no` 列＝先頭セルが非空のデータ行に合わせた仕様）。
- 実測: 上表のとおり。例外にならず警告も出ない。
- 帰結: **`MESSAGE`／送信同期系では 1 ブロックにレコードレイアウトを 2 件以上作れない**
  （軸E の E-3(複数) はメッセージ系では到達不能。ファイル系
  `XlsFormatReaderRealFileTest#readsMultipleRecordLayoutsFromOneFixedFileInRealBook` で担保する）。
- 影響: フィールド名・型記法・長さといった構造情報がデータ値として YAML に出る。値そのものは失われないが
  構造は崩れる。作成者が「メッセージ本文に複数レコードを書ける」と誤解した場合にだけ起こり、
  正しく書かれた入力では起こらないため影響度は「低」とする。
- 判断: 本体の仕様（`no` 列との両立）に由来するため変換ツール単独では直せない。**黙って吸収する**点を記録に留める。
  修正はこの作業では行わない。

### XLS-11 既知のデータタイプ名で始まる未知の名前は、既知タイプ＋グループ ID として解釈される（影響度 低・記録のみ）

| 入力 | 中間モデルへ入る結果 | 担保テスト |
|---|---|---|
| `SETUP_TABLEX=T`（`SETUP_TABLE` の綴り誤り） | `dataType` ＝ `SETUP_TABLE_DATA`、`groupId` ＝ `"X"`（角括弧なし）、`identifier` ＝ `"T"` | `XlsFormatReaderInvalidInputTest#readsSuffixAfterKnownDataTypeNameAsGroupIdInRealBook` |

- 原因: `TestCoreReaderAdapter#markerGroupId`（L282-286）がデータタイプ名の直後から `=` までを無条件に
  グループ ID として切り出す。正しい記法は `SETUP_TABLE[g1]=T` のように角括弧付きだが、角括弧の有無は
  検証されない。
- 影響: 変換後の YAML に作成者が意図しない `group_id: "X"` が出る。値そのものは失われず、
  Excel へ書き戻せば元の `SETUP_TABLEX=T` に戻る（往復は安定する）。
- 判断: 受容できる（記録のみ）。ただし XLS-10 と合わせると、「マーカーの綴り誤り」は
  **消える**（未知の名前）か**別グループになる**（既知名＋余分な文字）かのどちらかで、いずれも警告が無い。

### XLS-14 ブック破損時の例外がどのファイルかを示さない（影響度 低・記録のみ）

| 入力 | 送出される例外 | 担保テスト |
|---|---|---|
| 拡張子だけ `.xlsx` で中身が Excel でないファイル | `java.lang.RuntimeException: test data file open failed.`（原因: POI の `IllegalArgumentException: Your InputStream was neither an OLE2 stream, nor an OOXML stream`）。**連鎖するどのメッセージにもファイル名・パスが無い** | `XlsFormatReaderInvalidInputTest#failsWithGenericRuntimeExceptionWhenWorkbookIsBroken` |

- 原因: 本体 `PoiXlsReader#getWorkbook` L191 が `throw new RuntimeException("test data file open failed.", e)`
  としており、引数の `filePath` をメッセージに載せていない。
- 比較: シート不在（F1-01）は `PoiXlsReader#open` L75 が
  `sheet not found. path=[...] sheet=[...]` とパスを載せる。ブック不在は
  `IllegalArgumentException: resource open failed. url = [file:...]` が連鎖する（プローブ実測）。
  **破損だけが手掛かりを持たない。**
- 影響: `TestDataConverter#convert`（L71-76）は入力ディレクトリ配下の全ブックを順に読むが、
  読み取り例外を包み直さないため、破損ブックが 1 本あっても**どれが壊れているか分からない**。
  変換は必ず失敗する（loud）ので気づけはする。
- 判断: 例外型（汎用 `RuntimeException`）とメッセージは本体 `nablarch-testing` 側の実装であり、
  変換ツールからは変えられない。**変換ツール側で読み取り例外にリソース名を添えて包み直す**のが
  あるべき姿だが、`src/main` は本作業では変更しない。コーディネータの判断材料として記録する。

### 課題としないと判断した観測結果（#21）

| 観測 | 判断 |
|---|---|
| カラム行だけでデータ行が 0 件のテーブル／LIST_MAP が `rows=[]` になる | 妥当（データが無い以上、行も無い） |
| ディレクティブ行だけの `SETUP_FIXED`、FW ヘッダ行だけの `MESSAGE` がブロックとして生成され `records=[]` になる | 妥当（YAML 経路の `YamlFormatReaderTest#readMessage_emptyBody_isStillMapped` と同じ扱い） |
| 名前行・型行・長さ行だけで値行が 0 件の断片が `rows=[]` になる | 妥当（フィールド定義だけを持つレコードレイアウトは表現できる） |
| 固定長ファイルは断片を複数持て、レコード種別・フィールド定義・値行が断片ごとに独立して入る（断片 2 件・3 件の双方で実測） | 妥当（`XlsFormatReaderRealFileTest#readsMultipleRecordLayoutsFromOneFixedFileInRealBook` で固定。2 断片目の長さ省略記法 `-` も原文のまま） |
| シート不在が `IllegalArgumentException: sheet not found. path=[...] sheet=[...]` になる | 妥当（どのブックのどのシートかが分かる） |
| データ行がカラム行より短いとき空文字で埋められる | 妥当（XLS-04 のとおり Excel 上で空セルと区別できない） |
| 型行・長さ行の要素数不一致が例外で弾かれる（`field name size is 2. but types size is 1.` 等） | 妥当（器が組み立たない以上、原文の充填先も決まらない。黙って続けるより良い） |
| マーカーカラムの角括弧欠落（`[no]` ではなく `no`）で当該列がふつうのデータカラムになる | 妥当（マーカーカラムの判定は「`[` で始まり `]` で終わる」であり記法どおり） |

### 未確認（#21）

- **軸F の「継続する異常系」で警告が出ないことは、ログ出力の有無を目視で確認したにとどまる。**
  XLS-10／XLS-12／XLS-13／XLS-15 の担保テストはログハンドラを取り付けておらず、「WARN が出ない」ことを
  アサートしていない（`XlsFormatReaderTest` のカラム名重複テストのようなログ捕捉は行っていない）。
  課題として記録した挙動そのもの（中間モデルの内容）はアサートしてある。
- **ブック破損の再現は「Excel でない中身のファイル」1 種類のみ**である。
  ZIP としては開けるが内部構造が壊れている `.xlsx`（部分破損）の挙動は未確認。
- **XLS-15 は `MESSAGE` で実測した。**送信同期 4 種でも同じかは未確認
  （`SendSyncMessageParser` は `MessageParser` を継承し `onReadingValues` をさらに上書きするため
  同じく断片を増やせないと推定されるが、実行して確かめていない）。
