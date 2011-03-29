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
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;

import org.apache.commons.collections.list.TreeList;

import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.result.SecurityCheckRequestResult;
import com.eviware.soapui.security.result.SecurityCheckResult;
import com.eviware.soapui.security.result.SecurityResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.security.result.SecurityResult.SecurityStatus;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * SecurityTestLog
 * 
 * @author soapUI team
 */
public class SecurityTestLogModel extends AbstractListModel
{
	private List<Object> items = Collections.synchronizedList( new TreeList() );
	private List<SoftReference<SecurityResult>> results = Collections.synchronizedList( new TreeList() );
	private int maxSize = 100;
	private int stepCount;
	private int checkCount;
	private int requestCount;
	private SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );
	private int startCheckIndex;
	private int startStepIndex;

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
		results.add( null );
		fireIntervalAdded( this, items.size() - 1, items.size() - 1 );

		enforceMaxSize();
	}

	public synchronized SecurityResult getTestStepResultAt( int index )
	{
		if( index >= results.size() )
			return null;

		SoftReference<SecurityResult> result = results.get( index );
		return result == null ? null : result.get();
	}

	public synchronized void addSecurityTestStepResult( SecurityTestStepResult result )
	{
		stepCount++ ;
		checkCount = 0;

		int size = items.size();
		startStepIndex = size;

		SoftReference<SecurityResult> stepResultRef = new SoftReference<SecurityResult>( result );

		items.add( "Step " + stepCount + " [" + result.getOriginalTestStepResult().getTestStep().getName() + "] "
				+ result.getOriginalTestStepResult().getStatus() + ": took "
				+ result.getOriginalTestStepResult().getTimeTaken() + " ms" );
		results.add( stepResultRef );

		for( String msg : result.getOriginalTestStepResult().getMessages() )
		{
			items.add( " -> " + msg );
			results.add( stepResultRef );
			// checkResults.add( null );
		}
		// if( AbstractSecurityCheck.isSecurable(
		// result.getOriginalTestStepResult().getTestStep() )
		// && !result.getSecurityCheckResultList().isEmpty() )
		// {
		// for( int i = 0; i < result.getSecurityCheckResultList().size(); i++ )
		// {
		// SecurityCheckResult securityCheckResult =
		// result.getSecurityCheckResultList().get( i );
		// addSecurityCheckResult( securityCheckResult );
		// }
		// }

		fireIntervalAdded( this, size, items.size() - 1 );
		enforceMaxSize();
	}

	// called after whole security teststep finished to delete start line in case
	// only errors are beeing displayed
	public synchronized void updateSecurityTestStepResult( SecurityTestStepResult result, boolean errorsOnly )
	{
		if( errorsOnly && result.getStatus() != SecurityStatus.FAILED )
		{
			stepCount-- ;
			int size = items.size() - 1;
			while( size >= startStepIndex )
			{
				items.remove( size );
				results.remove( size );
				size-- ;
			}
			fireIntervalRemoved( this, startStepIndex, size );
		}
	}

	public synchronized void addSecurityCheckResult( SecurityCheck securityCheck )
	{
		int size = items.size();
		startCheckIndex = size;
		checkCount++ ;
		requestCount = 0;

		SecurityCheckResult securityCheckResult = securityCheck.getSecurityCheckResult();
		SoftReference<SecurityResult> checkResultRef = new SoftReference<SecurityResult>( securityCheckResult );

		items.add( "SecurityCheck " + checkCount + " [" + securityCheck.getName() + "] " );
		results.add( checkResultRef );

		fireIntervalAdded( this, size, items.size() - 1 );
		enforceMaxSize();
	}

	// updates log entry for security check with the status, time taken, and
	// similar info known only after finished
	public synchronized void updateSecurityCheckResult( SecurityCheckResult securityCheckResult, boolean errorsOnly )
	{
		if( errorsOnly && securityCheckResult.getStatus() != SecurityStatus.FAILED )
		{
			// remove all entries for the securityCheck that had no warnings
			checkCount-- ;
			int size = items.size() - 1;
			while( size >= startCheckIndex )
			{
				items.remove( size );
				results.remove( size );
				size-- ;
			}
			fireIntervalRemoved( this, startCheckIndex, size );

		}
		else
		{
			items.set( startCheckIndex, "SecurityCheck " + checkCount + " ["
					+ securityCheckResult.getSecurityCheck().getName() + "] " + securityCheckResult.getStatus()
					+ ", time taken = " + securityCheckResult.getTimeTaken() );
			SoftReference<SecurityResult> checkResultRef = new SoftReference<SecurityResult>( securityCheckResult );
			results.set( startCheckIndex, checkResultRef );

			fireContentsChanged( this, startCheckIndex, startCheckIndex );

		}
	}

	public synchronized void addSecurityCheckRequestResult( SecurityCheckRequestResult securityCheckRequestResult )
	{
		int size = items.size();
		requestCount++ ;

		StringToStringMap changedParams = StringToStringMap.fromXml( securityCheckRequestResult.getMessageExchange()
				.getProperties().get( AbstractSecurityCheck.SECURITY_CHANGED_PARAMETERS ) );
		StringBuilder changedParamsInfo = new StringBuilder();
		changedParamsInfo.append( "[" );
		Iterator<String> keys = changedParams.keySet().iterator();
		while( keys.hasNext() )
		{
			String param = ( String )keys.next();
			String value = changedParams.get( param );
			changedParamsInfo.append( param + "=" + value + "," );
		}
		changedParamsInfo.replace( changedParamsInfo.length() - 1, changedParamsInfo.length(), "]" );

		SoftReference<SecurityResult> checkReqResultRef = new SoftReference<SecurityResult>( securityCheckRequestResult );

		items.add( "[" + securityCheckRequestResult.getSecurityCheck().getName() + "] Request " + requestCount + " - "
				+ securityCheckRequestResult.getStatus() + " - " + changedParamsInfo.toString() );
		results.add( checkReqResultRef );

		changedParamsInfo.toString();

		for( String msg : securityCheckRequestResult.getMessages() )
		{
			items.add( " -> " + msg );
			results.add( checkReqResultRef );
		}

		fireIntervalAdded( this, size, items.size() - 1 );
		enforceMaxSize();
	}

	public synchronized void clear()
	{
		int sz = items.size();
		items.clear();
		results.clear();
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
			results.remove( 0 );
			fireIntervalRemoved( this, 0, 0 );
		}
	}

	public synchronized int getIndexOfSecurityCheck( SecurityCheck check )
	{
		for( int i = 0; i < results.size(); i++ )
		{
			SoftReference<SecurityResult> result = results.get( i );
			if( result != null )
			{
				SecurityResult referent = result.get();
				if( referent instanceof SecurityCheckResult )
				{
					if( ( ( SecurityCheckResult )referent ).getSecurityCheck() == check )
					{
						return i;
					}
				}
			}
		}
		return -1;
	}
}
