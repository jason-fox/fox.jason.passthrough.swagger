package fox.jason.passthrough.swagger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

// Resolves generated heading labels, falling back region -> base language -> English.
final class SwaggerLabels {

  private static final String DEFAULT_LANG = "en";
  private static final String RESOURCE_PATH =
      "/fox/jason/passthrough/swagger/labels/labels_%s.properties";

  private final Properties properties;

  private SwaggerLabels(Properties properties) {
    this.properties = properties;
  }

  static SwaggerLabels forLanguage(String lang) {
    String normalized =
        lang == null || lang.trim().isEmpty() ? DEFAULT_LANG : lang.trim().toLowerCase(Locale.ROOT);

    Properties resolved = load(normalized);
    if (resolved == null && normalized.contains("-")) {
      resolved = load(normalized.substring(0, normalized.indexOf('-')));
    }
    if (resolved == null) {
      resolved = load(DEFAULT_LANG);
    }
    return new SwaggerLabels(resolved);
  }

  private static Properties load(String lang) {
    String resource = String.format(RESOURCE_PATH, lang);
    try (InputStream in = SwaggerLabels.class.getResourceAsStream(resource)) {
      if (in == null) {
        return null;
      }
      Properties loaded = new Properties();
      loaded.load(new InputStreamReader(in, StandardCharsets.UTF_8));
      return loaded;
    } catch (IOException e) {
      return null;
    }
  }

  String get(String key) {
    return properties.getProperty(key, key);
  }
}
