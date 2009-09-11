package com.eviware.soapui.impl.wsdl.submit.transports.jms;

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

public class HermesJmsRequestSendReceiveTransport extends HermesJmsRequestTransport
{

	public Response execute(SubmitContext submitContext, Request request) throws Exception
	{
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		try
		{
			String queueNameSend = null;
			String queueNameReceive = null;
			String sessionName=null;
			String[] parameters = request.getEndpoint().substring(request.getEndpoint().indexOf("://") + 3).split("/");
			if (parameters.length == 3)
			{
				sessionName=parameters[0];
				queueNameSend = parameters[1];
				queueNameReceive=parameters[2];
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
			Queue queueSend = session.createQueue(queueNameSend);
			Queue queueReceive = session.createQueue(queueNameReceive);
			
			// producer from session with queue
			MessageProducer messageProducer = session.createProducer(queueSend);

			// message
			TextMessage textMessageSend = session.createTextMessage();
			textMessageSend.setText(request.getRequestContent());

			// send message to producer
			messageProducer.send(textMessageSend);

			// consumer from session with queue
			MessageConsumer messageConsumer = session.createConsumer(queueReceive);
			Message message = messageConsumer.receive();
			// TODO: IMPROVEMENT messageConsumer.receive(long timeout) - we can set how much time to wait for message to receive 

			TextMessage textMessageReceive = null;
			// print out received message
			if (message instanceof TextMessage)
			{
				textMessageReceive = (TextMessage) message;
			}
			
			// make response
			JMSResponse response = new JMSResponse(textMessageReceive.getText(), textMessageReceive, request);
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
