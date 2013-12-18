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

package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.config.CredentialsConfig.AuthType;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.actions.oauth.GetOAuthAccessTokenAction;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.types.StringList;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@ParametersAreNonnullByDefault
public class RequestAuthenticationInspector extends AbstractXmlInspector
{
	private static final int TOP_SPACING = 20;
	private static final int OUTERMOST_SPACING = 5;
	private static final int NORMAL_SPACING = 10;
	private static final int GROUP_SPACING = 20;

	private static final String COMBO_BOX_LABEL = "Authorization Type";
	private static final String LEGACY_FORM_LABEL = "Legacy form";
	private static final String OAUTH_2_FORM_LABEL = "OAuth 2 form";

	private static final ColumnSpec LABEL_COLUMN = new ColumnSpec( "left:72dlu" );
	private static final ColumnSpec RIGHTMOST_COLUMN = new ColumnSpec( "5px" );

	private final JPanel outerPanel = new JPanel( new BorderLayout() );
	private final JPanel cardPanel = new JPanel( new CardLayout() );

	private final SimpleBindingForm authTypeForm;
	private final SimpleBindingForm legacyForm;
	private final SimpleBindingForm oAuth2Form;

	private final AbstractHttpRequest<?> request;
	private final OAuth2Profile profile;

	protected RequestAuthenticationInspector( AbstractHttpRequest<?> request )
	{
		super( "Auth", "Authentication and Security-related settings", true, AuthInspectorFactory.INSPECTOR_ID );

		this.request = request;

		// Currently there's only support for one profile per project
		OAuth2ProfileContainer profileContainer = request.getOperation().getInterface().getProject().getOAuth2ProfileContainer();
		profile = profileContainer.getOAuth2ProfileList().get( 0 );

		authTypeForm = new SimpleBindingForm( new PresentationModel<AbstractHttpRequest<?>>( request ) );
		legacyForm = new SimpleBindingForm( new PresentationModel<AbstractHttpRequest<?>>( request ) );
		oAuth2Form = new SimpleBindingForm( new PresentationModel<AbstractHttpRequest<?>>( profile ) );

		buildUI();
	}

	@Override
	public boolean isEnabledFor( EditorView<XmlDocument> view )
	{
		return !view.getViewId().equals( RawXmlEditorFactory.VIEW_ID );
	}

	@Override
	public JComponent getComponent()
	{
		return outerPanel;
	}

	@Override
	public void release()
	{
		super.release();

		authTypeForm.getPresentationModel().release();
		legacyForm.getPresentationModel().release();
		oAuth2Form.getPresentationModel().release();
	}

	private void buildUI()
	{
		JPanel innerPanel = new JPanel( new BorderLayout() );

		populateAuthTypeForm( authTypeForm );

		innerPanel.add( authTypeForm.getPanel(), BorderLayout.PAGE_START );

		populateLegacyForm( legacyForm );
		cardPanel.add( legacyForm.getPanel(), LEGACY_FORM_LABEL );

		populateOAuth2Form( oAuth2Form );
		cardPanel.add( oAuth2Form.getPanel(), OAUTH_2_FORM_LABEL );

		selectCard();

		innerPanel.add( cardPanel, BorderLayout.CENTER );
		outerPanel.add( new JScrollPane( innerPanel ), BorderLayout.CENTER );
	}


	private void populateAuthTypeForm( SimpleBindingForm authTypeForm )
	{
		initForm( authTypeForm );

		authTypeForm.addSpace( OUTERMOST_SPACING );

		JComboBox comboBox = authTypeForm.appendComboBox( "authType", COMBO_BOX_LABEL, new String[] {
				AuthType.GLOBAL_HTTP_SETTINGS.toString(), AuthType.PREEMPTIVE.toString(),
				AuthType.NTLM_KERBEROS.toString(), AuthType.O_AUTH_2.toString() }, "" );
		comboBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				selectCard();
			}
		} );
	}

	private void populateLegacyForm( SimpleBindingForm legacyForm )
	{
		initForm( legacyForm );

		legacyForm.addSpace( TOP_SPACING );

		legacyForm.appendTextField( "username", "Username", "The username to use for HTTP Authentication" );
		legacyForm.appendPasswordField( "password", "Password", "The password to use for HTTP Authentication" );
		legacyForm.appendTextField( "domain", "Domain", "The domain to use for Authentication(NTLM/Kerberos)" );

		if( request instanceof WsdlRequest )
		{
			StringList outgoingNames = new StringList( request.getOperation().getInterface().getProject()
					.getWssContainer().getOutgoingWssNames() );
			outgoingNames.add( "" );
			StringList incomingNames = new StringList( request.getOperation().getInterface().getProject()
					.getWssContainer().getIncomingWssNames() );
			incomingNames.add( "" );

			legacyForm.addSpace( GROUP_SPACING );

			legacyForm.appendComboBox( "outgoingWss", "Outgoing WSS", outgoingNames.toStringArray(),
					"The outgoing WS-Security configuration to use" );
			legacyForm.appendComboBox( "incomingWss", "Incoming WSS", incomingNames.toStringArray(),
					"The incoming WS-Security configuration to use" );
		}
	}

	private void populateOAuth2Form( SimpleBindingForm oauth2Form )
	{
		initForm( oauth2Form );

		oauth2Form.addSpace( TOP_SPACING );

		oauth2Form.appendTextField( "clientID", "Client Identification", "" );
		oauth2Form.appendTextField( "clientSecret", "Client Secret", "" );

		oauth2Form.addSpace( GROUP_SPACING );

		oauth2Form.appendTextField( "authorizationURI", "Authorization URI", "" );
		oauth2Form.appendTextField( "accessTokenURI", "Access Token URI", "" );
		oauth2Form.appendTextField( "redirectURI", "Redirect URI", "" );

		oauth2Form.addSpace( GROUP_SPACING );

		oauth2Form.appendTextField( "scope", "Scope", "" );

		oauth2Form.addSpace( NORMAL_SPACING );

		// TODO This should be a bit wider, but leaving it at default size for now
		oauth2Form.addButtonWithoutLabel( "Get access token", new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				getAccessToken( e );
			}
		} );

		oauth2Form.addSpace( GROUP_SPACING );

		oauth2Form.appendTextField( "accessToken", "Access Token", "", SimpleForm.LONG_TEXT_FIELD_COLUMNS );
	}

	private void initForm( SimpleBindingForm form )
	{
		FormLayout formLayout = ( FormLayout )form.getPanel().getLayout();
		formLayout.setColumnSpec( 2, LABEL_COLUMN );
		formLayout.setColumnSpec( 5, RIGHTMOST_COLUMN );
	}

	private void selectCard()
	{
		Component component = authTypeForm.getComponent( COMBO_BOX_LABEL );
		JComboBox comboBox = ( JComboBox )component;
		CardLayout layout = ( CardLayout )cardPanel.getLayout();
		if( comboBox.getSelectedItem().equals( AuthType.O_AUTH_2.toString() ) )
		{
			layout.show( cardPanel, OAUTH_2_FORM_LABEL );
		}
		else
		{
			layout.show( cardPanel, LEGACY_FORM_LABEL );
		}
	}

	private void getAccessToken( ActionEvent e )
	{
		GetOAuthAccessTokenAction getAccessTokenAction = new GetOAuthAccessTokenAction( profile );
		getAccessTokenAction.actionPerformed( e );
	}
}