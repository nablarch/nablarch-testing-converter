# task-23 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 辺③について軸A の14種（`DEFAULT` を含む。辺③では到達可能）・軸B の4種・軸C の全フィールド（省略可能なものは省略時も）・軸E の 0／1／複数がすべてアサートされている | OK（**レビュー指摘により訂正のうえ再判定**） | **当初の OK は誤りだった。** レビュー指摘を自分で再現し、**軸A の A-12 `EXPECTED_REQUEST_BODY_MESSAGES`／A-13 `RESPONSE_HEADER_MESSAGES`／A-14 `RESPONSE_BODY_MESSAGES` が辺③で 1 つもアサートされていない**ことを確認した: `XlsFormatWriter#marker`（`src/main` L399-401）がこの 3 タイプだけ別文字列を返す変異を入れて全件実行すると、落ちたのは `RoundTripTest` の 3 件だけ（`Tests run: 425, Failures: 3`）で `XlsFormatWriterTest` 40 件は全緑だった。往復テストは Rules フェーズ2 により正式担保に数えないため**正式担保 0 = 未担保**である（唯一この 3 タイプを通していた `writesSequenceNoForAllSendSyncTypes` L818-835 がアサートするのは 4 タイプ共通の連番 `"1"` だけで、タイプを区別する出力を固定していない）。**テスト 3 件を追加して埋めた**: `XlsFormatWriterModelTest#writesExpectedRequestBodyMessagesMarker`（L325）／`#writesResponseHeaderMessagesMarker`（L345）／`#writesResponseBodyMessagesMarker`（L365）。粒度は A-11 の `XlsFormatWriterTest` L435 に揃え、グループ ID・識別子を含むマーカー全体をアサートする。**歯があることを同じ変異で実証**（再投入して `Tests run: 428, Failures: 6` ＝ 新規 3 件 ＋ `RoundTripTest` 3 件）。変異は毎回戻し `git diff HEAD -- src/main | wc -l` → 0。 **軸A 14/14（訂正後）。** ほかに #18 時点で空欄だった 3 件: A-01 `DEFAULT` → `writesDefaultDataTypeMarker`（L216）／A-07 `EXPECTED_FIXED` → `writesExpectedFixedFileBlockWithLengthRow`（L247）／A-09 `EXPECTED_VARIABLE` → `writesExpectedVariableFileBlockWithoutLengthRow`（L280）。**軸B 4/4** は #18 時点で ✅ 済みで #23 の対象ではない（`inventory.md` §3.3 の軸B 行は「（なし）」）。#23 自身が通すのは 3 種で `ListMapBlock` は通していない。**軸C 21/21（両状態）**: #18 時点で未担保だった 9 件を埋めた — C-02（L419）／C-04（L440）／C-08（L467）／C-09（L489）／C-12（L511）／C-13（L540）／C-15（L571）／C-17（L601）／C-18（L628）。**C-10 `FileDataBlock.fileType` はレビュー指摘を受けて変異で再確認した**: `layoutFile`（`src/main` L210）の `fixed` 判定を反転させると **往復でないテストが 8 件**落ちる（FIXED 側 6 件・VARIABLE 側 2 件）ため ✅。内訳は `inventory.md` §3.2 の C-10 直下の表。 **軸E 0／1／複数**: 0 件 3 要素を #23 が埋めた（E-1(0)＝C-04／E-2(0)＝C-09・C-18／E-3(0)＝C-12・C-15）。要素別の一覧は `inventory.md` **§3.1-3**（A-12〜A-14 を含めて更新）。 **ラウンド3 の訂正（2026-08-13）**: A-12〜A-14 の担保テストと台帳の「観測した版面」欄が「FW 制御ヘッダ行は無く」と書いており、**テストが確かめていない性質を観測事実として記録していた**。`XlsFormatWriter#layoutMessage` は `appendKeyValueRows(l, block.getFwHeaderFields())` をデータタイプで分岐せず無条件に呼んでおり、FW 制御ヘッダ行が出ないのは入力の `fwHeaderFields` が空 Map だからである。3 か所（アサートメッセージ 1・Javadoc 3 行・台帳 1 行）を「**本テストの入力が FW 制御ヘッダを持たない（空 Map）ため**次は名前行」へ改め、担保の穴として `inventory.md` §3.1-3 と `issues.md` **XLS-24** に開示した。穴であることは**変異で実証**した（`layoutMessage` を「送信系なら FW 制御ヘッダを出力しない」形＝ Javadoc が謳う性質を実装した形へ変異させて全件実行 → `Tests run: 428, Failures: 0` ＝ 1 件も落ちない＝区別するテストが存在しない）。**テストは足していない**（アサート内容・`@Test` 数は不変）。 **仕上げラウンド（2026-08-13・両レビュー PASS 後）**: この 3 件の `Then :` 行が「入力が空 Map のため次は名前行で、データ行の列 0 に連番が入る」と 1 文で書かれており、「ため」が連番にも係って読めた。**連番は `XlsDataTypeUtil.isSendSyncType` による送信系分岐の結果で `fwHeaderFields` が空であることとは無関係**（`layoutMessage` を読んで確認）のため 2 文に割り、`inventory.md` §3.1-3 の軸A 表 A-12 行も同じ形へ直した。同型の記述が残っていた `XlsFormatWriterTest#writesSendSyncMessageWithSequenceNo` の `Then :` 行も揃えた。アサート内容・`@Test` 数はここでも不変 | | |
| 辺③④の `DEFAULT` の扱いの非対称（辺③は書き出す／辺④は例外）が `issues.md` に記録され、かつ修正されていない | OK | `issues.md` の **XLS-20**（「`DataType.DEFAULT` の扱いが辺③と辺④で非対称で、辺③が書いたブロックは読み戻すと消える」）に記録した。表に 3 辺の実測を並べてある: 辺③ → `DEFAULT=T` を書き出す（`XlsFormatWriterModelTest#writesDefaultDataTypeMarker` L216）／辺④ → `IllegalArgumentException: unsupported DataType: DEFAULT`（既存 `YamlFormatWriterTest#serialize_unsupportedDataType_throws`。`YamlFormatWriter` L449 の `default:` を自分で読んで確認）／辺① → **ブロックが黙って消える**（`sections` 1 件・`blocks` 0 件。`#dropsDefaultDataTypeBlockWhenReadBack` L669。原因は本リポジトリの `TestCoreReaderAdapter` `HeaderCollector#parse` の `type == DataType.DEFAULT → continue`）。**修正していない**（`git diff HEAD -- src/main \| wc -l` → 0）。`inventory.md` §0.8-7・§3.2 軸A 表・§5.2・§5.3 の未解決 3 からも XLS-20 を参照している | | |
| #18 の棚卸し表で辺③に残っていた空欄が、埋まったか理由付きで残されたかのいずれかになっている | OK（**レビュー指摘により訂正**） | `inventory.md` §3.3 に「#23 後の状態」列を足した。**レビュー指摘を受けて A-12〜A-14 の 3 件を表に追加**し（#18 は ✅ と誤判定しており行として存在しなかった）、**要追加 0 ／ 担保済み 29 ／ 到達不能 0 ／ 対象外 1（計 30）**へ更新した。検算も表の下に置いた（担保済み: A 3 ＋ A 3 ＋ C 9 ＋ D 8 ＋ E 3 ＋ F 3 ＝ 29／対象外: F3-02 1／29＋0＋1 ＝ 30）。§5.1（#18 スナップショット）には表を書き換えずに補正値（辺③ 軸A は 3 ではなく 6、辺③合計 27→30、全体 107→110）を注記した。理由付きで残したのは **F3-02 `overwrite=false` 衝突の 1 件だけ**。**軸要素の外の担保の穴も開示を維持・追加した**: (1) `XlsFormatWriter#write` の `parent == null` 分岐（§3.1-2）、(2) #23 で JaCoCo で測った未到達 3 箇所（§3.1-3 末尾）、(3) **E-4(0 件) が `n/a` である理由**（§0.6 が E-4 に 0 件を定義していないため。C-02 の ✅ と矛盾に見える点への補足。§3.2 軸E 表の直後）。件数を動かした箇所はすべてコマンドから導き直した: `grep -c '^    @Test' …/XlsFormatWriterModelTest.java` → **18**、`grep -c 'Given:' 同` → **18**、`grep -c '^    @Test' …/XlsFormatWriterTest.java` → **40**、`…/XlsFormatWriterCellTypeTest.java` → **18**、`…/XlsFormatWriterInvalidOutputTest.java` → **16**、`grep -rn "new XlsFormatWriter(.*)\.write(" src/test --include=*.java | wc -l` → **20**（§3.1-2 の 19 は #23 で陳腐化）、`grep -rc "getCellType" src/test --include=*.java | wc -l` → **35**（同 `| grep -c ":0$"` → **32**。§3.2 の 34／32 は #23 で、ラウンド2 の 35／33 は `XlsFixture` の Javadoc に `getCellType()` の語を書いた時点で陳腐化した。台帳側も本コミットで 32 へ直した）（いずれも本コミット時点の実測。台帳の記載と一致） | | |
| src/main への変更がゼロ | OK | `git diff HEAD -- src/main \| wc -l` → **0**（2026-08-13。レビュー対応で `marker`／`layoutFile` に一時的な変異を 3 回入れたが、毎回 `git diff` で 0 行に戻したことを確認済み）。`git status --short` は `.rn/.../coverage/inventory.md`（M）と `src/test/.../XlsFixture.java`・`XlsFormatWriterTest.java`・`XlsFormatWriterModelTest.java`・`XlsFormatWriterInvalidOutputTest.java`（M）、本ファイル（??）のみ。**ラウンド3 で `layoutMessage` に 4 回目の変異**（送信系のとき `appendKeyValueRows(l, block.getFwHeaderFields())` を呼ばない形）を入れて XLS-24 の担保の穴を実証したが、これも戻して `git diff HEAD -- src/main \| wc -l` → **0** を再確認した。**仕上げラウンド（2026-08-13）でも変異は入れておらず、`git diff HEAD -- src/main \| wc -l` → 0**（変更したのは `src/test` の Javadoc／定数 3 ファイルと `coverage/` の 2 ファイル、本ファイル） | | |
| `mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` → `Tests run: 428, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`（2026-08-13・レビュー対応後）。#22 完了時点の 410 件 ＋ #23 の 15 件 ＋ レビュー対応の 3 件 ＝ 428。リグレッションなし（`XlsFormatWriterTest` 40・`XlsFormatWriterCellTypeTest` 18・`XlsFormatWriterInvalidOutputTest` 16・`XlsFormatReaderTest` 33 ほか、既存クラスの件数はいずれも不変）。**ラウンド3 の対応後も再実行して `Tests run: 428, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`**（2026-08-13）。ラウンド3 で変えたのは文言・Javadoc・台帳・課題一覧だけで、`@Test` の増減もアサート内容の変更もしていない。**仕上げラウンドの対応後も再実行して `Tests run: 428, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`**（2026-08-13）。件数は 428 のまま動いていない。**この 3 回はいずれも旧 `nablarch-testing-yaml` jar（`.m2` 2026-07-23 09:56 インストール分）を classpath に置いた実測である。**<br>**現行 jar（`.m2` 2026-08-13 17:04 インストール ＝ yaml `190cc9a` 差し戻し版）での再実測でも `Tests run: 428, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`。** 経緯と出典は下の「yaml jar 差し替えに伴う基準線の再取得」節 | | |

### yaml jar 差し替えに伴う基準線の再取得（2026-08-13・`#24` 着手前）

**現行の基準線（有効な記録）**

- **jar**: `nablarch-testing-yaml:1.0.0-SNAPSHOT` = yaml リポジトリ `190cc9a`「revert: `rows: []` の列名 DbInfo フォールバックを差し戻す」版。
  `.m2` インストール時刻 **2026-08-13 17:04**（`ls -la ~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/*.jar`）
- **コマンド**: `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true`
- **対象**: ワーキングツリー（HEAD `9b67b99`）。`git diff --stat bb58d05 HEAD -- src/` → **出力なし（`src/` 差分ゼロ）**。よって `bb58d05` に対する実測と同値
- **結果**: **`Tests run: 428, Failures: 0, Errors: 0, Skipped: 0`** / **`BUILD SUCCESS`**
- **7/23 版 jar での記録（428 件全 PASS）と同値**。件数・内訳とも一致した
- **`SampleConversionTest#convertsClimanSampleYamlToXls`**: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`（`target/surefire-reports/nablarch.test.tool.converter.SampleConversionTest.txt`）。
  `rows: []` を含むフィクスチャ 4 か所（`grep -rn 'rows: \[\]' src/test/java/nablarch/test/tool/converter/SampleConversionTest/` →
  `ClientActionTest/testShowWithEmptyClientTable.yaml:3` ／ `ClientActionTest/testFindNoClients.yaml:3` ／
  `ExportProjectsInPeriodActionRequestTest/testNormalEnd.yaml:173` ／ 同 `:199`）を通したうえで PASS しており、
  `YamlFormatReader#addTableBlocks` の `table.getColumnNames()` が `StubDbInfo` を叩かないことを実測で確認した
- **差し戻しの実物確認**: 現行 sources jar 内 `YamlTableDataBuilder.java` の `buildTableData` は
  `TableData td = new TableData(dbInfo, tableName, dataColumns.toArray(new String[0]), defaultValues);` の 1 経路のみで、
  列名未設定の分岐は存在しない。直前に FIXME コメント（「暫定で列名を未設定にして DbInfo フォールバックへ載せていたが、
  DB を持たない読み込み経路（変換ツール）を壊すため差し戻した。本体側の対応後に再検討する。」）が付いている

**残る jar 差分（converter への影響なし）**: 7/23 版に対して残る実コードの差は
`YamlMessageBuilder#buildSendSyncList` の `group_id` 照合のみ。converter は `buildSendSyncBodies` を通るため
この経路を実行しない（コーディネータ確認済み）。

**経緯（記録として保全。再開には使わない）**

2026-08-13 15:46 に一度 yaml `a966ab9` 版がインストールされ、その版での実測は
`Tests run: 428, Failures: 0, Errors: 1, Skipped: 0` / `BUILD FAILURE` だった。
失敗は `SampleConversionTest#convertsClimanSampleYamlToXls` の 1 件のみで、原因は下記のとおり
**yaml `a966ab9` の実装と converter の DB レス設計の衝突**であり converter 側の欠陥ではない。
yaml 側が `190cc9a` で差し戻した結果、上記のとおり解消した。converter 側は 1 行も変更していない。

<details>
<summary>`a966ab9` 版で発生していた失敗の詳細（解消済み）</summary>



`nablarch-testing-yaml:1.0.0-SNAPSHOT` が `.m2` 上で差し替わったため（`ls -la ~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/` →
`nablarch-testing-yaml-1.0.0-SNAPSHOT.jar` のタイムスタンプが **`8月 13 15:46`**）、
本ファイルの「428 件全 PASS」は旧 jar 基準の記録になった。classpath が変わった状態で取り直した実測を以下に記録する。

- **実行コマンド**: `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true`
- **対象**: ワーキングツリー（HEAD `fc59ef5`）。`git diff --stat bb58d05 fc59ef5` → `.rn/ntf-test-data-converter/steering.md | 7 ++++---` の 1 ファイルのみで、
  `src/` の差分はゼロ。よって `bb58d05` に対する実測と同値である
- **結果**: **`Tests run: 428, Failures: 0, Errors: 1, Skipped: 0`** / **`BUILD FAILURE`**
- **旧 jar 基準（本ファイル上部の記録）との差**: 総数 428 は不変（テストの増減なし）。`Errors` が **0 → 1** に変わった

**失敗したテスト**: `nablarch.test.tool.converter.SampleConversionTest#convertsClimanSampleYamlToXls`

**スタックトレース**（`mvn test -Dtest=SampleConversionTest -DtrimStackTrace=false` で取得。フレームワーク側は省略せず先頭 9 フレームを転記）:

```
java.lang.UnsupportedOperationException: DbInfo#getColumns must not be called on the DB-less converter read path.
	at nablarch.test.core.reader.StubDbInfo.notOnReadPath(StubDbInfo.java:32)
	at nablarch.test.core.reader.StubDbInfo.getColumns(StubDbInfo.java:49)
	at nablarch.test.core.db.TableData.getColumnNames(TableData.java:503)
	at nablarch.test.tool.converter.yaml.YamlFormatReader.addTableBlocks(YamlFormatReader.java:158)
	at nablarch.test.tool.converter.yaml.YamlFormatReader.addBlocksForSection(YamlFormatReader.java:109)
	at nablarch.test.tool.converter.yaml.YamlFormatReader.read(YamlFormatReader.java:91)
	at nablarch.test.tool.converter.YamlFormatHandler.read(YamlFormatHandler.java:45)
	at nablarch.test.tool.converter.TestDataConverter.convert(TestDataConverter.java:72)
	at nablarch.test.tool.converter.SampleConversionTest.convertsClimanSampleYamlToXls(SampleConversionTest.java:43)
```

**原因の連鎖（すべて一次情報で確認。converter は無変更）**:

1. 新 jar の `YamlTableDataBuilder#buildTableData` が、**データ列 0 件のとき列名を設定しない `TableData` を作る**。
   出典: `nablarch-testing-yaml-1.0.0-SNAPSHOT-sources.jar` 内 `YamlTableDataBuilder.java` L110-120。
   同 L112-113 のコメントが意図を明記している — 「列名を未設定（null）にして `TableData.getColumnNames()` の
   dbInfo フォールバックを効かせる。長さ 0 の配列を渡すと `loadData()` が DB を読まず、空テーブルの検証が素通りする。」
2. `TableData#getColumnNames()` は `columnNames == null` のとき `dbInfo.getColumns(tableName)` へフォールバックする。
   出典: `nablarch-testing-6-NEXT-SNAPSHOT-sources.jar` 内 `TableData.java`（`getColumnNames()` 本体）。
3. converter の `YamlFormatReader#addTableBlocks` は `table.getColumnNames()` を呼ぶ（`YamlFormatReader.java:158`）。
4. converter が渡す `DbInfo` は `StubDbInfo` であり、`getColumns` は設計上 `UnsupportedOperationException` を投げる
   （DB を持たない読み取り経路のため）。

つまり **新 jar が期待する dbInfo フォールバックと、converter の DB レス設計（`StubDbInfo`）が正面から衝突している。**
テスト側の書き方の問題ではない。

**入力側で当たっているケース**: `setup_tables` の `rows: []`（データ行 0 件 → データ列 0 件）。
`SampleConversionTest` のフィクスチャ内で該当するのは 2 ファイル —
`SampleConversionTest/ClientActionTest/testFindNoClients.yaml` と
`SampleConversionTest/ClientActionTest/testShowWithEmptyClientTable.yaml`（いずれも 3 行、`rows: []`）。
`ExportProjectsInPeriodActionRequestTest/testNormalEnd.yaml` にも `rows: []` が 2 か所あるが、
そちらは `expected_files` 配下でありテーブル系ではないため本経路を通らない。

**コーディネータの事前見立てとの差**: 「テーブルは `YamlTestCoreAdapterTest/tables.yaml` の 3 テーブルすべてが
列を持つため (2) の新分岐に入らない」は `YamlTestCoreAdapterTest` については正しいが、
`SampleConversionTest` のサンプル YAML が走査対象から漏れていた。**落ちた原因は事前に挙げられた (2) そのものである。**

**決着**: converter 側は 1 行も変更していない（`git diff HEAD -- src/` → 0 行）。
yaml 側が `190cc9a` で当該実装を差し戻し、`.m2` へ 2026-08-13 17:04 に再インストールされたことで解消した。
`rows: []` の期待値検証が素通りする（偽陰性）件は yaml 側で本体対応後に再検討される。**converter 側では扱わない。**

</details>

---

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
（steering Rules フェーズ2（往復テストの扱い））。置いた理由は #22 が生バイト検査 2 件を
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
| Verification approach meaningful to the objective (checks the right thing, not just "passed") | OK（ラウンド3・`b86ee3d`） | ラウンド1・2 は FAIL（いずれも台帳の記述精度）。**ラウンド3 で PASS。** 検証の中身: 担保テスト 15 件すべてに変異で歯があることを実証（9 種類の構造変異＝`marker` の 6 タイプ別文字列／`build` が sections 空でシート追加／`writeSection` が blocks 空で行追加／`layoutColumnRow` が空カラム名行を出さない・rows 空でダミー行／`appendRecords` が records 空でダミー行／`layoutMessage` の directives と fwHeader を入れ替え／`appendRecord` が fields 空で型行を出さない・rows 空でダミー行。**生存ゼロ**、かつ落ちたのは新規テストだけ）。台帳の併記コマンド 5 本を全再実行して記録値と完全一致。参照先・識別子を全数照合し実在しないもの 0 件。「到達不能」「対象外」の根拠（`HeaderCollector#parse` の `DEFAULT` → `continue`／`XlsFormatReader`・`YamlFormatReader` の `Collections.singletonList`／F3-02）も一次情報で確認。**`issues.md` XLS-24 の「未担保である」という主張自体を、記載どおりの変異（`layoutMessage` が送信系のとき `fwHeaderFields` を出力しない）で再現し `Tests run: 428, Failures: 0` ＝ 1 件も落ちないことを確認**。往復テストでしか通っていない担保済み要素は C-10(FIXED) のタグ漏れ 1 件のみで、実体は `writesFixedFileBlock` が非往復で担保（`8d7312c` で §3.1-3 に注記）。ラウンド2 の FAIL 要因（削除済み §3.1-4／§3.1-5 参照、「FW 制御ヘッダ行は無く」の観測事実書き）はいずれも `b86ee3d` で解消を確認 |

## Expert Reviews (axes the task needs)

### Craft Expert (coding)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Medium-specific best practice | OK（ラウンド3・`b86ee3d`） | ラウンド1・2 は FAIL。**ラウンド3 で PASS。** `TemporaryFolder` によりメソッド間で出力先が独立、静的可変状態なし、実行順依存なし、ロケール／TZ／OS 依存なし。`XlsFixture.open` は `InputStream` を try-with-resources で閉じる（POI 3.8 の `Workbook` に `close()` は無いため未クローズは不可避で、姉妹クラスと同一方針）。`causeOf` は `getCause()` の null を先に検査。JaCoCo をレビュア自身が取り直し、`XlsFormatWriter` 命令 8/782・分岐 3/100・行 1/151 未到達と未到達 3 箇所の内訳が台帳と一致 |
| Consistency with existing style | OK（ラウンド3・`b86ee3d`） | `Given:/When :/Then :` Javadoc ＋ `// Given` `// When` `// Then`、`@author`、1 ケース 1 `@Test`、行単位 `line(...)` 突き合わせのアサート粒度がいずれも `XlsFormatWriterTest`／`XlsFormatWriterCellTypeTest` と揃っている。ヘルパの写しは `XlsFixture` の Javadoc と `issues.md`「ヘルパ抽出の要否」で境界（POI を直接触るか否か）とともに「移さない」判断が明示され、根拠の実測値もレビュアの grep と一致。**PASS 後に残った記述精度 7 件は `8d7312c` で修正**（送信同期 Javadoc の係り受け 2 文化／`XlsFormatWriterTest` に残っていた「FW ヘッダ無し」1 行／`XlsFixture.EXTENSION` の別名参照を `".xlsx"` リテラルへ差し戻し ＋ `private` へ復帰／`XlsFixture` Javadoc の分類から `EXTENSION` を除去／steering Rules の非逐語引用を参照へ／`row` の Javadoc 圧縮／C-10(FIXED) タグ漏れの注記） |

### Verification Expert (test)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Artifact actually checked (tests run / claims verified / flow traced) | OK | `4905838` で PASS。以降 `src/` の差分はコメントのみ（コード行増減 0）につき再実行せず |
| Coverage (edge cases / claims / steps) | OK | 同上。`git diff --numstat 4905838 HEAD -- src/` → 106 追加 / 24 削除、うち非コメント行 **0**。`63c3f9b`..HEAD は `src/` 差分なし。変異テスト（22 変異・生存ゼロ）の結果が変わる余地がないため再実行しない（ユーザー判断・2026-08-13） |

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
  **レビュー ラウンド3 の対応（2026-08-13。テストのアサート内容も `@Test` の数も変えていない。
  変えたのは文言・Javadoc・台帳・課題一覧だけである）**:
  (A-1) 台帳の構造見直し（`c126856`）で削除済みの節を指す参照 3 か所を実在する場所へ直した
  （`XlsFormatWriterTest` と `XlsFormatWriterModelTest` の §3.1-4 → **§3.1 の末尾**、
  `XlsFixture` の §3.1-5 → **`issues.md` の「ヘルパ抽出の要否」節**。移送先に該当内容があることを
  現物を開いて確認した）。削除済み節（§1.2／§2.2／§3.2／§4.2／§3.1-4／§3.1-5）への参照が
  `src/` と `coverage/` に残っていないことを
  `grep -rnE '§(1\.2|2\.2|3\.2|4\.2|3\.1-4|3\.1-5)([^0-9-]|$)' src/ .rn/ntf-test-data-converter/coverage/`
  → **0 件**で確認した。
  (A-2) **テストが確かめていない性質を観測事実として記録していた 3 か所を訂正し、担保の穴として開示した**
  （詳細は上表 1 行目。`issues.md` **XLS-24** を新設し、`inventory.md` §3.1-3 に
  「担保の穴: 送信系の FW 制御ヘッダ」を追加。変異で `Tests run: 428, Failures: 0` を実測して穴を実証）。
  (B-1) 他ファイルの行番号を埋め込んでいた Javadoc 2 か所を識別子ベースへ直した
  （`XlsFixture` の `src/main L71` → `XlsFormatWriter` の `private static final String EXTENSION`。
  `XlsFormatWriterInvalidOutputTest` の 5 件の行番号は (B-3) で当該 Javadoc ごと撤去された）。
  (B-2) `XlsFormatWriterModelTest` のクラス Javadoc から `XlsFormatWriterTest` 40 件の内訳の数字を落とし、
  「同クラスは `build` と実ファイルが混在するが本クラスは全件が実ファイル経路」という本クラスの方針だけを残して
  内訳は §3.1 末尾へのポインタにした（3 重管理を 2 か所へ削減。自クラスの記述である
  `XlsFormatWriterTest` 側の内訳は残す）。
  (B-3) `XlsFormatWriterInvalidOutputTest` の `EXTENSION = XlsFixture.EXTENSION` を
  `= ".xlsx"` へ戻し、Javadoc も 1 行へ戻した（`XlsFixture.EXTENSION` の Javadoc 自身が
  「両者が一致していることを担保するものではない」と書いている定数から SUT の期待値を導いていたため）。
  併せて `XlsFixture#EXTENSION` の Javadoc から、もはや存在しない利用箇所の記述を落とした。
  (B-4) `XlsFormatWriterModelTest#row` の Javadoc 15 行を、姉妹メソッドと同じ 1 行へ縮めた。
  (C) **#27 への申し送り 2 件**（辺③の軸E で `E-1(1 件)`／`E-4(1 件)` が台帳のどこにも現れない／
  送信同期 4 種の担保が 2 クラスに分かれフィクスチャがほぼ複製）を `issues.md` に記録した。
  今回はテストも台帳の表も変えていない。
  **確認**: `mvn clean test -Djacoco.skip=true` → **428 件全 PASS**（`Failures: 0, Errors: 0, Skipped: 0`）／
  `git diff HEAD -- src/main | wc -l` → **0**／
  `grep -cE '\b(L[0-9]{1,4})\b' .rn/ntf-test-data-converter/coverage/inventory.md` → **0**／
  台帳に併記した導出コマンドはすべて実行して記載どおりの出力になることを確かめた
  （静的走査の perl は当初の版が誤った出力を返したため、書き換えて再実行した結果を載せている）。
  **仕上げラウンドの対応（2026-08-13。両レビュー PASS 後に残った「些細／記述精度」7 件。
  テストのアサート内容・`@Test` の数はいずれも変えていない）**:
  (1) 送信同期 3 件（`writesExpectedRequestBodyMessagesMarker`／`writesResponseHeaderMessagesMarker`／
  `writesResponseBodyMessagesMarker`）の `Then :` 行を 2 文に割った。旧文は「入力が空 Map のため次は名前行で、
  データ行の列 0 に連番が入る」で、「ため」が連番にも係って読めた。**連番は `XlsDataTypeUtil.isSendSyncType`
  による送信系分岐の結果であり `fwHeaderFields` が空であることとは無関係である**（`XlsFormatWriter#layoutMessage`
  を読んで確認。`appendKeyValueRows(l, block.getFwHeaderFields())` と
  `boolean sendSync = XlsDataTypeUtil.isSendSyncType(...)` は独立している）。`inventory.md` §3.1-3 の
  軸A 表 A-12 行も同じ形へ直した（A-13／A-14 は「同上」）。
  (2) 同型の記述が `XlsFormatWriterTest#writesSendSyncMessageWithSequenceNo` の Javadoc に 1 行残っていた
  （`Then : FW ヘッダ無し・データ行の列 0 に no（連番）。`）ため (1) と同じ形へ揃えた。
  (3) `XlsFormatWriterModelTest#write` が期待ファイル名を `XlsFixture.EXTENSION` から導いていたのを、
  自クラスの `private static final String EXTENSION = ".xlsx";` へ置き換えた
  （`XlsFormatWriterInvalidOutputTest` と同じ形。ラウンド3 の (B-3) がこちらに取り残していた）。
  併せて `XlsFixture#EXTENSION` を `private` へ戻し、但し書き段落を削った
  （`grep -rn "XlsFixture.EXTENSION" src/` → **0**）。
  (4) `XlsFixture` のクラス Javadoc 2 か所の「POI のブック・シートを直接触るヘルパ」の列挙から
  `{@link #EXTENSION}` を落とした（POI に触らない文字列定数であり、(3) で `private` に戻したため）。
  (5) steering Rules フェーズ2 を鉤括弧付きで「往復テストで担保を代替しない」と引用していた 3 か所
  （`XlsFormatWriterModelTest` クラス Javadoc・`inventory.md` §3.1-3 末尾・`issues.md` の #23 節）を
  「steering Rules フェーズ2（往復テストの扱い）」という参照へ改めた。`steering.md` の実文を開いて確認したところ、
  実文は後半に「ただし**既存**の往復テストが実ファイル経由で通している軸要素は、棚卸しに「🔺弱い担保」として
  必ず計上する」を持っており、鉤括弧の中身はその後半が落ちた要約だった。
  **本ファイル 42 行目にも同じ引用が残っているが、レビュー判定の外の prose 節であり
  今回の編集許可範囲（Completion Criteria 表の Self-check／Evidence 列と本行）外のため手を付けていない。**
  (6) `XlsFormatWriterModelTest#row` の Javadoc から「null を含められるよう」を落とした
  （本クラスの `row(...)` 呼び出し **14 か所**——`grep -o '\brow(' <同ファイル> | wc -l` → 15 から定義 1 を引いた数
  ——に `null` セルを渡すものは 1 つも無く、行使も検証もされていない性質だった）。
  (7) `inventory.md` §3.1-3 の軸C 表の直後に、§3.1 の表の C-10 タグの誤り 2 件を注記した
  （#7 `writesFixedFileBlock` の軸C 欄に `C-10` が欠けており C-10(FIXED) が往復テストでしか通っていないように
  見える／#38 の `**C-10(VARIABLE)**` の太字＝「唯一の担保」が #8 と重複していて誤り）。
  **C-10(FIXED) は穴ではない**: `writesFixedFileBlock` は長さ行 `["", "-", "5"]` を、
  `writesExpectedFixedFileBlockWithLengthRow` は `["", "5"]` を直接アサートしており、
  どちらも往復テストではない（両メソッドの本体を開いて確認した）。§3.1 は #18 時点のスナップショットのため
  表そのものは書き換えていない。
  **確認**: `mvn clean test -Djacoco.skip=true` → **`Tests run: 428, Failures: 0, Errors: 0, Skipped: 0`** /
  `BUILD SUCCESS`／`git diff HEAD -- src/main | wc -l` → **0**／
  `grep -cE '\b(L[0-9]{1,4})\b' .rn/ntf-test-data-converter/coverage/inventory.md` → **0**／
  `grep -rnE '§(1\.2|2\.2|3\.2|4\.2|3\.1-4|3\.1-5)([^0-9-]|$)' src/ .rn/ntf-test-data-converter/coverage/` → **0 件**／
  台帳に載っている件数の導出コマンド（`grep -c '^    @Test'` の 4 クラス分、`grep -c 'Given:'`、
  `grep -c 'EXPECTED_FIXED\|EXPECTED_VARIABLE'`、§3.1 末尾の awk）をすべて再実行し、記載どおり
  **18／18／40／0／18／16／`@Test=40 build=28 write=10 neither=2`** になることを確かめた。
- QA: **OK**（ラウンド3・`b86ee3d`。ラウンド1・2 は FAIL。担保テスト 15 件を変異で生存ゼロ確認、台帳の併記コマンド全再実行一致、XLS-24 の主張を変異で実証。残る指摘 4 件はすべて「些細／記述精度」で `8d7312c` にて対応）
- Design expert: N/A（既存クラス構成の中でテストクラスを 1 つ追加するタスクであり、構造・アプローチを新設していない。
  実 `.xlsx` を書いて開き直す方式は #22 の `XlsFormatWriterCellTypeTest` で確立済みのものを踏襲している）
- Craft expert (coding): **OK**（ラウンド3・`b86ee3d`。ラウンド1・2 は FAIL。参照先の実在を全数照合し 0 件の欠落、`@Test` 40 件の `build`／`write` 分類をメソッド本体 1 件ずつで再実測、JaCoCo を取り直して台帳と一致。残る指摘 3 件はすべて「些細／記述精度」で `8d7312c` にて対応）
- Verification expert (test): **OK**（`4905838` で PASS ＝ 22 変異・生存ゼロ。以降 HEAD まで `src/` の差分はコメント／Javadoc のみでコード行増減 0 のため再実行しない。ユーザー判断・2026-08-13。根拠: `git diff --numstat 4905838 63c3f9b -- src/` → 106/24 で非コメント行 0、`63c3f9b`..`b86ee3d` は `src/` 差分なし。`8d7312c` で `src/test` に入ったコード変更は `EXTENSION` 定数の定義位置のみで値・matcher・`@Test` 数は不変）
- Ready to check off: **Yes**。QA・Craft ともラウンド3 で PASS、Verification は上記の根拠で再実行免除。Completion criteria 5 項目すべて OK（両レビュアが独立に確認）。`src/main` 無変更（`git diff d9b03f6 8d7312c -- src/main` → 0 行）、`mvn clean test -Djacoco.skip=true` → 428 件全 PASS
