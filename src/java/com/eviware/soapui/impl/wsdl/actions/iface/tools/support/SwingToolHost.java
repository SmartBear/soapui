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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.support;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;

/**
 * Swing-based ToolHost
 * 
 * @author ole.matzura
 */

public class SwingToolHost implements ToolHost
{
	public void run( ToolRunner runner ) throws Exception
	{
		ProcessDialog processDialog = null;

		try
		{
			processDialog = new ProcessDialog( runner.getName(), runner.getDescription(), runner.showLog(), runner
					.canCancel() );
			ModelItem modelItem = runner.getModelItem();
			if( modelItem == null )
				processDialog.log( "Running " + runner.getName() + "\r\n" );
			else
				processDialog.log( "Running " + runner.getName() + " for [" + modelItem.getName() + "]\r\n" );
			processDialog.run( runner );
		}
		catch( Exception ex )
		{
			UISupport.showErrorMessage( ex );
			throw ex;
		}
		finally
		{
			if( processDialog != null )
				processDialog.setVisible( false );
		}
	}
}
