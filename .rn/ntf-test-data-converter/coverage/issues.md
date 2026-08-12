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
- 影響: ユーザー実測（steering Decisions「軸D の優先度」）では `nablarch-example-web` の Excel 6 ファイルに
  **数値セルが 39 件**あり、その実例が「値が数値の `1`・表示形式が `@`」である。
  Excel の画面表示は `1` だが、変換後の YAML には `1.0` が書かれる。
  ID 列・コード列であれば DB 投入値やアサート値が壊れる。
- 桁数の大きい数値は指数表記になり、元の桁を復元できない。
- 判断: **仕様として不適切**。少なくとも変換時に「文字列書式でないセルがある」ことを検知して WARN を出すべき。
  修正はこの作業では行わない。

### XLS-02 日付・時刻・日時セルがセルの表示形式を無視し、ロケール依存の表記になる（影響度 中）

| 入力セル | 中間モデルへ入る値（既定ロケール `en`） | 担保テスト |
|---|---|---|
| 日付 2026-08-07・表示形式 `yyyy/mm/dd` | `"07-Aug-2026"` | `XlsFormatReaderCellTypeTest#readsDateFormattedCellAsPoiDefaultDatePattern` |
| 時刻 シリアル値 `0.5`（12:00:00）・表示形式 `hh:mm:ss` | `"31-Dec-1899"` | `#readsTimeFormattedCellLosingTimeComponent` |
| 日時 2026-08-07 12:34:56・表示形式 `yyyy/mm/dd hh:mm:ss` | `"07-Aug-2026"` | `#readsDateTimeFormattedCellLosingTimeComponent` |

- 原因: POI の `XSSFCell#toString()` が、日付書式付き数値セルに対して
  `new SimpleDateFormat("dd-MMM-yyyy")` で文字列化する。セルの表示形式は使われない。
- **ロケール依存**: `SimpleDateFormat` の既定コンストラクタは既定ロケールを使うため、
  日本語環境では同じセルが `07-8月-2026` になる（実測で確認済み）。
  変換結果が実行環境のロケールで変わる。
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
| セル不在（その位置にセルがない） | `""` | `XlsFormatReaderCellTypeTest#readsAbsentCellAsEmptyString` |
| 空白セル（セルはあるが値なし） | `""` | `#readsBlankCellAsEmptyString` |
| 空文字の文字列セル | `""` | `#readsEmptyStringCellAsEmptyString` |

- 3 者はいずれも `""` になる。Excel 上で区別できない以上、これは受容できる挙動である。
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
| 真偽値 `TRUE` | `"TRUE"` | 保証外だが表示と一致する。実データ 0 件。妥当 |
| エラー値 `#DIV/0!` | `"#DIV/0!"` | 例外にならず表示と一致する。実データ 0 件。妥当 |
| 前後に空白を持つ文字列 `␣␣pad␣␣` | `"  pad  "` | 妥当（トリムされない） |
| 改行を含む文字列 | 改行を含んだまま 1 値 | 妥当 |
| リテラル文字列 `null` | `"null"` | 妥当（Excel 経路で `null` へ戻せないことは `XlsFormatWriter` の Javadoc に既記） |

### 未確認

なし。#19 の 17 ケースはすべて挙動を確認し、テストで固定した
（`XlsFormatReaderCellTypeTest` 18 件＝17 ケース＋空白セルの追加観測 1 件）。

参照フィクスチャ（Excel 保存物 `ClientActionTest.xlsx`）と POI 生成物の読み取り結果は**一致**した
（`XlsReferenceFixtureTest#poiGeneratedWorkbookReadsIdenticallyToExcelSavedWorkbook`）。
「未確認」として記録すべき差分はない。
