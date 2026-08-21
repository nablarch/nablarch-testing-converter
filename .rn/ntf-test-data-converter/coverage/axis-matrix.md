# 軸×要素対応表（軸要素 → 担保テストメソッド。task #27）

4 つの変換辺それぞれについて、軸A〜F の**全要素を行として並べ**、その要素を担保している
テストメソッド名を記した逆引きの表である。

- 作成日: 2026-08-21
- 対象コミット: `HEAD`（ブランチ `ntf-test-data-converter`）。**本書の判定・メソッド名はすべて HEAD の
  `src/test` ／ `src/main` を開いて確かめたものである**（`coverage/inventory.md` の記述を根拠にしていない。
  確かめていない箇所は §6 に「未確認」として列挙する）
- 位置づけ: `steering.md` Rules「**4 辺を通した逆引き（軸要素 → 担保テストメソッド）の正は #27 の
  `coverage/axis-matrix.md` とする**」に従う。`coverage/inventory.md` は順引き（テストメソッド → 軸要素）の
  台帳であり、本書はその逆方向の正である。**同じ関係を 2 方向に手書きしないため、本書は
  `inventory.md` の順引き表を転記しない。**
- `inventory.md` との関係: **`inventory.md` の §1〜§4 の表は各タスク時点のスナップショットであり、
  現在の担保を示すものではない。** 本書は HEAD 時点の現在地だけを示す。両者が食い違う箇所は §6 に挙げた。

## 0. 読み方

### 0.1 凡例（状態）

| 印 | 意味 |
|---|---|
| ✅ | 担保あり（その軸要素をその辺で直接アサートしているテストがある） |
| 🔺 | 弱い担保のみ（往復テスト経由でしか通っていない）。**正式担保には数えない** |
| ❌ | 未担保（到達可能なのに担保テストが無い） |
| — | 空欄（その辺ではその状態に到達できない／その辺の担当クラスの関心事ではない）。理由欄に根拠を書く |
| n/a | その要素に「省略」「空」という状態が存在しない |

**「担保テストメソッド」欄と「🔺 往復」欄の `—` は「該当なし」**（`inventory.md` の凡例と同じ）であり、
状態欄の `—`（空欄）とは意味が違う。状態が `—` の行は担保テストメソッドを持たないため
両欄とも `—` にし、理由欄に**到達不能であることを実行可能な形で示す根拠テスト**を挙げる。

**「担保テストメソッド」欄は `クラス名#メソッド名` の形で書く。**
同じクラスの 2 件目以降だけは `#メソッド名` と略す（直前に挙げたクラスを指す）。

### 0.2 判定基準

- **軸A**: その `DataType` の**ブロックが生成される／その `DataType` に依存する出力が書き出される**ことを
  アサートしているものを担保とする。**入力にその型のブロックを与えているだけでは担保にならない**
  （4 種共通の値しか見ていないテストを担保と数えた誤判定が辺③・辺④で実際に起きている。
  `inventory.md` §3.1-3 ／ §4.1-2）。
- **軸B**: 実装クラス（`TableDataBlock` ／ `ListMapBlock` ／ `FileDataBlock` ／ `MessageDataBlock`）が
  生成される／その形で書き出されることをアサートしているもの。
- **軸C**: 省略可能フィールドは「値あり」「省略」、空許容コレクションは「非空」「空」の双方を**別の行**として評価する。
- **軸D・軸F**: ケース 1 件を 1 行とする。
- **軸E**: （観点, 多重度）の組 1 件を 1 行とする。
- **読み手側の辺**（辺①・辺②）は**実ファイルを入力とする経路**の担保を正とする。in-memory 経路
  （`XlsFormatReaderTest` の `FakeTestDataReader` ／ `YamlFormatReaderTest` の `loadRawMap` 差し替え）だけの
  担保は ✅ に数えない。これは #20／#24 で確定した基準である。

### 0.3 軸の定義（要約）

| 軸 | 正式名 | 要素 | 詳細 |
|---|---|---|---|
| A | データタイプ | `DataType` 14 種（A-01〜A-14） | `inventory.md` §0.2 |
| B | ブロック実装 | sealed 階層の具象 4 種（B-1〜B-4） | `inventory.md` §0.3 |
| C | 中間モデル全フィールド | 8 クラス 21 フィールド（C-01〜C-21） | `inventory.md` §0.4 |
| D | 値の表現 | 辺① 8／辺② 12／辺③ 8／辺④ 9 ケース | `inventory.md` §0.5 |
| E | 多重度 | 4 観点 × 多重度（E-1〜E-3 は 0／1／複数、E-4 は 1／複数） | `inventory.md` §0.6 |
| F | 異常系 | 辺① 6／辺② 5／辺③ 4／辺④ 3 ケース | `inventory.md` §0.7 |

軸C の 21 フィールドは、必須スカラー 6（C-01・C-03・C-05・C-07・C-10・C-19）／
省略可能フィールド 4（C-06・C-16・C-20・C-21）／空許容コレクション 11
（C-02・C-04・C-08・C-09・C-11・C-12・C-13・C-14・C-15・C-17・C-18）に分かれる。
後ろの 2 群を 2 行に割るため、**軸C は 1 辺あたり 6 ＋ 4×2 ＋ 11×2 ＝ 36 行**になる。

### 0.4 中間モデルの不変条件が「到達不能」を作っている

#25.5 の後半で「不正値は書き出し側でなく**中間モデルの生成時に拒否する**」方針
（`steering.md` Decisions）に沿って番人が中間モデルへ集められた。その結果、**4 辺のどこからも
到達できなくなった軸要素がある**。本書ではそれらを状態欄 `—` とし、理由欄に
**その不変条件を担保しているモデルのテスト**を挙げる。

下表は HEAD の `src/main/java/nablarch/test/tool/converter/model/` を読んで確かめた不変条件の全数である。
**すべてが空欄を作るわけではない** —— 「効く軸要素」欄が `(空)` `(省略)` を伴う行だけが空欄を作り、
それ以外（C-01・C-03・C-05・C-06・C-07・C-10・C-19）は**その要素の `null` 側だけを閉じる**ものであって、
要素そのものは各辺で ✅ である。

| 不変条件（生成時に `IllegalArgumentException`） | 置き場所 | 効く軸要素 |
|---|---|---|
| `dataType` が `DataType.DEFAULT` のブロックは作れない | `TestDataBlock` | A-01（4 辺とも） |
| `dataType` ／ `groupId` ／ `identifier` が `null` のブロックは作れない | `TestDataBlock` | C-05・C-06・C-07 |
| カラム名 0 件のブロックは「セルを持つ行」を持てない | `ColumnRowDataBlock` | C-08(空) の形を限定 |
| `fileType` が `null` のファイルブロックは作れない | `FileDataBlock` | C-10 |
| 固定長ファイル・電文でフィールド長 `null` は保持できない | `ModelPreconditions#requireLengths` | C-21(省略) |
| 本文レコード 0 件の電文ブロックは作れない | `MessageDataBlock` | C-15(空)・E-3(0) の電文経路 |
| フィールド 0 件のレコードレイアウトは作れない | `RecordLayout` | C-17(空) |
| `name` ／ `type` が `null` のフィールド定義は作れない | `FieldDef` | C-19・C-20(省略) |
| 器・読み込み単位の名前 `null` は作れない | `TestDataContainer` ／ `TestDataSection` | C-01・C-03 |

### 0.5 往復テストの扱い（🔺 欄）

`steering.md` Rules（フェーズ2）に従い、既存の往復テスト（`RoundTripTest` ／
`XlsFormatWriterTest#roundTrips*` ／ `YamlFormatWriterTest#roundTrip_*`）が実ファイル経由で通している
軸要素は 🔺 欄に計上する。**正式担保としては数えない**（状態欄は 🔺 欄の内容では動かない）。

**🔺 欄の出典は `inventory.md` §0.8-8 の表（メソッド → 軸要素）を逆向きにしたものである。**
本タスクで確かめたのは**メソッド名が HEAD に実在すること**までであり、
**各往復テストが §0.8-8 の表のとおりの軸要素を通していることは読み直していない（未確認。§6-2）**。
ただし `XlsFormatWriterTest#roundTripsZeroRowTableWithoutEatingNextBlock` ／
`#roundTripsZeroRowListMapWithoutEatingNextBlock` の 2 件は #26.5 以後に足されたもので §0.8-8 に無いため、
本タスクで本文を読んで確かめた。

### 0.6 件数の導出コマンド

**本書の行数と状態別の件数は、本書自身を走査して導く。** 各表の行は必ず
`| <軸要素 ID> | <内容> | <状態> | <担保テストメソッド> | <🔺> | <理由・注記> |` の 6 列で、
軸要素 ID は `A`〜`F` で始まり、状態欄には `✅` `🔺` `❌` `—` `n/a` のいずれか 1 つだけを置く。

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter/.rn/ntf-test-data-converter/coverage
# 辺ごとの行数（辺① → 辺④）
for n in 1 2 3 4; do
  m=$((n + 1))
  printf "辺%s: " "$n"
  awk "/^## ${n}\. /,/^## ${m}\. /" axis-matrix.md | grep -cE '^\| [A-F][0-9-]'
done
# 辺 × 状態の件数
for n in 1 2 3 4; do
  m=$((n + 1))
  printf "辺%s " "$n"
  awk "/^## ${n}\. /,/^## ${m}\. /" axis-matrix.md \
    | grep -E '^\| [A-F][0-9-]' \
    | awk -F'|' '{gsub(/ /,"",$4); c[$4]++} END {for (k in c) printf "%s=%d ", k, c[k]; print ""}'
done
```

**本書に記した担保テストメソッドが 1 件残らず実在することの確認**（`クラス名#メソッド名` を
本書から機械的に抜き、`src/test` の該当クラスに同名の `void` メソッドがあるかを 1 件ずつ照合する）。

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
grep -ohE '\b[A-Z][A-Za-z0-9]*Test#[0-9A-Za-z_ぁ-んァ-ヶ一-龥ー]+' \
     .rn/ntf-test-data-converter/coverage/axis-matrix.md \
  | sort -u \
  | grep -vE '#(roundTrips|roundTrip_)$' \
  | grep -vxF 'XlsFormatReaderRealFileTest#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook' \
  | grep -vxF 'XlsFormatWriterTest#rejectsMessageBlockWithoutRecords' \
  | while IFS='#' read -r cls mth; do
      f=$(find src/test -name "$cls.java")
      if [ -z "$f" ]; then
        echo "NG(class) $cls#$mth"
      elif ! perl -CSDA -e 'my ($m, $f) = @ARGV; open(my $h, "<:utf8", $f) or exit 1;
                            local $/; my $src = <$h>; exit($src =~ /\bvoid\s+\Q$m\E\s*\(/ ? 0 : 1);' \
             "$mth" "$f"; then
        echo "NG(method) $cls#$mth"
      fi
    done | sort -u
```

出力が**無いこと**（＝ NG が 1 件も無いこと）が確認結果である。

**除外している 4 件は、いずれも「担保テストメソッドとして記した名前」ではない。**

- `XlsFormatWriterTest#roundTrips` ／ `YamlFormatWriterTest#roundTrip_` — §0.5 で往復テスト群を
  `#roundTrips*` ／ `#roundTrip_*` と総称している箇所。末尾の `*` はメソッド名の一部ではない
- `XlsFormatReaderRealFileTest#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook` ／
  `XlsFormatWriterTest#rejectsMessageBlockWithoutRecords` — §6-1 が
  「`inventory.md` はこの名前を挙げているが HEAD に無い」と述べている箇所。**無いことがそこでの主張である**

`perl` に `-CSDA` を付けているのは、**日本語名のテストメソッドがあるため**である
（`model/` 配下のテストクラスは日本語のメソッド名を使う）。`A` を落とすと `@ARGV` が
バイト列のまま渡り、UTF-8 で復号したファイル本文と照合できずに全件 NG になる。

**2026-08-21 実測: 照合対象は重複を除いて 210 件、NG は 0 件である。**
照合対象の件数は上のパイプラインの `while` を `wc -l` へ差し替えれば導ける。

---

## 1. 辺① Excel→中間モデル（`XlsFormatReader`）

担保の主体は実 `.xlsx` を入力とする 4 クラス（`XlsFormatReaderRealFileTest` ／
`XlsFormatReaderCellTypeTest` ／ `XlsFormatReaderInvalidInputTest` ／ `XlsReferenceFixtureTest`）である。
`XlsFormatReaderTest` は `FakeTestDataReader` に canned 行を与える in-memory 経路であり、
§0.2 の基準により ✅ に数えない（同クラスが唯一の担保である行は無い）。

### 1.1 軸A データタイプ（14 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| A-01 | `DEFAULT` | — | — | — | 到達不能。`TestCoreReaderAdapter` が `DEFAULT` のブロックを `continue` でスキップし、さらに `TestDataBlock` が生成時に `DEFAULT` を拒否する。根拠テスト `TestDataBlockTest#データタイプDEFAULTのブロックは生成できない`（`issues.md` XLS-20） |
| A-02 | `SETUP_TABLE_DATA` | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` | `RoundTripTest#xls_setupTable_isPreserved` ／ `XlsFormatWriterTest#roundTripsTable` | — |
| A-03 | `EXPECTED_TABLE_DATA` | ✅ | `XlsFormatReaderRealFileTest#readsExpectedTableBlockWithGroupIdFromRealBook` | `RoundTripTest#xls_expectedTable_withGroupId_isPreserved` | — |
| A-04 | `EXPECTED_COMPLETED` | ✅ | `XlsFormatReaderRealFileTest#readsExpectedCompletedTableBlockFromRealBook` | `RoundTripTest#xls_expectedCompleteTable_isPreserved` | — |
| A-05 | `LIST_MAP` | ✅ | `XlsFormatReaderRealFileTest#readsListMapBlockFromRealBook` | `RoundTripTest#xls_listMap_isPreserved` ／ `XlsFormatWriterTest#roundTripsListMap` | — |
| A-06 | `SETUP_FIXED` | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | `RoundTripTest#xls_setupFixed_isPreserved` ／ `XlsFormatWriterTest#roundTripsFixedFile` | — |
| A-07 | `EXPECTED_FIXED` | ✅ | `XlsFormatReaderRealFileTest#readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook` | `RoundTripTest#xls_expectedFixed_isPreserved` | — |
| A-08 | `SETUP_VARIABLE` | ✅ | `XlsFormatReaderRealFileTest#readsSetupVariableFileBlockWithoutFieldLengthFromRealBook` | `RoundTripTest#xls_setupVariable_isPreserved` ／ `XlsFormatWriterTest#roundTripsVariableFile` | — |
| A-09 | `EXPECTED_VARIABLE` | ✅ | `XlsFormatReaderRealFileTest#readsExpectedVariableFileBlockWithGroupIdFromRealBook` | `RoundTripTest#xls_expectedVariable_isPreserved` | — |
| A-10 | `MESSAGE` | ✅ | `XlsFormatReaderRealFileTest#readsMessageBlockFromRealBook` | `RoundTripTest#xls_message_isPreserved` ／ `XlsFormatWriterTest#roundTripsMessage` | — |
| A-11 | `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `XlsFormatReaderRealFileTest#readsAllFourSendSyncMessageTypesFromRealBook` | `RoundTripTest#xls_expectedRequestHeaderMessages_isPreserved` | 4 種を識別子 `RM01`〜`RM04` で分け、データタイプ・グループ ID・識別子を 1 対 1 で突き合わせる |
| A-12 | `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `XlsFormatReaderRealFileTest#readsAllFourSendSyncMessageTypesFromRealBook` | `RoundTripTest#xls_expectedRequestBodyMessages_isPreserved` | 同上 |
| A-13 | `RESPONSE_HEADER_MESSAGES` | ✅ | `XlsFormatReaderRealFileTest#readsAllFourSendSyncMessageTypesFromRealBook` | `RoundTripTest#xls_responseHeaderMessages_isPreserved` | 同上 |
| A-14 | `RESPONSE_BODY_MESSAGES` | ✅ | `XlsFormatReaderRealFileTest#readsAllFourSendSyncMessageTypesFromRealBook` | `RoundTripTest#xls_responseBodyMessages_isPreserved` | 同上 |

### 1.2 軸B ブロック実装（4 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| B-1 | `TableDataBlock` | ✅ | `XlsFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealSheet` | `XlsFormatWriterTest#roundTripsTable` | 1 シートから 4 実装を生成し、実装クラスと識別子（`T` ／ `lm` ／ `f.dat` ／ `m`）を突き合わせる |
| B-2 | `ListMapBlock` | ✅ | `XlsFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealSheet` | `XlsFormatWriterTest#roundTripsListMap` | 同上 |
| B-3 | `FileDataBlock` | ✅ | `XlsFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealSheet` | `XlsFormatWriterTest#roundTripsFixedFile` | 同上 |
| B-4 | `MessageDataBlock` | ✅ | `XlsFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealSheet` | `XlsFormatWriterTest#roundTripsMessage` | 同上 |

### 1.3 軸C 中間モデル全フィールド（36 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| C-01 | `TestDataContainer.name` | ✅ | `XlsFormatReaderRealFileTest#readsContainerAndSectionNamesFromRealBookAndSheetNames` | — | ブック名 `MyBook` が器の名前になる |
| C-02(非空) | `TestDataContainer.sections` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsContainerAndSectionNamesFromRealBookAndSheetNames` | — | 常に 1 件 |
| C-02(空) | 同 空 | — | — | — | 到達不能。`XlsFormatReader#read` が `Collections.singletonList(section)` を返す 1 シート単位 API（`inventory.md` §0.8-6） |
| C-03 | `TestDataSection.name` | ✅ | `XlsFormatReaderRealFileTest#readsContainerAndSectionNamesFromRealBookAndSheetNames` | — | シート名 `MySheet` が読み込み単位の名前になる |
| C-04(非空) | `TestDataSection.blocks` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealSheet` | — | — |
| C-04(空) | 同 空 | ✅ | `XlsFormatReaderRealFileTest#readsEmptyBlockListFromRealSheetWithoutMarkers` | — | マーカー行の無いシート |
| C-05 | `TestDataBlock.dataType` | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` | — | `getDataType()` を直接アサート。`null` は `TestDataBlock` が拒否する（`TestDataBlockTest#データ種別がnullのデータブロックは生成できない`） |
| C-06(値あり) | `TestDataBlock.groupId` 値あり | ✅ | `XlsFormatReaderRealFileTest#readsExpectedTableBlockWithGroupIdFromRealBook` | `RoundTripTest#xls_expectedTable_withGroupId_isPreserved` | `[g1]` が角括弧つきのまま入る |
| C-06(省略) | 同 省略（`""`） | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` ／ `#readsListMapBlockFromRealBook` ／ `#readsMessageBlockFromRealBook` | `RoundTripTest#xls_setupTable_isPreserved` | `null` ではなく `""`。`null` は `TestDataBlock` が拒否する |
| C-07 | `TestDataBlock.identifier` | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` | — | — |
| C-08(非空) | `ColumnRowDataBlock.columnNames` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` | — | — |
| C-08(空) | 同 空 | ✅ | `XlsFormatReaderRealFileTest#dropsMarkerOnlyRowsAsEmptyEntriesInRealBook` ／ `#dropsMarkerOnlyRowsAsEmptyEntriesInListMapInRealBook` | `XlsFormatWriterTest#roundTripsZeroRowTableWithoutEatingNextBlock` ／ `#roundTripsZeroRowListMapWithoutEatingNextBlock` | マーカー列だけのブロックで到達する（`issues.md` XLS-08）。除外後は行も空になる。**0 件テーブルに残る担保の穴は §7 の ①〜⑧** |
| C-09(非空) | `ColumnRowDataBlock.rows` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` | — | — |
| C-09(空) | 同 空 | ✅ | `XlsFormatReaderRealFileTest#readsEmptyRowsFromTableWithoutDataRowsInRealBook` ／ `#readsEmptyRowsFromListMapWithoutDataRowsInRealBook` | — | テーブル経路・LIST_MAP 経路の 2 つ |
| C-10 | `FileDataBlock.fileType`（FIXED ／ VARIABLE の双方） | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook`（FIXED）／ `#readsSetupVariableFileBlockWithoutFieldLengthFromRealBook`（VARIABLE） | — | 必須の 2 値。「省略」は存在しない。`null` は `FileDataBlock` が拒否する（`issues.md` XLS-29） |
| C-11(非空) | `FileDataBlock.directives` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | — | — |
| C-11(空) | 同 空 | — | — | — | 到達不能。本体 `DataFile` のコンストラクタが `file-type` を必ず注入する（`issues.md` XLS-07）。根拠テスト `XlsFormatReaderRealFileTest#readsExpectedFixedFileBlockWithOnlyInjectedDirectiveFromRealBook`（ディレクティブ行を 1 行も書かなくても件数 1） |
| C-12(非空) | `FileDataBlock.records` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | — | — |
| C-12(空) | 同 空 | ✅ | `XlsFormatReaderRealFileTest#readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook` | — | 0 バイトの空ファイルを表す合法な形 |
| C-13(非空) | `MessageDataBlock.directives` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsMessageBlockFromRealBook` | — | `text-encoding` を直接アサート |
| C-13(空) | 同 空 | — | — | — | 到達不能。C-11(空) と同じ器の性質（`issues.md` XLS-07）。根拠テスト `XlsFormatReaderRealFileTest#readsAllFourSendSyncMessageTypesFromRealBook`（送信系 4 種とも `file-type` 1 件が必ず入る） |
| C-14(非空) | `MessageDataBlock.fwHeaderFields` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsMessageBlockFromRealBook` | — | — |
| C-14(空) | 同 空 | ✅ | `XlsFormatReaderRealFileTest#readsAllFourSendSyncMessageTypesFromRealBook` | — | 送信系は入力によらず空 Map になる |
| C-15(非空) | `MessageDataBlock.records` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsMessageBlockFromRealBook` | — | — |
| C-15(空) | 同 空 | — | — | — | 到達不能。`MessageDataBlock` が本文レコード 0 件を生成時に拒否する（`issues.md` YML-12 2形目）。根拠テスト `XlsFormatReaderRealFileTest#rejectsMessageWithFwHeaderOnlyInRealBook` ／ `MessageDataBlockTest#本文レコードが0件の電文ブロックは生成できない` |
| C-16(値あり) | `RecordLayout.recordType` 値あり | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | — | — |
| C-16(省略) | 同 省略（`null`） | ✅ | `XlsFormatReaderRealFileTest#readsOmittedRecordTypeAsNullFromRealBook` | — | `""` ではなく `null`（`issues.md` XLS-06 の修正後） |
| C-17(非空) | `RecordLayout.fields` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | — | — |
| C-17(空) | 同 空 | — | — | — | 到達不能。名前行が 2 列未満だと本体 `DataFileParser` が失敗し、仮に届いても `RecordLayout` が拒否する（`issues.md` XLS-22）。根拠テスト `XlsFormatReaderInvalidInputTest#failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook` ／ `#failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook` ／ `RecordLayoutTest#フィールドを1件も持たないレコードは生成できない` |
| C-18(非空) | `RecordLayout.rows` 非空 | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | — | — |
| C-18(空) | 同 空 | ✅ | `XlsFormatReaderRealFileTest#readsEmptyRowsFromRecordLayoutWithoutValueRowsInRealBook` | — | — |
| C-19 | `FieldDef.name` | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | — | `null` は `FieldDef` が拒否する（`FieldDefTest#名称がnullのフィールド定義は生成できない`。`issues.md` XLS-31） |
| C-20(値あり) | `FieldDef.type` 値あり | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` | — | 器が正規化した値ではなく生行の原文が入る |
| C-20(省略) | 同 省略（`null`） | — | — | — | 到達不能。型の欠落は本体パーサが 2 通りの機構で弾き、仮に届いても `FieldDef` が拒否する。根拠テスト `XlsFormatReaderInvalidInputTest#failsWhenTypeRowIsShorterThanNameRowInRealBook`（機構①）／ `#failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook`（機構②）／ `FieldDefTest#データ型がnullのフィールド定義は生成できない` |
| C-21(値あり) | `FieldDef.length` 値あり | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` ／ `#readsOmittedFieldLengthNotationFromRealBook` | — | 後者は長さ省略記法 `-` が原文のまま入ることを固定する |
| C-21(省略) | 同 省略（`null`） | ✅ | `XlsFormatReaderRealFileTest#readsSetupVariableFileBlockWithoutFieldLengthFromRealBook` | — | **到達できるのは可変長ファイルだけ**。固定長ファイル・電文で `null` は `ModelPreconditions#requireLengths` が拒否する（`issues.md` XLS-30。`FileDataBlockTest#固定長ファイルでフィールド長がnullのフィールド定義は保持できない`） |

### 1.4 軸D 値の表現（セル種別 8 ケース）

対象は **NTF が実行できるテストデータ**（全セルが文字列書式）に限る。番号に欠番があるのは
タグを振り直していないためである（`inventory.md` §0.5）。

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| D1-01 | 文字列 | ✅ | `XlsFormatReaderCellTypeTest#readsStringCellAsIs` | `RoundTripTest#specialNotation_isPreservedInBothPaths` | — |
| D1-05 | 先頭ゼロ文字列 | ✅ | `XlsFormatReaderCellTypeTest#readsLeadingZeroStringCellAsIs` | — | `007` が落ちない |
| D1-12 | セル不在 | ✅ | `XlsFormatReaderCellTypeTest#readsAbsentCellAsEmptyString` ／ `#readsAbsentCellInMiddleOfRowAsEmptyString` | — | 行末の不在と行途中の不在を分けて通す |
| D1-13 | 空文字 | ✅ | `XlsFormatReaderCellTypeTest#readsEmptyStringCellAsEmptyString` ／ `#readsBlankCellAsEmptyString` | `RoundTripTest#xls_setupTable_isPreserved` | 3 種とも `""` になり中間モデル上で区別できない（`issues.md` XLS-04） |
| D1-14 | 前後空白 | ✅ | `XlsFormatReaderCellTypeTest#readsSurroundingWhitespacePreserved` | `RoundTripTest#leadingTrailingWhitespace_isPreservedInBothPaths` | — |
| D1-15 | 改行 | ✅ | `XlsFormatReaderCellTypeTest#readsEmbeddedNewlinePreserved` | — | — |
| D1-16 | リテラル `null` | ✅ | `XlsFormatReaderCellTypeTest#readsLiteralNullStringAsString` | `RoundTripTest#nullCell_xlsConvertsToLiteralString_yamlPreservesNull` | — |
| D1-17 | 表示形式 `@` の数値セル | ✅ | `XlsFormatReaderCellTypeTest#readsTextFormattedNumericCellAsDoubleString` | — | 値は `1.0` になる（`issues.md` XLS-01）。仕様外入力の実挙動の記録である |

Excel 保存物と POI 生成物の一致は `XlsReferenceFixtureTest#readsExcelSavedWorkbookIntoIntermediateModel` ／
`#poiGeneratedWorkbookReadsIdenticallyToExcelSavedWorkbook` が押さえる（軸要素そのものの担保ではない）。

### 1.5 軸E 多重度（11 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| E-1(0件) | セクション内ブロック数 0 | ✅ | `XlsFormatReaderRealFileTest#readsEmptyBlockListFromRealSheetWithoutMarkers` | — | C-04(空) と同じ入力 |
| E-1(1件) | 同 1 | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` | — | ヘルパ `onlyBlock` が `blocks.size()==1` をアサートする |
| E-1(複数) | 同 複数 | ✅ | `XlsFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealSheet` ／ `#readsAllFourSendSyncMessageTypesFromRealBook` | — | いずれも 4 件 |
| E-2(0件) | ブロック内行数 0 | ✅ | `XlsFormatReaderRealFileTest#readsEmptyRowsFromTableWithoutDataRowsInRealBook` ／ `#readsEmptyRowsFromListMapWithoutDataRowsInRealBook` | — | C-09(空) と同じ入力 |
| E-2(1件) | 同 1 | ✅ | `XlsFormatReaderRealFileTest#readsExpectedTableBlockWithGroupIdFromRealBook` | — | — |
| E-2(複数) | 同 複数 | ✅ | `XlsFormatReaderRealFileTest#readsSetupTableBlockFromRealBook` | — | 2 行 |
| E-3(0件) | ファイル内レコードレイアウト数 0 | ✅ | `XlsFormatReaderRealFileTest#readsEmptyRecordsFromFixedFileWithDirectiveOnlyInRealBook` | — | **ファイル系だけで到達する。** 電文系は C-15(空) と同じ理由で到達不能（`XlsFormatReaderRealFileTest#rejectsMessageWithFwHeaderOnlyInRealBook`） |
| E-3(1件) | 同 1 | ✅ | `XlsFormatReaderRealFileTest#readsSetupFixedFileBlockFromRealBook` ／ `#readsMessageBlockFromRealBook` | — | どちらも `records.size()==1` をアサートする |
| E-3(複数) | 同 複数 | ✅ | `XlsFormatReaderRealFileTest#readsMultipleRecordLayoutsFromOneFixedFileInRealBook` | — | **ファイル系だけで到達する。** 電文系は本体 `MessageParser` が 2 つ目の名前行を値行として吸収するため到達不能（`issues.md` XLS-15。根拠テスト `XlsFormatReaderInvalidInputTest#absorbsSecondNameRowAsDataRowInMessageBodyInRealBook`） |
| E-4(1件) | コンテナ内セクション数 1 | ✅ | `XlsFormatReaderRealFileTest#readsContainerAndSectionNamesFromRealBookAndSheetNames` | — | `sections.size()==1` をアサートする |
| E-4(複数) | 同 複数 | — | — | — | 到達不能。C-02(空) と同じ 1 シート単位 API（`inventory.md` §0.8-6） |

### 1.6 軸F 異常系（6 ケース）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| F1-01 | シート不在 | ✅ | `XlsFormatReaderInvalidInputTest#failsWithSheetNotFoundWhenSheetIsAbsentFromRealBook` | — | `IllegalArgumentException: sheet not found.`（原因例外なし） |
| F1-02 | ブック破損 | ✅ | `XlsFormatReaderInvalidInputTest#failsWithGenericRuntimeExceptionWhenWorkbookIsBroken` | — | 汎用 `RuntimeException: test data file open failed.`。ファイル名はどのメッセージにも出ない（`issues.md` XLS-14） |
| F1-03 | 未知のデータタイプ名 | ✅ | `XlsFormatReaderInvalidInputTest#ignoresBlockWhoseMarkerHasUnknownDataTypeNameInRealBook` ／ `#readsSuffixAfterKnownDataTypeNameAsGroupIdInRealBook` | — | 例外にならず継続する。未知名はブロックごと消え（`issues.md` XLS-10）、既知名＋余分な文字はグループ ID になる（XLS-11） |
| F1-04 | マーカーカラム欠落 | ✅ | `XlsFormatReaderInvalidInputTest#readsMarkerColumnWithoutBracketsAsOrdinaryDataColumnInRealBook` ／ `#dropsFirstFieldWhenSendSyncMetaColumnIsMissingInRealBook` | — | 送信同期のメタ列欠落は先頭フィールドと値を落とす（`issues.md` XLS-13） |
| F1-05 | カラム名重複 | ✅ | `XlsFormatReaderInvalidInputTest#deduplicatesDuplicateColumnNamesWithWarningInListMapFromRealBook` ／ `#deduplicatesDuplicateColumnNamesWithWarningInTableFromRealBook` | — | 後勝ちで除去し WARN ログ 1 件。ブック名・シート名・ブロック識別子・カラム名・採用列番号を含むことまでアサートする |
| F1-06 | 行と列の数の不一致 | ✅ | `XlsFormatReaderInvalidInputTest#padsShortDataRowAndDropsCellsBeyondColumnRowInRealBook` ／ `#padsShortValueRowAndDropsCellsBeyondNameRowInFixedFileInRealBook` ／ `#failsWhenLengthRowIsShorterThanNameRowInRealBook` ／ `#failsWhenFixedFileNameRowHasOnlyRecordTypeCellInRealBook` ／ `#failsWhenMessageNameRowHasOnlyRecordTypeCellInRealBook` ／ `#failsWhenTypeRowIsShorterThanNameRowInRealBook` ／ `#failsWhenTypeCellIsBlankInMiddleOfTypeRowInRealBook` | — | 値行の不足は空文字埋め・超過は切り捨てで例外にならない（`issues.md` XLS-12）。名前行・型行・長さ行の不整合は本体パーサが例外で弾く |

**軸F の残る空欄（軸要素の外）**: 継続する異常系（F1-03 ／ F1-04 ／ F1-06 の一部）で
「WARN が出ないこと」は担保テストがアサートしている（`assertNoWarning` ヘルパ）が、
捕捉できるのは `java.util.logging` のルートロガーに届くものだけである。

---

## 2. 辺② YAML→中間モデル（`YamlFormatReader`）

担保の主体は実 `.yaml` を入力とする 3 クラス（`YamlFormatReaderRealFileTest` ／
`YamlFormatReaderScalarTest` ／ `YamlFormatReaderInvalidInputTest`）である。
`YamlFormatReaderTest` は `loadRawMap` を in-memory `Map` へ差し替える経路で、
スカラー解決もスキーマ検証も通らないため §0.2 の基準により ✅ に数えない
（**ただし「拒否されること」の担保としては同クラスが正である行がある** —— C-15(空)・C-21(省略) の理由欄）。

### 2.1 軸A データタイプ（14 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| A-01 | `DEFAULT` | — | — | — | 到達不能。`YamlFormatReader#addBlocksForSection` ／ `#fileDataType` ／ `#addMessageBlocks` は `DEFAULT` 以外の 13 種しか分岐に持たず、さらに `TestDataBlock` が生成時に `DEFAULT` を拒否する。根拠テスト `TestDataBlockTest#データタイプDEFAULTのブロックは生成できない` |
| A-02 | `SETUP_TABLE_DATA` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_setupTable_isPreserved` | 1 ファイルに 13 エントリを書き、`getDataType()` の並びと識別子の並びを記述順で突き合わせる。**フィクスチャのセクション順は定義順の逆**にしてある |
| A-03 | `EXPECTED_TABLE_DATA` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_expectedTable_withGroupId_isPreserved` | 同上 |
| A-04 | `EXPECTED_COMPLETED` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_expectedCompleteTable_isPreserved` | 同上 |
| A-05 | `LIST_MAP` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_listMap_isPreserved` | 同上 |
| A-06 | `SETUP_FIXED` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_setupFixed_isPreserved` | 同上 |
| A-07 | `EXPECTED_FIXED` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_expectedFixed_isPreserved` | 同上 |
| A-08 | `SETUP_VARIABLE` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_setupVariable_isPreserved` | 同上 |
| A-09 | `EXPECTED_VARIABLE` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_expectedVariable_isPreserved` | 同上 |
| A-10 | `MESSAGE` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_message_isPreserved` | 同上 |
| A-11 | `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_expectedRequestHeaderMessages_isPreserved` | 同上 |
| A-12 | `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_expectedRequestBodyMessages_isPreserved` | 同上 |
| A-13 | `RESPONSE_HEADER_MESSAGES` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_responseHeaderMessages_isPreserved` | 同上 |
| A-14 | `RESPONSE_BODY_MESSAGES` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | `RoundTripTest#yaml_responseBodyMessages_isPreserved` | 同上 |

### 2.2 軸B ブロック実装（4 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| B-1 | `TableDataBlock` | ✅ | `YamlFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealYaml` | `RoundTripTest#yaml_setupTable_isPreserved` | 1 ファイルから 4 実装を生成し、実装クラスと識別子（`T` ／ `lm` ／ `f.dat` ／ `RM01`）を突き合わせる |
| B-2 | `ListMapBlock` | ✅ | `YamlFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealYaml` | `RoundTripTest#yaml_listMap_isPreserved` | 同上 |
| B-3 | `FileDataBlock` | ✅ | `YamlFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealYaml` | `RoundTripTest#yaml_setupFixed_isPreserved` | 同上 |
| B-4 | `MessageDataBlock` | ✅ | `YamlFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealYaml` | `RoundTripTest#yaml_message_isPreserved` | 同上 |

### 2.3 軸C 中間モデル全フィールド（36 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| C-01 | `TestDataContainer.name` | ✅ | `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks` | — | リソース名が器の名前になる |
| C-02(非空) | `TestDataContainer.sections` 非空 | ✅ | `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks` | — | 常に 1 件 |
| C-02(空) | 同 空 | — | — | — | 到達不能。`YamlFormatReader#read` が `Collections.singletonList(section)` を返す 1 リソース単位 API（`inventory.md` §0.8-6） |
| C-03 | `TestDataSection.name` | ✅ | `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks` | — | — |
| C-04(非空) | `TestDataSection.blocks` 非空 | ✅ | `YamlFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealYaml` | — | — |
| C-04(空) | 同 空 | ✅ | `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks` | — | `setup_tables: []` で到達する |
| C-05 | `TestDataBlock.dataType` | ✅ | `YamlFormatReaderRealFileTest#readsAllThirteenDataTypesFromRealYaml` | — | 13 種を記述順でアサートする |
| C-06(値あり) | `TestDataBlock.groupId` 値あり | ✅ | `YamlFormatReaderRealFileTest#readsSendSyncEntryWithoutGroupIdAsDefaultGroupFromRealYaml` | `RoundTripTest#yaml_expectedTable_withGroupId_isPreserved` | `group_id: "g"` が `[g]` へ整形されて入る |
| C-06(省略) | 同 省略（`""`） | ✅ | `YamlFormatReaderRealFileTest#readsSendSyncEntryWithoutGroupIdAsDefaultGroupFromRealYaml` ／ `#readsEmptyColumnNamesAndRowsFromTableWithoutRows` | `RoundTripTest#yaml_setupTable_isPreserved` | 省略時は空文字（デフォルトグループ。`issues.md` YML-02 の修正後） |
| C-07 | `TestDataBlock.identifier` | ✅ | `YamlFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealYaml` | — | 4 実装とも識別子を突き合わせる |
| C-08(非空) | `ColumnRowDataBlock.columnNames` 非空 | ✅ | `YamlFormatReaderRealFileTest#preservesListMapColumnOrderAndExcludesMarkerFromRealYaml` | — | 辞書順ではなく原文の記述順であることまで固定する |
| C-08(空) | 同 空 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyColumnNamesAndRowsFromTableWithoutRows` ／ `#readsEmptyColumnNamesAndRowsFromListMapWithoutRows` | — | `rows: []` で到達する。**0 件テーブルに残る担保の穴は §7 の ①〜⑧** |
| C-09(非空) | `ColumnRowDataBlock.rows` 非空 | ✅ | `YamlFormatReaderRealFileTest#readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml` | — | — |
| C-09(空) | 同 空 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyColumnNamesAndRowsFromTableWithoutRows` ／ `#readsEmptyColumnNamesAndRowsFromListMapWithoutRows` | — | C-08(空) と同じ入力 |
| C-10 | `FileDataBlock.fileType`（FIXED ／ VARIABLE の双方） | ✅ | `YamlFormatReaderRealFileTest#readsEmptyRecordsFromFixedFileWithoutRecords`（FIXED）／ `#readsInjectedDirectivesEvenWhenDirectivesAreOmittedInVariableFile`（VARIABLE） | — | スキーマ `$defs.file_data.type` が必須かつ `enum` ＝ `["fixed","variable"]` のため「省略」は存在しない |
| C-11(非空) | `FileDataBlock.directives` 非空 | ✅ | `YamlFormatReaderRealFileTest#stringifiesNonStringDirectiveValuesFromRealYaml` | — | integer ／ boolean の記法も文字列になることまで固定する |
| C-11(空) | 同 空 | — | — | — | 到達不能。本体 `DataFile` のコンストラクタが `file-type` を必ず注入する（`issues.md` XLS-07）。根拠テスト `YamlFormatReaderRealFileTest#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInFile`（件数 1 をアサート） |
| C-12(非空) | `FileDataBlock.records` 非空 | ✅ | `YamlFormatReaderRealFileTest#readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml` | — | — |
| C-12(空) | 同 空 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyRecordsFromFixedFileWithoutRecords` | — | スキーマ `$defs.file_data` は `records.minItems` ＝ 0 |
| C-13(非空) | `MessageDataBlock.directives` 非空 | ✅ | `YamlFormatReaderRealFileTest#readsMessageDirectivesFromRealYaml` | — | — |
| C-13(空) | 同 空 | — | — | — | 到達不能。C-11(空) と同じ器の性質（`issues.md` XLS-07）。根拠テストは 2 つの生成経路それぞれにある —— 受信メッセージ経路が `YamlFormatReaderRealFileTest#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInMessage`、送信系経路が `#readsInjectedFileTypeDirectiveEvenWhenDirectivesAreOmittedInSendSync` |
| C-14(非空) | `MessageDataBlock.fwHeaderFields` 非空 | ✅ | `YamlFormatReaderRealFileTest#readsFwHeaderFieldsFromRealYaml` | — | 記述順で入ることまで固定する |
| C-14(空) | 同 空 | ✅ | `YamlFormatReaderRealFileTest#keepsFwHeaderNamedRecordInMessageFromRealYaml` | — | `fw_header:` を書かない入力で空 Map になる |
| C-15(非空) | `MessageDataBlock.records` 非空 | ✅ | `YamlFormatReaderRealFileTest#keepsFwHeaderNamedRecordInMessageFromRealYaml` | — | `record_type: FW_HEADER` のレコードも落とさない（`issues.md` YML-03 の解消後） |
| C-15(空) | 同 空 | — | — | — | 到達不能。スキーマ `$defs.message_data.records.minItems` ＝ 1 で書けず、仮に届いても `MessageDataBlock` が拒否する（`issues.md` YML-12 2形目）。根拠テスト `YamlFormatReaderTest#readMessage_emptyBody_rejected` ／ `MessageDataBlockTest#本文レコードが0件の電文ブロックは生成できない` |
| C-16(値あり) | `RecordLayout.recordType` 値あり | ✅ | `YamlFormatReaderRealFileTest#readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml` | — | `head` ／ `data` をアサートする |
| C-16(省略) | 同 省略（`null`） | ✅ | `YamlFormatReaderRealFileTest#readsEmptyRowsFromRecordLayoutWithoutRows` ／ `#normalizesLowercaseDefaultRecordTypeToNull` | — | 後者は `"default"`（小文字）も `null` へ正規化されることを固定する |
| C-17(非空) | `RecordLayout.fields` 非空 | ✅ | `YamlFormatReaderRealFileTest#preservesFieldOrderAndValueAlignmentFromRealYaml` | — | 辞書順ではなく原文の記述順であることまで固定する |
| C-17(空) | 同 空 | — | — | — | 到達不能。スキーマ `$defs.record_fragment.properties.fields.minItems` ＝ 1 で書けず、仮に届いても `RecordLayout` が拒否する。根拠テスト `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFieldsIsEmpty` ／ `RecordLayoutTest#フィールドを1件も持たないレコードは生成できない` |
| C-18(非空) | `RecordLayout.rows` 非空 | ✅ | `YamlFormatReaderRealFileTest#preservesFieldOrderAndValueAlignmentFromRealYaml` | — | 値もフィールドの記述順に対応することまで固定する |
| C-18(空) | 同 空 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyRowsFromRecordLayoutWithoutRows` | — | — |
| C-19 | `FieldDef.name` | ✅ | `YamlFormatReaderRealFileTest#preservesFieldOrderAndValueAlignmentFromRealYaml` | — | `null` は `FieldDef` が拒否する（`FieldDefTest#名称がnullのフィールド定義は生成できない`） |
| C-20(値あり) | `FieldDef.type` 値あり | ✅ | `YamlFormatReaderRealFileTest#preservesFieldOrderAndValueAlignmentFromRealYaml` | — | — |
| C-20(省略) | 同 省略（`null`） | — | — | — | 到達不能。スキーマ `$defs.field_def.required` が `type` を必須とし、仮に届いても `FieldDef` が拒否する。根拠テスト `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFieldTypeIsMissing` ／ `FieldDefTest#データ型がnullのフィールド定義は生成できない` |
| C-21(値あり) | `FieldDef.length` 値あり | ✅ | `YamlFormatReaderRealFileTest#readsIntegerLengthNotationAsString` ／ `#preservesFieldOrderAndValueAlignmentFromRealYaml` | — | 前者は integer 記法 `length: 10` が文字列 `"10"` になることを固定する |
| C-21(省略) | 同 省略（`null`） | ✅ | `YamlFormatReaderRealFileTest#readsInjectedDirectivesEvenWhenDirectivesAreOmittedInVariableFile` | — | **到達できるのは可変長ファイルだけ**。固定長ファイルで `length` を書かない YAML はスキーマを通るが中間モデルの生成時に拒否される（`issues.md` XLS-30。`YamlFormatReaderTest#readFile_fixedWithoutLength_rejected`） |

### 2.4 軸D 値の表現（YAML スカラー 12 ケース）

対象は**本体スキーマを通る入力**に限る（`rows` の値の型が `["string","null"]` に限られるため、
引用符なしの `true` ／ `123` ／ `1.50` ／ `.inf` ／ `.nan` は対象外。`inventory.md` §0.5）。
担保テストはすべて `YamlFormatReaderScalarTest` にある。

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| D2-01 | 引用符なし文字列 | ✅ | `YamlFormatReaderScalarTest#readsUnquotedStringAsIs` | — | `abc` → `"abc"` |
| D2-02 | 引用符あり | ✅ | `YamlFormatReaderScalarTest#readsDoubleQuotedStringWithoutQuotes` ／ `#readsSingleQuotedStringWithoutQuotes` | `RoundTripTest#yaml_setupTable_isPreserved` | 引用の別は中間モデルに残らない |
| D2-03 | 引用符付き数値 | ✅ | `YamlFormatReaderScalarTest#readsQuotedNumberAsString` | `YamlFormatWriterTest#roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader` | `"123"` → `"123"` |
| D2-04 | 引用符付き末尾ゼロ小数 | ✅ | `YamlFormatReaderScalarTest#readsQuotedTrailingZeroDecimalAsString` | — | `"1.50"` → `"1.50"` |
| D2-05 | 真偽値に見える文字列 | ✅ | `YamlFormatReaderScalarTest#readsQuotedTrueAsString` ／ `#readsUppercaseTrueAsString` ／ `#readsYesAsString` | — | `"true"` ／ `TRUE` ／ `yes` とも記法どおりの文字列 |
| D2-06 | NULL | ✅ | `YamlFormatReaderScalarTest#readsUnquotedNullAsJavaNull` ／ `#readsOmittedValueAsJavaNull` | `RoundTripTest#nullCell_xlsConvertsToLiteralString_yamlPreservesNull` | 引用符なし `null` と値なしだけが Java `null` になる |
| D2-07 | NULL に見える文字列 | ✅ | `YamlFormatReaderScalarTest#readsQuotedNullAsString` ／ `#readsTildeAsString` ／ `#readsUppercaseNullAsString` | — | `"null"` ／ `~` ／ `NULL` はいずれも文字列（`issues.md` YML-01） |
| D2-08 | 日付・日時風文字列 | ✅ | `YamlFormatReaderScalarTest#readsDateLikeStringAsIs` ／ `#readsDateTimeLikeStringAsIs` | — | — |
| D2-09 | 複数行（リテラルブロック・折りたたみブロック） | ✅ | `YamlFormatReaderScalarTest#readsLiteralBlockScalarKeepingNewlines` ／ `#readsFoldedBlockScalarFoldingNewlinesIntoSpaces` | — | リテラルブロック記法は `"l1\nl2\n"`、折りたたみ記法 `>` は `"l1 l2\n"`（いずれも末尾に改行が付く） |
| D2-10 | 先頭ゼロ・非 JSON 数値記法 | ✅ | `YamlFormatReaderScalarTest#readsLeadingZeroNumberAsString` ／ `#readsHexNotationAsString` | — | `007` ／ `0x1F` とも記法どおりの文字列 |
| D2-11 | 空文字・前後空白 | ✅ | `YamlFormatReaderScalarTest#readsEmptyStringAsIs` ／ `#readsSurroundingWhitespacePreserved` | — | 引用符なしで書くと前後空白は落ちる（`issues.md` YML-11） |
| D2-12 | 特殊文字を含む文字列 | ✅ | `YamlFormatReaderScalarTest#readsColonContainingStringAsIs` ／ `#readsHashContainingStringAsIs` | — | 引用符なしで書くと `#` 以降が落ちる（`issues.md` YML-11） |

**軸D に残る空欄（軸要素の外）**: 12 ケースのうち 10 ケースは `setup_tables` の 1 経路でしか測っていない。
LIST_MAP 経路とレコード断片経路で測ったのは D2-06 と D2-11 の 2 ケースだけである
（`YamlFormatReaderScalarTest#readsUnquotedNullAsJavaNullInListMapPath` ／
`#readsUnquotedNullAsJavaNullInRecordFragmentPath` ／ `#readsEmptyStringAsIsInListMapPath` ／
`#readsEmptyStringAsIsInRecordFragmentPath`。2 ケースとも経路差は無かった）。
**残る 10 ケースに経路差が無いことは未確認である**（`inventory.md` §2.1-2 の開示）。

### 2.5 軸E 多重度（11 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| E-1(0件) | セクション内ブロック数 0 | ✅ | `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks` | — | C-04(空) と同じ入力 |
| E-1(1件) | 同 1 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyColumnNamesAndRowsFromTableWithoutRows` | — | ヘルパ `YamlFixture#onlyBlock` が `blocks.size()==1` をアサートする |
| E-1(複数) | 同 複数 | ✅ | `YamlFormatReaderRealFileTest#readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml` ／ `#readsAllThirteenDataTypesFromRealYaml` | — | 3 件／13 件 |
| E-2(0件) | ブロック内行数 0 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyColumnNamesAndRowsFromTableWithoutRows` ／ `#readsEmptyColumnNamesAndRowsFromListMapWithoutRows` | — | C-09(空) と同じ入力 |
| E-2(1件) | 同 1 | ✅ | `YamlFormatReaderRealFileTest#readsFourBlockImplementationsFromOneRealYaml` | — | テーブル・LIST_MAP とも `getRows().size()` が 1 であることをアサートする |
| E-2(複数) | 同 複数 | ✅ | `YamlFormatReaderRealFileTest#readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml` | — | 2 行 |
| E-3(0件) | ファイル内レコードレイアウト数 0 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyRecordsFromFixedFileWithoutRecords` | — | **ファイル系だけで到達する。** 電文系は C-15(空) と同じ理由で到達不能 |
| E-3(1件) | 同 1 | ✅ | `YamlFormatReaderRealFileTest#readsEmptyRowsFromRecordLayoutWithoutRows` | — | — |
| E-3(複数) | 同 複数 | ✅ | `YamlFormatReaderRealFileTest#readsMultipleBlocksRowsAndRecordLayoutsFromRealYaml` | — | 断片 2 件 |
| E-4(1件) | コンテナ内セクション数 1 | ✅ | `YamlFormatReaderRealFileTest#namesContainerAndSectionByResourceNameWithoutBlocks` | — | `sections.size()==1` をアサートする |
| E-4(複数) | 同 複数 | — | — | — | 到達不能。C-02(空) と同じ 1 リソース単位 API（`inventory.md` §0.8-6） |

### 2.6 軸F 異常系（5 ケース）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| F2-01 | スキーマ違反 | ✅ | `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenFileTypeIsNotInEnum` ／ `#failsWithSchemaValidationExceptionWhenFieldLengthDoesNotMatchPattern` | — | 違反キーワードの集合と位置を件数つきでアサートする（報告順は `Set` の反復順が契約されていないため突き合わせない） |
| F2-02 | YAML として不正 | ✅ | `YamlFormatReaderInvalidInputTest#failsWithParseErrorWhenYamlIsMalformed` | — | `IllegalStateException`（原因は `YamlEngineException`）。パースで止まるためスキーマ検証には到達しない |
| F2-03 | 未知のキー | ✅ | `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenTopLevelKeyIsUnknown` | — | スキーマのルートが `additionalProperties: false` のため実ファイルでは読み込みごと失敗する（in-memory 経路の「未知キーは無視」とは結果が異なる） |
| F2-04 | 必須構造の欠落 | ✅ | `YamlFormatReaderInvalidInputTest#failsWithSchemaValidationExceptionWhenRequiredRowsIsMissing` ／ `#failsWithSchemaValidationExceptionWhenFieldsIsEmpty` ／ `#failsWithSchemaValidationExceptionWhenFieldTypeIsMissing` | — | 後ろ 2 件は C-17(空) ／ C-20(省略) が到達不能である根拠を兼ねる |
| F2-05 | 空ファイル | ✅ | `YamlFormatReaderInvalidInputTest#readsEmptyFileAsContainerWithoutBlocks` | — | 例外にならず、コンテナ 1 件・セクション 1 件・ブロック 0 件になる |

**軸F の外に残る空欄**: スキーマの自由度を突いた入力の現状挙動は `YamlFormatReaderInvalidInputTest` の
掃引テスト群（`issues.md` YML-04〜YML-11）が固定しているが、**これらは軸A〜F のどの要素にも属さない**。
自由度どうしの組合せは通していない（`inventory.md` §2.1-2 の「この掃引で見ていない範囲」）。

---

## 3. 辺③ 中間モデル→Excel（`XlsFormatWriter`）

担保の主体は `XlsFormatWriterTest`（`build` でメモリ上のブックを作るものと実ファイルを書くものの両方を含む）・
`XlsFormatWriterModelTest`（全件が実 `.xlsx` を書いて開き直す）・`XlsFormatWriterCellTypeTest`・
`XlsFormatWriterInvalidOutputTest` の 4 クラスである。

**辺③では軸B が軸A から独立していない。** `XlsFormatWriter#layout` は `ColumnRowDataBlock` ／
`FileDataBlock` ／ `MessageDataBlock` の 3 分岐しか持たず、`TableDataBlock` と `ListMapBlock` は
どちらも `layoutColumnRow` を通る。版面上で両者を分けるのは `getDataType()` から作る識別セルだけで、
それは軸A そのものである（`inventory.md` §3.3 の B 行）。

### 3.1 軸A データタイプ（14 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| A-01 | `DEFAULT` | — | — | — | 到達不能。`TestDataBlock` が `DEFAULT` のブロックを生成時に拒否するため入力を組めない（`issues.md` XLS-20。#25.5 §1-G）。根拠テスト `TestDataBlockTest#データタイプDEFAULTのブロックは生成できない`。**#23 が辺③に置いていた担保テスト 2 件は入力を組めなくなったため削除済みで、HEAD に該当メソッドは無い**（`inventory.md` §0.1-2 の追補その 5 の削除一覧） |
| A-02 | `SETUP_TABLE_DATA` | ✅ | `XlsFormatWriterTest#writesTableBlock` | `RoundTripTest#xls_setupTable_isPreserved` ／ `XlsFormatWriterTest#roundTripsTable` | 識別セル `SETUP_TABLE=USERS` をアサートする |
| A-03 | `EXPECTED_TABLE_DATA` | ✅ | `XlsFormatWriterTest#writesTableMarkerWithGroupId` | `RoundTripTest#xls_expectedTable_withGroupId_isPreserved` | 識別セル `EXPECTED_TABLE[g1]=USERS` |
| A-04 | `EXPECTED_COMPLETED` | ✅ | `XlsFormatWriterTest#writesExpectedCompleteTableMarker` | `RoundTripTest#xls_expectedCompleteTable_isPreserved` | 識別セル `EXPECTED_COMPLETE_TABLE=USERS` |
| A-05 | `LIST_MAP` | ✅ | `XlsFormatWriterTest#writesListMapBlock` | `RoundTripTest#xls_listMap_isPreserved` ／ `XlsFormatWriterTest#roundTripsListMap` | 識別セル `LIST_MAP=result` |
| A-06 | `SETUP_FIXED` | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | `RoundTripTest#xls_setupFixed_isPreserved` ／ `XlsFormatWriterTest#roundTripsFixedFile` | 識別セル `SETUP_FIXED=test.dat` |
| A-07 | `EXPECTED_FIXED` | ✅ | `XlsFormatWriterModelTest#writesExpectedFixedFileBlockWithLengthRow` | `RoundTripTest#xls_expectedFixed_isPreserved` | 識別セル `EXPECTED_FIXED=exp.dat` と固定長固有の長さ行 |
| A-08 | `SETUP_VARIABLE` | ✅ | `XlsFormatWriterTest#writesVariableFileWithoutLengthRow` | `RoundTripTest#xls_setupVariable_isPreserved` ／ `XlsFormatWriterTest#roundTripsVariableFile` | 識別セル `SETUP_VARIABLE=in.csv` |
| A-09 | `EXPECTED_VARIABLE` | ✅ | `XlsFormatWriterModelTest#writesExpectedVariableFileBlockWithoutLengthRow` | `RoundTripTest#xls_expectedVariable_isPreserved` | 識別セル `EXPECTED_VARIABLE[g2]=exp.csv`。可変長なので長さ行なし |
| A-10 | `MESSAGE` | ✅ | `XlsFormatWriterTest#writesMessageBlock` | `RoundTripTest#xls_message_isPreserved` ／ `XlsFormatWriterTest#roundTripsMessage` | 識別セル `MESSAGE=msg1` |
| A-11 | `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `XlsFormatWriterTest#writesSendSyncMessageWithSequenceNo` | `RoundTripTest#xls_expectedRequestHeaderMessages_isPreserved` ／ `XlsFormatWriterTest#roundTripsSendSyncMessage` | 識別セル全体をアサートする |
| A-12 | `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `XlsFormatWriterModelTest#writesExpectedRequestBodyMessagesMarker` | `RoundTripTest#xls_expectedRequestBodyMessages_isPreserved` | #18〜#23 当初版は ✅ と誤判定していた（`#writesSequenceNoForAllSendSyncTypes` は 4 タイプ共通の連番しか見ていない）。#23 レビュー対応で追加（`inventory.md` §3.1-3） |
| A-13 | `RESPONSE_HEADER_MESSAGES` | ✅ | `XlsFormatWriterModelTest#writesResponseHeaderMessagesMarker` | `RoundTripTest#xls_responseHeaderMessages_isPreserved` | 同上 |
| A-14 | `RESPONSE_BODY_MESSAGES` | ✅ | `XlsFormatWriterModelTest#writesResponseBodyMessagesMarker` | `RoundTripTest#xls_responseBodyMessages_isPreserved` | 同上 |

### 3.2 軸B ブロック実装（4 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| B-1 | `TableDataBlock` | ✅ | `XlsFormatWriterTest#writesTableBlock` | `XlsFormatWriterTest#roundTripsTable` | 版面（識別行 → カラム名行 → データ行）を行単位でアサートする |
| B-2 | `ListMapBlock` | ✅ | `XlsFormatWriterTest#writesListMapBlock` | `XlsFormatWriterTest#roundTripsListMap` | **B-1 と同じ `layoutColumnRow` を通る**（本節冒頭）。テストを足しても通る `src/main` の経路は増えない |
| B-3 | `FileDataBlock` | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | `XlsFormatWriterTest#roundTripsFixedFile` | — |
| B-4 | `MessageDataBlock` | ✅ | `XlsFormatWriterTest#writesMessageBlock` | `XlsFormatWriterTest#roundTripsMessage` | — |

### 3.3 軸C 中間モデル全フィールド（36 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| C-01 | `TestDataContainer.name` | ✅ | `XlsFormatWriterTest#writesWorkbookFileWithSheetPerSection` | — | 器の名前が `MyBook.xlsx` になる |
| C-02(非空) | `TestDataContainer.sections` 非空 | ✅ | `XlsFormatWriterTest#writesWorkbookFileWithSheetPerSection` | — | 2 セクション → 2 シート |
| C-02(空) | 同 空 | ✅ | `XlsFormatWriterModelTest#writesWorkbookWithoutSheetsWhenContainerHasNoSections` | — | 例外にならずシート 0 枚のブックが書かれる（`issues.md` XLS-23） |
| C-03 | `TestDataSection.name` | ✅ | `XlsFormatWriterTest#writesWorkbookFileWithSheetPerSection` | — | 読み込み単位の名前がシート名になる |
| C-04(非空) | `TestDataSection.blocks` 非空 | ✅ | `XlsFormatWriterTest#writesTableBlock` | — | — |
| C-04(空) | 同 空 | ✅ | `XlsFormatWriterModelTest#writesEmptySheetWhenSectionHasNoBlocks` | — | シートは作られるが行が 1 行も無い |
| C-05 | `TestDataBlock.dataType` | ✅ | `XlsFormatWriterTest#writesExpectedCompleteTableMarker` | — | 識別セルの文字列がデータタイプから組まれる |
| C-06(値あり) | `TestDataBlock.groupId` 値あり | ✅ | `XlsFormatWriterTest#writesTableMarkerWithGroupId` | `RoundTripTest#xls_expectedTable_withGroupId_isPreserved` | `[g1]` が識別セルに現れる |
| C-06(省略) | 同 省略（`""`） | ✅ | `XlsFormatWriterTest#writesTableBlock` | `RoundTripTest#xls_setupTable_isPreserved` | 識別セルに角括弧が出ない |
| C-07 | `TestDataBlock.identifier` | ✅ | `XlsFormatWriterTest#writesTableBlock` | — | — |
| C-08(非空) | `ColumnRowDataBlock.columnNames` 非空 | ✅ | `XlsFormatWriterTest#writesTableBlock` | — | — |
| C-08(空) | 同 空 | ✅ | `XlsFormatWriterTest#writesMarkerColumnForZeroRowTableBlock` ／ `#writesMarkerColumnForZeroRowListMapBlock` | `XlsFormatWriterTest#roundTripsZeroRowTableWithoutEatingNextBlock` ／ `#roundTripsZeroRowListMapWithoutEatingNextBlock` | **マーカーカラム 1 列 `[EMPTY]` を書く**（`issues.md` XLS-27 の【決着】。#26.5 でセル値を `[空]` から改めた）。カラム名 0 件かつ「セルを持つ行」は `ColumnRowDataBlock` が生成時に拒否する（`TableDataBlockTest#カラムなしでセルを持つ行を抱えるブロックは生成できない`。`issues.md` XLS-21）。**残る穴は §7 の ①〜⑧** |
| C-09(非空) | `ColumnRowDataBlock.rows` 非空 | ✅ | `XlsFormatWriterTest#writesTableBlock` | — | — |
| C-09(空) | 同 空 | ✅ | `XlsFormatWriterModelTest#writesTableWithoutDataRowsWhenRowsAreEmpty` | — | 識別行とカラム名行だけ。データ行の位置は行そのものが無い |
| C-10 | `FileDataBlock.fileType`（FIXED ／ VARIABLE の双方） | ✅ | `XlsFormatWriterTest#writesFixedFileBlock`（FIXED＝長さ行が出る）／ `#writesVariableFileWithoutLengthRow`（VARIABLE＝長さ行が出ない） | `XlsFormatWriterTest#roundTripsFixedFile` ／ `#roundTripsVariableFile` | `null` は `FileDataBlock` が生成時に拒否する（`FileDataBlockTest#ファイル種別がnullのファイルブロックは生成できない`。`issues.md` XLS-29） |
| C-11(非空) | `FileDataBlock.directives` 非空 | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | ディレクティブ行が識別行の次に出る |
| C-11(空) | 同 空 | ✅ | `XlsFormatWriterTest#writesVariableFileWithoutLengthRow` | — | ディレクティブ行が 1 行も出ない |
| C-12(非空) | `FileDataBlock.records` 非空 | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | — |
| C-12(空) | 同 空 | ✅ | `XlsFormatWriterModelTest#writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty` | — | 識別行とディレクティブ行だけ。0 バイトの空ファイルを表す合法な形 |
| C-13(非空) | `MessageDataBlock.directives` 非空 | ✅ | `XlsFormatWriterModelTest#writesDirectiveRowsBeforeFwHeaderRowsInMessage` | — | ディレクティブ行が FW 制御ヘッダ行より上に出ることまで固定する |
| C-13(空) | 同 空 | ✅ | `XlsFormatWriterTest#writesMessageBlock` | — | 識別行の次が FW 制御ヘッダ行になる |
| C-14(非空) | `MessageDataBlock.fwHeaderFields` 非空 | ✅ | `XlsFormatWriterTest#writesMessageBlock` | — | — |
| C-14(空) | 同 空 | ✅ | `XlsFormatWriterTest#writesSendSyncMessageWithSequenceNo` | — | 識別行の次が名前行になる |
| C-15(非空) | `MessageDataBlock.records` 非空 | ✅ | `XlsFormatWriterTest#writesMessageBlock` | — | — |
| C-15(空) | 同 空 | — | — | — | 到達不能。`MessageDataBlock` が本文レコード 0 件を生成時に拒否するため入力を組めない（`issues.md` YML-12 2形目。#25.5 §6-J-2）。根拠テスト `MessageDataBlockTest#本文レコードが0件の電文ブロックは生成できない`。**#23 が置いた版面テスト 1 件と、その後 #25.5 が辺③に置いた番人テスト 2 件はいずれも削除済みで、HEAD に該当メソッドは無い** |
| C-16(値あり) | `RecordLayout.recordType` 値あり | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | 名前行の列 0 にレコード種別が出る |
| C-16(省略) | 同 省略（`null`） | ✅ | `XlsFormatWriterTest#allowsNullRecordTypeOnSingleRecord` | — | 名前行の列 0 が空セルになる。**2 レコード目以降の `null` ／ `""` は `IllegalStateException`**（`#rejectsNullRecordTypeOnSecondRecord` ／ `#rejectsEmptyRecordTypeOnSecondRecord`） |
| C-17(非空) | `RecordLayout.fields` 非空 | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | — |
| C-17(空) | 同 空 | — | — | — | 到達不能。`RecordLayout` がフィールド 0 件を生成時に拒否するため入力を組めない（`issues.md` XLS-22）。根拠テスト `RecordLayoutTest#フィールドを1件も持たないレコードは生成できない`。**辺③に置いていた番人テスト 2 件は空振りになったため削除済み** |
| C-18(非空) | `RecordLayout.rows` 非空 | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | — |
| C-18(空) | 同 空 | ✅ | `XlsFormatWriterModelTest#writesRecordWithoutDataRowsWhenRecordRowsAreEmpty` | — | 名前行・型行・長さ行まで。データ行は行そのものが無い |
| C-19 | `FieldDef.name` | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | 名前行に出る。`null` は `FieldDef` が拒否する（`FieldDefTest#名称がnullのフィールド定義は生成できない`） |
| C-20(値あり) | `FieldDef.type` 値あり | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | 型行に出る。空文字は弾かれず空セルになる（`#writesOmittedMetaAndFieldAsEmpty`） |
| C-20(省略) | 同 省略（`null`） | — | — | — | 到達不能。`FieldDef` が `type` ＝ `null` を生成時に拒否するため入力を組めない（`issues.md` XLS-31。#25.5 §1-D）。根拠テスト `FieldDefTest#データ型がnullのフィールド定義は生成できない`。境界（空文字は通す）は `FieldDefTest#データ型が空文字のフィールド定義は生成できる` |
| C-21(値あり) | `FieldDef.length` 値あり | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | 長さ行に `-` ／ `5` が原文のまま出る |
| C-21(省略) | 同 省略（`null`） | ✅ | `XlsFormatWriterTest#writesVariableFileWithoutLengthRow` | `XlsFormatWriterTest#roundTripsVariableFile` | **到達できるのは可変長ファイルだけ**。固定長ファイル・電文の `null` は `ModelPreconditions#requireLengths` が拒否する（`issues.md` XLS-30。`FileDataBlockTest#固定長ファイルでフィールド長がnullのフィールド定義は保持できない` ／ `MessageDataBlockTest#フィールド長がnullの電文ブロックは生成できない`） |

### 3.4 軸D 値の表現（セル型 8 ケース。すべて `getCellType()` をアサート）

担保テストはすべて `XlsFormatWriterCellTypeTest` にあり、**全件が書き出した実 `.xlsx` を開き直して**確かめる。

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| D3-01 | `"100"` | ✅ | `XlsFormatWriterCellTypeTest#writesNumericLookingStringAsStringCell` | — | 数値セルにならない（`getNumericCellValue()` が `IllegalStateException`） |
| D3-02 | `"=1+1"` | ✅ | `XlsFormatWriterCellTypeTest#writesFormulaLookingStringAsStringCell` | — | 数式セルにならない |
| D3-03 | `"007"` | ✅ | `XlsFormatWriterCellTypeTest#writesLeadingZeroStringAsStringCell` | — | 先頭ゼロが落ちない |
| D3-04 | `null` | ✅ | `XlsFormatWriterCellTypeTest#writesNullValueAsLiteralNullStringCell` | `RoundTripTest#nullCell_xlsConvertsToLiteralString_yamlPreservesNull` ／ `XlsFormatWriterTest#roundTripsNullCellAsLiteralNullString` | リテラル `"null"` になる（空白セルにならない） |
| D3-05 | `""` | ✅ | `XlsFormatWriterCellTypeTest#writesEmptyValueAsEmptyStringCell` | `XlsFormatWriterTest#roundTripsTable` | 長さ 0 の文字列セル（`CELL_TYPE_BLANK` へ退化しない） |
| D3-06 | 改行含む文字列 | ✅ | `XlsFormatWriterCellTypeTest#writesLineFeedStringAsStringCell` ／ `#replacesCrLfWithSingleLineFeedInStringCell` ／ `#replacesLoneCarriageReturnWithLineFeedInStringCell` ／ `#keepsCarriageReturnRawInSharedStringsXml` | — | `CR` は `LF` へ置換される（`issues.md` XLS-18）。**変化が起きるのは読み戻し（XML パース）区間**であることを、最後の 1 件が `xl/sharedStrings.xml` の生バイトで示す |
| D3-07 | 32767 文字超 | ✅ | `XlsFormatWriterCellTypeTest#writesStringLongerThanExcelCellLimitAsStringCell` ／ `#writesStringOfExcelCellLimitLengthAsStringCell` | — | 切り詰め・例外なし（`issues.md` XLS-19） |
| D3-08 | 制御文字含む | ✅ | `XlsFormatWriterCellTypeTest#replacesNulCharacterWithQuestionMark` ／ `#replacesBellCharacterWithQuestionMark` ／ `#replacesVerticalTabCharacterWithQuestionMark` ／ `#replacesUnitSeparatorCharacterWithQuestionMark` ／ `#writesTabCharacterAsIs` ／ `#writesDeleteCharacterAsIs` ／ `#burnsQuestionMarkIntoSharedStringsXmlForControlCharacter` | — | XML 1.0 で不正な文字は `?` へ置換（`issues.md` XLS-17）。**変化が起きるのは直列化区間**であることを最後の 1 件が生バイトで示す |

### 3.5 軸E 多重度（11 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| E-1(0件) | セクション内ブロック数 0 | ✅ | `XlsFormatWriterModelTest#writesEmptySheetWhenSectionHasNoBlocks` | — | C-04(空) と同じ入力 |
| E-1(1件) | 同 1 | ✅ | `XlsFormatWriterModelTest#writesTableWithoutDataRowsWhenRowsAreEmpty` | — | 識別行・カラム名行の次の行が `null` であることまで見るため、2 ブロック目が無いことも固定される |
| E-1(複数) | 同 複数 | ✅ | `XlsFormatWriterTest#insertsBlankRowBetweenBlocks` | — | 2 ブロックの間に空行 1 行が入る位置を固定する |
| E-2(0件) | ブロック内行数 0 | ✅ | `XlsFormatWriterModelTest#writesTableWithoutDataRowsWhenRowsAreEmpty`（テーブル経路）／ `#writesRecordWithoutDataRowsWhenRecordRowsAreEmpty`（ファイル経路の値行） | — | 順に C-09(空) ／ C-18(空) と同じ入力 |
| E-2(1件) | 同 1 | ✅ | `XlsFormatWriterTest#metaRowContainsOnlyValueCells` | — | — |
| E-2(複数) | 同 複数 | ✅ | `XlsFormatWriterTest#writesTableBlock` | — | 2 行 |
| E-3(0件) | ファイル内レコードレイアウト数 0 | ✅ | `XlsFormatWriterModelTest#writesFileBlockWithDirectivesOnlyWhenRecordsAreEmpty` | — | **ファイル系だけで到達する。** 電文系は C-15(空) と同じ理由で到達不能 |
| E-3(1件) | 同 1 | ✅ | `XlsFormatWriterTest#writesFixedFileBlock` | — | — |
| E-3(複数) | 同 複数 | ✅ | `XlsFormatWriterTest#writesMultipleRecordLayouts` | `XlsFormatWriterTest#roundTripsMultipleRecordLayouts` | 断片 2 件の版面の開始行を固定する |
| E-4(1件) | コンテナ内セクション数 1 | ✅ | `XlsFormatWriterInvalidOutputTest#writesSheetNameOfExcelLimitLengthAsIs` | — | `getNumberOfSheets()` が 1 であることをアサートする（同メソッドの主眼は F3-04 だが、セクション 1 件 → シート 1 枚を固定しているのは `src/test` でここだけである） |
| E-4(複数) | 同 複数 | ✅ | `XlsFormatWriterTest#writesWorkbookFileWithSheetPerSection` | — | 2 セクション → 2 シート |

### 3.6 軸F 異常系（4 ケース）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| F3-01 | 出力先不在 | ✅ | `XlsFormatWriterInvalidOutputTest#createsMissingOutputDirectoriesAndWritesWorkbook` | — | 例外にならず多階層の出力先が作られる。対になる「親に通常ファイルが居座りディレクトリを作れない」ケースは `XlsFormatWriterTest#wrapsIoFailure`（`UncheckedIOException`） |
| F3-02 | `overwrite=false` 衝突 | — | — | — | **対象外（衝突検査は上位層）。** `XlsFormatWriter` は `overwrite` を保持せず、検査は `TestDataConverter#checkOverwrite` で完結する。**ただし上位層の既存テストが通すのは `.yaml` を出力側とする衝突だけで、`.xlsx` が既存で衝突する分岐は 1 件も通っていない**（`inventory.md` §0.8-5 の訂正）。辺③に書いても再現できないためここでは空欄とする |
| F3-03 | 書き込み権限なし | ✅ | `XlsFormatWriterInvalidOutputTest#wrapsAccessDeniedExceptionWhenOutputDirectoryIsNotWritable` | — | `UncheckedIOException` ＋ 原因 `AccessDeniedException`。ファイルは作られない。POSIX 権限が効かない環境では `Assume` でスキップする |
| F3-04 | シート名が Excel 制約違反 | ✅ | `XlsFormatWriterInvalidOutputTest#rejectsSheetNameContainingSlash` ／ `#rejectsSheetNameContainingBackslash` ／ `#rejectsSheetNameContainingQuestionMark` ／ `#rejectsSheetNameContainingAsterisk` ／ `#rejectsSheetNameContainingOpeningBracket` ／ `#rejectsSheetNameContainingClosingBracket` ／ `#rejectsSheetNameContainingColon` ／ `#rejectsEmptySheetName` ／ `#writesSheetNameOfExcelLimitLengthAsIs` ／ `#rejectsSheetNameLongerThanExcelLimit` ／ `#rejectsSheetNameWhoseForbiddenCharacterWouldBeRemovedByTruncation` ／ `#rejectsSheetNameWhoseForbiddenCharacterIsAtTheLastPosition` ／ `#failsWhenSameSheetNameOfLimitLengthIsUsedTwice` ／ `#failsWhenSheetNamesDifferOnlyInCase` | — | 31 文字ちょうどはそのまま、32 文字以上は `IllegalArgumentException`（`issues.md` XLS-16 の修正後）。**`null` は辺③の担保ではない** —— `TestDataSection` ／ `TestDataContainer` が生成時に拒否するため（`TestDataContainerTest#名前がnullの読み込み単位は生成できない`。`issues.md` XLS-33）。**アポストロフィ（先頭／末尾）は #22 のスコープ外で未担保** |

**軸F の外に残る空欄**: `XlsFormatWriter#write` の `parent == null` 分岐（親ディレクトリを持たない相対パス）は
`src/test` 全体で一度も通っていない。§0.7 の軸F 4 要素のいずれにも当たらないため本表には計上しないが、
F3-01 の隣接領域であるため開示する（`issues.md` COV-08。`coverage-report.md` §3.2）。
同じく `XlsFormatWriter#layout` の未知ブロック向け `throw` と `#isMarkerColumn` の `null` ガードも未到達だが、
どちらも Java イディオムとしての安全網であり軸要素ではない。

---

## 4. 辺④ 中間モデル→YAML（`YamlFormatWriter`）

担保の主体は `YamlFormatWriterTest`（`serialize` の出力全文を完全一致でアサートするもの中心）・
`YamlFormatWriterModelTest` ／ `YamlFormatWriterScalarTest`（`serialize` と実ファイル往復）・
`YamlFormatWriterInvalidOutputTest`（`write` の異常系）の 4 クラスである。

**辺④でも `list_maps` は独立した経路ではない。** `YamlFormatWriter#emitListMap` と `#emitTable` は
どちらも同じ引数で `emitMapRows` を呼ぶため、**値の記法は同一のコードで担保されている**。
残る差はキー側の literal（`table:` ／ `id:`）だけである。出典:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter
grep -c "emitMapRows(sb, entry, block.getColumnNames(), block.getRows());" \
  src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java
```

出力は **2**（`emitTable` と `emitListMap` の 2 箇所）。

### 4.1 軸A データタイプ（14 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| A-01 | `DEFAULT` | — | — | — | 到達不能。`TestDataBlock` が `DEFAULT` のブロックを生成時に拒否するため入力を組めない（`issues.md` XLS-20。#25.5 §1-G）。根拠テスト `TestDataBlockTest#データタイプDEFAULTのブロックは生成できない`。**#18 以来 ✅ の根拠だった辺④の例外テストは入力を組めなくなったため削除済みで、HEAD に該当メソッドは無い。** 副作用として `YamlFormatWriter#sectionKey` の `default` 分岐が到達不能になった（`coverage-report.md` §3.3） |
| A-02 | `SETUP_TABLE_DATA` | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | `RoundTripTest#yaml_setupTable_isPreserved` ／ `YamlFormatWriterTest#roundTrip_table_isPreservedThroughRealReader` | `setup_tables:` キーへ写ることを出力全文の完全一致で固定する |
| A-03 | `EXPECTED_TABLE_DATA` | ✅ | `YamlFormatWriterTest#serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId` | `RoundTripTest#yaml_expectedTable_withGroupId_isPreserved` | `expected_tables:` キー |
| A-04 | `EXPECTED_COMPLETED` | ✅ | `YamlFormatWriterTest#serializeTable_completed_usesExpectedCompleteTablesKey` | `RoundTripTest#yaml_expectedCompleteTable_isPreserved` | `expected_complete_tables:` キー |
| A-05 | `LIST_MAP` | ✅ | `YamlFormatWriterTest#serializeListMap_usesIdKeyAndColumnOrder` | `RoundTripTest#yaml_listMap_isPreserved` | `list_maps:` キー |
| A-06 | `SETUP_FIXED` | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | `RoundTripTest#yaml_setupFixed_isPreserved` ／ `YamlFormatWriterTest#roundTrip_fixedFile_isPreservedThroughRealReader` | `setup_files:` キー |
| A-07 | `EXPECTED_FIXED` | ✅ | `YamlFormatWriterModelTest#writesSetupVariableAndExpectedFixedUnderTheirSectionKeysInEncounterOrder` ／ `#restoresExpectedFixedDataTypeThroughRealReader` | `RoundTripTest#yaml_expectedFixed_isPreserved` | `expected_files:` キーへ写ることを出力全文で。読み戻しても `EXPECTED_FIXED` ／ `FIXED` のまま |
| A-08 | `SETUP_VARIABLE` | ✅ | `YamlFormatWriterModelTest#writesSetupVariableAndExpectedFixedUnderTheirSectionKeysInEncounterOrder` ／ `#restoresSetupVariableDataTypeThroughRealReader` | `RoundTripTest#yaml_setupVariable_isPreserved` | `setup_files:` キーへ写る |
| A-09 | `EXPECTED_VARIABLE` | ✅ | `YamlFormatWriterTest#serializeFile_variableOmitsDirectivesAndRecordTypeAndLength` | `RoundTripTest#yaml_expectedVariable_isPreserved` | `expected_files:` キー ＋ `type: "variable"` |
| A-10 | `MESSAGE` | ✅ | `YamlFormatWriterTest#serializeMessage_withDirectivesAndFwHeader` | `RoundTripTest#yaml_message_isPreserved` ／ `YamlFormatWriterTest#roundTrip_message_preservesFwHeaderAndBody` | `messages:` キー |
| A-11 | `EXPECTED_REQUEST_HEADER_MESSAGES` | ✅ | `YamlFormatWriterTest#serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField` | `RoundTripTest#yaml_expectedRequestHeaderMessages_isPreserved` ／ `YamlFormatWriterTest#roundTrip_sendSync_preservesGroupIdAndNoField` | 単独ブロックの出力全文を完全一致でアサートする |
| A-12 | `EXPECTED_REQUEST_BODY_MESSAGES` | ✅ | `YamlFormatWriterModelTest#writesExpectedRequestBodyMessagesUnderItsOwnSectionKey` | `RoundTripTest#yaml_expectedRequestBodyMessages_isPreserved` | #18〜#25 当初版は ✅ と誤判定していた（`#serializeSendSync_allFourSectionKeys` は 4 キーが「どこかに現れる」ことしか見ていない）。#25 レビュー対応で追加（`inventory.md` §4.1-2） |
| A-13 | `RESPONSE_HEADER_MESSAGES` | ✅ | `YamlFormatWriterModelTest#writesResponseHeaderMessagesUnderItsOwnSectionKey` | `RoundTripTest#yaml_responseHeaderMessages_isPreserved` | 同上 |
| A-14 | `RESPONSE_BODY_MESSAGES` | ✅ | `YamlFormatWriterModelTest#writesResponseBodyMessagesUnderItsOwnSectionKey` | `RoundTripTest#yaml_responseBodyMessages_isPreserved` | 同上 |

### 4.2 軸B ブロック実装（4 要素）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| B-1 | `TableDataBlock` | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | `YamlFormatWriterTest#roundTrip_table_isPreservedThroughRealReader` | 出力全文の完全一致 |
| B-2 | `ListMapBlock` | ✅ | `YamlFormatWriterTest#serializeListMap_usesIdKeyAndColumnOrder` | `RoundTripTest#yaml_listMap_isPreserved` | **値の記法は B-1 と同じ `emitMapRows` を通る**（本節冒頭）。差は `id:` ／ `table:` のキーだけ |
| B-3 | `FileDataBlock` | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | `YamlFormatWriterTest#roundTrip_fixedFile_isPreservedThroughRealReader` | — |
| B-4 | `MessageDataBlock` | ✅ | `YamlFormatWriterTest#serializeMessage_withDirectivesAndFwHeader` | `YamlFormatWriterTest#roundTrip_message_preservesFwHeaderAndBody` | — |

### 4.3 軸C 中間モデル全フィールド（36 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| C-01 | `TestDataContainer.name` | — | — | — | **辺④は器の名前を読まない。** `YamlFormatWriter#write` は `container.getSections()` を走査して `section.getName() + ".yaml"` を組むだけで、`container.getName()` を 1 度も参照しない（`grep -c "container.getName()" src/main/java/nablarch/test/tool/converter/yaml/YamlFormatWriter.java` → **0**。`grep -c "getName()" <同ファイル>` → **3** で、その内訳は `section` ／ `block.getClass()` ／ `field` である）。出力先ディレクトリ名を決めるのは上位層の `ConverterPathResolver#outputBaseForYaml` であり辺④の担当ではない。**辺③は対照的に `XlsFormatWriter#write` が `container.getName() + ".xlsx"` を組む**（`grep -c "container.getName()" src/main/java/nablarch/test/tool/converter/xls/XlsFormatWriter.java` → **1**。§3.3 の C-01） |
| C-02(非空) | `TestDataContainer.sections` 非空 | ✅ | `YamlFormatWriterModelTest#writesOneYamlFilePerSectionWhenContainerHasMultipleSections` | — | 3 セクション → 3 ファイル |
| C-02(空) | 同 空 | ✅ | `YamlFormatWriterModelTest#writesNothingWhenContainerHasNoSections` | — | 例外にならず、**ファイルも出力先ディレクトリも作られない**（辺③がシート 0 枚のブックを書くのとは非対称） |
| C-03 | `TestDataSection.name` | ✅ | `YamlFormatWriterTest#write_writesEachSectionAsYamlFileWithSerializedContent` | — | 読み込み単位の名前が `<名前>.yaml` になる |
| C-04(非空) | `TestDataSection.blocks` 非空 | ✅ | `YamlFormatWriterTest#serialize_multipleSections_separatedByBlankLineInEncounterOrder` | — | — |
| C-04(空) | 同 空 | ✅ | `YamlFormatWriterTest#serialize_emptySection_isEmptyString` | — | 空文字列になる |
| C-05 | `TestDataBlock.dataType` | ✅ | `YamlFormatWriterTest#serializeTable_completed_usesExpectedCompleteTablesKey` | — | セクションキーがデータタイプから決まる |
| C-06(値あり) | `TestDataBlock.groupId` 値あり | ✅ | `YamlFormatWriterTest#serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId` | `RoundTripTest#yaml_expectedTable_withGroupId_isPreserved` | `[case01]` → `group_id: "case01"`（角括弧が外れる）。角括弧で囲まれていない値の防御的経路は `#serialize_unbracketedGroupId_isUsedAsRawValue` |
| C-06(省略) | 同 省略（`""`） | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | `RoundTripTest#yaml_setupTable_isPreserved` | `group_id:` キーごと出ない |
| C-07 | `TestDataBlock.identifier` | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | — | `table: "USERS"` |
| C-08(非空) | `ColumnRowDataBlock.columnNames` 非空 | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | — | カラム名が `rows:` の各要素のキーになる |
| C-08(空) | 同 空 | ✅ | `YamlFormatWriterTest#serialize_emptyColumnsRow_emitsEmptyFlowMap` | — | セルを持たない行が `- {}` になる。**カラム名 0 件かつ行 0 件のときはカラム名を書く場所が無く、往復するとカラム名が復元されない**（`issues.md` XLS-27 の申し送り。0 件テーブルに残る担保の穴は §7 の ①〜⑧） |
| C-09(非空) | `ColumnRowDataBlock.rows` 非空 | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | — | — |
| C-09(空) | 同 空 | ✅ | `YamlFormatWriterTest#serialize_emptyRows_emitsEmptyFlowList` | — | `rows: []` |
| C-10 | `FileDataBlock.fileType`（FIXED ／ VARIABLE の双方） | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords`（`type: "fixed"`）／ `#serializeFile_variableOmitsDirectivesAndRecordTypeAndLength`（`type: "variable"`） | `YamlFormatWriterTest#roundTrip_fixedFile_isPreservedThroughRealReader` | `null` は `FileDataBlock` が生成時に拒否する（`issues.md` XLS-29） |
| C-11(非空) | `FileDataBlock.directives` 非空 | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | — | `directives:` ブロックが記述順に出る |
| C-11(空) | 同 空 | ✅ | `YamlFormatWriterTest#serializeFile_variableOmitsDirectivesAndRecordTypeAndLength` | — | `directives:` キーごと出ない |
| C-12(非空) | `FileDataBlock.records` 非空 | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | — | 断片 2 件 |
| C-12(空) | 同 空 | ✅ | `YamlFormatWriterModelTest#writesEmptyRecordsListForFileBlockWithoutRecords` | — | `records: []` が出る（`issues.md` YML-12 1形目 の修正後。修正前はキーごと出ず読み戻せなかった）。読み戻せることは `#readsBackFileBlockWithEmptyRecords` が担保する |
| C-13(非空) | `MessageDataBlock.directives` 非空 | ✅ | `YamlFormatWriterTest#serializeMessage_withDirectivesAndFwHeader` | — | — |
| C-13(空) | 同 空 | ✅ | `YamlFormatWriterTest#serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField` | — | `directives:` キーごと出ない |
| C-14(非空) | `MessageDataBlock.fwHeaderFields` 非空 | ✅ | `YamlFormatWriterTest#serializeMessage_withDirectivesAndFwHeader` | `YamlFormatWriterTest#roundTrip_message_preservesFwHeaderAndBody` | `fw_header:` が記述順に出る |
| C-14(空) | 同 空 | ✅ | `YamlFormatWriterTest#serializeSendSync_requiresGroupIdOmitsFwHeaderAndKeepsNoField` | `YamlFormatWriterTest#roundTrip_sendSync_preservesGroupIdAndNoField` | `fw_header:` キーごと出ない |
| C-15(非空) | `MessageDataBlock.records` 非空 | ✅ | `YamlFormatWriterTest#serializeMessage_withDirectivesAndFwHeader` | — | — |
| C-15(空) | 同 空 | — | — | — | 到達不能。`MessageDataBlock` が本文レコード 0 件を生成時に拒否するため入力を組めない（`issues.md` YML-12 2形目。#25.5 §6-J-2）。根拠テスト `MessageDataBlockTest#本文レコードが0件の電文ブロックは生成できない`。**#18 の版面テストと、その後 #25.5 が辺④に置いた番人テスト 2 件はいずれも削除済みで、HEAD に該当メソッドは無い**（境界＝ファイルブロックの `records` 0 件は合法、は C-12(空) が担保する） |
| C-16(値あり) | `RecordLayout.recordType` 値あり | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | — | `record_type: "head"` ／ `"data"` |
| C-16(省略) | 同 省略（`null`） | ✅ | `YamlFormatWriterTest#serializeFile_variableOmitsDirectivesAndRecordTypeAndLength` ／ `#serialize_recordWithEmptyRows_emitsEmptyFlowList` | — | `record_type:` キーごと出ない |
| C-17(非空) | `RecordLayout.fields` 非空 | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | — | — |
| C-17(空) | 同 空 | — | — | — | 到達不能。`RecordLayout` がフィールド 0 件を生成時に拒否するため入力を組めない（`issues.md` YML-12 3形目 ＝ 辺③の XLS-22）。根拠テスト `RecordLayoutTest#フィールドを1件も持たないレコードは生成できない`。**辺④に置いていた番人テスト 2 件は空振りになったため削除済み** |
| C-18(非空) | `RecordLayout.rows` 非空 | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | — | — |
| C-18(空) | 同 空 | ✅ | `YamlFormatWriterTest#serialize_recordWithEmptyRows_emitsEmptyFlowList` | — | `rows: []` |
| C-19 | `FieldDef.name` | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | — | `{name: "f1", ...}`。`null` は `FieldDef` が拒否する（`FieldDefTest#名称がnullのフィールド定義は生成できない`。`issues.md` XLS-31） |
| C-20(値あり) | `FieldDef.type` 値あり | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` ／ `#serialize_fieldWithEmptyType_emitsEmptyType` | — | 後者は境界（空文字は弾かれず `type: ""` が出る）を固定する |
| C-20(省略) | 同 省略（`null`） | — | — | — | 到達不能。`FieldDef` が `type` ＝ `null` を生成時に拒否するため入力を組めない（`issues.md` YML-12 4形目 ／ XLS-31）。根拠テスト `FieldDefTest#データ型がnullのフィールド定義は生成できない`。**#18 が担保としていた「`type:` キーを省いて書く」テストと、その後 #25.5 が辺④に置いた番人テスト 2 件はいずれも削除済みで、HEAD に該当メソッドは無い** |
| C-21(値あり) | `FieldDef.length` 値あり | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | — | `{..., length: "5"}` |
| C-21(省略) | 同 省略（`null`） | ✅ | `YamlFormatWriterTest#serializeFile_variableOmitsDirectivesAndRecordTypeAndLength` | — | `length:` キーごと出ない。**到達できるのは可変長ファイルだけ**（`issues.md` XLS-30） |

### 4.4 軸D 値の表現（YAML 表現 9 ケース）

「記法」は出力 YAML の記法そのもの、「往復」は書いて読み戻したときに元の文字列へ戻ることを指す。

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| D4-01 | `"100"` | ✅ | `YamlFormatWriterScalarTest#writesNumberLookingStringAsDoubleQuotedScalar`（記法）／ `#restoresNumberLookingStringThroughRealReader`（往復） | `YamlFormatWriterTest#roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader` | `V: "100"` |
| D4-02 | `"true"` | ✅ | `YamlFormatWriterScalarTest#writesBooleanLookingStringAsDoubleQuotedScalar` ／ `#restoresBooleanLookingStringThroughRealReader` | — | レコード断片経路と `emitMap` 経路にも埋め込んである（`YamlFormatWriterModelTest#quotesBooleanAndDateLookingValuesInFwHeader`） |
| D4-03 | `"null"` | ✅ | `YamlFormatWriterTest#serialize_distinguishesNullFromNullString` | `YamlFormatWriterTest#roundTrip_nullAndNullStringAndNumeric_areDistinguishedThroughRealReader` | `V: "null"` |
| D4-04 | `null`（Java `null`） | ✅ | `YamlFormatWriterTest#serialize_distinguishesNullFromNullString` ／ `#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | `RoundTripTest#nullCell_xlsConvertsToLiteralString_yamlPreservesNull` | `V: null`（クォート無し）。Java `null` へ戻る |
| D4-05 | `""` | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | `YamlFormatWriterTest#roundTrip_table_isPreservedThroughRealReader` | `NAME: ""`。`null` と区別される |
| D4-06 | `"007"` | ✅ | `YamlFormatWriterScalarTest#writesLeadingZeroNumberAsDoubleQuotedScalar` ／ `#restoresLeadingZeroNumberThroughRealReader` | — | — |
| D4-07 | 改行含む | ✅ | `YamlFormatWriterTest#serialize_escapesQuotesBackslashAndControlChars` ／ `YamlFormatWriterScalarTest#writesNewlineContainingStringAsEscapedSingleLineScalar` ／ `#foldsLongEscapedValueWithBackslashContinuation` ／ `#restoresNewlineContainingStringThroughRealReader` ／ `#restoresFoldedLongEscapedValueThroughRealReader` | — | ブロックスカラーにはならず 1 行の `"l1\nl2"`。80 桁を超えると行末 `\` で折り返す（折り返しても往復する） |
| D4-08 | `"2026-08-07"` | ✅ | `YamlFormatWriterScalarTest#writesDateLookingStringAsDoubleQuotedScalar` ／ `#restoresDateLookingStringThroughRealReader` | — | 日付にならない。D4-02 と同じく別経路にも埋め込んである |
| D4-09 | コロン・ハイフン・`#` 含む | ✅ | `YamlFormatWriterScalarTest#writesColonHyphenAndHashContainingStringAsDoubleQuotedScalar` ／ `#restoresColonHyphenAndHashContainingStringThroughRealReader` | — | `#` 以降も残る |

**軸D に残る空欄（軸要素の外）**: 9 ケースのうち 7 ケースは `setup_tables` の `rows` の 1 経路でしか
固定していない（別経路へ埋め込んだのは D4-02 と D4-08 の 2 ケースだけ）。
**残る 7 ケースがレコード断片経路でも同じ記法になることはプローブで確認したがテストにしていない**
（`inventory.md` §4.1-2 の開示）。**キー側**のクォート判定は
`YamlFormatWriterModelTest#quotesDirectiveKeyContainingAnyYamlSpecialOrControlCharacter` が
YAML の特殊文字と制御文字を 1 文字ずつ `directives` のキーに置いて固定する（軸要素ではない）。

### 4.5 軸E 多重度（11 行）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| E-1(0件) | セクション内ブロック数 0 | ✅ | `YamlFormatWriterTest#serialize_emptySection_isEmptyString` | — | C-04(空) と同じ入力 |
| E-1(1件) | 同 1 | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | — | 出力全文の完全一致なので、2 ブロック目が無いことも固定される |
| E-1(複数) | 同 複数 | ✅ | `YamlFormatWriterTest#serialize_multipleSections_separatedByBlankLineInEncounterOrder` ／ `#serializeTable_withGroupsSameType_coalescedUnderOneSectionWithRawGroupId` | — | 前者は異なるデータタイプ 2 件、後者は同じデータタイプ 2 件 |
| E-2(0件) | ブロック内行数 0 | ✅ | `YamlFormatWriterTest#serialize_emptyRows_emitsEmptyFlowList` | — | C-09(空) と同じ入力 |
| E-2(1件) | 同 1 | ✅ | `YamlFormatWriterTest#serializeTable_completed_usesExpectedCompleteTablesKey` | — | — |
| E-2(複数) | 同 複数 | ✅ | `YamlFormatWriterTest#serializeTable_setupNoGroup_quotesValuesAndKeepsNullEmptyAndNotation` | — | 2 行 |
| E-3(0件) | ファイル内レコードレイアウト数 0 | ✅ | `YamlFormatWriterModelTest#writesEmptyRecordsListForFileBlockWithoutRecords` | — | **ファイル系だけで到達する。** 電文系は C-15(空) と同じ理由で到達不能 |
| E-3(1件) | 同 1 | ✅ | `YamlFormatWriterTest#serializeMessage_withDirectivesAndFwHeader` | — | — |
| E-3(複数) | 同 複数 | ✅ | `YamlFormatWriterTest#serializeFile_fixedWithDirectivesAndMultipleRecords` | `YamlFormatWriterTest#roundTrip_fixedFile_isPreservedThroughRealReader` | 断片 2 件 |
| E-4(1件) | コンテナ内セクション数 1 | ✅ | `YamlFormatWriterTest#write_writesEachSectionAsYamlFileWithSerializedContent` | — | 1 セクションから `td.yaml` が書かれ、中身が直列化結果と一致することをアサートする。**出力ファイルが 1 つだけであることは同メソッドではアサートしていない**（件数を固定するのは複数件側の `YamlFormatWriterModelTest#writesOneYamlFilePerSectionWhenContainerHasMultipleSections`） |
| E-4(複数) | 同 複数 | ✅ | `YamlFormatWriterModelTest#writesOneYamlFilePerSectionWhenContainerHasMultipleSections` | — | 3 セクション → 3 ファイル。書き出しの**順序**は固定していない（ファイルシステム上に順序が現れないため） |

### 4.6 軸F 異常系（3 ケース）

| 軸要素 | 内容 | 状態 | 担保テストメソッド | 🔺 往復 | 理由・注記 |
|---|---|---|---|---|---|
| F4-01 | 出力先不在 | ✅ | `YamlFormatWriterInvalidOutputTest#createsMissingOutputDirectoriesAndWritesYaml` | — | 例外にならず多階層の出力先が作られる。対になる「親に通常ファイルが居座りディレクトリを作れない」ケースは `YamlFormatWriterTest#write_ioError_throwsUncheckedIOException` |
| F4-02 | `overwrite=false` 衝突 | — | — | — | **対象外（上位層で担保済み）。** `YamlFormatWriter` は `overwrite` を保持せず、検査は `TestDataConverter#checkOverwrite` で完結する。**辺④（`.yaml` を出力側とする衝突）は上位層の既存テストが実際に通している** —— `TestDataConverterTest#failsOnExistingOutputWhenOverwriteFalse` ／ `ConverterMojoTest#throwsMojoExecutionExceptionOnOverwriteConflict`（どちらも XLS→YAML で `YamlFormatHandler#outputPaths` を通る） |
| F4-03 | 書き込み権限なし | ✅ | `YamlFormatWriterInvalidOutputTest#wrapsAccessDeniedExceptionWhenOutputDirectoryIsNotWritable` | — | `UncheckedIOException: failed to write YAML: <パス>` ＋ 原因 `AccessDeniedException`。ファイルは作られない |

**軸F の外に残る空欄**: `YamlFormatWriter#write` の `parent == null` 分岐（`issues.md` COV-09）、
`#emitBlock` の未知ブロック向け `throw`、`#rawGroup` の「`[` で始まるが `]` で終わらない」枝
（`issues.md` COV-13）、`#sectionKey` の `default` 分岐は未到達である。
いずれも軸A〜F の要素ではない（`coverage-report.md` §3.3）。
また、**書き出した YAML がスキーマに適合するかを見る担保は無い** —— #25 で見つけた 4 つの形は
`issues.md` YML-12 に記録して固定したが、「その 4 つ以外にスキーマ違反を書き得る形が無い」ことは確かめていない。

---

## 5. 集計

### 5.1 辺 × 軸の行数

| 軸 | 辺① | 辺② | 辺③ | 辺④ |
|---|---|---|---|---|
| A データタイプ | 14 | 14 | 14 | 14 |
| B ブロック実装 | 4 | 4 | 4 | 4 |
| C 中間モデル全フィールド | 36 | 36 | 36 | 36 |
| D 値の表現 | 8 | 12 | 8 | 9 |
| E 多重度 | 11 | 11 | 11 | 11 |
| F 異常系 | 6 | 5 | 4 | 3 |
| **合計** | **79** | **82** | **77** | **77** |

導出コマンドは §0.6 の 1 つめ（4 辺で順に 79 ／ 82 ／ 77 ／ 77、総計 **315** 行になる）。

### 5.2 辺 × 状態の件数

| 状態 | 辺① | 辺② | 辺③ | 辺④ | 合計 |
|---|---|---|---|---|---|
| ✅ 担保あり | 71 | 74 | 72 | 71 | 288 |
| 🔺 弱い担保のみ | 0 | 0 | 0 | 0 | 0 |
| ❌ 未担保 | 0 | 0 | 0 | 0 | 0 |
| — 空欄 | 8 | 8 | 5 | 6 | 27 |
| n/a | 0 | 0 | 0 | 0 | 0 |
| **合計** | **79** | **82** | **77** | **77** | **315** |

導出コマンドは §0.6 の 2 つめ。

**❌ が 0 件なのは「穴が無い」という意味ではない。** 本書の計上単位（§0.2）で数えたときの話であり、
軸A〜F のどの要素にも当てはまらない担保の穴は各節末尾と §7 に開示してある。

### 5.3 空欄（`—`）27 件の内訳

| 分類 | 件数 | 該当 |
|---|---|---|
| 構造上の到達不能（1 リソース単位 API） | 4 | 辺①・辺② の C-02(空) ／ E-4(複数)（2 辺 × 2 要素） |
| 器・スキーマ・本体パーサによる到達不能 | 8 | 辺①・辺② の C-11(空) ／ C-13(空) ／ C-17(空) ／ C-20(省略)（2 辺 × 4 要素） |
| 中間モデルの不変条件による到達不能（#25.5） | 12 | 4 辺の A-01（4）／4 辺の C-15(空)（4）／辺③・辺④ の C-17(空)（2）／辺③・辺④ の C-20(省略)（2） |
| その辺が読まないフィールド | 1 | 辺④ C-01（`YamlFormatWriter#write` は器の名前を参照しない） |
| 対象外（衝突検査は上位層） | 2 | 辺③ F3-02 ／ 辺④ F4-02 |
| **合計** | **27** | |

分類ごとの件数は「該当」欄の要素を数えたもので、4 ＋ 8 ＋ 12 ＋ 1 ＋ 2 ＝ **27** が下のコマンドの出力と一致する。

**辺① C-17(空) ／ C-20(省略) は 2 つの分類にまたがる**（本体パーサが先に弾き、仮に届いても中間モデルが拒否する）。
上表では**先に効くほう**（本体パーサ）に数えている。同じく辺② の 2 件はスキーマ側に数えている。

導出コマンド（分類の件数ではなく、空欄そのものの総数 27 を数える）:

```sh
cd /home/tie303177/work/nablarch/nablarch-testing-converter/.rn/ntf-test-data-converter/coverage
grep -E '^\| [A-F][0-9-]' axis-matrix.md | awk -F'|' '{gsub(/ /,"",$4)} $4 == "—"' | wc -l
```

---

## 6. 本作業で確かめた範囲と、確かめていない範囲

### 6-1. `inventory.md` の記述と HEAD のテストソースが食い違っていた箇所

`inventory.md` の §1〜§4 は各タスク時点のスナップショットであり書き換えない取り決めだが、
**§0.1-2 の「担保の現在地」を述べた記述は現行の正を述べる位置にあり、次の 2 点が陳腐化していた。**
加えてスナップショット表側にも割り当ての誤りが 1 点ある（下記）。**合わせて 3 点である。**
本書は HEAD のテストソースを正として書いてある。

| 箇所 | `inventory.md` の記述 | HEAD の事実 |
|---|---|---|
| 辺① C-15(空) ／ E-3(0件) の電文経路 | §1.2-2 ／ §1.3 が担保テストとして `XlsFormatReaderRealFileTest#readsEmptyRecordsFromMessageWithFwHeaderOnlyInRealBook` を挙げている | 同名メソッドは HEAD に無い。同じ入力は `#rejectsMessageWithFwHeaderOnlyInRealBook` へ**意味ごと反転**しており、本文レコード 0 件の電文は読み取り時点で `IllegalArgumentException` になる。**本書では辺① C-15(空) を「担保済み」ではなく「到達不能」とした** |
| 辺③ C-15(空) の番人 | §0.1-2 の追補その 5 が「`XlsFormatWriterTest#rejectsMessageBlockWithoutRecords` ／ `#rejectsSendSyncMessageBlockWithoutRecords` は HEAD にも在る（削除していない）」と書いている | どちらも HEAD に無い（`grep -rn "rejectsMessageBlockWithoutRecords" src/` → 0 件）。追補その 6 の §6-J-2 で番人が `MessageDataBlock` の生成時へ移り、空振りになった辺③のテストが削除された。**追補その 5 の当該注記だけが追補その 6 に追随していない** |

**もう 1 点、スナップショット表側の割り当てが実装と食い違っている箇所がある。**

| 箇所 | `inventory.md` の記述 | HEAD の事実 |
|---|---|---|
| 辺④ C-01 | §4.1 の表が `write_writesEachSectionAsYamlFileWithSerializedContent` の軸C 欄に `C-01` を挙げている | `YamlFormatWriter#write` は `container.getName()` を 1 度も参照しない（§4.3 の C-01 に導出コマンド）。同メソッドが固定しているのは `section.getName() + ".yaml"` と中身の一致であって器の名前ではない。**本書では辺④ C-01 を空欄とした** |

§4.1 は #18 時点のスナップショットであり書き換えない取り決めのため、
`inventory.md` 側はいずれも本タスクで書き換えていない（`steering.md` Rules に従い、本書が逆引きの正である）。

### 6-2. 推測で書かず「未確認」と明記した箇所

| # | 未確認の内容 | 影響 |
|---|---|---|
| 1 | **🔺 欄の軸要素対応**（§0.5）。往復テストのメソッド名が HEAD に実在することは確かめたが、各メソッドが `inventory.md` §0.8-8 の表のとおりの軸要素を通していることは読み直していない | 🔺 は正式担保に数えないため、状態欄（✅ ／ —）の判定には影響しない |
| 2 | **辺② 軸D の 10 ケースの経路差**（§2.4）。`setup_tables` 以外の 2 経路で同じ結果になることは確かめていない | 軸D 辺② の ✅ は `setup_tables` 経路での担保である |
| 3 | **辺④ 軸D の 7 ケースの経路差**（§4.4）。レコード断片経路はプローブでの確認にとどまりテストが無い | 同上 |
| 4 | **辺④が書き出す YAML のスキーマ適合**（§4.6）。「`issues.md` YML-12 の 4 形以外にスキーマ違反を書き得る形が無い」ことは確かめていない | — |
| 5 | **`.rn/` 側の他文書との整合**。本書は `inventory.md` ／ `issues.md` ／ `coverage-report.md` を書き換えていない。6-1 の 3 点以外にも陳腐化が残っていないかは全数では確かめていない | 本書の判定はテストソースを正としているため影響しない |

上記以外の**担保テストメソッドは、1 件残らずテストソースを開いて Given／When／Then とアサートを読み、
その軸要素を担保していることを確かめた**（§0.6 の 2 つめのコマンドで名前の実在も機械的に照合してある）。

---

## 7. 埋まっていない担保の穴（#26.5 から持ち越した 8 件）

**#26.5 で担保が二層（値の literal 2 件・機構の往復 4 件）あることを実測で確かめたうえで、なお埋まっていない穴である。**
——「未検証だから穴」ではない。8 件はいずれも #26.5 の変更が持ち込んだ欠陥ではなく既存の穴である
（`steering.md` #26.5「#27 へ持ち越す担保の穴 8 件」。ユーザー確定・2026-08-21）。

**8 件はすべて 0 件テーブル／マーカーカラム `[EMPTY]` にまつわるもので、本書の次の 4 行が理由欄から
ここを参照している**: 辺① C-08(空)（§1.3）／辺② C-08(空)（§2.3）／辺③ C-08(空)（§3.3）／
辺④ C-08(空)（§4.3）。**4 行とも状態は ✅ である** —— 空欄ではなく、
✅ の担保の厚みに残る穴としてここに開示する（`steering.md` Rules「担保の穴は、テストを足さない場合でも
台帳に開示する」）。

| # | 穴の内容 |
|---|---|
| ① | 実 `.xlsx` を通る唯一の経路 `SampleConversionTest#convertsClimanSampleIncludingZeroRowTable` がマーカーを検証していない（ブック数とファイル存在だけを見ている。coordinator が実物で確認済み） |
| ② | `EXPECTED_TABLE` の 0 件往復テストが無い |
| ③ | 0 件テーブルが唯一・末尾のブロックの往復テストが無い |
| ④ | `columnNames=[]` かつ「セルを持たない行」を N 件持つ形（XLS-08 ／ YML-04）の往復テストが無い |
| ⑤ | 実カラム名が `[EMPTY]` と衝突する形の明示テストが無い |
| ⑥ | DB 実行経路（`TableData#replaceData`）の再実測が無い |
| ⑦ | 2026-08-19 プローブの (2)(4) は `[空]` での実測であり `[EMPTY]` で再実測していない |
| ⑧ | 命名規約そのもの（ASCII の角括弧トークンであること）を固定するテストが無い |

**⑧ についてはテストを足さない判断が済んでいる**（ユーザー確定・2026-08-21）。
明文（`testdata_notation.rst`）が定めているのは「カラム名を半角角括弧で囲むとマーカーカラムになる」ことだけで、
`EMPTY` という語は明文が定めたものではなく converter の選択だからである。

**この 8 件がある一方で、0 件テーブルの現在の担保は次の 4 件である**（いずれも本タスクで本文を読んで確かめた）。

| 担保テストメソッド | 何を固定しているか |
|---|---|
| `XlsFormatWriterTest#writesMarkerColumnForZeroRowTableBlock` | 識別行の次の行がセル 1 個で、その値が `[EMPTY]` であること（テーブル系） |
| `XlsFormatWriterTest#writesMarkerColumnForZeroRowListMapBlock` | 同（LIST_MAP 系） |
| `XlsFormatWriterTest#roundTripsZeroRowTableWithoutEatingNextBlock` | 0 件テーブルを書いて読み戻すとブロックが 2 件のままで、次のブロックを食わないこと |
| `XlsFormatWriterTest#roundTripsZeroRowListMapWithoutEatingNextBlock` | 同（LIST_MAP 系） |
