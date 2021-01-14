# Swagger Plugin for DITA-OT [<img src="https://jason-fox.github.io/fox.jason.passthrough.swagger/swagger.png" align="right" width="300">](http://swaggerdita-ot.rtfd.io/)

[![license](https://img.shields.io/github/license/jason-fox/fox.jason.passthrough.swagger.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![DITA-OT 3.6](https://img.shields.io/badge/DITA--OT-3.6-blue.svg)](http://www.dita-ot.org/3.6)
[![CI](https://github.com/jason-fox/fox.jason.passthrough.swagger/workflows/CI/badge.svg)](https://github.com/jason-fox/fox.jason.passthrough.swagger/actions?query=workflow%3ACI)
[![Coverage Status](https://coveralls.io/repos/github/jason-fox/fox.jason.passthrough.swagger/badge.svg?branch=master)](https://coveralls.io/github/jason-fox/fox.jason.passthrough.swagger?branch=master)
[![Documentation Status](https://readthedocs.org/projects/swaggerdita-ot/badge/?version=latest)](https://swaggerdita-ot.readthedocs.io/en/latest/?badge=latest)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fox.jason.passthrough.swagger&metric=alert_status)](https://sonarcloud.io/dashboard?id=fox.jason.passthrough.swagger)

This is a [DITA-OT Plug-in](https://www.dita-ot.org/plugins) used to auto-create valid DITA-based REST API
documentation. The documentation can be generated directly from a [Swagger](https://github.com/swagger-api) file and
processed as if it had been written in DITA.

:arrow_forward: [Video from DITA-OT Day 2019](https://youtu.be/cd7XThpkivw)

[![](https://jason-fox.github.io/fox.jason.passthrough.swagger/nothing-video.png)](https://youtu.be/cd7XThpkivw)

<details>
<summary><strong>Table of Contents</strong></summary>

-   [Background](#background)
-   [Install](#install)
    -   [Installing DITA-OT](#installing-dita-ot)
    -   [Installing the Plug-in](#installing-the-plug-in)
    -   [Installing Pandoc](#installing-pandoc)
-   [Usage](#usage)
-   [License](#license)

</details>

## Background

[<img src="https://swagger.io/swagger/media/assets/images/swagger_logo.svg" align="right" height="55">](http://swagger.io/)

[Swagger](https://swagger.io/) is an open-source software framework backed by a large ecosystem of tools that helps
developers design, build, document, and consume RESTful Web services. While most users identify Swagger by the Swagger
UI tool, the Swagger toolset includes support for automated documentation, code generation, and test-case generation.

[Swagger2Markup](https://github.com/Swagger2Markup/swagger2markup) converts a Swagger JSON or YAML file into one or more
AsciiDoc or GitHub Flavored Markdown documents which can be combined with hand-written documentation. The Swagger source
file can be located locally or remotely via HTTP. Swagger2Markup supports the Swagger 1.2 and 2.0 specification.
Internally it uses the official swagger-parser and my markup-document-builder.

This plugin processes the Swagger file to Pandoc markdown, and the converts the markdown to DITA using the
[Pandoc DITA-OT Plugin](https://github.com/jason-fox/fox.jason.passthrough.pandoc)

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
    [here](https://www.dita-ot.org/3.6/topics/installing-client.html).

    1.  Download the `dita-ot-3.6.zip` package from the project website at
        [dita-ot.org/download](https://www.dita-ot.org/download)
    2.  Extract the contents of the package to the directory where you want to install DITA-OT.
    3.  **Optional**: Add the absolute path for the `bin` directory to the _PATH_ system variable.

    This defines the necessary environment variable to run the `dita` command from the command line.

```console
curl -LO https://github.com/dita-ot/dita-ot/releases/download/3.6/dita-ot-3.6.zip
unzip -q dita-ot-3.6.zip
rm dita-ot-3.6.zip
```

### Installing the Plug-in

-   Run the plug-in installation commands:

```console
dita install https://github.com/doctales/org.doctales.xmltask/archive/master.zip
dita install https://github.com/jason-fox/fox.jason.passthrough/archive/master.zip
dita install https://github.com/jason-fox/fox.jason.passthrough.pandoc/archive/master.zip
dita install https://github.com/jason-fox/fox.jason.extend.css/archive/master.zip
dita install https://github.com/jason-fox/fox.jason.passthrough.swagger/archive/master.zip
```

The `dita` command line tool requires no additional configuration.

---

### Installing Pandoc

To download a copy follow the instructions on the [Install page](https://github.com/jgm/pandoc/blob/master/INSTALL.md)

## Usage

For DITA processing, a swagger file can be defined in either `json` or `yaml` format.
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

## License

[Apache 2.0](LICENSE) © 2019 - 2021 Jason Fox

The Program includes the following additional software components which were obtained under license:

-   swagger2markup-cli-2.0.0.jar - https://github.com/Swagger2Markup/swagger2markup - **Apache 2.0 license**
