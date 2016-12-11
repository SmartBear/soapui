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

package com.eviware.soapui.impl.wsdl.panels.teststeps.amf;

import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import flex.messaging.io.ClassAliasRegistry;
import flex.messaging.io.MessageDeserializer;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.io.amf.MessageHeader;
import flex.messaging.io.amf.client.AMFHeaderProcessor;
import flex.messaging.io.amf.client.exceptions.ClientStatusException;
import flex.messaging.io.amf.client.exceptions.ServerStatusException;
import flex.messaging.io.amf.client.exceptions.ServerStatusException.HttpResponseInfo;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * AMFConnection derivate using HttpClient instead of UrlConnection
 *
 * @author Ole
 */

public class SoapUIAMFConnection {
    private static int DEFAULT_OBJECT_ENCODING = MessageIOConstants.AMF3;

    /**
     * Creates a default AMF connection instance.
     */
    public SoapUIAMFConnection() {
    }

    private ActionContext actionContext;
    private boolean connected;
    private int objectEncoding;
    private boolean objectEncodingSet = false;
    private SerializationContext serializationContext;
    private String url;

    private List<MessageHeader> amfHeaders;
    private AMFHeaderProcessor amfHeaderProcessor;
    private Map<String, String> httpRequestHeaders;
    private int responseCounter;

    private ExtendedPostMethod postMethod;
    private HttpContext httpState = new BasicHttpContext();
    private PropertyExpansionContext context;

    public int getObjectEncoding() {
        if (!objectEncodingSet) {
            return DEFAULT_OBJECT_ENCODING;
        }

        return objectEncoding;
    }

    public void setObjectEncoding(int objectEncoding) {
        this.objectEncoding = objectEncoding;
        objectEncodingSet = true;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Adds an AMF packet-level header which is sent with every request for the
     * life of this AMF connection.
     *
     * @param name           The name of the header.
     * @param mustUnderstand Whether the header must be processed or not.
     * @param data           The value of the header.
     */
    public void addAmfHeader(String name, boolean mustUnderstand, Object data) {
        if (amfHeaders == null) {
            amfHeaders = new ArrayList<MessageHeader>();
        }

        MessageHeader header = new MessageHeader(name, mustUnderstand, data);
        amfHeaders.add(header);
    }

    /**
     * Add an AMF packet-level header with mustUnderstand=false, which is sent
     * with every request for the life of this AMF connection.
     *
     * @param name The name of the header.
     * @param data The value of the header.
     */
    public void addAmfHeader(String name, Object data) {
        addAmfHeader(name, false, data);
    }

    /**
     * Removes any AMF headers found with the name given.
     *
     * @param name The name of the header(s) to remove.
     * @return true if a header existed with the given name.
     */
    public boolean removeAmfHeader(String name) {
        boolean exists = false;
        if (amfHeaders != null) {
            for (Iterator<MessageHeader> iterator = amfHeaders.iterator(); iterator.hasNext(); ) {
                MessageHeader header = iterator.next();
                if (name.equals(header.getName())) {
                    iterator.remove();
                    exists = true;
                }
            }
        }
        return exists;
    }

    /**
     * Removes all AMF headers.
     */
    public void removeAllAmfHeaders() {
        if (amfHeaders != null) {
            amfHeaders = null;
        }
    }

    /**
     * Adds a Http request header to the underlying connection.
     *
     * @param name  The name of the Http header.
     * @param value The value of the Http header.
     */
    public void addHttpRequestHeader(String name, String value) {
        if (httpRequestHeaders == null) {
            httpRequestHeaders = new HashMap<String, String>();
        }

        httpRequestHeaders.put(name, value);
    }

    /**
     * Removes the Http header found with the name given.
     *
     * @param name The name of the Http header.
     * @return true if a header existed with the given name.
     */
    public boolean removeHttpRequestHeader(String name) {
        boolean exists = false;
        if (httpRequestHeaders != null) {
            Object previousValue = httpRequestHeaders.remove(name);
            exists = (previousValue != null);
        }
        return exists;
    }

    /**
     * Removes all Http request headers.
     */
    public void removeAllHttpRequestHeaders() {
        if (httpRequestHeaders != null) {
            httpRequestHeaders = null;
        }
    }

    /**
     * Makes an AMF request to the server. A connection must have been made prior
     * to making a call.
     *
     * @param command   The method to call on the server.
     * @param arguments Arguments for the method.
     * @return The result of the call.
     * @throws ClientStatusException If there is a client side exception.
     * @throws ServerStatusException If there is a server side exception.
     */

    public Object call(PropertyExpansionContext context, String command, Object... arguments)
            throws ClientStatusException, ServerStatusException {
        this.context = context;

        if (!connected) {
            String message = "AMF connection is not connected";
            ClientStatusException cse = new ClientStatusException(message, ClientStatusException.AMF_CALL_FAILED_CODE);
            throw cse;
        }

        String responseURI = getResponseURI();

        ActionMessage requestMessage = new ActionMessage(getObjectEncoding());

        if (amfHeaders != null) {
            for (MessageHeader header : amfHeaders) {
                requestMessage.addHeader(header);
            }
        }

        MessageBody amfMessage = new MessageBody(command, responseURI, arguments);
        requestMessage.addBody(amfMessage);

        // Setup for AMF message serializer
        actionContext.setRequestMessage(requestMessage);
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        AmfMessageSerializer amfMessageSerializer = new AmfMessageSerializer();
        amfMessageSerializer.initialize(serializationContext, outBuffer, null/* debugTrace */);

        try {
            amfMessageSerializer.writeMessage(requestMessage);
            Object result = send(outBuffer);
            return result;
        } catch (Exception e) {
            if (e instanceof ClientStatusException) {
                throw (ClientStatusException) e;
            } else if (e instanceof ServerStatusException) {
                throw (ServerStatusException) e;
            }
            // Otherwise, wrap into a ClientStatusException.
            ClientStatusException exception = new ClientStatusException(e, ClientStatusException.AMF_CALL_FAILED_CODE);
            throw exception;
        } finally {
            try {
                outBuffer.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * Closes the underlying URL connection, sets the url to null, and clears the
     * cookies.
     */
    public void close() {
        // Clear the URL connection and URL.
        postMethod = null;
        url = null;

        serializationContext = null;
        connected = false;
    }

    /**
     * Connects to the URL provided. Any previous connections are closed.
     *
     * @param url The url to connect to.
     * @throws ClientStatusException If there is a client side exception.
     */
    public void connect(String url) throws ClientStatusException {
        if (connected) {
            close();
        }

        this.url = url;
        try {
            serializationContext = new SerializationContext();
            serializationContext.createASObjectForMissingType = true;
            internalConnect();
        } catch (IOException e) {
            ClientStatusException exception = new ClientStatusException(e, ClientStatusException.AMF_CONNECT_FAILED_CODE);
            throw exception;
        }
    }

    // --------------------------------------------------------------------------
    //
    // Protected Methods
    //
    // --------------------------------------------------------------------------

    /**
     * Generates the HTTP response info for the server status exception.
     *
     * @return The HTTP response info for the server status exception.
     */
    protected HttpResponseInfo generateHttpResponseInfo() {
        HttpResponseInfo httpResponseInfo = null;
        try {
            int responseCode = 0;
            if (postMethod.hasHttpResponse()) {
                responseCode = postMethod.getHttpResponse().getStatusLine().getStatusCode();
            }
            String responseMessage = postMethod.getResponseBodyAsString();
            httpResponseInfo = new HttpResponseInfo(responseCode, responseMessage);
        } catch (IOException ignore) {
        }
        return httpResponseInfo;
    }

    /**
     * Generates and returns the response URI.
     *
     * @return The response URI.
     */
    protected String getResponseURI() {
        String responseURI = "/" + responseCounter;
        responseCounter++;
        return responseURI;
    }

    /**
     * An internal method that sets up the underlying URL connection.
     *
     * @throws IOException If an exception is encountered during URL connection setup.
     */
    protected void internalConnect() throws IOException {
        serializationContext.instantiateTypes = false;
        postMethod = new ExtendedPostMethod(url);
        setHttpRequestHeaders();
        actionContext = new ActionContext();
        connected = true;
    }

    /**
     * Processes the HTTP response headers and body.
     */
    protected Object processHttpResponse(InputStream inputStream) throws ClassNotFoundException, IOException,
            ClientStatusException, ServerStatusException {
        return processHttpResponseBody(inputStream);
    }

    /**
     * Processes the HTTP response body.
     */
    protected Object processHttpResponseBody(InputStream inputStream) throws ClassNotFoundException, IOException,
            ClientStatusException, ServerStatusException {
        DataInputStream din = new DataInputStream(inputStream);
        ActionMessage message = new ActionMessage();
        actionContext.setRequestMessage(message);
        MessageDeserializer deserializer = new AmfMessageDeserializer();
        deserializer.initialize(serializationContext, din, null/* trace */);
        deserializer.readMessage(message, actionContext);
        din.close();
        context.setProperty(AMFResponse.AMF_RESPONSE_ACTION_MESSAGE, message);
        return processAmfPacket(message);
    }

    /**
     * Processes the AMF packet.
     */
    @SuppressWarnings("unchecked")
    protected Object processAmfPacket(ActionMessage packet) throws ClientStatusException, ServerStatusException {
        processAmfHeaders(packet.getHeaders());
        return processAmfBody(packet.getBodies());
    }

    /**
     * Processes the AMF headers by dispatching them to an AMF header processor,
     * if one exists.
     */
    protected void processAmfHeaders(ArrayList<MessageHeader> headers) throws ClientStatusException {
        // No need to process headers if there's no AMF header processor.
        if (amfHeaderProcessor == null) {
            return;
        }

        for (MessageHeader header : headers) {
            amfHeaderProcessor.processHeader(header);
        }
    }

    /**
     * Processes the AMF body. Note that this method won't work if batching of
     * AMF messages is supported at some point but for now we are guaranteed to
     * have a single message.
     */
    protected Object processAmfBody(ArrayList<MessageBody> messages) throws ServerStatusException {
        for (MessageBody message : messages) {
            String targetURI = message.getTargetURI();

            if (targetURI.endsWith(MessageIOConstants.RESULT_METHOD)) {
                return message.getData();
            } else if (targetURI.endsWith(MessageIOConstants.STATUS_METHOD)) {
                // String exMessage = "Server error";
                // HttpResponseInfo responseInfo = generateHttpResponseInfo();
                // ServerStatusException exception = new ServerStatusException(
                // exMessage, message.getData(), responseInfo );

                return message.getData();
                // throw exception;
            }
        }
        return null; // Should not happen.
    }

    /**
     * Writes the output buffer and processes the HTTP response.
     */
    protected Object send(ByteArrayOutputStream outBuffer) throws ClassNotFoundException, IOException,
            ClientStatusException, ServerStatusException {
        // internalConnect.
        internalConnect();

        postMethod.setEntity(new ByteArrayEntity(outBuffer.toByteArray()));

        HttpClientSupport.execute(postMethod, httpState);
        context.setProperty(AMFResponse.AMF_POST_METHOD, postMethod);

        return processHttpResponse(responseBodyInputStream());
    }

    private ByteArrayInputStream responseBodyInputStream() throws IOException {
        byte[] responseBody = postMethod.getResponseBody();
        ByteArrayInputStream bais = new ByteArrayInputStream(responseBody);
        context.setProperty(AMFResponse.AMF_RAW_RESPONSE_BODY, responseBody);
        return bais;
    }

    /**
     * Sets the Http request headers, including the cookie headers.
     */
    protected void setHttpRequestHeaders() {
        if (httpRequestHeaders != null) {
            for (Map.Entry<String, String> element : httpRequestHeaders.entrySet()) {
                String key = element.getKey();
                String value = element.getValue();
                postMethod.setHeader(key, value);
            }
        }
    }

    /**
     * Registers a custom alias for a class name bidirectionally.
     *
     * @param alias     The alias for the class name.
     * @param className The concrete class name.
     */
    public static void registerAlias(String alias, String className) {
        ClassAliasRegistry registry = ClassAliasRegistry.getRegistry();
        registry.registerAlias(alias, className);
        registry.registerAlias(className, alias);
    }
}
