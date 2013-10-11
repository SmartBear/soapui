/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.components.StringListFormComponent;
import com.jgoodies.binding.PresentationModel;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.namespace.QName;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;


public class RestParamsTable extends JPanel
{
	protected RestParamsPropertyHolder params;
	protected RestParamsTableModel paramsTableModel;
	protected JTable paramsTable;
	protected AddParamAction addParamAction = null;
	protected RemoveParamAction removeParamAction = null;
	protected UseDefaultParamsAction defaultParamsAction = null;
	protected MovePropertyDownAction movePropertyDownAction = null;
	protected MovePropertyUpAction movePropertyUpAction = null;
	protected UpdateParamsAction updateParamsAction = null;
	private PresentationModel<RestParamProperty> paramDetailsModel;
	private StringListFormComponent optionsFormComponent;
	private SimpleBindingForm detailsForm;
	private final ParamLocation defaultParamLocation;
	private boolean showEditableButtons;
	private boolean showDefaultParamsButton;
	private FocusAdapter focusAdapter = new FocusAdapter()
	{
		@Override
		public void focusGained( FocusEvent e )
		{
			System.out.println( "Gained focus" );
		}
	};

	public RestParamsTable( RestParamsPropertyHolder params, boolean showInspector, ParamLocation defaultParamLocation ,
									boolean showEditableButtons, boolean showDefaultParamsButton )
	{
		this( params, showInspector, new RestParamsTableModel( params ), defaultParamLocation, showEditableButtons,
				showDefaultParamsButton );
	}

	public RestParamsTable( RestParamsPropertyHolder params, boolean showInspector, RestParamsTableModel model,
									ParamLocation defaultParamLocation, boolean showEditableButtons, boolean showDefaultParamsButton )
	{
		super( new BorderLayout() );
		this.showEditableButtons = showEditableButtons;
		this.showDefaultParamsButton = showDefaultParamsButton;
		this.params = params;
		this.paramsTableModel = model;
		this.defaultParamLocation = defaultParamLocation;
		init( showInspector );
	}

	protected void init( boolean showInspector )
	{
		if ( showDefaultParamsButton )
		{
			defaultParamsAction = new UseDefaultParamsAction();
		}

		movePropertyDownAction = new MovePropertyDownAction();
		movePropertyUpAction = new MovePropertyUpAction();

		if( showEditableButtons )
		{
			initEditableButtons();
		}

		paramsTable = new JTable( paramsTableModel );
		paramsTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		paramsTable.setDefaultEditor( ParameterStyle.class, new DefaultCellEditor(
				new JComboBox( paramsTableModel.getParameterStylesForEdit() ) ) );
		paramsTable.setDefaultEditor( ParamLocation.class, new DefaultCellEditor(
				new JComboBox( paramsTableModel.getParameterLevels() ) ) );
		// Workaround: for some reason the lower part of text gets clipped on some platforms
		paramsTable.setRowHeight( 25 );
		paramsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{

			public void valueChanged( ListSelectionEvent e )
			{
				int selectedRow = paramsTable.getSelectedRow();
				if( showEditableButtons)
				{
					removeParamAction.setEnabled( selectedRow != -1 );
				}
				movePropertyDownAction.setEnabled( selectedRow < paramsTable.getRowCount() - 1 );
				movePropertyUpAction.setEnabled( selectedRow > 0 );

				if( selectedRow != -1 )
				{
					RestParamProperty selectedParameter = getSelectedParameter();
					if( paramDetailsModel != null )
					{
						paramDetailsModel.setBean( selectedParameter );
						detailsForm.setEnabled( true );
					}
				}
				else
				{
					if( paramDetailsModel != null )
					{
						detailsForm.setEnabled( false );
						paramDetailsModel.setBean( null );
					}
				}
			}
		} );

		add( buildToolbar(), BorderLayout.NORTH );

		if( showInspector )
		{
			final JSplitPane splitPane = UISupport.createVerticalSplit( new JScrollPane( paramsTable ), buildDetails() );
			add( splitPane, BorderLayout.CENTER );

			splitPane.setResizeWeight( 0.7 );
		}
		else
		{
			add( new JScrollPane( paramsTable ), BorderLayout.CENTER );
		}
	}

	@Override
	public synchronized void addKeyListener( KeyListener l )
	{
		super.addKeyListener( l );
		paramsTable.addKeyListener( l );
	}

	private void initEditableButtons()
	{
		addParamAction = new AddParamAction();
		removeParamAction = new RemoveParamAction();
		updateParamsAction = new UpdateParamsAction();

	}

	private JComponent buildDetails()
	{
		paramDetailsModel = new PresentationModel<RestParamProperty>( null );
		detailsForm = new SimpleBindingForm( paramDetailsModel );

		detailsForm.addSpace( 5 );
		detailsForm.appendCheckBox( "required", "Required", "Sets if parameter is required" );
		// form.appendTextField( "defaultValue", "Default",
		// "The default value for this parameter" );

		List<QName> types = new ArrayList<QName>();
		for( SchemaType type : XmlBeans.getBuiltinTypeSystem().globalTypes() )
		{
			types.add( type.getName() );
		}

		detailsForm.appendComboBox( "type", "Type", types.toArray(), "The type of the parameter" );
		optionsFormComponent = new StringListFormComponent( "Available values for this Parameter" );
		//TODO: Consider removing hardcoded size
		optionsFormComponent.setPreferredSize( new Dimension( 350, 100 ) );
		detailsForm.appendComponent( "options", "Options", optionsFormComponent );
		detailsForm.appendTextField( "description", "Description", "A short description of the parameter" );
		detailsForm.appendCheckBox( "disableUrlEncoding", "Disable Encoding",
				"Disables URL-Encoding of the parameter value" );

		detailsForm.addSpace( 5 );

		detailsForm.setEnabled( false );

		return new JScrollPane( detailsForm.getPanel() );
	}

	protected RestParamProperty getSelectedParameter()
	{
		return paramsTable.getSelectedRow() == -1 ? null : paramsTableModel.getParameterAt( paramsTable.getSelectedRow() );
	}

	public JTable getParamsTable()
	{
		return paramsTable;
	}

	public void release()
	{
		paramsTableModel.release();
		if( paramDetailsModel != null )
			paramDetailsModel.setBean( null );
	}

	protected Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		if( showEditableButtons )
		{
			toolbar.add( UISupport.createToolbarButton( addParamAction ) );
			toolbar.add( UISupport.createToolbarButton( removeParamAction, false ) );
			toolbar.addSeparator();
			toolbar.add( UISupport.createToolbarButton( updateParamsAction ) );
		}

		if( showDefaultParamsButton )
		{
			toolbar.add( UISupport.createToolbarButton( defaultParamsAction, paramsTable.getRowCount() > 0 ) );
		}

		toolbar.addSeparator();
		toolbar.add( UISupport.createToolbarButton( movePropertyDownAction, false ) );
		toolbar.add( UISupport.createToolbarButton( movePropertyUpAction, false ) );
		toolbar.addSeparator();

		insertAdditionalButtons( toolbar );

		toolbar.addGlue();

		toolbar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.WADL_PARAMS_HELP_URL ) ) );

		return toolbar;
	}

	protected void insertAdditionalButtons( JXToolBar toolbar )
	{
	}

	public void extractParams( RestParamsPropertyHolder params, ParamLocation location )
	{
		for( int i = 0; i < paramsTable.getRowCount(); i++ )
		{
			RestParamProperty prop = paramsTableModel.getParameterAt( i );
			if( paramsTableModel.getParamLocationAt( i ) == location )
			{
				params.addParameter( prop );
			}
		}
	}

	public void focusParameter(String parameterName)
	{
		paramsTable.grabFocus();
		for (int i = 0; i < paramsTable.getRowCount(); i++)
		{
			 if (paramsTable.getValueAt(i, 0).equals(parameterName))
			 {
				 paramsTable.editCellAt( i, 1 );
				 paramsTable.getEditorComponent().requestFocusInWindow();
				 return;
			 }
		}
		paramsTable.editCellAt(0, 1);
		JTextField editorComponent = ( JTextField )paramsTable.getEditorComponent();
		editorComponent.grabFocus();
		editorComponent.selectAll();
		System.out.println("Focused: " + paramsTable.getEditorComponent().hasFocus());

	}

	private class AddParamAction extends AbstractAction
	{
		public AddParamAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Adds a parameter to the parameter table" );
		}

		public void actionPerformed( ActionEvent e )
		{
			String name = UISupport.prompt( "Specify parameter name", "Add Parameter", "" );
			if( StringUtils.hasContent( name ) )
			{
				params.addProperty( name );
				RestParamProperty addedProperty = params.getProperty( name );
				addedProperty.setParamLocation( defaultParamLocation );

				final int row = params.getPropertyNames().length - 1;
				SwingUtilities.invokeLater( new Runnable()
				{
					public void run()
					{
						requestFocusInWindow();
						scrollRectToVisible( paramsTable.getCellRect( row, 1, true ) );
						SwingUtilities.invokeLater( new Runnable()
						{
							public void run()
							{
								paramsTable.editCellAt( row, 1 );
								paramsTable.getEditorComponent().requestFocusInWindow();
							}
						} );
					}
				} );

			}
		}
	}

	private class UpdateParamsAction extends AbstractAction
	{
		private UpdateParamsAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/update-request-parameters-from-url.png" ) );
			putValue( Action.SHORT_DESCRIPTION, "Updates params from a specified URL" );
		}

		public void actionPerformed( ActionEvent e )
		{
			String str = UISupport.prompt( "Enter new url below", "Extract Params", "" );
			if( str == null )
				return;

			try
			{
				params.resetValues();
				RestUtils.extractParams( str, params, false );
			}
			catch( Exception e1 )
			{
				UISupport.showErrorMessage( e1 );
			}
		}
	}

	private class RemoveParamAction extends AbstractAction
	{
		public RemoveParamAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Removes the selected parameter" );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int row = paramsTable.getSelectedRow();
			if( row == -1 )
				return;

			UISupport.stopCellEditing( paramsTable );

			String propertyName = paramsTableModel.getValueAt( row, 0 ).toString();
			if( UISupport.confirm( "Remove parameter [" + propertyName + "]?", "Remove Parameter" ) )
			{
				paramsTable.clearSelection();
				paramsTableModel.removeProperty( propertyName );
				//params.removeProperty( propertyName );
			}
		}
	}

	private class UseDefaultParamsAction extends AbstractAction
	{
		public UseDefaultParamsAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/default_properties.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Reverts all current parameters to default values" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( UISupport.confirm( "Revert all parameters to default values?", "Use Defaults" ) )
			{
				for( TestProperty property : params.getProperties().values() )
				{
					property.setValue( null );
				}
			}
		}
	}

	private class MovePropertyUpAction extends AbstractAction
	{
		public MovePropertyUpAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/up_arrow.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Moves selected parameter up one row" );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = paramsTable.getSelectedRow();
			if( ix != -1 )
			{
				moveProperty( ix, ix-1 );
				paramsTable.setRowSelectionInterval( ix - 1, ix - 1 );
			}
		}
	}

	private class MovePropertyDownAction extends AbstractAction
	{
		public MovePropertyDownAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/down_arrow.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Moves selected parameter down one row" );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = paramsTable.getSelectedRow();
			if( ix != -1 )
			{
				moveProperty( ix, ix+1 );
				paramsTable.setRowSelectionInterval( ix + 1, ix + 1 );
			}
		}
	}

	private void moveProperty( int oldRow, int newRow )
	{
		String propName = (String) paramsTableModel.getValueAt( oldRow, 0 );
		params.moveProperty( propName, newRow );
		paramsTableModel.moveProperty( propName, oldRow, newRow );
	}

	public void setParams( RestParamsPropertyHolder params )
	{
		this.params = params;
		paramsTableModel.setParams( params );
	}

	public void refresh()
	{
		paramsTableModel.fireTableDataChanged();
	}
}
