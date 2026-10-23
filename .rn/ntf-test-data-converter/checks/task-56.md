# #56 調査報告 —— ヘッダ行だけの電文ブロック・ファイルブロックの `rows: []` を yaml が拒否する件

指示書: `/home/tie303177/work/cowork/nablarch/ntf-doc-renewal/指示/ntf-step4-17-converter-empty-message-rows.md`（§5 の追記を含む）
ピン: `nablarch-testing@ae989ec` ／ `nablarch-testing-yaml@5a713fb`（スキーマの実体は `f3620fc` 以降不変）／ `nablarch-testing-converter@878ef9a`
ハーネス: `/tmp/claude-1000/-home-tie303177-work-nablarch-nablarch-testing-converter/9795ea9d-549b-469c-8714-8cf295e72d18/scratchpad/step4-17/`
（scratchpad のため消えることがある。作り直し方は本書 §2 に全部書いてある）

**実装はしていない。ソースは 1 行も変更していない。**

---

## 1. ①〜⑥（指示書 §1 の表）

| 段 | 電文ブロック（`expected_request_*_messages`） | ファイルブロック（`setup_files` ／ `expected_files`） |
|---|---|---|
| ① 現行の仕様 | **カラム名の行だけ書いてデータ行を 0 にすると、「レコードレイアウト（フィールド定義）は与えるが、期待するデータ行は 0 件」を意味する。** 用途は 2 つあり、どちらも `fields` が要る。(1) 取引単体テスト（MQ 経路）: `SendSyncSupport.parseRequestMessage` が `EXPECTED_REQUEST_*_MESSAGES` の **フォーマッタだけ**を使って実際の要求電文をパースしログへ出す（`SendSyncSupport.java:67-69`（ヘッダ）・`:82-84`（本文）。データ行は参照しない）。(2) リクエスト単体テスト: `RequestTestingMessagingClient.assertSendingMessage` が期待ヘッダの件数と実送信件数を突き合わせる（`RequestTestingMessagingClient.java:337,353,357,366`）。**期待 0 件は「この requestId で電文が 1 通も送られないこと」の検証**になる | **「このレコードレイアウトのデータ行が 0 件」を意味する。**書き出すと **0 バイトのファイル**になるが、レイアウト（レコード定義 1 件・`record-length`）は保持される |
| ② あるべき姿 | 同じ。Excel で書けたものは YAML でも書けるべき | 同じ |
| ③ GAP | **不具合。**YAML スキーマにはこの形の表現が**存在しない**。`rows: []` は `record_fragment.rows` の `minItems: 1`（`ntf-testdata-yaml-schema.json:459`）で拒否され、代替の `records: []` も電文では `minItems: 1`（`:247`／`:281`／`:313`）で拒否される。**Excel で書けて YAML で書けないブロックがある** | **不具合。**`rows: []` は同じく拒否される。`records: []` は `file_data.records` が `minItems: 0`（`:181`）なので通るが、**意味が違う**（下の実測 §2-4 を参照。レコード定義 1 件・`record-length: 10` が 0 件・`record-length: 0` になる） |
| ④ 決める仕様 | 不具合なので②。**`rows: []` を「データ行 0 件」として正式に認める** | 同上 |
| ⑤ 各形式の表現 | 下の §3 | 下の §3 |
| ⑥ 解説書に書くこと | 下の §4 | 下の §4 |

**①の要点**: 指示書 §5-2 は「本体がヘッダ行だけのブロックに `records: []` では表せない意味を与えていると実測で示せた場合だけ GAP として報告する」としていた。**示せた**（§2-2・§2-4）。`records: []` はレコード定義そのものを捨てるため、`parseRequestMessage` が使うフォーマッタも `assertSendingMessage` が使う期待ヘッダのレイアウトも失われる。電文ではそもそもスキーマが `records: []` を認めない。

---

## 2. 実測（指示書 §2）

### 2-1. 対象は特殊なケースではない —— 本体のテストデータ 12 ファイル 15 ブロックがこの形

`nablarch-testing@ae989ec` の `src/test/java/nablarch/test/core/messaging/data/*.xls`（44 個の `EXPECTED_REQUEST_*` ブロック）を変換ツールで YAML にし、`rows: []` を数えた。

```
RM11AC0202 / RM11AC0203 / RM11AC0204 / RM11AC0205 / RM11AC0206 / RM11AC0207 /
RM11AC0292 / RM11AC0293 / RM11AC0294  … expected_request_body_messages が 0 行（各 1 ブロック）
RM11AC0295 / RM11AC0296 / RM11AC0208  … header・body の両方が 0 行（各 2 ブロック）
合計 15 ブロック / 12 ファイル
```

`RM11AC0208` は `EXPECTED_REQUEST_HEADER_MESSAGES[case1]` ／ `EXPECTED_REQUEST_BODY_MESSAGES[case1]` の
**両方**が 0 行で、グループ ID 付き＝リクエスト単体テストの記法である（＝①(2) の「0 通送信の検証」の形）。

### 2-2. (a) 本体で直接読ませた結果

`BasicTestDataParser`（`PoiXlsReader` ＋ interpreter なし）で `RM11AC0202.xls` を読む（取引単体テスト経路 `getMessageWithoutCache`）:

```
---- EXPECTED_REQUEST_BODY_MESSAGES ----
  toDataRecords().size() = 0  ← データ行の件数
  formatter definition = records=1
    record[no] fields = [XML]
    directives = {file-type=Fixed, text-encoding=UTF-8, record-length=1}
---- RESPONSE_BODY_MESSAGES ----
  toDataRecords().size() = 1
  formatter definition = records=1
    record[no] fields = [XML1, XML2, XML3]
    directives = {file-type=Fixed, text-encoding=UTF-8, record-length=117}
```

**データ行 0 件・レコード定義 1 件（フィールド `XML`・`record-length=1`）**。この `formatter` が
`SendSyncSupport.parseRequestMessage` の使うものである（`SendSyncSupport.java:82-84`）。

リクエスト単体テスト経路（`getSendSyncMessage` にグループ ID `[case1]`）で `RM11AC0208.xls`:

```
---- EXPECTED_REQUEST_HEADER_MESSAGES ----
  requestId=RM11AC0208 getExpectedMessageList().size()=0
---- EXPECTED_REQUEST_BODY_MESSAGES ----
  requestId=RM11AC0208 getExpectedMessageList().size()=0
```

この 0 が `RequestTestingMessagingClient.java:357`／`:366` の `expectedHeaderRecords.size()` に入る。
実送信 0 通なら両方の条件が偽で通り、1 通でも送られると
`number of send message was invalid. expected number=[0], but actual number=[1].` で落ちる。

### 2-3. (b) 変換ツールの出力 と (c) yaml モジュールで読んだ結果

`RM11AC0202.xls` → `TestDataConverter.convert(XLS, YAML, ...)`:

```yaml
expected_request_body_messages:
  - id: "RM11AC0202"
    directives:
      file-type: "Fixed"
      text-encoding: "UTF-8"
    records:
      - record_type: "no"
        fields:
          - {name: "XML", type: "全半角", length: "1"}
        rows: []
```

これを `YamlLoader.load` に渡す:

```
NG: nablarch.test.core.reader.yaml.YamlSchemaValidationException
$.expected_request_body_messages[0].records[0].rows: 少なくとも 1 個の項目が必要ですが、0 が見つかりました
```

代替表現も実測した（同じファイルを手で書き換えて読ませた）:

| 書き方 | 結果 |
|---|---|
| `rows: []` | ✗ `$.expected_request_body_messages[0].records[0].rows: 少なくとも 1 個の項目が必要` |
| `records: []` | ✗ `$.expected_request_body_messages[0].records: 少なくとも 1 個の項目が必要` |
| ブロックごと削除（#97 の「手で外した」形） | ○ 読める。ただし①の 2 用途がどちらも失われる |

### 2-4. ファイルブロックでの実測 —— `rows: []` と `records: []` は本体で意味が違う

scratchpad で 2 種類の Excel を作り、本体（`getSetupFile`）と変換ツールと yaml に通した。

| Excel の形 | 本体 `createLayout()` | `write()` の出力 | 変換ツールの出力 | yaml で読む |
|---|---|---|---|---|
| カラム名の行あり・データ行 0 | **records=1**、`{file-type=Fixed, text-encoding=UTF-8, record-length=10}` | 0 バイト | `rows: []` | ✗ `rows` の `minItems` |
| カラム名の行なし | **records=0**、`{file-type=Fixed, text-encoding=UTF-8, record-length=0}` | 0 バイト | `records: []` | ○（`rows` 由来のエラーは出ない） |

**書き出したファイルはどちらも 0 バイトだが、レイアウトが違う。**`records: []` はレコード定義を 0 件にし
`record-length` を 0 にする。`expected_files` は実ファイルをこのレイアウトで読んで突き合わせるため、
両者を同一視できない。**したがってファイルブロックでも `records: []` は `rows: []` の代替にならない。**

実データでも確認した。Climan サンプル
`src/test/java/nablarch/test/tool/converter/SampleConversionTest/ExportProjectsInPeriodActionRequestTest/testNormalEnd.yaml`
の `expected_files[2]`／`[3]` は 12 フィールドを持つ可変長ブロックの `rows: []` である
（「この CSV は 0 行であること」の期待値）。

### 2-5. 別件で見つけた 2 件目の GAP —— `record-length` が string で出る

§2-4 の実測で、`rows` とは別に必ずこのエラーが出た:

```
$.setup_files[0].directives.record-length: string が見つかりました、integer が予期されました
```

- スキーマは `$defs.directives.properties.record-length` を `{"type": "integer"}` とする（`max-record-length` も同じ）
- 中間モデルはディレクティブ値を `String` で持つ（`YamlFormatReaderRealFileTest.java:532` が
  「integer が文字列になる」を固定している）
- `YamlFormatWriter.emitMap` は値を `q()`（`YamlFormatWriter.java:435-441`。SnakeYAML の scalar dump）で
  出すため、数値に見える文字列は `"10"` と引用符付きになる

**`record-length` を持つ固定長 Excel を変換すると、`rows` を直しても yaml が読めない。**
これは `f3620fc`（ディレクティブの型別限定）と同じ由来で、`ntf-step4-17` の射程外だが同じ経路で必ず踏む。
**扱いを決めていただきたい。**

### 2-6. (§2-4) `#97` で「手で外した」ブロックが解説書のどこに当たるか

**解説書には当たるものが無い。**該当ページ
`nablarch-document@2a02b011`（`origin/ntf-yaml-support`）の
`ja/development_tools/testing_framework/setup/deal_unit_test/http_messaging.rst`（142 行）は
コンポーネント設定ファイルの例だけで（`:56-106` Excel形式の場合／`:108-142` YAML形式の場合）、
テストデータのブロックは 1 つも載っていない（`git grep EXPECTED_REQUEST_BODY_MESSAGES` が 0 件）。

`#97` FB3 が動かしたのは、この `:108-142` の YAML 設定例を使って
`nablarch-testing@ae989ec` の `MockMessagingClientTest` を走らせるハーネスであり、
外したブロックはそのテストデータ（`src/test/java/nablarch/test/core/messaging/data/RM11AC02xx.xls` の
変換結果）の `expected_request_body_messages` である。

**なぜ外しても緑だったか**も判明した。`MockMessagingClientTest` が使う
`MockMessagingClient.sendSync` は `RESPONSE_BODY_MESSAGES`／`RESPONSE_HEADER_MESSAGES` しか読まない
（`MockMessagingClient.java:57,70`）。`EXPECTED_REQUEST_*` を読むのは
`MockMessagingContext.sendSync`（`:55` → `parseRequestMessage`）と
`RequestTestingMessagingClient`／`Provider` であり、`#97` のハーネスはそこを通らない。
**つまり「外しても緑」はこのハーネスに限った話で、①の 2 用途が失われることの反証にはならない。**

---

## 3. ⑤ 各形式の表現 —— 案・根拠・影響範囲

Excel 側は現状のまま（カラム名の行を書き、データ行を書かない）。論点は YAML 側だけである。

### 案A（推奨）: `record_fragment.rows` の `minItems: 1` を外し、`rows: []` を「データ行 0 件」と定義する

- **根拠**: ①で「レコードレイアウトは与えるがデータ行は 0」に本体が意味を与えていることが実測で示せた
  （§2-2・§2-4）。`records: []` はレコード定義を落とすため代替にならず、電文ではスキーマ自体が認めない。
  指示書 §5-2 の除外条件（「示せた場合だけ GAP として報告する」）に当たる
- **`records: []` との住み分け**: `records: []` ＝ レコード定義が無い（0 バイトの空ファイル。`record-length` も 0）。
  `rows: []` ＝ レコード定義はあるがデータ行が 0。**両方残す。**現行 description の
  「0バイトの空ファイルは、レコード定義を持たないブロックとして `records:` に空配列 `[]` を書いて表す」は
  正しいまま（ファイルの話）で、これに `rows: []` の意味を足す
- **影響範囲**:
  - yaml: `ntf-testdata-yaml-schema.json:459` の `minItems: 1` を削除し、`:459` の description
    「データは1件以上記述する（…）」を書き直す。`rows: []` を読めることのテストを追加
  - converter: **実装の変更なし。**§5-1 の 6 件は下の §3-3 のとおりテスト側だけで閉じる
  - 解説書: §4
- **弱点**: `minItems: 1` は user 確定（2026-08-31・`.rn/ntf-yaml/steering.md` `#49` Steps A）。**覆すには user の再判断が要る。**
  ただし当時の確定は「0 バイトの空ファイルをどう表すか」の文脈であり、ヘッダ行だけの電文ブロックは論点に入っていなかった（description の書きぶりがファイル前提）

### 案B: 変換ツールが 0 行のブロックを `records: []` に畳む

- **根拠になりうる点**: yaml の既決に触らない
- **却下する理由**: **意味が変わる**（§2-4 の実測。`record-length` 10 → 0、レコード定義 1 → 0）。
  電文ではそもそも `records` の `minItems: 1`／`maxItems: 1` で `records: []` が書けない（§2-3）。
  Excel → YAML → Excel の往復で情報が落ちる
- **影響範囲**: converter の辺③④と `RoundTripTest` を作り替えることになる。採らない

### 案C: 変換ツールが 0 行のブロックを出力しない（#97 の手作業の自動化）

- **却下する理由**: ①の 2 用途がどちらも失われる。とくにリクエスト単体テストでは
  「0 通送信であることの検証」が黙って消える（`RM11AC0208` がその形。§2-2）。
  変換ツールが検証を減らす方向に情報を捨てるのは、変換ツールの役目に反する
- **影響範囲**: 採らない

### 3-3. `ntf-step4-14` §3（`878ef9a`）の赤 6 件を、案A のもとでどう直すか

| # | テスト | `rows: []` の役割 | 直し方 |
|---|---|---|---|
| 1 | `SampleConversionTest#convertsClimanSampleIncludingZeroRowTable` | 0 行のレコードレイアウトを仕様として担保 | **直さない。**案A で緑になる（実装・テストとも変更なし） |
| 2 | `YamlFormatReaderRealFileTest#readsEmptyRowsFromRecordLayoutWithoutRows`（`:288-`） | 同上 | **直さない。**案A で緑になる |
| 3 | `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFieldsIsEmpty`（`:368`） | `fields: []` を検証したいのに `rows: []` も同時に引っかかり `[minItems]` が 2 件になった | **テストを直す。**案A で `rows` の `minItems` が消えるため期待は `[minItems]` 1 件に戻る。フィクスチャは変えない |
| 4-6 | `YamlTestDataValidatorTest#vdkey_noDirectivesSection_noError`／`#vfname_duplicateInSameFragment_reportsError`／`#vfname_duplicateInSameFragment_variable_reportsError` | フィクスチャの都合で `rows: []` を書いているだけ | **直さない。**案A で緑になる |

**実装（`src/main`）の変更は 1 行も要らない。**案A を採るなら converter 側は yaml の新版を install し直すだけで、
テストの修正は 3 番の期待値 1 箇所である。**案A を採らない場合は 6 件すべてのフィクスチャを書き換えることになり、
そのうち 1・2 は「0 行のレコードレイアウトを仕様として担保する」テストなので、担保を落とすことになる。**

---

## 4. ⑥ 解説書に書くこと（案。解説書は変更していない）

`nablarch-document` `ja/development_tools/testing_framework/implementation/testdata_notation.rst`。

1. **「メッセージングのデータを記述する」（`:1224-`）に 0 行ブロックの節を足す。**
   現状この節に 0 行の記述は無い（`rows: []` に触れるのは `:775`・`:885`・`:891` で、いずれも
   「テーブルのデータを記述する > 0件のデータを記述する」＝テーブル向け）。書く内容:
   - Excel ではカラム名の行だけ書いてデータ行を書かない、YAML では `rows: []` と書く
   - 意味は「レコードレイアウトは与えるが期待するデータ行は 0 件」
   - 取引単体テストでは要求電文をログへ出すためのレイアウトになり、リクエスト単体テストでは
     「その requestId で電文が 1 通も送られないこと」の検証になる
2. **「ファイルのデータを記述する」（`:897-`）に、`rows: []` と `records: []` の違いを書く。**
   `rows: []` はレコードレイアウトを保った 0 行、`records: []` はレコード定義を持たない 0 バイトの空ファイル
3. スキーマ `ntf-testdata-yaml-schema.json` の `record_fragment.rows` の description から
   「データは1件以上記述する」を削り、上の 2 点に合わせる

---

## 5. 再現手順（ハーネス）

`~/work/nablarch/nablarch-testing`（`ae989ec`）・`~/work/nablarch/nablarch-testing-yaml`（`5a713fb`）・
本リポ（`878ef9a`）が clone 済みで、`~/.m2` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` が
`e984103` 以降で install 済みであること。JDK は `/usr/lib/jvm/temurin-17-jdk-amd64`。

1. `mvn dependency:build-classpath` を converter と nablarch-testing の両方で取る
2. `DumpXls`（POI で .xls を全セル出力）・`ProbeMain`／`ProbeGroup`（`BasicTestDataParser` で
   本体の読み取り結果を出す）・`ConvertMain`（`TestDataConverter.convert`）・
   `ReadYamlMain`（`YamlLoader.load`）・`MakeFileXls`／`MakeNoRecXls`（POI で Excel を作る）を
   scratchpad に置いて `javac`／`java` で回す（ソースは上記 scratchpad ディレクトリ）
3. classpath には nablarch-testing の `target/test-classes` と `src/test/resources` を含める
   （`log.properties` がそこにあり、無いと `LoggerManager` の static 初期化で落ちる）
