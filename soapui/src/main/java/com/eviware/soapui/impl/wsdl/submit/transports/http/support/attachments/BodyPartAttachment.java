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
import com.eviware.soapui.impl.support.AbstractHttpOperation;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

/**
 * Attachment for a BodyPart
 *
 * @author ole.matzura
 */

public class BodyPartAttachment implements Attachment {
    private final BodyPart bodyPart;
    private File tempFile;
    private AbstractHttpOperation operation;
    private final boolean isRequest;
    private byte[] data;
    private AttachmentType attachmentType;

    public BodyPartAttachment(BodyPart bodyPart, AbstractHttpOperation operation, boolean isRequest,
                              AttachmentType attachmentType) {
        this.bodyPart = bodyPart;
        this.operation = operation;
        this.isRequest = isRequest;
        this.attachmentType = attachmentType;
    }

    public BodyPart getBodyPart() {
        return bodyPart;
    }

    public String getContentType() {
        try {
            return bodyPart.getContentType();
        } catch (MessagingException e) {
            SoapUI.logError(e);
            return null;
        }
    }

    public AttachmentEncoding getEncoding() {
        return operation == null ? AttachmentEncoding.NONE : operation.getAttachmentEncoding(getPart(), !isRequest);
    }

    public synchronized InputStream getInputStream() throws Exception {
        if (data != null) {
            return new ByteArrayInputStream(data);
        }

        AttachmentEncoding encoding = getEncoding();
        if (encoding == AttachmentEncoding.NONE) {
            return bodyPart.getInputStream();
        }

        data = Tools.readAll(bodyPart.getInputStream(), Tools.READ_ALL).toByteArray();

        if (encoding == AttachmentEncoding.BASE64) {
            if (Base64.isArrayByteBase64(data)) {
                data = Tools.readAll(new ByteArrayInputStream(Base64.decodeBase64(data)), Tools.READ_ALL)
                        .toByteArray();
            } else {
                throw new Exception("Attachment content for part [" + getPart() + "] is not base64 encoded");
            }
        } else if (encoding == AttachmentEncoding.HEX) {
            data = Hex.decodeHex(new String(data).toCharArray());
        }

        return new ByteArrayInputStream(data);
    }

    public String getName() {
        try {
            String[] values = bodyPart.getHeader("Content-Disposition");
            String disposition = values == null || values.length == 0 ? null : values[0];
            String name = HttpUtils.extractHttpHeaderParameter(disposition, "name");
            if (StringUtils.hasContent(name)) {
                return name;
            }

            values = bodyPart.getHeader("Content-Type");
            disposition = values == null || values.length == 0 ? null : values[0];
            name = HttpUtils.extractHttpHeaderParameter(disposition, "name");
            if (StringUtils.hasContent(name)) {
                return name;
            }

            String[] header = bodyPart.getHeader("Content-Id");
            if (header == null || header.length == 0) {
                return "<missing name>";
            }

            if (header[0].startsWith("<") && header[0].endsWith(">")) {
                header[0] = header[0].substring(1, header[0].length() - 1);
            }

            return header[0];
        } catch (MessagingException e) {
            SoapUI.logError(e);
            return null;
        }
    }

    public String getPart() {
        String name = getName();
        int ix = name.indexOf('=');
        if (ix > 0) {
            name = name.substring(0, ix);
        }

        return name;
    }

    public long getSize() {
        try {
            getInputStream();
            return data == null ? bodyPart.getSize() : data.length;
        } catch (Exception e) {
            SoapUI.logError(e);
            return -1;
        }
    }

    public String getUrl() {
        if (tempFile == null) {
            String contentType = getContentType();
            int ix = contentType.lastIndexOf('/');
            int iy = -1;
            if (ix != -1) {
                iy = contentType.indexOf(';', ix);
            }

            try {
                tempFile = File.createTempFile(
                        "response-attachment",
                        (ix == -1 ? ".dat" : "."
                                + (iy == -1 ? contentType.substring(ix + 1) : contentType.substring(ix + 1, iy))));

                OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
                InputStream inputStream = getInputStream();
                out.write(Tools.readAll(inputStream, 0).toByteArray());
                out.flush();
                out.close();

                inputStream.reset();
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }

        try {
            return tempFile.toURI().toURL().toString();
        } catch (MalformedURLException e) {
            SoapUI.logError(e);
            return null;
        }
    }

    public void setContentType(String contentType) {
    }

    public void setPart(String part) {
    }

    public boolean isCached() {
        return true;
    }

    public AttachmentType getAttachmentType() {
        return attachmentType == null ? AttachmentType.UNKNOWN : attachmentType;
    }

    public void release() {
        operation = null;
    }

    public String getContentID() {
        try {
            String[] header = bodyPart.getHeader("Content-ID");
            if (header != null && header.length > 0) {
                return header[0];
            }
        } catch (MessagingException e) {
            SoapUI.logError(e);
        }

        return null;
    }

    public void setOperation(WsdlOperation operation) {
        this.operation = operation;
    }

    public void setAttachmentType(AttachmentType attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getContentEncoding() {
        AttachmentEncoding encoding = getEncoding();
        if (encoding == AttachmentEncoding.BASE64) {
            return "base64";
        } else if (encoding == AttachmentEncoding.HEX) {
            return "hex";
        } else {
            return "binary";
        }
    }

    @Override
    public String getId() {
        return null;
    }
}
