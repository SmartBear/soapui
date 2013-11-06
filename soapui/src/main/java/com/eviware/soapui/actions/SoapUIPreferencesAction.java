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

package com.eviware.soapui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.SSLSettings;
import com.eviware.soapui.settings.SecuritySettings;
import com.eviware.soapui.settings.VersionUpdateSettings;
import com.eviware.soapui.settings.WSISettings;
import com.eviware.soapui.settings.WebRecordingSettings;
import com.eviware.soapui.settings.WsaSettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SwingConfigurationDialogImpl;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Action for managing SoapUI preferences
 * 
 * @author Ole.Matzura
 */

public class SoapUIPreferencesAction extends AbstractAction
{
	public static final String GLOBAL_SECURITY_SETTINGS = "Global Security Settings";
	public static final String WS_I_SETTINGS = "WS-I Settings";
	public static final String WSDL_SETTINGS = "WSDL Settings";
	public static final String UI_SETTINGS = "UI Settings";
	public static final String EDITOR_SETTINGS = "Editor Settings";
	public static final String PROXY_SETTINGS = "Proxy Settings";
	public static final String HTTP_SETTINGS = "HTTP Settings";
	public static final String SSL_SETTINGS = "SSL Settings";
	public static final String INTEGRATED_TOOLS = "Tools";
	public static final String WSA_SETTINGS = "WS-A Settings";
	public static final String LOADUI_SETTINGS = "loadUI Settings";
	public static final String WEBRECORDING_SETTINGS = "Web Recording Settings";
	public static final String GLOBAL_SENSITIVE_INFORMATION_TOKENS = "Global Sensitive Information Tokens";
	public static final String VERSIONUPDATE_SETTINGS = "Version Update Settings";
	private SwingConfigurationDialogImpl dialog;
	private JTabbedPane tabs;
	private List<Prefs> prefs = new ArrayList<Prefs>();

	private static SoapUIPreferencesAction instance;

	public SoapUIPreferencesAction()
	{
		super( "Preferences" );

		putValue( Action.SHORT_DESCRIPTION, "Sets global SoapUI preferences" );
		putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu alt P" ) );

		// addPrefs( new HttpPrefs( HTTP_SETTINGS));
		addPrefs( new AnnotatedSettingsPrefs( HttpSettings.class, HTTP_SETTINGS ) );
		addPrefs( new ProxyPrefs( PROXY_SETTINGS ) );
		addPrefs( new AnnotatedSettingsPrefs( SSLSettings.class, SSL_SETTINGS ) );
		addPrefs( new AnnotatedSettingsPrefs( WsdlSettings.class, WSDL_SETTINGS ) );
		addPrefs( new UIPrefs( UI_SETTINGS ) );
		addPrefs( new EditorPrefs( EDITOR_SETTINGS ) );
		addPrefs( new ToolsPrefs( INTEGRATED_TOOLS ) );
		addPrefs( new AnnotatedSettingsPrefs( WSISettings.class, WS_I_SETTINGS ) );
		addPrefs( new GlobalPropertiesPrefs() );
		addPrefs( new AnnotatedSettingsPrefs( SecuritySettings.class, GLOBAL_SECURITY_SETTINGS ) );
		addPrefs( new AnnotatedSettingsPrefs( WsaSettings.class, WSA_SETTINGS ) );
		addPrefs( new LoadUIPrefs( LOADUI_SETTINGS ) );
		addPrefs( new AnnotatedSettingsPrefs( WebRecordingSettings.class, WEBRECORDING_SETTINGS ) );
		addPrefs( new SecurityScansPrefs( GLOBAL_SENSITIVE_INFORMATION_TOKENS ) );
		addPrefs( new AnnotatedSettingsPrefs( VersionUpdateSettings.class, VERSIONUPDATE_SETTINGS ) );

		for( PrefsFactory factory : SoapUI.getFactoryRegistry().getFactories( PrefsFactory.class ) )
		{
			addPrefs( factory.createPrefs() );
		}

		instance = this;
	}

	public void addPrefs( Prefs pref )
	{
		prefs.add( pref );
	}

	public static SoapUIPreferencesAction getInstance()
	{
		if( instance == null )
			instance = new SoapUIPreferencesAction();

		return instance;
	}

	public void actionPerformed( ActionEvent e )
	{
		show( HTTP_SETTINGS );
	}

	public boolean show( String initialTab )
	{
		if( dialog == null )
			buildDialog();

		Settings settings = SoapUI.getSettings();
		for( Prefs pref : prefs )
			pref.setFormValues( settings );

		if( initialTab != null )
		{
			int ix = tabs.indexOfTab( initialTab );
			if( ix != -1 )
				tabs.setSelectedIndex( ix );
		}

		if( dialog.show( new StringToStringMap() ) )
		{
			for( Prefs pref : prefs )
				pref.getFormValues( settings );

			return true;
		}

		return false;
	}

	private void buildDialog()
	{
		dialog = new SwingConfigurationDialogImpl( "SoapUI Preferences", HelpUrls.PREFERENCES_HELP_URL,
				"Set global SoapUI settings", UISupport.OPTIONS_ICON );

		tabs = new JTabbedPane();
		tabs.setTabLayoutPolicy( JTabbedPane.SCROLL_TAB_LAYOUT );
		tabs.setTabPlacement( JTabbedPane.LEFT );
		for( Prefs pref : prefs )
		{
			tabs.addTab( pref.getTitle(), new JScrollPane( pref.getForm().getPanel() ) );
		}

		dialog.setContent( UISupport.createTabPanel( tabs, false ) );
	}

}
