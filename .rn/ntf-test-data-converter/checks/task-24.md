# task-24 Completion Check

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

### 2. 検証は converter の読み取り経路で実際に効く（一次情報）

`YamlLoader#load`（yaml sources jar）は `Load#loadFromInputStream` の直後に
`JSON_SCHEMA.validate(jsonNode)` を実行し、違反があれば `YamlSchemaValidationException` を投げる。
converter は `YamlFormatReader#read` → `YamlTestCoreAdapter#loadRawMap` → `YamlLoader#load` と辿るため、
**スキーマ違反の YAML は中間モデルへ到達しない**。したがって「スキーマを通るスカラー」だけが
辺② 軸D の対象になる。

### 3. どの記法がスキーマを通るかの実測

期待値を先に決めず、一時プローブ（`src/test/.../yaml/ProbeTest.java`。記録後に削除）で
33 記法 ＋ 複数行 3 記法を `new YamlFormatReader().read(...)` に通し、
スキーマ違反で落ちるか・中間モデルに何が入るかを出力させた。

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

### 4. 導出した辺② 軸D のケース（10 ケース）

タグは `D2-01`〜`D2-10`。上の実測で「スキーマを通る」ことを確認した記法だけで構成する。

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

**対象にしない**: 引用符なしの `123` / `1.50` / `true`、および `.inf` / `.nan`。
いずれもスキーマ違反で中間モデルへ到達しないため（＝ NTF が実行できないテストデータ）。
これらが例外になること自体は軸F（F2-01 スキーマ違反）で担保する。

### 5. 再検証（2026-08-14・セッション再開時）

上の 1〜3 を、記録を根拠にせず自分で実行し直した。

- **スキーマ制約（1 節）**: 再現コマンドをそのまま実行。`table_data` / `list_map_data` は
  `additionalProperties.type` = `["string","null"]`、`record_fragment` は `items.type` = `["string","null"]`。記録どおり。
- **実測（3 節）**: 一時プローブ（`src/test/.../yaml/ScalarProbeTest.java`。実行後に削除）で 37 記法を
  実ファイル経由 `new YamlFormatReader().read(...)` に通した。3 節の表と全件一致。特に:
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
| `YamlFormatReaderScalarTest` | 23 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderScalarTest.java` |
| `YamlFormatReaderInvalidInputTest` | 8 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java` |
| `YamlFormatReaderRealFileTest` | 11 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderRealFileTest.java` |
| （フィクスチャ）`YamlFixture` | — | テストではない。YAML テキストを実ファイルへ書き出し `new YamlFormatReader().read(...)` で読むヘルパ |

合計 42 件を追加した（`Tests run` は 419 → **461**）。

### 期待値を先に決めていないことの手順記録

一時プローブ（`src/test/.../yaml/ScalarProbeTest.java`。記録後に削除）で、軸D 12 ケース・軸F 5 ケース・
軸C/E の候補入力をすべて実ファイル経由で `read` に通し、**出力を印字させてから**アサートを書いた。
プローブで観測した結果は下表のとおりで、本 §1〜§5 の記録（2026-08-13／2026-08-14 の再検証）と全件一致した。

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
| インデント不正 ＋ 閉じないフローシーケンス | `IllegalStateException: Failed to parse YAML file: ...`（原因 `ParserException`） |
| 未知のトップレベルキー | `YamlSchemaValidationException`（`additionalProperties` / `$`） |
| `rows` 欠落 | `YamlSchemaValidationException`（`required` / `$.setup_tables[0]` / `rows`） |
| 空ファイル | 例外なし。セクション 1 件・ブロック 0 件 |
| `rows: []`（テーブル／LIST_MAP） | ブロック生成・`columnNames` 空・`rows` 空 |
| `records: []`（固定長ファイル） | ブロック生成・`records` 空・`directives` は `{file-type=Fixed}` |
| `directives` 省略（ファイル／メッセージ） | `{file-type=Fixed}` が注入される（空にならない） |
| `fields: []` | `YamlSchemaValidationException`（`minItems`） |
| `fields` の `type` 欠落 | `YamlSchemaValidationException`（`required` / `type`） |

### カバレッジ（JaCoCo 実測）

```sh
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean jacoco:instrument test jacoco:restore-instrumented-classes
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec
```

`target/site/jacoco/jacoco.csv` の `YamlFormatReader` 行:

| 指標 | 値 |
|---|---|
| 行 | 201 / 201（**100%**。`LINE_MISSED` = 0） |
| 分岐 | 107 / 108（**99.07%**。`BRANCH_MISSED` = 1） |
| メソッド | 23 / 23 |

未到達の分岐は 1 つだけで、`normalizeRecordType` の
`if ("Default".equals(recordType) || "default".equals(recordType))` の**後段（小文字 `"default"`）**である
（`jacoco.xml` の `YamlFormatReader.java` で `mb > 0` の行を抽出して特定）。
軸A〜F のどの要素にも属さないため #24 ではテストを足さず、`inventory.md` §2.1-2 の「開示」に記した。
**#26（カバレッジ計測と未到達分岐の列挙）で扱うこと。**

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 軸D の12ケース（D2-01〜D2-12）すべてがアサートされ、特に `null` ／値なし ／ `~` の3者の分かれ方と、`"null"` ／ `NULL` の扱いが結果として固定されている | OK | `YamlFormatReaderScalarTest` 23 件が 12 ケースを担保（要素→メソッドの対応は `inventory.md` §2.1-2 の軸D 表）。3 者の分かれ方は `readsUnquotedNullAsJavaNull`（Java `null`）／`readsOmittedValueAsJavaNull`（Java `null`）／`readsTildeAsString`（文字列 `"~"`）が、`"null"` ／ `NULL` は `readsQuotedNullAsString`（`"null"`）／`readsUppercaseNullAsString`（`"NULL"`）が固定している。いずれも実 `.yaml` を `new YamlFormatReader().read(...)` に通す経路 | | |
| 軸F の5ケース（スキーマ違反／不正 YAML／未知キー／必須構造欠落／空ファイル）で例外型または結果がアサートされている。スキーマ違反のケースの入力に、仕様外とした引用符なしスカラー記法（`true` / `123` / `1.50` / `.inf` / `.nan`）を使っていない | OK | `YamlFormatReaderInvalidInputTest` 8 件が 5 ケースすべてを担保（`inventory.md` §2.1-2 の軸F 表）。例外型は `YamlSchemaValidationException`（F2-01/03/04）・`IllegalStateException` ＋ 原因 `YamlEngineException`（F2-02）、F2-05 は例外にならずブロック 0 件になる結果をアサート。スキーマ違反の入力は `type: "text"`（列挙違反）と `length: "1a"`（パターン違反）で、いずれも引用符付き文字列である。**仕様外記法を使っていないことの確認**（2 通りで確かめた。下の「確認コマンド」参照）| | |
| `issues.md` に YML-01 と「対象としない入力」の YAML 側段落が記録されている（いずれも `src/main` 無変更） | OK | `issues.md` に `## #24 …` 節を新設し、`### YML-01 ~ ／ NULL は NULL にならず文字列になる` と `### 対象としない入力（辺②）` を置いた。YML-01 は解決経路 4 段（`YamlLoader#load` が `setSchema` を呼ばない → `LoadSettingsBuilder` の既定が `new JsonSchema()` → `JsonSchema` が `new JsonScalarResolver()` → `JsonScalarResolver.NULL` = `^(?:null)$`）を再現コマンド付きで示し、スキーマの該当パス（`$defs.table_data.properties.rows.items.additionalProperties.type` ほか）も併記した。**帰属は yaml 側**と明記。ID は `XLS-nn` ではなく `YML-nn` 系列。`src/main` 無変更は `git diff 3165770 -- src/main` が 0 行であることで確認 | | |
| 辺②について軸A の13種（`DEFAULT` を除く。`YamlFormatReader#addBlocksForSection` が既知セクションキーのみを分岐に持ち `DEFAULT` を生成しないため到達不能。根拠付きで空欄に残す）・軸B の4種・軸C の全フィールド（省略可能なものは省略時も。`sections` は `YamlFormatReader#read` が `Collections.singletonList` を返すため「空」「複数」とも到達不能として根拠付きで空欄）・軸E が埋まっている | OK | 軸A: `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` が 13 種を実 `.yaml` で ✅（A-01 は根拠付きで空欄）。軸B: `#readsFourBlockImplementationsFromOneRealYaml` が 4 種。軸C: #18 で欠けていた 8 要素のうち C-08/C-09/C-12/C-18/C-13(値あり) を担保済みへ、C-11(空)/C-13(空)/C-17/C-20 を根拠テスト付きの到達不能へ移した。C-02 は根拠付きで空欄。軸E: E-1/E-2/E-3 の 0・1・複数と E-4(1) を実 `.yaml` で担保、E-4(複数) は根拠付きで空欄（`inventory.md` §2.1-2 の軸C／軸E 表、§2.3 の未担保一覧＝**要追加 0 ／ 担保済み 22 ／ 到達不能 6**） | | |
| src/main への変更がゼロ | OK | `git diff 3165770 -- src/main \| wc -l` → **0** | | |
| `mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` → `Tests run: 461, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS`（基準線 419 ＋ 追加 42） | | |

### 確認コマンド（軸F の入力に仕様外のスカラー記法を使っていないこと）

対象ファイル: `src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java`

```sh
f=src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java

# A. 仕様外記法の文字列がソース中に現れるか
grep -nE 'true|false|123|1\.50|\.inf|\.nan' $f

# B. 埋め込み YAML の行のうち、値が二重引用符で始まらないもの
grep -oE '\+ "[^"]*\\n"' $f | grep -vE ': \\"'
```

- **A の結果**: ヒットするのは**クラス Javadoc の散文 4 行だけ**である
  （「F2-01 の入力に…仕様外とした引用符なしスカラー記法…は使わない」という但し書き本文と、
  `additionalProperties: false` の説明）。YAML フィクスチャの文字列リテラルには 1 件も現れない。
- **B の結果**: 出力されるのは `setup_files:` / `records:` / `- fields:` / `rows:` のような
  **マッピングキーだけの行**と、`fields: []` / `rows: []` の空配列だけである。
  **スカラー値を持つ行はすべて `: \"…\"` の形（＝二重引用符付き）**であり、B の出力に現れない。
- A だけでは「その語がソースに無い」ことしか言えず（YAML フィクスチャは Java の文字列リテラルなので
  行末が `\n"` になり、素朴な行末一致の grep は常に 0 を返してしまう）、B と組み合わせて初めて
  「**埋め込み YAML の全スカラー値が引用符付きである**」ことの根拠になる。

### 台帳の記述規約の自己点検（Steps の一項目）

| 点検項目 | 結果 |
|---|---|
| `grep -cE '\b(L[0-9]{1,4})\b' .rn/ntf-test-data-converter/coverage/inventory.md` が 0 | **0**（OK） |
| ファイル行数を書いていない | OK（#24 で書き足した箇所に行数の記述はない） |
| 書き足した件数のすべてに導出コマンドを併記 | OK（§2.1-2 のクラス別件数 3 件、§2.3 の軸D 23 件・軸F 8 件。いずれも `grep -c '^    @Test' <path>` を併記し、実行して同じ値を確認済み） |
| 逆引き表（軸要素 → 担保テストメソッド）を新設していない | **新設していない**。§2.1-2 の軸D／軸F／軸C／軸E 表は #19〜#23 が §1.2-2／§3.1-2／§3.1-3 で用いた「その要素を #24 が埋めたことを示す差分表」と同じ形式であり、逆引きの正は #27 の `coverage/axis-matrix.md` である旨は §2.3 冒頭の規則（§1.3 参照）どおり |
| 担保の穴をテストを足さない場合でも開示 | OK（§2.1-2 末尾「開示」に 2 点 —— `normalizeRecordType` の `"default"` 分岐が未到達／C-15 は実ファイル経路では到達不能） |

## Overall Verdict

- Self-check: OK

---

## 修正ラウンド 1（レビュー指摘の反映）

3 名のレビュアの指摘をコーディネータが triage した 21 項目（A1〜A8・B1〜B3・C1〜C5・D1〜D8）を、
`8aa536f` の成果物の上に積んで直した。作り直しはしていない。**各項目はまず一次情報（スキーマ本体・
実コード・実行結果）に当たって事実を確かめ、挙動を記録する項目は先に一時プローブで観測してから
アサートを書いた**（プローブ `src/test/.../yaml/ProbeTest.java` は記録後に削除済み）。

### Completion Criteria（再検証）

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 軸D の12ケース（D2-01〜D2-12）すべてがアサートされ、特に `null` ／値なし ／ `~` の3者の分かれ方と、`"null"` ／ `NULL` の扱いが結果として固定されている | OK（維持） | 12 ケースの定義もアサーションも変えていない。`YamlFormatReaderScalarTest` は 23 → **27** 件（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderScalarTest.java` → **27**）。増えた 4 件は D2-06／D2-11 を LIST_MAP 経路・レコード断片経路で確認したもので、**軸D の 12 ケース定義には足していない**（`inventory.md` §2.1-2「別経路での確認」表）。3 者の分かれ方を固定する 5 メソッドはそのまま | | |
| 軸F の5ケース（スキーマ違反／不正 YAML／未知キー／必須構造欠落／空ファイル）で例外型または結果がアサートされている。スキーマ違反のケースの入力に、仕様外とした引用符なしスカラー記法（`true` / `123` / `1.50` / `.inf` / `.nan`）を使っていない | OK（強化） | 件数は 8 のまま（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java` → **8**）。F2-01 の 2 件目を**キーワード集合＋位置の厳密アサート**へ強化した（A3）。入力の記法は変えていないため「仕様外記法を使っていない」ことは維持。ただし `123` が仕様外なのは **`rows` の値としてのみ**であることを Javadoc と `issues.md` に明記した（A2） | | |
| `issues.md` に YML-01 と「対象としない入力」の YAML 側段落が記録されている（いずれも `src/main` 無変更） | OK（拡充） | YML-01 に影響度「別枠」を付け、担保欄を「テストで担保した変種」（5 件）と「正規表現から導出した事実」（`Null` 1 件）に分けた（A7・B3）。「対象としない入力（辺②）」は適用範囲を **`rows` の値**に限定して書き直し、`field_def.length` の integer 記法が仕様内であることを再現コマンド付きで追記した（A2）。**YML-02**（送信系の `group_id` 省略でブロックが消える）と **YML-03**（`record_type: FW_HEADER` のレコードが捨てられる）を新規記録した（B1・B2）。`src/main` 無変更は `git diff 8aa536f -- src/main \| wc -l` → **0** | | |
| 辺②について軸A の13種・軸B の4種・軸C の全フィールド（省略可能なものは省略時も）・軸E が埋まっている | OK（維持。担保を厚くした） | §2.3 の集計は **要追加 0 ／ 担保済み 22 ／ 到達不能 6 ／ 総計 28** で変わらない。追加 9 件はいずれも既存の判定を動かさない（内訳は §2.3 末尾に明記）。厚くしたのは C-14（実ファイル経路。C5）・C-21 integer 記法（C3）・C-13 空の根拠を送信系経路にも用意（C4）・E-2(1 件) の引用を真にする行数アサート（A4）。§2.1 16 行目の軸F 欄と §2.3 の F 行の内訳を実物に合わせて訂正した（A1・A5） | | |
| src/main への変更がゼロ | OK | `git diff 8aa536f -- src/main \| wc -l` → **0**（`git diff HEAD -- src/main \| wc -l` → **0** も確認） | | |
| `mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean test -Djacoco.skip=true` → `Tests run: 470, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS`（修正前 461 ＋ 追加 9） | | |

### 指摘 21 項目の処置

**A. 台帳・課題一覧の記述が実物と食い違う**

| # | 確かめた事実（一次情報） | 処置 |
|---|---|---|
| A1 | 指摘は**正しい**。`$defs.group_message_data.required` も `$defs.expected_request_message_data.required` も `["id","records"]` だけで `group_id` を要求していない。`group_id` の description は「省略時は id 直接指定（先着1件）で動作する」「group_id を省略した場合は経路 B として動作する」と書いている（再現コマンドを `issues.md` YML-02 に併記し実行して確認） | 直した。`inventory.md` §2.1 16 行目の軸F 欄を「送信系必須の `group_id`」から実定義に基づく記述へ置き換え（🔺 F2-04 も外した）、`YamlFormatReaderTest#readSendSync_entryWithoutGroupId_isDropped` の Given コメントもスキーマの実定義へ置き換えた。**アサーションと入力は変えていない**（`git diff` で当該ファイルの変更がコメント 1 か所のみであることを確認） |
| A2 | 指摘は**正しい**。`$defs.field_def.properties.length` は `anyOf: [{type: integer, minimum: 0}, {type: string, pattern: "^([0-9]+\|-)$"}]` で、description に「integer 記法（10）も文字列記法（"10"）もどちらも有効」と明記されている | 直した。`issues.md`「対象としない入力（辺②）」の適用範囲を **`rows` の値**（`table_data` / `list_map_data` / `record_fragment`）に明示的に限定し、「`rows` 以外のプロパティには当てはまらない」節を再現コマンド付きで追加した。`YamlFormatReaderScalarTest` のクラス Javadoc と D2-03 の Javadoc、`YamlFormatReaderInvalidInputTest` のクラス Javadoc も同様に限定した。あわせて C3 で `length: 10` のテストを足した |
| A3 | 指摘は**正しい**。プローブで実測したところ `length: "1a"` が返す `ValidationMessage` は **2 件**で、`type`（`.../length/anyOf/0/type`）と `pattern`（`.../length/anyOf/1/pattern`）、位置はいずれも `$.setup_files[0].records[0].fields[0].length` である | **テスト側を強めた**（推奨どおり）。`assertTrue(... contains ...)` を `types(e)` ＝ `["type","pattern"]`・`locations(e)` ＝ 同一位置 2 件の厳密アサートへ置き換え、台帳の記述（キーワードと位置をアサートする）を真にした。台帳側にも件数 2 とその理由（`anyOf` の両枝を外す）を書いた |
| A4 | 指摘は**正しい**。`readsFourBlockImplementationsFromOneRealYaml` は行数をアサートしていなかった | **当該テストに行数アサートを足して引用を真にした**。テーブルと LIST_MAP について `getRows().size()` が 1 であることをアサートする 2 行を追加（入力は変更なし。E-2(1 件) の担保が実際に成立するようになった） |
| A5 | 数え直した。実物は F2-01 2 件／F2-02 1 件／F2-03 1 件／F2-04 3 件／F2-05 1 件 ＝ **8**。旧記述「本 4 ケースが 6 件」は F2-04 の 3 件のうち 2 件を C-17／C-20 側へ二重に振り分けていたため合わなかった | 直した。§2.3 の F 行を「本行の 4 ケース 7 件 ＋ F2-03 の 1 件」と書き分け、F2-04 の 3 件のうち 2 件が C-17／C-20 の根拠を**兼ねる**（別勘定ではない）ことを明記した。件数 8 は `grep -c '^    @Test' …` を併記し実行して確認 |
| A6 | 指摘は**正しい**。C-15 は §2.3 の「#18 の状態」列に無く、§2.1 の 12 行目（`readMessage_emptyBody_isStillMapped`）で ✅ とされていた | 直した。「到達不能と判定した軸要素（#24）」表の前置きに但し書きを足し、C-15 の行にも「この行だけ『要追加』からの移動ではない」と書いて、出典を開いた読み手が該当記述を見つけられるようにした |
| A7 | 指摘は**正しい**。YML-01 本文の `Null`、および「課題としない」表の `True` / `on` には対応するテストが無い（いずれも `JsonScalarResolver` の正規表現から導ける） | 直した。YML-01 を「テストで担保した変種」（5 件）と「正規表現から導出した事実」（`Null`）の 2 表に分割し、「課題としない」表の行も `TRUE` / `yes`（テストあり）と `True` / `on`（テスト無し・導出）に分けた |
| A8 | 指摘は**正しい**。`readValue` は常に `setup_tables` へ値を置く。行値の取り出しは 3 系統あり、レコード断片はスキーマのパスも別（`$defs.record_fragment.properties.rows.items.items.type`） | 直した。§2.1-2 の「開示」に 1 項（軸D の 12 ケースのうち 10 ケースは 1 経路でしか測っていない／残り 10 ケースの経路差は未確認）を足し、あわせて C2 で 2 ケース × 2 経路のテストを追加した |

**B. 挙動の記録（`issues.md`。修正はしない）**

| # | 実 `.yaml` で観測した結果 | 処置 |
|---|---|---|
| B1 | プローブで観測: **例外は出ず、黙って 0 件になる**。`group_id` 無しのみ 1 件 → ブロック 0 件。`group_id` 無し 1 件＋有り 1 件 → 有りの 1 件だけが残る | **YML-02** として記録（影響度 中・**検出できない**。並び順の原則どおり「検出できるか」優先の位置に置いた）。実ファイル経路の固定テスト `YamlFormatReaderRealFileTest#dropsSendSyncEntryWithoutGroupIdFromRealYaml` を 1 件追加。原因（`rawGroupsInOrder` が非 null の `group_id` だけを列挙する）とスキーマの実定義を再現コマンド付きで記録。`src/main` は無変更 |
| B2 | プローブで観測: `record_type: "FW_HEADER"` のレコードだけを書き `fw_header:` を書かない `messages` は、**例外にならずブロックは生成されるが `records` 0 件・`fwHeaderFields` 0 件**になる（本文レコードを足すと本文だけ残る。送信系でも同じ）。スキーマの description は `record_fragment.record_type` で「**FW_HEADER のような予約値はない**」、`message_data.records` で「**旧形式の record_type: FW_HEADER は廃止**」と 2 か所で明言している。にもかかわらず本体器（yaml jar の `YamlFileBuilder#buildFragmentsInternal` の `skipFwHeader` 分岐）と converter（`#recordsWithoutFwHeader`）の双方が落としている | **課題として記録する**と判断し **YML-03** を書いた（影響度 中・**検出できない**／帰属は yaml 側）。判断の根拠は「スキーマの description が仕様内と明言する入力が黙って消える」こと。ただし NTF 実行時も同じ器が同じレコードを落とすため**変換自体は NTF の解釈に忠実**である旨も書いた。固定テスト `#dropsFwHeaderNamedRecordFromRealYaml` を 1 件追加 |
| B3 | コーディネータの判断をそのとおり実装した | 凡例に「**影響度 別枠**」を 1 段足し、定義を指定どおり（変換結果は入力と一致するが作成者の意図と NTF 実行時の解釈が食い違う／上の 3 段は「変換結果が入力と一致するか」で定義されるため当てはまらない）に書いた。YML-01 の見出しに「影響度 別枠」を入れ、本文の「影響度の欄を置いていない理由」を「影響度を『別枠』とした理由」へ書き換えて拡張後の凡例を指すようにした。**並び順の原則は変えていない** |

**C. テストの補強**

| # | 観測 | 処置 |
|---|---|---|
| C1 | 指摘は**正しい**。sources jar の `YamlLoader#load` は `loaded == null` のとき `YAML_CACHE.put(...)` して `Collections.emptyMap()` を返し、`JSON_SCHEMA.validate` に到達しない。`setup_tables: []` は非 null の Map になるため検証を通る（プローブでブロック 0 件・コンテナ／セクション名ともリソース名を観測） | `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks` の入力を `# no sections` から **`setup_tables: []`** へ変更し、E-1(0 件)・C-04(空) の担保をスキーマ検証済みの経路に載せた。空ファイルそのもの（＝検証を迂回する分岐）の担保は `InvalidInputTest#readsEmptyFileAsContainerWithoutBlocks` に残し、両テストの Javadoc に「通る経路が違う」ことを相互参照で書いて重複を解消した |
| C2 | プローブで観測: `null` → Java `null`、`""` → 空文字。**LIST_MAP 経路・レコード断片経路（fixed／variable とも）で `setup_tables` 経路と差は無かった** | `YamlFormatReaderScalarTest` に 4 件追加（`readsUnquotedNullAsJavaNullInListMapPath` ／ `readsEmptyStringAsIsInListMapPath` ／ `readsUnquotedNullAsJavaNullInRecordFragmentPath` ／ `readsEmptyStringAsIsInRecordFragmentPath`）。**軸D の 12 ケース定義は変えていない**。台帳には「同一ケースを別経路で確認したもの」として §2.1-2 に別表を置き、経路差が無いという観測結果を書いた |
| C3 | プローブで観測: `length: 10`（引用符なし整数）はスキーマを通り、`FieldDef.length` には**文字列 `"10"`** が入る（`YamlSection#toStr` が `toString()` するため） | `YamlFormatReaderRealFileTest#readsIntegerLengthNotationAsString` を 1 件追加。**軸D の 12 ケースには足さず**、軸C の `FieldDef.length`（C-21）の担保として §2.1-2 の軸C 表に書いた |
| C4 | プローブで観測: 送信系（`addSendSyncBlocks` 経路）でも `directives` は空にならず `{file-type=Fixed}` を持つ | `#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInSendSync` を 1 件追加し、台帳 §2.1-2 の C-13(空・到達不能) の根拠を**2 つの生成経路の両方**に対応させた |
| C5 | プローブで観測: `fw_header:` に書いたキーはそのまま `MessageDataBlock.fwHeaderFields` に**記述順で**入る（`{requestId=RM01, userId=u1}`）。本文レコードは影響を受けない | `#readsFwHeaderFieldsFromRealYaml` を 1 件追加し、§2.1-2 の軸C 表に C-14（値あり）を「in-memory のみ → 実ファイル経路でも」として追記した |

**D. コーディング作法**

| # | 処置 |
|---|---|
| D1 | `YamlFixture.write(File, String)` を `read` へインライン化した（呼び出しは `read` 1 か所だけであることを確認済み） |
| D2 | `YamlFormatReaderInvalidInputTest` の `list(String...)` を削除し、その呼び出し 10 か所を `Arrays.asList` に統一した（A3 で足した 2 か所を含め、同ファイルの `Arrays.asList` は計 12 か所。`grep -c 'Arrays.asList' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java` → **12**。`list(` の残存が 0 であることも確認） |
| D3 | `assertTrue("…", !(e instanceof YamlSchemaValidationException))` を `assertFalse(…, e instanceof …)` にした（`assertFalse` を static import に追加） |
| D4 | static import の並びを完全修飾名の昇順に揃えた（いずれも `is` と `instanceOf` が逆だった）。`InvalidInputTest` は `instanceOf` / `is` / `assertFalse` / `assertThat` / `assertTrue` / `fail`、`RealFileTest` は `instanceOf` / `is` / `nullValue` / `assertThat` / `assertTrue`。`ScalarTest` は既に昇順だったため変更なし。既存クラス（`XlsFormatReaderRealFileTest`・`YamlFormatReaderTest` ほか）と同じ並びである |
| D5 | `YamlFixture.read` の Javadoc に「**副作用**: 読み込み前に `YamlLoader.clearCacheForTest()` を呼び、静的グローバルの LRU キャッシュを空にする」とその理由を書いた |
| D6 | `YamlFormatReaderRealFileTest`（16 件）と `YamlFormatReaderInvalidInputTest`（8 件）の各テスト Javadoc に `<p>担保する軸要素: …</p>` を 1 行足し、クラス Javadoc にも `XlsFormatReaderRealFileTest` と同じ「各テストの Javadoc には…軸要素の ID を記す」という一文を入れた |
| D7 | `readValue` の Javadoc に「ブロックスカラーの継続行のインデントは**半角空白 10 個以上**」であることと、その理由（値の位置が 8 桁目から始まるため）・インデント量が値に現れない理由を書いた |
| D8 | **揃えるほうを選んだ**。`YamlFixture` の API を `File` → `Path` に変え、内部（`Files`）と兄弟の `XlsFixture`（`open(Path)` ほか）に合わせた。呼び出し側 3 クラスには `XlsFormatReaderRealFileTest` と同じ `private Path dir()` ヘルパを置き、`folder.getRoot()` の直接参照を無くした |

### 台帳の記述規約の自己点検（再実行）

| 点検項目 | 結果 |
|---|---|
| `grep -cE '\b(L[0-9]{1,4})\b' .rn/ntf-test-data-converter/coverage/inventory.md` が 0 | **0**（OK。実行して確認） |
| ファイル行数・コマンドを併記しない件数を書いていない | OK（今回書き足した件数は §2.1-2 のクラス別 3 件と §2.3 の軸D・軸F。いずれも `grep -c '^    @Test' <path>` を併記） |
| 件数を書き換えたら記憶で直さずコマンドから導き直し、そのコマンドを併記する／併記したコマンドはそのまま実行して同じ結果が出る | OK。`inventory.md` 中の `grep -c '^    @Test' …` を**全件抜き出して実行**し、書かれた値と一致することを確認した（YAML 3 クラス: 27 / 8 / 16。XLS 5 クラス: 10 / 18 / 16 / 18 / 40） |
| 逆引き表（軸要素 → 担保テストメソッド）を新設していない | **新設していない**。追加した「別経路での確認」表は §2.1-2 の既存の軸D 表に続く**同一ケースの経路差の記録**であり、軸要素を網羅する索引ではない。逆引きの正が #27 の `coverage/axis-matrix.md` である点は変えていない |
| 担保の穴は、テストを足さない場合でも台帳に開示する | OK。§2.1-2 末尾の「開示」を 2 点 → **4 点**にした（既存 2 点＋軸D の 10 ケースは 1 経路でしか測っていない／YML-02・YML-03 で「警告が出ないこと」を実行可能な形にしていない）。§2.3 末尾の参照も 4 点に更新した |

### Overall Verdict（修正ラウンド 1）

- Self-check: OK

---

## 修正ラウンド 2（2 巡目レビュー指摘の反映）

実施日: 2026-08-14。起点コミット `95f19eb`。`src/main` は無変更。

### Completion Criteria（再々検証）

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 軸D の12ケース（D2-01〜D2-12）すべてがアサートされ、特に `null` ／値なし ／ `~` の3者の分かれ方と、`"null"` ／ `NULL` の扱いが結果として固定されている | OK（維持） | 12 ケースの定義もアサーションも変えていない。`YamlFormatReaderScalarTest` は **27 件のまま**（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderScalarTest.java` → **27**）。本ラウンドで同クラスに加えたのは（a）`readValue("")` の暗黙の合図を `readOmittedValue()` へ分けたこと（`readsOmittedValueAsJavaNull` の入力・期待値は同じ）、（b）ヘルパにブロック数アサートを足したこと、（c）Javadoc の訂正（インデント 10 → **9**、および D2-11 への但し書き）だけである | | |
| 軸F の5ケース（スキーマ違反／不正 YAML／未知のキー／必須構造の欠落／空ファイル）で例外型または結果がアサートされている。スキーマ違反のケースの入力に、仕様外とした引用符なしスカラー記法（`true` / `123` / `1.50` / `.inf` / `.nan`）を使っていない | OK（維持） | 軸F を担保する 8 件は入力もアサーションも変えていない（try/catch/`fail()` を `assertThrows` へ置き換えただけ。`assertSchemaViolation` の到達しない `return null;` は消えた）。同クラスの総数は 8 → **23**（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java` → **23**）。増えた 15 件は掃引で見つけた現状挙動の固定（YML-04〜YML-08）であり**軸F の要素ではない**。クラス Javadoc と `inventory.md` §2.3 の F 行に、8 件と 15 件の区別を明記した | | |
| `issues.md` に YML-01 と「対象としない入力」の YAML 側段落が記録されている（いずれも `src/main` 無変更） | OK（維持・拡充） | YML-01 と「対象としない入力（辺②）」は本ラウンドで変更していない。拡充は（a）YML-02 の引用元 JSON パスの明示と再現コマンドの差し替え、（b）新節「#24 修正ラウンド 2（スキーマの自由度の掃引）で記録した課題」に **YML-04〜YML-08** を追加、（c）「課題としないと判断した観測結果（#24）」の「空文字と値なしが区別される」行に YML-05 の但し書きを追加。`src/main` 無変更は `git diff 95f19eb -- src/main \| wc -l` → **0** | | |
| 辺②について軸A の13種・軸B の4種・軸C の全フィールド（省略可能なものは省略時も）・軸E が埋まっている | OK（維持） | §2.3 の集計は **要追加 0 ／ 担保済み 22 ／ 到達不能 6 ／ 総計 28** のまま。本ラウンドで足した 16 件のうち 15 件は軸A〜F のどの要素にも属さず、残る 1 件（`normalizesLowercaseDefaultRecordTypeToNull`）は #18 時点で既に ✅ の C-16 を実 `.yaml` で確かめたものである。その旨を §2.3 末尾に追記した | | |
| src/main への変更がゼロ | OK | `git diff 95f19eb -- src/main \| wc -l` → **0**（`git diff HEAD -- src/main \| wc -l` → **0** も確認） | | |
| `mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean test -Djacoco.skip=true` → `Tests run: 486, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS`（修正ラウンド 1 の 470 ＋ 追加 16） | | |

### 指摘への処置（A1〜A4・B1〜B2・C1〜C4）

**A. 記録されていない「黙って壊れる」挙動**

| # | 自分で確かめた事実（一次情報） | 処置 |
|---|---|---|
| A1 | 指摘は**正しい**。実 `.yaml` で観測: `setup_tables` の `rows: [{A: "1"}, {A: "2", B: "x"}]` → `columnNames=[A]`・`rows=[[1],[2]]` で `B: "x"` が例外も警告も無く消える。`list_maps` でも同じ。逆向き（2 行目でキーが欠ける）は `null` 埋めで救われる。原因は `YamlSection#resolveColumns` が `rows.get(0)` のキー集合しか返さないこと（yaml sources jar を grep して確認）と、`YamlFormatReader#nonMarkerColumns` がそれをそのまま使うこと。**さらに先頭行が空マッピング `{}` の場合、テーブル経路では後続行ごと `rows=[]` になる**（LIST_MAP 経路は行数だけ残る）ことも実測した | **YML-04** として記録（影響度 高・**検出できない**／帰属は yaml 側と converter の両方）。固定テスト 5 件を `YamlFormatReaderInvalidInputTest` に追加（テーブル／LIST_MAP の drop、`null` 埋めの非対称、空マッピング先頭行のテーブル経路／LIST_MAP 経路）。スキーマの該当パスと原因コードの再現コマンドを併記して実行確認 |
| A2 | 指摘は**正しい**。実 `.yaml` で観測: `fields=[f1]` に `rows: [["a","b","c"]]` → `rows=[[a]]`。`fields=[f1,f2,f3]` に `rows: [["a"]]` → `rows=[[a, "", ""]]`（**空文字**）。同じ断片に `rows: [["a", null]]` を足すと `[[a, null, ""]]` になり、**書いた `null` は残り欠損だけが `""` になる**。原因は nablarch-testing の `DataFileFragment#addValue` の `i < line.size() ? line.get(i) : ""`。スキーマ `$defs.record_fragment.properties.rows` の description は「各配列の要素数が fields の件数と一致しない場合は NTF がエラーを出す」と書いているが、変換時にはエラーにならない | **YML-05** として記録（影響度 中・**検出できない**。余りの drop と不足の `""` 充填を表で分けて書き、帰属を nablarch-testing 側と判定）。固定テスト 2 件を追加。**「空文字と null は区別される」への但し書き**を 2 か所に付けた —— `issues.md`「課題としないと判断した観測結果（#24）」の該当行と、`YamlFormatReaderScalarTest#readsEmptyStringAsIsInRecordFragmentPath` の Javadoc（固定テストへの相互参照つき） |
| A3 | 指摘は**正しい**。`record_type: "default"`（小文字）を書いた実 `.yaml` はスキーマを通り（`$defs.record_fragment.properties.record_type` に `enum` は無い）、`normalizeRecordType` の小文字側の分岐に到達する。観測: **作成者が書いた `"default"` は中間モデルで `null` になる**（`"Default"` と同じ）。参考として `"DEFAULT"`（全大文字）は `"DEFAULT"` のまま残ることもプローブで確認した（テストは足していない） | 固定テスト `YamlFormatReaderRealFileTest#normalizesLowercaseDefaultRecordTypeToNull` を 1 件足して分岐を閉じた。`inventory.md` §2.1-2 の「開示」1 点目を**実態に合わせて書き直した**（「到達不能で #26 送り」→「到達可能であり本ラウンドで閉じた。JaCoCo の数値は #24 時点の実測であり本ラウンド後は再計測していないので #26 で再計測すること」）。§2.3 末尾の参照も更新した |
| A4 | 指定の 4 手順で 1 周した。スキーマ本体を先頭から読み、「構造を縛っていない箇所」を **23 項目**列挙し、各項目につき実 `.yaml` を 1 回ずつ通して観測した。黙って壊れたのは 4 件（**YML-04**／**YML-05**／**YML-06**／**YML-08(a)**。既記録の YML-02・YML-03 に当たる項目も同じ掃引で再確認した）、loud に失敗したのは 5 件（未知の型名／固定長専用・可変長専用ディレクティブの取り違え／フィールド名の重複／**YML-07** の NPE／**YML-08(b)** のタブ記法）、残りは壊れなかった（表に観測結果を書いた） | **新規記録は YML-06**（`id` 重複時に 2 件目以降が 1 件目のデータで作られる。帰属は converter 側）、**YML-08**（ディレクティブ値の `trim()` で、スキーマ description が推奨するリテラル記法が空文字になる／タブ記法が例外になる。あわせてシンボル記法が中間モデルで実文字になる辺①との非対称も記録）、**YML-07**（`length: "-"` ＋ `text-encoding` 省略で手掛かりの無い NPE）。固定テストは順に 2 件・3 件・2 件を追加。**列挙した 23 項目そのものを `inventory.md` §2.1-2 の「開示（修正ラウンド 2）」に表で載せ**、あわせて「この掃引で見ていない範囲」5 点（組合せ未検証／NTF 実行時の description は未観測／辺③④は対象外・YML-08 の往復は未確認／インタープリタ記法は未掃引／軸D との掛け合わせ無し）を書いた。**4 手順の外へは広げていない** |

**B. 出典・記述の正確さ**

| # | 自分で確かめた事実（一次情報） | 処置 |
|---|---|---|
| B1 | 指摘は**正しい**。スキーマを開いて確認したところ、「group_id を省略した場合は経路 B として動作する」は `$defs.group_message_data.description`（**定義レベル**）にあり、`properties.group_id.description` には無い。「省略時は id 直接指定（先着1件）で動作する」は `$defs.expected_request_message_data.properties.group_id.description` にだけある。旧再現コマンドは `properties.group_id.description` しか印字しないため、引用文が 1 つも出なかった | 直した。`issues.md` YML-02 に**引用元の JSON パスと逐語引用の対応表**を置き、再現コマンドを `required` ＋ 定義レベル `description` ＋ `properties.group_id.description` の 3 つを印字するものへ差し替えた（実行して 3 引用がすべて出ることを確認）。`YamlFormatReaderRealFileTest#dropsSendSyncEntryWithoutGroupIdFromRealYaml` の Javadoc と `YamlFormatReaderTest#readSendSync_entryWithoutGroupId_isDropped` の Given コメントにも同じ取り違えがあったため、両方をパス付きの記述へ直した |
| B2 | 指摘は**正しい**。snakeyaml-engine 3.0.1 で実測（インデント 6〜12 を総当たり）: **9・10・11・12 は成功**、**8 は `ScannerException`**、**7 は `ParserException`**、**6 は `ScannerException`**。キー `V` は `"      - V:"` の 9 桁目から始まる | 直した。`YamlFormatReaderScalarTest#readValue` の Javadoc を「半角空白 **9 個以上**」へ書き改め、8／7／6 の実測結果と、その理由（キーの開始桁）も書いた。テストが 10 個で書いてある事実はそのまま残した |

**C. テストの書き方**

| # | 処置 |
|---|---|
| C1 | `YamlFormatReaderInvalidInputTest` の try/catch/`fail()` を `org.junit.Assert.assertThrows` へ置き換えた（`assertSchemaViolation` と `failsWithParseErrorWhenYamlIsMalformed` の 2 か所）。到達しない `return null;` は消えた。`fail` の static import も外した。新規追加の 2 件（`failsWhenFieldSeparatorIsWrittenAsActualTab`／`failsWithNullPointerExceptionWhenOndemandLengthIsUsedWithoutTextEncoding`）も同じ形で書いた |
| C2 | ダイヤモンド演算子に統一した。`YamlFormatReaderInvalidInputTest` の `types`／`locations`（`new ArrayList<String>()` → `<>`）と `YamlFormatReaderRealFileTest` の 2 か所（`new ArrayList<DataType>()`／`new ArrayList<String>(...)` → `<>`）。`grep -n 'new ArrayList<' src/test/java/nablarch/test/tool/converter/yaml/*.java` の残りは本ラウンドの scope 外のクラス（`YamlFormatReaderTest`／`YamlFormatWriterTest`）のみ |
| C3 | **`@After` を正とし、フィクスチャ側の事前クリアを外すほうを選んだ。** 理由: 3 クラスの `@After clearLoaderCache` は既存の `RoundTripTest` ほかと同じ形であり、そちらを消すと既存クラスと揃わなくなる。フィクスチャを使うテストはすべて `TemporaryFolder` がテストごとに作る別ディレクトリへ書くため、1 メソッドの中で同一パスを書き直さない限りキャッシュは衝突しない（現状の全テストは 1 メソッド 1 read である）。`YamlFixture.read` の Javadoc も「LRU キャッシュはここでは触らない。責務は利用側の `@After` に一本化してある」と実態どおりに書き改めた |
| C4 | （a）`YamlFixture.read` に `Files.createDirectories(dir)` を足し、`UncheckedIOException` のメッセージを `"failed to write fixture: " + file` にした（兄弟 `XlsFixture#writeTo` と同じ形）。（b）`readValue` ／ `readListMapValue` ／ `readRecordFragmentValue` の 3 ヘルパにブロック数アサート（`blocks(container).size()` == 1）を足した。新設した `YamlFormatReaderInvalidInputTest` のヘルパ `onlyBlock` ／ `onlyRecord` も同じアサートを持つ。（c）`readValue(String...)` の「1 要素目が空文字＝値なし」という暗黙の合図をやめ、値なし専用の `readOmittedValue()` に分けた（両者は共通の `readValueLine` を呼ぶ） |

### 台帳の記述規約の自己点検（再実行）

| 点検項目 | 結果 |
|---|---|
| `grep -cE '\b(L[0-9]{1,4})\b' .rn/ntf-test-data-converter/coverage/inventory.md` が 0 | **0**（OK。実行して確認） |
| ファイル行数・コマンドを併記しない件数を書いていない | OK。本ラウンドで書き換えた件数は §2.1-2 のクラス別 3 件（27 / 23 / 17）と §2.3 の F 行（総数 23）で、いずれも `grep -c '^    @Test' <path>` を併記した。掃引表の「23 項目」「5 点」は台帳内で全件を列挙しているため数え直せる |
| 件数を書き換えたら記憶で直さずコマンドから導き直し、そのコマンドを併記する／併記したコマンドはそのまま実行して同じ結果が出る | OK。`inventory.md` に現れる 11 個のテストクラスパスすべてについて `grep -c '^    @Test'` を実行し、書かれた値と一致することを確認した（XLS: 10 / 33 / 18 / 16 / 18 / 40、YAML: 23 / 17 / 27 / 20 / 33） |
| 併記した再現コマンドを実際に実行して、引用した文言が出力に現れる | OK。本ラウンドで追加・差し替えた再現コマンド 9 本（YML-02 の 1 本、YML-04 の 2 本、YML-05 の 2 本、YML-06 の 2 本、YML-07 の 1 本、YML-08 の 2 本）を**すべてそのまま実行**し、引用文・引用したコード行が出力に現れることを確認した |
| 担保の穴は、テストを足さない場合でも台帳に開示する | OK。§2.1-2 に「開示（修正ラウンド 2）」を新設し、掃引で列挙した 23 項目の全件と「見ていない範囲」5 点を書いた。既存の「開示」4 点のうち 1 点目（`"default"` 分岐）は実態に合わせて書き直した |
| 逆引き表を新設していない | 掃引表は「スキーマ上の自由度 → 観測結果」であって「軸要素 → 担保テストメソッド」ではないため、逆引きの正（#27 の `coverage/axis-matrix.md`）とは競合しない |

### Overall Verdict（修正ラウンド 2）

- Self-check: OK
