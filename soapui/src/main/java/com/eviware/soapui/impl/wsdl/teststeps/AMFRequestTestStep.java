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
import com.eviware.soapui.config.AMFRequestTestStepConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.amf.AMFRequest;
import com.eviware.soapui.impl.wsdl.panels.teststeps.amf.AMFResponse;
import com.eviware.soapui.impl.wsdl.panels.teststeps.amf.AMFSubmit;
import com.eviware.soapui.impl.wsdl.support.AMFMessageExchange;
import com.eviware.soapui.impl.wsdl.support.XmlBeansPropertiesTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertableConfig;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertionsSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.TestStepBeanProperty;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.SamplerTestStep;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ImageIcon;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.eviware.soapui.impl.wsdl.teststeps.Script.SCRIPT_PROPERTY;

/**
 * @author nebojsa.tasic
 */

public class AMFRequestTestStep extends WsdlTestStepWithProperties implements Assertable, MutableTestPropertyHolder,
        PropertyChangeListener, SamplerTestStep {
    @SuppressWarnings("unused")
    private final static Logger log = LogManager.getLogger(WsdlTestRequestStep.class);
    protected AMFRequestTestStepConfig amfRequestTestStepConfig;
    public final static String amfREQUEST = AMFRequestTestStep.class.getName() + "@amfrequest";
    public static final String STATUS_PROPERTY = WsdlTestRequest.class.getName() + "@status";
    public static final String RESPONSE_PROPERTY = "response";
    public static final String REQUEST_PROPERTY = "request";
    public static final String HTTP_HEADERS_PROPERTY = AMFRequest.class.getName() + "@request-headers";
    public static final String AMF_HEADERS_PROPERTY = AMFRequest.class.getName() + "@amfrequest-amfheaders";
    private AMFSubmit submit;

    private SoapUIScriptEngine scriptEngine;
    private AssertionsSupport assertionsSupport;
    private PropertyChangeNotifier notifier;
    private XmlBeansPropertiesTestPropertyHolder propertyHolderSupport;

    private AMFRequest amfRequest;

    public AMFRequestTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {

        super(testCase, config, true, forLoadTest);

        if (getConfig().getConfig() != null) {
            amfRequestTestStepConfig = (AMFRequestTestStepConfig) getConfig().getConfig().changeType(
                    AMFRequestTestStepConfig.type);

        } else {
            amfRequestTestStepConfig = (AMFRequestTestStepConfig) getConfig().addNewConfig().changeType(
                    AMFRequestTestStepConfig.type);
        }

        if (amfRequestTestStepConfig.getProperties() == null) {
            amfRequestTestStepConfig.addNewProperties();
        }

        amfRequest = new AMFRequest(this, forLoadTest);

        propertyHolderSupport = new XmlBeansPropertiesTestPropertyHolder(this, amfRequestTestStepConfig.getProperties());
        addResponseAsXmlVirtualProperty();

        initAssertions();

        scriptEngine = SoapUIScriptEngineRegistry.create(this);
        scriptEngine.setScript(getScript());
        if (forLoadTest && !isDisabled()) {
            try {
                scriptEngine.compile();
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }
    }

    private void addResponseAsXmlVirtualProperty() {
        TestStepBeanProperty responseProperty = new TestStepBeanProperty(WsdlTestStepWithProperties.RESPONSE_AS_XML,
                false, amfRequest, "responseContent", this) {
            @Override
            public String getDefaultValue() {
                return "";
            }

        };

        propertyHolderSupport.addVirtualProperty(WsdlTestStepWithProperties.RESPONSE_AS_XML, responseProperty);
    }

    public AMFRequestTestStepConfig getAMFRequestTestStepConfig() {
        return amfRequestTestStepConfig;
    }

    @Override
    public WsdlTestStep clone(WsdlTestCase targetTestCase, String name) {
        beforeSave();

        TestStepConfig config = (TestStepConfig) getConfig().copy();
        AMFRequestTestStep result = (AMFRequestTestStep) targetTestCase.addTestStep(config);

        return result;
    }

    @Override
    public void release() {
        super.release();
    }

    public TestStepResult run(TestCaseRunner runner, TestCaseRunContext runContext) {
        AMFTestStepResult testStepResult = new AMFTestStepResult(this);
        testStepResult.startTimer();
        runContext.setProperty(AssertedXPathsContainer.ASSERTEDXPATHSCONTAINER_PROPERTY, testStepResult);

        try {
            if (!initAmfRequest(runContext)) {
                throw new SubmitException("AMF request is not initialised properly !");
            }
            submit = amfRequest.submit(runContext, false);
            AMFResponse response = submit.getResponse();

            if (submit.getStatus() != Submit.Status.CANCELED) {
                if (submit.getStatus() == Submit.Status.ERROR) {
                    testStepResult.setStatus(TestStepStatus.FAILED);
                    testStepResult.addMessage(submit.getError().toString());

                    amfRequest.setResponse(null);
                } else if (response == null) {
                    testStepResult.setStatus(TestStepStatus.FAILED);
                    testStepResult.addMessage("Request is missing response");

                    amfRequest.setResponse(null);
                } else {
                    runContext.setProperty(AssertedXPathsContainer.ASSERTEDXPATHSCONTAINER_PROPERTY, testStepResult);
                    amfRequest.setResponse(response);

                    testStepResult.setTimeTaken(response.getTimeTaken());
                    testStepResult.setSize(response.getContentLength());

                    switch (amfRequest.getAssertionStatus()) {
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
                testStepResult.setRequestContent(response.getRequestContent());
            } else {
                testStepResult.setRequestContent(amfRequest.getRequestContent());
            }

            testStepResult.stopTimer();
        } catch (SubmitException e) {
            testStepResult.setStatus(TestStepStatus.FAILED);
            testStepResult.addMessage("SubmitException: " + e);
            testStepResult.stopTimer();
        } finally {
            submit = null;
        }

        if (testStepResult.getStatus() != TestStepStatus.CANCELED) {
            assertResponse(runContext);

            AssertionStatus assertionStatus = amfRequest.getAssertionStatus();
            switch (assertionStatus) {
                case FAILED: {
                    testStepResult.setStatus(TestStepStatus.FAILED);
                    if (getAssertionCount() == 0) {
                        testStepResult.addMessage("Invalid/empty response");
                    } else {
                        for (int c = 0; c < getAssertionCount(); c++) {
                            TestAssertion assertion = getAssertionAt(c);
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

        if (isDiscardResponse() && !SoapUI.getDesktop().hasDesktopPanel(this)) {
            amfRequest.setResponse(null);
        }

        // FIXME This should not be hard coded
        // FIXME This has to be tested before release
        firePropertyValueChanged("ResponseAsXml", null, testStepResult.getResponseContentAsXml());
        return testStepResult;
    }

    @Override
    public boolean cancel() {
        if (submit == null) {
            return false;
        }

        submit.cancel();

        return true;
    }

    public String getDefaultSourcePropertyName() {
        return "Response";
    }

    private void initAssertions() {
        assertionsSupport = new AssertionsSupport(this, new AssertableConfig() {

            public TestAssertionConfig addNewAssertion() {
                return getAMFRequestTestStepConfig().addNewAssertion();
            }

            public List<TestAssertionConfig> getAssertionList() {
                return getAMFRequestTestStepConfig().getAssertionList();
            }

            public void removeAssertion(int ix) {
                getAMFRequestTestStepConfig().removeAssertion(ix);
            }

            public TestAssertionConfig insertAssertion(TestAssertionConfig source, int ix) {
                TestAssertionConfig conf = getAMFRequestTestStepConfig().insertNewAssertion(ix);
                conf.set(source);
                return conf;
            }
        });
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

            oldStatus = newStatus;
            oldIcon = newIcon;
        }
    }

    public TestAssertion addAssertion(String assertionLabel) {
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();

        try {
            WsdlMessageAssertion assertion = assertionsSupport.addWsdlAssertion(assertionLabel);
            if (assertion == null) {
                return null;
            }

            if (getAMFRequest().getResponse() != null) {
                assertion.assertResponse(new AMFMessageExchange(this, getAMFRequest().getResponse()),
                        new WsdlTestRunContext(this));
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

    public TestAssertion cloneAssertion(TestAssertion source, String name) {
        return assertionsSupport.cloneAssertion(source, name);
    }

    public String getAssertableContentAsXml() {
        return getAssertableContent();
    }

    public String getAssertableContent() {
        return getAMFRequest().getResponse() == null ? null : getAMFRequest().getResponse().getContentAsString();
    }

    public WsdlMessageAssertion importAssertion(WsdlMessageAssertion source, boolean overwrite, boolean createCopy,
                                                String newName) {
        return assertionsSupport.importAssertion(source, overwrite, createCopy, newName);
    }

    public AssertableType getAssertableType() {
        return AssertableType.RESPONSE;
    }

    public TestAssertion getAssertionAt(int c) {
        return assertionsSupport.getAssertionAt(c);
    }

    public TestAssertion getAssertionByName(String name) {
        return assertionsSupport.getAssertionByName(name);
    }

    public int getAssertionCount() {
        return assertionsSupport.getAssertionCount();
    }

    public List<TestAssertion> getAssertionList() {
        return new ArrayList<TestAssertion>(assertionsSupport.getAssertionList());
    }

    public void propertyChange(PropertyChangeEvent arg0) {
        if (arg0.getPropertyName().equals(TestAssertion.CONFIGURATION_PROPERTY)
                || arg0.getPropertyName().equals(TestAssertion.DISABLED_PROPERTY)) {
            if (getAMFRequest().getResponse() != null) {
                assertResponse(new WsdlTestRunContext(this));
            }
        }
    }

    public Map<String, TestAssertion> getAssertions() {
        return assertionsSupport.getAssertions();
    }

    public String getDefaultAssertableContent() {
        return null;
    }

    public AssertionStatus getAssertionStatus() {
        return amfRequest.getAssertionStatus();
    }

    public ImageIcon getIcon() {
        return amfRequest.getIcon();
    }

    public Interface getInterface() {
        return null;
    }

    public TestAssertion moveAssertion(int ix, int offset) {
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();
        TestAssertion assertion = getAssertionAt(ix);
        try {
            return assertionsSupport.moveAssertion(ix, offset);
        } finally {
            ((WsdlMessageAssertion) assertion).release();
            notifier.notifyChange();
        }
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

    public void removeAssertionsListener(AssertionsListener listener) {
        assertionsSupport.removeAssertionsListener(listener);
    }

    public void assertResponse(SubmitContext context) {
        try {
            if (notifier == null) {
                notifier = new PropertyChangeNotifier();
            }

            AMFMessageExchange messageExchange = new AMFMessageExchange(this, getAMFRequest().getResponse());

            // assert!
            for (WsdlMessageAssertion assertion : assertionsSupport.getAssertionList()) {
                assertion.assertResponse(messageExchange, context);
            }

            notifier.notifyChange();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TestProperty addProperty(String name) {
        return propertyHolderSupport.addProperty(name);
    }

    public TestProperty removeProperty(String propertyName) {
        return propertyHolderSupport.removeProperty(propertyName);
    }

    public boolean renameProperty(String name, String newName) {
        return PropertyExpansionUtils.renameProperty(propertyHolderSupport.getProperty(name), newName, getTestCase()) != null;
    }

    // FIXME Remove the overridden methods in TestPropertyHolder
    @Override
    public Map<String, TestProperty> getProperties() {
        return propertyHolderSupport.getProperties();
    }

    @Override
    public TestProperty getProperty(String name) {
        return propertyHolderSupport.getProperty(name);
    }

    @Override
    public TestProperty getPropertyAt(int index) {
        return propertyHolderSupport.getPropertyAt(index);
    }

    @Override
    public int getPropertyCount() {
        return propertyHolderSupport.getPropertyCount();
    }


    @Override
    public List<TestProperty> getPropertyList() {
        return propertyHolderSupport.getPropertyList();
    }

    @Override
    public String[] getPropertyNames() {
        return propertyHolderSupport.getPropertyNames();
    }

    @Override
    public String getPropertyValue(String name) {
        return propertyHolderSupport.getPropertyValue(name);
    }

    @Override
    public void addTestPropertyListener(TestPropertyListener listener) {
        propertyHolderSupport.addTestPropertyListener(listener);
    }

    @Override
    public void removeTestPropertyListener(TestPropertyListener listener) {
        propertyHolderSupport.removeTestPropertyListener(listener);
    }

    @Override
    public boolean hasProperty(String name) {
        return propertyHolderSupport.hasProperty(name);
    }

    @Override
    public void setPropertyValue(String name, String value) {
        propertyHolderSupport.setPropertyValue(name, value);
    }

    public void setPropertyValue(String name, Object value) {
        setPropertyValue(name, String.valueOf(value));
    }

    @Override
    public void moveProperty(String propertyName, int targetIndex) {
        propertyHolderSupport.moveProperty(propertyName, targetIndex);
    }

    public AMFRequest getAMFRequest() {
        return amfRequest;
    }

    public void setResponse(AMFResponse response, SubmitContext context) {
        AMFResponse oldResponse = amfRequest.getResponse();
        amfRequest.setResponse(response);

        notifyPropertyChanged(RESPONSE_PROPERTY, oldResponse, response);
        assertResponse(context);
    }

    public String getScript() {
        return amfRequestTestStepConfig.getScript() != null ? amfRequestTestStepConfig.getScript().getStringValue() : "";
    }

    public void setScript(String script) {
        String old = getScript();
        scriptEngine.setScript(script);
        if (amfRequestTestStepConfig.getScript() == null) {
            amfRequestTestStepConfig.addNewScript();
        }

        amfRequestTestStepConfig.getScript().setStringValue(script);
        notifyPropertyChanged(SCRIPT_PROPERTY, old, script);
    }

    public String getAmfCall() {
        return amfRequestTestStepConfig.getAmfCall();
    }

    public void setAmfCall(String amfCall) {
        String old = getAmfCall();
        amfRequestTestStepConfig.setAmfCall(amfCall);
        notifyPropertyChanged("amfCall", old, amfCall);
    }

    public String getEndpoint() {
        return amfRequestTestStepConfig.getEndpoint();
    }

    public void setEndpoint(String endpoint) {
        String old = getEndpoint();
        amfRequestTestStepConfig.setEndpoint(endpoint);
        notifyPropertyChanged("endpoint", old, endpoint);
    }

    public boolean initAmfRequest(SubmitContext submitContext) {
        amfRequest.setScriptEngine(scriptEngine);
        amfRequest.setAmfCall(PropertyExpander.expandProperties(submitContext, getAmfCall()));
        amfRequest.setEndpoint(PropertyExpander.expandProperties(submitContext, getEndpoint()));
        amfRequest.setScript(getScript());
        amfRequest.setPropertyNames(getPropertyNames());
        amfRequest.setPropertyMap((HashMap<String, TestProperty>) getProperties());
        amfRequest.setHttpHeaders(getHttpHeaders());
        amfRequest.setAmfHeadersString(getAmfHeaders());

        return amfRequest.executeAmfScript(submitContext);
    }

    public void setHttpHeaders(StringToStringsMap httpHeaders) {
        StringToStringsMap old = getHttpHeaders();
        getSettings().setString(HTTP_HEADERS_PROPERTY, httpHeaders.toXml());
        notifyPropertyChanged(HTTP_HEADERS_PROPERTY, old, httpHeaders);
    }

    public StringToStringsMap getHttpHeaders() {
        return StringToStringsMap.fromXml(getSettings().getString(HTTP_HEADERS_PROPERTY, null));
    }

    public void setAmfHeaders(StringToStringMap amfHeaders) {
        StringToStringMap old = getAmfHeaders();
        getSettings().setString(AMF_HEADERS_PROPERTY, amfHeaders.toXml());
        notifyPropertyChanged(AMF_HEADERS_PROPERTY, old, amfHeaders);
    }

    public StringToStringMap getAmfHeaders() {
        return StringToStringMap.fromXml(getSettings().getString(AMF_HEADERS_PROPERTY, null));
    }

    public void resetConfigOnMove(TestStepConfig config) {
        super.resetConfigOnMove(config);
        amfRequestTestStepConfig = (AMFRequestTestStepConfig) config.getConfig().changeType(
                AMFRequestTestStepConfig.type);
        propertyHolderSupport.resetPropertiesConfig(amfRequestTestStepConfig.getProperties());
        // addResponseAsXmlVirtualProperty();
        assertionsSupport.refresh();
    }

    public XmlBeansPropertiesTestPropertyHolder getPropertyHolderSupport() {
        return propertyHolderSupport;
    }

    public TestStep getTestStep() {
        return this;
    }

    public boolean isDiscardResponse() {
        return amfRequest.isDiscardResponse();
    }

    public void setDiscardResponse(boolean discardResponse) {
        amfRequest.setDiscardResponse(discardResponse);
    }

    public TestRequest getTestRequest() {
        return amfRequest;
    }
}
