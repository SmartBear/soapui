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

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.TestStep;

/**
 * Default implementation
 */

public class WsdlSubmitContext extends AbstractSubmitContext
{
	private final TestStep step;

	public WsdlSubmitContext( ModelItem context )
	{
		super( context );
		step = context instanceof TestStep ? ( TestStep )context : null;
	}

	public Object getProperty( String name )
	{
		return getProperty( name, step, ( WsdlTestCase )( step == null ? null : step.getTestCase() ) );
	}

	@Override
	public Object get( Object key )
	{
		if( "settings".equals( key ) )
			return getSettings();

		return getProperty( key.toString() );
	}

	@Override
	public Object put( String key, Object value )
	{
		Object oldValue = get( key );
		setProperty( key, value );
		return oldValue;
	}

	public Settings getSettings()
	{
		return step == null ? step.getSettings() : null;
	}

	public String expand( String content )
	{
		return PropertyExpander.expandProperties( this, content );
	}
}
