package nablarch.test.tool.converter;

import java.nio.file.Path;
import java.util.List;

import nablarch.test.tool.converter.model.TestDataContainer;

/**
 * 形式（Excel／YAML）ごとに異なる入出力の振る舞いを 1 箇所へ集約するハンドラ。
 *
 * <p>
 * {@link TestDataConverter} は {@link DataFormat#handler()} で得た本ハンドラへ委譲し、形式で分岐しない。
 * </p>
 *
 * @author kiyobot
 */
interface FormatHandler {

    /**
     * 入力ディレクトリ配下から、本形式の変換元となる読込対象を列挙する。
     *
     * @param request 変換リクエスト
     * @return 読込対象パスのリスト（Excel はブックファイル、YAML はコンテナディレクトリ。辞書順）
     */
    List<Path> findSources(ConversionRequest request);

    /**
     * 1 つの読込対象を読み込み、複数リソースを 1 コンテナへ集約する。
     *
     * @param source        読込対象（{@link #findSources} の 1 要素）
     * @param excludeSheets 変換対象外とするリソース名（シート名・ファイル名）
     * @return 集約済みコンテナ
     */
    TestDataContainer read(Path source, List<String> excludeSheets);

    /**
     * 本形式を変換先とする Writer を生成する。
     *
     * @param request 変換リクエスト
     * @return Writer
     */
    TestDataFormatWriter createWriter(ConversionRequest request);

    /**
     * 1 つの読込対象に対応する出力先ディレクトリを解決する。
     *
     * @param request 変換リクエスト
     * @param source  読込対象
     * @return 出力先ディレクトリ
     */
    Path resolveOutputBase(ConversionRequest request, Path source);

    /**
     * 1 つの読込対象を構成するファイルを列挙する（出力が入力自身になっていないかの検査用）。
     *
     * @param source 読込対象
     * @return 読込対象を構成するファイルのリスト
     */
    List<Path> inputPaths(Path source);

    /**
     * コンテナが本形式へ書き出す出力ファイルパスを列挙する（上書き衝突チェック用）。
     *
     * @param container  出力するコンテナ
     * @param outputBase 出力先ディレクトリ
     * @return 出力ファイルパスのリスト
     */
    List<Path> outputPaths(TestDataContainer container, Path outputBase);

    /**
     * 出力先にあると、書き出したファイルの代わりに NTF に読まれてしまうファイルを列挙する。
     *
     * @param container  出力するコンテナ
     * @param outputBase 出力先ディレクトリ
     * @return 出力先にあってはならないファイルのリスト
     */
    List<Path> shadowingPaths(TestDataContainer container, Path outputBase);

    /**
     * 出力先にある本形式のファイルのうち、今回の変換で書き出さないものを列挙する（警告用）。
     *
     * @param container  出力するコンテナ
     * @param outputBase 出力先ディレクトリ
     * @return 書き出さないファイルのリスト（ファイル名の順）
     */
    List<Path> strayPaths(TestDataContainer container, Path outputBase);
}
