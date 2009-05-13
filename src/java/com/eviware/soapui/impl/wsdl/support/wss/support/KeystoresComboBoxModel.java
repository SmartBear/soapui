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

public class KeystoresComboBoxModel extends AbstractListModel implements ComboBoxModel, WssContainerListener
{
	private List<WssCrypto> cryptos = new ArrayList<WssCrypto>();
	private WssCrypto selectedCrypto;
	private final WssContainer container;

	public KeystoresComboBoxModel( WssContainer container, WssCrypto selectedCrypto )
	{
		this.container = container;
		this.selectedCrypto = selectedCrypto;

		cryptos.addAll( container.getCryptoList() );

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

	public void cryptoAdded( WssCrypto crypto )
	{
		cryptos.add( crypto );
		fireIntervalAdded( this, getSize() - 1, getSize() - 1 );
	}

	public void cryptoRemoved( WssCrypto crypto )
	{
		int index = cryptos.indexOf( crypto );
		cryptos.remove( index );
		fireIntervalRemoved( this, index, index );
	}

	public void incomingWssAdded( IncomingWss incomingWss )
	{
	}

	public void incomingWssRemoved( IncomingWss incomingWss )
	{
	}

	public void outgoingWssAdded( OutgoingWss outgoingWss )
	{
	}

	public void outgoingWssEntryAdded( WssEntry entry )
	{
	}

	public void outgoingWssEntryRemoved( WssEntry entry )
	{
	}

	public void outgoingWssRemoved( OutgoingWss outgoingWss )
	{
	}

	public void cryptoUpdated( WssCrypto crypto )
	{
	}

	public void release()
	{
		container.removeWssContainerListener( this );
		cryptos = null;
		selectedCrypto = null;
	}
}