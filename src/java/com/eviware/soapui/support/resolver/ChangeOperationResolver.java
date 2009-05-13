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

package com.eviware.soapui.support.resolver;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.project.SimpleDialog;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;
import com.eviware.soapui.support.swing.ModelItemListCellRenderer;

public abstract class ChangeOperationResolver implements Resolver
{
	private boolean resolved = false;
	private WsdlProject project;
	private Operation selectedOperation;
	private String operationType;

	public ChangeOperationResolver( WsdlTestStep testStep, String operationType )
	{
		this.project = testStep.getTestCase().getTestSuite().getProject();

		this.operationType = operationType;
	}

	public String getResolvedPath()
	{
		return "";
	}

	public boolean isResolved()
	{
		return resolved;
	}

	public boolean resolve()
	{
		PropertyChangeDialog pDialog = new PropertyChangeDialog( "Resolve " + operationType );
		pDialog.setVisible( true );
		resolved = update();
		return resolved;
	}

	public abstract boolean update();

	protected abstract Interface[] getInterfaces( WsdlProject project );

	public String getDescription()
	{
		return "Resolve: Select another " + operationType;
	}

	@Override
	public String toString()
	{
		return getDescription();
	}

	@SuppressWarnings( "serial" )
	private class PropertyChangeDialog extends SimpleDialog
	{
		private JComboBox sourceStepCombo;
		private JComboBox propertiesCombo;

		public PropertyChangeDialog( String title )
		{
			super( title, getDescription(), null );
		}

		protected Component buildContent()
		{
			SimpleForm form = new SimpleForm();

			form.addSpace( 5 );
			Interface[] ifaces = getInterfaces( project );
			DefaultComboBoxModel sourceStepComboModel = new DefaultComboBoxModel();
			sourceStepCombo = form.appendComboBox( "Interfaces", sourceStepComboModel, "Target Interface" );
			sourceStepCombo.setRenderer( new ModelItemListCellRenderer() );
			for( Interface element : ifaces )
				sourceStepComboModel.addElement( element );

			propertiesCombo = form.appendComboBox( operationType, ( ( Interface )sourceStepCombo.getSelectedItem() )
					.getOperationList().toArray(), "Target " + operationType );
			propertiesCombo.setRenderer( new ModelItemListCellRenderer() );

			sourceStepCombo.addActionListener( new ActionListener()
			{
				public void actionPerformed( ActionEvent e )
				{
					Interface iface = project.getInterfaceByName( ( ( Interface )sourceStepCombo.getSelectedItem() )
							.getName() );
					propertiesCombo.removeAllItems();
					if( iface != null )
					{
						propertiesCombo.setEnabled( true );
						for( Operation op : iface.getOperationList() )
							propertiesCombo.addItem( op );
					}
					else
					{
						propertiesCombo.setEnabled( false );
					}
				}
			} );

			form.addSpace( 5 );
			return form.getPanel();
		}

		protected boolean handleOk()
		{
			selectedOperation = ( Operation )propertiesCombo.getSelectedItem();
			return true;
		}
	}

	public Operation getSelectedOperation()
	{
		return selectedOperation;
	}

}
