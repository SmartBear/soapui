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

package com.eviware.soapui.impl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.BaseMockResponseConfig;
import com.eviware.soapui.config.CompressedStringConfig;
import com.eviware.soapui.config.HeaderConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.MutableWsdlAttachmentContainer;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.AttachmentUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MimeMessageMockResponseEntity;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MockResponseDataSource;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.impl.wsdl.support.MapTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.MessageXmlObject;
import com.eviware.soapui.impl.wsdl.support.MessageXmlPart;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunContext;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.settings.CommonSettings;
import com.eviware.soapui.support.scripting.ScriptEnginePool;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.ws.security.WSSecurityException;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.PreencodedMimeBodyPart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractMockResponse<MockResponseConfigType extends BaseMockResponseConfig>
        extends AbstractWsdlModelItem<MockResponseConfigType>
        implements MockResponse, MutableWsdlAttachmentContainer, PropertyExpansionContainer, TestPropertyHolder {
    public static final String AUTO_RESPONSE_COMPRESSION = "<auto>";
    public static final String NO_RESPONSE_COMPRESSION = "<none>";
    private MapTestPropertyHolder propertyHolder;

    private String responseContent;
    private MockResult mockResult;
    private ScriptEnginePool scriptEnginePool;


    public AbstractMockResponse(MockResponseConfigType config, MockOperation operation, String icon) {
        super(config, operation, icon);
        scriptEnginePool = new ScriptEnginePool(this);
        scriptEnginePool.setScript(getScript());
        propertyHolder = new MapTestPropertyHolder(this);
        propertyHolder.addProperty("Request");

        if (!config.isSetHttpResponseStatus()) {
            config.setHttpResponseStatus("" + HttpStatus.SC_OK);
        }

    }

    @Override
    public void setConfig(MockResponseConfigType config) {
        super.setConfig(config);

        if (scriptEnginePool != null) {
            scriptEnginePool.setScript(getScript());
        }
    }

    public String getResponseContent() {
        if (getConfig().getResponseContent() == null) {
            getConfig().addNewResponseContent();
        }

        if (responseContent == null) {
            responseContent = CompressedStringSupport.getString(getConfig().getResponseContent());
        }

        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        String oldContent = getResponseContent();
        if (responseContent != null && responseContent.equals(oldContent)) {
            return;
        }

        this.responseContent = responseContent;

        setConfigResponseContent(responseContent);

        notifyPropertyChanged(RESPONSE_CONTENT_PROPERTY, oldContent, responseContent);
    }

    private void setConfigResponseContent(String responseContent) {
        CompressedStringConfig compressedResponseContent = CompressedStringConfig.Factory.newInstance();
        compressedResponseContent.setStringValue(responseContent);
        getConfig().setResponseContent(compressedResponseContent);
    }

    public StringToStringsMap getResponseHeaders() {
        StringToStringsMap result = new StringToStringsMap();
        List<HeaderConfig> headerList = getConfig().getHeaderList();
        for (HeaderConfig header : headerList) {
            result.add(header.getName(), header.getValue());
        }

        return result;
    }

    public void setResponseHttpStatus(int httpStatus) {
        getConfig().setHttpResponseStatus("" + httpStatus);
    }

    public int getResponseHttpStatus() {

        if (getConfig().getHttpResponseStatus() != null) {
            return Integer.valueOf(getConfig().getHttpResponseStatus());

        } else {
            return HttpStatus.SC_OK;
        }
    }

    public String getResponseCompression() {
        if (getConfig().isSetCompression()) {
            return getConfig().getCompression();
        } else {
            return AUTO_RESPONSE_COMPRESSION;
        }
    }

    public void setMockResult(MockResult mockResult) {
        MockResult oldResult = this.mockResult;
        this.mockResult = mockResult;
        notifyPropertyChanged(mockresultProperty(), oldResult, mockResult);
    }

    public MockResult getMockResult() {
        return mockResult;
    }

    protected abstract String mockresultProperty();

    public String getScript() {
        return getConfig().isSetScript() ? getConfig().getScript().getStringValue() : null;
    }

    public void evaluateScript(MockRequest request) throws Exception {
        String script = getScript();
        if (script == null || script.trim().length() == 0) {
            return;
        }

        MockService mockService = getMockOperation().getMockService();
        MockRunner mockRunner = mockService.getMockRunner();
        MockRunContext context =
                mockRunner == null ? new WsdlMockRunContext(mockService, null) : mockRunner.getMockContext();

        context.setMockResponse(this);

        SoapUIScriptEngine scriptEngine = scriptEnginePool.getScriptEngine();

        try {
            scriptEngine.setVariable("context", context);
            scriptEngine.setVariable("requestContext", request == null ? null : request.getRequestContext());
            scriptEngine.setVariable("mockContext", context);
            scriptEngine.setVariable("mockRequest", request);
            scriptEngine.setVariable("mockResponse", this);
            scriptEngine.setVariable("log", SoapUI.ensureGroovyLog());

            scriptEngine.run();
        } catch (RuntimeException e) {
            throw new Exception(e.getMessage(), e);
        } finally {
            scriptEnginePool.returnScriptEngine(scriptEngine);
        }
    }

    public void setScript(String script) {
        String oldScript = getScript();
        if (!script.equals(oldScript)) {
            if (!getConfig().isSetScript()) {
                getConfig().addNewScript();
            }
            getConfig().getScript().setStringValue(script);

            scriptEnginePool.setScript(script);
        }
    }

    @Override
    public void release() {
        super.release();
        scriptEnginePool.release();
    }

    public MockResult execute(MockRequest request, MockResult result) throws DispatchException {
        try {
            getProperty("Request").setValue(request.getRequestContent());

            long delay = getResponseDelay();
            if (delay > 0) {
                Thread.sleep(delay);
            }

            String script = getScript();
            if (script != null && script.trim().length() > 0) {
                evaluateScript(request);
            }

            String responseContent = getResponseContent();

            // create merged context
            WsdlMockRunContext context = new WsdlMockRunContext(request.getContext().getMockService(), null);
            context.setMockResponse(this);

            // casting below cause WsdlMockRunContext is both a MockRunContext AND a Map<String,Object>
            context.putAll((WsdlMockRunContext) request.getContext());
            context.putAll((WsdlMockRunContext) request.getRequestContext());

            StringToStringsMap responseHeaders = getResponseHeaders();
            for (Map.Entry<String, List<String>> headerEntry : responseHeaders.entrySet()) {
                for (String value : headerEntry.getValue()) {
                    result.addHeader(headerEntry.getKey(), PropertyExpander.expandProperties(context, value));
                }
            }

            responseContent = PropertyExpander.expandProperties(context, responseContent, isEntitizeProperties());

            responseContent = executeSpecifics(request, responseContent, context);

            if (!result.isCommitted()) {
                responseContent = writeResponse(result, responseContent);
            }

            result.setResponseContent(responseContent);

            setMockResult(result);

            return result;
        } catch (Throwable e) {
            SoapUI.logError(e);
            throw new DispatchException(e);
        }
    }

    public String writeResponse(MockResult result, String responseContent) throws Exception {
        MimeMultipart mp = null;

        Operation operation = getMockOperation().getOperation();

        // variables needed for both multipart sections....
        boolean isXOP = isMtomEnabled() && isForceMtom();
        StringToStringMap contentIds = new StringToStringMap();

        // only support multipart for wsdl currently.....
        if (operation instanceof WsdlOperation) {
            if (operation == null) {
                throw new IllegalStateException("Missing WsdlOperation for mock response");
            }


            // preprocess only if neccessary
            if (isMtomEnabled() || isInlineFilesEnabled() || getAttachmentCount() > 0) {
                try {
                    mp = new MimeMultipart();

                    WsdlOperation wsdlOperation = ((WsdlOperation) operation);
                    MessageXmlObject requestXmlObject = createMessageXmlObject(responseContent, wsdlOperation);
                    MessageXmlPart[] requestParts = requestXmlObject.getMessageParts();
                    for (MessageXmlPart requestPart : requestParts) {
                        if (prepareMessagePart(mp, contentIds, requestPart)) {
                            isXOP = true;
                        }
                    }
                    responseContent = requestXmlObject.getMessageContent();
                } catch (Exception e) {
                    SoapUI.logError(e);
                }
            }

            responseContent = removeEmptyContent(responseContent);
        }

        if (isStripWhitespaces()) {
            responseContent = XmlUtils.stripWhitespaces(responseContent);
        }

        MockRequest request = result.getMockRequest();
        request.getHttpResponse().setStatus(this.getResponseHttpStatus());

        ByteArrayOutputStream outData = new ByteArrayOutputStream();

        // non-multipart request?
        String responseCompression = getResponseCompression();
        if (!isXOP && (mp == null || mp.getCount() == 0) && getAttachmentCount() == 0) {
            String encoding = getEncoding();
            if (responseContent == null) {
                responseContent = "";
            }

            byte[] content = encoding == null ? responseContent.getBytes() : responseContent.getBytes(encoding);

            if (!result.getResponseHeaders().containsKeyIgnoreCase("Content-Type")) {
                result.setContentType(getContentType());
            }

            String acceptEncoding = result.getMockRequest().getRequestHeaders().get("Accept-Encoding", "");
            if (AUTO_RESPONSE_COMPRESSION.equals(responseCompression) && acceptEncoding != null
                    && acceptEncoding.toUpperCase().contains("GZIP")) {
                if (!headerExists("Content-Encoding", "gzip", result)) {
                    result.addHeader("Content-Encoding", "gzip");
                }
                outData.write(CompressionSupport.compress(CompressionSupport.ALG_GZIP, content));
            } else if (AUTO_RESPONSE_COMPRESSION.equals(responseCompression) && acceptEncoding != null
                    && acceptEncoding.toUpperCase().contains("DEFLATE")) {
                result.addHeader("Content-Encoding", "deflate");
                outData.write(CompressionSupport.compress(CompressionSupport.ALG_DEFLATE, content));
            } else {
                outData.write(content);
            }
        } else // won't get here if rest at the moment...
        {
            // make sure..
            if (mp == null) {
                mp = new MimeMultipart();
            }

            // init root part
            initRootPart(responseContent, mp, isXOP);

            // init mimeparts
            AttachmentUtils.addMimeParts(this, Arrays.asList(getAttachments()), mp, contentIds);

            // create request message
            MimeMessage message = new MimeMessage(AttachmentUtils.JAVAMAIL_SESSION);
            message.setContent(mp);
            message.saveChanges();
            MimeMessageMockResponseEntity mimeMessageRequestEntity
                    = new MimeMessageMockResponseEntity(message, isXOP, this);

            result.addHeader("Content-Type", mimeMessageRequestEntity.getContentType().getValue());
            result.addHeader("MIME-Version", "1.0");
            mimeMessageRequestEntity.writeTo(outData);
        }

        if (outData.size() > 0) {
            byte[] data = outData.toByteArray();

            if (responseCompression.equals(CompressionSupport.ALG_DEFLATE)
                    || responseCompression.equals(CompressionSupport.ALG_GZIP)) {
                result.addHeader("Content-Encoding", responseCompression);
                data = CompressionSupport.compress(responseCompression, data);
            }
            if (result.getResponseHeaders().get("Transfer-Encoding") == null) {
                result.addHeader("Content-Length", "" + data.length);
            }
            result.writeRawResponseData(data);
        }


        return responseContent;
    }

    private boolean headerExists(String headerName, String headerValue, MockResult result) {
        StringToStringsMap resultResponseHeaders = result.getResponseHeaders();

        if (resultResponseHeaders.containsKeyIgnoreCase(headerName)) {
            if (resultResponseHeaders.get(headerName).contains(headerValue)) {
                return true;
            }
        }

        return false;
    }

    public boolean prepareMessagePart(MimeMultipart mp, StringToStringMap contentIds, MessageXmlPart requestPart) throws Exception {
        return AttachmentUtils.prepareMessagePart(this, mp, requestPart, contentIds);
    }

    public MessageXmlObject createMessageXmlObject(String responseContent, WsdlOperation wsdlOperation) {
        return new MessageXmlObject(wsdlOperation, responseContent, false);
    }

    private void initRootPart(String requestContent, MimeMultipart mp, boolean isXOP) throws MessagingException {
        MimeBodyPart rootPart = new PreencodedMimeBodyPart("8bit");
        rootPart.setContentID(AttachmentUtils.ROOTPART_SOAPUI_ORG);
        mp.addBodyPart(rootPart, 0);

        DataHandler dataHandler = new DataHandler(new MockResponseDataSource(this, requestContent, isXOP));
        rootPart.setDataHandler(dataHandler);
    }

    protected abstract String removeEmptyContent(String responseContent);

    public void setResponseHeaders(StringToStringsMap headers) {
        StringToStringsMap oldHeaders = getResponseHeaders();

        getConfig().setHeaderArray(new HeaderConfig[0]);

        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            for (String value : header.getValue()) {
                HeaderConfig headerConfig = getConfig().addNewHeader();
                headerConfig.setName(header.getKey());
                headerConfig.setValue(value);
            }
        }

        notifyPropertyChanged(WsdlMockResponse.HEADERS_PROPERTY, oldHeaders, headers);
    }

    protected abstract String executeSpecifics(MockRequest request, String responseContent, WsdlMockRunContext context) throws IOException, WSSecurityException;

    public boolean isEntitizeProperties() {
        return getSettings().getBoolean(CommonSettings.ENTITIZE_PROPERTIES);
    }

    public abstract long getResponseDelay();

    public abstract boolean isForceMtom();

    public abstract boolean isStripWhitespaces();

    public void addTestPropertyListener(TestPropertyListener listener) {
        propertyHolder.addTestPropertyListener(listener);
    }

    public ModelItem getModelItem() {
        return propertyHolder.getModelItem();
    }

    public Map<String, TestProperty> getProperties() {
        return propertyHolder.getProperties();
    }

    public TestProperty getProperty(String name) {
        return propertyHolder.getProperty(name);
    }

    public String[] getPropertyNames() {
        return propertyHolder.getPropertyNames();
    }

    public String getPropertyValue(String name) {
        return propertyHolder.getPropertyValue(name);
    }

    public boolean hasProperty(String name) {
        return propertyHolder.hasProperty(name);
    }

    public void removeTestPropertyListener(TestPropertyListener listener) {
        propertyHolder.removeTestPropertyListener(listener);
    }

    public void setPropertyValue(String name, String value) {
        propertyHolder.setPropertyValue(name, value);
    }


    public TestProperty getPropertyAt(int index) {
        return propertyHolder.getPropertyAt(index);
    }

    public int getPropertyCount() {
        return propertyHolder.getPropertyCount();
    }

    public List<TestProperty> getPropertyList() {
        return propertyHolder.getPropertyList();
    }

    public String getEncoding() {
        return getConfig().getEncoding();
    }

    public void setEncoding(String encoding) {
        getConfig().setEncoding(encoding);
    }
}
