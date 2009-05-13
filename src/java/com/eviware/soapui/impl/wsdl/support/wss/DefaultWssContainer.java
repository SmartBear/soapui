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

import java.security.Security;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.eviware.soapui.config.IncomingWssConfig;
import com.eviware.soapui.config.KeyMaterialCryptoConfig;
import com.eviware.soapui.config.OutgoingWssConfig;
import com.eviware.soapui.config.WssContainerConfig;
import com.eviware.soapui.impl.wsdl.support.wss.crypto.KeyMaterialWssCrypto;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.types.StringList;

public class DefaultWssContainer implements WssContainer
{
	private final ModelItem modelItem;
	private List<WssCrypto> cryptos = new ArrayList<WssCrypto>();
	private List<IncomingWss> incomingWssConfigs = new ArrayList<IncomingWss>();
	private List<OutgoingWss> outgoingWssConfigs = new ArrayList<OutgoingWss>();
	private final WssContainerConfig config;
	private Set<WssContainerListener> listeners = new HashSet<WssContainerListener>();

	static
	{
		Security.addProvider( new BouncyCastleProvider() );
	}

	public DefaultWssContainer( ModelItem modelItem, WssContainerConfig config )
	{
		this.modelItem = modelItem;
		this.config = config;

		for( KeyMaterialCryptoConfig cryptoConfig : config.getCryptoList() )
		{
			cryptos.add( new KeyMaterialWssCrypto( cryptoConfig, this ) );
		}

		for( IncomingWssConfig wssConfig : config.getIncomingList() )
		{
			incomingWssConfigs.add( new IncomingWss( wssConfig, this ) );
		}

		for( OutgoingWssConfig wssConfig : config.getOutgoingList() )
		{
			outgoingWssConfigs.add( new OutgoingWss( wssConfig, this ) );
		}
	}

	public ModelItem getModelItem()
	{
		return modelItem;
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		PropertyExpansionsResult result = new PropertyExpansionsResult( getModelItem(), this );

		for( OutgoingWss entry : outgoingWssConfigs )
		{
			result.addAll( entry.getPropertyExpansions() );
		}

		return result.toArray();
	}

	public List<WssCrypto> getCryptoList()
	{
		return new ArrayList<WssCrypto>( cryptos );
	}

	public WssCrypto addCrypto( String source, String password )
	{
		KeyMaterialWssCrypto result = new KeyMaterialWssCrypto( getConfig().addNewCrypto(), this, source, password );
		cryptos.add( result );

		fireCryptoAdded( result );

		return result;
	}

	protected void fireCryptoAdded( WssCrypto crypto )
	{
		for( WssContainerListener listener : listeners.toArray( new WssContainerListener[listeners.size()] ) )
		{
			listener.cryptoAdded( crypto );
		}
	}

	protected void fireCryptoRemoved( WssCrypto crypto )
	{
		for( WssContainerListener listener : listeners.toArray( new WssContainerListener[listeners.size()] ) )
		{
			listener.cryptoRemoved( crypto );
		}
	}

	public WssContainerConfig getConfig()
	{
		return config;
	}

	public int getCryptoCount()
	{
		return cryptos.size();
	}

	public WssCrypto getCryptoAt( int index )
	{
		return cryptos.get( index );
	}

	public void removeCryptoAt( int row )
	{
		WssCrypto crypto = cryptos.remove( row );
		fireCryptoRemoved( crypto );
		getConfig().removeCrypto( row );
	}

	public List<IncomingWss> getIncomingWssList()
	{
		return new ArrayList<IncomingWss>( incomingWssConfigs );
	}

	public IncomingWss addIncomingWss( String label )
	{
		IncomingWss incomingWss = new IncomingWss( getConfig().addNewIncoming(), this );
		incomingWss.setName( label );
		incomingWssConfigs.add( incomingWss );

		fireIncomingWssAdded( incomingWss );

		return incomingWss;
	}

	public int getIncomingWssCount()
	{
		return incomingWssConfigs.size();
	}

	public IncomingWss getIncomingWssAt( int index )
	{
		return incomingWssConfigs.get( index );
	}

	public void removeIncomingWssAt( int row )
	{
		IncomingWss incomingWss = incomingWssConfigs.remove( row );
		fireIncomingWssRemoved( incomingWss );
		getConfig().removeIncoming( row );
	}

	protected void fireIncomingWssAdded( IncomingWss incomingWss )
	{
		for( WssContainerListener listener : listeners.toArray( new WssContainerListener[listeners.size()] ) )
		{
			listener.incomingWssAdded( incomingWss );
		}
	}

	protected void fireIncomingWssRemoved( IncomingWss incomingWss )
	{
		for( WssContainerListener listener : listeners.toArray( new WssContainerListener[listeners.size()] ) )
		{
			listener.incomingWssRemoved( incomingWss );
		}
	}

	public List<OutgoingWss> getOutgoingWssList()
	{
		return new ArrayList<OutgoingWss>( outgoingWssConfigs );
	}

	public OutgoingWss addOutgoingWss( String label )
	{
		OutgoingWss result = new OutgoingWss( getConfig().addNewOutgoing(), this );
		result.setName( label );

		outgoingWssConfigs.add( result );

		fireOutgoingWssAdded( result );

		return result;
	}

	protected void fireOutgoingWssAdded( OutgoingWss result )
	{
		for( WssContainerListener listener : listeners.toArray( new WssContainerListener[listeners.size()] ) )
		{
			listener.outgoingWssAdded( result );
		}
	}

	protected void fireOutgoingWssRemoved( OutgoingWss result )
	{
		for( WssContainerListener listener : listeners.toArray( new WssContainerListener[listeners.size()] ) )
		{
			listener.outgoingWssRemoved( result );
		}
	}

	public int getOutgoingWssCount()
	{
		return outgoingWssConfigs.size();
	}

	public OutgoingWss getOutgoingWssAt( int index )
	{
		return outgoingWssConfigs.get( index );
	}

	public void removeOutgoingWssAt( int row )
	{
		OutgoingWss outgoingWss = outgoingWssConfigs.remove( row );
		fireOutgoingWssRemoved( outgoingWss );
		outgoingWss.release();
		getConfig().removeOutgoing( row );
	}

	public WssCrypto getCryptoByName( String cryptoName )
	{
		for( WssCrypto crypto : cryptos )
			if( crypto.getLabel().equals( cryptoName ) )
				return crypto;

		return null;
	}

	public IncomingWss getIncomingWssByName( String incomingName )
	{
		for( IncomingWss incomingWss : incomingWssConfigs )
			if( incomingWss.getName().equals( incomingName ) )
				return incomingWss;

		return null;
	}

	public OutgoingWss getOutgoingWssByName( String outgoingName )
	{
		for( OutgoingWss crypto : outgoingWssConfigs )
			if( crypto.getName().equals( outgoingName ) )
				return crypto;

		return null;
	}

	public void addWssContainerListener( WssContainerListener listener )
	{
		listeners.add( listener );
	}

	public void removeWssContainerListener( WssContainerListener listener )
	{
		listeners.remove( listener );
	}

	public void fireWssEntryAdded( WssEntry newEntry )
	{
		for( WssContainerListener listener : listeners.toArray( new WssContainerListener[listeners.size()] ) )
		{
			listener.outgoingWssEntryAdded( newEntry );
		}
	}

	public void fireWssEntryRemoved( WssEntry entry )
	{
		for( WssContainerListener listener : listeners.toArray( new WssContainerListener[listeners.size()] ) )
		{
			listener.outgoingWssEntryRemoved( entry );
		}
	}

	public String[] getCryptoNames()
	{
		StringList result = new StringList();

		for( WssCrypto crypto : getCryptoList() )
			result.add( crypto.getLabel() );

		return result.toStringArray();
	}

	public String[] getIncomingWssNames()
	{
		StringList result = new StringList();

		for( IncomingWss crypto : getIncomingWssList() )
			result.add( crypto.getName() );

		return result.toStringArray();
	}

	public String[] getOutgoingWssNames()
	{
		StringList result = new StringList();

		for( OutgoingWss crypto : getOutgoingWssList() )
			result.add( crypto.getName() );

		return result.toStringArray();
	}

	public void importConfig( WssContainer wssContainer )
	{
	}

	public void resetConfig( WssContainerConfig config )
	{
		getConfig().set( config );

		for( int c = 0; c < cryptos.size(); c++ )
		{
			( ( KeyMaterialWssCrypto )cryptos.get( c ) ).udpateConfig( getConfig().getCryptoArray( c ) );
		}

		for( int c = 0; c < incomingWssConfigs.size(); c++ )
		{
			incomingWssConfigs.get( c ).updateConfig( getConfig().getIncomingArray( c ) );
		}

		for( int c = 0; c < outgoingWssConfigs.size(); c++ )
		{
			outgoingWssConfigs.get( c ).updateConfig( getConfig().getOutgoingArray( c ) );
		}
	}

	public void fireCryptoUpdated( KeyMaterialWssCrypto crypto )
	{
		for( WssContainerListener listener : listeners.toArray( new WssContainerListener[listeners.size()] ) )
		{
			listener.cryptoUpdated( crypto );
		}
	}

	public void resolve( ResolveContext context )
	{
		for( int c = 0; c < cryptos.size(); c++ )
		{
			( ( KeyMaterialWssCrypto )cryptos.get( c ) ).resolve( context );
		}

		for( int c = 0; c < incomingWssConfigs.size(); c++ )
		{
			incomingWssConfigs.get( c ).resolve( context );
		}

		for( int c = 0; c < outgoingWssConfigs.size(); c++ )
		{
			outgoingWssConfigs.get( c ).resolve( context );
		}
	}
}
