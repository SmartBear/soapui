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

package com.eviware.soapui.model;

import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.components.SimpleForm;

/**
 * Behavior for an implementation of the soapui core model
 * 
 * @author Ole.Matzura
 */

public interface ModelImplementation
{
	public SimpleForm[] getOptions();

	public ActionList getActions();

	public Project buildProject( String path );
}
