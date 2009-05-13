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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.components.JButtonBar;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.ValidationMessage;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;

public class JTabbedFormDialog extends SwingXFormDialog
{
	private JDialog dialog;
	private List<SwingXFormImpl> forms = new ArrayList<SwingXFormImpl>();
	private JTabbedPane tabs;
	private JButtonBar buttons;

	public JTabbedFormDialog( String name, XForm[] forms, ActionList actions, String description, ImageIcon icon )
	{
		dialog = new JDialog( UISupport.getMainFrame(), name, true );
		tabs = new JTabbedPane();
		for( XForm form : forms )
		{
			SwingXFormImpl swingFormImpl = ( ( SwingXFormImpl )form );
			this.forms.add( swingFormImpl );

			JPanel panel = swingFormImpl.getPanel();
			panel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 5, 0 ) );

			tabs.addTab( form.getName(), panel );
		}

		buttons = UISupport.initDialogActions( actions, dialog );

		if( description != null || icon != null )
			dialog.getContentPane().add( UISupport.buildDescription( name, description, icon ), BorderLayout.NORTH );

		dialog.getContentPane().add( UISupport.createTabPanel( tabs, false ), BorderLayout.CENTER );

		buttons.setBorder( BorderFactory.createEmptyBorder( 3, 5, 3, 5 ) );
		dialog.getContentPane().add( buttons, BorderLayout.SOUTH );
		dialog.pack();
		Dimension size = dialog.getSize();
		if( size.getHeight() < 300 )
		{
			dialog.setSize( new Dimension( ( int )size.getWidth(), 300 ) );
		}
	}

	public void setValues( StringToStringMap values )
	{
		for( XForm form : forms )
			form.setValues( values );
	}

	public void setOptions( String field, Object[] options )
	{
		for( XForm form : forms )
			form.setOptions( field, options );
	}

	public XFormField getFormField( String name )
	{
		for( XForm form : forms )
		{
			XFormField formField = form.getFormField( name );
			if( formField != null )
				return formField;
		}

		return null;
	}

	public void addAction( Action action )
	{
		DefaultActionList actions = new DefaultActionList();
		actions.addAction( action );
		buttons.addActions( actions );
	}

	public StringToStringMap getValues()
	{
		StringToStringMap result = new StringToStringMap();

		for( XForm form : forms )
			result.putAll( form.getValues() );

		return result;
	}

	public void setVisible( boolean visible )
	{
		if( visible )
			tabs.setSelectedIndex( 0 );

		UISupport.centerDialog( dialog );

		dialog.setVisible( visible );
	}

	public boolean validate()
	{
		for( int i = 0; i < forms.size(); i++ )
		{
			XFormField[] formFields = forms.get( i ).getFormFields();
			for( int c = 0; c < formFields.length; c++ )
			{
				ValidationMessage[] messages = formFields[c].validate();
				if( messages != null && messages.length > 0 )
				{
					tabs.setSelectedIndex( i );
					( ( AbstractSwingXFormField<?> )messages[0].getFormField() ).getComponent().requestFocus();
					UISupport.showErrorMessage( messages[0].getMessage() );
					return false;
				}
			}
		}

		return true;
	}

	public void setFormFieldProperty( String name, Object value )
	{
		for( XForm form : forms )
			form.setFormFieldProperty( name, value );
	}

	public String getValue( String field )
	{
		for( XForm form : forms )
		{
			if( form.getComponent( field ) != null )
				return form.getComponent( field ).getValue();
		}

		return null;
	}

	public void setValue( String field, String value )
	{
		for( XForm form : forms )
		{
			if( form.getComponent( field ) != null )
				form.getComponent( field ).setValue( value );
		}
	}

	public int getValueIndex( String name )
	{
		for( SwingXFormImpl form : forms )
		{
			if( form.getComponent( name ) != null )
			{
				String[] options = form.getOptions( name );
				if( options == null )
					return -1;

				return Arrays.asList( options ).indexOf( form.getComponentValue( name ) );
			}
		}

		return -1;
	}

	public boolean show()
	{
		setReturnValue( XFormDialog.CANCEL_OPTION );
		show( new StringToStringMap() );
		return getReturnValue() == XFormDialog.OK_OPTION;
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
