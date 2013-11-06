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

package com.eviware.soapui.testondemand;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.impl.wsdl.support.InterfaceExternalDependency;
import com.eviware.soapui.impl.wsdl.support.MockServiceExternalDependency;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.iface.Interface;

public class DependencyValidator
{

	private File filename;

	public File getFilename()
	{
		return filename;
	}

	/*
	 * make sure all interfaces are loaded
	 */
	protected void loadInterfaces( WsdlProject project )
	{
		for( Interface inf : project.getInterfaceList() )
			try
			{
				if( inf instanceof WsdlInterface )
				{
					( ( WsdlInterface )inf ).getWsdlContext().loadIfNecessary();
				}
				else
				{
					( ( RestService )inf ).getDefinitionContext().loadIfNecessary();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
	}

	/*
	 * save interfaces to project
	 */
	protected void saveProject( WsdlProject project ) throws IOException
	{
		boolean shouldCache = project.isCacheDefinitions();

		try
		{
			File tempFile = File.createTempFile( "project-temp-", ".xml", null );
			// cache interfaces
			if( !shouldCache )
			{
				project.setCacheDefinitions( true );
			}
			project.saveIn( tempFile );
			this.filename = tempFile;
		}
		finally
		{
			project.setCacheDefinitions( shouldCache );
		}

	}

	public boolean isValid( WsdlTestCase testCase )
	{
		if( testCase != null )
		{
			WsdlProject project = testCase.getTestSuite().getProject();

			// perform validations
			validate( project );

			List<ExternalDependency> deps = project.getExternalDependencies();
			for( ExternalDependency d : deps )
			{
				if( !( d instanceof InterfaceExternalDependency || d instanceof MockServiceExternalDependency ) )
				{
					return false;
				}
			}
		}
		return true;
	}

	protected void validate( WsdlProject project )
	{
		loadInterfaces( project );

		try
		{
			saveProject( project );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

}
