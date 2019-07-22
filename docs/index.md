<h1>Swagger Plugin for DITA-OT</h1>

This is a DITA-OT Plug-in to used extend the available input formats for DITA-OT. Valid DITA can be generated directly from a [Swagger](https://github.com/swagger-api) files  and processed as if they had been written in DITA.

[<img src="https://swagger.io/swagger/media/assets/images/swagger_logo.svg" align="right" height="55" width="211">](http://swagger.io/)

[Swagger](https://swagger.io/) is an open-source software framework backed by a large ecosystem of tools that helps developers design, build, document, and consume RESTful Web services. While most users identify Swagger by the Swagger UI tool, the Swagger toolset includes support for automated documentation, code generation, and test-case generation.

[Swagger2Markup](https://github.com/Swagger2Markup/swagger2markup) converts a Swagger JSON or YAML file into one or more AsciiDoc or GitHub Flavored Markdown documents which can be combined with hand-written documentation. The Swagger source file can be located locally or remotely via HTTP. Swagger2Markup supports the Swagger 1.2 and 2.0 specification. Internally it uses the official swagger-parser and my markup-document-builder.

This plugin processes the Swagger file to Pandoc markdown, and the converts the markdown to DITA using the [Pandoc DITA-OT Plugin](https://github.com/jason-fox/fox.jason.passthrough.pandoc)
