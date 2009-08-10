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

package com.eviware.soapui.impl.rest.panels.resource;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;

import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.components.StringListFormComponent;
import com.jgoodies.binding.PresentationModel;

public class RestParamsTable extends JPanel
{
	protected RestParamsPropertyHolder params;
	protected RestParamsTableModel paramsTableModel;
	protected JTable paramsTable;
	protected AddParamAction addParamAction = new AddParamAction();
	protected RemoveParamAction removeParamAction = new RemoveParamAction();
	protected ClearParamsAction clearParamsAction = new ClearParamsAction();
	protected UseDefaultParamsAction defaultParamsAction = new UseDefaultParamsAction();
	protected MovePropertyDownAction movePropertyDownAction = new MovePropertyDownAction();
	protected MovePropertyUpAction movePropertyUpAction = new MovePropertyUpAction();
	private PresentationModel<RestParamProperty> paramDetailsModel;
	private StringListFormComponent optionsFormComponent;
	private SimpleBindingForm detailsForm;

	public RestParamsTable( RestParamsPropertyHolder params, boolean showInspector )
	{
		super( new BorderLayout() );
		this.params = params;
		init( params, showInspector );
	}

	protected RestParamsTableModel createTableModel( RestParamsPropertyHolder params )
	{
		return new RestParamsTableModel( params );
	}

	protected void init( RestParamsPropertyHolder params, boolean showInspector )
	{
		paramsTableModel = createTableModel( params );
		paramsTable = new JTable( paramsTableModel );
		paramsTable.setRowHeight( 19 );
		paramsTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		paramsTable.setDefaultEditor( ParameterStyle.class, new DefaultCellEditor( new JComboBox( new Object[] {
				ParameterStyle.QUERY, ParameterStyle.TEMPLATE, ParameterStyle.HEADER, ParameterStyle.MATRIX,
				ParameterStyle.PLAIN } ) ) );

		paramsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{

			public void valueChanged( ListSelectionEvent e )
			{
				int selectedRow = paramsTable.getSelectedRow();
				removeParamAction.setEnabled( selectedRow != -1 );
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
					// inspectorPanel.deactivate();
					if( paramDetailsModel != null )
					{
						detailsForm.setEnabled( false );
						paramDetailsModel.setBean( null );
					}
				}

				/*
				 * if( detailsInspector != null ) { detailsInspector.setEnabled(
				 * selectedRow != -1 );
				 * 
				 * if( selectedRow != -1 ) { RestParamProperty selectedParameter =
				 * getSelectedParameter(); paramDetailsModel.setBean(
				 * selectedParameter ); } else { inspectorPanel.deactivate();
				 * paramDetailsModel.setBean( null ); } }
				 */
			}
		} );

		add( buildToolbar(), BorderLayout.NORTH );

		if( showInspector )
		{
			final JSplitPane splitPane = UISupport.createVerticalSplit( new JScrollPane( paramsTable ), buildDetails() );
			add( splitPane, BorderLayout.CENTER );

			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					splitPane.setDividerLocation( 0.5 );
				}
			} );
		}
		else
		{
			add( new JScrollPane( paramsTable ), BorderLayout.CENTER );
		}
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
		optionsFormComponent.setPreferredSize( new Dimension( 350, 80 ) );
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

		toolbar.add( UISupport.createToolbarButton( addParamAction ) );
		toolbar.add( UISupport.createToolbarButton( removeParamAction, false ) );
		toolbar.add( UISupport.createToolbarButton( clearParamsAction, paramsTable.getRowCount() > 0 ) );
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

				clearParamsAction.setEnabled( true );
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
				params.removeProperty( propertyName );
				clearParamsAction.setEnabled( params.getPropertyCount() > 0 );
			}
		}
	}

	private class ClearParamsAction extends AbstractAction
	{
		public ClearParamsAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/clear_properties.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Clears all current parameter values" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( UISupport.confirm( "Clear all parameter values?", "Clear Parameters" ) )
			{
				params.clear();
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
				params.moveProperty( params.getPropertyAt( ix ).getName(), ix - 1 );
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
				params.moveProperty( params.getPropertyAt( ix ).getName(), ix + 1 );
				paramsTable.setRowSelectionInterval( ix + 1, ix + 1 );
			}
		}
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
