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
import com.eviware.soapui.config.RestRequestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRepresentation.Type;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.support.RestRequestConverter;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SinglePartHttpResponse;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory.ItemDeletedException;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.model.support.DefaultTestStepProperty;
import com.eviware.soapui.model.support.InterfaceListenerAdapter;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.model.support.TestPropertyListenerAdapter;
import com.eviware.soapui.model.support.TestStepBeanProperty;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepProperty;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ChangeRestMethodResolver;
import com.eviware.soapui.support.resolver.ImportInterfaceResolver;
import com.eviware.soapui.support.resolver.RemoveTestStepResolver;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.resolver.ResolveContext.PathToResolve;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.SchemaType;
import org.w3c.dom.Document;

import javax.swing.ImageIcon;
import javax.xml.namespace.QName;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.eviware.soapui.support.JsonUtil.seemsToBeJsonContentType;

public class RestTestRequestStep extends WsdlTestStepWithProperties implements RestTestRequestStepInterface, Securable {
    private final static Logger log = LogManager.getLogger(RestTestRequestStep.class);
    private RestRequestStepConfig restRequestStepConfig;
    private RestTestRequest testRequest;
    private RestResource restResource;
    private RestMethod restMethod;
    private final InternalProjectListener projectListener = new InternalProjectListener();
    private final InternalInterfaceListener interfaceListener = new InternalInterfaceListener();
    private WsdlSubmit<RestRequest> submit;
    // private final Set<String> requestProperties = new HashSet<String>();
    private final Map<String, RestTestStepProperty> requestProperties = new HashMap<String, RestTestStepProperty>();

    public RestTestRequestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest)
            throws ItemDeletedException {
        super(testCase, config, true, forLoadTest);

        if (getConfig().getConfig() != null) {
            restRequestStepConfig = (RestRequestStepConfig) getConfig().getConfig().changeType(
                    RestRequestStepConfig.type);

            testRequest = buildTestRequest(forLoadTest);
            if (testRequest == null) {
                throw new ItemDeletedException();
            }

            // testRequest = new RestTestRequest( null,
            // requestStepConfig.getRestRequest(), this, forLoadTest );
            testRequest.addPropertyChangeListener(this);
            testRequest.addTestPropertyListener(new InternalTestPropertyListener());

            if (config.isSetName()) {
                testRequest.setName(config.getName());
            } else {
                config.setName(testRequest.getName());
            }
        } else {
            restRequestStepConfig = (RestRequestStepConfig) getConfig().addNewConfig().changeType(
                    RestRequestStepConfig.type);
        }

        // Add request properties
        refreshRequestProperties();

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

            public SchemaType getSchemaType() {
                String requestContent = getTestRequest().getRequestContent();
                if (XmlUtils.seemsToBeXml(requestContent)) {

                    try {
                        // first the DOM of the current request
                        Document dom = XmlUtils.parseXml(requestContent);

                        // get matching representations
                        for (RestRepresentation representation : getTestRequest().getRepresentations(Type.REQUEST,
                                getTestRequest().getMediaType())) {
                            // is request element same as that of representation?
                            if (representation.getElement().equals(XmlUtils.getQName(dom.getDocumentElement()))) {
                                // this is it, return its type
                                return representation.getSchemaType();
                            }
                        }
                    } catch (Throwable e) {
                        SoapUI.logError(e);
                    }
                }

                // found nothing.. fall back
                return super.getSchemaType();
            }

            @Override
            public QName getType() {
                return getSchemaType().getName();
            }

        });

        addProperty(new TestStepBeanProperty(WsdlTestStepWithProperties.RESPONSE_AS_XML, true, testRequest,
                "responseContentAsXml", this) {
            @Override
            public String getDefaultValue() {
                return createDefaultResponseXmlContent();
            }

            public SchemaType getSchemaType() {
                try {
                    // first the DOM of the current request
                    Document dom = XmlUtils.parseXml(getTestRequest().getResponseContentAsXml());

                    // get matching representations
                    for (RestRepresentation representation : getTestRequest().getRepresentations(Type.RESPONSE,
                            getTestRequest().getResponse().getContentType())) {
                        // is request element same as that of representation?
                        if (representation.getElement().equals(XmlUtils.getQName(dom.getDocumentElement()))) {
                            // this is it, return its type
                            return representation.getSchemaType();
                        }
                    }
                } catch (Exception e) {
                    SoapUI.logError(e);
                }

                // found nothing.. fall back
                return super.getSchemaType();
            }

            @Override
            public QName getType() {
                return getSchemaType().getName();
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

        initRestTestRequest();

        if (!forLoadTest && restResource != null) {
            getResource().getService().getProject().addProjectListener(projectListener);
            getResource().getService().addInterfaceListener(interfaceListener);
            getResource().getService().addPropertyChangeListener(this);
            getResource().addPropertyChangeListener(this);
        }

        if (getRestMethod() != null) {
            getRestMethod().addPropertyChangeListener(this);
        }
    }

    private void refreshRequestProperties() {
        for (String key : requestProperties.keySet()) {
            deleteProperty(key, true);
        }
        requestProperties.clear();

        for (String key : testRequest.getProperties().keySet()) {
            requestProperties.put(key, new RestTestStepProperty(key));
            addProperty(requestProperties.get(key), true);
        }
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

    public RestRequestStepConfig getRequestStepConfig() {
        return restRequestStepConfig;
    }

    protected RestTestRequest buildTestRequest(boolean forLoadTest) {
        if (getRestMethod() == null) {
            return null;
        }
        return new RestTestRequest(getRestMethod(), getRequestStepConfig().getRestRequest(), this, forLoadTest);
    }

    private void initRestTestRequest() {
        if (getRestMethod() == null) {
            setDisabled(true);
        } else {
            getTestRequest().setRestMethod(getRestMethod());
        }
    }

    public String getService() {
        return getRequestStepConfig().getService();
    }

    public String getResourcePath() {
        return getRequestStepConfig().getResourcePath();
    }

    protected String createDefaultRawResponseContent() {
        return getResource() == null ? null : getResource().createResponse(true);
    }

    protected String createDefaultResponseXmlContent() {
        return getResource() == null ? null : getResource().createResponse(true);
    }

    protected String createDefaultRequestContent() {
        return getResource() == null ? null : getResource().createRequest(true);
    }

    @Override
    public Collection<Interface> getRequiredInterfaces() {
        ArrayList<Interface> result = new ArrayList<Interface>();
        result.add(findRestResource().getInterface());
        return result;
    }

    private RestResource findRestResource() {
        Project project = ModelSupport.getModelItemProject(this);
        for (Interface iface : project.getInterfaceList()) {
            if (iface.getName().equals(getRequestStepConfig().getService()) && iface instanceof RestService) {
                RestService restService = (RestService) iface;
                // get all resources with the configured path
                for (RestResource resource : restService.getResourcesByFullPath(getRequestStepConfig().getResourcePath())) {
                    // try to find matching method
                    if (getWsdlModelItemByName(resource.getRestMethodList(), getRequestStepConfig().getMethodName()) != null) {
                        return resource;
                    }
                }
            }
        }
        return null;
    }

    private RestMethod findRestMethod() {
        if (!restRequestStepConfig.isSetMethodName()) {
            RestRequestConverter.updateRestTestRequest(this);

            // Must be an old version RestRequest...
            if (getResource() == null) {
                restResource = RestRequestConverter.resolveResource(this);
                if (restResource == null) {
                    return null;
                }
                getRequestStepConfig().setService(restResource.getInterface().getName());
                getRequestStepConfig().setResourcePath(restResource.getFullPath());
            }
            RestMethod method = RestRequestConverter.getMethod(getResource(), getRequestStepConfig().getRestRequest()
                    .selectAttribute(null, "method").newCursor().getTextValue(), getRequestStepConfig().getRestRequest()
                    .getName());
            restRequestStepConfig.setMethodName(method.getName());
            return method;
        } else if (getResource() == null) {
            restResource = RestRequestConverter.resolveResource(this);
            if (restResource == null) {
                return null;
            }
            getRequestStepConfig().setService(restResource.getInterface().getName());
            getRequestStepConfig().setResourcePath(restResource.getFullPath());

            RestMethod m = (RestMethod) getWsdlModelItemByName(getResource().getRestMethodList(), getRequestStepConfig()
                    .getMethodName());
            if (m == null) {
                String mn = null;
                while (mn == null) {
                    mn = UISupport.prompt("Select method in REST Resource [" + restResource.getName() + "]",
                            "Missing REST Method", ModelSupport.getNames(restResource.getRestMethodList()));
                }

                restRequestStepConfig.setMethodName(mn);
                return restResource.getRestMethodByName(mn);
            }
        }

        return (RestMethod) getWsdlModelItemByName(getResource().getRestMethodList(), getRequestStepConfig()
                .getMethodName());
    }

    public RestMethod getRestMethod() {
        if (restMethod == null) {
            restMethod = findRestMethod();
        }
        return restMethod;
    }

    public RestResource getResource() {
        if (restResource == null) {
            restResource = findRestResource();
        }
        return restResource;
    }

    public Operation getOperation() {
        return getResource();
    }

    @Override
    public void release() {
        super.release();

        if (restResource != null) {
            restResource.removePropertyChangeListener(this);
            restResource.getService().getProject().removeProjectListener(projectListener);
            restResource.getService().removeInterfaceListener(interfaceListener);
            restResource.getService().removePropertyChangeListener(this);
        }

        if (restMethod != null) {
            restMethod.removePropertyChangeListener(this);
        }

        if (testRequest != null) {
            testRequest.removePropertyChangeListener(this);
            testRequest.release();
        }
    }

    @Override
    public void resetConfigOnMove(TestStepConfig config) {
        super.resetConfigOnMove(config);

        restRequestStepConfig = (RestRequestStepConfig) config.getConfig().changeType(RestRequestStepConfig.type);
        testRequest.updateConfig(restRequestStepConfig.getRestRequest());
    }

    public void propertyChange(PropertyChangeEvent event) {

        // TODO Some of these properties should be pulled up as they are common for may steps
        // FIXME The property names shouldn't be hardcoded
        if (event.getSource() == testRequest) {
            if (event.getNewValue() instanceof SinglePartHttpResponse) {
                SinglePartHttpResponse response = (SinglePartHttpResponse) event.getNewValue();
                firePropertyValueChanged("Response", String.valueOf(response), null);
                String XMLContent = response.getContentAsXml();
                // FIXME The value should not be hard coded
                firePropertyValueChanged("ResponseAsXml", null, XMLContent);
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

        if (event.getSource() == restResource) {
            if (event.getPropertyName().equals(RestResource.PATH_PROPERTY)) {
                getRequestStepConfig().setResourcePath(restResource.getFullPath());
            } else if (event.getPropertyName().equals("childMethods") && restMethod == event.getOldValue()) {
                // TODO: Convert to HttpTestRequestStep
                log.debug("Removing test step due to removed Rest method");
                getTestCase().removeTestStep(RestTestRequestStep.this);
            }
        } else if (restResource != null && event.getSource() == restResource.getInterface()) {
            if (event.getPropertyName().equals(Interface.NAME_PROPERTY)) {
                getRequestStepConfig().setService((String) event.getNewValue());
            }
        } else if (event.getSource() == restMethod) {
            if (event.getPropertyName().equals(RestMethod.NAME_PROPERTY)) {
                getRequestStepConfig().setMethodName((String) event.getNewValue());
            }
        }
        if (event.getPropertyName().equals(TestAssertion.CONFIGURATION_PROPERTY)
                || event.getPropertyName().equals(TestAssertion.DISABLED_PROPERTY)) {
            if (getTestRequest().getResponse() != null) {
                getTestRequest().assertResponse(new WsdlTestRunContext(this));
            }
        } else {
            if (event.getSource() == testRequest && event.getPropertyName().equals(WsdlTestRequest.NAME_PROPERTY)) {
                if (!super.getName().equals(event.getNewValue())) {
                    super.setName((String) event.getNewValue());
                }
            } else if (event.getSource() == testRequest && event.getPropertyName().equals("restMethod")) {
                refreshRequestProperties();
            }

            notifyPropertyChanged(event.getPropertyName(), event.getOldValue(), event.getNewValue());
        }

        // TODO copy from HttpTestRequestStep super.propertyChange( evt );
    }

    private void delegatePropertyChange(String customPropertyname, PropertyChangeEvent event) {
        firePropertyValueChanged(customPropertyname, String.valueOf(event.getOldValue()),
                String.valueOf(event.getNewValue()));

    }

    public class InternalProjectListener extends ProjectListenerAdapter {
        @Override
        public void interfaceRemoved(Interface iface) {
            if (restResource != null && restResource.getInterface().equals(iface)) {
                log.debug("Removing test step due to removed interface");
                (getTestCase()).removeTestStep(RestTestRequestStep.this);
            }
        }
    }

    public class InternalInterfaceListener extends InterfaceListenerAdapter {
        @Override
        public void operationRemoved(Operation operation) {
            if (operation == restResource) {
                log.debug("Removing test step due to removed operation");
                (getTestCase()).removeTestStep(RestTestRequestStep.this);
            }
        }

        @Override
        public void operationUpdated(Operation operation) {
            if (operation == restResource) {
                // requestStepConfig.setResourcePath( operation.get );
            }
        }
    }

    @Override
    public boolean dependsOn(AbstractWsdlModelItem<?> modelItem) {
        if (modelItem instanceof Interface && getTestRequest().getOperation() != null
                && getTestRequest().getOperation().getInterface() == modelItem) {
            return true;
        } else if (modelItem instanceof Operation && getTestRequest().getOperation() == modelItem) {
            return true;
        }

        return false;
    }

    public void setRestMethod(RestMethod method) {
        if (restMethod == method) {
            return;
        }

        RestMethod oldMethod = restMethod;
        restMethod = method;

        getRequestStepConfig().setService(method.getInterface().getName());
        getRequestStepConfig().setResourcePath(method.getResource().getFullPath());
        getRequestStepConfig().setMethodName(method.getName());

        // new resource?
        RestResource res = findRestResource();
        if (res != getResource()) {
            restResource.removePropertyChangeListener(this);
            restResource.getService().removeInterfaceListener(interfaceListener);
            restResource.getService().removePropertyChangeListener(this);

            restResource = res;

            restResource.getService().addInterfaceListener(interfaceListener);
            restResource.getService().addPropertyChangeListener(this);
            restResource.addPropertyChangeListener(this);
        }

        if (oldMethod != null) {
            oldMethod.removePropertyChangeListener(this);
        }

        restMethod.addPropertyChangeListener(this);
        getTestRequest().setRestMethod(restMethod);
    }

    public RestTestRequest getTestRequest() {
        return testRequest;
    }

    public Interface getInterface() {
        return getResource() == null ? null : getResource().getInterface();
    }

    @Override
    public ImageIcon getIcon() {
        return testRequest == null ? null : testRequest.getIcon();
    }

    public TestStep getTestStep() {
        return this;
    }

    @Override
    public void resolve(ResolveContext<?> context) {
        super.resolve(context);

        if (getRestMethod() == null) {
            if (context.hasThisModelItem(this, "Missing REST Method in Project", getRequestStepConfig().getService()
                    + "/" + getRequestStepConfig().getMethodName())) {
                return;
            }
            context.addPathToResolve(this, "Missing REST Method in Project",
                    getRequestStepConfig().getService() + "/" + getRequestStepConfig().getMethodName()).addResolvers(
                    new RemoveTestStepResolver(this), new ImportInterfaceResolver(this) {
                        @Override
                        protected boolean update() {
                            RestMethod restMethod = findRestMethod();
                            if (restMethod == null) {
                                return false;
                            }

                            setRestMethod(restMethod);
                            initRestTestRequest();
                            setDisabled(false);
                            return true;
                        }

                    }, new ChangeRestMethodResolver(this) {
                        @Override
                        public boolean update() {
                            RestMethod restMethod = getSelectedRestMethod();
                            if (restMethod == null) {
                                return false;
                            }

                            setRestMethod(restMethod);
                            initRestTestRequest();
                            setDisabled(false);
                            return true;
                        }

                        @Override
                        protected Interface[] getInterfaces(WsdlProject project) {
                            List<RestService> interfaces = ModelSupport.getChildren(project, RestService.class);
                            return interfaces.toArray(new Interface[interfaces.size()]);
                        }
                    }
            );
        } else {
            getRestMethod().resolve(context);
            if (context.hasThisModelItem(this, "Missing REST Method in Project", getRequestStepConfig().getService()
                    + "/" + getRequestStepConfig().getMethodName())) {
                @SuppressWarnings("rawtypes")
                PathToResolve path = context.getPath(this, "Missing REST Method in Project", getRequestStepConfig()
                        .getService() + "/" + getRequestStepConfig().getMethodName());
                path.setSolved(true);
            }
        }
    }

    @Override
    public void prepare(TestCaseRunner testRunner, TestCaseRunContext testRunContext) throws Exception {
        super.prepare(testRunner, testRunContext);

        testRequest.setResponse(null, testRunContext);

        for (TestAssertion assertion : testRequest.getAssertionList()) {
            assertion.prepare(testRunner, testRunContext);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends ModelItem> getChildren() {
        return testRequest == null ? Collections.EMPTY_LIST : testRequest.getAssertionList();
    }

	/*
     * @SuppressWarnings("unchecked") public void resolve(ResolveContext<?>
	 * context) { super.resolve(context);
	 * 
	 * if (getResource() == null) { if (context.hasThisModelItem(this,
	 * "Missing REST Resource in Project", getRequestStepConfig() .getService() +
	 * "/" + getRequestStepConfig().getResourcePath())) return;
	 * context.addPathToResolve( this, "Missing REST Resource in Project",
	 * getRequestStepConfig().getService() + "/" +
	 * getRequestStepConfig().getResourcePath()) .addResolvers(new
	 * RemoveTestStepResolver(this), new ImportInterfaceResolver(this) {
	 * 
	 * @Override protected boolean update() { RestResource restResource =
	 * findRestResource(); if (restResource == null) return false;
	 * 
	 * setResource(restResource); initRestTestRequest(); setDisabled(false);
	 * return true; }
	 * 
	 * }, new ChangeOperationResolver(this, "Resource") {
	 * 
	 * @Override public boolean update() { RestResource restResource =
	 * (RestResource) getSelectedOperation(); if (restResource == null) return
	 * false;
	 * 
	 * setResource(restResource); initRestTestRequest(); setDisabled(false);
	 * return true; }
	 * 
	 * protected Interface[] getInterfaces( WsdlProject project) {
	 * List<RestService> interfaces = ModelSupport .getChildren(project,
	 * RestService.class); return interfaces .toArray(new Interface[interfaces
	 * .size()]); } }); } else { getResource().resolve(context); if
	 * (context.hasThisModelItem(this, "Missing REST Resource in Project",
	 * getRequestStepConfig() .getService() + "/" +
	 * getRequestStepConfig().getResourcePath())) { PathToResolve path =
	 * context.getPath(this, "Missing REST Resource in Project",
	 * getRequestStepConfig().getService() + "/" +
	 * getRequestStepConfig().getResourcePath()); path.setSolved(true); } } }
	 */

    public PropertyExpansion[] getPropertyExpansions() {
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

        return result.toArray(new PropertyExpansion[result.size()]);
    }

    public AbstractHttpRequest<?> getHttpRequest() {
        return testRequest;
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

    public WsdlMessageAssertion getAssertionAt(int index) {
        return testRequest.getAssertionAt(index);
    }

    public int getAssertionCount() {
        return testRequest == null ? 0 : testRequest.getAssertionCount();
    }

    public String getDefaultAssertableContent() {
        return testRequest.getDefaultAssertableContent();
    }

    @Override
    public String getDefaultSourcePropertyName() {
        HttpResponse response = testRequest.getResponse();
        return response != null && seemsToBeJsonContentType(response.getContentType()) ? WsdlTestStepWithProperties.RESPONSE :
                WsdlTestStepWithProperties.RESPONSE_AS_XML;
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

    private class InternalTestPropertyListener extends TestPropertyListenerAdapter {
        @Override
        public void propertyAdded(String name) {
            requestProperties.put(name, new RestTestStepProperty(name));
            RestTestRequestStep.this.addProperty(requestProperties.get(name), true);
        }

        @Override
        public void propertyRemoved(String name) {
            requestProperties.remove(name);
            deleteProperty(name, true);
        }

        @Override
        public void propertyRenamed(String oldName, String newName) {
            RestTestStepProperty prop = requestProperties.remove(oldName);
            if (prop != null) {
                prop.setPropertyName(newName);
                requestProperties.put(newName, prop);
            }
            RestTestRequestStep.this.propertyRenamed(oldName);
        }

        @Override
        public void propertyValueChanged(String name, String oldValue, String newValue) {
            firePropertyValueChanged(name, oldValue, newValue);
        }

        @Override
        public void propertyMoved(String name, int oldIndex, int newIndex) {
            firePropertyMoved(name, oldIndex, newIndex);
        }
    }

    private class RestTestStepProperty implements TestStepProperty {
        private String propertyName;

        public RestTestStepProperty(String propertyName) {
            this.propertyName = propertyName;
        }

        public void setPropertyName(String name) {
            propertyName = name;
        }

        public TestStep getTestStep() {
            return RestTestRequestStep.this;
        }

        public String getName() {
            return propertyName;
        }

        public String getDescription() {
            return getTestRequest().getProperty(propertyName).getDescription();
        }

        public String getValue() {
            return getTestRequest().getProperty(propertyName).getValue();
        }

        public String getDefaultValue() {
            return getTestRequest().getProperty(propertyName).getDefaultValue();
        }

        public void setValue(String value) {
            getTestRequest().getProperty(propertyName).setValue(value);
        }

        public boolean isReadOnly() {
            return false;
        }

        public QName getType() {
            return getTestRequest().getProperty(propertyName).getType();
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
            return getTestRequest().getProperty(propertyName).getSchemaType();
        }

    }
}
