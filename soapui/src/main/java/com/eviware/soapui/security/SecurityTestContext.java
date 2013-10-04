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

package com.eviware.soapui.security;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;

/**
 * SecurityTestContext implementation for SecurityTests not active - just left
 * in case needed later
 * 
 * @author SoapUI team
 */

public class SecurityTestContext extends DefaultPropertyExpansionContext
// implements SecurityTestRunContext
{

	public SecurityTestContext( ModelItem modelItem )
	{
		super( modelItem );
		// TODO Auto-generated constructor stub
	}
	// private final SecurityTestRunner runner;
	//
	// public SecurityTestContext( SecurityTestRunner runner )
	// {
	// super( runner.getSecurityTest().getTestCase() );
	// this.runner = runner;
	// }
	//
	// public SecurityTestRunner getSecurityTestRunner()
	// {
	// return runner;
	// }
	//
	// @Override
	// public Object get( Object key )
	// {
	// if( "securityTestRunner".equals( key ) )
	// return runner;
	//
	// return super.get( key );
	// }
	//
	// public Object getProperty( String testStep, String propertyName )
	// {
	// return null;
	// }
	//
	// public TestCaseRunner getTestRunner()
	// {
	// return null;
	// }
}
