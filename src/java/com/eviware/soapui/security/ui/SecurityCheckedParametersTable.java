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
package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.model.security.SecurityParametersTableModel;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.impl.swing.JComboBoxFormField;
import com.eviware.x.impl.swing.SwingXFormDialog;

public class SecurityCheckedParametersTable extends JPanel
{

	private static final String CHOOSE_TEST_PROPERTY = "Choose Test Property";
	private SecurityParametersTableModel model;
	private JXToolBar toolbar;
	private JXTable table;
	private Map<String, TestProperty> properties;

	public SecurityCheckedParametersTable( SecurityParametersTableModel model, Map<String, TestProperty> properties )
	{
		this.model = model;
		this.properties = properties;
		init();
	}

	private void init()
	{
		setLayout( new BorderLayout() );
		toolbar = UISupport.createToolbar();

		toolbar.add( UISupport.createToolbarButton( new AddNewParameterAction() ) );
		toolbar.add( UISupport.createToolbarButton( new RemoveParameterAction() ) );
		toolbar.add( UISupport.createToolbarButton( new CopyParameterAction() ) );
		toolbar.addGlue();

		add( toolbar, BorderLayout.NORTH );
		table = new JXTable( model );
		add( new JScrollPane( table ), BorderLayout.CENTER );

	}

	/*
	 * Creates dialog
	 */
	private XFormDialog createDialog()
	{
		DefaultActionList actionList = new DefaultActionList();
		AddAction addAction = new AddAction();
		actionList.addAction( addAction, true );
		AddAndCopy addAndCopy = new AddAndCopy();
		actionList.addAction( addAndCopy );
		Close closeAction = new Close();
		actionList.addAction( closeAction );

		XFormDialog dialog = ADialogBuilder.buildDialog( AddParameterDialog.class, actionList, false );
		closeAction.setDialog( dialog );
		addAction.setDialog( dialog );
		addAndCopy.setDialog( dialog );

		final XFormField labelField = dialog.getFormField( AddParameterDialog.LABEL );
		labelField.setEnabled( false );
		final XFormField pathField = dialog.getFormField( AddParameterDialog.PATH );
		pathField.setEnabled( false );

		JComboBoxFormField nameField = ( JComboBoxFormField )dialog.getFormField( AddParameterDialog.NAME );
		nameField.addFormFieldListener( new XFormFieldListener()
		{

			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				if( !newValue.equals( CHOOSE_TEST_PROPERTY ) )
				{
					labelField.setEnabled( true );
					pathField.setEnabled( true );
				}
				else
				{
					labelField.setEnabled( false );
					pathField.setEnabled( false );
				}

			}
		} );
		ArrayList<String> options = new ArrayList<String>( properties.keySet() );
		options.set( 0, CHOOSE_TEST_PROPERTY );
		nameField.setOptions( options.toArray( new String[0] ) );
		return dialog;
	}

	private class AddNewParameterAction extends AbstractAction
	{

		public AddNewParameterAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Adds a parameter to security check" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			XFormDialog dialog = createDialog();
			dialog.show();
			model.fireTableDataChanged();
		}

	}

	private class RemoveParameterAction extends AbstractAction
	{

		public RemoveParameterAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Removes parameter from security check" );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			model.removeRows( table.getSelectedRows() );
			model.fireTableDataChanged();
		}

	}

	public class AddAndCopy extends AbstractAction
	{

		private XFormDialog dialog;

		public AddAndCopy()
		{
			super( "Add&Copy" );
		}

		public void setDialog( XFormDialog dialog )
		{
			this.dialog = dialog;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if( !model.addParameter( dialog.getValue( AddParameterDialog.LABEL ), dialog
					.getValue( AddParameterDialog.NAME ), dialog.getValue( AddParameterDialog.PATH ) ) )
				UISupport.showErrorMessage( "Label have to be unique!" );
		}

	}

	private class Close extends AbstractAction
	{

		private XFormDialog dialog;

		public Close()
		{
			super( "Close" );
		}

		public void setDialog( XFormDialog dialog )
		{
			this.dialog = dialog;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if( dialog != null )
			{
				( ( SwingXFormDialog )dialog ).setReturnValue( XFormDialog.CANCEL_OPTION );
				dialog.setVisible( false );
			}

		}

	}

	private class CopyParameterAction extends AbstractAction
	{

		public CopyParameterAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/clone_request.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Copies parameter" );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if( table.getSelectedRow() > -1 )
			{
				XFormDialog dialog = createDialog();

				int row = table.getSelectedRow();
				dialog.setValue( AddParameterDialog.LABEL, ( String )model.getValueAt( row, 0 ) );
				dialog.setValue( AddParameterDialog.NAME, ( String )model.getValueAt( row, 1 ) );
				dialog.setValue( AddParameterDialog.PATH, ( String )model.getValueAt( row, 2 ) );

				dialog.show();
				model.fireTableDataChanged();
			}
		}

	}

	private class AddAction extends AbstractAction
	{

		private XFormDialog dialog;

		public AddAction()
		{
			super( "Add" );
		}

		public void setDialog( XFormDialog dialog )
		{
			this.dialog = dialog;
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			if( model.addParameter( dialog.getValue( AddParameterDialog.LABEL ),
					dialog.getValue( AddParameterDialog.NAME ), dialog.getValue( AddParameterDialog.PATH ) ) )
			{
				JComboBoxFormField nameField = ( JComboBoxFormField )dialog.getFormField( AddParameterDialog.NAME );
				nameField.setSelectedOptions( new Object[] { nameField.getOptions()[0] } );
				dialog.setValue( AddParameterDialog.LABEL, "" );
				dialog.setValue( AddParameterDialog.PATH, "" );
			}
			else
				UISupport.showErrorMessage( "Label have to be unique!" );
		}

	}

	@AForm( description = "Add New Security Test Step Parameter", name = "Configure Security Test Step Parameters" )
	interface AddParameterDialog
	{
		@AField( description = "Parameter Name", name = "Parameter Name", type = AFieldType.ENUMERATION )
		static String NAME = "Parameter Name";

		@AField( description = "Parameter Label", name = "Parameter Label", type = AFieldType.STRING )
		static String LABEL = "Parameter Label";

		@AField( description = "Parameter XPath", name = "XPath", type = AFieldType.STRINGAREA )
		static String PATH = "XPath";

	}

}
