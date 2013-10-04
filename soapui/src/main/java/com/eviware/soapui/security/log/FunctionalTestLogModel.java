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

package com.eviware.soapui.security.log;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

import org.apache.commons.collections.list.TreeList;

import com.eviware.soapui.model.testsuite.TestStepResult;

/**
 * SecurityTest - Functional log
 * 
 * @author SoapUI team
 */
@SuppressWarnings( "serial" )
public class FunctionalTestLogModel extends AbstractListModel
{
	private List<Object> items = Collections.synchronizedList( new TreeList() );
	private List<SoftReference<TestStepResult>> results = Collections.synchronizedList( new TreeList() );
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
		results.add( null );
		fireIntervalAdded( this, items.size() - 1, items.size() - 1 );

		enforceMaxSize();
	}

	public synchronized TestStepResult getTestStepResultAt( int index )
	{
		if( index >= results.size() )
			return null;

		SoftReference<TestStepResult> result = results.get( index );
		return result == null ? null : result.get();
	}

	public synchronized void addSecurityTestFunctionalStepResult( TestStepResult result )
	{
		stepCount++ ;
		int size = items.size();
		SoftReference<TestStepResult> stepResultRef = new SoftReference<TestStepResult>( result );

		items.add( "Step " + stepCount + " [" + result.getTestStep().getName() + "] " + result.getStatus() + ": took "
				+ result.getTimeTaken() + " ms" );
		results.add( stepResultRef );

		for( String msg : result.getMessages() )
		{
			items.add( " -> " + msg );
			results.add( stepResultRef );
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

}
