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

package com.eviware.soapui.impl.wsdl.support.assertions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.support.resolver.ResolveContext;

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
	private AssertableConfig modelItemConfig;

	public AssertionsSupport( Assertable assertable, AssertableConfig modelItemConfig )
	{
		this.assertable = assertable;
		this.modelItemConfig = modelItemConfig;

		for( TestAssertionConfig rac : modelItemConfig.getAssertionList() )
		{
			addWsdlAssertion( rac );
		}
	}

	public WsdlMessageAssertion addWsdlAssertion( TestAssertionConfig config )
	{
		try
		{
			WsdlMessageAssertion assertion = TestAssertionRegistry.getInstance().buildAssertion( config, assertable );
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
			( ( PropertyChangeListener )assertable ).propertyChange( arg0 );
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

	public void removeAssertion( WsdlMessageAssertion assertion )
	{
		int ix = assertions.indexOf( assertion );
		if( ix == -1 )
		{
			throw new RuntimeException( "assertion [" + assertion.getName() + "] not available " );
		}

		assertion.removePropertyChangeListener( this );
		assertions.remove( ix );
		fireAssertionRemoved( assertion );

		assertion.release();

		modelItemConfig.removeAssertion( ix );
	}

	public WsdlMessageAssertion moveAssertion( int ix, int offset )
	{
		// int ix = assertions.indexOf( assertion );
		WsdlMessageAssertion assertion = getAssertionAt( ix );
		if( ix == -1 )
		{
			throw new RuntimeException( "assertion [" + assertion.getName() + "] not available " );
		}
		// if first selected can't move up and if last selected can't move down
		if( ( ix == 0 && offset == -1 ) || ( ix == assertions.size() - 1 && offset == 1 ) )
		{
			return assertion;
		}
		TestAssertionConfig conf = assertion.getConfig();
		XmlObject newXmlObject = conf.copy();

		TestAssertionConfig newConf = TestAssertionConfig.Factory.newInstance();
		newConf.set( newXmlObject );
		WsdlMessageAssertion newAssertion = TestAssertionRegistry.getInstance().buildAssertion( newConf, assertable );

		assertion.removePropertyChangeListener( this );
		assertions.remove( ix );

		assertion.release();

		modelItemConfig.removeAssertion( ix );

		newAssertion.addPropertyChangeListener( this );
		assertions.add( ix + offset, newAssertion );

		newAssertion.release();

		modelItemConfig.insertAssertion( newConf, ix + offset );
		fireAssertionMoved( newAssertion, ix, offset );
		return newAssertion;

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
		AssertionsListener[] listeners = assertionsListeners.toArray( new AssertionsListener[assertionsListeners.size()] );

		for( int c = 0; c < listeners.length; c++ )
		{
			listeners[c].assertionAdded( assertion );
		}
	}

	public void fireAssertionRemoved( WsdlMessageAssertion assertion )
	{
		AssertionsListener[] listeners = assertionsListeners.toArray( new AssertionsListener[assertionsListeners.size()] );

		for( int c = 0; c < listeners.length; c++ )
		{
			listeners[c].assertionRemoved( assertion );
		}
	}

	public void fireAssertionMoved( WsdlMessageAssertion assertion, int ix, int offset )
	{
		AssertionsListener[] listeners = assertionsListeners.toArray( new AssertionsListener[assertionsListeners.size()] );

		for( int c = 0; c < listeners.length; c++ )
		{
			listeners[c].assertionMoved( assertion, ix, offset );
		}
	}

	public void refresh()
	{
		int mod = 0;

		List<TestAssertionConfig> assertionList = modelItemConfig.getAssertionList();

		for( int i = 0; i < assertionList.size(); i++ )
		{
			TestAssertionConfig config = assertionList.get( i );
			if( TestAssertionRegistry.getInstance().canBuildAssertion( config ) )
			{
				assertions.get( i - mod ).updateConfig( config );
			}
			else
				mod++ ;
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
			if( assertion.getClass().equals( class1 ) )
				result.add( assertion );
		}

		return result;
	}

	public WsdlMessageAssertion getAssertionByName( String name )
	{
		for( WsdlMessageAssertion assertion : assertions )
		{
			if( assertion.getName().equals( name ) )
				return assertion;
		}

		return null;
	}

	public Map<String, TestAssertion> getAssertions()
	{
		Map<String, TestAssertion> result = new HashMap<String, TestAssertion>();

		for( TestAssertion assertion : assertions )
			result.put( assertion.getName(), assertion );

		return result;
	}

	public WsdlMessageAssertion importAssertion( WsdlMessageAssertion source, boolean overwrite, boolean createCopy )
	{
		TestAssertionConfig conf = modelItemConfig.addNewAssertion();
		conf.set( source.getConfig() );
		if( createCopy && conf.isSetId() )
			conf.unsetId();

		if( !source.isAllowMultiple() )
		{
			List<WsdlMessageAssertion> existing = getAssertionsOfType( source.getClass() );
			if( !existing.isEmpty() && !overwrite )
				return null;

			while( !existing.isEmpty() )
			{
				removeAssertion( existing.remove( 0 ) );
			}
		}

		WsdlMessageAssertion result = addWsdlAssertion( conf );
		fireAssertionAdded( result );
		return result;
	}

	public TestAssertion cloneAssertion( TestAssertion source, String name )
	{
		TestAssertionConfig conf = modelItemConfig.addNewAssertion();
		conf.set( ( ( WsdlMessageAssertion )source ).getConfig() );
		conf.setName( name );

		WsdlMessageAssertion result = addWsdlAssertion( conf );
		fireAssertionAdded( result );
		return result;

	}

	public WsdlMessageAssertion addWsdlAssertion( String assertionLabel )
	{
		try
		{
			TestAssertionConfig assertionConfig = modelItemConfig.addNewAssertion();
			assertionConfig.setType( TestAssertionRegistry.getInstance().getAssertionTypeForName( assertionLabel ) );

			WsdlMessageAssertion assertion = addWsdlAssertion( assertionConfig );
			if( assertion == null )
				return null;

			fireAssertionAdded( assertion );

			return assertion;
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
			return null;
		}
	}

	public void resolve( ResolveContext<?> context )
	{
		for( WsdlMessageAssertion assertion : assertions )
		{
			assertion.resolve( context );
		}
	}
}
