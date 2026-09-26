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
import io.swagger.models.ArrayModel;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import java.util.LinkedHashMap;
import java.util.Map;

/** Converts a parsed Swagger 2.0 document into the version-neutral {@link ApiModel}. */
final class Swagger2Adapter {

  private Swagger2Adapter() {}

  static ApiDoc convert(Swagger swagger) {
    ApiDoc doc = new ApiDoc();

    Info info = swagger.getInfo();
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
    if (swagger.getExternalDocs() != null) {
      doc.externalDocsDescription = swagger.getExternalDocs().getDescription();
      doc.externalDocsUrl = swagger.getExternalDocs().getUrl();
    }
    doc.host = swagger.getHost();
    doc.basePath = swagger.getBasePath();
    if (swagger.getSchemes() != null) {
      for (Scheme scheme : swagger.getSchemes()) {
        doc.schemes.add(scheme.toValue());
      }
    }
    if (swagger.getConsumes() != null) {
      doc.consumes.addAll(swagger.getConsumes());
    }
    if (swagger.getProduces() != null) {
      doc.produces.addAll(swagger.getProduces());
    }
    if (swagger.getTags() != null) {
      for (Tag tag : swagger.getTags()) {
        TagInfo info1 = new TagInfo();
        info1.name = tag.getName();
        info1.description = tag.getDescription();
        doc.tags.add(info1);
      }
    }

    if (swagger.getSecurityDefinitions() != null) {
      for (Map.Entry<String, SecuritySchemeDefinition> entry :
          swagger.getSecurityDefinitions().entrySet()) {
        SecurityScheme scheme = new SecurityScheme();
        scheme.name = entry.getKey();
        scheme.type = entry.getValue().getType();
        scheme.description = entry.getValue().getDescription();
        doc.securitySchemes.add(scheme);
      }
    }

    Map<String, Path> paths = swagger.getPaths();
    if (paths != null) {
      for (Map.Entry<String, Path> pathEntry : paths.entrySet()) {
        Path path = pathEntry.getValue();
        addOperation(doc, pathEntry.getKey(), "GET", path.getGet());
        addOperation(doc, pathEntry.getKey(), "PUT", path.getPut());
        addOperation(doc, pathEntry.getKey(), "POST", path.getPost());
        addOperation(doc, pathEntry.getKey(), "DELETE", path.getDelete());
        addOperation(doc, pathEntry.getKey(), "OPTIONS", path.getOptions());
        addOperation(doc, pathEntry.getKey(), "HEAD", path.getHead());
        addOperation(doc, pathEntry.getKey(), "PATCH", path.getPatch());
      }
    }

    if (swagger.getDefinitions() != null) {
      for (Map.Entry<String, Model> entry : swagger.getDefinitions().entrySet()) {
        doc.definitions.add(convertDefinition(entry.getKey(), entry.getValue()));
      }
    }

    return doc;
  }

  private static void addOperation(
      ApiDoc doc, String path, String method, io.swagger.models.Operation operation) {
    if (operation == null) {
      return;
    }
    Operation op = new Operation();
    op.path = path;
    op.method = method;
    op.operationId = operation.getOperationId();
    op.summary = operation.getSummary();
    op.description = operation.getDescription();
    if (operation.getConsumes() != null) {
      op.consumes.addAll(operation.getConsumes());
    }
    if (operation.getProduces() != null) {
      op.produces.addAll(operation.getProduces());
    }
    if (operation.getTags() != null) {
      op.tags.addAll(operation.getTags());
    }
    if (operation.getParameters() != null) {
      for (Parameter parameter : operation.getParameters()) {
        op.parameters.add(convertParameter(parameter));
      }
    }
    if (operation.getResponses() != null) {
      for (Map.Entry<String, Response> entry : operation.getResponses().entrySet()) {
        op.responses.add(convertResponse(entry.getKey(), entry.getValue()));
      }
    }
    doc.operations.add(op);
  }

  private static Param convertParameter(Parameter parameter) {
    Param param = new Param();
    param.in = parameter.getIn();
    param.name = parameter.getName();
    param.required = parameter.getRequired();
    param.description = parameter.getDescription();

    if (parameter instanceof BodyParameter) {
      Model schema = ((BodyParameter) parameter).getSchema();
      applyModelType(param, schema);
    } else if (parameter instanceof AbstractSerializableParameter) {
      AbstractSerializableParameter<?> serializable = (AbstractSerializableParameter<?>) parameter;
      param.type = formatType(serializable.getType(), serializable.getFormat());
    }
    return param;
  }

  private static void applyModelType(Param param, Model schema) {
    if (schema instanceof RefModel) {
      param.schemaRef = ((RefModel) schema).getSimpleRef();
    } else if (schema instanceof ArrayModel) {
      Property items = ((ArrayModel) schema).getItems();
      param.schemaIsArray = true;
      applyPropertyType(param, items);
    } else {
      param.type = "object";
    }
  }

  private static void applyPropertyType(Param param, Property property) {
    if (property instanceof RefProperty) {
      param.schemaRef = ((RefProperty) property).getSimpleRef();
    } else if (property != null) {
      param.type = formatType(property.getType(), property.getFormat());
    }
  }

  private static Resp convertResponse(String code, Response response) {
    Resp resp = new Resp();
    resp.code = code;
    resp.description = response.getDescription();
    if (response.getHeaders() != null) {
      for (Map.Entry<String, Property> entry : response.getHeaders().entrySet()) {
        HeaderInfo header = new HeaderInfo();
        header.name = entry.getKey();
        header.type = formatType(entry.getValue().getType(), entry.getValue().getFormat());
        resp.headers.add(header);
      }
    }
    Property schema = response.getSchema();
    if (schema instanceof RefProperty) {
      resp.schemaRef = ((RefProperty) schema).getSimpleRef();
    } else if (schema instanceof ArrayProperty) {
      resp.schemaIsArray = true;
      Property items = ((ArrayProperty) schema).getItems();
      if (items instanceof RefProperty) {
        resp.schemaRef = ((RefProperty) items).getSimpleRef();
      } else if (items != null) {
        resp.type = formatType(items.getType(), items.getFormat());
      }
    } else if (schema != null) {
      resp.type = formatType(schema.getType(), schema.getFormat());
    }
    if (response.getExamples() != null && !response.getExamples().isEmpty()) {
      Map.Entry<String, Object> example = response.getExamples().entrySet().iterator().next();
      resp.example = String.valueOf(example.getValue());
      resp.exampleLanguage = mimeToLanguage(example.getKey());
    }
    return resp;
  }

  private static Definition convertDefinition(String name, Model model) {
    Definition definition = new Definition();
    definition.name = name;
    definition.description = model.getDescription();

    if (model instanceof ModelImpl) {
      ModelImpl modelImpl = (ModelImpl) model;
      if (modelImpl.getEnum() != null && !modelImpl.getEnum().isEmpty()) {
        definition.isEnum = true;
        definition.enumValues.addAll(modelImpl.getEnum());
        return definition;
      }
    }

    Map<String, Property> properties = model.getProperties();
    if (properties == null) {
      properties = new LinkedHashMap<>();
    }
    for (Map.Entry<String, Property> entry : properties.entrySet()) {
      definition.properties.add(convertProperty(entry.getKey(), entry.getValue()));
    }
    return definition;
  }

  private static Prop convertProperty(String name, Property property) {
    Prop prop = new Prop();
    prop.name = name;
    prop.required = property.getRequired();
    prop.description = property.getDescription();
    if (property.getExample() != null) {
      prop.example = String.valueOf(property.getExample());
    }
    if (property instanceof RefProperty) {
      prop.schemaRef = ((RefProperty) property).getSimpleRef();
    } else if (property instanceof ArrayProperty) {
      prop.schemaIsArray = true;
      Property items = ((ArrayProperty) property).getItems();
      if (items instanceof RefProperty) {
        prop.schemaRef = ((RefProperty) items).getSimpleRef();
      } else if (items != null) {
        prop.type = formatType(items.getType(), items.getFormat());
      }
    } else {
      prop.type = formatType(property.getType(), property.getFormat());
    }
    return prop;
  }

  private static String formatType(String type, String format) {
    if (type == null) {
      return "object";
    }
    return format == null ? type : type + " (" + format + ")";
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
