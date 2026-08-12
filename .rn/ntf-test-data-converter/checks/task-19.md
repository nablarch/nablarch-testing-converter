# task-19 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 実 `.xlsx` ファイルを入力として `XlsFormatReader` を駆動するテストが存在し、`FakeTestDataReader` を経由していない | OK | 新規 2 クラス 21 件はすべて `new XlsFormatReader()`（本番配線＝`PoiXlsReader`）に実ファイルパスを渡す。`FakeTestDataReader` は `XlsFormatReaderTest` の private 内部クラスであり新規クラスからは参照不能（詳細は Evidence 1） | | |
| 実物 `.xlsx`（Excel 保存物）1 本が参照フィクスチャとして同梱され、POI 生成物と同じ読み取り結果になることが確認されている（確認できない場合は差分が `issues.md` に「未確認」として記録されている） | OK | `src/test/resources/nablarch/test/tool/converter/xls/reference/ProjectActionRequestTest.xlsx`（229,382 byte・無改変）を同梱。`XlsReferenceFixtureTest` 2 件が**非循環な方式**で一致を確認。確認できた射程と**できなかった射程（未確認）**を `issues.md` に明記（詳細は Evidence 2） | | |
| 軸D の 17 ケースすべてについて、中間モデルへ入る値がアサートされている（優先度の低いケースも省略しない。挙動を固定できなかったケースは `issues.md` に記録されている） | OK | `XlsFormatReaderCellTypeTest` 19 件＝ D1-01〜D1-17 の 17 ケース ＋ 空白セル ＋ 行途中の不在セル。固定できなかったケースは無し（詳細は Evidence 3） | | |
| 各ケースの結果が「実行して記録した現状の挙動」であり、実装を期待値に合わせて変更していない（src/main の diff がゼロ） | OK | 期待値はプローブ実行（scratchpad の使い捨てプログラム）で先に観測してからテストへ写した。`git status --porcelain -- src/main` → 出力なし（詳細は Evidence 4） | | |
| 仕様として不適切と判断した挙動が `issues.md` に記録され、かつ修正されていない | OK | `coverage/issues.md` に XLS-01〜XLS-05 を記録。src/main 無変更（詳細は Evidence 5） | | |
| `mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | `Tests run: 337, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`（作業前 316 → +21。リグレッションゼロ。詳細は Evidence 6） | | |

### Evidence 1 — 実 `.xlsx` 経路であることの根拠

追加した 3 ファイル（すべて `src/test/java/nablarch/test/tool/converter/xls/`）:

| ファイル | 役割 | 件数 |
|---|---|---|
| `XlsFixture.java` | POI で `.xlsx` を組み立てるフィクスチャビルダ（テスト無し・ヘルパ） | — |
| `XlsFormatReaderCellTypeTest.java` | 軸D セル種別 17 ケース | 19 `@Test` |
| `XlsReferenceFixtureTest.java` | 参照フィクスチャの読み取り結果の固定と POI 生成物との一致 | 2 `@Test` |

- 両テストとも `new XlsFormatReader()`（引数なし＝`new TestCoreReaderAdapter(new PoiXlsReader())`、`XlsFormatReader` L80-82）を使う。
  アダプタ注入コンストラクタ（L89）は使っていない。
- `FakeTestDataReader` は `XlsFormatReaderTest` の private static 内部クラス（同クラス L54-102）であり、
  他クラスからは参照できない。`grep -n "FakeTestDataReader" src/test/java/nablarch/test/tool/converter/xls/*.java`
  → `XlsFormatReaderTest.java` のみ。
- これにより `PoiXlsReader#readOneLine` の「実セル → 文字列行」区間（`cell.toString()`）が
  converter のテストで初めて実行される。

### Evidence 2 — 参照フィクスチャの選定と、非循環な一致確認

#### 2-1. 旧方式（`ClientActionTest.xlsx` ＋ `copyOf`）を廃止した理由

QA・Craft・Verification の 3 レビューが独立に同じ欠陥を指摘し、こちらでも再現した。

- `XlsFixture#copyOf` は元セルを `sourceCell.toString()` で文字列化して**文字列セル**として作り直す。
  リーダ側 `PoiXlsReader#readOneLine`（L123）も `cell.toString()` で値を取る。
  **同じ関数を 2 回適用しているだけの恒等式**であり、入力ブックのセル種別が何であれ一致する。
- 旧参照フィクスチャ `ClientActionTest.xlsx` は POI 全走査の結果、
  **文字列セル 572・空白セル 28・数値セル 0・数式セル 0・日付セル 0**。
  最重要課題 XLS-01（数値セルが `"1.0"` になる）を実 Excel 保存物で確認できない。

`copyOf` は本タスクの新方式で不要になったため **`XlsFixture` から削除**した（デッドコードを残さない）。

#### 2-2. 差し替え先の選定（同ブランチ 6 ファイルを自分で全走査した実測）

`nablarch-example-web` `origin/main` の `.xlsx` 6 ファイルを POI で全セル走査した。

| ファイル | シート | 数値セル | うち表示形式 `@` | **データ列にある数値セル** |
|---|---|---|---|---|
| `AuthenticationActionRequestTest.xlsx` | 6 | 5 | 5 | 0 |
| `ClientActionTest.xlsx` | 1 | 0 | 0 | 0 |
| `IndustryActionTest.xlsx` | 1 | 0 | 0 | 0 |
| **`ProjectActionRequestTest.xlsx`** | 26 | 26 | 25 | **1** |
| `ProjectBulkActionRequestTest.xlsx` | 10 | 3 | 3 | 0 |
| `ProjectUploadActionRequestTest.xlsx` | 6 | 5 | 5 | 0 |

- **コーディネータ指示の前提を 1 点訂正する。** 表示形式 `@` 付きの数値セル 38 件は
  **すべて `LIST_MAP` のマーカー列 `[no]` の値**であり、`HeaderLine#getEffectiveColumnNames()` が
  除外するため中間モデルには入らない（steering #15 の完了条件どおりの意図した除外）。
  よってこの 38 件だけでは XLS-01 は中間モデル上に現れない。
- 6 ファイル全体で **データ列に置かれた数値セルは 1 件だけ**存在し、それが
  `ProjectActionRequestTest.xlsx` の `downloadNormal` シート `M14`
  （`<c r="M14" s="55"><v>2000</v>`、`t` 属性なし＝数値セル、`cellXfs[55]` は `numFmtId="0"` ＝ General）。
  `SETUP_TABLE[1]=PROJECT` の `COST_OF_GOODS_SOLD` 列に当たり、中間モデルへ `"2000.0"` として入る。
  同じ行の `SALES`／`SGA`／`ALLOCATION_OF_CORP_EXPENSES`（文字列セル）は `"1000"`／`"3000"`／`"4000"`。
- 同シートは `[no]` マーカーの表示形式 `@` 付き数値セル（`<c r="A19" s="37"><v>1</v>`、
  `cellXfs[37]` は `numFmtId="49"` ＝ builtin `@`）も併せ持つ。
- 結論: **コーディネータ指示どおり `ProjectActionRequestTest.xlsx` を採用**する。
  XLS-01 が中間モデル上で実際に発現する唯一のファイルであり、選定理由は指示より強い。
  検証対象シートは `downloadNormal`（LIST_MAP 3・SETUP_TABLE 1・EXPECTED_VARIABLE 1 の 5 種 6 ブロック）。

#### 2-3. 同梱と真正性の確認

```sh
cd /home/tie303177/work/nablarch/nablarch-example-web
git show origin/main:src/test/java/com/nablarch/example/app/web/action/ProjectActionRequestTest.xlsx \
  > <converter>/src/test/resources/nablarch/test/tool/converter/xls/reference/ProjectActionRequestTest.xlsx
```

- 読み取りのみ。`nablarch-example-web` へは一切書き込んでいない（`git status --porcelain` → 出力なし）。
- 無改変であることを sha256 で確認: `git show` の出力と同梱ファイルがともに
  `93486dcadc5810ea760606e252cc30203fc0ffc89c333e9a41390976b760b4e8`（229,382 byte）。
- 真正な Excel 保存物であること: `docProps/app.xml` に
  `<Application>Microsoft Excel</Application>`・`<AppVersion>16.0300</AppVersion>`、
  `docProps/core.xml` の `dcterms:modified` は 2023-06-02。26 ワークシート。
- **同梱バイナリは 1 本のまま**（`ClientActionTest.xlsx` は `git rm` で削除済み）。

#### 2-4. 非循環にした比較方式

期待値を Excel 保存物から機械的に導出せず、**テストソースに直書きした論理内容**を唯一の権威にした。

| テスト | 何と何を突き合わせるか |
|---|---|
| `readsExcelSavedWorkbookIntoIntermediateModel` | 参照フィクスチャの読み取り結果 ↔ **直書きの期待値**（6 ブロックのデータタイプ・グループ・識別子・列名・全行の値・ディレクティブ・レコードレイアウト・フィールド定義） |
| `poiGeneratedWorkbookReadsIdenticallyToExcelSavedWorkbook` | 参照フィクスチャの読み取り結果 ↔ **同じ論理内容をセル種別を明示して組んだ POI 生成ブック**の読み取り結果（`text(...)` / `number(2000)` / `number(1, "@")` / `blank()` をテストソースで宣言） |

- POI 側のセル種別はテストソースが宣言するため、Excel 側の実体と食い違えば落ちる。
  「同じ関数を 2 回適用する」構造は無くなった。
- POI 生成ブックは、参照ブックの各行末にある空白セル（書式だけの最大 256 列ぶん）と全空行を再現しない。
  前者は空文字として読まれるだけで列構造に影響せず、後者は `PoiXlsReader#readLine` が読み飛ばすため。
  **この前提が誤っていればこのテストが落ちる**（実際には初回実行で一致した）。

#### 2-5. 非循環であることのミューテーション実証

| # | ミューテーション | 結果 |
|---|---|---|
| M2-1 | POI 側 `number(2000)` → `text("2000")`（Excel は数値セル・POI は文字列セル） | **FAIL**（`section[0].block[2].rows` expected `2000.0` / was `2000`） |
| M2-2 | POI 側 `text("10000")` → `number(10000)`（Excel は文字列セル・POI は数値セル） | **FAIL**（expected `10000` / was `10000.0`） |
| M2-3 | 直書き期待値 `"2000.0"` → `"2000"` | **FAIL**（`readsExcelSavedWorkbookIntoIntermediateModel`） |

旧方式（`copyOf`）では M2-1・M2-2 に相当する差はそもそも作れず、Verification の実測では
`row2 source=NUMERIC copy=STRING sourceToString=1.0 copyValue=1.0` で `EQUAL = true` になっていた。
新方式ではセル種別の食い違いが両方向で検出される。

#### 2-6. 残る射程の限定（「未確認」の記録）

**Evidence 2 の旧記述「完全一致。差分ゼロ。『未確認』として記録すべき事項は無い」は撤回する。**

確認できたのは Excel 保存物側に実在する **文字列セル／数値セル（General）／数値セル（`@`）／空白セル**
の 4 種のみ。日付・時刻・日時・数式・真偽値・エラーの各セルは参照フィクスチャにも
`nablarch-example-web` の 6 ファイル全体にも 1 件も存在しないため、**Excel 保存物との突き合わせは未実施**
である。D1-06〜D1-11 の挙動は POI 生成物でのみ確認したものであり、
`coverage/issues.md`「未確認」節に射程の限定として記録した。
検証対象シートも 26 シートのうち `downloadNormal` 1 枚に限っている。

### Evidence 3 — 軸D 17 ケースの実測結果

すべて `SETUP_TABLE` の 1 データ行（`KEY` 列＝ケース識別・`V` 列＝検証対象セル）として与え、
`V` 列の中間モデル値を記録した。`V` 列のみのシートにすると空セルのケースで行全体が空行になり
`PoiXlsReader#readLine` に読み飛ばされるため、行を空にしない `KEY` 列を必ず置いている
（**この回避を要すること自体を課題 XLS-05 として `issues.md` に記録した**）。

| ケース | 入力セル（POI） | 中間モデルへ入った値 | 担保テストメソッド | 判断 |
|---|---|---|---|---|
| D1-01 文字列 | STRING `abc` | `"abc"` | `readsStringCellAsIs` | 妥当 |
| D1-02 整数数値 | NUMERIC `1` | `"1.0"` | `readsIntegerNumericCellAsDoubleString` | **課題 XLS-01** |
| D1-03 小数数値 | NUMERIC `1.5` | `"1.5"` | `readsDecimalNumericCellAsDoubleString` | 課題 XLS-01（同根） |
| D1-04 大きい数値 | NUMERIC `12345678901234567890` | `"1.2345678901234567E19"` | `readsLargeNumericCellAsScientificNotation` | **課題 XLS-01** |
| D1-05 先頭ゼロ文字列 | STRING `007` | `"007"` | `readsLeadingZeroStringCellAsIs` | 妥当 |
| D1-06 日付書式 | NUMERIC 2026-08-07・書式 `yyyy/mm/dd` | `"07-Aug-2026"` | `readsDateFormattedCellAsPoiDefaultDatePattern` | **課題 XLS-02** |
| D1-07 時刻書式 | NUMERIC `0.5`・書式 `hh:mm:ss` | `"31-Dec-1899"` | `readsTimeFormattedCellLosingTimeComponent` | **課題 XLS-02** |
| D1-08 日時書式 | NUMERIC 2026-08-07 12:34:56・書式 `yyyy/mm/dd hh:mm:ss` | `"07-Aug-2026"` | `readsDateTimeFormattedCellLosingTimeComponent` | **課題 XLS-02** |
| D1-09 数式 | FORMULA `1+1` | `"1+1"` | `readsFormulaCellAsFormulaText` | **課題 XLS-03** |
| D1-10 真偽値 | BOOLEAN `true` | `"TRUE"` | `readsBooleanCellAsUpperCaseLiteral` | 妥当 |
| D1-11 エラー値 | ERROR `#DIV/0!` | `"#DIV/0!"` | `readsErrorCellAsErrorText` | 妥当 |
| D1-12 セル不在（行末） | セルを作らない | `""` | `readsAbsentCellAsEmptyString` | 課題 XLS-04（記録のみ） |
| D1-12 セル不在（行の途中＝穴） | `row(text("k"), absent(), text("z"))` | `""` | `readsAbsentCellInMiddleOfRowAsEmptyString` | 課題 XLS-04（記録のみ） |
| D1-13 空文字 | STRING `""` | `""` | `readsEmptyStringCellAsEmptyString` | 課題 XLS-04（記録のみ） |
| （追加観測）空白セル | BLANK（セルはあるが値なし） | `""` | `readsBlankCellAsEmptyString` | 課題 XLS-04（記録のみ） |
| D1-14 前後空白 | STRING `␣␣pad␣␣` | `"  pad  "` | `readsSurroundingWhitespacePreserved` | 妥当 |
| D1-15 改行 | STRING `line1\nline2` | `"line1\nline2"` | `readsEmbeddedNewlinePreserved` | 妥当 |
| D1-16 リテラル `null` | STRING `null` | `"null"` | `readsLiteralNullStringAsString` | 妥当 |
| D1-17 表示形式付き数値 | NUMERIC `1`・書式 `@`（テキスト） | `"1.0"` | `readsTextFormattedNumericCellAsDoubleString` | **課題 XLS-01（実データ影響 最大）** |

- 着手順は steering Decisions「軸D の優先度」に従い、最優先（D1-01／D1-02〜04／D1-12・D1-13／D1-17）を
  先に実測・固定してから、優先度の低い D1-06〜D1-11 を実施した。
- **17 ケースすべてを固定できた。** 挙動を固定できず `issues.md` へ逃がしたケースは無い。
- ただし D1-06〜D1-08 は POI が `new SimpleDateFormat("dd-MMM-yyyy")`（既定ロケール）で文字列化するため、
  素のままでは値が実行環境で変わる（`-Duser.language=ja -Duser.country=JP` で `07-8-2026` になることを実測）。
  **ロケール依存であること自体を課題 XLS-02 として記録した。**

#### 3-1. D1-17 が表示形式 `@` の付与を検証するようにした（Verification 指摘）

修正前の D1-17 は読み取り値 `"1.0"` しか見ておらず、観測上 D1-02（表示形式なしの数値セル）と
区別できなかった。書き出した `.xlsx` を読み戻し、当該セルが**数値セルであること**と
`getDataFormatString()` が `"@"` であることをアサートするようにした。

| ミューテーション | 修正前 | 修正後 |
|---|---|---|
| `XlsFixture#number(v, fmt)` から `cell.setCellStyle(...)` を削除 | D1-17 は **PASS のまま**（D1-07 だけが落ちた） | **D1-17 が FAIL**（`Expected: is "@"` / `but: was "General"`）。D1-07 も従来どおり FAIL |

#### 3-2. D1-12 に `PoiXlsReader#readOneLine` L123 を通すケースを追加した（QA・Verification 指摘）

`row(text("k"), absent())` は不在セルが行末になるため `Row#getLastCellNum()` が 1 になり、
`i < lastCellNum` ループが `V` 列に到達しない。`PoiXlsReader` を直接呼んで読み取り行をダンプした実測:

```
行末不在   line[2] size=1 [k]          ← V 列はそもそも行に入らない（L123 未実行）
途中に穴   line[2] size=3 [k, , z]     ← L123 の cell == null ? "" が実行される
```

- 行の途中に穴を置くケース `readsAbsentCellInMiddleOfRowAsEmptyString` を追加し、L123 を通した。
- 両テストとも、書き出した `.xlsx` を読み戻して**行の使用範囲**をアサートし、経路の違いを
  テスト自身が示すようにした（行末: `getLastCellNum() == 1`／穴: `getLastCellNum() == 3` かつ
  `getCell(1) == null`）。両者が別経路であることは各 Javadoc に明記した。
- ミューテーション: 穴ケースの期待行を `["k","HOLE","z"]` に変える → **FAIL**（`was [[k, , z]]`）。
  行末ケースの `getLastCellNum()` 期待値を 3 に変える → **FAIL**（`was <1s>`）。

#### 3-3. `Locale.setDefault` の適用範囲を 3 件に絞った（Craft 指摘）

`@Before`/`@After`（クラス全 19 件に JVM グローバルな変更が掛かる）をやめ、
メソッド注釈 `@EnglishLocale` が付いたテストだけを `ExternalResource` で包む `TestRule` へ移した。
付与したのは D1-06〜D1-08 の 3 件のみ。

- 実証: `readsDateFormattedCellAsPoiDefaultDatePattern` から `@EnglishLocale` を外して
  `-Duser.language=ja -Duser.country=JP` で実行 → **FAIL**（`but: was "07-8-2026"`）。付け直すと PASS。
- **タイムゾーンは固定していない。** POI の往復（`DateUtil#getExcelDate` ↔ `getJavaDate`）が
  同一の既定タイムゾーンを使い相殺するため。自分でも `UTC`／`America/Los_Angeles`／
  `Pacific/Kiritimati`／`Europe/Istanbul` の 4 つで本クラス 19 件が全 PASS することを実測した。
  将来この固定を足す必要がないことをルールの Javadoc に残した。

#### 3-4. ブック名一意化の根拠を実態に合わせた（Craft 指摘）

`readValue` の `@param book`（「`PoiXlsReader` のブックキャッシュ衝突を避けるためテストごとに一意」）は
不正確だった。自分でも実測して確認した。

- converter 経路は `TestCoreReaderAdapter` の全パーサが `parse(..., false)` を渡し、
  `TestDataParsingTemplate` L138-140 が `PoiXlsReader#setUseCache(false)` を呼ぶ（ソースで確認）。
- プローブ: 同一パスの `.xlsx` を書き換えて同一 JVM 内で 2 回読む → `read1 = FIRST` / `read2 = SECOND`。
  **ブックキャッシュは効いていない。**
- 加えて `@Rule TemporaryFolder` がテストメソッドごとに別ディレクトリを与える。
- 対応: `book` 引数を廃止して固定名 `cellTypes` にし、上記の根拠をフィールド Javadoc に書いた。
- #18 §0.8-8 のとおり D1-13／D1-14／D1-16 は `RoundTripTest` が実ファイル経由で 🔺 担保していたが、
  往復ではなく**直接テスト**として本タスクで正式に固定した（steering Rules に従い往復テストは正式担保に数えない）。
  往復テストには手を加えていない。

### Evidence 4 — 「実行して記録した現状の挙動」であることと src/main 無変更

- 手順: (1) scratchpad に使い捨てのプローブプログラム（`Probe.java` / `Probe2.java`）を置き、
  17 ケースのセルを 1 シートにまとめて `XlsFormatReader` で読んだ結果を標準出力へダンプ、
  (2) その出力を Evidence 3 の表として記録、(3) 記録した値をテストの期待値へ写した。
  プローブは scratchpad（`/tmp/claude-1000/.../scratchpad`）に置いたのみでリポジトリには含まれない。
- `git status --porcelain -- src/main` → **出力なし**（初回コミット時・レビュー修正後とも）。
- レビュー修正ラウンドの `git status --porcelain` 全出力（変更は `src/test` と `.rn` のみ）:

  ```
   M .rn/ntf-test-data-converter/coverage/issues.md
   M .rn/ntf-test-data-converter/checks/task-19.md
   M src/test/java/nablarch/test/tool/converter/xls/XlsFixture.java
   M src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderCellTypeTest.java
   M src/test/java/nablarch/test/tool/converter/xls/XlsReferenceFixtureTest.java
  D  src/test/resources/nablarch/test/tool/converter/xls/reference/ClientActionTest.xlsx
  ?? src/test/resources/nablarch/test/tool/converter/xls/reference/ProjectActionRequestTest.xlsx
  ```

- ミューテーション（Evidence 2-5・3-1〜3-3）はすべて実施後に元へ戻し、
  作業ツリーが上記の状態であることを確認した。
- `steering.md` は未編集（コーディネータの担当）。
- 他リポジトリ無変更: `nablarch-example-web` / `nablarch-testing` → `git status --porcelain` 出力なし。
  `nablarch-testing-yaml` の未追跡 3 件（`META-INF/`・`entity.list.txt`・`nablarch/`）は
  タイムスタンプが 2023-12〜2026-05 で本作業以前から存在するもの。

### Evidence 5 — `issues.md` に記録した課題

`.rn/ntf-test-data-converter/coverage/issues.md` を新規作成した。

| ID | 課題 | 影響度 | 対象ケース |
|---|---|---|---|
| XLS-01 | 数値セルが `double` の文字列表現（`1` → `"1.0"`、大きい値は指数表記）になる。表示形式 `@` も無視される | 高 | D1-02, D1-03, D1-04, **D1-17** |
| XLS-02 | 日付・時刻・日時セルがセルの表示形式を無視した `dd-MMM-yyyy` になり、ロケール依存かつ時刻成分が失われる | 中 | D1-06, D1-07, D1-08 |
| XLS-03 | 数式セルが計算結果ではなく数式文字列になる | 中 | D1-09 |
| XLS-04 | セル不在・空白セル・空文字セルが区別されない（すべて `""`）。Fake リーダ経路（既存 33 件）では `null` が保持されるため経路間で非対称 | 低（記録のみ） | D1-12, D1-13 |
| XLS-05 | **全カラムが空のデータ行が黙って消える**（`PoiXlsReader#readLine` L83-99 ＋ `isBlankLine` L136-143 がブロック区切りの空行と区別しない）。警告も出ない | 高 | 本タスクで発見 |

- XLS-05 は本タスクで発見した現状挙動。プローブ実測: `SETUP_TABLE` にデータ行 3 件
  （`[x1,y1]` / `["",""]` / `[x3,y3]`）を与えると中間モデルの行は **2 件**になり、
  空行だけが消える。`XlsFormatReaderCellTypeTest` が `KEY` 列を置いて回避しているのはこの挙動のため。
  **修正はしていない**（`src/main` 無変更）。
- いずれも `src/main` を変更していない（Evidence 4）。
- XLS-01 は steering Decisions のユーザー実測（`nablarch-example-web` の 6 ファイルに数値セル 39 件、
  実例は「値が数値の `1`・表示形式 `@`」）に直接該当し、変換後 YAML に `1.0` が書かれる。
- 本体 `PoiXlsReader` の Javadoc は「文字列書式以外のデータ書式が存在した場合の動作は保証しない」と
  明記しており、XLS-01〜XLS-03 はいずれも本体仕様上の保証外の範囲である。この前提も `issues.md` に記載した。

### Evidence 6 — `mvn clean test` の結果

```
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean test -Djacoco.skip=true
→ Tests run: 337, Failures: 0, Errors: 0, Skipped: 0
→ BUILD SUCCESS
```

内訳（新規分）: `XlsFormatReaderCellTypeTest` 19 ／ `XlsReferenceFixtureTest` 2 ＝ 21 件。
既存テストは全件そのまま PASS（`XlsFormatReaderTest` 33 ／ `XlsFormatWriterTest` 40 ／
`RoundTripTest` 30 ／ `YamlFormatReaderTest` 20 ／ `YamlFormatWriterTest` 33 ほか）。リグレッションゼロ。

ロケール・タイムゾーン依存が無いことの確認:

| 実行条件 | 結果 |
|---|---|
| `-DargLine="-Duser.language=ja -Duser.country=JP"`（全 337 件） | `Tests run: 337, Failures: 0, Errors: 0` / `BUILD SUCCESS` |
| `-Duser.language=ja -Duser.country=JP -Duser.timezone=Pacific/Kiritimati`（新規 21 件） | `Tests run: 21, Failures: 0, Errors: 0` |
| `-Duser.timezone=` `UTC` / `America/Los_Angeles` / `Pacific/Kiritimati` / `Europe/Istanbul`（`XlsFormatReaderCellTypeTest` 19 件） | いずれも `Tests run: 19, Failures: 0, Errors: 0` |

## フィクスチャ基盤の設計判断（Design レビュー対象）

### 何が必要だったか

軸D の 17 ケースは「セル種別（文字列／数値／日付／数式／真偽値／エラー／空白／不在）」と
「表示形式（`@`・`yyyy/mm/dd` 等）」を指定できないと組み立てられない。

### 既存ヘルパを読んだ結果（重複の有無）

| 既存ヘルパ | 何を組み立てるか | 本タスクに使えるか |
|---|---|---|
| `XlsFormatWriterTest` の private static（`container` / `row` / `map` / `build` / `cell` / `line`） | 中間モデル ＋ `XlsFormatWriter#build` のメモリ上ブック | 使えない。`XlsFormatWriter` は全値を文字列セルで書くため（`XlsFormatWriter#render`）セル種別を表現できない。また private static のためクラス外から使えない |
| `RoundTripTest` の private static（`container` / `table` / `file` / `message` / `xlsRoundTrip` ほか） | 中間モデル ＋ `XlsFormatWriter#write` の実ファイル | 同上。実 `.xlsx` は通るが文字列セルのみ |
| `XlsFormatReaderTest#readerOf` ＋ `FakeTestDataReader` | `List<List<String>>` の canned 行 | 使えない。POI を通らないのが本タスクの埋める穴そのもの |

→ 既存ヘルパはいずれも**中間モデル層**を組み立てる。本タスクが要るのは**セル層**であり、
対象レイヤが異なるため新設しても重複にならない。既存ヘルパは 1 つも変更していない。

### 採用した形

- **クラス**: `nablarch.test.tool.converter.xls.XlsFixture`（`src/test/java`・パッケージプライベート）
- **配置**: 被テストクラス（`XlsFormatReader`）と同一パッケージ。既存テストと同じ場所に置き、
  テスト専用であることをパッケージプライベートで示す。
- **API**: 流暢な組み立て ＋ セル指定の static ファクトリ

  ```java
  XlsFixture.book("myBook").sheet("mySheet")
          .row(text("SETUP_TABLE=USERS"))
          .row(text("USER_ID"), text("AGE"))
          .row(text("U1"), number(1, "@"))
          .writeTo(dir);
  ```

  セル指定: `text` / `number(v)` / `number(v, 表示形式)` / `date(v, 表示形式)` / `formula` /
  `bool` / `error(FormulaError)` / `blank()`（セルはあるが値なし）／ `absent()`（セルを作らない）。
  テスト側は static import して使う。

- **なぜこの形か**:
  - `row(CellSpec...)` の可変長引数が Excel の 1 行と 1 対 1 に対応し、テスト本文がシート版面のまま読める。
  - セル種別ごとに専用メソッドを分ける（`number` と `text` を型で分けない）ことで、
    「このケースは数値セルである」という**テストの意図がシグネチャに現れる**。
    `Object` を受けて中で型分岐する設計は、意図が呼び出し側から消えるため採らなかった。
  - `blank()` と `absent()` を別物として持つ。この 2 つは Excel 上で異なる状態であり、
    区別できるかどうかが D1-12／D1-13 の検証対象そのものである。
  - 表示形式は文字列で渡し、`CellStyle` は書式文字列でキャッシュする（Excel のスタイル数上限への配慮）。
  - `CellSpec` は抽象クラス＋無名サブクラスにした。`enum` ＋ フィールドの値オブジェクトにすると
    「種別ごとに意味のあるパラメータが違う」ことを型で表せず、未使用フィールドが増えるため。
  - `open(Path)` も本クラスに置き、**セルを組み立てる POI コードを 1 か所に閉じた**。
    テストクラス側に残る POI の生 API は、セル指定に使う `FormulaError` と、
    「リーダに何を食わせたか」をブック側で確認するための読み取り専用の利用
    （`Sheet` / `Row` / `Cell`。D1-17 の表示形式アサートと D1-12 の行の使用範囲アサート）だけである。
    セルの**生成**をテストクラスが直接行っている箇所は無い。
  - `copyOf(bookName, Sheet)` は当初「参照ブックを写した POI ブックを作る」ために置いていたが、
    比較の循環（Evidence 2-1）の原因そのものであり、非循環方式への移行で不要になったため削除した。
- **POI 3.8 制約**: `CellType` enum は POI 4 以降のため使っていない。3.8 の
  `Cell#setCellValue` / `setCellFormula` / `setCellErrorValue(byte)` / `CellStyle#setDataFormat(short)` /
  `Workbook#createDataFormat()` と `org.apache.poi.ss.usermodel.FormulaError`（3.8 に存在することを
  `javap` で確認）のみを使う。

### 意図的に入れなかったもの

- セルの色・罫線・列幅の指定（`XlsFormatWriter` の整形の関心事であり、辺①の読み取りに影響しない）。
- 複数シートの一括生成 API（`sheet()` を続けて呼べば足りる。#21 の軸E で必要になったときに評価する）。
- 中間モデルの組み立てヘルパ（既存 2 クラスと重複するため持たせない）。

## Expert Reviews

4 レビュー（QA／Craft (coding)／Verification (test)／Design）を実施し、指摘をすべて修正した。
以下は各指摘と対応状況。修正内容の詳細は Evidence 2・3 を参照。

### QA — PASS with findings（Major 1 / Minor 3）

| # | 重大度 | 指摘 | 対応 |
|---|---|---|---|
| Q-1 | Major | 参照フィクスチャ検査が循環している。`XlsFixture#copyOf` が元セルを `Cell#toString()` で文字列化するのに対しリーダ側も `cell.toString()` を使うため、同じ関数を 2 回適用する恒等式になっている | **修正済み**。`copyOf` を削除し、テストソースに直書きした論理内容を権威とする非循環方式へ全面的に置き換えた（Evidence 2-4）。両方向のミューテーションで非循環であることを実証（Evidence 2-5） |
| Q-2 | Minor | `D1-12` が `PoiXlsReader#readOneLine` L123 の null セル分岐を通っていない（不在セルが行末のため `lastCellNum` が縮む） | **修正済み**。行の途中に穴を置く `readsAbsentCellInMiddleOfRowAsEmptyString` を追加し L123 を通した。両テストが行の使用範囲をアサートして経路の違いを示す（Evidence 3-2） |
| Q-3 | Minor | 発見済みの「全空行の黙殺」が `issues.md` に未記載 | **修正済み**。XLS-05 として記録（プローブ実測付き）。**修正はしていない**（`src/main` 無変更） |
| Q-4 | Minor | ブック名一意化の根拠（ブックキャッシュ衝突）が不正確 | **修正済み**。自分でもソースとプローブで確認し、`book` 引数を廃止して固定名にしたうえで根拠を書き直した（Evidence 3-4） |

### Craft (coding) — PASS with findings（Major 0 / Minor 3 / Nit 8）

| # | 重大度 | 指摘 | 対応 |
|---|---|---|---|
| C-1 | Minor | `Locale.setDefault` が JVM グローバルなのにクラス全 18 件へ掛かっている。必要なのは D1-06〜D1-08 の 3 件だけ | **修正済み**。`@EnglishLocale` 注釈で絞る `TestRule`（内部で `ExternalResource`）へ移した。実証は Evidence 3-3 |
| C-2 | Minor | 「TimeZone は固定不要」という根拠がコードに残っていない | **修正済み**。ルールの Javadoc に理由（POI の往復が同一既定 TZ を使い相殺する）と実測 TZ を明記 |
| C-3 | Minor | `@param book` のブックキャッシュ根拠が不正確 | **修正済み**（Q-4 と同一。Evidence 3-4） |
| C-4 | Nit | `XlsFixture.java:117` — `sheet()` 未呼び出しで `row()` すると素の NPE | **修正済み**。`IllegalStateException("call sheet() first")` を投げる（`@throws` も追記） |
| C-5 | Nit | `XlsFixture.java:187` — `catch (Exception)` が広すぎる | **修正済み**。`catch (IOException \| InvalidFormatException)` に絞った（POI 3.8 の `WorkbookFactory.create(InputStream)` の検査例外が この 2 つだけであることを `javap` で確認しコメントに残した） |
| C-6 | Nit | `XlsReferenceFixtureTest.java:129` — `getSheet(SHEET)` の null 未確認 | **修正済み**（形は変更）。当該箇所は非循環方式への置き換えで消滅した。ブックを開き直す処理は `XlsFormatReaderCellTypeTest#writtenSheet()` に移り、そこで `assertThat(sheet, is(notNullValue()))` を行っている |
| C-7 | Nit | `XlsFixture.java:35` — クラス Javadoc「1 ブック 1 シート」が実態と不一致 | **修正済み**。「`sheet()` は続けて呼べ、呼ぶたびにシートが末尾に追加される」に訂正 |
| C-8 | Nit | `checks/task-19.md:192-193` の「テストクラス側には POI の生 API が現れない（`FormulaError` を除く）」が不正確 | **修正済み**。読み取り専用で `Sheet`/`Row`/`Cell` を使う箇所がある旨に書き換えた（本ファイル「採用した形」） |
| C-9 | Nit | `XlsReferenceFixtureTest.java:70-74` の `Paths.get(url.toURI())` は将来リソースが jar 由来になると壊れる | **見送り**（レビューでも「対応は任意」）。現ビルドではテストリソースが常に `target/test-classes` へ展開されるため問題ない。見送る理由を `referenceDir()` の Javadoc にコメントとして残した |

### Verification (test) — 検証不足あり（18 件のミューテーションは 18/18 検出）

| # | 指摘 | 対応 |
|---|---|---|
| V-1 | 参照フィクスチャ検査が恒等式（実測: `row2 source=NUMERIC copy=STRING sourceToString=1.0 copyValue=1.0` → `EQUAL = true`） | **修正済み**（Q-1 と同一） |
| V-2 | `XlsFixture#number(v, fmt)` から `setCellStyle` を削除しても D1-17 は PASS のまま。表示形式 `@` の付与を検証していない | **修正済み**。書き戻して `getDataFormatString()` が `"@"` であることをアサートし、同じミューテーションで D1-17 が落ちることを確認（Evidence 3-1） |
| V-3 | D1-12 が L123 を通っていない | **修正済み**（Q-2 と同一。Evidence 3-2） |
| V-4 | 推奨 2「テストに直書きした論理内容から POI ブックを組み立てて比較する」 | **採用**（Evidence 2-4） |
| — | 既存 18 件のミューテーションは 18/18 検出（弱いアサーションなし） | 対応不要 |

### Design — 構造として妥当（いま直すべき点なし）

| # | 指摘 | 対応 |
|---|---|---|
| D-1 | `XlsFixture` の API は #20/#21 の要求（複数ブロック種別・FILE ブロック・複数シート・0 行ブロック・カラム名重複・行列数不一致・シート不在）を現行のまま表現できる。**API を作り変えないこと** | **対応不要**。API シグネチャは変更していない。今回の変更は (a) `sheet()` 未呼び出しガードの追加、(b) `open()` の catch 絞り込み、(c) 未使用になった `copyOf` の削除の 3 点のみで、いずれも組み立て API の形を変えていない |
| D-2 | 「ブック破損（#21 F1-02）」だけは表現できないが、テスト側で `Files.write(dir.resolve("broken.xlsx"), new byte[]{...})` の 1 行で足りる。**ビルダに壊れたブックを吐く責務を持たせないこと** | **対応不要**。`XlsFixture` に破損ブック生成 API は追加していない。#21 で必要になったらテスト側で 1 行で作る旨をここに記録する |
| D-3 | converter 経路ではブックキャッシュが使われない（プローブで実測） | **反映済み**（Q-4／C-3 と同一。こちらでも再現確認した。Evidence 3-4） |

## Overall Verdict

- Self-check: OK
- QA: PASS with findings（Major 1 / Minor 3）→ 全件対応済み
- Craft expert (coding): PASS with findings（Major 0 / Minor 3 / Nit 8）→ 見送り 1 件（C-9・理由をコードに記録）を除き対応済み
- Verification expert (test): 検証不足 3 件 → 全件対応済み。既存 18 件のミューテーションは 18/18 検出
- Design expert: 構造として妥当（いま直すべき点なし）→ 構造変更なし
- `mvn -o clean test -Djacoco.skip=true`: `Tests run: 337, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`
- `git status --porcelain -- src/main`: **出力なし**（src/main 無変更）
- Ready to check off: Yes（コーディネータ判断が要る事項は「未確認範囲を埋めるために参照フィクスチャを
  追加同梱するか」の 1 点。Evidence 2-6 と `issues.md`「未確認」節を参照）
