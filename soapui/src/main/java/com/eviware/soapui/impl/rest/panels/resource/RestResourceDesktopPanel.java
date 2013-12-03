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

package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.actions.resource.NewRestMethodAction;
import com.eviware.soapui.impl.rest.panels.component.RestResourceEditor;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;
import org.apache.commons.lang.mutable.MutableBoolean;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;

public class RestResourceDesktopPanel extends ModelItemDesktopPanel<RestResource>
{
	public static final String REST_RESOURCE_EDITOR = "rest-resource-editor";
	// package protected to facilitate unit testing
	JTextField pathTextField;

	private MutableBoolean updating = new MutableBoolean();
	private RestParamsTable paramsTable;

	public RestResourceDesktopPanel( RestResource modelItem )
	{
		super( modelItem );
		setName( REST_RESOURCE_EDITOR );
		add( buildToolbar(), BorderLayout.NORTH );
		add( buildContent(), BorderLayout.CENTER );
	}

	private Component buildContent()
	{
		JTabbedPane tabs = new JTabbedPane();
		paramsTable = new RestParamsTable( getModelItem().getParams(), true, ParamLocation.RESOURCE, true, false );
		tabs.addTab( "Resource Parameters", paramsTable );
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

	private String getName( RestResource modelItem )
	{
		if( modelItem.getParentResource() != null )
			return getName( modelItem.getParentResource() ) + "/" + modelItem.getName();
		else
			return modelItem.getName();
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		toolbar.addFixed( createActionButton( SwingActionDelegate.createDelegate( NewRestMethodAction.SOAPUI_ACTION_ID,
				getModelItem(), null, "/create_empty_method.gif" ), true ) );

		toolbar.addSeparator();

		pathTextField = new RestResourceEditor( getModelItem(), updating );

		toolbar.addFixed( new JLabel( "Resource Path" ) );
		toolbar.addSeparator( new Dimension( 3, 3 ) );
		toolbar.addWithOnlyMinimumHeight( pathTextField );

		toolbar.addGlue();
		toolbar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.RESTRESOURCEEDITOR_HELPURL ) ) );

		return toolbar;
	}

	@Override
	public boolean dependsOn( ModelItem modelItem )
	{
		return getModelItem().dependsOn( modelItem );
	}

	public boolean onClose( boolean canCancel )
	{
		return release();
	}

	@Override
	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( "path" ) )
		{
			if( !updating.booleanValue() )
			{
				updating.setValue( true );
				pathTextField.setText( getModelItem().getFullPath() );
				updating.setValue( false );
			}
		}
		paramsTable.refresh();
		super.propertyChange( evt );
	}
}
