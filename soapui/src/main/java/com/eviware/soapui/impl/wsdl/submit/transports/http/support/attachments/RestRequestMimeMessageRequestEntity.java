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
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.WsdlRequestMimeMessageRequestEntity.DummyOutputStream;
import org.apache.http.Header;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * MimeMessage request class
 *
 * @author ole.matzura
 */

public class RestRequestMimeMessageRequestEntity extends AbstractHttpEntity {
    private final MimeMessage message;
    private final HttpRequestInterface<?> restRequest;

    public RestRequestMimeMessageRequestEntity(MimeMessage message, HttpRequestInterface<?> restRequest) {
        this.message = message;
        this.restRequest = restRequest;
    }

    public long getContentLength() {
        try {
            DummyOutputStream out = new DummyOutputStream();
            writeTo(out);
            return out.getSize();
        } catch (Exception e) {
            SoapUI.logError(e);
            return -1;
        }
    }

    public Header getContentType() {
        try {
            String header = message.getHeader("Content-Type")[0];
            int ix = header.indexOf("boundary");

            return new BasicHeader("Content-Type", restRequest.getMediaType() + "; " + header.substring(ix));
        } catch (MessagingException e) {
            SoapUI.logError(e);
        }

        return new BasicHeader("Content-Type", restRequest.getMediaType());
    }

    public boolean isRepeatable() {
        return true;
    }

    public void writeTo(OutputStream arg0) throws IOException {
        try {
            arg0.write("\r\n".getBytes());
            ((MimeMultipart) message.getContent()).writeTo(arg0);
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        try {
            return message.getInputStream();
        } catch (MessagingException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isStreaming() {
        return false;
    }
}
