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

package com.eviware.soapui.impl.wsdl.actions.operation;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Changes the label of a WsdlOperation as shown in SoapUI
 * 
 * @author Ole.Matzura
 */

public class RelabelOperationAction extends AbstractSoapUIAction<WsdlOperation>
{
	public RelabelOperationAction()
	{
		super( "Relabel", "Relabel this operation" );
	}

	public void perform( WsdlOperation operation, Object param )
	{
		String name = UISupport.prompt( "Specify label for operation\n(will not change underlying wsdl operation name)",
				"Relabel Operation", operation.getName() );
		if( name == null || name.equals( operation.getName() ) )
			return;

		operation.setName( name );
	}
}
