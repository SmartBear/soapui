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

import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
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
				sessionName =PropertyExpander.expandProperties(submitContext, parameters[0]);
				topicName = PropertyExpander.expandProperties(submitContext,parameters[1]).replaceFirst("topic_", "");
			}
			else
				throw new UnresolvedJMSEndpointException("bad jms alias!!!!!");

			Hermes hermes = getHermes(sessionName, request);
		   // connection factory
			connectionFactory = (TopicConnectionFactory) hermes.getConnectionFactory();

			// connection
			connection = connectionFactory.createTopicConnection();
			connection.start();

			// session
			session =  connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

			// destination
			Topic topic =(Topic) hermes.getDestination(topicName, Domain.TOPIC);

			// publisher
			TopicPublisher messageProducer = session.createPublisher(topic);

			// message
			TextMessage textMessage = session.createTextMessage();
			String messageBody = PropertyExpander.expandProperties(submitContext,request.getRequestContent());
			textMessage.setText(messageBody);
		
			JMSHeader jmsHeader= new JMSHeader();
         jmsHeader.setMessageHeaders(textMessage, request, hermes);
         JMSHeader.setMessageProperties(textMessage, request, hermes);
         
         // publish message to producer
			messageProducer.send(textMessage, 
										textMessage.getJMSDeliveryMode(),
										textMessage.getJMSPriority(),
										jmsHeader.getTimeTolive());

			// make response
			JMSResponse response = new JMSResponse("", null, request, timeStarted);
			attachResponseToRequest(submitContext, request, response);
			return response;
		}
		catch (Throwable jmse)
		{
			SoapUI.logError(jmse);
		}
		finally
		{
			// close session and connection
			if (session != null)
				session.close();
			if (connection != null)
				connection.close();
		}
		return null;
	}

}
