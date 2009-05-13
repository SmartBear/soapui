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

import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.entries.AddEncryptionEntry;
import com.eviware.soapui.impl.wsdl.support.wss.entries.AddSAMLEntry;
import com.eviware.soapui.impl.wsdl.support.wss.entries.AddSignatureEntry;
import com.eviware.soapui.impl.wsdl.support.wss.entries.AddTimestampEntry;
import com.eviware.soapui.impl.wsdl.support.wss.entries.AddUsernameEntry;
import com.eviware.soapui.support.registry.AbstractRegistry;

public class WssEntryRegistry extends AbstractRegistry<WssEntry, WSSEntryConfig, OutgoingWss>
{
	private static WssEntryRegistry instance;

	public WssEntryRegistry()
	{
		mapType( AddUsernameEntry.TYPE, AddUsernameEntry.class );
		mapType( AddTimestampEntry.TYPE, AddTimestampEntry.class );
		mapType( AddSAMLEntry.TYPE, AddSAMLEntry.class );
		mapType( AddSignatureEntry.TYPE, AddSignatureEntry.class );
		mapType( AddEncryptionEntry.TYPE, AddEncryptionEntry.class );
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
