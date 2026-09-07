# task-54 Completion Check

完了条件の締め —— 変異確認・カバレッジ・報告。

## 1. Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| テスト 1・2 の変異確認がコマンドと結果つきで記録されている | OK | `checks/step4-54-report.md` ② の変異表（M1・M2・M3。実行コマンドを併記） |
| 既存テストの変更が全件列挙されている | OK | 同報告 ③（期待値変更 8・削除 1・Javadoc のみ 4・追加 22） |
| カバレッジの未達に本是正が持ち込んだものが 0 件であることが示されている | OK | 同報告 ④。未達 30 行／8 分岐は #49 で承認済みの (c) と 1 行ずつ対応 |
| `mvn clean test` 全件緑・`@Ignore` 0 件・`git status --short` 空・push 済み | OK | `Tests run: 731, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS` |
| `git grep -rn "nablarch-document\|\.rst" src/` が 0 件 | OK | 出力なし。`git grep -nE '[A-Za-z]+\.java:[0-9]+' -- src` も 0 件 |

## 2. 本タスクで足した実装の変更

**#50〜#53 の時点では、本是正が 6 行・5 分岐の未達を持ち込んでいた。** 基準（未達 0）に戻すため、
本タスクで (a)(b) の分類と処置を行った。詳細は報告 ④ の「途中で 6 行・5 分岐の未達を出し、#54 で潰した経緯」。

| 箇所 | 分類 | 処置 |
|---|---|---|
| `TestCoreReaderAdapter#readMarkerOnlyBlock` のブロック不在・行の埋め・カラム名 0 件 | (a) | テスト 5 件を追加（アダプタ単体 3・実 `.xlsx` 経由 2） |
| `YamlFormatReader#addTableBlocks` の `t < entries.size()` | (b) | 削除し、既存の `requireSameSize` に置き換え |

**あわせて 2 件の挙動の穴を塞いだ。** `rowCount` を外したままだと、カラム名の行がマーカーカラムでもないのに
1 件も残らないブロック（`null` 記法だけのカラム行／グループ付き `LIST_MAP`）で中間モデルの番人に当たり、
変換が例外で止まっていた（実測）。`rowCount` をその 2 形にだけ効く形で残して解決した。

## 3. 台帳の更新

| ファイル | 変更 |
|---|---|
| `coverage/inventory.md` | 追補その 21 を追加（テストメソッド 710 → 731、ファイル別増減、削除 1 件・改名 4 件、軸要素の判定が変わった 1 か所、本是正完了時の JaCoCo） |

**導出コマンド（inventory.md §0.1-2 の ①〜③ と同じ。③ の比較対象だけ `8290d56` に置き換え）:**

```
① 731
② （0 件）
③ 8290d56: 710
   HEAD: 731
```

## 4. 指示書の禁止事項

| 事項 | 結果 |
|---|---|
| 解説書・本体・yaml・integration を変更しない | OK。他リポジトリは読み取りだけ |
| ソース・記録に解説書への参照を書かない | OK（上の grep 2 本が 0 件） |
| force push・`--amend` をしない | OK |

## Overall Verdict

- Self-check: OK
- Ready to check off: Yes
