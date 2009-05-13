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

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.jgoodies.forms.builder.ButtonBarBuilder;

public class DirectoryFormComponent extends JPanel implements JFormComponent
{
	private JTextField textField;
	private String initialFolder;

	public DirectoryFormComponent( String tooltip )
	{
		ButtonBarBuilder builder = new ButtonBarBuilder( this );
		textField = new JTextField( 30 );
		textField.setToolTipText( tooltip );
		builder.addGriddedGrowing( textField );
		builder.addRelatedGap();
		builder.addFixed( new JButton( new SelectDirectoryAction() ) );
	}

	public void setValue( String value )
	{
		textField.setText( value );
	}

	public String getValue()
	{
		return textField.getText();
	}

	public class SelectDirectoryAction extends AbstractAction
	{
		public SelectDirectoryAction()
		{
			super( "Browse..." );
		}

		public void actionPerformed( ActionEvent e )
		{
			File currentDirectory = StringUtils.hasContent( initialFolder ) ? new File( initialFolder ) : null;
			if( textField.getText().length() > 0 )
				currentDirectory = new File( textField.getText() );
			File file = UISupport.getFileDialogs().openDirectory( this, "Select directory", currentDirectory );
			if( file != null )
			{
				textField.setText( file.getAbsolutePath() );
			}
		}
	}

	public JTextComponent getTextField()
	{
		return textField;
	}

	public void setInitialFolder( String initialFolder )
	{
		this.initialFolder = initialFolder;
	}
}
