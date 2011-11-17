/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.assertions.recent;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.settings.RecentAssertionSettings;
import com.eviware.soapui.support.types.StringList;

public class RecentAssertionHandler
{
	private BoundedQueue bq;

	public RecentAssertionHandler()
	{
		bq = new BoundedQueue();
	}

	public void add( String assertion )
	{
		bq.add( assertion );
		save();
	}

	public StringList get()
	{
		return load();
	}

	private void save()
	{
		StringList list = new StringList();
		list.addAll( this.bq.getByInsertionOrder() );
		SoapUI.getSettings().setString( RecentAssertionSettings.RECENT_ASSERTIONS, list.toXml() );
	}

	private StringList load()
	{
		StringList list = new StringList();
		String temp = SoapUI.getSettings().getString( RecentAssertionSettings.RECENT_ASSERTIONS, null );
		if( temp != null && temp.trim().length() > 0 )
		{
			try
			{
				StringList assertions = StringList.fromXml( temp );
				for( String assertion : assertions )
				{
					list.add( assertion );
				}
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}
		return list;
	}
}
