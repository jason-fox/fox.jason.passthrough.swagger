/*
 *  This file is part of the DITA-OT Passthrough Plug-in project.
 *  See the accompanying LICENSE file for applicable licenses.
 */

package fox.jason.passthrough.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.util.FileUtils;

//
//    Entry point to the JavaScript tidier function which
//    removes HTML anchor tags and adds ids to headers

public class FormatMarkdownTask extends Task {

  /**
   * Field file.
   */
  private String file;

  /**
   * Creates a new <code>FormatMarkdownTask</code> instance.
   */
  public FormatMarkdownTask() {
    super();
    this.file = null;
  }

  /**
   * Method setFile.
   *
   * @param file String
   */
  public void setFile(String file) {
    this.file = file;
  }

  private String moveAnchors(String markdown) {
    String[] lines = markdown.split("\\r?\\n|\\r");
    List<String> text = new ArrayList<>();
    String id = null;
    boolean codeblock = true;
    String swaggerVerb = "";
    String[] splitLine = null;
    boolean firstTopic = true;
    int index = 0;

    // Read each line of the Markdown file in turn
    for (String line : lines) {
      if (firstTopic && line.startsWith("# ")) {
        text.add(line + " {.swagger}");
        firstTopic = false;
      } else if (line.startsWith("<a name")) {
        int start = line.indexOf('"') + 1;
        int end = line.indexOf('"', start);
        id = line.substring(start, end);
        text.add("");
      } else if (id == null && line.startsWith("### ")) {
        text.add(line + " {.section}");
      } else if (id == null && line.startsWith("#### ")) {
        text.add("#" + line + " {.section}");
      } else if (id == null && line.startsWith("#####")) {
        text.add(line + " {.example}");
      } else if (id != null && line.startsWith("#")) {
        if ("```".equals(lines[index + 1])) {
          splitLine = lines[index + 2].split(" ");
          String verb = splitLine[0].toLowerCase();
          text.add(line + " {#" + id + " .swagger-" + verb + "}");
        } else {
          text.add(line + " {#" + id + "}");
        }
        id = null;
      } else if (line.startsWith("```")) {
        if (codeblock) {
          if ("```".equals(line)) {
            splitLine = lines[index + 1].split(" ");
            swaggerVerb = "swagger-" + splitLine[0].toLowerCase();
            text.add("```" + swaggerVerb);
          } else {
            text.add(line);
          }
          codeblock = false;
        } else {
          text.add(line);
          codeblock = true;
        }
      } else {
        text.add(line);
      }
      index++;
    }
    return String.join("\n", text);
  }

  /**
   * Method execute.
   *
   * @throws BuildException if something goes wrong
   */
  @Override
  public void execute() {
    //
    //    @param file    - The Swagger Markdown file to clean up
    //
    if (this.file == null) {
      throw new BuildException("You must supply a Swagger Markdown file");
    }

    try {
      String swaggerText = FileUtils.readFully(new java.io.FileReader(file));
      String updatedText = moveAnchors(swaggerText);

      Echo task = (Echo) getProject().createTask("echo");
      task.setFile(new java.io.File(file));
      task.setMessage(updatedText);
      task.perform();
    } catch (IOException e) {
      throw new BuildException("Unable to read file", e);
    }
  }
}
