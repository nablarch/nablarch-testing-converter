# task-24 Completion Check

本ファイルは **#24 の最終状態**を記録する。到達までの経過（何巡目の指摘か、途中の再検証ログ）は残さない。
レビュー指摘とその処置は「レビュー指摘の処置」に一覧で残す。

## 辺② 軸D の導出（スキーマ起点。2026-08-13）

steering Decisions「軸D の対象範囲」に従い、別途渡された「YAML スカラー 10 ケース」表を根拠にせず、
スキーマと実測から導き直した。

### 1. スキーマが値に課す制約（一次情報）

出典: `nablarch-testing-yaml:1.0.0-SNAPSHOT` の jar 内 `nablarch/test/ntf-testdata-yaml-schema.json`
（`.m2` インストール時刻 2026-08-13 17:04 ＝ yaml `190cc9a` 版）。`$defs` の 3 定義を開いた。

| 定義 | `rows` の構造 | 値に課される型 |
|---|---|---|
| `table_data` | オブジェクトの配列 | `items.additionalProperties.type` = `["string","null"]` |
| `list_map_data` | オブジェクトの配列 | `items.additionalProperties.type` = `["string","null"]` |
| `record_fragment` | **配列の配列** | `items.items.type` = `["string","null"]` |

再現コマンド:

```sh
python3 -c "
import json,io,zipfile
z=zipfile.ZipFile('$HOME/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT.jar')
d=json.loads(z.read('nablarch/test/ntf-testdata-yaml-schema.json').decode('utf-8'))
for k in ['table_data','list_map_data','record_fragment']:
    print(k, json.dumps(d['\$defs'][k]['properties']['rows']['items'], ensure_ascii=False))
"
```

`table_data` の `rows` の description も「数値・真偽値も必ず文字列（クォート付き）で記述すること
（例: `AGE: \"30\"`、`FLAG: \"true\"`）」と明記しており、型制約と一致している。

**この制約は `rows` の値についてのものである。** 他のプロパティには当てはまらない
（例: `$defs.field_def.properties.length` は `anyOf: [{type: integer, minimum: 0}, {type: string, pattern: "^([0-9]+|-)$"}]` で、
description も「integer 記法（`10`）も文字列記法（`"10"`）もどちらも有効」と書いている）。

### 2. 検証は converter の読み取り経路で実際に効く（一次情報）

`YamlLoader#load`（yaml sources jar）は `Load#loadFromInputStream` の直後に
`JSON_SCHEMA.validate(jsonNode)` を実行し、違反があれば `YamlSchemaValidationException` を投げる。
converter は `YamlFormatReader#read` → `YamlTestCoreAdapter#loadRawMap` → `YamlLoader#load` と辿るため、
**スキーマ違反の YAML は中間モデルへ到達しない**。したがって「スキーマを通るスカラー」だけが
辺② 軸D の対象になる。

ただし `YamlLoader#load` は、読み込み結果が `null`（＝空ファイル）のときは `Collections.emptyMap()` を返して
`JSON_SCHEMA.validate` に到達しない。**空ファイルはスキーマ検証を迂回する**。

### 3. どの記法がスキーマを通るかの実測

期待値を先に決めず、一時プローブ（記録後に削除）で 33 記法 ＋ 複数行 3 記法を
`new YamlFormatReader().read(...)` に通し、スキーマ違反で落ちるか・中間モデルに何が入るかを出力させた。

**スキーマを通らない記法（＝仕様外の入力。軸D の対象にしない）**

| 記法 | 解決される型 | 結果 |
|---|---|---|
| `123`（引用符なし） | int | `YamlSchemaValidationException` |
| `1.50`（引用符なし） | float | `YamlSchemaValidationException` |
| `true`（引用符なし・小文字） | bool | `YamlSchemaValidationException` |
| `.inf` / `.nan` | float | `YamlSchemaValidationException` |

**スキーマを通る記法と、中間モデルへ入る値**

| YAML 上の記法 | 中間モデルの値 |
|---|---|
| `abc` / `"abc"` / `'abc'` | `"abc"`（3 記法とも同じ。引用の別は残らない） |
| `"123"` | `"123"` |
| `"1.50"` | `"1.50"` |
| `"true"` | `"true"` |
| `TRUE` / `True` / `yes` / `on`（引用符なし） | `"TRUE"` / `"True"` / `"yes"` / `"on"` |
| `null`（引用符なし・小文字） | **Java `null`** |
| 値なし（`- V:`） | **Java `null`** |
| `~` | **`"~"`（文字列）** |
| `Null` / `NULL` | `"Null"` / `"NULL"` |
| `"null"` | `"null"` |
| `2026-08-07` / `"2026-08-07"` / `2026-08-07T12:34:56` | 同じ文字列のまま |
| `007` / `"007"` | `"007"`（先頭ゼロが残る） |
| `0o17` / `0x1F` / `1_000` | 同じ文字列のまま |
| `""` | `""`（空文字） |
| `"  pad  "` | `"  pad  "`（前後空白が残る） |
| `"a: b"` / `"a #b"` | `"a: b"` / `"a #b"` |
| `\|`（リテラル） | `"l1\nl2\n"`（末尾に改行が付く） |
| `>`（フォールド） | `"l1 l2\n"`（改行が空白へ畳まれ、末尾に改行が付く） |
| `"l1\nl2"` | `"l1\nl2"` |

**プローブでしか分からなかった事実**

1. **`null` と `~` が非対称。** 引用符なしの `null` と「値なし」は Java `null` になるが、
   **`~` は文字列 `"~"` になる**。別途渡された 10 ケース表は `null`・`~`・値なし を 1 ケースに
   まとめていたが、実測では 3 者のうち 2 つだけが `null` である。
2. **真偽値・NULL は小文字だけが特別扱いされる。** `true` はスキーマ違反で弾かれるのに
   `TRUE` / `True` / `yes` / `on` は文字列として通る。`null` は Java `null` になるのに
   `Null` / `NULL` は文字列として通る。
3. **数値に見える記法のうち、通るものと通らないものが混在する。** `123` / `1.50` は弾かれるが、
   `007` / `0x1F` / `0o17` / `1_000` は文字列として通る。
4. **引用の別は中間モデルに残らない。** `abc` / `"abc"` / `'abc'` が同一の `"abc"` になる。
5. **`>` は改行を空白へ畳み、`|` と `>` はどちらも末尾に改行を足す。**

### 4. 確定した辺② 軸D のケース（12 ケース）

タグは `D2-01`〜`D2-12`。**D2-01〜D2-10** は上の実測で「スキーマを通る」ことを確認した記法から導いた。
**D2-11・D2-12** は導出とは別の理由で足した —— 辺① 軸D は空文字・前後空白を、辺④ 軸D は `""` と
コロン・ハイフン・`#` 含みを持ち、#25 が「辺④で書いた各ケースを辺②で読み戻す」突き合わせを行うため、
辺②側に対応ケースが無いと片側だけになる（steering Decisions「軸D の対象範囲」でユーザー確定・2026-08-14）。

| タグ | ケース | 対象記法 |
|---|---|---|
| D2-01 | 引用符なし文字列 | `abc` |
| D2-02 | 引用符あり文字列（二重・一重） | `"abc"` / `'abc'` |
| D2-03 | 引用符付き数値 | `"123"` |
| D2-04 | 引用符付き末尾ゼロ小数 | `"1.50"` |
| D2-05 | 真偽値に見える文字列 | `"true"` / `TRUE` / `yes` |
| D2-06 | NULL | `null`（引用符なし）／値なし |
| D2-07 | NULL に見える文字列 | `"null"` / `~` / `NULL` |
| D2-08 | 日付・日時風文字列 | `2026-08-07` / `2026-08-07T12:34:56` |
| D2-09 | 複数行 | `\|` / `>` |
| D2-10 | 先頭ゼロ・非 JSON 数値記法 | `007` / `0x1F` |
| D2-11 | 空文字・前後空白 | `""` / `"  pad  "` |
| D2-12 | 特殊文字を含む文字列 | `"a: b"` / `"a #b"` |

**対象にしない**: `rows` の値としての引用符なし `123` / `1.50` / `true`、および `.inf` / `.nan`。
いずれもスキーマ違反で中間モデルへ到達しないため（＝ NTF が実行できないテストデータ）。
**これらが例外になること自体はテストで固定しない**（steering Decisions）。

### 5. 再検証（2026-08-14）

上の 1〜3 を、記録を根拠にせず自分で実行し直した。

- **スキーマ制約（1 節）**: 再現コマンドをそのまま実行。`table_data` / `list_map_data` は
  `additionalProperties.type` = `["string","null"]`、`record_fragment` は `items.type` = `["string","null"]`。記録どおり。
- **実測（3 節）**: 一時プローブ（実行後に削除）で 37 記法を実ファイル経由
  `new YamlFormatReader().read(...)` に通した。3 節の表と全件一致。特に:
  - `null`（引用符なし）・値なし（`- V:`） → **Java `null`**
  - `~` → **文字列 `"~"`**、`"~"` → 文字列 `"~"`
  - `Null` / `NULL` → 文字列、`"null"` → 文字列 `"null"`
  - `TRUE` / `True` / `yes` / `on` → 文字列。引用符なし `true` は `YamlSchemaValidationException`
    （`boolean が見つかりました、[string, null] が予期されました`）
  - `123` / `1.50` / `.inf` / `.nan` → いずれもスキーマ違反で例外（`integer` / `number` と判定される）
  - `007` / `"007"` / `0x1F` / `0o17` / `1_000` → いずれも記法どおりの文字列
  - `|` → `"l1\nl2\n"`、`>` → `"l1 l2\n"`
- スキーマ `table_data.rows` の description には「`null`（クォートなし）および `"null"`（クォートあり）は
  ともに NullInterpreter により Java null に変換される」とあるが、これは NTF 実行時の解釈であり、
  converter は `InterpreterResolver.raw()` で配線しているため `"null"` は文字列のまま入る（上の実測どおり）。

---

## 実装（2026-08-14）

### 追加したテストクラス

| クラス | 件数 | 導出コマンド |
|---|---|---|
| `YamlFormatReaderScalarTest` | 27 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderScalarTest.java` |
| `YamlFormatReaderInvalidInputTest` | 25 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java` |
| `YamlFormatReaderRealFileTest` | 18 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderRealFileTest.java` |
| （フィクスチャ）`YamlFixture` | — | テストではない。YAML テキストを実ファイルへ書き出し `new YamlFormatReader().read(...)` で読むヘルパ |

合計 **70 件**を追加した（`Tests run` は基準線 419 → **489**）。3 クラスとも `new YamlFormatReader().read(...)` を
本番配線で呼び、スカラー解決とスキーマ検証の区間を実行する。

**70 件の内訳**: 軸D 12 ケースと軸F 5 ケースの担保、#18 の棚卸しで空欄だった軸A・B・C・E の補充、
および**スキーマの自由度の掃引**で見つけた現状挙動の固定（`issues.md` YML-04〜YML-10）である。
掃引ぶんのテストは**軸A〜F のどの要素にも新しい担保を与えない**ため `inventory.md` §2.3 の件数を動かさない
（内訳は §2.1-2・§2.3 末尾に記載）。

### 期待値を先に決めていないことの手順記録

軸D 12 ケース・軸F 5 ケース・軸C/E の候補入力、および掃引で列挙した 27 項目は、
すべて一時プローブ（記録後に削除）で実ファイル経由の `read` に通し、**出力を印字させてから**アサートを書いた。
プローブで観測した結果は下表のとおりで、上の §1〜§5 の記録と全件一致した。

| 入力 | 観測結果 |
|---|---|
| `abc` / `"abc"` / `'abc'` | いずれも `"abc"` |
| `"123"` / `"1.50"` / `"true"` / `TRUE` / `yes` | 記法どおりの文字列 |
| `null`（引用符なし） / 値なし（`- V:`） | **Java `null`** |
| `"null"` / `~` / `NULL` | **いずれも文字列** |
| `2026-08-07` / `2026-08-07T12:34:56` / `007` / `0x1F` | 記法どおりの文字列 |
| `\|` / `>` | `"l1\nl2\n"` / `"l1 l2\n"` |
| `""` / `"  pad  "` / `"a: b"` / `"a #b"` | 記法どおり |
| `type: "text"`（列挙違反） | `YamlSchemaValidationException`（`enum` / `$.setup_files[0].type`） |
| `length: "1a"`（パターン違反） | `YamlSchemaValidationException` **2 件**（`type` と `pattern`。`anyOf` の両枝を外すため）。位置はいずれも `$.setup_files[0].records[0].fields[0].length` |
| インデント不正 ＋ 閉じないフローシーケンス | `IllegalStateException: Failed to parse YAML file: ...`（原因 `ParserException`） |
| 未知のトップレベルキー | `YamlSchemaValidationException`（`additionalProperties` / `$`） |
| `rows` 欠落 | `YamlSchemaValidationException`（`required` / `$.setup_tables[0]` / `rows`） |
| 空ファイル | 例外なし。セクション 1 件・ブロック 0 件（スキーマ検証を迂回する経路） |
| `setup_tables: []` | 例外なし。ブロック 0 件（スキーマ検証を**通る**経路） |
| `rows: []`（テーブル／LIST_MAP） | ブロック生成・`columnNames` 空・`rows` 空 |
| `records: []`（固定長ファイル） | ブロック生成・`records` 空・`directives` は `{file-type=Fixed}` |
| `directives` 省略（ファイル／メッセージ／送信系） | `{file-type=Fixed}` が注入される（空にならない） |
| `fields: []` | `YamlSchemaValidationException`（`minItems`） |
| `fields` の `type` 欠落 | `YamlSchemaValidationException`（`required` / `type`） |
| `length: 10`（引用符なし整数） | スキーマを通り、`FieldDef.length` には**文字列 `"10"`**（`YamlSection#toStr` が `toString()` する） |
| `record_type: "default"`（小文字） | スキーマを通り、中間モデルでは **`null`**（`"Default"` と同じ。`"DEFAULT"` は `"DEFAULT"` のまま） |

掃引で見つけた「黙って壊れる」挙動（YML-04〜YML-06・YML-08(a)・YML-09）と loud に失敗する挙動は
`issues.md` に記録した。掃引で列挙した 27 項目の全件と「見ていない範囲」6 点は `inventory.md` §2.1-2 の
「開示」に載せてある。

### カバレッジ（JaCoCo 実測）

```sh
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes \
  && JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec \
  && awk -F, 'NR > 1 && $3 == "YamlFormatReader" { print "line " $9 "/" ($8 + $9) " branch " $7 "/" ($6 + $7) }' \
       target/site/jacoco/jacoco.csv
```

出力は `line 201/201 branch 108/108`（**行・分岐とも 100%**）。

この数値は `YamlFormatReader` **1 クラスぶん**であり、辺②の担当クラス全体でも #26 の対象 6 区分でもない。
`YamlFormatReader` 以外のカバレッジは #26 で扱う。

---

## レビュー指摘の処置

QA／Craft／Verification の 3 レビュアから受けた指摘を、種類ごとにまとめる。
**各項目はまず一次情報（スキーマ本体・実コード・実行結果）に当たって事実を確かめ、
挙動を記録する項目は先に一時プローブで観測してからアサートを書いた**（プローブはいずれも記録後に削除）。

### A. 台帳・課題一覧の記述が実物と食い違っていたもの

| 確かめた事実（一次情報） | 処置 |
|---|---|
| `$defs.group_message_data.required` も `$defs.expected_request_message_data.required` も `["id","records"]` だけで `group_id` を要求していない | `inventory.md` §2.1 の軸F 欄を「送信系必須の `group_id`」から実定義に基づく記述へ置換し 🔺 F2-04 を外した。`YamlFormatReaderTest#readSendSync_entryWithoutGroupId_isDropped` の Given コメントも置き換えた（**アサーションと入力は無変更**） |
| 「経路 B として動作する」は `$defs.group_message_data.description`（**定義レベル**）にあり `properties.group_id.description` には無い。旧再現コマンドは `properties.group_id.description` しか印字せず、引用文が 1 つも出なかった | `issues.md` YML-02 に**引用元 JSON パスと逐語引用の対応表**を置き、再現コマンドを `required` ＋ 定義レベル `description` ＋ `properties.group_id.description` の 3 つを印字する形へ差し替えた（実行して 3 引用がすべて出ることを確認）。テスト Javadoc と Given コメントの同じ取り違えも直した |
| `$defs.field_def.properties.length` は `anyOf: [{type: integer,…},{type: string, pattern:…}]` で、integer 記法も文字列記法も有効と description が明記している | 「対象としない入力（辺②）」の適用範囲を **`rows` の値**（`table_data` / `list_map_data` / `record_fragment`）に限定し、「`rows` 以外のプロパティには当てはまらない」節を再現コマンド付きで追加。3 クラスの Javadoc も同様に限定した |
| §2.3 の軸F 行の件数内訳が合わない（F2-04 の 3 件のうち 2 件を C-17／C-20 側へ二重に振り分けていた）。実物は F2-01 2 件／F2-02 1 件／F2-03 1 件／F2-04 3 件／F2-05 1 件 ＝ **8** | 「本行の 4 ケース 7 件 ＋ F2-03 の 1 件」と書き分け、F2-04 の 3 件のうち 2 件が C-17／C-20 の根拠を**兼ねる**（別勘定ではない）ことを明記した |
| 内訳の「8 件」「差の 15 件」が数字のまま置かれていた（総数 23 にはコマンドが併記されていた） | 節見出しコメントを境界にした `awk` ＋ `grep -c` の導出コマンドを併記した |
| クラス別件数表が `InvalidInputTest` の軸F 8 件を「意図的にスキーマ違反・不正 YAML にした入力」と一括りにしていたが、F2-05（空ファイル）はそのどちらでもない | 7 件と 1 件に書き分けた |
| C-15 は §2.3 の「#18 の状態」列に無く、§2.1 で ✅ とされていた（「要追加」からの移動ではない） | 「到達不能と判定した軸要素（#24）」表の前置きと C-15 行に但し書きを足し、出典を開いた読み手が該当記述を見つけられるようにした |
| YML-01 本文の `Null`、「課題としない」表の `True` / `on` には対応するテストが無い（いずれも `JsonScalarResolver` の正規表現から導ける） | YML-01 を「テストで担保した変種」（5 件）と「正規表現から導出した事実」（`Null`）の 2 表に分割し、「課題としない」表の行も `TRUE` / `yes`（テストあり）と `True` / `on`（テスト無し・導出）に分けた |
| 軸D の 12 ケースの多くは `setup_tables` 1 経路でしか測っていない。行値の取り出しは 3 系統あり、レコード断片はスキーマのパスも別（`$defs.record_fragment.properties.rows.items.items.type`） | §2.1-2 の「開示」に 1 項を足し、あわせて 2 ケース × 2 経路のテストを追加した（下の C 参照） |
| `normalizeRecordType` の `"default"`（小文字）分岐を「軸要素に属さないので #26 送り」と開示していたが**誤り**。`record_type: "default"` はスキーマを通り（`enum` が無い）この分岐に到達する | 開示を実態へ書き直し、`YamlFormatReaderRealFileTest#normalizesLowercaseDefaultRecordTypeToNull` を足して閉じた |
| 上の分岐を閉じたあとも「分岐 107/108・唯一の未到達は `"default"` 側」という JaCoCo の数値が台帳に残っており、再実行しても再現しない | 取り直して実測値（`line 201/201 branch 108/108`）へ差し替え、導出コマンドを**オフラインで実行できる形**に直した |
| `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenTopLevelKeyIsUnknown` の Javadoc と台帳が、「未知キーは無視」は「既知キーのうち分岐を持たないものに効く」と書いていた。その集合は**空**である —— スキーマのトップレベル `properties` 11 キーと `addBlocksForSection` の分岐 11 本が完全一致し、`read` は `yaml.keySet()` を走査する | 「**実ファイル経路では到達不能**」へ言い換え、11 と 11 が一致する事実を根拠として添えた（Javadoc と `issues.md`「課題としないと判断した観測結果（#24）」の該当行の両方） |
| `#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInFile` の Javadoc が「器が注入する `file-type` **だけ**を持つ」と一般化していた。実測では可変長は `{file-type=Variable, field-separator=,}` の 2 件になる | Javadoc に固定長 1 件／可変長 2 件の違いを書き（C-11「空にならない」の結論は両種別で成り立つことも明記）、掃引表の項目 9 にも可変長の観測を足した |
| `readsEmptyStringAsIsInRecordFragmentPath` は「書かれた空文字が保たれる」ことを固定できない。**この経路では欠損も `""` で埋まる**ため、書いた `""` を捨てる実装でも同じ結果になる（実測: `rows: - [""]` と `rows: - []` がどちらも同じ） | テストは残し（`""` が Java `null` にならないことは固定できる）、**固定できる範囲を Javadoc に明記**した。クラス内の「3 経路とも同じであった」にも同じ但し書きを付け、`inventory.md` の「別経路での確認」表の D2-11 行にも書いた |
| `YamlFormatReaderScalarTest#readValue` の Javadoc が「継続行のインデントは半角空白 **10** 個以上」としていた。実測（インデント 6〜12 の総当たり）では **9・10・11・12 が成功**、8 は `ScannerException`、7 は `ParserException`、6 は `ScannerException` | 「**9 個以上**」へ書き改め、8／7／6 の実測結果とその理由（キー `V` が `"      - V:"` の 9 桁目から始まる）も書いた。テストが 10 個で書いてある事実はそのまま残した |

### B. 記録すべき挙動が `issues.md` に無かったもの（いずれも `src/main` 無変更）

| 観測した現状挙動 | 処置 |
|---|---|
| 送信系で `group_id` の無いエントリは**例外も警告も出ず黙って 0 件になる**（`group_id` 無し 1 件＋有り 1 件なら有りだけが残る） | **YML-02**（影響度 中・検出できない）。固定テスト `#dropsSendSyncEntryWithoutGroupIdFromRealYaml` を追加。原因（`rawGroupsInOrder` が非 null の `group_id` だけを列挙する）とスキーマの実定義を再現コマンド付きで記録 |
| `record_type: "FW_HEADER"` のレコードだけを書くと、例外にならずブロックは生成されるが `records` 0 件・`fwHeaderFields` 0 件になる。スキーマ description は 2 か所で「`FW_HEADER` のような予約値はない」「旧形式の `record_type: FW_HEADER` は廃止」と明言している | **YML-03**（影響度 中・検出できない／帰属は yaml 側）。固定テスト `#dropsFwHeaderNamedRecordFromRealYaml` を追加。NTF 実行時も同じ器が同じレコードを落とすため**変換自体は NTF の解釈に忠実**である旨も書いた |
| 高／中／低はいずれも「変換結果が入力と一致するか」で定義されており、**変換は忠実なのに仕様として不適切**という課題（YML-01）を表現できない | 凡例に「**影響度 別枠**」を 1 段足して定義し、YML-01 の見出しと本文をそれに合わせた。並び順の原則は変えていない |
| テーブル／LIST_MAP のカラムは**先頭行のキー集合だけ**で決まり、後続行にしかないカラムが黙って消える。先頭行が `{}` だとテーブル経路で後続行ごと `rows=[]` になる | **YML-04**（影響度 高・検出できない／帰属は yaml 側と converter の両方）。固定テスト 5 件を追加（同節にはマーカー列だけの行を確かめる `readsMarkerOnlyTableAsColumnlessRows` を加えた 6 件が入る。XLS-08 と同型であることの確認であり YML-04 の表には載せていない） |
| レコード断片で行の要素数が `fields` の件数と食い違っても例外にならず、余りは捨てられ不足は**空文字**で埋まる（明示的に書いた `null` は `null` のまま残る） | **YML-05**（影響度 中・検出できない／帰属は nablarch-testing 側）。固定テスト 2 件を追加。**「空文字と `null` は区別される」への但し書き**を `issues.md` の該当行とテスト Javadoc の 2 か所に付けた |
| `id` が重複したエントリは 2 件目以降も 1 件目のデータでブロックが作られる | **YML-06**（影響度 中・検出できない／帰属は converter 側）。固定テスト 2 件を追加 |
| ディレクティブ値が `trim()` されるため、スキーマ description が推奨するリテラル記法が空文字になる（(a)）／タブ記法が例外になる（(b)）。あわせてシンボル記法が中間モデルで実文字になる辺①との非対称 | **YML-08**（影響度 中・(a) は検出できない／(b) は loud）。固定テスト 3 件を追加。往復が安定するかは**未確認**として明記（#25 で確認） |
| 長さ省略記法 `"-"` は `text-encoding` を書かないと手掛かりの無い `NullPointerException` になる | **YML-07**（影響度 低・loud に失敗するため検出できる）。固定テスト 2 件を追加 |
| 同じ `group_id` のエントリが離れて書かれていると、ブロックがグループの**初出順にまとめ直され**原文の記述順と食い違う（テーブル系・ファイル系・送信系の 3 経路とも）。値そのものは失われない | **YML-09**（影響度 中・検出できない／帰属は converter 側）。固定テスト `#reordersBlocksByFirstAppearanceOfGroupIdFromRealYaml` を追加。判断の根拠は「本リポジトリは並びの保持を変換の正しさとして扱ってきた（steering #15）」こと |
| テーブル系の `rows` に大小だけが違うキー（`id` と `ID`）を書くと、器が両方を大文字化するため 1 つへ潰れ、**片方の値が黙って消えて列名が重複する**（`columnNames=[ID, ID]` / `rows=[[2, 2], [4, 4]]`）。LIST_MAP は原文の大小のままで衝突しない | **YML-10**（影響度 高・検出できない／帰属は nablarch-testing 側 `TableData` の `toUpperCase()`）。固定テスト 2 件（衝突とその対比）を追加。掃引に**項目 27「器が行う正規化」**を立てて枠の外を塞ぎ、大文字化そのものは「課題としないと判断した観測結果（#24）」へ記録した |

### C. テストの補強

| 指摘 | 処置 |
|---|---|
| 台帳が「キーワードと位置をアサートする」と書いているのに `assertTrue(… contains …)` でしかなかった | `types(e)` ＝ `["type","pattern"]`・`locations(e)` ＝ 同一位置 2 件の厳密アサートへ置き換え、台帳にも件数 2 とその理由（`anyOf` の両枝を外す）を書いた |
| 台帳が E-2(1 件) の担保として引いている `readsFourBlockImplementationsFromOneRealYaml` が**行数をアサートしていなかった** | テーブルと LIST_MAP について `getRows().size()` が 1 であることをアサートする 2 行を追加（入力は変更なし） |
| 空ファイルは `JSON_SCHEMA.validate` に到達しない。E-1(0 件)・C-04(空) をその経路だけで担保するのは穴 | `#namesContainerAndSectionByResourceNameWithoutBlocks` の入力を `# no sections` から **`setup_tables: []`** へ変え、スキーマ検証済みの経路に載せた。空ファイルそのものの担保は `#readsEmptyFileAsContainerWithoutBlocks` に残し、両テストの Javadoc に「通る経路が違う」ことを相互参照で書いた |
| 軸D を `setup_tables` 1 経路でしか測っていない | `null` と `""` について LIST_MAP 経路・レコード断片経路のテストを 4 件追加（経路差は無かった）。**軸D の 12 ケース定義には足していない**（§2.1-2 に「別経路での確認」表として別立て） |
| `length` の integer 記法（仕様内）が未担保 | `#readsIntegerLengthNotationAsString` を追加。軸C の `FieldDef.length`（C-21）の担保として計上 |
| C-13(空・到達不能) の根拠が 1 つの生成経路にしか無い | `#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInSendSync` を追加し、2 つの生成経路の両方に対応させた |
| `MessageDataBlock.fwHeaderFields`（C-14 値あり）が in-memory 経路でしか担保されていない | `#readsFwHeaderFieldsFromRealYaml` を追加（`fw_header:` のキーが記述順で入ることを確認） |

### D. コーディング作法

| 指摘 | 処置 |
|---|---|
| try/catch/`fail()` で例外を捕まえている | `org.junit.Assert.assertThrows` へ置き換えた（到達しない `return null;` は消え、`fail` の static import も外れた） |
| `YamlFixture.write(File, String)` の呼び出しが 1 か所しかない | `read` へインライン化した |
| `YamlFixture` の API が `File` で、内部（`Files`）と兄弟の `XlsFixture`（`open(Path)` ほか）と揃っていない | API を `Path` に変え、呼び出し側 3 クラスに `XlsFormatReaderRealFileTest` と同じ `private Path dir()` ヘルパを置いて `folder.getRoot()` の直接参照を無くした |
| `read` が書き出し先ディレクトリの存在を仮定し、失敗時のメッセージが素っ気ない | `Files.createDirectories(dir)` を足し、`UncheckedIOException` のメッセージを `"failed to write fixture: " + file` にした（兄弟 `XlsFixture#writeTo` と同じ形） |
| `YamlFixture.read` の事前キャッシュクリアと利用側の `@After clearLoaderCache` が二重になっている | **`@After` を正とし、フィクスチャ側の事前クリアを外した**。理由: 3 クラスの `@After` は既存の `RoundTripTest` ほかと同じ形であり、そちらを消すと既存クラスと揃わなくなる。`TemporaryFolder` がテストごとに別ディレクトリを作るため 1 メソッド 1 read の現状では衝突しない。`read` の Javadoc も実態どおりに書き改めた |
| 3 クラスに「ブロックが 1 件であること」＋素キャストが散らばっている（失敗時に `ClassCastException` しか出ない） | `YamlFixture.onlyBlock(container, Class)` に集約した（兄弟 `XlsFormatReaderRealFileTest#onlyBlock` と同じ形）。同じ失敗がクラスごとに別のメッセージにならなくなった |
| ヘルパがブロック数を確かめずに先頭を取っている | `readValue` ／ `readListMapValue` ／ `readRecordFragmentValue` の 3 ヘルパにブロック数アサートを足した |
| `readValue(String...)` の「1 要素目が空文字＝値なし」が暗黙の合図になっている | 値なし専用の `readOmittedValue()` に分けた（両者は共通の `readValueLine` を呼ぶ） |
| `readValue(String...)` の可変長引数が、ブロックスカラーの 2 テストにだけ「2 要素目以降はインデント込みで書く」「そのインデントは 9 個以上」という規則を課している | `readValue(String)` を 1 引数に戻し、ブロックスカラー専用の `readBlockScalarValue(String header, String... contentLines)` を分けた。**インデントはヘルパが付ける**ので、呼び出し側は `readBlockScalarValue("\|", "l1", "l2")` と中身だけを書く |
| インデント量が値に現れない理由を「ブロックスカラーは最も浅い継続行のインデントを基準に切り落とすため」と書いていたが、YAML 1.2 §8.1.1.1 では **最初の非空行**のインデントで content indentation が決まる | 「最初の非空行のインデントで決まり、以降の行はそれ以上のインデントを要する」へ直した（規格の節番号を添えた） |
| `getStackTrace()[0]` / `[1]` を長さ検査なしで添字参照し、最内段（JDK 内部の `String#getBytes`）に固定していた。JDK が `Objects#requireNonNull` へ変えれば段がずれる。本課題の主張は最内段ではなく**呼び出し元**にある | ヘルパ `hasFrame(thrown, class, method)` を足してトレース全体を走査する形にし、器の `DataFileFragment#replaceFieldSize` の段が居ることだけをアサートするようにした。段の位置も行番号も見ない |
| ヘルパを末尾に置いているのは辺②の 3 クラス中このクラスだけで、兄弟の `XlsFormatReaderInvalidInputTest` ／ `XlsFormatReaderCellTypeTest` も先頭に置いている | `YamlFormatReaderInvalidInputTest` のヘルパ節を `@After` の直後へ移した |
| Javadoc・節見出しに作業経緯が残っている（「#24 修正ラウンド 2」「2026-08-14 のレビュー指摘で訂正した」） | 2 か所とも削除した。いずれも実体の説明は前後の文が持っており、経緯は `git log` にある |
| `YamlFixture#onlyBlock` の Javadoc が兄弟の `XlsFormatReaderRealFileTest#onlyBlock` と「同じ形」と書いていたが、あちらは private インスタンスメソッドで引数も違う | 「役割は同じだが 3 クラスで共有するため static でフィクスチャ側に置いた」へ直し、`XlsFixture` が中間モデル側のヘルパを引き受けないのに対し本クラスは引き受ける、という線引きの違いも書いた |
| `list(String...)` の自前ヘルパ／`assertTrue(!(…))`／raw type の `new ArrayList<String>()`／static import の並び | `Arrays.asList` に統一（`list(` の残存 0）、`assertFalse` へ置換、ダイヤモンド演算子へ統一、static import を完全修飾名の昇順へ揃えた（既存クラスと同じ並び） |
| 各テストがどの軸要素を担保するか Javadoc から読み取れない | 3 クラスの各テスト Javadoc に `<p>担保する軸要素: …</p>` を 1 行足し、クラス Javadoc にも `XlsFormatReaderRealFileTest` と同じ一文を入れた |
| ブロックスカラーが要求するインデント量の根拠が書かれていない | 継続行のインデント要件（半角空白 9 個以上）とその理由（キー `V` が 9 桁目から始まる）を `readBlockScalarValue` の Javadoc に書いた |

---

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 軸D の12ケース（D2-01〜D2-12）すべてがアサートされ、特に `null` ／値なし ／ `~` の3者の分かれ方と、`"null"` ／ `NULL` の扱いが結果として固定されている | OK | `YamlFormatReaderScalarTest` **27 件**（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderScalarTest.java` → **27**）が 12 ケースを担保する。要素→メソッドの対応は `inventory.md` §2.1-2 の軸D 表。3 者の分かれ方は `readsUnquotedNullAsJavaNull`（Java `null`）／`readsOmittedValueAsJavaNull`（Java `null`）／`readsTildeAsString`（文字列 `"~"`）が、`"null"` ／ `NULL` は `readsQuotedNullAsString`／`readsUppercaseNullAsString` が固定している。27 件のうち 4 件は同一ケースを LIST_MAP 経路・レコード断片経路で確認したもので、**12 ケース定義には足していない**（§2.1-2「別経路での確認」表） | | |
| 軸F の5ケース（スキーマ違反／不正 YAML／未知キー／必須構造欠落／空ファイル）で例外型または結果がアサートされている。スキーマ違反のケースの入力に、仕様外とした引用符なしスカラー記法（`true` / `123` / `1.50` / `.inf` / `.nan`）を使っていない | OK | 軸F を担保するのは `YamlFormatReaderInvalidInputTest` の **8 件**（同クラスの総数は `grep -c '^    @Test' …InvalidInputTest.java` → **25**。差の 17 件は掃引で見つけた現状挙動の固定＝ YML-04〜YML-08・YML-10 であり**軸F の要素ではない**。8 と 17 の導出コマンドは `inventory.md` §2.3）。例外型は `YamlSchemaValidationException`（F2-01/03/04）・`IllegalStateException` ＋ 原因 `YamlEngineException`（F2-02）、F2-05 は例外にならずブロック 0 件になる結果をアサート。スキーマ違反の入力は `type: "text"`（列挙違反）と `length: "1a"`（パターン違反）で、いずれも引用符付き文字列である。**仕様外記法を使っていないことの確認**は下の「確認コマンド」を参照 | | |
| `issues.md` に YML-01 と「対象としない入力」の YAML 側段落が記録されている（いずれも `src/main` 無変更） | OK | `issues.md` に `## #24 …` 節と `## #24 スキーマの自由度の掃引で記録した課題` 節があり、`### YML-01 …`（影響度 別枠。解決経路 4 段を再現コマンド付きで示し、担保を「テストで担保した変種」5 件と「正規表現から導出した事実」1 件に分けている）と `### 対象としない入力（辺②）`（適用範囲を `rows` の値に限定し、`field_def.length` の integer 記法が仕様内であることを再現コマンド付きで併記）を置いた。ID は `XLS-nn` ではなく `YML-nn` 系列。あわせて **YML-02〜YML-10** も記録した。`src/main` 無変更は `git diff 3165770 -- src/main \| wc -l` → **0** | | |
| 辺②について軸A の13種（`DEFAULT` を除く。到達不能。根拠付きで空欄）・軸B の4種・軸C の全フィールド（省略可能なものは省略時も。`sections` は「空」「複数」とも到達不能として根拠付きで空欄）・軸E が埋まっている | OK | 軸A: `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` が 13 種を実 `.yaml` で ✅（A-01 は根拠付きで空欄）。軸B: `#readsFourBlockImplementationsFromOneRealYaml` が 4 種。軸C: #18 で欠けていた 8 要素のうち C-08/C-09/C-12/C-18/C-13(値あり) を担保済みへ、C-11(空)/C-13(空)/C-17/C-20 を根拠テスト付きの到達不能へ移した。C-02 は根拠付きで空欄。C-14(値あり)・C-21 は実ファイル経路でも担保した。軸E: E-1/E-2/E-3 の 0・1・複数と E-4(1) を実 `.yaml` で担保、E-4(複数) は根拠付きで空欄。§2.3 の集計は **要追加 0 ／ 担保済み 22 ／ 到達不能 6 ／ 総計 28** | | |
| src/main への変更がゼロ | OK | `git diff 3165770 -- src/main \| wc -l` → **0**（`3165770` は #24 のタスク定義コミット＝実装着手前） | | |
| `mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean test -Djacoco.skip=true` → `Tests run: 489, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS`（基準線 419 ＋ 追加 70） | | |

### 確認コマンド（軸F の入力に仕様外のスカラー記法を使っていないこと）

対象ファイル: `src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java`

```sh
f=src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java

# A. 仕様外記法の文字列がソース中に現れるか
grep -nE 'true|false|123|1\.50|\.inf|\.nan' $f

# B. 埋め込み YAML の行のうち、値が二重引用符で始まらないもの
grep -oE '\+ "[^"]*\\n"' $f | grep -vE ': \\"'
```

- **A の結果**: ヒットは 7 行で、内訳は**クラス Javadoc の散文 4 行**（「F2-01 の入力に…仕様外とした
  引用符なしスカラー記法…は使わない」という但し書き本文 2 行と、`additionalProperties: false` の説明 2 行）と、
  **ヘルパ `hasFrame` の `boolean` リテラル・`@return` タグ 3 行**である。
  **同ファイルのどの YAML フィクスチャにも 1 件も現れない**（軸F の 8 件だけでなく、掃引ぶんの 17 件にも無い）。
- **B の結果**: 出力は 13 行（`sort -u` 後）で、`setup_tables:` / `records:` / `- fields:` / `rows:` のような
  **マッピングキーだけの行**、`fields: []` / `rows: []` の空配列、`- {}`（YML-04 の空マッピング行）である。
  **スカラー値を持つ行はすべて `: \"…\"` の形（＝二重引用符付き）**であり、B の出力に現れない。
- A だけでは「その語がソースに無い」ことしか言えず（YAML フィクスチャは Java の文字列リテラルなので
  行末が `\n"` になり、素朴な行末一致の grep は常に 0 を返してしまう）、B と組み合わせて初めて
  「**埋め込み YAML のスカラー値が引用符付きである**」ことの根拠になる。

### 台帳の記述規約の自己点検（Steps の一項目）

| 点検項目 | 結果 |
|---|---|
| `grep -cE '\b(L[0-9]{1,4})\b' .rn/ntf-test-data-converter/coverage/inventory.md` が 0 | **0**（OK。実行して確認） |
| ファイル行数を書いていない | OK |
| 書き足した件数のすべてに導出コマンドを併記／併記したコマンドはそのまま実行して同じ結果が出る | OK。`inventory.md` に現れる 11 個のテストクラスパスすべてについて `grep -c '^    @Test'` を実行し、書かれた値と一致することを確認した（XLS: 10 / 33 / 18 / 16 / 18 / 40、YAML: 25 / 18 / 27 / 20 / 33）。軸F の内訳 8／17 には `awk` ＋ `grep -c` の導出コマンドを併記した |
| 併記した再現コマンドを実際に実行して、引用した文言が出力に現れる | OK。追加・差し替えた再現コマンド（YML-02 の 1 本、YML-04 の 2 本、YML-05 の 2 本、YML-06 の 2 本、YML-07 の 1 本、YML-08 の 2 本、YML-09 の 2 本、YML-10 の 2 本）を**すべてそのまま実行**し、引用文・引用したコード行が出力に現れることを確認した |
| 担保の穴は、テストを足さない場合でも台帳に開示する | OK。§2.1-2 の「開示」に、掃引で列挙した 27 項目の全件と「見ていない範囲」6 点、および軸D を 1 経路でしか測っていない件を書いた |
| 逆引き表（軸要素 → 担保テストメソッド）を新設していない | **新設していない**。§2.1-2 の各表は #19〜#23 が §1.2-2／§3.1-2／§3.1-3 で用いた「その要素を #24 が埋めたことを示す差分表」と同じ形式である。掃引表は「スキーマ上の自由度 → 観測結果」であり逆引きではない。逆引きの正は #27 の `coverage/axis-matrix.md`（steering Rules） |

## Overall Verdict

- Self-check: OK
