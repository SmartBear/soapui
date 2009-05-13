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

package com.eviware.soapui.impl.wsdl.actions.iface;

import java.util.Arrays;
import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.panels.mock.WsdlMockServiceDesktopPanel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

/**
 * Generates a MockService for a specified Interface
 * 
 * @author ole.matzura
 */

public class GenerateMockServiceAction extends AbstractSoapUIAction<WsdlInterface>
{
	private static final String CREATE_MOCKSUITE_OPTION = "<create>";

	public GenerateMockServiceAction()
	{
		super( "Generate MockService", "Generates a MockService containing all Operations in this Interface" );
	}

	public void perform( WsdlInterface iface, Object param )
	{
		generateMockService( iface, false );
	}

	public WsdlMockService generateMockService( WsdlInterface iface, boolean atCreation )
	{
		XFormDialog dialog = ADialogBuilder.buildDialog( Form.class );
		dialog.setBooleanValue( Form.ADD_ENDPOINT, true );
		String[] names = ModelSupport.getNames( iface.getOperationList() );
		dialog.setOptions( Form.OPERATIONS, names );
		XFormOptionsField operationsFormField = ( XFormOptionsField )dialog.getFormField( Form.OPERATIONS );
		operationsFormField.setSelectedOptions( names );

		dialog.getFormField( Form.START_MOCKSERVICE ).setEnabled( !atCreation );

		WsdlProject project = ( WsdlProject )iface.getProject();
		String[] mockServices = ModelSupport.getNames( new String[] { CREATE_MOCKSUITE_OPTION }, project
				.getMockServiceList() );
		dialog.setOptions( Form.MOCKSERVICE, mockServices );

		dialog.setValue( Form.PATH, "/mock" + iface.getName() );
		dialog.setValue( Form.PORT, "8088" );

		if( dialog.show() )
		{
			List<String> operations = Arrays.asList( operationsFormField.getSelectedOptions() );
			if( operations.size() == 0 )
			{
				UISupport.showErrorMessage( "No Operations selected.." );
				return null;
			}

			String mockServiceName = dialog.getValue( Form.MOCKSERVICE );
			WsdlMockService mockService = ( WsdlMockService )project.getMockServiceByName( mockServiceName );

			if( mockService == null || mockServiceName.equals( CREATE_MOCKSUITE_OPTION ) )
			{
				mockServiceName = UISupport.prompt( "Specify name of MockService to create", getName(), iface.getName()
						+ " MockService" );
				if( mockServiceName == null )
					return null;

				mockService = ( WsdlMockService )project.addNewMockService( mockServiceName );
			}

			mockService.setPath( dialog.getValue( Form.PATH ) );
			try
			{
				mockService.setPort( Integer.parseInt( dialog.getValue( Form.PORT ) ) );
			}
			catch( NumberFormatException e1 )
			{
			}

			for( int i = 0; i < iface.getOperationCount(); i++ )
			{
				WsdlOperation operation = ( WsdlOperation )iface.getOperationAt( i );
				if( !operations.contains( operation.getName() ) )
					continue;

				WsdlMockOperation mockOperation = ( WsdlMockOperation )mockService.addNewMockOperation( operation );
				if( mockOperation != null )
					mockOperation.addNewMockResponse( "Response 1", true );
			}

			if( dialog.getBooleanValue( Form.ADD_ENDPOINT ) )
			{
				iface.addEndpoint( mockService.getLocalEndpoint() );
			}

			if( !atCreation )
			{
				WsdlMockServiceDesktopPanel desktopPanel = ( WsdlMockServiceDesktopPanel )UISupport
						.showDesktopPanel( mockService );

				if( dialog.getBooleanValue( Form.START_MOCKSERVICE ) )
				{
					desktopPanel.startMockService();
					SoapUI.getDesktop().minimize( desktopPanel );
				}
			}

			return mockService;
		}

		return null;
	}

	@AForm( name = "Generate MockService", description = "Set options for generated MockOperations for this Interface", helpUrl = HelpUrls.GENERATE_MOCKSERVICE_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	private interface Form
	{
		@AField( name = "MockService", description = "The MockService to create or use", type = AFieldType.ENUMERATION )
		public final static String MOCKSERVICE = "MockService";

		@AField( name = "Operations", description = "The Operations for which to Generate MockOperations", type = AFieldType.MULTILIST )
		public final static String OPERATIONS = "Operations";

		@AField( name = "Path", description = "The URL path to mount on", type = AFieldType.STRING )
		public final static String PATH = "Path";

		@AField( name = "Port", description = "The endpoint port to listen on", type = AFieldType.STRING )
		public final static String PORT = "Port";

		@AField( name = "Add Endpoint", description = "Adds the MockServices endpoint to the mocked Interface", type = AFieldType.BOOLEAN )
		public final static String ADD_ENDPOINT = "Add Endpoint";

		@AField( name = "Start MockService", description = "Starts the MockService immediately", type = AFieldType.BOOLEAN )
		public final static String START_MOCKSERVICE = "Start MockService";
	}
}
