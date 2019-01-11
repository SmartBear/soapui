/*
 * SoapUI, Copyright (C) 2004-2018 SmartBear Software
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

package com.eviware.soapui.monitor;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.support.soap.SoapMessageBuilder;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.SSLSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.log.JettyLogger;
import org.apache.log4j.Logger;
import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.io.Connection;
import org.mortbay.io.EndPoint;
import org.mortbay.io.nio.SelectChannelEndPoint;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.RequestLog;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Core Mock-Engine hosting a Jetty web server
 *
 * @author ole.matzura
 */

public class JettyMockEngine implements MockEngine {
    public final static Logger log = Logger.getLogger(JettyMockEngine.class);

    private Server server;
    private Map<Integer, Map<String, List<MockRunner>>> runners = new HashMap<Integer, Map<String, List<MockRunner>>>();
    private Map<Integer, SoapUIConnector> connectors = new HashMap<Integer, SoapUIConnector>();
    private List<MockRunner> mockRunners = new CopyOnWriteArrayList<MockRunner>();

    private SslSocketConnector sslConnector;

    private boolean addedSslConnector;

    public JettyMockEngine() {
        System.setProperty("org.mortbay.log.class", JettyLogger.class.getName());
    }

    public boolean hasRunningMock(MockService mockService) {
        for (MockRunner runner : mockRunners) {
            if (runner.getMockContext().getMockService() == mockService) {
                return true;
            }
        }

        return false;
    }

    public synchronized void startMockService(MockRunner runner) throws Exception {
        if (server == null) {
            initServer();
        }

        synchronized (server) {
            MockService mockService = runner.getMockContext().getMockService();
            int port = mockService.getPort();

            if (SoapUI.getSettings().getBoolean(SSLSettings.ENABLE_MOCK_SSL) && !addedSslConnector) {
                updateSslConnectorSettings();
                server.addConnector(sslConnector);
                addedSslConnector = true;
            } else {
                if (addedSslConnector) {
                    server.removeConnector(sslConnector);
                }

                addedSslConnector = false;
            }

            if (!runners.containsKey(port)) {
                SoapUIConnector connector = new SoapUIConnector();
                PropertySupport.applySystemProperties(connector, "soapui.mock.connector", runner.getMockContext().getMockService());

                connector.setPort(port);
                if (sslConnector != null) {
                    connector.setConfidentialPort(sslConnector.getPort());
                }

                if (mockService.getBindToHostOnly()) {
                    String host = PropertyExpander.expandProperties(mockService, mockService.getHost());
                    if (StringUtils.hasContent(host)) {
                        connector.setHost(host);
                    }
                }

                boolean wasRunning = server.isRunning();

                if (wasRunning) {
                    server.stop();
                }

                server.addConnector(connector);
                try {
                    server.start();
                } catch (RuntimeException e) {
                    UISupport.showErrorMessage(e);

                    server.removeConnector(connector);
                    if (wasRunning) {
                        server.start();
                        return;
                    }
                }

                connectors.put(port, connector);
                runners.put(port, new HashMap<String, List<MockRunner>>());
            }

            Map<String, List<MockRunner>> map = runners.get(port);
            String path = mockService.getPath();
            if (!map.containsKey(path)) {
                map.put(path, new ArrayList<MockRunner>());
            }
            map.get(path).add(runner);
            mockRunners.add(runner);

            log.info("Started mockService [" + mockService.getName() + "] on port [" + port + "] at path [" + path + "]");
        }
    }

    private void initServer() throws Exception {
        server = new Server();
        server.setThreadPool(new SoapUIJettyThreadPool());
        server.setHandler(new ServerHandler());

        RequestLogHandler logHandler = new RequestLogHandler();
        logHandler.setRequestLog(new MockRequestLog());
        server.addHandler(logHandler);

        sslConnector = new SslSocketConnector();
        sslConnector.setMaxIdleTime(30000);
    }

    private void updateSslConnectorSettings() {
        sslConnector.setKeystore(SoapUI.getSettings().getString(SSLSettings.MOCK_KEYSTORE, null));
        sslConnector.setPassword(SoapUI.getSettings().getString(SSLSettings.MOCK_PASSWORD, null));
        sslConnector.setKeyPassword(SoapUI.getSettings().getString(SSLSettings.MOCK_KEYSTORE_PASSWORD, null));
        String trustStore = SoapUI.getSettings().getString(SSLSettings.MOCK_TRUSTSTORE, null);
        if (StringUtils.hasContent(trustStore)) {
            sslConnector.setTruststore(trustStore);
            sslConnector.setTrustPassword(SoapUI.getSettings().getString(SSLSettings.MOCK_TRUSTSTORE_PASSWORD, null));
        }

        sslConnector.setPort((int) SoapUI.getSettings().getLong(SSLSettings.MOCK_PORT, 443));
        sslConnector.setNeedClientAuth(SoapUI.getSettings().getBoolean(SSLSettings.CLIENT_AUTHENTICATION));
    }

    public void stopMockService(MockRunner runner) {
        synchronized (server) {
            MockService mockService = runner.getMockContext().getMockService();
            final Integer port = mockService.getPort();
            Map<String, List<MockRunner>> map = runners.get(port);

            if (map == null || !map.containsKey(mockService.getPath())) {
                log.warn("Unable to find mockService [" + mockService.getName() + "] on port [" + port +
                        "] at path [" + mockService.getPath() + "] in order to stop it");
                return;
            }

            map.get(mockService.getPath()).remove(runner);
            if (map.get(mockService.getPath()).isEmpty()) {
                map.remove(mockService.getPath());
            }

            mockRunners.remove(runner);

            log.info("Stopped mockService [" + mockService.getName() + "] on port [" + port + "] at path [" + mockService.getPath() + "]");

            if (map.isEmpty() && !SoapUI.getSettings().getBoolean(HttpSettings.LEAVE_MOCKENGINE)) {
                SoapUIConnector connector = connectors.get(port);
                if (connector == null) {
                    log.warn("Missing connectors on port [" + port + "]");
                    return;
                }

                try {
                    log.info("Stopping connector on port " + port);
                    if (!connector.waitUntilIdle(5000)) {
                        log.warn("Failed to wait for idle.. stopping connector anyway..");
                    }
                    connector.stop();
                } catch (Exception e) {
                    SoapUI.logError(e);
                }
                server.removeConnector(connector);
                runners.remove(port);
                if (runners.isEmpty()) {
                    try {
                        log.info("No more connectors.. stopping server");
                        server.stop();
                        if (sslConnector != null) {
                            // server.removeConnector( sslConnector );
                            // sslConnector.stop();
                            // sslConnector = null;
                        }
                    } catch (Exception e) {
                        SoapUI.logError(e);
                    }
                }
            }
        }
    }

    private class SoapUIConnector extends SelectChannelConnector {
        private Set<HttpConnection> connections = new HashSet<HttpConnection>();

        @Override
        protected void connectionClosed(HttpConnection arg0) {
            super.connectionClosed(arg0);
            connections.remove(arg0);
        }

        @Override
        protected void connectionOpened(HttpConnection arg0) {
            super.connectionOpened(arg0);
            connections.add(arg0);
        }

        @Override
        protected Connection newConnection(SocketChannel socketChannel, SelectChannelEndPoint selectChannelEndPoint) {
            return new SoapUIHttpConnection(SoapUIConnector.this, selectChannelEndPoint, getServer());
        }

        public boolean waitUntilIdle(long maxWait) throws Exception {
            while (maxWait > 0 && hasActiveConnections()) {
                System.out.println("Waiting for active connections to finish..");
                Thread.sleep(500);
                maxWait -= 500;
            }

            return !hasActiveConnections();
        }

        private boolean hasActiveConnections() {
            for (HttpConnection connection : connections) {
                if (!connection.isIdle()) {
                    return true;
                }
            }

            return false;
        }
    }

    private class SoapUIHttpConnection extends HttpConnection {
        private CapturingServletInputStream capturingServletInputStream;
        private BufferedServletInputStream bufferedServletInputStream;
        private CapturingServletOutputStream capturingServletOutputStream;

        public SoapUIHttpConnection(Connector connector, EndPoint endPoint, Server server) {
            super(connector, endPoint, server);
        }

        @Override
        public ServletInputStream getInputStream() {
            if (SoapUI.getSettings().getBoolean(HttpSettings.ENABLE_MOCK_WIRE_LOG)) {
                if (capturingServletInputStream == null) {
                    capturingServletInputStream = new CapturingServletInputStream(super.getInputStream());
                    bufferedServletInputStream = new BufferedServletInputStream(capturingServletInputStream);
                }
            } else {
                bufferedServletInputStream = new BufferedServletInputStream(super.getInputStream());
            }

            return bufferedServletInputStream;
        }

        @Override
        public ServletOutputStream getOutputStream() {
            if (SoapUI.getSettings().getBoolean(HttpSettings.ENABLE_MOCK_WIRE_LOG)) {
                if (capturingServletOutputStream == null) {
                    capturingServletOutputStream = new CapturingServletOutputStream(super.getOutputStream());
                }
                return capturingServletOutputStream;
            } else {
                return super.getOutputStream();
            }
        }
    }

    private class BufferedServletInputStream extends ServletInputStream {
        private InputStream source = null;
        private byte[] data = null;
        private InputStream buffer1 = null;

        public BufferedServletInputStream(InputStream is) {
            super();
            source = is;
        }

        public InputStream getBuffer() throws IOException {
            if (source.available() > 0) {
                // New request content available
                data = null;
            }
            if (data == null) {
                ByteArrayOutputStream out = Tools.readAll(source, Tools.READ_ALL);
                data = out.toByteArray();
            }
            if (buffer1 == null) {
                buffer1 = new ByteArrayInputStream(data);
            }
            return buffer1;
        }

        public int read() throws IOException {
            return getBuffer().read();
        }

        public int readLine(byte[] b, int off, int len) throws IOException {

            if (len <= 0) {
                return 0;
            }
            int count = 0, c;

            while ((c = read()) != -1) {
                b[off++] = (byte) c;
                count++;
                if (c == '\n' || count == len) {
                    break;
                }
            }
            return count > 0 ? count : -1;
        }

        public int read(byte[] b) throws IOException {
            return getBuffer().read(b);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            return getBuffer().read(b, off, len);
        }

        public long skip(long n) throws IOException {
            return getBuffer().skip(n);
        }

        public int available() throws IOException {
            return getBuffer().available();
        }

        public void close() throws IOException {
            getBuffer().close();
        }

        public void mark(int readlimit) {
            // buffer.mark( readlimit );
        }

        public boolean markSupported() {
            return false;
        }

        public void reset() throws IOException {
            buffer1 = null;
        }
    }

    private class CapturingServletOutputStream extends ServletOutputStream {
        private ServletOutputStream outputStream;
        private ByteArrayOutputStream captureOutputStream = new ByteArrayOutputStream();

        public CapturingServletOutputStream(ServletOutputStream outputStream) {
            this.outputStream = outputStream;
        }

        public void print(String s) throws IOException {
            outputStream.print(s);
        }

        public void print(boolean b) throws IOException {
            outputStream.print(b);
        }

        public void print(char c) throws IOException {
            outputStream.print(c);
        }

        public void print(int i) throws IOException {
            outputStream.print(i);
        }

        public void print(long l) throws IOException {
            outputStream.print(l);
        }

        public void print(float v) throws IOException {
            outputStream.print(v);
        }

        public void print(double v) throws IOException {
            outputStream.print(v);
        }

        public void println() throws IOException {
            outputStream.println();
        }

        public void println(String s) throws IOException {
            outputStream.println(s);
        }

        public void println(boolean b) throws IOException {
            outputStream.println(b);
        }

        public void println(char c) throws IOException {
            outputStream.println(c);
        }

        public void println(int i) throws IOException {
            outputStream.println(i);
        }

        public void println(long l) throws IOException {
            outputStream.println(l);
        }

        public void println(float v) throws IOException {
            outputStream.println(v);
        }

        public void println(double v) throws IOException {
            outputStream.println(v);
        }

        public void write(int b) throws IOException {
            captureOutputStream.write(b);
            outputStream.write(b);
        }

        public void write(byte[] b) throws IOException {
            captureOutputStream.write(b);
            outputStream.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            captureOutputStream.write(b, off, len);
            outputStream.write(b, off, len);
        }

        public void flush() throws IOException {
            outputStream.flush();
        }

        public void close() throws IOException {
            outputStream.close();
            // log.info( "Closing output stream, captured: " +
            // captureOutputStream.toString() );
        }
    }

    private class CapturingServletInputStream extends ServletInputStream {
        private ServletInputStream inputStream;
        private ByteArrayOutputStream captureOutputStream = new ByteArrayOutputStream();

        public CapturingServletInputStream(ServletInputStream inputStream) {
            this.inputStream = inputStream;
        }

        public int read() throws IOException {
            int i = inputStream.read();
            captureOutputStream.write(i);
            return i;
        }

        public int readLine(byte[] bytes, int i, int i1) throws IOException {
            int result = inputStream.readLine(bytes, i, i1);
            captureOutputStream.write(bytes, i, i1);
            return result;
        }

        public int read(byte[] b) throws IOException {
            int i = inputStream.read(b);
            captureOutputStream.write(b);
            return i;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int result = inputStream.read(b, off, len);
            if (result != -1) {
                captureOutputStream.write(b, off, result);
            }
            return result;
        }

        public long skip(long n) throws IOException {
            return inputStream.skip(n);
        }

        public int available() throws IOException {
            return inputStream.available();
        }

        public void close() throws IOException {
            inputStream.close();
        }

        public void mark(int readLimit) {
            inputStream.mark(readLimit);
        }

        public boolean markSupported() {
            return inputStream.markSupported();
        }

        public void reset() throws IOException {
            inputStream.reset();
        }
    }

    private class ServerHandler extends AbstractHandler {
        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
                throws IOException, ServletException {
            // find mockService
            Map<String, List<MockRunner>> map = runners.get(request.getLocalPort());

            // ssl?
            if (map == null && sslConnector != null && request.getLocalPort() == sslConnector.getPort()) {
                for (Map<String, List<MockRunner>> runnerMap : runners.values()) {
                    if (runnerMap.containsKey(request.getPathInfo())) {
                        map = runnerMap;
                        break;
                    }
                }
            }

            if (map != null) {
                List<MockRunner> wsdlMockRunners = map.get(request.getPathInfo());
                if (wsdlMockRunners == null) {
                    String bestMatchedRootPath = "";

                    for (String root : map.keySet()) {
                        if (request.getPathInfo().startsWith(root) && root.length() > bestMatchedRootPath.length()) {
                            bestMatchedRootPath = root;
                            wsdlMockRunners = map.get(root);
                        }
                    }
                }

                if (wsdlMockRunners != null) {
                    try {
                        DispatchException ex = null;
                        MockResult result = null;

                        for (MockRunner wsdlMockRunner : wsdlMockRunners) {
                            if (!wsdlMockRunner.isRunning()) {
                                continue;
                            }

                            try {
                                result = wsdlMockRunner.dispatchRequest(request, response);
                                if (result != null) {
                                    result.finish();
                                    break;
                                }
                            } catch (DispatchException e) {
                                ex = e;
                            }
                        }

                        if (ex != null && result == null) {
                            throw ex;
                        }
                    } catch (Exception e) {
                        SoapUI.logError(e);

                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        response.setContentType("text/html");
                        response.getWriter().print(
                                SoapMessageBuilder.buildFault("Server", e.getMessage(), SoapVersion.Utils
                                        .getSoapVersionForContentType(request.getContentType(), SoapVersion.Soap11)));
                        // throw new ServletException( e );
                    }
                } else {
                    printMockServiceList(response);
                }
            } else {
                printMockServiceList(response);
            }

            response.flushBuffer();
        }

        private void printMockServiceList(HttpServletResponse response) throws IOException {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/html");

            MockRunner[] mockRunners = getMockRunners();
            PrintWriter out = response.getWriter();
            out.print("<html><body><p>There are currently " + mockRunners.length + " running SoapUI MockServices</p><ul>");

            for (MockRunner mockRunner : mockRunners) {
                out.print("<li><a href=\"");
                out.print(mockRunner.getMockContext().getMockService().getPath() + "?WSDL\">");
                out.print(mockRunner.getMockContext().getMockService().getName());
                out.print(" (on port " + mockRunner.getMockContext().getMockService().getPort() + ")");
                out.print("</a></li>");
            }

            out.print("</ul></p></body></html>");
        }
    }

    public MockRunner[] getMockRunners() {
        return mockRunners.toArray(new MockRunner[mockRunners.size()]);
    }

    private class MockRequestLog extends AbstractLifeCycle implements RequestLog {
        public void log(Request request, Response response) {
            if (!SoapUI.getSettings().getBoolean(HttpSettings.ENABLE_MOCK_WIRE_LOG)) {
                return;
            }

            if (SoapUI.getLogMonitor() == null || SoapUI.getLogMonitor().getLogArea("jetty log") == null
                    || SoapUI.getLogMonitor().getLogArea("jetty log").getLoggers() == null) {
                return;
            }

            Logger logger = SoapUI.getLogMonitor().getLogArea("jetty log").getLoggers()[0];

            try {
                ServletInputStream inputStream = request.getInputStream();
                if (inputStream instanceof CapturingServletInputStream) {
                    ByteArrayOutputStream byteArrayOutputStream = ((CapturingServletInputStream) inputStream).captureOutputStream;
                    String str = request.toString() + byteArrayOutputStream.toString();
                    BufferedReader reader = new BufferedReader(new StringReader(str));
                    ((CapturingServletInputStream) inputStream).captureOutputStream = new ByteArrayOutputStream();

                    String line = reader.readLine();
                    while (line != null) {
                        logger.info(">> \"" + line + "\"");
                        line = reader.readLine();
                    }
                }
            } catch (Exception e) {
                SoapUI.logError(e);
            }

            try {
                ServletOutputStream outputStream = response.getOutputStream();
                if (outputStream instanceof CapturingServletOutputStream) {
                    ByteArrayOutputStream byteArrayOutputStream = ((CapturingServletOutputStream) outputStream).captureOutputStream;
                    String str = request.toString() + byteArrayOutputStream.toString();
                    BufferedReader reader = new BufferedReader(new StringReader(str));
                    ((CapturingServletOutputStream) outputStream).captureOutputStream = new ByteArrayOutputStream();

                    String line = reader.readLine();
                    while (line != null) {
                        logger.info("<< \"" + line + "\"");
                        line = reader.readLine();
                    }
                }
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }
    }
}
