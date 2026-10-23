# task-27 Completion Check

計測時点は HEAD `356549a`（Step A の `issues.md` 修正を含む）。
コマンドはすべて `cd "$(git rev-parse --show-toplevel)"` から始めて実行した。

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| `axis-matrix.md` に辺①〜辺④の4表があり、軸A〜F の全要素が行として存在する | OK | 4 辺 × 6 軸 ＝ **24 個の軸小節**が実在する（`grep -cE '^### [1-4]\.[1-6] 軸[A-F]' axis-matrix.md` → `24`。見出しを列挙して 1.1〜1.6・2.1〜2.6・3.1〜3.6・4.1〜4.6 が軸A〜F の順で並ぶことも確認）。行数は §0.6 の ① と ⑤ を実行して 辺① **79** ／ 辺② **82** ／ 辺③ **77** ／ 辺④ **77** ／ 総計 **315**。軸ごとの内訳は `for ax in A B C D E F; do for n in 1 2 3 4; do awk "/^## \${n}\\. /,/^## \$((n+1))\\. /" axis-matrix.md \| grep -cE "^\\\| \${ax}[0-9-]"; done; done` で A ＝ 14/14/14/14・B ＝ 4/4/4/4・C ＝ 36/36/36/36・D ＝ 8/12/8/9・E ＝ 11/11/11/11・F ＝ 6/5/4/3 となり、**§5.1 の表と全 24 セルが一致する**（合計 79/82/77/77 も一致）。軸D・軸F の要素数が辺ごとに違うのは、ケース集合が辺ごとに定義されているためで、§5.1 が同じ値を宣言している | OK | 3 者とも OK。QA・Craft・Verification が独立に §0.6 の ①⑤ を実行し 79／82／77／77・計 315 を再現。Verification は 4 辺の ID を全列挙して `inventory.md` §0.2〜§0.7 の要素定義と突き合わせ、欠落なしを確認 |
| 各要素に担保テストメソッド名が記されており、記された全メソッド名がテストソースに実在する | OK | (a) **✅ の 283 行はすべて担保欄にメソッド名を持つ** —— `grep -E '^\| [A-F][0-9-]' axis-matrix.md \| awk -F'\|' '{st=$4; gsub(/ /,"",st); if (st=="✅" && $5 !~ /#/) print $2}'` の出力が **0 行**（担保欄が空・`—` のものを探す版も 0 行）。(b) **実在照合**は §0.6 の照合パイプラインをそのまま実行し、**出力 0 行**（＝ `NG(class)` ／ `NG(method)` ／ `NG(略記が解決できない)` が 1 件も無い）。(c) 抽出件数は §0.6 の `EXTRACT` で **320 ／ 288 ／ 32**、`FILTERED \| wc -l` が **27** で、いずれも §0.6 が書いている値と一致。`FILTERED` の実在照合も出力 0 行。**❌ 5 件・空欄 27 件には担保メソッドが無いのが正**であり、それぞれ次の行で理由を確認している | OK | 3 者とも OK。実在照合パイプラインの出力ゼロ・抽出 320 ／ 288 ／ 32・`src/main` 27 件 NG ゼロを 3 者が独立に再現。Verification は **§6 の除外を外した独立照合**も行い、NG は §6 が「HEAD に無い」と述べる 12 件と接頭辞 3 件だけで**未開示の不在ゼロ**であることを確認した |
| 空欄の要素には理由が書かれている | OK | `—` の 27 行で理由欄（第 7 列）が空のものを探す `awk` の出力が **0 行**、理由欄が 10 文字未満のものも **0 行**。あわせて **❌ 5 行の理由欄も全件埋まっている**（`C-21(省略)` 613 字／`A-06` 141 字／`A-07` 228 字／`A-08` 108 字／`A-09` 178 字。文字数は同じ `awk` で計測）。状態別の総数は §0.6 の ② で ✅ **283** ／ 🔺 **0** ／ ❌ **5** ／ `—` **27**（合計 315）で、§5.2 の表と一致する。空欄 27 件の分類（4＋8＋12＋1＋1＋1）は §5.3 が持ち、その合計 27 が §5.3 末尾の導出コマンドの出力と一致する | OK | 3 者とも OK。`—` 27 行すべてで理由欄が非空。Verification は `❌` 5 行を加えた 32 行で確認し、逆に `✅`／`❌` の行で担保メソッド欄が `—` の行がゼロであること・全 315 行が 6 列を保つことも確認 |
| `issues.md` に本作業で見つかった課題が一覧化されており、`git diff` 上 src/main への変更が **#25.5 で修正した課題（`issues.md` の判定が要対応であるもの）に限られている**ことが確認されている | OK | **下の「Step A の実施内容」に全量を記した。** 要点は 3 つ —— (1) `issues.md` を頭から末尾まで通読し、`checks/task-19.md`〜`task-26.5.md` と `steering.md` に現れる課題 ID を機械的に突き合わせた。**双方向とも未説明の差は 0 件**（片方向で見つかった XLS-02・XLS-03 は `356549a` で欠番の理由を追記して解消）。(2) `#18` チェックオフ `5bf7048` 以降に `src/main` を触ったコミットは **39 件**で、全件を `git show` で開いて分類した。**判定が要対応でない課題の修正は 1 件も無い。** (3) 判定が要対応の **26 件**のうち未実施は **XLS-44 の 1 件だけ**であることを、判定欄と修正コミットの対応から確かめた | OK | QA・Verification は OK。**Craft の NG は Invalid と判定した** —— 根拠は「確認したという記述が 3 つの成果物のどこにも無い」だったが、確認の記録は本ファイル（下の「Step A の実施内容」。`src/main` を触った 39 コミットの全数分類）にあり、レビュアには独立性のため `checks/` を読ませていない。Craft 自身も再レビューで「Invalid 判定を受け入れます」と述べている。3 者とも `git diff --stat 83137c2..HEAD -- src/main` が空であることは独立に再現した |

## Step A の実施内容（上表 4 行目の裏づけ）

### A-1. `issues.md` の通読と、課題の漏れの照合

**`issues.md` 全 4604 行（修正後）を先頭から末尾まで読んだ**（`grep` の拾い読みではない）。
そのうえで課題 ID を双方向に突き合わせた。

```sh
cd "$(git rev-parse --show-toplevel)"
D=.rn/ntf-test-data-converter
# ① checks(#19〜#26.5) と steering が挙げる ID のうち、issues.md に 1 度も現れないもの
grep -ho '\b\(XLS\|YML\|COV\)-[0-9]\+' \
    $D/checks/task-19.md $D/checks/task-2[0-6].md $D/checks/task-25.5.md \
    $D/checks/task-26.5.md $D/steering.md | sort -u > /tmp/m.txt
grep -ho '\b\(XLS\|YML\|COV\)-[0-9]\+' $D/coverage/issues.md | sort -u > /tmp/i.txt
comm -23 /tmp/m.txt /tmp/i.txt | wc -l          # → 0
# ② 逆向き: issues.md の各課題が、どのタスクの産物かを本文の ## 見出しが示しているか
awk '/^## /{h=$0} /^### (XLS|YML|COV)-/{print $2" <- "h}' $D/coverage/issues.md \
  | awk -F' <- ' '$2 !~ /#(19|20|21|22|23|24|25|25\.5|26|27)|§6|§7/'   # → 出力なし
```

- **① は 0 件、② は出力なし。** ② の `##` 見出しは 11 節あり、節ごとの課題件数は
  `awk '/^## /{h=$0} /^### (XLS|YML)-/{print h}' … | uniq -c` で
  #19 **3** ／ #20 **4** ／ #21 **6** ／ #22 **4** ／ #23 **5** ／ #24 軸D **3** ／ #24 掃引 **8** ／
  #25 **2** ／ #25.5 **9** ／ §6 **11** ／ #27 **2** ＝ **57** で、`grep -c '^### \(XLS\|YML\)-'` の 57 と一致する。
- 各タスクの `checks` が挙げる ID も節と一致した ——
  task-20 ＝ XLS-06/07/08/09、task-21 ＝ XLS-10〜15、task-22 ＝ XLS-16〜19、task-23 ＝ XLS-20〜24。
- **`COV-01`〜`COV-14`** は #26 の産物で、`issues.md` §7 の柱書が出典（#26 の計測 HEAD `da66425`）を明記している。
  `grep -c '^### COV-'` ＝ 14、`grep -c '^- 未到達箇所'` ＝ 14、分岐数の合計は
  `awk -F'|' '/^\| COV-[0-9]+ \|/ { s += $4 } END { print s }'` ＝ **19** で、§7 の表の記載と一致する。

**直した 1 件（`356549a`）。** ① の初回実行では **XLS-02 ／ XLS-03 の 2 件**が出力された。
`checks/task-19.md:256-257` が #19 の成果として記録しているのに、`issues.md` に節も言及も無かった。
実物を辿ると、`227adc1`（2026-08-13。`git merge-base --is-ancestor 227adc1 HEAD` で HEAD の祖先と確認）が
「converter の入出力は NTF が実行できるテストデータに限る」というユーザー確定に従って
**両課題を節ごと削除していた**（`git show 227adc1 -- …/issues.md | grep '^-### '` に
`-### XLS-02 …` ／ `-### XLS-03 …` が出る）。**判断の要る話ではなく、削除の事実が
`issues.md` に残っていないだけ**だったので、「対象としない入力」節に出典つきで追記した。
欠番がこの 2 件だけであることも機械で確かめてある（`XLS-01`〜`XLS-45` の欠けは 02・03 のみ、
`YML-01`〜`YML-14` と `COV-01`〜`COV-14` に欠けなし）。追記後も件数の導出コマンドの出力は不変
（57 ／ 要対応 26 ／ 対応不要 28 ／ 保留 2 ／ 対象外 1 ／ 判断欄 57 ／ 区分外 0 ／ 二重 0）。

### A-2. `src/main` への変更の全数と、その帰属

基準は **#18 のチェックオフ `5bf7048`**（フェーズ2 の実作業に入る直前）。
`git log --oneline 5bf7048..HEAD -- src/main | wc -l` → **39**。39 件すべてを `git show` で開いて分類した。

| 区分 | 件数 | コミットと帰属 |
|---|---|---|
| 判定が**要対応**の課題の修正（実装変更） | 32 | `36e94a4` YML-02 ／ `aec82f2` YML-12 1形目 ／ `e80a4dd` XLS-16 ／ `5721ecd` XLS-06 ／ `6c8d90e` YML-08 ／ `b9ff38e` XLS-22・YML-12 3形目 ／ `f0f8718` YML-03 ／ `f80c192` YML-12 4形目 ／ `04873de` YML-12 2形目 ／ `a794a8e` XLS-08 ／ `57c1b0d` XLS-27（当面）／ `44469b2` XLS-29 ／ `3000baf` XLS-30 ／ `d0023c0` XLS-31 ／ `5abc773` XLS-32 ／ `81cf234` XLS-33 ／ `7c10654` XLS-20 ／ `5ab13d8` XLS-28 ／ `b905183` XLS-34 ／ `836a2a4` XLS-35 ／ `1244e2b` XLS-36 ／ `5803fe6` XLS-37 ／ `d87bc0b` XLS-38 ／ `29c9d1d` XLS-40（フィールド名称側）／ `166a199` XLS-41 ／ `7201650` XLS-43 ／ `c31b534` XLS-22 の番人移設 ／ `7b0b381` XLS-29 の番人移設 ／ `9e40644` YML-12 2形目の番人移設 ／ `b762438` XLS-30 の番人移設 ／ `839bf64` XLS-27・XLS-21 ／ `becbe30` XLS-27（マーカー値 `[空]`→`[EMPTY]`） |
| Javadoc・コメントのみ（振る舞い無変更） | 5 | `b6be795`（YML-02・YML-08 の修正へ Javadoc を追随）／ `a667893`（`notation:nnn` を `30a8271` 基準へ貼り直し）／ `999f41d`（XLS-06・XLS-16 の根拠を明文へ差し替え。コミットメッセージも「振る舞いは変えない」）／ `54d2057`（番人移設後の Javadoc 追随）／ `d737815`（XLS-40 の番人を置かない理由を Javadoc へ。`ColumnRowDataBlock` ／ `ModelPreconditions` の Javadoc だけ） |
| 課題 ID に直接は紐づかない実装変更 | 2 | `51dbca0`（`YamlTestCoreAdapter` の `DEFAULT_GROUP_MARKER` の生 NUL を 8 進エスケープへ。同じ String になり挙動不変）／ `520e890`（#25.5 レビュー 1 巡目。B-1 同定数の値、B-2 シート名 `null` の番人、B-3 `formattedGroup` の重複削除）。**いずれも `36e94a4`（YML-02）が持ち込んだコードと `e80a4dd`（XLS-16）の番人に対する後始末**であり、要対応の修正の一部である |

- **判定が要対応でない課題の修正は 1 件も無い。** 上表の 32 件が触っている課題 ID は重複を除くと
  **XLS-06・08・16・20・21・22・27・28・29・30・31・32・33・34・35・36・37・38・40・41・43 ／
  YML-02・03・08・12 の 25 件**で、これは `issues.md` 冒頭の「#25.5 で修正済み 25 件」の列挙と**完全一致**する
  （両方をソートして `diff` を取り差分なし）。
- **`becbe30` だけが #25.5 の外（#26.5）で入った `src/main` の変更である。** 直しているのは XLS-27
  （判定 **要対応**）の修正が置いた定数 `EMPTY_BLOCK_MARKER_COLUMN` の値だけで、完了条件が課題の**判定欄**で
  範囲を定めている以上その内側にある。裏づけは `checks/task-26.5.md` の Completion Criteria 表。
- **#27 自身は `src/main` を 1 文字も触っていない。**
  `git diff --stat 0672a16..HEAD -- src/main pom.xml`（`0672a16` ＝ #26.5 のチェックオフ）が**空**。
  #27 が触った `src/test` は 3 件（`11df931` 辺② C-10 のアサート追加 ／ `783810b` 辺③ E-1(1件) ／
  `6d12021` 辺④ E-4(1件)）。

### A-3. 未実施の要対応が XLS-44 の 1 件だけであること

```sh
cd "$(git rev-parse --show-toplevel)"
F=.rn/ntf-test-data-converter/coverage/issues.md
awk '/^### (XLS|YML)-/{id=$2} /^- NTF 仕様としての判定/{ if ($0 ~ /\*\*要対応\*\*/) print id }' $F
```

出力は **26 件**。うち 25 件は判定欄そのものに「修正済み」と修正コミットが書かれており、
A-2 の 32 コミットが触った 25 件と**同一集合**である（`diff` で確認、差分なし）。
残る 1 件が **XLS-44** で、判定欄は `**要対応**（**未実施**。#27 では `src/main` を触らないため起票のみ）`。
**これ以外に未実施の要対応は無い。**

- **XLS-40 は「一部だけ実施」だが未実施の要対応には数えない。** フィールド名称側は `29c9d1d` で閉じ、
  カラム名側は帰属が `nablarch-testing` の `TableData` であるため**番人も WARN も入れないとユーザーが確定**し
  （2026-08-19）、あるべき姿のテストを `@Ignore` で残す形で決着している。`issues.md` 冒頭の内訳も
  同じ扱いで XLS-40 を「修正済み 25 件」に入れている。
- **YML-08 も同型である。** converter 側の 1 点（辺①との非対称）は `6c8d90e` で閉じ、
  `DataFile#setDirective` の `trim()` による損失は本リポジトリの外にあるため未解決のまま記録されている。
- `@Ignore` は 2 件で、いずれも他責の「あるべき姿」テストである
  （`grep -rn '^    @Ignore' src/test --include=*.java` → `YamlFormatReaderInvalidInputTest.java` の 2 行。
  `mvn` の `Skipped: 2` と一致する）。

### A-4. 本タスクで書いた主張と出典の照合

**上に書いた主張は、1 件ずつ出典を開いて確かめた。** 照合したのは次のとおり。

| 主張 | 開いた出典 |
|---|---|
| 対応表の行数・状態別件数（315／283／5／27、辺ごと 79・82・77・77） | `axis-matrix.md` §0.6 の ①②⑤ を実行し、§5.1・§5.2 の表と突き合わせた |
| 担保メソッドが全件実在すること | §0.6 の実在照合パイプラインと `FILTERED` を実行（どちらも出力 0 行） |
| 課題の件数（57／26／28／2／1）と書式（判断欄 57・判定欄 57・区分外 0・二重 0） | `issues.md` 冒頭「集計は課題 ID 単位で数えること」の全コマンドを実行 |
| §7 の `COV-nn`（14 件・未到達分岐 19 件） | `issues.md` §7 柱書の 3 コマンドを実行 |
| XLS-02・XLS-03 の削除 | `git merge-base --is-ancestor 227adc1 HEAD`／`git show 227adc1 -- …/issues.md`／`git show 227adc1^:…/issues.md`／`checks/task-19.md:256-257` |
| `src/main` を触った 39 コミットの帰属 | 39 件を `git show --stat`／`git show` で開いた。判別が付きにくい 6 件（`becbe30`・`51dbca0`・`520e890`・`b6be795`・`a667893`・`d737815`）は差分本文を全文読んだ |
| #27 が `src/main` 無変更であること | `git diff --stat 0672a16..HEAD -- src/main pom.xml`（空） |
| `becbe30` の位置づけ | `checks/task-26.5.md` の Completion Criteria 表（`XlsFormatWriter.java` の総行数 601 不変・定数名不変） |
| XLS-44 が未実施の要対応 1 件であること | `issues.md` XLS-44 の判定欄と、`steering.md` の Acceptance criteria ／ Rules（`45e857e` で書き分け） |

### A-5. `mvn clean test` の結果

```
$ JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true
[WARNING] Tests run: 597, Failures: 0, Errors: 0, Skipped: 2
[INFO] BUILD SUCCESS
```

**コーディネータの実測（`Tests run: 597, Failures: 0, Errors: 0, Skipped: 2`）と一致した。**
`Skipped: 2` は `YamlFormatReaderInvalidInputTest` の `@Ignore` 2 件（XLS-40 カラム名側 ／ YML-14）で、
`steering.md` #25.5 の Completion criteria が定める形どおりである。
**JaCoCo は一度も走らせていない**（`-Djacoco.skip=true` を付けて実行した）。

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Verification approach meaningful to the objective (checks the right thing, not just "passed") | OK | **3 巡目 pass。** 1 巡目は fail で 4 件 —— §3 柱書（11／14）・§3.6（32 件）・§6.1（105／34／103）の宣言値が HEAD と食い違う、および C-21(省略) の「本行だけが例外である」が 5 行しか照合していないのに 14 行に掛かる断定になっている。**再レビューで 4 件とも解消を確認。** 導出コマンドを自前のハーネスで抽出し直して 58 本を全数実行し不一致ゼロ。新設の 11 行表は 12 メソッドの本文を開いて型2 ゼロを確認し、「値を入れれば落ちる」が最も自明でない 3 行は Given 側も開いた。「指摘への対処が『その 4 件を直す』でなく『同型を機械的に潰す』になっており、これが正しい直し方」と評価。残した N-1（変異実測「2 行だけ」の主語の曖昧さ。誤りの向きは保守側）は `59d511d` で処置した |

## Expert Reviews (axes the task needs)

### Craft Expert (writing)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Medium-specific best practice | OK | **3 巡目 pass。** 1 巡目は fail で、表崩れ 1 件・「3 つの制約」の下に 4 項目・§6.2 の内訳合計が 317（総数 315 と不一致）・導出コマンドの宣言値不一致を指摘。**再レビューで全件解消を確認**（表行の直後の非表・非空行が 0 件、`axis-matrix.md` の主要 15 本を逐語で流して全一致、27 ＋ 288 ＝ 315 を検算） |
| Consistency with existing style | OK | **3 巡目 pass。** 1 巡目は fail で、出典を添えた鉤括弧 4 件が非逐語・スキーマパスが 3 通りに割れ 2 通りは JSON 上で解決できない・助数詞が「点」と「本」に割れる・`。 ` が 16 か所、を指摘。**再レビューで全件解消を確認**（引用 4 件が原典と一字一致、スキーマパス 6 か所すべてが実スキーマで解決し値も宣言どおり、`。 ` 0 件）。再レビューで新たに挙げた 4 件（§0.1 の助数詞規約が本文と逆／`coverage-report.md` の表 1 行の割れ／同 §0 の個人パス／XLS-44 の要約引用）は `59d511d` で処置した |

### Verification Expert (fact-check)

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Artifact actually checked (tests run / claims verified / flow traced) | OK | **3 巡目 pass。** 1 巡目は fail で、`sh` ブロック 27 本のうち 3 本が宣言値と不一致（QA・Craft と同一の 3 件）。**再レビューで 58 本を全数実行して不一致ゼロ。** `mvn -o clean test -Djacoco.skip=true` → `Tests run: 597, Failures: 0, Errors: 0, Skipped: 2`。**JaCoCo は 3 者とも再計測していない**（`steering.md` Rules）。JaCoCo 依存の 10 本は「実行できない」と明示された |
| Coverage (edge cases / claims / steps) | OK | **3 巡目 pass。型1・型2 とも新規ゼロ。** 新設の 11 行表は 11 件すべてテスト本文を開き、表が挙げるアサートが実在し値の有無に反応する形であることを確認。XLS-44 ／ XLS-45 の挙動の主張は**独立に書いた使い捨てプローブで再現**（辺④の 4 パターンが `identical? true`、辺③の可変長で `length` が出力に現れない）。出典つき鉤括弧を原典（`30a8271` の `notation`、本体スキーマ、`nablarch-testing` の sources jar）で照合。1 巡目に挙げた D5（XLS-44 の「1 対 1」が同節の「4 対 2 の写像」と矛盾）も解消を確認 |

## Triage（コーディネータ）

**3 者は `isolation: worktree` の独立サブエージェントとして起動し、`checks/` は渡していない**（`steering.md` Rules）。

| 巡 | 指摘 | 判定 | 処置 |
|---|---|---|---|
| 3 巡目 | QA 4 件・Craft 15 件・Verification 5 件（重複を除いて 17 件） | **Valid 17 件・Invalid 1 件** | Valid 17 件は `2261c74`〜`41f75c4` の 8 コミットで是正。Invalid 1 件は Craft の Completion criterion 4 の NG（上表 4 行目に理由） |
| 3 巡目・再レビュー | QA 1 件・Craft 4 件（重複を除いて 5 件） | **Valid 5 件** | `59d511d` で是正。3 者とも「重大度 低」「エスカレーション不要」と明記しており、いずれも 1 行の機械的な直しで判断を要さないため、上限外の巡としてエスカレーションせず処置した |

**繰り返し出た欠陥型への手当て。** 3 者が同じ 3 件（§3 柱書・§3.6・§6.1 の宣言値不一致）を挙げ、原因も同一だった —— #27 後半のコミット（`783810b` ／ `6d12021` ／ `50c4a38`）が入力を変えたのに、離れた節の宣言値を導き直していない。個別に直すのでは再発するため、**§0.6 に「`src/test` ／ `inventory.md` を触ったら本書の `sh` ブロックを 1 本残らず流し直す」を置き、実際に全数実行した。** その全数実行が、**是正自身が持ち込んだ欠陥を 2 件捕まえた** —— `f4f58fe`（引用の逐語化で入った `XlsFormatWriterTest#roundTrips*` の末尾 `*` を実在照合がメソッド名として拾い、抽出が 320／288 → 322／290 へ動き NG 2 件を返していた）と `41f75c4`（`issues.md` の ```sh ブロック 3 本が `$ ` プロンプト付きでそのまま実行できなかった）。

**2 巡続いた欠陥型（「照合していない範囲について完全列挙・唯一性を断定する」）は 3 巡目でも 1 件出た**（C-21(省略) の「本行だけが例外である」）。**主張を弱めず照合を 11 行へ広げる**道を採り、母集団の導出コマンドと 11 行それぞれのアサートを開示したうえで、**根拠が Then の通読であって変異実行ではないこと**（変異まで流したのは 3 行だけであること）も明記した。再レビューでは 3 者ともこの表に型1・型2 の欠陥を見つけていない。

**もう一方の欠陥型（「表が主張する内容を、テスト本文が実際には主張していない」）は 3 者とも新規ゼロ。** 本タスクで新たに ✅ にした 3 行と ❌ にした 5 行は、3 者が独立にテスト本文・プローブで再現している。

## コーディネータの独立確認

Verify のレビューとは別に、コーディネータ自身が実物で確かめたもの:

- 集計 ✅ 283 ／ ❌ 5 ／ 空欄 27 ／ 合計 315、辺別 71・74・71・67、軸別 A56・B16・C144・D37・E44・F18、27 ＋ 288 ＝ 315
- 実在照合 NG 0、抽出 320 ／ 288 ／ 32、`src/main` 27 件 NG 0
- `issues.md` 57 ／ 26 ／ 28 ／ 2 ／ 1 ／ 43 ／ 14、判断欄 57、未分類 0、二重 0、ID 欠番は XLS-02 ／ XLS-03 のみ
- **`sh` ブロックの独立全数実行** —— `axis-matrix.md` 30 本（ハードエラー 1 件は `EXTRACT` を前ブロックで定義する形で、本文がそう明記しているため正）、`issues.md` 29 本（ハードエラー 0）
- `notation:850` ／ `:883` ／ `:902` ／ `:1076` ／ `:1515` を `30a8271` で逐語照合
- `FileDataBlock` の食い違う組を作るテストが `src/test` に 0 件、`VariableLengthFileFragment` が `addValue` を上書きしていないこと、`YamlFileBuilder` の `if (messaging || hasLength)` とコメントの逐語
- `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` → `Tests run: 597, Failures: 0, Errors: 0, Skipped: 2` ／ `BUILD SUCCESS`
- `git diff --stat 83137c2..HEAD -- src/main` が空

## Overall Verdict

- Self-check: OK
- QA: OK（3 巡目 pass）
- Design expert: N/A（本タスクは既存の構造の上に台帳を作るもので、構造・方針を作りも変えもしない）
- Craft expert: OK（3 巡目 pass）
- Verification expert: OK（3 巡目 pass）
- Ready to check off: **Yes**
