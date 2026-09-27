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
import fox.jason.passthrough.markdown.MarkdownDita;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

  static String render(ApiDoc doc, String fallbackTitle, String sourceFileName) {
    return new DitaEmitter().emit(doc, fallbackTitle, sourceFileName);
  }

  private String emit(ApiDoc doc, String fallbackTitle, String sourceFileName) {
    for (Definition definition : doc.definitions) {
      definitionIds.put(definition.name, slugs.slugify(definition.name));
    }

    String title = isBlank(doc.title) ? fallbackTitle : doc.title;
    openTopic(slugs.slugify(title), "swagger", title);
    if (isBlank(sourceFileName)) {
      out.append("<body class=\"- topic/body \"></body>\n");
    } else {
      out.append("<body class=\"- topic/body \">\n");
      emitSpecObject(sourceFileName);
      out.append("</body>\n");
    }

    emitOverview(doc);
    emitPaths(doc);
    emitDefinitions(doc);
    emitSecurity(doc);

    out.append("</topic>\n");
    return out.toString();
  }

  // AST-bootstrap renders this as a single Scalar API reference component and ignores the
  // fallback; every other transtype falls through to <fallback>'s own content, which is nothing
  // more than the same overview/paths/definitions/security topics emitted below - html5-bootstrap
  // and PDF just need to know to render object/@data's non-<param> children, which html5 already
  // does by default.
  private void emitSpecObject(String sourceFileName) {
    String type = sourceFileName.toLowerCase().endsWith(".yaml")
            || sourceFileName.toLowerCase().endsWith(".yml")
        ? "application/yaml"
        : "application/json";
    out.append("<object class=\"- topic/object \" data=\"")
        .append(esc(sourceFileName))
        .append("\" type=\"")
        .append(type)
        .append("\" outputclass=\"swagger-spec\">\n");
    out.append("<fallback class=\"- topic/fallback \"/>\n");
    out.append("</object>\n");
  }

  private void emitOverview(ApiDoc doc) {
    openTopic(slugs.slugify("Overview"), null, "Overview");
    out.append("<body class=\"- topic/body \">\n");
    if (!isBlank(doc.description)) {
      out.append(MarkdownDita.renderBlocks(doc.description));
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

    // No top-level Tags catalog here: each tag already gets its own name/description directly
    // above its operations in emitPaths/emitTagGroup, so repeating the same list here up front is
    // the same kind of redundant listing already dropped from individual operations.

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

  // Groups operations by tag, in declared order, matching the swagger-ui/petstore.swagger.io
  // convention (https://petstore.swagger.io/#/): a topic per tag with its description, its
  // operations in spec order beneath it, an implicit "default" group for tagless operations
  // once any tag grouping exists, and a plain flat list when the spec defines no tags at all.
  // Tag-group topics (or the flat list) attach directly under the root topic - petstore-ui has
  // no separate "Paths" heading above its tag groups, so neither does this.
  private void emitPaths(ApiDoc doc) {
    Map<String, List<Operation>> byTag = new LinkedHashMap<>();
    for (TagInfo tag : doc.tags) {
      byTag.put(tag.name, new ArrayList<>());
    }
    List<Operation> untagged = new ArrayList<>();
    for (Operation operation : doc.operations) {
      if (operation.tags.isEmpty()) {
        untagged.add(operation);
        continue;
      }
      for (String tagName : operation.tags) {
        byTag.computeIfAbsent(tagName, key -> new ArrayList<>()).add(operation);
      }
    }

    if (byTag.isEmpty()) {
      for (Operation operation : doc.operations) {
        emitOperation(operation);
      }
    } else {
      for (Map.Entry<String, List<Operation>> entry : byTag.entrySet()) {
        emitTagGroup(entry.getKey(), tagDescription(doc, entry.getKey()), entry.getValue());
      }
      if (!untagged.isEmpty()) {
        emitTagGroup("default", null, untagged);
      }
    }
  }

  private void emitTagGroup(String name, String description, List<Operation> operations) {
    if (operations.isEmpty()) {
      return;
    }
    openTopic(slugs.slugify(name), null, name);
    out.append("<body class=\"- topic/body \">\n");
    if (!isBlank(description)) {
      out.append(MarkdownDita.renderBlocks(description));
    }
    out.append("</body>\n");
    for (Operation operation : operations) {
      emitOperation(operation);
    }
    out.append("</topic>\n");
  }

  private static String tagDescription(ApiDoc doc, String tagName) {
    for (TagInfo tag : doc.tags) {
      if (tag.name.equals(tagName)) {
        return tag.description;
      }
    }
    return null;
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
      out.append(MarkdownDita.renderBlocks(operation.description));
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

    // No per-operation tags list here: operations are already grouped by tag in emitPaths, so
    // repeating tag membership per operation is the redundant listing petstore-ui doesn't show.

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
      entry(MarkdownDita.renderInlineOnly(nullToEmpty(param.description)));
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
      StringBuilder description = new StringBuilder(MarkdownDita.renderInlineOnly(nullToEmpty(resp.description)));
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
        out.append(MarkdownDita.renderBlocks(definition.description));
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
      StringBuilder description = new StringBuilder(MarkdownDita.renderInlineOnly(nullToEmpty(prop.description)));
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
        out.append(" : ").append(MarkdownDita.renderInlineOnly(scheme.description));
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
