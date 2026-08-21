# #28 Evaluation sign-off — 自己点検

**実行日**: 2026-08-21
**対象**: Acceptance criteria のうち、`becd7b3` 時点で「検証不能」と記録した 1 件
（「全移動ファイルが本体現ブランチと package/import を除いて完全一致（実装無改変）」）の照合をやり直した記録。

---

## 1. 前回（`67a8780`）の記録は誤りであった

`67a8780` は照合の相手に `nablarch-testing` の **`d5ec1d0`**（`worktree-agent-a79308e7e5862d004`）を取り、
「一致 20・差分 5・上流に不在 3」「同ブランチの 50 コミットを全部当たっても完全一致するコミットは無い」と記録した。
**これは照合相手の取り違えであり、事実ではない。**

`d5ec1d0` は converter を作り直した別系統である。自分で実行して確かめた根拠は次の 2 つ。

```
$ git -C ~/work/nablarch/nablarch-testing ls-tree -r --name-only d5ec1d0 | grep -c '^src/main/java/nablarch/test/tool/converter'
25
$ git -C ~/work/nablarch/nablarch-testing ls-tree -r --name-only d5ec1d0 | grep -E 'FormatHandler'
(出力なし)
```

移動元は `FormatHandler` ／ `XlsFormatHandler` ／ `YamlFormatHandler` を持つ 28 ファイル版である。

```
$ git -C ~/work/nablarch/nablarch-testing ls-tree -r --name-only 1035207 | grep -E 'FormatHandler'
src/main/java/nablarch/test/tool/converter/FormatHandler.java
src/main/java/nablarch/test/tool/converter/XlsFormatHandler.java
src/main/java/nablarch/test/tool/converter/YamlFormatHandler.java
```

`d5ec1d0` はこの 3 ファイルを持たないため、そもそも移動元ではない。

---

## 2. 移動元は現存する（reflog 経由でのみ到達可能）

**移動元 SHA**: `103520709cf6ddeec6da7f901f2b4a5aacbffdef`（短縮 `1035207`）

| 項目 | 値 | 出典（そのまま実行できる） |
|---|---|---|
| ref | `refs/heads/convert-testdata-excel-to-text@{70}` | `git -C ~/work/nablarch/nablarch-testing reflog refs/heads/convert-testdata-excel-to-text` |
| コミット日時 | `2026-06-23 17:14:02 +0900` | `git -C ~/work/nablarch/nablarch-testing log -1 --format='%H%n%ci%n%s' 1035207` |
| 件名 | `docs(steering): reconcile — State クリア・本体後始末タスク開始` | 同上 |
| converter ファイル数 | 28 | `git -C … ls-tree -r --name-only 1035207 \| grep -c '^src/main/java/nablarch/test/tool/converter'` |
| 現 HEAD からの到達 | **不可** | `git -C … merge-base --is-ancestor 1035207 HEAD` → 非 0（`UNREACHABLE`） |

**ブランチは失われていない。同じブランチの履歴が作り直されただけである。** reflog の該当区間（実測）:

```
6aa6989 …@{68}: reset: moving to origin/develop
d5bd33f …@{69}: commit: refactor: 本体をdevelopへ戻し、キャッシュTemplate Method集約の最小改変のみ再適用
1035207 …@{70}: commit: docs(steering): reconcile — State クリア・本体後始末タスク開始
```

`@{69}` と `@{68}` により、`1035207` は現 HEAD（`06a73f8`・ブランチ `convert-testdata-excel-to-text`）から到達できなくなった。

---

## 3. 照合結果 — 28 件 / NG 0 件

**照合の射程**: 移動元 `1035207`（本体） × 移動時点 `2a069bc`（converter の初回取り込み。
`feat: complete task #2 — copy src/main converter (28 files)`・`2026-06-23 08:34:15 +0900`）。
`package` 行・`import` 行を除いた内容の sha1 を全ファイルで突き合わせる。

**実行したスクリプト（読み取りのみ。本体には一切書き込んでいない）**:

```sh
UP="$HOME/work/nablarch/nablarch-testing"
CONV="$HOME/work/nablarch/nablarch-testing-converter"
SRC=1035207   # 本体側 移動元（reflog 経由でのみ到達可能）
DST=2a069bc   # 変換ツール側 初回取り込み
PREFIX=src/main/java/nablarch/test/tool/converter
ng=0
for f in $(git -C "$CONV" ls-tree -r --name-only $DST | grep "^$PREFIX"); do
  a=$(git -C "$UP"   show "$SRC:$f" 2>/dev/null | grep -v -E '^[[:space:]]*(package|import) ' | sha1sum)
  b=$(git -C "$CONV" show "$DST:$f" 2>/dev/null | grep -v -E '^[[:space:]]*(package|import) ' | sha1sum)
  [ "$a" != "$b" ] && { echo "NG $f"; ng=$((ng+1)); }
done
echo "対象 $(git -C "$CONV" ls-tree -r --name-only $DST | grep -c "^$PREFIX") 件 / NG $ng 件"
```

**出力（2026-08-21 実測）**:

```
対象 28 件 / NG 0 件
```

**ファイル名の集合も両方向で一致する**（上のスクリプトは `DST` 側のファイルしか回らないため、別に確かめた）:

```sh
diff <(git -C "$UP"   ls-tree -r --name-only 1035207 | grep "^$PREFIX" | sort) \
     <(git -C "$CONV" ls-tree -r --name-only 2a069bc | grep "^$PREFIX" | sort)
```

→ 差分なし（両側 28 件）。

**結論: 「全移動ファイルが移動元と package/import を除いて完全一致（実装無改変）」は充足である。**

---

## 4. この照合が言っていないこと（射程の明示）

- 照合したのは**移動時点**（`2a069bc`）である。`2a069bc` 以降の `src/main` の変更は本照合の対象外である
  （`git log --oneline 2a069bc..HEAD -- src/main` → 79 コミット。#5 の Adapter 追加・#11〜#16 のプラグイン化・
  #17 の Javadoc 参照除去・#25.5 の不具合修正・#26.5 の `[EMPTY]` 化などが含まれる）。
  Acceptance criteria が挙げる除外（#25.5・#26.5）はいずれもこの区間にある。
- 本体側の `src/test`（形式間変換テスト 21 件）は本照合の対象外である。射程は `src/main` の converter 28 件のみ。

---

## 5. 移動元の退避はしない（ユーザー確定・2026-08-21）

**救出用のタグ・ブランチは作らない。** 照合は本ファイルに再現用スクリプトごと記録済みであり、
**再照合が必要になる場面は想定しない。** 未決事項として持ち越さない。

前提として、`1035207` は reflog からのみ到達可能である。`nablarch-testing` の設定は下記のとおりいずれも
未設定＝既定値であり、**到達不能コミットの reflog 保持は既定 30 日**、当該コミットは `2026-06-23` 作成で
約 2 か月が経過している。**`git gc` が走れば `1035207` は消える。** それを承知のうえでの判断である。

```
gc.reflogExpireUnreachable = (未設定＝既定 30 days)
gc.reflogExpire            = (未設定＝既定 90 days)
gc.auto                    = (未設定＝既定 6700)
```

本体の作業ツリーは `git status --short` が無出力＝クリーンであり、書き込みは行っていない
（タグ・ブランチも作っていない）。
