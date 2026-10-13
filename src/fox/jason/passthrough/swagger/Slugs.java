package fox.jason.passthrough.swagger;

import java.util.HashMap;
import java.util.Map;

/**
 * Turns heading text into DITA element ids, de-duplicating repeats across the
 * whole document by appending "-1", "-2", ... (matches the anchor scheme readers
 * expect from Pandoc/GitHub-style heading slugs).
 */
final class Slugs {

  private final Map<String, Integer> seen = new HashMap<>();

  String slugify(String text) {
    String base = text == null ? "" : text.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    base = base.replaceAll("^-+|-+$", "");
    if (base.isEmpty()) {
      base = "id";
    }
    Integer count = seen.get(base);
    if (count == null) {
      seen.put(base, 0);
      return base;
    }
    int next = count + 1;
    seen.put(base, next);
    return base + "-" + next;
  }
}
