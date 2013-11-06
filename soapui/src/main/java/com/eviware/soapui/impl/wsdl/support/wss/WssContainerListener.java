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

// FIXME Consider break this interface in sub interfaces as many implementing classes only use cryptoUpdated 
public interface WssContainerListener
{
	public void outgoingWssEntryAdded( WssEntry entry );

	public void outgoingWssEntryRemoved( WssEntry entry );

	public void outgoingWssEntryMoved( WssEntry entry, int offset );

	public void cryptoAdded( WssCrypto crypto );

	public void cryptoRemoved( WssCrypto crypto );

	public void cryptoUpdated( WssCrypto crypto );

	public void incomingWssAdded( IncomingWss incomingWss );

	public void incomingWssRemoved( IncomingWss incomingWss );

	public void outgoingWssAdded( OutgoingWss outgoingWss );

	public void outgoingWssRemoved( OutgoingWss outgoingWss );

}
