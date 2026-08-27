# #37 Completion Check

指示書「完了条件3」（`nablarch-document@a16be0a` の `.rn/20260724-ntf-yaml-support/ntf-step4-05-nablarch-testing-converter.md`）。
参照点: 解説書 `nablarch-document@5783b35` ／ `nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@0b3015c`。

**レビュア subagent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| 4 経路それぞれの合否が、表の行ごと・記載例ごとに全件挙がっている | OK | 下表 2 つ。**母集合 13 ＋ 記載例 7 ＝ 20 件 × 4 経路 ＝ 80 経路すべて合格** |
| 起点が実ファイルであり、比較がテスティングフレームワークの解釈後の値である（セルの見た目ではない） | OK | 起点は POI で組んだ実 `.xlsx`（`XlsFixture`）と `Files.write` で書いた実 `.yaml`。比較は中間モデルへ入った値（`XlsFormatReader` が 3 インタープリタを掛けたあとの Java `null` または `String`）。**版面のセル文字列は一切アサートしていない** |
| 一致しないものが `@Ignore` ＋ 印つきの理由で記録されている | OK（**該当 0 件**） | 80 経路すべて合格したため `@Ignore` は 1 件も無い。`mvn -o clean test` の `Skipped` は 0 |
| #32 の `XlsNotationSymmetryTest`（8 件）を軸要素対応表へ載せるかを判断し、載せる場合は載せる | OK | **載せない**と判断した。#33 の `XlsEmptyEntryTest`（12 件）と本タスクの `SpecialNotationRoundTripTest`（20 件）も同じ扱い。理由と開示は `coverage/inventory.md` §4.5（新設）と `coverage/axis-matrix.md` §0.5 の追記 |

## ゲート（Rules「#30 以降」）

- `git diff --stat 4ffdf81..作業ツリー -- src/`: **既存ファイルの変更は 0 件。**新規 1 ファイル
  `src/test/.../xls/SpecialNotationRoundTripTest.java`（20 メソッド）だけである。

- `mvn -o clean test` の最終行:

  ```
  Tests run: 645, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS
  ```

  625 件 → **645 件**（+20。すべて新規）。

## 母集合

| 出典 | 件数 |
|---|---|
| `implementation/testdata_notation.rst` の特殊記法の表（Excel 形式 `:1356`-`:1391` ／ YAML 形式 `:1408`-`:1443`） | **12 行**。2 つの表は同じ 12 の「値の種類」を同じ順で並べており、行ごとに Excel 記法と YAML 記法が対応する。**改行文字の行だけは CR と LF の 2 形を含むため 2 つに分けて測った** → **13 件** |
| `implementation/testdata_examples.rst` の「null・空文字・改行など特殊な値を記述する」（`:2133`-`:2461`） | **6 小節**。各小節が Excel 形式の例と YAML 形式の例を 1 つずつ持つ。**「空文字・改行を記述する」だけは YAML 側に LF の追加例（`:2284`-`:2289`）と全フィールド空文字のレコードの例（`:2293`-`:2308`）があるため 3 つに分けて測った** → **7 件** |

節の範囲（`:2133`-`:2461`）は `awk '/^-----/ {print NR-1": "prev} {prev=$0}'` で `----` 見出しの
境界を取って確かめた（次の `----` 見出しは `:2462`「コメント・マーカーカラム・空エントリを扱う」）。

## 4 経路の合否 —— `testdata_notation.rst` の表（13 件）

いずれも `SpecialNotationRoundTripTest` のメソッド。**XLS→XLS ／ XLS→YAML→XLS は Excel 記法を、
YAML→YAML ／ YAML→XLS→YAML は YAML 記法を起点にする。**

| # | 値の種類（表の行） | Excel 記法 | YAML 記法 | 解釈後の値 | XLS→XLS | XLS→YAML→XLS | YAML→YAML | YAML→XLS→YAML | メソッド |
|---|---|---|---|---|:-:|:-:|:-:|:-:|---|
| 1 | 通常の文字列（`:1356`／`:1408`） | `abc` | `"abc"` | `abc` | ✅ | ✅ | ✅ | ✅ | `plainString` |
| 2 | null（`:1359`／`:1411`） | `null` | `null` | Java `null` | ✅ | ✅ | ✅ | ✅ | `javaNull` |
| 3 | 文字列の null（`:1362`／`:1414`） | `"null"` | `"null"` | `null`（文字列） | ✅ | ✅ | ✅ | ✅ | `stringNull` |
| 4 | 空文字（`:1365`／`:1417`） | 空セル | `""` | `""` | ✅ | ✅ | ✅ | ✅ | `emptyString` |
| 5 | 先頭ゼロ付き数値（`:1368`／`:1420`） | `001` | `"001"` | `001` | ✅ | ✅ | ✅ | ✅ | `leadingZeroNumber` |
| 6 | `true`（文字列）（`:1371`／`:1423`） | `true` | `"true"` | `true` | ✅ | ✅ | ✅ | ✅ | `booleanLikeString` |
| 7 | 半角スペース 1 文字（`:1374`／`:1426`） | `" "` | `" "` | ` ` | ✅ | ✅ | ✅ | ✅ | `singleHalfWidthSpace` |
| 8 | ダブルクォート 1 文字（`:1377`／`:1429`） | `"""` | `'"'` | `"` | ✅ | ✅ | ✅ | ✅ | `singleDoubleQuote` |
| 9 | `${systemTime}`（`:1380`／`:1432`） | `${systemTime}` | `"${systemTime}"` | `${systemTime}` | ✅ | ✅ | ✅ | ✅ | `systemTimeNotation` |
| 10 | `${binaryFile:パス}`（`:1383`／`:1435`） | `${binaryFile:testdata.bin}` | `"${binaryFile:testdata.bin}"` | 同左 | ✅ | ✅ | ✅ | ✅ | `binaryFileNotation` |
| 11 | `${文字種,文字数}`（`:1386`／`:1438`） | `${半角英字,10}` | `"${半角英字,10}"` | 同左 | ✅ | ✅ | ✅ | ✅ | `charTypeNotation` |
| 12a | 改行文字 CR（`:1389`／`:1441`） | `a\rb`（2 文字の `\`＋`r`） | `"a\rb"` | `a` ＋ CR ＋ `b` | ✅ | ✅ | ✅ | ✅ | `carriageReturn` |
| 12b | 改行文字 LF（同上） | セル内の改行 | `"a\nb"` | `a` ＋ LF ＋ `b` | ✅ | ✅ | ✅ | ✅ | `lineFeed` |

**13 行 × 4 経路 ＝ 52 経路すべて合格。**加えて各行で
**「Excel 記法の解釈後の値 ＝ YAML 記法の解釈後の値 ＝ 解説書が定める値」**もアサートしている
（`tools/testdata_converter.rst:14`「同じ意味をそれぞれの記法で表したもの」）。

## 4 経路の合否 —— `testdata_examples.rst` の記載例（7 件）

| # | 記載例（小節） | Excel 形式 | YAML 形式 | XLS→XLS | XLS→YAML→XLS | YAML→YAML | YAML→XLS→YAML | メソッド |
|---|---|---|---|:-:|:-:|:-:|:-:|---|
| 1 | 日付・システム日時・NULL | `:2149`-`:2176` | `:2184`-`:2202` | ✅ | ✅ | ✅ | ✅ | `exampleDateSystemTimeAndNull` |
| 2 | 空文字・改行（テーブル） | `:2212`-`:2227` | `:2270`-`:2278` | ✅ | ✅ | ✅ | ✅ | `exampleEmptyStringAndLineBreak` |
| 3 | 同上（LF の追加例） | セル内改行（`:2229`） | `:2284`-`:2289` | ✅ | ✅ | ✅ | ✅ | `exampleLineFeedInBody` |
| 4 | 全フィールドが空文字のレコード | `:2233`-`:2260` | `:2293`-`:2308` | ✅ | ✅ | ✅ | ✅ | `exampleAllEmptyRecordInVariableFile` |
| 5 | スペース・ダブルクォート | `:2320`-`:2332` | `:2340`-`:2345` | ✅ | ✅ | ✅ | ✅ | `exampleSpaceAndDoubleQuote` |
| 6 | バイナリデータ | `:2355`-`:2366` | `:2376`-`:2382` | ✅ | ✅ | ✅ | ✅ | `exampleBinaryData` |
| 7 | 文字列の増幅 | `:2392`-`:2407` | `:2415`-`:2421` | ✅ | ✅ | ✅ | ✅ | `exampleAmplifiedString` |

**7 件 × 4 経路 ＝ 28 経路すべて合格。**加えて各記載例で
**「Excel 形式の例と YAML 形式の例が同じ値を表す」**もアサートしている（7 件とも一致した）。

**`アップロードファイルを指定する`（`:2423`-`:2461`）は母集合に含めていない。** 同小節の対象は
`${attach:ファイルパス}` であり、特殊記法の表（12 行）に無く、`LIST_MAP` のリクエストパラメータの
書き方の例だからである。**追わなかったことをここに開示する。**

## 変異（期待値をわざと崩すと落ちること）

9 件すべて FAIL を確認した（`mvn -o clean test -Dtest=SpecialNotationRoundTripTest#<メソッド>`）。
**4 経路それぞれと、解釈後の値・形式間の一致・記載例の突き合わせを 1 つずつ崩した。**

| 崩した箇所 | 崩した内容 | 実行したメソッド | 結果 |
|---|---|---|---|
| 経路 1 の判定 | `readXls(dir(2))` の期待に `"!"` を足す | `plainString` | FAIL |
| 経路 2 の判定 | `readXls(dir(4))` の期待に `"!"` を足す | `carriageReturn` | FAIL |
| 経路 3 の判定 | `readYaml(dir(6))` の期待に `"!"` を足す | `singleDoubleQuote` | FAIL |
| 経路 4 の判定 | `readYaml(dir(8))` の期待に `"!"` を足す | `emptyString` | FAIL |
| Excel 記法の解釈後の値 | 期待を反転（`null` ⇄ 非 `null`） | `javaNull` | FAIL |
| YAML 記法の解釈後の値 | 同上 | `stringNull` | FAIL |
| 形式間の一致 | `fromXls` と `fromYaml` の比較に `"!"` を足す | `lineFeed` | FAIL |
| 記載例の往復 | YAML 側フィクスチャの `["", "", ""]` を `["", "", "z"]` へ | `exampleAllEmptyRecordInVariableFile` | FAIL |
| 記載例の形式間一致 | Excel 側フィクスチャの `NULL` を `"NULL"` へ | `exampleDateSystemTimeAndNull` | FAIL |

**「記載例の往復」の 1 件目の試みは緑のままだった。** `fromXls` と `fromYaml` を入れ替えても
両者が完全に一致するため崩しにならなかったからである。**これは記載例の突き合わせが形式間の
一致を見ていなかったことの現れ**であり、`assertExampleFourRoutes` に
「Excel 形式の例と YAML 形式の例が同じ値を表す」アサートを足したうえで崩し直した。

## 台帳の更新

| 文書 | 更新 |
|---|---|
| `coverage/inventory.md` | **§4.5 を新設** —— Step 4 が新設した 3 クラス（`XlsNotationSymmetryTest` 8 件／`XlsEmptyEntryTest` 12 件／`SpecialNotationRoundTripTest` 20 件）を件数・導出コマンド・担保内容つきで開示し、軸要素対応表へ載せない判断とその理由を書いた |
| `coverage/axis-matrix.md` | §0.5（往復テストの扱い）に、上の 3 クラスを 🔺 欄の 3 群に加えていないこと、本書の 🔺 欄と導出コマンドの結果がこの 3 クラスを含まないことを追記 |

### 軸要素対応表へ載せないと判断した理由

steering Rules（フェーズ2）は「**各辺の担保を往復テスト（`RoundTripTest`）の追加で代替しない**。
ただし**既存**の往復テストが実ファイル経由で通している軸要素は 🔺弱い担保として必ず計上する」と定める。
3 クラスはいずれも**新設した往復テスト**であり、正式担保として数えるとこの規定に反する。
3 クラスが触れる軸要素（主に **D 値の表現**、次いで **C** の `rows` ／ `columnNames`）は
各辺の直接テストで既に✅であるため、🔺 欄へ足しても状態欄は動かず、**新しく埋まる穴は無い**。
したがって `inventory.md` §4.5 に開示するに留めた。

## Overall Verdict

- Self-check: OK
- QA: N/A（Rules「#30 以降、レビュア subagent は回さない」）
- Design expert: N/A（同上）
- Craft expert: N/A（同上）
- Verification expert: N/A（同上）
- Ready to check off: Yes（`mvn -o clean test` が全件緑・`BUILD SUCCESS`。`@Ignore` は 0 件）
