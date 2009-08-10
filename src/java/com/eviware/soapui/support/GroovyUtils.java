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

package com.eviware.soapui.support;

import java.io.File;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestStep;

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
			return new XmlHolder( XmlObject.Factory.parse( xmlPropertyOrString ) );
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
		return XmlObject.Factory.parse( node ).xmlText();
	}
}
