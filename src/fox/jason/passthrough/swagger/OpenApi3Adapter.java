package fox.jason.passthrough.swagger;

import fox.jason.passthrough.swagger.ApiModel.ApiDoc;
import fox.jason.passthrough.swagger.ApiModel.Definition;
import fox.jason.passthrough.swagger.ApiModel.HeaderInfo;
import fox.jason.passthrough.swagger.ApiModel.Operation;
import fox.jason.passthrough.swagger.ApiModel.Param;
import fox.jason.passthrough.swagger.ApiModel.Prop;
import fox.jason.passthrough.swagger.ApiModel.Resp;
import fox.jason.passthrough.swagger.ApiModel.SecurityScheme;
import fox.jason.passthrough.swagger.ApiModel.TagInfo;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.net.URI;
import java.util.Map;

/** Converts a parsed OpenAPI 3.x document into the version-neutral {@link ApiModel}. */
final class OpenApi3Adapter {

  private OpenApi3Adapter() {}

  static ApiDoc convert(OpenAPI api) {
    ApiDoc doc = new ApiDoc();

    Info info = api.getInfo();
    if (info != null) {
      doc.title = info.getTitle();
      doc.version = info.getVersion();
      doc.description = info.getDescription();
      if (info.getContact() != null) {
        doc.contactName = info.getContact().getName();
        doc.contactEmail = info.getContact().getEmail();
      }
      if (info.getLicense() != null) {
        doc.licenseName = info.getLicense().getName();
        doc.licenseUrl = info.getLicense().getUrl();
      }
    }
    if (api.getExternalDocs() != null) {
      doc.externalDocsDescription = api.getExternalDocs().getDescription();
      doc.externalDocsUrl = api.getExternalDocs().getUrl();
    }

    if (api.getServers() != null && !api.getServers().isEmpty()) {
      Server server = api.getServers().get(0);
      try {
        URI uri = URI.create(server.getUrl());
        doc.host = uri.getHost();
        doc.basePath = uri.getPath();
        if (uri.getScheme() != null) {
          doc.schemes.add(uri.getScheme());
        }
      } catch (IllegalArgumentException e) {
        doc.host = server.getUrl();
      }
    }

    if (api.getTags() != null) {
      for (Tag tag : api.getTags()) {
        TagInfo info1 = new TagInfo();
        info1.name = tag.getName();
        info1.description = tag.getDescription();
        doc.tags.add(info1);
      }
    }

    Components components = api.getComponents();
    if (components != null && components.getSecuritySchemes() != null) {
      components
          .getSecuritySchemes()
          .forEach(
              (name, scheme) -> {
                SecurityScheme s = new SecurityScheme();
                s.name = name;
                s.type = scheme.getType() == null ? null : scheme.getType().toString();
                s.description = scheme.getDescription();
                doc.securitySchemes.add(s);
              });
    }

    Paths paths = api.getPaths();
    if (paths != null) {
      for (Map.Entry<String, PathItem> pathEntry : paths.entrySet()) {
        PathItem item = pathEntry.getValue();
        addOperation(doc, pathEntry.getKey(), "GET", item.getGet());
        addOperation(doc, pathEntry.getKey(), "PUT", item.getPut());
        addOperation(doc, pathEntry.getKey(), "POST", item.getPost());
        addOperation(doc, pathEntry.getKey(), "DELETE", item.getDelete());
        addOperation(doc, pathEntry.getKey(), "OPTIONS", item.getOptions());
        addOperation(doc, pathEntry.getKey(), "HEAD", item.getHead());
        addOperation(doc, pathEntry.getKey(), "PATCH", item.getPatch());
      }
    }

    if (components != null && components.getSchemas() != null) {
      components
          .getSchemas()
          .forEach((name, schema) -> doc.definitions.add(convertDefinition(name, schema)));
    }

    return doc;
  }

  private static void addOperation(
      ApiDoc doc, String path, String method, io.swagger.v3.oas.models.Operation operation) {
    if (operation == null) {
      return;
    }
    Operation op = new Operation();
    op.path = path;
    op.method = method;
    op.operationId = operation.getOperationId();
    op.summary = operation.getSummary();
    op.description = operation.getDescription();
    if (operation.getTags() != null) {
      op.tags.addAll(operation.getTags());
    }
    if (operation.getParameters() != null) {
      for (Parameter parameter : operation.getParameters()) {
        op.parameters.add(convertParameter(parameter));
      }
    }

    RequestBody body = operation.getRequestBody();
    if (body != null && body.getContent() != null) {
      Map.Entry<String, MediaType> entry = preferredMediaType(body.getContent());
      if (entry != null) {
        Param param = new Param();
        param.in = "body";
        param.name = "body";
        param.required = Boolean.TRUE.equals(body.getRequired());
        Schema<?> requestSchema = entry.getValue().getSchema();
        param.description =
            body.getDescription() != null && !body.getDescription().isEmpty()
                ? body.getDescription()
                : requestSchema != null ? requestSchema.getDescription() : null;
        applySchemaType(param, requestSchema);
        op.parameters.add(param);
      }
    }

    if (operation.getResponses() != null) {
      operation.getResponses().forEach((code, resp) -> op.responses.add(convertResponse(code, resp)));
    }
    op.produces.addAll(mediaTypesOf(operation));
    doc.operations.add(op);
  }

  private static java.util.List<String> mediaTypesOf(io.swagger.v3.oas.models.Operation operation) {
    java.util.List<String> types = new java.util.ArrayList<>();
    if (operation.getResponses() == null) {
      return types;
    }
    for (ApiResponse response : operation.getResponses().values()) {
      if (response.getContent() != null) {
        for (String mime : response.getContent().keySet()) {
          if (!types.contains(mime)) {
            types.add(mime);
          }
        }
      }
    }
    return types;
  }

  private static Param convertParameter(Parameter parameter) {
    Param param = new Param();
    param.in = parameter.getIn();
    param.name = parameter.getName();
    param.required = Boolean.TRUE.equals(parameter.getRequired());
    param.description = parameter.getDescription();
    applySchemaType(param, parameter.getSchema());
    return param;
  }

  private static Resp convertResponse(String code, ApiResponse response) {
    Resp resp = new Resp();
    resp.code = code;
    resp.description = response.getDescription();
    if (response.getHeaders() != null) {
      response
          .getHeaders()
          .forEach(
              (name, header) -> {
                HeaderInfo info = new HeaderInfo();
                info.name = name;
                info.type = formatType(schemaType(header));
                resp.headers.add(info);
              });
    }
    if (response.getContent() != null) {
      Map.Entry<String, MediaType> entry = preferredMediaType(response.getContent());
      if (entry != null) {
        Schema<?> schema = entry.getValue().getSchema();
        applySchemaType(resp, schema);
        Object example = entry.getValue().getExample();
        if (example != null) {
          resp.example = String.valueOf(example);
          resp.exampleLanguage = mimeToLanguage(entry.getKey());
        }
      }
    }
    return resp;
  }

  private static Definition convertDefinition(String name, Schema<?> schema) {
    Definition definition = new Definition();
    definition.name = name;
    definition.description = schema.getDescription();

    if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
      definition.isEnum = true;
      for (Object value : schema.getEnum()) {
        definition.enumValues.add(String.valueOf(value));
      }
      return definition;
    }

    if (schema.getProperties() != null) {
      java.util.List<String> required =
          schema.getRequired() == null ? java.util.Collections.emptyList() : schema.getRequired();
      schema
          .getProperties()
          .forEach(
              (propName, propSchema) ->
                  definition.properties.add(
                      convertProperty(propName, propSchema, required.contains(propName))));
    }
    return definition;
  }

  private static Prop convertProperty(String name, Schema<?> schema, boolean required) {
    Prop prop = new Prop();
    prop.name = name;
    prop.required = required;
    prop.description = schema.getDescription();
    if (schema.getExample() != null) {
      prop.example = String.valueOf(schema.getExample());
    }
    applySchemaType(prop, schema);
    return prop;
  }

  private static void applySchemaType(Param param, Schema<?> schema) {
    SchemaType type = schemaType(schema);
    param.schemaRef = type.ref;
    param.schemaIsArray = type.isArray;
    param.type = type.text;
  }

  private static void applySchemaType(Resp resp, Schema<?> schema) {
    SchemaType type = schemaType(schema);
    resp.schemaRef = type.ref;
    resp.schemaIsArray = type.isArray;
    resp.type = type.text;
  }

  private static void applySchemaType(Prop prop, Schema<?> schema) {
    SchemaType type = schemaType(schema);
    prop.schemaRef = type.ref;
    prop.schemaIsArray = type.isArray;
    prop.type = type.text;
  }

  private static final class SchemaType {
    String ref;
    boolean isArray;
    String text;
  }

  private static SchemaType schemaType(Schema<?> schema) {
    SchemaType type = new SchemaType();
    if (schema == null) {
      return type;
    }
    if (schema.get$ref() != null) {
      type.ref = refName(schema.get$ref());
      return type;
    }
    if ("array".equals(schema.getType())) {
      type.isArray = true;
      Schema<?> items = itemsOf(schema);
      if (items != null) {
        if (items.get$ref() != null) {
          type.ref = refName(items.get$ref());
        } else {
          type.text = formatType(items.getType(), items.getFormat());
        }
      }
      return type;
    }
    type.text = formatType(schema.getType(), schema.getFormat());
    return type;
  }

  private static Schema<?> itemsOf(Schema<?> schema) {
    if (schema instanceof io.swagger.v3.oas.models.media.ArraySchema) {
      return ((io.swagger.v3.oas.models.media.ArraySchema) schema).getItems();
    }
    return null;
  }

  private static String schemaType(Header header) {
    Schema<?> schema = header.getSchema();
    return schema == null ? null : formatType(schema.getType(), schema.getFormat());
  }

  private static String formatType(String preformatted) {
    return preformatted;
  }

  private static String formatType(String type, String format) {
    if (type == null) {
      return "object";
    }
    return format == null ? type : type + " (" + format + ")";
  }

  private static Map.Entry<String, MediaType> preferredMediaType(Content content) {
    if (content == null || content.isEmpty()) {
      return null;
    }
    if (content.containsKey("application/json")) {
      return new java.util.AbstractMap.SimpleEntry<>(
          "application/json", content.get("application/json"));
    }
    return content.entrySet().iterator().next();
  }

  private static String refName(String ref) {
    int slash = ref.lastIndexOf('/');
    return slash < 0 ? ref : ref.substring(slash + 1);
  }

  private static String mimeToLanguage(String mimeType) {
    if (mimeType == null) {
      return "text";
    }
    int slash = mimeType.lastIndexOf('/');
    String subtype = slash < 0 ? mimeType : mimeType.substring(slash + 1);
    int plus = subtype.indexOf('+');
    return plus < 0 ? subtype : subtype.substring(plus + 1);
  }
}
