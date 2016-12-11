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

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments;

import com.eviware.soapui.SoapUI;

import javax.activation.DataSource;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DataSource for multipart attachments
 *
 * @author ole.matzura
 */

public class MultipartAttachmentDataSource implements DataSource {
    private final MimeMultipart multipart;

    public MultipartAttachmentDataSource(MimeMultipart multipart) {
        this.multipart = multipart;
    }

    public String getContentType() {
        return multipart.getContentType();
    }

    public InputStream getInputStream() throws IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            multipart.writeTo(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            SoapUI.logError(e);
            return null;
        }
    }

    public String getName() {
        return multipart.toString();
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}
