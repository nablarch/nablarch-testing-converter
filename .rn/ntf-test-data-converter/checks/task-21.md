# task-21 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 軸E の3観点（セクション内ブロック数／ブロック内行数／ファイル内レコードレイアウト数）それぞれで 0／1／複数がアサートされている。「ブック内シート数 複数」は到達不能として根拠付きで空欄に残されている | OK | **E-1〜E-3 の 3 観点 × 0／1／複数 ＝ 9 組すべてを実 `.xlsx` 経路でアサート**（`XlsFormatReaderRealFileTest`）。0 件の 3 組と E-3(複数) は #21 が新規担保（+6 メソッド）。E-4(複数) は `XlsFormatReader#read` L133 が `Collections.singletonList(section)` を返すため到達不能として空欄。E-1(0/1/複数)・E-2(1/複数)・E-3(1)・E-4(1) が実 `.xlsx` 経路で担保済みであることも 1 件ずつ確認した（詳細は Evidence 1） | | |
| 軸F の6ケース（シート不在／ブック破損／未知データタイプ名／マーカーカラム欠落／カラム名重複／行列数不一致）すべてで、例外型・メッセージまたは継続時の結果がアサートされている | OK | **6 ケースすべてを新規クラス `XlsFormatReaderInvalidInputTest`（16 `@Test`）が実 `.xlsx` 経路で担保**（F1-05 は #16 の 4 件が Fake リーダ経路のため、修正ラウンドで実ファイル版 2 件を追加）。継続する 4 ケースは「WARN も出ない」ことまでアサートする（詳細は Evidence 2・Evidence 9） | | |
| C-17／C-20 の「到達不能」が F1-06 のテストで実行可能な根拠を持ち、`issues.md` の「到達不能」表がそのテストメソッド名を参照している | OK | 4 メソッド（C-17 はファイル系・メッセージ系の 2 件に分割）を追加し、`issues.md` の「到達不能」表の C-17 行・C-20 行から参照（C-11／C-13／C-16 と同じ書式）。前任者が `issues.md:398`・`:399` に記録した期待値は鵜呑みにせず自分で実行して確認した（詳細は Evidence 3） | | |
| src/main への変更がゼロ | OK | `git diff -- src/main` → 出力なし。`git status --porcelain -- src/main` → 出力なし（詳細は Evidence 5） | | |
| `mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | `Tests run: 376, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`（作業前 354 → +22。リグレッションゼロ） | | |

---

### Evidence 1 — 軸E（4 観点 × 多重度）× 担保テストメソッド

追加は 2 ファイル。

| ファイル | 追加 `@Test` | 役割 |
|---|---|---|
| `src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderRealFileTest.java` | +6（17 → **23**） | 軸C の空コレクション（C-09／C-12／C-15／C-18）＝軸E の 0 件、＋ E-3(複数) |
| `src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderInvalidInputTest.java` | **16**（新規） | 軸F 6 ケース（F1-05 の実 `.xlsx` 担保 2 件を含む）＋ C-17／C-20 到達不能の根拠 4 件 ＋ XLS-15 の根拠 1 件 |

**E-1〜E-3 の 3 観点 × 0／1／複数 ＝ 9 組すべてが実 `.xlsx` 経路（`XlsFormatReaderRealFileTest`。すべて
`new XlsFormatReader()` ＋ `XlsFixture` の実ファイル）でアサートされている。**E-4 は別枠で、(1) がアサート済み、
(複数) が到達不能、(0) が n/a である（アサート済みのセルは 9 ＋ 1 ＝ 10 個）。
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
| F1-05 カラム名重複 | **`deduplicatesDuplicateColumnNamesWithWarningInListMapFromRealBook`／`deduplicatesDuplicateColumnNamesWithWarningInTableFromRealBook`（#21 の修正ラウンドで追加）**。#16 の `XlsFormatReaderTest` 4 件は Fake リーダ経路 | 後勝ちで 1 件に絞られ、WARN ログが 1 件出る（ブック名・シート名・ブロック識別子・カラム名・採用列番号を含む）。実 `.xlsx` 経路でも Fake 経路と同じ |
| F1-06 行と列の数の不一致 | `padsShortDataRowAndDropsCellsBeyondColumnRowInRealBook` | テーブル: 短い行は `""` 埋め（`[[a1, ""]]`）、長い行は超過セルを切り捨て（`[[c1, d1]]`。`e1` が消える。`issues.md` **XLS-12**） |
| F1-06（続き） | `padsShortValueRowAndDropsCellsBeyondNameRowInFixedFileInRealBook` | 固定長ファイルの値行でも同じ（`[[abc, ""]]` ／ `[[xyz]]`。`extra` が消える） |
| F1-06（続き） | `failsWhenLengthRowIsShorterThanNameRowInRealBook` | `IllegalStateException: can't get data. ...` ＋ 原因 `IllegalArgumentException: field name size is 2. but lengths size is 1.` |
| F1-06（続き） | `failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook` ／ `failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook` ／ `failsWhenTypeRowIsShorterThanNameRowInRealBook` ／ `failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook` | Evidence 3 参照 |

**継続する異常系と例外になる異常系の切り分け**（観測結果）:

- 例外になる: シート不在・ブック破損・**名前行／型行／長さ行**の要素数不整合
- 例外にならず継続する: 未知のデータタイプ名・マーカーカラム欠落・**値行**の要素数不整合

この非対称（値行だけは黙って埋める／捨てる）が `issues.md` XLS-12 の中身である。

### Evidence 3 — C-17／C-20「到達不能」の実行可能な根拠（レビュー指摘由来）

3 入力を実行し、例外型とメッセージをアサートした。`issues.md:398`・`:399` に前任者が記録していた期待値は
根拠にせず、**自分でプローブを実行して観測した結果**をアサーションにした（Evidence 4）。結果は前任者の記録と一致した。

| 入力 | 根拠づける要素 | 担保テストメソッド | 観測した例外（外側 → 原因） |
|---|---|---|---|
| 名前行が 1 列（レコード種別セルのみ）。`SETUP_FIXED` と `MESSAGE` で経路が別なのでメソッドも分けた | C-17 `RecordLayout.fields` 空 | `failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook`／`failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook` | `IllegalStateException: can't get data. ...` → `IllegalStateException: directive or data names row must have two columns at least. [data]`（本体 `DataFileParser` L234） |
| 型行が名前行より短い（フィールド 2・型 1） | C-20 `FieldDef.type` 省略（`null`）根拠① | `failsWhenTypeRowIsShorterThanNameRowInRealBook` | `IllegalStateException: can't get data. ...` → `IllegalArgumentException: field name size is 2. but types size is 1. FixedLengthFileFragment{...}`（本体 `DataFileFragment#assertSameSizeAsNames` 宣言 L339／`throw` L342／呼び出し `setTypes` L203） |
| 型セルが中間位置で空 | C-20 根拠② | `failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook` | `IllegalStateException: can't get data. ...` → `IllegalArgumentException: can't convert value []. convert table ={...}`（本体 `BasicDataTypeMapping#convertToFrameworkExpression` L69） |

- `issues.md` の「到達不能」表 C-17 行・C-20 行に **担保テストメソッド名を追記**した（C-11／C-13／C-16 と同じ書式）。
- `inventory.md` §1.3 の C-17 行・C-20 行にも同じテストメソッド名を追記した。
- メッセージのアサートは、環境依存の要素（一時ディレクトリの絶対パス、`HashMap` 由来で並び順が変わる
  `convert table =`）を含むものは部分文字列で、固定文言は完全一致で行う。
- 上表の行番号（`DataFileParser` L234 ほか）は `nablarch-testing` のソース位置である。**テスト Javadoc からは
  修正ラウンドで削除し、原本の `issues.md` と本書にだけ残した**（依存はバイナリ jar で解決されるため、
  本リポジトリだけを見る読み手は検証できず、依存更新で黙って腐るため）。

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
| 新規 `XlsFormatReaderInvalidInputTest` 16 件 | 軸F 6 ケース（F1-05 の実 `.xlsx` 担保を含む）＋ C-17／C-20 の根拠 ＋ XLS-15 の根拠 | **アサーション対象が例外型・例外メッセージ・例外連鎖という別種**であり、入力も意図的に壊したものだけを置く。専用ヘルパ（`causeOf` / `allMessagesOf` / `assertWrappedByCannotGetData`）が要り、正常系クラスへ混ぜると読み手が「壊れた入力」と「正しい入力」を取り違える。#19（`XlsFormatReaderCellTypeTest`）・#20（`XlsFormatReaderRealFileTest`）と同じく観点でクラスを分ける方針に沿う |

### Evidence 7 — `inventory.md` の更新と検算

- §1.2-2: テストクラス表に `XlsFormatReaderInvalidInputTest`（**16**）を追加し、`XlsFormatReaderRealFileTest` を
  「23（#20 が 17 ＋ #21 が 6）」へ更新。軸C 表に C-09／C-12／C-15／C-18 の 4 行を追加。**軸E 表・軸F 表を新設**。
  軸F 表の F1-05 行に実 `.xlsx` 経路の担保（#21 で追加）を記し、#16 の 4 件が Fake リーダ経路であることを明記。
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
[INFO] Tests run: 16, ... - in nablarch.test.tool.converter.xls.XlsFormatReaderInvalidInputTest
[INFO] Tests run: 23, ... - in nablarch.test.tool.converter.xls.XlsFormatReaderRealFileTest
[INFO] Tests run: 376, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

作業前 354 件 → 作業後 **376 件**（+22 ＝ 新規クラス 16 ＋ 既存クラスへ 6）。既存テストの修正・削除はゼロ。

### 未確認（本タスク）

- **「警告が出ない」ことのアサートは `java.util.logging` 経路に限る。** XLS-10／XLS-12／XLS-13／XLS-15 の
  担保テスト（計 5 メソッド）は JUL のルートロガーへハンドラを付けて WARNING 以上 0 件をアサートするが、
  `nablarch-testing` 自身のログ基盤（`nablarch.core.log`）への出力は捕捉していない。変換ツール側で JUL を
  使うのは `XlsFormatReader` の 1 箇所だけであることは `grep -rn "Logger" src/main/java` で確認した
  （`issues.md`「未確認（#21）」に同内容を記録）。
- **ブック破損は「Excel でない中身のファイル」1 種類のみ**で確認した。ZIP としては開けるが内部構造が壊れた
  `.xlsx`（部分破損）の挙動は未確認。
- **`MESSAGE` 本文の 2 つ目のレコードレイアウト**は値行として吸収される（XLS-15）。`MESSAGE` 系で E-3(複数) を作る手段は無いことまでは実測したが、送信同期 4 種でも同じかは未確認（同じ `MessageParser` 由来の実装を共有するため同一と推定されるが、実行して確かめていない）。

### Evidence 9 — レビュー指摘の修正ラウンド（コーディネータ triage 済みの指示 A〜D）

コミット `56128b5` に対する 3 レビューの指摘を、コーディネータが採用と決めた形で反映した（判定は渡されていない）。

| 指示 | 内容 | 対応 |
|---|---|---|
| A-1 | §5.1 の注記が #20 時点（要追加 11／担保済み 22）のまま／「#20 後の状態」列の参照が浮いている | §5.1 の**注記だけ**を「要追加 0 ／ 担保済み 33 ／ 到達不能 8 ／ 対象外 0」「§1.3 の『#21 後の状態』列」へ訂正。**表は #18 基準のまま未変更** |
| A-2 | §1.2-2 軸E の「9 組すべて」が数える単位を混ぜている | 「アサート済みのセルは 10 個（E-1〜E-3 の 9 組 ＋ E-4(1)）」「残りは E-4(複数)＝到達不能、E-4(0)＝n/a で 4×3＝12 セル」と分けて記述 |
| A-3 | `checks/task-21.md` が同一文中で 12 と 13 を併記 | 実測値へ統一（本ラウンドで 16 になったため 16 に更新） |
| A-4 | WARN 未アサートの対象が self-check 3 件・`issues.md` 4 件で不一致 | D-2 の実装により項目そのものを書き換え、両者を「JUL 経路に限る」で揃えた |
| B-1 | クラス Javadoc の課題 ID が XLS-10〜XLS-14 | XLS-10〜XLS-15 へ訂正 |
| B-2 | `absorbsSecondNameRowAsDataRowInMessageBodyInRealBook` が担当範囲宣言から漏れ／「担保する軸要素」行なし | クラス Javadoc に「軸E の到達不能根拠も持つ」旨を追加し、当該メソッドに「担保する軸要素: E-3（複数。メッセージ経路では到達不能であることの根拠）」を追記 |
| B-3 | `TestCoreReaderAdapter#markerGroupId` を「本体」と記載（誤り） | 「本リポジトリの `src/main/java/nablarch/test/core/reader/TestCoreReaderAdapter.java`（`nablarch-testing` 側ではない）」へ訂正。`issues.md` の XLS-10／XLS-11 の原因記述も同様に訂正し、**本リポジトリ内で修正可能**である旨を追記。他の「本体 …」参照は全件確認して誤りなし（下表） |
| B-4 | `XlsFormatReaderRealFileTest` のクラス Javadoc に E-3(複数) が反映されていない | 「E-2 0 件／E-3 0 件・複数件」へ訂正 |
| B-5 | 「`containsString` だけで突き合わせる」宣言が完全一致アサートと不一致 | 「環境依存を含むものは `containsString`、固定文言は完全一致」へ訂正 |
| B-6 | `nablarch-testing` のソース行番号を Javadoc に埋めている | 3 箇所（`DataFileParser` L234／`DataFileFragment` L339・L342・L203／`BasicDataTypeMapping` L69）から行番号を削除。クラス名＋メソッド名＋例外メッセージに留めた。行番号は原本の `issues.md` にのみ残す |
| C-1 | `byIdentifier.get(...)` の素キャスト | ヘルパ `blockOf(blocks, identifier, Class)` を追加（非 null と実装クラスをアサートしてから cast。失敗時は実在する識別子一覧を出す）。4 箇所を置換 |
| C-2 | C-17 根拠が 1 メソッドで 2 入力をループ | `failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook` と `failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook` に分割。共通アサートは private ヘルパ `assertNameRowNeedsTwoColumns` へ |
| D-1 | F1-05 の担保が Fake リーダ経路のみ | 実 `.xlsx` 経路のテストを 2 件追加（LIST_MAP／TABLE）。§1.2-2 の軸F 表と「#21 が追加で埋めた 2 件」の表を更新 |
| D-2 | 「WARN も出ない」が未アサート | JUL ルートロガー捕捉ヘルパ（`readCapturingWarnings` ／ `CapturingHandler` ／ `assertNoWarning`）を追加し、XLS-10／XLS-12（2 件）／XLS-13／XLS-15 の計 5 メソッドで WARNING 以上 0 件をアサート。**先に `Probe4` で観測し、0 件であることを確認してから固定した** |

**B-3 の全件確認**（`find` で本リポジトリ `src/main` と `nablarch-testing` の sources jar 展開物を突き合わせた）:

| Javadoc の参照 | 実在する場所 | 記述 |
|---|---|---|
| `TestCoreReaderAdapter`（`markerGroupId` / `HeaderCollector` / `BodyLineCollector`） | **本リポジトリ** `src/main/java/nablarch/test/core/reader/TestCoreReaderAdapter.java` | **誤り → 訂正済み**（テスト Javadoc 1 箇所、`issues.md` 2 箇所） |
| `TestDataParsingTemplate` | `nablarch-testing` | 「本体」で正しい |
| `PoiXlsReader` | `nablarch-testing` | 同上 |
| `HeaderLine` | `nablarch-testing` | 同上 |
| `MessageParser` / `SendSyncMessageParser` | `nablarch-testing` | 同上 |
| `DataFileParser` | `nablarch-testing` | 同上 |
| `DataFileFragment` | `nablarch-testing` | 同上 |
| `BasicDataTypeMapping` | `nablarch-testing` | 同上 |
| `DataFile` | `nablarch-testing` | 同上（`XlsFormatReaderRealFileTest` の Javadoc） |
| `TestCoreFileAdapter` | **本リポジトリ** `src/main/java/nablarch/test/core/file/` | 本タスクの Javadoc では「本体」と呼んでいない（参照なし） |

**D-2 の観測（`Probe4`。固定する前に実行した）**:

| 入力 | ルートロガーで捕捉した WARNING 以上 | `XlsFormatReader` ロガーで捕捉 |
|---|---|---|
| F1-05 LIST_MAP カラム名重複（実 `.xlsx`） | **1 件**（`[d1] シート "s" のブロック "dupMap" に重複カラム名 "COL_A" があります。3 列目の値を採用します。`） | 1 件（同内容） |
| F1-05 TABLE カラム名重複（実 `.xlsx`） | **1 件**（`... "MY_TABLE" ... "COL_X" ... 3 列目 ...`） | 1 件（同内容） |
| XLS-10 未知のデータタイプ名 | **0 件** | 0 件 |
| XLS-12 はみ出したセル（テーブル） | **0 件** | 0 件 |
| XLS-12 はみ出したセル（固定長ファイル） | **0 件** | 0 件 |
| XLS-13 送信同期のメタ列欠落 | **0 件** | 0 件 |
| XLS-15 メッセージ本文の 2 つ目のレコードレイアウト | **0 件** | 0 件 |

重複カラム名の 2 件で 1 件ずつ捕捉できることが、0 件アサートに検知力があることの裏づけである
（捕捉の仕組みが壊れていれば重複カラムのテストが落ちる）。

## Expert Reviews

3 レビューとも独立サブエージェントとして起動し、**先行ラウンドの判定・指摘は渡していない**（中立フレーミング）。
QA には「対応表・棚卸しの数字は、表の行を実際に足し合わせて検算すること」を必須観点として明示した。

| レビュー | ラウンド1（`56128b5`） | ラウンド2（`c724a37`） | ラウンド3（`c787352` ＋ 最終修正） |
|---|---|---|---|
| QA | **PASS**（Major 1・Minor 6） | **FAIL**（旧メソッド名参照が完了条件の未達） | **PASS**（Minor 1＝空 `<p>`。修正済み） |
| Craft (coding) | **FAIL**（Major 5・Minor 4） | **FAIL**（N1・N2 旧メソッド名／N3 未使用ヘルパ／N4 方針記述） | **PASS**（Minor 4。うち 3 件を修正、1 件は据え置き） |
| Verification (test) | **PASS**（Minor 3） | **PASS**（Minor 1＝旧メソッド名） | 再実行せず（変更が Javadoc・ドキュメント・未使用ヘルパ削除のみでアサーションに触れていないため） |

### 指摘への対応

- **ラウンド1**: `Evidence 9` に A〜D の対応を記録。事実誤り 1 件（`TestCoreReaderAdapter#markerGroupId` を「本体」と誤記。実際は本リポジトリ `src/main`）、台帳の陳腐化 1 件（§5.1 の注記が #20 時点のまま）、担保の穴 2 件（F1-05 が Fake 経路のみ／XLS-10・12・13・15 の「警告も出ない」が未アサート）を解消した。
- **ラウンド2**: メソッド分割で生じた旧メソッド名への参照 5 箇所を差し替え。3 レビュアーが独立に同じ箇所を挙げた。
- **ラウンド3**: 例外メッセージ突き合わせ方針の記述を実装に合わせて 4 類型へ（本体の `toString()` ダンプ、外部ライブラリ POI の文言）。空 `<p>` を除去。

### 据え置いたもの（理由付き）

| 指摘 | 判断 |
|---|---|
| Craft: 2 クラス間のヘルパ重複（`fieldNames`・`read`）を `XlsFixture` へ集約 | 見送り。アサート系ヘルパをクラスごとに残す判断はレビュアー自身も妥当としており、#22〜#25 で辺③④のフィクスチャ要件が固まってから判断するほうが手戻りが少ない |
| Craft: `assertThrows` と `try/fail/catch` の使い分け | 現状維持（レビュアーも推奨）。クラス内で混在なし |
| Craft: `assertNameRowNeedsTwoColumns` がヘルパ区画の外にある | 据え置き。呼び出し元 2 件の直後にあり意図が読め、C-17／C-20 の対を割ってもいない |
| ブック部分破損（ZIP は開けるが内部破損）の挙動／XLS-15 が送信同期 4 種でも起きるか | `issues.md`「未確認（#21）」に射程として記録済み |

### 検証の要点（Verification が実測）

- 追加 22 件すべてが、`src/main` へのミューテーションで 1 つ以上のケースにより赤くなる。空証明・トートロジーは無い。
- **ログ捕捉の 0 件アサートは空証明ではない** — `read()` に `LOGGER.warning` を注入すると `assertNoWarning` を持つ 5 メソッド全部が赤くなり、実行順を `reversealphabetical`／`random` に変えても同じ。
- C-17／C-20 の到達不能根拠は「例外が出なくなる方向」に壊しても赤くなり、到達可能化を検出する tripwire として機能する。
- 固定長経路とメッセージ経路を握り潰すミューテーションが、分割した 2 メソッドをそれぞれ独立に赤くする。

## Overall Verdict

- Self-check: OK（Completion criteria 5 項目すべて OK。留保していた E-3(複数) はコーディネータ指示により追加し解消）
- QA: **OK**（ラウンド3 で PASS。全表の検算一致・台帳とテストの全メソッド名突き合わせ 245 識別子で未解決参照ゼロ・全 376 テスト PASS・src/main 無変更を自ら実行して確認）
- Craft expert (coding): **OK**（ラウンド3 で PASS）
- Verification expert (test): **OK**（ラウンド2 で PASS）
- Ready to check off: **Yes**
