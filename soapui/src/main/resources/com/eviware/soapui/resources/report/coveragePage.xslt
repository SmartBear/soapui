<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cov="http://eviware.com/soapui/coverage">
    <xsl:output method="html" encoding="UTF-8"/>

    <!-- Generate a statistics page for project or interface. -->
    <xsl:template match="/cov:projectCoverage|/cov:interfaceCoverage">
        <html>
            <head>
                <title>
                    <xsl:value-of select="cov:name"/>
                </title>
                <link rel="stylesheet" type="text/css" href="coverage.css"/>
            </head>
            <body>
                <div id="page">
                    <h1>
                        <xsl:value-of select="cov:name"/>
                    </h1>
                    <xsl:apply-templates select="cov:contractCoverage"/>
                </div>
            </body>
        </html>
    </xsl:template>

    <!-- Generate a page with messages and color coding for operation. -->
    <xsl:template match="/cov:operationCoverage">
        <html>
            <head>
                <title>
                    <xsl:value-of select="cov:interfaceName"/> -
                    <xsl:value-of select="cov:name"/>
                </title>
                <link rel="stylesheet" type="text/css" href="coverage.css"/>
            </head>
            <body>
                <div id="page">
                    <h1>
                        <xsl:value-of select="cov:interfaceName"/> -
                        <xsl:value-of select="cov:name"/>
                    </h1>
                    <xsl:apply-templates select="cov:contractCoverage"/>
                    <xsl:apply-templates select="cov:message"/>
                </div>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="cov:message">
        <h3>
            <xsl:value-of select="@type"/>
        </h3>
        <xsl:apply-templates select="cov:contractCoverage"/>
        <div class="message">
            <xsl:apply-templates select="cov:line"/>
        </div>
    </xsl:template>

    <xsl:template match="cov:line">
        <!--
                    <xsl:if test="@coverage='false' or @coverage='true' or @coverage='assertion'">
                        <xsl:attribute name="class">coverage-<xsl:value-of select="@coverage"/></xsl:attribute>
                    </xsl:if>
         -->

        <p>
            <xsl:attribute name="class">coverage-<xsl:value-of select="@coverage"/>
            </xsl:attribute>
            <xsl:value-of select="cov:text"/>
            <xsl:apply-templates select="cov:segment"/>
        </p>
    </xsl:template>

    <xsl:template match="cov:segment">
        <span>
            <xsl:if test="@coverage='false' or @coverage='true' or @coverage='assertion'">
                <xsl:attribute name="class">coverage-<xsl:value-of select="@coverage"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="cov:text"/>
            <xsl:apply-templates select="cov:segment"/>
        </span>
    </xsl:template>

    <xsl:template match="cov:contractCoverage">
        <table class="contractCoverage">
            <tr>
                <td class="header">Number of Nodes</td>
                <td class="count">
                    <xsl:value-of select="cov:count"/>
                </td>
            </tr>
            <tr>
                <td class="header">Covered Nodes</td>
                <td class="count">
                    <xsl:value-of select="cov:coverage"/>
                </td>
                <td class="percent">
                    <xsl:choose>
                        <xsl:when test="cov:count = 0">(N/A)</xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="format-number(cov:coverage div cov:count, '0%')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
            <tr>
                <td class="header">Asserted Nodes</td>
                <td class="count">
                    <xsl:value-of select="cov:assertionCoverage"/>
                </td>
                <td class="percent">
                    <xsl:choose>
                        <xsl:when test="cov:count = 0">(N/A)</xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="format-number(cov:assertionCoverage div cov:count, '0%')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </table>
    </xsl:template>

</xsl:stylesheet>
