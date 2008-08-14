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

package com.eviware.soapui.support.editor.inspectors.wsa;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.eviware.soapui.config.MustUnderstandTypeConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.jgoodies.binding.PresentationModel;

public class RequestWsaInspector extends AbstractXmlInspector
{
	private JPanel mainPanel;
	private final WsdlRequest request;
	private SimpleBindingForm form;

	protected RequestWsaInspector( WsdlRequest request )
	{
		super( "Wsa", "Authentication and Security-related settings", true, WsaInspectorFactory.INSPECTOR_ID );
		this.request = request;
	}

	public JComponent getComponent()
	{
		if( mainPanel == null )
		{
			mainPanel = new JPanel( new BorderLayout() );

			form = new SimpleBindingForm( new PresentationModel<AbstractHttpRequest<?>>( request.getWsaConfig() ) );

			if( request instanceof WsdlRequest )
			{
				form.addSpace( 5 );
				//add mustUnderstand drop down list
				form.appendComboBox( "mustUnderstand", "Must understand", new String[] {MustUnderstandTypeConfig.NONE.toString(), 
						MustUnderstandTypeConfig.TRUE.toString(), MustUnderstandTypeConfig.FALSE.toString()},
					"The  property for controlling use of the mustUnderstand attribute" );
				
				form.appendComboBox( "version", "WS-A Version", new String[] {WsaVersionTypeConfig.X_200508.toString(), WsaVersionTypeConfig.X_200408.toString()},
					"The  property for managing WS-A version" );
				
//				form.appendTextField( "from", "From", "The source endpoint reference" );
//				form.appendTextField( "replyTo", "Reply to", "The reply endpoint reference" );
//				form.appendTextField( "faultTo", "Fault to", "The fault endpoint reference" );
				form.appendTextField( "action", "Action", "The action related to a message" );
				form.appendTextField( "messageID", "MessageID", " The ID of a message that can be used to uniquely identify a message" );
				form.appendTextField( "to", "To", "The destination endpoint reference" );
			}
			
			form.addSpace( 5 );
			
			mainPanel.add( new JScrollPane( form.getPanel() ), BorderLayout.CENTER );
		}

		return mainPanel;
	}
	
	@Override
	public boolean isEnabledFor( EditorView<XmlDocument> view )
	{
		return !view.getViewId().equals( RawXmlEditorFactory.VIEW_ID );
	}
}
