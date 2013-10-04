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
import javax.jms.Session;
import javax.jms.Topic;
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
		Session topicSession = null;
		TopicSubscriber topicDurableSubsriber = null;
		JMSConnectionHolder jmsConnectionHolder = null;
		try
		{
			init( submitContext, request );
			String clientIDString = StringUtils.hasContent( clientID ) ? clientID : jmsEndpoint.getSessionName() + "-"
					+ jmsEndpoint.getReceive();
			jmsConnectionHolder = new JMSConnectionHolder( jmsEndpoint, hermes, true, clientIDString, username, password );

			// session
			topicSession = jmsConnectionHolder.getSession();

			// destination
			Topic topicPublish = jmsConnectionHolder.getTopic( jmsConnectionHolder.getJmsEndpoint().getSend() );
			Topic topicSubscribe = jmsConnectionHolder.getTopic( jmsConnectionHolder.getJmsEndpoint().getReceive() );

			topicDurableSubsriber = createDurableSubscription( submitContext, topicSession, jmsConnectionHolder );

			Message messagePublish = messagePublish( submitContext, request, topicSession,
					jmsConnectionHolder.getHermes(), topicPublish, topicSubscribe );

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
			if( jmsConnectionHolder != null )
				jmsConnectionHolder.closeAll();
			closeSessionAndConnection( jmsConnectionHolder != null ? jmsConnectionHolder.getConnection() : null,
					topicSession );
		}
		return null;
	}
}
