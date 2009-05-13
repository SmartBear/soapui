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

package com.eviware.soapui.impl.wsdl.actions.testsuite;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * SoapUIActionGroup for WsdlTestSteps
 * 
 * @author ole.matzura
 */

public class WsdlTestSuiteSoapUIActionGroup extends DefaultSoapUIActionGroup<WsdlTestSuite>
{
	public WsdlTestSuiteSoapUIActionGroup( String id, String name )
	{
		super( id, name );
	}

	public SoapUIActionMappingList<WsdlTestSuite> getActionMappings( WsdlTestSuite modelItem )
	{
		SoapUIActionMappingList<WsdlTestSuite> actions = super.getActionMappings( modelItem );
		SoapUIActionMapping<WsdlTestSuite> toggleDisabledActionMapping = null;

		for( int c = 0; c < actions.size(); c++ )
		{
			if( actions.get( c ).getActionId().equals( ToggleDisableTestSuiteAction.SOAPUI_ACTION_ID ) )
			{
				toggleDisabledActionMapping = actions.get( c );
				break;
			}
		}

		if( toggleDisabledActionMapping != null )
		{
			if( modelItem.isDisabled() )
			{
				toggleDisabledActionMapping.setName( "Enable TestSuite" );
				toggleDisabledActionMapping.setDescription( "Enable this TestSuite" );
			}
			else
			{
				toggleDisabledActionMapping.setName( "Disable TestSuite" );
				toggleDisabledActionMapping.setDescription( "Disables this TestSuite" );
			}
		}

		return actions;
	}
}
