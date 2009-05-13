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

package com.eviware.soapui.impl.wsdl.support.wss.entries;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;

import org.apache.ws.security.WSEncryptionPart;

import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.support.wss.WssEntry;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;

public abstract class WssEntryBase implements WssEntry, PropertyExpansionContainer
{
	private WSSEntryConfig config;
	private OutgoingWss outgoingWss;
	private JComponent configComponent;
	private String label;

	public void init( WSSEntryConfig config, OutgoingWss outgoingWss, String label )
	{
		this.config = config;
		this.outgoingWss = outgoingWss;
		this.label = label;

		if( config.getConfiguration() == null )
			config.addNewConfiguration();

		load( new XmlObjectConfigurationReader( config.getConfiguration() ) );
	}

	public OutgoingWss getOutgoingWss()
	{
		return outgoingWss;
	}

	public String getPassword()
	{
		String password = config.getPassword();
		if( StringUtils.isNullOrEmpty( password ) )
			password = outgoingWss.getPassword();

		return password;
	}

	public String getUsername()
	{
		String username = config.getUsername();
		if( StringUtils.isNullOrEmpty( username ) )
			username = outgoingWss.getUsername();

		return username;
	}

	public void setPassword( String arg0 )
	{
		config.setPassword( arg0 );
	}

	public void setUsername( String arg0 )
	{
		config.setUsername( arg0 );
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

	public void setConfig( WSSEntryConfig config )
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

	public WssContainer getWssContainer()
	{
		return outgoingWss.getWssContainer();
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
	}

	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		PropertyExpansionsResult result = new PropertyExpansionsResult( getWssContainer().getModelItem(), this );

		addPropertyExpansions( result );

		return result.toArray();
	}

	protected void addPropertyExpansions( PropertyExpansionsResult result )
	{
		if( StringUtils.hasContent( config.getUsername() ) )
			result.extractAndAddAll( "username" );

		if( StringUtils.hasContent( config.getPassword() ) )
			result.extractAndAddAll( "password" );
	}

	public void udpateConfig( WSSEntryConfig config )
	{
		this.config = config;
	}

	@Override
	public String toString()
	{
		return getLabel();
	}

	protected List<StringToStringMap> readParts( XmlObjectConfigurationReader reader, String parameterName )
	{
		List<StringToStringMap> result = new ArrayList<StringToStringMap>();
		String[] parts = reader.readStrings( parameterName );
		if( parts != null && parts.length > 0 )
		{
			for( String part : parts )
			{
				result.add( StringToStringMap.fromXml( part ) );
			}
		}

		return result;
	}

	protected Vector<WSEncryptionPart> createWSParts( List<StringToStringMap> parts )
	{
		Vector<WSEncryptionPart> result = new Vector<WSEncryptionPart>();

		for( StringToStringMap map : parts )
		{
			if( map.hasValue( "id" ) )
			{
				result.add( new WSEncryptionPart( map.get( "id" ), map.get( "enc" ) ) );
			}
			else
			{
				String ns = map.get( "namespace" );
				if( ns == null )
					ns = "";

				String name = map.get( "name" );
				if( StringUtils.hasContent( name ) )
				{
					result.add( new WSEncryptionPart( name, ns, map.get( "enc" ) ) );
				}
			}
		}

		return result;
	}

	protected void saveParts( XmlObjectConfigurationBuilder builder, List<StringToStringMap> parts, String string )
	{
		for( StringToStringMap part : parts )
		{
			builder.add( string, part.toXml() );
		}
	}

	protected class KeyIdentifierTypeRenderer extends DefaultListCellRenderer
	{
		@Override
		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus )
		{
			Component result = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

			if( value.equals( 0 ) )
				setText( "<none>" );
			else if( value.equals( 1 ) )
				setText( "Binary Security Token" );
			else if( value.equals( 2 ) )
				setText( "Issuer Name and Serial Number" );
			else if( value.equals( 3 ) )
				setText( "X509 Certificate" );
			else if( value.equals( 4 ) )
				setText( "Subject Key Identifier" );
			else if( value.equals( 5 ) )
				setText( "Embedded KeyInfo" );
			else if( value.equals( 6 ) )
				setText( "Embed SecurityToken Reference" );
			else if( value.equals( 7 ) )
				setText( "UsernameToken Signature" );
			else if( value.equals( 8 ) )
				setText( "Thumbprint SHA1 Identifier" );
			else if( value.equals( 9 ) )
				setText( "Custom Reference" );

			return result;
		}
	}

	protected class KeyAliasComboBoxModel extends AbstractListModel implements ComboBoxModel
	{
		private KeyStore keyStore;
		private Object alias;
		private StringList aliases = new StringList();

		public KeyAliasComboBoxModel( WssCrypto crypto )
		{
			update( crypto );
		}

		void update( WssCrypto crypto )
		{
			keyStore = crypto == null || crypto.getCrypto() == null ? null : crypto.getCrypto().getKeyStore();

			if( keyStore != null )
			{
				if( !aliases.isEmpty() )
				{
					int sz = aliases.size();
					aliases.clear();
					fireIntervalRemoved( this, 0, sz - 1 );
				}

				try
				{
					for( Enumeration e = keyStore.aliases(); e.hasMoreElements(); )
					{
						aliases.add( e.nextElement().toString() );
					}

					fireIntervalAdded( this, 0, aliases.size() - 1 );
				}
				catch( KeyStoreException e )
				{
					e.printStackTrace();
				}
			}
		}

		public Object getSelectedItem()
		{
			return alias;
		}

		public void setSelectedItem( Object anItem )
		{
			this.alias = anItem;
		}

		public Object getElementAt( int index )
		{
			return aliases.get( index );
		}

		public int getSize()
		{
			return aliases.size();
		}
	}

	public void release()
	{
	}
}
