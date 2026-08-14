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
| 影響度 別枠 | 変換結果は入力と一致するが、**作成者の意図と NTF 実行時の解釈が食い違う**もの。上の 3 段は「変換結果が入力と一致するか」で定義されるため当てはまらない |
| 未確認 | 挙動を確認・固定できなかったもの |

**「影響度 別枠」を足した（2026-08-14・#24 のレビュー指摘による訂正）。** 高／中／低はいずれも
「変換結果が入力と一致するか」を軸に定義されており、**変換自体は忠実なのに仕様として不適切**という課題を
表現できなかった（YML-01 がそれである）。「低＝記録のみ。仕様として受容できると判断した」に寄せると
「受容した」という誤った意味が付くため、段を 1 つ足して区別する。**別枠は「軽い」という意味ではない。**

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

例: XLS-01（表示形式 `@` の数値セルが `"1.0"` になる）は値が違えばテストが FAIL して気づけるが、
XLS-05（全カラムが空のデータ行が消える）は行が消えても誰も気づかない。
影響度はどちらも「高」だが、**検出できないこと**を理由に XLS-05 を先に置く。

**課題 ID は発見順のまま振り直さない**ため、以下では ID の昇順と掲載順は一致しない。

---

## #19 辺① 軸D（セル種別 8 ケース）で記録した課題

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
  **後続の XLS-01 とは別件である**（セル書式の但し書きとは無関係で、行の読み飛ばしが原因）。
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

### 前提: 本体リーダの但し書き（XLS-01 の前提）

XLS-01 はセル書式に起因する課題であり、次の前提を持つ（上の XLS-05 はこの前提とは無関係である）。

`nablarch-testing` の `PoiXlsReader`（クラス Javadoc）は次のとおり明記している。

> EXCELに記述されたテストデータは、すべて文字列書式となっている必要がある。
> 文字列書式以外のデータ書式が存在した場合の動作は保証しない。

`PoiXlsReader#readOneLine` はセル値を `cell.toString()` で文字列化するだけで、セル種別・表示形式による
分岐を持たない。converter が入出力の対象とするのは **NTF が実行できるテストデータ**、すなわち
この但し書きを満たす「全セルが文字列書式」の Excel に限る。それを外れる入力の挙動は担保対象でも
記録対象でもない。**XLS-01 はこの但し書きの内側で起こる**（表示形式 `@` ＝ 文字列書式でありながら
セル種別が数値であるケース）ため課題として残す。

### XLS-01 表示形式 `@` の数値セルが `double` の文字列表現になる（影響度 高・値が変わるため後段のテスト失敗で検出できる）

| 入力セル | 中間モデルへ入る値 | 担保テスト |
|---|---|---|
| 数値 `1`・**表示形式 `@`（文字列書式）** | `"1.0"` | `XlsFormatReaderCellTypeTest#readsTextFormattedNumericCellAsDoubleString` |

- 原因: `PoiXlsReader#readOneLine` の `cell.toString()` が、数値セルに対して
  POI の `XSSFCell#toString()` → `getNumericCellValue() + ""` を返す。表示形式は参照されない。
- 表示形式が `@`（文字列書式）であっても、セル種別が数値であれば値が変わる。
  上の但し書きは書式についての条件であり、この形は但し書きを満たしたうえで値が変わる。
- 判断: **仕様として不適切**。少なくとも変換時に「**セル種別が文字列でないセル**がある」ことを検知して
  WARN を出すべき。検知条件は書式ではなくセル種別である — 本課題のセルは `numFmtId=49` ＝ `@` ＝
  **文字列書式であり**、「文字列書式でないセル」という条件では捕まえられない。
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
- **`A19` は「表示形式 `@` の数値セル」が Excel 保存物に実在することの根拠**であり、
  本課題の形が机上の仮定ではないことを示す。値が中間モデルへ届く経路にあれば
  `"1"` ではなく `"1.0"` になる。
- したがって影響度「高」は維持する。ただし**`nablarch-example-web` の 6 ファイルでは
  該当セルがすべてマーカー列にあり、変換結果を壊す発現は 0 件**である。
  「表示形式 `@` 付き数値セルが 38 件ある」ことをもって影響が広いとは言えない。
  なお 6 ファイルはサンプルアプリの内容であり、**対象PJの実データでの発現件数は未知である**
  （2026-08-12・ユーザー指摘による訂正）。

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
| 前後に空白を持つ文字列 `␣␣pad␣␣` | `"  pad  "` | 妥当（トリムされない） |
| 改行を含む文字列 | 改行を含んだまま 1 値 | 妥当 |
| リテラル文字列 `null` | `"null"` | 妥当（Excel 経路で `null` へ戻せないことは `XlsFormatWriter` の Javadoc に既記） |

### 対象としない入力

converter の入出力は **NTF が実行できるテストデータ**に限る。Excel 側の条件は
`PoiXlsReader` のクラス Javadoc が定める「全セルが文字列書式」であり、これを外れる入力の挙動は
担保対象でも記録対象でもない（不正な入力にどこまで対応するかに線は引けないため）。

したがって次のセル種別は本書の対象外とする — 表示形式を持たない数値セル／日付書式・時刻書式・
日時書式の数値セル／数式セル／真偽値セル／エラー値セル。

**表示形式 `@` の数値セルだけは対象内である。** `@` は文字列書式であり但し書きを満たすが、
セル種別が数値であるため値が変わる。これが XLS-01 である。

### 未確認

**「未確認なし」は撤回する（2026-08-12・レビュー指摘による訂正）。**

軸D の 8 ケースそのものは、POI 生成フィクスチャを入力にすべて挙動を確認しテストで固定した
（`XlsFormatReaderCellTypeTest` 10 件＝8 ケース＋空白セル 1 件＋行途中の不在セル 1 件。
件数は `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderCellTypeTest.java` → **10**）。
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

対象とするセル種別（文字列セル・表示形式 `@` の数値セル・空白セル・不在セル）は、
上表のとおり Excel 保存物と POI 生成物の読み取り結果が同一であることを確認できている。

残る未確認は**参照フィクスチャの検証範囲**である。

- 参照フィクスチャは 26 シートのうち `downloadNormal` 1 シートのみを検証対象とした。
  残り 25 シートの読み取り結果は未確認である（ブロック構成は同型であり、
  数値セルを含むのは検証対象シートだけであることは全ブック走査で確認済み）。
- steering Assumptions の例外は「実物 `.xlsx` 1 本のみ」であり、参照フィクスチャを増やす予定はない。

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
| C-17 `RecordLayout.fields` 空 | **2026-08-12・レビュー指摘により調査して追加**（当初は「軸E の 0 件と重なる」として #21 送りに分類していたが、実測すると到達不能だった）。フィールドを 0 件にするには名前行をレコード種別セル 1 列だけにするしかないが、本体 `DataFileParser` L234 が `IllegalStateException: directive or data names row must have two columns at least. [data]` で弾く。仮に名前行を空にできたとしても `DataFileFragment#setNames`（L190）の `assertNotNullOrEmpty`（L326）が `names must not be null or empty.` で弾く。いずれも `TestDataParsingTemplate#parse` L160 が `IllegalStateException("can't get data")` に包んで失敗する。`SETUP_FIXED`／`MESSAGE` の双方で実測した。**根拠テスト（#21 で追加）: `XlsFormatReaderInvalidInputTest#failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook` と `#failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook`**（`SETUP_FIXED`／`MESSAGE` は経路が別なので 2 メソッドに分けて例外型とメッセージをアサートする）。例外そのものは軸F の F1-06 としてタスク #21 が扱う |
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

- 原因: **本リポジトリの `src/main/java/nablarch/test/core/reader/TestCoreReaderAdapter.java`**（`nablarch-testing` 側ではない）の
  `HeaderCollector#parse`（L361-364）が、先頭セルから判定したデータタイプが
  `DEFAULT`（＝既知のどの名前にも一致しない）の行を `continue` でスキップする。マーカー行として認識されない
  以上ブロックは 1 件も生成されず、後続行も `BodyLineCollector`（L457-463）が `collecting == false` のまま
  読み飛ばす。
- 実測: 上表のとおり。**警告は 1 件も出ない**。担保テストが `java.util.logging` のルートロガーへハンドラを付け、
  WARNING 以上のレコードが 0 件であることをアサートしている（「検出できない」という主張の実行可能な根拠）。
  小文字表記（`setup_table=T`）でも同じくブロック 0 件になることをプローブで確認した
  （`DataType` の名前照合は大文字完全一致のため）。
- 影響: マーカーの綴り誤り・大文字小文字の誤りがあると、そのブロックは変換後の YAML に 1 行も出ない。
  変換結果を見比べない限り気づけない。
- 判断: **仕様として不適切**（少なくとも「先頭セルが `=` を含みデータタイプ名らしいのに未知」を WARN で
  報せるべき）。ただし NTF の Excel 形式では任意文字列の先頭セルは正当なデータ行でもあり得るため、
  検知は発見的（ヒューリスティック）にならざるを得ない。修正はこの作業では行わない。
- **修正するとしたら本リポジトリ内で完結する**（原因コードが `src/main` にあるため。XLS-11 も同じ）。
  `nablarch-testing` の変更を要する XLS-14 とはこの点が異なる。

### XLS-13 送信同期メッセージのメタ列（`no`）欠落で先頭フィールドと値が黙って失われる（影響度 低・**検出できない**）

| 入力 | 中間モデルへ入る結果 | 担保テスト |
|---|---|---|
| `EXPECTED_REQUEST_HEADER_MESSAGES[case1]=RM01`／名前行 `requestId`, `userId`（本来先頭に置くべき `no` 列が無い）／型行 `[空白]`, `半角英字`／長さ行 `[空白]`, `10`／値行 `RM01`, `user01` | `recordType` ＝ `"requestId"`、`fields` ＝ `[userId]`、`rows` ＝ `[[user01]]`。**先頭フィールド `requestId` と値 `RM01` が消える** | `XlsFormatReaderInvalidInputTest#dropsFirstFieldWhenSendSyncMetaColumnIsMissingInRealBook` |

- 原因: 送信同期・MESSAGE 経路は名前行の先頭セルを一律にレコード種別として扱い（`XlsFormatReader#toRecordLayouts` L306）、
  値行の先頭セルは本体 `SendSyncMessageParser` L134 の
  `currentFragment.addValueWithId(temp, temp.remove(NO_COLUMN_NUMBER))`（`NO_COLUMN_NUMBER` は L99 で `0`）が
  ID として取り除く。メタ列が無い入力でも「先頭列＝メタ列」という前提が変わらないため、実データが 1 列ぶんずれる。
- 実測: 上表のとおり。例外にならず警告も出ない（担保テストが WARNING 以上のログ 0 件をアサートする）。
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
- 実測: 上表のとおり。例外にならず警告も出ない（担保テスト 2 件が WARNING 以上のログ 0 件をアサートする）。
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
- 実測: 上表のとおり。例外にならず警告も出ない（担保テストが WARNING 以上のログ 0 件をアサートする）。
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

- 原因: **本リポジトリの `src/main/java/nablarch/test/core/reader/TestCoreReaderAdapter.java`**（`nablarch-testing` 側ではない）の
  `markerGroupId`（L282-286）がデータタイプ名の直後から `=` までを無条件に
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
| 実 `.xlsx` 経路のカラム名重複（F1-05）が Fake リーダ経路と同じ結果になる（後勝ちで 1 件に絞られ、WARN ログが 1 件出る） | 妥当（#16 で実装した意図どおり。実ファイル経路でも同じであることを `XlsFormatReaderInvalidInputTest#deduplicatesDuplicateColumnNamesWithWarningInListMapFromRealBook`／`#deduplicatesDuplicateColumnNamesWithWarningInTableFromRealBook` で固定した） |

### 未確認（#21）

- **「警告が出ない」ことのアサートは `java.util.logging` 経路に限る。**XLS-10／XLS-12／XLS-13／XLS-15 の担保テストは
  JUL のルートロガーへハンドラを付けて WARNING 以上が 0 件であることをアサートしているが、
  `nablarch-testing` 自身のログ基盤（`nablarch.core.log`）への出力は捕捉していない。
  変換ツール側で JUL を使うのは `XlsFormatReader`（`deduplicateColumnNames` の WARN）だけであり、
  そこが唯一の警告の出所であることはソースを走査して確認した（`grep -rn "Logger" src/main/java` が返すのは `XlsFormatReader` の import と `LOGGER` 宣言だけで、ロガーを持つクラスは 1 つ）。
- **ブック破損の再現は「Excel でない中身のファイル」1 種類のみ**である。
  ZIP としては開けるが内部構造が壊れている `.xlsx`（部分破損）の挙動は未確認。
- **XLS-15 は `MESSAGE` で実測した。**送信同期 4 種でも同じかは未確認
  （`SendSyncMessageParser` は `MessageParser` を継承し `onReadingValues` をさらに上書きするため
  同じく断片を増やせないと推定されるが、実行して確かめていない）。

---

## #22 辺③ 軸D（セル型 8 ケース）・軸F（異常系）で記録した課題

**掲載順**: 「凡例 → 並び順の原則」に従い、**衝突しない限り検出できない**もの（XLS-16）を先に置き、
値が変わるため後段で気づけるもの（XLS-17・XLS-18）、記録のみのもの（XLS-19）を後に置く。

**XLS-18 の訂正（2026-08-13）に伴い掲載順を見直したが、順序は変えない。** XLS-18 は「`CR` が落ちる」ではなく
「`CR` が `LF` へ置き換わる」であり、単独 `CR` では**長さが変わらない**ことが分かった。ただし値そのものは
変わるため、値を突き合わせる後段のテストがあれば失敗する＝「検出できない」区分には入らない。
これは XLS-17（制御文字が `?` へ置換。同じく長さが変わらない）と同じ性質であり、両者の相対順は据え置く。
**長さの差では気づけない**点は XLS-17・XLS-18 に共通する注意であり、各項目の「影響」欄に明記した。

以下はすべて `XlsFormatWriter` で中間モデルを実 `.xlsx` へ書き出し、書き出したファイルを POI で
開き直して実測したものである（プローブ実行 2026-08-13 ＋ 担保テストの実行）。
セル型は `Cell#getCellType()` で確かめている。

**XLS-17・XLS-18 については、さらに `unzip -p <book>.xlsx xl/sharedStrings.xml | od -An -tx1 -c` で
書き出した `.xlsx` の生バイトを直接確かめた（2026-08-13。レビュー指摘を受けた再実測）。**
これにより「値が変わるのが直列化区間か読み戻し区間か」を切り分けている（XLS-18 の該当項参照）。

**この切り分けはテストでも固定してある（2026-08-13・レビュー指摘の第 3 ラウンドで追加）。**
`XlsFormatWriterCellTypeTest#burnsQuestionMarkIntoSharedStringsXmlForControlCharacter` ／
`#keepsCarriageReturnRawInSharedStringsXml` が、書き出した `.xlsx` の ZIP エントリ
`xl/sharedStrings.xml` をパースせず**バイト列として**突き合わせる。
それまでは手作業のダンプでしか確かめておらず、テストは読み戻し値しか見ていなかったため、
POI／xmlbeans の挙動が変われば**テストは全て緑のまま本書の「区間の帰属」だけが誤りになる**状態だった。

### XLS-16 31 文字を超えるセクション名が黙って 31 文字へ切り詰められる（影響度 中・**衝突しない限り検出できない**）

| 入力 | 書き出されるブック | 担保テスト |
|---|---|---|
| セクション名 `a` × 32 文字 | シート名は `a` × **31 文字**。例外も警告も出ない。元の名前では `Workbook#getSheet` が `null` を返し、変換ツール自身の読み戻し（`XlsFormatReader`）も `IllegalArgumentException: sheet not found.` になる | `XlsFormatWriterInvalidOutputTest#truncatesSheetNameLongerThanExcelLimitSilently` |
| セクション名 `a` × 31 文字（上限ちょうど） | そのまま書かれる（切り詰めなし）。元の名前でシートを引ける | `#writesSheetNameOfExcelLimitLengthAsIs` |
| 先頭 31 文字が同じで 32 文字目だけ異なる 2 セクション | `IllegalArgumentException: The workbook already contains a sheet of this name`（衝突したときだけ失敗する） | `#failsWhenTruncatedSheetNamesCollide` |
| 大文字小文字だけが違う 2 セクション（`abc` と `ABC`。どちらも 3 文字で切り詰めは走らない） | 同上（`The workbook already contains a sheet of this name`）。ブックは作られない | `#failsWhenSheetNamesDifferOnlyInCase` |
| セクション名 `a` × 31 文字 ＋ `/`（32 文字。禁止文字が index 31） | **例外にならず**、`a` × 31 文字のシートを持つブックが書き出される（切り詰めが禁止文字検査を無効化する） | `#writesSheetNameWhoseForbiddenCharacterIsRemovedByTruncation` |
| セクション名 `a` × 30 文字 ＋ `/a`（32 文字。禁止文字が index 30） | `IllegalArgumentException: Invalid char (/) found at index (30) in sheet name 'aaa…a/'`。**メッセージのシート名は切り詰め後の 31 文字**である | `#rejectsSheetNameWhoseForbiddenCharacterSurvivesTruncation` |

- 原因: POI 3.8 の `XSSFWorkbook#createSheet(String)` が、31 文字を超える名前を `substring(0, 31)` で
  切り詰めてから `WorkbookUtil.validateSheetName` に掛ける（`javap -c` で逆アセンブルして確認。
  重複判定 `containsSheet` も両辺を 31 文字へ切り詰めて `equalsIgnoreCase` で比べるため、
  切り詰め後に同名になる 2 セクションはここで弾かれる）。
  **引用したこの機構は切り詰め側・`equalsIgnoreCase` 側の両方を実測で押さえてある**:
  切り詰め側は `#failsWhenTruncatedSheetNamesCollide`、`equalsIgnoreCase` 側は
  `#failsWhenSheetNamesDifferOnlyInCase`（レビュー指摘・第 3 ラウンドで追加）。`XlsFormatWriter#build`（L126）は
  `workbook.createSheet(section.getName())` をそのまま呼ぶだけで、長さを検査も報告もしない。
- 実測: 上表のとおり。切り詰めは**メモリ上のブックの時点で**起きている（`build` 直後の `getSheetName(0)` が
  既に 31 文字）。
- 影響: YAML → Excel 変換で、長いセクション名が変換後のブックでは別名になる。値は失われないが
  **名前が変わったことは変換結果を見比べない限り気づけない**。切り詰め後に衝突した場合だけは
  例外で止まるため、気づけるかどうかは入力次第である。
- **見出しのラベルを「検出できない」から「衝突しない限り検出できない」へ直した（2026-08-13・
  レビュー指摘による訂正）。** 上表のとおり本課題は失敗する（＝検出できる）サブケースを 3 つ含む
  （切り詰め後の衝突／大文字小文字だけが違う名前／切り詰め後も残る禁止文字。加えて読み戻しの
  `sheet not found.`）。「検出できない」と断言するラベルは、上表と「気づけるかどうかは入力次第」という
  本文の記述に噛み合っていなかった。**掲載順は変えない。** 「並び順の原則」が問うのは
  *検出できない経路があるか* であり、本課題の主経路（31 文字超のセクション名が 1 つだけあり衝突しない）は
  例外も警告も出ないまま名前が変わるためである。
  さらに**切り詰めが先に走ることで、本来なら例外になるはずの入力まで黙って通る**。
  Excel の禁止文字（`/ \ ? * [ ] :`）は POI の `WorkbookUtil.validateSheetName` が弾くが、
  検査が走るのは切り詰めた**後**の 31 文字に対してである。したがって
  **禁止文字が index 31 以降にある 32 文字以上のセクション名は、検査に到達せず不正な名前が消えたまま
  書き出される**（実測: `"a"×31 + "/"` → 例外なし・`a`×31 のシートを持つブックが生成）。
  「禁止文字なら必ず失敗するので気づける」という前提は成り立たない。
- 判断: **仕様として不適切**（31 文字超は変換前に弾くか、少なくとも WARN で報せるべき）。
  Excel の制約であり回避はできないため、黙って変えないことが要点である。修正はこの作業では行わない。
- **修正するとしたら本リポジトリ内で完結する**（`XlsFormatWriter#build` にシート名の検査を足せばよい）。

### XLS-17 XML で表現できない制御文字が保存時に `?` へ黙って置換される（影響度 中・値が変わるため後段のテスト失敗で検出できる）

| 入力（データ行の値） | 書き出されるセル | 担保テスト |
|---|---|---|
| `a` ＋ `U+0000` ／ `U+0007` ／ `U+000B` ／ `U+001F` ＋ `b` | 文字列セル。値は `a?b`（当該文字が `U+003F` へ置換。文字数は変わらない） | 文字ごとに 1 メソッド: `XlsFormatWriterCellTypeTest#replacesNulCharacterWithQuestionMark`／`#replacesBellCharacterWithQuestionMark`／`#replacesVerticalTabCharacterWithQuestionMark`／`#replacesUnitSeparatorCharacterWithQuestionMark` |
| `a` ＋ `U+0009`（TAB）／ `U+007F`（DEL）＋ `b` | 文字列セル。値は原文のまま（置換されない） | `#writesTabCharacterAsIs`／`#writesDeleteCharacterAsIs` |

- 原因（実測 6 文字からの推定）: 置換されたのは XML 1.0 が文字として認めない符号位置（`U+0000`・`U+0007`・
  `U+000B`・`U+001F`）だけで、XML 1.0 で正当な TAB・DEL は残った。すなわち「制御文字だから」ではなく
  「XML で表現できないから」置換されたと読める。`XlsFormatWriter` 自身は値をそのまま `Cell#setCellValue` に渡しており
  （`build` 直後のメモリ上のブックでは制御文字が保たれていることを担保テストがアサートしている）、
  置換は `.xlsx` の XML へ直列化する区間で起きる。
- 実測: 上表のとおり。例外にも警告にもならない。
- 影響: 制御文字を含むテストデータは変換後に別の値になる。値が変わるため、その値を使う後段のテストが
  失敗すれば気づける。ただし**文字数は変わらない**（1 文字が 1 文字へ置換される）ため、
  長さの差で見る比較では気づけない（XLS-18 の単独 `CR` と同じ性質）。
- 判断: 記録に留める（XML 形式である以上、原文のまま保存する手段が無い）。ただし**黙って**置換される点は
  課題であり、変換ツール側で検知して報せる余地がある。修正はこの作業では行わない。

### XLS-18 `CR` が保存時に黙って `LF` へ置き換わる（影響度 中・**単独 `CR` は文字数が変わらないため気づきにくい**）

**2026-08-13・レビュー指摘により訂正。** 当初この項目は「`CRLF` の `CR` が**落ちる**（削除される）」と
書いていたが、実測すると起きているのは削除ではなく **`CR` → `LF` の置換**である。置換の単位は
「`CRLF` の 2 文字」または「`LF` を伴わない単独の `CR` 1 文字」で、いずれも `LF` 1 文字になる。
`CRLF` で長さが 4 → 3 と減るのは 2 文字が 1 文字になるからであり、**単独の `CR` では長さが変わらない**。
実測でも `a`＋`CR`＋`CR`＋`b`（4 文字）→ `a`＋`LF`＋`LF`＋`b`（4 文字）であり、`LF` がさらにまとめられることはない。

| 入力（データ行の値） | 書き出されるセル | 担保テスト |
|---|---|---|
| `a` ＋ `CR` ＋ `b`（3 文字。単独 `CR`） | 文字列セル。値は `a` ＋ **`LF`** ＋ `b`（**3 文字。長さが変わらない**） | `XlsFormatWriterCellTypeTest#replacesLoneCarriageReturnWithLineFeedInStringCell` |
| `a` ＋ `CRLF` ＋ `b`（4 文字） | 文字列セル。値は `a` ＋ `LF` ＋ `b`（3 文字） | `#replacesCrLfWithSingleLineFeedInStringCell` |
| `a` ＋ `LF` ＋ `b` | 文字列セル。値は原文のまま | `#writesLineFeedStringAsStringCell` |

- 原因（推定）: XML の行末正規化（XML 1.0 は `CRLF` と単独 `CR` をいずれも `LF` 1 文字として読む）。
  POI 3.8 は `CR` を数値文字参照（`&#13;`）で退避しないため、書き出したファイルを読み直すと
  `CR` が `LF` になっている。**行末正規化がその機構であることは推定**である。
- **変化が起きる区間の訂正（2026-08-13・レビュー指摘による訂正）。** 当初この節は
  「**変わるのが直列化区間であることは実測**」と断定していたが、これは**実測で反証される**。
  書き出した `.xlsx` を `unzip -p <book>.xlsx xl/sharedStrings.xml` で展開して生バイトを見ると、
  **`CR` はファイルに生のまま保存されている**（数値文字参照 `&#13;` への退避も無い。`grep -c '&#'` は 0）。

  | 入力 | `xl/sharedStrings.xml` の生バイト | POI で読み直した値 |
  |---|---|---|
  | `a` ＋ `CR` ＋ `b` | `3c 74 3e 61 0d 62 3c 2f 74 3e` ＝ `<t>a[CR]b</t>` | `a` ＋ `LF` ＋ `b` |
  | `a` ＋ `CRLF` ＋ `b` | `3c 74 3e 61 0d 0a 62 3c 2f 74 3e` ＝ `<t>a[CR][LF]b</t>` | `a` ＋ `LF` ＋ `b` |

  すなわち**値が変わるのは直列化区間ではなく、XML をパースする読み戻し区間**である。
  同じ節の 1 文前で「書き出したファイルを**読み直すと** `CR` が `LF` になっている」と書いていたのと
  矛盾していたのを、読み戻し区間へ揃えて訂正した。
  なお「メモリ上のブックでは `CR` が保たれている」という担保テストのアサート自体は正しく、残してある。
  ただしそれが示すのは「`XlsFormatWriter` 自身は値を変えていない」ことだけで、
  「直列化で失われた」ことの証明にはならない。
- **XLS-17（制御文字）とは区間が違う。** 並べると次のとおりで、同じダンプで同時に確かめている。

  | 課題 | 変化が起きる区間 | `xl/sharedStrings.xml` の生バイト | ファイルを見て気づけるか |
  |---|---|---|---|
  | XLS-17 制御文字 → `?` | **直列化区間** | `<t>a?b</t>`（`?` ＝ `3f` が焼き込まれている） | **気づける** |
  | XLS-18 `CR` → `LF` | **読み戻し（XML パース）区間** | `<t>a[CR]b</t>`（`CR` ＝ `0d` が残っている） | **気づけない** |
- 実測: 上表のとおり。例外にも警告にもならない。プローブ（2026-08-13）では
  `a`＋`LF`＋`CR`＋`b`（4 文字）→ `a`＋`LF`＋`LF`＋`b`（4 文字）、`a`＋`CR`＋`CR`＋`b`（4 文字）→
  `a`＋`LF`＋`LF`＋`b`（4 文字）、値全体が `CR` 1 文字 → `LF` 1 文字も観測しており、
  「置換であって削除ではない」ことと整合する（この 3 パターンに担保テストは置いていない）。
- 影響: `CR` を含むセル値が変換後に別の値になる。**気づけるかどうかは `CR` の現れ方で違う。**
  - `CRLF`: 長さが 4 → 3 と減るため、値でも長さでも差が出る。
  - **単独 `CR`: 長さが変わらないまま文字だけが入れ替わる。** 差分を長さで見る比較や
    「文字数が同じなら同じ」とみなす検査では**気づけない**。値そのものを突き合わせる後段のテストが
    あれば失敗するが、無ければ黙って変わったままになる。
  - **`.xlsx` をバイトで比較しても `CR` は残って見える**（上の生バイト表）。原因を追う人が
    「書き出したファイルに `CR` があるなら書き出しは正しい」と判断して探す場所を間違える。
    見るべきは XML パーサ（読み戻し）側であり、`XlsFormatWriter` でも `.xlsx` の中身でもない。
    XLS-17 は逆に `?` がファイルに焼き込まれるため、ファイルを見れば分かる。
- 判断: 記録に留める（XML 形式に由来し、変換ツール側では回避できない）。修正はこの作業では行わない。

### XLS-19 Excel のセル文字数上限（32767）を超える値がそのまま書き出される（影響度 低・記録のみ）

| 入力（データ行の値） | 書き出されるセル | 担保テスト |
|---|---|---|
| 32768 文字（上限＋1） | 文字列セル。32768 文字がそのまま書かれ、POI で読み直すと 32768 文字に戻る（切り詰めも例外も無い） | `XlsFormatWriterCellTypeTest#writesStringLongerThanExcelCellLimitAsStringCell` |
| 32767 文字（上限ちょうど） | 文字列セル。そのまま | `#writesStringOfExcelCellLimitLengthAsStringCell` |

- 原因: POI 3.8 の `XSSFCell#setCellValue(String)` は長さを検査せず、そのまま
  `XSSFRichTextString` にして `CTCell` へ載せる（`javap -c` で逆アセンブルして確認。上限値との比較が無い）。
  `XlsFormatWriter` も検査しない。
- 実測: 上表のとおり。POI での読み直しは成功する。
- 影響: Excel の仕様上不正な長さのセルを持つブックが生成されうる。**Microsoft Excel が実際に開けるかは未確認**
  （本リポジトリのテストは POI での読み直しまでしか確かめていない）。
- 判断: 受容できる（記録のみ）。NTF のテストデータで 32767 文字を超えるセルは現実的でなく、
  上限は POI と Excel の側の制約である。修正はこの作業では行わない。

### 課題としないと判断した観測結果（#22）

| 観測 | 判断 |
|---|---|
| `"100"` ／ `"007"` ／ `"=1+1"` がいずれも文字列セル（`CELL_TYPE_STRING`）で書かれ、数値セル・数式セルにならない | 妥当（`XlsFormatWriter` はすべての値を `setCellValue(String)` で書く。記法をそのまま保つという Writer の設計どおり） |
| `null` 値がリテラル `"null"` の文字列セルになる（空白セルにならない） | 妥当（Writer の Javadoc に明記された NTF の慣習。読み戻すと文字列 `"null"` になる非可逆は `XlsFormatWriterTest#roundTripsNullCellAsLiteralNullString` で固定済み） |
| 空文字 `""` が長さ 0 の文字列セルになる（`CELL_TYPE_BLANK` へ退化しない） | 妥当（空文字と値なしを Excel 上で区別できる形で保つ） |
| 出力先ディレクトリが存在しないとき、黙って作られて書き出しが成功する（F3-01） | 妥当（`Files.createDirectories` による意図した挙動。親に通常ファイルが居座り作れない場合は `UncheckedIOException` になることを `XlsFormatWriterTest#wrapsIoFailure` が固定済み） |
| 書き込み権限が無いとき `UncheckedIOException: failed to write Excel: <パス>` ＋ 原因 `AccessDeniedException` になる（F3-03） | 妥当（どのファイルを書けなかったかがメッセージから分かる。XLS-14 の読み取り側と対照的に、書き出し側はパスを載せている） |
| シート名に Excel の禁止文字（`/ \ ? * [ ] :`）があり、**それが切り詰め後の 31 文字に残る**場合は POI の `IllegalArgumentException` で止まり、ブックが作られない（F3-04） | 妥当（不正なブックを黙って書かず、どの文字がどの位置で不正かをメッセージが示す）。ただし**禁止文字が index 31 以降にあると切り詰めで消えて検査に到達しない**。この抜けは課題として XLS-16 の「影響」に記録した |
| 大文字小文字だけが違うシート名（`abc` と `ABC`）が同名と判定され、`IllegalArgumentException: The workbook already contains a sheet of this name` で止まる。ブックは作られない（F3-04） | 妥当（Microsoft Excel 自身もシート名の大文字小文字を区別しないため、POI の `containsSheet` が `equalsIgnoreCase` で比べるのは Excel の制約に沿っている）。固定する意味は、XLS-16 が原因として引用している機構（切り詰め＋`equalsIgnoreCase`）の両輪を実測で押さえることにある。担保テストは `XlsFormatWriterInvalidOutputTest#failsWhenSheetNamesDifferOnlyInCase` |

### 未確認（#22）

- **32767 文字超のセルを Microsoft Excel が開けるかは未確認**（XLS-19）。確かめているのは POI での読み直しまでである。
- **制御文字の置換（XLS-17）は `U+0000` / `U+0007` / `U+000B` / `U+001F` の 4 つで実測した。**
  XML 1.0 が禁じる符号位置すべてで同じかは未確認（サロゲート単独・`U+FFFE` 等は試していない）。
- **軸F の書き込み権限（F3-03）は POSIX 権限が効く環境でのみ検証している。**
  担保テストは確認用ファイルの作成が `AccessDeniedException` で拒否されることを前提条件として確かめ、
  拒否されない環境（root 実行・権限を無視するファイルシステム）では `Assume` でスキップする。
  本作業の実行環境（非 root・ext4）では実際に実行され PASS している。

---

## #23 辺③ 軸A・B・C・E の欠け補充で記録した課題

**掲載順**: 「凡例 → 並び順の原則」に従い、**検出できない**もの（XLS-21・XLS-20）を先に置き、
loud に失敗するもの（XLS-22）、記録のみのもの（XLS-23・XLS-24）を後に置く。
**XLS-24 は #23 のレビュー ラウンド3 で追加した**（挙動の不具合ではなく、Javadoc の主張と実装・担保の
食い違いの記録である。発見順のまま最後の ID になっている）。

以下はすべて中間モデルを `new XlsFormatWriter().write(...)` で実 `.xlsx` へ書き出し、
書き出したファイルを POI で開き直して（読み戻しの記述は `new XlsFormatReader().read(...)` で）
実測したものである（プローブ実行 2026-08-13 ＋ 担保テストの実行）。

**読み戻しの側も担保テストを持たせてある。** 下表の「読み戻すとどうなるか」は辺③の担保ではないが、
テストを置かないと本体パーサ・`PoiXlsReader` の挙動が変わったときに**辺③の担保テストは緑のまま
本書の記述だけが誤りになる**。そのため `XlsFormatWriterModelTest` の末尾 3 件
（`#dropsDefaultDataTypeBlockWhenReadBack` ／ `#promotesFirstDataRowToColumnNamesWhenEmptyColumnNamesAreReadBack` ／
`#failsToReadBackRecordWithoutFields`）が読み戻しを実検査する。
これらは軸要素の担保としては数えない（steering Rules フェーズ2（往復テストの扱い））。

### XLS-21 カラム名 0 件のブロックを書き出すと、読み戻しでデータ行がカラム名へ昇格し値が消える（影響度 中・**検出できない**）

| 入力（中間モデル） | 書き出される版面 | 読み戻した中間モデル | 担保テスト |
|---|---|---|---|
| `SETUP_TABLE=T`／`columnNames=[]`／`rows=[[v1, v2]]` | 識別行 `SETUP_TABLE=T`／カラム名行 **空セル 2 個**（データ行の幅へ矩形整形される）／データ行 `v1`, `v2` | `columnNames=[V1, V2]`（値がカラム名になり、テーブル経路の大文字化が掛かる）／`rows=[]`（**データ行 0 件**） | 版面: `XlsFormatWriterModelTest#writesEmptyHeaderRowWhenColumnNamesAreEmpty`／読み戻し: `#promotesFirstDataRowToColumnNamesWhenEmptyColumnNamesAreReadBack` |

- 原因: `XlsFormatWriter#render` はカラム名行を版面幅へ矩形整形するため、カラム名 0 件でも
  **空セルだけの行**が出る。この行は `PoiXlsReader#isBlankLine` が空行として読み飛ばすため
  （`issues.md` **XLS-05** と同じ機構）、次の行＝先頭のデータ行がカラム名行として解釈される。
- 実測: 上表のとおり。例外にも警告にもならない。
- 影響: 値がカラム名へ化け、データ行がすべて消える。変換後にテストが通ってしまえば発見できない。
- **到達経路は未確認。** 中間モデル上は表現できる（`ColumnRowDataBlock` の `columnNames` は空許容）が、
  辺①・辺②のどちらかがこの形（カラム名 0 件かつ値を持つデータ行）を生むかは確かめていない。
  辺①でカラム名が 0 件になるのはマーカー列だけのブロック（**XLS-08**）だが、そのときのデータ行は
  セルを 1 つも持たない行であり、本課題の入力とは異なる。
- 判断: **仕様として不適切**（データ損失）。XLS-05／XLS-08 と同じ「空行として読み飛ばされる」機構の派生であり、
  単独で直すものではない。XLS-05 の対応を検討する際に併せて判断すること。修正はこの作業では行わない。

### XLS-20 `DataType.DEFAULT` の扱いが辺③と辺④で非対称で、辺③が書いたブロックは読み戻すと消える（影響度 中・**検出できない**）

| 辺 | `DataType.DEFAULT` のブロックを渡したときの挙動 | 担保テスト |
|---|---|---|
| 辺③ 中間モデル→Excel | **そのまま書き出す。** 識別セルは `DEFAULT=T`（グループ ID 付きなら `DEFAULT[g1]=T`）。ヘッダ色はその他グループ | `XlsFormatWriterModelTest#writesDefaultDataTypeMarker` |
| 辺④ 中間モデル→YAML | **例外で弾く。** `IllegalArgumentException: unsupported DataType: DEFAULT` | `YamlFormatWriterTest#serialize_unsupportedDataType_throws` |
| 辺① Excel→中間モデル（辺③の出力を読み戻した場合） | **ブロックが黙って消える**（`sections` は 1 件、`blocks` は 0 件）。例外も警告も無い | `XlsFormatWriterModelTest#dropsDefaultDataTypeBlockWhenReadBack` |

- 原因:
  - 辺③ — `XlsFormatWriter#marker` が `block.getDataType().getName() + getGroupId() + "=" + getIdentifier()` を
    組み立てるだけで、タイプを絞る分岐を持たない。
  - 辺④ — `YamlFormatWriter` の `DataType` → セクションキー変換が既知 13 種の `switch` で、
    `default:` が `IllegalArgumentException("unsupported DataType: " + type)` を送出する。
  - 辺① — `src/main/java/nablarch/test/core/reader/TestCoreReaderAdapter.java` の
    `HeaderCollector#parse` が、先頭セルから判定したデータタイプが `DEFAULT` の行を `continue` で読み飛ばす
    （`issues.md` **XLS-10** と同じ機構。`DEFAULT` は「既知のどの名前にも一致しない」の意味でも使われる）。
- 実測: 上表のとおり。
- 影響: 同じ中間モデルが出力形式によって「書ける」「例外」に分かれる。さらに辺③で書けた `.xlsx` は
  読み戻すとブロックごと消えるため、Excel→YAML と Excel→Excel で結果が変わる。
  ただし `DEFAULT` を持つブロックが中間モデルに現れる経路は辺①・辺②のいずれにも無い（§0.8-7）ため、
  現実に踏むのは中間モデルを手で組み立てた場合に限られる。
- 判断: **仕様として不適切**（非対称）。揃えるなら辺③も辺④と同じく弾くのが筋である
  （書けても読み戻せないため、書ける側に価値が無い）。修正はこの作業では行わない。

### XLS-22 フィールド 0 件のレコードレイアウトは、書き出せるが読み戻せない `.xlsx` になる（影響度 低・例外で止まるため検出できる）

| 入力（中間モデル） | 書き出される版面 | 読み戻し | 担保テスト |
|---|---|---|---|
| `SETUP_FIXED=f.dat`／`RecordLayout("data", fields=[], rows=[[v]])` | 識別行／名前行 `data`, 空セル／型行 空セル 2 個／長さ行 空セル 2 個／データ行 空セル, `v` | `IllegalStateException: can't get data. …` ← 原因 `IllegalStateException: directive or data names row must have two columns at least. [data]` | 版面: `XlsFormatWriterModelTest#writesRecordWithoutFieldColumnsWhenFieldsAreEmpty`／読み戻し: `#failsToReadBackRecordWithoutFields` |

- 原因: フィールドが 0 件だと名前行がレコード種別セル 1 個だけになる（版面幅への矩形整形で右は空セルになるが、
  本体パーサは空セルを行の使用範囲に数えない）。本体 `DataFileParser` が名前行に 2 列以上を要求するため弾かれる。
  これは辺①で C-17（`RecordLayout.fields` 空）を**到達不能**と判定した根拠と同じ機構である
  （「到達不能と判定した軸要素（#20 で新たに判明したもの）」の C-17 行）。
- 実測: 上表のとおり。書き出し自体は成功し、例外になるのは読み戻し側である。
- 影響: `XlsFormatWriter` の Javadoc は「本体パーサが読み戻せる版面で書く」と謳っているが、この入力では
  成り立たない。ただし読み戻しは loud に失敗するため、黙って壊れることはない。
  また辺①はこの形の中間モデルを生成できない（上記のとおり到達不能）ため、辺②由来の YAML で
  `fields: []` を書いた場合だけが到達経路になる（辺②側の到達可否は **未確認**）。
- 判断: 記録に留める。**同種の前提崩れを書き出し時に弾く番人は既に 1 つある**
  （`XlsFormatWriter#appendRecords` は 2 レコード目以降のレコード種別が空だと `IllegalStateException` を送出する）。
  フィールド 0 件も同じ思想で書き出し時に弾くのが筋だが、修正はこの作業では行わない。

### XLS-23 セクション 0 件のコンテナから、シートを 1 枚も持たない `.xlsx` が黙って書き出される（影響度 低・記録のみ）

| 入力（中間モデル） | 書き出されるブック | 担保テスト |
|---|---|---|
| `sections=[]` のコンテナ | 例外にならずファイルが作られる（POI で開き直すと `getNumberOfSheets()` が **0**） | `XlsFormatWriterModelTest#writesWorkbookWithoutSheetsWhenContainerHasNoSections` |

- 原因: `XlsFormatWriter#build` は `container.getSections()` をループするだけで、空を弾かない。
  POI 3.8 の `XSSFWorkbook#write` もシート 0 枚を拒否しない。
- 実測: 上表のとおり。書き出したファイルを `XlsFormatReader#read` で読むと
  `IllegalArgumentException: sheet not found. path=[…] sheet=[s]` になる（プローブ実測。担保テストは置いていない
  — シート名の指定が要る API であり「シートが無い」ことと「そのシートが無い」ことを区別できないため）。
- 影響: **Microsoft Excel がシート 0 枚のブックを開けるかは未確認**（本リポジトリのテストは POI での
  読み直しまでしか確かめていない）。Excel のブックは最低 1 シートを要するのが通例であり、
  開けない可能性がある。
- 判断: 受容できる（記録のみ）。セクション 0 件のコンテナは辺①・辺②のいずれも生成しない
  （どちらも `Collections.singletonList(section)` を返す。§0.8-6）ため、現実には中間モデルを
  手で組み立てた場合に限られる。修正はこの作業では行わない。

### XLS-24 `XlsFormatWriter` は「送信系は FW 制御ヘッダを書かない」を実装しておらず、その性質は未担保である（影響度 低・記録のみ／`src/main` は無変更）

**本項は #23 のレビュー ラウンド3 の指摘で判明した（2026-08-13）。** 挙動の不具合の記録ではなく、
**Javadoc の主張と実装・担保の食い違いの記録**である。

| 主張している場所 | 主張の内容 | 実装 |
|---|---|---|
| `XlsFormatWriter` のクラス Javadoc の版面一覧 | 「**送信系 4 種**: MESSAGE と同型だが FW 制御ヘッダは無く、データ行の列 0 は `no`（連番）」 | `layoutMessage` は `appendKeyValueRows(l, block.getFwHeaderFields())` を**データタイプで分岐せず無条件に**呼ぶ。分岐しているのは `no` 列（`sendSync`）だけである |

- **原因ではなく前提**: 送信系のブロックに FW 制御ヘッダ行が出ないのは「送信系だから」ではなく、
  中間モデル側の契約で `fwHeaderFields` が常に空 Map になるからである。契約を書いているのは
  `MessageDataBlock` の Javadoc（「`expected_request_*`／`response_*` 経路は空 Map とする（仕様 MS-04）」）と、
  `XlsFormatReader`／`YamlFormatReader`／`TestCoreReaderAdapter`／`YamlTestCoreAdapter` の
  「FW 制御ヘッダは送信系では常に空」である。**`XlsFormatWriter` 自身は何も保証していない。**
- **未担保であることの実測（変異・2026-08-13）**: `layoutMessage` を「送信系のときだけ
  `appendKeyValueRows(l, block.getFwHeaderFields())` を呼ばない」——すなわち Javadoc が謳う性質を
  実装した形——へ一時的に変異させて全件実行したところ、**`Tests run: 428, Failures: 0, Errors: 0, Skipped: 0`**
  であった。**1 件も落ちない＝ `src/test` に両者を区別するテストが存在しない**。
  変異は確認後に戻し、`git diff HEAD -- src/main | wc -l` → **0** を確かめた。
  変異の手順と、送信同期の `MessageDataBlock` に非空 `fwHeaderFields` を渡すテストが `src/test` に
  0 件であることの静的な裏取りコマンドは `inventory.md` **§3.1-3 の「担保の穴: 送信系の FW 制御ヘッダ」**にある。
- **影響**: 送信系のブロックに非空の `fwHeaderFields` を持つ中間モデルを渡すと、
  `XlsFormatWriter` は Javadoc の主張に反して FW 制御ヘッダ行を書き出す。この版面を
  `XlsFormatReader` が読み戻せるかは**未確認**である（読み手側は送信系に FW 制御ヘッダ行が
  来ることを想定していない旨を Javadoc に書いている）。ただしこの入力は現状のどの経路でも生じない。
- **判断**: 受容できる（記録のみ）。到達経路が無く、修正すると「契約の二重実装」になる。
  ただし**担保の穴としては開示する**（steering Rules フェーズ2）。修正はこの作業では行わない。
  テストも足していない（足すなら「送信系に非空 `fwHeaderFields` を渡すと何が起きるか」を
  現状挙動として固定する 1 件になるが、それは中間モデルの契約が禁じている入力を作ることになるため、
  足すかどうかは辺③以外の辺（辺②・辺④）の扱いと揃えて判断すべきである）。

### 課題としないと判断した観測結果（#23）

| 観測 | 判断 |
|---|---|
| `EXPECTED_FIXED` の識別セルが `EXPECTED_FIXED=exp.dat` になり、固定長なので長さ行が出る | 妥当（`marker` と `layoutFile` の設計どおり） |
| `EXPECTED_VARIABLE` の識別セルが `EXPECTED_VARIABLE[g2]=exp.csv` になり、可変長なので長さ行が出ない | 妥当（同上。グループ ID は中間モデルが整形済みの `[g2]` を保持しそのまま連結される） |
| `DataType.DEFAULT` のヘッダ色がその他グループ（`HEADER_OTHER`）になる | 妥当（`BlockLayout#headerFill` の Javadoc が `DEFAULT` を「それ以外」に明記している） |
| ブロック 0 件のセクションが、行を 1 行も持たないシートになる（C-04 / E-1(0)） | 妥当（読み戻すとブロック 0 件のセクションに戻る＝往復が安定する。実測） |
| データ行 0 件のブロックが識別行とカラム名行だけになる（C-09 / E-2(0)） | 妥当（読み戻すと `rows=[]` に戻る。実測） |
| レコードレイアウト 0 件のファイル／メッセージが識別行とディレクティブ行（FW 制御ヘッダ行）だけになる（C-12 / C-15 / E-3(0)） | 妥当（読み戻すと `records=[]` に戻る。実測） |
| 値行 0 件のレコードレイアウトが名前行・型行・長さ行だけになる（C-18） | 妥当（読み戻すと `rows=[]` に戻る。実測） |
| `MessageDataBlock.directives` に値があると、ディレクティブ行が FW 制御ヘッダ行の**上**に出る（C-13） | 妥当（`layoutMessage` の記述順どおり。読み戻すと `directives` と `fwHeaderFields` に分かれて戻ることを実測） |
| C-12 の入力に書いていない `file-type` が、読み戻した `directives` に現れる | 既知（**XLS-07**。器が既定ディレクティブを注入する） |

### 未確認（#23）

- **シート 0 枚のブックを Microsoft Excel が開けるかは未確認**（XLS-23）。確かめているのは POI での読み直しまでである。
- **XLS-21 の到達経路は未確認。** カラム名 0 件かつ値を持つデータ行という中間モデルを、辺①・辺②のどちらかが
  生成するかは確かめていない（辺①のマーカー列だけのブロックは値を持たない行になる＝ XLS-08）。
- **XLS-22 の到達経路（辺②の YAML で `fields: []` を書けるか）は未確認。** 辺①では到達不能である（#20 で確認済み）。

### ヘルパ抽出の要否（#22 からの持ち越し。#23 で判断を確定・2026-08-13）

`checks/task-22.md` が「中間モデル組み立てヘルパの抽出の**要否は #23 で判断する**」と書いて #23 へ委ねていた。
結論は次のとおり。

| ヘルパ | 本体 | 判断 |
|---|---|---|
| `line(Sheet, int)` | `XlsFormatWriterTest` ／ `XlsFormatWriterModelTest` の 2 定義が完全一致 | **`XlsFixture` へ抽出した** |
| `cell(Sheet, int, int)` | 定義は 1 つ | **`XlsFixture` へ抽出した**（`line` と対のため） |
| `row(String...)` | `Arrays.asList` 1 行。`xls` / `yaml` / `converter` / `core.reader` の 4 パッケージ 8 ファイルに定義 | **現状維持** |
| `map(String...)` | 2 定義が完全一致 | **現状維持** |
| `container(...)` | 引数の形が 5 通り | **現状維持** |
| 往復（`roundTrip` ／ `writeAndReadBack`） | 2 定義が同一ロジック | **現状維持** |
| `causeOf(Throwable)` | `XlsFormatReaderInvalidInputTest` ／ `XlsFormatWriterModelTest` の 2 定義が完全一致 | **現状維持** |

**判断の理由**

- **境界は「POI のブック・シートを直接触るか」に引いた。** `XlsFixture` は既に `static Workbook open(Path)` で
  パッケージの POI 読み出し側を担っている。同クラスの Javadoc が線を引いているのは「**中間モデル**組み立て
  ヘルパとは対象レイヤが異なる」であって、シート読み出しユーティリティは元から対象外ではない。
  `line` / `cell` はこちら側に入る。
- **`cell` は写しが 1 件だったが `line` と一緒に移した。** 対になるアクセサを分けて置くと、
  次の Writer 系テストクラス（すでに 4 本ある）が `cell` 側の写しを作る。
- **`row` は抽出しない。** 4 パッケージ 8 ファイルに定着したイディオムで、集約するとパッケージをまたぐ
  依存が増えるだけである（本体は 1 行）。
- **`map` は抽出しない。** 写しは 2 件あるが**中間モデル組み立て側**であり、`XlsFixture` の Javadoc が
  明示する境界の向こうにある。
- **`container` は抽出しない。** 5 定義はいずれも**そのクラスのブック名・シート名の決め方に合わせた局所版**で
  あり、共有版へ寄せるとその決め方を呼び出し側の引数へ戻すことになる。具体的には
  `XlsFormatWriterModelTest#container` は `TemporaryFolder` がメソッドごとに別ディレクトリを与える前提で
  シート名を定数 `SHEET` に固定した 2 引数版であり、`XlsFormatWriterTest` の 3 引数版へ寄せると
  **呼び出し 17 か所すべてが同じシート名リテラルを書く**ことになる。
  `XlsFormatWriterCellTypeTest#container(String)` は検証対象の値 1 個だけを受ける版、
  `XlsFormatWriterInvalidOutputTest#container(String, String...)` はシート名の並びだけを受ける版で、
  いずれも同じ理由による。
- **往復ヘルパも抽出しない。** ロジックは同一だが、`XlsFormatReader` を駆動する＝**辺①側の SUT を呼ぶ**
  ヘルパであり、`XlsFixture`（POI だけを触る）にも中間モデル組み立てにも属さない。往復は steering Rules
  フェーズ2 で正式担保に数えない位置づけであり、共通基盤へ格上げすると担保として使われやすくなる副作用がある。
- **`causeOf` も抽出しない。** 移さない理由は往復ヘルパと同じで、`XlsFixture` にも中間モデル組み立てにも
  属さないため境界のどちら側でもないこと、および本体が 3 行の「原因例外を 1 段たどるだけ」のアサートで、
  共有先を新設するほうが読み手の追跡経路を増やすことである。**写しであること自体はソース側の Javadoc でも
  開示している**（`XlsFormatWriterModelTest#causeOf` の「`XlsFormatReaderInvalidInputTest` の同名ヘルパと同じ」）。
  3 件目の写しが生まれたときは `XlsFixture` ではなく異常系テスト共通の置き場を新設して判断し直す。

**記録先**: 判断そのものは `XlsFixture` のクラス Javadoc（「本クラスが引き受けるヘルパの範囲」）に置いた。
`causeOf` は `XlsFixture` の守備範囲外なので同 Javadoc には書かず、本節と
`XlsFormatWriterModelTest#causeOf` の Javadoc に置いた。

### #27 への申し送り（#23 のレビュー ラウンド3 で判明・2026-08-13）

以下 2 件は **#23 では直さない**（テストも台帳の表も変えていない）。逆引きの正である
`coverage/axis-matrix.md` を作る #27 で、実物を確認したうえで扱うこと。

**1. 辺③の軸E で `E-1(1 件)` と `E-4(1 件)` が台帳のどこにも現れない**

- 事実: §3.1 の 40 行の軸E 欄に現れる値は `E-1(複数)`／`E-2(1)`／`E-2(複数)`／`E-3(1)`／`E-3(複数)`／
  `E-4(複数)` だけで、**`E-1(1)` と `E-4(1)` は 1 行も無い**。
  導出コマンド（§3.1 の表本体 40 行から軸E 欄だけを取り出して数える。行範囲は
  `grep -n "^### 3.1 " <台帳>` で見出しを引き、表本体はその 4 行後から 40 行である）:

  ```sh
  cd /home/tie303177/work/nablarch/nablarch-testing-converter
  h=$(grep -n "^### 3.1 " .rn/ntf-test-data-converter/coverage/inventory.md | cut -d: -f1)
  awk -v s=$((h+4)) 'NR>=s && NR<s+40' .rn/ntf-test-data-converter/coverage/inventory.md \
    | awk -F'|' '{print $8}' | sort | uniq -c
  ```

- 事実: §3.3（辺③ 未担保一覧）の軸E 行が挙げているのは `E-1(0 件)`／`E-2(0 件)`／`E-3(0 件)` の
  3 要素だけで、**`E-1(1)`／`E-4(1)` は未担保一覧にも載っていない**。
- したがって台帳の上では「**担保テストが挙がっていないのに、未担保一覧にも載っていない**」という
  穴の形になっている。#23 のレビューで A-12〜A-14 が見つかったのとまったく同じ形である。
- **実体としては担保されている見込みが高い。** 1 セクション 1 ブロックを渡すテスト（`container(...)` を
  使う辺③のテストの大半。例: `XlsFormatWriterTest#writesMessageBlock` は
  `container("book", "sheet", message)` ＝ セクション 1・ブロック 1）が `E-1(1)` と `E-4(1)` の
  両方を通している。ただし**#23 では変異で確かめていない**ため「見込み」であり、
  #27 で実物にあたって埋めること。

**2. 送信同期 4 種の担保が 2 クラスに分かれ、フィクスチャがほぼ複製されている**

- 事実: A-11（`EXPECTED_REQUEST_HEADER_MESSAGES`）を担保するのは
  `XlsFormatWriterTest#writesSendSyncMessageWithSequenceNo`、A-12〜A-14 を担保するのは
  `XlsFormatWriterModelTest#writesExpectedRequestBodyMessagesMarker` ／
  `#writesResponseHeaderMessagesMarker` ／ `#writesResponseBodyMessagesMarker` である。
- 事実: 両者の入力はほぼ複製である。レコード種別 `no`、フィールド定義
  （`requestId` 半角 20 ／ `resendFlag` 半角 1）、データ行 2 行（`RM21AA0104_01`, `0` ／
  `RM21AA0104_02`, `1`）、グループ ID `[case1]`、識別子 `RM21AA0104_01` がすべて一致し、
  違うのは**データタイプと、`build`（メモリ上のブック）か `write`＋開き直しか**だけである
  （`XlsFormatWriterTest#writesSendSyncMessageWithSequenceNo` と
  `XlsFormatWriterModelTest#sendSyncMessage` を並べて確認）。
- 問題: **5 種目の送信系が増えたときにどちらへ足すか決まらない。** 4 種が 1 か所に揃っていないため、
  「送信系の版面」を確かめたい読み手は 2 クラスを開くことになる。
- 申し送り: 将来 4 種を 1 か所へ揃えることを **#27 以降の候補**とする。#23 で動かさないのは、
  レビュー対応でアサート内容を変えない方針を採っているためである。

---

## #24 辺② 軸D（YAML スカラー 12 ケース）・軸F（異常系 5 ケース）で記録した課題

**課題 ID は `YML-nn`（Excel 側の `XLS-nn` とは別系列）を用いる。** 既存 24 件はすべて Excel 経路の課題であり、
YAML 経路の課題を同じ系列に続けると読み手が Excel 側の課題と取り違えるためである。
ID は発見順に振り、振り直さない。

以下はすべて `YamlFixture` が書き出した実 `.yaml` を `new YamlFormatReader().read(...)` に渡して
実測した結果である（`loadRawMap` を差し替える in-memory 経路ではスカラー解決もスキーマ検証も通らない）。

### YML-01 `~` ／ `NULL` は NULL にならず文字列になる（影響度 別枠・**変換時には検出できない**／帰属は yaml 側）

**テストで担保した変種**（実 `.yaml` を読んで観測した結果をアサートしている）:

| 入力（`rows` の値） | 中間モデルへ入る結果 | 担保テスト（`YamlFormatReaderScalarTest#`） |
|---|---|---|
| `null`（引用符なし・小文字） | **Java `null`** | `readsUnquotedNullAsJavaNull` |
| 値なし（`- V:`） | **Java `null`** | `readsOmittedValueAsJavaNull` |
| `~` | **文字列 `"~"`** | `readsTildeAsString` |
| `NULL`（大文字） | **文字列 `"NULL"`** | `readsUppercaseNullAsString` |
| `"null"`（引用符あり） | 文字列 `"null"` | `readsQuotedNullAsString` |

**正規表現から導出した事実**（テストは書いていない。下表 4 の `JsonScalarResolver` の `NULL` パターンから
機械的に導ける範囲であり、担保テストがあると読ませないためここに分ける）:

| 入力（`rows` の値） | 導出される結果 | 導出の根拠 |
|---|---|---|
| `Null`（先頭のみ大文字） | 文字列 `"Null"` | `NULL` ＝ `^(?:null)$` に一致しない（下表 4） |

- **6 者はいずれもスキーマを通る仕様内の入力である。** 値に課される型は
  `$defs.table_data.properties.rows.items.additionalProperties.type` ＝ `["string","null"]`
  （`list_map_data` も同じパス。`record_fragment` は `$defs.record_fragment.properties.rows.items.items.type`）で、
  文字列も `null` も許される。にもかかわらず Java `null` になるのは前 2 者だけで、
  **`~` / `NULL` / `Null` は文字列としてテストデータへ入る**。作成者が NULL のつもりで書いた値が黙って文字列になる。
- **`~` が NULL にならないのは YAML の標準的な null タグ解決と異なる。** どのコードがそうしているかは次のとおり
  （4 段すべてを一次情報で確認した）。

  | # | 位置 | 事実 |
  |---|---|---|
  | 1 | `nablarch/test/core/reader/yaml/YamlLoader.java#load`（yaml の sources jar 内） | `LoadSettings.builder().setAllowDuplicateKeys(false).build()` を使い、**`setSchema` を呼んでいない**（＝ SnakeYAML Engine の既定スキーマがそのまま効く） |
  | 2 | `org.snakeyaml.engine.v2.api.LoadSettingsBuilder` の引数なしコンストラクタ | フィールド `schema` に `new JsonSchema()` を代入する（既定値） |
  | 3 | `org.snakeyaml.engine.v2.schema.JsonSchema` のコンストラクタ | フィールド `scalarResolver` に `new JsonScalarResolver()` を代入する |
  | 4 | `org.snakeyaml.engine.v2.resolver.JsonScalarResolver` の静的初期化子 | `NULL` ＝ `^(?:null)$`（対して同パッケージの `CoreScalarResolver` は `^(?:~\|null\|Null\|NULL\| )$`）。`BOOL` ＝ `^(?:true\|false)$`、`INT` ＝ `^-?(0\|[1-9][0-9]*)$` |

  再現コマンド（2〜4）:

  ```sh
  cd "$(mktemp -d)" \
    && unzip -oq ~/.m2/repository/org/snakeyaml/snakeyaml-engine/3.0.1/snakeyaml-engine-3.0.1.jar \
    && /usr/lib/jvm/temurin-17-jdk-amd64/bin/javap -p -c \
         org/snakeyaml/engine/v2/api/LoadSettingsBuilder.class \
         org/snakeyaml/engine/v2/schema/JsonSchema.class \
         org/snakeyaml/engine/v2/resolver/JsonScalarResolver.class \
         org/snakeyaml/engine/v2/resolver/CoreScalarResolver.class \
       | grep -E 'JsonSchema|JsonScalarResolver|String \^'
  ```

  再現コマンド（1）。**`setSchema` が 1 行もヒットしないこと**が事実そのものである
  （ヒットするのは `LoadSettings` の import と `LoadSettings.builder()` の 2 行、および下の Javadoc 1 行）:

  ```sh
  cd "$(mktemp -d)" \
    && unzip -oq ~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT-sources.jar \
    && grep -n 'LoadSettings\|setSchema\|Core Schema' nablarch/test/core/reader/yaml/YamlLoader.java
  ```

- **`YamlLoader` のクラス Javadoc は実挙動と食い違っている。** 同 Javadoc は
  「デフォルトの Core Schema（YAML 1.2）が適用されるため、`no`/`yes`/`on`/`off` は文字列として扱われる」と
  書いているが、上の 2〜3 のとおり実際に適用されるのは **`JsonSchema`（＝ `JsonScalarResolver`）**である。
  `yes` などが文字列になる結論は両スキーマで同じだが、**`~` / `Null` / `NULL` の扱いは両者で異なる**
  （Core なら NULL、Json なら文字列）。Javadoc の記述どおりであればこの課題は起きない。
- **帰属は converter ではなく yaml 側である。** converter（`YamlFormatReader`）は
  `YamlTestCoreAdapter#loadRawMap` → `YamlLoader#load` が解決した値を受け取るだけで、
  スカラー解決には関与しない。したがって修正するとすれば yaml 側（`YamlLoader` の
  `LoadSettings` 構成、またはスキーマ／Javadoc）である。
- **影響度を「別枠」とした理由**: 本課題では**変換結果は入力と一致する**（文字列 `"~"` が入り
  文字列 `"~"` が出る）。食い違うのは作成者の意図と NTF 実行時の解釈の側である。
  凡例の高／中／低はいずれも「変換結果が入力と一致するか」で定義されているため当てはまらず、
  かといって「低＝仕様として受容できると判断した」でもない（下の判断のとおり**仕様として不適切**である）。
  この 1 件のために凡例を 1 段拡張し、**影響度 別枠**とした（コーディネータ判断・2026-08-14）。
  並び順の原則（検出できるかを優先）は変えていない。
- 判断: **仕様として不適切**（NULL のつもりの記述が黙って文字列になる）。ただし帰属は yaml 側であり、
  本作業では修正しない（`src/main` 無変更）。

### YML-02 送信系で `group_id` を省略したエントリがブロックごと黙って消える（影響度 中・**検出できない**）

| 入力 | 中間モデルへ入る結果 | 担保テスト |
|---|---|---|
| `response_body_messages` に `group_id` 無しの `id: "DROP"` 1 件と `group_id: "g"` の `id: "KEEP"` 1 件 | ブロックは **1 件**（`KEEP` だけ）。`DROP` はブロックもレコードも中間モデルに現れない。**例外は出ない** | `YamlFormatReaderRealFileTest#dropsSendSyncEntryWithoutGroupIdFromRealYaml` |

- **この入力はスキーマ上の仕様内である。** `$defs.group_message_data.required` も
  `$defs.expected_request_message_data.required` も `["id","records"]` だけで、**`group_id` を要求していない**。
  さらに次の 3 か所が**省略が正当な使い方であることを明示している**。
  **引用元の JSON パスを明示し、再現コマンドを差し替えた（2026-08-14・#24 のレビュー指摘による訂正）。**
  当初は定義レベルの description の文言をプロパティレベルの description の引用として書いており、
  併記していた再現コマンドを実行しても引用文が出てこなかった。

  | 引用元の JSON パス | 引用（逐語） |
  |---|---|
  | `$defs.group_message_data.description`（定義レベル） | 「group_id を省略した場合は経路 B として動作する」（同じ description の前段に「(B) MockMessagingContext / MockMessagingClient 経路では id で照合して先着1件収集する（group_id 不要）」とある） |
  | `$defs.group_message_data.properties.group_id.description` | 「MockMessagingContext / MockMessagingClient 経路では参照されないため省略可」 |
  | `$defs.expected_request_message_data.properties.group_id.description` | 「省略時は id 直接指定（先着1件）で動作する」 |

  再現コマンド（上表の 3 つの引用がすべて出力に現れる）:

  ```sh
  python3 -c "
  import json,zipfile,os
  z=zipfile.ZipFile(os.path.expanduser('~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT.jar'))
  d=json.loads(z.read('nablarch/test/ntf-testdata-yaml-schema.json').decode('utf-8'))
  for k in ['group_message_data','expected_request_message_data']:
      print('\$defs.'+k+'.required =', d['\$defs'][k]['required'])
      print('\$defs.'+k+'.description =', d['\$defs'][k]['description'])
      print('\$defs.'+k+'.properties.group_id.description =', d['\$defs'][k]['properties']['group_id']['description'])
  "
  ```

- 原因: `YamlFormatReader#addSendSyncBlocks` は `rawGroupsInOrder(yaml, sectionKey)` を回してブロックを作る。
  `rawGroupsInOrder` は `group_id` が **非 null のエントリだけ**を列挙するため、`group_id` の無いエントリは
  どのグループにも属さず、ブロック生成のループに一度も入らない。
  （`src/main/java/nablarch/test/tool/converter/yaml/YamlFormatReader.java` の `addSendSyncBlocks` と
  `rawGroupsInOrder`。同メソッドの Javadoc は「`group_id` 必須」と書いているが、上のとおり
  スキーマは必須にしていない。Javadoc の記述と一次情報が食い違っている。）
- 実測: 上表のとおり。**例外にならない**（担保テストが固定している）。
  なお XLS-10／XLS-13 が行っているような「WARNING 以上のログが 0 件」というアサートは本課題では
  行っていないため、**警告が 1 件も出ないことは未確認である**（`java.util.logging` のハンドラを
  付けていない）。「検出できない」の根拠は、例外にならずブロックが消えるという観測に置く。
- 影響: 送信系で `group_id` を省略した（＝ id 直接指定で使うつもりの）エントリは、変換後の成果物から
  ブロックごと消える。入力と出力を突き合わせない限り気づけない。
- `nablarch-example-web`（サンプルアプリ）由来の変換出力 YAML には発現していない
  （送信系セクション自体が無い。`grep -rn 'expected_request_\|response_header_messages\|response_body_messages'
  src/test/java/nablarch/test/tool/converter/SampleConversionTest/` → ヒット 0）。
  ただし辺②の入力は本来**手書きの YAML** であり、対象PJの実データでの発現は未知である。
- 判断: **仕様として不適切**（スキーマが仕様内と認める入力が黙って落ちる。少なくとも
  「`group_id` 無しのエントリを drop した」と報せるべきである）。ただし本作業では修正しない
  （`src/main` 無変更）。**修正するとしたら本リポジトリ内で完結する**（原因コードが `src/main` にあるため）。

### YML-03 `record_type: FW_HEADER` のレコードが黙って捨てられる（影響度 中・**検出できない**）

| 入力 | 中間モデルへ入る結果 | 担保テスト |
|---|---|---|
| `messages` に `record_type: "FW_HEADER"` のレコード 1 件だけ（`fw_header:` は書かない） | ブロックは生成されるが `records` **0 件**・`fwHeaderFields` **0 件**。書いたフィールド定義とデータ行が消える。**例外は出ない** | `YamlFormatReaderRealFileTest#dropsFwHeaderNamedRecordFromRealYaml` |

- **この入力はスキーマ上の仕様内である。** `$defs.record_fragment.properties.record_type` に `enum` は無く、
  その description は「メッセージング系（messages / expected_request_\* / response_\*）では NTF 内部で常に
  `"default"` に置換されるため実行時の挙動に影響しない（**可読性のために任意の名前を記述してよい。
  FW_HEADER のような予約値はない**）」と書いている。あわせて `$defs.message_data.properties.records` の
  description は「FW 制御ヘッダは fw_header に記述するため records には含めない（**旧形式の
  record_type: FW_HEADER は廃止**）」と書いている。**スキーマの description は 2 か所で「FW_HEADER は
  予約値ではない／廃止された」と述べている**が、実装は予約値として扱い続けている。

  再現コマンド:

  ```sh
  python3 -c "
  import json,zipfile,os
  z=zipfile.ZipFile(os.path.expanduser('~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT.jar'))
  d=json.loads(z.read('nablarch/test/ntf-testdata-yaml-schema.json').decode('utf-8'))
  print(d['\$defs']['record_fragment']['properties']['record_type'])
  print(d['\$defs']['message_data']['properties']['records']['description'])
  "
  ```

- 原因: 落としているのは **converter と本体器の両方**である。
  - 本体器（yaml 側）: `nablarch/test/core/reader/yaml/YamlFileBuilder.java` の `buildFragmentsInternal` が
    `if (skipFwHeader && FW_HEADER_RECORD_TYPE.equals(recordType)) { continue; }` で断片を作らない
    （`skipFwHeader` はメッセージ系・送信系で真）。`FW_HEADER_RECORD_TYPE` は
    `nablarch/test/core/reader/yaml/YamlSection.java` で `"FW_HEADER"` と定義されている。
  - converter: `YamlFormatReader#recordsWithoutFwHeader` が同じ名前のレコードを原文側からも除く。
    除かなければ「器の断片数と原文レコード数の不一致」で `IllegalStateException` になるため、
    converter 側だけを直しても解決しない。

  再現コマンド:

  ```sh
  cd "$(mktemp -d)" \
    && unzip -oq ~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT-sources.jar \
    && grep -n 'FW_HEADER_RECORD_TYPE' nablarch/test/core/reader/yaml/YamlFileBuilder.java nablarch/test/core/reader/yaml/YamlSection.java
  ```

- 実測: 上表のとおり。`FW_HEADER` レコードに本文レコードを 1 件足した入力では本文だけが残ることも、
  送信系（`response_body_messages`）でも同じく落ちることもプローブで確認した（いずれも例外にならない）。
  この 2 つはプローブでの観測であり**テストとしては固定していない**（担保テストは上表の 1 件のみ）。
  YML-02 と同じく、警告が 1 件も出ないことは未確認である。
- 影響: スキーマの description を読んで「FW_HEADER は予約値ではないので可読性のために使ってよい」と
  判断した作成者が `record_type: "FW_HEADER"` と書くと、そのレコードのフィールド定義もデータ行も
  変換後の成果物から消える。入力と出力を突き合わせない限り気づけない。
  なお NTF 実行時も同じ器（`YamlFileBuilder`）が同じレコードを落とすため、**converter の変換は
  NTF の解釈に忠実である**。食い違っているのは**スキーマの description と実装**の側である。
- `nablarch-example-web`（サンプルアプリ）由来の変換出力 YAML には発現していない
  （`grep -rn 'FW_HEADER' src/test/java/nablarch/test/tool/converter/SampleConversionTest/` → ヒット 0）。
  対象PJの実データでの発現は未知である。
- 判断: **仕様として不適切**（スキーマの description が「予約値はない」と明言する値を実装が予約値として扱い、
  黙ってデータを落とす）。**帰属は yaml 側**である（description を実装に合わせるか、実装から
  `skipFwHeader` の特別扱いを外すかは yaml 側の判断）。本作業では修正しない（`src/main` 無変更）。

### 対象としない入力（辺②）

converter の入出力は **NTF が実行できるテストデータ**に限る。#19 の「対象としない入力」が Excel 側の
但し書き（`PoiXlsReader` の「全セルが文字列書式」）で線を引いたのに対し、YAML 側で線を引くのは
**本体スキーマ**（yaml jar 内 `nablarch/test/ntf-testdata-yaml-schema.json`）である。
`YamlLoader#load` はパース直後に `JSON_SCHEMA.validate(...)` を実行し、違反があれば
`YamlSchemaValidationException` を投げるため、**スキーマ違反の YAML は中間モデルへ到達しない**。

**この段落の適用範囲は `rows` の値だけである（2026-08-14・#24 のレビュー指摘による訂正）。**
当初は「引用符なしの `123` は整数へ解決されスキーマ違反」と無限定に書いていたが、それが真なのは
**`rows` の値**（`table_data` / `list_map_data` / `record_fragment`）についてだけであり、
スキーマの他のプロパティには当てはまらない（下の「`rows` 以外のプロパティ」を参照）。

`rows` の値に課される型は次のパスで `["string","null"]` に限られる。

| 定義 | 型を課すパス |
|---|---|
| `table_data` ／ `list_map_data` | `$defs.<定義>.properties.rows.items.additionalProperties.type` |
| `record_fragment` | `$defs.record_fragment.properties.rows.items.items.type` |

再現コマンド:

```sh
python3 -c "
import json,zipfile,os
z=zipfile.ZipFile(os.path.expanduser('~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT.jar'))
d=json.loads(z.read('nablarch/test/ntf-testdata-yaml-schema.json').decode('utf-8'))
for k in ['table_data','list_map_data','record_fragment']:
    print(k, json.dumps(d['\$defs'][k]['properties']['rows']['items'], ensure_ascii=False))
"
```

したがって次の**引用符なし**スカラー記法を **`rows` の値として書いた場合**は本書の対象外とする —
`true` / `false`（真偽値へ解決）、`123`（整数へ解決）、`1.50` / `.inf` / `.nan`（浮動小数へ解決）。
いずれも読み込みが例外で止まるため、黙って壊れることはない。**これらの例外の形はテストで固定しない**
（不正な入力にどこまで対応するかに線は引けないため。ユーザー確定・2026-08-14）。

**`rows` 以外のプロパティには当てはまらない。** スキーマは値の位置ごとに別の型を課す。
とくに `$defs.field_def.properties.length` は

```
anyOf: [ {type: integer, minimum: 0}, {type: string, pattern: "^([0-9]+|-)$"} ]
```

であり、description も「**integer 記法（10）も文字列記法（"10"）もどちらも有効**」と明記している。
すなわち `length: 10`（引用符なし整数）は**仕様内の入力**であり、中間モデルには文字列 `"10"` が入る
（`YamlSection#toStr` が `Object#toString()` で文字列化するため。担保:
`YamlFormatReaderRealFileTest#readsIntegerLengthNotationAsString`）。

再現コマンド:

```sh
python3 -c "
import json,zipfile,os
z=zipfile.ZipFile(os.path.expanduser('~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT.jar'))
d=json.loads(z.read('nablarch/test/ntf-testdata-yaml-schema.json').decode('utf-8'))
print(json.dumps(d['\$defs']['field_def']['properties']['length'], ensure_ascii=False, indent=1))
"
```

**引用符を付ければ同じ見た目の値はすべて仕様内である**（`"true"` / `"123"` / `"1.50"`）。
これらは `YamlFormatReaderScalarTest` が D2-03（`readsQuotedNumberAsString`）／
D2-04（`readsQuotedTrailingZeroDecimalAsString`）／D2-05（`readsQuotedTrueAsString`）として担保している。
スキーマ違反が例外になること自体は軸F の F2-01 で担保するが、**その入力には上記の記法を使わず**、
`type` の列挙違反（`YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFileTypeIsNotInEnum`）と
`length` のパターン違反（`#failsWithSchemaValidationExceptionWhenFieldLengthDoesNotMatchPattern`）を用いている。

### 課題としないと判断した観測結果（#24）

| 観測 | 判断 |
|---|---|
| 引用符なしの `TRUE` / `yes` が文字列になる | 妥当。スキーマが `rows` の値を `["string","null"]` に限る以上そのとおりの挙動であり、**真偽値を表現する手段自体が無い**。作成者が真偽値のつもりで書く余地が無いため YML-01 とは性質が違う（**テストで担保**: `readsUppercaseTrueAsString` ／ `readsYesAsString`） |
| 引用符なしの `True` / `on` も同様に文字列になる | 同上。ただし**テストは書いていない** — `JsonScalarResolver` の `BOOL` ＝ `^(?:true\|false)$`（YML-01 の表 4）に一致しないことから機械的に導ける事実であり、記法を 1 つ足すたびにテストを増やす価値が無いと判断した |
| 引用符なしの `true` がスキーマ違反で例外になる | 仕様外の入力（上記「対象としない入力」）。例外で止まるので黙って壊れない |
| 引用の別（`abc` ／ `"abc"` ／ `'abc'`）が中間モデルに残らない | 妥当（3 記法とも `"abc"`。YAML の記法差であって値の差ではない） |
| `\|`（リテラル）が `"l1\nl2\n"`、`>`（フォールド）が `"l1 l2\n"` になる（末尾に改行が付く） | 妥当（YAML のブロックスカラー仕様どおり。担保: `readsLiteralBlockScalarKeepingNewlines` ／ `readsFoldedBlockScalarFoldingNewlinesIntoSpaces`） |
| `007` / `0x1F` / 日付風 `2026-08-07` が記法どおりの文字列になる | 妥当（`JsonScalarResolver` の `INT` / `FLOAT` に一致せず、日付タグの解決も持たないため） |
| 空文字 `""` と値なし（Java `null`）が区別される | 妥当（Excel 経路が両者を区別できない XLS-04 とは対照的だが、YAML では区別できる）。**但し書き（2026-08-14・修正ラウンド 2 で追加）**: 区別されるのは**書かれた値**についてだけである。レコード断片で行の要素数が `fields` の件数に足りない場合、欠けた位置は `null` ではなく `""` で埋まり、「書かれた空文字」と見分けが付かなくなる（**YML-05**） |
| 未知のトップレベルキーが**実ファイル経路では例外**になる（in-memory 経路では無視される） | 妥当。スキーマのルートが `additionalProperties: false` であるため。`YamlFormatReader#addBlocksForSection` の「未知キーは無視」は**スキーマが許す範囲にしか効かない**。loud に失敗するので黙って壊れない（担保: `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenTopLevelKeyIsUnknown`） |
| 空ファイルが例外にならず、ブロック 0 件のコンテナになる | 妥当（`YamlLoader#load` が `loaded == null` のとき空 Map を返す。トップレベルに必須キーは無いためスキーマ上も適合する。担保: `#readsEmptyFileAsContainerWithoutBlocks`） |
| `directives` を書かなくても中間モデルに `file-type` が現れる | 既知（**XLS-07**）。同じ本体器（`DataFile`）を使うため辺①と同じ挙動になる（担保: `YamlFormatReaderRealFileTest#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInFile` ／ `#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInMessage`） |
| `record_type` を書かないと `RecordLayout.recordType` が `null` になる | 妥当（`RecordLayout` の Javadoc「省略時は null」どおり。辺①の実 `.xlsx` 経路が `""` になる **XLS-06** とは非対称だが、YAML 側は仕様どおりである。担保: `#readsEmptyRowsFromRecordLayoutWithoutRows`） |

### 到達不能と判定した軸要素（#24）

`inventory.md` §2.3 では「要追加」に分類されていたが、実 `.yaml` 経路では生成できないことが判明したもの。

**ただし表の最終行 C-15 だけは前提が違う（2026-08-14・#24 のレビュー指摘による訂正）。** C-15 は
`inventory.md` §2.3 の「#18 の状態」列に現れず、§2.1 の表 12 行目
（`readMessage_emptyBody_isStillMapped`）で **✅ とされていた**要素である。#24 で判明したのは
「その ✅ は in-memory 経路のものであり、実 `.yaml` 経路では到達できない」という点であって、
「要追加」から「到達不能」へ移したわけではない。該当行にも但し書きを付けた。

| 軸要素 | 根拠 | 根拠テスト |
|---|---|---|
| C-11 `FileDataBlock.directives` 空 ／ C-13 `MessageDataBlock.directives` 空 | **XLS-07** と同じ（本体 `DataFile` のコンストラクタが `file-type` を必ず注入する）。YAML で `directives` を 1 つも書かなくても空 Map にならない | `YamlFormatReaderRealFileTest#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInFile` ／ `#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInMessage` |
| C-17 `RecordLayout.fields` 空 | スキーマ `$defs.record_fragment.properties.fields.minItems` ＝ 1。`fields: []` はスキーマ違反となり中間モデルへ到達しない | `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFieldsIsEmpty` |
| C-20 `FieldDef.type` 省略（`null`） | スキーマ `$defs.field_def.required` が `type` を必須とする。型を書かないフィールド定義は中間モデルへ到達しない | `#failsWithSchemaValidationExceptionWhenFieldTypeIsMissing` |
| C-15 `MessageDataBlock.records` 空（**上の但し書きのとおり、この行だけ「要追加」からの移動ではない**） | スキーマ `$defs.message_data.properties.records.minItems` ＝ 1（送信系の `expected_request_message_data` も同じ）。**実ファイル経路では到達できない**。#18 が ✅ としているのは in-memory 経路（`YamlFormatReaderTest#readMessage_emptyBody_isStillMapped`）である | 実 `.yaml` で `records` 0 件のブロックが**別経路で**生じることは `YamlFormatReaderRealFileTest#dropsFwHeaderNamedRecordFromRealYaml` が示す（`record_type: FW_HEADER` のレコードだけを書くと器も原文も 0 件になる。`issues.md` **YML-03**）。ただしこれは仕様上の到達手段ではなく課題である |

**#23 の「未確認」への回答**: `issues.md` の「未確認（#23）」に
「**XLS-22 の到達経路（辺②の YAML で `fields: []` を書けるか）は未確認**」と残していた。
**書けない**（上表 C-17）。したがって XLS-22（フィールド 0 件のレコードレイアウトは書けるが読み戻せない）の
入力を辺②が作ることはない。辺③が書き出した `.xlsx` を辺①で読み戻す経路でのみ現れる。

### 未確認（#24）

- **`YamlLoader` の LRU キャッシュ（`YAML_CACHE`、最大 8 エントリ）が converter の実運用で
  どう効くかは未確認である。** テストでは `YamlLoader.clearCacheForTest()` をテストごとに呼んで
  影響を排除しており、同一パスのファイルを書き換えて 2 回読む経路は確かめていない。
- **NTF 実行時の `NullInterpreter` による解釈は未確認である。** スキーマの `table_data.rows` の
  description は「`null`（クォートなし）および `"null"`（クォートあり）はともに NullInterpreter により
  Java null に変換される」と書いているが、converter は `InterpreterResolver.raw()` で配線しているため
  中間モデルには `"null"` が文字列のまま入る（実測済み）。**NTF 本体の実行時に本当に両者が同じ扱いになるかは
  確かめていない**（確かめるには NTF の実行が要る）。

---

## #24 スキーマの自由度の掃引で記録した課題

**本節は 2026-08-14 の 2 巡目レビュー指摘（「軸の枠に沿って埋める作り方では拾えない壊れ方が残っている」）を受けて
実施した掃引の結果である。** 掃引の手順と、列挙したスキーマ上の自由度の一覧は
`inventory.md` §2.1-2 の「開示」に載せた（どこまで見たか・見ていない範囲もそこに書いてある）。
**掃引はその後 3 巡目レビュー指摘を受けて項目 24〜26 まで広げ、そこで YML-09 を見つけた**（同じ掃引の続きであるため
節を分けずに本節へ入れている）。

**掲載順**: 「凡例 → 並び順の原則」に従い、**検出できない**もの（YML-04・YML-05・YML-06・YML-08・YML-09）を先に置き、
loud に失敗するもの（YML-07）を最後に置く。課題 ID は発見順のまま振り直していない
（YML-09 は最後に見つかったが検出できない側であるため YML-07 より前に来る）。
既出の YML-01（**変換時には**検出できない）・YML-02・YML-03（検出できない）もすべて検出できない側であるため、
本節を後ろに置くことは並び順の原則に反しない（ID 昇順と発見順が一致しているだけである）。

以下はすべて `YamlFixture` が書き出した実 `.yaml` を `new YamlFormatReader().read(...)` に渡して実測した。

### YML-04 テーブル／LIST_MAP のカラムは先頭行のキー集合だけで決まり、後続行にしかないカラムが黙って消える（影響度 高・**検出できない**）

| 入力（`rows`） | 中間モデルへ入る結果 | 担保テスト（`YamlFormatReaderInvalidInputTest#`） |
|---|---|---|
| `setup_tables`: `[{A: "1"}, {A: "2", B: "x"}]` | `columnNames=[A]`、`rows=[[1], [2]]`。**`B: "x"` が消える** | `dropsColumnThatAppearsOnlyInSecondRowOfTable` |
| `list_maps`: 同上 | 同上（経路差なし） | `dropsColumnThatAppearsOnlyInSecondRowOfListMap` |
| `setup_tables`: `[{A: "1", B: "x"}, {A: "2"}]`（逆向き＝後続行でキーが欠ける） | `columnNames=[A, B]`、`rows=[[1, x], [2, null]]`。欠けた側は `null` で救われる | `padsColumnMissingFromSecondRowWithNullInTable` |
| `setup_tables`: `[{}, {A: "1"}]`（先頭行が空マッピング） | `columnNames=[]`、`rows=[]`。**2 行目に書いたデータごと消える**（行数まで変わる） | `dropsAllRowsWhenFirstRowOfTableIsEmptyObject` |
| `list_maps`: `[{}, {A: "1"}]` | `columnNames=[]`、`rows=[[], []]`。行数は残るが値がすべて消える | `keepsRowCountButLosesValuesWhenFirstRowOfListMapIsEmptyObject` |

- **この入力はスキーマ上の仕様内である。** `$defs.table_data.properties.rows.items` は
  `{"type": "object", "additionalProperties": {"type": ["string", "null"]}}` であり、
  **キー集合に制約が無い**（`list_map_data` も同じ）。行ごとにキーが違ってよく、空マッピング `{}` も適合する。
  スキーマの description にも「全行で同じキーを書くこと」という条件は無い。

  再現コマンド:

  ```sh
  python3 -c "
  import json,zipfile,os
  z=zipfile.ZipFile(os.path.expanduser('~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT.jar'))
  d=json.loads(z.read('nablarch/test/ntf-testdata-yaml-schema.json').decode('utf-8'))['\$defs']
  for k in ['table_data','list_map_data']:
      print(k, json.dumps(d[k]['properties']['rows']['items'], ensure_ascii=False))
  "
  ```

- 原因: **帰属は yaml 側と converter の両方である。**
  - yaml 側: `nablarch/test/core/reader/yaml/YamlSection.java` の `resolveColumns` が
    `new ArrayList<String>(castMap(rows.get(0)).keySet())`、すなわち**先頭行のキー集合だけ**を返す。
    テーブル経路（`YamlTableDataBuilder`）・LIST_MAP 経路ともこれを使う。
  - converter: `YamlFormatReader#nonMarkerColumns` が `YamlSection.resolveColumns(...)` の結果を
    そのまま（マーカーを除いただけで）カラム順に使う。器が返す行 Map には 2 行目以降のキーも入っているが、
    converter は列挙したカラムぶんしか取り出さない。

  再現コマンド:

  ```sh
  cd "$(mktemp -d)" \
    && unzip -oq ~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT-sources.jar \
    && grep -n 'rows.get(0)' nablarch/test/core/reader/yaml/YamlSection.java
  ```

- 影響: 手書きの YAML で「その行にだけ意味のあるカラム」を後の行に足す書き方（NULL 許容カラムを 1 行だけ書く等）は
  自然に起こり得る。それが変換後の成果物から**警告なしに消える**。先頭行を `{}` にした場合はテーブル経路で
  ブロックの中身が丸ごと消える。
- **XLS-08 との関係**: マーカーカラム（`[no]`）だけの行は同じく「カラム 0 件・値を持たない行」になる
  （担保テスト `readsMarkerOnlyTableAsColumnlessRows`）。これは辺①の **XLS-08** と同型の現れ方であり、
  マーカー除外そのものは意図した仕様（steering #15）である。本課題は「先頭行だけでカラムが決まる」ことのほうを指す。
- 判断: **仕様として不適切**（データ損失。少なくとも「先頭行に無いキーを持つ行がある」ことを検知して WARN すべき。
  あるべき姿は全行のキーの和集合をカラムにすることだが、それは NTF 実行時の解釈も変えるため yaml 側の判断が要る）。
  修正はこの作業では行わない（`src/main` 無変更）。

### YML-05 レコード断片で行の要素数がフィールド数と食い違っても例外にならず、余りは捨てられ不足は空文字で埋まる（影響度 中・**検出できない**）

| 入力 | 中間モデルへ入る結果 | 担保テスト（`YamlFormatReaderInvalidInputTest#`） |
|---|---|---|
| `fields=[f1]` に `rows: [["a", "b", "c"]]` | `rows=[[a]]`。**2 個目以降が消える** | `dropsRecordFragmentValuesBeyondFieldCount` |
| `fields=[f1,f2,f3]` に `rows: [["a"]]` | `rows=[[a, "", ""]]`。**Java `null` ではなく空文字**で埋まる | `fillsMissingRecordFragmentValuesWithEmptyStringInsteadOfNull` |
| 同上に `rows: [["a", null]]` | `rows=[[a, null, ""]]`。**明示的に書いた `null` は `null` のまま**残り、欠損だけが空文字になる | 同上 |

- **この入力はスキーマ上の仕様内である**（構造としては通る）。`$defs.record_fragment.properties.rows.items` は
  要素数を `fields` の件数と紐づけておらず、**JSON Schema では表現できない制約**を description が言葉で書いている:
  「各配列の要素数が fields の件数と一致しない場合は **NTF がエラーを出す**」。
  **その約束は変換時には果たされない**（変換は例外にならず黙って通る。NTF 実行時にエラーになるかは未確認）。

  再現コマンド:

  ```sh
  python3 -c "
  import json,zipfile,os
  z=zipfile.ZipFile(os.path.expanduser('~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT.jar'))
  d=json.loads(z.read('nablarch/test/ntf-testdata-yaml-schema.json').decode('utf-8'))['\$defs']
  print(d['record_fragment']['properties']['rows']['description'])
  "
  ```

- 原因: **帰属は nablarch-testing 側**である。`nablarch/test/core/file/DataFileFragment.java` の `addValue` が
  `String value = i < line.size() ? line.get(i) : "";` として、フィールド名の件数ぶんだけ値を取り出し、
  足りない位置を空文字で埋める。converter（`YamlFormatReader#toRecordLayouts`）は器が持つ値 Map を
  断片のフィールド名順に並べ直すだけで、原文の要素数を見ない。

  再現コマンド:

  ```sh
  cd "$(mktemp -d)" \
    && unzip -oq ~/.m2/repository/com/nablarch/framework/nablarch-testing/6-NEXT-SNAPSHOT/nablarch-testing-6-NEXT-SNAPSHOT-sources.jar \
    && grep -n 'line.size()' nablarch/test/core/file/DataFileFragment.java
  ```

- **本課題は #24 の中心成果と直接ぶつかる。** 軸D では「空文字と Java `null` は区別される」ことを固定し、
  下の「課題としないと判断した観測結果（#24）」にも同じことを書いた。**それが成り立つのは
  「書かれた値」についてだけである。** 要素数が足りない行では、書かれていない位置が `""` になり、
  「作成者が空文字を書いた」のか「書き忘れた（NULL のつもりだった）」のかが中間モデル上で区別できない。
  該当行に但し書きを付けた（下表）。
- 影響: 余り側は値が消え、不足側は書いていない空文字が作られる。どちらも例外にも警告にもならないため、
  入力と出力を突き合わせない限り気づけない。
- 判断: **仕様として不適切**（余りの drop は少なくとも WARN が要る。不足の充填は
  スキーマ description が「NTF がエラーを出す」と書いている以上、変換時にも検知できるはずである）。
  修正はこの作業では行わない（`src/main` 無変更）。

### YML-06 `id` が重複したエントリは 2 件目以降も 1 件目のデータでブロックが作られる（影響度 中・**検出できない**）

| 入力 | 中間モデルへ入る結果 | 担保テスト（`YamlFormatReaderInvalidInputTest#`） |
|---|---|---|
| `list_maps` に `id: "lm"` を 2 件（1 件目 `{A: "first"}`／2 件目 `{A: "second"}`） | ブロックは 2 件。**どちらも `rows=[[first]]`**。`"second"` は中間モデルに現れない | `reusesFirstEntryRowsForDuplicateListMapId` |
| `messages` に `id: "RM01"` を 2 件（1 件目 `m1`／`"a"`、2 件目 `m2`／`"b"`） | ブロックは 2 件。2 件目は**フィールド定義だけが自分のもの**（`m2`）で、**データ行は 1 件目の本文**（`"a"`） | `reusesFirstEntryBodyForDuplicateMessageId` |

- **この入力はスキーマ上の仕様内である。** `id` に一意制約は無く、description が重複を明示的に扱っている —
  `$defs.list_map_data.description`「id が重複した場合は最初の1件のみ有効（2件目以降は無視）」、
  `$defs.message_data.description`「id で完全一致検索され先着1件のみ有効」。

  再現コマンド:

  ```sh
  python3 -c "
  import json,zipfile,os
  z=zipfile.ZipFile(os.path.expanduser('~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT.jar'))
  d=json.loads(z.read('nablarch/test/ntf-testdata-yaml-schema.json').decode('utf-8'))['\$defs']
  print(d['list_map_data']['description'])
  print(d['message_data']['description'])
  "
  ```

- 原因: **帰属は converter 側**である。器は description のとおり先着 1 件を返す
  （`YamlTableDataBuilder#buildListMapRows` と `YamlMessageBuilder#buildMessageContent` がいずれも
  `id.equals(toStr(map.get(FIELD_ID)))` で最初に一致したエントリを返す）。
  一方 `YamlFormatReader#addListMapBlocks` ／ `#addMessageBlocks` は**YAML エントリごとに 1 ブロック**を作り、
  行値だけを `id` で引き直す。結果として「N 件目のカラム／フィールド定義 × 1 件目の値」という
  原文のどこにも存在しない組み合わせが中間モデルに入る。

  再現コマンド:

  ```sh
  cd "$(mktemp -d)" \
    && unzip -oq ~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT-sources.jar \
    && grep -n 'id.equals(toStr(map.get(FIELD_ID)))' \
         nablarch/test/core/reader/yaml/YamlTableDataBuilder.java \
         nablarch/test/core/reader/yaml/YamlMessageBuilder.java
  ```

- 影響: NTF 実行時に 2 件目以降は無視されるため**実行時の挙動は変わらない**が、変換後の成果物には
  値が入れ替わったブロックが残る。入力と出力を突き合わせない限り気づけない。
  カラム名が 1 件目と違う場合は 2 件目の値が `null` になることもプローブで確認した
  （`[{id: lm, rows: [{A: first}]}, {id: lm, rows: [{B: second}]}]` → 2 件目は `rows=[[null]]`）。
  この変種は**テストとしては固定していない**。
- 判断: **仕様として不適切**（原文に存在しない組み合わせを作る。器の「先着 1 件」に合わせるなら
  2 件目以降はブロックを作らない、もしくは原文どおりの値を使うべきである）。
  **修正するとしたら本リポジトリ内で完結する**（原因コードが `src/main` にあるため）。
  修正はこの作業では行わない（`src/main` 無変更）。

### YML-08 ディレクティブ値が `trim()` されるため、スキーマ description が推奨する記法が壊れる（影響度 中・**(a) は検出できない**／(b) は loud）

| # | 入力（`directives`） | 中間モデルへ入る結果 | 担保テスト（`YamlFormatReaderInvalidInputTest#`） |
|---|---|---|---|
| (a) | `record-separator: "\r\n"`（description が推奨するリテラル記法。YAML は実際の CR LF に解決する） | **空文字**（値が消える）。例外にならない | `losesRecordSeparatorWrittenAsLiteralNewline` |
| (b) | `field-separator: "\t"`（description が「タブ文字に変換される」と書く記法。YAML は実際のタブに解決する） | `IllegalArgumentException: field-separator must be one character.but was `（末尾は空。trim 後の値） | `failsWhenFieldSeparatorIsWrittenAsActualTab` |
| (参考) | `record-separator: "CRLF"`（シンボル記法） | **シンボルではなく実際の改行 `"\r\n"`** が入る（辺①は逆正規化してシンボルへ戻すため非対称） | `readsRecordSeparatorSymbolAsActualNewline` |

- **いずれもスキーマ上の仕様内である。** `$defs.directives.properties.record-separator.description` は
  「`"CRLF"` / `"LF"` / `"CR"` / `"NONE"` のシンボル指定、または任意のリテラル文字列が有効。
  YAML でリテラル指定する場合はダブルクォート文字列内でエスケープシーケンスを使う（例: `"\r\n"` = CRLF、`"\n"` = LF）」、
  `field-separator.description` は「YAML では `"\t"` と記述するとタブ文字（U+0009）に変換される」と書いている。
  **どちらの記法も実際には通らない。**

  再現コマンド:

  ```sh
  python3 -c "
  import json,zipfile,os
  z=zipfile.ZipFile(os.path.expanduser('~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT.jar'))
  d=json.loads(z.read('nablarch/test/ntf-testdata-yaml-schema.json').decode('utf-8'))['\$defs']['directives']['properties']
  print(d['record-separator']['description'])
  print(d['field-separator']['description'])
  "
  ```

- 原因: `nablarch/test/core/file/DataFile.java` の `setDirective` が
  `convertDirectiveValue(directive, stringValue.trim())` として値を **trim してから**変換する。
  制御文字（CR・LF・タブ）だけの値は trim で空になり、(a) では空文字として保存され、
  (b) では「1 文字でなければならない」という `VariableLengthFile#convertDirectiveValue` の検査に引っ掛かる。
  タブ指定が通るのは**バックスラッシュと `t` の 2 文字**を渡した場合であり（`TAB_EXPRESSION` ＝ `"\\t"`）、
  YAML ではシングルクォート記法 `'\t'` がそれに当たる（プローブで確認。テストとしては固定していない）。

  再現コマンド:

  ```sh
  cd "$(mktemp -d)" \
    && unzip -oq ~/.m2/repository/com/nablarch/framework/nablarch-testing/6-NEXT-SNAPSHOT/nablarch-testing-6-NEXT-SNAPSHOT-sources.jar \
    && grep -n 'stringValue.trim()' nablarch/test/core/file/DataFile.java \
    && grep -n 'TAB_EXPRESSION' nablarch/test/core/file/VariableLengthFile.java
  ```

- **帰属は 3 者に分かれる。**
  - trim による損失そのもの: **nablarch-testing 側**（`DataFile#setDirective`）。
  - 「その記法で書ける」と書いている description: **yaml 側**（スキーマ）。
  - シンボル記法が中間モデルで実文字に変わること: **converter 側**。辺①は
    `XlsFormatReader#normalizeDirectiveValue` が実改行・実タブをシンボル（`CRLF` / `\t`）へ逆正規化しており、
    その Javadoc も「そのまま toString() すると本体 setDirective の trim() で失われる」と**この trim を認識している**。
    辺②（`YamlFormatReader#toStringDirectives`）は素通しを選んでいるため、同じ入力表記が辺①と辺②で
    別の中間モデル値になる。
- 影響: (a) はレコード区切りが黙って空になる。(b) は loud に失敗するので気づける。
  参考行（シンボル → 実文字）は値の意味こそ変わらないが、**中間モデルの値が原文と一致しない**。
- **未確認**: 中間モデルに入った実改行を辺③（Excel）／辺④（YAML）へ書き出したときに何が起こるか、
  すなわち往復が安定するかは**確かめていない**。辺②の観測だけからは、辺④が実改行をそのまま書けば
  読み戻しで (a) の経路に入る（＝空文字になる）ことが予想されるが、**実行して確かめていない**。
  #25 以降で確認すること。
- 判断: **仕様として不適切**。修正はこの作業では行わない（`src/main` 無変更）。

### YML-09 同じ `group_id` のエントリが離れて書かれていると、ブロックがグループの初出順にまとめ直され原文の記述順と食い違う（影響度 中・**検出できない**）

| 入力（1 ファイルに 3 セクション。`group_id` を `g1` → `g2` → `g1` と交互に書く） | 中間モデルへ入る結果 | 担保テスト |
|---|---|---|
| `setup_tables`: `T1`(g1) / `T2`(g2) / `T3`(g1) | ブロックは `T1`, **`T3`**, `T2` の順（原文は `T1`, `T2`, `T3`） | `YamlFormatReaderRealFileTest#reordersBlocksByFirstAppearanceOfGroupIdFromRealYaml` |
| `setup_files`: `a.dat`(g1) / `b.dat`(g2) / `c.dat`(g1) | ブロックは `a.dat`, **`c.dat`**, `b.dat` の順 | 同上 |
| `response_body_messages`: `M1`(g1) / `M2`(g2) / `M3`(g1) | ブロックは `M1`, **`M3`**, `M2` の順 | 同上 |

**値そのものは失われない**（`T3` の行は `[["3"]]`、`T2` の行は `[["2"]]` のまま。入れ替わるのは並びだけ）。
例外にも警告にもならない。

- **この入力はスキーマ上の仕様内である。** セクション配列（`setup_tables` ／ `setup_files` ／
  `response_body_messages`）はいずれも `{"type": "array", "items": {"$ref": ...}}` だけで、
  同じ `group_id` のエントリが配列内で**連続することを要求するキーワードを持たない**。
  それどころか description は「**同一 group_id を持つ複数エントリはすべて収集される**」と書いており、
  離れて書かれることを前提にしている。

  再現コマンド:

  ```sh
  python3 -c "
  import json,zipfile,os
  z=zipfile.ZipFile(os.path.expanduser('~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT.jar'))
  s=json.loads(z.read('nablarch/test/ntf-testdata-yaml-schema.json').decode('utf-8'))
  for k in ['setup_tables','setup_files','response_body_messages']:
      p=s['properties'][k]
      print(k, 'keys=', sorted(p.keys()), '| items=', json.dumps(p['items'], ensure_ascii=False))
  print(s['properties']['setup_tables']['description'].splitlines()[0])
  "
  ```

  出力の 3 行はいずれも `keys= ['description', 'items', 'type']` であり、順序に関するキーワードは無い。

- 原因: **帰属は converter 側である。** `YamlFormatReader` はセクションを**グループ単位で**走査する。
  テーブル系・ファイル系は `#formattedGroupsInOrder`、送信系は `#rawGroupsInOrder` が
  グループ ID を**初出順で重複排除**して返し、`#addTableBlocks` ／ `#addFileBlocks` ／
  `#addSendSyncBlocks` がそのグループごとにエントリを集めてブロックを作る。
  結果として、原文でグループが交互に現れても、ブロックは「グループの初出順 × グループ内の記述順」に並ぶ。

  再現コマンド:

  ```sh
  grep -n 'groups.contains(group)' src/main/java/nablarch/test/tool/converter/yaml/YamlFormatReader.java
  ```

  出力は 2 行（`formattedGroupsInOrder` と `rawGroupsInOrder` の重複排除）。

- 影響: 辺③④はこの並びのまま書き出すため、**変換後の成果物ではエントリの並びが原文と入れ替わる**。
  NTF は `group_id` で収集するため実行結果は変わらず、変換後にテストを流しても通ってしまう。
  入力と出力を目で突き合わせない限り気づけない。
- **#15 との関係**: 本リポジトリは並びの保持を変換の正しさとして扱ってきた
  （steering #15「LIST_MAP 列順保持修正」は列順がアルファベット順になることを不具合として直した）。
  同じ基準を当てれば、エントリの並びが変わることも「変換は忠実」として片付けられない。
- 判断: **仕様として不適切**（あるべき姿は原文の記述順を保つことである。グループ単位の走査は
  器へ渡す単位の都合であって、出力の並びを決める理由にはならない）。
  修正はこの作業では行わない（`src/main` 無変更）。

### YML-07 長さ省略記法 `"-"` は `text-encoding` を書かないと手掛かりの無い `NullPointerException` になる（影響度 低・loud に失敗するため検出できる）

| 入力 | 結果 | 担保テスト（`YamlFormatReaderInvalidInputTest#`） |
|---|---|---|
| 固定長ファイルの `length: "-"`（`directives` を書かない） | `NullPointerException`（**メッセージ `null`**。どのファイルのどのフィールドかを示す手掛かりが無い） | `failsWithNullPointerExceptionWhenOndemandLengthIsUsedWithoutTextEncoding` |
| 同じ入力に `text-encoding: "UTF-8"` を足したもの | 例外にならず、`FieldDef.length` に原文 `"-"` が入る | `readsOndemandLengthNotationWhenTextEncodingIsSpecified` |

- **この入力はスキーマ上の仕様内である。** `$defs.field_def.properties.length` のパターンは
  `^([0-9]+|-)$` で `"-"` を許し、description も「`"-"` はオンデマンド計算（そのフィールドに追加された
  全レコード値の最大バイト長に自動拡張される）」と意味を定めている。`text-encoding` は必須ではない。
- 原因: `nablarch/test/core/file/DataFileFragment.java` の `replaceFieldSize` が
  `data.getBytes(container.getEncodingFromDirectives())` を呼ぶ。`text-encoding` ディレクティブが無いと
  この値は `null` のままで、`String#getBytes(Charset)` が NPE を投げる。**帰属は nablarch-testing 側**である。
  可変長ファイル・メッセージ経路でも同じ場所で落ちることを実測した（テストは固定長 1 件のみ）。

  再現コマンド:

  ```sh
  cd "$(mktemp -d)" \
    && unzip -oq ~/.m2/repository/com/nablarch/framework/nablarch-testing/6-NEXT-SNAPSHOT/nablarch-testing-6-NEXT-SNAPSHOT-sources.jar \
    && grep -n 'getEncodingFromDirectives()' nablarch/test/core/file/DataFileFragment.java
  ```

- 辺①（Excel）で同じ記法を通している `XlsFormatReaderRealFileTest#readsOmittedFieldLengthNotationFromRealBook` は
  **入力に `text-encoding` 行を持っている**ため、この経路には入らない。すなわち辺①固有／辺②固有の話ではなく、
  `text-encoding` の有無で決まる。
- 影響: 変換が失敗するので気づけるが、**どのファイルのどのフィールドが原因かが分からない**（XLS-14 と同じ性質）。
- 判断: 変換ツール側で読み取り例外にリソース名を添えて包み直すのがあるべき姿である（XLS-14 と同じ結論）。
  本作業では修正しない（`src/main` 無変更）。
