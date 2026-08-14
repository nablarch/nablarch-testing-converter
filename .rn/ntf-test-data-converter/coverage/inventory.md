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
>   §1.1 は「`XlsFormatReaderTest` 33 件」を対象とした #18 時点の事実として**そのまま残す**。
> - **§1.3** — 未担保一覧を #19／#20／#21 の実測結果に合わせて更新した。分類を変更した行には
>   根拠（`coverage/issues.md` の課題 ID）を併記してある。
>
> **辺③（#22 辺③ 軸D・軸F。2026-08-13）**
>
> - **§3.1-2（新設）** — #22 が追加したテストクラス（`XlsFormatWriterCellTypeTest` /
>   `XlsFormatWriterInvalidOutputTest`）の担保を軸要素別に記す。
>   §3.1 は「`XlsFormatWriterTest` 40 件」を対象とした #18 時点の事実として**そのまま残す**。
> - **§3.3** — 辺③ 未担保一覧を #22 の実測結果に合わせて更新した。
>
> **辺③（#23 辺③ 軸A・B・C・E の欠け補充。2026-08-13）**
>
> - **§3.1-3（新設）** — #23 が追加したテストクラス（`XlsFormatWriterModelTest`）の担保を軸要素別に記す。
>   §3.1（`XlsFormatWriterTest` 40 件）と §3.1-2（#22 の 2 クラス）は**そのまま残す**。
>   `XlsFormatWriter` の JaCoCo 実測（未到達 3 箇所）も本節末尾に置いた。
> - **§3.3** — 辺③ 未担保一覧に「#23 後の状態」列を足し、**要追加 0 ／ 担保済み 29 ／ 対象外 1**
>   （総計 30）に更新した。#23 のレビュー対応で送信同期 3 件（A-12〜A-14）を足した分を含む。
> - **§0.8-6 / §0.8-7 / §5.2 / §5.3** — 辺③の C-02「sections 空」と A-01 `DEFAULT` が
>   #23 で担保済みになったことを追記した（§5.3 の未解決 3 は解決済みへ移した）。
>
> **辺③（#23 レビュー ラウンド2 対応。2026-08-13。テストの担保内容は変えていない）**
>
> - **§0.8-4** — `cell` / `line` の定義位置が `XlsFixture` へ移ったことを書き足した
>   （挙動が `getStringCellValue()` 固定である点は #18 から変わっていない）。
> - **§3.3** — **辺③では軸B が軸A から独立していない**ことを開示した（判定 ✅ 4/4 と件数 0 は変えない）。
>
> **辺②（#24 辺② 軸D・軸F・軸A/B/C/E。2026-08-14）**
>
> - **§2.1-2（新設）** — #24 が追加したテストクラス（`YamlFormatReaderScalarTest` /
>   `YamlFormatReaderInvalidInputTest` / `YamlFormatReaderRealFileTest`。いずれも実 `.yaml` を入力とする）の
>   担保を軸要素別に記す。
>   §2.1 は「`YamlFormatReaderTest` 20 件」を対象とした #18 時点の事実として**そのまま残す**。
> - **§2.3** — 辺② 未担保一覧に「#24 後の状態」列を足した。
> - **§0.5** — 軸D 辺②を 10 ケース → **12 ケース**（D2-01〜D2-12。ユーザー確定・2026-08-14）へ改めた。
> - **§0.8-3 / §5.2** — 辺②の軸D 件数と、軸A の辺②列（実 `.yaml` 経路でも ✅ になったこと）を追記した。
>
> **上記以外（§4・§5.1）は #18 時点のままである**（§5.1 の未担保件数も §1.3／§2.3／§3.3 の更新を
> 反映していない。4 辺を同じ基準で比べるため、あえて #18 基準を保っている）。
> **§5.2 だけは §1.2-2 の #20 実績と §2.1-2 の #24 実績を反映した現時点ビューである。**
>
> **§0（前提の実測）は原則として #18 時点のスナップショットである。** その後の変化は
> 各項の中に日付つきで追記してある（§0.8-4 の `getCellType()` 件数など）。

### 判定基準

- **軸A**: 「その `DataType` のブロックが生成されることをアサートしている」ことを担保とし、
  `getDataType()` の直接アサートとは区別する。`XlsFormatReaderTest` で `getDataType()` を参照するのは
  一部のテストだけだが、他のテストもデータタイプ名を含むマーカー行を入力に与え、
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
  printf "%-30s %s @Test\n" "$(basename $f)" "$(grep -c '@Test' $f)"
done
```

```
XlsFormatReaderTest.java       33 @Test
YamlFormatReaderTest.java      20 @Test
XlsFormatWriterTest.java       40 @Test
YamlFormatWriterTest.java      33 @Test
RoundTripTest.java             30 @Test
```

**上のブロックは現在（2026-08-13）の実測であり、上のコマンドをそのまま実行すれば再現する。**
`@Test` の数は 5 クラスとも #18 から変わっていない。

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

| # | 定数名 | `getType()` | `getName()`（記法名） |
|---|---|---|---|
| A-01 | `DEFAULT` | 0 | `DEFAULT` |
| A-02 | `SETUP_TABLE_DATA` | 1 | `SETUP_TABLE` |
| A-03 | `EXPECTED_TABLE_DATA` | 2 | `EXPECTED_TABLE` |
| A-04 | `EXPECTED_COMPLETED` | 4 | `EXPECTED_COMPLETE_TABLE` |
| A-05 | `LIST_MAP` | 3 | `LIST_MAP` |
| A-06 | `SETUP_FIXED` | 5 | `SETUP_FIXED` |
| A-07 | `EXPECTED_FIXED` | 6 | `EXPECTED_FIXED` |
| A-08 | `SETUP_VARIABLE` | 7 | `SETUP_VARIABLE` |
| A-09 | `EXPECTED_VARIABLE` | 8 | `EXPECTED_VARIABLE` |
| A-10 | `MESSAGE` | 9 | `MESSAGE` |
| A-11 | `EXPECTED_REQUEST_HEADER_MESSAGES` | 10 | 同名 |
| A-12 | `EXPECTED_REQUEST_BODY_MESSAGES` | 11 | 同名 |
| A-13 | `RESPONSE_HEADER_MESSAGES` | 12 | 同名 |
| A-14 | `RESPONSE_BODY_MESSAGES` | 13 | 同名 |

**突き合わせ結果: 14 種（`DEFAULT` ＋ 13）で steering の記述と一致。差異なし。**

### 0.3 軸B: `TestDataBlock` sealed 階層

`TestDataBlock`（abstract sealed）が permit するのは `ColumnRowDataBlock` / `FileDataBlock` / `MessageDataBlock`。
`ColumnRowDataBlock`（abstract sealed）がさらに `TableDataBlock` / `ListMapBlock` を permit する。
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
| C-06 | `TestDataBlock` | `groupId` | String | **省略可** | `""`（TestDataBlock の Javadoc「省略時は空文字」） |
| C-07 | `TestDataBlock` | `identifier` | String | 必須 | — |
| C-08 | `ColumnRowDataBlock` | `columnNames` | List | 空許容 | 空リスト |
| C-09 | `ColumnRowDataBlock` | `rows` | List<List> | 空許容 | 空リスト |
| C-10 | `FileDataBlock` | `fileType` | FileType | 必須（2値） | FIXED / VARIABLE（FileDataBlock の Javadoc） |
| C-11 | `FileDataBlock` | `directives` | Map | 空許容 | 空 Map |
| C-12 | `FileDataBlock` | `records` | List | 空許容 | 空リスト |
| C-13 | `MessageDataBlock` | `directives` | Map | 空許容 | 空 Map |
| C-14 | `MessageDataBlock` | `fwHeaderFields` | Map | 空許容 | 空 Map（MessageDataBlock の Javadoc「FW ヘッダを読まない経路では空 Map」） |
| C-15 | `MessageDataBlock` | `records` | List | 空許容 | 空リスト |
| C-16 | `RecordLayout` | `recordType` | String | **省略可** | `null`（RecordLayout の Javadoc「省略時は null」） |
| C-17 | `RecordLayout` | `fields` | List | 空許容 | 空リスト |
| C-18 | `RecordLayout` | `rows` | List<List> | 空許容 | 空リスト |
| C-19 | `FieldDef` | `name` | String | 必須 | — |
| C-20 | `FieldDef` | `type` | String | **省略可** | `null`（FieldDef の Javadoc「省略時は null」） |
| C-21 | `FieldDef` | `length` | String | **省略可** | `null`（FieldDef の Javadoc「省略時は null」） |

内訳: 必須スカラー 6 件（C-01, C-03, C-05, C-07, C-10, C-19）／
**省略可能フィールド 4 件**（C-06, C-16, C-20, C-21 — Javadoc に「省略時は…」と明記）／
空許容コレクション 11 件（C-02, C-04, C-08, C-09, C-11, C-12, C-13, C-14, C-15, C-17, C-18）。

**steering との差異（コーディネータ判断を仰ぐ点）**: steering #20 の Steps は
「`groupId` / `identifier` / `fileType` / `directives` / `fwHeaderFields` / `recordType` / `FieldDef.type` / `FieldDef.length` は
『値あり』『省略』の双方を通す」としているが、実定義上 `identifier`（C-07）と `fileType`（C-10）には
「省略」の表現が存在しない（`identifier` は必須スカラー、`fileType` は FIXED/VARIABLE の 2 値）。
本棚卸しでは実定義を正とし、`fileType` は「FIXED / VARIABLE 双方」、`identifier` は「値あり 1 通り」として扱う。
`directives` / `fwHeaderFields` は「非空 / 空 Map」の双方として扱う。

### 0.5 軸D 値の表現 — 要素（辺ごとに定義が異なる。steering #19/#22/#24/#25 の記述を要素化）

- **辺① セル種別 8 ケース**: D1-01 文字列／D1-05 先頭ゼロ文字列／D1-12 セル不在／D1-13 空文字／
  D1-14 前後空白／D1-15 改行／D1-16 リテラル `null`／D1-17 表示形式 `@` の数値セル

  対象は **NTF が実行できるテストデータ**に限る。Excel 側の条件は `PoiXlsReader` のクラス Javadoc が
  定める「全セルが文字列書式」であり、これを外れるセル種別（表示形式を持たない数値・日付書式・
  時刻書式・日時書式・数式・真偽値・エラー値）は担保対象でも記録対象でもない
  （ユーザー確定・2026-08-13）。**タグは振り直さないため番号に欠番が出る。**
  D1-17 だけは表示形式 `@` ＝ 文字列書式で但し書きを満たしながらセル種別が数値であり、
  前提の内側で値が変わる唯一のケースのため残す。
- **辺② YAML スカラー 12 ケース（2026-08-14・ユーザー確定。#18 時点の 10 ケース定義から改めた）**:
  D2-01 引用符なし文字列（`abc`）／D2-02 引用符あり（`"abc"`・`'abc'`）／D2-03 引用符付き数値（`"123"`）／
  D2-04 引用符付き末尾ゼロ小数（`"1.50"`）／D2-05 真偽値に見える文字列（`"true"`・`TRUE`・`yes`）／
  D2-06 NULL（`null`・値なし）／D2-07 NULL に見える文字列（`"null"`・`~`・`NULL`）／
  D2-08 日付・日時風文字列／D2-09 複数行（`|`・`>`）／D2-10 先頭ゼロ・非 JSON 数値記法（`007`・`0x1F`）／
  D2-11 空文字・前後空白（`""`・`"  pad  "`）／D2-12 特殊文字を含む文字列（`"a: b"`・`"a #b"`）

  **#18 時点の定義との差**: D2-06 が `null`・`~`・値なし を 1 ケースにまとめていたのに対し、
  実測では `~` だけが文字列になる（`coverage/issues.md` **YML-01**）ため D2-06（NULL になるもの）と
  D2-07（文字列になるもの）へ分けた。あわせて D2-11・D2-12 を足して 12 ケースになった。

  対象は **NTF が実行できるテストデータ**に限る。YAML 側の条件は本体スキーマ
  （yaml jar 内 `nablarch/test/ntf-testdata-yaml-schema.json`）であり、値の型を `["string","null"]` に
  限るため、引用符なしの `true` / `123` / `1.50` / `.inf` / `.nan` は担保対象でも記録対象でもない
  （`coverage/issues.md`「対象としない入力（辺②）」。ユーザー確定・2026-08-14）。
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
   事実: `XlsFormatReaderTest` は内部クラス `FakeTestDataReader` に `List<List<String>>` の
   canned 行を与えて `TestCoreReaderAdapter` を駆動する。実セル → 文字列行の区間（`PoiXlsReader`）は
   一度も実行されない。
   判断: **軸D 辺①（8 ケース）は 33 件からは全て未担保**とする。
2. **辺③の往復テスト 8 件（`roundTrips*`）は実 `.xlsx` を経由して `XlsFormatReader` を駆動している。**
   事実: `XlsFormatWriterTest#roundTrip` は `new XlsFormatWriter().write(...)` で実ファイルを書き、
   `new XlsFormatReader()`（本番配線＝`PoiXlsReader`）で読み戻す。
   判断: steering #19 の「実 `.xlsx` を入力として `XlsFormatReader` を駆動するテストが存在し、
   `FakeTestDataReader` を経由していない」は**既に部分的に満たされている**（文字列セル・空セル・
   リテラル `null` の 3 ケース相当が通る）。#19 はこれを起点にできる。
3. **辺②の既存 20 件は 1 件も実 YAML テキストを通っていない。**
   事実: `YamlFormatReaderTest#reader` は `YamlTestCoreAdapter#loadRawMap` を in-memory
   `LinkedHashMap` に差し替える。YAML パーサ（SnakeYAML Engine）は通らない。一方、辺④の往復テスト 6 件
   （`roundTrip_*`）は `writer.write(...)` で実 YAML ファイルを書き `new YamlFormatReader()` で読み戻す。
   判断: **軸D 辺②（#18 時点の定義で 10 ケース）は 20 件からは全て未担保**とし、往復 6 件が通す分は 🔺 で計上する。
   **#24 で解消した（2026-08-14 追記）**: 実 `.yaml` を入力とする 3 クラスを追加し、
   現在の定義（12 ケース。§0.5）をすべて担保した（§2.1-2）。
4. **`getCellType()` をアサートしているテストは #18 時点では src/test 全体でゼロだった。**
   事実（#18 時点）: `grep -rn "getCellType" src/test/` → 0 件。`XlsFormatWriterTest` のセル読み出しヘルパ
   `cell` ／ `line` は `getStringCellValue()` のみを使う。
   **この 2 つは #23 のレビュー対応で `XlsFixture` へ移した。**
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
   この行数をテスト件数として並べたもので、誤りだった。
   上記の「ゼロ」は #18 時点のスナップショットであり、現在形として読んではならない。
5. **`overwrite` フラグを writer は持たない。**
   事実: `grep -rln "overwrite" src/main/java` の結果、`overwrite` を保持するのは `ConversionRequest` /
   `TestDataConverter` / `ConverterMojo` の 3 クラスのみで、`XlsFormatWriter` / `YamlFormatWriter` は保持しない。
   `overwrite=false` 衝突を検査するのは `TestDataConverter#checkOverwrite` であり、
   上位層の既存テスト `TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse` と
   `ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict` が通している
   （いずれも出力先に既存ファイルを置いた状態で変換し、`ConverterException` ／ `MojoExecutionException` を
   アサートしている）。
   判断: 軸F の F3-02 / F4-02 は writer 単体では再現できないため、**辺③／辺④の対象外**
   として分類する（steering #22/#25 の Steps と一致）。

   **担保範囲の訂正（2026-08-13・レビュー指摘による訂正。2026-08-13 の第 3 ラウンドで根拠を再訂正）。**
   当初この項と各節は「上位層の既存テストで担保済み」と書いていたが、
   **担保されているのは `.yaml` を出力側とする衝突だけ**である。
   事実:
   - `checkOverwrite` は `target.outputPaths(container, outputBase)` を多態で呼び分ける。
     引用した 2 件の既存テストは**どちらも XLS→YAML** であり（`TestDataConverter.convert(DataFormat.XLS,
     DataFormat.YAML, ...)` ／ Mojo の `from=xls, to=yaml`。衝突させているのは `BookA/data.yaml`）、
     実行されるのは `YamlFormatHandler#outputPaths` である。
   - **`XlsFormatHandler#outputPaths` 自体は `overwrite=false` 下で実行されている。**
     4 引数入口 `TestDataConverter.convert(DataFormat, DataFormat, Path, Path)` は
     `Builder#overwrite` を呼ばずにリクエストを組み、`ConversionRequest` の `overwrite` 既定値は
     `false` なので、`checkOverwrite` は早期 return せず
     `outputPaths` を呼ぶ。実際に通しているのは XLS を出力側とする
     `TestDataConverterTest#convertsYamlToXls`／`#convertsXlsToXls`／
     `#convertsYamlWithFilesToXls` の 3 件である。
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
   事実: `XlsFormatReader#read` は `Collections.singletonList(section)` を返し、
   `YamlFormatReader#read` も同じく `Collections.singletonList(section)` を返す。
   一方 writer 側は `XlsFormatWriter#build` ／ `YamlFormatWriter#write` が
   `container.getSections()` をループするため、辺③／辺④では複数・0 とも到達可能である。
   判断: E-4「コンテナ内セクション数 複数」と C-02「sections 空」は**辺①・辺②では到達不能**、
   **辺③・辺④では要追加**として分類する。
   **辺③の C-02「sections 空」は #23 で担保済みになった（2026-08-13 追記）**:
   `XlsFormatWriterModelTest#writesWorkbookWithoutSheetsWhenContainerHasNoSections` が
   シートを 1 枚も持たないブックが書き出されることを実測して固定した（`issues.md` **XLS-23**）。
7. **`DataType.DEFAULT` はリーダ 2 経路のいずれでも生成されない。**
   事実: 辺① — `TestCoreReaderAdapter` が `type == DataType.DEFAULT` のブロックを `continue` でスキップする。
   辺② — `YamlFormatReader#addBlocksForSection` と `fileDataType` ／
   `addMessageBlocks` は `DEFAULT` 以外の 13 種のみを分岐に持ち、`DEFAULT` を返す経路がない。
   判断: A-01 `DEFAULT` は**辺①・辺②で到達不能**。writer 側（辺③）は
   `XlsFormatWriter#marker` がマーカー文字列を `block.getDataType().getName()` から組み立てるだけで
   タイプを絞らないため到達可能であり、**辺③は要追加**（辺④は `serialize_unsupportedDataType_throws` で担保済み）。
   **辺③は #23 で担保済みになった（2026-08-13 追記）**: `XlsFormatWriterModelTest#writesDefaultDataTypeMarker` が
   識別セル `DEFAULT=T` が書き出されることを実測して固定した。あわせて
   `#dropsDefaultDataTypeBlockWhenReadBack` が「辺③で書けたブロックが辺①で読み戻すと消える」ことを実検査する。
   辺③（書ける）と辺④（例外）の非対称は `issues.md` **XLS-20** に記録した（修正はしない）。

### 0.8-8 `RoundTripTest`（30 件）の扱い

`RoundTripTest`（30 `@Test`）は 4 辺いずれの
担当クラスのテストでもないため §0.1 の 126 件には含まれないが、`new XlsFormatWriter().write(...)` で実
`.xlsx` を書き `new XlsFormatReader().read(...)`（本番配線）で読み戻す XLS 経路 13 件と、
`new YamlFormatWriter().write(...)` → `new YamlFormatReader().read(...)` の YAML 経路 14 件、
両経路を 1 メソッドで通す 3 件からなり、**4 辺すべてを実ファイル経由で駆動している**
（往復ヘルパ: `xlsRoundTrip` ／ `yamlRoundTrip`）。

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

※ 軸E は全 30 件が「1 セクション・1 ブロック」固定（`xlsRoundTrip` ／ `yamlRoundTrip` が
`sections.size()==1` と `blocks.size()==1` をアサートする）ため、E-1(1)／E-4(1) 以外の多重度は通さない。
軸F は 30 件とも正常系のため通さない。

**この表によって従来 ❌ だった要素に新たに 🔺 が付くもの**:

| 辺 | 新たに 🔺 になる要素 | 根拠テスト |
|---|---|---|
| 辺① | A-04 `EXPECTED_COMPLETED` | `xls_expectedCompleteTable_isPreserved` |
| 辺① | A-07 `EXPECTED_FIXED` | `xls_expectedFixed_isPreserved` |
| 辺① | A-09 `EXPECTED_VARIABLE` | `xls_expectedVariable_isPreserved` |
| 辺① | C-06 `groupId` 省略(`""`) | `xls_setupTable_isPreserved`, `xls_expectedCompleteTable_isPreserved` ほか 5 件（`assertTableBlock` ／ `assertFileBlock` ／ `assertMessageBlock` が `getGroupId()` を `""` と突き合わせる XLS 経路 7 件） |
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

### 1.2-2 #19／#20／#21 が追加したテストクラスの担保（2026-08-12 追記）

**本節は #20 で新設し、#21 で追記した。** §1.1 は「`XlsFormatReaderTest` 33 件」を対象とした
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
| A-01 `DEFAULT` | —（到達不能。§0.8-7） | — |
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

**軸C（#18 時点から状態が変わったものだけ。根拠は `coverage/issues.md` の課題 ID）**

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

**軸D** — §0.5 の 8 ケース（D1-01／D1-05／D1-12〜D1-17）すべてを #19 の
`XlsFormatReaderCellTypeTest` が実 `.xlsx` で担保した（`issues.md` XLS-01／XLS-04／XLS-05）。
#18 で「全滅」としていた空欄は埋まっている。

**軸E（#21。0 件は実 `.xlsx` 経路で新規担保。1 件／複数件の既存担保も併記する）**

| 要素 | 0 件 | 1 件 | 複数件 | 担保テストメソッド |
|---|---|---|---|---|
| E-1 セクション内ブロック数 | ✅ | ✅ | ✅ | 0: `XlsFormatReaderRealFileTest#readsEmptyBlockListFromRealSheetWithoutMarkers`（#20）／1: 同クラスの単一ブロックのテスト多数／複数: `#readsFourBlockImplementationsFromOneRealSheet`（4 件）・`#readsAllFourSendSyncMessageTypesFromRealBook`（4 件） |
| E-2 ブロック内行数 | ✅（**#21**） | ✅ | ✅ | 0: `#readsEmptyRowsFromTableWithoutDataRowsInRealBook`／`#readsEmptyRowsFromListMapWithoutDataRowsInRealBook`／1: `#readsExpectedTableBlockWithGroupIdFromRealBook`（1 行）／複数: `#readsSetupTableBlockFromRealBook`（2 行） |
| E-3 ファイル内レコードレイアウト数 | ✅（**#21**） | ✅ | ✅（**#21** で実 `.xlsx` 経路も） | 0: `#readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook`（ファイル系）・`#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook`（メッセージ系）／1: `#readsSetupFixedFileBlockFromRealBook`・`#readsMessageBlockFromRealBook`（いずれも `records.size()==1` をアサート）／複数: **`#readsMultipleRecordLayoutsFromOneFixedFileInRealBook`**（断片 2 件。Fake リーダ経路には `XlsFormatReaderTest#readRestoresMultipleRecordLayoutsInFixedFile` がある） |
| E-4 コンテナ内セクション数 | n/a | ✅ | ❌（**到達不能**） | 1: `#readsContainerAndSectionNamesFromRealBookAndSheetNames`。複数は `XlsFormatReader#read` が `Collections.singletonList(section)` を返すため構造上到達不能（§0.8-6） |

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
  §3.1-2／§3.3 では `対象外（衝突検査は上位層）` へ改めた。詳細は §0.8-5 の訂正欄。

**本表は #21 の実測結果に合わせて更新した（2026-08-12）。** #18 時点は「要追加 38 ／ 到達不能 3」、
#20 完了時点は「要追加 11 ／ 担保済み 22 ／ 到達不能 8」であった。#21 が残る 11 件（C 4 件・E 2 件・F 5 件）を
埋めたため、**辺①の未担保は 0 件**になった。分類を変更した行には根拠（`coverage/issues.md` の
課題 ID）を併記してある。#18 時点の分類は各行の「#18」列に残した。

| 軸 | 未担保要素 | #18 の状態 | #21 後の状態 | 件数 |
|---|---|---|---|---|
| A | A-04 `EXPECTED_COMPLETED`／A-07 `EXPECTED_FIXED`／A-09 `EXPECTED_VARIABLE` | 要追加 | **担保済み（#20）** — 順に `XlsFormatReaderRealFileTest#readsExpectedCompletedTableBlockFromRealBook`／`#readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook`／`#readsExpectedVariableFileBlockWithGroupIdFromRealBook` | 3 |
| A | A-01 `DEFAULT` — `TestCoreReaderAdapter` が DEFAULT ブロックをスキップするためリーダ経路で生成されない（§0.8-7） | 到達不能 | 到達不能（変更なし） | 1 |
| B | （なし） | — | — | — | 0 |
| C | C-06 groupId 省略(`""`) | 要追加 | **担保済み（#20）** — `#readsSetupTableBlockFromRealBook` ほか 2 件が `""` を直接アサート | 1 |
| C | C-08 columnNames 空 | 要追加 | **担保済み（#20 修正ラウンド）** — `#readsEmptyColumnNamesFromMarkerOnlyTableInRealBook`／`#readsEmptyColumnNamesFromMarkerOnlyListMapInRealBook`。マーカー列だけのブロックで到達する（`issues.md` **XLS-08**）。**#20 の当初分類では「軸E の 0 件と重なる」として #21 送りにしていたが誤り**（軸E の 4 観点 E-1〜E-4 に「列名 0 件」に対応する要素は無い）。#18 §1.3 は本要素を「要追加」に列挙しただけで、どのタスクが埋めるかは指定していない | 1 |
| C | C-09 rows 空／C-12 FileDataBlock.records 空／C-15 MessageDataBlock.records 空／C-18 RecordLayout.rows 空 | 要追加 | **担保済み（#21）** — `XlsFormatReaderRealFileTest#readsEmptyRowsFromTableWithoutDataRowsInRealBook`／`#readsEmptyRowsFromListMapWithoutDataRowsInRealBook`（C-09 は 2 経路）／`#readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook`（C-12）／`#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook`（C-15）／`#readsEmptyRowsFromRecordLayoutWithoutValueRowsInRealBook`（C-18）。いずれも例外にならず空コレクションになることを実測して固定した | 4 |
| C | C-11 FileDataBlock.directives 空／C-13 MessageDataBlock.directives 空 | 要追加 | **到達不能** — 本体 `DataFile` のコンストラクタが `file-type` を必ず注入する（`issues.md` **XLS-07**）。根拠は `#readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook`／`#readsAllFourSendSyncMessageTypesFromRealBook` がテストで示す | 2 |
| C | C-16 recordType 省略(`null`) | 要追加 | **到達不能** — 実 `.xlsx` 経路では空セルが `""` として読まれる（`issues.md` **XLS-06**）。根拠は `#readsOmittedRecordTypeAsEmptyStringFromRealBook` | 1 |
| C | C-17 RecordLayout.fields 空 | 要追加 | **到達不能** — 名前行が 2 列未満だと本体 `DataFileParser` が失敗する（`issues.md`「到達不能」表）。根拠は **#21 が追加した** `XlsFormatReaderInvalidInputTest#failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook` と `#failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook` がテストで示す。**#20 の当初分類では #21 送りにしていたが誤り**（軸E の 0 件ではない）。#18 §1.3 は本要素を「要追加」に列挙しただけで、どのタスクが埋めるかは指定していない | 1 |
| C | C-20 FieldDef.type 省略(`null`) | 要追加 | **到達不能** — 型の欠落を本体パーサが 2 通りの機構で弾く（`issues.md`「到達不能」表）。根拠は **#21 が追加した** `XlsFormatReaderInvalidInputTest#failsWhenTypeRowIsShorterThanNameRowInRealBook`（機構①）／`#failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook`（機構②）がテストで示す | 1 |
| C | C-02 sections 空 — `XlsFormatReader#read` が `Collections.singletonList(section)` を返すため sections は常に 1 件（§0.8-6） | 到達不能 | 到達不能（変更なし） | 1 |
| D | §0.5 の 8 ケース（D1-01／D1-05／D1-12〜D1-17） | 要追加 | **担保済み（#19）** — `XlsFormatReaderCellTypeTest` 10 件（8 ケース＋空白セル＋行途中の不在セル。`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderCellTypeTest.java` → **10**）。Excel 保存物との突き合わせは `XlsReferenceFixtureTest` 2 件（`issues.md` XLS-01／XLS-04／XLS-05） | 8 |
| E | E-2(0 件)／E-3(0 件) | 要追加 | **担保済み（#21）** — E-2(0) は C-09 の 2 件と同じ入力、E-3(0) は C-12／C-15 と同じ入力（上記 C 行のテストメソッド） | 2 |
| E | E-4(複数) — `XlsFormatReader#read` が 1 シート単位 API（§0.8-6） | 到達不能 | 到達不能（変更なし） | 1 |
| F | F1-01 シート不在／F1-02 ブック破損／F1-03 未知データタイプ名（🔺 `readIgnoresDataTypePrefixedLineWithoutMarker` のみ）／F1-04 マーカーカラム欠落／F1-06 行列数不一致 | 要追加 | **担保済み（#21）** — `XlsFormatReaderInvalidInputTest` 16 件（内訳: 本 5 ケースが 9 件、F1-05 の実 `.xlsx` 担保が 2 件、C-17／C-20 到達不能の根拠が 4 件、XLS-15 の根拠が 1 件。§1.2-2 の軸F 表に要素別の担保テストメソッドを記載）。継続する異常系で見つけた課題は `issues.md` **XLS-10〜XLS-15** | 5 |
| **合計** | | **要追加 29 ／ 到達不能 3** | **要追加 0 ／ 担保済み 24 ／ 到達不能 8 ／ 対象外 0** | **32** |

検算: 軸D を 17 ケース → 8 ケースへ絞り込んだ（2026-08-13・ユーザー確定）ため、
本表の総計は 41 → **32**、要追加は 38 → **29**、担保済みは 33 → **24** へ 9 ずつ減った。
右列の内訳 24 ＋ 8 ＝ 32 が総計と一致する。

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

**#18 時点の「特に大きな空欄」**（軸D 全滅。当時の定義では 17 ケース、現在の定義では 8 ケース）は
#19 で解消し、#20 後に残っていた最大の空欄
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

**16 行目の軸F 欄を訂正した（2026-08-14・#24 のレビュー指摘による訂正）。** 本表は #18 時点の事実として
書き換えない方針だが、当該欄の「送信系必須の `group_id`」という記述は**一次情報（本体スキーマ）に反していた**
ため訂正した。`$defs.group_message_data.required` も `$defs.expected_request_message_data.required` も
`["id","records"]` だけで `group_id` を要求していない（`issues.md` **YML-02** に再現コマンドつきで記録）。
これに伴い F2-04（必須構造の欠落）の #18 時点の担保は **🔺 も無し**（未担保）になる。
判定の増減ではなく誤記の訂正であるため、他の行と §2.3 の件数は変わらない。

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
| 16 | `readSendSync_entryWithoutGroupId_isDropped` | A-14 | B-4 | C-07 | — | E-1(1) | —（軸F の要素ではない。**スキーマは送信系に `group_id` を要求していない**ため「必須構造の欠落」ではなく、仕様内の入力が黙って drop される現状挙動の固定である。`issues.md` **YML-02**） |
| 17 | `read_mixedSections_keepsDescriptionOrderAndIgnoresUnknownKeys` | A-02, A-10 | B-1, B-4 | C-04 | — | E-1(複数=2) | **F2-03** ✅（未知キー無視） |
| 18 | `read_namesContainerAndSectionByResourceName` | — | — | C-01, C-02(1件), C-03, **C-04(空)** | — | E-1(0), E-4(1) | 🔺**F2-05** に近い（空 Map。実ファイルではない） |
| 19 | `read_containerCountMismatch_failsFast` | A-06 | B-3 | — | — | — | ✅ 器↔原文の件数不整合 → `IllegalStateException` |
| 20 | `read_fragmentRecordMismatch_failsFast` | A-06 | B-3 | — | — | — | ✅ 器の断片構造↔原文レコード不整合 → `IllegalStateException` |

<a id="s2-1-2"></a>

### 2.1-2 #24 が追加したテストクラスの担保（2026-08-14 追記）

**本節は #24 で新設した。** §2.1 は「`YamlFormatReaderTest` 20 件」を対象とした #18 時点の事実であり
書き換えていない。ここには #24 が追加した**実 `.yaml` を入力とするテストクラス**の担保だけを記す。

| テストクラス | 件数 | 導出コマンド | 入力 |
|---|---|---|---|
| `YamlFormatReaderScalarTest` | 27 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderScalarTest.java` | `YamlFixture` が書き出した実 `.yaml` |
| `YamlFormatReaderInvalidInputTest` | 25 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java` | 同上（軸F の 8 件のうち **7 件**は意図的にスキーマ違反・不正 YAML にした入力で、**1 件（F2-05 `readsEmptyFileAsContainerWithoutBlocks`）は空ファイル**＝スキーマ違反でも不正 YAML でもない。残る 17 件は**スキーマを通る仕様内の入力**で、掃引で見つけた現状挙動を固定する） |
| `YamlFormatReaderRealFileTest` | 18 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderRealFileTest.java` | 同上 |

**件数を更新した（2026-08-14・#24 のレビュー指摘の反映で 9 件追加）。** 内訳は
`YamlFormatReaderScalarTest` ＋4（軸D の経路差確認。下の「別経路での確認」表）、
`YamlFormatReaderRealFileTest` ＋5（C-14 実ファイル経路・`FieldDef.length` の integer 記法・
送信系の `directives`・YML-02・YML-03）。

**件数をさらに更新した（2026-08-14・2 巡目レビュー指摘の反映＝修正ラウンド 2 で 16 件追加）。** 内訳は
`YamlFormatReaderInvalidInputTest` ＋15（掃引で見つけた `issues.md` **YML-04**〜**YML-08** の固定。
下の「開示」の掃引表を参照）、`YamlFormatReaderRealFileTest` ＋1
（`normalizeRecordType` の小文字 `"default"` 分岐。下の「開示」1 点目）。
**この 16 件は軸A〜F のどの要素にも新しい担保を与えないため、§2.3 の件数は動かない。**

**件数をさらに更新した（2026-08-14・3 巡目レビュー指摘の反映＝修正ラウンド 3 で 1 件追加）。** 内訳は
`YamlFormatReaderRealFileTest` ＋1（`reordersBlocksByFirstAppearanceOfGroupIdFromRealYaml`。
掃引項目 24 で見つけた `issues.md` **YML-09** の固定。同クラスは修正ラウンド 2 の時点で 17 件だった
——`git show b26b5a7:src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderRealFileTest.java | grep -c '^    @Test'` → **17**）。
**この 1 件も軸A〜F のどの要素にも新しい担保を与えないため、§2.3 の件数は動かない。**

**件数をさらに更新した（2026-08-14・QA レビュー指摘の反映で 2 件追加）。** 内訳は
`YamlFormatReaderInvalidInputTest` ＋2（掃引項目 27 で見つけた `issues.md` **YML-10** の固定＝
`dropsValueWhenTableColumnNamesDifferOnlyByCase` と、その対比 `keepsOriginalColumnCaseInListMap`）。
**この 2 件も軸A〜F のどの要素にも新しい担保を与えないため、§2.3 の件数は動かない。**

3 クラスとも `new YamlFormatReader().read(...)` を本番配線で呼ぶ。`YamlFormatReaderTest` 20 件が
`loadRawMap` を in-memory `Map` へ差し替えて**スカラー解決もスキーマ検証も通らない**のに対し、
本 3 クラスはその区間を実行する（§0.8-3）。

**軸D（`YamlFormatReaderScalarTest`。§0.5 の 12 ケース）**

| 要素 | 担保テストメソッド | 観測した値 |
|---|---|---|
| D2-01 引用符なし文字列 | `readsUnquotedStringAsIs` | `abc` → `"abc"` |
| D2-02 引用符あり | `readsDoubleQuotedStringWithoutQuotes` ／ `readsSingleQuotedStringWithoutQuotes` | `"abc"` / `'abc'` → `"abc"`（引用の別は残らない） |
| D2-03 引用符付き数値 | `readsQuotedNumberAsString` | `"123"` → `"123"` |
| D2-04 引用符付き末尾ゼロ小数 | `readsQuotedTrailingZeroDecimalAsString` | `"1.50"` → `"1.50"` |
| D2-05 真偽値に見える文字列 | `readsQuotedTrueAsString` ／ `readsUppercaseTrueAsString` ／ `readsYesAsString` | `"true"` / `TRUE` / `yes` → いずれも記法どおりの文字列 |
| D2-06 NULL | `readsUnquotedNullAsJavaNull` ／ `readsOmittedValueAsJavaNull` | `null`（引用符なし）・値なし（`- V:`）→ **Java `null`** |
| D2-07 NULL に見える文字列 | `readsQuotedNullAsString` ／ `readsTildeAsString` ／ `readsUppercaseNullAsString` | `"null"` / `~` / `NULL` → **いずれも文字列**（`issues.md` **YML-01**） |
| D2-08 日付・日時風文字列 | `readsDateLikeStringAsIs` ／ `readsDateTimeLikeStringAsIs` | `2026-08-07` / `2026-08-07T12:34:56` → 記法どおりの文字列 |
| D2-09 複数行 | `readsLiteralBlockScalarKeepingNewlines` ／ `readsFoldedBlockScalarFoldingNewlinesIntoSpaces` | `\|` → `"l1\nl2\n"`、`>` → `"l1 l2\n"`（いずれも末尾に改行が付く） |
| D2-10 先頭ゼロ・非 JSON 数値記法 | `readsLeadingZeroNumberAsString` ／ `readsHexNotationAsString` | `007` / `0x1F` → 記法どおりの文字列 |
| D2-11 空文字・前後空白 | `readsEmptyStringAsIs` ／ `readsSurroundingWhitespacePreserved` | `""` → `""`、`"  pad  "` → 前後空白を保つ |
| D2-12 特殊文字を含む文字列 | `readsColonContainingStringAsIs` ／ `readsHashContainingStringAsIs` | `"a: b"` / `"a #b"` → 1 値として入る |

12 ケースすべてを ✅ とする。#18 が 🔺 としていた D2-02／D2-03／D2-06／D2-07（往復テスト経由。§0.8-8）は
本クラスの直接テストで ✅ になった。**D2-06 と D2-07 の分かれ方（`null`・値なし だけが Java `null` になり、
`~`・`NULL`・`"null"` は文字列になる）が本タスクで固定した中心の事実である。**

**軸D の測定経路と、別経路での確認（2026-08-14・レビュー指摘の反映で追加）**

上の 12 ケースは**すべて `setup_tables` の `rows` で測っている**（`YamlFormatReaderScalarTest#readValue` が
常に `setup_tables` へ値を置く）。`YamlFormatReader` の行値の取り出しはテーブル／LIST_MAP／レコード断片の
3 系統があり、スキーマも別パスで型を課す（テーブル・LIST_MAP は
`properties.rows.items.additionalProperties.type`、レコード断片は
`$defs.record_fragment.properties.rows.items.items.type`）。したがって「12 ケースの結果が他の 2 系統でも
同じか」は 12 ケースのテストだけからは言えない。**12 ケース × 3 経路には広げず**、`null` と空文字の
2 ケースだけを別経路で測った（**軸D の 12 ケース定義は変えていない**。下表は同一ケースを別経路で
確認したものであり、新しい軸要素ではない）。

| 元のケース | LIST_MAP 経路（`list_maps`） | レコード断片経路（`record_fragment.rows`） | 経路差 |
|---|---|---|---|
| D2-06 `null`（引用符なし） | `readsUnquotedNullAsJavaNullInListMapPath` → Java `null` | `readsUnquotedNullAsJavaNullInRecordFragmentPath` → Java `null` | **無し**（`setup_tables` 経路と同じ） |
| D2-11 空文字 `""` | `readsEmptyStringAsIsInListMapPath` → `""` | `readsEmptyStringAsIsInRecordFragmentPath` → `""`（**固定できる性質が弱い**。下の但し書き） | **無し**（同上） |

**但し書き（レコード断片経路の空文字）**: この経路では、行の要素数が `fields` の件数に足りないときに
欠けた位置が**空文字で埋められる**（**YML-05**）。したがって「書かれた `""`」と「書かれなかった位置」が
中間モデル上で区別できず、**`rows: - [""]` と `rows: - []` は同じ結果になる**（実測）。
`readsEmptyStringAsIsInRecordFragmentPath` が固定できるのは「`""` は Java `null` にならない」ことまでで、
**「書いた空文字が保たれた」ことは示せない**。テーブル／LIST_MAP 経路では欠けたキーが `null` になるため
（`padsColumnMissingFromSecondRowWithNullInTable`）区別できる。この差はテストの Javadoc にも書いた。

残る 10 ケースを別経路で測っていないことは、下の「開示」に穴として記す。

**軸F（`YamlFormatReaderInvalidInputTest`。§0.7 の 5 ケース）**

| 要素 | 判定 | 担保テストメソッド | 観測した挙動 |
|---|---|---|---|
| F2-01 スキーマ違反 | ✅ | `failsWithSchemaValidationExceptionWhenFileTypeIsNotInEnum` ／ `failsWithSchemaValidationExceptionWhenFieldLengthDoesNotMatchPattern` | `YamlSchemaValidationException`。**違反のキーワードの集合と位置を、順序も件数も含めて厳密にアサートする** — 前者は `enum` 1 件・位置 `$.setup_files[0].type`、後者は `type` と `pattern` の 2 件（`length` が `anyOf` であり `"1a"` が両枝を外すため）で位置はいずれも `$.setup_files[0].records[0].fields[0].length`。**入力に、`rows` の値として仕様外とした引用符なしスカラー記法は使っていない**（`issues.md`「対象としない入力（辺②）」） |
| F2-02 YAML として不正 | ✅ | `failsWithParseErrorWhenYamlIsMalformed` | `IllegalStateException`（メッセージは `Failed to parse YAML file: <path>` で始まる）。原因例外は `YamlEngineException`。パースで止まるためスキーマ検証には到達しない |
| F2-03 未知のキー | ✅ | `failsWithSchemaValidationExceptionWhenTopLevelKeyIsUnknown` | `YamlSchemaValidationException`（`additionalProperties` 違反）。**in-memory 経路（`YamlFormatReaderTest#read_mixedSections_keepsDescriptionOrderAndIgnoresUnknownKeys`）が固定している「未知キーは無視」とは結果が異なる**。スキーマのルートが `additionalProperties: false` であるため、実ファイルでは読み込みごと失敗する |
| F2-04 必須構造の欠落 | ✅ | `failsWithSchemaValidationExceptionWhenRequiredRowsIsMissing` ／ `failsWithSchemaValidationExceptionWhenFieldsIsEmpty` ／ `failsWithSchemaValidationExceptionWhenFieldTypeIsMissing` | `YamlSchemaValidationException`（`required` / `minItems`）。後ろ 2 件は軸C の **C-17／C-20 が到達不能である根拠**でもある |
| F2-05 空ファイル | ✅ | `readsEmptyFileAsContainerWithoutBlocks` | 例外にならず、リソース名のコンテナ 1 件・セクション 1 件・ブロック 0 件になる |

**軸A（`YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml`）** — 1 ファイルに 11 セクションキー・
13 エントリを書き、`DEFAULT` を除く 13 種すべてが記述順に生成されることを `getDataType()` の並びでアサートする。
#18 時点で既に ✅ だった（in-memory 経路）が、**実 `.yaml` 経路でも ✅ になった**。
A-01 `DEFAULT` は到達不能のまま（§0.8-7）。

**軸B（`#readsFourBlockImplementationsFromOneRealYaml`）** — `TableDataBlock` / `ListMapBlock` /
`FileDataBlock` / `MessageDataBlock` の 4 種を 1 ファイルから生成し、実装クラスと識別子
（`T` / `lm` / `f.dat` / `RM01`）を突き合わせる。#18 時点で既に ✅ だったものを実 `.yaml` 経路でも通した。

**軸C（#18 時点から状態が変わったものだけ）**

| 要素 | #18 の判定 | #24 後 | 担保テストメソッド（`YamlFormatReaderRealFileTest#`）／根拠 |
|---|---|---|---|
| C-08 `columnNames` 空 | ❌ | ✅ | `readsEmptyColumnNamesAndRowsFromTableWithoutRows` ／ `readsEmptyColumnNamesAndRowsFromListMapWithoutRows`（`rows: []` で到達） |
| C-09 `rows` 空 | ❌ | ✅ | 同上（C-08 と同じ入力） |
| C-12 `FileDataBlock.records` 空 | ❌ | ✅ | `readsEmptyRecordsFromFixedFileWithoutRecords`（`records: []`。スキーマ `file_data` は `minItems: 0`） |
| C-18 `RecordLayout.rows` 空 | ❌ | ✅ | `readsEmptyRowsFromRecordLayoutWithoutRows` |
| C-13 `MessageDataBlock.directives` 値あり | ❌ | ✅ | `readsMessageDirectivesFromRealYaml` |
| C-14 `MessageDataBlock.fwHeaderFields` 値あり | ✅（in-memory のみ） | ✅（**実ファイル経路でも**） | `readsFwHeaderFieldsFromRealYaml`（`fw_header:` の 2 キーが記述順で入る。#24 のレビュー指摘の反映で追加） |
| C-21 `FieldDef.length` 値あり（integer 記法） | ✅（文字列記法のみ） | ✅（**integer 記法でも**） | `readsIntegerLengthNotationAsString`（`length: 10` はスキーマの `anyOf` 第 1 枝を通り、中間モデルには文字列 `"10"` が入る。#24 のレビュー指摘の反映で追加） |
| C-11 `FileDataBlock.directives` 空 | ❌ | **到達不能** | `issues.md` **XLS-07** と同じ器。根拠テスト `readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInFile` |
| C-13 `MessageDataBlock.directives` 空 | ❌ | **到達不能** | 同上。根拠テストは **2 つの生成経路それぞれ**にある — 受信メッセージ経路（`YamlFormatReader#addMessageBlocks`）が `readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInMessage`、送信系経路（`#addSendSyncBlocks`）が `readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInSendSync`（#24 のレビュー指摘の反映で後者を追加） |
| C-17 `RecordLayout.fields` 空 | ❌ | **到達不能** | スキーマ `$defs.record_fragment.properties.fields.minItems` ＝ 1。根拠テスト `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFieldsIsEmpty` |
| C-20 `FieldDef.type` 省略 | ❌ | **到達不能** | スキーマ `$defs.field_def.required` が `type` を必須とする。根拠テスト `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFieldTypeIsMissing` |

**軸E（#24。0 件・複数件を実 `.yaml` 経路で担保した）**

| 要素 | 0 件 | 1 件 | 複数件 | 担保テストメソッド（`YamlFormatReaderRealFileTest#`） |
|---|---|---|---|---|
| E-1 セクション内ブロック数 | ✅ | ✅ | ✅ | 0: `namesContainerAndSectionByResourceNameWithoutBlocks`／1: `readsEmptyColumnNamesAndRowsFromTableWithoutRows` ほか（ヘルパ `onlyBlock` が `blocks.size()==1` をアサート）／複数: `readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml`（3 件）・`readsAllThirteenDataTypesFromRealYaml`（13 件） |
| E-2 ブロック内行数 | ✅（**#24**） | ✅ | ✅ | 0: `readsEmptyColumnNamesAndRowsFromTableWithoutRows` ／ `readsEmptyColumnNamesAndRowsFromListMapWithoutRows`／1: `readsFourBlockImplementationsFromOneRealYaml`（テーブル・LIST_MAP とも `getRows().size()` が 1 であることをアサートする。レビュー指摘を受けて行数アサートを足し、引用を真にした）／複数: `readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml`（2 行） |
| E-3 ファイル内レコードレイアウト数 | ✅ | ✅ | ✅ | 0: `readsEmptyRecordsFromFixedFileWithoutRecords`／1: `readsEmptyRowsFromRecordLayoutWithoutRows`／複数: `readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml`（断片 2 件） |
| E-4 コンテナ内セクション数 | n/a | ✅ | ❌（**到達不能**） | 1: `namesContainerAndSectionByResourceNameWithoutBlocks`。複数は `YamlFormatReader#read` が `Collections.singletonList(section)` を返すため構造上到達不能（§0.8-6） |

**開示（テストを足していない担保の穴）**

- **`YamlFormatReader#normalizeRecordType` の `"default"`（小文字）分岐は到達可能であり、修正ラウンド 2 で閉じた。**
  **当初「軸A〜F のどの要素にも属さない」として #26 送りと開示していたが、これは誤りだった**
  （2026-08-14・2 巡目レビュー指摘）。`record_type: "default"` を書いた実 `.yaml` は
  スキーマを通り（`$defs.record_fragment.properties.record_type` に `enum` は無く、description も
  「可読性のために任意の名前を記述してよい」と書いている）、この分岐に到達する。
  実測すると**作成者が書いた `"default"` は中間モデルで `null` になる**（＝原文に残らない）。
  修正ラウンド 2 で `YamlFormatReaderRealFileTest#normalizesLowercaseDefaultRecordTypeToNull` を足して閉じた
  （`"Default"` 側は `YamlFormatReaderTest#readFile_recordTypeDefault_normalizedToNull` が通す）。
  **修正ラウンド 3（2026-08-14）で JaCoCo を取り直した結果、`YamlFormatReader` は
  行 201/201（100%）・分岐 108/108（100%）である。** 修正ラウンド 2 の時点で記していた
  「分岐 107/108（99.07%）・唯一の未到達分岐は `"default"` 側」は、**その分岐を閉じたあとの数値ではなく
  再実行しても再現しない**ため、実測値へ差し替えた（3 巡目レビュー指摘）。導出コマンド
  （**オフラインで実行できる形に直した**。JaCoCo 手順は steering Decisions のとおり）:

  ```sh
  JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes \
    && JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec \
    && awk -F, 'NR > 1 && $3 == "YamlFormatReader" { print "line " $9 "/" ($8 + $9) " branch " $7 "/" ($6 + $7) }' \
         target/site/jacoco/jacoco.csv
  ```

  出力は `line 201/201 branch 108/108`。**この数値は `YamlFormatReader` 1 クラスぶんであり、
  4 辺の担当クラス全体の計測と未到達分岐の列挙は #26 の仕事である。**
- **軸C の C-15（`MessageDataBlock.records` 空）は実 `.yaml` 経路では到達不能である。**
  スキーマ `$defs.message_data.properties.records.minItems` ＝ 1 のため。#18 が ✅ としているのは
  in-memory 経路（`YamlFormatReaderTest#readMessage_emptyBody_isStillMapped`）であり、
  **実ファイル経路での担保は無い**（`issues.md`「到達不能と判定した軸要素（#24）」）。
- **軸D の 12 ケースのうち 10 ケースは 1 経路（`setup_tables`）でしか測っていない。**
  `YamlFormatReaderScalarTest#readValue` は常に `setup_tables` へ値を置く。行値の取り出しは
  テーブル／LIST_MAP／レコード断片の 3 系統あり、スキーマも別パスで型を課すため、残り 2 経路での
  結果は D2-06（`null`）と D2-11（空文字）の 2 ケースしか確認していない
  （上の「別経路での確認」表。2 ケースとも経路差は無かった）。
  **12 ケース × 3 経路には広げないと判断した**（経路差が無いという観測が 2 ケースで得られたこと、
  および 3 系統とも同じ `YamlLoader` のスカラー解決結果を受け取る構造であることによる）。
  残る 10 ケースについて経路差が無いことは**未確認**である。
- **「警告が出ないこと」は辺②の課題（YML-02／YML-03）ではアサートしていない。**
  辺①の XLS-10／XLS-13 は `java.util.logging` のルートロガーにハンドラを付けて
  「WARNING 以上 0 件」を実行可能な根拠にしているが、#24 の 2 件は例外にならないことと
  結果が消えることの観測にとどめた（`issues.md` の各項に明記）。

**開示（修正ラウンド 2＝スキーマの自由度の掃引。2026-08-14 追記）**

軸A〜F は「中間モデルの形」から要素を立てているため、**軸のどの要素にも当てはまらない壊れ方**は拾えない。
2 巡目のレビュー指摘を受けて、**本体スキーマ**（yaml jar 内 `nablarch/test/ntf-testdata-yaml-schema.json`）を
先頭から読み、「スキーマが構造を縛っていない箇所」を列挙し、その自由度を使った入力を実 `.yaml` で
**1 項目につき 1 回ずつ**通した。以下がその全件である（**これが「どこまで見たか」である**）。
課題として記録したものは `issues.md` の該当 ID を、記録しなかったものは観測結果を書く。

| # | スキーマ上の自由度（JSON パス） | 自由度の中身 | 実 `.yaml` で観測した結果 |
|---|---|---|---|
| 1 | `$defs.table_data.properties.rows.items` | `additionalProperties` が任意キーを許し、行ごとのキー集合に制約が無い。**キーの大小・一意性にも制約が無い** | **YML-04**（先頭行に無いキーが黙って消える）。**大小だけが違うキー（`id` と `ID`）を同一行に書いた場合は YML-10**（大文字化で衝突し値が消える） |
| 2 | `$defs.list_map_data.properties.rows.items` | 同上 | **YML-04**（経路差なし）。ただし**大小の衝突は起きない** —— LIST_MAP は原文の大小のまま入る（**YML-10** の対比） |
| 3 | `$defs.record_fragment.properties.rows.items` | 要素数が `fields` の件数と紐づいていない | **YML-05**（余りは drop・不足は `""` 充填） |
| 4 | `$defs.record_fragment.properties.record_type` | `enum` が無い（description は「任意の名前でよい」） | `FW_HEADER` は **YML-03**（既記録）。`"Default"` / `"default"` は `null` へ正規化（テストで固定）。`"DEFAULT"` や任意名は原文のまま |
| 5 | `$defs.field_def.properties.type` | `enum` が無い（`minLength: 1` のみ） | 未知の型名は `IllegalArgumentException`（loud）。課題なし |
| 6 | `$defs.record_fragment.properties.fields` | `uniqueItems` が無く、フィールド名の重複は description にだけ「重複不可（重複時はエラー）」と書かれている | `IllegalArgumentException: Duplicate field names are not permitted in a record. duplicate field=[f1] . file=[f.dat]`（loud）。課題なし |
| 7 | `$defs.field_def.properties.length` の `"-"` | パターンは許すが意味は description にだけ書かれている | **YML-07**（`text-encoding` 省略時に手掛かりの無い NPE） |
| 8 | `$defs.field_def.properties.length` の省略 | 「固定長では実質必須」は description にだけ書かれている | 例外にならず `FieldDef.length` が `null` になる。変換は忠実。課題なし |
| 9 | `$defs.directives`（固定長専用／可変長専用の別） | キー集合は固定だが、ファイル種別との対応は description にだけ書かれている | 取り違えは `IllegalArgumentException: invalid directive found. [...]`（loud）。課題なし。**器が注入する既定ディレクティブはファイル種別で違う** —— 固定長は `{file-type=Fixed}` の 1 件、可変長は `{file-type=Variable, field-separator=,}` の 2 件（実測。C-11 が「空にならない＝到達不能」であることは両種別で成り立つ） |
| 10 | `$defs.directives.properties.record-separator` ／ `field-separator` | シンボル指定とリテラル指定の両方を description が認めている | **YML-08**（リテラルは trim で消える／シンボルは実文字になる） |
| 11 | `$defs.directives.properties.file-type` | `type` フィールドと矛盾させられる（整合の制約が無い） | 例外にならず、ブロックは `FIXED` のまま `file-type=Variable` を保持する。原文はどちらも残るため変換は忠実。課題なし |
| 12 | `$defs.directives.properties.record-length` | フィールド長の合計と紐づいていない | 上書き値がそのまま中間モデルへ入る。変換は忠実。課題なし |
| 13 | `$defs.fw_header.additionalProperties` | 任意キーを許す（`minProperties: 0`） | 記述順のまま入る。空マップも通る。課題なし |
| 14 | `$defs.list_map_data.properties.id` ／ `$defs.message_data.properties.id` | 一意制約が無い（「先着1件」は description にだけ書かれている） | **YML-06**（2 件目以降が 1 件目のデータで作られる） |
| 15 | `$defs.table_data.properties.table` ／ `$defs.file_data.properties.path` | 一意制約が無い | 重複してもエントリごとに独立したブロックになる。変換は忠実。課題なし |
| 16 | `$defs.group_message_data` ／ `$defs.expected_request_message_data` の `group_id` 省略 | 省略時の意味は description にだけ書かれている | **YML-02**（既記録） |
| 17 | `$defs.table_data` ／ `$defs.file_data` の `group_id` 省略 | 同上 | 整形済みグループ ID が空文字になる。変換は忠実。課題なし |
| 18 | `rows` の行オブジェクトのキー順 | 行ごとに順序が違ってよい | 値は名前で対応付けられるため順序差の影響は出ない。課題なし |
| 19 | マーカーカラム `[COL]` | スキーマは通常のキーと区別しない（全カラムがマーカーでも通る） | カラム 0 件・値を持たない行になる。辺①の **XLS-08** と同型（テストで固定） |
| 20 | `$defs.file_data.properties.records` の `minItems: 0` ／ `rows` の空配列 | 空を許す | 既に担保済み（C-12／C-09／E-2(0)／E-3(0)） |
| 21 | `$defs.message_data.properties.records` に `FW_HEADER` 名を書ける | `enum` が無い | **YML-03**（既記録）。送信系（`response_body_messages`）でも同じくブロックだけが残ることをプローブで確認（テストは固定していない） |
| 22 | `$defs.message_data.properties.records` に断片を 2 件以上書ける | 件数の上限が無い | 2 件とも保持される。辺①では **XLS-15** により不可能な形が辺②では作れる。課題なし |
| 23 | `expected_request_header_messages` と `expected_request_body_messages` の件数一致 | スキーマは縛らず description にだけ書かれている | converter は片方だけでもブロックを作る。NTF 実行時の制約であり変換の正しさとは別。課題なし |
| 24 | セクション配列内でのエントリの並び（`$defs.table_data` ／ `$defs.file_data` ／ `$defs.group_message_data` の `group_id`） | 同じ `group_id` のエントリが配列内で連続することを要求していない（順序の制約が無い） | **YML-09**（`g1` → `g2` → `g1` と書くとブロックがグループの初出順にまとめ直され、原文の記述順と食い違う。テーブル系・ファイル系・送信系の 3 経路とも同じ。**課題として記録した** — 判断の根拠は下の「掃引項目 24 を課題とした理由」） |
| 25 | `$defs.field_def.properties.length` の `"0"` | パターンは許すが意味（ダミーフィールド）は description にだけ書かれている | `"0"` が忠実に `FieldDef.length` へ入る（例外にも既定値の補完にもならない）。課題なし |
| 26 | 識別子系プロパティの `minLength` 不在（`$defs.table_data.properties.table` ／ `$defs.file_data.properties.path` ／ `$defs.list_map_data.properties.id` ／ `$defs.message_data.properties.id` ／ `$defs.record_fragment.properties.record_type` ／ `$defs.field_def.properties.name`）。`group_id` と `$defs.field_def.properties.type` には `minLength: 1` があるという非対称 | 空文字の識別子が書ける | 6 つとも例外にならず、**空の識別子がそのまま中間モデルへ入る**（`table: ""` → `""`、`path: ""` → `""`、`id: ""` → `""`、`name: ""` → `""`）。`table: "   "` は器が trim するため `""` になる。**`record_type: ""` は `null` ではなく `""` で入り、省略（`null`）と分かれる**（C-16 の「省略＝`null`」と隣接する事実。辺①の **XLS-06** は逆に実 `.xlsx` 経路で省略が `""` になる）。黙って消えるものは無いため課題なし |
| 27 | **器が行う正規化**（スキーマが縛っていない箇所ではなく、スキーマが触れていない箇所）。`$defs.table_data.properties.table` の description は「NTF により trim・大文字変換される」と書くが、**カラム名の大文字化についてはスキーマのどこにも記述が無い** | テーブル系は器（`TableData`）がテーブル名とカラム名を大文字化する（`my_table` → `MY_TABLE`、`user_id` → `USER_ID`）。LIST_MAP は原文の大小のまま | **YML-10**（大小だけが違うキーが大文字化で衝突し、値が黙って消えて列名が重複する）。大文字化そのものは `issues.md`「課題としないと判断した観測結果（#24）」に記録 |

**掃引項目 24 を課題とした理由（記録する／しないの判断・2026-08-14）**

データは失われない（値は正しいまま入る）が、**変換結果が原文と一致しない**（並びが変わる）。
辺③④はこの順で書き出すため、変換後の成果物ではエントリの並びが原文と入れ替わる。
本リポジトリは並びの保持を変換の正しさとして扱ってきた（#15「LIST_MAP 列順保持修正」は列順が
アルファベット順になることを不具合として直した）ため、「変換は忠実」として課題なしにはできない。
NTF は `group_id` で収集するため実行結果は変わらず、後段のテストは通ってしまう＝**検出できない**。
以上より `issues.md` に **YML-09**（影響度 中・検出できない）として記録し、根拠テスト
`YamlFormatReaderRealFileTest#reordersBlocksByFirstAppearanceOfGroupIdFromRealYaml` で現状挙動を固定した。

**この掃引で見ていない範囲（穴として開示する）**

- **各自由度は 1 項目につき 1 回ずつしか通していない。** 自由度どうしの組合せ
  （例: `id` 重複とカラム不一致を同時に持つ入力）は見ていない。
- **スキーマの description が NTF の実行時挙動を述べている部分は観測していない。**
  FK 制約と DELETE 順序、`expected_complete_tables` の型別デフォルト値補完、
  `testShots` の予約 ID など、変換ではなく NTF 実行時に効く記述である。確かめるには NTF の実行が要る。
- **辺③（中間モデル→Excel）・辺④（中間モデル→YAML）へ書き出したときの挙動は掃引の対象外である。**
  とくに YML-08（中間モデルに実改行が入る）の往復が安定するかは未確認であり、`issues.md` に明記した。
- **インタープリタ記法（`${...}` など値の中身の記法）は掃引していない。**
  スキーマは `rows` の値を `["string","null"]` としか縛っておらず記法に踏み込まないため、
  「スキーマが縛っていない箇所」ではあるが、converter は `InterpreterResolver.raw()` で配線しており
  値を素通しする（`${...}` を含む値が原文のまま入ることは in-memory 経路の `YamlFormatReaderTest` が
  固定している。実 `.yaml` 経路では確かめていない）。
- **軸D の 12 ケースを掃引の各項目へ掛け合わせてはいない**（上の 3 点目の「1 回ずつ」と同じ理由）。
- **値のサイズ・行数の上限は掃引していない**（2026-08-14・3 巡目レビュー指摘で追記）。
  辺③には D3-07（Excel のセル文字数上限 32767 を超える値。`issues.md` **XLS-19**）があるが、
  辺②の軸D 12 ケースにも上の掃引 27 項目にもサイズの観点は無い。**1 回だけ観測した**結果、
  40,000 文字の値は中間モデルへ同じ長さ・同じ内容で入り、5,000 行のテーブルも行数どおり入った
  （例外にも切り詰めにもならない）。スキーマは `rows` の値を `["string","null"]` としか縛らず
  長さ・件数の上限を持たないため**上限そのものは掃引していない**。テストは足していない。

<a id="s2-3"></a>

### 2.3 辺② 未担保一覧（#24 が埋めた対象）

計上単位と「状態」の 3 分類は §1.3 の規則に従う。

**本表は #24 の実測結果に合わせて「#24 後の状態」列を足した（2026-08-14）。** #18 時点は
「要追加 23 ／ 到達不能 3」であった。軸D の定義が 10 ケース → **12 ケース**へ改まった（§0.5）ため
総計は 26 → **28**、#18 基準の要追加は 23 → **25** になる。#18 時点の分類は「#18 の状態」列に残した。

| 軸 | 未担保要素 | #18 の状態 | #24 後の状態 | 件数 |
|---|---|---|---|---|
| A | （要追加はなし） | — | — | 0 |
| A | A-01 `DEFAULT` — `YamlFormatReader` の分岐に `DEFAULT` を返す経路がない（§0.8-7） | 到達不能 | 到達不能（変更なし） | 1 |
| B | （なし） | — | — | 0 |
| C | C-08 columnNames 空／C-09 rows 空／C-12 FileDataBlock.records 空／C-18 RecordLayout.rows 空／C-13 MessageDataBlock.directives（値あり） | 要追加 | **担保済み（#24）** — 順に `YamlFormatReaderRealFileTest#readsEmptyColumnNamesAndRowsFromTableWithoutRows`（C-08／C-09。LIST_MAP 経路は `#readsEmptyColumnNamesAndRowsFromListMapWithoutRows`）／`#readsEmptyRecordsFromFixedFileWithoutRecords`（C-12）／`#readsEmptyRowsFromRecordLayoutWithoutRows`（C-18）／`#readsMessageDirectivesFromRealYaml`（C-13） | 5 |
| C | C-11 FileDataBlock.directives 空 | 要追加 | **到達不能** — 本体 `DataFile` のコンストラクタが `file-type` を必ず注入する（`issues.md` **XLS-07**）。根拠は `#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInFile` がテストで示す | 1 |
| C | C-17 fields 空／C-20 FieldDef.type 省略 | 要追加 | **到達不能** — スキーマが `fields` に `minItems: 1` を、`field_def` に `type` 必須を課す。根拠は `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFieldsIsEmpty`／`#failsWithSchemaValidationExceptionWhenFieldTypeIsMissing` がテストで示す | 2 |
| C | C-02 sections 空 — `YamlFormatReader#read` が `Collections.singletonList(section)` を返すため sections は常に 1 件（§0.8-6） | 到達不能 | 到達不能（変更なし） | 1 |
| D | §0.5 の 12 ケース（D2-01〜D2-12。#18 時点の定義では 10 ケース。うち D2-02／D2-03／D2-06／D2-07 は往復テスト経由の 🔺 があった。§0.8-8） | 要追加 | **担保済み（#24）** — `YamlFormatReaderScalarTest` 27 件（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderScalarTest.java` → **27**）。要素別の担保テストメソッドは §2.1-2 の軸D 表。27 件のうち 4 件は D2-06／D2-11 を LIST_MAP 経路・レコード断片経路で確認したもので、**軸要素としては別勘定にしない**（§2.1-2 の「別経路での確認」表） | 12 |
| E | E-2(0 件) | 要追加 | **担保済み（#24）** — C-08／C-09 と同じ入力（`#readsEmptyColumnNamesAndRowsFromTableWithoutRows` ほか 1 件） | 1 |
| E | E-4(複数) — `YamlFormatReader#read` が 1 リソース単位 API（§0.8-6） | 到達不能 | 到達不能（変更なし） | 1 |
| F | F2-01 スキーマ違反／F2-02 不正 YAML／F2-04 必須構造欠落／F2-05 空ファイル（🔺 のみ） | 要追加 | **担保済み（#24）** — 軸F を担保するのは `YamlFormatReaderInvalidInputTest` の **8 件**である（同クラスの総数は `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java` → **25**。差の 17 件は掃引の固定テスト（`issues.md` YML-04〜YML-08・YML-10）であり、**軸F の要素ではない**。§2.1-2 の「開示」の掃引表を参照）。**8 件と 15 件の導出コマンドは本表の下**。**内訳（本行の 4 ケース 7 件 ＋ F2-03 の 1 件）**: F2-01 が 2 件／F2-02 が 1 件／F2-04 が 3 件／F2-05 が 1 件／F2-03 が 1 件。F2-04 の 3 件のうち 2 件（`#failsWithSchemaValidationExceptionWhenFieldsIsEmpty` ／ `#failsWithSchemaValidationExceptionWhenFieldTypeIsMissing`）は C-17／C-20 が到達不能である根拠を兼ねる（別勘定ではない）。F2-03 未知のキーは #18 時点で既に ✅（in-memory）だが実ファイル経路では結果が異なるため §2.1-2 の軸F 表に併記した | 4 |
| **合計** | | **要追加 25 ／ 到達不能 3** | **要追加 0 ／ 担保済み 22 ／ 到達不能 6 ／ 対象外 0** | **28** |

**軸F の 8 件と、差の 17 件の導出**（2026-08-14・3 巡目レビュー指摘で追加。総数 25 は上の表に
コマンドを併記しているが、内訳の 8 と 17 は数字のまま置かれていた）。
`YamlFormatReaderInvalidInputTest` は軸F の節（F2-01〜F2-05）を先頭に、掃引の節（YML-04 以降）を
後ろに置いており、境界は節見出しのコメント行である:

```sh
awk '/F2-01 スキーマ違反/,/YML-04 先頭行のキー集合/' \
    src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java \
  | grep -c '^    @Test'
awk '/YML-04 先頭行のキー集合/,0' \
    src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java \
  | grep -c '^    @Test'
```

出力は順に **8** と **15**（8 ＋ 15 ＝ 23）。8 件のメソッド名は §2.1-2 の軸F 表に全件挙げてある
（F2-01 が 2 件／F2-02 が 1 件／F2-03 が 1 件／F2-04 が 3 件／F2-05 が 1 件）。

**C-13 の数え方**: C-13 は「値あり」を #24 で担保し、「空」は到達不能である（§2.1-2 の軸C 表）。
計上単位は「軸要素 1 件を 1 件」（§1.3）であるため、**C-13 は担保済み側で 1 件だけ数え**、
到達不能側には計上しない。

**合計の検算**（表の「件数」列を上から順に足す）:

- 担保済み: C 5 ＋ D 12 ＋ E-2(0) 1 ＋ F 4 ＝ **22**
- 到達不能: A-01 1 ＋ C-11 1 ＋ C-17/C-20 2 ＋ C-02 1 ＋ E-4(複数) 1 ＝ **6**
- 要追加: **0**
- 総計: 22 ＋ 6 ＝ **28**（B は 0 件）

**#18 時点の「特に大きな空欄」**（軸D 全滅 ——当時の定義では 10 ケース、現在の定義では 12 ケース—— と、
`MessageDataBlock.directives` が値あり・空の両方とも 0 件）は #24 で解消した。
**辺②の「要追加」は 0 件**である。残る到達不能 6 件のうち 3 件（C-11／C-17／C-20）は根拠テストを持ち、
3 件（A-01／C-02／E-4(複数)）は `YamlFormatReader#read` と `addBlocksForSection` の構造そのものが根拠である。

**ただし「未担保 0 件」は本書の計上単位（§1.3 冒頭）での話である。** §2.1-2 末尾の「開示」4 点
（`normalizeRecordType` の `"default"` 分岐——**修正ラウンド 2 で到達可能と判明しテストで閉じた**／
C-15 は実ファイル経路では到達不能／軸D の 10 ケースは 1 経路でしか測っていない／
YML-02・YML-03 で「警告が出ないこと」を実行可能な形にしていない）と、
**掃引で列挙した 27 項目および「見ていない範囲」6 点**は
空欄・穴として残る（§2.1-2 の「開示（修正ラウンド 2）」。項目 24〜27 と「見ていない範囲」6 点目は
修正ラウンド 3 で足した）。**掃引はここで閉じる。**

導出コマンド（自由度の項目数 26。掃引表は §2.1-2 の中で唯一「先頭列が番号」の表である）:

```sh
awk '/^### 2\.1-2 /,/^<a id="s2-3">/' .rn/ntf-test-data-converter/coverage/inventory.md \
  | grep -cE '^\| [0-9]+ \|'
```

**#24 のレビュー指摘の反映（2026-08-14）で足したテスト 9 件は、上の件数を動かさない。** 内訳は
（a）既に ✅ だった軸要素を実ファイル経路・別経路でも通したもの（C-14 値あり／C-21 値あり／
軸D の別経路 4 件／E-2(1 件) の行数アサート）、（b）到達不能の根拠をもう 1 つの生成経路にも用意したもの
（C-13 空・送信系経路）、（c）軸要素ではなく現状挙動の課題を固定したもの
（`issues.md` **YML-02**（送信系の `group_id` 省略でブロックが消える）／**YML-03**
（`record_type: FW_HEADER` のレコードが捨てられる））である。
（c）の 2 件は軸A〜F のどの要素にも新しい ✅ を与えないため、計上単位の上では件数に影響しない。

**修正ラウンド 2（2026-08-14）で足したテスト 16 件も、上の件数を動かさない。** 15 件は
「スキーマが構造を縛っていない箇所を突いた入力」の現状挙動の固定（`issues.md` **YML-04**〜**YML-08**）で、
軸A〜F のどの要素にも属さない。残る 1 件（`normalizesLowercaseDefaultRecordTypeToNull`）は
C-16 の正規化を実 `.yaml` で確かめたもので、C-16 は #18 時点で既に ✅ である。

**修正ラウンド 3（2026-08-14）で足したテスト 1 件も、上の件数を動かさない。**
`reordersBlocksByFirstAppearanceOfGroupIdFromRealYaml` は掃引項目 24（`issues.md` **YML-09**）の
現状挙動の固定であり、軸A〜F のどの要素にも属さない。

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

**上表 40 件の内訳（実測・2026-08-13）**: **実ファイル 10 件 ／ `build` 28 件 ／
SUT のブックを作らない `ExcelFormatConfig` 単体 2 件**（10 ＋ 28 ＋ 2 ＝ 40）。
§3.1-3 の `XlsFormatWriterModelTest` との違いは「`build` か実ファイルか」ではなく
「**全件が実ファイル経路か否か**」である。

導出コマンド（`@Test` ごとにメソッド本体を切り出し、本体に現れる呼び出しで分類する。
`roundTrip` ヘルパ経由の 8 件を実ファイル側に数えるため `roundTrip(` も見る）:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
awk '/^    @Test/{t=1;d=0;s=0;b="";next}
     t{b=b $0"\n"; o=gsub(/\{/,"{"); c=gsub(/\}/,"}"); d+=o-c; if(o>0)s=1;
       if(s&&d<=0){t=0; tot++; if(b~/\.write\(/||b~/roundTrip\(/)w++; else if(b~/build\(/)bu++; else nn++}}
     END{printf "@Test=%d build=%d write=%d neither=%d\n",tot,bu,w,nn}' \
  src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterTest.java
```

```
@Test=40 build=28 write=10 neither=2
```

`@Test=40` が `grep -c '@Test' <同ファイル>` の 40 と一致することが、切り出しに漏れが無いことの確認になる
（`@Test(expected = ...)` 付きの 4 件を落とさないため、パターンは `^    @Test$` ではなく `^    @Test` である）。
`build` と `write` の両方を呼ぶメソッドは無い。

**実ファイルを書く 10 件**: `roundTrip` ヘルパ経由の 8 件（`roundTripsTable` / `roundTripsNullCellAsLiteralNullString` /
`roundTripsListMap` / `roundTripsFixedFile` / `roundTripsMultipleRecordLayouts` / `roundTripsVariableFile` /
`roundTripsMessage` / `roundTripsSendSyncMessage`）と `writesWorkbookFileWithSheetPerSection` ／ `wrapsIoFailure`。
**どちらも呼ばない 2 件**: `eachGroupHasDistinctDefaultColor` ／ `rejectsNegativeBlankRows`。
どちらも `ExcelFormatConfig` だけを叩き、`XlsFormatWriter` のブックを作らない
（前者は既定色 4 種が互いに異なることを、後者は負数を渡すと `IllegalArgumentException` になることを見る）。

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
| F3-01 出力先不在 | 🔺 | ✅ | `createsMissingOutputDirectoriesAndWritesWorkbook` | 例外にならず多階層の出力先が作られ、ブックが書き出される（`XlsFormatWriter#write` の `Files.createDirectories`）。既存の 🔺 `XlsFormatWriterTest#wrapsIoFailure` は「親に通常ファイルが居座りディレクトリを作れない」別ケース（`UncheckedIOException`）であり、両方で出力先まわりが揃う |
| F3-02 `overwrite=false` 衝突 | 対象外 | **対象外（変更なし）** | —（本クラスに該当テストは無い） | `XlsFormatWriter` は `overwrite` を保持しない（保持するのは `ConversionRequest` / `TestDataConverter` / `ConverterMojo`。§0.8-5）。衝突検査は `XlsFormatWriter` を呼ぶ前に上位層（`TestDataConverter#checkOverwrite`）で完結するため、辺③ では再現できない。**ただし「上位層の既存テストが担保している」のは `.yaml` を出力側とする衝突だけである**: `TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse`／`ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict` はどちらも XLS→YAML であり、通るのは `YamlFormatHandler#outputPaths`。`XlsFormatHandler#outputPaths` 自体は `overwrite=false` 下で実行されている（`TestDataConverterTest#convertsYamlToXls`, `#convertsXlsToXls` ほか 1 件（計 3 件）。変異で実証。§0.8-5 と同じ 3 件）が、**`.xlsx` が既存で衝突する分岐**（`checkOverwrite` の `Files.exists(output)` → `ConverterException`）は 1 件も通っていない（§0.8-5 の訂正） |
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
**`parent == null` 分岐は未担保**である。`src/main` のコメントが
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
  2. 本番配線 `TestDataConverter#convert`（`writer.write(container, outputBase.toString())`）。
     `outputBase` は `XlsFormatHandler#resolveOutputBase` が `request.getOutputPath()` から組む。
     テストが渡す出力先は `TemporaryFolder` 由来の絶対パス（`TestDataConverterTest` の `out = folder.newFolder("out").toPath()`／
     `ConverterMojoTest` の `inject(mojo, "output", out.toFile())`）と、`SampleConversionTest` の
     `OUTPUT_BASE = Paths.get(".output/SampleConversionTest")` だけで、いずれも空文字にならない。
- **#22 では埋めない。** #22 の軸F の定義は F3-01 出力先不在／F3-02 `overwrite=false` 衝突／F3-03 書き込み権限なし／
  F3-04 シート名制約違反の 4 要素（§0.7）であり、この分岐はいずれにも当たらない。
  本書の軸要素ではないため §3.3 の件数には算入しない（#22 完了時点の §3.3 の件数は
  要追加 15 ／担保済み 11 ／対象外 1 ＝ 27。#23 とそのレビュー対応を経た現在は
  **要追加 0 ／担保済み 29 ／対象外 1 ＝ 30** である。§3.3 の合計行を参照）。

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
同じ方針である。**`XlsFormatWriterTest` 40 件との違いは「全件がそうか否か」である**（→ §3.1 末尾の内訳）。

**`XlsFormatWriterTest` の 40 件は #23 では 1 行も変えていなかったが、#23 のレビュー対応で変更した。**
変更は次の 3 点のみで、`@Test` の数・アサートの内容は変えていない
（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterTest.java` → **40** のまま）。

1. セル読み出しヘルパ `cell` / `line` の定義を削除し `XlsFixture` の同名 static メソッドを static import
   （理由と判断は `issues.md`「ヘルパ抽出の要否」）
2. 未使用になった import（`org.apache.poi.ss.usermodel.Cell` / `Row`）の削除
3. クラス Javadoc の訂正（`build` と `write` の内訳。§3.1 末尾）

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
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅（**誤り**。下記） | ✅ | `writesExpectedRequestBodyMessagesMarker` | 識別セル `EXPECTED_REQUEST_BODY_MESSAGES[case1]=RM21AA0104_01`。**入力の FW 制御ヘッダが空 Map のため**識別行の次は名前行。データ行の列 0 は**送信系のため**連番（`XlsDataTypeUtil.isSendSyncType` による分岐であり、`fwHeaderFields` が空であることとは無関係） |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅（**誤り**。下記） | ✅ | `writesResponseHeaderMessagesMarker` | 識別セル `RESPONSE_HEADER_MESSAGES[case1]=RM21AA0104_01`。同上 |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅（**誤り**。下記） | ✅ | `writesResponseBodyMessagesMarker` | 識別セル `RESPONSE_BODY_MESSAGES[case1]=RM21AA0104_01`。同上 |

**上表の「入力の FW 制御ヘッダが空 Map のため」は言い換えではなく、書ける事実の限界である。**
当初は 3 行とも「FW ヘッダ行なし」と書いていたが、これは「送信系だから出ない」と読める記述であり、
テストが確かめていない性質を観測事実として記録していた（2026-08-13・#23 レビュー ラウンド3 指摘）。
担保の穴として §3.1-3 末尾（下記「送信系の FW 制御ヘッダ」）と `issues.md` **XLS-24** に開示した。

**A-12／A-13／A-14 の ✅ は #18 以来（#23 の当初版を含め）誤りだった。**
この 3 タイプを辺③で通していたのは `XlsFormatWriterTest#writesSequenceNoForAllSendSyncTypes` だけで、
同メソッドがアサートするのは**データ行の列 0 の連番 `"1"`**（`cell(sheet, 4, 0)`。4 タイプ共通の値）であり、
**タイプを区別する出力を 1 つも固定していなかった**。辺③で識別セルを直接アサートしていたのは
`XlsFormatWriterTest#writesSendSyncMessageWithSequenceNo`（A-11 `EXPECTED_REQUEST_HEADER_MESSAGES` の識別セル）の 1 箇所だけである。

- **変異による実測（2026-08-13。#23 レビュー指摘の再現）**: `XlsFormatWriter#marker` が
  この 3 タイプにだけ別文字列 `"MUTATED"` を返すよう `src/main` を一時的に変異させて全件実行したところ、
  落ちたのは **`RoundTripTest` の 3 件のみ**（`xls_expectedRequestBodyMessages_isPreserved` ／
  `xls_responseHeaderMessages_isPreserved` ／ `xls_responseBodyMessages_isPreserved`）で、
  **`XlsFormatWriterTest` 40 件は全緑**だった（`Tests run: 425, Failures: 3`）。
  往復テストは steering Rules フェーズ2 により正式な担保に数えないため、
  **この時点で A-12／A-13／A-14 は 🔺 相当（正式担保 0）**であった。
- **埋め方**: 開示だけで済ませず、上表の 3 メソッドを追加した。粒度は A-11 の担保テスト（`writesSendSyncMessageWithSequenceNo`）に揃え、
  グループ ID と識別子を含むマーカー全体を固定する。
- **歯があることの実証**: 同じ変異を再度入れて全件実行し、`XlsFormatWriterModelTest` の該当 3 件が
  落ちることを確認した（`Tests run: 428, Failures: 6` ＝ 新規 3 件 ＋ `RoundTripTest` 3 件）。
  変異は確認後に戻し、`git diff HEAD -- src/main` が 0 行であることを確かめた。

**担保の穴: 「送信系は FW 制御ヘッダを書かない」は未担保である（2026-08-13・#23 レビュー ラウンド3 で判明）**

`XlsFormatWriter` のクラス Javadoc は「**送信系 4 種**: MESSAGE と同型だが FW 制御ヘッダは無く、
データ行の列 0 は `no`（連番）」と書いている。しかしこの「FW 制御ヘッダは無く」を担保するテストは
辺③に 1 件も無い。

- `XlsFormatWriter#layoutMessage` は `appendKeyValueRows(l, block.getFwHeaderFields())` を
  **データタイプで分岐せず無条件に**呼ぶ（同メソッドを読んだ結果）。送信系だから FW 制御ヘッダ行が
  出ないのではなく、テスト入力の `fwHeaderFields` が空 Map だから出ていないだけである。
- **変異による実測**: `layoutMessage` を「送信系のときだけ `appendKeyValueRows(l, block.getFwHeaderFields())`
  を呼ばない」——すなわち Javadoc が謳う性質を `src/main` に実装した形——へ一時的に変異させて全件実行したところ、
  **`Tests run: 428, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`** であった。
  1 件も落ちない＝ `src/test` に両者を区別するテストが存在しない。変異は確認後に戻し、
  `git diff HEAD -- src/main | wc -l` → **0** を確かめた。

  ```sh
  cd /home/tie303177/work/nablarch/nablarch-testing-converter
  # XlsFormatWriter#layoutMessage の
  #   appendKeyValueRows(l, block.getFwHeaderFields());
  #   boolean sendSync = XlsDataTypeUtil.isSendSyncType(block.getDataType());
  # を次へ置き換えて全件実行し、確認後 git checkout -- src/main で戻す
  #   boolean sendSync = XlsDataTypeUtil.isSendSyncType(block.getDataType());
  #   if (!sendSync) {
  #       appendKeyValueRows(l, block.getFwHeaderFields());
  #   }
  JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true
  ```

- **静的な裏取り**: 送信同期の `MessageDataBlock` に非空の `fwHeaderFields` を渡すテストは `src/test` に
  **0 件**である。`MessageDataBlock` の構築サイトを全数取り、第 1 引数（データタイプ）と第 5 引数
  （`fwHeaderFields`）の組を数える。

  ```sh
  cd /home/tie303177/work/nablarch/nablarch-testing-converter
  perl -0777 -ne '
    while (/(?:new\s+MessageDataBlock|\bmessage)\s*\(/g) {
      my $p = pos($_); my $d = 1; my @a = (""); my $i = $p;
      while ($d > 0 && $i < length($_)) {
        my $c = substr($_, $i, 1);
        if ($c eq "(") { $d++ } elsif ($c eq ")") { $d--; last if $d == 0 }
        if ($c eq "," && $d == 1) { push @a, "" } else { $a[-1] .= $c }
        $i++;
      }
      next unless @a >= 6;
      my ($t, $fw) = ($a[0], $a[4]);
      $t =~ s/\s+//g; $fw =~ s/\s+//g;
      print "$t\t$fw\n";
    }
  ' $(grep -rl "MessageDataBlock" src/test --include=*.java) | sort | uniq -c
  ```

  実測（2026-08-13）:

  ```
        2 DataType.EXPECTED_REQUEST_BODY_MESSAGES	fwHeader()
        1 DataType.EXPECTED_REQUEST_BODY_MESSAGES	newLinkedHashMap<>()
        4 DataType.EXPECTED_REQUEST_HEADER_MESSAGES	fwHeader()
        2 DataType.EXPECTED_REQUEST_HEADER_MESSAGES	map()
        1 DataType.MESSAGE	fwHeader
        2 DataType.MESSAGE	fwHeader("requestId","RM01","userId","${user}")
        2 DataType.MESSAGE	fwHeader("requestId","RM01","userId","${u}")
        5 DataType.MESSAGE	fwHeader()
        2 DataType.MESSAGE	map("requestId","${rid}")
        3 DataType.MESSAGE	map("requestId","R01")
        1 DataType.MESSAGE	newLinkedHashMap<>()
        2 DataType.RESPONSE_BODY_MESSAGES	fwHeader()
        2 DataType.RESPONSE_HEADER_MESSAGES	fwHeader()
        1 DataTypetype	String>directives
        1 dt	newLinkedHashMap<>()
        1 type	fwHeader
        1 type	fwHeader()
        2 type	map()
  ```

  **送信同期 4 種の行は 13 件あり、第 5 引数はすべて空 Map**（`fwHeader()` ／ `map()` ／
  `new LinkedHashMap<>()`）である。非空（引数付きの `fwHeader(...)` ／ `map(...)`）は
  **すべて `DataType.MESSAGE`** に限られる。

  末尾 5 行はデータタイプをリテラルで書いていない構築サイトで、このコマンドでは型が解決できない。
  それぞれ現物を開いて確かめた: `type` の 4 件は `fwHeader()` ／ `map()` ／呼び出し元から渡された
  `fwHeader` 変数（`RoundTripTest#message` ヘルパ。その呼び出し元は上表の `DataType.*` 行として
  解決済み）であり、`dt` の 1 件は送信同期 4 種を回す `MessageDataBlockTest` のループで
  `new LinkedHashMap<>()` を渡す。`DataTypetype` の 1 行はヘルパの**宣言**が引っ掛かったものである。

  **上の変異による実測のほうが上位の根拠**であり、静的な数え上げはその裏取りである
  （静的走査はデータタイプが変数で渡る経路を機械的には追えないが、変異は経路によらず効く）。

- したがって「送信系は FW 制御ヘッダを書かない」という性質は **未担保**である。
  `XlsFormatWriter` のクラス Javadoc の当該記述は、中間モデル側の契約
  （`MessageDataBlock` の Javadoc「`expected_request_*`／`response_*` 経路は空 Map とする（仕様 MS-04）」・
  `XlsFormatReader`／`YamlFormatReader`／`TestCoreReaderAdapter`／`YamlTestCoreAdapter` の
  「FW 制御ヘッダは送信系では常に空」）に依存した記述であり、`XlsFormatWriter` 自身が保証しているわけではない。
- **今回はテストを足さず開示のみ**とした（steering Rules フェーズ2「担保の穴は、テストを足さない場合でも
  台帳に開示する」）。課題としての記録は `issues.md` **XLS-24**。`src/main` は変更していない。

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

**§3.1 の表の C-10 タグの付け方に誤りが 2 つある（2026-08-13・#23 レビュー ラウンド3 の仕上げで判明。
§3.1 は #18 時点のスナップショットのため表そのものは書き換えない）。**

- **C-10(FIXED) の非往復担保は `XlsFormatWriterTest#writesFixedFileBlock` と
  `XlsFormatWriterModelTest#writesExpectedFixedFileBlockWithLengthRow` である。** §3.1 の #7
  `writesFixedFileBlock` の軸C 欄に `C-10` タグが欠けており、C-10(FIXED) を挙げているのは
  #36 `roundTripsFixedFile`（往復テスト）だけになっている。表の面だけを読むと A-12〜A-14 と同型の誤判定
  （往復でしか通っていないのに ✅）に見えるが、**実体は穴ではない**。両メソッドとも FIXED 固有の長さ行を
  直接アサートしており（前者は型行の次に `["", "-", "5"]`、後者は `["", "5"]`）、`layoutFile` の
  `block.getFileType() == FileType.FIXED` 分岐を非往復で固定している（両メソッドの本体を開いて確認した）。
- **#38 `roundTripsVariableFile` の `**C-10(VARIABLE)**` の太字は誤りである。** 凡例の太字は
  「その辺でその要素を通す唯一の担保」を意味するが、C-10(VARIABLE) は #8
  `writesVariableFileWithoutLengthRow`（非往復）にも付いており唯一ではない。

**軸E の 0 件（#18 は未担保 3。#23 完了後は 0）**

| 要素 | 状態 | 担保テストメソッド（`XlsFormatWriterModelTest#`） | 備考 |
|---|---|---|---|
| E-1(0 件) セクション内ブロック数 0 | ✅ | `writesEmptySheetWhenSectionHasNoBlocks` | C-04 と同じ入力 |
| E-2(0 件) ブロック内行数 0 | ✅ | `writesTableWithoutDataRowsWhenRowsAreEmpty`（テーブル経路）／`writesRecordWithoutDataRowsWhenRecordRowsAreEmpty`（ファイル経路の値行） | 順に C-09／C-18 と同じ入力 |
| E-3(0 件) ファイル内レコードレイアウト数 0 | ✅ | `writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty`（ファイル経路）／`writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty`（メッセージ経路） | 順に C-12／C-15 と同じ入力 |

**末尾 3 件は軸要素の担保に数えていない。** `#dropsDefaultDataTypeBlockWhenReadBack` ／
`#promotesFirstDataRowToColumnNamesWhenEmptyColumnNamesAreReadBack` ／ `#failsToReadBackRecordWithoutFields` は
書き出したブックを `XlsFormatReader` で読み戻し、`issues.md` XLS-20／XLS-21／XLS-22 の「読み戻すとどうなるか」を
実検査する。steering Rules フェーズ2（往復テストの扱い）に従い、辺③の担保としても
辺①の担保としても数えない。置く理由は #22 が `xl/sharedStrings.xml` の生バイト検査 2 件を置いたのと同じで、
本体パーサ・`PoiXlsReader` の挙動が変わったときに**担保テストは緑のまま `issues.md` の記述だけが誤りになる**
状態を防ぐためである。したがって **12 件（#23 当初の担保）＋ 3 件（#23 レビュー対応の送信同期の担保）
＋ 3 件（issues 検査）＝ 18 件**である。

**JaCoCo 実測（#23 完了後・2026-08-13）**: `XlsFormatWriter` は命令 **98%**（8 / 782 未到達）・
分岐 **97%**（3 / 100 未到達）・行 **1 / 151 未到達**（取得手順は steering の Decisions）。
未到達は次の 3 箇所のみで、いずれも #23 の軸要素ではない。

| 箇所 | 未到達の内容 | 扱い |
|---|---|---|
| `write` の `if (parent != null)` | `null` 側の分岐（1 / 2） | 既知の担保の穴。到達経路の全数調査は [§3.1-2 の該当項](#s3-1-2-parent-null) |
| `layout` の `else if (block instanceof MessageDataBlock)` の false 側と直後の `throw` | 未知のブロック実装（1 / 2 分岐・1 行） | sealed 階層が permit する 3 種すべてを本節と §3.1 が通しているため到達不能。Java イディオムとしての安全網（steering #6 の判断と同じ思想） |
| `isMarkerColumn` の `columnName != null` | `null` 側の分岐（1 / 6） | steering #9 でコメント済みの防御ガード。`layoutColumnRow` のコメントが「カラム名が `null` の場合は…非マーカーとして扱う」と明記している |

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
| B | （なし。ただし**辺③では B-1 `TableDataBlock` と B-2 `ListMapBlock` のコード経路が同一**であり、軸B は軸A から独立していない。テストを足しても通る `src/main` の経路は増えないため件数は 0 のままとする。`XlsFormatWriter#layout` が `ColumnRowDataBlock` ／ `FileDataBlock` ／ `MessageDataBlock` の 3 分岐しか持たず、`TableDataBlock` と `ListMapBlock` はどちらも `layoutColumnRow` を通るためである。版面上で両者を分けるのは `layoutColumnRow` が `getDataType()` から作る識別セルだけで、それは軸A そのものである） | — | — | — | 0 |
| C | C-02 sections 空（writer 側は到達可能。§0.8-6）／C-04 blocks 空／C-08 columnNames 空／C-09 rows 空／C-12 FileDataBlock.records 空／**C-13 MessageDataBlock.directives 値あり**／C-15 MessageDataBlock.records 空／C-17 fields 空／C-18 RecordLayout.rows 空 | 要追加 | 要追加（#23。#22 の対象外） | **担保済み（#23）** — 順に `XlsFormatWriterModelTest#writesWorkbookWithoutSheetsWhenContainerHasNoSections`／`#writesEmptySheetWhenSectionHasNoBlocks`／`#writesEmptyHeaderRowWhenColumnNamesAreEmpty`／`#writesTableWithoutDataRowsWhenRowsAreEmpty`／`#writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty`／`#writesDirectiveRowsBeforeFwHeaderRowsInMessage`／`#writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty`／`#writesRecordWithoutFieldColumnsWhenFieldsAreEmpty`／`#writesRecordWithoutDataRowsWhenRecordRowsAreEmpty`（§3.1-3）。記録した課題は `issues.md` **XLS-21〜XLS-23** | 9 |
| D | D3-01〜D3-08 全 8 ケース（D3-04／D3-05 は値のみの 🔺。`getCellType()` をアサートするテストは全件ゼロ） | 要追加 | **担保済み（#22）** — `XlsFormatWriterCellTypeTest` 18 件（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterCellTypeTest.java` → 18）。内訳は **8 ケース**＋改行の異表記 **2 件**・上限ちょうど **1 件**・XML で表現できない制御文字を 1 文字 1 メソッドへ展開した増分 **3 件**・XML で正当な制御文字の対照 **2 件**（ここまで 16 件。読み戻したセル型と値を突き合わせる分）＋ `xl/sharedStrings.xml` の**生バイト**を検査する **2 件**（`burnsQuestionMarkIntoSharedStringsXmlForControlCharacter`／`keepsCarriageReturnRawInSharedStringsXml`。第 3 ラウンドで追加）＝ 8＋2＋1＋3＋2＋2 ＝ **18**。要素別の担保テストメソッドは §3.1-2 の軸D 表。記録した課題は `issues.md` **XLS-17〜XLS-19** | 担保済み（変更なし） | 8 |
| E | E-1(0 件)／E-2(0 件)／E-3(0 件) | 要追加 | 要追加（#23。#22 の対象外） | **担保済み（#23）** — E-1(0) は C-04、E-2(0) は C-09／C-18、E-3(0) は C-12／C-15 と同じ入力（上の C 行のテストメソッド。§3.1-3 の軸E 表） | 3 |
| F | F3-01 出力先不在（🔺 のみ）／F3-03 書き込み権限なし／F3-04 シート名制約違反 | 要追加 | **担保済み（#22）** — `XlsFormatWriterInvalidOutputTest` 16 件（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterInvalidOutputTest.java` → 16）。内訳は F3-01 **1 件**（`createsMissingOutputDirectoriesAndWritesWorkbook`）・F3-03 **1 件**（`wrapsAccessDeniedExceptionWhenOutputDirectoryIsNotWritable`）・F3-04 **14 件**（禁止文字を 1 文字 1 メソッドへ展開した **7 件**＋空文字 **1 件**＋31 文字ちょうど **1 件**＋31 文字超の黙った切り詰め **1 件**＋切り詰めが禁止文字検査を無効化する境界 **2 件**＋切り詰め後の衝突 **1 件**＋大文字小文字だけが違う名前の衝突 **1 件**（`failsWhenSheetNamesDifferOnlyInCase`。第 3 ラウンドで追加）＝ 7＋1＋1＋1＋2＋1＋1 ＝ 14。メソッド名の全列挙は §3.1-2 の軸F 表）＝ 1＋1＋14 ＝ **16**。要素別の担保テストメソッドは §3.1-2 の軸F 表。記録した課題は `issues.md` **XLS-16** | 担保済み（変更なし） | 3 |
| F | F3-02 `overwrite=false` 衝突 — `XlsFormatWriter` は `overwrite` を保持しない。衝突検査は上位層の `TestDataConverter#checkOverwrite` で完結する。上に挙げた既存テストが通すのは XLS→YAML の経路であり、**`.xlsx` を出力側とする衝突は未担保**（§0.8-5 の訂正） | 対象外（衝突検査は上位層） | 対象外（変更なし。#22 でも辺③に書かない） | 対象外（変更なし。#23 でも辺③に書かない） | 1 |
| **合計** | | **要追加 26（実際は 29）／ 到達不能 0 ／ 対象外 1** | **要追加 15（実際は 18）／ 担保済み 11 ／ 到達不能 0 ／ 対象外 1** | **要追加 0 ／ 担保済み 29 ／ 到達不能 0 ／ 対象外 1** | **30（うち対象外 1）** |

**合計の検算**（表の「件数」列を上から順に足す）:

- 担保済み: A 3 ＋ A 3（送信同期。#23 レビュー対応）＋ C 9 ＋ D 8 ＋ E 3 ＋ F 3 ＝ **29**
- 要追加: **0**
- 対象外: F3-02 **1**
- 総計: 29 ＋ 0 ＋ 1 ＝ **30**（B は 0 件）

「#18」「#22 後」の列に括弧で添えた「実際は」は、A-12〜A-14 が当時から未担保だったことを
遡って数え直した値である（当時の表には行として存在しなかった）。

**軸要素の外に、開示すべき担保の穴が 1 つある。** `XlsFormatWriter#write` の `parent == null` 分岐は
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

<a id="s4-3"></a>

### 4.3 辺④ 未担保一覧（#25 が埋める対象）

計上単位と「状態」の 3 分類は §1.3 の規則に従う。

| 軸 | 未担保要素 | 状態 | 件数 |
|---|---|---|---|
| A | A-07 `EXPECTED_FIXED`（🔺 `RoundTripTest#yaml_expectedFixed_isPreserved`）／A-08 `SETUP_VARIABLE`（🔺 `RoundTripTest#yaml_setupVariable_isPreserved`） | 要追加 | 2 |
| B | （なし） | — | 0 |
| C | C-02 sections 空・複数（writer 側は到達可能。§0.8-6）／C-12 FileDataBlock.records 空 | 要追加 | 2 |
| D | D4-01 `"100"`（記法アサートなしの 🔺）／D4-02 `"true"`／D4-06 `"007"`／D4-08 `"2026-08-07"`／D4-09 値側のコロン・ハイフン・`#` | 要追加 | 5 |
| E | E-4(複数) — `YamlFormatWriter#write` が sections をループするため到達可能（§0.8-6） | 要追加 | 1 |
| F | F4-01 出力先不在（🔺 のみ）／F4-03 書き込み権限なし | 要追加 | 2 |
| F | F4-02 `overwrite=false` 衝突 — `YamlFormatWriter` は `overwrite` を保持しない。`TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse`／`ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict` で担保済み（§0.8-5） | 対象外（上位層で担保済み） | 1 |
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
辺①はその後 #19／#20／#21 で担保済みになった（§1.3 の「#21 後の状態」列）。
辺①の最新の状態別内訳は **要追加 0 ／ 担保済み 24 ／ 到達不能 8 ／ 対象外 0** であり、
下の表の辺①列（要追加 38 ／ 到達不能 3）は #18 時点の値である。
**下の 2 表の辺①の軸D も #18 時点の定義（17 ケース）による。** 軸D 辺① は 2026-08-13 のユーザー確定で
**8 ケース**へ絞り込まれた（NTF が実行できるテストデータ ＝ 全セル文字列書式のみを対象とする。§0.5）。
補正を当てると辺① 軸D は「17」ではなく **8**、辺①の合計は「41」ではなく **32**、
全体の合計は（辺③の補正 +3 と合わせて）「107」ではなく **101**、
状態別の辺①「要追加 38」は **29** が正しい。
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
辺②は #24 が軸C 5 件・軸D 12 件・軸E 1 件・軸F 4 件を埋めたため最新は
**要追加 0 ／ 担保済み 22 ／ 到達不能 6 ／ 対象外 0**（§2.3）であり、
下の表の辺②列（要追加 23 ／ 到達不能 3）は #18 時点の値である。
**下の 2 表の辺②の軸D も #18 時点の定義（10 ケース）による。** 軸D 辺② は 2026-08-14 のユーザー確定で
**12 ケース**になった（§0.5）。補正を当てると辺② 軸D は「10」ではなく **12**、
辺②の合計は「26」ではなく **28**、状態別の辺②「要追加 23」は **25** が正しい。
辺④は #25 が未着手のため #18 時点のまま。

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
**辺②列は #24 で判定そのものは変わっていない（13/14 のまま）が、担保の経路が変わった。**
#18 時点は in-memory 経路（`YamlFormatReaderTest`）だけだったのに対し、#24 で
`YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` が実 `.yaml` 経路でも
13 種すべてを通した（§2.1-2）。辺④は #25 が未着手のため #18 時点から変わっていない。

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
| 1 | 辺①・辺② の `DataType.DEFAULT` | **到達不能**。辺①は `TestCoreReaderAdapter`、辺②は `YamlFormatReader` の分岐に `DEFAULT` を返す経路がないこと（§0.8-7）。#20/#24 では理由付きで空欄に残す |
| 2 | E-4「コンテナ内セクション数 複数」／C-02「sections 空・複数」 | 辺①・辺②は **到達不能**（`read` が `Collections.singletonList(section)` を返す 1 リソース単位 API）。辺③・辺④は writer が `container.getSections()` をループするため **要追加**（§0.8-6） |
| 3 | 辺③／辺④ の `overwrite=false` 衝突（F3-02 / F4-02） | **対象外（衝突検査は上位層）**。`overwrite` を保持するのは `ConversionRequest` / `TestDataConverter` / `ConverterMojo` で writer は保持しない。既存テスト `TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse` と `ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict` が `TestDataConverter#checkOverwrite` を通す。ただし両者とも XLS→YAML であり、**辺④（`.yaml` 出力）は担保されるが辺③（`.xlsx` 出力）は未担保**である（§0.8-5 の訂正） |
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
   **#24 の分は解決済み（2026-08-14）**: steering #24 の Completion criteria が
   「軸A の 13 種（`DEFAULT` を除く。… `DEFAULT` を生成しないため到達不能。根拠付きで空欄に残す）」と
   明記され、`sections` 空・E-4(複数) も「到達不能として根拠付きで空欄」と明記された。
   本書 §2.1-2 / §2.3 はこの読み替えどおりに記している。辺①（steering #21）側は未解決のまま。
3. **辺③ A-01 `DEFAULT` の扱い**: ~~writer 側は `XlsFormatWriter#marker` がマーカー文字列を
   `getDataType().getName()` から組み立てるだけなので `DEFAULT` ブロックも書けてしまう（＝到達可能）。
   辺④の `serialize_unsupportedDataType_throws` は `IllegalArgumentException` を投げる挙動を固定しているが、
   辺③に同等のガードはない。#23 で現状挙動を記録して固定する対象としてよいか
   （挙動が仕様として不適切なら `issues.md` 行き）。~~
   **解決済み（#23・2026-08-13）**: steering #23 の Steps が「現状の挙動をまず実行して記録してから固定する。
   辺③④の非対称を `issues.md` に課題として記録する（修正しない）」と指示しているため、そのとおり実施した。
   固定した挙動は `XlsFormatWriterModelTest#writesDefaultDataTypeMarker`（識別セル `DEFAULT=T` が書かれる）、
   課題は `issues.md` **XLS-20**。あわせて「辺③が書いたブロックは辺①で読み戻すと黙って消える」ことも
   実測して `#dropsDefaultDataTypeBlockWhenReadBack` で固定した。
