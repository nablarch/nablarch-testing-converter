# task-52 Completion Check

通しのテスト —— oracle・往復・スキーマ検証（指示書 §2 のテスト 1〜3、および 4 の通し版）。

## 1. Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| 指示書 §2 のテスト 1・2・3 が存在して緑 | OK | 下の §2 の対応表。`MarkerOnlyBlockConversionTest` 7 件がすべて緑 |
| oracle テストが変換ツール自身の reader を正解にしていない | OK | エントリの数と並びは `FrameworkOracle.listMapViaTestDataParser`／`#setupTablesViaTestDataParser`（本体 `BasicTestDataParser`）と `YamlFrameworkOracle`（`YamlTestDataParser`）から取る。マーカーカラムの名前と値は成果物そのもの（POI で開いた `.xlsx` のセル／本体 `YamlLoader` でロードした `.yaml`）から取る |
| `mvn -o clean test` 全件緑・`@Ignore` 0 件・`git status --short` 空 | OK | `Tests run: 725, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS` |

## 2. 指示書 §2 のテストとの対応

| 指示書 | テストメソッド（`MarkerOnlyBlockConversionTest`） |
|---|---|
| 1 oracle テスト（Excel→YAML。本体 `BasicTestDataParser` と `YamlTestDataParser` でエントリ数・並びが一致） | `#keepsListMapEntryCountAndOrderThroughXlsToYaml`（`LIST_MAP`）／`#keepsTableEntryCountThroughXlsToYaml`（テーブル系） |
| 2 往復テスト（Excel→YAML→Excel ／ YAML→Excel→YAML でマーカーカラムの名前・値・行数が保たれる。実ファイル起点） | `#roundTripsMarkerOnlyListMapFromXls`／`#roundTripsMarkerOnlyListMapFromYaml` |
| 3 スキーマ検証（変換後 YAML が `ntf-testdata-yaml-schema.json` を通る） | `#convertedYamlPassesSchemaValidation`／`#convertedYamlTablePassesSchemaValidation` |
| 4 非回帰（実データカラムを持つブロックのマーカーは従来どおり消える） | `#dropsMarkerColumnThroughConversionWhenBlockHasDataColumn`（通し版）。辺ごとの単体は `XlsMarkerOnlyEntryTest`（辺①）／`YamlMarkerOnlyBlockTest#dropsMarkerColumnWhenListMapHasDataColumn`・`#dropsMarkerColumnWhenTableHasDataColumn`（辺②） |

## 3. 正解（oracle）の取り方

**フレームワークはマーカーカラムの値を見ない。** マーカーカラムは読み込み対象から除外されるため、
フレームワークから取れる正解は<b>エントリの数と並び</b>だけである（`LIST_MAP` では 4 件とも空のマップになる）。
テストショット一覧と行の順序で対応付ける用途が掛かっているのはまさにその数と並びであり、
指示書 §2-1 が求めているのもそこである。

**名前と値はフレームワークからは取れないため、成果物そのものから取る。**

| 見るもの | 口 |
|---|---|
| 変換後 `.xlsx` のカラム名の行・データ行 | POI で開いて `XlsFixture#line`（変換ツールのリーダを通さない） |
| 変換後 `.yaml` の行（キー・値・並び） | 本体 `YamlLoader#load`（`nablarch-testing-yaml` のローダ。変換ツールのリーダを通さない） |

**フィクスチャの値は定義順・辞書順とずらしてある**（`3`, `1`, `4`, `2`）。並びを主張するテストが
順序を壊す変異で落ちるようにするためである（Rules の #24 の教訓）。

## 4. 変更の中身

| ファイル | 変更 |
|---|---|
| `src/test/.../xls/MarkerOnlyBlockConversionTest.java`（新設） | 通しのテスト 7 件。`XlsFixture` が `xls` パッケージの package private であるため同パッケージに置く（`SpecialNotationRoundTripTest` と同じ理由） |

`src/main` は 1 行も変えていない。

## 5. 指示書の禁止事項

| 事項 | 結果 |
|---|---|
| 解説書・本体・yaml・integration を変更しない | OK |
| ソース・記録に解説書への参照を書かない | OK。`git grep -nE '\.rst\|nablarch-document\|解説書' -- src/` → 0 件、`git grep -nE '[A-Za-z]+\.java:[0-9]+' -- src` → 0 件 |
| force push・`--amend` をしない | OK |

## Overall Verdict

- Self-check: OK
- Ready to check off: Yes
