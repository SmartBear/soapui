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

package com.eviware.soapui.security.log;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

import org.apache.commons.collections.list.TreeList;

import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.security.SecurityCheckRequestResult;
import com.eviware.soapui.security.SecurityCheckResult;

/**
 * SecurityTestLog
 * 
 * @author soapUI team
 */
public class SecurityTestLogModel extends AbstractListModel
{
	private List<Object> items = Collections.synchronizedList( new TreeList() );
	private List<SoftReference<SecurityCheckResult>> results = Collections.synchronizedList( new TreeList() );
	private int maxSize = 100;
	private int checkCount;

	public synchronized Object getElementAt( int arg0 )
	{
		try
		{
			return items.get( arg0 );
		}
		catch( Throwable e )
		{
			return null;
		}
	}

	@Override
	public int getSize()
	{
		return items.size();
	}

//	public void addEntry( SecurityTestLogMessageEntry securityTestLogMessageEntry )
//	{
//		items.add( securityTestLogMessageEntry );
//		fireIntervalAdded( this, items.size() - 1, items.size() - 1 );
//
//		enforceMaxSize();
//	}

	public synchronized SecurityCheckResult getResultAt( int index )
	{
		if( index >= results.size() )
			return null;

		SoftReference<SecurityCheckResult> result = results.get( index );
		return result == null ? null : result.get();
	}

	public synchronized void addSecurityCheckResult( SecurityCheckResult result )
	{
		checkCount++ ;

		int size = items.size();
		items.add( "Check " + checkCount + " [" + result.getSecurityCheck().getName() + "] " + result.getStatus()
				+ ": took " + result.getTimeTaken() + " ms" );
		SoftReference<SecurityCheckResult> ref = new SoftReference<SecurityCheckResult>( result );
		 results.add( ref );
		for( SecurityCheckRequestResult requestResult : result.getSecurityRequestResultList() )
		{
			for( String msg : requestResult.getMessages() )
			{
				items.add( " -> " + msg );
				 results.add( ref );
			}
		}

		fireIntervalAdded( this, size, items.size() - 1 );
		enforceMaxSize();
	}

	public synchronized void clear()
	{
		int sz = items.size();
		items.clear();
		 results.clear();
		checkCount = 0;
		fireIntervalRemoved( this, 0, sz );
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
			 results.remove( 0 );
			fireIntervalRemoved( this, 0, 0 );
		}
	}

//	public String getMessages()
//	{
//		StringBuffer sb = new StringBuffer();
//		for( Object messageEntry : items )
//		{
//			sb.append( ( ( SecurityTestLogMessageEntry )messageEntry ).getMessage() );
//			sb.append( "\n" );
//		}
//		return sb.toString();
//	}
	 public void setStepIndex( int i )
	 {
		 checkCount = i;
	 }
}
