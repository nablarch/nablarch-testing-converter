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
