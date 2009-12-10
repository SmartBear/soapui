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
		TopicConnectionFactory connectionFactory = null;
		TopicConnection connection = null;
		TopicSession session = null;
		TopicSubscriber topicDurableSubsriber = null;
		try
		{
			String[] parameters = extractEndpointParameters(request);
			String sessionName = getEndpointParameter(parameters, 0, null, submitContext);
			String topicNamePublish = getEndpointParameter(parameters, 1, Domain.TOPIC, submitContext);
			String topicNameSubscribe = getEndpointParameter(parameters, 2, Domain.TOPIC, submitContext);

			submitContext.setProperty(HERMES_SESSION_NAME, sessionName);

			Hermes hermes = getHermes(sessionName, request);
			// connection factory
			connectionFactory = (TopicConnectionFactory) hermes.getConnectionFactory();

			// connection
			connection = connectionFactory.createTopicConnection();
			connection.setClientID(sessionName+"-"+topicNameSubscribe);
			connection.start();

			// session
			session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

			// destination
			Topic topicPublish = (Topic) hermes.getDestination(topicNamePublish, Domain.TOPIC);
			Topic topicSubscribe = (Topic) hermes.getDestination(topicNameSubscribe, Domain.TOPIC);
			
			topicDurableSubsriber = session.createDurableSubscriber(topicSubscribe, "durableSubscription" + topicNameSubscribe);
			
		  Message messagePublish = messagePublish(submitContext, request, session, hermes, topicPublish);

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
			closeSessionAndConnection(connection, session);
		}
		return null;
	}
}
