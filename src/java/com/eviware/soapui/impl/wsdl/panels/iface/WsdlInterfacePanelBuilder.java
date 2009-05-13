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

package com.eviware.soapui.impl.wsdl.panels.iface;

import java.awt.Component;

import com.eviware.soapui.config.AnonymousTypeConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for WsdlInterface
 * 
 * @author Ole.Matzura
 */

public class WsdlInterfacePanelBuilder extends EmptyPanelBuilder<WsdlInterface>
{
	public WsdlInterfacePanelBuilder()
	{
	}

	public Component buildOverviewPanel( WsdlInterface iface )
	{
		JPropertiesTable<WsdlInterface> table = new JPropertiesTable<WsdlInterface>( "Interface Properties" );
		table.addProperty( "PortType", "name" );
		table.addProperty( "Description", "description", true );
		table.addProperty( "Definition URL", "definition", true );
		table.addProperty( "Binding", "bindingName" );
		table.addProperty( "SOAP Version", "soapVersion", new Object[] { SoapVersion.Soap11, SoapVersion.Soap12 } );
		table.addProperty( "Cached", "cached", false );
		table.addProperty( "Style", "style", false );
		// TODO extract info from wsdl if by default ws addresing is implemented
		table.addProperty( "WS-A version", "wsaVersion", new Object[] { WsaVersionTypeConfig.NONE.toString(),
				WsaVersionTypeConfig.X_200408.toString(), WsaVersionTypeConfig.X_200508.toString() } );
		table.addProperty( "WS-A anonymous", "anonymous", new Object[] { AnonymousTypeConfig.OPTIONAL.toString(),
				AnonymousTypeConfig.REQUIRED.toString(), AnonymousTypeConfig.PROHIBITED.toString() } );

		table.setPropertyObject( iface );

		return table;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}

	public DesktopPanel buildDesktopPanel( WsdlInterface iface )
	{
		return new WsdlInterfaceDesktopPanel( iface );
	}

	public boolean hasDesktopPanel()
	{
		return true;
	}
}
