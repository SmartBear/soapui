/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.eviware.soapui.model.iface.Attachment;

/**
 * Standard DataSource for existing attachments in SoapUI
 *
 * @author ole.matzura
 */

public class AttachmentDataSource implements DataSource {
    private final Attachment attachment;

    public AttachmentDataSource(Attachment attachment) {
        this.attachment = attachment;
    }

    public String getContentType() {
        return attachment.getContentType();
    }

    public InputStream getInputStream() throws IOException {
        try {
            return attachment.getInputStream();
        } catch (Exception e) {
            throw new IOException(e.toString());
        }
    }

    public String getName() {
        return attachment.getName();
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}
