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

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Selects the specified WsdlTestRequestSteps operation in the navigator
 * 
 * @author Ole.Matzura
 */

public class SelectOperationAction extends AbstractSoapUIAction<WsdlTestRequestStep>
{
	public SelectOperationAction()
	{
		super( "Select Operation", "Selects this TestRequests' Operation in the navigator" );
	}

	public void perform( WsdlTestRequestStep target, Object param )
	{
		UISupport.select( target.getTestRequest().getOperation() );
	}
}
