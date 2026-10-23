# task-10c self-check

## Files changed/created
- `src/main/java/nablarch/test/tool/converter/XlsOutputConfig.java` — new POJO with @Parameter fields and toExcelFormatConfig()
- `src/main/java/nablarch/test/tool/converter/ConversionRequest.java` — added excelFormatConfig field, getter, Builder method
- `src/main/java/nablarch/test/tool/converter/FormatHandler.java` — createWriter() → createWriter(ConversionRequest request)
- `src/main/java/nablarch/test/tool/converter/XlsFormatHandler.java` — createWriter uses request.getExcelFormatConfig()
- `src/main/java/nablarch/test/tool/converter/YamlFormatHandler.java` — createWriter(request) signature updated
- `src/main/java/nablarch/test/tool/converter/TestDataConverter.java` — createWriter(request) call site updated
- `src/main/java/nablarch/test/tool/converter/ConverterMojo.java` — added @Parameter xlsOutput, applied in execute()
- `README.md` — updated Excel output section with Maven <configuration> example
- `src/test/java/nablarch/test/tool/converter/ConverterMojoTest.java` — added 2 xlsOutput tests
- `src/test/java/nablarch/test/tool/converter/XlsOutputConfigTest.java` — new: 3 tests

## Self-check results
- XlsOutputConfig created: OK
- ConversionRequest excelFormatConfig field/getter/builder: OK
- FormatHandler.createWriter(request) signature: OK
- XlsFormatHandler/YamlFormatHandler updated: OK
- TestDataConverter call site updated: OK
- ConverterMojo xlsOutput field + usage: OK
- README updated (removed "Maven プラグイン経由では既定値が使われます", added <xlsOutput> example): OK
- mvn clean test (303 tests): OK — 0 Failures, 0 Errors
- commit sha: 6da3ae1
- pushed: OK
