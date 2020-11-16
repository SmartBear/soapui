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

package com.eviware.soapui.impl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.config.CredentialsConfig.AuthType;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.HttpAttachmentPart;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.IAfterRequestInjection;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.impl.wsdl.support.FileAttachment;
import com.eviware.soapui.impl.wsdl.support.IconAnimator;
import com.eviware.soapui.impl.wsdl.support.RequestFileAttachment;
import com.eviware.soapui.impl.wsdl.support.jms.header.JMSHeaderContainer;
import com.eviware.soapui.impl.wsdl.support.jms.property.JMSPropertyContainer;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep.RequestHeaderHolder;
import com.eviware.soapui.impl.wsdl.teststeps.SettingPathPropertySupport;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.settings.CommonSettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;

import javax.swing.ImageIcon;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractHttpRequest<T extends AbstractRequestConfig> extends AbstractWsdlModelItem<T> implements
        Request, AbstractHttpRequestInterface<T>, JMSHeaderContainer, JMSPropertyContainer {
    public final static Logger log = LogManager.getLogger(AbstractHttpRequest.class);
    public static final String BASIC_AUTH_PROFILE = "Basic";
    public static final String SELECTED_AUTH_PROFILE_PROPERTY_NAME = "selectedAuthProfile";
    public static final String CR_ESCAPE_SEQUENCE = "\\\\_r";

    private Set<SubmitListener> submitListeners = new HashSet<SubmitListener>();
    private String requestContent;
    private RequestIconAnimator<?> iconAnimator;
    private HttpResponse response;
    private SettingPathPropertySupport dumpFile;
    private List<FileAttachment<?>> attachments = new ArrayList<FileAttachment<?>>();
    private IAfterRequestInjection afterRequestInjection;

    protected AbstractHttpRequest(T config, AbstractHttpOperation parent, String icon, boolean forLoadTest) {
        super(config, parent, icon);

        if (!forLoadTest) {
            iconAnimator = initIconAnimator();
            if (SoapUI.usingGraphicalEnvironment()) {
                addSubmitListener(iconAnimator);
            }
        }

        initAttachments();

        dumpFile = new SettingPathPropertySupport(this, DUMP_FILE);
    }

    private void initAttachments() {
        for (AttachmentConfig ac : getConfig().getAttachmentList()) {
            RequestFileAttachment attachment = new RequestFileAttachment(ac, this);
            attachments.add(attachment);
        }
    }

    protected List<FileAttachment<?>> getAttachmentsList() {
        return attachments;
    }

    public Attachment attachBinaryData(byte[] data, String contentType) {
        RequestFileAttachment fileAttachment;
        try {
            File temp = File.createTempFile("binaryContent", ".tmp");

            OutputStream out = new FileOutputStream(temp);
            out.write(data);
            out.close();
            fileAttachment = new RequestFileAttachment(temp, false, this);
            fileAttachment.setContentType(contentType);
            attachments.add(fileAttachment);
            notifyPropertyChanged(ATTACHMENTS_PROPERTY, null, fileAttachment);
            return fileAttachment;
        } catch (IOException e) {
            SoapUI.logError(e);
        }
        return null;
    }

	/*
     * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.wsdl.AttachmentContainer#attachFile(java.io.File,
	 * boolean)
	 */

    public Attachment attachFile(File file, boolean cache) throws IOException {
        RequestFileAttachment fileAttachment = new RequestFileAttachment(file, cache, this);
        attachments.add(fileAttachment);
        notifyPropertyChanged(ATTACHMENTS_PROPERTY, null, fileAttachment);
        return fileAttachment;
    }

    public abstract RestRequestInterface.HttpMethod getMethod();

    /**
     * Override just to get a better return type
     *
     * @see com.eviware.soapui.impl.wsdl.AttachmentContainer#getAttachmentPart(java.lang.String)
     */

    public abstract HttpAttachmentPart getAttachmentPart(String partName);

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.impl.wsdl.AttachmentContainer#getAttachmentCount()
     */
    public int getAttachmentCount() {
        return attachments.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.impl.wsdl.AttachmentContainer#getAttachmentAt(int)
     */
    public Attachment getAttachmentAt(int index) {
        return attachments.get(index);
    }

    @SuppressWarnings("rawtypes")
    public void setAttachmentAt(int index, Attachment attachment) {
        if (attachments.size() > index) {
            attachments.set(index, (FileAttachment) attachment);
        } else {
            attachments.add((FileAttachment) attachment);
        }
        notifyPropertyChanged(ATTACHMENTS_PROPERTY, null, attachment);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.impl.wsdl.AttachmentContainer#getAttachmentsForPart
     * (java.lang.String)
     */
    public Attachment[] getAttachmentsForPart(String partName) {
        List<Attachment> result = new ArrayList<Attachment>();

        for (Attachment attachment : attachments) {
            if (partName.equals(attachment.getPart())) {
                result.add(attachment);
            }
        }

        return result.toArray(new Attachment[result.size()]);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.impl.wsdl.AttachmentContainer#removeAttachment(com.
     * eviware.soapui.model.iface.Attachment)
     */
    public void removeAttachment(Attachment attachment) {
        int ix = attachments.indexOf(attachment);
        attachments.remove(ix);

        try {
            notifyPropertyChanged(ATTACHMENTS_PROPERTY, attachment, null);
        } finally {
            getConfig().removeAttachment(ix);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.impl.wsdl.AttachmentContainer#getAttachments()
     */
    public Attachment[] getAttachments() {
        return attachments.toArray(new Attachment[attachments.size()]);
    }

    protected RequestIconAnimator<?> initIconAnimator() {
        return new RequestIconAnimator<AbstractHttpRequest<?>>(this, "/soap_request.png", "/soap_request.png", 4);
    }

    public void addSubmitListener(SubmitListener listener) {
        submitListeners.add(listener);
    }

    public void removeSubmitListener(SubmitListener listener) {
        submitListeners.remove(listener);
    }

    public boolean isMultipartEnabled() {
        return !getSettings().getBoolean(DISABLE_MULTIPART_ATTACHMENTS);
    }

    public void setMultipartEnabled(boolean multipartEnabled) {
        getSettings().setBoolean(DISABLE_MULTIPART_ATTACHMENTS, !multipartEnabled);
    }

    public boolean isEntitizeProperties() {
        return getSettings().getBoolean(CommonSettings.ENTITIZE_PROPERTIES);
    }

    public void setEntitizeProperties(boolean entitizeProperties) {
        getSettings().setBoolean(CommonSettings.ENTITIZE_PROPERTIES, entitizeProperties);
    }

    @Override
    public void release() {
        submitListeners.clear();

        super.release();
    }

    public SubmitListener[] getSubmitListeners() {
        return submitListeners.toArray(new SubmitListener[submitListeners.size()]);
    }

    public AbstractHttpOperation getOperation() {
        return (AbstractHttpOperation) getParent();
    }

    public void copyAttachmentsTo(WsdlRequest newRequest) {
        if (getAttachmentCount() > 0) {
            try {
                UISupport.setHourglassCursor();
                for (int c = 0; c < getAttachmentCount(); c++) {
                    try {
                        Attachment attachment = getAttachmentAt(c);
                        newRequest.importAttachment(attachment);
                    } catch (Exception e) {
                        SoapUI.logError(e);
                    }
                }
            } finally {
                UISupport.resetCursor();
            }
        }
    }

    public Attachment importAttachment(Attachment attachment) {
        if (attachment instanceof FileAttachment<?>) {
            AttachmentConfig oldConfig = ((FileAttachment<?>) attachment).getConfig();
            AttachmentConfig newConfig = (AttachmentConfig) getConfig().addNewAttachment().set(oldConfig);
            RequestFileAttachment newAttachment = new RequestFileAttachment(newConfig, this);
            attachments.add(newAttachment);
            return newAttachment;
        } else {
            log.error("Unknown attachment type: " + attachment);
        }

        return null;
    }

    public void addAttachmentsChangeListener(PropertyChangeListener listener) {
        addPropertyChangeListener(ATTACHMENTS_PROPERTY, listener);
    }

    public boolean isReadOnly() {
        return false;
    }

    public void removeAttachmentsChangeListener(PropertyChangeListener listener) {
        removePropertyChangeListener(ATTACHMENTS_PROPERTY, listener);
    }

    public String getRequestContent() {
        if (getConfig().getRequest() == null) {
            getConfig().addNewRequest();
        }

        if (requestContent == null) {
            requestContent = unescapeCarriageReturnsIn(CompressedStringSupport.getString(getConfig().getRequest()));
        }

        return requestContent;
    }

    public void setRequestContent(String request) {
        String old = getRequestContent();

        if ((StringUtils.isNullOrEmpty(request) && StringUtils.isNullOrEmpty(old))
                || (request != null && request.equals(old))) {
            return;
        }

        requestContent = request;
        notifyPropertyChanged(REQUEST_PROPERTY, old, request);
    }

    private String unescapeCarriageReturnsIn(String request) {
        if (request == null) {
            return null;
        }
        String modifiedRequest = request.replaceAll("\\\\r", "\r");
        modifiedRequest = modifiedRequest.replaceAll(CR_ESCAPE_SEQUENCE, "\\\\r");
        return modifiedRequest;
    }

    private String escapeCarriageReturnsIn(String request) {
        if (request == null) {
            return null;
        }
        String modifiedRequest = request.replaceAll("\\\\r", CR_ESCAPE_SEQUENCE);
        modifiedRequest = modifiedRequest.replaceAll("\r", "\\\\r");
        return modifiedRequest;
    }

    public boolean isPrettyPrint() {
        return getSettings().getBoolean(WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES);
    }

    public void setPrettyPrint(boolean prettyPrint) {
        boolean old = getSettings().getBoolean(WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES);
        getSettings().setBoolean(WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES, prettyPrint);
        notifyPropertyChanged(WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES, old, prettyPrint);
    }

    public void setEndpoint(String endpoint) {
        if (getOperation() != null) {
            getOperation().getInterface().getProject().getEndpointSupport()
                    .setEndpoint((AbstractHttpRequest<AbstractRequestConfig>) this, endpoint);
        } else {
            String old = getEndpoint();
            if (old != null && old.equals(endpoint)) {
                return;
            }

            getConfig().setEndpoint(endpoint);
            notifyPropertyChanged(ENDPOINT_PROPERTY, old, endpoint);
        }
    }

    public String getEndpoint() {
        if (getOperation() != null) {
            return getOperation().getInterface().getProject().getEndpointSupport()
                    .getEndpoint((AbstractHttpRequest<AbstractRequestConfig>) this);
        } else {
            return getConfig().getEndpoint();
        }
    }

    public String getEncoding() {
        return getConfig().getEncoding();
    }

    public void setEncoding(String encoding) {
        String old = getEncoding();
        getConfig().setEncoding(encoding);
        notifyPropertyChanged(ENCODING_PROPERTY, old, encoding);
    }

    public String getTimeout() {
        return getConfig().getTimeout();
    }

    public void setTimeout(String timeout) {
        String old = getTimeout();
        getConfig().setTimeout(timeout);
        notifyPropertyChanged("timeout", old, timeout);
    }

    public StringToStringsMap getRequestHeaders() {
        return StringToStringsMap.fromXml(getSettings().getString(REQUEST_HEADERS_PROPERTY, null));
    }

    public RequestIconAnimator<?> getIconAnimator() {
        return iconAnimator;
    }

    /**
     * Added for backwards compatibility
     *
     * @param map
     */

    public void setRequestHeaders(StringToStringMap map) {
        setRequestHeaders(new StringToStringsMap(map));
    }

    public void setRequestHeaders(StringToStringsMap map) {
        StringToStringsMap old = getRequestHeaders();
        getSettings().setString(REQUEST_HEADERS_PROPERTY, map.toXml());
        notifyPropertyChanged(REQUEST_HEADERS_PROPERTY, old, map);
    }

    @Override
    public ImageIcon getIcon() {
        return iconAnimator == null ? null : iconAnimator.getIcon();
    }

    public PropertyExpansion[] getPropertyExpansions() {
        PropertyExpansionsResult result = new PropertyExpansionsResult(this, this);

        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(this, this, "requestContent"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(this, this, "endpoint"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(this, this, "username"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(this, this, "password"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(this, this, "domain"));

        StringToStringsMap requestHeaders = getRequestHeaders();
        for (String key : requestHeaders.keySet()) {
            for (String value : requestHeaders.get(key)) {
                result.extractAndAddAll(new RequestHeaderHolder(key, value, this), "value");
            }
        }

        return result.toArray();
    }

    public String getUsername() {
        CredentialsConfig credentialsConfig = getConfig().getCredentials();
        if (credentialsConfig == null) {
            return null;
        }

        return credentialsConfig.getUsername();
    }

    public String getPassword() {
        CredentialsConfig credentialsConfig = getConfig().getCredentials();
        if (credentialsConfig == null) {
            return null;
        }

        return credentialsConfig.getPassword();
    }

    public String getDomain() {
        CredentialsConfig credentialsConfig = getConfig().getCredentials();
        if (credentialsConfig == null) {
            return null;
        }

        return credentialsConfig.getDomain();
    }

    public String getSelectedAuthProfile() {
        CredentialsConfig credentialsConfig = getCredentialsConfig();
        String selectedAuthProfile = credentialsConfig.getSelectedAuthProfile();
        if (selectedAuthProfile == null) {
            //For backward compatibility (4.6.4 or earlier projects)
            String authType = getAuthType();

            if (AuthType.PREEMPTIVE.toString().equals(authType)
                    || AuthType.GLOBAL_HTTP_SETTINGS.toString().equals(authType)) {
                addBasicProfileAndRemoveGlobalHttpSettingsAndPreEmptive(BASIC_AUTH_PROFILE);
                return BASIC_AUTH_PROFILE;
            } else if (AuthType.NTLM.toString().equals(authType) || AuthType.SPNEGO_KERBEROS.toString().equals(authType)) {
                addBasicAuthenticationProfile(authType);
                return authType;
            }

            return CredentialsConfig.AuthType.NO_AUTHORIZATION.toString();
        }
        //For 5.0 Alpha backward compatibility, where we still supported these types before merging them into one 'Basic'
        else if (AuthType.PREEMPTIVE.toString().equals(selectedAuthProfile)
                || AuthType.GLOBAL_HTTP_SETTINGS.toString().equals(selectedAuthProfile)) {
            addBasicProfileAndRemoveGlobalHttpSettingsAndPreEmptive(BASIC_AUTH_PROFILE);
            return BASIC_AUTH_PROFILE;
        }

        return selectedAuthProfile;
    }

    private void addBasicProfileAndRemoveGlobalHttpSettingsAndPreEmptive(String authType) {
        addBasicAuthenticationProfile(authType);
        removeGlobalHttpSettingsAndPreEmptiveProfiles();
    }

    private void removeGlobalHttpSettingsAndPreEmptiveProfiles() {
        removeBasicAuthenticationProfile(AuthType.PREEMPTIVE.toString());
        removeBasicAuthenticationProfile(AuthType.GLOBAL_HTTP_SETTINGS.toString());
    }

    public Set<String> getBasicAuthenticationProfiles() {
        Set<String> authTypes = new HashSet<String>();
        CredentialsConfig credentialsConfig = getConfig().getCredentials();
        if (credentialsConfig != null) {
            for (String type : credentialsConfig.getAddedBasicAuthenticationTypesList()) {
                if (AuthType.PREEMPTIVE.toString().equals(type)
                        || AuthType.GLOBAL_HTTP_SETTINGS.toString().equals(type)) {
                    authTypes.add(BASIC_AUTH_PROFILE);
                } else {
                    authTypes.add(type);
                }
            }
        }

        if (authTypes.contains(BASIC_AUTH_PROFILE)) {
            removeGlobalHttpSettingsAndPreEmptiveProfiles();
        }
        return authTypes;
    }

    public String getAuthType() {
        CredentialsConfig credentialsConfig = getCredentialsConfig();

        initializeAuthType(credentialsConfig);

        return credentialsConfig.getAuthType().toString();
    }

    private void initializeAuthType(CredentialsConfig credentialsConfig) {
        try {
            if (credentialsConfig.getAuthType() == null) {
                credentialsConfig.setAuthType(CredentialsConfig.AuthType.NO_AUTHORIZATION);
            }
        } catch (XmlValueOutOfRangeException e) {
            // Migration from deleted enum NTLM/Kerberos
            credentialsConfig.setAuthType(AuthType.NTLM);
        }
    }

    public void addBasicAuthenticationProfile(String authType) {
        List<String> addedBasicAuthenticationTypesList = getCredentialsConfig().getAddedBasicAuthenticationTypesList();
        if (!addedBasicAuthenticationTypesList.contains(authType)) {
            addedBasicAuthenticationTypesList.add(authType);
        }
    }

    public void removeBasicAuthenticationProfile(String authType) {
        CredentialsConfig credentialsConfig = getCredentialsConfig();
        for (int count = 0; count < credentialsConfig.sizeOfAddedBasicAuthenticationTypesArray(); count++) {
            if (credentialsConfig.getAddedBasicAuthenticationTypesArray(count).equals(authType)) {
                credentialsConfig.removeAddedBasicAuthenticationTypes(count);
                break;
            }
        }
    }

    public void setUsername(String username) {
        String old = getUsername();
        CredentialsConfig credentialsConfig = getCredentialsConfig();

        credentialsConfig.setUsername(username);
        notifyPropertyChanged("username", old, username);
    }

    public void setPassword(String password) {
        String old = getPassword();
        CredentialsConfig credentialsConfig = getCredentialsConfig();

        credentialsConfig.setPassword(password);
        notifyPropertyChanged("password", old, password);
    }

    public void setDomain(String domain) {
        String old = getDomain();
        CredentialsConfig credentialsConfig = getCredentialsConfig();

        credentialsConfig.setDomain(domain);
        notifyPropertyChanged("domain", old, domain);
    }

    public void setSelectedAuthProfileAndAuthType(String authProfile, AuthType.Enum authType) {
        setSelectedAuthProfile(authProfile);
        setAuthType(authType);
    }

    public CredentialsConfig.AuthType.Enum getBasicAuthType(String selectedProfile) {
        if (AbstractHttpRequest.BASIC_AUTH_PROFILE.equals(selectedProfile)) {
            if (getPreemptive()) {
                return CredentialsConfig.AuthType.PREEMPTIVE;
            } else {
                return CredentialsConfig.AuthType.GLOBAL_HTTP_SETTINGS;
            }
        } else {
            return CredentialsConfig.AuthType.Enum.forString(selectedProfile);
        }
    }

    private void setSelectedAuthProfile(String authProfile) {
        String old = getSelectedAuthProfile();
        CredentialsConfig credentialsConfig = getCredentialsConfig();

        credentialsConfig.setSelectedAuthProfile(authProfile);
        notifyPropertyChanged(SELECTED_AUTH_PROFILE_PROPERTY_NAME, old, authProfile);
    }

    private void setAuthType(AuthType.Enum authType) {
        if (authType != null
                && !AuthType.O_AUTH_2_0.equals(authType)
                && !AuthType.NO_AUTHORIZATION.equals(authType)
                && !AuthType.O_AUTH_1_0.equals(authType)) {
            if (authType.equals(AuthType.PREEMPTIVE) || authType.equals(AuthType.GLOBAL_HTTP_SETTINGS)) {
                addBasicAuthenticationProfile(BASIC_AUTH_PROFILE);
            } else {
                addBasicAuthenticationProfile(authType.toString());
            }
        }

        String old = getAuthType();
        CredentialsConfig credentialsConfig = getCredentialsConfig();

        credentialsConfig.setAuthType(authType);
        notifyPropertyChanged("authType", old, authType);
    }

    public boolean getPreemptive() {
        CredentialsConfig credentialsConfig = getCredentialsConfig();
        if (AuthType.PREEMPTIVE.toString().equals(getAuthType()) && !credentialsConfig.getPreemptive()) {
            credentialsConfig.setPreemptive(true);
        }
        return credentialsConfig.getPreemptive();
    }

    public void setPreemptive(boolean preemptive) {
        boolean old = getPreemptive();
        getCredentialsConfig().setPreemptive(preemptive);
        notifyPropertyChanged("preemptive", old, preemptive);
    }

    public String getSslKeystore() {
        return getConfig().getSslKeystore();
    }

    public void setSslKeystore(String sslKeystore) {
        String old = getSslKeystore();
        getConfig().setSslKeystore(sslKeystore);
        notifyPropertyChanged("sslKeystore", old, sslKeystore);
    }

    public String getBindAddress() {
        return getSettings().getString(BIND_ADDRESS, "");
    }

    public void setBindAddress(String bindAddress) {
        String old = getSettings().getString(BIND_ADDRESS, "");
        getSettings().setString(BIND_ADDRESS, bindAddress);
        notifyPropertyChanged(BIND_ADDRESS, old, bindAddress);
    }

    public long getMaxSize() {
        return getSettings().getLong(MAX_SIZE, 0);
    }

    public void setMaxSize(long maxSize) {
        long old = getSettings().getLong(MAX_SIZE, 0);
        getSettings().setLong(MAX_SIZE, maxSize);
        notifyPropertyChanged(MAX_SIZE, old, maxSize);
    }

    public String getDumpFile() {
        return dumpFile.get();
    }

    public void setDumpFile(String df) {
        String old = getDumpFile();
        dumpFile.set(df, false);
        notifyPropertyChanged(DUMP_FILE, old, getDumpFile());
    }

    public boolean isRemoveEmptyContent() {
        return getSettings().getBoolean(REMOVE_EMPTY_CONTENT);
    }

    public void setRemoveEmptyContent(boolean removeEmptyContent) {
        boolean old = getSettings().getBoolean(REMOVE_EMPTY_CONTENT);
        getSettings().setBoolean(REMOVE_EMPTY_CONTENT, removeEmptyContent);
        notifyPropertyChanged(REMOVE_EMPTY_CONTENT, old, removeEmptyContent);
    }

    public boolean isStripWhitespaces() {
        return getSettings().getBoolean(STRIP_WHITESPACES);
    }

    public void setStripWhitespaces(boolean stripWhitespaces) {
        boolean old = getSettings().getBoolean(STRIP_WHITESPACES);
        getSettings().setBoolean(STRIP_WHITESPACES, stripWhitespaces);
        notifyPropertyChanged(STRIP_WHITESPACES, old, stripWhitespaces);
    }

    public boolean isFollowRedirects() {
        if (!getSettings().isSet(FOLLOW_REDIRECTS)) {
            return true;
        } else {
            return getSettings().getBoolean(FOLLOW_REDIRECTS);
        }
    }

    public void setFollowRedirects(boolean followRedirects) {
        boolean old = getSettings().getBoolean(FOLLOW_REDIRECTS);
        getSettings().setBoolean(FOLLOW_REDIRECTS, followRedirects);
        notifyPropertyChanged(FOLLOW_REDIRECTS, old, followRedirects);
    }

    @Override
    public void beforeSave() {
        super.beforeSave();

        if (requestContent != null) {
            if (getConfig().getRequest() == null) {
                getConfig().addNewRequest();
            }

            CompressedStringSupport.setString(getConfig().getRequest(),  escapeCarriageReturnsIn(requestContent) );
            // requestContent = null;
        }
    }

    private CredentialsConfig getCredentialsConfig() {
        CredentialsConfig credentialsConfig = getConfig().getCredentials();
        if (credentialsConfig == null) {
            credentialsConfig = getConfig().addNewCredentials();
        }
        return credentialsConfig;
    }

    public static class RequestIconAnimator<T extends AbstractHttpRequest<?>> extends IconAnimator<T> implements
            SubmitListener {
        public RequestIconAnimator(T modelItem, String baseIcon, String animIcon, int iconCounts) {
            super(modelItem, baseIcon, animIcon, iconCounts);
        }

        public boolean beforeSubmit(Submit submit, SubmitContext context) {
            if (isEnabled() && submit.getRequest() == getTarget()) {
                start();
            }
            return true;
        }

        public void afterSubmit(Submit submit, SubmitContext context) {
            if (submit.getRequest() == getTarget()) {
                stop();
            }
        }
    }

    public void setIconAnimator(RequestIconAnimator<?> iconAnimator) {
        if (this.iconAnimator != null) {
            removeSubmitListener(this.iconAnimator);
        }

        this.iconAnimator = iconAnimator;
        if (SoapUI.usingGraphicalEnvironment()) {
            addSubmitListener(this.iconAnimator);
        }
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response, SubmitContext context) {
        HttpResponse oldResponse = getResponse();
        this.response = response;

        notifyPropertyChanged(RESPONSE_PROPERTY, oldResponse, response);
    }

    public void resolve(ResolveContext<?> context) {
        super.resolve(context);

        for (FileAttachment<?> attachment : attachments) {
            attachment.resolve(context);
        }
    }

    @Override
    public void addExternalDependencies(List<ExternalDependency> dependencies) {
        super.addExternalDependencies(dependencies);

        for (FileAttachment<?> attachment : attachments) {
            attachment.addExternalDependency(dependencies);
        }
    }

    public boolean hasEndpoint() {
        return StringUtils.hasContent(getEndpoint());
    }

    public void setAfterRequestInjection(IAfterRequestInjection afterRequestInjection) {
        this.afterRequestInjection = afterRequestInjection;
    }

    public IAfterRequestInjection getAfterRequestInjection() {
        return afterRequestInjection;
    }
}
