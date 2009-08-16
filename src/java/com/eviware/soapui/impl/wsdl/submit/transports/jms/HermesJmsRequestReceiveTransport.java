package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import hermes.Hermes;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public class HermesJmsRequestReceiveTransport extends AbstractHermesJmsRequestTransport
{

	private final static Logger log = Logger.getLogger(HermesJmsRequestReceiveTransport.class);

	public Response sendRequest(SubmitContext submitContext, Request request) throws Exception
	{
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		// at this point we should load hermes beans !!!!!!!!!!
		try
		{
			String queueName = null;
			String sessionName=null;
			String[] parameters = request.getEndpoint().substring(request.getEndpoint().indexOf("://") + 3).split("/");
			if (parameters.length == 3)
			{
				sessionName=parameters[0];
				queueName = parameters[2];
			}
			else
				throw new Exception("bad jms alias!!!!!");
			Hermes hermes = getHermes(sessionName);
			// connection factory
			connectionFactory = (javax.jms.ConnectionFactory) hermes.getConnectionFactory();

			// connection
			connection = connectionFactory.createConnection();
			connection.start();

			// session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// queue
			Queue queue;
			
			if (parameters.length == 3)
			{
				queueName = parameters[2];
			}
			else
				throw new Exception("bad jms alias!!!!!");

			queue = session.createQueue(queueName);

			// consumer from session with queue
			MessageConsumer messageConsumer = session.createConsumer(queue);
			Message message = messageConsumer.receive();

			TextMessage textMessage = null;
			// print out received message
			if (message instanceof TextMessage)
			{
				textMessage = (TextMessage) message;
				System.out.println("Read Message: " + textMessage.getText());

			}
		// make response
			JMSResponse response = new JMSResponse(textMessage.getText(), textMessage, request);
			return response;
		}
		catch (Throwable jmse)
		{
			log.info("Exception occurred : " + jmse.toString());

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
