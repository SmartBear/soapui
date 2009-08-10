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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.components.JButtonBar;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.ValidationMessage;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;

public class JFormDialog extends SwingXFormDialog
{
	private JDialog dialog;
	private SwingXFormImpl form;
	private JButtonBar buttons;
	private boolean resized;

	public JFormDialog( String name, SwingXFormImpl form, ActionList actions, String description, ImageIcon icon )
	{
		dialog = new JDialog( UISupport.getMainFrame(), name, true );

		buttons = UISupport.initDialogActions( actions, dialog );
		buttons.setBorder( BorderFactory.createEmptyBorder( 5, 0, 0, 0 ) );

		JPanel panel = new JPanel( new BorderLayout() );
		this.form = ( SwingXFormImpl )form;
		panel.add( ( this.form.getPanel() ), BorderLayout.CENTER );
		panel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

		if( description != null || icon != null )
			dialog.getContentPane().add( UISupport.buildDescription( name, description, icon ), BorderLayout.NORTH );

		dialog.getContentPane().add( panel, BorderLayout.CENTER );

		buttons.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createCompoundBorder( BorderFactory
				.createMatteBorder( 1, 0, 0, 0, Color.GRAY ), BorderFactory.createMatteBorder( 1, 0, 0, 0, Color.WHITE ) ),
				BorderFactory.createEmptyBorder( 3, 5, 3, 5 ) ) );

		dialog.getContentPane().add( buttons, BorderLayout.SOUTH );
	}

	public void setValues( StringToStringMap values )
	{
		form.setValues( values );
	}

	public void setSize( int i, int j )
	{
		dialog.setSize( i, j );
		resized = true;
	}

	public XForm[] getForms()
	{
		return new XForm[] { form };
	}

	public StringToStringMap getValues()
	{
		StringToStringMap result = new StringToStringMap();
		result.putAll( form.getValues() );

		return result;
	}

	public void setOptions( String field, Object[] options )
	{
		form.setOptions( field, options );
	}

	public void setVisible( boolean visible )
	{
		if( !resized )
		{
			dialog.pack();
			if( dialog.getHeight() < 270 )
			{
				dialog.setSize( new Dimension( dialog.getWidth(), 270 ) );
			}

			if( dialog.getWidth() < 450 )
			{
				dialog.setSize( new Dimension( 450, dialog.getHeight() ) );
			}
		}
		
		UISupport.centerDialog( dialog );
		dialog.setVisible( visible );
	}

	public void addAction( Action action )
	{
		DefaultActionList actions = new DefaultActionList();
		actions.addAction( action );
		buttons.addActions( actions );
	}

	public boolean validate()
	{
		XFormField[] formFields = form.getFormFields();
		for( int c = 0; c < formFields.length; c++ )
		{
			ValidationMessage[] messages = formFields[c].validate();
			if( messages != null && messages.length > 0 )
			{
				( ( AbstractSwingXFormField<?> )messages[0].getFormField() ).getComponent().requestFocus();
				UISupport.showErrorMessage( messages[0].getMessage() );
				return false;
			}
		}

		return true;
	}

	public void setFormFieldProperty( String name, Object value )
	{
		form.setFormFieldProperty( name, value );
	}

	public String getValue( String field )
	{
		return form.getComponentValue( field );
	}

	public void setValue( String field, String value )
	{
		form.setComponentValue( field, value );
	}

	public int getValueIndex( String name )
	{
		Object[] options = form.getOptions( name );
		if( options == null )
			return -1;

		return StringUtils.toStringList( options ).indexOf( form.getComponentValue( name ) );
	}

	public boolean show()
	{
		setReturnValue( XFormDialog.CANCEL_OPTION );
		show( new StringToStringMap() );
		return getReturnValue() == XFormDialog.OK_OPTION;
	}

	public XFormField getFormField( String name )
	{
		return form.getFormField( name );
	}

	public void setWidth( int i )
	{
		dialog.setPreferredSize( new Dimension( i, ( int )dialog.getPreferredSize().getHeight() ) );
	}

	public void release()
	{
		dialog.dispose();
	}
}
