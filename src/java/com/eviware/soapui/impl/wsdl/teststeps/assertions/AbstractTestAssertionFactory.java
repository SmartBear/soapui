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

package com.eviware.soapui.impl.wsdl.teststeps.assertions;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import com.eviware.soapui.config.RequestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.WsdlAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;

public abstract class AbstractTestAssertionFactory implements TestAssertionFactory
{
	private final String id;
	private final String label;
	private final Class<? extends TestAssertion> assertionClass;
	private final Class<? extends ModelItem> targetClass;

	public AbstractTestAssertionFactory( String id, String label, Class<? extends TestAssertion> assertionClass )
	{
		this.id = id;
		this.label = label;
		this.assertionClass = assertionClass;
		targetClass = null;
	}
	
	public AbstractTestAssertionFactory( String id, String label, Class<? extends TestAssertion> assertionClass, Class<? extends ModelItem> targetClass )
	{
		this.id = id;
		this.label = label;
		this.assertionClass = assertionClass;
		this.targetClass = targetClass;
	}
	
	public String getAssertionId()
	{
		return id;
	}

	public String getAssertionLabel()
	{
		return label;
	}

	@SuppressWarnings("deprecation")
	public boolean canAssert( Assertable assertable )
	{
		List<Class<?>> classes = Arrays.asList(assertable.getClass().getClasses());
		
		if( targetClass != null && !classes.contains(targetClass))
			return false;
				
		if( assertable.getAssertableType() == AssertableType.BOTH )
			return true;
		
		if(assertable.getAssertableType() == AssertableType.REQUEST && classes.contains( RequestAssertion.class )) 
			return true;

		else if(assertable.getAssertableType() == AssertableType.RESPONSE && classes.contains( ResponseAssertion.class )) 
			return true;
		
		return false;
	}

	public TestAssertion buildAssertion(RequestAssertionConfig config, Assertable assertable)
	{
		try
		{
			Constructor<? extends TestAssertion> ctor = assertionClass
			.getConstructor(new Class[] { RequestAssertionConfig.class,
					Assertable.class });

			return ctor.newInstance(config, assertable);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
