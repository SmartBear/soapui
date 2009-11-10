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
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public class HermesJmsRequestSendReceiveTransport extends HermesJmsRequestTransport
{

	public Response execute(SubmitContext submitContext, Request request, long timeStarted) throws Exception
	{
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		try
		{
			String[] parameters = extractEndpointParameters(request);
			String sessionName = getEndpointParameter(parameters, 0, null, submitContext);
			String queueNameSend = getEndpointParameter(parameters, 1, Domain.QUEUE, submitContext);
			String queueNameReceive = getEndpointParameter(parameters, 2, Domain.QUEUE, submitContext);
			
			submitContext.setProperty(HERMES_SESSION_NAME, sessionName);
			
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

			Message messageSend = messageSend(submitContext, request, session, hermes, queueSend);
			
			MessageConsumer messageConsumer = session.createConsumer(queueReceive);

			return makeResponse(submitContext, request, timeStarted, messageSend, messageConsumer);
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
