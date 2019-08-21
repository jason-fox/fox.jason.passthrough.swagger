<?xml version="1.0" encoding="UTF-8"?>
<!--
  This file is part of the DITA-OT swagger Plug-in project.
  See the accompanying LICENSE file for applicable licenses.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    exclude-result-prefixes="xs"
    version="2.0">

    <xsl:import href="../cfg/fo/attrs/swagger-attr.xsl"/>

    <xsl:template name="processSwaggerAttrSetReflection">
        <xsl:param name="attrSet"/>
        <xsl:param name="path"/>
        <xsl:apply-templates select="document($path)//xsl:attribute-set[@name = $attrSet]"/>
    </xsl:template>


   

    <xsl:template match="*[contains(@class,' pr-d/codeblock ') and starts-with(@outputclass, 'swagger-')]">
        <xsl:call-template name="generateAttrLabel"/>
        <fo:block>

            <xsl:call-template name="processSwaggerAttrSetReflection">
                <xsl:with-param name="attrSet" select="replace(@outputclass,'swagger-','__swagger__')"/>
                <xsl:with-param name="path" select="'../cfg/fo/attrs/swagger-attr.xsl'"/>
            </xsl:call-template>
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="setFrame"/>
            <xsl:call-template name="setScale"/>
            <xsl:call-template name="setExpanse"/>
              <!--xsl:apply-templates/-->
              <fo:inline>
                <xsl:call-template name="processSwaggerAttrSetReflection">
                  <xsl:with-param name="attrSet" select="replace(@outputclass,'swagger-','__swagger__verb__')"/>
                  <xsl:with-param name="path" select="'../cfg/fo/attrs/swagger-attr.xsl'"/>
              </xsl:call-template>
                <xsl:value-of select="substring-before(text(),' ')"/>
              </fo:inline>
              <fo:inline>
                <xsl:attribute name="padding-start">10pt</xsl:attribute>
                <xsl:value-of select="substring-after(text(),' ')"/>
              </fo:inline>
        </fo:block>
    </xsl:template>


</xsl:stylesheet>
