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

import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.text.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.http.ProxyUtils;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToStringMap;

public class ProxyPrefs implements Prefs
{

	public static final String HOST = "Host";
	public static final String PORT = "Port";
	public static final String USERNAME = "Username";
	public static final String PASSWORD = "Password";
	public static final String EXCLUDES = "Excludes";
	public static final String ENABLE_PROXY = "Enable Proxy";
	public static final String AUTO_PROXY = "Auto Proxy";

	private JTextField hostTextField;
	private JTextField portTextField;
	private JCheckBox enableProxyCheckbox;
	private JCheckBox autoProxyCheckbox;
	private SimpleForm proxyPrefForm;

	private final String title;

	public ProxyPrefs( String title )
	{
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

	public SimpleForm getForm()
	{
		if( proxyPrefForm == null )
		{
			proxyPrefForm = new SimpleForm();
			proxyPrefForm.addSpace( 5 );
			hostTextField = proxyPrefForm.appendTextField( HOST, "proxy host to use" );
			hostTextField.getDocument().addDocumentListener( new ProxyDocumentListenerAdapter() );
			portTextField = proxyPrefForm.appendTextField( PORT, "proxy port to use" );
			portTextField.getDocument().addDocumentListener( new ProxyDocumentListenerAdapter() );
			proxyPrefForm.appendTextField( USERNAME, "proxy username to use" );
			proxyPrefForm.appendPasswordField( PASSWORD, "proxy password to use" );
			proxyPrefForm.appendTextField( EXCLUDES, "Comma-seperated list of hosts to exclude" );
			enableProxyCheckbox = proxyPrefForm.appendCheckBox( ENABLE_PROXY, "enable using proxy", true );
			autoProxyCheckbox = proxyPrefForm.appendCheckBox( AUTO_PROXY, "enable auto proxy", true );
		}
		return proxyPrefForm;
	}

	private class ProxyDocumentListenerAdapter extends DocumentListenerAdapter
	{
		@Override
		public void update( Document document )
		{
			enableProxyCheckbox.setSelected( !StringUtils.isNullOrEmpty( hostTextField.getText() )
					&& !StringUtils.isNullOrEmpty( portTextField.getText() ) );
		}
	}

	public void getFormValues( Settings settings )
	{
		StringToStringMap values = new StringToStringMap();
		proxyPrefForm.getValues( values );
		storeValues( values, settings );
	}

	public StringToStringMap getValues( Settings settings )
	{
		StringToStringMap values = new StringToStringMap();
		values.put( HOST, settings.getString( ProxySettings.HOST, "" ) );
		values.put( PORT, settings.getString( ProxySettings.PORT, "" ) );
		values.put( USERNAME, settings.getString( ProxySettings.USERNAME, "" ) );
		values.put( PASSWORD, settings.getString( ProxySettings.PASSWORD, "" ) );
		values.put( EXCLUDES, settings.getString( ProxySettings.EXCLUDES, "" ) );
		values.put( ENABLE_PROXY, settings.getBoolean( ProxySettings.ENABLE_PROXY ) );
		values.put( AUTO_PROXY, settings.getBoolean( ProxySettings.AUTO_PROXY ) );
		return values;
	}

	public void setFormValues( Settings settings )
	{
		getForm().setValues( getValues( settings ) );
	}

	public void storeValues( StringToStringMap values, Settings settings )
	{
		settings.setString( ProxySettings.HOST, values.get( HOST ) );
		settings.setString( ProxySettings.PORT, values.get( PORT ) );
		settings.setString( ProxySettings.USERNAME, values.get( USERNAME ) );
		settings.setString( ProxySettings.PASSWORD, values.get( PASSWORD ) );
		settings.setString( ProxySettings.EXCLUDES, values.get( EXCLUDES ) );
		settings.setBoolean( ProxySettings.ENABLE_PROXY, values.getBoolean( ENABLE_PROXY ) );
		settings.setBoolean( ProxySettings.AUTO_PROXY, values.getBoolean( AUTO_PROXY ) );
		JToggleButton applyProxyButton = SoapUI.getApplyProxyButton();
		if( values.getBoolean( ENABLE_PROXY ) )
		{
			if( applyProxyButton != null )
				applyProxyButton.setIcon( UISupport.createImageIcon( SoapUI.PROXY_ENABLED_ICON ) );

			ProxyUtils.setProxyEnabled( true );
		}
		else
		{
			if( applyProxyButton != null )
				applyProxyButton.setIcon( UISupport.createImageIcon( SoapUI.PROXY_DISABLED_ICON ) );

			ProxyUtils.setProxyEnabled( false );
		}
		ProxyUtils.setAutoProxy( values.getBoolean( AUTO_PROXY ) );
	}

}
