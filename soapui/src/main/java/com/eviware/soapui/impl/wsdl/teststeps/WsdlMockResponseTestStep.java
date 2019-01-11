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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MockOperationDispatchStyleConfig;
import com.eviware.soapui.config.MockResponseConfig;
import com.eviware.soapui.config.MockResponseStepConfig;
import com.eviware.soapui.config.MockServiceConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse.ResponseHeaderHolder;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunner;
import com.eviware.soapui.impl.wsdl.mock.dispatch.QueryMatchMockOperationDispatcher;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.WsdlMockResultMessageExchange;
import com.eviware.soapui.impl.wsdl.support.IconAnimator;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertableConfig;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertionsSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.DefaultTestStepProperty;
import com.eviware.soapui.model.support.InterfaceListenerAdapter;
import com.eviware.soapui.model.support.MockRunListenerAdapter;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.support.TestStepBeanProperty;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertedXPath;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.OperationTestStep;
import com.eviware.soapui.model.testsuite.RequestAssertedMessageExchange;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.monitor.TestMonitor;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ChangeOperationResolver;
import com.eviware.soapui.support.resolver.ImportInterfaceResolver;
import com.eviware.soapui.support.resolver.RemoveTestStepResolver;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.resolver.ResolveContext.PathToResolve;
import com.eviware.soapui.support.types.StringToStringsMap;
import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WsdlMockResponseTestStep extends WsdlTestStepWithProperties implements OperationTestStep,
        PropertyChangeListener, Assertable, PropertyExpansionContainer {
    private final static Logger log = Logger.getLogger(WsdlMockResponseTestStep.class);

    public static final String STATUS_PROPERTY = WsdlMockResponseTestStep.class.getName() + "@status";
    public static final String TIMEOUT_PROPERTY = WsdlMockResponseTestStep.class.getName() + "@timeout";

    private MockResponseStepConfig mockResponseStepConfig;
    private MockResponseConfig mockResponseConfig;
    private WsdlMockOperation mockOperation;
    private WsdlTestMockService mockService;
    private WsdlMockRunner mockRunner;
    private WsdlMockResponse mockResponse;
    private WsdlMockResult lastResult;

    private AssertionsSupport assertionsSupport;
    private InternalMockRunListener mockRunListener;
    private StartStepMockRunListener startStepMockRunListener;

    private final InternalProjectListener projectListener = new InternalProjectListener();
    private final InternalInterfaceListener interfaceListener = new InternalInterfaceListener();
    private final InternalTestRunListener testRunListener = new InternalTestRunListener();
    private WsdlInterface iface;
    private AssertionStatus oldStatus;

    private IconAnimator<WsdlMockResponseTestStep> iconAnimator;
    private ImageIcon validRequestIcon;
    private ImageIcon failedRequestIcon;
    private ImageIcon disabledRequestIcon;
    private ImageIcon unknownRequestIcon;

    private WsdlMockResponse testMockResponse;
    private WsdlTestStep startTestStep;
    private boolean forLoadTest;

    public WsdlMockResponseTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        super(testCase, config, true, forLoadTest);

        if (config.getConfig() != null) {
            mockResponseStepConfig = (MockResponseStepConfig) config.getConfig().changeType(MockResponseStepConfig.type);
            mockResponseConfig = mockResponseStepConfig.getResponse();
        } else {
            mockResponseStepConfig = (MockResponseStepConfig) config.addNewConfig().changeType(
                    MockResponseStepConfig.type);
            mockResponseConfig = mockResponseStepConfig.addNewResponse();
        }

        initAssertions();
        initMockObjects(testCase);
        this.forLoadTest = forLoadTest;

        if (!forLoadTest) {
            if (iface != null) {
                iface.getProject().addProjectListener(projectListener);
                iface.addInterfaceListener(interfaceListener);
            }

            iconAnimator = new IconAnimator<WsdlMockResponseTestStep>(this, "/mockResponseStep.gif",
                    "/exec_mockResponse.gif", 4);

            initIcons();
        }

        // init properties
        initProperties();

        testCase.addTestRunListener(testRunListener);
        testCase.addPropertyChangeListener(this);
    }

    @Override
    public void afterLoad() {
        super.afterLoad();

        if (mockResponseStepConfig.isSetStartStep()) {
            startTestStep = getTestCase().getTestStepByName(mockResponseStepConfig.getStartStep());
            if (startTestStep != null) {
                startTestStep.addPropertyChangeListener(this);
            }
        }
    }

    private void initProperties() {
        if (mockResponse != null) {
            addProperty(new TestStepBeanProperty("Response", false, mockResponse, "responseContent", this));
        }

        addProperty(new DefaultTestStepProperty("Request", true, new DefaultTestStepProperty.PropertyHandlerAdapter() {
            public String getValue(DefaultTestStepProperty property) {
                MockResult mockResult = mockResponse == null ? null : mockResponse.getMockResult();
                return mockResult == null ? null : mockResult.getMockRequest().getRequestContent();
            }
        }, this));
    }

    @Override
    public ImageIcon getIcon() {
        if (forLoadTest || iconAnimator == null) {
            return null;
        }

        TestMonitor testMonitor = SoapUI.getTestMonitor();
        if (testMonitor != null
                && (testMonitor.hasRunningLoadTest(getTestCase()) || testMonitor.hasRunningSecurityTest(getTestCase()))) {
            return disabledRequestIcon;
        }

        ImageIcon icon = iconAnimator.getIcon();
        if (icon == iconAnimator.getBaseIcon()) {
            AssertionStatus status = getAssertionStatus();
            if (status == AssertionStatus.VALID) {
                return validRequestIcon;
            } else if (status == AssertionStatus.FAILED) {
                return failedRequestIcon;
            } else if (status == AssertionStatus.UNKNOWN) {
                return unknownRequestIcon;
            }
        }

        return icon;
    }

    public void initIcons() {
        if (validRequestIcon == null) {
            validRequestIcon = UISupport.createImageIcon("/valid_soap_request_step.png");
        }

        if (failedRequestIcon == null) {
            failedRequestIcon = UISupport.createImageIcon("/invalid_soap_request_step.png");
        }

        if (unknownRequestIcon == null) {
            unknownRequestIcon = UISupport.createImageIcon("/soap_request_step.png");
        }

        if (disabledRequestIcon == null) {
            disabledRequestIcon = UISupport.createImageIcon("/disabled_request.gif");
        }

    }

    private void initAssertions() {
        assertionsSupport = new AssertionsSupport(this, new AssertableConfig() {

            public TestAssertionConfig addNewAssertion() {
                return mockResponseStepConfig.addNewAssertion();
            }

            public List<TestAssertionConfig> getAssertionList() {
                return mockResponseStepConfig.getAssertionList();
            }

            public void removeAssertion(int ix) {
                mockResponseStepConfig.removeAssertion(ix);
            }

            public TestAssertionConfig insertAssertion(TestAssertionConfig source, int ix) {
                TestAssertionConfig conf = mockResponseStepConfig.insertNewAssertion(ix);
                conf.set(source);
                return conf;
            }
        });
    }

    private void initMockObjects(WsdlTestCase testCase) {
        MockServiceConfig mockServiceConfig = MockServiceConfig.Factory.newInstance();
        mockServiceConfig.setPath(mockResponseStepConfig.getPath());
        mockServiceConfig.setPort(mockResponseStepConfig.getPort());
        mockServiceConfig.setHost(mockResponseStepConfig.getHost());

        mockService = new WsdlTestMockService(this, mockServiceConfig);
        mockService.setName(getName());

        iface = (WsdlInterface) testCase.getTestSuite().getProject()
                .getInterfaceByName(mockResponseStepConfig.getInterface());
        if (iface == null) {
        } else {
            iface.addInterfaceListener(interfaceListener);

            mockOperation = (WsdlMockOperation) mockService.addNewMockOperation(iface.getOperationByName(mockResponseStepConfig
                    .getOperation()));

            if (mockResponseStepConfig.getHandleFault()) {
                mockService.setFaultMockOperation(mockOperation);
            }

            if (mockResponseStepConfig.getHandleResponse()) {
                mockService.setDispatchResponseMessages(true);
            }

            mockResponse = mockOperation.addNewMockResponse("MockResponse", false);
            mockResponse.setConfig(mockResponseConfig);

            mockOperation.setDefaultResponse(mockResponse.getName());

            mockResponse.addPropertyChangeListener(this);
            mockResponse.getWsaConfig().addPropertyChangeListener(this);
        }
    }

    public void resetConfigOnMove(TestStepConfig config) {
        super.resetConfigOnMove(config);

        mockResponseStepConfig = (MockResponseStepConfig) config.getConfig().changeType(MockResponseStepConfig.type);
        mockResponseConfig = mockResponseStepConfig.getResponse();
        mockResponse.setConfig(mockResponseConfig);
        assertionsSupport.refresh();
    }

    @Override
    public boolean cancel() {
        if (mockRunner != null) {
            mockRunner.stop();
            mockRunner = null;
        }

        if (mockRunListener != null) {
            mockRunListener.cancel();
        }

        return true;
    }

    @Override
    public void prepare(TestCaseRunner testRunner, TestCaseRunContext testRunContext) throws Exception {
        super.prepare(testRunner, testRunContext);

        LoadTestRunner loadTestRunner = (LoadTestRunner) testRunContext
                .getProperty(TestCaseRunContext.LOAD_TEST_RUNNER);
        mockRunListener = new InternalMockRunListener();

        for (TestAssertion assertion : getAssertionList()) {
            assertion.prepare(testRunner, testRunContext);
        }

        if (loadTestRunner == null) {
            mockService.addMockRunListener(mockRunListener);
            //			mockRunner = mockService.start( ( WsdlTestRunContext )testRunContext );
        } else {
            synchronized (STATUS_PROPERTY) {
                mockRunner = (WsdlMockRunner) testRunContext.getProperty("sharedMockServiceRunner");
                if (mockRunner == null) {
                    mockService.addMockRunListener(mockRunListener);
                    mockRunner = mockService.start((WsdlTestRunContext) testRunContext);
                } else {
                    mockRunner.getMockContext().getMockService().addMockRunListener(mockRunListener);
                }
            }
        }

        if (startTestStep instanceof WsdlMockResponseTestStep) {
            System.out.println("Adding StartStepMockRunListener from [" + getName() + "] to [" + startTestStep.getName()
                    + "]");
            startStepMockRunListener = new StartStepMockRunListener(testRunContext,
                    (WsdlMockResponseTestStep) startTestStep);
        }
    }

    protected void initTestMockResponse(TestCaseRunContext testRunContext) {
        if (StringUtils.hasContent(getQuery()) && StringUtils.hasContent(getMatch())) {
            String name = "MockResponse" + Math.random();
            testMockResponse = mockOperation.addNewMockResponse(name, false);
            testMockResponse.setConfig((MockResponseConfig) mockResponse.getConfig().copy());
            testMockResponse.setName(name);

            QueryMatchMockOperationDispatcher dispatcher = (QueryMatchMockOperationDispatcher) mockOperation
                    .setDispatchStyle(MockOperationDispatchStyleConfig.QUERY_MATCH.toString());

            for (QueryMatchMockOperationDispatcher.Query query : dispatcher.getQueries()) {
                dispatcher.deleteQuery(query);
            }

            mockOperation.setDefaultResponse(null);

            QueryMatchMockOperationDispatcher.Query query = dispatcher.addQuery("Match");
            query.setQuery(PropertyExpander.expandProperties(testRunContext, getQuery()));
            query.setMatch(PropertyExpander.expandProperties(testRunContext, getMatch()));
            query.setResponse(testMockResponse.getName());
        } else {
            testMockResponse = mockResponse;
            testMockResponse.setMockResult(null);
        }
    }

    public TestStepResult run(TestCaseRunner testRunner, TestCaseRunContext context) {
        LoadTestRunner loadTestRunner = (LoadTestRunner) context.getProperty(TestCaseRunContext.LOAD_TEST_RUNNER);
        if (loadTestRunner == null) {
            return internalRun((WsdlTestRunContext) context);
        } else {
            // block other threads during loadtesting -> this should be improved!
            //			synchronized( STATUS_PROPERTY )
            {
                if (loadTestRunner.getStatus() == LoadTestRunner.Status.RUNNING) {
                    return internalRun((WsdlTestRunContext) context);
                } else {
                    WsdlSingleMessageExchangeTestStepResult result = new WsdlSingleMessageExchangeTestStepResult(this);
                    result.setStatus(TestStepStatus.UNKNOWN);
                    return result;
                }
            }
        }
    }

    private TestStepResult internalRun(WsdlTestRunContext context) {
        if (iconAnimator != null) {
            iconAnimator.start();
        }

        WsdlSingleMessageExchangeTestStepResult result = new WsdlSingleMessageExchangeTestStepResult(this);

        try {
            this.lastResult = null;
            mockResponse.setMockResult(null);

            result.startTimer();

            if (!mockRunListener.hasResult()) {
                startListening(context);

                long timeout = getTimeout();
                synchronized (mockRunListener) {
                    mockRunListener.waitForRequest(timeout);
                }
            }

            result.stopTimer();
            if (mockRunner != null && mockRunner.isRunning()) {
                mockRunner.stop();
            }

            AssertedWsdlMockResultMessageExchange messageExchange = new AssertedWsdlMockResultMessageExchange(
                    mockRunListener.getLastResult());
            result.setMessageExchange(messageExchange);

            if (mockRunListener.getLastResult() != null) {
                lastResult = mockRunListener.getLastResult();
                mockResponse.setMockResult(lastResult);

                context.setProperty(AssertedXPathsContainer.ASSERTEDXPATHSCONTAINER_PROPERTY, messageExchange);
                assertResult(lastResult, context);
            }

            if (mockRunListener.getLastResult() == null) {
                if (mockRunListener.isCanceled()) {
                    result.setStatus(TestStepStatus.CANCELED);
                } else {
                    result.setStatus(TestStepStatus.FAILED);
                    result.addMessage("Timeout occured after " + getTimeout() + " milliseconds");
                }
            } else {
                AssertionStatus status = getAssertionStatus();
                if (status == AssertionStatus.FAILED) {
                    result.setStatus(TestStepStatus.FAILED);

                    if (getAssertionCount() == 0) {
                        result.addMessage("Invalid/empty request");
                    } else {
                        for (int c = 0; c < getAssertionCount(); c++) {
                            WsdlMessageAssertion assertion = getAssertionAt(c);
                            AssertionError[] errors = assertion.getErrors();
                            if (errors != null) {
                                for (AssertionError error : errors) {
                                    result.addMessage("[" + assertion.getName() + "] " + error.getMessage());
                                }
                            }
                        }
                    }
                } else if (status == AssertionStatus.UNKNOWN) {
                    result.setStatus(TestStepStatus.UNKNOWN);
                } else {
                    result.setStatus(TestStepStatus.OK);
                }

                mockRunListener.setLastResult(null);
            }
        } catch (Exception e) {
            result.stopTimer();
            result.setStatus(TestStepStatus.FAILED);
            result.setError(e);
            SoapUI.logError(e);
        } finally {
            if (iconAnimator != null) {
                iconAnimator.stop();
            }
        }

        return result;
    }

    private void assertResult(WsdlMockResult result, SubmitContext context) {
        if (oldStatus == null) {
            oldStatus = getAssertionStatus();
        }

        for (int c = 0; c < getAssertionCount(); c++) {
            WsdlMessageAssertion assertion = getAssertionAt(c);
            if (!assertion.isDisabled()) {
                assertion.assertRequest(new WsdlMockResultMessageExchange(result, getMockResponse()), context);
            }
        }

        AssertionStatus newStatus = getAssertionStatus();
        if (newStatus != oldStatus) {
            notifyPropertyChanged(STATUS_PROPERTY, oldStatus, newStatus);
            oldStatus = newStatus;
        }
    }

    @Override
    public void finish(TestCaseRunner testRunner, TestCaseRunContext testRunContext) {
        if (mockRunListener != null) {
            if (mockRunListener.isWaiting()) {
                mockRunListener.cancel();
            }

            mockService.removeMockRunListener(mockRunListener);
            mockRunListener = null;
        }

        if (startStepMockRunListener != null) {
            startStepMockRunListener.release();
            startStepMockRunListener = null;
        }

        if (testMockResponse != null) {
            if (testMockResponse != mockResponse) {
                mockOperation.removeMockResponse(testMockResponse);
            }

            testMockResponse = null;
        }

        if (mockRunner != null) {
            if (mockRunner.isRunning()) {
                mockRunner.stop();
            }

            mockRunner = null;
        }
    }

    public WsdlMockResult getLastResult() {
        return lastResult;
    }

    public class InternalMockRunListener extends MockRunListenerAdapter {
        private boolean canceled;
        private boolean waiting;
        private WsdlMockResult lastResult;

        public synchronized void onMockResult(MockResult result) {
            System.out.println("in onMockResult for [" + getName() + "] for result " + result.hashCode());

            // is this for us?
            if (this.lastResult == null && waiting && result.getMockResponse() == testMockResponse) {
                waiting = false;
                System.out.println("Got mockrequest to [" + getName() + "]");
                // save
                this.lastResult = (WsdlMockResult) result;
                notifyPropertyChanged("lastResult", null, lastResult);

                // stop runner -> NO, we can't stop, mockengine is still writing
                // response..
                // actually we have to - but this is not a problem if soapUI has been configured to leave the mockengine running
                // in which case it won't terminate the connector during the response
                mockRunner.stop();

                // testMockResponse.setMockResult( null );

                synchronized (this) {
                    notifyAll();
                }
            }
        }

        public void setLastResult(WsdlMockResult lastResult) {
            this.lastResult = lastResult;
        }

        public void cancel() {
            canceled = true;
            if (waiting) {
                synchronized (this) {
                    notifyAll();
                }
            }
            // mockRunListener.onMockResult( null );
        }

        public WsdlMockResult getLastResult() {
            return lastResult;
        }

        public boolean isCanceled() {
            return canceled;
        }

        public boolean hasResult() {
            return lastResult != null;
        }

        public boolean isWaiting() {
            return waiting;
        }

        public void setWaiting(boolean waiting) {
            this.waiting = waiting;
        }

        public void waitForRequest(long timeout) throws InterruptedException {
            waiting = true;
            wait(timeout);
        }

        @Override
        public void onMockRunnerStart(MockRunner mockRunner) {
            waiting = false;
            lastResult = null;
            canceled = false;
        }
    }

    public WsdlMockResponse getMockResponse() {
        return mockResponse;
    }

    public void setPort(int port) {
        int old = getPort();
        mockService.setPort(port);
        mockResponseStepConfig.setPort(port);
        notifyPropertyChanged("port", old, port);
    }

    public String getPath() {
        return mockResponseStepConfig.getPath();
    }

    public String getHost() {
        return mockResponseStepConfig.getHost();
    }

    public long getContentLength() {
        return mockResponse == null ? 0 : mockResponse.getContentLength();
    }

    public int getPort() {
        return mockResponseStepConfig.getPort();
    }

    public String getEncoding() {
        return mockResponse.getEncoding();
    }

    public void setEncoding(String encoding) {
        String old = getEncoding();
        mockResponse.setEncoding(encoding);
        notifyPropertyChanged("encoding", old, encoding);
    }

    public boolean isMtomEnabled() {
        return mockResponse.isMtomEnabled();
    }

    public void setMtomEnabled(boolean enabled) {
        if (isMtomEnabled() == enabled) {
            return;
        }
        mockResponse.setMtomEnabled(enabled);

        notifyPropertyChanged("mtomEnabled", !enabled, enabled);
    }

    public String getOutgoingWss() {
        return mockResponse.getOutgoingWss();
    }

    public void setOutgoingWss(String outgoingWss) {
        String old = getOutgoingWss();
        mockResponse.setOutgoingWss(outgoingWss);
        notifyPropertyChanged("outgoingWss", old, outgoingWss);
    }

    public void setQuery(String s) {
        String old = getQuery();
        mockResponseStepConfig.setQuery(s);
        notifyPropertyChanged("query", old, s);
    }

    public String getQuery() {
        return mockResponseStepConfig.getQuery();
    }

    public String getMatch() {
        return mockResponseStepConfig.getMatch();
    }

    public void setMatch(String s) {
        String old = getMatch();
        mockResponseStepConfig.setMatch(s);
        notifyPropertyChanged("match", old, s);
    }

    public String getStartStep() {
        return startTestStep == null ? "" : startTestStep.getName();
    }

    public void setStartStep(String startStep) {
        String old = getStartStep();
        if (startTestStep != null) {
            startTestStep.removePropertyChangeListener(this);
            startTestStep = null;
        }

        if (startStep != null) {
            startTestStep = getTestCase().getTestStepByName(startStep);
            if (startTestStep != null) {
                startTestStep.addPropertyChangeListener(this);
            }
        }

        mockResponseStepConfig.setStartStep(startStep);
        notifyPropertyChanged("startStep", old, startStep);
    }

    public boolean isRemoveEmptyContent() {
        return mockResponse.isRemoveEmptyContent();
    }

    public boolean isStripWhitespaces() {
        return mockResponse.isStripWhitespaces();
    }

    public void setRemoveEmptyContent(boolean removeEmptyContent) {
        mockResponse.setRemoveEmptyContent(removeEmptyContent);
    }

    public void setStripWhitespaces(boolean stripWhitespaces) {
        mockResponse.setStripWhitespaces(stripWhitespaces);
    }

    public void setPath(String path) {
        mockService.setPath(path);
        mockResponseStepConfig.setPath(path);
    }

    public void setHost(String host) {
        mockService.setHost(host);
        mockResponseStepConfig.setHost(host);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == mockResponse || evt.getSource() == mockResponse.getWsaConfig()) {
            if (!evt.getPropertyName().equals(WsdlMockResponse.ICON_PROPERTY)) {
                mockResponse.beforeSave();
                mockResponseConfig.set(mockResponse.getConfig());
            }

            notifyPropertyChanged(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        } else if (evt.getSource() == getTestCase() && evt.getPropertyName().equals("testSteps")
                && evt.getNewValue() == null && evt.getOldValue() == startTestStep && startTestStep != null) {
            setStartStep(null);
        } else if (evt.getSource() == startTestStep && evt.getPropertyName().equals(WsdlTestStep.NAME_PROPERTY)) {
            mockResponseStepConfig.setStartStep(String.valueOf(evt.getNewValue()));
        }
    }

    public WsdlMessageAssertion addAssertion(String assertionName) {
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();

        try {
            TestAssertionConfig assertionConfig = mockResponseStepConfig.addNewAssertion();
            assertionConfig.setType(TestAssertionRegistry.getInstance().getAssertionTypeForName(assertionName));

            WsdlMessageAssertion assertion = assertionsSupport.addWsdlAssertion(assertionConfig);
            assertionsSupport.fireAssertionAdded(assertion);

            if (getMockResponse().getMockResult() != null) {
                WsdlMockResult mockResult = (WsdlMockResult) getMockResponse().getMockResult();
                WsdlMockResultMessageExchange messageExchange
                        = new WsdlMockResultMessageExchange(mockResult, getMockResponse());
                assertion.assertRequest(messageExchange, new WsdlSubmitContext(this));
                notifier.notifyChange();
            }

            return assertion;
        } catch (Exception e) {
            SoapUI.logError(e);
            return null;
        }
    }

    public void addAssertionsListener(AssertionsListener listener) {
        assertionsSupport.addAssertionsListener(listener);
    }

    public WsdlMessageAssertion getAssertionAt(int c) {
        return assertionsSupport.getAssertionAt(c);
    }

    public int getAssertionCount() {
        return assertionsSupport.getAssertionCount();
    }

    public void removeAssertionsListener(AssertionsListener listener) {
        assertionsSupport.removeAssertionsListener(listener);
    }

    public AssertionStatus getAssertionStatus() {
        AssertionStatus currentStatus = AssertionStatus.UNKNOWN;
        int cnt = getAssertionCount();
        if (cnt == 0) {
            return currentStatus;
        }

        if (mockResponse.getMockResult() != null) {
            if (mockResponse.getMockResult().getMockRequest() == null) {
                currentStatus = AssertionStatus.FAILED;
            }
        } else {
            return currentStatus;
        }

        for (int c = 0; c < cnt; c++) {
            WsdlMessageAssertion assertion = getAssertionAt(c);
            if (assertion.isDisabled()) {
                continue;
            }

            if (assertion.getStatus() == AssertionStatus.FAILED) {
                currentStatus = AssertionStatus.FAILED;
                break;
            }
        }

        if (currentStatus == AssertionStatus.UNKNOWN) {
            currentStatus = AssertionStatus.VALID;
        }

        return currentStatus;
    }

    public void removeAssertion(TestAssertion assertion) {
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();

        try {
            assertionsSupport.removeAssertion((WsdlMessageAssertion) assertion);
        } finally {
            ((WsdlMessageAssertion) assertion).release();
            notifier.notifyChange();
        }
    }

    public TestAssertion moveAssertion(int ix, int offset) {
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();
        WsdlMessageAssertion assertion = getAssertionAt(ix);
        try {
            return assertionsSupport.moveAssertion(ix, offset);
        } finally {
            ((WsdlMessageAssertion) assertion).release();
            notifier.notifyChange();
        }
    }

    public String getAssertableContentAsXml() {
        return getAssertableContent();
    }
    public String getAssertableContent() {
        MockResult mockResult = getMockResponse().getMockResult();
        return mockResult == null ? null : mockResult.getMockRequest().getRequestContent();
    }

    public TestStep getTestStep() {
        return this;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        if (mockService != null) {
            mockService.setName(getName());
        }
    }

    public WsdlInterface getInterface() {
        return getOperation().getInterface();
    }

    public WsdlOperation getOperation() {
        return getMockResponse().getMockOperation().getOperation();
    }

    public void setInterface(String string) {
        WsdlInterface iface = (WsdlInterface) getTestCase().getTestSuite().getProject().getInterfaceByName(string);
        if (iface != null) {
            mockResponseStepConfig.setInterface(iface.getName());
            WsdlOperation operation = iface.getOperationAt(0);
            mockResponseStepConfig.setOperation(operation.getName());
            mockOperation.setOperation(operation);
        }
    }

    public void setOperation(String string) {
        WsdlOperation operation = getInterface().getOperationByName(string);
        if (operation != null) {
            mockResponseStepConfig.setOperation(string);
            mockOperation.setOperation(operation);
        }
    }

    private class PropertyChangeNotifier {
        private AssertionStatus oldStatus;
        private ImageIcon oldIcon;

        public PropertyChangeNotifier() {
            oldStatus = getAssertionStatus();
            oldIcon = getIcon();
        }

        public void notifyChange() {
            AssertionStatus newStatus = getAssertionStatus();
            ImageIcon newIcon = getIcon();

            if (oldStatus != newStatus) {
                notifyPropertyChanged(STATUS_PROPERTY, oldStatus, newStatus);
            }

            if (oldIcon != newIcon) {
                notifyPropertyChanged(ICON_PROPERTY, oldIcon, getIcon());
            }
        }
    }

    @Override
    public void release() {
        super.release();
        assertionsSupport.release();

        if (mockResponse != null) {
            mockResponse.removePropertyChangeListener(this);
            mockResponse.getWsaConfig().removePropertyChangeListener(this);
        }

        if (mockService != null) {
            mockService.release();
        }

        if (iface != null) {
            iface.getProject().removeProjectListener(projectListener);
            iface.removeInterfaceListener(interfaceListener);
        }

        getTestCase().removeTestRunListener(testRunListener);
        getTestCase().removePropertyChangeListener(this);

        if (startTestStep != null) {
            startTestStep.removePropertyChangeListener(this);
        }

        if (lastResult != null) {
            lastResult = null;
        }
    }

    public AssertableType getAssertableType() {
        return AssertableType.REQUEST;
    }

    @Override
    public Collection<Interface> getRequiredInterfaces() {
        ArrayList<Interface> result = new ArrayList<Interface>();
        result.add(getInterface());
        return result;
    }

    public String getDefaultSourcePropertyName() {
        return "Response";
    }

    public String getDefaultTargetPropertyName() {
        return "Request";
    }

    @Override
    public void beforeSave() {
        super.beforeSave();

        if (mockResponse != null) {
            mockResponse.beforeSave();
            mockResponseConfig.set(mockResponse.getConfig());
        }
    }

    public long getTimeout() {
        return mockResponseStepConfig.getTimeout();
    }

    public void setTimeout(long timeout) {
        long old = getTimeout();
        mockResponseStepConfig.setTimeout(timeout);
        notifyPropertyChanged(TIMEOUT_PROPERTY, old, timeout);
    }

    @Override
    public boolean dependsOn(AbstractWsdlModelItem<?> modelItem) {
        return modelItem == getOperation().getInterface();
    }

    public class InternalProjectListener extends ProjectListenerAdapter {
        public void interfaceRemoved(Interface iface) {
            if (getOperation() != null && getOperation().getInterface().equals(iface)) {
                log.debug("Removing test step due to removed interface");
                (getTestCase()).removeTestStep(WsdlMockResponseTestStep.this);
            }
        }
    }

    public class InternalInterfaceListener extends InterfaceListenerAdapter {
        public void operationRemoved(Operation operation) {
            if (operation == getOperation()) {
                log.debug("Removing test step due to removed operation");
                (getTestCase()).removeTestStep(WsdlMockResponseTestStep.this);
            }
        }

        @Override
        public void operationUpdated(Operation operation) {
            if (operation == getOperation()) {
                setOperation(operation.getName());
            }
        }
    }

    public WsdlMessageAssertion cloneAssertion(TestAssertion source, String name) {
        TestAssertionConfig conf = mockResponseStepConfig.addNewAssertion();
        conf.set(((WsdlMessageAssertion) source).getConfig());
        conf.setName(name);

        WsdlMessageAssertion result = assertionsSupport.addWsdlAssertion(conf);
        assertionsSupport.fireAssertionAdded(result);
        return result;
    }

    public List<TestAssertion> getAssertionList() {
        return new ArrayList<TestAssertion>(assertionsSupport.getAssertionList());
    }

    @Override
    public List<? extends ModelItem> getChildren() {
        return assertionsSupport.getAssertionList();
    }

    public PropertyExpansion[] getPropertyExpansions() {
        List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();

        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(this, mockResponse, "responseContent"));

        StringToStringsMap responseHeaders = mockResponse.getResponseHeaders();
        for (Map.Entry<String, List<String>> headerEntry : responseHeaders.entrySet()) {
            for (String value : headerEntry.getValue()) {
                result.addAll(PropertyExpansionUtils.extractPropertyExpansions(this,
                        new ResponseHeaderHolder(headerEntry.getKey(), value, mockResponse), "value"));
            }
        }
        mockResponse.addWsaPropertyExpansions(result, mockResponse.getWsaConfig(), this);

        return result.toArray(new PropertyExpansion[result.size()]);
    }

    public WsdlMessageAssertion getAssertionByName(String name) {
        return assertionsSupport.getAssertionByName(name);
    }

    public Map<String, TestAssertion> getAssertions() {
        Map<String, TestAssertion> result = new HashMap<String, TestAssertion>();

        for (TestAssertion assertion : getAssertionList()) {
            result.put(assertion.getName(), assertion);
        }

        return result;
    }

    private class AssertedWsdlMockResultMessageExchange extends WsdlMockResultMessageExchange implements
            RequestAssertedMessageExchange, AssertedXPathsContainer {
        private List<AssertedXPath> assertedXPaths;

        public AssertedWsdlMockResultMessageExchange(WsdlMockResult mockResult) {
            super(mockResult, mockResult == null ? null : (WsdlMockResponse) mockResult.getMockResponse());
        }

        public AssertedXPath[] getAssertedXPathsForRequest() {
            return assertedXPaths == null ? new AssertedXPath[0] : assertedXPaths
                    .toArray(new AssertedXPath[assertedXPaths.size()]);
        }

        public void addAssertedXPath(AssertedXPath assertedXPath) {
            if (assertedXPaths == null) {
                assertedXPaths = new ArrayList<AssertedXPath>();
            }

            assertedXPaths.add(assertedXPath);
        }
    }

    public String getDefaultAssertableContent() {
        return getOperation().createRequest(true);
    }

    @Override
    public void resolve(ResolveContext<?> context) {
        super.resolve(context);

        if (mockOperation == null) {
            if (context.hasThisModelItem(this, "Missing Operation in Project", mockResponseStepConfig.getInterface()
                    + "/" + mockResponseStepConfig.getOperation())) {
                return;
            }
            context.addPathToResolve(this, "Missing Operation in Project",
                    mockResponseStepConfig.getInterface() + "/" + mockResponseStepConfig.getOperation()).addResolvers(
                    new RemoveTestStepResolver(this), new ImportInterfaceResolver(this) {

                        @Override
                        protected boolean update() {
                            initMockObjects(getTestCase());
                            initProperties();
                            setDisabled(false);
                            return true;
                        }
                    }, new ChangeOperationResolver(this, "Operation") {

                        @Override
                        public boolean update() {
                            WsdlOperation operation = (WsdlOperation) getSelectedOperation();
                            setInterface(operation.getInterface().getName());
                            setOperation(operation.getName());
                            initMockObjects(getTestCase());
                            initProperties();
                            setDisabled(false);
                            return true;
                        }

                        protected Interface[] getInterfaces(WsdlProject project) {
                            List<WsdlInterface> interfaces = ModelSupport.getChildren(project, WsdlInterface.class);
                            return interfaces.toArray(new Interface[interfaces.size()]);

                        }

                    }
            );
        } else {
            mockOperation.resolve(context);
            if (context.hasThisModelItem(this, "Missing Operation in Project", mockResponseStepConfig.getInterface()
                    + "/" + mockResponseStepConfig.getOperation())) {
                @SuppressWarnings("rawtypes")
                //FIXME need to understand why this needs casting, we need to find the root cause
                        PathToResolve path = (PathToResolve) context.getPath(this, "Missing Operation in Project",
                        mockResponseStepConfig.getInterface() + "/" + mockResponseStepConfig.getOperation());
                path.setSolved(true);
            }
        }
    }

    private synchronized void startListening(TestCaseRunContext runContext) throws Exception {
        if (mockRunner == null) {
            mockRunner = mockService.start((WsdlTestRunContext) runContext);
        }

        if (testMockResponse == null) {
            initTestMockResponse(runContext);
        } else if (!mockRunner.isRunning()) {
            try {
                mockRunner.start();
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }
        if (mockRunListener != null) {
            mockRunListener.setWaiting(true);
        }
    }

    private class InternalTestRunListener extends TestRunListenerAdapter {
        @Override
        public void beforeStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep testStep) {
            if (runContext.getCurrentStep() == startTestStep) {
                if (startTestStep instanceof WsdlMockResponseTestStep) {
                    // do nothing - this is done in the StartStepMockRunListener instead
                } else {
                    if (!isDisabled()) {
                        try {
                            startListening(runContext);
                        } catch (Exception e) {
                            SoapUI.logError(e);
                        }
                    }
                }
            }
        }
    }

    private class StartStepMockRunListener implements PropertyChangeListener {
        private TestCaseRunContext runContext;
        private WsdlMockResponseTestStep wsdlMockResponseTestStep;

        public StartStepMockRunListener(TestCaseRunContext runContext, WsdlMockResponseTestStep wsdlMockResponseTestStep) {
            this.runContext = runContext;
            this.wsdlMockResponseTestStep = wsdlMockResponseTestStep;
            wsdlMockResponseTestStep.addPropertyChangeListener("lastResult", this);
        }

        public void release() {
            wsdlMockResponseTestStep.removePropertyChangeListener("lastResult", this);
            wsdlMockResponseTestStep = null;
            runContext = null;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (!isDisabled()) {
                try {
                    startListening(runContext);
                } catch (Exception e) {
                    SoapUI.logError(e);
                }
            }
        }
    }

}
