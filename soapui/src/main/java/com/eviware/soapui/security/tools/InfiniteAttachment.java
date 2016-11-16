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

package com.eviware.soapui.security.tools;

import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.support.RequestFileAttachment;
import com.eviware.soapui.model.iface.Attachment;

import java.io.IOException;
import java.io.InputStream;

public class InfiniteAttachment extends RequestFileAttachment {
    private long maxSize;

    public InfiniteAttachment(AttachmentConfig config, AbstractHttpRequestInterface<?> request, long maxSize) {
        super(config, request);
        this.maxSize = maxSize;
    }

    public InputStream getInputStream() throws IOException {
        return new InfiniteInputStream(maxSize);
    }

    public AttachmentType getAttachmentType() {
        return Attachment.AttachmentType.UNKNOWN;
    }

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }
}
