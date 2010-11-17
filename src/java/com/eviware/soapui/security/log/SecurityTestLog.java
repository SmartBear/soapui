/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

import org.apache.commons.collections.list.TreeList;

import com.eviware.soapui.security.SecurityTest;

/**
 * SecurityTestLog
 * 
 * @author soapUI team
 */
public class SecurityTestLog extends AbstractListModel
{
//	private List<SecurityTestLogMessageEntry> logEntries;
	private List<Object> items = Collections.synchronizedList( new TreeList() );
	private int maxSize = 0;

	public SecurityTestLog()
	{
//		logEntries = new ArrayList<SecurityTestLogMessageEntry>();
	}

	@Override
	public Object getElementAt( int arg0 )
	{
		return items.get( arg0 );
	}

	@Override
	public int getSize()
	{
		return items.size();
	}

	public void addEntry( SecurityTestLogMessageEntry securityTestLogMessageEntry )
	{
		items.add( securityTestLogMessageEntry.getMessage() );
		fireIntervalAdded( this, items.size() - 1, items.size() - 1 );
	}
	public synchronized void clear()
	{
		items.clear();
	}
	public int getMaxSize()
	{
		return maxSize;
	}

	public void setMaxSize( int maxSize )
	{
		this.maxSize = maxSize;
		enforceMaxSize();
	}
	private synchronized void enforceMaxSize()
	{
		while( items.size() > maxSize )
		{
			items.remove( 0 );
//			results.remove( 0 );
			fireIntervalRemoved( this, 0, 0 );
		}
	}



}
