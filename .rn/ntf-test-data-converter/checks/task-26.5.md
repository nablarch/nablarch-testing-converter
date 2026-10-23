# task-26.5 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| `grep -rn "\[空\]" src/` が 0 件 | OK | `grep -rn "\[空\]" src/` を実行し、出力 0 行・終了ステータス 1（該当なし）。改修前は 6 件（`XlsFormatWriter.java` 1／`XlsFormatWriterTest.java` 4／`SampleConversionTest.java` 1） | OK | 3 者が独立に再実行し、いずれも出力 0 行・exit=1。Craft・Verification は唯一の `.xlsx` フィクスチャ `xls/reference/ProjectActionRequestTest.xlsx` を `unzip -p` で展開走査し、`[空]`／`[EMPTY]` とも 0 件であることも確かめた |
| 既存テストが全件成功（`XlsFormatWriterTest#roundTripsZeroRowTableWithoutEatingNextBlock` を含む） | OK | `mvn clean test -Djacoco.skip=true` の Results: `Tests run: 595, Failures: 0, Errors: 0, Skipped: 2`・`BUILD SUCCESS`。`XlsFormatWriterTest` 単体は `Tests run: 45, Failures: 0, Errors: 0, Skipped: 0`（`roundTripsZeroRowTableWithoutEatingNextBlock` は同クラス内で、マーカー値をベタ書きせず往復で確かめる形のまま通っている） | OK | 3 者が surefire XML を直接集計して `Tests run: 595, Failures: 0, Errors: 0, Skipped: 2` を確認。**595 は本変更による目減りではない** —— `@Test` 注釈の実数は `2bbdbf2` 600 → `6114c35` 598 で、597 → 595 は `6114c35`（二重主張のテスト 2 件の削除）によるもの。`0e234e1..HEAD` の差分に `@Test` の増減は無い。Verification は変異試験も実施し、定数を `[空]` へ戻すと 2 件、括弧なしの `EMPTY` にすると 4 件が落ちることを実測した |
| `EMPTY_BLOCK_MARKER_COLUMN` を参照する箇所が `XlsFormatWriter` の 3 箇所のままであること | OK | `grep -rn "EMPTY_BLOCK_MARKER_COLUMN" src/` の出力は 3 行のみ、いずれも `XlsFormatWriter.java`（`:213` Javadoc の `{@value #EMPTY_BLOCK_MARKER_COLUMN}`／`:252` `layoutColumnRow` の参照／`:543` 定義）。`src/test` からの参照は 0 件 | OK | Craft・Verification が `git show 0e234e1:…` と突き合わせ、3 箇所の**行番号（213／252／543）まで不変**であることを確認した |
| 定数名 `EMPTY_BLOCK_MARKER_COLUMN` が変わっていないこと | OK | `:543` は `static final String EMPTY_BLOCK_MARKER_COLUMN = "[EMPTY]";`。`git diff` の当該ハンク は値の 1 トークンのみの差し替えで、名前の差分は無い | OK | Craft が `git diff --word-diff=porcelain` で確認。トークン差分は `-"[空]" / +"[EMPTY]"` の 1 組だけで、名前側に差分は無い |
| `XlsFormatWriter.java` の総行数が変わっていないこと（`coverage/coverage-report.md` が同ファイルの行番号を `da66425` 時点で引用しているため） | OK | `wc -l src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java` ＝ **601**（改修前も 601）。`git diff --stat` でも同ファイルは `2 +-`（1 行の書き換え、増減なし） | OK | 3 者が `HEAD` ／ `da66425` ／ `0e234e1` の 3 時点でいずれも **601** を実測。加えて QA が `coverage-report.md` の引用先の中身を現物で確認し（`:106` `getParent()` の null コメント／`:206` `unsupported block` の throw／`:557` `isMarkerColumn` の null 判定）、引用が無傷であることまで確かめた |
| `.rn/` 側で現行の正を述べている記述が `[EMPTY]` になっており、`[空]` が残るのは観測した現物の記録・完了済みステップの時点記録・本タスク自身の記述だけであること（上の導出コマンドの出力を 1 件ずつ開いて確かめる。`-- src/` は 0 件） | OK | `git grep -n -o -F '[EMPTY]' -- .rn/ntf-test-data-converter/coverage/` は 5 件（`inventory.md:862` 追補その 6 の軸表 C-08 行／`issues.md:85` 要対応内訳の XLS-27 の項／`:122` 番人 7 つの決着表の `columnNames` 0 件 行／`:507` XLS-08 末尾「もう成り立たない」の直後／`:2842` XLS-27【決着】の「改修」箇条）。残る `[空]` は `git grep -c -F '[空]' -- .` で `coverage/issues.md` **3** ／ `steering.md` **14**（総数は `git grep -o -F '[空]' -- . \| wc -l` ＝ **17**。`git grep` は追跡下だけを見るため、未追跡の本ファイル `checks/task-26.5.md`（3 行・9 か所）は数に入らない）。`git grep -c -F '[空]' -- src/` は出力 0 行・終了ステータス 1。17 件を 1 件ずつ開いて確かめ、**いずれも現行の正を述べていない** —— `steering.md` は ①Decisions の訂正ブロック 2 件（`:684`・`:862`。coordinator が本文を `[EMPTY]` へ差し替え、「当時の値は `[空]`」を括弧で添えた形）②完了済みステップの時点記録 2 件（`:1210` XLS-27 実測指示 2026-08-18 の「例 `[空]`」／`:1238` §6-K `839bf64` の記録）③本タスク自身の表題・理由・criterion 文言 3 件（`:1295`・`:1297`・`:1335`）④本ステップの実測記録と導出コマンド 7 件（`:1303`／`:1307`〜`:1309` のコマンド 3 行／`:1314` `804329a` 時点の内訳／`:1316` 作業ツリー値の注記／`:1320` 分類の訂正記録）。`coverage/issues.md` は XLS-27 プローブ実測（2026-08-19）の 3 件で、`:2739` と `:2752` が当時の版面そのもの、`:2741` が「値は当時のまま置いてある」旨のポインタ（現行値の在り処は直後の `:2742`）。`steering.md` は本タスクの Steps が置かれているファイルであって担当外ではなく、**本作業指示で編集を禁じられている**ため無変更で、coordinator が並行して直している（担当外と明記されているのは別リポジトリの `docs/pr75/docs/ntf-empty-table-assertion.md` だけである）。**件数は coordinator の並行修正で動くため、上は本 Evidence を書いた直前に実測した値である**（HEAD `4e5fe58` ＋ 作業ツリー、2026-08-21） | OK | QA・Verification が `coverage/` 側 5 件を独立に確認。**`steering.md` Decisions の訂正ブロック 2 件（`:684`・`:862`）は当初「揃えない」に分類していたが、QA・Craft の 2 者が独立に指摘し、coordinator が実物を読んで誤分類と追認した**（逐語の引用ではなく現在形で機構を述べた訂正ブロックで、標準が「揃える」と定める Decisions の中にある）。`3807b6a` で `[EMPTY]` へ揃え、日付を裏切らないよう「当時の値は `[空]`」を括弧で添えた |

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Verification approach meaningful to the objective (checks the right thing, not just "passed") | OK | **pass（6/6）。** 「通った」で済ませていないことを一次情報で確認済み —— nablarch-testing の `HeaderLine` のマーカー判定が `startsWith("[") && endsWith("]")` のみで**値の長さにも文字種にも依存しない**ことをソースで読み、`[空]`（3 文字・全角込み）→ `[EMPTY]`（7 文字・ASCII）が本体パーサに対して振る舞い中立であることを裏づけた。往復テストが `TestCoreReaderAdapter` 経由で本体パーサを通っていること、実カラム名との衝突が無いこと（`.xlsx` の展開走査を含む）も確認済み。**指摘 F1（`steering.md` の件数記録の自己無効化）は `3807b6a` で修正**、**F2（Steps の締めと `checks/` の追跡）は本コミットで解消**。**F3〜F5 は本変更が持ち込んだ欠陥ではなく既存の担保の穴**（Verification の変異試験で二層の担保が健全であることが実証されたため）で、#27 の軸マトリクスへ持ち越す |

## Expert Reviews (axes the task needs)

### Craft Expert (coding)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Medium-specific best practice | OK | **pass（6/6）。** `src/main` の実変更は 1 トークンのみで、`git diff --check` もクリーン。意図より広い変更は無い。**テスト期待値をリテラル `is("[EMPTY]")` で書いたのは、この文脈では正しい**（パッケージプライベート定数を参照すると恒真アサーションになり、値そのものを固定できない）。`{@value}` とベタ書きの混在も本体には無く（値を書いているのは定義 1 箇所だけ）、テスト側の `{@code [EMPTY]}` は別パッケージから `{@value}` 参照できない事情と、直下のアサーションと揃う可読性から現状が妥当 |
| Consistency with existing style | OK | GWT コメント書式・出典書式（`testdata_notation.rst:nnn` ＋ 基準コミット `30a8271`）は既存どおり維持。**指摘 A（self-check の Evidence が走査範囲を狭めていて再現しない）は本ファイルで修正**、**C（ポインタ 1 行が 130 字で折り返し幅から浮き、ボールドが 2 行続く）・D（版面図から 10 行離れていて図だけ見る読み手に届かない）は `d00da17` で修正**（2 行へ折り返し後半のみ強調、図の注釈側に当時の値である旨を追記）。**E（`steering.md` の 2 件が同じ扱いを受けていない）は採用**し `3807b6a` で揃えた |

### Verification Expert (test)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Artifact actually checked (tests run / claims verified / flow traced) | OK | **pass（6/6）。変異試験で担保が二層であることを実証した** —— 定数を `[空]` へ戻すと**ちょうど 2 件**（`writesMarkerColumnForZeroRowTableBlock` ／ `…ListMapBlock`）が落ち、括弧なしの `EMPTY` にすると**4 件**（前記 2 件＋往復 2 件）が落ちる。すなわち **build テストが綴りを固定し、往復テストが「角括弧で囲まれていること」という機構を固定している**。往復が本体パーサを通ることは `nablarch-testing` jar の `HeaderLine$1.evaluate` を `javap -c` で読んで確認済み。作業ツリーは `git checkout --` で復旧され、`git diff HEAD` は空 |
| Coverage (edge cases / claims / steps) | OK（穴は #27 へ持ち越し） | 自前プローブ 6 本（0 件テーブルが唯一のブロック／2 連続／`EXPECTED_TABLE`／直後が `LIST_MAP`／0 件 `LIST_MAP` が唯一）を一時テストで実行し、すべてブロック件数が保たれ `cols=[] rows=[]` へ戻ることを実測（プローブは削除済み）。実カラム名が `[EMPTY]` と衝突する形は 1 列黙って落ちるが、これは括弧スコープのマーカー意味論そのもので `[空]` でも同一であり**新規の欠陥ではない**。**恒久テストが無い経路（実 `.xlsx` を通る `SampleConversionTest` がマーカーを検証していない／`EXPECTED_TABLE` の 0 件往復／末尾・唯一ブロックの往復／衝突の明示／DB 実行経路の再実測／命名規約そのものの固定）は #27 の軸マトリクスへ持ち越す**（本変更が持ち込んだ穴ではないため #26.5 では足さない） |

## Overall Verdict

- Self-check: OK
- QA: OK
- Craft expert: OK
- Verification expert: OK
- Ready to check off: Yes（Verify は通過済み。ただし `complete task #26.5` の完了マーカーコミットは未実施 —— `/rn:dn` による中断のため。**ユーザーへ出した 2 問（① 命名規約を固定するテストを #26.5 で足すか ② 持ち越し 9 件を #27 へ加えてよいか）は未回答**であり、いずれも #26.5 の Completion criteria ではない）

## 補足: テスト先行（RED → GREEN）を実際に適用した証跡

1. **RED を先に実測した。** 先に `XlsFormatWriterTest` の期待値と Javadoc だけを `[EMPTY]` へ変え、
   `XlsFormatWriter` には触れずに `mvn test -Dtest=XlsFormatWriterTest -Djacoco.skip=true` を実行:

   ```
   [ERROR]   XlsFormatWriterTest.writesMarkerColumnForZeroRowTableBlock:415
   Expected: is "[EMPTY]"
        but: was "[空]"
   [ERROR]   XlsFormatWriterTest.writesMarkerColumnForZeroRowListMapBlock:442
   Expected: is "[EMPTY]"
        but: was "[空]"
   [ERROR] Tests run: 45, Failures: 2, Errors: 0, Skipped: 0
   ```

   落ちたのが**マーカーカラムの値を見ている 2 件だけ**であることも同時に確かめた（他 43 件は成功）。
2. **その後に実装を変えて GREEN にした。** `XlsFormatWriter.java:543` の定数値 1 行だけを `[EMPTY]` へ変え、
   同じコマンドで `Tests run: 45, Failures: 0, Errors: 0, Skipped: 0`・`BUILD SUCCESS`。
3. **全体で確認した。** `mvn clean test -Djacoco.skip=true` で `Tests run: 595, Failures: 0, Errors: 0, Skipped: 2`。
   **JaCoCo は一度も走らせていない**（全実行に `-Djacoco.skip=true` を付けた）。

## 追記: 2 問への回答を受けた処置（2026-08-21）

**上の「Ready to check off」で未回答としていた 2 問にユーザーが回答した。**

| 問 | 回答 | 処置 |
|---|---|---|
| ① 命名規約を固定するテストを #26.5 で足すか | **足さない。⑧ として #27 へ回す** | steering `#26.5` の「2 問への回答」に根拠つきで記録。**テスト件数は 595 のまま** |
| ②-1 担保の穴 8 件を #27 へ持ち越してよいか | **承認**（「二層の担保を実測で確かめたうえで、なお埋まっていない穴」と 1 文添える条件つき） | steering `#27` の Steps へ 1 行足した |
| ②-2 `issues.md` 申し送り節の陳腐化を #27 へ持ち越してよいか | **却下。#26.5 の中で直す** | `issues.md` の当該節を全文書き替えた |

**②-2 の処置の裏取り**（`src/` は無変更。`.md` 3 ファイルのみ変更）:

```sh
$ grep -rn 'カラム名を 1 件も持たないブロックは書き出せません' src/
（0 件）
$ grep -n 'カラム名を 1 件も持たない' src/main/java/nablarch/test/tool/converter/model/ColumnRowDataBlock.java
93:                            "カラム名を 1 件も持たないブロックはセルを持つデータ行を持てません"
$ grep -n 'EMPTY_BLOCK_MARKER_COLUMN' src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java
213:     * {@value #EMPTY_BLOCK_MARKER_COLUMN} を 1 つだけ書く。</b>...
252:                ? Arrays.asList(EMPTY_BLOCK_MARKER_COLUMN)
543:    static final String EMPTY_BLOCK_MARKER_COLUMN = "[EMPTY]";
$ sed -n '256,258p' src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java
        if (rows.isEmpty()) {
            parent.line(key("rows") + ": []");
            return;
```

**明文の実物確認**（`nablarch-document` `30a8271` の
`ja/development_tools/testing_framework/implementation/testdata_notation.rst` を `git show` で取り出して確認）:
`:789`「0件のデータは、以下のように記述する」の Excel 記述例は**カラム名 3 つを持つ形**、`:802`
「データ行を書かない場合でも、カラム名の行は省略できない」、`:819`「カラム名は、最初の行（`rows:` の先頭要素）
のキーで決まる」、`:836`「0件のデータは、`rows:` に空配列 `[]` を記載する」、`:1515` マーカーカラム、`:1550` 除外。
**この 6 か所から「Excel の 0 件テーブルはカラム名を持つが YAML は持てない」＝往復でカラム名が復元されない、
という伝達すべき事実を導いた。**

## 完了時点の Completion criteria 再判定（2026-08-21・完了マーカーコミット直前）

| # | 判定 | Evidence |
|---|---|---|
| 1 | OK | `grep -rn "\[空\]" src/ \| wc -l` → `0` |
| 2 | OK | `mvn -o clean test -Djacoco.skip=true` → `Tests run: 595, Failures: 0, Errors: 0, Skipped: 2`・`BUILD SUCCESS` |
| 3 | OK | `grep -c 'EMPTY_BLOCK_MARKER_COLUMN' .../XlsFormatWriter.java` → `3` |
| 4 | OK | 同上（定数名 `EMPTY_BLOCK_MARKER_COLUMN` のまま） |
| 5 | OK | `wc -l < .../XlsFormatWriter.java` → `601` |
| 6 | OK | `git grep -c -F '[空]'` → `checks/task-26.5.md:10`（本表の記載ぶんを含む）・`coverage/issues.md:3`・`steering.md:17`。`issues.md` の 3 件はいずれも 2026-08-19 プローブの版面記録（`:2739`／`:2741`／`:2752`）で、揃えない側に分類済み |
