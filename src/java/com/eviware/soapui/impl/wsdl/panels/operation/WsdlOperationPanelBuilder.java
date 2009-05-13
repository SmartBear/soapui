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

package com.eviware.soapui.impl.wsdl.panels.operation;

import javax.swing.JPanel;

import com.eviware.soapui.config.AnonymousTypeConfig;
import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.support.components.JPropertiesTable;

/**
 * PanelBuilder for WsdlOperation. Only builds an overview panel.
 * 
 * @author Ole.Matzura
 */

public class WsdlOperationPanelBuilder extends EmptyPanelBuilder<WsdlOperation>
{

	public WsdlOperationPanelBuilder()
	{
	}

	public JPanel buildOverviewPanel( WsdlOperation operation )
	{
		JPropertiesTable<WsdlOperation> table = new JPropertiesTable<WsdlOperation>( "Operation Properties" );
		table.addProperty( "Description", "description", true );
		table.addProperty( "SOAPAction", "action" );
		table.addProperty( "Operation", "bindingOperationName" );
		table.addProperty( "Style", "style" );
		table.addProperty( "Type", "type" );
		table.addProperty( "Input", "inputName" );
		table.addProperty( "Output", "outputName" );
		table.addProperty( "Sends Attachments", "sendsAttachments" );
		table.addProperty( "Receives Attachments", "receivesAttachments" );
		table.addProperty( "WS-A anonymous", "anonymous", new Object[] { AnonymousTypeConfig.OPTIONAL.toString(),
				AnonymousTypeConfig.REQUIRED.toString(), AnonymousTypeConfig.PROHIBITED.toString() } );
		table.setPropertyObject( operation );

		return table;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}
}
