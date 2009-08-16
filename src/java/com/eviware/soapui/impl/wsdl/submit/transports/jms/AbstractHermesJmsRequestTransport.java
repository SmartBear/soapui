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

import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.RequestTransport;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public abstract class AbstractHermesJmsRequestTransport implements RequestTransport
{
	protected List<RequestFilter> filters = new ArrayList<RequestFilter>();

	@Override
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

	public abstract Response sendRequest(SubmitContext submitContext, Request request) throws Exception;

	
	protected Hermes getHermes(String sessionName) throws NamingException
	{
		Properties props = new Properties() ;
		props.put(Context.INITIAL_CONTEXT_FACTORY, HermesInitialContextFactory.class.getName()) ;
		props.put(Context.PROVIDER_URL, "D:\\.hermes\\hermes-config.xml") ;// path to hermes-config.xml
		props.put("hermes.loader", JAXBHermesLoader.class.getName()) ;

		Context ctx = new InitialContext(props) ;
		Hermes hermes = (Hermes) ctx.lookup(sessionName);// lookup for session name configured in hermes
		return hermes;
	}
}
