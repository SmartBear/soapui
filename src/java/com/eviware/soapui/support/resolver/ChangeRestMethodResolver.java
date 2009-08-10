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

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.project.SimpleDialog;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;
import com.eviware.soapui.support.swing.ModelItemListCellRenderer;

public abstract class ChangeRestMethodResolver implements Resolver
{
	private boolean resolved = false;
	private WsdlProject project;
	private RestMethod selectedMethod;

	public ChangeRestMethodResolver( RestTestRequestStep testStep )
	{
		this.project = testStep.getTestCase().getTestSuite().getProject();
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
		PropertyChangeDialog pDialog = new PropertyChangeDialog( "Resolve REST Method" );
		pDialog.setVisible( true );
		resolved = update();
		return resolved;
	}

	public abstract boolean update();

	protected abstract Interface[] getInterfaces( WsdlProject project );

	public String getDescription()
	{
		return "Resolve: Select another REST Method";
	}

	@Override
	public String toString()
	{
		return getDescription();
	}

	@SuppressWarnings( "serial" )
	private class PropertyChangeDialog extends SimpleDialog
	{
		private JComboBox serviceCombo;
		private JComboBox resourceCombo;
		private JComboBox methodCombo;

		public PropertyChangeDialog( String title )
		{
			super( title, getDescription(), null );
		}

		protected Component buildContent()
		{
			SimpleForm form = new SimpleForm();

			form.addSpace( 5 );
			Interface[] ifaces = getInterfaces( project );
			DefaultComboBoxModel serviceComboModel = new DefaultComboBoxModel();
			serviceCombo = form.appendComboBox( "REST Services", serviceComboModel, "Target Service" );
			serviceCombo.setRenderer( new ModelItemListCellRenderer() );
			for( Interface element : ifaces )
			{
				if( element instanceof RestService )
					serviceComboModel.addElement( element );
			}

			resourceCombo = form.appendComboBox( "REST Resources", ( ( RestService )serviceCombo.getSelectedItem() )
					.getOperationList().toArray(), "Target Resource" );
			resourceCombo.setRenderer( new ModelItemListCellRenderer() );

			methodCombo = form.appendComboBox( "REST Methods", ( ( RestResource )resourceCombo.getSelectedItem() )
					.getRestMethodList().toArray(), "Target Method" );
			methodCombo.setRenderer( new ModelItemListCellRenderer() );

			serviceCombo.addActionListener( new ActionListener()
			{
				public void actionPerformed( ActionEvent e )
				{
					Interface iface = project.getInterfaceByName( ( ( Interface )serviceCombo.getSelectedItem() ).getName() );
					resourceCombo.removeAllItems();
					if( iface != null )
					{
						resourceCombo.setEnabled( true );
						for( Operation op : iface.getOperationList() )
							resourceCombo.addItem( op );
					}
					else
					{
						resourceCombo.setEnabled( false );
					}
				}
			} );

			resourceCombo.addActionListener( new ActionListener()
			{
				public void actionPerformed( ActionEvent e )
				{
					RestResource resource = ( RestResource )resourceCombo.getSelectedItem();
					methodCombo.removeAllItems();
					if( resource != null )
					{
						methodCombo.setEnabled( true );
						for( RestMethod method : resource.getRestMethodList() )
							methodCombo.addItem( method );
					}
					else
					{
						methodCombo.setEnabled( false );
					}
				}
			} );

			form.addSpace( 5 );
			return form.getPanel();
		}

		protected boolean handleOk()
		{
			selectedMethod = ( RestMethod )methodCombo.getSelectedItem();
			return true;
		}
	}

	public RestMethod getSelectedRestMethod()
	{
		return selectedMethod;
	}

}
