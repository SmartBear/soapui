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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RequestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.support.http.HttpRequestTestStep;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.WsdlSinglePartHttpResponse;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.model.support.DefaultTestStepProperty;
import com.eviware.soapui.model.support.InterfaceListenerAdapter;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.model.support.TestStepBeanProperty;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.OperationTestStep;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.support.resolver.ChangeOperationResolver;
import com.eviware.soapui.support.resolver.ImportInterfaceResolver;
import com.eviware.soapui.support.resolver.RemoveTestStepResolver;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.resolver.ResolveContext.PathToResolve;
import com.eviware.soapui.support.types.StringToStringsMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlString;

import javax.swing.ImageIcon;
import javax.xml.namespace.QName;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * WsdlTestStep that executes a WsdlTestRequest
 *
 * @author Ole.Matzura
 */

public class WsdlTestRequestStep extends WsdlTestStepWithProperties implements OperationTestStep,
        PropertyChangeListener, PropertyExpansionContainer, Assertable, HttpRequestTestStep, Securable {
    private final static Logger log = LogManager.getLogger(WsdlTestRequestStep.class);
    private RequestStepConfig requestStepConfig;
    private WsdlTestRequest testRequest;
    private WsdlOperation wsdlOperation;
    private final InternalProjectListener projectListener = new InternalProjectListener();
    private final InternalInterfaceListener interfaceListener = new InternalInterfaceListener();
    private WsdlSubmit<WsdlRequest> submit;

    public WsdlTestRequestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        super(testCase, config, true, forLoadTest);

        if (getConfig().getConfig() != null) {
            requestStepConfig = (RequestStepConfig) getConfig().getConfig().changeType(RequestStepConfig.type);

            wsdlOperation = findWsdlOperation();
            if (wsdlOperation == null) {
                log.error("Could not find operation [" + requestStepConfig.getOperation() + "] in interface ["
                        + requestStepConfig.getInterface() + "] for test request [" + getName() + "] in TestCase ["
                        + getTestCase().getTestSuite().getName() + "/" + getTestCase().getName() + "]");
                // requestStepConfig.setRequest(null);
                setDisabled(true);
            } else {
                initTestRequest(config, forLoadTest);
            }
        } else {
            requestStepConfig = (RequestStepConfig) getConfig().addNewConfig().changeType(RequestStepConfig.type);
        }

        // init properties
        if (testRequest != null) {
            initRequestProperties();
        }
    }

    private void initRequestProperties() {
        addProperty(new TestStepBeanProperty("Endpoint", false, testRequest, "endpoint", this, false));
        addProperty(new TestStepBeanProperty("Username", false, testRequest, "username", this, true));
        addProperty(new TestStepBeanProperty("Password", false, testRequest, "password", this, true));
        addProperty(new TestStepBeanProperty("Domain", false, testRequest, "domain", this, false));
        addProperty(new TestStepBeanProperty("AuthType", false, testRequest, "authType", this, true) {

            @Override
            public String getDefaultValue() {
                // TODO Auto-generated method stub
                return "XXX";
            }
        });

        addProperty(new TestStepBeanProperty("Request", false, testRequest, "requestContent", this, true) {
            @Override
            public String getDefaultValue() {
                return getOperation().createRequest(true);
            }

            @Override
            public SchemaType getSchemaType() {
                try {
                    WsdlInterface iface = getOperation().getInterface();
                    if (WsdlUtils.isRpc(iface.getBinding())) {
                        return WsdlUtils.generateRpcBodyType(getOperation());
                    } else {
                        return iface.getDefinitionContext().getSchemaTypeSystem()
                                .findElement(getOperation().getRequestBodyElementQName()).getType();
                    }
                } catch (Exception e) {
                    SoapUI.logError(e);
                    return XmlString.type;
                }
            }

            @Override
            public QName getType() {
                return getSchemaType().getName();
            }

        });
        addProperty(new TestStepBeanProperty("Response", true, testRequest, "responseContent", this) {
            @Override
            public String getDefaultValue() {
                return getOperation().createResponse(true);
            }
        });

        addProperty(new DefaultTestStepProperty("RawRequest", true, this) {
            @Override
            public String getValue() {
                WsdlResponse response = testRequest.getResponse();
                return response == null ? null : response.getRequestContent();
            }
        });
    }

    private void initTestRequest(TestStepConfig config, boolean forLoadTest) {
        if (!forLoadTest) {
            wsdlOperation.getInterface().getProject().addProjectListener(projectListener);
            wsdlOperation.getInterface().addInterfaceListener(interfaceListener);

            // we need to listen for name changes which happen when
            // interfaces are updated..
            wsdlOperation.getInterface().addPropertyChangeListener(this);
            wsdlOperation.addPropertyChangeListener(this);
        }

        testRequest = new WsdlTestRequest(wsdlOperation, requestStepConfig.getRequest(), this, forLoadTest);
        testRequest.addPropertyChangeListener(this);

        if (config.isSetName()) {
            testRequest.setName(config.getName());
        } else {
            config.setName(testRequest.getName());
        }
    }

    @Override
    public WsdlTestStep clone(WsdlTestCase targetTestCase, String name) {
        beforeSave();

        TestStepConfig config = (TestStepConfig) getConfig().copy();
        RequestStepConfig stepConfig = (RequestStepConfig) config.getConfig().changeType(RequestStepConfig.type);

        while (stepConfig.getRequest().sizeOfAttachmentArray() > 0) {
            stepConfig.getRequest().removeAttachment(0);
        }

        config.setName(name);
        stepConfig.getRequest().setName(name);

        WsdlTestRequestStep result = (WsdlTestRequestStep) targetTestCase.addTestStep(config);
        testRequest.copyAttachmentsTo(result.getTestRequest());

        return result;
    }

    private WsdlOperation findWsdlOperation() {
        WsdlTestCase testCase = getTestCase();
        if (testCase == null || testCase.getTestSuite() == null) {
            return null;
        }

        Project project = testCase.getTestSuite().getProject();
        WsdlOperation operation = null;
        for (int c = 0; c < project.getInterfaceCount(); c++) {
            if (project.getInterfaceAt(c).getName().equals(requestStepConfig.getInterface())) {
                WsdlInterface iface = (WsdlInterface) project.getInterfaceAt(c);
                for (int i = 0; i < iface.getOperationCount(); i++) {
                    if (iface.getOperationAt(i).getName().equals(requestStepConfig.getOperation())) {
                        operation = iface.getOperationAt(i);
                        break;
                    }
                }

                if (operation != null) {
                    break;
                }
            }
        }
        return operation;
    }

    public String getInterfaceName() {
        return requestStepConfig.getInterface();
    }

    public String getOperationName() {
        return requestStepConfig.getOperation();
    }

    @Override
    public void release() {
        super.release();

        if (wsdlOperation == null) {
            wsdlOperation = findWsdlOperation();
        }

        if (wsdlOperation != null) {
            wsdlOperation.removePropertyChangeListener(this);
            wsdlOperation.getInterface().getProject().removeProjectListener(projectListener);
            wsdlOperation.getInterface().removeInterfaceListener(interfaceListener);
            wsdlOperation.getInterface().removePropertyChangeListener(this);
        }

        // could be null if initialization failed..
        if (testRequest != null) {
            testRequest.removePropertyChangeListener(this);
            testRequest.release();
        }
    }

    @Override
    public void resetConfigOnMove(TestStepConfig config) {
        super.resetConfigOnMove(config);

        requestStepConfig = (RequestStepConfig) config.getConfig().changeType(RequestStepConfig.type);
        testRequest.updateConfig(requestStepConfig.getRequest());
    }

    @Override
    public ImageIcon getIcon() {
        return testRequest == null ? null : testRequest.getIcon();
    }

    public WsdlTestRequest getTestRequest() {
        return testRequest;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        testRequest.setName(name);
    }

    public void propertyChange(PropertyChangeEvent event) {
        // TODO Some of these properties should be pulled up as they are common for may steps
        // FIXME The property names shouldn't be hardcoded
        if (event.getSource() == testRequest) {
            if (event.getNewValue() instanceof WsdlSinglePartHttpResponse) {
                WsdlSinglePartHttpResponse response = (WsdlSinglePartHttpResponse) event.getNewValue();
                WsdlRequest request = response.getRequest();
                byte[] rawRequest = response.getRawRequestData();

                firePropertyValueChanged("Response", String.valueOf(response), null);
                firePropertyValueChanged("Request", String.valueOf(request), null);
                firePropertyValueChanged("RawRequest", String.valueOf(rawRequest), null);
            }

            if (event.getPropertyName().equals("domain")) {
                delegatePropertyChange("Domain", event);
            } else if (event.getPropertyName().equals("password")) {
                delegatePropertyChange("Password", event);
            } else if (event.getPropertyName().equals("username")) {
                delegatePropertyChange("Username", event);
            } else if (event.getPropertyName().equals("endpoint")) {
                delegatePropertyChange("Endpoint", event);
            }
        }

        if (event.getSource() == wsdlOperation) {
            if (event.getPropertyName().equals(Operation.NAME_PROPERTY)) {
                requestStepConfig.setOperation((String) event.getNewValue());
            }
        } else if (event.getSource() == wsdlOperation.getInterface()) {
            if (event.getPropertyName().equals(Interface.NAME_PROPERTY)) {
                requestStepConfig.setInterface((String) event.getNewValue());
            }
        } else if (event.getPropertyName().equals(TestAssertion.CONFIGURATION_PROPERTY)
                || event.getPropertyName().equals(TestAssertion.DISABLED_PROPERTY)) {
            if (getTestRequest().getResponse() != null) {
                getTestRequest().assertResponse(new WsdlTestRunContext(this));
            }
        } else {
            if (event.getSource() == testRequest && event.getPropertyName().equals(WsdlTestRequest.NAME_PROPERTY)) {
                if (!super.getName().equals((String) event.getNewValue())) {
                    super.setName((String) event.getNewValue());
                }
            }

            notifyPropertyChanged(event.getPropertyName(), event.getOldValue(), event.getNewValue());
        }
    }

    private void delegatePropertyChange(String customPropertyname, PropertyChangeEvent event) {
        firePropertyValueChanged(customPropertyname, String.valueOf(event.getOldValue()),
                String.valueOf(event.getNewValue()));

    }

    public TestStepResult run(TestCaseRunner runner, TestCaseRunContext runContext) {
        WsdlTestRequestStepResult testStepResult = new WsdlTestRequestStepResult(this);
        testStepResult.startTimer();
        runContext.setProperty(AssertedXPathsContainer.ASSERTEDXPATHSCONTAINER_PROPERTY, testStepResult);

        try {
            submit = testRequest.submit(runContext, false);
            WsdlResponse response = (WsdlResponse) submit.getResponse();

            if (submit.getStatus() != Submit.Status.CANCELED) {
                if (submit.getStatus() == Submit.Status.ERROR) {
                    testStepResult.setStatus(TestStepStatus.FAILED);
                    testStepResult.addMessage(submit.getError().toString());

                    testRequest.setResponse(null, runContext);
                } else if (response == null) {
                    testStepResult.setStatus(TestStepStatus.FAILED);
                    testStepResult.addMessage("Request is missing response");

                    testRequest.setResponse(null, runContext);
                } else {
                    runContext.setProperty(AssertedXPathsContainer.ASSERTEDXPATHSCONTAINER_PROPERTY, testStepResult);
                    testRequest.setResponse(response, runContext);

                    testStepResult.setTimeTaken(response.getTimeTaken());
                    testStepResult.setSize(response.getContentLength());

                    switch (testRequest.getAssertionStatus()) {
                        case FAILED:
                            testStepResult.setStatus(TestStepStatus.FAILED);
                            break;
                        case VALID:
                            testStepResult.setStatus(TestStepStatus.OK);
                            break;
                        case UNKNOWN:
                            testStepResult.setStatus(TestStepStatus.UNKNOWN);
                            break;
                    }

                    testStepResult.setResponse(response, testStepResult.getStatus() != TestStepStatus.FAILED);
                }
            } else {
                testStepResult.setStatus(TestStepStatus.CANCELED);
                testStepResult.addMessage("Request was canceled");
            }

            if (response != null) {
                testStepResult.setRequestContent(response.getRequestContent(),
                        testStepResult.getStatus() != TestStepStatus.FAILED);
            } else {
                testStepResult.setRequestContent(testRequest.getRequestContent(),
                        testStepResult.getStatus() != TestStepStatus.FAILED);
            }
            testStepResult.stopTimer();
        } catch (SubmitException e) {
            testStepResult.setStatus(TestStepStatus.FAILED);
            testStepResult.addMessage("SubmitException: " + e);
            testStepResult.stopTimer();
        } finally {
            submit = null;
        }

        testStepResult.setDomain(PropertyExpander.expandProperties(runContext, testRequest.getDomain()));
        testStepResult.setUsername(PropertyExpander.expandProperties(runContext, testRequest.getUsername()));
        testStepResult.setPassword(PropertyExpander.expandProperties(runContext, testRequest.getPassword()));
        testStepResult.setEndpoint(PropertyExpander.expandProperties(runContext, testRequest.getEndpoint()));
        testStepResult.setEncoding(PropertyExpander.expandProperties(runContext, testRequest.getEncoding()));

        if (testStepResult.getStatus() != TestStepStatus.CANCELED) {
            AssertionStatus assertionStatus = testRequest.getAssertionStatus();
            switch (assertionStatus) {
                case FAILED: {
                    testStepResult.setStatus(TestStepStatus.FAILED);
                    if (getAssertionCount() == 0) {
                        testStepResult.addMessage("Invalid/empty response");
                    } else {
                        for (int c = 0; c < getAssertionCount(); c++) {
                            WsdlMessageAssertion assertion = getAssertionAt(c);
                            AssertionError[] errors = assertion.getErrors();
                            if (errors != null) {
                                for (AssertionError error : errors) {
                                    testStepResult.addMessage("[" + assertion.getName() + "] " + error.getMessage());
                                }
                            }
                        }
                    }

                    break;
                }
                // default : testStepResult.setStatus( TestStepStatus.OK ); break;
            }
        }

        if (testRequest.isDiscardResponse() && !SoapUI.getDesktop().hasDesktopPanel(this)) {
            testRequest.setResponse(null, runContext);
            runContext.removeProperty(BaseHttpRequestTransport.RESPONSE);
        }

        return testStepResult;
    }

    public WsdlMessageAssertion getAssertionAt(int index) {
        return testRequest.getAssertionAt(index);
    }

    public int getAssertionCount() {
        return testRequest == null ? 0 : testRequest.getAssertionCount();
    }

    public WsdlTestRequest getHttpRequest() {
        return testRequest;
    }

    public class InternalProjectListener extends ProjectListenerAdapter {
        @Override
        public void interfaceRemoved(Interface iface) {
            if (wsdlOperation != null && wsdlOperation.getInterface().equals(iface)) {
                log.debug("Removing test step due to removed interface");
                (getTestCase()).removeTestStep(WsdlTestRequestStep.this);
            }
        }
    }

    public class InternalInterfaceListener extends InterfaceListenerAdapter {
        @Override
        public void operationRemoved(Operation operation) {
            if (operation == wsdlOperation) {
                log.debug("Removing test step due to removed operation");
                (getTestCase()).removeTestStep(WsdlTestRequestStep.this);
            }
        }

        @Override
        public void operationUpdated(Operation operation) {
            if (operation == wsdlOperation) {
                requestStepConfig.setOperation(operation.getName());
            }
        }
    }

    @Override
    public boolean cancel() {
        if (submit == null) {
            return false;
        }

        submit.cancel();

        return true;
    }

    @Override
    public Collection<Interface> getRequiredInterfaces() {
        ArrayList<Interface> result = new ArrayList<Interface>();
        result.add(findWsdlOperation().getInterface());
        return result;
    }

    public String getDefaultSourcePropertyName() {
        return "Response";
    }

    public String getDefaultTargetPropertyName() {
        return "Request";
    }

    @Override
    public boolean dependsOn(AbstractWsdlModelItem<?> modelItem) {
        if (modelItem instanceof Interface && testRequest.getOperation().getInterface() == modelItem) {
            return true;
        } else if (modelItem instanceof Operation && testRequest.getOperation() == modelItem) {
            return true;
        }

        return false;
    }

    @Override
    public void beforeSave() {
        super.beforeSave();

        if (testRequest != null) {
            testRequest.beforeSave();
        }
    }

    @Override
    public String getDescription() {
        return testRequest == null ? "<missing>" : testRequest.getDescription();
    }

    @Override
    public void setDescription(String description) {
        if (testRequest != null) {
            testRequest.setDescription(description);
        }
    }

    public void setOperation(WsdlOperation operation) {
        if (wsdlOperation == operation) {
            return;
        }

        WsdlOperation oldOperation = wsdlOperation;
        wsdlOperation = operation;
        requestStepConfig.setInterface(operation.getInterface().getName());
        requestStepConfig.setOperation(operation.getName());

        if (oldOperation != null) {
            oldOperation.removePropertyChangeListener(this);
        }

        wsdlOperation.addPropertyChangeListener(this);

        initTestRequest(this.getConfig(), false);
        testRequest.setOperation(wsdlOperation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends ModelItem> getChildren() {
        return testRequest == null ? Collections.EMPTY_LIST : testRequest.getAssertionList();
    }

    public PropertyExpansion[] getPropertyExpansions() {
        if (testRequest == null) {
            return new PropertyExpansion[0];
        }

        PropertyExpansionsResult result = new PropertyExpansionsResult(this, testRequest);

        result.extractAndAddAll("requestContent");
        result.extractAndAddAll("endpoint");
        result.extractAndAddAll("username");
        result.extractAndAddAll("password");
        result.extractAndAddAll("domain");

        StringToStringsMap requestHeaders = testRequest.getRequestHeaders();
        for (Map.Entry<String, List<String>> headerEntry : requestHeaders.entrySet()) {
            for (String value : headerEntry.getValue()) {
                result.extractAndAddAll(new HttpTestRequestStep.RequestHeaderHolder(headerEntry.getKey(), value,
                        testRequest), "value");
            }
        }

        testRequest.addWsaPropertyExpansions(result, testRequest.getWsaConfig(), this);
        testRequest.addJMSHeaderExpansions(result, testRequest.getJMSHeaderConfig(), this);
        return result.toArray(new PropertyExpansion[result.size()]);
    }

    public TestAssertion addAssertion(String type) {
        WsdlMessageAssertion result = testRequest.addAssertion(type);
        return result;
    }

    public void addAssertionsListener(AssertionsListener listener) {
        testRequest.addAssertionsListener(listener);
    }

    public TestAssertion cloneAssertion(TestAssertion source, String name) {
        return testRequest.cloneAssertion(source, name);
    }

    public String getAssertableContentAsXml() {
        return testRequest.getAssertableContentAsXml();
    }

    public String getAssertableContent() {
        return testRequest.getAssertableContent();
    }

    public AssertableType getAssertableType() {
        return testRequest.getAssertableType();
    }

    public TestAssertion getAssertionByName(String name) {
        return testRequest.getAssertionByName(name);
    }

    public List<TestAssertion> getAssertionList() {
        return testRequest.getAssertionList();
    }

    public AssertionStatus getAssertionStatus() {
        return testRequest.getAssertionStatus();
    }

    public Interface getInterface() {
        return wsdlOperation == null ? null : wsdlOperation.getInterface();
    }

    public WsdlOperation getOperation() {
        return wsdlOperation;
    }

    public TestStep getTestStep() {
        return this;
    }

    public void removeAssertion(TestAssertion assertion) {
        testRequest.removeAssertion(assertion);
    }

    public TestAssertion moveAssertion(int ix, int whereTo) {
        return testRequest.moveAssertion(ix, whereTo);
    }

    public void removeAssertionsListener(AssertionsListener listener) {
        testRequest.removeAssertionsListener(listener);
    }

    public Map<String, TestAssertion> getAssertions() {
        return testRequest.getAssertions();
    }

    @Override
    public void prepare(TestCaseRunner testRunner, TestCaseRunContext testRunContext) throws Exception {
        super.prepare(testRunner, testRunContext);

        testRequest.setResponse(null, testRunContext);

        for (TestAssertion assertion : testRequest.getAssertionList()) {
            assertion.prepare(testRunner, testRunContext);
        }
    }

    public String getDefaultAssertableContent() {
        return testRequest.getDefaultAssertableContent();
    }

    @SuppressWarnings("unchecked")
    public void resolve(ResolveContext<?> context) {
        super.resolve(context);

        if (wsdlOperation == null) {
            // not solved and we have it in list do not add it.
            if (context.hasThisModelItem(this, "Missing SOAP Operation in Project", requestStepConfig.getInterface()
                    + "/" + requestStepConfig.getOperation())) {
                return;
            }

            context.addPathToResolve(this, "Missing SOAP Operation in Project",
                    requestStepConfig.getInterface() + "/" + requestStepConfig.getOperation()).addResolvers(
                    new RemoveTestStepResolver(this), new ImportInterfaceResolver(this) {

                        @Override
                        protected boolean update() {
                            wsdlOperation = findWsdlOperation();
                            if (wsdlOperation == null) {
                                return false;
                            }

                            initTestRequest(getConfig(), false);
                            initRequestProperties();
                            setDisabled(false);
                            return true;
                        }
                    }, new ChangeOperationResolver(this, "Operation") {

                        @Override
                        public boolean update() {
                            WsdlOperation wsdlOperation = (WsdlOperation) getSelectedOperation();
                            if (wsdlOperation == null) {
                                return false;
                            }

                            setOperation(wsdlOperation);
                            initTestRequest(getConfig(), false);
                            initRequestProperties();
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
            testRequest.resolve(context);
            if (context.hasThisModelItem(this, "Missing SOAP Operation in Project", requestStepConfig.getInterface()
                    + "/" + requestStepConfig.getOperation())) {
                PathToResolve path = context.getPath(this, "Missing SOAP Operation in Project",
                        requestStepConfig.getInterface() + "/" + requestStepConfig.getOperation());
                path.setSolved(true);
            }
        }
    }

}
