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
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;

public class HermesJmsRequestSendTransport extends HermesJmsRequestTransport
{

	public Response execute(SubmitContext submitContext, Request request, long timeStarted) throws Exception
	{
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;

		try
		{
			String queueName = null;
			String sessionName = null;
			String[] parameters = request.getEndpoint().substring(request.getEndpoint().indexOf("://") + 3).split("/");
			if (parameters.length == 2)
			{
				sessionName = PropertyExpander.expandProperties(submitContext,parameters[0]);
				queueName = PropertyExpander.expandProperties(submitContext,parameters[1]).replaceFirst("queue_", "");
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
			Queue queue = (Queue)hermes.getDestination(queueName, Domain.QUEUE);

			// producer
			MessageProducer messageProducer = session.createProducer(queue);

			// message
			TextMessage textMessage = session.createTextMessage();
			String messageBody = PropertyExpander.expandProperties(submitContext,request.getRequestContent());
			textMessage.setText(messageBody);
		
			JMSHeader jmsHeader= new JMSHeader();
         jmsHeader.setMessageHeaders(textMessage, request, hermes);
         JMSHeader.setMessageProperties(textMessage, request, hermes);
         
         // send message to producer
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
