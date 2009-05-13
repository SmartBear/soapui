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

package com.eviware.soapui.impl.actions.multi;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIMultiAction;

public class MultiTestStepDeleteAction extends AbstractSoapUIMultiAction<ModelItem>
{
	public static final String SOAPUI_ACTION_ID = "MultiTestStepDeleteAction";

	public MultiTestStepDeleteAction()
	{
		super( SOAPUI_ACTION_ID, "Delete", "Delete selected items" );
	}

	public void perform( ModelItem[] targets, Object param )
	{
		if( UISupport.confirm( "Delete selected Test Steps?", "Delete Items" ) )
		{
			for( ModelItem target : targets )
			{
				( ( WsdlTestStep )target ).getTestCase().removeTestStep( ( WsdlTestStep )target );
			}
		}
	}

	public boolean applies( ModelItem target )
	{
		return( target instanceof WsdlTestStep );
	}
}
