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

package com.eviware.soapui.impl.wsdl.support.wsrm;

import com.eviware.soapui.config.WsrmConfigConfig;
import com.eviware.soapui.config.WsrmVersionTypeConfig;
import com.eviware.soapui.support.PropertyChangeNotifier;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigInteger;

public class WsrmConfig implements PropertyChangeNotifier
{

	private WsrmConfigConfig wsrmConfig;
	private String sequenceIdentifier;
	private Long lastMessageId;
	private String uuid;

	private PropertyChangeSupport propertyChangeSupport;

	private final WsrmContainer container;

	public WsrmConfig( WsrmConfigConfig wsrmConfig, WsrmContainer container )
	{
		this.setWsrmConfig( wsrmConfig );
		this.container = container;
		this.setPropertyChangeSupport( new PropertyChangeSupport( this ) );
		lastMessageId = 1l;

		if( !wsrmConfig.isSetVersion() )
		{
			wsrmConfig.setVersion( WsrmVersionTypeConfig.X_1_2 );
		}
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( listener );
	}

	public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( propertyName, listener );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
	}

	public void setWsrmConfig( WsrmConfigConfig wsrmConfig )
	{
		this.wsrmConfig = wsrmConfig;
	}

	public WsrmConfigConfig getWsrmConfig()
	{
		return wsrmConfig;
	}

	public void setPropertyChangeSupport( PropertyChangeSupport propertyChangeSupport )
	{
		this.propertyChangeSupport = propertyChangeSupport;
	}

	public PropertyChangeSupport getPropertyChangeSupport()
	{
		return propertyChangeSupport;
	}

	public WsrmContainer getContainer()
	{
		return container;
	}

	public void setAckTo( String newAckTo )
	{
		String oldValue = wsrmConfig.getAckTo();
		wsrmConfig.setAckTo( newAckTo );
		propertyChangeSupport.firePropertyChange( "ackTo", oldValue, newAckTo );
	}

	public String getAckTo()
	{
		return wsrmConfig.getAckTo();
	}

	public String getOfferEndpoint()
	{
		return wsrmConfig.getOfferEndpoint();
	}

	public void setSequenceExpires( BigInteger newTimeout )
	{
		BigInteger oldValue = wsrmConfig.getSequenceExpires();
		wsrmConfig.setSequenceExpires( newTimeout );
		propertyChangeSupport.firePropertyChange( "sequenceExpires", oldValue, newTimeout );
	}

	public void setOfferEndpoint(String endpointUri)
	{
		String oldValue = wsrmConfig.getOfferEndpoint();
		wsrmConfig.setOfferEndpoint( endpointUri );
		propertyChangeSupport.firePropertyChange( "offerEndpoint", oldValue, endpointUri );
	}

	public BigInteger getSequenceExpires()
	{
		return wsrmConfig.getSequenceExpires();
	}

	public void setWsrmEnabled( boolean enable )
	{
		boolean oldValue = isWsrmEnabled();
		container.setWsrmEnabled( enable );
		propertyChangeSupport.firePropertyChange( "wsrmEnabled", oldValue, enable );
	}

	public boolean isWsrmEnabled()
	{
		return container.isWsrmEnabled();
	}

	public void setVersion( String arg0 )
	{
		String oldValue = getVersion();
		wsrmConfig.setVersion( WsrmVersionTypeConfig.Enum.forString( arg0 ) );
		propertyChangeSupport.firePropertyChange( "version", oldValue, arg0 );
	}

	public String getVersion()
	{
		return wsrmConfig.getVersion().toString();
	}

	public void setSequenceIdentifier( String sequenceIdentifier )
	{
		this.sequenceIdentifier = sequenceIdentifier;
	}

	public String getSequenceIdentifier()
	{
		return sequenceIdentifier;
	}

	public Long nextMessageId()
	{
		this.lastMessageId++ ;
		return lastMessageId;
	}

	public Long getLastMessageId()
	{
		return lastMessageId;
	}

	public void setLastMessageId( long msgId )
	{
		lastMessageId = msgId;
	}

	public void setUuid( String uuid )
	{
		this.uuid = uuid;
	}

	public String getUuid()
	{
		return uuid;
	}

	public String getVersionNameSpace()
	{
		return WsrmUtils.getWsrmVersionNamespace( wsrmConfig.getVersion() );
	}
}
