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

package com.eviware.soapui.model.util;

import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.UISupport;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class ModelItemIconFactory
{
	private static Map<Class<? extends ModelItem>, String> modelItemIcons = new HashMap<Class<? extends ModelItem>, String>();

	static
	{
		// the "class" keys here are only used for lookup - but must be implementations of ModelItem
		// the icon is used in the project overview
		modelItemIcons.put( Project.class, WsdlProject.ICON_NAME );
		modelItemIcons.put( TestSuite.class, WsdlTestSuite.ICON_NAME );
		modelItemIcons.put( TestCase.class, WsdlTestCase.ICON_NAME );
		modelItemIcons.put( LoadTest.class, WsdlLoadTest.ICON_NAME );
		modelItemIcons.put( MockService.class, WsdlMockService.ICON_NAME );
		modelItemIcons.put( MockResponse.class, WsdlMockResponse.ICON_NAME );
		modelItemIcons.put( MockOperation.class, WsdlMockOperation.ICON_NAME );
		modelItemIcons.put( RestMockService.class, RestMockService.ICON_NAME );
		modelItemIcons.put( RestMockAction.class, RestMockAction.getDefaultIcon() );
		modelItemIcons.put( RestMockResponse.class, RestMockResponse.ICON_NAME );
		modelItemIcons.put( Operation.class, WsdlOperation.ICON_NAME );
		modelItemIcons.put( SecurityTest.class, SecurityTest.ICON_NAME );

		// the following use different icon files for the overview and in the tree
		modelItemIcons.put( TestStep.class, "/teststeps.gif" );
		modelItemIcons.put( TestAssertion.class, "/assertion.gif" );
		modelItemIcons.put( Request.class, "/request.gif" );
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
