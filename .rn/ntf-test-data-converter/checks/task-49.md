# task-49 —— (c) 判定を受けた第2ラウンド（C6 の安全網追加と (c) 全箇所の理由コメント）

指示書: `nablarch-document@origin/ntf-yaml-support`（`89451e9e`）の
`.rn/20260724-ntf-yaml-support/ntf-step4-09-converter-coverage.md` §5。

着手時 HEAD: `ef5add3`（`src` は `#48` 完了時 `1fcaec9` と同一）。完了時 HEAD: `7a539b1`。

---

## 1. 差分

`git diff --stat 1fcaec9..7a539b1 -- src/` → 7 ファイル・+39／−6（`src/test` の変更は 0）。

実行文の変更は **C6 の `else` 節（`throw` 1 文）だけ**である。他はすべてコメント行の追加・書き直しで、
`XlsFormatWriter.java` の −1／+1 は #48 (b) で消した `isMarkerColumn` の `null` 判定に言及したままだった
陳腐化コメントの是正である。

| ファイル | 変更 |
|---|---|
| `xls/XlsFormatReader.java` | +6（C6。`else` 節 ＝ コメント 4 行 ＋ `throw` 1 文 ＋ `}` ） |
| `core/reader/YamlTestCoreAdapter.java` | +5（C2 のコメント新設） |
| `yaml/YamlTestDataValidator.java` | +11／−1（C7 のコメント新設 6 行・C8 のコメント 1 行 → 4 行） |
| `ConverterFileFilter.java` | +6／−1（C3 のコメント 1 行 → 5 行） |
| `xls/Styles.java` | +5／−1（C4-Styles のコメント 1 行 → 4 行） |
| `xls/XlsFormatWriter.java` | +7／−2（C5-Xls のコメント 1 行 → 4 行、陳腐化コメントの是正 1 行） |
| `yaml/YamlFormatWriter.java` | +5／−1（C5-Yaml のコメント 1 行 → 4 行） |

## 2. (c) 全箇所のコメント一覧（逐語）

行番号は完了時 `7a539b1`。基準は「各コメント単体で『なぜ到達不能か』『なぜ残しているか』の両方が分かること」。

### C1. 本体テンプレートの抽象メソッド実装 —— 変更なし（両方を既に満たす）

`core/reader/TestCoreReaderAdapter.java:410`-`:411`（`HeaderCollector`）・`:504`-`:505`（`BodyLineCollector`）。
両箇所とも同文:

```
// parse(String id) を上書きしているため doParse() からこれらの抽象メソッドは呼ばれない。
// TestDataParsingTemplate の契約上、実装は必須。
```

到達不能の理由（`parse` を上書きしているため `doParse()` から呼ばれない）と残す理由（テンプレートの契約上、
実装が必須）の両方を述べているため書き換えていない。

### C2. YAML エントリが Map でない側 —— 新設

`core/reader/YamlTestCoreAdapter.java:229`-`:233`:

```
// 到達しない。ここへ来る yaml は loadRawMap ／ YamlLoader.load を通ったものだけで、
// 同メソッドはスキーマ検証に通らない YAML を返さずに例外にするため、セクションの要素が
// Map 以外になることがない。それでも残すのは、直後の Map への無検査キャストを守るためで、
// 判定を落とすとスキーマを経ない Map を渡されたときに ClassCastException になり、
// 原因の分からない場所で落ちる。
```

### C3. 同名ブック検査の `Files.list` が投げる `IOException` —— 追記

`tool/converter/ConverterFileFilter.java:185`-`:189`:

```
// 到達しない。dir は直前の Files.walk が走査できたブックの親ディレクトリであり、走査できた以上
// そのディレクトリは読める。Files.list が失敗するのは走査との間に権限か存在が変わった場合だけで、
// テストから決定的には作れない。それでも残すのは、Files.list がチェック例外 IOException を
// 宣言するため catch を外せず、ストリーム操作の関数インタフェース契約を満たすには
// ラップが要るためである。
```

書き換え前は残す理由（チェック例外の宣言）だけで、到達不能の理由が無かった。

### C4. 列挙を尽くした `switch` の `default`

**`tool/converter/DataFormat.java:47` —— 変更なし（両方を既に満たす）**:

```
// 将来 enum 値が追加された際にハンドラ未実装を即座に検出するための安全網。switch は網羅性をコンパイル時に保証しないため必須。
```

**`tool/converter/xls/Styles.java:91`-`:94` —— 追記**:

```
// 到達しない。Fill は 6 値で、NONE は直前の if で除かれ、残る 5 値を上で分岐済みのため。
// それでも残すのは、switch の網羅性がコンパイル時に保証されず、default を落とすと
// color が確定せずコンパイルできないためであり、Fill に値が増えたときに塗りの
// 未実装をその場で検出する安全網も兼ねる。
```

書き換え前は `// unreachable — new Fill constants must be handled here` の 1 行で、なぜ到達不能かを
述べていなかった。

**`tool/converter/yaml/YamlFormatWriter.java:506`-`:507` —— 変更なし（両方を既に満たす）**:

```
// 到達不能。DEFAULT は TestDataBlock が生成時に拒否し（issues.md XLS-20）、
// 残る 13 個の DataType は上ですべて分岐しているため。DataType が増えたときの安全網として残す。
```

### C5. sealed 階層に対する `instanceof` 連鎖の末尾 —— 2 箇所とも追記

**`tool/converter/xls/XlsFormatWriter.java:221`-`:224`**（`layout`。戻り値を返すメソッド）:

```
// 到達しない。TestDataBlock は sealed で、許可された派生型は上ですべて判定しているため、ここへ届く
// インスタンスを作れない。それでも残すのは、instanceof チェーンにはコンパイル時の網羅性保証がなく、
// 本メソッドは戻り値を返すため末尾の throw を落とすとコンパイルできないためで、sealed 階層が
// 将来変更された場合のランタイム安全網も兼ねる。
```

**`tool/converter/yaml/YamlFormatWriter.java:142`-`:145`**（`emitBlock`。戻り値を返さないメソッド）:

```
// 到達しない。TestDataBlock は sealed で、許可された派生型は上ですべて判定しているため、ここへ
// 届くインスタンスを作れない。それでも残すのは、instanceof チェーンにはコンパイル時の網羅性
// 保証がなく、sealed 階層に派生型が増えたとき、そのブロックが黙って出力から欠落するのを防いで
// その場で検出するためである。
```

**2 箇所で残す理由の書き方を変えている。**`layout` は戻り値を返すため `throw` を落とすとコンパイル
できないが、`emitBlock` は `void` なので `else` ごと落としてもコンパイルは通る。後者の残す理由は
コンパイル制約ではなく「派生型が増えたときに出力から黙って欠落するのを防ぐ」ことである。

### C6. 送信同期タイプ判定の偽側 —— `else` 節を新設（実行文の変更はここだけ）

`tool/converter/xls/XlsFormatReader.java:132`-`:137`:

```
} else {
    // 到達しない。readHeaders はマーカー行から取れた DataType だけを返し DataType.DEFAULT は
    // 返さないため、ここへ届くのは残る 13 種であり、上の 5 分岐がその 13 種を尽くしている。
    // それでも残すのは、DataType に値が増えたとき、その種別のブロックが黙って変換結果から
    // 欠落するのを防ぎ、その場で検出するための安全網としてである。
    throw new IllegalStateException("unhandled DataType: " + type);
}
```

同じ 13 種を捌く `YamlFormatWriter#sectionKey` の `default` throw と同型。追加した `throw` は到達不能
（承認済み扱い）で、テストは足していない。

### C7. スキーマリソースのロード失敗 —— 新設

`tool/converter/yaml/YamlTestDataValidator.java:58`-`:63`（`static` 初期化子の先頭）:

```
// 以下の失敗経路 2 つ（in == null ／ IOException）は到達しない。SCHEMA_RESOURCE は依存の
// nablarch-testing-yaml の jar に同梱されており、本クラスと同じクラスローダで必ず解決するため、
// in が null になることも、jar 内リソースの読み込みが IOException になることもない。
// それでも残すのは、null 判定を落とすとリソースを欠いた配布物で NullPointerException になり
// 原因が読めなくなるためと、try-with-resources の close が IOException を宣言するため
// catch 自体を外せないためである。
```

未達 4 行（`:65`・`:66`・`:69`・`:70`）は `try` ブロックの 2 経路にまたがるため、両方を 1 つのコメントで
`try` の直前にまとめている。

### C8. スキーマ検証器が投げる非検査例外 —— 追記

`tool/converter/yaml/YamlTestDataValidator.java:159`-`:162`:

```
// 到達しない。schema.validate へ届くのは直前の parseYaml を通った入力だけであり、parseYaml が
// 使う snakeyaml-engine は networknt が内部で使う parser より厳しいため、壊れた入力は手前で
// [V-YAML] として捕まる。それでも残すのは、リンタは全ファイルを走査し切ることが役目であり、
// 検証器が送出しうる非チェック例外で途中停止すると残りのファイルが検査されないためである。
```

書き換え前は残す理由（リンタが停止しないため）だけで、到達不能の理由が無かった。

### 併せて是正したもの（(c) ではない）

`tool/converter/xls/XlsFormatWriter.java:275`-`:276`。#48 (b) で `isMarkerColumn` の `null` 判定を削除
したのに、本文コメントが「isMarkerColumn の null 判定は防御として残してある」と言ったままだった。
当該 1 文を落とし、残る記述（カラム名の `null` は `ColumnRowDataBlock` が生成時に拒否するためここへは
届かない）だけにした。

## 3. 再測定

手順・抽出コマンドは `checks/task-48.md` §1 と同一。

| 区分 | missed | covered | 到達率 | #48 完了時（`ed7c6d9`） |
|---|---|---|---|---|
| LINE | 22 | 1677 | 1677/1699 ＝ 98.71% | 21 / 1677 |
| BRANCH | 8 | 800 | 800/808 ＝ 99.01% | 8 / 800 |
| INSTRUCTION | 90 | 8041 | — | 83 / 8040 |
| METHOD | 8 | 359 | — | 8 / 359 |
| CLASS | 0 | 52 | — | 0 / 52 |

**未達を持つ行は 30 行**（#48 完了時 29 行 ＋ 新設 `throw` 1 行）。**分岐は 8 で不変。**

行数が +1、命令が +7、覆われた行が同数であることが、追加した実行文が到達不能な `throw` 1 文だけである
ことと整合する。

### 未達全 30 行の (c) 対応（行番号ずれの吸収）

| ファイル:行（`7a539b1`） | ソース | #48 の行（`ed7c6d9`） | (c) |
|---|---|---|---|
| `core/reader/TestCoreReaderAdapter.java:415` | `}` | `:415` | C1 |
| `core/reader/TestCoreReaderAdapter.java:420` | `}` | `:420` | C1 |
| `core/reader/TestCoreReaderAdapter.java:424` | `return false;` | `:424` | C1 |
| `core/reader/TestCoreReaderAdapter.java:429` | `return false;` | `:429` | C1 |
| `core/reader/TestCoreReaderAdapter.java:509` | `}` | `:509` | C1 |
| `core/reader/TestCoreReaderAdapter.java:514` | `}` | `:514` | C1 |
| `core/reader/TestCoreReaderAdapter.java:518` | `return false;` | `:518` | C1 |
| `core/reader/TestCoreReaderAdapter.java:523` | `return false;` | `:523` | C1 |
| `core/reader/YamlTestCoreAdapter.java:234`（分岐 1） | `if (!(entryObj instanceof Map)) {` | `:229` | C2 |
| `core/reader/YamlTestCoreAdapter.java:235` | `continue;` | `:230` | C2 |
| `ConverterFileFilter.java:184` | `} catch (IOException e) {` | `:184` | C3 |
| `ConverterFileFilter.java:190` | `throw new UncheckedIOException(...)` | `:186` | C3 |
| `DataFormat.java:41`（分岐 1） | `switch (this) {` | `:41` | C4 |
| `DataFormat.java:48` | `throw new IllegalStateException(...)` | `:48` | C4 |
| `xls/Styles.java:74`（分岐 1） | `switch (fill) {` | `:74` | C4 |
| `xls/Styles.java:95` | `throw new IllegalStateException(...)` | `:92` | C4 |
| `xls/XlsFormatReader.java:128`（分岐 1） | `} else if (XlsDataTypeUtil.isSendSyncType(type)) {` | `:128` | C6 |
| `xls/XlsFormatReader.java:137` | `throw new IllegalStateException(...)` | **新設** | C6 |
| `xls/XlsFormatWriter.java:218`（分岐 1） | `} else if (block instanceof MessageDataBlock) {` | `:218` | C5 |
| `xls/XlsFormatWriter.java:225` | `throw new IllegalArgumentException(...)` | `:222` | C5 |
| `yaml/YamlFormatWriter.java:139`（分岐 1） | `} else if (block instanceof MessageDataBlock) {` | `:139` | C5 |
| `yaml/YamlFormatWriter.java:146` | `throw new IllegalArgumentException(...)` | `:143` | C5 |
| `yaml/YamlFormatWriter.java:491`（分岐 1） | `switch (type) {` | `:488` | C4 |
| `yaml/YamlFormatWriter.java:508` | `throw new IllegalArgumentException(...)` | `:505` | C4 |
| `yaml/YamlTestDataValidator.java:65`（分岐 1） | `if (in == null) {` | `:59` | C7 |
| `yaml/YamlTestDataValidator.java:66` | `throw new IllegalStateException(...)` | `:60` | C7 |
| `yaml/YamlTestDataValidator.java:69` | `} catch (IOException e) {` | `:63` | C7 |
| `yaml/YamlTestDataValidator.java:70` | `throw new IllegalStateException(...)` | `:64` | C7 |
| `yaml/YamlTestDataValidator.java:158` | `} catch (RuntimeException e) {` | `:152` | C8 |
| `yaml/YamlTestDataValidator.java:163` | `errors.add(new ValidationError(...))` | `:154` | C8 |

**(c) の箇所（＋新設 throw）と完全一致する。他に未達は無い。**

## 4. ゲート

- `mvn -o clean test` → `Tests run: 710, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS`
- `@Ignore` アノテーション 0 件（`Skipped: 0`。`git grep -n '@Ignore' -- src` の 4 件はいずれも
  「#35 で削除した」旨を述べる javadoc 本文で、アノテーションではない）
- `git grep -nE '\.rst|nablarch-document|解説書' -- src/` → 0 件
- `git grep -nE '[A-Za-z]+\.java:[0-9]+' -- src` → 0 件
- `git status --short` 空・`jacoco.exec` 削除済み

---

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| `git diff` の実行文の変更が C6 の `else` 節（throw 1 文）だけである | OK | §1。7 ファイル +39／−6 のうち実行文は `XlsFormatReader.java` の `throw` 1 文のみ |
| (c) 全箇所のコメント一覧（`file:line` と逐語）が記録にあり、全件が両方を述べている | OK | §2（C1〜C8 の 11 箇所。うち 3 箇所は既に両方を満たすため変更なし） |
| 再測定で残る未達が (c) の箇所（＋新設 throw）と完全一致する | OK | §3 の 30 行の対応表。分岐 8 は不変 |
| `mvn -o clean test` 全件緑（710 件基準）・`@Ignore` 0 件・`git status --short` 空 | OK | §4 |
| 解説書参照・`Foo.java:NN` 形式の記述が `src/` に 0 件 | OK | §4 |

## Overall Verdict

- Self-check: OK
- レビュア subagent: 回さない（Rules「#30 以降、レビュア subagent は回さない」／指示書 §5-4）
- Ready to check off: Yes
