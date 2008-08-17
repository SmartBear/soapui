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

package com.eviware.soapui.impl.rest.actions.request;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.support.AbstractAddToTestCaseAction;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.ui.desktop.SoapUIDesktop;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;

/**
 * Adds a WsdlRequest to a WsdlTestCase as a WsdlTestRequestStep
 * 
 * @author Ole.Matzura
 */

public class AddRestRequestToTestCaseAction extends AbstractAddToTestCaseAction<RestRequest>
{
	public static final String SOAPUI_ACTION_ID = "AddRestRequestToTestCaseAction";
	
   private static final String STEP_NAME = "Name";
	private static final String CLOSE_REQUEST = "Close Request Window";
	private static final String SHOW_TESTCASE = "Shows TestCase Editor";
	private static final String COPY_ATTACHMENTS = "Copy Attachments";
	private static final String COPY_HTTPHEADERS = "Copy HTTP Headers";
	
	private XFormDialog dialog;
	private StringToStringMap dialogValues = new StringToStringMap();
	private XFormField closeRequestCheckBox;

	public AddRestRequestToTestCaseAction()
   {
      super( "Add to TestCase", "Adds this REST Request to a TestCase" );
   }

   public void perform( RestRequest request, Object param )
	{
      WsdlProject project = request.getOperation().getInterface().getProject();
      
      WsdlTestCase testCase = getTargetTestCase( project );
      if( testCase != null )
      	addRequest( testCase, request, -1 );
	}   

	public RestTestRequestStep addRequest(WsdlTestCase testCase, RestRequest request, int position)
	{
		if( dialog == null )
			buildDialog();
		
		dialogValues.put( STEP_NAME, request.getOperation().getName() + " - " + request.getName() );
		dialogValues.put( CLOSE_REQUEST, "true" );
		dialogValues.put( SHOW_TESTCASE, "true" );
		dialog.getFormField( COPY_ATTACHMENTS ).setEnabled( request.getAttachmentCount() > 0 );
		dialog.setBooleanValue( COPY_ATTACHMENTS, true );

		dialog.getFormField( COPY_HTTPHEADERS ).setEnabled( request.getRequestHeaders().size() > 0 );
		dialog.setBooleanValue( COPY_HTTPHEADERS, false );
		
		SoapUIDesktop desktop = SoapUI.getDesktop();
		closeRequestCheckBox.setEnabled( desktop != null && desktop.hasDesktopPanel( request ));
		
		dialogValues = dialog.show( dialogValues );
		if( dialog.getReturnValue() != XFormDialog.OK_OPTION )
			return null;;

		String name = dialogValues.get( STEP_NAME );
		
		RestTestRequestStep testStep = (RestTestRequestStep) testCase.insertTestStep( 
				RestRequestStepFactory.createConfig( request, name ), position );
		
		if( testStep == null )
			return null;

		UISupport.selectAndShow( testStep );
		
		if( dialogValues.getBoolean( CLOSE_REQUEST ) && desktop != null )
		{
			desktop.closeDesktopPanel( request );
		}
		
		if( dialogValues.getBoolean( SHOW_TESTCASE ) )
		{
			UISupport.selectAndShow( testCase );
		}
			
		return testStep;
	}
	
	private void buildDialog()
	{
		XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Add Request to TestCase");
		XForm mainForm = builder.createForm( "Basic" );
		
		mainForm.addTextField( STEP_NAME, "Name of TestStep", XForm.FieldType.URL ).setWidth( 30 );

		closeRequestCheckBox = mainForm.addCheckBox( CLOSE_REQUEST, "(closes the current window for this request)" );
		mainForm.addCheckBox( SHOW_TESTCASE, "(opens the TestCase editor for the target TestCase)" );
		mainForm.addCheckBox( COPY_ATTACHMENTS, "(copies the requests attachments to the TestRequest)" );
		mainForm.addCheckBox( COPY_HTTPHEADERS, "(copies the requests HTTP-Headers to the TestRequest)" );
		
		dialog = builder.buildDialog( builder.buildOkCancelActions(), 
      		"Specify options for adding the request to a TestCase", UISupport.OPTIONS_ICON );		
	}
}
