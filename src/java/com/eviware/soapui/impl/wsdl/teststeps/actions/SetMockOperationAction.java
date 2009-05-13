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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.util.ModelItemNames;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

public class SetMockOperationAction extends AbstractSoapUIAction<WsdlMockResponseTestStep>
{
	private XFormDialog dialog;
	private WsdlProject project;

	public SetMockOperationAction()
	{
		super( "Set MockOperation", "Sets which Operation to Mock" );
	}

	public void perform( WsdlMockResponseTestStep mockResponseTestStep, Object param )
	{
		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( CreateForm.class );
			dialog.getFormField( CreateForm.INTERFACE ).addFormFieldListener( new XFormFieldListener()
			{

				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					updateOperations( newValue );
				}
			} );
		}

		project = mockResponseTestStep.getTestCase().getTestSuite().getProject();
		List<Interface> interfaces = new ArrayList<Interface>();
		for( int c = 0; c < project.getInterfaceCount(); c++ )
		{
			if( project.getInterfaceAt( c ).getOperationCount() > 0 )
				interfaces.add( project.getInterfaceAt( c ) );
		}

		dialog.setOptions( CreateForm.INTERFACE, new ModelItemNames<Interface>( interfaces ).getNames() );
		String ifaceName = mockResponseTestStep.getOperation().getInterface().getName();
		updateOperations( ifaceName );

		dialog.setValue( CreateForm.INTERFACE, ifaceName );
		dialog.setValue( CreateForm.OPERATION, mockResponseTestStep.getOperation().getName() );

		if( dialog.show() )
		{
			mockResponseTestStep.setInterface( dialog.getValue( CreateForm.INTERFACE ) );
			mockResponseTestStep.setOperation( dialog.getValue( CreateForm.OPERATION ) );
		}
	}

	private void updateOperations( String interfaceName )
	{
		WsdlInterface iface = ( WsdlInterface )project.getInterfaceByName( interfaceName );
		dialog.setOptions( CreateForm.OPERATION, new ModelItemNames<Operation>( iface.getOperationList() ).getNames() );
	}

	@AForm( description = "Set the Operation to mock (required for dispatch and validations)", name = "Set MockOperation", helpUrl = HelpUrls.SETMOCKOPERATION_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	private interface CreateForm
	{
		@AField( description = "Specifies the operation to be mocked", name = "Operation", type = AFieldType.ENUMERATION )
		public final static String OPERATION = "Operation";

		@AField( description = "Specifies the interface containing the operation to be mocked", name = "Interface", type = AFieldType.ENUMERATION )
		public final static String INTERFACE = "Interface";
	}
}
