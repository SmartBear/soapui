/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.actions.iface;

import java.io.File;

import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Exports the definition (wsdls and xsds) of a WsdlInterface to the file system
 * 
 * @author Ole.Matzura
 */

@SuppressWarnings("unchecked")
public class ExportDefinitionAction extends AbstractSoapUIAction<AbstractInterface>
{
   public static final String SOAPUI_ACTION_ID = "ExportDefinitionAction";

	public ExportDefinitionAction()
   {
      super("Export Definition", "Exports the entire WSDL and included/imported files to a local directory");
   }
   
   public void perform( AbstractInterface iface, Object param )
	{
     	try
		{
			if( exportDefinition( null, iface ) != null )
				UISupport.showInfoMessage( "Definition exported succesfully" );
		}
		catch (Exception e1)
		{
			UISupport.showErrorMessage( e1 );
		}
   }

	public String exportDefinition( String location, AbstractInterface iface ) throws Exception
	{
		File folderName =location == null ? UISupport.getFileDialogs().openDirectory( this, "Select output directory", null ) 
				: new File( location );
		
     	if( folderName == null )
     		return null; 
		
     	return iface.getDefinitionContext().export(folderName.getAbsolutePath());
	} 
}
