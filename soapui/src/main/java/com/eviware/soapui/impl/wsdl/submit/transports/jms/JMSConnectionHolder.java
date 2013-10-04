/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
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
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.NamingException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.StringUtils;

/**
 * class that holds jms connections and sessions
 * 
 * @author nebojsa.tasic
 * 
 */
public class JMSConnectionHolder
{
	private ConnectionFactory connectionFactory = null;
	private Connection connection = null;
	private Session session = null;

	private JMSEndpoint jmsEndpoint;
	private Hermes hermes;
	private String clientID;

	/**
	 * 
	 * @param jmsEndpoint
	 * @param hermes
	 * @param createQueueConnection
	 * @param createTopicConnection
	 * @param clientID
	 * @param username
	 * @param password
	 * @throws JMSException
	 */
	public JMSConnectionHolder( JMSEndpoint jmsEndpoint, Hermes hermes, boolean isTopicDomain, String clientID,
			String username, String password ) throws JMSException
	{
		try
		{
			this.jmsEndpoint = jmsEndpoint;
			this.hermes = hermes;
			this.clientID = clientID;

			connectionFactory = ( ConnectionFactory )hermes.getConnectionFactory();
			connection = createConnection( connectionFactory, isTopicDomain ? Domain.TOPIC : Domain.QUEUE, clientID,
					username, password );
			connection.start();

		}
		catch( Throwable t )
		{
			SoapUI.logError( t );

			if( connection != null )
				connection.close();

			throw new JMSException( t.getMessage() );

		}
	}

	private Connection createConnection( ConnectionFactory connectionFactory, Domain domain, String clientId,
			String username, String password ) throws JMSException
	{
		Connection connection = StringUtils.hasContent( username ) ? ( ( ConnectionFactory )connectionFactory )
				.createConnection( username, password ) : ( ( ConnectionFactory )connectionFactory ).createConnection();

		if( !StringUtils.isNullOrEmpty( clientId ) && domain.equals( Domain.TOPIC ) )
			connection.setClientID( clientId );

		return connection;

	}

	public ConnectionFactory getConnectionFactory()
	{
		return connectionFactory;
	}

	public Connection getConnection()
	{
		return connection;
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

	/**
	 * return topic by name
	 * 
	 * @return Queue
	 * @throws JMSException
	 *            , NamingException
	 */
	public Topic getTopic( String name ) throws JMSException, NamingException
	{
		if( name == null || name.isEmpty() )
		{
			return getSession().createTemporaryTopic();
		}
		else
		{
		return ( Topic )getHermes().getDestination( name, Domain.TOPIC );
	}
	}

	/**
	 * return queue by name
	 * 
	 * @return Queue
	 * @throws JMSException
	 *            , NamingException
	 */
	public Queue getQueue( String name ) throws JMSException, NamingException
	{
		if( name == null || name.isEmpty() )
		{
			return getSession().createTemporaryQueue();
		}
		else
		{
		return ( Queue )getHermes().getDestination( name, Domain.QUEUE );
	}
	}

	/**
	 * 
	 * @return Session
	 * @throws JMSException
	 */
	public Session getSession() throws JMSException
	{
		if( session == null )
		{
			return session = getConnection().createSession( false, Session.AUTO_ACKNOWLEDGE );
		}
		return session;
	}

	/**
	 * closes sessions and connections
	 */
	public void closeAll()
	{
		try
		{
			if( session != null )
				session.close();

			if( connection != null )
			{
				connection.close();
				connection = null;
			}
		}
		catch( JMSException e )
		{
			SoapUI.logError( e );
		}
	}

}
