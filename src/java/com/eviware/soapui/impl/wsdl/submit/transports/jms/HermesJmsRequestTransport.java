
package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import hermes.Hermes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

	
	public static class UnresolvedJMSEndpointException extends Exception
	{
		public UnresolvedJMSEndpointException(String msg)
		{
			super(msg);
		}
	}

}
