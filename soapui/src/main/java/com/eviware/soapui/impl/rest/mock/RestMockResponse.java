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

package com.eviware.soapui.impl.rest.mock;


import com.eviware.soapui.config.RESTMockResponseConfig;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.support.AbstractMockResponse;
import com.eviware.soapui.impl.support.http.MediaType;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import org.apache.ws.security.WSSecurityException;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

public class RestMockResponse extends AbstractMockResponse<RESTMockResponseConfig> implements MediaType {
    public final static String MOCKRESULT_PROPERTY = RestMockResponse.class.getName() + "@mockresult";
    public static final String ICON_NAME = "/restMockResponse.gif";

    public RestMockResponse(RestMockAction action, RESTMockResponseConfig config) {
        super(config, action, ICON_NAME);
    }

    @Override
    public int getAttachmentCount() {
        return 0;
    }

    @Override
    public Attachment getAttachmentAt(int index) {
        return null;
    }

    @Override
    public Attachment[] getAttachmentsForPart(String partName) {
        return new Attachment[0];
    }

    @Override
    public MessagePart.AttachmentPart[] getDefinedAttachmentParts() {
        return new MessagePart.AttachmentPart[0];
    }

    @Override
    public MessagePart.AttachmentPart getAttachmentPart(String partName) {
        return null;
    }

    @Override
    public void addAttachmentsChangeListener(PropertyChangeListener listener) {

    }

    @Override
    public void removeAttachmentsChangeListener(PropertyChangeListener listener) {

    }

    @Override
    public boolean isMultipartEnabled() {
        return false;
    }

    @Override
    public boolean isMtomEnabled() {
        return false;
    }

    @Override
    public boolean isInlineFilesEnabled() {
        return false;
    }

    @Override
    public boolean isEncodeAttachments() {
        return false;
    }

    @Override
    public Attachment.AttachmentEncoding getAttachmentEncoding(String partName) {
        return null;
    }

    @Override
    public Attachment[] getAttachments() {
        return new Attachment[0];
    }

    @Override
    public MockOperation getMockOperation() {
        return (MockOperation) getParent();
    }

    @Override
    public String getScriptHelpUrl() {
        return HelpUrls.REST_MOCK_RESPONSE_SCRIPT;
    }

    @Override
    public Attachment attachFile(File file, boolean cache) throws IOException {
        return null;
    }

    @Override
    public void removeAttachment(Attachment attachment) {

    }

    @Override
    public PropertyExpansion[] getPropertyExpansions() {
        return new PropertyExpansion[0];
    }

    @Override
    public String getPropertiesLabel() {
        return null;
    }

    protected String mockresultProperty() {
        return MOCKRESULT_PROPERTY;
    }

    @Override
    protected String executeSpecifics(MockRequest request, String responseContent, WsdlMockRunContext context) throws IOException, WSSecurityException {
        return responseContent;
    }

    @Override
    public String getContentType() {
        if (getEncoding() != null) {
            return getMediaType() + "; " + getEncoding();
        }
        return getMediaType();
    }

    @Override
    protected String removeEmptyContent(String responseContent) {
        return responseContent;
    }

    @Override
    public long getResponseDelay() {
        return 0;
    }

    @Override
    public boolean isForceMtom() {
        return false;
    }

    @Override
    public boolean isStripWhitespaces() {
        return false;
    }

    @Override
    public String getMediaType() {
        return getConfig().isSetMediaType() ? getConfig().getMediaType() : RestRequestInterface.DEFAULT_MEDIATYPE;
    }

    @Override
    public void setMediaType(String mediaType) {
        getConfig().setMediaType(mediaType);
    }


    public void setContentType(String contentType) {
        String[] parts = contentType.split(";");
        getConfig().setMediaType(parts[0]);

        String encodingValue = getEncodingValue(parts);
        if (encodingValue != null) {
            setEncoding(encodingValue);
        }
    }

    protected String getEncodingValue(String[] parameters) {
        String encoding = null;

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].trim().startsWith("charset=")) {
                String[] encodingParts = parameters[i].split("=");

                if (encodingParts.length > 1) {
                    encoding = encodingParts[1];
                    return encoding;
                }
            }

        }

        return encoding;
    }

}
