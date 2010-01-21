/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueSession;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public class HermesJmsRequestSendTransport extends HermesJmsRequestTransport
{

	public Response execute( SubmitContext submitContext, Request request, long timeStarted ) throws Exception
	{
		QueueSession queueSession = null;
		JMSConnectionHolder jmsConnectionHolder = null;
		try
		{
			init( submitContext, request );
			jmsConnectionHolder = new JMSConnectionHolder( jmsEndpoint, hermes, true, false, clientID , username, password); 

			// session
			queueSession = jmsConnectionHolder.getQueueSession();

			// queue
			Queue queueSend = jmsConnectionHolder.getQueue( jmsConnectionHolder.getJmsEndpoint().getSend() );

			Message messageSend = messageSend( submitContext, request, queueSession, jmsConnectionHolder.getHermes(),
					queueSend );

			return makeEmptyResponse( submitContext, request, timeStarted, messageSend );
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
			closeSessionAndConnection( jmsConnectionHolder.getQueueConnection(), queueSession );
		}
		return null;
	}
}
