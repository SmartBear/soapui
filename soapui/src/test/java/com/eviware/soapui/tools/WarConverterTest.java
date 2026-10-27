/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */

package com.eviware.soapui.tools;

import com.eviware.soapui.mockaswar.MockAsWarServlet;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WarConverterTest {

    private static final String SERVLET_CLASS_ENTRY = "WEB-INF/classes/com/eviware/soapui/mockaswar/MockAsWarServlet.class";
    private static final String JAVAEE_WEB_XML = "<web-app xmlns=\"http://java.sun.com/xml/ns/javaee\" version=\"2.5\"/>";

    private File tmpDir;

    @Before
    public void setUp() throws IOException {
        tmpDir = Files.createTempDirectory("warconverter").toFile();
    }

    @After
    public void tearDown() throws IOException {
        org.apache.commons.io.FileUtils.deleteDirectory(tmpDir);
    }

    @Test
    public void convertsServletClassesToJakarta()throws IOException {
        File source = createJavaxWar();
        File destination = new File(tmpDir, "converted.war");

        WarConverter.convert(source, destination);

        try (JarFile war = new JarFile(destination)) {
            String servletClass = read(war, SERVLET_CLASS_ENTRY);
            assertTrue(servletClass.contains("jakarta/servlet/http/HttpServlet"));
            assertFalse(servletClass.contains("javax/servlet"));

            // Tomcat 10+ still accepts the Servlet 2.5 descriptor, the TOMCAT profile leaves it as is
            assertEquals(JAVAEE_WEB_XML, read(war, "WEB-INF/web.xml"));
        }
    }

    @Test
    public void routesMigrationLogsToLog4jInsteadOfStderr() throws IOException {
        List<String> messages = new ArrayList<>();
        Appender appender = new AbstractAppender("warConverterTestAppender", null, null, true, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                messages.add(event.getMessage().getFormattedMessage());
            }
        };
        appender.start();
        org.apache.logging.log4j.core.Logger converterLogger =
                (org.apache.logging.log4j.core.Logger) LogManager.getLogger(WarConverter.class);
        converterLogger.addAppender(appender);
        org.apache.logging.log4j.Level previousLevel = converterLogger.getLevel();
        converterLogger.setLevel(org.apache.logging.log4j.Level.INFO);
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("org.apache.tomcat.jakartaee");
        try {
            WarConverter.convert(createJavaxWar(), new File(tmpDir, "converted.war"));
        } finally {
            converterLogger.setLevel(previousLevel);
            converterLogger.removeAppender(appender);
            appender.stop();
        }

        assertTrue("Migration logs should be forwarded to log4j: " + messages,
                messages.stream().anyMatch(m -> m.contains("Migration completed")));
        assertTrue("JUL parent handlers should be restored", julLogger.getUseParentHandlers());
        assertEquals(0, julLogger.getHandlers().length);
    }

    @Test
    public void tomcatTargetDefaultsToTomcat10Plus() {
        assertEquals(TomcatTarget.TOMCAT_10_PLUS, TomcatTarget.fromLabel(null));
        assertEquals(TomcatTarget.TOMCAT_10_PLUS, TomcatTarget.fromVersion(null));
        assertEquals(TomcatTarget.TOMCAT_10_PLUS, TomcatTarget.fromVersion("10"));
        assertEquals(TomcatTarget.TOMCAT_9_LEGACY, TomcatTarget.fromVersion("9"));
        assertEquals(TomcatTarget.TOMCAT_9_LEGACY, TomcatTarget.fromLabel(TomcatTarget.TOMCAT_9_LEGACY.getLabel()));
    }

    private File createJavaxWar() throws IOException {
        File war = new File(tmpDir, "javax.war");
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(war))) {
            out.putNextEntry(new JarEntry("WEB-INF/web.xml"));
            out.write(JAVAEE_WEB_XML.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();

            out.putNextEntry(new JarEntry(SERVLET_CLASS_ENTRY));
            try (InputStream in = MockAsWarServlet.class.getResourceAsStream("MockAsWarServlet.class")) {
                IOUtils.copy(in, out);
            }
            out.closeEntry();
        }
        return war;
    }

    private String read(JarFile war, String entryName) throws IOException {
        try (InputStream in = war.getInputStream(war.getJarEntry(entryName))) {
            // ISO-8859-1 keeps class file bytes intact for substring checks
            return new String(IOUtils.toByteArray(in), StandardCharsets.ISO_8859_1);
        }
    }
}
