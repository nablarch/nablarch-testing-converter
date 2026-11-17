package nablarch.test.tool.converter;

import java.nio.file.Path;

/**
 * 入力リソースの位置から出力先ディレクトリ（Writer に渡す {@code basePath}）を算出する純関数群。
 *
 * <p>
 * 入力ディレクトリ配下の相対構造を出力ディレクトリ側へそのまま写す
 * （ブック {@code foo/bar.xlsx} ⇔ YAML ディレクトリ {@code foo/bar/}）。
 * </p>
 *
 * @author kiyobot
 */
public final class ConverterPathResolver {

    /** ユーティリティクラスにつきインスタンス化不可。 */
    private ConverterPathResolver() {
    }

    /**
     * YAML 出力時の Writer 用 basePath を算出する。
     *
     * <p>出力は {@code <出力ルート>/<入力ルートからの相対ディレクトリ>/<ブック名>/} となる。</p>
     *
     * @param inputRoot  入力ルートディレクトリ
     * @param bookFile   変換対象の Excel ブックファイル（{@code inputRoot} 配下）
     * @param outputRoot 出力ルートディレクトリ
     * @return Writer に渡す出力先ディレクトリ
     */
    public static Path outputBaseForYaml(Path inputRoot, Path bookFile, Path outputRoot) {
        Path relative = inputRoot.relativize(bookFile);
        Path relativeDir = relative.getParent();
        Path dir = relativeDir == null ? outputRoot : outputRoot.resolve(relativeDir);
        return dir.resolve(stripExtension(bookFile.getFileName().toString()));
    }

    /**
     * Excel 出力時の Writer 用 basePath を算出する。
     *
     * <p>出力は {@code <出力ルート>/<入力ルートからの相対親ディレクトリ>/} となる。</p>
     *
     * @param inputRoot  入力ルートディレクトリ
     * @param yamlDir    変換対象の YAML コンテナディレクトリ（{@code inputRoot} 配下）
     * @param outputRoot 出力ルートディレクトリ
     * @return Writer に渡す出力先ディレクトリ
     */
    public static Path outputBaseForXls(Path inputRoot, Path yamlDir, Path outputRoot) {
        Path relative = inputRoot.relativize(yamlDir);
        Path relativeParent = relative.getParent();
        return relativeParent == null ? outputRoot : outputRoot.resolve(relativeParent);
    }

    /**
     * ファイル名から拡張子（最後の {@code "."} 以降）を取り除く。
     *
     * @param fileName ファイル名
     * @return 拡張子を除いた名前。{@code "."} を含まない場合はそのまま
     */
    static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? fileName : fileName.substring(0, dot);
    }
}
