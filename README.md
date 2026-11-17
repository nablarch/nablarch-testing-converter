# nablarch-testing-converter

## このリポジトリの目的

Nablarch Testing Framework（NTF）のテストデータを、Excel 形式と YAML 形式の間で相互に変換するツールです。Maven プラグインとして実行する方法と、Java のコードから呼び出す方法の 2 通りで使用できます。

利用方法（導入・実行方法・パラメータ・Excel 形式の出力の整形・変換の仕様）は、解説書の「テストデータ変換ツール」に記載しています。この README には、本モジュールを開発するための情報だけを記載します。

## 実行方法

リポジトリのルートディレクトリ（`pom.xml` のあるディレクトリ）で、次のコマンドを実行します。

| 目的 | コマンド |
|---|---|
| テスト | `mvn test` |
| BUILD | `mvn clean package` |

## パッケージの構成

| パッケージ | 内容 |
|---|---|
| `nablarch.test.tool.converter` | 変換の入口（`TestDataConverter`）、Maven プラグイン（`ConverterMojo`）、形式ごとの処理の切り替え |
| `nablarch.test.tool.converter.model` | 中間モデル。Excel 形式と YAML 形式に共通の、テストデータの意味だけを持つ |
| `nablarch.test.tool.converter.xls` | Excel 形式の読み書き |
| `nablarch.test.tool.converter.yaml` | YAML 形式の読み書きと、YAML 形式のテストデータの検査（`YamlTestDataValidator`） |
| `nablarch.test.tool.converter.reader` | Excel 形式・YAML 形式の読み込みで共通に使う部品 |
| `nablarch.test.core.reader`・`nablarch.test.core.file` | NTF の非公開メンバーを使うクラス（次節） |

### NTF と同じパッケージに置いているクラス

変換ツールは、テストデータを NTF と同じ読み方で読むために、NTF のパーサを再利用しています。NTF のパーサは、解析結果の取り出し口などがパッケージプライベート・protected のため、次の 2 クラスだけを NTF と同じパッケージに置いています。

| クラス | 使っている NTF の非公開メンバー |
|---|---|
| `nablarch.test.core.reader.XlsParserAccessor` | `DataFileParser#getResult`、`MessageParser#getDelegate`・`getFwHeader`・`onReadLine`・`onTargetTypeFound` など |
| `nablarch.test.core.file.DataFileAccessor` | `DataFile` の `all`・`directives`、`DataFileFragment` の `names`・`types`・`lengths`・`values` |

NTF のこれらのメンバーを変更するときは、変換ツールへの影響を確認してください。ほかのクラスは NTF の公開 API だけを使います。

## 依存関係

| ライブラリ | 役割 |
|---|---|
| `nablarch-testing` | NTF 本体。Excel 形式のテストデータのパーサを再利用する |
| `nablarch-testing-yaml` | YAML 形式のテストデータの読み込みとスキーマ |
| `poi-ooxml` | Excel 形式の読み書き（読み込みは NTF の `PoiXlsReader` を通す） |
| `snakeyaml-engine` | YAML の値の書き出し（引用符とエスケープ）と、`YamlTestDataValidator` での構文の検査 |
| `json-schema-validator` | YAML 形式のテストデータのスキーマ検証 |
| `maven-plugin-api`・`maven-plugin-annotations` | Maven プラグイン |
