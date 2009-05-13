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

package com.eviware.soapui.impl.wsdl.actions.support;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import com.eviware.soapui.support.HelpActionMarker;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;

/**
 * Opens a URL in the external browser
 * 
 * @author Ole.Matzura
 */

public class OpenUrlAction extends AbstractAction implements HelpActionMarker
{
	private final String url;

	public OpenUrlAction( String title, String url )
	{
		this( title, url, null );
	}

	public OpenUrlAction( String title, String url, KeyStroke accelerator )
	{
		super( title );
		this.url = url;

		putValue( Action.SHORT_DESCRIPTION, title );
		if( accelerator != null )
			putValue( Action.ACCELERATOR_KEY, accelerator );
	}

	public void actionPerformed( ActionEvent e )
	{
		if( url == null )
			UISupport.showErrorMessage( "Missing url" );
		else
			Tools.openURL( url );
	}
}
