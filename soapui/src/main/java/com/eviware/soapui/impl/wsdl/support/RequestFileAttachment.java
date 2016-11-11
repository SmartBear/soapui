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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlAttachmentContainer;

import java.io.File;
import java.io.IOException;

/**
 * Attachment for a WsdlRequest
 *
 * @author ole.matzura
 */

public class RequestFileAttachment extends FileAttachment<AbstractHttpRequest<?>> {
    public RequestFileAttachment(AttachmentConfig config, AbstractHttpRequestInterface<?> request) {
        super((AbstractHttpRequest<?>) request, config);
    }

    public RequestFileAttachment(File file, boolean cache, AbstractHttpRequest<?> request) throws IOException {
        super(request, file, cache, request.getConfig().addNewAttachment());
    }

    public AttachmentEncoding getEncoding() {
        AbstractHttpRequestInterface<?> request = getModelItem();
        if (request instanceof WsdlAttachmentContainer && ((WsdlAttachmentContainer) request).isEncodeAttachments()) {
            return ((WsdlAttachmentContainer) request).getAttachmentEncoding(getPart());
        } else {
            return AttachmentEncoding.NONE;
        }
    }

    @Override
    public AttachmentType getAttachmentType() {
        if (getModelItem() == null || getPart() == null || getModelItem().getAttachmentPart(getPart()) == null) {
            return AttachmentType.UNKNOWN;
        } else {
            return getModelItem().getAttachmentPart(getPart()).getAttachmentType();
        }
    }

    public String toString() {
        return getName();
    }

    @Override
    public String getId() {
        return getConfig().getId();
    }
}
