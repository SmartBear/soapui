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
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.StringUtils;

public class HermesJmsRequestPublishSubscribeTransport extends HermesJmsRequestTransport
{

	public Response execute( SubmitContext submitContext, Request request, long timeStarted ) throws Exception
	{
		TopicSession topicSession = null;
		TopicSubscriber topicDurableSubsriber = null;
		JMSConnectionHolder jmsConnectionHolder = null;
		try
		{
			init( submitContext, request );
			String clientIDString = StringUtils.hasContent( clientID ) ? clientID : jmsEndpoint.getSessionName() + "-"
					+ jmsEndpoint.getReceive();
			jmsConnectionHolder = new JMSConnectionHolder( jmsEndpoint, hermes, false, true, clientIDString, username,
					password );

			// session
			topicSession = jmsConnectionHolder.getTopicSession();

			// destination
			Topic topicPublish = jmsConnectionHolder.getTopic( jmsConnectionHolder.getJmsEndpoint().getSend() );
			Topic topicSubscribe = jmsConnectionHolder.getTopic( jmsConnectionHolder.getJmsEndpoint().getReceive() );

			topicDurableSubsriber = topicSession.createDurableSubscriber( topicSubscribe, StringUtils
					.hasContent( durableSubscriptionName ) ? durableSubscriptionName : "durableSubscription"
					+ jmsConnectionHolder.getJmsEndpoint().getReceive(), messageSelector, false );

			Message messagePublish = messagePublish( submitContext, request, topicSession,
					jmsConnectionHolder.getHermes(), topicPublish );

			return makeResponse( submitContext, request, timeStarted, messagePublish, topicDurableSubsriber );
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
			closeSessionAndConnection( jmsConnectionHolder != null ? jmsConnectionHolder.getTopicConnection() : null,
					topicSession );
		}
		return null;
	}
}
