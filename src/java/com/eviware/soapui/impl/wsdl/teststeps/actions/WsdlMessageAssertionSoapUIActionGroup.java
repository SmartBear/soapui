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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.support.DefaultActionMapping;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * SoapUIActionGroup for a WsdlMessageAssertion
 * 
 * @author ole.matzura
 */

public class WsdlMessageAssertionSoapUIActionGroup extends DefaultSoapUIActionGroup<WsdlMessageAssertion>
{
	public WsdlMessageAssertionSoapUIActionGroup( String id, String name )
	{
		super( id, name );
	}

	@Override
	public SoapUIActionMappingList<WsdlMessageAssertion> getActionMappings( WsdlMessageAssertion modelItem )
	{
		SoapUIActionMappingList<WsdlMessageAssertion> actions = super.getActionMappings( modelItem );
		SoapUIActionMappingList<WsdlMessageAssertion> result = new SoapUIActionMappingList<WsdlMessageAssertion>( actions );

		if( modelItem.isConfigurable() )
		{
			DefaultActionMapping<WsdlMessageAssertion> actionMapping = new DefaultActionMapping<WsdlMessageAssertion>(
					ConfigureAssertionAction.SOAPUI_ACTION_ID, "ENTER", null, true, null );

			actionMapping.setName( "Configure" );
			actionMapping.setDescription( "Configures this Assertion" );

			result.add( 0, actionMapping );
		}

		if( modelItem.isClonable() )
		{
			DefaultActionMapping<WsdlMessageAssertion> actionMapping = new DefaultActionMapping<WsdlMessageAssertion>(
					CloneAssertionAction.SOAPUI_ACTION_ID, "F9", null, true, null );

			result.add( 1, actionMapping );
		}

		// result.add( 1, SeperatorAction.getDefaultMapping() );

		SoapUIActionMapping<WsdlMessageAssertion> toggleDisabledActionMapping = null;
		for( int c = 0; c < result.size(); c++ )
		{
			if( result.get( c ).getActionId().equals( ToggleDisableAssertionAction.SOAPUI_ACTION_ID ) )
			{
				toggleDisabledActionMapping = result.get( c );
				break;
			}
		}

		if( toggleDisabledActionMapping != null )
		{
			if( modelItem.isDisabled() )
			{
				toggleDisabledActionMapping.setName( "Enable" );
				toggleDisabledActionMapping.setDescription( "Enable this Assertion" );
			}
			else
			{
				toggleDisabledActionMapping.setName( "Disable" );
				toggleDisabledActionMapping.setDescription( "Disables this Assertion" );
			}
		}

		return result;
	}
}
