# task-19 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 実 `.xlsx` ファイルを入力として `XlsFormatReader` を駆動するテストが存在し、`FakeTestDataReader` を経由していない | OK | 新規 2 クラス 20 件はすべて `new XlsFormatReader()`（本番配線＝`PoiXlsReader`）に実ファイルパスを渡す。`FakeTestDataReader` は `XlsFormatReaderTest` の private 内部クラスであり新規クラスからは参照不能（詳細は Evidence 1） | | |
| 実物 `.xlsx`（Excel 保存物）1 本が参照フィクスチャとして同梱され、POI 生成物と同じ読み取り結果になることが確認されている（確認できない場合は差分が `issues.md` に「未確認」として記録されている） | OK | `src/test/resources/nablarch/test/tool/converter/xls/reference/ClientActionTest.xlsx`（18,064 byte・無改変）を同梱。`XlsReferenceFixtureTest#poiGeneratedWorkbookReadsIdenticallyToExcelSavedWorkbook` が**一致**を確認（詳細は Evidence 2） | | |
| 軸D の 17 ケースすべてについて、中間モデルへ入る値がアサートされている（優先度の低いケースも省略しない。挙動を固定できなかったケースは `issues.md` に記録されている） | OK | `XlsFormatReaderCellTypeTest` 18 件＝ D1-01〜D1-17 の 17 ケース ＋ 空白セルの追加観測 1 件。固定できなかったケースは無し（詳細は Evidence 3） | | |
| 各ケースの結果が「実行して記録した現状の挙動」であり、実装を期待値に合わせて変更していない（src/main の diff がゼロ） | OK | 期待値はプローブ実行（scratchpad の使い捨てプログラム）で先に観測してからテストへ写した。`git status --porcelain -- src/main` → 出力なし（詳細は Evidence 4） | | |
| 仕様として不適切と判断した挙動が `issues.md` に記録され、かつ修正されていない | OK | `coverage/issues.md` を新規作成し XLS-01〜XLS-04 を記録。src/main 無変更（詳細は Evidence 5） | | |
| `mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | `Tests run: 336, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`（作業前 316 → +20。リグレッションゼロ。詳細は Evidence 6） | | |

### Evidence 1 — 実 `.xlsx` 経路であることの根拠

追加した 3 ファイル（すべて `src/test/java/nablarch/test/tool/converter/xls/`）:

| ファイル | 役割 | 件数 |
|---|---|---|
| `XlsFixture.java` | POI で `.xlsx` を組み立てるフィクスチャビルダ（テスト無し・ヘルパ） | — |
| `XlsFormatReaderCellTypeTest.java` | 軸D セル種別 17 ケース | 18 `@Test` |
| `XlsReferenceFixtureTest.java` | 参照フィクスチャと POI 生成物の読み取り一致 | 2 `@Test` |

- 両テストとも `new XlsFormatReader()`（引数なし＝`new TestCoreReaderAdapter(new PoiXlsReader())`、`XlsFormatReader` L80-82）を使う。
  アダプタ注入コンストラクタ（L89）は使っていない。
- `FakeTestDataReader` は `XlsFormatReaderTest` の private static 内部クラス（同クラス L54-102）であり、
  他クラスからは参照できない。`grep -n "FakeTestDataReader" src/test/java/nablarch/test/tool/converter/xls/*.java`
  → `XlsFormatReaderTest.java` のみ。
- これにより `PoiXlsReader#readOneLine` の「実セル → 文字列行」区間（`cell.toString()`）が
  converter のテストで初めて実行される。

### Evidence 2 — 参照フィクスチャと POI 生成物の一致

取得元（読み取りのみ。`nablarch-example-web` へは一切書き込んでいない — `git status --porcelain` → 出力なし）:

```sh
cd /home/tie303177/work/nablarch/nablarch-example-web
git show origin/main:src/test/java/com/nablarch/example/app/web/action/ClientActionTest.xlsx \
  > <converter>/src/test/resources/nablarch/test/tool/converter/xls/reference/ClientActionTest.xlsx
```

- 真正な Excel 保存物であること（`docProps/app.xml` に `<Application>Microsoft Excel</Application>`・
  `AppVersion 16.0300`）は steering Decisions で確認済み。本タスクでも `unzip` で
  1 シート（`setUpDb`）・全セル文字列型（`t="s"`）・書式のみの空白セルを含むことを確認した。
- 同梱ファイルは `git show` の出力そのままで、バイト単位で無改変（18,064 byte）。
- 一致確認の方法: `XlsFixture#copyOf(bookName, sheet)` が参照ブックの全セルを
  「元セルの表示文字列（`Cell#toString()`＝`PoiXlsReader` が読む値そのもの）を持つ文字列セル」として
  POI で作り直し、行・列インデックスと欠落（セル不在・行不在）を保つ。
  両ブックを `XlsFormatReader` に通し、コンテナ名・セクション名・全ブロックの
  dataType / groupId / identifier / columnNames / rows を逐次比較した（中間モデルは `equals` を持たないため）。
- 結果: **完全一致**。差分ゼロ。`issues.md` に「未確認」として記録すべき事項は無い。
- 参照フィクスチャそのものの読み取り結果（`#readsRealExcelSavedWorkbook`）:
  `SETUP_TABLE=PROJECT`（16 列・0 行）／`SETUP_TABLE=INDUSTRY`（2 列・93 行）／
  `SETUP_TABLE=CLIENT`（3 列・120 行）の 3 ブロック。

### Evidence 3 — 軸D 17 ケースの実測結果

すべて `SETUP_TABLE` の 1 データ行（`KEY` 列＝ケース識別・`V` 列＝検証対象セル）として与え、
`V` 列の中間モデル値を記録した。`V` 列のみのシートにすると空セルのケースで行全体が空行になり
`PoiXlsReader#readLine` に読み飛ばされるため、行を空にしない `KEY` 列を必ず置いている。

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
| D1-12 セル不在 | セルを作らない | `""` | `readsAbsentCellAsEmptyString` | 課題 XLS-04（記録のみ） |
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
  素のままでは値が実行環境で変わる（日本語環境では `07-8月-2026` になることを実測）。
  テストは `@Before`/`@After` で既定ロケールを `Locale.ENGLISH` に固定・復元して値を確定させている。
  **ロケール依存であること自体を課題 XLS-02 として記録した。**
- #18 §0.8-8 のとおり D1-13／D1-14／D1-16 は `RoundTripTest` が実ファイル経由で 🔺 担保していたが、
  往復ではなく**直接テスト**として本タスクで正式に固定した（steering Rules に従い往復テストは正式担保に数えない）。
  往復テストには手を加えていない。

### Evidence 4 — 「実行して記録した現状の挙動」であることと src/main 無変更

- 手順: (1) scratchpad に使い捨てのプローブプログラム（`Probe.java` / `Probe2.java`）を置き、
  17 ケースのセルを 1 シートにまとめて `XlsFormatReader` で読んだ結果を標準出力へダンプ、
  (2) その出力を Evidence 3 の表として記録、(3) 記録した値をテストの期待値へ写した。
  プローブは scratchpad（`/tmp/claude-1000/.../scratchpad`）に置いたのみでリポジトリには含まれない。
- `git status --porcelain -- src/main` → **出力なし**。
- `git status --porcelain` の全出力（新規のみ・既存ファイルの変更ゼロ）:

  ```
  ?? .rn/ntf-test-data-converter/coverage/issues.md
  ?? src/test/java/nablarch/test/tool/converter/xls/XlsFixture.java
  ?? src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderCellTypeTest.java
  ?? src/test/java/nablarch/test/tool/converter/xls/XlsReferenceFixtureTest.java
  ?? src/test/resources/nablarch/
  ```

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

- いずれも `src/main` を変更していない（Evidence 4）。
- XLS-01 は steering Decisions のユーザー実測（`nablarch-example-web` の 6 ファイルに数値セル 39 件、
  実例は「値が数値の `1`・表示形式 `@`」）に直接該当し、変換後 YAML に `1.0` が書かれる。
- 本体 `PoiXlsReader` の Javadoc は「文字列書式以外のデータ書式が存在した場合の動作は保証しない」と
  明記しており、XLS-01〜XLS-03 はいずれも本体仕様上の保証外の範囲である。この前提も `issues.md` に記載した。

### Evidence 6 — `mvn clean test` の結果

```
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean test -Djacoco.skip=true
→ Tests run: 336, Failures: 0, Errors: 0, Skipped: 0
→ BUILD SUCCESS
```

内訳（新規分）: `XlsFormatReaderCellTypeTest` 18 ／ `XlsReferenceFixtureTest` 2 ＝ 20 件。
既存テストは全件そのまま PASS（`XlsFormatReaderTest` 33 ／ `XlsFormatWriterTest` 40 ／
`RoundTripTest` 30 ／ `YamlFormatReaderTest` 20 ／ `YamlFormatWriterTest` 33 ほか）。リグレッションゼロ。

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
  - `copyOf(bookName, Sheet)` と `open(Path)` も本クラスに置き、**POI の API を触るコードを 1 か所に閉じた**。
    テストクラス側には POI の生 API が現れない（`FormulaError` を除く）。
- **POI 3.8 制約**: `CellType` enum は POI 4 以降のため使っていない。3.8 の
  `Cell#setCellValue` / `setCellFormula` / `setCellErrorValue(byte)` / `CellStyle#setDataFormat(short)` /
  `Workbook#createDataFormat()` と `org.apache.poi.ss.usermodel.FormulaError`（3.8 に存在することを
  `javap` で確認）のみを使う。

### 意図的に入れなかったもの

- セルの色・罫線・列幅の指定（`XlsFormatWriter` の整形の関心事であり、辺①の読み取りに影響しない）。
- 複数シートの一括生成 API（`sheet()` を続けて呼べば足りる。#21 の軸E で必要になったときに評価する）。
- 中間モデルの組み立てヘルパ（既存 2 クラスと重複するため持たせない）。

## QA Expert Review

## Expert Reviews

### Craft Expert (coding)

### Verification Expert (test)

### Design Expert

## Overall Verdict

- Self-check: OK
- QA: —
- Design expert: —
- Craft expert: —
- Verification expert: —
- Ready to check off: No（レビュー待ち）
