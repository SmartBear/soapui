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

package com.eviware.soapui.impl.wsdl.support.wss;

import java.util.List;

import com.eviware.soapui.impl.wsdl.support.wss.crypto.CryptoType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;

import javax.annotation.Nonnull;

public interface WssContainer extends PropertyExpansionContainer
{
	public ModelItem getModelItem();

	public void addWssContainerListener( WssContainerListener listener );

	public void removeWssContainerListener( WssContainerListener listener );

	public List<WssCrypto> getCryptoList();

	public WssCrypto addCrypto( String source, String password, @Nonnull CryptoType type );

	public int getCryptoCount();

	public void removeCrypto( @Nonnull WssCrypto crypto );

	public List<IncomingWss> getIncomingWssList();

	public IncomingWss addIncomingWss( String label );

	public int getIncomingWssCount();

	public IncomingWss getIncomingWssAt( int index );

	public void removeIncomingWssAt( int row );

	public List<OutgoingWss> getOutgoingWssList();

	public OutgoingWss addOutgoingWss( String label );

	public int getOutgoingWssCount();

	public OutgoingWss getOutgoingWssAt( int index );

	public void removeOutgoingWssAt( int row );

	public WssCrypto getCryptoByName( String cryptoName );

	public WssCrypto getCryptoByName( String cryptoName, boolean outgoingWSSConfig );

	public OutgoingWss getOutgoingWssByName( String outgoingName );

	public IncomingWss getIncomingWssByName( String incomingName );

	public String[] getCryptoNames();

	public String[] getOutgoingWssNames();

	public String[] getIncomingWssNames();

	public void importConfig( WssContainer wssContainer );
}
