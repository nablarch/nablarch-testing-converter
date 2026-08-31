# task-48 —— カバレッジ基準（未達0）の適用

指示書: `nablarch-document@origin/ntf-yaml-support`（`623d1bc`）の
`.rn/20260724-ntf-yaml-support/ntf-step4-09-converter-coverage.md`。

対象 HEAD: `a5f006c`（`src` は `46457d3` とバイト同一。`git diff --stat 46457d3 a5f006c -- src/` が無出力）。

---

## 1. 測定手順と抽出コマンド

```sh
rm -f jacoco.exec
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec
```

`target/site/jacoco/jacoco.xml` の `line` 要素から `mi>0` または `mb>0` の行を機械抽出する:

```python
import sys, xml.etree.ElementTree as ET
x = ET.parse(sys.argv[1]).getroot()
for c in x.findall('counter'):
    print(c.get('type'), 'missed=%s covered=%s' % (c.get('missed'), c.get('covered')))
rows = []
for pkg in x.findall('package'):
    p = pkg.get('name')
    for sf in pkg.findall('sourcefile'):
        path = 'src/main/java/%s/%s' % (p, sf.get('name'))
        for ln in sf.findall('line'):
            mi, ci = int(ln.get('mi')), int(ln.get('ci'))
            mb, cb = int(ln.get('mb')), int(ln.get('cb'))
            if mi > 0 or mb > 0:
                rows.append((path, int(ln.get('nr')), mi, ci, mb, cb))
for r in sorted(rows):
    print('%s:%d  mi=%d ci=%d mb=%d cb=%d' % r)
```

どちら側が未到達かは、同じ条件の中で「到達済みの側」を通している実在テストと、未達行に隣接する
本体行（`mi` が付いた側）から判定した。

## 2. 着手前（`a5f006c`）の実測

| 区分 | missed | covered | 到達率 |
|---|---|---|---|
| LINE | 72 | 1632 | 1632/1704 ＝ 95.77% |
| BRANCH | 46 | 764 | 764/810 ＝ 94.32% |
| INSTRUCTION | 285 | 7861 | — |
| METHOD | 29 | 341 | — |
| CLASS | 0 | 52 | — |

未達を持つ行は 114 行。`mvn -o clean test` は `Tests run: 682, Failures: 0, Errors: 0, Skipped: 0`。

**`checks/step4-2-report.md` §6 が `26701b7` で記録した分岐 763/810 との差 1 は、`#47` が
足した 4 テストによる**（`26701b7..a5f006c` は `src/test` だけの差分）。

## 3. 完了時（本タスク後）の実測

| 区分 | missed | covered | 到達率 |
|---|---|---|---|
| LINE | 21 | 1677 | 1677/1698 ＝ 98.76% |
| BRANCH | 8 | 800 | 800/808 ＝ 99.01% |
| INSTRUCTION | 83 | 8040 | — |
| METHOD | 8 | 359 | — |
| CLASS | 0 | 52 | — |

未達を持つ行は 29 行。`mvn -o clean test` は `Tests run: 710, Failures: 0, Errors: 0, Skipped: 0`。

**残る 29 行・8 分岐は §6 の (c) 一覧と完全一致する**（他に未達は無い）。

## 4. 分類の集計

| 分類 | 件数（未達を持つ行の数） | 処置 |
|---|---|---|
| (a) テスト不足 | 78 行 | テスト 28 件を追加（§7） |
| (b) 不要な実装 | 7 行 | 削除（§5） |
| (c) 到達不能 | 29 行 / 8 分岐 | 報告のみ（§6）。コメントは入れていない |

`114 = 78 + 7 + 29`。内訳は着手前の一覧と完了時の一覧を突き合わせて機械的に出した
（完了時に残る 29 行はすべて着手前の一覧に含まれ、着手前に無かった未達は 1 件も増えていない）。
(b) の 7 行は §5-1 の 6 行と、§5-2 で分岐だけを外した `XlsFormatWriter.java:589` の 1 行である。

---

## 5. (b) 不要な実装 —— 削除した 4 か所（6 行）

### 5-1. ユーティリティクラスの private コンストラクタが投げる `AssertionError`（3 件・6 行）

| ファイル | 削除前 |
|---|---|
| `ConverterFileFilter.java:38`-`:39` | `private ConverterFileFilter() { throw new AssertionError("ConverterFileFilter は static 専用です"); }` |
| `ConverterPathResolver.java:25`-`:26` | `private ConverterPathResolver() { throw new AssertionError("ConverterPathResolver は static 専用です"); }` |
| `TestDataConverter.java:34`-`:35` | `private TestDataConverter() { throw new AssertionError("TestDataConverter は static 専用です"); }` |

**呼び出し側の全走査**（`a5f006c` 時点）:

```
$ git grep -n 'new ConverterFileFilter(' -- src      → 0 件
$ git grep -n 'new ConverterPathResolver(' -- src    → 0 件
$ git grep -n 'new TestDataConverter(' -- src        → 0 件

$ git grep -n 'ConverterFileFilter(' -- src
src/main/java/nablarch/test/tool/converter/ConverterFileFilter.java:38:    private ConverterFileFilter() {
$ git grep -n 'ConverterPathResolver(' -- src
src/main/java/nablarch/test/tool/converter/ConverterPathResolver.java:25:    private ConverterPathResolver() {
$ git grep -n 'TestDataConverter(' -- src
src/main/java/nablarch/test/tool/converter/TestDataConverter.java:34:    private TestDataConverter() {
```

いずれも定義 1 件のみで呼び出しが無い。処置は本体（`throw` 文）の削除で、コンストラクタ自体と
「ユーティリティクラスにつきインスタンス化不可。」の Javadoc は残す。

**同じリポジトリの先例**: `GroupIdNotation.java:38`-`:39` と `DirectiveUtil.java:20`-`:22`（いずれも `a5f006c` 時点）は、
どちらも本体が空の private コンストラクタで「インスタンス化させない」を表している。3 件をこの形へ揃えた。
（空の private コンストラクタは JaCoCo 0.8.8 のユーティリティクラスフィルタで計測対象から外れるため、
削除後は未達として現れない。実際 `GroupIdNotation` と `DirectiveUtil` は着手前の未達一覧に出ていない。）

### 5-2. `XlsFormatWriter#isMarkerColumn` の `null` 判定（分岐 1）

```java
-        return columnName != null && columnName.startsWith("[") && columnName.endsWith("]");
+        return columnName.startsWith("[") && columnName.endsWith("]");
```

**呼び出し側の全走査**:

```
$ git grep -n 'isMarkerColumn(' -- src
src/main/java/nablarch/test/tool/converter/xls/BlockLayout.java:102:    boolean isMarkerColumn(int column) {   （別メソッド。int を取る）
src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java:274:            if (isMarkerColumn(columns.get(c))) {
src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java:524:                    fill = layout.isMarkerColumn(c) ? Fill.MARKER   （BlockLayout 側）
src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java:588:    private static boolean isMarkerColumn(String columnName) {
```

`String` 版の呼び出しは `:274` の 1 か所だけで、引数は `ColumnRowDataBlock#getColumnNames()` の要素である。
カラム名の `null` は中間モデルの生成時に拒否される（`ColumnRowDataBlock.java:87` の
`ModelPreconditions.requireNoNulls("カラム名リスト", columnNames)`）ため、`null` はここへ届かない。
削除前の Javadoc も「ここへは届かない」と書いていた。**番人は書き出し側でなく中間モデルの生成時に置く**
（Decisions「不正値は書き出し側でなく中間モデルの生成時に拒否する」・ユーザー確定 2026-08-19）に従い、
書き出し側の判定を外して Javadoc の `@param` へ前提として残した。

---

## 6. (c) 到達不能 —— 残る 29 行 / 8 分岐（user 判断待ち。コメントは入れていない）

行番号はいずれも本タスク完了時点（`ed7c6d9`）の `src/main`。

### C1. 本体テンプレートの抽象メソッド実装（8 行・分岐 0）

`core/reader/TestCoreReaderAdapter.java` の `:415`・`:420`・`:424`・`:429`（`HeaderCollector`）と
`:509`・`:514`・`:518`・`:523`（`BodyLineCollector`）。中身は空実装 2 本と `return false;` 2 本。

**テストで実現できない理由**: 両クラスとも `parse(String)` を上書きしているため、本体テンプレートの
`doParse()` からこれらは呼ばれない。両クラスは `TestCoreReaderAdapter` の `private static final` な
入れ子クラスであり、テストから型を名指しして直接呼ぶこともできない。

**残すべき理由**: 実装は必須である。`TestDataParsingTemplate` は `onReadLine`・`onTargetTypeFound`・
`isTargetType`・`shouldStopOnNextOne` を abstract として宣言している
（`nablarch-testing` の作業ツリー `245cc0c9`・`src/main/java/nablarch/test/core/reader/TestDataParsingTemplate.java:56`・
`:63`・`:73`・`:80`）。削除するとコンパイルできない。
ビルドが実際に使う `nablarch-testing-6-NEXT-SNAPSHOT.jar` でも同じ 4 本が abstract であることを
`javap -p nablarch.test.core.reader.TestDataParsingTemplate` で確認した。

### C2. YAML エントリが Map でない側（2 行・分岐 1）

`core/reader/YamlTestCoreAdapter.java:229`（`if (!(entryObj instanceof Map))`）・`:230`（`continue;`）。

**テストで実現できない理由**: この経路の入力は `loadRawMap` → `YamlLoader.load` を通ったものだけで、
同メソッドはスキーマ検証に通らない YAML を返さずに例外にする
（`nablarch-testing-yaml@8773796`・`src/main/java/nablarch/test/core/reader/yaml/YamlLoader.java:154`-`:156`）。
**実測**: 送信同期セクションの先頭にスカラを 1 件置いた YAML を `readSendSyncMessages(..., null, ...)` で
読ませると `YamlSchemaValidationException: $.expected_request_header_messages[0]: string が見つかりました、
object が予期されました` になり、`defaultGroupOnlyYaml` へ到達しない（本タスク中に実行して確認。
到達しないことが分かったのでテスト・フィクスチャとも残していない）。

### C3. 同名ブック検査の `Files.list` が投げる `IOException`（2 行・分岐 0）

`tool/converter/ConverterFileFilter.java:184`（`} catch (IOException e) {`）・`:186`（`throw new UncheckedIOException(...)`）。

**テストで実現できない理由**: `dir` は直前の `Files.walk` が返したブックの親ディレクトリであり、
走査できた＝そのディレクトリは読める。`Files.list` が失敗するのは、走査と `Files.list` の間に
権限か存在が変わった場合だけで、テストから決定的に作れない。
（同じ形の `catch` が `find` と `findYamlDirs` にもあったが、そちらは**入力ルート自体**を
`chmod 000` にすれば `Files.walk` が呼び出し箇所で `AccessDeniedException` を投げるため、(a) として
テストで解消した。`requireNoSameNameBook` は走査が成功していることが前提なので同じ手が使えない。）

**残すべき理由**: `Files.list` は検査例外 `IOException` を宣言するため、`catch` 自体を消せない。

### C4. 列挙を尽くした `switch` の `default`（6 行・分岐 3）

| 箇所 | 尽くしている列挙 |
|---|---|
| `tool/converter/DataFormat.java:41`（`switch`）・`:48`（`throw`） | `DataFormat` は `XLS`・`YAML` の 2 値。両方を上で分岐済み |
| `tool/converter/xls/Styles.java:74`・`:92` | `Fill` は 6 値。`NONE` は直前の `if (fill != Fill.NONE)` で除かれ、残る 5 値を上で分岐済み |
| `tool/converter/yaml/YamlFormatWriter.java:488`・`:505` | `DataType` は 14 値。`DEFAULT` は `TestDataBlock` が生成時に拒否し（`issues.md` XLS-20）、残る 13 値を上で分岐済み |

**テストで実現できない理由**: `default` へ入る列挙値が存在しない。

**残すべき理由**: `switch` の網羅性は Java 17 ではコンパイル時に保証されないため、`default` を落とすと
戻り値が確定せずコンパイルできない。3 か所とも「将来 列挙値が追加されたときの安全網」であることを
本文コメントに明記している。

### C5. sealed 階層に対する `instanceof` 連鎖の末尾（4 行・分岐 2）

| 箇所 | 内容 |
|---|---|
| `tool/converter/xls/XlsFormatWriter.java:218`・`:222` | `layout(TestDataBlock)` の連鎖末尾と `throw new IllegalArgumentException` |
| `tool/converter/yaml/YamlFormatWriter.java:139`・`:143` | `emitBlock(...)` の連鎖末尾と `throw new IllegalArgumentException` |

**テストで実現できない理由**: `TestDataBlock` は sealed で、許可された派生型は上ですべて判定している。
連鎖に入らない型のインスタンスを作れない。

**残すべき理由**: `instanceof` 連鎖にはコンパイル時の網羅性保証が無く、`XlsFormatWriter#layout` は
戻り値を返すため末尾の `throw` を落とすとコンパイルできない。2 か所とも
「sealed 階層が将来変更された場合のランタイム安全網」であることを本文コメントに明記している。

### C6. 送信同期タイプ判定の偽側（1 行・分岐 1）

`tool/converter/xls/XlsFormatReader.java:128`（`} else if (XlsDataTypeUtil.isSendSyncType(type)) {`）の偽側。

**テストで実現できない理由**: `readHeaders` はマーカー行から取れた `DataType` だけを返し、
`DataType.DEFAULT` は返さない（`TestCoreReaderAdapter.java:397`-`:398` で読み飛ばす）。届く型は残り 13 値で、
テーブル系 3（`SETUP_TABLE_DATA`・`EXPECTED_TABLE_DATA`・`EXPECTED_COMPLETED`）・ファイル系 4
（`SETUP_FIXED`・`EXPECTED_FIXED`・`SETUP_VARIABLE`・`EXPECTED_VARIABLE`）・`LIST_MAP`・`MESSAGE`・
送信同期 4（`EXPECTED_REQUEST_HEADER_MESSAGES`・`EXPECTED_REQUEST_BODY_MESSAGES`・
`RESPONSE_HEADER_MESSAGES`・`RESPONSE_BODY_MESSAGES`）で 13 を尽くしている
（列挙の全 14 値は `nablarch-testing` の作業ツリー `245cc0c9`・
`src/main/java/nablarch/test/core/reader/DataType.java:11`-`:56`。ビルドが使う
`nablarch-testing-6-NEXT-SNAPSHOT.jar` の `DataType` も `javap` で 14 定数であることを確認した）。
どの分岐にも当たらない型が存在しない。

**残すべき理由**: 条件を落として `else` にすると、将来 `DataType` に値が増えたとき、その型が黙って
送信同期ブロックとして読まれる。**ここは C4・C5 と違ってコンパイル上は `else` にできるため、
残すか外すかは判断の余地がある。**

### C7. スキーマリソースのロード失敗（4 行・分岐 1）

`tool/converter/yaml/YamlTestDataValidator.java:59`（`if (in == null)`）・`:60`・`:63`（`} catch (IOException e) {`）・`:64`。
いずれも static 初期化子。

**テストで実現できない理由**: `SCHEMA_RESOURCE`（`/nablarch/test/ntf-testdata-yaml-schema.json`）は
コンパイルスコープ依存 `nablarch-testing-yaml` の jar に同梱されており、テストと同じクラスローダで必ず解決する
（`~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT.jar`
のエントリ `nablarch/test/ntf-testdata-yaml-schema.json` を `unzip -l` で確認）。
`getResourceAsStream` が `null` を返す状態も、jar 内リソースの読み込みが `IOException` になる状態も、
同じ JVM のテストからは作れない。

**残すべき理由**: `InputStream#close`（try-with-resources）が `IOException` を宣言するため `catch` は必須。
`in == null` の判定を外すと、リソースが欠けた配布物で `NullPointerException` になり原因が読めなくなる。

### C8. スキーマ検証器が投げる非検査例外（2 行・分岐 0）

`tool/converter/yaml/YamlTestDataValidator.java:152`（`} catch (RuntimeException e) {`）・`:154`。

**テストで実現できない理由**: `schema.validate` へ届く入力は、直前の `parseYaml` を通ったものだけである。
`parseYaml` は snakeyaml-engine 3.0.1（YAML 1.2・`allowDuplicateKeys=false`）で、networknt が内部で使う
jackson-dataformat-yaml 2.18.3（snakeyaml 2.3）より**厳しい**。
**実測**: 6 種の異常入力——`!!int` に非数値／`!!binary` に不正 base64／再帰アンカー／未知タグ
（`!!python/object:foo`）／エイリアス爆発（非スカラ別名 50 超）／YAML 1.2 固有の終端記法——をこの
リンタに掛けたが、いずれも `parseYaml` の側で捕まって `[V-YAML]` になり、`schema.validate` へ届かなかった
（本タスク中に実行して確認。到達しないことが分かったのでテストは残していない）。

**残すべき理由**: コメントが述べるとおり、リンタは全ファイルを走査し切ることが役目であり、
検証器の非検査例外で途中停止すると残りのファイルが検査されない。

---

## 7. (a) テスト不足 —— 追加した 28 件と担保した未達

追加後 `Tests run: 710`（着手前 682 ＋ 28）。`@Ignore` 0 件。

| # | テスト | 担保した未達（着手前の `file:line`） |
|---|---|---|
| 1 | `XlsOutputConfigTest#allFieldsSpecified_appliesEveryValue` | `XlsOutputConfig.java` の setter 9 本（`:70`・`:71`・`:80`・`:81`・`:85`・`:86`・`:90`・`:91`・`:95`・`:96`・`:105`・`:106`・`:110`・`:111`・`:115`・`:116`）と `toExcelFormatConfig` の `!= null` 真側 9 分岐（`:134`・`:135`・`:140`・`:141`・`:143`・`:144`・`:146`・`:147`・`:149`・`:150`・`:153`・`:155`・`:156`・`:158`・`:159`・`:161`・`:162`） |
| 2 | `StubDbInfoTest#getColumnType_alwaysReturnsVarcharRegardlessOfTableAndColumn` | （既到達の再確認。番人 9 本と対にして意味を成すため同居させた） |
| 3 | `StubDbInfoTest#writePathMethods_allThrowUnsupportedOperationNamingTheMethod` | `StubDbInfo.java:32`・`:44`・`:49`・`:54`・`:59`・`:64`・`:69`・`:74`・`:79`・`:84` |
| 4 | `GroupIdNotationTest#rawGroupId_isWrappedInBrackets` | （既到達の再確認） |
| 5 | `GroupIdNotationTest#emptyRawGroupId_staysEmpty` | （既到達の再確認） |
| 6 | `GroupIdNotationTest#nullRawGroupId_becomesEmpty` | `GroupIdNotation.java:52` の `rawGroupId == null` 真側 |
| 7 | `DirectiveUtilTest#nullValue_staysNullAndBypassesValueMapper` | `DirectiveUtil.java:50` の `value == null` 真側 |
| 8 | `TestCoreFileAdapterTest#getTypes_returnsNullWhenFragmentHasNoTypeRow` | `FragmentView.java:51` の `types == null` 真側 |
| 9 | `ConverterFileFilterTest#findXlsFilesAcceptsBookWithoutParentDirectory` | `ConverterFileFilter.java:174` の `dir == null` 真側・`:175` |
| 10 | `ConverterFileFilterTest#findXlsFilesWrapsWalkFailure` | `ConverterFileFilter.java:107`・`:109` |
| 11 | `ConverterFileFilterTest#findYamlDirsWrapsWalkFailure` | `ConverterFileFilter.java:81`・`:83` |
| 12 | `FormatHandlerBoundaryTest#xlsRoundTripsThroughPathWithoutParentDirectory` | `XlsFormatWriter.java:123` の `parent == null` 側・`XlsFormatHandler.java:77` の `parent == null` 側 |
| 13 | `FormatHandlerBoundaryTest#yamlWritesThroughPathWithoutParentDirectory` | `YamlFormatWriter.java:84` の `parent == null` 側 |
| 14 | `FormatHandlerBoundaryTest#yamlReadWrapsDirectoryOpenFailure` | `YamlFormatHandler.java:86`・`:88` |
| 15 | `TestCoreReaderAdapterTest#readHeadersKeepsUnclosedBracketAsPartOfGroupId` | `TestCoreReaderAdapter.java:315` の `marker.charAt(last) == ']'` 偽側 |
| 16 | `TestCoreReaderAdapterTest#readHeadersKeepsTrailingBracketWhenNotOpened` | `TestCoreReaderAdapter.java:315` の `marker.charAt(0) == '['` 偽側 |
| 17 | `TestCoreReaderAdapterTest#readBlockBodyLinesTreatsTypeNamedRowWithoutEqualsAsData` | `TestCoreReaderAdapter.java:489` の `groupId != null` 偽側 |
| 18 | `XlsFormatWriterTest#wrapsOnlyValuesQuotedOnBothEnds` | `XlsFormatWriter.java:702` の「半角の先頭だけ一致」側・`:703` の全角 3 分岐 |
| 19 | `XlsFormatWriterTest#writesFileDataRowWithNoCellsAsMarkerColumnOnly` | `XlsFormatWriter.java:444` の `valueCells.isEmpty()` 真側 |
| 20 | `XlsFormatReaderTest#readsListMapOnlyOncePerIdentifier` | `XlsFormatReader.java:118` の `processed.add` 偽側 |
| 21 | `XlsFormatReaderTest#warnsOncePerDuplicateNameAndFallsBackToWholeResourceName` | `XlsFormatReader.java:647` の `warned.add` 偽側・`:715` と `:726` の `slash < 0` 真側 |
| 22 | `XlsFormatReaderAdapterContractTest#skipsMessageBlockWhenAdapterHasNoMessage` | `XlsFormatReader.java:232` の `message == null` 真側・`:235`・`:124` の `block != null` 偽側 |
| 23 | `XlsFormatReaderAdapterContractTest#failsWhenNameRowIsAbsentFromBodyLines` | `XlsFormatReader.java:341` の `idx < bodyLines.size()` 偽側・`:362` の `idx >= bodyLines.size()` 真側・`:363` |
| 24 | `XlsFormatReaderAdapterContractTest#failsWhenBodyLinesAreShorterThanFragment` | `XlsFormatReader.java:447` の `idx >= bodyLines.size()` 真側・`:448` |
| 25 | `XlsFormatReaderAdapterContractTest#passesNullTypeToModelWhenTypeRowIsShorterThanNames` | `XlsFormatReader.java:397` の `i < originalTypes.size()` 偽側 |
| 26 | `XlsFormatReaderAdapterContractTest#passesNullLengthToModelWhenLengthRowIsShorterThanNames` | `XlsFormatReader.java:329` の `recordType == null` 真側・`:398` の `i < originalLengths.size()` 偽側 |
| 27 | `XlsFormatReaderAdapterContractTest#skipsRawLineWithNoCellsWhenLocatingNameRow` | `XlsFormatReader.java:502` の `list.isEmpty()` 真側 |
| 28 | `XlsFormatReaderAdapterContractTest#failsWhenLaterFragmentNameRowDoesNotMatch` | `XlsFormatReader.java:362` の `!tail(...).equals(names)` 真側 |

### 到達に使った手立て（新しく持ち込んだもの）

- **`ConverterFileFilterTest`（#10・#11）** —— 入力ルートを `Files.setPosixFilePermissions(dir, 空)` で
  読めなくする。`Files.isDirectory` は真のままだが `Files.walk` は呼び出し箇所で `AccessDeniedException`
  を投げる（`FileTreeIterator` のコンストラクタがディレクトリを開けない場合を致命的として送出するため）。
  `@After` ではなく `finally` で `rwx------` へ戻す。**POSIX 権限に依存するため、root 実行や
  権限を持たないファイルシステムでは前提が崩れる。**
- **`ConverterFileFilterTest`（#9）** —— 入力ルートに空文字パス（`Paths.get("")`）を渡す。
  `Files.walk` はこのとき直下のエントリを親を持たない相対パス（`x.xlsx`）で返す。
  対象ファイルはプロセスのカレント直下に置き、`finally` で消す。
- **`FormatHandlerBoundaryTest`（#12・#13）** —— `basePath` に空文字を渡す。
  `Paths.get("", "x.xlsx")` は親を持たない `x.xlsx` になる。書き出し先はカレント直下で、`finally` で消す。
- **`XlsFormatReaderAdapterContractTest`（#22〜#28）** —— `XlsFormatReader` が持つテスト用の注入口
  （`XlsFormatReader(TestCoreReaderAdapter)`）を使い、`TestCoreReaderAdapter` を継承して
  `readFiles`／`readMessage`／`readBlockBodyLines` だけを差し替える。マーカー行の走査（`readHeaders`）は
  本体実装のまま動かす。**実 Excel を入力にすると器と生行は同じシートから作られるため必ず一致し、
  「一致しないときどうなるか」は実 Excel からは動かせない。** ここだけがその境界を担保する。

---

## 8. 変異確認 —— 追加した 28 件が実装を壊すと落ちること

追加したテスト 1 件につき、そのテストが担保する実装を 1 か所だけ壊し、そのテストだけを走らせて
落ちることを実測した。手順は 変異適用 → `mvn -o test -Dtest=<クラス>#<メソッド>` → `git checkout -- src/main` の繰り返し。
**28 件すべてが落ちた（SURVIVED 0 件）。**

| # | 変異（`src/main` の 1 か所） | テスト | 結果 |
|---|---|---|---|
| 1 | `XlsOutputConfig.java`: `this.markerColumnColor = markerColumnColor;` → `this.markerColumnColor = null;` | `XlsOutputConfigTest#allFieldsSpecified_appliesEveryValue` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 2 | `StubDbInfo.java`: `return Types.VARCHAR;` → `return Types.INTEGER;` | `StubDbInfoTest#getColumnType_alwaysReturnsVarcharRegardlessOfTableAndColumn` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 3 | `StubDbInfo.java`: `throw notOnReadPath("isDateTypeColumn");` → `return false;` | `StubDbInfoTest#writePathMethods_allThrowUnsupportedOperationNamingTheMethod` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 4 | `GroupIdNotation.java`: `return rawGroupId == null \|\| rawGroupId.isEmpty() ? "" : "[" + rawGroupId + "]";` → `return rawGroupId == null \|\| rawGroupId.isEmpty() ? "" : rawGroupId;` | `GroupIdNotationTest#rawGroupId_isWrappedInBrackets` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 5 | `GroupIdNotation.java`: `return rawGroupId == null \|\| rawGroupId.isEmpty() ? "" : "[" + rawGroupId + "]";` → `return rawGroupId == null ? "" : "[" + rawGroupId + "]";` | `GroupIdNotationTest#emptyRawGroupId_staysEmpty` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 6 | `GroupIdNotation.java`: `return rawGroupId == null \|\| rawGroupId.isEmpty() ? "" : "[" + rawGroupId + "]";` → `return rawGroupId.isEmpty() ? "" : "[" + rawGroupId + "]";` | `GroupIdNotationTest#nullRawGroupId_becomesEmpty` | **KILLED(Tests run: 1, Failures: 0, Errors: 1)** |
| 7 | `DirectiveUtil.java`: `value == null ? null : valueMapper.map(entry.getKey(), value.toString()));` → `valueMapper.map(entry.getKey(), String.valueOf(value)));` | `DirectiveUtilTest#nullValue_staysNullAndBypassesValueMapper` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 8 | `FragmentView.java`: `return types == null ? null : Collections.unmodifiableList(types);` → `return Collections.unmodifiableList(types);` | `TestCoreFileAdapterTest#getTypes_returnsNullWhenFragmentHasNoTypeRow` | **KILLED(Tests run: 1, Failures: 0, Errors: 1)** |
| 9 | `ConverterFileFilter.java`: `if (dir == null) {␤            return;␤        }` → `（削除）` | `ConverterFileFilterTest#findXlsFilesAcceptsBookWithoutParentDirectory` | **KILLED(Tests run: 1, Failures: 0, Errors: 1)** |
| 10 | `ConverterFileFilter.java`: `throw new UncheckedIOException("failed to scan input directory: " + inputRoot, e);␤        }␤    }␤␤    /**␤     * パスが include／exclude を通過するか判定する。` → `return new ArrayList<>();␤        }␤    }␤␤    /**␤     * パスが include／exclude を通過するか判定する。` | `ConverterFileFilterTest#findXlsFilesWrapsWalkFailure` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 11 | `ConverterFileFilter.java`: `throw new UncheckedIOException("failed to scan input directory: " + inputRoot, e);␤        }␤        return dirs.stream()` → `return new ArrayList<>();␤        }␤        return dirs.stream()` | `ConverterFileFilterTest#findYamlDirsWrapsWalkFailure` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 12 | `XlsFormatHandler.java`: `return parent == null ? "." : parent.toString();` → `return parent.toString();` | `FormatHandlerBoundaryTest#xlsRoundTripsThroughPathWithoutParentDirectory` | **KILLED(Tests run: 1, Failures: 0, Errors: 1)** |
| 13 | `YamlFormatWriter.java`: `if (parent != null) {␤                    Files.createDirectories(parent);␤                }` → `Files.createDirectories(parent);` | `FormatHandlerBoundaryTest#yamlWritesThroughPathWithoutParentDirectory` | **KILLED(Tests run: 1, Failures: 0, Errors: 1)** |
| 14 | `YamlFormatHandler.java`: `throw new UncheckedIOException("failed to list YAML files: " + yamlDir, e);` → `return new ArrayList<>();` | `FormatHandlerBoundaryTest#yamlReadWrapsDirectoryOpenFailure` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 15 | `TestCoreReaderAdapter.java`: `if (last > 0 && marker.charAt(0) == '[' && marker.charAt(last) == ']') {` → `if (last > 0 && marker.charAt(0) == '[') {` | `TestCoreReaderAdapterTest#readHeadersKeepsUnclosedBracketAsPartOfGroupId` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 16 | `TestCoreReaderAdapter.java`: `if (last > 0 && marker.charAt(0) == '[' && marker.charAt(last) == ']') {` → `if (last > 0 && marker.charAt(last) == ']') {` | `TestCoreReaderAdapterTest#readHeadersKeepsTrailingBracketWhenNotOpened` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 17 | `TestCoreReaderAdapter.java`: `if (eq < 0) {␤            return null;␤        }` → `if (eq < 0) {␤            return "";␤        }` | `TestCoreReaderAdapterTest#readBlockBodyLinesTreatsTypeNamedRowWithoutEqualsAsData` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 18 | `XlsFormatWriter.java`: `return (value.startsWith("\"") && value.endsWith("\""))␤                \|\| (value.startsWith("”") && value.endsWith("”"));` → `return value.startsWith("\"") && value.endsWith("\"");` | `XlsFormatWriterTest#wrapsOnlyValuesQuotedOnBothEnds` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 19 | `XlsFormatWriter.java`: `if (!valueCells.isEmpty() && isAllBlank(valueCells)) {` → `if (isAllBlank(valueCells)) {` | `XlsFormatWriterTest#writesFileDataRowWithNoCellsAsMarkerColumnOnly` | **KILLED(Tests run: 1, Failures: 0, Errors: 1)** |
| 20 | `XlsFormatReader.java`: `} else if (type == DataType.LIST_MAP) {␤                if (processed.add(singleKey(type, header.getIdentifier()))) {` → `} else if (type == DataType.LIST_MAP) {␤                if (true) {` | `XlsFormatReaderTest#readsListMapOnlyOncePerIdentifier` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 21 | `XlsFormatReader.java`: `if (warned.add(name)) {` → `if (true) {` | `XlsFormatReaderTest#warnsOncePerDuplicateNameAndFallsBackToWholeResourceName` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
| 22 | `XlsFormatReader.java`: `if (message == null) {␤            // ヘッダスキャンで MESSAGE= マーカーを検出したが、本体パーサが同 ID のデータを見つけられない場合。␤            // 本体の MessageParser が空結果を返したとき adapter.read` → `（削除）` | `XlsFormatReaderAdapterContractTest#skipsMessageBlockWhenAdapterHasNoMessage` | **KILLED(Tests run: 1, Failures: 0, Errors: 1)** |
| 23 | `XlsFormatReader.java`: `if (idx >= bodyLines.size() \|\| !tail(bodyLines.get(idx)).equals(names)) {` → `if (!tail(bodyLines.get(Math.min(idx, bodyLines.size() - 1))).equals(names) && false) {` | `XlsFormatReaderAdapterContractTest#failsWhenNameRowIsAbsentFromBodyLines` | **KILLED(Tests run: 1, Failures: 0, Errors: 1)** |
| 24 | `XlsFormatReader.java`: `if (idx >= bodyLines.size()) {␤            throw new IllegalStateException(␤                    "器の断片構造と生行が不整合です。断片 names=" + names + " の" + rowKind +` → `if (false) {␤            throw new IllegalStateException(␤                    "器の断片構造と生行が不整合です。断片 names=" + names + " の" + rowKind + "が生行に存在しません。");␤ ` | `XlsFormatReaderAdapterContractTest#failsWhenBodyLinesAreShorterThanFragment` | **KILLED(Tests run: 1, Failures: 0, Errors: 1)** |
| 25 | `XlsFormatReader.java`: `String type = i < originalTypes.size() ? originalTypes.get(i) : null;` → `String type = originalTypes.get(i);` | `XlsFormatReaderAdapterContractTest#passesNullTypeToModelWhenTypeRowIsShorterThanNames` | **KILLED(Tests run: 1, Failures: 0, Errors: 1)** |
| 26 | `XlsFormatReader.java`: `String length = originalLengths != null && i < originalLengths.size() ? originalLengths.get(i) : null;` → `String length = originalLengths != null ? originalLengths.get(i) : null;` | `XlsFormatReaderAdapterContractTest#passesNullLengthToModelWhenLengthRowIsShorterThanNames` | **KILLED(Tests run: 1, Failures: 0, Errors: 1)** |
| 27 | `XlsFormatReader.java`: `return list.isEmpty() ? list : list.subList(1, list.size());` → `return list.subList(1, list.size());` | `XlsFormatReaderAdapterContractTest#skipsRawLineWithNoCellsWhenLocatingNameRow` | **KILLED(Tests run: 1, Failures: 0, Errors: 1)** |
| 28 | `XlsFormatReader.java`: `if (idx >= bodyLines.size() \|\| !tail(bodyLines.get(idx)).equals(names)) {` → `if (idx >= bodyLines.size()) {` | `XlsFormatReaderAdapterContractTest#failsWhenLaterFragmentNameRowDoesNotMatch` | **KILLED(Tests run: 1, Failures: 1, Errors: 0)** |
