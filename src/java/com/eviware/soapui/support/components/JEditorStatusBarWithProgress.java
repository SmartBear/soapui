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

package com.eviware.soapui.support.components;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JProgressBar;

public class JEditorStatusBarWithProgress extends JEditorStatusBar
{
	private JProgressBar progressBar;

	public JEditorStatusBarWithProgress()
	{
		super();

		initProgressBar();
	}

	private void initProgressBar()
	{
		progressBar = new JProgressBar();
		progressBar.setBackground( Color.WHITE );
		progressBar.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 3 ),
				BorderFactory.createMatteBorder( 0, 0, 1, 1, Color.LIGHT_GRAY ) ) );

		setStatusComponent( progressBar );
	}

	public JEditorStatusBarWithProgress( JEditorStatusBarTarget target )
	{
		super( target );

		initProgressBar();
	}

	public JProgressBar getProgressBar()
	{
		return progressBar;
	}

	public void setIndeterminate( boolean newValue )
	{
		progressBar.setIndeterminate( newValue );
	}

	public void setValue( int n )
	{
		progressBar.setValue( n );
	}
}
