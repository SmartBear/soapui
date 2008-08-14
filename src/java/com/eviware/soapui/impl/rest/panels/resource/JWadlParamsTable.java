package com.eviware.soapui.impl.rest.panels.resource;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder.ParameterStyle;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;

public class JWadlParamsTable extends JPanel
{
	private final XmlBeansRestParamsTestPropertyHolder params;
	private WadlParamsTableModel paramsTableModel;
	private JXTable paramsTable;
	private AddParamAction addParamAction = new AddParamAction();
	private RemoveParamAction removeParamAction = new RemoveParamAction();
	private ClearParamsAction clearParamsAction = new ClearParamsAction();
	private MovePropertyDownAction movePropertyDownAction = new MovePropertyDownAction();
	private MovePropertyUpAction movePropertyUpAction = new MovePropertyUpAction();

	public JWadlParamsTable(XmlBeansRestParamsTestPropertyHolder params)
	{
		super( new BorderLayout() );
		this.params = params;
		
		paramsTableModel = new WadlParamsTableModel( params );
		paramsTable = new JXTable( paramsTableModel );
		paramsTable.setDefaultEditor(ParameterStyle.class, new DefaultCellEditor(new JComboBox( 
				new Object[] {ParameterStyle.QUERY, ParameterStyle.TEMPLATE, ParameterStyle.HEADER, ParameterStyle.MATRIX, ParameterStyle.PLAIN })));
		
		paramsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e)
			{
				removeParamAction.setEnabled( paramsTable.getSelectedRow() != -1 );
				movePropertyDownAction.setEnabled( paramsTable.getSelectedRow() >= paramsTable.getRowCount() );
				movePropertyUpAction.setEnabled( paramsTable.getSelectedRow() != -1 && paramsTable.getSelectedRow() < paramsTable.getRowCount() );
			}} );
		
		add( buildToolbar(), BorderLayout.NORTH );
		add( new JScrollPane( paramsTable), BorderLayout.CENTER );
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();
		
		toolbar.add( UISupport.createToolbarButton( addParamAction ));
		toolbar.add( UISupport.createToolbarButton( removeParamAction, false ));
		toolbar.add( UISupport.createToolbarButton( clearParamsAction, paramsTable.getRowCount() > 0 ));
		toolbar.addSeparator();
		toolbar.add( UISupport.createToolbarButton( movePropertyDownAction, false ));
		toolbar.add( UISupport.createToolbarButton( movePropertyUpAction, false ));
		
		return toolbar;
	}

	private class AddParamAction extends AbstractAction
	{
		public AddParamAction()
		{
			putValue(Action.SMALL_ICON, UISupport.createImageIcon("/add_property.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Adds a parameter to the parameter table");
		}

		public void actionPerformed(ActionEvent e)
		{
			String name = UISupport.prompt("Specify unique parameter name", "Add Parameter", "");
			if ( StringUtils.hasContent( name ))
			{
				if( params.hasProperty( name ))
				{
					UISupport.showErrorMessage( "Param name [" + name + "] already exists.." );
					return;
				}
				
				params.addProperty(name);
				final int row = params.getPropertyNames().length - 1;
				paramsTableModel.fireTableRowsInserted(row, row);
				SwingUtilities.invokeLater( new Runnable()
				{
					public void run()
					{
						requestFocusInWindow();
						scrollRectToVisible( paramsTable.getCellRect( row,1,true ) );
						SwingUtilities.invokeLater( new Runnable()
						{
							public void run()
							{
								paramsTable.editCellAt(row, 1);
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
			putValue(Action.SMALL_ICON, UISupport.createImageIcon("/remove_property.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Removes the selected parameter");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e)
		{
			int row = paramsTable.getSelectedRow();
			if (row == -1)
				return;

			UISupport.stopCellEditing(paramsTable);

			String propertyName = paramsTableModel.getValueAt(row, 0).toString();
			if (UISupport.confirm("Remove parameter [" + propertyName + "]?", "Remove Parameter"))
			{
				params.removeProperty( propertyName );
				paramsTableModel.fireTableRowsDeleted(row, row);
				clearParamsAction.setEnabled(params.getPropertyCount() > 0 );
			}
		}
	}
	
	private class ClearParamsAction extends AbstractAction
	{
		public ClearParamsAction()
		{
			putValue(Action.SMALL_ICON, UISupport.createImageIcon("/clear_properties.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Clears all current parameter values");
		}

		public void actionPerformed(ActionEvent e)
		{
			if( UISupport.confirm("Clear all parameter values?", "Clear Parameters"))
			{
				for( String name : params.getPropertyNames() )
				{
					params.getProperty( name ).setValue( null );
				}
			}
		}
	}

	private class MovePropertyUpAction extends AbstractAction
	{
		public MovePropertyUpAction()
		{
			putValue(Action.SMALL_ICON, UISupport.createImageIcon("/up_arrow.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Moves selected parameter up one row");
			setEnabled(false);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			int ix = paramsTable.getSelectedRow();
			if( ix != -1 )
			{
				params.moveProperty(	params.getPropertyAt(ix).getName(), ix-1 );
				paramsTable.setRowSelectionInterval(ix-1,ix-1);
			}
		}
	}
	
	private class MovePropertyDownAction extends AbstractAction
	{
		public MovePropertyDownAction()
		{
			putValue(Action.SMALL_ICON, UISupport.createImageIcon("/down_arrow.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Moves selected parameter down one row");
			setEnabled(false);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			int ix = paramsTable.getSelectedRow();
			if( ix != -1 )
			{
				params.moveProperty(	params.getPropertyAt(ix).getName(), ix+1 );
				paramsTable.setRowSelectionInterval(ix+1, ix+1);
			}
		}
	}
}
