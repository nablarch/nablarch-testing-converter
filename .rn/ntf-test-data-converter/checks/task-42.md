# #42 Completion Check

指示書 第2回 2-3（`nablarch-document@a12fb67`）。
参照点（ピン）: 解説書 `nablarch-document@a6da1f6` ／ `nablarch-testing@3c4bd2a` ／ `nablarch-testing-yaml@3fecc4e`。

**レビュア subagent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| 警告が 1 件出る（ブック名・シート名・データタイプ・グループ ID・読まれなかったブロックの識別子を含む） | OK | `XlsInterleavedBlockTest` 2 件が 5 要素すべてを assert |
| converter が自前で選別していない（本体の `break` に任せている） | OK | 足したのは `warnInterleavedBlocks`（検出と警告のみ）。ブロックの取り出し経路（`read` の分岐・`processed`）は触っていない |
| 先に落ちるテストを書き、(i) で落ちることを確認した | OK | 下記「(i) で落ちること」 |

## ゲート（Rules「#30 以降」）

- `git diff --stat 1915207..HEAD -- src/`（実測値はコミット後の `git diff --stat` を正とする）:

  ```
  src/main/java/nablarch/test/tool/converter/xls/XlsFormatReader.java          | +100
  src/test/java/nablarch/test/core/reader/YamlFrameworkOracle.java             | 新規
  src/test/java/nablarch/test/tool/converter/xls/XlsInterleavedBlockTest.java  | 新規
  ```

- `mvn -o clean test` の最終行:

  ```
  Tests run: 675, Failures: 3, Errors: 1, Skipped: 0
  BUILD FAILURE
  ```

  **緑ではない。** 残る赤 4 件はすべて #43（2-4。yaml 第2回への追随）が直す対象で、#41 完了時から変わっていない。
  実行件数 675 には、追跡していない測定用の一時テスト 9 件を含む。追跡対象は 666 件。

## (i) で落ちること（指示書 2-3 の 1）

実装（`warnInterleavedBlocks`）を入れる前に 3 件を書いて実行した。

```
Tests run: 3, Failures: 2, Errors: 0, Skipped: 0
  XlsInterleavedBlockTest.warnsAndDropsBlockAfterInterleavedDataType:269 警告の件数
    Expected: is <1>  but: was <0>
  XlsInterleavedBlockTest.warnsAndDropsBlockAfterInterleavedGroupId:221 警告の件数
    Expected: is <1>  but: was <0>
```

**落ちたのは (i)（警告の件数）だけで、(ii)・(iii) は実装前から通っている。**
読まれなかったブロックを出力から外しているのがフレームワーク本体の打ち切りであって
変換ツールではない、ということがこの実行から読める（着手前調査の (ii) 成立と一致）。

3 件目（`doesNotWarnForDataTypesFetchedByIdentifier`）は負のテストで、実装前も後も通る。

## 実装

`XlsFormatReader#read` の先頭で `warnInterleavedBlocks(headers, resourceName)` を呼ぶ。

- 対象は**収集方式が「グループ」のデータタイプ**だけ（`isGroupCollected` ＝ テーブル系・ファイル系・送信同期系）。
  識別子で 1 件を引く `LIST_MAP` ／ `MESSAGE` は対象外である。
- 検出は `readHeaders` が返す**記述順のヘッダ列**だけで行う。あるキー（データタイプ ＋ グループ ID）が
  最初に現れた位置より後ろで、最初に別のキーが現れた位置を打ち切り点とし、それより後ろに同じキーが
  再び現れていれば、その識別子を「読まれないブロック」として挙げる（`unreadIdentifiersAfter`）。
- キーごとに 1 件の警告を出す。同じキーを 2 度警告しないよう `warned` で抑える。
- **出力から外す処理は書いていない。** フレームワークが打ち切った結果、読まれなかったブロックは
  そもそも器に入ってこない。

警告の文面（5 要素をすべて含む）:

```
[<ブック名>] シート "<シート名>" では、データタイプ "<データタイプ名>"・グループID "<グループID>" の
データブロックの間に別のデータブロックが挟まっています。テスティングフレームワークは Excel 形式では
後ろのデータブロックを読まないため、次のデータブロックを出力しません: [<識別子>, ...]
```

## (iii) の突き合わせ方

出力（YAML）をフレームワークの YAML 読み込み（`nablarch-testing-yaml` の `YamlTestDataParser`）に読ませ、
元の `.xlsx` をフレームワーク本体（`PoiXlsReader` ＋ インタープリタ 3 本）に読ませた結果と比べる。
どちらも変換ツールのリーダを通していない。YAML 側の口はテスト専用の `YamlFrameworkOracle` に置いた
（#44 でも使う）。期待値の取得には**加工しない読み口** `getSetupTableData` を使う
（`getExpectedTableData` はデフォルト値の補完と期待値のマージを行うため使わない）。

## 完了条件4（期待値をわざと崩すと落ちること）

足した 3 件それぞれについて確認した（計 5 件の変異）。

| # | テスト | 崩した内容 | 結果 |
|---|---|---|---|
| 1 | `#warnsAndDropsBlockAfterInterleavedGroupId` | (i) 警告の件数 1 → 2 | Failures: 1 |
| 2 | `#warnsAndDropsBlockAfterInterleavedGroupId` | (ii) 出力の期待に `/C` を足す | Failures: 1 |
| 3 | `#warnsAndDropsBlockAfterInterleavedGroupId` | (iii) 突き合わせ相手をグループ `""` → `[g1]` へ | Failures: 1 |
| 4 | `#warnsAndDropsBlockAfterInterleavedDataType` | (i) 文言の期待 `EXPECTED_TABLE` → `ZZZ` | Failures: 1 |
| 5 | `#doesNotWarnForDataTypesFetchedByIdentifier` | 警告の件数 0 → 1 | Failures: 1 |

## 台帳の更新

- `coverage/inventory.md` §0.1-2 に**追補その 15**（663 → 666）
- 同 §4.6 に `xls/XlsInterleavedBlockTest` の行と、テスト専用クラス `YamlFrameworkOracle` の行
