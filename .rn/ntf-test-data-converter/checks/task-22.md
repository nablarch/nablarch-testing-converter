# task-22 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 軸D の8ケースすべてで `getCellType()` がアサートされている（`getStringCellValue()` のみのアサートで終わっていない） | OK | `XlsFormatWriterCellTypeTest` は **18 メソッド**（`grep -c "^    @Test"` → 18）。うち **16 メソッド**が `getCellType()` をアサートし（D3-08 の 6 文字はヘルパ `assertReplacedWithQuestionMark` L275 ／ `assertWrittenAsIs` L293 経由）、残る 2 メソッドは**ラウンド3 B-1 で追加した生バイト検査**である（下記）。要素別: D3-01 L313／D3-02 L335／D3-03 L356／D3-04 L382／D3-05 L402／D3-06 L423（LF）・L453（CRLF）・L482（単独 CR）／D3-07 L509（32768 文字）・L530（32767 文字）／D3-08 はヘルパ経由（L275／L293）。`grep -c "getCellType" XlsFormatWriterCellTypeTest.java` → **19** だが、これは<b>行数</b>であってアサート数ではない（Javadoc の散文 **2 行** ＋ アサート **17 行**。ラウンド3 A-3 の指摘のとおり、以前ここに書いていた「Javadoc 3 行 ＋ アサート 16 行」は誤りだった。`grep -c "getCellType(),"` → 17）。いずれも `XlsFormatWriter#write` で書いた実 `.xlsx` を `XlsFixture.open` で開き直したセルに対して行う（メモリ上のブックではない）。**ラウンド3 B-1 の反映**: `burnsQuestionMarkIntoSharedStringsXmlForControlCharacter`（L631）／`keepsCarriageReturnRawInSharedStringsXml`（L664）を追加し、書き出した `.xlsx` の ZIP エントリ `xl/sharedStrings.xml` を**パースせずバイト列で**検査する。実測（プローブで先に観測してから固定）: `NUL` → ファイルに `<t>a?b</t>`（`?` ＝ `0x3F` が焼き込まれ、`0x00` はファイルに残らない）／単独 `CR` → ファイルに `<t>a`＋`0x0D`＋`b</t>`（生のまま。`&#13;` への退避も `?` への置換も無い）。これで XLS-17（直列化区間）と XLS-18（読み戻し区間）を分ける根拠がテストで固定され、`replacesLoneCarriageReturnWithLineFeedInStringCell` ほかのアサートメッセージ「CR はファイルにも残る」が実検査に裏打ちされた。**ラウンド1 指摘B の反映**: 否定形アサート（`is(not(...))`）と `assertThrows` は型アサートの帰結であって独立した担保ではない旨をクラス Javadoc に明記した（アサート自体は完了条件の文言どおり残す）。**ラウンド2 の反映**: (C-1) 型定数の列挙を「本クラスで現れうるのは次の 4 つ」と限定し `CELL_TYPE_BOOLEAN=4`／`CELL_TYPE_ERROR=5` の存在に触れた／(B-5) トートロジー `getStringCellValue().length() is 3` を削除。**ラウンド3 の適用漏れ回収（2026-08-13）**: (1) `inventory.md` §3.3 の D 行が **16 件**のまま取り残されていたので **18 件**へ訂正し、`grep -c '^    @Test' …/XlsFormatWriterCellTypeTest.java` → 18 を併記したうえで内訳を 8＋2＋1＋3＋2＋**2**（生バイト検査）＝18 と検算できる形にした（§3.1-2 L959 の 18 と一致）。(2) Given/When/Then が無かった 6 メソッド（`replacesNulCharacterWithQuestionMark`／`replacesBellCharacterWithQuestionMark`／`replacesVerticalTabCharacterWithQuestionMark`／`replacesUnitSeparatorCharacterWithQuestionMark`／`writesTabCharacterAsIs`／`writesDeleteCharacterAsIs`）に姉妹クラス `XlsFormatReaderCellTypeTest` と同じ書式（`Given:` ／ `When :` ／ `Then :`）で補い、`@Test` 18 ＝ `Given:` 18 ＝ `When :` 18 ＝ `Then :` 18 で一致することを確認した | **OK**（ラウンド4 で 3 レビューとも OK） | QA（ラウンド4）: D3-01〜D3-08 の全担保メソッドに型アサートあり、`getStringCellValue()` のみで終わるメソッドは 0 と実測。`grep -n getCellType` 19 行のうち L46・L87 が Javadoc の散文で実アサート 17 行、という内訳も再現。Verification（ラウンド4）: 変異ハーネスで **34/34 kill・生存ゼロ**。とくに「読み戻し値は変わらないが区間の帰属だけが変わる」変異を 2 種入れたところ（`<t>a[CR]b</t>` → `<t>a&#10;b</t>` ／ `<t>a?b</t>` → `<t>a&#63;b</t>`）、**それぞれ生バイト検査 1 件だけが FAIL し他 17 件は緑**で、ラウンド3 B-1 の追加が狙いどおり区間の帰属を回帰検知することが実証された |
| `"=1+1"` が数式セルとして解釈されないこと、`"100"` が数値セルにならないことがアサートされている | OK | `writesFormulaLookingStringAsStringCell`（L247）: L252 `is(Cell.CELL_TYPE_STRING)` ＋ L254 値が `"=1+1"` のまま（計算結果 `2` にならない）＋ L256 `is(not(Cell.CELL_TYPE_FORMULA))` ＋ L257 `assertThrows(IllegalStateException.class, () -> cell.getCellFormula())`。`writesNumericLookingStringAsStringCell`（L225）: L230 `is(Cell.CELL_TYPE_STRING)` ＋ L231 値が `"100"` ＋ L234 `is(not(Cell.CELL_TYPE_NUMERIC))` ＋ L235 `assertThrows(IllegalStateException.class, () -> cell.getNumericCellValue())`。実行結果は全 PASS。実装が壊れたときに落ちるのは型アサートの行であり、後続 2 行はその帰結を字面どおり残したものである（クラス Javadoc「アサートの読み方」に明記） | **OK**（ラウンド4 で 3 レビューとも OK） | Verification（ラウンド1・ラウンド4）が変異で実証: 数値見え文字列を `setCellValue(double)` にすると `writesNumericLookingStringAsStringCell` と `writesLeadingZeroStringAsStringCell` が、`=` 始まりを `setCellFormula` にすると `writesFormulaLookingStringAsStringCell` が落ちる。**落ちる行はいずれも型アサート行**であり、後続の否定形アサートには到達しない — Javadoc の自己申告（否定形は型アサートの帰結であって独立担保ではない）が実測と一致している |
| 軸F の3ケース（出力先不在／書き込み権限なし／シート名制約違反）で例外型または結果がアサートされている。`overwrite=false` 衝突は上位層で担保済みとして根拠付きで対象外にされている | OK（F3-02 の**根拠の言い方**をラウンド3 A-1 で再訂正。結論＝辺③の対象外は維持） | `XlsFormatWriterInvalidOutputTest` **16 メソッド**（`grep -c "^    @Test"` → 16）。F3-01 `createsMissingOutputDirectoriesAndWritesWorkbook`（L277）／F3-03 `wrapsAccessDeniedExceptionWhenOutputDirectoryIsNotWritable`（L314。`UncheckedIOException` ＋ 原因 `AccessDeniedException` ＋ メッセージにパス ＋ ファイル未生成。本環境は非 root・ext4 で実際に実行され PASS＝`Skipped: 0`）／F3-04 は **14 件**: 禁止文字 7 件（L349 `/`・L355 `\`・L361 `?`・L367 `*`・L373 `[`・L379 `]`・L385 `:`）・`rejectsEmptySheetName`（L396）・`writesSheetNameOfExcelLimitLengthAsIs`（L423）・`truncatesSheetNameLongerThanExcelLimitSilently`（L452）・`writesSheetNameWhoseForbiddenCharacterIsRemovedByTruncation`（L494）・`rejectsSheetNameWhoseForbiddenCharacterSurvivesTruncation`（L524）・`failsWhenTruncatedSheetNamesCollide`（L561）・**`failsWhenSheetNamesDifferOnlyInCase`（L598。ラウンド3 B-2 で追加）**。**ラウンド3 A-1（事実誤り）**: 「`XlsFormatHandler#outputPaths` は `overwrite=false` 下で 1 件も実行されていない」は誤りだった。自分で変異させて確かめた結果は次のとおり（`src/main` は確認後に `git checkout` で復元済み。`git diff HEAD -- src/main | wc -l` → 0）: (M0) `XlsFormatHandler#outputPaths` の先頭で送出させる → `TestDataConverterTest.convertsXlsToXls:224` / `.convertsYamlToXls:202` / `.convertsYamlWithFilesToXls:427` の **3 件だけが ERROR**（`Tests run: 410, Failures: 0, Errors: 3`）＝実行されている。(M8) `checkOverwrite` の `Files.exists(output)` が真かつ出力が `.xlsx` のときだけ `AssertionError` を送出させる → **`Tests run: 410, Failures: 0, Errors: 0` / BUILD SUCCESS** ＝ `.xlsx` の衝突分岐は 1 件も通っていない。原因は 4 引数入口 `TestDataConverter.convert(DataFormat, DataFormat, Path, Path)`（L49-56）が `Builder#overwrite` を呼ばず、`ConversionRequest` の既定値が `false`（L120 Javadoc／L128 フィールド）なので `checkOverwrite` が早期 return しないこと。訂正済みの記述は「`.xlsx` が**衝突する分岐**が未担保」であり、`XlsFormatWriterInvalidOutputTest` のクラス Javadoc（L51-73）・`inventory.md` §0.8-5・§3.1-2 の F3-02 行に反映した。**ラウンド3 A-2**: 添えていた `grep -rn "outputPaths" src/test --include=*.java` → 0 件 は自己反証だった（実行すると当のクラス Javadoc 自身がヒットする）。Javadoc からはこの grep を削除し、`inventory.md` には検証可能な形（`… | grep -v XlsFormatWriterInvalidOutputTest` → 0 件）で言い直した。**ラウンド3 B-2**: 大文字小文字だけが違うシート名を**先に実行して観測**（`abc` / `ABC` → `IllegalArgumentException: The workbook already contains a sheet of this name`、ブック未生成）してから `failsWhenSheetNamesDifferOnlyInCase` で固定した。これで XLS-16 が原因として引用する `containsSheet` の両輪（切り詰め＋`equalsIgnoreCase`）が実測で押さえられた。`issues.md` の「課題としないと判断した観測結果」にも行を足した（Excel 自身も大文字小文字を区別しないため妥当）。**ラウンド3 C-2**: `assertRejectsSheetName` を `char` 引数に変え、`Invalid char (x) found at index (1)` まで固定した（対照の `rejectsSheetNameWhoseForbiddenCharacterSurvivesTruncation` の粒度に合わせた）。7 件とも PASS。**ラウンド3 C-3**: `dropWritePermission` が `AccessDeniedException` 以外の `IOException` を `IllegalStateException` で落としていた（Javadoc の「権限が効かない環境ではスキップする」に反する）ため、`Assume.assumeNoException` で逃がすよう改めた。ラウンド2 の B-1〜B-6（ブック名リテラルの一元化・衝突時にブックが作られないアサート・`build` の節移動・`notNullValue()` 三段・`restorePermission` の非対称の説明）は維持。**ラウンド3 の適用漏れ回収（2026-08-13）**: (1) `inventory.md` §3.3 の F 行が **15 件**のまま取り残されていたので **16 件**へ訂正し、`grep -c '^    @Test' …/XlsFormatWriterInvalidOutputTest.java` → 16 を併記したうえで内訳を F3-01 1＋F3-03 1＋F3-04 14（7＋1＋1＋1＋2＋1＋1）＝16 と検算できる形にした（§3.1-2 L960 の 16 と一致）。(2) Given/When/Then が無かった `rejectsSheetNameContaining*` 全 7 件に、文字と期待メッセージ（`Invalid char (x) found at index (1)` ／ `in sheet name 'axb'`）を書き分けて補い、`@Test` 16 ＝ `Given:` 16 ＝ `When :` 16 ＝ `Then :` 16 で一致することを確認した。(3) §3.1-2 L1010 の「`convertsYamlToXls` ほか 3 件」が本書の既存形式（「ほか N 件（計 M 件）」。現在行で L487／L862／L1082／L1102／L1152 ほか。`grep -c 'ほか [0-9]* 件（計 [0-9]* 件）' inventory.md` → 47 行。指示で示された L1054／L1074／L1119／L1235 は本回収の挿入で L1082／L1102／L1152／L1271 へ移動している）と不揃いで「計 4 件」と読めたため、実測どおり「`#convertsYamlToXls`, `#convertsXlsToXls` ほか 1 件（計 3 件）」へ改め §0.8-5 と数を揃えた。(4) **`XlsFormatWriter#write` の `parent == null` 分岐（L102-106）が未担保である旨を `inventory.md` に開示**した（§3.1-2 に到達経路の全数、§3.3 に相互参照）。自分で確かめた結果: 成立条件は `basePath` が空文字のときだけ（`jshell` で `Paths.get("", "Book.xlsx").getParent()` → `null` ／ `Paths.get(".output/SampleConversionTest", "Book.xlsx").getParent()` → `.output/SampleConversionTest`）、到達経路は直接呼び出し 19 か所（`grep -rn "new XlsFormatWriter(.*)\.write(" src/test --include=*.java \| wc -l` → 19。すべて `getAbsolutePath()`／`toString()` 由来の非空文字列）と本番配線 `TestDataConverter` L75 のみで、後者の `outputBase` も `TemporaryFolder` 由来の絶対パスと `SampleConversionTest` の `.output/SampleConversionTest`（L34）だけ。**テストは追加していない**（§0.7 の軸F 4 要素のいずれにも当たらないため #22 の範囲外）。本ファイル L55 の未到達箇所表とも一致する | **OK**（ラウンド4 で 3 レビューとも OK。F3-02 の根拠は 3 レビューが独立に変異で再現） | QA・Verification（ラウンド4）が独立に M0／M8 を再現し、実装担当の記録と**完全一致**した。M0（`XlsFormatHandler#outputPaths` で送出）→ `Tests run: 410, Failures: 0, Errors: 3`、落ちるのは `convertsXlsToXls:224`／`convertsYamlToXls:202`／`convertsYamlWithFilesToXls:427` の 3 件のみ。M8（`.xlsx` の衝突分岐で `AssertionError`）→ `Errors: 0` ＝ 衝突分岐は未通過。QA が補足として、`SampleConversionTest#convertsClimanSampleYamlToXls` も XLS 出力だが `.overwrite(true)` のため `checkOverwrite` が早期 return する（＝落ちるのが 3 件である理由）ことまで確認しており、記述と矛盾しない。**この根拠はラウンド1〜3 で 2 度書き換わっている**（「上位層で担保済み」→「1 件も実行されていない」（誤り）→「実行されているが `.xlsx` の衝突分岐は未通過」）。最終形のみが 3 レビューの実測と一致する |
| src/main への変更がゼロ | OK | `git diff HEAD -- src/main \| wc -l` → **0**（ラウンド3 の変異テスト M0／M8 を戻したあとに再確認。2026-08-13）。**適用漏れ回収の後にも再確認して 0**（2026-08-13）。`git status --short` は `.rn/.../inventory.md`・テスト 2 ファイル（M）と本ファイル（??）のみ | **OK**（ラウンド4 で 3 レビューとも OK） | 3 レビューとも `git diff 51196e5..HEAD --stat -- src/main` が空であることを自ら確認。Verification は変異ハーネスを入れた後に復元し、`git status` がクリーンであることまで報告。コーディネータも `eaf687e` の後に再確認して 0 行 |
| `mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` → `Tests run: 410, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`（ラウンド3 の修正後に実行。2026-08-13）。#21 完了時点の 376 件 ＋ #22 の 34 件（`XlsFormatWriterCellTypeTest` 18 ＋ `XlsFormatWriterInvalidOutputTest` 16）＝ 410。ラウンド3 で 3 件増（生バイト検査 2 件 ＋ 大文字小文字衝突 1 件）。**適用漏れ回収（Javadoc 13 メソッド ＋ 台帳訂正）の後にも再実行し `Tests run: 410, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`**（2026-08-13。件数は増減なし＝Javadoc のみの変更）。リグレッションなし | **OK**（ラウンド4 で 3 レビューとも OK） | 3 レビューとも隔離 worktree で自ら実行し `Tests run: 410, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS` を確認。**`Skipped: 0`** なので F3-03 の `Assume` は発火せず実際に実行されている（担保の穴なし）。ラウンド1・2 で観測された偽 FAIL（`NoClassDefFoundError`／`UncheckedIOException: oops`）は**コーディネータが QA と Verification を同時に走らせ、Verification が共有ワーキングツリーの `src/main` を変異させている最中に QA がビルドしたことによる干渉**であり、成果物の欠陥ではない。ラウンド3 以降は 3 レビューとも隔離 worktree で走らせ再現していない |

### Method（実行して記録してから固定する）の適用と、追加テストのカバレッジ

**Method の適用**: 期待値を先に置かず、まず一時プローブ（`ProbeTask22Test` ほか 4 クラス。記録後に削除済み）で
軸D 8 ケース・軸F 4 ケースを実行して結果を標準出力に出し、その結果を見てから妥当性を判断してアサートへ移した。
プローブでしか分からなかった事実が 4 件ある（いずれも #22 の課題記録の中身になっている）:

1. `CR` は書き出すと `LF` へ**置換**される（メモリ上では保たれる）— XLS-18
2. XML で不正な制御文字は `?` に置換される。TAB・DEL は残る（メモリ上ではどちらも保たれる）— XLS-17
3. 32768 文字は例外にも切り詰めにもならずそのまま書かれる — XLS-19
4. 32 文字のシート名は例外にならず 31 文字へ切り詰められる（`build` の時点で）— XLS-16

**レビュー指摘を受けて追加でプローブを実行した（2026-08-13。scratchpad の使い捨てクラス。コミットしない）。**
上記 1 は当初「`CRLF` の `CR` が**落ちる**」と書いていたが誤りで、実測すると `a`＋`CR`＋`b`（3 文字）→
`a`＋`LF`＋`b`（**3 文字**）、`a`＋`LF`＋`CR`＋`b` → `a`＋`LF`＋`LF`＋`b`、値全体が `CR` → `LF` であり、
**削除ではなく置換**である（単独 `CR` では長さが変わらない）。あわせてシート名 `"a"×31 + "/"`（32 文字）が
**例外にならずブックとして書き出される**こと（切り詰めが禁止文字検査より先に走る）、
`"a"×30 + "/a"` は `Invalid char (/) found at index (30) in sheet name 'aaa…a/'`（**切り詰め後の 31 文字**が
メッセージに出る）ことを実測した。いずれも担保テストへ落とし、`issues.md` XLS-16／XLS-18 と
`inventory.md` の該当行を訂正した。

**カバレッジ計測**（`mvn clean jacoco:instrument test jacoco:restore-instrumented-classes` → `mvn jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec`。
`target/site/jacoco/jacoco.xml` から `XlsFormatWriter` のカウンタを抽出）:

| 指標 | #22 前（新規 2 クラスを一時的に退避して計測） | #22 後 |
|---|---|---|
| INSTRUCTION | covered 774 ／ missed 8 | covered 774 ／ missed 8 |
| BRANCH | covered 97 ／ missed 3 | covered 97 ／ missed 3 |
| LINE | covered 150 ／ missed 1 | covered 150 ／ missed 1 |
| METHOD | covered 18 ／ missed 0 | covered 18 ／ missed 0 |

**レビュー指摘の修正後（テスト 31 件・407 件全 PASS の状態）に再計測したが、上表の数値は変わらなかった**
（2026-08-13。`INSTRUCTION` covered 774 ／ missed 8、`BRANCH` covered 97 ／ missed 3、
`LINE` covered 150 ／ missed 1、`METHOD` covered 18 ／ missed 0）。

**数値は前後で同一である。**#22 が足したのは到達行ではなく**アサートする性質**（セル型・直列化後の値・
異常系の例外型）であり、`XlsFormatWriter` の行・分岐は #21 時点で既に到達済みだった。
これは「カバレッジが高いこと」と「挙動が固定されていること」が別物であることの実測例でもある。

**未到達箇所（4 箇所。いずれも #22 の対象軸ではなく、#23 以降でも埋まらない可能性がある）**:

| 位置 | 内容 | 未到達の理由 |
|---|---|---|
| L104 分岐 1/2 | `if (parent != null)` の `null` 側 | `basePath` が空文字で親を持たない相対パス（例: `"foo.xlsx"`）になる呼び出しが無い |
| L171 分岐 1/2 ・ L174（8 命令・1 行） | `layout` の「どのブロック型にも当てはまらない」経路 ＋ `throw new IllegalArgumentException("unsupported block: ...")` | `TestDataBlock` の実装は 4 種（`ColumnRowDataBlock` 系 2・`FileDataBlock`・`MessageDataBlock`）ですべて分岐が用意されており、現行の型階層では到達できない |
| L410 分岐 1/6 | `isMarkerColumn` の分岐のうち 1 つ（`columnName == null` ／ `[` 始まりでないが `]` 終わり、のいずれか） | カラム名 `null` や `x]` を与えるテストが無い |

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Verification approach meaningful to the objective (checks the right thing, not just "passed") | **OK**（ラウンド4 で PASS） | 「動いた／通った」ではなく実 `.xlsx` を書いて開き直す経路を通し、`buildInMemory` との対比で値が変わる区間を特定している点を QA が妥当と判定。ラウンド1〜3 は一貫して **FAIL**（テストではなく**記録側の精度**が理由）: ラウンド1＝`inventory.md` の禁止文字の断定が実測で反証できる／XLS-18 が現象を取り違え、ラウンド2＝CR の変化区間の帰属が誤り（生バイトで反証）、ラウンド3＝`outputPaths` の事実誤りと自己反証する grep。ラウンド4 で残ったのは台帳の件数ズレ（16→18・15→16）のみで、これは**ラウンド3 の適用漏れ**としてユーザー判断のもと `eaf687e` で回収した |

## Expert Reviews (axes the task needs)

### Craft Expert (coding)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Medium-specific best practice | **OK**（ラウンド4 で PASS） | JUnit 4 の使い方（`@Rule TemporaryFolder` によるメソッド単位の隔離、`@After` が Rule の後片付けより先に権限を戻す順序、`assertThrows`）に破綻なし。ZIP エントリ読みは入れ子 try-with-resources で解放。POI 3.8 の `Workbook` は `Closeable` を実装しないため閉じられないことを Craft が `javap` で確認し、**リソース解放漏れの指摘は不成立**と自ら棄却。`dropWritePermission` が canary ファイルで権限の実効性を実測してから `Assume` する設計を「単なる `assumeFalse(isRoot)` より堅い」と評価。ラウンド4 の残指摘は手書きチャンク読みループ（`readAllBytes` へ置換可能）で、**任意対応としてユーザーが明示的に見送り** |
| Consistency with existing style | **OK**（ラウンド4 で PASS） | `@author kiyobot`・import 順（static → java → nablarch → org）・Given/When/Then・日本語コメント・定数の切り出しが姉妹クラス（`XlsFormatReaderCellTypeTest` ほか）と一致。**ソース中の Javadoc に行番号の埋め込みが 0 件**であることを `grep` で確認（ラウンド2 C-3 の反映。行番号引用は `.rn` 配下の台帳側のみという既存慣習に沿う）。ラウンド4 で指摘された Given/When/Then の欠落 13 メソッドは `eaf687e` で補い、`@Test` 18 ＝ `Given:` 18、`@Test` 16 ＝ `Given:` 16 をコーディネータが実測で確認 |

### Verification Expert (test)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Artifact actually checked (tests run / claims verified / flow traced) | **OK**（ラウンド2 で PASS、以降も維持） | 隔離 worktree で自ら `mvn clean test -Djacoco.skip=true` を実行し `Tests run: 410, Failures: 0, Errors: 0, Skipped: 0`。**変異ハーネスによる 34/34 kill・生存ゼロ**（親 pom が surefire `argLine` を固定しているため `-Djunit.argLine=...` で注入）。台帳の主張も自ら裏取り: `javap -c` で `XSSFWorkbook#createSheet` が `containsSheet` → `substring(0,31)` → `validateSheetName` の順であること・`containsSheet` が両辺を 31 文字へ切り詰めて `equalsIgnoreCase` すること・`XSSFCell#setCellValue(String)` に長さ検査が無いこと、`.xlsx` を unzip して `xl/sharedStrings.xml` の生バイト（CR は `0x0D` が生存／制御文字は `0x3F` が焼き込み／`&#` 不在）を確認 |
| Coverage (edge cases / claims / steps) | **OK**（ラウンド4 で PASS） | 境界が両側そろっている: 32767 ちょうど／32768、シート名 31 ちょうど／32、禁止文字が切り詰めで**残る** index 30 ／**消える** index 31 の対照、LF ／ CRLF ／単独 CR、大文字小文字のみ相違。変異で境界の分解能も実証（32767 超で切り詰める変異は超過側だけを、32766 で切り詰める変異は両方を落とす）。未担保（シート名のアポストロフィ・`null`、`XlsFormatWriter#write` の `parent == null` 分岐）は**テストを足さず台帳に開示**する方針で、`eaf687e` で `parent == null` の到達経路の全数列挙とともに記載済み |

## Overall Verdict

- Self-check: OK（レビュー ラウンド3 の指摘 A-1〜C-3 を反映して再確認。2026-08-13）。**A-1（最重要・事実誤り）**: 「`XlsFormatHandler#outputPaths` は `overwrite=false` 下で 1 件も実行されていない」は誤り。自分で M0／M8 を再現して確かめた（M0 → `TestDataConverterTest` の 3 件だけが ERROR ＝ 実行されている／M8 → 0 failures ＝ `.xlsx` の**衝突分岐**は未通過）。`src/main` は復元済み（`git diff HEAD -- src/main | wc -l` → 0）。3 箇所（クラス Javadoc・`inventory.md` §0.8-5・§3.1-2 の F3-02 行）を実測に一致する表現へ訂正し、**F3-02 を辺③の対象外とする結論は維持**した。**A-2**: 自己反証していた grep を削除し、検証可能な形へ言い直した。**A-3**: 「19 件」は `grep -c getCellType` の行数だったので、`@Test` 18／`getCellType()` アサート 17／Javadoc 散文 2 と内訳を明示した。**B-1**: `xl/sharedStrings.xml` の生バイトを検査する 2 件を追加（`?` ＝ `0x3F` の焼き込み／`CR` ＝ `0x0D` の生存と `&#13;` 不使用）。プローブで先に観測してから固定した。**B-2**: 大文字小文字だけが違うシート名の衝突を先に観測してから `failsWhenSheetNamesDifferOnlyInCase` で固定（`issues.md` にも開示）。**C-1**: XLS-16 のラベルを「検出できない」→「衝突しない限り検出できない」へ。掲載順は据え置き（理由を本文に明記）。**C-2**: `assertRejectsSheetName` を index まで固定。**C-3**: `dropWritePermission` の汎用 `IOException` を `Assume.assumeNoException` へ。却下された指摘（中間モデル組み立てヘルパの抽出／アポストロフィ・`null` の追加／否定形アサートと `assertThrows`／`buildInMemory` と `writeAndReopen` の別インスタンス）には手を入れていない。`mvn clean test -Djacoco.skip=true` → **410 件全 PASS**（`Failures: 0, Errors: 0, Skipped: 0`）、`git diff HEAD -- src/main` → 0 行。**ラウンド3 の適用漏れを回収した（2026-08-13。新しいレビューラウンドではなく、ラウンド3 でテスト 3 件を追加した際に `inventory.md` §3.3 だけが取り残されていた分の回収）**: (1) §3.3 D 行 16→**18 件**・F 行 15→**16 件**へ訂正し、それぞれ件数を導く `grep -c '^    @Test' <パス>` を併記して内訳の検算（8＋2＋1＋3＋2＋2＝18 ／ 1＋1＋14＝16）を置いた。§3.1-2 L959／L960 は既に 18／16 で正しく、一致を確認した。(2) §3.1-2 L1010 の「ほか 3 件」を既存形式「ほか N 件（計 M 件）」へ揃えて「ほか 1 件（計 3 件）」とし §0.8-5 と一致させた。(3) 台帳中の grep を**全数**実行して突き合わせ、不一致は `grep -rc "getCellType" src/test --include=*.java` の 1 件だけだった（実行すると 34 行返り 32 行が `:0`）ので `| grep -v ":0$"` を付けて記述どおりの出力になる形へ直した。他の grep（§0.1 のスクリプト・§0.8-4 の `grep -c "^    @Test"` 19／18 と `grep -c getCellType` 19・§0.8-5 の `grep -rln "overwrite" src/main/java` 3 クラスと `outputPaths … | grep -v XlsFormatWriterInvalidOutputTest` 0 件・§3.1-2 L962）はすべて記述と一致した。(4) `XlsFormatWriter#write` の `parent == null` 分岐（L102-106）が src/test 全体で未通過である旨を、到達経路の全数（直接呼び出し 19 か所 ＋ `TestDataConverter` L75）と `jshell` の実測を添えて §3.1-2 に開示し、§3.3 から相互参照した（**テストは追加していない**。§0.7 の軸F 4 要素の範囲外のため §3.3 の件数にも算入していない）。(5) Given/When/Then が欠けていた **13 メソッド**（`XlsFormatWriterCellTypeTest` の制御文字 6 件・`XlsFormatWriterInvalidOutputTest` の `rejectsSheetNameContaining*` 7 件）に姉妹クラスと同じ書式で補い、両クラスとも `@Test` ＝ `Given:` ＝ `When :` ＝ `Then :`（18／18／18／18 と 16／16／16／16）で一致することを `grep -c` で確認した。任意項目だった `readAllBytes()` 置換は行っていない
- QA: **OK**（**ラウンド4 で PASS**。ラウンド1〜3 は FAIL で、理由はいずれもテストではなく**記録側の精度**だった。ラウンド4 で自ら M0／M8 を再現し実装記録と完全一致、`javap`・生バイト・プローブのすべてで台帳の主張が再現。残した指摘は台帳の件数ズレ 3 件のみ）
- Design expert: N/A（既存クラス構成の中でテストを追加するタスクであり、構造・アプローチを新設していない。フィクスチャ基盤の新設は #19 で Design レビュー済み）
- Craft expert (coding): **OK**（**ラウンド4 で PASS**。ラウンド1 は PASS、ラウンド2 は F3-02 の担保範囲の誇張で FAIL、ラウンド3 は `outputPaths` の事実誤りで FAIL、ラウンド4 は台帳の件数ズレで FAIL → `eaf687e` で回収。テストコード自体には全ラウンドを通じて正しさの欠陥が指摘されていない）
- Verification expert (test): **OK**（**ラウンド2 で PASS**、ラウンド3・4 も PASS を維持。ラウンド4 で 34/34 kill・生存ゼロを実証し、生バイト検査 2 件が「読み戻し値は変わらないが区間の帰属だけが変わる」変異を単独で検知することまで確認）
- Ready to check off: **Yes**

### 最終コミット `eaf687e` の扱い（ユーザー判断・2026-08-13）

`eaf687e`（ラウンド3 の適用漏れの回収）は**サブエージェントのレビューを回していない**。ユーザーの判断で、
これはラウンド4 ではなく**ラウンド3 の適用が未完だっただけ**とされ、修正ラウンド上限 3 回には数えない扱いになった。
理由は「残っていたのは新たに見つかった欠陥ではなく、コーディネータが列挙し実測済みの閉じた集合であり、
判断の余地がない」こと。検証はレビュー再実行ではなく、**コーディネータの実測とユーザー自身の突き合わせ**で行う:

| 項目 | コーディネータの実測（2026-08-13） |
|---|---|
| `grep -c '^    @Test' …/XlsFormatWriterCellTypeTest.java` | **18**（台帳 §3.3 D 行の記載と一致） |
| `grep -c '^    @Test' …/XlsFormatWriterInvalidOutputTest.java` | **16**（台帳 §3.3 F 行の記載と一致） |
| `Given:` の数 | 18／16（`@Test` と一致。ラウンド4 の欠落 13 件を解消） |
| `grep -rc "getCellType" src/test --include=*.java \| grep -v ":0$"` | `XlsFormatReaderCellTypeTest:1` ／ `XlsFormatWriterCellTypeTest:19`（台帳の記載どおり再現） |
| `mvn clean test -Djacoco.skip=true` | `Tests run: 410, Failures: 0, Errors: 0, Skipped: 0` |
| `git diff 51196e5..HEAD -- src/main \| wc -l` | **0** |

### 却下した指摘（記録）

| 指摘 | 判断 |
|---|---|
| Craft: `checks/task-22.md` がコミットに含まれていない | **却下**。ワークフロー上、self-check ファイルは実装エキスパートがコミットせず、コーディネータがチェックオフコミットで追加する |
| QA・Craft: シート名のアポストロフィ（先頭/末尾）・`null` が未担保 | **却下（スコープ外）**。Steps が F3-04 の範囲を「31 文字超・禁止文字」と明記している。ただし**未担保である旨を台帳とクラス Javadoc に開示**した |
| Craft: 中間モデル組み立てヘルパが 3 クラスに重複 | **却下**。`XlsFixture` は「実 `.xlsx` のセル種別」層に限ると自身の Javadoc で線を引いており現状は設計どおり。レビュアー自身も「抽出は過剰」と判定。要否は #23 で判断する |
| QA: XLS-17 の「未確認」を追加プローブで閉じる | **却下（スコープ外）**。現在の記述は「実測 6 文字からの推定」と正直に書かれており事実に反していない |
| 全レビュー: 否定形アサート・`assertThrows` が型アサートの帰結である件 | **却下（意図どおり）**。完了条件の文言を字面どおり表すために残し、独立担保ではない旨をクラス Javadoc で明示。変異テストでも「落ちるのは型アサート行」と実証済み |
| Craft: 手書きチャンク読みループを `readAllBytes()` へ | **見送り（ユーザー判断で任意扱い）**。挙動にも担保にも台帳にも影響しない |
| Verification・QA: `mvn` 実行時の `NoClassDefFoundError` ／ `UncheckedIOException: oops` | **却下（成果物の欠陥ではない）**。コーディネータが複数レビュアーを同時に走らせ、片方が共有ツリーの `src/main` を変異させている最中に他方がビルドした干渉。ラウンド3 以降は隔離 worktree で解消 |
