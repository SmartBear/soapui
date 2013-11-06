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

package com.eviware.soapui.impl.wsdl.actions.project;

import java.io.File;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.definition.support.InvalidDefinitionException;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.iface.GenerateMockServiceAction;
import com.eviware.soapui.impl.wsdl.actions.iface.GenerateWsdlTestSuiteAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

/**
 * Action for creating a new WSDL project
 * 
 * @author Ole.Matzura
 */

public class AddWsdlAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "NewWsdlProjectAction";
	private XFormDialog dialog;

	public static final MessageSupport messages = MessageSupport.getMessages( AddWsdlAction.class );

	public AddWsdlAction()
	{
		super( messages.get( "Title" ), messages.get( "Description" ) );
	}

	public void perform( WsdlProject project, Object param )
	{
		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( Form.class );
			dialog.setValue( Form.CREATEREQUEST, Boolean.toString( true ) );
			dialog.getFormField( Form.INITIALWSDL ).addFormFieldListener( new XFormFieldListener()
			{
				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					String value = newValue.toLowerCase().trim();

					dialog.getFormField( Form.CREATEREQUEST ).setEnabled( value.length() > 0 );
					dialog.getFormField( Form.GENERATEMOCKSERVICE ).setEnabled( newValue.trim().length() > 0 );
					dialog.getFormField( Form.GENERATETESTSUITE ).setEnabled( newValue.trim().length() > 0 );
				}
			} );
		}
		else
		{
			dialog.setValue( Form.INITIALWSDL, "" );

			dialog.getFormField( Form.CREATEREQUEST ).setEnabled( false );
			dialog.getFormField( Form.GENERATEMOCKSERVICE ).setEnabled( false );
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
						url = new File( expUrl ).toURI().toURL().toString();

					WsdlInterface[] results = importWsdl( project, expUrl );

					if( !url.equals( expUrl ) )
					{
						for( WsdlInterface iface : results )
						{
							iface.setDefinition( url, false );
						}
					}

					break;
				}
			}
			catch( InvalidDefinitionException ex )
			{
				ex.show();
			}
			catch( Exception ex )
			{
				UISupport.showErrorMessage( ex );
			}
		}
	}

	private WsdlInterface[] importWsdl( WsdlProject project, String url ) throws SoapUIException
	{
		WsdlInterface[] results = WsdlInterfaceFactory.importWsdl( project, url, dialog.getValue( Form.CREATEREQUEST )
				.equals( "true" ) );
		for( WsdlInterface iface : results )
		{
			UISupport.select( iface );

			if( dialog.getValue( Form.GENERATETESTSUITE ).equals( "true" ) )
			{
				GenerateWsdlTestSuiteAction generateTestSuiteAction = new GenerateWsdlTestSuiteAction();
				generateTestSuiteAction.generateTestSuite( iface, true );
			}

			if( dialog.getValue( Form.GENERATEMOCKSERVICE ).equals( "true" ) )
			{
				GenerateMockServiceAction generateMockAction = new GenerateMockServiceAction();
				generateMockAction.generateMockService( iface, false );
			}
		}

		return results;
	}

	@AForm( name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWPROJECT_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( description = "Form.InitialWsdl.Description", type = AFieldType.FILE )
		public final static String INITIALWSDL = messages.get( "Form.InitialWsdl.Label" );

		@AField( description = "Form.CreateRequests.Description", type = AFieldType.BOOLEAN, enabled = false )
		public final static String CREATEREQUEST = messages.get( "Form.CreateRequests.Label" );

		@AField( description = "Form.GenerateTestSuite.Description", type = AFieldType.BOOLEAN, enabled = false )
		public final static String GENERATETESTSUITE = messages.get( "Form.GenerateTestSuite.Label" );

		@AField( description = "Form.GenerateMockService.Description", type = AFieldType.BOOLEAN, enabled = false )
		public final static String GENERATEMOCKSERVICE = messages.get( "Form.GenerateMockService.Label" );
	}
}
