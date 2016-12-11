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

package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.AttachmentDataSource;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.AttachmentUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.RestRequestDataSource;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.RestRequestMimeMessageRequestEntity;
import com.eviware.soapui.impl.wsdl.support.FileAttachment;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequest;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import com.eviware.soapui.support.types.StringToStringMap;
import org.apache.commons.httpclient.URI;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.xmlbeans.XmlBoolean;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.PreencodedMimeBodyPart;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.eviware.soapui.impl.support.HttpUtils.urlEncodeWithUtf8;

/**
 * RequestFilter that adds SOAP specific headers
 *
 * @author Ole.Matzura
 */

public class HttpRequestFilter extends AbstractRequestFilter {
    @Override
    public void filterHttpRequest(SubmitContext context, HttpRequestInterface<?> request) {
        HttpRequestBase httpMethod = (HttpRequestBase) context.getProperty(BaseHttpRequestTransport.HTTP_METHOD);

        String path = PropertyExpander.expandProperties(context, request.getPath());
        StringBuilder query = new StringBuilder();
        String encoding = System.getProperty("soapui.request.encoding", StringUtils.unquote(request.getEncoding()));

        StringToStringMap responseProperties = (StringToStringMap) context
                .getProperty(BaseHttpRequestTransport.RESPONSE_PROPERTIES);

        MimeMultipart formMp = ("multipart/form-data".equals(request.getMediaType())
                || "multipart/mixed".equals(request.getMediaType()))
                && httpMethod instanceof HttpEntityEnclosingRequestBase ? new MimeMultipart() : null;

        RestParamsPropertyHolder params = request.getParams();

        for (int c = 0; c < params.getPropertyCount(); c++) {
            RestParamProperty param = params.getPropertyAt(c);

            String value = PropertyExpander.expandProperties(context, param.getValue());
            responseProperties.put(param.getName(), value);

            List<String> valueParts = sendEmptyParameters(request)
                    || (!StringUtils.hasContent(value) && param.getRequired()) ? RestUtils
                    .splitMultipleParametersEmptyIncluded(value, request.getMultiValueDelimiter()) : RestUtils
                    .splitMultipleParameters(value, request.getMultiValueDelimiter());

            // skip HEADER and TEMPLATE parameter encoding (TEMPLATE is encoded by
            // the URI handling further down)
            if (value != null && param.getStyle() != ParameterStyle.HEADER && param.getStyle() != ParameterStyle.TEMPLATE
                    && !param.isDisableUrlEncoding()) {
                try {
                    if (StringUtils.hasContent(encoding)) {
                        value = URLEncoder.encode(value, encoding);
                        for (int i = 0; i < valueParts.size(); i++) {
                            valueParts.set(i, URLEncoder.encode(valueParts.get(i), encoding));
                        }
                    } else {
                        value = urlEncodeWithUtf8(value);
                        for (int i = 0; i < valueParts.size(); i++) {
                            valueParts.set(i, urlEncodeWithUtf8(valueParts.get(i)));
                        }
                    }
                } catch (UnsupportedEncodingException e1) {
                    SoapUI.logError(e1);
                    value = urlEncodeWithUtf8(value);
                    for (int i = 0; i < valueParts.size(); i++) {
                        valueParts.set(i, urlEncodeWithUtf8(valueParts.get(i)));
                    }
                }
                // URLEncoder replaces space with "+", but we want "%20".
                value = value.replaceAll("\\+", "%20");
                for (int i = 0; i < valueParts.size(); i++) {
                    valueParts.set(i, valueParts.get(i).replaceAll("\\+", "%20"));
                }
            }

            if (param.getStyle() == ParameterStyle.QUERY && !sendEmptyParameters(request)) {
                if (!StringUtils.hasContent(value) && !param.getRequired()) {
                    continue;
                }
            }

            switch (param.getStyle()) {
                case HEADER:
                    for (String valuePart : valueParts) {
                        httpMethod.addHeader(param.getName(), valuePart);
                    }
                    break;
                case QUERY:
                    if (formMp == null || !request.isPostQueryString()) {
                        for (String valuePart : valueParts) {
                            if (query.length() > 0) {
                                query.append('&');
                            }

                            query.append(urlEncodeWithUtf8(param.getName()));
                            query.append('=');
                            if (StringUtils.hasContent(valuePart)) {
                                query.append(valuePart);
                            }
                        }
                    } else {
                        try {
                            addFormMultipart(request, formMp, param.getName(), responseProperties.get(param.getName()));
                        } catch (MessagingException e) {
                            SoapUI.logError(e);
                        }
                    }

                    break;
                case TEMPLATE:
                    try {
                        value = getEncodedValue(value, encoding, param.isDisableUrlEncoding(), request
                                .getSettings().getBoolean(HttpSettings.ENCODED_URLS));
                        path = path.replaceAll("\\{" + param.getName() + "\\}", value == null ? "" : value);
                    } catch (UnsupportedEncodingException e) {
                        SoapUI.logError(e);
                    }
                    break;
                case MATRIX:
                    try {
                        value = getEncodedValue(value, encoding, param.isDisableUrlEncoding(), request
                                .getSettings().getBoolean(HttpSettings.ENCODED_URLS));
                    } catch (UnsupportedEncodingException e) {
                        SoapUI.logError(e);
                    }

                    if (param.getType().equals(XmlBoolean.type.getName())) {
                        if (value.toUpperCase().equals("TRUE") || value.equals("1")) {
                            path += ";" + param.getName();
                        }
                    } else {
                        path += ";" + param.getName();
                        if (StringUtils.hasContent(value)) {
                            path += "=" + value;
                        }
                    }
                    break;
                case PLAIN:
                    break;
            }
        }

        if (request.getSettings().getBoolean(HttpSettings.FORWARD_SLASHES)) {
            path = PathUtils.fixForwardSlashesInPath(path);
        }

        if (PathUtils.isHttpPath(path)) {
            try {
                // URI(String) automatically URLencodes the input, so we need to
                // decode it first...
                URI uri = new URI(path, request.getSettings().getBoolean(HttpSettings.ENCODED_URLS));
                context.setProperty(BaseHttpRequestTransport.REQUEST_URI, uri);
                java.net.URI oldUri = httpMethod.getURI();
                httpMethod
                        .setURI(HttpUtils.createUri(oldUri.getScheme(), oldUri.getRawUserInfo(), oldUri.getHost(), oldUri.getPort(), oldUri.getRawPath(),
                                uri.getEscapedQuery(), oldUri.getRawFragment()));
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        } else if (StringUtils.hasContent(path)) {
            try {
                java.net.URI oldUri = httpMethod.getURI();
                String pathToSet = StringUtils.hasContent(oldUri.getRawPath()) && !"/".equals(oldUri.getRawPath()) ? oldUri.getRawPath() + path : path;
                java.net.URI newUri = URIUtils.createURI(oldUri.getScheme(), oldUri.getHost(), oldUri.getPort(),
                        pathToSet, oldUri.getQuery(), oldUri.getFragment());
                httpMethod.setURI(newUri);
                context.setProperty(BaseHttpRequestTransport.REQUEST_URI, new URI(newUri.toString(), request
                        .getSettings().getBoolean(HttpSettings.ENCODED_URLS)));
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }

        if (query.length() > 0 && !request.isPostQueryString()) {
            try {
                java.net.URI oldUri = httpMethod.getURI();
                httpMethod.setURI(URIUtils.createURI(oldUri.getScheme(), oldUri.getHost(), oldUri.getPort(),
                        oldUri.getRawPath(), query.toString(), oldUri.getFragment()));
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }

        if (formMp != null) {
            // create request message
            try {
                if (request.hasRequestBody() && httpMethod instanceof HttpEntityEnclosingRequest) {
                    String requestContent = PropertyExpander.expandProperties(context, request.getRequestContent(),
                            request.isEntitizeProperties());
                    if (StringUtils.hasContent(requestContent)) {
                        initRootPart(request, requestContent, formMp);
                    }
                }

                for (Attachment attachment : request.getAttachments()) {
                    MimeBodyPart part = new PreencodedMimeBodyPart("binary");

                    if (attachment instanceof FileAttachment<?>) {
                        String name = attachment.getName();
                        if (StringUtils.hasContent(attachment.getContentID()) && !name.equals(attachment.getContentID())) {
                            name = attachment.getContentID();
                        }

                        part.setDisposition("form-data; name=\"" + name + "\"; filename=\"" + attachment.getName() + "\"");
                    } else {
                        part.setDisposition("form-data; name=\"" + attachment.getName() + "\"");
                    }

                    part.setDataHandler(new DataHandler(new AttachmentDataSource(attachment)));

                    formMp.addBodyPart(part);
                }

                MimeMessage message = new MimeMessage(AttachmentUtils.JAVAMAIL_SESSION);
                message.setContent(formMp);
                message.saveChanges();
                RestRequestMimeMessageRequestEntity mimeMessageRequestEntity = new RestRequestMimeMessageRequestEntity(
                        message, request);
                ((HttpEntityEnclosingRequest) httpMethod).setEntity(mimeMessageRequestEntity);
                httpMethod.setHeader("Content-Type", mimeMessageRequestEntity.getContentType().getValue());
                httpMethod.setHeader("MIME-Version", "1.0");
            } catch (Throwable e) {
                SoapUI.logError(e);
            }
        } else if (request.hasRequestBody() && httpMethod instanceof HttpEntityEnclosingRequest) {
            if (StringUtils.hasContent(request.getMediaType())) {
                httpMethod.setHeader("Content-Type", getContentTypeHeader(request.getMediaType(), encoding));
            }

            if (request.isPostQueryString()) {
                try {
                    ((HttpEntityEnclosingRequest) httpMethod).setEntity(new StringEntity(query.toString()));
                } catch (UnsupportedEncodingException e) {
                    SoapUI.logError(e);
                }
            } else {
                String requestContent = PropertyExpander.expandProperties(context, request.getRequestContent(),
                        request.isEntitizeProperties());
                List<Attachment> attachments = new ArrayList<Attachment>();

                for (Attachment attachment : request.getAttachments()) {
                    if (attachment.getContentType().equals(request.getMediaType())) {
                        attachments.add(attachment);
                    }
                }

                if (StringUtils.hasContent(requestContent) && attachments.isEmpty()) {
                    try {
                        byte[] content = encoding == null ? requestContent.getBytes() : requestContent.getBytes(encoding);
                        ((HttpEntityEnclosingRequest) httpMethod).setEntity(new ByteArrayEntity(content));
                    } catch (UnsupportedEncodingException e) {
                        ((HttpEntityEnclosingRequest) httpMethod).setEntity(new ByteArrayEntity(requestContent
                                .getBytes()));
                    }
                } else if (attachments.size() > 0) {
                    try {
                        MimeMultipart mp = null;

                        if (StringUtils.hasContent(requestContent)) {
                            mp = new MimeMultipart();
                            initRootPart(request, requestContent, mp);
                        } else if (attachments.size() == 1) {
                            ((HttpEntityEnclosingRequest) httpMethod).setEntity(new InputStreamEntity(attachments.get(0)
                                    .getInputStream(), -1));

                            httpMethod.setHeader("Content-Type", getContentTypeHeader(request.getMediaType(), encoding));
                        }

                        if (((HttpEntityEnclosingRequest) httpMethod).getEntity() == null) {
                            if (mp == null) {
                                mp = new MimeMultipart();
                            }

                            // init mimeparts
                            AttachmentUtils.addMimeParts(request, attachments, mp, new StringToStringMap());

                            // create request message
                            MimeMessage message = new MimeMessage(AttachmentUtils.JAVAMAIL_SESSION);
                            message.setContent(mp);
                            message.saveChanges();
                            RestRequestMimeMessageRequestEntity mimeMessageRequestEntity = new RestRequestMimeMessageRequestEntity(
                                    message, request);
                            ((HttpEntityEnclosingRequest) httpMethod).setEntity(mimeMessageRequestEntity);
                            httpMethod.setHeader("Content-Type",
                                    getContentTypeHeader(mimeMessageRequestEntity.getContentType().getValue(), encoding));
                            httpMethod.setHeader("MIME-Version", "1.0");
                        }
                    } catch (Exception e) {
                        SoapUI.logError(e);
                    }
                }
            }
        }
    }

    private boolean sendEmptyParameters(HttpRequestInterface<?> request) {
        return request instanceof HttpTestRequest && ((HttpTestRequest) request).isSendEmptyParameters();
    }

    private String getContentTypeHeader(String contentType, String encoding) {
        return (encoding == null || encoding.trim().length() == 0) ? contentType : contentType + ";charset=" + encoding;
    }

    private void addFormMultipart(HttpRequestInterface<?> request, MimeMultipart formMp, String name, String value)
            throws MessagingException {
        MimeBodyPart part = new MimeBodyPart();

        if (value.startsWith("file:")) {
            String fileName = value.substring(5);
            File file = new File(fileName);
            part.setDisposition("form-data; name=\"" + name + "\"; filename=\"" + file.getName() + "\"");
            if (file.exists()) {
                part.setDataHandler(new DataHandler(new FileDataSource(file)));
            } else {
                for (Attachment attachment : request.getAttachments()) {
                    if (attachment.getName().equals(fileName)) {
                        part.setDataHandler(new DataHandler(new AttachmentDataSource(attachment)));
                        break;
                    }
                }
            }

            part.setHeader("Content-Type", ContentTypeHandler.getContentTypeFromFilename(file.getName()));
            part.setHeader("Content-Transfer-Encoding", "binary");
        } else {
            part.setDisposition("form-data; name=\"" + name + "\"");
            part.setText(value, System.getProperty("soapui.request.encoding", request.getEncoding()));
        }

        formMp.addBodyPart(part);
    }

    protected void initRootPart(HttpRequestInterface<?> wsdlRequest, String requestContent, MimeMultipart mp)
            throws MessagingException {
        MimeBodyPart rootPart = new PreencodedMimeBodyPart("8bit");
        // rootPart.setContentID( AttachmentUtils.ROOTPART_SOAPUI_ORG );
        mp.addBodyPart(rootPart, 0);

        DataHandler dataHandler = new DataHandler(new RestRequestDataSource(wsdlRequest, requestContent));
        rootPart.setDataHandler(dataHandler);
    }

    protected String getEncodedValue(String value, String encoding, boolean isDisableUrlEncoding, boolean isPreEncoded) throws UnsupportedEncodingException {

        if (value == null) {
            return "";
        }

        // get default encoding if there is no encoding set
        if (!StringUtils.hasContent(encoding)) {
            encoding = System.getProperty("file.encoding");
        }

        if (isAlreadyEncoded(value, encoding)) {
            // Already encoded so we don't do anything
            return value;
        } else if (isDisableUrlEncoding || isPreEncoded) {
            // If encoding is disabled or it is pre-encoded then we don't encode
            return value;
        } else {
            // encoding NOT disabled neither it is pre-encoded, so we encode here
            String encodedValue = URLEncoder.encode(value, encoding);
            // URLEncoder replaces space with "+", but we want "%20".
            return encodedValue.replaceAll("\\+", "%20");
        }

    }

    protected boolean isAlreadyEncoded(String path, String encoding) throws UnsupportedEncodingException {
        String decodedPath = java.net.URLDecoder.decode(path, encoding);
        return !path.equals(decodedPath);

    }

}
