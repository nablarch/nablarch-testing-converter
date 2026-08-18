# 現状挙動の課題一覧

フェーズ2（タスク #19〜#26）で「実行して記録した現状の挙動」のうち、仕様として妥当でないと判断したもの、
または挙動を固定できなかったものを記録する。

**#19〜#25 の間は、記録した課題を修正しなかった**（steering Rules フェーズ2。この間の `src/main` への
変更はゼロである）。**#25.5 で方針が変わり、「NTF 仕様としての判定」が要対応とした 7 件のうち 6 件を修正した**
（steering Decisions「不具合修正の対象と手順（#25.5）」）。修正済みの課題は各項の
「NTF 仕様としての判定」欄にコミットを添えてある。それ以外は従来どおり記録のみである。

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

### 2 つの判定欄（2026-08-14・#25.5 で追加）

各課題には判定欄が 2 つある。**問うていることが違う。**

| 欄 | 何を問うか | 取りうる値 |
|---|---|---|
| 判断 | **往復基準**。変換結果が入力と一致するか、あるべき姿は何か。原因が converter の外にあっても「仕様として不適切」と書く | 仕様として不適切／受容できる（記録のみ）／記録に留める など |
| NTF 仕様としての判定 | **NTF の記法の明文に反するか**（＝ #25.5 で直す対象か）。解説書 `testdata_notation.rst`・本体スキーマ `ntf-testdata-yaml-schema.json`・中間モデルの Javadoc 契約に**明文の根拠があり**、現在の挙動がそれに反するかだけを見る | 要対応／対応不要／本作業の対象外（帰属が converter の外にある記録） |

**2 つは食い違うことがある。** 「判断: 仕様として不適切」でも、記法に明文が無ければ「対応不要」になる
（改善提案ではあるが記法違反ではない、という意味）。**「対応不要」は「問題ない」という意味ではない。**
食い違う 13 件（XLS-05・XLS-10・XLS-12・XLS-14・XLS-20・XLS-21・YML-01・YML-04・YML-05・YML-06・YML-07・
YML-09・YML-10）では、判定欄の中でその旨を明示した。**「判断」欄は 1 件も取り消していないので、両方を読むこと。**

課題は全部で **37 件**（課題 ID 単位）。内訳は **要対応 7 件**（XLS-06・XLS-16・XLS-22・YML-02・YML-03・
YML-08・YML-12）／**対応不要 29 件**／**本作業の対象外 1 件**（XLS-26）である。

要対応のうち **7 件すべてが修正済み**である（XLS-06・XLS-16・XLS-22・YML-02・YML-03・YML-08・YML-12。
**YML-12 は 4 形すべてが修正済みになった**）。**YML-03 は帰属が nablarch-testing-yaml 側だったため
`@Ignore("YML-03: yaml側の修正待ち")` の待機テストを置いて待っていたが、yaml 側が `0b53910` で修正されたため
2026-08-18 に両側そろって解消した**（converter 側は `YamlFormatReader#recordsWithoutFwHeader` の廃止。
`@Ignore` は残っていない）。詳細は各課題の判定欄と steering Decisions
「不具合修正の対象と手順（#25.5）」を参照。

> **XLS-22 を対応不要から要対応へ変えた（2026-08-18・ユーザー判断）。** 判定の根拠を「到達可能性」から
> 「両形式が表現できない値を中間モデルだけが保持できる＝中間モデルの契約の穴」へ一本化したことによる。
> あわせて YML-12 の 3 形目（同じ中間モデル値の辺④版）も要対応へ移った。
> **これに伴い 要対応 6 ／ 対応不要 30 → 要対応 7 ／ 対応不要 29 へ動いた。** 他の 35 件の判定は変えていない。

**集計は課題 ID 単位で数えること。** 判定欄の本文は複数行にわたり、地の文にも「要対応」「対応不要」の語が
現れるため、素の `grep -c` は判定欄以外の行を拾いうる。ID 単位の集計は次のコマンドで導ける
（`###` 見出しから ID を拾い、その配下の判定欄 1 行だけを数える）。

```
$ grep -c '^### \(XLS\|YML\)-' .rn/ntf-test-data-converter/coverage/issues.md
37
$ awk '/^### (XLS|YML)-/{id=$2} /^- NTF 仕様としての判定/{print id" "$0}' \
      .rn/ntf-test-data-converter/coverage/issues.md | grep -c '\*\*要対応\*\*'
7
$ awk '/^### (XLS|YML)-/{id=$2} /^- NTF 仕様としての判定/{print id" "$0}' \
      .rn/ntf-test-data-converter/coverage/issues.md | grep -c '\*\*対応不要\*\*'
29
```

> **上の 2 つ目のコマンドは現在 `6` を返す（2026-08-18 実測）。** YML-03 の判定欄だけが
> `**要対応 → 修正済み（2026-08-18）**` という書き方になっており、`**要対応**` の完全一致に
> 引っかからないためである。**要対応が 7 件であること自体は変わっていない**（XLS-06・XLS-16・
> XLS-22・YML-02・YML-03・YML-08・YML-12）。書式をそろえるか集計コマンドを直すかは
> 最終集計の際に決める。

出典に付す `notation:nnn` は
`~/work/nablarch/nablarch-document/ja/development_tools/testing_framework/implementation/testdata_notation.rst`
の行番号である。**基準は `nablarch-document` の `30a8271`（2026-08-18 08:54:15 +0900）であり、本書の
`notation:nnn` は全件この 1 つの基準にそろえてある。**（2026-08-18 に旧基準からの貼り直しを完了した。
旧基準の行番号は併記しない。）読むときは次のコマンドで開くこと（`N` は行番号）。

```
$ git -C ~/work/nablarch/nablarch-document show \
      30a8271:ja/development_tools/testing_framework/implementation/testdata_notation.rst | sed -n 'Np'
```

> **`nablarch-document` の HEAD は基準より先へ進んでいる（2026-08-18 実測）。** 同リポジトリ
> `ntf-yaml-support` ブランチの HEAD は `870e809` であり、
> `git diff --stat 30a8271 870e809 -- …/testdata_notation.rst` は `76 insertions(+), 76 deletions(-)` を返す
> （総行数は 1554 行のまま変わらないが本文が動いている）。**本書の行番号は HEAD ではなく `30a8271` で読むこと。**

`tsrc L…` は本体 `nablarch-testing` の sources jar を展開した原文の行番号である。

**「判断」欄の書式は全件そろっている。** #25.5 のレビュー 1 巡目で XLS-24 だけが
`- **判断**: `（太字）だったのを `- 判断: ` へ揃えた。

```
$ grep -c '^- 判断: ' .rn/ntf-test-data-converter/coverage/issues.md
37
$ grep -c '^- NTF 仕様としての判定' .rn/ntf-test-data-converter/coverage/issues.md
37
```

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
- NTF 仕様としての判定: **対応不要**（記法が明文で定めている挙動）。`notation:1535`「全要素が null または
  空文字のエントリは読み飛ばされる。Excel では行の全セルが空の場合、YAML では `rows:` 内の要素が空マッピング
  （`{}`）またはすべての値が空文字の場合にスキップされる」。読み飛ばしを実行するのも本体
  `PoiXlsReader#isBlankLine`（L140-147）であり、converter に判断の余地は無い。
  **上の「判断」（仕様として不適切）と食い違う。** 往復基準では確かに行が減るが、この欄が問うのは
  記法の明文に反するかであり、明文が読み飛ばしを定めている以上 converter の不具合ではない。
  WARN を出すべきという主張は記法への改善提案として残る（「判断」欄は取り消していない）。

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

| 入力セル | 中間モデルへ入る値 | テスト |
|---|---|---|
| 数値 `1`・**表示形式 `@`（文字列書式）** | `"1.0"` | `XlsFormatReaderCellTypeTest#readsTextFormattedNumericCellAsDoubleString`（**#25.5 以降は担保ではなく実挙動の記録**。下の判定を参照） |

- 原因: `PoiXlsReader#readOneLine` の `cell.toString()` が、数値セルに対して
  POI の `XSSFCell#toString()` → `getNumericCellValue() + ""` を返す。表示形式は参照されない。
- 表示形式が `@`（文字列書式）であっても、セル種別が数値であれば値が変わる。
  上の但し書きは書式についての条件であり、この形は但し書きを満たしたうえで値が変わる。
- 判断: **仕様として不適切**。少なくとも変換時に「**セル種別が文字列でないセル**がある」ことを検知して
  WARN を出すべき。検知条件は書式ではなくセル種別である — 本課題のセルは `numFmtId=49` ＝ `@` ＝
  **文字列書式であり**、「文字列書式でないセル」という条件では捕まえられない。
  修正はこの作業では行わない。
- NTF 仕様としての判定: **対応不要**（仕様外入力）。`notation:75`「Excelのセルの書式は、必ず文字列書式に
  統一しておく必要がある」と直後の `important`（`notation:79`）「Excelファイルに文字列以外の書式でデータを
  記述すると、Excelがセルの値を自動的に変換してしまう（…）ため、正しくデータを読み取れなくなる」により、
  数値書式のセルは記法の外にあり、解説書自身が「正しく読み取れない」と明言している。
  ユーザー確定（steering Decisions「不具合修正の対象と手順（#25.5）」）。担保テスト
  `XlsFormatReaderCellTypeTest#readsTextFormattedNumericCellAsDoubleString` は削除せず、
  アサートを「仕様外入力のため値は保証しない。これは要件ではなく実挙動の記録である」へ書き直した（2f21bce）。

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
- NTF 仕様としての判定: **対応不要**（記法に明文が無い）。記法が定める値の書き方は
  `notation:767-768`「null（Java の null）＝ セルに `null`（大文字小文字不問）」と
  `notation:769-770`「空文字 ＝ セルを空にする」の 2 つだけであり、**セルの不在・空白セル・空文字セルを
  区別する明文は無い**。3 者が同じ `""` になることは記法と矛盾しない。上の「判断」（実挙動として妥当）とも一致する。

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
- NTF 仕様としての判定: **対応不要**。XLS-05 と同じ `notation:1535`（全セルが空の行はスキップ）の派生であり、
  記法が明文で定めている読み飛ばしである。マーカー列がフィールドとして読まれないことも
  `notation:1269`「メッセージボディの各行の先頭要素は、フィールドとしては読み込まれないラベル列である」のとおり。

### XLS-06 レコード種別の省略が実 `.xlsx` 経路では `null` にならず空文字になる（影響度 中・**#25.5 で修正済み**）

| 入力 | 中間モデルへ入る値（#25.5 前） | 中間モデルへ入る値（#25.5 後） | 担保テスト |
|---|---|---|---|
| `SETUP_FIXED=f.dat` の名前行 先頭セル（レコード種別）が空白セル | `RecordLayout.recordType` ＝ `""` | `RecordLayout.recordType` ＝ **`null`** | `XlsFormatReaderRealFileTest#readsOmittedRecordTypeAsNullFromRealBook`（旧名 `readsOmittedRecordTypeAsEmptyStringFromRealBook`） |

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
- 判断: **仕様として不適切**（省略の表現が経路間で非対称）。**この「判断」は #19〜#25 の時点のもので、
  当時は修正しない前提だった。#25.5 で方針が変わり、下の判定のとおり修正済みである。**
- NTF 仕様としての判定: **要対応**。中間モデルの契約 `RecordLayout.java:26`「レコード種別（省略時は `null`）」に
  反する（辺①だけが `""` を入れ、辺②は `null` を入れる）。**#25.5 で修正済み（5721ecd）**。
  `XlsFormatReader` が空のレコード種別セルを `null` として中間モデルへ入れるようにし、辺②と揃えた。
- 帰結: 辺①では軸C の C-16「省略（`null`）」は #25.5 まで**到達不能**だった。
  修正後は到達可能になり、`inventory.md` §1.2-2／§1.3 の C-16 を「担保済み」へ移した。
  上の変換後 YAML と `record_type: ""` の記述は **#25.5 前**の実測である。修正後は
  `YamlFormatWriter` L275 の `if (record.getRecordType() != null)` により `record_type` キー自体が出ない
  （辺②由来の省略と同じ形になる）。**この 1 点はソースを読んで判断したものであり、変換後 YAML を
  取り直した実測ではない**（`readsOmittedRecordTypeAsNullFromRealBook` が固定しているのは辺①の読み取り結果まで）。

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
- NTF 仕様としての判定: **対応不要**（記法どおりの既定値）。`notation:950`「フィールド区切り文字。
  デフォルトは `","`」のとおり、ディレクティブには記法上の既定値がある。器が既定値を明示した状態で
  中間モデルへ渡すことは、記法の明文に反しない（実行時の解釈も変わらない）。上の「判断」（受容できる）と一致する。
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
- NTF 仕様としての判定: **対応不要**（挙動の課題ではない）。誤っているのは `src/main` のコメントの根拠づけだけで、
  記法の明文に反する挙動は無い（ガード自体は防御的で害が無く、実 Excel 経路では到達しない）。
  #25.5 は挙動の不具合修正が対象であるため、コメント文言の訂正は含めない。

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
- NTF 仕様としての判定: **対応不要**（記法に明文が無い）。`notation:198`「データブロック先頭セルに
  `データタイプ=識別子の値` の形式で記載する。データタイプ名で始まっていれば合致する」のとおり、
  データタイプ名に合致しない先頭セルはそもそもデータブロックではない。**未知のマーカーらしき行を
  警告せよという明文は記法のどこにも無い**（データタイプは `notation:126` のとおり予約語であり、
  予約語以外は解析の起点にならない）。**上の「判断」（WARN が要る＝仕様として不適切）と食い違う。**
  WARN は改善提案として残るが、記法違反ではないため #25.5 の対象にしない。
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
- NTF 仕様としての判定: **対応不要**（入力が記法違反）。`notation:389-390`「`no`: テストショット番号を
  1からの連番で記載する。空の場合はエラーになる」、`notation:1267`「フィールド名称行（先頭セルに `no` と
  記載する行）」、`notation:1269`「メッセージボディの各行の先頭要素は、フィールドとしては読み込まれない
  ラベル列である」により、メタ列は記法上必須である。**記法違反の入力に対する挙動を定めた明文は無い。**
  ずらすのも本体 `SendSyncMessageParser`（L99/L134）であり converter 側では変えられない。上の「判断」と一致する。

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
- NTF 仕様としての判定: **対応不要**（記法に明文が無い）。`notation:658` が定めているのは
  「カラム名を宣言していながら、個々の行でそのカラムの値を書かなかった場合」＝**不足側だけ**であり、
  **カラム名を宣言していない列に値を書く（余り側）ことを認める明文も、警告せよという明文も無い**。
  ファイルデータ側では逆に `notation:891`「データ要素数が不正である」がエラー条件として挙がるのみである。
  **上の「判断」（少なくとも WARN が要る）と食い違う。** WARN は改善提案として残る。

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
- NTF 仕様としての判定: **対応不要**（記法に明文が無い）。`notation:1158`「フレームワーク制御ヘッダ以降の
  メッセージボディは、フィールド名称・データ型・フィールド長・データという、前述のファイルデータと同じ構成を持つ」
  は構成の説明であり、**1 つのメッセージブロック内に 2 つ目のフィールド名称行を置けるとは書いていない**。
  Excel 形式では先頭要素がラベル列であること（`notation:1267`／`notation:1269`）が前提となる。
  吸収するのも本体 `MessageParser` の匿名 `FixedLengthFileParser` である。上の「判断」と一致する。

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
- NTF 仕様としての判定: **対応不要**（記法どおりの挙動）。`notation:198`「データタイプ名で始まっていれば
  合致する」＝**前方一致は明文で定められた解釈**であり、`notation:266`「データタイプが合致する（Excel 形式では
  データタイプ名の前方一致）データブロックのうち、ID が完全一致する最初の1件を取得」も同じ前提に立つ。
  上の「判断」（受容できる）と一致する。

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
- NTF 仕様としての判定: **対応不要**（記法に明文が無い）。例外を投げるのは本体
  `PoiXlsReader`（`throw new RuntimeException("test data file open failed.", e)`。tsrc L191 で実測）であり、
  **記法は例外の型・メッセージを一切規定していない**。**上の「判断」（変換ツール側で包み直すのがあるべき姿）
  とは問いが違う。** 包み直しは改善提案として残るが、記法違反ではないため #25.5 の対象にしない。

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

### XLS-16 31 文字を超えるセクション名が黙って 31 文字へ切り詰められる（影響度 中・**衝突しない限り検出できない**・**#25.5 で修正済み**）

| 入力 | 書き出されるブック（**#25.5 前**） | 書き出されるブック（**#25.5 後**） | 担保テスト（#25.5 後の名前） |
|---|---|---|---|
| セクション名 `a` × 32 文字 | シート名は `a` × **31 文字**。例外も警告も出ない。元の名前では `Workbook#getSheet` が `null` を返し、変換ツール自身の読み戻し（`XlsFormatReader`）も `IllegalArgumentException: sheet not found.` になる | `IllegalArgumentException: シート名が Excel の上限 31 文字を超えています。… sheetName='aaa…a', length=32`。**ブックは作られない** | `XlsFormatWriterInvalidOutputTest#rejectsSheetNameLongerThanExcelLimit`（旧名 `truncatesSheetNameLongerThanExcelLimitSilently`） |
| セクション名 `a` × 31 文字（上限ちょうど） | そのまま書かれる（切り詰めなし）。元の名前でシートを引ける | 変わらない | `#writesSheetNameOfExcelLimitLengthAsIs` |
| 先頭 31 文字が同じで 32 文字目だけ異なる 2 セクション | `IllegalArgumentException: The workbook already contains a sheet of this name`（衝突したときだけ失敗する） | 32 文字の時点で文字数超過として落ちるため、この入力は**衝突判定に到達しない**。重複判定は**同じ 31 文字のセクション名 2 つ**で担保し直した | `#failsWhenSameSheetNameOfLimitLengthIsUsedTwice`（旧名 `failsWhenTruncatedSheetNamesCollide`。入力も 31 文字 2 つへ変えてある） |
| 大文字小文字だけが違う 2 セクション（`abc` と `ABC`。どちらも 3 文字で切り詰めは走らない） | 同上（`The workbook already contains a sheet of this name`）。ブックは作られない | 変わらない（31 文字以下なので文字数検査を通り、POI の `equalsIgnoreCase` 判定に到達する） | `#failsWhenSheetNamesDifferOnlyInCase` |
| セクション名 `a` × 31 文字 ＋ `/`（32 文字。禁止文字が index 31） | **例外にならず**、`a` × 31 文字のシートを持つブックが書き出される（切り詰めが禁止文字検査を無効化する） | **文字数超過**で落ちる（禁止文字が切り詰めで消える前に弾かれる）。ブックは作られない | `#rejectsSheetNameWhoseForbiddenCharacterWouldBeRemovedByTruncation`（旧名 `writesSheetNameWhoseForbiddenCharacterIsRemovedByTruncation`） |
| セクション名 `a` × 30 文字 ＋ `/`（**31 文字**。禁止文字が index 30） | —（#25.5 前は 32 文字の `"a"×30 + "/a"` で検査していた。メッセージのシート名が切り詰め後の 31 文字になることが検査順序の裏づけだった） | `IllegalArgumentException: Invalid char (/) found at index (30) in sheet name 'aaa…a/'`。**メッセージのシート名は渡した名前そのもの**（31 文字なので切り詰めは起きない） | `#rejectsSheetNameWhoseForbiddenCharacterIsAtTheLastPosition`（旧名 `rejectsSheetNameWhoseForbiddenCharacterSurvivesTruncation`。入力も 31 文字へ変えてある） |

- 原因: POI 3.8 の `XSSFWorkbook#createSheet(String)` が、31 文字を超える名前を `substring(0, 31)` で
  切り詰めてから `WorkbookUtil.validateSheetName` に掛ける（`javap -c` で逆アセンブルして確認。
  重複判定 `containsSheet` も両辺を 31 文字へ切り詰めて `equalsIgnoreCase` で比べるため、
  切り詰め後に同名になる 2 セクションはここで弾かれる）。
  **引用したこの機構は #25.5 前の時点で切り詰め側・`equalsIgnoreCase` 側の両方を実測で押さえてあった**:
  切り詰め側は当時の `#failsWhenTruncatedSheetNamesCollide`（先頭 31 文字が同じ 32 文字 2 つ）、
  `equalsIgnoreCase` 側は `#failsWhenSheetNamesDifferOnlyInCase`（レビュー指摘・第 3 ラウンドで追加）。
  **#25.5 で 32 文字超が `createSheet` に到達しなくなったため、切り詰め側の入力はもう作れない**
  （同テストは同名 31 文字 2 つでの重複判定へ書き直した）。`XlsFormatWriter#build`（L126）は
  #25.5 前は `workbook.createSheet(section.getName())` をそのまま呼ぶだけで、長さを検査も報告もしなかった。
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
  Excel の制約であり回避はできないため、黙って変えないことが要点である。**この「判断」は #19〜#25 の
  時点のもので、当時は修正しない前提だった。#25.5 で方針が変わり、下の判定のとおり修正済みである**
  （採ったのは「変換前に弾く」ほうである）。
- NTF 仕様としての判定: **要対応**。`notation:590`「読み込み単位の名前（Excel 形式ではシート名、YAML 形式では
  ファイル名）と ID を指定して List 形式または Map 形式でデータを取得できる」のとおり**シート名は
  呼び出し側が渡す引き当てキー**であり、黙って別名へ変えれば呼び出し側から引けなくなる。
  **#25.5 で修正済み（e80a4dd）**。`XlsFormatWriter#build`（L128）が `createSheet`（L129）の**前に**
  `requireValidSheetNameLength`（L150）を呼び、31 文字超を `IllegalArgumentException` で落とす
  （POI 3.8 の `XSSFWorkbook#createSheet` による `substring(0,31)` の前に置いたため、
  切り詰めが禁止文字検査を無効化する抜けも同時に閉じた）。
- **修正は本リポジトリ内で完結した**（`XlsFormatWriter` のみ。本体・yaml への変更はゼロ）。

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
- NTF 仕様としての判定: **対応不要**（記法に明文が無い）。解説書に「制御文字」の語は **1 件も無い**
  （`grep -c 制御文字 testdata_notation.rst` → **0**。実行して確認）。XML で表現できない文字を保存する手段は
  `.xlsx`（XML 形式）には存在せず、置換するのも POI である。上の「判断」（記録に留める）と一致する。

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
- NTF 仕様としての判定: **対応不要**（記法どおりの帰結）。`notation:1426`「セル内の改行（Alt+Enter）は LF として
  扱われる。これはテスティングフレームワークとは関係のない Excel 自体の仕様」＝**Excel 形式のセルで表せる
  改行は LF である**ことを記法自身が明言している。CR が LF になるのはその帰結であり、converter の判断ではない。
  上の「判断」（記録に留める）と一致する。

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
- NTF 仕様としての判定: **対応不要**（記法に明文が無い）。解説書にセル文字数の上限（`32767`）への言及は
  **1 件も無い**（`grep -c 32767 testdata_notation.rst` → **0**。実行して確認）。Excel／POI 側の制約である。
  上の「判断」（受容できる）と一致する。

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
本書の記述だけが誤りになる**。そのため `XlsFormatWriterModelTest` の末尾 2 件
（`#dropsDefaultDataTypeBlockWhenReadBack` ／ `#promotesFirstDataRowToColumnNamesWhenEmptyColumnNamesAreReadBack`）
が読み戻しを実検査する。これらは軸要素の担保としては数えない（steering Rules フェーズ2（往復テストの扱い））。
**3 件目だった `#failsToReadBackRecordWithoutFields`（XLS-22）は削除した**——XLS-22 を修正して
その版面自体が書き出されなくなったため、読み戻しを検査する対象が無くなった。

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
- NTF 仕様としての判定: **対応不要**（到達経路が無い）。記法のテーブルデータは
  `notation:652`「データタイプと識別子の値・カラム名・データ行という共通の構成を持つ」のとおりカラム名を持つ構成であり、
  **カラム名 0 件のブロックを認める明文は無い**。辺①・辺②はこの形の中間モデルを生成せず、
  手で組み立てた場合にだけ到達する。読み戻しで消える機構自体は XLS-05 と同じ `notation:1535` である。
  **上の「判断」（仕様として不適切・データ損失）と食い違う。** 記法の外にある入力を守れという明文が無いため
  「対応不要」とするが、危険性の指摘としては「判断」欄が正しい。

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
- NTF 仕様としての判定: **対応不要**（到達経路が無い）。`DataType.DEFAULT` は記法の予約語
  （`notation:126`「データタイプは、データブロックの用途を示す予約語であり、解析の起点になる」）に無く、
  辺①・辺②のいずれもこの値の中間モデルを生成しない。**辺③と辺④で扱いを揃えよという明文も無い。**
  **上の「判断」（仕様として不適切・非対称）と食い違う。** 揃えるのが筋であることは変わらないが、
  記法違反ではないため #25.5 の対象にしない。

### XLS-22 フィールド 0 件のレコードレイアウトは、書き出せるが読み戻せない `.xlsx` になる（影響度 低・例外で止まるため検出できる・**#25.5 で修正済み**）

**辺④の同じ形は YML-12 の 3 形目**（`record_fragment.fields` 空）である。同じ中間モデル値
（`RecordLayout.fields` が空）を辺③・辺④のそれぞれが書き出したときの現れ方であり、
**#25.5 で両辺に同じ番人を置いて同時に閉じた**（`b9ff38e`）。

| 入力（中間モデル） | #25.5 前に書き出されていた版面 | #25.5 前の読み戻し | 担保テスト |
|---|---|---|---|
| `SETUP_FIXED=f.dat`／`RecordLayout("data", fields=[], rows=[[v]])` | 識別行／名前行 `data`, 空セル／型行 空セル 2 個／長さ行 空セル 2 個／データ行 空セル, `v` | `IllegalStateException: can't get data. …` ← 原因 `IllegalStateException: directive or data names row must have two columns at least. [data]` | **#25.5 後**は書き出し自体が `IllegalArgumentException` になる: `XlsFormatWriterTest#rejectsRecordWithoutFieldsInFileBlock`／`#rejectsRecordWithoutFieldsInMessageBlock` |

**上表の版面・読み戻しは #25.5 前の実測である**（プローブ実行 2026-08-13）。修正後はこの版面自体が
書き出されないため、当時の担保テスト（`XlsFormatWriterModelTest#writesRecordWithoutFieldColumnsWhenFieldsAreEmpty`
／`#failsToReadBackRecordWithoutFields`）は削除し、番人テストへ置き換えた。

- 原因: フィールドが 0 件だと名前行がレコード種別セル 1 個だけになる（版面幅への矩形整形で右は空セルになるが、
  本体パーサは空セルを行の使用範囲に数えない）。本体 `DataFileParser` が名前行に 2 列以上を要求するため弾かれる。
  これは辺①で C-17（`RecordLayout.fields` 空）を**到達不能**と判定した根拠と同じ機構である
  （「到達不能と判定した軸要素（#20 で新たに判明したもの）」の C-17 行）。
- 実測: 上表のとおり。書き出し自体は成功し、例外になるのは読み戻し側である。
- 影響: `XlsFormatWriter` の Javadoc は「本体パーサが読み戻せる版面で書く」と謳っているが、この入力では
  成り立たない。ただし読み戻しは loud に失敗するため、黙って壊れることはない。
  **到達経路は辺①・辺②のいずれにも無い**（辺①は上記のとおり到達不能。辺②も `$defs.record_fragment` が
  `fields` を必須かつ `minItems` ＝ 1 とするため到達不能で、根拠は
  `YamlFormatReaderInvalidInputTest` の C-17 到達不能テストである）。
  **これは影響範囲の説明であって判定の根拠ではない**——判定の根拠は上記のとおり中間モデルの契約の穴である。
- 判断: 仕様として不適切である。**同種の前提崩れを書き出し時に弾く番人は既に 1 つある**
  （`XlsFormatWriter#appendRecords` は 2 レコード目以降のレコード種別が空だと `IllegalStateException` を送出する）。
  フィールド 0 件も同じ思想で書き出し時に弾くのが筋である。**#25.5 で修正済み**（`b9ff38e`）。
- NTF 仕様としての判定: **要対応**。**根拠は到達可能性ではなく、中間モデルの契約の穴である。**
  フィールド 0 件のレコードレイアウトは **Excel 記法にも YAML 記法にも存在しない形**であり
  （Excel は `notation:888` が「フィールド名称リストまたはデータ型リストが未指定または空である」を
  記述時のエラーに挙げる。YAML は `$defs.record_fragment` が `fields` を必須かつ `minItems` ＝ 1 とする）、
  **両形式が表現できない値を中間モデルだけが保持できる**状態になっていた。中間モデルの契約は
  4 辺すべてが表現できる範囲で定めるべきであるから、これは辺③単体の不具合ではなく中間モデルの契約の穴である。
  **`notation:888` は「ファイルデータの記述時にエラーとなるケース」の一覧であり、辺①②（読み取り）に
  課される条件である。辺③（書き出し）に直接課す読み方はしない**——ここで根拠にしているのは
  「その形は Excel 記法として存在しない」ことであって「辺③が `notation:888` に違反する」ことではない。
  **#25.5 で修正済み**（`b9ff38e`）。`RecordLayout` の Javadoc に「`fields` は 1 件以上」を明記し、
  `XlsFormatWriter#appendRecords` が `IllegalArgumentException` で弾くようにした。
  **辺④の同じ形は YML-12 の 3 形目**（`record_fragment.fields` 空）であり、同じコミットで
  `YamlFormatWriter#emitRecords` にも同じ番人を置いた。

> **`RecordLayout` のコンストラクタには番人を置かない（2026-08-18・ユーザー確定）。**
> `fields` 空の検査を中間モデル側（コンストラクタ）で行う案を検討したうえで却下した。
> 同じ検討が後から蒸し返されないよう、判断と却下理由をここに残す。
>
> - **理由 1（番人の役割）** — 番人の役割は「**どの形式にも写せない値をそこで止めること**」であり、
>   それが起きる場所は**辺③④（書き出し）**である。中間モデル側で止めると、本作業の目的である
>   4 辺の担保から検査点が外れてしまう。
> - **理由 2（実測）** — コンストラクタに番人を置いた場合、当時の全 **541 件**のうち失敗するのは
>   **1 件のみ**で、**辺③④に置いた番人テスト 4 件が空振りになる**（番人へ到達する前に
>   中間モデルの生成で落ちるため）。番人を置いた意味が測れなくなる。
>   **この 541 件は #25.5 の途中時点（`f80c192` の前）の総数である**（現在の総数は 547 件）。
>   当時の実測値としてそのまま残し、書き換えない。
> - `RecordLayoutTest#レコード種別省略をnullで保持する` は現状のまま（`List.of()` を渡す）でよい。

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
- NTF 仕様としての判定: **対応不要**（到達経路が無い）。`notation:69` はシートの用意のしかたを推奨として述べるのみで、
  **シートを 1 枚も持たないブックについての明文は無い**。セクション 0 件のコンテナは辺①・辺②のいずれも生成しない。
  上の「判断」（受容できる）と一致する。

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
- 判断: 受容できる（記録のみ）。到達経路が無く、修正すると「契約の二重実装」になる。
  ただし**担保の穴としては開示する**（steering Rules フェーズ2）。修正はこの作業では行わない。
  テストも足していない（足すなら「送信系に非空 `fwHeaderFields` を渡すと何が起きるか」を
  現状挙動として固定する 1 件になるが、それは中間モデルの契約が禁じている入力を作ることになるため、
  足すかどうかは辺③以外の辺（辺②・辺④）の扱いと揃えて判断すべきである）。
- NTF 仕様としての判定: **対応不要**（挙動の課題ではない）。食い違っているのは `XlsFormatWriter` の Javadoc と
  担保の有無であり、記法の明文に反する挙動は無い。送信系の `fwHeaderFields` が空であることは中間モデル側の契約
  （`MessageDataBlock` の Javadoc・仕様 MS-04）が保証しており、非空を渡す入力はどの経路でも生じない。
  上の「判断」（受容できる／担保の穴としては開示する）と一致する。

### 課題としないと判断した観測結果（#23）

| 観測 | 判断 |
|---|---|
| `EXPECTED_FIXED` の識別セルが `EXPECTED_FIXED=exp.dat` になり、固定長なので長さ行が出る | 妥当（`marker` と `layoutFile` の設計どおり） |
| `EXPECTED_VARIABLE` の識別セルが `EXPECTED_VARIABLE[g2]=exp.csv` になり、可変長なので長さ行が出ない | 妥当（同上。グループ ID は中間モデルが整形済みの `[g2]` を保持しそのまま連結される） |
| `DataType.DEFAULT` のヘッダ色がその他グループ（`HEADER_OTHER`）になる | 妥当（`BlockLayout#headerFill` の Javadoc が `DEFAULT` を「それ以外」に明記している） |
| ブロック 0 件のセクションが、行を 1 行も持たないシートになる（C-04 / E-1(0)） | 妥当（読み戻すとブロック 0 件のセクションに戻る＝往復が安定する。実測） |
| データ行 0 件のブロックが識別行とカラム名行だけになる（C-09 / E-2(0)） | 妥当（読み戻すと `rows=[]` に戻る。実測） |
| レコードレイアウト 0 件のファイル／メッセージが識別行とディレクティブ行（FW 制御ヘッダ行）だけになる（C-12 / C-15 / E-3(0)） | **ファイル（C-12）のみ妥当**（読み戻すと `records=[]` に戻る。実測）。**メッセージ（C-15）は 2026-08-18・#25.5 で判断を変えた** —— レコード 0 件の電文は記法にもスキーマにも存在しない形のため、`XlsFormatWriter#layoutMessage` が `IllegalArgumentException` で弾くようにした（**YML-12** の 2 形目）。この観測を固定していた `XlsFormatWriterModelTest#writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty` は削除し、番人テスト `XlsFormatWriterTest#rejectsMessageBlockWithoutRecords` ／ `#rejectsSendSyncMessageBlockWithoutRecords` へ置き換えた |
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
- NTF 仕様としての判定: **対応不要**（帰属が converter の外・`NULL` は実行時に null になる）。
  記法が定める null の書き方は `notation:767-768`「null（Java の null）＝ セルに `null`（大文字小文字不問）」であり、
  **YAML のネイティブ null タグ `~` を null と読めという明文は無い**。加えて `notation:1312`「パース処理では、
  読み込んだ各セル・エントリ値を `Interpreter` のチェーンに順に通して変換する」と `notation:1322-1323`
  「`NullInterpreter`: `null`・`NULL`・`Null`（大文字小文字不問）を Java の null に変換する」により、
  **文字列として中間モデルへ入った `NULL`／`Null` は NTF 実行時に Java の null へ変換される**。
  **上の「判断」（NULL のつもりの記述が黙って文字列になる＝仕様として不適切）は、`NULL`／`Null` については
  この 2 行を根拠に成り立たない**（実行時の解釈は作成者の意図どおりになる）。記法に照らして残るのは `~` だけであり、
  それはそもそも記法が認めていない書き方である。スカラー解決を行うのは yaml 側の `YamlLoader` であり
  converter は関与しない。

### YML-02 送信系で `group_id` を省略したエントリがブロックごと黙って消える（影響度 中・**検出できない**・**#25.5 で修正済み**）

| 入力 | 中間モデルへ入る結果（**#25.5 前**） | 中間モデルへ入る結果（**#25.5 後**） | 担保テスト |
|---|---|---|---|
| `response_body_messages` に `group_id` 無しの `id: "DROP"` 1 件と `group_id: "g"` の `id: "KEEP"` 1 件 | ブロックは **1 件**（`KEEP` だけ）。`DROP` はブロックもレコードも中間モデルに現れない。**例外は出ない** | ブロックは **2 件**。`DROP` は**デフォルトグループ**（`getGroupId()` が空文字）のブロックとして残り、フィールド定義・データ行も保たれる。並びはデフォルトグループが先、`[g]` が後 | `YamlFormatReaderRealFileTest#readsSendSyncEntryWithoutGroupIdAsDefaultGroupFromRealYaml`（旧名 `dropsSendSyncEntryWithoutGroupIdFromRealYaml`）／in-memory 経路は `YamlFormatReaderTest#readSendSync_entryWithoutGroupId_isReadAsDefaultGroup`（旧名 `readSendSync_entryWithoutGroupId_isDropped`） |

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

- 原因（**#25.5 前**）: `YamlFormatReader#addSendSyncBlocks` は `rawGroupsInOrder(yaml, sectionKey)` を回して
  ブロックを作る。`rawGroupsInOrder` は `group_id` が **非 null のエントリだけ**を列挙していた
  （`if (group != null && !groups.contains(group))`）ため、`group_id` の無いエントリは
  どのグループにも属さず、ブロック生成のループに一度も入らなかった。
  （`src/main/java/nablarch/test/tool/converter/yaml/YamlFormatReader.java` の `addSendSyncBlocks` と
  `rawGroupsInOrder`。同メソッドの Javadoc は「`group_id` 必須」と書いていたが、上のとおり
  スキーマは必須にしていない。Javadoc の記述と一次情報が食い違っていた。）
- 修正（**#25.5**・36e94a4）: `rawGroupsInOrder` の `null` 除外をやめてデフォルトグループを `null` として列挙し、
  `entriesForRawGroup` の突き合わせを `Objects.equals` に、整形済みグループ ID を
  `rawGroup != null ? "[" + rawGroup + "]" : ""` に変えた。器側 `YamlTestCoreAdapter#readSendSyncMessages` も
  `group_id` 省略エントリを拾えるようにしてある。Javadoc の「`group_id` 必須」も直した。
- 実測: 上表のとおり。#25.5 前後とも**例外にならない**（担保テストが固定している）。
  なお XLS-10／XLS-13 が行っているような「WARNING 以上のログが 0 件」というアサートは本課題では
  行っていないため、**警告が 1 件も出ないことは未確認である**（`java.util.logging` のハンドラを
  付けていない）。「検出できない」の根拠は、例外にならずブロックが消えるという観測に置く。
- 影響（**#25.5 前**）: 送信系で `group_id` を省略した（＝ id 直接指定で使うつもりの）エントリは、変換後の成果物から
  ブロックごと消えた。入力と出力を突き合わせない限り気づけなかった。
- `nablarch-example-web`（サンプルアプリ）由来の変換出力 YAML には発現していない
  （送信系セクション自体が無い。`grep -rn 'expected_request_\|response_header_messages\|response_body_messages'
  src/test/java/nablarch/test/tool/converter/SampleConversionTest/` → ヒット 0）。
  ただし辺②の入力は本来**手書きの YAML** であり、対象PJの実データでの発現は未知である。
- 判断: **仕様として不適切**（スキーマが仕様内と認める入力が黙って落ちる。少なくとも
  「`group_id` 無しのエントリを drop した」と報せるべきである）。**この「判断」は #19〜#25 の時点のもので、
  当時は修正しない前提だった。#25.5 で方針が変わり、下の判定のとおり修正済みである。**
  **修正は本リポジトリ内で完結した**（原因コードが `src/main` にあったため）。
- NTF 仕様としての判定: **要対応**。`notation:254`「グループIDを省略した場合は、グループIDを持たない
  データブロック（デフォルトグループ）が対象になる」＝**省略は仕様内の書き方**であり、ブロックごと落とすのは
  この明文に反する。**#25.5 で修正済み（36e94a4）**。`YamlFormatReader` が `group_id` 省略の送信同期エントリを
  デフォルトグループのブロックとして読むようにした。

### YML-03 `record_type: FW_HEADER` のレコードが黙って捨てられる（影響度 中・**検出できない**・**修正済み（yaml 側 `0b53910` ＋ 本リポジトリ側 `f0f8718`）**）

| 入力 | 中間モデルへ入る結果（修正前） | 中間モデルへ入る結果（修正後・現在） | 担保テスト |
|---|---|---|---|
| `messages` に `record_type: "FW_HEADER"` のレコード 1 件だけ（`fw_header:` は書かない） | ブロックは生成されるが `records` **0 件**・`fwHeaderFields` **0 件**。書いたフィールド定義とデータ行が消える。**例外は出ない** | `records` **1 件**（`recordType` は原文どおり `FW_HEADER`。フィールド定義・データ行とも残る）・`fwHeaderFields` **0 件**（`fw_header:` を書いていないため） | `YamlFormatReaderRealFileTest#keepsFwHeaderNamedRecordInMessageFromRealYaml`（`@Ignore` を外して緑） |

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

- 原因（修正前）: 落としていたのは **converter と本体器の両方**であった。
  - 本体器（yaml 側）: `nablarch/test/core/reader/yaml/YamlFileBuilder.java` の `buildFragmentsInternal` が
    `if (skipFwHeader && FW_HEADER_RECORD_TYPE.equals(recordType)) { continue; }` で断片を作らなかった
    （`skipFwHeader` はメッセージ系・送信系で真）。`FW_HEADER_RECORD_TYPE` は
    `nablarch/test/core/reader/yaml/YamlSection.java` で `"FW_HEADER"` と定義されていた。
  - converter: `YamlFormatReader#recordsWithoutFwHeader` が同じ名前のレコードを原文側からも除いていた。
    除かなければ「器の断片数と原文レコード数の不一致」で `IllegalStateException` になるため、
    converter 側だけを直しても解決しなかった。
- **修正（2026-08-18）**: 両側そろって直した。
  - **yaml 側**: `nablarch-testing-yaml` の `0b53910`（ブランチ `feature/ntf-yaml`。
    「fix: record_type: FW_HEADER によるレコード読み飛ばしを廃止する」）。同リポジトリの `8e1ea76` の祖先に含まれる
    （`git merge-base --is-ancestor 0b53910 HEAD` で確認）。修正後は `grep -rn "FW_HEADER_RECORD_TYPE" src/` が
    ヒット 0 件で、定数ごと消えている。`mvn clean install` で `~/.m2` の
    `nablarch-testing-yaml-1.0.0-SNAPSHOT.jar` を差し替えた（2026-08-13 17:04 → 2026-08-18 09:30）。
  - **converter 側**: `YamlFormatReader#recordsWithoutFwHeader` を廃止し、メッセージ系（`#addMessageBlocks`）・
    送信系（`#addSendSyncBlocks`）の呼び出しをファイル系と同じ `#records(entry)` に揃えた。
    `YamlSection.FW_HEADER_RECORD_TYPE` が yaml 側で消えたため、この廃止は**コンパイルを通すために必須**でもある
    （そのままでは converter の `compile` が落ちる）。

  再現コマンド:

  ```sh
  cd "$(mktemp -d)" \
    && unzip -oq ~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT-sources.jar \
    && grep -n 'FW_HEADER_RECORD_TYPE' nablarch/test/core/reader/yaml/YamlFileBuilder.java nablarch/test/core/reader/yaml/YamlSection.java
  ```

- 実測: 上表のとおり。修正前の観測（`FW_HEADER` レコードに本文レコードを 1 件足した入力では本文だけが残る、
  送信系（`response_body_messages`）でも同じく落ちる）は、#25.5 まで
  `YamlFormatReaderRealFileTest#dropsFwHeaderNamedRecordFromSendSyncInRealYaml` が
  **1 件で両方とも固定していた**（送信系に `FW_HEADER` ＋本文を 1 件ずつ置き、本文だけが残ることをアサート）。
  現在は同じ入力で **2 件とも残る**ことを `#keepsFwHeaderNamedRecordInSendSyncFromRealYaml` が固定している。
  **修正前の挙動を固定しているアクティブなテストは無い**（残す必要が無くなったため）。
  修正前の「送信系に `FW_HEADER` のみを置いた形」の観測（ブロックだけが残る。掃引表 項目 21）も同様である。
- 影響（修正前）: スキーマの description を読んで「FW_HEADER は予約値ではないので可読性のために使ってよい」と
  判断した作成者が `record_type: "FW_HEADER"` と書くと、そのレコードのフィールド定義もデータ行も
  変換後の成果物から消えた。入力と出力を突き合わせない限り気づけない。
  当時は NTF 実行時も同じ器（`YamlFileBuilder`）が同じレコードを落としていたため **converter の変換は
  NTF の解釈に忠実**であり、食い違っていたのは**スキーマの description と実装**の側であった。
  修正後は description と実装が一致し、converter もそれに追随している。
- `nablarch-example-web`（サンプルアプリ）由来の変換出力 YAML には発現していない
  （`grep -rn 'FW_HEADER' src/test/java/nablarch/test/tool/converter/SampleConversionTest/` → ヒット 0）。
  対象PJの実データでの発現は未知である。
- **3 経路で扱いが揃った**（修正前は経路によって違った。いずれもアクティブなテストで固定している）:

  | 経路 | 修正前 | 修正後・現在 | 担保テスト（`YamlFormatReaderRealFileTest#`） |
  |---|---|---|---|
  | ファイル系（`setup_files` ／ `expected_files`） | **残る** | **残る** | `keepsFwHeaderNamedRecordInFileFromRealYaml` |
  | メッセージ系（`messages`） | 落ちる | **残る** | `keepsFwHeaderNamedRecordInMessageFromRealYaml`（旧テスト `dropsFwHeaderNamedRecordFromRealYaml` を置き換えたもの） |
  | 送信系（`response_body_messages` ほか） | 落ちる | **残る** | `keepsFwHeaderNamedRecordInSendSyncFromRealYaml`（旧テスト `dropsFwHeaderNamedRecordFromSendSyncInRealYaml` を置き換えたもの） |

  修正前は `YamlFormatReader#addFileBlocks` が `records(entry)` を、メッセージ系（`#addMessageBlocks`）と
  送信系（`#addSendSyncBlocks`）が `#recordsWithoutFwHeader(entry)` を使っていたためである。
  現在は 3 経路とも `#records(entry)` を使う。
- 判断: **仕様として不適切**（スキーマの description が「予約値はない」と明言する値を実装が予約値として扱い、
  黙ってデータを落とす）。**この「判断」は #19〜#25 の時点のもので、当時は帰属が yaml 側にあり
  本リポジトリだけでは直せなかった。** yaml 側が `0b53910` で `skipFwHeader` の特別扱いを外したため、
  下の判定のとおり両側そろって修正済みである。
- NTF 仕様としての判定: **要対応 → 修正済み（2026-08-18）**。本体スキーマ
  `nablarch/test/ntf-testdata-yaml-schema.json` の `$defs.record_fragment.properties.record_type.description` が
  「可読性のために任意の名前を記述してよい。**FW_HEADER のような予約値はない**」と明言しているのに対し、
  実装（`YamlFileBuilder#skipFwHeader`）が `FW_HEADER` を予約値として扱ってレコードを捨てていた。
  **修正の出典は yaml 側が `0b53910`（`nablarch-testing-yaml` ブランチ `feature/ntf-yaml`。
  「fix: record_type: FW_HEADER によるレコード読み飛ばしを廃止する」）、converter 側が
  `f0f8718`**（`YamlFormatReader#recordsWithoutFwHeader` の廃止）である。
  #25.5 で置いた `@Ignore("YML-03: yaml側の修正待ち")` の待機テスト 2 件
  （`YamlFormatReaderRealFileTest#keepsFwHeaderNamedRecordInMessageFromRealYaml` ／
  `#keepsFwHeaderNamedRecordInSendSyncFromRealYaml`）は `@Ignore` を外し、外した状態で緑になることを
  実行して確認した。あわせて、修正前の挙動を固定していた
  `YamlFormatReaderTest#readMessage_mapsRawFwHeaderAndExcludesFwHeaderRecord` を
  `#readMessage_mapsRawFwHeaderAndKeepsFwHeaderNamedRecord`（2 件とも残ることを期待値に書いたもの）へ
  置き換えた（2 本残さない）。

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
| 未知のトップレベルキーが**実ファイル経路では例外**になる（in-memory 経路では無視される） | 妥当。スキーマのルートが `additionalProperties: false` であるため。`YamlFormatReader#addBlocksForSection` の「未知キーは無視」は**実ファイル経路では到達不能**である（スキーマのトップレベル `properties` 11 キーと同メソッドの分岐 11 本が完全に一致し、分岐に落ちない既知キーは存在しない。JaCoCo が分岐 108/108 なのは in-memory 経路が通しているため）。loud に失敗するので黙って壊れない（担保: `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenTopLevelKeyIsUnknown`） |
| 空ファイルが例外にならず、ブロック 0 件のコンテナになる | 妥当（`YamlLoader#load` が `loaded == null` のとき空 Map を返す。トップレベルに必須キーは無いためスキーマ上も適合する。担保: `#readsEmptyFileAsContainerWithoutBlocks`） |
| `directives` を書かなくても中間モデルに `file-type` が現れる | 既知（**XLS-07**）。同じ本体器（`DataFile`）を使うため辺①と同じ挙動になる（担保: `YamlFormatReaderRealFileTest#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInFile` ／ `#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInMessage`） |
| `record_type` を書かないと `RecordLayout.recordType` が `null` になる | 妥当（`RecordLayout` の Javadoc「省略時は null」どおり。辺①の実 `.xlsx` 経路が `""` になる **XLS-06** とは非対称だが、YAML 側は仕様どおりである。担保: `#readsEmptyRowsFromRecordLayoutWithoutRows`） |
| テーブル系ではテーブル名とカラム名が大文字化される（`table: "my_table"` → `MY_TABLE`、`user_id` → `USER_ID`）。LIST_MAP は原文の大小のまま | 妥当。器（nablarch-testing の `TableData`）が NTF 実行時に行うのと**同じ正規化**であり、変換後の YAML／Excel を NTF が読んでも解釈は変わらない。テーブル名については `$defs.table_data.properties.table.description` が「NTF により trim・大文字変換される」と明言している。**ただしカラム名の大文字化はスキーマのどこにも書かれておらず、LIST_MAP との非対称も文書化されていない。**衝突が起きた場合は値が消えるため、そちらは **YML-10** として課題に記録した（担保: `dropsValueWhenTableColumnNamesDifferOnlyByCase` ／ `keepsOriginalColumnCaseInListMap`） |

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
| C-15 `MessageDataBlock.records` 空（**上の但し書きのとおり、この行だけ「要追加」からの移動ではない**） | スキーマ `$defs.message_data.properties.records.minItems` ＝ 1（送信系の `expected_request_message_data` も同じ）。**実ファイル経路では到達できない**。#18 が ✅ としているのは in-memory 経路（`YamlFormatReaderTest#readMessage_emptyBody_isStillMapped`）である | 修正前は、実 `.yaml` で `records` 0 件のブロックが**別経路で**生じることを **YML-03**（`record_type: FW_HEADER` のレコードだけを書くと器も原文も 0 件になる）が示していた。**YML-03 が 2026-08-18 に修正されたため、この経路は無くなった**（同じ入力で `records` は 1 件になる）。実ファイル経路で C-15 に到達する手段は現在も無い |

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
**掃引はその後のレビュー指摘を受けて項目 28 まで広げ、そこで YML-09（項目 24）・YML-10（項目 27）・YML-11（項目 28）を見つけた**
（同じ掃引の続きであるため節を分けずに本節へ入れている）。

**掲載順**: 「凡例 → 並び順の原則」に従い、**検出できない**もの（YML-04・YML-05・YML-06・YML-08・YML-09・
YML-10・YML-11）を先に置き、loud に失敗するもの（YML-07）を最後に置く。課題 ID は発見順のまま振り直していない
（YML-09〜YML-11 は最後に見つかったが検出できない側であるため YML-07 より前に来る）。
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
  - converter（**LIST_MAP 経路のみ**）: `YamlFormatReader#nonMarkerColumns` が
    `YamlSection.resolveColumns(...)` の結果をそのまま（マーカーを除いただけで）カラム順に使う。
    器が返す行 Map には 2 行目以降のキーも入っているが、converter は列挙したカラムぶんしか取り出さない。
  - **テーブル経路は `nonMarkerColumns` を通らない。** `YamlFormatReader#addTableBlocks` は
    器の `TableData#getColumnNames()` を使う（`nonMarkerColumns` の呼び出し元は `addListMapBlocks` の
    1 か所だけである）。テーブル経路でカラムが先頭行だけで決まるのは、`TableData` を組み立てる
    yaml 側の `YamlTableDataBuilder` が同じ `resolveColumns` を使うためであり、**帰属は yaml 側だけ**である。

    再現コマンド:

    ```sh
    grep -n 'nonMarkerColumns' src/main/java/nablarch/test/tool/converter/yaml/YamlFormatReader.java
    ```

    出力は 2 行（`addListMapBlocks` 内の呼び出し 1 行と定義 1 行）だけで、`addTableBlocks` には現れない。

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
- NTF 仕様としての判定: **対応不要**（記法が先頭行基準を前提にしている）。`notation:658`「YAML 形式では、
  `rows:` の**先頭行のキーの一部を後続の行が持たない場合**、そのカラムは `null` を明示的に指定したのと同じ扱いになる」＝
  **カラム集合が先頭行のキーで決まることを前提にした明文**であり、逆（後続行にだけあるキー）を拾えという明文は無い。
  空マッピング `{}` の行がスキップされることも `notation:1535` の明文どおりである。
  **上の「判断」（仕様として不適切・データ損失）と食い違う。** WARN の提案は改善提案として残るが、
  先頭行基準そのものは記法違反ではない。

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
- NTF 仕様としての判定: **対応不要**（不足側は明文どおり／余り側は器の側）。不足側は `notation:883`
  「データ行のセル数（Excel形式）または `rows:` の各要素の長さ（YAML形式）がフィールド数より少ない場合、
  不足したフィールドは `""` として補完される」＝**明文どおりの挙動**である。余り側は `notation:891`
  「データ要素数が不正である」がエラー条件として挙がるが、**その判定を握るのは本体
  `DataFileFragment#addValue`（`String value = i < line.size() ? line.get(i) : "";`。tsrc L107／L175）であり、
  余った要素は converter に届く前に器が捨てている**。converter 側では検知できない。
  **上の「判断」（余りの drop に WARN が要る）と食い違う。** 指摘としては正しいが、直す場所は converter ではない。

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
- NTF 仕様としての判定: **対応不要**（記法に明文が無い）。`notation:266`「データタイプが合致する…データブロックのうち、
  **ID が完全一致する最初の1件を取得**」により、NTF 実行時に有効なのは 1 件目だけである。
  **変換後の成果物に無効な 2 件目以降が残ることを禁じる明文は無い**（記法が定めるのは取得側の解釈であって、
  変換ツールが何を書き出すかではない）。**上の「判断」（原文に存在しない組み合わせを作る＝仕様として不適切）と
  食い違う。** 器の「先着 1 件」に合わせるのが望ましいという主張は改善提案として残る。

### YML-08 ディレクティブ値が `trim()` されるため、スキーマ description が推奨する記法が壊れる（影響度 中・**(a) は検出できない**／(b) は loud・**#25.5 で修正済み**）

**#25.5 で辺②を修正した（6c8d90e）。下表の「中間モデルへ入る結果」は 3 行とも改訂してある。**

| # | 入力（`directives`） | #25.5 前の結果 | **#25.5 後（現在）の結果** | 担保テスト（`YamlFormatReaderInvalidInputTest#`） |
|---|---|---|---|---|
| (a) | `record-separator: "\r\n"`（description が推奨するリテラル記法。YAML は実際の CR LF に解決する） | **空文字**（値が消える）。例外にならない | **`"NONE"`**（空を辺①と同じ規則でシンボルへ戻す）。**書いた改行そのものは依然として失われている**（本体の `trim()` は converter の外） | `readsRecordSeparatorWrittenAsLiteralNewlineAsNoneSymbol` |
| (b) | `field-separator: "\t"`（description が「タブ文字に変換される」と書く記法。YAML は実際のタブに解決する） | `IllegalArgumentException: field-separator must be one character.but was `（末尾は空。trim 後の値） | **変化なし**（例外は converter へ届く前に本体側で送出されるため、辺②の修正では変わらない） | `failsWhenFieldSeparatorIsWrittenAsActualTab` |
| (参考) | `record-separator: "CRLF"`（シンボル記法） | **シンボルではなく実際の改行 `"\r\n"`** が入る（辺①は逆正規化してシンボルへ戻すため非対称） | **`"CRLF"`**（シンボルのまま入る。**辺①と対称になった**） | `readsRecordSeparatorSymbolAsSymbol` |

`field-separator` をエスケープ 2 文字記法（YAML のシングルクォート `'\t'`）で書いた場合も同じ修正の対象で、
中間モデル値は **実タブ 1 文字 → 2 文字記法 `\t`** に変わった（`readsFieldSeparatorWrittenAsEscapedTabNotation`）。

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
    辺②（`YamlFormatReader#toStringDirectives`）は素通しを選んでいたため、同じ入力表記が辺①と辺②で
    別の中間モデル値になっていた。**この 1 点だけが converter 側の欠陥であり、#25.5 で直した（6c8d90e）。**
    残る 2 者（trim による損失・description）は本リポジトリの外にあり、**今も直っていない。**
- 影響: (a) は**書いた改行が失われる**（#25.5 前は空文字、後は `NONE`。いずれにせよ原文の CRLF ではない）。
  (b) は loud に失敗するので気づける。参考行（シンボル → 実文字）は #25.5 で解消し、
  **中間モデルの値が原文の表記と一致するようになった。**
- **辺④の往復を実行して確かめた（#25.5・2026-08-14。「未確認」を解消）。** 中間モデルを手で組み立てて
  `YamlFormatWriter#write` で書き出し、同じディレクトリを `YamlFormatReader#read` で読み戻した実測である。

  | 中間モデルの `record-separator` | 辺④が書き出した YAML | 読み戻した中間モデル値 | 往復 |
  |---|---|---|---|
  | `"CRLF"`（シンボル） | `record-separator: "CRLF"` | **`"CRLF"`** | **安定する** |
  | 実改行 `CR LF` | `record-separator: "\r\n"`（エスケープ表記で書かれる） | **`"NONE"`** | **安定しない**（`CR LF` → `NONE`） |
  | （参考）`field-separator` に実タブ | `field-separator: "\t"` | 読み戻しが例外 `IllegalArgumentException: field-separator must be one character.but was `（末尾は空） | **loud に失敗する** |

  **予想は半分外れた。** 「辺④が実改行をそのまま書けば読み戻しで (a) の経路に入る」までは当たっている
  （書き出された `"\r\n"` は (a) の入力そのものであり、本体の `trim()` で値が失われる）。
  外れたのは結果で、**空文字ではなく `NONE` になる**——#25.5 の修正（6c8d90e）で辺②が空をシンボルへ
  逆正規化するようになったためである。**値が失われることそのものは変わっていない。**
  なお #25.5 の修正後、辺①・辺②はどちらも実制御文字を中間モデルへ入れないため、
  **この入力は中間モデルを手で組み立てた場合にのみ生じる。**
- 判断: **仕様として不適切**。**#25.5 で converter 側の 1 点（辺①との非対称）だけを直した（6c8d90e）。**
  trim による損失と description の食い違いは本リポジトリの外にあり、**未解決のまま残っている。**
- NTF 仕様としての判定: **要対応**。`notation:947-948`（`record-separator` はシンボルまたは任意のリテラル文字列）、
  `notation:1080`（`field-separator=\t`）、`notation:1116`（`record-separator CRLF`）はいずれも**シンボルと
  エスケープ 2 文字の記法しか示しておらず**、実制御文字のままのディレクティブ値は記法の外にある
  （本体 `DataFile#setDirective` の `trim()` で失われ、読み戻せない）。**#25.5 で修正済み（6c8d90e）**。
  辺②（`YamlFormatReader#toStringDirectives`）を辺①（`XlsFormatReader#normalizeDirectiveValue`）と同じ
  逆正規化に通した（共通化先は `DirectiveUtil#normalizeSeparator`）。

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
- NTF 仕様としての判定: **対応不要**（記法に明文が無い）。記法が定めるのはグループの収集方式
  （`notation:254`／`notation:266`）であって、**変換後のファイルの中でエントリをどの順に並べるかではない**。
  値は 1 つも失われない。**上の「判断」（原文の記述順を保つのがあるべき姿＝仕様として不適切）と食い違う。**
  あるべき姿としての指摘は残るが、記法違反ではないため #25.5 の対象にしない。

### YML-10 テーブル系のカラム名は大文字化されるため、大小だけが違うキーが衝突して値が黙って消える（影響度 高・**検出できない**）

| 入力（`setup_tables` の `rows`） | 中間モデルへ入る結果 | 担保テスト（`YamlFormatReaderInvalidInputTest#`） |
|---|---|---|
| `[{id: "1", ID: "2"}, {id: "3", ID: "4"}]` | `identifier=MY_TABLE`、`columnNames=[ID, ID]`、`rows=[[2, 2], [4, 4]]`。**`id` に書いた `"1"` `"3"` はどこにも残らない**うえ、**列名が重複した中間モデル**になる | `dropsValueWhenTableColumnNamesDifferOnlyByCase` |
| `list_maps` に同じ `[{id: "1", ID: "2"}]` | `columnNames=[id, ID]`、`rows=[[1, 2]]`。**原文の大小が保たれ、値も失われない** | `keepsOriginalColumnCaseInListMap` |

例外にも警告にもならない。

- **この入力はスキーマ上の仕様内である。** `$defs.table_data.properties.rows.items` は
  `{"type": "object", "additionalProperties": {"type": ["string", "null"]}}` で、
  **キーの大小にも一意性にも制約が無い**。YAML としても `id` と `ID` は別キーであるため、
  ローダの重複キー検査（`setAllowDuplicateKeys(false)`）にも掛からない。

  再現コマンド:

  ```sh
  python3 -c "
  import json,zipfile,os
  z=zipfile.ZipFile(os.path.expanduser('~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT.jar'))
  d=json.loads(z.read('nablarch/test/ntf-testdata-yaml-schema.json').decode('utf-8'))['\$defs']
  print(json.dumps(d['table_data']['properties']['rows']['items'], ensure_ascii=False))
  "
  ```

- 原因: **帰属は nablarch-testing 側である。** `nablarch/test/core/db/TableData.java` が
  テーブル名（`name.trim().toUpperCase()`）とカラム名（`columnNames[i].toUpperCase()`）を大文字化し、
  さらに行 Map へ値を入れるときも `map.put(columnNames[i].toUpperCase(), value)` とキーを大文字化する。
  大文字化後に同名となった 2 カラムは **1 つの Map エントリへ潰れ、後勝ちの値だけが残る**。
  converter（`YamlFormatReader#addTableBlocks`）は器が返した列名配列でそのまま値を引くため、
  重複した列名がそのまま中間モデルへ出て、同じ値が 2 回並ぶ。

  再現コマンド:

  ```sh
  cd "$(mktemp -d)" \
    && unzip -oq ~/.m2/repository/com/nablarch/framework/nablarch-testing/6-NEXT-SNAPSHOT/nablarch-testing-6-NEXT-SNAPSHOT-sources.jar \
    && grep -n 'toUpperCase' nablarch/test/core/db/TableData.java | head -4
  ```

- **前提となる事実（大文字化そのもの）**: テーブル系では、衝突が無い場合でも
  テーブル名とカラム名が大文字化される（`table: "my_table"` → `MY_TABLE`、
  `user_id` → `USER_ID`）。**LIST_MAP は経路が違い、原文の大小のまま入る**
  （`nonMarkerColumns` が `YamlSection#resolveColumns` の生キーを使う）。
  スキーマが大文字変換に触れているのは `$defs.table_data.properties.table.description`
  （「NTF により trim・大文字変換される」）**だけ**で、カラム名については何も書いていない。
  大文字化そのものを課題としない判断は下の「課題としないと判断した観測結果（#24）」に記した。
- **YML-04 との関係**: どちらも「スキーマが縛らないキー集合」に起因し、値が黙って消える。
  YML-04 は**行をまたいだキーの差**、本課題は**同一行内の大小の衝突**であり、原因も帰属も別である
  （YML-04 は yaml 側 `YamlSection#resolveColumns` ＋ converter、本課題は nablarch-testing 側 `TableData`）。
- **辺① との関係**: 辺①は軸F に **F1-05「カラム名重複」**を要素として持ち、
  `XlsFormatReader` は重複を検出して WARN ログを出す（steering #16）。
  **辺②には同じ検出が無く、大小違いという形でだけ重複が起こる。**
- 影響: 手書きの YAML でカラム名の大小が揺れることは自然に起こり得る（`id` と `ID` を混ぜて書く）。
  それが警告なしに片方だけ残る。NTF 実行時も同じ大文字化を行うため後段のテストは通ってしまう。
- 判断: **仕様として不適切**（少なくとも「大文字化後に同名となるカラムがある」ことを検知して
  WARN すべきである。辺①が F1-05 で行っているのと同じ扱い）。
  修正はこの作業では行わない（`src/main` 無変更）。
- NTF 仕様としての判定: **対応不要**（帰属が converter の外）。カラム名を大文字化するのは本体
  `TableData`（`toUpperCase`。tsrc L97／L492／L530 で実測）であり、converter は器が返した列名を受け取るだけである。
  **記法はカラム名の大小の扱いに触れていない。** **上の「判断」（衝突を検知して WARN すべき＝仕様として不適切）と
  食い違う。** 衝突検知の提案は改善提案として残るが、大文字化を止める権限は converter に無い。

### YML-11 引用符なしで書いたスカラーは、前後の空白と `#` 以降が黙って落ちる（影響度 中・**検出できない**）

| 入力（`rows` の値） | 中間モデルへ入る結果 | 担保テスト（`YamlFormatReaderInvalidInputTest#`） |
|---|---|---|
| `- V:   pad  `（引用符なし） | **`"pad"`**（前後の空白が消える） | `dropsSurroundingSpacesFromUnquotedScalar` |
| `- V: a #b`（引用符なし） | **`"a"`**（`#` 以降がコメントとして落ちる） | `dropsCommentPartFromUnquotedScalarContainingHash` |
| `- V: "  pad  "` / `- V: "a #b"`（引用符あり） | そのまま入る（D2-11／D2-12。`YamlFormatReaderScalarTest#readsSurroundingWhitespacePreserved` ／ `#readsHashContainingStringAsIs`） | — |

例外にも警告にもならない。

- **この入力はスキーマ上の仕様内である。** `rows` の値の型は `["string","null"]` であり、
  引用符なしのプレーンスカラーも文字列として通る。
- 原因: **YAML の仕様である**（プレーンスカラーは前後の空白を含まず、`#` はコメントを開始する）。
  帰属は converter でも yaml 側でもなく**記法そのもの**にある。
- **スキーマの description は引用を指示している。** `$defs.table_data.properties.rows` は
  「数値・真偽値も必ず文字列（クォート付き）で記述すること」と書いている。**ただしその指示は
  数値・真偽値についてのものであり、空白や `#` を含む文字列には触れていない。**
- 影響: 作成者が書いた値と変換結果が食い違うのに、例外にも警告にもならない。
  Excel から移行してきた作成者は引用符の要否を意識しないため踏みやすい。
- **軸D との関係**: 軸D の 12 ケース（D2-11 空文字・前後空白／D2-12 特殊文字）は**引用符付きの形**で
  ユーザー確定している（steering Decisions）。ここに記録するのは、その裏側にあたる
  引用符なしの形の挙動である。**12 ケース定義は変えていない。**
- 判断: **記録にとどめる**（YAML 仕様どおりであり converter で直す対象ではない）。
  ただし「黙って値が変わる」点は YML-04 と同じ性質のため、課題として残す。
  修正はこの作業では行わない（`src/main` 無変更）。
- NTF 仕様としての判定: **対応不要**（記法どおりの挙動）。`notation:92`「YAMLファイルは YAML 1.2 に準拠する」のとおり、
  プレーンスカラーの前後空白除去と `#` 以降のコメント扱いは YAML 1.2 そのものの規定である。
  上の「判断」（YAML 仕様どおりであり converter で直す対象ではない）と一致する。

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
- NTF 仕様としての判定: **対応不要**（記法に明文が無い）。`NullPointerException` が生じるのは本体
  `DataFileFragment`（`data.getBytes(container.getEncodingFromDirectives())`。tsrc L138）であり、
  **記法は例外の型・メッセージを規定していない**。**上の「判断」（リソース名を添えて包み直すのがあるべき姿）
  とは問いが違う。** 包み直しは XLS-14 と同じく改善提案として残る。

---

## #25 辺④ 軸D（YAML 表現 9 ケース）・軸A〜F の欠け補充で記録した課題

**軸D の 9 ケース（D4-01〜D4-09）はすべて往復できた。** 中間モデル → `YamlFormatWriter#write` →
実 `YamlFormatReader` の順に通し、9 ケースとも元の文字列（`null` は Java `null`）へ復元されることを
実測した（担保テストは `inventory.md` §4.1-2 の軸D 表）。**したがって軸D 由来の課題は無い。**
本節に記録する 2 件はいずれも軸D ではなく、**辺④が「本体スキーマに適合しない YAML」を
黙って書き出す**ことに由来する。

掲載順は本ファイルの原則どおり「検出できないものを上」である。2 件とも
**変換時には検出できず、読み戻しのときに loud に失敗する**（値が黙って変わるものではない）。

### YML-12 スキーマが禁じる形の中間モデルを渡すと、読み戻せない YAML が黙って書き出される（影響度 中・**変換時には検出できない**／読み戻し時に loud・**#25.5 で修正済み**）

`YamlFormatWriter` は本体スキーマ（yaml jar 内 `nablarch/test/ntf-testdata-yaml-schema.json`）を
参照しない。以下の 4 つの形は書き出しに成功するが、生成された `.yaml` はスキーマ違反であり、
`YamlFormatReader`（＝ NTF 本体の読み取り経路）で読めない。

| 入力（中間モデル） | 書き出される YAML | 読み戻し（違反キーワード・位置） | 担保テスト（`YamlFormatWriterModelTest#`） |
|---|---|---|---|
| `FileDataBlock.records` が空 | **#25.5 前**: `records:` キーごと出ない ／ **#25.5 後**: `records: []` が出る | **#25.5 前**: `required` ／ `$.setup_files[0]` ／ **#25.5 後**: 読み戻せる（違反なし） | 記法: `writesEmptyRecordsListForFileBlockWithoutRecords`（旧名 `writesFileBlockWithoutRecordsKeyWhenRecordsAreEmpty`） ／ 読み戻し: `readsBackFileBlockWithEmptyRecords`（旧名 `failsToReadBackFileBlockWithoutRecords`） |
| `MessageDataBlock.records` が空 | **#25.5 前**: `records:` キーごと出ない（`id:` だけになる） ／ **#25.5 後**: 書き出し自体が `IllegalArgumentException` になる | **#25.5 前**: `required` ／ `$.messages[0]` | 番人: `YamlFormatWriterTest#serializeMessage_withoutRecords_rejected`／`#serializeSendSync_withoutRecords_rejected`（旧 `failsToReadBackMessageBlockWithoutRecords` と旧 `serializeMessage_emptyBody_emitsIdOnly` は削除）。辺③側は `XlsFormatWriterTest#rejectsMessageBlockWithoutRecords`／`#rejectsSendSyncMessageBlockWithoutRecords`（旧 `XlsFormatWriterModelTest#writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty` は削除）。境界＝ファイルブロックの 0 件は合法であることは `YamlFormatWriterModelTest#writesEmptyRecordsListForFileBlockWithoutRecords` ／ `XlsFormatWriterModelTest#writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty` が担保 |
| `RecordLayout.fields` が空 | **#25.5 前**: `fields: []` が出る ／ **#25.5 後**: 書き出し自体が `IllegalArgumentException` になる | **#25.5 前**: `minItems` ／ `$.setup_files[0].records[0].fields` | 番人: `YamlFormatWriterTest#serialize_recordWithoutFieldsInFileBlock_rejected`／`#serialize_recordWithoutFieldsInMessageBlock_rejected`（旧 `failsToReadBackRecordWithoutFields` と旧 `serialize_recordWithEmptyFieldsAndRows_emitsEmptyFlowLists` は削除） |
| `FieldDef.type` が `null` | **#25.5 前**: `{name: "c1"}`（`type` を省略）が出る ／ **#25.5 後**: 書き出し自体が `IllegalArgumentException` になる | **#25.5 前**: `required` ／ `$.expected_files[0].records[0].fields[0]` | 番人: `YamlFormatWriterTest#serialize_fieldWithNullTypeInFileBlock_rejected`／`#serialize_fieldWithNullTypeInMessageBlock_rejected`（旧 `failsToReadBackFieldWithoutType` と旧 `serialize_fieldWithNullType_omitsType` は削除。境界＝空文字は弾かないことは `#serialize_fieldWithEmptyType_emitsEmptyType` が担保） |

- 原因（**#25.5 前**）: 直列化は中間モデルの形をそのまま写していた（`emitRecords` は空なら `records:` を出さず、
  `emitFlowList` は空なら `key: []` を出し、`fieldFlow` は `type` が `null` なら書かない。
  **#25.5 後の `fieldFlow` は `type` を常に書く**——`null` は `emitRecords` の番人が弾くため）。
  **#25.5 で `emitRecords` を直し、空なら `records: []` を出すようにした**（1 形目）。
  **メッセージ系は `records.minItems` ＝ 1 で `[]` も通らないため、`emitMessage` が 0 件を
  `IllegalArgumentException` で弾くようにした**（2 形目。これにより `emitRecords` に空が入るのは
  ファイルデータブロック経由だけになったので、`emitEmptyList` の引数は削った）。
  スキーマ側の該当箇所は次のとおりで、**`file_data` と `message_data` で `records` の扱いが違う**
  （2026-08-14 に jar 内の実スキーマを展開して確認した。導出コマンドは下記）。

  | `$defs` | `required` | `records` / `fields` の `minItems` |
  |---|---|---|
  | `file_data` | `path` ／ `type` ／ `records` | `records.minItems` ＝ **0** |
  | `message_data` | `id` ／ `records` | `records.minItems` ＝ **1** |
  | `record_fragment` | `fields` ／ `rows` | `fields.minItems` ＝ **1** |
  | `field_def` | `name` ／ `type` | — |

  ```sh
  unzip -p "$(find ~/.m2 -name 'nablarch-testing-yaml-1.0.0-SNAPSHOT.jar' | head -1)" \
      nablarch/test/ntf-testdata-yaml-schema.json | python3 -c "
  import json,sys
  d=json.load(sys.stdin)
  for k in ['file_data','message_data','record_fragment','field_def']:
      v=d['\$defs'][k]
      print(k, 'required=', v.get('required'))
      for pk,pv in v.get('properties',{}).items():
          if 'minItems' in pv: print('   ', pk, 'minItems=', pv['minItems'])
  "
  ```

  したがって 4 件の性質は同じではない。

  - **1 件目（`FileDataBlock.records` 空）だけが「表現の食い違い」である。**
    `records` は必須だが `minItems` が 0 なので、**空配列 `records: []` と書けば通る**。
    にもかかわらず辺④は空を「キーごと省略」で表していたため落ちていた（**#25.5 で修正済み**）。
  - **2 件目（`MessageDataBlock.records` 空）は表現の問題ではない。**
    `message_data` は `records.minItems` ＝ 1 であり、`records: []` と書いても通らない。
    すなわちスキーマは「レコードを 1 件も持たないメッセージ」という形そのものを認めていない
    （送信系の `expected_request_message_data` ／ `group_message_data` も同じ。**#25.5 で修正済み**）。
  - 3・4 件目（`fields` 空／`type` 省略）も同じく、スキーマが認めない形を辺④が書けてしまうという話である
    （`fields.minItems` ＝ 1 ／ `type` 必須）。
- **到達経路がある（実測・2026-08-14）。** 上 2 件（`records` 空）は**辺①が生成できる**。
  レコードレイアウトを持たないファイルブロック／メッセージブロックを含む `.xlsx` を
  `XlsFormatReader`（本番配線）で読むと `records=0` の中間モデルになる。
  さらに `TestDataConverter.convert(DataFormat.XLS, DataFormat.YAML, in, out)` を通すと、
  `records:` を持たない `.yaml` が生成され、それを読み直すと上表のとおりスキーマ違反で失敗する。
  **すなわち XLS→YAML 変換の成果物が NTF で読めなくなる。**
  再現に使った `.xlsx` は `XlsFormatWriter` で生成したものであり、**Excel で手書きした版面での再現は未確認**である。
  **#25.5 後はどちらも解消した** —— ファイルブロックは `records: []` を書くようになって読み戻せるようになり、
  メッセージブロックは書き出し時に `IllegalArgumentException` で止まる（黙って読めない `.yaml` を作らない）。
  なお辺①がレコード 0 件のメッセージブロックを作れること自体は変わらない
  （`XlsFormatReaderRealFileTest#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook`）。
  そのような `.xlsx` を変換しようとすると、**#25.5 後は変換が loud に失敗する**。
- 下 2 件（`fields` 空／`type` 省略）は辺②では到達不能である（`inventory.md` §2.3。スキーマが弾く）。
  **`fields` 空は辺③でも同じ形が「書けるが読み戻せない `.xlsx`」になる（XLS-22）。同じ中間モデル値の
  辺③版・辺④版であり、#25.5 で両辺に同じ番人を置いて同時に閉じた**（`b9ff38e`）。
  **`type` 省略（4 形目）も同じ扱いにした。**辺①でも到達不能だが（型が欠ける入力は本体パーサが 2 通りの
  機構で弾く。XLS-07 直下の「到達不能」表 C-20）、データ型を持たないフィールド定義は Excel 記法にも
  YAML 記法にも存在しない形であり、両形式が表現できない値を中間モデルだけが保持できていた。
  #25.5 で辺③④の双方に番人を置いて閉じた（辺③側に固有の課題 ID は無い）。
- 影響: 変換は成功し、`.yaml` も生成されるため、変換の時点では気づけない。
  気づくのは NTF がそのテストデータを読むとき（またはこのツールで読み戻すとき）である。
  スキーマ検証のメッセージは違反位置（`$.setup_files[0]`）を示すため、原因の特定自体は難しくない。
- 判断: **仕様として不適切**である。`YamlFormatWriter` の Javadoc は「`YamlFormatReader` と記法対称に
  直列化する」と謳っており、読み戻せない出力はその主張に反する。あるべき姿は 4 件で分かれる。
  - **1 件目のみ「空のコレクションを `records: []` と書く」**（スキーマが受け付ける形が存在するため）。
  - **2・3・4 件目は「書き出し時に弾く」**（スキーマが形そのものを認めていないため、
    どう書いても読み戻せない。辺③の `XlsFormatWriter#appendRecords` が同種の前提崩れを
    送出で弾いているのと同じ思想）。**2・3・4 件目とも #25.5 で実現済み**（残りは無い。2 形目 `04873de`／3 形目 `b9ff38e`／4 形目 `f80c192`）。
- NTF 仕様としての判定: **要対応**（4 形すべて）。`notation:881`「0バイトの空ファイルは、レコード定義を
  持たないファイルデータブロックとして表現する」／`notation:1146`「0バイトの空ファイルを表現するには、`records:` に
  空配列 `[]` を記載する」に反する（`records:` キーごと落としていた）。**#25.5 で 1 形目を修正済み（aec82f2）**。
  `YamlFormatWriter` がレコード 0 件のファイルデータブロックに `records: []` を書き出すようにした。
  **3 形目（`record_fragment.fields` 空）も要対応へ倒し、#25.5 で修正済み**（`b9ff38e`）。
  根拠は XLS-22 と同じ「中間モデルの契約の穴」である——フィールド 0 件のレコードレイアウトは
  Excel 記法にも YAML 記法にも存在しない形であり、両形式が表現できない値を中間モデルだけが
  保持できていた。`YamlFormatWriter#emitRecords` が `IllegalArgumentException` で弾くようにした。
  **4 形目（`field_def.type` 省略）も要対応へ倒し、#25.5 で修正済み**（`f80c192`）。根拠は
  記法とスキーマの明文である——`notation:883`
  「固定長ファイルでは、フィールド名称・データ型・フィールド長の3リストが同サイズで必須であり…
  可変長ファイルでは、フィールド名称・データ型の2リストが同サイズで必須であり」／`notation:888`
  「- フィールド名称リストまたはデータ型リストが未指定または空である」
  （`notation:885` が「ファイルデータの記述時にエラーとなるのは、以下のようなケースである。」）、
  および `$defs.field_def.required` ＝ `["name", "type"]`。`FieldDef` の Javadoc に
  「`type` は必須（`null` 不可）」の契約を明記し、`XlsFormatWriter#appendRecords` ／
  `YamlFormatWriter#emitRecords` が `null` を `IllegalArgumentException` で弾くようにした
  （弾くのは `null` のみ。空文字は弾かない）。
  **2 形目（`message_data.records` 空）も要対応へ倒し、#25.5 で修正済み**（`04873de`。ユーザー確定・2026-08-18）。
  根拠は記法とスキーマの明文である。

  **以下の行番号は本書の基準 `30a8271` 時点のもの**であり、次のコマンドで確かめられる。

  ```sh
  cd /home/tie303177/work/nablarch/nablarch-document
  for n in 881 1109 1146 1158 1257; do
    printf '%s: ' "$n"
    git show 30a8271:ja/development_tools/testing_framework/implementation/testdata_notation.rst | sed -n "${n}p"
  done
  ```

  - **電文についてレコード 0 件の記法は明文が無い。** 電文が存在しない場合の記法は
    `notation:1257`「応答不要メッセージ受信では…`expectedMessages` のデータブロックを
    記述する必要はない」＝**ブロックごと省略する**である。
  - **0 バイト空ファイル特例は電文に及ばない。** 同特例は `notation:881`「0バイトの空ファイルは、
    レコード定義を持たないファイルデータブロックとして表現する」／`:1109`「0バイトの空ファイルを
    表現するには、ディレクティブのみを記述してレコード定義を省略する。」／`:1146`
    「`setup_files`・`expected_files` の各エントリには `path`・`type`・`records` の3キーが必須であり、
    いずれかを省略するとエラーになる。0バイトの空ファイルを表現するには、`records:` に空配列 `[]` を
    記載する。」と、いずれも**ファイルに限定**して書かれている。
    `notation:1158`「フレームワーク制御ヘッダ以降のメッセージボディは、フィールド名称・データ型・
    フィールド長・データという、前述のファイルデータと同じ構成を持つ」は直前の列挙のとおり
    **カラム構成のみ**を指しており、空ファイル特例まで及ぶ読み方はしない。
  - スキーマは `$defs.message_data` ／ `$defs.expected_request_message_data` ／
    `$defs.group_message_data` が `required` ＝ `["id","records"]` かつ `records.minItems` ＝ **1**、
    `$defs.file_data` だけが `records.minItems` ＝ **0** である。**この非対称は不整合ではなく、
    明文の有無に対応した意図的なものと扱う**（スキーマは変更対象ではない）。

  `MessageDataBlock` の Javadoc に「`records` は 1 件以上（0 件不可）」の契約を明記し、
  `XlsFormatWriter#layoutMessage` ／ `YamlFormatWriter#emitMessage` が 0 件を
  `IllegalArgumentException` で弾くようにした。**番人は共通の `appendRecords` ／ `emitRecords` には
  置かない**——ファイルデータブロックの 0 件は 0 バイト空ファイルを表す合法な形であり、
  `emitFile` は `records: []` を出す正当な経路だからである（この番人を置いたことで `emitRecords` に
  空が入るのはファイル経路だけになり、`emitEmptyList` 引数は到達不能な分岐ごと削った）。

  4 形すべての修正はいずれも本リポジトリ内で完結した（1・3 形目は `YamlFormatWriter` のみ、
  2 形目は `MessageDataBlock` の Javadoc と `XlsFormatWriter` ／ `YamlFormatWriter`、
  4 形目は `FieldDef` の Javadoc と `XlsFormatWriter` ／ `YamlFormatWriter`）。

### YML-13 折り返しの起きるキーを書き出すと、YAML として読めないファイルになる（影響度 低・**変換時には検出できない**／読み戻し時に loud）

| 入力（中間モデル） | 書き出される YAML | 読み戻し | 担保テスト（`YamlFormatWriterScalarTest#`） |
|---|---|---|---|
| エスケープを要する文字（改行など）を含み、かつ 80 桁を超える**カラム名**（`directives` ／ `fw_header` のキーでも同じ） | ダブルクォートスカラーが行末の `\` で折り返され、**キーが 2 行にまたがる** | `IllegalStateException: Failed to parse YAML file: …`（パースで止まり、スキーマ検証には到達しない） | 記法: `foldsLongEscapedKeyWithBackslashContinuation` ／ 読み戻し: `failsToReadBackFoldedKey` |
| 同じ形の**値**（`rows` の値） | 同じく折り返されるが、読み戻しは成功する | 元の文字列へ復元される | `foldsLongEscapedValueWithBackslashContinuation` ／ `restoresFoldedLongEscapedValueThroughRealReader` |

- 原因: `YamlFormatWriter#q` は値を単独で snakeyaml-engine の `Dump#dumpToString` に渡す。
  同エンジンは出力幅（既定 80 桁。`DumpSettings.builder().build().getWidth()` → **80**。実測）を
  超えると、エスケープを挟んだ位置でダブルクォートスカラーを
  行末の `\` で折り返す。`key(String)` はクォートが要るキーに同じ `q` を使うため、キーも折り返される。
  **YAML の simple key は 1 行に収まらなければならない**ため、折り返されたキーはパースできない。
  折り返しは長さだけでは起きない（エスケープを要する文字を 1 つも含まない 300 文字の値は 1 行のまま。実測）。
- **折り返しの継続行の字下げは半角空白 2 個で固定であり、差し込み先のインデントに揃わない。**
  `q` は差し込み先の深さを知らないためである。値側ではこれでも復元できる
  （snakeyaml-engine と PyYAML の 2 実装で同じ文字列に復元されることを実測。**他の実装は未確認**）。
- 影響: 到達には「キーにエスケープ対象文字を含む」かつ「80 桁を超える」の両方が要るため、
  実データで踏む見込みは小さい。踏んだ場合は**そのファイル全体が読めなくなる**（ブロック単位ではない）。
  例外メッセージはファイルパスを示すが、原因がキーの折り返しであることは示さない。
- 判断: 記録に留める（影響度 低）。あるべき姿は、キーに使う `q` では折り返しを禁じる
  （`DumpSettings#setWidth` を十分大きく取る、またはキー専用の整形にする）ことである。
  修正はこの作業では行わない（`src/main` 無変更）。
- NTF 仕様としての判定: **対応不要**（記法に明文が無い）。**記法はキーに使える文字種・長さを規定していない**。
  到達には「キーにエスケープ対象文字を含む」かつ「80 桁を超える」の両方が要り、踏んだ場合は読み戻しが
  loud に失敗する（黙ってデータが変わることはない）。上の「判断」（記録に留める）と一致する。

### 課題としないと判断した観測結果（#25）

| 観測 | 判断 |
|---|---|
| 軸D の 9 ケース（`"100"` ／ `"true"` ／ `"null"` ／ `null` ／ `""` ／ `"007"` ／改行含む ／ `"2026-08-07"` ／コロン・ハイフン・`#` 含む）がすべて往復する | 妥当。全値ダブルクォート＋`null` だけアンクォートという方針が効いている（`inventory.md` §4.1-2 の軸D 表） |
| 改行を含む値がブロックスカラー（`\|` ／ `>`）ではなく 1 行の `"…\n…"` になる | 妥当（読み戻すと改行を含んだまま戻る。担保: `YamlFormatWriterScalarTest#writesNewlineContainingStringAsEscapedSingleLineScalar` ／ `#restoresNewlineContainingStringThroughRealReader`） |
| セクション 0 件のコンテナから、ファイルも出力先ディレクトリも作られない | 妥当（記録のみ）。辺③は同じ入力から**シート 0 枚のブックを書き出す**（**XLS-23**）ため非対称だが、辺④は壊れた成果物を残さないぶん筋がよい。担保: `YamlFormatWriterModelTest#writesNothingWhenContainerHasNoSections` |
| 出力先が存在しないと、例外にならずディレクトリが作られる（F4-01） | 妥当（辺③の `XlsFormatWriterInvalidOutputTest#createsMissingOutputDirectoriesAndWritesWorkbook` と同じ挙動。担保: `YamlFormatWriterInvalidOutputTest#createsMissingOutputDirectoriesAndWritesYaml`） |
| 小文字のテーブル名・カラム名が読み戻しで大文字化され、往復で元に戻らない | 既知（**YML-10** ／ 大文字化そのものは「課題としないと判断した観測結果（#24）」）。辺④側の現れ方を `YamlFormatWriterModelTest#uppercasesTableAndColumnNamesWhenReadBack` で固定した |
| `field-separator` にリテラルのタブを持つブロックが、忠実に `"\t"` と書かれるのに読み戻せない | 既知（**YML-08**。辺②側でディレクティブ値が `trim()` されるため）。辺④側の現れ方＝**タブ区切りの可変長ファイルは往復できない**ことを `YamlFormatWriterModelTest#failsToReadBackLiteralTabFieldSeparator` で固定した |
| `group_id` に YAML 特殊文字（コロン）を含めても往復する | 妥当（値としてクォートされるため。プローブ実測。担保テストは置いていない —— 軸要素ではなく、既存の `YamlFormatWriterTest#serialize_quotesKeyContainingSpecialChars` がキー側のクォートを、`#roundTrip_sendSync_preservesGroupIdAndNoField` が `group_id` の往復を通しているため） |

### 未確認（#25）

- **折り返しを含む出力 YAML を、snakeyaml-engine と PyYAML 以外の実装が同じ文字列へ復元するかは未確認である**（YML-13）。
- **YML-12 の到達経路を Excel で手書きした版面で再現できるかは未確認である。** 実測に使った `.xlsx` は
  `XlsFormatWriter` が生成したものである（読み取り側は本番配線の `XlsFormatReader` を通している）。
- **軸D の 9 ケースのうち、テストで 1 経路（`setup_tables` の `rows`）でしか固定していないのは 7 ケースである。**
  `"true"`（D4-02）と `"2026-08-07"`（D4-08）は #25 のレビュー対応でレコード断片（`records[].rows`）経路と
  `directives` 経路にも埋め込み、記法を固定した（`YamlFormatWriterModelTest#record()` ／
  `#writesEmptyRecordsListForFileBlockWithoutRecords`（#25.5 で改名。旧名
  `writesFileBlockWithoutRecordsKeyWhenRecordsAreEmpty`）。当初版は「レコード断片経路は
  プローブで確認したがテストにしていない」と開示していた箇所である）。
  残る 7 ケースがレコード断片経路でも同じ記法（フロー list の中のダブルクォート）で書かれ、
  同じく往復することはプローブで確認したがテストにはしていない。
  **LIST_MAP 経路（`list_maps`）は 9 ケースとも観測していない。**

---

## #25.5 不具合修正で記録した課題

### XLS-25 辺③④の番人が送出する例外型が `ConverterException` に揃っておらず、Maven 実行で利用者に生スタックトレースが残る（影響度 低・loud に失敗するため検出できる）

- 観測（2026-08-14・#25.5 のレビュー指摘）: `XlsFormatWriter` が入力を弾く番人は
  `IllegalArgumentException`／`IllegalStateException` を送出する。
  一方 `ConverterMojo` が包むのは `catch (ConverterException | UncheckedIOException)` の 2 種だけである
  （`src/main/java/nablarch/test/tool/converter/ConverterMojo.java:99`）。
  したがってこれらは `MojoExecutionException` に包まれず、Maven 実行では利用者に生のスタックトレースが出る。
- 該当箇所（実測。`grep -n "throw new Illegal" src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java`）:
  - `requireValidSheetNameLength` の 2 本（シート名が null ／ 31 文字超）。**#25.5 の XLS-16 修正で足したもの**
  - `appendRecords` の 1 本（2 レコード目以降のレコード種別が空）。**#25.5 より前から在る**
  - `layout` の 1 本（未知のブロック実装）。到達不能（`inventory.md` §3.1-3 の未到達分岐表）
- 影響: シート名の長さは**利用者が書いたテストデータ**に由来する入力ミスであり、
  本来は「どのシート名がなぜ駄目か」を Maven のエラーとして示すべきものである。
  ただし変換は中止されるため、**壊れた成果物が黙って残ることはない**。
- 判断: 仕様として不適切。番人の例外型は `ConverterException` に揃えるべきである。
  **ただし #25.5 では直さない。** XLS-16 で足した 2 本だけを `ConverterException` にすると、
  同じ性質の既存の番人（`appendRecords` の `IllegalStateException`）が取り残され、
  **同一ファイルの中に新しい不揃いを作る**。揃えるなら辺③④の番人を一度に扱うべきで、
  それは #25.5 のスコープ（ユーザー確定の 5 件）に含まれない。
- NTF 仕様としての判定: **対応不要**（記法に明文が無い）。
  記法（`testdata_notation.rst`）は変換ツールが送出する例外型を規定していない。
  上の「判断」（仕様として不適切）とは食い違う。**改善提案であって記法違反ではない。**
- 次タスクの候補: **「辺③④の番人の例外型を `ConverterException` に揃える」**。
  対象は上記 4 本と、辺④（`YamlFormatWriter`）の同種の番人である。

### XLS-26 本体 `MessageParser` は本文レコード 0 件の電文を受け付ける（**本作業の対象外・記録のみ**／モジュール: `nablarch-testing` 本体）

**本項はモジュールが違う。** 記録するのは本体 `nablarch-testing` の挙動であり、
**converter（本リポジトリ）の 4 辺の課題ではない**。凡例の影響度 4 段は「変換結果が入力と一致するか」で
定義されるため本項には当てはまらないので、影響度は付けない。

- 事実（実測）: 本体 `nablarch.test.core.reader.MessageParser` は、**本文レコード定義を 1 行も持たない
  `MESSAGE` ブロック**（識別行 ＋ FW 制御ヘッダ行だけの版面）を**例外なく受け付け**、`MessagePool`
  （`RequestTestingMessagePool`）を返す。`MessageParser#getResult` が `null` を返すのは委譲先
  `FixedLengthFileParser` の結果が空のときだけであり（tsrc L126-133）、レコード定義が 0 件でも
  `FixedLengthFile` は 1 件できるため、この版面は `null` にならない。
- 証拠（既存テスト。新規プローブは書いていない）:
  - `src/test/java/nablarch/test/core/reader/TestCoreReaderAdapterTest.java#readMessageReturnsRawMessagePool`
    — `MESSAGE=msg1` ＋ `requestId`／`userId` の 2 行だけの版面を渡し、例外にならず
    `assertNotNull(message.getBody())` まで通る。`TestCoreReaderAdapter#readMessage` は本体
    `MessageParser` を直接 `new` している
    （`src/main/java/nablarch/test/core/reader/TestCoreReaderAdapter.java:179`）。
  - `src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderRealFileTest.java#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook`
    — 実 `.xlsx` を `XlsFormatReader`（本番配線）で読み、`getRecords()` が空リストになることを実測する。
    この経路も `XlsFormatReader#readMessageBlock`（L226-227）→ `TestCoreReaderAdapter#readMessage` →
    本体 `MessageParser` である。
- なぜ課題か: **記法（`30a8271` 時点）には電文のレコード 0 件を表す書き方の明文が無い。**
  電文が存在しない場合は `notation:1257`「応答不要メッセージ受信では…`expectedMessages` の
  データブロックを記述する必要はない」のとおり**データブロックごと省略する**のが記法である。
  すなわち**記法に無い形を本体が黙って通している**。
- **スコープの限定（重要）**: 本体 `FixedLengthFileParser` が**ファイル**のレコード 0 件を受け付けるのは
  **不具合ではない**。0 バイトの空ファイルには `notation:881`／`:1109`／`:1146` に明文がある。
  **本項は電文（`MessageParser`）に限定した記録である。**
- 関連: converter 側は `04873de`（YML-12 2形目）で辺③④に番人を置いたため、この形の中間モデルは
  書き出しで `IllegalArgumentException` になる。**ただし辺①は実 `.xlsx` から今もこの形を読み取れる**
  （上記 `readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook`）。したがって
  **そのような `.xlsx` を変換しようとすると loud に失敗する**（黙って読めない成果物は作らない）。
  この到達経路は塞いでいない。
- 判断: 本体側の記法との食い違いとして記録に値する。**converter 側で直せるものではない**
  （読み取りを本体パーサに委ねている以上、変換ツールが本体の受け入れ範囲を狭めるのは筋が違う）。
- NTF 仕様としての判定: **本作業の対象外**（記録のみ。ユーザー確定・2026-08-18）。
  帰属は `nablarch-testing` 本体であり、#25.5 の対象（converter の `src/main`）に含まれない。
  **要対応／対応不要の二値のいずれでもない**ため、冒頭の内訳（要対応 7 件／対応不要 29 件）には数えない。

---

## #25.5 の記録

### `@Ignore` 一覧（`cdbcf63`）の導出方法と、そこから漏れた 2 件

`cdbcf63` で `@Ignore` を付けた 10 本は、**本ファイルの各課題の「担保テスト」欄からの機械的な導出**である。
したがって**担保テスト欄に載っていないテストは漏れる**。実際に 2 件漏れており、いずれも修正コミットで
赤になって顕在化し、その場で期待値を直した。

| 漏れたテスト | 課題 | 顕在化したコミット |
|---|---|---|
| `YamlFormatReaderTest#readSendSync_entryWithoutGroupId_isDropped` | YML-02 | `36e94a4`（コミットメッセージに漏れとして記載済み） |
| `YamlFormatReaderInvalidInputTest#readsFieldSeparatorWrittenAsEscapedTabNotation` | YML-08 | `6c8d90e`（「期待値更新」とだけ書いており、漏れであることを書いていなかった。本節が記録） |

**新たな漏れは無いことを QA が全数実験で確認済み**（`git checkout 8c327d0 -- src/test` を HEAD の
`src/main` へ重ねて実行し、失敗 13 件＝置き換えた 13 本と一致）。

### 残置している「緑の嘘」

**残置は 0 本になった（2026-08-18）。** 「緑の嘘」とは、`mvn test` で緑になるが**固定しているのは
「スキーマや本体パーサが認めない形を書けてしまう」という現状の記録であって NTF の仕様ではない**テストを指す
（実行結果からは「仕様どおり」と区別がつかない）。

**4 本 → 2 本 → 1 本 → 0 本へ減った。** XLS-22 と YML-12 の 3 形目を要対応へ倒して修正したときに
2 本（`XlsFormatWriterModelTest#failsToReadBackRecordWithoutFields`／
`YamlFormatWriterModelTest#failsToReadBackRecordWithoutFields`）を、YML-12 の 4 形目を修正したときに
1 本（`YamlFormatWriterModelTest#failsToReadBackFieldWithoutType`）を、YML-12 の 2 形目を修正したときに
1 本（`YamlFormatWriterModelTest#failsToReadBackMessageBlockWithoutRecords`）を削除し、
いずれも番人テストへ置き換えた。
**#25.5 で直したのは YML-12 の 4 形すべて（1 形目＝ファイルブロックの `records` 欠落／
2 形目＝メッセージブロックの `records` 欠落／3 形目＝`fields` 欠落／4 形目＝`FieldDef.type` 欠落）、
および XLS-22 である。**

残置が 0 本であることは、次のコマンドで確かめられる（削除した 4 本が `src/test` に存在しないことを示す）。

```
$ grep -rn 'failsToReadBackRecordWithoutFields\|failsToReadBackFieldWithoutType\|failsToReadBackMessageBlockWithoutRecords' src/test --include=*.java | wc -l
0
```

`src/test` に残る `failsToReadBack…` は 2 本だけで、いずれも「緑の嘘」ではない
（`grep -rn 'failsToReadBack' src/test --include=*.java | grep 'public void'` で確かめられる）。
`YamlFormatWriterScalarTest#failsToReadBackFoldedKey` は **YML-13**（判定は対応不要）の実挙動の記録、
`YamlFormatWriterModelTest#failsToReadBackLiteralTabFieldSeparator` は **YML-08** の実挙動の記録である。
