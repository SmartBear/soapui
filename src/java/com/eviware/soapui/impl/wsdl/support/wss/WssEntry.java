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

import javax.swing.JComponent;

import org.apache.ws.security.message.WSSecHeader;
import org.w3c.dom.Document;

import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.registry.RegistryEntry;

public interface WssEntry extends RegistryEntry<WSSEntryConfig, OutgoingWss>
{
	public void process( WSSecHeader secHeader, Document doc, PropertyExpansionContext context );

	public JComponent getConfigurationPanel();

	public String getLabel();

	public OutgoingWss getOutgoingWss();

	public void udpateConfig( WSSEntryConfig config );

	public void release();
}
