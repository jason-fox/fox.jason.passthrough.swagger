package fox.jason.passthrough.swagger;

import java.util.ArrayList;
import java.util.List;

/**
 * Version-neutral representation of an API document, populated by
 * {@link Swagger2Adapter} or {@link OpenApi3Adapter} and rendered by
 * {@link DitaEmitter}.
 */
public final class ApiModel {

  private ApiModel() {}

  public static final class ApiDoc {
    public String title;
    public String version;
    public String description;
    public String contactName;
    public String contactEmail;
    public String licenseName;
    public String licenseUrl;
    public String externalDocsDescription;
    public String externalDocsUrl;
    public String host;
    public String basePath;
    public final List<String> schemes = new ArrayList<>();
    public final List<String> consumes = new ArrayList<>();
    public final List<String> produces = new ArrayList<>();
    public final List<TagInfo> tags = new ArrayList<>();
    public final List<Operation> operations = new ArrayList<>();
    public final List<Definition> definitions = new ArrayList<>();
    public final List<SecurityScheme> securitySchemes = new ArrayList<>();
  }

  public static final class TagInfo {
    public String name;
    public String description;
  }

  public static final class Operation {
    public String method;
    public String path;
    public String operationId;
    public String summary;
    public String description;
    public final List<String> consumes = new ArrayList<>();
    public final List<String> produces = new ArrayList<>();
    public final List<String> tags = new ArrayList<>();
    public final List<Param> parameters = new ArrayList<>();
    public final List<Resp> responses = new ArrayList<>();
  }

  public static final class Param {
    public String in;
    public String name;
    public boolean required;
    public String description;
    public String type;
    public String schemaRef;
    public boolean schemaIsArray;
  }

  public static final class Resp {
    public String code;
    public String description;
    public final List<HeaderInfo> headers = new ArrayList<>();
    public String type;
    public String schemaRef;
    public boolean schemaIsArray;
    public String example;
    public String exampleLanguage;
  }

  public static final class HeaderInfo {
    public String name;
    public String type;
  }

  public static final class Definition {
    public String name;
    public String description;
    public boolean isEnum;
    public final List<String> enumValues = new ArrayList<>();
    public final List<Prop> properties = new ArrayList<>();
  }

  public static final class Prop {
    public String name;
    public boolean required;
    public String description;
    public String example;
    public String type;
    public String schemaRef;
    public boolean schemaIsArray;
  }

  public static final class SecurityScheme {
    public String name;
    public String type;
    public String description;
  }
}
