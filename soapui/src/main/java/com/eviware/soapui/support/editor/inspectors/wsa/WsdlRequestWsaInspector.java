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

package com.eviware.soapui.support.editor.inspectors.wsa;

import com.eviware.soapui.config.MustUnderstandTypeConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.editor.xml.XmlInspector;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class WsdlRequestWsaInspector extends AbstractWsaInspector implements XmlInspector
{
	private JCheckBox generateMessageIdCheckBox;
	private JCheckBox addDefaultToCheckBox;
	private JCheckBox addDefaultActionCheckBox;
	private JTextField messageIdTextField;
	private JTextField toTextField;
	private JTextField actionTextField;

	public WsdlRequestWsaInspector( WsdlRequest request )
	{
		super( request );
	}

	public void buildContent( SimpleBindingForm form )
	{
		form.addSpace( 5 );
		form.appendCheckBox( "wsaEnabled", "Enable WS-A addressing", "" );
		form.addSpace( 5 );
		// add mustUnderstand drop down list
		form.appendComboBox( "mustUnderstand", "Must understand", new String[] {
				MustUnderstandTypeConfig.NONE.toString(), MustUnderstandTypeConfig.TRUE.toString(),
				MustUnderstandTypeConfig.FALSE.toString() },
				"The  property for controlling use of the mustUnderstand attribute" );

		form.appendComboBox( "version", "WS-A Version", new String[] { WsaVersionTypeConfig.X_200508.toString(),
				WsaVersionTypeConfig.X_200408.toString() }, "The  property for managing WS-A version" );

		addDefaultActionCheckBox = form.appendCheckBox( "addDefaultAction", "Add default wsa:Action",
				"Add default wsa:Action" );
		actionTextField = form
				.appendTextField( "action", "Action",
						"The action related to a message, will be generated if left empty and ws-a settings 'use default action...' checked " );
		actionTextField.setEnabled( !addDefaultActionCheckBox.isSelected() );
		addDefaultActionCheckBox.addItemListener( new ItemListener()
		{

			public void itemStateChanged( ItemEvent arg0 )
			{
				actionTextField.setEnabled( !addDefaultActionCheckBox.isSelected() );
			}
		} );

		addDefaultToCheckBox = form.appendCheckBox( "addDefaultTo", "Add default wsa:To", "Add default wsa:To" );
		toTextField = form.appendTextField( "to", "To",
				"The destination endpoint reference, will be generated if left empty" );
		toTextField.setEnabled( !addDefaultToCheckBox.isSelected() );
		addDefaultToCheckBox.addItemListener( new ItemListener()
		{

			public void itemStateChanged( ItemEvent arg0 )
			{
				toTextField.setEnabled( !addDefaultToCheckBox.isSelected() );
			}
		} );

		form.appendTextField( "replyTo", "Reply to", "The reply endpoint reference, will be generated if left empty" );
		form.appendTextArea( "replyToRefParams", "ReplyTo Reference Parameters",
				"ReplyTo Reference Parameters, content will be inserted as an xml (not text)" );
		generateMessageIdCheckBox = form.appendCheckBox( "generateMessageId", "Generate MessageID",
				"Randomly generate MessageId" );
		messageIdTextField = form
				.appendTextField(
						"messageID",
						"MessageID",
						" The ID of a message that can be used to uniquely identify a message, will be generated if left empty and ws-a settings 'generate message id' checked " );
		messageIdTextField.setEnabled( !generateMessageIdCheckBox.isSelected() );
		generateMessageIdCheckBox.addItemListener( new ItemListener()
		{

			public void itemStateChanged( ItemEvent arg0 )
			{
				messageIdTextField.setEnabled( !generateMessageIdCheckBox.isSelected() );
			}
		} );
		form.addSpace( 10 );
		form.appendTextField( "from", "From", "The source endpoint reference" );
		form.appendTextField( "faultTo", "Fault to", "The fault endpoint reference" );
		form.appendTextArea( "faultToRefParams", "FaultTo Reference Parameters",
				"FaultTo Reference Parameters, content will be inserted as an xml (not text)" );
		form.appendTextField( "relatesTo", "Relates to", "The endpoint reference request relates to" );
		form.appendTextField( "relationshipType", "Relationship type", "Relationship type" );
		form.addSpace( 5 );
	}
}
