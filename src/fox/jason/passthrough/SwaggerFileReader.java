package fox.jason.passthrough;

import fox.jason.passthrough.swagger.ApiDocumentConverter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SwaggerFileReader extends AbstractFileReader {

  public SwaggerFileReader() {}

  @Override
  protected String runTarget(File inputFile, String title) throws IOException {
    return ApiDocumentConverter.convertToDita(inputFile, title, null, getDefaultLanguage());
  }

  @Override
  protected String runTarget(File inputFile, String title, URI sourceUri) throws IOException {
    Path source = Paths.get(sourceUri);
    String specFileName = "swagger/" + source.getFileName();

    return ApiDocumentConverter.convertToDita(inputFile, title, specFileName, getDefaultLanguage());
  }
}
