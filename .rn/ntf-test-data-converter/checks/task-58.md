# #58 自己点検 —— 単一フィールドの電文・ファイルでディレクティブ行を名前行と誤認する不具合（XLS-46）の修正の記録

修正コミット: `7a97049`（2026-10-13。`fix: 単一フィールド電文で名前行をディレクティブ行と誤認する不具合を修正`）
記録日: 2026-10-19（修正は .rn の手順の外で先に入ったため、本書は後追いの記録である）
課題: `coverage/issues.md` XLS-46

---

## 1. 経緯

- 2026-10-02: `nablarch-example-http-messaging-send` の YAML 移行で、Excel なしの `mvn clean test` が 3 件とも
  `YamlSchemaValidationException`（`fields[0].length: "JSON"`）で失敗した。利用者側は当初
  「nablarch-testing-yaml が JSON 電文を未サポート」と判断した。
- 2026-10-08 の検証で、真因が converter の名前行の取り違え（＋利用者側の diConfig の設定漏れ）と判明した。
- 2026-10-13: `7a97049` で修正。同日、利用者側で修正版 converter を使って再変換し、Excel なしで 3/3 成功した
  （同 PJ `e8fcdda`）。
- 2026-10-19: 本タスク #58 として記録した（`steering.md`・本書・`issues.md` XLS-46・`inventory.md` 追補その 22）。

## 2. 変更内容（`7a97049`）

- `XlsFormatReader#skipToFirstNameRow` に当該ブロックのディレクティブ名の集合（`view.getDirectives().keySet()`）を渡す。
- `XlsFormatReader#isNameRow` を新設。先頭セルがディレクティブ名の生行は名前行とみなさない。それ以外は
  従来どおり `tail(line).equals(names)` で判定する。
- 回帰テスト 2 件を `XlsFormatReaderAdapterContractTest` に追加:
  - `locatesNameRowAfterDirectiveRowWhenSingleFieldNameCollidesWithDirectiveValue`
  - `doesNotSkipLegitimateSingleFieldNameRowWhenNoDirectiveRowPresent`

```
$ git diff --stat 4a59436 7a97049 -- src/
 .../test/tool/converter/xls/XlsFormatReader.java   | 33 +++++++-
 .../xls/XlsFormatReaderAdapterContractTest.java    | 99 ++++++++++++++++++++++
 2 files changed, 129 insertions(+), 3 deletions(-)
```

`7a97049` 以降、`src/` の変更は無い（本タスクの記録コミットは `.rn/` のみ）。

## 3. 検証（2026-10-19 実測）

```
$ JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 LANG=ja_JP.UTF-8 LC_ALL=ja_JP.UTF-8 mvn -q clean test -Djacoco.skip=true
（rc=0。surefire XML 59 ファイルを集計）
Tests run: 734, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

- 734 ＝ 732（#57 時点）＋ 2（本修正の回帰テスト）。件数の導出は `inventory.md` 追補その 22。
- 本機の JDK は `/usr/lib/jvm/java-17-openjdk-amd64`（Rules の `temurin-17-jdk-amd64` は本機に無い）。
- 利用者側での確認（2026-10-13、`nablarch-example-http-messaging-send`）: 修正版で再変換した YAML に
  `length: "JSON"`／`record_type: "file-type"` は 0 件。Excel なしで `Tests run: 3, Failures: 0, Errors: 0`。

## 4. Completion criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| 単一フィールドでディレクティブ値とフィールド名が衝突しても、本当の名前行から原文を復元する | OK | `locatesNameRowAfterDirectiveRowWhenSingleFieldNameCollidesWithDirectiveValue` が PASS |
| ディレクティブ行が無いとき、正当な名前行を読み飛ばさない | OK | `doesNotSkipLegitimateSingleFieldNameRowWhenNoDirectiveRowPresent` が PASS |
| 既存の挙動を変えない | OK | 既存 732 件を含む 734 件全件 PASS。既存テストの期待値は変えていない |
| 課題として記録する | OK | `issues.md` XLS-46（要対応・修正済み）。件数 57 → 58、要対応 27 → 28 を導出コマンドで確認 |
| 件数をコマンドから導き直す | OK | `inventory.md` 追補その 22（731 → 734。#55 の未記録 ＋1 も開示） |

## 5. 残る差・未決

- **MESSAGE の FW 制御ヘッダ行は除外していない**（ユーザー判断待ち）。`notation:1267` は FW 制御ヘッダを
  ディレクティブと同じ「名前・値」形式で名前行より前に書くと定めるが、その値は `view.getDirectives()` に
  入らないため `isNameRow` の除外に掛からない。発現条件・影響・実データの走査結果は `issues.md` XLS-46 の
  「残る差」。
- push・PR は行わない（ユーザー指示・2026-10-19。push はユーザー側で行う）。Rules の「タスク完了後は即 push し
  PR を作成する」は本タスクには適用しない。
