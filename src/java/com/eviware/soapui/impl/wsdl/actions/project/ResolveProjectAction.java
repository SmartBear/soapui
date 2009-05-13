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

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.resolver.ResolveDialog;

/**
 * Renames a WsdlProject
 * 
 * @author Ole.Matzura
 */

public class ResolveProjectAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "ResolveProjectAction";

	private ResolveDialog dialog;

	public ResolveProjectAction()
	{
		super( "Resolve", "Resolve item dependencies in this project" );
	}

	public void perform( WsdlProject project, Object param )
	{
		if( dialog == null )
		{
			dialog = new ResolveDialog( getName(), getDescription(), null );
			dialog.setShowOkMessage( true );
		}

		dialog.resolve( project );
	}
}
