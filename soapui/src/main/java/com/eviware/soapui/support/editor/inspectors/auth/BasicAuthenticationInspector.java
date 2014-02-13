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

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@ParametersAreNonnullByDefault
public class BasicAuthenticationInspector extends AbstractXmlInspector
{
	public static final String COMBO_BOX_LABEL = "Authorization Type";

	public static final int TOP_SPACING = 10;
	public static final int OUTERMOST_SPACING = 5;
	public static final int NORMAL_SPACING = 10;
	public static final int GROUP_SPACING = 20;

	public static final String AUTH_TYPE_PROPERTY_NAME = "authType";
	public static final String LEGACY_FORM_LABEL = "Legacy form";

	private static final ColumnSpec LABEL_COLUMN = new ColumnSpec( "left:72dlu" );
	private static final ColumnSpec RIGHTMOST_COLUMN = new ColumnSpec( "5px" );
	public static final Color CARD_BORDER_COLOR = new Color( 121, 121, 121 );
	public static final Color CARD_BACKGROUND_COLOR = new Color( 228, 228, 228 );

	private final JPanel outerPanel = new JPanel( new BorderLayout() );
	private final JPanel cardPanel = new JPanel( new CardLayout() );
	private final SimpleBindingForm authTypeForm;
	private final SimpleBindingForm card;

	protected BasicAuthenticationInspector( AbstractHttpRequest<?> request )
	{
		super( AuthInspectorFactory.INSPECTOR_ID, "Authentication and Security-related settings",
				true, AuthInspectorFactory.INSPECTOR_ID );

		authTypeForm = new SimpleBindingForm( new PresentationModel<AbstractHttpRequest<?>>( request ) );
		card = new SimpleBindingForm( new PresentationModel<AbstractHttpRequest<?>>( request ) );

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
		card.getPresentationModel().release();
	}

	JPanel getCardPanel()
	{
		return cardPanel;
	}

	SimpleBindingForm getAuthTypeForm()
	{
		return authTypeForm;
	}

	SimpleBindingForm getBasicForm()
	{
		return card;
	}

	void selectCard()
	{
		CardLayout layout = ( CardLayout )cardPanel.getLayout();
		layout.show( cardPanel, LEGACY_FORM_LABEL );
	}

	private void buildUI()
	{
		JPanel innerPanel = new JPanel( new BorderLayout() );

		populateAuthTypeForm( authTypeForm );

		innerPanel.add( authTypeForm.getPanel(), BorderLayout.PAGE_START );

		setBorderAndBackgroundColorOnCard( card.getPanel() );
		populateBasicForm( card );

		cardPanel.add( card.getPanel(), LEGACY_FORM_LABEL );
		cardPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		innerPanel.add( cardPanel, BorderLayout.CENTER );
		outerPanel.add( new JScrollPane( innerPanel ), BorderLayout.CENTER );
	}

	protected void setBorderAndBackgroundColorOnCard( JPanel card )
	{
		card.setBorder( BorderFactory.createLineBorder( CARD_BORDER_COLOR ) );
		card.setBackground( CARD_BACKGROUND_COLOR );
	}

	private void populateAuthTypeForm( SimpleBindingForm authTypeForm )
	{
		initForm( authTypeForm );

		authTypeForm.addSpace( OUTERMOST_SPACING );

		String[] options = {
				CredentialsConfig.AuthType.GLOBAL_HTTP_SETTINGS.toString(),
				CredentialsConfig.AuthType.PREEMPTIVE.toString(),
				CredentialsConfig.AuthType.NTLM.toString(),
				CredentialsConfig.AuthType.SPNEGO_KERBEROS.toString(),
		};

		JComboBox comboBox = authTypeForm.appendComboBox( AUTH_TYPE_PROPERTY_NAME, COMBO_BOX_LABEL, options, "" );
		comboBox.setName( COMBO_BOX_LABEL );
		comboBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				selectCard();
			}
		} );
	}

	private void populateBasicForm( SimpleBindingForm legacyForm )
	{
		initForm( legacyForm );

		legacyForm.addSpace( TOP_SPACING );

		legacyForm.appendTextField( "username", "Username", "The username to use for HTTP Authentication" );
		legacyForm.appendPasswordField( "password", "Password", "The password to use for HTTP Authentication" );
		legacyForm.appendTextField( "domain", "Domain", "The domain to use for Authentication(NTLM/Kerberos)" );
	}

	void initForm( SimpleBindingForm form )
	{
		FormLayout formLayout = ( FormLayout )form.getPanel().getLayout();
		formLayout.setColumnSpec( 2, LABEL_COLUMN );
		formLayout.setColumnSpec( 5, RIGHTMOST_COLUMN );
	}
}