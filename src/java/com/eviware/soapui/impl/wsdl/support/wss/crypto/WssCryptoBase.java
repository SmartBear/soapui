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

package com.eviware.soapui.impl.wsdl.support.wss.crypto;

import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import com.eviware.soapui.config.WSSCryptoConfig;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;

public abstract class WssCryptoBase implements WssCrypto, PropertyExpansionContainer
{
	private WSSCryptoConfig config;
	private WssContainer container;
	private JComponent configComponent;
	private String label;

	public void init( WSSCryptoConfig config, WssContainer container, String label )
	{
		this.config = config;
		this.container = container;
		this.label = label;

		if( config.getConfiguration() == null )
			config.addNewConfiguration();

		load( new XmlObjectConfigurationReader( config.getConfiguration() ) );
	}

	public JComponent getConfigurationPanel()
	{
		if( configComponent == null )
			configComponent = buildUI();

		return configComponent;
	}

	public String getLabel()
	{
		return label;
	}

	protected abstract JComponent buildUI();

	protected abstract void load( XmlObjectConfigurationReader reader );

	public void setConfig( WSSCryptoConfig config )
	{
		this.config = config;
	}

	public void saveConfig()
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		save( builder );
		config.getConfiguration().set( builder.finish() );
	}

	protected abstract void save( XmlObjectConfigurationBuilder builder );

	public WssContainer getContainer()
	{
		return container;
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
	}

	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		PropertyExpansionsResult result = new PropertyExpansionsResult( getContainer().getModelItem(), this );

		addPropertyExpansions( result );

		return result.toArray();
	}

	protected void addPropertyExpansions( PropertyExpansionsResult result )
	{
		result.extractAndAddAll( "username" );
		result.extractAndAddAll( "password" );
	}

	@Override
	public String toString()
	{
		return getLabel();
	}

	public void udpateConfig( WSSCryptoConfig config )
	{
		this.config = config;
	}
}
