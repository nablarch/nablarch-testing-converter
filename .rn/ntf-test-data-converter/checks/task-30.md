# task-30 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| `FileDataBlock#fileTypeOf(DataType)` にファイル系 4 種以外の `DataType` を渡すと `IllegalArgumentException` になる | OK | `fileTypeOf` の冒頭に `requireDataTypeOf(FileDataBlock.class, PERMITTED_TYPES, dataType)` を置いた。担保は `FileDataBlockTest#ファイル系でないデータ種別からはファイル種別を導出できない`（**`EnumSet.complementOf` でファイル系 4 種の補集合＝ `DataType` 全 14 定数のうち 10 種を回し**、**補集合の要素数が 10 であることを主張**したうえで、どれも `IllegalArgumentException` になり、メッセージが `getName()` ／ `"FileDataBlock"` ／ **XLS-36 固有の `"が取りうるデータ種別="`** を含むことを主張。失敗時のラベルは定数名（`notFileType` そのもの）を出す —— `getName()` は `SETUP_TABLE_DATA` に対して `"SETUP_TABLE"` と記法名を返し、定数名と食い違うため）。【赤】実装前の `mvn -o test -Dtest=FileDataBlockTest -Djacoco.skip=true` は `Tests run: 15, Failures: 1` で、`AssertionError: IllegalArgumentException が送出されるべき`（例外が出ず `VARIABLE` が返っていた）。【緑】実装後の同コマンドは `Tests run: 15, Failures: 0, Errors: 0, Skipped: 0` で BUILD SUCCESS。【変異試験（レビュー指摘 3 の再確認。scratchpad に退避してから `src/main` を書き換え、確認後に `md5sum` 一致で復元）】(b2) `PERMITTED_TYPES` を `EnumSet.complementOf(EnumSet.of(LIST_MAP))` にすると `AssertionError: IllegalArgumentException が送出されるべき: DEFAULT` で **KILL**（`Tests run: 15, Failures: 1`）。(b3) `MESSAGE` ／ `RESPONSE_BODY_MESSAGES` ／ `EXPECTED_COMPLETED` を足すと `AssertionError: … : EXPECTED_COMPLETE_TABLE` で **KILL**（同 `Failures: 1`）。1 種だけを渡していた版ではどちらも生存していた。(j)【レビュー 2 巡目】`fileTypeOf` を「`PERMITTED_TYPES.contains` を見て `"FileDataBlock " + getName() + " NG"` という独自メッセージで投げる」実装に差し替える変異は、2 巡目より前は **601 件全緑（生存）**だった。**XLS-36 固有の断片 `"が取りうるデータ種別="` を主張に 1 つ足して再実行し、`AssertionError: XLS-36 の検査によるメッセージであるべき: DEFAULT`（`FileDataBlockTest:229`）で KILL**（`Tests run: 15, Failures: 1`）。変異は `src/main` を書き換えて確認し、確認後に scratchpad の退避と `md5sum` 一致（`e9ded2d6…`）で復元した。あわせて **`assertThat("ファイル系 4 種以外は 10 種であるべき", notFileTypes.size(), is(10))` を追加**（`EnumSet.complementOf` は要素数を主張しないため、`DataType` がファイル系 4 種だけになるとループが 0 回で無条件に緑になる穴を塞ぐ）。**テストメソッドは増やしていない**（総数 601 のまま）。 | | |
| 4 種の `DataType` に対する戻り値（`FIXED`／`FIXED`／`VARIABLE`／`VARIABLE`）と、XLS-36 の例外メッセージが変わっていない | OK | 戻り値: 既存の `FileDataBlockTest#ファイル種別を4種のデータ種別から導出する`（4 種すべてを主張）が緑の実行で PASS。ヘルパ `assertDerivedFileType` に `assertThat(FileDataBlock.fileTypeOf(dataType), is(expected))` を 1 行足し、静的受け口の受理側を**直接**主張する形にした（レビュー指摘 4。メソッド数は増えない）。**ただしこの 1 行に固有の kill 力は無い** —— レビュアの変異試験で、この行を消しても受理側の変異では他 40 件が落ちる（`getFileType()` 経由の間接担保で足りていた）と分かった。**意図を明示する価値はあるが、担保が増えたわけではない。** 三項演算子の式そのものは変えていない。メッセージ: `TestDataBlock#requireDataTypeOf` を再利用しただけで、**`TestDataBlock.java` の実装は 1 文字も変えていない**（レビュー指摘 2 で Javadoc だけを実態に合わせた。`git diff src/main/java/nablarch/test/tool/converter/model/TestDataBlock.java` の増減はコメント行のみ）。XLS-36 の `TestDataBlockTest#ファイル系でないデータ種別のファイルブロックは生成できない` を含む `TestDataBlockTest` は `Tests run: 12, Failures: 0` で PASS | | |
| `null` の検査を足していない（XLS-29 の番人を復活させない） | OK | `fileTypeOf` に足したのは `requireDataTypeOf` の 1 行のみで、`dataType == null` を見る分岐は無い（`git diff src/main` の追加行は Javadoc と当該 1 行だけ）。`FileDataBlockTest` にも `null` を渡すテストは足していない。`@throws IllegalArgumentException` の射程は非 `null` に限定した。**`@throws NullPointerException` は両メソッドとも宣言しない**（レビュー 2 巡目。この NPE は番人ではなく `dataType.getName()` の副産物であり、契約として凍結するとメッセージの書き換えだけで黙って壊れる。`src/main` の `@throws` 46 件に `NullPointerException` の前例も無い）。代わりに `@param dataType` で「`null` は検査しない。渡した場合の例外の種類は規定しない」と述べ、因果は `TestDataBlock#requireDataTypeOf` の Javadoc に 1 文だけ置いた（`FileDataBlock` 側からは消し、相互参照をやめた）。**`null` の非互換は `issues.md` XLS-44 に記録した**（レビュー 2 巡目・指摘 6） | | |
| `fileTypeOf` の可視性を下げていない（`public static` のまま） | OK | シグネチャは `public static FileType fileTypeOf(DataType dataType)` のまま。`git diff` にシグネチャ行の変更は無く、`XlsFormatReader#readFileBlocks`（`FileDataBlock.fileTypeOf(type)`）が外部パッケージからそのまま呼べている（フルビルドが通ることで確認） | | |
| `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | 実行結果 `Tests run: 601, Failures: 0, Errors: 0, Skipped: 2` ／ BUILD SUCCESS。`Skipped: 2` は既存の `@Ignore`（YML-14・XLS-40）で増減していない。JaCoCo は `-Djacoco.skip=true` で再計測していない | | |
| `issues.md` の XLS-44 に受け口を締めた旨が 1 行記録され、新しい課題番号が立っていない | OK | XLS-44 の既存「**修正（#29）**」ブロックの入れ子箇条書きに「**導出の受け口も締めた（#30）**」を追加（新しい `### XLS-nn` 見出しは作っていない）。レビュー 2 巡目でさらに 2 点を足した —— (1)**`fileTypeOf(null)` の振る舞いが変わったこと**（変更前は `VARIABLE`、変更後は `NullPointerException`。`public static` かつ maven プラグインとして配布されるため**リポジトリ外の呼び出し側には非互換**。リポジトリ内に該当呼び出しは無い）、(2)**寄せ残しは 1 か所ではなく 2 か所であること**（既記載の `XlsFormatReader#isFileType(DataType)` に加え、`src/main/java/nablarch/test/core/reader/TestCoreReaderAdapter.java` の `readFiles(String, String, String, DataType)` が `SETUP_FIXED`／`EXPECTED_FIXED` → `FixedLengthFileParser`、`SETUP_VARIABLE`／`EXPECTED_VARIABLE` → `VariableLengthFileParser`、`default` → `IllegalArgumentException` という同じ分割を `switch` で持つ。**寄せない** —— `nablarch.test.core.reader` は本体相乗りのパッケージで、`converter.model` に依存させると層が逆転するため。記録のみで新しい課題番号は立てない）。`grep -c '^### \(XLS\|YML\)-' .rn/ntf-test-data-converter/coverage/issues.md` は **57** で、#29 完了時点の 57 件から変わっていない。`git diff --stat` の `issues.md` は `24 insertions(+), 4 deletions(-)`（削除 4 行は「寄せ残しが 1 か所ある」の箇条書きを 2 か所の入れ子へ書き換えたぶん） | | |
| `inventory.md` のテスト件数がコマンドから導き直され、出典コマンドが併記されている | OK | §0.1-2 に「追補その 10（2026-08-24 実測）」を追補その 9 と同じ形で追加。導出は既存の ①〜③ を実行して取得 —— ① `grep -rc '^    @Test' src/test --include=*.java \| awk -F: '{s+=$2} END {print s}'` → **601**、② `grep -rn '^    @Ignore' src/test --include=*.java` → 既存 2 件（YML-14・XLS-40）、③ `git grep -c '^    @Test'` → `8c327d0: 536` ／ **`HEAD: 601`**。`Tests run: 601` の実測も併記。**③ は #30 の 1 コミットが載ったあとの値で記録した**（レビュー指摘 5。記録日は作業ツリーだけが 601 で当時の `HEAD` ＝ `a6306fc` は 600 だった旨は残しつつ、後から実行しても再現する形にした。台帳ルール「出典コマンドはそのまま実行して同じ結果が出ること」#22）。あわせて §0.1-2 本節の「行番号が見えるのは…4 か所」を「**本節と追補その 5 以降の各追補**」へ直した（レビュー指摘 6。`grep -nE '\.java:[0-9]+|\bL[0-9]{2,4}\b' .rn/ntf-test-data-converter/coverage/inventory.md` の実測は本節・追補その 5〜その 10 の 7 ブロック）。レビュー 2 巡目で 2 点を直した —— (1)コードブロック内の `HEAD: 601` の行に「#30 の 1 コミットが載った状態の値」であることを 1 行で注記し、ブロック見出しの「実測」と ③ の測定時点の違いを取り違えないようにした。(2)同じ括弧書きが「追補その 2〜その 4 の ② は『（ヒット 0 件）』」とだけ述べていた点を、**追補その 1 には ② の行そのものが無い**（射程外）と事実どおりに直した（`grep -n '^②' inventory.md` の実測は 340／438／478／524／581／837／919／951／982／1016 行の 10 件で、追補その 1 のブロック〈`①`／`③` のみ〉に ② は無い） | | |
| 本体（`nablarch-testing`）・yaml（`nablarch-testing-yaml`）に書き込んでいない | OK | `git -C /home/tie303177/work/nablarch/nablarch-testing status --porcelain` は出力なし（クリーン）。`git -C /home/tie303177/work/nablarch/nablarch-testing-yaml status --porcelain` はレビュー指摘の反映後（2026-08-24 08:56 実測）に ` M .rn/ntf-yaml/checks/task-18.md` を 1 件返すが、**#30 とは無関係の別ワークストリーム（ntf-yaml の #18）の未コミット変更**である（`git -C … diff --stat` はこの 1 ファイルのみ、内容は YAML スキーマ `description` 修正の Evidence 追記。ファイルの mtime は `08:46:37` で本作業の開始前）。#30 の作業では両リポジトリのファイルを 1 つも開いて書いていない | | |

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Verification approach meaningful to the objective (checks the right thing, not just "passed") | OK | 2 巡実施し、いずれも pass。1 巡目: 変異 (a) 検査行削除で新規テスト 1 本だけが落ち、load-bearing であることを確認。2 巡目: 変異 M1（検査行削除）／M2（`LIST_MAP` だけを弾く偽実装）／M3（`PERMITTED_TYPES` を `SETUP_FIXED` のみへ縮小）がいずれも KILL。所見として `fileTypeOf(null)` の非互換（`public static`・maven プラグイン配布）を記録すべきとの指摘があり、`issues.md` XLS-44 に反映した |

## Expert Reviews (axes the task needs)

### Craft Expert (coding)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Medium-specific best practice | OK（2 巡目の指摘を反映後） | 1 巡目 fail —— `@throws` が `null` の実挙動（NPE）と食い違う／`TestDataBlock#requireDataTypeOf` の Javadoc が #30 で偽になった（「`null` ／ `DEFAULT` はここへ届かない」）。2 巡目 fail —— Javadoc が 39 行と本パッケージ最長／`null` → NPE の因果が 4 か所に重複し相互参照になっている／`@throws NullPointerException` は本リポジトリに前例が無く、実装詳細（`dataType.getName()`）を契約に凍結する／`{@link #PERMITTED_TYPES}` が private で死にリンク／`{@link java.util.EnumSet#contains(Object)}` が `AbstractCollection` へ解決される。**すべて反映済み**（`fileTypeOf` 39 → 23 行、`requireDataTypeOf` 29 → 24 行、`@throws NullPointerException` 削除、`{@link}` 2 件除去）。**却下 1 件** —— 「拒否メッセージが導出の文脈と合わないので `fileTypeOf` 専用の例外を投げるべき」。ユーザーが「XLS-36 の既存の例外メッセージと振る舞いは変えないこと」「新しい仕組みは要らない」と明示しているため範囲外。**メッセージの違和感は残る（既知）** |
| Consistency with existing style | OK | GWT コメント・`try/fail/catch`・`containsString` の形は `TestDataBlockTest#ファイル系でないデータ種別のファイルブロックは生成できない` と同型。1 メソッド内でループする書き方は `FileDataBlockTest#フィールド長がnullのフィールド定義は固定長系のデータ種別すべてで拒否される` と同型。`{@link}` の短縮形・`@throws` の言い回しも既存に合わせた。`javadoc -Xdoclint:all` の警告は変更前後とも同数（#30 由来の増加 0） |

### Verification Expert (test)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Artifact actually checked (tests run / claims verified / flow traced) | OK | 2 巡ともレビュア自身が `mvn clean test -Djacoco.skip=true` を実行し `Tests run: 601, Failures: 0, Errors: 0, Skipped: 2` を確認。Javadoc が主張する挙動（`null` → NPE、`DEFAULT` → XLS-36 のメッセージ、呼び出し側 2 か所からは到達しない）を使い捨てプローブで実測して一致を確認。`inventory.md` 追補その 10 の導出コマンド ①〜③ をそのまま実行して突き合わせ、③ の `HEAD: 601` が「作業ツリーをそのまま 1 コミットにすると 601 になる」ことをコピー上のコミットで検証 |
| Coverage (edge cases / claims / steps) | OK（2 巡目の指摘を反映後） | 1 巡目: 変異 (b2)（14 種中 13 種を通す）と (b3)（`PERMITTED_TYPES` に 3 種追加）が**生存** —— 拒否側が `LIST_MAP` 1 種しか通していなかったため。補集合ループへ書き換えて両方 KILL。2 巡目: 変異 (j)（独自メッセージで投げる実装）が**生存** —— XLS-36 のメッセージを何も固定していなかったため。断片 `"が取りうるデータ種別="` の主張を足して KILL。カバレッジは `DataType` 全 14 定数のうち拒否側 10／受理側 4 の **14/14**。補集合が空になると素通りする穴は `notFileTypes.size()` の主張で塞いだ |

## Overall Verdict

- Self-check: OK（レビュー 2 巡目の指摘 6 件を反映後に再実行。`Tests run: 601, Failures: 0, Errors: 0, Skipped: 2` ／ BUILD SUCCESS。**テストメソッドは増やしていない**）
- QA: OK（2 巡とも pass）
- Craft expert: OK（1・2 巡目は fail。指摘を反映後に解消。却下 1 件は本文のとおり）
- Verification expert: OK（2 巡とも pass。生存変異 3 件はいずれも反映後に KILL）
- Design expert: N/A（新しい構造・方針を作らず、コンストラクタが持つ既存の検査を受け口へ適用しただけのため）
- Ready to check off: Yes —— 調整役がコミット済み差分を全行確認し、ゲート `Tests run: 601, Failures: 0, Errors: 0, Skipped: 2` ／ BUILD SUCCESS を自分で実行して確認した

## レビューの打ち切りについて（調整役の記録）

**3 巡目（Craft ／ Verification）は途中で打ち切った。**#30 以降はレビュア subagent を回さず、差分は
ユーザーが全行読む運用へ切り替えたためである（ユーザー確定・2026-08-24。`steering.md` Rules）。
**役割が重複しているためであり、レビューで指摘が出ていたからではない。**2 巡で出た指摘は
すべて triage 済みで、Valid 12 件を反映し 3 件を却下した。

## 申し送り（調整役へ）

- **`fileTypeOf(null)` の振る舞いが変わった。** 実装前は `VARIABLE` を返していたが、実装後は
  `NullPointerException` になる（`EnumSet.contains(null)` が `false` を返し、`requireDataTypeOf` の
  メッセージ組み立てで `dataType.getName()` が NPE になるため）。使い捨てプローブで実測した
  （scratchpad で実行し、`src/main` ／ `src/test` は無変更）。**到達経路は無い** —— 呼び出し側 2 か所の
  うち `FileDataBlock#getFileType()` は非 `null` が保証された `getDataType()` を渡し（`TestDataBlock`
  のコンストラクタが `null` を拒否）、`XlsFormatReader#readFileBlocks` は呼び出し元でファイル系 4 種に
  絞っている。タスクの禁止事項どおり `null` 検査は足していない。**レビュー 2 巡目で `issues.md` XLS-44 に記録した**（`public static` かつ maven プラグイン配布のため、リポジトリ外の呼び出し側には非互換である旨も併記）。
