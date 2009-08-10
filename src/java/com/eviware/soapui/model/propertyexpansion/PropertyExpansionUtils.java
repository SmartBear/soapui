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

package com.eviware.soapui.model.propertyexpansion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.support.SettingsTestPropertyHolder;
import com.eviware.soapui.model.testsuite.RenameableTestProperty;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToObjectMap;

public class PropertyExpansionUtils
{
	public final static Logger log = Logger.getLogger( PropertyExpansionUtils.class );

	private static SettingsTestPropertyHolder globalTestPropertyHolder;

	public static String getGlobalProperty( String propertyName )
	{
		if( globalTestPropertyHolder == null )
		{
			initGlobalProperties();
		}

		return globalTestPropertyHolder.getPropertyValue( propertyName );
	}

	private synchronized static void initGlobalProperties()
	{
		globalTestPropertyHolder = new SettingsTestPropertyHolder( SoapUI.getSettings(), null );

		String propFile = System.getProperty( "soapui.properties" );
		if( StringUtils.hasContent( propFile ) )
			globalTestPropertyHolder.addPropertiesFromFile( propFile );
	}

	public static void saveGlobalProperties()
	{
		if( globalTestPropertyHolder != null )
		{
			globalTestPropertyHolder.saveTo( SoapUI.getSettings() );
		}
	}

	/**
	 * @deprecated Use {@link PropertyExpander#expandProperties(String)} instead
	 */
	public static String expandProperties( String content )
	{
		return PropertyExpander.expandProperties( content );
	}

	/**
	 * @deprecated Use
	 *             {@link PropertyExpander#expandProperties(PropertyExpansionContext,String)}
	 *             instead
	 */
	public static String expandProperties( PropertyExpansionContext context, String content )
	{
		return PropertyExpander.expandProperties( context, content );
	}

	/**
	 * @deprecated Use
	 *             {@link PropertyExpander#expandProperties(PropertyExpansionContext,String,boolean)}
	 *             instead
	 */

	public static String expandProperties( PropertyExpansionContext context, String content, boolean entitize )
	{
		return PropertyExpander.expandProperties( context, content, entitize );
	}

	/**
	 * Checks if a property can be transferred to another specified property via
	 * a property-transfer
	 */

	public static boolean canTransferToProperty( TestProperty source, TestProperty target )
	{
		return false;
	}

	/**
	 * Checks if a modelItem can acces a specified property via
	 * property-expansion
	 */

	public static boolean canExpandProperty( ModelItem contextModelItem, TestProperty property )
	{
		ModelItem propertyModelItem = property.getModelItem();

		// global / anonymous reference?
		if( propertyModelItem == null || propertyModelItem instanceof Project )
			return true;

		if( contextModelItem instanceof TestSuite )
		{
			return propertyModelItem == contextModelItem;
		}

		if( contextModelItem instanceof TestCase )
		{
			return propertyModelItem == contextModelItem
					|| ( propertyModelItem instanceof TestSuite && ( ( TestCase )contextModelItem ).getTestSuite() == propertyModelItem );
		}

		if( contextModelItem instanceof TestStep )
		{
			TestStep testStep = ( ( TestStep )contextModelItem );

			return propertyModelItem == contextModelItem
					|| ( propertyModelItem instanceof TestSuite && testStep.getTestCase().getTestSuite() == propertyModelItem )
					|| ( propertyModelItem instanceof TestCase && testStep.getTestCase() == propertyModelItem )
					|| ( propertyModelItem instanceof TestStep && testStep.getTestCase() == ( ( TestStep )propertyModelItem )
							.getTestCase() );
		}

		if( contextModelItem instanceof MockService )
		{
			return propertyModelItem == contextModelItem;
		}

		if( contextModelItem instanceof MockOperation )
		{
			return propertyModelItem == contextModelItem
					|| ( propertyModelItem instanceof MockService && ( ( MockOperation )contextModelItem ).getMockService() == propertyModelItem );
		}

		if( contextModelItem instanceof MockResponse )
		{
			MockResponse testStep = ( ( MockResponse )contextModelItem );

			return propertyModelItem == contextModelItem
					|| ( propertyModelItem instanceof MockService && testStep.getMockOperation().getMockService() == propertyModelItem )
					|| ( propertyModelItem instanceof MockOperation && testStep.getMockOperation() == propertyModelItem )
					|| ( propertyModelItem instanceof MockResponse && testStep.getMockOperation() == ( ( MockResponse )propertyModelItem )
							.getMockOperation() );
		}

		System.out
				.println( "property " + property.getName() + " can not be transferred to " + contextModelItem.getName() );
		return false;
	}

	public static MutableTestPropertyHolder getGlobalProperties()
	{
		if( globalTestPropertyHolder == null )
		{
			initGlobalProperties();
		}

		return globalTestPropertyHolder;
	}

	public static MutablePropertyExpansion[] renameProperty( RenameableTestProperty property, String newName,
			ModelItem root )
	{
		UISupport.setHourglassCursor();

		try
		{
			List<MutablePropertyExpansion> result = new ArrayList<MutablePropertyExpansion>();
			List<MutablePropertyExpansion> properties = new ArrayList<MutablePropertyExpansion>();

			PropertyExpansion[] propertyExpansions = getPropertyExpansions( root, true, true );
			for( PropertyExpansion pe : propertyExpansions )
			{
				MutablePropertyExpansion mpe = ( MutablePropertyExpansion )pe;
				if( mpe.getProperty().equals( property ) )
				{
					mpe.setProperty( property );
					properties.add( mpe );
				}
			}

			property.setName( newName );

			for( MutablePropertyExpansion mpe : properties )
			{
				try
				{
					mpe.update();
					result.add( mpe );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}

			return result.toArray( new MutablePropertyExpansion[result.size()] );
		}
		finally
		{
			UISupport.resetCursor();
		}
	}

	public static PropertyExpansion[] getPropertyExpansions( ModelItem modelItem, boolean mutableOnly, boolean deep )
	{
		List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();

		if( modelItem instanceof PropertyExpansionContainer )
		{
			PropertyExpansion[] pes = ( ( PropertyExpansionContainer )modelItem ).getPropertyExpansions();
			if( pes != null && pes.length > 0 )
			{
				for( PropertyExpansion pe : pes )
				{
					if( mutableOnly && !( pe instanceof MutablePropertyExpansion ) )
						continue;

					result.add( pe );
				}
			}
		}

		if( deep )
		{
			List<? extends ModelItem> children = modelItem.getChildren();
			if( children != null && children.size() > 0 )
			{
				for( ModelItem child : children )
				{
					result.addAll( Arrays.asList( getPropertyExpansions( child, mutableOnly, deep ) ) );
				}
			}
		}

		return result.toArray( new PropertyExpansion[result.size()] );
	}

	public static Collection<? extends PropertyExpansion> extractPropertyExpansions( ModelItem modelItem, Object target,
			String propertyName )
	{
		List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();
		Set<String> expansions = new HashSet<String>();

		try
		{
			Object property = PropertyUtils.getProperty( target, propertyName );
			if( property instanceof String && PropertyUtils.isWriteable( target, propertyName ) )
			{
				String str = property.toString();

				if( !StringUtils.isNullOrEmpty( str ) )
				{
					int ix = str.indexOf( "${" );
					while( ix != -1 )
					{
						// TODO handle nested property-expansions..
						int ix2 = str.indexOf( '}', ix + 2 );
						if( ix2 == -1 )
							break;

						String expansion = str.substring( ix + 2, ix2 );
						if( !expansions.contains( expansion ) )
						{
							MutablePropertyExpansion tp = createMutablePropertyExpansion( expansion, modelItem, target,
									propertyName );
							if( tp != null )
							{
								result.add( tp );
								expansions.add( expansion );
							}
						}

						str = str.substring( ix2 );
						ix = str.indexOf( "${" );
					}
				}
			}
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		return result;
	}

	public static MutablePropertyExpansionImpl createMutablePropertyExpansion( String pe, ModelItem modelItem,
			Object target, String propertyName )
	{
		WsdlTestStep testStep = null;
		WsdlTestCase testCase = null;
		WsdlTestSuite testSuite = null;
		WsdlProject project = null;
		WsdlMockService mockService = null;
		WsdlMockResponse mockResponse = null;
		TestPropertyHolder holder = null;

		if( modelItem instanceof WsdlTestStep )
		{
			testStep = ( WsdlTestStep )modelItem;
			testCase = testStep.getTestCase();
			testSuite = testCase.getTestSuite();
			project = testSuite.getProject();
		}
		else if( modelItem instanceof WsdlTestCase )
		{
			testCase = ( WsdlTestCase )modelItem;
			testSuite = testCase.getTestSuite();
			project = testSuite.getProject();
		}
		else if( modelItem instanceof WsdlTestSuite )
		{
			testSuite = ( WsdlTestSuite )modelItem;
			project = testSuite.getProject();
		}
		else if( modelItem instanceof WsdlInterface )
		{
			project = ( ( WsdlInterface )modelItem ).getProject();
		}
		else if( modelItem instanceof WsdlProject )
		{
			project = ( WsdlProject )modelItem;
		}
		else if( modelItem instanceof WsdlMockService )
		{
			mockService = ( WsdlMockService )modelItem;
			project = mockService.getProject();
		}
		else if( modelItem instanceof AbstractHttpRequestInterface<?> )
		{
			project = ( ( AbstractHttpRequest<?> )modelItem ).getOperation().getInterface().getProject();
		}
		else if( modelItem instanceof WsdlMockOperation )
		{
			mockService = ( ( WsdlMockOperation )modelItem ).getMockService();
			project = mockService.getProject();
		}
		else if( modelItem instanceof WsdlMockResponse )
		{
			mockResponse = ( WsdlMockResponse )modelItem;
			mockService = mockResponse.getMockOperation().getMockService();
			project = mockService.getProject();
		}

		// explicit item reference?
		if( pe.startsWith( PropertyExpansion.PROJECT_REFERENCE ) )
		{
			holder = project;
			pe = pe.substring( PropertyExpansion.PROJECT_REFERENCE.length() );
		}
		else if( pe.startsWith( PropertyExpansion.TESTSUITE_REFERENCE ) )
		{
			holder = testSuite;
			pe = pe.substring( PropertyExpansion.TESTSUITE_REFERENCE.length() );
		}
		else if( pe.startsWith( PropertyExpansion.TESTCASE_REFERENCE ) )
		{
			holder = testCase;
			pe = pe.substring( PropertyExpansion.TESTCASE_REFERENCE.length() );
		}
		else if( pe.startsWith( PropertyExpansion.MOCKSERVICE_REFERENCE ) )
		{
			holder = mockService;
			pe = pe.substring( PropertyExpansion.MOCKSERVICE_REFERENCE.length() );
		}
		else if( pe.startsWith( PropertyExpansion.MOCKRESPONSE_REFERENCE ) )
		{
			holder = mockResponse;
			pe = pe.substring( PropertyExpansion.MOCKRESPONSE_REFERENCE.length() );
		}
		else if( testCase != null )
		{
			int sepIx = pe.indexOf( PropertyExpansion.PROPERTY_SEPARATOR );
			if( sepIx > 0 )
			{
				holder = testCase.getTestStepByName( pe.substring( 0, sepIx ) );
				if( holder != null )
				{
					pe = pe.substring( sepIx + 1 );
				}
			}
		}

		int sepIx = pe.indexOf( PropertyExpansion.XPATH_SEPARATOR );
		String xpath = null;

		if( sepIx > 0 )
		{
			xpath = pe.substring( sepIx + 1 );
			pe = pe.substring( 0, sepIx );
		}

		if( holder == null )
			holder = getGlobalProperties();

		TestProperty tp = holder.getProperty( pe );
		return tp == null ? null : new MutablePropertyExpansionImpl( tp, xpath, target, propertyName );
	}

	/**
	 * @deprecated Use
	 *             {@link PropertyExpander#expandProperties(ModelItem,String)}
	 *             instead
	 */
	public static String expandProperties( ModelItem contextModelItem, String content )
	{
		return PropertyExpander.expandProperties( contextModelItem, content );
	}

	public static class GlobalPropertyExpansionContext implements PropertyExpansionContext
	{
		public Object getProperty( String name )
		{
			return getGlobalProperties().getProperty( name );
		}

		public void setProperty( String name, Object value )
		{
			getGlobalProperties().setPropertyValue( name, String.valueOf( value ) );
		}

		public boolean hasProperty( String name )
		{
			return getGlobalProperties().hasProperty( name );
		}

		public Object removeProperty( String name )
		{
			return getGlobalProperties().removeProperty( name );
		}

		public String[] getPropertyNames()
		{
			return getGlobalProperties().getPropertyNames();
		}

		public ModelItem getModelItem()
		{
			return null;
		}

		public String expand( String content )
		{
			return PropertyExpander.expandProperties( this, content );
		}

		public StringToObjectMap getProperties()
		{
			StringToObjectMap result = new StringToObjectMap();
			Map<String, TestProperty> props = getGlobalProperties().getProperties();
			for( String key : props.keySet() )
			{
				result.put( key, props.get( key ) );
			}

			return result;
		}
	}
	
	public static boolean containsPropertyExpansion( String str )
	{
		return str != null && str.indexOf( "${" ) >= 0 && str.indexOf( '}' ) > 2;
	}
}
