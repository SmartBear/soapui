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

import hermes.Domain;
import hermes.Hermes;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public class HermesJmsRequestSendTransport extends HermesJmsRequestTransport
{

	public Response execute( SubmitContext submitContext, Request request, long timeStarted ) throws Exception
	{
		QueueConnectionFactory queueConnectionFactory = null;
		QueueConnection queueConnection = null;
		QueueSession queueSession = null;
		try
		{
			JMSEndpoint jmsEndpoint = new JMSEndpoint( request, submitContext );

			submitContext.setProperty( HERMES_SESSION_NAME,jmsEndpoint.getSessionName() );

			Hermes hermes = getHermes( jmsEndpoint.getSessionName(), request );

			// connection factory
			queueConnectionFactory = ( javax.jms.QueueConnectionFactory )hermes.getConnectionFactory();

			queueConnection = (QueueConnection)createConnection( submitContext, request, queueConnectionFactory ,Domain.QUEUE,  null);
			queueConnection.start();

			// session
			queueSession = queueConnection.createQueueSession( false, Session.AUTO_ACKNOWLEDGE );

			// queue
			Queue queueSend = ( Queue )hermes.getDestination( jmsEndpoint.getSend(), Domain.QUEUE );

			Message messageSend = messageSend( submitContext, request, queueSession, hermes, queueSend );

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
			closeSessionAndConnection( queueConnection, queueSession );
		}
		return null;
	}
}
