package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import hermes.Hermes;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public class HermesJmsRequestSendTransport extends HermesJmsRequestTransport
{

	public Response execute(SubmitContext submitContext, Request request) throws Exception
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
				sessionName = parameters[0];
				queueName = parameters[1];
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
			Queue queue = session.createQueue(queueName);

			// producer
			MessageProducer messageProducer = session.createProducer(queue);

			// message
			TextMessage textMessage = session.createTextMessage();
			textMessage.setText(request.getRequestContent());

			// send message
			messageProducer.send(textMessage);

			// make response
			JMSResponse response = new JMSResponse(textMessage.getText(), textMessage, request);
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
