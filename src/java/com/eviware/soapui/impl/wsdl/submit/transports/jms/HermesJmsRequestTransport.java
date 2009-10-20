
package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import hermes.Hermes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
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

import org.apache.commons.lang.NotImplementedException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.RequestTransport;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry.CannotResolveJmsTypeException;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry.MissingTransportException;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.HermesUtils;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.support.ModelSupport;

public class HermesJmsRequestTransport implements RequestTransport
{

	public static final String JMS_MESSAGE_RECEIVE = "JMS_MESSAGE_RECEIVE";
	public static final String JMS_MESSAGE_SEND = "JMS_MESSAGE_SEND";
	public static final String JMS_RESPONSE  = "JMS_RESPONSE";
	public static final String JMS_ERROR  = "JMS_ERROR";
	public static final String HERMES_SESSION_NAME  = "HERMES_SESSION_NAME";
	public static final String JMS_RECEIVE_TIMEOUT = "JMS_RECEIVE_TIMEOUT";
	
	
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
		return resolveType(submitContext,request).execute(submitContext, request, timeStarted);
	}

	protected Response execute(SubmitContext submitContext, Request request, long timeStarted) throws Exception
	{
		throw new NotImplementedException();
	}

	private static HermesJmsRequestTransport resolveType(SubmitContext submitContext,Request request) throws CannotResolveJmsTypeException,
			MissingTransportException
	{

		int ix = request.getEndpoint().indexOf("://");
		if (ix == -1)
			throw new MissingTransportException("Missing protocol in endpoint [" + request.getEndpoint() + "]");

		String[] params = request.getEndpoint().substring(ix + 3).split("/");

		// resolve sending class
		if (params.length == 2)
		{

			String destinationName = PropertyExpander.expandProperties(submitContext,params[1]);
			if (destinationName.startsWith("queue_"))
			{
				return new HermesJmsRequestSendTransport();
			}
			else if (destinationName.startsWith("topic_"))
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
			String destinationName =PropertyExpander.expandProperties(submitContext, params[2]);
			if (destinationName.startsWith("queue_"))
			{
				return new HermesJmsRequestReceiveTransport();
			}
			else if (destinationName.startsWith("topic_"))
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
			String destinationSendName =PropertyExpander.expandProperties(submitContext, params[1]);
			String destinationReceiveName =PropertyExpander.expandProperties(submitContext, params[2]);
			if (destinationSendName.startsWith("queue_") && destinationReceiveName.startsWith("queue_"))
			{
				return new HermesJmsRequestSendReceiveTransport();
			}
			else if (destinationSendName.startsWith("queue_") && destinationReceiveName.startsWith("topic_"))
			{
				return new HermesJmsRequestSendSubscribeTransport();
			}
			else if (destinationSendName.startsWith("topic_") && destinationReceiveName.startsWith("topic_"))
			{
				return new HermesJmsRequestPublishSubscribeTransport();
			}
			else if (destinationSendName.startsWith("topic_") && destinationReceiveName.startsWith("queue_"))
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
		catch( Exception e )
		{}
		
		return to;
	}

	
	protected JMSHeader createJMSHeader(SubmitContext submitContext, Request request, Hermes hermes, TextMessage textMessage)
	{
		JMSHeader jmsHeader = new JMSHeader();
		jmsHeader.setMessageHeaders(textMessage, request, hermes, submitContext);
		JMSHeader.setMessageProperties(textMessage, request, hermes, submitContext);
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


	protected TextMessage messageSend(SubmitContext submitContext, Request request, Session session, Hermes hermes, Queue queueSend) throws JMSException
	{
		TextMessage textMessageSend = session.createTextMessage();
		String messageBody = PropertyExpander.expandProperties(submitContext, request.getRequestContent());
		textMessageSend.setText(messageBody);
		
	
		MessageProducer messageProducer = session.createProducer(queueSend);
	
		JMSHeader jmsHeader = createJMSHeader(submitContext, request, hermes, textMessageSend);
	
		// send message to producer
		messageProducer.send(textMessageSend, textMessageSend.getJMSDeliveryMode(), textMessageSend.getJMSPriority(),
				jmsHeader.getTimeTolive());
	
		submitContext.setProperty(JMS_MESSAGE_SEND, textMessageSend);
		return textMessageSend;
	}


	protected Response makeResponse(SubmitContext submitContext, Request request, long timeStarted, TextMessage textMessageSend, MessageConsumer messageConsumer)
			throws JMSException
	{
		JMSResponse response;
		long timeout = getTimeout(submitContext, request);
	
		Message messageReceive = messageConsumer.receive(timeout);
		
		if (messageReceive != null)
		{
			TextMessage textMessageReceive = null;
			if (messageReceive instanceof TextMessage)
			{
				textMessageReceive = (TextMessage) messageReceive;
			}
			// make response
			response = new JMSResponse(textMessageReceive.getText(), textMessageSend,textMessageReceive, request, timeStarted);
			
			
			submitContext.setProperty(JMS_MESSAGE_RECEIVE, messageReceive);
			submitContext.setProperty(JMS_RESPONSE, response);
			
			
			return response;
		}
		else
		{
			return  new JMSResponse("", null,null, request, timeStarted);
		}
	}


	protected TextMessage messagePublish(SubmitContext submitContext, Request request, TopicSession topicSession, Hermes hermes, Topic topicPublish)
			throws JMSException
	{
		TopicPublisher messageProducer = topicSession.createPublisher(topicPublish);
	
		TextMessage textMessagePublish = topicSession.createTextMessage();
		String messageBody = PropertyExpander.expandProperties(submitContext, request.getRequestContent());
		textMessagePublish.setText(messageBody);
	
		JMSHeader jmsHeader = new JMSHeader();
		jmsHeader.setMessageHeaders(textMessagePublish, request, hermes, submitContext);
		JMSHeader.setMessageProperties(textMessagePublish, request, hermes, submitContext);
	
		messageProducer.send(textMessagePublish, textMessagePublish.getJMSDeliveryMode(), textMessagePublish
				.getJMSPriority(), jmsHeader.getTimeTolive());
	
		submitContext.setProperty(JMS_MESSAGE_SEND, textMessagePublish);
		return textMessagePublish;
	}


	public static class UnresolvedJMSEndpointException extends Exception
	{
		public UnresolvedJMSEndpointException(String msg)
		{
			super(msg);
		}
	}

}
