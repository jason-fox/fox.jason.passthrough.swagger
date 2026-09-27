# Swagger Plugin for DITA-OT [<img src="https://jason-fox.github.io/fox.jason.passthrough.swagger/swagger.png" align="right" width="300">](http://swaggerdita-ot.rtfd.io/)

[![license](https://img.shields.io/github/license/jason-fox/fox.jason.passthrough.swagger.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![DITA-OT 4.2](https://img.shields.io/badge/DITA--OT-4.2-green.svg)](http://www.dita-ot.org/4.2)
[![CI](https://github.com/jason-fox/fox.jason.passthrough.swagger/workflows/CI/badge.svg)](https://github.com/jason-fox/fox.jason.passthrough.swagger/actions?query=workflow%3ACI)
[![Coverage Status](https://coveralls.io/repos/github/jason-fox/fox.jason.passthrough.swagger/badge.svg?branch=master)](https://coveralls.io/github/jason-fox/fox.jason.passthrough.swagger?branch=master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fox.jason.passthrough.swagger&metric=alert_status)](https://sonarcloud.io/dashboard?id=fox.jason.passthrough.swagger)

This is a [DITA-OT Plug-in](https://www.dita-ot.org/plugins) used to auto-create valid DITA-based REST API
documentation. The documentation can be generated directly from a [Swagger 2.0 or OpenAPI 3.x](https://github.com/swagger-api)
file and processed as if it had been written in DITA.

:arrow_forward: [Video from DITA-OT Day 2019](https://youtu.be/cd7XThpkivw)

[![](https://jason-fox.github.io/fox.jason.passthrough.swagger/nothing-video.png)](https://youtu.be/cd7XThpkivw)

<details>
<summary><strong>Table of Contents</strong></summary>

-   [Background](#background)
-   [Install](#install)
    -   [Installing DITA-OT](#installing-dita-ot)
    -   [Installing the Plug-in](#installing-the-plug-in)
-   [Usage](#usage)
-   [License](#license)

</details>

## Background

[<img src="https://swagger.io/swagger/media/assets/images/swagger_logo.svg" align="right" height="55">](http://swagger.io/)

[Swagger](https://swagger.io/) is an open-source software framework backed by a large ecosystem of tools that helps
developers design, build, document, and consume RESTful Web services. While most users identify Swagger by the Swagger
UI tool, the Swagger toolset includes support for automated documentation, code generation, and test-case generation.

This plugin reads a Swagger 2.0 or OpenAPI 3.x file (JSON or YAML, local or remote) and generates DITA directly,
using the official [swagger-parser](https://github.com/swagger-api/swagger-parser) and
[swagger-parser-v3](https://github.com/swagger-api/swagger-parser) libraries to parse the document. There is no
intermediate Markdown or AsciiDoc step, and no dependency on Pandoc.

#### Sample Swagger Endpoint

```json
  "paths": {
    "/pet": {
      "put": {
        "tags": [ "pet" ],
        "summary": "Update an existing pet",
        "description": "",
        "operationId": "updatePet",
        "consumes": ["application/json", "application/xml"],
        "produces": ["application/xml", "application/json"],
        "parameters": [
          {
            "in": "body", "name": "body",  "required": true,
            "description": "Pet object that needs to be added to the store",
            "schema": { "$ref": "#/definitions/Pet" }
          }
        ],
        "responses": {
          "400": {"description": "Invalid ID supplied"},
          "404": {"description": "Pet not found"},
          "405": {"description": "Validation exception"}
        },
        "security": [
          {
            "petstore_auth": ["write:pets","read:pets"]
          }
        ]
      }
    },
```

#### Sample DITA Output

> ![](https://jason-fox.github.io/fox.jason.passthrough.swagger/request-formatted.png)

## Install

The DITA-OT Swagger plug-in has been tested against [DITA-OT 3.x](http://www.dita-ot.org/download). It is recommended
that you upgrade to the latest version.

### Installing DITA-OT

<a href="https://www.dita-ot.org"><img src="https://www.dita-ot.org/images/dita-ot-logo.svg" align="right" height="55"></a>

The DITA-OT Swagger plug-in is a file reader for the DITA Open Toolkit.

-   Full installation instructions for downloading DITA-OT can be found
    [here](https://www.dita-ot.org/4.0/topics/installing-client.html).

    1.  Download the `dita-ot-4.2.zip` package from the project website at
        [dita-ot.org/download](https://www.dita-ot.org/download)
    2.  Extract the contents of the package to the directory where you want to install DITA-OT.
    3.  **Optional**: Add the absolute path for the `bin` directory to the _PATH_ system variable.

    This defines the necessary environment variable to run the `dita` command from the command line.

```console
curl -LO https://github.com/dita-ot/dita-ot/releases/download/4.2/dita-ot-4.2.zip
unzip -q dita-ot-4.2.zip
rm dita-ot-4.2.zip
```

### Installing the Plug-in

-   Run the plug-in installation commands:

```console
dita install https://github.com/jason-fox/fox.jason.extend.css/archive/master.zip
dita install https://github.com/jason-fox/fox.jason.passthrough.swagger/archive/master.zip
```

The `dita` command line tool requires no additional configuration.

## Usage

For DITA processing, a Swagger 2.0 or OpenAPI 3.x file can be defined in either `json` or `yaml` format.
To mark a file to be passed through for **Swagger** processing, label it with `format="swagger"` within the `*.ditamap`
as shown:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE bookmap PUBLIC "-//OASIS//DTD DITA BookMap//EN" "bookmap.dtd">
<bookmap>
    ...etc
    <appendices toc="yes" print="yes">
      <topicmeta>
        <navtitle>Appendices</navtitle>
      </topicmeta>
      <appendix format="swagger" href="Swagger_Definition.json"/>
   </appendices>
</bookmap>
```

The additional file will be converted to a `*.dita` file and will be added to the build job without further processing.
Unless overriden, the `navtitle` of the included topic will be the same as root name of the file. Any underscores in the
filename will be replaced by spaces in title.

The converted topic's body also carries an `<object data="..." outputclass="swagger-spec">` reference to the raw
spec, alongside an empty `<fallback>` (every other transtype's fallback is the topic's own decomposed
sections/tables, its sibling content, not `<fallback>` itself). The [DITA Bootstrap
AST](https://github.com/jason-fox/dita-bootstrap.ast) plug-in's `ast-bootstrap` transtype renders this as a
`ScalarApiReference` node for a React harness to hand to
[`@scalar/api-reference-react`](https://scalar.com/products/api-references/integrations/react); html5/PDF output is
unaffected since neither has a template that treats `outputclass="swagger-spec"` specially.

`object/@data` must point at a file distinct from the swagger topicref's own `href`, or DITA-OT's job model collides
and silently falls back to copying the raw source instead of converting it - so a copy of the source is written
alongside it at build time under a `.ast` suffix (`Swagger_Definition.json` → `Swagger_Definition.ast.json`). This
generated file is a build artifact, not authored content - `.gitignore` it in projects that use this plug-in.

## License

[Apache 2.0](LICENSE) © 2019 - 2024 Jason Fox

The Program includes the following additional software components which were obtained under license. See
[NOTICES.txt](NOTICES.txt) for the full text of each license.

-   swagger-parser, swagger-parser-v3 and their swagger-core/swagger-models/swagger-annotations dependencies -
    https://github.com/swagger-api/swagger-parser - **Apache 2.0 license**
-   Jackson (jackson-core, jackson-databind, jackson-annotations, jackson-dataformat-yaml, jackson-datatype-jsr310) -
    https://github.com/FasterXML/jackson - **Apache 2.0 license**
-   Guava and its supporting libraries (guava, failureaccess, listenablefuture, jsr305, error_prone_annotations,
    j2objc-annotations) - https://github.com/google/guava - **Apache 2.0 license**
-   SnakeYAML - https://bitbucket.org/snakeyaml/snakeyaml - **Apache 2.0 license**
-   Apache Commons IO and Commons Lang3 - https://commons.apache.org/ - **Apache 2.0 license**
-   Jakarta Bean Validation API - https://github.com/jakartaee/validation - **Apache 2.0 license**
-   javax.validation validation-api - https://github.com/jakartaee/validation - **Apache 2.0 license**
-   SLF4J (slf4j-api, slf4j-ext) - https://www.slf4j.org/ - **MIT license**
-   Animal Sniffer Annotations - https://www.mojohaus.org/animal-sniffer/ - **MIT license**
-   Jakarta XML Binding API and Jakarta Activation API - https://github.com/eclipse-ee4j - **Eclipse Distribution
    License 1.0**
-   Checker Framework checker-compat-qual - https://github.com/typetools/checker-framework - **GNU General Public
    License, version 2, with the Classpath Exception**
