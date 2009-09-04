package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import hermes.Hermes;
import hermes.HermesInitialContextFactory;
import hermes.JAXBHermesLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.RequestTransport;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry.CannotResolveJmsTypeException;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry.MissingTransportException;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public   class HermesJmsRequestTransport implements RequestTransport
{
	private final static Logger log = Logger.getLogger(HermesJmsRequestTransport.class);

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

	public  Response sendRequest(SubmitContext submitContext, Request request) throws Exception{
		return resolveType(request).execute(submitContext, request);
	}

	protected Response execute(SubmitContext submitContext, Request request) throws Exception
	{
		throw new NotImplementedException();
	}
	
	private static HermesJmsRequestTransport resolveType( Request request) throws CannotResolveJmsTypeException, MissingTransportException
	{
		
		int ix = request.getEndpoint().indexOf( "://" );
		if( ix == -1 )
			throw new MissingTransportException( "Missing protocol in endpoint [" + request.getEndpoint() + "]" );
		
		
		String[] params = request.getEndpoint().substring(ix+3).split("/");
		if(params.length==2){
			return  new HermesJmsRequestSendTransport();
		}else if(params.length==3 && params[1].equals("-")){
			return new HermesJmsRequestReceiveTransport();
		}else if(params.length==3){
			return new HermesJmsRequestSendReceiveTransport();
		}else {
			throw new CannotResolveJmsTypeException("Bad jms alias! /nFor JMS please use this endpont pattern:\nfor sending 'jms://configfilename/queue' \nfor receive  'jms://configfilename/-/topic'\nfor send-receive 'jms://configfilename/queue/topic'");
		}
		
	}

	protected Hermes getHermes(String sessionName) throws NamingException
	{
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, HermesInitialContextFactory.class.getName());
		props.put(Context.PROVIDER_URL, "D:\\.hermes\\hermes-config.xml");// path to hermes-config.xml
		props.put("hermes.loader", JAXBHermesLoader.class.getName());
		try
		{
			Context ctx = new InitialContext(props);
			Hermes hermes = (Hermes) ctx.lookup(sessionName);// lookup for session name configured in hermes
			return hermes;
		}
		catch (Throwable t)
		{
			log.info(t);
			t.printStackTrace();
		}
		return null;
	}
	
	
}
