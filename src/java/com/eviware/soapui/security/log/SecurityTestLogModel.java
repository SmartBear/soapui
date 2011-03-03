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

import com.eviware.soapui.security.SecurityCheckRequestResult;
import com.eviware.soapui.security.SecurityCheckResult;
import com.eviware.soapui.security.SecurityTestStepResult;
import com.eviware.soapui.security.check.AbstractSecurityCheck;

/**
 * SecurityTestLog
 * 
 * @author soapUI team
 */
public class SecurityTestLogModel extends AbstractListModel
{
	private List<Object> items = Collections.synchronizedList( new TreeList() );
	private List<SoftReference<SecurityTestStepResult>> testStepResults = Collections.synchronizedList( new TreeList() );
	private int maxSize = 100;
	private int stepCount;

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

	public synchronized void addText( String msg )
	{
		items.add( msg );
		testStepResults.add( null );
		fireIntervalAdded( this, items.size() - 1, items.size() - 1 );

		enforceMaxSize();
	}

	public synchronized SecurityTestStepResult getTestStepResultAt( int index )
	{
		if( index >= testStepResults.size() )
			return null;

		SoftReference<SecurityTestStepResult> result = testStepResults.get( index );
		return result == null ? null : result.get();
	}

	public synchronized void addSecurityTestStepResult( SecurityTestStepResult result )
	{
		stepCount++ ;

		int size = items.size();

		SoftReference<SecurityTestStepResult> stepResultRef = new SoftReference<SecurityTestStepResult>( result );

		items.add( "Step " + stepCount + " [" + result.getOriginalTestStepResult().getTestStep().getName() + "] "
				+ result.getOriginalTestStepResult().getStatus() + ": took "
				+ result.getOriginalTestStepResult().getTimeTaken() + " ms" );
		testStepResults.add( stepResultRef );
		for( String msg : result.getOriginalTestStepResult().getMessages() )
		{
			items.add( " -> " + msg );
			testStepResults.add( stepResultRef );
		}
		if( AbstractSecurityCheck.isSecurable( result.getOriginalTestStepResult().getTestStep() )
				&& !result.getSecurityCheckResultList().isEmpty() )
		{
			for( int i = 0; i < result.getSecurityCheckResultList().size(); i++ )
			{
				SecurityCheckResult securityCheckResult = result.getSecurityCheckResultList().get( i );
				items.add( "Check " + ( i + 1 ) + " [" + securityCheckResult.getSecurityCheck().getName() + "] "
						+ securityCheckResult.getStatus() + ": took " + result.getTimeTaken() + " ms" );
				testStepResults.add( stepResultRef );
				for( SecurityCheckRequestResult requestResult : securityCheckResult.getSecurityRequestResultList() )
				{
					for( String msg : requestResult.getMessages() )
					{
						items.add( " -> " + msg );
						testStepResults.add( stepResultRef );
					}
				}
			}
		}

		fireIntervalAdded( this, size, items.size() - 1 );
		enforceMaxSize();
	}

	public synchronized void clear()
	{
		int sz = items.size();
		items.clear();
		testStepResults.clear();
		stepCount = 0;
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
			testStepResults.remove( 0 );
			fireIntervalRemoved( this, 0, 0 );
		}
	}

}
