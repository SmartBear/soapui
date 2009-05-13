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

package com.eviware.soapui.impl.wsdl.support.wss;

import java.util.ArrayList;
import java.util.List;

import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.OutgoingWssConfig;
import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.resolver.ResolveContext;

public class OutgoingWss implements PropertyExpansionContainer
{
	public static final String WSSENTRY_PROPERTY = OutgoingWss.class.getName() + "@wssEntry";

	private OutgoingWssConfig config;
	private List<WssEntry> entries = new ArrayList<WssEntry>();
	private final DefaultWssContainer container;

	public OutgoingWss( OutgoingWssConfig config, DefaultWssContainer container )
	{
		this.config = config;
		this.container = container;

		for( WSSEntryConfig entryConfig : config.getEntryList() )
		{
			WssEntry entry = WssEntryRegistry.get().build( entryConfig, this );
			if( entry != null )
				entries.add( entry );
		}
	}

	public WssContainer getWssContainer()
	{
		return container;
	}

	public String getName()
	{
		return config.getName();
	}

	public String getPassword()
	{
		return config.getPassword();
	}

	public String getUsername()
	{
		return config.getUsername();
	}

	public void setName( String arg0 )
	{
		config.setName( arg0 );
	}

	public void setPassword( String arg0 )
	{
		config.setPassword( arg0 );
	}

	public void setUsername( String arg0 )
	{
		config.setUsername( arg0 );
	}

	public String getActor()
	{
		return config.getActor();
	}

	public boolean getMustUnderstand()
	{
		return config.getMustUnderstand();
	}

	public void setActor( String arg0 )
	{
		config.setActor( arg0 );
	}

	public void setMustUnderstand( boolean arg0 )
	{
		config.setMustUnderstand( arg0 );
	}

	public WssEntry addEntry( String type )
	{
		WssEntry newEntry = WssEntryRegistry.get().create( type, this );
		entries.add( newEntry );

		container.fireWssEntryAdded( newEntry );

		return newEntry;
	}

	public void removeEntry( WssEntry entry )
	{
		int index = entries.indexOf( entry );

		container.fireWssEntryRemoved( entries.remove( index ) );
		config.removeEntry( index );
		entry.release();
	}

	public OutgoingWssConfig getConfig()
	{
		return config;
	}

	public void processOutgoing( Document soapDocument, PropertyExpansionContext context )
	{
		Element header = WSSecurityUtil.findWsseSecurityHeaderBlock( soapDocument, soapDocument.getDocumentElement(),
				false );

		while( header != null )
		{
			header.getParentNode().removeChild( header );
			header = WSSecurityUtil.findWsseSecurityHeaderBlock( soapDocument, soapDocument.getDocumentElement(), false );
		}

		WSSecHeader secHeader = new WSSecHeader();

		if( StringUtils.hasContent( getActor() ) )
			secHeader.setActor( getActor() );

		secHeader.setMustUnderstand( getMustUnderstand() );

		secHeader.insertSecurityHeader( soapDocument );

		for( WssEntry entry : entries )
		{
			try
			{
				entry.process( secHeader, soapDocument, context );
			}
			catch( Throwable e )
			{
				SoapUI.logError( e );
			}
		}
	}

	public List<WssEntry> getEntries()
	{
		return entries;
	}

	public void updateConfig( OutgoingWssConfig config )
	{
		this.config = config;

		for( int c = 0; c < entries.size(); c++ )
		{
			entries.get( c ).udpateConfig( this.config.getEntryArray( c ) );
		}
	}

	public void release()
	{
		for( WssEntry entry : entries )
			entry.release();
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		PropertyExpansionsResult result = new PropertyExpansionsResult( getWssContainer().getModelItem(), this );

		result.extractAndAddAll( "username" );
		result.extractAndAddAll( "password" );

		for( WssEntry entry : entries )
		{
			if( entry instanceof PropertyExpansionContainer )
				result.addAll( ( ( PropertyExpansionContainer )entry ).getPropertyExpansions() );
		}

		return result.toArray();
	}

	public void resolve( ResolveContext context )
	{
	}
}
