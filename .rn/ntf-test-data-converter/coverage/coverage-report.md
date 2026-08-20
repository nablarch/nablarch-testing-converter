# カバレッジ計測と未到達分岐の一覧（タスク #26）

4 辺の担当クラスの行・分岐カバレッジを JaCoCo で計測し、**未到達分岐 34 件・未到達行 16 件を 1 件ずつ**
列挙して、「テストを足すべき」／「テスト不要」に分類したもの。

## 0. 計測条件

| 項目 | 値 |
|---|---|
| 計測時点の HEAD SHA | `da6642578c366b73b3a001980142d9f741f82f7e`（`ntf-test-data-converter`） |
| ワーキングツリー | **計測時点でクリーン**（`git status --short` が無出力）。本書と `issues.md` §7 の追記はその後に行ったため、いま実行するとその 2 ファイルが出る。`src/` は最後まで無変更（`git diff HEAD -- src/main src/test pom.xml \| wc -l` → 0） |
| JDK | `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64` |
| JaCoCo | オフライン計測（`jacoco:instrument` → `test` → `jacoco:restore-instrumented-classes`）。実行データは `jacoco.exec`（プロジェクト直下。`.gitignore:3` で除外） |

**実行したコマンド**（この順にそのまま実行する。1 つめの前に `rm -f jacoco.exec` を行い、
`jacoco.exec` が前回実行の追記で汚れていないことを担保している）。

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
rm -f jacoco.exec
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec
```

**`Tests run` の実測行**（1 つめのコマンドの surefire 集計行。`BUILD SUCCESS`）。

```
[WARNING] Tests run: 595, Failures: 0, Errors: 0, Skipped: 2
```

> **`-o`（オフライン）を付けているのは依存解決をリポジトリキャッシュに閉じるためであり、
> 計測結果には影響しない。** `-o` を外しても同じ数値になることは未確認。

**この計測は 2 回（`jacoco.exec` を削除したうえでのやり直しを含む）行い、
本書の全数値が一致することを確かめた。**

## 1. 対象 6 区分の行・分岐カバレッジ

数値の導出コマンド（`target/site/jacoco/jacoco.csv` の 6=BRANCH_MISSED・7=BRANCH_COVERED・
8=LINE_MISSED・9=LINE_COVERED 列から導く）。

```sh
awk -F, 'NR>1 && ($3 ~ /^(XlsFormatReader|XlsFormatWriter|YamlFormatReader|YamlFormatWriter|TestCoreReaderAdapter)/ \
  || $2 ~ /converter\.model$/) { printf "%-52s line %3d/%-3d branch %3d/%-3d\n", $3, $9, $8+$9, $7, $6+$7 }' \
  target/site/jacoco/jacoco.csv
```

| # | 区分 | クラス（JaCoCo の CLASS 名） | 行 | 分岐 |
|---|---|---|---|---|
| ① | 辺① Excel→中間モデル | `XlsFormatReader` | 190/195 | 115/140 |
| ① | 〃 | `XlsFormatReader.new DirectiveUtil.ValueMapper() {...}` | 2/2 | 0/0 |
| ② | 辺② YAML→中間モデル | `YamlFormatReader` | 192/192 | 102/102 |
| ② | 〃 | `YamlFormatReader.new DirectiveUtil.ValueMapper() {...}` | 2/2 | 0/0 |
| ③ | 辺③ 中間モデル→Excel | `XlsFormatWriter` | 157/158 | 101/104 |
| ④ | 辺④ 中間モデル→YAML | `YamlFormatWriter` | 157/159 | 86/90 |
| ⑤ | 本体アダプタ | `TestCoreReaderAdapter` | 46/46 | 11/11 |
| ⑤ | 〃 | `TestCoreReaderAdapter.HeaderCollector` | 16/20 | 6/6 |
| ⑤ | 〃 | `TestCoreReaderAdapter.BodyLineCollector` | 23/27 | 12/14 |
| ⑤ | 〃 | `TestCoreReaderAdapter.SendSyncBodyCollector` | 8/8 | 0/0 |
| ⑥ | 中間モデル | `TestDataContainer` | 8/8 | 2/2 |
| ⑥ | 〃 | `TestDataSection` | 8/8 | 2/2 |
| ⑥ | 〃 | `TestDataBlock` | 22/22 | 10/10 |
| ⑥ | 〃 | `TableDataBlock` | 4/4 | 0/0 |
| ⑥ | 〃 | `ColumnRowDataBlock` | 11/11 | 6/6 |
| ⑥ | 〃 | `ListMapBlock` | 2/2 | 0/0 |
| ⑥ | 〃 | `FileDataBlock` | 14/14 | 4/4 |
| ⑥ | 〃 | `FileDataBlock.FileType` | 1/1 | 0/0 |
| ⑥ | 〃 | `MessageDataBlock` | 13/13 | 2/2 |
| ⑥ | 〃 | `RecordLayout` | 15/15 | 2/2 |
| ⑥ | 〃 | `FieldDef` | 12/12 | 4/4 |
| ⑥ | 〃 | `ModelPreconditions` | 40/40 | 28/28 |

**区分ごとの合計**

| 区分 | 行 | 分岐 | 未到達行 | 未到達分岐 |
|---|---|---|---|---|
| ① 辺① `XlsFormatReader`（匿名クラス含む） | 192/197 | 115/140 | 5 | 25 |
| ② 辺② `YamlFormatReader`（匿名クラス含む） | 194/194 | 102/102 | 0 | 0 |
| ③ 辺③ `XlsFormatWriter` | 157/158 | 101/104 | 1 | 3 |
| ④ 辺④ `YamlFormatWriter` | 157/159 | 86/90 | 2 | 4 |
| ⑤ `TestCoreReaderAdapter`（入れ子 3 クラス含む） | 93/101 | 29/31 | 8 | 2 |
| ⑥ 中間モデル 11 クラス（+ `FileDataBlock.FileType`） | 150/150 | 60/60 | 0 | 0 |
| **合計** | **943/959** | **493/527** | **16** | **34** |

**辺②（`YamlFormatReader`）と中間モデル 11 クラスは行・分岐とも 100 % である。**

## 2. 参考：6 区分の外のクラス（本タスクの対象外）

対象 6 区分に含まれないクラスの実測。**分類・根拠付けは行っていない**（タスクの対象外）。
導出コマンドは §1 の `awk` の条件を `!(...)` に反転したもの。

| パッケージ | クラス | 行 | 分岐 |
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

## 3. 未到達分岐の全数と、どちら側が未到達かの導出

**未到達分岐の総数は 34 件。** JaCoCo の分岐は `IFxx` ／ `TABLESWITCH` ／ `LOOKUPSWITCH` の
出口エッジ数であり、1 行に複数の条件がある場合はその行にまとまって計上される。
行単位の `mb`（未到達分岐数）・`cb`（到達分岐数）・`mi`（未実行命令数）・`ci`（実行命令数）は
`target/site/jacoco/jacoco.xml` から取る。次のコマンドがこの節と §4・§5 の一覧をそのまま出す。

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

実行結果の末尾行（**この 2 つの数がこの文書の 34 件・16 件の出典**）。

```
{'XlsFormatReader.java': (25, 5), 'XlsFormatWriter.java': (3, 1),
 'YamlFormatWriter.java': (4, 2), 'YamlFormatReader.java': (0, 0),
 'TestCoreReaderAdapter.java': (2, 8)}
```

`25 + 3 + 4 + 0 + 2 = 34`（分岐）／`5 + 1 + 2 + 0 + 8 = 16`（行）。

**「どちら側が未到達か」の決め方。** JaCoCo は側を直接は出さない。次の 3 つだけを根拠にしている。

1. 同じ行または直後の行の `mi` ／ `ci`（例：`if (x) { throw ... }` の `throw` 行が `mi>0 ci=0` なら
   `if` の true 側が未到達）
2. 消去法（`mb + cb` は `if` の条件数 × 2、`switch` は相異なる飛び先数 + `default` に一致するため、
   到達済みが確定した側を引く）
3. 到達済みであることを示す `src/test` の実在テスト（該当箇所に明記した）

**実験で確かめた 1 件。** `XlsFormatReader.java:585`（`value != null && !value.isEmpty()`）は
2 つの条件のうちどちらの側が未到達か消去法では決まらなかったため、
`XlsFormatReaderTest` に「`SETUP_TABLE` ブロックで先頭セルが空文字のデータ行（`["", "x"]`）」を作る
使い捨てのテストを 1 件足して
再計測した（`Tests run: 596`）。**L585 の `mb=1 cb=3` は変わらなかった**ため、
`!value.isEmpty()` の false 側は元から到達済みであり、未到達なのは `value != null` の false 側と確定した。
**この使い捨てテストは `git checkout --` で戻してあり、本タスクの成果物には含まれない**
（§0 の計測はこの取り消し後のクリーンなツリーで行っている）。

## 4. 未到達分岐 34 件の一覧と分類

出典表記の約束：`notation:nnn` は `nablarch-document` の
`ja/development_tools/testing_framework/implementation/testdata_notation.rst` の
**`30a8271` 時点**の行番号（`steering.md`「出典の版」に合わせた）。
`nablarch-testing` 側の行番号は同リポジトリの `c5f3340` 時点。

分類は 2 値。**「テストを足すべき」15 件・「テスト不要」19 件**（`15 + 19 = 34`）。件数の導出——

```sh
F=.rn/ntf-test-data-converter/coverage/coverage-report.md
grep -cE '^\| [0-9]+ \|.*\*\*テストを足すべき\*\*' $F   # → 15
grep -cE '^\| [0-9]+ \|.*テスト不要（' $F                  # → 19
```

（行頭を `| 数字 |` に限っているのは、この 2 行自身がヒットして主張を反証するのを避けるためである。
`grep -c 'テスト不要（' $F` は本コマンド行自身を数えて 20 を返す。）

### 4.1 辺① `XlsFormatReader`（25 件）

| # | メソッド | 行 | 未到達の側 | 分類 | 根拠 |
|---|---|---|---|---|---|
| 1 | `read` | 102 で開始、**116** | `processed.add(singleKey(LIST_MAP, id))` の **false 側** | **テストを足すべき** | `notation:622`「同一の読み込み単位内に同じ ID のデータブロックが複数ある場合は**先着一致**となり、2件目以降は無視される」。この分岐は明文化された先着一致そのものであり、到達不能ではない |
| 2 | `read` | **120** | `processed.add(singleKey(MESSAGE, id))` の **false 側** | **テストを足すべき** | 上と同型の重複識別子ガード。**MESSAGE の識別子重複を直接定めた明文は見つけられなかった（未確認）**が、実装は LIST_MAP と同じ先着一致を実現しており、記法上禁じられてもいないため到達可能 |
| 3 | `read` | **122** | `if (block != null)` の **false 側** | **テストを足すべき** | `readMessageBlock` が `null` を返す経路（#4 と同じ 1 本のシナリオ）。ソースの注記が「本体の `MessageParser` が空結果を返したとき `adapter.readMessage` は `null` を返す（**正常系**）」と明記している（`XlsFormatReader.java:230-232`） |
| 4 | `readMessageBlock` | **230** | `if (message == null)` の **true 側** | **テストを足すべき** | 同上。未到達行 `L233 (return null)` と整合（`mi=2 ci=0`） |
| 5 | `read` | **126** | `else if (XlsDataTypeUtil.isSendSyncType(type))` の **false 側** | テスト不要（到達不能） | `DataType` は 14 値（`nablarch-testing` `c5f3340` `DataType.java` の定数 14 個）。`DEFAULT` は `TestCoreReaderAdapter.HeaderCollector.parse` が `if (type == DataType.DEFAULT) continue;`（`TestCoreReaderAdapter.java:362-364`）で `BlockHeader` にしない。残る 13 値を `isTableType`（3）・`isFileType`（4）・`LIST_MAP`（1）・`MESSAGE`（1）・`isSendSyncType`（4）が**重複なく全数**分岐する（`3+4+1+1+4 = 13`）ため、この `else if` が false になる `type` は存在しない |
| 6 | `readListMapBlock` | **188** | `value == null ? null : stripQuotes(value)` の **null 側（true 側）** | テスト不要（到達不能・型／契約に遡る） | `value` は `mapRow.get(column)`。(a) セル値そのものが `null` になり得ない——`PoiXlsReader.java:123`（`nablarch-testing` `c5f3340`）が `String cellValue = cell == null ? "" : cell.toString();` で必ず非 `null` を入れる。(b) キーが欠けることもない——`HeaderLine.getMapExcludingMarkerColumns`（同 `HeaderLine.java:59-67`）が `effectiveColumnNames` の全要素をキーに `put` し、値は `excludeMarkerColumns`（同 `:75-85`）が `(i >= line.size()) ? "" : line.get(i)` で必ず埋める。`column` は `readListMapColumnNames`（`TestCoreReaderAdapter.java:121-130`）が返す `effectiveColumnNames` の部分列 |
| 7 | `emptyToNull` | **328** | `recordType == null` の **true 側**（`recordType.isEmpty()` の両側は到達済み） | テスト不要（到達不能・型／契約に遡る） | 同じく `PoiXlsReader.java:123`。レコード種別セルは生行の先頭要素であり、空セルは `""` であって `null` にならない。`isEmpty()` 側は `XlsFormatReaderRealFileTest#readsOmittedRecordTypeAsNullFromRealBook` が実 `.xlsx` で担保している |
| 8 | `skipToFirstNameRow` | **340** | `idx < bodyLines.size()` の **false 側** | テスト不要（内部整合性ガード） | 名前行が生行に見つからないまま走査が尽きる状態。直後に `verifyNameRow` が必ず `IllegalStateException` を投げるため（#9）、到達＝二経路読み込みの実装バグ。ソースが「内部整合性ガード。断片構造と生行の対応が壊れていれば二経路読み込みロジックのバグ」と明記（`XlsFormatReader.java:360`） |
| 9 | `verifyNameRow` | **361** | `idx >= bodyLines.size()` の **true 側** | テスト不要（内部整合性ガード） | 同上（`mb=2`。未到達行 `L362` の `throw` と整合） |
| 10 | `verifyNameRow` | **361** | `!tail(bodyLines.get(idx)).equals(names)` の **true 側** | テスト不要（内部整合性ガード） | 同上 |
| 11 | `requireLine` | **446** | `idx >= bodyLines.size()` の **true 側** | テスト不要（内部整合性ガード） | ソースが同じ文言で内部整合性ガードと明記（`XlsFormatReader.java:445`）。未到達行 `L447` の `throw`（`mi=8 ci=0`）と整合 |
| 12 | `readFieldDefs` | **396** | `i < originalTypes.size()` の **false 側**（`type` に `null` を入れる側） | テスト不要（NTF 記法外） | フィールド名称行より型行が短い状態。`notation:883` が固定長で「フィールド名称・データ型・フィールド長の3リストが**同サイズで必須**」・可変長で「フィールド名称・データ型の2リストが**同サイズで必須**」と定め、`notation:889` が「フィールド名称・データ型・フィールド長リストのサイズが一致していない」を**記述時のエラー**に挙げる |
| 13 | `readFieldDefs` | **397** | `i < originalLengths.size()` の **false 側**（`originalLengths != null` の両側は到達済み） | テスト不要（NTF 記法外） | 同上（固定長でフィールド長行だけが短い状態）。`notation:883` ／ `notation:889` |
| 14 | `isQuotationWrapped` | **507** | `value == null` の **true 側** | テスト不要（到達不能・型／契約に遡る） | 唯一の呼び出し元は `normalizeDirectiveValue`（`XlsFormatReader.java:494`）であり、`value` はディレクティブ行のセル値。`PoiXlsReader.java:123` により `null` にならない |
| 15 | `isQuotationWrapped` | **507** | `value.length() <= 2` の **true 側** | **テストを足すべき** | 2 文字以下のディレクティブ値。ソース自身が「デフォルトディレクティブとして本体器に注入される `"` 1 文字（可変長の `quoting-delimiter` 既定値）等は記法ではなく生値であり…ここでも素通しする」（`XlsFormatReader.java:491-495`）と、**通ることを前提にした挙動**を書いているのに一度も通っていない。未到達行 `L508 (return false)` と整合 |
| 16 | `isQuotationWrapped` | **510** | `value.endsWith("\"")` の **false 側** | **テストを足すべき** | 半角 `"` で始まるが `"` で終わらない値（例 `"abc`）。`notation:1325`「半角または全角ダブルクォートで**前後が囲まれた場合のみ**、外側1層を除去する」の「のみ」を守っていることを示す分岐。消去法：`L494 (return stripQuotes(value))` が到達済み＝`startsWith` true・`endsWith` true は到達済み、`L511` が評価されている＝`startsWith` false も到達済み |
| 17 | `isQuotationWrapped` | **511** | `value.startsWith("”")` の **true 側** | **テストを足すべき** | 全角 `”` の記法。`notation:1325`「**半角または全角**ダブルクォートで前後が囲まれた場合のみ」／`notation:1397`「前後のダブルクォート（**全角・半角問わない**）を除いた文字列として扱う」。`mb=3 cb=1` で到達済みは `startsWith("”")` の false 側だけ |
| 18 | `isQuotationWrapped` | **511** | `value.endsWith("”")` の **true 側** | **テストを足すべき** | 同上 |
| 19 | `isQuotationWrapped` | **511** | `value.endsWith("”")` の **false 側** | **テストを足すべき** | 同上（`”` で始まり `”` で終わらない値） |
| 20 | `tail` | **522** | `list.isEmpty()` の **true 側** | **テストを足すべき** | 生行が空リストになる場合。`TestCoreReaderAdapter.BodyLineCollector.parse`（`:464`）が `NablarchTestUtils.trimTailCopy(line)` を通すため、**全セルが空の行は空リストになる**（`nablarch-testing` `c5f3340` `NablarchTestUtils.java:251-263` の `trimTail` が末尾の空要素を全部落とす）。`notation:883` は「全フィールドを省略した行（Excel形式では先頭セルが空の行…）」を有効なレコードとして認めており、この形は記法外ではない |
| 21 | `stripQuotes` | **541** | `if (value == null)` の **true 側** | テスト不要（到達不能・呼び出し元 4 箇所を全数確認） | 呼び出し元は 4 箇所（`grep -n 'stripQuotes(' src/main/java/nablarch/test/tool/converter/xls/XlsFormatReader.java` → `:157` `:188` `:425` `:494`。`:539` は定義行）。`:157` と `:188` は三項演算子で `null` を除外済み、`:425` は `i < valueCells.size() ? valueCells.get(i) : ""` の結果で、生行の要素は `PoiXlsReader.java:123` により非 `null`、`:494` は直前の `isQuotationWrapped` が `null` に対して `false` を返す（`:507`）ため、いずれも `null` を渡さない。**ソースの Javadoc／コメントが「`null` を返すためこのガードは必須」と読める書き方をしているが、実測はガードが一度も通らないことを示す**（既知課題 `issues.md` XLS-09）。未到達行 `L542` と整合 |
| 22 | `isEmptyEntry` | **585** | `value != null` の **false 側**（`!value.isEmpty()` の両側は到達済み） | テスト不要（到達不能・型／契約に遡る） | 呼び出し元は `dropEmptyEntries`（`:566-574`）の 2 箇所（`:162` テーブル系／`:193` LIST_MAP）。要素は #6 と同じ経路で作られるため `null` にならない。**どちらの側が未到達かは §3 の使い捨てテストによる実験で確定した** |
| 23 | `deduplicateColumnNames` | **617** | `warned.add(name)` の **false 側** | **テストを足すべき** | 同一のカラム名が **3 回以上**現れたとき（2 回目の重複検出で WARN を重複出力しない）。カラム名の重複そのものは既存テストで到達済み（`cb=1`）であり、3 回以上にするだけで到達する |
| 24 | `bookName` | **694** | `slash < 0` の **true 側** | テスト不要（本番経路に存在しない・呼び出し元を全数確認） | 本番の唯一の呼び出し元は `XlsFormatHandler.java:46` の `reader.read(basePath, bookName + "/" + sheetName)` であり、リテラルの `"/"` を必ず含む。`read` の Javadoc も `resourceName` を「`"ブック名/シート名"`」と契約している（`XlsFormatReader.java:97`）。**ただし `read` は `public` なので担保の穴として §7 に開示する** |
| 25 | `sheetName` | **705** | `slash < 0` の **true 側** | テスト不要（同上） | 同上 |

### 4.2 辺③ `XlsFormatWriter`（3 件）

| # | メソッド | 行 | 未到達の側 | 分類 | 根拠 |
|---|---|---|---|---|---|
| 26 | `write` | **107** | `if (parent != null)` の **false 側** | **テストを足すべき** | `basePath` が空文字列などで親ディレクトリを持たない相対パス（例 `foo.xlsx`）が生成される場合。**ソースのコメント自身が「…`getParent()` は `null` を返すため、null チェックが必須」と書いている**（`XlsFormatWriter.java:106`）のに一度も通っていない。`write` は `public` であり `basePath` に空文字列を渡せば到達する。`inventory.md` §3.1-2 が既に「担保の穴」として開示している |
| 27 | `layout` | **202** | `else if (block instanceof MessageDataBlock)` の **false 側** | テスト不要（Java イディオム＝到達不能な安全網） | false 側は直後の `throw new IllegalArgumentException("unsupported block: " …)`（未到達行 `L206`）に落ちる。`TestDataBlock` の具象サブクラスは `TableDataBlock` ／ `ColumnRowDataBlock`／`ListMapBlock` ／ `FileDataBlock` ／ `MessageDataBlock` に閉じており、`instanceof` の連鎖がこれを全数分岐する。将来 `TestDataBlock` にサブクラスが増えたときのための安全網であり、現状の型階層では到達しない |
| 28 | `isMarkerColumn` | **557** | `columnName != null` の **false 側**（`startsWith("[")` ／ `endsWith("]")` の両側は到達済み） | テスト不要（中間モデルの不変条件） | カラム名 `null` は `ColumnRowDataBlock` の生成時に `ModelPreconditions` が拒否する（方針は `steering.md` Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」・`issues.md` XLS-38）。**書き出し側に番人を置かない方針の裏返しとしてこの `null` ガードだけが残っている。** `endsWith("]")` の false 側が到達済みであることは `XlsFormatWriterTest.java:874`（カラム名 `"[half"`）で確認した |

### 4.3 辺④ `YamlFormatWriter`（4 件）

| # | メソッド | 行 | 未到達の側 | 分類 | 根拠 |
|---|---|---|---|---|---|
| 29 | `write` | **84** | `if (parent != null)` の **false 側** | **テストを足すべき** | #26 と同型（`YamlFormatWriter.java:83` に同じコメントがある） |
| 30 | `emitBlock` | **139** | `else if (block instanceof MessageDataBlock)` の **false 側** | テスト不要（Java イディオム＝到達不能な安全網） | #27 と同型。未到達行 `L143` の `throw` と整合 |
| 31 | `rawGroup` | **484** | `groupId.charAt(last) == ']'` の **false 側**（`groupId.charAt(0) == '['` の両側は到達済み） | テスト不要（NTF 記法外） | `[` で始まるが `]` で終わらないグループ ID。`notation:278`「データタイプ名の直後に `[グループID]` を付ける。例: `SETUP_TABLE[case_001]=EMPLOYEE_TABLE`」が `[ ]` で囲むことを定めており、`[case_001` のような閉じ括弧なしは記法として存在しない。`charAt(0) == '['` の false 側は `YamlFormatWriterTest#serialize_unbracketedGroupId_isUsedAsRawValue`（グループ ID `"raw"`）が担保 |
| 32 | `sectionKey` | **503** | `switch (type)` の **`default` 側** | テスト不要（Java イディオム＋中間モデルの不変条件） | `default` は `throw new IllegalArgumentException("unsupported DataType: " …)`（未到達行 `L520`）。写せないのは `DataType.DEFAULT` だけで、`TestDataBlock` が生成時に拒否する（`issues.md` XLS-20）。残る 13 値は `case` で全数分岐している。`mb=1 cb=11` は、13 個の `case` ラベルのうち `SETUP_FIXED`／`SETUP_VARIABLE` と `EXPECTED_FIXED`／`EXPECTED_VARIABLE` がそれぞれ同じ飛び先を共有し、switch の相異なる後続が 11 + `default` の 12 本になるためである |

### 4.4 `TestCoreReaderAdapter.BodyLineCollector`（2 件）

| # | メソッド | 行 | 未到達の側 | 分類 | 根拠 |
|---|---|---|---|---|---|
| 33 | `parse` | **454** | `if (groupId != null)` の **false 側** | **テストを足すべき** | 先頭セルがデータタイプ名で始まるのに `=` を含まない行（`markerGroupId` が `null` を返す。`TestCoreReaderAdapter.java:282-286`）。**同型のガードは `HeaderCollector.parse`（`:366-369`）では両側とも到達済み**であり、記法として書けない形ではない。`BodyLineCollector` 側の固定が抜けている |
| 34 | `parse` | **458** | `groupId.equals(targetGroupId)` の **false 側** | **テストを足すべき** | 同一シートに同じデータタイプで**異なるグループ ID** のブロックがある場合。`notation:250-254`／`notation:306` が同一読み込み単位に複数グループを併記することを正面から扱っている（「デフォルトグループと個別グループのデータは併用でき、両方が混在した場合は両方のデータが有効になる」）ため、記法として正当な入力である |

## 5. 未到達行 16 件の一覧

分岐と重複するもの（`throw` 行・`return` 行）が大半である。すべて §4 のいずれかの分岐に紐づく。

| ファイル | 行 | 内容 | §4 の対応 |
|---|---|---|---|
| `XlsFormatReader.java` | 233 | `return null;`（MESSAGE 空ブロック） | #4 |
| `XlsFormatReader.java` | 362 | `throw new IllegalStateException(...)`（`verifyNameRow`） | #9・#10 |
| `XlsFormatReader.java` | 447 | `throw new IllegalStateException(...)`（`requireLine`） | #11 |
| `XlsFormatReader.java` | 508 | `return false;`（`isQuotationWrapped`） | #14・#15 |
| `XlsFormatReader.java` | 542 | `return null;`（`stripQuotes`） | #21 |
| `XlsFormatWriter.java` | 206 | `throw new IllegalArgumentException("unsupported block: " …)` | #27 |
| `YamlFormatWriter.java` | 143 | `throw new IllegalArgumentException("unsupported block: " …)` | #30 |
| `YamlFormatWriter.java` | 520 | `throw new IllegalArgumentException("unsupported DataType: " …)` | #32 |
| `TestCoreReaderAdapter.java` | 380 | `HeaderCollector#onReadLine` の本体（空） | 下記 |
| `TestCoreReaderAdapter.java` | 385 | `HeaderCollector#onTargetTypeFound` の本体（空） | 下記 |
| `TestCoreReaderAdapter.java` | 389 | `HeaderCollector#isTargetType` の `return false;` | 下記 |
| `TestCoreReaderAdapter.java` | 394 | `HeaderCollector#shouldStopOnNextOne` の `return false;` | 下記 |
| `TestCoreReaderAdapter.java` | 474 | `BodyLineCollector#onReadLine` の本体（空） | 下記 |
| `TestCoreReaderAdapter.java` | 479 | `BodyLineCollector#onTargetTypeFound` の本体（空） | 下記 |
| `TestCoreReaderAdapter.java` | 483 | `BodyLineCollector#isTargetType` の `return false;` | 下記 |
| `TestCoreReaderAdapter.java` | 488 | `BodyLineCollector#shouldStopOnNextOne` の `return false;` | 下記 |

**`TestCoreReaderAdapter` の 8 行は「テスト不要」。** `HeaderCollector` ／ `BodyLineCollector` は
いずれも `TestDataParsingTemplate#parse(String id)` を上書きしており、
テンプレートメソッド `doParse()` からこの 4 つの抽象メソッドが呼ばれる経路が無い。
それでも実装が必須なのは `TestDataParsingTemplate` が `abstract` で宣言しているためで、
**コンパイラが要求する到達不能な実装**である（ソースにも
`// parse(String id) を上書きしているため doParse() からこれらの抽象メソッドは呼ばれない。`
`// TestDataParsingTemplate の契約上、実装は必須。` と明記されている。
`TestCoreReaderAdapter.java:375-376` ／ `:469-470`）。

## 6. 「テストを足すべき」15 件の処理

**15 件すべてを `issues.md` へ残課題として記録した**（`COV-01`〜`COV-11`。
1 課題が複数分岐にまたがるものがあるため件数は一致しない）。**テストは足していない。**

**足さずに記録を選んだ理由。** タスク #26 の目的は「計測と未到達分岐の列挙・根拠付け」であり、
テストの追加は後続タスク（#27 の軸マトリクス作成、#28）の範囲である。ここでテストを 15 件足すと、
軸マトリクスができる前に軸に紐づかないテストが増え、#27 の突き合わせ対象が動いてしまう。
完了条件も「追加されたか `issues.md` へ残課題として記録されたかのいずれか」と明示的に記録を認めている。

| `issues.md` の ID | §4 の # | 対象 |
|---|---|---|
| `COV-01` | 1, 2 | LIST_MAP ／ MESSAGE の重複識別子（先着一致） |
| `COV-02` | 3, 4 | MESSAGE 空ブロック（`readMessage` が `null`） |
| `COV-03` | 15 | 2 文字以下のディレクティブ値 |
| `COV-04` | 16 | 半角 `"` で始まり `"` で終わらない値 |
| `COV-05` | 17, 18, 19 | 全角 `”` の QuotationTrimmer 記法 |
| `COV-06` | 20 | 全セルが空の生行（`tail` が空リストを受ける） |
| `COV-07` | 23 | 同一カラム名が 3 回以上 |
| `COV-08` | 26 | 辺③ `parent == null` |
| `COV-09` | 29 | 辺④ `parent == null` |
| `COV-10` | 33 | `BodyLineCollector` の非マーカー行 |
| `COV-11` | 34 | `BodyLineCollector` の異なるグループ ID |

## 7. 開示：担保の穴と、確かめきれなかったこと

1. **`bookName` ／ `sheetName` の `slash < 0` 側（#24・#25）は `public` API の穴である。**
   本番の呼び出し元が必ず `"/"` を含む文字列を組み立てるため「テスト不要」に分類したが、
   `XlsFormatReader#read` は `public` であり、外部の呼び出し元が `"/"` なしの
   `resourceName` を渡せば到達する。契約は Javadoc にしかない（型では縛れていない）。

2. **MESSAGE の重複識別子（#2）については、記法の明文を見つけられなかった。**
   `notation:622` は `LIST_MAP` の ID 解決規則として先着一致を明文化しているが、
   MESSAGE の識別子について同等の記述は確認できていない（**未確認**）。
   実装が LIST_MAP と同じ形のガードを置いていることだけを根拠に「テストを足すべき」とした。

3. **`inventory.md` 追補その 6 の実測（同 `:809-810`）とは一致している。行番号だけがずれている。**
   同文書は `XlsFormatWriter` を `line 157/158 branch 101/104`、`YamlFormatWriter` を
   `line 157/159 branch 86/90` と記録しており、本計測と同じである。
   一方、同文書が併記する未到達の行番号（`XlsFormatWriter.java` L107／L202／**L205**／**L550**、
   `YamlFormatWriter.java` L84／L139／L143／**L483**／**L502**／**L519**）は、
   本計測では L107／L202／**L206**／**L557**、L84／L139／L143／**L484**／**L503**／**L520** である。
   **差はコミット `54d2057` による行ずれである**——`54d2057^` 時点の `XlsFormatWriter.java:205`／`:550` と
   `YamlFormatWriter.java:483`／`:502`／`:519` の本文が、本書の L206／L557 と L484／L503／L520 の本文に
   一致することを確かめた（`git show 54d2057^:<path> | sed -n '205p;550p'` 等）。
   同コミットの両ファイルの差分は Javadoc・コメント行だけで、コード行の増減を含まない——次の 2 つの
   コマンドはいずれも `0` を返す。

   ```sh
   git show 54d2057 -- src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java \
     | grep -E '^[+-]' | grep -vE '^[+-]{3}' | grep -vE '^[+-]\s*(\*|//|/\*)' | wc -l
   git show 54d2057 -- src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java \
     | grep -E '^[+-]' | grep -vE '^[+-]{3}' | grep -vE '^[+-]\s*(\*|//|/\*)' | wc -l
   ```

   本書の行番号は §0 の HEAD SHA `da66425` 時点のものである。

4. **`-o`（オフライン）なしでの再現は未確認**（§0 の注記のとおり）。
