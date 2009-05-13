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

package com.eviware.soapui.model.propertyexpansion.resolvers.providers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.resolvers.DynamicPropertyResolver.ValueProvider;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.workspace.Workspace;

public class WorkspaceDirProvider implements ValueProvider
{
	public String getValue( PropertyExpansionContext context )
	{
		Workspace workspace = SoapUI.getWorkspace();

		if( workspace == null )
		{
			ModelItem modelItem = context.getModelItem();
			if( modelItem instanceof Workspace )
			{
				workspace = ( Workspace )modelItem;
			}
			else
			{
				Project project = ModelSupport.getModelItemProject( modelItem );
				if( project != null )
					workspace = project.getWorkspace();
			}
		}

		return workspace == null ? null : PathUtils.getAbsoluteFolder( workspace.getPath() );
	}
}