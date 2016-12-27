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

package com.eviware.soapui.impl.wsdl.endpoint;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.CredentialsConfig.AuthType;
import com.eviware.soapui.config.CredentialsConfig.AuthType.Enum;
import com.eviware.soapui.config.DefaultEndpointStrategyConfig;
import com.eviware.soapui.config.EndpointConfig;
import com.eviware.soapui.config.ProjectConfig;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.filters.HttpAuthenticationRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.filters.WssAuthenticationRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.project.EndpointStrategy;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.project.ProjectListener;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringList;
import org.apache.commons.httpclient.URI;
import org.apache.http.client.methods.HttpRequestBase;

import javax.swing.JComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultEndpointStrategy implements EndpointStrategy, PropertyExpansionContainer {
    private WsdlProject project;
    private DefaultEndpointStrategyConfig config;
    private Map<String, EndpointDefaults> defaults = new HashMap<String, EndpointDefaults>();
    private PropertyChangeListener propertyChangeListener = new InternalPropertyChangeListener();
    private ProjectListener projectListener = new InternalProjectListener();
    private DefaultEndpointStrategyConfigurationPanel configurationPanel;

    public void init(Project project) {
        this.project = (WsdlProject) project;
        initConfig();

        project.addProjectListener(projectListener);

        for (Interface iface : project.getInterfaceList()) {
            for (String endpoint : iface.getEndpoints()) {
                // ensure we have defaults
                getEndpointDefaults(endpoint);
            }

            iface.addPropertyChangeListener(AbstractInterface.ENDPOINT_PROPERTY, propertyChangeListener);
        }

        removeUnusedEndpoints();
    }

    private void initConfig() {
        ProjectConfig projectConfig = this.project.getConfig();

        if (!projectConfig.isSetEndpointStrategy()) {
            projectConfig.addNewEndpointStrategy();
        }

        config = (DefaultEndpointStrategyConfig) projectConfig.getEndpointStrategy().changeType(
                DefaultEndpointStrategyConfig.type);

        for (EndpointConfig endpointConfig : config.getEndpointList()) {
            if (!endpointConfig.isSetMode()) {
                endpointConfig.setMode(EndpointConfig.Mode.COMPLEMENT);
            }

            defaults.put(endpointConfig.getStringValue(), new EndpointDefaults(endpointConfig));
        }
    }

    private void removeUnusedEndpoints() {
        if (config == null) {
            return;
        }

        Set<String> endpoints = new HashSet<String>();

        for (Interface iface : project.getInterfaceList()) {
            endpoints.addAll(Arrays.asList(iface.getEndpoints()));
        }

        StringList keys = new StringList();

        synchronized (defaults) {
            for (String key : defaults.keySet()) {
                if (!endpoints.contains(key)) {
                    keys.add(key);
                }
            }

            for (String key : keys) {
                EndpointDefaults def = defaults.remove(key);
                config.getEndpointList().remove(def);
            }
        }
    }

    public void filterRequest(SubmitContext context, Request wsdlRequest) {
        HttpRequestBase httpMethod = (HttpRequestBase) context.getProperty(BaseHttpRequestTransport.HTTP_METHOD);
        URI tempUri = (URI) context.getProperty(BaseHttpRequestTransport.REQUEST_URI);
        java.net.URI uri = null;

        try {
            uri = new java.net.URI(tempUri.toString());
        } catch (URISyntaxException e) {
            SoapUI.logError(e);
        }

        if (uri == null) {
            uri = httpMethod.getURI();
        }

        if (uri == null) {
            return;
        }

        EndpointDefaults def = defaults.get(uri.toString());

        if (def == null) {
            synchronized (defaults) {
                for (String ep : defaults.keySet()) {
                    try {
                        URL tempUrl = new URL(PropertyExpander.expandProperties(context, ep));
                        if (tempUrl.toString().equalsIgnoreCase(uri.toString())) {
                            def = defaults.get(ep);
                            break;
                        }
                    } catch (Exception e) {
                        // we can hide this exception for now, it could happen for
                        // invalid property-expansions, etc
                        // if the endpoint really is wrong there will be other
                        // exception later on
                    }
                }

                if (wsdlRequest instanceof RestRequestInterface) {
                    for (String ep : defaults.keySet()) {
                        try {
                            URL tempUrl = new URL(PropertyExpander.expandProperties(context, ep));
                            if (tempUrl.getHost().toString().equalsIgnoreCase(uri.getHost().toString())) {
                                def = defaults.get(ep);
                                break;
                            }
                        } catch (Exception e) {
                            // we can hide this exception for now, it could happen for
                            // invalid property-expansions, etc
                            // if the endpoint really is wrong there will be other
                            // exception later on
                        }
                    }
                }
            }

            if (def == null) {
                return;
            }
        }

        applyDefaultsToWsdlRequest(context, (AbstractHttpRequestInterface<?>) wsdlRequest, def);
    }

    protected void applyDefaultsToWsdlRequest(SubmitContext context, AbstractHttpRequestInterface<?> wsdlRequest,
                                              EndpointDefaults def) {
        String requestUsername = PropertyExpander.expandProperties(context, wsdlRequest.getUsername());
        String requestPassword = PropertyExpander.expandProperties(context, wsdlRequest.getPassword());
        String requestDomain = PropertyExpander.expandProperties(context, wsdlRequest.getDomain());

        String defUsername = PropertyExpander.expandProperties(context, def.getUsername());
        String defPassword = PropertyExpander.expandProperties(context, def.getPassword());
        String defDomain = PropertyExpander.expandProperties(context, def.getDomain());

        Enum authType = AuthType.Enum.forString(wsdlRequest.getAuthType());

        if (def.getMode() == EndpointConfig.Mode.OVERRIDE) {
            overrideRequest(context, wsdlRequest, def, requestUsername, requestPassword, requestDomain, defUsername,
                    defPassword, defDomain, authType);
        } else if (def.getMode() == EndpointConfig.Mode.COPY) {
            copyToRequest(context, wsdlRequest, def, requestUsername, requestPassword, requestDomain, defUsername,
                    defPassword, defDomain, authType);
        } else if (def.getMode() == EndpointConfig.Mode.COMPLEMENT) {
            complementRequest(context, wsdlRequest, def, requestUsername, requestPassword, requestDomain, defUsername,
                    defPassword, defDomain, authType);
        }
    }

    private void overrideRequest(SubmitContext context, AbstractHttpRequestInterface<?> wsdlRequest,
                                 EndpointDefaults def, String requestUsername, String requestPassword, String requestDomain,
                                 String defUsername, String defPassword, String defDomain,
                                 com.eviware.soapui.config.CredentialsConfig.AuthType.Enum authType) {
        String username = StringUtils.hasContent(defUsername) ? defUsername : requestUsername;
        String password = StringUtils.hasContent(defPassword) ? defPassword : requestPassword;

        if (StringUtils.hasContent(username) || StringUtils.hasContent(password)) {
            // only set if not set in request
            String wssType = def.getWssType();
            String wssTimeToLive = def.getWssTimeToLive();

            if (wssType == null) {
                String domain = StringUtils.hasContent(defDomain) ? defDomain : requestDomain;
                HttpAuthenticationRequestFilter.initRequestCredentials(context, username, project.getSettings(), password,
                        domain, authType);
            }

            if (StringUtils.hasContent(wssType) || StringUtils.hasContent(wssTimeToLive)) {
                try {
                    // set to null so existing don't get removed
                    if (wssTimeToLive != null && wssTimeToLive.length() == 0) {
                        wssTimeToLive = null;
                    }

                    WssAuthenticationRequestFilter.setWssHeaders(context, username, password, wssType, wssTimeToLive);
                } catch (Exception e) {
                    SoapUI.logError(e);
                }
            }
        }
    }

    private void copyToRequest(SubmitContext context, AbstractHttpRequestInterface<?> wsdlRequest,
                               EndpointDefaults def, String requestUsername, String requestPassword, String requestDomain,
                               String defUsername, String defPassword, String defDomain,
                               com.eviware.soapui.config.CredentialsConfig.AuthType.Enum authType) {
        // only set if not set in request
        String wssType = def.getWssType();

        if (wssType != null) {
            HttpAuthenticationRequestFilter
                    .initRequestCredentials(context, null, project.getSettings(), null, null, null);
        } else {
            HttpAuthenticationRequestFilter.initRequestCredentials(context, defUsername, project.getSettings(),
                    defPassword, defDomain, authType);
        }

        String wssTimeToLive = def.getWssTimeToLive();
        if (wssTimeToLive == null) {
            wssTimeToLive = "";
        }

        try {
            WssAuthenticationRequestFilter.setWssHeaders(context, defUsername, defPassword, wssType, wssTimeToLive);
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    private void complementRequest(SubmitContext context, AbstractHttpRequestInterface<?> httpRequest,
                                   EndpointDefaults def, String requestUsername, String requestPassword, String requestDomain,
                                   String defUsername, String defPassword, String defDomain,
                                   com.eviware.soapui.config.CredentialsConfig.AuthType.Enum authType) {
        String username = StringUtils.hasContent(requestUsername) ? requestUsername : defUsername;
        String password = StringUtils.hasContent(requestPassword) ? requestPassword : defPassword;

        if (httpRequest instanceof WsdlRequest) {
            WsdlRequest wsdlRequest = (WsdlRequest) httpRequest;
            // only set if not set in request
            String wssType = StringUtils.isNullOrEmpty(wsdlRequest.getWssPasswordType()) ? def.getWssType()
                    : (StringUtils.hasContent(username) && StringUtils.hasContent(password)) ? null : wsdlRequest
                    .getWssPasswordType();

            String wssTimeToLive = StringUtils.isNullOrEmpty(wsdlRequest.getWssTimeToLive()) ? def.getWssTimeToLive()
                    : null;

            if (!StringUtils.hasContent(wssType)
                    && (StringUtils.hasContent(username) || StringUtils.hasContent(password))) {
                String domain = StringUtils.hasContent(requestDomain) ? requestDomain : defDomain;
                HttpAuthenticationRequestFilter.initRequestCredentials(context, username, project.getSettings(), password,
                        domain, authType);
            } else if (StringUtils.hasContent(wssType) || StringUtils.hasContent(wssTimeToLive)) {
                try {
                    // set to null so existing don't get removed
                    if (wssTimeToLive != null && wssTimeToLive.length() == 0) {
                        wssTimeToLive = null;
                    }

                    if (StringUtils.hasContent(username) || StringUtils.hasContent(password)) {
                        WssAuthenticationRequestFilter.setWssHeaders(context, username, password, wssType, wssTimeToLive);
                    }
                } catch (Exception e) {
                    SoapUI.logError(e);
                }
            }
        } else {
            if ((StringUtils.hasContent(username) || StringUtils.hasContent(password))) {
                String domain = StringUtils.hasContent(requestDomain) ? requestDomain : defDomain;
                HttpAuthenticationRequestFilter.initRequestCredentials(context, username, project.getSettings(), password,
                        domain, authType);
            }
        }
    }

    public void release() {
        project.removeProjectListener(projectListener);
        for (Interface iface : project.getInterfaceList()) {
            iface.removePropertyChangeListener(AbstractInterface.ENDPOINT_PROPERTY, propertyChangeListener);
        }

        if (configurationPanel != null) {
            configurationPanel.release();
        }
    }

    private class InternalProjectListener extends ProjectListenerAdapter {
        @Override
        public void interfaceAdded(Interface iface) {
            for (String endpoint : iface.getEndpoints()) {
                // ensure we have defaults
                getEndpointDefaults(endpoint);
            }

            iface.addPropertyChangeListener(AbstractInterface.ENDPOINT_PROPERTY, propertyChangeListener);
        }

        @Override
        public void interfaceRemoved(Interface iface) {
            iface.removePropertyChangeListener(AbstractInterface.ENDPOINT_PROPERTY, propertyChangeListener);
            removeUnusedEndpoints();
        }
    }

    private class InternalPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            // new endpoint?
            String newValue = evt.getNewValue() == null ? null : evt.getNewValue().toString();
            if (evt.getOldValue() == null) {
                getEndpointDefaults(newValue);
            }
            // changed endpoint?
            else if (newValue != null) {
                String oldValue = evt.getOldValue().toString();
                EndpointDefaults def = defaults.containsKey(newValue) ? defaults.get(newValue)
                        : getEndpointDefaults(oldValue);
                def.endpointConfig.setStringValue(newValue);

                synchronized (defaults) {
                    defaults.remove(oldValue);
                    defaults.put(newValue, def);
                }
            } else {
                removeUnusedEndpoints();
            }
        }
    }

    public class EndpointDefaults implements PropertyExpansionContainer {
        private final EndpointConfig endpointConfig;

        public EndpointConfig getEndpointConfig() {
            return endpointConfig;
        }

        public EndpointDefaults(EndpointConfig endpointConfig) {
            this.endpointConfig = endpointConfig;

            if (!endpointConfig.isSetMode()) {
                endpointConfig.setMode(EndpointConfig.Mode.COMPLEMENT);
            }
        }

        public String getDomain() {
            return endpointConfig.getDomain();
        }

        public String getPassword() {
            return endpointConfig.getPassword();
        }

        public String getUsername() {
            return endpointConfig.getUsername();
        }

        public String getWssTimeToLive() {
            return endpointConfig.getWssTimeToLive();
        }

        public String getWssType() {
            String wssPasswordType = endpointConfig.getWssType();
            return StringUtils.isNullOrEmpty(wssPasswordType) || WsdlRequest.PW_TYPE_NONE.equals(wssPasswordType) ? null
                    : wssPasswordType;
        }

        public void setDomain(String arg0) {
            endpointConfig.setDomain(arg0);
        }

        public void setPassword(String arg0) {
            endpointConfig.setPassword(arg0);
        }

        public void setUsername(String arg0) {
            endpointConfig.setUsername(arg0);
        }

        public void setWssTimeToLive(String arg0) {
            endpointConfig.setWssTimeToLive(arg0);
        }

        public String getIncomingWss() {
            return endpointConfig.getIncomingWss();
        }

        public String getOutgoingWss() {
            return endpointConfig.getOutgoingWss();
        }

        public void setIncomingWss(String arg0) {
            endpointConfig.setIncomingWss(arg0);
        }

        public void setOutgoingWss(String arg0) {
            endpointConfig.setOutgoingWss(arg0);
        }

        public void setWssType(String wssPasswordType) {
            if (wssPasswordType == null || wssPasswordType.equals(WsdlRequest.PW_TYPE_NONE)) {
                if (endpointConfig.isSetWssType()) {
                    endpointConfig.unsetWssType();
                }
            } else {
                endpointConfig.setWssType(wssPasswordType);
            }
        }

        public EndpointConfig.Mode.Enum getMode() {
            return endpointConfig.getMode();
        }

        public void setMode(EndpointConfig.Mode.Enum mode) {
            endpointConfig.setMode(mode);
        }

        protected EndpointConfig getConfig() {
            return endpointConfig;
        }

        public PropertyExpansion[] getPropertyExpansions() {
            PropertyExpansionsResult result = new PropertyExpansionsResult(project, this);

            result.extractAndAddAll("username");
            result.extractAndAddAll("password");
            result.extractAndAddAll("domain");

            return result.toArray();
        }
    }

    public EndpointDefaults getEndpointDefaults(String endpoint) {
        if (config == null) {
            initConfig();
        }

        if (!defaults.containsKey(endpoint)) {
            synchronized (defaults) {
                EndpointConfig newEndpoint = config.addNewEndpoint();
                newEndpoint.setStringValue(endpoint);
                defaults.put(endpoint, new EndpointDefaults(newEndpoint));
            }
        }

        return defaults.get(endpoint);
    }

    public void onSave() {
        if (config == null) {
            return;
        }

        removeUnusedEndpoints();

        // remove unused
        for (int c = 0; c < config.sizeOfEndpointArray(); c++) {
            EndpointConfig ec = config.getEndpointArray(c);
            if (StringUtils.isNullOrEmpty(ec.getDomain()) && StringUtils.isNullOrEmpty(ec.getUsername())
                    && StringUtils.isNullOrEmpty(ec.getPassword()) && StringUtils.isNullOrEmpty(ec.getWssType())
                    && StringUtils.isNullOrEmpty(ec.getWssTimeToLive()) && StringUtils.isNullOrEmpty(ec.getIncomingWss())
                    && StringUtils.isNullOrEmpty(ec.getOutgoingWss()) && ec.getMode() == EndpointConfig.Mode.COMPLEMENT) {
                synchronized (defaults) {
                    defaults.remove(ec.getStringValue());
                    config.removeEndpoint(c);
                    c--;
                }
            }
        }

        if (config.sizeOfEndpointArray() == 0) {
            project.getConfig().unsetEndpointStrategy();
            config = null;
        }
    }

    public void importEndpoints(Interface iface) {
        EndpointStrategy ep = iface.getProject().getEndpointStrategy();
        if (ep instanceof DefaultEndpointStrategy) {
            DefaultEndpointStrategy dep = (DefaultEndpointStrategy) ep;
            String[] endpoints = iface.getEndpoints();

            for (String endpoint : endpoints) {
                getEndpointDefaults(endpoint).getConfig().set(dep.getEndpointDefaults(endpoint).getConfig());
            }
        }
    }

    public JComponent getConfigurationPanel(Interface iface) {
        configurationPanel = new DefaultEndpointStrategyConfigurationPanel(iface, this);
        return configurationPanel;
    }

    public void afterRequest(SubmitContext context, Response response) {
    }

    public PropertyExpansion[] getPropertyExpansions() {
        PropertyExpansionsResult result = new PropertyExpansionsResult(project, this);

        for (EndpointDefaults ed : defaults.values()) {
            result.addAll(ed.getPropertyExpansions());
        }

        return result.toArray();
    }

    public void changeEndpoint(String oldEndpoint, String newEndpoint) {
        synchronized (defaults) {
            EndpointDefaults endpointDefaults = defaults.remove(oldEndpoint);
            if (endpointDefaults != null) {
                endpointDefaults.getConfig().setStringValue(newEndpoint);
                defaults.put(newEndpoint, endpointDefaults);
            }
        }
    }

    public void afterRequest(SubmitContext context, Request request) {
    }
}
