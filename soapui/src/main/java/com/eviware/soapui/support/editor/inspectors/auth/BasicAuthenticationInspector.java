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
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.support.UISupport;
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
import java.util.ArrayList;

@ParametersAreNonnullByDefault
public class BasicAuthenticationInspector<T extends AbstractHttpRequest> extends AbstractXmlInspector
{
	public static final String AUTHORIZATION_TYPE_COMBO_BOX_NAME = "Authorization Type";

	public static final int TOP_SPACING = 10;
	public static final int OUTERMOST_SPACING = 5;
	public static final int NORMAL_SPACING = 10;
	public static final int GROUP_SPACING = 20;

	public static final String AUTH_TYPE_PROPERTY_NAME = "authType";
	public static final String BASIC_FORM_LABEL = "Legacy form";

	private static final ColumnSpec LABEL_COLUMN = new ColumnSpec( "left:72dlu" );
	private static final ColumnSpec RIGHTMOST_COLUMN = new ColumnSpec( "5px" );
	public static final Color CARD_BORDER_COLOR = new Color( 121, 121, 121 );
	public static final Color CARD_BACKGROUND_COLOR = new Color( 228, 228, 228 );

	private final JPanel outerPanel = new JPanel( new BorderLayout() );
	private final JPanel cardPanel = new JPanel( new CardLayout() );
	private SimpleBindingForm authTypeForm;
	private SimpleBindingForm basicAuthenticationForm;
	protected T request;

	protected BasicAuthenticationInspector( T request )
	{
		super( AuthInspectorFactory.INSPECTOR_ID, "Authentication and Security-related settings",
				true, AuthInspectorFactory.INSPECTOR_ID );
		this.request = request;

		buildUI();

		showFormForSelectedAuthType( request.getAuthType() );
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
		basicAuthenticationForm.getPresentationModel().release();
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
		return basicAuthenticationForm;
	}

	String getFormTypeForSelection( String selectedItem )
	{
		return BASIC_FORM_LABEL;
	}

	protected void buildUI()
	{

		authTypeForm = new SimpleBindingForm( new PresentationModel<AbstractHttpRequest<?>>( request ) );
		basicAuthenticationForm = new SimpleBindingForm( new PresentationModel<AbstractHttpRequest<?>>( request ) );

		JPanel innerPanel = new JPanel( new BorderLayout() );

		populateAuthTypeForm( authTypeForm );

		JPanel formWrapperPanel = new JPanel( new BorderLayout() );
		formWrapperPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 0, 10 ) );
		formWrapperPanel.add(  authTypeForm.getPanel(), BorderLayout.LINE_START) ;
		formWrapperPanel.add( UISupport.createFormButton( new ShowOnlineHelpAction( "http://www.soapui.org" ) ),
				BorderLayout.AFTER_LINE_ENDS );

		innerPanel.add( formWrapperPanel, BorderLayout.PAGE_START  );

		setBorderAndBackgroundColorOnPanel( basicAuthenticationForm.getPanel() );
		populateBasicForm( basicAuthenticationForm );

		cardPanel.add( basicAuthenticationForm.getPanel(), BASIC_FORM_LABEL );
		cardPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		innerPanel.add( cardPanel, BorderLayout.CENTER );
		outerPanel.add( new JScrollPane( innerPanel ), BorderLayout.CENTER );
	}

	protected void setBorderAndBackgroundColorOnPanel( JPanel card )
	{
		card.setBorder( BorderFactory.createLineBorder( CARD_BORDER_COLOR ) );
		card.setBackground( CARD_BACKGROUND_COLOR );
	}

	protected void setBorderOnPanel( JPanel card )
	{
		card.setBorder(BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder( 1,1,1,1,CARD_BORDER_COLOR ),
				BorderFactory.createMatteBorder(10, 10, 10, 10, CARD_BACKGROUND_COLOR)));
	}

	protected void setBackgroundColorOnPanel( JPanel card )
	{
		card.setBackground( CARD_BACKGROUND_COLOR );
	}

	private void populateAuthTypeForm( SimpleBindingForm authTypeForm )
	{
		initForm( authTypeForm );

		authTypeForm.addSpace( OUTERMOST_SPACING );

		ArrayList<String> authenticationTypes = getAuthenticationTypes();
		String[] options = authenticationTypes.toArray( new String[authenticationTypes.size()] );

		final JComboBox comboBox = authTypeForm.appendComboBox( AUTH_TYPE_PROPERTY_NAME, AUTHORIZATION_TYPE_COMBO_BOX_NAME, options, "" );
		comboBox.setName( AUTHORIZATION_TYPE_COMBO_BOX_NAME );
		comboBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				showFormForSelectedAuthType(  comboBox.getSelectedItem().toString() );
			}
		} );

	}

	protected void showFormForSelectedAuthType( String selectedType )
	{
		String formType = getFormTypeForSelection( selectedType);
		CardLayout layout = ( CardLayout )cardPanel.getLayout();
		layout.show( cardPanel, formType );
	}

	protected ArrayList<String> getAuthenticationTypes()
	{
		ArrayList<String> options = new ArrayList<String>();
		options.add( CredentialsConfig.AuthType.GLOBAL_HTTP_SETTINGS.toString() );
		options.add( CredentialsConfig.AuthType.PREEMPTIVE.toString() );
		options.add( CredentialsConfig.AuthType.NTLM.toString() );
		options.add( CredentialsConfig.AuthType.SPNEGO_KERBEROS.toString() );
		return options;
	}

	private void populateBasicForm( SimpleBindingForm basicConfigurationForm )
	{
		initForm( basicConfigurationForm );

		basicConfigurationForm.addSpace( TOP_SPACING );

		basicConfigurationForm.appendTextField( "username", "Username", "The username to use for HTTP Authentication" );
		basicConfigurationForm.appendPasswordField( "password", "Password", "The password to use for HTTP Authentication" );
		basicConfigurationForm.appendTextField( "domain", "Domain", "The domain to use for Authentication(NTLM/Kerberos)" );
	}

	void initForm( SimpleBindingForm form )
	{
		FormLayout formLayout = ( FormLayout )form.getPanel().getLayout();
		formLayout.setColumnSpec( 2, LABEL_COLUMN );
		formLayout.setColumnSpec( 5, RIGHTMOST_COLUMN );
	}
}