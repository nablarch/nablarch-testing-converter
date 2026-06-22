# CC① 作業指示 — nablarch-testing-converter 構築

## 前提

- 全リポジトリは同じ親ディレクトリに clone 済み。

- **yaml リポジトリ（nablarch-testing-yaml）が先に構築・公開済みであること**。converter は yaml に依存するため、yaml が無いとビルドできない。

- **NTF 本体（nablarch-testing）はブランチ `convert-testdata-excel-to-text` のまま**。移動元はこのブランチ。

- このCCは **nablarch-testing-converter で起動**し、converter リポジトリのみ変更する。**本体・yaml は参照のみ。書き込まない。**

- 本体からの削除は後始末 CC（CC②）が行う。このCCはやらない。

## 絶対禁止（最優先）

- **実装の変更は一切しない**。移動するコードのロジック・シグネチャ・振る舞いを変えない。

- 許されるのは物理移動・package/import の機械的調整・pom 設定のみ。

- 通らず実装変更が要ると判断したら **止めてユーザーに確認**。PASS のために実装をいじるのは事故。

- 本体・yaml には書き込まない。

## 目的

NTF テストデータ変換ツール（src/main）と形式間変換テスト（src/test）を converter リポジトリへ切り出し、`mvn test` 全 PASS にする。移動元（本体現ブランチ）との差分ゼロ（実装無改変）を確認する。

## 依存（pom 設定の指針・本体 pom を参考）

- **nablarch-testing-yaml** を依存に追加（converter は `core.reader.YamlTestCoreAdapter` / `core.reader.yaml.YamlSection` / `YamlMessageBuilder.MessageContent` を import）。

- **本体 nablarch-testing** を compile 依存（`core.db.*` / `core.file.*` / `core.reader.*` / `core.util.*` を import）。

- **Excel**: `org.apache.poi:poi-ooxml:3.8`（本体 pom と同一）。

- **YAML**: `org.snakeyaml:snakeyaml-engine:3.0.1`。

- テスト: JUnit。親 POM・groupId は本体に合わせる。

## 移動対象（本体現ブランチ → converter。同一パッケージで配置）

### A. src/main（28件・変換ツール本体）
- `src/main/java/nablarch/test/tool/converter/ 配下一式（28件）`

（内訳: 直下・model/・xls/・yaml/ サブパッケージ）

### B. src/test（21件・形式間変換テスト b）
- `src/test/java/nablarch/test/tool/converter/ 配下一式（21件）`

テストデータは静的同梱ではなく、変換テストが実行時に一時生成・参照する方式（converter 配下に固定 .xls/.yaml は無い）。

## 手順

1. `nablarch-testing-converter` で develop から作業ブランチを作成。

2. pom.xml を作成（上記「依存」: yaml・本体・poi 3.8・snakeyaml 3.0.1）。

3. A（src/main 変換ツール 28）を同一パッケージパスへコピー配置。

4. B（src/test 変換テスト 21）を対応パスへコピー配置。

5. `mvn test` で **全テスト PASS** を確認。落ちたら **配置（pom 依存・リソース）**で解決。**コードは変更しない**。解決できず実装変更が要るなら **止めて確認**。

6. **差分チェック（実装無改変の証明）**: 移動した各ファイルを本体現ブランチの対応ファイルと 1 件ずつ diff し、**package/import 以外の差分が無い**ことを確認。

   - 例: `diff <(git -C ../nablarch-testing show convert-testdata-excel-to-text:src/main/java/nablarch/test/tool/converter/TestDataConverter.java) src/main/java/nablarch/test/tool/converter/TestDataConverter.java`

   - 1 行でも実装差分があれば NG。**止めて報告**。

7. 全対象で差分ゼロを確認後、commit・push。

## 完了条件

- converter `mvn test` 全 PASS。

- 全移動ファイルが本体現ブランチと（package/import 除き）完全一致＝実装無改変を確認済み。

- push 済み。本体・yaml に書き込んでいない。

## 注意

- yaml が未構築だとビルド不能。先に yaml の完了（公開）を確認してから着手する。

- `YamlModeTestBase` や `*YamlTest`、結合テストは converter ではなく integration 行き。移動対象に含めない。
