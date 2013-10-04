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
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public class HermesJmsRequestPublishReceiveTransport extends HermesJmsRequestTransport
{

	public Response execute( SubmitContext submitContext, Request request, long timeStarted ) throws Exception
	{
		Session topicSession = null;
		Session queueSession = null;

		JMSConnectionHolder jmsConnectionHolderTopic = null;
		JMSConnectionHolder jmsConnectionHolderQueue = null;
		try
		{
			init( submitContext, request );
			jmsConnectionHolderTopic = new JMSConnectionHolder( jmsEndpoint, hermes, true, clientID, username, password );
			jmsConnectionHolderQueue = new JMSConnectionHolder( jmsEndpoint, hermes, false, null, username, password );

			// session
			topicSession = jmsConnectionHolderTopic.getSession();
			queueSession = jmsConnectionHolderQueue.getSession();

			// destination
			Topic topicPublish = jmsConnectionHolderTopic.getTopic( jmsConnectionHolderTopic.getJmsEndpoint().getSend() );
			Queue queueReceive = jmsConnectionHolderQueue
					.getQueue( jmsConnectionHolderQueue.getJmsEndpoint().getReceive() );

			Message messagePublish = messagePublish( submitContext, request, topicSession,
					jmsConnectionHolderTopic.getHermes(), topicPublish, queueReceive );

			MessageConsumer messageConsumer = queueSession.createConsumer( queueReceive,
					submitContext.expand( messageSelector ) );

			return makeResponse( submitContext, request, timeStarted, messagePublish, messageConsumer );
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
			if( jmsConnectionHolderQueue != null )
				jmsConnectionHolderQueue.closeAll();

			closeSessionAndConnection( jmsConnectionHolderQueue != null ? jmsConnectionHolderQueue.getConnection() : null,
					queueSession );
			closeSessionAndConnection( jmsConnectionHolderTopic != null ? jmsConnectionHolderTopic.getConnection() : null,
					topicSession );

		}
		return null;

	}
}
