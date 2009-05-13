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

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlAsyncResponseTestStep;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

public class SetAsyncResponseMockOperationAction extends AbstractSoapUIAction<WsdlAsyncResponseTestStep>
{
	private XFormDialog dialog;
	private WsdlAsyncResponseTestStep testStep;

	public SetAsyncResponseMockOperationAction()
	{
		super( "Set MockOperation", "Sets the operation to mock" );
	}

	public void perform( WsdlAsyncResponseTestStep target, Object param )
	{
		this.testStep = target;

		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( Form.class );
			dialog.getFormField( Form.INTERFACE ).addFormFieldListener( new XFormFieldListener()
			{
				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					WsdlProject project = testStep.getTestCase().getTestSuite().getProject();
					dialog.setOptions( Form.OPERATION, ModelSupport.getNames( project.getInterfaceByName( newValue )
							.getOperationList() ) );
				}
			} );
		}

		WsdlProject project = target.getTestCase().getTestSuite().getProject();
		dialog.setOptions( Form.INTERFACE, ModelSupport.getNames( project.getInterfaceList(),
				new ModelSupport.InterfaceTypeFilter( WsdlInterfaceFactory.WSDL_TYPE ) ) );
		dialog.setValue( Form.INTERFACE, target.getInterface().getName() );

		dialog.setOptions( Form.OPERATION, ModelSupport.getNames( project.getInterfaceByName(
				target.getInterface().getName() ).getOperationList() ) );
		dialog.setValue( Form.OPERATION, target.getOperation().getName() );

		if( dialog.show() )
		{
			String ifaceName = dialog.getValue( Form.INTERFACE );
			String operationName = dialog.getValue( Form.OPERATION );

			WsdlInterface iface = ( WsdlInterface )project.getInterfaceByName( ifaceName );
			WsdlOperation operation = iface.getOperationByName( operationName );
			target.setOperation( operation.getName() );
		}
	}

	@AForm( description = "Specify Interface/Operation for AsyncResponse", name = "Set Mock Operation" )
	protected interface Form
	{
		@AField( name = "Interface", description = "The interface to mock", type = AFieldType.ENUMERATION )
		public final static String INTERFACE = "Interface";

		@AField( name = "Operation", description = "The operation to mock", type = AFieldType.ENUMERATION )
		public final static String OPERATION = "Operation";
	}
}
