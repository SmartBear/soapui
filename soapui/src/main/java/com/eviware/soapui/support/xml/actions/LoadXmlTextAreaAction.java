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

package com.eviware.soapui.support.xml.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;

/**
 * Loads XML into a JXmlTextArea from a file
 * 
 * @author Ole.Matzura
 */

public class LoadXmlTextAreaAction extends AbstractAction
{
	private final RSyntaxTextArea textArea;
	private String dialogTitle;

	public LoadXmlTextAreaAction( RSyntaxTextArea textArea, String dialogTitle )
	{
		super( "Load from.." );
		this.textArea = textArea;
		this.dialogTitle = dialogTitle;
		if( UISupport.isMac() )
		{
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "meta L" ) );
		}
		else
		{
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "ctrl L" ) );
		}

	}

	public void actionPerformed( ActionEvent e )
	{
		File file = UISupport.getFileDialogs().open( this, dialogTitle, ".xml", "XML Files (*.xml)", null );
		if( file == null )
			return;

		try
		{
			textArea.setText( Tools.readAll( new FileInputStream( file ), 0 ).toString() );
		}
		catch( IOException e1 )
		{
			UISupport.showErrorMessage( "Error loading xml from file: " + e1.getMessage() );
		}
	}
}
