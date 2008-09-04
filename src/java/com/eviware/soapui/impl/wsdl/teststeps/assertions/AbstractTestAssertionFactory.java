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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;

public abstract class AbstractTestAssertionFactory implements TestAssertionFactory
{
	private final String id;
	private final String label;
	private final Class<? extends TestAssertion> assertionClass;
	private final Class<? extends ModelItem> targetClass;

	public AbstractTestAssertionFactory(String id, String label, Class<? extends TestAssertion> assertionClass)
	{
		this.id = id;
		this.label = label;
		this.assertionClass = assertionClass;
		targetClass = null;
	}

	public AbstractTestAssertionFactory(String id, String label, Class<? extends TestAssertion> assertionClass,
			Class<? extends ModelItem> targetClass)
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

	public boolean canAssert(Assertable assertable)
	{
		List<Class<?>> classes = Arrays.asList(assertionClass.getInterfaces());

      List<Class> classList = getImplementedAndExtendedClasses( assertable );
      if (targetClass != null && !classList.contains(targetClass))
			return false;

		if (assertable.getAssertableType() == AssertableType.BOTH)
			return true;

		if (assertable.getAssertableType() == AssertableType.REQUEST
				&& classes.contains(com.eviware.soapui.model.testsuite.RequestAssertion.class))
			return true;

		else if (assertable.getAssertableType() == AssertableType.RESPONSE
				&& classes.contains(com.eviware.soapui.model.testsuite.ResponseAssertion.class))
			return true;

		return false;
	}

   private List<Class> getImplementedAndExtendedClasses( Object obj )
   {
      ArrayList<Class> result = new ArrayList<Class>();
      addImplementedAndExtendedClasses( obj.getClass(), result );
      return result;
   }

   private void addImplementedAndExtendedClasses( Class clazz, ArrayList<Class> result )
   {
      result.add( clazz );
//      result.addAll( Arrays.asList( clazz.getInterfaces() ));
      addImplementedInterfaces(clazz, result);
      if( clazz.getSuperclass() != null )
      {
          addImplementedAndExtendedClasses( clazz.getSuperclass(), result );
      }
   }

   private void addImplementedInterfaces( Class intrfc, ArrayList<Class> result )
   {
//      result.add( intrfc.getClass() );
      Class<?> [] interfacesArray = intrfc.getInterfaces();
      if( interfacesArray.length > 0 )
      {
         result.addAll( Arrays.asList( interfacesArray ));
         for (int i = 0; i < interfacesArray.length; i++)
			{
				Class<?> class1 = interfacesArray[i];
				addImplementedInterfaces( class1, result );
			}
      }
   }

   public TestAssertion buildAssertion(TestAssertionConfig config, Assertable assertable)
	{
		try
		{
			Constructor<? extends TestAssertion> ctor = assertionClass.getConstructor(new Class[] {
					TestAssertionConfig.class, Assertable.class });

			return ctor.newInstance(config, assertable);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
