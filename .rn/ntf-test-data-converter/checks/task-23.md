# task-23 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 辺③について軸A の14種（`DEFAULT` を含む。辺③では到達可能）・軸B の4種・軸C の全フィールド（省略可能なものは省略時も）・軸E の 0／1／複数がすべてアサートされている | OK（**レビュー指摘により訂正のうえ再判定**） | **当初の OK は誤りだった。** レビュー指摘を自分で再現し、**軸A の A-12 `EXPECTED_REQUEST_BODY_MESSAGES`／A-13 `RESPONSE_HEADER_MESSAGES`／A-14 `RESPONSE_BODY_MESSAGES` が辺③で 1 つもアサートされていない**ことを確認した: `XlsFormatWriter#marker`（`src/main` L399-401）がこの 3 タイプだけ別文字列を返す変異を入れて全件実行すると、落ちたのは `RoundTripTest` の 3 件だけ（`Tests run: 425, Failures: 3`）で `XlsFormatWriterTest` 40 件は全緑だった。往復テストは Rules フェーズ2 により正式担保に数えないため**正式担保 0 = 未担保**である（唯一この 3 タイプを通していた `writesSequenceNoForAllSendSyncTypes` L818-835 がアサートするのは 4 タイプ共通の連番 `"1"` だけで、タイプを区別する出力を固定していない）。**テスト 3 件を追加して埋めた**: `XlsFormatWriterModelTest#writesExpectedRequestBodyMessagesMarker`（L325）／`#writesResponseHeaderMessagesMarker`（L345）／`#writesResponseBodyMessagesMarker`（L365）。粒度は A-11 の `XlsFormatWriterTest` L435 に揃え、グループ ID・識別子を含むマーカー全体をアサートする。**歯があることを同じ変異で実証**（再投入して `Tests run: 428, Failures: 6` ＝ 新規 3 件 ＋ `RoundTripTest` 3 件）。変異は毎回戻し `git diff HEAD -- src/main | wc -l` → 0。 **軸A 14/14（訂正後）。** ほかに #18 時点で空欄だった 3 件: A-01 `DEFAULT` → `writesDefaultDataTypeMarker`（L216）／A-07 `EXPECTED_FIXED` → `writesExpectedFixedFileBlockWithLengthRow`（L247）／A-09 `EXPECTED_VARIABLE` → `writesExpectedVariableFileBlockWithoutLengthRow`（L280）。**軸B 4/4** は #18 時点で ✅ 済みで #23 の対象ではない（`inventory.md` §3.3 の軸B 行は「（なし）」）。#23 自身が通すのは 3 種で `ListMapBlock` は通していない。**軸C 21/21（両状態）**: #18 時点で未担保だった 9 件を埋めた — C-02（L419）／C-04（L440）／C-08（L467）／C-09（L489）／C-12（L511）／C-13（L540）／C-15（L571）／C-17（L601）／C-18（L628）。**C-10 `FileDataBlock.fileType` はレビュー指摘を受けて変異で再確認した**: `layoutFile`（`src/main` L210）の `fixed` 判定を反転させると **往復でないテストが 8 件**落ちる（FIXED 側 6 件・VARIABLE 側 2 件）ため ✅。内訳は `inventory.md` §3.2 の C-10 直下の表。 **軸E 0／1／複数**: 0 件 3 要素を #23 が埋めた（E-1(0)＝C-04／E-2(0)＝C-09・C-18／E-3(0)＝C-12・C-15）。要素別の一覧は `inventory.md` **§3.1-3**（A-12〜A-14 を含めて更新） | | |
| 辺③④の `DEFAULT` の扱いの非対称（辺③は書き出す／辺④は例外）が `issues.md` に記録され、かつ修正されていない | OK | `issues.md` の **XLS-20**（「`DataType.DEFAULT` の扱いが辺③と辺④で非対称で、辺③が書いたブロックは読み戻すと消える」）に記録した。表に 3 辺の実測を並べてある: 辺③ → `DEFAULT=T` を書き出す（`XlsFormatWriterModelTest#writesDefaultDataTypeMarker` L216）／辺④ → `IllegalArgumentException: unsupported DataType: DEFAULT`（既存 `YamlFormatWriterTest#serialize_unsupportedDataType_throws`。`YamlFormatWriter` L449 の `default:` を自分で読んで確認）／辺① → **ブロックが黙って消える**（`sections` 1 件・`blocks` 0 件。`#dropsDefaultDataTypeBlockWhenReadBack` L669。原因は本リポジトリの `TestCoreReaderAdapter` `HeaderCollector#parse` の `type == DataType.DEFAULT → continue`）。**修正していない**（`git diff HEAD -- src/main \| wc -l` → 0）。`inventory.md` §0.8-7・§3.2 軸A 表・§5.2・§5.3 の未解決 3 からも XLS-20 を参照している | | |
| #18 の棚卸し表で辺③に残っていた空欄が、埋まったか理由付きで残されたかのいずれかになっている | OK（**レビュー指摘により訂正**） | `inventory.md` §3.3 に「#23 後の状態」列を足した。**レビュー指摘を受けて A-12〜A-14 の 3 件を表に追加**し（#18 は ✅ と誤判定しており行として存在しなかった）、**要追加 0 ／ 担保済み 29 ／ 到達不能 0 ／ 対象外 1（計 30）**へ更新した。検算も表の下に置いた（担保済み: A 3 ＋ A 3 ＋ C 9 ＋ D 8 ＋ E 3 ＋ F 3 ＝ 29／対象外: F3-02 1／29＋0＋1 ＝ 30）。§5.1（#18 スナップショット）には表を書き換えずに補正値（辺③ 軸A は 3 ではなく 6、辺③合計 27→30、全体 107→110）を注記した。理由付きで残したのは **F3-02 `overwrite=false` 衝突の 1 件だけ**。**軸要素の外の担保の穴も開示を維持・追加した**: (1) `XlsFormatWriter#write` の `parent == null` 分岐（§3.1-2）、(2) #23 で JaCoCo で測った未到達 3 箇所（§3.1-3 末尾）、(3) **E-4(0 件) が `n/a` である理由**（§0.6 が E-4 に 0 件を定義していないため。C-02 の ✅ と矛盾に見える点への補足。§3.2 軸E 表の直後）。件数を動かした箇所はすべてコマンドから導き直した: `grep -c '^    @Test' …/XlsFormatWriterModelTest.java` → **18**、`grep -c 'Given:' 同` → **18**、`grep -c '^    @Test' …/XlsFormatWriterTest.java` → **40**、`…/XlsFormatWriterCellTypeTest.java` → **18**、`…/XlsFormatWriterInvalidOutputTest.java` → **16**、`grep -rn "new XlsFormatWriter(.*)\.write(" src/test --include=*.java | wc -l` → **20**（§3.1-2 の 19 は #23 で陳腐化）、`grep -rc "getCellType" src/test --include=*.java | wc -l` → **35**（同 `| grep -c ":0$"` → **32**。§3.2 の 34／32 は #23 で、ラウンド2 の 35／33 は `XlsFixture` の Javadoc に `getCellType()` の語を書いた時点で陳腐化した。台帳側も本コミットで 32 へ直した）（いずれも本コミット時点の実測。台帳の記載と一致） | | |
| src/main への変更がゼロ | OK | `git diff HEAD -- src/main \| wc -l` → **0**（2026-08-13。レビュー対応で `marker`／`layoutFile` に一時的な変異を 3 回入れたが、毎回 `git diff` で 0 行に戻したことを確認済み）。`git status --short` は `.rn/.../coverage/inventory.md`（M）と `src/test/.../XlsFixture.java`・`XlsFormatWriterTest.java`・`XlsFormatWriterModelTest.java`・`XlsFormatWriterInvalidOutputTest.java`（M）、本ファイル（??）のみ | | |
| `mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` → `Tests run: 428, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`（2026-08-13・レビュー対応後）。#22 完了時点の 410 件 ＋ #23 の 15 件 ＋ レビュー対応の 3 件 ＝ 428。リグレッションなし（`XlsFormatWriterTest` 40・`XlsFormatWriterCellTypeTest` 18・`XlsFormatWriterInvalidOutputTest` 16・`XlsFormatReaderTest` 33 ほか、既存クラスの件数はいずれも不変） | | |

### Method（実行して記録してから固定する）の適用

期待値を先に置かず、まず一時プローブ（`src/test/.../ProbeTest.java`。2 回実行し、記録後に削除）で
15 要素すべてを `XlsFormatWriter` に通し、版面（行・セル）と読み戻し結果を標準出力へ出してから
アサートへ移した。**プローブでしか分からなかった事実が 5 件ある**（いずれも #23 の課題記録・台帳の中身になった）:

1. `DataType.DEFAULT` は識別セル `DEFAULT=T` として**そのまま書き出される**（グループ ID 付きなら `DEFAULT[g1]=T`）。
   例外にならない。しかし読み戻すと `blocks` が **0 件**になる（例外も警告も無い）— XLS-20
2. `sections` が空だと**シート 0 枚の `.xlsx` が書き出される**（ファイルは作られ、POI で開き直せる。
   `XlsFormatReader` で読むと `sheet not found.`）— XLS-23
3. `columnNames` が空だとカラム名行が**空セルだけの行**になり、読み戻すと空行として読み飛ばされて
   **データ行がカラム名へ昇格**する（`columnNames=[V1, V2]`・`rows=[]`。値が消える）— XLS-21
4. `RecordLayout.fields` が空だと**読み戻せない `.xlsx`** になる
   （`can't get data` ← `directive or data names row must have two columns at least. [data]`）— XLS-22
5. 上記以外の「空」（C-04／C-09／C-12／C-15／C-18）は例外にも情報欠落にもならず、
   対応する行が出ないだけで読み戻しても空コレクションに戻る（＝往復が安定する）。
   `MessageDataBlock.directives`（C-13）は FW 制御ヘッダ行より**上**に出て、読み戻すと
   `directives` と `fwHeaderFields` に正しく分かれる

3 と 4 と 5 は「空にすると何が起きるか」を予想で書いていたら取り違えていた
（3 は「空のカラム名行がそのまま残る」、4 は「フィールドが無いだけの版面になる」と書きかねなかった）。

記録した挙動のうち**妥当でないと判断した 4 件を `issues.md` に課題として記録し、`src/main` は変更していない**。
掲載順は「凡例 → 並び順の原則」に従い検出できないものを先に置いた: XLS-21（データが消える・検出できない）→
XLS-20（ブロックが消える・検出できない）→ XLS-22（loud に失敗する）→ XLS-23（記録のみ）。

**読み戻しの側にもテストを置いた（3 件）。** `#dropsDefaultDataTypeBlockWhenReadBack`（L669）／
`#promotesFirstDataRowToColumnNamesWhenEmptyColumnNamesAreReadBack`（L695）／
`#failsToReadBackRecordWithoutFields`（L722）。これらは**軸要素の担保に数えていない**
（steering Rules フェーズ2 の「往復テストで担保を代替しない」）。置いた理由は #22 が生バイト検査 2 件を
置いたのと同じで、本体パーサ・`PoiXlsReader` の挙動が変わったときに**担保テストは緑のまま
`issues.md` の XLS-20／XLS-21／XLS-22 の記述だけが誤りになる**状態を防ぐためである。
この扱いはクラス Javadoc・`inventory.md` §3.1-3・`issues.md` の #23 節の 3 箇所に明記した。

### 追加テストのカバレッジ

取得手順は steering の Decisions どおり
（`mvn clean jacoco:instrument test jacoco:restore-instrumented-classes` → `mvn jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec`）。
`target/site/jacoco/nablarch.test.tool.converter.xls/` の HTML から抽出（2026-08-13・#23 完了後）。

| クラス | 命令 | 分岐 | 未到達行 |
|---|---|---|---|
| `XlsFormatWriter` | **98%**（missed 8 / 782） | **97%**（missed 3 / 100） | 1 / 151 |
| `BlockLayout` | **100%** | **100%** | 0 / 26 |
| （パッケージ全体） | 93%（missed 176 / 2,629） | 90%（missed 28 / 293） | 12 / 478 |

**#22 完了時点（`checks/task-22.md` の計測）と数値は同じである**（INSTRUCTION covered 774 ／ missed 8、
BRANCH covered 97 ／ missed 3、LINE covered 150 ／ missed 1）。#23 が足したのも到達行ではなく
**アサートする性質**（残り 3 データタイプの識別セル・空コレクションの版面・多重度 0）であり、
`XlsFormatWriter` の行・分岐は #21 時点で既に到達済みだった。

**未到達 3 箇所（いずれも #23 の軸要素ではない）**:

| 位置 | 内容 | 未到達の理由 |
|---|---|---|
| L104 分岐 1/2 | `if (parent != null)` の `null` 側 | `basePath` が空文字のときだけ成立する。到達経路の全数調査は `inventory.md` §3.1-2（#22 が開示済み）。#23 でも埋めていない（軸要素ではないため） |
| L171 分岐 1/2 ・ L174（1 行） | `layout` の「どのブロック型にも当てはまらない」経路 ＋ `throw new IllegalArgumentException("unsupported block: …")` | `TestDataBlock` は sealed で具象 4 種（`TableDataBlock` / `ListMapBlock` / `FileDataBlock` / `MessageDataBlock`）だけを permit し、すべて分岐が用意されている。現行の型階層では到達不能な安全網（steering #6 の判断と同じ思想） |
| L410 分岐 1/6 | `isMarkerColumn` の `columnName != null` の `null` 側 | steering #9 でコメント済みの防御ガード。`layoutColumnRow` L189 のコメントが「カラム名が `null` の場合は `isMarkerColumn` 内で `null` チェックして `false` を返す」と明記している |

`inventory.md` §3.1-3 の末尾に同じ表を置き、§3.3 から相互参照した。

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Verification approach meaningful to the objective (checks the right thing, not just "passed") | | |

## Expert Reviews (axes the task needs)

### Craft Expert (coding)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Medium-specific best practice | | |
| Consistency with existing style | | |

### Verification Expert (test)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Artifact actually checked (tests run / claims verified / flow traced) | | |
| Coverage (edge cases / claims / steps) | | |

## Overall Verdict

- Self-check: OK（2026-08-13・**レビュー対応後に再判定**）。#18 の棚卸しが辺③に残していた 15 要素
  （軸A 3・軸C 9・軸E 3）を `XlsFormatWriterModelTest`（12 件）で埋め、`issues.md` の主張を腐らせないための
  読み戻し検査 3 件（軸要素の担保には数えない）を置いた。**加えてレビュー指摘の担保の穴を自分で再現し、
  A-12／A-13／A-14 の識別セルを固定するテスト 3 件を追加した**（変異で穴と歯の両方を実証。詳細は上表 1 行目）
  ＝ 計 18 `@Test`。`@Test` 18 ＝ `Given:` 18 ＝ `When :` 18 ＝ `Then :` 18 を `grep -c` で確認。
  C-10 も変異で再確認し、往復でないテスト 8 件が落ちることから ✅ と判定した。
  #22 から持ち越されていた**ヘルパ抽出の要否も判断して記録した**（`line` / `cell` を `XlsFixture` へ抽出、
  `row` / `map` / `container` / 往復ヘルパは現状維持。判断は `XlsFixture` クラス Javadoc、実測とコマンドは
  `inventory.md` §3.1-5）。台帳の事実誤り 3 件（§3.1-3 の「40 件は `build` を見る」・§3.2 の C-14／C-15 件数・
  §3.2 の getCellType 34／32）も実測で訂正し、#23 のファイル追加で陳腐化していた §3.1-2 の 19 か所も
  20 か所へ直した。現状挙動はすべて**先に実行して観測してから**固定し、妥当でないと判断した 4 件を
  `issues.md` **XLS-20〜XLS-23** に記録した（`src/main` は無変更。`git diff HEAD -- src/main | wc -l` → 0）。
  `mvn clean test -Djacoco.skip=true` → **428 件全 PASS**（`Failures: 0, Errors: 0, Skipped: 0`）。
  台帳の件数はすべてコマンドから導き直し、併記したコマンドを実行して一致を確かめた。
  **レビュー ラウンド2 の対応（2026-08-13。テストのアサート内容は 1 行も変えていない）**:
  (1) §3.1-4 の「`build` だけを見るのは 30 件（40 − 10）」という**引き算で出した数字を撤回**し、
  実測（**実ファイル 10 ／ `build` 28 ／ SUT のブックを作らない 2**）と導出コマンドへ差し替えた
  （同じ内訳を書いていた 2 クラスの Javadoc も直した）。
  (2) §3.2 が `cell` / `line` を「`XlsFormatWriterTest` L100-107／L110-121・現在も変わっていない」と
  書いていたのを訂正した（挙動は不変だが定義位置は `XlsFixture` L240／L263 へ移動済み。§0.8-4 は時点を明記）。
  (3) §0.1 の印字ブロックを現在の実測へ更新した（`XlsFormatWriterTest.java` 1080 → **1076** 行）。
  (4) 更新サマリ（§0 冒頭）の「§3.3 … 担保済み 26」を **29** へ直し、台帳内の同じ数字を全数一致させた。
  (5) `XlsFixture#cell` / `#line` に `@throws IllegalStateException`（文字列セル専用）を明記し、
  `{@link Arrays#asList}` を `{@code Arrays.asList}` へ直した（`javadoc -private -Xdoclint:reference` の
  xls パッケージ唯一のエラーが消えることを実行して確認）。クラス Javadoc の要約・境界も実装に合わせた。
  (6) `EXTENSION` の「定義は 1 か所」を実態（`xls` パッケージに `.xlsx` リテラルが他に 4 か所）へ書き直した。
  (7) §3.1-5 に `causeOf` の写し 1 件を追加し、`container` を抽出しない理由を循環していない根拠へ置き換えた。
  (8) **辺③では軸B（B-1 `TableDataBlock` ／ B-2 `ListMapBlock`）が軸A から独立していない**ことを
  §3.2／§3.3 に開示した（`XlsFormatWriter#layout` L166-175 を読み、両者が同じ `layoutColumnRow` を
  通ることを確認。判定 ✅ 4/4 と件数 0 は変えていない）。
  (9) 本ファイルの「Method の適用」節・Completion Criteria の Evidence 列の行番号を現物へ合わせた。
  ラウンド2 の編集で自分が陳腐化させた行番号（クラス Javadoc の増減で `XlsFormatWriterTest` は +13、
  `XlsFormatWriterModelTest` は +12 ずれた）も全数取り直した。
- QA:
- Design expert: N/A（既存クラス構成の中でテストクラスを 1 つ追加するタスクであり、構造・アプローチを新設していない。
  実 `.xlsx` を書いて開き直す方式は #22 の `XlsFormatWriterCellTypeTest` で確立済みのものを踏襲している）
- Craft expert (coding):
- Verification expert (test):
- Ready to check off:
