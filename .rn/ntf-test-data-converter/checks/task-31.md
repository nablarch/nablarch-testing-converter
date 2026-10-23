# task-31 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 可変長の `FileDataBlock` を `length` 付きで生成できない（`"-"`・空文字も含む）。拒否は中間モデルの生成時であって書き出し側の番人ではない | OK | `ModelPreconditions#requireNoLengths(List<RecordLayout>, String)` を新設し、`FileDataBlock` のコンストラクタが導出したファイル種別で分岐して呼ぶ（`FIXED` → 既存の `#requireLengths`／`VARIABLE` → `#requireNoLengths`）。担保は `FileDataBlockTest#可変長ファイルでフィールド長を持つフィールド定義は保持できない`（`SETUP_VARIABLE` ＋ `length="10"`）と `#フィールド長を持つフィールド定義は可変長系のデータ種別すべてで拒否される`（可変長系 2 種 × `"10"`／`"-"`／空文字 ＝ 6 通り。メッセージに `フィールド名=[id]` が入ることも主張）。【赤】実装前の `mvn -o test -Dtest='FileDataBlockTest,YamlFormatReaderInvalidInputTest' -Djacoco.skip=true` は `Tests run: 52, Failures: 3`（新規 3 件がいずれも「IllegalArgumentException が送出されるべき」で失敗）。【緑】実装後の全量は `Tests run: 605, Failures: 0, Errors: 0, Skipped: 2`／BUILD SUCCESS。【変異試験 M1】`FileDataBlock` の `else { requireNoLengths(...) }` を削ると `Tests run: 605, Failures: 3` で **KILL**（落ちるのは新規 3 件だけ）。【変異試験 M2】`requireNoLengths` の条件を `!= null && !"-".equals(...) && !isEmpty()` に緩めると `AssertionError: … SETUP_VARIABLE length=[-]` で **KILL**。どちらも scratchpad へ退避してから `src/main` を書き換え、確認後に `md5sum` 一致で復元した（`FileDataBlock.java` = `828877cf424ac98836ee29232144f3ad`／`ModelPreconditions.java` = `27a0db7ae2a890e58257ff189ec7931e`） | | |
| 辺②の実ファイル経路で、可変長ファイルのフィールドに `length` を書いた YAML が落ちる（`loadRawMap` 差し替えの in-memory 経路は担保に数えない） | OK | `YamlFormatReaderInvalidInputTest#rejectsVariableFileFieldWithLengthFromRealYaml` を追加。同クラスは全件が `YamlFixture.read` 経由＝実 `.yaml` を書き出して本番配線（`YamlTestCoreAdapter` → `YamlLoader` → SnakeYAML Engine ＋ JSON スキーマ検証）を通す。`type: "variable"` のファイルの `fields` に `length: "10"` を書き、`IllegalArgumentException` とメッセージ（`可変長ファイルでフィールド長を持つフィールド定義は保持できません` ／ `フィールド名=[c1]`）を主張する。**スキーマ検証は現時点では通る**（本体スキーマが可変長の `length` を禁じていないため）ので、**今は converter の中間モデルの生成時に落ちる**。**スキーマ側の対応が入れば落ちる段がスキーマ検証まで前へ動き、例外が `YamlSchemaValidationException` へ変わるため、本テストは落ちる**（同クラスは `IllegalStateException` を継承しており `IllegalArgumentException` のサブクラスではない。nablarch-testing-yaml の `src/main/java/nablarch/test/core/reader/yaml/YamlSchemaValidationException.java:12`（`a5cb6dd` 時点）`public class YamlSchemaValidationException extends IllegalStateException`。`YamlFixture.read` が包み直さないことは、同じ経路でこの例外をそのまま受ける `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFieldTypeIsMissing`（同ファイル `:382`、ヘルパ `#assertSchemaViolation` は `:135`）から分かる）。**そのときは本テストを、スキーマ違反を主張する形（`assertSchemaViolation`）へ書き替える** | | |
| 固定長側の振る舞いと例外メッセージが変わっていない | OK | `ModelPreconditions#requireLengths` の本体は 1 文字も変えていない（`git diff` の当該メソッドの変更は Javadoc の 1 段落追加のみ）。既存の `FileDataBlockTest#固定長ファイルでフィールド長がnullのフィールド定義は保持できない` ／ `#フィールド長がnullのフィールド定義は固定長系のデータ種別すべてで拒否される` ／ `MessageDataBlockTest#フィールド長がnullの電文ブロックは生成できない` はいずれも無改変で PASS。電文（`MessageDataBlock`）は常に `#requireLengths` 側であり、`#requireNoLengths` の呼び出し元にならない | | |
| 辺③④のライタに番人も WARN も足していない／`YamlTestDataValidator` を改修していない | OK | `git diff --stat ba84c2d -- src/` が挙げるのは 7 ファイルだけで、`XlsFormatWriter.java` ／ `YamlFormatWriter.java` ／ `YamlTestDataValidator.java` はいずれも含まれない（`src/main` は `FieldDef.java` ／ `FileDataBlock.java` ／ `ModelPreconditions.java` の 3 件のみ） | | |
| `issues.md` XLS-45 の見出しと判定が「要対応・#31 で修正済み」になっており、旧判定（対応不要）が覆った経緯が残っている | OK | 見出しを「…**#27 で起票。#31 で修正済み**」へ改め、判定欄を `**要対応**（修正済み・#31）。**2026-08-24 に「対応不要」から変えた。**` とし、続けて【判定の訂正】に旧判定の全文を引用したうえで「旧判定は『可変長＋`length` を書ける入力を converter が受け取ってしまう』ことを所与にしていた／その所与が覆った」と書いた。起票時の但し書き（「中間モデルの課題ではない…生成時に拒否してはならない」）も削らず引用ブロックへ移し、前提ごと覆ったことを明記した | | |
| `issues.md` 冒頭の件数（要対応／対応不要）が導出コマンドから導き直されている | OK | 実行して得た値: `grep -c '^### \(XLS\|YML\)-'` → **57**／`grep -c '^- NTF 仕様としての判定'` → **57**／要対応 **27**／対応不要 **27**／保留 **2**／本作業の対象外 **1**（`27 + 27 + 2 + 1 = 57`）。和の検算 2 本（未分類・二重区分）はいずれも **0**。`grep -c '^- 判断: '` → **57**。前置の内訳は `XLS-` **43**／`YML-` **14**。冒頭の内訳・導出ブロックの併記出力・「食い違う 14 件」→ **13 件**（XLS-45 を除外）をすべて書き換えた | | |
| XLS-45 の申し送りが「禁止で確定」へ書き直され、申し送りの束の表が 4 件になっている（`steering.md` Rules の列挙も 4 件） | OK | 節見出しを「申し送り: …をどう扱うか」→「**可変長フィールドの `length` は禁止で確定（XLS-45。2026-08-24）**」へ改め、本文を問い（無視／禁止／任意）から確定事項の記録（converter は #31 で実装済み／本体スキーマは未対応／解説書は元から禁止と読める／禁止にする理由）へ書き直した。「申し送りの束」の表から行 5 を削って 4 行にし、外した理由を表の直後に 1 段落で残した。`steering.md` Rules の列挙も 4 件へ改めた | | |
| `axis-matrix.md` の辺③ 軸C C-21(省略) について、❌ が動くか動かないかが導出コマンドの出力とともに報告されている | OK | §3.3 直下の導出コマンドを実行し、**#31 の前は `✅ 11 ／ — 3 ／ ❌ 1`、#31 の後は `✅ 12 ／ — 3`（❌ 0）** を得た。状態欄を ❌ → ✅ へ改め、理由欄に「アサートは値に反応しないが、反応すべき対抗値が型として存在しないため穴ではなくなった」と、その根拠テスト 2 件を書いた。照合節は残し、見出しを「唯一の例外」→「唯一『値に反応しない』行」へ改めて、事実（15 行のうち本行だけが値に反応しない）が変わっていないことと、なぜ穴でなくなったのかを併記した。§5.2 の表（辺③ ✅ 70 → 71・❌ 1 → 0・合計 283 → 284・❌ 合計 1 → 0）と §5.2 直後・§6 の記述も導出コマンド（§0.6 の ②）の出力へ揃えた | | |
| `inventory.md` のテスト件数がコマンドから導き直され、出典コマンドが併記されている | OK | §0.1-2 に「追補その 12（2026-08-24 実測）」を追補その 11 と同じ形で追加。導出は既存の ①〜③ —— ① `grep -rc '^    @Test' src/test --include=*.java \| awk -F: '{s+=$2} END {print s}'` → **605**、② `grep -rn '^    @Ignore' src/test --include=*.java` → 既存 2 件（YML-14・XLS-40）、③ `git grep -c '^    @Test'` → `8c327d0: 536` ／ **`HEAD: 605`**（#31 の 1 コミットが載った状態の値。追補その 10 と同じ注）。`Tests run: 605` の実測も併記。追加 3 件・削除 0 件の内訳表と、既存 6 件の入力を直した旨（`XlsFormatWriterModelTest#writesLengthRowDecidedSolelyByDataType` の 1 件と `YamlFormatWriterModelTest` の 5 件。後者は共有ヘルパ `variableRecord()` を足して差し替え）も書いた | | |
| `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test` が全テスト PASS する | OK | `Tests run: 605, Failures: 0, Errors: 0, Skipped: 2` ／ `BUILD SUCCESS`（2026-08-24 11:00:48 実測）。`Skipped: 2` は既存の `@Ignore`（YML-14・XLS-40）で増減していない。JaCoCo は再計測していない（Rules） | | |
| 本体（`nablarch-testing`）・yaml（`nablarch-testing-yaml`）に書き込んでいない | OK | `git -C ~/work/nablarch/nablarch-testing status --porcelain` は出力なし（クリーン）。`git -C ~/work/nablarch/nablarch-testing-yaml status --porcelain` は `?? .rn/ntf-yaml/checks/task-21.md` を 1 件返すが、**別ワークストリーム（ntf-yaml の #21）の未追跡ファイル**である（mtime `2026-08-24 10:55:38`。#31 の作業では両リポジトリのファイルを 1 つも開いて書いていない。`nablarch-document` は読み取りのみ） | | |

## 台帳の宣言値のずれ（#31 で導き直したもの）

**`src/test` を触ったため `axis-matrix.md` ／ `inventory.md` の `sh` ブロックを全数流し直した**
（`axis-matrix.md` の §0.6 の取り決め。JaCoCo の 3 ブロックは Rules により再計測しない）。
**#31 が動かした値のほかに、#29 ／ #30 ／ §6-K のぶんが宣言値へ反映されないまま残っていた箇所が
4 つあった。**いずれも導き直して併記した。

| 場所 | 旧宣言値 | 実測値 | 動いた理由 |
|---|---|---|---|
| `axis-matrix.md` §0.4 の `IllegalArgumentException` 送出箇所 | 19 | **21** | #30 で `TestDataBlock#requireDataTypeOf` の `null` 分岐（＋1）、#31 で `#requireNoLengths`（＋1） |
| `axis-matrix.md` §0.6 の抽出／照合対象／対象外 | 323 ／ 290 ／ 33 | **326 ／ 292 ／ 34** | #30 のぶんが未反映（322 ／ 289 ／ 33）＋ #31 のぶん |
| `axis-matrix.md` §3 柱書 `XlsFormatWriterModelTest` | 12 ／ 15 | **13 ／ 17** | #29 が同クラスへ 1 件足したぶんが未反映。呼び出しが 14 件になり「テストメソッドと 1 対 1」でなくなった（#31 の `#writesLengthRowDecidedSolelyByDataType` が 1 メソッドで 2 回書き出すため） |
| `axis-matrix.md` §x.3 の見出し「（36 行）」×4 | 36 | **35** | #29 で C-10 が欠番になったぶんが未反映（§5.1 の 35 と食い違っていた） |
| `axis-matrix.md` §6.1 の `inventory.md` 照合 | 106 ／ 34 ／ 104 | **109 ／ 35 ／ 107** | #29 ／ #30 ／ #31 が `inventory.md` へ足した追補のぶん |
| `inventory.md` §2.1-2 の `YamlFormatReaderInvalidInputTest` 区間別 | 8 ／ 2 ／ 21 | **8 ／ 2 ／ 24** | 3 つ目の区間のみ。#31 のぶん 1 件と、それ以前の 2 件（**どのタスクのぶんかは未確認**） |
| `inventory.md` §3.1 の `XlsFormatWriterTest` 内訳 | `@Test=40 build=28 write=10 neither=2` | **`@Test=45 build=31 write=12 neither=2`** | `write` の ＋2 は `839bf64`（§6-K・0 件テーブル）の `roundTripsZeroRow…` 2 件。`build` の ＋3 が**どのタスクのぶんかは未確認** |

## Overall Verdict

- Self-check: OK
- QA: N/A（**#30 以降、レビュア subagent は回さない** —— `steering.md` Rules・ユーザー確定 2026-08-24）
- Design expert: N/A（同上）
- Craft expert: N/A（同上）
- Verification expert: N/A（同上）
- Ready to check off: Yes
