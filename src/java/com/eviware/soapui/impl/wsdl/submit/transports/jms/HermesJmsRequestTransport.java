
package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import hermes.Hermes;
import hermes.HermesInitialContextFactory;
import hermes.JAXBHermesLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.NotImplementedException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.RequestTransport;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry.CannotResolveJmsTypeException;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry.MissingTransportException;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.HermesJMSClasspathHacker;

public class HermesJmsRequestTransport implements RequestTransport
{

	private static boolean hermesJarsLoaded = false;
	private static Map<String,Context> contextMap = new HashMap<String,Context>();
	
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
				throw new NotImplementedException();
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
			else if (destinationSendName.startsWith("topic_") || destinationReceiveName.startsWith("topic_"))
			{
				throw new NotImplementedException();
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
			if (!hermesJarsLoaded)
			{
				addHermesJarsToClasspath();
				hermesJarsLoaded = true;
			}

			Context ctx = hermesContext(project);

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

	
	private Context hermesContext(WsdlProject project) throws NamingException
	{
		if(contextMap.containsKey(project.getName())){
			return contextMap.get(project.getName());
		}
		
		String hermesConfigPath = PropertyExpander.expandProperties(project, project.getHermesConfig());
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, HermesInitialContextFactory.class.getName());
		props.put(Context.PROVIDER_URL, hermesConfigPath + "\\hermes-config.xml");
		props.put("hermes.loader", JAXBHermesLoader.class.getName());

		Context ctx = new InitialContext(props);
		contextMap.put(project.getName(), ctx);
		return ctx;
	}
	
	// TODO: this could be called on souapui startup if hermes config path is set
	private static void addHermesJarsToClasspath() throws IOException, MalformedURLException
	{
		String hermesLib = SoapUI.getSettings().getString(ToolsSettings.HERMES_1_13, null) + File.separator + "lib";

		if (hermesLib == null || "".equals(hermesLib))
		{
			throw new FileNotFoundException("HermesJMS home not specified !!!");
		}

		File dir = new File(hermesLib);

		String[] children = dir.list();
		for (String filename : children)
		{
			HermesJMSClasspathHacker.addURL(new URL("file:" + File.separator + hermesLib + File.separator + filename));
		}

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
