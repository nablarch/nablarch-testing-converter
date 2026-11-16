package nablarch.test.tool.converter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
 * 形式固有の振る舞い（読込対象の列挙・読込・Writer 生成・出力先解決・出力先の検査に使うファイルの列挙）は
 * {@link FormatHandler} に集約され、本クラスは {@link DataFormat#handler()} へ委譲するだけの形式中立な共通フロー
 * （列挙 → 読込 → 出力先解決 → 出力先の検査 → 書込 → 残ったファイルの警告）に徹する。
 * 形式の追加はハンドラを 1 つ増やすだけで済む。
 * </p>
 *
 * <p>
 * Maven プラグイン（{@link ConverterMojo}）と Java API の共通の入口である。テストコードから呼び出して、
 * テストの実行時に変換することもできる。
 * </p>
 *
 * @author kiyobot
 */
public final class TestDataConverter {

    /** ロガー。 */
    private static final Logger LOGGER = Logger.getLogger(TestDataConverter.class.getName());

    /** ユーティリティクラスにつきインスタンス化不可。 */
    private TestDataConverter() {
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
     * @throws ConverterException 入力ディレクトリが存在しない／出力が入力のファイル自身になる／
     *                            出力先に同名の {@code .xls} がある／上書き禁止下で出力が衝突した場合
     */
    public static int convert(ConversionRequest request) {
        FormatHandler source = request.getSourceFormat().handler();
        FormatHandler target = request.getTargetFormat().handler();
        TestDataFormatWriter writer = target.createWriter(request);

        int converted = 0;
        for (Path src : source.findSources(request)) {
            TestDataContainer container = source.read(src, request.getExcludeSheets());
            Path outputBase = target.resolveOutputBase(request, src);
            List<Path> outputs = target.outputPaths(container, outputBase);
            checkNotInput(source.inputPaths(src), outputs);
            checkNotShadowed(target.shadowingPaths(container, outputBase));
            checkOverwrite(request, outputs);
            writer.write(container, outputBase.toString());
            warnStrayOutputs(container, target.strayPaths(container, outputBase), outputBase);
            converted++;
        }
        return converted;
    }

    /**
     * 出力するファイルが、入力のファイル自身でないことを検証する。
     *
     * <p>
     * 変換は NTF が読まない情報（{@code //} のコメント、セルのメモ、YAML の {@code #} のコメントなど）を
     * 落とすため、入力をその場で上書きすると元に戻せない。同じ形式・同じディレクトリで変換した場合に
     * 起こるため、{@code overwrite} によらず止める。
     * </p>
     *
     * @param inputs  入力のファイル
     * @param outputs 出力するファイル
     * @throws ConverterException 出力するファイルが入力のファイル自身である場合
     */
    private static void checkNotInput(List<Path> inputs, List<Path> outputs) {
        for (Path output : outputs) {
            if (!Files.exists(output)) {
                continue;
            }
            for (Path input : inputs) {
                if (isSameFile(output, input)) {
                    throw new ConverterException("output is the input file itself;"
                            + " converting in place would lose comments that the conversion drops."
                            + " Specify an output directory different from the input: " + output);
                }
            }
        }
    }

    /**
     * 出力先に、書き出したファイルの代わりに NTF に読まれてしまうファイルが無いことを検証する。
     *
     * <p>
     * Excel 形式へは常に {@code .xlsx} で書くため、{@code .xls} を変換して同じディレクトリへ戻すと、
     * 同名の {@code .xls} と {@code .xlsx} が並ぶ。本体は {@code .xls} を先に読むので、書き出した
     * {@code .xlsx} は読まれない。{@code .xlsx} を上書きしても {@code .xls} が残る限り同じなので、
     * {@code overwrite} によらず止める。
     * </p>
     *
     * @param shadowing 出力先にあってはならないファイル
     * @throws ConverterException いずれかのファイルが存在する場合
     */
    private static void checkNotShadowed(List<Path> shadowing) {
        for (Path path : shadowing) {
            if (Files.exists(path)) {
                throw new ConverterException("same-name .xls book exists in the output directory: " + path
                        + " (NTF reads the .xls book before the .xlsx book to be written;"
                        + " remove the .xls book or specify another output directory)");
            }
        }
    }

    /**
     * 出力先に、今回の変換で書き出さなかった同じ形式のファイルがあれば警告する。
     *
     * <p>
     * 変換元から消したシートや名前を変えたシートの YAML は出力先に残り、YAML から Excel へ戻すと
     * シートとして復元される。手で置いたファイルの可能性もあるため削除はせず、利用者に知らせるだけにする。
     * </p>
     *
     * @param container  出力したコンテナ
     * @param stray      書き出さなかったファイル
     * @param outputBase 出力先ディレクトリ
     */
    private static void warnStrayOutputs(TestDataContainer container, List<Path> stray, Path outputBase) {
        if (stray.isEmpty()) {
            return;
        }
        List<String> names = new ArrayList<>();
        for (Path path : stray) {
            names.add(path.getFileName().toString());
        }
        LOGGER.warning("[" + container.getName() + "] 出力先 " + outputBase
                + " に、今回の変換で書き出していないファイルがあります。"
                + "変換元から消したシートや名前を変えたシートのものであれば、削除してください"
                + "（残したまま Excel 形式へ変換すると、シートとして復元されます）: " + names);
    }

    /**
     * 2 つのパスが同じファイルを指すかを判定する。
     *
     * @param a パス（存在すること）
     * @param b パス（存在すること）
     * @return 同じファイルなら真
     */
    private static boolean isSameFile(Path a, Path b) {
        try {
            return Files.isSameFile(a, b);
        } catch (IOException e) {
            throw new UncheckedIOException("failed to compare files: " + a + ", " + b, e);
        }
    }

    /**
     * 上書き禁止時に出力衝突がないことを検証する。
     *
     * @param request 変換リクエスト
     * @param outputs 出力するファイル
     * @throws ConverterException 上書き禁止下で出力が衝突した場合
     */
    private static void checkOverwrite(ConversionRequest request, List<Path> outputs) {
        if (request.isOverwrite()) {
            return;
        }
        for (Path output : outputs) {
            if (Files.exists(output)) {
                throw new ConverterException("output already exists (overwrite=false): " + output);
            }
        }
    }

}
