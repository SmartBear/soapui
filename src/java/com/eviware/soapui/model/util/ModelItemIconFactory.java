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

package com.eviware.soapui.model.util;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.UISupport;

public class ModelItemIconFactory
{
	private static Map<Class<? extends ModelItem>, String> modelItemIcons = new HashMap<Class<? extends ModelItem>, String>();

	static
	{
		modelItemIcons.put( Project.class, "/project.gif" );
		modelItemIcons.put( TestSuite.class, "/testSuite.gif" );
		modelItemIcons.put( TestCase.class, "/testCase.gif" );
		modelItemIcons.put( TestStep.class, "/teststeps.gif" );
		modelItemIcons.put( TestAssertion.class, "/assertion.gif" );
		modelItemIcons.put( LoadTest.class, "/loadTest.gif" );
		modelItemIcons.put( MockService.class, "/mockService.gif" );
		modelItemIcons.put( MockResponse.class, "/mockResponse.gif" );
		modelItemIcons.put( MockOperation.class, "/mockOperation.gif" );
		modelItemIcons.put( Request.class, "/request.gif" );
		modelItemIcons.put( Operation.class, "/operation.gif" );
		modelItemIcons.put( Interface.class, "/interface.gif" );
		
	}

	public static ImageIcon getIcon( Class<? extends ModelItem> clazz )
	{
		if( modelItemIcons.containsKey( clazz ) )
			return UISupport.createImageIcon( modelItemIcons.get( clazz ) );

		for( Class<?> iface : clazz.getInterfaces() )
		{
			if( modelItemIcons.containsKey( iface ) )
				return UISupport.createImageIcon( modelItemIcons.get( iface ) );
		}

		return null;
	}

	@SuppressWarnings( "unchecked" )
	public static String getIconPath( Class<? extends ModelItem> clazz )
	{
		if( modelItemIcons.containsKey( clazz ) )
			return modelItemIcons.get( clazz );

		for( Class<?> iface : clazz.getInterfaces() )
		{
			if( modelItemIcons.containsKey( iface ) )
				return modelItemIcons.get( iface );
		}

		while( clazz.getSuperclass() != null && ModelItem.class.isAssignableFrom( clazz.getSuperclass() ) )
		{
			return getIconPath( ( Class<? extends ModelItem> )clazz.getSuperclass() );
		}

		return null;
	}

	public static <T extends ModelItem> String getIconPath( T modelItem )
	{
		return getIconPath( modelItem.getClass() );
	}

	public static <T extends ModelItem> ImageIcon getIcon( T modelItem )
	{
		return getIcon( modelItem.getClass() );
	}
}
