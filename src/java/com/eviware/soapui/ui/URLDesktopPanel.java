/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.ui;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.components.BrowserComponent;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

public class URLDesktopPanel extends DefaultDesktopPanel
{
	private BrowserComponent browser;
	private boolean closed;

	public URLDesktopPanel( String title, String description, String url ) throws InterruptedException,
			InvocationTargetException
	{
		super( title, description, new JPanel( new BorderLayout() ) );

		JPanel panel = ( JPanel )getComponent();

		browser = new BrowserComponent( true, false );
		panel.add( browser.getComponent(), BorderLayout.CENTER );

		if( StringUtils.hasContent( url ) )
			navigate( url, null, true );
	}

	public void navigate( String url, String errorUrl, boolean async )
	{
		// if (!browser.isBrowserInitialised()) {
		// browser.initBrowser();
		// }
		if( async )
		{
			SwingUtilities.invokeLater( new Navigator( url, errorUrl ) );
		}
		else
		{
			browser.navigate( url, errorUrl );
		}
	}

	public boolean onClose( boolean canCancel )
	{
		browser.release();
		closed = true;
		return super.onClose( canCancel );
	}

	public boolean isClosed()
	{
		return closed;
	}

	private class Navigator implements Runnable
	{
		private final String url;
		private final String errorUrl;

		public Navigator( String url, String errorUrl )
		{
			this.url = url;
			this.errorUrl = errorUrl;
		}

		public void run()
		{

			browser.navigate( url, errorUrl );

		}
	}
}
