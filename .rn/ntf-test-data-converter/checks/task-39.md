# #39 Completion Check

指示書「4. 完了条件」の 7〜10 と「6. 報告」（`nablarch-document@a16be0a` の
`.rn/20260724-ntf-yaml-support/ntf-step4-05-nablarch-testing-converter.md`）。

**レビュア subagent は回していない**（Rules「#30 以降、レビュア subagent は回さない」）。

## Completion Criteria

| Criterion | Self-check | Evidence |
|---|---|---|
| `mvn -o clean test` が緑（`@Ignore` を除く） | OK | `Tests run: 656, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS`。**`@Ignore` は 0 件**（#35 で 2 件削除して以降、Step 4 では 1 件も置いていない）ので「除く」対象そのものが無い |
| カバレッジ C0/C1 の計測結果が報告にあり、下がった箇所が挙がっている | OK | `checks/step4-report.md` §6-3（全体・6 区分の対比）と §6-4（未到達分岐が増えた 4 箇所と、その到達可能性の判断） |
| 報告の 6 項がすべて埋まっている | OK | `checks/step4-report.md` の §1〜§6（§0 は着手前の実測）。冒頭の「第1節のみ記入済み」も現状へ直した |
| `git status --short` が空 | OK | 本コミット後に無出力。`jacoco.exec` は計測後に削除した（`.gitignore:3` にもあるが残さない） |
| push 済み | OK | 本コミットを `origin/ntf-test-data-converter` へ push |

## ゲート（Rules「#30 以降」）

- `git diff --stat b5f5063..作業ツリー -- src/`: **`src/` は無変更**（#39 は報告と台帳だけのタスクである）。

- `mvn -o clean test` の最終行:

  ```
  Tests run: 656, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS
  ```

## カバレッジの計測について

**`coverage/coverage-report.md` は書き換えていない。**同書の全数値は `da66425` の 1 回の実行
（`jacoco.csv` md5 `d28e374e9027ade63d7919f7a7b5826e`）に固定したままである。
`steering.md` Rules「JaCoCo の再計測はしない」の趣旨は**同書が引く行番号を自己無効化させないこと**であり、
指示書の完了条件 8 は Step 4 時点の C0/C1 を求めている。この 2 つは両立するので、
**新しい計測は `checks/step4-report.md` §6-3 の中だけに置いた**（`checks/` は時点の証拠記録である）。

実行したコマンド（`b5f5063`・ワーキングツリーはクリーン）:

```sh
rm -f jacoco.exec
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec
md5sum target/site/jacoco/jacoco.csv   # → 825ff458b5ebff0103030bc6f12bb07f
```

結果: 行 **1632/1706 ＝ 95.66%**（C0）／分岐 **761/818 ＝ 93.03%**（C1）。
区分別の対比と、下がった箇所（未到達分岐が増えた 4 箇所）は報告 §6-3・§6-4。

## 報告に載せた「調整側の判断を仰ぐもの」

**マージ可否の判断は出さない**（Rules・ユーザー確定 2026-08-24）。次の 5 件を報告へ開示した。

| # | 内容 | 置き場所 |
|---|---|---|
| 1 | 指示書との食い違い 3 件（記載例のレコード件数 3→2／期待値 44→62／YML-04 は一部だけ解消） | 報告 §2 |
| 2 | 観測できる出力の変化 2 件（`TABLE[]=x` ／ 角括弧の無いグループ ID のブロックが消える） | 報告 §2 |
| 3 | 付随して解消した課題 3 件（XLS-05 の一部・XLS-39・COV-13）。**XLS-39 を申し送りの束に残すかは調整側の判断** | 報告 §2 |
| 4 | 母集合に含めなかった記載例 1 件（「アップロードファイルを指定する」） | 報告 §3 |
| 5 | 残課題として開示した未到達分岐 2 件（`isQuotationWrapped` の全角側／`markerGroupId` の角括弧が閉じていない側）。**足すかどうかは調整側の判断** | 報告 §6-4 |

## Overall Verdict

- Self-check: OK
- QA: N/A（Rules「#30 以降、レビュア subagent は回さない」）
- Design expert: N/A（同上）
- Craft expert: N/A（同上）
- Verification expert: N/A（同上）
- Ready to check off: Yes
