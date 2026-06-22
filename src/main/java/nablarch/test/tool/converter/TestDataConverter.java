package nablarch.test.tool.converter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import nablarch.test.tool.converter.model.TestDataContainer;

/**
 * テストデータ変換ツールの入口。
 *
 * <p>
 * 変換元・変換先の形式と入出力ディレクトリを受け取り、入力ディレクトリ配下の各リソースを
 * 形式中立な中間モデル（{@link TestDataContainer}）経由で変換先形式へ書き出す。Excel↔YAML の
 * 双方向に加え、同一形式変換（往復検証用）も扱う 4 方向対応。
 * </p>
 *
 * <p>
 * 形式固有の振る舞い（読込対象の列挙・読込・Writer 生成・出力先解決）は {@link FormatHandler} に集約され、
 * 本クラスは {@link DataFormat#handler()} へ委譲するだけの形式中立な共通フロー
 * （列挙 → 読込 → 出力先解決 → 衝突チェック → 書込）に徹する。形式の追加はハンドラを 1 つ増やすだけで済む。
 * </p>
 *
 * <p>
 * 本体テストコードから直接呼び出して「Excel を実行時に一時 YAML へ変換」する用途を主目的とする
 * （CLI・Maven プラグインはリポジトリ分割後に整備）。
 * </p>
 *
 * @author kiyobot
 */
public final class TestDataConverter {

    /** ユーティリティクラスにつきインスタンス化不可。 */
    private TestDataConverter() {
        throw new AssertionError("TestDataConverter は static 専用です");
    }

    /**
     * 変換元・変換先の形式と入出力ディレクトリだけを指定する簡易入口。
     *
     * <p>上書きは行わず、include／exclude による絞り込みもしない既定リクエストで変換する。</p>
     *
     * @param from   変換元形式
     * @param to     変換先形式
     * @param input  入力ディレクトリ
     * @param output 出力ディレクトリ
     * @return 変換したコンテナ（テストクラス相当）の件数
     */
    public static int convert(DataFormat from, DataFormat to, Path input, Path output) {
        return convert(new ConversionRequest.Builder()
                .sourceFormat(from)
                .targetFormat(to)
                .inputPath(input)
                .outputPath(output)
                .build());
    }

    /**
     * 変換リクエストを解釈して変換を実行する共通入口。
     *
     * @param request 変換リクエスト
     * @return 変換したコンテナ（テストクラス相当）の件数
     * @throws ConverterException 入力ディレクトリが存在しない／上書き禁止下で出力が衝突した場合
     */
    public static int convert(ConversionRequest request) {
        FormatHandler source = request.getSourceFormat().handler();
        FormatHandler target = request.getTargetFormat().handler();
        TestDataFormatWriter writer = target.createWriter();

        int converted = 0;
        for (Path src : source.findSources(request)) {
            TestDataContainer container = source.read(src, request.getExcludeSheets());
            Path outputBase = target.resolveOutputBase(request, src);
            checkOverwrite(request, target, container, outputBase);
            writer.write(container, outputBase.toString());
            converted++;
        }
        return converted;
    }

    /**
     * 上書き禁止時に出力衝突がないことを検証する。
     *
     * @param request    変換リクエスト
     * @param target     変換先形式のハンドラ
     * @param container  出力するコンテナ
     * @param outputBase 出力先ディレクトリ
     * @throws ConverterException 上書き禁止下で出力が衝突した場合
     */
    private static void checkOverwrite(ConversionRequest request, FormatHandler target,
                                       TestDataContainer container, Path outputBase) {
        if (request.isOverwrite()) {
            return;
        }
        for (Path output : target.outputPaths(container, outputBase)) {
            if (Files.exists(output)) {
                throw new ConverterException("output already exists (overwrite=false): " + output);
            }
        }
    }

}
