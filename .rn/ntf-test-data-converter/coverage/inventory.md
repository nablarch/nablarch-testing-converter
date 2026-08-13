# 既存テスト 4 辺分の軸棚卸し（task #18）

4 つの変換辺の既存テスト 126 件が、軸A〜F のどの要素を担保しているかを 1 件ずつ棚卸しした結果。
以降のタスク #19〜#25 は、本書「未担保一覧」に挙がった要素だけを埋める。
本書は #26（カバレッジ計測と未到達分岐の列挙）の入力であり、#27 で
`.rn/ntf-test-data-converter/coverage/axis-matrix.md`（各要素に担保テストメソッド名を記した 4 辺ぶんの
軸×要素対応表）へ発展させる土台でもある。

- 作成日: 2026-08-12
- 対象コミット: `c1d2d21`（棚卸し実施時 HEAD）
- 判定方法: 全テストメソッドのテスト本文を読み、実際にアサートしている対象のみを「担保」とした。
  推測で埋めていない。アサートが間接的・副次的なものは 🔺 で区別した。

> **#19〜#23 による更新について（最終更新 2026-08-13）**
>
> 本書は #18 時点の棚卸し結果である。その後の各タスクが実ファイルを経路に含むテストを追加し、
> **辺①（§1）と辺③（§3）の未担保状況が変わった**。本書では #18 時点の記述を消さずに
> 次の箇所へ反映してある。
>
> **辺①（#19 辺① 軸D／#20 辺① 軸A・B・C／#21 辺① 軸E・軸F。2026-08-12）**
>
> - **§1.2-2（新設）** — #19／#20／#21 が追加したテストクラス（`XlsFormatReaderCellTypeTest` /
>   `XlsReferenceFixtureTest` / `XlsFormatReaderRealFileTest` / `XlsFormatReaderInvalidInputTest`）の
>   担保を軸要素別に記す。
>   §1.1／§1.2 は「`XlsFormatReaderTest` 33 件」を対象とした #18 時点の事実として**そのまま残す**。
> - **§1.3** — 未担保一覧を #19／#20／#21 の実測結果に合わせて更新した。分類を変更した行には
>   根拠（`coverage/issues.md` の課題 ID）を併記してある。
>
> **辺③（#22 辺③ 軸D・軸F。2026-08-13）**
>
> - **§3.1-2（新設）** — #22 が追加したテストクラス（`XlsFormatWriterCellTypeTest` /
>   `XlsFormatWriterInvalidOutputTest`）の担保を軸要素別に記す。
>   §3.1 は「`XlsFormatWriterTest` 40 件」を対象とした #18 時点の事実として**そのまま残す**。
> - **§3.2** — 軸D 表・軸F 表に「#22 後」列を足し、`getCellType()` の件数に関する記述を
>   #18 時点のスナップショットと現時点の実測とに書き分けた。軸A・軸B・軸C・軸E の各表は #18 時点のままである。
> - **§3.3** — 辺③ 未担保一覧を #22 の実測結果に合わせて更新した。
>
> **辺③（#23 辺③ 軸A・B・C・E の欠け補充。2026-08-13）**
>
> - **§3.1-3（新設）** — #23 が追加したテストクラス（`XlsFormatWriterModelTest`）の担保を軸要素別に記す。
>   §3.1（`XlsFormatWriterTest` 40 件）と §3.1-2（#22 の 2 クラス）は**そのまま残す**。
>   `XlsFormatWriter` の JaCoCo 実測（未到達 3 箇所）も本節末尾に置いた。
> - **§3.2** — 軸A 表・軸C 表・軸E 表を #23 の実測結果に合わせて更新した（見出しの件数も直した）。
>   軸B 表は #18 時点で ✅ 4/4 のため変更なし。軸D 表・軸F 表は #22 の記述のままである。
> - **§3.3** — 辺③ 未担保一覧に「#23 後の状態」列を足し、**要追加 0 ／ 担保済み 26 ／ 対象外 1** に更新した。
> - **§0.8-6 / §0.8-7 / §5.2 / §5.3** — 辺③の C-02「sections 空」と A-01 `DEFAULT` が
>   #23 で担保済みになったことを追記した（§5.3 の未解決 3 は解決済みへ移した）。
>
> **上記以外（§2・§4・§5.1）は #18 時点のままである**（§5.1 の未担保件数も §1.3／§3.3 の更新を
> 反映していない。4 辺を同じ基準で比べるため、あえて #18 基準を保っている）。
> **§5.2 だけは §1.2-2 の #20 実績を反映した現時点ビューである。**
>
> **§0（前提の実測）は原則として #18 時点のスナップショットである。** その後の変化は
> 各項の中に日付つきで追記してある（§0.8-4 の `getCellType()` 件数など）。

### 判定基準

- **軸A**: 「その `DataType` のブロックが生成されることをアサートしている」ことを担保とし、
  `getDataType()` の直接アサートとは区別する。`XlsFormatReaderTest` で `getDataType()` を参照するのは
  L281 / L542 / L578 / L641 の 4 箇所のみだが、他のテストもデータタイプ名を含むマーカー行を入力に与え、
  そこから生成されたブロックの型・内容をアサートしている。よって軸A の ✅ は 10/14 になる。
  なお軸C の C-05（`TestDataBlock.dataType`）は `getDataType()` を直接アサートしているテストにのみ ✅ を
  付けており、軸A の ✅ とは別基準である（例: `readMapsTableBlockPreservingRawValues` は軸A A-02 は ✅ だが
  軸C の C-05 は付けていない）。
- **軸C**: 省略可能フィールドは「値あり」「省略」、空許容コレクションは「非空」「空」の双方を別々に評価する。
- **軸D・F**: 辺ごとに要素の定義が異なる（§0.5 / §0.7）。
- **軸E**: 観点 × 多重度（0 件／1 件／複数件）の組ごとに評価する。

## 凡例

| 印 | 意味 |
|---|---|
| ✅ | 担保あり（その軸要素を直接アサートしている） |
| 🔺 | 弱い担保（間接的・副次的にしか通っていない。理由は同じセルに併記する） |
| ❌ | 未担保 |
| ※ | 補足注（軸要素 ID を伴わない、そのテスト固有の観点のメモ） |
| — | 該当なし（そのテストはその軸を通さない／その要素に担保テストが 1 件もない） |
| n/a | その要素に「省略」「空」という状態が存在しない（必須スカラー・2 値の列挙型など） |
| **太字** | その辺でその要素を通す唯一の担保 |

用語:

- **器**: `YamlFormatReader` が `TestCoreReaderAdapter` から受け取る中間モデルの骨格。YAML の原文（生 Map）と
  対で保持し、両者の件数・構造が一致することを前提に組み立てる。
- **版面**: `XlsFormatWriter` が生成する Excel シート上の行・列の配置。値そのものではなく、
  どの行に何が出るか（行の有無・位置）をアサートしているものを指す。

内部参照は `§0.4` / `§0.8-5` のように節番号で書く。

## 軸の一覧

| 軸 | 正式名 | 要素の定義 |
|---|---|---|
| A | データタイプ | `DataType` 14 種（§0.2） |
| B | ブロック実装 | `TestDataBlock` sealed 階層の具象 4 種（§0.3） |
| C | 中間モデル全フィールド | 中間モデル 8 クラスの全 21 フィールド（§0.4） |
| D | 値の表現 | 辺ごとに定義が異なる（§0.5） |
| E | 多重度 | 4 観点 × 0 件／1 件／複数件（§0.6） |
| F | 異常系 | 辺ごとに定義が異なる（§0.7） |

## 目次

- [0. 前提の実測](#s0)
- [1. 辺① Excel→中間モデル（`XlsFormatReaderTest` 33 件）](#s1) — [1.3 辺① 未担保一覧](#s1-3)
- [2. 辺② YAML→中間モデル（`YamlFormatReaderTest` 20 件）](#s2) — [2.3 辺② 未担保一覧](#s2-3)
- [3. 辺③ 中間モデル→Excel（`XlsFormatWriterTest` 40 件）](#s3) — [3.3 辺③ 未担保一覧](#s3-3)
- [4. 辺④ 中間モデル→YAML（`YamlFormatWriterTest` 33 件）](#s4) — [4.3 辺④ 未担保一覧](#s4-3)
- [5. 全体サマリ](#s5) — [5.1 未担保件数（辺 × 軸）](#s5-1) / [5.3 コーディネータに判断を仰ぎたい点](#s5-3)

---

<a id="s0"></a>

## 0. 前提の実測

### 0.1 テストメソッド件数（実測と steering 想定の突き合わせ）

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
for f in src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderTest.java \
         src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderTest.java \
         src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterTest.java \
         src/test/java/nablarch/test/tool/converter/yaml/YamlFormatWriterTest.java \
         src/test/java/nablarch/test/tool/converter/RoundTripTest.java; do
  printf "%-30s %s @Test  %s lines\n" "$(basename $f)" "$(grep -c '@Test' $f)" "$(wc -l < $f)"
done
```

```
XlsFormatReaderTest.java       33 @Test  1179 lines
YamlFormatReaderTest.java      20 @Test  608 lines
XlsFormatWriterTest.java       40 @Test  1080 lines
YamlFormatWriterTest.java      33 @Test  724 lines
RoundTripTest.java             30 @Test  882 lines
```

`RoundTripTest`（30 件）は 4 辺いずれの担当クラスのテストでもないため上の 126 件には含まれないが、
4 辺すべてを実ファイル経由で駆動するため §0.8-8 で扱う。

| 辺 | クラス | steering 想定 | 実測 | 差異 |
|---|---|---|---|---|
| 辺① Excel→中間モデル | `XlsFormatReaderTest` | 33 | **33** | なし |
| 辺② YAML→中間モデル | `YamlFormatReaderTest` | 20 | **20** | なし |
| 辺③ 中間モデル→Excel | `XlsFormatWriterTest` | 40 | **40** | なし |
| 辺④ 中間モデル→YAML | `YamlFormatWriterTest` | 33 | **33** | なし |
| 合計 | | 126 | **126** | なし |

### 0.2 軸A: `DataType` 実定義との突き合わせ

実定義: `/home/tie303177/work/nablarch/nablarch-testing/src/main/java/nablarch/test/core/reader/DataType.java`
（converter リポジトリには存在せず、依存の `nablarch-testing` 本体で定義されている）

| # | 定数名 | `getType()` | `getName()`（記法名） | 定義行 |
|---|---|---|---|---|
| A-01 | `DEFAULT` | 0 | `DEFAULT` | L11 |
| A-02 | `SETUP_TABLE_DATA` | 1 | `SETUP_TABLE` | L14 |
| A-03 | `EXPECTED_TABLE_DATA` | 2 | `EXPECTED_TABLE` | L17 |
| A-04 | `EXPECTED_COMPLETED` | 4 | `EXPECTED_COMPLETE_TABLE` | L23 |
| A-05 | `LIST_MAP` | 3 | `LIST_MAP` | L29 |
| A-06 | `SETUP_FIXED` | 5 | `SETUP_FIXED` | L32 |
| A-07 | `EXPECTED_FIXED` | 6 | `EXPECTED_FIXED` | L35 |
| A-08 | `SETUP_VARIABLE` | 7 | `SETUP_VARIABLE` | L38 |
| A-09 | `EXPECTED_VARIABLE` | 8 | `EXPECTED_VARIABLE` | L41 |
| A-10 | `MESSAGE` | 9 | `MESSAGE` | L44 |
| A-11 | `EXPECTED_REQUEST_HEADER_MESSAGES` | 10 | 同名 | L47 |
| A-12 | `EXPECTED_REQUEST_BODY_MESSAGES` | 11 | 同名 | L50 |
| A-13 | `RESPONSE_HEADER_MESSAGES` | 12 | 同名 | L53 |
| A-14 | `RESPONSE_BODY_MESSAGES` | 13 | 同名 | L56 |

**突き合わせ結果: 14 種（`DEFAULT` ＋ 13）で steering の記述と一致。差異なし。**

### 0.3 軸B: `TestDataBlock` sealed 階層

`TestDataBlock`（abstract sealed, L16-17）が permit するのは `ColumnRowDataBlock` / `FileDataBlock` / `MessageDataBlock`。
`ColumnRowDataBlock`（abstract sealed, L19-20）がさらに `TableDataBlock` / `ListMapBlock` を permit する。
したがって **具象（final）実装は 4 種**であり、steering の「4種」と一致する。

| # | 具象クラス | 直接の親 | 保持する `DataType` |
|---|---|---|---|
| B-1 | `TableDataBlock` | `ColumnRowDataBlock` | SETUP_TABLE_DATA / EXPECTED_TABLE_DATA / EXPECTED_COMPLETED |
| B-2 | `ListMapBlock` | `ColumnRowDataBlock` | LIST_MAP（コンストラクタで固定） |
| B-3 | `FileDataBlock` | `TestDataBlock` | SETUP_FIXED / EXPECTED_FIXED / SETUP_VARIABLE / EXPECTED_VARIABLE |
| B-4 | `MessageDataBlock` | `TestDataBlock` | MESSAGE / 送信系 4 種 |

### 0.4 軸C: 中間モデル全フィールド（実クラスから読み取り）

全 **21 フィールド**。`src/main/java/nablarch/test/tool/converter/model/` の各クラスの `private final` 宣言をすべて列挙した。

| # | クラス | フィールド | 型 | 省略区分 | 省略時の表現（Javadoc 根拠） |
|---|---|---|---|---|---|
| C-01 | `TestDataContainer` | `name` | String | 必須 | — |
| C-02 | `TestDataContainer` | `sections` | List | 空許容 | 空リスト |
| C-03 | `TestDataSection` | `name` | String | 必須 | — |
| C-04 | `TestDataSection` | `blocks` | List | 空許容 | 空リスト |
| C-05 | `TestDataBlock` | `dataType` | DataType | 必須 | — |
| C-06 | `TestDataBlock` | `groupId` | String | **省略可** | `""`（TestDataBlock L27/L41「省略時は空文字」） |
| C-07 | `TestDataBlock` | `identifier` | String | 必須 | — |
| C-08 | `ColumnRowDataBlock` | `columnNames` | List | 空許容 | 空リスト |
| C-09 | `ColumnRowDataBlock` | `rows` | List<List> | 空許容 | 空リスト |
| C-10 | `FileDataBlock` | `fileType` | FileType | 必須（2値） | FIXED / VARIABLE（FileDataBlock L25） |
| C-11 | `FileDataBlock` | `directives` | Map | 空許容 | 空 Map |
| C-12 | `FileDataBlock` | `records` | List | 空許容 | 空リスト |
| C-13 | `MessageDataBlock` | `directives` | Map | 空許容 | 空 Map |
| C-14 | `MessageDataBlock` | `fwHeaderFields` | Map | 空許容 | 空 Map（MessageDataBlock L40「FW ヘッダを読まない経路では空 Map」） |
| C-15 | `MessageDataBlock` | `records` | List | 空許容 | 空リスト |
| C-16 | `RecordLayout` | `recordType` | String | **省略可** | `null`（RecordLayout L26/L36「省略時は null」） |
| C-17 | `RecordLayout` | `fields` | List | 空許容 | 空リスト |
| C-18 | `RecordLayout` | `rows` | List<List> | 空許容 | 空リスト |
| C-19 | `FieldDef` | `name` | String | 必須 | — |
| C-20 | `FieldDef` | `type` | String | **省略可** | `null`（FieldDef L24/L38「省略時は null」） |
| C-21 | `FieldDef` | `length` | String | **省略可** | `null`（FieldDef L25/L43「省略時は null」） |

内訳: 必須スカラー 6 件（C-01, C-03, C-05, C-07, C-10, C-19）／
**省略可能フィールド 4 件**（C-06, C-16, C-20, C-21 — Javadoc に「省略時は…」と明記。
C-06 は TestDataBlock L27/L41、C-16 は RecordLayout L26/L36、C-20 は FieldDef L24/L38、C-21 は FieldDef L25/L43）／
空許容コレクション 11 件（C-02, C-04, C-08, C-09, C-11, C-12, C-13, C-14, C-15, C-17, C-18）。

**steering との差異（コーディネータ判断を仰ぐ点）**: steering #20 の Steps は
「`groupId` / `identifier` / `fileType` / `directives` / `fwHeaderFields` / `recordType` / `FieldDef.type` / `FieldDef.length` は
『値あり』『省略』の双方を通す」としているが、実定義上 `identifier`（C-07）と `fileType`（C-10）には
「省略」の表現が存在しない（`identifier` は必須スカラー、`fileType` は FIXED/VARIABLE の 2 値）。
本棚卸しでは実定義を正とし、`fileType` は「FIXED / VARIABLE 双方」、`identifier` は「値あり 1 通り」として扱う。
`directives` / `fwHeaderFields` は「非空 / 空 Map」の双方として扱う。

### 0.5 軸D 値の表現 — 要素（辺ごとに定義が異なる。steering #19/#22/#24/#25 の記述を要素化）

- **辺① セル種別 17 ケース**: D1-01 文字列／D1-02 整数数値／D1-03 小数数値／D1-04 大きい数値／
  D1-05 先頭ゼロ文字列／D1-06 日付書式／D1-07 時刻書式／D1-08 日時書式／D1-09 数式／D1-10 真偽値／
  D1-11 エラー値／D1-12 セル不在／D1-13 空文字／D1-14 前後空白／D1-15 改行／D1-16 リテラル `null`／D1-17 表示形式付き数値
- **辺② YAML スカラー 10 ケース**: D2-01 引用符なし文字列／D2-02 引用符あり／D2-03 数値／D2-04 末尾ゼロ小数／
  D2-05 `true`・`TRUE`・`yes`／D2-06 `null`・`~`・値なし／D2-07 `"null"`／D2-08 日付風／D2-09 複数行 `|`・`>`／D2-10 先頭ゼロ
- **辺③ セル型 8 ケース（`getCellType()` をアサート）**: D3-01 `"100"`／D3-02 `"=1+1"`／D3-03 `"007"`／
  D3-04 `null`／D3-05 `""`／D3-06 改行含む文字列／D3-07 32767 文字超／D3-08 制御文字含む
- **辺④ YAML 表現 9 ケース**: D4-01 `"100"`／D4-02 `"true"`／D4-03 `"null"`／D4-04 `null`／D4-05 `""`／
  D4-06 `"007"`／D4-07 改行含む／D4-08 `"2026-08-07"`／D4-09 コロン・ハイフン・`#` 含む

### 0.6 軸E 多重度 — 要素（4 観点 × 0 件／1 件／複数件。steering #21 より）

- **E-1 セクション内ブロック数**（0／1／複数）
- **E-2 ブロック内行数**（0／1／複数）
- **E-3 ファイル内レコードレイアウト数**（0／1／複数）
- **E-4 コンテナ内セクション数**（1／複数）— 呼称は本書を通じて「E-4 コンテナ内セクション数」に統一する。
  辺ごとの実体は、辺①（ブック内シート数）／辺②（ディレクトリ内 YAML ファイル数）／
  辺③（ブック内シート数）／辺④（出力 YAML ファイル数）。

### 0.7 軸F 異常系 — 要素（辺ごと。steering #21/#22/#24/#25 より）

- **辺①（6）**: F1-01 シート不在／F1-02 ブック破損／F1-03 未知のデータタイプ名／
  F1-04 マーカーカラム欠落／F1-05 カラム名重複／F1-06 行と列の数の不一致
- **辺②（5）**: F2-01 スキーマ違反／F2-02 YAML として不正／F2-03 未知のキー／F2-04 必須構造の欠落／F2-05 空ファイル
- **辺③（4）**: F3-01 出力先不在／F3-02 同名ファイル既存かつ `overwrite=false`／F3-03 書き込み権限なし／
  F3-04 シート名が Excel 制約違反（31 文字超・禁止文字）
- **辺④（3）**: F4-01 出力先不在／F4-02 `overwrite=false` 衝突／F4-03 書き込み権限なし

### 0.8 棚卸しで判明した横断的な事実

以下は「事実:」（コードを読んで確認した内容）と「判断:」（それに基づく本書の扱い）を分けて記す。
判断のうちコーディネータの確認を要するものは §5.3 に再掲する。

1. **辺①の既存 33 件は 1 件も実 `.xlsx` を通っていない。**
   事実: `XlsFormatReaderTest` は内部クラス `FakeTestDataReader`（L54-102）に `List<List<String>>` の
   canned 行を与えて `TestCoreReaderAdapter` を駆動する。実セル → 文字列行の区間（`PoiXlsReader`）は
   一度も実行されない。
   判断: **軸D 辺①（17 ケース）は 33 件からは全て未担保**とする。
2. **辺③の往復テスト 8 件（`roundTrips*`）は実 `.xlsx` を経由して `XlsFormatReader` を駆動している。**
   事実: `XlsFormatWriterTest#roundTrip`（L861-865）は `new XlsFormatWriter().write(...)` で実ファイルを書き、
   `new XlsFormatReader()`（本番配線＝`PoiXlsReader`）で読み戻す。
   判断: steering #19 の「実 `.xlsx` を入力として `XlsFormatReader` を駆動するテストが存在し、
   `FakeTestDataReader` を経由していない」は**既に部分的に満たされている**（文字列セル・空セル・
   リテラル `null` の 3 ケース相当が通る）。#19 はこれを起点にできる。
3. **辺②の既存 20 件は 1 件も実 YAML テキストを通っていない。**
   事実: `YamlFormatReaderTest#reader`（L538-545）は `YamlTestCoreAdapter#loadRawMap` を in-memory
   `LinkedHashMap` に差し替える。YAML パーサ（SnakeYAML Engine）は通らない。一方、辺④の往復テスト 6 件
   （`roundTrip_*`）は `writer.write(...)` で実 YAML ファイルを書き `new YamlFormatReader()` で読み戻す。
   判断: **軸D 辺②（10 ケース）は 20 件からは全て未担保**とし、往復 6 件が通す分は 🔺 で計上する。
4. **`getCellType()` をアサートしているテストは #18 時点では src/test 全体でゼロだった。**
   事実（#18 時点）: `grep -rn "getCellType" src/test/` → 0 件。`XlsFormatWriterTest` のセル読み出しヘルパ
   `cell`（L100-107）／`line`（L110-121）は `getStringCellValue()` のみを使う。
   判断（#18 時点）: **軸D 辺③（8 ケース）は `getCellType()` 観点では全て未担保**とする。
   **現在は 0 件ではない（2026-08-13 追記。件数の内訳をレビュー指摘により訂正）。**
   #19 が `XlsFormatReaderCellTypeTest` に、#22 が `XlsFormatWriterCellTypeTest` に入れた。実測は次のとおり。

   | クラス | 追加タスク | `@Test` の数（`grep -c "^    @Test"`） | `getCellType()` を使うアサートの数 |
   |---|---|---|---|
   | `XlsFormatReaderCellTypeTest` | #19 | 19 | **1**（`readsTextFormattedNumericCellAsDoubleString` 内の 1 行） |
   | `XlsFormatWriterCellTypeTest` | #22 | 18 | **17** |

   `grep -c getCellType src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterCellTypeTest.java`
   は **19** を返すが、これは<b>行数</b>であってアサート数でもテスト数でもない
   （19 行のうち 2 行はクラス Javadoc の散文）。以前ここに「#22 が 19 件を入れた」と書いていたのは
   この行数をテスト件数として並べたもので、誤りだった。最新の内訳と経緯は §3.2 の軸D 表の直後に記す。
   上記の「ゼロ」は #18 時点のスナップショットであり、現在形として読んではならない。
5. **`overwrite` フラグを writer は持たない。**
   事実: `grep -rln "overwrite" src/main/java` の結果、`overwrite` を保持するのは `ConversionRequest` /
   `TestDataConverter` / `ConverterMojo` の 3 クラスのみで、`XlsFormatWriter` / `YamlFormatWriter` は保持しない。
   `overwrite=false` 衝突を検査するのは `TestDataConverter#checkOverwrite`（L90-99）であり、
   上位層の既存テスト `TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse`（L336）と
   `ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict`（L267）が通している
   （いずれも出力先に既存ファイルを置いた状態で変換し、`ConverterException` ／ `MojoExecutionException` を
   アサートしている）。
   判断: 軸F の F3-02 / F4-02 は writer 単体では再現できないため、**辺③／辺④の対象外**
   として分類する（steering #22/#25 の Steps と一致）。

   **担保範囲の訂正（2026-08-13・レビュー指摘による訂正。2026-08-13 の第 3 ラウンドで根拠を再訂正）。**
   当初この項と各節は「上位層の既存テストで担保済み」と書いていたが、
   **担保されているのは `.yaml` を出力側とする衝突だけ**である。
   事実:
   - `checkOverwrite`（L90-99）は `target.outputPaths(container, outputBase)` を多態で呼び分ける。
     引用した 2 件の既存テストは**どちらも XLS→YAML** であり（`TestDataConverter.convert(DataFormat.XLS,
     DataFormat.YAML, ...)` ／ Mojo の `from=xls, to=yaml`。衝突させているのは `BookA/data.yaml`）、
     実行されるのは `YamlFormatHandler#outputPaths`（L63）である。
   - **`XlsFormatHandler#outputPaths`（L63-67）自体は `overwrite=false` 下で実行されている。**
     4 引数入口 `TestDataConverter.convert(DataFormat, DataFormat, Path, Path)`（L49-56）は
     `Builder#overwrite` を呼ばずにリクエストを組み、`ConversionRequest` の `overwrite` 既定値は
     `false`（L120 の Javadoc・L128 のフィールド）なので、`checkOverwrite` は早期 return せず
     `outputPaths` を呼ぶ。実際に通しているのは XLS を出力側とする
     `TestDataConverterTest#convertsYamlToXls`（L202）／`#convertsXlsToXls`（L224）／
     `#convertsYamlWithFilesToXls`（L427）の 3 件である。
   - **1 件も通っていないのは `.xlsx` が既存で衝突する分岐**（`checkOverwrite` の
     `Files.exists(output)` が真 → `ConverterException`）**のほう**である。

   **確かめ方（変異テスト。2026-08-13 実測。`src/main` は確認後に元へ戻してある）**:

   | 変異 | 結果 |
   |---|---|
   | `XlsFormatHandler#outputPaths` の先頭で `IllegalStateException` を送出する | `TestDataConverterTest` の `convertsXlsToXls` / `convertsYamlToXls` / `convertsYamlWithFilesToXls` の **3 件だけが ERROR**（`Tests run: 410, Failures: 0, Errors: 3`）。つまり `outputPaths` は実行されている |
   | `checkOverwrite` の `Files.exists(output)` が真かつ出力が `.xlsx` のときだけ `AssertionError` を送出する | **410 件すべて PASS**（`Tests run: 410, Failures: 0, Errors: 0`）。つまり `.xlsx` の衝突分岐は 1 件も通っていない |

   以前ここには「`grep -rn "outputPaths" src/test --include=*.java` → **0 件**」と書いていたが、
   これは誤りだった。このコマンドがヒットするのは `XlsFormatWriterInvalidOutputTest` の
   クラス Javadoc の記述だけで（確認: `grep -rn "outputPaths" src/test --include=*.java | grep -v
   XlsFormatWriterInvalidOutputTest` → **0 件**）、当時も 0 件ではなく、自分の書いた記述を数えていた。
   そもそも `outputPaths` を**直接呼ぶ**テストが無いことと、`outputPaths` が**実行されない**ことは別である
   （上表の変異のとおり、多態呼び出しで実行されている）。

   したがって正確には、**共通処理 `checkOverwrite` の分岐も `XlsFormatHandler#outputPaths` 自体も
   既存テストが通しているが、`.xlsx` を出力側とする衝突（＝辺③の F3-02 が指す状況）は未担保**である。
   ただしこれは上位層側の穴であって辺③（`XlsFormatWriter` 単体）の責務ではないため、
   **F3-02 を辺③の対象外とする結論は変えない**（`XlsFormatWriter` は `overwrite` を保持しないので、
   辺③に書いても再現できない）。本書で「上位層で担保済み」と記した箇所はこの但し書きつきで読むこと。
   辺④（YAML 出力）の F4-02 については、上記 2 件がまさに `.yaml` を出力側とする衝突であり、
   この但し書きは当たらない。
6. **1 リソース単位 API のため、辺①・辺②では「セクション複数」「セクション 0」が構造上生成されない。**
   事実: `XlsFormatReader#read`（L101-133）は `Collections.singletonList(section)` を返し、
   `YamlFormatReader#read`（L87-95）も同じく `Collections.singletonList(section)` を返す。
   一方 writer 側は `XlsFormatWriter#build`（L125）／`YamlFormatWriter#write`（L74）が
   `container.getSections()` をループするため、辺③／辺④では複数・0 とも到達可能である。
   判断: E-4「コンテナ内セクション数 複数」と C-02「sections 空」は**辺①・辺②では到達不能**、
   **辺③・辺④では要追加**として分類する。
   **辺③の C-02「sections 空」は #23 で担保済みになった（2026-08-13 追記）**:
   `XlsFormatWriterModelTest#writesWorkbookWithoutSheetsWhenContainerHasNoSections` が
   シートを 1 枚も持たないブックが書き出されることを実測して固定した（`issues.md` **XLS-23**）。
7. **`DataType.DEFAULT` はリーダ 2 経路のいずれでも生成されない。**
   事実: 辺① — `TestCoreReaderAdapter` L362 が `type == DataType.DEFAULT` のブロックを `continue` でスキップする。
   辺② — `YamlFormatReader#addBlocksForSection`（L106-133）と `fileDataType`（L534-536）／
   `addMessageBlocks`（L264）は `DEFAULT` 以外の 13 種のみを分岐に持ち、`DEFAULT` を返す経路がない。
   判断: A-01 `DEFAULT` は**辺①・辺②で到達不能**。writer 側（辺③）は
   `XlsFormatWriter` L400 がマーカー文字列を `block.getDataType().getName()` から組み立てるだけで
   タイプを絞らないため到達可能であり、**辺③は要追加**（辺④は `serialize_unsupportedDataType_throws` で担保済み）。
   **辺③は #23 で担保済みになった（2026-08-13 追記）**: `XlsFormatWriterModelTest#writesDefaultDataTypeMarker` が
   識別セル `DEFAULT=T` が書き出されることを実測して固定した。あわせて
   `#dropsDefaultDataTypeBlockWhenReadBack` が「辺③で書けたブロックが辺①で読み戻すと消える」ことを実検査する。
   辺③（書ける）と辺④（例外）の非対称は `issues.md` **XLS-20** に記録した（修正はしない）。

### 0.8-8 `RoundTripTest`（30 件）の扱い

`src/test/java/nablarch/test/tool/converter/RoundTripTest.java`（30 `@Test` / 882 行）は 4 辺いずれの
担当クラスのテストでもないため §0.1 の 126 件には含まれないが、`new XlsFormatWriter().write(...)` で実
`.xlsx` を書き `new XlsFormatReader().read(...)`（本番配線）で読み戻す XLS 経路 13 件と、
`new YamlFormatWriter().write(...)` → `new YamlFormatReader().read(...)` の YAML 経路 14 件、
両経路を 1 メソッドで通す 3 件からなり、**4 辺すべてを実ファイル経由で駆動している**
（往復ヘルパ: `xlsRoundTrip` L720-728 ／ `yamlRoundTrip` L733-741）。

steering Rules（フェーズ2）に従い、これらが通す軸要素は **🔺弱い担保として計上するが正式担保としては数えず、
直接テストの追加対象からも外さない**。したがって未担保一覧には残したうえで「`RoundTripTest#xxx` で 🔺 担保あり
（重複を避けること）」と注記する。

| # | テストメソッド | 経路（駆動する辺） | 🔺 で通す軸A | 🔺 で通す軸B | 🔺 で通す軸C | 🔺 で通す軸D |
|---|---|---|---|---|---|---|
| 1 | `xls_setupTable_isPreserved` | XLS（辺③→辺①） | A-02 | B-1 | C-05, **C-06(省略=`""`)**, C-07, C-08, C-09 | 辺① D1-01, D1-13／辺③ D3-05 |
| 2 | `xls_expectedTable_withGroupId_isPreserved` | XLS（辺③→辺①） | A-03 | B-1 | C-05, C-06(値あり), C-07, C-08, C-09 | 辺① D1-01 |
| 3 | `xls_expectedCompleteTable_isPreserved` | XLS（辺③→辺①） | **A-04（辺①で唯一）** | B-1 | C-05, C-06(省略), C-07, C-08, C-09 | 辺① D1-01 |
| 4 | `xls_listMap_isPreserved` | XLS（辺③→辺①） | A-05 | B-2 | C-05, C-07, C-08, C-09 | 辺① D1-01, D1-13／辺③ D3-05 |
| 5 | `xls_setupFixed_isPreserved` | XLS（辺③→辺①） | A-06 | B-3 | C-05, C-06(省略), C-07, C-10(FIXED), C-11(値あり), C-12, C-16(値あり), C-17〜C-21 | 辺① D1-01 |
| 6 | `xls_expectedFixed_isPreserved` | XLS（辺③→辺①） | **A-07（辺①・辺③で唯一）** | B-3 | C-05, C-06(省略), C-07, C-10(FIXED), C-12, C-16(値あり), C-17〜C-21 | 辺① D1-01 |
| 7 | `xls_setupVariable_isPreserved` | XLS（辺③→辺①） | A-08 | B-3 | C-05, C-06(省略), C-07, C-10(VARIABLE), C-21(省略) | 辺① D1-01 |
| 8 | `xls_expectedVariable_isPreserved` | XLS（辺③→辺①） | **A-09（辺①・辺③で唯一）** | B-3 | C-05, C-06(省略), C-07, C-10(VARIABLE), C-21(省略) | 辺① D1-01 |
| 9 | `xls_message_isPreserved` | XLS（辺③→辺①） | A-10 | B-4 | C-05, C-06(省略), C-07, C-14(値あり), C-15, C-16〜C-21 | 辺① D1-01 |
| 10 | `xls_expectedRequestHeaderMessages_isPreserved` | XLS（辺③→辺①） | A-11 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16〜C-21 | 辺① D1-01 |
| 11 | `xls_expectedRequestBodyMessages_isPreserved` | XLS（辺③→辺①） | A-12 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16〜C-21 | 辺① D1-01 |
| 12 | `xls_responseHeaderMessages_isPreserved` | XLS（辺③→辺①） | A-13 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16〜C-21 | 辺① D1-01 |
| 13 | `xls_responseBodyMessages_isPreserved` | XLS（辺③→辺①） | A-14 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16〜C-21 | 辺① D1-01 |
| 14 | `yaml_setupTable_isPreserved` | YAML（辺④→辺②） | A-02 | B-1 | C-05, C-06(省略), C-07, C-08, C-09 | 辺② D2-02, D2-06／辺④ D4-04, D4-05 |
| 15 | `yaml_expectedTable_withGroupId_isPreserved` | YAML（辺④→辺②） | A-03 | B-1 | C-05, C-06(値あり), C-07, C-08, C-09 | 辺② D2-02 |
| 16 | `yaml_expectedCompleteTable_isPreserved` | YAML（辺④→辺②） | A-04 | B-1 | C-05, C-06(省略), C-07, C-08, C-09 | 辺② D2-02 |
| 17 | `yaml_listMap_isPreserved` | YAML（辺④→辺②） | A-05 | B-2 | C-05, C-07, C-08, C-09 | 辺② D2-02 |
| 18 | `yaml_listMap_withNullValue_isPreserved` | YAML（辺④→辺②） | A-05 | B-2 | C-09 | 辺② D2-06／辺④ D4-04 |
| 19 | `yaml_setupFixed_isPreserved` | YAML（辺④→辺②） | A-06 | B-3 | C-05, C-06(省略), C-07, C-10(FIXED), C-11(値あり), C-12, C-16(値あり), C-17〜C-21 | 辺② D2-02 |
| 20 | `yaml_expectedFixed_isPreserved` | YAML（辺④→辺②） | **A-07（辺④で唯一）** | B-3 | C-05, C-06(省略), C-07, C-10(FIXED), C-12, C-16(値あり), C-17〜C-21 | 辺② D2-02 |
| 21 | `yaml_setupVariable_isPreserved` | YAML（辺④→辺②） | **A-08（辺④で唯一）** | B-3 | C-05, C-06(省略), C-07, C-10(VARIABLE), C-21(省略) | 辺② D2-02 |
| 22 | `yaml_expectedVariable_isPreserved` | YAML（辺④→辺②） | A-09 | B-3 | C-05, C-06(省略), C-07, C-10(VARIABLE), C-21(省略) | 辺② D2-02 |
| 23 | `yaml_message_isPreserved` | YAML（辺④→辺②） | A-10 | B-4 | C-05, C-06(省略), C-07, C-14(値あり), C-15, C-16〜C-21 | 辺② D2-02 |
| 24 | `yaml_expectedRequestHeaderMessages_isPreserved` | YAML（辺④→辺②） | A-11 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16(省略=null), C-17〜C-21 | 辺② D2-02 |
| 25 | `yaml_expectedRequestBodyMessages_isPreserved` | YAML（辺④→辺②） | A-12 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16(省略=null), C-17〜C-21 | 辺② D2-02 |
| 26 | `yaml_responseHeaderMessages_isPreserved` | YAML（辺④→辺②） | A-13 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16(省略=null), C-17〜C-21 | 辺② D2-02 |
| 27 | `yaml_responseBodyMessages_isPreserved` | YAML（辺④→辺②） | A-14 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16(省略=null), C-17〜C-21 | 辺② D2-02 |
| 28 | `nullCell_xlsConvertsToLiteralString_yamlPreservesNull` | XLS＋YAML（4 辺） | A-02 | B-1 | C-09（行のみアサート） | 辺① **D1-16 リテラル `null`**／辺② D2-06／辺③ D3-04（値のみ）／辺④ D4-04 |
| 29 | `leadingTrailingWhitespace_isPreservedInBothPaths` | XLS＋YAML（4 辺） | A-02 | B-1 | C-09（行のみアサート） | 辺① **D1-14 前後空白**／辺② D2-02 |
| 30 | `specialNotation_isPreservedInBothPaths` | XLS＋YAML（4 辺） | A-02 | B-1 | C-09（行のみアサート） | 辺① D1-01／辺② D2-02 |

※ 軸E は全 30 件が「1 セクション・1 ブロック」固定（`xlsRoundTrip` L725-726 ／ `yamlRoundTrip` L738-739 が
`sections.size()==1` と `blocks.size()==1` をアサートする）ため、E-1(1)／E-4(1) 以外の多重度は通さない。
軸F は 30 件とも正常系のため通さない。

**この表によって従来 ❌ だった要素に新たに 🔺 が付くもの**:

| 辺 | 新たに 🔺 になる要素 | 根拠テスト |
|---|---|---|
| 辺① | A-04 `EXPECTED_COMPLETED` | `xls_expectedCompleteTable_isPreserved` |
| 辺① | A-07 `EXPECTED_FIXED` | `xls_expectedFixed_isPreserved` |
| 辺① | A-09 `EXPECTED_VARIABLE` | `xls_expectedVariable_isPreserved` |
| 辺① | C-06 `groupId` 省略(`""`) | `xls_setupTable_isPreserved`, `xls_expectedCompleteTable_isPreserved` ほか 5 件（`assertTableBlock` L751 ／ `assertFileBlock` L761 ／ `assertMessageBlock` L790 が `getGroupId()` を `""` と突き合わせる XLS 経路 7 件） |
| 辺① | D1-14 前後空白 | `leadingTrailingWhitespace_isPreservedInBothPaths` |
| 辺③ | A-07 `EXPECTED_FIXED` | `xls_expectedFixed_isPreserved` |
| 辺③ | A-09 `EXPECTED_VARIABLE` | `xls_expectedVariable_isPreserved` |
| 辺③ | **A-12 `EXPECTED_REQUEST_BODY_MESSAGES`**（2026-08-13 追記） | `xls_expectedRequestBodyMessages_isPreserved` |
| 辺③ | **A-13 `RESPONSE_HEADER_MESSAGES`**（同上） | `xls_responseHeaderMessages_isPreserved` |
| 辺③ | **A-14 `RESPONSE_BODY_MESSAGES`**（同上） | `xls_responseBodyMessages_isPreserved` |
| 辺④ | A-07 `EXPECTED_FIXED` | `yaml_expectedFixed_isPreserved` |
| 辺④ | A-08 `SETUP_VARIABLE` | `yaml_setupVariable_isPreserved` |

辺① D1-01 文字列／D1-13 空文字／D1-16 リテラル `null`、辺② D2-02／D2-03／D2-06／D2-07、
辺③ D3-04／D3-05、辺④ D4-01 は既に `XlsFormatWriterTest#roundTrips*` ／ `YamlFormatWriterTest#roundTrip_*`
経由で 🔺 であり、`RoundTripTest` は担保の厚みを増すが判定は変えない。
辺②については新たに 🔺 になる要素はない。

**辺③ A-12〜A-14 の 3 行は 2026-08-13（#23 レビュー対応）で追加した。**
#18 時点は辺③ A-12〜A-14 を ✅ と判定していたためこの表に載せていなかったが、変異による実測で
`RoundTripTest` の 3 件だけが辺③のこれらを通していること（＝🔺）が分かった
（[§3.1-3](#s3-1-3-sendsync)）。3 件とも #23 レビュー対応で ✅ になっているが、
**この表は「その要素を `RoundTripTest` が 🔺 として通している」という事実の一覧**であるため、
✅ 化後も行は残す（辺① A-04／A-07／A-09 と同じ扱い）。

**未担保件数への影響はない。** 🔺 は正式担保として数えないため、§5.1 の 107 件は本項の追加後も変わらない。

---

<a id="s1"></a>

## 1. 辺① Excel→中間モデル（`XlsFormatReaderTest` 33 件）

### 1.1 テストメソッド → 担保する軸要素

| # | テストメソッド | 軸A | 軸B | 軸C | 軸D | 軸E | 軸F |
|---|---|---|---|---|---|---|---|
| 1 | `readMapsTableBlockPreservingRawValues` | A-02 | B-1 | C-07, C-08, C-09 | — ※文字列レベルで `${}`／`""`／null セル区別 | E-1(1), E-2(複数) | — |
| 2 | `readTableNormalizesExcelQuotationNotation` | A-02 | B-1 | C-09 | — ※Excel 引用符記法 `""`→`""`・`"abc"`→`abc` | E-2(1) | — |
| 3 | `readListMapNormalizesExcelQuotationNotation` | A-05 | B-2 | C-09 | — ※同上 | E-2(1) | — |
| 4 | `readFixedFileNormalizesExcelQuotationNotation` | A-06 | B-3 | C-12, C-18 | — ※同上 | E-3(1) | — |
| 5 | `readMapsMultipleTablesWithoutDuplication` | A-02 | B-1 | C-07 | — | E-1(複数=2) | — |
| 6 | `readPreservesGroupIdAndDataType` | A-03 | B-1 | C-05, C-06(値あり), C-09 | — | E-1(1) | — |
| 7 | `readListMapPreservesColumnOrder` | A-05 | B-2 | C-08(記述順), C-09 | — | E-2(複数=2) | — |
| 8 | `readListMapExcludesMarkerColumns` | A-05 | B-2 | C-08, C-09 | — ※マーカーカラム `[no]` 除外 | E-2(複数=2) | — |
| 9 | `readMapsListMapBlock` | A-05 | B-2 | C-07, C-08, C-09 | — ※`${}`／`""` | E-2(複数=2) | — |
| 10 | `readMapsFixedLengthFileBlock` | A-06 | B-3 | C-07, C-10(FIXED), C-12, C-16(値あり), C-17, C-18, C-19, C-20(値あり), C-21(値あり) | — | E-3(1) | — |
| 11 | `readRestoresOriginalRecordTypeTypeAndOmittedLengthFromRawLines` | A-06 | B-3 | C-16, C-18, C-20, C-21(`"-"`) | — ※長さ記法 `-` の原文保持 | E-3(1) | — |
| 12 | `readRestoresMultipleRecordLayoutsInFixedFile` | A-06 | B-3 | C-12, C-16, C-17, C-18, C-19, C-20, C-21 | — | E-3(複数=2), E-2(1/複数) | — |
| 13 | `readMapsVariableLengthFileBlock` | A-08 | B-3 | C-10(VARIABLE), C-18, C-19, C-21(省略=null) | — | E-3(1) | — |
| 14 | `readMapsMessageBlock` | A-10 | B-4 | C-05, C-07, C-14(値あり), C-15, C-18, C-19 | — | E-3(1) | — |
| 15 | `readMapsExpectedRequestHeaderMessageBlock` | A-11 | B-4 | C-05, C-06(値あり), C-07, C-13(値あり), C-14(空), C-15, C-17, C-18, C-19, C-20, C-21 | — | E-3(1) | — |
| 16 | `readMapsAllFourSendSyncMessageTypes` | A-11, A-12, A-13, A-14 | B-4 | C-05, C-06 | — | E-1(複数=4) | — |
| 17 | `readMapsMultipleSendSyncBlocksInSameGroup` | A-11 | B-4 | C-07 | — | E-1(複数=2) | — |
| 18 | `readNormalizesRecordSeparatorCrlfSymbol` | A-08 | B-3 | C-11(値あり) | — ※`record-separator` CRLF シンボル逆正規化 | — | — |
| 19 | `readNormalizesRecordSeparatorLfSymbol` | A-08 | B-3 | C-11 | — ※LF シンボル | — | — |
| 20 | `readNormalizesRecordSeparatorCrSymbol` | A-08 | B-3 | C-11 | — ※CR シンボル | — | — |
| 21 | `defaultConstructorWiresProductionAdapter` | — | — | — | — | — | —（本番配線の生成可能性のみ） |
| 22 | `readIgnoresDataTypePrefixedLineWithoutMarker` | — | — | C-04(空) | — | E-1(0) | 🔺F1-03 に近い（データタイプ名で始まるが `=` なしの行を無視） |
| 23 | `readPreservesErrorModeRowInSendSyncMessage` | A-14 | B-4 | C-16, C-17, C-18, C-19 | — ※`errorMode:timeout` の原文保持 | E-3(1) | — |
| 24 | `readDerivesContainerAndSectionNamesFromResource` | A-02 | B-1 | C-01, C-02(1件), C-03 | — | E-4(1) | — |
| 25 | `readListMapWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins` | A-05 | B-2 | C-08, C-09 | — | — | **F1-05** ✅（WARN・後勝ち） |
| 26 | `readListMapWithMultipleDuplicateColumnsEmitsWarnPerName` | A-05 | B-2 | C-08, C-09 | — | — | **F1-05** ✅（複数名の重複） |
| 27 | `readTableWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins` | A-02 | B-1 | C-08, C-09 | — | — | **F1-05** ✅（TABLE 系） |
| 28 | `readListMapWithoutDuplicatesEmitsNoWarn` | A-05 | B-2 | — | — | — | F1-05 の非回帰（WARN なし） |
| 29 | `readReturnsEmptySectionWhenNoBlocks` | — | — | C-02(1件), C-04(空) | — | **E-1(0)** ✅ | — |
| 30 | `readAssemblesMixedBlockTypesInOneSection` | A-02, A-05, A-06, A-10 | **B-1, B-2, B-3, B-4** | C-04 | — | E-1(複数=4) | — |
| 31 | `readNormalizesRecordSeparatorEmptyValueToNoneSymbol` | A-08 | B-3 | C-11 | — ※NONE シンボル逆正規化 | — | — |
| 32 | `readPassesThroughUnknownRecordSeparatorValue` | A-08 | B-3 | C-11 | — ※未知値のパススルー | — | — |
| 33 | `readStripsQuotesFromQuotedGenericDirectiveValue` | A-08 | B-3 | C-11 | — ※ディレクティブ値の引用符除去 | — | — |

### 1.2 軸要素 → 担保テストメソッド

**軸A（✅ 10 ／ 🔺 3 ／ ❌ 1 ＝ 14）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| A-01 `DEFAULT` | ❌ | —（到達不能。`TestCoreReaderAdapter` L362 が DEFAULT ブロックをスキップする。§0.8-7） |
| A-02 `SETUP_TABLE_DATA` | ✅ | `readMapsTableBlockPreservingRawValues`, `readTableNormalizesExcelQuotationNotation`, `readMapsMultipleTablesWithoutDuplication`, `readTableWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins`, `readDerivesContainerAndSectionNamesFromResource`, `readAssemblesMixedBlockTypesInOneSection` |
| A-03 `EXPECTED_TABLE_DATA` | ✅ | `readPreservesGroupIdAndDataType` |
| A-04 `EXPECTED_COMPLETED` | 🔺 | `XlsFormatReaderTest` には 0 件。`RoundTripTest#xls_expectedCompleteTable_isPreserved` が実 `.xlsx` 経由で通す（§0.8-8。重複を避けること） |
| A-05 `LIST_MAP` | ✅ | `readListMapNormalizesExcelQuotationNotation`, `readListMapPreservesColumnOrder`, `readListMapExcludesMarkerColumns`, `readMapsListMapBlock`, `readListMapWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins`, `readListMapWithMultipleDuplicateColumnsEmitsWarnPerName`, `readListMapWithoutDuplicatesEmitsNoWarn`, `readAssemblesMixedBlockTypesInOneSection` |
| A-06 `SETUP_FIXED` | ✅ | `readFixedFileNormalizesExcelQuotationNotation`, `readMapsFixedLengthFileBlock`, `readRestoresOriginalRecordTypeTypeAndOmittedLengthFromRawLines`, `readRestoresMultipleRecordLayoutsInFixedFile`, `readAssemblesMixedBlockTypesInOneSection` |
| A-07 `EXPECTED_FIXED` | 🔺 | `XlsFormatReaderTest` には 0 件。`RoundTripTest#xls_expectedFixed_isPreserved` が実 `.xlsx` 経由で通す（§0.8-8。重複を避けること） |
| A-08 `SETUP_VARIABLE` | ✅ | `readMapsVariableLengthFileBlock`, `readNormalizesRecordSeparatorCrlfSymbol`, `readNormalizesRecordSeparatorLfSymbol`, `readNormalizesRecordSeparatorCrSymbol`, `readNormalizesRecordSeparatorEmptyValueToNoneSymbol`, `readPassesThroughUnknownRecordSeparatorValue`, `readStripsQuotesFromQuotedGenericDirectiveValue` |
| A-09 `EXPECTED_VARIABLE` | 🔺 | `XlsFormatReaderTest` には 0 件。`RoundTripTest#xls_expectedVariable_isPreserved` が実 `.xlsx` 経由で通す（§0.8-8。重複を避けること） |
| A-10 `MESSAGE` | ✅ | `readMapsMessageBlock`, `readAssemblesMixedBlockTypesInOneSection` |
| A-11 `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `readMapsExpectedRequestHeaderMessageBlock`, `readMapsAllFourSendSyncMessageTypes`, `readMapsMultipleSendSyncBlocksInSameGroup` |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `readMapsAllFourSendSyncMessageTypes` |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅ | `readMapsAllFourSendSyncMessageTypes` |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅ | `readMapsAllFourSendSyncMessageTypes`, `readPreservesErrorModeRowInSendSyncMessage` |

**軸B（✅ 4 ／ 4）**

| 要素 | 判定 | 担保テストメソッド（代表） |
|---|---|---|
| B-1 `TableDataBlock` | ✅ | `readMapsTableBlockPreservingRawValues`, `readAssemblesMixedBlockTypesInOneSection` ほか 5 件（計 7 件） |
| B-2 `ListMapBlock` | ✅ | `readMapsListMapBlock`, `readAssemblesMixedBlockTypesInOneSection` ほか 6 件（計 8 件） |
| B-3 `FileDataBlock` | ✅ | `readMapsFixedLengthFileBlock`, `readMapsVariableLengthFileBlock` ほか 10 件（計 12 件） |
| B-4 `MessageDataBlock` | ✅ | `readMapsMessageBlock`, `readMapsExpectedRequestHeaderMessageBlock` ほか 4 件（計 6 件） |

**軸C（21 フィールド ─ 両状態担保 9 ／ 未担保 12）**

省略可能フィールドは「値あり」「省略」、空許容コレクションは「非空」「空」を別々に評価する。
n/a 6 件（C-01, C-03, C-05, C-07, C-10, C-19）は「省略」「空」という状態を持たない必須スカラー／2 値であり、
「値あり」の担保をもって両状態担保として数える。

| 要素 | 値あり／非空 | 省略／空 | 担保テストメソッド |
|---|---|---|---|
| C-01 `TestDataContainer.name` | ✅ | n/a | `readDerivesContainerAndSectionNamesFromResource` |
| C-02 `TestDataContainer.sections` | ✅(1件) | ❌ 空（**到達不能**） | `readDerivesContainerAndSectionNamesFromResource`, `readReturnsEmptySectionWhenNoBlocks`（`XlsFormatReader#read` L133 が `Collections.singletonList(section)` を返すため、辺①では sections は常に 1 件。空・複数は構造上生成されない。§0.8-6） |
| C-03 `TestDataSection.name` | ✅ | n/a | `readDerivesContainerAndSectionNamesFromResource` |
| C-04 `TestDataSection.blocks` | ✅ | ✅ 空 | 非空: `readMapsTableBlockPreservingRawValues`, `readMapsListMapBlock` ほか 28 件（ブロックを 1 件以上組み立てる計 30 件）／空: `readReturnsEmptySectionWhenNoBlocks`, `readIgnoresDataTypePrefixedLineWithoutMarker` |
| C-05 `TestDataBlock.dataType` | ✅ | n/a | `readPreservesGroupIdAndDataType`, `readMapsMessageBlock`, `readMapsExpectedRequestHeaderMessageBlock`, `readMapsAllFourSendSyncMessageTypes` |
| C-06 `TestDataBlock.groupId` | ✅ | 🔺 省略(`""`) | 値あり: `readPreservesGroupIdAndDataType`, `readMapsExpectedRequestHeaderMessageBlock` ほか 1 件（計 3 件）／省略: `XlsFormatReaderTest` で `""` をアサートするテストは 0 件。`RoundTripTest#xls_setupTable_isPreserved` ほか 6 件（計 7 件）が実 `.xlsx` 経由で `getGroupId()` が `""` であることをアサートする（§0.8-8。重複を避けること） |
| C-07 `TestDataBlock.identifier` | ✅ | n/a | `readMapsTableBlockPreservingRawValues`, `readMapsListMapBlock` ほか 5 件（計 7 件） |
| C-08 `ColumnRowDataBlock.columnNames` | ✅ | ❌ 空 | 非空: `readMapsTableBlockPreservingRawValues`, `readListMapPreservesColumnOrder` ほか 5 件（計 7 件） |
| C-09 `ColumnRowDataBlock.rows` | ✅ | ❌ 空 | 非空: `readMapsTableBlockPreservingRawValues`, `readTableNormalizesExcelQuotationNotation` ほか 8 件（計 10 件） |
| C-10 `FileDataBlock.fileType` | ✅ FIXED / ✅ VARIABLE | n/a | FIXED: `readMapsFixedLengthFileBlock`／VARIABLE: `readMapsVariableLengthFileBlock` |
| C-11 `FileDataBlock.directives` | ✅ | ❌ 空 | 非空: `readNormalizesRecordSeparator*`（4件）, `readPassesThroughUnknownRecordSeparatorValue`, `readStripsQuotesFromQuotedGenericDirectiveValue` |
| C-12 `FileDataBlock.records` | ✅ | ❌ 空 | 非空: `readMapsFixedLengthFileBlock`, `readRestoresMultipleRecordLayoutsInFixedFile` |
| C-13 `MessageDataBlock.directives` | ✅ | ❌ 空 | 非空: `readMapsExpectedRequestHeaderMessageBlock` のみ |
| C-14 `MessageDataBlock.fwHeaderFields` | ✅ | ✅ 空 | 非空: `readMapsMessageBlock`／空: `readMapsExpectedRequestHeaderMessageBlock` |
| C-15 `MessageDataBlock.records` | ✅ | ❌ 空 | 非空: `readMapsMessageBlock`, `readMapsExpectedRequestHeaderMessageBlock` |
| C-16 `RecordLayout.recordType` | ✅ | ❌ 省略(null) | 値あり: `readMapsFixedLengthFileBlock`, `readRestoresOriginalRecordTypeTypeAndOmittedLengthFromRawLines`, `readRestoresMultipleRecordLayoutsInFixedFile`, `readPreservesErrorModeRowInSendSyncMessage` |
| C-17 `RecordLayout.fields` | ✅ | ❌ 空 | 非空: `readMapsFixedLengthFileBlock`, `readRestoresMultipleRecordLayoutsInFixedFile` ほか 2 件（計 4 件） |
| C-18 `RecordLayout.rows` | ✅ | ❌ 空 | 非空: `readFixedFileNormalizesExcelQuotationNotation`, `readMapsFixedLengthFileBlock` ほか 6 件（計 8 件） |
| C-19 `FieldDef.name` | ✅ | n/a | `readMapsFixedLengthFileBlock`, `readMapsExpectedRequestHeaderMessageBlock` ほか 4 件（計 6 件） |
| C-20 `FieldDef.type` | ✅ | ❌ 省略(null) | 値あり: `readMapsFixedLengthFileBlock`, `readRestoresOriginalRecordTypeTypeAndOmittedLengthFromRawLines` |
| C-21 `FieldDef.length` | ✅ | ✅ 省略(null) | 値あり: `readMapsFixedLengthFileBlock`（`"10"`/`"5"`）, `readRestoresOriginalRecordTypeTypeAndOmittedLengthFromRawLines`（`"-"`）／省略: `readMapsVariableLengthFileBlock` |

**軸D（✅ 0 ／ 🔺 4 ／ ❌ 13 ＝ 17）**

D1-01〜D1-17 すべて ❌。理由: 既存 33 件は `FakeTestDataReader`（`XlsFormatReaderTest` L54-102）に文字列行を直接与えるため、
実セル → 文字列行の変換区間を一切通らない。セル種別という概念がテスト入力に存在しない。

🔺 弱い担保（実 `.xlsx` を通る往復テスト経由）:
- D1-01 文字列: `XlsFormatWriterTest#roundTripsTable`, `#roundTripsListMap` ほか 6 件（`roundTrips*` 全 8 件）、
  `RoundTripTest` の XLS 経路 13 件と両経路 3 件（§0.8-8）
- D1-13 空文字: `XlsFormatWriterTest#roundTripsTable`, `#roundTripsNullCellAsLiteralNullString`、
  `RoundTripTest#xls_setupTable_isPreserved`, `#xls_listMap_isPreserved`
- **D1-14 前後空白**: `RoundTripTest#leadingTrailingWhitespace_isPreservedInBothPaths`（§0.8-8。半角/全角の前後空白が
  Excel 往復で脱落しないことをアサートする）
- D1-16 リテラル `null`: `XlsFormatWriterTest#roundTripsNullCellAsLiteralNullString`,
  `RoundTripTest#nullCell_xlsConvertsToLiteralString_yamlPreservesNull`

**軸E**

| 要素 | 0 件 | 1 件 | 複数件 | 担保テストメソッド |
|---|---|---|---|---|
| E-1 セクション内ブロック数 | ✅ | ✅ | ✅ | 0: `readReturnsEmptySectionWhenNoBlocks`, `readIgnoresDataTypePrefixedLineWithoutMarker`／1: `readMapsTableBlockPreservingRawValues`, `readPreservesGroupIdAndDataType` ほか 24 件（ブロック 1 件のみを組み立てる計 26 件）／複数: `readMapsMultipleTablesWithoutDuplication`, `readMapsAllFourSendSyncMessageTypes` ほか 2 件（計 4 件） |
| E-2 ブロック内行数 | ❌ | ✅ | ✅ | 1: `readTableNormalizesExcelQuotationNotation`, `readListMapNormalizesExcelQuotationNotation` ほか 1 件（計 3 件）／複数: `readMapsTableBlockPreservingRawValues`, `readListMapPreservesColumnOrder` ほか 2 件（計 4 件） |
| E-3 ファイル内レコードレイアウト数 | ❌ | ✅ | ✅ | 1: `readFixedFileNormalizesExcelQuotationNotation`, `readMapsFixedLengthFileBlock` ほか 5 件（計 7 件）／複数: `readRestoresMultipleRecordLayoutsInFixedFile` |
| E-4 コンテナ内セクション数（辺①の実体: ブック内シート数） | n/a | ✅ | ❌（**到達不能**） | 1: `readDerivesContainerAndSectionNamesFromResource`。複数は `XlsFormatReader#read`（L96-101 の Javadoc・L133）が `"ブック名/シート名"` の 1 シート単位 API で `Collections.singletonList(section)` を返すため構造上到達不能（§0.8-6） |

**軸F（✅ 1 ／ 🔺 1 ／ ❌ 4 ＝ 6）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| F1-01 シート不在 | ❌ | — |
| F1-02 ブック破損 | ❌ | — |
| F1-03 未知のデータタイプ名 | 🔺 | `readIgnoresDataTypePrefixedLineWithoutMarker`（データタイプ名で始まるが `=` を持たない行の無視。「未知の名前」そのものではない） |
| F1-04 マーカーカラム欠落 | ❌ | — |
| F1-05 カラム名重複 | ✅ | `readListMapWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins`, `readListMapWithMultipleDuplicateColumnsEmitsWarnPerName`, `readTableWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins`（非回帰: `readListMapWithoutDuplicatesEmitsNoWarn`） |
| F1-06 行と列の数の不一致 | ❌ | — |

### 1.2-2 #19／#20／#21 が追加したテストクラスの担保（2026-08-12 追記）

**本節は #20 で新設し、#21 で追記した。** §1.1／§1.2 は「`XlsFormatReaderTest` 33 件」を対象とした
#18 時点の事実であり書き換えていない。ここには #19／#20／#21 が追加した
**実 `.xlsx` を入力とするテストクラス**の担保だけを記す。

| テストクラス | 追加タスク | 件数 | 入力 |
|---|---|---|---|
| `XlsFormatReaderCellTypeTest` | #19 | 19 | `XlsFixture` が POI で組み立てた実 `.xlsx` |
| `XlsReferenceFixtureTest` | #19 | 2 | Excel 保存物の参照フィクスチャ ＋ POI 生成物 |
| `XlsFormatReaderRealFileTest` | #20 ＋ #21 | 23（#20 が 17 ＋ #21 が 6） | `XlsFixture` が POI で組み立てた実 `.xlsx` |
| `XlsFormatReaderInvalidInputTest` | #21 | 16 | 同上（意図的に壊した入力・破損ブック） |

**軸A（`XlsFormatReaderRealFileTest`。#20 で ✅ 化した 3 件を太字）**

列を 2 つに分けてある。**「`getDataType()` をアサート」列がその要素の担保**であり、「同経路を通過（参考）」列は
同じデータタイプのブロックを実 `.xlsx` から作ってはいるが `getDataType()` 自体は見ていないテストである。
後者は担保として数えない（重複テストを避けるための索引として載せている）。
参考列の括弧書きは、そのテストが**軸A について**何を見ていないかを示すためのものであり、
アサーション全体の要約ではない（各テストは自分の担当軸要素については別途アサートしている）。

| 要素 | `getDataType()` をアサート（＝担保） | 同経路を通過（参考） |
|---|---|---|
| A-01 `DEFAULT` | —（到達不能。§1.2 と同じ） | — |
| A-02 `SETUP_TABLE_DATA` | `readsSetupTableBlockFromRealBook` | `readsEmptyColumnNamesFromMarkerOnlyTableInRealBook`, `readsContainerAndSectionNamesFromRealBookAndSheetNames`, `readsFourBlockImplementationsFromOneRealSheet`（3 件とも `getDataType()` は見ない。最後の 1 件は実装クラスと識別子を見る） |
| A-03 `EXPECTED_TABLE_DATA` | `readsExpectedTableBlockWithGroupIdFromRealBook` | — |
| **A-04 `EXPECTED_COMPLETED`** | `readsExpectedCompletedTableBlockFromRealBook`（#18 では 🔺 `RoundTripTest` のみ → ✅） | — |
| A-05 `LIST_MAP` | `readsListMapBlockFromRealBook` | `readsEmptyColumnNamesFromMarkerOnlyListMapInRealBook`, `readsFourBlockImplementationsFromOneRealSheet`（2 件とも `getDataType()` は見ない） |
| A-06 `SETUP_FIXED` | `readsSetupFixedFileBlockFromRealBook` | `readsOmittedFieldLengthNotationFromRealBook`, `readsOmittedRecordTypeAsEmptyStringFromRealBook`, `readsFourBlockImplementationsFromOneRealSheet`（3 件とも `getDataType()` は見ない。前 2 件は軸C の `length` 省略・`recordType` 省略が担当） |
| **A-07 `EXPECTED_FIXED`** | `readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook`（#18 では 🔺 → ✅） | — |
| A-08 `SETUP_VARIABLE` | `readsSetupVariableFileBlockWithoutFieldLengthFromRealBook` | — |
| **A-09 `EXPECTED_VARIABLE`** | `readsExpectedVariableFileBlockWithGroupIdFromRealBook`（#18 では 🔺 → ✅） | — |
| A-10 `MESSAGE` | `readsMessageBlockFromRealBook` | `readsFourBlockImplementationsFromOneRealSheet` |
| A-11〜A-14 送信同期 4 種 | `readsAllFourSendSyncMessageTypesFromRealBook`（識別子を `RM01`〜`RM04` と別々にし、データタイプ・グループ ID・識別子・ディレクティブを 1 対 1 で突き合わせる） | — |

**軸B** — B-1〜B-4 のすべてを `readsFourBlockImplementationsFromOneRealSheet` が 1 シートから生成し、
実装クラスに加えて識別子（`T` / `lm` / `f.dat` / `m`）まで突き合わせる。各種別の個別テストも
`onlyBlock(Class)` ヘルパで実装クラスを確認している。

**軸C（#18 §1.2 から状態が変わったものだけ。根拠は `coverage/issues.md` の課題 ID）**

| 要素 | #18 の判定 | #21 後 | 担保テストメソッド／根拠 |
|---|---|---|---|
| C-06 `groupId` 省略(`""`) | 🔺（`RoundTripTest` のみ） | ✅ | `readsSetupTableBlockFromRealBook`, `readsListMapBlockFromRealBook`, `readsMessageBlockFromRealBook` |
| C-08 `columnNames` 空 | ❌ | ✅ | `readsEmptyColumnNamesFromMarkerOnlyTableInRealBook`, `readsEmptyColumnNamesFromMarkerOnlyListMapInRealBook`（`issues.md` XLS-08） |
| C-11 `FileDataBlock.directives` 空 | ❌ | **到達不能** | `issues.md` XLS-07。根拠テスト `readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook` |
| C-13 `MessageDataBlock.directives` 空 | ❌ | **到達不能** | `issues.md` XLS-07。根拠テスト `readsAllFourSendSyncMessageTypesFromRealBook` |
| C-16 `recordType` 省略(`null`) | ❌ | **到達不能** | `issues.md` XLS-06。根拠テスト `readsOmittedRecordTypeAsEmptyStringFromRealBook` |
| C-17 `RecordLayout.fields` 空 | ❌ | **到達不能** | `issues.md`「到達不能」表（名前行が 2 列未満だと本体パーサが失敗する） |
| C-20 `FieldDef.type` 省略(`null`) | ❌ | **到達不能** | `issues.md`「到達不能」表（型の欠落は本体パーサが 2 通りの機構で弾く） |
| C-21 `length` 値あり（省略記法 `-`） | ✅（Fake 経路のみ） | ✅（実 `.xlsx` 経路も） | `readsOmittedFieldLengthNotationFromRealBook` |
| C-09 `rows` 空 | ❌ | ✅（**#21**） | `readsEmptyRowsFromTableWithoutDataRowsInRealBook`（テーブル経路）／`readsEmptyRowsFromListMapWithoutDataRowsInRealBook`（LIST_MAP 経路） |
| C-12 `FileDataBlock.records` 空 | ❌ | ✅（**#21**） | `readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook` |
| C-15 `MessageDataBlock.records` 空 | ❌ | ✅（**#21**） | `readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook` |
| C-18 `RecordLayout.rows` 空 | ❌ | ✅（**#21**） | `readsEmptyRowsFromRecordLayoutWithoutValueRowsInRealBook` |

**軸D** — D1-01〜D1-17 の 17 ケースすべてを #19 の `XlsFormatReaderCellTypeTest` が実 `.xlsx` で担保した
（`issues.md` XLS-01〜XLS-05）。#18 で「全滅」としていた空欄は埋まっている。

**軸E（#21。0 件は実 `.xlsx` 経路で新規担保。1 件／複数件の既存担保も併記する）**

| 要素 | 0 件 | 1 件 | 複数件 | 担保テストメソッド |
|---|---|---|---|---|
| E-1 セクション内ブロック数 | ✅ | ✅ | ✅ | 0: `XlsFormatReaderRealFileTest#readsEmptyBlockListFromRealSheetWithoutMarkers`（#20）／1: 同クラスの単一ブロックのテスト多数／複数: `#readsFourBlockImplementationsFromOneRealSheet`（4 件）・`#readsAllFourSendSyncMessageTypesFromRealBook`（4 件） |
| E-2 ブロック内行数 | ✅（**#21**） | ✅ | ✅ | 0: `#readsEmptyRowsFromTableWithoutDataRowsInRealBook`／`#readsEmptyRowsFromListMapWithoutDataRowsInRealBook`／1: `#readsExpectedTableBlockWithGroupIdFromRealBook`（1 行）／複数: `#readsSetupTableBlockFromRealBook`（2 行） |
| E-3 ファイル内レコードレイアウト数 | ✅（**#21**） | ✅ | ✅（**#21** で実 `.xlsx` 経路も） | 0: `#readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook`（ファイル系）・`#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook`（メッセージ系）／1: `#readsSetupFixedFileBlockFromRealBook`・`#readsMessageBlockFromRealBook`（いずれも `records.size()==1` をアサート）／複数: **`#readsMultipleRecordLayoutsFromOneFixedFileInRealBook`**（断片 2 件。Fake リーダ経路には `XlsFormatReaderTest#readRestoresMultipleRecordLayoutsInFixedFile` がある） |
| E-4 コンテナ内セクション数 | n/a | ✅ | ❌（**到達不能**） | 1: `#readsContainerAndSectionNamesFromRealBookAndSheetNames`。複数は `XlsFormatReader#read` L133 が `Collections.singletonList(section)` を返すため構造上到達不能（§0.8-6） |

- **アサート済みのセルは 10 個ある。**内訳は E-1〜E-3 の 3 観点 × 0／1／複数 ＝ **9 組**（すべて実 `.xlsx` 経路）と、
  E-4(1) の 1 組。残るセルは E-4(複数)＝到達不能で空欄、E-4(0)＝n/a（コンテナは必ず 1 セクションを持つ）の 2 つで、
  合わせて 4 観点 × 3 多重度 ＝ 12 セルになる。
  #21 の当初実装では E-3(複数) を Fake リーダ経路の ✅（#18 判定）に依拠して空けていたが、
  **辺①の担保は実ファイル経路で揃えるという #20 の基準に合わせ、コーディネータの指示で追加した**。
- **E-3(複数) は `MessageDataBlock` 経路では到達不能**である。本体 `MessageParser` が生成する
  `FixedLengthFileParser` は `onReadingValues` を上書きし、先頭セルが非空でも新しい断片を作らないため、
  2 つ目の名前行が値行として吸収される（`issues.md` **XLS-15**。根拠テスト
  `XlsFormatReaderInvalidInputTest#absorbsSecondNameRowAsDataRowInMessageBodyInRealBook`）。
  E-3 は（観点, 多重度）単位で数えるため、ファイル系での担保をもって E-3(複数) は ✅ とする。

**軸F（#21。6 ケース中 5 ケースを新規担保。F1-05 は #16 で担保済み）**

| 要素 | 判定 | 担保テストメソッド（`XlsFormatReaderInvalidInputTest#`。F1-05 のみ別クラス） | 観測した挙動 |
|---|---|---|---|
| F1-01 シート不在 | ✅（**#21**） | `failsWithSheetNotFoundWhenSheetIsAbsentFromRealBook` | `IllegalArgumentException: sheet not found. path=[...] sheet=[...]`（原因例外なし） |
| F1-02 ブック破損 | ✅（**#21**） | `failsWithGenericRuntimeExceptionWhenWorkbookIsBroken` | `java.lang.RuntimeException: test data file open failed.` ＋ POI の `IllegalArgumentException`。ファイル名はどのメッセージにも出ない（`issues.md` **XLS-14**） |
| F1-03 未知のデータタイプ名 | ✅（**#21**。#18 は 🔺） | `ignoresBlockWhoseMarkerHasUnknownDataTypeNameInRealBook`／`readsSuffixAfterKnownDataTypeNameAsGroupIdInRealBook` | 例外にならず継続。未知名はブロックごと消える（**XLS-10**）。既知名＋余分な文字はグループ ID になる（**XLS-11**） |
| F1-04 マーカーカラム欠落 | ✅（**#21**） | `readsMarkerColumnWithoutBracketsAsOrdinaryDataColumnInRealBook`／`dropsFirstFieldWhenSendSyncMetaColumnIsMissingInRealBook` | 例外にならず継続。角括弧なしの列はデータ列になる。送信同期のメタ列欠落は先頭フィールドと値を落とす（**XLS-13**） |
| F1-05 カラム名重複 | ✅（**#21** で実 `.xlsx` 経路も） | `deduplicatesDuplicateColumnNamesWithWarningInListMapFromRealBook`／`deduplicatesDuplicateColumnNamesWithWarningInTableFromRealBook`（**#21**）。#16 の `XlsFormatReaderTest#readListMapWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins` ほか 3 件は **Fake リーダ経路** | 後勝ちで除去＋WARN ログ 1 件（ブック名・シート名・ブロック識別子・カラム名・採用列番号を含む）。実 `.xlsx` 経路でも Fake 経路と同じ結果になることを実測 |
| F1-06 行と列の数の不一致 | ✅（**#21**） | `padsShortDataRowAndDropsCellsBeyondColumnRowInRealBook`／`padsShortValueRowAndDropsCellsBeyondNameRowInFixedFileInRealBook`／`failsWhenLengthRowIsShorterThanNameRowInRealBook`／`failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook`／`failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook`／`failsWhenTypeRowIsShorterThanNameRowInRealBook`／`failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook` | **値行**の不足は空文字埋め・超過は切り捨て（例外にならない。**XLS-12**）。**名前行・型行・長さ行**の不整合は本体パーサが例外で弾く |
| （軸F の要素ではない補足） | — | `absorbsSecondNameRowAsDataRowInMessageBodyInRealBook` | `MESSAGE` 本文に 2 つ目のレコードレイアウトを書くと、名前行・型行・長さ行がすべて 1 つ目の値行として吸収される（**XLS-15**。E-3(複数) がメッセージ系で到達不能である根拠） |

- 末尾 4 メソッド（`failsWhenFixedFileNameRowHasOnlyRecordTypeCell...`／`failsWhenMessageNameRowHasOnlyRecordTypeCell...`／
  `failsWhenTypeRowIsShorterThanNameRow...`／
  `failsWhenTypeCellIsBlankInMiddleOfTypeRow...`）は、軸C の **C-17／C-20 が到達不能である根拠**を
  実行可能にするテストでもある（`issues.md` の「到達不能」表が参照している）。

<a id="s1-3"></a>

### 1.3 辺① 未担保一覧（#19〜#21 が埋める対象）

**計上単位**（4 辺すべてでこの規則に従う）:

- 軸A・軸B・軸C は **軸要素 1 件を 1 件**と数える。同一要素で「値あり」「省略」の両方が欠けていても 1 件とし、
  欠けている状態は要素名に併記する。
- 軸D・軸F は **ケース 1 件を 1 件**と数える。
- 軸E は **（観点, 多重度）の組 1 件を 1 件**と数える。
- 🔺（弱い担保のみ）の要素は未担保として計上する。正式担保は ✅ のみ。

**状態**の分類（**`担保済み` を #20 で追加した**）:

- `要追加` — テストを書く対象。**#19／#20／#21 の完了により 0 件になった。**
- `担保済み` — #19／#20／#21 が実 `.xlsx` を入力とするテストで埋めた。担保テストメソッド名を併記する。
- `到達不能` — 構造上その状態が生成されない。根拠を併記する。テストは書かず、理由付きで空欄に残す。
- `対象外` — 辺の担当クラスの関心事ではなく、上位層の関心事であるもの。理由を併記する。
  §5.1 では `対象外（上位層で担保済み）` と表記しているが、**辺③の F3-02 についてはこの表記は正確でない**
  （上位層の既存テストが通すのは XLS→YAML の経路だけで、`.xlsx` を出力側とする衝突は未担保）。
  §3.1-2／§3.2／§3.3 では `対象外（衝突検査は上位層）` へ改めた。詳細は §0.8-5 の訂正欄。

**本表は #21 の実測結果に合わせて更新した（2026-08-12）。** #18 時点は「要追加 38 ／ 到達不能 3」、
#20 完了時点は「要追加 11 ／ 担保済み 22 ／ 到達不能 8」であった。#21 が残る 11 件（C 4 件・E 2 件・F 5 件）を
埋めたため、**辺①の未担保は 0 件**になった。分類を変更した行には根拠（`coverage/issues.md` の
課題 ID）を併記してある。#18 時点の分類は各行の「#18」列に残した。

| 軸 | 未担保要素 | #18 の状態 | #21 後の状態 | 件数 |
|---|---|---|---|---|
| A | A-04 `EXPECTED_COMPLETED`／A-07 `EXPECTED_FIXED`／A-09 `EXPECTED_VARIABLE` | 要追加 | **担保済み（#20）** — 順に `XlsFormatReaderRealFileTest#readsExpectedCompletedTableBlockFromRealBook`／`#readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook`／`#readsExpectedVariableFileBlockWithGroupIdFromRealBook` | 3 |
| A | A-01 `DEFAULT` — `TestCoreReaderAdapter` L362 が DEFAULT ブロックをスキップするためリーダ経路で生成されない（§0.8-7） | 到達不能 | 到達不能（変更なし） | 1 |
| B | （なし） | — | — | — | 0 |
| C | C-06 groupId 省略(`""`) | 要追加 | **担保済み（#20）** — `#readsSetupTableBlockFromRealBook` ほか 2 件が `""` を直接アサート | 1 |
| C | C-08 columnNames 空 | 要追加 | **担保済み（#20 修正ラウンド）** — `#readsEmptyColumnNamesFromMarkerOnlyTableInRealBook`／`#readsEmptyColumnNamesFromMarkerOnlyListMapInRealBook`。マーカー列だけのブロックで到達する（`issues.md` **XLS-08**）。**#20 の当初分類では「軸E の 0 件と重なる」として #21 送りにしていたが誤り**（軸E の 4 観点 E-1〜E-4 に「列名 0 件」に対応する要素は無い）。#18 §1.3 は本要素を「要追加」に列挙しただけで、どのタスクが埋めるかは指定していない | 1 |
| C | C-09 rows 空／C-12 FileDataBlock.records 空／C-15 MessageDataBlock.records 空／C-18 RecordLayout.rows 空 | 要追加 | **担保済み（#21）** — `XlsFormatReaderRealFileTest#readsEmptyRowsFromTableWithoutDataRowsInRealBook`／`#readsEmptyRowsFromListMapWithoutDataRowsInRealBook`（C-09 は 2 経路）／`#readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook`（C-12）／`#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook`（C-15）／`#readsEmptyRowsFromRecordLayoutWithoutValueRowsInRealBook`（C-18）。いずれも例外にならず空コレクションになることを実測して固定した | 4 |
| C | C-11 FileDataBlock.directives 空／C-13 MessageDataBlock.directives 空 | 要追加 | **到達不能** — 本体 `DataFile` コンストラクタ L92 が `file-type` を必ず注入する（`issues.md` **XLS-07**）。根拠は `#readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook`／`#readsAllFourSendSyncMessageTypesFromRealBook` がテストで示す | 2 |
| C | C-16 recordType 省略(`null`) | 要追加 | **到達不能** — 実 `.xlsx` 経路では空セルが `""` として読まれる（`issues.md` **XLS-06**）。根拠は `#readsOmittedRecordTypeAsEmptyStringFromRealBook` | 1 |
| C | C-17 RecordLayout.fields 空 | 要追加 | **到達不能** — 名前行が 2 列未満だと本体 `DataFileParser` L234 が失敗する（`issues.md`「到達不能」表）。根拠は **#21 が追加した** `XlsFormatReaderInvalidInputTest#failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook` と `#failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook` がテストで示す。**#20 の当初分類では #21 送りにしていたが誤り**（軸E の 0 件ではない）。#18 §1.3 は本要素を「要追加」に列挙しただけで、どのタスクが埋めるかは指定していない | 1 |
| C | C-20 FieldDef.type 省略(`null`) | 要追加 | **到達不能** — 型の欠落を本体パーサが 2 通りの機構で弾く（`issues.md`「到達不能」表）。根拠は **#21 が追加した** `XlsFormatReaderInvalidInputTest#failsWhenTypeRowIsShorterThanNameRowInRealBook`（機構①）／`#failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook`（機構②）がテストで示す | 1 |
| C | C-02 sections 空 — `XlsFormatReader#read` L133 が `Collections.singletonList(section)` を返すため sections は常に 1 件（§0.8-6） | 到達不能 | 到達不能（変更なし） | 1 |
| D | D1-01〜D1-17 全 17 ケース | 要追加 | **担保済み（#19）** — `XlsFormatReaderCellTypeTest` 19 件（17 ケース＋空白セル＋行途中の不在セル）。Excel 保存物との突き合わせは `XlsReferenceFixtureTest` 2 件（`issues.md` XLS-01〜XLS-05） | 17 |
| E | E-2(0 件)／E-3(0 件) | 要追加 | **担保済み（#21）** — E-2(0) は C-09 の 2 件と同じ入力、E-3(0) は C-12／C-15 と同じ入力（上記 C 行のテストメソッド） | 2 |
| E | E-4(複数) — `XlsFormatReader#read` が 1 シート単位 API（§0.8-6） | 到達不能 | 到達不能（変更なし） | 1 |
| F | F1-01 シート不在／F1-02 ブック破損／F1-03 未知データタイプ名（🔺 `readIgnoresDataTypePrefixedLineWithoutMarker` のみ）／F1-04 マーカーカラム欠落／F1-06 行列数不一致 | 要追加 | **担保済み（#21）** — `XlsFormatReaderInvalidInputTest` 16 件（内訳: 本 5 ケースが 9 件、F1-05 の実 `.xlsx` 担保が 2 件、C-17／C-20 到達不能の根拠が 4 件、XLS-15 の根拠が 1 件。§1.2-2 の軸F 表に要素別の担保テストメソッドを記載）。継続する異常系で見つけた課題は `issues.md` **XLS-10〜XLS-15** | 5 |
| **合計** | | **要追加 38 ／ 到達不能 3** | **要追加 0 ／ 担保済み 33 ／ 到達不能 8 ／ 対象外 0** | **41** |

**合計の検算**（表の「件数」列を上から順に足す）:

- 担保済み: A 3 ＋ C-06 1 ＋ C-08 1 ＋ C-09/12/15/18 4 ＋ D 17 ＋ E-2(0)/E-3(0) 2 ＋ F 5 ＝ **33**
- 到達不能: A-01 1 ＋ C-11/C-13 2 ＋ C-16 1 ＋ C-17 1 ＋ C-20 1 ＋ C-02 1 ＋ E-4(複数) 1 ＝ **8**
- 要追加: **0**
- 総計: 33 ＋ 8 ＋ 0 ＝ **41**（B は 0 件）

**#21 が埋めた 11 件の内訳**（#20 が送った対象。すべて完了した）:

| 軸 | 要素 | 担保テストメソッド |
|---|---|---|
| C | C-09 `ColumnRowDataBlock.rows` 空 | `XlsFormatReaderRealFileTest#readsEmptyRowsFromTableWithoutDataRowsInRealBook`／`#readsEmptyRowsFromListMapWithoutDataRowsInRealBook`（E-2(0 件) と同じ入力） |
| C | C-12 `FileDataBlock.records` 空 | `#readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook`（E-3(0 件) と同じ入力） |
| C | C-15 `MessageDataBlock.records` 空 | `#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook`（E-3(0 件) と同じ入力） |
| C | C-18 `RecordLayout.rows` 空 | `#readsEmptyRowsFromRecordLayoutWithoutValueRowsInRealBook` |
| E | E-2(0 件)／E-3(0 件) | 上記 C-09／C-12・C-15 と同じ 2 件・3 件 |
| F | F1-01／F1-02／F1-03／F1-04／F1-06 | `XlsFormatReaderInvalidInputTest` 16 件（§1.2-2 の軸F 表） |

**#21 が追加で埋めた 2 件（上記 11 件の外）**: いずれも #18 の棚卸しが **Fake リーダ経路の担保をもって ✅**
と判定していたため §1.3 の未担保一覧（41 件）には現れないが、**辺①の担保は実ファイル経路で揃える**という
#20 の基準に照らすと空欄だったものである。コーディネータの指示により追加した。本表の件数（41）と合計には影響しない。

| 要素 | 実 `.xlsx` 経路の担保（#21 で追加） | #18 が ✅ とした Fake 経路の担保 |
|---|---|---|
| E-3(複数) | `XlsFormatReaderRealFileTest#readsMultipleRecordLayoutsFromOneFixedFileInRealBook` | `XlsFormatReaderTest#readRestoresMultipleRecordLayoutsInFixedFile` |
| F1-05 カラム名重複 | `XlsFormatReaderInvalidInputTest#deduplicatesDuplicateColumnNamesWithWarningInListMapFromRealBook`／`#deduplicatesDuplicateColumnNamesWithWarningInTableFromRealBook` | `XlsFormatReaderTest#readListMapWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins` ほか 3 件（#16） |

- **C-08 と C-17 は #21 のスコープから外れる**（前者は #20 で担保済み、後者は到達不能）。
  #18 §1.3 の「要追加 11（C）」＝ C-06／C-08／C-09／C-11／C-12／C-13／C-15／C-16／C-17／C-18／C-20 の
  内訳は、#20 の実測により次の 3 群に分かれる。

  | 群 | 要素 | 件数 |
  |---|---|---|
  | 担保済み（#20） | C-06／C-08 | 2 |
  | 到達不能 | C-11／C-13／C-16／C-17／C-20 | 5 |
  | 担保済み（**#21**。#20 の時点では「要追加」） | C-09／C-12／C-15／C-18 | 4 |
  | **合計** | | **11** |

  2 ＋ 5 ＋ 4 ＝ 11。末尾の 4 件が上表の C 4 件であり、#21 で担保済みになった。

**#18 時点の「特に大きな空欄」**（軸D 17 ケース全滅）は #19 で解消し、#20 後に残っていた最大の空欄
（軸F の 5 ケース）は #21 で解消した。**辺①の「要追加」は 0 件**である。残る 8 件は到達不能で、
うち 5 件（C-11／C-13／C-16／C-17／C-20）は根拠テストを持ち、3 件（A-01／C-02／E-4(複数)）は
`XlsFormatReader#read` と `TestCoreReaderAdapter` の構造そのものが根拠である。

**ただし「未担保 0 件」は本書の計上単位（§1.3 冒頭）での話である。** 次の 1 点は空欄として残る。

- 継続する異常系（F1-03／F1-04／F1-06 の一部）で「WARN が出ないこと」はアサートしていない
  （`issues.md`「未確認（#21）」）。

なお #18 の棚卸しが Fake リーダ経路の担保をもって ✅ としていた軸E の要素のうち、実 `.xlsx` 経路で
空欄だったのは **E-3(複数) の 1 件だけ**である（#21 で追加。§1.2-2 の軸E 表）。
E-1(0/1/複数)・E-2(1/複数)・E-3(1)・E-4(1) は `XlsFormatReaderRealFileTest` が実 `.xlsx` で
アサートしていることを 1 件ずつ確認した。

---

<a id="s2"></a>

## 2. 辺② YAML→中間モデル（`YamlFormatReaderTest` 20 件）

### 2.1 テストメソッド → 担保する軸要素

| # | テストメソッド | 軸A | 軸B | 軸C | 軸D | 軸E | 軸F |
|---|---|---|---|---|---|---|---|
| 1 | `readTable_setup_mapsUppercaseNameAndColumnsWithRawValues` | A-02 | B-1 | C-05, C-06(省略=`""`), C-07, C-08, C-09 | — ※Map 値レベルで `${}`／null／`""` | E-1(1), E-2(複数=2) | — |
| 2 | `readTable_expectedWithGroup_formatsGroupIdAndCreatesBlockPerGroup` | A-03 | B-1 | C-05, C-06(値あり), C-09 | — | E-1(複数=2) | — |
| 3 | `readTable_completed_mapsExpectedCompletedType` | A-04 | B-1 | C-05, C-07 | — | E-1(1) | — |
| 4 | `readListMap_preservesYamlColumnOrderExcludesMarkersAndKeepsNull` | A-05 | B-2 | C-05, C-07, C-08(YAML 順), C-09 | — ※マーカーカラム `[ignore]` 除外・null 保持 | E-2(複数=2) | — |
| 5 | `readFile_fixed_mapsRawFieldDefsAndValues` | A-06 | B-3 | C-05, C-07, C-10(FIXED), C-11(値あり), C-12, C-16(値あり), C-17, C-18, C-19, C-20, C-21(値あり＋省略) | — ※`${}`／`""` | E-3(複数=2), E-2(複数=2) | — |
| 6 | `readFile_variable_mapsVariableTypeWithNullLengths` | A-09 | B-3 | C-05, C-10(VARIABLE), C-17, C-18, C-19, C-20, C-21(省略) | — | E-3(1) | — |
| 7 | `readFile_expectedFixedWithMultipleGroups_mapsExpectedFixedAndDedupesGroups` | A-07 | B-3 | C-05, C-06(値あり), C-07 | — | E-1(複数=3) | — |
| 8 | `readFile_setupVariable_mapsSetupVariableType` | A-08 | B-3 | C-05, C-10(VARIABLE) | — | E-1(1) | — |
| 9 | `readFile_recordTypeOmitted_keepsNullRecordType` | A-06 | B-3 | **C-16(省略=null)** ✅ | — | E-3(1) | — |
| 10 | `readFile_recordTypeDefault_normalizedToNull` | A-08 | B-3 | C-16(`"Default"`→null) | — ※特殊値の正規化 | E-3(1) | — |
| 11 | `readMessage_mapsRawFwHeaderAndExcludesFwHeaderRecord` | A-10 | B-4 | C-05, C-06(省略), C-07, C-14(値あり), C-15, C-16, C-17, C-18, C-19, C-20, C-21 | — ※`${}` | E-3(1) | — |
| 12 | `readMessage_emptyBody_isStillMapped` | A-10 | B-4 | C-07, **C-14(空)**, **C-15(空)** | — | **E-3(0)** ✅ | — |
| 13 | `readMessage_nullContent_isSkipped` | — | — | C-04(空) | — | E-1(0) | ※器が null を返す場合のスキップ |
| 14 | `readSendSync_groupsByRawValueFormatsGroupIdAndKeepsNoField` | A-11 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-17, C-18, C-19, C-20, C-21 | — ※`${}`・`no` フィールド保持 | E-1(複数=3) | — |
| 15 | `readSendSync_allFourTypesAreRecognized` | A-11, A-12, A-13, A-14 | B-4 | C-05 | — | E-1(複数=4) | — |
| 16 | `readSendSync_entryWithoutGroupId_isDropped` | A-14 | B-4 | C-07 | — | E-1(1) | 🔺**F2-04** に近い（送信系必須の `group_id` 欠落エントリを drop） |
| 17 | `read_mixedSections_keepsDescriptionOrderAndIgnoresUnknownKeys` | A-02, A-10 | B-1, B-4 | C-04 | — | E-1(複数=2) | **F2-03** ✅（未知キー無視） |
| 18 | `read_namesContainerAndSectionByResourceName` | — | — | C-01, C-02(1件), C-03, **C-04(空)** | — | E-1(0), E-4(1) | 🔺**F2-05** に近い（空 Map。実ファイルではない） |
| 19 | `read_containerCountMismatch_failsFast` | A-06 | B-3 | — | — | — | ✅ 器↔原文の件数不整合 → `IllegalStateException` |
| 20 | `read_fragmentRecordMismatch_failsFast` | A-06 | B-3 | — | — | — | ✅ 器の断片構造↔原文レコード不整合 → `IllegalStateException` |

### 2.2 軸要素 → 担保テストメソッド

**軸A（✅ 13 ／ 🔺 0 ／ ❌ 1 ＝ 14）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| A-01 `DEFAULT` | ❌ | —（到達不能。`YamlFormatReader#addBlocksForSection` L106-133 ／ `fileDataType` L534-536 ／ `addMessageBlocks` L264 は `DEFAULT` 以外の 13 種のみを分岐に持ち、`DEFAULT` を返す経路がない。§0.8-7） |
| A-02 `SETUP_TABLE_DATA` | ✅ | `readTable_setup_mapsUppercaseNameAndColumnsWithRawValues`, `read_mixedSections_keepsDescriptionOrderAndIgnoresUnknownKeys` |
| A-03 `EXPECTED_TABLE_DATA` | ✅ | `readTable_expectedWithGroup_formatsGroupIdAndCreatesBlockPerGroup` |
| A-04 `EXPECTED_COMPLETED` | ✅ | `readTable_completed_mapsExpectedCompletedType` |
| A-05 `LIST_MAP` | ✅ | `readListMap_preservesYamlColumnOrderExcludesMarkersAndKeepsNull` |
| A-06 `SETUP_FIXED` | ✅ | `readFile_fixed_mapsRawFieldDefsAndValues`, `readFile_recordTypeOmitted_keepsNullRecordType`, `read_containerCountMismatch_failsFast`, `read_fragmentRecordMismatch_failsFast` |
| A-07 `EXPECTED_FIXED` | ✅ | `readFile_expectedFixedWithMultipleGroups_mapsExpectedFixedAndDedupesGroups` |
| A-08 `SETUP_VARIABLE` | ✅ | `readFile_setupVariable_mapsSetupVariableType`, `readFile_recordTypeDefault_normalizedToNull` |
| A-09 `EXPECTED_VARIABLE` | ✅ | `readFile_variable_mapsVariableTypeWithNullLengths` |
| A-10 `MESSAGE` | ✅ | `readMessage_mapsRawFwHeaderAndExcludesFwHeaderRecord`, `readMessage_emptyBody_isStillMapped`, `read_mixedSections_keepsDescriptionOrderAndIgnoresUnknownKeys` |
| A-11 `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `readSendSync_groupsByRawValueFormatsGroupIdAndKeepsNoField`, `readSendSync_allFourTypesAreRecognized` |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `readSendSync_allFourTypesAreRecognized` |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅ | `readSendSync_allFourTypesAreRecognized` |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅ | `readSendSync_allFourTypesAreRecognized`, `readSendSync_entryWithoutGroupId_isDropped` |

**軸B（✅ 4 ／ 4）**

| 要素 | 判定 | 担保テストメソッド（代表） |
|---|---|---|
| B-1 `TableDataBlock` | ✅ | `readTable_setup_mapsUppercaseNameAndColumnsWithRawValues`, `readTable_completed_mapsExpectedCompletedType` ほか 2 件（計 4 件） |
| B-2 `ListMapBlock` | ✅ | **`readListMap_preservesYamlColumnOrderExcludesMarkersAndKeepsNull`**（辺②で唯一） |
| B-3 `FileDataBlock` | ✅ | `readFile_fixed_mapsRawFieldDefsAndValues`, `readFile_variable_mapsVariableTypeWithNullLengths` ほか 6 件（計 8 件） |
| B-4 `MessageDataBlock` | ✅ | `readMessage_mapsRawFwHeaderAndExcludesFwHeaderRecord`, `readMessage_emptyBody_isStillMapped` ほか 4 件（計 6 件） |

**軸C（21 フィールド ─ 両状態担保 12 ／ 未担保 9）**

省略可能フィールドは「値あり」「省略」、空許容コレクションは「非空」「空」を別々に評価する。
n/a 6 件（C-01, C-03, C-05, C-07, C-10, C-19）は「省略」「空」という状態を持たない必須スカラー／2 値であり、
「値あり」の担保をもって両状態担保として数える。

| 要素 | 値あり／非空 | 省略／空 | 担保テストメソッド |
|---|---|---|---|
| C-01 `TestDataContainer.name` | ✅ | n/a | `read_namesContainerAndSectionByResourceName` |
| C-02 `TestDataContainer.sections` | ✅(1件) | ❌ 空（**到達不能**） | `read_namesContainerAndSectionByResourceName`（`YamlFormatReader#read` L94 が `Collections.singletonList(section)` を返すため、辺②では sections は常に 1 件。§0.8-6） |
| C-03 `TestDataSection.name` | ✅ | n/a | `read_namesContainerAndSectionByResourceName` |
| C-04 `TestDataSection.blocks` | ✅ | ✅ 空 | 空: `read_namesContainerAndSectionByResourceName`, `readMessage_nullContent_isSkipped` |
| C-05 `TestDataBlock.dataType` | ✅ | n/a | `readTable_setup_...`, `readTable_expectedWithGroup_...` ほか 9 件（計 11 件） |
| C-06 `TestDataBlock.groupId` | ✅ | ✅ 省略(`""`) | 値あり: `readTable_expectedWithGroup_...`, `readFile_expectedFixedWithMultipleGroups_...` ほか 1 件（計 3 件）／省略: `readTable_setup_...`, `readMessage_mapsRawFwHeader...` |
| C-07 `TestDataBlock.identifier` | ✅ | n/a | `readTable_setup_...`, `readTable_completed_...` ほか 7 件（計 9 件） |
| C-08 `ColumnRowDataBlock.columnNames` | ✅ | ❌ 空 | 非空: `readTable_setup_...`, `readListMap_preservesYamlColumnOrder...`（計 2 件） |
| C-09 `ColumnRowDataBlock.rows` | ✅ | ❌ 空 | 非空: `readTable_setup_...`, `readTable_expectedWithGroup_...` ほか 1 件（計 3 件） |
| C-10 `FileDataBlock.fileType` | ✅ FIXED / ✅ VARIABLE | n/a | FIXED: `readFile_fixed_...`／VARIABLE: `readFile_variable_...`, `readFile_setupVariable_...` |
| C-11 `FileDataBlock.directives` | ✅ | ❌ 空 | 非空: `readFile_fixed_mapsRawFieldDefsAndValues` のみ |
| C-12 `FileDataBlock.records` | ✅ | ❌ 空 | 非空: `readFile_fixed_...` |
| C-13 `MessageDataBlock.directives` | ❌ | ❌ | **アサートするテストが 0 件**（`getDirectives()` の呼び出しは `YamlFormatReaderTest` L164 の FileDataBlock のみ） |
| C-14 `MessageDataBlock.fwHeaderFields` | ✅ | ✅ 空 | 非空: `readMessage_mapsRawFwHeaderAndExcludesFwHeaderRecord`／空: `readMessage_emptyBody_isStillMapped`, `readSendSync_groupsByRawValue...` |
| C-15 `MessageDataBlock.records` | ✅ | ✅ 空 | 非空: `readMessage_mapsRawFwHeader...`／空: `readMessage_emptyBody_isStillMapped` |
| C-16 `RecordLayout.recordType` | ✅ | ✅ 省略(null) | 値あり: `readFile_fixed_...`, `readMessage_mapsRawFwHeader...`／省略: `readFile_recordTypeOmitted_keepsNullRecordType`, `readFile_recordTypeDefault_normalizedToNull` |
| C-17 `RecordLayout.fields` | ✅ | ❌ 空 | 非空: `readFile_fixed_...`, `readFile_variable_...` ほか 2 件（計 4 件） |
| C-18 `RecordLayout.rows` | ✅ | ❌ 空 | 非空: `readFile_fixed_...`, `readFile_variable_...` ほか 2 件（計 4 件） |
| C-19 `FieldDef.name` | ✅ | n/a | `readFile_fixed_...`, `readFile_variable_...` ほか 2 件（計 4 件） |
| C-20 `FieldDef.type` | ✅ | ❌ 省略(null) | 値あり: `readFile_fixed_...`（`半角英字`/`数値`） |
| C-21 `FieldDef.length` | ✅ | ✅ 省略(null) | 値あり: `readFile_fixed_...`（`"5"`）／省略: `readFile_fixed_...`（`f2`）, `readFile_variable_...` |

**軸D（✅ 0 ／ 🔺 4 ／ ❌ 6 ＝ 10）**

D2-01〜D2-10 すべて ❌。理由: 既存 20 件は `YamlFormatReaderTest#reader`（L538-545）で `loadRawMap` を
in-memory `LinkedHashMap` に差し替えるため、YAML テキストのパースを一切通らない。スカラー型の解釈が発生しない。

🔺 弱い担保（実 YAML ファイルを通る往復テスト経由）:
- D2-02 引用符あり文字列: `YamlFormatWriterTest#roundTrip_table_isPreservedThroughRealReader`, `#roundTrip_fixedFile_isPreservedThroughRealReader` ほか 4 件（`roundTrip_*` 全 6 件）、`RoundTripTest` の YAML 経路 14 件と両経路 3 件（§0.8-8）
- D2-03 数値（`"123"`）: `YamlFormatWriterTest#roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader`
- D2-06 `null`: 同上、`RoundTripTest#yaml_setupTable_isPreserved`, `#yaml_listMap_withNullValue_isPreserved`, `#nullCell_xlsConvertsToLiteralString_yamlPreservesNull`（`~` と「値なし」は未担保）
- D2-07 `"null"`: `YamlFormatWriterTest#roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader`

`RoundTripTest` によって新たに 🔺 になる辺②の要素はない（§0.8-8）。

**軸E**

| 要素 | 0 件 | 1 件 | 複数件 | 担保テストメソッド |
|---|---|---|---|---|
| E-1 セクション内ブロック数 | ✅ | ✅ | ✅ | 0: `read_namesContainerAndSectionByResourceName`, `readMessage_nullContent_isSkipped`／1: `readTable_setup_...`, `readTable_completed_...` ほか 2 件（計 4 件）／複数: `readTable_expectedWithGroup_...`, `readFile_expectedFixedWithMultipleGroups_...` ほか 3 件（計 5 件） |
| E-2 ブロック内行数 | ❌ | ✅ | ✅ | 1: `readTable_completed_mapsExpectedCompletedType`（`rows` に 1 件のみ。§2.1 の軸E 欄には E-2(1) を明記していない）／複数: `readTable_setup_...`, `readListMap_preservesYamlColumnOrder...` ほか 1 件（計 3 件） |
| E-3 ファイル内レコードレイアウト数 | ✅ | ✅ | ✅ | 0: `readMessage_emptyBody_isStillMapped`／1: `readFile_variable_...`, `readFile_recordTypeOmitted_...` ほか 2 件（計 4 件）／複数: `readFile_fixed_mapsRawFieldDefsAndValues` |
| E-4 コンテナ内セクション数（辺②の実体: ディレクトリ内 YAML ファイル数） | n/a | ✅ | ❌（**到達不能**） | 1: `read_namesContainerAndSectionByResourceName`。複数は `YamlFormatReader#read`（L87-95）が 1 リソース単位 API で `Collections.singletonList(section)` を返すため構造上到達不能（§0.8-6） |

**軸F（✅ 1 ／ 🔺 2 ／ ❌ 2 ＝ 5）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| F2-01 スキーマ違反 | ❌ | —（`YamlTestDataValidatorTest` は別クラスであり本辺の対象外） |
| F2-02 YAML として不正 | ❌ | — |
| F2-03 未知のキー | ✅ | `read_mixedSections_keepsDescriptionOrderAndIgnoresUnknownKeys` |
| F2-04 必須構造の欠落 | 🔺 | `readSendSync_entryWithoutGroupId_isDropped`（送信系必須 `group_id` の欠落）、`readMessage_nullContent_isSkipped` |
| F2-05 空ファイル | 🔺 | `read_namesContainerAndSectionByResourceName`（空 Map。実ファイルではない） |
| （steering 外で担保済みの異常系） | ✅ | `read_containerCountMismatch_failsFast`, `read_fragmentRecordMismatch_failsFast`（器↔原文の不整合 fail-fast。steering の 5 ケースには含まれないが本辺固有の異常系として担保済み） |

<a id="s2-3"></a>

### 2.3 辺② 未担保一覧（#24 が埋める対象）

計上単位と「状態」の 3 分類は §1.3 の規則に従う。

| 軸 | 未担保要素 | 状態 | 件数 |
|---|---|---|---|
| A | （要追加はなし） | — | 0 |
| A | A-01 `DEFAULT` — `YamlFormatReader` の分岐に `DEFAULT` を返す経路がない（§0.8-7） | 到達不能 | 1 |
| B | （なし） | — | 0 |
| C | C-08 columnNames 空／C-09 rows 空／C-11 FileDataBlock.directives 空／C-12 FileDataBlock.records 空／**C-13 MessageDataBlock.directives（値あり・空の双方が欠）**／C-17 fields 空／C-18 RecordLayout.rows 空／C-20 FieldDef.type 省略 | 要追加 | 8 |
| C | C-02 sections 空 — `YamlFormatReader#read` L94 が `Collections.singletonList(section)` を返すため sections は常に 1 件（§0.8-6） | 到達不能 | 1 |
| D | D2-01〜D2-10 全 10 ケース（うち D2-02／D2-03／D2-06／D2-07 は往復テスト経由の 🔺 あり。§0.8-8 と上の「軸D」節を参照し重複を避けること） | 要追加 | 10 |
| E | E-2(0 件) | 要追加 | 1 |
| E | E-4(複数) — `YamlFormatReader#read` が 1 リソース単位 API（§0.8-6） | 到達不能 | 1 |
| F | F2-01 スキーマ違反／F2-02 不正 YAML／F2-04 必須構造欠落（🔺 のみ）／F2-05 空ファイル（🔺 のみ） | 要追加 | 4 |
| **合計** | | **要追加 23 ／ 到達不能 3 ／ 対象外 0** | **26（うち到達不能 3）** |

**特に大きな空欄**: 軸D 10 ケース全滅（実 YAML テキストを一度も通らないため）と、
`MessageDataBlock.directives`（C-13）が値あり・空の両方とも 0 件。

---

<a id="s3"></a>

## 3. 辺③ 中間モデル→Excel（`XlsFormatWriterTest` 40 件）

### 3.1 テストメソッド → 担保する軸要素

| # | テストメソッド | 軸A | 軸B | 軸C | 軸D | 軸E | 軸F |
|---|---|---|---|---|---|---|---|
| 1 | `writesTableBlock` | A-02 | B-1 | C-06(省略→`[]` なし), C-07, C-08, C-09 | 🔺D3-04 null→リテラル `"null"`／🔺D3-05 `""`→空セル（いずれも `getStringCellValue` のみ） | E-2(複数=2) | — |
| 2 | `metaRowContainsOnlyValueCells` | A-02 | B-1 | — | — | E-2(1) | — |
| 3 | `writesTableMarkerWithGroupId` | A-03 | B-1 | C-06(値あり) | — | E-2(1) | — |
| 4 | `writesExpectedCompleteTableMarker` | A-04 | B-1 | C-05 | — | E-2(1) | — |
| 5 | `writesListMapBlock` | A-05 | B-2 | C-07, C-08, C-09 | 🔺D3-05 | E-2(複数=2) | — |
| 6 | `tintsMarkerColumn` | A-02 | B-1 | — | ※マーカーカラム `[NOTE]` 記法 | — | — |
| 7 | `writesFixedFileBlock` | A-06 | B-3 | C-07, C-11(値あり), C-12, C-16(値あり), C-17, C-18, C-19, C-20, C-21(`"-"`/`"5"`) | ※長さ記法 `-` | E-3(1) | — |
| 8 | `writesVariableFileWithoutLengthRow` | A-08 | B-3 | C-10(VARIABLE 版面), C-11(空), C-21(省略) | — | E-3(1) | — |
| 9 | `writesMultipleRecordLayouts` | A-06 | B-3 | C-12(2件), C-11(空) | — | **E-3(複数=2)** | — |
| 10 | `rejectsNullRecordTypeOnSecondRecord` | A-06 | B-3 | C-16(null) | — | E-3(複数) | ✅ 2 レコード目 recordType null → `IllegalStateException` |
| 11 | `rejectsEmptyRecordTypeOnSecondRecord` | A-06 | B-3 | C-16(`""`) | — | E-3(複数) | ✅ 2 レコード目 recordType 空文字 → `IllegalStateException` |
| 12 | `allowsNullRecordTypeOnSingleRecord` | A-06 | B-3 | **C-16(省略=null)** ✅ | — | E-3(1) | — |
| 13 | `writesMessageBlock` | A-10 | B-4 | C-07, C-13(空), C-14(値あり), C-15, C-17, C-18, C-19, C-20, C-21 | — | E-3(1) | — |
| 14 | `writesSendSyncMessageWithSequenceNo` | A-11 | B-4 | C-06(値あり), C-07, C-13(空), C-14(空), C-15, C-18 | — | E-2(複数=2) | — |
| 15 | `appliesHeaderBackgroundColor` | A-02 | B-1 | — | — | — | — |
| 16 | `appliesTestShotsHeaderColor` | A-05 | B-2 | — | — | — | — |
| 17 | `appliesSetupHeaderColor` | A-02 | B-1 | — | — | — | — |
| 18 | `appliesExpectedHeaderColor` | A-03 | B-1 | — | — | — | — |
| 19 | `appliesOtherHeaderColorForMessage` | A-10 | B-4 | C-14(値あり) | — | — | — |
| 20 | `appliesOtherHeaderColorForNonTestShotsListMap` | A-05 | B-2 | — | — | — | — |
| 21 | `eachGroupHasDistinctDefaultColor` | — | — | — | — | — | —（SUT は `ExcelFormatConfig`。Writer は駆動しない） |
| 22 | `drawsBlockOuterBorder` | A-02 | B-1 | — | — | E-2(1) | — |
| 23 | `insertsBlankRowBetweenBlocks` | A-02 | B-1 | — | — | **E-1(複数=2)** | — |
| 24 | `appliesAutoColumnWidth` | A-02 | B-1 | — | — | — | — |
| 25 | `honorsConfigOverrides` | A-02 | B-1 | — | — | E-1(複数=2) | — |
| 26 | `rejectsNegativeBlankRows` | — | — | — | — | — | ✅ 設定値負数 → `IllegalArgumentException`（steering の 4 ケース外） |
| 27 | `writesWorkbookFileWithSheetPerSection` | A-02, A-05 | B-1, B-2 | C-01, **C-02(複数=2)**, C-03 | — | **E-4(複数=2)** | — |
| 28 | `honorsMarkerColumnColorOverride` | A-02 | B-1 | — | ※マーカーカラム記法 | — | — |
| 29 | `doesNotTintUnclosedBracketColumn` | A-02 | B-1 | — | ※未閉じ括弧 `[half` はマーカーでない | — | — |
| 30 | `writesOmittedMetaAndFieldAsEmpty` | A-06 | B-3 | **C-20(省略)**, **C-21(省略)**, C-11(値 null) | ※null→空セル（メタ側） | E-3(1) | — |
| 31 | `writesSequenceNoForAllSendSyncTypes` | A-11, A-12, A-13, A-14 | B-4 | C-05, C-06(値あり) | — | E-3(1) | — |
| 32 | `wrapsIoFailure` | A-02 | B-1 | — | — | — | 🔺**F3-01**（親に通常ファイルが居座り出力先を作れない）→ `UncheckedIOException` |
| 33 | `roundTripsTable` | A-02 | B-1 | C-05, C-07, C-08, C-09 | ※実 `.xlsx` 往復（文字列・`${}`・空文字） | E-2(複数=2) | — |
| 34 | `roundTripsNullCellAsLiteralNullString` | A-02 | B-1 | C-09 | 🔺D3-04 null→`"null"`（非可逆を固定）／🔺D3-05 `""` | E-2(1) | — |
| 35 | `roundTripsListMap` | A-05 | B-2 | C-07, C-08, C-09 | ※実 `.xlsx` 往復 | E-2(複数=2) | — |
| 36 | `roundTripsFixedFile` | A-06 | B-3 | C-07, C-10(FIXED), C-16, C-18, C-20, C-21 | ※長さ記法 `-` の往復 | E-3(1) | — |
| 37 | `roundTripsMultipleRecordLayouts` | A-06 | B-3 | C-12(2件), C-16, C-18 | — | E-3(複数=2) | — |
| 38 | `roundTripsVariableFile` | A-08 | B-3 | **C-10(VARIABLE)** ✅, C-21(省略) | — | E-3(1) | — |
| 39 | `roundTripsMessage` | A-10 | B-4 | C-05, C-07, C-14(値あり), C-17, C-18, C-19 | — | E-3(1) | — |
| 40 | `roundTripsSendSyncMessage` | A-11 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-17, C-18, C-19 | — | E-3(1) | — |

**上表 #31 `writesSequenceNoForAllSendSyncTypes` の軸A 欄「A-11, A-12, A-13, A-14」は誤りである
（2026-08-13・#23 レビュー指摘。本表は #18 時点のスナップショットのため書き換えない）。**
同メソッドは 4 タイプのブロックを入力に与えるが、アサートするのは 4 タイプ共通の連番 `"1"` だけで、
**タイプを区別する出力を固定していない**（変異による実測は [§3.1-3](#s3-1-3-sendsync)）。
正しくは軸A 欄は「—」（このテストは軸A を担保しない）である。A-11 は #14 `writesSendSyncMessageWithSequenceNo`
が識別セル全体をアサートしており独立に ✅、A-12〜A-14 は #23 レビュー対応で
`XlsFormatWriterModelTest` に追加した 3 メソッドが担保する。

### 3.1-2 #22 が追加したテストクラスの担保（2026-08-13 追記）

**本節は #22 で新設した。** §3.1 は「`XlsFormatWriterTest` 40 件」を対象とした #18 時点の事実であり
書き換えていない。ここには #22 が追加した**書き出したファイルを開き直して確かめるテストクラス**の担保だけを記す。
#22 の対象は軸D（セル型 8 ケース）と軸F（異常系）に限る。軸A・軸B・軸C・軸E の欠けは #23 の対象であり、
**その担保は §3.1-3 に記す**（#23 完了・2026-08-13）。

| テストクラス | 追加タスク | 件数 | 検証対象 |
|---|---|---|---|
| `XlsFormatWriterCellTypeTest` | #22 | 18 | `XlsFormatWriter#write` が書いた実 `.xlsx` を POI で開き直し `Cell#getCellType()` と値を突き合わせる（16 件）。加えて ZIP エントリ `xl/sharedStrings.xml` の**生バイト**を検査する（2 件） |
| `XlsFormatWriterInvalidOutputTest` | #22 | 16 | 出力先・シート名の異常系（例外型・メッセージ・ファイルの有無・書けてしまった結果） |

件数は `grep -c "^    @Test" src/test/java/nablarch/test/tool/converter/xls/<クラス>.java` の実測（2026-08-13）。

**1 ケース 1 `@Test` で展開している。** 制御文字 6 文字（D3-08）とシート名の禁止文字 7 文字（F3-04）は
ループで束ねず文字ごとに 1 メソッドへ分けた。姉妹クラス `XlsFormatReaderCellTypeTest`（1 ケース 1 `@Test`）に
スタイルを揃えるためであり、ループだと最初の 1 件が落ちた時点で残りが実行されず全体像が取れないためでもある。

**軸D（辺③ 8 ケース。すべて `getCellType()` をアサート。#18 は ✅ 0 ／ 🔺 2 ／ ❌ 6）**

| 要素 | #18 | #22 後 | 担保テストメソッド（`XlsFormatWriterCellTypeTest#`） | 観測したセル型・値 |
|---|---|---|---|---|
| D3-01 `"100"` | ❌ | ✅ | `writesNumericLookingStringAsStringCell` | `CELL_TYPE_STRING`・`"100"`。数値セルにならない（`getNumericCellValue()` が `IllegalStateException`） |
| D3-02 `"=1+1"` | ❌ | ✅ | `writesFormulaLookingStringAsStringCell` | `CELL_TYPE_STRING`・`"=1+1"`。数式セルにならない（`getCellFormula()` が `IllegalStateException`。計算結果 `2` にもならない） |
| D3-03 `"007"` | ❌ | ✅ | `writesLeadingZeroStringAsStringCell` | `CELL_TYPE_STRING`・`"007"`（先頭ゼロが落ちない） |
| D3-04 `null` | 🔺（値のみ） | ✅ | `writesNullValueAsLiteralNullStringCell` | `CELL_TYPE_STRING`・`"null"`（空白セルにならない） |
| D3-05 `""` | 🔺（値のみ） | ✅ | `writesEmptyValueAsEmptyStringCell` | `CELL_TYPE_STRING`・長さ 0（`CELL_TYPE_BLANK` へ退化しない） |
| D3-06 改行含む文字列 | ❌ | ✅ | `writesLineFeedStringAsStringCell`／`replacesCrLfWithSingleLineFeedInStringCell`／`replacesLoneCarriageReturnWithLineFeedInStringCell` | `LF` は原文のまま。`CR` は **`LF` へ置換**される（削除ではない）。`CRLF`（4 文字）は `LF` 1 文字にまとまって 3 文字になるが、単独 `CR`（`a`＋`CR`＋`b`）は 3 文字のまま長さが変わらない（`issues.md` **XLS-18**） |
| D3-07 32767 文字超 | ❌ | ✅ | `writesStringLongerThanExcelCellLimitAsStringCell`／`writesStringOfExcelCellLimitLengthAsStringCell` | 32768 文字も 32767 文字も `CELL_TYPE_STRING` で内容ごとそのまま書かれる（切り詰め・例外なし。`issues.md` **XLS-19**） |
| D3-08 制御文字含む | ❌ | ✅ | `replacesNulCharacterWithQuestionMark`／`replacesBellCharacterWithQuestionMark`／`replacesVerticalTabCharacterWithQuestionMark`／`replacesUnitSeparatorCharacterWithQuestionMark`／`writesTabCharacterAsIs`／`writesDeleteCharacterAsIs` | XML 1.0 で不正な `U+0000`／`U+0007`／`U+000B`／`U+001F` は `?` へ置換（`issues.md` **XLS-17**）。XML 1.0 で正当な `U+0009`／`U+007F` は原文のまま |

- **すべてファイル経由で確かめている。** メモリ上のブックだけを見ると D3-06（`CR` を含む改行）と
  D3-08（制御文字）を取り逃す（どちらも `build` 直後は原文のままである）。
  該当する担保テストは「メモリ上では保たれている」ことも併せてアサートしている。
- **ただし D3-06 と D3-08 では変化の起きる区間が違う（2026-08-13・レビュー指摘による訂正）。**
  当初この節は両者をまとめて「変わるのは `.xlsx` へ直列化する区間である」と書いていたが、
  書き出した `.xlsx` を `unzip -p <book>.xlsx xl/sharedStrings.xml | od -An -tx1 -c` で展開して
  生バイトを確かめた結果、**D3-06 の `CR` については誤りだった**。

  | ケース | 課題 | `xl/sharedStrings.xml` の生バイト | 変化が起きる区間 | 担保テスト（`XlsFormatWriterCellTypeTest#`） |
  |---|---|---|---|---|
  | D3-08 制御文字 | XLS-17 | `<t>a?b</t>`（`?` ＝ `3f` が焼き込まれている。`00` はファイルに残らない） | **直列化区間** | `burnsQuestionMarkIntoSharedStringsXmlForControlCharacter` |
  | D3-06 `CR` | XLS-18 | `<t>a[CR]b</t>`（`CR` ＝ `0d` が生のまま残る。`&#13;` への退避も無い） | **読み戻し（XML パース）区間** | `keepsCarriageReturnRawInSharedStringsXml` |

  **この 2 件はレビュー指摘（第 3 ラウンド）で追加した。** それまで区間の帰属は手作業のダンプでしか
  確かめておらず、テストは読み戻し値しか見ていなかった。POI／xmlbeans の挙動が変われば
  セル型・値のテスト 16 件は緑のまま、本書と Javadoc の「区間の帰属」だけが誤りになる状態だった。
  追加した 2 件は ZIP エントリを直接開き、パースせずバイト列として突き合わせる。

  したがって「メモリ上では保たれている」というアサートが示すのは、
  **`XlsFormatWriter` 自身が値を変えていないこと**だけであり、
  D3-06 について「直列化で失われた」ことの証明にはならない（ファイルにも残っているため）。
  読み手への影響は、**`.xlsx` をバイトで比較しても `CR` は残って見えるため探す場所を間違える**点である
  （`issues.md` XLS-18 の「影響」欄）。

**軸F（辺③ 4 ケース中 3 ケースを新規担保。F3-02 は対象外）**

| 要素 | #18 | #22 後 | 担保テストメソッド（`XlsFormatWriterInvalidOutputTest#`） | 観測した挙動 |
|---|---|---|---|---|
| F3-01 出力先不在 | 🔺 | ✅ | `createsMissingOutputDirectoriesAndWritesWorkbook` | 例外にならず多階層の出力先が作られ、ブックが書き出される（`XlsFormatWriter#write` L105 の `Files.createDirectories`）。既存の 🔺 `XlsFormatWriterTest#wrapsIoFailure` は「親に通常ファイルが居座りディレクトリを作れない」別ケース（`UncheckedIOException`）であり、両方で出力先まわりが揃う |
| F3-02 `overwrite=false` 衝突 | 対象外 | **対象外（変更なし）** | —（本クラスに該当テストは無い） | `XlsFormatWriter` は `overwrite` を保持しない（保持するのは `ConversionRequest` / `TestDataConverter` / `ConverterMojo`。§0.8-5）。衝突検査は `XlsFormatWriter` を呼ぶ前に上位層（`TestDataConverter#checkOverwrite` L90-99）で完結するため、辺③ では再現できない。**ただし「上位層の既存テストが担保している」のは `.yaml` を出力側とする衝突だけである**: `TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse`（L336）／`ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict`（L267）はどちらも XLS→YAML であり、通るのは `YamlFormatHandler#outputPaths`（L63）。`XlsFormatHandler#outputPaths`（L63-67）自体は `overwrite=false` 下で実行されている（`TestDataConverterTest#convertsYamlToXls`, `#convertsXlsToXls` ほか 1 件（計 3 件）。変異で実証。§0.8-5 と同じ 3 件）が、**`.xlsx` が既存で衝突する分岐**（`checkOverwrite` の `Files.exists(output)` → `ConverterException`）は 1 件も通っていない（§0.8-5 の訂正） |
| F3-03 書き込み権限なし | ❌ | ✅ | `wrapsAccessDeniedExceptionWhenOutputDirectoryIsNotWritable` | `UncheckedIOException: failed to write Excel: <出力先パス>` ＋ 原因 `java.nio.file.AccessDeniedException`。ファイルは作られない。POSIX 権限が効かない環境（root 実行など）では `Assume` でスキップする（確認用ファイルの作成が拒否されることを前提条件として確かめる） |
| F3-04 シート名が Excel 制約違反 | ❌ | ✅ | 禁止文字 7 件: `rejectsSheetNameContainingSlash`／`rejectsSheetNameContainingBackslash`／`rejectsSheetNameContainingQuestionMark`／`rejectsSheetNameContainingAsterisk`／`rejectsSheetNameContainingOpeningBracket`／`rejectsSheetNameContainingClosingBracket`／`rejectsSheetNameContainingColon`。ほか `rejectsEmptySheetName`／`writesSheetNameOfExcelLimitLengthAsIs`／`truncatesSheetNameLongerThanExcelLimitSilently`／`writesSheetNameWhoseForbiddenCharacterIsRemovedByTruncation`／`rejectsSheetNameWhoseForbiddenCharacterSurvivesTruncation`／`failsWhenTruncatedSheetNamesCollide`／`failsWhenSheetNamesDifferOnlyInCase` | **31 文字ちょうどはそのまま書かれる**（切り詰めなし）。**31 文字超は例外にならず黙って 31 文字へ切り詰められる**（`issues.md` **XLS-16**）。切り詰め後に衝突したときだけ `IllegalArgumentException: The workbook already contains a sheet of this name`（**大文字小文字だけが違う名前も同名と判定される**。切り詰めが走らない 3 文字で実測）。空文字は `IllegalArgumentException: sheetName '' is invalid`。禁止文字（`/ \ ? * [ ] :`）は POI の `IllegalArgumentException: Invalid char (x) found at index (i) in sheet name '...'` でブックを作らずに失敗するが、**これは切り詰め後の名前に禁止文字が残る場合に限る**（下記） |

**F3-04 の「禁止文字は必ず失敗する」は無条件では成り立たない。** POI 3.8 の `XSSFWorkbook#createSheet(String)` は
`substring(0, 31)` による切り詰めを `WorkbookUtil.validateSheetName` **より先に**適用する。したがって
**禁止文字が index 31 以降にある 32 文字以上のシート名は検査に到達せず、例外にならずブックが書き出される**。
実測（2026-08-13。担保テストは `writesSheetNameWhoseForbiddenCharacterIsRemovedByTruncation`）:
`"a"×31 + "/"`（32 文字）→ 例外なし・`a`×31 のシートを持つブックが生成。対照として
`"a"×30 + "/a"`（32 文字。切り詰め後も `/` が残る）→ `Invalid char (/) found at index (30) in sheet name 'aaa…a/'`
となり、**メッセージのシート名が切り詰め後の 31 文字である**ことが検査順序の裏づけになる
（担保テストは `rejectsSheetNameWhoseForbiddenCharacterSurvivesTruncation`）。

**F3-04 で #22 が担保する範囲**は、31 文字超・禁止文字（`/ \ ? * [ ] :`）・空文字・31 文字ちょうど（正常側の境界）・
重複判定（切り詰め後の衝突／大文字小文字だけが違う名前）である。
**シート名のアポストロフィ（先頭／末尾）と `null` は #22 のスコープ外であり未担保**（タスク #22 の Steps が
F3-04 の範囲を「31 文字超・禁止文字」と定めているため）。

<a id="s3-1-2-parent-null"></a>

**F3-01 の隣接領域に、src/test 全体で一度も通っていない分岐が 1 つある（2026-08-13・第 3 ラウンドの指摘により開示）。**
上表の F3-01 行は「出力先まわりが揃う」と書いているが、`XlsFormatWriter#write` の
**`parent == null` 分岐（L102-106）は未担保**である。`src/main` のコメント（L103）が
「親ディレクトリを持たない相対パス（例: `"foo.xlsx"`）が生成されると `getParent()` は `null` を返すため、
null チェックが必須」と明記している分岐であるため、担保の穴としてここに開示する。

- **成立条件**: `Paths.get(basePath, container.getName() + ".xlsx").getParent()` が `null` になるのは
  `basePath` が空文字のときだけである（`jshell` で実測（2026-08-13）:
  `Paths.get("", "Book.xlsx").getParent()` → `null` ／
  `Paths.get(".output/SampleConversionTest", "Book.xlsx").getParent()` → `.output/SampleConversionTest` ／
  `Paths.get("/tmp/junit123", "Book.xlsx").getParent()` → `/tmp/junit123`）。
- **到達経路の全数**: `XlsFormatWriter#write` が呼ばれるのは src/test では次の 2 経路しかない。
  1. 直接呼び出し **20 か所**（`grep -rn "new XlsFormatWriter(.*)\.write(" src/test --include=*.java | wc -l` → 20。
     内訳は `grep -rc` で `XlsFormatWriterInvalidOutputTest:10`／`XlsFormatWriterTest:3`／
     `XlsFormatWriterCellTypeTest:2`／`TestDataConverterTest:2`／`XlsFormatWriterModelTest:1`／
     `RoundTripTest:1`／`ConverterMojoTest:1`）。
     20 か所の `basePath` 実引数はすべて `folder.getRoot().getAbsolutePath()` ／ `<File>.getAbsolutePath()` ／
     `<Path>.toString()` であり、空文字にならない。
     **#22 時点は 19 か所だった**（内訳に `XlsFormatWriterModelTest` が無かった）。#23 が
     `XlsFormatWriterModelTest` を追加したことで 20 になり、この記述は陳腐化していた
     （2026-08-13・#23 レビュー対応で訂正。増えた 1 か所も `basePath` は絶対パスであり結論は変わらない）。
  2. 本番配線 `TestDataConverter#convert`（L75 `writer.write(container, outputBase.toString())`）。
     `outputBase` は `XlsFormatHandler#resolveOutputBase`（L58-60）が `request.getOutputPath()` から組む。
     テストが渡す出力先は `TemporaryFolder` 由来の絶対パス（`TestDataConverterTest` L69 `out = folder.newFolder("out").toPath()`／
     `ConverterMojoTest` の `inject(mojo, "output", out.toFile())`）と、`SampleConversionTest` の
     `OUTPUT_BASE = Paths.get(".output/SampleConversionTest")`（L34）だけで、いずれも空文字にならない。
- **#22 では埋めない。** #22 の軸F の定義は F3-01 出力先不在／F3-02 `overwrite=false` 衝突／F3-03 書き込み権限なし／
  F3-04 シート名制約違反の 4 要素（§0.7）であり、この分岐はいずれにも当たらない。
  本書の軸要素ではないため §3.3 の件数（要追加 15 ／担保済み 11 ／対象外 1 ＝ 27）には算入しない。

### 3.1-3 #23 が追加したテストクラスの担保（2026-08-13 追記）

**本節は #23 で新設した。** §3.1（`XlsFormatWriterTest` 40 件）と §3.1-2（#22 の 2 クラス）は
それぞれの時点の事実であり書き換えていない。ここには #23 が追加したテストクラスの担保だけを記す。
#23 の対象は §3.3 が「#23 の対象」として残していた **15 要素（軸A 3・軸C 9・軸E 3）**と、
**#23 のレビューで担保の穴として判明した軸A 3 要素（A-12／A-13／A-14）**である。

| テストクラス | 追加タスク | 件数 | 検証対象 |
|---|---|---|---|
| `XlsFormatWriterModelTest` | #23 | 18 | `XlsFormatWriter#write` が書いた実 `.xlsx` を POI で開き直し、**残り 3 データタイプの識別セル**・**送信同期 3 種の識別セル**・**空のコレクション・多重度 0 が版面のどこに現れる／現れないか**を突き合わせる（15 件）。加えて `issues.md` XLS-20／XLS-21／XLS-22 が主張する**読み戻しの結果**を `XlsFormatReader` で実検査する（3 件） |

件数は `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterModelTest.java` → **18**（2026-08-13 実測）。
`@Test` 18 件と Javadoc の `Given:` 18 件は一致する（`grep -c 'Given:' <同ファイル>` → 18）。

**実ファイル経路で確かめている。** 本クラスは 18 件すべてが `write` の作ったファイルを開き直す。
空の行・空のセルが直列化で落ちないことまで含めて固定するためであり、`XlsFormatWriterCellTypeTest`（#22）と
同じ方針である。**`XlsFormatWriterTest` 40 件との違いは「全件がそうか否か」である**（→ §3.1-4）。

**`XlsFormatWriterTest` の 40 件は #23 では 1 行も変えていなかったが、#23 のレビュー対応で変更した。**
変更は次の 3 点のみで、`@Test` の数・アサートの内容は変えていない
（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterTest.java` → **40** のまま）。

1. セル読み出しヘルパ `cell` / `line` の定義を削除し `XlsFixture` の同名 static メソッドを static import
   （理由と判断は §3.1-5）
2. 未使用になった import（`org.apache.poi.ss.usermodel.Cell` / `Row`）の削除
3. クラス Javadoc の訂正（`build` と `write` の内訳。§3.1-4）

A-07／A-09 が同クラスに 0 件であることは
`grep -c 'EXPECTED_FIXED\|EXPECTED_VARIABLE' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterTest.java` → **0**
で確かめた（2026-08-13 実測。#23 レビュー対応後も 0 のまま）。

<a id="s3-1-3-sendsync"></a>

**軸A（#18 は ✅ 11 ／ 🔺 2 ／ ❌ 1。#23 完了後は ✅ 14）**

| 要素 | #18 | #23 後 | 担保テストメソッド（`XlsFormatWriterModelTest#`） | 観測した版面 |
|---|---|---|---|---|
| A-01 `DEFAULT` | ❌ | ✅ | `writesDefaultDataTypeMarker` | 識別セル `DEFAULT=T`（例外にならない）。ヘッダ色はその他グループ。読み戻すとブロックが消える（`issues.md` **XLS-20**。`#dropsDefaultDataTypeBlockWhenReadBack`） |
| A-07 `EXPECTED_FIXED` | 🔺 | ✅ | `writesExpectedFixedFileBlockWithLengthRow` | 識別セル `EXPECTED_FIXED=exp.dat`。識別行 → ディレクティブ行 → 名前行 → 型行 → **長さ行** → データ行 |
| A-09 `EXPECTED_VARIABLE` | 🔺 | ✅ | `writesExpectedVariableFileBlockWithoutLengthRow` | 識別セル `EXPECTED_VARIABLE[g2]=exp.csv`。可変長なので**長さ行なし** |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅（**誤り**。下記） | ✅ | `writesExpectedRequestBodyMessagesMarker` | 識別セル `EXPECTED_REQUEST_BODY_MESSAGES[case1]=RM21AA0104_01`。FW ヘッダ行なし・データ行の列 0 に連番 |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅（**誤り**。下記） | ✅ | `writesResponseHeaderMessagesMarker` | 識別セル `RESPONSE_HEADER_MESSAGES[case1]=RM21AA0104_01`。同上 |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅（**誤り**。下記） | ✅ | `writesResponseBodyMessagesMarker` | 識別セル `RESPONSE_BODY_MESSAGES[case1]=RM21AA0104_01`。同上 |

**A-12／A-13／A-14 の ✅ は #18 以来（#23 の当初版を含め）誤りだった。**
この 3 タイプを辺③で通していたのは `XlsFormatWriterTest#writesSequenceNoForAllSendSyncTypes`（L805-822）だけで、
同メソッドがアサートするのは**データ行の列 0 の連番 `"1"`**（L820 `cell(sheet, 4, 0)`。4 タイプ共通の値）であり、
**タイプを区別する出力を 1 つも固定していなかった**。辺③で識別セルを直接アサートしていたのは
`XlsFormatWriterTest` L422（A-11 `EXPECTED_REQUEST_HEADER_MESSAGES`）の 1 箇所だけである。

- **変異による実測（2026-08-13。#23 レビュー指摘の再現）**: `XlsFormatWriter#marker`（`src/main` L399-401）が
  この 3 タイプにだけ別文字列 `"MUTATED"` を返すよう `src/main` を一時的に変異させて全件実行したところ、
  落ちたのは **`RoundTripTest` の 3 件のみ**（`xls_expectedRequestBodyMessages_isPreserved` ／
  `xls_responseHeaderMessages_isPreserved` ／ `xls_responseBodyMessages_isPreserved`）で、
  **`XlsFormatWriterTest` 40 件は全緑**だった（`Tests run: 425, Failures: 3`）。
  往復テストは steering Rules フェーズ2 により正式な担保に数えないため、
  **この時点で A-12／A-13／A-14 は 🔺 相当（正式担保 0）**であった。
- **埋め方**: 開示だけで済ませず、上表の 3 メソッドを追加した。粒度は L422（A-11）に揃え、
  グループ ID と識別子を含むマーカー全体を固定する。
- **歯があることの実証**: 同じ変異を再度入れて全件実行し、`XlsFormatWriterModelTest` の該当 3 件が
  落ちることを確認した（`Tests run: 428, Failures: 6` ＝ 新規 3 件 ＋ `RoundTripTest` 3 件）。
  変異は確認後に戻し、`git diff HEAD -- src/main` が 0 行であることを確かめた。

**軸C（#18 は未担保 9。#23 完了後は 0）**

| 要素 | 状態 | 担保テストメソッド（`XlsFormatWriterModelTest#`） | 観測した版面 |
|---|---|---|---|
| C-02 `sections` 空 | ✅ | `writesWorkbookWithoutSheetsWhenContainerHasNoSections` | 例外にならずファイルが作られ、**シート 0 枚**のブックになる（`issues.md` **XLS-23**） |
| C-04 `blocks` 空 | ✅ | `writesEmptySheetWhenSectionHasNoBlocks` | シートは作られるが行が 1 行も無い（`getPhysicalNumberOfRows()` が 0） |
| C-08 `columnNames` 空 | ✅ | `writesEmptyHeaderRowWhenColumnNamesAreEmpty` | カラム名行が**データ行の幅ぶんの空セル**になる（行自体は消えない）。読み戻すとデータ行がカラム名へ昇格する（`issues.md` **XLS-21**。`#promotesFirstDataRowToColumnNamesWhenEmptyColumnNamesAreReadBack`） |
| C-09 `rows` 空 | ✅ | `writesTableWithoutDataRowsWhenRowsAreEmpty` | 識別行とカラム名行だけ。データ行の位置は行そのものが無い（`getRow(2)` が `null`） |
| C-12 `FileDataBlock.records` 空 | ✅ | `writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty` | 識別行とディレクティブ行だけ。名前行・型行・長さ行・データ行は 1 行も出ない |
| C-13 `MessageDataBlock.directives` 値あり | ✅ | `writesDirectiveRowsBeforeFwHeaderRowsInMessage` | ディレクティブ行が記述順に並び、**FW 制御ヘッダ行より上**に出る（識別行 → ディレクティブ 2 行 → FW ヘッダ 1 行 → 名前行 → 型行 → 長さ行 → データ行） |
| C-15 `MessageDataBlock.records` 空 | ✅ | `writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty` | 識別行 → ディレクティブ行 → FW 制御ヘッダ行まで。本文の行は 1 行も出ない |
| C-17 `RecordLayout.fields` 空 | ✅ | `writesRecordWithoutFieldColumnsWhenFieldsAreEmpty` | 名前行はレコード種別セルだけ（右は矩形整形の空セル）、型行・長さ行は空セルだけ、データ行の値はフィールド定義が無いまま出る。この版面は読み戻せない（`issues.md` **XLS-22**。`#failsToReadBackRecordWithoutFields`） |
| C-18 `RecordLayout.rows` 空 | ✅ | `writesRecordWithoutDataRowsWhenRecordRowsAreEmpty` | 名前行・型行・長さ行まで。データ行は行そのものが無い |

**軸E の 0 件（#18 は未担保 3。#23 完了後は 0）**

| 要素 | 状態 | 担保テストメソッド（`XlsFormatWriterModelTest#`） | 備考 |
|---|---|---|---|
| E-1(0 件) セクション内ブロック数 0 | ✅ | `writesEmptySheetWhenSectionHasNoBlocks` | C-04 と同じ入力 |
| E-2(0 件) ブロック内行数 0 | ✅ | `writesTableWithoutDataRowsWhenRowsAreEmpty`（テーブル経路）／`writesRecordWithoutDataRowsWhenRecordRowsAreEmpty`（ファイル経路の値行） | 順に C-09／C-18 と同じ入力 |
| E-3(0 件) ファイル内レコードレイアウト数 0 | ✅ | `writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty`（ファイル経路）／`writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty`（メッセージ経路） | 順に C-12／C-15 と同じ入力 |

**末尾 3 件は軸要素の担保に数えていない。** `#dropsDefaultDataTypeBlockWhenReadBack` ／
`#promotesFirstDataRowToColumnNamesWhenEmptyColumnNamesAreReadBack` ／ `#failsToReadBackRecordWithoutFields` は
書き出したブックを `XlsFormatReader` で読み戻し、`issues.md` XLS-20／XLS-21／XLS-22 の「読み戻すとどうなるか」を
実検査する。steering Rules フェーズ2 の「往復テストで担保を代替しない」に従い、辺③の担保としても
辺①の担保としても数えない。置く理由は #22 が `xl/sharedStrings.xml` の生バイト検査 2 件を置いたのと同じで、
本体パーサ・`PoiXlsReader` の挙動が変わったときに**担保テストは緑のまま `issues.md` の記述だけが誤りになる**
状態を防ぐためである。したがって **12 件（#23 当初の担保）＋ 3 件（#23 レビュー対応の送信同期の担保）
＋ 3 件（issues 検査）＝ 18 件**である。

**JaCoCo 実測（#23 完了後・2026-08-13）**: `XlsFormatWriter` は命令 **98%**（8 / 782 未到達）・
分岐 **97%**（3 / 100 未到達）・行 **1 / 151 未到達**（取得手順は steering の Decisions）。
未到達は次の 3 箇所のみで、いずれも #23 の軸要素ではない。

| 箇所 | 未到達の内容 | 扱い |
|---|---|---|
| `write` L104 `if (parent != null)` | `null` 側の分岐（1 / 2） | 既知の担保の穴。到達経路の全数調査は [§3.1-2 の該当項](#s3-1-2-parent-null) |
| `layout` L171 `else if (block instanceof MessageDataBlock)` の false 側と L174 の `throw` | 未知のブロック実装（1 / 2 分岐・1 行） | sealed 階層が permit する 3 種すべてを本節と §3.1 が通しているため到達不能。Java イディオムとしての安全網（steering #6 の判断と同じ思想） |
| `isMarkerColumn` L410 `columnName != null` | `null` 側の分岐（1 / 6） | steering #9 でコメント済みの防御ガード。`layoutColumnRow` L189 のコメントが「カラム名が `null` の場合は…非マーカーとして扱う」と明記している |

<a id="s3-1-4"></a>

### 3.1-4 「§3.1 の 40 件は `build` を見る」は誤りだった（2026-08-13・#23 レビュー指摘により訂正）

**訂正前の記述**: §3.1-3 は「§3.1 の 40 件は `build`（メモリ上のブック）を見るが、本クラスは
`write` が実際に作ったファイルを開き直す」と書いていた。`XlsFormatWriterModelTest` のクラス Javadoc も
同じことを書いていた。**どちらも誤り**である。同じ本書の §0.8-2 が「辺③の往復テスト 8 件は実 `.xlsx` を
経由して」と書いており自己矛盾していた。

**実測（2026-08-13）**: `XlsFormatWriterTest` で実ファイルを書くのは **40 件中 10 件**である。

```
$ grep -n 'new XlsFormatWriter()\.write(' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterTest.java
733:        new XlsFormatWriter().write(c, folder.getRoot().getAbsolutePath());
855:        new XlsFormatWriter().write(c, blocker.getAbsolutePath());
863:        new XlsFormatWriter().write(container, folder.getRoot().getAbsolutePath());
```

（行番号は #23 完了時点 `3125c92` のもの。`git show 3125c92:<パス> | grep -n` で再現できる。
#23 レビュー対応で import と Javadoc を変えたため現在の行番号はずれる。）

| 呼び出し位置 | 呼ぶテストメソッド | 件数 |
|---|---|---|
| L863（`roundTrip` ヘルパ L861-865） | `roundTripsTable` / `roundTripsNullCellAsLiteralNullString` / `roundTripsListMap` / `roundTripsFixedFile` / `roundTripsMultipleRecordLayouts` / `roundTripsVariableFile` / `roundTripsMessage` / `roundTripsSendSyncMessage` | 8 |
| L733 | `writesWorkbookFileWithSheetPerSection` | 1 |
| L855 | `wrapsIoFailure` | 1 |
| **合計** | | **10** |

したがって **`build` だけを見るのは 30 件**（40 − 10）である。`XlsFormatWriterModelTest` との違いは
「`build` か実ファイルか」ではなく「**全件が実ファイル経路か否か**」であり、§3.1-3 と当該クラス Javadoc は
その表現へ訂正した。

**src/test 全体の `XlsFormatWriter#write` 直接呼び出しは 20 か所である**（#23 で `XlsFormatWriterModelTest` が
1 か所増えたため、§3.1-2 が書いていた 19 か所は陳腐化していた。§3.1-2 側も訂正済み）。

```
$ grep -rn "new XlsFormatWriter(.*)\.write(" src/test --include=*.java | wc -l
20
$ grep -rc "new XlsFormatWriter(.*)\.write(" src/test --include=*.java | grep -v ':0$'
src/test/java/nablarch/test/tool/converter/ConverterMojoTest.java:1
src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterCellTypeTest.java:2
src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterInvalidOutputTest.java:10
src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterTest.java:3
src/test/java/nablarch/test/tool/converter/TestDataConverterTest.java:2
src/test/java/nablarch/test/tool/converter/RoundTripTest.java:1
src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterModelTest.java:1
```

増えた 1 か所（`XlsFormatWriterModelTest#write` ヘルパ）が渡す `basePath` は
`folder.getRoot().getAbsolutePath()` であり空文字にならないため、§3.1-2 の
[`parent == null` 分岐が未担保](#s3-1-2-parent-null)という結論は変わらない。

<a id="s3-1-5"></a>

### 3.1-5 ヘルパ抽出の要否（#22 からの持ち越し。#23 で判断を確定。2026-08-13）

`.rn/ntf-test-data-converter/checks/task-22.md` が「中間モデル組み立てヘルパの抽出の**要否は #23 で判断する**」と
書いて #23 へ委ねていた。#23 は Writer 系 4 本目のテストクラスを追加しながらこの判断をどこにも記録して
いなかったため、ここで確定して記録する。

**実測した重複（2026-08-13。定義位置は #23 完了時点 `3125c92`）**

```
$ git grep -n "List<String> line(Sheet" 3125c92 -- src/test
3125c92:.../xls/XlsFormatWriterModelTest.java:187:    private static List<String> line(Sheet sheet, int r) {
3125c92:.../xls/XlsFormatWriterTest.java:110:    private static List<String> line(Sheet sheet, int r) {
$ git grep -n "String cell(Sheet" 3125c92 -- src/test
3125c92:.../xls/XlsFormatWriterTest.java:100:    private static String cell(Sheet sheet, int r, int c) {
$ git grep -c "static List<String> row(String" 3125c92 -- src/test | wc -l
8
$ git grep -c "static Map<String, String> map(String" 3125c92 -- src/test | wc -l
2
$ git grep -n "static TestDataContainer container(" 3125c92 -- src/test | wc -l
5
```

| ヘルパ | 定義数（`3125c92`） | 本体 | 判断 |
|---|---|---|---|
| `line(Sheet, int)` | 2（`XlsFormatWriterTest` L110 ／ `XlsFormatWriterModelTest` L187） | 完全一致（11 行） | **`XlsFixture` へ抽出した** |
| `cell(Sheet, int, int)` | 1（`XlsFormatWriterTest` L100） | — | **`XlsFixture` へ抽出した**（`line` と対のため） |
| `row(String...)` | 8 ファイル（`xls` / `yaml` / `converter` / `core.reader` の 4 パッケージ） | `Arrays.asList` 1 行 | **現状維持** |
| `map(String...)` | 2（`XlsFormatWriterTest` ／ `XlsFormatWriterModelTest`） | 完全一致 | **現状維持** |
| `container(...)` | 5（引数の形が 5 通り） | 別物 | **重複ではない。現状維持** |
| 往復（`roundTrip` ／ `writeAndReadBack`） | 2（同一ロジック） | — | **現状維持**（下記） |

**判断の理由**

- **境界は「POI のブック・シートを直接触るか」に引いた。** `XlsFixture` は既に
  `static Workbook open(Path)` でパッケージの POI 読み出し側を担っており、抽出前の時点で
  **7 テストクラス**が使っていた（`git grep -l "XlsFixture" 3125c92 -- src/test | wc -l` → 8 ファイル。
  うち 1 つは `XlsFixture` 自身）。抽出後は `XlsFormatWriterTest` が加わり
  **8 クラス**になる（`grep -rln "XlsFixture" src/test --include=*.java | wc -l` → 9）。
  同クラスの Javadoc が線を引いているのは「**中間モデル**組み立てヘルパとは対象レイヤが異なる」であって、
  シート読み出しユーティリティは元から対象外ではない。`line` / `cell` はこちら側に入る。
- **`cell` は写しが 1 件だったが `line` と一緒に移した。** 対になるアクセサを分けて置くと、
  次の Writer 系テストクラス（すでに 4 本ある）が `cell` 側の写しを作る。
- **`row` は抽出しない。** 4 パッケージ 8 ファイルに定着したイディオムで、集約するとパッケージをまたぐ
  依存が増えるだけである（本体は 1 行）。
- **`map` は抽出しない。** 写しは 2 件あるが**中間モデル組み立て側**であり、`XlsFixture` の Javadoc が
  明示する境界の向こうにある。
- **`container` はそもそも重複ではない**（5 定義・5 通りの引数）。
- **往復ヘルパ（`XlsFormatWriterTest#roundTrip` ／ `XlsFormatWriterModelTest#writeAndReadBack`）も抽出しない。**
  ロジックは同一だが、`XlsFormatReader` を駆動する＝**辺①側の SUT を呼ぶ**ヘルパであり、
  `XlsFixture`（POI だけを触る）にも中間モデル組み立てにも属さない。往復は steering Rules フェーズ2 で
  正式担保に数えない位置づけであり、共通基盤へ格上げすると担保として使われやすくなる副作用がある。

**記録先**: 判断そのものは `XlsFixture` のクラス Javadoc（「本クラスが引き受けるヘルパの範囲」）に置き、
実測値とコマンドは本節に置いた。

### 3.2 軸要素 → 担保テストメソッド

**軸A（#18 時点の記載: ✅ 11 ／ 🔺 2 ／ ❌ 1 ＝ 14。実際は ✅ 8 ／ 🔺 5 ／ ❌ 1 だった ─ 下記。
#23 レビュー対応後は ✅ 14）**

**#18 時点の内訳は誤っていた。** A-12／A-13／A-14 を ✅ としていたが、変異による実測（2026-08-13）で
**タイプを区別する出力を固定するテストが辺③に 1 件も無い**ことが分かった（落ちるのは `RoundTripTest` の
3 件だけ＝🔺）。#18 時点の正しい内訳は **✅ 8 ／ 🔺 5（A-07・A-09・A-12・A-13・A-14）／ ❌ 1（A-01）**である。
根拠は [§3.1-3](#s3-1-3-sendsync)。

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| A-01 `DEFAULT` | ✅（#23 で ❌→✅） | `XlsFormatWriterModelTest#writesDefaultDataTypeMarker`（§3.1-3）。writer 側は到達可能である（`XlsFormatWriter` L400 がマーカー文字列を `block.getDataType().getName()` から組み立てるだけでタイプを絞らない。§0.8-7）。#18 時点は ❌。辺④が例外で弾くのとの非対称は `issues.md` **XLS-20** |
| A-02 `SETUP_TABLE_DATA` | ✅ | `writesTableBlock`, `metaRowContainsOnlyValueCells`, `appliesHeaderBackgroundColor`, `appliesSetupHeaderColor`, `drawsBlockOuterBorder`, `insertsBlankRowBetweenBlocks`, `appliesAutoColumnWidth`, `honorsConfigOverrides`, `writesWorkbookFileWithSheetPerSection`, `honorsMarkerColumnColorOverride`, `doesNotTintUnclosedBracketColumn`, `tintsMarkerColumn`, `wrapsIoFailure`, `roundTripsTable`, `roundTripsNullCellAsLiteralNullString` |
| A-03 `EXPECTED_TABLE_DATA` | ✅ | `writesTableMarkerWithGroupId`, `appliesExpectedHeaderColor` |
| A-04 `EXPECTED_COMPLETED` | ✅ | `writesExpectedCompleteTableMarker` |
| A-05 `LIST_MAP` | ✅ | `writesListMapBlock`, `appliesTestShotsHeaderColor`, `appliesOtherHeaderColorForNonTestShotsListMap`, `writesWorkbookFileWithSheetPerSection`, `roundTripsListMap` |
| A-06 `SETUP_FIXED` | ✅ | `writesFixedFileBlock`, `writesMultipleRecordLayouts`, `rejectsNullRecordTypeOnSecondRecord`, `rejectsEmptyRecordTypeOnSecondRecord`, `allowsNullRecordTypeOnSingleRecord`, `writesOmittedMetaAndFieldAsEmpty`, `roundTripsFixedFile`, `roundTripsMultipleRecordLayouts` |
| A-07 `EXPECTED_FIXED` | ✅（#23 で 🔺→✅） | `XlsFormatWriterModelTest#writesExpectedFixedFileBlockWithLengthRow`（§3.1-3）。`XlsFormatWriterTest` には 0 件のままで、#18 時点は `RoundTripTest#xls_expectedFixed_isPreserved` 経由の 🔺 だけだった（§0.8-8） |
| A-08 `SETUP_VARIABLE` | ✅ | `writesVariableFileWithoutLengthRow`, `roundTripsVariableFile` |
| A-09 `EXPECTED_VARIABLE` | ✅（#23 で 🔺→✅） | `XlsFormatWriterModelTest#writesExpectedVariableFileBlockWithoutLengthRow`（§3.1-3）。`XlsFormatWriterTest` には 0 件のままで、#18 時点は `RoundTripTest#xls_expectedVariable_isPreserved` 経由の 🔺 だけだった（§0.8-8） |
| A-10 `MESSAGE` | ✅ | `writesMessageBlock`, `appliesOtherHeaderColorForMessage`, `roundTripsMessage` |
| A-11 `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `writesSendSyncMessageWithSequenceNo`（L422 が識別セル全体をアサート）, `writesSequenceNoForAllSendSyncTypes`, `roundTripsSendSyncMessage` |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅（#23 レビュー対応で 🔺→✅） | `XlsFormatWriterModelTest#writesExpectedRequestBodyMessagesMarker`（[§3.1-3](#s3-1-3-sendsync)）。`writesSequenceNoForAllSendSyncTypes` は連番 `"1"` しか見ておらず**タイプを区別しない**（変異で実証） |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅（#23 レビュー対応で 🔺→✅） | `XlsFormatWriterModelTest#writesResponseHeaderMessagesMarker`（同上） |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅（#23 レビュー対応で 🔺→✅） | `XlsFormatWriterModelTest#writesResponseBodyMessagesMarker`（同上） |

**A-12／A-13／A-14 の判定は #18 以来 ✅ と書かれていたが誤りだった**（#23 レビュー指摘）。
`writesSequenceNoForAllSendSyncTypes` が固定するのは 4 タイプ共通の連番 `"1"` だけで、
`XlsFormatWriter#marker` をこの 3 タイプだけ別文字列にする変異を入れても落ちるのは `RoundTripTest` の 3 件
（＝🔺）だけだった。正式担保 0 の状態だったため 3 メソッドを追加して埋めた。実測と手順は
[§3.1-3](#s3-1-3-sendsync)。

**軸B（✅ 4 ／ 4）**

| 要素 | 判定 | 担保テストメソッド（代表） |
|---|---|---|
| B-1 `TableDataBlock` | ✅ | `writesTableBlock`, `roundTripsTable` ほか 16 件（計 18 件） |
| B-2 `ListMapBlock` | ✅ | `writesListMapBlock`, `roundTripsListMap` ほか 3 件（計 5 件） |
| B-3 `FileDataBlock` | ✅ | `writesFixedFileBlock`, `writesVariableFileWithoutLengthRow` ほか 8 件（計 10 件） |
| B-4 `MessageDataBlock` | ✅ | `writesMessageBlock`, `writesSendSyncMessageWithSequenceNo` ほか 4 件（計 6 件） |

**軸C（21 フィールド ─ #18 時点: 両状態担保 12 ／ 未担保 9。#23 完了後: 両状態担保 21 ／ 未担保 0）**

省略可能フィールドは「値あり」「省略」、空許容コレクションは「非空」「空」を別々に評価する。
n/a 6 件（C-01, C-03, C-05, C-07, C-10, C-19）は「省略」「空」という状態を持たない必須スカラー／2 値であり、
「値あり」の担保をもって両状態担保として数える。
**#18 時点で ❌ だった 9 件は #23 が埋めた**（担保テストメソッドは `XlsFormatWriterModelTest#` のもの。詳細は §3.1-3）。

| 要素 | 値あり／非空 | 省略／空 | 担保テストメソッド |
|---|---|---|---|
| C-01 `TestDataContainer.name` | ✅ | n/a | `writesWorkbookFileWithSheetPerSection`（`MyBook.xlsx`） |
| C-02 `TestDataContainer.sections` | ✅(複数=2) | ✅ 空（#23 で ❌→✅） | 複数: `writesWorkbookFileWithSheetPerSection`／空: `XlsFormatWriterModelTest#writesWorkbookWithoutSheetsWhenContainerHasNoSections`（シート 0 枚のブックになる。`issues.md` **XLS-23**）。辺③は writer 側であり `XlsFormatWriter#build` L125 が `container.getSections()` をループするため空・複数とも到達可能（§0.8-6） |
| C-03 `TestDataSection.name` | ✅ | n/a | `writesWorkbookFileWithSheetPerSection`（Sheet1/Sheet2） |
| C-04 `TestDataSection.blocks` | ✅ | ✅ 空（#23 で ❌→✅） | 非空: `writesTableBlock`, `writesFixedFileBlock` ほか 36 件（§3.1 で軸B 欄が空でない計 38 件）／空: `XlsFormatWriterModelTest#writesEmptySheetWhenSectionHasNoBlocks`（行を 1 行も持たないシートになる） |
| C-05 `TestDataBlock.dataType` | ✅ | n/a | `writesExpectedCompleteTableMarker`, `writesSequenceNoForAllSendSyncTypes` ほか 3 件（計 5 件） |
| C-06 `TestDataBlock.groupId` | ✅ | ✅ 省略(`""`) | 値あり: `writesTableMarkerWithGroupId`, `writesSendSyncMessageWithSequenceNo`, `roundTripsSendSyncMessage`／省略: `writesTableBlock`（`SETUP_TABLE=USERS` に `[]` が出ない） |
| C-07 `TestDataBlock.identifier` | ✅ | n/a | `writesTableBlock`, `writesListMapBlock` ほか 8 件（計 10 件） |
| C-08 `ColumnRowDataBlock.columnNames` | ✅ | ✅ 空（#23 で ❌→✅） | 非空: `writesTableBlock`, `writesListMapBlock` ほか 2 件（計 4 件）／空: `XlsFormatWriterModelTest#writesEmptyHeaderRowWhenColumnNamesAreEmpty`（カラム名行がデータ行の幅ぶんの空セルになる。`issues.md` **XLS-21**） |
| C-09 `ColumnRowDataBlock.rows` | ✅ | ✅ 空（#23 で ❌→✅） | 非空: `writesTableBlock`, `writesListMapBlock` ほか 3 件（計 5 件）／空: `XlsFormatWriterModelTest#writesTableWithoutDataRowsWhenRowsAreEmpty`（識別行とカラム名行だけになる） |
| C-10 `FileDataBlock.fileType` | ✅ FIXED / ✅ VARIABLE | n/a | **往復テストを除いても担保されている（変異で実証。下記）。** FIXED: `writesFixedFileBlock`（L257 が長さ行 `["", "-", "5"]` をアサート）, `XlsFormatWriterModelTest#writesExpectedFixedFileBlockWithLengthRow`（L252）／VARIABLE: `writesVariableFileWithoutLengthRow`（L283 が長さ行の無い版面をアサート）, `XlsFormatWriterModelTest#writesExpectedVariableFileBlockWithoutLengthRow`（L283）。`getFileType()` を明示アサートするのは 🔺 の往復 2 件（`roundTripsFixedFile` L941 ／ `roundTripsVariableFile` L999）だけだが、**版面が writer 側の観測点である**（C-11 と同じ） |
| C-11 `FileDataBlock.directives` | ✅ | ✅ 空 | 非空: `writesFixedFileBlock`, `writesOmittedMetaAndFieldAsEmpty`（ディレクティブ行が出る版面をアサート＝暗黙）／空: `writesVariableFileWithoutLengthRow`, `writesMultipleRecordLayouts`（ディレクティブ行が出ない版面をアサート＝暗黙。C-10 と同じく writer 側では版面が唯一の観測点であり、`getDirectives()` を読み戻すテストはない） |
| C-12 `FileDataBlock.records` | ✅ | ✅ 空（#23 で ❌→✅） | 非空: `writesFixedFileBlock`, `writesMultipleRecordLayouts`／空: `XlsFormatWriterModelTest#writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty`（識別行とディレクティブ行だけになる） |
| C-13 `MessageDataBlock.directives` | ✅（#23 で ❌→✅） | ✅ 空 | 値あり **2 件**: `XlsFormatWriterModelTest#writesDirectiveRowsBeforeFwHeaderRowsInMessage`（ディレクティブ行が FW 制御ヘッダ行より上に出る）, `#writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty`／空 **9 件**: `XlsFormatWriterTest` の 6 メソッド ＋ `XlsFormatWriterModelTest` の送信同期 3 メソッド。#18 時点は**値ありのテストが 0 件**だった（`XlsFormatWriterTest` の `new MessageDataBlock(...)` 6 箇所すべてで 4 引数目が空 `map()`。下表参照） |
| C-14 `MessageDataBlock.fwHeaderFields` | ✅ | ✅ 空 | 非空 **5 件**／空 **6 件**。内訳は下表。**#23 レビューで訂正**（空側に `writesSequenceNoForAllSendSyncTypes` が抜けていた） |
| C-15 `MessageDataBlock.records` | ✅ | ✅ 空（#23 で ❌→✅） | 非空 **10 件**／空 **1 件**（`XlsFormatWriterModelTest#writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty`）。内訳は下表。**#23 レビューで訂正**（非空を「計 2 件」と書いていたが、`XlsFormatWriterTest` の `new MessageDataBlock(...)` 6 箇所は<b>すべて</b>非空の `records` を渡している） |
| C-16 `RecordLayout.recordType` | ✅ | ✅ 省略(null) | 値あり: `writesFixedFileBlock`, `writesMultipleRecordLayouts`／省略: `allowsNullRecordTypeOnSingleRecord` |
| C-17 `RecordLayout.fields` | ✅ | ✅ 空（#23 で ❌→✅） | 非空: `writesFixedFileBlock`, `writesMessageBlock` ほか 2 件（計 4 件）／空: `XlsFormatWriterModelTest#writesRecordWithoutFieldColumnsWhenFieldsAreEmpty`（読み戻せない版面になる。`issues.md` **XLS-22**） |
| C-18 `RecordLayout.rows` | ✅ | ✅ 空（#23 で ❌→✅） | 非空: `writesFixedFileBlock`, `writesMessageBlock` ほか 5 件（計 7 件）／空: `XlsFormatWriterModelTest#writesRecordWithoutDataRowsWhenRecordRowsAreEmpty`（名前行・型行・長さ行までで値行が出ない） |
| C-19 `FieldDef.name` | ✅ | n/a | `writesFixedFileBlock`, `writesMessageBlock` ほか 2 件（計 4 件） |
| C-20 `FieldDef.type` | ✅ | ✅ 省略(null) | 値あり: `writesFixedFileBlock`／省略: `writesOmittedMetaAndFieldAsEmpty` |
| C-21 `FieldDef.length` | ✅ | ✅ 省略(null) | 値あり: `writesFixedFileBlock`（`"-"`/`"5"`）／省略: `writesVariableFileWithoutLengthRow`, `writesOmittedMetaAndFieldAsEmpty`, `roundTripsVariableFile` |

**C-10 `FileDataBlock.fileType` は往復テストを除いても担保されている（変異による実証。2026-08-13）。**
#23 レビューは「C-10 の**明示的**なアサートは往復テスト（`roundTripsFixedFile` ／ `roundTripsVariableFile`）だけ
＝🔺 ではないか」と指摘した。`XlsFormatWriter#layoutFile`（`src/main` L210）の
`boolean fixed = block.getFileType() == FileDataBlock.FileType.FIXED;` を `!=` に反転させて全件実行し、
落ちるテストを数えた。

```
$ JAVA_HOME=... mvn clean test -Djacoco.skip=true   # layoutFile の fixed 判定を反転
Tests run: 428, Failures: 11, Errors: 1
```

落ちた 12 件のうち **往復テストでないものが 8 件**あった。

| クラス | 落ちたテスト（往復でないもの） | 何を捉えたか |
|---|---|---|
| `XlsFormatWriterTest` | `writesFixedFileBlock`(L257) ／ `writesMultipleRecordLayouts`(L308) ／ `writesOmittedMetaAndFieldAsEmpty`(L794) | FIXED で長さ行が出る版面 |
| `XlsFormatWriterTest` | `writesVariableFileWithoutLengthRow`(L283) | VARIABLE で長さ行が出ない版面 |
| `XlsFormatWriterModelTest` | `writesExpectedFixedFileBlockWithLengthRow`(L252) ／ `writesRecordWithoutFieldColumnsWhenFieldsAreEmpty`(L603) ／ `writesRecordWithoutDataRowsWhenRecordRowsAreEmpty`(L631) | FIXED で長さ行が出る版面 |
| `XlsFormatWriterModelTest` | `writesExpectedVariableFileBlockWithoutLengthRow`(L283) | VARIABLE で長さ行が出ない版面 |

残る 4 件（`RoundTripTest#xls_setupFixed_isPreserved` ／ `#xls_expectedFixed_isPreserved` ／
`XlsFormatWriterTest#roundTripsFixedFile` ／ `#roundTripsMultipleRecordLayouts`）は往復であり数えない。
**FIXED 側・VARIABLE 側とも往復でないテストが落ちる**ため、C-10 は ✅ である。
`getFileType()` を読み戻して直接比べるアサートは往復にしか無いが、
**writer 側の観測点は版面である**（C-11 と同じ扱い。§凡例の「版面」を参照）。
変異は確認後に戻し、`git diff HEAD -- src/main` が 0 行であることを確かめた。

**C-13／C-14／C-15 の内訳（`new MessageDataBlock(...)` の全 9 定義箇所を実物で読んだ結果。2026-08-13）**

```
$ grep -rn -A3 "new MessageDataBlock(" src/test/java/nablarch/test/tool/converter/xls/*.java
```

`MessageDataBlock(DataType, groupId, identifier, directives, fwHeaderFields, records)` の
第 4／第 5／第 6 引数を読む。定義箇所は 9 つだが、そのうち 1 つ（`sendSyncMessage` ヘルパ）は 3 メソッドが
共有するため、テストメソッド単位では 11 件になる。

| 定義箇所 | テストメソッド | C-13 `directives` | C-14 `fwHeaderFields` | C-15 `records` |
|---|---|---|---|---|
| `XlsFormatWriterTest` L388 | `writesMessageBlock` | 空 | 非空 | 非空 |
| `XlsFormatWriterTest` L415 | `writesSendSyncMessageWithSequenceNo` | 空 | 空 | 非空 |
| `XlsFormatWriterTest` L526 | `appliesOtherHeaderColorForMessage` | 空 | 非空 | 非空 |
| `XlsFormatWriterTest` L816 | `writesSequenceNoForAllSendSyncTypes` | 空 | 空 | 非空 |
| `XlsFormatWriterTest` L1016 | `roundTripsMessage` | 空 | 非空 | 非空 |
| `XlsFormatWriterTest` L1043 | `roundTripsSendSyncMessage` | 空 | 空 | 非空 |
| `XlsFormatWriterModelTest` L374（`sendSyncMessage` ヘルパ） | `writesExpectedRequestBodyMessagesMarker` / `writesResponseHeaderMessagesMarker` / `writesResponseBodyMessagesMarker`（3 件） | 空 | 空 | 非空 |
| `XlsFormatWriterModelTest` L533 | `writesDirectiveRowsBeforeFwHeaderRowsInMessage` | **非空** | 非空 | 非空 |
| `XlsFormatWriterModelTest` L561 | `writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty` | **非空** | 非空 | **空** |
| **メソッド数の合計（11 件）** | | 非空 2 ／ 空 9 | 非空 5 ／ 空 6 | 非空 10 ／ 空 1 |

行番号は 2026-08-13（#23 レビュー対応後）の実測。`XlsFormatWriterTest` の 6 箇所は #23 でも
#23 レビュー対応でも中身を変えていない（変えたのは import・Javadoc・ヘルパ削除だけ。§3.1-3）。

**軸D（#18 時点: ✅ 0 ／ 🔺 2 ／ ❌ 6 ＝ 8。すべて `getCellType()` 観点。#22 完了後は ✅ 8）**

| 要素 | #18 の判定 | #22 後 | 備考 |
|---|---|---|---|
| D3-01 `"100"` | ❌ | ✅ | 数値セルにならないことのアサートなし → `XlsFormatWriterCellTypeTest#writesNumericLookingStringAsStringCell`（§3.1-2） |
| D3-02 `"=1+1"` | ❌ | ✅ | 数式解釈されないことのアサートなし → `#writesFormulaLookingStringAsStringCell`（§3.1-2） |
| D3-03 `"007"` | ❌ | ✅ | `#writesLeadingZeroStringAsStringCell` |
| D3-04 `null` | 🔺 | ✅ | `writesTableBlock`, `roundTripsNullCellAsLiteralNullString` が値（`"null"` 文字列化）はアサートするが `getCellType()` はしない。`RoundTripTest#nullCell_xlsConvertsToLiteralString_yamlPreservesNull` も同じく値のみ（§0.8-8）→ セル型は `#writesNullValueAsLiteralNullStringCell` |
| D3-05 `""` | 🔺 | ✅ | `writesTableBlock`, `writesListMapBlock` ほか 1 件が値のみアサート。`RoundTripTest#xls_setupTable_isPreserved`, `#xls_listMap_isPreserved` も同じく値のみ（§0.8-8）→ セル型は `#writesEmptyValueAsEmptyStringCell` |
| D3-06 改行含む文字列 | ❌ | ✅ | `#writesLineFeedStringAsStringCell`／`#replacesCrLfWithSingleLineFeedInStringCell`／`#replacesLoneCarriageReturnWithLineFeedInStringCell`（`issues.md` XLS-18） |
| D3-07 32767 文字超 | ❌ | ✅ | `#writesStringLongerThanExcelCellLimitAsStringCell`／`#writesStringOfExcelCellLimitLengthAsStringCell`（`issues.md` XLS-19） |
| D3-08 制御文字含む | ❌ | ✅ | `#replacesNulCharacterWithQuestionMark`／`#replacesBellCharacterWithQuestionMark`／`#replacesVerticalTabCharacterWithQuestionMark`／`#replacesUnitSeparatorCharacterWithQuestionMark`／`#writesTabCharacterAsIs`／`#writesDeleteCharacterAsIs`（`issues.md` XLS-17） |

`grep -rn "getCellType" src/test/` → #18 時点は **0 件**。セル読み出しヘルパ `cell`（L100-107）/`line`（L110-121）は
`getStringCellValue()` 固定であり、これは現在も変わっていない（#22 は `XlsFormatWriterTest` を変更していない）。
**辺③（writer 側）で `getCellType()` を使うのは #22 が追加した `XlsFormatWriterCellTypeTest` が最初である**
（観測結果は §3.1-2 の軸D 表）。src/test 全体で見ると最初は #22 ではなく **#19** であり、
`XlsFormatReaderCellTypeTest#readsTextFormattedNumericCellAsDoubleString`（L478）が
`assertThat("検証対象セルが数値セルであること", cell.getCellType(), is(Cell.CELL_TYPE_NUMERIC));` を持つ
（フィクスチャのセル型が意図どおりであることを確かめる用途）。この行を入れたコミットは
`git blame -L 478,478` で **`c04261d`**（"test(xls): #19 の4レビュー指摘を修正 …"）であり、#18 完了コミット
`5bf7048` より後である（2026-08-13 に実物で確認して訂正。当初この節は「`getCellType()` を使うのは
#22 が追加した `XlsFormatWriterCellTypeTest` である」と現在形で断定していたが事実に反していた）。
現在の `grep -rc "getCellType" src/test --include=*.java | grep -v ":0$"` は `XlsFormatReaderCellTypeTest:1` と
`XlsFormatWriterCellTypeTest:19` を返す（2026-08-13 実測）。
`| grep -v ":0$"` を落とすと `grep -rc` は走査した全 **35 ファイル**を返し、うち **33 行**が `:0` である
（`grep -rc "getCellType" src/test --include=*.java | wc -l` → **35**、
`grep -rc "getCellType" src/test --include=*.java | grep -c ":0$"` → **33**）。以前ここには
`| grep -v ":0$"` 無しのコマンドを載せたうえで上記 2 行だけを結果として書いていたが、これは出力そのものでは
なく部分集合であった（2026-08-13・第 3 ラウンドの指摘により訂正）。
**さらに #22 が書いた「34 ／ 32」は #23 が `XlsFormatWriterModelTest.java` を追加した時点で陳腐化していた**
（走査対象のファイルが 1 つ増え `:0` も 1 つ増える）。上の値は #23 レビュー対応時の実測に更新した
（2026-08-13）。この段落は「軸D 表は #22 の記述のまま」という断りの外にある ─
**現在形で検証可能な数字を主張する散文**だからである。なお `XlsFormatWriterCellTypeTest:19` は<b>行数</b>であって
アサート数ではない（§0.8-4 のとおり 19 行のうち 2 行はクラス Javadoc の散文で、アサートは 17 行）。

**軸E（#18 時点: 0 件の 3 要素が未担保。#23 完了後はすべて ✅）**

| 要素 | 0 件 | 1 件 | 複数件 | 担保テストメソッド |
|---|---|---|---|---|
| E-1 セクション内ブロック数 | ✅（#23 で ❌→✅） | ✅ | ✅ | 0: `XlsFormatWriterModelTest#writesEmptySheetWhenSectionHasNoBlocks`（§3.1-3）／1: `writesTableBlock`, `writesFixedFileBlock` ほか 34 件（§3.1 の軸B 欄が空でない 38 件から複数ブロックの 2 件を除く 36 件。§3.1 の軸E 欄には E-1(1) を明記していない）／複数: `insertsBlankRowBetweenBlocks`, `honorsConfigOverrides`（計 2 件） |
| E-2 ブロック内行数 | ✅（#23 で ❌→✅） | ✅ | ✅ | 0: `XlsFormatWriterModelTest#writesTableWithoutDataRowsWhenRowsAreEmpty`（テーブル経路）／`#writesRecordWithoutDataRowsWhenRecordRowsAreEmpty`（ファイル経路の値行）（§3.1-3）／1: `metaRowContainsOnlyValueCells`, `writesTableMarkerWithGroupId` ほか 3 件（計 5 件）／複数: `writesTableBlock`, `writesListMapBlock` ほか 3 件（計 5 件） |
| E-3 ファイル内レコードレイアウト数 | ✅（#23 で ❌→✅） | ✅ | ✅ | 0: `XlsFormatWriterModelTest#writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty`（ファイル経路）／`#writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty`（メッセージ経路）（§3.1-3）／1: `writesFixedFileBlock`, `writesVariableFileWithoutLengthRow` ほか 8 件（計 10 件）／複数: `writesMultipleRecordLayouts`, `roundTripsMultipleRecordLayouts` ほか 2 件（計 4 件） |
| E-4 コンテナ内セクション数（辺③の実体: ブック内シート数） | n/a | ✅ | ✅ | 1: `writesTableBlock`, `writesFixedFileBlock` ほか 35 件（`writesWorkbookFileWithSheetPerSection` を除く 37 件。§3.1 の軸E 欄には E-4(1) を明記していない）／複数: **`writesWorkbookFileWithSheetPerSection`**（辺③で唯一） |

**E-4 の「0 件」欄が `n/a` である理由**（読者が C-02 行と突き合わせたときに矛盾に見えるため補足。2026-08-13 追記）:
§0.6 が定める E-4 の多重度は**「1 件」と「複数件」の 2 つだけ**で、E-1〜E-3 と違い 0 件を要素に持たない。
よって `n/a` は「0 件を通していない」ではなく「**軸E に 0 件という要素が存在しない**」という意味である。
一方、軸C の C-02（`TestDataContainer.sections` 空）は #23 で ✅ になっており、
**セクション 0 件の入力自体は `XlsFormatWriterModelTest#writesWorkbookWithoutSheetsWhenContainerHasNoSections`
が通している**（シート 0 枚のブックが書き出される。`issues.md` XLS-23）。
担保の穴ではなく、軸E と軸C で要素の切り方が違うだけである。

**軸F（#18 時点: ✅ 0 ／ 🔺 1 ／ ❌ 2 ／ 対象外 1 ＝ 4。#22 完了後は ✅ 3 ／ 対象外 1）**

| 要素 | #18 の判定 | #22 後 | 担保テストメソッド |
|---|---|---|---|
| F3-01 出力先不在 | 🔺 | ✅ | 🔺: `wrapsIoFailure`（正確には「親に通常ファイルが居座り親ディレクトリを作れない」ケース。「出力先不在」そのものではない）／✅: `XlsFormatWriterInvalidOutputTest#createsMissingOutputDirectoriesAndWritesWorkbook`（§3.1-2） |
| F3-02 `overwrite=false` 衝突 | **対象外（衝突検査は上位層）** | **対象外（変更なし）** | `overwrite` を保持するのは `ConversionRequest` / `TestDataConverter` / `ConverterMojo` であり `XlsFormatWriter` は保持しない。共通処理 `TestDataConverter#checkOverwrite`（L90-99）の分岐は上位層の既存テスト（L336／L267）が通しているが、**両者とも XLS→YAML であり `.xlsx` を出力側とする衝突は未担保**である（§0.8-5 の訂正）。#22 でも辺③の対象外のままとした（`XlsFormatWriter` 単体では再現できない） |
| F3-03 書き込み権限なし | ❌ | ✅ | `XlsFormatWriterInvalidOutputTest#wrapsAccessDeniedExceptionWhenOutputDirectoryIsNotWritable`（§3.1-2） |
| F3-04 シート名が Excel 制約違反 | ❌ | ✅ | `XlsFormatWriterInvalidOutputTest` の 14 件（禁止文字 7 件 `#rejectsSheetNameContainingSlash`〜`#rejectsSheetNameContainingColon`／`#rejectsEmptySheetName`／`#writesSheetNameOfExcelLimitLengthAsIs`／`#truncatesSheetNameLongerThanExcelLimitSilently`／`#writesSheetNameWhoseForbiddenCharacterIsRemovedByTruncation`／`#rejectsSheetNameWhoseForbiddenCharacterSurvivesTruncation`／`#failsWhenTruncatedSheetNamesCollide`／`#failsWhenSheetNamesDifferOnlyInCase`）。全メソッド名と担保範囲（アポストロフィ・`null` は範囲外）は §3.1-2。`issues.md` XLS-16 |
| （steering 外で担保済みの異常系） | ✅ | `rejectsNullRecordTypeOnSecondRecord`, `rejectsEmptyRecordTypeOnSecondRecord`（2 レコード目 recordType 空 → `IllegalStateException`）、`rejectsNegativeBlankRows`（設定値負数 → `IllegalArgumentException`） |

<a id="s3-3"></a>

### 3.3 辺③ 未担保一覧（#22〜#23 が埋めた対象）

計上単位と「状態」の 3 分類は §1.3 の規則に従う。

**本表は #23 の実測結果に合わせて更新した（2026-08-13）。** #18 時点は「要追加 26 ／ 対象外 1」、
#22 完了時点は「要追加 15 ／ 担保済み 11 ／ 対象外 1」であった。#23 が残る 15 件（軸A 3・軸C 9・軸E 3）を
埋めたため、**辺③の「要追加」は 0 件**になった。#18 時点の分類は各行の「#18」列に、
#22 完了時点の分類は「#22 後」列に残した。

**ただし #18 の「要追加 26」自体が 3 件少なかった（2026-08-13・#23 レビュー指摘）。**
軸A の A-12／A-13／A-14 は #18 で ✅ と判定されていたため本表に 1 件も上がっていなかったが、
実際には正式担保 0（🔺 のみ）だった（変異で実証。[§3.1-3](#s3-1-3-sendsync)）。
**#23 レビュー対応でテスト 3 件を追加して埋めた**うえで、下表の A 行に追記した。
これに伴い辺③の軸要素の総計は 27 → **30**（うち対象外 1）になる。

| 軸 | 未担保要素 | #18 の状態 | #22 後の状態 | #23 後の状態 | 件数 |
|---|---|---|---|---|---|
| A | A-01 `DEFAULT`（writer 側は到達可能。§0.8-7）／A-07 `EXPECTED_FIXED`（🔺 `RoundTripTest#xls_expectedFixed_isPreserved`）／A-09 `EXPECTED_VARIABLE`（🔺 `RoundTripTest#xls_expectedVariable_isPreserved`） | 要追加 | 要追加（#23。#22 の対象外） | **担保済み（#23）** — 順に `XlsFormatWriterModelTest#writesDefaultDataTypeMarker`／`#writesExpectedFixedFileBlockWithLengthRow`／`#writesExpectedVariableFileBlockWithoutLengthRow`（§3.1-3）。記録した課題は `issues.md` **XLS-20** | 3 |
| A | **A-12 `EXPECTED_REQUEST_BODY_MESSAGES`／A-13 `RESPONSE_HEADER_MESSAGES`／A-14 `RESPONSE_BODY_MESSAGES`**（#18 は ✅ と誤判定。実際は 🔺 `RoundTripTest` の 3 件のみ） | （表に上がっていなかった） | （同左） | **担保済み（#23 レビュー対応）** — 順に `XlsFormatWriterModelTest#writesExpectedRequestBodyMessagesMarker`／`#writesResponseHeaderMessagesMarker`／`#writesResponseBodyMessagesMarker`（[§3.1-3](#s3-1-3-sendsync)）。変異で穴と歯の両方を実証済み | 3 |
| B | （なし） | — | — | — | 0 |
| C | C-02 sections 空（writer 側は到達可能。§0.8-6）／C-04 blocks 空／C-08 columnNames 空／C-09 rows 空／C-12 FileDataBlock.records 空／**C-13 MessageDataBlock.directives 値あり**／C-15 MessageDataBlock.records 空／C-17 fields 空／C-18 RecordLayout.rows 空 | 要追加 | 要追加（#23。#22 の対象外） | **担保済み（#23）** — 順に `XlsFormatWriterModelTest#writesWorkbookWithoutSheetsWhenContainerHasNoSections`／`#writesEmptySheetWhenSectionHasNoBlocks`／`#writesEmptyHeaderRowWhenColumnNamesAreEmpty`／`#writesTableWithoutDataRowsWhenRowsAreEmpty`／`#writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty`／`#writesDirectiveRowsBeforeFwHeaderRowsInMessage`／`#writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty`／`#writesRecordWithoutFieldColumnsWhenFieldsAreEmpty`／`#writesRecordWithoutDataRowsWhenRecordRowsAreEmpty`（§3.1-3）。記録した課題は `issues.md` **XLS-21〜XLS-23** | 9 |
| D | D3-01〜D3-08 全 8 ケース（D3-04／D3-05 は値のみの 🔺。`getCellType()` をアサートするテストは全件ゼロ） | 要追加 | **担保済み（#22）** — `XlsFormatWriterCellTypeTest` 18 件（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterCellTypeTest.java` → 18）。内訳は **8 ケース**＋改行の異表記 **2 件**・上限ちょうど **1 件**・XML で表現できない制御文字を 1 文字 1 メソッドへ展開した増分 **3 件**・XML で正当な制御文字の対照 **2 件**（ここまで 16 件。読み戻したセル型と値を突き合わせる分）＋ `xl/sharedStrings.xml` の**生バイト**を検査する **2 件**（`burnsQuestionMarkIntoSharedStringsXmlForControlCharacter`／`keepsCarriageReturnRawInSharedStringsXml`。第 3 ラウンドで追加）＝ 8＋2＋1＋3＋2＋2 ＝ **18**。要素別の担保テストメソッドは §3.1-2 の軸D 表。記録した課題は `issues.md` **XLS-17〜XLS-19** | 担保済み（変更なし） | 8 |
| E | E-1(0 件)／E-2(0 件)／E-3(0 件) | 要追加 | 要追加（#23。#22 の対象外） | **担保済み（#23）** — E-1(0) は C-04、E-2(0) は C-09／C-18、E-3(0) は C-12／C-15 と同じ入力（上の C 行のテストメソッド。§3.1-3 の軸E 表） | 3 |
| F | F3-01 出力先不在（🔺 のみ）／F3-03 書き込み権限なし／F3-04 シート名制約違反 | 要追加 | **担保済み（#22）** — `XlsFormatWriterInvalidOutputTest` 16 件（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterInvalidOutputTest.java` → 16）。内訳は F3-01 **1 件**（`createsMissingOutputDirectoriesAndWritesWorkbook`）・F3-03 **1 件**（`wrapsAccessDeniedExceptionWhenOutputDirectoryIsNotWritable`）・F3-04 **14 件**（禁止文字を 1 文字 1 メソッドへ展開した **7 件**＋空文字 **1 件**＋31 文字ちょうど **1 件**＋31 文字超の黙った切り詰め **1 件**＋切り詰めが禁止文字検査を無効化する境界 **2 件**＋切り詰め後の衝突 **1 件**＋大文字小文字だけが違う名前の衝突 **1 件**（`failsWhenSheetNamesDifferOnlyInCase`。第 3 ラウンドで追加）＝ 7＋1＋1＋1＋2＋1＋1 ＝ 14。メソッド名の全列挙は §3.2 の軸F 表）＝ 1＋1＋14 ＝ **16**。要素別の担保テストメソッドは §3.1-2 の軸F 表。記録した課題は `issues.md` **XLS-16** | 担保済み（変更なし） | 3 |
| F | F3-02 `overwrite=false` 衝突 — `XlsFormatWriter` は `overwrite` を保持しない。衝突検査は上位層の `TestDataConverter#checkOverwrite`（L90-99）で完結する。既存テスト（L336／L267）が通すのは XLS→YAML の経路であり、**`.xlsx` を出力側とする衝突は未担保**（§0.8-5 の訂正） | 対象外（衝突検査は上位層） | 対象外（変更なし。#22 でも辺③に書かない） | 対象外（変更なし。#23 でも辺③に書かない） | 1 |
| **合計** | | **要追加 26（実際は 29）／ 到達不能 0 ／ 対象外 1** | **要追加 15（実際は 18）／ 担保済み 11 ／ 到達不能 0 ／ 対象外 1** | **要追加 0 ／ 担保済み 29 ／ 到達不能 0 ／ 対象外 1** | **30（うち対象外 1）** |

**合計の検算**（表の「件数」列を上から順に足す）:

- 担保済み: A 3 ＋ A 3（送信同期。#23 レビュー対応）＋ C 9 ＋ D 8 ＋ E 3 ＋ F 3 ＝ **29**
- 要追加: **0**
- 対象外: F3-02 **1**
- 総計: 29 ＋ 0 ＋ 1 ＝ **30**（B は 0 件）

「#18」「#22 後」の列に括弧で添えた「実際は」は、A-12〜A-14 が当時から未担保だったことを
遡って数え直した値である（当時の表には行として存在しなかった）。

**軸要素の外に、開示すべき担保の穴が 1 つある。** `XlsFormatWriter#write` の `parent == null` 分岐（L102-106）は
src/test 全体で一度も通っていない。§0.7 の軸F 4 要素のいずれにも当たらないため上表には算入していないが、
F3-01 の隣接領域であり `src/main` のコメントが「null チェックが必須」と明記している分岐であるため開示する。
到達経路の全数と実測は [§3.1-2 の該当項](#s3-1-2-parent-null)。

**#23 レビューで判明した「表に上がっていなかった穴」の教訓。** A-12〜A-14 は「そのデータタイプの
ブロックを入力に与えて何かをアサートしている」テストが存在したため ✅ と判定されていたが、
アサートしていた値（連番 `"1"`）は**4 タイプで同じ**であり、タイプを区別していなかった。
判定基準（§判定基準）の「その `DataType` のブロックが生成／書き出されることをアサートしている」を
満たすかどうかは、**入力の型ではなく出力がその型に依存しているか**で見る必要がある。
迷ったときは変異（その型だけ出力を変える）を入れて落ちるテストを見るのが確実である。

**特に大きな空欄**（#18 時点の評価）: `getCellType()` を使ったテストが 1 件も存在しないため軸D 8 ケース全滅。#22 の主眼。
→ **#22 で解消した。** 次いで `MessageDataBlock.directives` に値を入れて書き出すテストが 0 件（C-13）。
→ **#23 で解消した**（`XlsFormatWriterModelTest#writesDirectiveRowsBeforeFwHeaderRowsInMessage`）。

**辺③の「要追加」は 0 件である。** ただし上の `parent == null` 分岐に加え、#23 の JaCoCo 実測で
`XlsFormatWriter` の未到達が 3 箇所（分岐 3 / 100・行 1 / 151）であることを確かめた。
残る 2 箇所（`layout` の未知ブロック向け `throw`、`isMarkerColumn` の `null` ガード）はいずれも
Java イディオムとしての安全網であり軸要素ではない。内訳は §3.1-3 末尾の JaCoCo 表。

---

<a id="s4"></a>

## 4. 辺④ 中間モデル→YAML（`YamlFormatWriterTest` 33 件）

### 4.1 テストメソッド → 担保する軸要素

| # | テストメソッド | 軸A | 軸B | 軸C | 軸D | 軸E | 軸F |
|---|---|---|---|---|---|---|---|
| 1 | `serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | A-02 | B-1 | C-06(省略→`group_id` キーなし), C-07, C-08, C-09 | **D4-04 `null`** ✅, **D4-05 `""`** ✅, ※`${}` の全値クォート | E-2(複数=2) | — |
| 2 | `serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId` | A-03 | B-1 | C-06(値あり `[case01]`→`case01`) | — | E-1(複数=2) | — |
| 3 | `serializeTable_completed_usesExpectedCompleteTablesKey` | A-04 | B-1 | C-05 | — | E-2(1) | — |
| 4 | `serializeListMap_usesIdKeyAndColumnOrder` | A-05 | B-2 | C-07, C-08, C-09 | D4-04 `null` | E-2(複数=2) | — |
| 5 | `serializeFile_fixedWithDirectivesAndOmittedLength` | A-06 | B-3 | C-07, C-10(FIXED), C-11(値あり), C-12(2件), C-16(値あり), C-17, C-18, C-19, C-20, C-21(値あり＋省略) | D4-05 `""` | **E-3(複数=2)**, E-2(複数=2) | — |
| 6 | `serializeFile_variableOmitsDirectivesAndRecordTypeAndLength` | A-09 | B-3 | C-10(VARIABLE), **C-11(空)**, **C-16(省略)**, **C-21(省略)** | — | E-3(1) | — |
| 7 | `serializeMessage_withDirectivesAndFwHeader` | A-10 | B-4 | C-07, **C-13(値あり)** ✅, C-14(値あり), C-15, C-16, C-17, C-18, C-19, C-20, C-21 | ※`${}` | E-3(1) | — |
| 8 | `serializeMessage_emptyBody_emitsIdOnly` | A-10 | B-4 | C-07, C-13(空), C-14(空), **C-15(空)** ✅ | — | **E-3(0)** ✅ | — |
| 9 | `serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField` | A-11 | B-4 | C-06(値あり), C-07, C-13(空), C-14(空), C-16(省略), C-17, C-18, C-19, C-20, C-21 | ※`${}` | E-3(1) | — |
| 10 | `serializeSendSync_allFourSectionKeys` | A-11, A-12, A-13, A-14 | B-4 | C-05 | — | E-1(複数=4) | — |
| 11 | `serialize_multipleSections_separatedByBlankLineInEncounterOrder` | A-02, A-10 | B-1, B-4 | C-05 | — | **E-1(複数=2)** | — |
| 12 | `serialize_emptySection_isEmptyString` | — | — | **C-04(空)** ✅ | — | **E-1(0)** ✅ | — |
| 13 | `serialize_escapesQuotesBackslashAndControlChars` | A-02 | B-1 | C-09 | **D4-07 改行含む** ✅（`\n`/`\r`/`\t`/`\x01`/`"`/`\` のエスケープ） | E-2(1) | — |
| 14 | `serialize_surrogatePair_isOutputAsUtf8WithoutEscape` | A-02 | B-1 | C-09 | ※BMP 外文字（U+1F600）の非エスケープ出力 | E-2(複数=2) | — |
| 15 | `serialize_quotesKeyContainingSpecialChars` | A-10 | B-4 | C-13(値あり) | ※**キー**中のコロン・空白のクォート（D4-09 の値側ではない） | — | — |
| 16 | `serialize_emptyKey_isQuoted` | A-10 | B-4 | C-13(値あり) | ※空キーのクォート | — | — |
| 17 | `serialize_distinguishesNullFromNullString` | A-02 | B-1 | C-09 | **D4-03 `"null"`** ✅, **D4-04 `null`** ✅ | E-2(複数=2) | — |
| 18 | `serialize_emptyRows_emitsEmptyFlowList` | A-02 | B-1 | **C-09(空)** ✅ | — | **E-2(0)** ✅ | — |
| 19 | `serialize_emptyColumnsRow_emitsEmptyFlowMap` | A-02 | B-1 | **C-08(空)** ✅ | — | E-2(1) | — |
| 20 | `serialize_recordWithEmptyFieldsAndRows_emitsEmptyFlowLists` | A-06 | B-3 | **C-17(空)** ✅, **C-18(空)** ✅, C-16(省略) | — | E-3(1) | — |
| 21 | `serialize_rowShorterThanColumns_fillsMissingWithNull` | A-02 | B-1 | C-08, C-09 | D4-04 `null`（補完） | — | ✅ 行と列の数の不一致（行が短い → null 補完） |
| 22 | `serialize_fieldWithNullType_omitsType` | A-09 | B-3 | **C-20(省略)** ✅ | — | E-3(1) | — |
| 23 | `serialize_keyStartingWithIndicator_isQuoted` | A-10 | B-4 | C-13(値あり) | ※**キー**先頭の YAML インジケータ `-`（D4-09 の値側ではない） | — | — |
| 24 | `serialize_unbracketedGroupId_isUsedAsRawValue` | A-02 | B-1 | C-06(非整形値 `raw`) | — | — | ※防御的経路（`[]` で囲まれていない groupId） |
| 25 | `serialize_unsupportedDataType_throws` | **A-01 `DEFAULT`** ✅ | B-1 | C-05 | — | — | ✅ 未サポート `DataType` → `IllegalArgumentException` |
| 26 | `write_ioError_throwsUncheckedIOException` | A-02 | B-1 | — | — | — | 🔺**F4-01**（親に通常ファイルが居座り出力先を作れない）→ `UncheckedIOException` |
| 27 | `write_writesEachSectionAsYamlFileWithSerializedContent` | A-02 | B-1 | C-01, C-02(1件), C-03 | — | E-4(1) | — |
| 28 | `roundTrip_table_isPreservedThroughRealReader` | A-02 | B-1 | C-05, C-07, C-08, C-09 | ※実 YAML 往復（`${}`/`null`/`""`） | E-2(複数=2) | — |
| 29 | `roundTrip_fixedFile_isPreservedThroughRealReader` | A-06 | B-3 | C-05, C-07, C-10(FIXED), C-12(2件), C-16, C-19, C-20, C-21(値あり＋省略), C-18 | ※実 YAML 往復 | E-3(複数=2) | — |
| 30 | `roundTrip_message_preservesFwHeaderAndBody` | A-10 | B-4 | C-05, C-07, C-14(値あり), C-16, C-18 | ※`${}` の往復 | E-3(1) | — |
| 31 | `roundTrip_sendSync_preservesGroupIdAndNoField` | A-11 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-17, C-18, C-19, C-20, C-21 | ※`${}` の往復 | E-3(1) | — |
| 32 | `roundTrip_leadingTrailingWhitespace_isPreservedThroughRealReader` | A-02 | B-1 | C-09 | ※前後・中間の半角/全角空白が往復で脱落しない | E-2(1) | — |
| 33 | `roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader` | A-02 | B-1 | C-09 | 🔺**D4-01 `"100"` 相当（`"123"`）**・D4-03 `"null"`・D4-04 `null` の往復区別（出力 YAML の記法アサートではない） | E-2(複数=3) | — |

### 4.2 軸要素 → 担保テストメソッド

**軸A（✅ 12 ／ 🔺 2 ／ ❌ 0 ＝ 14）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| A-01 `DEFAULT` | ✅ | `serialize_unsupportedDataType_throws`（**4 辺で唯一 `DEFAULT` を通すテスト**） |
| A-02 `SETUP_TABLE_DATA` | ✅ | `serializeTable_setupNoGroup_...`, `serialize_multipleSections_...`, `serialize_escapesQuotesBackslashAndControlChars`, `serialize_surrogatePair_...`, `serialize_distinguishesNullFromNullString`, `serialize_emptyRows_...`, `serialize_emptyColumnsRow_...`, `serialize_rowShorterThanColumns_...`, `serialize_unbracketedGroupId_...`, `write_ioError_...`, `write_writesEachSectionAsYamlFile...`, `roundTrip_table_...`, `roundTrip_leadingTrailingWhitespace_...`, `roundTrip_nullAndNullStringAndNumeric_...` |
| A-03 `EXPECTED_TABLE_DATA` | ✅ | `serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId` |
| A-04 `EXPECTED_COMPLETED` | ✅ | `serializeTable_completed_usesExpectedCompleteTablesKey` |
| A-05 `LIST_MAP` | ✅ | `serializeListMap_usesIdKeyAndColumnOrder` |
| A-06 `SETUP_FIXED` | ✅ | `serializeFile_fixedWithDirectivesAndOmittedLength`, `serialize_recordWithEmptyFieldsAndRows_...`, `roundTrip_fixedFile_...` |
| A-07 `EXPECTED_FIXED` | 🔺 | `YamlFormatWriterTest` には 0 件。`RoundTripTest#yaml_expectedFixed_isPreserved` が `YamlFormatWriter` で EXPECTED_FIXED ブロックを実 `.yaml` へ書き出す（§0.8-8。重複を避けること） |
| A-08 `SETUP_VARIABLE` | 🔺 | `YamlFormatWriterTest` には 0 件。`RoundTripTest#yaml_setupVariable_isPreserved` が `YamlFormatWriter` で SETUP_VARIABLE ブロックを実 `.yaml` へ書き出す（§0.8-8。重複を避けること） |
| A-09 `EXPECTED_VARIABLE` | ✅ | `serializeFile_variableOmitsDirectivesAndRecordTypeAndLength`, `serialize_fieldWithNullType_omitsType` |
| A-10 `MESSAGE` | ✅ | `serializeMessage_withDirectivesAndFwHeader`, `serializeMessage_emptyBody_emitsIdOnly`, `serialize_multipleSections_...`, `serialize_quotesKeyContainingSpecialChars`, `serialize_emptyKey_isQuoted`, `serialize_keyStartingWithIndicator_isQuoted`, `roundTrip_message_...` |
| A-11 `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField`, `serializeSendSync_allFourSectionKeys`, `roundTrip_sendSync_...` |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `serializeSendSync_allFourSectionKeys` |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅ | `serializeSendSync_allFourSectionKeys` |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅ | `serializeSendSync_allFourSectionKeys` |

**軸B（✅ 4 ／ 4）**

| 要素 | 判定 | 担保テストメソッド（代表） |
|---|---|---|
| B-1 `TableDataBlock` | ✅ | `serializeTable_setupNoGroup_...`, `serializeTable_withGroupsSameType_...` ほか 15 件（計 17 件） |
| B-2 `ListMapBlock` | ✅ | **`serializeListMap_usesIdKeyAndColumnOrder`**（辺④で唯一） |
| B-3 `FileDataBlock` | ✅ | `serializeFile_fixedWithDirectivesAndOmittedLength`, `serializeFile_variableOmits...` ほか 3 件（計 5 件） |
| B-4 `MessageDataBlock` | ✅ | `serializeMessage_withDirectivesAndFwHeader`, `serializeMessage_emptyBody_emitsIdOnly` ほか 8 件（計 10 件） |

**軸C（21 フィールド ─ 両状態担保 19 ／ 未担保 2）**

省略可能フィールドは「値あり」「省略」、空許容コレクションは「非空」「空」を別々に評価する。
n/a 6 件（C-01, C-03, C-05, C-07, C-10, C-19）は「省略」「空」という状態を持たない必須スカラー／2 値であり、
「値あり」の担保をもって両状態担保として数える。
未担保 2 件は C-02（sections 空・複数が欠）と C-12（records 空が欠）。4 つの辺のなかでもっとも充実している。

| 要素 | 値あり／非空 | 省略／空 | 担保テストメソッド |
|---|---|---|---|
| C-01 `TestDataContainer.name` | ✅ | n/a | `write_writesEachSectionAsYamlFileWithSerializedContent`（`td.yaml`） |
| C-02 `TestDataContainer.sections` | ✅(1件) | ❌ 空／❌ 複数 | `write_writesEachSectionAsYamlFileWithSerializedContent`（辺④は writer 側であり `YamlFormatWriter#write` L74 が `container.getSections()` をループするため、空・複数とも到達可能。§0.8-6） |
| C-03 `TestDataSection.name` | ✅ | n/a | `write_writesEachSectionAsYamlFileWithSerializedContent` |
| C-04 `TestDataSection.blocks` | ✅ | ✅ 空 | 空: `serialize_emptySection_isEmptyString` |
| C-05 `TestDataBlock.dataType` | ✅ | n/a | `serializeTable_completed_...`, `serializeSendSync_allFourSectionKeys` ほか 6 件（計 8 件） |
| C-06 `TestDataBlock.groupId` | ✅ | ✅ 省略(`""`) | 値あり: `serializeTable_withGroupsSameType_...`, `serializeSendSync_requiresGroupId...`, `serialize_unbracketedGroupId_...`／省略: `serializeTable_setupNoGroup_...` |
| C-07 `TestDataBlock.identifier` | ✅ | n/a | `serializeTable_setupNoGroup_...`, `serializeListMap_usesIdKeyAndColumnOrder` ほか 8 件（計 10 件） |
| C-08 `ColumnRowDataBlock.columnNames` | ✅ | ✅ 空 | 空: `serialize_emptyColumnsRow_emitsEmptyFlowMap` |
| C-09 `ColumnRowDataBlock.rows` | ✅ | ✅ 空 | 空: `serialize_emptyRows_emitsEmptyFlowList` |
| C-10 `FileDataBlock.fileType` | ✅ FIXED / ✅ VARIABLE | n/a | FIXED: `serializeFile_fixedWithDirectivesAndOmittedLength`（`type: "fixed"`）／VARIABLE: `serializeFile_variableOmits...`（`type: "variable"`） |
| C-11 `FileDataBlock.directives` | ✅ | ✅ 空 | 非空: `serializeFile_fixedWith...`／空: `serializeFile_variableOmits...`（`directives:` キーが出ない） |
| C-12 `FileDataBlock.records` | ✅ | ❌ 空 | 非空: `serializeFile_fixedWith...`（2 件） |
| C-13 `MessageDataBlock.directives` | ✅ | ✅ 空 | 非空: `serializeMessage_withDirectivesAndFwHeader`, `serialize_quotesKeyContainingSpecialChars`, `serialize_emptyKey_isQuoted`, `serialize_keyStartingWithIndicator_isQuoted`／空: `serializeMessage_emptyBody_emitsIdOnly` |
| C-14 `MessageDataBlock.fwHeaderFields` | ✅ | ✅ 空 | 非空: `serializeMessage_withDirectivesAndFwHeader`, `roundTrip_message_...`／空: `serializeMessage_emptyBody_...`, `serializeSendSync_requiresGroupId...` |
| C-15 `MessageDataBlock.records` | ✅ | ✅ 空 | 空: `serializeMessage_emptyBody_emitsIdOnly` |
| C-16 `RecordLayout.recordType` | ✅ | ✅ 省略(null) | 値あり: `serializeFile_fixedWith...`, `serializeMessage_withDirectives...`／省略: `serializeFile_variableOmits...`, `serializeSendSync_requiresGroupId...`, `serialize_recordWithEmptyFieldsAndRows_...` |
| C-17 `RecordLayout.fields` | ✅ | ✅ 空 | 空: `serialize_recordWithEmptyFieldsAndRows_emitsEmptyFlowLists` |
| C-18 `RecordLayout.rows` | ✅ | ✅ 空 | 空: `serialize_recordWithEmptyFieldsAndRows_emitsEmptyFlowLists` |
| C-19 `FieldDef.name` | ✅ | n/a | `serializeFile_fixedWith...`, `serializeMessage_withDirectivesAndFwHeader` ほか 3 件（計 5 件） |
| C-20 `FieldDef.type` | ✅ | ✅ 省略(null) | 省略: `serialize_fieldWithNullType_omitsType` |
| C-21 `FieldDef.length` | ✅ | ✅ 省略(null) | 値あり: `serializeFile_fixedWith...`（`"5"`）／省略: `serializeFile_fixedWith...`（`f2`）, `serializeFile_variableOmits...` |

**軸D（✅ 4 ／ 🔺 2 ／ ❌ 3 ＝ 9）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| D4-01 `"100"` | 🔺 | `roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader`（`"123"` を往復で区別。**出力 YAML の記法（クォート有無）をアサートしていない**）。`RoundTripTest` の YAML 経路も数値文字列を往復させるが記法はアサートしない（§0.8-8） |
| D4-02 `"true"` | ❌ | — |
| D4-03 `"null"` | ✅ | `serialize_distinguishesNullFromNullString`（`V: "null"`）, `roundTrip_nullAndNullStringAndNumeric_...` |
| D4-04 `null` | ✅ | `serializeTable_setupNoGroup_...`（`NOTE: null`）, `serializeListMap_usesIdKeyAndColumnOrder`, `serialize_distinguishesNullFromNullString`, `serialize_rowShorterThanColumns_fillsMissingWithNull` |
| D4-05 `""` | ✅ | `serializeTable_setupNoGroup_...`（`NAME: ""`）, `serializeFile_fixedWith...`（`[""]`） |
| D4-06 `"007"` | ❌ | — |
| D4-07 改行含む | ✅ | `serialize_escapesQuotesBackslashAndControlChars`（`\n`/`\r` を `\\n`/`\\r` へエスケープ） |
| D4-08 `"2026-08-07"` | ❌ | — |
| D4-09 コロン・ハイフン・`#` 含む | 🔺 | **キー側のみ**: `serialize_quotesKeyContainingSpecialChars`（`a:b`, `c d`）, `serialize_keyStartingWithIndicator_isQuoted`（`-x`）。**値側は未担保**。`#` はキー・値とも未担保 |

**軸E**

| 要素 | 0 件 | 1 件 | 複数件 | 担保テストメソッド |
|---|---|---|---|---|
| E-1 セクション内ブロック数 | ✅ | ✅ | ✅ | 0: `serialize_emptySection_isEmptyString`／複数: `serializeTable_withGroupsSameType_...`, `serializeSendSync_allFourSectionKeys`, `serialize_multipleSections_...` |
| E-2 ブロック内行数 | ✅ | ✅ | ✅ | 0: `serialize_emptyRows_emitsEmptyFlowList`／複数: `serializeTable_setupNoGroup_...`, `serializeFile_fixedWith...`, `roundTrip_nullAndNullStringAndNumeric_...` |
| E-3 ファイル内レコードレイアウト数 | ✅ | ✅ | ✅ | 0: `serializeMessage_emptyBody_emitsIdOnly`／複数: `serializeFile_fixedWith...`, `roundTrip_fixedFile_...` |
| E-4 コンテナ内セクション数（辺④の実体: 出力 YAML ファイル数） | n/a | ✅ | ❌ | 1: **`write_writesEachSectionAsYamlFileWithSerializedContent`**（辺④で唯一）。複数は `YamlFormatWriter#write` L74 が `container.getSections()` をループするため到達可能（§0.8-6） |

**軸F（✅ 0 ／ 🔺 1 ／ ❌ 1 ／ 対象外 1 ＝ 3）**

| 要素 | 判定 | 担保テストメソッド |
|---|---|---|
| F4-01 出力先不在 | 🔺 | `write_ioError_throwsUncheckedIOException`（正確には「親に通常ファイルが居座り親ディレクトリを作れない」ケース） |
| F4-02 `overwrite=false` 衝突 | **対象外（上位層で担保済み）** | `overwrite` を保持するのは `ConversionRequest` / `TestDataConverter` / `ConverterMojo` であり `YamlFormatWriter` は保持しない。上位層の `TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse`（L336）と `ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict`（L267）で担保済み（§0.8-5） |
| F4-03 書き込み権限なし | ❌ | — |
| （steering 外で担保済みの異常系） | ✅ | `serialize_unsupportedDataType_throws`（`DataType.DEFAULT` → `IllegalArgumentException`）、`serialize_rowShorterThanColumns_fillsMissingWithNull`（行と列の数の不一致 → null 補完）、`serialize_unbracketedGroupId_isUsedAsRawValue`（非整形 groupId の防御的経路） |

<a id="s4-3"></a>

### 4.3 辺④ 未担保一覧（#25 が埋める対象）

計上単位と「状態」の 3 分類は §1.3 の規則に従う。

| 軸 | 未担保要素 | 状態 | 件数 |
|---|---|---|---|
| A | A-07 `EXPECTED_FIXED`（🔺 `RoundTripTest#yaml_expectedFixed_isPreserved`）／A-08 `SETUP_VARIABLE`（🔺 `RoundTripTest#yaml_setupVariable_isPreserved`） | 要追加 | 2 |
| B | （なし） | — | 0 |
| C | C-02 sections 空・複数（writer 側は到達可能。§0.8-6）／C-12 FileDataBlock.records 空 | 要追加 | 2 |
| D | D4-01 `"100"`（記法アサートなしの 🔺）／D4-02 `"true"`／D4-06 `"007"`／D4-08 `"2026-08-07"`／D4-09 値側のコロン・ハイフン・`#` | 要追加 | 5 |
| E | E-4(複数) — `YamlFormatWriter#write` L74 が sections をループするため到達可能（§0.8-6） | 要追加 | 1 |
| F | F4-01 出力先不在（🔺 のみ）／F4-03 書き込み権限なし | 要追加 | 2 |
| F | F4-02 `overwrite=false` 衝突 — `YamlFormatWriter` は `overwrite` を保持しない。`TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse`（L336）／`ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict`（L267）で担保済み（§0.8-5） | 対象外（上位層で担保済み） | 1 |
| **合計** | | **要追加 12 ／ 到達不能 0 ／ 対象外 1** | **13（うち対象外 1）** |

**特に大きな空欄**: 軸D の 5 ケース（特に `"true"`・`"007"`・日付風文字列は、辺②で読み戻したときに
型が変わりうる往復リスクの中心）。軸C は 4 つの辺のなかでもっとも埋まっている。

---

<a id="s5"></a>

## 5. 全体サマリ

<a id="s5-1"></a>

### 5.1 未担保件数（辺 × 軸）

計上単位は §1.3 の規則に従う。🔺（弱い担保のみ）の要素も未担保として計上している。

**本節は #18 時点のスナップショットであり、4 辺を同じ基準で比べるためにその基準を保っている。**
辺①はその後 #19／#20／#21 で 33 件が担保済みになった（§1.3 の「#21 後の状態」列）。
辺①の最新の状態別内訳は **要追加 0 ／ 担保済み 33 ／ 到達不能 8 ／ 対象外 0** であり、
下の表の辺①列（要追加 38 ／ 到達不能 3）は #18 時点の値である。
辺③は #22 が軸D 8 件・軸F 3 件を、#23 が軸A 3 件・軸C 9 件・軸E 3 件を、#23 のレビュー対応が
軸A 3 件（A-12〜A-14。#18 時点で ✅ と誤判定されており本節の集計にも入っていなかった。§3.3）を
埋めたため最新は **要追加 0 ／ 担保済み 29 ／ 対象外 1**（§3.3）であり、
下の表の辺③列（要追加 26 ／ 対象外 1）は #18 時点の値である。
**下の 2 表の辺③の数字は #18 時点の誤判定を含む。** 辺③ 軸A は「3」ではなく **6**、辺③の合計は
「27」ではなく **30**、全体の合計は「107」ではなく **110**、状態別の「要追加 26」は **29** が正しい
（A-12〜A-14 を ✅ と誤判定していたため 3 件が計上から漏れていた。根拠は
[§3.1-3](#s3-1-3-sendsync)、影響範囲は §3.3）。
本節は #18 時点のスナップショットとして数字を保存する節であるため**表そのものは書き換えない**。
比較に使うときはこの補正を当てること。
辺②・辺④は #24／#25 が未着手のため #18 時点のまま。

| 軸 | 辺① | 辺② | 辺③ | 辺④ | 合計 |
|---|---|---|---|---|---|
| A データタイプ | 4 | 1 | 3 | 2 | 10 |
| B ブロック実装 | 0 | 0 | 0 | 0 | 0 |
| C 中間モデル全フィールド | 12 | 9 | 9 | 2 | 32 |
| D 値の表現 | 17 | 10 | 8 | 5 | 40 |
| E 多重度 | 3 | 2 | 3 | 1 | 9 |
| F 異常系 | 5 | 4 | 4 | 3 | 16 |
| **合計** | **41** | **26** | **27** | **13** | **107** |

**状態別の内訳**（#18 時点。§1.3 / §2.3 / §3.3 / §4.3 の #18 列の合計）:

| 状態 | 辺① | 辺② | 辺③ | 辺④ | 合計 |
|---|---|---|---|---|---|
| 要追加 | 38 | 23 | 26 | 12 | 99 |
| 到達不能 | 3 | 3 | 0 | 0 | 6 |
| 対象外（上位層で担保済み） | 0 | 0 | 1 | 1 | 2 |
| **合計** | **41** | **26** | **27** | **13** | **107** |

到達不能 6 件の内訳: 辺①・辺② それぞれの A-01 `DEFAULT`（§0.8-7）／C-02 sections 空（§0.8-6）／E-4 複数（§0.8-6）。
なお辺①は #20 の実測で C-11／C-13／C-16／C-17／C-20 の 5 件が到達不能へ移り、辺①の到達不能は 8 件になっている（§1.3）。
対象外 2 件の内訳: 辺③ F3-02 ／ 辺④ F4-02（いずれも `overwrite=false` 衝突。§0.8-5）。

**`RoundTripTest`（30 件）による 🔺 の追加は上の件数を変えない。** 🔺 は正式担保として数えないため、
§0.8-8 で 🔺 を付けた辺① A-04／A-07／A-09／C-06 省略／D1-14、辺③ A-07／A-09、辺④ A-07／A-08 は
いずれも「要追加」のまま残している（重複テストを書かないよう、追加時は §0.8-8 の表を参照すること）。

### 5.2 軸A の辺横断ビュー（`DataType` 14 種 × 4 辺）

**本節は §5.1 と違い現時点の状態を示す。** 辺①は #20 完了後の値（`XlsFormatReaderRealFileTest` による ✅ 化を反映。§1.2-2）。
**#21 は軸E・軸F だけを埋めるため、本節（軸A）の判定は #20 完了時点から変わっていない。**
**#22 も軸D・軸F だけを埋めるため、辺③列の判定は #18 時点から変わらなかったが、#23 が辺③の軸A 3 件
（A-01／A-07／A-09）を埋めたため辺③列を更新した**（2026-08-13。担保テストメソッドは §3.1-3）。
**さらに #23 のレビューで A-12／A-13／A-14 の辺③が 🔺 だったことが判明し、テスト 3 件を追加して ✅ にした**
（変異による実測は [§3.1-3](#s3-1-3-sendsync)）。
辺②・辺④は #24／#25 が未着手のため #18 時点から変わっていない。

| DataType | 辺① | 辺② | 辺③ | 辺④ |
|---|---|---|---|---|
| A-01 `DEFAULT` | ❌（到達不能） | ❌（到達不能） | ✅（#23 で ❌→✅） | ✅ |
| A-02 `SETUP_TABLE_DATA` | ✅ | ✅ | ✅ | ✅ |
| A-03 `EXPECTED_TABLE_DATA` | ✅ | ✅ | ✅ | ✅ |
| A-04 `EXPECTED_COMPLETED` | ✅（#20 で 🔺→✅） | ✅ | ✅ | ✅ |
| A-05 `LIST_MAP` | ✅ | ✅ | ✅ | ✅ |
| A-06 `SETUP_FIXED` | ✅ | ✅ | ✅ | ✅ |
| A-07 `EXPECTED_FIXED` | ✅（#20 で 🔺→✅） | ✅ | ✅（#23 で 🔺→✅） | 🔺 |
| A-08 `SETUP_VARIABLE` | ✅ | ✅ | ✅ | 🔺 |
| A-09 `EXPECTED_VARIABLE` | ✅（#20 で 🔺→✅） | ✅ | ✅（#23 で 🔺→✅） | ✅ |
| A-10 `MESSAGE` | ✅ | ✅ | ✅ | ✅ |
| A-11 `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | ✅ | ✅ | ✅ |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | ✅ | ✅（#23 レビューで 🔺→✅） | ✅ |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅ | ✅ | ✅（#23 レビューで 🔺→✅） | ✅ |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅ | ✅ | ✅（#23 レビューで 🔺→✅） | ✅ |
| **✅ 担保数** | 13/14 | 13/14 | **14/14** | 12/14 |
| **🔺 弱い担保** | 0 | 0 | **0** | 2 |
| **❌ 未担保** | 1 | 1 | **0** | 0 |

`EXPECTED_FIXED`（A-07）は #20 で辺①が、#23 で辺③が ✅ になり、残る 🔺 は辺④の 1 辺だけである
（`RoundTripTest#yaml_expectedFixed_isPreserved` 経由。§0.8-8）。
`SETUP_VARIABLE`（A-08）の辺④も 🔺 のままで #25 の対象。`EXPECTED_VARIABLE`（A-09）の辺③は #23 で ✅ になった。
`DEFAULT`（A-01）は辺①・辺②で到達不能、辺③は #23 で ✅（`writesDefaultDataTypeMarker`）、
辺④は `serialize_unsupportedDataType_throws` で ✅ だが、辺③は書き出し・辺④は例外という**非対称**である
（`issues.md` **XLS-20**。修正はしていない）。
送信同期 3 種（A-12〜A-14）の辺③は #18 以来 ✅ と書かれていたが実際は 🔺 で、#23 のレビューで
3 メソッドを追加して ✅ にした（[§3.1-3](#s3-1-3-sendsync)）。

**辺③は軸A 14 種すべてが ✅ である**（✅ 14 ／ 🔺 0 ／ ❌ 0）。**この判定は #23 の当初版では成り立って
いなかった**（A-12〜A-14 が 🔺 で ✅ 11 ／ 🔺 3。#23 レビュー対応でテストを追加して初めて成立した）。
上表の「✅ 担保数 14/14 ／ 🔺 0」は 2026-08-13 の #23 レビュー対応後の値である。

<a id="s5-3"></a>

### 5.3 コーディネータに判断を仰ぎたい点

#### 解決済み（steering 更新 commit `66eb28f` で確定）

| # | 論点 | 確定内容 |
|---|---|---|
| 1 | 辺①・辺② の `DataType.DEFAULT` | **到達不能**。辺①は `TestCoreReaderAdapter` L362、辺②は `YamlFormatReader` の分岐に `DEFAULT` を返す経路がないこと（§0.8-7）。#20/#24 では理由付きで空欄に残す |
| 2 | E-4「コンテナ内セクション数 複数」／C-02「sections 空・複数」 | 辺①・辺②は **到達不能**（`read` が `Collections.singletonList(section)` を返す 1 リソース単位 API）。辺③・辺④は writer が `container.getSections()` をループするため **要追加**（§0.8-6） |
| 3 | 辺③／辺④ の `overwrite=false` 衝突（F3-02 / F4-02） | **対象外（衝突検査は上位層）**。`overwrite` を保持するのは `ConversionRequest` / `TestDataConverter` / `ConverterMojo` で writer は保持しない。既存テスト `TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse`（L336）と `ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict`（L267）が `TestDataConverter#checkOverwrite`（L90-99）を通す。ただし両者とも XLS→YAML であり、**辺④（`.yaml` 出力）は担保されるが辺③（`.xlsx` 出力）は未担保**である（§0.8-5 の訂正） |
| 4 | 既存の往復テスト（`RoundTripTest` 30 件、`XlsFormatWriterTest#roundTrips*` 8 件、`YamlFormatWriterTest#roundTrip_*` 6 件）の扱い | **🔺弱い担保として計上するが正式担保としては数えず、直接テストの追加対象からも外さない**（steering Rules フェーズ2）。§0.8-8 に `RoundTripTest` 30 件の対応表を置き、未担保一覧に 🔺 の注記を併記した |

#### 未解決（コーディネータの確認が要る）

1. **軸C の「省略」定義**: steering #20 の Steps は `identifier` と `fileType` も「値あり」「省略」双方を通すとしているが、
   実定義には省略表現がない（§0.4）。実定義を正として `fileType` は FIXED/VARIABLE の 2 値、
   `identifier` は必須スカラーとして扱ってよいか（本棚卸しは実定義を正として扱っている）。
2. **steering の完了条件と「到達不能」の整合**: steering #24 の Completion criteria は
   「辺②について軸A の 14 種…が埋まっている」、#23 は「辺③について軸A の 14 種…がすべてアサートされている」と読める。
   本棚卸しでは辺② A-01 `DEFAULT` を到達不能と判定した（§0.8-7。辺①については steering #20 が既に到達不能と明記済み）。
   #24 の完了条件を「13 種＋ `DEFAULT` は到達不能として理由付きで空欄」に読み替えてよいか。
   同じく steering #21 は辺① E-4 のみを到達不能としているが、本棚卸しでは C-02「sections 空」も同じ根拠で到達不能と判定した。
3. **辺③ A-01 `DEFAULT` の扱い**: ~~writer 側は `XlsFormatWriter` L400 がマーカー文字列を
   `getDataType().getName()` から組み立てるだけなので `DEFAULT` ブロックも書けてしまう（＝到達可能）。
   辺④の `serialize_unsupportedDataType_throws` は `IllegalArgumentException` を投げる挙動を固定しているが、
   辺③に同等のガードはない。#23 で現状挙動を記録して固定する対象としてよいか
   （挙動が仕様として不適切なら `issues.md` 行き）。~~
   **解決済み（#23・2026-08-13）**: steering #23 の Steps が「現状の挙動をまず実行して記録してから固定する。
   辺③④の非対称を `issues.md` に課題として記録する（修正しない）」と指示しているため、そのとおり実施した。
   固定した挙動は `XlsFormatWriterModelTest#writesDefaultDataTypeMarker`（識別セル `DEFAULT=T` が書かれる）、
   課題は `issues.md` **XLS-20**。あわせて「辺③が書いたブロックは辺①で読み戻すと黙って消える」ことも
   実測して `#dropsDefaultDataTypeBlockWhenReadBack` で固定した。
