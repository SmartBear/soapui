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

package com.eviware.x.impl.swing;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

import com.eviware.soapui.settings.ProjectSettings;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.x.form.XFormTextField;
import com.eviware.x.form.XForm.FieldType;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.l2fprod.common.swing.JDirectoryChooser;

public class FileFormField extends AbstractSwingXFormField<JPanel> implements XFormTextField
{
	private final static Logger log = Logger.getLogger( FileFormField.class );

	private JTextField textField;
	private final FieldType type;
	private JButton selectDirectoryButton;
	private String projectRoot;

	private boolean updating;
	private String oldValue;
	private String currentDirectory;

	public FileFormField( String tooltip, FieldType type )
	{
		super( new JPanel() );
		this.type = type;

		ButtonBarBuilder builder = new ButtonBarBuilder( getComponent() );
		textField = new JUndoableTextField( 30 );
		textField.setToolTipText( tooltip );
		builder.addGriddedGrowing( textField );
		builder.addRelatedGap();
		selectDirectoryButton = new JButton( new SelectDirectoryAction() );
		builder.addFixed( selectDirectoryButton );

		textField.getDocument().addDocumentListener( new DocumentListenerAdapter()
		{

			@Override
			public void update( Document document )
			{
				String text = textField.getText();

				if( !updating )
					fireValueChanged( text, oldValue );

				oldValue = text;
			}
		} );
	}

	public void setValue( String value )
	{
		updating = true;
		oldValue = null;
		updateValue( value );
		updating = false;
	}

	private void updateValue( String value )
	{
		if( value != null && projectRoot != null && value.startsWith( projectRoot ) )
		{
			if( value.equals( projectRoot ) )
				value = "";
			else if( value.length() > projectRoot.length() + 1 )
				value = value.substring( projectRoot.length() + 1 );
		}

		textField.setText( value );
	}

	public String getValue()
	{
		String text = textField.getText().trim();

		if( projectRoot != null && text.length() > 0 )
		{
			String tempName = projectRoot + File.separatorChar + text;
			if( new File( tempName ).exists() )
			{
				text = tempName;
			}
		}

		return text;
	}

	public void setEnabled( boolean enabled )
	{
		textField.setEnabled( enabled );
		selectDirectoryButton.setEnabled( enabled );
	}

	@Override
	public boolean isEnabled()
	{
		return textField.isEnabled();
	}

	public void setCurrentDirectory( String currentDirectory )
	{
		this.currentDirectory = currentDirectory;
	}

	public class SelectDirectoryAction extends AbstractAction
	{
		private JFileChooser fileChooser;

		public SelectDirectoryAction()
		{
			super( "Browse..." );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( fileChooser == null )
			{
				if( type == FieldType.FOLDER || type == FieldType.PROJECT_FOLDER )
					fileChooser = new JDirectoryChooser();
				else
					fileChooser = new JFileChooser();
			}

			String value = FileFormField.this.getValue();
			if( value.length() > 0 )
			{
				fileChooser.setSelectedFile( new File( value ) );
			}
			else if( currentDirectory != null )
			{
				fileChooser.setCurrentDirectory( new File( currentDirectory ) );
			}
			else if( projectRoot != null )
			{
				fileChooser.setCurrentDirectory( new File( projectRoot ) );
			}

			int returnVal = fileChooser.showOpenDialog( UISupport.getMainFrame() );
			if( returnVal == JFileChooser.APPROVE_OPTION )
			{
				updateValue( fileChooser.getSelectedFile().getAbsolutePath() );
			}
		}
	}

	public void setProperty( String name, Object value )
	{
		super.setProperty( name, value );

		if( name.equals( ProjectSettings.PROJECT_ROOT ) && type == FieldType.PROJECT_FOLDER )
		{
			projectRoot = ( String )value;
			log.debug( "Set projectRoot to [" + projectRoot + "]" );
		}
		else if( name.equals( CURRENT_DIRECTORY ) )
		{
			currentDirectory = ( String )value;
			log.debug( "Set projectRoot to [" + projectRoot + "]" );
		}
	}

	public void setWidth( int columns )
	{
		textField.setColumns( columns );
	}

	public String getCurrentDirectory()
	{
		return currentDirectory;
	}
}
