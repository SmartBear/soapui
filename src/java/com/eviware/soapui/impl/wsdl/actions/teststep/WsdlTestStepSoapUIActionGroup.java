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

package com.eviware.soapui.impl.wsdl.actions.teststep;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.support.ShowDesktopPanelAction;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.support.action.SoapUIActionGroup;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.support.DefaultActionMapping;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * SoapUIActionGroup for WsdlTestSteps
 * 
 * @author ole.matzura
 */

public class WsdlTestStepSoapUIActionGroup extends DefaultSoapUIActionGroup<WsdlTestStep>
{
	private boolean initialized;

	public WsdlTestStepSoapUIActionGroup( String id, String name )
	{
		super( id, name );
	}

	public SoapUIActionMappingList<WsdlTestStep> getActionMappings( WsdlTestStep modelItem )
	{
		SoapUIActionMappingList<WsdlTestStep> actions = super.getActionMappings( modelItem );
		SoapUIActionMapping<WsdlTestStep> toggleDisabledActionMapping = null;

		if( !initialized )
		{
			int insertIndex = 0;

			// add open-editor action
			if( modelItem.hasEditor() )
			{
				DefaultActionMapping<WsdlTestStep> actionMapping = new DefaultActionMapping<WsdlTestStep>(
						ShowDesktopPanelAction.SOAPUI_ACTION_ID, "ENTER", null, true, null );

				actionMapping.setName( "Open Editor" );
				actionMapping.setDescription( "Opens the editor for this TestStep" );

				actions.add( 0, actionMapping );
				insertIndex++ ;
			}

			toggleDisabledActionMapping = new DefaultActionMapping<WsdlTestStep>(
					ToggleDisableTestStepAction.SOAPUI_ACTION_ID, null, null, false, null );

			actions.add( insertIndex, toggleDisabledActionMapping );
			insertIndex++ ;

			// add default teststep actions
			SoapUIActionGroup<WsdlTestStep> actionGroup = SoapUI.getActionRegistry()
					.getActionGroup( "WsdlTestStepActions" );
			if( actionGroup != null )
			{
				actions.addAll( insertIndex, actionGroup.getActionMappings( modelItem ) );
			}

			initialized = true;
		}
		else
		{
			for( int c = 0; c < actions.size(); c++ )
			{
				if( actions.get( c ).getActionId().equals( ToggleDisableTestStepAction.SOAPUI_ACTION_ID ) )
				{
					toggleDisabledActionMapping = actions.get( c );
					break;
				}
			}
		}

		if( toggleDisabledActionMapping != null )
		{
			if( modelItem.isDisabled() )
			{
				toggleDisabledActionMapping.setName( "Enable TestStep" );
				toggleDisabledActionMapping.setDescription( "Enable this TestStep during TestCase execution" );
			}
			else
			{
				toggleDisabledActionMapping.setName( "Disable TestStep" );
				toggleDisabledActionMapping.setDescription( "Disables this TestStep during TestCase execution" );
			}
		}

		return actions;
	}
}
