<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:rx="http://www.renderx.com/XSL/Extensions"
    version="2.0">


    <xsl:attribute-set name="__swagger__common" use-attribute-sets="codeblock">
     <xsl:attribute name="border-style">solid</xsl:attribute>
     <xsl:attribute name="border-width">1pt</xsl:attribute>
     <xsl:attribute name="padding-top">6pt</xsl:attribute>
     <xsl:attribute name="padding-bottom">6pt</xsl:attribute>
     <xsl:attribute name="padding-start">10pt</xsl:attribute>
     <xsl:attribute name="font-weight">bold</xsl:attribute>
     <xsl:attribute name="font-family">monospace</xsl:attribute>

     <xsl:attribute name="space-after">10pt</xsl:attribute>
     <xsl:attribute name="space-before">10pt</xsl:attribute>
     <xsl:attribute name="start-indent">from-parent(start-indent)</xsl:attribute>

    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__verb">
     <xsl:attribute name="color">white</xsl:attribute>
     <xsl:attribute name="font-family">sans-serif</xsl:attribute>
     <xsl:attribute name="padding-start">8pt</xsl:attribute>
     <xsl:attribute name="padding-end">8pt</xsl:attribute>
     <xsl:attribute name="padding-top">4pt</xsl:attribute>
     <xsl:attribute name="padding-bottom">3pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__get" use-attribute-sets="__swagger__common">
     <xsl:attribute name="border-color">#61affe</xsl:attribute>
     <xsl:attribute name="background-color">#eaf2fb</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__verb__get" use-attribute-sets="__swagger__verb">
     <xsl:attribute name="background-color">#61affe</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__post" use-attribute-sets="__swagger__common">
     <xsl:attribute name="border-color">#49cc90</xsl:attribute>
     <xsl:attribute name="background-color">#e5f6ef</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__verb__post" use-attribute-sets="__swagger__verb">
     <xsl:attribute name="background-color">#49cc90</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__put" use-attribute-sets="__swagger__common">
     <xsl:attribute name="border-color">#fca130</xsl:attribute>
     <xsl:attribute name="background-color">#fbf1e3</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__verb__put" use-attribute-sets="__swagger__verb">
     <xsl:attribute name="background-color">#fca130</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__delete" use-attribute-sets="__swagger__common">
     <xsl:attribute name="border-color">#f93e3e</xsl:attribute>
     <xsl:attribute name="background-color">#f8e3e5</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__verb__delete" use-attribute-sets="__swagger__verb">
     <xsl:attribute name="background-color">#f93e3e</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__patch" use-attribute-sets="__swagger__common">
     <xsl:attribute name="border-color">#50e3c2</xsl:attribute>
     <xsl:attribute name="background-color">#e9fcf9</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__verb__patch" use-attribute-sets="__swagger__verb">
     <xsl:attribute name="background-color">#50e3c2</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__head" use-attribute-sets="__swagger__common">
     <xsl:attribute name="border-color">#9012fe</xsl:attribute>
     <xsl:attribute name="background-color">#f3e7fd</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__verb__head" use-attribute-sets="__swagger__verb">
     <xsl:attribute name="background-color">#9012fe</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__options" use-attribute-sets="__swagger__common">
     <xsl:attribute name="border-color">#0d5aa7</xsl:attribute>
     <xsl:attribute name="background-color">#e6edf6</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__verb__options" use-attribute-sets="__swagger__verb">
     <xsl:attribute name="background-color">#0d5aa7</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__deprecated" use-attribute-sets="__swagger__common">
     <xsl:attribute name="border-color">#ebebeb</xsl:attribute>
     <xsl:attribute name="background-color">#f9f9f9</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__swagger__verb__deprecated" use-attribute-sets="__swagger__verb">
     <xsl:attribute name="background-color">#ebebeb</xsl:attribute>
    </xsl:attribute-set>

</xsl:stylesheet>