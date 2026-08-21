# 軸×要素対応表（軸要素 → 担保テストメソッド。task #27）

4 つの変換辺それぞれについて、軸A〜F の全要素を行として並べ、その要素を担保している
テストメソッド名を記した逆引きの表である。

- 作成日: 2026-08-21
- 対象コミット: `HEAD`（ブランチ `ntf-test-data-converter`）。**状態欄の判定と「担保テストメソッド」欄の
  メソッド名は、1 件残らず HEAD の `src/test` ／ `src/main` を開いて確かめたものである。**
  この 2 欄について `inventory.md` の記述を根拠にした箇所は無い。例外は「🔺 往復」欄で、
  この欄の軸要素対応は `inventory.md` の記述を出典としており本タスクでは読み直していない
  （内訳と理由は §0.5。未確認として §6.2）。確かめていない箇所は §6 に「未確認」として列挙する。
  なお `inventory.md` は、軸の定義（§0.3）や軸D・軸F の対象範囲など、2 欄の外では出典として引いている
- 位置づけ: `steering.md` Rules「4 辺を通した逆引き（軸要素 → 担保テストメソッド）の正は #27 の
  `coverage/axis-matrix.md` とする」に従う。`inventory.md` は順引きの節（テストメソッド → 軸要素。
  `inventory.md` §1.1 ／ 同 §2.1 ／ 同 §3.1 ／ 同 §4.1 など）と逆引きの節（軸要素 → テストメソッド。
  `inventory.md` §1.2-2 ／ 同 §2.1-2 ／ 同 §3.1-2 ／ 同 §3.1-3 ／ 同 §4.1-2 の 5 節）の両方を持つ
  （`steering.md` Rules が「既存クラスの棚卸しは §X.1 系、各タスクが新規追加したクラスの担保は
  §X.1-2 系に書く」と定めているため）。本書はそのうち逆引きだけを 4 辺ぶん通しで持つ現在地の台帳である。
  同じ関係を 2 方向に手書きしないため、本書は `inventory.md` の順引き表を転記しない
- `inventory.md` との関係: `inventory.md` §1〜§4 の表は各タスク時点のスナップショットであり、
  現在の担保を示すものではない。本書は HEAD 時点の現在地だけを示す。ただし 🔺 往復欄だけは、
  その一部でスナップショット表（`inventory.md` §3.1 ／ 同 §4.1）を出典にしている（§0.5）。
  両者が食い違う箇所は §6.1 に挙げた

## 0. 読み方

### 0.1 凡例（状態）

| 印 | 意味 |
|---|---|
| ✅ | 担保あり（その軸要素をその辺で直接アサートしているテストがある） |
| 🔺 | 弱い担保のみ（往復テスト経由でしか通っていない）。正式担保には数えない |
| ❌ | 未担保（到達可能なのに担保テストが無い） |
| — | 空欄（その辺ではその状態に到達できない／その辺の担当クラスの関心事ではない）。理由欄に根拠を書く |

**`—` と `❌` の意味は `inventory.md` の凡例と違う。** 相違は 2 点ある。
(a) `inventory.md` の `—` は「該当なし（そのテストはその軸を通さない／その要素に担保テストが 1 件もない）」で、
このうち後半は本書では `❌` に当たる。(b) 本書の `❌` は「到達可能なのに担保が無い」に狭めてある。
(b) は §5.4 が論拠に使っている相違である —— 辺③ F3-02 は変換全体としては到達可能だが辺③の担当クラスからは
到達できないため、本書の定義では `❌` ではなく `—` になり、穴は §5.4 に開示する形になる。

**`n/a` は使わない。** 「省略」「空」という状態が存在しない要素については、そもそも
「(値あり)／(省略)」「(非空)／(空)」の 2 行に割らず 1 行だけ立てる（軸C の行割りは §0.2 のとおり、
省略可能フィールド 4 と空許容コレクション 11 だけを 2 行に割る）。したがって
「存在しない状態」を表す行そのものが本書に無く、`n/a` を置く場所が無い。4 辺の C-10
（`FileDataBlock.fileType`）がその例で、FIXED ／ VARIABLE の必須 2 値を 1 行で扱い状態は ✅ である。

**「担保テストメソッド」欄と「🔺 往復」欄の `—` は「該当なし」**（`inventory.md` の凡例と同じ）であり、
状態欄の `—`（空欄）とは意味が違う。状態が `—` の行は担保テストメソッドを持たないため
両欄とも `—` にし、理由欄に到達不能であることを実行可能な形で示す根拠テストを挙げる。

**🔺 も 2 役を持つ。** 状態欄の値としての 🔺（＝その軸要素の担保が往復テストしか無い）と、
列見出しとしての「🔺 往復」欄（＝状態欄が ✅ でも ✅ 以外でも、その軸要素を通す往復テストを併記する欄）である。
§5.2 が報告する「🔺 弱い担保のみ 0 件」は状態欄だけの集計であって、🔺 往復欄が空という意味ではない
（🔺 往復欄が `—` でない行は 98 行ある。導出は §0.6 の ③）。
なお `inventory.md` の凡例は 🔺 を「間接的・副次的にしか通っていない」と定義しているが、
本書は「実ファイルを通す往復テスト経由でしか通っていない」に狭めて使う（§0.5 に挙げた 3 群だけを計上する）。

**太字の扱い。** `inventory.md` の凡例では太字が「その辺でその要素を通す唯一の担保」という意味を持つが、
本書の太字は強調であり、唯一性を表さない。唯一性を主張する箇所は本文で明示し、
導出コマンドを併記する（§3.5 E-4(1件) の理由欄がその例）。太字は次の 4 つの用途にだけ使う。

1. 段落の先頭に置く見出し（その段落が何の話かを示す。この段落の「太字の扱い。」がその形）
2. 箇条書きの項目の先頭に置くラベル（§0.2 の「軸A」「軸B」など）
3. その節で 1 つだけ持ち帰らせたい事実
4. 状態が ❌ の行の理由欄の書き出し 1 文（なぜ ❌ なのかを 1 文で示す）

**要素表のセルの中と、コマンドの出力値には太字を使わない**（❌ の行だけが例外で、集計表の「合計」行は残す）。
表のセルで太字が残っていないことの導出は §0.6 の ⑥。

**表記の規約 —— 「担保テストメソッド」欄は `クラス名#メソッド名` の形で書く。**
略記（クラス名を省いて `#` からメソッド名だけを書く形）には次の 3 つの制約を置く。

1. 略記が指すのは、同じセル内でそれより前に完全な形で書いた直近のクラスである。1 セルに 2 つ以上の
   クラス名が現れるときは、その略記より前にある最後のものを指す。セルをまたいで
   「直前に挙げたクラス」を引き継がない（表は 6 列で、担保テストメソッド欄と理由欄のあいだに
   🔺 往復欄が挟まるため、セルをまたぐと直前が別クラスになる）。表以外の本文では 1 行を 1 セルとみなす。
2. 略記できるのは、クラス名が `Test` で終わるテストクラスのメソッドだけである。
3. `src/main` のメソッドは略記しない（`XlsFormatWriter#layout` のように必ずクラス名を書く）。
   したがって略記は、本書では必ずテストクラスのメソッドを指す。
4. テストヘルパ（`@Test` の付かないメソッド）もクラス名を書き切る。 §0.6 の実在照合コマンドは
   `void` メソッドだけを照合するため、`void` を返さないヘルパは照合の網に掛からない。
   本書が引くヘルパは `XlsFormatReaderRealFileTest#onlyBlock`（§1.5）と
   `YamlFixture#onlyBlock`（§2.5）で、`src/test` には同名の定義が 5 クラスに 6 つある
   （`grep -rnE '(private|static).*\bonlyBlock\s*\(' src/test --include=*.java` の出力が 6 行）。
   裸で `onlyBlock` と書くとどれを指すか決まらない。

**節の参照。** 他文書の節はファイル名を必ず前置する（`inventory.md` §0.7 ／ `steering.md` #25.5 §1-G ／
`coverage-report.md` §3.2 のように）。同じ文書の節を続けて挙げるときは 2 つめ以降を `同 §…` と書く。
ファイル名の前置も `同` も付かない `§…` は本書の節を指す。
言い回しはこの 2 形（`<ファイル名> §…` ／ `同 §…`）だけを使い、`同文書の §…` のような 3 つめの形は使わない。
複数の節を挙げるときは範囲指定（`§1.1〜§4.1`）を使わず 1 つずつ列挙する —— 間に別番号の節が挟まると
範囲がどこまでを含むのか読めないためである。

**節番号の付け方。** 見出しは `## <番号>. `／`### <番号>.<枝番> `／`#### ` の 3 段で、
枝番はピリオドで継ぐ（`### 6.1` であって `### 6-1.` ではない）。ハイフンは
`inventory.md` §0.8-6 ／ `steering.md` #25.5 §6-J-2 のように他文書側が下位項目に使う記法であり、
本書の節番号には使わない。

**巨大セルの分離基準。** 担保テストメソッド欄にメソッド名を 4 つ以上並べない。
4 件以上になる軸要素は、同欄に代表 1 件と「全 n 件は下表」と書き、直下に `####` の内訳表を置く。
件数が 4 未満でも、メソッドごとにアサートする内容が違う場合は同じ形にしてよい（§1.6 の F1-05 がその例）。
本書で内訳表を持つのは F1-05 ／ F1-06 ／ D3-06 ／ D3-08 ／ F3-04 ／ D4-07 の 6 か所である。
基準を満たしていることの導出は §0.6 の ④。

**各節末尾の見出し「軸X の外に残る空欄」** は、その軸の要素表に行として立たない担保の穴を開示する。
表記はこの 1 形に統一する。

### 0.2 判定基準

- **軸A**: その `DataType` のブロックが生成される／その `DataType` に依存する出力が書き出されることを
  アサートしているものを担保とする。入力にその型のブロックを与えているだけでは担保にならない
  （4 種共通の値しか見ていないテストを担保と数えた誤判定が辺③・辺④で実際に起きている。
  `inventory.md` §3.1-3 ／ 同 §4.1-2）。
- **軸B**: 実装クラス（`TableDataBlock` ／ `ListMapBlock` ／ `FileDataBlock` ／ `MessageDataBlock`）が
  生成される／その形で書き出されることをアサートしているもの。
- **軸C**: 省略可能フィールドは「値あり」「省略」、空許容コレクションは「非空」「空」の双方を別の行として評価する。
- **軸D・軸F**: ケース 1 件を 1 行とする。
- **軸E**: （観点, 多重度）の組 1 件を 1 行とする。**「0 件」「1 件」の行を ✅ にするには、
  その件数が結果に現れていることをアサートしていなければならない。** 読み手側（辺①・辺②）は
  `size()` をその件数でアサートしていること、書き手側（辺③・辺④）は「n＋1 件目が無いこと」まで
  固定していること（辺④なら出力全文の完全一致、辺③なら次の要素が始まる行位置が空であること）。
  入力にその件数を与えただけの行は ✅ に数えない。
  辺③で「ブロックの最後の行の次が `null`」を根拠にするときは足りない ——
  `XlsFormatWriter#writeSection` がブロック間の空行を `rowNum += config.getBlankRowsBetweenBlocks();` で
  行を生成せずに飛ばすため、2 ブロック目があってもその手前の行は空行として `null` のままだからである
  （既定値は 1。`ExcelFormatConfig#defaults` の末尾引数）。2 ブロック目の識別行が来る位置まで見るか、
  シート全体の行数（`getPhysicalNumberOfRows()`）を固定する必要がある
  （前者は §3.5 の E-2(1件)、後者は同 E-1(1件) がその形）。
- **読み手側の辺**（辺①・辺②）は実ファイルを入力とする経路の担保を正とする。in-memory 経路
  （`XlsFormatReaderTest` の `FakeTestDataReader` ／ `YamlFormatReaderTest` の `loadRawMap` 差し替え）だけの
  担保は ✅ に数えない。これは #20／#24 で確定した基準である。

### 0.3 軸の定義（要約）

| 軸 | 正式名 | 要素 | 詳細 |
|---|---|---|---|
| A | データタイプ | `DataType` 14 種（A-01〜A-14） | `inventory.md` §0.2 |
| B | ブロック実装 | sealed 階層の具象 4 種（B-1〜B-4） | `inventory.md` §0.3 |
| C | 中間モデル全フィールド | 8 クラス 21 フィールド（C-01〜C-21） | `inventory.md` §0.4 |
| D | 値の表現 | 辺① 8／辺② 12／辺③ 8／辺④ 9 ケース | `inventory.md` §0.5 |
| E | 多重度 | 4 観点 × 多重度（E-1〜E-3 は 0／1／複数、E-4 は 1／複数） | `inventory.md` §0.6 |
| F | 異常系 | 辺① 6／辺② 5／辺③ 4／辺④ 3 ケース | `inventory.md` §0.7 |

軸C の 21 フィールドは、必須スカラー 6（C-01・C-03・C-05・C-07・C-10・C-19）／
省略可能フィールド 4（C-06・C-16・C-20・C-21）／空許容コレクション 11
（C-02・C-04・C-08・C-09・C-11・C-12・C-13・C-14・C-15・C-17・C-18）に分かれる。
後ろの 2 群を 2 行に割るため、軸C は 1 辺あたり 6 ＋ 4×2 ＋ 11×2 ＝ 36 行になる。

### 0.4 中間モデルの不変条件が「到達不能」を作っている

#25.5 の後半で「不正値は書き出し側でなく中間モデルの生成時に拒否する」方針
（`steering.md` Decisions）に沿って番人が中間モデルへ集められた。その結果、4 辺のどこからも
到達できなくなった軸要素がある。本書ではそれらを状態欄 `—` とし、理由欄に
その不変条件を担保しているモデルのテストを挙げる。

下表は HEAD の `src/main/java/nablarch/test/tool/converter/model/` を読んで確かめた不変条件のうち、
本書の空欄に効くものだけを挙げたものである（`model/` の不変条件の全数ではない。下段を参照）。
どの行がどの空欄を作るかは「本書の空欄への効き方」欄で直接示す —— 「効く軸要素」欄の表記から
読み手が推測しなくてよいようにするためである。

| 不変条件（生成時に `IllegalArgumentException`） | 置き場所 | 効く軸要素 | 本書の空欄への効き方 |
|---|---|---|---|
| `dataType` が `DataType.DEFAULT` のブロックは作れない | `TestDataBlock` | A-01 | 4 辺の A-01 を空欄にする（4 件） |
| `dataType` ／ `groupId` ／ `identifier` が `null` のブロックは作れない | `TestDataBlock` | C-05・C-06・C-07 | 空欄を作らない（`null` 側だけを閉じる。3 要素とも 4 辺で ✅） |
| カラム名 0 件のブロックは「セルを持つ行」を持てない | `ColumnRowDataBlock` | C-08(空) | 空欄を作らない（C-08(空) の形を限定するだけ。4 辺で ✅） |
| `fileType` が `null` のファイルブロックは作れない | `FileDataBlock` | C-10 | 空欄を作らない（4 辺で ✅） |
| 固定長ファイル・電文でフィールド長 `null` は保持できない | `ModelPreconditions#requireLengths` | C-21(省略) | 空欄を作らない（到達先を可変長ファイルに限定するだけ。4 辺で ✅） |
| 本文レコード 0 件の電文ブロックは作れない | `MessageDataBlock` | C-15(空)・E-3(0件) | 4 辺の C-15(空) を空欄にする（4 件）。 E-3(0件) は 4 辺で ✅（ファイル系で到達する） |
| フィールド 0 件のレコードレイアウトは作れない | `RecordLayout` | C-17(空) | 辺③・辺④ の C-17(空) を空欄にする（2 件）。 辺①・辺② の C-17(空) も空欄だが、先に効くのは本体パーサ／スキーマである（§5.3） |
| `name` ／ `type` が `null` のフィールド定義は作れない | `FieldDef` | C-19・C-20(省略) | 辺③・辺④ の C-20(省略) を空欄にする（2 件）。 C-19 は 4 辺で ✅。辺①・辺② の C-20(省略) も空欄だが、先に効くのは本体パーサ／スキーマである（§5.3） |
| コンテナ・読み込み単位の名前 `null` は作れない | `TestDataContainer` ／ `TestDataSection` | C-01・C-03 | 空欄を作らない（辺④ C-01 は空欄だが理由は別で、辺④がコンテナの名前を読まないためである。§4.3） |

「本書の空欄への効き方」欄の件数は 4 ＋ 4 ＋ 2 ＋ 2 ＝ 12 で、§5.3 の分類
「中間モデルの不変条件による到達不能」12 件と一致する。

**上表の 9 行と、`model/` の `IllegalArgumentException` 送出箇所の数は単位が違う。**
上表は「不変条件」を単位に数えたもので、1 つの不変条件が複数の箇所で送出しうる。
`ModelPreconditions#requireNoNulls` がその例で、リスト版と Map 版の 2 つのオーバーロードが
合わせて 3 箇所で送出する。したがって「9 と 20 の差が 11 個の不変条件である」とは読めない。

```sh
cd "$(git rev-parse --show-toplevel)"
M=src/main/java/nablarch/test/tool/converter/model
grep -rho "throw new IllegalArgumentException" "$M"/ | wc -l   # 送出箇所の総数
# requireNoNulls の 2 つのオーバーロードと、そこから送出される件数
grep -cE "static .*requireNoNulls\(" "$M"/ModelPreconditions.java
perl -0777 -ne 'while (/\bstatic\b[^;{()]*\brequireNoNulls\s*\(/g) {
      my $i = index($_, "{", pos); my $d = 1; my $j = $i + 1;
      while ($d > 0 && $j < length) { my $c = substr($_, $j, 1);
        $d++ if $c eq "{"; $d-- if $c eq "}"; $j++ }
      $n += () = (substr($_, $i, $j - $i) =~ /throw new IllegalArgumentException/g) }
    END { print "$n\n" }' "$M"/ModelPreconditions.java
```

出力は順に 20 ／ 2 ／ 3 —— 送出箇所は全体で 20 か所、`requireNoNulls` は
オーバーロード 2 つで送出 3 か所、である。

上表に無い不変条件には、たとえば次のものがある（いずれも HEAD の `model/` を読んで確かめた）。
どれも本書の状態欄を動かさない（拒否される形が軸要素の「空」「省略」に当たらないため）。

| 不変条件 | 置き場所 |
|---|---|
| 自分の系統に属さない `DataType` のブロックは作れない | `TestDataBlock#requireDataTypeOf`（`issues.md` XLS-36） |
| フィールド名称が重複したレコードレイアウトは作れない | `ModelPreconditions#requireNoDuplicates`（`issues.md` XLS-40） |
| フィールド定義の件数より要素数が多いデータ行は保持できない | `ModelPreconditions#requireRowsNotLongerThan`（`issues.md` XLS-41） |
| リストの要素が `null` のモデルは作れない | `ModelPreconditions#requireNoNulls`（`issues.md` XLS-38） |
| Map のキー・値が `null` のモデルは作れない | `ModelPreconditions#requireNoNulls`（Map 版。`issues.md` XLS-43） |

**この 5 行も全数ではない**（上表 9 行と合わせて 14 行であり、送出箇所 20 との差は残る）。
`model/` の不変条件を全数で数えたわけではない（未確認。§6.2）。本書が閉じているのは空欄の側だけである ——
§5.3 が「中間モデルの不変条件による到達不能」に分類した 12 件は、いずれも上表のどれかの行が作ったものであり、
上表に現れない不変条件が作った空欄は本書に無い。§5.3 の総数 27 は本書自身から機械的に導いてある（§5.3 の導出コマンド）。

### 0.5 往復テストの扱い（🔺 欄）

`steering.md` Rules（フェーズ2）に従い、既存の往復テストが実ファイル経由で通している軸要素は 🔺 欄に計上する。
正式担保としては数えない（状態欄は 🔺 欄の内容では動かない）。計上する往復テストは次の 3 群である。

| 群 | 本書の 🔺 欄に現れるメソッド数 | 軸要素対応の出典 |
|---|---|---|
| `RoundTripTest` | 29 | `inventory.md` §0.8-8 の表（メソッド → 軸要素）を逆向きにしたもの |
| `XlsFormatWriterTest` の `roundTrips` で始まるメソッド群 | 10 | 下記のとおり `inventory.md` §0.8-8 には無い |
| `YamlFormatWriterTest` の `roundTrip_` で始まるメソッド群 | 5 | 同上 |

`RoundTripTest` の 29 は本書が引く数であり、同クラスの `@Test` は 30 件ある（下の表）。

```sh
cd "$(git rev-parse --show-toplevel)"
# 🔺 往復欄（6 列目）に現れるメソッドをクラス別に数える
# 略記は同じセル内で解決する（§0.1 の表記の規約）
perl -CSDA -ne 'next unless /^\| [A-F][0-9-]/; my @c = split(/\|/, $_, -1); my $k;
    while ($c[5] =~ /`([A-Z][A-Za-z0-9]*)?#(\w+)/g) {
      print(((defined $1 ? ($k = $1) : ($k // "UNRESOLVED")) . "#$2\n")) }' \
    .rn/ntf-test-data-converter/coverage/axis-matrix.md \
  | sort -u | awk -F'#' '{print $1}' | sort | uniq -c
```

出力は `RoundTripTest` 29 ／ `XlsFormatWriterTest` 10 ／ `YamlFormatWriterTest` 5
（重複を除いて 44 メソッド）の 3 行である。

**出典の質は群によって違う。** 3 群それぞれについて次のとおりである。

- 本書が引く `RoundTripTest` の 29 件: 出典は `inventory.md` §0.8-8 の表。本タスクで確かめたのは
  メソッド名が HEAD に実在することまでであり、各メソッドが同表のとおりの軸要素を通していることは
  読み直していない（未確認。§6.2）。
- `XlsFormatWriterTest` の 10 件 ／ `YamlFormatWriterTest` の 5 件: `inventory.md` §0.8-8 の表は
  `RoundTripTest` の 30 メソッドだけを行に持ち、この 15 件には同節での軸要素の割り当てが無い
  （同節の末尾が「既に `roundTrips*` ／ `roundTrip_*` 経由で 🔺 であり」と総称で触れるだけである）。
  **うち 13 件の軸要素対応の出典は `inventory.md` §3.1 ／ 同 §4.1 のスナップショット表であり、
  本書冒頭が「現在の担保を示すものではない」と断っている表である。** この 13 件も本タスクで
  読み直していない（未確認。§6.2）。
- 残る 2 件（`XlsFormatWriterTest#roundTripsZeroRowTableWithoutEatingNextBlock` ／
  `XlsFormatWriterTest#roundTripsZeroRowListMapWithoutEatingNextBlock`）は本タスクで本文を読んで確かめた（§7 の担保表）。
  この 2 件は `inventory.md` §0.1-2 の「担保の現在地」表に現れるが、そこでの軸要素対応は C-08 であって
  🔺 欄が必要とする軸要素対応ではない。`inventory.md` §0.8-8 の往復テスト表には無い。

**引いていない往復テストが両側にある。**

| クラス | HEAD のメソッド数 | 本書が引く数 | 引いていないメソッド |
|---|---|---|---|
| `RoundTripTest`（`@Test` 全件） | 30 | 29 | `RoundTripTest#yaml_listMap_withNullValue_isPreserved` |
| `XlsFormatWriterTest` の `roundTrips` で始まるもの | 10 | 10 | なし |
| `YamlFormatWriterTest` の `roundTrip_` で始まるもの | 6 | 5 | `YamlFormatWriterTest#roundTrip_leadingTrailingWhitespace_isPreservedThroughRealReader` |

```sh
cd "$(git rev-parse --show-toplevel)"
grep -c '@Test' src/test/java/nablarch/test/tool/converter/RoundTripTest.java
grep -oP 'void \Kround\w+' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterTest.java | wc -l
grep -oP 'void \Kround\w+' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatWriterTest.java | wc -l
```

出力は順に 30 ／ 10 ／ 6。本書が引く数（29 ／ 10 ／ 5）は上のクラス別集計コマンドの出力である。
引いていない 2 件の名前は、両コマンドの出力の差を取れば得られる。

### 0.6 件数の導出コマンド

**本書の行数と状態別の件数は、本書自身を走査して導く。** 各表の行は必ず
`| <軸要素 ID> | <内容> | <状態> | <担保テストメソッド> | <🔺> | <理由・注記> |` の 6 列で、
軸要素 ID は `A`〜`F` で始まり、状態欄には `✅` `🔺` `❌` `—` のいずれか 1 つだけを置く。

**コマンドは必ずリポジトリルートから始める。** 個人のチェックアウトパスを埋め込むと、
別のチェックアウトやレビュー用 worktree から実行したときに、本書が対象としている commit ではなく
そのチェックアウトの HEAD を測ってしまう。測った commit は次の 1 行で分かる。

```sh
cd "$(git rev-parse --show-toplevel)" && git rev-parse HEAD
```

```sh
cd "$(git rev-parse --show-toplevel)"/.rn/ntf-test-data-converter/coverage
# ① 辺ごとの行数（辺① → 辺④）
for n in 1 2 3 4; do
  m=$((n + 1))
  printf "辺%s: " "$n"
  awk "/^## ${n}\. /,/^## ${m}\. /" axis-matrix.md | grep -cE '^\| [A-F][0-9-]'
done
# ② 辺 × 状態の件数
for n in 1 2 3 4; do
  m=$((n + 1))
  printf "辺%s " "$n"
  awk "/^## ${n}\. /,/^## ${m}\. /" axis-matrix.md \
    | grep -E '^\| [A-F][0-9-]' \
    | awk -F'|' '{gsub(/ /,"",$4); c[$4]++} END {for (k in c) printf "%s=%d ", k, c[k]; print ""}'
done
# ③ 🔺 往復欄が「該当なし」でない行数（状態欄の 🔺 の件数とは別物。§0.1）
grep -E '^\| [A-F][0-9-]' axis-matrix.md | awk -F'|' '{gsub(/ /,"",$6)} $6 != "—"' | wc -l
# ④ 担保テストメソッド欄にメソッド名を 4 つ以上並べたセル（§0.1 の分離基準。出力が無いのが正）
perl -CSDA -ne 'next unless /^\| [A-F][0-9-]/; my @c = split(/\|/, $_, -1);
    my $n = () = ($c[4] =~ /#\w/g); my $id = $c[1]; $id =~ s/^\s+|\s+$//g;
    print "$id ($n)\n" if $n >= 4;' axis-matrix.md
# ⑤ 本書の全行数（4 辺の合計。§5.1）
grep -cE '^\| [A-F][0-9-]' axis-matrix.md
# ⑥ 状態が ❌ でない要素表の行に残った太字（§0.1 の太字の扱い。出力が無いのが正）
perl -CSDA -ne 'next unless /^\| [A-F][0-9-]/; my @c = split(/\|/, $_, -1);
    my $st = $c[3]; $st =~ s/\s//g; next if $st eq "\x{274c}";
    my $id = $c[1]; $id =~ s/^\s+|\s+$//g; print "$id\n" if /\*\*/;' axis-matrix.md
```

**本書に記した担保テストメソッドが 1 件残らず実在することの確認。**
`クラス名#メソッド名` の形と、クラス名を省いた略記の形の両方を本書から機械的に抜き（略記は §0.1 の規約どおり
同じセル内で解決する）、`src/test` の該当クラスに同名のメソッドがあるかを 1 件ずつ照合する
（`void` のテストメソッドと、`void` を返さないテストヘルパの両方を拾う。§0.1 の略記規約 4）。
§6 は照合対象から外す —— §6 は「`inventory.md` がこの名前を挙げているが HEAD に無い」と述べる節で、
無いことがそこでの主張である（その名前は下の別コマンドで「無いこと」を照合する）。

```sh
cd "$(git rev-parse --show-toplevel)"
A=.rn/ntf-test-data-converter/coverage/axis-matrix.md
awk '/^## 6\. /{skip=1} /^## 7\. /{skip=0} !skip' "$A" \
  | perl -CSDA -ne 'chomp; for my $c (/^\|/ ? split(/\|/, $_, -1) : ($_)) { my $k;
        while ($c =~ /`([A-Z][A-Za-z0-9]*)?#(\w+)/g) {
          print(((defined $1 ? ($k = $1) : ($k // "UNRESOLVED")) . "#$2\n")) } }' \
  | sort -u \
  | while IFS='#' read -r cls mth; do
      case "$cls" in
        UNRESOLVED) echo "NG(略記が解決できない) #$mth"; continue ;;
        *Test) ;;
        *) continue ;;          # src/main のメソッド（§0.1 の規約によりクラス名を必ず伴う）
      esac
      f=$(find src/test -name "$cls.java")
      if [ -z "$f" ]; then
        echo "NG(class) $cls#$mth"
      elif ! perl -CSDA -e 'my ($m, $f) = @ARGV; open(my $h, "<:utf8", $f) or exit 1;
                            local $/; my $src = <$h>;
                            exit(($src =~ /\bvoid\s+\Q$m\E\s*\(/
                               || $src =~ /\b(?:private|protected|public|static)\b[^;{()\n]*\b\Q$m\E\s*\(/) ? 0 : 1);' \
             "$mth" "$f"; then
        echo "NG(method) $cls#$mth"
      fi
    done | sort -u
```

出力が無いこと（＝ NG が 1 件も無いこと）が確認結果である。

**メソッド名の抽出に `grep` の文字クラス（`[…ぁ-んァ-ヶ一-龥ー]` のような形）を使わない。**
その形は範囲がロケールの照合順序に依存し、環境によって日本語名のテストメソッドを途中で切る。
本書は代わりに Perl の `\w`（`-CSDA` で入出力を UTF-8 として復号したうえで Unicode 意味の
`\w` を使う）で拾う。これはロケールに依存しない。`perl -e` 側にも `-CSDA` が要るのは、
`A` を落とすと `@ARGV` がバイト列のまま渡り、UTF-8 で復号したファイル本文と照合できずに全件 NG になるためである。

**本書が「HEAD に無い」と述べている名前が、本当に無いことの確認**（§6.1）。

```sh
cd "$(git rev-parse --show-toplevel)"
grep -rho "void readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook" src/ | wc -l
```

出力は 0（同名の Javadoc 参照は `src/test` に 1 か所あるが、`void` で始まるメソッド定義は無い。
`grep -rn` で名前だけを引くと Javadoc が引っかかるため、必ず `void` を付けて引く）。

抽出件数と照合対象件数は、上のパイプラインの `while` 以降を差し替えて導く。

```sh
cd "$(git rev-parse --show-toplevel)"
A=.rn/ntf-test-data-converter/coverage/axis-matrix.md
EXTRACT() {
  awk '/^## 6\. /{skip=1} /^## 7\. /{skip=0} !skip' "$A" \
    | perl -CSDA -ne 'chomp; for my $c (/^\|/ ? split(/\|/, $_, -1) : ($_)) { my $k;
          while ($c =~ /`([A-Z][A-Za-z0-9]*)?#(\w+)/g) {
            print(((defined $1 ? ($k = $1) : ($k // "UNRESOLVED")) . "#$2\n")) } }' \
    | sort -u
}
EXTRACT | wc -l              # 抽出（重複を除く）
EXTRACT | grep -c 'Test#'    # 照合対象（…Test クラスのメソッド）
EXTRACT | grep -v 'Test#'    # 照合対象から外れるもの
```

出力は 320 ／ 288 ／ 32 行である（#27 の水平展開で新たに 5 件のメソッドを引いたぶん、
315 ／ 288 ／ 27 から抽出と対象外だけが増えた。5 件は NTF 本体の `DataFileParser#processDirectives` ／
`DataFileParser#onReadingValues` ／ `DataFileFragment#setNames` と、`src/main` の
`XlsFormatWriter#appendRecord` ／ `YamlFormatWriter#emitFile` で、いずれもテストクラスではないため
照合対象 288 は変わらない）。
32 行の内訳は、本リポジトリの `src/main` のメソッド 27 件と、
NTF 本体（`nablarch-testing`）のメソッド 4 件（`TableData#replaceData` ／ `DataFileParser#processDirectives` ／
`DataFileParser#onReadingValues` ／ `DataFileFragment#setNames`）と、
テストヘルパ `YamlFixture#onlyBlock` 1 件である。`TableData` が本リポジトリの `src/main` に無いことは
`find src/main -name 'TableData.java' | wc -l` が 0 を返すことで分かる（`src/main` に現れるのは
`import nablarch.test.core.db.TableData;` と Javadoc の参照だけである）。
`src/main` の 25 件が実在することは次で確かめる（`EXTRACT` は 1 つ上のブロックで定義した関数）。

```sh
cd "$(git rev-parse --show-toplevel)"
EXTRACT | grep -v 'Test#' | grep -v '^TableData#' | grep -v '^YamlFixture#' \
  | while IFS='#' read -r cls mth; do
      f=$(find src/main -name "$cls.java")
      if [ -z "$f" ]; then echo "NG(class) $cls#$mth"
      elif ! grep -qE "\b$mth[[:space:]]*\(" "$f"; then echo "NG(method) $cls#$mth"; fi
    done
```

出力が無いことが確認結果である。
なお `case` 文の `*Test` に当たらないため、この 27 件はいずれも上の実在照合の対象外である。

---

## 1. 辺① Excel→中間モデル（`XlsFormatReader`）

担保の主体は実 `.xlsx` を入力とする 3 クラス（`XlsFormatReaderRealFileTest` ／
`XlsFormatReaderCellTypeTest` ／ `XlsFormatReaderInvalidInputTest`）である。
`XlsFormatReaderTest` は `FakeTestDataReader` に canned 行を与える in-memory 経路であり、
§0.2 の基準により ✅ に数えない。**同クラスは §1 のどの行の担保テストメソッド欄にも現れない。**
`XlsReferenceFixtureTest` も担保テストメソッド欄に現れない（§1.4 末尾のとおり、軸要素そのものの担保ではないため）。

```sh
cd "$(git rev-parse --show-toplevel)"/.rn/ntf-test-data-converter/coverage
# 辺①の担保テストメソッド欄（5 列目）に現れるクラス
awk '/^## 1\. /,/^## 2\. /' axis-matrix.md | grep -E '^\| [A-F][0-9-]' \
  | awk -F'|' '{print $5}' | grep -oP '`\K[A-Z][A-Za-z0-9]*(?=#)' | sort -u
```

出力は `XlsFormatReaderCellTypeTest` ／ `XlsFormatReaderInvalidInputTest` ／
`XlsFormatReaderRealFileTest` の 3 行で、`XlsFormatReaderTest` も `XlsReferenceFixtureTest` も現れない。

### 1.1 軸A データタイプ（14 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| A-01 | `DEFAULT` | — | — | — | 到達不能。`TestCoreReaderAdapter` が `DEFAULT` のブロックを `continue` でスキップし、さらに `TestDataBlock` が生成時に `DEFAULT` を拒否する。根拠テスト `TestDataBlockTest#データタイプDEFAULTのブロックは生成できない`（`issues.md` XLS-20） |
| A-02 | `SETUP_TABLE_DATA` | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` | `RoundTripTest#xls_setupTable_isPreserved` ／ `XlsFormatWriterTest#roundTripsTable` | — |
| A-03 | `EXPECTED_TABLE_DATA` | ✅ | `XlsFormatReaderRealFileTest#readsExpectedTableBlockWithGroupIdFromRealBook` | `RoundTripTest#xls_expectedTable_withGroupId_isPreserved` | — |
| A-04 | `EXPECTED_COMPLETED` | ✅ | `XlsFormatReaderRealFileTest#readsExpectedCompletedTableBlockFromRealBook` | `RoundTripTest#xls_expectedCompleteTable_isPreserved` | — |
| A-05 | `LIST_MAP` | ✅ | `XlsFormatReaderRealFileTest#readsListMapBlockFromRealBook` | `RoundTripTest#xls_listMap_isPreserved` ／ `XlsFormatWriterTest#roundTripsListMap` | — |
| A-06 | `SETUP_FIXED` | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | `RoundTripTest#xls_setupFixed_isPreserved` ／ `XlsFormatWriterTest#roundTripsFixedFile` | — |
| A-07 | `EXPECTED_FIXED` | ✅ | `XlsFormatReaderRealFileTest#readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook` | `RoundTripTest#xls_expectedFixed_isPreserved` | — |
| A-08 | `SETUP_VARIABLE` | ✅ | `XlsFormatReaderRealFileTest#readsSetupVariableFileBlockWithoutFieldLengthFromRealBook` | `RoundTripTest#xls_setupVariable_isPreserved` ／ `XlsFormatWriterTest#roundTripsVariableFile` | — |
| A-09 | `EXPECTED_VARIABLE` | ✅ | `XlsFormatReaderRealFileTest#readsExpectedVariableFileBlockWithGroupIdFromRealBook` | `RoundTripTest#xls_expectedVariable_isPreserved` | — |
| A-10 | `MESSAGE` | ✅ | `XlsFormatReaderRealFileTest#readsMessageBlockFromRealBook` | `RoundTripTest#xls_message_isPreserved` ／ `XlsFormatWriterTest#roundTripsMessage` | — |
| A-11 | `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `XlsFormatReaderRealFileTest#readsAllFourSendSyncMessageTypesFromRealBook` | `RoundTripTest#xls_expectedRequestHeaderMessages_isPreserved` | 4 種を識別子 `RM01`〜`RM04` で分け、データタイプ・グループ ID・識別子を 1 対 1 で突き合わせる |
| A-12 | `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `XlsFormatReaderRealFileTest#readsAllFourSendSyncMessageTypesFromRealBook` | `RoundTripTest#xls_expectedRequestBodyMessages_isPreserved` | 同上 |
| A-13 | `RESPONSE_HEADER_MESSAGES` | ✅ | `XlsFormatReaderRealFileTest#readsAllFourSendSyncMessageTypesFromRealBook` | `RoundTripTest#xls_responseHeaderMessages_isPreserved` | 同上 |
| A-14 | `RESPONSE_BODY_MESSAGES` | ✅ | `XlsFormatReaderRealFileTest#readsAllFourSendSyncMessageTypesFromRealBook` | `RoundTripTest#xls_responseBodyMessages_isPreserved` | 同上 |

### 1.2 軸B ブロック実装（4 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| B-1 | `TableDataBlock` | ✅ | `XlsFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealSheet` | `XlsFormatWriterTest#roundTripsTable` | 1 シートから 4 実装を生成し、実装クラスと識別子（`T` ／ `lm` ／ `f.dat` ／ `m`）を突き合わせる |
| B-2 | `ListMapBlock` | ✅ | `XlsFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealSheet` | `XlsFormatWriterTest#roundTripsListMap` | 同上 |
| B-3 | `FileDataBlock` | ✅ | `XlsFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealSheet` | `XlsFormatWriterTest#roundTripsFixedFile` | 同上 |
| B-4 | `MessageDataBlock` | ✅ | `XlsFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealSheet` | `XlsFormatWriterTest#roundTripsMessage` | 同上 |

### 1.3 軸C 中間モデル全フィールド（36 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| C-01 | `TestDataContainer.name` | ✅ | `XlsFormatReaderRealFileTest#readsContainerAndSectionNamesFromRealBookAndSheetNames` | — | ブック名 `MyBook` がコンテナの名前になる |
| C-02(非空) | `TestDataContainer.sections` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsContainerAndSectionNamesFromRealBookAndSheetNames` | — | 常に 1 件 |
| C-02(空) | 同 空 | — | — | — | 到達不能。`XlsFormatReader#read` が `Collections.singletonList(section)` を返す 1 シート単位 API（`inventory.md` §0.8-6）。根拠テスト `XlsFormatReaderRealFileTest#readsContainerAndSectionNamesFromRealBookAndSheetNames`（`container.getSections().size()` を 1 でアサートする） |
| C-03 | `TestDataSection.name` | ✅ | `XlsFormatReaderRealFileTest#readsContainerAndSectionNamesFromRealBookAndSheetNames` | — | シート名 `MySheet` が読み込み単位の名前になる |
| C-04(非空) | `TestDataSection.blocks` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealSheet` | — | — |
| C-04(空) | 同 空 | ✅ | `XlsFormatReaderRealFileTest#readsEmptyBlockListFromRealSheetWithoutMarkers` | — | マーカー行の無いシート |
| C-05 | `TestDataBlock.dataType` | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` | — | `getDataType()` を直接アサート。`null` は `TestDataBlock` が拒否する（`TestDataBlockTest#データ種別がnullのデータブロックは生成できない`） |
| C-06(値あり) | `TestDataBlock.groupId` 値あり | ✅ | `XlsFormatReaderRealFileTest#readsExpectedTableBlockWithGroupIdFromRealBook` | `RoundTripTest#xls_expectedTable_withGroupId_isPreserved` | `[g1]` が角括弧つきのまま入る |
| C-06(省略) | 同 省略（`""`） | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` ／ `#readsListMapBlockFromRealBook` ／ `#readsMessageBlockFromRealBook` | `RoundTripTest#xls_setupTable_isPreserved` | `null` ではなく `""`。`null` は `TestDataBlock` が拒否する |
| C-07 | `TestDataBlock.identifier` | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` | — | — |
| C-08(非空) | `ColumnRowDataBlock.columnNames` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` | — | — |
| C-08(空) | 同 空 | ✅ | `XlsFormatReaderRealFileTest#dropsMarkerOnlyRowsAsEmptyEntriesInRealBook` ／ `#dropsMarkerOnlyRowsAsEmptyEntriesInListMapInRealBook` | `XlsFormatWriterTest#roundTripsZeroRowTableWithoutEatingNextBlock` ／ `#roundTripsZeroRowListMapWithoutEatingNextBlock` | マーカー列だけのブロックで到達する（`issues.md` XLS-08）。除外後は行も空になる。0 件テーブルに残る担保の穴は §7 の ①〜⑧ |
| C-09(非空) | `ColumnRowDataBlock.rows` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` | — | — |
| C-09(空) | 同 空 | ✅ | `XlsFormatReaderRealFileTest#readsEmptyRowsFromTableWithoutDataRowsInRealBook` ／ `#readsEmptyRowsFromListMapWithoutDataRowsInRealBook` | — | テーブル経路・LIST_MAP 経路の 2 つ |
| C-10 | `FileDataBlock.fileType`（FIXED ／ VARIABLE の双方） | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook`（FIXED）／ `#readsSetupVariableFileBlockWithoutFieldLengthFromRealBook`（VARIABLE） | — | 必須の 2 値。「省略」は存在しない。`null` は `FileDataBlock` が拒否する（`issues.md` XLS-29） |
| C-11(非空) | `FileDataBlock.directives` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | — | — |
| C-11(空) | 同 空 | — | — | — | 到達不能。NTF 本体の `DataFile` のコンストラクタが `file-type` を必ず注入する（`issues.md` XLS-07）。根拠テスト `XlsFormatReaderRealFileTest#readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook`（ディレクティブ行を 1 行も書かなくても件数 1） |
| C-12(非空) | `FileDataBlock.records` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | — | — |
| C-12(空) | 同 空 | ✅ | `XlsFormatReaderRealFileTest#readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook` | — | 0 バイトの空ファイルを表す合法な形 |
| C-13(非空) | `MessageDataBlock.directives` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsMessageBlockFromRealBook` | — | `text-encoding` を直接アサート |
| C-13(空) | 同 空 | — | — | — | 到達不能。C-11(空) と同じく NTF 本体の `DataFile` が `file-type` を必ず注入する（`issues.md` XLS-07）。根拠テスト `XlsFormatReaderRealFileTest#readsAllFourSendSyncMessageTypesFromRealBook`（送信系 4 種とも、ディレクティブ行を 1 行も書かずに `file-type` 1 件が入る）。根拠は送信同期経路の 1 本だけである —— 受信 `MESSAGE` 経路でディレクティブ行 0 行を通す根拠テストは `src/test` に無い（辺②の同じ行は 2 経路それぞれの根拠を挙げており、辺で厚みが割れている。未確認。§6.2） |
| C-14(非空) | `MessageDataBlock.fwHeaderFields` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsMessageBlockFromRealBook` | — | — |
| C-14(空) | 同 空 | ✅ | `XlsFormatReaderRealFileTest#readsAllFourSendSyncMessageTypesFromRealBook` | — | 送信系は入力によらず空 Map になる |
| C-15(非空) | `MessageDataBlock.records` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsMessageBlockFromRealBook` | — | — |
| C-15(空) | 同 空 | — | — | — | 到達不能。`MessageDataBlock` が本文レコード 0 件を生成時に拒否する（`issues.md` YML-12 2形目）。根拠テスト `XlsFormatReaderRealFileTest#rejectsMessageWithFwHeaderOnlyInRealBook` ／ `MessageDataBlockTest#本文レコードが0件の電文ブロックは生成できない` |
| C-16(値あり) | `RecordLayout.recordType` 値あり | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | — | — |
| C-16(省略) | 同 省略（`null`） | ✅ | `XlsFormatReaderRealFileTest#readsOmittedRecordTypeAsNullFromRealBook` | — | `""` ではなく `null`（`issues.md` XLS-06 の修正後） |
| C-17(非空) | `RecordLayout.fields` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | — | — |
| C-17(空) | 同 空 | — | — | — | 到達不能。名前行が 2 列未満だと本体側が失敗し、仮に届いても `RecordLayout` が拒否する（`issues.md` XLS-22）。根拠テスト `XlsFormatReaderInvalidInputTest#failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook` ／ `#failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook` ／ `RecordLayoutTest#フィールドを1件も持たないレコードは生成できない`。本体側の番人は 1 つではない —— 根拠テスト 2 件が通すのは `DataFileParser#processDirectives` の `line.size() < 2` だが、これは 1 断片目の名前行にしか効かない。2 断片目以降の名前行は `DataFileParser#onReadingValues` から `createNewFragment` を経て `DataFileFragment#setNames` の `assertNotNullOrEmpty` で閉じる。後者を通す根拠テストは `src/test` に無い（未確認。§6.2 の 8） |
| C-18(非空) | `RecordLayout.rows` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | — | — |
| C-18(空) | 同 空 | ✅ | `XlsFormatReaderRealFileTest#readsEmptyRowsFromRecordLayoutWithoutValueRowsInRealBook` | — | — |
| C-19 | `FieldDef.name` | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | — | `null` は `FieldDef` が拒否する（`FieldDefTest#名称がnullのフィールド定義は生成できない`。`issues.md` XLS-31） |
| C-20(値あり) | `FieldDef.type` 値あり | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | — | NTF 本体が正規化した FW シンボル・実バイト長ではなく、生行の原文が入る |
| C-20(省略) | 同 省略（`null`） | — | — | — | 到達不能。型の欠落は本体パーサが 2 通りの機構で弾き、仮に届いても `FieldDef` が拒否する。根拠テスト `XlsFormatReaderInvalidInputTest#failsWhenTypeRowIsShorterThanNameRowInRealBook`（機構①）／ `#failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook`（機構②）／ `FieldDefTest#データ型がnullのフィールド定義は生成できない` |
| C-21(値あり) | `FieldDef.length` 値あり | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` ／ `#readsOmittedFieldLengthNotationFromRealBook` | — | 後者は長さ省略記法 `-` が原文のまま入ることを固定する |
| C-21(省略) | 同 省略（`null`） | ✅ | `XlsFormatReaderRealFileTest#readsSetupVariableFileBlockWithoutFieldLengthFromRealBook` | — | 到達できるのは可変長ファイルだけ。固定長ファイル・電文で `null` は `ModelPreconditions#requireLengths` が拒否する（`issues.md` XLS-30。`FileDataBlockTest#固定長ファイルでフィールド長がnullのフィールド定義は保持できない`） |

### 1.4 軸D 値の表現（セル種別 8 ケース）

対象は NTF が実行できるテストデータ（全セルが文字列書式）に限る。番号に欠番があるのは
タグを振り直していないためである（`inventory.md` §0.5）。

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| D1-01 | 文字列 | ✅ | `XlsFormatReaderCellTypeTest#readsStringCellAsIs` | `RoundTripTest#specialNotation_isPreservedInBothPaths` | — |
| D1-05 | 先頭ゼロ文字列 | ✅ | `XlsFormatReaderCellTypeTest#readsLeadingZeroStringCellAsIs` | — | `007` が落ちない |
| D1-12 | セル不在 | ✅ | `XlsFormatReaderCellTypeTest#readsAbsentCellAsEmptyString` ／ `#readsAbsentCellInMiddleOfRowAsEmptyString` | — | 行末の不在と行途中の不在を分けて通す |
| D1-13 | 空文字 | ✅ | `XlsFormatReaderCellTypeTest#readsEmptyStringCellAsEmptyString` ／ `#readsBlankCellAsEmptyString` | `RoundTripTest#xls_setupTable_isPreserved` | 3 種とも `""` になり中間モデル上で区別できない（`issues.md` XLS-04） |
| D1-14 | 前後空白 | ✅ | `XlsFormatReaderCellTypeTest#readsSurroundingWhitespacePreserved` | `RoundTripTest#leadingTrailingWhitespace_isPreservedInBothPaths` | — |
| D1-15 | 改行 | ✅ | `XlsFormatReaderCellTypeTest#readsEmbeddedNewlinePreserved` | — | — |
| D1-16 | リテラル `null` | ✅ | `XlsFormatReaderCellTypeTest#readsLiteralNullStringAsString` | `RoundTripTest#nullCell_xlsConvertsToLiteralString_yamlPreservesNull` | — |
| D1-17 | 表示形式 `@` の数値セル | ✅ | `XlsFormatReaderCellTypeTest#readsTextFormattedNumericCellAsDoubleString` | — | 値は `1.0` になる（`issues.md` XLS-01）。仕様外入力の実挙動の記録である |

Excel 保存物と POI 生成物の一致は `XlsReferenceFixtureTest#readsExcelSavedWorkbookIntoIntermediateModel` ／ `XlsReferenceFixtureTest#poiGeneratedWorkbookReadsIdenticallyToExcelSavedWorkbook` が押さえる（軸要素そのものの担保ではない）。

### 1.5 軸E 多重度（11 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| E-1(0件) | セクション内ブロック数 0 | ✅ | `XlsFormatReaderRealFileTest#readsEmptyBlockListFromRealSheetWithoutMarkers` | — | C-04(空) と同じ入力 |
| E-1(1件) | 同 1 | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` | — | ヘルパ `XlsFormatReaderRealFileTest#onlyBlock` が `blocks.size()==1` をアサートする |
| E-1(複数) | 同 複数 | ✅ | `XlsFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealSheet` ／ `#readsAllFourSendSyncMessageTypesFromRealBook` | — | いずれも 4 件 |
| E-2(0件) | ブロック内行数 0 | ✅ | `XlsFormatReaderRealFileTest#readsEmptyRowsFromTableWithoutDataRowsInRealBook` ／ `#readsEmptyRowsFromListMapWithoutDataRowsInRealBook` | — | C-09(空) と同じ入力 |
| E-2(1件) | 同 1 | ✅ | `XlsFormatReaderRealFileTest#readsExpectedTableBlockWithGroupIdFromRealBook` | — | `getRows()` をリスト全体（1 行ぶん）と等値でアサートするため件数が 1 に決まる |
| E-2(複数) | 同 複数 | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` | — | 2 行 |
| E-3(0件) | ファイル内レコードレイアウト数 0 | ✅ | `XlsFormatReaderRealFileTest#readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook` | — | ファイル系だけで到達する。 電文系は C-15(空) と同じ理由で到達不能（根拠テスト `XlsFormatReaderRealFileTest#rejectsMessageWithFwHeaderOnlyInRealBook`） |
| E-3(1件) | 同 1 | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` ／ `#readsMessageBlockFromRealBook` | — | どちらも `records.size()==1` をアサートする |
| E-3(複数) | 同 複数 | ✅ | `XlsFormatReaderRealFileTest#readsMultipleRecordLayoutsFromOneFixedFileInRealBook` | — | ファイル系だけで到達する。 電文系は本体 `MessageParser` が 2 つ目の名前行を値行として吸収するため到達不能（`issues.md` XLS-15。根拠テスト `XlsFormatReaderInvalidInputTest#absorbsSecondNameRowAsDataRowInMessageBodyInRealBook`） |
| E-4(1件) | コンテナ内セクション数 1 | ✅ | `XlsFormatReaderRealFileTest#readsContainerAndSectionNamesFromRealBookAndSheetNames` | — | `sections.size()==1` をアサートする |
| E-4(複数) | 同 複数 | — | — | — | 到達不能。C-02(空) と同じ 1 シート単位 API（`inventory.md` §0.8-6）。根拠テスト `XlsFormatReaderRealFileTest#readsContainerAndSectionNamesFromRealBookAndSheetNames`（`container.getSections().size()` を 1 でアサートする） |

### 1.6 軸F 異常系（6 ケース）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| F1-01 | シート不在 | ✅ | `XlsFormatReaderInvalidInputTest#failsWithSheetNotFoundWhenSheetIsAbsentFromRealBook` | — | `IllegalArgumentException: sheet not found.`（原因例外なし） |
| F1-02 | ブック破損 | ✅ | `XlsFormatReaderInvalidInputTest#failsWithGenericRuntimeExceptionWhenWorkbookIsBroken` | — | 汎用 `RuntimeException: test data file open failed.`。ファイル名はどのメッセージにも出ない（`issues.md` XLS-14） |
| F1-03 | 未知のデータタイプ名 | ✅ | `XlsFormatReaderInvalidInputTest#ignoresBlockWhoseMarkerHasUnknownDataTypeNameInRealBook` ／ `#readsSuffixAfterKnownDataTypeNameAsGroupIdInRealBook` | — | 例外にならず継続する。未知名はブロックごと消え（`issues.md` XLS-10）、既知名＋余分な文字はグループ ID になる（XLS-11） |
| F1-04 | マーカーカラム欠落 | ✅ | `XlsFormatReaderInvalidInputTest#readsMarkerColumnWithoutBracketsAsOrdinaryDataColumnInRealBook` ／ `#dropsFirstFieldWhenSendSyncMetaColumnIsMissingInRealBook` | — | 送信同期のメタ列欠落は先頭フィールドと値を落とす（`issues.md` XLS-13） |
| F1-05 | カラム名重複 | ✅ | `XlsFormatReaderInvalidInputTest#deduplicatesDuplicateColumnNamesWithWarningInListMapFromRealBook` ／ `#deduplicatesDuplicateColumnNamesWithWarningInTableFromRealBook` | — | 後勝ちで除去し WARN ログ 1 件。メッセージに含むことをアサートする項目はメソッドごとに違う（下表） |
| F1-06 | 行と列の数の不一致 | ✅ | `XlsFormatReaderInvalidInputTest#padsShortDataRowAndDropsCellsBeyondColumnRowInRealBook`（代表。全 7 件は下表） | — | 値行の不足は空文字埋め・超過は切り捨てで例外にならない（`issues.md` XLS-12）。名前行・型行・長さ行の不整合は本体パーサが例外で弾く |

#### F1-05 の 2 メソッドがアサートする項目

**「シート名」を見るのは LIST_MAP 側だけである。** テーブル側は見ていない。

| 担保テストメソッド | WARN メッセージに含むことをアサートする項目 |
|---|---|
| `XlsFormatReaderInvalidInputTest#deduplicatesDuplicateColumnNamesWithWarningInListMapFromRealBook` | ブック名 ／ シート名 ／ ブロック識別子 ／ カラム名 ／ 採用列番号（「3 列目」）の 5 つ |
| `XlsFormatReaderInvalidInputTest#deduplicatesDuplicateColumnNamesWithWarningInTableFromRealBook` | ブック名 ／ ブロック識別子 ／ カラム名 ／ 採用列番号（「3 列目」）の 4 つ（シート名は見ていない） |

どちらも「重複は後勝ちで 1 件に絞られる」「採用されるのは後方の列の値」「WARN ログの件数は 1」の
3 点は共通してアサートする。

#### F1-06 を担保する 7 メソッド

| 担保テストメソッド | 挙動 |
|---|---|
| `XlsFormatReaderInvalidInputTest#padsShortDataRowAndDropsCellsBeyondColumnRowInRealBook` | テーブル系の値行。不足は空文字埋め・超過は切り捨て（例外にならない） |
| `XlsFormatReaderInvalidInputTest#padsShortValueRowAndDropsCellsBeyondNameRowInFixedFileInRealBook` | 固定長ファイルの値行。同上 |
| `XlsFormatReaderInvalidInputTest#failsWhenLengthRowIsShorterThanNameRowInRealBook` | 長さ行が名前行より短い。本体パーサが例外 |
| `XlsFormatReaderInvalidInputTest#failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook` | 固定長ファイルの名前行が 2 列未満。本体パーサが例外（C-17(空) が到達不能である根拠を兼ねる） |
| `XlsFormatReaderInvalidInputTest#failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook` | 電文の名前行が 2 列未満。同上 |
| `XlsFormatReaderInvalidInputTest#failsWhenTypeRowIsShorterThanNameRowInRealBook` | 型行が名前行より短い。本体パーサが例外（C-20(省略) の機構①） |
| `XlsFormatReaderInvalidInputTest#failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook` | 型行の途中が空セル。本体パーサが例外（C-20(省略) の機構②） |

**軸F の外に残る空欄**: 継続する異常系（F1-03 ／ F1-04 ／ F1-06 の一部）で
「WARN が出ないこと」は担保テストがアサートしている（`assertNoWarning` ヘルパ）が、
捕捉できるのは `java.util.logging` のルートロガーに届くものだけである。

---

## 2. 辺② YAML→中間モデル（`YamlFormatReader`）

担保の主体は実 `.yaml` を入力とする 3 クラス（`YamlFormatReaderRealFileTest` ／
`YamlFormatReaderScalarTest` ／ `YamlFormatReaderInvalidInputTest`）である。
`YamlFormatReaderTest` は `loadRawMap` を in-memory `Map` へ差し替える経路で、
スカラー解決もスキーマ検証も通らないため §0.2 の基準により ✅ に数えない
（**ただし「拒否されること」の担保としては同クラスが正である行がある** —— C-15(空)・C-21(省略) の理由欄）。

### 2.1 軸A データタイプ（14 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| A-01 | `DEFAULT` | — | — | — | 到達不能。`YamlFormatReader#addBlocksForSection` ／ `#fileDataType` ／ `#addMessageBlocks` は `DEFAULT` 以外の 13 種しか分岐に持たず、さらに `TestDataBlock` が生成時に `DEFAULT` を拒否する。根拠テスト `TestDataBlockTest#データタイプDEFAULTのブロックは生成できない` |
| A-02 | `SETUP_TABLE_DATA` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_setupTable_isPreserved` | 1 ファイルに 13 エントリを書き、`getDataType()` の並びと識別子の並びを記述順で突き合わせる。フィクスチャのセクション順は定義順の逆にしてある |
| A-03 | `EXPECTED_TABLE_DATA` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_expectedTable_withGroupId_isPreserved` | 同上 |
| A-04 | `EXPECTED_COMPLETED` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_expectedCompleteTable_isPreserved` | 同上 |
| A-05 | `LIST_MAP` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_listMap_isPreserved` | 同上 |
| A-06 | `SETUP_FIXED` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_setupFixed_isPreserved` | 同上 |
| A-07 | `EXPECTED_FIXED` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_expectedFixed_isPreserved` | 同上 |
| A-08 | `SETUP_VARIABLE` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_setupVariable_isPreserved` | 同上 |
| A-09 | `EXPECTED_VARIABLE` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_expectedVariable_isPreserved` | 同上 |
| A-10 | `MESSAGE` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_message_isPreserved` | 同上 |
| A-11 | `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_expectedRequestHeaderMessages_isPreserved` | 同上 |
| A-12 | `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_expectedRequestBodyMessages_isPreserved` | 同上 |
| A-13 | `RESPONSE_HEADER_MESSAGES` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_responseHeaderMessages_isPreserved` | 同上 |
| A-14 | `RESPONSE_BODY_MESSAGES` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_responseBodyMessages_isPreserved` | 同上 |

### 2.2 軸B ブロック実装（4 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| B-1 | `TableDataBlock` | ✅ | `YamlFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealYaml` | `RoundTripTest#yaml_setupTable_isPreserved` | 1 ファイルから 4 実装を生成し、実装クラスと識別子（`T` ／ `lm` ／ `f.dat` ／ `RM01`）を突き合わせる |
| B-2 | `ListMapBlock` | ✅ | `YamlFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealYaml` | `RoundTripTest#yaml_listMap_isPreserved` | 同上 |
| B-3 | `FileDataBlock` | ✅ | `YamlFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealYaml` | `RoundTripTest#yaml_setupFixed_isPreserved` | 同上 |
| B-4 | `MessageDataBlock` | ✅ | `YamlFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealYaml` | `RoundTripTest#yaml_message_isPreserved` | 同上 |

### 2.3 軸C 中間モデル全フィールド（36 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| C-01 | `TestDataContainer.name` | ✅ | `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks` | — | リソース名がコンテナの名前になる |
| C-02(非空) | `TestDataContainer.sections` 非空 | ✅ | `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks` | — | 常に 1 件 |
| C-02(空) | 同 空 | — | — | — | 到達不能。`YamlFormatReader#read` が `Collections.singletonList(section)` を返す 1 リソース単位 API（`inventory.md` §0.8-6）。根拠テスト `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks`（`container.getSections().size()` を 1 でアサートする） |
| C-03 | `TestDataSection.name` | ✅ | `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks` | — | — |
| C-04(非空) | `TestDataSection.blocks` 非空 | ✅ | `YamlFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealYaml` | — | — |
| C-04(空) | 同 空 | ✅ | `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks` | — | `setup_tables: []` で到達する |
| C-05 | `TestDataBlock.dataType` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | — | 13 種を記述順でアサートする |
| C-06(値あり) | `TestDataBlock.groupId` 値あり | ✅ | `YamlFormatReaderRealFileTest#readsSendSyncEntryWithoutGroupIdAsDefaultGroupFromRealYaml` | `RoundTripTest#yaml_expectedTable_withGroupId_isPreserved` | `group_id: "g"` が `[g]` へ整形されて入る |
| C-06(省略) | 同 省略（`""`） | ✅ | `YamlFormatReaderRealFileTest#readsSendSyncEntryWithoutGroupIdAsDefaultGroupFromRealYaml` ／ `#readsEmptyColumnNamesAndRowsFromTableWithoutRows` | `RoundTripTest#yaml_setupTable_isPreserved` | 省略時は空文字（デフォルトグループ。`issues.md` YML-02 の修正後） |
| C-07 | `TestDataBlock.identifier` | ✅ | `YamlFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealYaml` | — | 4 実装とも識別子を突き合わせる |
| C-08(非空) | `ColumnRowDataBlock.columnNames` 非空 | ✅ | `YamlFormatReaderRealFileTest#preservesListMapColumnOrderAndExcludesMarkerFromRealYaml` | — | 辞書順ではなく原文の記述順であることまで固定する |
| C-08(空) | 同 空 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyColumnNamesAndRowsFromTableWithoutRows` ／ `#readsEmptyColumnNamesAndRowsFromListMapWithoutRows` | — | `rows: []` で到達する。0 件テーブルに残る担保の穴は §7 の ①〜⑧ |
| C-09(非空) | `ColumnRowDataBlock.rows` 非空 | ✅ | `YamlFormatReaderRealFileTest#readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml` | — | — |
| C-09(空) | 同 空 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyColumnNamesAndRowsFromTableWithoutRows` ／ `#readsEmptyColumnNamesAndRowsFromListMapWithoutRows` | — | C-08(空) と同じ入力 |
| C-10 | `FileDataBlock.fileType`（FIXED ／ VARIABLE の双方） | ❌ | `YamlFormatReaderRealFileTest#readsEmptyRecordsFromFixedFileWithoutRecords`（FIXED 側のみ） | — | **VARIABLE 側を実ファイル経路でアサートしているテストが無い。** 従来 VARIABLE 側の根拠に挙げていた `YamlFormatReaderRealFileTest#readsInjectedDirectivesEvenWhenDirectivesAreOmittedInVariableFile` は `getDirectives().get("file-type")` が `Variable` であることを見ており、これは C-11(非空) の担保であって `fileType` ではない。VARIABLE 側を `getFileType()` で押さえているのは `YamlFormatReaderTest` の in-memory 経路（`loadRawMap` 差し替え）だけで、§0.2 によりこれは ✅ に数えない。Acceptance criteria の「`FileDataBlock.fileType` は `FIXED`／`VARIABLE` の両方を通す」に触れる。スキーマ `$defs.file_data.type` が必須かつ `enum` ＝ `["fixed","variable"]` のため「省略」は存在しない（行を割らない理由）。導出は下のコマンド |
| C-11(非空) | `FileDataBlock.directives` 非空 | ✅ | `YamlFormatReaderRealFileTest#stringifiesNonStringDirectiveValuesFromRealYaml` | — | integer ／ boolean の記法も文字列になることまで固定する |
| C-11(空) | 同 空 | — | — | — | 到達不能。NTF 本体の `DataFile` のコンストラクタが `file-type` を必ず注入する（`issues.md` XLS-07）。根拠テスト `YamlFormatReaderRealFileTest#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInFile`（件数 1 をアサート） |
| C-12(非空) | `FileDataBlock.records` 非空 | ✅ | `YamlFormatReaderRealFileTest#readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml` | — | — |
| C-12(空) | 同 空 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyRecordsFromFixedFileWithoutRecords` | — | スキーマ `$defs.file_data` は `records.minItems` ＝ 0 |
| C-13(非空) | `MessageDataBlock.directives` 非空 | ✅ | `YamlFormatReaderRealFileTest#readsMessageDirectivesFromRealYaml` | — | — |
| C-13(空) | 同 空 | — | — | — | 到達不能。C-11(空) と同じく NTF 本体の `DataFile` が `file-type` を必ず注入する（`issues.md` XLS-07）。根拠テストは 2 つの生成経路それぞれにある —— 受信メッセージ経路が `YamlFormatReaderRealFileTest#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInMessage`、送信系経路が `#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInSendSync` |
| C-14(非空) | `MessageDataBlock.fwHeaderFields` 非空 | ✅ | `YamlFormatReaderRealFileTest#readsFwHeaderFieldsFromRealYaml` | — | 記述順で入ることまで固定する |
| C-14(空) | 同 空 | ✅ | `YamlFormatReaderRealFileTest#keepsFwHeaderNamedRecordInMessageFromRealYaml` | — | `fw_header:` を書かない入力で空 Map になる |
| C-15(非空) | `MessageDataBlock.records` 非空 | ✅ | `YamlFormatReaderRealFileTest#keepsFwHeaderNamedRecordInMessageFromRealYaml` | — | `record_type: FW_HEADER` のレコードも落とさない（`issues.md` YML-03 の解消後） |
| C-15(空) | 同 空 | — | — | — | 到達不能。仮にスキーマを通っても `MessageDataBlock` が生成時に拒否する（`issues.md` YML-12 2形目）。根拠テスト `MessageDataBlockTest#本文レコードが0件の電文ブロックは生成できない` ／ `YamlFormatReaderTest#readMessage_emptyBody_rejected`。後者は `loadRawMap` を固定 Map に差し替える経路で走るためスキーマ検証を通っておらず、アサートしているのはモデル側の拒否である。スキーマ `$defs.message_data.records.minItems` ＝ 1 が先に閉じることの出典は本体スキーマ本体（下のコマンド）であって、これをアサートするテストは `src/test` に無い |
| C-16(値あり) | `RecordLayout.recordType` 値あり | ✅ | `YamlFormatReaderRealFileTest#readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml` | — | `head` をアサートする。2 件目の `data` は `getRecordType()` を見ておらず、固定しているのはレコード数 2 件と 2 件目の行数だけである |
| C-16(省略) | 同 省略（`null`） | ✅ | `YamlFormatReaderRealFileTest#readsEmptyRowsFromRecordLayoutWithoutRows` ／ `#normalizesLowercaseDefaultRecordTypeToNull` | — | 後者は `"default"`（小文字）も `null` へ正規化されることを固定する |
| C-17(非空) | `RecordLayout.fields` 非空 | ✅ | `YamlFormatReaderRealFileTest#preservesFieldOrderAndValueAlignmentFromRealYaml` | — | 辞書順ではなく原文の記述順であることまで固定する |
| C-17(空) | 同 空 | — | — | — | 到達不能。スキーマ `$defs.record_fragment.properties.fields.minItems` ＝ 1 で書けず、仮に届いても `RecordLayout` が拒否する。根拠テスト `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFieldsIsEmpty` ／ `RecordLayoutTest#フィールドを1件も持たないレコードは生成できない` |
| C-18(非空) | `RecordLayout.rows` 非空 | ✅ | `YamlFormatReaderRealFileTest#preservesFieldOrderAndValueAlignmentFromRealYaml` | — | 値もフィールドの記述順に対応することまで固定する |
| C-18(空) | 同 空 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyRowsFromRecordLayoutWithoutRows` | — | — |
| C-19 | `FieldDef.name` | ✅ | `YamlFormatReaderRealFileTest#preservesFieldOrderAndValueAlignmentFromRealYaml` | — | `null` は `FieldDef` が拒否する（`FieldDefTest#名称がnullのフィールド定義は生成できない`） |
| C-20(値あり) | `FieldDef.type` 値あり | ✅ | `YamlFormatReaderRealFileTest#readsIntegerLengthNotationAsString` ／ `#readsSendSyncEntryWithoutGroupIdAsDefaultGroupFromRealYaml` | — | どちらも `getType()` が `半角英字` であることをアサートする。従来挙げていた `YamlFormatReaderRealFileTest#preservesFieldOrderAndValueAlignmentFromRealYaml` は入力に `type` を書くだけで `getType()` を呼ばないため差し替えた |
| C-20(省略) | 同 省略（`null`） | — | — | — | 到達不能。スキーマ `$defs.field_def.required` が `type` を必須とし、仮に届いても `FieldDef` が拒否する。根拠テスト `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFieldTypeIsMissing` ／ `FieldDefTest#データ型がnullのフィールド定義は生成できない` |
| C-21(値あり) | `FieldDef.length` 値あり | ✅ | `YamlFormatReaderRealFileTest#readsIntegerLengthNotationAsString` ／ `#readsSendSyncEntryWithoutGroupIdAsDefaultGroupFromRealYaml` | — | 前者は integer 記法 `length: 10` が文字列 `"10"` になることを固定する。従来挙げていた `YamlFormatReaderRealFileTest#preservesFieldOrderAndValueAlignmentFromRealYaml` は `getLength()` を呼ばないため差し替えた |
| C-21(省略) | 同 省略（`null`） | ✅ | `YamlFormatReaderRealFileTest#readsInjectedDirectivesEvenWhenDirectivesAreOmittedInVariableFile` | — | 到達できるのは可変長ファイルだけ。固定長ファイルで `length` を書かない YAML はスキーマを通るが中間モデルの生成時に拒否される（`issues.md` XLS-30。`YamlFormatReaderTest#readFile_fixedWithoutLength_rejected`） |
C-15(空) の理由欄が引くスキーマ側の出典（テストではなく本体スキーマそのものを読む）:

```sh
unzip -p "$(find ~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml \
  -name 'nablarch-testing-yaml-1.0.0-SNAPSHOT.jar')" \
  nablarch/test/ntf-testdata-yaml-schema.json |
  python3 -c 'import json,sys; d=json.load(sys.stdin)["$defs"]; [print(k, d[k]["required"], d[k]["properties"]["records"]["minItems"]) for k in ("message_data","expected_request_message_data","group_message_data")]'
```

出力は 3 行で、`message_data` ／ `expected_request_message_data` ／ `group_message_data` の
いずれも `['id', 'records'] 1` である。バージョンは `pom.xml` の `nablarch-testing-yaml` の
`<version>` に合わせる。


C-10 の ❌ の根拠（辺②のテストで `getFileType()` を呼ぶ箇所を全部数える）:

```sh
cd "$(git rev-parse --show-toplevel)"
grep -rn 'getFileType()' src/test/java/nablarch/test/tool/converter/yaml --include=*.java
```

出力は 8 行で、実ファイル経路のクラス `YamlFormatReaderRealFileTest` は 1 行だけ、しかも
`FileType.FIXED` である。`FileType.VARIABLE` を見る 3 行のうち `YamlFormatReaderTest` の 2 行は
in-memory 経路、`YamlFormatWriterModelTest` の 1 行は辺④の読み戻しであって辺②の担保ではない。

### 2.4 軸D 値の表現（YAML スカラー 12 ケース）

対象は本体スキーマを通る入力に限る（`rows` の値の型が `["string","null"]` に限られるため、
引用符なしの `true` ／ `123` ／ `1.50` ／ `.inf` ／ `.nan` は対象外。`inventory.md` §0.5）。
担保テストはすべて `YamlFormatReaderScalarTest` にある。

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| D2-01 | 引用符なし文字列 | ✅ | `YamlFormatReaderScalarTest#readsUnquotedStringAsIs` | — | `abc` → `"abc"` |
| D2-02 | 引用符あり | ✅ | `YamlFormatReaderScalarTest#readsDoubleQuotedStringWithoutQuotes` ／ `#readsSingleQuotedStringWithoutQuotes` | `RoundTripTest#yaml_setupTable_isPreserved` | 引用の別は中間モデルに残らない |
| D2-03 | 引用符付き数値 | ✅ | `YamlFormatReaderScalarTest#readsQuotedNumberAsString` | `YamlFormatWriterTest#roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader` | `"123"` → `"123"` |
| D2-04 | 引用符付き末尾ゼロ小数 | ✅ | `YamlFormatReaderScalarTest#readsQuotedTrailingZeroDecimalAsString` | — | `"1.50"` → `"1.50"` |
| D2-05 | 真偽値に見える文字列 | ✅ | `YamlFormatReaderScalarTest#readsQuotedTrueAsString` ／ `#readsUppercaseTrueAsString` ／ `#readsYesAsString` | — | `"true"` ／ `TRUE` ／ `yes` とも記法どおりの文字列 |
| D2-06 | NULL | ✅ | `YamlFormatReaderScalarTest#readsUnquotedNullAsJavaNull` ／ `#readsOmittedValueAsJavaNull` | `RoundTripTest#nullCell_xlsConvertsToLiteralString_yamlPreservesNull` | 引用符なし `null` と値なしだけが Java `null` になる |
| D2-07 | NULL に見える文字列 | ✅ | `YamlFormatReaderScalarTest#readsQuotedNullAsString` ／ `#readsTildeAsString` ／ `#readsUppercaseNullAsString` | — | `"null"` ／ `~` ／ `NULL` はいずれも文字列（`issues.md` YML-01） |
| D2-08 | 日付・日時風文字列 | ✅ | `YamlFormatReaderScalarTest#readsDateLikeStringAsIs` ／ `#readsDateTimeLikeStringAsIs` | — | — |
| D2-09 | 複数行（リテラルブロック・折りたたみブロック） | ✅ | `YamlFormatReaderScalarTest#readsLiteralBlockScalarKeepingNewlines` ／ `#readsFoldedBlockScalarFoldingNewlinesIntoSpaces` | — | リテラルブロック記法は `"l1\nl2\n"`、折りたたみ記法 `>` は `"l1 l2\n"`（いずれも末尾に改行が付く） |
| D2-10 | 先頭ゼロ・非 JSON 数値記法 | ✅ | `YamlFormatReaderScalarTest#readsLeadingZeroNumberAsString` ／ `#readsHexNotationAsString` | — | `007` ／ `0x1F` とも記法どおりの文字列 |
| D2-11 | 空文字・前後空白 | ✅ | `YamlFormatReaderScalarTest#readsEmptyStringAsIs` ／ `#readsSurroundingWhitespacePreserved` | — | 引用符なしで書くと前後空白は落ちる（`issues.md` YML-11） |
| D2-12 | 特殊文字を含む文字列 | ✅ | `YamlFormatReaderScalarTest#readsColonContainingStringAsIs` ／ `#readsHashContainingStringAsIs` | — | 引用符なしで書くと `#` 以降が落ちる（`issues.md` YML-11） |

**軸D の外に残る空欄**: 12 ケースのうち 10 ケースは `setup_tables` の 1 経路でしか測っていない。
LIST_MAP 経路とレコード断片経路で測ったのは D2-06 と D2-11 の 2 ケースだけである
（`YamlFormatReaderScalarTest#readsUnquotedNullAsJavaNullInListMapPath` ／ `YamlFormatReaderScalarTest#readsUnquotedNullAsJavaNullInRecordFragmentPath` ／ `YamlFormatReaderScalarTest#readsEmptyStringAsIsInListMapPath` ／ `YamlFormatReaderScalarTest#readsEmptyStringAsIsInRecordFragmentPath`。2 ケースとも経路差は無かった）。
**残る 10 ケースに経路差が無いことは未確認である**（`inventory.md` §2.1-2 の開示）。

### 2.5 軸E 多重度（11 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| E-1(0件) | セクション内ブロック数 0 | ✅ | `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks` | — | C-04(空) と同じ入力 |
| E-1(1件) | 同 1 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyColumnNamesAndRowsFromTableWithoutRows` | — | ヘルパ `YamlFixture#onlyBlock` が `blocks.size()==1` をアサートする |
| E-1(複数) | 同 複数 | ✅ | `YamlFormatReaderRealFileTest#readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml` ／ `#readsAllThirteenDataTypesFromRealYaml` | — | 3 件／13 件 |
| E-2(0件) | ブロック内行数 0 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyColumnNamesAndRowsFromTableWithoutRows` ／ `#readsEmptyColumnNamesAndRowsFromListMapWithoutRows` | — | C-09(空) と同じ入力 |
| E-2(1件) | 同 1 | ✅ | `YamlFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealYaml` | — | テーブル・LIST_MAP とも `getRows().size()` が 1 であることをアサートする |
| E-2(複数) | 同 複数 | ✅ | `YamlFormatReaderRealFileTest#readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml` | — | 2 行 |
| E-3(0件) | ファイル内レコードレイアウト数 0 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyRecordsFromFixedFileWithoutRecords` | — | ファイル系だけで到達する。 電文系は C-15(空) と同じ理由で到達不能 |
| E-3(1件) | 同 1 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyRowsFromRecordLayoutWithoutRows` | — | `getRecords().size()` を 1 でアサートする |
| E-3(複数) | 同 複数 | ✅ | `YamlFormatReaderRealFileTest#readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml` | — | 断片 2 件 |
| E-4(1件) | コンテナ内セクション数 1 | ✅ | `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks` | — | `sections.size()==1` をアサートする |
| E-4(複数) | 同 複数 | — | — | — | 到達不能。C-02(空) と同じ 1 リソース単位 API（`inventory.md` §0.8-6）。根拠テスト `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks`（`container.getSections().size()` を 1 でアサートする） |

### 2.6 軸F 異常系（5 ケース）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| F2-01 | スキーマ違反 | ✅ | `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFileTypeIsNotInEnum` ／ `#failsWithSchemaValidationExceptionWhenFieldLengthDoesNotMatchPattern` | — | 違反キーワードの集合と位置を件数つきでアサートする（報告順は `Set` の反復順が契約されていないため突き合わせない） |
| F2-02 | YAML として不正 | ✅ | `YamlFormatReaderInvalidInputTest#failsWithParseErrorWhenYamlIsMalformed` | — | `IllegalStateException`（原因は `YamlEngineException`）。パースで止まるためスキーマ検証には到達しない |
| F2-03 | 未知のキー | ✅ | `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenTopLevelKeyIsUnknown` | — | スキーマのルートが `additionalProperties: false` のため実ファイルでは読み込みごと失敗する（in-memory 経路の「未知キーは無視」とは結果が異なる） |
| F2-04 | 必須構造の欠落 | ✅ | `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenRequiredRowsIsMissing` ／ `#failsWithSchemaValidationExceptionWhenFieldsIsEmpty` ／ `#failsWithSchemaValidationExceptionWhenFieldTypeIsMissing` | — | 後ろ 2 件は C-17(空) ／ C-20(省略) が到達不能である根拠を兼ねる |
| F2-05 | 空ファイル | ✅ | `YamlFormatReaderInvalidInputTest#readsEmptyFileAsContainerWithoutBlocks` | — | 例外にならず、コンテナ 1 件・セクション 1 件・ブロック 0 件になる |

**軸F の外に残る空欄**: スキーマの自由度を突いた入力の現状挙動は `YamlFormatReaderInvalidInputTest` の
掃引テスト群（`issues.md` YML-04〜YML-11）が固定しているが、これらは軸A〜F のどの要素にも属さない。
自由度どうしの組合せは通していない（`inventory.md` §2.1-2 の「この掃引で見ていない範囲」）。

---

## 3. 辺③ 中間モデル→Excel（`XlsFormatWriter`）

担保の主体は `XlsFormatWriterTest`（`build` でメモリ上のブックを作るものと実ファイルを書くものの両方を含む）・
`XlsFormatWriterModelTest`（**全件が実 `.xlsx` を書いて開き直す**）・`XlsFormatWriterCellTypeTest`・
`XlsFormatWriterInvalidOutputTest` の 4 クラスである。

`XlsFormatWriterModelTest` の「全件」の導出:

```sh
cd "$(git rev-parse --show-toplevel)"
M=src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterModelTest.java
grep -cE '^[[:space:]]*@Test$' "$M"       # テストメソッド数
grep -c 'writeAndReopen' "$M"             # 定義 2 ＋ 内部呼び出し 1 ＋ 呼び出し
```

出力は 11 ／ 14。14 の内訳は `writeAndReopen` ／ `writeAndReopenSheet` の定義 2 行、
`writeAndReopenSheet` が `writeAndReopen` を呼ぶ 1 行、テストメソッドからの呼び出し 11 行であり、
テストメソッド 11 件と呼び出し 11 件が 1 対 1 で対応する。

**辺③では軸B が軸A から独立していない。** `XlsFormatWriter#layout` は `ColumnRowDataBlock` ／
`FileDataBlock` ／ `MessageDataBlock` の 3 分岐しか持たず、`TableDataBlock` と `ListMapBlock` は
どちらも `layoutColumnRow` を通る。版面上で両者を分けるのは `getDataType()` から作る識別セルだけで、
それは軸A そのものである（`inventory.md` §3.3 の B 行）。

### 3.1 軸A データタイプ（14 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| A-01 | `DEFAULT` | — | — | — | 到達不能。`TestDataBlock` が `DEFAULT` のブロックを生成時に拒否するため入力を組めない（`issues.md` XLS-20。`steering.md` #25.5 §1-G）。根拠テスト `TestDataBlockTest#データタイプDEFAULTのブロックは生成できない`。#23 が辺③に置いていた担保テスト 2 件は入力を組めなくなったため削除済みで、HEAD に該当メソッドは無い（`inventory.md` §0.1-2 の追補その 5 の削除一覧） |
| A-02 | `SETUP_TABLE_DATA` | ✅ | `XlsFormatWriterTest#writesTableBlock` | `RoundTripTest#xls_setupTable_isPreserved` ／ `XlsFormatWriterTest#roundTripsTable` | 識別セル `SETUP_TABLE=USERS` をアサートする |
| A-03 | `EXPECTED_TABLE_DATA` | ✅ | `XlsFormatWriterTest#writesTableMarkerWithGroupId` | `RoundTripTest#xls_expectedTable_withGroupId_isPreserved` | 識別セル `EXPECTED_TABLE[g1]=USERS` |
| A-04 | `EXPECTED_COMPLETED` | ✅ | `XlsFormatWriterTest#writesExpectedCompleteTableMarker` | `RoundTripTest#xls_expectedCompleteTable_isPreserved` | 識別セル `EXPECTED_COMPLETE_TABLE=USERS` |
| A-05 | `LIST_MAP` | ✅ | `XlsFormatWriterTest#writesListMapBlock` | `RoundTripTest#xls_listMap_isPreserved` ／ `XlsFormatWriterTest#roundTripsListMap` | 識別セル `LIST_MAP=result` |
| A-06 | `SETUP_FIXED` | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | `RoundTripTest#xls_setupFixed_isPreserved` ／ `XlsFormatWriterTest#roundTripsFixedFile` | 識別セル `SETUP_FIXED=test.dat` |
| A-07 | `EXPECTED_FIXED` | ✅ | `XlsFormatWriterModelTest#writesExpectedFixedFileBlockWithLengthRow` | `RoundTripTest#xls_expectedFixed_isPreserved` | 識別セル `EXPECTED_FIXED=exp.dat` と固定長固有の長さ行 |
| A-08 | `SETUP_VARIABLE` | ✅ | `XlsFormatWriterTest#writesVariableFileWithoutLengthRow` | `RoundTripTest#xls_setupVariable_isPreserved` ／ `XlsFormatWriterTest#roundTripsVariableFile` | 識別セル `SETUP_VARIABLE=in.csv` |
| A-09 | `EXPECTED_VARIABLE` | ✅ | `XlsFormatWriterModelTest#writesExpectedVariableFileBlockWithoutLengthRow` | `RoundTripTest#xls_expectedVariable_isPreserved` | 識別セル `EXPECTED_VARIABLE[g2]=exp.csv`。可変長なので長さ行なし |
| A-10 | `MESSAGE` | ✅ | `XlsFormatWriterTest#writesMessageBlock` | `RoundTripTest#xls_message_isPreserved` ／ `XlsFormatWriterTest#roundTripsMessage` | 識別セル `MESSAGE=msg1` |
| A-11 | `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `XlsFormatWriterTest#writesSendSyncMessageWithSequenceNo` | `RoundTripTest#xls_expectedRequestHeaderMessages_isPreserved` ／ `XlsFormatWriterTest#roundTripsSendSyncMessage` | 識別セル全体をアサートする |
| A-12 | `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `XlsFormatWriterModelTest#writesExpectedRequestBodyMessagesMarker` | `RoundTripTest#xls_expectedRequestBodyMessages_isPreserved` | #18〜#23 当初版は ✅ と誤判定していた（`XlsFormatWriterTest#writesSequenceNoForAllSendSyncTypes` は 4 タイプ共通の連番しか見ていない）。#23 レビュー対応で追加（`inventory.md` §3.1-3） |
| A-13 | `RESPONSE_HEADER_MESSAGES` | ✅ | `XlsFormatWriterModelTest#writesResponseHeaderMessagesMarker` | `RoundTripTest#xls_responseHeaderMessages_isPreserved` | 同上 |
| A-14 | `RESPONSE_BODY_MESSAGES` | ✅ | `XlsFormatWriterModelTest#writesResponseBodyMessagesMarker` | `RoundTripTest#xls_responseBodyMessages_isPreserved` | 同上 |

### 3.2 軸B ブロック実装（4 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| B-1 | `TableDataBlock` | ✅ | `XlsFormatWriterTest#writesTableBlock` | `XlsFormatWriterTest#roundTripsTable` | 版面（識別行 → カラム名行 → データ行）を行単位でアサートする |
| B-2 | `ListMapBlock` | ✅ | `XlsFormatWriterTest#writesListMapBlock` | `XlsFormatWriterTest#roundTripsListMap` | B-1 と同じ `layoutColumnRow` を通る（本節冒頭）。テストを足しても通る `src/main` の経路は増えない |
| B-3 | `FileDataBlock` | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | `XlsFormatWriterTest#roundTripsFixedFile` | — |
| B-4 | `MessageDataBlock` | ✅ | `XlsFormatWriterTest#writesMessageBlock` | `XlsFormatWriterTest#roundTripsMessage` | — |

### 3.3 軸C 中間モデル全フィールド（36 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| C-01 | `TestDataContainer.name` | ✅ | `XlsFormatWriterTest#writesWorkbookFileWithSheetPerSection` | — | コンテナの名前が `MyBook.xlsx` になる |
| C-02(非空) | `TestDataContainer.sections` 非空 | ✅ | `XlsFormatWriterTest#writesWorkbookFileWithSheetPerSection` | — | 2 セクション → 2 シート |
| C-02(空) | 同 空 | ✅ | `XlsFormatWriterModelTest#writesWorkbookWithoutSheetsWhenContainerHasNoSections` | — | 例外にならずシート 0 枚のブックが書かれる（`issues.md` XLS-23） |
| C-03 | `TestDataSection.name` | ✅ | `XlsFormatWriterTest#writesWorkbookFileWithSheetPerSection` | — | 読み込み単位の名前がシート名になる |
| C-04(非空) | `TestDataSection.blocks` 非空 | ✅ | `XlsFormatWriterTest#writesTableBlock` | — | — |
| C-04(空) | 同 空 | ✅ | `XlsFormatWriterModelTest#writesEmptySheetWhenSectionHasNoBlocks` | — | シートは作られるが行が 1 行も無い |
| C-05 | `TestDataBlock.dataType` | ✅ | `XlsFormatWriterTest#writesExpectedCompleteTableMarker` | — | 識別セルの文字列がデータタイプから組まれる |
| C-06(値あり) | `TestDataBlock.groupId` 値あり | ✅ | `XlsFormatWriterTest#writesTableMarkerWithGroupId` | `RoundTripTest#xls_expectedTable_withGroupId_isPreserved` | `[g1]` が識別セルに現れる |
| C-06(省略) | 同 省略（`""`） | ✅ | `XlsFormatWriterTest#writesTableBlock` | `RoundTripTest#xls_setupTable_isPreserved` | 識別セルに角括弧が出ない |
| C-07 | `TestDataBlock.identifier` | ✅ | `XlsFormatWriterTest#writesTableBlock` | — | — |
| C-08(非空) | `ColumnRowDataBlock.columnNames` 非空 | ✅ | `XlsFormatWriterTest#writesTableBlock` | — | — |
| C-08(空) | 同 空 | ✅ | `XlsFormatWriterTest#writesMarkerColumnForZeroRowTableBlock` ／ `#writesMarkerColumnForZeroRowListMapBlock` | `XlsFormatWriterTest#roundTripsZeroRowTableWithoutEatingNextBlock` ／ `#roundTripsZeroRowListMapWithoutEatingNextBlock` | マーカーカラム 1 列 `[EMPTY]` を書く（`issues.md` XLS-27 の【決着】。#26.5 でセル値を `[空]` から改めた）。カラム名 0 件かつ「セルを持つ行」は `ColumnRowDataBlock` が生成時に拒否する（`TableDataBlockTest#カラムなしでセルを持つ行を抱えるブロックは生成できない`。`issues.md` XLS-21）。残る穴は §7 の ①〜⑧ |
| C-09(非空) | `ColumnRowDataBlock.rows` 非空 | ✅ | `XlsFormatWriterTest#writesTableBlock` | — | — |
| C-09(空) | 同 空 | ✅ | `XlsFormatWriterModelTest#writesTableWithoutDataRowsWhenRowsAreEmpty` | — | 識別行とカラム名行だけ。データ行の位置は行そのものが無い |
| C-10 | `FileDataBlock.fileType`（FIXED ／ VARIABLE の双方） | ✅ | `XlsFormatWriterTest#writesFixedFileBlock`（FIXED＝長さ行が出る）／ `#writesVariableFileWithoutLengthRow`（VARIABLE＝長さ行が出ない） | `XlsFormatWriterTest#roundTripsFixedFile` ／ `#roundTripsVariableFile` | `null` は `FileDataBlock` が生成時に拒否する（`FileDataBlockTest#ファイル種別がnullのファイルブロックは生成できない`。`issues.md` XLS-29） |
| C-11(非空) | `FileDataBlock.directives` 非空 | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | ディレクティブ行が識別行の次に出る |
| C-11(空) | 同 空 | ✅ | `XlsFormatWriterTest#writesVariableFileWithoutLengthRow` | — | ディレクティブ行が 1 行も出ない |
| C-12(非空) | `FileDataBlock.records` 非空 | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | — |
| C-12(空) | 同 空 | ✅ | `XlsFormatWriterModelTest#writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty` | — | 識別行とディレクティブ行だけ。0 バイトの空ファイルを表す合法な形 |
| C-13(非空) | `MessageDataBlock.directives` 非空 | ✅ | `XlsFormatWriterModelTest#writesDirectiveRowsBeforeFwHeaderRowsInMessage` | — | ディレクティブ行が FW 制御ヘッダ行より上に出ることまで固定する |
| C-13(空) | 同 空 | ✅ | `XlsFormatWriterTest#writesMessageBlock` | — | 識別行の次が FW 制御ヘッダ行になる |
| C-14(非空) | `MessageDataBlock.fwHeaderFields` 非空 | ✅ | `XlsFormatWriterTest#writesMessageBlock` | — | — |
| C-14(空) | 同 空 | ✅ | `XlsFormatWriterTest#writesSendSyncMessageWithSequenceNo` | — | 識別行の次が名前行になる |
| C-15(非空) | `MessageDataBlock.records` 非空 | ✅ | `XlsFormatWriterTest#writesMessageBlock` | — | — |
| C-15(空) | 同 空 | — | — | — | 到達不能。`MessageDataBlock` が本文レコード 0 件を生成時に拒否するため入力を組めない（`issues.md` YML-12 2形目。`steering.md` #25.5 §6-J-2）。根拠テスト `MessageDataBlockTest#本文レコードが0件の電文ブロックは生成できない`。#23 が置いた版面テスト 1 件と、その後 #25.5 が辺③に置いた番人テスト 2 件はいずれも削除済みで、HEAD に該当メソッドは無い |
| C-16(値あり) | `RecordLayout.recordType` 値あり | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | 名前行の列 0 にレコード種別が出る |
| C-16(省略) | 同 省略（`null`） | ✅ | `XlsFormatWriterTest#allowsNullRecordTypeOnSingleRecord` | — | 名前行の列 0 が空セルになる。2 レコード目以降の `null` ／ `""` は `IllegalStateException`（`XlsFormatWriterTest#rejectsNullRecordTypeOnSecondRecord` ／ `XlsFormatWriterTest#rejectsEmptyRecordTypeOnSecondRecord`） |
| C-17(非空) | `RecordLayout.fields` 非空 | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | — |
| C-17(空) | 同 空 | — | — | — | 到達不能。`RecordLayout` がフィールド 0 件を生成時に拒否するため入力を組めない（`issues.md` XLS-22）。根拠テスト `RecordLayoutTest#フィールドを1件も持たないレコードは生成できない`。辺③に置いていた番人テスト 2 件は空振りになったため削除済み |
| C-18(非空) | `RecordLayout.rows` 非空 | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | — |
| C-18(空) | 同 空 | ✅ | `XlsFormatWriterModelTest#writesRecordWithoutDataRowsWhenRecordRowsAreEmpty` | — | 名前行・型行・長さ行まで。データ行は行そのものが無い |
| C-19 | `FieldDef.name` | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | 名前行に出る。`null` は `FieldDef` が拒否する（`FieldDefTest#名称がnullのフィールド定義は生成できない`） |
| C-20(値あり) | `FieldDef.type` 値あり | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | 型行に出る。空文字は弾かれず空セルになる（`XlsFormatWriterTest#writesOmittedMetaAndFieldAsEmpty`） |
| C-20(省略) | 同 省略（`null`） | — | — | — | 到達不能。`FieldDef` が `type` ＝ `null` を生成時に拒否するため入力を組めない（`issues.md` XLS-31。`steering.md` #25.5 §1-D）。根拠テスト `FieldDefTest#データ型がnullのフィールド定義は生成できない`。境界（空文字は通す）は `FieldDefTest#データ型が空文字のフィールド定義は生成できる` |
| C-21(値あり) | `FieldDef.length` 値あり | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | 長さ行に `-` ／ `5` が原文のまま出る |
| C-21(省略) | 同 省略（`null`） | ❌ | `XlsFormatWriterTest#writesVariableFileWithoutLengthRow`（`length` の値には無反応） | `XlsFormatWriterTest#roundTripsVariableFile` | **省略された `length` が出力に現れないことを、値に反応する形でアサートしているテストが無い。** 到達できるのは可変長ファイルだけだが、`XlsFormatWriter#appendRecord` は長さ行を `if (fixed)` の中でしか作らないため、可変長では `getLength()` が 1 度も読まれない。したがって同じ入力の `length` を `null` から `"5"` に変えても出力は 1 行も変わらず、根拠テストは緑のままである。行 3 がデータ行であることは C-10 の VARIABLE 側の担保であって、この行の主張ではない。**テストを足しても埋まらない**（辺③の出力が可変長では `length` に依存しない）。他の (省略)／(空) 行（C-06(省略)・C-16(省略)・C-11(空)・C-13(空)・C-14(空)）はいずれも値を入れれば落ちる形であり、本行だけが例外である。なお固定長ファイル・電文の `null` は `ModelPreconditions#requireLengths` が拒否する（`issues.md` XLS-30。`FileDataBlockTest#固定長ファイルでフィールド長がnullのフィールド定義は保持できない` ／ `MessageDataBlockTest#フィールド長がnullの電文ブロックは生成できない`） |

### 3.4 軸D 値の表現（セル型 8 ケース。すべて `getCellType()` をアサート）

担保テストはすべて `XlsFormatWriterCellTypeTest` にある。**同クラス 18 件のうち 16 件は書き出した実 `.xlsx` を
POI で開き直して確かめ、残る 2 件は `xl/sharedStrings.xml` の生バイトを読む**（後者が D3-06 ／ D3-08 の
「どの区間で変化が起きるか」を決める証拠である）。

```sh
cd "$(git rev-parse --show-toplevel)"
C=src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterCellTypeTest.java
grep -cE '^[[:space:]]*@Test$' "$C"     # テストメソッド数
grep -c 'sharedStringsXml(' "$C"        # 生バイト経路（定義 1 ＋ 呼び出し）
grep -c 'writeAndReopen(' "$C"          # 開き直し経路（定義 1 ＋ 呼び出し）
```

出力は 18 ／ 3 ／ 13。生バイト経路は定義 1 行と呼び出し 2 行、開き直し経路は定義 1 行と
呼び出し 12 行（テストメソッドから直接 10 件、ヘルパ `XlsFormatWriterCellTypeTest#assertReplacedWithQuestionMark`
と `XlsFormatWriterCellTypeTest#assertWrittenAsIs` から 1 件ずつ）で、後者のヘルパ 2 つを 6 件のテストメソッドが使うため
開き直しに届くテストメソッドは 10 ＋ 6 ＝ 16 件になる。

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| D3-01 | `"100"` | ✅ | `XlsFormatWriterCellTypeTest#writesNumericLookingStringAsStringCell` | — | 数値セルにならない（`getNumericCellValue()` が `IllegalStateException`） |
| D3-02 | `"=1+1"` | ✅ | `XlsFormatWriterCellTypeTest#writesFormulaLookingStringAsStringCell` | — | 数式セルにならない |
| D3-03 | `"007"` | ✅ | `XlsFormatWriterCellTypeTest#writesLeadingZeroStringAsStringCell` | — | 先頭ゼロが落ちない |
| D3-04 | `null` | ✅ | `XlsFormatWriterCellTypeTest#writesNullValueAsLiteralNullStringCell` | `RoundTripTest#nullCell_xlsConvertsToLiteralString_yamlPreservesNull` ／ `XlsFormatWriterTest#roundTripsNullCellAsLiteralNullString` | リテラル `"null"` になる（空白セルにならない） |
| D3-05 | `""` | ✅ | `XlsFormatWriterCellTypeTest#writesEmptyValueAsEmptyStringCell` | `XlsFormatWriterTest#roundTripsTable` | 長さ 0 の文字列セル（`CELL_TYPE_BLANK` へ退化しない） |
| D3-06 | 改行含む文字列 | ✅ | `XlsFormatWriterCellTypeTest#writesLineFeedStringAsStringCell`（代表。全 4 件は下表） | — | `CR` は `LF` へ置換される（`issues.md` XLS-18）。変化が起きるのは読み戻し（XML パース）区間であることを、生バイトを読む 1 件が示す |
| D3-07 | 32767 文字超 | ✅ | `XlsFormatWriterCellTypeTest#writesStringLongerThanExcelCellLimitAsStringCell` ／ `#writesStringOfExcelCellLimitLengthAsStringCell` | — | 切り詰め・例外なし（`issues.md` XLS-19） |
| D3-08 | 制御文字含む | ✅ | `XlsFormatWriterCellTypeTest#replacesNulCharacterWithQuestionMark`（代表。全 7 件は下表） | — | XML 1.0 で不正な文字は `?` へ置換（`issues.md` XLS-17）。変化が起きるのは直列化区間であることを、生バイトを読む 1 件が示す |

#### D3-06 を担保する 4 メソッド

| 担保テストメソッド | 何を固定しているか |
|---|---|
| `XlsFormatWriterCellTypeTest#writesLineFeedStringAsStringCell` | `LF` を含む値が文字列セルとしてそのまま往復する |
| `XlsFormatWriterCellTypeTest#replacesCrLfWithSingleLineFeedInStringCell` | `CRLF` が `LF` 1 文字になる |
| `XlsFormatWriterCellTypeTest#replacesLoneCarriageReturnWithLineFeedInStringCell` | 単独の `CR` が `LF` になる |
| `XlsFormatWriterCellTypeTest#keepsCarriageReturnRawInSharedStringsXml` | ファイルには `CR` が生のまま焼き込まれている（＝変化は読み戻し区間で起きる） |

#### D3-08 を担保する 7 メソッド

| 担保テストメソッド | 何を固定しているか |
|---|---|
| `XlsFormatWriterCellTypeTest#replacesNulCharacterWithQuestionMark` | `NUL`（U+0000）が `?` へ置換される |
| `XlsFormatWriterCellTypeTest#replacesBellCharacterWithQuestionMark` | `BEL`（U+0007）が `?` へ置換される |
| `XlsFormatWriterCellTypeTest#replacesVerticalTabCharacterWithQuestionMark` | 垂直タブ（U+000B）が `?` へ置換される |
| `XlsFormatWriterCellTypeTest#replacesUnitSeparatorCharacterWithQuestionMark` | ユニットセパレータ（U+001F）が `?` へ置換される |
| `XlsFormatWriterCellTypeTest#writesTabCharacterAsIs` | タブ（U+0009）はそのまま残る |
| `XlsFormatWriterCellTypeTest#writesDeleteCharacterAsIs` | `DEL`（U+007F）はそのまま残る |
| `XlsFormatWriterCellTypeTest#burnsQuestionMarkIntoSharedStringsXmlForControlCharacter` | ファイルに `?` が焼き込まれている（＝変化は直列化区間で起きる） |

### 3.5 軸E 多重度（11 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| E-1(0件) | セクション内ブロック数 0 | ✅ | `XlsFormatWriterModelTest#writesEmptySheetWhenSectionHasNoBlocks` | — | C-04(空) と同じ入力 |
| E-1(1件) | 同 1 | ✅ | `XlsFormatWriterModelTest#writesOnlyOneBlockWhenSectionHasSingleBlock` | — | #27 で追加（`783810b`）。シート全体の行数 `getPhysicalNumberOfRows()` を `is(3)`（識別行・カラム名行・データ行 1 行）で固定する。2 ブロック目が書き出されれば 6 になって落ちる（ブロック間の空行は行を作らないため 3+3）。実測で確かめた —— 一時的に 2 ブロック目を足すと `Expected: is <3> but: was <6>` で落ちる。「次の行が `null`」を根拠にする以前の判定は誤りだった —— `XlsFormatWriter#writeSection` はブロック間の空行を行を生成せずに `rowNum` を進めるため、2 ブロック目があっても行 2・行 3 は `null` のままで、2 ブロック目の識別行は行 4 に来る（これも同じ一時変更で実測した）。そのため `XlsFormatWriterModelTest#writesTableWithoutDataRowsWhenRowsAreEmpty` は E-2(0件) の担保としてのみ有効である（データ行は空行を挟まないため行 2 が `null` なら 0 行が決まる） |
| E-1(複数) | 同 複数 | ✅ | `XlsFormatWriterTest#insertsBlankRowBetweenBlocks` | — | 2 ブロックの間に空行 1 行が入る位置を固定する |
| E-2(0件) | ブロック内行数 0 | ✅ | `XlsFormatWriterModelTest#writesTableWithoutDataRowsWhenRowsAreEmpty`（テーブル経路）／ `#writesRecordWithoutDataRowsWhenRecordRowsAreEmpty`（ファイル経路の値行） | — | 順に C-09(空) ／ C-18(空) と同じ入力 |
| E-2(1件) | 同 1 | ✅ | `XlsFormatWriterTest#insertsBlankRowBetweenBlocks` | — | データ行 1 行のブロックを次のブロックの開始行で固定する —— 同メソッドの Then は `cell(sheet,0,0)` ／ `sheet.getRow(3)` が `null` ／ `cell(sheet,4,0)` の 3 点だけを見る（行 1・行 2 は見ていない）。ブロック間の空行は 1 行なので、2 ブロック目の識別行が行 4 に来ることは 1 ブロック目が行 0〜2 の 3 行であることを意味し、識別行・カラム名行を除いたデータ行は 1 行に決まる。データ行が 0 行なら 2 ブロック目は行 3 に、2 行なら行 5 に来て落ちる。`XlsFormatWriterTest#metaRowContainsOnlyValueCells` は担保ではない —— 同メソッドの Then は `sheet.getRow(0).getLastCellNum()` が 1 であることだけを見ており、行 0 は識別行なのでデータ行の件数も内容もアサートしていない |
| E-2(複数) | 同 複数 | ✅ | `XlsFormatWriterTest#writesTableBlock` | — | 2 行 |
| E-3(0件) | ファイル内レコードレイアウト数 0 | ✅ | `XlsFormatWriterModelTest#writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty` | — | ファイル系だけで到達する。 電文系は C-15(空) と同じ理由で到達不能 |
| E-3(1件) | 同 1 | ✅ | `XlsFormatWriterModelTest#writesRecordWithoutDataRowsWhenRecordRowsAreEmpty` | — | ディレクティブ 0 件のファイルブロックなので、レコードレイアウト 1 件が行 1〜3（名前行・型行・長さ行）を占め、同メソッドが行 4 を `null` でアサートする。レコードレイアウトどうしは空行を挟まない（`XlsFormatWriterTest#writesMultipleRecordLayouts` が 1 件目の名前行を行 1、2 件目の名前行を行 5 でアサートしており、1 件目が占める 4 行＝名前・型・長さ・データ各 1 行の直後に 2 件目が始まる）ため、2 件目があれば行 4 から始まって落ちる。`XlsFormatWriterTest#writesFixedFileBlock` は担保ではない —— 同メソッドの Then は行 0〜5 の内容を見るだけで行 6 にも総行数にも触れず、2 件目のレコードレイアウトが余計に書き出されても通る |
| E-3(複数) | 同 複数 | ✅ | `XlsFormatWriterTest#writesMultipleRecordLayouts` | `XlsFormatWriterTest#roundTripsMultipleRecordLayouts` | 断片 2 件の版面の開始行を固定する |
| E-4(1件) | コンテナ内セクション数 1 | ✅ | `XlsFormatWriterInvalidOutputTest#writesSheetNameOfExcelLimitLengthAsIs` | — | `getNumberOfSheets()` が 1 であることをアサートする（同メソッドの主眼は F3-04）。セクション 1 件 → シート 1 枚を固定しているのは `src/test` でここだけである（下の導出コマンド） |
| E-4(複数) | 同 複数 | ✅ | `XlsFormatWriterTest#writesWorkbookFileWithSheetPerSection` | — | 2 セクション → 2 シート |

E-1(0件)／E-1(1件) の担保の唯一性（シート全体の行数を見ている箇所を `src/test` 全体から引く）:

```sh
cd "$(git rev-parse --show-toplevel)"
grep -rn "getPhysicalNumberOfRows\|getLastRowNum" src/test --include=*.java | sed 's/:[0-9]*:/: /'
```

出力は 3 行で、すべて `XlsFormatWriterModelTest` である。アサートは 2 行——`is(0)`（＝ E-1(0件)）と
`is(3)`（＝ E-1(1件)。#27 で追加）。残る 1 行は追加したテストの Javadoc である。
シート全体の行数を見ている箇所は、この 2 つのアサート以外に無い。

E-4(1件) の唯一性の導出（`getNumberOfSheets()` を見ている箇所を `src/test` 全体から引く）:

```sh
cd "$(git rev-parse --show-toplevel)"
grep -rn "getNumberOfSheets()" src/test --include=*.java | sed 's/:[0-9]*:/: /'
```

出力は 2 行で、`is(1)` を見ているのは `XlsFormatWriterInvalidOutputTest` の 1 行だけである
（もう 1 行は `XlsFormatWriterModelTest` の `is(0)` ＝ §3.3 の C-02(空)）。

### 3.6 軸F 異常系（4 ケース）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| F3-01 | 出力先不在 | ✅ | `XlsFormatWriterInvalidOutputTest#createsMissingOutputDirectoriesAndWritesWorkbook` | — | 例外にならず多階層の出力先が作られる。対になる「親に通常ファイルが居座りディレクトリを作れない」ケースは `XlsFormatWriterTest#wrapsIoFailure`（`UncheckedIOException`） |
| F3-02 | `overwrite=false` 衝突 | — | — | — | 対象外（衝突検査は上位層）。 `XlsFormatWriter` は `overwrite` を保持せず、検査は `TestDataConverter#checkOverwrite` で完結する。辺③に書いても再現できないためここでは空欄とする。ただし上位層にも `.xlsx` 側の担保が無い —— この穴は §5.4 に開示する |
| F3-03 | 書き込み権限なし | ✅ | `XlsFormatWriterInvalidOutputTest#wrapsAccessDeniedExceptionWhenOutputDirectoryIsNotWritable` | — | `UncheckedIOException` ＋ 原因 `AccessDeniedException`。ファイルは作られない。POSIX 権限が効かない環境では `Assume` でスキップする |
| F3-04 | シート名が Excel 制約違反 | ✅ | `XlsFormatWriterInvalidOutputTest#rejectsSheetNameContainingSlash`（代表。全 14 件は下表） | — | 31 文字ちょうどはそのまま、32 文字以上は `IllegalArgumentException`（`issues.md` XLS-16 の修正後）。`null` は辺③の担保ではない —— `TestDataSection` ／ `TestDataContainer` が生成時に拒否するため（`TestDataContainerTest#名前がnullの読み込み単位は生成できない`。`issues.md` XLS-33）。アポストロフィ（先頭／末尾）は #22 のスコープ外で未担保 |

#### F3-04 を担保する 14 メソッド

| 観点 | 担保テストメソッド |
|---|---|
| 禁止文字 7 種 | `XlsFormatWriterInvalidOutputTest#rejectsSheetNameContainingSlash` ／ `#rejectsSheetNameContainingBackslash` ／ `#rejectsSheetNameContainingQuestionMark` ／ `#rejectsSheetNameContainingAsterisk` ／ `#rejectsSheetNameContainingOpeningBracket` ／ `#rejectsSheetNameContainingClosingBracket` ／ `#rejectsSheetNameContainingColon` |
| 空のシート名 | `XlsFormatWriterInvalidOutputTest#rejectsEmptySheetName` |
| 文字数の境界（31 は通す／32 は拒否） | `XlsFormatWriterInvalidOutputTest#writesSheetNameOfExcelLimitLengthAsIs` ／ `#rejectsSheetNameLongerThanExcelLimit` |
| 切り詰めで禁止文字が消える形 | `XlsFormatWriterInvalidOutputTest#rejectsSheetNameWhoseForbiddenCharacterWouldBeRemovedByTruncation` ／ `#rejectsSheetNameWhoseForbiddenCharacterIsAtTheLastPosition` |
| シート名の重複 | `XlsFormatWriterInvalidOutputTest#failsWhenSameSheetNameOfLimitLengthIsUsedTwice` ／ `#failsWhenSheetNamesDifferOnlyInCase` |

**軸F の外に残る空欄**: `XlsFormatWriter#write` の `parent == null` 分岐は、`basePath` が空文字のときだけ通る
（`Paths.get(basePath, name)` が親を持たない相対パスになるのはこの場合だけ。`src/main` にその旨のコメントがある）。
`src/test` にこの分岐を通す呼び出しは無い —— 辺③④のライタの `write(container, basePath)` を呼ぶ箇所は
`src/test` に 32 か所あり、`basePath` に渡している式は 9 種類で、いずれも `TemporaryFolder` 由来のパスである。

```sh
cd "$(git rev-parse --show-toplevel)"
# write(container, basePath) の第 2 引数（basePath）だけを取り出して並べる
perl -0777 -ne 'while (/(?:FormatWriter\(\)|writer)\.write\(/g) {
      my $i = pos; my $d = 1; my @a = ("");
      while ($d > 0 && $i < length) { my $c = substr($_, $i, 1);
        $d++ if $c eq "("; $d-- if $c eq ")"; last if $d == 0;
        if ($c eq "," && $d == 1) { push @a, "" } else { $a[-1] .= $c } $i++ }
      my $b = $a[-1]; $b =~ s/\s+/ /g; $b =~ s/^ | $//g; print "$b\n" }' \
    $(grep -rl 'FormatWriter()\.write(\|writer\.write(' src/test --include=*.java) \
  | sort | uniq -c | sort -rn
```

出力は 9 行・合計 32 件で、内訳は `folder.getRoot().getAbsolutePath()` 17 ／ `baseDir.toString()` 3 ／
`base` 3（`base` は `folder.getRoot().getAbsolutePath()` を受けたローカル変数）／
`readOnly.getAbsolutePath()` 2 ／ `out.getAbsolutePath()` 2 ／ `missing.getAbsolutePath()` 2 ／
`in.toString()` 1 ／ `file.getAbsolutePath()` 1 ／ `blocker.getAbsolutePath()` 1 である。
**空文字リテラルは 1 件も無い。**

`inventory.md` §0.7 の軸F 4 要素のいずれにも当たらないため本表には計上しないが、
F3-01 の隣接領域であるため開示する（`issues.md` COV-08。`coverage-report.md` §3.2）。
同じく `XlsFormatWriter#layout` の未知ブロック向け `throw` と `XlsFormatWriter#isMarkerColumn` の
`null` ガードも未到達だが、どちらも Java イディオムとしての安全網であり軸要素ではない。

---

## 4. 辺④ 中間モデル→YAML（`YamlFormatWriter`）

担保の主体は `YamlFormatWriterTest`（`serialize` の出力全文を完全一致でアサートするもの中心）・
`YamlFormatWriterModelTest` ／ `YamlFormatWriterScalarTest`（`serialize` と実ファイル往復）・
`YamlFormatWriterInvalidOutputTest`（`write` の異常系）の 4 クラスである。

**辺④でも `list_maps` は独立した経路ではない。** `YamlFormatWriter#emitListMap` と `#emitTable` は
どちらも同じ引数で `emitMapRows` を呼ぶため、値の記法は同一のコードで担保されている。
残る差はキー側の literal（`table:` ／ `id:`）だけである。出典:

```sh
cd "$(git rev-parse --show-toplevel)"
grep -c "emitMapRows(sb, entry, block.getColumnNames(), block.getRows());" \
  src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java
```

出力は 2（`emitTable` と `emitListMap` の 2 箇所）。

### 4.1 軸A データタイプ（14 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| A-01 | `DEFAULT` | — | — | — | 到達不能。`TestDataBlock` が `DEFAULT` のブロックを生成時に拒否するため入力を組めない（`issues.md` XLS-20。`steering.md` #25.5 §1-G）。根拠テスト `TestDataBlockTest#データタイプDEFAULTのブロックは生成できない`。#18 以来 ✅ の根拠だった辺④の例外テストは入力を組めなくなったため削除済みで、HEAD に該当メソッドは無い。 副作用として `YamlFormatWriter#sectionKey` の `default` 分岐が到達不能になった（`coverage-report.md` §3.3） |
| A-02 | `SETUP_TABLE_DATA` | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | `RoundTripTest#yaml_setupTable_isPreserved` ／ `YamlFormatWriterTest#roundTrip_table_isPreservedThroughRealReader` | `setup_tables:` キーへ写ることを出力全文の完全一致で固定する |
| A-03 | `EXPECTED_TABLE_DATA` | ✅ | `YamlFormatWriterTest#serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId` | `RoundTripTest#yaml_expectedTable_withGroupId_isPreserved` | `expected_tables:` キー |
| A-04 | `EXPECTED_COMPLETED` | ✅ | `YamlFormatWriterTest#serializeTable_completed_usesExpectedCompleteTablesKey` | `RoundTripTest#yaml_expectedCompleteTable_isPreserved` | `expected_complete_tables:` キー |
| A-05 | `LIST_MAP` | ✅ | `YamlFormatWriterTest#serializeListMap_usesIdKeyAndColumnOrder` | `RoundTripTest#yaml_listMap_isPreserved` | `list_maps:` キー |
| A-06 | `SETUP_FIXED` | ❌ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords`（`SETUP_VARIABLE` と区別しない） | `RoundTripTest#yaml_setupFixed_isPreserved` ／ `YamlFormatWriterTest#roundTrip_fixedFile_isPreservedThroughRealReader` | **`SETUP_FIXED` を `SETUP_VARIABLE` から区別する出力が辺④に無い。** 固定できているのは「SETUP 系 → `setup_files:`」という 2 対 1 の写像までである。§4.1 末尾を参照 |
| A-07 | `EXPECTED_FIXED` | ❌ | `YamlFormatWriterModelTest#writesSetupVariableAndExpectedFixedUnderTheirSectionKeysInEncounterOrder` ／ `#restoresExpectedFixedDataTypeThroughRealReader`（どちらも `EXPECTED_VARIABLE` と区別しない） | `RoundTripTest#yaml_expectedFixed_isPreserved` | **`EXPECTED_FIXED` を `EXPECTED_VARIABLE` から区別する出力が辺④に無い。** 読み戻し側も同じで、`YamlFormatReader#fileDataType` がセクションキーと `type:` から `DataType` を組み直すため、入力の `DataType` を取り違えても `back.getDataType()` は一致してしまう。§4.1 末尾を参照 |
| A-08 | `SETUP_VARIABLE` | ❌ | `YamlFormatWriterModelTest#writesSetupVariableAndExpectedFixedUnderTheirSectionKeysInEncounterOrder` ／ `#restoresSetupVariableDataTypeThroughRealReader`（どちらも `SETUP_FIXED` と区別しない） | `RoundTripTest#yaml_setupVariable_isPreserved` | **`SETUP_VARIABLE` を `SETUP_FIXED` から区別する出力が辺④に無い。** A-07 と同じく読み戻し側も区別しない。§4.1 末尾を参照 |
| A-09 | `EXPECTED_VARIABLE` | ❌ | `YamlFormatWriterTest#serializeFile_variableOmitsDirectivesAndRecordTypeAndLength`（`EXPECTED_FIXED` と区別しない） | `RoundTripTest#yaml_expectedVariable_isPreserved` | **`EXPECTED_VARIABLE` を `EXPECTED_FIXED` から区別する出力が辺④に無い。** 従来この行が区別の根拠に挙げていた `type: "variable"` は `DataType` ではなく `FileDataBlock.fileType` から出る。§4.1 末尾を参照 |
| A-10 | `MESSAGE` | ✅ | `YamlFormatWriterTest#serializeMessage_withDirectivesAndFwHeader` | `RoundTripTest#yaml_message_isPreserved` ／ `YamlFormatWriterTest#roundTrip_message_preservesFwHeaderAndBody` | `messages:` キー |
| A-11 | `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `YamlFormatWriterTest#serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField` | `RoundTripTest#yaml_expectedRequestHeaderMessages_isPreserved` ／ `YamlFormatWriterTest#roundTrip_sendSync_preservesGroupIdAndNoField` | 単独ブロックの出力全文を完全一致でアサートする |
| A-12 | `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `YamlFormatWriterModelTest#writesExpectedRequestBodyMessagesUnderItsOwnSectionKey` | `RoundTripTest#yaml_expectedRequestBodyMessages_isPreserved` | #18〜#25 当初版は ✅ と誤判定していた（`YamlFormatWriterTest#serializeSendSync_allFourSectionKeys` は 4 キーが「どこかに現れる」ことしか見ていない）。#25 レビュー対応で追加（`inventory.md` §4.1-2） |
| A-13 | `RESPONSE_HEADER_MESSAGES` | ✅ | `YamlFormatWriterModelTest#writesResponseHeaderMessagesUnderItsOwnSectionKey` | `RoundTripTest#yaml_responseHeaderMessages_isPreserved` | 同上 |
| A-14 | `RESPONSE_BODY_MESSAGES` | ✅ | `YamlFormatWriterModelTest#writesResponseBodyMessagesUnderItsOwnSectionKey` | `RoundTripTest#yaml_responseBodyMessages_isPreserved` | 同上 |

**ファイル系 4 種（A-06 ／ A-07 ／ A-08 ／ A-09）の `DataType` は、辺④の出力に完全には現れない。**
`YamlFormatWriter#sectionKey` は `SETUP_FIXED` と `SETUP_VARIABLE` をどちらも `setup_files`、
`EXPECTED_FIXED` と `EXPECTED_VARIABLE` をどちらも `expected_files` へ写す。固定長／可変長を分ける
`type:` は `YamlFormatWriter#emitFile` が `block.getFileType()` から出しており、`DataType` を見ていない。
`FileDataBlock` は `DataType` の FIXED ／ VARIABLE の別と `fileType` が一致することを検査しないため
（コンストラクタが課すのは `DataType` が 4 種のいずれかであることと `fileType` が `null` でないことだけ）、
両者を食い違わせたブロックを合法に作れてしまう。

実測（`serialize` の出力を比べる。出力は 4 行とも `true`）:

```
fileType=FIXED    SETUP_FIXED    vs SETUP_VARIABLE    identical? true
fileType=FIXED    EXPECTED_FIXED vs EXPECTED_VARIABLE identical? true
fileType=VARIABLE SETUP_FIXED    vs SETUP_VARIABLE    identical? true
fileType=VARIABLE EXPECTED_FIXED vs EXPECTED_VARIABLE identical? true
```

**この 4 件はテストを足しても埋まらない。** 埋めるには辺④の外を動かす必要がある —— 中間モデル側に
「`DataType` の FIXED ／ VARIABLE の別と `fileType` が一致すること」の不変条件を置いて `type:` を
`DataType` の関数にするか、この 4 行の主張を 2 対 1 の写像（SETUP 系 → `setup_files` ／
EXPECTED 系 → `expected_files`）へ書き改めるかである。どちらも #27 の範囲を超えるため、
ここでは ❌ として開示するにとどめる。

`inventory.md` §4.1-2 が挙げる変異実測（`sectionKey` の分岐を入れ替えるとこれらのテストが落ちる）は
事実だが、それは写像そのものの変異であって入力 `DataType` の差し替えではない。§0.2 の軸A の判定基準
（その `DataType` に依存する出力をアサートしているか）で見ると、この 4 件は別の型に差し替えても通る。

### 4.2 軸B ブロック実装（4 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| B-1 | `TableDataBlock` | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | `YamlFormatWriterTest#roundTrip_table_isPreservedThroughRealReader` | 出力全文の完全一致 |
| B-2 | `ListMapBlock` | ✅ | `YamlFormatWriterTest#serializeListMap_usesIdKeyAndColumnOrder` | `RoundTripTest#yaml_listMap_isPreserved` | 値の記法は B-1 と同じ `emitMapRows` を通る（本節冒頭）。差は `id:` ／ `table:` のキーだけ |
| B-3 | `FileDataBlock` | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | `YamlFormatWriterTest#roundTrip_fixedFile_isPreservedThroughRealReader` | — |
| B-4 | `MessageDataBlock` | ✅ | `YamlFormatWriterTest#serializeMessage_withDirectivesAndFwHeader` | `YamlFormatWriterTest#roundTrip_message_preservesFwHeaderAndBody` | — |

### 4.3 軸C 中間モデル全フィールド（36 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| C-01 | `TestDataContainer.name` | — | — | — | 辺④はコンテナの名前を読まない。 `YamlFormatWriter#write` は `container.getSections()` を走査して `section.getName() + ".yaml"` を組むだけで、`container.getName()` を 1 度も参照しない（導出コマンドは本表の下）。出力先ディレクトリ名を決めるのは上位層の `ConverterPathResolver#outputBaseForYaml` であり辺④の担当ではない |
| C-02(非空) | `TestDataContainer.sections` 非空 | ✅ | `YamlFormatWriterModelTest#writesOneYamlFilePerSectionWhenContainerHasMultipleSections` | — | 3 セクション → 3 ファイル |
| C-02(空) | 同 空 | ✅ | `YamlFormatWriterModelTest#writesNothingWhenContainerHasNoSections` | — | 例外にならず、ファイルも出力先ディレクトリも作られない（辺③がシート 0 枚のブックを書くのとは非対称） |
| C-03 | `TestDataSection.name` | ✅ | `YamlFormatWriterModelTest#writesOneYamlFilePerSectionWhenContainerHasMultipleSections` | — | 読み込み単位の名前が `<名前>.yaml` になる。Given がコンテナ名と読み込み単位名を違えているため、ファイル名の由来が決まる —— コンテナ名 `td` に対し読み込み単位名は `zebra` ／ `alpha` ／ `mango` で、書かれた 3 ファイルを読み込み単位名で引き当てる。`YamlFormatWriterTest#write_writesEachSectionAsYamlFileWithSerializedContent` は Given のコンテナ名と読み込み単位名が両方 `"td"` のため由来を判別できない（同メソッドが固定するのは中身が `serialize` の結果と一致すること） |
| C-04(非空) | `TestDataSection.blocks` 非空 | ✅ | `YamlFormatWriterTest#serialize_multipleSections_separatedByBlankLineInEncounterOrder` | — | — |
| C-04(空) | 同 空 | ✅ | `YamlFormatWriterTest#serialize_emptySection_isEmptyString` | — | 空文字列になる |
| C-05 | `TestDataBlock.dataType` | ✅ | `YamlFormatWriterTest#serializeTable_completed_usesExpectedCompleteTablesKey` | — | セクションキーがデータタイプから決まる |
| C-06(値あり) | `TestDataBlock.groupId` 値あり | ✅ | `YamlFormatWriterTest#serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId` | `RoundTripTest#yaml_expectedTable_withGroupId_isPreserved` | `[case01]` → `group_id: "case01"`（角括弧が外れる）。角括弧で囲まれていない値の防御的経路は `YamlFormatWriterTest#serialize_unbracketedGroupId_isUsedAsRawValue` |
| C-06(省略) | 同 省略（`""`） | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | `RoundTripTest#yaml_setupTable_isPreserved` | `group_id:` キーごと出ない |
| C-07 | `TestDataBlock.identifier` | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | — | `table: "USERS"` |
| C-08(非空) | `ColumnRowDataBlock.columnNames` 非空 | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | — | カラム名が `rows:` の各要素のキーになる |
| C-08(空) | 同 空 | ✅ | `YamlFormatWriterTest#serialize_emptyColumnsRow_emitsEmptyFlowMap` | — | セルを持たない行が `- {}` になる。カラム名 0 件かつ行 0 件のときはカラム名を書く場所が無く、往復するとカラム名が復元されない（`issues.md` XLS-27 の申し送り。0 件テーブルに残る担保の穴は §7 の ①〜⑧） |
| C-09(非空) | `ColumnRowDataBlock.rows` 非空 | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | — | — |
| C-09(空) | 同 空 | ✅ | `YamlFormatWriterTest#serialize_emptyRows_emitsEmptyFlowList` | — | `rows: []` |
| C-10 | `FileDataBlock.fileType`（FIXED ／ VARIABLE の双方） | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords`（`type: "fixed"`）／ `#serializeFile_variableOmitsDirectivesAndRecordTypeAndLength`（`type: "variable"`） | `YamlFormatWriterTest#roundTrip_fixedFile_isPreservedThroughRealReader` | `null` は `FileDataBlock` が生成時に拒否する（`issues.md` XLS-29） |
| C-11(非空) | `FileDataBlock.directives` 非空 | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | — | `directives:` ブロックが記述順に出る |
| C-11(空) | 同 空 | ✅ | `YamlFormatWriterTest#serializeFile_variableOmitsDirectivesAndRecordTypeAndLength` | — | `directives:` キーごと出ない |
| C-12(非空) | `FileDataBlock.records` 非空 | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | — | 断片 2 件 |
| C-12(空) | 同 空 | ✅ | `YamlFormatWriterModelTest#writesEmptyRecordsListForFileBlockWithoutRecords` | — | `records: []` が出る（`issues.md` YML-12 1形目 の修正後。修正前はキーごと出ず読み戻せなかった）。読み戻せることは `YamlFormatWriterModelTest#readsBackFileBlockWithEmptyRecords` が担保する |
| C-13(非空) | `MessageDataBlock.directives` 非空 | ✅ | `YamlFormatWriterTest#serializeMessage_withDirectivesAndFwHeader` | — | — |
| C-13(空) | 同 空 | ✅ | `YamlFormatWriterTest#serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField` | — | `directives:` キーごと出ない |
| C-14(非空) | `MessageDataBlock.fwHeaderFields` 非空 | ✅ | `YamlFormatWriterTest#serializeMessage_withDirectivesAndFwHeader` | `YamlFormatWriterTest#roundTrip_message_preservesFwHeaderAndBody` | `fw_header:` が記述順に出る |
| C-14(空) | 同 空 | ✅ | `YamlFormatWriterTest#serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField` | `YamlFormatWriterTest#roundTrip_sendSync_preservesGroupIdAndNoField` | `fw_header:` キーごと出ない |
| C-15(非空) | `MessageDataBlock.records` 非空 | ✅ | `YamlFormatWriterTest#serializeMessage_withDirectivesAndFwHeader` | — | — |
| C-15(空) | 同 空 | — | — | — | 到達不能。`MessageDataBlock` が本文レコード 0 件を生成時に拒否するため入力を組めない（`issues.md` YML-12 2形目。`steering.md` #25.5 §6-J-2）。根拠テスト `MessageDataBlockTest#本文レコードが0件の電文ブロックは生成できない`。#18 が辺④に置いていた直列化テスト（出力 YAML の全文を固定するもの）と、その後 #25.5 が辺④に置いた番人テスト 2 件はいずれも削除済みで、HEAD に該当メソッドは無い（境界＝ファイルブロックの `records` 0 件は合法、は C-12(空) が担保する） |
| C-16(値あり) | `RecordLayout.recordType` 値あり | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | — | `record_type: "head"` ／ `"data"` |
| C-16(省略) | 同 省略（`null`） | ✅ | `YamlFormatWriterTest#serializeFile_variableOmitsDirectivesAndRecordTypeAndLength` ／ `#serialize_recordWithEmptyRows_emitsEmptyFlowList` | — | `record_type:` キーごと出ない |
| C-17(非空) | `RecordLayout.fields` 非空 | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | — | — |
| C-17(空) | 同 空 | — | — | — | 到達不能。`RecordLayout` がフィールド 0 件を生成時に拒否するため入力を組めない（`issues.md` YML-12 3形目 ＝ 辺③の XLS-22）。根拠テスト `RecordLayoutTest#フィールドを1件も持たないレコードは生成できない`。辺④に置いていた番人テスト 2 件は空振りになったため削除済み |
| C-18(非空) | `RecordLayout.rows` 非空 | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | — | — |
| C-18(空) | 同 空 | ✅ | `YamlFormatWriterTest#serialize_recordWithEmptyRows_emitsEmptyFlowList` | — | `rows: []` |
| C-19 | `FieldDef.name` | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | — | `{name: "f1", ...}`。`null` は `FieldDef` が拒否する（`FieldDefTest#名称がnullのフィールド定義は生成できない`。`issues.md` XLS-31） |
| C-20(値あり) | `FieldDef.type` 値あり | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` ／ `#serialize_fieldWithEmptyType_emitsEmptyType` | — | 後者は境界（空文字は弾かれず `type: ""` が出る）を固定する |
| C-20(省略) | 同 省略（`null`） | — | — | — | 到達不能。`FieldDef` が `type` ＝ `null` を生成時に拒否するため入力を組めない（`issues.md` YML-12 4形目 ／ XLS-31）。根拠テスト `FieldDefTest#データ型がnullのフィールド定義は生成できない`。#18 が担保としていた「`type:` キーを省いて書く」テストと、その後 #25.5 が辺④に置いた番人テスト 2 件はいずれも削除済みで、HEAD に該当メソッドは無い |
| C-21(値あり) | `FieldDef.length` 値あり | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | — | `{..., length: "5"}` |
| C-21(省略) | 同 省略（`null`） | ✅ | `YamlFormatWriterTest#serializeFile_variableOmitsDirectivesAndRecordTypeAndLength` | — | `length:` キーごと出ない。到達できるのは可変長ファイルだけ（`issues.md` XLS-30） |

#### C-01 が空欄である導出

```sh
cd "$(git rev-parse --show-toplevel)"
Y=src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java
X=src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java
grep -c "container.getName()" "$Y"   # 辺④
grep -c "getName()"           "$Y"   # 辺④（内訳の確認用）
grep -c "container.getName()" "$X"   # 辺③（対照）
```

出力は順に 0 ／ 3 ／ 1。辺④の 3 件の内訳は `section.getName()`（出力ファイル名）・
`block.getClass().getName()`（未知ブロックの例外メッセージ）・`field.getName()`（フィールド名の直列化）で、
**コンテナの名前を読む箇所は 1 つも無い**。辺③は対照的に `XlsFormatWriter#write` が
`container.getName() + ".xlsx"` を組む（§3.3 の C-01）。

### 4.4 軸D 値の表現（YAML 表現 9 ケース）

「記法」は出力 YAML の記法そのもの、「往復」は書いて読み戻したときに元の文字列へ戻ることを指す。

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| D4-01 | `"100"` | ✅ | `YamlFormatWriterScalarTest#writesNumberLookingStringAsDoubleQuotedScalar`（記法）／ `#restoresNumberLookingStringThroughRealReader`（往復） | `YamlFormatWriterTest#roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader` | `V: "100"` |
| D4-02 | `"true"` | ✅ | `YamlFormatWriterScalarTest#writesBooleanLookingStringAsDoubleQuotedScalar` ／ `#restoresBooleanLookingStringThroughRealReader` | — | レコード断片経路と `emitMap` 経路にも埋め込んである（`YamlFormatWriterModelTest#quotesBooleanAndDateLookingValuesInFwHeader`） |
| D4-03 | `"null"` | ✅ | `YamlFormatWriterTest#serialize_distinguishesNullFromNullString` | `YamlFormatWriterTest#roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader` | `V: "null"` |
| D4-04 | `null`（Java `null`） | ✅ | `YamlFormatWriterTest#serialize_distinguishesNullFromNullString` ／ `#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | `RoundTripTest#nullCell_xlsConvertsToLiteralString_yamlPreservesNull` | `V: null`（クォート無し）。Java `null` へ戻る |
| D4-05 | `""` | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | `YamlFormatWriterTest#roundTrip_table_isPreservedThroughRealReader` | `NAME: ""`。`null` と区別される |
| D4-06 | `"007"` | ✅ | `YamlFormatWriterScalarTest#writesLeadingZeroNumberAsDoubleQuotedScalar` ／ `#restoresLeadingZeroNumberThroughRealReader` | — | — |
| D4-07 | 改行含む | ✅ | `YamlFormatWriterScalarTest#writesNewlineContainingStringAsEscapedSingleLineScalar`（代表。全 5 件は下表） | — | ブロックスカラーにはならず 1 行の `"l1\nl2"`。80 桁を超えると行末 `\` で折り返す（折り返しても往復する） |
| D4-08 | `"2026-08-07"` | ✅ | `YamlFormatWriterScalarTest#writesDateLookingStringAsDoubleQuotedScalar` ／ `#restoresDateLookingStringThroughRealReader` | — | 日付にならない。D4-02 と同じく別経路にも埋め込んである |
| D4-09 | コロン・ハイフン・`#` 含む | ✅ | `YamlFormatWriterScalarTest#writesColonHyphenAndHashContainingStringAsDoubleQuotedScalar` ／ `#restoresColonHyphenAndHashContainingStringThroughRealReader` | — | `#` 以降も残る |

#### D4-07 を担保する 5 メソッド

| 担保テストメソッド | 何を固定しているか |
|---|---|
| `YamlFormatWriterScalarTest#writesNewlineContainingStringAsEscapedSingleLineScalar` | 改行を含む値が 1 行の `"l1\nl2"` になる（ブロックスカラーにならない） |
| `YamlFormatWriterScalarTest#foldsLongEscapedValueWithBackslashContinuation` | 80 桁を超えると行末 `\` で折り返す |
| `YamlFormatWriterScalarTest#restoresNewlineContainingStringThroughRealReader` | 実 `YamlFormatReader` で読み戻すと元の文字列へ戻る |
| `YamlFormatWriterScalarTest#restoresFoldedLongEscapedValueThroughRealReader` | 折り返した値も読み戻すと元へ戻る |
| `YamlFormatWriterTest#serialize_escapesQuotesBackslashAndControlChars` | 引用符・バックスラッシュ・制御文字のエスケープを改行と同じ経路で固定する |

**軸D の外に残る空欄**: 9 ケースのうち 7 ケースは `setup_tables` の `rows` の 1 経路でしか
固定していない（別経路へ埋め込んだのは D4-02 と D4-08 の 2 ケースだけ）。
**残る 7 ケースがレコード断片経路でも同じ記法になることはプローブで確認したがテストにしていない**
（`inventory.md` §4.1-2 の開示）。キー側のクォート判定は
`YamlFormatWriterModelTest#quotesDirectiveKeyContainingAnyYamlSpecialOrControlCharacter` が
YAML の特殊文字と制御文字を 1 文字ずつ `directives` のキーに置いて固定する（軸要素ではない）。

### 4.5 軸E 多重度（11 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| E-1(0件) | セクション内ブロック数 0 | ✅ | `YamlFormatWriterTest#serialize_emptySection_isEmptyString` | — | C-04(空) と同じ入力 |
| E-1(1件) | 同 1 | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | — | 出力全文の完全一致なので、2 ブロック目が無いことも固定される |
| E-1(複数) | 同 複数 | ✅ | `YamlFormatWriterTest#serialize_multipleSections_separatedByBlankLineInEncounterOrder` ／ `#serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId` | — | 前者は異なるデータタイプ 2 件、後者は同じデータタイプ 2 件 |
| E-2(0件) | ブロック内行数 0 | ✅ | `YamlFormatWriterTest#serialize_emptyRows_emitsEmptyFlowList` | — | C-09(空) と同じ入力 |
| E-2(1件) | 同 1 | ✅ | `YamlFormatWriterTest#serialize_emptyColumnsRow_emitsEmptyFlowMap` ／ `#serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId` | — | どちらも出力全文の完全一致で、`rows:` の下に要素が 1 つだけ出ることが決まる（前者は `- {}` 1 件、後者は 2 ブロックがそれぞれ 1 行）。`YamlFormatWriterTest#serializeTable_completed_usesExpectedCompleteTablesKey` は担保ではない —— 同メソッドの Then は `startsWith("expected_complete_tables:\n")` の 1 本だけで `rows` を一切アサートしていない（同メソッドは A-04 ／ C-05 の担保である） |
| E-2(複数) | 同 複数 | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | — | 2 行 |
| E-3(0件) | ファイル内レコードレイアウト数 0 | ✅ | `YamlFormatWriterModelTest#writesEmptyRecordsListForFileBlockWithoutRecords` | — | ファイル系だけで到達する。 電文系は C-15(空) と同じ理由で到達不能 |
| E-3(1件) | 同 1 | ✅ | `YamlFormatWriterTest#serializeMessage_withDirectivesAndFwHeader` | — | 出力全文の完全一致なので、`records:` の下に断片が 1 つだけ出ることも固定される |
| E-3(複数) | 同 複数 | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | `YamlFormatWriterTest#roundTrip_fixedFile_isPreservedThroughRealReader` | 断片 2 件 |
| E-4(1件) | コンテナ内セクション数 1 | ✅ | `YamlFormatWriterModelTest#writesOneYamlFileWhenContainerHasSingleSection` | — | #27 で追加（`6d12021`）。出力先ディレクトリの実ファイル数 `out.list().length` を `is(1)` で固定し、中身をリテラルで突き合わせる。2 件目のセクションが書き出されれば落ちる。実測で確かめた —— 一時的に 2 件目のセクションを足すと `Expected: is <1> but: was <2>` で落ちる。コンテナ名 `book` とセクション名 `solo` を別にしてあるので、ファイル名がコンテナ名から作られるようになっても落ちる。`YamlFormatWriterTest#write_writesEachSectionAsYamlFileWithSerializedContent` を担保としていた以前の判定は誤りだった —— 同メソッドの Then は `assertTrue(out.exists())` と「中身が `writer.serialize(section)` と一致する」の 2 本で、後者は実装の出力を実装の出力と比べる自己参照であり、ファイル件数も直列化結果の正しさも固定しない。なお同メソッドはコンテナ名と読み込み単位名が同じ `"td"` のため C-03 の担保にもならない（§4.3） |
| E-4(複数) | 同 複数 | ✅ | `YamlFormatWriterModelTest#writesOneYamlFilePerSectionWhenContainerHasMultipleSections` | — | 3 セクション → 3 ファイル。書き出しの順序は固定していない（ファイルシステム上に順序が現れないため） |

E-4(1件)／E-4(複数) の担保の唯一性（出力ディレクトリのファイル件数を見ている箇所を辺④のテストから引く）:

```sh
cd "$(git rev-parse --show-toplevel)"
grep -rn 'list()\.length\|listFiles' src/test/java/nablarch/test/tool/converter/yaml/ | sed 's/:[0-9]*:/: /'
```

出力は 5 行。件数をアサートしているのは `YamlFormatWriterModelTest` の 2 行——
`assertThat("書かれたファイル数", out.list().length, is(3))`（＝ E-4(複数)）と
`is(1)`（＝ E-4(1件)。#27 で追加）。残る 3 行は追加したテストの Javadoc 1 行と
`YamlTestDataValidatorTest` のコメント 2 行である。

### 4.6 軸F 異常系（3 ケース）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| F4-01 | 出力先不在 | ✅ | `YamlFormatWriterInvalidOutputTest#createsMissingOutputDirectoriesAndWritesYaml` | — | 例外にならず多階層の出力先が作られる。対になる「親に通常ファイルが居座りディレクトリを作れない」ケースは `YamlFormatWriterTest#write_ioError_throwsUncheckedIOException` |
| F4-02 | `overwrite=false` 衝突 | — | — | — | 対象外（衝突検査は上位層）。 `YamlFormatWriter` は `overwrite` を保持せず、検査は `TestDataConverter#checkOverwrite` で完結する。辺④は上位層に担保がある（挙げるテストは §5.4）。同じ `—` でも辺③の F3-02 は上位層にも担保が無く、中身が正反対である（§5.4） |
| F4-03 | 書き込み権限なし | ✅ | `YamlFormatWriterInvalidOutputTest#wrapsAccessDeniedExceptionWhenOutputDirectoryIsNotWritable` | — | `UncheckedIOException: failed to write YAML: <パス>` ＋ 原因 `AccessDeniedException`。ファイルは作られない |

**軸F の外に残る空欄**: `YamlFormatWriter#write` の `parent == null` 分岐（`issues.md` COV-09）、
`YamlFormatWriter#emitBlock` の未知ブロック向け `throw`、`YamlFormatWriter#rawGroup` の
「`[` で始まるが `]` で終わらない」枝（`issues.md` COV-13）、`YamlFormatWriter#sectionKey` の
`default` 分岐は未到達である。
いずれも軸A〜F の要素ではない（`coverage-report.md` §3.3）。
また、**書き出した YAML がスキーマに適合するかを見る担保は無い** —— #25 で見つけた 4 つの形は
`issues.md` YML-12 に記録して固定したが、「その 4 つ以外にスキーマ違反を書き得る形が無い」ことは確かめていない。

---

## 5. 集計

### 5.1 辺 × 軸の行数

| 軸 | 辺① | 辺② | 辺③ | 辺④ |
|---|---|---|---|---|
| A データタイプ | 14 | 14 | 14 | 14 |
| B ブロック実装 | 4 | 4 | 4 | 4 |
| C 中間モデル全フィールド | 36 | 36 | 36 | 36 |
| D 値の表現 | 8 | 12 | 8 | 9 |
| E 多重度 | 11 | 11 | 11 | 11 |
| F 異常系 | 6 | 5 | 4 | 3 |
| **合計** | **79** | **82** | **77** | **77** |

辺ごとの行数は §0.6 の ①（4 辺で順に 79 ／ 82 ／ 77 ／ 77）、総計 315 は §0.6 の ⑤ で導く。
どちらも手計算ではなく本書自身を走査した値である。

### 5.2 辺 × 状態の件数

| 状態 | 辺① | 辺② | 辺③ | 辺④ | 合計 |
|---|---|---|---|---|---|
| ✅ 担保あり | 71 | 73 | 71 | 67 | 282 |
| 🔺 弱い担保のみ | 0 | 0 | 0 | 0 | 0 |
| ❌ 未担保 | 0 | 1 | 1 | 4 | 6 |
| — 空欄 | 8 | 8 | 5 | 6 | 27 |
| **合計** | **79** | **82** | **77** | **77** | **315** |

導出コマンドは §0.6 の ②（`n/a` の行を置かない理由は §0.1）。

**❌ の 6 件は水平展開で出た（#27）。** 軸E の総点検でいったん 2 件（辺③ E-1(1件)・辺④ E-4(1件)）が
✅ から ❌ へ動いたが、そちらは #27 の中でテストを 2 本足して埋めてある（`783810b` ／ `6d12021`）。
残る 6 件は水平展開——「表が主張する内容を、テスト本文が実際には主張していない」を全セルへ広げた
点検——で出たもので、軸A〜D は空欄へ振り替えず ❌ を立てて理由を書く取り決め（`steering.md` #27）に従う。

「🔺 弱い担保のみ 0 件」は状態欄だけの集計である —— 🔺 往復欄が `—` でない行は 98 行ある
（導出は §0.6 の ③）。§0.1 の「🔺 も 2 役を持つ」を参照。

**❌ が 0 件であることは「穴が無い」という意味ではない。** 本書の計上単位（§0.2）で
数えたときの話であり、軸A〜F のどの要素にも当てはまらない担保の穴は各節末尾と §5.4 と §7 に開示してある。

### 5.3 空欄（`—`）27 件の内訳

| 分類 | 件数 | 該当 |
|---|---|---|
| 構造上の到達不能（読み手側が 1 シート／1 リソース単位 API） | 4 | 辺①・辺② の C-02(空) ／ E-4(複数)（2 辺 × 2 要素） |
| 入力側が先に閉じている到達不能（閉じている機構は要素で分かれる。C-11(空) ／ C-13(空) は 2 辺とも NTF 本体の `DataFile` の注入、C-17(空) ／ C-20(省略) は辺①が本体パーサ・辺②が YAML スキーマ） | 8 | 辺①・辺② の C-11(空) ／ C-13(空) ／ C-17(空) ／ C-20(省略)（2 辺 × 4 要素） |
| 中間モデルの不変条件による到達不能（#25.5） | 12 | 4 辺の A-01（4）／4 辺の C-15(空)（4）／辺③・辺④ の C-17(空)（2）／辺③・辺④ の C-20(省略)（2） |
| その辺が読まないフィールド | 1 | 辺④ C-01（`YamlFormatWriter#write` はコンテナの名前を参照しない） |
| 対象外（衝突検査は上位層）かつ上位層が担保済み | 1 | 辺④ F4-02 |
| 対象外（衝突検査は上位層）だが上位層にも担保が無い（§5.4） | 1 | 辺③ F3-02 |
| **合計** | **27** | |

**衝突検査の 2 件を 1 つにまとめない。** どちらも辺の担当クラスの関心事ではないため状態は `—` だが、
上位層まで見たときの担保の有無が正反対である（辺④は上位層の既存テストが実際に通しており、
辺③は上位層にも担保が無い。§5.4）。

分類ごとの件数は「該当」欄の要素を数えたもので、4 ＋ 8 ＋ 12 ＋ 1 ＋ 1 ＋ 1 ＝ 27 が
下のコマンドの出力と一致する。

**辺① C-17(空) ／ C-20(省略) は 2 つの分類にまたがる**（本体パーサが先に弾き、仮に届いても中間モデルが拒否する）。
上表では先に効くほう（本体パーサ）に数えている。同じく辺② の 2 件はスキーマ側に数えている。

**辺② の C-11(空) ／ C-13(空) を閉じているのはスキーマではない。** `$defs.directives` は
`required` も `minProperties` も持たないため `directives: {}` はスキーマ検証を通る。それでも
`FileDataBlock.directives` ／ `MessageDataBlock.directives` が空にならないのは、辺①と同じく
NTF 本体の `DataFile` のコンストラクタが `file-type` を注入するからである（`issues.md` XLS-07）。
§2.3 の 2 行の理由欄はもともとこの機構を挙げている。

```sh
unzip -p "$(find ~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml \
  -name 'nablarch-testing-yaml-1.0.0-SNAPSHOT.jar')" \
  nablarch/test/ntf-testdata-yaml-schema.json |
  python3 -c 'import json,sys; d=json.load(sys.stdin)["$defs"]["directives"]; print(sorted(d.keys()))'
```

出力は `['additionalProperties', 'description', 'properties', 'type']` の 1 行で、
`required` も `minProperties` も現れない。

導出コマンド（分類の件数ではなく、空欄そのものの総数 27 を数える）:

```sh
cd "$(git rev-parse --show-toplevel)"/.rn/ntf-test-data-converter/coverage
grep -E '^\| [A-F][0-9-]' axis-matrix.md | awk -F'|' '{gsub(/ /,"",$4)} $4 == "—"' | wc -l
```

### 5.4 `—` の裏に隠れている担保の穴（1 件）

**辺③ F3-02（`overwrite=false` で `.xlsx` が既存のときの衝突）は、変換全体としては到達可能なのに、
担保テストが `src/test` に 1 件も無い。**
§5.3 で `—`（対象外）に置いているのは本書の計上単位が「その辺の担当クラス」だからであって
（`XlsFormatWriter` は `overwrite` を保持せず、検査は上位層の `TestDataConverter#checkOverwrite` で完結する）、
穴が無いからではない。§7 の 8 件とは別の穴である（§7 は #26.5 から持ち越した 0 件テーブル関連の 8 件で、
ユーザー確定の単位。ここに 9 件目として足さない）。

- 上位層の衝突検査そのものは担保されている。 `TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse`
  ／ `ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict` の 2 件。
  この 2 件が辺④ F4-02（§4.6）の「上位層に担保がある」の中身でもある ——
  どちらも XLS→YAML で `YamlFormatHandler#outputPaths` を通るため、`.yaml` 側の衝突は実際に通っている。
- ただし 2 件とも出力側は `.yaml` である。 出力先へ事前にファイルを置いてから
  変換するテストは `src/test` に 4 か所あり、4 か所とも `.yaml` である。

```sh
cd "$(git rev-parse --show-toplevel)"
grep -rnE 'Files\.(createFile|write)\(out\b' src/test --include=*.java | sed 's/:[0-9]*:/: /'
```

出力は 4 行で、置いているファイルは順に `out.resolve("BookA/data.yaml")` ／
`out.resolve("BookA/data.yaml")` ／ `out.resolve("Book/data.yaml")` ／ `out.resolve("BookA/data.yaml")` である。
**`.xlsx` を事前に置く行は 1 つも無い。** したがって `XlsFormatHandler#outputPaths`
（`container.getName() + ".xlsx"` を返す）が衝突を検出する分岐は `src/test` で 1 度も通っていない。

**状態を `❌` にしていないのは、`❌` の定義（§0.1）が「その辺で到達可能なのに担保テストが無い」だからである。**
辺③の担当クラス（`XlsFormatWriter`）からはこの分岐に到達できないので、状態欄は `—` のままとし、
変換全体で見たときの穴としてここに開示する。

---

## 6. 本作業で確かめた範囲と、確かめていない範囲

### 6.1 `inventory.md` の記述と HEAD のテストソースが食い違っていた箇所

`inventory.md` の §1〜§4 は各タスク時点のスナップショットであり書き換えない取り決めだが、
`inventory.md` §0.1-2 の「担保の現在地」を述べた記述は現行の正を述べる位置にある。 そこで本タスクでは
同 §0.1-2 の「担保の現在地（HEAD で存在を確認済み）」表を全数、機械的に照合した。

```sh
cd "$(git rev-parse --show-toplevel)"
awk '/^\| 削除したテスト \| 担保の現在地/,/^$/' .rn/ntf-test-data-converter/coverage/inventory.md \
  | grep '^|' | grep -v '^| 削除したテスト' | grep -v '^|---' \
  | awk -F'|' '{print $3}' \
  | perl -CSDA -ne 'while (/`([A-Z][A-Za-z0-9]*)?#(\w+)/g) { print((defined $1 ? $1 : "-") . "#$2\n") }' \
  | sort -u \
  | while IFS='#' read -r cls mth; do
      [ "$(grep -rho "void $mth" src/ | wc -l)" = 0 ] && echo "HEAD に無い: $cls#$mth"
    done
```

出力は 8 行である（うち 2 行はクラス名つき、6 行はクラス名を省いた略記のまま出る。
`inventory.md` 側が略記で書いているためで、下表ではクラス名を書き切ってある）。
8 件とも、`inventory.md` の本文を読むと「追補その 5 が挙げていた `#…` は HEAD に無い」という形で
不在を明示している文脈に現れる名前であり、現在の担保として挙げているものではない。
**したがって `inventory.md` §0.1-2 の当該表に、本書と食い違う記述は残っていない。**

| # | 名前（クラス名を書き切った形） | `inventory.md` 側でクラスを決めている箇所 |
|---|---|---|
| 1 | `XlsFormatWriterTest#rejectsTableBlockWithoutColumnNames` | `inventory.md` §0.1-2 の C-08 行 |
| 2 | `XlsFormatWriterTest#rejectsListMapBlockWithoutColumnNames` | 同上 |
| 3 | `XlsFormatWriterTest#rejectsFieldWithoutLengthInFixedFileBlock` | `inventory.md` §0.1-2 の C-21 行 |
| 4 | `XlsFormatWriterTest#rejectsFieldWithoutLengthInMessageBlock` | 同上 |
| 5 | `YamlFormatWriterTest#serialize_fieldWithoutLengthInFixedFileBlock_rejected` | 同上 |
| 6 | `YamlFormatWriterTest#serialize_fieldWithoutLengthInMessageBlock_rejected` | 同上 |
| 7 | `SampleConversionTest#stopsClimanSampleConversionBecauseOfZeroRowTable` | `inventory.md` §0.1-2 のサンプル変換の行 |
| 8 | `RecordLayoutTest#レコード種別を省略してもフィールド0件のレコードは生成できない` | `inventory.md` §0.1-2 の C-17 行 |

クラス名を書き切った形で不在を確かめ直すコマンド:

```sh
cd "$(git rev-parse --show-toplevel)"
for p in XlsFormatWriterTest#rejectsTableBlockWithoutColumnNames \
         XlsFormatWriterTest#rejectsListMapBlockWithoutColumnNames \
         XlsFormatWriterTest#rejectsFieldWithoutLengthInFixedFileBlock \
         XlsFormatWriterTest#rejectsFieldWithoutLengthInMessageBlock \
         YamlFormatWriterTest#serialize_fieldWithoutLengthInFixedFileBlock_rejected \
         YamlFormatWriterTest#serialize_fieldWithoutLengthInMessageBlock_rejected \
         SampleConversionTest#stopsClimanSampleConversionBecauseOfZeroRowTable \
         RecordLayoutTest#レコード種別を省略してもフィールド0件のレコードは生成できない; do
  cls=${p%%#*}; mth=${p#*#}
  printf "%s -> %s\n" "$p" "$(grep -c "void $mth" "$(find src/test -name "$cls.java")")"
done
```

出力は 8 行とも `-> 0` である。

> **経緯（本タスク中に動いた）**: 本タスク着手時点の `inventory.md` §0.1-2 は、この 8 件のうち 6 件を
> 「担保の現在地」として挙げており、また「`inventory.md` §3.1-3 の C-15 行が挙げる
> `XlsFormatWriterTest#rejectsMessageBlockWithoutRecords` ／ `#rejectsSendSyncMessageBlockWithoutRecords` は
> HEAD にも在る（削除していない）」と書いていた（どちらも HEAD に無い）。
> 並行して走っていた別タスクが `inventory.md` を HEAD の実測へ追随させたため、いずれも解消している。
> 本書はこの経緯を記録するだけで `inventory.md` を書き換えていない。

#### スナップショット節側で見つけた食い違い（本タスクで内容まで読んだのは 3 点。全数ではない）

**「残るのは n 点だけ」とは言えない。** 下表は本タスクで本文まで読んで内容を確かめた 3 点であって、
スナップショット節に他の食い違いが無いことは確かめていない（機械照合の結果は下段。未確認は §6.2 の 5）。

| 箇所 | `inventory.md` の記述 | HEAD の事実 |
|---|---|---|
| 辺① C-15(空) ／ E-3(0件) の電文経路 | `inventory.md` §1.2-2 ／ 同 §1.3 が担保テストとして `XlsFormatReaderRealFileTest#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook` を挙げている | 同名のメソッドは HEAD に無い（`grep -rho "void readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook" src/ \| wc -l` → 0。同名の Javadoc 参照だけが 1 か所ある）。同じ入力は `XlsFormatReaderRealFileTest#rejectsMessageWithFwHeaderOnlyInRealBook` へ意味ごと反転しており、本文レコード 0 件の電文は読み取り時点で `IllegalArgumentException` になる。本書では辺① C-15(空) を「担保済み」ではなく「到達不能」とした |
| 辺② C-14(空) ／ C-15(空) ／ E-3(0件) | `inventory.md` §2.1 の表が `YamlFormatReaderTest#readMessage_emptyBody_isStillMapped` を担保として挙げている | 同名のメソッドは HEAD に無い（`grep -c "void readMessage_emptyBody_isStillMapped" src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderTest.java` → 0）。`YamlFormatReaderTest#readMessage_emptyBody_rejected` へ意味ごと反転している（`inventory.md` §0.1-2 の本文が「`#readMessage_emptyBody_rejected`（`#readMessage_emptyBody_isStillMapped` を反転）が固定する」と書いている）。本書では辺② C-15(空) を「到達不能」とし、根拠テストに反転後の名前を挙げている（§2.3） |
| 辺④ C-01 | `inventory.md` §4.1 の表が `YamlFormatWriterTest#write_writesEachSectionAsYamlFileWithSerializedContent` の軸C 欄に `C-01` を挙げている | `YamlFormatWriter#write` は `container.getName()` を 1 度も参照しない（§4.3 の C-01 に導出コマンド）。同メソッドが固定しているのは `section.getName() + ".yaml"` と中身の一致であってコンテナの名前ではない。本書では辺④ C-01 を空欄とした |

名前のレベルでは `inventory.md` 全体を機械照合してある。**ただしこれは「名前が HEAD に在るか」だけの照合であり、
在る名前の軸要素対応が正しいかは見ていない。**

```sh
cd "$(git rev-parse --show-toplevel)"
I=.rn/ntf-test-data-converter/coverage/inventory.md
REFS() { perl -CSDA -ne 'while (/`([A-Z][A-Za-z0-9]*Test)#(\w+)/g) { print "$1#$2\n" }' "$I" | sort -u; }
REFS | wc -l                                  # クラス名つきのテスト参照（重複を除く）
REFS | while IFS='#' read -r cls mth; do
        f=$(find src/test -name "$cls.java")
        if [ -z "$f" ]; then echo "NG(class) $cls#$mth"
        elif ! perl -CSDA -e 'my ($m,$f)=@ARGV; open(my $h,"<:utf8",$f) or exit 1;
                              local $/; my $s=<$h>; exit($s =~ /\bvoid\s+\Q$m\E\s*\(/ ? 0 : 1);' "$mth" "$f";
        then echo "NG(method) $cls#$mth"; fi
      done | wc -l                            # HEAD に無いもの
perl -CSDA -ne 'while (/`#(\w+)/g) { print "#$1\n" }' "$I" | sort -u | wc -l   # 略記のまま書かれた参照
```

出力は 105 ／ 34 ／ 103 である。34 件には少なくとも次の 4 種が混ざっており、
**本タスクで種別を読み分けたのは上表の 3 点と §6.1 冒頭の 8 件だけである**（残りは未確認）。

- 総称・接頭辞として書かれた名前（`RoundTripTest#xxx` ／ `XlsFormatWriterTest#roundTrips` ／ `YamlFormatWriterTest#roundTrip_` など）
- `void` を返さないテストヘルパやフィールド（`YamlFormatReaderTest#reader` ／ `YamlFormatWriterModelTest#record` ／ `YamlFormatReaderScalarTest#readValue` など）
- 「削除済みで HEAD に無い」と本文が明示している名前
- 現在の担保として挙げているが HEAD に無い名前（上表の 3 点がこれに当たる）

略記のまま書かれた 103 件は、クラスが機械的に決まらないため上の照合の対象外である。

`inventory.md` §1.2-2 ／ 同 §1.3 ／ 同 §2.1 ／ 同 §4.1 はスナップショットであり書き換えない取り決めのため、
`inventory.md` 側はいずれも本タスクで書き換えていない（`steering.md` Rules に従い、本書が逆引きの正である）。

### 6.2 推測で書かず「未確認」と明記した箇所

| # | 未確認の内容 | 影響 |
|---|---|---|
| 1 | 🔺 欄の軸要素対応（§0.5）。往復テスト 44 メソッドのうち 42 件は、名前が HEAD に実在することは確かめたが、その軸要素対応を本タスクで読み直していない。内訳は `RoundTripTest` 29 件（出典 `inventory.md` §0.8-8）と `XlsFormatWriterTest` ／ `YamlFormatWriterTest` の 13 件（出典 `inventory.md` §3.1 ／ 同 §4.1 のスナップショット表。本書冒頭が「現在の担保を示すものではない」と断っている表である）。残る 2 件（0 件テーブルの往復）は本文を読んで確かめた | 🔺 は正式担保に数えないため、状態欄の判定には影響しない。`inventory.md` のスナップショット表を出典に使っているのは本書ではこの欄だけである（`inventory.md` そのものは、§0.3 の軸の定義・§1.4 の欠番の説明・§2.4 の対象範囲・§3.6 と §4.6 の軸F の要素数など、ほかにも出典として引いている） |
| 2 | 辺② 軸D の 10 ケースの経路差（§2.4）。`setup_tables` 以外の 2 経路で同じ結果になることは確かめていない | 軸D 辺② の ✅ は `setup_tables` 経路での担保である |
| 3 | 辺④ 軸D の 7 ケースの経路差（§4.4）。レコード断片経路はプローブでの確認にとどまりテストが無い | 同上 |
| 4 | 辺④が書き出す YAML のスキーマ適合（§4.6）。「`issues.md` YML-12 の 4 形以外にスキーマ違反を書き得る形が無い」ことは確かめていない | — |
| 5 | `inventory.md` のスナップショット節の内容照合。§6.1 で内容まで確かめたのは `inventory.md` §0.1-2 の「担保の現在地」表（8 件）と、スナップショット節で見つけた 3 点だけである。名前のレベルでは同文書全体を機械照合したが（§6.1 の 105 ／ 34 ／ 103）、HEAD に無い 34 件の種別も、HEAD に在る 71 件の軸要素対応も読み分けていない。本書は `inventory.md` ／ `issues.md` ／ `coverage-report.md` を書き換えていない | 本書の判定はテストソースを正としているため影響しない |
| 6 | 辺① C-13(空) の到達不能根拠が送信同期経路の 1 本だけである（§1.3）。受信 `MESSAGE` 経路でディレクティブ行 0 行を通す根拠テストが `src/test` に無い | 辺② の同じ行は 2 経路それぞれの根拠を挙げており、辺で厚みが割れている。到達不能という判定そのものは `DataFile` の注入機構（`issues.md` XLS-07）に依るため変わらない |
| 7 | `model/` の不変条件の全数（§0.4）。上表 9 行＋下表 5 行の 14 行が全数かは数えていない | 本書が閉じているのは空欄の側だけである（§0.4 の末尾） |
| 8 | 辺① C-17(空) の本体側の番人が 2 つある（§1.3）。根拠テスト 2 件が通すのは `DataFileParser#processDirectives` の 2 列ガードで、2 断片目以降の名前行を閉じる `DataFileFragment#setNames` の `assertNotNullOrEmpty` を通す根拠テストは `src/test` に無い | 到達不能という判定は変わらない（2 経路とも本体側で閉じることは実装を読んで確かめた）。理由欄が挙げていた機構名が実際より狭かった |

上記以外の担保テストメソッドは、1 件残らずテストソースを開いて Given／When／Then とアサートを読み、
その軸要素を担保していることを確かめた（§0.6 の実在照合コマンドで名前の実在も機械的に照合してある）。
**軸E については本巡で 4 辺 44 セルを総点検し、「0 件」「1 件」を主張するセルすべてについて
担保テストの本文を開いて件数が固定されているかを確かめ直した**（総点検で ❌ が 2 件出て、#27 の中で
テストを 2 本足して埋めた。経緯は §5.2）。**この読み方——「表が主張する要素を、そのテスト本文が
実際にアサートしているか」——を全セルへ広げる水平展開は本書の外（#27 の Steps）で扱う。**

---

## 7. 埋まっていない担保の穴（#26.5 から持ち越した 8 件）

**#26.5 で担保が二層（値の literal 2 件・機構の往復 4 件）あることを実測で確かめたうえで、なお埋まっていない穴である。**
——「未検証だから穴」ではない。8 件はいずれも #26.5 の変更が持ち込んだ欠陥ではなく既存の穴である
（`steering.md` #26.5「#27 へ持ち越す担保の穴 8 件」。ユーザー確定・2026-08-21）。

**8 件はすべて 0 件テーブル／マーカーカラム `[EMPTY]` にまつわるもので、本書の次の 4 行が理由欄から
ここを参照している**: 辺① C-08(空)（§1.3）／辺② C-08(空)（§2.3）／辺③ C-08(空)（§3.3）／
辺④ C-08(空)（§4.3）。4 行とも状態は ✅ である —— 空欄ではなく、
✅ の担保の厚みに残る穴としてここに開示する（`steering.md` Rules「担保の穴は、テストを足さない場合でも
台帳に開示する」）。

| # | 穴の内容 |
|---|---|
| ① | 実 `.xlsx` を通る唯一の経路 `SampleConversionTest#convertsClimanSampleIncludingZeroRowTable` がマーカーを検証していない。同メソッドの Then は「変換件数が 2」と `Files.exists` 2 本だけで、書き出したブックを開き直していない（テスト本文を読んで確かめた）。`[EMPTY]` という語も同クラスに 1 度しか現れず、それは Javadoc の中である（導出は下のコマンド） |
| ② | `EXPECTED_TABLE` の 0 件往復テストが無い |
| ③ | 0 件テーブルが唯一・末尾のブロックの往復テストが無い |
| ④ | `columnNames=[]` かつ「セルを持たない行」を N 件持つ形（XLS-08 ／ YML-04）の往復テストが無い |
| ⑤ | 実カラム名が `[EMPTY]` と衝突する形の明示テストが無い |
| ⑥ | DB 実行経路（`TableData#replaceData`）の再実測が無い |
| ⑦ | 2026-08-19 プローブの (2)(4) は `[空]` での実測であり `[EMPTY]` で再実測していない |
| ⑧ | 命名規約そのもの（ASCII の角括弧トークンであること）を固定するテストが無い |

① の導出:

```sh
cd "$(git rev-parse --show-toplevel)"
S=src/test/java/nablarch/test/tool/converter/SampleConversionTest.java
grep -c "\[EMPTY\]" "$S"        # → 1（Javadoc の 1 行だけ）
grep -c "assert.*EMPTY" "$S"     # → 0（アサートは 1 件も無い）
```

**⑧ についてはテストを足さない判断が済んでいる**（ユーザー確定・2026-08-21）。
明文（`testdata_notation.rst`）が定めているのは「カラム名を半角角括弧で囲むとマーカーカラムになる」ことだけで、
`EMPTY` という語は明文が定めたものではなく converter の選択だからである。

**この 8 件がある一方で、0 件テーブルの現在の担保は次の 4 件である**（いずれも本タスクで本文を読んで確かめた）。

| 担保テストメソッド | 何を固定しているか |
|---|---|
| `XlsFormatWriterTest#writesMarkerColumnForZeroRowTableBlock` | 識別行の次の行がセル 1 個で、その値が `[EMPTY]` であること（テーブル系） |
| `XlsFormatWriterTest#writesMarkerColumnForZeroRowListMapBlock` | 同（LIST_MAP 系） |
| `XlsFormatWriterTest#roundTripsZeroRowTableWithoutEatingNextBlock` | 0 件テーブルを書いて読み戻すとブロックが 2 件のままで、次のブロックを食わないこと |
| `XlsFormatWriterTest#roundTripsZeroRowListMapWithoutEatingNextBlock` | 同（LIST_MAP 系） |
