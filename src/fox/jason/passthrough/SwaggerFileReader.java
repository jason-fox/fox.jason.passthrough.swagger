package fox.jason.passthrough;

import java.io.File;
import java.io.IOException;

public class SwaggerFileReader extends PandocFileReader {

  public SwaggerFileReader() {}

  private static final String ANT_FILE = "/../process_swagger.xml";

  @Override
  protected String runTarget(File inputFile, String title) throws IOException {
    File markdownFile = File.createTempFile("swagger", "md");
    markdownFile.deleteOnExit();
    writeToFile(
      executeAntTask(
        calculateJarPath(SwaggerFileReader.class) + ANT_FILE,
        inputFile,
        title
      ),
      markdownFile
    );
    return executePandoc(markdownFile, title);
  }
}
