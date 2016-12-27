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
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DataSource for a BodyPart
 *
 * @author ole.matzura
 */

public class BodyPartDataSource implements DataSource {
    private final BodyPart bodyPart;

    public BodyPartDataSource(BodyPart bodyPart) {
        this.bodyPart = bodyPart;
    }

    public String getContentType() {
        try {
            return bodyPart.getContentType();
        } catch (MessagingException e) {
            SoapUI.logError(e);
            return null;
        }
    }

    public InputStream getInputStream() throws IOException {
        try {
            return bodyPart.getInputStream();
        } catch (MessagingException e) {
            SoapUI.logError(e);
            return null;
        }
    }

    public String getName() {
        try {
            return bodyPart.getHeader("Content-ID")[0];
        } catch (MessagingException e) {
            SoapUI.logError(e);
            return null;
        }
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }

}
