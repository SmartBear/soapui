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

package com.eviware.soapui.impl.support.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import com.eviware.soapui.support.HelpActionMarker;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;

/**
 * Shows an online help page
 * 
 * @author Ole.Matzura
 */

public class ShowOnlineHelpAction extends AbstractAction implements HelpActionMarker
{
	private final String url;

	public ShowOnlineHelpAction( String url )
	{
		this( "Online Help", url, UISupport.getKeyStroke( "F1" ) );
	}

	public ShowOnlineHelpAction( String title, String url )
	{
		this( title, url, null, null, null );
	}

	public ShowOnlineHelpAction( String title, String url, String description )
	{
		this( title, url, null, description, null );
	}

	public ShowOnlineHelpAction( String title, String url, String description, String iconPath )
	{
		this( title, url, null, description, iconPath );
	}

	public ShowOnlineHelpAction( String title, String url, KeyStroke accelerator )
	{
		this( title, url, accelerator, null );
	}

	public ShowOnlineHelpAction( String title, String url, KeyStroke accelerator, String description )
	{
		this( title, url, accelerator, description, null );
	}

	public ShowOnlineHelpAction( String title, String url, KeyStroke accelerator, String description, String iconPath )
	{
		super( title );
		this.url = url;
		putValue( Action.SHORT_DESCRIPTION, description == null ? "Show online help" : description );
		if( accelerator != null )
			putValue( Action.ACCELERATOR_KEY, accelerator );

		putValue( Action.SMALL_ICON, iconPath == null ? UISupport.HELP_ICON : UISupport.createImageIcon( iconPath ) );
	}

	public void actionPerformed( ActionEvent e )
	{
		if( url == null )
			UISupport.showErrorMessage( "Missing help URL" );
		else
			Tools.openURL( url );
	}
}
