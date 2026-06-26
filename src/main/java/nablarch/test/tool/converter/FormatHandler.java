package nablarch.test.tool.converter;

import java.nio.file.Path;
import java.util.List;

import nablarch.test.tool.converter.model.TestDataContainer;

/**
 * 形式（Excel／YAML）ごとに異なる入出力の振る舞いを 1 箇所へ集約するハンドラ。
 *
 * <p>
 * 入口（{@link TestDataConverter}）は形式を名指しで分岐せず、{@link DataFormat#handler()} で得た本ハンドラへ
 * 委譲する。これにより形式の追加はハンドラ実装を 1 つ増やすだけで済み、入口の共通フロー
 * （列挙 → 読込 → 出力先解決 → 衝突チェック → 書込）は変更不要になる（開放閉鎖原則）。
 * </p>
 *
 * <p>
 * 形式固有の責務は次の 4 つ。リソース粒度の差（Excel は 1 ブックに複数シート、YAML は 1 ディレクトリに
 * 複数ファイル）は各ハンドラが吸収する。
 * </p>
 * <ul>
 *   <li>変換元としての読込対象（ブックファイル／コンテナディレクトリ）の列挙</li>
 *   <li>1 つの読込対象を中間モデル（{@link TestDataContainer}）へ集約</li>
 *   <li>変換先としての {@link TestDataFormatWriter} の生成</li>
 *   <li>変換先としての出力先ディレクトリ・出力ファイルパスの解決</li>
 * </ul>
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
     * コンテナが本形式へ書き出す出力ファイルパスを列挙する（上書き衝突チェック用）。
     *
     * @param container  出力するコンテナ
     * @param outputBase 出力先ディレクトリ
     * @return 出力ファイルパスのリスト
     */
    List<Path> outputPaths(TestDataContainer container, Path outputBase);
}
