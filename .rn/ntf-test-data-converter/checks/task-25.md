# task-25 Completion Check

本ファイルは **#25 の最終状態**を記録する。到達までの経過（何巡目の指摘か、途中の再検証ログ）は残さない。
レビュー指摘とその処置は「レビュー指摘の処置」に一覧で残す。

対象は**辺④（中間モデル → YAML）**。軸D の 9 ケースは steering の #25 Steps に列挙された 9 つであり、
#25 で導出をやり直していない。タグ `D4-01`〜`D4-09` は `coverage/inventory.md` の
「辺④ YAML 表現 9 ケース」で同じ並びに対応づけてある（辺② の導出は `checks/task-24.md`）。

---

## 実装（2026-08-14）

### 追加したテストクラス

| クラス | 件数 | 担う軸 | 導出コマンド |
|---|---|---|---|
| `YamlFormatWriterScalarTest` | 16 | 軸D（記法と往復） | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatWriterScalarTest.java` |
| `YamlFormatWriterModelTest` | 17 | 軸A・軸C・軸E | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatWriterModelTest.java` |
| `YamlFormatWriterInvalidOutputTest` | 2 | 軸F（F4-01／F4-03） | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatWriterInvalidOutputTest.java` |

合計 **35 件**を追加した（`Tests run` は基準線 501 → **536**）。
`Scalar` と `Model` は `serialize` を直接アサートし、`InvalidOutputTest` は `serialize` を一度も呼ばず
`write` が書き出したファイル本文と送出された例外を見る。往復を見るものは `writer.write(...)` で実ファイルを
書き、本番配線の `new YamlFormatReader().read(...)` で読み戻す。

### 期待値を先に決めていないことの手順記録

軸D 9 ケース・軸F 2 ケース・軸A/C/E の候補入力は、すべて一時プローブ（記録後に削除）で
`serialize` ／ `write` に通し、**出力を印字させてから**アサートを書いた。期待値は実装から導出せず
すべてリテラルで置いてある（SUT の出力を期待値にする形は残っていない）。

### 順序を主張するフィクスチャの作り方

steering Rules「順序を主張するテストは、フィクスチャを最初から定義順・辞書順とずらして作る」
（#24 の教訓・ユーザー確定）に従い、`YamlFormatWriterModelTest` の 5 つのフィクスチャを
いずれも辞書順・定義順とずらして組み立てた。クラス Javadoc に 5 件を列挙し、
**新しく足すときはそこへ加える**ことを明記してある。

| 投入順 | `LC_ALL=C sort` の結果 | 判定 |
|---|---|---|
| `setup_files` → `expected_files` | `expected_files` / `setup_files` | 辞書順の逆（`DataType` 定義順とも逆） |
| `text-encoding` → `file-type` | `file-type` / `text-encoding` | 辞書順の逆 |
| `resendFlag` → `dateSent` | `dateSent` / `resendFlag` | 辞書順の逆 |
| `flag` → `date` | `date` / `flag` | 辞書順の逆 |
| `zip` → `name` | `name` / `zip` | 辞書順の逆 |

### カバレッジ（JaCoCo 実測）

```sh
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes \
  && JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec \
  && awk -F, 'NR > 1 && $3 == "YamlFormatWriter" { print "line " $9 "/" ($8 + $9) " branch " $7 "/" ($6 + $7) }' \
       target/site/jacoco/jacoco.csv
```

出力は `line 158/159 branch 89/92`。未到達 3 箇所は**いずれも軸A〜F の要素ではない**
（`write` の親ディレクトリ null ガード／`emitBlock` の `instanceof` チェーンの `else`／
`rawGroup` の「`[` で始まるが `]` で終わらない」枝）。内訳は `inventory.md` §4.1-2 の「開示」。

この数値は `YamlFormatWriter` **1 クラスぶん**であり、辺④の担当クラス全体でも #26 の対象 6 区分でもない。

---

## レビュー指摘の処置

QA／Craft／Verification の 3 レビュアから受けた指摘を、種類ごとにまとめる。
**各項目はまず一次情報（実コード・実行結果・スキーマ本体）に当たって事実を確かめ、
挙動を記録する項目は先に一時プローブで観測してからアサートを書いた。**
変異解析のために `src/main` を書き換えた場合は毎回復元し、最終状態で `git diff` が 0 行であることを確認した。

### A. 担保の穴だったもの（生存変異・いずれも閉じた）

| 確かめた事実（変異を入れて実行） | 処置 |
|---|---|
| **送信同期 4 種の `DataType` → セクションキー写像が固定されていなかった。** 既存 `YamlFormatWriterTest#serializeSendSync_allFourSectionKeys` はキーの**集合**を `assertTrue(contains)` で見るだけで、どのタイプがどのキーへ行くかを 1 つも固定していない。2 タイプの出力を入れ替える変異が生存する | 単独ブロックの出力全文を完全一致でアサートする 3 メソッドを追加（`writesExpectedRequestBodyMessagesUnderItsOwnSectionKey` ほか）。写像を入れ替える変異 15 通りすべてで死ぬことを確認した。**台帳の A-12〜A-14 は #18 以来 ✅ と誤判定されていた**（実際は 🔺）ため、§4.1／§4.1-2／§4.3／§5.1／§5.2 へ訂正を波及させた。辺③の #23 レビューで見つかった誤りと同じ形である |
| **`isPlainSafeKey` の特殊文字集合（18 文字）と制御文字ガードが、コロン・空白・空文字・先頭 `-` の 4 つしか担保されていなかった。** 集合から `#` を 1 文字外すだけで全件が通る。実害は大きく、`#` が外れるとカラム名 `#x` の行が `- #x: "v"` となって**行全体が YAML コメント化し、データ行が黙って消える** | `quotesDirectiveKeyContainingAnyYamlSpecialOrControlCharacter` を追加（18 文字＋制御文字 2 の 20 ケース）。JaCoCo で未到達だった制御文字の枝もここで閉じ、分岐が 88/92 → **89/92** になった |
| **`rowFlow` 経路だけ値のクォートを落とす変異が生存した**（フィクスチャの値が素の文字列だけだったため） | `record()` のフィクスチャを 2 フィールドの真偽値風・日付風の値へ替えた。ただし**これは「歯がある証明」ではない**（下の C を参照） |
| **`emitMap` 経路だけ値のクォートを落とす変異が生存し、この経路が開示リストから漏れていた** | `quotesBooleanAndDateLookingValuesInFwHeader` を追加し、開示にも `emitMap` 経路を加えた |

### B. 台帳・課題一覧の記述が実物と食い違っていたもの

| 確かめた事実（一次情報） | 処置 |
|---|---|
| `issues.md` YML-12 のスキーマ記述が `minItems` を一括りにしていたが、`$defs` ごとに違う（`file_data.records` は 0、`message_data.records` は 1、`record_fragment.fields` は 1、`field_def.required` は `[name, type]`） | jar 内 `nablarch/test/ntf-testdata-yaml-schema.json` を展開する再現コマンド付きの表に分け、「あるべき姿」も 1 件目のみ `records: []`、2〜4 件目は書き出し時に弾く、と振り直した |
| §4.1-2 が「3 クラスとも `serialize` を直接アサートする」と書いていたが、`YamlFormatWriterInvalidOutputTest` は `serialize` を一度も呼ばない（`grep -c serialize` → 0） | 「`serialize` を直接アサートするのは Scalar／Model の 2 クラス」へ書き改め、`InvalidOutputTest` が何を見るかを明記した |
| §4.1-2 が「残り 2 経路のうち 2 つにも埋め込み」と書いた直後に `list_maps` を第 3 の経路として挙げており、数が合わない。さらに **`list_maps` は値の書き出し経路として独立していない** —— `YamlFormatWriter#emitTable` と `#emitListMap` は**どちらも同じ引数で `emitMapRows` を呼ぶ**（差は `entry.prop("table", …)` と `entry.prop("id", …)` の 1 行だけ） | 「埋めた経路は 2 つ（レコード断片／`emitMap`）」に確定し、`list_maps` は「値の記法は `setup_tables` と同一コードで担保されており独立した穴ではない」と別立てで書き直した。出典コマンド（`grep -c "emitMapRows(sb, entry, …)"` → 2）を併記した |
| 辺④の文脈に Excel 用語「**版面**」が残っていた（同文書の用語定義では `XlsFormatWriter` が生成するシート上の配置） | 該当 2 箇所を「出力全文で」へ置換した。XLS 文脈の用法は変更していない |
| 冒頭サマリの引用ブロック内に `>` の無い素の空行があり、Markdown 上でブロック引用が 2 つに割れていた | 空行を削除した |

### C. 主張の粒度が実測を超えていたもの

| 確かめた事実 | 処置 |
|---|---|
| `record()` と `writesFileBlockWithoutRecordsKeyWhenRecordsAreEmpty` の Javadoc が「この経路でクォートを落とす変異を入れても 1 件も落ちない（生存変異）」と書いていたが、**条件を付けない変異なら既存 `YamlFormatWriterTest` の 5 件が落ちる**。実際に生存するのは新フィクスチャの 2 値だけを狙い撃ちした変異であり、それはフィクスチャに合わせて作った変異なので「その 2 値がアサートされている」以上を示さない | Javadoc 2 箇所と台帳を同じ粒度へ落とし、**「歯がある証明ではなく、軸D の 2 ケースを別経路でも観測した記録である」**と書き改めた。条件なし変異の実測値（`rowFlow` 側 `Failures: 5, Errors: 2`／`emitMap` 側 5 件）も併記した |
| `YamlSeq#header` から `key(k)` を外す変異が 536 件すべて通る（Verification が 71 件の変異のうち唯一生存させたもの）。呼び出し元 4 箇所（`"records"`／`"rows"`／`keyName` 2 箇所）に渡るのは 5 箇所ともコンパイル時定数で、いずれも plain-safe である | **担保の穴ではなく等価変異**であることを、上記 grep を出典として §4.1-2 の開示に記録した。テストは足していない。将来のレビュアが同じ検証をやり直さずに済むようにするため |
| `writesFileBlockWithoutRecordsKeyWhenRecordsAreEmpty` のディレクティブが `text-encoding: "true"` ／ `file-type: "2026-08-07"` になっており、**NTF のテストデータとして成立しない組み合わせ**だった（変異を殺すために値を替えた結果） | ディレクティブを実在する値（`UTF-8` ／ `Fixed`）へ戻し、`emitMap` 経路の真偽値風・日付風の担保は `quotesBooleanAndDateLookingValuesInFwHeader` へ分離した。`fw_header` はスキーマ上 `additionalProperties`（キーは利用者定義）なので実在しうる形になる |
| `quotesBooleanAndDateLookingValuesInFwHeader` のフィールド名 `resendFlag` → `sendDate` は**辞書順そのもの**なのに、Javadoc が「辞書順の逆である」と事実に反して書いていた。steering Rules の順序フィクスチャ規則に反する（担保の穴は開いていない —— 同じ `emitMap` 経路のキー順は `text-encoding` → `file-type` が押さえている） | `sendDate` → `dateSent` へ改名し、キーも値も辞書順の逆にした。`emitMap` のキーを辞書順ソートする変異でこのメソッドが落ちることを確認した（差し替え前は落ちなかった）。**クラス Javadoc のフィクスチャ列挙にこのメソッドを加えて 5 件にした** —— 列挙が 4 件のままだったことが見落としの一因である |

### D. テスト・コーディング作法

| 指摘 | 処置 |
|---|---|
| 20 ケースのキー検査を 1 メソッド内の `for` で回しており、最初に落ちた 1 件で止まる。リポジトリには**理由付きで明文化された「1 ケース 1 `@Test`」の規約**がある（`XlsFormatWriterInvalidOutputTest` F3-04 節・`XlsFormatWriterCellTypeTest`。理由は「ループで束ねると最初の 1 文字が落ちた時点で残りが実行されず、どの文字で挙動が違うのかが分からなくなるため」） | 20 メソッドへは割らず、**ループ内は判定だけにして不一致を集め、ループ後に 1 回だけアサートする**形にした。これで規約の理由そのものが成立しなくなる。逸脱の理由（(a) 20 ケースは `isPlainSafeKey` の同一判定 1 つを通る 1 つの振る舞いであり、Xls の禁止文字 7 件のように文字ごとに挙動が分かれるわけではない／(b) 失敗を集約したので規約の理由は当たらない）を Javadoc と §4.1-2 の両方に記録した。制御文字ガードを外す変異で「20 ケース中 2 件」と両方が報告されることを実測で確認した |
| `EXTENSION = ".yaml"` が 2 クラスに重複定義されていた | `YamlFixture.EXTENSION` へ寄せた。`YamlFixture` 自身が持っていた `".yaml"` リテラルも畳んだ。本体の `YamlFormatWriter.YAML_EXTENSION` は参照していない（テストが SUT から期待値を導かないため） |
| `writesOneYamlFilePerSectionWhenContainerHasMultipleSections` の期待値が `writer.serialize(…)` の呼び出し（SUT で期待値を作る形）だった | リテラル `oneRowTableYaml(name)` に置き換えた |

---

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 軸D の9ケースすべてで出力 YAML の記法（引用符の有無・複数行記法・NULL 表現）がアサートされている | OK | `inventory.md` §4.1-2 の軸D 表に D4-01〜D4-09 の 9 行がそろい、各行の「記法の担保」列にメソッドが入っている。新規 5 件は `YamlFormatWriterScalarTest`、既存 4 件は `YamlFormatWriterTest` の `serialize_distinguishesNullFromNullString`／`serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation`／`serialize_escapesQuotesBackslashAndControlChars`。いずれも `is(...)` の出力全文一致である。#18 が 🔺 としていた D4-01（往復のみで記法アサートが無かった）に記法の担保が付いた | OK | Verification が 9 ケースぶんの記法アサートを実物で確認 |
| 9ケースそれぞれについて、辺④で書き辺②で読んだとき元の文字列が復元されるか否かが判定・記録されている | OK | 同表の「往復」列と「復元」列に 9 行そろい、**9 ケースすべてが復元される**。往復は `writer.write(...)` → `new YamlFormatReader().read(...)` の実ファイル経路で見ている。全値ダブルクォート＋`null` だけアンクォートという方針が復元の成立理由であることも記録した。**復元されない形は別に存在し**、`issues.md` **YML-12**（スキーマが禁じる形の中間モデルを渡すと読み戻せない YAML が黙って書かれる）と **YML-13**（折り返しの起きるキーは YAML として読めなくなる）に記録した。いずれも `src/main` は修正していない | OK | Verification が往復テストの実在と入力を確認 |
| 軸F の2ケース（出力先不在／書き込み権限なし）で例外型または結果がアサートされている。`overwrite=false` 衝突は上位層で担保済みとして根拠付きで対象外にされている | OK | `YamlFormatWriterInvalidOutputTest` の **2 件**。F4-01 は例外にならず多階層の出力先が作られて YAML が書かれる結果を、F4-03 は `UncheckedIOException`（原因 `AccessDeniedException`・ファイルは作られない）をアサートする。F4-03 は権限が効かない環境で `Assume` によりスキップするが、全実行で `Skipped: 0` なので実際に走っている。F4-02 の対象外根拠は `grep -c overwrite …/YamlFormatWriter.java` → **0**、`grep -rln overwrite src/main/java` → `TestDataConverter`／`ConverterMojo`／`ConversionRequest` の **3 件のみ**で、上位層 2 テストの本文も開いて `.yaml` を出力側とする衝突であることを確認した | OK | QA・Verification が独立に grep と上位層テスト本文を確認 |
| 辺④について軸A の14種・軸B の4種・軸C の全フィールド（省略可能なものは省略時も）・軸E が埋まっている | OK | §4.3 の集計は **要追加 0 ／ 担保済み 15 ／ 到達不能 0 ／ 対象外 1 ／ 総計 16**。軸A は #18 以来 ✅ と誤判定されていた A-12〜A-14 を含めて 14 種が埋まり、新規 3 メソッドが単独ブロックの出力全文一致で写像を固定する。軸B は §4.1 の表に B-1〜B-4 が現れる。軸E は 0・1・複数が埋まっている | OK | QA が `sectionKey` の写像入れ替え変異 5 通り（前回生存 2 ＋ QA が選んだ 3）を実行し、**`RoundTripTest` を除いた writer 系だけで全滅する**ことを確認して閉じた |
| src/main への変更がゼロ | OK | `git diff f3efa1b..HEAD -- src/main pom.xml \| wc -l` → **0**（`f3efa1b` は #24 のチェックオフ＝#25 着手前）。`git status --porcelain` は空 | OK | 3 レビュアとも変異解析後の復元を確認 |
| `mvn clean test -Djacoco.skip=true` が全テスト PASS する | OK | `JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true` → `Tests run: 536, Failures: 0, Errors: 0, Skipped: 0` ／ `BUILD SUCCESS`（基準線 501 ＋ 追加 35） | OK | 3 レビュアとも同値を独立に実行 |

### 台帳の記述規約の自己点検

| 点検項目 | 結果 |
|---|---|
| `grep -cE '\b(L[0-9]{1,4})\b' .rn/ntf-test-data-converter/coverage/inventory.md` が 0 | **0**（実行して確認） |
| ファイル行数を書いていない | OK |
| 書き足した件数のすべてに導出コマンドを併記／併記したコマンドはそのまま実行して同じ結果が出る | OK。クラス別件数（16 ／ 17 ／ 2）、`emitMapRows` の 2 箇所、`.header(...)` の 4 箇所、`overwrite` の grep、スキーマ導出、JaCoCo の 3 数値を実行して一致を確認した |
| 担保の穴は、テストを足さない場合でも台帳に開示する | OK。§4.1-2 の「開示」に、軸D の測定経路（埋めた 2 経路と `list_maps` が独立でないこと）、JaCoCo 未到達 3 箇所、`YamlSeq#header` の等価変異、20 ケースのループが規約から逸脱していることを記録した |
| 逆引き表（軸要素 → 担保テストメソッド）を新設していない | 新設していない。§4.1-2 は #19〜#24 が用いた「その要素を当該タスクが埋めたことを示す差分表」と同じ形式である。逆引きの正は #27 の `coverage/axis-matrix.md`（steering Rules） |

---

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| 検証のやり方が目的に対して意味を持っているか | OK（2 巡目で是正） | 初回に完了条件 4 を **FAIL**。既存 `serializeSendSync_allFourSectionKeys` がキーの集合しか見ておらず写像を固定していないことを、変異を入れて示した。修正後は QA 自身が選んだ 3 通りを含む 5 通りの写像入れ替え変異を実行し、**`RoundTripTest` を除いた writer 系だけで全滅する**ことを確認して閉じた |
| 台帳・課題一覧の主張が一次情報と一致するか | OK | 台帳が併記した数値・コマンド（件数・JaCoCo・変異ごとの Failures 数・スキーマ導出）を**全件そのまま実行**して一致を確認。誤りとして挙がった 3 件（YML-12 の `minItems`、`serialize` を呼ぶクラス数、経路の数え方）はいずれも是正済み |

## Expert Reviews

### Craft Expert（coding）

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| 媒体別ベストプラクティス | OK | 明文化された「1 ケース 1 `@Test`」規約との衝突を指摘し、逸脱の是非を判断材料付きで提示した。最終形（失敗の集約＋逸脱理由の明記）について「規約を守ったふりで回避したのではなく、規約が守ろうとしていたものを別の手段で満たしている」と判定 |
| 既存スタイルとの一貫性 | OK | 兄弟クラス（`XlsFixture`／`XlsFormatWriterInvalidOutputTest`／`XlsFormatWriterCellTypeTest`）との差はすべて解消または理由を明記。`EXTENSION` の重複も解消した |
| フィクスチャが順序規則を守っているか | OK（3 巡目で是正） | 5 件すべてを `LC_ALL=C sort` にかけて確認。`quotesBooleanAndDateLookingValuesInFwHeader` の並びが辞書順そのものであり Javadoc の記述が事実と逆であることを見つけ、是正した |

### Verification Expert（test）

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| テストが実際に挙動を固定しているか（変異で確認） | OK（2 巡目で PASS） | 初回に生存変異 3 件（`isPlainSafeKey` から `#` を除去／`rowFlow` 経路の非クォート化／`emitMap` 経路の非クォート化）を検出。修正後、**新規設計 61 件を含む計 71 件の変異**を実行し、生存は 1 件のみ。それも `YamlSeq#header` の**入力で区別しえない等価変異**である（上の C に記録）。JaCoCo は台帳の記載値と完全一致し、未到達 3 箇所も一致した |
| エッジケースの網羅 | OK | 空（sections 0／records 0／fields 0／rows 0／空キー）・境界（80 桁折り返し・制御文字）・異常系（権限なし・出力先不在・スキーマ違反 4 形）・型変換（真偽値風・数値風・ゼロ詰め・日付風・`null` と `"null"`）がいずれも埋まっていることを確認 |

## Overall Verdict

- Self-check: OK
- QA: OK
- Design expert: N/A（構造・アプローチを新設・変更しないタスクのため spawn していない）
- Craft expert: OK
- Verification expert: OK
- Ready to check off: **YES**（2026-08-14。レビュー往復の上限 3 巡に到達し、3 巡目の指摘まで反映済み。4 巡目は回していない）
