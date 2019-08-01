<h1>Swagger Plugin for DITA-OT</h1>

This is a DITA-OT Plug-in used to auto-create valid DITA-based REST API documentation. The documentation can be generated directly from a [Swagger](https://github.com/swagger-api) file and processed as if it had been written in DITA.

[<img src="https://swagger.io/swagger/media/assets/images/swagger_logo.svg" align="right" height="55" width="211">](http://swagger.io/)

[Swagger](https://swagger.io/) is an open-source software framework backed by a large ecosystem of tools that helps developers design, build, document, and consume RESTful Web services. While most users identify Swagger by the Swagger UI tool, the Swagger toolset includes support for automated documentation, code generation, and test-case generation.

[Swagger2Markup](https://github.com/Swagger2Markup/swagger2markup) converts a Swagger JSON or YAML file into one or more AsciiDoc or GitHub Flavored Markdown documents which can be combined with hand-written documentation. The Swagger source file can be located locally or remotely via HTTP. Swagger2Markup supports the Swagger 1.2 and 2.0 specification. Internally it uses the official swagger-parser and my markup-document-builder.

This plugin processes the Swagger file to Pandoc markdown, and the converts the markdown to DITA using the [Pandoc DITA-OT Plugin](https://github.com/jason-fox/fox.jason.passthrough.pandoc)

<h4>Sample Swagger Endpoint</h4>

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

<h4>Sample DITA Output</h4>

<img src="https://jason-fox.github.io/fox.jason.passthrough.swagger/request-formatted.png" align="center" style="border-style:solid; border-width:1px; border-color:#ddd"/>
