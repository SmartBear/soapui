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
package com.eviware.soapui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.eviware.soapui.support.SoapUIVersionUpdate;

public class VersionUpdateAction extends AbstractAction
{

	public VersionUpdateAction()
	{
		super( "Check for updates" );
		putValue( Action.SHORT_DESCRIPTION, "Checks if newer version is available" );
	}

	@Override
	public void actionPerformed( ActionEvent e )
	{
		new SoapUIVersionUpdate().checkForNewVersion( true );
	}

}
