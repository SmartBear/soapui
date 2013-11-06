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

package com.eviware.soapui.support.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Utility for creating simple configuration dialogs
 * 
 * @author Ole.Matzura
 */

public class SwingConfigurationDialogImpl implements ConfigurationDialog
{
	private JDialog dialog;
	private SimpleForm form;
	private boolean result;
	private Map<String, String> values;
	private Map<String, FieldType> fieldTypes = new HashMap<String, FieldType>();
	private final String title;
	private Dimension size;
	private JComponent content;
	private ActionList actions;
	private String description;
	private ImageIcon icon;

	public SwingConfigurationDialogImpl( String title, String helpUrl, String description, ImageIcon icon )
	{
		this.title = title;
		this.description = description;
		this.icon = icon;
		form = new SimpleForm( new FormLayout( "10px,left:pref,10px,left:pref,5px" ) );

		actions = new DefaultActionList( "Actions" );
		if( helpUrl != null )
		{
			actions.addAction( new ShowOnlineHelpAction( helpUrl ) );
			actions.addSeparator();
		}

		OkAction okAction = new OkAction();
		actions.addAction( okAction );
		actions.addAction( new CancelAction() );
		actions.setDefaultAction( okAction );
	}

	public boolean show( Map<String, String> values )
	{
		if( dialog == null )
			buildDialog();

		this.values = values;

		result = false;

		form.setValues( values );

		if( size == null )
			dialog.pack();
		else
			dialog.setSize( size );

		UISupport.showDialog( dialog );
		return result;
	}

	public Dimension getSize()
	{
		return size;
	}

	public void setSize( Dimension preferredSize )
	{
		this.size = preferredSize;
	}

	public void addTextField( String name, String tooltip )
	{
		addTextField( name, tooltip, FieldType.TEXT );
	}

	public void addTextField( String name, String tooltip, FieldType type )
	{
		if( type == FieldType.DIRECTORY )
			form.append( name, new DirectoryFormComponent( tooltip ) );
		else
			form.appendTextField( name, tooltip );

		fieldTypes.put( name, type );
	}

	public void addCheckBox( String caption, String label, boolean selected )
	{
		form.appendCheckBox( caption, label, selected );
	}

	public void setContent( JComponent content )
	{
		this.content = content;
	}

	private void buildDialog()
	{
		dialog = new JDialog( UISupport.getMainFrame(), title, true );

		form.getPanel().setBorder( BorderFactory.createEmptyBorder( 5, 0, 5, 0 ) );

		JPanel panel = new JPanel( new BorderLayout() );
		JComponent contentPanel = content == null ? form.getPanel() : content;
		panel.add( contentPanel, BorderLayout.CENTER );

		JButtonBar buttons = UISupport.initDialogActions( actions, dialog );
		buttons.setBorder( BorderFactory.createEmptyBorder( 3, 5, 3, 5 ) );

		if( content == null )
		{
			JPanel p = new JPanel( new BorderLayout() );
			p.add( new JSeparator(), BorderLayout.NORTH );
			p.add( buttons, BorderLayout.CENTER );

			panel.add( p, BorderLayout.SOUTH );
		}
		else
		{
			panel.add( buttons, BorderLayout.SOUTH );
		}

		if( description != null || icon != null )
			dialog.getContentPane().add( UISupport.buildDescription( title, description, icon ), BorderLayout.NORTH );

		dialog.getContentPane().add( panel );
		dialog.pack();
	}

	private class OkAction extends AbstractAction
	{
		public OkAction()
		{
			super( "OK" );
		}

		public void actionPerformed( ActionEvent e )
		{
			result = true;

			form.getValues( values );
			dialog.setVisible( false );
			try
			{
				SoapUI.saveSettings();
			}
			catch( Exception e1 )
			{
				SoapUI.logError( e1, "There was an error when attempting to save your preferences" );
				UISupport.showErrorMessage( e1 );
			}
		}
	}

	private class CancelAction extends AbstractAction
	{
		public CancelAction()
		{
			super( "Cancel" );
		}

		public void actionPerformed( ActionEvent e )
		{
			dialog.setVisible( false );
		}
	}

	public void addComboBox( String label, Object[] objects, String tooltip )
	{
		form.appendComboBox( label, objects, tooltip );
	}

	public void setValues( String id, String[] values )
	{
		JComponent component = form.getComponent( id );
		if( component instanceof JComboBox )
		{
			( ( JComboBox )component ).setModel( new DefaultComboBoxModel( values ) );
		}
		else if( component instanceof JList )
		{
			( ( JList )component ).setModel( new DefaultComboBoxModel( values ) );
		}
		else
			throw new RuntimeException( "Could not set values on [" + component + "]" );
	}

	public void addComboBox( String label, String tooltip )
	{
		form.appendComboBox( label, new String[] {}, tooltip );
	}

	public void addComponent( JComponent component )
	{
		form.addComponent( component );
	}

	public void getValues( Map<String, String> values )
	{
		form.getValues( values );
	}

	public ActionList getActions()
	{
		return actions;
	}

	public void hide()
	{
		dialog.setVisible( false );
	}
}
