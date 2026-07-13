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
  -Dnablarch-testing-converter.output=src/test/java/com/example/batch/ \
  -Dnablarch-testing-converter.overwrite=true
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

## Excel 出力の整形設定

`yaml → xls` 変換時の Excel 出力は `ExcelFormatConfig` で細かく制御できます。
Maven プラグインの `<configuration>` ブロックで指定する方法と、Java API から直接指定する方法の両方に対応しています。

色名は Apache POI の `IndexedColors` 列挙定数名（例: `AQUA`、`YELLOW`、`LIME`、`PALE_BLUE`）で指定します。
有効な値の一覧は [Apache POI の IndexedColors Javadoc](https://poi.apache.org/apidocs/dev/org/apache/poi/ss/usermodel/IndexedColors.html) を参照してください。

### 既定値

| 設定項目 | 既定値 | 説明 |
|---|---|---|
| testShots ヘッダ色 | ライム | `LIST_MAP=testShots` ブロックのヘッダ行背景色 |
| SETUP 系ヘッダ色 | 淡い青 | `SETUP_TABLE` / `SETUP_FIXED` / `SETUP_VARIABLE` のヘッダ行背景色 |
| EXPECTED 系ヘッダ色 | 淡い黄 | `EXPECTED_*` / `RESPONSE_*` のヘッダ行背景色 |
| その他ヘッダ色 | ラベンダー | `MESSAGE` / `LIST_MAP`（testShots 以外）/ `DEFAULT` のヘッダ行背景色 |
| マーカー列色 | 淡い橙 | `[...]` 形式のマーカーカラム背景色 |
| 列幅自動調整 | ON（上限 20 文字） | 各列の内容に合わせて列幅を調整する |
| 外枠罫線 | あり | データブロックの外周に細線の罫線を引く |
| 内部グリッド線 | あり | データブロック内のセル間に罫線を引く |
| 目盛り線 | OFF | シートの薄いグリッド（Excel デフォルト）の表示 |
| ブロック間空行 | 1 行 | データブロック間に挿入する空行数 |

### Maven プラグインで設定する

pom.xml の `<configuration>` ブロックに `<xlsOutput>` を追加します。省略したフィールドは既定値が使われます。

```xml
<plugin>
  <groupId>com.nablarch.framework</groupId>
  <artifactId>nablarch-testing-converter</artifactId>
  <version>VERSION</version>
  <configuration>
    <xlsOutput>
      <setupHeaderColor>AQUA</setupHeaderColor>
      <expectedHeaderColor>YELLOW</expectedHeaderColor>
      <drawCellBorder>false</drawCellBorder>
      <blankRowsBetweenBlocks>2</blankRowsBetweenBlocks>
    </xlsOutput>
  </configuration>
</plugin>
```

### Java API で設定する

`ExcelFormatConfig.defaults()` をベースに `with*` メソッドで差し替えたコピーを作り、`ConversionRequest.Builder` または `XlsFormatWriter` に渡します。

```java
import nablarch.test.tool.converter.xls.ExcelFormatConfig;
import nablarch.test.tool.converter.xls.XlsFormatWriter;
import org.apache.poi.ss.usermodel.IndexedColors;

ExcelFormatConfig config = ExcelFormatConfig.defaults()
        .withSetupHeaderColor(IndexedColors.AQUA.getIndex())       // SETUP 系の色を変更
        .withExpectedHeaderColor(IndexedColors.YELLOW.getIndex())  // EXPECTED 系の色を変更
        .withCellBorder(false)                                     // 内部グリッド線をOFF
        .withBlankRowsBetweenBlocks(2);                            // ブロック間空行を2行に

XlsFormatWriter writer = new XlsFormatWriter(config);
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
