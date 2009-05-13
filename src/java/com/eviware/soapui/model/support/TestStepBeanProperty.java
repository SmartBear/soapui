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

import org.apache.commons.beanutils.PropertyUtils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;

/**
 * TestStepProperty implementation that maps to a standard javabean property
 * 
 * @author Ole.Matzura
 */

public class TestStepBeanProperty extends DefaultTestStepProperty
{
	public TestStepBeanProperty( String name, boolean isReadOnly, Object targetObject, String targetName,
			WsdlTestStep testStep )
	{
		super( name, isReadOnly, new BeanPropertyHandler( targetObject, targetName ), testStep );
	}

	/**
	 * PropertyHandler for setting/getting bean properties
	 * 
	 * @author Ole.Matzura
	 */

	public static class BeanPropertyHandler implements PropertyHandler
	{
		private final Object target;
		private final String targetName;

		public BeanPropertyHandler( Object targetObject, String targetName )
		{
			this.target = targetObject;
			this.targetName = targetName;
		}

		public String getValue( DefaultTestStepProperty prop )
		{
			try
			{
				Object property = PropertyUtils.getProperty( target, targetName );
				return property == null ? null : property.toString();
			}
			catch( Exception e )
			{
				if( target instanceof ModelItem )
				{
					SoapUI.logError( new Exception( "Error getting property [" + targetName + "] from modelItem ["
							+ ( ( ModelItem )target ).getName() + "]", e ) );
				}
				else
				{
					SoapUI.logError( new Exception(
							"Error getting property [" + targetName + "] from bean [" + target + "]", e ) );
				}

				return null;
			}
		}

		public void setValue( DefaultTestStepProperty property, String value )
		{
			try
			{
				PropertyUtils.setProperty( target, targetName, value );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}
	}

}
