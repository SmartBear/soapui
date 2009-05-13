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

package com.eviware.soapui.support.xml.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.JXEditTextArea;

/**
 * Saves the XML of a JXmlTextArea to a file
 * 
 * @author Ole.Matzura
 */

public class SaveXmlTextAreaAction extends AbstractAction
{
	private final JXEditTextArea textArea;
	private String dialogTitle;
	private static final Logger log = Logger.getLogger( SaveXmlTextAreaAction.class );

	public SaveXmlTextAreaAction( JXEditTextArea textArea, String dialogTitle )
	{
		super( "Save as.." );
		this.textArea = textArea;
		this.dialogTitle = dialogTitle;
		putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu S" ) );
	}

	public void actionPerformed( ActionEvent e )
	{
		File file = UISupport.getFileDialogs().saveAs( this, dialogTitle, ".xml", "XML Files (*.xml)", null );
		if( file == null )
			return;

		FileWriter writer = null;

		try
		{
			try
			{
				XmlObject xml = XmlObject.Factory.parse( textArea.getText() );
				xml.save( file );
			}
			catch( XmlException e1 )
			{
				writer = new FileWriter( file );
				writer.write( textArea.getText() );
				writer.close();
			}

			log.info( "XML written to [" + file.getAbsolutePath() + "]" );
		}
		catch( IOException e1 )
		{
			UISupport.showErrorMessage( "Error saving xml to file: " + e1.getMessage() );
		}
		finally
		{
			if( writer != null )
			{
				try
				{
					writer.close();
				}
				catch( IOException e1 )
				{
					SoapUI.logError( e1 );
				}
			}
		}
	}
}