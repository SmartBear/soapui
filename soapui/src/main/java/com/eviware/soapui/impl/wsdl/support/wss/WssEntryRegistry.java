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

import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.entries.EncryptionEntry;
import com.eviware.soapui.impl.wsdl.support.wss.entries.AutomaticSAMLEntry;
import com.eviware.soapui.impl.wsdl.support.wss.entries.ManualSAMLEntry;
import com.eviware.soapui.impl.wsdl.support.wss.entries.SignatureEntry;
import com.eviware.soapui.impl.wsdl.support.wss.entries.TimestampEntry;
import com.eviware.soapui.impl.wsdl.support.wss.entries.UsernameEntry;
import com.eviware.soapui.support.registry.AbstractRegistry;

public class WssEntryRegistry extends AbstractRegistry<WssEntry, WSSEntryConfig, OutgoingWss>
{
	private static WssEntryRegistry instance;

	public WssEntryRegistry()
	{
		mapType( UsernameEntry.TYPE, UsernameEntry.class );
		mapType( TimestampEntry.TYPE, TimestampEntry.class );
		mapType( ManualSAMLEntry.TYPE, ManualSAMLEntry.class );
		mapType( AutomaticSAMLEntry.TYPE, AutomaticSAMLEntry.class );
		mapType( SignatureEntry.TYPE, SignatureEntry.class );
		mapType( EncryptionEntry.TYPE, EncryptionEntry.class );
	}

	public static synchronized WssEntryRegistry get()
	{
		if( instance == null )
			instance = new WssEntryRegistry();

		return instance;
	}

	@Override
	protected WSSEntryConfig addNewConfig( OutgoingWss container )
	{
		return container.getConfig().addNewEntry();
	}
}
