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

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AnonymousTypeConfig;
import com.eviware.soapui.config.DefinitionCacheConfig;
import com.eviware.soapui.config.OperationConfig;
import com.eviware.soapui.config.SoapVersionTypesConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.config.WsdlInterfaceConfig;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.impl.wsdl.support.InterfaceExternalDependency;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.support.policy.PolicyUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapMessageBuilder;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlLoader;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.impl.wsdl.teststeps.BeanPathPropertySupport;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.settings.WsaSettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.types.StringList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3.x2007.x05.addressing.metadata.AddressingDocument.Addressing;
import org.w3.x2007.x05.addressing.metadata.AnonymousResponsesDocument.AnonymousResponses;
import org.w3.x2007.x05.addressing.metadata.NonAnonymousResponsesDocument.NonAnonymousResponses;
import org.xmlsoap.schemas.ws.x2004.x09.policy.Policy;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * WSDL implementation of Interface, maps to a WSDL Binding
 *
 * @author Ole.Matzura
 */

public class WsdlInterface extends AbstractInterface<WsdlInterfaceConfig> {
    public static final String STYLE_DOCUMENT = "Document";
    public static final String STYLE_RPC = "RPC";

    public static final String JBOSSWS_ACTIONS = "jbossws";
    public static final String WSTOOLS_ACTIONS = "wstools";
    public static final String XML_ACTIONS = "xml";

    private final static Logger log = LogManager.getLogger(WsdlInterface.class);
    private List<WsdlOperation> operations = new ArrayList<WsdlOperation>();
    private WsdlProject project;
    private SoapMessageBuilder soapMessageBuilder;
    private WsdlContext wsdlContext;
    private boolean updating = false;
    private BeanPathPropertySupport definitionProperty;
    private String interfaceAnonymous;
    private String interfaceWsaVersion;
    boolean policyFlag = false;

    public WsdlInterface(WsdlProject project, WsdlInterfaceConfig interfaceConfig) {
        super(interfaceConfig, project, "/interface.png");

        if (!interfaceConfig.isSetWsaVersion()) {
            interfaceConfig.setWsaVersion(WsaVersionTypeConfig.NONE);
        }

        this.project = project;

        List<OperationConfig> operationConfigs = interfaceConfig.getOperationList();
        for (int i = 0; i < operationConfigs.size(); i++) {
            operations.add(new WsdlOperation(this, operationConfigs.get(i)));
        }

        definitionProperty = new BeanPathPropertySupport(this, "definition");
    }

    public WsdlOperation getOperationAt(int index) {
        return operations.get(index);
    }

    public int getOperationCount() {
        return operations.size();
    }

    public WsdlOperation addNewOperation(BindingOperation operation) {
        WsdlOperation operationImpl = new WsdlOperation(this, getConfig().addNewOperation());
        operations.add(operationImpl);

        operationImpl.initFromBindingOperation(operation);
        fireOperationAdded(operationImpl);
        return operationImpl;
    }

    public WsdlProject getProject() {
        return project;
    }

    public void setDefinition(String wsdlUrl) throws Exception {
        setDefinition(wsdlUrl, true);
    }

    public void setDefinition(String wsdlUrl, boolean updateCache) throws Exception {
        String old = definitionProperty.set(wsdlUrl, false);

        if (wsdlContext != null) {
            wsdlContext.setDefinition(definitionProperty.expandUrl(), updateCache);
        }

        notifyPropertyChanged(DEFINITION_PROPERTY, old, wsdlUrl);
        notifyPropertyChanged(UPDATING_PROPERTY, true, false);
    }

    public DefinitionCacheConfig cacheDefinition(WsdlLoader loader) throws Throwable {
        log.debug("Caching definition for [" + loader.getBaseURI() + "]");
        if (getConfig().isSetDefinitionCache()) {
            getConfig().unsetDefinitionCache();
        }

        DefinitionCacheConfig definitionCache = null;
        try {
            definitionCache = getConfig().addNewDefinitionCache();
            definitionCache.set(WsdlUtils.cacheWsdl(loader));
        } catch (Throwable e) {
            getConfig().unsetDefinitionCache();
            throw e;
        }

        return definitionCache;
    }

    public String getDefinition() {
        if (!getConfig().isSetDefinition()) {
            return null;
        }

        String result = definitionProperty.get();

        if (PathUtils.isFilePath(result) && !PathUtils.isRelativePath(result) && !result.startsWith("file:")
                && !result.startsWith("$")) {
            try {
                result = new File(result).toURI().toURL().toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public String getType() {
        return WsdlInterfaceFactory.WSDL_TYPE;
    }

    public boolean isDefinitionShareble() {
        return true;
    }

    public synchronized WsdlContext getWsdlContext() {
        if (wsdlContext == null) {
            wsdlContext = new WsdlContext(PathUtils.expandPath(getDefinition(), this), this);
        }

        return wsdlContext;
    }

    /**
     * Used by importer so we dont need to reload the context after importing..
     *
     * @param wsdlContext
     */

    public void setWsdlContext(WsdlContext wsdlContext) {
        this.wsdlContext = wsdlContext;
        this.wsdlContext.setInterface(this);

        if (soapMessageBuilder != null) {
            soapMessageBuilder.setWsdlContext(wsdlContext);
        }
    }

    public SoapMessageBuilder getMessageBuilder() {
        if (soapMessageBuilder == null) {
            try {
                soapMessageBuilder = new SoapMessageBuilder(this);
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }
        return soapMessageBuilder;
    }

    public void setSoapMessageBuilder(SoapMessageBuilder builder) {
        soapMessageBuilder = builder;
        soapMessageBuilder.setInterface(this);
    }

    public QName getBindingName() {
        return getConfig().getBindingName() == null ? null : QName.valueOf(getConfig().getBindingName());
    }

    public void setBindingName(QName name) {
        getConfig().setBindingName(name.toString());
    }

    public SoapVersion getSoapVersion() {
        if (getConfig().getSoapVersion() == SoapVersionTypesConfig.X_1_2) {
            return SoapVersion.Soap12;
        }

        return SoapVersion.Soap11;
    }

    public void setSoapVersion(SoapVersion version) {
        if (version == SoapVersion.Soap11) {
            getConfig().setSoapVersion(SoapVersionTypesConfig.X_1_1);
        } else if (version == SoapVersion.Soap12) {
            getConfig().setSoapVersion(SoapVersionTypesConfig.X_1_2);
        } else {
            throw new RuntimeException("Unknown soapVersion [" + version + "], must be 1.1 or 1.2");
        }
    }

    public boolean updateDefinition(String url, boolean createRequests) throws Exception {
        WsdlContext.uncache(url);

        WsdlContext newContext = new WsdlContext(url, (WsdlInterface) null);
        if (!newContext.load()) {
            return false;
        }

        BindingTuple tuple = findBinding(newContext);
        if (tuple == null) {
            return false;
        }

        setBindingName(tuple.binding.getQName());

        // update name
        if (getSettings().getBoolean(WsdlSettings.NAME_WITH_BINDING)) {
            setName(tuple.binding.getQName().getLocalPart());
        }

        // update context
        setWsdlContext(newContext);

        transferOperations(tuple.binding, createRequests);

        setDefinition(url, false);

        transferEndpoints(tuple.port);

        updateWsaPolicy(url, newContext);

        getProject().fireInterfaceUpdated(this);

        return true;
    }

    private void updateWsaPolicy(String url, WsdlContext newContext) throws Exception {

        Definition definition = newContext.getDefinition();
        policyFlag = false;
        processPolicy(PolicyUtils.getAttachedPolicy(getBinding(), definition));
        Map<?, ?> serviceMap = definition.getAllServices();
        if (serviceMap.isEmpty()) {
            log.info("Missing services in [" + url + "], check for bindings");
        } else {
            Iterator<?> i = serviceMap.values().iterator();
            while (i.hasNext()) {
                Service service = (Service) i.next();
                Map<?, ?> portMap = service.getPorts();
                Iterator<?> i2 = portMap.values().iterator();
                while (i2.hasNext()) {
                    Port port = (Port) i2.next();
                    processPolicy(PolicyUtils.getAttachedPolicy(port, definition));
                }
            }
        }
    }

    public BindingTuple prepareUpdateDefinition(String url) throws Exception {
        WsdlContext newContext = new WsdlContext(url, getSoapVersion());
        if (!newContext.load()) {
            return null;
        }

        BindingTuple tuple = findBinding(newContext);
        return tuple;
    }

    public void updateDefinition(BindingTuple tuple) throws Exception {
        setBindingName(tuple.binding.getQName());

        if (getConfig().isSetDefinitionCache()) {
            getConfig().unsetDefinitionCache();
        }

        // update name
        if (getSettings().getBoolean(WsdlSettings.NAME_WITH_BINDING)) {
            setName(tuple.binding.getQName().getLocalPart());
        }

        // update context
        wsdlContext = tuple.context;
        wsdlContext.setInterface(this);
        if (soapMessageBuilder != null) {
            soapMessageBuilder.setWsdlContext(wsdlContext);
        }
    }

    public BindingOperation findBindingOperation(Definition definition, String bindingOperationName, String inputName,
                                                 String outputName) {
        Binding binding = definition.getBinding(getBindingName());
        return WsdlUtils.findBindingOperation(binding, bindingOperationName, inputName, outputName);
    }

    public Binding getBinding() {
        try {
            return findBinding(getWsdlContext()).binding;
        } catch (Exception e) {
            SoapUI.logError(e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private BindingTuple findBinding(WsdlContext newContext) throws Exception {
        BindingTuple tuple = new BindingTuple();
        tuple.context = newContext;

        // start by finding the old binding in the new definition
        Definition definition = newContext.getDefinition();
        Map serviceMap = definition.getAllServices();
        Iterator<String> i = serviceMap.keySet().iterator();
        while (i.hasNext()) {
            tuple.service = (Service) serviceMap.get(i.next());
            Map portMap = tuple.service.getPorts();

            Iterator i2 = portMap.keySet().iterator();
            while (i2.hasNext()) {
                tuple.port = (Port) portMap.get(i2.next());
                if (tuple.port.getBinding().getQName().equals(getBindingName())) {
                    tuple.binding = tuple.port.getBinding();
                }
            }

            if (tuple.binding != null) {
                break;
            }
            tuple.service = null;
        }

        if (tuple.service == null && tuple.binding == null) {
            tuple.binding = definition.getBinding(getBindingName());
        }

        // missing matching binding, prompt for new one to use instead (will
        // happen if binding has been renamed)
        if (tuple.binding == null) {
            Map bindings = definition.getAllBindings();

            Object retval = UISupport.prompt("Missing matching binding [" + getBindingName()
                    + "] in definition, select new\nbinding to map to", "Map Binding", bindings.keySet().toArray());

            if (retval == null) {
                return null;
            }

            tuple.binding = (Binding) bindings.get(retval);
        }

        return tuple;
    }

    @SuppressWarnings("unchecked")
    public void transferOperations(Binding binding, boolean createRequests) {
        // prepare for transfer of operations/requests
        List<BindingOperation> newOperations = new ArrayList<BindingOperation>(binding.getBindingOperations());
        Map<String, WsdlOperation> oldOperations = new HashMap<String, WsdlOperation>();
        for (int c = 0; c < operations.size(); c++) {
            oldOperations.put(operations.get(c).getBindingOperationName(), operations.get(c));
        }

        // clear existing from both collections
        for (int c = 0; c < newOperations.size(); c++) {
            BindingOperation newOperation = newOperations.get(c);
            String bindingOperationName = newOperation.getName();
            if (oldOperations.containsKey(bindingOperationName)) {
                log.info("Synchronizing existing operation [" + bindingOperationName + "]");
                WsdlOperation wsdlOperation = oldOperations.get(bindingOperationName);
                WsdlUtils.getAnonymous(wsdlOperation);
                wsdlOperation.initFromBindingOperation(newOperation);
                fireOperationUpdated(wsdlOperation);

                oldOperations.remove(bindingOperationName);
                newOperations.remove(c);
                c--;
            }
        }

        // remove leftover operations
        Iterator<String> i = oldOperations.keySet().iterator();
        while (i.hasNext()) {
            String name = i.next();

            if (newOperations.size() > 0) {
                List<String> list = new ArrayList<String>();
                list.add("none - delete operation");
                for (int c = 0; c < newOperations.size(); c++) {
                    list.add(newOperations.get(c).getName());
                }

                String retval = (String) UISupport.prompt("Binding operation [" + name
                        + "] not found in new interface, select new\nbinding operation to map to", "Map Operation",
                        list.toArray(), "none/cancel - delete operation");

                int ix = retval == null ? -1 : list.indexOf(retval) - 1;

                // delete operation?
                if (ix < 0) {
                    deleteOperation(name);
                }
                // change operation?
                else {
                    BindingOperation newOperation = newOperations.get(ix);
                    WsdlOperation wsdlOperation = oldOperations.get(name);
                    wsdlOperation.initFromBindingOperation(newOperation);
                    fireOperationUpdated(wsdlOperation);
                    newOperations.remove(ix);
                }

                oldOperations.remove(name);
            } else {
                deleteOperation(name);
                oldOperations.remove(name);
            }

            i = oldOperations.keySet().iterator();
        }

        // add leftover new operations
        if (newOperations.size() > 0) {
            for (int c = 0; c < newOperations.size(); c++) {
                BindingOperation newOperation = newOperations.get(c);
                WsdlOperation wsdlOperation = addNewOperation(newOperation);

                if (createRequests) {
                    WsdlRequest request = wsdlOperation.addNewRequest("Request 1");
                    try {
                        request.setRequestContent(wsdlOperation.createRequest(true));
                    } catch (Exception e) {
                        SoapUI.logError(e);
                    }
                }
            }
        }
    }

    public void transferEndpoints(Port port) {
        if (port != null) {
            String endpoint = WsdlUtils.getSoapEndpoint(port);
            if (endpoint != null) {
                StringList list = new StringList(getEndpoints());

                // expand properties..
                for (int c = 0; c < list.size(); c++) {
                    list.set(c, PropertyExpander.expandProperties(this, list.get(c)));
                }

                if (!list.contains(endpoint)) {
                    if (UISupport.confirm("Update existing requests with new endpoint\n[" + endpoint + "]",
                            "Update Definition")) {
                        for (int c = 0; c < getOperationCount(); c++) {
                            Operation operation = getOperationAt(c);

                            for (int ix = 0; ix < operation.getRequestCount(); ix++) {
                                operation.getRequestAt(ix).setEndpoint(endpoint);
                            }
                        }
                    }

                    addEndpoint(endpoint);
                }
            }
        }
    }

    public void deleteOperation(String bindingOperationName) {
        for (int c = 0; c < operations.size(); c++) {
            WsdlOperation wsdlOperation = operations.get(c);
            if (wsdlOperation.getBindingOperationName().equals(bindingOperationName)) {
                log.info("deleting operation [" + bindingOperationName + "]");

                // remove requests first (should this be done by some listener?)
                while (wsdlOperation.getRequestCount() > 0) {
                    wsdlOperation.removeRequest(wsdlOperation.getRequestAt(0));
                }

                operations.remove(c);

                try {
                    fireOperationRemoved(wsdlOperation);
                } finally {
                    wsdlOperation.release();
                    getConfig().removeOperation(c);
                }

                return;
            }
        }
    }

    public void removeOperation(WsdlOperation wsdlOperation) {
        int c = operations.indexOf(wsdlOperation);
        if (c < 0) {
            throw new IllegalArgumentException(wsdlOperation.getName() + " not found");
        }

        log.info("deleting operation [" + wsdlOperation.getName() + "]");

        // remove requests first (should this be done by some listener?)
        while (wsdlOperation.getRequestCount() > 0) {
            wsdlOperation.removeRequest(wsdlOperation.getRequestAt(0));
        }

        operations.remove(c);

        try {
            fireOperationRemoved(wsdlOperation);
        } finally {
            wsdlOperation.release();
            getConfig().removeOperation(c);
        }
    }

    public WsdlOperation getOperationByName(String name) {
        return (WsdlOperation) getWsdlModelItemByName(operations, name);
    }

    public Map<String, Operation> getOperations() {
        Map<String, Operation> result = new HashMap<String, Operation>();
        for (Operation operation : operations) {
            result.put(operation.getName(), operation);
        }

        return result;
    }

    public boolean isCached() {
        if (wsdlContext != null && wsdlContext.isCached()) {
            return true;
        }

        DefinitionCacheConfig cacheConfig = getConfig().getDefinitionCache();
        if (cacheConfig == null || cacheConfig.getRootPart() == null || cacheConfig.sizeOfPartArray() == 0) {
            return false;
        }

        return true;
    }

    public String getStyle() {
        if (wsdlContext == null || !wsdlContext.isLoaded()) {
            return "<not loaded>";
        }

        try {
            Binding binding = wsdlContext.getDefinition().getBinding(getBindingName());
            if (binding == null) {
                return "<missing binding>";
            }

            if (WsdlUtils.isRpc(binding)) {
                return STYLE_RPC;
            } else {
                return STYLE_DOCUMENT;
            }
        } catch (Exception e) {
            SoapUI.logError(e);
            return "<error>";
        }
    }

    @Override
    public void release() {
        super.release();

        for (WsdlOperation operation : operations) {
            operation.release();
        }

        if (wsdlContext != null) {
            wsdlContext.release();
        }
    }

    public List<Operation> getOperationList() {
        return new ArrayList<Operation>(operations);
    }

    public static class BindingTuple {
        public WsdlContext context = null;
        public Service service = null;
        public Port port = null;
        public Binding binding = null;
    }

    public boolean isUpdating() {
        return updating;
    }

    public void setUpdating(boolean updating) {
        if (this.updating == updating) {
            return;
        }

        if (updating) {
            List<AbstractWsdlModelItem<?>> messages = getAllMessages();
            for (AbstractWsdlModelItem<?> modelItem : messages) {
                modelItem.beforeSave();
            }
        }

        boolean oldValue = this.updating;
        this.updating = updating;

        notifyPropertyChanged(UPDATING_PROPERTY, oldValue, updating);
    }

    public List<AbstractWsdlModelItem<?>> getAllMessages() {
        ArrayList<AbstractWsdlModelItem<?>> list = new ArrayList<AbstractWsdlModelItem<?>>();
        getAllMessages(getProject(), list);
        return list;
    }

    private void getAllMessages(ModelItem modelItem, List<AbstractWsdlModelItem<?>> list) {
        if (modelItem instanceof AbstractHttpRequestInterface<?>) {
            AbstractHttpRequest<?> wsdlRequest = (AbstractHttpRequest<?>) modelItem;
            if (wsdlRequest.getOperation().getInterface() == this) {
                list.add(wsdlRequest);
            }
        } else if (modelItem instanceof WsdlTestRequestStep) {
            WsdlTestRequestStep testRequestStep = (WsdlTestRequestStep) modelItem;
            WsdlTestRequest testRequest = testRequestStep.getTestRequest();
            if (testRequest != null && testRequest.getOperation() != null
                    && testRequest.getOperation().getInterface() == this) {
                list.add(testRequest);
            }
        } else if (modelItem instanceof WsdlMockResponse) {
            WsdlMockResponse mockResponse = (WsdlMockResponse) modelItem;
            if (mockResponse.getMockOperation() != null && mockResponse.getMockOperation().getOperation() != null
                    && mockResponse.getMockOperation().getOperation().getInterface() == this) {
                list.add(mockResponse);
            }
        }

        // Traverse the ModelItem hierarchy.
        for (ModelItem child : modelItem.getChildren()) {
            getAllMessages(child, list);
        }
    }

    @Override
    public void addExternalDependencies(List<ExternalDependency> dependencies) {
        super.addExternalDependencies(dependencies);
        dependencies.add(new InterfaceExternalDependency(definitionProperty));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void resolve(ResolveContext<?> context) {
        super.resolve(context);

        String definition = definitionProperty.expandUrl();
        if (!isCached() && definition.startsWith("file:")) {
            try {
                File file = new File(definition.substring(5));
                if (!file.exists()) {
                    if (context.hasThisModelItem(this, "Missing WSDL file", definition)) {
                        return;
                    }
                    context.addPathToResolve(this, "Missing WSDL file", definition, new ResolveContext.FileResolver(
                            "Select WSDL File", "wsdl", "WSDL Files (*.wsdl)", file.getParent()) {

                        @Override
                        public boolean apply(File newFile) {
                            try {
                                setDefinition(newFile.toURI().toURL().toString());
                                return true;
                            } catch (Exception e) {
                                log.error("Invalid URL for new Definition", e);
                                return false;
                            }
                        }
                    });
                } else {
                    if (context.hasThisModelItem(this, "Missing WSDL file", definition)) {
                        context.getPath(this, "Missing WSDL file", definition).setSolved(true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getInterfaceType() {
        return WsdlInterfaceFactory.WSDL_TYPE;
    }

    public String getTechnicalId() {
        return getBindingName().toString();
    }

    public String getWsaVersion() {
        if (getConfig().getWsaVersion().equals(WsaVersionTypeConfig.X_200408)) {
            return WsaVersionTypeConfig.X_200408.toString();
        } else if (getConfig().getWsaVersion().equals(WsaVersionTypeConfig.X_200508)) {
            return WsaVersionTypeConfig.X_200508.toString();
        }

        return WsaVersionTypeConfig.NONE.toString();
    }

    public void setWsaVersion(String wsAddressing) {
        if (wsAddressing.equals(WsaVersionTypeConfig.X_200508.toString())) {
            getConfig().setWsaVersion(WsaVersionTypeConfig.X_200508);
        } else if (wsAddressing.equals(WsaVersionTypeConfig.X_200408.toString())) {
            getConfig().setWsaVersion(WsaVersionTypeConfig.X_200408);
        } else {
            getConfig().setWsaVersion(WsaVersionTypeConfig.NONE);
        }

    }

    public void setAnonymous(String anonymous) {
        if (anonymous.equals(AnonymousTypeConfig.REQUIRED.toString())) {
            getConfig().setAnonymous(AnonymousTypeConfig.REQUIRED);
        } else if (anonymous.equals(AnonymousTypeConfig.PROHIBITED.toString())) {
            getConfig().setAnonymous(AnonymousTypeConfig.PROHIBITED);
        } else {
            getConfig().setAnonymous(AnonymousTypeConfig.OPTIONAL);
        }

    }

    public String getAnonymous() {
        // return WsdlUtils.getAnonymous(this);
        if (getConfig().isSetAnonymous()) {
            if (getConfig().getAnonymous().equals(AnonymousTypeConfig.PROHIBITED)) {
                return AnonymousTypeConfig.PROHIBITED.toString();
            } else if (getConfig().getAnonymous().equals(AnonymousTypeConfig.REQUIRED)) {
                return AnonymousTypeConfig.REQUIRED.toString();
            }
        }

        return AnonymousTypeConfig.OPTIONAL.toString();
    }

    @Override
    public WsdlContext getDefinitionContext() {
        return getWsdlContext();
    }

    // need to fix removing mock response and test cases.
    private void replace(WsdlOperation wsdlOperation, OperationConfig reloadedOperation) {
        int index = operations.indexOf(wsdlOperation);

        int c = operations.indexOf(wsdlOperation);
        if (c < 0) {
            throw new IllegalArgumentException(wsdlOperation.getName() + " not found");
        }

        log.info("deleting operation [" + wsdlOperation.getName() + "]");

        // remove requests first (should this be done by some listener?)
        while (wsdlOperation.getRequestCount() > 0) {
            wsdlOperation.removeRequest(wsdlOperation.getRequestAt(0));
        }

        operations.remove(c);

        try {
            fireOperationRemoved(wsdlOperation);
        } finally {
            wsdlOperation.release();
            getConfig().removeOperation(c);
        }

        OperationConfig newConfig = (OperationConfig) getConfig().addNewOperation().set(reloadedOperation)
                .changeType(OperationConfig.type);
        WsdlOperation newOperation = new WsdlOperation(this, newConfig);
        operations.add(index, newOperation);
        newOperation.afterLoad();
        fireOperationAdded(newOperation);

    }

    /**
     * Method for processing policy on interface level it should include
     * processing of all types of policies, but for now there's only Addressing
     * policy implemented
     *
     * @param policy
     * @return this interface changed in a proper way indicated by the policy
     */
    public void processPolicy(Policy policy) throws Exception {

        // default is optional
        // String anonymous = AnonymousTypeConfig.OPTIONAL.toString();
        // if (StringUtils.isNullOrEmpty(interfaceAnonymous) || policyFlag)
        // interfaceAnonymous = AnonymousTypeConfig.OPTIONAL.toString() ;
        // if (StringUtils.isNullOrEmpty(interfaceWsaVersion)|| policyFlag)
        // interfaceWsaVersion = WsaVersionTypeConfig.NONE.toString();
        if (!policyFlag) {
            interfaceAnonymous = AnonymousTypeConfig.OPTIONAL.toString();
            interfaceWsaVersion = WsaVersionTypeConfig.NONE.toString();
        }
        policyFlag = true;

        if (policy != null) {
            List<Addressing> addressingList = policy.getAddressingList();
            List<?> usingAddressingList = policy.getUsingAddressingList();
            for (Addressing addressing : addressingList) {
                policyFlag = true;
                String optional = addressing.getOptional().toString();
                if (StringUtils.isNullOrEmpty(optional) || optional.equals("false")
                        || (optional.equals("true") && SoapUI.getSettings().getBoolean(WsaSettings.ENABLE_FOR_OPTIONAL))) {
                    interfaceWsaVersion = WsaVersionTypeConfig.X_200508.toString();
                }
                Policy innerPolicy = addressing.getPolicy();
                if (innerPolicy != null) {
                    List<AnonymousResponses> anonymousList = innerPolicy.getAnonymousResponsesList();
                    List<NonAnonymousResponses> nonAnonymousList = innerPolicy.getNonAnonymousResponsesList();
                    if (anonymousList.size() > 0 && nonAnonymousList.size() > 0) {
                        throw new Exception(
                                "Wrong addressing policy, anonymousResponses and nonAnonymousResponses can not be specified together");
                    }
                    if (anonymousList.size() > 0) {
                        interfaceAnonymous = AnonymousTypeConfig.REQUIRED.toString();
                    } else {
                        if (nonAnonymousList.size() > 0) {
                            interfaceAnonymous = AnonymousTypeConfig.PROHIBITED.toString();
                        }
                    }
                }
            }
            if (interfaceWsaVersion == WsaVersionTypeConfig.NONE.toString() && !usingAddressingList.isEmpty()) {
                /*
				 * UsingAddressing can also be specified insde Policy check
				 * http://www.w3.org/TR/ws-addr-wsdl/#id2263339
				 */
                interfaceWsaVersion = WsaVersionTypeConfig.X_200508.toString();
            }
        }
        setAnonymous(interfaceAnonymous);
        // set wsaVersion to one from policy only if it was null from wsdl binding
        if (getConfig().getWsaVersion().equals(WsaVersionTypeConfig.NONE)) {
            setWsaVersion(interfaceWsaVersion);
        }

    }
}
