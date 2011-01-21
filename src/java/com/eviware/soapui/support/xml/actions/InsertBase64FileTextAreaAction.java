/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.xml.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.JXEditTextArea;

/**
 * Inserts a file as base64 into a JXmlTextArea at the current cursor position.
 * 
 * @author Cory Lewis
 * @author Ole.Matzura
 */

public class InsertBase64FileTextAreaAction extends AbstractAction
{
	private final JXEditTextArea textArea;
	private String dialogTitle;

	public InsertBase64FileTextAreaAction( JXEditTextArea textArea, String dialogTitle )
	{
		super( "Insert file as Base64" );

		this.textArea = textArea;
		this.dialogTitle = dialogTitle;
		putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu G" ) );
	}

	public void actionPerformed( ActionEvent e )
	{
		File file = UISupport.getFileDialogs().open( this, dialogTitle, null, null, null );
		if( file == null )
			return;

		try
		{
			// read file
			byte[] ba = FileUtils.readFileToByteArray( file );

			// convert to base 64
			Base64 b64 = new Base64();
			String hex = new String( b64.encode( ba ) );
			// insert into text at cursor position
			int pos = textArea.getCaretPosition();
			StringBuffer text = new StringBuffer( textArea.getText() );
			text.insert( pos, hex );
			textArea.setText( text.toString() );

		}
		catch( IOException e1 )
		{
			UISupport.showErrorMessage( "Error reading from file: " + e1.getMessage() );
		}
	}

}
