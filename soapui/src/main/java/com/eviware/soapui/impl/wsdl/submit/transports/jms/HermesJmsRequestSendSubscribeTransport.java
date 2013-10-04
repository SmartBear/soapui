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
		Session topicSession = null;
		Session queueSession = null;
		TopicSubscriber topicDurableSubsriber = null;
		JMSConnectionHolder jmsConnectionHolderTopic = null;
		JMSConnectionHolder jmsConnectionHolderQueue = null;
		try
		{
			init( submitContext, request );
			String clientIDString = StringUtils.hasContent( clientID ) ? clientID : jmsEndpoint.getSessionName() + "-"
					+ jmsEndpoint.getReceive();
			jmsConnectionHolderTopic = new JMSConnectionHolder( jmsEndpoint, hermes, true, clientIDString, username,
					password );
			jmsConnectionHolderQueue = new JMSConnectionHolder( jmsEndpoint, hermes, false, null, username, password );
			// session
			topicSession = jmsConnectionHolderTopic.getSession();
			queueSession = jmsConnectionHolderQueue.getSession();

			Queue queueSend = jmsConnectionHolderQueue.getQueue( jmsConnectionHolderQueue.getJmsEndpoint().getSend() );

			Topic topicSubscribe = jmsConnectionHolderTopic.getTopic( jmsConnectionHolderTopic.getJmsEndpoint()
					.getReceive() );
			topicDurableSubsriber = createDurableSubscription( submitContext, topicSession, jmsConnectionHolderTopic );

			Message textMessageSend = messageSend( submitContext, request, queueSession,
					jmsConnectionHolderQueue.getHermes(), queueSend, topicSubscribe );

			return makeResponse( submitContext, request, timeStarted, textMessageSend, topicDurableSubsriber );
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
			if( topicDurableSubsriber != null )
				topicDurableSubsriber.close();
			if( jmsConnectionHolderQueue != null )
				jmsConnectionHolderQueue.closeAll();
			if( jmsConnectionHolderTopic != null )
				jmsConnectionHolderTopic.closeAll();
			closeSessionAndConnection( jmsConnectionHolderQueue != null ? jmsConnectionHolderQueue.getConnection() : null,
					queueSession );
			closeSessionAndConnection( jmsConnectionHolderTopic != null ? jmsConnectionHolderTopic.getConnection() : null,
					topicSession );
		}
		return null;
	}
}
