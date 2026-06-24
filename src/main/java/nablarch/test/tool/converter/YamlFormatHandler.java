package nablarch.test.tool.converter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import nablarch.test.tool.converter.model.TestDataContainer;
import nablarch.test.tool.converter.model.TestDataSection;
import nablarch.test.tool.converter.yaml.YamlFormatReader;
import nablarch.test.tool.converter.yaml.YamlFormatWriter;

/**
 * YAML（{@code .yaml}）形式の入出力を担うハンドラ。
 *
 * <p>変換元の読込単位は 1 ディレクトリ（複数ファイル）、変換先の出力単位は {@code <basePath>/<セクション名>.yaml}。</p>
 *
 * @author kiyobot
 */
final class YamlFormatHandler implements FormatHandler {

    /** YAML ファイルの拡張子（小文字） */
    private static final String YAML_EXTENSION = ".yaml";

    @Override
    public List<Path> findSources(ConversionRequest request) {
        return ConverterFileFilter.findYamlDirs(request.getInputPath(), request.getIncludes(), request.getExcludes());
    }

    @Override
    public TestDataContainer read(Path yamlDir, List<String> excludeSheets) {
        // YAML はファイル単位で管理するためシート除外の概念がなく、excludeSheets は使用しない。
        YamlFormatReader reader = new YamlFormatReader();
        String basePath = yamlDir.toString();
        String dirName = yamlDir.getFileName().toString();

        List<TestDataSection> sections = new ArrayList<>();
        for (Path yamlFile : listYamlFiles(yamlDir)) {
            String resourceName = ConverterPathResolver.stripExtension(yamlFile.getFileName().toString());
            TestDataContainer single = reader.read(basePath, resourceName);
            sections.addAll(single.getSections());
        }
        return new TestDataContainer(dirName, sections);
    }

    @Override
    public TestDataFormatWriter createWriter() {
        return new YamlFormatWriter();
    }

    @Override
    public Path resolveOutputBase(ConversionRequest request, Path source) {
        return ConverterPathResolver.outputBaseForYaml(request.getInputPath(), source, request.getOutputPath());
    }

    @Override
    public List<Path> outputPaths(TestDataContainer container, Path outputBase) {
        List<Path> paths = new ArrayList<>();
        for (TestDataSection section : container.getSections()) {
            paths.add(outputBase.resolve(section.getName() + YAML_EXTENSION));
        }
        return paths;
    }

    /**
     * ディレクトリ直下の YAML ファイルを辞書順で列挙する。
     *
     * @param yamlDir YAML コンテナディレクトリ
     * @return YAML ファイルのリスト（ファイル名辞書順）
     */
    private static List<Path> listYamlFiles(Path yamlDir) {
        TreeMap<String, Path> byName = new TreeMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(yamlDir)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)
                        && entry.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(YAML_EXTENSION)) {
                    byName.put(entry.getFileName().toString(), entry);
                }
            }
        } catch (IOException e) {
            // Files.newDirectoryStream が宣言するチェック例外。呼び出し元は IOException を受け取れないため UncheckedIOException でラップする。
            throw new UncheckedIOException("failed to list YAML files: " + yamlDir, e);
        }
        return new ArrayList<>(byName.values());
    }
}
