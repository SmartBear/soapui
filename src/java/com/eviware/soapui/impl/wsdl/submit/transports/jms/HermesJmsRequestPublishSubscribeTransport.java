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
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public class HermesJmsRequestPublishSubscribeTransport extends HermesJmsRequestTransport
{

	public Response execute(SubmitContext submitContext, Request request, long timeStarted) throws Exception
	{
		TopicConnectionFactory topicConnectionFactory = null;
		TopicConnection topicConnection = null;
		TopicSession topicSession = null;
		TopicSubscriber topicDurableSubsriber = null;
		try
		{
			JMSEndpoint jmsEndpoint = new JMSEndpoint( request, submitContext );

			submitContext.setProperty(HERMES_SESSION_NAME, jmsEndpoint.getSessionName());

			Hermes hermes = getHermes(jmsEndpoint.getSessionName(), request);
			// connection factory
			topicConnectionFactory = (TopicConnectionFactory) hermes.getConnectionFactory();

		// connection
			topicConnection = (TopicConnection)createConnection( submitContext, request, topicConnectionFactory ,Domain.TOPIC,  jmsEndpoint.getSessionName()+"-"+jmsEndpoint.getReceive());
			topicConnection.start();

			// session
			topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

			// destination
			Topic topicPublish = (Topic) hermes.getDestination(jmsEndpoint.getSend(), Domain.TOPIC);
			Topic topicSubscribe = (Topic) hermes.getDestination(jmsEndpoint.getReceive(), Domain.TOPIC);
			
			topicDurableSubsriber = topicSession.createDurableSubscriber(topicSubscribe, "durableSubscription" + jmsEndpoint.getReceive());
			
		  Message messagePublish = messagePublish(submitContext, request, topicSession, hermes, topicPublish);

			return makeResponse(submitContext, request, timeStarted, messagePublish, topicDurableSubsriber);
		}
		catch (JMSException jmse)
		{
			return errorResponse(submitContext, request, timeStarted, jmse);
		}
		catch (Throwable t)
		{
			SoapUI.logError(t);
		}
		finally
		{
			if( topicDurableSubsriber != null )
				topicDurableSubsriber.close();
			closeSessionAndConnection(topicConnection, topicSession);
		}
		return null;
	}
}
