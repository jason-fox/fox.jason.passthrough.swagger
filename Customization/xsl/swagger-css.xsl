<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">

  <xsl:param name="SWAGGER-CSS" select="'swagger.css'"/> 

  <xsl:template match="/|node()|@*" mode="gen-swagger-style">
     <xsl:variable name="urltest" as="xs:boolean"> <!-- test for URL -->
        <xsl:call-template name="url-string">
          <xsl:with-param name="urltext" select="concat($CSSPATH, $SWAGGER-CSS)"/>
        </xsl:call-template>
      </xsl:variable>

     <xsl:choose>
        <xsl:when test="$urltest">
          <link rel="stylesheet" type="text/css" href="{$CSSPATH}{$SWAGGER-CSS}" />
        </xsl:when>
        <xsl:otherwise>
          <link rel="stylesheet" type="text/css" href="{$PATH2PROJ}{$CSSPATH}{$SWAGGER-CSS}" />
        </xsl:otherwise>
      </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
