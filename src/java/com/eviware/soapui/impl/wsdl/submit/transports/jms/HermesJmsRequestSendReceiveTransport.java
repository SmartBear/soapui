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
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;

public class HermesJmsRequestSendReceiveTransport extends HermesJmsRequestTransport
{

	public Response execute(SubmitContext submitContext, Request request, long timeStarted) throws Exception
	{
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		try
		{
			String queueNameSend = null;
			String queueNameReceive = null;
			String sessionName = null;
			String[] parameters = request.getEndpoint().substring(request.getEndpoint().indexOf("://") + 3).split("/");
			if (parameters.length == 3)
			{
				sessionName = PropertyExpander.expandProperties(submitContext,parameters[0]);
				queueNameSend = PropertyExpander.expandProperties(submitContext,parameters[1]).replaceFirst("queue_", "");
				queueNameReceive = PropertyExpander.expandProperties(submitContext,parameters[2]).replaceFirst("queue_", "");
			}
			else
				throw new Exception("bad jms alias!!!!!");
			Hermes hermes = getHermes(sessionName, request);
			// connection factory
			connectionFactory = (javax.jms.ConnectionFactory) hermes.getConnectionFactory();

			// connection
			connection = connectionFactory.createConnection();
			connection.start();

			// session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// queue
			Queue queueSend = (Queue) hermes.getDestination(queueNameSend, Domain.QUEUE);
			Queue queueReceive = (Queue) hermes.getDestination(queueNameReceive, Domain.QUEUE);

			// producer from session with queue
			MessageProducer messageProducer = session.createProducer(queueSend);

			// message
			TextMessage textMessageSend = session.createTextMessage();
			String messageBody = PropertyExpander.expandProperties(submitContext,request.getRequestContent());
			textMessageSend.setText(messageBody);
        
			JMSHeader jmsHeader= new JMSHeader();
         jmsHeader.setMessageHeaders(textMessageSend, request, hermes);
         JMSHeader.setMessageProperties(textMessageSend, request, hermes);
			
         // send message to producer
			messageProducer.send(textMessageSend, 
										textMessageSend.getJMSDeliveryMode(), 
										textMessageSend.getJMSPriority(),
										jmsHeader.getTimeTolive());

			// consumer from session with queue
			MessageConsumer messageConsumer = session.createConsumer(queueReceive);

			long timeout = getTimeout(submitContext, request);

			Message message = messageConsumer.receive(timeout);

			if (message != null)
			{
				TextMessage textMessageReceive = null;
				if (message instanceof TextMessage)
				{
					textMessageReceive = (TextMessage) message;
				}
				// make response
				JMSResponse response = new JMSResponse(textMessageReceive.getText(), textMessageReceive, request, timeStarted);
				
				attachResponseToRequest(submitContext, request, response);
				
				return response;
			}
			else
			{
				return new JMSResponse("", null, request, timeStarted);
			}
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
