Rn version: 0.8.0

# Goal

nablarch-testing（ブランチ `convert-testdata-excel-to-text`）の変換ツール（src/main 28件）と形式間変換テスト（src/test 21件）を nablarch-testing-converter リポジトリへ切り出し、`mvn test` 全 PASS・実装無改変を確認する。

（フェーズ2）切り出した converter の4つの変換辺（① Excel→中間モデル／② YAML→中間モデル／③ 中間モデル→Excel／④ 中間モデル→YAML）の変換ルールを、converter 自身のユニットテストで担保する。担保の網羅は主観で判断せず、6軸（A データタイプ／B ブロック実装／C 中間モデル全フィールド／D 値の表現／E 多重度／F 異常系）を4辺それぞれについて埋め、軸×要素対応表とカバレッジ計測で客観的に示す。

# Acceptance criteria

- converter の `mvn test` が全テスト PASS する
- 全移動ファイルが本体現ブランチと package/import を除いて完全一致（実装無改変）。**ただし #25.5 で修正した src/main のファイルは除外し、除外したファイル名・課題ID・変更理由を併記する**（ユーザー確定・2026-08-14）
- pom.xml が yaml・本体・poi 3.8・snakeyaml 3.0.1 の依存を正しく持つ
- 本体（nablarch-testing）・yaml（nablarch-testing-yaml）に一切書き込んでいない
- ブランチが push 済み

（フェーズ2）

- 辺①に実 `.xlsx` を入力とするテストが存在し、軸A（`DataType` 14種）すべてが実ファイル経由で1回以上通っている
- 4辺それぞれで、軸B（`TestDataBlock` sealed 階層 4種）と軸C（中間モデル全フィールド。省略可能なフィールドは「値あり」「省略」の双方）が非デフォルト値で1回以上 IN／OUT されている。`FileDataBlock.fileType` は `FIXED`／`VARIABLE` の両方を通す
- 軸D が4辺すべてでアサートされている（辺① セル種別8ケース／辺③ セル型8ケース（`getCellType()` をアサート）／辺② スキーマから導出したスカラー12ケース／辺④ YAML 表現9ケース）。ケース数の根拠は Decisions「軸D の対象範囲」
- 4辺それぞれで軸E（0件／1件／複数件）と軸F（異常系）が埋まっている
- 参照フィクスチャとして同梱した実物 `.xlsx`（Excel 保存物）1本と、POI 生成フィクスチャの読み取り結果が同一であることが確認されている（確認できない場合は差分が `issues.md` に「未確認」として記録されている）
- 4辺ぶんの軸×要素対応表が成果物として存在し、各要素に担保テストメソッド名が記されている。空欄には理由が書かれている
- 4辺の担当クラス（`XlsFormatReader` / `XlsFormatWriter` / `YamlFormatReader` / `YamlFormatWriter` / `TestCoreReaderAdapter` / 中間モデル各クラス）の行・分岐カバレッジが計測され、未到達分岐が列挙されている。テスト不要と判断したものには根拠が書かれている
- 本作業で見つかった現状挙動の課題が課題一覧へ記録されている。**#25.5 で要対応と判定した 6 件は修正され、それ以外は修正されずに記録のみである**（ユーザー確定・2026-08-14 の方針変更に合わせて改訂。改訂前は「修正されずに」が全件に掛かっていた）
- `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` が全テスト PASS する

# Assumptions

- 全リポジトリは同じ親ディレクトリに clone 済み
- nablarch-testing-yaml は構築・公開済み（converter のビルドに必要）
- 本体は `convert-testdata-excel-to-text` ブランチのまま（移動元）
- テストデータは静的同梱ではなく変換テストが実行時に一時生成・参照する方式（.xls/.yaml の固定ファイルは不要）。**例外**（ユーザー承認済み）: POI 生成物と Excel 保存物の同一性を確認するため、実物 `.xlsx` 1本のみ参照フィクスチャとして同梱してよい
- YamlModeTestBase や *YamlTest・結合テストは integration 行きであり converter 対象外

# Rules

- 1 task = 1 commit
- （フェーズ1）実装の変更は一切しない。package/import の機械的調整と pom 設定のみ許可。**フェーズ2 の #25.5 だけがこの例外で、要対応 6 件の修正に限り src/main を変更する**（ユーザー確定・2026-08-14）
- 本体・yaml には書き込まない
- `mvn test` が通らず実装変更が要ると判断したら止めてユーザーに確認する
- タスク完了後は即 push し、PR を作成してユーザーがコードを PR 上で確認できるようにする

（フェーズ2）

- **未知の挙動を調べる段階では期待値を先に決めない。** まず現状の挙動を実行して記録し、それが仕様として妥当かを判断してから固定する。**不具合と判定済みのものは、仕様どおりの期待値を先に書く（TDD）**（ユーザー確定・2026-08-14）
- **本作業で見つかった不具合は、要対応と判定した 6 件（YML-02・YML-12・XLS-16・XLS-06・YML-08・XLS-22）に限り修正する。それ以外は従来どおり記録のみ**（当初 5 件・ユーザー確定 2026-08-14。**XLS-22 を追加・ユーザー確定 2026-08-18**）。記録先は `.rn/ntf-test-data-converter/coverage/issues.md`。修正対象の判定根拠と手順は Decisions「不具合修正の対象と手順（#25.5）」
- 各辺の担保を往復テスト（`RoundTripTest`）の追加で代替しない。ただし**既存**の往復テスト（`RoundTripTest` 30件、`XlsFormatWriterTest#roundTrips*` 8件、`YamlFormatWriterTest#roundTrip_*` 6件）が実ファイル経由で通している軸要素は、棚卸しに「🔺弱い担保」として必ず計上する（重複テストを書かないため）。正式担保としては数えず、直接テストの追加対象からは外さない
- 既存テストを軸で棚卸ししてから新規テストを足す。棚卸しなしの新規追加はしない
- 対応表・カバレッジを示さずに「網羅した」と報告しない
- **テストメソッドを増減させたら、`inventory.md` 内の該当する件数を「記憶している箇所を直す」のではなく、コマンドから導き直す。各件数にはそれを導いたコマンドを併記する**（#22 で確定・2026-08-13）。#22 ではラウンド3 で 3 テストを追加した際、`§3.1-2`・Javadoc・`issues.md` は更新されたのに `§3.3` の件数だけが取り残され、同一文書内で 16 と 18 が矛盾した。#23・#24・#25 はいずれもテストを追加して同じ `inventory.md` を更新するため、同じ取り残しが起きる
- **台帳に載せる出典コマンドは、そのまま実行して同じ結果が出ること。** 誤った結果を返すコマンドは件数の誤記と同じ扱いとする（#22 で確定・2026-08-13。`grep -rc` の `| grep -v ":0$"` 欠落、および自分自身がヒットして主張を反証する grep が実際に発生した）
- **担保の穴は、テストを足さない場合でも台帳に開示する。** 開示しないのは件数を誤るのと同じ性質の誤りとする（#22 で確定・2026-08-13）
- **台帳（`coverage/inventory.md`）に「他ファイルの行番号」「ファイル行数」「コマンドを併記しない件数」を書かない**（#23 後の構造見直しで確定・2026-08-13）。行番号とファイル行数は他ファイルを編集するたびに移動し、台帳を直すと台帳の別箇所が自己無効化する。識別はクラス名・メソッド名で行う。#22・#23 の計 5 ラウンドの FAIL はすべて台帳の記述精度であり、テストコードの欠陥ではなかった
- **同じ関係を 2 方向に手書きしない。** ひとつのテストメソッドを、台帳の中で「テストメソッド → 軸要素」と「軸要素 → 担保テストメソッド」の両方に書かない。**4 辺を通した逆引き（軸要素 → 担保テストメソッド）の正は #27 の `coverage/axis-matrix.md` とする**（#23 後の構造見直しで確定・2026-08-13。**文言は #24 の 2 巡目レビュー指摘を受けて 2026-08-14 に実態へ合わせた**）。既存クラスの棚卸しは §X.1 系（メソッド → 軸要素）、各タスクが新規追加したクラスの担保は §X.1-2 系（軸要素 → メソッド）に書く。この 2 つは対象クラスが重ならないため二重記載にならない。規約を入れた `3f9e665` の時点で §1.2-2・§3.1-2・§3.1-3 が既にこの形であり（`git show 3f9e665:.rn/ntf-test-data-converter/coverage/inventory.md | grep -c 担保テストメソッド` → 15）、当初の文言のほうが実態と食い違っていた
- **文書の揃え方**（ユーザー確定・2026-08-13）: 定義を変えたら `steering.md`／`coverage/inventory.md`／`coverage/issues.md` は指示に列挙が無くても現行定義へ揃えてよい。揃えないのは `checks/` だけ（時点の証拠記録であるため）
- **レビュア subagent は `isolation: worktree` で起動する**（#23 で確立・2026-08-13）。レビュア には `checks/{task-id}.md`（自己点検）を渡さない・読ませない
- **順序を主張するテストは、フィクスチャを最初から定義順・辞書順とずらして作る**（#24 の教訓・ユーザー確定・2026-08-14）。#24 で 3 巡かけて出た生存変異 9 件は、共通原因が「順序を主張する入力が辞書順・定義順と一致していた」ことだった。一致していると、順序を壊す変異を入れてもテストが通ってしまい、アサートが順序を担保していない。これは辺②に固有の話ではないため、#25 以降でフィクスチャを作るときは書く時点でずらす。3 巡かけて見つけるより書くときに外すほうが安い
- **作業対象と無関係なファイルの変更は、独立した `chore:` コミットに分ける**（ユーザー確定・2026-08-14）。`.gitignore` への `.claude/worktrees/` 追加が `c15d531`（辺②のレビュー反映コミット）に相乗りしていた。履歴は書き換えないが、以降は分ける

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

### ローカルリポジトリの扱い（ユーザー確定・2026-08-14）

- **本セッションは既定の `~/.m2` をそのまま使う。** `-Dmaven.repo.local` を指定しない。`MAVEN_ARGS` も設定しない（設定するのは配布リハーサル側だけ）。
- 対象PJ配布のリハーサルは専用のローカルリポジトリ `~/work/pj111/.m2-rehearsal/repository` へ分離済みで、`~/.m2` はもう共有されていない。
- **`mvn -U` を打たない。** SNAPSHOT を取り直すと外の作業と競合する。解決できない成果物が出たら、自分で埋めずに成果物名と版を挙げて報告する。
- **converter の `pom.xml` を配布用のピン留め版（`1.0.0-r190cc9a` 等）へ追随させない。** ピン留めは対象PJへ配る成果物の版であって開発リポの版ではなく、当て方はリハーサル側 clone の patch が持つ。開発リポを過去の版へ固定すると、yaml が次に進んだとき辺②の担保が実物からずれる。

### レビュア用 worktree の除外先（ユーザー確定・2026-08-14）

**`.gitignore` の `.claude/worktrees/` は残す。`.git/info/exclude` へ移さない。**
このリポは `.rn/` 配下をすでに追跡下に置いており、エージェントの作業物を本体に持つ方針を取っている。
その中で `.claude/worktrees/` だけを除外の対象外にする理由が無い。また `.git/info/exclude` は clone に
乗らないため、このリポを clone するリハーサル・対象PJ で同じ混入が再発する。`.gitignore` が唯一、
配布先まで持って行ける置き場である。

## converter 側では扱わない件

- `rows: []`（データ行 0 件）のとき期待値検証が素通りする（偽陰性）問題は **yaml 側**の課題である。`nablarch-testing-yaml` の `YamlTableDataBuilder` に FIXME として記されている。converter 側で直さない・触らない。

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

**辺② 軸D は 12 ケース（ユーザー確定・2026-08-14）。** 別途渡された「YAML スカラー 10 ケース」表は
根拠にしない。`ntf-testdata-yaml-schema.json` が `rows` の値を `["string","null"]` に強制しているため、
スキーマを通る YAML で現れうるスカラーだけが対象になる。導出とその再検証は `checks/task-24.md`。

| タグ | ケース | 対象記法 |
|---|---|---|
| D2-01 | 引用符なし文字列 | `abc` |
| D2-02 | 引用符あり文字列（二重・一重） | `"abc"` / `'abc'` |
| D2-03 | 引用符付き数値 | `"123"` |
| D2-04 | 引用符付き末尾ゼロ小数 | `"1.50"` |
| D2-05 | 真偽値に見える文字列 | `"true"` / `TRUE` / `yes` |
| D2-06 | NULL | `null`（引用符なし）／値なし |
| D2-07 | NULL に見える文字列 | `"null"` / `~` / `NULL` |
| D2-08 | 日付・日時風文字列 | `2026-08-07` / `2026-08-07T12:34:56` |
| D2-09 | 複数行 | `\|` / `>` |
| D2-10 | 先頭ゼロ・非 JSON 数値記法 | `007` / `0x1F` |
| D2-11 | 空文字・前後空白 | `""` / `"  pad  "` |
| D2-12 | 特殊文字を含む文字列 | `"a: b"` / `"a #b"` |

**D2-11・D2-12 を足した理由**: 辺① 軸D は空文字・前後空白を、辺④ 軸D は `""` とコロン・ハイフン・`#`
含みを持つ。#25 は「辺④で書いた各ケースを辺②で読み戻して復元されるか」を判定する手順であり、
辺②側に対応ケースが無いとその突き合わせが片側だけになる。

**スキーマを通らない記法はテストで固定しない。** 引用符なしの `true` / `123` / `1.50` / `.inf` / `.nan` は
`YamlSchemaValidationException` で中間モデルへ到達しない＝ NTF が実行できないテストデータであり、
上の但し書きにより担保対象でも記録対象でもない。**例外が出ることをテストで固定せず**、
`issues.md` の「対象としない入力」に YAML 側の段落として（Excel 側の同節と対になる形で）記す。

**忠実性はテストではなくコード構造で保証されている。** `XlsFormatReader` は自前の POI 解析を持たず
`PoiXlsReader` をそのまま注入して使う（`XlsFormatReader.java:81`）。「実セル → 文字列」の区間が
NTF 本体と同一コード 1 本であるため、変換の前後で値は変わらない。

## Fake 経路の担保をどう数えるか（#21 のレビューで確定・2026-08-12）

**辺①では、`FakeTestDataReader` 経路の担保を実 `.xlsx` 経路の担保として数えない。**

- #18 の棚卸しは Fake 経路のテストも ✅ に数えていた。#20 の Purpose が「既存33件は Fake 経路のため流用せず実ファイル経由で組み直す」としたことで基準が変わり、#21 で E-3(複数) と F1-05（カラム名重複）の 2 要素が「#18 は ✅ だが実 `.xlsx` 経路では空欄」と判明して追加した。
- **#22 以降（辺②③④）へ機械的に広げない。** 辺①でこの基準を採ったのは、`PoiXlsReader` の「実セル → 文字列行」区間が Fake 経路では一度も動かず、そこに軸D（セル種別）の挙動が集中しているためである。追加するかは「Fake 経路と実ファイル経路で結果が分岐しうるか」で判断し、分岐しないなら重複テストとして書かない（Rules の「重複テストを書かない」が優先）。判断の根拠は対応表に書く。

## 不具合修正の対象と手順（#25.5）（ユーザー確定・2026-08-14）

**方針を変えた。** #19〜#25 は「見つけた不具合は直さず記録する」で通してきたが、**要対応と判定した
6 件に限り修正する**。それ以外は従来どおり記録のみである。Rules の該当 2 行と Acceptance criteria の
2 行はこの決定に合わせて改訂済み。

**当初は 5 件だった。XLS-22 を 2026-08-18 に追加した（ユーザー確定）。** 判定の根拠を「到達可能性」から
**「両形式が表現できない値を中間モデルだけが保持できる＝中間モデルの契約の穴」**へ一本化したことによる
（`issues.md` **XLS-22**）。**中間モデルの契約は 4 辺すべてが表現できる範囲で定める** —— これが判断の枠組みであり、
「到達不能だから対応不要」はリスクの判断であって NTF 仕様としての判断ではない。
同じ中間モデル値の辺④版である **YML-12 3形目**も同時に対象へ入った。

**出典の版**: 以下で `notation:nnn` と書くのは
`~/work/nablarch/nablarch-document/ja/development_tools/testing_framework/implementation/testdata_notation.rst`
の行番号（`nablarch-document` の `df7bff7` 時点）。**引用は全件、実物を開いて確認した。**

### 修正する 6 件

| ID | 辺 | 現在の挙動 | 仕様どおりの期待値 | 出典（確認済み） |
|---|---|---|---|---|
| YML-02 | ② | `group_id` を省略した送信同期エントリをブロックごと落とす | 省略時はデフォルトグループのブロックとして読む | `notation:254`「グループIDを省略した場合は、グループIDを持たないデータブロック（デフォルトグループ）が対象になる」 |
| YML-12 | ④ | レコードが空のファイルブロックで `records:` キーごと落とす | `records: []` を出力する | `notation:879`「0バイトの空ファイルは、レコード定義を持たないファイルデータブロックとして表現する」／`notation:1144`「0バイトの空ファイルを表現するには、`records:` に空配列 `[]` を記載する」 |
| XLS-16 | ③ | シート名を 31 文字へ黙って切り詰める | 黙って切り詰めない。31 文字超は例外で落とす | `notation:588`（下記の訂正を参照） |
| XLS-06 | ① | レコード種別の空セルを `""` にする | `null` を入れる（辺②と同じ） | `RecordLayout.java:26`「レコード種別（省略時は `null`）」 |
| XLS-22 | ③④ | `fields` が空の `RecordLayout` を、Excel は読み戻せない版面として・YAML は `fields: []` として書き出してしまう | 書き出し側が `IllegalArgumentException` で落とす（`RecordLayout` の Javadoc に「`fields` は 1 件以上」の契約を明記する） | `notation:886`「フィールド名称リストまたはデータ型リストが未指定または空である」を記述時のエラーに挙げる（＝**その形は Excel 記法として存在しない**）／YAML 本体スキーマ `nablarch/test/ntf-testdata-yaml-schema.json` の `$defs.record_fragment` が `fields` を必須かつ `minItems` ＝ 1 とする |
| YML-08 | ② | ディレクティブ値の実制御文字を素通しする | 辺①（`XlsFormatReader#normalizeDirectiveValue`）と同じ逆正規化を通す。対象は `record-separator` ／ `field-separator` | `notation:945`（`record-separator` は シンボル または任意のリテラル文字列）／`notation:1078`（`field-separator=\t`）／`notation:1114`（`record-separator CRLF`）。いずれもシンボルとエスケープ 2 文字の記法しか示していない |

**XLS-16 の出典を訂正した。** 当初示された `notation:68`（「シート名をテストメソッド名と同名にする」）は
実際には `notation:69` であり、**その直後 `notation:73` の tip が「シート名とテストメソッド名の対応は
『制約』ではなく『推奨』であり、両者が異なっていても正しく動作する」と明記している**。
したがって「テストメソッド名と揃える推奨」から切り詰めの不当性は導けない。**根拠は `notation:588` に置く**
——「読み込み単位の名前（Excel 形式ではシート名、YAML 形式ではファイル名）と ID を指定して
List 形式または Map 形式でデータを取得できる」。続く `TestSupport#getListMap(String sheetName, String id)`
のとおり**シート名は呼び出し側が渡す引き当てキー**であり、黙って別名に変えれば呼び出し側から引けなくなる。
結論（黙って切り詰めない）は変わらない。

**XLS-16 の切り詰めは converter のコードではない。** POI 3.8 の `XSSFWorkbook#createSheet(String)` が
`substring(0, 31)` してから `WorkbookUtil.validateSheetName` に掛ける（`issues.md` XLS-16 に逆アセンブルの
実測あり）。`XlsFormatWriter#build` が長さを検査せず渡すことが converter 側の欠陥であり、
**検査を `createSheet` の前に置けば、切り詰めが禁止文字検査を無効化する抜け（禁止文字が index 31 以降）も
同時に閉じる**。

**YML-12 で直すのは 4 形のうち 1 つ目と 3 つ目。** `issues.md` YML-12 の「あるべき姿」は 4 形に分かれており、
`records: []` で通るのは `file_data`（`records.minItems` ＝ 0）だけである。残る 3 形
（`message_data.records` 空／`record_fragment.fields` 空／`field_def.type` 省略）はスキーマが形そのものを
認めておらず、`[]` を書いても読み戻せない。

**当初はこの 3 形をすべて対象外としていたが、3 形目（`record_fragment.fields` 空）を対象へ移した**
（ユーザー確定・2026-08-18）。辺③の XLS-22 と同じ中間モデル値であり、同じ「契約の穴」だからである。
**残る 2 形目（`message_data.records` 空）と 4 形目（`field_def.type` 省略）は判断待ちであり、
着手していない**（2026-08-18 時点。判断が出るまで記録のみのまま）。

### 修正しない 2 件（判定の根拠つき）

- **YML-03** — 原因は nablarch-testing-yaml の `YamlFileBuilder#skipFwHeader` にあり、converter 側だけ直すと
  `IllegalStateException` になる。**仕様どおりの期待値を書いた `@Ignore("YML-03: yaml側の修正待ち")` の
  テストを置いて待つ。**
- **XLS-01** — **対応不要。** `notation:75`「Excelのセルの書式は、必ず文字列書式に統一しておく必要がある」と
  直後の `important`「Excelファイルに文字列以外の書式でデータを記述すると、Excelがセルの値を自動的に
  変換してしまう（…）ため、正しくデータを読み取れなくなる」により、数値書式セルは**仕様外入力**であり、
  解説書自身が壊れると明言している。**テストは削除せず、アサートを「仕様外入力のため値は保証しない」旨へ
  書き直して実挙動の記録として残す。**

### 手順（1 件 ＝ 1 コミット）

1. 仕様どおりの期待値でテストを書く
2. **赤になることを実行して確認し、失敗メッセージを記録する（ここを飛ばさない）**
3. 実装を直して緑にする
4. **現状挙動を固定していた既存テストは削除する。** 2 本残すとどちらが正か分からなくなり、
   いま消そうとしている問題（緑のアサートが不具合を正常に見せる）を作り直すことになる

### 着手前に 1 コミット — 緑の嘘を消す

不具合の挙動を緑のアサートで固定しているテストに `@Ignore("課題ID: 一行説明")` を付ける。
着手時点で `@Ignore` ／ `@Disabled` はリポジトリ全体で **0 件**であり（`grep -rl "@Ignore\|@Disabled" src/test/java | wc -l` → 0）、
実行結果からは不具合と正常が区別できない状態だった。

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

### #24: 辺② 軸D（YAML スカラー12ケース）・軸A〜F の欠け補充

**Purpose**: YAML のスカラー型が中間モデルへどう入るかを固定し、#18 の棚卸しで空欄となった辺②の軸要素を埋める。

**Prerequisites**: #18

**Steps**:

- [x] 軸D の12ケース（D2-01〜D2-12。定義は Decisions「軸D の対象範囲」）を実 YAML フィクスチャで読み、現状の挙動をまず記録してから固定する
- [x] 軸F: スキーマ違反／YAML として不正／未知のキー／必須構造の欠落／空ファイルのテストを追加する。**スキーマ違反のケースは、Decisions で仕様外とした引用符なしスカラー記法（`true` / `123` / `1.50` / `.inf` / `.nan`）を入力に使わない**（それらの例外はテストで固定しない、が確定事項のため）
- [x] #18 の棚卸し表で辺②の空欄となっている軸A・B・C・E の要素を埋める
- [x] 仕様として不適切と判断した挙動を `issues.md` に記録する（**修正しない**）。本タスクで記録が確定しているのは次の 2 点:
  - **YML-01（新規・ID は `XLS-nn` を使わない）**: 引用符なしの `null` と値なしは Java `null` になるが、`~` / `NULL` / `Null` はいずれも文字列になる。3 者ともスキーマを通る仕様内の入力であり、作成者が NULL のつもりで書いた値が黙って文字列としてテストデータに入る。帰属は converter ではなく yaml 側（loader のスカラー解決）と明記し、出典として解決を行っているクラス・メソッドの位置とスキーマの該当パスの両方を書く。影響度と検出可否は既存フォーマットに合わせる
  - **「対象としない入力」の YAML 側段落**: 引用符なしの `true` / `123` / `1.50` / `.inf` / `.nan` がスキーマ違反で対象外であることを、Excel 側の同節と対になる形で書く（スキーマの出典パスを添える）
- [x] **記録しないと確定しているもの**: 引用符なしの `true` がスキーマ違反で例外になること（仕様外の入力。例外で止まるので黙って壊れない）と、`TRUE` / `yes` が文字列になること（スキーマが値を `["string","null"]` に限る以上そのとおりの挙動で、真偽値を表現する手段自体が無い）
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` で全 PASS を確認する
- [x] **台帳の記述規約の自己点検**（Rules 参照）: `grep -cE '\b(L[0-9]{1,4})\b' .rn/ntf-test-data-converter/coverage/inventory.md` → **0** であること。ファイル行数を書いていないこと。本タスクで書き足した件数のすべてに、それを導いたコマンドを併記していること。逆引き表（軸要素 → 担保テストメソッド）を新設していないこと
- [x] self-check（OK/NG per completion criterion、checks/task-24.md に記録）
- [x] QA expert review（subagent） — 3 巡実施。指摘は全件反映済み（`checks/task-24.md`）
- [x] Craft expert review（subagent, coding） — 3 巡実施。指摘は全件反映済み
- [x] Verification expert review（subagent, test） — 3 巡実施。生存変異 計 9 件をすべて閉じ、閉じたあと同じ変異で検知を確認

**Completion criteria**:

- 軸D の12ケース（D2-01〜D2-12）すべてがアサートされ、特に `null` ／値なし ／ `~` の3者の分かれ方と、`"null"` ／ `NULL` の扱いが結果として固定されている
- 軸F の5ケース（スキーマ違反／不正 YAML／未知キー／必須構造欠落／空ファイル）で例外型または結果がアサートされている。スキーマ違反のケースの入力に、仕様外とした引用符なしスカラー記法（`true` / `123` / `1.50` / `.inf` / `.nan`）を使っていない
- `issues.md` に YML-01 と「対象としない入力」の YAML 側段落が記録されている（いずれも `src/main` 無変更）
- 辺②について軸A の13種（`DEFAULT` を除く。`YamlFormatReader#addBlocksForSection` L106-133 が既知セクションキーのみを分岐に持ち `DEFAULT` を生成しないため到達不能。根拠付きで空欄に残す）・軸B の4種・軸C の全フィールド（省略可能なものは省略時も。`sections` は `YamlFormatReader#read` L94 が `Collections.singletonList` を返すため「空」「複数」とも到達不能として根拠付きで空欄）・軸E が埋まっている
- src/main への変更がゼロ
- `mvn clean test -Djacoco.skip=true` が全テスト PASS する

---

### #25: 辺④ 軸D（YAML 表現9ケース）・軸A〜F の欠け補充

**Purpose**: 中間モデルの値が YAML へどう書かれるかを固定し、辺②と対で往復可能性（引用符が落ちて再読込で型が変わらないか）を確認する。#18 の棚卸しで空欄となった辺④の軸要素も埋める。

**Prerequisites**: #24

**Steps**:

- [x] 軸D の9ケース（`"100"` ／ `"true"` ／ `"null"` ／ `null` ／ `""` ／ `"007"` ／改行含む／`"2026-08-07"` ／コロン・ハイフン・`#` 含む。タグ D4-01〜D4-09 は `coverage/inventory.md` §「辺④ YAML 表現 9 ケース」で同じ並びに対応）を書き出し、出力 YAML の記法をアサートするテストを追加する（現状の挙動をまず記録してから固定する）
- [x] 各ケースについて、#24 で固定した辺②の読み取り挙動と突き合わせ、文字列が同じ文字列として復元されるか否かを判定し記録する — 9 ケースとも復元される
- [x] 軸F: 出力先不在／書き込み権限なしのテストを追加する。`overwrite=false` 衝突は `YamlFormatWriter` が `overwrite` を保持しないため辺④の対象外とし、上位層で担保済みである根拠を対応表に記録する（#22 と同じ扱い）
- [x] #18 の棚卸し表で辺④の空欄となっている軸A・B・C・E の要素を埋める — 軸A の A-12〜A-14 が #18 以来 ✅ と誤判定されていた（実際は 🔺）ため訂正して埋め直した
- [x] 復元できない組み合わせがあれば `issues.md` に課題として記録する（**修正しない**） — YML-12（スキーマが禁じる形の中間モデルから読み戻せない YAML が黙って書かれる）と YML-13（折り返しの起きるキーは YAML として読めなくなる）
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` で全 PASS を確認する — `Tests run: 536, Failures: 0, Errors: 0, Skipped: 0`
- [x] **台帳の記述規約の自己点検**（Rules 参照）: `grep -cE '\b(L[0-9]{1,4})\b' .rn/ntf-test-data-converter/coverage/inventory.md` → **0**。ファイル行数は書いていない。書き足した件数のすべてに導出コマンドを併記。逆引き表は新設していない
- [x] self-check（OK/NG per completion criterion、checks/task-25.md に記録）
- [x] QA expert review（subagent） — 3 巡実施。指摘は全件反映済み（`checks/task-25.md`）
- [x] Craft expert review（subagent, coding） — 3 巡実施。指摘は全件反映済み
- [x] Verification expert review（subagent, test） — 3 巡実施。生存変異 計 4 件をすべて閉じ、計 71 件の変異で再確認（残る生存 1 件は `YamlSeq#header` の等価変異として台帳に開示）

**Completion criteria**:

- 軸D の9ケースすべてで出力 YAML の記法（引用符の有無・複数行記法・NULL 表現）がアサートされている
- 9ケースそれぞれについて、辺④で書き辺②で読んだとき元の文字列が復元されるか否かが判定・記録されている（復元されない場合は課題として記録され、修正されていない）
- 軸F の2ケース（出力先不在／書き込み権限なし）で例外型または結果がアサートされている。`overwrite=false` 衝突は上位層で担保済みとして根拠付きで対象外にされている
- 辺④について軸A の14種・軸B の4種・軸C の全フィールド（省略可能なものは省略時も）・軸E が埋まっている
- src/main への変更がゼロ
- `mvn clean test -Djacoco.skip=true` が全テスト PASS する

---

### #25.5: 不具合修正（TDD）

**Purpose**: 要対応と判定した 6 件を TDD で修正し、不具合の挙動を緑のアサートで固定している状態を解消する。判定と出典は Decisions「不具合修正の対象と手順（#25.5）」。

**Prerequisites**: #25

**Steps**:

- [x] **（先に 1 コミット）緑の嘘を消す。** 不具合の挙動を緑のアサートで固定しているテストを列挙し、`@Ignore("課題ID: 一行説明")` を付ける。列挙は `issues.md` の各課題の「担保テスト」欄から機械的に導き、**どのテストを対象にしたか・なぜそれが不具合の固定に当たるかを一覧で残す**
- [x] YML-02（辺②）: `group_id` 省略時はデフォルトグループのブロックとして読む
- [x] YML-12（辺④）: レコードが空のファイルブロックで `records: []` を出力する（4 形のうち 1 つ目のみ。残り 3 形は記録のまま）
- [x] **XLS-22（辺③④）／YML-12 3形目**: `fields` が空の `RecordLayout` を書き出し側が `IllegalArgumentException` で落とす。`RecordLayout` の Javadoc に「`fields` は 1 件以上」の契約を明記する（追加・ユーザー確定 2026-08-18）
- [x] XLS-16（辺③）: シート名を黙って切り詰めない。31 文字超は `XlsFormatWriter#build` が `createSheet` の前に検査して例外で落とす
- [x] XLS-06（辺①）: レコード種別の空セルに `null` を入れる（辺②と同じ）
- [x] YML-08（辺②）: `record-separator` ／ `field-separator` を辺①と同じ逆正規化に通す
- [x] YML-03: 仕様どおりの期待値を書いた `@Ignore("YML-03: yaml側の修正待ち")` のテストを置く（修正はしない）
- [x] XLS-01: テストを削除せず、アサートを「仕様外入力のため値は保証しない」旨へ書き直して実挙動の記録として残す
- [x] `issues.md` の全 36 件に「NTF 仕様としての判定」欄を追加し、要対応 7 件／対応不要 29 件を出典つきで明記する（XLS-22 の判定変更を反映した 2026-08-18 時点の実数。導出コマンドは `issues.md` 冒頭）。**既存の「判断」欄は往復基準（変換結果が入力と一致するか）で書かれており、辺ごとの判定とは別物なので、両方を残して区別する**
- [x] YML-08 の「未確認」を潰す: 中間モデルに入った実改行を辺④が書き出したとき、読み戻しで空文字になるかを**実行して**確かめ、結果を `issues.md` に記録する
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` で全 PASS を確認する
- [x] `inventory.md` のテスト件数を、増減した箇所すべてコマンドから導き直す（Rules）
- [x] self-check（OK/NG per completion criterion、checks/task-25.5.md に記録）
- [x] QA expert review（subagent）
- [x] Craft expert review（subagent, coding）
- [x] Verification expert review（subagent, test）

**Completion criteria**:

- 6 件それぞれについて、仕様どおりの期待値のテストが存在し、**修正前に赤になったこと（失敗メッセージ）が記録されている**
- 6 件それぞれについて、現状挙動を固定していた既存テストが削除されている（同じ挙動を主張するテストが 2 本残っていない）
- 1 件 ＝ 1 コミットになっている（`@Ignore` 付与のコミットを含め、混ぜていない）
- YML-03 の `@Ignore` テストが存在し、理由が `@Ignore` の引数に書かれている
- XLS-01 のテストが削除されておらず、アサートが「仕様外入力のため値は保証しない」旨へ書き直されている
- `issues.md` の全 36 件に「NTF 仕様としての判定」欄があり、要対応 7 件／対応不要 29 件が出典つきで書かれている。既存の「判断」欄が残っており、両者の違いが説明されている
- YML-08 の「未確認」が実行結果で埋まっている
- `mvn clean test -Djacoco.skip=true` が全テスト PASS する（`@Ignore` は Skipped として現れてよい）
- `src/main` の変更が 6 件の修正に必要な範囲に限られており、変更したファイル名・課題ID・変更理由が記録されている

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
- [ ] `issues.md` を通読し、#19〜#26 で記録した課題が漏れなく載っていること・**#25.5 で修正した 5 件を除き**修正されていないことを確認する
- [ ] self-check（OK/NG per completion criterion、checks/task-27.md に記録）
- [ ] QA expert review（subagent）
- [ ] Craft expert review（subagent, writing）
- [ ] Verification expert review（subagent, fact-check）

**Completion criteria**:

- `axis-matrix.md` に辺①〜辺④の4表があり、軸A〜F の全要素が行として存在する
- 各要素に担保テストメソッド名が記されており、記された全メソッド名がテストソースに実在する
- 空欄の要素には理由が書かれている
- `issues.md` に本作業で見つかった課題が一覧化されており、`git diff` 上 src/main への変更が **#25.5 で修正した 5 件に限られている**ことが確認されている

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

(written by /rn:dn, read and reset to this placeholder by /rn:up. `Status` is `paused` while a
session is suspended — the signal /rn:up and /rn:dn search for — and resets to `not suspended` here,
so only a genuinely suspended session reads `paused`.)

- **Status**: not suspended
- **Date**: YYYY-MM-DD
- **Last completed**: #N description
- **Next**: #N description
- **Notes**: bounded forward pointer — branch/PR, next concrete action, open blockers, user-deferred paths, open questions / pending decisions not yet captured in `design.md`; not a re-narration of the session (that lives in `git log`)
