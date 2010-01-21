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
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.NamingException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.StringUtils;

public class JMSConnectionHolder
{
	private TopicConnectionFactory topicConnectionFactory = null;
	private TopicConnection topicConnection = null;
	private TopicSession topicSession = null;

	private QueueConnectionFactory queueConnectionFactory = null;
	private QueueConnection queueConnection = null;
	QueueSession queueSession = null;

	private JMSEndpoint jmsEndpoint;
	private Hermes hermes;
	private String clientID;

	public JMSConnectionHolder( JMSEndpoint jmsEndpoint, Hermes hermes, boolean createQueueConnection,
			boolean createTopicConnection, String clientID, String username, String password ) throws JMSException
	{
		try
		{
			this.jmsEndpoint = jmsEndpoint;
			this.hermes= hermes;
			this.clientID = clientID ;

			if( createTopicConnection )
			{
				topicConnectionFactory = ( TopicConnectionFactory )hermes.getConnectionFactory();
				topicConnection = ( TopicConnection )createConnection( topicConnectionFactory, Domain.TOPIC, clientID ,username, password);
				topicConnection.start();
			}

			if( createQueueConnection )
			{
				queueConnectionFactory = ( QueueConnectionFactory )hermes.getConnectionFactory();
				queueConnection = ( QueueConnection )createConnection( queueConnectionFactory, Domain.QUEUE, clientID , username, password);
				queueConnection.start();
			}

		}
		catch( Throwable t )
		{
			SoapUI.logError( t );
		}
	}

	private Connection createConnection( ConnectionFactory connectionFactory, Domain domain, String clientId,
			String username, String password ) throws JMSException
	{
		QueueConnection queueConnection;
		TopicConnection topicConnection;

		

		if( domain.equals( Domain.TOPIC ) )
		{
			topicConnection = StringUtils.hasContent( username ) ? ( ( TopicConnectionFactory )connectionFactory )
					.createTopicConnection( username, password ) : ( ( TopicConnectionFactory )connectionFactory )
					.createTopicConnection();

			if( !StringUtils.isNullOrEmpty( clientId ) )
				topicConnection.setClientID( clientId );

			return topicConnection;
		}
		else if( domain.equals( Domain.QUEUE ) )
		{
			queueConnection = StringUtils.hasContent( username ) ? ( ( QueueConnectionFactory )connectionFactory )
					.createQueueConnection( username, password ) : ( ( QueueConnectionFactory )connectionFactory )
					.createQueueConnection();

			if( !StringUtils.isNullOrEmpty( clientId ) )
				queueConnection.setClientID( clientId );

			return queueConnection;
		}
		else
		{
			return null;
		}
	}


	public TopicConnectionFactory getTopicConnectionFactory()
	{
		return topicConnectionFactory;
	}

	public TopicConnection getTopicConnection()
	{
		return topicConnection;
	}

	public QueueConnectionFactory getQueueConnectionFactory()
	{
		return queueConnectionFactory;
	}

	public QueueConnection getQueueConnection()
	{
		return queueConnection;
	}

	public String getClientID()
	{
		return clientID;
	}

	public Hermes getHermes()

	{
		return hermes;
	}

	public JMSEndpoint getJmsEndpoint()
	{
		return jmsEndpoint;
	}

	

	public Topic getTopic( String name ) throws JMSException, NamingException
	{
		return ( Topic )getHermes().getDestination( name, Domain.TOPIC );
	}

	public Queue getQueue( String name ) throws JMSException, NamingException
	{
		return ( Queue )getHermes().getDestination( name, Domain.QUEUE );
	}

	public QueueSession getQueueSession() throws JMSException
	{
		if( queueSession == null )
		{
			return queueSession = getQueueConnection().createQueueSession( false, Session.AUTO_ACKNOWLEDGE );
		}
		return queueSession;
	}
	
	public TopicSession getTopicSession() throws JMSException
	{
		if( topicSession == null )
		{
			return topicSession = getTopicConnection().createTopicSession( false, Session.AUTO_ACKNOWLEDGE );
		}
		return topicSession;
	}

}
