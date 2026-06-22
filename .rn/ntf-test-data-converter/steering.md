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

# Tasks

### #1: pom.xml の作成

**Purpose**: converter リポジトリのビルド基盤を整える。

**Prerequisites**: none

**Steps**:

- [ ] 本体 pom.xml を参照し groupId・親 POM を確認する
- [ ] nablarch-testing-yaml の groupId・artifactId・version を確認する
- [ ] yaml・本体・poi 3.8・snakeyaml 3.0.1 を依存に持つ pom.xml を作成する
- [ ] self-check（OK/NG per completion criterion、checks/task-1.md に記録）
- [ ] QA expert review（subagent）
- [ ] language expert review（subagent）
- [ ] software-engineering expert review（subagent）
- [ ] user review

**Completion criteria**:

- pom.xml が存在し、nablarch-testing-yaml・nablarch-testing（compile）・poi-ooxml:3.8・snakeyaml-engine:3.0.1 が依存として記載されている
- groupId・親 POM が本体に合わせて設定されている

---

### #2: src/main 移動（変換ツール 28件）

**Purpose**: 本体現ブランチの `src/main/java/nablarch/test/tool/converter/` 配下 28件を同一パッケージパスへコピー配置する。

**Prerequisites**: #1

**Steps**:

- [ ] 本体現ブランチから対象 28件を同一パッケージパスへコピーする
- [ ] package/import を converter リポジトリ向けに機械的調整する（ロジック変更なし）
- [ ] self-check（OK/NG per completion criterion、checks/task-2.md に記録）
- [ ] QA expert review（subagent）
- [ ] language expert review（subagent）
- [ ] software-engineering expert review（subagent）
- [ ] user review

**Completion criteria**:

- `src/main/java/nablarch/test/tool/converter/` 配下に 28件が存在する（直下・model/・xls/・yaml/ サブパッケージ含む）
- 各ファイルが本体現ブランチの対応ファイルと package/import を除いて完全一致する

---

### #3: src/test 移動（形式間変換テスト 21件）

**Purpose**: 本体現ブランチの `src/test/java/nablarch/test/tool/converter/` 配下 21件を対応パスへコピー配置する。

**Prerequisites**: #2

**Steps**:

- [ ] 本体現ブランチから対象 21件を対応パスへコピーする
- [ ] package/import を converter リポジトリ向けに機械的調整する（ロジック変更なし）
- [ ] self-check（OK/NG per completion criterion、checks/task-3.md に記録）
- [ ] QA expert review（subagent）
- [ ] language expert review（subagent）
- [ ] software-engineering expert review（subagent）
- [ ] user review

**Completion criteria**:

- `src/test/java/nablarch/test/tool/converter/` 配下に 21件が存在する
- 各ファイルが本体現ブランチの対応ファイルと package/import を除いて完全一致する

---

### #5: Adapter 群追加（4件＋テスト3件＋データ）

**Purpose**: 本体現ブランチ `convert-testdata-excel-to-text` の Adapter 群をコンバーターリポジトリへ受け入れる。パッケージプライベートアクセスのため、本体と同一パッケージ（`nablarch.test.core.reader` / `nablarch.test.core.file`）に配置する。

**Prerequisites**: #4（mvn test 全 PASS 後）

**Steps**:

- [ ] 本体現ブランチから src/main 4件（TestCoreFileAdapter, YamlTestCoreAdapter, TestCoreReaderAdapter, StubDbInfo）をコピー配置
- [ ] 本体現ブランチから src/test 3件＋データディレクトリをコピー配置
- [ ] `mvn test` で全 PASS を確認（失敗は配置・依存で解決。実装変更不可）
- [ ] 全追加ファイルを本体現ブランチと diff し package/import 以外の差分ゼロを確認
- [ ] self-check（OK/NG per completion criterion、checks/task-5.md に記録）
- [ ] QA expert review（subagent）
- [ ] language expert review（subagent）
- [ ] software-engineering expert review（subagent）
- [ ] user review

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

- [ ] `mvn test` を実行し結果を確認する
- [ ] 失敗があれば pom 依存・リソース配置で解決する（コード変更不可）
- [ ] 全移動ファイルを本体現ブランチと 1件ずつ diff し package/import 以外の差分がないことを確認する
- [ ] self-check（OK/NG per completion criterion、checks/task-4.md に記録）
- [ ] QA expert review（subagent）
- [ ] user review

**Completion criteria**:

- `mvn test` が全テスト PASS する
- 全移動ファイルが本体現ブランチと package/import を除いて完全一致する（diff 結果がゼロ）
- 本体・yaml リポジトリに変更が加えられていない

# Decisions

<!-- 必要に応じて記入 -->

# State

(written by /rn:bb, read and reset to this placeholder by /rn:hi)

- **Status**: paused
- **Date**: 2026-06-22
- **Last completed**: なし（全タスク未着手）
- **Next**: #1 pom.xml の作成
- **Notes**: |
    追加指示あり：Adapter 群 4件（TestCoreFileAdapter, YamlTestCoreAdapter, TestCoreReaderAdapter, StubDbInfo）とテスト 3件＋データを steering.md にタスク #5 として追加済み。
    タスク #5 は本体と同一パッケージ（nablarch.test.core.reader / nablarch.test.core.file）への配置が必須。
    コンバーターリポジトリは空（src/ なし、pom.xml なし）のため #1 から開始。
    本体ブランチは convert-testdata-excel-to-text、yaml は mvn install 済みと仮定。
    /rn:hi 再開時はタスク #1（pom.xml 作成）から task-workflow.md に従い実行すること。
