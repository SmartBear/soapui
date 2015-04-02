<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cov="http://eviware.com/soapui/coverage">
    <xsl:output method="html" encoding="UTF-8"/>

    <!--
         This stylesheet generates the left frame of a coverage report.
         This page has links to detailed pages for all operations in the project.
      -->

    <xsl:template match="/cov:projectCoverage">
        <html>
            <head>
                <title>
                    <xsl:value-of select="cov:name"/> Coverage Report
                </title>
                <link rel="stylesheet" type="text/css" href="coverage.css"/>
            </head>
            <body>
                <div id="browser">
                    <h1>
                        <xsl:value-of select="cov:name"/>
                    </h1>
                    <table>
                        <tr>
                            <td width="16px">
                                <img src="project.png"/>
                            </td>
                            <td colspan="3" align="left">
                                <a href="project.html" target="operationFrame">
                                    <xsl:value-of select="cov:name"/>
                                </a>
                            </td>
                        </tr>
                        <xsl:apply-templates select="cov:interface"/>
                    </table>
                </div>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="cov:interface">
        <tr>
            <td/>
            <td width="16px">
                <img src="interface.png"/>
            </td>
            <td colspan="2" align="left">
                <a href="{cov:ref}" target="operationFrame">
                    <xsl:value-of select="cov:name"/>
                </a>
            </td>
        </tr>
        <xsl:apply-templates select="cov:operation"/>
    </xsl:template>

    <xsl:template match="cov:operation">
        <tr>
            <td colspan="2"/>
            <td width="16px">
                <img src="operation.png"/>
            </td>
            <td>
                <a href="{cov:ref}" target="operationFrame">
                    <xsl:value-of select="cov:name"/>
                </a>
            </td>
        </tr>
    </xsl:template>

</xsl:stylesheet>
