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

package com.eviware.soapui.impl.rest.actions.service;

import java.io.File;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.support.definition.export.WadlDefinitionExporter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Exports the definition (wsdls and xsds) of a WsdlInterface to the file system
 * 
 * @author Ole.Matzura
 */

@SuppressWarnings( "unchecked" )
public class ExportWadlAction extends AbstractSoapUIAction<RestService>
{
	public static final String SOAPUI_ACTION_ID = "ExportWadlAction";

	public ExportWadlAction()
	{
		super( "Export WADL", "Exports the entire WADL and included/imported files to a local directory" );
	}

	public void perform( RestService iface, Object param )
	{
		try
		{
			String path = exportDefinition( null, iface );
			if( path != null )
			{
				UISupport.showInfoMessage( "WADL exported succesfully to [" + path + "]", "Export WADL" );
			}
		}
		catch( Exception e1 )
		{
			UISupport.showErrorMessage( e1 );
		}
	}

	public String exportDefinition( String location, RestService iface ) throws Exception
	{
		File folderName = location == null ? UISupport.getFileDialogs().openDirectory( this, "Select output directory",
				null ) : new File( location );

		if( folderName == null )
			return null;

		WadlDefinitionExporter exporter = new WadlDefinitionExporter( iface );
		return exporter.export( folderName.getAbsolutePath() );
	}
}