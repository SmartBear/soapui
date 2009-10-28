package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import hermes.Domain;
import hermes.Hermes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.NotImplementedException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.RequestTransport;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry.CannotResolveJmsTypeException;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry.MissingTransportException;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.HermesUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.JMSUtils;
import com.eviware.soapui.impl.wsdl.support.RequestFileAttachment;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequest;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;

public class HermesJmsRequestTransport implements RequestTransport
{

	public static final String JMS_MESSAGE_RECEIVE = "JMS_MESSAGE_RECEIVE";
	public static final String JMS_MESSAGE_SEND = "JMS_MESSAGE_SEND";
	public static final String JMS_RESPONSE = "JMS_RESPONSE";
	public static final String JMS_ERROR = "JMS_ERROR";
	public static final String HERMES_SESSION_NAME = "HERMES_SESSION_NAME";
	public static final String JMS_RECEIVE_TIMEOUT = "JMS_RECEIVE_TIMEOUT";
	public static final String QUEUE_ENDPOINT_PREFIX = "queue_";
	public static final String TOPIC_ENDPOINT_PREFIX = "topic_";

	protected List<RequestFilter> filters = new ArrayList<RequestFilter>();

	public void abortRequest(SubmitContext submitContext)
	{
	}

	public void addRequestFilter(RequestFilter filter)
	{
		filters.add(filter);
	}

	public void removeRequestFilter(RequestFilter filter)
	{
		filters.remove(filter);
	}

	public Response sendRequest(SubmitContext submitContext, Request request) throws Exception
	{
		long timeStarted = Calendar.getInstance().getTimeInMillis();
		submitContext.setProperty(JMS_RECEIVE_TIMEOUT, getTimeout(submitContext, request));
		return resolveType(submitContext, request).execute(submitContext, request, timeStarted);
	}

	protected Response execute(SubmitContext submitContext, Request request, long timeStarted) throws Exception
	{
		throw new NotImplementedException();
	}

	private HermesJmsRequestTransport resolveType(SubmitContext submitContext, Request request)
			throws CannotResolveJmsTypeException, MissingTransportException
	{
		int ix = request.getEndpoint().indexOf("://");
		if (ix == -1)
			throw new MissingTransportException("Missing protocol in endpoint [" + request.getEndpoint() + "]");

		String[] params = extractEndpointParameters(request);

		// resolve sending class
		if (params.length == 2)
		{
			String destinationName = PropertyExpander.expandProperties(submitContext, params[1]);
			if (destinationName.startsWith(QUEUE_ENDPOINT_PREFIX))
			{
				return new HermesJmsRequestSendTransport();
			}
			else if (destinationName.startsWith(TOPIC_ENDPOINT_PREFIX))
			{
				return new HermesJmsRequestPublishTransport();
			}
			else
			{
				cannotResolve();
			}

		}
		// resolve receiving class
		else if (params.length == 3 && PropertyExpander.expandProperties(submitContext, params[1]).equals("-"))
		{
			String destinationName = PropertyExpander.expandProperties(submitContext, params[2]);
			if (destinationName.startsWith(QUEUE_ENDPOINT_PREFIX))
			{
				return new HermesJmsRequestReceiveTransport();
			}
			else if (destinationName.startsWith(TOPIC_ENDPOINT_PREFIX))
			{
				return new HermesJmsRequestSubscribeTransport();
			}
			else
			{
				cannotResolve();
			}
		}
		// resolve send-receive class
		else if (params.length == 3)
		{
			String destinationSendName = PropertyExpander.expandProperties(submitContext, params[1]);
			String destinationReceiveName = PropertyExpander.expandProperties(submitContext, params[2]);
			if (destinationSendName.startsWith(QUEUE_ENDPOINT_PREFIX)
					&& destinationReceiveName.startsWith(QUEUE_ENDPOINT_PREFIX))
			{
				return new HermesJmsRequestSendReceiveTransport();
			}
			else if (destinationSendName.startsWith(QUEUE_ENDPOINT_PREFIX)
					&& destinationReceiveName.startsWith(TOPIC_ENDPOINT_PREFIX))
			{
				return new HermesJmsRequestSendSubscribeTransport();
			}
			else if (destinationSendName.startsWith(TOPIC_ENDPOINT_PREFIX)
					&& destinationReceiveName.startsWith(TOPIC_ENDPOINT_PREFIX))
			{
				return new HermesJmsRequestPublishSubscribeTransport();
			}
			else if (destinationSendName.startsWith(TOPIC_ENDPOINT_PREFIX)
					&& destinationReceiveName.startsWith(QUEUE_ENDPOINT_PREFIX))
			{
				return new HermesJmsRequestPublishReceiveTransport();
			}
			else
			{
				cannotResolve();
			}
		}
		else
		{
			cannotResolve();
		}
		return null;
	}

	private static void cannotResolve() throws CannotResolveJmsTypeException
	{
		throw new CannotResolveJmsTypeException(
				"\nBad jms alias! \nFor JMS please use this endpont pattern:\nfor sending 'jms://sessionName/queue_myqueuename' \nfor receive  'jms://sessionName/-/queue_myqueuename'\nfor send-receive 'jms://sessionName/queue_myqueuename1/queue_myqueuename2'");
	}

	protected Hermes getHermes(String sessionName, Request request) throws NamingException
	{
		WsdlProject project = (WsdlProject) ModelSupport.getModelItemProject(request);
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Context ctx = HermesUtils.hermesContext(project);

			Hermes hermes = (Hermes) ctx.lookup(sessionName);
			return hermes;
		}
		catch (NamingException ne)
		{
			UISupport
					.showErrorMessage("Hermes configuration is not valid. Please check that 'Hermes Config' project property is set to path of proper hermes-config.xml file");
			throw new NamingException("Session name '" + sessionName
					+ "' does not exist in Hermes configuration or path to Hermes config ( " + project.getHermesConfig()
					+ " )is not valid !!!!");
		}
		catch (MalformedURLException mue)
		{
			SoapUI.logError(mue);
		}
		catch (IOException ioe)
		{
			SoapUI.logError(ioe);
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
		return null;
	}

	protected long getTimeout(SubmitContext submitContext, Request request)
	{
		String timeout = PropertyExpander.expandProperties(submitContext, request.getTimeout());
		long to = 0;
		try
		{
			to = Long.parseLong(timeout);
		}
		catch (Exception e)
		{
		}

		return to;
	}

	protected JMSHeader createJMSHeader(SubmitContext submitContext, Request request, Hermes hermes, Message message)
	{
		JMSHeader jmsHeader = new JMSHeader();
		jmsHeader.setMessageHeaders(message, request, hermes, submitContext);
		JMSHeader.setMessageProperties(message, request, hermes, submitContext);
		return jmsHeader;
	}

	protected void closeSessionAndConnection(Connection connection, Session session) throws JMSException
	{
		if (session != null)
			session.close();
		if (connection != null)
			connection.close();
	}

	protected Response errorResponse(SubmitContext submitContext, Request request, long timeStarted, JMSException jmse)
	{
		JMSResponse response;
		SoapUI.logError(jmse);
		submitContext.setProperty(JMS_ERROR, jmse);
		response = new JMSResponse("", null, null, request, timeStarted);
		submitContext.setProperty(JMS_RESPONSE, response);
		return response;
	}

	protected Message messageSend(SubmitContext submitContext, Request request, Session session, Hermes hermes,
			Queue queueSend) throws JMSException
	{
		MessageProducer messageProducer = session.createProducer(queueSend);
		Message messageSend = createMessage(submitContext, request, session);
		return send(submitContext, request, hermes, messageProducer, messageSend);
	}

	protected Message messagePublish(SubmitContext submitContext, Request request, TopicSession topicSession,
			Hermes hermes, Topic topicPublish) throws JMSException
	{
		TopicPublisher topicPublisher = topicSession.createPublisher(topicPublish);
		Message messagePublish = createMessage(submitContext, request, topicSession);
		return send(submitContext, request, hermes, topicPublisher, messagePublish);
	}

	private Message send(SubmitContext submitContext, Request request, Hermes hermes, MessageProducer messageProducer,
			Message message) throws JMSException
	{
		JMSHeader jmsHeader = createJMSHeader(submitContext, request, hermes, message);
		messageProducer.send(message, message.getJMSDeliveryMode(), message.getJMSPriority(), jmsHeader.getTimeTolive());
		submitContext.setProperty(JMS_MESSAGE_SEND, message);
		return message;
	}

	protected Response makeResponse(SubmitContext submitContext, Request request, long timeStarted, Message messageSend,
			MessageConsumer messageConsumer) throws JMSException
	{
		long timeout = getTimeout(submitContext, request);
		Message messageReceive = messageConsumer.receive(timeout);
		if (messageReceive != null)
		{
			JMSResponse response = resolveMessage(request, timeStarted, messageSend, messageReceive);
			submitContext.setProperty(JMS_MESSAGE_RECEIVE, messageReceive);
			submitContext.setProperty(JMS_RESPONSE, response);
			return response;
		}
		else
		{
			return new JMSResponse("", null, null, request, timeStarted);
		}
	}

	private JMSResponse resolveMessage(Request request, long timeStarted, Message messageSend, Message messageReceive)
			throws JMSException
	{
		if (messageReceive instanceof TextMessage)
		{
			TextMessage textMessageReceive = (TextMessage) messageReceive;
			return new JMSResponse(textMessageReceive.getText(), messageSend, textMessageReceive, request, timeStarted);
		}
		else if (messageReceive instanceof MapMessage)
		{
			MapMessage mapMessageReceive = (MapMessage) messageReceive;
			return new JMSResponse(JMSUtils.extractMapMessagePayloadToString(mapMessageReceive), messageSend,
					mapMessageReceive, request, timeStarted);
		}
		else if (messageReceive instanceof BytesMessage)
		{
			BytesMessage bytesMessageReceive = (BytesMessage) messageReceive;
			JMSResponse jmsResponse = new JMSResponse("", messageSend, bytesMessageReceive, request, timeStarted);
			addAttachment(request, bytesMessageReceive, jmsResponse);
			return jmsResponse;
		}
		return null;
	}

	protected Response makeEmptyResponse(SubmitContext submitContext, Request request, long timeStarted,
			Message messageSend)
	{
		JMSResponse response = new JMSResponse("", messageSend, null, request, timeStarted);
		submitContext.setProperty(JMS_RESPONSE, response);
		return response;
	}

	private Message createMessage(SubmitContext submitContext, Request request, Session session) throws JMSException
	{
		if (request instanceof WsdlRequest || request instanceof HttpTestRequest || request instanceof RestRequest)
		{
			if (hasAttachment(request))
			{
				if (isTextAttachment(request))
				{
					return createTextMessageFromAttachment(submitContext, request, session);
				}
				else
				{
					return createBytesMessage(request, session);
				}
			}
			else
			{
				return createTextMessage(submitContext, request, session);
			}
		}

		return null;
	}

	private Message createTextMessageFromAttachment(SubmitContext submitContext, Request request, Session session)
	{
		try
		{
			String content = convertStreamToString(request.getAttachments()[0].getInputStream());
			TextMessage textMessageSend = session.createTextMessage();
			String messageBody = PropertyExpander.expandProperties(submitContext, content);
			textMessageSend.setText(messageBody);
			return textMessageSend;
		}
		catch (Exception e)
		{
			SoapUI.logError(e);
		}
		return null;
	}

	private String convertStreamToString(InputStream is)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try
		{
			while ((line = reader.readLine()) != null)
			{
				sb.append(line + "\n");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private boolean hasAttachment(Request request)
	{
		if (request.getAttachments().length > 0)
			return true;
		return false;
	}

	private Message createTextMessage(SubmitContext submitContext, Request request, Session session) throws JMSException
	{
		TextMessage textMessageSend = session.createTextMessage();
		String messageBody = PropertyExpander.expandProperties(submitContext, request.getRequestContent());
		textMessageSend.setText(messageBody);
		return textMessageSend;
	}

	private boolean isTextAttachment(Request request)
	{
		if (request.getAttachments().length > 0 && (request.getAttachments()[0].getContentType().contains("/text"))
				|| request.getAttachments()[0].getContentType().contains("/xml"))
		{
			return true;
		}
		return false;
	}

	private Message createBytesMessage(Request request, Session session)
	{
		try
		{
			InputStream in = request.getAttachments()[0].getInputStream();
			int buff = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((buff = in.read()) != -1)
			{
				baos.write(buff);
			}
			BytesMessage bytesMessage = session.createBytesMessage();
			bytesMessage.writeBytes(baos.toByteArray());
			return bytesMessage;
		}
		catch (Exception e)
		{
			SoapUI.logError(e);
		}
		return null;
	}

	private void addAttachment(Request request, BytesMessage bytesMessageReceive, JMSResponse jmsResponse)
			throws JMSException
	{
		try
		{
			byte[] buff = new byte[1];
			File temp = File.createTempFile("bytesmessage", ".tmp");
			OutputStream out = new FileOutputStream(temp);
			while (bytesMessageReceive.readBytes(buff) != -1)
			{
				out.write(buff);
			}
			out.close();
			Attachment[] attachments = new Attachment[] { new RequestFileAttachment(temp, false,
					(AbstractHttpRequest<?>) request) };
			jmsResponse.setAttachments(attachments);
		}
		catch (IOException e)
		{
			SoapUI.logError(e);
		}
	}

	protected String getEndpointParameter(String[] parameters, int i, Domain domain, SubmitContext submitContext)
	{
		if (domain == null)
			return PropertyExpander.expandProperties(submitContext, parameters[i]);
		else if (domain.equals(Domain.QUEUE))
			return PropertyExpander.expandProperties(submitContext, parameters[i]).replaceFirst(
					HermesJmsRequestTransport.QUEUE_ENDPOINT_PREFIX, "");
		else
			return PropertyExpander.expandProperties(submitContext, parameters[i]).replaceFirst(
					HermesJmsRequestTransport.TOPIC_ENDPOINT_PREFIX, "");
	}

	protected String[] extractEndpointParameters(Request request)
	{
		String[] parameters = request.getEndpoint().substring(request.getEndpoint().indexOf("://") + 3).split("/");
		return parameters;
	}

	public static class UnresolvedJMSEndpointException extends Exception
	{
		public UnresolvedJMSEndpointException(String msg)
		{
			super(msg);
		}
	}

}
