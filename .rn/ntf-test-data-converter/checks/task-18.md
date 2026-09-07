# task-18 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| `coverage/inventory.md` に4辺ぶんの棚卸し表があり、既存126件（33+20+40+33）の全テストメソッド名が漏れなく載っている | OK | `grep -c '@Test'` の実測 33/20/40/33 ＝ 126 が steering 想定と一致。突合スクリプトで missing count = 0（詳細は下記 Evidence 1） | OK | RoundTripTest 30 件の取りこぼしを指摘（Major M-1）→ 修正済み |
| 軸A の要素が `DataType` の実定義と一致している（14種でない場合はその旨と実際の要素が記録されている） | OK | 実定義 `DataType.java` L8-56 の全 enum 定数を列挙 → 14 種（`DEFAULT` ＋13）。steering と差異なし（詳細は下記 Evidence 2） | OK | 指摘なし |
| 軸C の対象フィールドが実クラスの定義と一致し、省略可能なフィールドが識別されている | OK | model 8 クラスの `private final` を全列挙 → 21 フィールド。Javadoc 根拠で 3 分類（詳細は下記 Evidence 3） | OK | Javadoc 行番号 2 件のズレを指摘 → 修正済み |
| 各辺について未担保の軸要素が一覧化されており、以降のタスクが埋めるべき対象が確定している | OK | §1.3/§2.3/§3.3/§4.3 に未担保一覧（41/26/27/13 ＝ 107 件）。各要素に「状態」（要追加／到達不能／対象外）と担当タスクを明示（詳細は下記 Evidence 4） | OK | 「要追加」と「到達不能」の未区別を指摘（Craft M1）→ 修正済み |
| src/main・src/test への変更がゼロ（棚卸しのみ） | OK | `git status --porcelain -- src` → 出力なし（詳細は下記 Evidence 5） | OK | 指摘なし |

### Evidence 1 — 全テストメソッド名の網羅

実測コマンドは `inventory.md` §0.1 に貼付。`XlsFormatReaderTest` 33 ／ `YamlFormatReaderTest` 20 ／
`XlsFormatWriterTest` 40 ／ `YamlFormatWriterTest` 33 ＝ 126（steering 想定値と完全一致）。
4 クラスの `public void <name>` 全件が `inventory.md` 中に `` `<name>` `` として出現することを突合スクリプトで確認
（missing count = 0）。逆向き検査（`inventory.md` 中の関数名が実在するか）も実施し、非実在は 0 件。
レビュー指摘を受けた再修正後も同スクリプトを再実行し、`RoundTripTest` 30 件を含めて非実在 0 件を再確認した。

### Evidence 2 — 軸A と `DataType` 実定義の突き合わせ

実定義 `/home/tie303177/work/nablarch/nablarch-testing/src/main/java/nablarch/test/core/reader/DataType.java`
L8-56 を読み、全 enum 定数を列挙 → `DEFAULT`(L11), `SETUP_TABLE_DATA`(L14), `EXPECTED_TABLE_DATA`(L17),
`EXPECTED_COMPLETED`(L23), `LIST_MAP`(L29), `SETUP_FIXED`(L32), `EXPECTED_FIXED`(L35), `SETUP_VARIABLE`(L38),
`EXPECTED_VARIABLE`(L41), `MESSAGE`(L44), `EXPECTED_REQUEST_HEADER_MESSAGES`(L47),
`EXPECTED_REQUEST_BODY_MESSAGES`(L50), `RESPONSE_HEADER_MESSAGES`(L53), `RESPONSE_BODY_MESSAGES`(L56)
の **14 種（`DEFAULT` ＋13）**。steering 記述と一致・差異なし。`inventory.md` §0.2 に定義行番号・
`getType()`・`getName()` つきで記録。

### Evidence 3 — 軸C のフィールドと省略区分

`src/main/java/nablarch/test/tool/converter/model/` の 8 クラスを全文 Read し `private final` 宣言を列挙 →
全 **21 フィールド**（TestDataContainer 2 ／ TestDataSection 2 ／ TestDataBlock 3 ／ ColumnRowDataBlock 2 ／
FileDataBlock 3 ／ MessageDataBlock 3 ／ RecordLayout 3 ／ FieldDef 3）。省略区分は Javadoc 記述を根拠に 3 分類 —
**省略可能 4 件**（`TestDataBlock.groupId` L27/L41「省略時は空文字」、`RecordLayout.recordType` L26/L36
「省略時は null」、`FieldDef.type` L24/L38、`FieldDef.length` L25/L43）／空許容コレクション 11 件／
必須スカラー 6 件。`inventory.md` §0.4 に表として記録。steering #20 が挙げる `identifier`・`fileType` には
実定義上「省略」表現がない点を差異として明記し、判断依頼として §5.3 に記載。

### Evidence 4 — 未担保一覧と「状態」分類

辺ごとに未担保一覧を設置 — §1.3 辺①（41、うち到達不能 3）／§2.3 辺②（26、うち到達不能 3）／
§3.3 辺③（27、うち対象外 1）／§4.3 辺④（13、うち対象外 1）、合計 107 件。
§5.1 に辺×軸のクロス表と状態別内訳（要追加 99 ／ 到達不能 6 ／ 対象外 2）を掲載。
軸D は steering #19/#22/#24/#25 の記述を D1-01〜17 ／ D2-01〜10 ／ D3-01〜08 ／ D4-01〜09 として ID 化、
軸E は E-1〜E-4 × 0／1／複数、軸F は F1-01〜06 ／ F2-01〜05 ／ F3-01〜04 ／ F4-01〜03 として ID 化。
集計の整合は機械照合で確認した — (a) 各辺の未担保表の件数列の合計＝状態内訳の合計＝表記の合計、
(b) §5.1 の 2 つの表それぞれで行合計・列合計が一致、(c) §5.2 の ✅/🔺/❌ 数が各辺 §x.2 の軸A 表の実測と一致、
(d) 各辺 §x.2 の軸C 表の両状態担保／未担保の実測が見出しの数値および §5.1 の軸C 行と一致。

### Evidence 5 — src 無変更

`git status --porcelain -- src` → 出力なし。`git diff --stat -- src` ／ `git diff --cached --stat -- src` →
出力なし。レビュー指摘修正後も再確認済み（`RoundTripTest.java` は読み取りのみ）。
`.rn/ntf-test-data-converter/steering.md` も本タスクからは未編集（`git status --porcelain -- .rn/.../steering.md` → 出力なし）。

## 棚卸しで判明した主な事実（後続タスクへの申し送り）

`inventory.md` §0.8「棚卸しで判明した横断的な事実」（7 項、事実／判断を分けて記載）と
§0.8-8「`RoundTripTest`（30 件）の扱い」を参照。本ファイルでは二重管理しない。

担当タスクへの割り当てのみ再掲する:

| §0.8 の項 | 影響するタスク |
|---|---|
| 1・2（辺①既存 33 件は実 `.xlsx` を通らない／既存往復テストは通る） | #19 |
| 3（辺②既存 20 件は YAML パーサを通らない） | #24 |
| 4（`getCellType()` を使うテストが 0 件） | #22 |
| 5（`overwrite` を writer は保持しない → F3-02/F4-02 は上位層で担保済み） | #22, #25 |
| 6（`read` が 1 リソース単位 API → C-02/E-4 の到達可否） | #21, #23, #24, #25 |
| 7（`DataType.DEFAULT` はリーダ 2 経路で生成されない） | #20, #23, #24 |
| 0.8-8（`RoundTripTest` 30 件が 🔺 で通す軸要素） | #19〜#25 全て（重複回避） |

## QA Expert Review

Verdict: **PASS with findings**（Major 1 件 ／ Minor 3 件。全件 Valid・修正済み）

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| 棚卸しの網羅性（4 辺 126 件の全メソッドが載っているか） | OK | 126 件全件の突合 missing = 0 |
| 判定基準の一貫性（同じ根拠に同じ印が付いているか） | NG → 修正済み | `XlsFormatWriterTest#roundTrips*` を 🔺 計上しながら `RoundTripTest` 30 件に同じ基準を適用していなかった（Major M-1） |
| 後続タスクが空欄を埋められる粒度になっているか | OK | 各未担保要素に ID・状態・担当タスクが付いている |
| 事実の裏取り（コマンド・行番号） | NG → 修正済み | Javadoc 行番号 2 件のズレ、§5.3-3 の未確認事項（Minor m-2, m-3） |

QA finding（Major 1 件 ／ Minor 3 件）:

| # | 指摘 | 判定 | 対応 |
|---|---|---|---|
| M-1 | `RoundTripTest`（30 件）が棚卸しから完全に抜けている。`XlsFormatWriterTest#roundTrips*` を 🔺 計上しているのに同じ基準を適用しておらず内部矛盾。#19〜#25 が重複テストを書くリスク | **Valid** | 修正済み。`inventory.md` §0.8-8 に 30 件全件の対応表を新設し、辺① A-04／A-07／A-09／C-06 省略／D1-14、辺③ A-07／A-09、辺④ A-07／A-08 を新たに 🔺 として計上。各未担保一覧に「🔺 担保あり（重複を避けること）」の注記を追加。steering Rules に従い正式担保としては数えないため未担保 107 件は不変 |
| m-2 | §5.3-3 の `overwrite=false` が判断依頼のまま。事実で埋めるべき | **Valid** | 修正済み。§0.8-5 に上位層の既存テスト（`TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse` L336 ／ `ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict` L267）を実確認して記載し、F3-02／F4-02 を「対象外（上位層で担保済み）」に確定。§5.3 では「解決済み」表へ移動 |
| m-3 | 行番号ズレ 2 件（`TestDataBlock` L28→L27、`FieldDef` L25→L24） | **Valid** | 修正済み（Verification 指摘と同一。下記参照） |
| m-4 | 辺③ C-11「空」を `writesVariableFileWithoutLengthRow` ／ `writesMultipleRecordLayouts` で ✅ としているが行位置しかアサートしておらず間接担保。C-10 の「暗黙」と表記が揃っていない | **Valid** | 修正済み。🔺 に落とさず **注記を付けて揃える**方を採用した — 辺③は writer であり中間モデルの getter を読み戻す観測点がなく、「ディレクティブ行が出ない版面」が空 Map の直接の観測結果であるため。C-10・C-11 の両セルに「版面で暗黙」と明記し、C-11 には `getDirectives()` を読み戻すテストがない旨も併記した。判定（✅）は据え置きのため件数変化なし |

## Expert Reviews

### Craft Expert (writing)

Verdict: **PASS with findings**（Major 2 件 ／ Minor 13 件 ／ Nit 5 件。全件 Valid・修正済み）

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| 読者が迷わず目的の情報に到達できるか | NG → 修正済み | 793 行・6 章・約 20 表に目次がない（M3）。凡例直後に章リンク目次を追加 |
| 記号・用語の定義と実表記の一致 | NG → 修正済み | 🔺 が二役（弱い担保／補足注）だった（M2）。補足注を ※ に分離し、`—`・`n/a`・**太字** を凡例に追加 |
| 分類の粒度（読者が次に何をすべきか分かるか） | NG → 修正済み | 未担保一覧が「要追加」と「到達不能」を区別していなかった（M1）。「状態」列を追加し 3 分類 |
| 表記の一貫性 | NG → 修正済み | 数詞・空白・内部参照記法・E-4 呼称・計上単位の揺れ（M7, M11, M13, N18, N19） |

Craft finding（Major: M1-M2 ／ Minor: M3-M15 ／ Nit: N16-N20）:

| # | 指摘 | 判定 | 対応 |
|---|---|---|---|
| M1 | 未担保一覧が「要追加」と「判断待ち／到達不能」を区別していない | **Valid** | 修正済み。4 辺すべての未担保一覧に「状態」列（要追加／到達不能／対象外〈上位層で担保済み〉）を追加し、合計を「41（うち到達不能 3）」形式に。到達不能 6 件・対象外 2 件をすべて根拠付きで確定（判断待ちは残していない） |
| M2 | 凡例が実際の表記と食い違っている（🔺 の二役、備考欄の記述、`—`/`n/a`/**太字** が未定義） | **Valid** | 修正済み。補足注 42 箇所を ※ に置換、凡例に `—`・`n/a`・**太字**・※ を追加、「詳細は各表の備考欄」を「理由は同じセルに併記する」に変更 |
| M3 | 目次がない | **Valid** | 修正済み。凡例・軸一覧の直後に章リンク目次を追加（HTML アンカー `<a id="s0">`〜`<a id="s5-3">` を各節に埋め込み、日本語見出しでもリンクが壊れないようにした） |
| M4 | 軸の正式名が §5.1 まで出てこない | **Valid** | 修正済み。凡例の隣に「軸の一覧」表を新設し、あわせて §0.5〜§0.7 の見出しを「0.5 軸D 値の表現 — 要素」等に変更 |
| M5 | 逆向き索引に「多数」13 箇所・「ほか」29 箇所・「全テスト」1 箇所があり具体名まで追えない | **Valid** | 修正済み。§x.1 の表から機械的に各要素の担保テスト集合を抽出し、全 43 箇所を「代表 2 件は実名＋ほか N 件（計 M 件）」形式に置換。#27 の完了条件「記された全メソッド名がテストソースに実在する」へ持ち上がる形になった |
| M6 | §0.8「判明した横断的な事実」に判断・提案が混在 | **Valid** | 修正済み。全 7 項を「事実:」「判断:」に分割。判断のうち確認が要るものは §5.3 に再掲 |
| M7 | E-4 の呼称が 4 通り | **Valid** | 修正済み。ID 名を「E-4 コンテナ内セクション数」に固定し、辺ごとの実体（ブック内シート数／ディレクトリ内 YAML ファイル数／出力 YAML ファイル数）を括弧書きに。§0.6 に統一方針を明記 |
| M8 | 「（追加で担保済みの異常系）」と「（steering 外で担保済みの異常系）」の混在 | **Valid** | 修正済み。「（steering 外で担保済みの異常系）」に統一 |
| M9 | §0.1 の実測コマンドが省略記法で再実行できない | **Valid** | 修正済み。実行可能な `for` ループをそのまま貼付し、出力も貼り直した（`RoundTripTest` を含む 5 クラス分） |
| M10 | 「器」「版面」が初出無定義 | **Valid** | 修正済み。凡例の直後に「用語」として 2 語を定義 |
| M11 | 内部参照記法の混在（「0.4 参照」/「0.8-5 参照」 vs 「§0.4」） | **Valid** | 修正済み。`§0.4` 形式に統一し、凡例に「内部参照は §0.4 のように節番号で書く」と明記 |
| M12 | 辺④だけ軸C 見出しに要約があり、n/a 6 件を「両状態担保」に含めた数え方が不明 | **Valid** | 修正済み。4 辺すべての軸C 見出しを「21 フィールド ─ 両状態担保 N ／ 未担保 M」形式に統一し、n/a 6 件（C-01, C-03, C-05, C-07, C-10, C-19）を明示して「値ありの担保をもって両状態担保として数える」と定義。軸A・B・D・F の見出しも「✅ n ／ 🔺 n ／ ❌ n ＝ 合計」形式に統一 |
| M13 | 計上単位の揺れ（「要素×状態」vs「要素」） | **Valid** | 修正済み。§1.3 に計上単位の規則を明記（軸A・B・C は要素 1 件、軸D・F はケース 1 件、軸E は（観点, 多重度）の組 1 件）し、§2.3/§3.3/§4.3 から参照。辺② の「9（うち C-13 は 2 状態とも欠）」は要素欄へ移し、件数欄は数値のみに |
| M14 | Evidence セルが 324〜668 文字と長すぎる | **Valid** | 修正済み。表のセルは判定＋一行根拠に短縮し、詳細を「Evidence 1〜5」として表の下へ展開（`task-17.md` の「QA finding」節と同じ形） |
| M15 | 「棚卸しで判明した主な事実」が `inventory.md` §0.8 とほぼ二重管理 | **Valid** | 修正済み。本ファイルは §0.8／§0.8-8 への参照に留め、タスク割り当ての対応表のみ残した |
| N16 | 空テーブル 3 つ | **Valid** | 修正済み。3 レビューとも実施済みのため実際の結果を記録した |
| N17 | Overall Verdict に「Design expert: N/A」行がない | **Valid** | 修正済み |
| N18 | 数詞・空白の揺れ（「4辺分」/「4つの変換辺」/「4 辺中」、半角 "vs"） | **Valid** | 修正済み。「4 辺分」「4 つの変換辺」「4 つの辺のうち」に統一、§0.1 見出しの "vs" を「実測と steering 想定の突き合わせ」に変更 |
| N19 | `—` の後の空白の揺れ | **Valid** | 修正済み。`—（…）` に統一 |
| N20 | 冒頭が #26/#27 に触れていない | **Valid** | 修正済み。「#26 の入力であり #27 で `axis-matrix.md` へ発展させる土台」である旨を追記 |

### Verification Expert (fact-check)

Verdict: **不一致あり**（3 件。全件 Valid・修正済み。裏取り 31 項目は一致）

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| 裏取り項目の一致 | OK | 31 項目を実ファイル・実コマンドと突き合わせ、31 項目とも一致 |
| 行番号の正確性 | NG → 修正済み | Javadoc 参照 2 件が 1 行ズレ |
| 用語定義と実運用の一致 | NG → 修正済み | 「担保」の定義が §0.8 冒頭の宣言より緩い箇所がある |

Verification finding（不一致 3 件 — 行番号ズレ 2 件／「担保」定義の緩み 1 件）:

| # | 指摘 | 判定 | 対応 |
|---|---|---|---|
| V1 | 行番号 1 行ズレ — `inventory.md` の `TestDataBlock L28/L41`（C-06）は正しくは **L27**/L41（L28 は `identifier` の行）。旧 `task-18.md` L9 にも複製されていた | **Valid** | 修正済み。`inventory.md` §0.4 の C-06 行・内訳文と本ファイルの Evidence 3 の両方を L27 に訂正。実ファイル `TestDataBlock.java` L27（`@param groupId … 省略時は空文字`）を再読して確認 |
| V2 | 行番号 1 行ズレ — `inventory.md` の `FieldDef L25/L38`（C-20 `type`）は正しくは **L24**/L38（L25 は `length` の行）。C-21 が引く L25 は正しいのでそのまま。旧 `task-18.md` L9 にも複製されていた | **Valid** | 修正済み。`inventory.md` §0.4 の C-20 行・内訳文と本ファイルの Evidence 3 の両方を L24 に訂正。実ファイル `FieldDef.java` L24（`@param type … 省略時は null`）を再読して確認。C-21 の L25/L43 は変更なし |
| V3 | 「担保」定義の緩み — `inventory.md` は「実際にアサートしている対象のみを担保とした」と宣言するが、軸A の ✅ にはマーカー文字列を入力に与えブロック型をアサートしているだけで `getDataType()` を検証していないものが含まれる（`XlsFormatReaderTest` で `getDataType()` を参照するのは L281/L542/L578/L641 の 4 箇所のみ、軸A は 10/14 ✅）。ただし判定自体は一貫している（同じテストに軸C の C-05 を付けていない） | **Valid**（誤読のおそれ。判定変更は不要） | 修正済み。`inventory.md` 冒頭に「判定基準」節を新設し、軸A は「その `DataType` のブロックが生成されることをアサートしている」を担保とし `getDataType()` の直接アサートとは区別すること、軸C の C-05 は `getDataType()` を直接アサートするテストにのみ付けていることを明記。✅/🔺/❌ の判定自体は変更していない |

裏取りが一致した主な項目（抜粋）: `DataType` 14 種と定義行番号、model 8 クラス 21 フィールド、
4 クラスのテスト件数 33/20/40/33、`grep -rn "getCellType" src/test/` → 0 件、
`overwrite` を保持する 3 クラス、`TestCoreReaderAdapter` L362 の DEFAULT スキップ、
`XlsFormatReader#read` L96-101 の 1 シート単位 API、`RecordLayout` L26/L36。

### Design Expert

N/A — 本タスクは既存テストの棚卸し（文書作成）であり、構造・アプローチを新設しない。

## Overall Verdict

- Self-check: OK
- QA: OK（Major 1 件・Minor 3 件 → 全件 Valid・修正済み）
- Design expert: N/A
- Craft expert: OK（Major 2 件・Minor 13 件・Nit 5 件 → 全件 Valid・修正済み）
- Verification expert: OK（不一致 3 件〈行番号ズレ 2 ／ 定義の緩み 1〉→ 全件 Valid・修正済み。裏取り 31 項目は一致）
- Ready to check off: Yes
