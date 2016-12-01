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

package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.RequestTransport;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry.CannotResolveJmsTypeException;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry.MissingTransportException;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.HermesUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.JMSUtils;
import com.eviware.soapui.impl.wsdl.support.RequestFileAttachment;
import com.eviware.soapui.impl.wsdl.support.jms.header.JMSHeaderConfig;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequest;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import hermes.Hermes;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.NotImplementedException;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HermesJmsRequestTransport implements RequestTransport {

    public static final String IS_JMS_MESSAGE_RECEIVED = "JMS_MESSAGE_RECEIVE";
    public static final String JMS_MESSAGE_SEND = "JMS_MESSAGE_SEND";
    public static final String JMS_RESPONSE = "JMS_RESPONSE";
    public static final String JMS_ERROR = "JMS_ERROR";
    public static final String JMS_RECEIVE_TIMEOUT = "JMS_RECEIVE_TIMEOUT";

    protected String username;
    protected String password;
    protected JMSEndpoint jmsEndpoint;
    protected String durableSubscriptionName;
    protected String clientID;
    protected String messageSelector;
    protected boolean sendAsBytesMessage;
    protected boolean addSoapAction;
    protected Hermes hermes;
    protected static List<RequestFilter> filters = new ArrayList<RequestFilter>();

    public Response sendRequest(SubmitContext submitContext, Request request) throws Exception {
        long timeStarted = Calendar.getInstance().getTimeInMillis();
        submitContext.setProperty(JMS_RECEIVE_TIMEOUT, getTimeout(submitContext, request));

        return resolveType(submitContext, request).execute(submitContext, request, timeStarted);
    }

    protected void init(SubmitContext submitContext, Request request) throws NamingException {
        this.jmsEndpoint = new JMSEndpoint(request, submitContext);
        this.hermes = getHermes(jmsEndpoint.getSessionName(), request);
        this.username = submitContext.expand(request.getUsername());
        this.password = submitContext.expand(request.getPassword());
        JMSHeaderConfig jmsConfig = ((AbstractHttpRequest<?>) request).getJMSHeaderConfig();
        this.durableSubscriptionName = submitContext.expand(jmsConfig.getDurableSubscriptionName());
        this.clientID = submitContext.expand(jmsConfig.getClientID());
        this.messageSelector = jmsConfig.getMessageSelector();// expand latter
        // just before use
        this.sendAsBytesMessage = jmsConfig.getSendAsBytesMessage();
        this.addSoapAction = jmsConfig.getSoapActionAdd();
        submitContext.setProperty(HermesJmsRequestTransport.IS_JMS_MESSAGE_RECEIVED, false);
    }

    protected Response execute(SubmitContext submitContext, Request request, long timeStarted) throws Exception {
        throw new NotImplementedException();
    }

    private HermesJmsRequestTransport resolveType(SubmitContext submitContext, Request request)
            throws CannotResolveJmsTypeException, MissingTransportException {
        String endpoint = PropertyExpander.expandProperties(submitContext, request.getEndpoint());
        int ix = endpoint.indexOf("://");
        if (ix == -1) {
            throw new MissingTransportException("Missing protocol in endpoint [" + endpoint + "]");
        }

        String[] params = JMSEndpoint.extractEndpointParameters(request, submitContext);

        // resolve sending class
        if (params.length == 2) {
            String destinationName = PropertyExpander.expandProperties(submitContext, params[1]);
            if (destinationName.startsWith(JMSEndpoint.QUEUE_ENDPOINT_PREFIX)) {
                return new HermesJmsRequestSendTransport();
            } else if (destinationName.startsWith(JMSEndpoint.TOPIC_ENDPOINT_PREFIX)) {
                return new HermesJmsRequestPublishTransport();
            } else {
                cannotResolve();
            }

        }
        // resolve receiving class
        else if (params.length == 3 && PropertyExpander.expandProperties(submitContext, params[1]).equals("-")) {
            String destinationName = PropertyExpander.expandProperties(submitContext, params[2]);
            if (destinationName.startsWith(JMSEndpoint.QUEUE_ENDPOINT_PREFIX)) {
                return new HermesJmsRequestReceiveTransport();
            } else if (destinationName.startsWith(JMSEndpoint.TOPIC_ENDPOINT_PREFIX)) {
                return new HermesJmsRequestSubscribeTransport();
            } else {
                cannotResolve();
            }
        }
        // resolve send-receive class
        else if (params.length == 3) {
            String destinationSendName = PropertyExpander.expandProperties(submitContext, params[1]);
            String destinationReceiveName = PropertyExpander.expandProperties(submitContext, params[2]);
            if (destinationSendName.startsWith(JMSEndpoint.QUEUE_ENDPOINT_PREFIX)
                    && destinationReceiveName.startsWith(JMSEndpoint.QUEUE_ENDPOINT_PREFIX)) {
                return new HermesJmsRequestSendReceiveTransport();
            } else if (destinationSendName.startsWith(JMSEndpoint.QUEUE_ENDPOINT_PREFIX)
                    && destinationReceiveName.startsWith(JMSEndpoint.TOPIC_ENDPOINT_PREFIX)) {
                return new HermesJmsRequestSendSubscribeTransport();
            } else if (destinationSendName.startsWith(JMSEndpoint.TOPIC_ENDPOINT_PREFIX)
                    && destinationReceiveName.startsWith(JMSEndpoint.TOPIC_ENDPOINT_PREFIX)) {
                return new HermesJmsRequestPublishSubscribeTransport();
            } else if (destinationSendName.startsWith(JMSEndpoint.TOPIC_ENDPOINT_PREFIX)
                    && destinationReceiveName.startsWith(JMSEndpoint.QUEUE_ENDPOINT_PREFIX)) {
                return new HermesJmsRequestPublishReceiveTransport();
            } else {
                cannotResolve();
            }
        } else {
            cannotResolve();
        }
        return null;
    }

    private static void cannotResolve() throws CannotResolveJmsTypeException {
        throw new CannotResolveJmsTypeException(
                "\nBad jms alias! \nFor JMS please use this endpont pattern:\nfor sending 'jms://sessionName::queue_myqueuename' \nfor receive  'jms://sessionName::-::queue_myqueuename'\nfor send-receive 'jms://sessionName::queue_myqueuename1::queue_myqueuename2'");
    }

    protected Hermes getHermes(String sessionName, Request request) throws NamingException {
        WsdlProject project = (WsdlProject) ModelSupport.getModelItemProject(request);
        return HermesUtils.getHermes(project, sessionName);
    }

    protected long getTimeout(SubmitContext submitContext, Request request) {
        String timeout = PropertyExpander.expandProperties(submitContext, request.getTimeout());
        long to = 0;
        try {
            to = Long.parseLong(timeout);
        } catch (Exception e) {
        }

        return to;
    }

    protected JMSHeader createJMSHeader(SubmitContext submitContext, Request request, Hermes hermes, Message message,
                                        Destination replyToDestination) {
        JMSHeader jmsHeader = new JMSHeader();
        jmsHeader.setMessageHeaders(message, request, hermes, submitContext);
        JMSHeader.setMessageProperties(message, request, hermes, submitContext);
        try {
            if (message.getJMSReplyTo() == null) {
                message.setJMSReplyTo(replyToDestination);
            }

            if (addSoapAction) {

                message.setStringProperty(JMSHeader.SOAPJMS_SOAP_ACTION, request.getOperation().getName());
                if (request.getOperation() instanceof WsdlOperation) {
                    message.setStringProperty(JMSHeader.SOAP_ACTION,
                            ((WsdlOperation) request.getOperation()).getAction());
                } else {
                    message.setStringProperty(JMSHeader.SOAP_ACTION, request.getOperation().getName());
                }
            }
        } catch (JMSException e) {
            SoapUI.logError(e);
        }
        return jmsHeader;
    }

    protected void closeSessionAndConnection(Connection connection, Session session) throws JMSException {
        if (session != null) {
            session.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    protected Response errorResponse(SubmitContext submitContext, Request request, long timeStarted, JMSException jmse) {
        JMSResponse response;
        SoapUI.logError(jmse);
        submitContext.setProperty(JMS_ERROR, jmse);
        response = new JMSResponse("", null, null, request, timeStarted);
        submitContext.setProperty(JMS_RESPONSE, response);
        return response;
    }

    protected Message messageSend(SubmitContext submitContext, Request request, Session session, Hermes hermes,
                                  Queue queueSend, Destination replyToDestination) throws JMSException {
        MessageProducer messageProducer = session.createProducer(queueSend);
        Message messageSend = createMessage(submitContext, request, session);
        return send(submitContext, request, hermes, messageProducer, messageSend, replyToDestination);
    }

    protected Message messagePublish(SubmitContext submitContext, Request request, Session topicSession, Hermes hermes,
                                     Topic topicPublish, Destination replyToDestination) throws JMSException {
        MessageProducer topicPublisher = topicSession.createProducer(topicPublish);
        Message messagePublish = createMessage(submitContext, request, topicSession);
        return send(submitContext, request, hermes, topicPublisher, messagePublish, replyToDestination);
    }

    private Message send(SubmitContext submitContext, Request request, Hermes hermes, MessageProducer messageProducer,
                         Message message, Destination replyToDestination) throws JMSException {
        JMSHeader jmsHeader = createJMSHeader(submitContext, request, hermes, message, replyToDestination);
        messageProducer.send(message, message.getJMSDeliveryMode(), message.getJMSPriority(), jmsHeader.getTimeTolive());
        submitContext.setProperty(JMS_MESSAGE_SEND, message);
        return message;
    }

    protected Response makeResponse(SubmitContext submitContext, Request request, long timeStarted,
                                    Message messageSend, MessageConsumer messageConsumer) throws JMSException {
        long timeout = getTimeout(submitContext, request);
        Message messageReceive = messageConsumer.receive(timeout);
        if (messageReceive != null) {
            JMSResponse response = resolveMessage(request, timeStarted, messageSend, messageReceive);
            submitContext.setProperty(IS_JMS_MESSAGE_RECEIVED, true);
            submitContext.setProperty(JMS_RESPONSE, response);
            return response;
        } else {
            return new JMSResponse("", null, null, request, timeStarted);
        }
    }

    private JMSResponse resolveMessage(Request request, long timeStarted, Message messageSend, Message messageReceive)
            throws JMSException {
        if (messageReceive instanceof TextMessage) {
            TextMessage textMessageReceive = (TextMessage) messageReceive;
            return new JMSResponse(textMessageReceive.getText(), messageSend, textMessageReceive, request, timeStarted);
        } else if (messageReceive instanceof MapMessage) {
            MapMessage mapMessageReceive = (MapMessage) messageReceive;
            return new JMSResponse(JMSUtils.extractMapMessagePayloadToXML(mapMessageReceive), messageSend,
                    mapMessageReceive, request, timeStarted);
        } else if (messageReceive instanceof BytesMessage) {

            BytesMessage bytesMessageReceive = (BytesMessage) messageReceive;

            String bytesMessageAsString = new String(JMSUtils.extractByteArrayFromMessage(bytesMessageReceive));
            // if message seems to be XML make xml response
            if (XmlUtils.seemsToBeXml(bytesMessageAsString)) {
                return new JMSResponse(bytesMessageAsString, messageSend, bytesMessageReceive, request, timeStarted);
            } else {
                JMSResponse jmsResponse = new JMSResponse("", messageSend, bytesMessageReceive, request, timeStarted);
                addAttachment(request, bytesMessageReceive, jmsResponse);
                return jmsResponse;
            }
        }
        return null;
    }

    protected Response makeEmptyResponse(SubmitContext submitContext, Request request, long timeStarted,
                                         Message messageSend) {
        JMSResponse response = new JMSResponse("", messageSend, null, request, timeStarted);
        submitContext.setProperty(JMS_RESPONSE, response);
        return response;
    }

    private Message createMessage(SubmitContext submitContext, Request request, Session session) throws JMSException {
        if (request instanceof WsdlRequest || request instanceof HttpTestRequest || request instanceof RestRequest) {
            if (hasAttachment(request)) {
                if (isTextAttachment(request) && !sendAsBytesMessage) {
                    return createTextMessageFromAttachment(submitContext, request, session);
                } else {
                    return createBytesMessage(request, session);
                }
            } else {
                String requestContent = applyFilters(submitContext, request);
                if (sendAsBytesMessage) {
                    return createBytesMessageFromText(submitContext, requestContent, session);
                } else {
                    return createTextMessage(submitContext, requestContent, session);
                }
            }
        }

        return null;
    }

    private String applyFilters(SubmitContext submitContext, Request request) {
        submitContext.setProperty(BaseHttpRequestTransport.REQUEST_CONTENT, request.getRequestContent());
        submitContext.setProperty(WSDL_REQUEST, request);

        for (RequestFilter filter : filters) {
            filter.filterRequest(submitContext, request);
        }

        String requestContent = (String) submitContext.getProperty(BaseHttpRequestTransport.REQUEST_CONTENT);
        return requestContent;
    }

    private Message createBytesMessageFromText(SubmitContext submitContext, String requestContent, Session session)
            throws JMSException {
        BytesMessage bytesMessage = session.createBytesMessage();
        bytesMessage.writeBytes(requestContent.getBytes());
        return bytesMessage;
    }

    private Message createTextMessageFromAttachment(SubmitContext submitContext, Request request, Session session) {
        try {
            String content = convertStreamToString(request.getAttachments()[0].getInputStream());
            TextMessage textMessageSend = session.createTextMessage();
            String messageBody = PropertyExpander.expandProperties(submitContext, content);
            textMessageSend.setText(messageBody);
            return textMessageSend;
        } catch (Exception e) {
            SoapUI.logError(e);
        }
        return null;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private boolean hasAttachment(Request request) {
        if (request.getAttachments().length > 0) {
            return true;
        }
        return false;
    }

    private Message createTextMessage(SubmitContext submitContext, String requestContent, Session session)
            throws JMSException {
        TextMessage textMessageSend = session.createTextMessage();
        textMessageSend.setText(requestContent);
        return textMessageSend;
    }

    private boolean isTextAttachment(Request request) {
        if (request.getAttachments().length > 0
                && (request.getAttachments()[0].getContentType().contains("/text")
                || request.getAttachments()[0].getContentType().contains("/xml") || request.getAttachments()[0]
                .getContentType().contains("text/plain"))) {
            return true;
        }
        return false;
    }

    private Message createBytesMessage(Request request, Session session) {
        try {
            InputStream in = request.getAttachments()[0].getInputStream();
            int buff = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((buff = in.read()) != -1) {
                baos.write(buff);
            }
            BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(baos.toByteArray());
            return bytesMessage;
        } catch (Exception e) {
            SoapUI.logError(e);
        }
        return null;
    }

    private void addAttachment(Request request, BytesMessage bytesMessageReceive, JMSResponse jmsResponse)
            throws JMSException {
        try {
            byte[] buff = new byte[1];
            File temp = File.createTempFile("bytesmessage", ".tmp");
            OutputStream out = new FileOutputStream(temp);
            bytesMessageReceive.reset();
            while (bytesMessageReceive.readBytes(buff) != -1) {
                out.write(buff);
            }
            out.close();
            Attachment[] attachments = new Attachment[]{new RequestFileAttachment(temp, false,
                    (AbstractHttpRequest<?>) request)};
            jmsResponse.setAttachments(attachments);
        } catch (IOException e) {
            SoapUI.logError(e);
        }
    }

    protected TopicSubscriber createDurableSubscription(SubmitContext submitContext, Session topicSession,
                                                        JMSConnectionHolder jmsConnectionHolder) throws JMSException, NamingException {

        Topic topicSubscribe = jmsConnectionHolder.getTopic(jmsConnectionHolder.getJmsEndpoint().getReceive());

        // create durable subscriber
        TopicSubscriber topicDurableSubsriber = topicSession.createDurableSubscriber(topicSubscribe,
                StringUtils.hasContent(durableSubscriptionName) ? durableSubscriptionName : "durableSubscription"
                        + jmsConnectionHolder.getJmsEndpoint().getReceive(), submitContext.expand(messageSelector), false);
        return topicDurableSubsriber;
    }

    @SuppressWarnings("serial")
    public static class UnresolvedJMSEndpointException extends Exception {
        public UnresolvedJMSEndpointException(String msg) {
            super(msg);
        }
    }

    public void abortRequest(SubmitContext submitContext) {
    }

    public void addRequestFilter(RequestFilter filter) {
        filters.add(filter);
    }

    public void removeRequestFilter(RequestFilter filter) {
        filters.remove(filter);
    }

    @Override
    public void insertRequestFilter(RequestFilter filter, RequestFilter refFilter) {
        int ix = filters.indexOf( refFilter );
        if( ix == -1 )
            filters.add( filter );
        else
            filters.add( ix, filter );
    }
}
