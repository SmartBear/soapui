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
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;

public class HermesJmsRequestSubscribeTransport extends HermesJmsRequestTransport
{

	public Response execute(SubmitContext submitContext, Request request, long timeStarted) throws Exception
	{
		TopicConnectionFactory connectionFactory = null;
		TopicConnection connection = null;
		TopicSession session = null;
		JMSResponse response = null;
		TopicSubscriber topicDurableSubsriber = null;
		try
		{
			String topicNameSubscribe = null;
			String sessionName = null;
			String[] parameters = request.getEndpoint().substring(request.getEndpoint().indexOf("://") + 3).split("/");
			if (parameters.length == 3)
			{
				sessionName = PropertyExpander.expandProperties(submitContext, parameters[0]);
				topicNameSubscribe = PropertyExpander.expandProperties(submitContext, parameters[2]).replaceFirst("topic_",
						"");
			}
			else
				throw new Exception("bad jms alias!!!!!");

			submitContext.setProperty(HERMES_SESSION_NAME, sessionName);

			Hermes hermes = getHermes(sessionName, request);
			// connection factory
			connectionFactory = (TopicConnectionFactory) hermes.getConnectionFactory();

			// connection
			connection = connectionFactory.createTopicConnection();
			connection.setClientID("" + (Math.random() * 1000));
			connection.start();

			// session
			session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

			// destination
			Topic topicSubscribe = (Topic) hermes.getDestination(topicNameSubscribe, Domain.TOPIC);
			

    		// create durable subscriber
			topicDurableSubsriber = session.createDurableSubscriber(topicSubscribe, "durableSubscription" + topicNameSubscribe);
					

			long timeout = getTimeout(submitContext, request);
			Message messageReceive = topicDurableSubsriber.receive(timeout);
			
			if (messageReceive != null)
			{
				TextMessage textMessageReceive = null;
				if (messageReceive instanceof TextMessage)
				{
					textMessageReceive = (TextMessage) messageReceive;
				}
				// make response
				response = new JMSResponse(textMessageReceive.getText(), null, textMessageReceive, request,
						timeStarted);

				submitContext.setProperty(JMS_MESSAGE_RECEIVE, messageReceive);
				submitContext.setProperty(JMS_RESPONSE, response);

				return response;
			}
			else
			{
				return new JMSResponse("", null, null, request, timeStarted);
			}
		}
		catch (JMSException jmse)
		{
			SoapUI.logError(jmse);
			submitContext.setProperty(JMS_ERROR, jmse);
			response = new JMSResponse("", null, null, request, timeStarted);
			submitContext.setProperty(JMS_RESPONSE, response);

			return response;
		}
		catch (Throwable t)
		{
			SoapUI.logError(t);
		}
		finally
		{
			if (topicDurableSubsriber != null)
				topicDurableSubsriber.close();
			if (session != null)
				session.close();
			if (connection != null)
				connection.close();
		}
		return null;
	}
}

