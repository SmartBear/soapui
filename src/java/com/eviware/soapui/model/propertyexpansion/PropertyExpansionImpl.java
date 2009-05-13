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

import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.StringUtils;

public class PropertyExpansionImpl implements PropertyExpansion
{
	private String xpath;
	private TestProperty property;
	private String containerInfo;

	public PropertyExpansionImpl( TestProperty property, String xpath )
	{
		this.property = property;
		this.xpath = xpath;

		containerInfo = property.getName();
		if( property.getModelItem() != null )
			containerInfo += " in " + property.getModelItem().getName();
	}

	public TestProperty getProperty()
	{
		return property;
	}

	public String toString()
	{
		StringBuffer result = new StringBuffer();
		result.append( "${" );

		ModelItem modelItem = property.getModelItem();

		if( modelItem instanceof Project )
			result.append( PropertyExpansionImpl.PROJECT_REFERENCE );
		else if( modelItem instanceof TestSuite )
			result.append( PropertyExpansionImpl.TESTSUITE_REFERENCE );
		else if( modelItem instanceof TestCase )
			result.append( PropertyExpansionImpl.TESTCASE_REFERENCE );
		else if( modelItem instanceof MockService )
			result.append( PropertyExpansionImpl.MOCKSERVICE_REFERENCE );
		else if( modelItem instanceof MockResponse )
			result.append( PropertyExpansionImpl.MOCKRESPONSE_REFERENCE );
		else if( modelItem instanceof TestStep )
			result.append( modelItem.getName() ).append( PROPERTY_SEPARATOR );
		else if( modelItem instanceof TestRequest )
			result.append( ( ( TestRequest )modelItem ).getTestStep().getName() ).append( PROPERTY_SEPARATOR );

		result.append( property.getName() );
		if( StringUtils.hasContent( xpath ) )
			result.append( PROPERTY_SEPARATOR ).append( xpath );

		result.append( '}' );

		return result.toString();
	}

	public String getXPath()
	{
		return xpath;
	}

	public String getContainerInfo()
	{
		return containerInfo;
	}

	public void setContainerInfo( String containerInfo )
	{
		this.containerInfo = containerInfo;
	}

	protected void setProperty( TestProperty property )
	{
		this.property = property;
	}

	protected void setXPath( String xpath )
	{
		this.xpath = xpath;
	}
}
