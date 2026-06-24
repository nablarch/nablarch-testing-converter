# nablarch-testing-converter

Nablarch テストデータを XLS ↔ YAML 間で変換するツールです。

Maven プラグインとして CLI から実行する方法と、Java API から直接呼び出す方法の2通りで使用できます。

### 変換の動作

- **入力**：`.xls`・`.xlsx` の両方に対応しています。
- **出力**：XLS への変換は常に `.xlsx` で書き出します。
- **ディレクトリ**：入力ディレクトリ配下を再帰検索し、入力との相対パスを保ったまま出力ディレクトリへ書き出します。

---

## Maven プラグインとして使う

### pom.xml への追加

```xml
<plugin>
  <groupId>com.nablarch.framework</groupId>
  <artifactId>nablarch-testing-converter</artifactId>
  <version>VERSION</version>
</plugin>
```

> プロジェクト外から実行する場合（POM なしディレクトリ）も動作します。

### 実行コマンド

```
mvn com.nablarch.framework:nablarch-testing-converter:VERSION:convert \
  -Dnablarch-testing-converter.from=xls \
  -Dnablarch-testing-converter.to=yaml \
  -Dnablarch-testing-converter.input=src/test/java/com/example/batch/ \
  -Dnablarch-testing-converter.output=src/test/java/com/example/batch/
```

### パラメータ一覧

| パラメータ | 必須 | 既定値 | 説明 |
|---|---|---|---|
| `nablarch-testing-converter.from` | ○ | — | 変換元形式（`xls` または `yaml`） |
| `nablarch-testing-converter.to` | ○ | — | 変換先形式（`xls` または `yaml`） |
| `nablarch-testing-converter.input` | ○ | — | 入力ディレクトリ |
| `nablarch-testing-converter.output` | ○ | — | 出力ディレクトリ |
| `nablarch-testing-converter.overwrite` | — | `false` | `true` にすると既存ファイルを上書きする |

`includes` / `excludes`（glob パターン）・`excludeSheets`（シート名）は pom.xml の `<configuration>` ブロックで指定します。

```xml
<plugin>
  <groupId>com.nablarch.framework</groupId>
  <artifactId>nablarch-testing-converter</artifactId>
  <version>VERSION</version>
  <configuration>
    <excludeSheets>
      <excludeSheet>abnormal_case</excludeSheet>
    </excludeSheets>
  </configuration>
</plugin>
```

---

## Java API から直接呼ぶ

### 簡易版（4引数）

```java
import nablarch.test.tool.converter.DataFormat;
import nablarch.test.tool.converter.TestDataConverter;

Path input  = Paths.get("src/test/java/com/example/batch/");
Path output = Paths.get("src/test/java/com/example/batch/");

int count = TestDataConverter.convert(DataFormat.XLS, DataFormat.YAML, input, output);
```

### Builder 版（include / exclude / overwrite を細かく制御）

```java
import nablarch.test.tool.converter.ConversionRequest;
import nablarch.test.tool.converter.DataFormat;
import nablarch.test.tool.converter.TestDataConverter;

ConversionRequest request = new ConversionRequest.Builder()
        .sourceFormat(DataFormat.XLS)
        .targetFormat(DataFormat.YAML)
        .inputPath(Paths.get("src/test/java/com/example/batch/"))
        .outputPath(Paths.get("src/test/java/com/example/batch/"))
        .overwrite(true)
        .excludeSheet("abnormal_case")
        .build();

int count = TestDataConverter.convert(request);
```
