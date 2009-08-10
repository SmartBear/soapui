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

package com.eviware.soapui.impl.rest.actions.method;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Deletes a RestMethod from its containing Resource
 * 
 * @author Dain Nilsson
 */

public class DeleteRestMethodAction extends AbstractSoapUIAction<RestMethod>
{
	public DeleteRestMethodAction()
	{
		super( "Delete", "Deletes this Method" );
	}

	public void perform( RestMethod method, Object param )
	{
		if( UISupport.confirm( "Delete Method [" + method.getName() + "] from [" + method.getOperation().getName() + "]",
				"Delete Method" ) )
		{
			method.getOperation().deleteMethod( method );
		}
	}
}
