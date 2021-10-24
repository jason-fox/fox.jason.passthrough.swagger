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
	<xsl:template match="*[contains(@class,' pr-d/codeblock ') and starts-with(@outputclass, 'swagger-')]">
		<pre>
			<xsl:call-template name="commonattributes"/>
			<xsl:call-template name="setscale"/>
			<xsl:call-template name="setidaname"/>
			<code>
				<span class="swagger-verb">
					<xsl:value-of select="substring-before(text(),' ')"/>
				</span>
				<xsl:value-of select="substring-after(text(),' ')"/>
			</code>
		</pre>
    </xsl:template>
</xsl:stylesheet>
