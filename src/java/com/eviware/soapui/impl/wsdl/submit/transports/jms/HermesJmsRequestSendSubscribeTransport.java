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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.StringUtils;

public class HermesJmsRequestSendSubscribeTransport extends HermesJmsRequestTransport
{

	public Response execute( SubmitContext submitContext, Request request, long timeStarted ) throws Exception
	{
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		TopicSubscriber topicSubsriber = null;
		try
		{
			
			JMSEndpoint jmsEndpoint = new JMSEndpoint( request, submitContext );
			
//			String[] parameters = extractEndpointParameters( request );
//			String sessionName = getEndpointParameter( parameters, 0, null, submitContext );
//			String queueNameSend = getEndpointParameter( parameters, 1, Domain.QUEUE, submitContext );
//			String topicNameReceive = getEndpointParameter( parameters, 2, Domain.TOPIC, submitContext );

			submitContext.setProperty( HERMES_SESSION_NAME, jmsEndpoint.getSessionName() );

			Hermes hermes = getHermes( jmsEndpoint.getSessionName(), request );

			connectionFactory = ( javax.jms.ConnectionFactory )hermes.getConnectionFactory();

			// connection
			String username = submitContext.expand( request.getUsername() );
			String password = submitContext.expand( request.getPassword() );

			connection = StringUtils.hasContent( username ) ? connectionFactory.createConnection( username, password )
					: connectionFactory.createConnection();

			connection.setClientID( jmsEndpoint.getSessionName() + "-" + jmsEndpoint.getReceive() );
			connection.start();

			session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );

			Queue queueSend = ( Queue )hermes.getDestination( jmsEndpoint.getSend(), Domain.QUEUE );
			Topic topicReceive = ( Topic )hermes.getDestination( jmsEndpoint.getReceive(), Domain.TOPIC );

			topicSubsriber = session.createDurableSubscriber( topicReceive, "durableSubscription" + jmsEndpoint.getReceive());

			Message textMessageSend = messageSend( submitContext, request, session, hermes, queueSend );

			return makeResponse( submitContext, request, timeStarted, textMessageSend, topicSubsriber );
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
			if( topicSubsriber != null )
				topicSubsriber.close();
			closeSessionAndConnection( connection, session );
		}
		return null;
	}
}
