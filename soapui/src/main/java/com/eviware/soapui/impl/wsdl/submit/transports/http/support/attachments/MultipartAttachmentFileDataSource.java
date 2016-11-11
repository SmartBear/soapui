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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MultipartAttachmentFileDataSource implements DataSource {
    private final MimeMultipart multipart;

    public MultipartAttachmentFileDataSource(MimeMultipart multipart) {
        this.multipart = multipart;
    }

    public String getContentType() {
        return multipart.getContentType();
    }

    public InputStream getInputStream() throws IOException {
        try {
            File file = File.createTempFile("largeAttachment", ".tmp");
            file.deleteOnExit();

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            multipart.writeTo(out);

            out.flush();
            out.close();

            return new BufferedInputStream(new FileInputStream(file));
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
