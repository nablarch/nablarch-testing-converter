Rn version: 0.8.0

# Goal

nablarch-testing（ブランチ `convert-testdata-excel-to-text`）の変換ツール（src/main 28件）と形式間変換テスト（src/test 21件）を nablarch-testing-converter リポジトリへ切り出し、`mvn test` 全 PASS・実装無改変を確認する。

（フェーズ2）切り出した converter の4つの変換辺（① Excel→中間モデル／② YAML→中間モデル／③ 中間モデル→Excel／④ 中間モデル→YAML）の変換ルールを、converter 自身のユニットテストで担保する。担保の網羅は主観で判断せず、6軸（A データタイプ／B ブロック実装／C 中間モデル全フィールド／D 値の表現／E 多重度／F 異常系）を4辺それぞれについて埋め、軸×要素対応表とカバレッジ計測で客観的に示す。

# Acceptance criteria

- converter の `mvn test` が全テスト PASS する
- 全移動ファイルが本体現ブランチと package/import を除いて完全一致（実装無改変）
- pom.xml が yaml・本体・poi 3.8・snakeyaml 3.0.1 の依存を正しく持つ
- 本体（nablarch-testing）・yaml（nablarch-testing-yaml）に一切書き込んでいない
- ブランチが push 済み

（フェーズ2）

- 辺①に実 `.xlsx` を入力とするテストが存在し、軸A（`DataType` 14種）すべてが実ファイル経由で1回以上通っている
- 4辺それぞれで、軸B（`TestDataBlock` sealed 階層 4種）と軸C（中間モデル全フィールド。省略可能なフィールドは「値あり」「省略」の双方）が非デフォルト値で1回以上 IN／OUT されている。`FileDataBlock.fileType` は `FIXED`／`VARIABLE` の両方を通す
- 軸D が4辺すべてでアサートされている（辺① セル種別8ケース／辺③ セル型8ケース（`getCellType()` をアサート）／辺② スキーマから導出したスカラー／辺④ YAML 表現9ケース）。ケース数の根拠は Decisions「軸D の対象範囲」
- 4辺それぞれで軸E（0件／1件／複数件）と軸F（異常系）が埋まっている
- 参照フィクスチャとして同梱した実物 `.xlsx`（Excel 保存物）1本と、POI 生成フィクスチャの読み取り結果が同一であることが確認されている（確認できない場合は差分が `issues.md` に「未確認」として記録されている）
- 4辺ぶんの軸×要素対応表が成果物として存在し、各要素に担保テストメソッド名が記されている。空欄には理由が書かれている
- 4辺の担当クラス（`XlsFormatReader` / `XlsFormatWriter` / `YamlFormatReader` / `YamlFormatWriter` / `TestCoreReaderAdapter` / 中間モデル各クラス）の行・分岐カバレッジが計測され、未到達分岐が列挙されている。テスト不要と判断したものには根拠が書かれている
- 本作業で見つかった現状挙動の課題が、修正されずに課題一覧へ記録されている
- `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` が全テスト PASS する

# Assumptions

- 全リポジトリは同じ親ディレクトリに clone 済み
- nablarch-testing-yaml は構築・公開済み（converter のビルドに必要）
- 本体は `convert-testdata-excel-to-text` ブランチのまま（移動元）
- テストデータは静的同梱ではなく変換テストが実行時に一時生成・参照する方式（.xls/.yaml の固定ファイルは不要）。**例外**（ユーザー承認済み）: POI 生成物と Excel 保存物の同一性を確認するため、実物 `.xlsx` 1本のみ参照フィクスチャとして同梱してよい
- YamlModeTestBase や *YamlTest・結合テストは integration 行きであり converter 対象外

# Rules

- 1 task = 1 commit
- 実装の変更は一切しない。package/import の機械的調整と pom 設定のみ許可
- 本体・yaml には書き込まない
- `mvn test` が通らず実装変更が要ると判断したら止めてユーザーに確認する
- タスク完了後は即 push し、PR を作成してユーザーがコードを PR 上で確認できるようにする

（フェーズ2）

- 期待値を先に決めない。まず現状の挙動を実行して記録し、それが仕様として妥当かを判断してから固定する
- 本作業で見つかった不具合は、この作業の中では修正しない。`.rn/ntf-test-data-converter/coverage/issues.md` に記録して切り分ける（src/main を変更しない）
- 各辺の担保を往復テスト（`RoundTripTest`）の追加で代替しない。ただし**既存**の往復テスト（`RoundTripTest` 30件、`XlsFormatWriterTest#roundTrips*` 8件、`YamlFormatWriterTest#roundTrip_*` 6件）が実ファイル経由で通している軸要素は、棚卸しに「🔺弱い担保」として必ず計上する（重複テストを書かないため）。正式担保としては数えず、直接テストの追加対象からは外さない
- 既存テストを軸で棚卸ししてから新規テストを足す。棚卸しなしの新規追加はしない
- 対応表・カバレッジを示さずに「網羅した」と報告しない
- **テストメソッドを増減させたら、`inventory.md` 内の該当する件数を「記憶している箇所を直す」のではなく、コマンドから導き直す。各件数にはそれを導いたコマンドを併記する**（#22 で確定・2026-08-13）。#22 ではラウンド3 で 3 テストを追加した際、`§3.1-2`・Javadoc・`issues.md` は更新されたのに `§3.3` の件数だけが取り残され、同一文書内で 16 と 18 が矛盾した。#23・#24・#25 はいずれもテストを追加して同じ `inventory.md` を更新するため、同じ取り残しが起きる
- **台帳に載せる出典コマンドは、そのまま実行して同じ結果が出ること。** 誤った結果を返すコマンドは件数の誤記と同じ扱いとする（#22 で確定・2026-08-13。`grep -rc` の `| grep -v ":0$"` 欠落、および自分自身がヒットして主張を反証する grep が実際に発生した）
- **担保の穴は、テストを足さない場合でも台帳に開示する。** 開示しないのは件数を誤るのと同じ性質の誤りとする（#22 で確定・2026-08-13）
- **台帳（`coverage/inventory.md`）に「他ファイルの行番号」「ファイル行数」「コマンドを併記しない件数」を書かない**（#23 後の構造見直しで確定・2026-08-13）。行番号とファイル行数は他ファイルを編集するたびに移動し、台帳を直すと台帳の別箇所が自己無効化する。識別はクラス名・メソッド名で行う。#22・#23 の計 5 ラウンドの FAIL はすべて台帳の記述精度であり、テストコードの欠陥ではなかった
- **同じ関係を 2 方向に手書きしない。** 台帳が持つのは「テストメソッド → 軸要素」（§X.1 系）だけとし、逆引き（軸要素 → 担保テストメソッド）の正は #27 の `coverage/axis-matrix.md` とする（同・2026-08-13）。#24・#25 の途中で逆引きが要る場面ではテストメソッド名を grep する

# Tasks

### #1: pom.xml の作成

**Purpose**: converter リポジトリのビルド基盤を整える。

**Prerequisites**: none

**Steps**:

- [x] 本体 pom.xml を参照し groupId・親 POM を確認する
- [x] nablarch-testing-yaml の groupId・artifactId・version を確認する
- [x] yaml・本体・poi 3.8・snakeyaml 3.0.1 を依存に持つ pom.xml を作成する
- [x] self-check（OK/NG per completion criterion、checks/task-1.md に記録）
- [x] QA expert review（subagent）
- [x] language expert review（subagent）
- [x] software-engineering expert review（subagent）
- [x] user review

**Completion criteria**:

- pom.xml が存在し、nablarch-testing-yaml・nablarch-testing（compile）・poi-ooxml:3.8・snakeyaml-engine:3.0.1 が依存として記載されている
- groupId・親 POM が本体に合わせて設定されている

---

### #2: src/main 移動（変換ツール 28件）

**Purpose**: 本体現ブランチの `src/main/java/nablarch/test/tool/converter/` 配下 28件を同一パッケージパスへコピー配置する。

**Prerequisites**: #1

**Steps**:

- [x] 本体現ブランチから対象 28件を同一パッケージパスへコピーする
- [x] package/import を converter リポジトリ向けに機械的調整する（ロジック変更なし）
- [x] self-check（OK/NG per completion criterion、checks/task-2.md に記録）
- [x] QA expert review（subagent）
- [x] language expert review（subagent）
- [x] software-engineering expert review（subagent）
- [x] user review

**Completion criteria**:

- `src/main/java/nablarch/test/tool/converter/` 配下に 28件が存在する（直下・model/・xls/・yaml/ サブパッケージ含む）
- 各ファイルが本体現ブランチの対応ファイルと package/import を除いて完全一致する

---

### #3: src/test 移動（形式間変換テスト 21件）

**Purpose**: 本体現ブランチの `src/test/java/nablarch/test/tool/converter/` 配下 21件を対応パスへコピー配置する。

**Prerequisites**: #2

**Steps**:

- [x] 本体現ブランチから対象 21件を対応パスへコピーする
- [x] package/import を converter リポジトリ向けに機械的調整する（ロジック変更なし）
- [x] self-check（OK/NG per completion criterion、checks/task-3.md に記録）
- [x] QA expert review（subagent）
- [x] language expert review（subagent）
- [x] software-engineering expert review（subagent）
- [x] user review

**Completion criteria**:

- `src/test/java/nablarch/test/tool/converter/` 配下に 21件が存在する
- 各ファイルが本体現ブランチの対応ファイルと package/import を除いて完全一致する

---

### #6: 分類1 再分析 → 削除対象なしを確認

**Purpose**: 当初「到達不能デッドコード」と分類した2箇所を再検証し、削除対象がないことをコードレビューで確認する。

**Prerequisites**: #5

**Steps**:

- [x] `XlsFormatReader#stripQuotes` null ガード（L455）: `valueCells.get(i)`（L332）が null を返しうる（Excel 空白セル）ため load-bearing。削除不可。
- [x] `YamlFormatWriter#emitBlock` else-throw（L141）: `instanceof` チェーンはコンパイラが網羅性を保証しない。else-throw は sealed 階層変更時のランタイム安全網として維持が正しい。
- [x] self-check（OK/NG per completion criterion、checks/task-6.md に記録）
- [x] QA expert review（subagent）
- [x] language expert review（subagent）
- [x] software-engineering expert review（subagent）
- [x] user review ✓

**Completion criteria**:

- 2箇所とも削除対象でないことが根拠付きで確認されている
- ソースコードへの変更はゼロ

---

### #7: 分類2 — NTF仕様内テスト追加（4箇所）

**Purpose**: カバレッジ計測で未カバーだった仕様内コードパス4箇所にテストを追加する。

**Prerequisites**: #6

**Steps**:

- [x] `XlsFormatReader#normalizeDirectiveValue`: record-separator の CRLF/LF/CR シンボル変換（L394/L408/L422）のテストを追加する
- [x] `XlsFormatReader#readMessageBlock`: `message == null → return null` パス（MESSAGE ブロック不在）のテストを追加する
- [x] `mvn test` で全 PASS を確認する
- [x] self-check（OK/NG per completion criterion、checks/task-7.md に記録）
- [x] QA expert review（subagent）
- [x] language expert review（subagent）
- [x] software-engineering expert review（subagent）
- [x] user review ✓

**Completion criteria**:

- record-separator の CRLF/LF/CR 各シンボル変換パスをカバーするテストが存在する
- MESSAGE ブロック不在（`readMessageBlock` が null を返す）パスをカバーするテストが存在する
- `mvn test` が全テスト PASS する

---

### #8: 分類3 — Java イディオム コメント追加（7箇所）

**Purpose**: Java言語仕様上必要な実装（到達不能に見える防御コード）に説明コメントを追加し、読み手の混乱を防ぐ。

**Prerequisites**: #7

**Steps**:

- [x] `TestDataConverter` / `ConverterPathResolver` のプライベートコンストラクタ（既存コメントで対応済み）
- [x] `ConverterFileFilter` の `UncheckedIOException` ラップにコメント追加
- [x] `YamlTestDataValidator#loadSchema` の null ガード・`IOException` catch にコメント追加
- [x] `YamlTestDataValidator` の `RuntimeException` catch にコメント追加
- [x] `XlsFormatReader#stripQuotes` の null ガードにコメント追加
- [x] `YamlFormatWriter#emitBlock` の else-throw にコメント追加
- [x] `XlsFormatReader#toRecordLayouts` / `requireLine` の `IllegalStateException` にコメント追加
- [x] `StubDbInfo` 未カバーメソッド群にコメント追加
- [x] `TestCoreReaderAdapter` `HeaderCollector` / `BodyLineCollector` の抽象メソッド実装にコメント追加
- [x] `XlsFormatReader#readMessageBlock` の null リターンにコメント追加（task #7 で実施済み）
- [x] self-check（checks/task-8.md に記録）
- [x] user review

**Completion criteria**:

- 各箇所にコメントが追加されており、読み手がなぜそのコードが存在するか理解できる
- コードロジックは一切変更されていない（コメント追加のみ）
- `mvn test` が全テスト PASS する

---

### #5: Adapter 群追加（4件＋テスト3件＋データ）

**Purpose**: 本体現ブランチ `convert-testdata-excel-to-text` の Adapter 群をコンバーターリポジトリへ受け入れる。パッケージプライベートアクセスのため、本体と同一パッケージ（`nablarch.test.core.reader` / `nablarch.test.core.file`）に配置する。

**Prerequisites**: #4（mvn test 全 PASS 後）

**Steps**:

- [x] 本体現ブランチから src/main 4件（TestCoreFileAdapter, YamlTestCoreAdapter, TestCoreReaderAdapter, StubDbInfo）をコピー配置
- [x] 本体現ブランチから src/test 3件＋データディレクトリをコピー配置
- [x] `mvn test` で全 PASS を確認（失敗は配置・依存で解決。実装変更不可）
- [x] 全追加ファイルを本体現ブランチと diff し package/import 以外の差分ゼロを確認
- [x] self-check（OK/NG per completion criterion、checks/task-5.md に記録）
- [x] QA expert review（subagent）
- [x] language expert review（subagent）
- [x] software-engineering expert review（subagent）
- [x] user review

**Completion criteria**:

- `src/main/java/nablarch/test/core/file/TestCoreFileAdapter.java` が存在する
- `src/main/java/nablarch/test/core/reader/` に YamlTestCoreAdapter, TestCoreReaderAdapter, StubDbInfo の3件が存在する
- `src/test/java/nablarch/test/core/` 配下にテスト3件＋データが存在する
- `mvn test` が全テスト PASS する
- 各追加ファイルが本体現ブランチと package/import を除いて完全一致する

---

### #4: mvn test 全 PASS・差分ゼロ確認

**Purpose**: `mvn test` で全テストが通ることと、全移動ファイルの実装無改変を確認する。

**Prerequisites**: #3

**Steps**:

- [x] `mvn test` を実行し結果を確認する
- [x] 失敗があれば pom 依存・リソース配置で解決する（コード変更不可）
- [x] 全移動ファイルを本体現ブランチと 1件ずつ diff し package/import 以外の差分がないことを確認する
- [x] self-check（OK/NG per completion criterion、checks/task-4.md に記録）
- [x] QA expert review（subagent）
- [x] user review

**Completion criteria**:

- `mvn test` が全テスト PASS する
- 全移動ファイルが本体現ブランチと package/import を除いて完全一致する（diff 結果がゼロ）
- 本体・yaml リポジトリに変更が加えられていない

### #9: 分類B — Java イディオム コメント追加（4箇所）

**Purpose**: カバレッジ未到達だが Java イディオム/防御的コードとして正当な4箇所にコメントを追加し、読み手の混乱を防ぐ。

**Prerequisites**: #8

**Steps**:

- [x] `XlsFormatWriter`/`YamlFormatWriter` の `parent != null` ガードにコメント追加
- [x] `XlsFormatWriter`/`YamlFormatWriter` の `isMarkerColumn` null チェックにコメント追加
- [x] `DataFormat` switch の `default: throw IllegalStateException` にコメント追加
- [x] `YamlFormatHandler` の `catch (IOException e)` → UncheckedIOException にコメント追加
- [x] self-check（OK/NG per completion criterion、checks/task-9.md に記録）
- [x] QA expert review（subagent）
- [x] language expert review（subagent）
- [x] software-engineering expert review（subagent）
- [x] user review

**Completion criteria**:

- 各箇所にコメントが追加されており、読み手がなぜそのコードが存在するか理解できる
- コードロジックは一切変更されていない（コメント追加のみ）
- `mvn test` が全テスト PASS する

---

### #10: 分類C — NTF仕様パス テスト追加（4箇所）

**Purpose**: JaCoCo 計測で未カバーだった NTF 仕様内コードパス4箇所にテストを追加し、カバレッジを向上させる。coverage-only テスト（NTF では発生しない入力・到達不能状態の強制・converter から呼ばれないメソッド）は削除する。

**Prerequisites**: #9

**Steps**:

- [x] coverage-only テスト3件を削除する（`serialize_keyContainingControlChar_isQuoted`・`readFile_directiveWithNullValue_preservesNullInDirectives`・`fragmentViewGetTypesReturnsNullWhenTypesNotSet`）
- [x] NTF仕様テスト4件が残っていることを確認する（`readNormalizesRecordSeparatorEmptyValueToNoneSymbol`・`readPassesThroughUnknownRecordSeparatorValue`・`readStripsQuotesFromQuotedGenericDirectiveValue`・`skipsExcludedSheetsFromXlsBook`）
- [x] `mvn test` で全 PASS を確認する
- [x] self-check（OK/NG per completion criterion、checks/task-10.md に記録）
- [x] QA expert review（subagent）
- [x] language expert review（subagent）
- [x] software-engineering expert review（subagent）
- [x] user review（ARGUMENTS による次タスク指示をもって承認とみなす）

**Completion criteria**:

- coverage-only テスト3件（`serialize_keyContainingControlChar_isQuoted`・`readFile_directiveWithNullValue_preservesNullInDirectives`・`fragmentViewGetTypesReturnsNullWhenTypesNotSet`）が削除されている
- NTF仕様テスト4件（`readNormalizesRecordSeparatorEmptyValueToNoneSymbol`・`readPassesThroughUnknownRecordSeparatorValue`・`readStripsQuotesFromQuotedGenericDirectiveValue`・`skipsExcludedSheetsFromXlsBook`）が存在する
- `mvn test` が全テスト PASS する
- テスト以外のコードロジックは一切変更されていない

---

### #11: pom.xml プラグイン化

**Purpose**: converter の pom.xml を `maven-plugin` packaging に変更し、ConverterMojo のビルド基盤を整える。

**Prerequisites**: #10

**Steps**:

- [x] `<packaging>maven-plugin</packaging>` を追加する
- [x] `org.apache.maven:maven-plugin-api`・`org.apache.maven.plugin-tools:maven-plugin-annotations`（scope=provided）を依存追加する。バージョンは親 POM（nablarch-parent 6u3）で管理されているか確認し、無ければ明示する。
- [x] `maven-plugin-plugin`（goalPrefix: `nablarch-testing-converter`）を build/plugins に追加する
- [x] `mvn -DskipTests package` で通ることを確認する（プラグイン記述子生成の成否確認）
- [x] self-check（OK/NG per completion criterion、checks/task-11.md に記録）
- [x] QA expert review（subagent）
- [x] user review

**Completion criteria**:

- `<packaging>maven-plugin</packaging>` が pom.xml に存在する
- `maven-plugin-api`・`maven-plugin-annotations`（scope=provided）が依存に存在する
- `maven-plugin-plugin`（goalPrefix: `nablarch-testing-converter`）が build/plugins に存在する
- `mvn -DskipTests package` が通る（プラグイン記述子が生成される）

---

### #12: ConverterMojo TDD実装

**Purpose**: `ConverterMojo`（Maven プラグイン `convert` goal）を TDD で実装する。テストを先に書いて RED を確認してから実装して GREEN にする。変換ロジックには一切手を入れず、Mojo は薄いラッパーに徹する。

**Prerequisites**: #11

**Steps**:

- [x] `ConverterMojoTest` を作成し RED を確認する（正常系・委譲 / 全パラメータ反映 / 不正形式 / 入力不在 / 上書き衝突 の5観点）
- [x] `ConverterMojo` を実装し GREEN にする（`@Mojo(name = "convert")`・Builder 組み立て・`ConverterException` → `MojoExecutionException` 変換）
- [x] pom.xml の `<skipErrorNoDescriptorsFound>true</skipErrorNoDescriptorsFound>` を削除し、`mvn -DskipTests package` でプラグイン記述子に `convert` goal が登録されることを確認する
- [x] `mvn test` で全テスト PASS（既存テストのリグレッションゼロ）を確認する
- [x] self-check（OK/NG per completion criterion、checks/task-12.md に記録）
- [x] QA expert review（subagent）
- [x] language expert review（subagent）
- [x] software-engineering expert review（subagent）
- [x] user review

**Completion criteria**:

- `src/main/java/nablarch/test/tool/converter/ConverterMojo.java` が存在する
- `src/test/java/nablarch/test/tool/converter/ConverterMojoTest.java` が存在する
- テストが5観点（正常系委譲・全パラメータ反映・不正形式・入力不在・上書き衝突）をカバーする
- `mvn test` が全テスト PASS する（既存テストのリグレッションゼロ）
- `ConverterException` → `MojoExecutionException` 変換が実装されている
- `DataFormat.fromArgument` の不正値時の挙動がテストで固定されている

---

### #13: pom.xml 依存スコープ修正

**Purpose**: `nablarch-core-dataformat` が `<scope>test</scope>` になっているためプラグイン実行時にクラスが見つからない不具合を修正する。あわせて本体コード（src/main）が参照する全依存のスコープを棚卸しする。

**Prerequisites**: #12

**Steps**:

- [x] `src/main/java` 配下が参照するライブラリ一覧を洗い出す（import 解析）
- [x] pom.xml の全依存と scope を照合し、test スコープで compile スコープが必要なものを特定する
- [x] `nablarch-core-dataformat` の `<scope>test</scope>` を削除する（compile スコープへ昇格）
- [x] 他に修正が必要な依存があれば修正する（なし）
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean install -Djacoco.skip=true` でビルドが通ることを確認する
- [x] self-check（OK/NG per completion criterion、checks/task-13.md に記録）
- [x] QA expert review（subagent）
- [x] language expert review（subagent — N/A: pom.xml only change, no prose/naming to review）
- [x] software-engineering expert review（subagent）
- [x] user review

**Completion criteria**:

- `nablarch-core-dataformat` が compile スコープになっている（`<scope>test</scope>` が削除されている）
- 本体コード（src/main）が参照する依存がすべて compile スコープになっている（棚卸し結果の報告）
- `mvn clean install` が通る

---

### #14: プラグインゴール実行検証

**Purpose**: ローカルインストール後に nablarch-example-batch で実際にプラグインゴールを実行し、xls→yaml・yaml→xls の両方向で変換が成功し、変換後 YAML でテストが通ることを確認する。

**Prerequisites**: #13

**Steps**:

- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean install -Djacoco.skip=true` で converter をローカルインストールする
- [x] `nablarch-example-batch` を適切なディレクトリにクローンする（まだ存在しない場合）
- [x] README のコマンド（xls→yaml）でプラグインゴールを実行し成功を確認する
- [x] README のコマンド（yaml→xls）でプラグインゴールを実行し成功を確認する
- [x] 変換後 YAML で `nablarch-example-batch` の `mvn test` を実行し全テスト PASS を確認する（12 tests, 0 failures）
- [x] README の手順に誤りがあれば pom.xml または README を修正する（`overwrite=true` 追記 — commit d1b8d7f）
- [x] self-check（OK/NG per completion criterion、checks/task-14.md に記録）
- [x] QA expert review（subagent）
- [x] user review

**Completion criteria**:

- `xls→yaml` 変換がプラグインゴール経由で成功し、YAML ファイルが生成されている
- `yaml→xls` 変換がプラグインゴール経由で成功し、XLSX ファイルが生成されている
- 変換後 YAML で `nablarch-example-batch` の `mvn test` が全テスト PASS する（テスト件数を報告）
- README に記載のコマンドがそのとおりに動作する（動作しない箇所は修正済み）

---

### #16: 重複カラム名 WARN ログ対応

**Purpose**: Excel のヘッダ行に重複カラム名が存在する場合、変換を止めずに後勝ちで続行し、WARN ログで重複を通知する。LIST_MAP ブロックと TABLE 系ブロック（SETUP_TABLE / EXPECTED_TABLE / EXPECTED_COMPLETED）が対象。README にも動作を追記する。

**Prerequisites**: #15

**Steps**:

- [x] 影響範囲を確認する（LIST_MAP と TABLE 系の両方に重複カラム問題が存在することを自分で確認する）
- [x] `XlsFormatReader#readListMapBlock` に重複検出ロジックを追加し、重複があれば後勝ちで列名を正規化し WARN ログを出す（`java.util.logging.Logger` を使用）
- [x] `XlsFormatReader#readTableBlocks` に同様の重複検出・WARN ロジックを追加する
- [x] 重複カラム名を含む Excel テストデータ（または相当するテストフィクスチャ）を作成し、WARN ログが出ることを確認するテストを追加する
- [x] `mvn clean test -Djacoco.skip=true` で全テスト PASS を確認する
- [x] README に「重複カラム名があった場合の動作」セクションを追記する
- [x] self-check（OK/NG per completion criterion、checks/task-16.md に記録）
- [x] QA expert review（subagent）
- [x] Craft expert review（subagent, coding）
- [x] Verification expert review（subagent, test）

**Completion criteria**:

- LIST_MAP ブロックのヘッダ行に重複カラム名がある場合、変換が続行され WARN ログが出力される（ファイル名・シート名・重複カラム名・採用値を含む）
- TABLE 系ブロック（SETUP_TABLE / EXPECTED_TABLE / EXPECTED_COMPLETED）のヘッダ行に重複カラム名がある場合も同様に WARN ログが出力される
- 後勝ち（後方の列の値を採用）で上書きされる（NTF 実行時の TreeMap.put() と同じ挙動）
- 重複カラムを含むテストデータで WARN が出ることを確認するテストが存在する
- `mvn clean test -Djacoco.skip=true` が全テスト PASS する
- README に重複カラム名があった場合の動作が明記されている

---

### #15: LIST_MAP 列順保持修正

**Purpose**: Excel → YAML 変換時に LIST_MAP ブロックの列順がアルファベット順になる不具合を修正する。`nablarch-testing` 本体の `HeaderLine#getEffectiveColumnNames()` が持つ順序付き列名リストを converter 側へ届け、Excel の記述順を保持する。

**Prerequisites**: #14

**Steps**:

- [x] `nablarch-testing` 本体（`convert-testdata-excel-to-text` ブランチ）の関連クラス（`HeaderLine`, `ListMapParser`, `TestCoreReaderAdapter`）を読み、`getEffectiveColumnNames()` が公開されているかを確認する
- [x] `getMapExcludingMarkerColumns()` の TreeMap を変更する方針 vs 本体を変更せず converter 側で列順を取得する方針の影響範囲を調査し、採用方針を決定する
- [x] 採用方針に従い実装する（`XlsFormatReader#readListMapBlock` の列順取得ロジックを修正）
- [x] `nablarch-example-batch` の Excel → YAML → Excel ラウンドトリップで列順が保持されることを確認する
- [x] `mvn clean test -Djacoco.skip=true` で全テスト PASS を確認する
- [x] self-check（OK/NG per completion criterion、checks/task-15.md に記録）
- [x] QA expert review（subagent）
- [x] Craft expert review（subagent, coding）
- [x] Verification expert review（subagent, test）
- [x] Design expert review（subagent — 構造/アプローチを変更するため）

**Completion criteria**:

- `nablarch-example-batch` の `ImportZipCodeFileActionRequestTest` を Excel → YAML 変換すると、`testShots` の列順が元のExcel（`no, description, expectedStatusCode, setUpTable, expectedTable, setUpFile, expectedLog, diConfig, requestPath, userId`）と一致する
- YAML → Excel で戻したExcelの `testShots` 列順が元のExcelと一致する
- LIST_MAP 以外のブロック（SETUP_TABLE / EXPECTED_TABLE / SETUP_VARIABLE / EXPECTED_VARIABLE）の列順が変換前後で保持される（デグレなし）
- マーカーカラム（`[no]` 等）が変換後のYAML / Excelに含まれない（除外が機能している）
- `nablarch-example-batch` の `mvn test` が全テスト PASS する（テスト件数を報告する）
- 本体（`nablarch-testing`）を変更した場合は本体のテストがすべて PASS する
- 採用した方針（本体を変更したか否か）と理由が checks/task-15.md に記録されている
- `mvn clean test -Djacoco.skip=true` が全テスト PASS する

---

# Decisions

## ビルド環境

- Java: OpenJDK 17 (Temurin-17.0.19) — `/usr/lib/jvm/temurin-17-jdk-amd64`
- Maven: 3.9.9
- コンパイル・テスト・インストールはすべて Java 17 で実施する
- `JAVA_HOME` が環境変数に設定されていないため、明示が必要:
  ```sh
  JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn ...
  ```
- 通常のテスト実行コマンド:
  ```sh
  JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true
  ```
  （JaCoCo の offline instrumentation が `-Djacoco.skip=true` なしだと失敗するため）
- install コマンド:
  ```sh
  JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean install -Djacoco.skip=true
  ```

## JaCoCo カバレッジ取得手順（設定変更不要）

親 POM に Offline Instrumentation 設定済み。以下のコマンドで取得できる：

```sh
# 1. 計測・テスト実行
mvn clean jacoco:instrument test jacoco:restore-instrumented-classes

# 2. レポート生成（exec はプロジェクトルートに出力される）
mvn jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec
```

- `jacoco.exec` の出力先はプロジェクトルート（`${user.dir}/jacoco.exec`）
- `target/site/jacoco/` に HTML レポートが生成される
- `pom.xml` への追記・`argLine` 変更は不要

## フェーズ2 フィクスチャ方針（plan gate 承認時に確定・2026-08-12）

**採用: POI 生成方式 + 実物 `.xlsx` 1本の参照フィクスチャ。**

- 辺①の `.xlsx` フィクスチャはテスト実行時に POI で組み立てる。バイナリの静的同梱はしない。
- ただし POI 生成物と Excel 保存物の読み取り結果が同一である保証はないため、実物 `.xlsx` を **1本だけ** 参照フィクスチャとして同梱し、同じ読み取り結果になることを確認する（Assumptions の「固定バイナリを同梱しない」に対するユーザー承認済みの例外）。
- 参照フィクスチャの候補: `nablarch-example-web` の `origin/main`
  `src/test/java/com/nablarch/example/app/web/action/ClientActionTest.xlsx`。
  真正な Excel 保存物であることを確認済み（`docProps/app.xml` に
  `<Application>Microsoft Excel</Application>`・`AppVersion 16.0300`、`docProps/core.xml` の
  `dcterms:modified` は 2020-06-24）。同ブランチの他 5 ファイルも同様に利用可能。
- 同一性が確認できない場合は `issues.md` に「未確認」として記録し、差分の内容を残す。

## 軸D の対象範囲（ユーザー確定・2026-08-13）

**converter の入出力は「NTF が実行できるテストデータ」に限る。** それを外れる入力の挙動は
担保対象でも記録対象でもない。不正な入力にどこまで対応するかに線は引けないためである。

| 形式 | 条件 | 出典 |
|---|---|---|
| Excel | 全セルが文字列書式。それ以外は動作を保証しない | `nablarch-testing` の `PoiXlsReader` クラス Javadoc |
| YAML | 行の値は `["string","null"]` | `nablarch-testing-yaml` の `nablarch/test/ntf-testdata-yaml-schema.json` |

**辺① 軸D は 8 ケース**（D1-01 文字列／D1-05 先頭ゼロ文字列／D1-12 セル不在／D1-13 空文字／
D1-14 前後空白／D1-15 改行／D1-16 リテラル `null`／D1-17 表示形式 `@` の数値セル）。
対象外にしたのは、表示形式を持たない数値セル・日付書式・時刻書式・日時書式・数式・真偽値・エラー値。
**タグは振り直さないため番号に欠番が出る。**

**D1-17 だけは残す。** 表示形式 `@` は文字列書式であり但し書きを満たすが、セル種別が数値であるため
値が `1` → `"1.0"` に変わる。前提の内側で値が変わる唯一のケースである。参照フィクスチャ
`ProjectActionRequestTest.xlsx` の `downloadNormal` シート `A19` が
`<c r="A19" s="37"><v>1</v></c>`（`t` 属性なし＝数値セル）で `cellXfs[37]` の `numFmtId` が 49 ＝ `@` であり、
Excel が実際に保存した版面に存在する形であることを確認済み。

**辺② 軸D はスキーマから導き直す。** 別途渡された「YAML スカラー 10 ケース」表を根拠にしない。
`ntf-testdata-yaml-schema.json` が `rows` の値を `["string","null"]` に強制しているため、
スキーマを通る YAML で現れうるスカラーだけが対象になる。#24 の着手時にスキーマの該当箇所
（`table_data` / `list_map_data` / `record_fragment` の `rows`）を開いて導出し、
根拠を出典付きで `checks/task-24.md` に書く。

**忠実性はテストではなくコード構造で保証されている。** `XlsFormatReader` は自前の POI 解析を持たず
`PoiXlsReader` をそのまま注入して使う（`XlsFormatReader.java:81`）。「実セル → 文字列」の区間が
NTF 本体と同一コード 1 本であるため、変換の前後で値は変わらない。

## Fake 経路の担保をどう数えるか（#21 のレビューで確定・2026-08-12）

**辺①では、`FakeTestDataReader` 経路の担保を実 `.xlsx` 経路の担保として数えない。**

- #18 の棚卸しは Fake 経路のテストも ✅ に数えていた。#20 の Purpose が「既存33件は Fake 経路のため流用せず実ファイル経由で組み直す」としたことで基準が変わり、#21 で E-3(複数) と F1-05（カラム名重複）の 2 要素が「#18 は ✅ だが実 `.xlsx` 経路では空欄」と判明して追加した。
- **#22 以降（辺②③④）へ機械的に広げない。** 辺①でこの基準を採ったのは、`PoiXlsReader` の「実セル → 文字列行」区間が Fake 経路では一度も動かず、そこに軸D（セル種別）の挙動が集中しているためである。追加するかは「Fake 経路と実ファイル経路で結果が分岐しうるか」で判断し、分岐しないなら重複テストとして書かない（Rules の「重複テストを書かない」が優先）。判断の根拠は対応表に書く。

---

### #17: Javadoc からの外部文書参照の除去（全 19 件）

**Purpose**: Javadoc・コメントを自己完結させる。読者がリポジトリ外を参照しなくても内容が分かる状態にする。

**Prerequisites**: #16

**Steps**:

- [x] 全 19 件（分類 A）の括弧ごと削除を実施する
- [x] `grep -rn "設計書\|解説書\|設計図\|仕様書\|§[0-9]" src --include=*.java` がゼロになることを確認する
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` で全テスト PASS を確認する
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn javadoc:javadoc` で警告数が増えていないことを確認する
- [x] self-check（OK/NG per completion criterion、checks/task-17.md に記録）
- [x] QA expert review（subagent）
- [x] Craft expert review（subagent, writing）
- [x] Verification expert review（subagent, fact-check）

**Completion criteria**:

- `grep -rn "設計書\|解説書\|設計図\|仕様書\|§[0-9]" src --include=*.java` の出力がゼロ
- 分類 A 19 件すべてで括弧ごと削除のみ実施（ロジック・アサーション内容は無変更）
- `mvn clean test -Djacoco.skip=true` が全テスト PASS する
- `mvn javadoc:javadoc` が通り、警告数が作業前から増えていない

---

### #18: 既存テスト 4辺分の軸棚卸し

**Purpose**: 辺①33件・辺②20件・辺③40件・辺④33件の既存テストが、軸A〜F のどの要素を担保しているかを1件ずつ棚卸しし、欠けを可視化する。以降の追加タスクはこの表の空欄だけを埋める。

**Prerequisites**: #17

**Steps**:

- [x] `XlsFormatReaderTest`（33件）・`YamlFormatReaderTest`（20件）・`XlsFormatWriterTest`（40件）・`YamlFormatWriterTest`（33件）の全テストメソッドを列挙する
- [x] 軸C の全フィールドを実クラス（`TestDataContainer` / `TestDataSection` / `TestDataBlock` / `ColumnRowDataBlock` / `FileDataBlock` / `MessageDataBlock` / `RecordLayout` / `FieldDef`）から読み取り、省略可能なフィールドを特定する
- [x] 軸A の `DataType` 14種を `nablarch.test.core.reader.DataType` の実定義と突き合わせ、14種であることを確認する（ユーザー側でも `DEFAULT` ＋13 の計14種と確認済み。突き合わせは省略せず実施し、差異があれば実定義を正とし記録する）
- [x] 各テストメソッドを軸A〜F の要素へ対応付け、`.rn/ntf-test-data-converter/coverage/inventory.md` に4辺ぶんの棚卸し表として記録する
- [x] 各辺の空欄（未担保の軸要素）を一覧として同ファイルに明記する
- [x] self-check（OK/NG per completion criterion、checks/task-18.md に記録）
- [x] QA expert review（subagent）
- [x] Craft expert review（subagent, writing）
- [x] Verification expert review（subagent, fact-check）

**Completion criteria**:

- `.rn/ntf-test-data-converter/coverage/inventory.md` に4辺ぶんの棚卸し表があり、既存126件（33+20+40+33）の全テストメソッド名が漏れなく載っている
- 軸A の要素が `DataType` の実定義と一致している（14種でない場合はその旨と実際の要素が記録されている）
- 軸C の対象フィールドが実クラスの定義と一致し、省略可能なフィールドが識別されている
- 各辺について未担保の軸要素が一覧化されており、以降のタスクが埋めるべき対象が確定している
- src/main・src/test への変更がゼロ（棚卸しのみ）

---

### #19: 辺① 実 `.xlsx` フィクスチャ基盤と軸D（セル種別8ケース）

**Purpose**: 実 `.xlsx` を入力に `XlsFormatReader` を駆動するテスト基盤を作り、セル種別8ケースが中間モデルへどう入るかを現状の挙動として記録・固定する。`PoiXlsReader` の「実セル → 文字列行」区間を初めて実行させる。

**Prerequisites**: #18

**Steps**:

- [x] POI で `.xlsx` を組み立てるテストフィクスチャヘルパを作る（セル種別・書式・数式・エラー値を指定できること）
- [x] 実物 `.xlsx` 1本（`nablarch-example-web` `origin/main` の `ClientActionTest.xlsx`）を参照フィクスチャとして `src/test/resources` へ取り込む
- [x] 同じシート内容を POI で生成し、参照フィクスチャと `XlsFormatReader` の読み取り結果が一致することを確認する。一致しなければ差分を `issues.md` に「未確認」として記録する
- [x] 8ケースそれぞれのセルを含む `.xlsx` を生成し、`new XlsFormatReader().read(...)` で読んだ結果を**まず実行して記録する**（期待値を先に決めない）。対象範囲は Decisions「軸D の対象範囲」に従う
- [x] 記録した現状挙動を `.rn/ntf-test-data-converter/coverage/issues.md` の観点で評価し、仕様として妥当なものはテストで固定する
- [x] 妥当でないと判断したもの・挙動を固定できなかったものは `issues.md` に課題として記録する（**修正しない**）
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` で全 PASS を確認する
- [x] self-check（OK/NG per completion criterion、checks/task-19.md に記録）
- [x] QA expert review（subagent）
- [x] Craft expert review（subagent, coding）
- [x] Verification expert review（subagent, test）
- [x] Design expert review（subagent — フィクスチャ基盤という構造を新設するため）

**Completion criteria**:

- 実 `.xlsx` ファイルを入力として `XlsFormatReader` を駆動するテストが存在し、`FakeTestDataReader` を経由していない
- 実物 `.xlsx`（Excel 保存物）1本が参照フィクスチャとして同梱され、POI 生成物と同じ読み取り結果になることが確認されている（確認できない場合は差分が `issues.md` に「未確認」として記録されている）
- 軸D の8ケース（文字列／先頭ゼロ文字列／セル不在／空文字／前後空白／改行／リテラル `null`／表示形式 `@` の数値セル）すべてについて、中間モデルへ入る値がアサートされている（挙動を固定できなかったケースは `issues.md` に記録されている）
- 各ケースの結果が「実行して記録した現状の挙動」であり、実装を期待値に合わせて変更していない（src/main の diff がゼロ）
- 仕様として不適切と判断した挙動が `issues.md` に記録され、かつ修正されていない
- `mvn clean test -Djacoco.skip=true` が全テスト PASS する

---

### #20: 辺① 軸A・B・C（実ファイル経由）

**Purpose**: 14の `DataType`、4種のブロック実装、中間モデルの全フィールドが、実 `.xlsx` から中間モデルへ正しく入ることを固定する。既存33件は `FakeTestDataReader` 経由のため流用せず、実ファイル経由で組み直す。

**Prerequisites**: #19

**Steps**:

- [x] 14の `DataType` それぞれについて、実 `.xlsx` から中間モデルへ入ることをアサートするテストを追加する
- [x] 軸B の4種（`TableDataBlock` / `ListMapBlock` / `FileDataBlock` / `MessageDataBlock`）が実ファイル経由で生成されることをアサートする
- [x] 軸C の全フィールドを非デフォルト値でアサートする。**省略可能なのは実定義上 `groupId` / `recordType` / `FieldDef.type` / `FieldDef.length` の4件のみ**（#18 で確認）。この4件は「値あり」「省略」の双方を通す。`directives` / `fwHeaderFields` は「非空」「空 Map」の双方を通す。`identifier` は必須スカラー、`fileType` は `FIXED`/`VARIABLE` の2値であり「省略」の表現を持たないため双方通しの対象外とする
- [x] `FileDataBlock.fileType` の `FIXED` / `VARIABLE` 両方を通す
- [x] `DataType.DEFAULT` はリーダ経路では生成されない（`TestCoreReaderAdapter` L362 が DEFAULT ブロックをスキップする）。辺①では「到達不能」として理由付きで空欄に残す
- [x] `TestDataContainer.sections` は `XlsFormatReader#read` L133 が `Collections.singletonList(section)` を返すため常に1件。「空」「複数」はいずれも到達不能として理由付きで空欄に残す
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` で全 PASS を確認する（354 件 PASS）
- [x] self-check（OK/NG per completion criterion、checks/task-20.md に記録）
- [x] QA expert review（subagent） — ラウンド3 で PASS（`0811032`）
- [x] Craft expert review（subagent, coding） — ラウンド2 で PASS（`d9293bb`）
- [x] Verification expert review（subagent, test） — ラウンド2 で PASS（`d9293bb`）

**Completion criteria**:

- 軸A の14種すべてが、実 `.xlsx` を入力とするテストで1回以上中間モデルへ入っている
- 軸B の4種すべてが実 `.xlsx` 経由で生成されアサートされている
- 軸C の全フィールドが非デフォルト値で1回以上アサートされ、省略可能なフィールドは省略時の挙動もアサートされている
- `fileType` の `FIXED` / `VARIABLE` 双方がアサートされている
- src/main への変更がゼロ
- `mvn clean test -Djacoco.skip=true` が全テスト PASS する

---

### #21: 辺① 軸E（多重度）・軸F（異常系）

**Purpose**: 実 `.xlsx` 入力に対する多重度と異常系の挙動を固定する。

**Prerequisites**: #20

**Steps**:

- [x] 軸E: 1セクションに 0／1／複数ブロック、1ブロックに 0／1／複数行、1ファイルに 0／1／複数レコードレイアウトのテストを追加する。「1ブックに複数シート」は `XlsFormatReader.read` が `"ブック名/シート名"` の1シート単位 API（`XlsFormatReader` L96-101）のため到達不能。理由付きで空欄に残す
- [x] 軸F: シート不在／ブック破損／未知のデータタイプ名／マーカーカラム欠落／カラム名重複／行と列の数の不一致のテストを追加する（現状の挙動をまず記録してから固定する）
- [x] F1-06（行と列の数の不一致）で、#20 が「到達不能」と判定した C-17／C-20 の根拠を実行可能なテストにする。3 入力（名前行 1 列／型行が名前行より短い／型セルが中間位置で空）それぞれで例外型とメッセージをアサートし、`issues.md` の「到達不能」表からそのテストメソッド名を参照する。#20 時点では C-17／C-20 だけが散文の記述のみで、本体パーサの挙動が変われば到達可能に変わっても検出できない状態になっている（C-11／C-13／C-16 は根拠テストを持つ）
- [x] 異常系のうち仕様として不適切と判断した挙動を `issues.md` に記録する（**修正しない**）
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` で全 PASS を確認する（376 件 PASS）
- [x] self-check（OK/NG per completion criterion、checks/task-21.md に記録）
- [x] QA expert review（subagent） — ラウンド3 で PASS
- [x] Craft expert review（subagent, coding） — ラウンド3 で PASS
- [x] Verification expert review（subagent, test） — ラウンド2 で PASS

**Completion criteria**:

- 軸E の3観点（セクション内ブロック数／ブロック内行数／ファイル内レコードレイアウト数）それぞれで 0／1／複数がアサートされている。「ブック内シート数 複数」は到達不能として根拠付きで空欄に残されている
- 軸F の6ケース（シート不在／ブック破損／未知データタイプ名／マーカーカラム欠落／カラム名重複／行列数不一致）すべてで、例外型・メッセージまたは継続時の結果がアサートされている
- C-17／C-20 の「到達不能」が F1-06 のテストで実行可能な根拠を持ち、`issues.md` の「到達不能」表がそのテストメソッド名を参照している
- src/main への変更がゼロ
- `mvn clean test -Djacoco.skip=true` が全テスト PASS する

---

### #22: 辺③ 軸D（セル型8ケース）・軸F（異常系）

**Purpose**: `XlsFormatWriter` が書き出す Excel のセル型を `getCellType()` で検証し、文字列として書かれること・数式解釈されないことを固定する。異常系の挙動も固定する。

**Prerequisites**: #18

**Steps**:

- [x] 軸D の8ケース（`"100"` ／ `"=1+1"` ／ `"007"` ／ `null` ／ `""` ／改行含む文字列／32767文字超／制御文字含む）を書き出し、読み返して `getCellType()` をアサートするテストを追加する（現状の挙動をまず記録してから固定する）
- [x] 軸F: 出力先不在／書き込み権限なし／シート名が Excel 制約違反（31文字超・禁止文字）のテストを追加する。`overwrite=false` 衝突は `XlsFormatWriter` が `overwrite` を保持せず（保持するのは `ConversionRequest` / `TestDataConverter` / `ConverterMojo`）、上位層の既存テスト（`TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse` L336・`ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict` L267）が衝突検査を通しているため辺③の対象外とし、根拠を対応表に記録する（**#22 で判明**: 当初この Steps に書いていた L331／L262 は実測とズレていた。また「上位層で担保済み」は担保範囲を広く言いすぎで、正確には `XlsFormatHandler#outputPaths` は実行されているが `.xlsx` が既存で**衝突する分岐**は未担保。詳細は `checks/task-22.md`）
- [x] 仕様として不適切と判断した挙動を `issues.md` に記録する（**修正しない**）— XLS-16〜XLS-19 の4件
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` で全 PASS を確認する（410 件 PASS）
- [x] self-check（OK/NG per completion criterion、checks/task-22.md に記録）
- [x] QA expert review（subagent） — ラウンド4 で PASS
- [x] Craft expert review（subagent, coding） — ラウンド4 で PASS
- [x] Verification expert review（subagent, test） — ラウンド2 で PASS

**Completion criteria**:

- 軸D の8ケースすべてで `getCellType()` がアサートされている（`getStringCellValue()` のみのアサートで終わっていない）
- `"=1+1"` が数式セルとして解釈されないこと、`"100"` が数値セルにならないことがアサートされている
- 軸F の3ケース（出力先不在／書き込み権限なし／シート名制約違反）で例外型または結果がアサートされている。`overwrite=false` 衝突は上位層で担保済みとして根拠付きで対象外にされている
- src/main への変更がゼロ
- `mvn clean test -Djacoco.skip=true` が全テスト PASS する

---

### #23: 辺③ 軸A・B・C・E の欠け補充

**Purpose**: #18 の棚卸しで空欄となった辺③の軸A・B・C・E の要素をテストで埋める。

**Prerequisites**: #22

**Steps**:

- [x] #18 の棚卸し表で辺③の空欄となっている軸A・B・C・E の要素を確認する（A 3／B 0／C 9／E 3 ＝ 15 要素）
- [x] 空欄の要素それぞれについてテストを追加する（軸C は省略可能フィールドの「値あり」「省略」双方）
- [x] `DataType.DEFAULT` は辺③では到達可能（`XlsFormatWriter#marker` が `getDataType().getName()` からマーカーを組み立てるだけでタイプを絞らない）。現状の挙動をまず実行して記録してから固定する。辺④は `serialize_unsupportedDataType_throws` のとおり `DEFAULT` を例外で弾くため、**辺③④で扱いが非対称**である。この非対称を `issues.md` に課題として記録する（**修正しない**）— XLS-20
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` で全 PASS を確認する（428 件 PASS）
- [x] self-check（OK/NG per completion criterion、checks/task-23.md に記録）
- [x] QA expert review（subagent） — ラウンド3 で PASS（`b86ee3d`）。担保テスト 15 件を変異で生存ゼロ確認
- [x] Craft expert review（subagent, coding） — ラウンド3 で PASS（`b86ee3d`）
- [x] Verification expert review（subagent, test） — `4905838` で PASS（22 変異・生存ゼロ）。以降 `src/` の差分はコメントのみ（コード行増減 0）につき再実行せず（ユーザー判断・2026-08-13）

**Completion criteria**:

- 辺③について軸A の14種（`DEFAULT` を含む。辺③では到達可能）・軸B の4種・軸C の全フィールド（省略可能なものは省略時も）・軸E の 0／1／複数がすべてアサートされている
- 辺③④の `DEFAULT` の扱いの非対称（辺③は書き出す／辺④は例外）が `issues.md` に記録され、かつ修正されていない
- #18 の棚卸し表で辺③に残っていた空欄が、埋まったか理由付きで残されたかのいずれかになっている
- src/main への変更がゼロ
- `mvn clean test -Djacoco.skip=true` が全テスト PASS する

---

### #24: 辺② 軸D（YAML スカラー10ケース）・軸A〜F の欠け補充

**Purpose**: YAML のスカラー型が中間モデルへどう入るかを固定し、#18 の棚卸しで空欄となった辺②の軸要素を埋める。

**Prerequisites**: #18

**Steps**:

- [ ] 軸D の10ケース（引用符なし文字列／引用符あり／数値／末尾ゼロ小数／`true`・`TRUE`・`yes`／`null`・`~`・値なし／`"null"`／日付風／複数行 `|`・`>`／先頭ゼロ）を実 YAML フィクスチャで読み、現状の挙動をまず記録してから固定する
- [ ] 軸F: スキーマ違反／YAML として不正／未知のキー／必須構造の欠落／空ファイルのテストを追加する
- [ ] #18 の棚卸し表で辺②の空欄となっている軸A・B・C・E の要素を埋める
- [ ] 仕様として不適切と判断した挙動を `issues.md` に記録する（**修正しない**）
- [ ] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` で全 PASS を確認する
- [ ] **台帳の記述規約の自己点検**（Rules 参照）: `grep -cE '\b(L[0-9]{1,4})\b' .rn/ntf-test-data-converter/coverage/inventory.md` → **0** であること。ファイル行数を書いていないこと。本タスクで書き足した件数のすべてに、それを導いたコマンドを併記していること。逆引き表（軸要素 → 担保テストメソッド）を新設していないこと
- [ ] self-check（OK/NG per completion criterion、checks/task-24.md に記録）
- [ ] QA expert review（subagent）
- [ ] Craft expert review（subagent, coding）
- [ ] Verification expert review（subagent, test）

**Completion criteria**:

- 軸D の10ケースすべてがアサートされ、特に `null` ／ `~` ／値なし の3者が区別されるか否か、`"null"` とリテラル NULL が区別されるか否かが結果として固定されている
- 軸F の5ケース（スキーマ違反／不正 YAML／未知キー／必須構造欠落／空ファイル）で例外型または結果がアサートされている
- 辺②について軸A の13種（`DEFAULT` を除く。`YamlFormatReader#addBlocksForSection` L106-133 が既知セクションキーのみを分岐に持ち `DEFAULT` を生成しないため到達不能。根拠付きで空欄に残す）・軸B の4種・軸C の全フィールド（省略可能なものは省略時も。`sections` は `YamlFormatReader#read` L94 が `Collections.singletonList` を返すため「空」「複数」とも到達不能として根拠付きで空欄）・軸E が埋まっている
- src/main への変更がゼロ
- `mvn clean test -Djacoco.skip=true` が全テスト PASS する

---

### #25: 辺④ 軸D（YAML 表現9ケース）・軸A〜F の欠け補充

**Purpose**: 中間モデルの値が YAML へどう書かれるかを固定し、辺②と対で往復可能性（引用符が落ちて再読込で型が変わらないか）を確認する。#18 の棚卸しで空欄となった辺④の軸要素も埋める。

**Prerequisites**: #24

**Steps**:

- [ ] 軸D の9ケース（`"100"` ／ `"true"` ／ `"null"` ／ `null` ／ `""` ／ `"007"` ／改行含む／`"2026-08-07"` ／コロン・ハイフン・`#` 含む）を書き出し、出力 YAML の記法をアサートするテストを追加する（現状の挙動をまず記録してから固定する）
- [ ] 各ケースについて、#24 で固定した辺②の読み取り挙動と突き合わせ、文字列が同じ文字列として復元されるか否かを判定し記録する
- [ ] 軸F: 出力先不在／書き込み権限なしのテストを追加する。`overwrite=false` 衝突は `YamlFormatWriter` が `overwrite` を保持しないため辺④の対象外とし、上位層で担保済みである根拠を対応表に記録する（#22 と同じ扱い）
- [ ] #18 の棚卸し表で辺④の空欄となっている軸A・B・C・E の要素を埋める
- [ ] 復元できない組み合わせがあれば `issues.md` に課題として記録する（**修正しない**）
- [ ] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` で全 PASS を確認する
- [ ] self-check（OK/NG per completion criterion、checks/task-25.md に記録）
- [ ] QA expert review（subagent）
- [ ] Craft expert review（subagent, coding）
- [ ] Verification expert review（subagent, test）

**Completion criteria**:

- 軸D の9ケースすべてで出力 YAML の記法（引用符の有無・複数行記法・NULL 表現）がアサートされている
- 9ケースそれぞれについて、辺④で書き辺②で読んだとき元の文字列が復元されるか否かが判定・記録されている（復元されない場合は課題として記録され、修正されていない）
- 軸F の2ケース（出力先不在／書き込み権限なし）で例外型または結果がアサートされている。`overwrite=false` 衝突は上位層で担保済みとして根拠付きで対象外にされている
- 辺④について軸A の14種・軸B の4種・軸C の全フィールド（省略可能なものは省略時も）・軸E が埋まっている
- src/main への変更がゼロ
- `mvn clean test -Djacoco.skip=true` が全テスト PASS する

---

### #26: カバレッジ計測と未到達分岐の列挙

**Purpose**: 4辺の担当クラスの行・分岐カバレッジを JaCoCo で計測し、未到達分岐を列挙して、テスト不要と判断したものに根拠を付ける。

**Prerequisites**: #21, #23, #25

**Steps**:

- [ ] Decisions 記載の手順（`mvn clean jacoco:instrument test jacoco:restore-instrumented-classes` → `mvn jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec`）でカバレッジを取得する
- [ ] `XlsFormatReader` / `XlsFormatWriter` / `YamlFormatReader` / `YamlFormatWriter` / `TestCoreReaderAdapter` / 中間モデル各クラスの行・分岐カバレッジ数値を `.rn/ntf-test-data-converter/coverage/coverage-report.md` に記録する
- [ ] 未到達の分岐を1件ずつ（クラス・メソッド・行番号つきで）列挙する
- [ ] 各未到達分岐を「テストを足すべき」「テスト不要」に分類し、テスト不要には根拠を書く（Java イディオム／到達不能／NTF 仕様外 など）
- [ ] self-check（OK/NG per completion criterion、checks/task-26.md に記録）
- [ ] QA expert review（subagent）
- [ ] Craft expert review（subagent, writing）
- [ ] Verification expert review（subagent, fact-check）

**Completion criteria**:

- `.rn/ntf-test-data-converter/coverage/coverage-report.md` に対象6区分すべての行・分岐カバレッジ数値が記録されている
- 未到達分岐がクラス・メソッド・行番号つきで漏れなく列挙されている
- 各未到達分岐が「テストを足すべき」「テスト不要」に分類され、後者には根拠が書かれている
- 「テストを足すべき」に分類されたものは、追加されたか `issues.md` へ残課題として記録されたかのいずれかになっている
- src/main への変更がゼロ

---

### #27: 4辺の軸×要素対応表と課題一覧の提出

**Purpose**: 4辺ぶんの軸×要素対応表を、各要素に担保テストメソッド名を記した形で完成させ、本作業で見つかった課題を一覧として確定する。

**Prerequisites**: #26

**Steps**:

- [ ] `.rn/ntf-test-data-converter/coverage/axis-matrix.md` に4辺ぶんの軸×要素対応表を作る。各要素に担保テストメソッド名を記す
- [ ] 空欄が残る要素には理由を書く
- [ ] 表に記したテストメソッド名が実在することを、テストソースと突き合わせて確認する
- [ ] `issues.md` を通読し、#19〜#26 で記録した課題が漏れなく載っていること・いずれも修正されていない（src/main 無変更）ことを確認する
- [ ] self-check（OK/NG per completion criterion、checks/task-27.md に記録）
- [ ] QA expert review（subagent）
- [ ] Craft expert review（subagent, writing）
- [ ] Verification expert review（subagent, fact-check）

**Completion criteria**:

- `axis-matrix.md` に辺①〜辺④の4表があり、軸A〜F の全要素が行として存在する
- 各要素に担保テストメソッド名が記されており、記された全メソッド名がテストソースに実在する
- 空欄の要素には理由が書かれている
- `issues.md` に本作業で見つかった課題が一覧化されており、`git diff` 上 src/main への変更がゼロであることが確認されている

---

### #28: Evaluation sign-off

**Purpose**: Acceptance criteria を通しで実行し、その結果をユーザーへ提示して承認を得る。

**Prerequisites**: #27

**Steps**:

- [ ] Acceptance criteria（フェーズ2分を含む全項目）を1件ずつ検証し、結果をまとめる
- [ ] 結果をユーザーへ提示し、`/rn:ty`（承認）または `/rn:gm`（差し戻し）の判定を受ける

**Completion criteria**:

- Acceptance criteria の実行結果がユーザーに承認されている

---

# State

- **Status**: paused
- **Date**: 2026-08-13
- **Last completed**: #23 辺③ 軸A・B・C・E の欠け補充（`bb58d05`）。その後 軸D 辺① の絞り込み（`227adc1`）と追随修正（`a51a8b3`）を実施
- **Next**: #24（辺② 軸D・軸A〜F の欠け補充）。**軸D のケース定義をユーザーに提示したところで中断した**
- **Notes**: ブランチ `ntf-test-data-converter` / PR #1 https://github.com/nablarch/nablarch-testing-converter/pull/1。`src/main` 無変更（`git diff HEAD -- src/main` → 0 行）。
  **基準線**: `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` →
  **`Tests run: 419, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`**
  （yaml jar は 2026-08-13 17:04 インストールの `190cc9a` 差し戻し版。428 → 419 は軸D 辺① の絞り込みで 9 件削除したため）。
  **🚦 再開時に最初にすること — ユーザーの回答待ちが 2 件ある**（`checks/task-24.md` に導出と根拠を記録済み）:
  (1) 辺② 軸D の 10 ケース定義でよいか。特に **`~` を D2-06(NULL) から D2-07(NULL に見える文字列) へ移した**点と、
  **D2-10 に `0x1F` を足した**点。実測では引用符なしの `null`・値なしは Java `null` だが `~` は文字列 `"~"` になる。
  (2) 大文字小文字で挙動が割れる件（`true` はスキーマ違反だが `TRUE`／`yes` は文字列として通る。
  `null` は Java `null` だが `Null`／`NULL` は文字列）を `issues.md` の課題として記録するか、スキーマ仕様どおりとして記録しないか。
  **回答が出るまで #24 のテストと台帳は書かない**（定義が変わると全部書き直しになるため）。
  **#24 の Steps 1 行目に残る旧 10 ケース表（引用符なしの `true`・`123` を含む）は実測でスキーマ違反と判明している。**
  上記 (1) の確定後に現行定義へ揃えること。
  **文書の揃え方（ユーザー確定・2026-08-13）**: 定義を変えたら `steering.md`／`inventory.md`／`issues.md` は
  指示に列挙が無くても現行定義へ揃えてよい。揃えないのは `checks/` だけ（時点の証拠記録）。
  **converter 側では扱わない件**: `rows: []` の期待値検証が素通りする（偽陰性）問題は yaml 側の FIXME として
  本体対応後に再検討される。converter 側で直さない・触らない。
  **#23 の Verification は再実行していない（ユーザー承認・2026-08-13）**: `4905838` で PASS 済み（22 変異・生存ゼロ）。以降 `src/` の差分はコメント／Javadoc のみでコード行の増減 0。
  **台帳の構造見直しで消したもの**: 逆引き表 §1.2／§2.2／§3.2／§4.2、訂正履歴 §3.1-4／§3.1-5、および他ファイルの行番号・ファイル行数。逆引きの正は #27 の `coverage/axis-matrix.md`。規約は Rules（フェーズ2）に 2 項追加し、#24 の Steps に self-check を入れた。**#24 は台帳を書く前にこの規約を読むこと。**
  **#27 への申し送り（`issues.md` に記録済み）**: (1) 軸E の `E-1(1 件)`・`E-4(1 件)` が台帳 §3.1 の軸E 欄に 1 行も現れない（実体は担保済みだが表の上では穴の形）。(2) 送信同期 4 種の担保が `XlsFormatWriterTest` と `XlsFormatWriterModelTest` の 2 クラスに分散している。
  **決着済み（ユーザー判断・2026-08-13）**: 「件数にコマンドを併記」の既存記述への遡及適用は行わない。#24 以降の新規記述に self-check で効かせる。
  **レビュー運用（#23 で確立）**: レビュア subagent には `isolation: worktree` を使う。#23 で QA に `src/main` への一時変異を許可したまま Craft と同じ作業ツリーで並走させ、Craft のテスト実行が変異を踏んだ。
