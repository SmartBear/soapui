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

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.actions.resource.NewRestMethodAction;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;

public class RestResourceDesktopPanel extends ModelItemDesktopPanel<RestResource>
{
	// package protected to facilitate unit testing
	JUndoableTextField pathTextField;

	private boolean updating;
	private RestParamsTable paramsTable;

	public RestResourceDesktopPanel( RestResource modelItem )
	{
		super( modelItem );

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

		pathTextField = new JUndoableTextField( getModelItem().getFullPath(), 20 );
		pathTextField.getDocument().addDocumentListener( new DocumentListenerAdapter()
		{
			public void update( Document document )
			{
				if( !updating )
				{
					updating = true;
					getModelItem().setPath( extractCurrentResourcePathFrom( getText( document ) ) );
					updating = false;
				}
			}
		} );
		pathTextField.addFocusListener( new FocusListener()
		{
			public void focusLost( FocusEvent e )
			{
				for( String p : RestUtils.extractTemplateParams( getModelItem().getPath() ) )
				{
					if( !getModelItem().hasProperty( p ) )
					{
						if( UISupport.confirm( "Add template parameter [" + p + "] to resource?", "Add Parameter" ) )
						{
							RestParamProperty property = getModelItem().addProperty( p );
							property.setStyle( ParameterStyle.TEMPLATE );
							String value = UISupport.prompt( "Specify default value for parameter [" + p + "]",
									"Add Parameter", "" );
							if( value != null )
								property.setDefaultValue( value );
						}
					}
				}
			}

			public void focusGained( FocusEvent e )
			{
			}
		} );

		toolbar.addFixed( new JLabel( "Resource Path" ) );
		toolbar.addSeparator( new Dimension( 3, 3 ) );
		toolbar.addWithOnlyMinimumHeight( pathTextField );

		toolbar.addGlue();
		toolbar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.RESTRESOURCEEDITOR_HELPURL ) ) );

		return toolbar;
	}

	private String extractCurrentResourcePathFrom( String fullPath )
	{
		RestResource parentResource = getModelItem().getParentResource();
		if ( parentResource == null)
		{
			return fullPath;
		}
		String parentPath = parentResource.getFullPath();
		if (fullPath.startsWith( parentPath + "/" ))
		{
			return fullPath.substring(parentPath.length() + 1);
		}
		return fullPath.contains("/") ? fullPath.substring(fullPath.lastIndexOf( '/' ) + 1) : fullPath;
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
			if( !updating )
			{
				updating = true;
				pathTextField.setText( String.valueOf( evt.getNewValue() ) );
				updating = false;
			}
		}
		paramsTable.refresh();
		super.propertyChange( evt );
	}
}
