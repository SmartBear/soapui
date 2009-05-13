/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

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

package com.eviware.soapui.impl.wsdl.actions.project;

import java.io.File;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.support.WadlImporter;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

/**
 * Action for creating a new WSDL project
 * 
 * @author Ole.Matzura
 */

public class AddWadlAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "NewWsdlProjectAction";
	private XFormDialog dialog;

	public static final MessageSupport messages = MessageSupport.getMessages( AddWadlAction.class );

	public AddWadlAction()
	{
		super( messages.get( "Title" ), messages.get( "Description" ) );
	}

	public void perform( WsdlProject project, Object param )
	{
		PropertyExpansionContext context = new DefaultPropertyExpansionContext( project.getModelItem() );
		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( Form.class );
			dialog.getFormField( Form.INITIALWSDL ).addFormFieldListener( new XFormFieldListener()
			{
				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					String value = newValue.toLowerCase().trim();

					dialog.getFormField( Form.GENERATETESTSUITE ).setEnabled( newValue.trim().length() > 0 );
				}
			} );
		}
		else
		{
			dialog.setValue( Form.INITIALWSDL, "" );
			dialog.getFormField( Form.GENERATETESTSUITE ).setEnabled( false );
		}

		while( dialog.show() )
		{
			try
			{
				String url = dialog.getValue( Form.INITIALWSDL ).trim();
				if( StringUtils.hasContent( url ) )
				{
					String expUrl = PathUtils.expandPath( url, project );

					if( new File( expUrl ).exists() )
						expUrl = new File( expUrl ).toURI().toURL().toString();

					RestService result = importWadl( project, expUrl );
					if( !url.equals( expUrl ) && result != null )
					{
						result.setWadlUrl( url );
					}
					break;
				}
			}
			catch( Exception ex )
			{
				UISupport.showErrorMessage( ex );
			}
		}
	}

	private RestService importWadl( WsdlProject project, String url )
	{
		RestService restService = ( RestService )project
				.addNewInterface( project.getName(), RestServiceFactory.REST_TYPE );
		UISupport.select( restService );
		try
		{
			new WadlImporter( restService ).initFromWadl( url );
		}
		catch( Exception e )
		{
			UISupport.showErrorMessage( e );
		}

		return restService;
	}

	@AForm( name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWPROJECT_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( description = "Form.InitialWadl.Description", type = AFieldType.FILE )
		public final static String INITIALWSDL = messages.get( "Form.InitialWadl.Label" );

		@AField( description = "Form.GenerateTestSuite.Description", type = AFieldType.BOOLEAN, enabled = false )
		public final static String GENERATETESTSUITE = messages.get( "Form.GenerateTestSuite.Label" );
	}
}