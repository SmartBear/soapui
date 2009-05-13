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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.resolvers.DynamicPropertyResolver.ValueProvider;
import com.eviware.soapui.model.support.ModelSupport;

public class ProjectDirProvider implements ValueProvider
{
	public String getValue( PropertyExpansionContext context )
	{
		Project project = ModelSupport.getModelItemProject( context.getModelItem() );
		if( project != null )
		{
			return getProjectFolder( project );
		}

		return null;
	}

	public static String getProjectFolder( Project project )
	{
		if( project.getPath() != null )
		{
			File file = new File( project.getPath() );
			if( file.exists() )
			{
				return new File( file.getAbsolutePath() ).getParent();
			}
			else
			{
				try
				{
					URL url = new URL( project.getPath() );
					String str = url.getProtocol() + "://" + url.getHost()
							+ ( ( url.getPort() != -1 ? ":" + url.getPort() : "" ) ) + url.getPath();
					int ix = str.lastIndexOf( '/' );
					if( ix != -1 )
						return str.substring( 0, ix );
				}
				catch( MalformedURLException e )
				{
				}
			}
		}

		return null;
	}
}