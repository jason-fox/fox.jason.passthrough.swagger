package fox.jason.passthrough;

import fox.jason.passthrough.swagger.ApiDocumentConverter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class SwaggerFileReader extends AbstractFileReader {

  public SwaggerFileReader() {}

  @Override
  protected String runTarget(File inputFile, String title) throws IOException {
    return ApiDocumentConverter.convertToDita(inputFile, title, null);
  }

  @Override
  protected String runTarget(File inputFile, String title, URI sourceUri) throws IOException {
    Path source = Paths.get(sourceUri);
    String specFileName = astSpecFileName(source.getFileName().toString());

    // DITA-OT's object/@data copy-through (GenListModuleReader#parseObject) requires a real file
    // distinct from the topicref's own href - pointing @data at the original file collides with
    // its "format=swagger" role in the job model and silently reverts to a raw copy instead of
    // the converted topic, so the raw spec is duplicated here under a name of its own.
    Files.copy(
        inputFile.toPath(), source.resolveSibling(specFileName), StandardCopyOption.REPLACE_EXISTING);

    return ApiDocumentConverter.convertToDita(inputFile, title, specFileName);
  }

  private static String astSpecFileName(String originalFileName) {
    int dot = originalFileName.lastIndexOf('.');
    return dot < 0
        ? originalFileName + ".ast"
        : originalFileName.substring(0, dot) + ".ast" + originalFileName.substring(dot);
  }
}
