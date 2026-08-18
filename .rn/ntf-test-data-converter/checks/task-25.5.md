# task-25.5 Completion Check

本ファイルは **#25.5 の最終状態**を記録する。到達までの経過は残さない。

対象は `coverage/issues.md` の 7 課題（YML-02 / YML-03 / YML-08 / YML-12 / XLS-01 / XLS-06 / XLS-16）。
起点は `8c327d0`（#25 のチェックオフ）。**本体（nablarch-testing）・yaml（nablarch-testing-yaml）には
1 行も書き込んでいない**（`git diff 8c327d0..HEAD --stat` の対象は本リポジトリのみ）。

---

## 実装（2026-08-14）

### コミット（17 本）

| # | SHA | 内容 |
|---|---|---|
| 1 | `cdbcf63` | `@Ignore` 付与（緑の嘘を消す）10 本 |
| 2 | `36e94a4` | YML-02 の修正 |
| 3 | `aec82f2` | YML-12 の修正 |
| 4 | `e80a4dd` | XLS-16 の修正 |
| 5 | `5721ecd` | XLS-06 の修正 |
| 6 | `6c8d90e` | YML-08 の修正 |
| 7 | `9e8fd6b` | YML-03 の待機テスト設置（修正はしない） |
| 8 | `2f21bce` | XLS-01 のアサート書き直し |
| 9 | `b3af6e6` | `issues.md` の「NTF 仕様としての判定」欄追加（全 35 件） |
| 10 | `b689856` | YML-08 の「未確認」を実測で埋める |
| 11 | `2e1413e` | `inventory.md` の件数の導き直し |
| 12 | `51dbca0` | `YamlTestCoreAdapter.java` の生 NUL バイトを Java の 8 進エスケープへ置き換え（挙動不変） |
| 13 | `520e890` | レビュー B-1／B-2／B-3 —— 番兵の値を素の `default-group` へ、シート名 null を同じ番人＋専用メッセージへ、`YamlFormatReader` の重複計算を削除 |
| 14 | `b6be795` | レビュー A-3／A-6 —— `YamlFormatWriter`／`DirectiveUtil` の Javadoc を現状へ合わせる |
| 15 | `ccf3c1a` | レビュー B-2 の担保テスト／B-5／A-4／A-5／B-4／C-5 |
| 16 | `8d781b8` | `defaultGroupOnlyYaml` のセクション不在ガードの担保テスト |
| 17 | `45194f9` | レビュー A-1／A-2／C-2〜C-6 の台帳反映（`issues.md`／`inventory.md`） |

1〜12 は 1 件 ＝ 1 コミット。13〜17 はレビュー指摘の対応で、内容の論理単位ごとに分けてある。
各コミット後に push した。force push はしていない。

### コミット 1 —「緑の嘘を消す」洗い出しの結果

**`@Ignore` を付けたのは 10 本**である。ユーザーからの目安「14 本」との差 4 本は、
**目安に届かなかったのではなく、判定基準に照らして 4 本を意図的に外した**結果である（下の「外したもの」）。
着手時点の `@Ignore` ／ `@Disabled` は 0 件だった。

| # | クラス | メソッド（コミット 1 時点の名前） | 課題 | 不具合の固定に当たる理由 |
|---|---|---|---|---|
| 1 | `XlsFormatReaderRealFileTest` | `readsOmittedRecordTypeAsEmptyStringFromRealBook` | XLS-06 | 空セルが `""` になることを正として固定していた（契約は `null`） |
| 2 | `XlsFormatWriterInvalidOutputTest` | `truncatesSheetNameLongerThanExcelLimitSilently` | XLS-16 | 31 文字超が黙って切り詰められることを正として固定していた |
| 3 | `XlsFormatWriterInvalidOutputTest` | `writesSheetNameWhoseForbiddenCharacterIsRemovedByTruncation` | XLS-16 | 切り詰めで禁止文字が消え、例外にならず書き出されることを正として固定していた |
| 4 | `YamlFormatReaderInvalidInputTest` | `losesRecordSeparatorWrittenAsLiteralNewline` | YML-08 | `record-separator` が空文字になることを正として固定していた |
| 5 | `YamlFormatReaderInvalidInputTest` | `readsRecordSeparatorSymbolAsActualNewline` | YML-08 | シンボル `CRLF` が実改行のまま入る（辺①と非対称な）挙動を正として固定していた |
| 6 | `YamlFormatReaderRealFileTest` | `dropsSendSyncEntryWithoutGroupIdFromRealYaml` | YML-02 | `group_id` 省略エントリがブロックごと落ちることを正として固定していた |
| 7 | `YamlFormatReaderRealFileTest` | `dropsFwHeaderNamedRecordFromRealYaml` | YML-03 | `record_type: FW_HEADER` のレコードが捨てられることを正として固定していた |
| 8 | `YamlFormatReaderRealFileTest` | `dropsFwHeaderNamedRecordFromSendSyncInRealYaml` | YML-03 | 同上（送信系） |
| 9 | `YamlFormatWriterModelTest` | `writesFileBlockWithoutRecordsKeyWhenRecordsAreEmpty` | YML-12 | `records:` キーごと落とすことを正として固定していた |
| 10 | `YamlFormatWriterModelTest` | `failsToReadBackFileBlockWithoutRecords` | YML-12 | 書き出した YAML が読み戻せないことを正として固定していた |

**外したもの（`issues.md` の担保テスト欄に挙がっていたが `@Ignore` を付けなかった 8 本）**

| クラス#メソッド | 課題 | 外した判定 |
|---|---|---|
| `XlsFormatReaderCellTypeTest#readsTextFormattedNumericCellAsDoubleString` | XLS-01 | **アサートしているのが（NTF 仕様として）修正不要な挙動**。POI 由来で converter では直せず、判定も「対応不要」。コミット 8 でアサートを「担保」から「実挙動の記録」へ書き直した |
| `XlsFormatReaderTest#readFile_recordTypeOmitted_keepsNullRecordType` | XLS-06 | **正しい挙動を固定している**（in-memory 経路は元から `null`）。修正後も緑 |
| `XlsFormatWriterInvalidOutputTest#writesSheetNameOfExcelLimitLengthAsIs` | XLS-16 | **正しい挙動を固定している**（31 文字ちょうどはそのまま書かれる）。修正後も緑 |
| `XlsFormatWriterInvalidOutputTest#failsWhenSheetNamesDifferOnlyInCase` | XLS-16 | **正しい挙動を固定している**（3 文字同士の重複判定。文字数検査に掛からない）。修正後も緑 |
| `XlsFormatWriterInvalidOutputTest#failsWhenTruncatedSheetNamesCollide` | XLS-16 | **変更前後どちらも正しい**型。修正後は 32 文字が衝突判定へ到達しなくなるため、`@Ignore` ではなくコミット 4 で入力と期待値を書き直した（→ `failsWhenSameSheetNameOfLimitLengthIsUsedTwice`） |
| `XlsFormatWriterInvalidOutputTest#rejectsSheetNameWhoseForbiddenCharacterSurvivesTruncation` | XLS-16 | 同上（→ `rejectsSheetNameWhoseForbiddenCharacterIsAtTheLastPosition`。入力を 31 文字へ変えた） |
| `YamlFormatReaderInvalidInputTest#failsWhenFieldSeparatorIsWrittenAsActualTab` | YML-08 | **converter の修正では変わらない**。例外は値が converter へ届く前に本体 `DataFile#setDirective` で送出される。修正後も緑 |
| `YamlFormatWriterTest#serializeMessage_emptyBody_emitsIdOnly` ／ `#serialize_fieldWithNullType_omitsType` ／ `#serialize_recordWithEmptyFieldsAndRows_emitsEmptyFlowLists` | YML-12 | **正しい挙動を固定している**。修正はファイルブロックの `records` が空の場合にだけ効くため、メッセージ系・フィールド系のこの 3 本は挙動不変。修正後も緑（3 本で 1 行にまとめた） |

**取りこぼし 2 本（開示）**。コミット 1 の洗い出しは `issues.md` の担保テスト欄を起点にしたため、
欄に載っていない次の 2 本を取りこぼした。どちらもコミット 2／6 の赤で顕在化し、その場で期待値を直した
（`@Ignore` は付けていない）。

- `YamlFormatReaderTest#readSendSync_entryWithoutGroupId_isDropped`（YML-02 の in-memory 経路）
- `YamlFormatReaderInvalidInputTest#readsFieldSeparatorWrittenAsEscapedTabNotation`（YML-08 の `field-separator` 側）

### TDD — 5 件の赤（実行時の出力そのまま）

**YML-02**（`mvn -o test -Dtest=YamlFormatReaderRealFileTest#readsSendSyncEntryWithoutGroupIdAsDefaultGroupFromRealYaml`）

```
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.375 s <<< FAILURE! - in nablarch.test.tool.converter.yaml.YamlFormatReaderRealFileTest
[ERROR] readsSendSyncEntryWithoutGroupIdAsDefaultGroupFromRealYaml(nablarch.test.tool.converter.yaml.YamlFormatReaderRealFileTest)  Time elapsed: 0.347 s  <<< FAILURE!
java.lang.AssertionError: 

Expected: is <2>
     but: was <1>
	at nablarch.test.tool.converter.yaml.YamlFormatReaderRealFileTest.readsSendSyncEntryWithoutGroupIdAsDefaultGroupFromRealYaml(YamlFormatReaderRealFileTest.java:980)
```

**YML-12**（`-Dtest=YamlFormatWriterModelTest#writesEmptyRecordsListForFileBlockWithoutRecords+readsBackFileBlockWithEmptyRecords`）

```
[ERROR] Tests run: 2, Failures: 1, Errors: 1, Skipped: 0, Time elapsed: 0.625 s <<< FAILURE! - in nablarch.test.tool.converter.yaml.YamlFormatWriterModelTest
[ERROR] writesEmptyRecordsListForFileBlockWithoutRecords(nablarch.test.tool.converter.yaml.YamlFormatWriterModelTest)  Time elapsed: 0.501 s  <<< FAILURE!
java.lang.AssertionError: 

Expected: is "setup_files:
  - path: \"n.dat\"
    type: \"fixed\"
    directives:
      text-encoding: \"UTF-8\"
      file-type: \"Fixed\"
    records: []
"
     but: was "setup_files:
  - path: \"n.dat\"
    type: \"fixed\"
    directives:
      text-encoding: \"UTF-8\"
      file-type: \"Fixed\"
"
	at nablarch.test.tool.converter.yaml.YamlFormatWriterModelTest.writesEmptyRecordsListForFileBlockWithoutRecords(YamlFormatWriterModelTest.java:595)

[ERROR] readsBackFileBlockWithEmptyRecords(nablarch.test.tool.converter.yaml.YamlFormatWriterModelTest)  Time elapsed: 0.066 s  <<< ERROR!
nablarch.test.core.reader.yaml.YamlSchemaValidationException: 
YAML file failed schema validation: /tmp/junit12085338139288734687/td.yaml
$.setup_files[0]: 必須プロパティ 'records' が見つかりません
	at nablarch.test.tool.converter.yaml.YamlFormatWriterModelTest.writeAndReadBack(YamlFormatWriterModelTest.java:144)
	at nablarch.test.tool.converter.yaml.YamlFormatWriterModelTest.readsBackFileBlockWithEmptyRecords(YamlFormatWriterModelTest.java:809)
```

**XLS-16**（`-Dtest=XlsFormatWriterInvalidOutputTest`）

```
[ERROR] Tests run: 16, Failures: 2, Errors: 0, Skipped: 0, Time elapsed: 0.76 s <<< FAILURE! - in nablarch.test.tool.converter.xls.XlsFormatWriterInvalidOutputTest
[ERROR] rejectsSheetNameLongerThanExcelLimit(nablarch.test.tool.converter.xls.XlsFormatWriterInvalidOutputTest)  Time elapsed: 0.036 s  <<< FAILURE!
java.lang.AssertionError: expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown
	at nablarch.test.tool.converter.xls.XlsFormatWriterInvalidOutputTest.rejectsSheetNameLongerThanExcelLimit(XlsFormatWriterInvalidOutputTest.java:522)

[ERROR] rejectsSheetNameWhoseForbiddenCharacterWouldBeRemovedByTruncation(nablarch.test.tool.converter.xls.XlsFormatWriterInvalidOutputTest)  Time elapsed: 0.022 s  <<< FAILURE!
java.lang.AssertionError: expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown
	at nablarch.test.tool.converter.xls.XlsFormatWriterInvalidOutputTest.rejectsSheetNameWhoseForbiddenCharacterWouldBeRemovedByTruncation(XlsFormatWriterInvalidOutputTest.java:555)
```

**XLS-06**（`-Dtest=XlsFormatReaderRealFileTest#readsOmittedRecordTypeAsNullFromRealBook`）

```
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.496 s <<< FAILURE! - in nablarch.test.tool.converter.xls.XlsFormatReaderRealFileTest
[ERROR] readsOmittedRecordTypeAsNullFromRealBook(nablarch.test.tool.converter.xls.XlsFormatReaderRealFileTest)  Time elapsed: 0.479 s  <<< FAILURE!
java.lang.AssertionError: 

Expected: is null
     but: was ""
	at nablarch.test.tool.converter.xls.XlsFormatReaderRealFileTest.readsOmittedRecordTypeAsNullFromRealBook(XlsFormatReaderRealFileTest.java:875)
```

**YML-08**（`-Dtest=YamlFormatReaderInvalidInputTest`）

```
[ERROR] Tests run: 31, Failures: 3, Errors: 0, Skipped: 0, Time elapsed: 0.587 s <<< FAILURE! - in nablarch.test.tool.converter.yaml.YamlFormatReaderInvalidInputTest
[ERROR] readsRecordSeparatorWrittenAsLiteralNewlineAsNoneSymbol(nablarch.test.tool.converter.yaml.YamlFormatReaderInvalidInputTest)  Time elapsed: 0.013 s  <<< FAILURE!
java.lang.AssertionError: 
本体の trim() で空になった値を、辺①と同じ規則でシンボルへ戻す
Expected: is "NONE"
     but: was ""
	at nablarch.test.tool.converter.yaml.YamlFormatReaderInvalidInputTest.readsRecordSeparatorWrittenAsLiteralNewlineAsNoneSymbol(YamlFormatReaderInvalidInputTest.java:947)

[ERROR] readsRecordSeparatorSymbolAsSymbol(nablarch.test.tool.converter.yaml.YamlFormatReaderInvalidInputTest)  Time elapsed: 0.003 s  <<< FAILURE!
java.lang.AssertionError: 

Expected: is "CRLF"
     but: was "\r
"
	at nablarch.test.tool.converter.yaml.YamlFormatReaderInvalidInputTest.readsRecordSeparatorSymbolAsSymbol(YamlFormatReaderInvalidInputTest.java:981)

[ERROR] readsFieldSeparatorWrittenAsEscapedTabNotation(nablarch.test.tool.converter.yaml.YamlFormatReaderInvalidInputTest)  Time elapsed: 0.002 s  <<< FAILURE!
java.lang.AssertionError: 
器はタブ 1 文字へ変換するが、中間モデルへは 2 文字記法へ戻して入れる
Expected: is "\	"
     but: was "\	"
	at nablarch.test.tool.converter.yaml.YamlFormatReaderInvalidInputTest.readsFieldSeparatorWrittenAsEscapedTabNotation(YamlFormatReaderInvalidInputTest.java:1119)
```

（3 件目の `Expected` / `but` は端末上では同じに見えるが、期待は**バックスラッシュ ＋ `t` の 2 文字**、
実際は**実タブ 1 文字**である。）

**YML-03 の待機テスト（修正しないので赤のまま `@Ignore` する）**

```
[ERROR] Tests run: 24, Failures: 2, Errors: 0, Skipped: 0, Time elapsed: 0.713 s <<< FAILURE! - in nablarch.test.tool.converter.yaml.YamlFormatReaderRealFileTest
[ERROR] keepsFwHeaderNamedRecordInMessageFromRealYaml(nablarch.test.tool.converter.yaml.YamlFormatReaderRealFileTest)  Time elapsed: 0.015 s  <<< FAILURE!
java.lang.AssertionError: 
FW_HEADER 名のレコードも落とさない
Expected: is <1>
     but: was <0>
	at nablarch.test.tool.converter.yaml.YamlFormatReaderRealFileTest.keepsFwHeaderNamedRecordInMessageFromRealYaml(YamlFormatReaderRealFileTest.java:1014)

[ERROR] keepsFwHeaderNamedRecordInSendSyncFromRealYaml(nablarch.test.tool.converter.yaml.YamlFormatReaderRealFileTest)  Time elapsed: 0.006 s  <<< FAILURE!
java.lang.AssertionError: 
FW_HEADER 名のレコードも落とさない
Expected: is <2>
     but: was <1>
	at nablarch.test.tool.converter.yaml.YamlFormatReaderRealFileTest.keepsFwHeaderNamedRecordInSendSyncFromRealYaml(YamlFormatReaderRealFileTest.java:654)
```

### 削除した既存テスト

**単純に消したテストは 0 件**である。不具合の挙動を固定していた 13 本は、いずれも**同じ位置で
仕様どおりの期待値を持つテストへ置き換えた**（改名＋アサート書き直し）。**この置き換えでは
テストメソッド総数は変わっていない**（不具合修正のコミット群まで `8c327d0` と同じ 536）。
現在の `HEAD` が **540** なのは、レビュー指摘を受けて後から担保を 4 本足したためである
（`XlsFormatWriterInvalidOutputTest#rejectsNullSheetName` 1 本、`YamlTestCoreAdapterTest` の
デフォルトグループ 3 本）。

導出コマンド:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
for c in 8c327d0 HEAD; do
  printf "%s: %s\n" "$c" "$(git grep -c '^    @Test' $c -- 'src/test/**/*.java' | awk -F: '{s+=$NF} END {print s}')"
done
```

| 置き換え前（`8c327d0`） | 置き換え後（`HEAD`） | 課題 |
|---|---|---|
| `XlsFormatReaderRealFileTest#readsOmittedRecordTypeAsEmptyStringFromRealBook` | `#readsOmittedRecordTypeAsNullFromRealBook` | XLS-06 |
| `XlsFormatWriterInvalidOutputTest#truncatesSheetNameLongerThanExcelLimitSilently` | `#rejectsSheetNameLongerThanExcelLimit` | XLS-16 |
| `#writesSheetNameWhoseForbiddenCharacterIsRemovedByTruncation` | `#rejectsSheetNameWhoseForbiddenCharacterWouldBeRemovedByTruncation` | XLS-16 |
| `#failsWhenTruncatedSheetNamesCollide` | `#failsWhenSameSheetNameOfLimitLengthIsUsedTwice` | XLS-16 |
| `#rejectsSheetNameWhoseForbiddenCharacterSurvivesTruncation` | `#rejectsSheetNameWhoseForbiddenCharacterIsAtTheLastPosition` | XLS-16 |
| `YamlFormatReaderInvalidInputTest#losesRecordSeparatorWrittenAsLiteralNewline` | `#readsRecordSeparatorWrittenAsLiteralNewlineAsNoneSymbol` | YML-08 |
| `#readsRecordSeparatorSymbolAsActualNewline` | `#readsRecordSeparatorSymbolAsSymbol` | YML-08 |
| `YamlFormatReaderRealFileTest#dropsSendSyncEntryWithoutGroupIdFromRealYaml` | `#readsSendSyncEntryWithoutGroupIdAsDefaultGroupFromRealYaml` | YML-02 |
| `YamlFormatReaderTest#readSendSync_entryWithoutGroupId_isDropped` | `#readSendSync_entryWithoutGroupId_isReadAsDefaultGroup` | YML-02 |
| `YamlFormatReaderRealFileTest#dropsFwHeaderNamedRecordFromRealYaml` | `#keepsFwHeaderNamedRecordInMessageFromRealYaml`（`@Ignore`） | YML-03 |
| `#dropsFwHeaderNamedRecordFromSendSyncInRealYaml` | `#keepsFwHeaderNamedRecordInSendSyncFromRealYaml`（`@Ignore`） | YML-03 |
| `YamlFormatWriterModelTest#writesFileBlockWithoutRecordsKeyWhenRecordsAreEmpty` | `#writesEmptyRecordsListForFileBlockWithoutRecords` | YML-12 |
| `#failsToReadBackFileBlockWithoutRecords` | `#readsBackFileBlockWithEmptyRecords` | YML-12 |

`XlsFormatReaderCellTypeTest#readsTextFormattedNumericCellAsDoubleString`（XLS-01）は名前を変えず、
アサートだけを「担保」から「実挙動の記録」へ書き直した。

### `src/main` で変更したファイル

`git diff --stat 8c327d0 HEAD -- src/main` で確認できる。

| ファイル | 課題ID | 変更理由 |
|---|---|---|
| `src/main/java/nablarch/test/core/reader/YamlTestCoreAdapter.java` | **YML-02** | `notation:254`「グループIDを省略した場合は…デフォルトグループが対象になる」に反し、器の読み取り側が `group_id` 省略エントリを拾えなかったため |
| `src/main/java/nablarch/test/tool/converter/yaml/YamlFormatReader.java` | **YML-02**／**YML-08** | (YML-02) `rawGroupsInOrder` が `group_id` 非 null のエントリしか列挙せず、省略エントリがブロックごと落ちていた。(YML-08) ディレクティブ値を素通ししており、同じ入力表記が辺①と別の中間モデル値になっていた |
| `src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java` | **YML-12** | `records` が空のファイルブロックで `records:` キーごと落としており、書き出した YAML が本体スキーマ（`$defs.file_data.required` が `records` を含む）を満たさず読み戻せなかった |
| `src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java` | **XLS-16** | `notation:588` のとおりシート名は呼び出し側の引き当てキーであり、POI が 31 文字超を黙って切り詰めると引けなくなる。`createSheet` の前に文字数を検査して落とすようにした |
| `src/main/java/nablarch/test/tool/converter/xls/XlsFormatReader.java` | **XLS-06**／**YML-08** | (XLS-06) 中間モデルの契約 `RecordLayout.java:26`「省略時は `null`」に反し空セルを `""` にしていた。(YML-08) 逆正規化を `DirectiveUtil` へ切り出して辺②と共有した |
| `src/main/java/nablarch/test/tool/converter/DirectiveUtil.java` | **YML-08** | 辺①・辺②が同じ規則でディレクティブ値を逆正規化するための共有ユーティリティ。**クラス自体は #25.5 より前からある**（`git cat-file -e 8c327d0:src/main/java/nablarch/test/tool/converter/DirectiveUtil.java` → exit 0）。#25.5 が足したのは `normalizeSeparator` 1 メソッドである |

### カバレッジ（JaCoCo 実測・2026-08-14）

```sh
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes \
  && JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec \
  && awk -F, 'NR > 1 && ($3 == "XlsFormatWriter" || $3 == "YamlFormatWriter" || $3 == "YamlFormatReader" || $3 == "DirectiveUtil" || $3 == "YamlTestCoreAdapter") \
        { print $3 " line " $9 "/" ($8 + $9) " branch " $7 "/" ($6 + $7) }' \
       target/site/jacoco/jacoco.csv
```

| クラス | #25.5 前 | #25.5 後 | 未到達の増減 |
|---|---|---|---|
| `XlsFormatWriter` | `branch 97/100`・行 1 未到達 | `line 157/158 branch 101/104` | 分岐の未到達は **3 のまま**。XLS-16 の修正でいったん 4 へ増えた `requireValidSheetNameLength` の `sheetName == null` 側は、レビュー B-2 で null を同じ番人が落とすようにし担保テスト `rejectsNullSheetName` を足したため閉じた |
| `YamlFormatWriter` | `line 158/159 branch 89/92` | `line 160/161 branch 91/94` | 変わらず（YML-12 で増えた 2 分岐は到達済み） |
| `YamlFormatReader` | `line 201/201 branch 108/108` | `line 200/200 branch 106/106` | 未到達なし。レビュー B-3 で重複計算していた `formattedGroup` を削ったため、行が 1・分岐が 2 減った |
| `DirectiveUtil` | `line 20/20 branch 17/18`（#25.5 前も同じ。クラスは 8c327d0 に存在する） | `line 20/20 branch 17/18` | 未到達 1。**#25.5 が足した `normalizeSeparator` ではなく、既存の `toStringDirectives`（`DirectiveUtil.java:45` の三項演算子の `null` 側）である**（`jacoco.xml` の行別 `mb` で確認） |
| `YamlTestCoreAdapter` | — | `line 49/50 branch 18/19` | YML-02 で足した `defaultGroupOnlyYaml` の未到達 2 分岐のうち 1 件（対象セクション不在）はレビュー対応でテストを足して閉じた。残る 1 件（エントリが `Map` でない）は本体スキーマ検証が先に落とすため到達不能である（`inventory.md` §0.1-2 に実測つきで開示） |

未到達分岐の内訳と、いずれも軸A〜F の要素ではない根拠は `inventory.md` §0.1-2／§3.1-3／§4.1-2 の「開示」。

---

## 追補 — YML-12 4形目（`FieldDef.type` が `null`）の修正（2026-08-18）

中間モデルの契約として **`FieldDef.type` は必須（`null` 不可）** を宣言し、書き出しの 2 辺
（辺③ `XlsFormatWriter` ／ 辺④ `YamlFormatWriter`）が `null` を `IllegalArgumentException` で弾くようにした。
番人は**モデルのコンストラクタには置かない**（steering「`RecordLayout` コンストラクタに番人は置かない」と同じ理由。
写せない値を止める場所は書き出し辺である）。**検査するのは `null` だけで、空文字 `""` は弾かない。**
`length` は従来どおり省略可。**2 形目（`MessageDataBlock.records` 空）には手を触れていない。**

### 判定の根拠（解説書と本体スキーマの明文のみ。実装の挙動は根拠にしていない）

```sh
cd /home/tie303177/work/nablarch/nablarch-document
git log -1 --format='%H %ad' 30a8271
git show 30a8271:ja/development_tools/testing_framework/implementation/testdata_notation.rst | sed -n '883p;885p;888p'
```

出力（`30a8271f6ada3259b014618abea72a588db043d9 Tue Aug 18 08:54:15 2026 +0900`）を実際に開いて一致を確認した。

- `notation:883`（`30a8271` 時点。`df7bff7` では 881 行目）——「固定長ファイルでは、フィールド名称・データ型・フィールド長の3リストが同サイズで必須であり」／「可変長ファイルでは、フィールド名称・データ型の2リストが同サイズで必須であり」
- `notation:885`（同）——「ファイルデータの記述時にエラーとなるのは、以下のようなケースである。」
- `notation:888`（同。`df7bff7` では 886 行目）——「- フィールド名称リストまたはデータ型リストが未指定または空である」
- YAML 本体スキーマ `nablarch-testing-yaml`（`8e1ea76`）`src/main/resources/nablarch/test/ntf-testdata-yaml-schema.json` の `$defs.field_def.required` ＝ `["name", "type"]`（`python3` で JSON を読み出して確認）

**Excel 記法・YAML 記法のいずれも型の無いフィールド定義を認めていない**。したがってこれは中間モデルだけが
保持できる「契約の穴」であり、辺③④の両方で塞ぐ。

### TDD — 赤（実行時の出力そのまま）

修正前の `src/main`（`9f2223e` 時点の `XlsFormatWriter` ／ `YamlFormatWriter`）に、新しい番人テストだけを載せて実行した。

```sh
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o test -Djacoco.skip=true \
  -Dtest='XlsFormatWriterTest,YamlFormatWriterTest' -DfailIfNoTests=false
```

```
[ERROR] Tests run: 37, Failures: 2, Errors: 0, Skipped: 0, Time elapsed: 1.319 s <<< FAILURE! - in nablarch.test.tool.converter.yaml.YamlFormatWriterTest
[ERROR] serialize_fieldWithNullTypeInFileBlock_rejected(nablarch.test.tool.converter.yaml.YamlFormatWriterTest)  Time elapsed: 0.03 s  <<< FAILURE!
java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

[ERROR] serialize_fieldWithNullTypeInMessageBlock_rejected(nablarch.test.tool.converter.yaml.YamlFormatWriterTest)  Time elapsed: 0.004 s  <<< FAILURE!
java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

[INFO] Running nablarch.test.tool.converter.xls.XlsFormatWriterTest
[ERROR] Tests run: 44, Failures: 2, Errors: 0, Skipped: 0, Time elapsed: 3.868 s <<< FAILURE! - in nablarch.test.tool.converter.xls.XlsFormatWriterTest
[ERROR] rejectsFieldWithoutTypeInMessageBlock(nablarch.test.tool.converter.xls.XlsFormatWriterTest)  Time elapsed: 0.095 s  <<< FAILURE!
java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

[ERROR] rejectsFieldWithoutTypeInFileBlock(nablarch.test.tool.converter.xls.XlsFormatWriterTest)  Time elapsed: 0.024 s  <<< FAILURE!
java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

[ERROR] Tests run: 81, Failures: 4, Errors: 0, Skipped: 0
[INFO] BUILD FAILURE
```

### 緑（修正後）

```sh
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true
```

```
[INFO] Tests run: 545, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

テスト総数は `9f2223e` の **541** から **545** へ（削除 2・追加 6）。導出:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
git grep -c '^    @Test' HEAD -- 'src/test/**/*.java' | awk -F: '{s+=$NF} END {print s}'   # 541（本コミット前）
grep -rc '^    @Test' src/test --include=*.java | awk -F: '{s+=$2} END {print s}'          # 545（本コミット）
```

### 削除した既存テスト（旧挙動を緑で固定していたもの）と担保の移り先

| 削除したテスト | 何を固定していたか | 担保の移り先 |
|---|---|---|
| `YamlFormatWriterTest#serialize_fieldWithNullType_omitsType` | 型 `null` のフィールドを `{name: "c1"}` と**型を落として書き出す**挙動 | 送出を弾く `serialize_fieldWithNullTypeInFileBlock_rejected` ／ `serialize_fieldWithNullTypeInMessageBlock_rejected`。「空文字は弾かず `type: ""` を書く」境界は `serialize_fieldWithEmptyType_emitsEmptyType` が持つ |
| `YamlFormatWriterModelTest#failsToReadBackFieldWithoutType` | 型を落とした YAML が**読み戻せない**こと（不具合の再現を緑で固定） | 番人が送出時点で弾くため、読み戻せない YAML を作れなくなった。担保は上の 2 本。クラス Javadoc の「残置している緑の嘘」の記述も 2 形目 1 本だけへ改めた |

置き換え（削除ではない）:

- `XlsFormatWriterTest#writesOmittedMetaAndFieldAsEmpty` —— 入力を `new FieldDef("f1", null, null)` から
  `new FieldDef("f1", "", null)` へ改めた。空セルを書く担保はそのまま残り、**「`null` は弾くが空文字は弾かない」境界テストを兼ねる**。
- `FieldDefTest#型と長さの省略をnullで保持する` —— `長さの省略をnullで保持する`（`length` は省略可）と
  `契約違反のnull型もモデル自身は検査せず保持する`（番人は書き出し辺にあり、モデルは検査しない）の 2 本へ分けた。

辺③側に旧挙動を緑で固定していたテストは無かった。型 `null` を渡す箇所は次で全件洗い出し、
残っているのは**新しい番人テスト 4 本と、モデルが検査しないことを述べる `FieldDefTest` 1 本だけ**であることを確認した。

```sh
grep -rn 'FieldDef("[^"]*", *null' src/test --include=*.java   # 3 件（XlsFormatWriterTest L392／L410 の番人 2 本＋FieldDefTest L63）
grep -rn 'field("[^"]*", *null' src/test --include=*.java      # 2 件（YamlFormatWriterTest L505／L522 の番人 2 本）
```

### `src/main` で変更したファイル

| ファイル | 変更 |
|---|---|
| `src/main/java/nablarch/test/tool/converter/model/FieldDef.java` | クラス Javadoc の「型・長さの省略は `null`」が契約と矛盾していたので「長さの省略は `null`」へ改め、`type` が必須である理由（`notation:883`／`:888`（`30a8271` 時点）・`$defs.field_def.required`）と、番人は書き出し辺にある旨を追記。検査は入れていない |
| `src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java` | `appendRecords` の `fields` 空番人の直後に、`type == null` を弾く番人を追加（識別子・レコード番号・フィールド名を診断に含む） |
| `src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java` | `emitRecords` に同じ番人を追加（`record_type`・フィールド名を診断に含む）。`fieldFlow` の「`type` が `null` なら出力しない」分岐は到達不能になったので削除し、`type` を常に出力するようにした |

### カバレッジ（JaCoCo 実測・2026-08-18。同一セッションで前後とも計測）

前は `9f2223e` の一時ワークツリー、後は本作業ツリーで、いずれも次を実行した。

```sh
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes \
  && JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec \
  && awk -F, 'NR > 1 && ($3 == "XlsFormatWriter" || $3 == "YamlFormatWriter") \
        { print $3 " line " $9 "/" ($8 + $9) " branch " $7 "/" ($6 + $7) }' target/site/jacoco/jacoco.csv
```

| クラス | 修正前（`9f2223e`） | 修正後 | 未到達の増減 |
|---|---|---|---|
| `XlsFormatWriter` | `line 159/160 branch 103/106` | `line 164/165 branch 107/110` | 行 1・分岐 3 のまま（増えた 4 分岐はすべて到達済み） |
| `YamlFormatWriter` | `line 163/164 branch 93/96` | `line 168/169 branch 95/98` | 行 1・分岐 3 のまま |

### 台帳へ反映したこと

- `coverage/issues.md` —— YML-12 の 4 形目の行を修正前／修正後の形へ書き直し、番人テスト 4 本と境界テストを明記。判定を「要対応（4 形のうち 1・3・4 形目）」に更新し、根拠に `notation:883`／`:885`／`:888`（`30a8271` 時点。`df7bff7` の行番号も併記）と `$defs.field_def.required` を置いた。「残置している『緑の嘘』」の表は 2 行 → **1 行**（2 形目の `failsToReadBackMessageBlockWithoutRecords` のみ）になり、件数はその場に書いた `grep … | wc -l` で導いた
- `coverage/inventory.md` —— §0 に本修正の変更ログ、§0.1-2 に件数（541 → 545）と JaCoCo の実測、§0.4／§3.1／§4.1 に C-20 の扱い（型レベルの「省略可」と契約の「必須」の別）と該当テスト行の注記を追加

### 未決（コーディネーター判断）

- **本修正のコミットハッシュを台帳に書けていない。** コミットは自分自身の SHA を含められないため、`b9ff38e`（修正）→ `aff5bb5`／`4bbd1fa`（記録）の前例どおり、記録は別の `docs` コミットが要る。現状 `issues.md` は「#25.5 で修正済み」とだけ書いてある
- **`issues.md` 冒頭の「要対応 7 件」の導出コマンドは、いま実行すると 6 を返す。** 本修正より前からのずれで、原因は YML-03 の行が `- NTF 仕様としての判定: **要対応 → 修正済み（2026-08-18）**` と書式から外れていること（`git show 9f2223e:` でも 6）。steering の「最後に 1 回だけ件数を確定する」で扱う想定のため、本修正では触っていない
- **steering の チェックボックス（YML-12 4形目）は未チェックのまま。** steering はコーディネーターの持ち物のため編集していない

---

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 不具合の挙動を緑のアサートで固定していたテストが 1 本も残っていない | OK | コミット 1 で 10 本に `@Ignore` を付け、うち 8 本はコミット 2〜6 で仕様どおりの期待値へ書き直して `@Ignore` を外した。残る 2 本は YML-03 の待機テストで `@Ignore` のまま。取りこぼした 2 本（上の「開示」）も赤で顕在化して直っている。`grep -rn '^    @Ignore' src/test --include=*.java` → **2 行**（どちらも `YamlFormatReaderRealFileTest`。L638／L1001） | OK | 下の 13 件実験で「旧挙動を固定していたテストの全体集合」が閉じていることを確認。列挙の取りこぼしの開示不足は Q-1 として指摘（反映済み） |
| 5 件の修正がいずれも TDD の手順（仕様どおりのテスト → 赤を実行して確認 → 実装 → 旧テスト削除）で行われている | OK | 上の「TDD — 5 件の赤」に、5 件それぞれの surefire 出力をそのまま貼った（要約していない）。**手順 2 を飛ばした件は無い。** 旧テストは 13 本すべて同じ位置で置き換え済み（上の「削除した既存テスト」） | OK | **QA が自分で実行**。①各修正コミットの `src/main` 差分だけを `git apply -R` で戻し、5 件の赤をすべて独立再現（失敗メッセージが記録と一字一句一致）。②`HEAD` の `src/main` に `git checkout 8c327d0 -- src/test` を重ねると失敗はちょうど **13 件**。詳細は下の「QA Expert Review」 |
| YML-02／YML-12／XLS-16／XLS-06／YML-08 の 5 件が修正され、それぞれ 1 コミットに分かれている | OK | `36e94a4`／`aec82f2`／`e80a4dd`／`5721ecd`／`6c8d90e`。`git log --stat 8c327d0..HEAD` のとおり、いずれも当該課題のファイルだけを含む | OK | `git log --stat 8c327d0..HEAD` を照合。個別の追試は記録なし |
| YML-03 は修正せず、仕様どおりの期待値を書いた待機テストが `@Ignore` で置かれている | OK | `9e8fd6b`。`keepsFwHeaderNamedRecordInMessageFromRealYaml`（L1002）／`keepsFwHeaderNamedRecordInSendSyncFromRealYaml`（L639）。どちらも `@Ignore("YML-03: yaml側の修正待ち")`。赤であることを実行して確認済み（上の出力）。本体側が直った日に `@Ignore` を外せば通る | OK | 自己申告の Evidence を照合。個別の追試は記録なし |
| XLS-01 は「対応不要」と判定され、テストが担保ではなく実挙動の記録として書き直されている | OK | `2f21bce`。`readsTextFormattedNumericCellAsDoubleString` の Javadoc とアサートのメッセージを、POI 由来で converter では直せないこと・NTF 仕様として対応不要であることを述べる形へ書き直した。`issues.md` XLS-01 の表見出しも「担保テスト」→「テスト」へ改めた | OK | 自己申告の Evidence を照合。個別の追試は記録なし |
| `issues.md` の全 35 件に「NTF 仕様としての判定」が出典つきで入っている | OK | `b3af6e6`。`- NTF 仕様としての判定` を全 35 節に追加した。**その後 `45194f9` で XLS-25 を 1 件足したため現在は 36 件**（`grep -c '^- NTF 仕様としての判定' issues.md` → 36。`grep -c '^### ' issues.md` → 60 は課題節 36 ＋ 見出し 24）。**要対応 6 ／ 対応不要 30**（`grep -c '^- NTF 仕様としての判定: \*\*要対応' issues.md` → 6 ／ `…\*\*対応不要` → 30）。ユーザー明示の「要対応 6 ／ 対応不要 29」から増えた 1 件は新規の XLS-25 であり、**既存 35 件の判定は 1 件も変えていない**（XLS-22 の再判定は指示待ちのため未着手）。既存の「判断」欄（往復基準）は残し、両者の違いを節冒頭で説明した。`^- 判断: ` は **36 件**（XLS-24 が `- **判断**:` と太字で書式が揃っていなかったのをレビュー C-3 で直し、全件同一書式になった） | OK | 件数を照合（36 件／要対応 6／対応不要 30）。**XLS-22 の分類は Q-3 として保留指摘**（ユーザー判断待ち） |
| YML-08 の「未確認」（辺④へ書き出して読み戻したときの往復）が実測で埋まっている | OK | `b689856`。中間モデルを手で組み立てて `YamlFormatWriter#write` → `YamlFormatReader#read` を実行した。`"CRLF"` は往復して安定、実 `CR LF` は **`NONE`** になって安定しない、`field-separator` の実タブは読み戻しが例外。**予想（空文字になる）は半分外れた**ことも含めて記録した | OK | 自己申告の Evidence を照合。個別の追試は記録なし |
| `inventory.md` の件数がすべて実行可能な導出コマンドから導かれている | OK | `2e1413e`。§0.1-2 を新設し、テスト総数・`@Ignore` 2 件・`8c327d0` との比較の 3 コマンドを併記（総数は `2e1413e` 時点で 536、レビュー対応で担保を 4 件足した現在は **540**。`45194f9` で台帳を実測へ揃えた）。§1.3／§2.3／§3.3／§4.3 の 4 合計にも導出コマンドを付け、実行して **32 ／ 28 ／ 30 ／ 16** を再現した。`XlsFormatReaderCellTypeTest` の `@Test` 件数が 19 と書かれていた誤りを **10** へ訂正し、#25.5 の修正が原因ではないことを `git` で確かめて開示した | OK | 自己申告の Evidence を照合。個別の追試は記録なし |
| 本体（nablarch-testing）・yaml（nablarch-testing-yaml）に書き込んでいない | OK | #25.5 のコミットはすべて `/home/tie303177/work/nablarch/nablarch-testing-converter` 配下のみ（`git log --stat 8c327d0..HEAD`）。両リポジトリとも最終確認時点で `git status --short` は 0 行。`pom.xml` も無変更（`git diff 8c327d0 HEAD -- pom.xml` → 0 行） | OK | コミット範囲が本リポジトリ配下のみであることを照合 |
| `src/main` / `src/test` のソースがすべてテキストのままである（`grep` が黙って読み飛ばすファイルが無い） | OK | コミット 12。`YamlTestCoreAdapter.java` L48 に生の NUL（U+0000）が 2 個入っており `file` が `data`、`git diff` が `Binary files ... differ`、`grep -rn DEFAULT_GROUP_MARKER src/` が **exit 1・0 件**（`-a` を付けると 4 件）になっていた。Java の 8 進エスケープ `"\0default-group\0"` へ置き換えて解消。**`\u0000` は使っていない**（ソース全体の前処理で置換され、結局同じ生 NUL がトークン列に入るため）。修正後は `file` → `Java source, Unicode text, UTF-8 text`／`grep -rn` が `-a` なしで **4 件**／`git diff` が通常のテキスト差分。挙動不変であることは class ファイル定数プールで確認（修正後も modified UTF-8 の `C0 80 default-group C0 80` が 1 件、生 NUL 0 件）。混入元は **`36e94a4`（YML-02）** で、当該ファイル 1 本のみ（各コミットを `git diff --numstat <c>^ <c>` で走査し、`-` 行が出るのは `36e94a4` と修正コミット `51dbca0` だけ。他 10 コミットは 0）。**修正後は `git diff --numstat 8c327d0..HEAD` に `-` 行が 1 本も無く**、当該ファイルは `54  2` とテキストで数えられる。番兵の値そのものは `520e890`（レビュー B-1）で素の `default-group` へ変えた。同じ混入を**この台帳の本文でもしていた**（L272 に生 NUL 1 個）ため合わせて除去し、`file` → `Unicode text, UTF-8 text` ／ `grep -rlP '\x00' .rn/ src/` → **0 件**を確認した | OK | 自己申告の Evidence を照合。個別の追試は記録なし |
| `FieldDef.type` ／ `MessageDataBlock.records` の契約が Javadoc に明記され、辺③（`XlsFormatWriter`）と辺④（`YamlFormatWriter`）の双方が `IllegalArgumentException` で弾く。現状挙動を固定していたテストは置き換えられている | **OK（`FieldDef.type` のみ）** ／ `MessageDataBlock.records`（YML-12 2形目）は**別タスクで未着手** | 上の「追補 — YML-12 4形目」。`FieldDef` のクラス Javadoc に契約と出典（`notation:883`／`:888`（`30a8271` 時点）・`$defs.field_def.required`）を明記し、辺③ `XlsFormatWriter#appendRecords` と辺④ `YamlFormatWriter#emitRecords` に番人を置いた。担保は `rejectsFieldWithoutTypeInFileBlock`／`rejectsFieldWithoutTypeInMessageBlock`／`serialize_fieldWithNullTypeInFileBlock_rejected`／`serialize_fieldWithNullTypeInMessageBlock_rejected` の 4 本と、境界（空文字は弾かない）の `writesOmittedMetaAndFieldAsEmpty`／`serialize_fieldWithEmptyType_emitsEmptyType`。旧挙動を固定していた 2 本（`serialize_fieldWithNullType_omitsType`／`failsToReadBackFieldWithoutType`）は削除済みで、`grep -rn 'failsToReadBack' src/test --include=*.java` に残る「緑の嘘」は 2 形目の `failsToReadBackMessageBlockWithoutRecords`（L790）だけである（`failsToReadBackLiteralTabFieldSeparator` は YML-08 の実挙動記録）。`mvn clean test -Djacoco.skip=true` → `Tests run: 545, Failures: 0, Errors: 0, Skipped: 0` | — | — |
| `mvn clean test -Djacoco.skip=true` が PASS する | OK | `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean test -Djacoco.skip=true` → **`Tests run: 540, Failures: 0, Errors: 0, Skipped: 2`** ／ `BUILD SUCCESS`。基準線 `8c327d0` は 536／Skipped 0 で、差は YML-03 の待機テスト 2 本と、レビュー対応で足した担保 4 本（`rejectsNullSheetName` ＋ `YamlTestCoreAdapterTest` のデフォルトグループ 3 本）である | OK | Verification expert が同じコマンドを独立に実行し PASS を実測（レビュー時点 536／Skipped 2） |

---

## QA Expert Review

**判定: Completion criteria 9 件すべて OK。**

QA は自己申告の Evidence を読むだけで済ませず、次の 2 つを自分で実行して裏を取った。

- **5 件の赤の独立再現** —— 各修正コミットの `src/main` 差分だけを `git apply -R` で戻し、当該テストを実行した。
  得られた失敗メッセージは、上の「TDD — 5 件の赤」に記録された出力と**一字一句一致**した。
  すなわち赤の記録は事後の書き起こしではない。
- **基準「旧テストが 2 本残っていない」の決定的実験** —— `HEAD` の `src/main` に対して
  `git checkout 8c327d0 -- src/test` で旧テストを重ねると、失敗は**ちょうど 13 件**であり、
  これは旧挙動を固定していたテストの全体集合と一致する。
  `git diff 8c327d0..HEAD -- src/test | grep "^[-+]    public void "` も 13 組の 1:1 置換を示した。
  よって「同じ挙動を主張するテストが 2 本残っている」箇所は無い。

**QA の指摘 4 件**（うち 3 件は反映済み、1 件が保留）

| # | 指摘 | 状態 |
|---|---|---|
| Q-1 | `@Ignore` 列挙の取りこぼしの開示が不完全（`readsFieldSeparatorWrittenAsEscapedTabNotation` が未開示だった） | 反映済み（本ファイル「取りこぼし 2 本（開示）」） |
| Q-2 | 「緑の嘘」が対象外の形で 4 本残っている | 反映済み（`ccf3c1a` ほか。「外したもの」で 1 本ずつ判定を明示） |
| Q-3 | XLS-22 の「NTF 仕様としての判定」の分類 | **保留**（ユーザー判断待ち） |
| Q-4 | ドキュメントの事実誤り（`DirectiveUtil` を「新規」と記述） | 反映済み（`45194f9`。実際は `8c327d0` に存在） |

## Expert Reviews

### Design expert

- **方針・構造: OK** —— 辺の対称性は改善のみで、新しい非対称は入っていない。
  YML-02 の番兵は `YamlTestCoreAdapter` の内部に閉じており外へ漏れない。
  XLS-16 の例外化は同ファイルの既存の番人（シート名重複・禁止文字）と一貫している。
- **全体整合: NG（指摘 2 件、いずれも反映済み）**
  - `inventory.md` が `DirectiveUtil` を「新規」と書いていた（`45194f9` で訂正）。
  - `YamlFormatWriter:47` の Javadoc が「`group_id` 必須」と現状に反していた（`b6be795` で訂正）。
  - 未到達分岐の帰属誤り（`inventory.md:271`）も指摘どおりで、実測の結果
    `DirectiveUtil.java:45`（既存 `toStringDirectives` の `null` 側）と判明した。

### Craft expert（coding）

- **両観点 OK。**
- 要修正 1 件: `YamlFormatWriterModelTest` に置き換え前の Javadoc が 2 枚孤児として残っていた（反映済み）。
- 軽微 5 件: `YamlFormatReader:295` の重複計算／`DirectiveUtil` の Javadoc 未追随／ダイヤモンド演算子 3 行／
  番兵の値／未使用 import と `"DROP"`（いずれも `520e890`・`b6be795`・`ccf3c1a` で反映済み）。
- 「順序を主張するテストはフィクスチャをずらす」（#25 の教訓）に反する箇所は無し。

### Verification expert（test）

- **両観点 OK。**
- レビュー時点で `Tests run: 536, Failures: 0, Errors: 0, Skipped: 2` を自分で実測した
  （レビュー対応で担保を 4 本足した現在は 540）。
- `@Ignore` は 2 本のみで、どちらも `@Ignore("YML-03: yaml側の修正待ち")` であることを確認。
- **赤はコミット履歴からは裏付けられない**（5 件とも実装とテストが同一コミットのため、
  履歴上に赤の状態が存在しない）。ただし独立再現で 4 件を確認した。
  —— この点は QA の `git apply -R` による 5 件全部の再現で埋まっている。

## Overall Verdict

- Self-check: OK —— YML-12 4形目（`FieldDef.type` 必須）を TDD で修正し、赤 4 件 → 実装 → 旧テスト 2 本の置き換えまで完了（上の「追補 — YML-12 4形目」。`Tests run: 545, Failures: 0, Errors: 0, Skipped: 0`）。**残る 2形目（`MessageDataBlock.records`）は別タスクで未着手**。レビュー 4 種の指摘 16 件はすべて反映済み（`520e890`／`b6be795`／`ccf3c1a`／`8d781b8`／`45194f9`）。保留は XLS-22 の「NTF 仕様としての判定」1 件のみで、ユーザー確認待ちのため未着手。
- QA: OK —— Completion criteria 9 件すべて OK。指摘 4 件のうち 3 件は反映済み、XLS-22 の分類のみ保留。
- Expert reviews: Design は方針・構造 OK／全体整合 NG（指摘 2 件は反映済み）、Craft OK、Verification OK。
- **Coordinator: 未確定** —— 保留中の XLS-22 の判定をユーザーに確認したうえで最終判定する。
  それ以外に未解決の指摘は無い（triage 1 巡目完了、上限 3 巡）。

---

## 追補 — YML-12 2形目（`MessageDataBlock.records` が空）の修正（2026-08-18・コミット `04873de`）

中間モデルの契約として **`MessageDataBlock.records` は 1 件以上（0 件不可）** を宣言し、書き出しの 2 辺
（辺③ `XlsFormatWriter#layoutMessage` ／ 辺④ `YamlFormatWriter#emitMessage`）が 0 件を
`IllegalArgumentException` で弾くようにした。番人は**モデルには置かない**（4形目 と同じ理由）。
**共通の `appendRecords` ／ `emitRecords` にも置かない** —— ファイルデータブロックのレコード 0 件は
0 バイト空ファイルを表す合法な形だからである。これで **YML-12 は 4 形すべて修正済み**になった。

### 判定の根拠（解説書と本体スキーマの明文のみ。実装の挙動は根拠にしていない）

```sh
cd /home/tie303177/work/nablarch/nablarch-document
git log -1 --format='%H %ci' 30a8271
for n in 881 1109 1146 1158 1257; do
  printf '%s: ' "$n"
  git show 30a8271:ja/development_tools/testing_framework/implementation/testdata_notation.rst | sed -n "${n}p"
done
```

出力（`30a8271f6ada3259b014618abea72a588db043d9 2026-08-18 08:54:15 +0900`）を自分で開いて一致を確認した。

- **電文のレコード 0 件は記法に明文が無い。** 電文が存在しない場合は `:1257`「応答不要メッセージ受信では…
  `expectedMessages` のデータブロックを記述する必要はない」＝**ブロックごと省略**が記法である。
- **0 バイト空ファイル特例は電文に及ばない。** `:881`／`:1109`／`:1146` はいずれも**ファイルに限定**して
  書かれている。`:1158`「…前述のファイルデータと同じ構成を持つ」は**カラム構成のみ**を指す。
- **スキーマ**（`nablarch-testing-yaml` の `nablarch/test/ntf-testdata-yaml-schema.json`）:
  `$defs.message_data` ／ `$defs.expected_request_message_data` ／ `$defs.group_message_data` はいずれも
  `required` ＝ `["id","records"]` かつ `records.minItems` ＝ **1**、`$defs.file_data` だけが **0**。
  この非対称は明文の有無に対応した意図的なものと扱い、スキーマは変更しない。

### TDD — 赤（`src/main` に触れる前の実測）

```
Tests run: 547, Failures: 4, Errors: 0, Skipped: 0
```

失敗した 4 件（いずれも `java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException`）:

- `XlsFormatWriterTest#rejectsMessageBlockWithoutRecords`
- `XlsFormatWriterTest#rejectsSendSyncMessageBlockWithoutRecords`
- `YamlFormatWriterTest#serializeMessage_withoutRecords_rejected`
- `YamlFormatWriterTest#serializeSendSync_withoutRecords_rejected`

### TDD — 緑

```
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean test -Djacoco.skip=true
Tests run: 547, Failures: 0, Errors: 0, Skipped: 0
```

545 → 547 の内訳は **削除 3 件・追加 5 件**（`inventory.md` §0.1-2 の追補）。

### 削除したテストと担保の移し先

| 削除 | 何を固定していたか | 移し先 |
|---|---|---|
| `YamlFormatWriterTest#serializeMessage_emptyBody_emitsIdOnly` | 辺④が `id:` だけの電文を書くこと | `#serializeMessage_withoutRecords_rejected` ／ `#serializeSendSync_withoutRecords_rejected` |
| `YamlFormatWriterModelTest#failsToReadBackMessageBlockWithoutRecords` | それが読み戻せないこと（「緑の嘘」の最後の 1 本） | 同上（出力そのものが無くなったため置き換え先は番人のみ） |
| `XlsFormatWriterModelTest#writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty` | 辺③が識別行＋メタ行だけの版面を書くこと | `XlsFormatWriterTest#rejectsMessageBlockWithoutRecords` ／ `#rejectsSendSyncMessageBlockWithoutRecords` |

境界（ファイルブロックの 0 件は合法）は `XlsFormatWriterModelTest#writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty`
／ `YamlFormatWriterModelTest#writesEmptyRecordsListForFileBlockWithoutRecords` が担保する。

### JaCoCo（`inventory.md` §0.1-2 の追補その 4）

`XlsFormatWriter` `line 167/168 branch 109/112` ／ `YamlFormatWriter` `line 170/171 branch 95/98` ／
`YamlFormatReader` `line 192/192 branch 102/102` ／ `DirectiveUtil` `line 20/20 branch 17/18`。
**4 クラスとも未到達の件数は変化なし**（辺④は番人で 2 分岐増え、到達不能になった `emitEmptyList` 引数の
削除で 2 分岐減った）。

### 残置している「緑の嘘」

**0 本**（4 → 2 → 1 → 0）。`issues.md`「残置している『緑の嘘』」節に検証コマンドを載せた。

### Self-check

- OK —— TDD（赤 4 件を実測 → 実装 → 緑 547 件）、台帳（`issues.md` ／ `inventory.md`）反映、
  引用行はすべて自分で `git show 30a8271:… | sed -n 'Np'` で開いて確認済み。`checks/` はコミットに含めていない。
