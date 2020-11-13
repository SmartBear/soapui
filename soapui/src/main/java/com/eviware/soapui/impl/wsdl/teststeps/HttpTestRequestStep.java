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
import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.config.RequestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestRequestConverter;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.http.HttpRequest;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SinglePartHttpResponse;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.model.support.DefaultTestStepProperty;
import com.eviware.soapui.model.support.TestPropertyListenerAdapter;
import com.eviware.soapui.model.support.TestStepBeanProperty;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepProperty;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.types.StringToStringsMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.SchemaType;

import javax.swing.ImageIcon;
import javax.xml.namespace.QName;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HttpTestRequestStep extends WsdlTestStepWithProperties implements HttpTestRequestStepInterface, Securable {
    @SuppressWarnings("unused")
    private final static Logger log = LogManager.getLogger(HttpTestRequestStep.class);
    private HttpRequestConfig httpRequestConfig;
    private HttpTestRequest testRequest;
    private WsdlSubmit<HttpRequest> submit;

    public HttpTestRequestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        super(testCase, config, true, forLoadTest);

        if (getConfig().getConfig() != null) {
            httpRequestConfig = RestRequestConverter.updateIfNeeded(getConfig().getConfig());

            getConfig().setConfig(httpRequestConfig);
            httpRequestConfig = (HttpRequestConfig) getConfig().getConfig();
            testRequest = buildTestRequest(forLoadTest);
            testRequest.addPropertyChangeListener(this);
            testRequest.addTestPropertyListener(new InternalTestPropertyListener());

            if (config.isSetName()) {
                testRequest.setName(config.getName());
            } else {
                config.setName(testRequest.getName());
            }
        } else {
            httpRequestConfig = (HttpRequestConfig) getConfig().addNewConfig().changeType(HttpRequestConfig.type);
        }

        for (TestProperty property : testRequest.getProperties().values()) {
            addProperty(new RestTestStepProperty((RestParamProperty) property));
        }

        // init default properties
        addProperty(new TestStepBeanProperty("Endpoint", false, testRequest, "endpoint", this, false));
        addProperty(new TestStepBeanProperty("Username", false, testRequest, "username", this, true));
        addProperty(new TestStepBeanProperty("Password", false, testRequest, "password", this, true));
        addProperty(new TestStepBeanProperty("Domain", false, testRequest, "domain", this, false));

        // init properties
        addProperty(new TestStepBeanProperty("Request", false, testRequest, "requestContent", this, true) {
            @Override
            public String getDefaultValue() {
                return createDefaultRequestContent();
            }
        });

        addProperty(new TestStepBeanProperty(WsdlTestStepWithProperties.RESPONSE_AS_XML, true, testRequest,
                "responseContentAsXml", this) {
            @Override
            public String getDefaultValue() {
                return createDefaultResponseXmlContent();
            }
        });

        addProperty(new TestStepBeanProperty("Response", true, testRequest, "responseContentAsString", this) {
            @Override
            public String getDefaultValue() {
                return createDefaultRawResponseContent();
            }
        });

        addProperty(new DefaultTestStepProperty("RawRequest", true, this) {
            @Override
            public String getValue() {
                HttpResponse response = testRequest.getResponse();
                return response == null ? null : response.getRequestContent();
            }
        });
    }

    protected HttpTestRequest buildTestRequest(boolean forLoadTest) {
        return new HttpTestRequest(httpRequestConfig, this, forLoadTest);
    }

    protected String createDefaultRawResponseContent() {
        return "";
    }

    protected String createDefaultResponseXmlContent() {
        return "";
    }

    protected String createDefaultRequestContent() {
        return "";
    }

    public HttpRequestConfig getRequestStepConfig() {
        return httpRequestConfig;
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

    @Override
    public void release() {
        super.release();

        // could be null if initialization failed..
        if (testRequest != null) {
            testRequest.removePropertyChangeListener(this);
            testRequest.release();
        }
    }

    @Override
    public void resetConfigOnMove(TestStepConfig config) {
        super.resetConfigOnMove(config);

        httpRequestConfig = (HttpRequestConfig) config.getConfig().changeType(HttpRequestConfig.type);
        testRequest.updateConfig(httpRequestConfig);
    }

    @Override
    public ImageIcon getIcon() {
        return testRequest == null ? null : testRequest.getIcon();
    }

    public HttpTestRequest getTestRequest() {
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
            if (event.getNewValue() instanceof SinglePartHttpResponse) {
                SinglePartHttpResponse response = (SinglePartHttpResponse) event.getNewValue();
                firePropertyValueChanged("Response", String.valueOf(response), null);
                String XMLCOntent = response.getContentAsXml();
                firePropertyValueChanged("ResponseAsXml", String.valueOf(XMLCOntent), null);
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

        if (event.getPropertyName().equals(TestAssertion.CONFIGURATION_PROPERTY)
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
        RestRequestStepResult testStepResult = new RestRequestStepResult(this);

        try {
            submit = testRequest.submit(runContext, false);
            HttpResponse response = (HttpResponse) submit.getResponse();

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
                    testStepResult.setResponse(response);

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
                }
            } else {
                testStepResult.setStatus(TestStepStatus.CANCELED);
                testStepResult.addMessage("Request was canceled");
            }

            if (response != null) {
                testStepResult.setRequestContent(response.getRequestContent());
                testStepResult.addProperty("URL", response.getURL() == null ? "<missing>" : response.getURL().toString());
                testStepResult.addProperty("Method", String.valueOf(response.getMethod()));
                testStepResult.addProperty("StatusCode", String.valueOf(response.getStatusCode()));
                testStepResult.addProperty("HTTP Version", response.getHttpVersion());
            } else {
                testStepResult.addMessage("Missing Response");
                testStepResult.setRequestContent(testRequest.getRequestContent());
            }
        } catch (SubmitException e) {
            testStepResult.setStatus(TestStepStatus.FAILED);
            testStepResult.addMessage("SubmitException: " + e);
        } finally {
            submit = null;
        }

        testStepResult.setDomain(PropertyExpander.expandProperties(runContext, testRequest.getDomain()));
        testStepResult.setUsername(PropertyExpander.expandProperties(runContext, testRequest.getUsername()));
        testStepResult.setEndpoint(PropertyExpander.expandProperties(runContext, testRequest.getEndpoint()));
        testStepResult.setPassword(PropertyExpander.expandProperties(runContext, testRequest.getPassword()));
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

    @Override
    public boolean cancel() {
        if (submit == null) {
            return false;
        }

        submit.cancel();

        return true;
    }

    @Override
    public boolean dependsOn(AbstractWsdlModelItem<?> modelItem) {
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
                result.extractAndAddAll(new RequestHeaderHolder(headerEntry.getKey(), value, testRequest), "value");
            }
        }

        for (String key : testRequest.getParams().getPropertyNames()) {
            result.extractAndAddAll(new RequestParamHolder(key), "value");
        }

        return result.toArray(new PropertyExpansion[result.size()]);
    }

    public AbstractHttpRequest<?> getHttpRequest() {
        return testRequest;
    }

    public static class RequestHeaderHolder {
        private final String key;
        private final String oldValue;
        private AbstractHttpRequest<?> testRequest;

        public RequestHeaderHolder(String key, String oldValue, AbstractHttpRequest<?> testRequest) {
            this.key = key;
            this.oldValue = oldValue;
            this.testRequest = testRequest;
        }

        public String getValue() {
            return oldValue;
        }

        public void setValue(String value) {
            StringToStringsMap valueMap = testRequest.getRequestHeaders();
            valueMap.replace(key, oldValue, value);
            testRequest.setRequestHeaders(valueMap);
        }
    }

    public class RequestParamHolder {
        private final String name;

        public RequestParamHolder(String name) {
            this.name = name;
        }

        public String getValue() {
            return testRequest.getParams().getPropertyValue(name);
        }

        public void setValue(String value) {
            testRequest.setPropertyValue(name, value);
        }
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
        return null;
    }

    public TestStep getTestStep() {
        return this;
    }

    public void removeAssertion(TestAssertion assertion) {
        testRequest.removeAssertion(assertion);
    }

    public void removeAssertionsListener(AssertionsListener listener) {
        testRequest.removeAssertionsListener(listener);
    }

    public TestAssertion moveAssertion(int ix, int offset) {
        return testRequest.moveAssertion(ix, offset);
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

    public String getDefaultSourcePropertyName() {
        return WsdlTestStepWithProperties.RESPONSE_AS_XML;
    }

    public String getDefaultTargetPropertyName() {
        return "Request";
    }

    public String getDefaultAssertableContent() {
        return testRequest.getDefaultAssertableContent();
    }

    public void resolve(ResolveContext<?> context) {
        super.resolve(context);

        testRequest.resolve(context);
    }

    @Override
    protected void addExternalDependencies(List<ExternalDependency> dependencies) {
        super.addExternalDependencies(dependencies);
        testRequest.addExternalDependencies(dependencies);
    }

    private class InternalTestPropertyListener extends TestPropertyListenerAdapter {
        @Override
        public void propertyAdded(String name) {
            HttpTestRequestStep.this.addProperty(new RestTestStepProperty(getTestRequest().getProperty(name)), true);
        }

        @Override
        public void propertyRemoved(String name) {
            HttpTestRequestStep.this.deleteProperty(name, true);
        }

        @Override
        public void propertyRenamed(String oldName, String newName) {
            HttpTestRequestStep.this.propertyRenamed(oldName);
        }

        @Override
        public void propertyValueChanged(String name, String oldValue, String newValue) {
            HttpTestRequestStep.this.firePropertyValueChanged(name, oldValue, newValue);
        }

        @Override
        public void propertyMoved(String name, int oldIndex, int newIndex) {
            HttpTestRequestStep.this.firePropertyMoved(name, oldIndex, newIndex);
        }
    }

    private class RestTestStepProperty implements TestStepProperty {
        private RestParamProperty property;

        public RestTestStepProperty(RestParamProperty property) {
            this.property = property;
        }

        public TestStep getTestStep() {
            return HttpTestRequestStep.this;
        }

        public String getName() {
            return property.getName();
        }

        public String getDescription() {
            return property.getDescription();
        }

        public String getValue() {
            return property.getValue();
        }

        public String getDefaultValue() {
            return property.getDefaultValue();
        }

        public void setValue(String value) {
            property.setValue(value);
        }

        public boolean isReadOnly() {
            return false;
        }

        public QName getType() {
            return property.getType();
        }

        public ModelItem getModelItem() {
            return getTestRequest();
        }

        @Override
        public boolean isRequestPart() {
            return true;
        }

        @Override
        public SchemaType getSchemaType() {
            return property.getSchemaType();
        }
    }
}
