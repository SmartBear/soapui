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
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.WsdlRequestMimeMessageRequestEntity.DummyOutputStream;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.model.mock.MockResponse;
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
 * MimeMessage response for a WsdlMockResponse
 *
 * @author ole.matzura
 */

public class MimeMessageMockResponseEntity extends AbstractHttpEntity {
    private final MimeMessage message;
    private final boolean isXOP;
    private final MockResponse mockResponse;

    public MimeMessageMockResponseEntity(MimeMessage message, boolean isXOP, MockResponse response) {
        this.message = message;
        this.isXOP = isXOP;
        this.mockResponse = response;
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
            if (mockResponse instanceof WsdlMockResponse) {
                SoapVersion soapVersion = ((WsdlMockResponse) mockResponse).getSoapVersion();

                if (isXOP) {
                    String header = message.getHeader("Content-Type")[0];
                    return new BasicHeader("Content-Type", AttachmentUtils.buildMTOMContentType(header, null, soapVersion));
                } else {
                    String header = message.getHeader("Content-Type")[0];
                    int ix = header.indexOf("boundary");
                    return new BasicHeader("Content-Type", "multipart/related; type=\"" + soapVersion.getContentType()
                            + "\"; start=\"" + AttachmentUtils.ROOTPART_SOAPUI_ORG + "\"; " + header.substring(ix));
                }
            } else {
                throw new IllegalStateException("Multipart support is only available for SOAP");
            }

        } catch (MessagingException e) {
            SoapUI.logError(e);
        }

        return null;
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
