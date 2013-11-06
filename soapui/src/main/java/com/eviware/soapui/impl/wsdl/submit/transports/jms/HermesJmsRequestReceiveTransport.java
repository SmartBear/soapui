/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public class HermesJmsRequestReceiveTransport extends HermesJmsRequestTransport
{

	public Response execute( SubmitContext submitContext, Request request, long timeStarted ) throws Exception
	{
		Session queueSession = null;
		JMSConnectionHolder jmsConnectionHolder = null;
		try
		{
			init( submitContext, request );
			jmsConnectionHolder = new JMSConnectionHolder( jmsEndpoint, hermes, false, clientID, username, password );

			// session
			queueSession = jmsConnectionHolder.getSession();

			// destination
			Queue queue = jmsConnectionHolder.getQueue( jmsConnectionHolder.getJmsEndpoint().getReceive() );

			// consumer
			MessageConsumer messageConsumer = queueSession.createConsumer( queue, submitContext.expand( messageSelector ) );

			return makeResponse( submitContext, request, timeStarted, null, messageConsumer );

		}
		catch( JMSException jmse )
		{
			return errorResponse( submitContext, request, timeStarted, jmse );
		}
		catch( Throwable t )
		{
			SoapUI.logError( t );
		}
		finally
		{
			if( jmsConnectionHolder != null )
				jmsConnectionHolder.closeAll();
			closeSessionAndConnection( jmsConnectionHolder != null ? jmsConnectionHolder.getConnection() : null,
					queueSession );
		}
		return null;
	}
}
