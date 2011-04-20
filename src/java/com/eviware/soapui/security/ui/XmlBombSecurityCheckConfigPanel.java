/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.eviware.soapui.security.check.XmlBombSecurityCheck;
import com.eviware.soapui.support.components.SimpleForm;

public class XmlBombSecurityCheckConfigPanel extends SecurityCheckConfigPanel
{
	private static final String ATTACHMENT_PREFIX_FIELD = "Attachment Prefix Field";
	private static final String ENABLE_ATTACHMENT_FIELD = "Send bomb as attachment";

	private XmlBombSecurityCheck xmlCheck;

	public XmlBombSecurityCheckConfigPanel( XmlBombSecurityCheck xmlCheck )
	{
		super( new BorderLayout() );

		this.xmlCheck = xmlCheck;
		form = new SimpleForm();
		form.addSpace( 5 );

		JTextField attachmentPrefixField = form.appendTextField( ATTACHMENT_PREFIX_FIELD, "Attachment Prefix Field" );
		attachmentPrefixField.setMaximumSize( new Dimension( 80, 10 ) );
		attachmentPrefixField.setColumns( 20 );
		attachmentPrefixField.setText( xmlCheck.getAttachmentPrefix() );
		attachmentPrefixField.setEnabled( xmlCheck.isAttachXmlBomb() );

		JCheckBox attachXml = form.appendCheckBox( ENABLE_ATTACHMENT_FIELD, null, xmlCheck.isAttachXmlBomb() );
		attachXml.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent arg0 )
			{
				form.getComponent( ATTACHMENT_PREFIX_FIELD ).setEnabled(
						( ( JCheckBox )form.getComponent( ENABLE_ATTACHMENT_FIELD ) ).isSelected() );
			}
		} );

		add( form.getPanel() );
	}

	@Override
	public void save()
	{
		String prefix = form.getComponentValue( ATTACHMENT_PREFIX_FIELD );

		xmlCheck.setAttachmentPrefix( prefix );

		xmlCheck.setAttachXmlBomb( ( ( JCheckBox )form.getComponent( ENABLE_ATTACHMENT_FIELD ) ).isSelected() );
	}

}
