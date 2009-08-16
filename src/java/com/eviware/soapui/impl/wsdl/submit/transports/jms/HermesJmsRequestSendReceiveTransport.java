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

import org.apache.log4j.Logger;

import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public class HermesJmsRequestSendReceiveTransport extends AbstractHermesJmsRequestTransport
{

	private final static Logger log = Logger.getLogger(HermesJmsRequestSendReceiveTransport.class);

	public Response sendRequest(SubmitContext submitContext, Request request) throws Exception
	{
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		// at this point we should load hermes beans !!!!!!!!!!
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
			Hermes hermes = getHermes(sessionName);
			// connection factory
			connectionFactory = (javax.jms.ConnectionFactory) hermes.getConnectionFactory();

			// connection
			connection = connectionFactory.createConnection();
			connection.start();

			// session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// queue
			Queue queueSend;
			Queue queueReceive;
		

			queueSend = session.createQueue(queueNameSend);
			queueReceive = session.createQueue(queueNameReceive);
			
			// producer from session with queue
			MessageProducer messageProducer = session.createProducer(queueSend);

			// message
			TextMessage textMessageSend = session.createTextMessage();
			textMessageSend.setText(request.getRequestContent());
			log.info("Sending Message: \n" + textMessageSend.getText());

			// send message to producer
			messageProducer.send(textMessageSend);

			// consumer from session with queue
			MessageConsumer messageConsumer = session.createConsumer(queueReceive);
			Message message = messageConsumer.receive();

			TextMessage textMessageReceive = null;
			// print out received message
			if (message instanceof TextMessage)
			{
				textMessageReceive = (TextMessage) message;
				System.out.println("Read Message: " + textMessageReceive.getText());

			}
			
			// make response
			JMSResponse response = new JMSResponse(textMessageReceive.getText(), textMessageReceive, request);
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
