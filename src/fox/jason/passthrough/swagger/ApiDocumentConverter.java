package fox.jason.passthrough.swagger;

import io.swagger.parser.SwaggerParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

/** Entry point: parses a Swagger 2.0 or OpenAPI 3.x file and renders it straight to DITA. */
public final class ApiDocumentConverter {

  private static final Pattern OPENAPI_3 =
      Pattern.compile("(?m)^\\s*[\"']?openapi[\"']?\\s*:\\s*[\"']?3");

  private ApiDocumentConverter() {}

  public static String convertToDita(File inputFile, String fallbackTitle, String sourceFileName)
      throws IOException {
    String content = new String(Files.readAllBytes(inputFile.toPath()), StandardCharsets.UTF_8);

    ApiModel.ApiDoc doc =
        OPENAPI_3.matcher(content).find()
            ? OpenApi3Adapter.convert(parseOpenApi3(content))
            : Swagger2Adapter.convert(new SwaggerParser().parse(content));

    return DitaEmitter.render(doc, fallbackTitle, sourceFileName);
  }

  private static OpenAPI parseOpenApi3(String content) throws IOException {
    SwaggerParseResult result = new OpenAPIV3Parser().readContents(content);
    if (result.getOpenAPI() == null) {
      throw new IOException(
          "Unable to parse OpenAPI document: " + String.join("; ", result.getMessages()));
    }
    return result.getOpenAPI();
  }
}
