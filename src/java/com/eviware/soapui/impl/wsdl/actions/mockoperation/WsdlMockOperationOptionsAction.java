/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.actions.mockoperation;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.config.DispatchStyleConfig;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.iface.AbstractSwingAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.util.ModelItemNames;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

/**
 * Displays the options for the specified WsdlMockOperation
 * 
 * @author ole.matzura
 */

public class WsdlMockOperationOptionsAction extends AbstractSwingAction<WsdlMockOperation>
{
	private XFormDialog dialog;
	private DefineNamespacesAction defineNamespacesAction;
	private WsdlProject project;

	public WsdlMockOperationOptionsAction( WsdlMockOperation mockOperation )
	{
		super( "MockOperation Options", "Sets options for this MockOperation", mockOperation );
		
		project = mockOperation.getMockService().getProject();
	}
	
	@Override
	public void actionPerformed( ActionEvent arg0, WsdlMockOperation mockOperation )
	{
		if( mockOperation.getOperation() == null )
		{
			UISupport.showErrorMessage( "Missing operation for this mock response" );
			return;
		}
		
		if( dialog == null )
		{
			DefaultActionList actions = new DefaultActionList();
			defineNamespacesAction = new DefineNamespacesAction( mockOperation );
			actions.addAction( defineNamespacesAction  );
			
			dialog = ADialogBuilder.buildDialog( Form.class, actions );
			dialog.getFormField( Form.DISPATCH_STYLE ).addFormFieldListener( new XFormFieldListener() {

				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					boolean enabled = newValue.equals( "XPATH" ) || newValue.equals( "SCRIPT" );
					
					enableXPathFields( enabled );
					defineNamespacesAction.setEnabled( enabled);
				}} );
			
			dialog.getFormField( Form.INTERFACE ).addFormFieldListener( new XFormFieldListener() {

				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					WsdlInterface iface = (WsdlInterface) project.getInterfaceByName( newValue );
					dialog.setOptions( Form.OPERATION,  
								new ModelItemNames<Operation>( iface.getOperationList() ).getNames() );
				}} );
		}

		List<Interface> interfaces = new ArrayList<Interface>();
		for( int c = 0; c < project.getInterfaceCount(); c++ )
		{
			AbstractInterface<?> iface = project.getInterfaceAt( c );
			if( iface.getInterfaceType().equals(WsdlInterfaceFactory.WSDL_TYPE) && iface.getOperationCount() > 0 )
				interfaces.add( iface);
		}
		
		dialog.setOptions( Form.INTERFACE, new ModelItemNames<Interface>( interfaces ).getNames() );
		dialog.setOptions( Form.OPERATION,  
					new ModelItemNames<Operation>( interfaces.get( 0 ).getOperationList() ).getNames() );

		dialog.setValue( Form.INTERFACE, mockOperation.getOperation().getInterface().getName() );
		dialog.setValue( Form.OPERATION, mockOperation.getOperation().getName() );
		
		dialog.setOptions( Form.DEFAULT_RESPONSE, 
					new ModelItemNames<MockResponse>( mockOperation.getMockResponses() ).getNames() );
		
		dialog.setValue( Form.DISPATCH_STYLE, mockOperation.getDispatchStyle().toString() );
		dialog.setValue( Form.DISPATCH_PATH, mockOperation.getDispatchPath() );
		dialog.setValue( Form.DEFAULT_RESPONSE, mockOperation.getDefaultResponse() );
		
		enableXPathFields( dialog.getValue( Form.DISPATCH_STYLE ).equals( "XPATH" ) ||
					dialog.getValue( Form.DISPATCH_STYLE ).equals( "SCRIPT" ));
		
		if( dialog.show() )
		{
			mockOperation.setDispatchStyle( 
						DispatchStyleConfig.Enum.forString( dialog.getValue( Form.DISPATCH_STYLE )) );
			mockOperation.setDispatchPath( dialog.getValue( Form.DISPATCH_PATH ));
			mockOperation.setDefaultResponse( dialog.getValue( Form.DEFAULT_RESPONSE ) );
			
			WsdlInterface iface = (WsdlInterface) project.getInterfaceByName( dialog.getValue( Form.INTERFACE ) );
			WsdlOperation operation = iface.getOperationByName( dialog.getValue( Form.OPERATION ) );
			
			if( operation != mockOperation.getOperation() )
				mockOperation.setOperation( operation );
		}
	}
	
	private void enableXPathFields( boolean enabled )
	{
		dialog.getFormField( Form.DISPATCH_PATH ).setEnabled( enabled);
		dialog.getFormField( Form.DEFAULT_RESPONSE ).setEnabled( enabled);
		defineNamespacesAction.setEnabled( enabled );
	}

	@AForm(description="Set options for this MockOperation", name="MockOperation Options",
			 helpUrl=HelpUrls.MOCKOPERATIONOPTIONS_HELP_URL, icon=UISupport.OPTIONS_ICON_PATH)
	private class Form
	{
		@AField(description = "Specifies the operation to be mocked", name = "Operation", type = AFieldType.ENUMERATION)
		public final static String OPERATION = "Operation";
		
		@AField(description = "Specifies the interface containing the operation to be mocked", name = "Interface", type = AFieldType.ENUMERATION)
		public final static String INTERFACE = "Interface";
		
		@AField(description="How to dispatch requests to responses", name="Dispatch Style", 
					type=AFieldType.ENUMERATION, values= {"SEQUENCE", "RANDOM", "XPATH", "SCRIPT" })
		public final static String DISPATCH_STYLE = "Dispatch Style";
		
		@AField(description="The XPath to use for selecting the corresponding response", name="Dispatch Path",
					type=AFieldType.STRINGAREA)
		public final static String DISPATCH_PATH = "Dispatch Path";
		
		@AField(description="Default response to use", name="Default Response", 
					type=AFieldType.ENUMERATION )
		public final static String DEFAULT_RESPONSE = "Default Response";
	}
	
	public class DefineNamespacesAction extends AbstractSwingAction<WsdlMockOperation>
	{
		public DefineNamespacesAction( WsdlMockOperation mockOperation )
		{
			super( "Define Namespaces", "Defines namespaces from last Mock Request", mockOperation );
		}

		@Override
		public void actionPerformed( ActionEvent arg0, WsdlMockOperation mockOperation )
		{
			WsdlMockResult result = mockOperation.getLastMockResult();
			if( result == null || result.getMockRequest() == null )
			{
				UISupport.showErrorMessage( "Missing request to define from" );
			}
			else
			{
				try
				{
					String ns = XmlUtils.declareXPathNamespaces( result.getMockRequest().getRequestContent() );
					if( ns != null )
					{
						ns += dialog.getValue( Form.DISPATCH_PATH );
						dialog.setValue( Form.DISPATCH_PATH, ns );
					}
				}
				catch( Exception e )
				{
					UISupport.showErrorMessage( e );
				}
			}	
		}
	}
}
