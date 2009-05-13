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

import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.jgoodies.forms.builder.ButtonBarBuilder;

public class FileFormComponent extends JPanel implements JFormComponent
{
	private JTextField textField;
	private AbstractWsdlModelItem<?> modelItem;

	public FileFormComponent( String tooltip )
	{
		ButtonBarBuilder builder = new ButtonBarBuilder( this );
		textField = new JTextField( 30 );
		textField.setToolTipText( tooltip );
		builder.addGriddedGrowing( textField );
		builder.addRelatedGap();
		builder.addFixed( new JButton( new SelectFileAction() ) );
	}

	public void setValue( String value )
	{
		textField.setText( value );
	}

	public JTextField getTextField()
	{
		return textField;
	}

	public String getValue()
	{
		return textField.getText();
	}

	public void setFile( File file )
	{
		setValue( file.getAbsolutePath() );
	}

	public void setModelItem( AbstractWsdlModelItem<?> modelItem )
	{
		this.modelItem = modelItem;
	}

	public class SelectFileAction extends AbstractAction
	{
		public SelectFileAction()
		{
			super( "Browse..." );
		}

		public void actionPerformed( ActionEvent e )
		{
			String value = FileFormComponent.this.getValue();
			File file = UISupport.getFileDialogs().open( this, "Select file", null, null,
					StringUtils.hasContent( value ) ? value : PathUtils.getExpandedResourceRoot( modelItem ) );
			if( file != null )
			{
				setFile( file );
			}
		}
	}
}
