/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.mockaswar;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunner;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.monitor.MockEngine;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;
import com.smartbear.soapui.core.Logging;
import org.apache.commons.collections.list.TreeList;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.xmlbeans.XmlException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet implementation class SoapUIMockServlet
 */
@SuppressWarnings("unchecked")
public class MockAsWarServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    protected static Logger logger = Logger.getLogger(MockAsWarServlet.class.getName());
    protected WsdlProject project;
    long maxResults;
    List<MockResult> results = new TreeList();
    private List<LogEvent> events = new TreeList();
    boolean enableWebUI;

    public void init() throws ServletException {
        super.init();
        try {
            SoapUI.setSoapUICore(new MockServletSoapUICore(getServletContext()), true);

            String mockServiceEndpoint = initMockServiceParameters();

            logger.info("Loading project");

            initProject(getServletContext().getRealPath(getInitParameter("projectFile")));

            if (project == null || project.getName() == null) {
                initProject(getServletContext().getResource("/" + getInitParameter("projectFile")).toString());
            }

            if (project == null) {
                logger.info("Starting Mock service(s)");
            }

            for (MockService mockService : project.getMockServiceList()) {
                logger.info("Starting mock service [" + mockService.getName() + "]");
                if (StringUtils.hasContent(mockServiceEndpoint)) {
                    ((WsdlMockService) mockService).setMockServiceEndpoint(mockServiceEndpoint);
                }

                mockService.start();
            }

            for (MockService mockService : project.getRestMockServiceList()) {
                logger.info("Starting REST mock service [" + mockService.getName() + "]");
                mockService.start();
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    protected void initProject(String path) throws XmlException, IOException, SoapUIException {
        project = (WsdlProject) ProjectFactoryRegistry.getProjectFactory("wsdl").createNew(path);
    }

    protected String initMockServiceParameters() {
        try {
            if (StringUtils.hasContent(getInitParameter("listeners"))) {
                logger.info("Init listeners");
                try {
                    System.setProperty("soapui.ext.listeners", getServletContext().getRealPath(getInitParameter("listeners")));
                } catch (Exception e) {
                    logger.info("Listeners not set! Reason : " + e.getMessage());
                }
            } else {
                logger.info("Listeners not set!");
            }

            if (StringUtils.hasContent(getInitParameter("actions"))) {
                logger.info("Init actions");
                try {
                    System.setProperty("soapui.ext.actions", getServletContext().getRealPath(getInitParameter("actions")));
                } catch (Exception e) {
                    logger.info("Actions not set! Reason : " + e.getMessage());
                }
            } else {
                logger.info("Actions not set!");
            }

            if (SoapUI.getSoapUICore() == null) {
                if (StringUtils.hasContent(getInitParameter("soapUISettings"))) {
                    logger.info("Init settings");
                    SoapUI.setSoapUICore(
                            new MockServletSoapUICore(getServletContext(), getInitParameter("soapUISettings")), true);
                } else {
                    logger.info("Settings not set!");
                    SoapUI.setSoapUICore(new MockServletSoapUICore(getServletContext()), true);
                }
            } else {
                logger.info("SoapUI core already exists, reusing existing one");
            }

            if (StringUtils.hasContent(getInitParameter("enableWebUI"))) {
                if ("true".equals(getInitParameter("enableWebUI"))) {
                    logger.info("WebUI ENABLED");
                    enableWebUI = true;
                } else {
                    logger.info("WebUI DISABLED");
                    enableWebUI = false;
                }
            }

        } catch (Exception e) {
            logger.info("Property set with error!" + e.getMessage());
        }
        try {
            maxResults = Integer.parseInt(getInitParameter("maxResults"));
        } catch (NumberFormatException ex) {
            maxResults = 1000;
        }

        Logging.addAppender(Logging.ensureGroovyLog().getName(), new GroovyLogAppender());

        return getInitParameter("mockServiceEndpoint");
    }

    public void destroy() {
        super.destroy();
        getMockServletCore().stop();
    }

    protected MockAsWarCoreInterface getMockServletCore() {
        return (MockAsWarCoreInterface) SoapUI.getSoapUICore();
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        try {
            getMockServletCore().dispatchRequest(request, response);
        } catch (DispatchException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void printResult(PrintWriter out, MockResult result) {

        out.print("<h4>Details for MockResult at " + new java.util.Date(result.getTimestamp()) + " ("
                + result.getTimeTaken() + "ms)</h4>");

        out.println("<hr/><p><b>Request Headers</b>:</p>");
        out.print("<table border=\"1\"><tr><td>Header</td><td>Value</td></tr>");
        StringToStringsMap headers = result.getMockRequest().getRequestHeaders();
        for (String name : headers.getKeys()) {
            for (String value : headers.get(name)) {
                out.println("<tr><td>" + name + "</td><td>" + value + "</td></tr>");
            }
        }
        out.println("</table>");

        out.println("<hr/><b>Incoming Request</b>:<br/><pre>"
                + XmlUtils.entitize(result.getMockRequest().getRequestContent()) + "</pre>");

        out.println("<hr/><p><b>Response Headers</b>:</p>");
        out.print("<table border\"1\"><tr><td>Header</td><td>Value</td></tr>");
        headers = result.getResponseHeaders();
        for (String name : headers.getKeys()) {
            for (String value : headers.get(name)) {
                out.println("<tr><td>" + name + "</td><td>" + value + "</td></tr>");
            }
        }
        out.println("</table>");

        out.println("<hr/><b>Returned Response</b>:<pre>" + XmlUtils.entitize(result.getResponseContent()) + "</pre>");
    }

    class MockServletSoapUICore extends DefaultSoapUICore implements MockEngine, MockAsWarCoreInterface {
        private final ServletContext servletContext;
        private List<MockRunner> mockRunners = new ArrayList<MockRunner>();

        public MockServletSoapUICore(ServletContext servletContext, String soapUISettings) {
            super(servletContext.getRealPath("/"), servletContext.getRealPath(soapUISettings));
            this.servletContext = servletContext;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.eviware.soapui.mockaswar.MockAsWarCoreInterface#dispatchRequest
         * (javax.servlet.http.HttpServletRequest,
         * javax.servlet.http.HttpServletResponse)
         */
        @Override
        public void dispatchRequest(HttpServletRequest request, HttpServletResponse response) throws DispatchException,
                IOException {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null) {
                pathInfo = "";
            }

            MockRunner mockRunner = getMatchedMockRunner(getMockRunners(), pathInfo);

            if (mockRunner != null) {
                MockResult result = mockRunner.dispatchRequest(request, response);

                if (maxResults > 0) {
                    synchronized (results) {
                        while (maxResults > 0 && results.size() > maxResults) {
                            results.remove(0);
                        }
                        if (result != null) {
                            results.add(result);
                        }
                    }
                }
                return;
            }

            if (enableWebUI) {
                String realPath = servletContext.getRealPath(pathInfo);
                File file = realPath == null ? null : new File(realPath);
                if (file != null && file.exists() && file.isFile()) {
                    FileInputStream in = new FileInputStream(file);
                    response.setStatus(HttpServletResponse.SC_OK);
                    long length = file.length();
                    response.setContentLength((int) length);
                    response.setContentType(ContentTypeHandler.getContentTypeFromFilename(file.getName()));
                    Tools.readAndWrite(in, length, response.getOutputStream());
                    in.close();
                } else if (pathInfo.equals("/master")) {
                    printMaster(request, response, mockRunners);
                } else if (pathInfo.equals("/detail")) {
                    printDetail(request, response);
                } else if (pathInfo.equals("/log")) {
                    printLog(request, response);
                } else {
                    printFrameset(request, response);
                }
            } else {
                printDisabledLogFrameset(request, response);
            }
        }

        private MockRunner getMatchedMockRunner(MockRunner[] mockRunners, String pathInfo) {

            MockRunner mockRunner = null;
            String bestMatchedRootPath = "";

            for (MockRunner runner : mockRunners) {
                String mockServicePath = runner.getMockContext().getMockService().getPath();
                if (pathInfo.startsWith(mockServicePath) && mockServicePath.length() > bestMatchedRootPath.length()) {
                    bestMatchedRootPath = mockServicePath;
                    mockRunner = runner;
                }
            }

            return mockRunner;

        }

        /*
         * (non-Javadoc)
         *
         * @see com.eviware.soapui.mockaswar.MockAsWarCoreInterface#stop()
         */
        @Override
        public void stop() {
            for (MockRunner mockRunner : getMockRunners()) {
                mockRunner.stop();
            }
        }

        public MockServletSoapUICore(ServletContext servletContext) {
            super(servletContext.getRealPath("/"), null);
            this.servletContext = servletContext;
        }

        @Override
        protected MockEngine buildMockEngine() {
            return this;
        }

        public MockRunner[] getMockRunners() {
            return mockRunners.toArray(new MockRunner[mockRunners.size()]);
        }

        public boolean hasRunningMock(MockService mockService) {
            for (MockRunner runner : mockRunners) {
                if (runner.getMockContext().getMockService() == mockService) {
                    return true;
                }
            }

            return false;
        }

        public void startMockService(MockRunner runner) throws Exception {
            mockRunners.add(runner);
        }

        public void stopMockService(MockRunner runner) {
            mockRunners.remove(runner);
        }
    }

    public void printMaster(HttpServletRequest request, HttpServletResponse response, List<MockRunner> mockRunners)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        startHtmlPage(out, "MockService Log Table", "15");

        out.print("<img src=\"header_logo.png\"><h3>SoapUI MockServices Log for project [" + project.getName()
                + "]</h3>" + "<p style=\"text-align: left\">WSDLs:");

        for (MockRunner mockRunner : mockRunners) {
            String overviewUrl = ((WsdlMockRunner) mockRunner).getOverviewUrl();
            if (overviewUrl.startsWith("/")) {
                overviewUrl = overviewUrl.substring(1);
            }

            out.print(" [<a target=\"new\" href=\"" + overviewUrl + "\">" + mockRunner.getMockContext().getMockService().getName()
                    + "</a>]");
        }

        out.print("</p>");

        out.print("<hr/><p><b>Processed Requests</b>: ");
        out.print("[<a href=\"master\">Refresh</a>] ");
        out.print("[<a href=\"master?clear\">Clear</a>]</p>");

        if ("clear".equals(request.getQueryString())) {
            results.clear();
        }

        out.print("<table border=\"1\">");
        out.print("<tr><td></td><td>Timestamp</td><td>Time Taken</td><td>MockOperation</td><td>MockResponse</td><td>MockService</td></tr>");

        int cnt = 1;

        for (MockResult result : results) {

            if (result != null) {
                out.print("<tr><td>" + (cnt++) + "</td>");
                out.print("<td><a target=\"detail\" href=\"detail?" + result.hashCode() + "\">"
                        + new java.util.Date(result.getTimestamp()) + "</a></td>");
                out.print("<td>" + result.getTimeTaken() + "</td>");
                out.print("<td>" + result.getMockOperation().getName() + "</td>");
                if (result.getMockResponse() != null) {
                    out.print("<td>" + result.getMockResponse().getName() + "</td>");
                }
                out.print("<td>" + result.getMockOperation().getMockService().getName() + "</td></tr>");
            }
        }

        out.print("</table>");

        out.print("</body></html>");
        out.flush();

    }

    private void startHtmlPage(PrintWriter out, String title, String refresh) {
        out.print("<html><head>");
        out.print("<title>" + title + "</title>");
        if (refresh != null) {
            out.print("<meta http-equiv=\"refresh\" content=\"" + refresh + "\"/>");
        }

        out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"stylesheet.css\" />");
        out.print("</head><body>");
    }

    public void printDisabledLogFrameset(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.print("<html><head><title>SoapUI MockServices Log for project [" + project.getName() + "]</title></head>");
        out.print("<body>");
        out.print("<h3>");
        out.print("Log is disabled.");
        out.print("</h3>");
        out.print("</body></html>");
        out.flush();
    }

    public void printFrameset(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.print("<html><head><title>SoapUI MockServices Log for project [" + project.getName() + "]</title></head>");
        out.print("<frameset rows=\"40%,40%,*\">");
        out.print("<frame src=\"master\"/>");
        out.print("<frame name=\"detail\" src=\"detail\"/>");
        out.print("<frame src=\"log\"/>");
        out.print("</frameset>");
        out.print("</html>");
        out.flush();
    }

    public void printDetail(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        startHtmlPage(out, "MockService Detail", null);

        int id = 0;

        try {
            id = Integer.parseInt(request.getQueryString());
        } catch (NumberFormatException e) {
        }

        if (id > 0) {
            for (MockResult result : results) {
                if (result.hashCode() == id) {
                    id = 0;
                    printResult(out, result);
                }
            }
        }

        if (id > 0) {
            out.print("<p>Missing specified MockResult</p>");
        }

        out.print("</body></html>");
        out.flush();
    }

    private class GroovyLogAppender extends AbstractAppender {
        static final String GROOVY_LOG_APPENDER_NAME = "GROOVY_LOG_APPENDER";

        GroovyLogAppender() {
            super(GROOVY_LOG_APPENDER_NAME, null, PatternLayout.createDefaultLayout());
        }

        @Override
        public void append(LogEvent event) {
            events.add(event);
        }

        public void close() {
        }

        public boolean requiresLayout() {
            return false;
        }
    }

    public void printLog(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        startHtmlPage(out, "MockService Groovy Log", "15");
        out.print("<p><b>Groovy Log output</b>: ");
        out.print("[<a href=\"log\">Refresh</a>] ");
        out.print("[<a href=\"log?clear\">Clear</a>]</p>");

        if ("clear".equals(request.getQueryString())) {
            events.clear();
        }

        out.print("<table border=\"1\">");
        out.print("<tr><td></td><td>Timestamp</td><td>Message</td></tr>");

        int cnt = 1;

        for (LogEvent event : events) {

            out.print("<tr><td>" + (cnt++) + "</td>");
            out.print("<td>" + new java.util.Date(event.getTimeMillis()) + "</td>");
            out.print("<td>" + event.getMessage().getFormattedMessage() + "</td></tr>");
        }

        out.print("</table>");

        out.print("</body></html>");
        out.flush();
    }
}
