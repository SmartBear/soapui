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

package com.eviware.soapui.support;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Node;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.xml.XmlUtils;

public class GroovyUtils
{
	protected final PropertyExpansionContext context;

	public GroovyUtils( PropertyExpansionContext context )
	{
		this.context = context;
	}

	public final String getProjectPath()
	{
		Project project = ModelSupport.getModelItemProject( context.getModelItem() );

		String path = project.getPath();
		int ix = path.lastIndexOf( File.separatorChar );
		return ix == -1 ? "" : path.substring( 0, ix );
	}

	public final XmlHolder getXmlHolder( String xmlPropertyOrString ) throws Exception
	{
		try
		{
			// return new XmlHolder( XmlObject.Factory.parse( xmlPropertyOrString )
			// );
			return new XmlHolder( XmlUtils.createXmlObject( xmlPropertyOrString ) );
		}
		catch( Exception e )
		{
			return new XmlHolder( context, xmlPropertyOrString );
		}
	}

	public final String expand( String property )
	{
		return PropertyExpander.expandProperties( context, property );
	}

	public final void setPropertyValue( String testStep, String property, String value ) throws Exception
	{
		if( !( context instanceof TestCaseRunContext ) )
			return;

		TestStep step = ( ( TestCaseRunContext )context ).getTestCase().getTestStepByName( testStep );
		if( step != null )
		{
			step.setPropertyValue( property, value );
		}
		else
		{
			throw new Exception( "Missing TestStep [" + testStep + "] in TestCase" );
		}
	}

	public final String getXml( Node node ) throws XmlException
	{
		// return XmlObject.Factory.parse( node ).xmlText();
		return XmlUtils.createXmlObject( node ).xmlText();
	}

	private static Set<String> registeredDrivers = new HashSet<String>();

	public static void registerJdbcDriver( String name )
	{
		if( registeredDrivers.contains( name ) )
			return;

		try
		{
			Driver d = ( Driver )Class.forName( name, true, SoapUI.getSoapUICore().getExtensionClassLoader() )
					.newInstance();
			DriverManager.registerDriver( new DriverShim( d ) );
			registeredDrivers.add( name );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	static class DriverShim implements Driver
	{
		private Driver driver;

		DriverShim( Driver d )
		{
			this.driver = d;
		}

		public boolean acceptsURL( String u ) throws SQLException
		{
			return this.driver.acceptsURL( u );
		}

		public Connection connect( String u, Properties p ) throws SQLException
		{
			return this.driver.connect( u, p );
		}

		public int getMajorVersion()
		{
			return this.driver.getMajorVersion();
		}

		public int getMinorVersion()
		{
			return this.driver.getMinorVersion();
		}

		public DriverPropertyInfo[] getPropertyInfo( String u, Properties p ) throws SQLException
		{
			return this.driver.getPropertyInfo( u, p );
		}

		public boolean jdbcCompliant()
		{
			return this.driver.jdbcCompliant();
		}
	}
}
