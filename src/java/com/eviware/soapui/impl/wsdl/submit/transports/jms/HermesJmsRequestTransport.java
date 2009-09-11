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
import java.util.List;
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
import com.eviware.soapui.support.ClasspathHacker;

public class HermesJmsRequestTransport implements RequestTransport
{

	private static boolean hermesJarsLoaded = false;

	protected List<RequestFilter> filters = new ArrayList<RequestFilter>();

	public void abortRequest(SubmitContext submitContext)
	{
		throw new NotImplementedException();
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
		return resolveType(request).execute(submitContext, request);
	}

	protected Response execute(SubmitContext submitContext, Request request) throws Exception
	{
		throw new NotImplementedException();
	}

	private static HermesJmsRequestTransport resolveType(Request request) throws CannotResolveJmsTypeException,
			MissingTransportException
	{

		int ix = request.getEndpoint().indexOf("://");
		if (ix == -1)
			throw new MissingTransportException("Missing protocol in endpoint [" + request.getEndpoint() + "]");

		String[] params = request.getEndpoint().substring(ix + 3).split("/");
		if (params.length == 2)
		{
			return new HermesJmsRequestSendTransport();
		}
		else if (params.length == 3 && params[1].equals("-"))
		{
			return new HermesJmsRequestReceiveTransport();
		}
		else if (params.length == 3)
		{
			return new HermesJmsRequestSendReceiveTransport();
		}
		else
		{
			throw new CannotResolveJmsTypeException(
					"Bad jms alias! /nFor JMS please use this endpont pattern:\nfor sending 'jms://configfilename/queue' \nfor receive  'jms://configfilename/-/topic'\nfor send-receive 'jms://configfilename/queue/topic'");
		}

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
		String hermesConfigPath = PropertyExpander.expandProperties(project, project.getHermesConfig());
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, HermesInitialContextFactory.class.getName());
		props.put(Context.PROVIDER_URL, hermesConfigPath + "\\hermes-config.xml");
		props.put("hermes.loader", JAXBHermesLoader.class.getName());

		Context ctx = new InitialContext(props);
		return ctx;
	}

	private void addHermesJarsToClasspath() throws IOException, MalformedURLException
	{
		String hermesLib = SoapUI.getSettings().getString( ToolsSettings.HERMES_1_13, null )+ File.separator + "lib";
	   
		if(hermesLib==null || "".equals(hermesLib)) 	{
			throw new FileNotFoundException("HermesJMS home not specified !!!") ;
		}
		
		File dir = new File(hermesLib);

		String[] children = dir.list();
		for (String filename : children)
		{
			ClasspathHacker.addURL(new URL("file:" + File.separator + hermesLib + File.separator + filename));
		}
	

	}

}
