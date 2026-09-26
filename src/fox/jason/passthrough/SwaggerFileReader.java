package fox.jason.passthrough;

import fox.jason.passthrough.swagger.ApiDocumentConverter;
import java.io.File;
import java.io.IOException;

public class SwaggerFileReader extends AbstractFileReader {

  public SwaggerFileReader() {}

  @Override
  protected String runTarget(File inputFile, String title) throws IOException {
    return ApiDocumentConverter.convertToDita(inputFile, title);
  }
}
