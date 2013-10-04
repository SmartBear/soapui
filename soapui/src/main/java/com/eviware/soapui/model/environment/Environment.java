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

package com.eviware.soapui.model.environment;

import com.eviware.soapui.config.ServiceConfig;
import com.eviware.soapui.model.project.Project;

public interface Environment
{

	public void setProject( Project project );

	public Project getProject();

	public void release();

	public Service addNewService( String name, ServiceConfig.Type.Enum serviceType );

	public void removeService( Service service );

	public String getName();

	public Property addNewProperty( String name, String value );

	public void removeProperty( Property property );

	public void changePropertyName( String name, String value );

	public void moveProperty( String name, int idx );
	
	public void setName(String name);

}
