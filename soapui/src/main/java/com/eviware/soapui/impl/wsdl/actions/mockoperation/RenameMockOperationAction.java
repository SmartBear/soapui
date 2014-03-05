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

package com.eviware.soapui.impl.wsdl.actions.mockoperation;

import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.support.AbstractMockOperation;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlMockOperation
 * 
 * @author Ole.Matzura
 */

public class RenameMockOperationAction extends AbstractSoapUIAction<AbstractMockOperation>
{
	public RenameMockOperationAction()
	{
		super( "Rename", "Renames this node" );
	}

	public void perform( AbstractMockOperation mockOperation, Object param )
	{
		String nodeName = mockOperation instanceof RestMockAction ? "RestMockAction" : "MockOperation";
		String name = UISupport.prompt( "Specify name of " + nodeName, "Rename " + nodeName, mockOperation.getName() );
		if( name == null || name.equals( mockOperation.getName() ) )
			return;

		mockOperation.setName( name );
	}

}
