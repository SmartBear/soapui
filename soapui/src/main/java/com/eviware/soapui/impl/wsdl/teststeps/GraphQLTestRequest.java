package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.GraphQLTestRequestConfig;
import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.support.AbstractHttpOperation;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.HttpAttachmentPart;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertableConfig;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertionsSupport;
import com.eviware.soapui.impl.wsdl.support.jms.header.JMSHeaderConfig;
import com.eviware.soapui.impl.wsdl.support.jms.property.JMSPropertiesConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.monitor.TestMonitor;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.parser.Parser;

import javax.swing.ImageIcon;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphQLTestRequest extends AbstractHttpRequest<GraphQLTestRequestConfig> implements GraphQLTestRequestInterface {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String ICON_PATH = "/graphql-request.png";
    private static final String PASS_ICON_PATH = "/graphql-request-pass.png";
    private static final String FAIL_ICON_PATH = "/graphql-request-fail.png";
    private static final String DISABLED_ICON_PATH = "/graphql-request-disabled.png";

    private static final String QUERY = "query";
    private static final String VARIABLES = "variables";
    private static final String OPERATION_NAME = "operationName";

    private ImageIcon validRequestIcon;
    private ImageIcon failedRequestIcon;
    private ImageIcon disabledRequestIcon;
    private ImageIcon unknownRequestIcon;

    private JMSPropertiesConfig jmsPropertiesConfig;
    private final boolean forLoadTest;
    private GraphQLRequestTestStep testStep;
    private PropertyChangeNotifier notifier;
    private AssertionsSupport assertionsSupport;
    private HttpResponseMessageExchange messageExchange;
    private ObjectNode body;
    private XmlBeansRestParamsTestPropertyHolder params;

    public GraphQLTestRequest(GraphQLTestRequestConfig config, GraphQLRequestTestStep testStep, boolean forLoadTest) {
        super(config, null, ICON_PATH, forLoadTest);

        this.forLoadTest = forLoadTest;
        setSettings(new XmlBeansSettingsImpl(this, testStep.getSettings(), config.getSettings()));

        this.testStep = testStep;

        params = new XmlBeansRestParamsTestPropertyHolder(this, RestParametersConfig.Factory.newInstance());
        params.addProperty(QUERY);
        params.addProperty(VARIABLES);
        params.addProperty(OPERATION_NAME);

        initBody();
        initAssertions();

        if (!forLoadTest) {
            initIcons();
        }
    }

    @Override
    public RestRequestInterface.HttpMethod getMethod() {
        String method = getConfig().getMethod();
        return method == null ? RestRequestInterface.HttpMethod.POST : RestRequestInterface.HttpMethod.valueOf(method);
    }

    protected void initIcons() {
        validRequestIcon = UISupport.createImageIcon(PASS_ICON_PATH);
        failedRequestIcon = UISupport.createImageIcon(FAIL_ICON_PATH);
        unknownRequestIcon = UISupport.createImageIcon(ICON_PATH);
        disabledRequestIcon = UISupport.createImageIcon(DISABLED_ICON_PATH);
    }

    protected RequestIconAnimator<?> initIconAnimator() {
        return null;
    }

    private void initBody() {
        ObjectMapper mapper = new ObjectMapper();
        body = mapper.createObjectNode();
        String nullValue = null;
        body.put(QUERY, nullValue);
        body.put(OPERATION_NAME, nullValue);
        body.put(VARIABLES, nullValue);

        String requestContent = super.getRequestContent();
        setBodyRequest(requestContent);
        updateParameters();
    }

    private void initAssertions() {
        assertionsSupport = new AssertionsSupport(testStep, new AssertableConfig() {
            public TestAssertionConfig addNewAssertion() {
                return getConfig().addNewAssertion();
            }

            public List<TestAssertionConfig> getAssertionList() {
                return getConfig().getAssertionList();
            }

            public void removeAssertion(int ix) {
                getConfig().removeAssertion(ix);
            }

            public TestAssertionConfig insertAssertion(TestAssertionConfig source, int ix) {
                TestAssertionConfig conf = getConfig().insertNewAssertion(ix);
                conf.set(source);
                return conf;
            }
        });
    }

    @Override
    public ImageIcon getIcon() {
        if (forLoadTest) {
            return null;
        }

        TestMonitor testMonitor = SoapUI.getTestMonitor();
        if (testMonitor != null
                && (testMonitor.hasRunningLoadTest(getTestStep().getTestCase()) || testMonitor
                .hasRunningSecurityTest(getTestStep().getTestCase()))) {
            return disabledRequestIcon;
        }

        RequestIconAnimator<?> iconAnimator = getIconAnimator();
        if (iconAnimator != null) {
            ImageIcon icon = iconAnimator.getIcon();
            if (icon != iconAnimator.getBaseIcon()) {
                return icon;
            }
        }

        Assertable.AssertionStatus status = getAssertionStatus();
        if (status == Assertable.AssertionStatus.VALID) {
            return validRequestIcon;
        } else if (status == Assertable.AssertionStatus.FAILED) {
            return failedRequestIcon;
        } else if (status == Assertable.AssertionStatus.UNKNOWN) {
            return unknownRequestIcon;
        }

        return unknownRequestIcon;
    }

    @Override
    public HttpAttachmentPart getAttachmentPart(String partName) {
        return null;
    }


    @Override
    public MessagePart.AttachmentPart[] getDefinedAttachmentParts() {
        return new MessagePart.AttachmentPart[0];
    }

    @Override
    public ModelItem getModelItem() {
        return testStep;
    }

    @Override
    public JMSHeaderConfig getJMSHeaderConfig() {
        return null;
    }

    @Override
    public JMSPropertiesConfig getJMSPropertiesConfig() {
        if (jmsPropertiesConfig == null) {
            if (!getConfig().isSetJmsPropertyConfig()) {
                getConfig().addNewJmsPropertyConfig();
            }
            jmsPropertiesConfig = new JMSPropertiesConfig(getConfig().getJmsPropertyConfig(), this);
        }
        return jmsPropertiesConfig;
    }

    private String getBodyString(String fieldName) {
        JsonNode node = body.get(fieldName);
        if (node == null || node.isNull()) {
            return "";
        }
        if (node.isObject()) {
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
            } catch (JsonProcessingException e) {
                return node.toString();
            }
        }
        return node.asText();
    }

    @Override
    public String getVariables() {
        return getBodyString(VARIABLES);
    }

    @Override
    public void setVariables(String variables) {
        body.set(VARIABLES, createVariableNode(variables));
        updateRequestContent();
    }

    private JsonNode createVariableNode(String variables) {
        try {
            return mapper.readTree(variables);
        } catch (Exception ignore) {
        }
        return new TextNode(variables);
    }

    @Override
    public String getQuery() {
        return getBodyString(QUERY);
    }

    @Override
    public void setQuery(String query) {
        String processedQuery = processBodyParameterValue(query);
        body.put(QUERY, processedQuery);
        body.put(OPERATION_NAME, processedQuery == null ? null : getOperationName(processedQuery));
        updateRequestContent();
    }

    public String processBodyParameterValue(String value) {
        return StringUtils.isNullOrEmpty(value) ? null : value;
    }

    private void updateRequestContent() {
        super.setRequestContent(body.toString());
        updateParameters();
    }

    private String getOperationName(String query) {
        Parser parser = new Parser();
        Set operationList = new LinkedHashSet<String>();
        try {
            Document document = parser.parseDocument(query);
            document.getDefinitions().stream().forEach(def -> {
                if (def instanceof OperationDefinition) {
                    String name = ((OperationDefinition) def).getName();
                    if (StringUtils.hasContent(name)) {
                        operationList.add(name);
                    }
                }
            });
        } catch (Exception ignore) {
        }

        final int queryWithFewOperations = 2;
        if (operationList.size() >= queryWithFewOperations) {
            return (String) operationList.stream().findFirst().get();
        }

        return null;
    }

    @Override
    public void setRequestContent(String request) {
        setBodyRequest(request);
        updateRequestContent();
    }

    private void setBodyRequest(String request) {
        ObjectNode bodyNode;
        try {
            JsonNode jsonNode = mapper.readTree(request);
            if ((jsonNode == null) || !jsonNode.isObject()) {
                return;
            }

            bodyNode = (ObjectNode) jsonNode;
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        if (bodyNode == null) {
            return;
        }

        body.set(QUERY, bodyNode.get(QUERY));
        JsonNode variablesNode = bodyNode.get(VARIABLES);
        boolean needUpdateRequestContent = false;
        if (!variablesNode.isObject() && !variablesNode.isNull()) {
            variablesNode = createVariableNode(variablesNode.textValue());
            needUpdateRequestContent = true;
        }
        body.set(VARIABLES, variablesNode);
        body.set(OPERATION_NAME, bodyNode.get(OPERATION_NAME));
        if (needUpdateRequestContent) {
            updateRequestContent();
        }
    }

    private void updateParameters() {
        if (getMethod() == RestRequestInterface.HttpMethod.GET) {
            String query = StringUtils.emptyIfNull(getBodyString(QUERY));
            String operation = StringUtils.emptyIfNull(getBodyString(OPERATION_NAME));
            String variables = StringUtils.emptyIfNull(getBodyString(VARIABLES));

            params.setPropertyValue(QUERY, query);
            params.setPropertyValue(OPERATION_NAME, operation);
            params.setPropertyValue(VARIABLES, variables);

        } else {
            params.setPropertyValue(QUERY, "");
            params.setPropertyValue(OPERATION_NAME, "");
            params.setPropertyValue(VARIABLES, "");
        }
    }

    @Override
    public WsdlSubmit<GraphQLTestRequest> submit(SubmitContext submitContext, boolean async) throws SubmitException {
        String endpoint = PropertyExpander.expandProperties(submitContext, getEndpoint());

        try {
            WsdlSubmit<GraphQLTestRequest> submitter = new WsdlSubmit<>(this, getSubmitListeners(),
                    RequestTransportRegistry.getTransport(endpoint, submitContext));
            submitter.submitRequest(submitContext, async);
            return submitter;
        } catch (Exception e) {
            throw new SubmitException(e.toString());
        }
    }

    @Override
    public MessagePart[] getRequestParts() {
        return new MessagePart[0];
    }

    @Override
    public MessagePart[] getResponseParts() {
        return new MessagePart[0];
    }

    @Override
    public void setMethod(RestRequestInterface.HttpMethod method) {
        RestRequestInterface.HttpMethod oldMethod = getMethod();
        if (oldMethod == method) {
            return;
        }

        if (method != RestRequestInterface.HttpMethod.GET &&
                method != RestRequestInterface.HttpMethod.POST) {
            throw new IllegalArgumentException(String.format("The method %s is not allowed", method.toString()));
        }

        getConfig().setMethod(method.toString());
        notifyPropertyChanged("method", oldMethod, method);
        updateParameters();
    }

    @Override
    public boolean hasRequestBody() {
        return getMethod() == RestRequestInterface.HttpMethod.POST;
    }

    @Override
    public RestParamsPropertyHolder getParams() {
        return params;
    }

    @Override
    public boolean isPostQueryString() {
        return false;
    }

    @Override
    public void setPostQueryString(boolean b) {

    }

    @Override
    public String getResponseContentAsXml() {
        HttpResponse response = getResponse();
        if (response == null) {
            return null;
        }

        return response.getContentAsXml();
    }

    @Override
    public void updateConfig(GraphQLTestRequestConfig request) {
        setConfig(request);

        List<AttachmentConfig> attachmentConfigs = getConfig().getAttachmentList();
        for (int i = 0; i < attachmentConfigs.size(); i++) {
            AttachmentConfig config = attachmentConfigs.get(i);
            getAttachmentsList().get(i).updateConfig(config);
        }

        if (jmsPropertiesConfig != null) {
            jmsPropertiesConfig.setJmsPropertyConfConfig(request.getJmsPropertyConfig());
        }
        assertionsSupport.refresh();
    }

    @Override
    public String getPath() {
        return getEndpoint();
    }

    @Override
    public String getMultiValueDelimiter() {
        return null;
    }

    @Override
    public String getMediaType() {
        return "application/json";
    }

    @Override
    public void setMediaType(String mediaType) {

    }

    @Override
    public TestProperty addProperty(String name) {
        return null;
    }

    @Override
    public TestProperty removeProperty(String propertyName) {
        return null;
    }

    @Override
    public boolean renameProperty(String name, String newName) {
        return false;
    }

    @Override
    public void moveProperty(String propertyName, int targetIndex) {

    }

    @Override
    public String[] getPropertyNames() {
        return new String[0];
    }

    @Override
    public void setPropertyValue(String name, String value) {

    }

    @Override
    public String getPropertyValue(String name) {
        return params.getPropertyValue(name);
    }

    @Override
    public TestProperty getProperty(String name) {
        return params.getProperty(name);
    }

    @Override
    public Map<String, TestProperty> getProperties() {
        return params.getProperties();
    }

    @Override
    public void addTestPropertyListener(TestPropertyListener listener) {

    }

    @Override
    public void removeTestPropertyListener(TestPropertyListener listener) {

    }

    @Override
    public boolean hasProperty(String name) {
        return params.hasProperty(name);
    }

    @Override
    public int getPropertyCount() {
        return params.getPropertyCount();
    }

    @Override
    public List<TestProperty> getPropertyList() {
        return params.getPropertyList();
    }

    @Override
    public TestProperty getPropertyAt(int index) {
        return params.getPropertyAt(index);
    }

    @Override
    public String getPropertiesLabel() {
        return "GraphQL Params";
    }

    @Override
    public void setResponse(HttpResponse response, SubmitContext context) {
        super.setResponse(response, context);
        assertResponse(context);
    }

    @Override
    public void assertResponse(SubmitContext context) {
        if (notifier == null) {
            notifier = new PropertyChangeNotifier();
        }

        messageExchange = getResponse() == null ? null : new HttpResponseMessageExchange(this);

        if (messageExchange != null) {
            for (WsdlMessageAssertion assertion : assertionsSupport.getAssertionList()) {
                assertion.assertResponse(messageExchange, context);
            }
        }

        notifier.notifyChange();
    }

    @Override
    public String getResponseContentAsString() {
        return getResponse() == null ? null : getResponse().getContentAsString();
    }

    @Override
    public WsdlTestStep getTestStep() {
        return testStep;
    }

    @Override
    public ModelItem getParent() {
        return getTestStep();
    }

    @Override
    public WsdlTestCase getTestCase() {
        return testStep.getTestCase();
    }

    @Override
    public AbstractHttpOperation getOperation() {
        return null;
    }

    @Override
    public WsdlMessageAssertion importAssertion(WsdlMessageAssertion source, boolean overwrite, boolean createCopy, String newName) {
        return assertionsSupport.importAssertion(source, overwrite, createCopy, newName);
    }

    public boolean isDiscardResponse() {
        return getSettings().getBoolean("discardResponse");
    }

    public void setDiscardResponse(boolean discardResponse) {
        getSettings().setBoolean("discardResponse", discardResponse);
    }

    @Override
    public TestAssertion addAssertion(String selection) {
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();

        try {
            WsdlMessageAssertion assertion = assertionsSupport.addWsdlAssertion(selection);
            if (assertion == null) {
                return null;
            }

            if (getResponse() != null) {
                assertion.assertResponse(new HttpResponseMessageExchange(this), new WsdlTestRunContext(testStep));
                notifier.notifyChange();
            }

            return assertion;
        } catch (Exception e) {
            SoapUI.logError(e);
            return null;
        }
    }

    @Override
    public void addAssertionsListener(AssertionsListener listener) {
        assertionsSupport.addAssertionsListener(listener);
    }

    @Override
    public int getAssertionCount() {
        return assertionsSupport.getAssertionCount();
    }

    @Override
    public WsdlMessageAssertion getAssertionAt(int index) {
        return assertionsSupport.getAssertionAt(index);
    }

    @Override
    public void removeAssertionsListener(AssertionsListener listener) {
        assertionsSupport.removeAssertionsListener(listener);
    }

    @Override
    public void removeAssertion(TestAssertion assertion) {
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();

        try {
            assertionsSupport.removeAssertion((WsdlMessageAssertion) assertion);

        } finally {
            ((WsdlMessageAssertion) assertion).release();
            notifier.notifyChange();
        }
    }

    @Override
    public Assertable.AssertionStatus getAssertionStatus() {
        if (messageExchange == null) {
            return Assertable.AssertionStatus.UNKNOWN;
        }
        if (!messageExchange.hasResponse() && getOperation() != null && getOperation().isBidirectional()) {
            return Assertable.AssertionStatus.FAILED;
        } else {
            return assertionsSupport.getAssertionStatus();
        }
    }

    @Override
    public String getAssertableContentAsXml() {
        return getResponseContentAsXml();
    }

    @Override
    public String getAssertableContent() {
        return getResponseContentAsString();
    }

    @Override
    public String getDefaultAssertableContent() {
        return "";
    }

    @Override
    public TestAssertionRegistry.AssertableType getAssertableType() {
        return TestAssertionRegistry.AssertableType.RESPONSE;
    }

    @Override
    public List<TestAssertion> getAssertionList() {
        return new ArrayList<TestAssertion>(assertionsSupport.getAssertionList());
    }

    @Override
    public TestAssertion getAssertionByName(String name) {
        return assertionsSupport.getAssertionByName(name);
    }

    @Override
    public Interface getInterface() {
        return null;
    }

    @Override
    public TestAssertion cloneAssertion(TestAssertion source, String name) {
        return assertionsSupport.cloneAssertion(source, name);
    }

    @Override
    public Map<String, TestAssertion> getAssertions() {
        return assertionsSupport.getAssertions();
    }

    @Override
    public TestAssertion moveAssertion(int ix, int offset) {
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();
        WsdlMessageAssertion assertion = getAssertionAt(ix);
        try {
            return assertionsSupport.moveAssertion(ix, offset);
        } finally {
            assertion.release();
            notifier.notifyChange();
        }
    }

    @Override
    public void resolve(ResolveContext<?> context) {
        super.resolve(context);
        assertionsSupport.resolve(context);
    }

    private class PropertyChangeNotifier {
        private Assertable.AssertionStatus oldStatus;
        private ImageIcon oldIcon;

        public PropertyChangeNotifier() {
            oldStatus = getAssertionStatus();
            oldIcon = getIcon();
        }

        public void notifyChange() {
            Assertable.AssertionStatus newStatus = getAssertionStatus();
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
}
