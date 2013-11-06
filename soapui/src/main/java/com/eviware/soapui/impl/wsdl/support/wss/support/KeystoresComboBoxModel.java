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

package com.eviware.soapui.impl.wsdl.support.wss.support;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainerListener;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.support.wss.WssEntry;
import com.eviware.soapui.impl.wsdl.support.wss.crypto.CryptoType;

public class KeystoresComboBoxModel extends AbstractListModel implements ComboBoxModel, WssContainerListener
{
	private List<WssCrypto> cryptos = new ArrayList<WssCrypto>();
	private WssCrypto selectedCrypto;
	private final WssContainer container;
	private final boolean outgoingConfig;

	public KeystoresComboBoxModel( WssContainer container, WssCrypto selectedCrypto, boolean outgoingWSSConfig )
	{
		this.container = container;
		this.selectedCrypto = selectedCrypto;
		this.outgoingConfig = outgoingWSSConfig;

		List<WssCrypto> currentCryptos = container.getCryptoList();

		// Only allow keystores for outgoing configuration
		if( outgoingWSSConfig )
		{
			for( WssCrypto currentCrypto : currentCryptos )
			{
				if( currentCrypto.getType() == CryptoType.KEYSTORE )
				{
					cryptos.add( currentCrypto );
				}
			}
		}
		else
		{
			cryptos.addAll( currentCryptos );
		}

		container.addWssContainerListener( this );
	}

	public String getSelectedItem()
	{
		return selectedCrypto == null ? null : selectedCrypto.getLabel();
	}

	public void setSelectedItem( Object anItem )
	{
		selectedCrypto = null;

		for( WssCrypto crypto : cryptos )
			if( crypto.getLabel().equals( anItem ) )
				selectedCrypto = crypto;
	}

	public Object getElementAt( int index )
	{
		return cryptos.get( index ).getLabel();
	}

	public int getSize()
	{
		return cryptos == null ? 0 : cryptos.size();
	}

	public void release()
	{
		container.removeWssContainerListener( this );
		cryptos = null;
		selectedCrypto = null;
	}

	@Override
	public void cryptoAdded( WssCrypto crypto )
	{
		// Only allow adding keystores if this is outgoing configuration
		if( !outgoingConfig || ( outgoingConfig && crypto.getType() == CryptoType.KEYSTORE ) )
		{
			cryptos.add( crypto );
			fireIntervalAdded( this, getSize() - 1, getSize() - 1 );
		}
	}

	@Override
	public void cryptoRemoved( WssCrypto crypto )
	{
		// Only allow removing keystores if this is outgoing configuration
		if( !outgoingConfig || ( outgoingConfig && crypto.getType() == CryptoType.KEYSTORE ) )
		{
			int index = cryptos.indexOf( crypto );
			cryptos.remove( index );
			fireIntervalRemoved( this, index, index );
		}
	}

	// FIXME Add adapter to remove this empty methods

	@Override
	public void outgoingWssAdded( OutgoingWss outgoingWss )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoingWssRemoved( OutgoingWss outgoingWss )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoingWssEntryAdded( WssEntry entry )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoingWssEntryMoved( WssEntry entry, int offset )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoingWssEntryRemoved( WssEntry entry )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void incomingWssAdded( IncomingWss incomingWss )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void incomingWssRemoved( IncomingWss incomingWss )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void cryptoUpdated( WssCrypto crypto )
	{
		// TODO Auto-generated method stub

	}
}
