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

package com.eviware.soapui.support.editor.inspectors.aut;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.types.StringList;
import com.jgoodies.binding.PresentationModel;

public class RequestAutInspector extends AbstractXmlInspector
{
	private JPanel mainPanel;
	private final AbstractHttpRequest<?> request;
	private SimpleBindingForm form;

	protected RequestAutInspector( AbstractHttpRequest<?> request )
	{
		super( "Aut", "Authentication and Security-related settings", true, AutInspectorFactory.INSPECTOR_ID );
		this.request = request;
	}

	public JComponent getComponent()
	{
		if( mainPanel == null )
		{
			mainPanel = new JPanel( new BorderLayout() );

			form = new SimpleBindingForm( new PresentationModel<AbstractHttpRequest<?>>( request ) );
			form.addSpace( 5 );
			form.appendTextField( "username", "Username", "The username to use for HTTP Authentication" );
			form.appendTextField( "password", "Password", "The password to use for HTTP Authentication" );
			form.appendTextField( "domain", "Domain", "The domain to use for HTTP Authentication" );

			if( request instanceof WsdlRequest )
			{
				StringList outgoingNames = new StringList( request.getOperation().getInterface().getProject()
						.getWssContainer().getOutgoingWssNames() );
				outgoingNames.add( "" );
				StringList incomingNames = new StringList( request.getOperation().getInterface().getProject()
						.getWssContainer().getIncomingWssNames() );
				incomingNames.add( "" );

				form.addSpace( 5 );
				form.appendComboBox( "outgoingWss", "Outgoing WSS", outgoingNames.toStringArray(),
						"The outgoing WS-Security configuration to use" );
				form.appendComboBox( "incomingWss", "Incoming WSS", incomingNames.toStringArray(),
						"The incoming WS-Security configuration to use" );
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
