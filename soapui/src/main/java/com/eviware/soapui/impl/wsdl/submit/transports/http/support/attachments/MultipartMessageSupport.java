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
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Attachment.AttachmentType;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.xml.XmlUtils;

import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing large MultiParts
 *
 * @author ole.matzura
 */

public class MultipartMessageSupport {
    private final List<BodyPartAttachment> attachments = new ArrayList<BodyPartAttachment>();
    private Attachment rootPart;
    private MimeMessage message;
    private String responseContent;
    private boolean prettyPrint;

    public MultipartMessageSupport(DataSource dataSource, String rootPartId, AbstractHttpOperation operation,
                                   boolean isRequest, boolean prettyPrint) throws MessagingException {
        this.prettyPrint = prettyPrint;
        MimeMultipart mp = new MimeMultipart(dataSource);
        message = new MimeMessage(AttachmentUtils.JAVAMAIL_SESSION);
        message.setContent(mp);

        AttachmentType attachmentType = AttachmentType.MIME;

        for (int c = 0; c < mp.getCount(); c++) {
            BodyPart bodyPart = mp.getBodyPart(c);

            String contentType = bodyPart.getContentType().toUpperCase();
            if (contentType.startsWith("APPLICATION/XOP+XML")) {
                attachmentType = AttachmentType.XOP;
            }

            if (contentType.startsWith("MULTIPART/")) {
                MimeMultipart mp2 = new MimeMultipart(new BodyPartDataSource(bodyPart));
                for (int i = 0; i < mp2.getCount(); i++) {
                    attachments.add(new BodyPartAttachment(mp2.getBodyPart(i), operation, isRequest, attachmentType));
                }
            } else {
                BodyPartAttachment attachment = new BodyPartAttachment(bodyPart, operation, isRequest, attachmentType);

                String[] contentIdHeaders = bodyPart.getHeader("Content-ID");
                if (contentIdHeaders != null && contentIdHeaders.length > 0 && contentIdHeaders[0].equals(rootPartId)) {
                    rootPart = attachment;
                } else {
                    attachments.add(attachment);
                }
            }
        }

        // if no explicit root part has been set, use the first one in the result
        if (operation != null && rootPart == null) {
            rootPart = attachments.remove(0);
        }

        if (rootPart != null) {
            ((BodyPartAttachment) rootPart).setAttachmentType(AttachmentType.CONTENT);
        }
    }

    public void setOperation(WsdlOperation operation) {
        for (BodyPartAttachment attachment : attachments) {
            attachment.setOperation(operation);
        }
    }

    public Attachment[] getAttachments() {
        return attachments.toArray(new Attachment[attachments.size()]);
    }

    public Attachment getRootPart() {
        return rootPart;
    }

    public Attachment[] getAttachmentsForPart(String partName) {
        List<Attachment> results = new ArrayList<Attachment>();

        for (Attachment attachment : attachments) {
            if (attachment.getPart().equals(partName)) {
                results.add(attachment);
            }
        }

        return results.toArray(new Attachment[results.size()]);
    }

    public String getResponseContent() {
        if (rootPart == null) {
            return null;
        }

        if (responseContent == null) {
            try {
                InputStream in = rootPart.getInputStream();
                ByteArrayOutputStream out = Tools.readAll(in, Tools.READ_ALL);
                byte[] data = out.toByteArray();
                int contentOffset = 0;

                String contentType = rootPart.getContentType();
                if (contentType != null && data.length > 0) {
                    String charset = null;
                    if (contentType.indexOf("charset=") > 0) {
                        try {
                            int ix = contentType.indexOf("charset=");
                            int ix2 = contentType.indexOf(";", ix);

                            charset = ix2 == -1 ? contentType.substring(ix + 8) : contentType.substring(ix + 8, ix2);
                        } catch (Throwable e) {
                            SoapUI.logError(e);
                        }
                    }

                    int ix = contentType.indexOf(';');
                    if (ix > 0) {
                        contentType = contentType.substring(0, ix);
                        if (contentType.toLowerCase().endsWith("xml")) {
                            if (data.length > 3 && data[0] == (byte) 239 && data[1] == (byte) 187 && data[2] == (byte) 191) {
                                charset = "UTF-8";
                                contentOffset = 3;
                            }
                        }
                    }

                    charset = StringUtils.unquote(charset);

                    responseContent = charset == null ? new String(data) : new String(data, contentOffset,
                            (int) (data.length - contentOffset), charset);
                }

                if (responseContent == null) {
                    responseContent = new String(data);
                }

                return responseContent;
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }

        return responseContent;
    }

    public String getContentAsString() {
        if (responseContent == null) {
            getResponseContent();
        }

        if (prettyPrint) {
            responseContent = XmlUtils.prettyPrintXml(responseContent);
            prettyPrint = false;
        }

        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    public Attachment getAttachmentWithContentId(String contentId) {
        for (Attachment attachment : attachments) {
            if (contentId.equals(attachment.getContentID())) {
                return attachment;
            }
        }

        return null;
    }
}
