/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
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
import javax.jms.Topic;
import javax.jms.TopicSession;
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
		TopicSession topicSession = null;
		QueueSession queueSession = null;
		TopicSubscriber topicSubsriber = null;
		JMSConnectionHolder jmsConnectionHolder = null;
		try
		{
			init( submitContext, request );
			String clientIDString = StringUtils.hasContent( clientID ) ? clientID : jmsEndpoint.getSessionName() + "-"
					+ jmsEndpoint.getReceive();
			jmsConnectionHolder = new JMSConnectionHolder( jmsEndpoint, hermes, true, true, clientIDString, username,
					password );

			// session
			topicSession = jmsConnectionHolder.getTopicSession();
			queueSession = jmsConnectionHolder.getQueueSession();

			Queue queueSend = jmsConnectionHolder.getQueue( jmsConnectionHolder.getJmsEndpoint().getSend() );
			Topic topicReceive = jmsConnectionHolder.getTopic( jmsConnectionHolder.getJmsEndpoint().getReceive() );

			topicSubsriber = topicSession.createDurableSubscriber( topicReceive, StringUtils
					.hasContent( durableSubscriptionName ) ? durableSubscriptionName : "durableSubscription"
					+ jmsConnectionHolder.getJmsEndpoint().getReceive(), messageSelector, false );

			Message textMessageSend = messageSend( submitContext, request, queueSession, jmsConnectionHolder.getHermes(),
					queueSend );

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
			closeSessionAndConnection( jmsConnectionHolder != null ? jmsConnectionHolder.getQueueConnection() : null,queueSession );
			closeSessionAndConnection( jmsConnectionHolder != null ? jmsConnectionHolder.getTopicConnection() : null,topicSession );
		}
		return null;
	}
}
