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
  var codeblock = true;
  var swaggerVerb = "";
  var firstTopic = true;

  // Read each line of the Markdown file in turn
  for (var i = 0; i < lines.length; i++) {
    if (firstTopic && lines[i].startsWith("# ")){
      text.push(lines[i] + " {.swagger}");
      firstTopic == false;
    } else if (lines[i].startsWith("<a name")) {
      var start = lines[i].indexOf('"') + 1;
      var end = lines[i].indexOf('"', start);
      id = lines[i].substring(start, end);
      text.push("");
    } else if (id === null && lines[i].startsWith("### ")) {
      text.push(lines[i] + " {.section}");
    } else if (id === null && lines[i].startsWith("#### ")) {
      text.push("#" + lines[i] + " {.section}");
    } else if (id === null && lines[i].startsWith("#####")) {
      text.push(lines[i] + " {.example}");
    } else if (id !== null && lines[i].startsWith("#")) {
      if(lines[i+1].equals("```")){
        var arr = lines[i+2].split(' ');
        var verb =  arr[0].toLowerCase();
        text.push(lines[i] + " {#" + id + " .swagger-" + verb +"}");
      } else { 
        text.push(lines[i] + " {#" + id + "}");
      }
      id = null;
    } else if (lines[i].startsWith("```")){
      if(codeblock){
        if(lines[i].equals("```")){
          var arr = lines[i+1].split(' ');
          swaggerVerb = "swagger-" + arr[0].toLowerCase();
          text.push("```" + swaggerVerb);
        } else {
          text.push(lines[i]);
        }
        codeblock = false;
      } else {
        text.push(lines[i]);
        codeblock = true;
      }
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
