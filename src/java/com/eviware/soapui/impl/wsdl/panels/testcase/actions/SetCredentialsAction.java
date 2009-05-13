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

package com.eviware.soapui.impl.wsdl.panels.testcase.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleForm;
import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 * Set the credentials for all requests in a testcase
 * 
 * @author Ole.Matzura
 */

public class SetCredentialsAction extends AbstractAction
{
	private final WsdlTestCase testCase;
	private JDialog dialog;
	private SimpleForm form;

	private static final String DOMAIN = "Domain";
	private static final String PASSWORD = "Password";
	private static final String USERNAME = "Username";

	public SetCredentialsAction( WsdlTestCase testCase )
	{
		this.testCase = testCase;
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/set_credentials.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, "Sets the credentials for all requests in this testcase" );
	}

	public void actionPerformed( ActionEvent e )
	{
		if( dialog == null )
		{
			buildDialog();
		}

		UISupport.showDialog( dialog );
	}

	private void buildDialog()
	{
		dialog = new JDialog( UISupport.getMainFrame(), "Set TestCase Credentials" );
		form = new SimpleForm();
		form.appendTextField( USERNAME, "Username to use for authentication" );
		form.appendTextField( PASSWORD, "Password to use for authentication" );
		form.appendTextField( DOMAIN, "Domain to specify (for NTLM)" );
		form.getPanel().setBorder( BorderFactory.createEmptyBorder( 0, 0, 10, 0 ) );

		JPanel panel = new JPanel( new BorderLayout() );
		panel.add( form.getPanel(), BorderLayout.CENTER );

		JPanel buttonBar = ButtonBarFactory.buildOKCancelBar( new JButton( new OkAction() ), new JButton(
				new CancelAction() ) );
		panel.add( buttonBar, BorderLayout.SOUTH );
		panel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		panel.setPreferredSize( new Dimension( 270, ( int )panel.getPreferredSize().getHeight() ) );

		dialog.getContentPane().add( panel );
		dialog.pack();
	}

	private class OkAction extends AbstractAction
	{
		public OkAction()
		{
			super( "Ok" );
		}

		public void actionPerformed( ActionEvent e )
		{
			for( int c = 0; c < testCase.getTestStepCount(); c++ )
			{
				TestStep step = testCase.getTestStepAt( c );
				if( step instanceof WsdlTestRequestStep )
				{
					WsdlTestRequestStep requestStep = ( WsdlTestRequestStep )step;
					requestStep.getTestRequest().setUsername( form.getComponentValue( USERNAME ) );
					requestStep.getTestRequest().setPassword( form.getComponentValue( PASSWORD ) );
					requestStep.getTestRequest().setDomain( form.getComponentValue( DOMAIN ) );
				}
			}

			dialog.setVisible( false );
		}
	}

	private class CancelAction extends AbstractAction
	{
		public CancelAction()
		{
			super( "Cancel" );
		}

		public void actionPerformed( ActionEvent e )
		{
			dialog.setVisible( false );
		}
	}
}