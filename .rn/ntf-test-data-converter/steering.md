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

# Decisions

<!-- 必要に応じて記入 -->

# State

(written by /rn:bb, read and reset to this placeholder by /rn:hi)

- **Status**: paused
- **Date**: 2026-06-23
- **Last completed**: カバレッジ計測（mvn jacoco offline instrumentation → 277テスト全PASS、C0 96.6% / C1 91.3%）と未カバー箇所のNTF仕様観点分類
- **Next**: 分類1のコード削除 → 分類2のテスト追加 → 分類3のコメント追加（各1タスク）
- **Notes**: |

    ## カバレッジ計測手順（再現用）

    ```
    JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 \
      mvn clean jacoco:instrument test jacoco:restore-instrumented-classes jacoco:report \
      -Djacoco.dataFile=/home/tie303177/work/nablarch/nablarch-testing-converter/jacoco.exec
    ```
    - jacoco.exec はプロジェクトルート（target/ ではない）
    - レポート: target/site/jacoco/jacoco.csv

    ## 未カバー箇所の分類

    ### 分類1: NTF仕様外のコード → 削除

    | 箇所 | 理由 |
    |---|---|
    | `XlsFormatReader#stripQuotes` L455: `if (value == null) return null;` | 全呼び出し元（L150, L182, L332, L408）がnull非通過を保証。到達不能デッドコード |
    | `YamlFormatWriter#emitBlock` L141: `throw new IllegalArgumentException("unsupported block")` | `TestDataBlock` は sealed（permits ColumnRowDataBlock/FileDataBlock/MessageDataBlock）、全具象サブクラスを網羅済みのため到達不能 |

    ### 分類2: NTF仕様内 → テスト追加

    | 箇所 | 理由 |
    |---|---|
    | `XlsFormatReader#normalizeDirectiveValue` L394/L408/L422: record-separator の CRLF/LF/CR 各シンボル変換 | DR-09/DR-10 仕様（record-separator のシンボル ⇔ 実改行文字変換）。仕様上正規の変換パスだがテスト未整備 |
    | `XlsFormatReader#readMessageBlock` L224: `message == null → return null` | MESSAGE ブロック不在（Excel に対象 ID が存在しない）の正常系。仕様内だが未テスト |

    ### 分類3: Java言語仕様上必要な実装 → コメント追加

    | 箇所 | 理由 |
    |---|---|
    | `TestDataConverter` L34-35 / `ConverterPathResolver` L25-26 | ユーティリティクラスのプライベートコンストラクタ（`AssertionError`）。Java のインスタンス化防止イディオム |
    | `ConverterFileFilter` L38-39 / L71-72 / L96-97 | `Files.walk` の `IOException` → `UncheckedIOException` ラップ。Java 検査例外の処理義務 |
    | `YamlTestDataValidator#loadSchema` L250-255 | `getResourceAsStream` の `null` ガード・`IOException` catch。クラスパスリソースロードの Java イディオム |
    | `YamlTestDataValidator` L141-142 | V-SCH スキーマ検証中の `RuntimeException` catch。networknt ライブラリの予期せぬ例外を検証エラーへ変換する防御コード |
    | `XlsFormatReader#toRecordLayouts` L307 / `requireLine` L354 | 器↔生行の内部一貫性ガード（`IllegalStateException`）。2系統の読み込み経路が一致することを表明する番人コード |
    | `StubDbInfo` 未カバーメソッド群 (L42-80) | `DbInfo` インターフェース実装義務。DB 書き込み経路専用で読み込み経路から呼ばれない（クラス Javadoc に番人コードとして説明済み） |
    | `TestCoreReaderAdapter` `HeaderCollector` L433-447 / `BodyLineCollector` L525-539 | `TestDataParsingTemplate` の抽象メソッド実装義務（`onReadLine`, `onTargetTypeFound`, `isTargetType`, `shouldStopOnNextOne`）。両クラスは `parse(String id)` をオーバーライドするため基底の `doParse()` から呼ばれない |
