# Goal

nablarch-testing（ブランチ `convert-testdata-excel-to-text`）の変換ツール（src/main 28件）と形式間変換テスト（src/test 21件）を nablarch-testing-converter リポジトリへ切り出し、`mvn test` 全 PASS・実装無改変を確認する。

# Acceptance criteria

- converter の `mvn test` が全テスト PASS する
- 全移動ファイルが本体現ブランチと package/import を除いて完全一致（実装無改変）
- pom.xml が yaml・本体・poi 3.8・snakeyaml 3.0.1 の依存を正しく持つ
- 本体（nablarch-testing）・yaml（nablarch-testing-yaml）に一切書き込んでいない
- ブランチが push 済み

# Assumptions

- 全リポジトリは同じ親ディレクトリに clone 済み
- nablarch-testing-yaml は構築・公開済み（converter のビルドに必要）
- 本体は `convert-testdata-excel-to-text` ブランチのまま（移動元）
- テストデータは静的同梱ではなく変換テストが実行時に一時生成・参照する方式（.xls/.yaml の固定ファイルは不要）
- YamlModeTestBase や *YamlTest・結合テストは integration 行きであり converter 対象外

# Rules

- 1 task = 1 commit
- 実装の変更は一切しない。package/import の機械的調整と pom 設定のみ許可
- 本体・yaml には書き込まない
- `mvn test` が通らず実装変更が要ると判断したら止めてユーザーに確認する
- タスク完了後は即 push し、PR を作成してユーザーがコードを PR 上で確認できるようにする

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

# Decisions

<!-- 必要に応じて記入 -->

# State

(written by /rn:bb, read and reset to this placeholder by /rn:hi)
