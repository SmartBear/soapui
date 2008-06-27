/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.support.assertions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RequestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.WsdlAssertionRegistry;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionsListener;

/**
 * Utility for implementing the Assertable interface
 *  
 * @author ole.matzura
 */

public class AssertionsSupport implements PropertyChangeListener
{
	private List<AssertionsListener> assertionsListeners = new ArrayList<AssertionsListener>();
	private List<WsdlMessageAssertion> assertions = new ArrayList<WsdlMessageAssertion>();
	private final Assertable assertable;

	public AssertionsSupport( Assertable assertable, List<RequestAssertionConfig> assertionList )
	{
		this.assertable = assertable;

		for( RequestAssertionConfig rac : assertionList )
		{
			addWsdlAssertion( rac );
		}
	}

	public WsdlMessageAssertion addWsdlAssertion( RequestAssertionConfig config )
	{
		try
		{
			WsdlMessageAssertion assertion = WsdlAssertionRegistry.getInstance().buildAssertion(
						config, assertable );
			if( assertion == null )
			{
				return null;
			}
			else
			{
				assertions.add( assertion );
				assertion.addPropertyChangeListener( this );

				return assertion;
			}
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
			return null;
		}
	}

	public void propertyChange( PropertyChangeEvent arg0 )
	{
		if( assertable instanceof PropertyChangeListener )
			((PropertyChangeListener)assertable).propertyChange( arg0 );
	}

	public int getAssertionCount()
	{
		return assertions.size();
	}

	public WsdlMessageAssertion getAssertionAt( int c )
	{
		return assertions.get( c );
	}

	public void addAssertionsListener( AssertionsListener listener )
	{
		assertionsListeners.add( listener );
	}

	public void removeAssertionsListener( AssertionsListener listener )
	{
		assertionsListeners.remove( listener );
	}

	public int removeAssertion( WsdlMessageAssertion assertion )
	{
		int ix = assertions.indexOf( assertion );
		if( ix == -1 )
		{
			throw new RuntimeException( "assertion [" + assertion.getName() + "] not available " );
		}

		assertion.removePropertyChangeListener( this );
		assertions.remove( ix );
		fireAssertionRemoved( assertion );

		return ix;
	}

	public void release()
	{
		for( WsdlMessageAssertion assertion : assertions )
			assertion.release();

	}

	public Iterator<WsdlMessageAssertion> iterator()
	{
		return assertions.iterator();
	}

	public void fireAssertionAdded( WsdlMessageAssertion assertion )
	{
		AssertionsListener[] listeners = assertionsListeners
					.toArray( new AssertionsListener[assertionsListeners.size()] );

		for( int c = 0; c < listeners.length; c++ )
		{
			listeners[c].assertionAdded( assertion );
		}
	}

	public void fireAssertionRemoved( WsdlMessageAssertion assertion )
	{
		AssertionsListener[] listeners = assertionsListeners
					.toArray( new AssertionsListener[assertionsListeners.size()] );

		for( int c = 0; c < listeners.length; c++ )
		{
			listeners[c].assertionRemoved( assertion );
		}
	}
	
	public void updateConfig( List<RequestAssertionConfig> assertionList )
	{
		int mod = 0;
		
      for (int i = 0; i < assertionList.size(); i++)
      {
         RequestAssertionConfig config = assertionList.get( i );
         if( WsdlAssertionRegistry.getInstance().canBuildAssertion( config ))
         {
         	assertions.get( i-mod ).updateConfig( config );
         }
         else mod++;
      }
	}

	public List<WsdlMessageAssertion> getAssertionList()
	{
		return assertions;
	}

	public List<WsdlMessageAssertion> getAssertionsOfType( Class<? extends WsdlMessageAssertion> class1 )
	{
		List<WsdlMessageAssertion> result = new ArrayList<WsdlMessageAssertion>();
		
		for( WsdlMessageAssertion assertion : assertions )
		{
			if( assertion.getClass().equals( class1 ))
				result.add(  assertion );
		}
		
		return result;
	}

	public WsdlMessageAssertion getAssertionByName( String name )
	{
		for( WsdlMessageAssertion assertion : assertions )
		{
			if( assertion.getName().equals( name ))
				return assertion;
		}
		
		return null;
	}
}
