/*
 * Copyright 2004-2014 SmartBear Software
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
package com.smartbear.soapui.utils.jetty;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

public class JettyTestCaseBase {
    private static final AtomicInteger PORT = new AtomicInteger(8888);

    private final int port;
    private final String resourceBase;
    private final Server server;

    protected JettyTestCaseBase() {
        port = PORT.getAndIncrement();
        resourceBase = Files.createTempDir().getAbsolutePath();
        try {
            FileUtils.copyDirectory(new File(JettyTestCaseBase.class.getResource("/config").toURI()).getParentFile(), new File(resourceBase));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unexpected URI syntax exception! - this shouldn't happen", e);
        }
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setResourceBase(resourceBase);
        this.server = new Server(port);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource_handler, new DefaultHandler()});
        this.server.setHandler(handlers);
    }

    protected void replaceInFile(String fileName, String from, String to) throws IOException {
        File wsdlFile = new File(getResourceBase(), fileName);
        String wsdl = Files.toString(wsdlFile, Charset.forName("UTF-8"));

        wsdl = wsdl.replace(from, to);

        Files.write(wsdl, wsdlFile, Charset.forName("UTF-8"));
    }

    protected void startJetty() throws Exception {
        server.start();
    }

    protected void stopJetty() throws Exception {
        try {
            server.stop();
        } finally {
            try {
                FileUtils.deleteDirectory(new File(getResourceBase()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Before
    public void setup() throws Exception {
        startJetty();
    }

    @After
    public void shutdown() throws Exception {
        stopJetty();
    }

    protected String getResourceBase() {
        return resourceBase;
    }

    protected int getPort() {
        return port;
    }
}
