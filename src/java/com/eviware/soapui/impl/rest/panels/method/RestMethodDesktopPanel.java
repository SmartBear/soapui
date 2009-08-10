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

package com.eviware.soapui.impl.rest.panels.method;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JComboBox;
import javax.swing.JTabbedPane;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.method.NewRestRequestAction;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

public class RestMethodDesktopPanel extends ModelItemDesktopPanel<RestMethod>
{
	private RestParamsTable paramsTable;
	private boolean updatingRequest;
	private JComboBox methodCombo;

	public RestMethodDesktopPanel( RestMethod modelItem )
	{
		super( modelItem );

		add( buildToolbar(), BorderLayout.NORTH );
		add( buildContent(), BorderLayout.CENTER );
	}

	private Component buildContent()
	{
		JTabbedPane tabs = new JTabbedPane();

		paramsTable = new RestParamsTable( getModelItem().getParams(), true );
		tabs.addTab( "Method Parameters", paramsTable );

		tabs
				.addTab( "Representations", new RestRepresentationsTable( getModelItem(), new RestRepresentation.Type[] {
						RestRepresentation.Type.REQUEST, RestRepresentation.Type.RESPONSE, RestRepresentation.Type.FAULT },
						false ) );

		/*
		 * tabs.addTab("Response Representations", new RestRepresentationsTable(
		 * getModelItem(), new RestRepresentation.Type[] {
		 * RestRepresentation.Type.RESPONSE, RestRepresentation.Type.FAULT },
		 * false));
		 */

		return UISupport.createTabPanel( tabs, false );
	}

	@Override
	public String getTitle()
	{
		return getName( getModelItem() );
	}

	public RestParamsTable getParamsTable()
	{
		return paramsTable;
	}

	@Override
	protected boolean release()
	{
		paramsTable.release();
		return super.release();
	}

	private String getName( RestMethod modelItem )
	{
		return modelItem.getName();
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		methodCombo = new JComboBox( RestRequestInterface.RequestMethod.getMethods() );

		methodCombo.setSelectedItem( getModelItem().getMethod() );
		methodCombo.setToolTipText( "Set desired HTTP method" );
		methodCombo.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				updatingRequest = true;
				getModelItem().setMethod( ( RestRequestInterface.RequestMethod )methodCombo.getSelectedItem() );
				updatingRequest = false;
			}
		} );

		toolbar.addLabeledFixed( "HTTP method", methodCombo );
		toolbar.addSeparator();

		toolbar.addFixed( createActionButton( SwingActionDelegate.createDelegate( NewRestRequestAction.SOAPUI_ACTION_ID,
				getModelItem(), null, "/create_empty_request.gif" ), true ) );

		toolbar.addSeparator();

		toolbar.addGlue();
		toolbar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.RESTMETHODEDITOR_HELP_URL ) ) );

		return toolbar;
	}

	@Override
	public boolean dependsOn( ModelItem modelItem )
	{
		return getModelItem().dependsOn( modelItem );
	}

	public boolean onClose( boolean canCancel )
	{
		return true;
	}

	@Override
	public void propertyChange( PropertyChangeEvent evt )
	{
		super.propertyChange( evt );

		if( evt.getPropertyName().equals( "method" ) && !updatingRequest )
		{
			methodCombo.setSelectedItem( evt.getNewValue() );
		}
	}

}
