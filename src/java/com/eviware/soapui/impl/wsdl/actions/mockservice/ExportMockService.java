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
package com.eviware.soapui.impl.wsdl.actions.mockservice;

import java.io.File;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class ExportMockService extends AbstractSoapUIAction<WsdlMockService>
{

	public ExportMockService()
	{
		super( "Export", "Export this mock service" );
	}

	public void perform(WsdlMockService mService, Object param)
	{
		mService.beforeSave();
		String defaultFileName = System.getProperty( "user.home" ) + File.separator + mService.getName() + ".xml";
		File file = UISupport.getFileDialogs().saveAs( this, "Select test case file", "xml", "XML",
				new File( defaultFileName ) );

		if( file == null )
			return;

		String fileName = file.getAbsolutePath();
		if( fileName == null )
			return;

		mService.export( file );
	}

}
