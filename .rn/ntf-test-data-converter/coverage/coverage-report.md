# カバレッジ計測と未到達分岐の一覧（タスク #26）

**対象 6 区分（§1）の未到達分岐は 34 件で、分類は「テストを足すべき」19 件・「テスト不要」15 件である。**
未到達行は 16 件で、うち 8 行は上の分岐のいずれかに紐づく（その分岐の未到達側でだけ実行される行。
相手の `#` は §4 の表の「§3 の対応」欄にある）。
「テストを足すべき」19 件は `issues.md` §7 へ **14 件の課題**（`COV-01`〜`COV-14`）としてまとめた
（1 課題が複数分岐にまたがる）。テストは足していない。
辺②（`YamlFormatReader`）と中間モデルの 12 クラスは行・分岐とも 100% だが、**100% は担保を意味しない**（§1 の注記）。
**§6 に 6 節・7 件を開示した。うち「担保の穴」は §6-1・§6-2・§6-5・§6-6 の 4 件、
処置済みの台帳訂正は §6-3(a)・§6-3(b) の 2 件、確かめきれなかったことは §6-4 の 1 件である**
（性質が違うものを 1 つの件数にまとめない。§6-3 だけは 1 節の中に体裁の手当てと事実誤りの訂正を含むため 2 件に分けた）。

## 0. 計測条件

| 項目 | 値 |
|---|---|
| 計測時点の HEAD SHA | `da6642578c366b73b3a001980142d9f741f82f7e`（`ntf-test-data-converter`） |
| ワーキングツリー | **計測時点でクリーン**（`git status --short` が無出力）。`src/` は最後まで無変更 |
| JDK | `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64` |
| JaCoCo | オフライン計測（`jacoco:instrument` → `test` → `jacoco:restore-instrumented-classes`）。実行データは `jacoco.exec`（プロジェクト直下。`.gitignore` で除外） |

`src/` が無変更であることと `jacoco.exec` が追跡対象外であることの確認コマンド。

```sh
git diff HEAD -- src/main src/test pom.xml | wc -l   # → 0
grep -n '^jacoco\.exec$' .gitignore                  # → 3:jacoco.exec
```

**実行したコマンド**（この順にそのまま実行する。1 つめの前に `rm -f jacoco.exec` を行い、
`jacoco.exec` が前回実行の追記で汚れていないことを担保している）。

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
rm -f jacoco.exec
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec
```

**本書の全数値は、上の 1 回の実行が作った `target/site/jacoco/` の
`jacoco.csv` ／ `jacoco.xml`（同一実行のペア）だけから導いている。**
別の実行と突き合わせるときは、上の 2 コマンドを流し直して次の md5 を比べる。

```sh
md5sum target/site/jacoco/jacoco.csv
# → d28e374e9027ade63d7919f7a7b5826e  target/site/jacoco/jacoco.csv
```

**`Tests run` の実測行**（surefire 集計行。`BUILD SUCCESS`）。

```
[WARNING] Tests run: 595, Failures: 0, Errors: 0, Skipped: 2
```

**`Skipped: 2` の内訳。** `Skipped` を持つ surefire レポートは 1 ファイルだけで、中身は `@Ignore` 2 件である。
**2 件とも辺②のテストクラスにあり、いずれも「あるべき姿」を書いて他責先の修正を待っているものである。**

```sh
grep -l 'Skipped: [1-9]' target/surefire-reports/*.txt
# → target/surefire-reports/nablarch.test.tool.converter.yaml.YamlFormatReaderInvalidInputTest.txt
grep -rn '@Ignore' src/test/java --include=*.java | grep -v '{@code'
```

| 課題 ID | テストクラス | テストメソッド | 他責先 |
|---|---|---|---|
| YML-14 | `YamlFormatReaderInvalidInputTest` | `failsToReadRecordFragmentRowWithMoreValuesThanFields` | `nablarch-testing` の `DataFileFragment`（`addValue` が余りの値を捨てる） |
| XLS-40 | 同上 | `keepsOriginalColumnCaseInTable` | `nablarch-testing` の `TableData`（カラム名を大文字化する） |

**この 2 件は「あるべき姿を書いたまま無効化されているテスト」であり、担保の穴である**（§6-6 に開示）。

> **`-o`（オフライン）を付けているのは依存解決をリポジトリキャッシュに閉じるためであり、
> 計測結果には影響しない。** `-o` を外しても同じ数値になることは未確認（§6-4）。

## 1. 対象 6 区分の行・分岐カバレッジ

数値の導出コマンド（`target/site/jacoco/jacoco.csv` の 6=BRANCH_MISSED・7=BRANCH_COVERED・
8=LINE_MISSED・9=LINE_COVERED 列から導く）。

```sh
awk -F, 'NR>1 && ($3 ~ /^(XlsFormatReader|XlsFormatWriter|YamlFormatReader|YamlFormatWriter|TestCoreReaderAdapter)/ \
  || $2 ~ /converter\.model$/) { printf "%-52s line %3d/%-3d branch %3d/%-3d\n", $3, $9, $8+$9, $7, $6+$7 }' \
  target/site/jacoco/jacoco.csv
```

**区分①〜④が 4 辺そのもので、⑤は辺①だけの土台、⑥は 4 辺が共有する土台である**
（⑤は辺①が本体リーダを叩くアダプタ。`src/main` で `TestCoreReaderAdapter` を使うのは `XlsFormatReader` だけで、
辺②は別のアダプタ `YamlTestCoreAdapter` を使う——`grep -rln 'TestCoreReaderAdapter' src/main/java/nablarch/test/tool`
→ `XlsFormatReader.java` と `TestDataBlock.java`（Javadoc の言及）の 2 件。
`YamlTestCoreAdapter` は対象 6 区分の外で、その未到達は §6-2 の 14 クラス内訳に載せてある。
⑥は 4 辺が受け渡す中間モデル）。以降この 6 つを「対象 6 区分」と呼び、辺番号は再掲しない。

**下表の行の並びは区分順に組み替えてある**（コマンドの出力は `jacoco.csv` の記録順で、並びだけが異なる）。
値は 1 行ずつ出力と一致する。

| 区分 | クラス（JaCoCo の CLASS 名） | 行（到達/全体） | 分岐（到達/全体） |
|---|---|---|---|
| ① Excel→中間モデル | `XlsFormatReader` | 190/195 | 115/140 |
| ① 同上 | `XlsFormatReader.new DirectiveUtil.ValueMapper() {...}` | 2/2 | 0/0 |
| ② YAML→中間モデル | `YamlFormatReader` | 192/192 | 102/102 |
| ② 同上 | `YamlFormatReader.new DirectiveUtil.ValueMapper() {...}` | 2/2 | 0/0 |
| ③ 中間モデル→Excel | `XlsFormatWriter` | 157/158 | 101/104 |
| ④ 中間モデル→YAML | `YamlFormatWriter` | 157/159 | 86/90 |
| ⑤ 本体アダプタ | `TestCoreReaderAdapter` | 46/46 | 11/11 |
| ⑤ 同上 | `TestCoreReaderAdapter.HeaderCollector` | 16/20 | 6/6 |
| ⑤ 同上 | `TestCoreReaderAdapter.BodyLineCollector` | 23/27 | 12/14 |
| ⑤ 同上 | `TestCoreReaderAdapter.SendSyncBodyCollector` | 8/8 | 0/0 |
| ⑥ 中間モデル | `TestDataContainer` | 8/8 | 2/2 |
| ⑥ 同上 | `TestDataSection` | 8/8 | 2/2 |
| ⑥ 同上 | `TestDataBlock` | 22/22 | 10/10 |
| ⑥ 同上 | `TableDataBlock` | 4/4 | 0/0 |
| ⑥ 同上 | `ColumnRowDataBlock` | 11/11 | 6/6 |
| ⑥ 同上 | `ListMapBlock` | 2/2 | 0/0 |
| ⑥ 同上 | `FileDataBlock` | 14/14 | 4/4 |
| ⑥ 同上 | `FileDataBlock.FileType` | 1/1 | 0/0 |
| ⑥ 同上 | `MessageDataBlock` | 13/13 | 2/2 |
| ⑥ 同上 | `RecordLayout` | 15/15 | 2/2 |
| ⑥ 同上 | `FieldDef` | 12/12 | 4/4 |
| ⑥ 同上 | `ModelPreconditions` | 40/40 | 28/28 |

**区分ごとの合計。** 手で足さずに次のコマンドで導く（下表はその出力そのものである）。

```sh
awk -F, 'NR>1 {
  if ($3 ~ /^XlsFormatReader/)            k="① XlsFormatReader";
  else if ($3 ~ /^YamlFormatReader/)      k="② YamlFormatReader";
  else if ($3 ~ /^XlsFormatWriter/)       k="③ XlsFormatWriter";
  else if ($3 ~ /^YamlFormatWriter/)      k="④ YamlFormatWriter";
  else if ($3 ~ /^TestCoreReaderAdapter/) k="⑤ TestCoreReaderAdapter";
  else if ($2 ~ /converter\.model$/)      k="⑥ 中間モデル";
  else next;
  ml[k]+=$8; lc[k]+=$9; mb[k]+=$6; bc[k]+=$7; n[k]++;
  tml+=$8; tlc+=$9; tmb+=$6; tbc+=$7; tn++;
}
END {
  m=split("① XlsFormatReader,② YamlFormatReader,③ XlsFormatWriter,④ YamlFormatWriter,⑤ TestCoreReaderAdapter,⑥ 中間モデル", a, ",");
  for (i=1;i<=m;i++) { k=a[i];
    printf "%-26s classes %2d  line %3d/%-3d  branch %3d/%-3d  missed line %d branch %d\n", k, n[k], lc[k], ml[k]+lc[k], bc[k], mb[k]+bc[k], ml[k], mb[k] }
  printf "%-26s classes %2d  line %3d/%-3d  branch %3d/%-3d  missed line %d branch %d\n", "合計", tn, tlc, tml+tlc, tbc, tmb+tbc, tml, tmb;
}' target/site/jacoco/jacoco.csv
```

```
① XlsFormatReader          classes  2  line 192/197  branch 115/140  missed line 5 branch 25
② YamlFormatReader         classes  2  line 194/194  branch 102/102  missed line 0 branch 0
③ XlsFormatWriter          classes  1  line 157/158  branch 101/104  missed line 1 branch 3
④ YamlFormatWriter         classes  1  line 157/159  branch  86/90   missed line 2 branch 4
⑤ TestCoreReaderAdapter    classes  4  line  93/101  branch  29/31   missed line 8 branch 2
⑥ 中間モデル                    classes 12  line 150/150  branch  60/60   missed line 0 branch 0
合計                         classes 22  line 943/959  branch 493/527  missed line 16 branch 34
```

**辺②（区分②）と中間モデル（区分⑥）の 12 クラスは行・分岐とも 100% である。**
区分⑥が 12 クラスであることの導出（JaCoCo の CLASS 単位。`FileDataBlock.FileType` を含む）。

```sh
awk -F, 'NR>1 && $2 ~ /converter\.model$/ { print $3 }' target/site/jacoco/jacoco.csv | wc -l   # → 12
```

> **100% は担保を意味しない。カバレッジ数値だけを担保の根拠にしないこと。**
> `inventory.md` §2.1-2 の末尾が #26 宛にこの点を指示している —— 辺② は行 100%・分岐 100% の状態でも
> 変異が 3 件生存し、閉じた次のラウンドでもさらに 3 件が生存した（原因は入力の作り方の偏りと
> アサートの弱さで、どちらもカバレッジでは検出できない）。**本書の §0 が示すとおり、その辺②のテストクラスには
> あるべき姿を書いた `@Ignore` が 2 件残っている**（YML-14・XLS-40）。
> 「行・分岐 100%」と「あるべき姿のテストが 2 件無効化されている」は同時に成り立つ。

## 2. 未到達分岐の全数と、どちら側が未到達かの導出

**未到達分岐の総数は 34 件・未到達行は 16 件。** JaCoCo が数える分岐は `IFxx` ／ `TABLESWITCH` ／ `LOOKUPSWITCH` の
出口エッジであり、1 行に複数の条件がある場合はその行にまとまって計上される。
**本書はこの出口エッジを一貫して「分岐」と呼び、数えるときの助数詞は「件」でそろえる**
（`switch` の `case` が飛ぶ先だけは「飛び先」と呼んで分岐と呼び分ける。
同じ飛び先を共有する `case` は 1 件の分岐に畳まれるためである）。
行単位の `mb`（未到達分岐数）・`cb`（到達分岐数）・`mi`（未実行命令数）・`ci`（実行命令数）は
`target/site/jacoco/jacoco.xml` から取る。

**次のコマンドが出すもの。** 対象 5 ファイルについて、`mi > 0` **または** `mb > 0` の行を
1 行ずつ印字し、最後にファイルごとの（未到達分岐数, 未到達行数）を出す。
**この末尾行が 34 件・16 件の出典である。**
印字される行は §4 の未到達行 16 件の**上位集合**で、部分実行行（`mi > 0` かつ `ci > 0`。行としては到達済み）も含む。
**§3 の「未到達の側」「分類」「根拠」はこのコマンドの出力ではない**（決め方は下記）。

```sh
python3 - <<'EOF'
import re
xml = open('target/site/jacoco/jacoco.xml', encoding='utf-8').read()
targets = ('XlsFormatReader.java', 'XlsFormatWriter.java', 'YamlFormatReader.java',
           'YamlFormatWriter.java', 'TestCoreReaderAdapter.java')
tot = {}
for m in re.finditer(r'<sourcefile name="([^"]+)">(.*?)</sourcefile>', xml, re.S):
    name, body = m.group(1), m.group(2)
    if name not in targets:
        continue
    mb = ml = 0
    for l in re.finditer(r'<line nr="(\d+)" mi="(\d+)" ci="(\d+)" mb="(\d+)" cb="(\d+)"/>', body):
        nr, mi, ci, b, cb = map(int, l.groups())
        if mi or b:
            print(f'{name} L{nr} mi={mi} ci={ci} mb={b} cb={cb}')
        mb += b
        ml += 1 if (mi and ci == 0) else 0
    tot[name] = (mb, ml)
print(tot)   # {file: (未到達分岐数, 未到達行数)}
EOF
```

実行結果の末尾行。**`print(tot)` は dict を 1 行で出力する。下は読みやすさのために
3 行へ折り返したものであり、折り返し以外は出力そのままである。**

```
{'XlsFormatReader.java': (25, 5), 'XlsFormatWriter.java': (3, 1),
 'YamlFormatWriter.java': (4, 2), 'YamlFormatReader.java': (0, 0),
 'TestCoreReaderAdapter.java': (2, 8)}
```

`25 + 3 + 4 + 0 + 2 = 34`（分岐）／`5 + 1 + 2 + 0 + 8 = 16`（行）。§1 の区分別合計とも一致する。

**「どちら側が未到達か」の決め方。** JaCoCo は側を直接は出さない。次の 3 つだけを根拠にしている。

1. 同じ行または直後の行の `mi` ／ `ci`（例：`if (x) { throw ... }` の `throw` 行が `mi>0 ci=0` なら
   `if` の true 側が未到達）
2. 消去法（`mb + cb` は `if` の条件数 × 2、`switch` は相異なる飛び先数 + `default` に一致するため、
   到達済みが確定した側を引く）
3. 到達済みであることを示す `src/test` の実在テスト（該当箇所に明記した）

**`isEmptyEntry` の 4 分岐（#22）は実在テストで側を確定した。**
`XlsFormatReader.java:585`（`if (value != null && !value.isEmpty())`）は `mb=1 cb=3` で、
4 件の分岐のうち 1 件だけが未到達である。**`!value.isEmpty()` の false 側（＝空文字の要素）は
次の 2 件が到達させている。**

| 到達させているテスト | 入力 | アサート | 通る呼び出し元 |
|---|---|---|---|
| `XlsFormatReaderTest#readTableNormalizesExcelQuotationNotation` | `XlsFormatReaderTest.java:171` `lines.add(row("\"\"", "\"abc\"", "${expr}"));` | 同 `:179` `assertThat(row.get(0), is(""));` | `dropEmptyEntries(rows)`（`XlsFormatReader.java:162`。テーブル系） |
| `XlsFormatReaderTest#readListMapNormalizesExcelQuotationNotation` | 同 `:196` `lines.add(row("\"\"", "\"val\""));` | 同 `:204` `assertThat(row.get(0), is(""));` | `dropEmptyEntries(rows)`（同 `:193`。LIST_MAP） |

**先頭セルは Excel の引用符記法 `""` であり、`stripQuotes`（`XlsFormatReader.java:539-545`）が
`dropEmptyEntries` に渡る前に空文字へ畳む**（アサートが `is("")` でそれを固定している）。
そのため `isEmptyEntry`（`:583-590`）のループは、
1 要素目で `value != null` が true・`!value.isEmpty()` が **false** になって次の要素へ進み、
2 要素目（`abc` ／ `val`）で `!value.isEmpty()` が **true** になって `return false` する。
**`value != null` の true 側と `!value.isEmpty()` の両側の計 3 件が到達済みであるから、
残る 1 件すなわち `value != null` の false 側が未到達である。**

**`isQuotationWrapped` の `L510`（#16）も実在テストで側を確定した。**
`L510`（`value.startsWith("\"") && value.endsWith("\"")`）は `mb=1 cb=3` で、到達済み 3 件の内訳は次のとおり。

- **(a)(b)** `L494`（`return stripQuotes(value);`）が到達済み（`mi=0 ci=3`）＝ `startsWith("\"")` の true 側と
  `endsWith("\"")` の true 側。ここを通れるのは `L510` だけである（`L511` は `mb=3 cb=1` で
  全角側が一度も true にならない。#17〜#19）。
- **(c)** `startsWith("\"")` の **false 側**は `XlsFormatReaderTest#readMapsExpectedRequestHeaderMessageBlock` が
  通している —— 入力 `XlsFormatReaderTest.java:567` `lines.add(row("text-encoding", "ms932"));`、
  アサート 同 `:584` `assertThat(message.getDirectives().get("text-encoding"), is("ms932"));`。
  `ms932` は 5 文字なので `L507` を抜け、`record-separator` ／ `field-separator` でもないので
  `L493` の `isQuotationWrapped` へ入る。このテストの入力は `EXPECTED_REQUEST_HEADER_MESSAGES`（送信同期系）
  なので `readSendSyncBlocks` を通り、経路は `XlsFormatReader.java:276` → `toStringDirectives`（`:467-475`）
  → `normalizeDirectiveValue`（`:472` から `:482`）→ `:493` である
  （`toStringDirectives` の呼び出しは `:214` `:246` `:276` の 3 箇所で、
  `:246` は `readMessageBlock` が `DataType.MESSAGE` を扱う別経路である）。

**したがって残る 1 件、すなわち `endsWith("\"")` の false 側が未到達である。**

## 3. 未到達分岐 34 件の一覧と分類

出典表記の約束：`notation:nnn` は `nablarch-document` の
`ja/development_tools/testing_framework/implementation/testdata_notation.rst` の
**`30a8271` 時点**の行番号（`steering.md`「出典の版」に合わせた）。
`nablarch-testing` 側の行番号は同リポジトリの `c5f3340` 時点。
`src/main` ／ `src/test` の行番号は §0 の HEAD SHA `da66425` 時点。
**出典（ファイル名・行番号・文書名）を添えた鉤括弧・`>` は逐語引用であり、引用中の太字は引用者による**
（`notation` の原文にも `src/main` のコメントにも強調の書式は無い）。
**`notation:nnn` からの引用には、次の 2 つの正規化を掛けている**（いずれも引用者による変形であり、
省略記号では示していない）。**(1)** RST のインラインエスケープ `\ `（バックスラッシュ＋半角空白）を
半角空白へ均し、全角の括弧・読点に隣接して生じるぶんは落とした（例 `notation:266` の
`データタイプが合致する（\ Excel\ 形式では…`）。**(2)** 文の途中までを引く場合は、
末尾に省略記号を置かず切ったところで閉じている（例 `notation:1325` は原文の「。」の手前で切ってある）。
**引用の内側に現れる鉤括弧は、入れ子を避けるため『 』へ改めている**（例 §6-3 の `steering.md` 引用）。
**出典を添えない鉤括弧は引用ではなく、本書内の分類ラベル**（「テストを足すべき」「テスト不要」など）
**または語の強調である。**

**根拠欄が挙げる「こう書けば到達する」は、コードを読んで導いた推定であり、実行して確かめた事実ではない。**
#26 ではテストを 1 件も足していないためである（§5。`git diff --stat da66425 58bae09 -- src/main src/test pom.xml` が無出力。
`58bae09` は #26 の完了コミット）。実行して確かめた事実は、JaCoCo の数値（`mi` ／ `ci` ／ `mb` ／ `cb`）と、
**到達済みの側**を示す実在テスト（§2）だけである。両者は文体では区別できないので、ここで断る。
**同じ断りが `issues.md` §7 の `- 到達する入力:` 欄にも掛かる**（同欄も #26 で書いたものである）。

**節の並びは辺①（§3.1）→ 辺③（§3.2）→ 辺④（§3.3）→ 区分⑤（§3.4）である**
（辺②は未到達分岐 0 件のため節を置かない。§1 の区分別合計）。

分類は 2 値。**「テストを足すべき」19 件・「テスト不要」15 件**（`19 + 15 = 34`）。件数の導出——

```sh
F=.rn/ntf-test-data-converter/coverage/coverage-report.md
grep -cE '^\| [0-9]+ \|.*\*\*テストを足すべき\*\*' $F   # → 19
grep -cE '^\| [0-9]+ \|.*テスト不要（' $F                  # → 15
```

（行頭を `| 数字 |` に限っているのは、この節の説明文とコマンド行自身が同じ語を含み、
限らないとそれらまで数えて主張を反証するためである。
素朴な `grep -c 'テスト不要（' $F` は 22 を返す。）

**「テスト不要」側の 5 値の意味と件数は次の §3.0 の凡例にある。**
§3.1〜§3.4 の各表の「分類」欄はこの 5 語だけを使う。

### 3.0 「テスト不要」の理由の凡例

**§3 の分岐 34 件については、「テスト不要」の根拠は次の 5 値に閉じる。各行はこの語だけを使う。**
（§4 の未到達行 8 件は分岐ではなく、この 5 値のどれにも当たらない。§4 で別の語を当てている。）

| 理由 | 意味 | 件数 |
|---|---|---|
| 型の全数分岐 | 取りうる値を上ですべて分岐し尽くしているため、残る側へ入る値が型・列挙として存在しない（sealed 階層／`DataType`） | 3 |
| 中間モデルの不変条件 | その値は中間モデルの生成時に拒否されるため、この箇所へ届かない | 2 |
| 内部整合性ガード | `src/main` 自身が「到達＝実装のバグ」と明記しているガード。**4 件とも根拠はそのコメントだけで、到達可能性は §6-5(a) で「未確認」と自認している** | 4 |
| 実 `.xlsx` では生じない／Fake から到達可 | 実 `.xlsx` 経路のセル値は `PoiXlsReader.java:123`（`cell == null ? "" : cell.toString()`）により必ず非 `null` である。**ただし converter の Fake リーダ経由では到達できる**（§6-1 に開示） | 4 |
| 本体パーサが先に弾く | その入力を書いても、本体パーサが器の組み立て時に失敗するため、この箇所へ届かない | 2 |

**到達可能性の開示（§6-5）への参照は、行ごとではなく上の凡例に 1 か所だけ置いた。** 「内部整合性ガード」は 4 件（#8〜#11）が
すべて §6-5(a) の対象なので、分類ラベルの側に 1 回書けば §3.1 のどの行からも辿れる。
各行にも `§6-5` を書くと、§6-5 が `#5・#8・#9・#10・#11` を名指ししているのと**同じ関係を 2 方向に**
手書きすることになる（`steering.md` Rules「同じ関係を 2 方向に手書きしない」）。
ラベルから開示節への参照はその関係の写しではないので、規約と衝突しない。
**「型の全数分岐」は 3 件のうち #5 だけが §6-5(b) の対象でラベルの側では表せないため、
そちらは #5 の行に書いてある。**

**同じ「実 `.xlsx` では `null` が生じない」状況でも、記法の明文がその `null` の扱いを定めているものは、
この理由を使わず「テストを足すべき」にしている。** これが #21（`stripQuotes`）と #22（`isEmptyEntry`）の
分け目である —— #22 は `notation:1535`「全要素が null または空文字のエントリは読み飛ばされる。」が
`null` を名指ししているのに対し、#21 の根拠は呼び出し元 4 箇所の実装と `issues.md` XLS-09 だけで、
`null` の扱いを定めた明文は挙がっていない（切り分けは → `issues.md` COV-14）。

件数の導出（合計 15）。

```sh
F=.rn/ntf-test-data-converter/coverage/coverage-report.md
grep -cE '^\| [0-9]+ \|.*テスト不要（型の全数分岐）' $F                              # → 3
grep -cE '^\| [0-9]+ \|.*テスト不要（中間モデルの不変条件）' $F                      # → 2
grep -cE '^\| [0-9]+ \|.*テスト不要（内部整合性ガード）' $F                          # → 4
grep -cE '^\| [0-9]+ \|.*テスト不要（実 `\.xlsx` では生じない／Fake から到達可）' $F  # → 4
grep -cE '^\| [0-9]+ \|.*テスト不要（本体パーサが先に弾く）' $F                      # → 2
```

> **「本体パーサが先に弾く」は他の 4 値と機構が違う。** 記法の禁止でも型の制約でもなく、
> **実行時に本体パーサが器の組み立てに失敗すること**による到達不能である。
>
> **3 巡目レビューで「NTF 仕様外」（記法の明文がその入力を書けないものとして定めている）を落とした。**
> この語を使っていた #12・#13 の根拠は、記法が「そう書いてはいけない」と言っているだけで
> 「その入力を書いても分岐に届かない」を示しておらず、到達可能性の議論になっていなかった。
> 到達不能の実体は本体パーサの器の組み立て失敗であるため、新しい値へ移した（§3.1 #12・#13）。

### 3.1 辺① `XlsFormatReader`（25 件）

| # | メソッド | 行（`da66425` 時点） | 未到達の側 | 分類 | 根拠 |
|---|---|---|---|---|---|
| 1 | `read` | **116** | `processed.add(singleKey(type, header.getIdentifier()))` の **false 側**（`:115` の `else if (type == DataType.LIST_MAP)` の中） | **テストを足すべき** | `notation:622`「同一の読み込み単位内に同じ ID のデータブロックが複数ある場合は**先着一致**となり、2件目以降は無視される」。この分岐は明文化された先着一致そのものであり、到達不能ではない |
| 2 | `read` | **120** | `processed.add(singleKey(type, header.getIdentifier()))` の **false 側**（`:119` の `else if (type == DataType.MESSAGE)` の中） | **テストを足すべき** | 同じ先着一致が `MESSAGE` にも明文で及ぶ。`notation:256`「データブロックの収集方法は、データタイプによってあらかじめ決まっている」に続く表（`notation:258-270`）が「**単一**」方式の選び方を `notation:266`「データタイプが合致する（Excel 形式ではデータタイプ名の前方一致）データブロックのうち、ID が完全一致する最初の1件を取得」と定め、その「該当データタイプ」欄（`notation:267`）に `` `LIST_MAP` ``・`` `MESSAGE` `` を挙げている。**この欄は「など」で閉じており列挙は網羅ではないが、`MESSAGE` が「単一」方式であることはこの明示で足りる。** |
| 3 | `read` | **122** | `if (block != null)` の **false 側** | **テストを足すべき** | `readMessageBlock` が `null` を返す経路（#4 と同じ 1 本のシナリオ）。ソースの注記が「本体の `MessageParser` が空結果を返したとき `adapter.readMessage` は `null` を返す（**正常系**）」と明記している（`XlsFormatReader.java:231-232`。`:230` は `if (message == null) {` そのもの） |
| 4 | `readMessageBlock` | **230** | `if (message == null)` の **true 側** | **テストを足すべき** | 同上。未到達行 `L233 (return null)` と整合（`mi=2 ci=0`） |
| 5 | `read` | **126** | `else if (XlsDataTypeUtil.isSendSyncType(type))` の **false 側** | テスト不要（型の全数分岐） | `DataType` は 14 値（`nablarch-testing` `c5f3340` `DataType.java` の定数 14 個）。`DEFAULT` は `TestCoreReaderAdapter.HeaderCollector.parse` が `if (type == DataType.DEFAULT) {` / `    continue;` / `}`（`TestCoreReaderAdapter.java:362-364`。実物は 3 行）で `BlockHeader` にしない。残る 13 値を `isTableType`（3）・`isFileType`（4）・`LIST_MAP`（1）・`MESSAGE`（1）・`isSendSyncType`（4）が**重複なく全数**分岐する（`3+4+1+1+4 = 13`）。**したがって「`HeaderCollector` が `DEFAULT` を落とすため、本経路では生じない」。** ただし 14 値目を排除しているのは型システムではなく実行時の `continue` 1 行であり、#27・#30 の sealed 根拠のようにコンパイラが強制するものではない（§6-5 に開示） |
| 6 | `readListMapBlock` | **188** | `value == null ? null : stripQuotes(value)` の **null 側（true 側）** | テスト不要（実 `.xlsx` では生じない／Fake から到達可） | `value` は `mapRow.get(column)`。(a) セル値そのものが `null` になり得ない——`PoiXlsReader.java:123`（`nablarch-testing` `c5f3340`）が `String cellValue = cell == null ? "" : cell.toString();` で必ず非 `null` を入れる。(b) キーが欠けることもない——`HeaderLine.getMapExcludingMarkerColumns`（同 `HeaderLine.java:59-67`）が `effectiveColumnNames` の全要素をキーに `put` し、値は `excludeMarkerColumns`（同 `:75-85`）が `(i >= line.size()) ? "" : line.get(i)` で必ず埋める。`column` は `readListMapColumnNames`（`TestCoreReaderAdapter.java:121-130`）が返す `effectiveColumnNames` の部分列。**（`:157` と三項演算子の構文が同じだから #22 と同じ分類にすべき、という指摘は 3 巡目レビューで出たが採らない——`:157` の値は `table.getValue(r, column)`、本行の値は `mapRow.get(column)` で出どころが別であり、構文の一致は `null` の生じ方が同じであることの根拠にならない。本行の根拠は上の (a)(b) である。）** |
| 7 | `emptyToNull` | **328** | `recordType == null` の **true 側**（`recordType.isEmpty()` の両側は到達済み） | テスト不要（実 `.xlsx` では生じない／Fake から到達可） | 同じく `PoiXlsReader.java:123`。レコード種別セルは生行の先頭要素であり、空セルは `""` であって `null` にならない。`isEmpty()` 側は `XlsFormatReaderRealFileTest#readsOmittedRecordTypeAsNullFromRealBook` が実 `.xlsx` で担保している |
| 8 | `skipToFirstNameRow` | **340** | `idx < bodyLines.size()` の **false 側** | テスト不要（内部整合性ガード） | 名前行が生行に見つからないまま走査が尽きる状態。直後に `verifyNameRow` が必ず `IllegalStateException` を投げるため（#9）、到達＝二経路読み込みの実装バグ。ソースが「内部整合性ガード。断片構造と生行の対応が壊れていれば二経路読み込みロジックのバグ」と明記（`XlsFormatReader.java:360`） |
| 9 | `verifyNameRow` | **361** | `idx >= bodyLines.size()` の **true 側** | テスト不要（内部整合性ガード） | 同上（`mb=2`。未到達行 `L362` の `throw` と整合） |
| 10 | `verifyNameRow` | **361** | `!tail(bodyLines.get(idx)).equals(names)` の **true 側** | テスト不要（内部整合性ガード） | 同上 |
| 11 | `requireLine` | **446** | `idx >= bodyLines.size()` の **true 側** | テスト不要（内部整合性ガード） | ソースが同じ文言で内部整合性ガードと明記（`XlsFormatReader.java:445`）。未到達行 `L447` の `throw`（`mi=8 ci=0`）と整合 |
| 12 | `readFieldDefs` | **396** | `i < originalTypes.size()` の **false 側**（`type` に `null` を入れる側） | テスト不要（本体パーサが先に弾く） | フィールド名称行より型行が短い状態。**この `null` フォールバックへ届く前に、本体パーサが器の組み立てに失敗する。** 証人テストは `XlsFormatReaderInvalidInputTest#failsWhenTypeRowIsShorterThanNameRowInRealBook`（型行が短い。Javadoc が「そこへ届く前に本体パーサが器の組み立てに失敗する」と書いている）と `#failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook`（中間位置の空セル。要素数は揃うので別機構で弾かれる）の 2 件。機構の全数と例外メッセージは `issues.md` の C-20（`FieldDef.type` 省略）の行にある——「器が成立する入力では型が常に全フィールドぶん揃うため、`XlsFormatReader#readFieldDefs` …の `null` フォールバックには到達しない」（`…` は同行が書いている記録日時点の行番号を省いた）。**さらに `FieldDef` のコンストラクタが `type == null` を `IllegalArgumentException` で拒否する**（`FieldDef.java:83-84`）ため二重に塞がれている。記法の側でも `notation:883` が固定長で「フィールド名称・データ型・フィールド長の3リストが同サイズで必須」・可変長で「フィールド名称・データ型の2リストが同サイズで必須」と定め、`notation:889` が「フィールド名称・データ型・フィールド長リストのサイズが一致していない」を記述時のエラーに挙げているが、**これは補強材料であって到達不能の根拠ではない**（記法の禁止は「書いても届かない」を示さない） |
| 13 | `readFieldDefs` | **397** | `i < originalLengths.size()` の **false 側**（`originalLengths != null` の両側は到達済み） | テスト不要（本体パーサが先に弾く） | 同上（固定長でフィールド長行だけが短い状態）。証人テストは `XlsFormatReaderInvalidInputTest#failsWhenLengthRowIsShorterThanNameRowInRealBook` で、Javadoc が「型行・長さ行の不一致は本体パーサが弾く」という非対称を固定すると書いている。`issues.md` C-20 の行と `notation:883` ／ `notation:889` は #12 と同じ |
| 14 | `isQuotationWrapped` | **507** | `value == null` の **true 側** | テスト不要（実 `.xlsx` では生じない／Fake から到達可） | 唯一の呼び出し元は `normalizeDirectiveValue`（`XlsFormatReader.java:493`）であり、`value` はディレクティブ行のセル値。`PoiXlsReader.java:123` により `null` にならない |
| 15 | `isQuotationWrapped` | **507** | `value.length() <= 2` の **true 側** | **テストを足すべき** | 2 文字以下のディレクティブ値。ソース自身が「デフォルトディレクティブとして本体器に注入される `"` 1 文字（可変長の `quoting-delimiter` 既定値）等は記法ではなく生値であり…ここでも素通しする」（`XlsFormatReader.java:490-492`）と、**通ることを前提にした挙動**を書いているのに一度も通っていない。未到達行 `L508 (return false)` と整合 |
| 16 | `isQuotationWrapped` | **510** | `value.endsWith("\"")` の **false 側** | **テストを足すべき** | 半角 `"` で始まるが `"` で終わらない値（例 `"abc`）。`notation:1325`「半角または全角ダブルクォートで**前後が囲まれた場合のみ**、外側1層を除去する」の「のみ」を守っていることを示す分岐であり、到達不能ではない。どちらの側が未到達か（`L510` は `mb=1 cb=3`。到達済み 3 件の内訳）は §2 で確定した |
| 17 | `isQuotationWrapped` | **511** | `value.startsWith("”")` の **true 側** | **テストを足すべき** | 全角 `”` の記法。`notation:1325`「**半角または全角**ダブルクォートで前後が囲まれた場合のみ」／`notation:1397`「前後のダブルクォート（**全角・半角問わない**）を除いた文字列として扱う」。`mb=3 cb=1` で到達済みは `startsWith("”")` の false 側だけ |
| 18 | `isQuotationWrapped` | **511** | `value.endsWith("”")` の **true 側** | **テストを足すべき** | 同上 |
| 19 | `isQuotationWrapped` | **511** | `value.endsWith("”")` の **false 側** | **テストを足すべき** | 同上（`”` で始まり `”` で終わらない値） |
| 20 | `tail` | **522** | `list.isEmpty()` の **true 側** | **テストを足すべき** | 生行が空リストになる場合。`TestCoreReaderAdapter.BodyLineCollector.parse`（`:464`）が `NablarchTestUtils.trimTailCopy(line)` を通すため、**全セルが空の行は空リストになる**（`nablarch-testing` `c5f3340` `NablarchTestUtils.java:251-263` の `trimTail` が末尾の空要素を全部落とす）。`notation:883` は「全フィールドを省略した行（Excel形式では先頭セルが空の行…）」を有効なレコードとして認めており、この形は仕様外ではない |
| 21 | `stripQuotes` | **541** | `if (value == null)` の **true 側** | テスト不要（実 `.xlsx` では生じない／Fake から到達可） | 呼び出し元は 4 箇所（`grep -n 'stripQuotes(' src/main/java/nablarch/test/tool/converter/xls/XlsFormatReader.java` → `:157` `:188` `:425` `:494`。`:539` は定義行）。`:157` と `:188` は三項演算子で `null` を除外済み、`:425` は `i < valueCells.size() ? valueCells.get(i) : ""` の結果で、生行の要素は実 `.xlsx` では `PoiXlsReader.java:123` により非 `null`、`:494` は直前の `isQuotationWrapped` が `null` に対して `false` を返す（`:507`）ため、いずれも `null` を渡さない。**ソースの `:540` のコメントが「…`null` を返すため、このガードは必須。」と読める書き方をしているが、実測はガードが一度も通らないことを示す**（既知課題 `issues.md` XLS-09）。未到達行 `L542` と整合 |
| 22 | `isEmptyEntry` | **585** | `value != null` の **false 側**（`!value.isEmpty()` の両側は到達済み） | **テストを足すべき** | **記法の明文が挙動を定めており、スイート自身が既に `null` セルを注入している。** (a) `notation:1535`「全要素が **null** または空文字のエントリは読み飛ばされる。」——`null` を名指ししている。(b) `XlsFormatReaderTest#readMapsTableBlockPreservingRawValues` が `XlsFormatReaderTest.java:141` で `lines.add(row("literal", null));` を置き、同 `:153` の `// null セルは null のまま（空文字と区別）` と同 `:155` の `assertThat(table.getRows().get(1).get(1), is(nullValue()));` で挙動を固定している。これが理由で `XlsFormatReader.java:157`（`value == null ? null : stripQuotes(...)`）は `mb=0 cb=2` で両側到達済みである。(c) 到達させる入力は `row(null, "x")` のように**先頭要素が `null` の行**を 1 行足すだけでよい（既存の `row("literal", null)` は先頭要素が非空なので `return false` して 2 要素目を見ない）。呼び出し元は `dropEmptyEntries`（`:566-574`）の 2 箇所（`:162` テーブル系／`:193` LIST_MAP）。どちらの側が未到達かは §2 の実在テスト 2 件で確定した |
| 23 | `deduplicateColumnNames` | **617** | `warned.add(name)` の **false 側** | **テストを足すべき** | 同一のカラム名が **3 回以上**現れたとき（2 回目の重複検出で WARN を重複出力しない）。カラム名の重複そのものは既存テストで到達済み（`cb=1`）であり、3 回以上にするだけで到達する |
| 24 | `bookName` | **694** | `slash < 0` の **true 側** | **テストを足すべき** | `read` は `public`（宣言は `XlsFormatReader.java:101`）であり、`'/'` を含まない `resourceName` を渡せば到達する。**Javadoc（`XlsFormatReader.java:690`）が `@return ブック名（{@code '/'} が無ければリソース名全体）` と振る舞いを明記しており、明文化済みで一度も通っていない。** 本番の唯一の呼び出し元 `XlsFormatHandler.java:46` が `bookName + "/" + sheetName` を渡すことは、到達不能の根拠にはならない（`issues.md` XLS-20 の【判定の訂正】が「到達経路が無い」を根拠にした判定を退けている） |
| 25 | `sheetName` | **705** | `slash < 0` の **true 側** | **テストを足すべき** | 同上（Javadoc は `XlsFormatReader.java:701` の `@return シート名（{@code '/'} が無ければリソース名全体）`） |

### 3.2 辺③ `XlsFormatWriter`（3 件）

| # | メソッド | 行（`da66425` 時点） | 未到達の側 | 分類 | 根拠 |
|---|---|---|---|---|---|
| 26 | `write` | **107** | `if (parent != null)` の **false 側** | **テストを足すべき** | `basePath` が空文字列などで親ディレクトリを持たない相対パス（例 `foo.xlsx`）が生成される場合。**ソースのコメント自身が「…`getParent()` は `null` を返すため、null チェックが必須」と書いている**（`XlsFormatWriter.java:106`）のに一度も通っていない。`write` は `public` であり `basePath` に空文字列を渡せば到達する。`inventory.md` §3.1-2 が既に「担保の穴」として開示している |
| 27 | `layout` | **202** | `else if (block instanceof MessageDataBlock)` の **false 側** | テスト不要（型の全数分岐） | false 側は直後の `throw new IllegalArgumentException("unsupported block: " …)`（未到達行 `L206`）に落ちる。`TestDataBlock` は `sealed` で（`TestDataBlock.java:86-87`：`public abstract sealed class TestDataBlock permits ColumnRowDataBlock, FileDataBlock, MessageDataBlock`）、`ColumnRowDataBlock` も `sealed`（`ColumnRowDataBlock.java:67-68`：`public abstract sealed class ColumnRowDataBlock extends TestDataBlock permits TableDataBlock, ListMapBlock`）、末端の 4 クラスはいずれも `final` である。**具象サブクラスは `TableDataBlock` ／ `ListMapBlock` ／ `FileDataBlock` ／ `MessageDataBlock` の 4 つに閉じており**（`ColumnRowDataBlock` は `abstract`）、`instanceof` の連鎖がこれを全数分岐する。**`permits` を書き換えない限り、コンパイル単位の外からサブクラスを足すことはできない。** |
| 28 | `isMarkerColumn` | **557** | `columnName != null` の **false 側**（`startsWith("[")` ／ `endsWith("]")` の両側は到達済み） | テスト不要（中間モデルの不変条件） | カラム名 `null` は `ColumnRowDataBlock` の生成時に `ModelPreconditions` が拒否する（方針は `steering.md` Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」・`issues.md` XLS-38）。**書き出し側に番人を置かない方針の裏返しとしてこの `null` ガードだけが残っている。** `endsWith("]")` の false 側が到達済みであることは `XlsFormatWriterTest.java:874`（カラム名 `"[half"`）で確認した |

### 3.3 辺④ `YamlFormatWriter`（4 件）

| # | メソッド | 行（`da66425` 時点） | 未到達の側 | 分類 | 根拠 |
|---|---|---|---|---|---|
| 29 | `write` | **84** | `if (parent != null)` の **false 側** | **テストを足すべき** | #26 と同型（`YamlFormatWriter.java:83` に**同型の**コメントがある。例示ファイル名だけが違い、`XlsFormatWriter.java:106` は `"foo.xlsx"`、こちらは `"foo.yaml"`） |
| 30 | `emitBlock` | **139** | `else if (block instanceof MessageDataBlock)` の **false 側** | テスト不要（型の全数分岐） | #27 と同型（sealed 階層の全数分岐）。未到達行 `L143` の `throw` と整合 |
| 31 | `rawGroup` | **484** | `groupId.charAt(last) == ']'` の **false 側**（`groupId.charAt(0) == '['` の両側は到達済み） | **テストを足すべき** | `[` で始まるが `]` で終わらないグループ ID（例 `[abc`）。**辺①から作れる** —— `TestCoreReaderAdapter#markerGroupId`（`:282-286`）はマーカー先頭セルのうちデータタイプ名の直後から `=` までをそのまま切り出すため
（`firstCell.substring(type.getName().length())` の結果から `indexOf('=')` の手前までを取る。**この 1 文は原典の引用ではなく実装の要約である**）、`SETUP_TABLE[abc=T` と書けば中間モデルの `groupId` は `"[abc"` になる。中間モデルの契約も `groupId` に `null` を禁じるだけで（`TestDataBlock.java:17`・`:121`）、`[ ]` で囲まれた形であることは要求していない。`charAt(0) == '['` の false 側は `YamlFormatWriterTest#serialize_unbracketedGroupId_isUsedAsRawValue`（グループ ID `"raw"`）が担保。**分類の経緯（初版の「記法外」根拠を退けた理由）・YAML スキーマの確認・XLS-39 との切り分けは → `issues.md` COV-13** |
| 32 | `sectionKey` | **503** | `switch (type)` の **`default` 側** | テスト不要（中間モデルの不変条件） | `default` は `throw new IllegalArgumentException("unsupported DataType: " …)`（未到達行 `L520`）。`case` が網羅していないのは `DataType.DEFAULT` だけで、その値は `TestDataBlock` が生成時に拒否する（`issues.md` XLS-20）。`mb=1 cb=11` は、13 個の `case` ラベルのうち `SETUP_FIXED`／`SETUP_VARIABLE` と `EXPECTED_FIXED`／`EXPECTED_VARIABLE` がそれぞれ同じ飛び先を共有するため、相異なる飛び先が 11 になり、`default` を加えて分岐が 12 件になるためである |

### 3.4 区分⑤ `TestCoreReaderAdapter.BodyLineCollector`（2 件）

| # | メソッド | 行（`da66425` 時点） | 未到達の側 | 分類 | 根拠 |
|---|---|---|---|---|---|
| 33 | `parse` | **454** | `if (groupId != null)` の **false 側** | **テストを足すべき** | 先頭セルがデータタイプ名で始まるのに `=` を含まない行（`markerGroupId` が `null` を返す。`TestCoreReaderAdapter.java:282-286`）。**同型のガードは `HeaderCollector.parse`（`:366-369`）では両側とも到達済み**であり、記法として書けない形ではない。`BodyLineCollector` 側の固定が抜けている |
| 34 | `parse` | **458** | `groupId.equals(targetGroupId)` の **false 側** | **テストを足すべき** | 同一シートに同じデータタイプで**異なるグループ ID** のブロックがある場合。`notation:252`「同じ読み込み単位の中に、同じデータタイプのデータブロックを複数記述したい場合は、グループIDでそれらを区別する」・`notation:254`「デフォルトグループと個別グループのデータは併用でき、両方が混在した場合は両方のデータが有効になる」により、**記法として正当な入力である**。**テストを足すときの版面の制約（Excel 形式ではグループごとにまとめて記述しなければならない。`notation:306`）は → `issues.md` COV-11** |

## 4. 未到達行 16 件の一覧

**16 行のうち 8 行は §3 のいずれかの分岐に紐づく** —— その分岐の未到達側でだけ実行される行であり、
下表の「§3 の対応」欄に相手の `#` を示した。
**残る 8 行（`TestCoreReaderAdapter.java`）は分岐を持たない未到達行**であり、§3 のどの分岐にも紐づかない
（理由は表の下）。**`throw` ／ `return` という行の形は両者を分けない** —— 紐づかない 8 行のうち 4 行
（`TestCoreReaderAdapter.java:389`・`394`・`483`・`488`）も `return false;` である。

| ファイル | 行 | 内容 | §3 の対応 |
|---|---|---|---|
| `XlsFormatReader.java` | 233 | `return null;`（MESSAGE 空ブロック） | #4 |
| `XlsFormatReader.java` | 362 | `throw new IllegalStateException(...)`（`verifyNameRow`） | #9・#10 |
| `XlsFormatReader.java` | 447 | `throw new IllegalStateException(...)`（`requireLine`） | #11 |
| `XlsFormatReader.java` | 508 | `return false;`（`isQuotationWrapped`） | #14・#15 |
| `XlsFormatReader.java` | 542 | `return null;`（`stripQuotes`） | #21 |
| `XlsFormatWriter.java` | 206 | `throw new IllegalArgumentException("unsupported block: " …)` | #27 |
| `YamlFormatWriter.java` | 143 | `throw new IllegalArgumentException("unsupported block: " …)` | #30 |
| `YamlFormatWriter.java` | 520 | `throw new IllegalArgumentException("unsupported DataType: " …)` | #32 |
| `TestCoreReaderAdapter.java` | 380 | `HeaderCollector#onReadLine` の本体（空） | —（分岐なし） |
| `TestCoreReaderAdapter.java` | 385 | `HeaderCollector#onTargetTypeFound` の本体（空） | —（分岐なし） |
| `TestCoreReaderAdapter.java` | 389 | `HeaderCollector#isTargetType` の `return false;` | —（分岐なし） |
| `TestCoreReaderAdapter.java` | 394 | `HeaderCollector#shouldStopOnNextOne` の `return false;` | —（分岐なし） |
| `TestCoreReaderAdapter.java` | 474 | `BodyLineCollector#onReadLine` の本体（空） | —（分岐なし） |
| `TestCoreReaderAdapter.java` | 479 | `BodyLineCollector#onTargetTypeFound` の本体（空） | —（分岐なし） |
| `TestCoreReaderAdapter.java` | 483 | `BodyLineCollector#isTargetType` の `return false;` | —（分岐なし） |
| `TestCoreReaderAdapter.java` | 488 | `BodyLineCollector#shouldStopOnNextOne` の `return false;` | —（分岐なし） |

**`TestCoreReaderAdapter` の 8 行は「テストで到達させられない」。**
**これは §3.0 の 5 値の外にある別の語である**（5 値は §3 の分岐 34 件だけに使う。
分岐ではなく行であり、理由も「コンパイラが要求する到達不能な実装」で 5 値のどれにも当たらない）。
`HeaderCollector` ／ `BodyLineCollector` は
いずれも `TestDataParsingTemplate#parse(String id)` を上書きしており、
テンプレートメソッド `doParse()` からこの 4 つの抽象メソッドが呼ばれる経路が無い。
それでも実装が必須なのは `TestDataParsingTemplate` が `abstract` で宣言しているためで、
**コンパイラが要求する到達不能な実装**である（ソースにも
`// parse(String id) を上書きしているため doParse() からこれらの抽象メソッドは呼ばれない。`
`// TestDataParsingTemplate の契約上、実装は必須。` と明記されている。
`TestCoreReaderAdapter.java:375-376` ／ `:469-470`）。

## 5. 「テストを足すべき」19 件の処理

**19 件すべてを `issues.md` §7 へ残課題として記録した**（14 件の課題 `COV-01`〜`COV-14`。
1 課題が複数分岐にまたがるため件数は一致しない）。**テストは足していない。**

**`COV-nn` ↔ 本書 §3 の `#` の対応表は `issues.md` §7 にある**（同じ関係を 2 方向に手書きしないため、
本書には置かない。`steering.md` Rules「同じ関係を 2 方向に手書きしない」）。

**足さずに記録を選んだ理由。** タスク #26 の目的は「計測と未到達分岐の列挙・根拠付け」であり、
テストの追加は後続タスク（#27 の軸マトリクス作成、#28）の範囲である。ここでテストを足すと、
軸マトリクスができる前に軸に紐づかないテストが増え、#27 の突き合わせ対象が動いてしまう。
完了条件も「追加されたか `issues.md` へ残課題として記録されたかのいずれか」と明示的に記録を認めている。

## 6. 開示：担保の穴・台帳の訂正・確かめきれなかったこと

**6 節あり、性質で数えると 7 件ある。**
**この 6 節のうち §6-3 だけは 1 節の中に性質の違う 2 件を含むため、表では (a)(b) に分けた**
（性質が違うものを 1 つの件数にまとめない）。

| # | 内容 | 性質 |
|---|---|---|
| §6-1 | 「実 `.xlsx` では生じない／Fake から到達可」4 件は Fake リーダ経由で到達可能 | **担保の穴** |
| §6-2 | 対象 6 区分の外に未分類の未到達が分岐 16 件・行 63 件 | **担保の穴** |
| §6-3(a) | `inventory.md` から他ファイルの行番号を落とし、古い JaCoCo 数値に計測日を付けた | 処置済みの台帳訂正（陳腐化した記述の手当て。論旨は変えていない。穴ではない） |
| §6-3(b) | `inventory.md` の 1 文が `DirectiveUtil` の未到達**行**について実測と反対のことを述べていた | **処置済みの事実誤りの訂正**（穴ではない） |
| §6-4 | `-o` なしでの再現は未確認 | 確かめきれなかったこと |
| §6-5 | 「テスト不要」5 件（#5・#8〜#11）の根拠が外部の契約でなく `src/main` 自身のコメント・実行時の 1 行である | **担保の穴** |
| §6-6 | `@Ignore` 2 件（YML-14・XLS-40）があるべき姿を書いたまま無効化されている | **担保の穴** |

1. **§6-1 「実 `.xlsx` では生じない／Fake から到達可」と分類した 4 件（#6・#7・#14・#21）は、
   converter のテスト用シームからは通常の呼び出しで到達できる。**
   根拠の根はいずれも `nablarch-testing` の `PoiXlsReader.java:123`
   （`String cellValue = cell == null ? "" : cell.toString();`）——**別リポジトリの実装の挙動**であって、
   converter 自身の型でも Javadoc 契約でもない。converter には
   パッケージプライベートな `XlsFormatReader(TestCoreReaderAdapter adapter)`（`XlsFormatReader.java:89`）と
   `FakeTestDataReader implements TestDataReader`（`XlsFormatReaderTest.java:54`）があり、
   canned 行の要素に Java の `null` を直接置けば（リフレクション不要で）到達する。
   **現に到達している** —— `XlsFormatReaderTest.java:141` の `lines.add(row("literal", null));` により、
   テーブル経路の `XlsFormatReader.java:157`（`value == null ? null : stripQuotes(...)`）は
   `mb=0 cb=2` で**両側とも到達済み**である。
   `issues.md` XLS-09 も同じことを書いている——「残る到達経路は、Fake リーダ
   （`XlsFormatReaderTest` の `FakeTestDataReader`）が canned 行の要素に
   Java の `null` を直接置いた場合の `toRecordLayouts` 経路だけであり、**現行スイートにその入力は無い**」。
   **つまり「到達不能」ではなく「実 `.xlsx` からは生じない・Fake リーダからは到達可能」が実態である。**
   分類は「テスト不要」のままにした（実 `.xlsx` の入力では固定できる挙動が無いため）が、
   **これは担保の穴である。**

2. **§6-2 対象 6 区分の外に、分類していない未到達が分岐 16 件・行 63 件残っている。**
   §1 の `awk` の条件を `!( ... )` で反転して導く。

   ```sh
   awk -F, 'NR>1 && !($3 ~ /^(XlsFormatReader|XlsFormatWriter|YamlFormatReader|YamlFormatWriter|TestCoreReaderAdapter)/ \
     || $2 ~ /converter\.model$/) { ml+=$8; lc+=$9; mb+=$6; bc+=$7; n++ } \
     END { printf "classes=%d line %d/%d branch %d/%d missed_line=%d missed_branch=%d\n", \
           n, lc, ml+lc, bc, mb+bc, ml, mb }' target/site/jacoco/jacoco.csv
   # → classes=29 line 638/701 branch 237/253 missed_line=63 missed_branch=16
   ```

   **内訳は 14 クラスで、分岐 16 件・行 63 件がすべてここに収まる**（除外条件は付けていない。
   未到達を 1 つでも持つクラスを全件出す）。

   ```sh
   awk -F, 'NR>1 && !($3 ~ /^(XlsFormatReader|XlsFormatWriter|YamlFormatReader|YamlFormatWriter|TestCoreReaderAdapter)/ \
     || $2 ~ /converter\.model$/) && ($6 > 0 || $8 > 0) \
     { printf "%-22s missed_line=%-3d missed_branch=%d\n", $3, $8, $6; ml+=$8; mb+=$6; n++ } \
     END { printf "%-22s missed_line=%-3d missed_branch=%d\n", "計 " n " クラス", ml, mb }' \
     target/site/jacoco/jacoco.csv
   ```

   ```
   FragmentView           missed_line=0   missed_branch=1
   ExcelFormatConfig      missed_line=5   missed_branch=0
   Styles                 missed_line=1   missed_branch=1
   YamlTestDataValidator  missed_line=5   missed_branch=1
   DirectiveUtil          missed_line=0   missed_branch=1
   YamlFormatHandler      missed_line=2   missed_branch=0
   ConverterPathResolver  missed_line=2   missed_branch=0
   XlsFormatHandler       missed_line=0   missed_branch=1
   XlsOutputConfig        missed_line=25  missed_branch=8
   ConverterFileFilter    missed_line=9   missed_branch=1
   TestDataConverter      missed_line=2   missed_branch=0
   DataFormat             missed_line=1   missed_branch=1
   YamlTestCoreAdapter    missed_line=1   missed_branch=1
   StubDbInfo             missed_line=10  missed_branch=0
   計 14 クラス               missed_line=63  missed_branch=16
   ```

   **最大の穴は `XlsOutputConfig` で、16 分岐のうち 8 件・63 行のうち 25 行を 1 クラスで占める。**
   未到達分岐 8 件はすべて `toExcelFormatConfig()` の
   `if (xxxColor != null) { config = config.withXxx(resolveColor(xxxColor)); }` の **true 側**であり
   （`jacoco.xml` の `XlsOutputConfig.java` で `mb>0` の行は `L134`・`L140`・`L143`・`L146`・`L149`・
   `L155`・`L158`・`L161` の 8 行。いずれも `mb=1 cb=1`）、
   **Excel 出力の書式オプション（各種の背景色・列幅自動調整など）が既定値以外で一度も指定されていない**
   ことを意味する。`ExcelFormatConfig` の未到達行 5 件（`L143`・`L169`・`L182`・`L221`・`L260`。
   いずれも `mi=25 ci=0`）はその裏返しで、対応する `withXxx` が一度も呼ばれていない。
   `XlsOutputConfig` は Maven プラグインのパラメータ受け口であって辺③の変換ルールそのものではないが、
   **辺③の出力書式を決める設定であり、「対象 6 区分の外だから」で落としてよいものではない。**

   **#26 は対象 6 区分だけを分類対象としたため、この 16 分岐・63 行は「テストを足すべき／テスト不要」の
   どちらにも入っていない。#27 の軸マトリクスもクラス単位ではなく軸要素単位で作るため、
   ここを拾うことは保証されていない。** 拾うかどうかは #27 で決める必要がある
   （**少なくとも `XlsOutputConfig` の書式オプションは明示的に扱いを決めること**）。

3. **§6-3 `inventory.md` の訂正。この節は性質の違う 2 つを含む** —— **(a)** 陳腐化した記述の手当て
   （他ファイルの行番号を落とす・`L` 形式の反例を落とす・古い JaCoCo 数値に計測日を付ける）と、
   **(b)** 事実誤りの訂正（`DirectiveUtil` について台帳の 1 文が実測と反対のことを述べていた）である。
   **(b) の範囲は、下で `(b)` と印を付けたところから「ここまでが (b)」までである。
   それ以外はすべて (a) である。**

   同文書の辺③④の JaCoCo 実測（`XlsFormatWriter` `line 157/158 branch 101/104`、
   `YamlFormatWriter` `line 157/159 branch 86/90`）は**本計測と一致している**が、併記されていた未到達の
   行番号はコミット `54d2057`（Javadoc・コメントのみの変更）による行ずれで古くなっていた。
   **(b) さらに `DirectiveUtil` について、台帳の 1 文が事実誤りだった。**
   誤っていたのは括弧書きの「下記 `jacoco.xml` の走査で未到達行は L45 のみ」だけである
   （`9cc42b2` 時点の `inventory.md`。`git show 9cc42b2:.rn/ntf-test-data-converter/coverage/inventory.md | grep -n '未到達行は L45 のみ'` → `721:` の行）。
   実測は `toStringDirectives` の三項演算子 1 行が**部分実行**（`mb=1 cb=1`・`ci=9`。行としては到達済み）
   であり、`DirectiveUtil` の未到達行は 0 件である（`line 20/20`）。
   **一方、その 9 行前の段落「未到達は `DirectiveUtil.java:45` の `value == null ? null : valueMapper.map(...)` の
   `null` 側 1 分岐である。」は正しい**（同 `:712-713`。**分岐**について述べており、行については述べていない）。
   **台帳の誤りは `:721` の括弧書き 1 文に閉じる。**（ここまでが (b)。以降は (a) に戻る。）
   `steering.md` Rules「台帳（`coverage/inventory.md`）に『他ファイルの行番号』…を書かない…。
   **行番号とファイル行数は**他ファイルを編集するたびに移動し、台帳を直すと台帳の別箇所が自己無効化する。
   識別はクラス名・メソッド名で行う」に従い、**行番号を書き直すのではなく落とし、
   識別をクラス名・メソッド名に変えて、行番号つきの一覧は本書 §3 を指すポインタにした。**
   ただし `DirectiveUtil` は対象 6 区分の外で §3 の表に載らないため、ポインタを本節（§6-3）と §6-2 へ張り直し、
   台帳側には「**行としては到達済み**——`mb=1 cb=1`・`ci=9` の部分実行であって未到達行ではない」
   だけを残した（3 巡目レビュー指摘）。

   **さらに `L155` 形式（`.java:` を伴わない行番号）の反例が 1 件残っていた**（3 巡目レビュー指摘）——
   `XlsFormatWriter#requireValidSheetNameLength` に添えられた行番号で、凍結ブロックの中ではなく
   台帳自身の地の文だった。これを落とし、**確認コマンドの正規表現を `L` 形式も拾うよう広げた。**

   **あわせて、台帳が現在形で書いていた辺②の JaCoCo 数値 2 つに計測日を付けた。**
   その 2 つ（`行 200/200・分岐 106/106` と `行 201/201・分岐 108/108`）は本計測の
   `line 192/192 branch 102/102` と食い違い、しかも互いに違っていた。
   **どちらも計測日を明記した過去形へ改め、現行値を併記した**（**未到達 0 件＝100% という性質は
   3 つとも同じであり、台帳の論旨は変えていない**）。
   **処置はこの 2 か所に閉じており、台帳本文の JaCoCo 数値が陳腐化していないかを継続的に検査する手段は
   用意していない（未実施）。** 行番号は下の 2 本のコマンドが書式だけで拾えるが、数値の側に同等のものは無い——
   日付つきの過去形でも数値は数値であり、書式からは陳腐化を判定できないためである。

   **処置後の状態は次のコマンドで確かめられる**（件数を手で書かない）。
   **`inventory.md` は #26 のあとも凍結出力ブロックが追記されており、そのたびに両方の件数が動く。
   ここで確かめたいのは #26 の処置後の状態なので、`git show` で #26 の完了コミット `58bae09` に固定した。**

   ```sh
   B=58bae09:.rn/ntf-test-data-converter/coverage/inventory.md
   git show $B | grep -cE '\.java:[0-9]+|\bL[0-9]{2,4}\b'                                    # → 8
   git show $B | grep -E '\.java:[0-9]+|\bL[0-9]{2,4}\b' | grep -cE '^(② |   )src/test/java/'  # → 8
   ```

   **2 つが一致するので、残る 8 件はすべて `grep -rn '^    @Ignore' src/test --include=*.java` の
   出力を記録日のまま凍結したブロックの中にあり、台帳自身が主張している行番号は 0 件である。**
   凍結出力を書き直さない理由（書き直すと「記録した日にそのまま実行して得た出力である」という記録の
   性質が壊れる）は `inventory.md` §0.1-2 に明記した。

4. **§6-4 `-o`（オフライン）なしでの再現は未確認**（§0 の注記のとおり）。

5. **§6-5 「テスト不要」5 件（#5・#8・#9・#10・#11）の根拠は、外部の契約ではなく
   `src/main` 自身のコメント 1 つ・実行時の 1 行であり、§6-1 と同じ Fake リーダのシームから到達できる。**

   **(a) 「内部整合性ガード」4 件（#8・#9・#10・#11）。**
   他の 4 つの理由（型の全数分岐／中間モデルの不変条件／実 `.xlsx` では生じない／本体パーサが先に弾く）は
   sealed 宣言・`ModelPreconditions`・`PoiXlsReader` の実装・本体パーサの例外という**外部の根拠**を持つのに対し、
   この 4 件が持つのは `XlsFormatReader.java:360` ／ 同 `:445` の
   「内部整合性ガード。断片構造と生行の対応が壊れていれば二経路読み込みロジックのバグ」という
   **自分自身のコメントだけ**である。しかもそのコメントが言う「二経路読み込み」は、
   **同じシートを「器（`FileView` の断片構造）」と「生行」の 2 回に分けて読むこと**を指しており、
   `toRecordLayouts(view, bodyLines, ...)` を呼ぶ 3 箇所すべてがこの形である ——
   `readFileBlocks`（`XlsFormatReader.java:210` の `TestCoreFileAdapter.read(file)` と
   同 `:212` の `adapter.readBlockBodyLines(...)`）／`readMessageBlock`（同 `:236` と `:239-241`）／
   `readSendSyncBlocks`（同 `:272` と `:273-274`）。
   `readBlockBodyLines` の実体は `TestCoreReaderAdapter.java:264` で、
   いずれも `reader`（`TestDataReader`）を通してシートを読み直す。
   この読み直しは §6-1 が開示したのと同じ `FakeTestDataReader`（`XlsFormatReaderTest.java:54`）の
   シームを通る（`XlsFormatReaderTest.java:121-124` の `readerOf(...)` が
   `new XlsFormatReader(new TestCoreReaderAdapter(fake))` で差し込んでいる）。
   **ただし「器側と生行側を食い違わせる canned 入力を実際に組めるか」は #26 では確かめていない**
   ——どちらの読み取りも同じ canned 行から出発するためである。
   **したがって言えるのは、この 4 件の「到達不能」が外部の契約（sealed 宣言・`ModelPreconditions`・
   `PoiXlsReader` の実装・本体パーサの例外）ではなく `src/main` 自身のコメント 1 つだけに
   支えられているということであり、他の 4 つの理由と根拠の強さが同じではない。**

   **(b) 「型の全数分岐」3 件のうち #5 だけは、根拠の強さが他の 2 件と違う**（3 巡目レビュー指摘）。
   #27・#30 の根拠は `sealed` ／ `permits` 宣言であり、**コンパイラが強制するので反証できない**。
   これに対し #5 は、`DataType` の 14 値目 `DEFAULT` を排除しているのが型システムではなく
   `TestCoreReaderAdapter.HeaderCollector.parse` の `continue` **1 行**（`TestCoreReaderAdapter.java:362-364`）である。
   しかも `TestCoreReaderAdapter` は `public class`（`final` でない。`:37`）、
   `readHeaders` は `public`（`final` でない。`:238`）、
   `BlockHeader` のコンストラクタはパッケージプライベートで検証を持たず（`BlockHeader.java:29-33`）、
   `XlsFormatReader` にはアダプタ注入コンストラクタがある（`XlsFormatReader.java:89`）——
   **上の (a) ／ §6-1 が開示しているのと同じシームから、`DEFAULT` を持つ `BlockHeader` を差し込める。**
   このため根拠の言い方を「この `else if` が false になる `type` は存在しない」から
   「`HeaderCollector` が `DEFAULT` を落とすため、本経路では生じない」へ改めた。

   **5 件とも分類は「テスト不要」のままにした**（到達＝実装のバグ／本経路では生じない、という
   位置づけは変わらないため）が、**根拠が自己申告・実行時の 1 行である以上これは担保の穴である。**

6. **§6-6 §0 の `@Ignore` 2 件（YML-14・XLS-40）は、あるべき姿を書いたまま無効化されている担保の穴である。**
   いずれも他責先（`nablarch-testing` の `DataFileFragment` ／ 同 `TableData`）の修正待ちで、
   #26 では動かせない。**「行・分岐 100%」の辺②に載っている点は §1 の注記のとおりである。**

## 7. 参考：6 区分の外のクラス（本タスクの対象外）

対象 6 区分に含まれないクラスの実測。**分類・根拠付けは行っていない**（タスクの対象外。§6-2 に開示）。
導出コマンドは §1 の `awk` の条件を `!( ... )` で反転したもの。

```sh
awk -F, 'NR>1 && !($3 ~ /^(XlsFormatReader|XlsFormatWriter|YamlFormatReader|YamlFormatWriter|TestCoreReaderAdapter)/ \
  || $2 ~ /converter\.model$/) { printf "%-40s %-30s line %3d/%-3d branch %3d/%-3d\n", $2, $3, $9, $8+$9, $7, $6+$7 }' \
  target/site/jacoco/jacoco.csv
```

**下表の行はパッケージ名・クラス名で整列してある**（コマンドの出力は `jacoco.csv` の記録順で、
並びだけが異なる。値は 1 行ずつ出力と一致する）。

| パッケージ | クラス | 行（到達/全体） | 分岐（到達/全体） |
|---|---|---|---|
| `nablarch.test.core.file` | `FileView` | 8/8 | 0/0 |
| `nablarch.test.core.file` | `FragmentView` | 10/10 | 3/4 |
| `nablarch.test.core.file` | `TestCoreFileAdapter` | 15/15 | 6/6 |
| `nablarch.test.core.reader` | `BlockHeader` | 8/8 | 0/0 |
| `nablarch.test.core.reader` | `MessageData` | 6/6 | 0/0 |
| `nablarch.test.core.reader` | `StubDbInfo` | 2/12 | 0/0 |
| `nablarch.test.core.reader` | `YamlTestCoreAdapter` | 49/50 | 18/19 |
| `nablarch.test.tool.converter` | `ConversionRequest` | 20/20 | 2/2 |
| `nablarch.test.tool.converter` | `ConversionRequest.Builder` | 39/39 | 8/8 |
| `nablarch.test.tool.converter` | `ConverterException` | 4/4 | 0/0 |
| `nablarch.test.tool.converter` | `ConverterFileFilter` | 62/71 | 29/30 |
| `nablarch.test.tool.converter` | `ConverterMojo` | 26/26 | 2/2 |
| `nablarch.test.tool.converter` | `ConverterPathResolver` | 9/11 | 6/6 |
| `nablarch.test.tool.converter` | `DataFormat` | 14/15 | 6/7 |
| `nablarch.test.tool.converter` | `DirectiveUtil` | 20/20 | 17/18 |
| `nablarch.test.tool.converter` | `TestDataConverter` | 25/27 | 8/8 |
| `nablarch.test.tool.converter` | `XlsFormatHandler` | 22/22 | 5/6 |
| `nablarch.test.tool.converter` | `XlsOutputConfig` | 25/50 | 14/22 |
| `nablarch.test.tool.converter` | `YamlFormatHandler` | 28/30 | 10/10 |
| `nablarch.test.tool.converter.xls` | `BlockLayout` | 26/26 | 9/9 |
| `nablarch.test.tool.converter.xls` | `ExcelFormatConfig` | 40/45 | 4/4 |
| `nablarch.test.tool.converter.xls` | `Fill` | 7/7 | 0/0 |
| `nablarch.test.tool.converter.xls` | `RowKind` | 5/5 | 0/0 |
| `nablarch.test.tool.converter.xls` | `Styles` | 39/40 | 29/30 |
| `nablarch.test.tool.converter.xls` | `WidthTracker` | 13/13 | 6/6 |
| `nablarch.test.tool.converter.xls` | `XlsDataTypeUtil` | 1/1 | 8/8 |
| `nablarch.test.tool.converter.yaml` | `ValidationError` | 9/9 | 0/0 |
| `nablarch.test.tool.converter.yaml` | `YamlSeq` | 15/15 | 2/2 |
| `nablarch.test.tool.converter.yaml` | `YamlTestDataValidator` | 91/96 | 45/46 |

（`jacoco.csv` のクラス行は全 51 行。うち 22 行が §1、29 行がこの表。`22 + 29 = 51`。
`awk -F, 'NR>1' target/site/jacoco/jacoco.csv | wc -l` → `51`）
