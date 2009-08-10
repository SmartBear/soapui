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

package com.eviware.soapui.impl.wsdl.panels.testcase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.MessageExchangeTestStepResult;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

public class JTestCaseTestRunLog extends JTestRunLog
{
	private final WsdlTestCase testCase;
	private JButton addToMockServiceButton;
	private XFormDialog addDialog;

	public JTestCaseTestRunLog( WsdlTestCase testCase )
	{
		super( testCase.getSettings() );
		this.testCase = testCase;
	}

	@Override
	protected void addToolbarButtons( JXToolBar toolbar )
	{
		super.addToolbarButtons( toolbar );

		toolbar.addFixed( addToMockServiceButton = UISupport.createToolbarButton( UISupport
				.createImageIcon( "/mockService.gif" ) ) );
		addToMockServiceButton.addActionListener( new AddToMockServiceAction() );
	}

	public void release()
	{
		if( addDialog != null )
		{
			addDialog.release();
			addDialog = null;
		}

		super.release();
	}

	@AForm( description = "Set options for adding selected requests to a MockService", name = "Add To MockService" )
	private class AddToMockServiceAction implements ActionListener
	{
		private static final String CREATE_NEW_OPTION = "<Create New>";

		@AField( name = "Target MockService", description = "The target TestSuite", type = AFieldType.ENUMERATION )
		public final static String MOCKSERVICE = "Target MockService";

		@AField( name = "Open Editor", description = "Open the created MockService", type = AFieldType.BOOLEAN )
		public final static String OPENEDITOR = "Open Editor";

		public void actionPerformed( ActionEvent e )
		{
			if( getLogListModel().getSize() == 0 )
				return;

			if( testCase.getDiscardOkResults() )
			{
				UISupport.showInfoMessage( "Ok Results have been discarded" );
				return;
			}

			if( addDialog == null )
			{
				addDialog = ADialogBuilder.buildDialog( this.getClass() );
			}

			String[] testSuiteNames = ModelSupport.getNames( new String[] { CREATE_NEW_OPTION }, testCase.getTestSuite()
					.getProject().getMockServiceList() );
			addDialog.setOptions( MOCKSERVICE, testSuiteNames );

			if( addDialog.show() )
			{
				String targetMockServiceName = addDialog.getValue( MOCKSERVICE );

				WsdlMockService mockService = testCase.getTestSuite().getProject().getMockServiceByName(
						targetMockServiceName );
				if( mockService == null )
				{
					targetMockServiceName = ModelSupport.promptForUniqueName( "MockService", testCase.getTestSuite()
							.getProject(), "" );
					if( targetMockServiceName == null )
						return;

					mockService = testCase.getTestSuite().getProject().addNewMockService( targetMockServiceName );
				}

				int cnt = 0;
				MessageExchangeTestStepResult result = null;
				for( int c = 0; c < getLogListModel().getSize(); c++ )
				{
					Object obj = getLogListModel().getResultAt( c );
					if( result != obj && obj instanceof MessageExchangeTestStepResult )
					{
						result = ( MessageExchangeTestStepResult )obj;

						for( MessageExchange me : result.getMessageExchanges() )
						{
							if( me.isDiscarded() )
								continue;

							WsdlMockOperation mockOperation = mockService.getMockOperation( me.getOperation() );
							if( mockOperation == null )
								mockOperation = mockService.addNewMockOperation( ( WsdlOperation )me.getOperation() );

							WsdlMockResponse mockResponse = mockOperation.addNewMockResponse( "Recorded Test Response "
									+ ( ++cnt ), false );
							mockResponse.setResponseContent( me.getResponseContent() );

							Attachment[] requestAttachments = me.getResponseAttachments();
							if( requestAttachments != null )
							{
								for( Attachment attachment : requestAttachments )
								{
									mockResponse.addAttachment( attachment );
								}
							}
						}
					}
				}

				if( cnt == 0 )
				{
					UISupport.showInfoMessage( "No response messages found" );
				}
				else
				{
					UISupport.showInfoMessage( "Added " + cnt + " MockResponses to MockService" );

					if( addDialog.getBooleanValue( OPENEDITOR ) )
						UISupport.selectAndShow( mockService );
				}

			}
		}
	}
}
