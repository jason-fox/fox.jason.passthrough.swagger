package fox.jason.passthrough.swagger;

import fox.jason.passthrough.swagger.ApiModel.ApiDoc;
import fox.jason.passthrough.swagger.ApiModel.Definition;
import fox.jason.passthrough.swagger.ApiModel.HeaderInfo;
import fox.jason.passthrough.swagger.ApiModel.Operation;
import fox.jason.passthrough.swagger.ApiModel.Param;
import fox.jason.passthrough.swagger.ApiModel.Prop;
import fox.jason.passthrough.swagger.ApiModel.Resp;
import fox.jason.passthrough.swagger.ApiModel.SecurityScheme;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Walks a version-neutral {@link ApiModel.ApiDoc} and renders it as a single DITA topic tree.
 *
 * <p>The output carries no DOCTYPE, so every element must spell out its own DITA {@code class}
 * generalization attribute explicitly - DITA-OT's pipeline dispatches on that attribute, not on
 * the element name, and has nothing to default it from without a DTD.
 */
final class DitaEmitter {

  private static final String TOPIC_DOMAINS =
      "(topic abbrev-d) a(props deliveryTarget) (topic equation-d) (topic hazard-d) "
          + "(topic hi-d) (topic indexing-d) (topic markup-d) (topic mathml-d) (topic pr-d) "
          + "(topic relmgmt-d) (topic sw-d) (topic svg-d) (topic ui-d) (topic ut-d) "
          + "(topic markup-d xml-d)";

  private final StringBuilder out = new StringBuilder();
  private final Slugs slugs = new Slugs();
  private final Map<String, String> definitionIds = new HashMap<>();

  static String render(ApiDoc doc, String fallbackTitle) {
    return new DitaEmitter().emit(doc, fallbackTitle);
  }

  private String emit(ApiDoc doc, String fallbackTitle) {
    for (Definition definition : doc.definitions) {
      definitionIds.put(definition.name, slugs.slugify(definition.name));
    }

    String title = isBlank(doc.title) ? fallbackTitle : doc.title;
    openTopic(slugs.slugify(title), "swagger", title);
    out.append("<body class=\"- topic/body \"></body>\n");

    emitOverview(doc);
    emitPaths(doc);
    emitDefinitions(doc);
    emitSecurity(doc);

    out.append("</topic>\n");
    return out.toString();
  }

  private void emitOverview(ApiDoc doc) {
    openTopic(slugs.slugify("Overview"), null, "Overview");
    out.append("<body class=\"- topic/body \">\n");
    if (!isBlank(doc.description)) {
      out.append("<p class=\"- topic/p \">").append(esc(collapse(doc.description))).append("</p>\n");
    }

    startSection("Version information");
    out.append("<p class=\"- topic/p \"><i class=\"+ topic/ph hi-d/i \">Version</i>: ")
        .append(esc(nullToUnknown(doc.version)))
        .append("</p>\n");
    endSection();

    if (!isBlank(doc.contactName) || !isBlank(doc.contactEmail)) {
      startSection("Contact information");
      out.append("<p class=\"- topic/p \">");
      if (!isBlank(doc.contactName)) {
        out.append("<i class=\"+ topic/ph hi-d/i \">Contact</i>: ").append(esc(doc.contactName)).append(' ');
      }
      if (!isBlank(doc.contactEmail)) {
        out.append("<i class=\"+ topic/ph hi-d/i \">Contact Email</i>: ").append(esc(doc.contactEmail));
      }
      out.append("</p>\n");
      endSection();
    }

    if (!isBlank(doc.licenseName)) {
      startSection("License information");
      out.append("<p class=\"- topic/p \"><i class=\"+ topic/ph hi-d/i \">License</i>: ")
          .append(esc(doc.licenseName));
      if (!isBlank(doc.licenseUrl)) {
        out.append(' ')
            .append("<i class=\"+ topic/ph hi-d/i \">License URL</i>: ")
            .append(esc(doc.licenseUrl));
      }
      out.append("</p>\n");
      endSection();
    }

    if (!isBlank(doc.host) || !isBlank(doc.basePath) || !doc.schemes.isEmpty()) {
      startSection("URI scheme");
      out.append("<p class=\"- topic/p \">");
      if (!isBlank(doc.host)) {
        out.append("<i class=\"+ topic/ph hi-d/i \">Host</i>: ").append(esc(doc.host)).append(' ');
      }
      if (!isBlank(doc.basePath)) {
        out.append("<i class=\"+ topic/ph hi-d/i \">BasePath</i>: ")
            .append(esc(doc.basePath))
            .append(' ');
      }
      if (!doc.schemes.isEmpty()) {
        out.append("<i class=\"+ topic/ph hi-d/i \">Schemes</i>: ")
            .append(esc(String.join(", ", upper(doc.schemes))));
      }
      out.append("</p>\n");
      endSection();
    }

    if (!doc.tags.isEmpty()) {
      startSection("Tags");
      out.append("<ul class=\"- topic/ul \">\n");
      for (ApiModel.TagInfo tag : doc.tags) {
        out.append("<li class=\"- topic/li \">").append(esc(tag.name));
        if (!isBlank(tag.description)) {
          out.append(" : ").append(esc(tag.description));
        }
        out.append("</li>\n");
      }
      out.append("</ul>\n");
      endSection();
    }

    emitMimeSection("Consumes", doc.consumes);
    emitMimeSection("Produces", doc.produces);

    if (!isBlank(doc.externalDocsDescription) || !isBlank(doc.externalDocsUrl)) {
      startSection("External Docs");
      out.append("<p class=\"- topic/p \">");
      if (!isBlank(doc.externalDocsDescription)) {
        out.append("<i class=\"+ topic/ph hi-d/i \">Description</i>: ")
            .append(esc(doc.externalDocsDescription))
            .append(' ');
      }
      if (!isBlank(doc.externalDocsUrl)) {
        out.append("<i class=\"+ topic/ph hi-d/i \">URL</i>: ").append(esc(doc.externalDocsUrl));
      }
      out.append("</p>\n");
      endSection();
    }

    out.append("</body>\n");
    out.append("</topic>\n");
  }

  private void emitPaths(ApiDoc doc) {
    openTopic(slugs.slugify("Paths"), null, "Paths");
    out.append("<body class=\"- topic/body \"></body>\n");

    for (Operation operation : doc.operations) {
      emitOperation(operation);
    }

    out.append("</topic>\n");
  }

  private void emitOperation(Operation operation) {
    String method = operation.method.toLowerCase();
    String title = !isBlank(operation.summary) ? operation.summary : operation.operationId;
    String idSource =
        !isBlank(operation.operationId) ? operation.operationId : method + " " + operation.path;

    openTopic(slugs.slugify(idSource), "swagger-" + method, title);
    out.append("<body class=\"- topic/body \">\n");
    out.append("<codeblock class=\"+ topic/pre pr-d/codeblock \" outputclass=\"swagger-")
        .append(method)
        .append("\">")
        .append(esc(operation.method.toUpperCase()))
        .append(' ')
        .append(esc(operation.path))
        .append("</codeblock>\n");

    if (!isBlank(operation.description)) {
      startSection("Description");
      out.append("<p class=\"- topic/p \">")
          .append(esc(collapse(operation.description)))
          .append("</p>\n");
      endSection();
    }

    if (!operation.parameters.isEmpty()) {
      startSection("Parameters");
      emitParameterTable(operation.parameters);
      endSection();
    }

    if (!operation.responses.isEmpty()) {
      startSection("Responses");
      emitResponseTable(operation.responses);
      endSection();
    }

    emitMimeSection("Consumes", operation.consumes);
    emitMimeSection("Produces", operation.produces);

    if (!operation.tags.isEmpty()) {
      startSection("Tags");
      out.append("<ul class=\"- topic/ul \">\n");
      for (String tag : operation.tags) {
        out.append("<li class=\"- topic/li \">").append(esc(tag)).append("</li>\n");
      }
      out.append("</ul>\n");
      endSection();
    }

    for (Resp response : operation.responses) {
      if (!isBlank(response.example)) {
        out.append("<example class=\"- topic/example \" id=\"")
            .append(slugs.slugify("Response " + response.code))
            .append("\" outputclass=\"example\">\n");
        out.append("<title class=\"- topic/title \">Response ")
            .append(esc(response.code))
            .append("</title>\n");
        out.append("<codeblock class=\"+ topic/pre pr-d/codeblock \" outputclass=\"")
            .append(esc(response.exampleLanguage == null ? "text" : response.exampleLanguage))
            .append("\">")
            .append(esc(response.example))
            .append("</codeblock>\n");
        out.append("</example>\n");
      }
    }

    out.append("</body>\n");
    out.append("</topic>\n");
  }

  private void emitParameterTable(List<Param> parameters) {
    startTable(4, "Type", "Name", "Description", "Schema");
    for (Param param : parameters) {
      out.append("<row class=\"- topic/row \">\n");
      entry(capitalize(param.in));
      entry(
          "<lines class=\"- topic/lines \"><b class=\"+ topic/ph hi-d/b \">"
              + esc(param.name)
              + "</b>\n<i class=\"+ topic/ph hi-d/i \">"
              + (param.required ? "required" : "optional")
              + "</i></lines>");
      entry(esc(nullToEmpty(param.description)));
      entry(schemaCell(param.schemaRef, param.schemaIsArray, param.type));
      out.append("</row>\n");
    }
    endTable();
  }

  private void emitResponseTable(List<Resp> responses) {
    startTable(3, "HTTP Code", "Description", "Schema");
    for (Resp resp : responses) {
      out.append("<row class=\"- topic/row \">\n");
      entry("<b class=\"+ topic/ph hi-d/b \">" + esc(resp.code) + "</b>");
      StringBuilder description = new StringBuilder(esc(nullToEmpty(resp.description)));
      if (!resp.headers.isEmpty()) {
        for (HeaderInfo header : resp.headers) {
          description
              .append("\n<b class=\"+ topic/ph hi-d/b \">Headers</b>: <codeph class=\"+ topic/ph pr-d/codeph \">")
              .append(esc(header.name))
              .append("</codeph> (")
              .append(esc(header.type))
              .append(')');
        }
        entry("<lines class=\"- topic/lines \">" + description + "</lines>");
      } else {
        entry(description.toString());
      }
      entry(schemaCell(resp.schemaRef, resp.schemaIsArray, resp.type));
      out.append("</row>\n");
    }
    endTable();
  }

  private String schemaCell(String ref, boolean isArray, String type) {
    if (ref != null) {
      String xref =
          "<xref class=\"- topic/xref \" href=\"#"
              + definitionIds.getOrDefault(ref, slugs.slugify(ref))
              + "\" type=\"topic\">"
              + esc(ref)
              + "</xref>";
      return isArray ? "Array of " + xref : xref;
    }
    if (type == null) {
      return "No Content";
    }
    return isArray ? "Array of " + esc(type) : esc(type);
  }

  private void emitDefinitions(ApiDoc doc) {
    if (doc.definitions.isEmpty()) {
      return;
    }
    openTopic(slugs.slugify("Definitions"), null, "Definitions");
    out.append("<body class=\"- topic/body \"></body>\n");

    for (Definition definition : doc.definitions) {
      openTopic(definitionIds.get(definition.name), null, definition.name);
      out.append("<body class=\"- topic/body \">\n");
      if (!isBlank(definition.description)) {
        out.append("<p class=\"- topic/p \">")
            .append(esc(collapse(definition.description)))
            .append("</p>\n");
      }
      if (definition.isEnum) {
        out.append("<p class=\"- topic/p \"><i class=\"+ topic/ph hi-d/i \">Type</i>: enum (")
            .append(esc(String.join(", ", definition.enumValues)))
            .append(")</p>\n");
      } else if (!definition.properties.isEmpty()) {
        emitPropertyTable(definition.properties);
      }
      out.append("</body>\n");
      out.append("</topic>\n");
    }

    out.append("</topic>\n");
  }

  private void emitPropertyTable(List<Prop> properties) {
    startTable(3, "Name", "Description", "Schema");
    for (Prop prop : properties) {
      out.append("<row class=\"- topic/row \">\n");
      entry(
          "<lines class=\"- topic/lines \"><b class=\"+ topic/ph hi-d/b \">"
              + esc(prop.name)
              + "</b>\n<i class=\"+ topic/ph hi-d/i \">"
              + (prop.required ? "required" : "optional")
              + "</i></lines>");
      StringBuilder description = new StringBuilder(esc(nullToEmpty(prop.description)));
      if (isBlank(prop.example)) {
        entry(description.toString());
      } else {
        description
            .append("\n<b class=\"+ topic/ph hi-d/b \">Example</b>: <codeph class=\"+ topic/ph pr-d/codeph \">")
            .append(esc(prop.example))
            .append("</codeph>");
        entry("<lines class=\"- topic/lines \">" + description + "</lines>");
      }
      entry(schemaCell(prop.schemaRef, prop.schemaIsArray, prop.type));
      out.append("</row>\n");
    }
    endTable();
  }

  private void emitSecurity(ApiDoc doc) {
    if (doc.securitySchemes.isEmpty()) {
      return;
    }
    openTopic(slugs.slugify("Security"), null, "Security");
    out.append("<body class=\"- topic/body \">\n<ul class=\"- topic/ul \">\n");
    for (SecurityScheme scheme : doc.securitySchemes) {
      out.append("<li class=\"- topic/li \"><b class=\"+ topic/ph hi-d/b \">")
          .append(esc(scheme.name))
          .append("</b> (")
          .append(esc(scheme.type))
          .append(')');
      if (!isBlank(scheme.description)) {
        out.append(" : ").append(esc(scheme.description));
      }
      out.append("</li>\n");
    }
    out.append("</ul>\n</body>\n");
    out.append("</topic>\n");
  }

  private void emitMimeSection(String heading, List<String> mimeTypes) {
    if (mimeTypes.isEmpty()) {
      return;
    }
    startSection(heading);
    out.append("<ul class=\"- topic/ul \">\n");
    for (String mime : mimeTypes) {
      out.append("<li class=\"- topic/li \"><codeph class=\"+ topic/ph pr-d/codeph \">")
          .append(esc(mime))
          .append("</codeph></li>\n");
    }
    out.append("</ul>\n");
    endSection();
  }

  private void openTopic(String id, String outputclass, String title) {
    out.append("<topic class=\"- topic/topic \" domains=\"")
        .append(TOPIC_DOMAINS)
        .append("\" id=\"")
        .append(id)
        .append('"');
    if (outputclass != null) {
      out.append(" outputclass=\"").append(outputclass).append('"');
    }
    out.append(">\n");
    out.append("<title class=\"- topic/title \">").append(esc(title)).append("</title>\n");
  }

  private void startSection(String heading) {
    out.append("<section class=\"- topic/section \" id=\"")
        .append(slugs.slugify(heading))
        .append("\" outputclass=\"section\">\n");
    out.append("<title class=\"- topic/title \">").append(esc(heading)).append("</title>\n");
  }

  private void endSection() {
    out.append("</section>\n");
  }

  private int tableCols = 1;

  private void startTable(int cols, String... headers) {
    tableCols = cols;
    entryColumn = 1;
    out.append("<table class=\"- topic/table \">\n<tgroup class=\"- topic/tgroup \" cols=\"")
        .append(cols)
        .append("\">\n");
    for (int i = 1; i <= cols; i++) {
      out.append("<colspec class=\"- topic/colspec \" colname=\"c")
          .append(i)
          .append("\" colnum=\"")
          .append(i)
          .append("\"/>\n");
    }
    out.append("<thead class=\"- topic/thead \"><row class=\"- topic/row \">");
    for (int i = 0; i < headers.length; i++) {
      out.append("<entry class=\"- topic/entry \" colname=\"c")
          .append(i + 1)
          .append("\">")
          .append(headers[i])
          .append("</entry>");
    }
    out.append("</row></thead>\n<tbody class=\"- topic/tbody \">\n");
  }

  private void endTable() {
    out.append("</tbody>\n</tgroup>\n</table>\n");
  }

  private int entryColumn = 1;

  private void entry(String content) {
    out.append("<entry class=\"- topic/entry \" colname=\"c")
        .append(entryColumn)
        .append("\">")
        .append(content)
        .append("</entry>\n");
    entryColumn++;
    if (entryColumn > tableCols) {
      entryColumn = 1;
    }
  }

  private static List<String> upper(List<String> values) {
    List<String> result = new java.util.ArrayList<>();
    for (String value : values) {
      result.add(value.toUpperCase());
    }
    return result;
  }

  private static String capitalize(String value) {
    if (isBlank(value)) {
      return "";
    }
    return Character.toUpperCase(value.charAt(0)) + value.substring(1);
  }

  private static String collapse(String text) {
    return text.trim().replaceAll("\\s*\\n\\s*", " ");
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private static String nullToUnknown(String value) {
    return isBlank(value) ? "Unknown" : value;
  }

  private static String esc(String value) {
    if (value == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder(value.length());
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '&':
          sb.append("&amp;");
          break;
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }
}
