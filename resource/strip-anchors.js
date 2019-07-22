/*
 *  This file is part of the DITA-OT Swagger DITA Plug-in project.
 *  See the accompanying LICENSE file for applicable licenses.
 */

//
//    Entry point to the JavaScript tidier function which
//    removes HTML anchor tags and adds ids to headers
//
//    @param file    - The Swagger Markdown file to clean up
//

var file = attributes.get("file");
// Analyse the dita and prettify it.
function moveAnchors(markdown) {
  var lines = markdown.split("\n");
  var text = [];
  var id = null;

  // Read each line of the Markdown file in turn
  for (var i = 1; i < lines.length; i++) {
    if (lines[i].startsWith("<a name")) {
      var start = lines[i].indexOf('"') + 1;
      var end = lines[i].indexOf('"', start);
      id = lines[i].substring(start, end);
      text.push("");
    } else if (id !== null && lines[i].startsWith("#")) {
      text.push(lines[i] + " {#" + id + "}");
      id = null;
    } else {
      text.push(lines[i]);
    }
  }
  return text.join("\n");
}

var swaggerText = org.apache.tools.ant.util.FileUtils.readFully(
  new java.io.FileReader(file)
);
var updatedText = moveAnchors(swaggerText);
var task = project.createTask("echo");
task.setFile(new java.io.File(file));
task.setMessage(updatedText);
task.perform();
