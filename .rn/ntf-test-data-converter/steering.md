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
- 本作業で見つかった現状挙動の課題が課題一覧へ記録されている。**#25.5 で要対応と判定したものは修正され、それ以外は修正されずに記録のみである**（ユーザー確定・2026-08-14 の方針変更に合わせて改訂。改訂前は「修正されずに」が全件に掛かっていた。**件数は 5 → 6 → 7 → 15 と動いたため、件数ではなく `issues.md` の判定欄を正とする。2026-08-18 時点で要対応 15 件**）
- `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` が全テスト PASS する

# Assumptions

- 全リポジトリは同じ親ディレクトリに clone 済み
- nablarch-testing-yaml は構築・公開済み（converter のビルドに必要）
- 本体は `convert-testdata-excel-to-text` ブランチのまま（移動元）
- テストデータは静的同梱ではなく変換テストが実行時に一時生成・参照する方式（.xls/.yaml の固定ファイルは不要）。**例外**（ユーザー承認済み）: POI 生成物と Excel 保存物の同一性を確認するため、実物 `.xlsx` 1本のみ参照フィクスチャとして同梱してよい
- YamlModeTestBase や *YamlTest・結合テストは integration 行きであり converter 対象外

# Rules

- 1 task = 1 commit
- （フェーズ1）実装の変更は一切しない。package/import の機械的調整と pom 設定のみ許可。**フェーズ2 の #25.5 だけがこの例外で、`issues.md` の「NTF 仕様としての判定」が要対応であるものの修正に限り src/main を変更する**（ユーザー確定・2026-08-14。範囲の正は `issues.md` の判定欄であり、この行は参照に留める。**2026-08-18 時点では 15 件**）
- 本体・yaml には書き込まない
- `mvn test` が通らず実装変更が要ると判断したら止めてユーザーに確認する
- タスク完了後は即 push し、PR を作成してユーザーがコードを PR 上で確認できるようにする

（フェーズ2）

- **未知の挙動を調べる段階では期待値を先に決めない。** まず現状の挙動を実行して記録し、それが仕様として妥当かを判断してから固定する。**不具合と判定済みのものは、仕様どおりの期待値を先に書く（TDD）**（ユーザー確定・2026-08-14）
- **本作業で見つかった不具合は、`issues.md` の「NTF 仕様としての判定」が要対応であるものに限り修正する。それ以外は従来どおり記録のみ**（**2026-08-18 時点で 15 件** ＝ XLS-06・XLS-08・XLS-16・XLS-22・XLS-27・XLS-28・XLS-29・XLS-30・XLS-31・XLS-32・XLS-33 ／ YML-02・YML-03・YML-08・YML-12。当初 5 件・ユーザー確定 2026-08-14 → **XLS-22 を追加して 6 件**・ユーザー確定 2026-08-18 → **YML-03 を追加して 7 件**・ユーザー指示 2026-08-18（帰属先の nablarch-testing-yaml が `0b53910` で直ったため）→ **`7200b0f` で XLS-27〜33 の 7 件が加わり XLS-08 が要対応へ移って 15 件** → **XLS-20 を追加して 16 件**・
2026-08-19（`73297e2`。旧判定の根拠が事実誤りだったため。`issues.md` XLS-20 の【判定の訂正】） → **#25.5 §6 の中間モデル一巡点検で XLS-34〜38・XLS-40・XLS-41・XLS-43 が加わり、2026-08-19 時点で 24 件**）。**上の列挙は時点の記録であって正ではない。正は `issues.md` の判定欄であり、実数は `issues.md` 冒頭の導出コマンドから導くこと**（列挙を範囲の定義と読むと、あとから要対応になった課題が範囲外に読めてしまう。同じ取り残しが `7200b0f` のときに実際に起きた）。記録先は `.rn/ntf-test-data-converter/coverage/issues.md`。修正対象の判定根拠と手順は Decisions「不具合修正の対象と手順（#25.5）」
- 各辺の担保を往復テスト（`RoundTripTest`）の追加で代替しない。ただし**既存**の往復テスト（`RoundTripTest` 30件、`XlsFormatWriterTest#roundTrips*` 8件、`YamlFormatWriterTest#roundTrip_*` 6件）が実ファイル経由で通している軸要素は、棚卸しに「🔺弱い担保」として必ず計上する（重複テストを書かないため）。正式担保としては数えず、直接テストの追加対象からは外さない
- 既存テストを軸で棚卸ししてから新規テストを足す。棚卸しなしの新規追加はしない
- 対応表・カバレッジを示さずに「網羅した」と報告しない
- **テストメソッドを増減させたら、`inventory.md` 内の該当する件数を「記憶している箇所を直す」のではなく、コマンドから導き直す。各件数にはそれを導いたコマンドを併記する**（#22 で確定・2026-08-13）。#22 ではラウンド3 で 3 テストを追加した際、`§3.1-2`・Javadoc・`issues.md` は更新されたのに `§3.3` の件数だけが取り残され、同一文書内で 16 と 18 が矛盾した。#23・#24・#25 はいずれもテストを追加して同じ `inventory.md` を更新するため、同じ取り残しが起きる
- **台帳に載せる出典コマンドは、そのまま実行して同じ結果が出ること。** 誤った結果を返すコマンドは件数の誤記と同じ扱いとする（#22 で確定・2026-08-13。`grep -rc` の `| grep -v ":0$"` 欠落、および自分自身がヒットして主張を反証する grep が実際に発生した）
- **担保の穴は、テストを足さない場合でも台帳に開示する。** 開示しないのは件数を誤るのと同じ性質の誤りとする（#22 で確定・2026-08-13）
- **台帳（`coverage/inventory.md`）に「他ファイルの行番号」「ファイル行数」「コマンドを併記しない件数」を書かない**（#23 後の構造見直しで確定・2026-08-13）。行番号とファイル行数は他ファイルを編集するたびに移動し、台帳を直すと台帳の別箇所が自己無効化する。識別はクラス名・メソッド名で行う。#22・#23 の計 5 ラウンドの FAIL はすべて台帳の記述精度であり、テストコードの欠陥ではなかった
- **同じ関係を 2 方向に手書きしない。** ひとつのテストメソッドを、台帳の中で「テストメソッド → 軸要素」と「軸要素 → 担保テストメソッド」の両方に書かない。**4 辺を通した逆引き（軸要素 → 担保テストメソッド）の正は #27 の `coverage/axis-matrix.md` とする**（#23 後の構造見直しで確定・2026-08-13。**文言は #24 の 2 巡目レビュー指摘を受けて 2026-08-14 に実態へ合わせた**）。既存クラスの棚卸しは §X.1 系（メソッド → 軸要素）、各タスクが新規追加したクラスの担保は §X.1-2 系（軸要素 → メソッド）に書く。この 2 つは対象クラスが重ならないため二重記載にならない。規約を入れた `3f9e665` の時点で §1.2-2・§3.1-2・§3.1-3 が既にこの形であり（`git show 3f9e665:.rn/ntf-test-data-converter/coverage/inventory.md | grep -c 担保テストメソッド` → 15）、当初の文言のほうが実態と食い違っていた
- **文書の揃え方**（ユーザー確定・2026-08-13）: 定義を変えたら `steering.md`／`coverage/inventory.md`／`coverage/issues.md` は指示に列挙が無くても現行定義へ揃えてよい。揃えないのは `checks/` だけ（時点の証拠記録であるため）
- **JaCoCo の再計測はしない**（ユーザー指示・2026-08-21）。#26 の計測は `da66425` 固定であり、全数値の出典は `coverage/coverage-report.md` §0 が記録した 1 回の実行（`jacoco.csv` md5 `d28e374e9027ade63d7919f7a7b5826e`）である。流し直すと行番号と数値が動き、`coverage-report.md` が引用する行番号がすべて自己無効化する
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
- **`install` は必ず `clean` を付ける（ユーザー指示・2026-08-18 に手順として定着させた）。**
  `clean` を省くと、直前の JaCoCo 計測が `target/classes` に残した計装済みクラスを拾って
  `Cannot process instrumented class` で落ちる。**#26（カバレッジ計測）では計測と install を
  交互に打つため、同じ失敗が繰り返し起きる。** `clean` を付けるのは遅いからではなく、
  前の計測の残骸を消すためである。

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

## 不正値は書き出し側でなく中間モデルの生成時に拒否する（ユーザー確定・2026-08-19）

**以降の同型案件すべてに適用する共通方針である。** §1-D（`FieldDef.name`）・§1-E（`TestDataBlock.groupId`）の
判断として示された。

1. **番人（書き出し側の例外チェック）は置かない。既にあるものは外す。** 出口で落とすのは、持ってはいけない値を
   中間モデルが持てる状態をそのままにする対処であり、暫定対応にあたる。
2. **あるべき姿は「中間モデルがその状態を持てないこと」。** `FieldDef` は `name` が `null` のインスタンスを
   作れない、`TestDataBlock` は `groupId` が `null` のインスタンスを作れない。**生成時点で拒否する。**
3. **この拒否は入力の検証ではなく不変条件の保証である。** NTF 仕様に合わない Excel／YAML が落ちるのは
   従来どおり正しい。それとは別に、中間モデルに `null` が入ることは呼び出し側のバグであり、生成時に露見させる。
4. **実装の前に、`null` の生成元を特定すること。** NTF 仕様に適合する入力から `null` が生まれるなら、真因は
   辺①②（リーダー）側にある。その場合はリーダーを直し、テストは辺①②の入力から書く。書き出し側で塞いで
   終わりにしない。
5. **真因が他モジュールにあるなら、あるべき姿のテストを書いて FAIL させ、`@Ignore` に理由と他責先を記載する。**
   プロダクションコードには一切残さない。

**この方針は `issues.md` XLS-22 の「`RecordLayout` のコンストラクタには番人を置かない（2026-08-18・ユーザー確定、
`ea52297`）」を反転する。** 当時の却下理由は「番人の役割は『どの形式にも写せない値をそこで止めること』であり、
それが起きる場所は辺③④（書き出し）である」というものであり、本方針の 1・2 が正面から否定する。
もう 1 つの却下理由（コンストラクタに置くと辺③④の番人テスト 4 件が空振りになる）は、番人テスト自体を
生成時拒否のテストへ置き換えることで解消する。

### §1-D・§1-E の実装内容（ユーザー確定・2026-08-19）

- **§1-D（`FieldDef.name`）**: `FieldDef` の生成時に `name` の `null` を拒否する。**空文字は通す**
  （本体スキーマ `$defs.field_def.name` に `minLength` が無いため）。辺③④に番人は置かない。
  **承認済みの `type` 番人（`f80c192`）も同じ形へ寄せる** —— `FieldDef` が `type` に `null` を持てないようにし、
  書き出し側のチェックは外す。
- **§1-E（`TestDataBlock.groupId`）**: 契約は既に「省略時は空文字」と明記されている
  （`TestDataBlock.java:27`／`:41`）。生成時に `null` を拒否し、空文字をデフォルトグループとする。
  **`null` を空文字へ正規化する案は採らない** —— 黙って置き換えると呼び出し側のバグが正当な省略と
  見分けられなくなる。辺③の `SETUP_TABLEnull=T`（`XlsFormatWriter.java:557`）と辺④の NPE
  （`YamlFormatWriter.java:529`）はこれで自然に消えるため、番人は追加しない。

**`null` の生成元は特定済み（2026-08-19・実物確認）。両方とも NTF 仕様適合入力からは生まれない**ため、
方針 4 のリーダー修正は不要である。
- `groupId`: 辺①は `TestCoreReaderAdapter.java:365-369` で `markerGroupId` が `null` を返す行をブロックごと
  `continue` するため `BlockHeader.groupId` は必ず非 null（無指定は空文字）。辺②は
  `YamlFormatReader.java:486-487` が省略時に明示的に `""` を返す。どちらのリーダーも `null` を作れない。
- `name`: 辺②は `YamlSection.toStr(null)` が `null` を返す（`YamlSection.java:112-114`）が、それは `name` キーの
  無い YAML ＝ スキーマの `required` ＝ `["name","type"]` 違反であり仕様不適合入力である。辺①は
  `FragmentView.getNames()` 由来で、名前行は本体パーサが 2 列以上を要求する。

---

## 不具合修正の対象と手順（#25.5）（ユーザー確定・2026-08-14）

**方針を変えた。** #19〜#25 は「見つけた不具合は直さず記録する」で通してきたが、**`issues.md` の
「NTF 仕様としての判定」が要対応であるものに限り修正する**。それ以外は従来どおり記録のみである。
Rules の該当 2 行と Acceptance criteria の 2 行はこの決定に合わせて改訂済み。

> **件数ではなく `issues.md` の判定欄を正とする書き方へ改めた（2026-08-18）。** 理由は 2 つある。
> (1) 件数は #25.5 の途中で **5 → 6 → 7 → 15** と 4 回動いており、そのたびに steering の複数箇所が
> 取り残された。実際に Rules の「要対応 7 件の修正に限り src/main を変更する」は、`7200b0f` で
> 要対応になった XLS-08・XLS-27〜33 が**範囲外に読める**状態のまま残っていた。
> (2) 判定の正は各課題の「NTF 仕様としての判定」欄であり、steering に件数を書くのは同じ事実の二重管理に
> なる。**steering は参照に留め、実数は `issues.md` 冒頭の導出コマンドから導く。**
> 件数を併記する場合は「2026-08-18 時点で 15 件」のように**時点を添える**。

**現況（2026-08-19 §6-K 後に再実行・`issues.md` 冒頭の導出コマンドで確認）**: 全 55 件・**要対応 25 ／
対応不要 27 ／ 保留 2 ／ 本作業の対象外 1**（区分外 0・二重 0）。**要対応 25 件はすべて修正済み**（XLS-06・
XLS-08・XLS-16・XLS-20・XLS-21・XLS-22・XLS-27・XLS-28・XLS-29・XLS-30・XLS-31・XLS-32・XLS-33・
XLS-34・XLS-35・XLS-36・XLS-37・XLS-38・XLS-40・XLS-41・XLS-43 ／ YML-02・YML-03・YML-08・YML-12）。
内訳は `issues.md` 冒頭にも書いてある。

> **「未完 1 件（XLS-27・本体修正待ち）」は取り消した（2026-08-19）。** §6-K（`839bf64`）で
> **マーカーカラム 1 列（`[EMPTY]`。当時の値は `[空]`。#26.5 で改めた）で 0 件テーブルを書けることが明文から確定した**ため、
> 「converter では閉じ切れない」という前提そのものが誤りだった。**あわせて XLS-21 の判定が
> 「対応不要」から「要対応」へ移った**ので、要対応は 24 → 25 ／ 対応不要は 28 → 27 になっている。

**判定に「保留」を足した（ユーザー指示・2026-08-19）。** **明文が converter 側の判断で埋まらない**課題に
使う区分であり、「対応不要」（明文に反しないので直さない）とは区別する。**保留は実装しない。**
現在 2 件ある。

- **XLS-42**（`fields` 件数より**値が少ない**行）—— **明文どうしが矛盾している。** `notation:883` は
  「行の要素数がフィールド数より少ない場合、不足したフィールドは `""` として補完される」と定め、さらに
  「全フィールドを省略した行（YAML 形式では `rows:` に空配列 `[]`）を書けば、全フィールドが `""` の
  レコードとして保持される」と**記法として明示的に案内している**。一方、本体スキーマ
  `$defs.record_fragment.rows` は「各配列の要素数が fields の件数と一致しない場合は NTF がエラーを出す」と
  定める。**一致を強制すると `notation:883` の書き方で書かれた仕様適合データを中間モデルが持てなくなる**ため、
  converter 側で一方に寄せて実装してはならない。**§6-G は実装から記録へ切り替えた。**
  **明文が一致している「多い側」は XLS-41 として切り出し、要対応として §6-G で修正した**
  （`166a199`。根拠は `notation:891`「データ要素数が不正である」）。**矛盾が残る「少ない側」だけが
  この XLS-42 であり、保留のまま実装しない。**
- **XLS-39**（グループ ID に区切り文字 `[` `]` を含む）—— **明文が無い。** 旧判定「対応不要」の根拠は
  「記法がグループ ID に使える文字を定めていないから仕様外入力」というものだったが、これは**定めていない
  ことを禁止の根拠にしている**（Decisions「記法の根拠に実装の挙動を使わない」の「明文だけから組み立てる」に
  反する）。実測でも本体スキーマの `group_id` は 4 箇所すべて `minLength: 1` のみで文字種の制約が無く、
  `[a]x[b]` は**スキーマ適合の入力**である。**「グループ ID に使える文字を明文で定める必要がある」として
  解説書担当宛の申し送りを 1 件立てた**（`issues.md` XLS-39 の節の直後）。

**2026-08-18 の 全 44 件・15 ／ 28 から動いた理由は 3 つある** —— XLS-20 の判定を「対応不要」から
「要対応」へ変えたこと（`73297e2`）、**§6 で 8 件（XLS-34〜XLS-41）を新規に起こしたこと**、そして
**XLS-39・XLS-41 を保留へ戻したこと**である。

**以下のこの節の記述は、当時（全 37 件・要対応 7 件）の決定の経緯として残してある。**

**当初は 5 件だった。XLS-22 を 2026-08-18 に追加した（ユーザー確定）。** 判定の根拠を「到達可能性」から
**「両形式が表現できない値を中間モデルだけが保持できる＝中間モデルの契約の穴」**へ一本化したことによる
（`issues.md` **XLS-22**）。**中間モデルの契約は 4 辺すべてが表現できる範囲で定める** —— これが判断の枠組みであり、
「到達不能だから対応不要」はリスクの判断であって NTF 仕様としての判断ではない。
同じ中間モデル値の辺④版である **YML-12 3形目**も同時に対象へ入った。

**さらに YML-03 を 2026-08-18 に追加した（ユーザー指示）。** 対象外としていた理由は「帰属が
nablarch-testing-yaml 側にあり converter だけでは直せない」であって、判定そのものは当初から**要対応**である。
その yaml 側が `0b53910`（ブランチ `feature/ntf-yaml`）で直ったため、対象外の理由が消えた。
**これで修正対象は 7 件になり、「修正しない」は XLS-01 の 1 件だけになった。**

**出典の版**: 以下で `notation:nnn` と書くのは
`~/work/nablarch/nablarch-document/ja/development_tools/testing_framework/implementation/testdata_notation.rst`
の行番号（`nablarch-document` ブランチ `ntf-yaml-support` の **`30a8271`**（2026-08-18 08:54:15 +0900）時点）。
**引用は全件、実物を開いて確認した。**

> **基準を `df7bff7` から `30a8271` へ貼り替えた（2026-08-18・ユーザー指示）。** 行番号は文書が動けばずれるため、
> **どのコミット時点かを書かない行番号は出典として使わない**。`df7bff7` → `30a8271` のずれは
> **499 行目以降が一律 +2**（`@@ -497,6 +497,8 @@` でラベル行と空行が入ったため）で、
> 引用している全行を両版から取り出して本文一致を突き合わせ済みである。読み直すときは
> `git show 30a8271:ja/development_tools/testing_framework/implementation/testdata_notation.rst` を開く。
>
> **`nablarch-document` の HEAD は作業中も動いている。** 2026-08-18 14:29 時点で `ntf-yaml-support` の
> HEAD は `f2feca2`（2026-08-18 14:20:53 +0900）であり、
> `git diff --stat 30a8271 f2feca2 -- ja/.../testdata_notation.rst` は
> `76 insertions(+), 76 deletions(-)`（総行数は 1554 行のまま。両版とも `wc -l` は 1554）。
> 差分を実物で確認したところ、**変更は RST のエスケープ（``` `` ``` の前の `\ ` の有無）だけで
> 本文の意味は変わっていない**ため、引用している行の本文と行番号は `f2feca2` でもそのまま通る。
> とはいえ HEAD は今後も動くので、**基準は `30a8271` に固定したまま引用する**（ユーザー確定・2026-08-18）。

### 修正する 7 件（**2026-08-18 時点の決定。その後 8 件が要対応へ加わった。上の「現況」を参照**）

| ID | 辺 | 現在の挙動 | 仕様どおりの期待値 | 出典（確認済み） |
|---|---|---|---|---|
| YML-02 | ② | `group_id` を省略した送信同期エントリをブロックごと落とす | 省略時はデフォルトグループのブロックとして読む | `notation:254`「グループIDを省略した場合は、グループIDを持たないデータブロック（デフォルトグループ）が対象になる」 |
| YML-12 | ④ | レコードが空のファイルブロックで `records:` キーごと落とす | `records: []` を出力する | `notation:881`「0バイトの空ファイルは、レコード定義を持たないファイルデータブロックとして表現する」／`notation:1146`「0バイトの空ファイルを表現するには、`records:` に空配列 `[]` を記載する」 |
| XLS-16 | ③ | シート名を 31 文字へ黙って切り詰める | 黙って切り詰めない。31 文字超は例外で落とす | `notation:590`（下記の訂正を参照） |
| XLS-06 | ① | レコード種別の空セルを `""` にする | `null` を入れる（辺②と同じ） | `RecordLayout.java:26`「レコード種別（省略時は `null`）」 |
| XLS-22 | ③④ | `fields` が空の `RecordLayout` を、Excel は読み戻せない版面として・YAML は `fields: []` として書き出してしまう | 書き出し側が `IllegalArgumentException` で落とす（`RecordLayout` の Javadoc に「`fields` は 1 件以上」の契約を明記する） | `notation:888`「フィールド名称リストまたはデータ型リストが未指定または空である」を記述時のエラーに挙げる（＝**その形は Excel 記法として存在しない**）／YAML 本体スキーマ `nablarch/test/ntf-testdata-yaml-schema.json` の `$defs.record_fragment` が `fields` を必須かつ `minItems` ＝ 1 とする |
| YML-03 | ② | `record_type: "FW_HEADER"` のレコードを、メッセージ系・送信系でだけ黙って捨てる（ファイル系では残る） | 3 経路とも捨てずに残す | YAML 本体スキーマ `nablarch/test/ntf-testdata-yaml-schema.json` の `$defs.record_fragment.properties.record_type.description`「可読性のために任意の名前を記述してよい。**FW_HEADER のような予約値はない**」／`$defs.message_data.properties.records.description`「**旧形式の record_type: FW_HEADER は廃止**」。修正の出典は yaml 側 `0b53910`（ブランチ `feature/ntf-yaml`） |
| YML-08 | ② | ディレクティブ値の実制御文字を素通しする | 辺①（`XlsFormatReader#normalizeDirectiveValue`）と同じ逆正規化を通す。対象は `record-separator` ／ `field-separator` | `notation:947-948`（`record-separator` は シンボル または任意のリテラル文字列）／`notation:1080`（`field-separator=\t`）／`notation:1116`（`record-separator CRLF`）。いずれもシンボルとエスケープ 2 文字の記法しか示していない |

**XLS-16 の出典を訂正した。** 当初示された `notation:68`（「シート名をテストメソッド名と同名にする」）は
実際には `notation:69` であり、**その直後 `notation:73` の tip が「シート名とテストメソッド名の対応は
『制約』ではなく『推奨』であり、両者が異なっていても正しく動作する」と明記している**。
したがって「テストメソッド名と揃える推奨」から切り詰めの不当性は導けない。**根拠は `notation:590` に置く**
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

**残る 2 形目（`message_data.records` 空）と 4 形目（`field_def.type` 省略）も要対応にした**
（ユーザー確定・2026-08-18）。**3 形とも同じ「4 辺のどれも表現できない値を中間モデルが保持している」形**であり、
XLS-22 と同じ手順（Javadoc に契約を明記／辺③④に番人／現状固定テストは置き換え／TDD・1 コミット）で閉じる。
`YML-12` は課題 ID としては既に要対応なので、**この 2 形の追加で ID 単位の件数は動かない。**

- **4 形目（`FieldDef.type` が `null`）** — `notation:883`「固定長ファイルでは、フィールド名称・データ型・
  フィールド長の3リストが同サイズで必須であり…可変長ファイルでは、フィールド名称・データ型の2リストが
  同サイズで必須であり」／`notation:885`「ファイルデータの記述時にエラーとなるのは、以下のようなケースである。」＋
  `notation:888`「フィールド名称リストまたはデータ型リストが未指定または空である」／YAML 本体スキーマ
  `$defs.field_def.required` ＝ `["name", "type"]`。**Excel 記法・YAML 記法のいずれも型の無いフィールドを
  認めていない。**
- **2 形目（`MessageDataBlock.records` が空）** — 「Excel は表現できるが YAML は表現できない形式間の非対称」
  ではない。**Excel も表現できない。** `notation:1158`「フレームワーク制御ヘッダ以降のメッセージボディは、
  フィールド名称・データ型・フィールド長・データという、前述のファイルデータと同じ構成を持つ」は、
  直前の列挙が示すとおり**カラム構成のみ**を指し、0 バイト空ファイルの特例までは及ばない。
  0 バイト特例は `notation:881`「0バイトの空ファイルは、レコード定義を持たないファイルデータブロックとして
  表現する」／`notation:1109`「0バイトの空ファイルを表現するには、ディレクティブのみを記述してレコード定義を
  省略する」／`notation:1146`「0バイトの空ファイルを表現するには、`records:` に空配列 `[]` を記載する」と、
  **いずれもファイルに限定して書かれている**。電文についてレコード 0 件の記法は明文が無く、電文が存在しない
  場合の記法は `notation:1257`「応答不要メッセージ受信では…`expectedMessages` のデータブロックを記述する
  必要はない」＝**ブロックごと省略する**である。したがって YAML スキーマの `$defs.file_data.records.minItems` ＝ 0 と
  `$defs.message_data.records.minItems` ＝ 1 の差は**不整合ではなく、明文の有無に対応した意図的な非対称**であり、
  **スキーマ側は正しく、直す対象ではない。**

### `RecordLayout` コンストラクタに番人は置かない（ユーザー確定・2026-08-18）

契約（`fields` は 1 件以上／`type` は `null` 不可）の検査を中間モデルのコンストラクタへ前倒しする案は**却下した**。
**番人の役割は「どの形式にも写せない値をそこで止めること」であり、それが起きる場所は辺③④である。**
中間モデル側で止めると、本作業の目的である 4 辺の担保から検査点が外れる。実測でも、コンストラクタ番人は
541 件中 1 件（`RecordLayoutTest#レコード種別省略をnullで保持する`）だけを落とし、代わりに辺③④の番人テスト
4 件が書き出し側を検査しなくなる。判断と実測は `issues.md` **XLS-22** に残す（同じ検討を蒸し返さないため）。
`RecordLayoutTest#レコード種別省略をnullで保持する` は現状のまま（`List.of()` を渡す）でよい。

### 記法の根拠に実装の挙動を使わない（ユーザー指示・2026-08-18）

**「自前プローブで辺③→辺①の往復が通った」は、本体パーサがその形を受け付けるという実装の事実であって、
NTF 仕様（解説書）がその形を記法として認めている根拠にはならない。** むしろ「記法に無い形を実装が通してしまう」
なら、それ自体が不具合の候補である。**判定は解説書（`testdata_notation.rst`）と本体スキーマの明文だけから
組み立てる。** ②の検討でこの誤りが出たため、以降のすべての判定に適用する。

### 中間モデル一巡点検で出た 7 件を全件 #25.5 に含める（ユーザー確定・2026-08-18）

中間モデルの一巡点検で見つかった「両形式が表現できない値を中間モデルだけが保持できる」7 件（A〜G）を、
記録だけでなく**全件 #25.5 で修正する**。理由は 2 つ（ユーザー確定）——(1) 契約の穴を残したまま #26 へ進むと
番人の分岐が増えてカバレッジ計測をやり直すことになる、(2) E・F・G の「辺③④の片方だけに番人がある非対称」は
どちらが正しいかを決めないと 4 辺の担保にならない。

**条件 2 つ（ユーザー指示）**:

- **番人を置く前に、その形が記法の外であることを明文で確かめる。**「到達できないから」「実装がそう動くから」は
  根拠にならない。明文が無ければ番人を置かず、「明文が無い」と記録する
- **E・F・G の非対称は、どちらへ揃えるかを明文で決める。** 片側に合わせる理由を出典付きで書く

**出典は全件、`30a8271` の実物を開いて確認済み。**

| # | 項目 | 対象フィールド | 明文の出典 | 番人の要否・揃える先 |
|---|---|---|---|---|
| A | カラム名 0 件 | `ColumnRowDataBlock.columnNames` | `notation:652`（テーブルデータは「データタイプと識別子の値・カラム名・データ行」という共通の構成を持つ）／`notation:802`（Excel はカラム名行を省略できない）／`notation:628`（LIST_MAP） | **要**（辺③）→ **実装済み `57c1b0d`**（XLS-27 の当面の対応） |
| B | ファイル種別 `null` | `FileDataBlock.fileType` | `notation:1146`／`notation:883`／YAML 本体スキーマ `$defs.file_data.required` に `type`、`type.enum` ＝ `["fixed","variable"]` | **要**（辺③④とも） |
| C | フィールド長 `null` | `FieldDef.length` | `notation:883`／`notation:889`／`notation:1158`（電文も同構成） | **要**（辺③④とも）。ただし**固定長ファイルと電文に限る**。可変長では `null` が正 |
| D | 名称 `null` | `FieldDef.name` | `notation:888`／YAML 本体スキーマ `$defs.field_def.required` ＝ `["name","type"]`・`name.type` ＝ `"string"` | **要**（辺③④とも）。既存の `type` 番人（`f80c192`）と同型 |
| E | グループ ID `null` | `TestDataBlock.groupId` | `notation:254`（省略かデフォルトグループかの 2 値であり `null` は無い）／YAML 本体スキーマ `group_id` は `type: string, minLength: 1` | **要**（辺③④とも。現状どちらにも番人が無いので揃える先の判断は不要） |
| F | セクション名 `null` | `TestDataSection.name` | `notation:590`（読み込み単位の名前は Excel 形式ではシート名、YAML 形式ではファイル名） | **要**。**辺③（弾く側）へ揃える** —— 辺④は現状 `null.yaml` を作る。`notation:590` は名前がファイル名／シート名になると定めており、名前が無い状態を認める明文が無い |
| G | データタイプ `DEFAULT` | `TestDataBlock.dataType` | `notation:206`・`notation:212-235`（「対応は、以下のとおりである」＝ `:206` と、その YAML 最上位キー対応表 `:212-235` に **`DEFAULT` の行が無い**） | **要**。**辺④（弾く側）へ揃える** —— 辺③は現状 `DEFAULT=T` と書けるが読み戻すと消える（XLS-20）。**根拠が「対応表に行が無い」という不在である点は記録に明示する** |

**`DataType.DEFAULT` は記法の予約語である。** `notation:188-190` のデータタイプ表に載っている
（「フレームワーク内部用（通常は使用しない）」）。`issues.md` **XLS-20** の
「記法の予約語（`notation:126`）に無く」は事実誤りであり、G の実装と併せて訂正した（`7c10654`）。

> **A〜G の 7 件は 2026-08-19 に全件完了した。** ただし**揃え先は当初の記載から変わっている** ——
> 2026-08-19 の Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」により、
> B〜G は書き出し側の番人ではなく**中間モデルの生成時拒否**で閉じた（A の `columnNames` 0 件だけは
> 辺③の番人のまま。**マーカーカラム案の実測は 2026-08-19 に完了し (1)〜(4) すべて通ったが、採否が未決のため番人を維持している**）。上表の「番人の要否・揃える先」欄は
> **点検時点の判断**であり、実際にどう閉じたかは各タスク行と `issues.md` の各課題を参照すること。
>
> **#22 の完了条件（本ファイル後半）に残る「辺③④の `DEFAULT` の扱いの非対称が `issues.md` に
> 記録され、かつ修正されていない」は #22 完了時点の記述である。**§1-G で修正したため、現在の実装は
> それと異なる。

### ~~XLS-27 の番人は 0 件テーブルを含む YAML を変換不能にする~~（実測・2026-08-18。**2026-08-19 の §6-K で解消**）

> **この節は 2026-08-18 時点の記録であり、制約はもう無い（2026-08-19・§6-K・`839bf64`）。** 0 件テーブルは
> マーカーカラム 1 列（`[EMPTY]`。当時の値は `[空]`。#26.5 で改めた）で Excel に書けるため、番人（`57c1b0d`）を撤去した。明文の根拠は
> `notation:836` ／ `:802` ／ `:819` ／ `:1515` ／ `:1550` の 5 か所である（`issues.md` XLS-27 の【決着】節）。
> **climan サンプルは 2 冊のブックへ変換できる。** 下の最終段落が予告していた
> 「本体修正後は変換成功を確認するテストへ戻す」は、本体修正を待たずに実施済みで、
> `SampleConversionTest#stopsClimanSampleConversionBecauseOfZeroRowTable` は変換成功を主張する形へ反転した。
> **以下は当時の記録として残す。**

A の番人（`57c1b0d`）を入れた結果、**同梱している climan サンプル自身が YAML→XLS 変換できなくなった**。
`SampleConversionTest/ClientActionTest/testShowWithEmptyClientTable.yaml` と `testFindNoClients.yaml` が
`setup_tables` 配下に 0 件テーブル（`rows: []`）を持つためである。
「空のテーブルを用意する」は NTF の日常的なテストパターンであり、**本体修正が入るまで 0 件テーブルを含む
YAML はすべて変換できない**。

> **当初「計 4 箇所」と書いていたのは不正確だった（追試で判明・`a03c54d`）。** `rows: []` は 3 ファイル・
> 4 箇所あるが、番人に当たる 0 件**テーブル**は上記 2 箇所だけである。
> `ExportProjectsInPeriodActionRequestTest/testNormalEnd.yaml:173`／`:199` の `rows: []` は
> `expected_files` 配下の**ファイルデータの 0 件レコード**であり、番人（`layoutColumnRow` ＝ テーブル／
> `LIST_MAP` のみを通る）には当たらない（実測。このディレクトリ単独では変換が成功する）。
> 詳細は `issues.md` **XLS-27**「影響範囲（制約・実測 2026-08-18）」。無言で壊れた `.xlsx` を書くよりは中止が正しいという判断で入れているが、
XLS-27 の 2 段目（本体修正後に「識別子行だけを書く」へ切り替え）が済むまでは実運用上の制約として残る。
`SampleConversionTest#stopsClimanSampleConversionBecauseOfZeroRowTable` がこの制約を固定しており、
**本体修正後は変換成功を確認するテストへ戻す。**

### 修正しない 2 件 →（2026-08-18 以降）1 件（判定の根拠つき）

- **YML-03** — 原因は nablarch-testing-yaml の `YamlFileBuilder#skipFwHeader` にあり、converter 側だけ直すと
  `IllegalStateException` になる。**仕様どおりの期待値を書いた `@Ignore("YML-03: yaml側の修正待ち")` の
  テストを置いて待つ。**
  **→ 待ちは解消した（2026-08-18・ユーザー報告）。** yaml 側が `0b53910`（ブランチ `feature/ntf-yaml`）で
  `skipFwHeader` の特別扱いを廃止したため、`mvn install` で `~/.m2` を差し替えたうえで
  converter 側の `YamlFormatReader#recordsWithoutFwHeader` を廃止し（yaml 側で
  `YamlSection.FW_HEADER_RECORD_TYPE` が消えたため、この廃止はコンパイルにも必須）、
  待機テスト 2 件の `@Ignore` を外して緑にした。**#25.5 で修正した件数は 6 件 → 7 件になり、
  「修正しない 2 件」は XLS-01 の 1 件になった。** 詳細は `issues.md` **YML-03**。
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

**Purpose**: 要対応と判定したもの（**2026-08-18 時点で 15 件。当初 5 → 6 → 7 → 15 と増えた**）を TDD で修正し、不具合の挙動を緑のアサートで固定している状態を解消する。判定と出典は Decisions「不具合修正の対象と手順（#25.5）」。

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
- [x] **YML-03（辺②）: yaml 側 `0b53910` の取り込み後に修正する**（追加・ユーザー指示 2026-08-18）。`mvn install` で `~/.m2` を差し替え、`YamlFormatReader#recordsWithoutFwHeader` を廃止してファイル系と同じ `#records(entry)` に揃え、待機テスト 2 件の `@Ignore` を外して緑にする
- [x] XLS-01: テストを削除せず、アサートを「仕様外入力のため値は保証しない」旨へ書き直して実挙動の記録として残す
- [x] `issues.md` の全課題に「NTF 仕様としての判定」欄を追加し、要対応／対応不要／本作業の対象外を出典つきで明記する（欄を足した時点は全 36 件・要対応 7 件／対応不要 29 件。**その後 `7200b0f` で XLS-27〜33 の 7 件が加わり XLS-08 が要対応へ移ったため、現在は全 44 件・要対応 15 ／ 対応不要 28 ／ 本作業の対象外 1**。導出コマンドは `issues.md` 冒頭）。**既存の「判断」欄は往復基準（変換結果が入力と一致するか）で書かれており、辺ごとの判定とは別物なので、両方を残して区別する**
- [x] YML-08 の「未確認」を潰す: 中間モデルに入った実改行を辺④が書き出したとき、読み戻しで空文字になるかを**実行して**確かめ、結果を `issues.md` に記録する
- [x] **（方法論の訂正）** `notation:nnn` の引用を `nablarch-document` の `30a8271` 基準へ貼り直し、基準コミットを本文に明記する（`issues.md`・`steering.md`・`inventory.md`・`src` の Javadoc）→ `179fb07`（steering）／`a667893`（issues・inventory・src）。**「499 行目以降が一律 +2」は目安であり、既に `30a8271` 基準の引用が混在していたため、全件を引用文と実物で突き合わせて決めた。`grep -rn "df7bff7" src .rn/ntf-test-data-converter/coverage` は 0 件**
- [x] **YML-12 4形目（辺③④）**: `FieldDef.type` は必須（`null` 不可）を Javadoc に契約として明記し、書き出し側が `IllegalArgumentException` で弾く（追加・ユーザー確定 2026-08-18）→ `f80c192`
- [x] **YML-12 2形目（辺③④）**: `MessageDataBlock.records` は 1 件以上を Javadoc に契約として明記し、書き出し側が `IllegalArgumentException` で弾く（追加・ユーザー確定 2026-08-18）→ `04873de`
- [x] `RecordLayout` コンストラクタに番人を**置かない**判断と、却下した理由・実測を `issues.md` に残す（`XLS-22` の節）→ `ea52297`
- [x] 本体パーサがレコード 0 件を受け付ける事実を、**新規 ID `XLS-26`** で `issues.md` に記録する（判定「本作業の対象外・記録のみ」／モジュールは `nablarch-testing` 本体）→ `a37eeb3`。**対象は `MessageParser`（電文）に限定した。`FixedLengthFileParser` 単体のレコード 0 件は 0 バイト空ファイルとして `notation:881`／`:1109`／`:1146` に明文があり不具合ではない**
- [x] **XLS-08（辺①）**: マーカーカラム除外の**後**に空エントリ判定を行う（追加・ユーザー確定 2026-08-18。判定を「対応不要」→「要対応」へ変更）→ `a794a8e`
- [x] **XLS-27（辺③・当面の対応）**: カラム名を 1 件も持たないテーブル系ブロックを `IllegalArgumentException` で弾く（新規課題・ユーザー確定 2026-08-18。本体修正後に「識別子行だけを書く」へ切り替える 2 段構えの 1 段目）→ `57c1b0d`
- [x] **XLS-21 の機構を XLS-27 へつなぐ**（§5・ユーザー確定 2026-08-18。判定は対応不要のまま）→ `8f55f78`
- [x] **XLS-27（記録・ユーザー指示 2026-08-18）**: 実測した現象を `issues.md` XLS-27 の証拠として残す —— `EMPTY_T`(`rows: []`) の直後の `NEXT_T` が消え、`EMPTY_T` の `rows` に `SETUP_TABLE=NEXT_T` の `"C1"` と `"v1"` の 2 行が入る。**警告が出ないことも併記する**。あわせて影響範囲として「本体修正（またはマーカーカラム案）が入るまで、0 件テーブルを含む YAML は Excel へ変換できない」を明記し、同梱サンプル 4 箇所が該当する事実を添える。**この制約は解説書担当と対象 PJ へ伝える必要があるため、あとで拾えるよう課題側に書く** → `a03c54d`。**追試で「同梱サンプル 4 箇所」が不正確と判明した**（4 箇所のうち番人に当たる 0 件テーブルは `ClientActionTest` の 2 箇所だけ。残る 2 箇所は `expected_files` 配下のファイルデータの 0 件レコード）。記録はその事実へ訂正した
- [x] **XLS-27（マーカーカラム案の実測・ユーザー指示 2026-08-18）**: **番人の実装は変えずに**、カラム名を持たない 0 件テーブルの識別子行の次の行へマーカーカラムを 1 つだけ（例 `[空]`）書く形を端から端まで実測する。根拠になりうる明文は `notation:1515`（マーカーカラムは読み込み対象から除外される）／`notation:733`（データ行を 1 行も書かないことで 0 件にできる）／`notation:802`（カラム名行は省略できない）。確かめること —— (1) 本体（`nablarch-testing`）に読ませたとき `TableData` が有効カラム 0 個・行 0 件になるか (2) `SETUP_TABLE` として実行したとき全件 DELETE だけが行われ空行が INSERT されないか (3) 辺①で読んだとき中間モデルが `columnNames=[]`／`rows=[]` になるか (4) `EXPECTED_TABLE` でも同じか。**(1)〜(4) は現時点すべて未確認**。**採否はユーザーが決める**（記法がこの書き方を定めていないため、変換ツールが独断で出力するのは越権）ので、番人（中止）は維持したまま実測結果だけを返す。**いずれかで通らなかった場合は案を捨て、新規課題は起こさず XLS-27 に「マーカーカラム案は不可（理由）」を追記して本体修正待ちに戻す** → **実測 2026-08-19 完了。(1)〜(4) すべて通った**（`issues.md` XLS-27「マーカーカラム案の実測（2026-08-19・プローブ）」に全出力を記録）。**(1)** `TestCoreReaderAdapter#readTables(..., SETUP_TABLE_DATA)` で `table=T1 columns=[] rows=0`、後続 `T2` も食われず `tables=2`。**(2)** H2（`nablarch-test-support` の `db-default.xml`／`datasource.xml` 経由）へ 2 行入れたテーブルに対し、本体 `BasicTestDataParser#getSetupTableData` → `TableData#replaceData()` を実行し、SQL ログで `DELETE FROM XLS27_PROBE`（`update_count = 2`）と `INSERT INTO XLS27_PROBE(ID,NAME) VALUES (?,?)`（`batch_count = 0`）を実測。`before=2 → after=0`・残存行 0 で、**空行の INSERT は起きない**。**(3)** `new XlsFormatReader().read(...)` で `block=T1 columnNames=[] rows=[]`（XLS-08 の正規化がそのまま効く）。**(4)** `EXPECTED_TABLE` でも本体・辺①とも同じ。**番人（`57c1b0d`）は維持したままで `src/main` は無変更。プローブはリポジトリに残していない。採否はユーザーが決めるため辺③の改修（`layoutColumnRow` を「カラム名が空ならマーカーカラム 1 つを書く」へ差し替える）は行っていない。**
- [x] **§6-H（中間モデル。Map のキー・値の `null`）**: **要対応として実装する**（ユーザー確定・2026-08-19。§6-E の範囲外だったものを新規 ID で切り出す）。ディレクティブの Map（`FileDataBlock.directives` ／ `MessageDataBlock.directives`）と、同じ「名前・値」形式で記述するフレームワーク制御ヘッダ（`MessageDataBlock.fwHeaderFields`。`notation:1267`）について、**キー `null`・値 `null` を生成時に拒否する**（`ModelPreconditions` に寄せてよい）。根拠は `notation:906`「ディレクティブは…キー名と値の2要素で記述するものである（最低2要素が必要）」／`notation:892`「ディレクティブまたはレコード種別・フィールド名称定義の要素数が2未満である」（記述時エラー）と、本体スキーマ `$defs.directives` が各キーを `string` ／ `integer` ／ `boolean` と定め `null` を許さないこと。**キー `null`・値 `null` はどちらも NTF 仕様として表現できず、中間モデルが持てること自体が契約の穴である。** **空文字は拒否しない**（空文字を禁じる明文が無い。往復で `null` → `""` になる件は `null` を生成時に拒否すれば発生しなくなる）。**進行順**: ユーザーの指示は「1（§6-F の残り）と 3（§6-G の切り出し）を先に片付けてから XLS-27 の実測へ進む。2 は新規課題として台帳に立てる」であり、本ステップの実装順はそこに含まれていないため **XLS-27 の実測のあと**に置いた（**台帳への登録は完了。`XLS-43` として `88835b9` で立てた**。要対応 24 件のうち未完 2 件が XLS-27 と XLS-43 である） → **2026-08-19 に実装完了**。`ModelPreconditions#requireNoNulls(String, Map)` にキー・値の `null` 検査を足した（呼び出し元 3 か所は既に同じメソッドを通っていたため、**書き出し側には何も足していない**）。担保テストは `FileDataBlockTest` 3 件（うち 1 件は**空文字が通ること**の担保）と `MessageDataBlockTest` 2 件。**既存の `XlsFormatWriterTest#writesOmittedMetaAndFieldAsEmpty` はディレクティブ値 `null` を入力にしていたが、その入力がもう作れないため空文字へ書き直した**（§1-C で長さを `null` → 空文字へ書き直したのと同じ扱い）。実測 `mvn clean test` は `Tests run: 599, Failures: 0, Errors: 0, Skipped: 2`。
- [x] **§1-B（辺③④）**: `FileDataBlock.fileType` が `null` のブロックを弾く → `44469b2`（SHA の記録は `b7c1f86`）
- [x] **§1-C（辺③④）**: `FieldDef.length` が `null` のフィールドを弾く（**固定長ファイルと電文に限る**。可変長では `null` が正） → `3000baf`（SHA の記録は `9062039`）
- [x] **§1-D（中間モデル）**: `FieldDef` の生成時に `name` ＝ `null` を拒否する（空文字は通す）。**承認済みの `type` の番人（`f80c192`）も同じ形へ寄せ、辺③④のチェックを外した** → `d0023c0`（`issues.md` の反映は `f39f7b1`）。明文確認の記録は `issues.md` XLS-31 の「§1-D の明文確認」節。**方針 4（生成元の特定）: 辺①②のどちらのリーダーも `name` ＝ `null` を作らないためリーダー側の修正は不要**
- [x] **§1-E（中間モデル）**: `TestDataBlock` の生成時に `groupId` ＝ `null` を拒否する（空文字はデフォルトグループとして通す）。sealed 階層の根 1 箇所でブロック 4 種別すべてを覆う。辺③の `SETUP_TABLEnull=T`（`XlsFormatWriter#marker`）と辺④の `NullPointerException`（`YamlFormatWriter#rawGroup`）は到達不能になり、番人は追加していない → `5abc773`（`issues.md` の反映は `f39f7b1`）。**方針 4: 辺①は `TestCoreReaderAdapter.java:365-369` が `markerGroupId` ＝ `null` の行を読み飛ばし、辺②は `YamlFormatReader.java:486-487` が空文字を返すため、リーダー側の修正は不要**
- [x] **§1-F（中間モデル）**: `TestDataSection` の生成時に `name` ＝ `null` を拒否する（空文字は通す。POI の `sheetName '' is invalid` は Excel 形式固有の制約）。**辺③の `null` 分岐（`XlsFormatWriter#requireValidSheetNameLength`）と担保テスト `rejectsNullSheetName` を外し、31 文字上限の検査だけ残した**（Excel 固有の上限は中間モデルの不変条件ではないため）。辺④は無変更で `null.yaml` が到達不能になった → `81cf234`。**方針 4: 辺①の `XlsFormatReader#sheetName`・辺②の `resourceName` 直渡しとも `null` を作らない**
- [x] **§1-G（中間モデル）**: `TestDataBlock` の生成時に `dataType` ＝ `DataType.DEFAULT` を拒否する。**辺③（`XlsFormatWriter#marker`）・辺④（`YamlFormatWriter#sectionKey` の `default:`）とも無変更で到達不能になった** → `7c10654`。`issues.md` XLS-20 の事実誤り（「`DEFAULT` は記法の予約語に無く」）も訂正し、**判定を「対応不要」から「要対応」へ変えた**（要対応 15→16・対応不要 28→27）。明文は `notation:188-190`（データタイプ表に `DEFAULT` の行があり「フレームワーク内部用（通常は使用しない）」）と`notation:206-241`（YAML トップレベルキー対応表に `DEFAULT` の行が**無い**）。**方針 4: 辺①は `HeaderCollector#parse` が `DEFAULT` 行を読み飛ばし、辺②は既知セクションキーのみを分岐に持つ**。**`dataType` ＝ `null` は §1-G では扱っていない（§6 で扱う）**
- [x] **XLS-28（辺①の入口）**: 同名で拡張子違いの Excel ブック（`Foo.xls` と `Foo.xlsx`）の同居を検出してエラーで止める（新規課題・ユーザー確定 2026-08-18。`notation:44`）→ `5ab13d8`。`ConverterFileFilter#findXlsFiles` が、変換対象になったブックごとに同じディレクトリの同名ブックを検査し 2 つ以上あれば `ConverterException` で止める。**判定は列挙結果どうしではなく実ディスク上の隣接ファイルで行う**（本体 `PoiXlsReader#open`（`nablarch-testing` の `PoiXlsReader.java:62-65`）は `.xls` を先に解決し、include／exclude を知らないため、片方を exclude で外しても読み違いは起きる）。変換対象にならなかったブックの同居は検査しない。テスト 5 件（`ConverterFileFilterTest` 4 件・`TestDataConverterTest` 1 件）
- [x] 中間モデルの全クラス・全フィールドを一巡し、「両形式が表現できない値を中間モデルが保持できる」箇所が他に無いかを点検して結果を記録する（無ければ「無し」と明記する）。**観点をもう 1 つ足す**（§6・ユーザー確定 2026-08-18）——**辺①が本体（`nablarch-testing`）経由で記法に無い形を中間モデルへ持ち込む経路が無いか**。XLS-08（マーカーカラムだけのブロックが「カラム 0 個・行 2 件」で入る）がその 1 例目である → **完了（2026-08-19）。結果は `coverage/issues.md` の「§6 中間モデル一巡点検（2026-08-19）」節**（一巡表 ＋ 新規課題 8 件 XLS-34〜XLS-41）。**「無し」ではなかった。**うち **6 件が要対応**（XLS-34 `dataType` ＝ `null` ／ XLS-35 `identifier` ＝ `null` ／ XLS-36 ブロッククラスと `dataType` の不整合 ／ XLS-37 `TestDataContainer.name` ＝ `null` ／ XLS-38 コレクション・Map の `null` と要素 `null` ／ XLS-40 カラム名・フィールド名の重複）、**2 件が保留**（XLS-39 グループ ID に `[` `]` を含む ／ XLS-41 `fields` 件数と行の要素数の不一致）。**足した観点（辺①が本体経由で記法に無い形を持ち込む経路）は新たな検出無し**（XLS-08 以外に無い）。**点検当日は「要対応 7 ／ 対応不要 1」と書いたが、同日のユーザー指摘で XLS-39 を「対応不要」から・XLS-41 を「要対応」から保留へ戻した**（集計は 全 44 → 52 件・要対応 16 → 22 ／ 対応不要 27 → 27 ／ 保留 0 → 2）
- [x] **§6-A（中間モデル）**: `TestDataBlock` の生成時に `dataType` ＝ `null` を拒否する（`issues.md` XLS-34）→ `b905183`。**辺③④は無変更**（`XlsFormatWriter#marker` の `DataType#getName`・辺④の `switch` の `type.ordinal()` による NPE が到達不能になった）。**方針 4: 辺①②のどちらのリーダーも `DataType` の定数を直接渡しており `null` を作らない**。テスト 1 件
- [x] **§6-B（中間モデル）**: `TestDataBlock` の生成時に `identifier` ＝ `null` を拒否する（`issues.md` XLS-35）→ `836a2a4`。**空文字は通す**（Excel は `id=[]` で往復し、YAML の `table: ""` はスキーマ適合）。**方針 4: 辺①は本体 `TestCoreReaderAdapter` がマーカー行の `=` の後ろを切り出し、辺②は `table` ／ `path` ／ `id` の必須キーから取るため `null` を作らない**。テスト 2 件
- [x] **§6-C（中間モデル）**: 各具象ブロックの生成時に、自分の系統に属さない `DataType` を拒否する（`issues.md` XLS-36）→ `1244e2b`。**辺③④は無変更**。`TableDataBlock` ／ `FileDataBlock` ／ `MessageDataBlock` の各コンストラクタが `super(...)` の**直後**に `TestDataBlock#requireDataTypeOf` を呼ぶ形にした（`ListMapBlock` は `LIST_MAP` を直に渡すため対象外）。取りうるデータ種別は各クラスの `PERMITTED_TYPES`（`EnumSet`。テーブル 3 種／ファイル 4 種／電文 5 種）に置き、`notation:212-235` の対応表の区切りに合わせた。**`super(...)` の引数位置ではなく直後に置いた** —— 引数位置だと `null` を素通しにする分岐を足すことになるが、直後なら `super(...)` が先に `null`（§6-A）と `DEFAULT`（§1-G）を専用メッセージで拒否するため、**到達しない分岐を作らずに §6-A のメッセージを保てる**。**方針 4: 辺①は `isTableType` ／ `isFileType` ／ `isSendSyncType` と `LIST_MAP` ／ `MESSAGE` で系統ごとに分岐してから該当クラスを生成し、辺②は各セクションキーの処理に `DataType` の定数を直接与えるため、どちらも不整合な組を作らない**。テスト 3 件（`94b0fbe` の RED をそのまま GREEN にした）
- [x] **§6-D（中間モデル）**: `TestDataContainer` の生成時に `name` ＝ `null` を拒否する（`issues.md` XLS-37。§1-F と同型）→ `5803fe6`。**空文字は通す**（ブック名の書式は Excel 形式固有の制約であって中間モデルの不変条件ではない。§1-F と同じ扱い）。**辺③④は無変更**（辺③が文字列連結で作っていた `null.xlsx` は到達不能になり、辺④はもともと器の名前を出力パスに使わない）。**方針 4: 辺①は `XlsFormatReader.java:703-706`、辺②は `YamlFormatReader.java:94` のとおり、どちらのリーダーも `name` ＝ `null` を作らない**。テスト 2 件
- [x] **§6-E（中間モデル）**: コレクション・Map の `null` と要素 `null` を生成時に拒否する（`issues.md` XLS-38。10 箇所 ＋ `columnNames` の要素）→ `d87bc0b`。検査は新設の `ModelPreconditions`（パッケージプライベート）に集約し、例外メッセージへ項目名と何件目かを出す。**要素の `null` は `columnNames` だけでなく `sections` ／ `blocks` ／ `records` ／ `fields` と `rows` の行そのものにも及ぶ**。**データ行の「セル」の `null` は通す**（`notation:767-772` ／ `:829-834` が記法として定めている）。**Map のキー・値の `null` は扱っていない** —— XLS-38 の観測（両辺が例外になる 10 箇所）に含まれず、ディレクティブ値 `null` は辺③が空セルとして書き出すため例外にならない（`XlsFormatWriterTest#writesOmittedMetaAndFieldAsEmpty` が固定済み）。往復で `""` になる点は未評価で、**要否はユーザー判断待ち**。**方針 4: 辺①②のどちらのリーダーも `null` のコレクションを作らない**。テスト 18 件
- [x] **§6-F（中間モデル）**: カラム名・フィールド名の重複を生成時に拒否する（`issues.md` XLS-40）→ **フィールド名称側のみ実装**（`29c9d1d`）。`RecordLayout` が同一レコード種別内のフィールド名称の重複を拒否する（`notation:887`。判定は完全一致。記法に大文字小文字を同一視する明文が無い）。テスト 2 件
- [x] **§6-F の残り（カラム名の重複）**: **番人も WARN も入れない。他責として扱う**（ユーザー確定・2026-08-19）。(a) 番人は**不可** —— `id` ／ `ID` を書いた YAML はスキーマ適合入力であり（本体スキーマ `$defs.table_data.properties.rows.items` は `{"type": "object", "additionalProperties": {"type": ["string", "null"]}}` のみで、キーの大小にも一意性にも制約を置かない）、番人は仕様適合入力を変換不能にする（Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」の 3「この拒否は入力の検証ではなく不変条件の保証である」に反する。番人が落としてよいのは仕様に合わない入力だけである）。(b) WARN も**採らない** —— 値が消える事実は変わらず、原因が converter の外にあるのに converter 側で肩代わりする暫定対応になる。**帰属は nablarch-testing** —— `nablarch/test/core/db/TableData.java`（`convert-testdata-excel-to-text` の `65911f5`）が `:97` `name.trim().toUpperCase()`／`:492` `columnNames[i].toUpperCase()`／`:530` `map.put(columnNames[i].toUpperCase(), value)` で大文字化しており、converter に止める権限は無い。**あるべき姿は「値が黙って消えないこと」** —— 記法はカラム名の大小の扱いにいっさい触れておらず（`notation:819` はカラム名を「最初の行のキーで決まる」とだけ定める。`大文字` の記述は `:768`／`:1323`／`:1393` の 3 箇所で、いずれも値の `null` 表記の話）、スキーマが大文字変換に触れているのは `table` キーの説明だけである。明文どおりに読めば `id` と `ID` は別カラムであり、両方の値が残る（LIST_MAP は同じ記法で実際そう振る舞う。`keepsOriginalColumnCaseInListMap`）。**やること**: (1) あるべき姿（`columnNames` ＝ `[id, ID]` で両方の値が残る）を主張するテストを足し、FAIL を確認したうえで `@Ignore`（理由＋他責先 `nablarch-testing` `TableData`）を付ける。**番人は入れない**。(2) 既存の `dropsValueWhenTableColumnNamesDifferOnlyByCase` は本体挙動の記録として残し、Javadoc に「他責の現状を固定したもの。あるべき姿は `@Ignore` 側」と明記する。(3) 解説書担当への申し送りを 1 件立てる（テーブル系のカラム名の大小の扱いの明文化）。(4) `issues.md` XLS-40 をフィールド名称側 修正済みとして閉じ、カラム名側を他責扱いに書き換える → `d737815`。`@Ignore` を外した実測の FAIL は `Expected: is <[id, ID]> but: was <[ID, ID]>`。全 590 件 PASS（Skipped 1 ＝ 今回の `@Ignore`）
- [x] **§6-G（中間モデル）**: **実装しない。記録へ切り替えた**（ユーザー確定・2026-08-19）。`fields` の件数と `rows` 各行の要素数の一致は**生成時に保証しない** —— `notation:883`（少ない側は `""` で補完され、`rows: []` の行は記法として明示的に案内されている）と本体スキーマ `$defs.record_fragment.rows`（一致しなければエラー）が**正面から食い違っており**、一致を強制すると仕様適合データを中間モデルが持てなくなる。**`issues.md` XLS-41 の判定を「要対応」から「保留」へ戻し、両方の出典を併記して矛盾の事実を記録した。** 明文が一致している「多い側」だけの切り出しは `notation:891` を根拠に要対応にできるが**ユーザー判断待ち**
- [x] **§6-G の切り出し（`fields` 件数より行の要素数が「多い側」）** → `166a199`（実装）・`88835b9`（台帳）: **要対応として実装する**（ユーザー確定・2026-08-19）。`RecordLayout` の生成時に**「行の要素数 ≦ `fields` の件数」**を保証する（**一致の強制ではない**）。根拠は `notation:891`「データ要素数が不正である」（ファイルデータの記述時エラー一覧）。**「少ない側」は `notation:883` により仕様適合であり拒否してはならない。** `issues.md` XLS-41 を「多い側」に限定して書き直し、判定を保留から**要対応**へ倒す。`notation:883` と本体スキーマ `$defs.record_fragment.rows` の矛盾（少ない側）は**保留のまま新規 ID で台帳に残し**、解説書・スキーマ担当宛の申し送りを添える。**完了（2026-08-19）。** `ModelPreconditions#requireRowsNotLongerThan` を追加し `RecordLayout` の生成時に呼ぶ。`RecordLayoutTest` に 3 本追加（多い側の拒否・件目の明示・少ない側と `[]` 行の保持）し、前 2 本が追加時点で RED であることを確認した。全体は **593 件・失敗 0・スキップ 1**（XLS-40 の `@Ignore`）（**当時「594 件」と書いたが、`166a199` を含む HEAD で `mvn clean test` を実行し直すと `Tests run: 593, Failures: 0, Errors: 0, Skipped: 1` である。2026-08-19 に再実測して訂正した**）。**方針 4 の実測**: 辺①は `fields` も行も `names.size()` 件で作るため構造的に多い側を作れない（`XlsFormatReader:394-399,422-427`）。辺②はプローブ実測で `fields` 1 件・`rows: [["a","b","c"]]` の YAML が `fields=1 rows=[[a]]` になり、**余りの値が黙って消える**（`YamlFormatReader:340-348`）。生成元は API の直接利用者だけである。**少ない側は `XLS-42`（保留）として切り出し**、解説書担当・スキーマ担当宛の申し送りを添えた（余りが黙って消える点の明文化も同じ申し送りに含めた）
- [x] **§6-G の切り出しの続き（辺②で余りの値が黙って消える件）** → `b19a236`: **独立した課題として台帳に立てる**（ユーザー確定・2026-08-19）。**`YML-14` として `YML-05` から「多い側」を切り出した。** 判定は **対応不要（帰属が converter の外＝本体 `DataFileFragment#addValue`）** で **YML-10 と同じ他責の扱い**。立て方は XLS-40 のカラム名側にそろえた —— (1) 現状を固定するテスト（`YamlFormatReaderInvalidInputTest#dropsRecordFragmentValuesBeyondFieldCount`。**既存テストをそのまま使い、重複は作らない**）の Javadoc に「他責の現状の記録であり、あるべき姿は `@Ignore` 側」と明記、(2) あるべき姿（`notation:891` に従い読み込みがエラーになる）を主張する `#failsToReadRecordFragmentRowWithMoreValuesThanFields` を追加し、`@Ignore` を外した実測で RED（`expected java.lang.RuntimeException to be thrown, but nothing was thrown`）を確認したうえで `@Ignore`（理由＋他責先＝本体パーサ、出典 `notation:891`）を付けた、(3) `src/main` は無変更・番人も WARN も無し、(4) XLS-42 の申し送りはそのまま残し YML-14 から参照を張った。**導出コマンド再実行の実測: 全 55 件・要対応 24 ／ 対応不要 28 ／ 保留 2 ／ 対象外 1（区分外 0・二重 0）。** 全体は **594 件・失敗 0・スキップ 2**（XLS-40 と YML-14 の `@Ignore`）。**なお「余り側は器の側」であること自体は `YML-05` の判定欄に既に記録されており、XLS-42 の引用ブロックの中にしか無かったわけではない**（実物で確認。切り出しの意義は、明文（`notation:891`）に反する多い側の判定を、明文どおりの少ない側と分けて独立に持たせた点にある）
- [x] **最後に 1 回だけ**、課題 ID 単位で要対応／対応不要の実数を確定する（①②の反映と中間モデル点検の後）。**2026-08-18 に中間の最新化を 1 回入れた（`3f38cca`。全 44 件・要対応 15 ／ 対応不要 28 ／ 本作業の対象外 1）**——集計記述が `7200b0f` を反映しておらず、`steering.md:40` が XLS-08・XLS-27〜33 を範囲外に読ませていたため。→ **2026-08-19 に確定した。`issues.md` 冒頭の導出コマンドを実行した実測値は 全 55 件・要対応 24 ／ 対応不要 28 ／ 保留 2 ／ 本作業の対象外 1、区分外 0・二重 0（`24 + 28 + 2 + 1 = 55`。`###` 見出しの数 55 とも判定欄の行数 55 とも一致）**。要対応 24 件のうち **修正済み 23 ／ 未完 1（XLS-27。本体修正待ちで converter 側では閉じ切れない）**。この内訳は `issues.md` 冒頭にも同じ数字で書いてある
- [x] `mvn clean install` を手順として Decisions に定着させる（`steering.md` の Decisions「ビルド・テストの実行方法」に記載）
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` で全 PASS を確認する
- [x] **§6-I（XLS-22 の番人移設）** → `c31b534`: **フィールド 0 件のレコードレイアウトを拒否する番人を、辺③④の書き出し側から `RecordLayout` の生成時へ移した**（Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」・2026-08-19 に沿う）。**きっかけは、辺③④の番人テスト 4 件がすでに空振りになっていたことである** —— `@Test(expected = IllegalArgumentException.class)` で例外の型しか見ておらず、§6-G（XLS-41）でコンストラクタに入れた別の番人が先に落としていた。`mvn test` の結果では区別がつかず、**JaCoCo の未到達行（`XlsFormatWriter.java:380-381` ／ `YamlFormatWriter.java:354-358`）だけが手がかりだった**。根拠は `notation:888`「フィールド名称リストまたはデータ型リストが未指定または空である」（記述時エラー）と本体スキーマ `$defs.record_fragment` の `fields` 必須・`minItems` ＝ 1。`ModelPreconditions#requireNotEmpty` を追加して `RecordLayout` の生成時に呼び、辺③④の番人は撤去した。**削除 4 件・追加 3 件・書き直し 1 件**（`git show c31b534 -- src/test` の実測）。追加の 3 件目は削除した番人テストの位置へ置き直した `XlsFormatWriterTest#writesEmptyCellsForRowShorterThanBlockWidth`（`notation:883` が正常と定める「不足側は空セルで補完される」の担保）。全体は **598 件・失敗 0・スキップ 2**。`issues.md` XLS-22 に「番人の移設（2026-08-19・実測）」として、プローブ出力・JaCoCo の実測・空振りの説明を記録した
- [x] `inventory.md` のテスト件数を、増減した箇所すべてコマンドから導き直す（Rules）。**2026-08-18 に一度済ませたが、§1-B〜G・XLS-28 でテストが増えるため無効になった。これらが済んだあとに 1 回だけやり直す** → **2026-08-19 に実施（`0a14655`）。** `inventory.md` §0.1-2 へ追補その 5 を足した。**①②③ の実測は `598` ／ `@Ignore` 2 件（YML-14 `YamlFormatReaderInvalidInputTest.java:740`・XLS-40 `:1277`）／ `8c327d0: 536, HEAD: 598`**、`mvn clean test` は `Tests run: 598, Failures: 0, Errors: 0, Skipped: 2`。**547 → 598 の内訳は削除 19 件・追加 70 件**で、ファイル別の増減表・削除 19 件の起点コミット（`git log -S` で全件裏取り）・`src/main` に手を入れた 17 ファイル・JaCoCo 6 クラスを載せた。**未到達は `YamlFormatWriter` だけ 行 1・分岐 1 増えた**（§1-G で `DataType.DEFAULT` を生成時に拒否したため `sectionKey` の `default` が到達不能になった。安全網として残し開示）。あわせて、§1〜§4 のスナップショット表に残る削除済みテスト名について**担保の現在地を対応表で一括提示**し（表そのものは取り決めどおり書き換えない）、`XlsFormatWriterModelTest` の件数を 15 → **11** へ導き直した
- [x] **§6-J-1（XLS-29 の番人移設）** → `7b0b381`: **ファイル種別 `null` の番人を辺③④の書き出し側から `FileDataBlock` の生成時へ移した**（型 2 ＝「明文に反する状態を中間モデルが持てるなら生成時に拒否する」）。明文は `notation:883`（記法は固定長ファイルと可変長ファイルの 2 種類に尽きる）・`notation:1146`（`setup_files` ／ `expected_files` の各エントリは `path` ／ `type` ／ `records` の 3 キーが必須）と本体スキーマ `$defs.file_data`（`type` は `required` かつ `enum` ＝ `["fixed", "variable"]`）。**赤の記録**（`src/main` に触れる前の実測）: `FileDataBlockTest.ファイル種別がnullのファイルブロックは生成できない:202` `java.lang.AssertionError: IllegalArgumentException が送出されるべき`。**削除 3 件・追加 1 件** —— 削除したのは `FileDataBlockTest#契約違反のnullファイル種別もモデル自身は検査せず保持する`（生成できなくなり主張が成り立たない）と辺③④の番人テスト 2 件（`@Test(expected = ...)` で型しか見ておらず、移設後は `new FileDataBlock(...)` の行で落ちて**空振りの緑**になる。§6-I と同じ形）。全体は **596 件・失敗 0・スキップ 2**
- [x] **§6-J-2（YML-12 2 形目の番人移設）** → `9e40644`: **電文の `records` 空の番人を辺③④から `MessageDataBlock` の生成時へ移した**（型 2）。明文は `notation:1257`（電文が存在しない場合はデータブロックごと省略する。レコード 0 件の電文を表す書き方は記法に無い）と本体スキーマの電文系 3 定義（`message_data` ／ `expected_request_message_data` ／ `group_message_data`）がいずれも `records.minItems` ＝ 1 であること。**0 バイトの空ファイル特例（`notation:1158` 付近）はファイルに限られ電文には及ばない。** 移設で**辺①②が実在の入力から 0 件ブロックを作っていたことが露見した**（失敗する場所が書き出し時から読み込み時へ前倒しになる。変換が失敗すること自体は移設の前後で変わらない）。実測した RED 2 件（`XlsFormatReaderRealFileTest.readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook` ／ `YamlFormatReaderTest.readMessage_emptyBody_isStillMapped`）はあるべき姿を主張するテストへ書き換えた。**空振りを残さないため書き出し側の番人テスト 4 件は削除した。** 全体は **593 件・失敗 0・スキップ 2**（596 − 4 ＋ 1）
- [x] **§6-J-3（XLS-30 の番人移設）** → `b762438`: **固定長ファイル・電文で `length` ＝ `null` の番人を辺③④から `FileDataBlock`（`FileType.FIXED` のとき）・`MessageDataBlock`（常に）の生成時へ移した**（型 2）。共通部は `ModelPreconditions#requireLengths`。明文は `notation:883`（固定長は 3 リスト同サイズ必須／可変長はフィールド長 不要）・`:889`（記述時エラー「フィールド名称・データ型・フィールド長リストのサイズが一致していない」）・`:1158`（電文ボディはファイルデータと同じ構成）。**本体スキーマ `$defs.field_def` の `required` は `["name","type"]` で `length` を含まないが、`properties.length.description` が「フィールド長（バイト数）。固定長ファイルでは実質必須（省略すると NTF が record-length を計算できない）。可変長ファイルでは不要（省略可）」と書いている**（実物を読んだ逐語）。**可変長は `null` が正しいため拒否しない。** 移設で**辺②が仕様不適合の入力から `length` なしの固定長ブロックを作っていたことが露見**（RED 1 件 `YamlFormatReaderTest.readFile_fixed_mapsRawFieldDefsAndValues:158`）。**番人テスト 4 件を削除**し、可変長で `null` を通す担保 2 件は残した。番人が外れて不要になった `YamlFormatWriter#emitRecords` の `lengthRequired` 引数も削除した。全体は **593 件・失敗 0・スキップ 2**
- [x] **§6-K（XLS-27 の番人撤去と XLS-21 の生成時拒否）** → `839bf64`: **0 件テーブルをマーカーカラム 1 列（`[空]`）で書き出す形へ切り替え、変換を中止する番人（`57c1b0d`）を撤去した**（XLS-27。型 1 ＝「明文だけで判断する」）。明文は 5 か所 —— `notation:836`（0 件テーブルにはカラム名を書く場所が無い）／`:802`（Excel ではカラム名行を省略できない）／`:819`（カラム名は最初の行のキーで決まる）／`:1515`（マーカーカラムを書ける）／`:1550`（マーカーカラムは読み込み対象から除外される）。**3 つを同時に満たす書き方はマーカーカラムだけである**ため、「converter は 0 件テーブルを Excel へ書けない」という前提そのものが誤りだった。あわせて **XLS-21 の判定を「対応不要」から「要対応」へ変え**、`ColumnRowDataBlock` の生成時に「カラム名 0 件で**セルを持つ**行」を拒否した（型 2。旧判定の根拠が「到達経路が無い」＝実装の到達可能性で、型 1 が根拠に禁じているものだった。明文は `:652` ／ `:819` ／ `:802`）。**セルを持たない行は拒否しない**ので、辺①②が正しく作る XLS-08 ／ YML-04 の形は通る。**2 つを 1 コミットにした理由**: 先に XLS-27 の番人だけ外すと、セルを持つカラム名なしブロックが黙って誤って書かれる窓が開き、また旧番人テストが構築するブロックは新しいモデル側の番人が先に落とすため、分けると必ずどちらかが壊れる。**削除 2 件**（`XlsFormatWriterTest#rejectsTableBlockWithoutColumnNames` ／ `#rejectsListMapBlockWithoutColumnNames`。空振りになるため）、`SampleConversionTest#stopsClimanSampleConversionBecauseOfZeroRowTable` は判定が覆ったので**反転**した（CliMan サンプルは 2 冊のブックへ変換できる）。全体は **597 件・失敗 0・スキップ 2**
- [x] **XLS-06・XLS-16 の決着（書き出し側に残す）** → `999f41d`: **どちらも中間モデルへ寄せず辺③に残す**（型 1。`src/main` の振る舞いは無変更で Javadoc のみ）。**XLS-06**（2 レコード目以降の `recordType` 空）—— `notation:1082`「新たなレコード種別とフィールド名称を書いた時点で、新しいレコードレイアウトとして扱われる」より Excel 記法では書き表せないが、`notation:1143`「先頭を空にするという Excel 形式の制約はない」と本体スキーマ `$defs.record_fragment` の `required` ＝ `["fields", "rows"]`（`record_type` を**含まない**）より**辺④では正しく書ける**。よって中間モデルの不変条件にできない。**XLS-16**（シート名 31 文字超）—— **記法 全 1554 行（`30a8271`）にシート名の長さを定める記述は 1 つも無い**（`grep` 済み）。31 文字は Excel の格納形式そのものの上限であり、辺④は同じ名前を書けるため不変条件にできない。**Javadoc に残っていた「ユーザー判断待ち」の記述は事実に反するので明文の根拠へ差し替えた**（決定 1・2026-08-19）。あわせて `XlsFormatWriterTest` のクラス Javadoc の件数を実測へ直した（40 件・build 28／write 10／2 → **45 件・31／12／2**）。**これで書き出し側に残っていた番人 7 つはすべて決着した**（`issues.md` 冒頭の決着表）
- [x] **台帳と件数の反映（§6-J・§6-K ぶん）** → `e7f19e3`（`issues.md`）・`1b82188`（`inventory.md`）・`2bbdbf2`（集計の導き直し）: `issues.md` に番人 7 つの決着表・XLS-27 の【決着】節（明文 5 か所）・XLS-21 の判定変更（旧判定は引用ブロックで保存。**導出コマンドが二重に数えないよう判定行を行頭に置かない**）を記録し、`inventory.md` §0.1-2 へ**追補その 6** を足した。**導出コマンドの実測**: ①`597` ／ ②`@Ignore` 2 件（YML-14 `YamlFormatReaderInvalidInputTest.java:740`・XLS-40 `:1277`）／ ③`8c327d0: 536` ／ `HEAD: 597`。`mvn clean test -Djacoco.skip=true` は `Tests run: 597, Failures: 0, Errors: 0, Skipped: 2`。台帳は **全 55 件・要対応 25 ／ 対応不要 27 ／ 保留 2 ／ 本作業の対象外 1（区分外 0・二重 0）**で、**要対応は 25 件すべて修正済み**（XLS-27 が §6-K で閉じ、XLS-21 が要対応へ移って閉じたため）。**JaCoCo の未到達は 1 つも増えていない** —— `XlsFormatWriter` 行 157/158・分岐 101/104、`YamlFormatWriter` 行 157/159・分岐 86/90、`ModelPreconditions` 40/40・28/28、`FileDataBlock` 14/14・4/4、`MessageDataBlock` 13/13・2/2、`ColumnRowDataBlock` 11/11・6/6、`RecordLayout` 15/15・2/2、`YamlFormatReader` 192/192・102/102、`DirectiveUtil` 20/20・17/18（未到達の行番号がずれただけであることを追補その 6 に明記した）
- [x] self-check（OK/NG per completion criterion、checks/task-25.5.md に記録）**#25.5 へ追加したステップの分を再実施する** → `bcd62b1`。**OK 13・NG 2** —— **NG-1: 修正前の赤の失敗メッセージが記録されていない 11 件**（§1-D `d0023c0` ／ §1-E `5abc773` ／ §1-F `81cf234` ／ §1-G `7c10654` ／ §6-A `b905183` ／ §6-B `836a2a4` ／ §6-D `5803fe6` ／ §6-E `d87bc0b` ／ §6-F `29c9d1d` ／ §6-H `7201650` ／ §6-I `c31b534`。コミット本文・`checks/`・`steering.md`・`issues.md` の 4 か所を検査して不在を確認した。**赤の確認自体は行っているが記録が無い。後付けの書き起こしはしない**）、**NG-2: `839bf64`（§6-K）が XLS-27 と XLS-21 の 2 件を含む**（分けるとどちらかがコミット単位で緑にならないため。理由は §6-K に記載）。実測は `Tests run: 597, Failures: 0, Errors: 0, Skipped: 2`
- [x] QA expert review（subagent）**追加ステップの分を再実施する** → `6114c35`。**指摘 6 件・採用 4 件／不採用 2 件（不採用はいずれも「既に満たしている」）。** 採用したのは次の 4 件で、**すべて実物を開いて裏を取ってから直した**。（1）YML-08 の根拠にしていた本体スキーマの description が**現在は存在しない** —— 正のスキーマ（`~/work/nablarch/nablarch-testing-yaml/src/main/resources/nablarch/test/ntf-testdata-yaml-schema.json`）と台帳自身が載せている m2 の jar 経由のコマンドの**両方**で確かめ、`grep -c 'と記述するとタブ文字'` ＝ 0、対照の `grep -c 'id が重複した場合は最初の1件のみ有効'` ＝ 1（ファイル取り違えでないことの担保）。**見出しを「スキーマ description が推奨する記法が壊れる」→「実制御文字で書いた区切り文字が失われる」へ書き換え、帰属を 3 者 → 2 者（yaml 側を落とす）へ直し、日付つきの訂正ブロックを添えた。converter 側の修正 `6c8d90e` は根拠が `notation:947-948` ／ `:1080` ／ `:1116` であって description ではないため無変更。**（2）`inventory.md` §0.1 の 4 辺の要素数が旧値 —— 実測で導き直して 33/21/45/31 ＝ **130**（旧 33/20/40/33/30 ＝ 126）とし、`8c327d0` なら旧値が再現することを日付つきで併記。（3）§3.1-2 のクラス別件数と「`null` 担保」の記述が §1-F の削除を反映していない —— 17 → **16**、担保の現在地を `TestDataContainerTest#名前がnullの読み込み単位は生成できない` へ書き換え、直接呼び出し 21 → **20**。（4）**同じ番人・同じメッセージを主張するテストが 2 本ある** 2 件を削除（`RecordLayoutTest#レコード種別を省略してもフィールド0件のレコードは生成できない` ／ `MessageDataBlockTest#本文レコードが0件の送信系電文ブロックも生成できない`）。**残した 1 本に「この軸では分岐しない」根拠と、分岐しないこと自体を担保する別テスト名を書き足した。** 実測は `Tests run: 595, Failures: 0, Errors: 0, Skipped: 2`（`inventory.md` **追補その 7** に 597 → 595 の内訳表）
- [x] Craft expert review（subagent, coding）**追加ステップの分を再実施する** → `54d2057`。**指摘 11 件・採用 8 件／不採用 3 件。** 採用はすべて **Javadoc・コメントだけの修正で、`src/main` の振る舞いは 1 行も変えていない**（ユーザー確定・2026-08-19「確定した判断とコメントが食い違ったら、振る舞いを変えない範囲で直してよい」）。主なものは —— `FieldDef` のクラス Javadoc と `@param length` が「検査は書き出し側が行う」と旧構造を書いていたのを `FileDataBlock`（FIXED）／`MessageDataBlock` ＋ `ModelPreconditions#requireLengths`（§6-J-3 `b762438`）へ、`XlsFormatWriter:255` のコメントを「`ColumnRowDataBlock` が生成時に拒否するためここへは届かない」へ、`DirectiveUtil` の「`null` のまま保持する」を「保持はされない —— `ModelPreconditions#requireNoNulls(String, Map)` で拒否する」へ、`ModelPreconditions:189` の `notation:883` の**引用が逐語でなかった**のを逐語へ、`YamlFormatWriter` の壊れた `{@link #emitRecords}` を実在する参照へ。**何にも紐づいていなかった孤児 Javadoc ブロック**（`EMPTY_BLOCK_MARKER_COLUMN` の手前に doc コメントが 2 つ続いていた）は `isMarkerColumn` の直前へ移した。**不採用 3 件はいずれも「振る舞いを変える」ため**（§6-K の判断と衝突する）—— （5）`IllegalArgumentException` と `IllegalStateException` の統一：明文が無く、`XlsFormatWriterTest:370` ／ `:452` の `@Test(expected = IllegalStateException.class)` を壊す。（7）例外メッセージのレコード番号を 0 始まり → 1 始まりへ：明文が無く、利用者に見えるメッセージを変える。（11）空文字のブック名を書き出し側で拒否する：**Decisions「番人は書き出し側でなく中間モデルの生成時に置く」に反する**うえ、**XLS-37 で「空文字は拒否しない」が確定済み**（新規 ID は立てず、`issues.md` XLS-37 に実測の補足を足した。下の台帳ステップ）
- [x] Verification expert review（subagent, test）**追加ステップの分を再実施する** → `fda3946`。**指摘 11 件・全件採用（いずれも台帳の記述と実物の食い違い）。** 直したのは —— `issues.md:147` が冒頭の内訳と別の数字（54／28）を持っていたのを**「冒頭の内訳を正とする」へ改め、同じ数字を 2 か所に書かない形にした**、判定欄書式検査のブロックの期待値 52 → **55** と、そこで数える対象を `grep -c '^### \(XLS\|YML\)-'` と明示（**素の `^### ` は課題以外の見出しまで拾い 89 を返す**）、番人の位置を示すコマンドを `grep -n 'throw new \(IllegalArgumentException\|IllegalStateException\)'` へ直し、`XlsFormatWriter.java:544-545` のような**行番号での参照をメソッド名（`XlsFormatWriter#marker`）へ**、`notation:206-241` → `notation:206`・`:212-235`。`steering.md` 側も現況ブロックを **55／25／27／2／1** へ、保留の一覧を XLS-41 → **XLS-42** へ、`RecordLayout` の番人 3 → **5**（`grep -c 'ModelPreconditions\.' … RecordLayout.java` ＝ 5）へ直した。**この巡で見つかった最大の学びは、素朴な `grep -c 対応不要` が 29 を返すこと** —— XLS-21 の本文が旧判定に言及しているためで、台帳自身の導出コマンド（`^- NTF 仕様としての判定.*\*\*対応不要\*\*`）は 27 で正しかった。**台帳を直すのでなく、まず台帳の導出コマンドを実行して確かめる**
- [x] **台帳と件数の反映（レビュー 2 巡目ぶん）**: Craft（11）の**空文字のブック名**を `issues.md` **XLS-37 の補足**として記録した（**新規 ID は立てない** —— XLS-37 が既に「空文字は拒否しない」を確定させており、同じ判断を 2 か所に持たせないため）。**使い捨てのプローブで実測**し、名前 `""` の器を辺③へ渡すと `<basePath>/.xlsx` が黙って作られること（出力 `PROBE-FILE: [.xlsx] size=3457`）を確かめてからプローブを削除した。**判定は変えない（対応不要）** —— `notation:44`（同名の 1 つの Excel ファイルがテストクラスに対応する）も `notation:53`（テストコードと同じ名前で配置することを**推奨**する）も命名を推奨として述べるにとどまり、**空文字を禁じる明文が無い**（判断の型 1「明文だけで判断する」）。**導出コマンドの実測（この巡のあと）**: 台帳は **全 55 件・要対応 25 ／ 対応不要 27 ／ 保留 2 ／ 本作業の対象外 1（区分外 0・二重 0）で変わらない**。テストは ①`595` ／ ②`@Ignore` 2 件（`YamlFormatReaderInvalidInputTest.java:740` YML-14・`:1280` XLS-40）／ ③`8c327d0: 536` ／ `HEAD: 595`

**Completion criteria**:

- 修正した課題それぞれについて、仕様どおりの期待値のテストが存在し、**修正前に赤になったこと（失敗メッセージ）が記録されている**（YML-03 の待機テスト 2 件は #25.5 で赤を記録済み。`checks/task-25.5.md` の実行出力）
- 修正した課題それぞれについて、現状挙動を固定していた既存テストが削除されている（同じ挙動を主張するテストが 2 本残っていない）
- 1 件 ＝ 1 コミットになっている（`@Ignore` 付与のコミットを含め、混ぜていない）
- ~~YML-03 の `@Ignore` テストが存在し、理由が `@Ignore` の引数に書かれている~~ → ~~**2026-08-18 に YML-03 を修正したため、`@Ignore` は 0 件であること**（`grep -rn '^    @Ignore' src/test --include=*.java` がヒット 0 件）に置き換えた~~ → **2026-08-19 の他責の型（ユーザー確定）により `@Ignore` は 2 件であること**（YML-14 ／ XLS-40 のカラム名側。どちらも**あるべき姿を主張するテスト**で、**どちらも `YamlFormatReaderInvalidInputTest` にある**。理由と他責先を `@Ignore` の引数に書く）。実測は `grep -rn '^    @Ignore' src/test --include=*.java` が `YamlFormatReaderInvalidInputTest.java:740`（YML-14）と `:1280`（XLS-40）の 2 件（**`:1277` → `:1280` はレビュー 2 巡目の追記で行がずれただけで、テストは同じ**）（**インデントを含めて数える**。素の `@Ignore` は `import` 行と Javadoc の `{@code @Ignore}` まで拾い 6 件返る）
- XLS-01 のテストが削除されておらず、アサートが「仕様外入力のため値は保証しない」旨へ書き直されている
- ~~`issues.md` の全 44 件に「NTF 仕様としての判定」欄があり、**要対応 15 件／対応不要 28 件／本作業の対象外 1 件**が出典つきで書かれている（2026-08-18 時点。導出コマンドと出力は `issues.md` 冒頭）~~ → **`issues.md` の全 55 件に判定欄があり、要対応 25 件／対応不要 27 件／保留 2 件／本作業の対象外 1 件が出典つきで書かれている**（2026-08-19 実測。区分外 0・二重 0。**「保留」は 2026-08-19 に足した区分で、明文が converter 側の判断で埋まらない課題に使う**）。既存の「判断」欄が残っており、両者の違いが説明されている
- YML-08 の「未確認」が実行結果で埋まっている
- `mvn clean test -Djacoco.skip=true` が全テスト PASS する（~~`@Ignore` は Skipped として現れてよい~~ → ~~YML-03 修正後は `Skipped: 0` であること~~ → **他責の型で置いた `@Ignore` 2 件の分だけ `Skipped: 2` であること**。2026-08-19 実測は `Tests run: 595, Failures: 0, Errors: 0, Skipped: 2`（レビュー 2 巡目で二重主張のテスト 2 件を削除したため 597 → 595。`inventory.md` 追補その 7））
- `src/main` の変更が要対応と判定した課題の修正に必要な範囲に限られており、変更したファイル名・課題ID・変更理由が記録されている
- ~~`FieldDef.type` ／ `MessageDataBlock.records` の契約が Javadoc に明記され、辺③（`XlsFormatWriter`）と辺④（`YamlFormatWriter`）の双方が `IllegalArgumentException` で弾く~~ → **契約が Javadoc に明記され、`FieldDef` ／ `MessageDataBlock` の生成時が `IllegalArgumentException` で弾く**（Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」・2026-08-19。`FieldDef.type` は §1-D `d0023c0`、`MessageDataBlock.records` は §6-J-2 `9e40644` でモデルへ寄せ、**辺③④のチェックは撤去した**。残すと空振りの緑になるため）。現状挙動を固定していたテストは置き換えられている（2 本残っていない）
- `issues.md` の `notation:nnn` が全件 `30a8271` 基準であり、基準コミットが本文に書かれている
- ~~`RecordLayout` コンストラクタに番人を置かない判断と、却下理由・実測が `issues.md` に残っている~~ → **この判断は 2026-08-19 の Decisions（生成時に拒否する）で覆った。** `RecordLayout` の生成時には現在 **5 つ**の番人がある（`grep -c 'ModelPreconditions\.' src/main/java/nablarch/test/tool/converter/model/RecordLayout.java` ＝ 5）—— フィールド定義リストの要素 `null`、`fields` 空 ＝ §6-I `c31b534`、フィールド名称の重複 ＝ §6-F `29c9d1d`、データ行リストの要素 `null`、行の要素数 ≦ `fields` 件数 ＝ §6-G の切り出し `166a199`。**旧判断と却下理由は `issues.md` に記録として残す**（消さない）
- 本体パーサがレコード 0 件を受け付ける事実が新規 ID で記録され、判定が「本作業の対象外・記録のみ」である
- 中間モデルの全クラス・全フィールドの点検結果が記録されている（該当が無ければ「無し」と明記）
- 課題 ID 単位の要対応／対応不要の実数が、そのまま実行できる導出コマンド付きで確定している

---

### #26: カバレッジ計測と未到達分岐の列挙

**Purpose**: 4辺の担当クラスの行・分岐カバレッジを JaCoCo で計測し、未到達分岐を列挙して、テスト不要と判断したものに根拠を付ける。

**Prerequisites**: #21, #23, #25

**Steps**:

- [x] Decisions 記載の手順（`mvn clean jacoco:instrument test jacoco:restore-instrumented-classes` → `mvn jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec`）でカバレッジを取得する
- [x] `XlsFormatReader` / `XlsFormatWriter` / `YamlFormatReader` / `YamlFormatWriter` / `TestCoreReaderAdapter` / 中間モデル各クラスの行・分岐カバレッジ数値を `.rn/ntf-test-data-converter/coverage/coverage-report.md` に記録する
- [x] 未到達の分岐を1件ずつ（クラス・メソッド・行番号つきで）列挙する
- [x] 各未到達分岐を「テストを足すべき」「テスト不要」に分類し、テスト不要には根拠を書く（Java イディオム／到達不能／NTF 仕様外 など）
- [x] self-check（OK/NG per completion criterion、checks/task-26.md に記録）
- [x] QA expert review（subagent）
- [x] Craft expert review（subagent, writing）
- [x] Verification expert review（subagent, fact-check）
- [x] **追加 1 巡（ユーザー確定・2026-08-21。範囲は A 群 4 件＋B 群 5 件の 9 件に限る）**: 3 巡目で未処置のまま残した Valid 18 件のうち、**A 群 4 件（事実誤り）＝ C3-1・C3-2・C3-8・QA-4** と **B 群 5 件（引用の逐語性）＝ V3-1・V3-3〜V3-6** を直した。**QA-1 は却下**（分類「テストを足すべき 19／テスト不要 15」は動かさない。却下理由を `coverage-report.md` §3.1 #6 の行に 1 文で残した）。**残る C 群 8 件（用語・体裁）＝ QA-2・C3-3〜C3-7・C3-9・C3-10 は #27 の対応表を書くときにまとめて整える。** **QA-3（`checks/task-26.md` が無い）は無効だが、原因である未追跡を本コミットで解消した**（3 巡とも同じ指摘が上がったのは、worktree で起動したレビュアに未追跡ファイルが見えないためである）

**Completion criteria**:

- `.rn/ntf-test-data-converter/coverage/coverage-report.md` に対象6区分すべての行・分岐カバレッジ数値が記録されている
- 未到達分岐がクラス・メソッド・行番号つきで漏れなく列挙されている
- 各未到達分岐が「テストを足すべき」「テスト不要」に分類され、後者には根拠が書かれている
- 「テストを足すべき」に分類されたものは、追加されたか `issues.md` へ残課題として記録されたかのいずれかになっている
- src/main への変更がゼロ

---

### #26.5: マーカーカラムのセル値を `[空]` から `[EMPTY]` へ改める

**Purpose**: 0 行テーブルを表す空ブロックマーカーのセル値を、英字大文字の `[EMPTY]` に改める。ユーザー確定（2026-08-20）。理由は ① マーカーカラムの命名は英字が慣習（解説書の例は `[no]`・`[desc]` ですべて英字小文字）② 検索性 ③ `[空]` は解説書で「そのセルが空である」の意味に別途使われ字面が衝突する ④ 人が書く印（`[no]`・`[desc]`）とツールが埋める印（`[EMPTY]`）を大文字で区別できる。

**Prerequisites**: #26

**Steps**:

- [x] 着手時に最新を採り直す（別セッションが並行して動いているため。申し送りの基準コミットは `c8ead78`）→ **`804329a` で採り直した。** `git fetch --all` 後 `git log --oneline HEAD..@{u}` は 0 件で、**別セッションの並行変更は入っていない**（`c8ead78..HEAD` の 7 コミットはいずれも本セッション系列の #25.5〜#26 ぶん）。**`[空]` の出現箇所を数える導出コマンド**（件数が後から動いたときはこれを再実行して確かめる。ユーザー指示・2026-08-21）:

  ```bash
  # <rev> を省くと作業ツリーを数える。時点の値を確かめたいときは <rev> を必ず付ける
  git grep -c -F '[空]' <rev> -- .                            # ファイル別の件数
  git grep -o -F '[空]' <rev> -- . | wc -l                    # 総数
  git grep -c -F '[空]' <rev> -- src/                         # Completion criteria の判定に使う範囲
  find src -name '*.xls*' -exec sh -c \
    'unzip -p "$1" | grep -qa "\[空\]" && echo "HIT: $1"' _ {} \;   # バイナリのフィクスチャに埋まっていないか
  ```

  **`804329a` 時点の実測: 全 19 件・6 ファイル**（`<rev>` ＝ `804329a` で再現する）。内訳は `src/` **6 件**（`xls/XlsFormatWriter.java` 1／`xls/XlsFormatWriterTest.java` 4／`SampleConversionTest.java` 1）と `.rn/` **13 件**（`steering.md` 6／`coverage/issues.md` 6／`coverage/inventory.md` 1）。`checks/` は 0 件。`.xls*` のフィクスチャ（1 本）を展開しての走査も 0 件で、**バイナリに埋まった `[空]` は無い**

  **`<rev>` を省いた作業ツリーの値は、上の 19 件とは一致しない。** この記録と導出コマンドを `steering.md` へ書いたこと自体が `[空]` の出現を増やしており、以後も本タスクの記述が動けば増減する（レビュー指摘・2026-08-21）。**判定に使うのは `-- src/` の件数と `coverage/` 側の内訳であって、`steering.md` を含む総数ではない。** 時点どうしを比べるときは必ず `<rev>` を付けて数えること
- [x] `.rn/` 側の **現行の正を述べている記述**を `[EMPTY]` へ揃える（標準・ユーザー確定 2026-08-13「定義を変えたら現行の正を保持する文書は指示に無くても揃える」）。**分ける基準は「機構の説明か、観測した現物の記録か」である** —— いま converter が何を書き出すかを述べた文は、日付が入っていても**揃える**。当時観測した版面・出力そのものの記録は**値を残し**、現行値の在り処を指す 1 行を添える（件数は書かない）。
  - **揃える**: `coverage/inventory.md` 追補その 6 の軸表 `C-08 columnNames 空` 行／`coverage/issues.md` の ①冒頭「要対応 25 件の内訳」の XLS-27 の項 ②同じく冒頭の番人 7 つの決着表の `columnNames` 0 件 行 ③XLS-08 の末尾（「もう成り立たない」の直後）④XLS-27【決着】の「改修」箇条／**`steering.md` Decisions の訂正ブロック 2 つ**（「未完 1 件…は取り消した」と「~~XLS-27 の番人は…~~」節の冒頭）
  - **揃えない**: `coverage/issues.md` の XLS-27 プローブ実測（2026-08-19）の版面記録／`steering.md` の完了済みステップの時点記録・本タスク自身の表題と理由・本ステップの実測記録と導出コマンド
  - **`steering.md` Decisions の 2 つは、当初「引用ブロックだから揃えない」と分類していた。** 実物を開かず `grep` の 1 行だけで判断したのが誤りで、あれは逐語の引用ではなく**現在形で機構を述べた訂正ブロック**であり、しかも標準が「揃える」と定める Decisions の中にある。QA・Craft の 2 者が独立に指摘し、coordinator が実物を読んで追認した（2026-08-21）。**日付を裏切らないよう、値を差し替えたうえで「当時の値は `[空]`」を括弧で添える形にした**
- [x] `XlsFormatWriter.java` の定数 `EMPTY_BLOCK_MARKER_COLUMN` の**値だけ**を `[EMPTY]` に変える（**定数名は変えない**。`{@value}` を使う Javadoc と定数参照箇所には手を入れない）→ `becbe30`。`:543` の 1 行のみ。総行数 601・定数名・参照 3 箇所（`:213`／`:252`／`:543`）はいずれも不変
- [x] `XlsFormatWriterTest.java`・`SampleConversionTest.java` の期待値を追随させる → `becbe30`。**テスト先行で RED を実測してから実装した**（`XlsFormatWriterTest` の期待値だけ先に変え、`Expected: is "[EMPTY]" but: was "[空]"` で 2 件 FAIL を確認）。`mvn clean test -Djacoco.skip=true` は `Tests run: 595, Failures: 0, Errors: 0, Skipped: 2`（**595 は本変更による目減りではない**。597 → 595 は `6114c35` の二重主張テスト 2 件削除によるもので、`0e234e1..HEAD` に `@Test` の増減は無い）
- [x] self-check（OK/NG per completion criterion、checks/task-26.5.md に記録）→ **6 件すべて OK**
- [x] QA expert review（subagent）→ **pass（6/6）。指摘 5 件。** `HeaderLine` のマーカー判定が `startsWith("[") && endsWith("]")` のみで値に依存しないことをソースで確かめ、本変更が本体パーサに対して振る舞い中立であることを裏づけた。F1（件数記録の自己無効化）は `3807b6a`、F2（Steps の締めと `checks/` の追跡）は本コミットで解消。F3〜F5 は持ち越し（下記）
- [x] Craft expert review（subagent, coding）→ **pass（6/6）。指摘 5 件・採用 4 件／不採用 0 件・観測 1 件を採用。** A（self-check の Evidence が走査範囲を狭めて再現しない）は `checks/task-26.5.md`、C・D（ポインタの折り返しと版面図への注釈）は `d00da17`、B（件数記録の自己無効化）と E（`steering.md` の 2 件が同じ扱いを受けていない）は `3807b6a` で処置した
- [x] Verification expert review（subagent, test）→ **pass（6/6）。変異試験で担保が二層であることを実証した** —— 定数を `[空]` へ戻すと 2 件、括弧なしの `EMPTY` にすると 4 件が落ちる（build テストが綴りを、往復テストが「角括弧で囲まれていること」を固定している）。**押さえられていない経路 5 件を指摘**（下記の持ち越し）

**Completion criteria**:

- `grep -rn "\[空\]" src/` が 0 件
- 既存テストが全件成功（`XlsFormatWriterTest#roundTripsZeroRowTableWithoutEatingNextBlock` を含む）
- `EMPTY_BLOCK_MARKER_COLUMN` を参照する箇所が `XlsFormatWriter` の 3 箇所のままであること
- 定数名 `EMPTY_BLOCK_MARKER_COLUMN` が変わっていないこと
- `XlsFormatWriter.java` の総行数が変わっていないこと（`coverage/coverage-report.md` が同ファイルの行番号を `da66425` 時点で引用しているため）
- `.rn/` 側で現行の正を述べている記述が `[EMPTY]` になっており、`[空]` が残るのは観測した現物の記録・完了済みステップの時点記録・本タスク自身の記述だけであること（上の導出コマンドの出力を 1 件ずつ開いて確かめる。`-- src/` は 0 件）

**#27 へ持ち越す担保の穴 8 件（3 者のレビューで出たもの。ユーザー確定・2026-08-21）**: いずれも**本変更が持ち込んだ欠陥ではなく既存の穴**である（変異試験で二層の担保が健全であることが実証されたため）。**#26.5 で担保が二層（値の literal 2 件・機構の往復 4 件）あることを実測で確かめたうえで、なお埋まっていない穴である**——「未検証だから穴」ではない（#27 で読む者が誤読しないよう、持ち越し先にもこの 1 文を添えること。ユーザー指示・2026-08-21）。① 実 `.xlsx` を通る唯一の経路 `SampleConversionTest#convertsClimanSampleIncludingZeroRowTable` がマーカーを検証していない（ブック数とファイル存在だけを見ている。coordinator が実物で確認済み） ② `EXPECTED_TABLE` の 0 件往復テストが無い ③ 0 件テーブルが唯一・末尾のブロックの往復テストが無い ④ `columnNames=[]` かつ「セルを持たない行」を N 件持つ形（XLS-08 ／ YML-04）の往復テストが無い ⑤ 実カラム名が `[EMPTY]` と衝突する形の明示テストが無い ⑥ DB 実行経路（`TableData#replaceData`）の再実測が無い ⑦ 2026-08-19 プローブの (2)(4) は `[空]` での実測であり `[EMPTY]` で再実測していない ⑧ 命名規約そのもの（ASCII の角括弧トークンであること）を固定するテストが無い。

**2 問への回答（ユーザー確定・2026-08-21）**: **① 命名規約を固定するテストは足さない。⑧ として #27 へ回す。** 根拠は明文である —— `testdata_notation.rst:1515`（`30a8271`）が定めているのは「カラム名を半角角括弧 `[ ]` で囲むとマーカーカラムになる」ことだけで、**`EMPTY` という語は明文が定めたものではなく converter の選択である**。明文に根拠を持たない語を「規約」として固定するテストは、型 1（明文だけで判断する）に反する。固定すべき角括弧の側は往復 4 件が本体パーサ経由で押さえており、値も `XlsFormatWriterTest.java:415`／`:442` が定数参照ではなく `is("[EMPTY]")` の直書きなので定数変更で落ちる。**テスト件数は 595 のまま動かない。** **② 担保の穴 8 件は #27 へ持ち越す（上のとおり）。`issues.md` の申し送り節の陳腐化は持ち越さず #26.5 の中で直した。** 直した理由は 2 つ —— (a) 陳腐化は挙げた 2 文ではなく節全体に及んでおり、2 文だけ直すと残りの偽が「直した節」の顔をして残る。(b) **この節は解説書担当・対象 PJ へそのまま渡す文面であり、外へ渡る文面に既知の偽を残さない。** **処置**: 旧文面（`57c1b0d` の番人による制約の申し送り）を全文撤回し、`issues.md`「0 件テーブルの申し送り（XLS-27。対象 PJ・解説書担当 宛）」へ書き替えた。**伝達は必要と判定した** —— 変換が止まらなくなった代わりに「0 件テーブルは往復でカラム名が復元されない」という利用上の事実が残るためである（Excel の 0 件テーブルはカラム名の行を持つ〈`notation:789`／`:802`〉が、YAML はカラム名を `rows:` の先頭要素のキーで決めるため〈`:819`〉`rows: []`〈`:836`〉には書く場所が無く、`YamlFormatWriter#emitMapRows:256-257` が行 0 件なら `rows: []` だけを書いてカラム名を捨てる。戻すと `XlsFormatWriter#layoutColumnRow:252` が `[EMPTY]` 1 列を置く）。**旧文面が引いていた例外メッセージは `src/` に 1 件も無いことを実測で確かめた**（`grep -rn 'カラム名を 1 件も持たないブロックは書き出せません' src/` → 0 件。`ColumnRowDataBlock:93` に残るのは別の規則である）。旧文面は `839bf64` 以前の履歴で読める。

**担当外（報告に含めること）**: `nablarch-testing` 側 `docs/pr75/docs/ntf-empty-table-assertion.md` の「未決」記述の更新は**別リポジトリであり、#21〜#23 を進めている CC の担当**。本セッションは手を出さない。

---

### #27: 4辺の軸×要素対応表と課題一覧の提出

**Purpose**: 4辺ぶんの軸×要素対応表を、各要素に担保テストメソッド名を記した形で完成させ、本作業で見つかった課題を一覧として確定する。

**Prerequisites**: #26

**Steps**:

- [ ] `.rn/ntf-test-data-converter/coverage/axis-matrix.md` に4辺ぶんの軸×要素対応表を作る。各要素に担保テストメソッド名を記す
- [ ] 空欄が残る要素には理由を書く
- [ ] 表に記したテストメソッド名が実在することを、テストソースと突き合わせて確認する
- [ ] `issues.md` を通読し、#19〜#26 で記録した課題が漏れなく載っていること・**#25.5 で修正した課題（判定が要対応であるもの）を除き**修正されていないことを確認する
- [ ] **#26 からの持ち越し 8 件（用語・体裁）を対応表を書くときにまとめて整える**（ユーザー確定・2026-08-21）: QA-2・C3-3〜C3-7・C3-9・C3-10。**内容の正は `checks/task-26.md`「追加 1 巡」節**
- [ ] **#26.5 からの持ち越し 8 件（担保の穴）を軸マトリクスの空欄の理由として書く**（ユーザー確定・2026-08-21）: ①〜⑧ の内容は #26.5 の「#27 へ持ち越す担保の穴 8 件」が正。**持ち越し先にも「#26.5 で担保が二層（値の literal 2 件・機構の往復 4 件）あることを実測で確かめたうえで、なお埋まっていない穴である」と 1 文添える**——「未検証だから穴」と誤読させないため（ユーザー指示・2026-08-21）
- [ ] self-check（OK/NG per completion criterion、checks/task-27.md に記録）
- [ ] QA expert review（subagent）
- [ ] Craft expert review（subagent, writing）
- [ ] Verification expert review（subagent, fact-check）

**Completion criteria**:

- `axis-matrix.md` に辺①〜辺④の4表があり、軸A〜F の全要素が行として存在する
- 各要素に担保テストメソッド名が記されており、記された全メソッド名がテストソースに実在する
- 空欄の要素には理由が書かれている
- `issues.md` に本作業で見つかった課題が一覧化されており、`git diff` 上 src/main への変更が **#25.5 で修正した課題（`issues.md` の判定が要対応であるもの）に限られている**ことが確認されている

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

- **Status**: paused
- **Date**: 2026-08-21
- **Last completed**: #26.5 マーカーカラムのセル値を `[EMPTY]` へ改める（`0672a16`）
- **Next**: #27 4辺の軸×要素対応表と課題一覧の提出
- **Notes**: branch `ntf-test-data-converter`（push 済み・ローカル差分なし・未追跡パス無し）。**次は #27 の Steps 1**（`coverage/axis-matrix.md` を新規に作る）。**#27 の Steps には持ち越し 2 群が入っている** —— #26 からの 8 件（用語・体裁。内容の正は `checks/task-26.md`「追加 1 巡」節）と、#26.5 からの 8 件（担保の穴。内容の正は #26.5 の「#27 へ持ち越す担保の穴 8 件」）。後者は**添える 1 文が指定されている**ので Steps の本文を読むこと。**未解決のブロッカー・ユーザー回答待ちは無い**（#26.5 の 2 問は `0672a16` で処置済み）。**JaCoCo の再計測はしない**（Rules に記載）。
