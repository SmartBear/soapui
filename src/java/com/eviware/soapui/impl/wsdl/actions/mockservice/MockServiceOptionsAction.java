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

package com.eviware.soapui.impl.wsdl.actions.mockservice;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

/**
 * Displays the options for the specified WsdlMockService
 * 
 * @author ole.matzura
 */

public class MockServiceOptionsAction extends AbstractSoapUIAction<WsdlMockService>
{
	private XFormDialog dialog;

	public MockServiceOptionsAction()
	{
		super( "Options", "Sets options for this MockService" );
	}

	public void perform( WsdlMockService mockService, Object param )
	{
		if( mockService.getMockRunner() != null && mockService.getMockRunner().isRunning() )
		{
			UISupport.showErrorMessage( "Can not set MockService options while running" );
			return;
		}

		if( dialog == null )
			dialog = ADialogBuilder.buildDialog( OptionsForm.class );

		dialog.setValue( OptionsForm.PATH, mockService.getPath() );
		dialog.setValue( OptionsForm.HOST, mockService.getHost() );
		dialog.setIntValue( OptionsForm.PORT, mockService.getPort() );
		dialog.setBooleanValue( OptionsForm.HOSTONLY, mockService.getBindToHostOnly() );
		dialog.setValue( OptionsForm.DOCROOT, mockService.getDocroot() );
		dialog.setOptions( OptionsForm.FAULT_OPERATION, ModelSupport.getNames( new String[] { "- none -" }, mockService
				.getMockOperationList() ) );
		dialog.setValue( OptionsForm.FAULT_OPERATION, String.valueOf( mockService.getFaultMockOperation() ) );

		if( dialog.show() )
		{
			mockService.setPath( dialog.getValue( OptionsForm.PATH ) );
			mockService.setPort( dialog.getIntValue( OptionsForm.PORT, mockService.getPort() ) );
			mockService.setHost( dialog.getValue( OptionsForm.HOST ) );
			mockService.setBindToHostOnly( dialog.getBooleanValue( OptionsForm.HOSTONLY ) );
			mockService.setDocroot( dialog.getValue( OptionsForm.DOCROOT ) );
			mockService.setFaultMockOperation( mockService.getMockOperationByName( dialog
					.getValue( OptionsForm.FAULT_OPERATION ) ) );
		}
	}

	@AForm( name = "MockService Options", description = "Set options for this MockService", helpUrl = HelpUrls.MOCKSERVICEOPTIONS_HELP_URL, icon = UISupport.OPTIONS_ICON_PATH )
	private class OptionsForm
	{
		@AField( name = "Path", description = "The path this MockService will mount on" )
		public final static String PATH = "Path";

		@AField( name = "Port", description = "The port this MockService will mount on", type = AFieldType.INT )
		public final static String PORT = "Port";

		@AField( name = "Host", description = "The local host to bind to and use in Port endpoints" )
		public final static String HOST = "Host";

		@AField( name = "Host Only", description = "Only binds to specified host", type = AFieldType.BOOLEAN )
		public final static String HOSTONLY = "Host Only";

		@AField( name = "Docroot", description = "The document root to serve (empty = none)", type = AFieldType.FOLDER )
		public final static String DOCROOT = "Docroot";

		@AField( name = "Fault Operation", description = "The MockOperation that should handle incoming SOAP Faults", type = AFieldType.ENUMERATION )
		public final static String FAULT_OPERATION = "Fault Operation";
	}
}
