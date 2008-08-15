/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.actions.request;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.support.AbstractAddToTestCaseAction;
import com.eviware.x.form.XFormDialog;

/**
 * Adds a WsdlRequest to a WsdlTestCase as a WsdlTestRequestStep
 * 
 * @author Ole.Matzura
 */

public class AddRestRequestToTestCaseAction extends AbstractAddToTestCaseAction<RestRequest>
{
	public static final String SOAPUI_ACTION_ID = "AddRestRequestToTestCaseAction";
	
	private XFormDialog dialog;

	public AddRestRequestToTestCaseAction()
   {
      super( "Add to TestCase", "Adds this request to a TestCase" );
   }

   public void perform( RestRequest request, Object param )
	{
      WsdlProject project = request.getOperation().getInterface().getProject();
      
	}   
}
