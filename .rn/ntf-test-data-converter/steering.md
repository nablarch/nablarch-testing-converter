Rn version: 0.8.0

# Goal

nablarch-testing（ブランチ `convert-testdata-excel-to-text`）の変換ツール（src/main 28件）と形式間変換テスト（src/test 21件）を nablarch-testing-converter リポジトリへ切り出し、`mvn test` 全 PASS・実装無改変を確認する。

（フェーズ2）切り出した converter の4つの変換辺（① Excel→中間モデル／② YAML→中間モデル／③ 中間モデル→Excel／④ 中間モデル→YAML）の変換ルールを、converter 自身のユニットテストで担保する。担保の網羅は主観で判断せず、6軸（A データタイプ／B ブロック実装／C 中間モデル全フィールド／D 値の表現／E 多重度／F 異常系）を4辺それぞれについて埋め、軸×要素対応表とカバレッジ計測で客観的に示す。

# Acceptance criteria

- converter の `mvn test` が全テスト PASS する
- 全移動ファイルが**移動元 `1035207`**（本体 `nablarch-testing` の `convert-testdata-excel-to-text@{70}`。`103520709cf6ddeec6da7f901f2b4a5aacbffdef`・`2026-06-23 17:14:02 +0900`。**現 HEAD `06a73f8` からは到達不能**で reflog 経由で健在）と package/import を除いて完全一致（実装無改変）。**「本体現ブランチ」という書き方を基準 SHA へ改めた**（2026-08-21。ブランチ名では移動元へ辿り着けなくなり、#28 で照合が再現できなかったため。条件そのものは変えていない）。照合結果は `checks/task-28.md`（28 件 / NG 0 件）。
  - **照合の射程 —— 照合は移動時点 `2a069bc` の内容に対して行う。**converter 側の基準は `2a069bc`（`feat: complete task #2 — copy src/main converter (28 files)`）である。**移動後の `src/main` の変更（#4〜#18 のプラグイン化・#25.5・#26.5）は本項の対象外であり、その妥当性は別項（`issues.md` の判定欄に基づく変更限定）が担保する。**移動後の変更量は `2a069bc..HEAD` で 79 コミット、うち #18 まで（`5bf7048`）で 40 コミット・33 ファイル・+2514／-524 行である（`git rev-list --count 2a069bc..5bf7048 -- src/main` → 40、`git diff --stat 2a069bc 5bf7048 -- src/main` → `33 files changed, 2514 insertions(+), 524 deletions(-)`。2026-08-21 実測）。**これは条件を緩めたものではなく、#28 で実際に検証した範囲を書いたものである。**
  - **上の「対象外」に挙げた 3 つは移動後変更の内訳であって、本項の除外の全体ではない。**このうち #25.5（`issues.md` の判定欄が要対応とした src/main の修正。ユーザー確定・2026-08-14）と #26.5（`XlsFormatWriter.java`。`becbe30`。XLS-27 のマーカーカラムのセル値を `[空]` から `[EMPTY]` へ改めたもの。ユーザー確定・2026-08-20）は、**それ自体が src/main を触るタスクとして個別に承認されている**。#26.5 の 1 件だけが #25.5 の外にある。除外したファイル名・課題ID・変更理由は `checks/task-25.5.md`・`checks/task-26.5.md` に併記する
- pom.xml が yaml・本体・poi 3.8・snakeyaml 3.0.1 の依存を正しく持つ
- 本体（nablarch-testing）・yaml（nablarch-testing-yaml）に一切書き込んでいない
- ブランチが push 済み

（フェーズ2）

- 辺①に実 `.xlsx` を入力とするテストが存在し、軸A（`DataType` 14種）すべてが実ファイル経由で1回以上通っている
- 4辺それぞれで、軸B（`TestDataBlock` sealed 階層 4種）と軸C（中間モデル全フィールド。省略可能なフィールドは「値あり」「省略」の双方）が非デフォルト値で1回以上 IN／OUT されている。**固定長・可変長の双方を通す**（#29 より前は「`FileDataBlock.fileType` は `FIXED`／`VARIABLE` の両方を通す」と書いていた。**#29 で `fileType` はフィールドではなくなり `DataType` からの導出値になったため、軸C の C-10 は欠番になり、この要求はファイル系 4 種の `DataType`（軸A の A-06〜A-09）が担う**。`issues.md` XLS-44。**要求そのものは変えていない**）
- 軸D が4辺すべてでアサートされている（辺① セル種別8ケース／辺③ セル型8ケース（`getCellType()` をアサート）／辺② スキーマから導出したスカラー12ケース／辺④ YAML 表現9ケース）。ケース数の根拠は Decisions「軸D の対象範囲」
- 4辺それぞれで軸E（0件／1件／複数件）と軸F（異常系）が埋まっている
- 参照フィクスチャとして同梱した実物 `.xlsx`（Excel 保存物）1本と、POI 生成フィクスチャの読み取り結果が同一であることが確認されている（確認できない場合は差分が `issues.md` に「未確認」として記録されている）
- 4辺ぶんの軸×要素対応表が成果物として存在し、各要素に担保テストメソッド名が記されている。空欄には理由が書かれている
- 4辺の担当クラス（`XlsFormatReader` / `XlsFormatWriter` / `YamlFormatReader` / `YamlFormatWriter` / `TestCoreReaderAdapter` / 中間モデル各クラス）の行・分岐カバレッジが計測され、未到達分岐が列挙されている。テスト不要と判断したものには根拠が書かれている
- 本作業で見つかった現状挙動の課題が課題一覧へ記録されている。**#25.5 で要対応と判定したものは修正され、それ以外は修正されずに記録のみである**（ユーザー確定・2026-08-14 の方針変更に合わせて改訂。改訂前は「修正されずに」が全件に掛かっていた。**件数は 5 → 6 → 7 → 15 と動いたため、件数ではなく `issues.md` の判定欄を正とする。2026-08-18 時点で要対応 15 件**。**#27 で XLS-44 を要対応として起票した。これは #25.5 の対象ではなく、#28 の承認後に独立タスク #29 として実施した**（マージ前に片付ける——ユーザー確定・2026-08-21）。**#27 で対応不要として起票した XLS-45 も、2026-08-24 に判定が要対応へ変わり独立タスク #31 として実施した**（NTF 仕様として可変長ファイルでは `length` を書けないことが確定したため。ユーザー確定）。したがって本項の「修正され」は **#25.5 の時点で要対応と判定したもの**に掛かる）
- `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` が全テスト PASS する

# Assumptions

- 全リポジトリは同じ親ディレクトリに clone 済み
- nablarch-testing-yaml は構築・公開済み（converter のビルドに必要）
- **移動元は本体 `nablarch-testing` の `convert-testdata-excel-to-text@{70}` ＝ `1035207`（`103520709cf6ddeec6da7f901f2b4a5aacbffdef`・`2026-06-23 17:14:02 +0900`）である。**同ブランチの履歴はその後 `@{69}`（`d5bd33f`）・`@{68}`（`reset: moving to origin/develop`）で作り直されており、**`1035207` は現 HEAD（`06a73f8`）から到達できない**（`git -C ~/work/nablarch/nablarch-testing merge-base --is-ancestor 1035207 HEAD` が非 0）。オブジェクトは reflog 経由で健在である（`git -C ~/work/nablarch/nablarch-testing reflog refs/heads/convert-testdata-excel-to-text`）。照合結果は `checks/task-28.md`。
  - **照合の射程は移動時点である。** converter 側の基準は `2a069bc`。**移動後の `src/main` の変更（#4〜#18 のプラグイン化・#25.5・#26.5）は照合の対象外であり、その妥当性は `issues.md` の判定欄に基づく変更限定が担保する。**移動後は `2a069bc..HEAD` で 79 コミット、うち #18 まで（`5bf7048`）で 40 コミット・33 ファイル・+2514／-524 行が動いている（2026-08-21 実測。出典コマンドは Acceptance criteria の当該項）。本体側の `src/test` も対象外で、射程は `src/main` の converter 28 件のみである。
  - **`1035207` の退避はしない**（ユーザー確定・2026-08-21）。救出用のタグ・ブランチは本体に作らない。照合は `checks/task-28.md` に再現用スクリプトごと記録済みであり、**再照合が必要になる場面は想定しない**。（reflog の既定保持は到達不能コミットで 30 日であり、gc が走れば `1035207` は消える。それを承知のうえでの判断である。）
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
2026-08-19（`73297e2`。旧判定の根拠が事実誤りだったため。`issues.md` XLS-20 の【判定の訂正】） → **#25.5 §6 の中間モデル一巡点検で XLS-34〜38・XLS-40・XLS-41・XLS-43 が加わり、2026-08-19 時点で 24 件**）。**上の列挙は時点の記録であって正ではない。正は `issues.md` の判定欄であり、実数は `issues.md` 冒頭の導出コマンドから導くこと**（列挙を範囲の定義と読むと、あとから要対応になった課題が範囲外に読めてしまう。同じ取り残しが `7200b0f` のときに実際に起きた）。記録先は `.rn/ntf-test-data-converter/coverage/issues.md`。修正対象の判定根拠と手順は Decisions「不具合修正の対象と手順（#25.5）」。**この規定は #25.5 の作業範囲を定めるものであり、#25.5 より後に立った要対応には掛からない**（ユーザー確定・2026-08-21）。**#27 で起票した XLS-44 は要対応であり、#28 の承認後に独立タスク #29 として実施した**（#27 の完了条件が `src/main` 無変更を求めるため #27 では起票のみとした。マージ前に片付ける——ユーザー確定・2026-08-21）
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
- **#30 以降、レビュア subagent は回さない**（ユーザー確定・2026-08-24）。差分は調整側（ユーザー）が全行読む。**役割が重複しているためであり、レビューで指摘が出ていたからではない。**代わりに完了報告へ次の 2 つを必ず付ける —— (1) `git diff --stat <前タスクの完了コミット>..HEAD -- src/`（`src/` に触れていない場合はその旨）／(2) `mvn clean test` の最終行（`Tests run: … Failures: … Errors: … Skipped: …` と `BUILD SUCCESS`）。**`mvn` はユーザー側では走らせない**（`target/` が壊れて converter 側の実行と衝突するため）。ゲートの結果は報告に依存するので、**PASS していないものを PASS と書かない。落ちたら落ちたまま報告する。**変わらないもの: 赤→緑の順序／1 件 1 コミット／`checks/{task-id}.md` の記録／完了時の `/rn:ty` 判定要求
- **（#29 まで）レビュア subagent は `isolation: worktree` で起動する**（#23 で確立・2026-08-13）。レビュア には `checks/{task-id}.md`（自己点検）を渡さない・読ませない
- **順序を主張するテストは、フィクスチャを最初から定義順・辞書順とずらして作る**（#24 の教訓・ユーザー確定・2026-08-14）。#24 で 3 巡かけて出た生存変異 9 件は、共通原因が「順序を主張する入力が辞書順・定義順と一致していた」ことだった。一致していると、順序を壊す変異を入れてもテストが通ってしまい、アサートが順序を担保していない。これは辺②に固有の話ではないため、#25 以降でフィクスチャを作るときは書く時点でずらす。3 巡かけて見つけるより書くときに外すほうが安い
- **State をプレースホルダへ戻すときに失われる記録は、戻す前に Steps か Rules へ移す**（ユーザー確定・2026-08-21。以後の既定）。`/rn:up` の State リセットで消えるのは「次に何をするか」だけであるべきで、制約（例: JaCoCo を再計測しない）や持ち越し事項が State にしか無い状態でリセットすると復元できない
- **作業対象と無関係なファイルの変更は、独立した `chore:` コミットに分ける**（ユーザー確定・2026-08-14）。`.gitignore` への `.claude/worktrees/` 追加が `c15d531`（辺②のレビュー反映コミット）に相乗りしていた。履歴は書き換えないが、以降は分ける
- **申し送りの束（XLS-27・XLS-39・XLS-40・XLS-42 の 4 件）はまだ出さない**（ユーザー指示・2026-08-24）。出す判断は調整側（ユーザー）でする。**XLS-45 は #31 で束から外した** —— 「可変長フィールドの `length` をどう扱うか」は問いでなくなり（禁止で確定・ユーザー確定 2026-08-24）、スキーマと解説書の対応は別途依頼済みであるため（`issues.md`「可変長フィールドの `length` は禁止で確定（XLS-45。2026-08-24）」の節）。
- **マージ可否の判断は出さない。完了報告に「要対応 0 件」と書かない**（ユーザー確定・2026-08-24。**#31 完了後も同じ**）。予定されていた要対応 1 件は **#31（XLS-45）として受領し、実施した**。判断は調整側（ユーザー）が出す。
- **逸脱の追認（2026-08-24）**: #30 で逸脱として報告した 2 件 —— (1) `inventory.md` の件数をコマンドから導き直した（Rules の #22 規定）／(2) `TestDataBlock` の Javadoc を訂正した —— は**いずれも Rules に従った結果であり、直さないほうが誤りだった**（ユーザー追認・2026-08-24）。同種の是正は以後も逸脱として上げなくてよい。
- **移動・複製の照合をしたら、相手側の SHA を必ず記録する**（ユーザー確定・2026-08-21）。#2 の QA は「全 28 件を source ブランチと diff して全件ゼロ」とだけ記録し、**基準 SHA を残さなかった**（`checks/task-2.md`）。その後 upstream 側で同じブランチの履歴が作り直されたため、#28 で照合を再現しようとした時点で「ブランチ名」からは移動元へ辿り着けず、**別系統の `d5ec1d0` を相手に取って誤った差分を記録した**（`67a8780`）。ブランチ名・タグ名は動く。**動かないのは SHA だけである。**
- **他リポジトリの挙動に言及するときは、その rev の実物を開いて確かめた出典を必ず添える**（ユーザー確定・2026-08-24）。末端まで追わずに将来の挙動を断定しない。#31 で `YamlSchemaValidationException` を `IllegalArgumentException` 系と暗に置いて「スキーマ側の対応が入っても本テストは緑のまま」と書いたが、実物は `IllegalStateException` を継承していた（nablarch-testing-yaml の `src/main/java/nablarch/test/core/reader/yaml/YamlSchemaValidationException.java:12`（`a5cb6dd` 時点））。#30 でも同種のものがあった。**出典は「ファイル:行（rev 時点）」の形で書く。**
- **持ち越しの未決 2 件（#31 から。State をリセットしても消さない）** —— (1) `handover.md`（解説書・スキーマ担当宛の申し送り）の**提出タイミングは調整側（ユーザー）の判断**であり、こちらからは出さない。(2) **台帳の宣言値のずれ 2 件の出所が未確認**（`checks/task-31.md`「台帳の宣言値のずれ」）。どちらも Step 4（#32〜#39）の作業範囲外であり、Step 4 の完了条件にも入らない
- **#34（2-3）の実装方針はユーザー判定済み（2026-08-27）** —— `[ ]` を知ってよいのは 2 層（A Excel 版面の読み書き／B 上流 API 境界）だけ。整形はリーダーではなく `TestCoreReaderAdapter`・`YamlTestCoreAdapter` の中に置く。正は指示書 `nablarch-document@0d9a049` の 2-3。判定の経緯は `checks/step4-report.md` §1-2 (b)

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

**注記**（2026-08-21 追記）: 以下 #2・#3・#4・#5 の本文にある「本体現ブランチ」13 箇所は、移動元 `1035207`（`convert-testdata-excel-to-text@{70}`）を指す。現 HEAD `06a73f8` からは到達不能である。正は Acceptance criteria の実装無改変の項。

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

- [x] `.rn/ntf-test-data-converter/coverage/axis-matrix.md` に4辺ぶんの軸×要素対応表を作る。各要素に担保テストメソッド名を記す → **`e977824`（新規）→ `1c3253b`（1 巡目修正）→ `832a700`（2 巡目修正）。315 行（辺① 79／辺② 82／辺③ 77／辺④ 77）**
- [x] 空欄が残る要素には理由を書く → **空欄（`—`）27 件すべてに理由と根拠テストを付けた。分類は §5.3、`—` の裏に隠れる穴は §5.4**
- [x] **表の全セルについて、そのテスト本文が当該セルの主張する要素を実際にアサートしていることを確認する**（ユーザー確定・2026-08-21。改訂前は「表に記したテストメソッド名が実在することを、テストソースと突き合わせて確認する」だった。**実在の確認は、そのメソッドが当該要素を実際にアサートしていることを示さない。** 1 巡目で同型の 1 件だけを直して水平展開しなかったことが発端である） → **名前の実在照合は済み**（§0.6 の照合コマンド。抽出 315／照合対象 288／NG 0。略記 `#メソッド名` とテストヘルパも解決して照合する。3 者のレビュアが独立に照合して NG 0 を再現済み）。**本文の照合は軸E の 44 セルだけ済み**（`832a700`。❌ 2 件が出て `783810b` ／ `6d12021` で埋めた）。**残る 246 セル（軸A・B・C・D・F の ✅ セル）は下の水平展開で行う** → **完了・2026-08-21。**水平展開で全セルを点検し終え、そこで出た ❌ 6 件の扱いもユーザー確定（2026-08-21）で決着した—— 辺② C-10 はテストを足して ✅ へ戻し（`11df931` ／ `9ed87c3`）、辺④ A-06〜A-09 は原因を `issues.md` **XLS-44** として起票して 4 行から参照させ（`90d68a3` ／ `4d68e78`）、辺③ C-21(省略) は ❌ ＋ 開示のまま据え置いたうえで辺③の値落ちを `issues.md` **XLS-45** として起票した（`78e513f`）。**現在の集計は ✅ 283 ／ ❌ 5 ／ 空欄 27 ／ 合計 315**（§5.2）
- [x] **（#27 で追加・ユーザー確定 2026-08-21）辺② C-10 を埋めるテストを足す**: 手段は既存 `YamlFormatReaderRealFileTest#readsInjectedDirectivesEvenWhenDirectivesAreOmittedInVariableFile` への 1 行追加（`assertThat(block.getFileType(), is(FileDataBlock.FileType.VARIABLE))`）。実 `.yaml` で `type: "variable"` を読ませるフィクスチャが既にあるため新規フィクスチャは要らない。**Javadoc の「担保する軸要素」に C-10(VARIABLE) を加えること。** 足したうえで `axis-matrix.md` 辺② C-10 を ✅ へ戻し、C-10 の根拠として置いてある導出コマンド（現在は「出力は 8 行」と書いてある ❌ の根拠）を、担保の所在を示す形へ導き直す → **`11df931`（テスト）／ `9ed87c3`（台帳）。** アサートが効いていることは、フィクスチャを `type: "variable"` → `"fixed"` に変えて `Expected: is <VARIABLE> but: was <FIXED>` で落ちることを実測して確かめた（`length` も足した —— 可変長のままだと `ModelPreconditions#requireLengths` が先に投げてアサートまで届かないため）。導出コマンドの出力は 9 行になり、実ファイル経路の 2 行が FIXED と VARIABLE を 1 行ずつ見ている。**あわせて §0.6 末尾の `src/main` 実在照合コマンドを直した**（`56d458a`）—— 「出力が無いことが確認結果である」と書いてあるのに `NG(class)` を 3 行返す状態で、Rules「台帳に載せる出典コマンドは、そのまま実行して同じ結果が出ること」に反していた。同じ箇所の「`src/main` の 25 件」と「この 27 件」の矛盾も実測（27）へそろえた
- [x] **（#27 で追加・ユーザー確定 2026-08-21）辺④ A-06〜A-09 の原因を中間モデルの課題として新規 ID で `issues.md` へ起票する**: 事実は、NTF 仕様がファイル種別を**ひとつの概念としてしか持たない**（本体スキーマ `$defs.file_data.properties.type` の description が「fixed = 固定長（SETUP_FIXED / EXPECTED_FIXED）、variable = 可変長（SETUP_VARIABLE / EXPECTED_VARIABLE）」と 1 対 1 で定める）のに対し、`FileDataBlock` はこれを `DataType` と `fileType` の 2 フィールドで持ち、コンストラクタが `fileType` の `null` と `DataType` の 4 種限定は検査する一方で**両者が食い違う組み合わせを検査していない**こと。`new FileDataBlock(SETUP_FIXED, ..., FileType.VARIABLE, ...)` が作れ、これは NTF 仕様として表現できない状態である。あわせて `ModelPreconditions#requireLengths` が `fileType == FIXED` でしか走らないため、この食い違いは既存の不変条件も素通りする。**あるべき姿は `fileType` を `DataType` から導出し、重複そのものを無くすこと**（4 種の `DataType` と `FileType` は全単射なので導出できる）。整合検査を足すより望ましく、判断型②（不正な状態は中間モデルの生成時に拒否する）そのものである。これが済めば辺④の `type:` が `DataType` から出るため A-06〜A-09 は ✅ になり、軸C から C-10 という行自体が消える。**#27 の中では直さない**（#27 の完了条件が src/main 無改変を求めるため）。起票では事実・出典・あるべき姿・影響範囲（4 辺すべての `FileDataBlock` 生成箇所）を書く。判定欄は NTF 仕様から自分で決めてよい。**実施は #28 の後に独立タスクとして立てる。** `axis-matrix.md` の 4 セルは ❌ ＋ 現在の理由のままとし、理由欄の末尾に起票した課題 ID を添える。**選択肢 (3)（4 行の主張を 2 対 1 の写像へ書き改める）は採らない** —— 表の主張を実装の出力に合わせて書き換えることになり、実装から判断することになるため → **`90d68a3`（起票）／ `ce91b86`（出典の是正）／ `23427ea`・`4d68e78`（対応表への反映）。****ID は XLS-44。判定は要対応（未実施）。** 4 セルは ❌ のままで、理由欄の末尾に `（原因は `issues.md` XLS-44）` を添えた。§4.1 末尾の 2 択は 1 本にし、(3) を採らないことと理由を独立した段に書いた。**プロンプトに載せた「4 種の `DataType` と `FileType` は全単射」は誤りだった** —— `DataType` → `FileType` の向きだけが一意で、逆は SETUP ／ EXPECTED の情報が要る（4 対 2 の写像）。導出には向きが一意であれば足り、結論は変わらない。**要対応が 1 件増えて未実施のまま残るため、Acceptance criteria と Rules に掛かる範囲を書き分けた**（`45e857e`）
- [x] **（#27 で追加・ユーザー確定 2026-08-21）辺③ が可変長ファイルの `length` を黙って落とすことを `issues.md` へ起票する**: 辺③ C-21(省略) は**中間モデルの課題ではない。** 本体スキーマ `$defs.field_def.properties.length` の description は「可変長ファイルでは不要（省略可）」であり、スキーマにも可変長で `length` を禁じる条件は無い。**可変長＋`length` は NTF の YAML 記法として書ける**ため、中間モデルが可変長＋`length` を保持することは正当であり、生成時に拒否してはならない（拒否すると辺②が読めなくなる）。したがって `axis-matrix.md` の C-21(省略) は **❌ ＋ 開示のまま**とし、`—` へ振り替えない（Rules「担保の穴は、テストを足さない場合でも台帳に開示する」）。**起票するのは別件で、「辺③ が可変長ファイルの `length` を黙って落とす」こと** —— Excel 記法は可変長にフィールド長行を持たず（`testdata_notation.rst:1076`「固定長との違いは、可変長ファイルの場合はフィールド長行を記載しない点のみである」／`:883`。**rev と行番号は起票時に再測する**）、`XlsFormatWriter#appendRecord` が `if (fixed)` の中でしか `getLength()` を読まないため、可変長では `length` が出力に一切写らない。これは辺②の余剰落ちと同じ「変換で値が黙って消える」型である。要対応か記録のみかは NTF 仕様から判断する → **`78e513f`。ID は XLS-45。判定は対応不要**（Excel 記法に可変長のフィールド長行が無い以上、辺③ が `length` を書かないのは `notation:1076` ／ `:883` の明文どおりであり、converter に書ける先が無い）**／判断は仕様として不適切**（変換で値が黙って消える。YML-14 と同じ型）。**影響度は中** —— `length: "-"` は可変長でも NTF の格納値を変える（`YamlFileBuilder` が可変長でも `length` が 1 件以上あれば `setLengths` を呼び、`DataFileFragment#addValue` が改行・前後空白を除去する）ため、落ちる値は無害でない。`axis-matrix.md` C-21(省略) は ❌ ＋ 開示のまま据え置いた（逆向きの参照は XLS-45 の節が持つため、セル側には課題 ID を添えていない）。**解説書担当・スキーマ担当宛の申し送りは立てた**（ユーザー確定・2026-08-21。`issues.md` の「申し送り: 可変長フィールドの `length` を NTF としてどう扱うか（XLS-45…）」の節）。**単独では出さず、converter 完了後に XLS-27・XLS-39・XLS-40・XLS-42 とまとめて出す**（`issues.md`「申し送りの束」）
- [x] `issues.md` を通読し、#19〜**#27** で記録した課題が漏れなく載っていること・**#25.5 で修正した課題（判定が要対応であるもの）を除き**修正されていないことを確認する（**範囲を #26 から #27 へ広げた** —— #27 で XLS-44・XLS-45 の 2 件を起票したため。**XLS-44 は要対応だが未実施であり、これは #25.5 の対象外である**。`45e857e` で AC と Rules に書き分けた） → **完了。** 全 4604 行を通読し、`checks/task-19.md`〜`task-26.5.md` と `steering.md` の課題 ID を双方向に機械照合した（未説明の差 0 件）。片方向で出た XLS-02・XLS-03 は `227adc1`（2026-08-13）が節ごと削除したもので、欠番の理由を `356549a` で「対象としない入力」節に残した（欠番はこの 2 件だけ）。**`src/main` を触ったコミットは #18 のチェックオフ `5bf7048` 以降で 39 件**あり、全件を `git show` で開いて分類した—— 要対応の課題の修正 32 件（触る課題 ID は重複を除いて 25 件で、`issues.md` 冒頭の列挙と完全一致）／Javadoc・コメントのみ 5 件／要対応修正の後始末 2 件。**判定が要対応でない課題の修正は 1 件も無い。** **未実施の要対応は XLS-44 の 1 件だけ**であることも判定欄と修正コミットの対応から確かめた。詳細は `checks/task-27.md`
- [x] **#26 からの持ち越し 8 件（用語・体裁）を対応表を書くときにまとめて整える**（ユーザー確定・2026-08-21）: QA-2・C3-3〜C3-7・C3-9・C3-10。**内容の正は `checks/task-26.md`「追加 1 巡」節** → **`afdf5d2`。対象は `coverage-report.md`。** QA-2 は各行に `§6-5` を足さず §3.0 凡例の「内部整合性ガード」に 1 か所置いた（採った道と規約に触れない理由も残した）。C3-3 は §3 柱書に「根拠欄の『こう書けば到達する』はコードからの推定であり実行して確かめた事実ではない」を断った。C3-4 は §6-3 を (a) 陳腐化の手当て ／ (b) 事実誤りの訂正に割り、柱書を「6 節・7 件」へ導き直した。C3-5 は出口エッジの呼称を「分岐」・助数詞を「件」にそろえた（「後続」を廃止、「飛び先」は switch 限定）。C3-6 は「裏面」を「紐づく」にそろえ、`throw` ／ `return` が識別にならないことを 4 行の実例つきで明記した。C3-7 は §3.1 #16 の証明を §2 の地の文へ移した。C3-9 は #21 と #22 の分け目を §3.0 凡例の下に出した。C3-10 は §3.4 の見出しに区分⑤ を戻した。**あわせて §6-3 の確認コマンド 2 本を #26 の完了コミット `58bae09` に固定した**—— #27 で `inventory.md` に凍結出力ブロックが増え、宣言値 8 に対して 10 を返す状態になっていたため（現在の `inventory.md` でも 10 ／ 10 で一致し、結論そのものは生きている）
- [x] **#26.5 からの持ち越し 8 件（担保の穴）を軸マトリクスの空欄の理由として書く**（ユーザー確定・2026-08-21）: ①〜⑧ の内容は #26.5 の「#27 へ持ち越す担保の穴 8 件」が正。**持ち越し先にも「#26.5 で担保が二層（値の literal 2 件・機構の往復 4 件）あることを実測で確かめたうえで、なお埋まっていない穴である」と 1 文添える**——「未検証だから穴」と誤読させないため（ユーザー指示・2026-08-21） → **§7 に 8 件を置き、指定の 1 文を 8 件全体に掛かる位置に置いた。辺①〜④の C-08(空) の 4 行から参照させている**
- [x] **（#27 で追加）`inventory.md` §0.1-2「担保の現在地」の陳腐化を現行の正へ揃える** → **`86de187`。** `inventory.md` 全体で HEAD に実在しないテストメソッド名はユニーク 47 件・出現 129 か所あるが、直したのは**現行の正を述べる位置**（§0.1-2 追補その 5・§0.8-7・§5.3）だけである。「削除したテスト」欄・増減内訳（当時の記録として正しい）と、§1〜§4 のスナップショット表（書き換えない取り決め）は触っていない。**スナップショット側の不在 18 件は現在の担保先とセットで実装担当の報告に残っている**（本コミットの本文には無い）
- [x] **軸E の ❌ 2 件をどう扱うかユーザー判断を仰ぐ** → **ユーザー確定・2026-08-21: 道 2（#27 の中でテスト 2 本を足して埋める）。** 理由は 3 つ —— ① Acceptance criteria の軸E の条件（:22）は無条件に「埋まっている」ことを求めており、対応表全体の条件（:24）のような「空欄には理由が書かれている」という逃げ道が無い。開示のみで #28 に入ると未達を抱えたまま承認を求めることになる ② 穴が実在することはユーザー側でも実測で確認済み ③ 両ファイルは本作業で新規作成したもの（`0e4ad9a`）で移動ファイルではないため、Acceptance criteria の「実装無改変」に触れない。**以下は判断を仰いだ時点の記録である**: 軸E の 44 セルを 4 辺横断で総点検した結果、**辺③ E-1(1件)（セクション内ブロック数 1）と辺④ E-4(1件)（出力 YAML ファイル数 1）に担保が無い**ことが確定し、`832a700` で ✅ → ❌ にした。集計は ✅ 286・❌ 2・空欄 27。**Acceptance criteria「4辺それぞれで軸E（0件／1件／複数件）と軸F（異常系）が埋まっている」に触れる。** 実測の根拠は 2 つ —— シート全体の行数を見るアサートは `src/test` 全体で `XlsFormatWriterModelTest` の 1 か所だけでしかも 0 件側（`is(0)`）、出力ファイル件数を見るアサートは `YamlFormatWriterModelTest` の複数件側（`is(3)`）だけである。**提示した選択肢は 3 つ**——(1) #27 では ❌ として開示するにとどめ #28 の Evaluation で扱う ／ (2) #27 の中でテスト 2 本を足して埋める（`src/test` を触るのでタスクの性格が変わるが作業量は小さい） ／ (3) #27.5 を立てる。**コーディネータの推奨は (2)。** 回答が無いあいだは (1) の形で進める
- [x] **（#27 で追加・道 2）辺③ E-1(1件) を埋めるテストを足す** → **`783810b`。** `XlsFormatWriterModelTest#writesOnlyOneBlockWhenSectionHasSingleBlock`。件数は出力側で固定した —— シート全体の行数 `getPhysicalNumberOfRows()` を `is(3)`（識別行・カラム名行・データ行 1 行）でアサートし、2 ブロック目が書かれれば 6 になって落ちる。一時的に 2 ブロック目を足して `Expected: is <3> but: was <6>` で落ちることを実測した。あわせて、従来の根拠「次の行が `null`」が成り立たないことも実測した（2 ブロック目があっても行 3 は `null` のままで、識別行は行 4 に来る）
- [x] **（#27 で追加・道 2）辺④ E-4(1件) を埋めるテストを足す** → **`6d12021`。** `YamlFormatWriterModelTest#writesOneYamlFileWhenContainerHasSingleSection`。件数は出力側で固定した —— 出力先ディレクトリの実ファイル数 `out.list().length` を `is(1)` でアサートし、中身はリテラルで突き合わせる（`writer.serialize(...)` との照合は実装の出力を実装の出力と比べる自己参照になるため使わない）。一時的に 2 件目のセクションを足して `Expected: is <1> but: was <2>` で落ちることを実測した
- [x] **（#27 で追加）足した 2 本を対応表・台帳へ反映する** → **`50c4a38`。** `axis-matrix.md` は §3.5 ／ §4.5 を ✅ へ、導出コマンドを「❌ の根拠」から「担保の唯一性」へ、§5.2 を ✅ 288・❌ 0・空欄 27 へ導き直した。`inventory.md` は §0.1-2 に追補その 8（① 597 ／ `Tests run: 597`）、§3.1-3 を 11 → 12、§4.1-2 を 16／14／2 → 16／15／2 へ導き直した。全件実行は `Tests run: 597, Failures: 0, Errors: 0, Skipped: 2`
- [x] **水平展開（軸B・C・D・F・A へ同型の点検を広げる）。着手前に見積りだけ報告する**（ユーザー指示・2026-08-21）: 発端は「1 巡目で同型の 1 件だけを直して水平展開しなかった」ことである。探す欠陥型は**「表が主張する内容を、テスト本文が実際には主張していない」**。**新たに ❌ が出た場合の扱いもユーザー確定済み** —— Acceptance criteria が無条件に「埋まっている」ことを求めている軸（E・F）は #27 の中でテストを足して埋め、それ以外の軸（A・B・C・D）は空欄＋理由でよい。**規模を理由に範囲を勝手に狭めない**（狭めるかどうかはユーザーが判断する） → **見積りを報告済み（2026-08-21）。以下は実測値である**:
  - **ユーザー確定・2026-08-21: 全件（273 セル）を実施する。空欄（`—`）27 件を対象に含める。**見積り 4〜7 時間は承認済み。範囲を狭めない（狭めるかどうかはユーザーが判断する）。順序は 空欄 27 件 → 軸F → 軸B・D → 軸A → 軸C。**軸を 1 本終えるたびに ❌ の件数と所要時間を 1 行で報告する**。見積りからの乖離が大きくなった時点で報告する
  - **空欄 27 件の点検は 2 段**（ユーザー確定・2026-08-21）: (1) 根拠テスト本文が理由欄の述べる拒否／API 形状を実際にアサートしているか、(2)「到達不能」という主張そのものが、その辺の入口を全部数えたうえで言えているか。**「仮に届いても中間モデルが拒否する」型（12 件）は (1) だけで足りる。「本体パーサ／スキーマ／API 形状が先に閉じている」型（12 件）と「対象外」型（2 件）は (2) が要る**
  - **新たに出たものの扱い**（ユーザー確定・2026-08-21）: 軸E・F の ✅ が崩れたら #27 の中で埋める／軸A〜D の ✅ が崩れたら ❌ を立てて理由を書く（`—` に振り替えない）／空欄が到達可能と判明したら、軸E・F は埋め、軸A〜D は ❌ へ移して §5.4 と同じ形で開示する。**1 件 1 コミット。docs のコミットと src/test のコミットを混ぜない**
  - **完了・2026-08-21。全 273 セル（空欄 27 件を含む）を点検し終えた。所要は 31 分**（`723e70e` 15:04 → `c0ac0e1` 15:35。サブエージェント 20 本を 3 波で並行実行。直列見積り 4〜7 時間に対する実測）。**結果は 10 件で、1 件 1 コミット**（`f27fb8d` ／ `ed76073` ／ `205d4e5` ／ `75ded0a` ／ `3a35a75` ／ `f919d8f` ／ `a9f936a` ／ `7f416a8` ／ `c0ac0e1`。src/test は 1 行も触っていない）:
    - **状態が ✅ → ❌ へ動いたもの 6 件**（軸A〜D のため取り決めどおり ❌ ＋ 理由。`—` へ振り替えていない）—— 辺② C-10（VARIABLE 側の担保が in-memory 経路だけ）／辺③ C-21(省略)（可変長では `length` が出力に写らずアサートが値に無反応）／辺④ A-06・A-07・A-08・A-09（辺④の出力が `DataType` の FIXED ／ VARIABLE の別に依存しない。`serialize` の出力がバイト単位で同一であることを実測）
    - **状態は動かず出典・機構名を直したもの 4 件** —— 辺② C-15(空)（スキーマ側の出典をテストから本体スキーマへ）／§5.3 の分類（辺② C-11(空)・C-13(空) を閉じているのは YAML スキーマでなく `DataFile` の注入）／辺① C-17(空)（本体側の番人が 2 つあり、根拠テストは片方しか通していない。§6.2 の 8）／辺② 軸C の 3 行（根拠テスト欄が当該フィールドをアサートしないメソッドを挙げていた）
    - **集計は ✅ 282・❌ 6・空欄 27・合計 315**（辺① 71/0/8、辺② 73/1/8、辺③ 71/1/5、辺④ 67/4/6）。実在照合コマンドは NG 0、§0.6 の ④ と ⑥ は出力なし（正）を確認済み
    - **ユーザー判断待ちが 2 件ある**（下の「#27 の残り」を参照）
  - **点検はサブエージェントで並行に行う**（ユーザー指示・2026-08-21）。読むだけで書き込みを伴わないため衝突しない。分割の単位は 軸 × 辺（軸C の 125 セルは辺ごとに 4 分割）、空欄 27 件は §5.3 の分類の型ごと。**各サブエージェントへ必ず渡すもの**は 3 つ —— 探す欠陥型／判定基準（`axis-matrix.md` §0.1・同 §0.2）／既知の実例 2 件（辺③ E-1(1件)・辺④ E-4(1件)。どちらも「入力にその件数を与えているだけで、出力に n＋1 件目が無いことをアサートしていなかった」型）。**出力の形も固定する** —— セルごとに ✅ 維持／❌ 疑い と、根拠となるアサート行を `ファイル:行番号` で 1 件以上（行番号を挙げられないセルは自動的に ❌ 疑い）。迷ったら ✅ でなく ❌ 疑い側へ倒す。**`axis-matrix.md` を書き換えさせない・コミットさせない。表への反映と ❌ の確定は本文を自分で開き直したうえで行い、サブエージェントの報告をそのまま事実として採らない**
  - **対象は 246 セル**（軸A 52・軸B 16・軸C 125・軸D 37・軸F 16）。軸E の 44 セルは `832a700` で点検済み
  - **開くテスト本文は 161 件**（246 セルが挙げるメソッドの重複を除いた数）。**うち 35 件は軸E の点検で既に読んでおり、新たに読むのは 126 件**（下の 2 つ目のコマンド）
  - **上の 246 セルに空欄（`—`）27 件は含まない。** 空欄の理由が挙げる根拠テストにも同じ型の穴はありうる（理由の正しさをテスト本文で確かめていないため）。**含めるかどうかはユーザーの判断**であり、こちらで足していない
  - **軸E の実績は 44 セル・41 メソッドで 79 分**（`1c3253b` 12:46 → `832a700` 14:05。**同コミットには軸E 以外の直しも含まれるため上限値である**）。セル比 5.6 倍・新規メソッド比 3.1 倍から**おおよそ 4〜7 時間**（連続作業時間。❌ が出た場合のテスト追加は別途）
  - 導出コマンド（`axis-matrix.md` を走査する。`✅` の行だけを数える）:
    ```sh
    cd "$(git rev-parse --show-toplevel)"/.rn/ntf-test-data-converter/coverage
    perl -ne '
      if (/^### \d\.\d 軸([A-F])/) { $ax = $1 }
      next unless /^\| [A-F][0-9-]/;
      my @c = split(/\|/, $_, -1);
      my $st = $c[3]; $st =~ s/\s//g; next unless $st eq "✅";
      $cell{$ax}++;
      my $k; while ($c[4] =~ /`([A-Z][A-Za-z0-9]*)?#(\w+)/g) { $k = $1 if defined $1; $m{$ax}{"$k#$2"} = 1 }
      END { for my $a (sort keys %cell) { printf "axis %s: cells %3d, methods %3d\n", $a, $cell{$a}, scalar keys %{$m{$a}} } }
    ' axis-matrix.md
    ```
    ```sh
    cd "$(git rev-parse --show-toplevel)"/.rn/ntf-test-data-converter/coverage
    perl -ne '
      if (/^### \d\.\d 軸([A-F])/) { $ax = $1 }
      next unless /^\| [A-F][0-9-]/;
      my @c = split(/\|/, $_, -1);
      my $st = $c[3]; $st =~ s/\s//g; next unless $st eq "✅";
      my $k; while ($c[4] =~ /`([A-Z][A-Za-z0-9]*)?#(\w+)/g) { $k = $1 if defined $1;
        ($ax eq "E" ? $e{"$k#$2"} : $o{"$k#$2"}) = 1 }
      END { my $done = grep { $e{$_} } keys %o;
            printf "E: %d, other: %d, already read: %d, new: %d\n",
                   scalar keys %e, scalar keys %o, $done, (scalar keys %o) - $done }
    ' axis-matrix.md
    ```
- [x] self-check（OK/NG per completion criterion、checks/task-27.md に記録） → **4 件とも OK。** `Tests run: 597, Failures: 0, Errors: 0, Skipped: 2`
- [x] QA expert review（subagent） → **3 巡目 pass。** 1 巡目 fail（4 件）→ 再レビューで全件解消
- [x] Craft expert review（subagent, writing） → **3 巡目 pass。** 1 巡目 fail（15 件。うち 1 件は Invalid）→ 再レビューで全件解消
- [x] Verification expert review（subagent, fact-check） → **3 巡目 pass。** 1 巡目 fail（5 件）→ 再レビューで全件解消。**3 者の指摘は重複を除いて 17 件で、`2261c74`〜`41f75c4` の 8 コミットで是正。再レビューが出した 5 件（すべて重大度 低）は `59d511d` で処置した。** 繰り返し出た欠陥型（宣言値の導き直し漏れ）には、§0.6 に「`src/test` ／ `inventory.md` を触ったら `sh` ブロックを全本流し直す」を置いて機械的に潰す形で手当てした。その全数実行が、是正自身が持ち込んだ欠陥を 2 件（`f4f58fe` ／ `41f75c4`）捕まえている。三者の判定と triage は `checks/task-27.md`

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

- [x] Acceptance criteria（フェーズ2分を含む全項目）を1件ずつ検証し、結果をまとめる → **検証は済み（2026-08-21）。13 項目のうち 12 項目が充足、1 項目が条件つき充足。検証不能は 0 件。**
  - **「全移動ファイルが移動元 `1035207` と package/import を除いて完全一致（実装無改変）」は充足である**（2026-08-21 に移動元 SHA を特定して照合し直した。全文は `checks/task-28.md`）。
    - **移動元 ＝ `1035207`**（`103520709cf6ddeec6da7f901f2b4a5aacbffdef`。本体 `convert-testdata-excel-to-text@{70}`・`2026-06-23 17:14:02 +0900`）。**現 HEAD `06a73f8` からは到達不能で、reflog 経由でのみ到達できる。** 対する converter 側は移動時点 `2a069bc`（`feat: complete task #2 — copy src/main converter (28 files)`）。
    - **結果は 対象 28 件 / NG 0 件。** `package` 行・`import` 行を除いた sha1 が全ファイルで一致し、ファイル名の集合も両方向で一致する（`diff <(git -C "$UP" ls-tree …1035207) <(git -C "$CONV" ls-tree …2a069bc)` → 差分なし）。再現用スクリプトは `checks/task-28.md` §3。**本体には読み取りしかしていない**（`git status --short` 無出力）。
    - **`becd7b3`／`67a8780` 時点の「検証不能」「一致 20・差分 5・上流に不在 3」は誤りである。** 照合の相手に `d5ec1d0`（`worktree-agent-a79308e7e5862d004`）を取ったための取り違えで、これは converter を作り直した別系統（25 ファイル・`FormatHandler`／`XlsFormatHandler`／`YamlFormatHandler` を持たない）である。
    - **照合の射程は移動時点までである。** converter 側の基準は `2a069bc`。`2a069bc..HEAD` の `src/main` は 79 コミット動いており、うち #18 まで（`5bf7048`）で 40 コミット・33 ファイル・+2514／-524 行である（`git rev-list --count 2a069bc..5bf7048 -- src/main` → 40、`git diff --stat 2a069bc 5bf7048 -- src/main` → `33 files changed, 2514 insertions(+), 524 deletions(-)`。2026-08-21 実測）。中身は ConverterMojo 実装・内部クラスの top-level 化・json-schema-validator 3.0.2→1.5.9 移行・Excel 出力の表示品質・重複カラム名 WARN など、いずれも #4〜#18 のタスクとして承認済みのものである。**これらと #25.5・#26.5 は本項（実装無改変）の対象外であり、その妥当性は別項（`issues.md` の判定欄に基づく変更限定）が担保する。**本体側の `src/test` も対象外で、射程は `src/main` の converter 28 件のみ。**この射程は条件を緩めたものではなく、実際に検証した範囲である**（Acceptance criteria・Assumptions にも同文を書いた）。
    - **移動元 `1035207` の退避はしない**（ユーザー確定・2026-08-21）。救出用のタグ・ブランチは作らない。照合は `checks/task-28.md` に再現用スクリプトごと記録済みであり、**再照合が必要になる場面は想定しない**。未決事項として持ち越さない。（`1035207` は reflog 依存で、`gc.reflogExpireUnreachable` 既定 30 日に対し作成から約 2 か月。gc が走れば消える。それを承知のうえでの判断である。）
  - **条件つき充足 1 件: 「辺①…軸A（`DataType` 14種）すべてが実ファイル経由で1回以上通っている」。** 13 種は ✅ で、残る 1 種 `DEFAULT` は **中間モデルが生成時に拒否するため入力を組めない**（`issues.md` XLS-20）。`axis-matrix.md` 辺① A-01 に `—` と理由・根拠テストを記載済み。
  - **充足 11 件**: `mvn test` 全 PASS（`Tests run: 597, Failures: 0, Errors: 0, Skipped: 2`）／ pom.xml の 4 依存（yaml `1.0.0-SNAPSHOT`・本体・poi-ooxml `3.8`・snakeyaml-engine `3.0.1`）／ 本体・yaml とも作業ツリーがクリーンで書き込みなし／ push 済み／ 軸B は 4 辺とも 4/4 ✅・軸C は空欄と ❌ に理由あり・`fileType` は FIXED ／ VARIABLE とも ✅／ 軸D は 8・12・8・9 で規定どおり全 ✅／ 軸E・軸F は到達不能な空欄を除き全 ✅／ 参照フィクスチャ `ProjectActionRequestTest.xlsx` と POI 生成物の一致は `XlsReferenceFixtureTest#poiGeneratedWorkbookReadsIdenticallyToExcelSavedWorkbook` が担保／ 対応表 315 行（✅283・❌5・空欄27、理由つき）／ カバレッジ計測と未到達分岐 34 件の分類（足すべき 19・不要 15）／ 課題一覧 57 件（要対応 26・うち未実施は XLS-44 の 1 件）／ `JAVA_HOME=… mvn clean test -Djacoco.skip=true` 全 PASS
  - **❌ 5 件は開示のまま #28 へ持ち込んでいる**（辺③ C-21(省略) ＝ `issues.md` XLS-45 ／ 辺④ A-06〜A-09 ＝ `issues.md` XLS-44。後者は要対応・未実施で、実施は #28 の後に独立タスク #29。**#29 はマージ前に片付ける**——ユーザー確定・2026-08-21）
- [x] 結果をユーザーへ提示し、`/rn:ty`（承認）または `/rn:gm`（差し戻し）の判定を受ける → **2 巡目の提示（`372f862`）で承認された（2026-08-21）。**ユーザーは差し戻し 5 件の反映を実物で確認し（射程の 3 箇所・束 5 件・`U-1` の 1 行・`checks/task-28.md` §5・「マージ前」の 7 箇所）、本体にタグ・ブランチが作られていないこと・`ae30342..HEAD` に `src/` の差分が無いことも確認したうえでの承認である。**1 巡目の提示（`ae30342`）は差し戻された。**提示内容は AC 13 項目（充足 12／条件つき充足 1／検証不能 0）、ゲート再実行（`JAVA_HOME=… mvn clean test -Djacoco.skip=true` → `Tests run: 597, Failures: 0, Errors: 0, Skipped: 2`・BUILD SUCCESS）、および差し戻し 5 件への対応（下記）。
  - **1 巡目（`ae30342` 時点）の提示は差し戻された**（2026-08-21）。指摘は 5 件 —— ① 実装無改変の条件文に照合の射程を書く（除外を #25.5・#26.5 の 2 つに限定した書き方は現 HEAD に対して成り立たない）／② `U-1` を申し送りの束から外す／③ XLS-39 を束に入れる／④ `1035207` の退避はしない／⑤ XLS-44 はマージ前に片付ける。**いずれも文書のみで、`src/main` ／ `src/test` は触っていない。**
  - **差し戻しへの対応（2 巡目）**: ① Acceptance criteria・Assumptions・本 Steps の 3 箇所に射程を明記した（照合は移動時点 `2a069bc` に対して行う／移動後の変更は対象外／妥当性は別項が担保する／実測値と出典コマンドを併記）。② `issues.md`「申し送りの束」の表から `U-1` の行を削り、「`U-1` は調整側（`~/work/cowork/nablarch/ntf-converter/README.md`「U-1 解説書担当への回答は保留中」）が保持する。converter は保持しない」の 1 行に置き換えた。③ 束を **XLS-27・XLS-39・XLS-40・XLS-42・XLS-45 の 5 件**とし、「束に入れていない申し送りが 1 件ある／未指定」の記述を削除した。④ `checks/task-28.md` §5 を「退避しない。再照合が必要になる場面は想定しない」という判断の記録へ改めた（`checks/` は本来揃えない取り決めだが、本件はユーザーの明示指示による）。⑤ Acceptance criteria・Rules・`issues.md`（4 箇所）に「実施は独立タスク #29。**マージ前に片付ける**」を書いた。
  - **#29（XLS-44 の実施）について。** **あるべき姿は `FileDataBlock` の `fileType` を `DataType` から導出して重複を消すこと。**`DataType` → ファイル種別の向きは明文上一意に定まる（本体スキーマ `$defs.file_data.properties.type` の description ＝「fixed = 固定長（SETUP_FIXED / EXPECTED_FIXED）、variable = 可変長（SETUP_VARIABLE / EXPECTED_VARIABLE）」／`notation:850`。逆向きは SETUP／EXPECTED の情報が要るため一意でない）。**Steps 起こしは本再提出の承認後でよい**（ユーザー確定・2026-08-21）。**承認を受けて #29 を起こした（2026-08-21）。**

**Completion criteria**:

- Acceptance criteria の実行結果がユーザーに承認されている

---

### #29: XLS-44 — `FileDataBlock.fileType` を `DataType` から導出して重複を消す

**Purpose**: 中間モデルが NTF 仕様で表現できない状態（`DataType` ＝ `SETUP_FIXED` かつ ファイル種別 ＝ 可変長、およびその逆）を保持できてしまい、同じモデルから辺③と辺④が別のファイル種別を書く問題（`issues.md` **XLS-44**・要対応・未実施）を解消する。**採る手は整合検査（番人）ではなく導出である** —— `fileType` を `DataType` から導出すれば、食い違う組はそもそも表現できなくなる。`steering.md` Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」より一段強い形で、不正な状態を作れなくする（ユーザー確定・2026-08-21）。

**明文の根拠**: 本体スキーマ `nablarch/test/ntf-testdata-yaml-schema.json` の `$defs.file_data.properties.type` の description（「fixed = 固定長（SETUP_FIXED / EXPECTED_FIXED）、variable = 可変長（SETUP_VARIABLE / EXPECTED_VARIABLE）」）と `notation:850`。**`DataType` → ファイル種別の向きは一意に定まる**（逆向きは SETUP／EXPECTED の情報が要るため定まらない —— 4 対 2 の写像であって全単射ではない）。

**Prerequisites**: #28

**Steps**:

- [x] 【赤】`FileDataBlockTest` に、4 種の `DataType`（`SETUP_FIXED`／`EXPECTED_FIXED`／`SETUP_VARIABLE`／`EXPECTED_VARIABLE`）それぞれについて導出されたファイル種別が `FIXED`／`FIXED`／`VARIABLE`／`VARIABLE` になることを主張するテストを書く（既存の `固定可変とSETUP_EXPECTEDの全組合せを保持する` は対角 4 組を「引数で渡して保持されること」しか見ていないため、導出を主張する形へ書き換える）
- [x] 【赤】XLS-30 の番人が `DataType` 起点で走ることを主張するテストを書く —— `DataType` ＝ `SETUP_FIXED`（または `EXPECTED_FIXED`）で `length` ＝ `null` のフィールド定義を持つブロックが `IllegalArgumentException` で拒否されること。**XLS-44 の観測では `fileType` ＝ `VARIABLE` を渡すとこの検査が素通りしていた**（`issues.md` XLS-44「既存の不変条件も素通りする」）
- [x] 【赤】辺③ —— `DataType` ＝ `SETUP_FIXED` のブロックから長さ行が出ること／`SETUP_VARIABLE` のブロックからは出ないことを `XlsFormatWriterModelTest` で主張する（現在の出力条件は `XlsFormatWriter#layoutFile` の `boolean fixed = block.getFileType() == FileType.FIXED`）
- [x] 【赤】辺④ —— `type:` が `DataType` から出ることを 4 ケースで主張する（`SETUP_FIXED`／`EXPECTED_FIXED` → `type: "fixed"`、`SETUP_VARIABLE`／`EXPECTED_VARIABLE` → `type: "variable"`）。これが `axis-matrix.md` 辺④ 軸A の **A-06〜A-09**（現在 ❌ 4 件）の担保になる
- [x] 【赤】辺④→辺② の往復で `SETUP_FIXED` が `SETUP_VARIABLE` へ化けないことを主張するテストを書く（XLS-44 の「辺④は検出できない」経路が塞がることの担保）
- [x] 【緑】`FileDataBlock` のコンストラクタから `FileType` 引数を外し、`DataType` から導出する。`getFileType()` は導出値を返す。末尾の `if (fileType == FileType.FIXED) { ModelPreconditions.requireLengths(...); }` を導出値（＝ `DataType`）起点にする
- [x] 【緑】`src/main` の生成箇所 2 か所を追随させる —— `xls/XlsFormatReader.java`（既に `isFixed(type)` で導出しており、引数を落とすだけ）／`yaml/YamlFormatReader.java`（`fileDataType(setup, fileType)` で `DataType` を決める向きは変えず、`FileDataBlock` へ `fileType` を渡すのをやめる）
- [x] 【緑】**`FileType` enum と `getFileType()` は残す**（ユーザー確定・2026-08-21）。NTF 仕様が「ファイル種別」を名前つきの 2 値として持つためである（本体スキーマ `$defs.file_data.properties.type` は `required` かつ `enum` ＝ `["fixed", "variable"]`／`notation:883`（`30a8271` 時点）は記法を固定長ファイルと可変長ファイルの 2 種類に尽くしている）。**XLS-44 が消すのは概念ではなく二つ目の真実の置き場である。**真偽値へ寄せると仕様側の語彙をモデルから落とし、辺④で 2 値を復元し直すことになる
- [x] 【緑】寄せる重複は **`XlsFormatReader#isFixed(DataType)`（`XlsFormatReader.java:682`）の 1 か所だけ**にする（ユーザー確定・2026-08-21）。`DataType` → ファイル種別で、モデルが持つ導出とまったく同じものだから消える。**次の 3 か所は寄せない** —— `YamlFormatWriter.java:193` の `getFileType() == FIXED ? "fixed" : "variable"`（ファイル種別 → YAML の語への写像。辺④固有の語彙変換）／`XlsFormatWriter.java:286` の `boolean fixed = block.getFileType() == FileType.FIXED`（辺③固有の長さ行の出し分け）／`YamlFormatReader.java:517` の `fileDataType(boolean setup, FileType)`（逆向き。辺②が `DataType` を決めるのに必要）
- [x] 【緑】**XLS-29 の番人が到達不能になることを記録する。** `fileType` 引数が消えると `fileType == null` の検査は到達できず、それを主張する `FileDataBlockTest#ファイル種別がnullのファイルブロックは生成できない` も成立しなくなる。**これは退行ではなく、番人が不要になった（不正な状態を型が表現できなくなった）ということである。**テストは削除し、`issues.md` の XLS-29（`:3012`）・番人の表（`:132`）・`:4156` を「生成時の検査から、型として表現不能へ改めた（#29）」と更新する。**番人を惜しんで `DataType` の `null` 検査などに置き換えないこと**（ユーザー確定・2026-08-21）
- [x] 【緑】`src/test` の生成箇所 46 か所を追随させる（`grep -rc 'new FileDataBlock' src/main src/test --include=*.java | grep -v ':0$'` → `src/main` 2・`src/test` 46・計 48。2026-08-21 実測）。**食い違う組を作っていたテスト（あれば）は、その意図ごと見直す**
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` で全 PASS を確認する
- [x] 文書を現行定義へ揃える（`steering.md`／`coverage/inventory.md`／`coverage/issues.md`／`coverage/axis-matrix.md`。`checks/` は揃えない）
  - `issues.md` XLS-44 —— 判定欄を「要対応・**実施済み（#29）**」へ改め、担保テスト名を書く。「未実施は XLS-44 の 1 件」と書いている箇所（冒頭 `:17`・`:101`・`:4125` ほか）を実施済みへ改める
  - `axis-matrix.md` —— 辺④ 軸A の A-06〜A-09 を ✅ にし担保テスト名を入れる。軸C の `FileDataBlock.fileType` の行は**フィールドが無くなるため削る**（削った旨を注記する）
  - `steering.md` Acceptance criteria —— 「`FileDataBlock.fileType` は `FIXED`／`VARIABLE` の両方を通す」を、フィールド消滅後に成り立つ表現（固定長系・可変長系の `DataType` を両方通す）へ改める。「未実施は XLS-44 の 1 件」「#29 はマージ前に片付ける」の記述も実施済みへ揃える
  - `inventory.md` —— テストメソッドを増減させたため、件数をコマンドから導き直して出典コマンドを併記する（Rules の #22 規定）
  - `issues.md` —— `XlsFormatReader#isFileType(DataType)`（`:670`）が `FileDataBlock.PERMITTED_TYPES` と同じ集合を二重に持っている件。**寄せられるなら寄せてよいが必須ではない**（#29 の範囲を広げてまでやることではない）。寄せない場合は `issues.md` に 1 行残す（ユーザー確定・2026-08-21）
- [x] 1 コミットにまとめて push する

**Completion criteria**:

- `FileDataBlock` のコンストラクタに `FileType` 引数が無く、`DataType` ＝ `SETUP_FIXED`／`EXPECTED_FIXED` と可変長（およびその逆）の食い違う組が**型として表現できない**
- `DataType` ＝ 固定長系で `length` ＝ `null` のフィールド定義を持つブロックが `IllegalArgumentException` で拒否される（XLS-30 の番人が `DataType` 起点で走る）
- 辺③ の長さ行の出力条件が `DataType` 起点であり、`SETUP_FIXED`／`EXPECTED_FIXED` のブロックからは長さ行が出る
- 辺④ の `type:` が `DataType` から出る（`SETUP_FIXED`／`EXPECTED_FIXED` → `fixed`、`SETUP_VARIABLE`／`EXPECTED_VARIABLE` → `variable`）
- 辺④→辺② の往復で `SETUP_FIXED` が `SETUP_VARIABLE` へ化けない
- `axis-matrix.md` 辺④ 軸A の A-06〜A-09 が ✅ で、担保テストメソッド名が記されている
- `FileType` enum と `getFileType()` が残っている（消すのはフィールドと二つ目の真実の置き場であって、仕様側の語彙ではない）
- XLS-29 の番人の到達不能化が `issues.md` に記録されている（`DataType` の `null` 検査などへ置き換えていない）
- `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` が全テスト PASS する
- `steering.md`・`coverage/inventory.md`・`coverage/issues.md`・`coverage/axis-matrix.md` が現行定義へ揃っている（`checks/` は対象外）
- 本体・yaml に書き込んでいない

---
### #30: XLS-44 —— `FileDataBlock#fileTypeOf(DataType)` の受け口を締める

**Purpose**: #29 で `fileType` を `DataType` からの導出に変えたが、導出の置き場である `FileDataBlock#fileTypeOf(DataType)` は `public static` で、**ファイル系 4 種以外の `DataType`（例 `LIST_MAP`）を渡すと黙って `VARIABLE` を返す**（`return dataType == SETUP_FIXED || dataType == EXPECTED_FIXED ? FIXED : VARIABLE;`）。呼び出し側 2 か所（`FileDataBlock#getFileType()`／`XlsFormatReader.java:207`）はいずれも手前で 4 種に絞っているため誤りは起きないが、**#29 の趣旨は「不正な状態を型で表現できなくする」ことであり、同じ亀裂が公開ヘルパの側に小さく残っている。到達可能性を理由に残さない**（ユーザー確定・2026-08-24）。

**Prerequisites**: #29

**Steps**:

- [x] 【赤】`FileDataBlockTest` に、`fileTypeOf` へファイル系 4 種以外の `DataType`（例 `DataType.LIST_MAP`）を渡すと `IllegalArgumentException` になることを主張するテストを**1 本だけ**足す。4 種それぞれが `FIXED`／`FIXED`／`VARIABLE`／`VARIABLE` を返すことは既存の `FileDataBlockTest#ファイル種別を4種のデータ種別から導出する` が担保しているため、足すのは拒否側の 1 本でよい（ユーザー確定・2026-08-24）
- [x] 【緑】`fileTypeOf` の冒頭で `PERMITTED_TYPES` を検査する（`requireDataTypeOf(FileDataBlock.class, PERMITTED_TYPES, dataType)`）。コンストラクタが既に持っている検査と同じ考え方であり、**新しい仕組みは要らない**。**XLS-36 の既存の例外メッセージと振る舞いは変えない**（`TestDataBlockTest#ファイル系でないデータ種別のファイルブロックは生成できない` が `"FileDataBlock"` を含むことを主張している）。コンストラクタ経由では検査が二重に走ることになるが、それでよい（ユーザー確定・2026-08-24）
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` で全 PASS を確認する
- [x] 文書を現行定義へ揃える
  - `issues.md` —— XLS-44 の「**修正（#29）**」に続けて **1 行**足す（受け口を締めた旨）。**新しい課題番号は立てない**（ユーザー確定・2026-08-24）
  - `inventory.md` —— テストメソッドを 1 件増やしたため、件数をコマンドから導き直して出典コマンドを併記する（Rules の #22 規定。600 → 601）
  - `axis-matrix.md` は動かない（軸要素の担保状況は変わらないため）
- [x] self-check（OK/NG per completion criterion、`checks/task-30.md` に記録）
- [x] QA expert review（subagent）／Craft expert review（subagent, coding）／Verification expert review（subagent, test） —— **2 巡実施し、3 巡目の途中で打ち切った**。**#30 以降はレビュア subagent を回さない**（ユーザー確定・2026-08-24。Rules 参照）。2 巡で出た指摘は triage 済みで、Valid 12 件を反映し 3 件を却下した（却下の主なものは「`fileTypeOf` の拒否メッセージが導出の文脈と合わない」——専用の例外を投げる案は「新しい仕組みは要らない」「既存メッセージと振る舞いは変えない」に反するため）
- [x] **1 コミット**にまとめて push する（ユーザー確定・2026-08-24。#29 と同じ形）

**追加ステップ（`fileTypeOf(null)` を閉じる。ユーザー確定・2026-08-24）** —— **#30 の承認は「`fileTypeOf(null)` を閉じるまで」を条件とする。**`34e78cc` はそのまま残し、続きを **1 コミット**で足す。`fileTypeOf(null)` が `NullPointerException` になる状態を残さない —— `TestDataBlock` のコンストラクタは同じ入力を「データ種別が null のデータブロックは作れません」で拒否しており、**判定は XLS-34 で確定済み**（データタイプの無いブロックはどちらの形式でも書けない）である。決まっている答えがあるのに受け口によって NPE と IAE に分かれるのは、#30 が閉じようとした穴の残りである。「`null` は検査しない。渡した場合の例外の種類は規定しない」を production の契約に書くのは、**決めていないことを契約にしたもの**であり暫定対応にあたる。

- [x] 【赤】`FileDataBlockTest` に、`FileDataBlock.fileTypeOf(null)` が `IllegalArgumentException` になることを主張するテストを **1 本**足す（現状は `NullPointerException`）
- [x] 【緑】`TestDataBlock#requireDataTypeOf` に `null` の分岐を足し、**XLS-34 と同じ趣旨のメッセージ**で `IllegalArgumentException` を投げる。**既存メッセージ本文は変えない**（`permitted.contains` 側の XLS-36 のメッセージは既存テストが文言を主張しているため無変更。`null` は**別の分岐**として足す）。**`fileTypeOf` に独自の例外メッセージを持たせない**（ユーザー確定・2026-08-24。`fileTypeOf(LIST_MAP)` のメッセージも XLS-36 のままでよい —— そこで拒否している事実は「そのデータ種別は `FileDataBlock` の系統ではない」でコンストラクタと同一であり、独自メッセージにすると `FileDataBlockTest#ファイル系でないデータ種別からはファイル種別を導出できない` が主張している「生成時と同じ検査による拒否」という担保が崩れる）
- [x] Javadoc から「`null` は検査しない。渡した場合の例外の種類は規定しない」を消し、`@throws` の「非 `null` で、かつ」の限定も外す（`FileDataBlock#fileTypeOf` ／ `TestDataBlock#requireDataTypeOf` の 2 か所）
- [x] `issues.md` XLS-44 の「`fileTypeOf(null)` は `VARIABLE` → `NullPointerException` に変わる」を書き直す。**あわせて非互換の記述を検証する** —— `fileTypeOf` は #29 で新設したメソッドであり、それ以前に外部から呼べた API ではない。「外部呼び出し側には非互換」と言えるのは #29 以降の版を誰かが使っている場合だけなので、その前提が立つか確かめ、**立たないなら非互換の記述は落とす**
- [x] `inventory.md` のテスト件数をコマンドから導き直す（Rules の #22 規定）
- [x] `checks/task-30.md` に追加分の self-check を追記する
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test` 全 PASS を確認し、**1 コミット**にまとめて push する

**判定 —— 承認された（`/rn:ty`・2026-08-24）。**条件だった「`fileTypeOf(null)` を閉じるまで」は続きの `ba84c2d` で満たされており、#30 は `34e78cc` ＋ `ba84c2d` の 2 コミットをもって確定とする。これで全 30 タスクが完了（未チェックの Steps 0 件）。

**やらないこと**（ユーザー確定・2026-08-24）: `requireDataTypeOf` の既存メッセージ本文を変えない／`fileTypeOf` に独自の例外メッセージを持たせない／辺③④のライタに番人や WARN を足さない／`nablarch-testing`・`nablarch-testing-yaml` に書き込まない。

**Completion criteria**:

- `FileDataBlock#fileTypeOf(DataType)` にファイル系 4 種以外の `DataType` を渡すと `IllegalArgumentException` になる
- 4 種の `DataType` に対する戻り値（`FIXED`／`FIXED`／`VARIABLE`／`VARIABLE`）と、XLS-36 の例外メッセージが**変わっていない**
- **`FileDataBlock#fileTypeOf(null)` が `IllegalArgumentException` になる**（`NullPointerException` にならない）。メッセージの趣旨は XLS-34 と同じ（データタイプの無いブロックはどちらの形式でも書けない）。**【訂正・2026-08-24】この行はもともと「`null` の検査を足していない（XLS-29 の番人を復活させない）」だった。**復活ではない —— XLS-29 の番人は `fileType` ＝ `null` の拒否であり、そのフィールド自体が #29 で消えている。ここで足すのは `dataType` ＝ `null` の拒否（XLS-34）であって、コンストラクタが既に同じ入力を拒否している。**受け口によって NPE と IAE に分かれる状態を残さない**（ユーザー確定・2026-08-24）
- **`FileDataBlock#fileTypeOf` と `TestDataBlock#requireDataTypeOf` の Javadoc から「例外の種類は規定しない」が消え、`@throws` の「非 `null` で、かつ」という限定も外れている**
- **`fileTypeOf(LIST_MAP)` のメッセージが XLS-36 の `requireDataTypeOf` のまま変わっていない**（独自メッセージを持たせていない）
- **既存の振る舞いが変わっていない** —— コンストラクタ経路では `super(...)` が先に `null` を落とすため `requireDataTypeOf` に `null` は届かない。変わるのは `fileTypeOf(null)` だけである
- **`issues.md` XLS-44 の `fileTypeOf(null)` の記述が書き直され、非互換の記述はその前提を検証したうえで残す／落とすが決まっている**
- **`fileTypeOf` の可視性を下げていない**（導出の唯一の置き場であるため `public static` のままでよい）
- `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` が全テスト PASS する
- `issues.md` の XLS-44 に受け口を締めた旨が 1 行記録され、新しい課題番号が立っていない
- `inventory.md` のテスト件数がコマンドから導き直され、出典コマンドが併記されている
- 本体（`nablarch-testing`）・yaml（`nablarch-testing-yaml`）に書き込んでいない

---

### #31: XLS-45 —— 可変長ファイルのフィールド定義が `length` を持つ状態を中間モデルで作れなくする

**Purpose**: **NTF 仕様として、可変長ファイルでは `length` を書けない**（ユーザー確定・2026-08-24。YAML 記法は未リリースであり、記法側を変更してよい）。この決定により `issues.md` **XLS-45** の「NTF 仕様としての判定」は **対応不要 → 要対応**へ変わる。中間モデルの生成時に拒否し、可変長＋`length` という状態そのものを作れなくする（`steering.md` Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」。**書き出し側の番人ではない**）。

**明文の根拠**（すべて実物で確認済み）:

1. **Excel 記法に可変長のフィールド長行は無い。** `nablarch-document`（ブランチ `ntf-yaml-support`）の `ja/development_tools/testing_framework/implementation/testdata_notation.rst` —— `notation:1076`「固定長との違いは、可変長ファイルの場合はフィールド長行を記載しない点のみである。」／`notation:883`「可変長ファイルでは、フィールド名称・データ型の2リストが同サイズで必須であり、フィールド長は不要である。」（行番号は本書ほかと同じ `30a8271` 基準。**確認時の HEAD は `3132688`** であり、同ブランチの HEAD でも `:1076` の本文は一致した）
2. **可変長では数値の `length` は NTF 実行時に使われない。** 本体 `nablarch-testing` の `nablarch/test/core/file/VariableLengthFileFragment.java` の `createFieldDefinition` は `setName`／`setPosition`／`setEncoding` だけを設定し、`lengths` を一度も読まない（同ファイルに `lengths` の出現は 0 件。`addValue` の上書きも無い）
3. **`"-"` だけは効いてしまう。** 同 `nablarch/test/core/file/DataFileFragment.java` の `setLengths` が `"-"` を `isOndemandCalcFieldSizeList` に立て、`addValue` が該当フィールドの値に `removeLineSeparatorWithTrim`（改行と前後空白の除去）を掛ける。**これは長さの指定ではなく値の整形であり、フィールド長の枠に相乗りしている。**可変長でこれが起きるのは相乗りの結果であって、追認すべき仕様ではない

**Prerequisites**: #30

**やらないこと**（ユーザー確定・2026-08-24）:

- **辺③④のライタに番人や WARN を足さない。** 可変長が `length` を持てなくなれば、`XlsFormatWriter` が黙って落とす経路も `YamlFormatWriter` の非対称も、両方とも到達しなくなる
- **`YamlTestDataValidator` を改修しない。** スキーマをクラスパスの `/nablarch/test/ntf-testdata-yaml-schema.json` から読むだけなので、スキーマ側の対応が入れば V-SCH は自動で追随する。スキーマ対応が入るまで V-SCH が通ることは織り込み済みでよい
- **本体（`nablarch-testing`）を直さない。** 可変長で `length` が来なくなれば `nablarch-testing-yaml` の `YamlFileBuilder` にある `messaging || hasLength` の分岐が偽になり、上記のトリム経路に入らない。**放置ではなく到達しなくなる**
- **固定長側の既存の振る舞い・例外メッセージを変えない**（既存テストが文言を主張している）
- **メッセージ系（`messaging`）は常に固定長なので影響しない**
- **`nablarch-testing`・`nablarch-testing-yaml` に書き込まない**

**Steps**:

- [x] 【赤】`FileDataBlockTest` に、可変長のデータ種別（`SETUP_VARIABLE`／`EXPECTED_VARIABLE`）で `length` を持つフィールド定義のブロックが `IllegalArgumentException` で拒否されることを主張するテストを足す。**`"-"` も同じく拒否されること**を含める（`"-"` は長さの指定ではなく値の整形の指示であり、フィールド長の枠に相乗りしているだけである）
- [x] 【赤】辺②の**実ファイル経路**で、可変長ファイルのフィールドに `length` を書いた YAML が `IllegalArgumentException` で落ちることを主張するテストを `YamlFormatReaderInvalidInputTest` に足す（**`loadRawMap` 差し替えの in-memory 経路は担保に数えない**。スキーマは現時点で可変長の `length` を許すため、`YamlTestDataValidator` は通り、中間モデルの生成時に落ちる）
- [x] 【緑】`ModelPreconditions` に可変長側の検査を足し、`FileDataBlock` のコンストラクタから呼ぶ（`getFileType() == FIXED` のときは既存の `requireLengths`、`VARIABLE` のときは新しい検査）。**固定長側の既存メッセージは変えない**
- [x] 【緑】Javadoc を揃える —— `FieldDef`（「可変長ファイルでは省略可（`null` 可）」→ 可変長では `length` を持てない）／`FileDataBlock`／`ModelPreconditions`（「可変長ファイルは呼び出さない」の段）
- [x] 【緑】既存テストのうち可変長＋`length` を組んでいるものがあれば、その意図ごと見直す
- [x] `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test` で全 PASS を確認する
- [x] 文書を現行定義へ揃える（`steering.md`／`coverage/inventory.md`／`coverage/issues.md`／`coverage/axis-matrix.md`。`checks/` は揃えない）
  - `issues.md` XLS-45 —— **見出しと判定を「要対応・#31 で修正済み」へ改める**。「NTF 仕様としての判定: 対応不要（明文に反していない）」の段は、上記の決定で覆ったことを明記して**書き直す（消さず、経緯が追える形で）**
  - `issues.md` 冒頭の内訳 —— 要対応 26 → 27／対応不要 28 → 27（合計 57 は不変）。判定欄の**導出コマンドを実行して**値を導き直す。「判断」と「NTF 仕様としての判定」が食い違う一覧から **XLS-45 を外す**（14 件 → 13 件）
  - `issues.md` XLS-45 の申し送りの節 —— **問いではなくなった。**「無視／禁止／任意のどれか決めてほしい」から「**禁止で確定**（ユーザー確定・2026-08-24）。スキーマと解説書の対応を別途依頼済み」へ書き直し、**申し送りの束の表からも外す**（束は 4 件になる）
  - `steering.md` Rules —— 申し送りの束の列挙を 4 件（XLS-27・XLS-39・XLS-40・XLS-42）へ改める
  - `axis-matrix.md` —— **辺③ 軸C の C-21(省略) の ❌ が動くかを導出コマンドから確かめて報告する**（こちらでは断定していない）。動くなら表・§5.2・§0.3 系の集計をコマンドから導き直す
  - `inventory.md` —— テストメソッドを増やしたため、件数をコマンドから導き直して出典コマンドを併記する（Rules の #22 規定）
- [x] self-check（OK/NG per completion criterion、`checks/task-31.md` に記録）
- [x] **1 コミット**にまとめて push する（レビュア subagent は回さない —— Rules・ユーザー確定 2026-08-24）
- [x] **【差し戻し対応・2026-08-24】**`YamlFormatReaderInvalidInputTest#rejectsVariableFileFieldWithLengthFromRealYaml` の Javadoc と `checks/task-31.md` 2 行目の Evidence を実物どおりへ直す —— 「今は converter の生成時に落ちる」／「スキーマ側の対応が入ると落ちる段が前へ動き、例外が `YamlSchemaValidationException`（`IllegalStateException` 系）へ変わるため本テストは落ちる」／「そのときは本テストをスキーマ違反を主張する形へ書き替える」の 3 点。**テスト本文・アサートは変えない**（現状の主張は正しい）／**`src/main` を触らない**／**ほかは直さない**。1 コミット

**Completion criteria**:

- **可変長の `FileDataBlock` を `length` 付きで生成できない**（`SETUP_VARIABLE`／`EXPECTED_VARIABLE` で `length` が非 `null` のフィールド定義を含むと `IllegalArgumentException`。`"-"` も含む）。**拒否は中間モデルの生成時であって、書き出し側の番人ではない**
- 辺②の**実ファイル経路**で、可変長ファイルのフィールドに `length` を書いた YAML が落ちる（`loadRawMap` 差し替えの in-memory 経路は担保に数えない）
- **固定長側の振る舞いと例外メッセージが変わっていない**
- **辺③④のライタに番人も WARN も足していない**／**`YamlTestDataValidator` を改修していない**
- `issues.md` XLS-45 の見出しと判定が「要対応・#31 で修正済み」になっており、旧判定（対応不要）が覆った経緯が残っている
- `issues.md` 冒頭の件数（要対応／対応不要）が導出コマンドから導き直されている
- XLS-45 の申し送りが「禁止で確定」へ書き直され、申し送りの束の表が 4 件になっている（`steering.md` Rules の列挙も 4 件）
- `axis-matrix.md` の辺③ 軸C C-21(省略) について、❌ が動くか動かないかが**導出コマンドの出力とともに**報告されている
- `inventory.md` のテスト件数がコマンドから導き直され、出典コマンドが併記されている
- `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test` が全テスト PASS する
- 本体（`nablarch-testing`）・yaml（`nablarch-testing-yaml`）に書き込んでいない

---

## Step 4 共通の前提（#32〜#39）

**指示書**: `nablarch-document@origin/ntf-yaml-support` の `.rn/20260724-ntf-yaml-support/ntf-step4-05-nablarch-testing-converter.md`。**18 件（是正 7 件・テスト追加 11 件）が確定済みで、探索は不要。範囲を広げない。**

**指示書の版**: **`a16be0a`**（2026-08-27。`docs(step4): converter 指示書 2-2 の除外理由を実測に合わせて訂正する`）。`0d9a049`（2-3 と表の行数の訂正）を含む。**2-2 の「テーブルと `LIST_MAP` は対象外」は誤りとして撤回され、両者が対象へ戻った**（詳細は #33）。

**進め方（ユーザー確定・2026-08-27）**: **#33〜#39 は通しで実施し、タスクごとの `/rn:ty` 判定要求を挟まない。**Rules の「完了時の `/rn:ty` 判定要求」は Step 4 には掛からない。指示書がもともと求めているのは報告 1 本（`checks/step4-report.md`、第6節の 6 項目）であり、途中で止まる約束は「2-1・2-3 の着手前検証」（判定済み）と、**2-2 の着手前特定（実装より前に結果を報告する）**だけである。1 件 1 コミットと `checks/task-NN.md` の記録は従来どおり残す。

**参照点（ピン）**: 解説書 `nablarch-document@5783b35`（`git show 5783b35:<path>`。作業ツリーの HEAD を読まない）／本モジュール `60d9a2d`／`nablarch-testing@3c4bd2a`（**変更しない**）／`nablarch-testing-yaml@0b3015c`（**変更しない**。`~/.m2` に install 済み）。

**共通のやらないこと**: 解説書を直さない（誤りと判断したら根拠を添えて報告して止める）／`nablarch-testing`・`nablarch-testing-yaml` を直さない／解説書に無い書き方を追いかけない／形式間の対応表を作らない。

**落ちたときの扱い**: 第2節（#32〜#36）の是正は直す。**第3節（#38）と完了条件3（#37）で落ちたものは直さず `@Ignore` にし、機械的に集められる印つきの理由を付ける**（例: `@Ignore("NTF-DOC: tools/testdata_converter.rst:287 — 期待 X / 実際 Y")`）。**何を直すかはディレクターが全モジュール分を集めてから判断する。範囲の判断を持たない。**

**ビルド**: `mvn -o clean test`（`clean` 必須。`target/classes` が jacoco 計装済みで残っていると `Cannot process instrumented class` で落ちる）。`mvn install` を打つ場合は `JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64` を付ける。

**報告の置き場**: `.rn/ntf-test-data-converter/checks/step4-report.md`（指示書「6. 報告」の 6 項構成。第1節は記入済み）。

---

### #32: 2-1 —— Excel 形式の読み書きを記法⇄値の対称な写像にする

**Purpose**: 中間モデルが持つのは「テスティングフレームワークが解釈したあとの値」（Java null または `String`）であって Excel 形式固有の記法ではない（`5783b35` の `tools/testdata_converter.rst:14`・`:22`・`:34`-`:35`）。読みで外した記法を書きで戻さないため写像が非対称になっている症状 4 件（`null`／`"null"` の潰れ・`"""` の再読込例外・`\r` の 2 文字化）を、この 1 つの原因で断つ。

**Prerequisites**: なし（第2節の根本。#33 は本タスクと同じ原因を持つ）

**着手前の検証**: **完了・ユーザー受理済み（2026-08-27）**。反例なし（NG=0）。結果は `checks/step4-report.md` §1-1。母集合は `notation.rst`（`5783b35`）の Excel 形式 12 行・YAML 形式 12 行（指示書の「13 行」は `0d9a049` で 12 行へ訂正済み）。

**Steps**:

- [x] Excel の読み込み（`XlsFormatReader` の値の入口 `:157`（TABLE）・`:188`（LIST_MAP）・`:425`（FILE／MESSAGE のデータ行））で `NullInterpreter` → `QuotationTrimmer` → `LineSeparatorInterpreter` をこの順に掛ける（`nablarch-testing@3c4bd2a` の `src/test/resources/unit-test.xml:29`-`:40` と同順）。`${...}` 系は掛けない（`tools/testdata_converter.rst:61`）
- [x] Excel の書き出し（`XlsFormatWriter.nullToLiteral:580`-`:582` ／ `literals:566`-`:572`）で逆写像を行う。Java null → `null` リテラル。`String` は i（CR → 2 文字の `\` ＋ `r`）→ ii（`equalsIgnoreCase("null")` なら半角ダブルクォートで囲む）→ iii（半角 `"` で始まり `"` で終わる、または全角 `”` で始まり `”` で終わるなら半角ダブルクォートで囲む）の順に判定する。**戻さないもの**: 値の途中のダブルクォート／2 文字の `\` ＋ `n`／LF
- [x] `RoundTripTest:652`-`:665` `nullCell_xlsConvertsToLiteralString_yamlPreservesNull` の期待値を `nullValue()` へ変え、メソッド名と Javadoc を直す（`:660` の `is("null")`）
- [x] `RoundTripTest` のクラス Javadoc `:47`-`:53`「可逆性の対象外」から null の非対称の記述（`:50`-`:52`）を落とす
- [x] `XlsFormatWriter` のクラス Javadoc `:56`-`:58`「読み戻しでは文字列 `null` として戻るため、`null`↔`null` は Excel 経路では復元されない」を落とす
- [x] `XlsFormatReader:528`-`:538` の `stripQuotes` の Javadoc を、掛けるインタープリタが 3 つになったことに合わせて書き直す
- [x] 直す前に落ちて直したあとに通るテストを用意し、テスト名を報告に書く
- [x] 足した／直したテストそれぞれについて、期待値をわざと崩すと落ちることを 1 度確認し、崩した内容を報告に書く

**Completion criteria**:

- 中間モデルに入る Excel 由来の値が、テスティングフレームワークが解釈したあとの値（Java null または `String`）になっている
- 症状 4 件（`null` → 文字列 `null` ／ `"null"` → Java null ／ `"""` の再読込例外 ／ `\r` の 2 文字化）が、いずれも実ファイル起点で再現しなくなっている
- `${systemTime}` 等が記法のまま保たれている（解決していない）
- 直す前は落ちて直したあとは通るテストが挙がっている
- 期待値をわざと崩すと落ちることを確認した記録が報告にある

---

### #33: 2-2 —— 全フィールドが空文字のレコードを Excel 形式へ書き戻せるようにする

**Purpose**: `5783b35` の `implementation/testdata_examples.rst:2231`「全フィールドが空文字のレコードは、いずれか1つのフィールドに ``""`` と記述する。全セルを空にした行は読み飛ばされ、レコードにならないためである。」記載例（同 `:2237`-`:2260`）を往復させるとレコードが 1 件消える（原本 3 件・XLS→XLS 後 2 件・XLS→YAML→XLS 後 2 件）。

**Prerequisites**: #32（原因は同じ）

**指示書の訂正（2026-08-27・`a16be0a`）**: 旧版の「**テーブルと `LIST_MAP` は対象外**（全要素が空のエントリを記述する記法が無い）」は誤りであり、ディレクターが実測で訂正した。**Excel 形式では各セルに `""` と書けば表せる。**本体は空エントリの判定を**解釈前の生セル**で行うため（`nablarch-testing@3c4bd2a` の `PoiXlsReader.java:92`・`TestDataParsingTemplate.java:180`。どちらも `interpret` の前）、`""`（2 文字）は空セルに当たらず、エントリは読み飛ばされない。**テーブルと `LIST_MAP` も 2-2 の対象である。**

ディレクターの実測（2026-08-27。POI で組んだ実 `.xlsx`。`SETUP_TABLE=T`／カラム行 `[no]`,`id`,`name`／データ行 3 件 = `1,U0001,yamada` ／ `(空),"",""` ／ `3,(空),(空)`）: 本体 `PoiXlsReader#readLine` は **3 件**返し、`XlsFormatReader#read` は **1 件**（`U0001,yamada` だけ）しか返さない。2 件目が消えるのは `XlsFormatReader:664` の `isEmptyCell` が `""`・`””` を空セル扱いするため。3 件目が消えるのはマーカーカラム除外後に判定しているため（**XLS-08。こちらは現状のままでよく、本タスクでは直さない**——ユーザー確定・2026-08-27。`tools/testdata_converter.rst:63` のとおり往復で消えることが解説書に明記されている）。

**XLS→YAML 方向は現状の解説書では欠落する**（`testdata_notation.rst:1500`。YAML 形式では全値が空文字の要素はスキップされる）。**#37（完了条件3）で `@Ignore` ＋ 印つきの理由に記録する。**

**`@Ignore` の理由文の訂正（ユーザー確定・2026-08-27）**: この欠落を「YAML 形式では表せない」と書かない。**解説書の中の矛盾であり、是正の対象である。**`tools/testdata_converter.rst:14` は「両者の間に、テスティングフレームワークの仕様上の意味だけを持つ中間モデルを置く。Excel 形式と YAML 形式は、その意味をそれぞれの記法で表したものとして扱う」、同 `:22` は「往復したとき……仕様上の意味は変わらない」と定めている。同じ意味を表す 2 つの記法である以上、読み飛ばしの規則が形式で違ってよい理由がない。**ディレクターが `implementation/testdata_notation.rst:1500` を Excel 側へ揃える方向で直す**（記法として空のエントリだけを読み飛ばす。Excel は全セルが空セル、YAML は空マッピング `{}`。`""` と書かれた空文字は値として残す）。**解説書と `nablarch-testing-yaml` はディレクターが直す。converter 側からは触らない。**したがって #37 の理由文は次の書式にする。

    @Ignore("NTF-DOC: implementation/testdata_notation.rst:1500 — 空エントリ規則が Excel と YAML で不一致。Excel 側へ揃える是正待ち。期待 X / 実際 Y")

**KEY 列の扱い（ユーザー確定・2026-08-27）**: 指示書の「この回避が不要になる可能性がある」は誤りとして落ちた。**`XlsFormatReaderCellTypeTest` の `KEY` 列は残す。**理由は上の着手前特定のとおり本体 `PoiXlsReader#isBlankLine` にあり、converter の `isEmptyCell` を直しても不要にならない。

**#33 着手前特定の結果（2026-08-27。ユーザーへ報告済み）**: **期待値を変える既存テストは 0 件。**

- `isEmptyCell` を「生セルが `null` か空文字のときだけ空」へ変え、他を触らずに `mvn -o clean test` を実行した結果は `Tests run: 614, Failures: 5, Errors: 0, Skipped: 2` で、**赤 5 件は着手前と同一**（同じメソッド・同じ差分メッセージ）だった。変更は実行後に revert 済み
- **変えない 3 件**（`""` を含むデータ行を持つ既存テストの全件）—— `XlsFormatReaderTest#readTableNormalizesExcelQuotationNotation:171`（`""`, `"abc"`, `${expr}`）／`#readListMapNormalizesExcelQuotationNotation:196`（`""`, `"val"`）／`#readFixedFileNormalizesExcelQuotationNotation:222`（`(空)`, `""`, `"x"`）。いずれも同じ行に空でないセルがあり、現在も空エントリと判定されていない
- `isEmptyCell` が真を返す引用符記法は**ちょうど 2 文字の `""`・`””` だけ**である（`stripQuotes(cell).isEmpty()` が真になるのはこの 2 形のみ）。`XlsNotationSymmetryTest:185`・`:239`・`:293` の `"""` は 3 文字で対象外。参照フィクスチャ `ProjectActionRequestTest.xlsx` の `sharedStrings.xml` にも `""`・`””` のセルは 0 件
- **指示書の想定と食い違った点 1 件** —— `XlsFormatReaderCellTypeTest` の `KEY` 列は**外せない**。置いている理由は converter の `isEmptyCell` ではなく本体側で、`nablarch-testing@3c4bd2a` の `PoiXlsReader.java:93` が `isBlankLine`（同 `:140`-`:147`。`e.isEmpty()` で生セルを判定）に当たる行を捨てるため、空セルのケースは converter へ届く前に落ちる。同クラスに `""` を使うケースは 0 件
- **書き出し側の波及先も 0 件** —— 全要素が空文字のエントリをモデルに持つ既存テストは xls 側に存在しない（`src/test/.../xls/` と `RoundTripTest` に対する `asList("")`・`singletonList("")`・`List.of("")` の grep が 0 件）。`[""]` を持つのは YAML 側の 3 箇所（`YamlFormatWriterTest:597`／`YamlFormatReaderTest:177`／`YamlFormatReaderInvalidInputTest:825`）だけで、いずれも `XlsFormatWriter` を通らない

**Steps**:

- [x] **着手前に特定する（実装より前に結果を報告する）**——`isEmptyCell` の変更で期待値が変わる既存テストの全件（`""` を含むデータ行を持つテスト）。`XlsFormatReaderCellTypeTest` が置いている `KEY` 列の回避が不要になるかも見る。**変える／変えないの判断と件数を報告に書く**（**完了・2026-08-27。変える 0 件／変えない 3 件。`KEY` 列の回避は維持**。詳細は下の「#33 着手前特定の結果」）
- [ ] `XlsFormatReader#isEmptyCell` が `""`・`””` を空セル扱いするのをやめる。本体と同じく、生セルが `null` か空文字のときだけ空とする（テーブル・`LIST_MAP` の両経路に効く）
- [ ] ファイル・メッセージのデータ行を Excel 形式へ書き出すとき、全要素が空文字になる行は先頭要素を `""` と書く
- [ ] テーブル・`LIST_MAP` のエントリを Excel 形式へ書き出すとき、全要素が空文字になるエントリは**各セルへ `""` と書く**
- [ ] 直す前に落ちて直したあとに通るテストを用意する
- [ ] 期待値をわざと崩すと落ちることを 1 度確認する

**Completion criteria**:

- `testdata_examples.rst:2237`-`:2260` の記載例を XLS→XLS・XLS→YAML→XLS で往復させて、テスティングフレームワークが読むレコードが 3 件のまま保たれる
- `""` だけからなるテーブル・`LIST_MAP` のエントリが XLS→XLS で保たれる（本体 `PoiXlsReader` が読む件数と一致する）
- `isEmptyCell` の変更で期待値が動いた既存テストが、変えた／変えなかったの判断つきで全件・件数つきで挙がっている
- 直す前は落ちて直したあとは通るテストが挙がっている

---

### #34: 2-3 —— 中間モデルから Excel 形式の書式（`[ ]`）を外す

**Purpose**: 中間モデルが持つのは「テスティングフレームワークの仕様上の意味だけ」（`tools/testdata_converter.rst:14`）。グループ ID を囲む半角角括弧は Excel 形式の書式であって値ではない。現状は壊れていないが、モデルの持ち方が正しくない。

**Prerequisites**: なし

**着手前の検証**: **完了・ユーザー判定済み（2026-08-27）**。全走査の結果は `checks/step4-report.md` §1-2（`src/main` 11 箇所・既存テストの期待値 44 件）。判定は下表のとおり。

**指示書の訂正（2026-08-27）**: 旧版の「`[ ]` を付けるのは `XlsFormatWriter.marker` の中だけにする」は誤りであり、ディレクターが実測で訂正した（`nablarch-document@0d9a049` の `.rn/20260724-ntf-yaml-support/ntf-step4-05-nablarch-testing-converter.md` 2-3）。**`[ ]` を知ってよいのは次の 2 層だけ**にする。

| 層 | 何を知るか | どこ |
|---|---|---|
| A Excel 版面の読み書き | `[ ]` は Excel 形式の書式である | 付ける＝`XlsFormatWriter.marker`／外す＝`TestCoreReaderAdapter.markerGroupId` |
| B 上流 API の境界 | 上流が整形済みグループ ID を要求する | `TestCoreReaderAdapter`・`YamlTestCoreAdapter` の各公開メソッドが**生値で受け取り、上流へ渡す直前に整形する** |

層 B が要る理由は、変更禁止の上流 2 つが API 境界で整形済み（`[g1]`）を要求するため（`nablarch-testing@3c4bd2a` の `GroupDataParsingTemplate.java:41`-`:42` の前方一致 ／ `nablarch-testing-yaml@0b3015c` の `YamlSection.groupMatches:281`-`:284` の `equals` 比較）。整形は `XlsFormatReader`・`YamlFormatReader` には置かない（リーダー側に置くと `YamlFormatReader` が `[ ]` を組み立てるままになり、本タスクが消そうとしている依存が残るため）。

**Steps**:

- [ ] `YamlFormatReader.formatGroup:485`-`:488` の `[ ]` 付与をやめ、モデルへは生値を入れる（`:169`・`:203`・`:236`・`:299`）。**上流呼び出しのためにも組み立てない**
- [ ] `YamlFormatWriter.rawGroup:479`-`:487` の推測剥がしをやめ、生値をそのまま書く
- [ ] `XlsFormatReader` は `[ ]` を扱わない。`header.getGroupId()`（生値）をそのまま各アダプタへ渡す（**#32 の着手前検証 (a) で挙げた 7 つの行範囲は変更不要**）
- [ ] 層 A ——`TestCoreReaderAdapter.markerGroupId:282`-`:286` で `[ ]` を外す／`XlsFormatWriter.marker:529`-`:531` で `[ ]` を付ける
- [ ] 層 B ——`TestCoreReaderAdapter`・`YamlTestCoreAdapter` の各公開メソッドが生値で受け取り、上流へ渡す直前に整形する。整形の式は `groupId == null || groupId.isEmpty() ? "" : "[" + groupId + "]"` の 1 つに揃える（生値の空文字は「グループ指定なし」）
- [ ] **整形しない例外 2 件** ——`TestCoreReaderAdapter.readBlockBodyLines`（`markerGroupId` の出力との内部比較であり両側とも生値になるため）／`YamlTestCoreAdapter.readSendSyncMessages`（上流 `YamlMessageBuilder.buildSendSyncBodies`（`0b3015c:150`-`:163`）が**生値で**比較するため。同 `:140`-`:141` の Javadoc が明記）
- [ ] 層 B を持つメソッドの Javadoc を、生値を受ける旨へ書き直す（`YamlTestCoreAdapter:114`・`:143`／`TestCoreReaderAdapter:212`・`:230`-`:231`・`:259`／`XlsFormatWriter:524`／`TestDataBlock:77`）
- [ ] 既存テストの期待値 44 件のうち、変えたもの・変えなかったものを件数つきで報告に書く（`YamlTestCoreAdapterTest:74`・`:76` の 2 件は**生値へ変わる**。旧版の「変えない 2 件」は誤り）
- [ ] **`TABLE[]=x`（空のグループ ID）は追わない。** 生値化すると往復後に `TABLE=x` になる。この観測できる出力の変化を報告に 1 行書く
- [ ] 期待値をわざと崩すと落ちることを 1 度確認する

**Completion criteria**:

- 中間モデルの `groupId` が生値である（`[ ]` を持たない）
- 4 種のグループ ID（省略・単一・複数・送信系）で 4 経路の往復が壊れていない
- `[ ]` を知るのが層 A・層 B の 2 層だけに閉じており、`XlsFormatReader`・`YamlFormatReader` が `[ ]` を扱わない
- 既存テストの期待値を変えた箇所・変えなかった箇所が全件・件数つきで挙がっている

---

### #35: 2-4 —— 解説書に記述の無い「あるべき姿」を追う `@Ignore` 2 件を削除する

**Purpose**: どちらも `解説書に無い書き方は直さない・テストしない` に反する。他責先がリリース済みの `nablarch-testing` で直す予定も無いため、置いておくと永久に赤いままの宿題になる。

**Prerequisites**: なし

**Steps**:

- [ ] `YamlFormatReaderInvalidInputTest:740` `failsToReadRecordFragmentRowWithMoreValuesThanFields` を削除する（`5783b35` の `testdata_notation.rst:891` はパディングとバイナリデータの記述で、`@Ignore` の主張は無い。超過値を黙って捨てる挙動は論点4 として user 判断済み・現行どおりで仕様）
- [ ] `YamlFormatReaderInvalidInputTest:1280` `keepsOriginalColumnCaseInTable` を削除する（`5783b35` の `ja/development_tools/testing_framework` 全走査でテーブルのカラム名の大小についての記述は 0 件）
- [ ] 同ファイルの Javadoc や他のテストからこの 2 件を `{@link}` で参照している箇所を全走査し、参照ごと外す
- [ ] **削除するのはこの 2 件だけ**であることを確認する

**Completion criteria**:

- 2 件のテストメソッドと、その `{@link}` 参照が残っていない
- 他の `@Ignore` を消していない

---

### #36: 2-5〜2-7 —— 依存先 `nablarch-testing-yaml` の Step 4 是正に追随する（着手時点の赤 5 件）

**Purpose**: 着手時点で赤い 5 件はいずれも「converter 側のテストが、解説書に反する旧挙動を期待値に書いている」ものであり、`src/main` の欠陥ではない。5 件とも解説書どおりの期待値へ直す。

**Prerequisites**: なし

**やらないこと**: `nablarch-testing-yaml` の是正を評価し直さない／`YamlTestCoreAdapter` の未使用メソッドを整理しない／`YamlTestCoreAdapter#isResourceExisting` そのものは消さない。

**Steps**:

- [ ] **2-5** `YamlTestCoreAdapter:93`-`:100` の Javadoc を、入れ物（ディレクトリ）の存在を返す旨へ直す（委譲先は `YamlLoader#isResourceExisting`。`nablarch-testing-yaml@0b3015c` の `YamlLoader.java:184`-`:186`・`:165`-`:178`。読み込み単位の存在は `:200`-`:202` の `isDataExisting`）
- [ ] **2-5** `YamlTestCoreAdapterTest:364`-`:371` を新仕様へ直し、メソッド名を「入れ物の存在を映す」ことが分かる名前へ変える。押さえるのは 3 点 ——（入れ物ディレクトリが在る → `true`。読み込み単位名が実在しない `noSuchFile` でも `true`）／（入れ物ディレクトリが無い → `false`）／（`/` を含まない `resourceName` は全体を入れ物名として扱う。`YamlLoader.java:171`-`:172`）
- [ ] **2-6** `YamlFormatReaderScalarTest` の `readValueLine:159`-`:175` ／ `readListMapValue:184`-`:194` が組み立てる行に、空でない値を持つカラムを 1 つ足す（例: `K: "x"` を先に置き、検証対象は `V` に置く）。取り出す値は従来どおり `V` 列
- [ ] **2-6** `readValue`・`readValueLine`・`readListMapValue`・`readBlockScalarValue` の呼び出し元を全走査し、`:172`・`:173`・`:191`・`:192` の期待値と取り出す列の位置（`:174`・`:193`）を漏れなく直す。**何件直したかを報告に書く**
- [ ] **2-6** 「すべての値が空文字の行は読み飛ばされる」こと自体を押さえるテストを 1 件足す（`testdata_notation.rst:1500`。`{}` の行と、値が `""` だけの行の両方を含める）
- [ ] **2-7** `YamlFormatReaderInvalidInputTest:601`・`:628` の期待値を、`{}` の行だけが読み飛ばされ 2 行目が残る形へ直す（テーブル経路は `columnNames` が `["A"]`・行が 1 件（`["1"]`）。**実際に走らせて観測した値で書く**）
- [ ] **2-7** メソッド名（`dropsAllRowsWhen...` ／ `keepsRowCountButLosesValuesWhen...`）と Javadoc（`:582`-`:588`「最も損失が大きい形」「2 行目に書いたデータも消える」）を、欠陥の名前でないものへ直す
- [ ] **2-7** `YML-04` を参照している箇所を全走査し、参照ごと整理する。`coverage/issues.md` は **YML-04 が解消済みであることを 1 行追記するに留める**（記録の書き換えはしない）
- [ ] 赤 5 件それぞれについて、直したあとの期待値と、そう決めた根拠（解説書の `file:line` と実測値）を報告に書く
- [ ] 直したテストそれぞれについて、期待値をわざと崩すと落ちることを 1 度確認する

**Completion criteria**:

- 着手時点の赤 5 件がすべて通る
- 5 件それぞれの新しい期待値に、解説書の `file:line` と実測値の根拠が付いている
- 2-6 のヘルパー修正で期待値が動いた呼び出し元が、件数つきで全件挙がっている
- 「すべての値が空文字の行は読み飛ばされる」正のテストが 1 件ある
- `coverage/issues.md` の YML-04 に解消済みの 1 行が入っている（書き換えていない）

---

### #37: 完了条件3 —— 母集合が 4 経路で保たれることを実ファイル起点で押さえる

**Purpose**: `RoundTripTest` は中間モデルを起点に中間モデルへ戻すため（クラス Javadoc `:43`）、記法⇄値の写像が非対称でも書きと読みが同じ非対称なら緑になる。**この確認の代わりにならない。** 実ファイルを起点にし、テスティングフレームワークが解釈したあとの値で比べる。

**Prerequisites**: #32・#33・#34

**母集合**: `5783b35` の `implementation/testdata_notation.rst` の特殊記法の表（Excel 形式 12 行 `:1356`-`:1391` ／ YAML 形式 12 行 `:1408`-`:1443`。指示書の「13 行」は `0d9a049` で 12 行へ訂正済み）と、`implementation/testdata_examples.rst` の「null・空文字・改行など特殊な値を記述する」の各記載例。

**Steps**:

- [ ] 母集合の各行・各記載例について、**XLS→XLS・XLS→YAML→XLS・YAML→YAML・YAML→XLS→YAML の 4 経路**で、テスティングフレームワークが解釈したあとの値が往復前と一致することを、実ファイル起点のテストで押さえる
- [ ] 一致しないものは `@Ignore` ＋ 印つきの理由で記録する（直さない）。**全要素が空文字のエントリが XLS→YAML で落ちる件の理由文は #33 の「`@Ignore` の理由文の訂正」に従う**（「YAML 形式では表せない」と書かない）
- [ ] 表の行ごと・記載例ごとに 4 経路それぞれの合否を報告に書く
- [ ] #32 で新設した `XlsNotationSymmetryTest`（8 件）を `coverage/inventory.md` の軸要素対応表へ載せるかを、本タスクの母集合と重ね合わせて判断し、載せる場合は載せる（ユーザー指示・2026-08-27）
- [ ] 期待値をわざと崩すと落ちることを 1 度確認する

**Completion criteria**:

- 4 経路それぞれの合否が、表の行ごと・記載例ごとに全件挙がっている
- 起点が実ファイルであり、比較がテスティングフレームワークの解釈後の値である（セルの見た目ではない）
- 一致しないものが `@Ignore` ＋ 印つきの理由で記録されている

---

### #38: 第3節 —— テスト追加 11 件

**Purpose**: いずれも解説書に記述があり、既存テスト 605 メソッドが押さえていないもの（`60d9a2d` 実測）。既に押さえているものを二重に書かない。

**Prerequisites**: なし（#32〜#34 と独立に書けるが、実行は是正後）

**Steps**（解説書は `5783b35` の `tools/testdata_converter.rst`）:

- [ ] **3-1** `:53`-`:55` —— 3-2〜3-5 で `YamlTestDataValidator` が報告する種類の不正な YAML を変換元にしても、`TestDataConverter.convert` が検証を理由に失敗しない
- [ ] **3-2** `:59` —— セルの背景色・書式・結合セルを設定した Excel を xls→xls で往復させると、往復後のセルにその色・書式・結合が無い（**負のテスト**）
- [ ] **3-3** `:59` —— コメント行を含む YAML を yaml→yaml で往復させると、往復後にコメントが無い（**負のテスト**）
- [ ] **3-4** `:176` —— 変換元が YAML 形式のとき `excludeSheets` を指定しても、変換件数と出力内容が指定しないときと一致する（エラーにもならない）（**負のテスト**）
- [ ] **3-5** `:233` —— 直下とサブディレクトリの両方に不正な YAML を置いて `validate` すると、返る `ValidationError` は直下のぶんだけになる。直下に `.yaml` を持たない上位ディレクトリを指定すると空リストが返る
- [ ] **3-6** `:251`-`:254` —— `withTestShotsHeaderColor(x)` で識別子 `testShots` の `LIST_MAP` のヘッダ行の背景色が `x` になる
- [ ] **3-7** `:259`-`:262` —— `withExpectedHeaderColor(x)` で `EXPECTED_` 始まりと `RESPONSE_` 始まりのヘッダ行の背景色が**どちらも** `x` になる
- [ ] **3-8** `:263`-`:266` —— `withOtherHeaderColor(x)` で `MESSAGE` と `testShots` 以外の `LIST_MAP` のヘッダ行の背景色が `x` になり、`testShots` の `LIST_MAP` は変わらない
- [ ] **3-9** `:275`-`:278` —— `withMaxColumnWidthChars(n)` が効く。**上限文字数が実際に列幅を打ち切ること**（既定 20 に対し 30 文字の値を持つ列が 20 文字相当で頭打ちになること）も押さえる
- [ ] **3-10** `:287`-`:290` —— `withDisplayGridlines(true)` で**出力したシートのグリッド線表示がオンになる**。既定（`false`）ではオフになる
- [ ] **3-11** `:239` —— `ExcelFormatConfig` を設定した `ConversionRequest` で `to=yaml` の変換を実行しても、出力 YAML の中身が設定なしの場合と一致する（**負のテスト**）
- [ ] 落ちたものは直さず `@Ignore` ＋ 印つきの理由にする。理由の文言をそのまま報告に載せる
- [ ] **3-2・3-3・3-4・3-11 の 4 件（負のテスト）は特に念入りに**、期待値をわざと崩すと落ちることを確認する

**Completion criteria**:

- 11 件すべてについてテストが存在する
- 通ったもの・`@Ignore` にしたものの内訳が挙がっている
- `@Ignore` の理由が印つき（`NTF-DOC:` ＋ 解説書の `file:line` ＋ 期待／実際）である
- 負のテスト 4 件について、崩すと落ちることを確認した記録がある

---

### #39: 完了条件の締め —— 全件緑・カバレッジ計測・報告のまとめ・push

**Purpose**: 指示書「4. 完了条件」の残り（7・8・9・10）と「6. 報告」を締める。

**Prerequisites**: #32〜#38

**Steps**:

- [ ] `mvn -o clean test` が緑であること（`@Ignore` を除く）を確認する
- [ ] カバレッジ C0/C1 を計測し、`src/main` の是正で下がった箇所があれば挙げる
- [ ] `checks/step4-report.md` の §2〜§6 を埋める（指示書「6. 報告」の 6 項構成）
- [ ] 既存テストの期待値を変えた箇所を、どれを変えどれを変えなかったかも含めて件数つきで全件挙げる
- [ ] `git status --short` が空になることを確認する（一時ファイル・作業用スクリプト・ログを残さない。`jacoco.exec` と `target/` は `.gitignore:1`・`:3`）
- [ ] 変更を push する

**Completion criteria**:

- `mvn -o clean test` が緑（`@Ignore` を除く）
- カバレッジ C0/C1 の計測結果が報告にあり、下がった箇所が挙がっている
- 報告の 6 項がすべて埋まっている
- `git status --short` が空
- push 済み

---

# State

(written by /rn:dn, read and reset to this placeholder by /rn:up. `Status` is `paused` while a
session is suspended — the signal /rn:up and /rn:dn search for — and resets to `not suspended` here,
so only a genuinely suspended session reads `paused`.)

- **Status**: not suspended
- **Date**: -
- **Last completed**: -
- **Next**: -
- **Notes**: -
