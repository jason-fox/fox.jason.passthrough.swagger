<?xml version="1.0" encoding="utf-8"?>
<!--
	This file is part of the DITA-OT swagger Plug-in project.
	See the accompanying LICENSE file for applicable licenses.
-->
<xsl:stylesheet
  version="2.0"
  xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
	<!-- Shared by the plain <pre> rendering below and dita-bootstrap's accordion-button rendering
	     (bootstrap-accordion.xsl) - takes an explicit $codeblock param rather than using "." so it
	     renders identically regardless of which element is the caller's context node. -->
	<xsl:template name="swagger-summary-row">
		<xsl:param name="codeblock" select="."/>
		<xsl:param name="operationTitle" select="$codeblock/../../*[contains(@class,' topic/title ')]"/>
		<code>
			<span class="swagger-verb">
				<xsl:value-of select="substring-before($codeblock/text(),' ')"/>
			</span>
			<xsl:value-of select="substring-after($codeblock/text(),' ')"/>
		</code>
		<xsl:if test="$operationTitle">
			<span class="swagger-summary small"><xsl:apply-templates select="$operationTitle/node()"/></span>
		</xsl:if>
	</xsl:template>

	<xsl:template match="*[contains(@class,' pr-d/codeblock ') and starts-with(@outputclass, 'swagger-')]">
		<pre>
			<xsl:call-template name="commonattributes"/>
			<xsl:call-template name="setscale"/>
			<xsl:call-template name="setidaname"/>
			<xsl:call-template name="swagger-summary-row"/>
		</pre>
    </xsl:template>

	<xsl:template
	  match="*[contains(@class,' topic/topic ')][starts-with(@outputclass, 'swagger-')]/*[contains(@class,' topic/title ')]"
	  priority="10"
	>
		<a>
			<xsl:attribute name="id"><xsl:apply-templates select="." mode="return-aria-label-id"/></xsl:attribute>
		</a>
	</xsl:template>
	<xsl:template match="*[contains(@class,' topic/object ')][@outputclass = 'swagger-spec']"/>
</xsl:stylesheet>
