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

package com.eviware.soapui.model.support;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlString;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.RenameableTestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepProperty;

/***
 * Default implementation of TestStepProperty interface
 * 
 * @author Ole.Matzura
 */

public class DefaultTestStepProperty implements TestStepProperty, RenameableTestProperty
{
	private String name;
	private boolean isReadOnly;
	private String description;
	private PropertyHandler handler;
	private final WsdlTestStep testStep;

	public DefaultTestStepProperty( String name, boolean isReadOnly, PropertyHandler handler, WsdlTestStep testStep )
	{
		this.name = name;
		this.isReadOnly = isReadOnly;
		this.handler = handler;
		this.testStep = testStep;
	}

	public DefaultTestStepProperty( String name, WsdlTestStep testStep )
	{
		this( name, false, new SimplePropertyHandler(), testStep );
	}

	public DefaultTestStepProperty( String name, boolean isReadOnly, WsdlTestStep testStep )
	{
		this( name, isReadOnly, new SimplePropertyHandler(), testStep );
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription( String description )
	{
		this.description = description;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public void setIsReadOnly( boolean isReadOnly )
	{
		this.isReadOnly = isReadOnly;
	}

	public boolean isReadOnly()
	{
		return isReadOnly;
	}

	public void setPropertyHandler( PropertyHandler handler )
	{
		this.handler = handler;
	}

	public String getValue()
	{
		return handler == null ? null : handler.getValue( this );
	}

	public void setValue( String value )
	{
		if( isReadOnly() )
			throw new RuntimeException( "Trying to set read-only property [" + getName() + "]" );

		if( handler != null )
		{
			handler.setValue( this, value );
		}
	}

	public TestStep getTestStep()
	{
		return testStep;
	}

	/**
	 * Handler for providing and setting property values
	 * 
	 * @author Ole.Matzura
	 */

	public interface PropertyHandler
	{
		public String getValue( DefaultTestStepProperty property );

		public void setValue( DefaultTestStepProperty property, String value );
	}

	/**
	 * Empty implementation of PropertyHandler interface
	 * 
	 * @author Ole.Matzura
	 */

	public static class PropertyHandlerAdapter implements PropertyHandler
	{
		public String getValue( DefaultTestStepProperty property )
		{
			return null;
		}

		public void setValue( DefaultTestStepProperty property, String value )
		{
		}
	}

	/**
	 * Simple implementation of PropertyHandler interface
	 * 
	 * @author Ole.Matzura
	 */

	public static class SimplePropertyHandler implements PropertyHandler
	{
		private String value;

		public String getValue( DefaultTestStepProperty property )
		{
			return value;
		}

		public void setValue( DefaultTestStepProperty property, String value )
		{
			this.value = value;
		}
	}

	public QName getType()
	{
		return XmlString.type.getName();
	}

	public ModelItem getModelItem()
	{
		return testStep;
	}

	public String getDefaultValue()
	{
		return null;
	}
}
