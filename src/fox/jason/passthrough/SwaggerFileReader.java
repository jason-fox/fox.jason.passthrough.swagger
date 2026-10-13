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
    return ApiDocumentConverter.convertToDita(inputFile, title, null, getDefaultLanguage());
  }

  @Override
  protected String runTarget(File inputFile, String title, URI sourceUri) throws IOException {
    if (System.getProperty("passthrough.spec.keep") == null) {
      return runTarget(inputFile, title);
    }
    Path source = Paths.get(sourceUri);
    String specFileName = "swagger/" + source.getFileName();
    stageSpec(inputFile, source, specFileName);

    return ApiDocumentConverter.convertToDita(inputFile, title, specFileName, getDefaultLanguage());
  }

  // A job entry whose file is missing from the temp dir is reported as "not found" when
  // resources are copied, so the spec is staged there for DITA-OT to copy to the output.
  private static void stageSpec(File inputFile, Path source, String specFileName) throws IOException {
    String tempDir = System.getProperty("dita.temp.dir");
    String inputDir = System.getProperty("passthrough.input.dir");
    if (tempDir == null || inputDir == null) {
      return;
    }
    Path target =
        Paths.get(tempDir)
            .resolve(Paths.get(inputDir).relativize(source.getParent()))
            .resolve(specFileName);
    Files.createDirectories(target.getParent());
    Files.copy(inputFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
  }
}
