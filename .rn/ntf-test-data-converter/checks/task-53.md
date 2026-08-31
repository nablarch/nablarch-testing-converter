# task-53 Completion Check

カラム名 0 件で行を持つ形を中間モデルの生成時に拒否し、辺④の `{}` 分岐を外す。

## 1. Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| カラム名 0 件・行 1 件以上のブロックが生成時に拒否され、その理由がメッセージに書かれている | OK | `ColumnRowDataBlock` のコンストラクタ。メッセージは「カラム名を 1 件も持たないブロックはデータ行を持てません（…YAML ではカラム名が rows: の先頭要素のキーで決まるため、カラム名が無い行は書けません）。識別子=[…] 行数=…」。担保は `TableDataBlockTest#カラムなしで行を持つブロックは生成できない`／`#カラムなしでセルを持つ行を抱えるブロックは生成できない` |
| `YamlFormatWriter` にカラム 0 件の行を書く経路が無い | OK | `emitMapRows` の `columns.isEmpty()` 分岐を削除。走査結果は下の §3 |
| カラム名 0 件・行 0 件（YAML の 0 件テーブル）は従来どおり作れる | OK | `TableDataBlockTest#カラムなし行なしのブロックは生成できる`／`YamlFormatWriterTest#serialize_emptyColumnsAndRows_emitsEmptyFlowList`／`YamlFormatReaderRealFileTest#readsEmptyColumnNamesAndRowsFromTableWithoutRows` ほか |
| `mvn -o clean test` 全件緑・`@Ignore` 0 件・`git status --short` 空 | OK | `Tests run: 726, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS` |

## 2. なぜ今この形を閉じられるか

**#50・#51 の前は、この形をどちらの読みも作っていた。** カラム名の行がマーカーカラムだけのブロックが
マーカーの除外を受けると「カラム名 0 件・セルを 1 つも持たない行が n 件」になっていた
（`issues.md` XLS-08 ／ YML-04）。**2 辺がマーカーカラムの名前と値を保つようになり、この形は
どちらの読みからも作られなくなった。**

- 辺①: カラム名の行は記法上省略できず、空行でもありえない（本体が読み込みの入口で空行を落とす）。
  マーカーを除いて 0 件になった場合はマーカーカラム名がそのまま `columnNames` に入るため、
  行を持つブロックのカラム名が 0 件になることはない。
- 辺②: カラム名は `rows:` の先頭の非空行のキーで決まる。非空とは値を 1 つ以上持つことなので、
  行があればキーが 1 つ以上ある。マーカーだけならそのキーがそのまま入る。

**残るのは「カラム名 0 件・行 0 件」だけである**（YAML の 0 件テーブル `rows: []`）。この形は拒否しない。

**番人は書き出し側でなく中間モデルの生成時に置く**（Rules の Decisions）。書き出し側で弾くと、
不正な中間モデルを作れてしまう状態が残る。

## 3. 呼び出し側の全走査

```sh
git grep -n "emitMapRows" -- src/main
```

```
src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java:162:        emitMapRows(sb, entry, block.getColumnNames(), block.getRows());
src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java:175:        emitMapRows(sb, entry, block.getColumnNames(), block.getRows());
src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java:275:    private void emitMapRows(...)
```

呼び出しは 2 か所とも `ColumnRowDataBlock`（`TableDataBlock` ／ `ListMapBlock`）の
`getColumnNames()` ／ `getRows()` をそのまま渡している。**したがって新しい番人が両方を覆う。**

## 4. 変更の中身

| ファイル | 変更 |
|---|---|
| `src/main/.../model/ColumnRowDataBlock.java` | 番人を「カラム名 0 件では行を 1 件も持てない」へ広げ、Javadoc を実態へ合わせた |
| `src/main/.../yaml/YamlFormatWriter.java` | `emitMapRows` のカラム 0 件分岐（`- {}` を書く経路）を削除し、なぜここへ届かないかを Javadoc に書いた |
| `src/main/.../xls/XlsFormatWriter.java` | `layoutColumnRow` の Javadoc —— 「マーカーカラムは除外されるため読み戻すと 0 件に戻る」を実態（書いたマーカーカラムがそのまま戻る）へ改めた。**コードは変えていない** |

## 5. 旧不変条件を固定していた既存テストの変更（全件）

| # | テスト | 変更 |
|---|---|---|
| 1 | `TableDataBlockTest#カラムなしでセルを持たない行は保持する` | `#カラムなしで行を持つブロックは生成できない` へ差し替え（保持 → 拒否）。Javadoc も新不変条件へ |
| 2 | `TableDataBlockTest#カラムなしでセルを持つ行を抱えるブロックは生成できない` | 期待するメッセージを新しい文言へ |
| 3 | `TableDataBlockTest#カラムなし行なしのブロックは生成できる` | **新設**。拒否しない側（YAML の 0 件テーブル）を明示的に固定する |
| 4 | `YamlFormatWriterTest#serialize_emptyColumnsRow_emitsEmptyFlowMap` | **削除**。`- {}` を書く形が作れなくなったため |
| 5 | `YamlFormatWriterTest#serialize_emptyColumnsAndRows_emitsEmptyFlowList` | **新設**。辺④が扱うカラム名 0 件はこの形（行 0 件 → `rows: []`）だけであることを固定する |

## 6. 台帳の更新

| ファイル | 変更 |
|---|---|
| `coverage/issues.md` | XLS-21 の「拒否するのは『セルを 1 つ以上持つ行』だけである」を【2026-08-31・#53】で置き換え、拒否範囲を広げた理由と担保テストを書いた |
| `coverage/axis-matrix.md` | 辺④ C-08(空) の担保を `#serialize_emptyColumnsAndRows_emitsEmptyFlowList` へ差し替え。辺④ E-2(1件) から削除テストを外す。§0.4 の不変条件行を「行を 1 件も持てない」へ。§7 の穴 ④ を解消として取り消し線＋理由、柱書を「8 件のうち 1 件解消・残り 7 件」へ |
| `coverage/inventory.md` | §4 系のテストメソッド表 19 行目を差し替え後のメソッドへ |

**§0.6 の導出コマンドを流し直した結果**（状態別の件数は #50 から変わっていない）:

```
辺1 ✅=69 —=9 ／ 辺2 ✅=73 —=8 ／ 辺3 ✅=71 —=5 ／ 辺4 ✅=70 —=6
③ 🔺往復欄が「該当なし」でない行数: 95
担保テストメソッドの実在確認: 出力なし（全件実在）
```

## 7. 指示書の禁止事項

| 事項 | 結果 |
|---|---|
| 解説書・本体・yaml・integration を変更しない | OK |
| ソース・記録に解説書への参照を書かない | OK。`git grep -nE '\.rst\|nablarch-document\|解説書' -- src/` → 0 件、`git grep -nE '[A-Za-z]+\.java:[0-9]+' -- src` → 0 件 |
| force push・`--amend` をしない | OK |

## Overall Verdict

- Self-check: OK
- Ready to check off: Yes
