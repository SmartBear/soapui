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

package com.eviware.soapui.impl.wsdl.actions.testcase;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * SoapUIActionGroup for WsdlTestSteps
 * 
 * @author ole.matzura
 */

public class WsdlTestCaseSoapUIActionGroup extends DefaultSoapUIActionGroup<WsdlTestCase>
{
	public WsdlTestCaseSoapUIActionGroup( String id, String name )
	{
		super( id, name );
	}

	public SoapUIActionMappingList<WsdlTestCase> getActionMappings( WsdlTestCase modelItem )
	{
		SoapUIActionMappingList<WsdlTestCase> actions = super.getActionMappings( modelItem );
		SoapUIActionMapping<WsdlTestCase> toggleDisabledActionMapping = null;

		for( int c = 0; c < actions.size(); c++ )
		{
			if( actions.get( c ).getActionId().equals( ToggleDisableTestCaseAction.SOAPUI_ACTION_ID ) )
			{
				toggleDisabledActionMapping = actions.get( c );
				break;
			}
		}

		if( toggleDisabledActionMapping != null )
		{
			if( modelItem.isDisabled() )
			{
				toggleDisabledActionMapping.setName( "Enable TestCase" );
				toggleDisabledActionMapping.setDescription( "Enable this TestCase" );
			}
			else
			{
				toggleDisabledActionMapping.setName( "Disable TestCase" );
				toggleDisabledActionMapping.setDescription( "Disables this TestCase" );
			}
		}

		return actions;
	}
}
