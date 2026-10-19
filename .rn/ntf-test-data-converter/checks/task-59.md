# #59 自己点検 —— MESSAGE の FW 制御ヘッダ行も名前行の候補から除く（XLS-46 の残り）

記録日: 2026-10-19
課題: `coverage/issues.md` XLS-46
由来: #58 の「残る差」（`checks/task-58.md` §5）。ユーザー確定（2026-10-19）: 除外する

---

## 1. 不具合

#58（`7a97049`）は、先頭セルがディレクティブ名（`view.getDirectives().keySet()`）の生行を名前行の候補から
除いた。MESSAGE の FW 制御ヘッダ行は除いていなかった。

- `notation:1267`: FW 制御ヘッダは「フィールド名称行（先頭セルに `no` と記載する行）より前に
  `| フィールド名 | 値 |`（ディレクティブと同じ『名前・値』形式）で記載する」。
- 本体 `nablarch-testing` の `MessageParser#processDirectives`: ディレクティブでない行でも、先頭セルが
  FW 制御ヘッダ名（既定 `requestId`／`userId`／`resendFlag`／`resultCode`。`reader.fwHeaderfields` で変更可）
  なら `fwHeader` に入れて設定行として扱う。
- `fwHeader` は `view.getDirectives()` に入らないため、#58 の除外に掛からない。本文のフィールドが 1 つで
  FW ヘッダ値がそのフィールド名と一致すると、FW ヘッダ行を名前行と誤認する。

## 2. 赤（修正前）

`XlsFormatReaderTest` に 2 件を先に足した。どちらも `readerOf`（本体パーサを通す経路）で読む。

| テスト | 入力の要点 |
|---|---|
| `readSkipsFwHeaderRowWhoseValueCollidesWithSingleBodyFieldName` | `requestId \| body1` → `no \| body1` |
| `readSkipsLaterFwHeaderRowWhoseValueCollidesWithSingleBodyFieldName` | `text-encoding \| ms932` → `requestId \| RM11AC0101` → `userId \| body1` → `no \| body1` |

```
$ mvn -q test -Djacoco.skip=true -Dtest=XlsFormatReaderTest
Tests run: 37, Failures: 2, Errors: 0, Skipped: 0
readSkipsLaterFwHeaderRowWhoseValueCollidesWithSingleBodyFieldName ... but: was "userId"
readSkipsFwHeaderRowWhoseValueCollidesWithSingleBodyFieldName ... but: was "requestId"
```

レコード種別が FW ヘッダ名になっており、不具合が本体パーサ経由でも再現した。

## 3. 緑（修正）

- `XlsFormatReader#toRecordLayouts` が除外の集合 `settingRowNames` を引数で受け取る形にした。
- `readMessageBlock` は、ディレクティブ名と FW ヘッダ名（本体が読んだ `message.getFwHeader().keySet()`）の
  和集合を渡す。FW ヘッダ名を converter 側の固定値にしないので、`reader.fwHeaderfields` の設定にも本体と
  同じく従う。
- ファイル系（`readFileBlocks`）・同期送信系 4 種（`readSendSyncBlocks`）は従来どおりディレクティブ名だけを
  渡す（本体 `SendSyncMessageParser` は FW 制御ヘッダを特別扱いしない）。
- `skipToFirstNameRow`／`isNameRow` の引数名を `directiveNames` → `settingRowNames` に改め、Javadoc を更新した。

```
$ git diff --stat 5d6075e -- src/
 .../test/tool/converter/xls/XlsFormatReader.java   | 45 ++++++++-----
 .../tool/converter/xls/XlsFormatReaderTest.java    | 78 ++++++++++++++++++++++
 2 files changed, 105 insertions(+), 18 deletions(-)
```

## 4. 検証（2026-10-19 実測）

### 4-1. 全件

```
$ JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 LANG=ja_JP.UTF-8 LC_ALL=ja_JP.UTF-8 mvn clean test -Djacoco.skip=true
[INFO] Tests run: 736, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

736 ＝ 734（#58 時点）＋ 2。件数の導出は `inventory.md` 追補その 23。

### 4-2. 既存 Example の変換結果が変わらないこと

- 入力: Example 10 PJ（batch／db-queue／http-messaging／http-messaging-send／mom-delayed-receive／
  mom-delayed-send／mom-sync-receive／mom-sync-send-batch／rest／web）の `ntf-yaml-support` ブランチで削除された
  Excel を、各 PJ の git 履歴（削除コミットの親）から復元した 33 冊。同名の `.xls`／`.xlsx` は `.xls` を採った
  （http-messaging-send・web の 2 組）。
- 修正前: `7a97049` を一時 worktree でコンパイルしたクラス。修正後: 作業ツリーのクラス。
  どちらも `TestDataConverter.convert(DataFormat.XLS, DataFormat.YAML, in, out)` で変換した（作業は scratchpad。
  worktree は比較後に削除した）。
- 結果: 両者とも YAML 119 ファイル。`diff -r` で**完全一致**。
- `nablarch-example-http-messaging-send` の出力 4 ファイルは、同 PJ にコミット済みの YAML ともバイト一致。
- → **Example の再変換は不要**。

### 4-3. `~/.m2` の更新

`mvn -o clean -DskipTests -Djacoco.skip=true install` で `nablarch-testing-converter-1.0.0-SNAPSHOT.jar` を
更新した（2026-10-19 17:00）。他セッションの再変換（nablarch-example-web など）はこの jar を使う。

## 5. Completion criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| MESSAGE で FW ヘッダ値が単一フィールド名と一致しても、本当の名前行から原文を復元する | OK | 新設 2 件が赤 → 緑 |
| ディレクティブ行の除外（#58）を壊さない | OK | `XlsFormatReaderAdapterContractTest` の #58 の 2 件を含む 736 件全件 PASS |
| 既存 Example の変換結果が変わらない | OK | 4-2（33 冊・119 ファイル完全一致） |
| 件数をコマンドから導き直す | OK | `inventory.md` 追補その 23（734 → 736） |

push・PR は行わない（ユーザー指示・2026-10-19）。
