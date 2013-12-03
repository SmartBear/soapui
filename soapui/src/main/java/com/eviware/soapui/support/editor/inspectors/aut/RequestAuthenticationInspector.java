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

package com.eviware.soapui.support.editor.inspectors.aut;

import com.eviware.soapui.config.CredentialsConfig.AuthType;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.types.StringList;
import com.jgoodies.binding.PresentationModel;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

public class RequestAuthenticationInspector extends AbstractXmlInspector
{
	private JPanel mainPanel;
	private final AbstractHttpRequest<?> request;
	private SimpleBindingForm form;

	protected RequestAuthenticationInspector( AbstractHttpRequest<?> request )
	{
		super( "Auth", "Authentication and Security-related settings", true, AuthInspectorFactory.INSPECTOR_ID );
		this.request = request;
	}

	public JComponent getComponent()
	{
		if( mainPanel == null )
		{
			mainPanel = new JPanel( new BorderLayout() );

			form = new SimpleBindingForm( new PresentationModel<AbstractHttpRequest<?>>( request ) );
			form.addSpace( 5 );
			form.appendComboBox( "authType", "Authorisation Type", new String[] { AuthType.GLOBAL_HTTP_SETTINGS.toString(),
					AuthType.PREEMPTIVE.toString(), AuthType.NTLM_KERBEROS.toString() }, "" );
			form.appendTextField( "username", "Username", "The username to use for HTTP Authentication" );
			form.appendPasswordField( "password", "Password", "The password to use for HTTP Authentication" );
			form.appendTextField( "domain", "Domain", "The domain to use for Authentication(NTLM/Kerberos)" );

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
	public void release()
	{
		super.release();

		if( form != null )
			form.getPresentationModel().release();
	}

	@Override
	public boolean isEnabledFor( EditorView<XmlDocument> view )
	{
		return !view.getViewId().equals( RawXmlEditorFactory.VIEW_ID );
	}
}
