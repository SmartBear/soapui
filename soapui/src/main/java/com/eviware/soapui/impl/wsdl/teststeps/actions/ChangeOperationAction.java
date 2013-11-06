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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

/**
 * Prompts to change the WsdlOperation of a WsdlTestRequestStep
 * 
 * @author Ole.Matzura
 */

public class ChangeOperationAction extends AbstractSoapUIAction<WsdlTestRequestStep>
{
	private XFormDialog dialog;
	private WsdlTestRequestStep testStep;

	public ChangeOperationAction()
	{
		super( "Change Operation", "Changes the Interface Operation for this Test Request" );
	}

	public void perform( WsdlTestRequestStep target, Object param )
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
					dialog.setOptions( Form.OPERATION,
							ModelSupport.getNames( project.getInterfaceByName( newValue ).getOperationList() ) );
					dialog.setValue( Form.OPERATION, testStep.getOperationName() );
				}
			} );

			dialog.getFormField( Form.RECREATE_REQUEST ).addFormFieldListener( new XFormFieldListener()
			{

				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					boolean enabled = Boolean.parseBoolean( newValue );

					dialog.getFormField( Form.CREATE_OPTIONAL ).setEnabled( enabled );
					dialog.getFormField( Form.KEEP_EXISTING ).setEnabled( enabled );
				}
			} );

			dialog.getFormField( Form.CREATE_OPTIONAL ).setEnabled( false );
			dialog.getFormField( Form.KEEP_EXISTING ).setEnabled( false );
		}

		WsdlProject project = target.getTestCase().getTestSuite().getProject();
		dialog.setOptions( Form.INTERFACE, ModelSupport.getNames( project.getInterfaceList(),
				new ModelSupport.InterfaceTypeFilter( WsdlInterfaceFactory.WSDL_TYPE ) ) );
		dialog.setValue( Form.INTERFACE, target.getInterfaceName() );

		dialog.setOptions( Form.OPERATION,
				ModelSupport.getNames( project.getInterfaceByName( target.getInterfaceName() ).getOperationList() ) );
		dialog.setValue( Form.OPERATION, target.getOperationName() );
		dialog.setValue( Form.NAME, target.getName() );

		if( dialog.show() )
		{
			String ifaceName = dialog.getValue( Form.INTERFACE );
			String operationName = dialog.getValue( Form.OPERATION );

			WsdlInterface iface = ( WsdlInterface )project.getInterfaceByName( ifaceName );
			WsdlOperation operation = iface.getOperationByName( operationName );
			target.setOperation( operation );

			String name = dialog.getValue( Form.NAME ).trim();
			if( name.length() > 0 && !target.getName().equals( name ) )
				target.setName( name );

			if( dialog.getBooleanValue( Form.RECREATE_REQUEST ) )
			{
				String req = operation.createRequest( dialog.getBooleanValue( Form.CREATE_OPTIONAL ) );
				if( req == null )
				{
					UISupport.showErrorMessage( "Request creation failed" );
					return;
				}

				WsdlTestRequest request = target.getTestRequest();
				if( dialog.getBooleanValue( Form.KEEP_EXISTING ) )
				{
					req = XmlUtils.transferValues( request.getRequestContent(), req );
				}

				request.setRequestContent( req );
			}
		}
	}

	@AForm( description = "Specify Interface/Operation for TestRequest", name = "Change Operation", helpUrl = HelpUrls.CHANGEOPERATION_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	protected interface Form
	{
		@AField( name = "Name", description = "The Name of the TestRequests", type = AFieldType.STRING )
		public final static String NAME = "Name";

		@AField( name = "Interface", description = "The TestRequests' Interface", type = AFieldType.ENUMERATION )
		public final static String INTERFACE = "Interface";

		@AField( name = "Operation", description = "The TestRequests' Operation", type = AFieldType.ENUMERATION )
		public final static String OPERATION = "Operation";

		@AField( name = "Recreate Request", description = "Recreates the request content from the new Operations Definition", type = AFieldType.BOOLEAN )
		public final static String RECREATE_REQUEST = "Recreate Request";

		@AField( name = "Create Optional", description = "Creates optional content when recreating the request", type = AFieldType.BOOLEAN )
		public final static String CREATE_OPTIONAL = "Create Optional";

		@AField( name = "Keep Existing", description = "Tries to keep existing values when recreating the request", type = AFieldType.BOOLEAN )
		public final static String KEEP_EXISTING = "Keep Existing";
	}
}
