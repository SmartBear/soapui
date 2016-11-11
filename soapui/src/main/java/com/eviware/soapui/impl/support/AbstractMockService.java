/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
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

package com.eviware.soapui.impl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.BaseMockServiceConfig;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.AbstractTestPropertyHolderWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunner;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.impl.wsdl.support.IconAnimator;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.BeanPathPropertySupport;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunListener;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.mock.MockServiceListener;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.SSLSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.scripting.ScriptEnginePool;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractMockService<MockOperationType extends MockOperation,
        MockServiceConfigType extends BaseMockServiceConfig>
        extends AbstractTestPropertyHolderWsdlModelItem<MockServiceConfigType>
        implements MockService, HasHelpUrl {
    public final static String START_SCRIPT_PROPERTY = AbstractMockService.class.getName() + "@startScript";
    public final static String STOP_SCRIPT_PROPERTY = AbstractMockService.class.getName() + "@stopScript";

    protected List<MockOperation> mockOperations = new ArrayList<MockOperation>();
    private Set<MockRunListener> mockRunListeners = new HashSet<MockRunListener>();
    private Set<MockServiceListener> mockServiceListeners = new HashSet<MockServiceListener>();
    private MockServiceIconAnimator iconAnimator;
    private WsdlMockRunner mockRunner;

    private SoapUIScriptEngine startScriptEngine;
    private SoapUIScriptEngine stopScriptEngine;
    private BeanPathPropertySupport docrootProperty;
    private ScriptEnginePool onRequestScriptEnginePool;
    private ScriptEnginePool afterRequestScriptEnginePool;


    protected AbstractMockService(MockServiceConfigType config, ModelItem parent, String icon) {
        super(config, parent, icon);

        if (!config.isSetPort() || config.getPort() < 1) {
            config.setPort(8080);
        }

        if (!config.isSetPath()) {
            config.setPath("/");
        }

        if (!config.isSetId()) {
            config.setId(ModelSupport.generateModelItemID());
        }

        initHost(config);

        docrootProperty = new BeanPathPropertySupport(this, "docroot");

        iconAnimator = new MockServiceIconAnimator();
        addMockRunListener(iconAnimator);
    }

    private void initHost(MockServiceConfigType config) {
        try {
            if (!config.isSetHost() || !StringUtils.hasContent(config.getHost())) {
                config.setHost(InetAddress.getLocalHost().getHostName());
            }
        } catch (UnknownHostException e) {
            SoapUI.logError(e);
        }
    }

    // Implements MockService
    @Override
    public WsdlProject getProject() {
        return (WsdlProject) getParent();
    }

    @Override
    public MockOperationType getMockOperationAt(int index) {
        return (MockOperationType) mockOperations.get(index);
    }

    @Override
    public MockOperation getMockOperationByName(String name) {

        for (MockOperation operation : mockOperations) {
            if (operation.getName() != null && operation.getName().equals(name)) {
                return operation;
            }
        }

        return null;
    }

    public void addMockOperation(MockOperationType mockOperation) {
        if (canIAddAMockOperation(mockOperation)) {
            mockOperations.add(mockOperation);
        } else {
            throw new IllegalStateException(mockOperation.getName() + " is not attached to service " + this.getName());
        }
    }

    protected String getProtocol() {
        try {
            boolean sslEnabled = SoapUI.getSettings().getBoolean(SSLSettings.ENABLE_MOCK_SSL);
            String protocol = sslEnabled ? "https://" : "http://";
            return protocol;
        } catch (Exception e) {
            return "http://";
        }
    }

    protected abstract boolean canIAddAMockOperation(MockOperationType mockOperation);

    @Override
    public int getMockOperationCount() {
        return mockOperations.size();
    }


    @Override
    public int getPort() {
        return getConfig().getPort();
    }

    @Override
    public String getPath() {
        return getConfig().getPath();
    }

    @Override
    public void removeMockOperation(MockOperation mockOperation) {
        int ix = mockOperations.indexOf(mockOperation);
        if (ix == -1) {
            throw new RuntimeException("Unknown MockOperation specified to removeMockOperation");
        }

        mockOperations.remove(ix);
        fireMockOperationRemoved(mockOperation);
        mockOperation.release();

        if (this instanceof WsdlMockService) {
            ((WsdlMockService) this).getConfig().removeMockOperation(ix);
        } else if (this instanceof RestMockService) {
            ((RestMockService) this).getConfig().removeRestMockAction(ix);
        }
    }

    public String getLocalEndpoint() {
        String host = getHost();
        if (StringUtils.isNullOrEmpty(host)) {
            host = "127.0.0.1";
        }

        return getProtocol() + host + ":" + getPort() + getPath();
    }

    @Override
    public String getHost() {
        return getConfig().getHost();
    }

    public void setHost(String host) {
        getConfig().setHost(host);
    }

    @Override
    public void setPort(int port) {
        int oldPort = getPort();
        getConfig().setPort(port);
        notifyPropertyChanged(PORT_PROPERTY, oldPort, port);
    }

    @Override
    public void setPath(String path) {
        String oldPath = getPath();
        getConfig().setPath(path);
        notifyPropertyChanged(PATH_PROPERTY, oldPath, path);
    }

    @Override
    public WsdlMockRunner start() throws Exception {
        return start(null);
    }

    @Override
    public void startIfConfigured() throws Exception {
        if (SoapUI.getSettings().getBoolean(HttpSettings.START_MOCK_SERVICE)) {
            start();
        }
    }


    @Override
    public boolean getBindToHostOnly() {
        return getConfig().getBindToHostOnly();
    }

    public void setBindToHostOnly(boolean bindToHostOnly) {
        getConfig().setBindToHostOnly(bindToHostOnly);
    }

    // TODO: think about naming - this does not start nothing.....
    public WsdlMockRunner start(WsdlTestRunContext context) throws Exception {
        String path = getPath();
        if (path == null || path.trim().length() == 0 || path.trim().charAt(0) != '/') {
            throw new Exception("Invalid path; must start with '/'");
        }

        mockRunner = new WsdlMockRunner(this, context);
        return mockRunner;
    }

    @Override
    public void addMockRunListener(MockRunListener listener) {
        mockRunListeners.add(listener);
    }

    @Override
    public void removeMockRunListener(MockRunListener listener) {
        mockRunListeners.remove(listener);
    }

    @Override
    public void addMockServiceListener(MockServiceListener listener) {
        mockServiceListeners.add(listener);
    }

    @Override
    public void removeMockServiceListener(MockServiceListener listener) {
        mockServiceListeners.remove(listener);
    }

    @Override
    public WsdlMockRunner getMockRunner() {
        return mockRunner;
    }

    public void setMockRunner(WsdlMockRunner mockRunner) {
        this.mockRunner = mockRunner;
    }

    @Override
    public MockRunListener[] getMockRunListeners() {
        return mockRunListeners.toArray(new MockRunListener[mockRunListeners.size()]);
    }

    public MockServiceListener[] getMockServiceListeners() {
        return mockServiceListeners.toArray(new MockServiceListener[mockServiceListeners.size()]);
    }

    @Override
    public List<MockOperation> getMockOperationList() {
        return Collections.unmodifiableList(new ArrayList<MockOperation>(mockOperations));
    }

    protected List<MockOperation> getMockOperations() {
        return mockOperations;
    }

    @Override
    public void fireMockOperationAdded(MockOperation mockOperation) {
        for (MockServiceListener listener : getMockServiceListeners()) {
            listener.mockOperationAdded(mockOperation);
        }
    }

    @Override
    public void fireMockOperationRemoved(MockOperation mockOperation) {
        for (MockServiceListener listener : getMockServiceListeners()) {
            listener.mockOperationRemoved(mockOperation);
        }
    }

    @Override
    public void fireMockResponseAdded(MockResponse mockResponse) {
        for (MockServiceListener listener : getMockServiceListeners()) {
            listener.mockResponseAdded(mockResponse);
        }
    }

    @Override
    public void fireMockResponseRemoved(MockResponse mockResponse) {
        for (MockServiceListener listener : getMockServiceListeners()) {
            listener.mockResponseRemoved(mockResponse);
        }
    }

    @Override
    public void release() {
        super.release();

        mockServiceListeners.clear();

        if (mockRunner != null) {
            if (mockRunner.isRunning()) {
                mockRunner.stop();
            }

            if (mockRunner != null) {
                mockRunner.release();
            }
        }

        if (onRequestScriptEnginePool != null) {
            onRequestScriptEnginePool.release();
        }

        if (afterRequestScriptEnginePool != null) {
            afterRequestScriptEnginePool.release();
        }

        if (startScriptEngine != null) {
            startScriptEngine.release();
        }

        if (stopScriptEngine != null) {
            stopScriptEngine.release();
        }

    }

    @Override
    public void setStartScript(String script) {
        String oldScript = getStartScript();

        if (!getConfig().isSetStartScript()) {
            getConfig().addNewStartScript();
        }

        getConfig().getStartScript().setStringValue(script);

        if (startScriptEngine != null) {
            startScriptEngine.setScript(script);
        }

        notifyPropertyChanged(START_SCRIPT_PROPERTY, oldScript, script);
    }

    @Override
    public String getStartScript() {
        return getConfig().isSetStartScript() ? getConfig().getStartScript().getStringValue() : null;
    }

    @Override
    public void setStopScript(String script) {
        String oldScript = getStopScript();

        if (!getConfig().isSetStopScript()) {
            getConfig().addNewStopScript();
        }

        getConfig().getStopScript().setStringValue(script);
        if (stopScriptEngine != null) {
            stopScriptEngine.setScript(script);
        }

        notifyPropertyChanged(STOP_SCRIPT_PROPERTY, oldScript, script);
    }

    @Override
    public String getStopScript() {
        return getConfig().isSetStopScript() ? getConfig().getStopScript().getStringValue() : null;
    }

    @Override
    public Object runStartScript(WsdlMockRunContext runContext, MockRunner runner) throws Exception {
        String script = getStartScript();
        if (StringUtils.isNullOrEmpty(script)) {
            return null;
        }

        if (startScriptEngine == null) {
            startScriptEngine = SoapUIScriptEngineRegistry.create(this);
            startScriptEngine.setScript(script);
        }

        startScriptEngine.setVariable("context", runContext);
        startScriptEngine.setVariable("mockRunner", runner);
        startScriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
        return startScriptEngine.run();
    }

    @Override
    public Object runStopScript(WsdlMockRunContext runContext, MockRunner runner) throws Exception {
        String script = getStopScript();
        if (StringUtils.isNullOrEmpty(script)) {
            return null;
        }

        if (stopScriptEngine == null) {
            stopScriptEngine = SoapUIScriptEngineRegistry.create(this);
            stopScriptEngine.setScript(script);
        }

        stopScriptEngine.setVariable("context", runContext);
        stopScriptEngine.setVariable("mockRunner", runner);
        stopScriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
        return stopScriptEngine.run();
    }

    @Override
    public void setOnRequestScript(String script) {
        String oldScript = getOnRequestScript();

        if (!getConfig().isSetOnRequestScript()) {
            getConfig().addNewOnRequestScript();
        }

        getConfig().getOnRequestScript().setStringValue(script);

        if (onRequestScriptEnginePool != null) {
            onRequestScriptEnginePool.setScript(script);
        }

        notifyPropertyChanged("onRequestScript", oldScript, script);
    }

    @Override
    public String getOnRequestScript() {
        return getConfig().isSetOnRequestScript() ? getConfig().getOnRequestScript().getStringValue() : null;
    }

    @Override
    public void setAfterRequestScript(String script) {
        String oldScript = getAfterRequestScript();

        if (!getConfig().isSetAfterRequestScript()) {
            getConfig().addNewAfterRequestScript();
        }

        getConfig().getAfterRequestScript().setStringValue(script);
        if (afterRequestScriptEnginePool != null) {
            afterRequestScriptEnginePool.setScript(script);
        }

        notifyPropertyChanged("afterRequestScript", oldScript, script);
    }

    @Override
    public String getAfterRequestScript() {
        return getConfig().isSetAfterRequestScript() ? getConfig().getAfterRequestScript().getStringValue() : null;
    }

    @Override
    public Object runOnRequestScript(WsdlMockRunContext runContext, MockRequest mockRequest) throws Exception {
        String script = getOnRequestScript();
        if (StringUtils.isNullOrEmpty(script)) {
            return null;
        }

        if (onRequestScriptEnginePool == null) {
            onRequestScriptEnginePool = new ScriptEnginePool(this);
            onRequestScriptEnginePool.setScript(script);
        }

        SoapUIScriptEngine scriptEngine = onRequestScriptEnginePool.getScriptEngine();

        try {
            scriptEngine.setVariable("context", runContext);
            scriptEngine.setVariable("mockRequest", mockRequest);
            scriptEngine.setVariable("mockRunner", getMockRunner());
            scriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
            return scriptEngine.run();
        } finally {
            onRequestScriptEnginePool.returnScriptEngine(scriptEngine);
        }
    }

    @Override
    public Object runAfterRequestScript(WsdlMockRunContext runContext, MockResult mockResult) throws Exception {
        String script = getAfterRequestScript();
        if (StringUtils.isNullOrEmpty(script)) {
            return null;
        }

        if (afterRequestScriptEnginePool == null) {
            afterRequestScriptEnginePool = new ScriptEnginePool(this);
            afterRequestScriptEnginePool.setScript(script);
        }

        SoapUIScriptEngine scriptEngine = afterRequestScriptEnginePool.getScriptEngine();

        try {
            scriptEngine.setVariable("context", runContext);
            scriptEngine.setVariable("mockResult", mockResult);
            scriptEngine.setVariable("mockRunner", getMockRunner());
            scriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
            return scriptEngine.run();
        } finally {
            afterRequestScriptEnginePool.returnScriptEngine(scriptEngine);
        }
    }

    public void setDocroot(String docroot) {
        docrootProperty.set(docroot, true);
    }

    public String getDocroot() {
        return docrootProperty.get();
    }

    @Override
    public void addExternalDependencies(List<ExternalDependency> dependencies) {
        super.addExternalDependencies(dependencies);
        //Disable since ProjectExporter.packageAll doesn't seem to handle folders
        //dependencies.add( new MockServiceExternalDependency( docrootProperty ) );
    }

    @Override
    public void resolve(ResolveContext<?> context) {
        super.resolve(context);
        docrootProperty.resolveFile(context, "Missing MockService docroot");
    }

    public boolean isDispatchResponseMessages() {
        return getConfig().getDispatchResponseMessages();
    }

    public void setDispatchResponseMessages(boolean dispatchResponseMessages) {
        boolean old = isDispatchResponseMessages();
        getConfig().setDispatchResponseMessages(dispatchResponseMessages);
        notifyPropertyChanged("dispatchResponseMessages", old, dispatchResponseMessages);
    }

    public abstract String getIconName();

    public void fireOnMockResult(Object result) {
        if (result != null && result instanceof MockResult) {
            for (MockRunListener listener : getMockRunListeners()) {
                listener.onMockResult((MockResult) result);
            }
        }
    }

    private class MockServiceIconAnimator
            extends IconAnimator<MockService>
            implements MockRunListener {
        public MockServiceIconAnimator() {
            super(AbstractMockService.this, getIconName(), getIconName(), 4);
        }

        public MockResult onMockRequest(MockRunner runner, HttpServletRequest request, HttpServletResponse response) {
            return null;
        }

        public void onMockResult(MockResult result) {
        }

        public void onMockRunnerStart(MockRunner mockRunner) {
            start();
        }

        public void onMockRunnerStop(MockRunner mockRunner) {
            stop();
            AbstractMockService.this.mockRunner = null;
        }
    }

}
