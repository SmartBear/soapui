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

	static final String CHOOSE_TEST_PROPERTY = "Choose Test Property";
	private SecurityParametersTableModel model;
	private JXToolBar toolbar;
	private JXTable table;
	protected Map<String, TestProperty> properties;
	protected DefaultActionList actionList;

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
	protected XFormDialog createAddParameterDialog()
	{
		actionList = new DefaultActionList();
		AddAction addAction = new AddAction();
		actionList.addAction( addAction, true );
		AddAndCopy addAndCopy = new AddAndCopy();
		actionList.addAction( addAndCopy );
		Close closeAction = new Close();
		actionList.addAction( closeAction );

		XFormDialog dialog = buildAddParameterDialog();

		closeAction.setDialog( dialog );
		addAction.setDialog( dialog );
		addAndCopy.setDialog( dialog );

		final XFormField labelField = dialog.getFormField( AddParameterDialog.LABEL );
		labelField.setEnabled( false );
		final XFormField pathField = disablePath( dialog );
		enablePathField( pathField, false );
		JComboBoxFormField nameField = ( JComboBoxFormField )dialog.getFormField( AddParameterDialog.NAME );
		nameField.addFormFieldListener( new XFormFieldListener()
		{

			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				if( !newValue.equals( CHOOSE_TEST_PROPERTY ) )
				{
					labelField.setEnabled( true );
					enablePathField( pathField, true );
				}
				else
				{
					labelField.setEnabled( false );
					enablePathField( pathField, false );
				}

			}
		} );
		ArrayList<String> options = new ArrayList<String>( properties.keySet() );
		options.set( 0, CHOOSE_TEST_PROPERTY );
		nameField.setOptions( options.toArray( new String[0] ) );
		return dialog;
	}

	/*
	 * Override this if you want to use different dialog layout
	 * 
	 * @param actionList
	 * 
	 * @return
	 */
	protected XFormDialog buildAddParameterDialog()
	{
		return ADialogBuilder.buildDialog( AddParameterDialog.class, actionList, false );
	}

	/*
	 * 
	 * Overide this is path element is not same as in AddParameterDialog
	 * 
	 * @param dialog
	 * 
	 * @return
	 */
	protected XFormField disablePath( XFormDialog dialog )
	{
		final XFormField pathField = dialog.getFormField( AddParameterDialog.PATH );
		pathField.setEnabled( false );
		return pathField;
	}

	/**
	 * @param pathField
	 */
	protected void enablePathField( final XFormField pathField, boolean enable )
	{
		pathField.setEnabled( enable );
	}

	/**
	 * 
	 * 
	 * @return
	 */
	protected boolean addParameter( XFormDialog dialog )
	{
		return model.addParameter( dialog.getValue( AddParameterDialog.LABEL ),
				dialog.getValue( AddParameterDialog.NAME ), dialog.getValue( AddParameterDialog.PATH ) );
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
			XFormDialog dialog = createAddParameterDialog();
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
			if( !addParameter( dialog ) )
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
				XFormDialog dialog = createAddParameterDialog();

				int row = table.getSelectedRow();
				initDialogForCopy( dialog, row );

				dialog.show();
				model.fireTableDataChanged();
			}
		}

	}

	/**
	 * @param dialog
	 * @param row
	 */
	protected void initDialogForCopy( XFormDialog dialog, int row )
	{
		dialog.setValue( AddParameterDialog.LABEL, ( String )model.getValueAt( row, 0 ) );
		dialog.setValue( AddParameterDialog.NAME, ( String )model.getValueAt( row, 1 ) );
		dialog.setValue( AddParameterDialog.PATH, ( String )model.getValueAt( row, 2 ) );
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
			if( dialog.getValue( AddParameterDialog.LABEL ) == null
					|| dialog.getValue( AddParameterDialog.LABEL ).trim().length() == 0 )
			{
				UISupport.showErrorMessage( "Label is required!" );
			}
			else
			{
				if( addParameter( dialog ) )
				{
					JComboBoxFormField nameField = ( JComboBoxFormField )dialog.getFormField( AddParameterDialog.NAME );
					nameField.setSelectedOptions( new Object[] { nameField.getOptions()[0] } );
					dialog.setValue( AddParameterDialog.LABEL, "" );
					resetPathField(dialog);
				}
				else
					UISupport.showErrorMessage( "Label have to be unique!" );
			}
		}

	}

	public SecurityParametersTableModel getModel()
	{
		return model;
	}

	/**
	 * 
	 */
	protected void resetPathField(XFormDialog dialog)
	{
		dialog.setValue( AddParameterDialog.PATH, "" );
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
