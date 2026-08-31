# 完了報告 —— マーカーカラムだけのブロックを保つ是正（#50〜#54）

報告の順は指示書 §5 のとおり ①差分の要約 ②テスト1〜4 と変異確認 ③既存テスト変更の全件 ④カバレッジ。

---

## ① 差分の要約

起点は `8290d56`（本是正を起こした `/rn:up` の突き合わせコミット。実装は #49 完了時のまま）。

```
git diff --stat 8290d56..HEAD -- src/
→ 19 files changed, 1319 insertions(+), 114 deletions(-)
```

### `src/main` の変更（7 ファイル）

| ファイル | 変更 |
|---|---|
| `core/reader/MarkerOnlyBlock.java`（新設） | マーカーカラムだけのブロックの版面（カラム名と各行の値）を運ぶ値クラス |
| `core/reader/TestCoreReaderAdapter.java` | `readMarkerOnlyBlock` を追加。`BodyLineCollector` にインタープリタ列・行末トリムの有無を渡せるようにし、識別子の比較を本体の器（テーブル名は trim ＋ 大文字化）に合わせて正規化する |
| `xls/XlsFormatReader.java` | マーカー除外後のカラム名が 0 件のときだけ版面を使う経路を追加。`rowCount` はカラム名が 1 件も残らないブロック（マーカーでもない）にだけ効く形で残す |
| `yaml/YamlFormatReader.java` | `markerOnlyColumns`（カラム名がマーカーだけかの判定）と `rawRows`（原文からの行復元）を追加し、`addListMapBlocks`・`addTableBlocks` が使う |
| `model/ColumnRowDataBlock.java` | 番人を「カラム名 0 件では行を 1 件も持てない」へ広げた |
| `yaml/YamlFormatWriter.java` | `emitMapRows` のカラム 0 件分岐（`- {}` を書く経路）を削除 |
| `xls/XlsFormatWriter.java` | `layoutColumnRow` の Javadoc を実態へ（コード変更なし） |

### 設計の要点（実物で確認した事実）

1. **本体は行の解釈をマーカーカラムの除外より前に行の全セルへ掛ける**
   （`nablarch-testing@dcaed44` の
   `src/main/java/nablarch/test/core/reader/TestDataParsingTemplate.java:183`）。よって Excel 側の
   マーカーカラムの値も**解釈後の値**として取り出せ、中間モデルの取り決め（解釈後の値を持つ）に収まる。
   **その結果、辺③（Excel 書き）は記法への戻しを 1 行も変えずに逆写像として働く。**
2. **YAML 側はマーカーカラムを値加工の前に読み飛ばす**
   （`nablarch-testing-yaml@4431cf8` の
   `src/main/java/nablarch/test/core/reader/yaml/YamlTableDataBuilder.java:142`・`:201`）。
   変換ツールの読みはインタープリタを 1 つも積まない（同 rev の `InterpreterResolver.java:54`-`:56` の `raw()`）ため、
   YAML は原文がそのまま値になる。**辺④（YAML 書き）も変更していない。**
3. **本体はマーカーだけのブロックでも行を落とさない**（同 `dcaed44` の `ListMapParser.java:86`-`:89`／
   `TableDataParser.java:98`-`:101`）。落としていたのは変換ツールの `XlsFormatReader#rowCount` だけだった。
4. **カラム名 0 件で行を持つ形はどちらの読みからも作られなくなった**ため、中間モデルの生成時に拒否し、
   辺④の `- {}` を書く分岐を落とした（番人は書き出し側でなく生成時に置く）。

---

## ② 指示書 §2 のテスト 1〜4 と変異確認

### テストの所在

| 指示書 | テストメソッド |
|---|---|
| 1 oracle（Excel→YAML。本体 `BasicTestDataParser` と `YamlTestDataParser` でエントリ数・並びが一致） | `MarkerOnlyBlockConversionTest#keepsListMapEntryCountAndOrderThroughXlsToYaml`／`#keepsTableEntryCountThroughXlsToYaml` |
| 2 往復（Excel→YAML→Excel ／ YAML→Excel→YAML。実ファイル起点） | `MarkerOnlyBlockConversionTest#roundTripsMarkerOnlyListMapFromXls`／`#roundTripsMarkerOnlyListMapFromYaml` |
| 3 スキーマ検証 | `MarkerOnlyBlockConversionTest#convertedYamlPassesSchemaValidation`／`#convertedYamlTablePassesSchemaValidation` |
| 4 非回帰（実データカラムを持つブロックのマーカーは消える） | `MarkerOnlyBlockConversionTest#dropsMarkerColumnThroughConversionWhenBlockHasDataColumn`／`XlsMarkerOnlyEntryTest` 2 件／`YamlMarkerOnlyBlockTest#dropsMarkerColumnWhenListMapHasDataColumn`・`#dropsMarkerColumnWhenTableHasDataColumn` |

各辺の単体の担保は `XlsMarkerOnlyBlockTest`（辺① 4 件）と `YamlMarkerOnlyBlockTest`（辺② 4 件）にある。

### 変異確認

コマンドはいずれも次の形（変異を当てて実行し、`git checkout -- <file>` で戻す）:

```sh
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 \
  mvn -o test -Dtest=MarkerOnlyBlockConversionTest -DfailIfNoSpecifiedTests=false -Djacoco.skip=true
```

| 変異 | 内容 | 結果 |
|---|---|---|
| **M1** 辺①を旧実装へ戻す | `XlsFormatReader#markerOnlyBlock` を常に `null` にし、行数を `columnNames.isEmpty() ? 0 : n` へ戻す | `Tests run: 7, Failures: 4`。落ちたのは **テスト1 の 2 件**（`keepsListMapEntryCountAndOrderThroughXlsToYaml:241`／`keepsTableEntryCountThroughXlsToYaml:264`）と **テスト2 の 2 件**（`roundTripsMarkerOnlyListMapFromXls:290`／`roundTripsMarkerOnlyListMapFromYaml:317`） |
| **M2** 辺②を旧実装へ戻す | `YamlFormatReader#markerOnlyColumns` を常に空リストにする | `Tests run: 7, Errors: 2`。落ちたのは **テスト2 の 2 件**（`roundTripsMarkerOnlyListMapFromXls:284`／`roundTripsMarkerOnlyListMapFromYaml:313`。`yamlToXls` で中間モデルの番人が `IllegalArgumentException`）。**テスト1 は辺②を通らないため緑のまま**で、これは想定どおりである |
| **M3** 並びを壊す | `TestCoreReaderAdapter#readMarkerOnlyBlock` の返す行を逆順にする | `Tests run: 11, Failures: 6`（`MarkerOnlyBlockConversionTest` ＋ `XlsMarkerOnlyBlockTest`）。落ちたのは **テスト2 の 2 件**（`roundTripsMarkerOnlyListMapFromXls:292`「データ行 1」／`roundTripsMarkerOnlyListMapFromYaml:317`）と `XlsMarkerOnlyBlockTest` の 4 件 |

**テスト1 が M3 で落ちないことについて。** フレームワークはマーカーカラムの値を読み込み対象から除外するため、
**フレームワークから見ると 4 件のエントリはいずれも空のマップである**。テスト1 が本体の読みから取れる正解は
「エントリの数と並び（位置）」までであり、値の並びは区別できない。値の並びはテスト2（成果物そのものを見る）が
固定しており、M3 はそこで落ちる。**この分担は指示書 §2-1 の「エントリ数・並びが一致」を満たしている。**

---

## ③ 既存テストの変更（全件）

### 期待値を変えたもの（8 件）

| # | テスト | 変更 | タスク |
|---|---|---|---|
| 1 | `XlsFormatReaderRealFileTest#dropsMarkerOnlyRowsAsEmptyEntriesInRealBook` | `#keepsMarkerOnlyColumnAndValuesInRealBook` へ改名。「列名 0 件・行 0 件」→「`[no]` 1 列・行 2 件」 | #50 |
| 2 | `XlsFormatReaderRealFileTest#dropsMarkerOnlyRowsAsEmptyEntriesInListMapInRealBook` | `#keepsMarkerOnlyColumnAndValuesInListMapInRealBook` へ改名。同上（行 1 件） | #50 |
| 3 | `XlsFormatWriterTest#roundTripsZeroRowTableWithoutEatingNextBlock` | 読み戻したカラム名を「0 件」→「`[EMPTY]` 1 件」へ。行 0 件は変えていない | #50 |
| 4 | `XlsFormatWriterTest#roundTripsZeroRowListMapWithoutEatingNextBlock` | 同上 | #50 |
| 5 | `XlsReferenceFixtureTest#readsExcelSavedWorkbookIntoIntermediateModel` | `expectedRequestParams()` を「列名 0 件・行 0 件」→「`[no]` 1 列・行 `["1.0"]`」へ（値が `"1.0"` なのは当該セルが表示形式 `@` 付きの数値セルであるため。`issues.md` XLS-01） | #50 |
| 6 | `YamlFormatReaderInvalidInputTest#readsMarkerOnlyTableAsColumnlessRows` | `#keepsMarkerOnlyTableColumnsAndValues` へ改名。「カラム 0 件・値を持たない行 2 件」→「`[no]` 1 列・行 2 件」 | #51 |
| 7 | `TableDataBlockTest#カラムなしでセルを持たない行は保持する` | `#カラムなしで行を持つブロックは生成できない` へ差し替え（保持 → 拒否） | #53 |
| 8 | `TableDataBlockTest#カラムなしでセルを持つ行を抱えるブロックは生成できない` | 期待するメッセージを新しい文言へ | #53 |

### 削除したもの（1 件）

| # | テスト | 理由 | タスク |
|---|---|---|---|
| 9 | `YamlFormatWriterTest#serialize_emptyColumnsRow_emitsEmptyFlowMap` | キーを 1 つも持たない行（`- {}`）を書く形が中間モデルで作れなくなり、書き出す経路ごと無くなった | #53 |

### Javadoc・コメントだけを直したもの（4 件。期待値は変えていない）

| # | 箇所 | 変更 | タスク |
|---|---|---|---|
| 10 | `XlsMarkerOnlyEntryTest` クラス Javadoc | マーカーカラムだけのブロックの扱いを新仕様の説明と `XlsMarkerOnlyBlockTest` への参照へ | #50 |
| 11 | `XlsFormatReaderInvalidInputTest#readsMarkerColumnWithoutBracketsAsOrdinaryDataColumnInRealBook` | 参照先メソッドを実在するものへ | #50 |
| 12 | `XlsFormatReaderRealFileTest` クラス Javadoc（C-08 の段落） | 参照メソッド名を改名後へ | #50 |
| 13 | `XlsFormatWriter#layoutColumnRow`（`src/main`） | 「読み戻すと 0 件に戻る」→「書いたマーカーカラムがそのまま戻る」 | #53 |

### 追加したテスト（22 件）

| クラス | 件数 | 内容 | タスク |
|---|---|---|---|
| `XlsMarkerOnlyBlockTest`（新設） | 4 | 辺①の担保（テーブル／`LIST_MAP`／値の解釈／グループ付き・小文字テーブル名） | #50 |
| `YamlMarkerOnlyBlockTest`（新設） | 4 | 辺②の担保（`list_maps`／テーブル系／非回帰 2 件） | #51 |
| `MarkerOnlyBlockConversionTest`（新設） | 7 | 通しの oracle 2・往復 2・スキーマ 2・非回帰 1 | #52 |
| `TableDataBlockTest` | 1 | 「カラム名 0 件・行 0 件は作れる」 | #53 |
| `YamlFormatWriterTest` | 1 | `serialize_emptyColumnsAndRows_emitsEmptyFlowList` | #53 |
| `TestCoreReaderAdapterTest` | 3 | `readMarkerOnlyBlock` の 3 経路（ブロック不在／短い行の埋め／非マーカー混在） | #54 |
| `XlsFormatReaderInvalidInputTest` | 2 | カラム名が 1 件も残らないブロック 2 形（`null` 記法だけのカラム行／グループ付き `LIST_MAP`） | #54 |

---

## ④ カバレッジ

### 手順（`ntf-step4-09` と同一）

```sh
rm -f jacoco.exec
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec
```

未達の機械抽出（`target/site/jacoco/jacoco.xml` の `line` 要素から `mi>0` または `mb>0`）:

```sh
python3 - <<'PY'
import xml.etree.ElementTree as ET
r = ET.parse('target/site/jacoco/jacoco.xml').getroot()
for pkg in r.findall('package'):
    for sf in pkg.findall('sourcefile'):
        for ln in sf.findall('line'):
            if int(ln.get('mi')) > 0 or int(ln.get('mb')) > 0:
                print('%s/%s:%s mi=%s mb=%s cb=%s'
                      % (pkg.get('name'), sf.get('name'), ln.get('nr'),
                         ln.get('mi'), ln.get('mb'), ln.get('cb')))
PY
```

### 測定値

| 区分 | missed | covered | 到達率 | #49 完了時 |
|---|---|---|---|---|
| LINE | 22 | 1744 | 1744/1766 ＝ 98.75% | 22 / 1677 |
| BRANCH | 8 | 832 | 832/840 ＝ 99.05% | 8 / 800 |
| INSTRUCTION | 90 | 8368 | — | 90 / 8041 |
| METHOD | 8 | 367 | — | 8 / 359 |
| CLASS | 0 | 53 | — | 0 / 52 |

**未達を持つ行は 30 行・未達分岐は 8 で、#49 完了時と同数である。**

### 未達全 30 行と、#49 で承認された (c) との対応（行番号ずれの吸収）

| ファイル:行（HEAD） | #49 の行（`7a539b1`） | (c) |
|---|---|---|
| `core/reader/TestCoreReaderAdapter.java:495` | `:415` | C1 |
| `core/reader/TestCoreReaderAdapter.java:500` | `:420` | C1 |
| `core/reader/TestCoreReaderAdapter.java:504` | `:424` | C1 |
| `core/reader/TestCoreReaderAdapter.java:509` | `:429` | C1 |
| `core/reader/TestCoreReaderAdapter.java:595` | `:509` | C1 |
| `core/reader/TestCoreReaderAdapter.java:600` | `:514` | C1 |
| `core/reader/TestCoreReaderAdapter.java:604` | `:518` | C1 |
| `core/reader/TestCoreReaderAdapter.java:609` | `:523` | C1 |
| `core/reader/YamlTestCoreAdapter.java:234`（分岐 1） | `:234` | C2 |
| `core/reader/YamlTestCoreAdapter.java:235` | `:235` | C2 |
| `ConverterFileFilter.java:184` | `:184` | C3 |
| `ConverterFileFilter.java:190` | `:190` | C3 |
| `DataFormat.java:41`（分岐 1） | `:41` | C4 |
| `DataFormat.java:48` | `:48` | C4 |
| `xls/Styles.java:74`（分岐 1） | `:74` | C4 |
| `xls/Styles.java:95` | `:95` | C4 |
| `xls/XlsFormatReader.java:130`（分岐 1） | `:128` | C6 |
| `xls/XlsFormatReader.java:139` | `:137` | C6 |
| `xls/XlsFormatWriter.java:218`（分岐 1） | `:218` | C5 |
| `xls/XlsFormatWriter.java:225` | `:225` | C5 |
| `yaml/YamlFormatWriter.java:140`（分岐 1） | `:139` | C5 |
| `yaml/YamlFormatWriter.java:147` | `:146` | C5 |
| `yaml/YamlFormatWriter.java:499`（分岐 1） | `:491` | C4 |
| `yaml/YamlFormatWriter.java:516` | `:508` | C4 |
| `yaml/YamlTestDataValidator.java:65`（分岐 1） | `:65` | C7 |
| `yaml/YamlTestDataValidator.java:66` | `:66` | C7 |
| `yaml/YamlTestDataValidator.java:69` | `:69` | C7 |
| `yaml/YamlTestDataValidator.java:70` | `:70` | C7 |
| `yaml/YamlTestDataValidator.java:158` | `:158` | C8 |
| `yaml/YamlTestDataValidator.java:163` | `:163` | C8 |

**本是正が持ち込んだ未達は 0 件である。**

### 途中で 6 行・5 分岐の未達を出し、#54 で潰した経緯

#50〜#53 の時点では、次の 2 か所が未達だった。**どちらも「テストで到達できない防御」ではなく、
実際には到達できる経路（(a) テスト不足）と、到達しない実装（(b)）だった。**

| 箇所 | 分類 | 処置 |
|---|---|---|
| `TestCoreReaderAdapter#readMarkerOnlyBlock` のブロック不在・短い行の埋め・カラム名 0 件 | (a) | アダプタの公開 API に対する単体テストを 3 件追加（`TestCoreReaderAdapterTest`）。あわせて、カラム名が 1 件も残らないブロックへ到達する実 `.xlsx` 2 形のテストを追加（`XlsFormatReaderInvalidInputTest`） |
| `YamlFormatReader#addTableBlocks` の `t < entries.size()` | (b) | 器と Map エントリは同じエントリ列から作られるため 1:1 同順であり、この防御は到達しない。ファイル系・送信同期系と同じ `requireSameSize` へ置き換えた（同メソッドの分岐は既存テストで覆われている） |

**あわせて 2 件の挙動の穴を塞いだ。** `rowCount` を外したままだと、**カラム名の行が
マーカーカラムでもないのに 1 件も残らないブロック**で中間モデルの番人に当たり、
変換が `IllegalArgumentException` で止まっていた（実測）。次の 2 形が該当する。

| 入力 | 旧（本是正の前） | 本是正の途中 | 現在 |
|---|---|---|---|
| `SETUP_TABLE=T` ／ カラム行のセルが `null` 記法だけ ／ データ行あり | 列名 0 件・行 0 件 | **例外** | 列名 0 件・行 0 件（旧に戻した） |
| `LIST_MAP[g1]=lm`（識別行にグループ ID） | 列名 0 件・行 0 件 | **例外** | 列名 0 件・行 0 件（旧に戻した） |

どちらも `rowCount` を「カラム名が 1 件も残らないブロックにだけ効く」形で残すことで解決した。
担保は `XlsFormatReaderInvalidInputTest#dropsRowsWhenTableColumnRowInterpretsToNothingInRealBook` ／
`#dropsRowsWhenListMapMarkerHasGroupIdInRealBook`。

---

## ⑤ ゲート

| 項目 | 結果 |
|---|---|
| `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test` | `Tests run: 731, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS` |
| `@Ignore` アノテーション | 0 件（`grep -rn '^    @Ignore' src/test --include=*.java` → 出力なし） |
| `git status --short` | 空 |
| `git grep -nE '\.rst\|nablarch-document\|解説書' -- src/` | 0 件 |
| `git grep -nE '[A-Za-z]+\.java:[0-9]+' -- src` | 0 件 |
| push | 済み |
| `git diff --stat 8290d56..HEAD -- src/` | 19 files changed, 1319 insertions(+), 114 deletions(-) |

**マージ可否の判断は出さない**（Rules）。判断は調整側（ユーザー）が出す。
