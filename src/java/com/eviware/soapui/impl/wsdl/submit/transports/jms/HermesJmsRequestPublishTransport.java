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
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;

public class HermesJmsRequestPublishTransport extends HermesJmsRequestTransport
{

	public Response execute(SubmitContext submitContext, Request request, long timeStarted) throws Exception
	{
		TopicConnectionFactory connectionFactory = null;
		TopicConnection connection = null;
		TopicSession session = null;
		try
		{
			String topicName = null;
			String sessionName = null;
			String[] parameters = request.getEndpoint().substring(request.getEndpoint().indexOf("://") + 3).split("/");
			if (parameters.length == 2)
			{
				sessionName = PropertyExpander.expandProperties(submitContext, parameters[0]);
				topicName = PropertyExpander.expandProperties(submitContext, parameters[1]).replaceFirst("topic_", "");
			}
			else
				throw new UnresolvedJMSEndpointException("bad jms alias!!!!!");

			submitContext.setProperty(HERMES_SESSION_NAME, sessionName);
			Hermes hermes = getHermes(sessionName, request);
			// connection factory
			connectionFactory = (TopicConnectionFactory) hermes.getConnectionFactory();

			// connection
			connection = connectionFactory.createTopicConnection();
			connection.start();

			// session
			session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

			// destination
			Topic topicPublish = (Topic) hermes.getDestination(topicName, Domain.TOPIC);

			TextMessage textMessagePublish = messagePublish(submitContext, request, session, hermes, topicPublish);

			return makeResponseOnly(submitContext, request, timeStarted, textMessagePublish);
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
