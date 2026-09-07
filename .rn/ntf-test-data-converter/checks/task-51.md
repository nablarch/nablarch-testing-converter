# task-51 Completion Check

辺② —— YAML 読みをマーカーカラムだけのブロックで辺①と対称にする。

## 1. Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| マーカーカラムだけの `list_maps`・テーブル系エントリについて、中間モデルのカラム名がマーカーカラム名と一致し、行数・値が本体 `YamlTestDataParser` の読みと一致する | OK | `YamlMarkerOnlyBlockTest#keepsMarkerOnlyColumnsAndValuesInListMap`／`#keepsMarkerOnlyColumnsAndValuesInTable`。件数の正解は `YamlFrameworkOracle`（`YamlTestDataParser` 経由） |
| 実データカラムを持つエントリのマーカーカラムは従来どおりカラム名から落ちる | OK | `YamlMarkerOnlyBlockTest#dropsMarkerColumnWhenListMapHasDataColumn`／`#dropsMarkerColumnWhenTableHasDataColumn`（新規。非回帰） |
| 直した既存テストが全件列挙されている | OK | 下の §3 |
| `mvn -o clean test` 全件緑・`@Ignore` 0 件・`git status --short` 空 | OK | `Tests run: 718, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS`。`@Ignore` アノテーション 0 件 |

## 2. 変更の中身

| ファイル | 変更 |
|---|---|
| `src/main/.../yaml/YamlFormatReader.java` | `markerOnlyColumns`（カラム名がマーカーだけかの判定）と `rawRows`（原文からの行復元）を追加。`addListMapBlocks`・`addTableBlocks` がそれを使う |
| `src/test/.../yaml/YamlMarkerOnlyBlockTest.java`（新設） | 本タスクの担保 4 件（oracle 2 件・非回帰 2 件） |

### 設計上の要点

**値は YAML の原文がそのまま中間モデルの値である。** 辺①（Excel）は本体の解釈を通した値を持つが、
YAML 経路は値加工のインタープリタを 1 つも積まない（`nablarch-testing-yaml@4431cf8` の
`src/main/java/nablarch/test/core/reader/yaml/InterpreterResolver.java:54`-`:56` の `raw()` を
変換ツールの読みが使う）。したがって辺②は原文をそのまま持てばよく、辺④（`YamlFormatWriter`）は
1 行も変えずに対称になる。

**マーカーカラムは器に入らないため、値は同じ Map の原文から復元する。** 本体の YAML 読み込みは
マーカーカラムを値加工より前に読み飛ばす（`nablarch-testing-yaml@4431cf8` の
`src/main/java/nablarch/test/core/reader/yaml/YamlTableDataBuilder.java:142`・`:201`）。

**行の除去条件は本体へ委ねた。** 「値を 1 つも持たない行（空マッピング `{}`）だけを取り除く」判定は
本体の `YamlSection#dropBlankRows` をそのまま呼ぶ。変換ツール側で二重実装しない。

**テーブル系だけは器と Map エントリを突き合わせる必要がある。** 器（`TableData`）がカラムを 1 つも
持たないときだけ、同じグループの Map エントリ列（器と 1:1 同順）を同じ添字で見に行く。
**器がカラムを持つ既存の入力ではこの経路へ入らない**ため、既存の読みに新しい失敗経路を持ち込んでいない。

**カラム名は大文字化しない。** テーブル系のカラム名を大文字へ揃えるのは器（`TableData`）であり、
マーカーカラムはその器に入らない。Excel 側も同じ理由でマーカーカラム名はそのまま残るため、
往復で名前が変わらない。

## 3. 旧仕様を期待していた既存テストの変更（全件）

| # | テスト | 変更 |
|---|---|---|
| 1 | `YamlFormatReaderInvalidInputTest#readsMarkerOnlyTableAsColumnlessRows` | `#keepsMarkerOnlyTableColumnsAndValues` へ改名。期待を「カラム 0 件・値を持たない行 2 件」→「`[no]` 1 列・行 `[["1"],["2"]]`」へ。Javadoc も新仕様へ |

**#50 と違い、辺②で期待値が変わった既存テストはこの 1 件だけである。**
`YamlFormatReaderRealFileTest` の 0 件テーブル（`rows: []`）は行が無くカラム名が決まらないため、
従来どおり「カラム名 0 件・行 0 件」のままである。

## 4. 台帳の更新

| ファイル | 変更 |
|---|---|
| `coverage/issues.md` | YML-04 の「XLS-08 との関係」を 2026-08-31・#51 の内容へ更新。`nonMarkerColumns` の grep を根拠にした段落へ、`addTableBlocks` が Map エントリを見るようになったが主張は変わらない旨を追記 |
| `coverage/axis-matrix.md` | 変更なし。辺② C-08(空) は YAML の 0 件テーブル（`rows: []`）で到達するため ✅ のまま |

**§0.6 の担保テストメソッド実在確認**: 出力なし（全件実在）。

`inventory.md` のテストメソッド件数（追補）は、#54（締め）でまとめて導き直す。

## 5. 指示書の禁止事項

| 事項 | 結果 |
|---|---|
| 解説書・本体・yaml・integration を変更しない | OK |
| ソース・記録に解説書への参照を書かない | OK。`git grep -nE '\.rst\|nablarch-document\|解説書' -- src/` → 0 件、`git grep -nE '[A-Za-z]+\.java:[0-9]+' -- src` → 0 件 |
| force push・`--amend` をしない | OK |

## Overall Verdict

- Self-check: OK
- Ready to check off: Yes
