/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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
import com.eviware.soapui.config.PartsConfig;
import com.eviware.soapui.config.PartsConfig.Part;
import com.eviware.soapui.impl.wsdl.AttachmentContainer;
import com.eviware.soapui.impl.wsdl.HttpAttachmentPart;
import com.eviware.soapui.impl.wsdl.WsdlAttachmentContainer;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.support.MessageXmlPart;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlValidator;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Attachment.AttachmentEncoding;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBase64Binary;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlHexBinary;
import org.apache.xmlbeans.XmlObject;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.PreencodedMimeBodyPart;
import javax.wsdl.Input;
import javax.wsdl.Output;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Attachment-related utility classes
 *
 * @author ole.matzura
 */

public class AttachmentUtils {
    private final static Logger log = LogManager.getLogger(AttachmentUtils.class);
    private static final QName XMLMIME_CONTENTTYPE_200505 = new QName("http://www.w3.org/2005/05/xmlmime",
            "contentType");
    private static final QName XMLMIME_CONTENTTYPE_200411 = new QName("http://www.w3.org/2004/11/xmlmime",
            "contentType");
    private static final QName SWAREF_QNAME = new QName("http://ws-i.org/profiles/basic/1.1/xsd", "swaRef");
    public static final QName XOP_HREF_QNAME = new QName("href");
    private static final QName XOP_INCLUDE_QNAME = new QName("http://www.w3.org/2004/08/xop/include", "Include");
    public static final String ROOTPART_SOAPUI_ORG = "<rootpart@soapui.org>";
    public static final long MAX_SIZE_IN_MEMORY_ATTACHMENT = 500 * 1024;

    public static boolean prepareMessagePart(WsdlAttachmentContainer container, MimeMultipart mp,
                                             MessageXmlPart messagePart, StringToStringMap contentIds) throws Exception, MessagingException {
        boolean isXop = false;

        XmlObjectTreeModel treeModel = null;
        XmlCursor cursor = messagePart.newCursor();
        XmlObject rootXmlObject = cursor.getObject();

        try {
            while (!cursor.isEnddoc()) {
                if (cursor.isContainer()) {
                    // could be an attachment part (as of "old" SwA specs which
                    // specify a content
                    // element referring to the attachment)
                    if (messagePart.isAttachmentPart()) {
                        String href = cursor.getAttributeText(XOP_HREF_QNAME);
                        if (href != null && href.length() > 0) {
                            contentIds.put(messagePart.getPart().getName(), href);
                        }

                        break;
                    }

                    XmlObject xmlObj = cursor.getObject();
                    SchemaType schemaType = xmlObj.schemaType();
                    if (schemaType.isNoType()) {
                        if (treeModel == null) {
                            treeModel = new XmlObjectTreeModel(messagePart.getSchemaType().getTypeSystem(), rootXmlObject);
                        }

                        XmlTreeNode tn = treeModel.getXmlTreeNode(xmlObj);
                        if (tn != null) {
                            schemaType = tn.getSchemaType();
                        }
                    }

                    if (AttachmentUtils.isSwaRefType(schemaType)) {
                        String textContent = XmlUtils.getNodeValue(cursor.getDomNode());
                        if (StringUtils.hasContent(textContent) && textContent.startsWith("cid:")) {
                            textContent = textContent.substring(4);

                            try {
                                // is the textcontent already a URI?
                                new URI(textContent);
                                contentIds.put(textContent, textContent);
                            } catch (RuntimeException e) {
                                // not a URI.. try to create one..
                                String contentId = textContent + "@soapui.org";
                                cursor.setTextValue("cid:" + contentId);
                                contentIds.put(textContent, contentId);
                            }
                        }
                    } else if (AttachmentUtils.isXopInclude(schemaType)) {
                        String contentId = cursor.getAttributeText(new QName("href"));
                        if (contentId != null && contentId.length() > 0) {
                            contentIds.put(contentId, contentId);
                            isXop = true;

                            Attachment[] attachments = container.getAttachmentsForPart(contentId);
                            if (attachments.length == 1) {
                                XmlCursor cur = cursor.newCursor();
                                if (cur.toParent()) {
                                    String contentType = getXmlMimeContentType(cur);
                                    if (contentType != null && contentType.length() > 0) {
                                        attachments[0].setContentType(contentType);
                                    }
                                }

                                cur.dispose();
                            }
                        }
                    } else {
                        // extract contentId
                        String textContent = XmlUtils.getNodeValue(cursor.getDomNode());
                        if (StringUtils.hasContent(textContent)) {
                            Attachment attachment = null;
                            boolean isXopAttachment = false;

                            // is content a reference to a file?
                            if (container.isInlineFilesEnabled() && textContent.startsWith("file:")) {
                                String filename = textContent.substring(5);
                                if (container.isMtomEnabled()) {
                                    MimeBodyPart part = new PreencodedMimeBodyPart("binary");
                                    String xmimeContentType = getXmlMimeContentType(cursor);

                                    if (StringUtils.isNullOrEmpty(xmimeContentType)) {
                                        xmimeContentType = ContentTypeHandler.getContentTypeFromFilename(filename);
                                    }

                                    part.setDataHandler(new DataHandler(new XOPPartDataSource(new File(filename),
                                            xmimeContentType, schemaType)));
                                    part.setContentID("<" + filename + ">");
                                    mp.addBodyPart(part);

                                    isXopAttachment = true;
                                } else {
                                    if (new File(filename).exists()) {
                                        inlineData(cursor, schemaType, new FileInputStream(filename));
                                    } else {
                                        Attachment att = getAttachmentForFilename(container, filename);
                                        if (att != null) {
                                            inlineData(cursor, schemaType, att.getInputStream());
                                        }
                                    }
                                }
                            } else {
                                Attachment[] attachmentsForPart = container.getAttachmentsForPart(textContent);
                                if (textContent.startsWith("cid:")) {
                                    textContent = textContent.substring(4);
                                    attachmentsForPart = container.getAttachmentsForPart(textContent);

                                    Attachment[] attachments = attachmentsForPart;
                                    if (attachments.length == 1) {
                                        attachment = attachments[0];
                                    } else if (attachments.length > 1) {
                                        attachment = buildMulitpartAttachment(attachments);
                                    }

                                    isXopAttachment = container.isMtomEnabled();
                                    contentIds.put(textContent, textContent);
                                }
                                // content should be binary data; is this an XOP element
                                // which should be serialized with MTOM?
                                else if (container.isMtomEnabled()
                                        && (SchemaUtils.isBinaryType(schemaType) || SchemaUtils.isAnyType(schemaType))) {
                                    if ("true".equals(System.getProperty("soapui.mtom.strict"))) {
                                        if (SchemaUtils.isAnyType(schemaType)) {
                                            textContent = null;
                                        } else {
                                            for (int c = 0; c < textContent.length(); c++) {
                                                if (Character.isWhitespace(textContent.charAt(c))) {
                                                    textContent = null;
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    if (textContent != null) {
                                        MimeBodyPart part = new PreencodedMimeBodyPart("binary");
                                        String xmimeContentType = getXmlMimeContentType(cursor);

                                        part.setDataHandler(new DataHandler(new XOPPartDataSource(textContent,
                                                xmimeContentType, schemaType)));

                                        textContent = "http://www.soapui.org/" + System.nanoTime();

                                        part.setContentID("<" + textContent + ">");
                                        mp.addBodyPart(part);

                                        isXopAttachment = true;
                                    }
                                } else if (container.isInlineFilesEnabled() && attachmentsForPart != null
                                        && attachmentsForPart.length > 0) {
                                    attachment = attachmentsForPart[0];
                                }
                            }

                            // add XOP include?
                            if (isXopAttachment && container.isMtomEnabled()) {
                                buildXopInclude(cursor, textContent);
                                isXop = true;
                            }
                            // inline?
                            else if (attachment != null) {
                                inlineAttachment(cursor, schemaType, attachment);
                            }
                        }
                    }
                }

                cursor.toNextToken();
            }
        } finally {
            cursor.dispose();
        }

        return isXop;
    }

    private static Attachment getAttachmentForFilename(WsdlAttachmentContainer container, String filename) {
        for (Attachment attachment : container.getAttachments()) {
            if (filename.equals(attachment.getName())) {
                return attachment;
            }
        }

        return null;
    }

    private static void inlineAttachment(XmlCursor cursor, SchemaType schemaType, Attachment attachment)
            throws Exception {
        inlineData(cursor, schemaType, attachment.getInputStream());
    }

    private static void inlineData(XmlCursor cursor, SchemaType schemaType, InputStream in) throws IOException {
        String content = null;
        byte[] data = Tools.readAll(in, -1).toByteArray();

        if (SchemaUtils.isInstanceOf(schemaType, XmlHexBinary.type)) {
            content = new String(Hex.encodeHex(data));
        } else if (SchemaUtils.isInstanceOf(schemaType, XmlBase64Binary.type)) {
            content = new String(Base64.encodeBase64(data));
        } else {
            content = new String(data);
        }

        XmlCursor c = cursor.newCursor();
        c.setTextValue(content);
        c.dispose();
    }

    private static void buildXopInclude(XmlCursor cursor, String contentId) {
        // build xop:Include
        XmlCursor c = cursor.newCursor();
        c.removeXmlContents();
        c.toFirstContentToken();
        c.beginElement(XOP_INCLUDE_QNAME);
        c.insertAttributeWithValue(XOP_HREF_QNAME, "cid:" + contentId);
        c.toNextSibling();
        c.removeXml();
        c.dispose();
    }

    private static Attachment buildMulitpartAttachment(Attachment[] attachments) {
        System.out.println("buildMulitpartAttachment(Attachment[] attachments) not implemented!");
        return null;
    }

    public static String buildRootPartContentType(String action, SoapVersion soapVersion) {
        String contentType = "application/xop+xml; charset=UTF-8; type=\"" + soapVersion.getContentType();
        if (soapVersion == SoapVersion.Soap12) {
            contentType += "\"; action=\"" + action;
        }
        return contentType + "\"";
    }

    public static String buildMTOMContentType(String header, String action, SoapVersion soapVersion) {
        int ix = header.indexOf("boundary");
        String contentType = "multipart/related; type=\"application/xop+xml\"; start=\"" + ROOTPART_SOAPUI_ORG + "\"; "
                + "start-info=\"" + soapVersion.getContentType();

        if (soapVersion == SoapVersion.Soap12 && action != null) {
            contentType += "\"; action=\"" + action;
        }

        // nested or not? see
        // http://www.eviware.com/forums/index.php?topic=2866.new#new
        // contentType += "; action=\\\"" + action + "\\\"\"; action=\"" + action;

        return contentType + "\"; " + header.substring(ix);
    }

    public static boolean isSwaRefType(SchemaType schemaType) {
        return schemaType != null && schemaType.getName() != null && schemaType.getName().equals(SWAREF_QNAME);
    }

    public static String getXmlMimeContentType(XmlCursor cursor) {
        String attributeText = cursor.getAttributeText(XMLMIME_CONTENTTYPE_200411);
        if (attributeText == null) {
            attributeText = cursor.getAttributeText(XMLMIME_CONTENTTYPE_200505);
        }
        return attributeText;
    }

    public static AttachmentEncoding getAttachmentEncoding(WsdlOperation operation,
                                                           HttpAttachmentPart httpAttachmentPart, boolean isResponse) {
        if (httpAttachmentPart.getSchemaType() != null) {
            if (SchemaUtils.isInstanceOf(httpAttachmentPart.getSchemaType(), XmlBase64Binary.type)) {
                return AttachmentEncoding.BASE64;
            } else if (SchemaUtils.isInstanceOf(httpAttachmentPart.getSchemaType(), XmlHexBinary.type)) {
                return AttachmentEncoding.HEX;
            }
        }

        return getAttachmentEncoding(operation, httpAttachmentPart.getName(), isResponse);
    }

    public static AttachmentEncoding getAttachmentEncoding(WsdlOperation operation, String partName, boolean isResponse) {
        // make sure we have access
        if (operation == null || operation.getBindingOperation() == null
                || operation.getBindingOperation().getOperation() == null) {
            return AttachmentEncoding.NONE;
        }

        javax.wsdl.Part part = null;

        if (isResponse) {
            Output output = operation.getBindingOperation().getOperation().getOutput();
            if (output == null || output.getMessage() == null) {
                return AttachmentEncoding.NONE;
            } else {
                part = output.getMessage().getPart(partName);
            }
        } else {
            Input input = operation.getBindingOperation().getOperation().getInput();
            if (input == null || input.getMessage() == null) {
                return AttachmentEncoding.NONE;
            } else {
                part = input.getMessage().getPart(partName);
            }
        }

        if (part != null) {
            QName typeName = part.getTypeName();
            if (typeName.getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema")) {
                if (typeName.getLocalPart().equals("base64Binary")) {
                    return AttachmentEncoding.BASE64;
                } else if (typeName.getLocalPart().equals("hexBinary")) {
                    return AttachmentEncoding.HEX;
                }
            }
        }

        return AttachmentEncoding.NONE;
    }

    public static boolean isXopInclude(SchemaType schemaType) {
        return XOP_INCLUDE_QNAME.equals(schemaType.getName());
    }

    public static List<HttpAttachmentPart> extractAttachmentParts(WsdlOperation operation, String messageContent,
                                                                  boolean addAnonymous, boolean isResponse, boolean forceMtom) {
        List<HttpAttachmentPart> result = new ArrayList<HttpAttachmentPart>();

        PartsConfig messageParts = isResponse ? operation.getConfig().getResponseParts() : operation.getConfig()
                .getRequestParts();
        if (messageParts != null) {
            for (Part part : messageParts.getPartList()) {
                HttpAttachmentPart attachmentPart = new HttpAttachmentPart(part.getName(), part.getContentTypeList());
                attachmentPart.setType(Attachment.AttachmentType.MIME);
                result.add(attachmentPart);
            }
        }

        if (messageContent.length() > 0) {
            WsdlContext wsdlContext = operation.getInterface().getWsdlContext();
            WsdlValidator validator = new WsdlValidator(wsdlContext);
            try {
                XmlObject[] requestDocuments = validator.getMessageParts(messageContent, operation.getName(), isResponse);

                for (XmlObject partDoc : requestDocuments) {
                    XmlCursor cursor = partDoc.newCursor();
                    while (!cursor.isEnddoc()) {
                        if (cursor.isContainer()) {
                            SchemaType schemaType = cursor.getObject().schemaType();
                            if (schemaType != null) {
                                String attributeText = AttachmentUtils.getXmlMimeContentType(cursor);

                                // xop?
                                if (SchemaUtils.isBinaryType(schemaType) || SchemaUtils.isAnyType(schemaType)) {
                                    String contentId = cursor.getTextValue();
                                    if (contentId.startsWith("cid:")) {
                                        HttpAttachmentPart attachmentPart = new HttpAttachmentPart(contentId.substring(4),
                                                attributeText);
                                        attachmentPart
                                                .setType(attributeText == null && !forceMtom ? Attachment.AttachmentType.CONTENT
                                                        : Attachment.AttachmentType.XOP);
                                        result.add(attachmentPart);
                                    }
                                } else if (AttachmentUtils.isXopInclude(schemaType)) {
                                    String contentId = cursor.getAttributeText(new QName("href"));
                                    if (contentId != null && contentId.length() > 0) {
                                        HttpAttachmentPart attachmentPart = new HttpAttachmentPart(contentId, attributeText);
                                        attachmentPart.setType(Attachment.AttachmentType.XOP);
                                        result.add(attachmentPart);
                                    }
                                }
                                // swaref?
                                else if (AttachmentUtils.isSwaRefType(schemaType)) {
                                    String contentId = cursor.getTextValue();
                                    if (contentId.startsWith("cid:")) {
                                        HttpAttachmentPart attachmentPart = new HttpAttachmentPart(contentId.substring(4),
                                                attributeText);
                                        attachmentPart.setType(Attachment.AttachmentType.SWAREF);
                                        result.add(attachmentPart);
                                    }
                                }
                            }
                        }

                        cursor.toNextToken();
                    }
                }
            } catch (Exception e) {
                if (e instanceof NullPointerException) {
                    SoapUI.logError(e);
                }
                log.warn(e.getMessage());
            }
        }

        if (addAnonymous) {
            result.add(new HttpAttachmentPart());
        }

        return result;
    }

    /**
     * Adds defined attachments as mimeparts
     */

    public static void addMimeParts(AttachmentContainer container, List<Attachment> attachments, MimeMultipart mp,
                                    StringToStringMap contentIds) throws MessagingException {
        // no multipart handling?
        if (!container.isMultipartEnabled()) {
            for (int c = 0; c < attachments.size(); c++) {
                Attachment att = attachments.get(c);
                if (att.getAttachmentType() != Attachment.AttachmentType.CONTENT) {
                    addSingleAttachment(mp, contentIds, att);
                }
            }
        } else {
            // first identify if any part has more than one attachments
            Map<String, List<Attachment>> attachmentsMap = new HashMap<String, List<Attachment>>();
            for (int c = 0; c < attachments.size(); c++) {
                Attachment att = attachments.get(c);
                if (att.getAttachmentType() == Attachment.AttachmentType.CONTENT) {
                    continue;
                }

                String partName = att.getPart();

                if (!attachmentsMap.containsKey(partName)) {
                    attachmentsMap.put(partName, new ArrayList<Attachment>());
                }

                attachmentsMap.get(partName).add(att);
            }

            // add attachments
            for (Iterator<String> i = attachmentsMap.keySet().iterator(); i.hasNext(); ) {
                attachments = attachmentsMap.get(i.next());
                if (attachments.size() == 1) {
                    Attachment att = attachments.get(0);
                    addSingleAttachment(mp, contentIds, att);
                }
                // more than one attachment with the same part -> create multipart
                // attachment
                else if (attachments.size() > 1) {
                    addMultipartAttachment(mp, contentIds, attachments);
                }
            }
        }
    }

    /**
     * Adds a mulitpart MimeBodyPart from an array of attachments
     */

    public static void addMultipartAttachment(MimeMultipart mp, StringToStringMap contentIds,
                                              List<Attachment> attachments) throws MessagingException {
        MimeMultipart multipart = new MimeMultipart("mixed");
        long totalSize = 0;

        for (int c = 0; c < attachments.size(); c++) {
            Attachment att = attachments.get(c);
            String contentType = att.getContentType();
            totalSize += att.getSize();

            MimeBodyPart part = contentType.startsWith("text/") ? new MimeBodyPart() : new PreencodedMimeBodyPart(
                    "binary");

            part.setDataHandler(new DataHandler(new AttachmentDataSource(att)));
            initPartContentId(contentIds, part, att, false);
            multipart.addBodyPart(part);
        }

        MimeBodyPart part = new PreencodedMimeBodyPart("binary");

        if (totalSize > MAX_SIZE_IN_MEMORY_ATTACHMENT) {
            part.setDataHandler(new DataHandler(new MultipartAttachmentFileDataSource(multipart)));
        } else {
            part.setDataHandler(new DataHandler(new MultipartAttachmentDataSource(multipart)));
        }

        Attachment attachment = attachments.get(0);
        initPartContentId(contentIds, part, attachment, true);

        mp.addBodyPart(part);
    }

    public static void initPartContentId(StringToStringMap contentIds, MimeBodyPart part, Attachment attachment,
                                         boolean isMultipart) throws MessagingException {
        String partName = attachment.getPart();

        String contentID = attachment.getContentID();
        if (StringUtils.hasContent(contentID)) {
            contentID = contentID.trim();
            int ix = contentID.indexOf(' ');
            if (ix != -1) {
                part.setContentID("<" + (isMultipart ? contentID.substring(ix + 1) : contentID.substring(0, ix))
                        + ">");
            } else {
                if (!contentID.startsWith("<")) {
                    contentID = "<" + contentID;
                }

                if (!contentID.endsWith(">")) {
                    contentID = contentID + ">";
                }

                part.setContentID(contentID);
            }
        } else if (partName != null && !partName.equals(HttpAttachmentPart.ANONYMOUS_NAME)) {
            if (contentIds.containsKey(partName)) {
                part.setContentID("<" + contentIds.get(partName) + ">");
            } else {
                part.setContentID("<" + partName + "=" + System.nanoTime() + "@soapui.org>");
            }
        }

        // set content-disposition
        String name = attachment.getName();
        String file = attachment.getUrl();
        if (PathUtils.isFilePath(file)) {
            int ix = file.lastIndexOf(File.separatorChar);
            if (ix == -1) {
                ix = file.lastIndexOf('/');
            }

            if (ix > 0 && ix < file.length() - 1) {
                file = file.substring(ix + 1);
            }

            part.setDisposition("attachment; name=\"" + name + "\"; filename=\"" + file + "\"");
        } else {
            part.setDisposition("attachment; name=\"" + name + "\"");
        }
    }

    /**
     * Adds a simple MimeBodyPart from an attachment
     */

    public static void addSingleAttachment(MimeMultipart mp, StringToStringMap contentIds, Attachment att)
            throws MessagingException {
        String contentType = att.getContentType();
        MimeBodyPart part = contentType.startsWith("text/") ? new MimeBodyPart()
                : new PreencodedMimeBodyPart("binary");

        part.setDataHandler(new DataHandler(new AttachmentDataSource(att)));
        initPartContentId(contentIds, part, att, false);

        mp.addBodyPart(part);
    }

    public static final Session JAVAMAIL_SESSION = Session.getDefaultInstance(new Properties());
}
