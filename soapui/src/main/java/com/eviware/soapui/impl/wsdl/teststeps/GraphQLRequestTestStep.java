package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.GraphQLTestRequestConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SinglePartHttpResponse;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.support.DefaultTestStepProperty;
import com.eviware.soapui.model.support.TestStepBeanProperty;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;

import javax.swing.ImageIcon;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Map;

public class GraphQLRequestTestStep extends WsdlTestStepWithProperties implements
        GraphQLTestRequestStepInterface, Securable {

    public static final String ICON_NAME = "/graphql-request.png";

    private GraphQLTestRequestConfig graphQLRequestConfig;
    private GraphQLTestRequest graphQLTestRequest;
    private WsdlSubmit<GraphQLTestRequest> submit;

    public GraphQLRequestTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        super(testCase, config, true, forLoadTest);
        setIcon(UISupport.createImageIcon(ICON_NAME));
        if (getConfig().getConfig() != null) {
            graphQLRequestConfig = (GraphQLTestRequestConfig) getConfig().getConfig().changeType(GraphQLTestRequestConfig.type);
            graphQLTestRequest = new GraphQLTestRequest(graphQLRequestConfig, this, forLoadTest);

            graphQLTestRequest.addPropertyChangeListener(this);

            if (config.isSetName()) {
                graphQLTestRequest.setName(config.getName());
            } else {
                config.setName(graphQLTestRequest.getName());
            }
        } else {
            graphQLRequestConfig = (GraphQLTestRequestConfig) getConfig().addNewConfig().changeType(GraphQLTestRequestConfig.type);
        }

        initProperties();
    }

    private void initProperties() {
        addProperty(new TestStepBeanProperty(WsdlTestStepWithProperties.RESPONSE_AS_XML, true, graphQLTestRequest,
                "responseContentAsXml", this) {
            @Override
            public String getDefaultValue() {
                return "";
            }
        });

        addProperty(new TestStepBeanProperty("Response", true, graphQLTestRequest, "responseContentAsString", this) {
            @Override
            public String getDefaultValue() {
                return "";
            }
        });

        addProperty(new DefaultTestStepProperty("RawRequest", true, this) {
            @Override
            public String getValue() {
                HttpResponse response = graphQLTestRequest.getResponse();
                return response == null ? null : response.getRequestContent();
            }
        });

        addProperty(new DefaultTestStepProperty("RawResponse", true, this) {
            @Override
            public String getValue() {
                HttpResponse response = graphQLTestRequest.getResponse();
                return response == null ? null : new String(response.getRawResponseData());
            }
        });

    }

    @Override
    public PropertyExpansion[] getPropertyExpansions() {
        return new PropertyExpansion[0];
    }

    @Override
    public TestStepResult run(TestCaseRunner testRunner, TestCaseRunContext testRunContext) {
        GraphQLRequestTestStepResult testStepResult = new GraphQLRequestTestStepResult(this);

        try {
            submit = graphQLTestRequest.submit(testRunContext, false);

            HttpResponse response = (HttpResponse) submit.getResponse();
            Submit.Status currentStatus = submit.getStatus();
            if (currentStatus == Submit.Status.ERROR) {
                testStepResult.setStatus(TestStepResult.TestStepStatus.FAILED);
                testStepResult.addMessage(submit.getError().toString());
                testStepResult.setSubmit(submit);

                graphQLTestRequest.setResponse(null, testRunContext);
            } else if (currentStatus == Submit.Status.CANCELED) {
                testStepResult.setStatus(TestStepResult.TestStepStatus.CANCELED);
                testStepResult.addMessage("Request was canceled");
            } else if (response == null) {
                testStepResult.setStatus(TestStepResult.TestStepStatus.FAILED);
                testStepResult.addMessage("Request is missing response");

                graphQLTestRequest.setResponse(null, testRunContext);
            } else {
                graphQLTestRequest.setResponse(response, testRunContext);

                testStepResult.setTimeTaken(response.getTimeTaken());
                testStepResult.setSize(response.getContentLength());
                testStepResult.setResponse(response);

                switch (graphQLTestRequest.getAssertionStatus()) {
                    case FAILED:
                        testStepResult.setStatus(TestStepResult.TestStepStatus.FAILED);
                        break;
                    case VALID:
                        testStepResult.setStatus(TestStepResult.TestStepStatus.OK);
                        break;
                    case UNKNOWN:
                        testStepResult.setStatus(TestStepResult.TestStepStatus.UNKNOWN);
                        break;
                }
            }

            if (response != null) {
                testStepResult.setRequestContent(response.getRequestContent());
                testStepResult.addProperty("URL", response.getURL() == null ? "<missing>" : response.getURL().toString());
                testStepResult.addProperty("Method", String.valueOf(response.getMethod()));
                testStepResult.addProperty("StatusCode", String.valueOf(response.getStatusCode()));
                testStepResult.addProperty("HTTP Version", response.getHttpVersion());
            } else {
                testStepResult.setRequestContent(graphQLTestRequest.getRequestContent());
            }

        } catch (Request.SubmitException e) {
            testStepResult.setStatus(TestStepResult.TestStepStatus.FAILED);
            testStepResult.addMessage("SubmitException: " + e);
        } finally {
            submit = null;
        }

        testStepResult.setEndpoint(PropertyExpander.expandProperties(testRunContext, graphQLTestRequest.getEndpoint()));
        testStepResult.setEncoding(PropertyExpander.expandProperties(testRunContext, graphQLTestRequest.getEncoding()));

        if (testStepResult.getStatus() != TestStepResult.TestStepStatus.CANCELED) {
            Assertable.AssertionStatus assertionStatus = graphQLTestRequest.getAssertionStatus();
            switch (assertionStatus) {
                case FAILED: {
                    testStepResult.setStatus(TestStepResult.TestStepStatus.FAILED);
                    if (getAssertionCount() == 0) {
                        testStepResult.addMessage("Invalid/empty response");
                    } else {
                        for (int i = 0; i < getAssertionCount(); i++) {
                            WsdlMessageAssertion assertion = getAssertionAt(i);
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

        if (graphQLTestRequest.isDiscardResponse() && !SoapUI.getDesktop().hasDesktopPanel(this)) {
            graphQLTestRequest.setResponse(null, testRunContext);
            testRunContext.removeProperty(BaseHttpRequestTransport.RESPONSE);
        }

        return testStepResult;
    }

    @Override
    public GraphQLTestRequest getTestRequest() {
        return graphQLTestRequest;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        graphQLTestRequest.setName(name);
    }

    @Override
    public AbstractHttpRequest<?> getHttpRequest() {
        return graphQLTestRequest;
    }

    @Override
    public TestAssertion addAssertion(String type) {
        return graphQLTestRequest.addAssertion(type);
    }

    @Override
    public void addAssertionsListener(AssertionsListener listener) {
        graphQLTestRequest.addAssertionsListener(listener);
    }

    @Override
    public void removeAssertionsListener(AssertionsListener listener) {
        graphQLTestRequest.removeAssertionsListener(listener);
    }

    @Override
    public int getAssertionCount() {
        return graphQLTestRequest.getAssertionCount();
    }

    @Override
    public WsdlMessageAssertion getAssertionAt(int index) {
        return graphQLTestRequest.getAssertionAt(index);
    }


    @Override
    public void removeAssertion(TestAssertion assertion) {
        graphQLTestRequest.removeAssertion(assertion);
    }

    @Override
    public Assertable.AssertionStatus getAssertionStatus() {
        return graphQLTestRequest.getAssertionStatus();
    }

    @Override
    public String getAssertableContentAsXml() {
        return graphQLTestRequest.getAssertableContentAsXml();
    }

    @Override
    public String getAssertableContent() {
        return graphQLTestRequest.getAssertableContent();
    }

    @Override
    public String getDefaultAssertableContent() {
        return graphQLTestRequest.getDefaultAssertableContent();
    }

    @Override
    public TestAssertionRegistry.AssertableType getAssertableType() {
        return graphQLTestRequest.getAssertableType();
    }

    @Override
    public List<TestAssertion> getAssertionList() {
        return graphQLTestRequest.getAssertionList();
    }

    @Override
    public TestAssertion getAssertionByName(String name) {
        return graphQLTestRequest.getAssertionByName(name);
    }

    @Override
    public TestStep getTestStep() {
        return this;
    }

    @Override
    public Interface getInterface() {
        return null;
    }

    @Override
    public TestAssertion cloneAssertion(TestAssertion source, String name) {
        return graphQLTestRequest.cloneAssertion(source, name);
    }

    @Override
    public Map<String, TestAssertion> getAssertions() {
        return graphQLTestRequest.getAssertions();
    }

    @Override
    public TestAssertion moveAssertion(int ix, int offset) {
        return graphQLTestRequest.moveAssertion(ix, offset);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() == graphQLTestRequest) {
            if (event.getNewValue() instanceof SinglePartHttpResponse) {
                SinglePartHttpResponse response = (SinglePartHttpResponse) event.getNewValue();
                firePropertyValueChanged("Response", String.valueOf(response), null);
                String XMLCOntent = response.getContentAsXml();
                firePropertyValueChanged("ResponseAsXml", String.valueOf(XMLCOntent), null);
            }
        }

        if (event.getPropertyName().equals(TestAssertion.CONFIGURATION_PROPERTY)
                || event.getPropertyName().equals(TestAssertion.DISABLED_PROPERTY)) {
            if (getTestRequest().getResponse() != null) {
                getTestRequest().assertResponse(new WsdlTestRunContext(this));
            }
        } else {
            if (event.getSource() == graphQLTestRequest && event.getPropertyName().equals(WsdlTestRequest.NAME_PROPERTY)) {
                if (!super.getName().equals(event.getNewValue())) {
                    super.setName((String) event.getNewValue());
                }
            }

            notifyPropertyChanged(event.getPropertyName(), event.getOldValue(), event.getNewValue());
        }
    }

    @Override
    public void release() {
        super.release();

        if (graphQLTestRequest != null) {
            graphQLTestRequest.removePropertyChangeListener(this);
            graphQLTestRequest.release();
        }
    }

    @Override
    public void resolve(ResolveContext<?> context) {
        super.resolve(context);
        graphQLTestRequest.resolve(context);
    }

    @Override
    public void beforeSave() {
        super.beforeSave();

        if (graphQLTestRequest != null) {
            graphQLTestRequest.beforeSave();
        }
    }

    @Override
    public ImageIcon getIcon() {
        return graphQLTestRequest == null ? null : graphQLTestRequest.getIcon();
    }

    @Override
    public void resetConfigOnMove(TestStepConfig config) {
        super.resetConfigOnMove(config);

        graphQLRequestConfig = (GraphQLTestRequestConfig) config.getConfig().changeType(GraphQLTestRequestConfig.type);
        graphQLTestRequest.updateConfig(graphQLRequestConfig);
    }
}
