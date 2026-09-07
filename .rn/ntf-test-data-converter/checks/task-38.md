# #38 Completion Check

指示書 第3節（`nablarch-document@a16be0a` の `.rn/20260724-ntf-yaml-support/ntf-step4-05-nablarch-testing-converter.md`）。
参照点: 解説書 `nablarch-document@5783b35` ／ `nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@0b3015c`。

**レビュア subagent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| 11 件すべてについてテストが存在する | OK | 下表。新規 2 クラス（`xls/ExcelOutputDocumentedBehaviorTest` 6 件／`ConverterDocumentedBehaviorTest` 5 件）＝ 11 件 |
| 通ったもの・`@Ignore` にしたものの内訳が挙がっている | OK | **通った 11 件／`@Ignore` 0 件。** `mvn -o clean test` の `Skipped` は 0 |
| `@Ignore` の理由が印つき（`NTF-DOC:` ＋ 解説書の `file:line` ＋ 期待／実際）である | N/A（**該当 0 件**） | 落ちたものが無いため `@Ignore` を 1 件も置いていない |
| 負のテスト 4 件について、崩すと落ちることを確認した記録がある | OK | 下表「変異」。**負のテスト 4 件（3-2・3-3・3-4・3-11）はそれぞれ 2〜3 か所を崩して確認した**（計 9 件）。全 11 件では 18 件の変異を確認した |

## ゲート（Rules「#30 以降」）

- `git diff --stat 1ba9dfc..作業ツリー -- src/`: **既存ファイルの変更は 0 件。**新規 2 ファイルだけである。

  ```
  src/test/.../xls/ExcelOutputDocumentedBehaviorTest.java   （6 メソッド）
  src/test/.../ConverterDocumentedBehaviorTest.java         （5 メソッド）
  ```

- `mvn -o clean test` の最終行:

  ```
  Tests run: 656, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS
  ```

  645 件 → **656 件**（+11。すべて新規）。

## 11 件の内訳

| # | 解説書 | 内容 | 結果 | テスト |
|---|---|---|:-:|---|
| 3-1 | `:53`-`:55` | `YamlTestDataValidator` が報告する種類の不正な YAML を変換元にしても、`convert` が検証を理由に失敗しない（**負**） | ✅ | `ConverterDocumentedBehaviorTest#convertDoesNotRunValidation` |
| 3-2 | `:59` | セルの背景色・表示形式・結合セルを設定した Excel を xls→xls で往復させると、往復後にその色・書式・結合が無い（**負**） | ✅ | `ExcelOutputDocumentedBehaviorTest#dropsCellColorFormatAndMergeOnRoundTrip` |
| 3-3 | `:59` | コメント行を含む YAML を yaml→yaml で往復させると、往復後にコメントが無い（**負**） | ✅ | `ConverterDocumentedBehaviorTest#dropsYamlCommentsOnRoundTrip` |
| 3-4 | `:176` | 変換元が YAML のとき `excludeSheets` を指定しても、変換件数と出力内容が指定しないときと一致する（**負**） | ✅ | `ConverterDocumentedBehaviorTest#ignoresExcludeSheetsWhenSourceIsYaml` |
| 3-5 | `:233` | 直下とサブディレクトリの両方に不正な YAML を置いて `validate` すると直下のぶんだけ返る。直下に `.yaml` を持たない上位ディレクトリは空リスト | ✅ | `ConverterDocumentedBehaviorTest#validateLooksOnlyAtDirectChildren` |
| 3-6 | `:251`-`:254` | `withTestShotsHeaderColor(x)` で `testShots` の `LIST_MAP` のヘッダ行が `x` になる | ✅ | `ExcelOutputDocumentedBehaviorTest#appliesConfiguredTestShotsHeaderColor` |
| 3-7 | `:259`-`:262` | `withExpectedHeaderColor(x)` で `EXPECTED_` 始まりと `RESPONSE_` 始まりのヘッダ行が**どちらも** `x` になる | ✅ | `ExcelOutputDocumentedBehaviorTest#appliesConfiguredExpectedHeaderColorToBothExpectedAndResponse` |
| 3-8 | `:263`-`:266` | `withOtherHeaderColor(x)` で `MESSAGE` と `testShots` 以外の `LIST_MAP` が `x` になり、`testShots` は変わらない | ✅ | `ExcelOutputDocumentedBehaviorTest#appliesConfiguredOtherHeaderColorExceptTestShots` |
| 3-9 | `:275`-`:278` | `withMaxColumnWidthChars(n)` が効く。既定 20 に対し 30 文字の値を持つ列が 20 文字相当で頭打ちになる | ✅ | `ExcelOutputDocumentedBehaviorTest#limitsColumnWidthByMaxColumnWidthChars` |
| 3-10 | `:287`-`:290` | `withDisplayGridlines(true)` でグリッド線表示がオン、既定ではオフ | ✅ | `ExcelOutputDocumentedBehaviorTest#togglesDisplayGridlines` |
| 3-11 | `:239` | `ExcelFormatConfig` を設定した `ConversionRequest` で `to=yaml` を実行しても、出力 YAML の中身が設定なしと一致する（**負**） | ✅ | `ConverterDocumentedBehaviorTest#excelFormatConfigDoesNotAffectYamlOutput` |

**11 件すべて緑。`@Ignore` は 0 件。**

## 3-1 で分かったこと（指示書の想定より狭い）

**`YamlTestDataValidator` の検査 7 項目のうち、変換を通るのは一部である。**
ただし**止めているのはいずれも `YamlTestDataValidator` ではなく上流**であり、
`tools/testdata_converter.rst:53`-`:55`（「検証は変換の処理経路には組み込まれておらず……」）と矛盾しない。
実測（2026-08-28）は次のとおり。

| 検査項目 | 変換元にしたときの結果 | 止めているもの |
|---|---|---|
| **V-COL** フィールド数と要素数の不一致 | **変換できる**（余りの値は黙って捨てられる。`issues.md` **YML-14**） | —— |
| **V-DKEY** 未知のディレクティブ名 | 変換できない。`YamlSchemaValidationException: $.setup_files[0].directives: プロパティ 'no-such-directive' がスキーマで定義されておらず、スキーマでは追加のプロパティが許可されていません` | 上流ローダの**スキーマ検証**（未知のディレクティブ名はスキーマ非適合でもある） |
| **V-FNAME** 同一レコード内のフィールド名重複 | 変換できない。`IllegalArgumentException: Duplicate field names are not permitted in a record. duplicate field=[col1] . file=[test.dat]` | 本体（`nablarch-testing`）の**レコード解析** |

**そのため 3-1 のテストは V-COL 1 項目で押さえている。**当初 3 項目（V-COL・V-FNAME・V-DKEY）を
1 つの入力へ詰めて書いたが上の 2 つで落ちたため、**実測に合わせて V-COL だけへ絞った。**
この経緯と 2 つの例外はテストの Javadoc にも書いた。**指示書の「3-2〜3-5 で……報告する種類の
不正な YAML」という書き方は、7 項目のうちどれを指すかが曖昧であり、実際には項目によって結果が違う。**

## 変異（期待値をわざと崩すと落ちること）

18 件すべて FAIL を確認した（`mvn -o clean test -Dtest=<クラス>#<メソッド>`）。
**負のテスト 4 件（3-2・3-3・3-4・3-11）は 2〜3 か所ずつ崩した。**

| # | 崩した箇所 | 崩した内容 | 結果 |
|---|---|---|---|
| 3-1 | 検証が報告すること | `[V-COL]` を `[V-NONE]` へ | FAIL |
| 3-1 | 変換が成功すること | 件数の期待を 1 → 0 | FAIL |
| **3-2** | 色が残らないこと | `not(RED)` を `is(RED)` へ | FAIL |
| **3-2** | 結合が残らないこと | 期待を 0 → 1 へ | FAIL |
| **3-2** | 書式が残らないこと | 期待を `General` → `0.00` へ | FAIL |
| **3-3** | コメントが消えること | `not(containsString(...))` を `containsString(...)` へ | FAIL |
| **3-3** | 値は残ること | `containsString("A: \"1\"")` を `not(...)` へ | FAIL |
| **3-4** | 件数が一致すること | 期待を `plain` → `plain + 1` へ | FAIL |
| **3-4** | 内容が一致すること | 期待の末尾に `"x"` を足す | FAIL |
| 3-5 | 直下だけを見ること | 期待を 1 → 2 へ | FAIL |
| 3-5 | 上位が空リストであること | 期待を空リスト → `direct` へ | FAIL |
| 3-6 | 設定した色が効くこと | 期待を `CUSTOM` → 既定値へ | FAIL |
| 3-7 | `RESPONSE_` にも効くこと | 期待を `CUSTOM` → 既定値へ | FAIL |
| 3-8 | `testShots` は変わらないこと | 期待を既定値 → `CUSTOM` へ | FAIL |
| 3-9 | 上限で頭打ちになること | 期待を `20 * 256` → `32 * 256`（30 ＋ 余白 2）へ | FAIL |
| 3-10 | 既定がオフであること | 期待を `false` → `true` へ | FAIL |
| **3-11** | 内容が一致すること | 期待の末尾に `"x"` を足す | FAIL |
| **3-11** | 余計な出力が無いこと | 期待を `td.yaml` → `td.xlsx` へ | FAIL |

**負のテストが「ただ通っているだけ」でないことの追加の裏づけ。** 3-2 は同じ往復で
**値が保たれること**（`["1", "2"]`）も同時にアサートしており、色・書式・結合だけが落ちていることを示す。
3-3 は**値が保たれること**（`A: "1"` が出力に在ること）を同時にアサートする。
3-4・3-11 は**設定なしの出力と全文一致**を見るので、両方が壊れて同じになる形では通らない。

## 台帳の更新

| 文書 | 更新 |
|---|---|
| `coverage/inventory.md` | §4.5 へ #38 の 2 クラスを追加（件数・導出コマンド・担保内容）。「#38 の 2 クラスはそもそも軸の対象外である」旨を追記 |
| `coverage/axis-matrix.md` | §0.5 の追記へ、#38 の 2 クラスも 🔺 欄に含めないことを追記 |

## Overall Verdict

- Self-check: OK
- QA: N/A（Rules「#30 以降、レビュア subagent は回さない」）
- Design expert: N/A（同上）
- Craft expert: N/A（同上）
- Verification expert: N/A（同上）
- Ready to check off: Yes（`mvn -o clean test` が全件緑・`BUILD SUCCESS`。`@Ignore` は 0 件）
