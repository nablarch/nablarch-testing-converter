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
