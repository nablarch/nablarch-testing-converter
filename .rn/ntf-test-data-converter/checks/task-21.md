# task-21 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 軸E の3観点（セクション内ブロック数／ブロック内行数／ファイル内レコードレイアウト数）それぞれで 0／1／複数がアサートされている。「ブック内シート数 複数」は到達不能として根拠付きで空欄に残されている | OK | **9 組すべてを実 `.xlsx` 経路でアサート**（`XlsFormatReaderRealFileTest`）。0 件の 3 組と E-3(複数) は #21 が新規担保（+6 メソッド）。E-4(複数) は `XlsFormatReader#read` L133 が `Collections.singletonList(section)` を返すため到達不能として空欄。E-1(0/1/複数)・E-2(1/複数)・E-3(1)・E-4(1) が実 `.xlsx` 経路で担保済みであることも 1 件ずつ確認した（詳細は Evidence 1） | | |
| 軸F の6ケース（シート不在／ブック破損／未知データタイプ名／マーカーカラム欠落／カラム名重複／行列数不一致）すべてで、例外型・メッセージまたは継続時の結果がアサートされている | OK | 5 ケースを新規クラス `XlsFormatReaderInvalidInputTest`（13 `@Test`。うち 12 件が軸F、1 件が XLS-15 の根拠）が担保。F1-05 カラム名重複は #16 の `XlsFormatReaderTest` 4 件で担保済み（詳細は Evidence 2） | | |
| C-17／C-20 の「到達不能」が F1-06 のテストで実行可能な根拠を持ち、`issues.md` の「到達不能」表がそのテストメソッド名を参照している | OK | 3 メソッドを追加し、`issues.md` の「到達不能」表の C-17 行・C-20 行から参照（C-11／C-13／C-16 と同じ書式）。前任者が `issues.md:398`・`:399` に記録した期待値は鵜呑みにせず自分で実行して確認した（詳細は Evidence 3） | | |
| src/main への変更がゼロ | OK | `git diff -- src/main` → 出力なし。`git status --porcelain -- src/main` → 出力なし（詳細は Evidence 5） | | |
| `mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | `Tests run: 373, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`（作業前 354 → +19。リグレッションゼロ） | | |

---

### Evidence 1 — 軸E（4 観点 × 多重度）× 担保テストメソッド

追加は 2 ファイル。

| ファイル | 追加 `@Test` | 役割 |
|---|---|---|
| `src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderRealFileTest.java` | +6（17 → **23**） | 軸C の空コレクション（C-09／C-12／C-15／C-18）＝軸E の 0 件、＋ E-3(複数) |
| `src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderInvalidInputTest.java` | **13**（新規） | 軸F 5 ケース ＋ C-17／C-20 到達不能の根拠 ＋ XLS-15 の根拠 |

**全 9 組が実 `.xlsx` 経路（`XlsFormatReaderRealFileTest`。すべて `new XlsFormatReader()` ＋ `XlsFixture` の実ファイル）でアサートされている。**
表の `#` はすべて `XlsFormatReaderRealFileTest` のメソッドである。

| 観点 | 0 件 | 1 件 | 複数件 |
|---|---|---|---|
| E-1 セクション内ブロック数 | `#readsEmptyBlockListFromRealSheetWithoutMarkers`（#20。`getBlocks()` が空リスト） | `onlyBlock` ヘルパが `blocks.size()==1` をアサートする各テスト（#20） | `#readsFourBlockImplementationsFromOneRealSheet`（`kinds.size()==4`）／`#readsAllFourSendSyncMessageTypesFromRealBook`（`blocks.size()==4`）（#20） |
| E-2 ブロック内行数 | **`#readsEmptyRowsFromTableWithoutDataRowsInRealBook`（#21）／`#readsEmptyRowsFromListMapWithoutDataRowsInRealBook`（#21）** | `#readsExpectedTableBlockWithGroupIdFromRealBook`（1 行）／`#readsListMapBlockFromRealBook`（1 行）（#20） | `#readsSetupTableBlockFromRealBook`（2 行）（#20） |
| E-3 ファイル内レコードレイアウト数 | **`#readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook`（#21・ファイル系）／`#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook`（#21・メッセージ系）** | `#readsSetupFixedFileBlockFromRealBook`／`#readsMessageBlockFromRealBook`（いずれも `records.size()==1`）（#20） | **`#readsMultipleRecordLayoutsFromOneFixedFileInRealBook`（#21・コーディネータ指示で追加）** |
| E-4 コンテナ内セクション数 | n/a | `#readsContainerAndSectionNamesFromRealBookAndSheetNames`（#20。`sections.size()==1`） | **到達不能** — `XlsFormatReader#read` L133 が `Collections.singletonList(section)` を返す 1 シート単位 API（L96-101 の Javadoc）。テストは書かず空欄 |

加えて C-18（レコードレイアウト内の値行 0 件）を `#readsEmptyRowsFromRecordLayoutWithoutValueRowsInRealBook` が担保する
（軸E の 4 観点には対応する要素が無いが、#20 が #21 へ送った 11 件に含まれる）。

**E-3(複数) の追加（コーディネータ指示）**: #18 の棚卸しは Fake 経路の
`XlsFormatReaderTest#readRestoresMultipleRecordLayoutsInFixedFile` をもって ✅ と判定していたため、
#20 が #21 へ送った 11 件に入っていなかった。実 `.xlsx` 経路では空欄であり、
`XlsFormatReader#toRecordLayouts` の**断片ループ**（`verifyNameRow` による位置合わせ）を実ファイルで通す
唯一の経路になるため追加した。入力の論理内容は Fake 経路テストを参考にしたが、**期待値は流用せず
プローブで観測した結果を固定**した（`Probe3`。断片 2 件・3 件の両方を観測）。

**#18 の ✅ が Fake 経路由来だった軸E 要素の洗い直し**（コーディネータ指示 2）: E-1(0/1/複数)・E-2(1/複数)・
E-3(1)・E-4(1) は上表のとおり実 `.xlsx` 経路のアサートを 1 件ずつ確認した。**実ファイル経路で空欄だったのは
E-3(複数) の 1 件だけ**であり、他に同じ穴は無かった。

**E-3(複数) はメッセージ系では到達不能**である。本体 `MessageParser` が生成する `FixedLengthFileParser` は
`onReadingValues` を上書きし、先頭セルが非空でも新しい断片を作らない（送信同期の `no` 列に合わせた仕様）。
2 つ目の名前行・型行・長さ行は 1 つ目の値行として吸収される。実測して `issues.md` の **XLS-15** に記録し、
根拠テスト `XlsFormatReaderInvalidInputTest#absorbsSecondNameRowAsDataRowInMessageBodyInRealBook` を置いた。
E-3 は（観点, 多重度）単位で数えるため、ファイル系での担保をもって ✅ とする。

### Evidence 2 — 軸F（6 ケース）× 担保テストメソッドと観測した挙動

`XlsFormatReaderInvalidInputTest` は全メソッドが `XlsFixture` で組み立てた実 `.xlsx`（または意図的に壊した
ファイル）を `new XlsFormatReader().read(...)`（本番配線＝`PoiXlsReader`）へ渡す。`FakeTestDataReader` は
経由しない。

| 要素 | 担保テストメソッド | 観測した挙動（アサート内容） |
|---|---|---|
| F1-01 シート不在 | `failsWithSheetNotFoundWhenSheetIsAbsentFromRealBook` | `IllegalArgumentException`。メッセージ `sheet not found. path=[...絶対パス...] sheet=[noSuchSheet]`。**原因例外なし**（`reader.open` が `TestDataParsingTemplate` の try の外側で呼ばれるため `can't get data.` に包まれない） |
| F1-02 ブック破損 | `failsWithGenericRuntimeExceptionWhenWorkbookIsBroken` | `java.lang.RuntimeException`（サブクラスではなく汎用型そのもの）／メッセージ `test data file open failed.`／原因は POI の `IllegalArgumentException: Your InputStream was neither an OLE2 stream, nor an OOXML stream`。**連鎖するどのメッセージにもファイル名が無い**（`issues.md` **XLS-14**） |
| F1-03 未知のデータタイプ名 | `ignoresBlockWhoseMarkerHasUnknownDataTypeNameInRealBook` | 例外にならず継続。`UNKNOWN_TYPE=X` のブロックはマーカー行・カラム行・データ行とも中間モデルに現れず、同居する `SETUP_TABLE=T` だけが読まれる（`issues.md` **XLS-10**） |
| F1-03（続き） | `readsSuffixAfterKnownDataTypeNameAsGroupIdInRealBook` | `SETUP_TABLEX=T` は `SETUP_TABLE_DATA` ＋ `groupId="X"`（角括弧なし）として読まれる（`issues.md` **XLS-11**） |
| F1-04 マーカーカラム欠落 | `readsMarkerColumnWithoutBracketsAsOrdinaryDataColumnInRealBook` | 例外にならず継続。`[no]` ではなく `no` と書いた列は除外されずデータ列 `columnNames=[no, A]` になる |
| F1-04（続き） | `dropsFirstFieldWhenSendSyncMetaColumnIsMissingInRealBook` | 例外にならず継続。送信同期でメタ列 `no` を欠くと `recordType="requestId"`・`fields=[userId]`・`rows=[[user01]]` となり、**先頭フィールドと値が消える**（`issues.md` **XLS-13**） |
| F1-05 カラム名重複 | （#16）`XlsFormatReaderTest#readListMapWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins` ほか 3 件 | 後勝ちで除去＋WARN ログ（本タスクでは追加しない） |
| F1-06 行と列の数の不一致 | `padsShortDataRowAndDropsCellsBeyondColumnRowInRealBook` | テーブル: 短い行は `""` 埋め（`[[a1, ""]]`）、長い行は超過セルを切り捨て（`[[c1, d1]]`。`e1` が消える。`issues.md` **XLS-12**） |
| F1-06（続き） | `padsShortValueRowAndDropsCellsBeyondNameRowInFixedFileInRealBook` | 固定長ファイルの値行でも同じ（`[[abc, ""]]` ／ `[[xyz]]`。`extra` が消える） |
| F1-06（続き） | `failsWhenLengthRowIsShorterThanNameRowInRealBook` | `IllegalStateException: can't get data. ...` ＋ 原因 `IllegalArgumentException: field name size is 2. but lengths size is 1.` |
| F1-06（続き） | `failsWhenNameRowHasOnlyRecordTypeCellInRealBook` ／ `failsWhenTypeRowIsShorterThanNameRowInRealBook` ／ `failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook` | Evidence 3 参照 |

**継続する異常系と例外になる異常系の切り分け**（観測結果）:

- 例外になる: シート不在・ブック破損・**名前行／型行／長さ行**の要素数不整合
- 例外にならず継続する: 未知のデータタイプ名・マーカーカラム欠落・**値行**の要素数不整合

この非対称（値行だけは黙って埋める／捨てる）が `issues.md` XLS-12 の中身である。

### Evidence 3 — C-17／C-20「到達不能」の実行可能な根拠（レビュー指摘由来）

3 入力を実行し、例外型とメッセージをアサートした。`issues.md:398`・`:399` に前任者が記録していた期待値は
根拠にせず、**自分でプローブを実行して観測した結果**をアサーションにした（Evidence 4）。結果は前任者の記録と一致した。

| 入力 | 根拠づける要素 | 担保テストメソッド | 観測した例外（外側 → 原因） |
|---|---|---|---|
| 名前行が 1 列（レコード種別セルのみ）。`SETUP_FIXED` と `MESSAGE` の 2 ブックで確認 | C-17 `RecordLayout.fields` 空 | `failsWhenNameRowHasOnlyRecordTypeCellInRealBook` | `IllegalStateException: can't get data. ...` → `IllegalStateException: directive or data names row must have two columns at least. [data]`（本体 `DataFileParser` L234） |
| 型行が名前行より短い（フィールド 2・型 1） | C-20 `FieldDef.type` 省略（`null`）根拠① | `failsWhenTypeRowIsShorterThanNameRowInRealBook` | `IllegalStateException: can't get data. ...` → `IllegalArgumentException: field name size is 2. but types size is 1. FixedLengthFileFragment{...}`（本体 `DataFileFragment#assertSameSizeAsNames` 宣言 L339／`throw` L342／呼び出し `setTypes` L203） |
| 型セルが中間位置で空 | C-20 根拠② | `failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook` | `IllegalStateException: can't get data. ...` → `IllegalArgumentException: can't convert value []. convert table ={...}`（本体 `BasicDataTypeMapping#convertToFrameworkExpression` L69） |

- `issues.md` の「到達不能」表 C-17 行・C-20 行に **担保テストメソッド名を追記**した（C-11／C-13／C-16 と同じ書式）。
- `inventory.md` §1.3 の C-17 行・C-20 行にも同じテストメソッド名を追記した。
- メッセージのアサートは環境非依存の部分文字列のみ。理由: 外側メッセージは一時ディレクトリの絶対パスを含み、
  根拠②の `convert table =` は `HashMap` 由来で並び順が変わる（実測で確認）。

### Evidence 4 — 「期待値を先に決めていない」ことの根拠

手順は「①プローブで観測 → ②妥当性を判断 → ③観測どおりに固定 → ④妥当でないものを `issues.md` へ」である。

1. **観測**: スクラッチのプローブクラス（`Probe` / `Probe2`。作業ディレクトリ外に置き、リポジトリには含めない）で
   11 件＋3 入力＋派生ケース計 **39 入力**（`Probe` 33 ＋ `Probe2` 3 ＋ `Probe3` 3）を実 `.xlsx` に書き出して `read` し、
   返り値または例外連鎖を標準出力へ落とした。
   この段階ではアサーションを 1 つも書いていない。
2. **判断**: 観測結果を「妥当」「不適切」に仕分けた。不適切と判断した 6 件を `issues.md` の XLS-10〜XLS-15 に記録し、
   **修正はしていない**（`src/main` 無変更）。妥当と判断した 7 件は `issues.md`「課題としないと判断した観測結果（#21）」に列挙した。
3. **固定**: テストのアサーションは観測値そのままである。あるべき姿（例: 「はみ出したセルは WARN を出すべき」）は
   アサートしていない。
4. 前任者の記録（`issues.md:398`・`:399`）は手掛かりとしてのみ使い、値は自分の実行結果で確かめた。

**観測してはじめて分かった（事前には書けなかった）挙動**の例:

- `SETUP_TABLEX=T` が `SETUP_TABLE` ＋ グループ ID `X` として読まれる（角括弧の検証が無い）
- 送信同期のメタ列欠落で先頭フィールドと値が落ちる
- 値行の超過セルは黙って捨てられるのに、型行・長さ行の超過／不足は例外になる
- ブック破損時の例外にファイル名が入らない（シート不在は入る）

### Evidence 5 — src/main 無変更

```sh
$ git diff -- src/main
（出力なし）
$ git status --porcelain -- src/main
（出力なし）
```

作業ツリーの変更は次の 4 ファイルのみ（コミットはしていない）。

```
 M .rn/ntf-test-data-converter/coverage/inventory.md
 M .rn/ntf-test-data-converter/coverage/issues.md
 M src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderRealFileTest.java
?? src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderInvalidInputTest.java
```

### Evidence 6 — テストクラスを分けた判断

| 追加先 | 内容 | 理由 |
|---|---|---|
| 既存 `XlsFormatReaderRealFileTest` に +6 | C-09／C-12／C-15／C-18（＝E-2(0)／E-3(0)）＋ E-3(複数) | **正常系のアサーションであり、同クラスが担う軸C の「非空」側と対になる**。同じ入力形式・同じヘルパ（`onlyBlock`／`fieldNames`／`assertDirectiveCount`）をそのまま使え、同クラスの Javadoc が「この 4 要素は #21 送り」と明記していた箇所を実測結果へ置き換えられる |
| 新規 `XlsFormatReaderInvalidInputTest` 13 件 | 軸F 5 ケース ＋ C-17／C-20 の根拠 ＋ XLS-15 の根拠 | **アサーション対象が例外型・例外メッセージ・例外連鎖という別種**であり、入力も意図的に壊したものだけを置く。専用ヘルパ（`causeOf` / `allMessagesOf` / `assertWrappedByCannotGetData`）が要り、正常系クラスへ混ぜると読み手が「壊れた入力」と「正しい入力」を取り違える。#19（`XlsFormatReaderCellTypeTest`）・#20（`XlsFormatReaderRealFileTest`）と同じく観点でクラスを分ける方針に沿う |

### Evidence 7 — `inventory.md` の更新と検算

- §1.2-2: テストクラス表に `XlsFormatReaderInvalidInputTest`（12）を追加し、`XlsFormatReaderRealFileTest` を
  「23（#20 が 17 ＋ #21 が 6）」へ更新（`XlsFormatReaderInvalidInputTest` は 13）。軸C 表に C-09／C-12／C-15／C-18 の 4 行を追加。**軸E 表・軸F 表を新設**。
- §1.3: C 4 件・E 2 件・F 5 件を「担保済み（#21）」へ変更し、C-17／C-20 の到達不能行に根拠テスト名を追記。
- **検算**（表の「件数」列を実際に足した）:
  - 担保済み: 3（A）＋1（C-06）＋1（C-08）＋4（C-09/12/15/18）＋17（D）＋2（E 0 件）＋5（F）＝ **33**
  - 到達不能: 1（A-01）＋2（C-11/C-13）＋1（C-16）＋1（C-17）＋1（C-20）＋1（C-02）＋1（E-4 複数）＝ **8**
  - 要追加: **0**／総計 33＋8＝**41**（#18 時点の 41 と一致。B は 0 件）
- §5.1 は #18 基準のスナップショットのため**触っていない**（本書冒頭 §0 の案内どおり）。
  §5.2（軸A の現時点ビュー）は #21 が軸A を変えないため値は変えず、その旨だけ明記した。

### Evidence 8 — テスト実行結果

```
$ JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true
...
[INFO] Tests run: 13, ... - in nablarch.test.tool.converter.xls.XlsFormatReaderInvalidInputTest
[INFO] Tests run: 23, ... - in nablarch.test.tool.converter.xls.XlsFormatReaderRealFileTest
[INFO] Tests run: 373, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

作業前 354 件 → 作業後 **373 件**（+19 ＝ 新規クラス 13 ＋ 既存クラスへ 6）。既存テストの修正・削除はゼロ。

### 未確認（本タスク）

- **継続する異常系で「WARN が出ないこと」はアサートしていない。** XLS-10／XLS-12／XLS-13 の担保テストは
  ログハンドラを取り付けておらず、警告の不在は実行時のログ出力を目視で確認したにとどまる
  （`issues.md`「未確認（#21）」に同内容を記録）。
- **ブック破損は「Excel でない中身のファイル」1 種類のみ**で確認した。ZIP としては開けるが内部構造が壊れた
  `.xlsx`（部分破損）の挙動は未確認。
- **`MESSAGE` 本文の 2 つ目のレコードレイアウト**は値行として吸収される（XLS-15）。`MESSAGE` 系で E-3(複数) を作る手段は無いことまでは実測したが、送信同期 4 種でも同じかは未確認（同じ `MessageParser` 由来の実装を共有するため同一と推定されるが、実行して確かめていない）。

## Expert Reviews

（未実施。steering #21 の Steps にある QA / Craft / Verification の 3 レビューはコーディネータが起動する）

### QA

### Craft (coding)

### Verification (test)

## Overall Verdict

- Self-check: OK（Completion criteria 5 項目すべて OK。留保していた E-3(複数) はコーディネータ指示により追加し解消）
- QA: 未実施
- Craft expert (coding): 未実施
- Verification expert (test): 未実施
- Ready to check off: レビュー後に判断
