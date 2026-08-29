# 既存テスト 4 辺分の軸棚卸し（task #18）

4 つの変換辺の既存テスト 126 件が、軸A〜F のどの要素を担保しているかを 1 件ずつ棚卸しした結果。
以降のタスク #19〜#25 は、本書「未担保一覧」に挙がった要素だけを埋める。
本書は #26（カバレッジ計測と未到達分岐の列挙）の入力であり、#27 で
`.rn/ntf-test-data-converter/coverage/axis-matrix.md`（各要素に担保テストメソッド名を記した 4 辺ぶんの
軸×要素対応表）へ発展させる土台でもある。

- 作成日: 2026-08-12
- 対象コミット: `c1d2d21`（棚卸し実施時 HEAD）
- 出典 `testdata_notation.rst`／`notation:nnn` の行番号: `nablarch-document` の `30a8271`（2026-08-18 08:54:15 +0900）時点。本書の引用は全件この基準にそろえてある
- 判定方法: 全テストメソッドのテスト本文を読み、実際にアサートしている対象のみを「担保」とした。
  推測で埋めていない。アサートが間接的・副次的なものは 🔺 で区別した。

> **#19〜#23 による更新について（最終更新 2026-08-13）**
>
> 本書は #18 時点の棚卸し結果である。その後の各タスクが実ファイルを経路に含むテストを追加し、
> **辺①（§1）と辺③（§3）の未担保状況が変わった**。本書では #18 時点の記述を消さずに
> 次の箇所へ反映してある。
>
> **辺①（#19 辺① 軸D／#20 辺① 軸A・B・C／#21 辺① 軸E・軸F。2026-08-12）**
>
> - **§1.2-2（新設）** — #19／#20／#21 が追加したテストクラス（`XlsFormatReaderCellTypeTest` /
>   `XlsReferenceFixtureTest` / `XlsFormatReaderRealFileTest` / `XlsFormatReaderInvalidInputTest`）の
>   担保を軸要素別に記す。
>   §1.1 は「`XlsFormatReaderTest` 33 件」を対象とした #18 時点の事実として**そのまま残す**。
> - **§1.3** — 未担保一覧を #19／#20／#21 の実測結果に合わせて更新した。分類を変更した行には
>   根拠（`coverage/issues.md` の課題 ID）を併記してある。
>
> **辺③（#22 辺③ 軸D・軸F。2026-08-13）**
>
> - **§3.1-2（新設）** — #22 が追加したテストクラス（`XlsFormatWriterCellTypeTest` /
>   `XlsFormatWriterInvalidOutputTest`）の担保を軸要素別に記す。
>   §3.1 は「`XlsFormatWriterTest` 40 件」を対象とした #18 時点の事実として**そのまま残す**。
> - **§3.3** — 辺③ 未担保一覧を #22 の実測結果に合わせて更新した。
>
> **辺③（#23 辺③ 軸A・B・C・E の欠け補充。2026-08-13）**
>
> - **§3.1-3（新設）** — #23 が追加したテストクラス（`XlsFormatWriterModelTest`）の担保を軸要素別に記す。
>   §3.1（`XlsFormatWriterTest` 40 件）と §3.1-2（#22 の 2 クラス）は**そのまま残す**。
>   `XlsFormatWriter` の JaCoCo 実測（#23 時点の未到達 3 箇所。#25.5 でいったん 4 箇所になり、
>   レビュー 1 巡目の B-2 で 3 箇所に戻した）も本節末尾に置いた。
> - **§3.3** — 辺③ 未担保一覧に「#23 後の状態」列を足し、**要追加 0 ／ 担保済み 29 ／ 対象外 1**
>   （総計 30）に更新した。#23 のレビュー対応で送信同期 3 件（A-12〜A-14）を足した分を含む。
> - **§0.8-6 / §0.8-7 / §5.2 / §5.3** — 辺③の C-02「sections 空」と A-01 `DEFAULT` が
>   #23 で担保済みになったことを追記した（§5.3 の未解決 3 は解決済みへ移した）。
>
> **辺③（#23 レビュー ラウンド2 対応。2026-08-13。テストの担保内容は変えていない）**
>
> - **§0.8-4** — `cell` / `line` の定義位置が `XlsFixture` へ移ったことを書き足した
>   （挙動が `getStringCellValue()` 固定である点は #18 から変わっていない）。
> - **§3.3** — **辺③では軸B が軸A から独立していない**ことを開示した（判定 ✅ 4/4 と件数 0 は変えない）。
>
> **辺②（#24 辺② 軸D・軸F・軸A/B/C/E。2026-08-14）**
>
> - **§2.1-2（新設）** — #24 が追加したテストクラス（`YamlFormatReaderScalarTest` /
>   `YamlFormatReaderInvalidInputTest` / `YamlFormatReaderRealFileTest`。いずれも実 `.yaml` を入力とする）の
>   担保を軸要素別に記す。
>   §2.1 は「`YamlFormatReaderTest` 20 件」を対象とした #18 時点の事実として**そのまま残す**。
> - **§2.3** — 辺② 未担保一覧に「#24 後の状態」列を足した。
> - **§0.5** — 軸D 辺②を 10 ケース → **12 ケース**（D2-01〜D2-12。ユーザー確定・2026-08-14）へ改めた。
> - **§0.8-3 / §5.2** — 辺②の軸D 件数と、軸A の辺②列（実 `.yaml` 経路でも ✅ になったこと）を追記した。
>
> **辺④（#25 辺④ 軸D・軸A〜F の欠け補充。2026-08-14）**
>
> - **§4.1-2（新設）** — #25 が追加したテストクラス（`YamlFormatWriterScalarTest` /
>   `YamlFormatWriterModelTest` / `YamlFormatWriterInvalidOutputTest`）の担保を軸要素別に記す。
>   §4.1 は「`YamlFormatWriterTest` 33 件」を対象とした #18 時点の事実として**そのまま残す**。
>   `YamlFormatWriter` の JaCoCo 実測も本節末尾に置いた。
> - **§4.3** — 辺④ 未担保一覧に「#25 後の状態」列を足した。
> - **§4.1 末尾 / §4.1-2 / §4.3 / §5.1 / §5.2**（#25 レビュー対応・2026-08-14） — 送信同期 3 種
>   （A-12〜A-14）の辺④が #18 以来 ✅ と誤判定されていた（実際は 🔺）ことを訂正し、
>   辺④の軸A 件数を 2 → **5**、担保済みを 12 → **15**、総計を 13 → **16** に改めた。
>   辺③の #23 レビューでの訂正（§3.3）と同じ形である。
> - **§4.1-2**（#25 レビュー修正ラウンド 2・2026-08-14） — 軸要素の判定は変えていない。
>   (a) 軸D の測定経路の数え方を「埋めたのは 2 経路」に確定し、`list_maps` を独立した未観測経路として
>   並べるのをやめた（`emitListMap` は `emitTable` と同じ `emitMapRows` を呼ぶため値の記法は同一コード）。
>   (b) 「歯がある証明」と読める書き方を、実測の粒度（狙い撃ち変異が死ぬこと）まで落とした。
>   (c) 20 ケースのキー検査が「1 ケース 1 `@Test`」の規約から逸脱していることと、その理由を記録した。
>   (d) `YamlSeq#header` の生存変異が等価変異であることを記録した。
>   (e) 辺④の文脈に残っていた Excel 用語「版面」を置き換えた。
>   テストは 1 件増えて `YamlFormatWriterModelTest` 17 件・全体 **536** 件になった。
>
> **#25.5（不具合修正・TDD。2026-08-14）**
>
> - **§0.1-2（新設）** — #25.5 後のテストメソッド件数（**540**。不具合修正そのものは総数を変えておらず、
>   #25 完了時点の 536 との差 4 件はレビュー指摘で足した担保である）・`@Ignore` 2 件・
>   `Tests run: 540, Failures: 0, Errors: 0, Skipped: 2`・`src/main` に手を入れたファイルと課題 ID を、
>   そのまま実行できる導出コマンドつきで記す。
> - **§1.2-2 / §1.3** — 辺① C-16「`recordType` 省略（`null`）」を**到達不能 → 担保済み**へ移した
>   （`issues.md` **XLS-06** の修正で `null` が入るようになったため）。§1.3 の合計は
>   **要追加 0 ／ 担保済み 25 ／ 到達不能 7**（総計 32）になった。
> - **§0.8-4** — `XlsFormatReaderCellTypeTest` の `@Test` 件数を **19 → 10** へ訂正した
>   （#25.5 の修正で減ったのではなく、2026-08-13 の絞り込み時に取り残していた誤りである）。
> - **§1.3 / §2.3 / §3.3 / §4.3** — 4 節すべての合計に、**そのまま実行すれば同じ数になる導出コマンド**を
>   併記した（順に 32 ／ 28 ／ 30 ／ 16）。§1.3 の検算ブロックは表と食い違っていた（41 と 32 の併存）ため直した。
> - **§0.1-2 / §3.1-3 / §4.1-2 の JaCoCo** — 修正で分岐・行の総数が変わったため取り直した。
>   **数値はレビュー 1 巡目（2026-08-14）に取り直したもので、B-2 の修正を含む。**
>   `XlsFormatWriter` は `line 157/158 branch 101/104`（未到達 **3** 箇所。XLS-16 の修正でいったん
>   4 箇所へ増えた `requireValidSheetNameLength` の `sheetName == null` 側は、レビュー B-2 で
>   null を同じ番人が落とすようにし担保テストを足したため閉じた）、`YamlFormatWriter` は
>   `line 158/159 branch 89/92` → **`line 160/161 branch 91/94`**（YML-12 で足した分岐は到達済み。
>   未到達 3 箇所は変わらず）、`YamlFormatReader` は `line 200/200 branch 106/106`。
>   `DirectiveUtil` の `line 20/20 branch 17/18`（未到達 1 分岐）は §0.1-2 に開示した。
>   **`DirectiveUtil` は新規クラスではない**（#25.5 が足したのは `normalizeSeparator` 1 メソッド）。
>   YML-02 で新設した `YamlTestCoreAdapter#defaultGroupOnlyYaml` の未到達分岐も §0.1-2 に開示した。
> - **テストメソッド名の追随** — #25.5 で改名・書き直したテスト（辺① C-16 ／ 辺③ F3-04 の 4 件 ／
>   辺④ C-12 ／ 辺② 送信系 `group_id` 省略 ／ YML-03 の待機テスト 2 件）を、本書の該当行すべてで
>   現在の名前へ直し、旧名を併記した。**軸要素の判定は C-16 以外は変えていない。**
>
> **#25.5 追補（XLS-22 ／ YML-12 3形目 の修正・TDD。2026-08-18）**
>
> `RecordLayout.fields` が空の値は **Excel 記法にも YAML スキーマにも存在しない**——それを中間モデルだけが
> 保持できるのは**契約の穴**である、という判断（ユーザー確定・2026-08-18）に基づき、
> `RecordLayout` の Javadoc に「`fields` は 1 件以上」の契約を明記し、辺③ `XlsFormatWriter` と
> 辺④ `YamlFormatWriter` に `IllegalArgumentException` の番人を置いた（コミット `b9ff38e`。
> 判定と根拠は `issues.md` **XLS-22** ／ **YML-12 3形目**）。本書には次の箇所へ反映した。
>
> - **§0.4** — C-17 の「空許容」が**型定義上の話**であり契約は 1 件以上であることを、表の下に追補した
>   （表そのものは #18 時点の実定義の読み取りとして残す）。
> - **§0.1-2** — テストメソッド件数（540 → **541**。削除 3 件・追加 4 件）・`src/main` の変更ファイル・
>   JaCoCo を 2026-08-18 実測で追補した。**未到達は 4 クラスとも増えていない。**
> - **§3.1-3 / §3.3** — 辺③ C-17 の担保テストが
>   `XlsFormatWriterModelTest#writesRecordWithoutFieldColumnsWhenFieldsAreEmpty`（削除）から
>   `XlsFormatWriterTest#rejectsRecordWithoutFieldsInFileBlock` ／ `#rejectsRecordWithoutFieldsInMessageBlock`
>   へ移ったことを反映した。読み戻し検査 `#failsToReadBackRecordWithoutFields` は検査対象の版面が
>   書き出されなくなったため削除し、§3.1-3 の「末尾 3 件」は **2 件**・同クラスの件数は 18 → **16** になった。
>   **軸要素の判定 ✅ と §3.3 の件数 9 は変えていない。**
> - **§4.1 末尾 / §4.1-2** — 辺④の同じ変更（#20 の
>   `serialize_recordWithEmptyFieldsAndRows_emitsEmptyFlowLists` → `#serialize_recordWithEmptyRows_emitsEmptyFlowList`
>   への書き直しで C-17(空) の担保が外れたこと、`YamlFormatWriterModelTest` 17 → **16** 件）を注記した。
>   C-17(空) の担保は `YamlFormatWriterTest` の番人テスト 2 件へ移った。
>
> **YML-03 追補（yaml 側の修正取り込み。2026-08-18）**
>
> `record_type: FW_HEADER` のレコードが黙って捨てられる問題（**YML-03**）は帰属が
> `nablarch-testing-yaml` 側にあり、#25.5 では仕様どおりの期待値を書いた `@Ignore` の待機テスト 2 件を
> 置いて待っていた。yaml 側が `0b53910`（ブランチ `feature/ntf-yaml`）で `skipFwHeader` の特別扱いを
> 廃止したため、本リポジトリ側も `YamlFormatReader#recordsWithoutFwHeader` を廃止して
> メッセージ系・送信系の呼び出しをファイル系と同じ `#records(entry)` に揃えた
> （判定と根拠は `issues.md` **YML-03**）。本書には次の箇所へ反映した。
>
> - **§0.1-2** — 待機テスト（`@Ignore`）が **2 件 → 0 件**になったこと、`mvn test` の
>   `Skipped: 2 → 0`（`Tests run` は 541 のまま）、`YamlFormatReader` の JaCoCo 実測を追補した。
> - **§2.1（辺② の #18 表・11 行目）** — 挙動を固定していた
>   `YamlFormatReaderTest#readMessage_mapsRawFwHeaderAndExcludesFwHeaderRecord` を
>   `#readMessage_mapsRawFwHeaderAndKeepsFwHeaderNamedRecord` へ書き直した（2 件とも残ることが期待値）。
>   **担保する軸要素は変えていない。**
> - **§2.1-2 / §2.3 まわりの注記** — 「`@Ignore` の待機テストなので現状挙動を固定していない」旨の
>   記述を、現在の事実（アクティブなテストで固定している）へ直した。
>
> **テストメソッド総数は変わっていない**（改名のみ。541 件）。

> **#25.5 追補（YML-12 4形目 の修正・TDD。2026-08-18）**
>
> `FieldDef.type` が `null` の値は **Excel 記法にも YAML スキーマにも存在しない**——それを中間モデルだけが
> 保持できるのは**契約の穴**である、という判断（ユーザー確定・2026-08-18。XLS-22 ／ YML-12 3形目 と同じ）に
> 基づき、`FieldDef` の Javadoc に「`type` は必須（`null` 不可）」の契約を明記し、辺③ `XlsFormatWriter` と
> 辺④ `YamlFormatWriter` に `IllegalArgumentException` の番人を置いた
> （判定と根拠は `issues.md` **YML-12 4形目**）。弾くのは `null` だけで、空文字は弾かない。
> 本書には次の箇所へ反映した。
>
> - **§0.4** — C-20 の「省略可」が**型定義上の話**であり契約は必須（`null` 不可）であることを、
>   表の下に追補した（表そのものは #18 時点の実定義の読み取りとして残す）。
> - **§0.1-2** — テストメソッド件数（541 → **545**）・`src/main` の変更ファイル・`mvn clean test` の
>   結果行を 2026-08-18 実測で追補した。
> - **§3.1（辺③ の #18 表・30 行目）／§4.1（辺④ の #18 表・22 行目）** — C-20(省略) を担保していた
>   2 件の扱いが変わったことを、各表の下に注記した。

> **#25.5 追補（YML-12 2形目 の修正・TDD。2026-08-18）**
>
> `MessageDataBlock.records` が空（0 件）の電文は **Excel 記法にも YAML スキーマにも存在しない**——
> それを中間モデルだけが保持できるのは**契約の穴**である、という判断（ユーザー確定・2026-08-18。
> XLS-22 ／ YML-12 3形目・4形目 と同じ）に基づき、`MessageDataBlock` の Javadoc に
> 「`records` は 1 件以上（0 件不可）」の契約を明記し、辺③ `XlsFormatWriter#layoutMessage` と
> 辺④ `YamlFormatWriter#emitMessage` に `IllegalArgumentException` の番人を置いた
> （判定と根拠は `issues.md` **YML-12 2形目**）。
> **番人は共通の `appendRecords` ／ `emitRecords` には置かない**——ファイルデータブロックの
> レコード 0 件は 0 バイト空ファイルを表す**合法な形**だからである（C-12 は従来どおり ✅）。
> 本書には次の箇所へ反映した。
>
> - **§0.4** — C-15 の「空許容」が**型定義上の話**であり契約は 1 件以上であることを、表の下に追補した
>   （表そのものは #18 時点の実定義の読み取りとして残す）。
> - **§0.1-2** — テストメソッド件数（545 → **547**）・`src/main` の変更ファイル・JaCoCo・
>   `mvn clean test` の結果行を 2026-08-18 実測で追補した。
> - **§3.1-3（辺③ の軸C 表・軸E 表）／§4.1-2（辺④ の #18 表・8 行目ほか）／§5.2（辺③ の C 行）** —
>   C-15(空) を担保していたテストの扱いが変わったことを、各表の下に注記した。

> **#25.5 追補（§1-B〜§1-G・XLS-28・§6-A〜§6-H・YML-14・XLS-22 の番人移設。2026-08-19）**
>
> #25.5 の残りステップをまとめて走らせた。反映先は **§0.1-2 の 1 か所に集約した**。
>
> - **§0.1-2** — テストメソッド件数（547 → **598**。削除 19 件・追加 70 件）・ファイル別の増減表・
>   削除した 19 件の起点コミット・`src/main` に手を入れた 17 ファイル・JaCoCo を 2026-08-19 実測で追補した。
>   **未到達は `YamlFormatWriter` が 行 1・分岐 1 増えた**（`sectionKey` の `default` が §1-G で到達不能になった。
>   §0.1-2 に開示）。ほかの 5 クラスは変わっていない。
> - **§0.1-2 の「削除した 19 件を参照している既存記述の現在地」** — §1〜§4 のスナップショット表に残る
>   削除済みテスト名を、現在どこが担保しているかの対応表として一括で示した。
>   **表そのものは書き換えていない。軸要素の判定も変えていない。**
> - **§3.1-3** — `XlsFormatWriterModelTest` の件数を 15 → **11** へ導き直し、「issues 検査 3 件」が
>   0 件になったことを追補した。
> - **§3.1（#30 `writesOmittedMetaAndFieldAsEmpty` の追補）** — C-20 の `null` を弾く担保が
>   `XlsFormatWriterTest` から `FieldDefTest` へ移ったことを追補した。
>
> **上記以外（§5.1）は #18 時点のままである**（§5.1 の未担保件数も §1.3／§2.3／§3.3／§4.3 の更新を
> 反映していない。4 辺を同じ基準で比べるため、あえて #18 基準を保っている）。
> **§5.2 だけは §1.2-2 の #20 実績・§2.1-2 の #24 実績・§4.1-2 の #25 実績を反映した現時点ビューである。**
>
> **§0（前提の実測）は原則として #18 時点のスナップショットである。** その後の変化は
> 各項の中に日付つきで追記してある（§0.8-4 の `getCellType()` 件数など）。

### 判定基準

- **軸A**: 「その `DataType` のブロックが生成されることをアサートしている」ことを担保とし、
  `getDataType()` の直接アサートとは区別する。`XlsFormatReaderTest` で `getDataType()` を参照するのは
  一部のテストだけだが、他のテストもデータタイプ名を含むマーカー行を入力に与え、
  そこから生成されたブロックの型・内容をアサートしている。よって軸A の ✅ は 10/14 になる。
  なお軸C の C-05（`TestDataBlock.dataType`）は `getDataType()` を直接アサートしているテストにのみ ✅ を
  付けており、軸A の ✅ とは別基準である（例: `readMapsTableBlockPreservingRawValues` は軸A A-02 は ✅ だが
  軸C の C-05 は付けていない）。
- **軸C**: 省略可能フィールドは「値あり」「省略」、空許容コレクションは「非空」「空」の双方を別々に評価する。
- **軸D・F**: 辺ごとに要素の定義が異なる（§0.5 / §0.7）。
- **軸E**: 観点 × 多重度（0 件／1 件／複数件）の組ごとに評価する。

## 凡例

| 印 | 意味 |
|---|---|
| ✅ | 担保あり（その軸要素を直接アサートしている） |
| 🔺 | 弱い担保（間接的・副次的にしか通っていない。理由は同じセルに併記する） |
| ❌ | 未担保 |
| ※ | 補足注（軸要素 ID を伴わない、そのテスト固有の観点のメモ） |
| — | 該当なし（そのテストはその軸を通さない／その要素に担保テストが 1 件もない） |
| n/a | その要素に「省略」「空」という状態が存在しない（必須スカラー・2 値の列挙型など） |
| **太字** | その辺でその要素を通す唯一の担保 |

用語:

- **器**: `YamlFormatReader` が `TestCoreReaderAdapter` から受け取る中間モデルの骨格。YAML の原文（生 Map）と
  対で保持し、両者の件数・構造が一致することを前提に組み立てる。
- **版面**: `XlsFormatWriter` が生成する Excel シート上の行・列の配置。値そのものではなく、
  どの行に何が出るか（行の有無・位置）をアサートしているものを指す。

内部参照は `§0.4` / `§0.8-5` のように節番号で書く。

## 軸の一覧

| 軸 | 正式名 | 要素の定義 |
|---|---|---|
| A | データタイプ | `DataType` 14 種（§0.2） |
| B | ブロック実装 | `TestDataBlock` sealed 階層の具象 4 種（§0.3） |
| C | 中間モデル全フィールド | 中間モデル 8 クラスの全 21 フィールド（§0.4） |
| D | 値の表現 | 辺ごとに定義が異なる（§0.5） |
| E | 多重度 | 4 観点 × 0 件／1 件／複数件（§0.6） |
| F | 異常系 | 辺ごとに定義が異なる（§0.7） |

## 目次

- [0. 前提の実測](#s0)
- [1. 辺① Excel→中間モデル（`XlsFormatReaderTest` 33 件）](#s1) — [1.3 辺① 未担保一覧](#s1-3)
- [2. 辺② YAML→中間モデル（`YamlFormatReaderTest` 20 件）](#s2) — [2.3 辺② 未担保一覧](#s2-3)
- [3. 辺③ 中間モデル→Excel（`XlsFormatWriterTest` 40 件）](#s3) — [3.3 辺③ 未担保一覧](#s3-3)
- [4. 辺④ 中間モデル→YAML（`YamlFormatWriterTest` 33 件）](#s4) — [4.3 辺④ 未担保一覧](#s4-3)
- [5. 全体サマリ](#s5) — [5.1 未担保件数（辺 × 軸）](#s5-1) / [5.3 コーディネータに判断を仰ぎたい点](#s5-3)

---

<a id="s0"></a>

## 0. 前提の実測

### 0.1 テストメソッド件数（実測と steering 想定の突き合わせ）

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
for f in src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderTest.java \
         src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderTest.java \
         src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterTest.java \
         src/test/java/nablarch/test/tool/converter/yaml/YamlFormatWriterTest.java \
         src/test/java/nablarch/test/tool/converter/RoundTripTest.java; do
  printf "%-30s %s @Test\n" "$(basename $f)" "$(grep -c '@Test' $f)"
done
```

```
XlsFormatReaderTest.java       33 @Test
YamlFormatReaderTest.java      21 @Test
XlsFormatWriterTest.java       45 @Test
YamlFormatWriterTest.java      31 @Test
RoundTripTest.java             30 @Test
```

**上のブロックは 2026-08-19 の実測であり、上のコマンドをそのまま実行すれば再現する。**

> **出力を導き直した（2026-08-19）。** 従来ここには `33 / 20 / 40 / 33 / 30`（合計 126）と、
> 「`@Test` の数は 5 クラスとも #18 から変わっていない」が書いてあった。**#25.5 の §1・§6 で
> 4 クラスが増減したため、いまはコマンドを実行しても再現しない。** `8c327d0`（#25.5 開始時点）で
> 同じコマンドを実行すると `33 / 20 / 40 / 33 / 30` が返り、旧出力はその時点の値だったことが確かめられる
> （`git grep -c '@Test' 8c327d0 -- <上の 5 ファイル>`）。**「steering 想定」の列は #18 当時の想定であり、
> 突き合わせの記録として動かさない。** 全体の件数は §0.1-2 の最新の追補（2026-08-21 時点では追補その 8）が正である。

`RoundTripTest`（30 件）は 4 辺いずれの担当クラスのテストでもないため下の 130 件には含まれないが、
4 辺すべてを実ファイル経由で駆動するため §0.8-8 で扱う。

| 辺 | クラス | steering 想定（#18 当時） | 実測（2026-08-19） | 差異 |
|---|---|---|---|---|
| 辺① Excel→中間モデル | `XlsFormatReaderTest` | 33 | **33** | なし |
| 辺② YAML→中間モデル | `YamlFormatReaderTest` | 20 | **21** | **+1**（#25.5） |
| 辺③ 中間モデル→Excel | `XlsFormatWriterTest` | 40 | **45** | **+5**（#25.5） |
| 辺④ 中間モデル→YAML | `YamlFormatWriterTest` | 33 | **31** | **−2**（#25.5） |
| 合計 | | 126 | **130** | **+4** |

<a id="s0-1-2"></a>

### 0.1-2 #25.5（不具合修正）後の件数（2026-08-14 実測）

**#25.5 の不具合修正そのものはテストメソッドの総数を変えていない。** 現状挙動を固定していたテストを
仕様どおりの期待値へ書き直す（＝同じメソッドの中身を入れ替える／改名する）修正だったためである。
総数が 536 から 540 へ増えた 4 件は、レビュー指摘を受けて**後から足した担保**であり、
不具合修正の副産物ではない（`XlsFormatWriterInvalidOutputTest#rejectsNullSheetName` 1 件と
`YamlTestCoreAdapterTest` のデフォルトグループ直接テスト 3 件）。
下の 3 つのコマンドの出力は **記録日（2026-08-14）時点の実測**であり、
**現在の実行結果とは一致しない**（その後もテストが増減しているため。
現在の値は §0.1-2 の最新の追補にある）。

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
# ① テストメソッド総数
grep -rc '^    @Test' src/test --include=*.java | awk -F: '{s+=$2} END {print s}'
# ② 待機テスト（@Ignore）の件数と場所
grep -rn '^    @Ignore' src/test --include=*.java
# ③ #25.5 の起点（8c327d0）と現在との比較
for c in 8c327d0 HEAD; do
  printf "%s: %s\n" "$c" "$(git grep -c '^    @Test' $c -- 'src/test/**/*.java' | awk -F: '{s+=$NF} END {print s}')"
done
```

```
① 540
② src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderRealFileTest.java:638:    @Ignore("YML-03: yaml側の修正待ち")
   src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderRealFileTest.java:1001:    @Ignore("YML-03: yaml側の修正待ち")
③ 8c327d0: 536
   HEAD: 540
```

**記録日（2026-08-14）時点**の `@Ignore` 2 件は `keepsFwHeaderNamedRecordInSendSyncFromRealYaml` と
`keepsFwHeaderNamedRecordInMessageFromRealYaml` で、いずれも **YML-03**（本体側 yaml の修正待ち）の
**待機テスト**だった。仕様どおりの期待値を書いてあるので、本体側が直った日に `@Ignore` を外せば通る。
**この 2 件は YML-03 の解消（§0.1-2 の追補その 2）で無くなっており、現在の `@Ignore` 2 件は
別のもの（YML-14・XLS-40）である。現在の内訳は `coverage/coverage-report.md` §0 が正である。**

> **上の ② の中に見える `…java:638` ／ `…java:1001` は `grep -rn` の出力であって、
> 台帳が主張している行番号ではない。** この文書の ② はすべて `grep -rn` の出力である
> （**長い行は折り返し・省略してある**）。**行番号が見えるのは本節と追補その 5 以降の各追補**で、
> いずれも記録した日の実測である（**追補その 1 には ② の行そのものが無く**、追補その 2〜その 4 の
> ② は「（ヒット 0 件）」であり行番号を 1 つも持たない）。追補が増えれば見える箇所も 1 つずつ増えるため、
> **箇所数は書かない**（数えるなら下の確認コマンドを実行すること）。
> **書き直すと「記録した日にそのまま実行して得た出力である」という記録の性質が壊れる**ため、
> 出力は当日のまま凍結し、
> 本文側の識別はクラス名・メソッド名で行う（`steering.md` Rules「台帳に他ファイルの行番号を書かない」）。
> **台帳の本文に他ファイルの行番号は 1 つも無い**（確認コマンドは §0.1-2 系の追補すべてを含む全文に対して
> `grep -nE '\.java:[0-9]+|\bL[0-9]{2,4}\b' .rn/ntf-test-data-converter/coverage/inventory.md` を実行し、
> ヒットが `grep -rn '^    @Ignore' src/test --include=*.java` の出力ブロック内だけであることを見る。
> **`L` に続けて行番号を書く形式**の 1 件は 3 巡目レビューで見つかって落とした。`coverage-report.md` §6-3）。

ビルド全体の実測（`JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean test -Djacoco.skip=true`）:

```
Tests run: 540, Failures: 0, Errors: 0, Skipped: 2
```

`Skipped: 2` は上の `@Ignore` 2 件である。#25 完了時点（8c327d0）は
`Tests run: 536, Failures: 0, Errors: 0, Skipped: 0` で、差は `@Ignore` 2 件と
レビュー指摘で足した担保 4 件である。

**#25.5 で `src/main` に手を入れたファイル**（`git diff --stat 8c327d0 HEAD -- src/main` で確認できる）:

| ファイル | 課題 | 変更の要点 |
|---|---|---|
| `src/main/java/nablarch/test/core/reader/YamlTestCoreAdapter.java` | **YML-02** | `group_id` を省略した送信同期エントリをデフォルトグループとして読む |
| `src/main/java/nablarch/test/tool/converter/yaml/YamlFormatReader.java` | **YML-02**／**YML-08** | 同上＋ディレクティブ値を辺①と同じ逆正規化に通す |
| `src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java` | **YML-12** | レコードが空のファイルブロックへ `records: []` を出力する |
| `src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java` | **XLS-16** | シート名 31 文字超を切り詰めず `IllegalArgumentException` で落とす |
| `src/main/java/nablarch/test/tool/converter/xls/XlsFormatReader.java` | **XLS-06**／**YML-08** | レコード種別の空セルを `null` にする＋逆正規化を `DirectiveUtil` へ切り出す |
| `src/main/java/nablarch/test/tool/converter/DirectiveUtil.java` | **YML-08** | 辺①・辺②が共有するディレクティブ値の逆正規化（`normalizeSeparator` を追加。**クラス自体は #25.5 以前から在る**） |

**追補（2026-08-18 実測。XLS-22／YML-12 3形目 の修正ぶん）**

上の数値は 2026-08-14 時点のものである。その後 XLS-22 ／ YML-12 3形目 を修正した
（コミット `b9ff38e`）ため、現在の値は次のとおりである。**導出コマンドは上の ①〜③ と同じ。**

```
① 541
③ 8c327d0: 536
   HEAD: 541
```

```
Tests run: 541, Failures: 0, Errors: 0, Skipped: 2
```

540 → 541 の内訳は **削除 3 件・追加 4 件**である。削除したのは現状挙動を固定していた
`XlsFormatWriterModelTest#writesRecordWithoutFieldColumnsWhenFieldsAreEmpty` ／
`#failsToReadBackRecordWithoutFields` ／ `YamlFormatWriterModelTest#failsToReadBackRecordWithoutFields`
の 3 件（修正で「書き出せてしまう」版面そのものが無くなったため、期待値を書き直す先が無い）。
追加したのは番人の担保 4 件（`XlsFormatWriterTest#rejectsRecordWithoutFieldsInFileBlock` ／
`#rejectsRecordWithoutFieldsInMessageBlock` ／ `YamlFormatWriterTest#serialize_recordWithoutFieldsInFileBlock_rejected` ／
`#serialize_recordWithoutFieldsInMessageBlock_rejected`）である。
なお `YamlFormatWriterTest#serialize_recordWithEmptyFieldsAndRows_emitsEmptyFlowLists` は
`#serialize_recordWithEmptyRows_emitsEmptyFlowList` へ書き直した（C-18「`rows` 空」の担保は
フィールド 1 件の入力で維持し、C-17「`fields` 空」の担保だけを落とした）ので、この 1 件は増減に入らない。

**`src/main` に手を入れたファイル**（上の表への追加ぶん）:

| ファイル | 課題 | 変更の要点 |
|---|---|---|
| `src/main/java/nablarch/test/tool/converter/model/RecordLayout.java` | **XLS-22**／**YML-12** | Javadoc に「`fields` は 1 件以上」の契約を明記（検査は書き出し側） |
| `src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java` | **XLS-22** | `fields` が空のレコードレイアウトを `IllegalArgumentException` で落とす |
| `src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java` | **YML-12 3形目** | 同上 |

**JaCoCo 実測（2026-08-18。導出コマンドは §3.1-3 ／ §4.1-2 に載せたものと同じで `$3` を変えるだけ）**

| クラス | 2026-08-14 | 2026-08-18 | 未到達の増減 |
|---|---|---|---|
| `XlsFormatWriter` | `line 157/158 branch 101/104` | **`line 159/160 branch 103/106`** | 変化なし（行 1・分岐 3。番人の 2 分岐は両側とも到達済み） |
| `YamlFormatWriter` | `line 160/161 branch 91/94` | **`line 163/164 branch 93/96`** | 変化なし（行 1・分岐 3。同上） |
| `YamlFormatReader` | `line 200/200 branch 106/106` | **同左** | 変化なし |
| `DirectiveUtil` | `line 20/20 branch 17/18` | **同左** | 変化なし（下の開示のとおり） |

**追補その 2（2026-08-18 実測。YML-03 の解消ぶん）**

yaml 側の `0b53910` を `mvn clean install` で `~/.m2` へ取り込み（jar のタイムスタンプが
2026-08-13 17:04 → 2026-08-18 09:30 に変わることで確認できる）、converter 側の
`YamlFormatReader#recordsWithoutFwHeader` を廃止した。**導出コマンドは上の ①〜③ と同じ。**

```
① 541
② （ヒット 0 件。待機テストは無くなった）
③ 8c327d0: 536
   HEAD: 541
```

```
Tests run: 541, Failures: 0, Errors: 0, Skipped: 0
```

**総数は変わらない**（`@Ignore` を 2 件外し、挙動を固定していた
`YamlFormatReaderTest#readMessage_mapsRawFwHeaderAndExcludesFwHeaderRecord` を
`#readMessage_mapsRawFwHeaderAndKeepsFwHeaderNamedRecord` へ改名・書き直しただけで、
テストメソッドの増減はないため）。`Skipped` が 2 → 0 になった点だけが動いている。

**`src/main` に手を入れたファイル**（上の 2 つの表への追加ぶん）:

| ファイル | 課題 | 変更の要点 |
|---|---|---|
| `src/main/java/nablarch/test/tool/converter/yaml/YamlFormatReader.java` | **YML-03** | メッセージ系・送信系専用だった `#recordsWithoutFwHeader` を廃止し、ファイル系と同じ `#records(entry)` に揃えた（yaml 側で `YamlSection.FW_HEADER_RECORD_TYPE` が消えたため、コンパイルを通すためにも必須） |

**JaCoCo 実測（2026-08-18。導出コマンドは下の開示に載せたものと同じで `$3` を変えるだけ）**

| クラス | 追補その 1（同日） | 追補その 2（同日・現在） | 未到達の増減 |
|---|---|---|---|
| `YamlFormatReader` | `line 200/200 branch 106/106`（※ 下記） | **`line 192/192 branch 102/102`** | 変化なし（0 件のまま） |
| `XlsFormatWriter` | `line 159/160 branch 103/106` | 同左 | 変化なし |
| `YamlFormatWriter` | `line 163/164 branch 93/96` | 同左 | 変化なし |
| `DirectiveUtil` | `line 20/20 branch 17/18` | 同左 | 変化なし |

※ 追補その 1 の表では `YamlFormatReader` を `branch 106/106` と記したが、これは
`recordsWithoutFwHeader`（分岐 4 件・行 8 件。すべて到達済み）を含む値である。同メソッドの廃止で
行 8 件・分岐 4 件が丸ごと消え、**未到達は 0 件のまま**である。

**追補（2026-08-18 実測。YML-12 4形目 の修正ぶん）**

`FieldDef.type` の契約（必須・`null` 不可）を辺③④の番人で担保するようにしたため、件数が動いた。
**導出コマンドは上の ①〜③ と同じ。**

```
① 545
② （ヒット 0 件。待機テストは無い）
③ 8c327d0: 536
   HEAD: 545
```

```
Tests run: 545, Failures: 0, Errors: 0, Skipped: 0
```

541 → 545 の内訳は **削除 2 件・追加 6 件**である。削除したのは現状挙動を固定していた
`YamlFormatWriterTest#serialize_fieldWithNullType_omitsType`（`{name: "c1"}` を書くことを固定）と
`YamlFormatWriterModelTest#failsToReadBackFieldWithoutType`（書けて読み戻せないことを固定）の 2 件
（修正で「書き出せてしまう」出力そのものが無くなったため、期待値を書き直す先が無い）。
追加したのは番人の担保 4 件（`XlsFormatWriterTest#rejectsFieldWithoutTypeInFileBlock` ／
`#rejectsFieldWithoutTypeInMessageBlock` ／ `YamlFormatWriterTest#serialize_fieldWithNullTypeInFileBlock_rejected` ／
`#serialize_fieldWithNullTypeInMessageBlock_rejected`）、境界（空文字は弾かない）の担保 1 件
（`YamlFormatWriterTest#serialize_fieldWithEmptyType_emitsEmptyType`）、
`FieldDefTest` の分割 1 件（`型と長さの省略をnullで保持する` を `長さの省略をnullで保持する` と
`契約違反のnull型もモデル自身は検査せず保持する` の 2 件に分けた）である。
なお `XlsFormatWriterTest#writesOmittedMetaAndFieldAsEmpty` は入力の型を `null` から空文字へ変えた
（辺③の境界の担保を兼ねる）ので、この 1 件は増減に入らない。

**`src/main` に手を入れたファイル**（上の 3 つの表への追加ぶん）:

| ファイル | 課題 | 変更の要点 |
|---|---|---|
| `src/main/java/nablarch/test/tool/converter/model/FieldDef.java` | **YML-12 4形目** | Javadoc に「`type` は必須（`null` 不可）」の契約を明記（検査は書き出し側） |
| `src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java` | **YML-12 4形目** | `type` が `null` のフィールド定義を `IllegalArgumentException` で落とす |
| `src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java` | **YML-12 4形目** | 同上。あわせて `fieldFlow` は `type` を常に出力する（`null` は番人が弾くため） |

**JaCoCo 実測（2026-08-18。導出コマンドは下の開示に載せたものと同じで `$3` を変えるだけ）**

| クラス | 追補その 2 | 追補その 3（現在） | 未到達の増減 |
|---|---|---|---|
| `XlsFormatWriter` | `line 159/160 branch 103/106` | **`line 164/165 branch 107/110`** | 変化なし（未到達 行 1・分岐 3 のまま） |
| `YamlFormatWriter` | `line 163/164 branch 93/96` | **`line 168/169 branch 95/98`** | 変化なし（未到達 行 1・分岐 3 のまま） |
| `YamlFormatReader` | `line 192/192 branch 102/102` | 同左 | 変化なし（0 件のまま） |
| `DirectiveUtil` | `line 20/20 branch 17/18` | 同左 | 変化なし |

**追補（2026-08-18 実測。YML-12 2形目 の修正ぶん）**

`MessageDataBlock.records` の契約（1 件以上・0 件不可）を辺③④の番人で担保するようにしたため、
件数が動いた。**導出コマンドは上の ①〜③ と同じ。**

```
① 547
② （ヒット 0 件。待機テストは無い）
③ 8c327d0: 536
   HEAD: 547
```

```
Tests run: 547, Failures: 0, Errors: 0, Skipped: 0
```

545 → 547 の内訳は **削除 3 件・追加 5 件**である。削除したのは現状挙動を固定していた次の 3 件
（修正で「書き出せてしまう」出力・版面そのものが無くなったため、期待値を書き直す先が無い）。

| 削除したテスト | 何を固定していたか |
|---|---|
| `YamlFormatWriterTest#serializeMessage_emptyBody_emitsIdOnly` | 辺④が `id:` だけの電文を書くこと |
| `YamlFormatWriterModelTest#failsToReadBackMessageBlockWithoutRecords` | それが書けて読み戻せないこと（「緑の嘘」の最後の 1 本） |
| `XlsFormatWriterModelTest#writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty` | 辺③が識別行＋メタ行だけの電文ブロックを書くこと |

追加したのは番人の担保 4 件（`XlsFormatWriterTest#rejectsMessageBlockWithoutRecords` ／
`#rejectsSendSyncMessageBlockWithoutRecords` ／ `YamlFormatWriterTest#serializeMessage_withoutRecords_rejected` ／
`#serializeSendSync_withoutRecords_rejected`）と、`MessageDataBlockTest` の分割 1 件
（`FWヘッダ非使用経路は空Mapで表しディレクティブとレコードも空で保持する` を
`FWヘッダ非使用経路は空Mapで表しディレクティブも空で保持する` と
`契約違反のレコード0件もモデル自身は検査せず保持する` の 2 件に分けた）である。
なお `YamlFormatWriterTest#serialize_quotesKeyContainingSpecialChars` ／ `#serialize_emptyKey_isQuoted` ／
`#serialize_keyStartingWithIndicator_isQuoted` ／ `YamlFormatWriterModelTest#quotesBooleanAndDateLookingValuesInFwHeader`
の 4 件は、**都合上レコード 0 件の `MessageDataBlock` をフィクスチャに使っていた**ため、
テストの意図（キー・値のクォート）を変えずに本文レコード 1 件を足しただけである（増減に入らない）。

**境界（ファイルブロックの 0 件は合法）の担保**は
`XlsFormatWriterModelTest#writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty`（辺③）と
`YamlFormatWriterModelTest#writesEmptyRecordsListForFileBlockWithoutRecords`（辺④）である。

**`src/main` に手を入れたファイル**（上の 4 つの表への追加ぶん）:

| ファイル | 課題 | 変更の要点 |
|---|---|---|
| `src/main/java/nablarch/test/tool/converter/model/MessageDataBlock.java` | **YML-12 2形目** | Javadoc に「`records` は 1 件以上（0 件不可）」の契約と出典を明記（検査は書き出し側） |
| `src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java` | **YML-12 2形目** | `layoutMessage` がレコード 0 件の電文を `IllegalArgumentException` で落とす |
| `src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java` | **YML-12 2形目** | `emitMessage` が同上。あわせて `emitRecords` の `emitEmptyList` 引数を削った（メッセージ系が空で入ることが無くなり、`false` 側が到達不能になったため。空なら常に `records: []` を書く） |

**JaCoCo 実測（2026-08-18。導出コマンドは下の開示に載せたものと同じで `$3` を変えるだけ）**

| クラス | 追補その 3 | 追補その 4（現在） | 未到達の増減 |
|---|---|---|---|
| `XlsFormatWriter` | `line 164/165 branch 107/110` | **`line 167/168 branch 109/112`** | 変化なし（未到達 行 1・分岐 3 のまま） |
| `YamlFormatWriter` | `line 168/169 branch 95/98` | **`line 170/171 branch 95/98`** | 変化なし（未到達 行 1・分岐 3 のまま。番人で 2 分岐増え、`emitEmptyList` の削除で 2 分岐減った） |
| `YamlFormatReader` | `line 192/192 branch 102/102` | 同左 | 変化なし（0 件のまま） |
| `DirectiveUtil` | `line 20/20 branch 17/18` | 同左 | 変化なし |

**追補（2026-08-19 実測。§1-B〜§1-G・XLS-28・§6-A〜§6-H・YML-14・XLS-22 の番人移設ぶん）**

前の追補（547 件）から #25.5 の残り全ステップを走らせたぶんをまとめて導き直した。
**導出コマンドは上の ①〜③ と同じ。**

```
① 598
② src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:740
     @Ignore("YML-14: 反映されない値がある入力はエラーになるべき（testdata_notation.rst:891）。…")
   src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:1277
     @Ignore("XLS-40: カラム名の大小を保つあるべき姿。他責先は nablarch-testing の TableData…")
③ 8c327d0: 536
   HEAD: 598
```

```
Tests run: 598, Failures: 0, Errors: 0, Skipped: 2
```

**待機テスト（`@Ignore`）が 0 件から 2 件になった。** どちらも他責の型（`src/main` は無変更、
あるべき姿を主張するテストを書いて FAIL を確認し、理由＋他責先を書いた `@Ignore` を付ける）である。
`Skipped: 2` はこの 2 件である。

547 → 598 の内訳は **削除 19 件・追加 70 件**である。ファイル別の増減は次のとおり
（導出コマンドは `git grep -c '^    @Test'` を `04873de` と `HEAD` で取って突き合わせたもの）。

| テストクラス | 04873de | HEAD | 増減 |
|---|---:|---:|---:|
| `model/TestDataBlockTest` | 0 | 12 | **+12**（新設） |
| `model/RecordLayoutTest` | 2 | 13 | +11 |
| `model/FileDataBlockTest` | 5 | 12 | +7 |
| `model/TestDataContainerTest` | 2 | 9 | +7 |
| `model/MessageDataBlockTest` | 5 | 10 | +5 |
| `ConverterFileFilterTest` | 8 | 12 | +4 |
| `model/TableDataBlockTest` | 4 | 8 | +4 |
| `model/FieldDefTest` | 5 | 8 | +3 |
| `xls/XlsFormatWriterTest` | 46 | 48 | +2 |
| `yaml/YamlFormatReaderInvalidInputTest` | 31 | 33 | +2 |
| `TestDataConverterTest` | 13 | 14 | +1 |
| `xls/XlsFormatWriterInvalidOutputTest` | 17 | 16 | −1 |
| `yaml/YamlFormatWriterTest` | 38 | 36 | −2 |
| `xls/XlsFormatWriterModelTest` | 15 | 11 | −4 |
| 合計 | 547 | 598 | **+51** |

**増加の中心は `model/` のテストである。** #25.5 の後半は「不正値は書き出し側でなく中間モデルの
生成時に拒否する」方針（`steering.md` Decisions）に沿って番人を中間モデルへ集めたため、
検査点が辺③④のテストからモデルのテストへ移った。`model/` 6 クラスだけで **+46 件**である。

**削除した 19 件は、すべて「修正で入力そのものが作れなくなった」か「置き換え先ができた」かである。**

| 削除したテスト | 理由 | 起点コミット |
|---|---|---|
| `SampleConversionTest#convertsClimanSampleYamlToXls` | 0 件テーブルの書き出しを番人で止めたためサンプル変換が止まる。`#stopsClimanSampleConversionBecauseOfZeroRowTable` へ置き換え | `57c1b0d`（XLS-27） |
| `xls/XlsFormatReaderRealFileTest#readsEmptyColumnNamesFromMarkerOnlyTableInRealBook` ／ `#…ListMapInRealBook` | マーカーカラム除外のあとに空エントリ判定を行うようにしたため、版面の読み取り結果が変わった。`#dropsMarkerOnlyRowsAsEmptyEntriesInRealBook` ／ `#…InListMapInRealBook` へ置き換え | `a794a8e`（XLS-08） |
| `xls/XlsFormatWriterModelTest#promotesFirstDataRowToColumnNamesWhenEmptyColumnNamesAreReadBack` ／ `#writesEmptyHeaderRowWhenColumnNamesAreEmpty` | カラム名 0 件のブロックを辺③が書かなくなり、固定する版面が無くなった | `57c1b0d`（XLS-27） |
| `xls/XlsFormatWriterModelTest#writesDefaultDataTypeMarker` ／ `#dropsDefaultDataTypeBlockWhenReadBack` ／ `yaml/YamlFormatWriterTest#serialize_unsupportedDataType_throws` | `DataType.DEFAULT` のデータブロックを生成時に拒否したため、入力を組めなくなった | `7c10654`（§1-G・XLS-20） |
| `xls/XlsFormatWriterInvalidOutputTest#rejectsNullSheetName` | 読み込み単位の名前 `null` を生成時に拒否したため、辺③の番人へ到達しなくなった | `81cf234`（§1-F・XLS-33） |
| `xls/XlsFormatWriterTest#rejectsFieldWithoutTypeInFileBlock` ／ `#…InMessageBlock` ／ `yaml/YamlFormatWriterTest#serialize_fieldWithNullTypeInFileBlock_rejected` ／ `#…InMessageBlock_rejected` ／ `model/FieldDefTest#契約違反のnull型もモデル自身は検査せず保持する` | `FieldDef` の `name`・`type` の `null` を生成時に拒否したため、辺③④の番人へ到達しなくなった | `d0023c0`（§1-D・XLS-31） |
| `yaml/YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndOmittedLength` | 固定長ファイルの `length` `null` を弾くようにしたため入力が組めなくなった。`#serializeFile_fixedWithDirectivesAndMultipleRecords` へ書き直した | `3000baf`（§1-C・XLS-30） |
| `xls/XlsFormatWriterTest#rejectsRecordWithoutFieldsInFileBlock` ／ `#…InMessageBlock` ／ `yaml/YamlFormatWriterTest#serialize_recordWithoutFieldsInFileBlock_rejected` ／ `#…InMessageBlock_rejected` | フィールド 0 件の番人を `RecordLayout` の生成時へ移した。**この 4 件は移設前からすでに空振りだった**（`issues.md` XLS-22「番人の移設」） | `c31b534`（XLS-22） |

**削除した 19 件を参照している既存記述の現在地。** 本書の §1〜§4 の表は各タスク時点のスナップショットで、
その場では書き換えない取り決めである（各節に明記）。**削除したテスト名がそこに残っているため、
現在どこが担保しているかをここに一括で示す。** 表の判定（✅ など）は変わっていない。

> **本表の書き替え（2026-08-21 実測）。** 本表は追補その 5 を書いた 2026-08-19 時点の HEAD で確認したものだが、
> 同日の**追補その 6（§6-J-2・§6-J-3・§6-K）と追補その 7 に追随していなかった**。
> 「担保の現在地」列に**現在の HEAD に存在しないテストメソッド名を挙げていた 4 行**（`convertsClimanSampleYamlToXls`
> の行・辺③ C-08 の行・`promotesFirstDataRowToColumnNamesWhenEmptyColumnNamesAreReadBack` の行・
> `serializeFile_fixedWithDirectivesAndOmittedLength` の行）と、C-17 の行の 2 つ目の名前、
> および**表の直後の段落**を、実物（`src/test`）を開いて確かめた現在の担保へ書き替えた。
> **削除したテスト（左列）は当時の記録なのでそのまま残す。**
> 書き替え後の本表と直後の段落が「現在の担保」として挙げるテストメソッド 21 件が HEAD の `src/test` に
> 実在することの確認コマンド（**出力が 0 行なら全件実在**。件数は下のリストの要素数がそのまま導出である）:
>
> ```sh
> cd /home/tie303177/work/nablarch/nablarch-testing-converter
> for m in convertsClimanSampleIncludingZeroRowTable \
>          dropsMarkerOnlyRowsAsEmptyEntriesInRealBook \
>          dropsMarkerOnlyRowsAsEmptyEntriesInListMapInRealBook \
>          writesMarkerColumnForZeroRowTableBlock writesMarkerColumnForZeroRowListMapBlock \
>          roundTripsZeroRowTableWithoutEatingNextBlock roundTripsZeroRowListMapWithoutEatingNextBlock \
>          カラムなしでセルを持つ行を抱えるブロックは生成できない \
>          データタイプDEFAULTのブロックは生成できない 名前がnullの読み込み単位は生成できない \
>          データ型がnullのフィールド定義は生成できない データ型が空文字のフィールド定義は生成できる \
>          serializeFile_fixedWithDirectivesAndMultipleRecords \
>          固定長ファイルでフィールド長がnullのフィールド定義は保持できない \
>          フィールド長がnullの電文ブロックは生成できない 可変長ファイルはフィールド長がnullでも生成できる \
>          readFile_fixedWithoutLength_rejected フィールドを1件も持たないレコードは生成できない \
>          レコード種別省略をnullで保持する 本文レコードが0件の電文ブロックは生成できない \
>          メッセージ系の全データ種別を保持する; do
>   grep -rqF "public void $m(" src/test --include=*.java || echo "NOT FOUND: $m"
> done
> ```

| 削除したテスト | 担保の現在地（HEAD で存在を確認済み。2026-08-21 実測） |
|---|---|
| `convertsClimanSampleYamlToXls` | `SampleConversionTest#convertsClimanSampleIncludingZeroRowTable`。**サンプルは現在「0 件テーブルを含んだまま変換が通る」ことを固定している**（出力ブック数 2 冊）。追補その 5 が挙げていた `#stopsClimanSampleConversionBecauseOfZeroRowTable` は、`issues.md` **XLS-27** の【決着】（0 件テーブルを落とさずマーカーカラムを書く）で反転し HEAD に無い（追補その 6 の末尾に同じことが書いてある） |
| `readsEmptyColumnNamesFromMarkerOnlyTableInRealBook` ／ `#…ListMapInRealBook`（辺① C-08） | `XlsFormatReaderRealFileTest#dropsMarkerOnlyRowsAsEmptyEntriesInRealBook` ／ `#dropsMarkerOnlyRowsAsEmptyEntriesInListMapInRealBook` |
| `writesEmptyHeaderRowWhenColumnNamesAreEmpty`（辺③ C-08） | `XlsFormatWriterTest#writesMarkerColumnForZeroRowTableBlock` ／ `#writesMarkerColumnForZeroRowListMapBlock` ／ `#roundTripsZeroRowTableWithoutEatingNextBlock` ／ `#roundTripsZeroRowListMapWithoutEatingNextBlock`。**辺③はカラム名 0 件のブロックを拒否せず、マーカーカラム 1 列 `[EMPTY]` を書く**（`issues.md` **XLS-27** の【決着】）。カラム名 0 件で「セルを持つ行」だけは `TableDataBlockTest#カラムなしでセルを持つ行を抱えるブロックは生成できない` が拒否する（**XLS-21**）。追補その 5 が挙げていた `#rejectsTableBlockWithoutColumnNames` ／ `#rejectsListMapBlockWithoutColumnNames` は HEAD に無い（追補その 6 の C-08 行と同じ内容） |
| `promotesFirstDataRowToColumnNamesWhenEmptyColumnNamesAreReadBack` | `XlsFormatWriterTest#roundTripsZeroRowTableWithoutEatingNextBlock` ／ `#roundTripsZeroRowListMapWithoutEatingNextBlock`。**読み戻す対象は XLS-27 の【決着】で戻った。** 辺③がマーカーカラム `[EMPTY]` を書き、辺①で読み戻すと `getColumnNames().isEmpty()` ／ `getRows().isEmpty()` がともに真のブロックになり、後続ブロックも食われないことをこの 2 件が固定している（`XlsFormatWriterTest` を開いて確認）。追補その 5 の「担保先なし」は追補その 6 に追随していなかった記述である |
| `writesDefaultDataTypeMarker` ／ `dropsDefaultDataTypeBlockWhenReadBack` ／ `serialize_unsupportedDataType_throws`（A-01 `DEFAULT`） | `TestDataBlockTest#データタイプDEFAULTのブロックは生成できない`。**辺③の A-01 は「書ける」から「生成時に拒否」へ変わった**（§1-G・XLS-20）。辺③④とも `DEFAULT` を持つ入力を組めない |
| `rejectsNullSheetName`（辺③ F3 の `null` 側） | `TestDataContainerTest#名前がnullの読み込み単位は生成できない`（`XlsFormatWriterInvalidOutputTest` のクラス Javadoc から参照を張ってある） |
| `rejectsFieldWithoutTypeInFileBlock` ／ `#…InMessageBlock` ／ `serialize_fieldWithNullTypeInFileBlock_rejected` ／ `#…InMessageBlock_rejected` ／ `契約違反のnull型もモデル自身は検査せず保持する`（C-20 `type` 省略） | `FieldDefTest#データ型がnullのフィールド定義は生成できない`。境界（空文字は通す）は `#データ型が空文字のフィールド定義は生成できる` |
| `serializeFile_fixedWithDirectivesAndOmittedLength` | 記法検査は `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` へ書き直した。**`length` の番人も中間モデルの生成時へ移った**（`FileDataBlock` ／ `MessageDataBlock` のコンストラクタが `ModelPreconditions#requireLengths` を呼ぶ。`issues.md` **XLS-30**・§6-J-3）。担保は `FileDataBlockTest#固定長ファイルでフィールド長がnullのフィールド定義は保持できない` ／ `MessageDataBlockTest#フィールド長がnullの電文ブロックは生成できない`、可変長が `null` を通すことは `FileDataBlockTest#可変長ファイルはフィールド長がnullでも生成できる`、辺②の拒否は `YamlFormatReaderTest#readFile_fixedWithoutLength_rejected`。**#31 で可変長側に逆向きの番人（`ModelPreconditions#requireNoLengths`。`length` が `null` でないものを拒否する）が付いた**（`issues.md` **XLS-45**。担保は `FileDataBlockTest#可変長ファイルでフィールド長を持つフィールド定義は保持できない` ほか。追補その 12）。追補その 5 の「**`length` の番人だけは辺③④に置いたままである**」と、そこが挙げていた辺③④の 4 件（`XlsFormatWriterTest#rejectsFieldWithoutLengthInFixedFileBlock` ／ `#rejectsFieldWithoutLengthInMessageBlock` ／ `YamlFormatWriterTest#serialize_fieldWithoutLengthInFixedFileBlock_rejected` ／ `#serialize_fieldWithoutLengthInMessageBlock_rejected`）は**いずれも HEAD に無い** |
| `rejectsRecordWithoutFieldsInFileBlock` ／ `#…InMessageBlock` ／ `serialize_recordWithoutFieldsInFileBlock_rejected` ／ `#…InMessageBlock_rejected`（C-17 `fields` 空） | `RecordLayoutTest#フィールドを1件も持たないレコードは生成できない`。**2 つ目に挙げていた `#レコード種別を省略してもフィールド0件のレコードは生成できない` は追補その 7 で削除した**（同じ番人・同じメッセージを主張する二重主張だったため。レコード種別 `null` の保持そのものは `#レコード種別省略をnullで保持する` が担保する） |

**§3.1-3 の C-15 行が挙げる `XlsFormatWriterTest#rejectsMessageBlockWithoutRecords` ／
`#rejectsSendSyncMessageBlockWithoutRecords` は、いずれも現在の HEAD に無い**（2026-08-21 実測）。
追補その 5 を書いた時点では在ったため本表に載せていなかったが、同日の **§6-J-2**（`issues.md` **YML-12 2 形目**）で
番人が `MessageDataBlock` の生成時へ移り、空振りになった 2 件は追補その 6 で削除されている。
**現在の担保は `MessageDataBlockTest#本文レコードが0件の電文ブロックは生成できない` である**
（送信系の版 `#本文レコードが0件の送信系電文ブロックも生成できない` は追補その 7 で削除した。
電文系 5 種すべてを受理することは `#メッセージ系の全データ種別を保持する` が担保する）。追補その 6 の C-15 行と同じ内容である。

**`src/main` に手を入れたファイル**（上の 4 つの表への追加ぶん。`git diff --numstat 04873de..HEAD -- src/main` の 17 件）:

| ファイル | 課題 | 変更の要点 |
|---|---|---|
| `converter/ConverterFileFilter.java` | **XLS-28** | 拡張子違いの同名 Excel ブックの同居を辺①の入口で止める |
| `converter/DirectiveUtil.java` | （出典の貼り直しのみ） | `notation:nnn` を `30a8271` 基準へ貼り直した。振る舞いは無変更 |
| `converter/model/ModelPreconditions.java` | **XLS-38 / XLS-40 / XLS-41 / XLS-43 / XLS-22** | 新設。`null` 要素・重複名・行の要素数上限・Map のキー値 `null`・空リストの各不変条件をまとめる |
| `converter/model/TestDataBlock.java` | **§1-E / §1-G(XLS-20) / XLS-34 / XLS-35 / XLS-36** | `groupId`・`dataType`・識別子の `null`、`DEFAULT`、系統外のデータ種別を生成時に拒否 |
| `converter/model/FileDataBlock.java` | **§1-B(XLS-29) / XLS-36 / XLS-38** | `fileType` 必須、系統チェック、コレクション・Map の `null` 拒否 |
| `converter/model/MessageDataBlock.java` | **XLS-36 / XLS-38** | 同上 |
| `converter/model/TableDataBlock.java` | **XLS-36** | 系統チェック |
| `converter/model/ColumnRowDataBlock.java` | **XLS-38 / XLS-40** | コレクションの `null` 拒否。カラム名の重複は他責として番人を置かない旨を Javadoc に明記 |
| `converter/model/FieldDef.java` | **§1-C(XLS-30) / §1-D(XLS-31)** | `name`・`type` の `null` を生成時に拒否。`length` の契約を明記 |
| `converter/model/RecordLayout.java` | **XLS-38 / XLS-40 / XLS-41 / XLS-22** | 要素 `null`・フィールド名称の重複・行の要素数超過・フィールド 0 件を生成時に拒否 |
| `converter/model/TestDataContainer.java` | **XLS-37 / XLS-38** | 名前の `null`・コレクションの `null` 拒否 |
| `converter/model/TestDataSection.java` | **§1-F(XLS-33) / XLS-38** | 同上 |
| `converter/xls/XlsFormatReader.java` | **XLS-08** | マーカーカラム除外のあとに空エントリ判定を行う |
| `converter/xls/XlsFormatWriter.java` | **XLS-27 / §1-B / §1-C / §1-D / §1-F / XLS-22**（＋ **#26.5**） | 0 件テーブルの番人を追加。`FieldDef`・`RecordLayout` へ移した番人を撤去。**#26.5（`becbe30`）で `EMPTY_BLOCK_MARKER_COLUMN` を `"[空]"` から `"[EMPTY]"` へ改めた**（1 行の変更。**この 1 件だけが #25.5 の外にある src/main 変更であり、`steering.md` の Acceptance criteria が実装無改変の除外として名指ししている**） |
| `converter/yaml/YamlFormatWriter.java` | **§1-B / §1-C / §1-D / §1-F / §1-G / XLS-22** | 同上 |
| `converter/xls/BlockLayout.java` ／ `converter/xls/Fill.java` | **§1-G(XLS-20)** | `DEFAULT` の識別セルを書く経路が消えたことに伴う 1 行ずつの調整 |

**JaCoCo 実測（2026-08-19。導出コマンドは下の開示に載せたものと同じで `$3` を変えるだけ）**

| クラス | 追補その 4 | 追補その 5（現在） | 未到達の増減 |
|---|---|---|---|
| `XlsFormatWriter` | `line 167/168 branch 109/112` | **`line 169/170 branch 111/114`** | 変化なし（未到達 行 1・分岐 3 のまま） |
| `YamlFormatWriter` | `line 170/171 branch 95/98` | **`line 170/172 branch 98/102`** | **行 1・分岐 1 増えた**（下の開示） |
| `YamlFormatReader` | `line 192/192 branch 102/102` | 同左 | 変化なし（0 件のまま） |
| `DirectiveUtil` | `line 20/20 branch 17/18` | 同左 | 変化なし |
| `ModelPreconditions`（新設） | — | **`line 32/32 branch 22/22`** | 0 件 |
| `RecordLayout` | （未計測） | **`line 15/15 branch 2/2`** | 0 件 |

**開示（`YamlFormatWriter#sectionKey` の `default` が到達不能になった —— #25.5 の副作用）**

`YamlFormatWriter#sectionKey` の `switch (type)` の `default` 側 1 分岐と、その中の
`throw new IllegalArgumentException("unsupported DataType: " + type)` 1 行が
未到達である。**§1-G（XLS-20）で `DataType.DEFAULT` のデータブロックを生成時に拒否したため**、
この `default` へ入る値を作れなくなった（`switch` は残る 13 個の `DataType` をすべて分岐しており、
`DEFAULT` だけが漏れていた）。到達させていたテスト `serialize_unsupportedDataType_throws` は
入力を組めなくなったため削除した（上の削除表）。

**`DataType` が増えたときの安全網として残す。** 辺③にも同じ性質の安全網が
`XlsFormatWriter#layout`（`unsupported block`。`TestDataBlock` の実装クラスを
上ですべて分岐しているため到達しない）として未到達のまま在り、扱いをそろえる。
コードには到達不能である理由をコメントで書いてある。**軸A〜F の要素ではない。**

未到達の実測（`target/site/jacoco/jacoco.xml` を走査したもの）。**識別はクラス名・メソッド名で行う**
（`steering.md` Rules「台帳に他ファイルの行番号を書かない」。行番号つきの一覧は
`coverage-report.md` §3 にある）:

```
XlsFormatWriter#write（parent null 分岐）・#layout（unsupported block の分岐と throw 行）
                 ・#isMarkerColumn（columnName null 分岐）                  → 行 1・分岐 3
YamlFormatWriter#write（parent null 分岐）・#emitBlock（unsupported block の分岐と throw 行）
                 ・#rawGroup（`]` 側の分岐）・#sectionKey（default 分岐と throw 行） → 行 2・分岐 4
```

**開示（XLS-22 の番人 4 本は移設前から空振りだった —— JaCoCo で検出した）**

移設前の実測では `XlsFormatWriter#appendRecords` ／ `YamlFormatWriter#emitRecords`
（＝フィールド 0 件のレコードレイアウトを弾く番人そのもの）が未到達であった。
番人テストは 4 本とも緑だったが、`@Test(expected = IllegalArgumentException.class)` で
例外の型しか見ておらず、**§6-G（XLS-41）でコンストラクタに入れた別の番人が先に落としていた**。
`mvn test` の結果では区別がつかず、**JaCoCo の未到達行だけが手がかりだった**。
詳細は `issues.md` XLS-22 の「番人の移設（2026-08-19・実測）」と「残置している『緑の嘘』」節にある。

**開示（`DirectiveUtil` に残る未到達分岐 1 件 —— #25.5 で足した箇所ではない）**

JaCoCo 実測は **行 20/20（100%）・分岐 17/18（94.4%）** である。導出コマンド:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes \
  && JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec \
  && awk -F, 'NR > 1 && $3 == "DirectiveUtil" { print "line " $9 "/" ($8 + $9) " branch " $7 "/" ($6 + $7) }' \
       target/site/jacoco/jacoco.csv
```

出力は `line 20/20 branch 17/18`。未到達は `DirectiveUtil#toStringDirectives` の
`value == null ? null : valueMapper.map(...)` の **`null` 側 1 分岐**である
（**行としては到達済み**——`mb=1 cb=1`・`ci=9` の部分実行であって未到達行ではない。
**行は 20/20 で未到達行は 0 件**である。#26 の実測。`DirectiveUtil` は対象 6 区分の外なので
`coverage-report.md` §3 の表には載っておらず、扱いは同 §6-2・§6-3 にある）。

**この分岐は既存メソッド `toStringDirectives` の中にあり、#25.5 で足した `normalizeSeparator` では
ない。** `DirectiveUtil` は `8c327d0` の時点で存在し（`git cat-file -e
8c327d0:src/main/java/nablarch/test/tool/converter/DirectiveUtil.java` → exit 0。
`git diff --diff-filter=A --name-only 8c327d0..HEAD` は 1 件も返さない）、#25.5 が足したのは
`normalizeSeparator` 1 メソッドだけである（`git diff --numstat 8c327d0..HEAD --
src/main/java/nablarch/test/tool/converter/DirectiveUtil.java` → `42  0`）。
`normalizeSeparator` の分岐は全数到達済みである（下記 `jacoco.xml` の走査で `mb > 0` の行は
`toStringDirectives` の三項演算子の 1 行だけ）
（`target/site/jacoco/nablarch.test.tool.converter/DirectiveUtil.java.html` の当該行が
`title="1 of 2 branches missed."`、`jacoco.xml` の `toStringDirectives` が
`INSTRUCTION missed="2"`。この 2 命令は
`javap -c -p -cp target/classes nablarch.test.tool.converter.DirectiveUtil` の
`65: aconst_null` / `66: goto 90` ——すなわち `null` 側だけを通る 2 命令である）。
**これは軸A〜F の要素ではない。** 値が `null` のディレクティブは、実ファイル経路のどちらからも作れない:

- 辺①（Excel）の `directives` は `nablarch-testing` の `DataFile#directives` フィールドを
  そのまま写したものである（`TestCoreFileAdapter#read` の `new LinkedHashMap<>(file.directives)`）。
  この `Map` へ書き込む箇所は本体全体で `DataFile#setDirective` の 1 行だけであり
  （`git -C ~/work/nablarch/nablarch-testing grep -n 'directives\.put\|directives\.remove' c5f3340 -- 'src/main/java'`
  が `DataFile#setDirective` の `directives.put(directiveName, value);` 1 行だけを返す）、
  そこは直前で `convertDirectiveValue(directive, stringValue.trim())` を通るため
  `null` を渡すと NPE になる。
- 辺②（YAML）のスキーマ `$defs.directives` は `additionalProperties: false` の閉じた定義で、
  17 個のプロパティはすべて `type` が `string` ／ `integer` ／ `boolean` のいずれか単独であり、
  `null` を許すものは無い（`nablarch/test/ntf-testdata-yaml-schema.json`）。

したがってこの枝は、`Map` に `null` 値を直接入れた in-memory 入力でしか通らない安全網である。

**開示（`YamlTestCoreAdapter#defaultGroupOnlyYaml` に残る未到達分岐 1 件 —— #25.5 で新設した箇所）**

YML-02 の修正で新設した `defaultGroupOnlyYaml` に、当初は未到達分岐が 2 本残っていた
（レビュー 1 巡目の実測で発見。それまで本書に開示が無かった）。
うち 1 本は担保テストを足して閉じ、残る 1 本は到達不能であることを実測で確かめた。
現在の JaCoCo 実測は **行 49/50・分岐 18/19** である。導出コマンド:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes \
  && JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec \
  && awk -F, 'NR > 1 && $3 == "YamlTestCoreAdapter" { print "line " $9 "/" ($8 + $9) " branch " $7 "/" ($6 + $7) }' \
       target/site/jacoco/jacoco.csv
```

出力は `line 49/50 branch 18/19`。

| 分岐 | 状態 |
|---|---|
| `if (sectionObj instanceof List)` の false 側（対象セクションが YAML に無い） | **閉じた。** `YamlTestCoreAdapterTest#readSendSyncMessages_nullGroupId_sectionAbsent_returnsEmpty` が通す |
| `if (!(entryObj instanceof Map))` の true 側（エントリがスカラー） | **到達不能。** 公開 API から届かない |

到達不能の実測: `expected_request_header_messages: ["not a map"]` だけを書いた `.yaml` を
`YamlTestCoreAdapter#readSendSyncMessages(..., null, ...)` へ食わせて実行したところ、
`loadRawMap` が委譲する `YamlLoader.load` のスキーマ検証が
`nablarch.test.core.reader.yaml.YamlSchemaValidationException: YAML file failed schema validation`
を送出し、`defaultGroupOnlyYaml` に制御が届かなかった。
残る 1 行の未到達（`continue`）も同じ枝の中にある。防御ガードであり軸要素ではない。

**追補その 6（2026-08-19 実測。§6-J-2・§6-J-3・§6-K ぶん）**

追補その 5（598 件）から、書き出し側に残っていた番人の決着 3 ステップを走らせたぶんを導き直した。
**導出コマンドは上の ①〜③ と同じ。**

```
① 597
② src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:740
     @Ignore("YML-14: 反映されない値がある入力はエラーになるべき（testdata_notation.rst:891）。…")
   src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:1277
     @Ignore("XLS-40: カラム名の大小を保つあるべき姿。他責先は nablarch-testing の TableData…")
③ 8c327d0: 536
   HEAD: 597
```

```
Tests run: 597, Failures: 0, Errors: 0, Skipped: 2
```

**598 → 597（差 −1）の内訳**（`git diff 0a14655 HEAD -- src/test` で `public void` の増減を数えた実測。
追加 16 件・削除 17 件）:

| ファイル | 増減 |
|---|---|
| `model/FileDataBlockTest` | **+2**（XLS-30 の固定長・可変長） |
| `model/MessageDataBlockTest` | **+2**（XLS-30 の電文） |
| `model/TableDataBlockTest` | **+2**（XLS-21 の 2 形） |
| `yaml/YamlFormatReaderTest` | **+1**（XLS-30 の辺②拒否） |
| `xls/XlsFormatWriterTest` | **−3**（番人テスト 4 件削除・マーカーカラムと往復 4 件追加。ほかに XLS-29 ぶんの削除 1 件） |
| `yaml/YamlFormatWriterTest` | **−5**（YML-12 2 形目 2 件・XLS-30 2 件・XLS-29 1 件の番人テストを削除） |

**削除した番人テストは「空振り」になるためである。** 番人が中間モデルの生成時へ移ると、
書き出し側のテストは `build`／`serialize` へ届く前にコンストラクタで落ちる。
`@Test(expected = ...)` は例外の型しか見ないので緑のままだが、意図した番人は 1 行も通らない。

**JaCoCo 実測（2026-08-19。導出コマンドは §3.1-3 ／ §4.1-2 に載せたものと同じで `$3` を変えるだけ）**

| クラス | 追補その 5 | 追補その 6（現在） | 未到達の増減 |
|---|---|---|---|
| `XlsFormatWriter` | `line 169/170 branch 111/114` | **`line 157/158 branch 101/104`** | **変化なし**（未到達 行 1・分岐 3 のまま。総数が減ったのは番人 3 つを外したため） |
| `YamlFormatWriter` | `line 170/172 branch 98/102` | **`line 157/159 branch 86/90`** | **変化なし**（未到達 行 2・分岐 4 のまま。**これが現時点の値である。§4.1-2 の開示が挙げる「行 1・分岐 3／3 箇所」は #25.5 追補〈2026-08-18〉までの時点の値であって現時点の値ではない**） |
| `YamlFormatReader` | `line 192/192 branch 102/102` | 同左 | 変化なし（0 件のまま） |
| `ModelPreconditions` | `line 32/32 branch 22/22` | **`line 40/40 branch 28/28`** | 0 件（`requireLengths` を足しても未到達は出ていない） |
| `FileDataBlock`（新規計測） | — | **`line 14/14 branch 4/4`** | 0 件 |
| `MessageDataBlock`（新規計測） | — | **`line 13/13 branch 2/2`** | 0 件 |
| `ColumnRowDataBlock`（新規計測） | — | **`line 11/11 branch 6/6`** | 0 件 |
| `RecordLayout` | `line 15/15 branch 2/2` | 同左 | 0 件 |
| `DirectiveUtil` | `line 20/20 branch 17/18` | 同左 | 変化なし |

**この 3 ステップで未到達は 1 つも増えていない。** 既知の未到達の位置だけが番人の削除でずれた
（`target/site/jacoco/jacoco.xml` を走査した実測。**識別はクラス名・メソッド名で行う**）:

```
XlsFormatWriter#write（parent null 分岐）・#layout（unsupported block の分岐と throw 行）
                 ・#isMarkerColumn（columnName null 分岐）                  → 行 1・分岐 3
YamlFormatWriter#write（parent null 分岐）・#emitBlock（unsupported block の分岐と throw 行）
                 ・#rawGroup（`]` 側の分岐）・#sectionKey（default 分岐と throw 行） → 行 2・分岐 4
```

**未到達のメソッドの顔ぶれは追補その 5 から変わっておらず、行番号だけが動いた**（そのため本節では
行番号を書かない）。開示の内容（`unsupported block` と `unsupported DataType` の安全網が
到達不能であること）も変わらない。**行番号つきの一覧は `coverage-report.md` §3 にある**（#26 の実測）。

**軸の担保への影響（§3.1／§3.3／§4.1 のスナップショット表は書き換えない。現在地だけ示す）**

| 軸要素 | 追補その 5 時点の担保 | 現在の担保 |
|---|---|---|
| C-08 `columnNames` 空 | `XlsFormatWriterTest#rejectsTableBlockWithoutColumnNames` ／ `#rejectsListMapBlockWithoutColumnNames`（辺③が `IllegalArgumentException` で落とす） | **`XlsFormatWriterTest#writesMarkerColumnForZeroRowTableBlock` ／ `#writesMarkerColumnForZeroRowListMapBlock` ／ `#roundTripsZeroRowTableWithoutEatingNextBlock` ／ `#roundTripsZeroRowListMapWithoutEatingNextBlock`**。**落とさずマーカーカラム 1 列 `[EMPTY]` を書く**へ変わった（`issues.md` **XLS-27** の【決着】）。あわせて**カラム名 0 件で「セルを持つ行」**は `TableDataBlockTest#カラムなしでセルを持つ行を抱えるブロックは生成できない` が拒否する（**XLS-21**） |
| C-15 `MessageDataBlock.records` 空 ／ E-3(0 件) のメッセージ経路 | `XlsFormatWriterTest#rejectsMessageBlockWithoutRecords` ／ `#rejectsSendSyncMessageBlockWithoutRecords`（辺③） | **`MessageDataBlockTest#本文レコードが0件の電文ブロックは生成できない`**（送信系の版は追補その 7 で削除した。生成時拒否へ移設。`issues.md` **YML-12 2 形目**・§6-J-2）。**辺③④からこの版面へ到達する経路は無くなった** |
| C-21 `FieldDef.length` ＝ `null` の固定長・電文経路 | `XlsFormatWriterTest#rejectsFieldWithoutLengthInFixedFileBlock` ／ `#rejectsFieldWithoutLengthInMessageBlock`（辺③） | **`FileDataBlockTest#固定長ファイルでフィールド長がnullのフィールド定義は保持できない` ／ `MessageDataBlockTest#フィールド長がnullの電文ブロックは生成できない`**（生成時拒否へ移設。`issues.md` **XLS-30**・§6-J-3）。**可変長は `null` が正しい**ため `FileDataBlockTest#可変長ファイルはフィールド長がnullでも生成できる` が通す |
| `FileDataBlock.fileType` ＝ `null` | `XlsFormatWriterTest#rejectsFileBlockWithoutFileType` ／ `YamlFormatWriterTest#serialize_fileBlockWithoutFileType_rejected`（辺③④） | **番人そのものが無くなった（#29）。** `fileType` は `DataType` からの導出値になり、`null` を渡す口が消えたため検査が到達不能になった（`issues.md` **XLS-44** ／ **XLS-29** の【2026-08-21・#29】）。生成時拒否の版 `FileDataBlockTest#ファイル種別がnullのファイルブロックは生成できない` も削除した。**不変条件は型が保証している** —— `FileDataBlock#fileTypeOf` が 4 種の `DataType` を `FIXED` ／ `VARIABLE` のどちらかへ必ず写す |

**辺②（`YamlFormatReader`）の担保も動いた。** XLS-30 の移設により、スキーマ適合の YAML から
長さ無しの固定長ファイルブロック／空ボディの電文が中間モデルへ入る経路が塞がった。
`YamlFormatReaderTest#readFile_fixedWithoutLength_rejected`（新規）と
`#readMessage_emptyBody_rejected`（`#readMessage_emptyBody_isStillMapped` を反転）が固定する。

**同梱サンプルの変換が通るようになった。** `SampleConversionTest#stopsClimanSampleConversionBecauseOfZeroRowTable`
（0 件テーブルがあるため変換が中止されることを固定していた）は
`#convertsClimanSampleIncludingZeroRowTable`（出力ブック数 2 冊）へ反転した。


**追補その 7（2026-08-19 実測。#25.5 レビュー 2 巡目ぶん）**

追補その 6（597 件）から、**QA レビューの指摘で見つかった二重主張のテスト 2 件を削除した**ぶんを導き直した。
**導出コマンドは上の ①〜③ と同じ。**

```
① 595
② src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:740
     @Ignore("YML-14: 反映されない値がある入力はエラーになるべき（testdata_notation.rst:891）。…")
   src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:1280
     @Ignore("XLS-40: カラム名の大小を保つあるべき姿。他責先は nablarch-testing の TableData…")
③ 8c327d0: 536
   HEAD: 595
```

```
Tests run: 595, Failures: 0, Errors: 0, Skipped: 2
```

**597 → 595（差 −2）の内訳** —— 削除したのは次の 2 件で、**どちらも番人が分岐しない軸で
同じ番人・同じメッセージを主張していた**。番人を壊したとき 2 本とも同じ壊れ方をするため、
2 本目は回帰検知の力を増やしていない。

| 削除したテスト | 同じ番人を主張していた残る 1 本 | 分岐しないことの根拠 |
|---|---|---|
| `model/RecordLayoutTest#レコード種別を省略してもフィールド0件のレコードは生成できない` | `#フィールドを1件も持たないレコードは生成できない` | `RecordLayout` のコンストラクタは `recordType` を代入するだけで分岐せず、`requireNotEmpty("フィールド定義", …)` は `recordType` の値に依らず同じ経路を通る。**レコード種別 `null` の保持そのものは `#レコード種別省略をnullで保持する` が担保する** |
| `model/MessageDataBlockTest#本文レコードが0件の送信系電文ブロックも生成できない` | `#本文レコードが0件の電文ブロックは生成できない` | `MessageDataBlock` の `records.isEmpty()` 検査は `dataType` で分岐しない。**電文系 5 種すべてを受理することは `#メッセージ系の全データ種別を保持する` が担保する**（`MESSAGE` ／ `EXPECTED_REQUEST_HEADER_MESSAGES` ／ `EXPECTED_REQUEST_BODY_MESSAGES` ／ `RESPONSE_HEADER_MESSAGES` ／ `RESPONSE_BODY_MESSAGES` をループで通す） |

**削除した 2 件が主張していた不変条件そのものは残っている**（上表の「残る 1 本」）。
**残した側の Javadoc に「この軸では分岐しないため版を増やさない」ことと、移した担保先を書いた。**


**追補その 8（2026-08-21 実測。#27 で軸E の穴 2 件を埋めたぶん）**

追補その 7（595 件）から、**#27 の軸E 総点検で ❌ と確定した 2 件を埋めるテスト 2 件を足した**ぶんを
導き直した。**導出コマンドは上の ①〜③ と同じ。**

```
① 597
② src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:740
     @Ignore("YML-14: 反映されない値がある入力はエラーになるべき（testdata_notation.rst:891）。…")
   src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:1280
     @Ignore("XLS-40: カラム名の大小を保つあるべき姿。他責先は nablarch-testing の TableData…")
③ 8c327d0: 536
   HEAD: 597
```

```
Tests run: 597, Failures: 0, Errors: 0, Skipped: 2
```

**595 → 597（差 ＋2）の内訳** —— どちらも「入力にその件数を与えているだけで、出力に n＋1 件目が
無いことをアサートしていない」型の穴であり、**件数を出力側で固定した**。

| 追加したテスト | 埋めた軸要素 | 件数を固定した手段 | commit |
|---|---|---|---|
| `xls/XlsFormatWriterModelTest#writesOnlyOneBlockWhenSectionHasSingleBlock` | 辺③ E-1(1 件)（セクション内ブロック数 1） | シート全体の行数 `getPhysicalNumberOfRows()` を `is(3)`（識別行・カラム名行・データ行 1 行）。2 ブロック目が書かれれば 6 になって落ちる | `783810b` |
| `yaml/YamlFormatWriterModelTest#writesOneYamlFileWhenContainerHasSingleSection` | 辺④ E-4(1 件)（コンテナ内セクション数 1） | 出力先ディレクトリの実ファイル数 `out.list().length` を `is(1)`。2 件目のセクションが書き出されれば落ちる | `6d12021` |

**どちらも一時的に n＋1 件目を足して落ちることを実測した**（順に
`Expected: is <3> but: was <6>` ／ `Expected: is <1> but: was <2>`）。
**穴の所在と判定の根拠は `coverage/axis-matrix.md` §3.5 ／ §4.5 が正である。**

**追補その 9（2026-08-21 実測。#29 で XLS-44 を実施したぶん）**

追補その 8（597 件）から、**`FileDataBlock.fileType` を `DataType` からの導出に変えた**ぶんを
導き直した（`issues.md` **XLS-44**）。**導出コマンドは上の ①〜③ と同じ。**

```
① 600
② src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:740
     @Ignore("YML-14: 反映されない値がある入力はエラーになるべき（testdata_notation.rst:891）。…")
   src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:1280
     @Ignore("XLS-40: カラム名の大小を保つあるべき姿。他責先は nablarch-testing の TableData…")
③ 8c327d0: 536
   HEAD: 600
```

```
Tests run: 600, Failures: 0, Errors: 0, Skipped: 2
```

**597 → 600（差 ＋3）の内訳** —— 追加 4 件・削除 1 件である。

| 増減 | テスト | 担保・理由 |
|---|---|---|
| ＋ | `model/FileDataBlockTest#フィールド長がnullのフィールド定義は固定長系のデータ種別すべてで拒否される` | XLS-30 の番人が `DataType` 起点で走ること（導出前は `fileType` に `VARIABLE` を渡すと素通りした） |
| ＋ | `xls/XlsFormatWriterModelTest#writesLengthRowDecidedSolelyByDataType` | 辺③の長さ行の有無が `DataType` だけで決まること |
| ＋ | `yaml/YamlFormatWriterModelTest#writesFileTypeKeyDerivedFromDataType` | 辺④の `type:` が `DataType` から出ること（軸A A-06〜A-09） |
| ＋ | `yaml/YamlFormatWriterModelTest#restoresAllFourFileDataTypesThroughRealReader` | 辺④→辺② の往復でファイル系 4 種が化けないこと |
| − | `model/FileDataBlockTest#ファイル種別がnullのファイルブロックは生成できない` | **主張そのものが成り立たなくなった。** `fileType` を渡す口が消え、`fileType == null` の検査が到達不能になった（`issues.md` **XLS-29** の【2026-08-21・#29】）。番人を惜しんで別の検査に置き換えてはいない |

`model/FileDataBlockTest#固定可変とSETUP_EXPECTEDの全組合せを保持する` は
`#ファイル種別を4種のデータ種別から導出する` へ書き換えた（件数は増減しない）。
**軸C の C-10（`FileDataBlock.fileType`）はこの変更で欠番になった**（§0.4 の追補）。

**追補その 10（2026-08-24 実測。#30 で `fileTypeOf` の受け口を締めたぶん）**

追補その 9（600 件）から、**導出の受け口 `FileDataBlock#fileTypeOf` にファイル系 4 種以外の
データ種別を拒否する検査を置いた**ぶんを導き直した（`issues.md` **XLS-44**）。
**導出コマンドは上の ①〜③ と同じ。**

```
① 601
② src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:740
     @Ignore("YML-14: 反映されない値がある入力はエラーになるべき（testdata_notation.rst:891）。…")
   src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:1280
     @Ignore("XLS-40: カラム名の大小を保つあるべき姿。他責先は nablarch-testing の TableData…")
③ 8c327d0: 536
   HEAD: 601   ← #30 の 1 コミットが載った状態の値（記録日の HEAD a6306fc は 600。下の注を参照）
```

```
Tests run: 601, Failures: 0, Errors: 0, Skipped: 2
```

**③ の `HEAD: 601` は #30 の 1 コミットが載ったあとの値である。**
記録した 2026-08-24 の時点では #30 の変更がまだ作業ツリーにしか無く、③ が数える
コミット済みスナップショット（当時の `HEAD` ＝ `a6306fc`）は 600 だった。#30 は調整役が
1 コミットにまとめてから積むため、**この追補が載った `HEAD` では ①・③・`Tests run:` が
そろって 601 になる**（③ を 600 のまま凍結すると、そのままコマンドを実行しても再現しなくなる）。

**600 → 601（差 ＋1）の内訳** —— 追加 1 件のみ。

| 増減 | テスト | 担保・理由 |
|---|---|---|
| ＋ | `model/FileDataBlockTest#ファイル系でないデータ種別からはファイル種別を導出できない` | `public static` の受け口 `FileDataBlock#fileTypeOf` が、ファイル系 4 種以外（`DataType` 全 14 定数からファイル系 4 種を除いた 10 種すべて）を渡されたときに黙って `VARIABLE` を返さず `IllegalArgumentException` になること |

4 種それぞれの戻り値は既存の `model/FileDataBlockTest#ファイル種別を4種のデータ種別から導出する` が
担保しているため、足したのは拒否側の 1 本だけである。**XLS-36 の例外メッセージは変えていない**
（検査はコンストラクタと同じ `TestDataBlock#requireDataTypeOf` を使う）。

**追補その 11（2026-08-24 実測。#30 の続きで `fileTypeOf(null)` を閉じたぶん）**

追補その 10（601 件）から、**`fileTypeOf(null)` が `NullPointerException` ではなく
`IllegalArgumentException` になるようにした**ぶんを導き直した（`issues.md` **XLS-44** ／ **XLS-34**）。
**導出コマンドは上の ①〜③ と同じ。**

```
① 602
② src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:740
     @Ignore("YML-14: 反映されない値がある入力はエラーになるべき（testdata_notation.rst:891）。…")
   src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:1280
     @Ignore("XLS-40: カラム名の大小を保つあるべき姿。他責先は nablarch-testing の TableData…")
③ 8c327d0: 536
   HEAD: 602   ← この続きの 1 コミットが載った状態の値（記録時の HEAD b59104b は 601。追補その 10 と同じ注）
```

```
Tests run: 602, Failures: 0, Errors: 0, Skipped: 2
```

**601 → 602（差 ＋1）の内訳** —— 追加 1 件のみ。

| 増減 | テスト | 担保・理由 |
|---|---|---|
| ＋ | `model/FileDataBlockTest#データ種別がnullではファイル種別を導出できない` | `FileDataBlock.fileTypeOf(null)` が `NullPointerException` ではなく `IllegalArgumentException` になり、メッセージが XLS-34 の趣旨（データタイプの無いブロックはどちらの形式でも書けない）であること |

**受け口によって例外の種類が分かれる状態を残さないための 1 本である。** `TestDataBlock` の
コンストラクタは同じ入力（`dataType` ＝ `null`）を `IllegalArgumentException` で拒否しており
（`TestDataBlockTest#データ種別がnullのデータブロックは生成できない`）、`null` に対する答えは
XLS-34 で確定している。**コンストラクタ経路の振る舞いは変わっていない** —— `super(...)` が先に
`null` を落とすため `requireDataTypeOf` に `null` は届かない。**XLS-36 の例外メッセージも変えていない**
（`null` は `requireDataTypeOf` の別の分岐として足した）。

**追補その 12（2026-08-24 実測。#31 で XLS-45 を実施したぶん）**

追補その 11（602 件）から、**可変長ファイルのフィールド定義が `length` を持てないようにした**ぶんを
導き直した（`issues.md` **XLS-45**）。**導出コマンドは上の ①〜③ と同じ。**

```
① 605
② src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:740
     @Ignore("YML-14: 反映されない値がある入力はエラーになるべき（testdata_notation.rst:891）。…")
   src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java:1280
     @Ignore("XLS-40: カラム名の大小を保つあるべき姿。他責先は nablarch-testing の TableData…")
③ 8c327d0: 536
   HEAD: 605   ← #31 の 1 コミットが載った状態の値（記録時の HEAD ea1d560 は 602。追補その 10 と同じ注）
```

```
Tests run: 605, Failures: 0, Errors: 0, Skipped: 2
```

**602 → 605（差 ＋3）の内訳** —— 追加 3 件のみ。削除は無い。

| 増減 | テスト | 担保・理由 |
|---|---|---|
| ＋ | `model/FileDataBlockTest#可変長ファイルでフィールド長を持つフィールド定義は保持できない` | 可変長系のデータ種別のブロックが `length` を持つフィールド定義を保持できず `IllegalArgumentException` になること |
| ＋ | `model/FileDataBlockTest#フィールド長を持つフィールド定義は可変長系のデータ種別すべてで拒否される` | 拒否が可変長系 2 種（`SETUP_VARIABLE` ／ `EXPECTED_VARIABLE`）すべてに掛かり、`"10"` ／ `"-"` ／ 空文字の 3 表記すべてを弾くこと（6 通り）。`"-"` を含めているのは、それが本体で値の整形（改行・前後空白の除去）を起こす唯一の表記であり、長さの指定ではなくフィールド長の枠に相乗りしているためである |
| ＋ | `yaml/YamlFormatReaderInvalidInputTest#rejectsVariableFileFieldWithLengthFromRealYaml` | **辺②の実ファイル経路**で、可変長ファイルのフィールドに `length` を書いた YAML が落ちること（`loadRawMap` 差し替えの in-memory 経路は担保に数えない） |

**既存テスト 6 件の入力を直した（件数は増減しない）。** いずれも可変長のブロックへ長さつきの
レコードを渡していたもので、そのままでは中間モデルの生成時に落ちる。
`xls/XlsFormatWriterModelTest#writesLengthRowDecidedSolelyByDataType` は
「長さを持つ同一のレコードを固定長系・可変長系の両方へ渡す」形だったのを
「名前・型・データ行が同じで長さだけが違う 2 つのレコード」へ改めた
（XLS-44 の主張は「識別セルと長さ行が食い違わないこと」であり、長さの値ではなくデータ種別で決まる）。
`yaml/YamlFormatWriterModelTest` の 5 件は、同クラスの共有ヘルパ `record()` の可変長版
`variableRecord()`（長さだけを落とし、名前・型・値は同じ）を足して差し替えた。

**辺③④のライタ・`YamlTestDataValidator` には番人も WARN も足していない**（`issues.md` XLS-45 の
「converter 側の対応（#31）」）。

**追補その 13（2026-08-29 実測。#40 で 2-1（Excel 読みの値処理を本体と同じ順序にする）を実施したぶん）**

追補その 12（605 件）から、Step 4 第1回（#32〜#39）で足した 51 件と、#40 で足した 5 件を導き直した。
**導出コマンドは上の ①〜③ と同じ。**

```
① 670
② （0 件）
③ 8c327d0: 536
   HEAD: 656   ← #40 のコミットが載る前の値
```

**① が 670、③ の HEAD が 656 と食い違うのは、① が作業ツリーを数えるためである。** 差 14 件の内訳は
**#40 が足した 5 件**（`xls/XlsTrailingNullTest`）と、**追跡していない測定用の一時テスト 9 件**
（`core/reader/ZzOracleProbeTest` ／ `xls/ZzProbeTest`。#46 で削除する）である。
追跡対象だけを数えた値は **661** である。

```sh
# 追跡対象だけを数える（一時テストを除く）
git ls-files 'src/test/**/*.java' | xargs grep -c '^    @Test' | awk -F: '{s+=$2} END {print s}'
```

**605 → 661（差 ＋56）の内訳** —— Step 4 第1回で ＋51（§4.5 の 5 クラス）、#40 で ＋5。削除は無い。

| 増減 | テスト | 担保・理由 |
|---|---|---|
| ＋5 | `xls/XlsTrailingNullTest`（5 件） | ファイル・電文・送信同期電文の末尾に連続して `null` 記法を書いたときの値が、**フレームワーク本体が読む値と一致する**こと。期待値はテストが書かず `core/reader/FrameworkOracle` が本体から取る |

**#40 は既存テスト 2 件の期待値を変えた（件数は増減しない）。**
`core/reader/TestCoreReaderAdapterTest#readTablesReturnsRawTableData` →
`#readTablesReturnsValuesInterpretedByFramework`、
`xls/XlsFormatReaderTest#readMapsTableBlockPreservingRawValues` →
`#readMapsTableBlockWithFrameworkInterpretedValues`。どちらも「器から出る値は記法のまま（未加工）」を
主張していたが、本体にセルを解釈させる配線へ変えたため主張が成り立たなくなった。
フィクスチャがセルに Java `null` を入れていた点も直した（実 `PoiXlsReader` は空セルを空文字で返すため、
その入力は実在しない）。

**追補その 14（2026-08-29 実測。#41 で 2-2（マーカーカラムだけに値があるエントリを残す）を実施したぶん）**

追補その 13（追跡対象 661 件）から、#41 で足した 2 件を導き直した。**導出コマンドは上の ①〜③ と同じ。**
追跡対象だけを数える式は追補その 13 に併記したものを使う。

```
① 672   ← 作業ツリー。追跡していない測定用の一時テスト 9 件を含む
③ 8c327d0: 536
   HEAD: 661   ← #41 のコミットが載る前の値
```

追跡対象は **663** 件（661 ＋ 2）。

**661 → 663（差 ＋2）の内訳** —— 追加 2 件のみ。削除は無い。

| 増減 | テスト | 担保・理由 |
|---|---|---|
| ＋ | `xls/XlsMarkerOnlyEntryTest#keepsMarkerOnlyEntryInTableAsFrameworkDoes` | マーカーカラムだけに値があるエントリが、**本体が読むのと同じ 3 件**残ること（テーブル系） |
| ＋ | `xls/XlsMarkerOnlyEntryTest#keepsMarkerOnlyEntryInListMapAsFrameworkDoes` | 同上（`LIST_MAP` 経路。経路が別なので個別に固定する） |

**#41 は既存テスト 3 件の期待値を変えていない。** カラム名がマーカーカラムだけのブロックの結果（行 0 件）は
変わらないためである（`issues.md` XLS-08 の【2026-08-29・#41】）。
`XlsFormatReaderRealFileTest` 2 件と `XlsReferenceFixtureTest` 1 件について、
assert メッセージ・Javadoc の「全要素が空のエントリになるため読み飛ばされる（XLS-08）」を
「カラム名を 1 つも持たないブロックはデータ行を持たない」へ書き直した（**主張は同じ、理由の説明が変わった**）。

### 0.2 軸A: `DataType` 実定義との突き合わせ

実定義: `/home/tie303177/work/nablarch/nablarch-testing/src/main/java/nablarch/test/core/reader/DataType.java`
（converter リポジトリには存在せず、依存の `nablarch-testing` 本体で定義されている）

| # | 定数名 | `getType()` | `getName()`（記法名） |
|---|---|---|---|
| A-01 | `DEFAULT` | 0 | `DEFAULT` |
| A-02 | `SETUP_TABLE_DATA` | 1 | `SETUP_TABLE` |
| A-03 | `EXPECTED_TABLE_DATA` | 2 | `EXPECTED_TABLE` |
| A-04 | `EXPECTED_COMPLETED` | 4 | `EXPECTED_COMPLETE_TABLE` |
| A-05 | `LIST_MAP` | 3 | `LIST_MAP` |
| A-06 | `SETUP_FIXED` | 5 | `SETUP_FIXED` |
| A-07 | `EXPECTED_FIXED` | 6 | `EXPECTED_FIXED` |
| A-08 | `SETUP_VARIABLE` | 7 | `SETUP_VARIABLE` |
| A-09 | `EXPECTED_VARIABLE` | 8 | `EXPECTED_VARIABLE` |
| A-10 | `MESSAGE` | 9 | `MESSAGE` |
| A-11 | `EXPECTED_REQUEST_HEADER_MESSAGES` | 10 | 同名 |
| A-12 | `EXPECTED_REQUEST_BODY_MESSAGES` | 11 | 同名 |
| A-13 | `RESPONSE_HEADER_MESSAGES` | 12 | 同名 |
| A-14 | `RESPONSE_BODY_MESSAGES` | 13 | 同名 |

**突き合わせ結果: 14 種（`DEFAULT` ＋ 13）で steering の記述と一致。差異なし。**

### 0.3 軸B: `TestDataBlock` sealed 階層

`TestDataBlock`（abstract sealed）が permit するのは `ColumnRowDataBlock` / `FileDataBlock` / `MessageDataBlock`。
`ColumnRowDataBlock`（abstract sealed）がさらに `TableDataBlock` / `ListMapBlock` を permit する。
したがって **具象（final）実装は 4 種**であり、steering の「4種」と一致する。

| # | 具象クラス | 直接の親 | 保持する `DataType` |
|---|---|---|---|
| B-1 | `TableDataBlock` | `ColumnRowDataBlock` | SETUP_TABLE_DATA / EXPECTED_TABLE_DATA / EXPECTED_COMPLETED |
| B-2 | `ListMapBlock` | `ColumnRowDataBlock` | LIST_MAP（コンストラクタで固定） |
| B-3 | `FileDataBlock` | `TestDataBlock` | SETUP_FIXED / EXPECTED_FIXED / SETUP_VARIABLE / EXPECTED_VARIABLE |
| B-4 | `MessageDataBlock` | `TestDataBlock` | MESSAGE / 送信系 4 種 |

### 0.4 軸C: 中間モデル全フィールド（実クラスから読み取り）

全 **21 フィールド**。`src/main/java/nablarch/test/tool/converter/model/` の各クラスの `private final` 宣言をすべて列挙した。

| # | クラス | フィールド | 型 | 省略区分 | 省略時の表現（Javadoc 根拠） |
|---|---|---|---|---|---|
| C-01 | `TestDataContainer` | `name` | String | 必須 | — |
| C-02 | `TestDataContainer` | `sections` | List | 空許容 | 空リスト |
| C-03 | `TestDataSection` | `name` | String | 必須 | — |
| C-04 | `TestDataSection` | `blocks` | List | 空許容 | 空リスト |
| C-05 | `TestDataBlock` | `dataType` | DataType | 必須 | — |
| C-06 | `TestDataBlock` | `groupId` | String | **省略可** | `""`（TestDataBlock の Javadoc「省略時は空文字」） |
| C-07 | `TestDataBlock` | `identifier` | String | 必須 | — |
| C-08 | `ColumnRowDataBlock` | `columnNames` | List | 空許容 | 空リスト |
| C-09 | `ColumnRowDataBlock` | `rows` | List<List> | 空許容 | 空リスト |
| C-11 | `FileDataBlock` | `directives` | Map | 空許容 | 空 Map |
| C-12 | `FileDataBlock` | `records` | List | 空許容 | 空リスト |
| C-13 | `MessageDataBlock` | `directives` | Map | 空許容 | 空 Map |
| C-14 | `MessageDataBlock` | `fwHeaderFields` | Map | 空許容 | 空 Map（MessageDataBlock の Javadoc「FW ヘッダを読まない経路では空 Map」） |
| C-15 | `MessageDataBlock` | `records` | List | 空許容 | 空リスト |
| C-16 | `RecordLayout` | `recordType` | String | **省略可** | `null`（RecordLayout の Javadoc「省略時は null」） |
| C-17 | `RecordLayout` | `fields` | List | 空許容（型定義上）／**契約は 1 件以上**（下の追補） | 空リスト |
| C-18 | `RecordLayout` | `rows` | List<List> | 空許容 | 空リスト |
| C-19 | `FieldDef` | `name` | String | 必須 | — |
| C-20 | `FieldDef` | `type` | String | **省略可** | `null`（FieldDef の Javadoc「省略時は null」） |
| C-21 | `FieldDef` | `length` | String | **省略可** | `null`（FieldDef の Javadoc「省略時は null」） |

内訳: 必須スカラー 5 件（C-01, C-03, C-05, C-07, C-19）／
**省略可能フィールド 4 件**（C-06, C-16, C-20, C-21 — Javadoc に「省略時は…」と明記）／
空許容コレクション 11 件（C-02, C-04, C-08, C-09, C-11, C-12, C-13, C-14, C-15, C-17, C-18）。
**合計 20 フィールド。C-10 は欠番である。**

**追補（2026-08-21・#29）: C-10（`FileDataBlock.fileType`）は欠番になった。** 中間モデルが
`fileType` を保持するのをやめ、`DataType` から導出するようにしたためである（`issues.md` **XLS-44**）。
`getFileType()` は導出値を返すアクセサとして残るが、フィールドではないので軸C の対象ではない。
**C-11 以降は繰り上げない** —— 繰り上げると本書・`axis-matrix.md`・`issues.md` に散らばる既存の
ID 参照がすべて別の要素を指すためである。

**追補（2026-08-18・#25.5）: C-17 `RecordLayout.fields` の「空許容」は型定義上の話であり、契約としては
1 件以上である。** Excel 記法・YAML スキーマのどちらもフィールドを持たないレコードレイアウトを
認めていないため（Excel は `testdata_notation.rst:888`、YAML は本体スキーマ
`nablarch/test/ntf-testdata-yaml-schema.json` の `$defs.record_fragment` が `fields` 必須かつ `minItems` ＝ 1）、
空を保持できるのは中間モデルだけという**契約の穴**だった。#25.5 で `RecordLayout` の Javadoc に
「1 件以上」を明記し、書き出し側（`XlsFormatWriter` ／ `YamlFormatWriter`）が空を受けたら
`IllegalArgumentException` で落とすようにした（`issues.md` **XLS-22** ／ **YML-12 3形目**）。
**上表の「空許容」列は #18 時点の実定義の読み取りとしてそのまま残す**（`RecordLayout` は
自分では検査しないため、型としては空リストを保持できる）。

**追補（2026-08-18・#25.5）: C-20 `FieldDef.type` の「省略可」は型定義上の話であり、契約としては
必須（`null` 不可）である。** Excel 記法・YAML スキーマのどちらもデータ型を持たないフィールド定義を
認めていないため（Excel は `testdata_notation.rst:883` が固定長で
「フィールド名称・データ型・フィールド長の3リストが同サイズで必須」・可変長で
「フィールド名称・データ型の2リストが同サイズで必須」と定め、`:888` が
「フィールド名称リストまたはデータ型リストが未指定または空である」を記述時のエラーに挙げる。
YAML は本体スキーマ `nablarch/test/ntf-testdata-yaml-schema.json` の `$defs.field_def` が
`required` ＝ `["name", "type"]`）、`null` を保持できるのは中間モデルだけという**契約の穴**だった。
#25.5 で `FieldDef` の Javadoc に「`type` は必須（`null` 不可）」を明記し、書き出し側
（`XlsFormatWriter` ／ `YamlFormatWriter`）が `null` を受けたら `IllegalArgumentException` で
落とすようにした（`issues.md` **YML-12 4形目**）。**弾くのは `null` だけで、空文字は弾かない。**
`C-21`（`length`）は従来どおり省略可である。**上表の「省略可」列は #18 時点の実定義の読み取りとして
そのまま残す**（`FieldDef` は自分では検査しないため、型としては `null` を保持できる）。

**追補（2026-08-18・#25.5）: C-15 `MessageDataBlock.records` の「空許容」は型定義上の話であり、
契約としては 1 件以上（0 件不可）である。** Excel 記法・YAML スキーマのどちらも本文レコード 0 件の電文を
認めていないため、空を保持できるのは中間モデルだけという**契約の穴**だった。
YAML は本体スキーマ `nablarch/test/ntf-testdata-yaml-schema.json` の `$defs.message_data` ／
`$defs.expected_request_message_data` ／ `$defs.group_message_data` がいずれも
`required` ＝ `["id","records"]` かつ `records.minItems` ＝ **1** である。Excel も同じで、
記法に電文のレコード 0 件を表す書き方の明文が無い（電文が存在しない場合は
`testdata_notation.rst:1257`（`30a8271` 時点）のとおり**データブロックごと省略する**）。
**0 バイト空ファイル特例（`records: []`）は電文に及ばない** —— 同特例は `notation:881`／`:1109`／`:1146`
（同）といずれも**ファイルに限定**して書かれており、スキーマ側も `$defs.file_data` だけが
`records.minItems` ＝ **0** である。`:1158`「…前述のファイルデータと同じ構成を持つ」は
**カラム構成のみ**を指す。
#25.5 で `MessageDataBlock` の Javadoc に「1 件以上」を明記し、書き出し側
（`XlsFormatWriter#layoutMessage` ／ `YamlFormatWriter#emitMessage`）が 0 件を受けたら
`IllegalArgumentException` で落とすようにした（`issues.md` **YML-12 2形目**）。
**`C-12`（`FileDataBlock.records`）は従来どおり空許容である**（0 バイトの空ファイル）。
**上表の「空許容」列は #18 時点の実定義の読み取りとしてそのまま残す**（`MessageDataBlock` は
自分では検査しないため、型としては空リストを保持できる）。

**steering との差異（コーディネータ判断を仰ぐ点）**: steering #20 の Steps は
「`groupId` / `identifier` / `fileType` / `directives` / `fwHeaderFields` / `recordType` / `FieldDef.type` / `FieldDef.length` は
『値あり』『省略』の双方を通す」としているが、実定義上 `identifier`（C-07）と `fileType`（C-10）には
「省略」の表現が存在しない（`identifier` は必須スカラー、`fileType` は FIXED/VARIABLE の 2 値）。
本棚卸しでは実定義を正とし、`fileType` は「FIXED / VARIABLE 双方」、`identifier` は「値あり 1 通り」として扱う。
**`fileType` は #29 でフィールドではなくなり、C-10 は欠番になった**（上の追補）。**固定長／可変長の
双方を通すという要求そのものは残っており、いまはファイル系 4 種の `DataType`（A-06〜A-09）が担う。**
`directives` / `fwHeaderFields` は「非空 / 空 Map」の双方として扱う。

### 0.5 軸D 値の表現 — 要素（辺ごとに定義が異なる。steering #19/#22/#24/#25 の記述を要素化）

- **辺① セル種別 8 ケース**: D1-01 文字列／D1-05 先頭ゼロ文字列／D1-12 セル不在／D1-13 空文字／
  D1-14 前後空白／D1-15 改行／D1-16 リテラル `null`／D1-17 表示形式 `@` の数値セル

  対象は **NTF が実行できるテストデータ**に限る。Excel 側の条件は `PoiXlsReader` のクラス Javadoc が
  定める「全セルが文字列書式」であり、これを外れるセル種別（表示形式を持たない数値・日付書式・
  時刻書式・日時書式・数式・真偽値・エラー値）は担保対象でも記録対象でもない
  （ユーザー確定・2026-08-13）。**タグは振り直さないため番号に欠番が出る。**
  D1-17 だけは表示形式 `@` ＝ 文字列書式で但し書きを満たしながらセル種別が数値であり、
  前提の内側で値が変わる唯一のケースのため残す。
- **辺② YAML スカラー 12 ケース（2026-08-14・ユーザー確定。#18 時点の 10 ケース定義から改めた）**:
  D2-01 引用符なし文字列（`abc`）／D2-02 引用符あり（`"abc"`・`'abc'`）／D2-03 引用符付き数値（`"123"`）／
  D2-04 引用符付き末尾ゼロ小数（`"1.50"`）／D2-05 真偽値に見える文字列（`"true"`・`TRUE`・`yes`）／
  D2-06 NULL（`null`・値なし）／D2-07 NULL に見える文字列（`"null"`・`~`・`NULL`）／
  D2-08 日付・日時風文字列／D2-09 複数行（`|`・`>`）／D2-10 先頭ゼロ・非 JSON 数値記法（`007`・`0x1F`）／
  D2-11 空文字・前後空白（`""`・`"  pad  "`）／D2-12 特殊文字を含む文字列（`"a: b"`・`"a #b"`）

  **#18 時点の定義との差**: D2-06 が `null`・`~`・値なし を 1 ケースにまとめていたのに対し、
  実測では `~` だけが文字列になる（`coverage/issues.md` **YML-01**）ため D2-06（NULL になるもの）と
  D2-07（文字列になるもの）へ分けた。あわせて D2-11・D2-12 を足して 12 ケースになった。

  対象は **NTF が実行できるテストデータ**に限る。YAML 側の条件は本体スキーマ
  （yaml jar 内 `nablarch/test/ntf-testdata-yaml-schema.json`）であり、値の型を `["string","null"]` に
  限るため、引用符なしの `true` / `123` / `1.50` / `.inf` / `.nan` は担保対象でも記録対象でもない
  （`coverage/issues.md`「対象としない入力（辺②）」。ユーザー確定・2026-08-14）。
- **辺③ セル型 8 ケース（`getCellType()` をアサート）**: D3-01 `"100"`／D3-02 `"=1+1"`／D3-03 `"007"`／
  D3-04 `null`／D3-05 `""`／D3-06 改行含む文字列／D3-07 32767 文字超／D3-08 制御文字含む
- **辺④ YAML 表現 9 ケース**: D4-01 `"100"`／D4-02 `"true"`／D4-03 `"null"`／D4-04 `null`／D4-05 `""`／
  D4-06 `"007"`／D4-07 改行含む／D4-08 `"2026-08-07"`／D4-09 コロン・ハイフン・`#` 含む

### 0.6 軸E 多重度 — 要素（4 観点 × 0 件／1 件／複数件。steering #21 より）

- **E-1 セクション内ブロック数**（0／1／複数）
- **E-2 ブロック内行数**（0／1／複数）
- **E-3 ファイル内レコードレイアウト数**（0／1／複数）
- **E-4 コンテナ内セクション数**（1／複数）— 呼称は本書を通じて「E-4 コンテナ内セクション数」に統一する。
  辺ごとの実体は、辺①（ブック内シート数）／辺②（ディレクトリ内 YAML ファイル数）／
  辺③（ブック内シート数）／辺④（出力 YAML ファイル数）。

### 0.7 軸F 異常系 — 要素（辺ごと。steering #21/#22/#24/#25 より）

- **辺①（6）**: F1-01 シート不在／F1-02 ブック破損／F1-03 未知のデータタイプ名／
  F1-04 マーカーカラム欠落／F1-05 カラム名重複／F1-06 行と列の数の不一致
- **辺②（5）**: F2-01 スキーマ違反／F2-02 YAML として不正／F2-03 未知のキー／F2-04 必須構造の欠落／F2-05 空ファイル
- **辺③（4）**: F3-01 出力先不在／F3-02 同名ファイル既存かつ `overwrite=false`／F3-03 書き込み権限なし／
  F3-04 シート名が Excel 制約違反（31 文字超・禁止文字）
- **辺④（3）**: F4-01 出力先不在／F4-02 `overwrite=false` 衝突／F4-03 書き込み権限なし

### 0.8 棚卸しで判明した横断的な事実

以下は「事実:」（コードを読んで確認した内容）と「判断:」（それに基づく本書の扱い）を分けて記す。
判断のうちコーディネータの確認を要するものは §5.3 に再掲する。

1. **辺①の既存 33 件は 1 件も実 `.xlsx` を通っていない。**
   事実: `XlsFormatReaderTest` は内部クラス `FakeTestDataReader` に `List<List<String>>` の
   canned 行を与えて `TestCoreReaderAdapter` を駆動する。実セル → 文字列行の区間（`PoiXlsReader`）は
   一度も実行されない。
   判断: **軸D 辺①（8 ケース）は 33 件からは全て未担保**とする。
2. **辺③の往復テスト 8 件（`roundTrips*`）は実 `.xlsx` を経由して `XlsFormatReader` を駆動している。**
   事実: `XlsFormatWriterTest#roundTrip` は `new XlsFormatWriter().write(...)` で実ファイルを書き、
   `new XlsFormatReader()`（本番配線＝`PoiXlsReader`）で読み戻す。
   判断: steering #19 の「実 `.xlsx` を入力として `XlsFormatReader` を駆動するテストが存在し、
   `FakeTestDataReader` を経由していない」は**既に部分的に満たされている**（文字列セル・空セル・
   リテラル `null` の 3 ケース相当が通る）。#19 はこれを起点にできる。
3. **辺②の既存 20 件は 1 件も実 YAML テキストを通っていない。**
   事実: `YamlFormatReaderTest#reader` は `YamlTestCoreAdapter#loadRawMap` を in-memory
   `LinkedHashMap` に差し替える。YAML パーサ（SnakeYAML Engine）は通らない。一方、辺④の往復テスト 6 件
   （`roundTrip_*`）は `writer.write(...)` で実 YAML ファイルを書き `new YamlFormatReader()` で読み戻す。
   判断: **軸D 辺②（#18 時点の定義で 10 ケース）は 20 件からは全て未担保**とし、往復 6 件が通す分は 🔺 で計上する。
   **#24 で解消した（2026-08-14 追記）**: 実 `.yaml` を入力とする 3 クラスを追加し、
   現在の定義（12 ケース。§0.5）をすべて担保した（§2.1-2）。
4. **`getCellType()` をアサートしているテストは #18 時点では src/test 全体でゼロだった。**
   事実（#18 時点）: `grep -rn "getCellType" src/test/` → 0 件。`XlsFormatWriterTest` のセル読み出しヘルパ
   `cell` ／ `line` は `getStringCellValue()` のみを使う。
   **この 2 つは #23 のレビュー対応で `XlsFixture` へ移した。**
   判断（#18 時点）: **軸D 辺③（8 ケース）は `getCellType()` 観点では全て未担保**とする。
   **現在は 0 件ではない（2026-08-13 追記。件数の内訳をレビュー指摘により訂正）。**
   #19 が `XlsFormatReaderCellTypeTest` に、#22 が `XlsFormatWriterCellTypeTest` に入れた。実測は次のとおり。

   | クラス | 追加タスク | `@Test` の数（`grep -c "^    @Test"`） | `getCellType()` を使うアサートの数 |
   |---|---|---|---|
   | `XlsFormatReaderCellTypeTest` | #19 | **10** | **1**（`readsTextFormattedNumericCellAsDoubleString` 内の 1 行） |
   | `XlsFormatWriterCellTypeTest` | #22 | 18 | **17** |

   **`XlsFormatReaderCellTypeTest` の件数を 19 → 10 へ訂正した（2026-08-14・#25.5 で件数を導き直した際に発見）。**
   19 だったのは `c04261d` 時点で、直後の `227adc1`「軸D 辺① を NTF 実行可能な入力 8 ケースへ絞り込む」で
   10 になっていたのに本表を更新していなかった。**#25.5 の修正が減らしたのではない**
   （`8c327d0`（#25 完了時点）でも 10 である。`git show 8c327d0:<path> | grep -c '^    @Test'` で確認）。

   `grep -c getCellType src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterCellTypeTest.java`
   は **19** を返すが、これは<b>行数</b>であってアサート数でもテスト数でもない
   （19 行のうち 2 行はクラス Javadoc の散文）。以前ここに「#22 が 19 件を入れた」と書いていたのは
   この行数をテスト件数として並べたもので、誤りだった。
   上記の「ゼロ」は #18 時点のスナップショットであり、現在形として読んではならない。
5. **`overwrite` フラグを writer は持たない。**
   事実: `grep -rln "overwrite" src/main/java` の結果、`overwrite` を保持するのは `ConversionRequest` /
   `TestDataConverter` / `ConverterMojo` の 3 クラスのみで、`XlsFormatWriter` / `YamlFormatWriter` は保持しない。
   `overwrite=false` 衝突を検査するのは `TestDataConverter#checkOverwrite` であり、
   上位層の既存テスト `TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse` と
   `ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict` が通している
   （いずれも出力先に既存ファイルを置いた状態で変換し、`ConverterException` ／ `MojoExecutionException` を
   アサートしている）。
   判断: 軸F の F3-02 / F4-02 は writer 単体では再現できないため、**辺③／辺④の対象外**
   として分類する（steering #22/#25 の Steps と一致）。

   **担保範囲の訂正（2026-08-13・レビュー指摘による訂正。2026-08-13 の第 3 ラウンドで根拠を再訂正）。**
   当初この項と各節は「上位層の既存テストで担保済み」と書いていたが、
   **担保されているのは `.yaml` を出力側とする衝突だけ**である。
   事実:
   - `checkOverwrite` は `target.outputPaths(container, outputBase)` を多態で呼び分ける。
     引用した 2 件の既存テストは**どちらも XLS→YAML** であり（`TestDataConverter.convert(DataFormat.XLS,
     DataFormat.YAML, ...)` ／ Mojo の `from=xls, to=yaml`。衝突させているのは `BookA/data.yaml`）、
     実行されるのは `YamlFormatHandler#outputPaths` である。
   - **`XlsFormatHandler#outputPaths` 自体は `overwrite=false` 下で実行されている。**
     4 引数入口 `TestDataConverter.convert(DataFormat, DataFormat, Path, Path)` は
     `Builder#overwrite` を呼ばずにリクエストを組み、`ConversionRequest` の `overwrite` 既定値は
     `false` なので、`checkOverwrite` は早期 return せず
     `outputPaths` を呼ぶ。実際に通しているのは XLS を出力側とする
     `TestDataConverterTest#convertsYamlToXls`／`#convertsXlsToXls`／
     `#convertsYamlWithFilesToXls` の 3 件である。
   - **1 件も通っていないのは `.xlsx` が既存で衝突する分岐**（`checkOverwrite` の
     `Files.exists(output)` が真 → `ConverterException`）**のほう**である。

   **確かめ方（変異テスト。2026-08-13 実測。`src/main` は確認後に元へ戻してある）**:

   | 変異 | 結果 |
   |---|---|
   | `XlsFormatHandler#outputPaths` の先頭で `IllegalStateException` を送出する | `TestDataConverterTest` の `convertsXlsToXls` / `convertsYamlToXls` / `convertsYamlWithFilesToXls` の **3 件だけが ERROR**（`Tests run: 410, Failures: 0, Errors: 3`）。つまり `outputPaths` は実行されている |
   | `checkOverwrite` の `Files.exists(output)` が真かつ出力が `.xlsx` のときだけ `AssertionError` を送出する | **410 件すべて PASS**（`Tests run: 410, Failures: 0, Errors: 0`）。つまり `.xlsx` の衝突分岐は 1 件も通っていない |

   以前ここには「`grep -rn "outputPaths" src/test --include=*.java` → **0 件**」と書いていたが、
   これは誤りだった。このコマンドがヒットするのは `XlsFormatWriterInvalidOutputTest` の
   クラス Javadoc の記述だけで（確認: `grep -rn "outputPaths" src/test --include=*.java | grep -v
   XlsFormatWriterInvalidOutputTest` → **0 件**）、当時も 0 件ではなく、自分の書いた記述を数えていた。
   そもそも `outputPaths` を**直接呼ぶ**テストが無いことと、`outputPaths` が**実行されない**ことは別である
   （上表の変異のとおり、多態呼び出しで実行されている）。

   したがって正確には、**共通処理 `checkOverwrite` の分岐も `XlsFormatHandler#outputPaths` 自体も
   既存テストが通しているが、`.xlsx` を出力側とする衝突（＝辺③の F3-02 が指す状況）は未担保**である。
   ただしこれは上位層側の穴であって辺③（`XlsFormatWriter` 単体）の責務ではないため、
   **F3-02 を辺③の対象外とする結論は変えない**（`XlsFormatWriter` は `overwrite` を保持しないので、
   辺③に書いても再現できない）。本書で「上位層で担保済み」と記した箇所はこの但し書きつきで読むこと。
   辺④（YAML 出力）の F4-02 については、上記 2 件がまさに `.yaml` を出力側とする衝突であり、
   この但し書きは当たらない。**#25 で実物（`TestDataConverter#checkOverwrite` ／ `YamlFormatHandler#outputPaths` ／
   上位層のテスト 2 件）を開いて再確認した**（§4.1-2 の「F4-02 を対象外とした根拠」）。
6. **1 リソース単位 API のため、辺①・辺②では「セクション複数」「セクション 0」が構造上生成されない。**
   事実: `XlsFormatReader#read` は `Collections.singletonList(section)` を返し、
   `YamlFormatReader#read` も同じく `Collections.singletonList(section)` を返す。
   一方 writer 側は `XlsFormatWriter#build` ／ `YamlFormatWriter#write` が
   `container.getSections()` をループするため、辺③／辺④では複数・0 とも到達可能である。
   判断: E-4「コンテナ内セクション数 複数」と C-02「sections 空」は**辺①・辺②では到達不能**、
   **辺③・辺④では要追加**として分類する。
   **辺③の C-02「sections 空」は #23 で担保済みになった（2026-08-13 追記）**:
   `XlsFormatWriterModelTest#writesWorkbookWithoutSheetsWhenContainerHasNoSections` が
   シートを 1 枚も持たないブックが書き出されることを実測して固定した（`issues.md` **XLS-23**）。
   **辺④の C-02「sections 空・複数」と E-4(複数) は #25 で担保済みになった（2026-08-14 追記）**:
   `YamlFormatWriterModelTest#writesNothingWhenContainerHasNoSections`（空。**ファイルも出力先
   ディレクトリも作られない** —— 辺③がシート 0 枚のブックを書くのとは非対称）と
   `#writesOneYamlFilePerSectionWhenContainerHasMultipleSections`（複数）で固定した。
7. **`DataType.DEFAULT` はリーダ 2 経路のいずれでも生成されない。**
   事実: 辺① — `TestCoreReaderAdapter` が `type == DataType.DEFAULT` のブロックを `continue` でスキップする。
   辺② — `YamlFormatReader#addBlocksForSection` と `fileDataType` ／
   `addMessageBlocks` は `DEFAULT` 以外の 13 種のみを分岐に持ち、`DEFAULT` を返す経路がない。
   判断: A-01 `DEFAULT` は**辺①・辺②で到達不能**。writer 側（辺③）は
   `XlsFormatWriter#marker` がマーカー文字列を `block.getDataType().getName()` から組み立てるだけで
   タイプを絞らないため到達可能であり、**辺③は要追加**（辺④は `serialize_unsupportedDataType_throws` で担保済み）。
   **辺③は #23 で担保済みになった（2026-08-13 追記）**: `XlsFormatWriterModelTest#writesDefaultDataTypeMarker` が
   識別セル `DEFAULT=T` が書き出されることを実測して固定した。あわせて
   `#dropsDefaultDataTypeBlockWhenReadBack` が「辺③で書けたブロックが辺①で読み戻すと消える」ことを実検査する。
   辺③（書ける）と辺④（例外）の非対称は `issues.md` **XLS-20** に記録した（修正はしない）。
   **その後 #25.5 §1-G（XLS-20）で決着した（2026-08-21 に実物で確認して追記）**: `DataType.DEFAULT` の
   データブロックは `TestDataBlock` の生成時に拒否されるようになり、辺③④とも `DEFAULT` を持つ入力を組めない。
   上の 3 件（`XlsFormatWriterModelTest#writesDefaultDataTypeMarker` ／ `#dropsDefaultDataTypeBlockWhenReadBack` ／
   辺④の `serialize_unsupportedDataType_throws`）は**いずれも HEAD に無い**。現在の担保は
   `TestDataBlockTest#データタイプDEFAULTのブロックは生成できない` である（§0.1-2 追補その 5 の対応表と同じ）。

### 0.8-8 `RoundTripTest`（30 件）の扱い

`RoundTripTest`（30 `@Test`）は 4 辺いずれの
担当クラスのテストでもないため §0.1 の 130 件（2026-08-19 実測）には含まれないが、`new XlsFormatWriter().write(...)` で実
`.xlsx` を書き `new XlsFormatReader().read(...)`（本番配線）で読み戻す XLS 経路 13 件と、
`new YamlFormatWriter().write(...)` → `new YamlFormatReader().read(...)` の YAML 経路 14 件、
両経路を 1 メソッドで通す 3 件からなり、**4 辺すべてを実ファイル経由で駆動している**
（往復ヘルパ: `xlsRoundTrip` ／ `yamlRoundTrip`）。

steering Rules（フェーズ2）に従い、これらが通す軸要素は **🔺弱い担保として計上するが正式担保としては数えず、
直接テストの追加対象からも外さない**。したがって未担保一覧には残したうえで「`RoundTripTest#xxx` で 🔺 担保あり
（重複を避けること）」と注記する。

| # | テストメソッド | 経路（駆動する辺） | 🔺 で通す軸A | 🔺 で通す軸B | 🔺 で通す軸C | 🔺 で通す軸D |
|---|---|---|---|---|---|---|
| 1 | `xls_setupTable_isPreserved` | XLS（辺③→辺①） | A-02 | B-1 | C-05, **C-06(省略=`""`)**, C-07, C-08, C-09 | 辺① D1-01, D1-13／辺③ D3-05 |
| 2 | `xls_expectedTable_withGroupId_isPreserved` | XLS（辺③→辺①） | A-03 | B-1 | C-05, C-06(値あり), C-07, C-08, C-09 | 辺① D1-01 |
| 3 | `xls_expectedCompleteTable_isPreserved` | XLS（辺③→辺①） | **A-04（辺①で唯一）** | B-1 | C-05, C-06(省略), C-07, C-08, C-09 | 辺① D1-01 |
| 4 | `xls_listMap_isPreserved` | XLS（辺③→辺①） | A-05 | B-2 | C-05, C-07, C-08, C-09 | 辺① D1-01, D1-13／辺③ D3-05 |
| 5 | `xls_setupFixed_isPreserved` | XLS（辺③→辺①） | A-06 | B-3 | C-05, C-06(省略), C-07, C-10(FIXED), C-11(値あり), C-12, C-16(値あり), C-17〜C-21 | 辺① D1-01 |
| 6 | `xls_expectedFixed_isPreserved` | XLS（辺③→辺①） | **A-07（辺①・辺③で唯一）** | B-3 | C-05, C-06(省略), C-07, C-10(FIXED), C-12, C-16(値あり), C-17〜C-21 | 辺① D1-01 |
| 7 | `xls_setupVariable_isPreserved` | XLS（辺③→辺①） | A-08 | B-3 | C-05, C-06(省略), C-07, C-10(VARIABLE), C-21(省略) | 辺① D1-01 |
| 8 | `xls_expectedVariable_isPreserved` | XLS（辺③→辺①） | **A-09（辺①・辺③で唯一）** | B-3 | C-05, C-06(省略), C-07, C-10(VARIABLE), C-21(省略) | 辺① D1-01 |
| 9 | `xls_message_isPreserved` | XLS（辺③→辺①） | A-10 | B-4 | C-05, C-06(省略), C-07, C-14(値あり), C-15, C-16〜C-21 | 辺① D1-01 |
| 10 | `xls_expectedRequestHeaderMessages_isPreserved` | XLS（辺③→辺①） | A-11 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16〜C-21 | 辺① D1-01 |
| 11 | `xls_expectedRequestBodyMessages_isPreserved` | XLS（辺③→辺①） | A-12 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16〜C-21 | 辺① D1-01 |
| 12 | `xls_responseHeaderMessages_isPreserved` | XLS（辺③→辺①） | A-13 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16〜C-21 | 辺① D1-01 |
| 13 | `xls_responseBodyMessages_isPreserved` | XLS（辺③→辺①） | A-14 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16〜C-21 | 辺① D1-01 |
| 14 | `yaml_setupTable_isPreserved` | YAML（辺④→辺②） | A-02 | B-1 | C-05, C-06(省略), C-07, C-08, C-09 | 辺② D2-02, D2-06／辺④ D4-04, D4-05 |
| 15 | `yaml_expectedTable_withGroupId_isPreserved` | YAML（辺④→辺②） | A-03 | B-1 | C-05, C-06(値あり), C-07, C-08, C-09 | 辺② D2-02 |
| 16 | `yaml_expectedCompleteTable_isPreserved` | YAML（辺④→辺②） | A-04 | B-1 | C-05, C-06(省略), C-07, C-08, C-09 | 辺② D2-02 |
| 17 | `yaml_listMap_isPreserved` | YAML（辺④→辺②） | A-05 | B-2 | C-05, C-07, C-08, C-09 | 辺② D2-02 |
| 18 | `yaml_listMap_withNullValue_isPreserved` | YAML（辺④→辺②） | A-05 | B-2 | C-09 | 辺② D2-06／辺④ D4-04 |
| 19 | `yaml_setupFixed_isPreserved` | YAML（辺④→辺②） | A-06 | B-3 | C-05, C-06(省略), C-07, C-10(FIXED), C-11(値あり), C-12, C-16(値あり), C-17〜C-21 | 辺② D2-02 |
| 20 | `yaml_expectedFixed_isPreserved` | YAML（辺④→辺②） | **A-07（辺④で唯一）** | B-3 | C-05, C-06(省略), C-07, C-10(FIXED), C-12, C-16(値あり), C-17〜C-21 | 辺② D2-02 |
| 21 | `yaml_setupVariable_isPreserved` | YAML（辺④→辺②） | **A-08（辺④で唯一）** | B-3 | C-05, C-06(省略), C-07, C-10(VARIABLE), C-21(省略) | 辺② D2-02 |
| 22 | `yaml_expectedVariable_isPreserved` | YAML（辺④→辺②） | A-09 | B-3 | C-05, C-06(省略), C-07, C-10(VARIABLE), C-21(省略) | 辺② D2-02 |
| 23 | `yaml_message_isPreserved` | YAML（辺④→辺②） | A-10 | B-4 | C-05, C-06(省略), C-07, C-14(値あり), C-15, C-16〜C-21 | 辺② D2-02 |
| 24 | `yaml_expectedRequestHeaderMessages_isPreserved` | YAML（辺④→辺②） | A-11 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16(省略=null), C-17〜C-21 | 辺② D2-02 |
| 25 | `yaml_expectedRequestBodyMessages_isPreserved` | YAML（辺④→辺②） | A-12 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16(省略=null), C-17〜C-21 | 辺② D2-02 |
| 26 | `yaml_responseHeaderMessages_isPreserved` | YAML（辺④→辺②） | A-13 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16(省略=null), C-17〜C-21 | 辺② D2-02 |
| 27 | `yaml_responseBodyMessages_isPreserved` | YAML（辺④→辺②） | A-14 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-15, C-16(省略=null), C-17〜C-21 | 辺② D2-02 |
| 28 | `nullCell_isPreservedInBothPaths`（**#32 で改称**。旧 `nullCell_xlsConvertsToLiteralString_yamlPreservesNull`） | XLS＋YAML（4 辺） | A-02 | B-1 | C-09（行のみアサート） | 辺① **D1-16 `null` 記法**／辺② D2-06／辺③ D3-04（値のみ）／辺④ D4-04 |
| 29 | `leadingTrailingWhitespace_isPreservedInBothPaths` | XLS＋YAML（4 辺） | A-02 | B-1 | C-09（行のみアサート） | 辺① **D1-14 前後空白**／辺② D2-02 |
| 30 | `specialNotation_isPreservedInBothPaths` | XLS＋YAML（4 辺） | A-02 | B-1 | C-09（行のみアサート） | 辺① D1-01／辺② D2-02 |

※ 軸E は全 30 件が「1 セクション・1 ブロック」固定（`xlsRoundTrip` ／ `yamlRoundTrip` が
`sections.size()==1` と `blocks.size()==1` をアサートする）ため、E-1(1)／E-4(1) 以外の多重度は通さない。
軸F は 30 件とも正常系のため通さない。

**この表によって従来 ❌ だった要素に新たに 🔺 が付くもの**:

| 辺 | 新たに 🔺 になる要素 | 根拠テスト |
|---|---|---|
| 辺① | A-04 `EXPECTED_COMPLETED` | `xls_expectedCompleteTable_isPreserved` |
| 辺① | A-07 `EXPECTED_FIXED` | `xls_expectedFixed_isPreserved` |
| 辺① | A-09 `EXPECTED_VARIABLE` | `xls_expectedVariable_isPreserved` |
| 辺① | C-06 `groupId` 省略(`""`) | `xls_setupTable_isPreserved`, `xls_expectedCompleteTable_isPreserved` ほか 5 件（`assertTableBlock` ／ `assertFileBlock` ／ `assertMessageBlock` が `getGroupId()` を `""` と突き合わせる XLS 経路 7 件） |
| 辺① | D1-14 前後空白 | `leadingTrailingWhitespace_isPreservedInBothPaths` |
| 辺③ | A-07 `EXPECTED_FIXED` | `xls_expectedFixed_isPreserved` |
| 辺③ | A-09 `EXPECTED_VARIABLE` | `xls_expectedVariable_isPreserved` |
| 辺③ | **A-12 `EXPECTED_REQUEST_BODY_MESSAGES`**（2026-08-13 追記） | `xls_expectedRequestBodyMessages_isPreserved` |
| 辺③ | **A-13 `RESPONSE_HEADER_MESSAGES`**（同上） | `xls_responseHeaderMessages_isPreserved` |
| 辺③ | **A-14 `RESPONSE_BODY_MESSAGES`**（同上） | `xls_responseBodyMessages_isPreserved` |
| 辺④ | A-07 `EXPECTED_FIXED`（**#25 で直接の担保が付き ✅ になった**） | `yaml_expectedFixed_isPreserved` |
| 辺④ | A-08 `SETUP_VARIABLE`（同上） | `yaml_setupVariable_isPreserved` |

辺① D1-01 文字列／D1-13 空文字／D1-16 リテラル `null`、辺② D2-02／D2-03／D2-06／D2-07、
辺③ D3-04／D3-05、辺④ D4-01 は既に `XlsFormatWriterTest#roundTrips*` ／ `YamlFormatWriterTest#roundTrip_*`
経由で 🔺 であり、`RoundTripTest` は担保の厚みを増すが判定は変えない。
**辺④ D4-01 と 辺④ A-07／A-08 は #25 で直接の担保が付き ✅ になった**（§4.1-2）。
辺②については新たに 🔺 になる要素はない。

**辺③ A-12〜A-14 の 3 行は 2026-08-13（#23 レビュー対応）で追加した。**
#18 時点は辺③ A-12〜A-14 を ✅ と判定していたためこの表に載せていなかったが、変異による実測で
`RoundTripTest` の 3 件だけが辺③のこれらを通していること（＝🔺）が分かった
（[§3.1-3](#s3-1-3-sendsync)）。3 件とも #23 レビュー対応で ✅ になっているが、
**この表は「その要素を `RoundTripTest` が 🔺 として通している」という事実の一覧**であるため、
✅ 化後も行は残す（辺① A-04／A-07／A-09 と同じ扱い）。

**未担保件数への影響はない。** 🔺 は正式担保として数えないため、§5.1 の 107 件は本項の追加後も変わらない。

---

<a id="s1"></a>

## 1. 辺① Excel→中間モデル（`XlsFormatReaderTest` 33 件）

### 1.1 テストメソッド → 担保する軸要素

| # | テストメソッド | 軸A | 軸B | 軸C | 軸D | 軸E | 軸F |
|---|---|---|---|---|---|---|---|
| 1 | `readMapsTableBlockPreservingRawValues` | A-02 | B-1 | C-07, C-08, C-09 | — ※文字列レベルで `${}`／`""`／null セル区別 | E-1(1), E-2(複数) | — |
| 2 | `readTableNormalizesExcelQuotationNotation` | A-02 | B-1 | C-09 | — ※Excel 引用符記法 `""`→`""`・`"abc"`→`abc` | E-2(1) | — |
| 3 | `readListMapNormalizesExcelQuotationNotation` | A-05 | B-2 | C-09 | — ※同上 | E-2(1) | — |
| 4 | `readFixedFileNormalizesExcelQuotationNotation` | A-06 | B-3 | C-12, C-18 | — ※同上 | E-3(1) | — |
| 5 | `readMapsMultipleTablesWithoutDuplication` | A-02 | B-1 | C-07 | — | E-1(複数=2) | — |
| 6 | `readPreservesGroupIdAndDataType` | A-03 | B-1 | C-05, C-06(値あり), C-09 | — | E-1(1) | — |
| 7 | `readListMapPreservesColumnOrder` | A-05 | B-2 | C-08(記述順), C-09 | — | E-2(複数=2) | — |
| 8 | `readListMapExcludesMarkerColumns` | A-05 | B-2 | C-08, C-09 | — ※マーカーカラム `[no]` 除外 | E-2(複数=2) | — |
| 9 | `readMapsListMapBlock` | A-05 | B-2 | C-07, C-08, C-09 | — ※`${}`／`""` | E-2(複数=2) | — |
| 10 | `readMapsFixedLengthFileBlock` | A-06 | B-3 | C-07, C-10(FIXED), C-12, C-16(値あり), C-17, C-18, C-19, C-20(値あり), C-21(値あり) | — | E-3(1) | — |
| 11 | `readRestoresOriginalRecordTypeTypeAndOmittedLengthFromRawLines` | A-06 | B-3 | C-16, C-18, C-20, C-21(`"-"`) | — ※長さ記法 `-` の原文保持 | E-3(1) | — |
| 12 | `readRestoresMultipleRecordLayoutsInFixedFile` | A-06 | B-3 | C-12, C-16, C-17, C-18, C-19, C-20, C-21 | — | E-3(複数=2), E-2(1/複数) | — |
| 13 | `readMapsVariableLengthFileBlock` | A-08 | B-3 | C-10(VARIABLE), C-18, C-19, C-21(省略=null) | — | E-3(1) | — |
| 14 | `readMapsMessageBlock` | A-10 | B-4 | C-05, C-07, C-14(値あり), C-15, C-18, C-19 | — | E-3(1) | — |
| 15 | `readMapsExpectedRequestHeaderMessageBlock` | A-11 | B-4 | C-05, C-06(値あり), C-07, C-13(値あり), C-14(空), C-15, C-17, C-18, C-19, C-20, C-21 | — | E-3(1) | — |
| 16 | `readMapsAllFourSendSyncMessageTypes` | A-11, A-12, A-13, A-14 | B-4 | C-05, C-06 | — | E-1(複数=4) | — |
| 17 | `readMapsMultipleSendSyncBlocksInSameGroup` | A-11 | B-4 | C-07 | — | E-1(複数=2) | — |
| 18 | `readNormalizesRecordSeparatorCrlfSymbol` | A-08 | B-3 | C-11(値あり) | — ※`record-separator` CRLF シンボル逆正規化 | — | — |
| 19 | `readNormalizesRecordSeparatorLfSymbol` | A-08 | B-3 | C-11 | — ※LF シンボル | — | — |
| 20 | `readNormalizesRecordSeparatorCrSymbol` | A-08 | B-3 | C-11 | — ※CR シンボル | — | — |
| 21 | `defaultConstructorWiresProductionAdapter` | — | — | — | — | — | —（本番配線の生成可能性のみ） |
| 22 | `readIgnoresDataTypePrefixedLineWithoutMarker` | — | — | C-04(空) | — | E-1(0) | 🔺F1-03 に近い（データタイプ名で始まるが `=` なしの行を無視） |
| 23 | `readPreservesErrorModeRowInSendSyncMessage` | A-14 | B-4 | C-16, C-17, C-18, C-19 | — ※`errorMode:timeout` の原文保持 | E-3(1) | — |
| 24 | `readDerivesContainerAndSectionNamesFromResource` | A-02 | B-1 | C-01, C-02(1件), C-03 | — | E-4(1) | — |
| 25 | `readListMapWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins` | A-05 | B-2 | C-08, C-09 | — | — | **F1-05** ✅（WARN・後勝ち） |
| 26 | `readListMapWithMultipleDuplicateColumnsEmitsWarnPerName` | A-05 | B-2 | C-08, C-09 | — | — | **F1-05** ✅（複数名の重複） |
| 27 | `readTableWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins` | A-02 | B-1 | C-08, C-09 | — | — | **F1-05** ✅（TABLE 系） |
| 28 | `readListMapWithoutDuplicatesEmitsNoWarn` | A-05 | B-2 | — | — | — | F1-05 の非回帰（WARN なし） |
| 29 | `readReturnsEmptySectionWhenNoBlocks` | — | — | C-02(1件), C-04(空) | — | **E-1(0)** ✅ | — |
| 30 | `readAssemblesMixedBlockTypesInOneSection` | A-02, A-05, A-06, A-10 | **B-1, B-2, B-3, B-4** | C-04 | — | E-1(複数=4) | — |
| 31 | `readNormalizesRecordSeparatorEmptyValueToNoneSymbol` | A-08 | B-3 | C-11 | — ※NONE シンボル逆正規化 | — | — |
| 32 | `readPassesThroughUnknownRecordSeparatorValue` | A-08 | B-3 | C-11 | — ※未知値のパススルー | — | — |
| 33 | `readStripsQuotesFromQuotedGenericDirectiveValue` | A-08 | B-3 | C-11 | — ※ディレクティブ値の引用符除去 | — | — |

### 1.2-2 #19／#20／#21 が追加したテストクラスの担保（2026-08-12 追記）

**本節は #20 で新設し、#21 で追記した。** §1.1 は「`XlsFormatReaderTest` 33 件」を対象とした
#18 時点の事実であり書き換えていない。ここには #19／#20／#21 が追加した
**実 `.xlsx` を入力とするテストクラス**の担保だけを記す。

| テストクラス | 追加タスク | 件数 | 入力 |
|---|---|---|---|
| `XlsFormatReaderCellTypeTest` | #19 | 19 | `XlsFixture` が POI で組み立てた実 `.xlsx` |
| `XlsReferenceFixtureTest` | #19 | 2 | Excel 保存物の参照フィクスチャ ＋ POI 生成物 |
| `XlsFormatReaderRealFileTest` | #20 ＋ #21 | 23（#20 が 17 ＋ #21 が 6） | `XlsFixture` が POI で組み立てた実 `.xlsx` |
| `XlsFormatReaderInvalidInputTest` | #21 | 16 | 同上（意図的に壊した入力・破損ブック） |

**軸A（`XlsFormatReaderRealFileTest`。#20 で ✅ 化した 3 件を太字）**

列を 2 つに分けてある。**「`getDataType()` をアサート」列がその要素の担保**であり、「同経路を通過（参考）」列は
同じデータタイプのブロックを実 `.xlsx` から作ってはいるが `getDataType()` 自体は見ていないテストである。
後者は担保として数えない（重複テストを避けるための索引として載せている）。
参考列の括弧書きは、そのテストが**軸A について**何を見ていないかを示すためのものであり、
アサーション全体の要約ではない（各テストは自分の担当軸要素については別途アサートしている）。

| 要素 | `getDataType()` をアサート（＝担保） | 同経路を通過（参考） |
|---|---|---|
| A-01 `DEFAULT` | —（到達不能。§0.8-7） | — |
| A-02 `SETUP_TABLE_DATA` | `readsSetupTableBlockFromRealBook` | `readsEmptyColumnNamesFromMarkerOnlyTableInRealBook`, `readsContainerAndSectionNamesFromRealBookAndSheetNames`, `readsFourBlockImplementationsFromOneRealSheet`（3 件とも `getDataType()` は見ない。最後の 1 件は実装クラスと識別子を見る） |
| A-03 `EXPECTED_TABLE_DATA` | `readsExpectedTableBlockWithGroupIdFromRealBook` | — |
| **A-04 `EXPECTED_COMPLETED`** | `readsExpectedCompletedTableBlockFromRealBook`（#18 では 🔺 `RoundTripTest` のみ → ✅） | — |
| A-05 `LIST_MAP` | `readsListMapBlockFromRealBook` | `readsEmptyColumnNamesFromMarkerOnlyListMapInRealBook`, `readsFourBlockImplementationsFromOneRealSheet`（2 件とも `getDataType()` は見ない） |
| A-06 `SETUP_FIXED` | `readsSetupFixedFileBlockFromRealBook` | `readsOmittedFieldLengthNotationFromRealBook`, `readsOmittedRecordTypeAsNullFromRealBook`, `readsFourBlockImplementationsFromOneRealSheet`（3 件とも `getDataType()` は見ない。前 2 件は軸C の `length` 省略・`recordType` 省略が担当） |
| **A-07 `EXPECTED_FIXED`** | `readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook`（#18 では 🔺 → ✅） | — |
| A-08 `SETUP_VARIABLE` | `readsSetupVariableFileBlockWithoutFieldLengthFromRealBook` | — |
| **A-09 `EXPECTED_VARIABLE`** | `readsExpectedVariableFileBlockWithGroupIdFromRealBook`（#18 では 🔺 → ✅） | — |
| A-10 `MESSAGE` | `readsMessageBlockFromRealBook` | `readsFourBlockImplementationsFromOneRealSheet` |
| A-11〜A-14 送信同期 4 種 | `readsAllFourSendSyncMessageTypesFromRealBook`（識別子を `RM01`〜`RM04` と別々にし、データタイプ・グループ ID・識別子・ディレクティブを 1 対 1 で突き合わせる） | — |

**軸B** — B-1〜B-4 のすべてを `readsFourBlockImplementationsFromOneRealSheet` が 1 シートから生成し、
実装クラスに加えて識別子（`T` / `lm` / `f.dat` / `m`）まで突き合わせる。各種別の個別テストも
`onlyBlock(Class)` ヘルパで実装クラスを確認している。

**軸C（#18 時点から状態が変わったものだけ。根拠は `coverage/issues.md` の課題 ID）**

| 要素 | #18 の判定 | #21 後 | 担保テストメソッド／根拠 |
|---|---|---|---|
| C-06 `groupId` 省略(`""`) | 🔺（`RoundTripTest` のみ） | ✅ | `readsSetupTableBlockFromRealBook`, `readsListMapBlockFromRealBook`, `readsMessageBlockFromRealBook` |
| C-08 `columnNames` 空 | ❌ | ✅ | `readsEmptyColumnNamesFromMarkerOnlyTableInRealBook`, `readsEmptyColumnNamesFromMarkerOnlyListMapInRealBook`（`issues.md` XLS-08） |
| C-11 `FileDataBlock.directives` 空 | ❌ | **到達不能** | `issues.md` XLS-07。根拠テスト `readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook` |
| C-13 `MessageDataBlock.directives` 空 | ❌ | **到達不能** | `issues.md` XLS-07。根拠テスト `readsAllFourSendSyncMessageTypesFromRealBook` |
| C-16 `recordType` 省略(`null`) | ❌ | ✅（**#25.5 で到達不能 → 担保済みへ変わった**） | `readsOmittedRecordTypeAsNullFromRealBook`。#21 時点は「実 `.xlsx` 経路では空セルが `""` として読まれるため到達不能」としていたが、その `""` 自体が不具合（`issues.md` **XLS-06**）であり、#25.5 で `null` を入れるよう直した（5721ecd）。旧テスト名は `readsOmittedRecordTypeAsEmptyStringFromRealBook` |
| C-17 `RecordLayout.fields` 空 | ❌ | **到達不能** | `issues.md`「到達不能」表（名前行が 2 列未満だと本体パーサが失敗する） |
| C-20 `FieldDef.type` 省略(`null`) | ❌ | **到達不能** | `issues.md`「到達不能」表（型の欠落は本体パーサが 2 通りの機構で弾く） |
| C-21 `length` 値あり（省略記法 `-`） | ✅（Fake 経路のみ） | ✅（実 `.xlsx` 経路も） | `readsOmittedFieldLengthNotationFromRealBook` |
| C-09 `rows` 空 | ❌ | ✅（**#21**） | `readsEmptyRowsFromTableWithoutDataRowsInRealBook`（テーブル経路）／`readsEmptyRowsFromListMapWithoutDataRowsInRealBook`（LIST_MAP 経路） |
| C-12 `FileDataBlock.records` 空 | ❌ | ✅（**#21**） | `readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook` |
| C-15 `MessageDataBlock.records` 空 | ❌ | ✅（**#21**） | `readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook` |
| C-18 `RecordLayout.rows` 空 | ❌ | ✅（**#21**） | `readsEmptyRowsFromRecordLayoutWithoutValueRowsInRealBook` |

**軸D** — §0.5 の 8 ケース（D1-01／D1-05／D1-12〜D1-17）すべてを #19 の
`XlsFormatReaderCellTypeTest` が実 `.xlsx` で担保した（`issues.md` XLS-01／XLS-04／XLS-05）。
#18 で「全滅」としていた空欄は埋まっている。

**軸E（#21。0 件は実 `.xlsx` 経路で新規担保。1 件／複数件の既存担保も併記する）**

| 要素 | 0 件 | 1 件 | 複数件 | 担保テストメソッド |
|---|---|---|---|---|
| E-1 セクション内ブロック数 | ✅ | ✅ | ✅ | 0: `XlsFormatReaderRealFileTest#readsEmptyBlockListFromRealSheetWithoutMarkers`（#20）／1: 同クラスの単一ブロックのテスト多数／複数: `#readsFourBlockImplementationsFromOneRealSheet`（4 件）・`#readsAllFourSendSyncMessageTypesFromRealBook`（4 件） |
| E-2 ブロック内行数 | ✅（**#21**） | ✅ | ✅ | 0: `#readsEmptyRowsFromTableWithoutDataRowsInRealBook`／`#readsEmptyRowsFromListMapWithoutDataRowsInRealBook`／1: `#readsExpectedTableBlockWithGroupIdFromRealBook`（1 行）／複数: `#readsSetupTableBlockFromRealBook`（2 行） |
| E-3 ファイル内レコードレイアウト数 | ✅（**#21**） | ✅ | ✅（**#21** で実 `.xlsx` 経路も） | 0: `#readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook`（ファイル系）・`#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook`（メッセージ系）／1: `#readsSetupFixedFileBlockFromRealBook`・`#readsMessageBlockFromRealBook`（いずれも `records.size()==1` をアサート）／複数: **`#readsMultipleRecordLayoutsFromOneFixedFileInRealBook`**（断片 2 件。Fake リーダ経路には `XlsFormatReaderTest#readRestoresMultipleRecordLayoutsInFixedFile` がある） |
| E-4 コンテナ内セクション数 | n/a | ✅ | ❌（**到達不能**） | 1: `#readsContainerAndSectionNamesFromRealBookAndSheetNames`。複数は `XlsFormatReader#read` が `Collections.singletonList(section)` を返すため構造上到達不能（§0.8-6） |

- **アサート済みのセルは 10 個ある。**内訳は E-1〜E-3 の 3 観点 × 0／1／複数 ＝ **9 組**（すべて実 `.xlsx` 経路）と、
  E-4(1) の 1 組。残るセルは E-4(複数)＝到達不能で空欄、E-4(0)＝n/a（コンテナは必ず 1 セクションを持つ）の 2 つで、
  合わせて 4 観点 × 3 多重度 ＝ 12 セルになる。
  #21 の当初実装では E-3(複数) を Fake リーダ経路の ✅（#18 判定）に依拠して空けていたが、
  **辺①の担保は実ファイル経路で揃えるという #20 の基準に合わせ、コーディネータの指示で追加した**。
- **E-3(複数) は `MessageDataBlock` 経路では到達不能**である。本体 `MessageParser` が生成する
  `FixedLengthFileParser` は `onReadingValues` を上書きし、先頭セルが非空でも新しい断片を作らないため、
  2 つ目の名前行が値行として吸収される（`issues.md` **XLS-15**。根拠テスト
  `XlsFormatReaderInvalidInputTest#absorbsSecondNameRowAsDataRowInMessageBodyInRealBook`）。
  E-3 は（観点, 多重度）単位で数えるため、ファイル系での担保をもって E-3(複数) は ✅ とする。

**軸F（#21。6 ケース中 5 ケースを新規担保。F1-05 は #16 で担保済み）**

| 要素 | 判定 | 担保テストメソッド（`XlsFormatReaderInvalidInputTest#`。F1-05 のみ別クラス） | 観測した挙動 |
|---|---|---|---|
| F1-01 シート不在 | ✅（**#21**） | `failsWithSheetNotFoundWhenSheetIsAbsentFromRealBook` | `IllegalArgumentException: sheet not found. path=[...] sheet=[...]`（原因例外なし） |
| F1-02 ブック破損 | ✅（**#21**） | `failsWithGenericRuntimeExceptionWhenWorkbookIsBroken` | `java.lang.RuntimeException: test data file open failed.` ＋ POI の `IllegalArgumentException`。ファイル名はどのメッセージにも出ない（`issues.md` **XLS-14**） |
| F1-03 未知のデータタイプ名 | ✅（**#21**。#18 は 🔺） | `ignoresBlockWhoseMarkerHasUnknownDataTypeNameInRealBook`／`dropsMarkerWhoseGroupIdIsNotBracketedInRealBook`（**#34 で改称**） | 例外にならず継続。未知名はブロックごと消える（**XLS-10**）。既知名＋余分な文字も **#34 以降は消える**（**XLS-11**） |
| F1-04 マーカーカラム欠落 | ✅（**#21**） | `readsMarkerColumnWithoutBracketsAsOrdinaryDataColumnInRealBook`／`dropsFirstFieldWhenSendSyncMetaColumnIsMissingInRealBook` | 例外にならず継続。角括弧なしの列はデータ列になる。送信同期のメタ列欠落は先頭フィールドと値を落とす（**XLS-13**） |
| F1-05 カラム名重複 | ✅（**#21** で実 `.xlsx` 経路も） | `deduplicatesDuplicateColumnNamesWithWarningInListMapFromRealBook`／`deduplicatesDuplicateColumnNamesWithWarningInTableFromRealBook`（**#21**）。#16 の `XlsFormatReaderTest#readListMapWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins` ほか 3 件は **Fake リーダ経路** | 後勝ちで除去＋WARN ログ 1 件（ブック名・シート名・ブロック識別子・カラム名・採用列番号を含む）。実 `.xlsx` 経路でも Fake 経路と同じ結果になることを実測 |
| F1-06 行と列の数の不一致 | ✅（**#21**） | `padsShortDataRowAndDropsCellsBeyondColumnRowInRealBook`／`padsShortValueRowAndDropsCellsBeyondNameRowInFixedFileInRealBook`／`failsWhenLengthRowIsShorterThanNameRowInRealBook`／`failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook`／`failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook`／`failsWhenTypeRowIsShorterThanNameRowInRealBook`／`failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook` | **値行**の不足は空文字埋め・超過は切り捨て（例外にならない。**XLS-12**）。**名前行・型行・長さ行**の不整合は本体パーサが例外で弾く |
| （軸F の要素ではない補足） | — | `absorbsSecondNameRowAsDataRowInMessageBodyInRealBook` | `MESSAGE` 本文に 2 つ目のレコードレイアウトを書くと、名前行・型行・長さ行がすべて 1 つ目の値行として吸収される（**XLS-15**。E-3(複数) がメッセージ系で到達不能である根拠） |

- 末尾 4 メソッド（`failsWhenFixedFileNameRowHasOnlyRecordTypeCell...`／`failsWhenMessageNameRowHasOnlyRecordTypeCell...`／
  `failsWhenTypeRowIsShorterThanNameRow...`／
  `failsWhenTypeCellIsBlankInMiddleOfTypeRow...`）は、軸C の **C-17／C-20 が到達不能である根拠**を
  実行可能にするテストでもある（`issues.md` の「到達不能」表が参照している）。

<a id="s1-3"></a>

### 1.3 辺① 未担保一覧（#19〜#21 が埋める対象）

**計上単位**（4 辺すべてでこの規則に従う）:

- 軸A・軸B・軸C は **軸要素 1 件を 1 件**と数える。同一要素で「値あり」「省略」の両方が欠けていても 1 件とし、
  欠けている状態は要素名に併記する。
- 軸D・軸F は **ケース 1 件を 1 件**と数える。
- 軸E は **（観点, 多重度）の組 1 件を 1 件**と数える。
- 🔺（弱い担保のみ）の要素は未担保として計上する。正式担保は ✅ のみ。

**状態**の分類（**`担保済み` を #20 で追加した**）:

- `要追加` — テストを書く対象。**#19／#20／#21 の完了により 0 件になった。**
- `担保済み` — #19／#20／#21 が実 `.xlsx` を入力とするテストで埋めた。担保テストメソッド名を併記する。
- `到達不能` — 構造上その状態が生成されない。根拠を併記する。テストは書かず、理由付きで空欄に残す。
- `対象外` — 辺の担当クラスの関心事ではなく、上位層の関心事であるもの。理由を併記する。
  §5.1 では `対象外（上位層で担保済み）` と表記しているが、**辺③の F3-02 についてはこの表記は正確でない**
  （上位層の既存テストが通すのは XLS→YAML の経路だけで、`.xlsx` を出力側とする衝突は未担保）。
  §3.1-2／§3.3 では `対象外（衝突検査は上位層）` へ改めた。詳細は §0.8-5 の訂正欄。

**本表は #21 の実測結果に合わせて更新した（2026-08-12）。** #18 時点は「要追加 38 ／ 到達不能 3」、
#20 完了時点は「要追加 11 ／ 担保済み 22 ／ 到達不能 8」であった。#21 が残る 11 件（C 4 件・E 2 件・F 5 件）を
埋めたため、**辺①の未担保は 0 件**になった。分類を変更した行には根拠（`coverage/issues.md` の
課題 ID）を併記してある。#18 時点の分類は各行の「#18」列に残した。

| 軸 | 未担保要素 | #18 の状態 | #21 後の状態 | 件数 |
|---|---|---|---|---|
| A | A-04 `EXPECTED_COMPLETED`／A-07 `EXPECTED_FIXED`／A-09 `EXPECTED_VARIABLE` | 要追加 | **担保済み（#20）** — 順に `XlsFormatReaderRealFileTest#readsExpectedCompletedTableBlockFromRealBook`／`#readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook`／`#readsExpectedVariableFileBlockWithGroupIdFromRealBook` | 3 |
| A | A-01 `DEFAULT` — `TestCoreReaderAdapter` が DEFAULT ブロックをスキップするためリーダ経路で生成されない（§0.8-7） | 到達不能 | 到達不能（変更なし） | 1 |
| B | （なし） | — | — | — | 0 |
| C | C-06 groupId 省略(`""`) | 要追加 | **担保済み（#20）** — `#readsSetupTableBlockFromRealBook` ほか 2 件が `""` を直接アサート | 1 |
| C | C-08 columnNames 空 | 要追加 | **担保済み（#20 修正ラウンド）** — `#readsEmptyColumnNamesFromMarkerOnlyTableInRealBook`／`#readsEmptyColumnNamesFromMarkerOnlyListMapInRealBook`。マーカー列だけのブロックで到達する（`issues.md` **XLS-08**）。**#20 の当初分類では「軸E の 0 件と重なる」として #21 送りにしていたが誤り**（軸E の 4 観点 E-1〜E-4 に「列名 0 件」に対応する要素は無い）。#18 §1.3 は本要素を「要追加」に列挙しただけで、どのタスクが埋めるかは指定していない | 1 |
| C | C-09 rows 空／C-12 FileDataBlock.records 空／C-15 MessageDataBlock.records 空／C-18 RecordLayout.rows 空 | 要追加 | **担保済み（#21）** — `XlsFormatReaderRealFileTest#readsEmptyRowsFromTableWithoutDataRowsInRealBook`／`#readsEmptyRowsFromListMapWithoutDataRowsInRealBook`（C-09 は 2 経路）／`#readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook`（C-12）／`#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook`（C-15）／`#readsEmptyRowsFromRecordLayoutWithoutValueRowsInRealBook`（C-18）。いずれも例外にならず空コレクションになることを実測して固定した | 4 |
| C | C-11 FileDataBlock.directives 空／C-13 MessageDataBlock.directives 空 | 要追加 | **到達不能** — 本体 `DataFile` のコンストラクタが `file-type` を必ず注入する（`issues.md` **XLS-07**）。根拠は `#readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook`／`#readsAllFourSendSyncMessageTypesFromRealBook` がテストで示す | 2 |
| C | C-16 recordType 省略(`null`) | 要追加 | **担保済み（#25.5 で到達不能から移した）** — #21 時点は「実 `.xlsx` 経路では空セルが `""` として読まれる」ため到達不能としていたが、その `""` 自体が不具合であり（`issues.md` **XLS-06**）、#25.5 が `null` を入れるよう直した（5721ecd）。根拠は `#readsOmittedRecordTypeAsNullFromRealBook` | 1 |
| C | C-17 RecordLayout.fields 空 | 要追加 | **到達不能** — 名前行が 2 列未満だと本体 `DataFileParser` が失敗する（`issues.md`「到達不能」表）。根拠は **#21 が追加した** `XlsFormatReaderInvalidInputTest#failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook` と `#failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook` がテストで示す。**#20 の当初分類では #21 送りにしていたが誤り**（軸E の 0 件ではない）。#18 §1.3 は本要素を「要追加」に列挙しただけで、どのタスクが埋めるかは指定していない | 1 |
| C | C-20 FieldDef.type 省略(`null`) | 要追加 | **到達不能** — 型の欠落を本体パーサが 2 通りの機構で弾く（`issues.md`「到達不能」表）。根拠は **#21 が追加した** `XlsFormatReaderInvalidInputTest#failsWhenTypeRowIsShorterThanNameRowInRealBook`（機構①）／`#failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook`（機構②）がテストで示す | 1 |
| C | C-02 sections 空 — `XlsFormatReader#read` が `Collections.singletonList(section)` を返すため sections は常に 1 件（§0.8-6） | 到達不能 | 到達不能（変更なし） | 1 |
| D | §0.5 の 8 ケース（D1-01／D1-05／D1-12〜D1-17） | 要追加 | **担保済み（#19）** — `XlsFormatReaderCellTypeTest` 10 件（8 ケース＋空白セル＋行途中の不在セル。`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatReaderCellTypeTest.java` → **10**）。Excel 保存物との突き合わせは `XlsReferenceFixtureTest` 2 件（`issues.md` XLS-01／XLS-04／XLS-05） | 8 |
| E | E-2(0 件)／E-3(0 件) | 要追加 | **担保済み（#21）** — E-2(0) は C-09 の 2 件と同じ入力、E-3(0) は C-12／C-15 と同じ入力（上記 C 行のテストメソッド） | 2 |
| E | E-4(複数) — `XlsFormatReader#read` が 1 シート単位 API（§0.8-6） | 到達不能 | 到達不能（変更なし） | 1 |
| F | F1-01 シート不在／F1-02 ブック破損／F1-03 未知データタイプ名（🔺 `readIgnoresDataTypePrefixedLineWithoutMarker` のみ）／F1-04 マーカーカラム欠落／F1-06 行列数不一致 | 要追加 | **担保済み（#21）** — `XlsFormatReaderInvalidInputTest` 16 件（内訳: 本 5 ケースが 9 件、F1-05 の実 `.xlsx` 担保が 2 件、C-17／C-20 到達不能の根拠が 4 件、XLS-15 の根拠が 1 件。§1.2-2 の軸F 表に要素別の担保テストメソッドを記載）。継続する異常系で見つけた課題は `issues.md` **XLS-10〜XLS-15** | 5 |
| **合計** | | **要追加 29 ／ 到達不能 3** | **要追加 0 ／ 担保済み 25 ／ 到達不能 7 ／ 対象外 0**（#25.5 で C-16 が到達不能 → 担保済みへ移った。#21 完了時点は 担保済み 24 ／ 到達不能 8） | **32** |

検算: 軸D を 17 ケース → 8 ケースへ絞り込んだ（2026-08-13・ユーザー確定）ため、
本表の総計は 41 → **32**、要追加は 38 → **29**、担保済みは 33 → **24** へ 9 ずつ減った。
右列の内訳 24 ＋ 8 ＝ 32 が総計と一致する。

**合計の検算**（表の「件数」列を上から順に足す。**#25.5 で取り直した**）:

- 担保済み: A 3 ＋ C-06 1 ＋ C-08 1 ＋ C-09/12/15/18 4 ＋ **C-16 1** ＋ D 8 ＋ E-2(0)/E-3(0) 2 ＋ F 5 ＝ **25**
- 到達不能: A-01 1 ＋ C-11/C-13 2 ＋ C-17 1 ＋ C-20 1 ＋ C-02 1 ＋ E-4(複数) 1 ＝ **7**
- 要追加: **0**
- 総計: 25 ＋ 7 ＋ 0 ＝ **32**（B は 0 件）

導出コマンド（表の「件数」列を機械的に足す。**そのまま実行すれば 32 になる**）:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter/.rn/ntf-test-data-converter/coverage
awk '/^\| 軸 \| 未担保要素 \| #18 の状態 \| #21 後の状態 \| 件数 \|/,/^\| \*\*合計\*\*/' inventory.md \
  | grep -vE '^\| (軸|---|\*\*合計)' \
  | awk -F'|' '{gsub(/[^0-9]/,"",$(NF-1)); s+=$(NF-1)} END {print s}'
```

**この検算ブロックは #25.5 で 2 か所直した。** 直前まで「D 17 ／ 担保済み 33 ／ 総計 41」と書かれていたが、
これは軸D を 17 ケース → 8 ケースへ絞り込んだ（2026-08-13）際に上の表と合計行だけを直し、
この検算ブロックを取り残していたものである（同じ節の中で 32 と 41 が併存していた）。
残る 1 か所は C-16 の移動（到達不能 → 担保済み）である。

**#21 が埋めた 11 件の内訳**（#20 が送った対象。すべて完了した）:

| 軸 | 要素 | 担保テストメソッド |
|---|---|---|
| C | C-09 `ColumnRowDataBlock.rows` 空 | `XlsFormatReaderRealFileTest#readsEmptyRowsFromTableWithoutDataRowsInRealBook`／`#readsEmptyRowsFromListMapWithoutDataRowsInRealBook`（E-2(0 件) と同じ入力） |
| C | C-12 `FileDataBlock.records` 空 | `#readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook`（E-3(0 件) と同じ入力） |
| C | C-15 `MessageDataBlock.records` 空 | `#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook`（E-3(0 件) と同じ入力） |
| C | C-18 `RecordLayout.rows` 空 | `#readsEmptyRowsFromRecordLayoutWithoutValueRowsInRealBook` |
| E | E-2(0 件)／E-3(0 件) | 上記 C-09／C-12・C-15 と同じ 2 件・3 件 |
| F | F1-01／F1-02／F1-03／F1-04／F1-06 | `XlsFormatReaderInvalidInputTest` 16 件（§1.2-2 の軸F 表） |

**#21 が追加で埋めた 2 件（上記 11 件の外）**: いずれも #18 の棚卸しが **Fake リーダ経路の担保をもって ✅**
と判定していたため §1.3 の未担保一覧（41 件）には現れないが、**辺①の担保は実ファイル経路で揃える**という
#20 の基準に照らすと空欄だったものである。コーディネータの指示により追加した。本表の件数（41）と合計には影響しない。

| 要素 | 実 `.xlsx` 経路の担保（#21 で追加） | #18 が ✅ とした Fake 経路の担保 |
|---|---|---|
| E-3(複数) | `XlsFormatReaderRealFileTest#readsMultipleRecordLayoutsFromOneFixedFileInRealBook` | `XlsFormatReaderTest#readRestoresMultipleRecordLayoutsInFixedFile` |
| F1-05 カラム名重複 | `XlsFormatReaderInvalidInputTest#deduplicatesDuplicateColumnNamesWithWarningInListMapFromRealBook`／`#deduplicatesDuplicateColumnNamesWithWarningInTableFromRealBook` | `XlsFormatReaderTest#readListMapWithDuplicateColumnEmitsWarnAndDeduplicatesLastWins` ほか 3 件（#16） |

- **C-08 と C-17 は #21 のスコープから外れる**（前者は #20 で担保済み、後者は到達不能）。
  #18 §1.3 の「要追加 11（C）」＝ C-06／C-08／C-09／C-11／C-12／C-13／C-15／C-16／C-17／C-18／C-20 の
  内訳は、#20 の実測により次の 3 群に分かれる。

  | 群 | 要素 | 件数 |
  |---|---|---|
  | 担保済み（#20） | C-06／C-08 | 2 |
  | 到達不能 | C-11／C-13／C-16／C-17／C-20 | 5 |
  | 担保済み（**#21**。#20 の時点では「要追加」） | C-09／C-12／C-15／C-18 | 4 |
  | **合計** | | **11** |

  2 ＋ 5 ＋ 4 ＝ 11。末尾の 4 件が上表の C 4 件であり、#21 で担保済みになった。

**#18 時点の「特に大きな空欄」**（軸D 全滅。当時の定義では 17 ケース、現在の定義では 8 ケース）は
#19 で解消し、#20 後に残っていた最大の空欄
（軸F の 5 ケース）は #21 で解消した。**辺①の「要追加」は 0 件**である。残る 8 件は到達不能で、
うち 5 件（C-11／C-13／C-16／C-17／C-20）は根拠テストを持ち、3 件（A-01／C-02／E-4(複数)）は
`XlsFormatReader#read` と `TestCoreReaderAdapter` の構造そのものが根拠である。

**ただし「未担保 0 件」は本書の計上単位（§1.3 冒頭）での話である。** 次の 1 点は空欄として残る。

- 継続する異常系（F1-03／F1-04／F1-06 の一部）で「WARN が出ないこと」はアサートしていない
  （`issues.md`「未確認（#21）」）。

なお #18 の棚卸しが Fake リーダ経路の担保をもって ✅ としていた軸E の要素のうち、実 `.xlsx` 経路で
空欄だったのは **E-3(複数) の 1 件だけ**である（#21 で追加。§1.2-2 の軸E 表）。
E-1(0/1/複数)・E-2(1/複数)・E-3(1)・E-4(1) は `XlsFormatReaderRealFileTest` が実 `.xlsx` で
アサートしていることを 1 件ずつ確認した。

---

<a id="s2"></a>

## 2. 辺② YAML→中間モデル（`YamlFormatReaderTest` 20 件）

### 2.1 テストメソッド → 担保する軸要素

**16 行目の軸F 欄を訂正した（2026-08-14・#24 のレビュー指摘による訂正）。** 本表は #18 時点の事実として
書き換えない方針だが、当該欄の「送信系必須の `group_id`」という記述は**一次情報（本体スキーマ）に反していた**
ため訂正した。`$defs.group_message_data.required` も `$defs.expected_request_message_data.required` も
`["id","records"]` だけで `group_id` を要求していない（`issues.md` **YML-02** に再現コマンドつきで記録）。
これに伴い F2-04（必須構造の欠落）の #18 時点の担保は **🔺 も無し**（未担保）になる。
判定の増減ではなく誤記の訂正であるため、他の行と §2.3 の件数は変わらない。

| # | テストメソッド | 軸A | 軸B | 軸C | 軸D | 軸E | 軸F |
|---|---|---|---|---|---|---|---|
| 1 | `readTable_setup_mapsUppercaseNameAndColumnsWithRawValues` | A-02 | B-1 | C-05, C-06(省略=`""`), C-07, C-08, C-09 | — ※Map 値レベルで `${}`／null／`""` | E-1(1), E-2(複数=2) | — |
| 2 | `readTable_expectedWithGroup_keepsRawGroupIdAndCreatesBlockPerGroup`（**#34 で改称**） | A-03 | B-1 | C-05, C-06(値あり), C-09 | — | E-1(複数=2) | — |
| 3 | `readTable_completed_mapsExpectedCompletedType` | A-04 | B-1 | C-05, C-07 | — | E-1(1) | — |
| 4 | `readListMap_preservesYamlColumnOrderExcludesMarkersAndKeepsNull` | A-05 | B-2 | C-05, C-07, C-08(YAML 順), C-09 | — ※マーカーカラム `[ignore]` 除外・null 保持 | E-2(複数=2) | — |
| 5 | `readFile_fixed_mapsRawFieldDefsAndValues` | A-06 | B-3 | C-05, C-07, C-10(FIXED), C-11(値あり), C-12, C-16(値あり), C-17, C-18, C-19, C-20, C-21(値あり＋省略) | — ※`${}`／`""` | E-3(複数=2), E-2(複数=2) | — |
| 6 | `readFile_variable_mapsVariableTypeWithNullLengths` | A-09 | B-3 | C-05, C-10(VARIABLE), C-17, C-18, C-19, C-20, C-21(省略) | — | E-3(1) | — |
| 7 | `readFile_expectedFixedWithMultipleGroups_mapsExpectedFixedAndDedupesGroups` | A-07 | B-3 | C-05, C-06(値あり), C-07 | — | E-1(複数=3) | — |
| 8 | `readFile_setupVariable_mapsSetupVariableType` | A-08 | B-3 | C-05, C-10(VARIABLE) | — | E-1(1) | — |
| 9 | `readFile_recordTypeOmitted_keepsNullRecordType` | A-06 | B-3 | **C-16(省略=null)** ✅ | — | E-3(1) | — |
| 10 | `readFile_recordTypeDefault_normalizedToNull` | A-08 | B-3 | C-16(`"Default"`→null) | — ※特殊値の正規化 | E-3(1) | — |
| 11 | `readMessage_mapsRawFwHeaderAndKeepsFwHeaderNamedRecord`<br>（**2026-08-18・YML-03 の解消で改名**。旧名 `#readMessage_mapsRawFwHeaderAndExcludesFwHeaderRecord`。担保する軸要素は不変） | A-10 | B-4 | C-05, C-06(省略), C-07, C-14(値あり), C-15, C-16, C-17, C-18, C-19, C-20, C-21 | — ※`${}` | E-3(1) | — |
| 12 | `readMessage_emptyBody_isStillMapped` | A-10 | B-4 | C-07, **C-14(空)**, **C-15(空)** | — | **E-3(0)** ✅ | — |
| 13 | `readMessage_nullContent_isSkipped` | — | — | C-04(空) | — | E-1(0) | ※器が null を返す場合のスキップ |
| 14 | `readSendSync_groupsByRawValueKeepsRawGroupIdAndNoField`（**#34 で改称**） | A-11 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-17, C-18, C-19, C-20, C-21 | — ※`${}`・`no` フィールド保持 | E-1(複数=3) | — |
| 15 | `readSendSync_allFourTypesAreRecognized` | A-11, A-12, A-13, A-14 | B-4 | C-05 | — | E-1(複数=4) | — |
| 16 | `readSendSync_entryWithoutGroupId_isReadAsDefaultGroup`（#25.5 で `readSendSync_entryWithoutGroupId_isDropped` から改名） | A-14 | B-4 | C-07 | — | E-1(1) | —（軸F の要素ではない。**スキーマは送信系に `group_id` を要求していない**ため「必須構造の欠落」ではない。#25.5 までは「仕様内の入力が黙って drop される現状挙動の固定」だったが、**#25.5 でデフォルトグループとして読むよう直した**ため、現在はデフォルトグループの担保である。`issues.md` **YML-02**・36e94a4） |
| 17 | `read_mixedSections_keepsDescriptionOrderAndIgnoresUnknownKeys` | A-02, A-10 | B-1, B-4 | C-04 | — | E-1(複数=2) | **F2-03** ✅（未知キー無視） |
| 18 | `read_namesContainerAndSectionByResourceName` | — | — | C-01, C-02(1件), C-03, **C-04(空)** | — | E-1(0), E-4(1) | 🔺**F2-05** に近い（空 Map。実ファイルではない） |
| 19 | `read_containerCountMismatch_failsFast` | A-06 | B-3 | — | — | — | ✅ 器↔原文の件数不整合 → `IllegalStateException` |
| 20 | `read_fragmentRecordMismatch_failsFast` | A-06 | B-3 | — | — | — | ✅ 器の断片構造↔原文レコード不整合 → `IllegalStateException` |

<a id="s2-1-2"></a>

### 2.1-2 #24 が追加したテストクラスの担保（2026-08-14 追記）

**本節は #24 で新設した。** §2.1 は「`YamlFormatReaderTest` 20 件」を対象とした #18 時点の事実であり
書き換えていない。ここには #24 が追加した**実 `.yaml` を入力とするテストクラス**の担保だけを記す。

| テストクラス | 件数 | 導出コマンド | 入力 |
|---|---|---|---|
| `YamlFormatReaderScalarTest` | **28**（#36 で導き直した。空エントリの読み飛ばしを押さえる `skipsRowWhoseValuesAreAllEmpty` を 1 件追加した） | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderScalarTest.java` | `YamlFixture` が書き出した実 `.yaml` |
| `YamlFormatReaderInvalidInputTest` | **32**（#35 で導き直した。宣言値 31 は `226d0f8` 時点の値で、その後 `d737815` ＋1・`b19a236` ＋1・`96b5aea`（#31）＋1 で 34 になり、**#35 の `@Ignore` 2 件削除で 32** になった） | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java` | 同上（軸F の 8 件のうち **7 件**は意図的にスキーマ違反・不正 YAML にした入力で、**1 件（F2-05 `readsEmptyFileAsContainerWithoutBlocks`）は空ファイル**＝スキーマ違反でも不正 YAML でもない。**区間別は #35 で導き直して 8 ／ 2 ／ 22 である**（軸F 8 件／ローダの他の失敗経路 2 件／**スキーマを通る仕様内の入力の掃引 22 件**）。#31 の実測は 8 ／ 2 ／ 24 で、**#35 が掃引側から `@Ignore` 2 件を削除して 22 になった**。「軸F の 8 件」「ローダ 2 件」は #24 当時から変わっていない） |
| `YamlFormatReaderRealFileTest` | 24 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderRealFileTest.java` | 同上 |

**件数を更新した（2026-08-14・#24 のレビュー指摘の反映で 9 件追加）。** 内訳は
`YamlFormatReaderScalarTest` ＋4（軸D の経路差確認。下の「別経路での確認」表）、
`YamlFormatReaderRealFileTest` ＋5（C-14 実ファイル経路・`FieldDef.length` の integer 記法・
送信系の `directives`・YML-02・YML-03）。

**件数をさらに更新した（2026-08-14・2 巡目レビュー指摘の反映＝修正ラウンド 2 で 16 件追加）。** 内訳は
`YamlFormatReaderInvalidInputTest` ＋15（掃引で見つけた `issues.md` **YML-04**〜**YML-08** の固定。
下の「開示」の掃引表を参照）、`YamlFormatReaderRealFileTest` ＋1
（`normalizeRecordType` の小文字 `"default"` 分岐。下の「開示」1 点目）。
**この 16 件は軸A〜F のどの要素にも新しい担保を与えないため、§2.3 の件数は動かない。**

**件数をさらに更新した（2026-08-14・3 巡目レビュー指摘の反映＝修正ラウンド 3 で 1 件追加）。** 内訳は
`YamlFormatReaderRealFileTest` ＋1（`reordersBlocksByFirstAppearanceOfGroupIdFromRealYaml`。
掃引項目 24 で見つけた `issues.md` **YML-09** の固定。同クラスは修正ラウンド 2 の時点で 17 件だった
——`git show b26b5a7:src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderRealFileTest.java | grep -c '^    @Test'` → **17**）。
**この 1 件も軸A〜F のどの要素にも新しい担保を与えないため、§2.3 の件数は動かない。**

**件数をさらに更新した（2026-08-14・QA レビュー指摘の反映で 2 件追加）。** 内訳は
`YamlFormatReaderInvalidInputTest` ＋2（掃引項目 27 で見つけた `issues.md` **YML-10** の固定＝
`dropsValueWhenTableColumnNamesDifferOnlyByCase` と、その対比 `keepsOriginalColumnCaseInListMap`）。
**この 2 件も軸A〜F のどの要素にも新しい担保を与えないため、§2.3 の件数は動かない。**

**件数をさらに更新した（2026-08-14・Verification レビュー指摘の反映で 5 件追加）。** 内訳は
`YamlFormatReaderInvalidInputTest` ＋2（掃引項目 28 ＝ `issues.md` **YML-11** の固定）、
`YamlFormatReaderRealFileTest` ＋3（生存していた変異 3 件を閉じるもの＝
`preservesListMapColumnOrderAndExcludesMarkerFromRealYaml` ／ `keepsFwHeaderNamedRecordInFileFromRealYaml` ／
`stringifiesNonStringDirectiveValuesFromRealYaml`）。**`readsAllThirteenDataTypesFromRealYaml` は
件数を増やさずフィクスチャのセクション順を定義順の逆に変えた**（記述順の保持を固定するため）。
このうち §2.3 の件数を動かすものは無い（`stringifiesNonStringDirectiveValuesFromRealYaml` は
既に ✅ の C-11(値あり) を非文字列記法で確かめたもの、
`preservesListMapColumnOrderAndExcludesMarkerFromRealYaml` は既に ✅ の C-08／C-09 を
カラム順とマーカー除外の観点で確かめたものである）。

**件数をさらに更新した（2026-08-14・QA／Craft 再レビュー指摘の反映で 2 件追加）。** 内訳は
`YamlFormatReaderInvalidInputTest` ＋1（`fillsEmptyRecordFragmentRowWithEmptyStringIndistinguishableFromWrittenOne`。
D2-11 の担保の限界＝ YML-05 を実行可能な形にしたもの）、`YamlFormatReaderRealFileTest` ＋1
（`readsInjectedDirectivesEvenWhenDirectivesAreOmittedInVariableFile`。可変長では `field-separator` も
注入されるという Javadoc の「実測」記述をテストで固定したもの）。**どちらも §2.3 の件数は動かさない。**

**件数をさらに更新した（2026-08-14・Verification 再レビュー指摘の反映で 3 件追加）。** 内訳は
`YamlFormatReaderRealFileTest` ＋1（`dropsFwHeaderNamedRecordFromSendSyncInRealYaml`。送信系の
FW_HEADER 除外が変異で生存していたため。**#25.5 で仕様どおりの期待値を書いた
`keepsFwHeaderNamedRecordInSendSyncFromRealYaml`（**2026-08-18 に `@Ignore` を外し緑**）へ
置き換えた**ので、この名前のテストは現在は存在しない。件数は 1 のままである）、`YamlFormatReaderInvalidInputTest` ＋2
（`failsWhenYamlRootIsNotMapping` ／ `failsWhenSameKeyAppearsTwiceInOneMapping`。ローダの他の失敗経路）。
**どれも §2.3 の件数は動かさない**（前 2 件は軸F の 5 ケースに属さないローダの分岐、
1 件は経路差の固定である）。あわせて既存テストへアサートを 3 か所足した
（FW_HEADER 名レコードの種別／ディレクティブ件数／`length` 省略＝`null`）。

**件数をさらに更新した（2026-08-14・3 巡目レビュー指摘の反映で 1 件追加）。** 内訳は
`YamlFormatReaderInvalidInputTest` ＋1（`readsFieldSeparatorWrittenAsEscapedTabNotation`。
YML-08 の field-separator だけ「通る側」のテストが無く、Javadoc の断定を実行可能な形にしていなかったため）。
**§2.3 の件数は動かさない。** あわせて既存テストへアサートを 4 か所足した
（`DataType` の総数 14／YML-04・YML-05 の代表テストの警告 0 件／`YamlFixture#blocks` のセクション数）。

3 クラスとも `new YamlFormatReader().read(...)` を本番配線で呼ぶ。`YamlFormatReaderTest` 20 件が
`loadRawMap` を in-memory `Map` へ差し替えて**スカラー解決もスキーマ検証も通らない**のに対し、
本 3 クラスはその区間を実行する（§0.8-3）。

**軸D（`YamlFormatReaderScalarTest`。§0.5 の 12 ケース）**

| 要素 | 担保テストメソッド | 観測した値 |
|---|---|---|
| D2-01 引用符なし文字列 | `readsUnquotedStringAsIs` | `abc` → `"abc"` |
| D2-02 引用符あり | `readsDoubleQuotedStringWithoutQuotes` ／ `readsSingleQuotedStringWithoutQuotes` | `"abc"` / `'abc'` → `"abc"`（引用の別は残らない） |
| D2-03 引用符付き数値 | `readsQuotedNumberAsString` | `"123"` → `"123"` |
| D2-04 引用符付き末尾ゼロ小数 | `readsQuotedTrailingZeroDecimalAsString` | `"1.50"` → `"1.50"` |
| D2-05 真偽値に見える文字列 | `readsQuotedTrueAsString` ／ `readsUppercaseTrueAsString` ／ `readsYesAsString` | `"true"` / `TRUE` / `yes` → いずれも記法どおりの文字列 |
| D2-06 NULL | `readsUnquotedNullAsJavaNull` ／ `readsOmittedValueAsJavaNull` | `null`（引用符なし）・値なし（`- V:`）→ **Java `null`** |
| D2-07 NULL に見える文字列 | `readsQuotedNullAsString` ／ `readsTildeAsString` ／ `readsUppercaseNullAsString` | `"null"` / `~` / `NULL` → **いずれも文字列**（`issues.md` **YML-01**） |
| D2-08 日付・日時風文字列 | `readsDateLikeStringAsIs` ／ `readsDateTimeLikeStringAsIs` | `2026-08-07` / `2026-08-07T12:34:56` → 記法どおりの文字列 |
| D2-09 複数行 | `readsLiteralBlockScalarKeepingNewlines` ／ `readsFoldedBlockScalarFoldingNewlinesIntoSpaces` | `\|` → `"l1\nl2\n"`、`>` → `"l1 l2\n"`（いずれも末尾に改行が付く） |
| D2-10 先頭ゼロ・非 JSON 数値記法 | `readsLeadingZeroNumberAsString` ／ `readsHexNotationAsString` | `007` / `0x1F` → 記法どおりの文字列 |
| D2-11 空文字・前後空白 | `readsEmptyStringAsIs` ／ `readsSurroundingWhitespacePreserved` | `""` → `""`、`"  pad  "` → 前後空白を保つ |
| D2-12 特殊文字を含む文字列 | `readsColonContainingStringAsIs` ／ `readsHashContainingStringAsIs` | `"a: b"` / `"a #b"` → 1 値として入る |

12 ケースすべてを ✅ とする。#18 が 🔺 としていた D2-02／D2-03／D2-06／D2-07（往復テスト経由。§0.8-8）は
本クラスの直接テストで ✅ になった。**D2-06 と D2-07 の分かれ方（`null`・値なし だけが Java `null` になり、
`~`・`NULL`・`"null"` は文字列になる）が本タスクで固定した中心の事実である。**

**軸D の測定経路と、別経路での確認（2026-08-14・レビュー指摘の反映で追加）**

上の 12 ケースは**すべて `setup_tables` の `rows` で測っている**（`YamlFormatReaderScalarTest#readValue` が
常に `setup_tables` へ値を置く）。`YamlFormatReader` の行値の取り出しはテーブル／LIST_MAP／レコード断片の
3 系統があり、スキーマも別パスで型を課す（テーブル・LIST_MAP は
`properties.rows.items.additionalProperties.type`、レコード断片は
`$defs.record_fragment.properties.rows.items.items.type`）。したがって「12 ケースの結果が他の 2 系統でも
同じか」は 12 ケースのテストだけからは言えない。**12 ケース × 3 経路には広げず**、`null` と空文字の
2 ケースだけを別経路で測った（**軸D の 12 ケース定義は変えていない**。下表は同一ケースを別経路で
確認したものであり、新しい軸要素ではない）。

| 元のケース | LIST_MAP 経路（`list_maps`） | レコード断片経路（`record_fragment.rows`） | 経路差 |
|---|---|---|---|
| D2-06 `null`（引用符なし） | `readsUnquotedNullAsJavaNullInListMapPath` → Java `null` | `readsUnquotedNullAsJavaNullInRecordFragmentPath` → Java `null` | **無し**（`setup_tables` 経路と同じ） |
| D2-11 空文字 `""` | `readsEmptyStringAsIsInListMapPath` → `""` | `readsEmptyStringAsIsInRecordFragmentPath` → `""`（**固定できる性質が弱い**。下の但し書き） | **無し**（同上） |

**但し書き（レコード断片経路の空文字）**: この経路では、行の要素数が `fields` の件数に足りないときに
欠けた位置が**空文字で埋められる**（**YML-05**）。したがって「書かれた `""`」と「書かれなかった位置」が
中間モデル上で区別できず、**`rows: - [""]` と `rows: - []` は同じ結果になる**
（実測。この事実自体を `YamlFormatReaderInvalidInputTest#fillsEmptyRecordFragmentRowWithEmptyStringIndistinguishableFromWrittenOne` で固定した）。
`readsEmptyStringAsIsInRecordFragmentPath` が固定できるのは「`""` は Java `null` にならない」ことまでで、
**「書いた空文字が保たれた」ことは示せない**。テーブル／LIST_MAP 経路では欠けたキーが `null` になるため
（`padsColumnMissingFromSecondRowWithNullInTable`）区別できる。この差はテストの Javadoc にも書いた。

残る 10 ケースを別経路で測っていないことは、下の「開示」に穴として記す。

**軸F（`YamlFormatReaderInvalidInputTest`。§0.7 の 5 ケース）**

| 要素 | 判定 | 担保テストメソッド | 観測した挙動 |
|---|---|---|---|
| F2-01 スキーマ違反 | ✅ | `failsWithSchemaValidationExceptionWhenFileTypeIsNotInEnum` ／ `failsWithSchemaValidationExceptionWhenFieldLengthDoesNotMatchPattern` | `YamlSchemaValidationException`。**違反のキーワードの集合と位置を、件数と集合を厳密にアサートする（報告順は `JsonSchema#validate` が返す `Set` の反復順が契約されていないため突き合わせない）** — 前者は `enum` 1 件・位置 `$.setup_files[0].type`、後者は `type` と `pattern` の 2 件（`length` が `anyOf` であり `"1a"` が両枝を外すため）で位置はいずれも `$.setup_files[0].records[0].fields[0].length`。**入力に、`rows` の値として仕様外とした引用符なしスカラー記法は使っていない**（`issues.md`「対象としない入力（辺②）」） |
| F2-02 YAML として不正 | ✅ | `failsWithParseErrorWhenYamlIsMalformed` | `IllegalStateException`（メッセージは `Failed to parse YAML file: <path>` で始まる）。原因例外は `YamlEngineException`。パースで止まるためスキーマ検証には到達しない |
| F2-03 未知のキー | ✅ | `failsWithSchemaValidationExceptionWhenTopLevelKeyIsUnknown` | `YamlSchemaValidationException`（`additionalProperties` 違反）。**in-memory 経路（`YamlFormatReaderTest#read_mixedSections_keepsDescriptionOrderAndIgnoresUnknownKeys`）が固定している「未知キーは無視」とは結果が異なる**。スキーマのルートが `additionalProperties: false` であるため、実ファイルでは読み込みごと失敗する |
| F2-04 必須構造の欠落 | ✅ | `failsWithSchemaValidationExceptionWhenRequiredRowsIsMissing` ／ `failsWithSchemaValidationExceptionWhenFieldsIsEmpty` ／ `failsWithSchemaValidationExceptionWhenFieldTypeIsMissing` | `YamlSchemaValidationException`（`required` / `minItems`）。後ろ 2 件は軸C の **C-17／C-20 が到達不能である根拠**でもある |
| F2-05 空ファイル | ✅ | `readsEmptyFileAsContainerWithoutBlocks` | 例外にならず、リソース名のコンテナ 1 件・セクション 1 件・ブロック 0 件になる |

**軸A（`YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml`）** — 1 ファイルに 11 セクションキー・
13 エントリを書き、`DEFAULT` を除く 13 種すべてが記述順に生成されることを `getDataType()` の並びでアサートする。
#18 時点で既に ✅ だった（in-memory 経路）が、**実 `.yaml` 経路でも ✅ になった**。
A-01 `DEFAULT` は到達不能のまま（§0.8-7）。

**軸B（`#readsFourBlockImplementationsFromOneRealYaml`）** — `TableDataBlock` / `ListMapBlock` /
`FileDataBlock` / `MessageDataBlock` の 4 種を 1 ファイルから生成し、実装クラスと識別子
（`T` / `lm` / `f.dat` / `RM01`）を突き合わせる。#18 時点で既に ✅ だったものを実 `.yaml` 経路でも通した。

**軸C（#18 時点から状態が変わったものだけ）**

| 要素 | #18 の判定 | #24 後 | 担保テストメソッド（`YamlFormatReaderRealFileTest#`）／根拠 |
|---|---|---|---|
| C-08 `columnNames` 空 | ❌ | ✅ | `readsEmptyColumnNamesAndRowsFromTableWithoutRows` ／ `readsEmptyColumnNamesAndRowsFromListMapWithoutRows`（`rows: []` で到達） |
| C-09 `rows` 空 | ❌ | ✅ | 同上（C-08 と同じ入力） |
| C-12 `FileDataBlock.records` 空 | ❌ | ✅ | `readsEmptyRecordsFromFixedFileWithoutRecords`（`records: []`。スキーマ `file_data` は `minItems: 0`） |
| C-18 `RecordLayout.rows` 空 | ❌ | ✅ | `readsEmptyRowsFromRecordLayoutWithoutRows` |
| C-13 `MessageDataBlock.directives` 値あり | ❌ | ✅ | `readsMessageDirectivesFromRealYaml` |
| C-14 `MessageDataBlock.fwHeaderFields` 値あり | ✅（in-memory のみ） | ✅（**実ファイル経路でも**） | `readsFwHeaderFieldsFromRealYaml`（`fw_header:` の 2 キーが記述順で入る。#24 のレビュー指摘の反映で追加） |
| C-21 `FieldDef.length` 値あり（integer 記法） | ✅（文字列記法のみ） | ✅（**integer 記法でも**） | `readsIntegerLengthNotationAsString`（`length: 10` はスキーマの `anyOf` 第 1 枝を通り、中間モデルには文字列 `"10"` が入る。#24 のレビュー指摘の反映で追加） |
| C-11 `FileDataBlock.directives` 空 | ❌ | **到達不能** | `issues.md` **XLS-07** と同じ器。根拠テスト `readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInFile` |
| C-13 `MessageDataBlock.directives` 空 | ❌ | **到達不能** | 同上。根拠テストは **2 つの生成経路それぞれ**にある — 受信メッセージ経路（`YamlFormatReader#addMessageBlocks`）が `readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInMessage`、送信系経路（`#addSendSyncBlocks`）が `readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInSendSync`（#24 のレビュー指摘の反映で後者を追加） |
| C-17 `RecordLayout.fields` 空 | ❌ | **到達不能** | スキーマ `$defs.record_fragment.properties.fields.minItems` ＝ 1。根拠テスト `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFieldsIsEmpty` |
| C-20 `FieldDef.type` 省略 | ❌ | **到達不能** | スキーマ `$defs.field_def.required` が `type` を必須とする。根拠テスト `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFieldTypeIsMissing` |

**軸E（#24。0 件・複数件を実 `.yaml` 経路で担保した）**

| 要素 | 0 件 | 1 件 | 複数件 | 担保テストメソッド（`YamlFormatReaderRealFileTest#`） |
|---|---|---|---|---|
| E-1 セクション内ブロック数 | ✅ | ✅ | ✅ | 0: `namesContainerAndSectionByResourceNameWithoutBlocks`／1: `readsEmptyColumnNamesAndRowsFromTableWithoutRows` ほか（ヘルパ `onlyBlock` が `blocks.size()==1` をアサート）／複数: `readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml`（3 件）・`readsAllThirteenDataTypesFromRealYaml`（13 件） |
| E-2 ブロック内行数 | ✅（**#24**） | ✅ | ✅ | 0: `readsEmptyColumnNamesAndRowsFromTableWithoutRows` ／ `readsEmptyColumnNamesAndRowsFromListMapWithoutRows`／1: `readsFourBlockImplementationsFromOneRealYaml`（テーブル・LIST_MAP とも `getRows().size()` が 1 であることをアサートする。レビュー指摘を受けて行数アサートを足し、引用を真にした）／複数: `readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml`（2 行） |
| E-3 ファイル内レコードレイアウト数 | ✅ | ✅ | ✅ | 0: `readsEmptyRecordsFromFixedFileWithoutRecords`／1: `readsEmptyRowsFromRecordLayoutWithoutRows`／複数: `readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml`（断片 2 件） |
| E-4 コンテナ内セクション数 | n/a | ✅ | ❌（**到達不能**） | 1: `namesContainerAndSectionByResourceNameWithoutBlocks`。複数は `YamlFormatReader#read` が `Collections.singletonList(section)` を返すため構造上到達不能（§0.8-6） |

**開示（テストを足していない担保の穴）**

- **`YamlFormatReader#normalizeRecordType` の `"default"`（小文字）分岐は到達可能であり、修正ラウンド 2 で閉じた。**
  **当初「軸A〜F のどの要素にも属さない」として #26 送りと開示していたが、これは誤りだった**
  （2026-08-14・2 巡目レビュー指摘）。`record_type: "default"` を書いた実 `.yaml` は
  スキーマを通り（`$defs.record_fragment.properties.record_type` に `enum` は無く、description も
  「可読性のために任意の名前を記述してよい」と書いている）、この分岐に到達する。
  実測すると**作成者が書いた `"default"` は中間モデルで `null` になる**（＝原文に残らない）。
  修正ラウンド 2 で `YamlFormatReaderRealFileTest#normalizesLowercaseDefaultRecordTypeToNull` を足して閉じた
  （`"Default"` 側は `YamlFormatReaderTest#readFile_recordTypeDefault_normalizedToNull` が通す）。
  **修正ラウンド 3（2026-08-14）で JaCoCo を取り直した結果、`YamlFormatReader` は
  行 200/200（100%）・分岐 106/106（100%）だった**（**2026-08-14 時点の実測**。その後
  `recordsWithoutFwHeader` の廃止で総数が減り、#26 の計測〈`da66425`・2026-08-20〉では
  `line 192/192 branch 102/102` である。§0.1-2 の追補その 2 の注記を参照。
  **どちらも未到達 0 件＝100% であり、以下の論旨は変わらない**）。 修正ラウンド 2 の時点で記していた
  「分岐 107/108（99.07%）・唯一の未到達分岐は `"default"` 側」は、**その分岐を閉じたあとの数値ではなく
  再実行しても再現しない**ため、実測値へ差し替えた（3 巡目レビュー指摘）。導出コマンド
  （**オフラインで実行できる形に直した**。JaCoCo 手順は steering Decisions のとおり）:

  ```sh
  JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes \
    && JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec \
    && awk -F, 'NR > 1 && $3 == "YamlFormatReader" { print "line " $9 "/" ($8 + $9) " branch " $7 "/" ($6 + $7) }' \
         target/site/jacoco/jacoco.csv
  ```

  出力は `line 200/200 branch 106/106`（**レビュー 1 巡目まで反映・2026-08-14**）。
  **依然として未到達は 0 である。** #25 時点は `line 201/201 branch 108/108` だった。
  不具合修正（YML-02 の `rawGroupsInOrder` 化・YML-08 の `DirectiveUtil` 切り出し）はいずれも
  既存テストが通る経路に入って同値のままだったが、レビュー 1 巡目の B-3 で
  `addSendSyncBlocks` の `formattedGroup`（`rawGroup != null ? "[" + rawGroup + "]" : ""`）を
  既存ヘルパ `formatGroup(entry)` の呼び出しへ置き換えたため、**行が 1・分岐が 2 減った**。
  **この数値は `YamlFormatReader` 1 クラスぶんであり、
  4 辺の担当クラス全体の計測と未到達分岐の列挙は #26 の仕事である。**
- **軸C の C-15（`MessageDataBlock.records` 空）は実 `.yaml` 経路では到達不能である。**
  スキーマ `$defs.message_data.properties.records.minItems` ＝ 1 のため、
  「レコードを 1 件も書かない」入力は書けない。#18 が ✅ としているのは
  in-memory 経路（`YamlFormatReaderTest#readMessage_emptyBody_isStillMapped`）であり、
  **実ファイル経路での担保は無い**（`issues.md`「到達不能と判定した軸要素（#24）」）。
  **`YamlFormatReaderRealFileTest#dropsFwHeaderNamedRecordFromRealYaml` は実 `.yaml` で
  `records` 0 件を観測していたが、これは C-15 の担保に数えない** —— 書いたレコードが
  YML-03 で落とされた結果であって、仕様上の到達手段ではないためである
  （2026-08-14・QA レビュー指摘。同テストの「担保する軸要素」からも C-15 を外し、
  同じ但し書きを Javadoc に置いた）。#25.5 で同テストは仕様どおりの期待値を書いた
  `keepsFwHeaderNamedRecordInMessageFromRealYaml` へ置き換え、**2026-08-18 の YML-03 修正で
  `@Ignore` を外して緑になった。同じ入力で `records` は 1 件になるため、`records` 0 件という観測は
  そもそも起きなくなっている**（修正前の観測は `issues.md` **YML-03** に残っている）。
  C-15 の判定（実 `.yaml` 経路では到達不能）は変わらない。
- **軸D の 12 ケースのうち 10 ケースは 1 経路（`setup_tables`）でしか測っていない。**
  `YamlFormatReaderScalarTest#readValue` は常に `setup_tables` へ値を置く。行値の取り出しは
  テーブル／LIST_MAP／レコード断片の 3 系統あり、スキーマも別パスで型を課すため、残り 2 経路での
  結果は D2-06（`null`）と D2-11（空文字）の 2 ケースしか確認していない
  （上の「別経路での確認」表。2 ケースとも経路差は無かった）。
  **12 ケース × 3 経路には広げないと判断した**（経路差が無いという観測が 2 ケースで得られたこと、
  および 3 系統とも同じ `YamlLoader` のスカラー解決結果を受け取る構造であることによる）。
  残る 10 ケースについて経路差が無いことは**未確認**である。
- **「警告が出ないこと」のアサートには 2 段階の穴がある。**

  1. **課題ごとの有無**: `YamlFixture.readCapturingWarnings` で「WARNING 以上 0 件」を
     アサートしているのは **YML-04／YML-05／YML-09／YML-10／YML-11 の代表テスト 6 件**である
     （`grep -ho 'reading.warnings()' src/test/java/nablarch/test/tool/converter/yaml/*.java | wc -l` → **6**）。
     **YML-02／YML-03 ではアサートしていない**（例外にならないことと結果が消えることの観測にとどめた）。
  2. **捕捉範囲**: 捕捉できるのは `java.util.logging` のルートロガーに届くものだけである。
     `nablarch-testing` 自身のログ基盤（`nablarch.core.log`）への出力は捕捉できないため、
     **「どこにも警告が出ない」ことは証明していない**。言えるのは「JUL 経路には出ない」までである。

  なお辺②の読み取り経路には JUL のロガーが 1 つも無い
  （`grep -rn 'java.util.logging' src/main/java` のヒットは `XlsFormatReader` の import 1 行のみ）。
  したがってこのアサートは**現状の実装では落ちようがなく**、意味を持つのは
  「将来 JUL の警告を足したときに気づける」という回帰検知としてである。
  辺①（`XlsFormatReader`）は重複カラム名で実際に JUL の WARNING を出すため、同じアサートが
  そちらでは現在の挙動そのものを固定している。この非対称を承知のうえで残している。

**開示（修正ラウンド 2＝スキーマの自由度の掃引。2026-08-14 追記）**

軸A〜F は「中間モデルの形」から要素を立てているため、**軸のどの要素にも当てはまらない壊れ方**は拾えない。
2 巡目のレビュー指摘を受けて、**本体スキーマ**（yaml jar 内 `nablarch/test/ntf-testdata-yaml-schema.json`）を
先頭から読み、「スキーマが構造を縛っていない箇所」を列挙し、その自由度を使った入力を実 `.yaml` で
**1 項目につき 1 回ずつ**通した。以下がその全件である（**これが「どこまで見たか」である**）。
課題として記録したものは `issues.md` の該当 ID を、記録しなかったものは観測結果を書く。

| # | スキーマ上の自由度（JSON パス） | 自由度の中身 | 実 `.yaml` で観測した結果 |
|---|---|---|---|
| 1 | `$defs.table_data.properties.rows.items` | `additionalProperties` が任意キーを許し、行ごとのキー集合に制約が無い。**キーの大小・一意性にも制約が無い** | **YML-04**（先頭行に無いキーが黙って消える）。**大小だけが違うキー（`id` と `ID`）を同一行に書いた場合は YML-10**（大文字化で衝突し値が消える） |
| 2 | `$defs.list_map_data.properties.rows.items` | 同上 | **YML-04**（経路差なし）。ただし**大小の衝突は起きない** —— LIST_MAP は原文の大小のまま入る（**YML-10** の対比） |
| 3 | `$defs.record_fragment.properties.rows.items` | 要素数が `fields` の件数と紐づいていない | **YML-05**（余りは drop・不足は `""` 充填） |
| 4 | `$defs.record_fragment.properties.record_type` | `enum` が無い（description は「任意の名前でよい」） | `FW_HEADER` は **YML-03**（既記録）。`"Default"` / `"default"` は `null` へ正規化（テストで固定）。`"DEFAULT"` や任意名は原文のまま |
| 5 | `$defs.field_def.properties.type` | `enum` が無い（`minLength: 1` のみ） | 未知の型名は `IllegalArgumentException`（loud）。課題なし |
| 6 | `$defs.record_fragment.properties.fields` | `uniqueItems` が無く、フィールド名の重複は description にだけ「重複不可（重複時はエラー）」と書かれている | `IllegalArgumentException: Duplicate field names are not permitted in a record. duplicate field=[f1] . file=[f.dat]`（loud）。課題なし |
| 7 | `$defs.field_def.properties.length` の `"-"` | パターンは許すが意味は description にだけ書かれている | **YML-07**（`text-encoding` 省略時に手掛かりの無い NPE） |
| 8 | `$defs.field_def.properties.length` の省略 | 「固定長では実質必須」は description にだけ書かれている | 例外にならず `FieldDef.length` が `null` になる。変換は忠実。課題なし |
| 9 | `$defs.directives`（固定長専用／可変長専用の別） | キー集合は固定だが、ファイル種別との対応は description にだけ書かれている | 取り違えは `IllegalArgumentException: invalid directive found. [...]`（loud）。課題なし。**器が注入する既定ディレクティブはファイル種別で違う** —— 固定長は `{file-type=Fixed}` の 1 件、可変長は `{file-type=Variable, field-separator=,}` の 2 件（実測。C-11 が「空にならない＝到達不能」であることは両種別で成り立つ） |
| 10 | `$defs.directives.properties.record-separator` ／ `field-separator` | シンボル指定とリテラル指定の両方を description が認めている | **YML-08**（リテラルは trim で消える／シンボルは実文字になる） |
| 11 | `$defs.directives.properties.file-type` | `type` フィールドと矛盾させられる（整合の制約が無い） | 例外にならず、ブロックは `FIXED` のまま `file-type=Variable` を保持する。原文はどちらも残るため変換は忠実。課題なし |
| 12 | `$defs.directives.properties.record-length` | フィールド長の合計と紐づいていない | 上書き値がそのまま中間モデルへ入る。変換は忠実。課題なし |
| 13 | `$defs.fw_header.additionalProperties` | 任意キーを許す（`minProperties: 0`） | 記述順のまま入る。空マップも通る。課題なし |
| 14 | `$defs.list_map_data.properties.id` ／ `$defs.message_data.properties.id` | 一意制約が無い（「先着1件」は description にだけ書かれている） | **YML-06**（2 件目以降が 1 件目のデータで作られる） |
| 15 | `$defs.table_data.properties.table` ／ `$defs.file_data.properties.path` | 一意制約が無い | 重複してもエントリごとに独立したブロックになる。変換は忠実。課題なし |
| 16 | `$defs.group_message_data` ／ `$defs.expected_request_message_data` の `group_id` 省略 | 省略時の意味は description にだけ書かれている | **YML-02**（既記録） |
| 17 | `$defs.table_data` ／ `$defs.file_data` の `group_id` 省略 | 同上 | 整形済みグループ ID が空文字になる。変換は忠実。課題なし |
| 18 | `rows` の行オブジェクトのキー順 | 行ごとに順序が違ってよい | 値は名前で対応付けられるため順序差の影響は出ない。課題なし |
| 19 | マーカーカラム `[COL]` | スキーマは通常のキーと区別しない（全カラムがマーカーでも通る） | カラム 0 件・値を持たない行になる。辺①の **XLS-08** と同型（テストで固定） |
| 20 | `$defs.file_data.properties.records` の `minItems: 0` ／ `rows` の空配列 | 空を許す | 既に担保済み（C-12／C-09／E-2(0)／E-3(0)） |
| 21 | `$defs.message_data.properties.records` に `FW_HEADER` 名を書ける | `enum` が無い | **YML-03**（既記録）。送信系（`response_body_messages`）でも落ちていた。**2026-08-18 に yaml 側 `0b53910` ＋ 本リポジトリの `YamlFormatReader#recordsWithoutFwHeader` 廃止で解消済み**であり、現在は 3 経路とも残る。それを `keepsFwHeaderNamedRecordInSendSyncFromRealYaml` ／ `keepsFwHeaderNamedRecordInMessageFromRealYaml` ／ `#keepsFwHeaderNamedRecordInFileFromRealYaml` がアクティブに固定している（`@Ignore` は外した。修正前の観測は `issues.md` **YML-03** に残る） |
| 22 | `$defs.message_data.properties.records` に断片を 2 件以上書ける | 件数の上限が無い | 2 件とも保持される。辺①では **XLS-15** により不可能な形が辺②では作れる。課題なし |
| 23 | `expected_request_header_messages` と `expected_request_body_messages` の件数一致 | スキーマは縛らず description にだけ書かれている | converter は片方だけでもブロックを作る。NTF 実行時の制約であり変換の正しさとは別。課題なし |
| 24 | セクション配列内でのエントリの並び（`$defs.table_data` ／ `$defs.file_data` ／ `$defs.group_message_data` の `group_id`） | 同じ `group_id` のエントリが配列内で連続することを要求していない（順序の制約が無い） | **YML-09**（`g1` → `g2` → `g1` と書くとブロックがグループの初出順にまとめ直され、原文の記述順と食い違う。テーブル系・ファイル系・送信系の 3 経路とも同じ。**課題として記録した** — 判断の根拠は下の「掃引項目 24 を課題とした理由」） |
| 25 | `$defs.field_def.properties.length` の `"0"` | パターンは許すが意味（ダミーフィールド）は description にだけ書かれている | `"0"` が忠実に `FieldDef.length` へ入る（例外にも既定値の補完にもならない）。課題なし |
| 26 | 識別子系プロパティの `minLength` 不在（`$defs.table_data.properties.table` ／ `$defs.file_data.properties.path` ／ `$defs.list_map_data.properties.id` ／ `$defs.message_data.properties.id` ／ `$defs.record_fragment.properties.record_type` ／ `$defs.field_def.properties.name`）。`group_id` と `$defs.field_def.properties.type` には `minLength: 1` があるという非対称 | 空文字の識別子が書ける | 6 つとも例外にならず、**空の識別子がそのまま中間モデルへ入る**（`table: ""` → `""`、`path: ""` → `""`、`id: ""` → `""`、`name: ""` → `""`）。`table: "   "` は器が trim するため `""` になる。**`record_type: ""` は `null` ではなく `""` で入り、省略（`null`）と分かれる**（C-16 の「省略＝`null`」と隣接する事実。辺①の **XLS-06** は逆に実 `.xlsx` 経路で省略が `""` になる）。黙って消えるものは無いため課題なし |
| 27 | **器が行う正規化**（スキーマが縛っていない箇所ではなく、スキーマが触れていない箇所）。`$defs.table_data.properties.table` の description は「NTF により trim・大文字変換される」と書くが、**カラム名の大文字化についてはスキーマのどこにも記述が無い** | テーブル系は器（`TableData`）がテーブル名とカラム名を大文字化する（`my_table` → `MY_TABLE`、`user_id` → `USER_ID`）。LIST_MAP は原文の大小のまま | **YML-10**（大小だけが違うキーが大文字化で衝突し、値が黙って消えて列名が重複する）。大文字化そのものは `issues.md`「課題としないと判断した観測結果（#24）」に記録 |
| 28 | **YAML 記法そのもの**（スキーマは `rows` の値を `["string","null"]` としか縛らず、引用符の要否に踏み込まない） | 引用符なしのプレーンスカラーも文字列として通る | **YML-11**（前後の空白が消える／`#` 以降がコメントとして落ちる。YAML 仕様どおりだが書いた値と食い違う）。引用符付き（軸D の D2-11／D2-12）は保たれる |

**掃引項目 24 を課題とした理由（記録する／しないの判断・2026-08-14）**

データは失われない（値は正しいまま入る）が、**変換結果が原文と一致しない**（並びが変わる）。
辺③④はこの順で書き出すため、変換後の成果物ではエントリの並びが原文と入れ替わる。
本リポジトリは並びの保持を変換の正しさとして扱ってきた（#15「LIST_MAP 列順保持修正」は列順が
アルファベット順になることを不具合として直した）ため、「変換は忠実」として課題なしにはできない。
NTF は `group_id` で収集するため実行結果は変わらず、後段のテストは通ってしまう＝**検出できない**。
以上より `issues.md` に **YML-09**（影響度 中・検出できない）として記録し、根拠テスト
`YamlFormatReaderRealFileTest#reordersBlocksByFirstAppearanceOfGroupIdFromRealYaml` で現状挙動を固定した。

**この掃引で見ていない範囲（穴として開示する）**

- **各自由度は 1 項目につき 1 回ずつしか通していない。** 自由度どうしの組合せ
  （例: `id` 重複とカラム不一致を同時に持つ入力）は見ていない。
- **スキーマの description が NTF の実行時挙動を述べている部分は観測していない。**
  FK 制約と DELETE 順序、`expected_complete_tables` の型別デフォルト値補完、
  `testShots` の予約 ID など、変換ではなく NTF 実行時に効く記述である。確かめるには NTF の実行が要る。
- **辺③（中間モデル→Excel）・辺④（中間モデル→YAML）へ書き出したときの挙動は掃引の対象外である。**
  とくに YML-08（中間モデルに実改行が入る）の往復が安定するかは未確認であり、`issues.md` に明記した。
- **インタープリタ記法（`${...}` など値の中身の記法）は掃引していない。**
  スキーマは `rows` の値を `["string","null"]` としか縛っておらず記法に踏み込まないため、
  「スキーマが縛っていない箇所」ではあるが、converter は `InterpreterResolver.raw()` で配線しており
  値を素通しする（`${...}` を含む値が原文のまま入ることは in-memory 経路の `YamlFormatReaderTest` が
  固定している。実 `.yaml` 経路では確かめていない）。
- **軸D の 12 ケースを掃引の各項目へ掛け合わせてはいない**（上の 3 点目の「1 回ずつ」と同じ理由）。
- **`YamlFormatReader#toStringDirectives` の「`null` 値は `null` のまま保持する」は実 `.yaml` 経路では
  到達不能である**（2026-08-14・Verification レビュー指摘で追記）。`$defs.directives` の 17 プロパティは
  型が `string` / `integer` / `boolean` のいずれかで **`null` を許さない**ため、スキーマを通る YAML から
  `null` のディレクティブ値は書けない。

  ```sh
  python3 -c "
  import json,zipfile,os
  z=zipfile.ZipFile(os.path.expanduser('~/.m2/repository/com/nablarch/framework/nablarch-testing-yaml/1.0.0-SNAPSHOT/nablarch-testing-yaml-1.0.0-SNAPSHOT.jar'))
  d=json.loads(z.read('nablarch/test/ntf-testdata-yaml-schema.json').decode('utf-8'))['\$defs']['directives']
  ps=d['properties']
  print(len(ps), sorted(set(json.dumps(v.get('type')) for v in ps.values())))
  "
  ```

  出力は `17 ['"boolean"', '"integer"', '"string"']`。**テストは足していない**（in-memory 経路なら
  固定できるが、実 `.yaml` 経路の担保という本タスクの狙いから外れるため）。
- **C-06（`groupId` 省略＝`""`）は当初、実ファイル経路でのアサートが無かった**
  （2026-08-14・QA レビュー指摘）。#18 時点で ✅ だったのは in-memory 経路の `YamlFormatReaderTest` である。
  `readsEmptyColumnNamesAndRowsFromTableWithoutRows` に 1 行足して閉じた。
- **値のサイズ・行数の上限は掃引していない**（2026-08-14・3 巡目レビュー指摘で追記）。
  辺③には D3-07（Excel のセル文字数上限 32767 を超える値。`issues.md` **XLS-19**）があるが、
  辺②の軸D 12 ケースにも上の掃引 28 項目にもサイズの観点は無い。**1 回だけ観測した**結果、
  40,000 文字の値は中間モデルへ同じ長さ・同じ内容で入り、5,000 行のテーブルも行数どおり入った
  （例外にも切り詰めにもならない）。スキーマは `rows` の値を `["string","null"]` としか縛らず
  長さ・件数の上限を持たないため**上限そのものは掃引していない**。テストは足していない。

**担保の強さについて開示すること（2026-08-14・Verification レビュー指摘で追記）**

- **軸D の 27 件が固定しているのは、大半が「器と snakeyaml-engine のスカラー解決の結果」であって
  converter のコードではない。** `src/main` の変異で軸D テストが殺せるのは、行値を取り出す
  **3 か所の null 経路だけ**である（実測・2026-08-14）。それぞれ「null を空文字へ変える」変異を入れ、
  落ちた軸D テストは次のとおり:

  | 変異箇所 | 落ちた軸D テスト |
  |---|---|
  | `addTableBlocks`（`value == null ? null : value.toString()`） | `readsUnquotedNullAsJavaNull` ／ `readsOmittedValueAsJavaNull`（2 件） |
  | `addListMapBlocks`（`mapRow.get(column)`） | `readsUnquotedNullAsJavaNullInListMapPath`（1 件） |
  | `toRecordLayouts`（`valueMap.get(name)`） | `readsUnquotedNullAsJavaNullInRecordFragmentPath`（1 件） |

  残る 23 件は characterization である。**軸D の ✅ を converter の実装の担保として読まないこと。**
  辺②で軸D を測る目的は「YAML 記法 → 中間モデルの値」を記録・固定することであり、
  この性質はタスクの狙いどおりである。
- **行・分岐 100% は変異に耐えることを意味しない。** `YamlFormatReader` は
  行 201/201・分岐 108/108（**2026-08-14 時点の実測。#26 の計測〈`da66425`・2026-08-20〉では
  `line 192/192 branch 102/102`**）だが、
  その状態でも 3 つの変異（LIST_MAP のカラム順をソートする／セクションを記述順でなく定義順に走査する／
  ファイル系でも `FW_HEADER` レコードを落とす）が生存していた（2026-08-14・Verification レビューで判明）。
  いずれも**入力の作り方が偏っていた**ことが原因で、テストを 3 件足して閉じた
  （`preservesListMapColumnOrderAndExcludesMarkerFromRealYaml` ／ `readsAllThirteenDataTypesFromRealYaml` の
  セクション逆順化 ／ `keepsFwHeaderNamedRecordInFileFromRealYaml`）。
  **その次のラウンドでもさらに 3 件が生存した**（`fw_header` の `LinkedHashMap` を `TreeMap` へ／
  `normalizeRecordType` が `"FW_HEADER"` も `null` にする／送信系の FW_HEADER 除外をやめる）。
  原因は同じで、順序アサートのキーが辞書順と一致していたこと・種別名を見ていなかったこと・
  送信系に該当入力が無かったことである。テスト 1 件追加とアサート 2 か所の追加・入力の並べ替えで閉じた。
  **#26 ではカバレッジ数値だけを担保の根拠にしないこと。**

<a id="s2-3"></a>

### 2.3 辺② 未担保一覧（#24 が埋めた対象）

計上単位と「状態」の 3 分類は §1.3 の規則に従う。

**本表は #24 の実測結果に合わせて「#24 後の状態」列を足した（2026-08-14）。** #18 時点は
「要追加 23 ／ 到達不能 3」であった。軸D の定義が 10 ケース → **12 ケース**へ改まった（§0.5）ため
総計は 26 → **28**、#18 基準の要追加は 23 → **25** になる。#18 時点の分類は「#18 の状態」列に残した。

| 軸 | 未担保要素 | #18 の状態 | #24 後の状態 | 件数 |
|---|---|---|---|---|
| A | （要追加はなし） | — | — | 0 |
| A | A-01 `DEFAULT` — `YamlFormatReader` の分岐に `DEFAULT` を返す経路がない（§0.8-7） | 到達不能 | 到達不能（変更なし） | 1 |
| B | （なし） | — | — | 0 |
| C | C-08 columnNames 空／C-09 rows 空／C-12 FileDataBlock.records 空／C-18 RecordLayout.rows 空／C-13 MessageDataBlock.directives（値あり） | 要追加 | **担保済み（#24）** — 順に `YamlFormatReaderRealFileTest#readsEmptyColumnNamesAndRowsFromTableWithoutRows`（C-08／C-09。LIST_MAP 経路は `#readsEmptyColumnNamesAndRowsFromListMapWithoutRows`）／`#readsEmptyRecordsFromFixedFileWithoutRecords`（C-12）／`#readsEmptyRowsFromRecordLayoutWithoutRows`（C-18）／`#readsMessageDirectivesFromRealYaml`（C-13） | 5 |
| C | C-11 FileDataBlock.directives 空 | 要追加 | **到達不能** — 本体 `DataFile` のコンストラクタが `file-type` を必ず注入する（`issues.md` **XLS-07**）。根拠は `#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInFile` がテストで示す | 1 |
| C | C-17 fields 空／C-20 FieldDef.type 省略 | 要追加 | **到達不能** — スキーマが `fields` に `minItems: 1` を、`field_def` に `type` 必須を課す。根拠は `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFieldsIsEmpty`／`#failsWithSchemaValidationExceptionWhenFieldTypeIsMissing` がテストで示す | 2 |
| C | C-02 sections 空 — `YamlFormatReader#read` が `Collections.singletonList(section)` を返すため sections は常に 1 件（§0.8-6） | 到達不能 | 到達不能（変更なし） | 1 |
| D | §0.5 の 12 ケース（D2-01〜D2-12。#18 時点の定義では 10 ケース。うち D2-02／D2-03／D2-06／D2-07 は往復テスト経由の 🔺 があった。§0.8-8） | 要追加 | **担保済み（#24）** — `YamlFormatReaderScalarTest` 27 件（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderScalarTest.java` → **27**）。要素別の担保テストメソッドは §2.1-2 の軸D 表。27 件のうち 4 件は D2-06／D2-11 を LIST_MAP 経路・レコード断片経路で確認したもので、**軸要素としては別勘定にしない**（§2.1-2 の「別経路での確認」表） | 12 |
| E | E-2(0 件) | 要追加 | **担保済み（#24）** — C-08／C-09 と同じ入力（`#readsEmptyColumnNamesAndRowsFromTableWithoutRows` ほか 1 件） | 1 |
| E | E-4(複数) — `YamlFormatReader#read` が 1 リソース単位 API（§0.8-6） | 到達不能 | 到達不能（変更なし） | 1 |
| F | F2-01 スキーマ違反／F2-02 不正 YAML／F2-04 必須構造欠落／F2-05 空ファイル（🔺 のみ） | 要追加 | **担保済み（#24）** — 軸F を担保するのは `YamlFormatReaderInvalidInputTest` の **8 件**である（同クラスの総数は `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java` → **32**（#35 で導き直した。宣言の 31 は `226d0f8` 時点）。差の 24 件は、掃引の固定テスト 22 件（`issues.md` YML-04〜YML-08・YML-10・YML-11）と、ローダの他の失敗経路 2 件（ルートがマッピングでない／同一マッピング内のキー重複）であり、**いずれも軸F の要素ではない**。§2.1-2 の「開示」の掃引表を参照）。**8 件・2 件・22 件（#35 で導き直した実測。宣言の 21 は #24 時点）の導出コマンドは本表の下**。**内訳（本行の 4 ケース 7 件 ＋ F2-03 の 1 件）**: F2-01 が 2 件／F2-02 が 1 件／F2-04 が 3 件／F2-05 が 1 件／F2-03 が 1 件。F2-04 の 3 件のうち 2 件（`#failsWithSchemaValidationExceptionWhenFieldsIsEmpty` ／ `#failsWithSchemaValidationExceptionWhenFieldTypeIsMissing`）は C-17／C-20 が到達不能である根拠を兼ねる（別勘定ではない）。F2-03 未知のキーは #18 時点で既に ✅（in-memory）だが実ファイル経路では結果が異なるため §2.1-2 の軸F 表に併記した | 4 |
| **合計** | | **要追加 25 ／ 到達不能 3** | **要追加 0 ／ 担保済み 22 ／ 到達不能 6 ／ 対象外 0** | **28** |

**軸F の 8 件・ローダ分岐 2 件・掃引 21 件の導出**（2026-08-14・3 巡目レビュー指摘で追加。総数 31 は
上の表にコマンドを併記しているが、内訳は数字のまま置かれていた）。
`YamlFormatReaderInvalidInputTest` は軸F の節（F2-01〜F2-05）を先頭に、ローダの他の失敗経路の節、
掃引の節（YML-04 以降）の順に置いており、境界は節見出しのコメント行である:

```sh
awk '/F2-01 スキーマ違反/,/ローダの他の失敗経路/' \
    src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java \
  | grep -c '^    @Test'
awk '/ローダの他の失敗経路/,/YML-04 先頭行のキー集合/' \
    src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java \
  | grep -c '^    @Test'
awk '/YML-04 先頭行のキー集合/,0' \
    src/test/java/nablarch/test/tool/converter/yaml/YamlFormatReaderInvalidInputTest.java \
  | grep -c '^    @Test'
```

出力は順に **8** ／ **2** ／ **22**（8 ＋ 2 ＋ 22 ＝ 32）。**2026-08-28 に #35 で導き直した**
（#35 が掃引の節から `@Ignore` つきテスト 2 件を削除したため、3 つ目の区間が 24 → 22 になった。
1 つ目・2 つ目は変わっていない）。**以下は #31（2026-08-24）時点の記録である** ——
宣言値は **8** ／ **2** ／ **21**（計 31）のまま取り残されており、3 件ずれていた。
うち 1 件は #31 が末尾に足した
`YamlFormatReaderInvalidInputTest#rejectsVariableFileFieldWithLengthFromRealYaml`
（`issues.md` **XLS-45**）、残る 2 件は #31 より前に足されて宣言値へ反映されなかったぶんである
（**どのタスクのぶんかは未確認**）。**3 つ目の区間だけが動いており、軸F の 8 件（1 つ目）と
ローダの失敗経路 2 件（2 つ目）は変わっていない。**
8 件のメソッド名は §2.1-2 の軸F 表に全件挙げてある
（F2-01 が 2 件／F2-02 が 1 件／F2-03 が 1 件／F2-04 が 3 件／F2-05 が 1 件）。

**C-13 の数え方**: C-13 は「値あり」を #24 で担保し、「空」は到達不能である（§2.1-2 の軸C 表）。
計上単位は「軸要素 1 件を 1 件」（§1.3）であるため、**C-13 は担保済み側で 1 件だけ数え**、
到達不能側には計上しない。

**合計の検算**（表の「件数」列を上から順に足す）:

- 担保済み: C 5 ＋ D 12 ＋ E-2(0) 1 ＋ F 4 ＝ **22**
- 到達不能: A-01 1 ＋ C-11 1 ＋ C-17/C-20 2 ＋ C-02 1 ＋ E-4(複数) 1 ＝ **6**
- 要追加: **0**
- 総計: 22 ＋ 6 ＝ **28**（B は 0 件）

導出コマンド（表の「件数」列を機械的に足す。**そのまま実行すれば 28 になる**）:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter/.rn/ntf-test-data-converter/coverage
awk '/^\| 軸 \| 未担保要素 \| #18 の状態 \| #24 後の状態 \| 件数 \|/,/^\| \*\*合計\*\*/' inventory.md \
  | grep -vE '^\| (軸|---|\*\*合計)' \
  | awk -F'|' '{gsub(/[^0-9]/,"",$(NF-1)); s+=$(NF-1)} END {print s}'
```

**#18 時点の「特に大きな空欄」**（軸D 全滅 ——当時の定義では 10 ケース、現在の定義では 12 ケース—— と、
`MessageDataBlock.directives` が値あり・空の両方とも 0 件）は #24 で解消した。
**辺②の「要追加」は 0 件**である。残る到達不能 6 件のうち 3 件（C-11／C-17／C-20）は根拠テストを持ち、
3 件（A-01／C-02／E-4(複数)）は `YamlFormatReader#read` と `addBlocksForSection` の構造そのものが根拠である。

**ただし「未担保 0 件」は本書の計上単位（§1.3 冒頭）での話である。** §2.1-2 末尾の「開示」4 点
（`normalizeRecordType` の `"default"` 分岐——**修正ラウンド 2 で到達可能と判明しテストで閉じた**／
C-15 は実ファイル経路では到達不能／軸D の 10 ケースは 1 経路でしか測っていない／
YML-02・YML-03 で「警告が出ないこと」を実行可能な形にしていない）と、
**掃引で列挙した 28 項目および「見ていない範囲」6 点**は
空欄・穴として残る（§2.1-2 の「開示（修正ラウンド 2）」。項目 24〜28 と「見ていない範囲」6 点目は
修正ラウンド 3 で足した）。**掃引はここで閉じる。**

導出コマンド（掃引表の項目数 28。掃引表は §2.1-2 の中で唯一「先頭列が番号」の表である）:

```sh
awk '/^### 2\.1-2 /,/^<a id="s2-3">/' .rn/ntf-test-data-converter/coverage/inventory.md \
  | grep -cE '^\| [0-9]+ \|'
```

**#24 のレビュー指摘の反映（2026-08-14）で足したテスト 9 件は、上の件数を動かさない。** 内訳は
（a）既に ✅ だった軸要素を実ファイル経路・別経路でも通したもの（C-14 値あり／C-21 値あり／
軸D の別経路 4 件／E-2(1 件) の行数アサート）、（b）到達不能の根拠をもう 1 つの生成経路にも用意したもの
（C-13 空・送信系経路）、（c）軸要素ではなく現状挙動の課題を固定したもの
（`issues.md` **YML-02**（送信系の `group_id` 省略でブロックが消える）／**YML-03**
（`record_type: FW_HEADER` のレコードが捨てられる））である。
（c）の 2 件は軸A〜F のどの要素にも新しい ✅ を与えないため、計上単位の上では件数に影響しない。

**修正ラウンド 2（2026-08-14）で足したテスト 16 件も、上の件数を動かさない。** 15 件は
「スキーマが構造を縛っていない箇所を突いた入力」の現状挙動の固定（`issues.md` **YML-04**〜**YML-08**）で、
軸A〜F のどの要素にも属さない。残る 1 件（`normalizesLowercaseDefaultRecordTypeToNull`）は
C-16 の正規化を実 `.yaml` で確かめたもので、C-16 は #18 時点で既に ✅ である。

**修正ラウンド 3（2026-08-14）で足したテスト 1 件も、上の件数を動かさない。**
`reordersBlocksByFirstAppearanceOfGroupIdFromRealYaml` は掃引項目 24（`issues.md` **YML-09**）の
現状挙動の固定であり、軸A〜F のどの要素にも属さない。

---

<a id="s3"></a>

## 3. 辺③ 中間モデル→Excel（`XlsFormatWriterTest` 40 件）

### 3.1 テストメソッド → 担保する軸要素

| # | テストメソッド | 軸A | 軸B | 軸C | 軸D | 軸E | 軸F |
|---|---|---|---|---|---|---|---|
| 1 | `writesTableBlock` | A-02 | B-1 | C-06(省略→`[]` なし), C-07, C-08, C-09 | 🔺D3-04 null→リテラル `"null"`／🔺D3-05 `""`→空セル（いずれも `getStringCellValue` のみ） | E-2(複数=2) | — |
| 2 | `metaRowContainsOnlyValueCells` | A-02 | B-1 | — | — | E-2(1) | — |
| 3 | `writesTableMarkerWithGroupId` | A-03 | B-1 | C-06(値あり) | — | E-2(1) | — |
| 4 | `writesExpectedCompleteTableMarker` | A-04 | B-1 | C-05 | — | E-2(1) | — |
| 5 | `writesListMapBlock` | A-05 | B-2 | C-07, C-08, C-09 | 🔺D3-05 | E-2(複数=2) | — |
| 6 | `tintsMarkerColumn` | A-02 | B-1 | — | ※マーカーカラム `[NOTE]` 記法 | — | — |
| 7 | `writesFixedFileBlock` | A-06 | B-3 | C-07, C-11(値あり), C-12, C-16(値あり), C-17, C-18, C-19, C-20, C-21(`"-"`/`"5"`) | ※長さ記法 `-` | E-3(1) | — |
| 8 | `writesVariableFileWithoutLengthRow` | A-08 | B-3 | C-10(VARIABLE 版面), C-11(空), C-21(省略) | — | E-3(1) | — |
| 9 | `writesMultipleRecordLayouts` | A-06 | B-3 | C-12(2件), C-11(空) | — | **E-3(複数=2)** | — |
| 10 | `rejectsNullRecordTypeOnSecondRecord` | A-06 | B-3 | C-16(null) | — | E-3(複数) | ✅ 2 レコード目 recordType null → `IllegalStateException` |
| 11 | `rejectsEmptyRecordTypeOnSecondRecord` | A-06 | B-3 | C-16(`""`) | — | E-3(複数) | ✅ 2 レコード目 recordType 空文字 → `IllegalStateException` |
| 12 | `allowsNullRecordTypeOnSingleRecord` | A-06 | B-3 | **C-16(省略=null)** ✅ | — | E-3(1) | — |
| 13 | `writesMessageBlock` | A-10 | B-4 | C-07, C-13(空), C-14(値あり), C-15, C-17, C-18, C-19, C-20, C-21 | — | E-3(1) | — |
| 14 | `writesSendSyncMessageWithSequenceNo` | A-11 | B-4 | C-06(値あり), C-07, C-13(空), C-14(空), C-15, C-18 | — | E-2(複数=2) | — |
| 15 | `appliesHeaderBackgroundColor` | A-02 | B-1 | — | — | — | — |
| 16 | `appliesTestShotsHeaderColor` | A-05 | B-2 | — | — | — | — |
| 17 | `appliesSetupHeaderColor` | A-02 | B-1 | — | — | — | — |
| 18 | `appliesExpectedHeaderColor` | A-03 | B-1 | — | — | — | — |
| 19 | `appliesOtherHeaderColorForMessage` | A-10 | B-4 | C-14(値あり) | — | — | — |
| 20 | `appliesOtherHeaderColorForNonTestShotsListMap` | A-05 | B-2 | — | — | — | — |
| 21 | `eachGroupHasDistinctDefaultColor` | — | — | — | — | — | —（SUT は `ExcelFormatConfig`。Writer は駆動しない） |
| 22 | `drawsBlockOuterBorder` | A-02 | B-1 | — | — | E-2(1) | — |
| 23 | `insertsBlankRowBetweenBlocks` | A-02 | B-1 | — | — | **E-1(複数=2)** | — |
| 24 | `appliesAutoColumnWidth` | A-02 | B-1 | — | — | — | — |
| 25 | `honorsConfigOverrides` | A-02 | B-1 | — | — | E-1(複数=2) | — |
| 26 | `rejectsNegativeBlankRows` | — | — | — | — | — | ✅ 設定値負数 → `IllegalArgumentException`（steering の 4 ケース外） |
| 27 | `writesWorkbookFileWithSheetPerSection` | A-02, A-05 | B-1, B-2 | C-01, **C-02(複数=2)**, C-03 | — | **E-4(複数=2)** | — |
| 28 | `honorsMarkerColumnColorOverride` | A-02 | B-1 | — | ※マーカーカラム記法 | — | — |
| 29 | `doesNotTintUnclosedBracketColumn` | A-02 | B-1 | — | ※未閉じ括弧 `[half` はマーカーでない | — | — |
| 30 | `writesOmittedMetaAndFieldAsEmpty` | A-06 | B-3 | **C-20(省略)**, **C-21(省略)**, C-11(値 null) | ※null→空セル（メタ側） | E-3(1) | — |
| 31 | `writesSequenceNoForAllSendSyncTypes` | A-11, A-12, A-13, A-14 | B-4 | C-05, C-06(値あり) | — | E-3(1) | — |
| 32 | `wrapsIoFailure` | A-02 | B-1 | — | — | — | 🔺**F3-01**（親に通常ファイルが居座り出力先を作れない）→ `UncheckedIOException` |
| 33 | `roundTripsTable` | A-02 | B-1 | C-05, C-07, C-08, C-09 | ※実 `.xlsx` 往復（文字列・`${}`・空文字） | E-2(複数=2) | — |
| 34 | `roundTripsNullCellAsJavaNull`（**#32 で改称**。旧 `roundTripsNullCellAsLiteralNullString`） | A-02 | B-1 | C-09 | 🔺D3-04 null→`null` 記法→Java null（**#32 で非可逆が解消**）／🔺D3-05 `""` | E-2(1) | — |
| 35 | `roundTripsListMap` | A-05 | B-2 | C-07, C-08, C-09 | ※実 `.xlsx` 往復 | E-2(複数=2) | — |
| 36 | `roundTripsFixedFile` | A-06 | B-3 | C-07, C-10(FIXED), C-16, C-18, C-20, C-21 | ※長さ記法 `-` の往復 | E-3(1) | — |
| 37 | `roundTripsMultipleRecordLayouts` | A-06 | B-3 | C-12(2件), C-16, C-18 | — | E-3(複数=2) | — |
| 38 | `roundTripsVariableFile` | A-08 | B-3 | **C-10(VARIABLE)** ✅, C-21(省略) | — | E-3(1) | — |
| 39 | `roundTripsMessage` | A-10 | B-4 | C-05, C-07, C-14(値あり), C-17, C-18, C-19 | — | E-3(1) | — |
| 40 | `roundTripsSendSyncMessage` | A-11 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-17, C-18, C-19 | — | E-3(1) | — |

**上表 #30 `writesOmittedMetaAndFieldAsEmpty` の入力は #25.5 追補（2026-08-18）で書き直した
（本表は #18 時点のスナップショットのため表そのものは書き換えない）。** `issues.md`
**YML-12 4形目** を修正し、`type` が `null` のフィールド定義は `IllegalArgumentException` で
落とすようにしたため、**C-20(省略＝`null`) はこのテストの担保から外れた**。入力の型を空文字へ変えて
「空セルとして書かれる」記法検査と **C-21(省略)** ・C-11(値 null) の担保は残し、同時に
**番人の境界（弾くのは `null` だけで空文字は弾かない）**を担保する位置づけにした。
`null` を弾くことの担保は `XlsFormatWriterTest#rejectsFieldWithoutTypeInFileBlock` ／
`#rejectsFieldWithoutTypeInMessageBlock` の 2 件である。
**追補（2026-08-19）: この 2 件は §1-D（`d0023c0`）で削除した。**
`FieldDef` が `type` ＝ `null` を生成時に拒否するようになり、辺③の番人へ到達しなくなったためである。
現在の担保は `FieldDefTest#データ型がnullのフィールド定義は生成できない` である（§0.1-2 の対応表）。

**上表 #31 `writesSequenceNoForAllSendSyncTypes` の軸A 欄「A-11, A-12, A-13, A-14」は誤りである
（2026-08-13・#23 レビュー指摘。本表は #18 時点のスナップショットのため書き換えない）。**
同メソッドは 4 タイプのブロックを入力に与えるが、アサートするのは 4 タイプ共通の連番 `"1"` だけで、
**タイプを区別する出力を固定していない**（変異による実測は [§3.1-3](#s3-1-3-sendsync)）。
正しくは軸A 欄は「—」（このテストは軸A を担保しない）である。A-11 は #14 `writesSendSyncMessageWithSequenceNo`
が識別セル全体をアサートしており独立に ✅、A-12〜A-14 は #23 レビュー対応で
`XlsFormatWriterModelTest` に追加した 3 メソッドが担保する。

**上表 40 件の内訳（実測・2026-08-13）**: **実ファイル 10 件 ／ `build` 28 件 ／
SUT のブックを作らない `ExcelFormatConfig` 単体 2 件**（10 ＋ 28 ＋ 2 ＝ 40）。
§3.1-3 の `XlsFormatWriterModelTest` との違いは「`build` か実ファイルか」ではなく
「**全件が実ファイル経路か否か**」である。

導出コマンド（`@Test` ごとにメソッド本体を切り出し、本体に現れる呼び出しで分類する。
`roundTrip` ヘルパ経由の 8 件を実ファイル側に数えるため `roundTrip(` も見る）:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
awk '/^    @Test/{t=1;d=0;s=0;b="";next}
     t{b=b $0"\n"; o=gsub(/\{/,"{"); c=gsub(/\}/,"}"); d+=o-c; if(o>0)s=1;
       if(s&&d<=0){t=0; tot++; if(b~/\.write\(/||b~/roundTrip\(/)w++; else if(b~/build\(/)bu++; else nn++}}
     END{printf "@Test=%d build=%d write=%d neither=%d\n",tot,bu,w,nn}' \
  src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterTest.java
```

```
@Test=45 build=31 write=12 neither=2
```

**2026-08-24 に #31 で導き直した** —— 宣言値は `@Test=40 build=28 write=10 neither=2` のまま
取り残されており、`grep -c '@Test'` の 45（§0.1 の 5 クラス表）と食い違っていた。
**#31 は `XlsFormatWriterTest` を触っていない**ため、ずれは #31 より前に生じたものである
（`839bf64`（§6-K・0 件テーブル）が `roundTripsZeroRowTableWithoutEatingNextBlock` ／
`roundTripsZeroRowListMapWithoutEatingNextBlock` の 2 件を足しており、`write` の 10 → 12 はこれで説明が付く。
`build` の 28 → 31 の 3 件がどのタスクのぶんかは**未確認**）。

`@Test=45` が `grep -c '@Test' <同ファイル>` の 45 と一致することが、切り出しに漏れが無いことの確認になる
（`@Test(expected = ...)` 付きの 4 件を落とさないため、パターンは `^    @Test$` ではなく `^    @Test` である）。
`build` と `write` の両方を呼ぶメソッドは無い。

**実ファイルを書く 12 件**: `roundTrip` ヘルパ経由の 10 件（`roundTripsTable` /
`roundTripsZeroRowTableWithoutEatingNextBlock` / `roundTripsZeroRowListMapWithoutEatingNextBlock` /
`roundTripsNullCellAsJavaNull` /
`roundTripsListMap` / `roundTripsFixedFile` / `roundTripsMultipleRecordLayouts` / `roundTripsVariableFile` /
`roundTripsMessage` / `roundTripsSendSyncMessage`）と `writesWorkbookFileWithSheetPerSection` ／ `wrapsIoFailure`
（**0 件テーブル・0 件 LIST_MAP の 2 件は §6-K の `839bf64` で足されたもので、上の宣言値と同じく
反映が取り残されていた**。出典 `git log -S'roundTripsZeroRowTableWithoutEatingNextBlock' --oneline
-- src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterTest.java`）。
**どちらも呼ばない 2 件**: `eachGroupHasDistinctDefaultColor` ／ `rejectsNegativeBlankRows`。
どちらも `ExcelFormatConfig` だけを叩き、`XlsFormatWriter` のブックを作らない
（前者は既定色 4 種が互いに異なることを、後者は負数を渡すと `IllegalArgumentException` になることを見る）。

### 3.1-2 #22 が追加したテストクラスの担保（2026-08-13 追記）

**本節は #22 で新設した。** §3.1 は「`XlsFormatWriterTest` 40 件」を対象とした #18 時点の事実であり
書き換えていない。ここには #22 が追加した**書き出したファイルを開き直して確かめるテストクラス**の担保だけを記す。
#22 の対象は軸D（セル型 8 ケース）と軸F（異常系）に限る。軸A・軸B・軸C・軸E の欠けは #23 の対象であり、
**その担保は §3.1-3 に記す**（#23 完了・2026-08-13）。

| テストクラス | 追加タスク | 件数 | 検証対象 |
|---|---|---|---|
| `XlsFormatWriterCellTypeTest` | #22（**#32 で 1 件追加**） | 19 | `XlsFormatWriter#write` が書いた実 `.xlsx` を POI で開き直し `Cell#getCellType()` と値を突き合わせる（**17 件**）。加えて ZIP エントリ `xl/sharedStrings.xml` の**生バイト**を検査する（2 件） |
| `XlsFormatWriterInvalidOutputTest` | #22 | 16 | 出力先・シート名の異常系（例外型・メッセージ・ファイルの有無・書けてしまった結果） |

件数は `grep -c "^    @Test" src/test/java/nablarch/test/tool/converter/xls/<クラス>.java` の実測。
`XlsFormatWriterInvalidOutputTest` は #22 時点で 16 件、#25.5 のレビュー対応で
`rejectsNullSheetName` 1 件を足して 17 件になり、**§1-F（`81cf234`）でその 1 件を削除して
16 件に戻った**（2026-08-19 実測）。

> **17 → 16 へ導き直した（2026-08-19）。** `rejectsNullSheetName` は §1-F で
> **読み込み単位の名前 `null` を `TestDataSection` ／ `TestDataContainer` の生成時に拒否するようにしたため
> 辺③の番人へ到達しなくなり、削除した**（削除の記録は本書 §0.1-2 の削除一覧と `issues.md` XLS-33）。
> 移った先は `TestDataContainerTest#名前がnullの読み込み単位は生成できない`（本書 §0.1-2 の対応表）。
> `grep -rn "rejectsNullSheetName" src/test` は **0 件**を返す。

**1 ケース 1 `@Test` で展開している。** 制御文字 6 文字（D3-08）とシート名の禁止文字 7 文字（F3-04）は
ループで束ねず文字ごとに 1 メソッドへ分けた。姉妹クラス `XlsFormatReaderCellTypeTest`（1 ケース 1 `@Test`）に
スタイルを揃えるためであり、ループだと最初の 1 件が落ちた時点で残りが実行されず全体像が取れないためでもある。

**軸D（辺③ 8 ケース。すべて `getCellType()` をアサート。#18 は ✅ 0 ／ 🔺 2 ／ ❌ 6）**

| 要素 | #18 | #22 後 | 担保テストメソッド（`XlsFormatWriterCellTypeTest#`） | 観測したセル型・値 |
|---|---|---|---|---|
| D3-01 `"100"` | ❌ | ✅ | `writesNumericLookingStringAsStringCell` | `CELL_TYPE_STRING`・`"100"`。数値セルにならない（`getNumericCellValue()` が `IllegalStateException`） |
| D3-02 `"=1+1"` | ❌ | ✅ | `writesFormulaLookingStringAsStringCell` | `CELL_TYPE_STRING`・`"=1+1"`。数式セルにならない（`getCellFormula()` が `IllegalStateException`。計算結果 `2` にもならない） |
| D3-03 `"007"` | ❌ | ✅ | `writesLeadingZeroStringAsStringCell` | `CELL_TYPE_STRING`・`"007"`（先頭ゼロが落ちない） |
| D3-04 `null` | 🔺（値のみ） | ✅ | `writesNullValueAsLiteralNullStringCell` | `CELL_TYPE_STRING`・`"null"`（空白セルにならない） |
| D3-05 `""` | 🔺（値のみ） | ✅ | `writesEmptyValueAsEmptyStringCell` | `CELL_TYPE_STRING`・長さ 0（`CELL_TYPE_BLANK` へ退化しない） |
| D3-06 改行含む文字列 | ❌ | ✅ | `writesLineFeedStringAsStringCell`／`replacesCrLfWithSingleLineFeedInStringCell`／`replacesLoneCarriageReturnWithLineFeedInStringCell`／`writesCarriageReturnInDataValueAsBackslashRNotation`（**#32 で追加**） | `LF` は原文のまま。**データ行の値の `CR` は 2 文字の `\` ＋ `r`（Excel 記法）として書かれ、セルへ `CR` が載らない**（#32）。`CR` がセルへ載る経路（カラム名など）では **`LF` へ置換**される（削除ではない）。`CRLF`（4 文字）は `LF` 1 文字にまとまって 3 文字になるが、単独 `CR`（`a`＋`CR`＋`b`）は 3 文字のまま長さが変わらない（`issues.md` **XLS-18**）。**`CR` の 2 件は #32 で入力をデータ行の値からカラム名へ移した** |
| D3-07 32767 文字超 | ❌ | ✅ | `writesStringLongerThanExcelCellLimitAsStringCell`／`writesStringOfExcelCellLimitLengthAsStringCell` | 32768 文字も 32767 文字も `CELL_TYPE_STRING` で内容ごとそのまま書かれる（切り詰め・例外なし。`issues.md` **XLS-19**） |
| D3-08 制御文字含む | ❌ | ✅ | `replacesNulCharacterWithQuestionMark`／`replacesBellCharacterWithQuestionMark`／`replacesVerticalTabCharacterWithQuestionMark`／`replacesUnitSeparatorCharacterWithQuestionMark`／`writesTabCharacterAsIs`／`writesDeleteCharacterAsIs` | XML 1.0 で不正な `U+0000`／`U+0007`／`U+000B`／`U+001F` は `?` へ置換（`issues.md` **XLS-17**）。XML 1.0 で正当な `U+0009`／`U+007F` は原文のまま |

- **すべてファイル経由で確かめている。** メモリ上のブックだけを見ると D3-06（`CR` を含む改行）と
  D3-08（制御文字）を取り逃す（どちらも `build` 直後は原文のままである）。
  該当する担保テストは「メモリ上では保たれている」ことも併せてアサートしている。
- **ただし D3-06 と D3-08 では変化の起きる区間が違う（2026-08-13・レビュー指摘による訂正）。**
  当初この節は両者をまとめて「変わるのは `.xlsx` へ直列化する区間である」と書いていたが、
  書き出した `.xlsx` を `unzip -p <book>.xlsx xl/sharedStrings.xml | od -An -tx1 -c` で展開して
  生バイトを確かめた結果、**D3-06 の `CR` については誤りだった**。

  | ケース | 課題 | `xl/sharedStrings.xml` の生バイト | 変化が起きる区間 | 担保テスト（`XlsFormatWriterCellTypeTest#`） |
  |---|---|---|---|---|
  | D3-08 制御文字 | XLS-17 | `<t>a?b</t>`（`?` ＝ `3f` が焼き込まれている。`00` はファイルに残らない） | **直列化区間** | `burnsQuestionMarkIntoSharedStringsXmlForControlCharacter` |
  | D3-06 `CR` | XLS-18 | `<t>a[CR]b</t>`（`CR` ＝ `0d` が生のまま残る。`&#13;` への退避も無い） | **読み戻し（XML パース）区間** | `keepsCarriageReturnRawInSharedStringsXml` |

  **この 2 件はレビュー指摘（第 3 ラウンド）で追加した。** それまで区間の帰属は手作業のダンプでしか
  確かめておらず、テストは読み戻し値しか見ていなかった。POI／xmlbeans の挙動が変われば
  セル型・値のテスト 16 件は緑のまま、本書と Javadoc の「区間の帰属」だけが誤りになる状態だった。
  追加した 2 件は ZIP エントリを直接開き、パースせずバイト列として突き合わせる。

  したがって「メモリ上では保たれている」というアサートが示すのは、
  **`XlsFormatWriter` 自身が値を変えていないこと**だけであり、
  D3-06 について「直列化で失われた」ことの証明にはならない（ファイルにも残っているため）。
  読み手への影響は、**`.xlsx` をバイトで比較しても `CR` は残って見えるため探す場所を間違える**点である
  （`issues.md` XLS-18 の「影響」欄）。

**軸F（辺③ 4 ケース中 3 ケースを新規担保。F3-02 は対象外）**

| 要素 | #18 | #22 後 | 担保テストメソッド（`XlsFormatWriterInvalidOutputTest#`） | 観測した挙動 |
|---|---|---|---|---|
| F3-01 出力先不在 | 🔺 | ✅ | `createsMissingOutputDirectoriesAndWritesWorkbook` | 例外にならず多階層の出力先が作られ、ブックが書き出される（`XlsFormatWriter#write` の `Files.createDirectories`）。既存の 🔺 `XlsFormatWriterTest#wrapsIoFailure` は「親に通常ファイルが居座りディレクトリを作れない」別ケース（`UncheckedIOException`）であり、両方で出力先まわりが揃う |
| F3-02 `overwrite=false` 衝突 | 対象外 | **対象外（変更なし）** | —（本クラスに該当テストは無い） | `XlsFormatWriter` は `overwrite` を保持しない（保持するのは `ConversionRequest` / `TestDataConverter` / `ConverterMojo`。§0.8-5）。衝突検査は `XlsFormatWriter` を呼ぶ前に上位層（`TestDataConverter#checkOverwrite`）で完結するため、辺③ では再現できない。**ただし「上位層の既存テストが担保している」のは `.yaml` を出力側とする衝突だけである**: `TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse`／`ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict` はどちらも XLS→YAML であり、通るのは `YamlFormatHandler#outputPaths`。`XlsFormatHandler#outputPaths` 自体は `overwrite=false` 下で実行されている（`TestDataConverterTest#convertsYamlToXls`, `#convertsXlsToXls` ほか 1 件（計 3 件）。変異で実証。§0.8-5 と同じ 3 件）が、**`.xlsx` が既存で衝突する分岐**（`checkOverwrite` の `Files.exists(output)` → `ConverterException`）は 1 件も通っていない（§0.8-5 の訂正） |
| F3-03 書き込み権限なし | ❌ | ✅ | `wrapsAccessDeniedExceptionWhenOutputDirectoryIsNotWritable` | `UncheckedIOException: failed to write Excel: <出力先パス>` ＋ 原因 `java.nio.file.AccessDeniedException`。ファイルは作られない。POSIX 権限が効かない環境（root 実行など）では `Assume` でスキップする（確認用ファイルの作成が拒否されることを前提条件として確かめる） |
| F3-04 シート名が Excel 制約違反 | ❌ | ✅ | 禁止文字 7 件: `rejectsSheetNameContainingSlash`／`rejectsSheetNameContainingBackslash`／`rejectsSheetNameContainingQuestionMark`／`rejectsSheetNameContainingAsterisk`／`rejectsSheetNameContainingOpeningBracket`／`rejectsSheetNameContainingClosingBracket`／`rejectsSheetNameContainingColon`。ほか `rejectsEmptySheetName`／`writesSheetNameOfExcelLimitLengthAsIs`／`rejectsSheetNameLongerThanExcelLimit`／`rejectsSheetNameWhoseForbiddenCharacterWouldBeRemovedByTruncation`／`rejectsSheetNameWhoseForbiddenCharacterIsAtTheLastPosition`／`failsWhenSameSheetNameOfLimitLengthIsUsedTwice`／`failsWhenSheetNamesDifferOnlyInCase`（**後半 4 件は #25.5 の XLS-16 修正に伴って改名・書き直した**。改名前は `truncatesSheetNameLongerThanExcelLimitSilently`／`writesSheetNameWhoseForbiddenCharacterIsRemovedByTruncation`／`rejectsSheetNameWhoseForbiddenCharacterSurvivesTruncation`／`failsWhenTruncatedSheetNamesCollide`。件数 14 は変わらない） | **31 文字ちょうどはそのまま書かれる**（切り詰めなし）。**31 文字超は `IllegalArgumentException: シート名が Excel の上限 31 文字を超えています` で失敗し、ブックは作られない**（#25.5 の **XLS-16** 修正後。修正前は例外にならず黙って 31 文字へ切り詰められていた）。同名のシートが 2 枚できる形は `IllegalArgumentException: The workbook already contains a sheet of this name`（**大文字小文字だけが違う名前も同名と判定される**。切り詰めが走らない 3 文字で実測）。空文字は `IllegalArgumentException: sheetName '' is invalid`。禁止文字（`/ \ ? * [ ] :`）は POI の `IllegalArgumentException: Invalid char (x) found at index (i) in sheet name '...'` でブックを作らずに失敗する（下記） |

**F3-04 の「禁止文字は必ず失敗する」は、#25.5 の XLS-16 修正までは無条件には成り立っていなかった。**
POI 3.8 の `XSSFWorkbook#createSheet(String)` は `substring(0, 31)` による切り詰めを
`WorkbookUtil.validateSheetName` **より先に**適用する。したがって
**禁止文字が index 31 以降にある 32 文字以上のシート名は検査に到達せず、例外にならずブックが書き出されていた**。
#22 時点の実測（2026-08-13）: `"a"×31 + "/"`（32 文字）→ 例外なし・`a`×31 のシートを持つブックが生成。対照として
`"a"×30 + "/a"`（32 文字。切り詰め後も `/` が残る）→ `Invalid char (/) found at index (30) in sheet name 'aaa…a/'`
となり、**メッセージのシート名が切り詰め後の 31 文字である**ことが検査順序の裏づけになっていた。

**#25.5 で `XlsFormatWriter` が `createSheet` を呼ぶ前に文字数を検査するようにした**ため
（`XlsFormatWriter#requireValidSheetNameLength`）、32 文字以上の名前は切り詰めに到達せず
`IllegalArgumentException: シート名が Excel の上限 31 文字を超えています。… sheetName='…', length=32` で落ちる。
上の 2 ケースは、切り詰めの抜けが閉じたことを固定する形へ書き直した
（`rejectsSheetNameWhoseForbiddenCharacterWouldBeRemovedByTruncation` は 32 文字なので**文字数**で落ち、
対照の `rejectsSheetNameWhoseForbiddenCharacterIsAtTheLastPosition` は 31 文字なので**禁止文字**で落ちる。
後者のメッセージのシート名は切り詰めが起きないため渡した名前そのものである）。

**F3-04 で #22 が担保する範囲**は、31 文字超・禁止文字（`/ \ ? * [ ] :`）・空文字・31 文字ちょうど（正常側の境界）・
重複判定（同名 2 枚／大文字小文字だけが違う名前）である。
**シート名のアポストロフィ（先頭／末尾）は #22 のスコープ外であり未担保**（タスク #22 の Steps が
F3-04 の範囲を「31 文字超・禁止文字」と定めているため）。
**`null` は辺③の担保では無くなった（2026-08-19 に導き直した）。** いったんは #25.5 のレビュー対応（B-2）で
`requireValidSheetNameLength` に `sheetName == null` の分岐を足し、
`XlsFormatWriterInvalidOutputTest#rejectsNullSheetName` で担保していた。**その後 §1-F（`81cf234`・XLS-33）で
名前 `null` を `TestDataSection` ／ `TestDataContainer` の生成時に拒否するようにしたため、辺③の分岐は
到達しなくなり、分岐も担保テストも削除した。** 現在の `requireValidSheetNameLength`（`XlsFormatWriter`）に
`null` 分岐は無く、その Javadoc が「`null` は検査しない。`TestDataSection` が生成時に拒否するため、
ここへは届かない」と書いている。**担保の現在地は
`TestDataContainerTest#名前がnullの読み込み単位は生成できない`**（本書 §0.1-2 の対応表）。

<a id="s3-1-2-parent-null"></a>

**F3-01 の隣接領域に、src/test 全体で一度も通っていない分岐が 1 つある（2026-08-13・第 3 ラウンドの指摘により開示）。**
上表の F3-01 行は「出力先まわりが揃う」と書いているが、`XlsFormatWriter#write` の
**`parent == null` 分岐は未担保**である。`src/main` のコメントが
「親ディレクトリを持たない相対パス（例: `"foo.xlsx"`）が生成されると `getParent()` は `null` を返すため、
null チェックが必須」と明記している分岐であるため、担保の穴としてここに開示する。

- **成立条件**: `Paths.get(basePath, container.getName() + ".xlsx").getParent()` が `null` になるのは
  `basePath` が空文字のときだけである（`jshell` で実測（2026-08-13）:
  `Paths.get("", "Book.xlsx").getParent()` → `null` ／
  `Paths.get(".output/SampleConversionTest", "Book.xlsx").getParent()` → `.output/SampleConversionTest` ／
  `Paths.get("/tmp/junit123", "Book.xlsx").getParent()` → `/tmp/junit123`）。
- **到達経路の全数**: `XlsFormatWriter#write` が呼ばれるのは src/test では次の 2 経路しかない。
  1. 直接呼び出し **20 か所**（`grep -rn "new XlsFormatWriter(.*)\.write(" src/test --include=*.java | wc -l` → 20。
     内訳は `grep -rc` で `XlsFormatWriterInvalidOutputTest:10`／`XlsFormatWriterTest:3`／
     `XlsFormatWriterCellTypeTest:2`／`TestDataConverterTest:2`／`XlsFormatWriterModelTest:1`／
     `RoundTripTest:1`／`ConverterMojoTest:1`。**2026-08-19 実測**）。
     20 か所の `basePath` 実引数はすべて `folder.getRoot().getAbsolutePath()` ／ `<File>.getAbsolutePath()` ／
     `<Path>.toString()` であり、空文字にならない。
     **#22 時点は 19 か所だった**（内訳に `XlsFormatWriterModelTest` が無かった）。#23 が
     `XlsFormatWriterModelTest` を追加したことで 20 になり、この記述は陳腐化していた
     （2026-08-13・#23 レビュー対応で訂正。増えた 1 か所も `basePath` は絶対パスであり結論は変わらない）。
     **#25.5 のレビュー対応（B-2）で `rejectsNullSheetName` を足して 21 になり、§1-F（`81cf234`）で
     その 1 件を削除して 20 に戻った**（2026-08-19 実測。増減したどちらの 1 か所も
     `folder.getRoot().getAbsolutePath()` であり**結論は変わらない**）。
  2. 本番配線 `TestDataConverter#convert`（`writer.write(container, outputBase.toString())`）。
     `outputBase` は `XlsFormatHandler#resolveOutputBase` が `request.getOutputPath()` から組む。
     テストが渡す出力先は `TemporaryFolder` 由来の絶対パス（`TestDataConverterTest` の `out = folder.newFolder("out").toPath()`／
     `ConverterMojoTest` の `inject(mojo, "output", out.toFile())`）と、`SampleConversionTest` の
     `OUTPUT_BASE = Paths.get(".output/SampleConversionTest")` だけで、いずれも空文字にならない。
- **#22 では埋めない。** #22 の軸F の定義は F3-01 出力先不在／F3-02 `overwrite=false` 衝突／F3-03 書き込み権限なし／
  F3-04 シート名制約違反の 4 要素（§0.7）であり、この分岐はいずれにも当たらない。
  本書の軸要素ではないため §3.3 の件数には算入しない（#22 完了時点の §3.3 の件数は
  要追加 15 ／担保済み 11 ／対象外 1 ＝ 27。#23 とそのレビュー対応を経た現在は
  **要追加 0 ／担保済み 29 ／対象外 1 ＝ 30** である。§3.3 の合計行を参照）。

### 3.1-3 #23 が追加したテストクラスの担保（2026-08-13 追記）

**本節は #23 で新設した。** §3.1（`XlsFormatWriterTest` 40 件）と §3.1-2（#22 の 2 クラス）は
それぞれの時点の事実であり書き換えていない。ここには #23 が追加したテストクラスの担保だけを記す。
#23 の対象は §3.3 が「#23 の対象」として残していた **15 要素（軸A 3・軸C 9・軸E 3）**と、
**#23 のレビューで担保の穴として判明した軸A 3 要素（A-12／A-13／A-14）**である。

| テストクラス | 追加タスク | 件数 | 検証対象 |
|---|---|---|---|
| `XlsFormatWriterModelTest` | #23 | 18 | `XlsFormatWriter#write` が書いた実 `.xlsx` を POI で開き直し、**残り 3 データタイプの識別セル**・**送信同期 3 種の識別セル**・**空のコレクション・多重度 0 が版面のどこに現れる／現れないか**を突き合わせる（15 件）。加えて `issues.md` XLS-20／XLS-21／XLS-22 が主張する**読み戻しの結果**を `XlsFormatReader` で実検査する（3 件） |

件数は `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterModelTest.java` → **18**（2026-08-13 実測）。
`@Test` 18 件と Javadoc の `Given:` 18 件は一致する（`grep -c 'Given:' <同ファイル>` → 18）。

**実ファイル経路で確かめている。** 本クラスは 18 件すべてが `write` の作ったファイルを開き直す。
空の行・空のセルが直列化で落ちないことまで含めて固定するためであり、`XlsFormatWriterCellTypeTest`（#22）と
同じ方針である。**`XlsFormatWriterTest` 40 件との違いは「全件がそうか否か」である**（→ §3.1 末尾の内訳）。

**`XlsFormatWriterTest` の 40 件は #23 では 1 行も変えていなかったが、#23 のレビュー対応で変更した。**
変更は次の 3 点のみで、`@Test` の数・アサートの内容は変えていない
（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterTest.java` → **40** のまま）。

1. セル読み出しヘルパ `cell` / `line` の定義を削除し `XlsFixture` の同名 static メソッドを static import
   （理由と判断は `issues.md`「ヘルパ抽出の要否」）
2. 未使用になった import（`org.apache.poi.ss.usermodel.Cell` / `Row`）の削除
3. クラス Javadoc の訂正（`build` と `write` の内訳。§3.1 末尾）

A-07／A-09 が同クラスに 0 件であることは
`grep -c 'EXPECTED_FIXED\|EXPECTED_VARIABLE' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterTest.java` → **0**
で確かめた（2026-08-13 実測。#23 レビュー対応後も 0 のまま）。

<a id="s3-1-3-sendsync"></a>

**軸A（#18 は ✅ 11 ／ 🔺 2 ／ ❌ 1。#23 完了後は ✅ 14）**

| 要素 | #18 | #23 後 | 担保テストメソッド（`XlsFormatWriterModelTest#`） | 観測した版面 |
|---|---|---|---|---|
| A-01 `DEFAULT` | ❌ | ✅ | `writesDefaultDataTypeMarker` | 識別セル `DEFAULT=T`（例外にならない）。ヘッダ色はその他グループ。読み戻すとブロックが消える（`issues.md` **XLS-20**。`#dropsDefaultDataTypeBlockWhenReadBack`） |
| A-07 `EXPECTED_FIXED` | 🔺 | ✅ | `writesExpectedFixedFileBlockWithLengthRow` | 識別セル `EXPECTED_FIXED=exp.dat`。識別行 → ディレクティブ行 → 名前行 → 型行 → **長さ行** → データ行 |
| A-09 `EXPECTED_VARIABLE` | 🔺 | ✅ | `writesExpectedVariableFileBlockWithoutLengthRow` | 識別セル `EXPECTED_VARIABLE[g2]=exp.csv`。可変長なので**長さ行なし** |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅（**誤り**。下記） | ✅ | `writesExpectedRequestBodyMessagesMarker` | 識別セル `EXPECTED_REQUEST_BODY_MESSAGES[case1]=RM21AA0104_01`。**入力の FW 制御ヘッダが空 Map のため**識別行の次は名前行。データ行の列 0 は**送信系のため**連番（`XlsDataTypeUtil.isSendSyncType` による分岐であり、`fwHeaderFields` が空であることとは無関係） |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅（**誤り**。下記） | ✅ | `writesResponseHeaderMessagesMarker` | 識別セル `RESPONSE_HEADER_MESSAGES[case1]=RM21AA0104_01`。同上 |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅（**誤り**。下記） | ✅ | `writesResponseBodyMessagesMarker` | 識別セル `RESPONSE_BODY_MESSAGES[case1]=RM21AA0104_01`。同上 |

**上表の「入力の FW 制御ヘッダが空 Map のため」は言い換えではなく、書ける事実の限界である。**
当初は 3 行とも「FW ヘッダ行なし」と書いていたが、これは「送信系だから出ない」と読める記述であり、
テストが確かめていない性質を観測事実として記録していた（2026-08-13・#23 レビュー ラウンド3 指摘）。
担保の穴として §3.1-3 末尾（下記「送信系の FW 制御ヘッダ」）と `issues.md` **XLS-24** に開示した。

**A-12／A-13／A-14 の ✅ は #18 以来（#23 の当初版を含め）誤りだった。**
この 3 タイプを辺③で通していたのは `XlsFormatWriterTest#writesSequenceNoForAllSendSyncTypes` だけで、
同メソッドがアサートするのは**データ行の列 0 の連番 `"1"`**（`cell(sheet, 4, 0)`。4 タイプ共通の値）であり、
**タイプを区別する出力を 1 つも固定していなかった**。辺③で識別セルを直接アサートしていたのは
`XlsFormatWriterTest#writesSendSyncMessageWithSequenceNo`（A-11 `EXPECTED_REQUEST_HEADER_MESSAGES` の識別セル）の 1 箇所だけである。

- **変異による実測（2026-08-13。#23 レビュー指摘の再現）**: `XlsFormatWriter#marker` が
  この 3 タイプにだけ別文字列 `"MUTATED"` を返すよう `src/main` を一時的に変異させて全件実行したところ、
  落ちたのは **`RoundTripTest` の 3 件のみ**（`xls_expectedRequestBodyMessages_isPreserved` ／
  `xls_responseHeaderMessages_isPreserved` ／ `xls_responseBodyMessages_isPreserved`）で、
  **`XlsFormatWriterTest` 40 件は全緑**だった（`Tests run: 425, Failures: 3`）。
  往復テストは steering Rules フェーズ2 により正式な担保に数えないため、
  **この時点で A-12／A-13／A-14 は 🔺 相当（正式担保 0）**であった。
- **埋め方**: 開示だけで済ませず、上表の 3 メソッドを追加した。粒度は A-11 の担保テスト（`writesSendSyncMessageWithSequenceNo`）に揃え、
  グループ ID と識別子を含むマーカー全体を固定する。
- **歯があることの実証**: 同じ変異を再度入れて全件実行し、`XlsFormatWriterModelTest` の該当 3 件が
  落ちることを確認した（`Tests run: 428, Failures: 6` ＝ 新規 3 件 ＋ `RoundTripTest` 3 件）。
  変異は確認後に戻し、`git diff HEAD -- src/main` が 0 行であることを確かめた。

**担保の穴: 「送信系は FW 制御ヘッダを書かない」は未担保である（2026-08-13・#23 レビュー ラウンド3 で判明）**

`XlsFormatWriter` のクラス Javadoc は「**送信系 4 種**: MESSAGE と同型だが FW 制御ヘッダは無く、
データ行の列 0 は `no`（連番）」と書いている。しかしこの「FW 制御ヘッダは無く」を担保するテストは
辺③に 1 件も無い。

- `XlsFormatWriter#layoutMessage` は `appendKeyValueRows(l, block.getFwHeaderFields())` を
  **データタイプで分岐せず無条件に**呼ぶ（同メソッドを読んだ結果）。送信系だから FW 制御ヘッダ行が
  出ないのではなく、テスト入力の `fwHeaderFields` が空 Map だから出ていないだけである。
- **変異による実測**: `layoutMessage` を「送信系のときだけ `appendKeyValueRows(l, block.getFwHeaderFields())`
  を呼ばない」——すなわち Javadoc が謳う性質を `src/main` に実装した形——へ一時的に変異させて全件実行したところ、
  **`Tests run: 428, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`** であった。
  1 件も落ちない＝ `src/test` に両者を区別するテストが存在しない。変異は確認後に戻し、
  `git diff HEAD -- src/main | wc -l` → **0** を確かめた。

  ```sh
  cd /home/tie303177/work/nablarch/nablarch-testing-converter
  # XlsFormatWriter#layoutMessage の
  #   appendKeyValueRows(l, block.getFwHeaderFields());
  #   boolean sendSync = XlsDataTypeUtil.isSendSyncType(block.getDataType());
  # を次へ置き換えて全件実行し、確認後 git checkout -- src/main で戻す
  #   boolean sendSync = XlsDataTypeUtil.isSendSyncType(block.getDataType());
  #   if (!sendSync) {
  #       appendKeyValueRows(l, block.getFwHeaderFields());
  #   }
  JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn clean test -Djacoco.skip=true
  ```

- **静的な裏取り**: 送信同期の `MessageDataBlock` に非空の `fwHeaderFields` を渡すテストは `src/test` に
  **0 件**である。`MessageDataBlock` の構築サイトを全数取り、第 1 引数（データタイプ）と第 5 引数
  （`fwHeaderFields`）の組を数える。

  ```sh
  cd /home/tie303177/work/nablarch/nablarch-testing-converter
  perl -0777 -ne '
    while (/(?:new\s+MessageDataBlock|\bmessage)\s*\(/g) {
      my $p = pos($_); my $d = 1; my @a = (""); my $i = $p;
      while ($d > 0 && $i < length($_)) {
        my $c = substr($_, $i, 1);
        if ($c eq "(") { $d++ } elsif ($c eq ")") { $d--; last if $d == 0 }
        if ($c eq "," && $d == 1) { push @a, "" } else { $a[-1] .= $c }
        $i++;
      }
      next unless @a >= 6;
      my ($t, $fw) = ($a[0], $a[4]);
      $t =~ s/\s+//g; $fw =~ s/\s+//g;
      print "$t\t$fw\n";
    }
  ' $(grep -rl "MessageDataBlock" src/test --include=*.java) | sort | uniq -c
  ```

  実測（2026-08-13）:

  ```
        2 DataType.EXPECTED_REQUEST_BODY_MESSAGES	fwHeader()
        1 DataType.EXPECTED_REQUEST_BODY_MESSAGES	newLinkedHashMap<>()
        4 DataType.EXPECTED_REQUEST_HEADER_MESSAGES	fwHeader()
        2 DataType.EXPECTED_REQUEST_HEADER_MESSAGES	map()
        1 DataType.MESSAGE	fwHeader
        2 DataType.MESSAGE	fwHeader("requestId","RM01","userId","${user}")
        2 DataType.MESSAGE	fwHeader("requestId","RM01","userId","${u}")
        5 DataType.MESSAGE	fwHeader()
        2 DataType.MESSAGE	map("requestId","${rid}")
        3 DataType.MESSAGE	map("requestId","R01")
        1 DataType.MESSAGE	newLinkedHashMap<>()
        2 DataType.RESPONSE_BODY_MESSAGES	fwHeader()
        2 DataType.RESPONSE_HEADER_MESSAGES	fwHeader()
        1 DataTypetype	String>directives
        1 dt	newLinkedHashMap<>()
        1 type	fwHeader
        1 type	fwHeader()
        2 type	map()
  ```

  **送信同期 4 種の行は 13 件あり、第 5 引数はすべて空 Map**（`fwHeader()` ／ `map()` ／
  `new LinkedHashMap<>()`）である。非空（引数付きの `fwHeader(...)` ／ `map(...)`）は
  **すべて `DataType.MESSAGE`** に限られる。

  末尾 5 行はデータタイプをリテラルで書いていない構築サイトで、このコマンドでは型が解決できない。
  それぞれ現物を開いて確かめた: `type` の 4 件は `fwHeader()` ／ `map()` ／呼び出し元から渡された
  `fwHeader` 変数（`RoundTripTest#message` ヘルパ。その呼び出し元は上表の `DataType.*` 行として
  解決済み）であり、`dt` の 1 件は送信同期 4 種を回す `MessageDataBlockTest` のループで
  `new LinkedHashMap<>()` を渡す。`DataTypetype` の 1 行はヘルパの**宣言**が引っ掛かったものである。

  **上の変異による実測のほうが上位の根拠**であり、静的な数え上げはその裏取りである
  （静的走査はデータタイプが変数で渡る経路を機械的には追えないが、変異は経路によらず効く）。

- したがって「送信系は FW 制御ヘッダを書かない」という性質は **未担保**である。
  `XlsFormatWriter` のクラス Javadoc の当該記述は、中間モデル側の契約
  （`MessageDataBlock` の Javadoc「`expected_request_*`／`response_*` 経路は空 Map とする（仕様 MS-04）」・
  `XlsFormatReader`／`YamlFormatReader`／`TestCoreReaderAdapter`／`YamlTestCoreAdapter` の
  「FW 制御ヘッダは送信系では常に空」）に依存した記述であり、`XlsFormatWriter` 自身が保証しているわけではない。
- **今回はテストを足さず開示のみ**とした（steering Rules フェーズ2「担保の穴は、テストを足さない場合でも
  台帳に開示する」）。課題としての記録は `issues.md` **XLS-24**。`src/main` は変更していない。

**軸C（#18 は未担保 9。#23 完了後は 0）**

| 要素 | 状態 | 担保テストメソッド（`XlsFormatWriterModelTest#`） | 観測した版面 |
|---|---|---|---|
| C-02 `sections` 空 | ✅ | `writesWorkbookWithoutSheetsWhenContainerHasNoSections` | 例外にならずファイルが作られ、**シート 0 枚**のブックになる（`issues.md` **XLS-23**） |
| C-04 `blocks` 空 | ✅ | `writesEmptySheetWhenSectionHasNoBlocks` | シートは作られるが行が 1 行も無い（`getPhysicalNumberOfRows()` が 0） |
| C-08 `columnNames` 空 | ✅ | ~~`writesEmptyHeaderRowWhenColumnNamesAreEmpty`~~ → **`XlsFormatWriterTest#rejectsTableBlockWithoutColumnNames` ／ `#rejectsListMapBlockWithoutColumnNames`**（#25.5 追補・2026-08-18・`57c1b0d`） | **カラム名 0 件は書き出さず `IllegalArgumentException` で落とす**（`issues.md` **XLS-27**）。従来はカラム名行がデータ行の幅ぶんの空セルになり、読み戻すと**次のブロックを食っていた**。読み戻し検査 `#promotesFirstDataRowToColumnNamesWhenEmptyColumnNamesAreReadBack` は到達不能になったため削除（`issues.md` **XLS-21**） |
| C-09 `rows` 空 | ✅ | `writesTableWithoutDataRowsWhenRowsAreEmpty` | 識別行とカラム名行だけ。データ行の位置は行そのものが無い（`getRow(2)` が `null`） |
| C-12 `FileDataBlock.records` 空 | ✅ | `writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty` | 識別行とディレクティブ行だけ。名前行・型行・長さ行・データ行は 1 行も出ない |
| C-13 `MessageDataBlock.directives` 値あり | ✅ | `writesDirectiveRowsBeforeFwHeaderRowsInMessage` | ディレクティブ行が記述順に並び、**FW 制御ヘッダ行より上**に出る（識別行 → ディレクティブ 2 行 → FW ヘッダ 1 行 → 名前行 → 型行 → 長さ行 → データ行） |
| C-15 `MessageDataBlock.records` 空 | ✅ | ~~`writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty`~~ → **`XlsFormatWriterTest#rejectsMessageBlockWithoutRecords` ／ `#rejectsSendSyncMessageBlockWithoutRecords`**（#25.5 追補・2026-08-18） | **#25.5 追補で挙動が変わった。** #23 時点は「識別行 → ディレクティブ行 → FW 制御ヘッダ行まで。本文の行は 1 行も出ない」版面が書き出され、それは読み戻しても電文として成立しなかった（`issues.md` **YML-12 2形目**）。現在は書き出し側が `IllegalArgumentException` で落とすため、この版面は生成されない。担保テストは上記 2 件へ移り、#23 の 1 件（`#writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty`）は削除した |
| C-17 `RecordLayout.fields` 空 | ✅ | ~~`writesRecordWithoutFieldColumnsWhenFieldsAreEmpty`~~ → **`XlsFormatWriterTest#rejectsRecordWithoutFieldsInFileBlock` ／ `#rejectsRecordWithoutFieldsInMessageBlock`**（#25.5 追補・2026-08-18） | **#25.5 追補で挙動が変わった。** #23 時点は「名前行はレコード種別セルだけ（右は矩形整形の空セル）、型行・長さ行は空セルだけ、データ行の値はフィールド定義が無いまま出る」版面が書き出され、それは読み戻せなかった（`issues.md` **XLS-22**）。現在は書き出し側が `IllegalArgumentException` で落とすため、この版面は生成されない。担保テストは上記 2 件へ移り、#23 の 2 件（`#writesRecordWithoutFieldColumnsWhenFieldsAreEmpty` ／ `#failsToReadBackRecordWithoutFields`）は削除した |
| C-18 `RecordLayout.rows` 空 | ✅ | `writesRecordWithoutDataRowsWhenRecordRowsAreEmpty` | 名前行・型行・長さ行まで。データ行は行そのものが無い |

**§3.1 の表の C-10 タグの付け方に誤りが 2 つある（2026-08-13・#23 レビュー ラウンド3 の仕上げで判明。
§3.1 は #18 時点のスナップショットのため表そのものは書き換えない）。**

- **C-10(FIXED) の非往復担保は `XlsFormatWriterTest#writesFixedFileBlock` と
  `XlsFormatWriterModelTest#writesExpectedFixedFileBlockWithLengthRow` である。** §3.1 の #7
  `writesFixedFileBlock` の軸C 欄に `C-10` タグが欠けており、C-10(FIXED) を挙げているのは
  #36 `roundTripsFixedFile`（往復テスト）だけになっている。表の面だけを読むと A-12〜A-14 と同型の誤判定
  （往復でしか通っていないのに ✅）に見えるが、**実体は穴ではない**。両メソッドとも FIXED 固有の長さ行を
  直接アサートしており（前者は型行の次に `["", "-", "5"]`、後者は `["", "5"]`）、`layoutFile` の
  `block.getFileType() == FileType.FIXED` 分岐を非往復で固定している（両メソッドの本体を開いて確認した）。
- **#38 `roundTripsVariableFile` の `**C-10(VARIABLE)**` の太字は誤りである。** 凡例の太字は
  「その辺でその要素を通す唯一の担保」を意味するが、C-10(VARIABLE) は #8
  `writesVariableFileWithoutLengthRow`（非往復）にも付いており唯一ではない。

**軸E の 0 件（#18 は未担保 3。#23 完了後は 0）**

| 要素 | 状態 | 担保テストメソッド（`XlsFormatWriterModelTest#`） | 備考 |
|---|---|---|---|
| E-1(0 件) セクション内ブロック数 0 | ✅ | `writesEmptySheetWhenSectionHasNoBlocks` | C-04 と同じ入力 |
| E-2(0 件) ブロック内行数 0 | ✅ | `writesTableWithoutDataRowsWhenRowsAreEmpty`（テーブル経路）／`writesRecordWithoutDataRowsWhenRecordRowsAreEmpty`（ファイル経路の値行） | 順に C-09／C-18 と同じ入力 |
| E-3(0 件) ファイル内レコードレイアウト数 0 | ✅ | `writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty`（ファイル経路）／~~`writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty`~~ → **`XlsFormatWriterTest#rejectsMessageBlockWithoutRecords` ／ `#rejectsSendSyncMessageBlockWithoutRecords`**（メッセージ経路。#25.5 追補・2026-08-18） | 順に C-12／C-15 と同じ入力。**メッセージ経路は #25.5 追補で「0 件は書き出さず落とす」に変わった**（C-15 の行に同じ）。ファイル経路の 0 件は記法どおり合法で従来のまま |

**末尾 3 件は軸要素の担保に数えていない。** `#dropsDefaultDataTypeBlockWhenReadBack` ／
`#promotesFirstDataRowToColumnNamesWhenEmptyColumnNamesAreReadBack` ／ `#failsToReadBackRecordWithoutFields` は
書き出したブックを `XlsFormatReader` で読み戻し、`issues.md` XLS-20／XLS-21／XLS-22 の「読み戻すとどうなるか」を
実検査していた。**#25.5 追補（2026-08-18）で 2 件を削除し、現在は `#dropsDefaultDataTypeBlockWhenReadBack` の 1 件である**
（`#failsToReadBackRecordWithoutFields` は XLS-22 の修正で、`#promotesFirstDataRowToColumnNamesWhenEmptyColumnNamesAreReadBack`
は XLS-27 の番人（`57c1b0d`）で、いずれも当該版面が書き出されなくなり読み戻しを検査する対象が無くなったため）。steering Rules フェーズ2（往復テストの扱い）に従い、辺③の担保としても
辺①の担保としても数えない。置く理由は #22 が `xl/sharedStrings.xml` の生バイト検査 2 件を置いたのと同じで、
本体パーサ・`PoiXlsReader` の挙動が変わったときに**担保テストは緑のまま `issues.md` の記述だけが誤りになる**
状態を防ぐためである。したがって **12 件（#23 当初の担保）＋ 3 件（#23 レビュー対応の送信同期の担保）
＋ 3 件（issues 検査）＝ 18 件**である。**#25.5 追補（2026-08-18）で 2 件減り、現在は 16 件である**
（減ったのは XLS-22 の 2 件で、番人の担保 2 件は `XlsFormatWriterTest` 側に置いた）。
**さらに YML-12 2形目 の修正（2026-08-18）で `#writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty` 1 件が減り、現在は 15 件である**
（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterModelTest.java` → 15。
内訳は 10 件＋3 件＋2 件。番人の担保 2 件は同じく `XlsFormatWriterTest` 側に置いた）。

**追補（2026-08-19 実測）: さらに 4 件減り、現在は 11 件である。**
`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterModelTest.java` → **11**。
減った 4 件は `#writesDefaultDataTypeMarker` ／ `#dropsDefaultDataTypeBlockWhenReadBack`（§1-G・XLS-20 で
`DataType.DEFAULT` のブロックを生成時に拒否し、入力を組めなくなった。`7c10654`）と、
`#writesEmptyHeaderRowWhenColumnNamesAreEmpty` ／ `#promotesFirstDataRowToColumnNamesWhenEmptyColumnNamesAreReadBack`
（XLS-27 の番人でカラム名 0 件のブロックを辺③が書かなくなった。`57c1b0d`）である。
**上段の「issues 検査 3 件」は、これで 0 件になった**（3 件とも削除済み）。
残る 11 件はすべて軸要素の担保である
（HEAD の 11 件は `#writesExpectedFixedFileBlockWithLengthRow` ／ `#writesExpectedVariableFileBlockWithoutLengthRow` ／
`#writesExpectedRequestBodyMessagesMarker` ／ `#writesResponseHeaderMessagesMarker` ／ `#writesResponseBodyMessagesMarker` ／
`#writesWorkbookWithoutSheetsWhenContainerHasNoSections` ／ `#writesEmptySheetWhenSectionHasNoBlocks` ／
`#writesTableWithoutDataRowsWhenRowsAreEmpty` ／ `#writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty` ／
`#writesDirectiveRowsBeforeFwHeaderRowsInMessage` ／ `#writesRecordWithoutDataRowsWhenRecordRowsAreEmpty`）。

**追補（2026-08-21 実測。#27）: 1 件増えて 12 件である。**
`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterModelTest.java` → **12**。
増えた 1 件は `#writesOnlyOneBlockWhenSectionHasSingleBlock`（辺③ E-1(1 件)＝セクション内ブロック数 1。
`783810b`）で、これも軸要素の担保である（経緯は [§0.1-2](#s0-1-2) の追補その 8、
判定の根拠は `coverage/axis-matrix.md` §3.5）。

**追補（2026-08-21 実測。#29）: さらに 1 件増えて 13 件である。**
`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterModelTest.java` → **13**。
増えた 1 件は `#writesLengthRowDecidedSolelyByDataType`（辺③の長さ行の有無が `DataType` だけで決まること。
`issues.md` **XLS-44**）である。

**JaCoCo 実測（#25.5 のレビュー 1 巡目まで反映・2026-08-14）**: `XlsFormatWriter` は
分岐 **101 / 104**（3 未到達）・行 **157 / 158**（1 未到達）である。導出コマンド:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes \
  && JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec \
  && awk -F, 'NR > 1 && $3 == "XlsFormatWriter" { print "line " $9 "/" ($8 + $9) " branch " $7 "/" ($6 + $7) }' \
       target/site/jacoco/jacoco.csv
```

出力は `line 157/158 branch 101/104`。**#25.5 追補（XLS-22 の修正・2026-08-18）後は
`line 159/160 branch 103/106` であり、未到達は行 1・分岐 3 のまま変わらない**（番人 `if` の 2 分岐は
両側とも到達済み。[§0.1-2](#s0-1-2) の追補表）。**さらに YML-12 4形目 の修正後は
`line 164/165 branch 107/110`、2形目 の修正後は `line 167/168 branch 109/112` で、
いずれも未到達は行 1・分岐 3 のまま変わらない**（`layoutMessage` に足した番人 `if` の 2 分岐も
両側とも到達済み）。**#23 完了時点（2026-08-13）の実測は
命令 98%（8 / 782 未到達）・分岐 3 / 100 未到達・行 1 / 151 未到達で、未到達は下表の 3 箇所だった。**
#25.5 の XLS-16 修正（`requireValidSheetNameLength` の追加）で分岐の総数が 4 増え、
そのうち `sheetName == null` 側がいったん未到達として残って 4 箇所になったが、
**レビュー 1 巡目（B-2）で null を同じ番人が落とすようにし担保テスト
（`XlsFormatWriterInvalidOutputTest#rejectsNullSheetName`）を足したため、未到達は 3 箇所に戻った。**
行の未到達は一貫して 1 である。残る 3 箇所はいずれも軸A〜F の要素ではない。

| 箇所 | 未到達の内容 | 扱い |
|---|---|---|
| `XlsFormatWriter#write` の `if (parent != null)` | `null` 側の分岐（1 / 2） | 既知の担保の穴。到達経路の全数調査は [§3.1-2 の該当項](#s3-1-2-parent-null) |
| `XlsFormatWriter#layout` の `else if (block instanceof MessageDataBlock)` の false 側と直後の `throw` | 未知のブロック実装（1 / 2 分岐・1 行） | sealed 階層が permit する 3 種すべてを本節と §3.1 が通しているため到達不能。Java イディオムとしての安全網（steering #6 の判断と同じ思想） |
| `XlsFormatWriter#isMarkerColumn` の `columnName != null` | `null` 側の分岐（1 / 6） | steering #9 でコメント済みの防御ガード。`layoutColumnRow` のコメントが「カラム名の null は ColumnRowDataBlock が生成時に拒否するため、ここへは届かない（ModelPreconditions#requireNoNulls）。isMarkerColumn の null 判定は防御として残してある。」と明記している（＝不到達は中間モデルの不変条件によるものである） |

> **行番号を書かないのは `steering.md` Rules（台帳に他ファイルの行番号を書かない）に従うためである。**
> 行番号つきの一覧は `coverage-report.md` §3 にある（#26 の実測）。

<a id="s3-3"></a>

### 3.3 辺③ 未担保一覧（#22〜#23 が埋めた対象）

計上単位と「状態」の 3 分類は §1.3 の規則に従う。

**本表は #23 の実測結果に合わせて更新した（2026-08-13）。** #18 時点は「要追加 26 ／ 対象外 1」、
#22 完了時点は「要追加 15 ／ 担保済み 11 ／ 対象外 1」であった。#23 が残る 15 件（軸A 3・軸C 9・軸E 3）を
埋めたため、**辺③の「要追加」は 0 件**になった。#18 時点の分類は各行の「#18」列に、
#22 完了時点の分類は「#22 後」列に残した。

**ただし #18 の「要追加 26」自体が 3 件少なかった（2026-08-13・#23 レビュー指摘）。**
軸A の A-12／A-13／A-14 は #18 で ✅ と判定されていたため本表に 1 件も上がっていなかったが、
実際には正式担保 0（🔺 のみ）だった（変異で実証。[§3.1-3](#s3-1-3-sendsync)）。
**#23 レビュー対応でテスト 3 件を追加して埋めた**うえで、下表の A 行に追記した。
これに伴い辺③の軸要素の総計は 27 → **30**（うち対象外 1）になる。

| 軸 | 未担保要素 | #18 の状態 | #22 後の状態 | #23 後の状態 | 件数 |
|---|---|---|---|---|---|
| A | A-01 `DEFAULT`（writer 側は到達可能。§0.8-7）／A-07 `EXPECTED_FIXED`（🔺 `RoundTripTest#xls_expectedFixed_isPreserved`）／A-09 `EXPECTED_VARIABLE`（🔺 `RoundTripTest#xls_expectedVariable_isPreserved`） | 要追加 | 要追加（#23。#22 の対象外） | **担保済み（#23）** — 順に `XlsFormatWriterModelTest#writesDefaultDataTypeMarker`／`#writesExpectedFixedFileBlockWithLengthRow`／`#writesExpectedVariableFileBlockWithoutLengthRow`（§3.1-3）。記録した課題は `issues.md` **XLS-20** | 3 |
| A | **A-12 `EXPECTED_REQUEST_BODY_MESSAGES`／A-13 `RESPONSE_HEADER_MESSAGES`／A-14 `RESPONSE_BODY_MESSAGES`**（#18 は ✅ と誤判定。実際は 🔺 `RoundTripTest` の 3 件のみ） | （表に上がっていなかった） | （同左） | **担保済み（#23 レビュー対応）** — 順に `XlsFormatWriterModelTest#writesExpectedRequestBodyMessagesMarker`／`#writesResponseHeaderMessagesMarker`／`#writesResponseBodyMessagesMarker`（[§3.1-3](#s3-1-3-sendsync)）。変異で穴と歯の両方を実証済み | 3 |
| B | （なし。ただし**辺③では B-1 `TableDataBlock` と B-2 `ListMapBlock` のコード経路が同一**であり、軸B は軸A から独立していない。テストを足しても通る `src/main` の経路は増えないため件数は 0 のままとする。`XlsFormatWriter#layout` が `ColumnRowDataBlock` ／ `FileDataBlock` ／ `MessageDataBlock` の 3 分岐しか持たず、`TableDataBlock` と `ListMapBlock` はどちらも `layoutColumnRow` を通るためである。版面上で両者を分けるのは `layoutColumnRow` が `getDataType()` から作る識別セルだけで、それは軸A そのものである） | — | — | — | 0 |
| C | C-02 sections 空（writer 側は到達可能。§0.8-6）／C-04 blocks 空／C-08 columnNames 空／C-09 rows 空／C-12 FileDataBlock.records 空／**C-13 MessageDataBlock.directives 値あり**／C-15 MessageDataBlock.records 空／C-17 fields 空／C-18 RecordLayout.rows 空 | 要追加 | 要追加（#23。#22 の対象外） | **担保済み（#23）** — 順に `XlsFormatWriterModelTest#writesWorkbookWithoutSheetsWhenContainerHasNoSections`／`#writesEmptySheetWhenSectionHasNoBlocks`／`#writesEmptyHeaderRowWhenColumnNamesAreEmpty`（**#25.5 追補で削除。C-08 の担保は `XlsFormatWriterTest#rejectsTableBlockWithoutColumnNames` ／ `#rejectsListMapBlockWithoutColumnNames` へ移った** —— `issues.md` **XLS-27** の当面の対応で、カラム名 0 件のテーブル系ブロックは書き出さず `IllegalArgumentException` で落とすようになったため。判定 ✅ と件数 9 は変わらない）／`#writesTableWithoutDataRowsWhenRowsAreEmpty`／`#writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty`／`#writesDirectiveRowsBeforeFwHeaderRowsInMessage`／`#writesMessageBlockWithMetaRowsOnlyWhenRecordsAreEmpty`（**#25.5 追補で削除。C-15 の担保は `XlsFormatWriterTest#rejectsMessageBlockWithoutRecords` ／ `#rejectsSendSyncMessageBlockWithoutRecords` へ移った** —— `issues.md` **YML-12 2形目** を修正し、本文レコード 0 件の電文は書き出さず `IllegalArgumentException` で落とすようになったため。判定 ✅ と件数 9 は変わらない）／`#writesRecordWithoutFieldColumnsWhenFieldsAreEmpty`（**#25.5 追補で削除。C-17 の担保は `XlsFormatWriterTest#rejectsRecordWithoutFieldsInFileBlock` ／ `#rejectsRecordWithoutFieldsInMessageBlock` へ移った** —— `issues.md` **XLS-22** を修正し、空 `fields` は書き出さず `IllegalArgumentException` で落とすようになったため。判定 ✅ と件数 9 は変わらない）／`#writesRecordWithoutDataRowsWhenRecordRowsAreEmpty`（§3.1-3）。記録した課題は `issues.md` **XLS-21〜XLS-23**（**XLS-22 は #25.5 で修正済み**） | 9 |
| D | D3-01〜D3-08 全 8 ケース（D3-04／D3-05 は値のみの 🔺。`getCellType()` をアサートするテストは全件ゼロ） | 要追加 | **担保済み（#22。#32 で 1 件追加）** — `XlsFormatWriterCellTypeTest` 19 件（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterCellTypeTest.java` → 19）。内訳は **8 ケース**＋改行の異表記 **3 件**（うち 1 件は #32 追加の `writesCarriageReturnInDataValueAsBackslashRNotation`）・上限ちょうど **1 件**・XML で表現できない制御文字を 1 文字 1 メソッドへ展開した増分 **3 件**・XML で正当な制御文字の対照 **2 件**（ここまで 16 件。読み戻したセル型と値を突き合わせる分）＋ `xl/sharedStrings.xml` の**生バイト**を検査する **2 件**（`burnsQuestionMarkIntoSharedStringsXmlForControlCharacter`／`keepsCarriageReturnRawInSharedStringsXml`。第 3 ラウンドで追加）＝ 8＋3＋1＋3＋2＋2 ＝ **19**。要素別の担保テストメソッドは §3.1-2 の軸D 表。記録した課題は `issues.md` **XLS-17〜XLS-19** | 担保済み（変更なし） | 8 |
| E | E-1(0 件)／E-2(0 件)／E-3(0 件) | 要追加 | 要追加（#23。#22 の対象外） | **担保済み（#23）** — E-1(0) は C-04、E-2(0) は C-09／C-18、E-3(0) は C-12／C-15 と同じ入力（上の C 行のテストメソッド。§3.1-3 の軸E 表） | 3 |
| F | F3-01 出力先不在（🔺 のみ）／F3-03 書き込み権限なし／F3-04 シート名制約違反 | 要追加 | **担保済み（#22）** — `XlsFormatWriterInvalidOutputTest` 16 件（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsFormatWriterInvalidOutputTest.java` → 16。**2026-08-19 実測**。#22 時点は 16 件、#25.5 のレビュー対応（B-2）で `rejectsNullSheetName` を 1 件足して 17 件になり、§1-F（`81cf234`）でその 1 件を削除して 16 件に戻った。`null` の担保は `TestDataContainerTest#名前がnullの読み込み単位は生成できない` へ移っている）。内訳は F3-01 **1 件**（`createsMissingOutputDirectoriesAndWritesWorkbook`）・F3-03 **1 件**（`wrapsAccessDeniedExceptionWhenOutputDirectoryIsNotWritable`）・F3-04 **14 件**（禁止文字を 1 文字 1 メソッドへ展開した **7 件**＋空文字 **1 件**＋31 文字ちょうど **1 件**＋31 文字超の拒否 **1 件**＋切り詰めと禁止文字検査の境界 **2 件**＋同名 2 枚の衝突 **1 件**＋大文字小文字だけが違う名前の衝突 **1 件**（`failsWhenSheetNamesDifferOnlyInCase`。第 3 ラウンドで追加）＝ 7＋1＋1＋1＋2＋1＋1 ＝ 14。メソッド名の全列挙は §3.1-2 の軸F 表）＝ 1＋1＋14 ＝ **16**（**`null` 1 件は §1-F で削除**）。要素別の担保テストメソッドは §3.1-2 の軸F 表。記録した課題は `issues.md` **XLS-16**（**#25.5 で修正済み**。31 文字超は黙って切り詰めるのをやめて拒否するようになり、上記 4 件を改名・書き直した。この改名で件数は増えていない） | 担保済み（変更なし） | 3 |
| F | F3-02 `overwrite=false` 衝突 — `XlsFormatWriter` は `overwrite` を保持しない。衝突検査は上位層の `TestDataConverter#checkOverwrite` で完結する。上に挙げた既存テストが通すのは XLS→YAML の経路であり、**`.xlsx` を出力側とする衝突は未担保**（§0.8-5 の訂正） | 対象外（衝突検査は上位層） | 対象外（変更なし。#22 でも辺③に書かない） | 対象外（変更なし。#23 でも辺③に書かない） | 1 |
| **合計** | | **要追加 26（実際は 29）／ 到達不能 0 ／ 対象外 1** | **要追加 15（実際は 18）／ 担保済み 11 ／ 到達不能 0 ／ 対象外 1** | **要追加 0 ／ 担保済み 29 ／ 到達不能 0 ／ 対象外 1** | **30（うち対象外 1）** |

**合計の検算**（表の「件数」列を上から順に足す）:

- 担保済み: A 3 ＋ A 3（送信同期。#23 レビュー対応）＋ C 9 ＋ D 8 ＋ E 3 ＋ F 3 ＝ **29**
- 要追加: **0**
- 対象外: F3-02 **1**
- 総計: 29 ＋ 0 ＋ 1 ＝ **30**（B は 0 件）

導出コマンド（表の「件数」列を機械的に足す。**そのまま実行すれば 30 になる**）:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter/.rn/ntf-test-data-converter/coverage
awk '/^\| 軸 \| 未担保要素 \| #18 の状態 \| #22 後の状態 \| #23 後の状態 \| 件数 \|/,/^\| \*\*合計\*\*/' inventory.md \
  | grep -vE '^\| (軸|---|\*\*合計)' \
  | awk -F'|' '{gsub(/[^0-9]/,"",$(NF-1)); s+=$(NF-1)} END {print s}'
```

「#18」「#22 後」の列に括弧で添えた「実際は」は、A-12〜A-14 が当時から未担保だったことを
遡って数え直した値である（当時の表には行として存在しなかった）。

**軸要素の外に、開示すべき担保の穴が 1 つある。** `XlsFormatWriter#write` の `parent == null` 分岐は
src/test 全体で一度も通っていない。§0.7 の軸F 4 要素のいずれにも当たらないため上表には算入していないが、
F3-01 の隣接領域であり `src/main` のコメントが「null チェックが必須」と明記している分岐であるため開示する。
到達経路の全数と実測は [§3.1-2 の該当項](#s3-1-2-parent-null)。

**#23 レビューで判明した「表に上がっていなかった穴」の教訓。** A-12〜A-14 は「そのデータタイプの
ブロックを入力に与えて何かをアサートしている」テストが存在したため ✅ と判定されていたが、
アサートしていた値（連番 `"1"`）は**4 タイプで同じ**であり、タイプを区別していなかった。
判定基準（§判定基準）の「その `DataType` のブロックが生成／書き出されることをアサートしている」を
満たすかどうかは、**入力の型ではなく出力がその型に依存しているか**で見る必要がある。
迷ったときは変異（その型だけ出力を変える）を入れて落ちるテストを見るのが確実である。

**特に大きな空欄**（#18 時点の評価）: `getCellType()` を使ったテストが 1 件も存在しないため軸D 8 ケース全滅。#22 の主眼。
→ **#22 で解消した。** 次いで `MessageDataBlock.directives` に値を入れて書き出すテストが 0 件（C-13）。
→ **#23 で解消した**（`XlsFormatWriterModelTest#writesDirectiveRowsBeforeFwHeaderRowsInMessage`）。

**辺③の「要追加」は 0 件である。** ただし上の `parent == null` 分岐に加え、#23 の JaCoCo 実測で
`XlsFormatWriter` の未到達が 3 箇所（分岐 3 / 100・行 1 / 151）であることを確かめた。
残る 2 箇所（`layout` の未知ブロック向け `throw`、`isMarkerColumn` の `null` ガード）はいずれも
Java イディオムとしての安全網であり軸要素ではない。内訳は §3.1-3 末尾の JaCoCo 表。

---

<a id="s4"></a>

## 4. 辺④ 中間モデル→YAML（`YamlFormatWriterTest` 33 件）

### 4.1 テストメソッド → 担保する軸要素

| # | テストメソッド | 軸A | 軸B | 軸C | 軸D | 軸E | 軸F |
|---|---|---|---|---|---|---|---|
| 1 | `serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | A-02 | B-1 | C-06(省略→`group_id` キーなし), C-07, C-08, C-09 | **D4-04 `null`** ✅, **D4-05 `""`** ✅, ※`${}` の全値クォート | E-2(複数=2) | — |
| 2 | `serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId` | A-03 | B-1 | C-06(値あり `[case01]`→`case01`) | — | E-1(複数=2) | — |
| 3 | `serializeTable_completed_usesExpectedCompleteTablesKey` | A-04 | B-1 | C-05 | — | E-2(1) | — |
| 4 | `serializeListMap_usesIdKeyAndColumnOrder` | A-05 | B-2 | C-07, C-08, C-09 | D4-04 `null` | E-2(複数=2) | — |
| 5 | `serializeFile_fixedWithDirectivesAndOmittedLength` | A-06 | B-3 | C-07, C-10(FIXED), C-11(値あり), C-12(2件), C-16(値あり), C-17, C-18, C-19, C-20, C-21(値あり＋省略) | D4-05 `""` | **E-3(複数=2)**, E-2(複数=2) | — |
| 6 | `serializeFile_variableOmitsDirectivesAndRecordTypeAndLength` | A-09 | B-3 | C-10(VARIABLE), **C-11(空)**, **C-16(省略)**, **C-21(省略)** | — | E-3(1) | — |
| 7 | `serializeMessage_withDirectivesAndFwHeader` | A-10 | B-4 | C-07, **C-13(値あり)** ✅, C-14(値あり), C-15, C-16, C-17, C-18, C-19, C-20, C-21 | ※`${}` | E-3(1) | — |
| 8 | `serializeMessage_emptyBody_emitsIdOnly` | A-10 | B-4 | C-07, C-13(空), C-14(空), **C-15(空)** ✅ | — | **E-3(0)** ✅ | — |
| 9 | `serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField` | A-11 | B-4 | C-06(値あり), C-07, C-13(空), C-14(空), C-16(省略), C-17, C-18, C-19, C-20, C-21 | ※`${}` | E-3(1) | — |
| 10 | `serializeSendSync_allFourSectionKeys` | A-11, A-12, A-13, A-14 | B-4 | C-05 | — | E-1(複数=4) | — |
| 11 | `serialize_multipleSections_separatedByBlankLineInEncounterOrder` | A-02, A-10 | B-1, B-4 | C-05 | — | **E-1(複数=2)** | — |
| 12 | `serialize_emptySection_isEmptyString` | — | — | **C-04(空)** ✅ | — | **E-1(0)** ✅ | — |
| 13 | `serialize_escapesQuotesBackslashAndControlChars` | A-02 | B-1 | C-09 | **D4-07 改行含む** ✅（`\n`/`\r`/`\t`/`\x01`/`"`/`\` のエスケープ） | E-2(1) | — |
| 14 | `serialize_surrogatePair_isOutputAsUtf8WithoutEscape` | A-02 | B-1 | C-09 | ※BMP 外文字（U+1F600）の非エスケープ出力 | E-2(複数=2) | — |
| 15 | `serialize_quotesKeyContainingSpecialChars` | A-10 | B-4 | C-13(値あり) | ※**キー**中のコロン・空白のクォート（D4-09 の値側ではない） | — | — |
| 16 | `serialize_emptyKey_isQuoted` | A-10 | B-4 | C-13(値あり) | ※空キーのクォート | — | — |
| 17 | `serialize_distinguishesNullFromNullString` | A-02 | B-1 | C-09 | **D4-03 `"null"`** ✅, **D4-04 `null`** ✅ | E-2(複数=2) | — |
| 18 | `serialize_emptyRows_emitsEmptyFlowList` | A-02 | B-1 | **C-09(空)** ✅ | — | **E-2(0)** ✅ | — |
| 19 | `serialize_emptyColumnsRow_emitsEmptyFlowMap` | A-02 | B-1 | **C-08(空)** ✅ | — | E-2(1) | — |
| 20 | `serialize_recordWithEmptyFieldsAndRows_emitsEmptyFlowLists` | A-06 | B-3 | **C-17(空)** ✅, **C-18(空)** ✅, C-16(省略) | — | E-3(1) | — |
| 21 | `serialize_rowShorterThanColumns_fillsMissingWithNull` | A-02 | B-1 | C-08, C-09 | D4-04 `null`（補完） | — | ✅ 行と列の数の不一致（行が短い → null 補完） |
| 22 | `serialize_fieldWithNullType_omitsType` | A-09 | B-3 | **C-20(省略)** ✅ | — | E-3(1) | — |
| 23 | `serialize_keyStartingWithIndicator_isQuoted` | A-10 | B-4 | C-13(値あり) | ※**キー**先頭の YAML インジケータ `-`（D4-09 の値側ではない） | — | — |
| 24 | `serialize_groupId_isWrittenVerbatim`（**#34 で改称**。旧 `serialize_unbracketedGroupId_isUsedAsRawValue`） | A-02 | B-1 | C-06(生値 `raw`) | — | — | ※書き出しが groupId を加工しないことの番人（**#34 以降、中間モデルは生値を持つ**。旧「防御的経路」は `rawGroup` の削除で消えた） |
| 25 | `serialize_unsupportedDataType_throws` | **A-01 `DEFAULT`** ✅ | B-1 | C-05 | — | — | ✅ 未サポート `DataType` → `IllegalArgumentException` |
| 26 | `write_ioError_throwsUncheckedIOException` | A-02 | B-1 | — | — | — | 🔺**F4-01**（親に通常ファイルが居座り出力先を作れない）→ `UncheckedIOException` |
| 27 | `write_writesEachSectionAsYamlFileWithSerializedContent` | A-02 | B-1 | C-01, C-02(1件), C-03 | — | E-4(1) | — |
| 28 | `roundTrip_table_isPreservedThroughRealReader` | A-02 | B-1 | C-05, C-07, C-08, C-09 | ※実 YAML 往復（`${}`/`null`/`""`） | E-2(複数=2) | — |
| 29 | `roundTrip_fixedFile_isPreservedThroughRealReader` | A-06 | B-3 | C-05, C-07, C-10(FIXED), C-12(2件), C-16, C-19, C-20, C-21(値あり＋省略), C-18 | ※実 YAML 往復 | E-3(複数=2) | — |
| 30 | `roundTrip_message_preservesFwHeaderAndBody` | A-10 | B-4 | C-05, C-07, C-14(値あり), C-16, C-18 | ※`${}` の往復 | E-3(1) | — |
| 31 | `roundTrip_sendSync_preservesGroupIdAndNoField` | A-11 | B-4 | C-05, C-06(値あり), C-07, C-14(空), C-17, C-18, C-19, C-20, C-21 | ※`${}` の往復 | E-3(1) | — |
| 32 | `roundTrip_leadingTrailingWhitespace_isPreservedThroughRealReader` | A-02 | B-1 | C-09 | ※前後・中間の半角/全角空白が往復で脱落しない | E-2(1) | — |
| 33 | `roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader` | A-02 | B-1 | C-09 | 🔺**D4-01 `"100"` 相当（`"123"`）**・D4-03 `"null"`・D4-04 `null` の往復区別（出力 YAML の記法アサートではない） | E-2(複数=3) | — |

**上表 #20 `serialize_recordWithEmptyFieldsAndRows_emitsEmptyFlowLists` は #25.5 追補（2026-08-18）で
`serialize_recordWithEmptyRows_emitsEmptyFlowList` へ書き直した（本表は #18 時点のスナップショットのため
表そのものは書き換えない）。** `issues.md` **YML-12 3形目**（＝辺④の XLS-22）を修正し、`fields` が空の
レコードレイアウトは `IllegalArgumentException` で落とすようにしたため、**C-17(空) はこのテストの担保から外れた**。
書き直し後が担保するのは **C-18(空)** ✅ と C-16(省略) だけである（入力のフィールドを 1 件にして `rows: []` の
記法検査は残した）。C-17(空) は番人の担保
`YamlFormatWriterTest#serialize_recordWithoutFieldsInFileBlock_rejected` ／
`#serialize_recordWithoutFieldsInMessageBlock_rejected` の 2 件へ移った。

**上表 #22 `serialize_fieldWithNullType_omitsType` は #25.5 追補（2026-08-18）で削除した（本表は
#18 時点のスナップショットのため表そのものは書き換えない）。** `issues.md` **YML-12 4形目** を修正し、
`type` が `null` のフィールド定義は `IllegalArgumentException` で落とすようにしたため、
**このテストが固定していた `{name: "c1"}` という出力そのものが無くなった**（C-20(省略) は辺④の
担保から外れた）。代わりに置いたのは番人の担保 2 件
（`#serialize_fieldWithNullTypeInFileBlock_rejected` ／ `#serialize_fieldWithNullTypeInMessageBlock_rejected`）と、
境界（弾くのは `null` だけで空文字は弾かない）を主張する `#serialize_fieldWithEmptyType_emitsEmptyType` 1 件である。

**上表 #8 `serializeMessage_emptyBody_emitsIdOnly` は #25.5 追補（2026-08-18）で削除した（本表は
#18 時点のスナップショットのため表そのものは書き換えない）。** `issues.md` **YML-12 2形目** を修正し、
本文レコード 0 件の電文は `IllegalArgumentException` で落とすようにしたため、
**このテストが固定していた「`id:` だけの電文」という出力そのものが無くなった**
（**C-15(空)** と **E-3(0)** は辺④の「書き出せる」担保から外れた）。代わりに置いたのは番人の担保 2 件
（`#serializeMessage_withoutRecords_rejected` ／ `#serializeSendSync_withoutRecords_rejected`）で、
境界（ファイルブロックの `records` 0 件は記法どおり合法）は
`YamlFormatWriterModelTest#writesEmptyRecordsListForFileBlockWithoutRecords` が担保する。
なお同じく削除した `YamlFormatWriterModelTest#failsToReadBackMessageBlockWithoutRecords`
（読み戻せないことを固定していた「緑の嘘」の最後の 1 本）も、修正で対象の出力が無くなったため置き換え先は無い。

**上表 #15 `serialize_quotesKeyContainingSpecialChars` ／ #16 `serialize_emptyKey_isQuoted` ／
#23 `serialize_keyStartingWithIndicator_isQuoted` の 3 件は、#25.5 追補（2026-08-18）で
フィクスチャだけを直した。** いずれも都合上**本文レコード 0 件の `MessageDataBlock`** を入力に使っており、
上の番人に落とされるようになったためである。**テストの意図（キー側のクォート判定）と軸の担保は変えていない** ——
本文レコードを 1 件足しただけで、#15／#16 は期待 YAML にその 1 件ぶんを追記し、#23 は `contains` の
アサートなので入力の差し替えだけで済んでいる。`YamlFormatWriterModelTest#quotesBooleanAndDateLookingValuesInFwHeader`
（[§4.1-2](#s4-1-2) の軸D `emitMap` 経路）も同じ理由・同じ直し方である。

**上表 #10 `serializeSendSync_allFourSectionKeys` の軸A 欄「A-11, A-12, A-13, A-14」は誤りである
（2026-08-14・#25 レビュー指摘。本表は #18 時点のスナップショットのため書き換えない）。**
同メソッドは 4 タイプのブロックを**まとめて 1 つの出力に**直列化し、4 つのセクションキーが
「どこかに現れる」ことを `assertTrue(yaml.contains(...))` で見るだけで、
**`DataType` → セクションキーの写像を 1 つも固定していない**（変異による実測は [§4.1-2](#s4-1-2-sendsync)）。
正しくは軸A 欄は「—」（このテストは軸A を担保しない）である。A-11 は #9
`serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField` が単独ブロックの出力全文を
完全一致でアサートしており独立に ✅、A-12〜A-14 は #25 レビュー対応で
`YamlFormatWriterModelTest` に追加した 3 メソッドが担保する。
**辺③の #31 `writesSequenceNoForAllSendSyncTypes` とまったく同じ形の誤りである**（§3.1 の同種の注記）。

<a id="s4-1-2"></a>

### 4.1-2 #25 が追加したテストクラスの担保（2026-08-14 追記）

**本節は #25 で新設した。** §4.1 は「`YamlFormatWriterTest` 33 件」を対象とした #18 時点の事実であり
書き換えていない。ここには #25 が追加したテストクラスの担保だけを、**軸要素 → メソッド**の向きで記す
（§4.1 は メソッド → 軸要素 の向き。対象クラスが重ならないため二重記載にならない。
4 辺を通した逆引きの正は #27 の `axis-matrix.md`）。

| テストクラス | 件数 | 担う軸 |
|---|---|---|
| `YamlFormatWriterScalarTest` | 16 | 軸D（9 ケースのうち #25 が埋めた分）＋ `issues.md` YML-13 |
| `YamlFormatWriterModelTest` | 17（**#25.5 追補後は 14** —— YML-12 3形目 で `#failsToReadBackRecordWithoutFields`、4形目 で `#failsToReadBackFieldWithoutType`、2形目 で `#failsToReadBackMessageBlockWithoutRecords` を削除した） | 軸A（A-07／A-08／**A-12〜A-14**）・軸C（C-02／C-12）・軸D（D4-02／D4-08 を `emitMap` 経路で）・軸E（E-4）＋ キーのクォート判定 ＋ `issues.md` YML-12／YML-08／YML-10 |
| `YamlFormatWriterInvalidOutputTest` | 2 | 軸F（F4-01／F4-03） |

件数の導出コマンド:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
for f in YamlFormatWriterScalarTest YamlFormatWriterModelTest YamlFormatWriterInvalidOutputTest; do
  printf "%-34s %s @Test\n" "$f" "$(grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/$f.java)"
done
```

出力は順に **16** ／ **17** ／ **2**（合計 **35**）。**#25.5 追補（2026-08-18）後は 16 ／ 14 ／ 2（合計 32）である**
（`YamlFormatWriterModelTest` から YML-12 の 3形目・4形目・2形目 の読み戻し検査を 1 件ずつ、計 3 件削除した。
いずれも番人の担保は `YamlFormatWriterTest` 側に置いたため、このコマンドの対象 3 クラスには入らない）。
**さらに #27（2026-08-21）で 1 件増え、16 ／ 15 ／ 2（合計 33）になった**
（`YamlFormatWriterModelTest#writesOneYamlFileWhenContainerHasSingleSection`。辺④ E-4(1 件)＝
コンテナ内セクション数 1 の担保。`6d12021`。経緯は [§0.1-2](#s0-1-2) の追補その 8、
判定の根拠は `coverage/axis-matrix.md` §4.5）。**#29（2026-08-21）でさらに 2 件増え、
現在は 16 ／ 17 ／ 2（合計 35）である**（`YamlFormatWriterModelTest#writesFileTypeKeyDerivedFromDataType` ／
`#restoresAllFourFileDataTypesThroughRealReader`。辺④ 軸A の A-06〜A-09 の担保。`issues.md` **XLS-44**）。全体は `mvn clean test -Djacoco.skip=true` で
**501 → 536**（Failures 0 ／ Errors 0 ／ Skipped 0。**これは #25 完了時点の値**。#25.5 の途中では
`Tests run: 540, Failures: 0, Errors: 0, Skipped: 2` であり、**現在（YML-12 2形目 まで反映・2026-08-18）は
`Tests run: 547, Failures: 0, Errors: 0, Skipped: 0` である** —— [§0.1-2](#s0-1-2)）。
**うち 5 件は #25 のレビュー対応で足した**（A-12〜A-14 の 3 件、キーのクォート 1 件、
`emitMap` 経路の記法 1 件。いずれも `YamlFormatWriterModelTest`。当初版は 30 件・531 件、
レビュー修正ラウンド 1 の時点では 32 件・535 件だった）。

**`serialize` を直接アサートするのは Scalar／Model の 2 クラスである。**
`YamlFormatWriterInvalidOutputTest` は `serialize` を一度も呼ばず、`write` が書き出したファイル本文と
送出された例外を見る。往復を見るものは `writer.write(...)` で実ファイルを書き、本番配線の
`new YamlFormatReader().read(...)` で読み戻す
（辺②側の単独の担保は §2.1-2 の `YamlFormatReaderScalarTest` にある）。

**軸D（§0.5 の辺④ 9 ケース。記法と往復を分けて示す）**

「記法」列は**出力 YAML の記法そのもの**（引用符の有無・折り返し・NULL 表現）をアサートしているメソッド、
「往復」列は**書いて読み戻したときに元の文字列へ戻るか**をアサートしているメソッドである。
クラス名を書いていないものは `YamlFormatWriterScalarTest` のメソッド。

| 要素 | 出力 YAML の記法（実測） | 記法の担保 | 往復の担保 | 復元 |
|---|---|---|---|---|
| D4-01 `"100"` | `V: "100"` | `writesNumberLookingStringAsDoubleQuotedScalar` | `restoresNumberLookingStringThroughRealReader` | **する** |
| D4-02 `"true"` | `V: "true"` | `writesBooleanLookingStringAsDoubleQuotedScalar` | `restoresBooleanLookingStringThroughRealReader` | **する** |
| D4-03 `"null"` | `V: "null"` | `YamlFormatWriterTest#serialize_distinguishesNullFromNullString`（**既存**） | `YamlFormatWriterTest#roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader`（**既存**） | **する** |
| D4-04 `null`（Java `null`） | `V: null`（クォート無し） | 同上（**既存**） | 同上（**既存**） | **する**（Java `null` へ戻る） |
| D4-05 `""` | `NAME: ""` | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation`（**既存**） | `YamlFormatWriterTest#roundTrip_table_isPreservedThroughRealReader`（**既存**） | **する**（`null` と区別される） |
| D4-06 `"007"` | `V: "007"` | `writesLeadingZeroNumberAsDoubleQuotedScalar` | `restoresLeadingZeroNumberThroughRealReader` | **する** |
| D4-07 改行含む | 短い値は 1 行の `V: "l1\nl2"`（ブロックスカラーではない）。80 桁を超えると行末 `\` で折り返す | `YamlFormatWriterTest#serialize_escapesQuotesBackslashAndControlChars`（**既存**）／`writesNewlineContainingStringAsEscapedSingleLineScalar`／折り返し `foldsLongEscapedValueWithBackslashContinuation` | `restoresNewlineContainingStringThroughRealReader`／折り返し `restoresFoldedLongEscapedValueThroughRealReader` | **する**（折り返しても） |
| D4-08 `"2026-08-07"` | `V: "2026-08-07"` | `writesDateLookingStringAsDoubleQuotedScalar` | `restoresDateLookingStringThroughRealReader` | **する**（日付にならない） |
| D4-09 コロン・ハイフン・`#` 含む | `V: "a: b - c #d"` | `writesColonHyphenAndHashContainingStringAsDoubleQuotedScalar` | `restoresColonHyphenAndHashContainingStringThroughRealReader` | **する**（`#` 以降も残る） |

**9 ケースすべてで記法がアサートされ、9 ケースすべてが復元される。** #18 が 🔺 としていた D4-01
（往復のみで記法アサートが無かった）は記法の担保が付いた。
**全値ダブルクォート＋`null` だけアンクォートという方針が、往復が成立している理由である** ——
引用符が落ちれば D4-01／D4-02／D4-06／D4-08 は本体スキーマ（`rows` の値を `["string","null"]` に限る）に
違反して読み戻せなくなり、D4-09 は `issues.md` **YML-11** のとおり値が黙って変わる。

**軸D の測定経路**: 上表は**すべて `setup_tables` の `rows`** で測っている。ただし
`"true"`（D4-02）と `"2026-08-07"`（D4-08）の 2 ケースだけは、**#25 のレビュー対応で
別の 2 経路 —— レコード断片（`records[].rows`）と `emitMap`（`directives` ／ `fw_header`）——
にもフィクスチャとして埋め込み、記法を出力全文で固定した**
（レコード断片は `YamlFormatWriterModelTest#record()`、
`emitMap` 経路は `#quotesBooleanAndDateLookingValuesInFwHeader`）。
**埋めた経路は 2 つである。**

**`list_maps` は独立した経路ではない。** `YamlFormatWriter#emitListMap` と `#emitTable` は
どちらも同じ引数で `emitMapRows` を呼ぶため、**値の記法は `setup_tables` と同一のコードで担保されている**。
両者の差は `emitMapRows` を呼ぶ前に組み立てる 1 行だけで、`emitTable` が `entry.prop("table", ...)`、
`emitListMap` が `entry.prop("id", ...)` を呼ぶ点である（その前の `emitGroupId` は同一）。
すなわち残る差はキー側の literal であって値の記法ではない。出典（2026-08-14 実行）:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
grep -c "emitMapRows(sb, entry, block.getColumnNames(), block.getRows());" \
  src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java
```

出力は **2**（`emitTable` と `emitListMap` の 2 箇所）。

**軸A（#25 が埋めた 2 種 ＋ #25 レビューで判明した 3 種）**

| 要素 | #18 の判定 | #25 後 | 担保テストメソッド（`YamlFormatWriterModelTest#`） |
|---|---|---|---|
| A-07 `EXPECTED_FIXED` | 🔺（`RoundTripTest#yaml_expectedFixed_isPreserved` 経由） | ✅ | `writesSetupVariableAndExpectedFixedUnderTheirSectionKeysInEncounterOrder`（`expected_files` キーへ写ることを出力全文の記法で）／`restoresExpectedFixedDataTypeThroughRealReader`（読み戻しても `EXPECTED_FIXED`・`FIXED` のまま） |
| A-08 `SETUP_VARIABLE` | 🔺（`RoundTripTest#yaml_setupVariable_isPreserved` 経由） | ✅ | 同じ記法テスト（`setup_files` キーへ写る）／`restoresSetupVariableDataTypeThroughRealReader` |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅（**誤り**。下記） | ✅ | `writesExpectedRequestBodyMessagesUnderItsOwnSectionKey` |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅（**誤り**。下記） | ✅ | `writesResponseHeaderMessagesUnderItsOwnSectionKey` |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅（**誤り**。下記） | ✅ | `writesResponseBodyMessagesUnderItsOwnSectionKey` |

**変異による確認（2026-08-14 実測。`src/main` は確認後に戻し `git diff f3efa1b -- src/main` → 0 行）**:
`YamlFormatWriter#sectionKey` の `case` を入れ替えて `EXPECTED_FIXED` を `setup_files` へ、
`SETUP_VARIABLE` を `expected_files` へ写すよう変異させると、A-07／A-08 の 3 メソッドが落ちる
（`RoundTripTest` の 2 件も落ちる）。すなわち #18 の 🔺 は 🔺 のままではなく直接の担保になった。

<a id="s4-1-2-sendsync"></a>

**A-12／A-13／A-14 の ✅ は #18 以来（#25 の当初版を含め）誤りだった。**
この 3 タイプを辺④で通していたのは `YamlFormatWriterTest#serializeSendSync_allFourSectionKeys` だけで、
同メソッドは送信同期 4 種を**まとめて 1 つの出力に**直列化し、4 つのキー文字列が「どこかに現れる」ことを
`assertTrue(yaml.contains("..._messages:\n"))` で見ているだけである。**`DataType` → セクションキーの写像を
1 つも固定していない**ため、4 種の写像を入れ替えても 4 キーはすべて現れて通ってしまう。
辺④で単独ブロックの出力全文を完全一致で見ていたのは
`YamlFormatWriterTest#serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField`
（A-11 `EXPECTED_REQUEST_HEADER_MESSAGES`）の 1 箇所だけであり、担保があったのは A-11 だけだった。
**これは #23 のレビューが辺③でまったく同じ形で見つけた欠陥である**（[§3.1-3](#s3-1-3-sendsync)。
辺③では `writesSequenceNoForAllSendSyncTypes` が 4 タイプ共通の値だけを見ていた）。

- **変異による実測（2026-08-14。#25 レビュー指摘の再現）**: `sectionKey` の
  `RESPONSE_HEADER_MESSAGES` ↔ `RESPONSE_BODY_MESSAGES` を入れ替えた変異と、
  `EXPECTED_REQUEST_BODY_MESSAGES` ↔ `RESPONSE_HEADER_MESSAGES` を入れ替えた変異の 2 通りを入れて全件実行した。
  **どちらも当初版では writer 系が全緑**（落ちるのは `RoundTripTest` の 2 件だけ）であり、
  往復テストは steering Rules フェーズ2 により正式な担保に数えないため、
  **この時点で A-12／A-13／A-14 は 🔺 相当（正式担保 0）**であった。
- **埋め方**: 上表の 3 メソッドを追加した。粒度は A-11 の担保テストに揃え、
  **その `DataType` 単独のセクションを直列化して出力全文を完全一致でアサートする**
  （`assertTrue(contains(...))` は使わない）。セクションキー以外は 4 種で完全に同一のフィクスチャなので、
  写像が入れ替われば必ず落ちる。
- **歯があることの実証**: 同じ 2 通りの変異を再度入れて全件実行し、
  入れ替えた側の新規メソッドが落ちることを確認した（それぞれ `Failures: 4`
  ＝ 新規 2 件 ＋ `RoundTripTest` 2 件）。
  **レビュー修正ラウンド 2 で `RESPONSE_HEADER_MESSAGES` ↔ `RESPONSE_BODY_MESSAGES` の側を
  取り直し、`Tests run: 536, Failures: 4` を確認した**（落ちるのは
  `#writesResponseHeaderMessagesUnderItsOwnSectionKey` ／ `#writesResponseBodyMessagesUnderItsOwnSectionKey` と
  `RoundTripTest#yaml_responseHeaderMessages_isPreserved` ／ `#yaml_responseBodyMessages_isPreserved`。
  ラウンド 1 時点の実測値は全体 535 件でのものだった）。
  変異は確認後に戻し、`git diff -- src/main pom.xml` → **0 行**を確かめた。

**キーのクォート判定（軸要素ではない。#25 レビューで判明した穴）**

`YamlFormatWriter#isPlainSafeKey` は、キーに「制御文字（`< 0x20`）」「半角空白」
「特殊文字集合 `"'#:,[]{}&*!|>%@` ` ?` の 18 文字のいずれか」を含むとき、または空文字・先頭が `-?:` のとき
クォートする。**#25 の当初版までに固定されていたのはコロン・空白・空文字・先頭 `-` の 4 つだけ**で
（§4.1 の 15・16・23 行目）、残る 17 文字と制御文字は未固定だった。

- **変異による実測（2026-08-14）**: 集合から `#` を 1 文字外すだけで **531 件すべてが通る**（生存変異）。
  実害は小さくない —— `#` が外れるとカラム名 `#x` の行が `- #x: "v"` となり、
  **行全体が YAML コメント化してデータ行が黙って消える**。
- **埋め方**: `YamlFormatWriterModelTest#quotesDirectiveKeyContainingAnyYamlSpecialOrControlCharacter` を
  追加した。特殊文字 18 文字と制御文字 2 種（`0x01` / `0x1f`）を 1 文字ずつ `directives` のキーに置き、
  20 通りそれぞれで出力全文を完全一致でアサートする。期待表記は実行して観測した結果である。
- **「1 ケース 1 `@Test`」の規約から辺④だけ逸脱している**（レビュー修正ラウンド 2 で明記した）。
  規約は `XlsFormatWriterInvalidOutputTest` の F3-04 節と `XlsFormatWriterCellTypeTest` の
  コメントにあり、理由は「ループで束ねると最初の 1 文字が落ちた時点で残りが実行されず、
  どの文字で挙動が違うのかが分からなくなるため」である。逸脱してよいと判断した根拠は 2 つ。
  (a) 20 ケースは `isPlainSafeKey` の**同一の判定 1 つ**（文字集合の `indexOf` と `c < 0x20` の
  制御文字ガード）を通る 1 つの振る舞いであり、Xls 側の禁止文字 7 件のように文字ごとに
  違うメッセージ・違う挙動を持たない。(b) ループ内では判定するだけで**アサートはループ後に 1 回**に
  集約したので、1 件目が落ちても残り 19 ケースは実行され、失敗メッセージに落ちた全件が出る ——
  すなわち規約の理由が成立しない。逸脱の理由は当該メソッドの Javadoc にも書いてある。
- **歯があることの実証（2026-08-14 に修正ラウンド 2 の形で取り直した）**: `#` を集合から外す変異、
  および `c < 0x20` のガードを外す変異を入れて全件実行し、それぞれ同メソッドが落ちることを確認した
  （どちらも `Tests run: 536, Failures: 1`）。集約したアサートが機能していることも同時に確認できており、
  失敗メッセージは順に `20 ケース中 1 件が期待どおりにクォートされなかった` ／
  `20 ケース中 2 件が…`（制御文字 `0x01` / `0x1f` の 2 件）である。あわせて JaCoCo で
  未到達だった「キーに制御文字を含む」枝が閉じ、分岐が 88/92 から **89/92** になった（下の「開示」）。

**軸C・軸E（#25 が埋めた 3 件）**

| 要素 | #18 の判定 | #25 後 | 担保テストメソッド（`YamlFormatWriterModelTest#`） |
|---|---|---|---|
| C-02 `sections` 空 | ❌ | ✅ | `writesNothingWhenContainerHasNoSections` — 例外にならず**ファイルも出力先ディレクトリも作られない**（辺③は同じ入力からシート 0 枚のブックを書く。`issues.md` **XLS-23**） |
| C-02 `sections` 複数 ／ E-4(複数) | ❌ | ✅ | `writesOneYamlFilePerSectionWhenContainerHasMultipleSections` — セクション 3 件（`zebra` / `alpha` / `mango`）から `<セクション名>.yaml` が 3 つ書かれ、各ファイルの中身がそのセクションの直列化結果と一致する |
| C-12 `FileDataBlock.records` 空 | ❌ | ✅ | `writesEmptyRecordsListForFileBlockWithoutRecords` — `records: []` が出る（**#25.5 の YML-12 修正後**。修正前は `records:` キーごと出ず、その出力は読み戻せなかった。`issues.md` **YML-12**）。改名前の名前は `writesFileBlockWithoutRecordsKeyWhenRecordsAreEmpty`。読み戻せることは `#readsBackFileBlockWithEmptyRecords` が担保する |

**軸F（§0.7 の辺④ 3 ケース）**

| 要素 | 判定 | 担保テストメソッド | 観測した挙動 |
|---|---|---|---|
| F4-01 出力先不在 | ✅ | `YamlFormatWriterInvalidOutputTest#createsMissingOutputDirectoriesAndWritesYaml` | 例外にならず、多階層の出力先が作られて YAML が書かれる（`Files.createDirectories`）。#18 が 🔺 としていた `YamlFormatWriterTest#write_ioError_throwsUncheckedIOException` は「親に通常ファイルが居座り**ディレクトリを作れない**」別の入力である |
| F4-02 `overwrite=false` 衝突 | **対象外**（上位層で担保済み） | — | 下の「F4-02 を対象外とした根拠」 |
| F4-03 書き込み権限なし | ✅ | `#wrapsAccessDeniedExceptionWhenOutputDirectoryIsNotWritable` | `UncheckedIOException`（メッセージは `failed to write YAML: <パス>`）。原因は `java.nio.file.AccessDeniedException`。ファイルは作られない。権限が効かない環境では `Assume` でスキップする |

**F4-02 を対象外とした根拠（2026-08-14 に実物で確認した）**

- `YamlFormatWriter` は `overwrite` を保持しない。`grep -rln "overwrite" src/main/java` が挙げるのは
  `TestDataConverter` ／ `ConverterMojo` ／ `ConversionRequest` の 3 クラスだけで、
  `grep -c "overwrite" src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java` は **0** である。
- 衝突を検査するのは `TestDataConverter#checkOverwrite`。`request.isOverwrite()` が偽のとき
  `target.outputPaths(container, outputBase)` の各パスに `Files.exists` を掛け、真なら
  `ConverterException("output already exists (overwrite=false): …")` を送出する。
- 上位層の既存テスト 2 件はいずれも **XLS→YAML** であり、`checkOverwrite` が多態で呼ぶ
  `FormatHandler#outputPaths` の実体は `YamlFormatHandler#outputPaths`
  （`container.getSections()` の各セクション名に `.yaml` を付けて並べる）である。
  すなわち**辺④（`.yaml` を出力側とする衝突）は担保されている**。
  - `TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse` — 出力先に `BookA/data.yaml` を
    置いた状態で `TestDataConverter.convert(DataFormat.XLS, DataFormat.YAML, in, out)` を呼び、
    `ConverterException` のメッセージが `output already exists` で始まることをアサートする。
  - `ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict` — 同じ状態で Mojo を
    `from=xls` / `to=yaml` / `overwrite` 既定（false）で実行し、`MojoExecutionException` をアサートする。
- 辺③（`.xlsx` を出力側とする衝突）は同じ根拠が成り立たず**未担保**である（§0.8-5 の訂正）。
  この非対称は #22 で判明したものであり、辺④側は当てはまらない。

**開示（テストを足していない担保の穴）**

- **`YamlFormatWriter` の JaCoCo 実測（2026-08-14。#25.5 の不具合修正後に取り直した）は
  行 160/161（99.4%）・分岐 91/94（96.8%）である。**
  導出コマンド（JaCoCo 手順は steering Decisions のとおり。`pom.xml` は変更していない）:

  ```sh
  JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o clean jacoco:instrument test jacoco:restore-instrumented-classes \
    && JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 mvn -o jacoco:report -Djacoco.dataFile=$(pwd)/jacoco.exec \
    && awk -F, 'NR > 1 && $3 == "YamlFormatWriter" { print "line " $9 "/" ($8 + $9) " branch " $7 "/" ($6 + $7) }' \
         target/site/jacoco/jacoco.csv
  ```

  出力は `line 160/161 branch 91/94`。**#25.5 追補（YML-12 3形目 の修正・2026-08-18）後は
  `line 163/164 branch 93/96` であり、未到達は行 1・分岐 3 のまま変わらない**（`emitRecords` に足した
  番人 `if` の 2 分岐は両側とも到達済み。[§0.1-2](#s0-1-2) の追補表）。**さらに 4形目 の修正後は
  `line 168/169 branch 95/98`、2形目 の修正後は `line 170/171 branch 95/98` で、いずれも未到達は
  行 1・分岐 3 のまま変わらない**（2形目 では `emitMessage` の番人で 2 分岐増え、到達不能になった
  `emitEmptyList` 引数を削って 2 分岐減った）。**#25 時点の `line 158/159 branch 89/92` から
  行の総数が 2・分岐の総数が 2 増えたのは YML-12 1形目 の修正（当時 `emitRecords` に `emitEmptyList` 引数を
  足した。**この引数は 2形目 の修正で削った** —— 上記）によるもので、増えた分岐はいずれも到達済みである**（未到達は行 1・分岐 3 のまま変わらない）。
  **ここまでの数値はいずれも #25.5 追補（2026-08-18）までの時点のものであり、その時点の未到達は
  行 1・分岐 3、箇所としては 3 箇所である。**（**その後、追補その 5 で `YamlFormatWriter#sectionKey` の `default` 側 1 分岐と
  その中の `throw` 1 行が到達不能になり——#25.5 §1-G〈XLS-20〉で `DataType.DEFAULT` のデータブロックを
  生成時に拒否した副作用。§0.1-2 の追補その 5 の開示——未到達は 行 2・分岐 4 になった。
  現時点の値は §0.1-2 の「追補その 6」の表〈2026-08-19 実測の `line 157/159 branch 86/90`＝未到達 行 2・分岐 4〉が正で、
  #26 の計測〈`da66425`〉と一致する。未到達 4 分岐の内訳は `coverage-report.md` §3.3 にある。**）
  #25.5 追補時点の未到達 **3 箇所**は、**いずれも軸A〜F の要素ではない**:
  `write` の「親ディレクトリを持たない相対パス」ガード（`getParent()` が `null` になる枝）／
  `emitBlock` の `instanceof` チェーンの `else`（sealed 階層の安全網。唯一の未到達行はここ）／
  `rawGroup` の「`[` で始まるが `]` で終わらない `groupId`」枝。
  **#25 の当初版で 4 箇所目に挙げていた `isPlainSafeKey` の「キーに制御文字（`< 0x20`）を含む」枝は
  レビュー対応で閉じた**（`quotesDirectiveKeyContainingAnyYamlSpecialOrControlCharacter`）。
  **この数値は `YamlFormatWriter` 1 クラスぶんであり、4 辺の担当クラス全体の計測と未到達分岐の列挙は #26 の仕事である。**
- **軸D の 9 ケースのうち 7 ケースは 1 経路（`setup_tables` の `rows`）でしか固定していない。**
  `"true"`（D4-02）と `"2026-08-07"`（D4-08）だけは #25 のレビュー対応でレコード断片
  （`records[].rows`）経路と `emitMap`（`directives` ／ `fw_header`）経路にも埋め込み、
  記法を出力全文で固定した。
  残る 7 ケースがレコード断片経路でも同じ記法で書かれ同じく往復することはプローブで確認したが、
  テストにはしていない（辺②の §2.1-2 が「12 ケースのうち 10 ケースは 1 経路でしか測っていない」と
  開示しているのと同じ性質の穴である）。
  **`list_maps` はこの穴に数えない。**`emitListMap` は `emitTable` と同じ `emitMapRows` を呼ぶため、
  値の記法は `setup_tables` と同一コードで担保されている（上の「軸D の測定経路」に出典）。
  残る差はキー側の literal（`table:` ／ `id:`）だけである。
- **この 2 ケースの埋め込みが示すのは「その 2 値がその経路でアサートされている」ことまでである。**
  #25 の当初版（素の値だけを置いた版）でも、**条件を付けない変異なら既存テストが捉える** ——
  `rowFlow` からクォートを外す変異では `YamlFormatWriterTest` だけで `Failures: 5, Errors: 2`、
  `emitMap` から外す変異では同クラスの 5 件が落ちる（2026-08-14 実測。全体は順に
  `Tests run: 536, Failures: 10, Errors: 9` ／ `Tests run: 536, Failures: 9, Errors: 0`）。
  当初版で生存したのは**この 2 値だけを狙い撃ちした変異**である（`rowFlow` ／ `emitMap` が
  `"true"` と `"2026-08-07"` のときだけ非クォートで書く変異。現在は順に
  `Tests run: 536, Failures: 5, Errors: 2` ／ `Failures: 1` で死ぬ）。
  すなわち**この埋め込みは「歯がある証明」ではなく、軸D の 2 ケースを別経路でも観測した記録である**。
  変異は 4 通りとも確認後に戻し、`git diff -- src/main pom.xml` → **0 行**を確かめた。
- **`YamlSeq#header(String k)` の `key(k)` は入力で区別できない等価変異であり、テストを足すべき穴ではない。**
  Verification レビューが実行した変異のうち唯一生存したのがこれである。
  **2026-08-14 に自分で再現した**（`header` の本文を `line(k + ":")` へ変異させて全件実行 →
  `Tests run: 536, Failures: 0, Errors: 0, Skipped: 0`）。
  ただし `header(...)` の実引数は**すべてコンパイル時定数かつ plain-safe**であるため、
  クォートの有無が出力に現れる入力が存在しない。出典（2026-08-14 実行）:

  ```sh
  cd /home/tie303177/work/nablarch/nablarch-testing-converter
  grep -rho "\.header([^)]*)" src/main/java/nablarch/test/tool/converter/yaml/ | sort | uniq -c
  grep -rho 'emitMap(sb, entry, "[a-z_]*"\|emitFlowList(sb, item, "[a-z_]*"' \
    src/main/java/nablarch/test/tool/converter/yaml/ | sort | uniq -c
  ```

  1 つ目の出力は `.header("records")` 1 ／ `.header("rows")` 1 ／ `.header(keyName)` 2 の **4 箇所**。
  2 つ目が `keyName` の実引数で、`"directives"` 2 ／ `"fw_header"` 1 ／ `"fields"` 1 ／ `"rows"` 1 の
  **5 箇所ともリテラル**である。**将来のレビュアはこの検証をやり直さなくてよい。**
- **`write` が複数セクションを書き出す順序はテストで固定していない。**
  ファイルシステム上に順序が現れないためである（`writesOneYamlFilePerSectionWhenContainerHasMultipleSections` が
  固定しているのは「セクション名 → 中身」の対応であって、書き出しの順ではない）。
- **書き出した YAML がスキーマに適合するかを見る担保は無い。** `YamlFormatWriter` はスキーマを参照せず、
  読み戻しを試みるテストだけが違反に気づける。#25 で見つけた 4 つの形は `issues.md` **YML-12** に
  記録して固定したが、**「その 4 つ以外にスキーマ違反を書き得る形が無い」ことは確かめていない**。

<a id="s4-3"></a>

### 4.3 辺④ 未担保一覧（#25 が埋めた対象）

計上単位と「状態」の 3 分類は §1.3 の規則に従う。

**本表は #25 の実測結果に合わせて「#25 後の状態」列を足した（2026-08-14）。** #18 時点は
「要追加 12 ／ 対象外 1」と数えていた。軸D の定義（9 ケース）は #18 から変わっていない。
**ただし軸A は #25 のレビューで 3 件（A-12／A-13／A-14）増えた。** この 3 件は
**#18 時点で ✅ と誤判定されており、本節の集計にも入っていなかった**ためである
（実際は 🔺 相当だった。根拠と変異による実測は [§4.1-2](#s4-1-2-sendsync)。
辺③でも #23 のレビューでまったく同じ 3 件が同じ理由で判明している。§3.3）。
したがって総計は 13 ではなく **16** が正しい。#18 時点の分類は「#18 の状態」列に残した。

| 軸 | 未担保要素 | #18 の状態 | #25 後の状態 | 件数 |
|---|---|---|---|---|
| A | A-07 `EXPECTED_FIXED`（🔺 `RoundTripTest#yaml_expectedFixed_isPreserved`）／A-08 `SETUP_VARIABLE`（🔺 `RoundTripTest#yaml_setupVariable_isPreserved`） | 要追加 | **担保済み（#25）** — 記法は `YamlFormatWriterModelTest#writesSetupVariableAndExpectedFixedUnderTheirSectionKeysInEncounterOrder`、往復は `#restoresExpectedFixedDataTypeThroughRealReader` ／ `#restoresSetupVariableDataTypeThroughRealReader`（変異による確認は §4.1-2 の軸A 表） | 2 |
| A | A-12 `EXPECTED_REQUEST_BODY_MESSAGES` ／ A-13 `RESPONSE_HEADER_MESSAGES` ／ A-14 `RESPONSE_BODY_MESSAGES` — **#18 時点で ✅ と誤判定されており本節の集計にも入っていなかった**（実際は `serializeSendSync_allFourSectionKeys` の `contains` アサートだけで、写像を 1 つも固定していなかった） | （集計外。誤って ✅） | **担保済み（#25 レビュー）** — `YamlFormatWriterModelTest#writesExpectedRequestBodyMessagesUnderItsOwnSectionKey` ／ `#writesResponseHeaderMessagesUnderItsOwnSectionKey` ／ `#writesResponseBodyMessagesUnderItsOwnSectionKey`（いずれも出力全文を完全一致。変異による実測は [§4.1-2](#s4-1-2-sendsync)） | 3 |
| B | （なし） | — | — | 0 |
| C | C-02 sections 空・複数（writer 側は到達可能。§0.8-6）／C-12 FileDataBlock.records 空 | 要追加 | **担保済み（#25）** — 順に `YamlFormatWriterModelTest#writesNothingWhenContainerHasNoSections`（空）／`#writesOneYamlFilePerSectionWhenContainerHasMultipleSections`（複数）／`#writesEmptyRecordsListForFileBlockWithoutRecords`（C-12。**#25.5 の YML-12 修正で改名**。旧名は `writesFileBlockWithoutRecordsKeyWhenRecordsAreEmpty`） | 2 |
| D | D4-01 `"100"`（記法アサートなしの 🔺）／D4-02 `"true"`／D4-06 `"007"`／D4-08 `"2026-08-07"`／D4-09 値側のコロン・ハイフン・`#` | 要追加 | **担保済み（#25）** — `YamlFormatWriterScalarTest` 16 件（`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatWriterScalarTest.java` → **16**）。要素別の担保メソッドは §4.1-2 の軸D 表。**残る 4 ケース（D4-03／D4-04／D4-05／D4-07 の記法）は既存の `YamlFormatWriterTest` が通しており、重複させていない**（同表にメソッド名を挙げてある） | 5 |
| E | E-4(複数) — `YamlFormatWriter#write` が sections をループするため到達可能（§0.8-6） | 要追加 | **担保済み（#25）** — C-02(複数) と同じ入力（`#writesOneYamlFilePerSectionWhenContainerHasMultipleSections`） | 1 |
| F | F4-01 出力先不在（🔺 のみ）／F4-03 書き込み権限なし | 要追加 | **担保済み（#25）** — `YamlFormatWriterInvalidOutputTest` の **2 件**（同クラスの総数も 2。`grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/yaml/YamlFormatWriterInvalidOutputTest.java` → **2**） | 2 |
| F | F4-02 `overwrite=false` 衝突 — `YamlFormatWriter` は `overwrite` を保持しない。`TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse`／`ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict` で担保済み（§0.8-5） | 対象外（上位層で担保済み） | 対象外（変更なし。根拠は #25 で実物を開いて確認した。§4.1-2 の「F4-02 を対象外とした根拠」） | 1 |
| **合計** | | **要追加 12 ／ 到達不能 0 ／ 対象外 1（＋ 誤判定により集計外 3）** | **要追加 0 ／ 担保済み 15 ／ 到達不能 0 ／ 対象外 1** | **16（うち対象外 1）** |

**合計の検算**（表の「件数」列を上から順に足す）:

- 担保済み: A 2 ＋ A 3（A-12〜A-14）＋ C 2 ＋ D 5 ＋ E 1 ＋ F 2 ＝ **15**
- 対象外: F4-02 ＝ **1**
- 要追加: **0**
- 総計: 15 ＋ 1 ＝ **16**（B は 0 件）

導出コマンド（表の「件数」列を機械的に足す。**そのまま実行すれば 16 になる**）:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter/.rn/ntf-test-data-converter/coverage
awk '/^\| 軸 \| 未担保要素 \| #18 の状態 \| #25 後の状態 \| 件数 \|/,/^\| \*\*合計\*\*/' inventory.md \
  | grep -vE '^\| (軸|---|\*\*合計)' \
  | awk -F'|' '{gsub(/[^0-9]/,"",$(NF-1)); s+=$(NF-1)} END {print s}'
```

**#18 時点の 13 との差 3 は、A-12〜A-14 を ✅ と誤判定して計上から落としていた分である**
（担保が増えたのではなく、数え漏れが解消した）。

**#18 時点の「特に大きな空欄」**（軸D の 5 ケース。特に `"true"`・`"007"`・日付風文字列は、辺②で
読み戻したときに型が変わりうる往復リスクの中心とされていた）は #25 で解消した。
**実測では 9 ケースとも往復し、型が変わるものは 1 つも無かった**（§4.1-2 の軸D 表）。
**辺④の「要追加」は 0 件**であり、到達不能と判定した要素も無い。

**ただし「未担保 0 件」は本書の計上単位（§1.3 冒頭）での話である。** §4.1-2 末尾の「開示」4 点
（JaCoCo 未到達 3 箇所／軸D 9 ケースのうち 7 ケースを 1 経路でしか固定していない・LIST_MAP 経路は未観測／
複数セクションの書き出し順を固定していない／書き出した YAML のスキーマ適合を見る担保が無い）と、
`issues.md` の **YML-12**／**YML-13** は空欄・穴として残る。

---

<a id="s5"></a>

<a id="s4-5"></a>

## 4.5 Step 4（#32〜#39）が新設したテストクラス（2026-08-28 追記）

**本節は #37 で新設し、#38 で 2 クラスを足した。**Step 4 は解説書の記述に対する担保を足すもので、
軸A〜F の要素表とは切り口が違う。**5 クラスとも軸要素対応表（`axis-matrix.md`）へは載せない**——理由は下記。
件数は導出コマンドの実測である。

| テストクラス | 追加タスク | 件数 | 導出コマンド | 何を担保するか |
|---|---|---:|---|---|
| `xls/XlsNotationSymmetryTest` | #32 | 8 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsNotationSymmetryTest.java` | Excel 形式の読み書きが**記法⇄値の対称な写像**であること（原因側）。実 `.xlsx` 起点 |
| `xls/XlsEmptyEntryTest` | #33 | 12 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsEmptyEntryTest.java` | 全要素が空文字のエントリが Excel 形式で失われないこと（**件数の保存**）。実 `.xlsx` 起点・本体 `PoiXlsReader` が読む件数と突き合わせ |
| `xls/SpecialNotationRoundTripTest` | #37 | 20 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/SpecialNotationRoundTripTest.java` | 特殊記法の母集合（`testdata_notation.rst` の表 12 行 ＋ `testdata_examples.rst` の記載例 6 対）を**実ファイル起点・4 経路**で往復させ、解釈後の値が保たれること |
| `xls/ExcelOutputDocumentedBehaviorTest` | #38 | 6 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/ExcelOutputDocumentedBehaviorTest.java` | `tools/testdata_converter.rst` が述べる Excel 出力の振る舞い（3-2・3-6〜3-10）。**設定した整形が効くこと**と、**色・書式・結合セルが往復で落ちること**（負のテスト） |
| `ConverterDocumentedBehaviorTest` | #38 | 5 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/ConverterDocumentedBehaviorTest.java` | 同上・変換ツールの入口（3-1・3-3・3-4・3-5・3-11）。検証が変換経路に無いこと／YAML コメントが落ちること／YAML 変換で `excludeSheets` と `ExcelFormatConfig` が効かないこと／`validate` が直下だけを見ること |

**軸要素対応表へ載せない理由。** 先の 3 クラスはいずれも**往復（4 経路）で値が保たれること**を測る。
steering Rules（フェーズ2）は「各辺の担保を往復テストの追加で代替しない。既存の往復テストが通している
軸要素は 🔺弱い担保として計上する」と定めており、**新設した往復テストを正式担保として数えると
この規定に反する**。3 クラスが触れる軸要素（主に **D 値の表現**、次いで **C 中間モデル全フィールド**の
`rows` ／ `columnNames`）は、いずれも各辺の直接テストで既に✅であり、**新しく埋まる穴は無い**。
したがって「🔺弱い担保が増えた」という位置づけで本節に開示するに留める。
**#38 の 2 クラスはそもそも軸の対象外である** —— 測っているのは中間モデルの写しではなく、
変換ツールの入口の振る舞い（整形設定・検証の呼ばれ方・絞り込みオプションの効き方）だからである。

**この 5 クラスが埋めているのは軸ではなく、解説書の記述に対する担保である。**
母集合と経路ごとの合否は `checks/step4-report.md` に記録する。

## 4.6 Step 4 第2回（#40〜#45）が新設したテストクラス（2026-08-29 追記）

**本節は #40 で新設した。**第2回は「解説書に書いてあることを実装とテストで押さえる」もので、
軸A〜F の要素表とは切り口が違う。**軸要素対応表（`axis-matrix.md`）へは載せない**——理由は §4.5 と同じで、
測っているのは中間モデルの写しの穴ではなく、フレームワーク本体と意味が一致するかである。
件数は導出コマンドの実測である。

| テストクラス | 追加タスク | 件数 | 導出コマンド | 何を担保するか |
|---|---|---:|---|---|
| `xls/XlsTrailingNullTest` | #40 | 5 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsTrailingNullTest.java` | ファイル・電文・送信同期電文の末尾に連続して `null` 記法を書いたときの値が、**フレームワーク本体が読む値と一致する**こと。実 `.xlsx` 起点 |
| `xls/XlsMarkerOnlyEntryTest` | #41 | 2 | `grep -c '^    @Test' src/test/java/nablarch/test/tool/converter/xls/XlsMarkerOnlyEntryTest.java` | マーカーカラムだけに値があるエントリが、**本体が読むのと同じ件数**残ること（テーブル系・`LIST_MAP`）。実 `.xlsx` 起点 |

**期待値の出どころを本体に移した（#40）。** 上記クラスは期待値を自分で書かず、
`core/reader/FrameworkOracle`（テスト専用）が本体パーサへ同じ `.xlsx` を読ませて取り出した値と突き合わせる。
変換ツールと期待値が同じ写し間違いを持つと検知できないためである
（変換ツール自身の 2 つのリーダを突き合わせていた `SpecialNotationRoundTripTest` で実際に起きた）。
本体の値そのものも各テストで明示し、本体側が黙って変わったときに気づけるようにしている。

**テスト専用の相乗りクラス 3 本（#40）。** いずれも `src/main` からは使わない。

| クラス | 置いたパッケージ | 越えた壁 |
|---|---|---|
| `core/reader/FrameworkOracle` | `nablarch.test.core.reader` | 本体パーサの `getResult()` ／ `MessageParser#getDelegate()`（パッケージプライベート） |
| `core/file/DataFileInspector` | `nablarch.test.core.file` | `DataFileFragment` の `names` ／ `values`（protected）。`DataFile#toDataRecords()` は固定長レコードとしての型変換を伴い空文字が `null` へ変わるため、値の突き合わせには使えない |
| `core/messaging/MessagePoolInspector` | `nablarch.test.core.messaging` | `MessagePool#getSource()`（protected） |

## 5. 全体サマリ

<a id="s5-1"></a>

### 5.1 未担保件数（辺 × 軸）

計上単位は §1.3 の規則に従う。🔺（弱い担保のみ）の要素も未担保として計上している。

**本節は #18 時点のスナップショットであり、4 辺を同じ基準で比べるためにその基準を保っている。**
辺①はその後 #19／#20／#21 で担保済みになった（§1.3 の「#21 後の状態」列）。
辺①の最新の状態別内訳は **要追加 0 ／ 担保済み 24 ／ 到達不能 8 ／ 対象外 0** であり、
下の表の辺①列（要追加 38 ／ 到達不能 3）は #18 時点の値である。
**下の 2 表の辺①の軸D も #18 時点の定義（17 ケース）による。** 軸D 辺① は 2026-08-13 のユーザー確定で
**8 ケース**へ絞り込まれた（NTF が実行できるテストデータ ＝ 全セル文字列書式のみを対象とする。§0.5）。
補正を当てると辺① 軸D は「17」ではなく **8**、辺①の合計は「41」ではなく **32**、
全体の合計は（辺③の補正 +3 と合わせて）「107」ではなく **101**、
状態別の辺①「要追加 38」は **29** が正しい。
辺③は #22 が軸D 8 件・軸F 3 件を、#23 が軸A 3 件・軸C 9 件・軸E 3 件を、#23 のレビュー対応が
軸A 3 件（A-12〜A-14。#18 時点で ✅ と誤判定されており本節の集計にも入っていなかった。§3.3）を
埋めたため最新は **要追加 0 ／ 担保済み 29 ／ 対象外 1**（§3.3）であり、
下の表の辺③列（要追加 26 ／ 対象外 1）は #18 時点の値である。
**下の 2 表の辺③の数字は #18 時点の誤判定を含む。** 辺③ 軸A は「3」ではなく **6**、辺③の合計は
「27」ではなく **30**、全体の合計は「107」ではなく **110**、状態別の「要追加 26」は **29** が正しい
（A-12〜A-14 を ✅ と誤判定していたため 3 件が計上から漏れていた。根拠は
[§3.1-3](#s3-1-3-sendsync)、影響範囲は §3.3）。
本節は #18 時点のスナップショットとして数字を保存する節であるため**表そのものは書き換えない**。
比較に使うときはこの補正を当てること。
辺②は #24 が軸C 5 件・軸D 12 件・軸E 1 件・軸F 4 件を埋めたため最新は
**要追加 0 ／ 担保済み 22 ／ 到達不能 6 ／ 対象外 0**（§2.3）であり、
下の表の辺②列（要追加 23 ／ 到達不能 3）は #18 時点の値である。
**下の 2 表の辺②の軸D も #18 時点の定義（10 ケース）による。** 軸D 辺② は 2026-08-14 のユーザー確定で
**12 ケース**になった（§0.5）。補正を当てると辺② 軸D は「10」ではなく **12**、
辺②の合計は「26」ではなく **28**、状態別の辺②「要追加 23」は **25** が正しい。
辺④は #25 が軸A 2 件・軸C 2 件・軸D 5 件・軸E 1 件・軸F 2 件を、#25 のレビュー対応が
軸A 3 件（A-12〜A-14。#18 時点で ✅ と誤判定されており本節の集計にも入っていなかった。§4.3）を
埋めたため最新は **要追加 0 ／ 担保済み 15 ／ 到達不能 0 ／ 対象外 1**（§4.3）であり、
下の表の辺④列（要追加 12 ／ 対象外 1）は #18 時点の値である。
**辺④の軸D は #18 時点も現在も 9 ケースで変わっていないが、下の 2 表の辺④の数字は #18 時点の誤判定を含む。**
辺④ 軸A は「2」ではなく **5**、辺④の合計は「13」ではなく **16**、状態別の辺④「要追加 12」は **15** が正しい
（A-12〜A-14 を ✅ と誤判定していたため 3 件が計上から漏れていた。根拠は
[§4.1-2](#s4-1-2-sendsync)、影響範囲は §4.3）。**辺③とまったく同じ形の誤判定である。**

| 軸 | 辺① | 辺② | 辺③ | 辺④ | 合計 |
|---|---|---|---|---|---|
| A データタイプ | 4 | 1 | 3 | 2 | 10 |
| B ブロック実装 | 0 | 0 | 0 | 0 | 0 |
| C 中間モデル全フィールド | 12 | 9 | 9 | 2 | 32 |
| D 値の表現 | 17 | 10 | 8 | 5 | 40 |
| E 多重度 | 3 | 2 | 3 | 1 | 9 |
| F 異常系 | 5 | 4 | 4 | 3 | 16 |
| **合計** | **41** | **26** | **27** | **13** | **107** |

**状態別の内訳**（#18 時点。§1.3 / §2.3 / §3.3 / §4.3 の #18 列の合計）:

| 状態 | 辺① | 辺② | 辺③ | 辺④ | 合計 |
|---|---|---|---|---|---|
| 要追加 | 38 | 23 | 26 | 12 | 99 |
| 到達不能 | 3 | 3 | 0 | 0 | 6 |
| 対象外（上位層で担保済み） | 0 | 0 | 1 | 1 | 2 |
| **合計** | **41** | **26** | **27** | **13** | **107** |

到達不能 6 件の内訳: 辺①・辺② それぞれの A-01 `DEFAULT`（§0.8-7）／C-02 sections 空（§0.8-6）／E-4 複数（§0.8-6）。
なお辺①は #20 の実測で C-11／C-13／C-16／C-17／C-20 の 5 件が到達不能へ移り、辺①の到達不能は 8 件になっている（§1.3）。
対象外 2 件の内訳: 辺③ F3-02 ／ 辺④ F4-02（いずれも `overwrite=false` 衝突。§0.8-5）。

**`RoundTripTest`（30 件）による 🔺 の追加は上の件数を変えない。** 🔺 は正式担保として数えないため、
§0.8-8 で 🔺 を付けた辺① A-04／A-07／A-09／C-06 省略／D1-14、辺③ A-07／A-09、辺④ A-07／A-08 は
いずれも #18 時点では「要追加」のまま残していた（重複テストを書かないよう、追加時は §0.8-8 の表を参照すること）。
**このうち辺④の A-07／A-08 は #25 で直接の担保が付き ✅ になった**（§4.1-2 の軸A 表。
`RoundTripTest` の 🔺 はそのまま残っており、二重の担保になっている）。

### 5.2 軸A の辺横断ビュー（`DataType` 14 種 × 4 辺）

**本節は §5.1 と違い現時点の状態を示す。** 辺①は #20 完了後の値（`XlsFormatReaderRealFileTest` による ✅ 化を反映。§1.2-2）。
**#21 は軸E・軸F だけを埋めるため、本節（軸A）の判定は #20 完了時点から変わっていない。**
**#22 も軸D・軸F だけを埋めるため、辺③列の判定は #18 時点から変わらなかったが、#23 が辺③の軸A 3 件
（A-01／A-07／A-09）を埋めたため辺③列を更新した**（2026-08-13。担保テストメソッドは §3.1-3）。
**さらに #23 のレビューで A-12／A-13／A-14 の辺③が 🔺 だったことが判明し、テスト 3 件を追加して ✅ にした**
（変異による実測は [§3.1-3](#s3-1-3-sendsync)）。
**同じ 3 種は辺④でも 🔺 だったことが #25 のレビューで判明し、同じくテスト 3 件を追加して ✅ にした**
（変異による実測は [§4.1-2](#s4-1-2-sendsync)）。
**辺②列は #24 で判定そのものは変わっていない（13/14 のまま）が、担保の経路が変わった。**
#18 時点は in-memory 経路（`YamlFormatReaderTest`）だけだったのに対し、#24 で
`YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` が実 `.yaml` 経路でも
13 種すべてを通した（§2.1-2）。**辺④列は #25 で A-07／A-08 が 🔺→✅ になった**（§4.1-2 の軸A 表）。

| DataType | 辺① | 辺② | 辺③ | 辺④ |
|---|---|---|---|---|
| A-01 `DEFAULT` | ❌（到達不能） | ❌（到達不能） | ✅（#23 で ❌→✅） | ✅ |
| A-02 `SETUP_TABLE_DATA` | ✅ | ✅ | ✅ | ✅ |
| A-03 `EXPECTED_TABLE_DATA` | ✅ | ✅ | ✅ | ✅ |
| A-04 `EXPECTED_COMPLETED` | ✅（#20 で 🔺→✅） | ✅ | ✅ | ✅ |
| A-05 `LIST_MAP` | ✅ | ✅ | ✅ | ✅ |
| A-06 `SETUP_FIXED` | ✅ | ✅ | ✅ | ✅ |
| A-07 `EXPECTED_FIXED` | ✅（#20 で 🔺→✅） | ✅ | ✅（#23 で 🔺→✅） | ✅（#25 で 🔺→✅） |
| A-08 `SETUP_VARIABLE` | ✅ | ✅ | ✅ | ✅（#25 で 🔺→✅） |
| A-09 `EXPECTED_VARIABLE` | ✅（#20 で 🔺→✅） | ✅ | ✅（#23 で 🔺→✅） | ✅ |
| A-10 `MESSAGE` | ✅ | ✅ | ✅ | ✅ |
| A-11 `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | ✅ | ✅ | ✅ |
| A-12 `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | ✅ | ✅（#23 レビューで 🔺→✅） | ✅（#25 レビューで 🔺→✅） |
| A-13 `RESPONSE_HEADER_MESSAGES` | ✅ | ✅ | ✅（#23 レビューで 🔺→✅） | ✅（#25 レビューで 🔺→✅） |
| A-14 `RESPONSE_BODY_MESSAGES` | ✅ | ✅ | ✅（#23 レビューで 🔺→✅） | ✅（#25 レビューで 🔺→✅） |
| **✅ 担保数** | 13/14 | 13/14 | **14/14** | **14/14** |
| **🔺 弱い担保** | 0 | 0 | **0** | **0** |
| **❌ 未担保** | 1 | 1 | **0** | 0 |

`EXPECTED_FIXED`（A-07）は #20 で辺①が、#23 で辺③が、#25 で辺④が ✅ になり、**🔺 は 4 辺とも無くなった**
（#25 以前の辺④は `RoundTripTest#yaml_expectedFixed_isPreserved` 経由の 🔺 だけだった。§0.8-8）。
`SETUP_VARIABLE`（A-08）の辺④も #25 で ✅ になった。`EXPECTED_VARIABLE`（A-09）の辺③は #23 で ✅ になった。
`DEFAULT`（A-01）は辺①・辺②で到達不能、辺③は #23 で ✅（`writesDefaultDataTypeMarker`）、
辺④は `serialize_unsupportedDataType_throws` で ✅ だが、辺③は書き出し・辺④は例外という**非対称**である
（`issues.md` **XLS-20**。修正はしていない）。
送信同期 3 種（A-12〜A-14）は**辺③・辺④の両方**で #18 以来 ✅ と書かれていたが実際はどちらも 🔺 で、
辺③は #23 のレビューで、辺④は #25 のレビューで、それぞれ 3 メソッドを追加して ✅ にした
（[§3.1-3](#s3-1-3-sendsync) ／ [§4.1-2](#s4-1-2-sendsync)）。
**2 辺で同じ形の誤判定が起きたのは、どちらも「4 種をまとめて 1 つの出力に書き、4 種共通の性質だけを
アサートする」テストを担保と数えていたためである**（辺③は連番 `"1"`、辺④は 4 つのキー文字列の
`contains`）。writer 側で `DataType` を区別する出力を単独ブロックで固定していたのは、
どちらの辺でも A-11 の 1 箇所だけだった。

**辺③と辺④は軸A 14 種すべてが ✅ である**（どちらも ✅ 14 ／ 🔺 0 ／ ❌ 0。辺④は #25 で
A-07／A-08 が ✅ になって揃った）。**この判定は辺③・辺④とも当初版では成り立っていなかった** ——
辺③は #23 の当初版で A-12〜A-14 が 🔺（✅ 11 ／ 🔺 3）、辺④は #25 の当初版で同じ 3 種が 🔺
（✅ 11 ／ 🔺 3）であり、いずれもレビュー対応でテストを追加して初めて成立した。
上表の「✅ 担保数 14/14 ／ 🔺 0」は、辺③が 2026-08-13 の #23 レビュー対応後、
辺④が 2026-08-14 の #25 レビュー対応後の値である。

<a id="s5-3"></a>

### 5.3 コーディネータに判断を仰ぎたい点

#### 解決済み（steering 更新 commit `66eb28f` で確定）

| # | 論点 | 確定内容 |
|---|---|---|
| 1 | 辺①・辺② の `DataType.DEFAULT` | **到達不能**。辺①は `TestCoreReaderAdapter`、辺②は `YamlFormatReader` の分岐に `DEFAULT` を返す経路がないこと（§0.8-7）。#20/#24 では理由付きで空欄に残す |
| 2 | E-4「コンテナ内セクション数 複数」／C-02「sections 空・複数」 | 辺①・辺②は **到達不能**（`read` が `Collections.singletonList(section)` を返す 1 リソース単位 API）。辺③・辺④は writer が `container.getSections()` をループするため **要追加**（§0.8-6） |
| 3 | 辺③／辺④ の `overwrite=false` 衝突（F3-02 / F4-02） | **対象外（衝突検査は上位層）**。`overwrite` を保持するのは `ConversionRequest` / `TestDataConverter` / `ConverterMojo` で writer は保持しない。既存テスト `TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse` と `ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict` が `TestDataConverter#checkOverwrite` を通す。ただし両者とも XLS→YAML であり、**辺④（`.yaml` 出力）は担保されるが辺③（`.xlsx` 出力）は未担保**である（§0.8-5 の訂正） |
| 4 | 既存の往復テスト（`RoundTripTest` 30 件、`XlsFormatWriterTest#roundTrips*` 8 件、`YamlFormatWriterTest#roundTrip_*` 6 件）の扱い | **🔺弱い担保として計上するが正式担保としては数えず、直接テストの追加対象からも外さない**（steering Rules フェーズ2）。§0.8-8 に `RoundTripTest` 30 件の対応表を置き、未担保一覧に 🔺 の注記を併記した |

#### 未解決（コーディネータの確認が要る）

1. **軸C の「省略」定義**: steering #20 の Steps は `identifier` と `fileType` も「値あり」「省略」双方を通すとしているが、
   実定義には省略表現がない（§0.4）。実定義を正として `fileType` は FIXED/VARIABLE の 2 値、
   `identifier` は必須スカラーとして扱ってよいか（本棚卸しは実定義を正として扱っている）。
2. **steering の完了条件と「到達不能」の整合**: steering #24 の Completion criteria は
   「辺②について軸A の 14 種…が埋まっている」、#23 は「辺③について軸A の 14 種…がすべてアサートされている」と読める。
   本棚卸しでは辺② A-01 `DEFAULT` を到達不能と判定した（§0.8-7。辺①については steering #20 が既に到達不能と明記済み）。
   #24 の完了条件を「13 種＋ `DEFAULT` は到達不能として理由付きで空欄」に読み替えてよいか。
   同じく steering #21 は辺① E-4 のみを到達不能としているが、本棚卸しでは C-02「sections 空」も同じ根拠で到達不能と判定した。
   **#24 の分は解決済み（2026-08-14）**: steering #24 の Completion criteria が
   「軸A の 13 種（`DEFAULT` を除く。… `DEFAULT` を生成しないため到達不能。根拠付きで空欄に残す）」と
   明記され、`sections` 空・E-4(複数) も「到達不能として根拠付きで空欄」と明記された。
   本書 §2.1-2 / §2.3 はこの読み替えどおりに記している。辺①（steering #21）側は未解決のまま。
3. **辺③ A-01 `DEFAULT` の扱い**: ~~writer 側は `XlsFormatWriter#marker` がマーカー文字列を
   `getDataType().getName()` から組み立てるだけなので `DEFAULT` ブロックも書けてしまう（＝到達可能）。
   辺④の `serialize_unsupportedDataType_throws` は `IllegalArgumentException` を投げる挙動を固定しているが、
   辺③に同等のガードはない。#23 で現状挙動を記録して固定する対象としてよいか
   （挙動が仕様として不適切なら `issues.md` 行き）。~~
   **解決済み（#23・2026-08-13）**: steering #23 の Steps が「現状の挙動をまず実行して記録してから固定する。
   辺③④の非対称を `issues.md` に課題として記録する（修正しない）」と指示しているため、そのとおり実施した。
   固定した挙動は `XlsFormatWriterModelTest#writesDefaultDataTypeMarker`（識別セル `DEFAULT=T` が書かれる）、
   課題は `issues.md` **XLS-20**。あわせて「辺③が書いたブロックは辺①で読み戻すと黙って消える」ことも
   実測して `#dropsDefaultDataTypeBlockWhenReadBack` で固定した。
   **その後 #25.5 §1-G で決着した（2026-08-21 に実物で確認して追記）**: `DEFAULT` は `TestDataBlock` の生成時に
   拒否されるようになり、上の 2 件は**いずれも HEAD に無い**。現在の担保は
   `TestDataBlockTest#データタイプDEFAULTのブロックは生成できない`（§0.8-7 ／ §0.1-2 追補その 5 の対応表）。
