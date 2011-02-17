package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.model.security.SecurityParametersTableModel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;

public class SecurityCheckedParametersTable extends JPanel
{

	private SecurityParametersTableModel model;
	private JXToolBar toolbar;

	public SecurityCheckedParametersTable( SecurityParametersTableModel model )
	{
		this.model = model;

		init();
	}

	private void init()
	{
		setLayout( new BorderLayout() );
		toolbar = UISupport.createToolbar();

		toolbar.add( UISupport.createToolbarButton( new AddNewParameterAction() ) );
		toolbar.add( UISupport.createToolbarButton( new RemoveParameterAction() ) );
		toolbar.addGlue();

		add( toolbar, BorderLayout.NORTH );

		add( new JScrollPane( new JXTable( model ) ), BorderLayout.CENTER );

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
			model.addParameter();
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
			// TODO Auto-generated method stub

		}

	}
}
