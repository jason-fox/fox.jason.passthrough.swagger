<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">

<xsl:template name="gen-user-styles">
  <xsl:apply-templates select="." mode="gen-user-styles"/>
  <xsl:apply-templates select="." mode="gen-prism-style"/>
  <xsl:apply-templates select="." mode="gen-swagger-style"/>
</xsl:template>
  
</xsl:stylesheet>
