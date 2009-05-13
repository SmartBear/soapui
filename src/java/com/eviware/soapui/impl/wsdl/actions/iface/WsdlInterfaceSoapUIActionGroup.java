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

package com.eviware.soapui.impl.wsdl.actions.iface;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * ActionGroup handler for WsdlInterface
 * 
 * @author ole.matzura
 */

public class WsdlInterfaceSoapUIActionGroup extends DefaultSoapUIActionGroup<WsdlInterface>
{
	public WsdlInterfaceSoapUIActionGroup( String id, String name )
	{
		super( id, name );
	}

	@Override
	public SoapUIActionMappingList<WsdlInterface> getActionMappings( WsdlInterface modelItem )
	{
		SoapUIActionMappingList<WsdlInterface> actionMappings = super.getActionMappings( modelItem );

		// SoapUIActionMapping<WsdlInterface> mapping = actionMappings.getMapping(
		// WSToolsRegenerateJava2WsdlAction.SOAPUI_ACTION_ID );
		// WSToolsRegenerateJava2WsdlAction action = (
		// WSToolsRegenerateJava2WsdlAction ) mapping.getAction();
		// mapping.setEnabled( modelItem.getSettings().isSet(
		// action.getValuesSettingID() ) );

		return actionMappings;
	}
}
