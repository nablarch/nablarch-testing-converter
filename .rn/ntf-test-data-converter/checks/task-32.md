# #32 Completion Check

指示書 2-1（`nablarch-document@0d9a049` の `.rn/20260724-ntf-yaml-support/ntf-step4-05-nablarch-testing-converter.md`）。
参照点: 解説書 `nablarch-document@5783b35` ／ `nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@0b3015c`。

**レビュア subgent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| 中間モデルに入る Excel 由来の値が、テスティングフレームワークが解釈したあとの値（Java `null` または `String`）になっている | OK | `XlsFormatReader#interpretValue` が `NullInterpreter` → `QuotationTrimmer` → `LineSeparatorInterpreter` を本体設定（`nablarch-testing@3c4bd2a` の `src/test/resources/unit-test.xml:29`-`:40`）と同順で掛ける。値の入口 3 か所（TABLE／LIST_MAP／FILE・MESSAGE のデータ行）すべてを通す。`${...}` 系は掛けない（`XlsNotationSymmetryTest#keepsDollarNotationUninterpreted`） |
| 症状 4 件（`null` → 文字列 `null` ／ `"null"` → Java null ／ `"""` の再読込例外 ／ `\r` の 2 文字化）が、いずれも実ファイル起点で再現しなくなっている | OK | `XlsNotationSymmetryTest#roundTripsSpecialNotationsFromRealBookWithoutLoss`（実 `.xlsx` → モデル → 実 `.xlsx` → モデル。4 記法を 1 行に載せ、1 周目と往復後が一致）。個別は `#readsNullNotationAsJavaNull`／`#readsQuotedNullNotationAsStringNull`／`#readsTripleQuoteNotationAsSingleDoubleQuote`／`#readsBackslashRNotationAsCarriageReturn` |
| `${systemTime}` 等が記法のまま保たれている（解決していない） | OK | `XlsNotationSymmetryTest#keepsDollarNotationUninterpreted`（`${systemTime}`・`${半角英字,10}`） |
| 直す前は落ちて直したあとは通るテストが挙がっている | OK | 下表「赤→緑」参照。是正前は `XlsNotationSymmetryTest` 8 件中 5 件が赤（3 Failures・2 Errors）。是正後は 8 件緑 |
| 期待値をわざと崩すと落ちることを確認した記録が報告にある | OK | 下表「変異」15 件。全件 FAIL を確認（`mvn -o clean test -Dtest=<クラス>#<メソッド>`） |

## 赤→緑（是正前に落ち、是正後に通る）

`git stash push -- src/main` で `src/main` だけを是正前へ戻して `XlsNotationSymmetryTest` を実行した実測。

| テスト | 是正前 | 是正後 |
|---|---|---|
| `XlsNotationSymmetryTest#readsNullNotationAsJavaNull` | FAIL（`is null` に対し `"null"`） | PASS |
| `#readsBackslashRNotationAsCarriageReturn` | FAIL（CR に対し 2 文字の `\r`） | PASS |
| `#writesBackTheSameNotationAsTheSourceBook` | FAIL（`[null, "null", """, …]` に対し `[null, null, ", …]`） | PASS |
| `#roundTripsSpecialNotationsFromRealBookWithoutLoss` | ERROR（`InterpretationFailedException` ← `StringIndexOutOfBoundsException: Range [1, 0)`。`"""` の再読込） | PASS |
| `#readsQuotedValueAsPlainValueAndWritesItWithoutQuotes` | ERROR（同上） | PASS |
| `#readsQuotedNullNotationAsStringNull` | PASS | PASS（`NullInterpreter` が引用符付きへ及ばないことの番人） |
| `#readsTripleQuoteNotationAsSingleDoubleQuote` | PASS | PASS |
| `#keepsDollarNotationUninterpreted` | PASS | PASS |

## 変異（期待値をわざと崩すと落ちること）

15 件すべて FAIL を確認した。

| テスト | 崩した内容 | 結果 |
|---|---|---|
| `XlsNotationSymmetryTest#readsNullNotationAsJavaNull` | `nullValue()` を `"null"` へ | FAIL |
| `#readsQuotedNullNotationAsStringNull` | `"null"` を `nullValue()` へ | FAIL |
| `#readsTripleQuoteNotationAsSingleDoubleQuote` | `"` 1 文字を `"""` へ | FAIL |
| `#readsBackslashRNotationAsCarriageReturn` | CR を 2 文字の `\r` へ | FAIL |
| `#keepsDollarNotationUninterpreted` | 記法 `${systemTime}` を解決後の日付文字列へ | FAIL |
| `#writesBackTheSameNotationAsTheSourceBook` | 入力と同じ記法を「戻さなかった場合の版面」へ | FAIL |
| `#readsQuotedValueAsPlainValueAndWritesItWithoutQuotes` | 外側を外した `ab` を記法のままへ | FAIL |
| `#roundTripsSpecialNotationsFromRealBookWithoutLoss` | 1 要素目の Java `null` を文字列 `"NULL"` へ | FAIL |
| `XlsFormatWriterCellTypeTest#writesCarriageReturnInDataValueAsBackslashRNotation` | 2 文字の `\r` を CR へ | FAIL |
| `#replacesCrLfWithSingleLineFeedInStringCell` | LF 1 文字を CRLF へ | FAIL |
| `#replacesLoneCarriageReturnWithLineFeedInStringCell` | LF を CR へ | FAIL |
| `#keepsCarriageReturnRawInSharedStringsXml` | 「生の CR がある」を「無い」へ | FAIL |
| `XlsFormatReaderCellTypeTest#readsNullNotationAsJavaNullAndQuotedNullAsString` | `nullValue()` を `"null"` へ | FAIL |
| `XlsFormatWriterTest#roundTripsNullCellAsJavaNull` | `nullValue()` を `"null"` へ | FAIL |
| `RoundTripTest#nullCell_isPreservedInBothPaths` | `nullValue()` を `"null"` へ | FAIL |

> **手順上の注意**: 変異の 1 巡目は `mvn -o -q test`（`clean` 無し）で回してしまい、全件が
> `jacoco:instrument` の `Cannot process instrumented class` で異常終了していた。「落ちた」ことが
> 期待値の崩れによるものか判別できないため破棄し、`mvn -o -q clean test` で取り直した。
> 上表は取り直したほうの結果である。

## ゲート（Rules「#30 以降」）

- `git diff --stat`（`56ffbe6`..作業ツリー。`src/` のみ）:

  ```
  src/main/.../xls/XlsFormatReader.java              | 155 ++++++++++++-----
  src/main/.../xls/XlsFormatWriter.java              |  77 ++++++++--
  src/test/.../converter/RoundTripTest.java          |  22 +--
  src/test/.../xls/XlsFormatReaderCellTypeTest.java  |  16 ++-
  src/test/.../xls/XlsFormatWriterCellTypeTest.java  | 135 +++++++++++++---
  src/test/.../xls/XlsFormatWriterTest.java          |  16 ++-
  6 files changed, 341 insertions(+), 80 deletions(-)
  ```

  加えて新規 1 ファイル `src/test/.../xls/XlsNotationSymmetryTest.java`（8 メソッド）。

- `mvn -o clean test` の最終行:

  ```
  Tests run: 614, Failures: 5, Errors: 0, Skipped: 2
  BUILD FAILURE
  ```

  **緑ではない。** 落ちている 5 件は**着手前から赤い 5 件と同一**であり（`checks/step4-report.md` §0）、
  **#36（2-5〜2-7）の担当**である。#32 が新たに落としたものは 0 件。着手前 605 件 → 614 件（+9。
  新規 8 ＋ `XlsFormatWriterCellTypeTest` に 1）。

## 是正の内訳

| 箇所 | 変更 |
|---|---|
| `XlsFormatReader#interpretValue`（新設） | 3 インタープリタを本体と同順で掛ける。値の入口 `:157`（TABLE）・`:188`（LIST_MAP）・`:425`（FILE／MESSAGE のデータ行）から呼ぶ |
| `XlsFormatReader#stripQuotes` | ディレクティブ値専用として残す（`QuotationTrimmer` のみ）。Javadoc を書き直し、データ行の値は `interpretValue` である旨と分けている理由を記した |
| `XlsFormatReader#dropEmptyEntries` ／ `#isEmptyEntry` ／ `#isEmptyCell`（新設） ／ `#interpretRows`（新設） | **空エントリ判定を解釈後の値ではなく記法（セルの文字列）で行うようにした。** 判定対象は `notation:1500`「Excel では行の全セルが空の場合」であり、`null` 記法を書いたセルは空セルではない。この直しが無いと、全セルが `null` 記法の行が丸ごと消える（`#readsNullNotationAsJavaNull` で実際に落ちた） |
| `XlsFormatReader` クラス Javadoc | 「IN 値は記法のまま運ばれる」を、器から出る値の話と中間モデルへ入る値の話に分けた |
| `XlsFormatWriter#toCellNotation`（`nullToLiteral` を置き換え） | 逆写像。Java `null` → `null` リテラル／i CR → 2 文字の `\` ＋ `r`／ii `null`（大小不問）→ 半角クォートで囲む／iii 前後が同じクォート（半角・全角）→ 半角クォートで囲む |
| `XlsFormatWriter#isQuotationWrapped`（新設） | iii の判定。本体 `QuotationTrimmer.java:25`-`:27` が外す条件そのもの |
| `XlsFormatWriter` クラス Javadoc | 「値は記法のまま書く」「`null`↔`null` は復元されない」を、記法へ戻す旨と戻さないもの（値の途中のクォート／`\`＋`n`／LF）へ書き直した |

## 既存テストの変更（6 件）

| テスト | 変更 |
|---|---|
| `RoundTripTest#nullCell_xlsConvertsToLiteralString_yamlPreservesNull` → `#nullCell_isPreservedInBothPaths` | 期待値を `is("null")` から `is(nullValue())` へ。メソッド名・Javadoc を改称 |
| `RoundTripTest` クラス Javadoc `:47`-`:53` | 「可逆性の対象外」から null の非対称の記述を削除 |
| `XlsFormatWriterTest#roundTripsNullCellAsLiteralNullString` → `#roundTripsNullCellAsJavaNull` | 同上 |
| `XlsFormatReaderCellTypeTest#readsLiteralNullStringAsString` → `#readsNullNotationAsJavaNullAndQuotedNullAsString` | `null` 記法 → Java `null`、`"null"` → 文字列 `null` の 2 点をアサート |
| `XlsFormatWriterCellTypeTest#replacesCrLfWithSingleLineFeedInStringCell` ／ `#replacesLoneCarriageReturnWithLineFeedInStringCell` ／ `#keepsCarriageReturnRawInSharedStringsXml` | **入力をデータ行の値からカラム名へ移した。** データ行の値は記法へ戻されて CR がセルへ載らなくなるため。XLS-18 は記法への戻しを受けない経路（カラム名・ディレクティブ値・レコード種別・フィールド名・FW 制御ヘッダ値）では引き続き起きるので、証拠を失わないよう入口だけ移した |

**期待値を変えなかったもの**: 上記以外の全既存テスト。着手前 605 件のうち 599 件は無変更で、`mvn` の結果も変わっていない。

## 台帳の更新

| 文書 | 更新 |
|---|---|
| `coverage/inventory.md` | 改称した 2 件の行（`:1462`・`:2310`・`:2372`）／`XlsFormatWriterCellTypeTest` の件数 18 → **19**（`grep -c '^    @Test' …` → 19 で導出）／軸D D3-06 の担保テストに `writesCarriageReturnInDataValueAsBackslashRNotation` を追加し、CR 2 件の入力がカラム名になったことを明記 |
| `coverage/axis-matrix.md` | D1-16／D2-06／D3-04／D4-04 の担保テスト名を改称後へ。D3-06 の一覧に新規 1 件を追加 |
| `coverage/issues.md` | **XLS-18** に「到達経路が狭まった（#32）」を追記（記録は残す）／**XLS-08** に解説書との食い違いを追記（下記）／「課題としないと判断した観測結果」2 件を現行へ |

**未実施（範囲外として上げる）**: 本タスクで新設した `XlsNotationSymmetryTest`（8 件）は
`inventory.md` の軸要素対応表へ載せていない。Step 4 の各タスクの Steps に台帳への登録が無く、
軸要素の割り当ては #37（完了条件3）が扱う母集合と重なるためである。判断は調整側に委ねる。

## 付随して見つけたこと（直していない）

- **XLS-08 が現行の解説書と食い違う。** `5783b35` の `implementation/testdata_notation.rst:1500` は
  「この判定はマーカーカラムを除外する**前**に行われる。そのため、マーカーカラムだけに値がある
  エントリは読み飛ばされず、他のカラムがすべて空のエントリとして読み込まれる。」と述べている。
  一方 converter は #25.5 の XLS-08 修正で「除外 → 空エントリ判定」を採っており
  （`XlsFormatReader#dropEmptyEntries`。ユーザー確定・2026-08-18。当時は解説書に順序の記述が無く、
  「解説書側へ明文化するよう申し送る予定」としていた）、**マーカーカラムだけの行を読み飛ばす**。
  解説書は逆の順序で明文化された。#32 の範囲外なので直していない。`issues.md` XLS-08 に追記した。

## Overall Verdict

- Self-check: OK
- QA: N/A（Rules「#30 以降、レビュア subagent は回さない」）
- Design expert: N/A（同上）
- Craft expert: N/A（同上）
- Verification expert: N/A（同上）
- Ready to check off: Yes（`mvn` の赤 5 件は #36 の担当であり、#32 が落としたものは 0 件）
