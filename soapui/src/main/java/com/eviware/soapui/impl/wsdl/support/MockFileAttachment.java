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
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;

import java.io.File;
import java.io.IOException;

/**
 * Attachment for a WsdlMockResponse
 *
 * @author Ole.Matzura
 */

public class MockFileAttachment extends FileAttachment<WsdlMockResponse> {
    public MockFileAttachment(AttachmentConfig config, WsdlMockResponse mockResponse) {
        super(mockResponse, config);
    }

    public MockFileAttachment(File file, boolean cache, WsdlMockResponse response) throws IOException {
        super(response, file, cache, response.getConfig().addNewAttachment());
    }

    @Override
    public AttachmentType getAttachmentType() {
        if (getPart() == null || getModelItem().getAttachmentPart(getPart()) == null) {
            return AttachmentType.UNKNOWN;
        } else {
            return getModelItem().getAttachmentPart(getPart()).getAttachmentType();
        }
    }

    public AttachmentEncoding getEncoding() {
        if (getModelItem().isEncodeAttachments()) {
            return getModelItem().getAttachmentEncoding(getPart());
        } else {
            return AttachmentEncoding.NONE;
        }
    }

    @Override
    public String getId() {
        return null;
    }
}
