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

import java.io.IOException;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Saves a WsdlProject
 * 
 * @author Ole.Matzura
 */

public class SaveProjectAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "SaveProjectAction";

	public SaveProjectAction()
	{
		super( "Save Project", "Saves this project" );
	}

	public void perform( WsdlProject project, Object param )
	{
		try
		{
			if( StringUtils.hasContent( project.getPath() ) || project.getWorkspace() == null )
				project.save();
			else
				project.save( project.getWorkspace().getProjectRoot() );
		}
		catch( IOException e1 )
		{
			UISupport.showErrorMessage( "Failed to save project; " + e1 );
		}
	}
}
