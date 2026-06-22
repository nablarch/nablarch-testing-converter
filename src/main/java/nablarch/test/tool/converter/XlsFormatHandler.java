package nablarch.test.tool.converter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import nablarch.test.core.reader.PoiXlsReader;
import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;
import nablarch.test.tool.converter.xls.XlsFormatReader;
import nablarch.test.tool.converter.xls.XlsFormatWriter;

/**
 * Excel（{@code .xlsx}）形式の入出力を担うハンドラ。
 *
 * <p>変換元の読込単位は 1 ブック（複数シート）、変換先の出力単位は {@code <basePath>/<コンテナ名>.xlsx}。</p>
 *
 * @author kiyobot
 */
final class XlsFormatHandler implements FormatHandler {

    /** Excel ブックの拡張子（小文字） */
    private static final String XLSX_EXTENSION = ".xlsx";

    @Override
    public List<Path> findSources(ConversionRequest request) {
        return ConverterFileFilter.findXlsFiles(request.getInputPath(), request.getIncludes(), request.getExcludes());
    }

    @Override
    public TestDataContainer read(Path bookFile, List<String> excludeSheets) {
        XlsFormatReader reader = new XlsFormatReader();
        String basePath = parentOf(bookFile);
        String bookName = ConverterPathResolver.stripExtension(bookFile.getFileName().toString());

        List<String> sheetNames = new ArrayList<>(PoiXlsReader.getSheetNames(bookFile.toFile()));
        // 本体 getSheetNames はシート名を順不同で返すため、出力の再現性を担保するため辞書順にソートする。
        sheetNames.sort(null);

        List<TestDataSection> sections = new ArrayList<>();
        for (String sheetName : sheetNames) {
            if (excludeSheets.contains(sheetName)) {
                // 異常系データ等、中間モデル化できないシートは変換対象外。
                continue;
            }
            TestDataContainer single = reader.read(basePath, bookName + "/" + sheetName);
            sections.addAll(single.getSections());
        }
        return new TestDataContainer(bookName, sections);
    }

    @Override
    public TestDataFormatWriter createWriter() {
        return new XlsFormatWriter();
    }

    @Override
    public Path resolveOutputBase(ConversionRequest request, Path source) {
        return ConverterPathResolver.outputBaseForXls(request.getInputPath(), source, request.getOutputPath());
    }

    @Override
    public List<Path> outputPaths(TestDataContainer container, Path outputBase) {
        List<Path> paths = new ArrayList<>();
        paths.add(outputBase.resolve(container.getName() + XLSX_EXTENSION));
        return paths;
    }

    /**
     * ファイルの親ディレクトリパス文字列を返す（親が無ければカレント）。
     *
     * @param file ファイル
     * @return 親ディレクトリパス文字列
     */
    private static String parentOf(Path file) {
        Path parent = file.getParent();
        return parent == null ? "." : parent.toString();
    }
}
