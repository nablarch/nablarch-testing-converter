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
| XLS-27 の 0 件テーブルの現象が `issues.md` に追試の証拠として残り、「警告が出ない」の根拠・制約・同梱サンプルの該当箇所（導出コマンド付き）・申し送りが揃っている | OK | **追試は自分で実行した。** 番人（`57c1b0d`）が入っているため、`git show 57c1b0d^:src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java` を scratchpad で単独コンパイルし、クラスパスの先頭に置いて現行クラスを覆う方法で辺②③①を通した（`src/main`／`src/test` は無変更。プローブはリポジトリに残していない）。覆い無しでは番人の `IllegalArgumentException`（`ブロック=[SETUP_TABLE=EMPTY_T]`）で止まることも実行して確認。得た出力（`[辺②]`／`[辺③]`／`[辺①]`）はそのまま `issues.md` XLS-27 の「観測（追試・実測 2026-08-18）」へ貼った —— `NEXT_T` が消え、`EMPTY_T` の `rows` が `[[C1], [v1]]`、`columnNames` が `[SETUP_TABLE=NEXT_T]` になる。**「警告が出ない」は実行とコードの両方で裏を取った** —— (a) 辺②③①の実行中だけ `System.out`／`System.err` を差し替え、`java.util.logging` の root へ `Level.ALL` のハンドラを足して捕捉 → 標準出力 0 バイト／ログレコード 0 件（標準エラーの 186 バイトは SLF4J の初期化通知 3 行のみ）、(b) `grep -rn 'LOGGER\.\|System\.out\|System\.err' src/main/java | wc -l` → **1**（`XlsFormatReader:618` の重複カラム名警告だけ）。**制約**（本体修正またはマーカーカラム案が入るまで 0 件テーブルを含む YAML は Excel へ変換できない）を「影響範囲（制約）」として整理し直し、旧「副作用」欄の事実を吸収して重複を作っていない。**同梱サンプルは導出コマンド付き** —— `grep -rn 'rows: \[\]' src --include=*.yaml | wc -l` → **4**、`grep -rln …` → **3 ファイル**。さらに追試で **4 箇所のうち番人に当たるのは 2 箇所だけ**であることが判明したため（`grep -rn -B1 'rows: \[\]' src --include=*.yaml | grep 'table:'` → 2 行。`testNormalEnd.yaml` の 2 箇所は `expected_files` 配下のファイルデータで、そのディレクトリ単独では`TestDataConverter.convert` が戻り値 1 で成功することを実行して確認）、従来の記述を訂正して記録した。**申し送りは機械的に拾える見出し** —— `### 0 件テーブル制約の申し送り（XLS-27。解説書担当・対象 PJ 宛。ユーザー指示 2026-08-18）` をXLS-27 の節の直後に置き、宛先・制約の中身・解除条件を書いた（`grep -n '申し送り' issues.md` で拾える）。**`src/main`／`src/test` は無変更**（`git status --short` の差分は `coverage/issues.md` と本ファイルの 2 本だけ。本ファイルはコミットしない） | — | — |
| `SampleConversionTest#stopsClimanSampleConversionBecauseOfZeroRowTable` の Javadoc が、番人に当たるサンプルの箇所を実物どおりに書いている | OK | 旧 Javadoc は「3 ファイルが `rows: []` のテーブルを持つ」と書いていたが、実物を開いて誤りを確認した —— `grep -rn 'rows: \[\]' src/test/java/nablarch/test/tool/converter/SampleConversionTest/` → **4 箇所・3 ファイル**。うち `ClientActionTest/testFindNoClients.yaml:3` と `ClientActionTest/testShowWithEmptyClientTable.yaml:3` は `setup_tables:` 直下の 0 件テーブル、`ExportProjectsInPeriodActionRequestTest/testNormalEnd.yaml:173`／`:199` は `grep -n '^[a-z_]*:' → list_maps:1／setup_tables:61／expected_files:93` と `sed -n '160,205p'` のとおり `expected_files` 配下のファイルデータの 0 件レコードで、直上に `fields:` を持つ。番人は `XlsFormatWriter#layout` が `ColumnRowDataBlock` のときだけ呼ぶ `layoutColumnRow`（L238）にあり、`expected_files` は `YamlFormatReader:236` で `FileDataBlock` になるため通らない。Javadoc を「4 箇所のうち番人に当たるのは `ClientActionTest` の 2 箇所だけ」「`testNormalEnd.yaml` の 2 箇所は綴りが同じでも別物で番人に当たらない」と読める形へ訂正し、出典（`testdata_notation.rst:819`／`:836`・`issues.md` XLS-27）は活かした。**「2 冊」は実測で正しいことを確認** —— サンプル一式を scratchpad へ複製して 0 件テーブルの 2 ファイルだけ除き、`TestDataConverter.convert(YAML→XLS)` を実行 → 戻り値 **2**、出力は `ClientActionTest.xlsx` と `ExportProjectsInPeriodActionRequestTest.xlsx` の **2 冊**（`testNormalEnd.yaml` は `rows: []` を含んだまま成功した。これが「番人に当たらない」の実行側の裏でもある）。プローブはリポジトリに残していない。`JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true -Dtest=SampleConversionTest` → **`Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`**／`BUILD SUCCESS`。変更は当該 Javadoc のみでアサート本体は無変更 | — | — |
| §1-B: `FileDataBlock.fileType` の契約が Javadoc に明記され、辺③（`XlsFormatWriter#layoutFile`）と辺④（`YamlFormatWriter#emitFile`）の双方が `IllegalArgumentException` で弾く。現状挙動を固定していた既存テストが 2 本残っていない | OK | 下の「追補 — §1-B（`FileDataBlock.fileType` が `null`）の修正」。根拠は `notation:883`（記法はファイルデータを固定長と可変長の 2 種類に尽くす）／`notation:1146`（`type` は必須キー）／`$defs.file_data` の `required` ＝ `["path","type","records"]`・`type.enum` ＝ `["fixed","variable"]`（`30a8271` の実物と本体スキーマ JSON を自分で開いて確認。実装の挙動は根拠にしていない）。赤は `src/main` に触れる前に実測（`Tests run: 3, Failures: 2` ／ 出力そのままを追補に貼付）、緑は `mvn clean test -Djacoco.skip=true` → **`Tests run: 550, Failures: 0, Errors: 0, Skipped: 0`**。**現状挙動を固定していた既存テストは無し** —— `HEAD` の `src/test` の `new FileDataBlock(` 全 39 箇所の第 4 引数を機械的に切り出し、`FileType.FIXED`／`.VARIABLE` 以外は仮引数素通しのヘルパ 2 箇所だけ（その呼び出し側も全件 `FIXED`／`VARIABLE`）であることを確認した。`inventory.md` は steering の取り決めにより未更新 | — | — |
| §1-C: `FieldDef.length` の条件つき必須（固定長ファイル・電文では `null` 不可／可変長ファイルでは省略可）が Javadoc に明記され、辺③（`XlsFormatWriter#appendRecords`）と辺④（`YamlFormatWriter#emitRecords`）の双方が固定長ファイル・電文でだけ `IllegalArgumentException` で弾く。現状挙動を緑で固定していた既存テストが残っていない | OK | 下の「追補 — §1-C（`FieldDef.length` が `null`）の修正」。根拠は `notation:883`（固定長は 3 リスト同サイズで必須／可変長はフィールド長不要）／`:889`（3 リストのサイズ不一致は記述時エラー）／`:1158`（電文のメッセージボディはファイルデータと同じ構成）。本体スキーマ `$defs.field_def.required` は `["name","type"]` で `length` を含まないが、同スキーマの `length` の説明が「固定長ファイルでは実質必須…可変長ファイルでは不要（省略可）」であり**解説書と食い違っていない**（食い違いが無いこと自体を `FieldDef` の Javadoc と `issues.md` に記録した）。赤は `src/main` に触れる前に実測（`Tests run: 4, Failures: 4` ／ 出力そのままを追補に貼付）、緑は `mvn clean test -Djacoco.skip=true` → **`Tests run: 554, Failures: 0, Errors: 0, Skipped: 0`**。旧挙動を緑で固定していた既存テスト **3 本**は削除ではなく入力を直した（担保している別観点が失われるため。理由は各テストの Javadoc に記載）。可変長で弾かないことは既存の 2 本（`writesVariableFileWithoutLengthRow`／`serializeFile_variableOmitsDirectivesAndRecordTypeAndLength`）が担保する。`inventory.md` は steering の取り決めにより未更新 | — | — |
| §1-D: `FieldDef.name` が `null` のフィールドについて、番人を置く前にその形が記法の外であることを明文で確かめている。明文が届いていなければ番人を置かず「明文が無い」と記録している | **OK（番人は置かず保留）** | 下の「追補 — §1-D（`FieldDef.name` が `null`）の明文確認」。`30a8271` の実物を開いて `:871`／`:883`／`:885-893`／`:1057-1064`／`:1140`／`:1158` を読み、**解説書の明文は個別要素の `null` まで届いていない**と判定した —— `:888` は「フィールド名称リストまたはデータ型リスト**が**未指定または空である」で主語がリストであり、要素 1 個が `null` の形は「未指定」でも「空」でもない。`:889` はサイズの話で、名称だけ `null` でもサイズは一致しうる（「名称 `null` は名称リストを 1 つ短くする」と読むなら**同じ読みが空文字にも当てはまり**、`null` だけを弾く番人の境界と矛盾する）。個別要素に届く `:871`「各フィールドの名称」／`:1060`「フィールドの数だけ記載する」は、**Excel の版面に `null` と空文字を区別するセルの状態が無い**ため境界を導けない。YAML の `:1140`「`fields:` の各要素は `{name:, type:, length:}` の形式」は個別要素に届くが、同じ列挙に可変長では不要な `length` が入っており必須要件の行ではない。**届いている明文は本体スキーマ `$defs.field_def` だけ**（`required` ＝ `["name","type"]`・`name` ＝ `{"type":"string"}`）。**実測** —— 辺④の出力 `- {name: null, type: "半角英字", length: "10"}` を `YamlTestDataValidator` に掛けると `$.setup_files[0].records[0].fields[0].name: null が見つかりました、string が予期されました`、`name: ""` では検出 0 件。よってユーザー指示（「明文が届いていなければ番人を置かない」）に従い**辺③④のどちらにも番人を置かず**、事実と未決を `issues.md` XLS-31 に記録した（`src/main`・`src/test` は無変更） | — | — |
| §1-E: `TestDataBlock.groupId` が `null` のブロックについて、番人を置く前にその形が記法の外であることを明文で確かめている。明文が届いていなければ番人を置かず「明文が無い」と記録している | **OK（番人は置かず保留）** | 下の「追補 — §1-E（`TestDataBlock.groupId` が `null`）の明文確認」。`30a8271` の実物を開いて `:198`／`:254`／`:269`／`:278`／`:330`／`:1016`／`:1265` を読み、本体スキーマ JSON の `group_id` を 4 定義（`table_data`／`file_data`／`expected_request_message_data`／`group_message_data`）とも `python3` で取り出して確認した（コマンドは下の「読んだ実物」）。**明文が定めているのは版面／YAML に書かれる文字列であって、その「書かれない」状態を中間モデルのどの Java 値で表すかは決めていない** —— `""` も `null` も Excel では「データタイプ名の直後に何も無い」、YAML では「`group_id:` を置かない」という**同じ 1 つの出力**にしかならず、記法はこの 2 つを区別しない（§1-D と同じ構造）。**本体スキーマも `null` を排除しない** —— `group_id` は 4 定義とも `{"type":"string","minLength":1}` だが **`required` に入っていない任意キー**なので、`null` を「キーを置かない」と写せば適合する（§1-D の `$defs.field_def` は `name` が `required` で `null` を逃がせず、そこだけは明文が届いていた。**§1-E はそこも届かない**）。よってユーザー指示（「明文が届いていなければ番人を置かない」）に従い**辺③④のどちらにも番人を置かず**、事実と未決を `issues.md` XLS-32 に記録した（`src/main`・`src/test` は無変更）。**明文で決着している事実が 1 つある** —— 辺③の現状出力 `SETUP_TABLEnull=T` は `:198`・`:278` の示す形に無いため**現状維持は選べない**が、是正は「弾く」と「`null` を省略として正規化する」の 2 通りあり、後者も明文に違反しないため明文からは決まらない（未決・推奨は弾く側）。**現状の挙動は自分で実行して確かめ**（使い捨てプローブ。リポジトリには残していない）、XLS-32 の「観測」欄と一致した。`JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` → **`Tests run: 554, Failures: 0, Errors: 0, Skipped: 0`**／`BUILD SUCCESS` | — | — |

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

---

## 追補 — §1-B（`FileDataBlock.fileType` が `null`）の修正（2026-08-18・コミット `44469b2`）

中間モデルの契約として **`FileDataBlock.fileType` は必須（`null` 不可）** を宣言し、書き出しの 2 辺
（辺③ `XlsFormatWriter#layoutFile` ／ 辺④ `YamlFormatWriter#emitFile`）が `null` を
`IllegalArgumentException` で弾くようにした。番人は**モデルのコンストラクタには置かない**
（steering Decisions「`RecordLayout` コンストラクタに番人は置かない」と同じ理由。写せない値を止める場所は
書き出し辺である）。番人は辺ごとに書いた（共通化しない）—— 弾く理由の明文が辺③（Excel 記法の
固定長／可変長の二分）と辺④（スキーマの `required` ＋ `enum`）で別であり、診断メッセージも別だからである。

### 判定の根拠（解説書と本体スキーマの明文のみ。実装の挙動は根拠にしていない）

```sh
cd /home/tie303177/work/nablarch/nablarch-document
git log -1 --format='%H %ci' 30a8271
for n in 883 1146; do
  printf '%s: ' "$n"
  git show 30a8271:ja/development_tools/testing_framework/implementation/testdata_notation.rst | sed -n "${n}p"
done
```

出力（`30a8271f6ada3259b014618abea72a588db043d9 2026-08-18 08:54:15 +0900`）を自分で開いて一致を確認した。

- `notation:883`（Excel／YAML 共通の記法制約）——「固定長ファイルと可変長ファイルには、それぞれ固有の
  記法制約がある。固定長ファイルでは、フィールド名称・データ型・フィールド長の3リストが同サイズで必須であり…
  可変長ファイルでは、フィールド名称・データ型の2リストが同サイズで必須であり、フィールド長は不要である。」
  ——**記法はファイルデータをこの 2 種類に尽くしており、どちらでもないファイルデータブロックは存在しない。**
  固定長と可変長では長さ行の有無という版面そのものが違うため、種別が決まらないと版面を決められない。
- `notation:1146`（YAML）——「`setup_files`・`expected_files` の各エントリには `path`・`type`・`records` の
  3キーが必須であり、いずれかを省略するとエラーになる。」
- **本体スキーマ** `nablarch-testing-yaml` の
  `src/main/resources/nablarch/test/ntf-testdata-yaml-schema.json` の `$defs.file_data` ——
  `required` ＝ `["path", "type", "records"]`、`type` ＝ `{"type": "string", "enum": ["fixed", "variable"]}`
  （`python3` で JSON を読み出して確認。`null` は `string` ではなく `enum` にも無い）。

**判定は覆らなかった**（`issues.md` XLS-29 の「NTF 仕様としての判定: **要対応**」のまま）。
明文は Excel 側・YAML 側の双方にあり、「到達できないから」「実装がそう動くから」は根拠に使っていない。

### TDD — 赤（`src/main` に触れる前の実行出力そのまま）

```sh
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -q test -Djacoco.skip=true \
  -Dtest='XlsFormatWriterTest#rejectsFileBlockWithoutFileType,YamlFormatWriterTest#serialize_fileBlockWithoutFileType_rejected,FileDataBlockTest#契約違反のnullファイル種別もモデル自身は検査せず保持する' \
  -Dsurefire.failIfNoSpecifiedTests=false
```

```
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.732 s <<< FAILURE! - in nablarch.test.tool.converter.yaml.YamlFormatWriterTest
[ERROR] serialize_fileBlockWithoutFileType_rejected(nablarch.test.tool.converter.yaml.YamlFormatWriterTest)  Time elapsed: 0.645 s  <<< FAILURE!
java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.637 s <<< FAILURE! - in nablarch.test.tool.converter.xls.XlsFormatWriterTest
[ERROR] rejectsFileBlockWithoutFileType(nablarch.test.tool.converter.xls.XlsFormatWriterTest)  Time elapsed: 0.637 s  <<< FAILURE!
java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

[ERROR] Failures:
[ERROR]   XlsFormatWriterTest.rejectsFileBlockWithoutFileType Expected exception: java.lang.IllegalArgumentException
[ERROR]   YamlFormatWriterTest.serialize_fileBlockWithoutFileType_rejected Expected exception: java.lang.IllegalArgumentException
[ERROR] Tests run: 3, Failures: 2, Errors: 0, Skipped: 0
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:2.22.2:test (default-test) on project nablarch-testing-converter: There are test failures.
```

3 件目（`FileDataBlockTest#契約違反のnullファイル種別もモデル自身は検査せず保持する`）は**赤の時点で緑**である。
モデル自身は検査しないことを述べるテストであり、修正の前後で挙動が変わらないのが正しい。

### TDD — 緑

```sh
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true
```

```
[INFO] Tests run: 550, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

テスト総数は **547 → 550**（削除 0・追加 3）。導出:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
git grep -c '^    @Test' HEAD -- 'src/test/**/*.java' | awk -F: '{s+=$NF} END {print s}'   # 547（本コミット前）
grep -rc '^    @Test' src/test --include=*.java | awk -F: '{s+=$2} END {print s}'          # 550（本コミット）
```

### 削除した既存テストと担保の移し先

**無し。** 現状挙動（`null` を黙って可変長にする）を緑のアサートで固定していたテストは 1 件も無かった。
根拠は次の全件走査である —— 修正前（`HEAD`）の `src/test` にある `new FileDataBlock(` の呼び出しを
**39 箇所**取り出し、括弧の深さを数えて第 4 引数（`fileType`）だけを機械的に切り出した。
`FileDataBlock.FileType.FIXED` ／ `.VARIABLE` を渡さないものは **2 箇所**だけで、いずれも仮引数
`fileType` をそのまま素通しするヘルパ（`RoundTripTest#file`（L843）／
`FileDataBlockTest#assertFileBlock`（L69））であり、その呼び出し側も全件が `FIXED` ／ `VARIABLE` を
渡していた（`RoundTripTest` の 8 箇所・`FileDataBlockTest#固定可変とSETUP_EXPECTEDの全組合せを保持する`
の 4 箇所）。したがって `null` を渡すテストは修正前に 1 件も存在しない。

```sh
# 第 4 引数の切り出し（HEAD の src/test 全件）
python3 - <<'PY'
import subprocess, re
files = [f for f in subprocess.run(['git','ls-files','src/test'],capture_output=True,text=True).stdout.split()
         if f.endswith('.java')]
n = 0
for f in files:
    src = subprocess.run(['git','show','HEAD:'+f],capture_output=True,text=True).stdout
    for m in re.finditer(r'new FileDataBlock\(', src):
        i, depth, args, cur = m.end(), 1, [], ''
        while depth > 0:
            c = src[i]
            if c == '(': depth += 1
            elif c == ')':
                depth -= 1
                if depth == 0: break
            if depth == 1 and c == ',': args.append(cur.strip()); cur = ''
            else: cur += c
            i += 1
        args.append(cur.strip())
        n += 1
        fourth = ' '.join(args[3].split())
        if 'FileType' not in fourth:
            print('NON-FILETYPE ARG:', f, src[:m.start()].count('\n') + 1, fourth)
print('total', n)
PY
# → NON-FILETYPE ARG: RoundTripTest.java 843 fileType
#   NON-FILETYPE ARG: FileDataBlockTest.java 69 fileType
#   total 39
```

追加した 3 件:

| 追加したテスト | 何を担保するか |
|---|---|
| `XlsFormatWriterTest#rejectsFileBlockWithoutFileType` | 辺③が `IllegalArgumentException` で弾く（従来は長さ行の無い可変長の版面へ黙って倒れていた） |
| `YamlFormatWriterTest#serialize_fileBlockWithoutFileType_rejected` | 辺④が `IllegalArgumentException` で弾く（従来は `type: "variable"` と黙って書いていた） |
| `FileDataBlockTest#契約違反のnullファイル種別もモデル自身は検査せず保持する` | 中間モデル自身は検査せず保持するだけであること（番人の置き場所が書き出し辺であることの裏） |

### `src/main` で変更したファイル

| ファイル | 変更 |
|---|---|
| `src/main/java/nablarch/test/tool/converter/model/FileDataBlock.java` | クラス Javadoc に「`fileType` は必須（`null` 不可）」の契約と出典（`notation:883`／`:1146`（`30a8271` 時点）・`$defs.file_data` の `required` と `enum`）、番人は書き出し辺にある旨を追記。コンストラクタの `@param` と `getFileType()` の `@return` にも必須である旨を書いた。**検査は入れていない** |
| `src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java` | `layoutFile` の先頭に `getFileType() == null` の番人を追加（診断に `identifier=[...]`）。Javadoc に根拠を記載 |
| `src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java` | `emitFile` の先頭に同じ番人を追加（診断に `identifier=[...]`）。Javadoc に根拠を記載 |

### 台帳へ反映したこと

- `coverage/issues.md` —— XLS-29 の見出しに「**#25.5 で修正済み**」を足し、「修正」欄（番人の置き場所・
  テストメソッド名・削除した既存テストが無いことと、その根拠）を追加した。判定欄（**要対応**）は変えていない
- `coverage/inventory.md` —— **触っていない**（件数の導き直しは §1-B〜G・XLS-28 が全部済んでから 1 回でやると
  steering で決めてあるため）

### 未決（コーディネーター判断）

- **本修正のコミット SHA を `issues.md` に書けない。** コミットは自分自身の SHA を含められないため、
  `b9ff38e`（修正）→ 別コミット（記録）の前例どおり、SHA の記録には別の `docs` コミットが要る。
  **実際の SHA は `44469b2`**（push 済み）。`issues.md` XLS-29 の「修正」欄に埋めること
- **steering のチェックボックス（§1-B）は未チェックのまま。** steering はコーディネーターの持ち物のため編集していない

### Self-check

- OK —— TDD（赤 2 件を `src/main` に触れる前に実測 → 実装 → 緑 550 件）、明文の確認（`30a8271` の実物と
  本体スキーマの JSON を自分で開いた）、削除対象テストの全件走査（無しを走査結果で確認）、
  `issues.md` 反映。`checks/` はコミットに含めていない

---

## 追補 — §1-C（`FieldDef.length` が `null`）の修正（2026-08-18）

中間モデルの契約として **`FieldDef.length` は条件つきで必須**——固定長ファイル・電文のフィールド定義では
`null` 不可、**可変長ファイルのフィールド定義では省略可（`null` が正）**——を宣言し、書き出しの 2 辺
（辺③ `XlsFormatWriter#appendRecords` ／ 辺④ `YamlFormatWriter#emitRecords`）が、
**固定長ファイル・電文のときだけ** `null` を `IllegalArgumentException` で弾くようにした。
番人は**モデルのコンストラクタには置かない**（steering Decisions「`RecordLayout` コンストラクタに番人は
置かない」と同じ理由）。

辺③はすでに `appendRecords` が `boolean fixed` を受け取っており、そこに条件を足した。辺④の
`emitRecords` は同じ文脈を持っていなかったため、**引数 `lengthRequired` を足して辺③と対称にした**
（`emitFile` は `fileType == FIXED`、`emitMessage` は常に `true` を渡す）。0 件レコードの検査を
「共通の `emitRecords` には置かない」としてある既存の Javadoc とは別の判断であることを、
その理由（0 件検査はブロック単位でありファイルでは 0 件が正／長さの検査はフィールド単位で、
呼び出し側へ移すと入れ子ループが重複する）とともに Javadoc に併記した。

### 判定の根拠（解説書と本体スキーマの明文のみ。実装の挙動・到達可能性は根拠にしていない）

```sh
cd /home/tie303177/work/nablarch/nablarch-document
git log -1 --format='%H %ci' 30a8271
for n in 849 883 889 1158; do
  printf '%s: ' "$n"
  git show 30a8271:ja/development_tools/testing_framework/implementation/testdata_notation.rst | sed -n "${n}p"
done
```

- `notation:883`——「固定長ファイルでは、フィールド名称・データ型・フィールド長の3リストが同サイズで
  必須であり…可変長ファイルでは、フィールド名称・データ型の2リストが同サイズで必須であり、
  フィールド長は不要である。」**固定長で必須、可変長で不要**が同じ 1 行に書かれている。
- `notation:889`（ファイルデータの記述時にエラーとなるケース）——「フィールド名称・データ型・フィールド長
  リストのサイズが一致していない」。
- `notation:1158`——「フレームワーク制御ヘッダ以降のメッセージボディは、フィールド名称・データ型・
  フィールド長・データという、前述のファイルデータと同じ構成を持つ」＝**電文も同じ制約に掛かる**。
- **`:883` は形式に依らない。** 節の見出しと下線の水準を抽出して確かめた——`:848`
  「ファイルのデータを記述する」は水準 3（`~~~`）で、`:883` はその直下にある。Excel 形式（`:1006`）・
  YAML 形式（`:1121`）は水準 4（`^^^`）で **`:883` より後**に現れる。したがって `:883` の制約は
  Excel／YAML の双方に掛かる。

**本体スキーマとの関係（食い違いは無い）**——`nablarch-testing-yaml` の
`src/main/resources/nablarch/test/ntf-testdata-yaml-schema.json` を `python3` で読み出して確認した。
`$defs.field_def.required` は `["name", "type"]` であり **`length` を含まない**。`$defs.record_fragment` が
固定長ファイル・可変長ファイル・電文で共用されるため、条件つきの必須を `required` では表せないからである。
ただし同スキーマ自身が `length` の説明に「固定長ファイルでは実質必須（省略すると NTF が record-length を
計算できない）。可変長ファイルでは不要（省略可）」と書いており、**`notation:883` と一致する。**
番人の根拠は解説書の明文に置き、スキーマが強制していない事実はそのまま `FieldDef` の Javadoc と
`issues.md` XLS-30 に記録した（片方だけを都合よく引いていない）。

**判定は覆らなかった**（`issues.md` XLS-30 の「NTF 仕様としての判定: **要対応**」のまま）。

### TDD — 赤（`src/main` に触れる前の実行出力そのまま）

```sh
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -q test -Djacoco.skip=true \
  -Dtest='XlsFormatWriterTest#rejectsFieldWithoutLengthInFixedFileBlock+rejectsFieldWithoutLengthInMessageBlock,YamlFormatWriterTest#serialize_fieldWithoutLengthInFixedFileBlock_rejected+serialize_fieldWithoutLengthInMessageBlock_rejected' \
  -Dsurefire.failIfNoSpecifiedTests=false
```

```
[ERROR] Tests run: 2, Failures: 2, Errors: 0, Skipped: 0, Time elapsed: 0.975 s <<< FAILURE! - in nablarch.test.tool.converter.yaml.YamlFormatWriterTest
[ERROR] serialize_fieldWithoutLengthInMessageBlock_rejected(nablarch.test.tool.converter.yaml.YamlFormatWriterTest)  Time elapsed: 0.871 s  <<< FAILURE!
java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

[ERROR] serialize_fieldWithoutLengthInFixedFileBlock_rejected(nablarch.test.tool.converter.yaml.YamlFormatWriterTest)  Time elapsed: 0.001 s  <<< FAILURE!
java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

[ERROR] Tests run: 2, Failures: 2, Errors: 0, Skipped: 0, Time elapsed: 1.057 s <<< FAILURE! - in nablarch.test.tool.converter.xls.XlsFormatWriterTest
[ERROR] rejectsFieldWithoutLengthInMessageBlock(nablarch.test.tool.converter.xls.XlsFormatWriterTest)  Time elapsed: 0.941 s  <<< FAILURE!
java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

[ERROR] rejectsFieldWithoutLengthInFixedFileBlock(nablarch.test.tool.converter.xls.XlsFormatWriterTest)  Time elapsed: 0.116 s  <<< FAILURE!
java.lang.AssertionError: Expected exception: java.lang.IllegalArgumentException

[ERROR] Failures:
[ERROR]   XlsFormatWriterTest.rejectsFieldWithoutLengthInFixedFileBlock Expected exception: java.lang.IllegalArgumentException
[ERROR]   XlsFormatWriterTest.rejectsFieldWithoutLengthInMessageBlock Expected exception: java.lang.IllegalArgumentException
[ERROR]   YamlFormatWriterTest.serialize_fieldWithoutLengthInFixedFileBlock_rejected Expected exception: java.lang.IllegalArgumentException
[ERROR]   YamlFormatWriterTest.serialize_fieldWithoutLengthInMessageBlock_rejected Expected exception: java.lang.IllegalArgumentException
[ERROR] Tests run: 4, Failures: 4, Errors: 0, Skipped: 0
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:2.22.2:test (default-test) on project nablarch-testing-converter: There are test failures.
```

（この出力は、実装後に `git checkout HEAD --` で `src/main` の 3 ファイルだけを番人の入る前へ戻し、
新規テスト 4 本だけを実行して採取した。採取後に実装を復元してある。）

### TDD — 緑

```sh
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true
```

```
[INFO] Tests run: 554, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

テスト総数は **550 → 554**（削除 0・追加 4）。導出:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
git grep -c '^    @Test' HEAD -- 'src/test/**/*.java' | awk -F: '{s+=$NF} END {print s}'   # 550（本コミット前）
grep -rc '^    @Test' src/test --include=*.java | awk -F: '{s+=$2} END {print s}'          # 554（本コミット）
```

### 現状挙動を緑で固定していた既存テストと担保の移し先

**削除は 0 本。3 本の入力を直した**（削除すると同じテストが担保している別の観点——空文字境界・
directives・複数レコード・実リーダでの往復——まで失われるため）。3 本とも改変の理由と出典を
Javadoc に書いた。全件は、修正前の `src/test` にある 3 引数の `new FieldDef(...)` ／ `field(...)` の
呼び出しを括弧の深さを数えて機械的に切り出し（第 3 引数が `null` のものは **25 箇所**）、
その 1 件ずつを開いて文脈（可変長か固定長か・辺③④か辺①②か）を判定して洗い出した。
25 箇所のうち番人に当たるのは下記 3 本だけで、残りは可変長ファイルか読み込み側（辺①②）の
フィクスチャだった。

| テスト | 直したこと | 担保の移し先 |
|---|---|---|
| `XlsFormatWriterTest#writesOmittedMetaAndFieldAsEmpty` | `new FieldDef("f1", "", null)` → `("f1", "", "")` | **空文字境界**（番人は `null` だけを弾き、空文字は弾かない）の担保として同じテストが持つ。データ型の番人で `f80c192` が取ったのと同じ扱い |
| `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndOmittedLength` → `#serializeFile_fixedWithDirectivesAndMultipleRecords` に改名 | `field("f2", "数値", null)` → `("f2", "数値", "5")`。期待 YAML も `length: "5"` を含む形へ | `length` 省略は `#serializeFile_variableOmitsDirectivesAndRecordTypeAndLength`（可変長）が担保 |
| `YamlFormatWriterTest#roundTrip_fixedFile_isPreservedThroughRealReader` | `field("f2", "数値", null)` → `("f2", "数値", "5")`、`assertFieldDef(..., "f2", "数値", null)` → `"5"` | 同上 |

追加した 4 件と、可変長で**弾かない**ことの担保:

| テスト | 何を担保するか |
|---|---|
| `XlsFormatWriterTest#rejectsFieldWithoutLengthInFixedFileBlock` | 辺③が固定長ファイルで弾く（従来は長さセルを空文字で書いていた） |
| `XlsFormatWriterTest#rejectsFieldWithoutLengthInMessageBlock` | 辺③が電文で弾く |
| `YamlFormatWriterTest#serialize_fieldWithoutLengthInFixedFileBlock_rejected` | 辺④が固定長ファイルで弾く（従来は `length` キーを落としていた） |
| `YamlFormatWriterTest#serialize_fieldWithoutLengthInMessageBlock_rejected` | 辺④が電文で弾く |
| `XlsFormatWriterTest#writesVariableFileWithoutLengthRow`（既存） | **可変長では弾かない**。番人の範囲を広げると落ちる。Javadoc に範囲と出典（`:883`）を追記した |
| `YamlFormatWriterTest#serializeFile_variableOmitsDirectivesAndRecordTypeAndLength`（既存） | 同上（辺④） |

重複テストは作っていない（steering Rules の「重複テストを書かない」に従い、
既存の可変長テストに範囲の記述を足す形にした。データ型の番人で `f80c192` が取ったのと同じ方法）。

### `src/main` で変更したファイル

| ファイル | 変更 |
|---|---|
| `src/main/java/nablarch/test/tool/converter/model/FieldDef.java` | クラス Javadoc に `length` の条件つき必須の契約と出典（`notation:883`／`:889`／`:1158`（`30a8271` 時点）・本体スキーマが `required` では強制していない事実とその理由）を追記。`@param length`／`getLength()` の `@return` も条件つき必須へ書き換え。**検査は入れていない** |
| `src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java` | `appendRecords` のフィールド走査に `fixed && getLength() == null` の番人を追加（診断に `identifier=[...]`・`レコード番号=`・`フィールド名=[...]`）。Javadoc に根拠と、`layoutMessage` が常に `fixed` ＝ 真で呼ぶこと・可変長では `null` が正であることを記載 |
| `src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java` | `emitRecords` に引数 `lengthRequired` を追加し、同じ番人を置いた（診断に `record_type=[...]`・`フィールド名=[...]`）。呼び出し側は `emitFile` が `fileType == FIXED`、`emitMessage` が `true`。共通メソッドへ置いた理由を Javadoc に記載 |

### 台帳へ反映したこと

- `coverage/issues.md` —— XLS-30 の見出しに「**#25.5 で修正済み**」を足し、本体スキーマとの関係（食い違いが
  無いこと）と「修正」欄（番人の置き場所・診断メッセージ・テストメソッド名・入力を直した 3 本と担保の
  移し先・`inventory.md` が古くなる箇所）を追加した。判定欄（**要対応**）は変えていない
- `coverage/inventory.md` —— **触っていない**（件数の導き直しは §1-B〜G・XLS-28 が全部済んでから 1 回でやると
  steering で決めてあるため）。上の 3 本の書き換えでテスト名・観点が古くなる行があることは `issues.md` に
  書き残した

### 未決（コーディネーター判断）

- **本修正のコミット SHA を `issues.md` に書けない。** コミットは自分自身の SHA を含められないため、
  `b9ff38e`（修正）→ 別コミット（記録）／`44469b2` → `b7c1f86` の前例どおり、SHA の記録には別の `docs`
  コミットが要る。`issues.md` XLS-30 の「修正」欄に埋めること
- **steering のチェックボックス（§1-C）は未チェックのまま。** steering はコーディネーターの持ち物のため編集していない

### Self-check

- OK —— TDD（赤 4 件を番人の入る前の `src/main` で実測 → 実装 → 緑 554 件）、明文の確認（`30a8271` の実物と
  本体スキーマの JSON を自分で開き、`:883` が形式に依らない位置にあることを見出しの水準で確かめた）、
  旧挙動を固定していたテストの全件走査（`null` 長さの 25 箇所を 1 件ずつ読んで 3 本を特定）、
  `issues.md` 反映。`checks/` はコミットに含めていない


---

## 追補 — §1-D（`FieldDef.name` が `null`）の明文確認（2026-08-18）

**結論: 番人を置かなかった。** 解説書（`testdata_notation.rst`・`30a8271`）の明文は、
`fields` の**個別要素**の名称が `null` である形までは届いていない。届いている明文は
本体スキーマ `$defs.field_def` だけであり、それだけを根拠に辺③（Excel）へも番人を置いてよいかは
ユーザー判断に委ねた。`src/main`・`src/test` は無変更で、記録だけを `issues.md` XLS-31 に足した。

steering Decisions「中間モデル一巡点検で出た 7 件を全件 #25.5 に含める」の**条件 1**
（「番人を置く前に、その形が記法の外であることを明文で確かめる。明文が無ければ番人を置かず、
『明文が無い』と記録する」）と**条件 2**（「到達可能性を根拠にしない」）に従った。
**実装の挙動も到達可能性も根拠にしていない**（steering Decisions「記法の根拠に実装の挙動を使わない」）。

### 読んだ実物

```sh
cd /home/tie303177/work/nablarch/nablarch-document
git show 30a8271:ja/development_tools/testing_framework/implementation/testdata_notation.rst > /tmp/notation.rst
grep -n 'フィールド名称' /tmp/notation.rst      # 871 / 883 / 887 / 889 / 890 / 892 / 1025-1026 / 1059-1060 / 1070 / 1074 / 1082 / 1158 / 1162 / 1208 / 1267 / 1269 / 1271
sed -n '860,895p;1055,1075p;1136,1146p' /tmp/notation.rst
python3 -c "import json;print(json.load(open('/home/tie303177/work/nablarch/nablarch-testing-yaml/src/main/resources/nablarch/test/ntf-testdata-yaml-schema.json'))['\$defs']['field_def'])"
```

### 判定 —— 明文はどこまで届くか

| 出典 | 本文 | 個別要素の `null` に届くか |
|---|---|---|
| `:888` | 「フィールド名称リストまたはデータ型リスト**が**未指定または空である」 | **届かない。** 未指定・空の主語は**リスト**。`fields` が 1 件以上あり 1 件の名称だけが `null` という形は、リストが未指定でも空でもない |
| `:889` | 「フィールド名称・データ型・フィールド長リストのサイズが一致していない」 | **届かない。** 名称だけ `null` でも 3 リストのサイズは一致しうる。「名称 `null` は名称リストを 1 つ短くする」と読むなら**同じ読みが空文字にも当てはまり**、`null` だけを弾く番人の境界（既存の `type` の番人と同じ）と矛盾する |
| `:887` / `:890` / `:1070` / `:1162` | 「同一レコード種別内でフィールド名称が重複している」／「存在しないフィールド名称を指定している」／重複禁止の important 2 本 | **届かない。** いずれも名称が在ることを前提に**重複**と**引き当て**を言っているだけで、名称が無ければならないとは書いていない |
| `:892` | 「ディレクティブまたはレコード種別・フィールド名称定義の要素数が2未満である」 | **届かない。** 行（リスト）の要素数の話 |
| `:871` / `:1060` | 「フィールド名称——各フィールドの名称」／「フィールド名称——フィールドの数だけ記載する」 | **個別要素には届くが境界を決められない。** Excel の版面には「名称が `null`」と「名称が空文字」を区別するセルの状態が無い（どちらも空セル）。番人の境界はまさにその区別である |
| `:1140` | 「``fields:`` の各要素は `{name: フィールド名, type: データ型, length: バイト長}` の形式」 | **個別要素には届くが、必須要件の行ではない。** 同じ列挙に `length` が入っており、`length` は可変長ファイルでは不要（`:883`。§1-C／XLS-30）。つまりこの行は「形」を示している |
| 本体スキーマ `$defs.field_def` | `required` ＝ `["name","type"]`／`name` ＝ `{"type": "string"}` | **届く。** キー省略は `required` 違反、値 `null` は `string` 違反 |

### 実測（明文の確認を裏付けるためだけの使い捨てプローブ。リポジトリには残していない）

`FieldDef(null, "半角英字", "10")` を持つ `SETUP_FIXED` ブロックを辺③④へ渡した。

```
[辺③] java.lang.NullPointerException: Cannot invoke "String.length()" because "value" is null
[辺④] 例外なし
--- sheet.yaml ---
setup_files:
  - path: "bad.dat"
    type: "fixed"
    records:
      - record_type: "data"
        fields:
          - {name: null, type: "半角英字", length: "10"}
        rows:
          - ["v"]
```

その出力を `YamlTestDataValidator` に掛けた結果:

```
[.../sheet.yaml] $.setup_files[0].records[0].fields[0].name: [V-SCH] スキーマ非適合:
$.setup_files[0].records[0].fields[0].name: null が見つかりました、string が予期されました
```

`name: ""` へ差し替えると**検出 0 件**（`$defs.field_def.properties.name` に `minLength` が無いため）。
**スキーマの境界（`null` 不可・空文字は可）が、置こうとしていた番人の境界とちょうど一致している。**

> この実測は**判定の根拠ではない**（steering Decisions「記法の根拠に実装の挙動を使わない」）。
> 判定は上の表のとおり明文だけで組み立てており、実測はスキーマ違反が実際に検出されること・
> `issues.md` XLS-31 の「観測」欄が実物と一致することの確認に使った。

### `type` の先例（`f80c192`）はそのままは及ばない

- **解説書の中では名称とデータ型は対称である。** どちらも `:883`（3 リスト／2 リストが同サイズで必須）と
  `:888`（名称リスト**または**データ型リストが未指定または空）に**並んで**書かれており、
  **リストの水準**である点も、個別要素まで届いていない点も同じである。
- **差が出るのはスキーマのほうである。** `$defs.field_def.properties.type` には `minLength: 1` があるが
  `name` には無い。つまりスキーマは `type: ""` を弾き、`name: ""` は通す。
- したがって「`type` に番人を置いたのだから `name` にも置く」は自動的には言えない。
  逆に言えば、**辺③の `type` の番人も解説書の同じ行に依っている**という事実がここで見えた。
  これは §1-D の作業範囲外なので触っていない（報告のみ）。

### 未決（ユーザー判断）

- **本体スキーマ `$defs.field_def` だけを根拠に番人を置いてよいか。** 置くなら、スキーマが直接掛かるのは
  YAML（辺④）であり、**Excel（辺③）にはこれに当たる明文が無い**。辺③にも置くか、辺④だけに置くかは
  判断が要る。判断が付くまで**どちらにも置いていない**。
- **steering のチェックボックス（§1-D）は未チェックのまま。** steering はコーディネーターの持ち物のため編集していない。
- **`issues.md` 冒頭の集計（「課題は全部で 37 件」「要対応 7 件」）は現物とずれている**——
  `grep -c '^### \(XLS\|YML\)-'` → **44**、判定欄の `**要対応**` → **14**（XLS-08・XLS-27〜XLS-33 が
  後から増えたため）。本作業では判定を 1 件も動かしていないのでずれを持ち込んでもいないが、
  §1-B〜G・XLS-28 が済んだあとの一括の導き直し（steering `#25.5` の残タスク）で直す対象である。

### Self-check

- OK —— 明文の確認（`30a8271` の実物と本体スキーマ JSON を自分で開き、`フィールド名称` の全 17 箇所を
  行番号ごとに読んで届く／届かないを判定した）、条件 2（到達可能性を根拠にしない）の遵守、
  番人を置かないという結論と未決の `issues.md` への記録。**`src/main`・`src/test` は無変更**のため
  TDD の赤は不要（steering の指示どおり `docs` コミットにした）。`checks/` はコミットに含めていない。

---

## 追補 — §1-E（`TestDataBlock.groupId` が `null`）の明文確認（2026-08-18）

**結論: 番人を置かなかった。** 解説書（`testdata_notation.rst`・`30a8271`）と本体スキーマの明文が
定めているのは**版面／YAML に書かれる文字列**であって、その「書かれない」状態を中間モデルの
どの Java 値（`null` か空文字か）で表すかは決めていない。`src/main`・`src/test` は無変更で、
記録だけを `issues.md` XLS-32 に足した。

steering Decisions「中間モデル一巡点検で出た 7 件を全件 #25.5 に含める」の**条件 1**
（「番人を置く前に、その形が記法の外であることを明文で確かめる。明文が無ければ番人を置かず、
『明文が無い』と記録する」）と**条件 2**（「到達可能性を根拠にしない」）に従った。
**実装の挙動も到達可能性も根拠にしていない**（steering Decisions「記法の根拠に実装の挙動を使わない」）。

### 読んだ実物

```sh
cd /home/tie303177/work/nablarch/nablarch-document
git show 30a8271:ja/development_tools/testing_framework/implementation/testdata_notation.rst > /tmp/notation.rst
grep -n 'group_id\|グループID' /tmp/notation.rst    # 20/126/131/138/178-187/248-254/269/272/274/278/282/306/310/318/326/330/335/342/364/423/432/441/444/455/459/482-497/540-549/904/1016/1051-1052/1210/1253/1265/1273
sed -n '192,200p;244,280p;326,342p' /tmp/notation.rst
python3 - <<'PY'
import json
d=json.load(open('/home/tie303177/work/nablarch/nablarch-testing-yaml/src/main/resources/nablarch/test/ntf-testdata-yaml-schema.json'))
for n,v in d['$defs'].items():
    if 'group_id' in v.get('properties',{}):
        print(n, v.get('required'), v['properties']['group_id'])
PY
```

### 判定 —— 明文はどこまで届くか

| 出典 | 本文 | 何が決まるか／`null` に届くか |
|---|---|---|
| `:198` | 「データブロック先頭セルに ``データタイプ=識別子の値`` の形式で記載する。データタイプ名で始まっていれば合致する（前方一致）。例: ``SETUP_TABLE=USER_MASTER``」 | **Excel の識別子セルの形（グループ ID を書かない場合）が決まる** |
| `:278` | 「データタイプ名の直後に ``[グループID]`` を付ける。例: ``SETUP_TABLE[case_001]=EMPLOYEE_TABLE``」 | **Excel の識別子セルの形（書く場合）が決まる。括弧付きである** |
| `:1016` ／ `:1265` | ``SETUP_FIXED[グループID]=ファイルパス`` ／ ``EXPECTED_REQUEST_BODY_MESSAGES[グループID]=リクエストID`` | 同じ形がファイル系・電文系にも示されている |
| `:254` | 「グループIDを省略した場合は、グループIDを持たないデータブロック（デフォルトグループ）が対象になる。…」 | **省略が正当な状態であることが決まる。`null` には触れていない。** 「省略か値ありの 2 値」を定めているのはこの行ではない |
| `:269` | 「グループIDが一致する複数ブロックを収集する。Excel形式では ``データタイプ + グループID + '='`` による前方一致…」 | **収集の判定方法。式に括弧が含まれるか読み取れないため、出力形の根拠には使わない**（`:278` に置く） |
| `:330` | 「エントリに ``group_id:`` フィールドを設ける。」 | YAML の書き方（キーを置く／置かない） |
| 本体スキーマ `$defs.table_data` ／ `file_data` ／ `expected_request_message_data` ／ `group_message_data` の `group_id` | 4 定義とも `{"type":"string","minLength":1}`。**`required` に入っていない任意キー。** description に「省略時はグループIDなし（デフォルトグループ）扱い」「空文字 `""` は誤マッチを引き起こすため `minLength: 1` で禁止」 | **書くなら 1 文字以上の文字列。書かない（キーを置かない）ことは適合。したがって `null` をキー省略へ写せば適合してしまい、`null` を排除しない** |

**届かない理由（§1-D と同じ構造）**

- `""` も `null` も、Excel では「データタイプ名の直後に何も無い」、YAML では「`group_id:` を置かない」という
  **同じ 1 つの出力**にしかならない。記法はこの 2 つを区別しない。§1-D で `FieldDef.name` の `null` と
  空文字を Excel の版面が区別できなかったのと同じである。
- **§1-D より弱い。** §1-D では `$defs.field_def.required` が `name` を含むため `null` をキー省略へ
  逃がせず、スキーマだけは明文が届いていた。**§1-E の `group_id` は任意キーなので、そこも届かない。**
- したがって「`null` を省略として正規化する」という是正も明文には一切違反せず、
  **明文だけからは「弾く」と「省略として扱う」のどちらが正しいかを決められない。**
- 「中間モデルの Javadoc 契約が『省略時は空文字』だから `null` は契約の外」は正しいが、
  **それは中間モデル自身の契約であって記法の明文ではない。**

**明文で決着していること（§1-D との違い）**

- **辺③の現状出力 `SETUP_TABLEnull=T` は記法に無い形である**（`:198`・`:278`。括弧の無い `null` が
  データタイプ名と `=` の間に入る形はどの行にも無い）。**したがって現状維持は選べない。**
- 辺④の `NullPointerException`（`Cannot invoke "String.isEmpty()" because "groupId" is null`）も
  原因を示さないため直す対象である。

### 実測（明文の確認を裏付けるためだけの使い捨てプローブ。リポジトリには残していない）

`TableDataBlock(SETUP_TABLE_DATA, groupId, "T", ["C1"], [["v1"]])` を辺③（`XlsFormatWriter#build`）と
辺④（`YamlFormatWriter#serialize`）へ渡した（`build`／`serialize` がパッケージプライベートのため、
プローブは `...converter.xls` と `...converter.yaml` の 2 本に分けて置き、実行後に削除した）。

```
### XLS groupId=null
XLS OK:
[SETUP_TABLEnull=T]
[C1]
[v1]

### XLS groupId=""
XLS OK:
[SETUP_TABLE=T]

### XLS groupId="[g1]"
XLS OK:
[SETUP_TABLE[g1]=T]

### YAML groupId=null
YAML EX: java.lang.NullPointerException: Cannot invoke "String.isEmpty()" because "groupId" is null

### YAML groupId=""
YAML OK:
setup_tables:
  - table: "T"
    rows:
      - C1: "v1"

### YAML groupId="[g1]"
YAML OK:
setup_tables:
  - group_id: "g1"
    table: "T"
    rows:
      - C1: "v1"
```

**`issues.md` XLS-32 の「観測」欄の記述と一致した。** 記録を鵜呑みにせず自分で実行して確かめている。

### 空文字（＝省略）が正しい形であることの確認と、番人を置く場合の担保

- 明文: `:254`（省略は正当な状態）＋スキーマ description（「省略時はグループIDなし（デフォルトグループ）扱い」
  ／「空文字 `""` は…`minLength: 1` で禁止」）。**空文字は「書いてはいけない」のであって、中間モデルが
  空文字で省略を表すこと自体は禁じられていない** —— 辺④は空文字のとき `group_id:` キーごと書かない。
- 実測: 上のとおり、空文字は辺③が `SETUP_TABLE=T`（括弧なし）、辺④が `group_id:` 行なしを出す。
- **番人を置く場合に「空文字を弾かない」ことを担保する既存テスト**（新規に足す必要はない）:
  - 辺③ `XlsFormatWriterTest#writesTableBlock`（`groupId` ＝ `""` → `SETUP_TABLE=USERS`）
  - 辺④ `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation`
    （`groupId` ＝ `""` の出力を全文一致でアサートし、`group_id:` 行が無いことを固定している）
  - 値ありの側は 辺③ `XlsFormatWriterTest#writesTableMarkerWithGroupId`（`EXPECTED_TABLE[g1]=USERS`）／
    辺④ `YamlFormatWriterTest#serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId`
- **置く場合の置き場所**（実装はしていない。調べた結果のみ）: `groupId` は `TestDataBlock` の共通フィールドで
  **ブロック 4 種別すべてが持つ**ため、辺③は `XlsFormatWriter#marker`（`getDataType().getName() +
  getGroupId() + "=" + getIdentifier()` を組む唯一の場所）、辺④は `YamlFormatWriter#rawGroup`
  （`emitGroupId` から呼ばれ、`emitTable`／`emitListMap`／`emitFile`／`emitMessage` の 4 経路が合流する）で、
  **どちらも 1 箇所で全種別を覆える**。ブロック種別ごとに検査を書き散らす必要はない。

### 未決（ユーザー判断）

- **是正の手段をどちらにするか。** 明文はどちらも許す。判断が付くまで**辺③④のどちらにも番人を置いていない**。
  - (a) **番人を置く**（§1-B・§1-C と同じ形）。根拠は記法の明文ではなく**中間モデルの Javadoc 契約**になる。
  - (b) **`null` を省略として正規化する**（`""` と同じ扱い）。出力は記法に適合するが、同じ意味に 2 表現を
    許すことになり、本作業が排してきた「黙って倒す」側の挙動でもある。
  - **推奨は (a)** —— 中間モデルの契約が既に 1 表現に定めていること、§1-B・§1-C・`f80c192` と揃えて
    「契約違反は書き出し辺で loud に止める」形が 4 辺の担保として一貫すること。**根拠が明文でない以上、判断は委ねる。**
- **steering のチェックボックス（§1-E）は未チェックのまま。** steering はコーディネーターの持ち物のため編集していない。
- **括弧の無い非空グループ ID（例 `"g1"`）を中間モデルが保持できてしまう穴は §1-E の対象外**として触れていない
  （`issues.md` XLS-32 に記録のみ。**実行しての確認はしていない —— 未確認**）。
- **`issues.md` 冒頭の集計のずれ**（§1-D の追補で開示したもの）は本作業でも動かしていない。

### Self-check

- OK —— 明文の確認（`30a8271` の実物と本体スキーマ JSON を自分で開き、`グループID`／`group_id` の全出現を
  行番号ごとに読んで届く／届かないを判定した）、条件 2（到達可能性を根拠にしない）の遵守、
  現状挙動の自力実測（XLS-32 の記録と一致）、空文字が正しい形であることの確認と担保テストの特定、
  番人を置かないという結論と未決の `issues.md` への記録。**`src/main`・`src/test` は無変更**のため
  TDD の赤は不要（指示どおり `docs` コミットにした）。
- OK —— `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` →
  **`Tests run: 554, Failures: 0, Errors: 0, Skipped: 0`**／`BUILD SUCCESS`。
- OK —— `checks/task-25.5.md` はコミットに含めていない。QA 列・`## QA Expert Review`・
  `## Expert Reviews`・`## Overall Verdict` には触れていない。
