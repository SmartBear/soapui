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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToStringMap;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProxyPrefs implements Prefs
{

	public static final String HOST = "Host";
	public static final String PORT = "Port";
	public static final String USERNAME = "Username";
	public static final String PASSWORD = "Password";
	public static final String EXCLUDES = "Excludes";

	private JTextField hostTextField;
	private JTextField portTextField;
	private JTextField userTextField;
	private JPasswordField passwordTextField;
	private JTextField excludesTextField;

	private SimpleForm proxyPrefForm;

	private final String title;

	private JRadioButton automatic;
	private JRadioButton none;
	private JRadioButton manual;

	private boolean autoProxy;
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
			addProxySettingRadioButtons();
			hostTextField = proxyPrefForm.appendTextField( HOST, "proxy host to use" );
			portTextField = proxyPrefForm.appendTextField( PORT, "proxy port to use" );
			excludesTextField = proxyPrefForm.appendTextField( EXCLUDES, "Comma-seperated list of hosts to exclude" );
			proxyPrefForm.appendSeparator();
			userTextField = proxyPrefForm.appendTextField( USERNAME, "proxy username to use" );
			passwordTextField = proxyPrefForm.appendPasswordField( PASSWORD, "proxy password to use" );

		}
		return proxyPrefForm;
	}

	private void addProxySettingRadioButtons()
	{
		ButtonGroup group = new ButtonGroup();
		JPanel radioPanel = new JPanel(  );
		radioPanel.setLayout( new BoxLayout( radioPanel, BoxLayout.Y_AXIS ) );
		radioPanel.add( Box.createVerticalStrut( 4 ) );
		automatic = createRadioButton( "Automatic", group, radioPanel );
		none = createRadioButton( "None", group, radioPanel );
		manual = createRadioButton( "Manual", group, radioPanel );
		proxyPrefForm.append( "Proxy Setting", radioPanel );
		automatic.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				autoProxy = true;
				setManualProxyTextFieldsEnabled( true, false );
			}
		} );
		manual.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				autoProxy = false;
				setManualProxyTextFieldsEnabled( true, true );
			}
		} );
		none.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				setManualProxyTextFieldsEnabled( false, false );
			}
		} );
	}

	private JRadioButton createRadioButton( String text, ButtonGroup group, JPanel radioPanel )
	{
		JRadioButton radioButton = new JRadioButton( text );
		radioButton.setBorder( null );
		group.add( radioButton );
		radioPanel.add( radioButton );
		return radioButton;
	}

	private void setManualProxyTextFieldsEnabled( boolean userPasswordEnabled, boolean otherFieldsEnabled )
	{
		hostTextField.setEnabled( otherFieldsEnabled );
		portTextField.setEnabled( otherFieldsEnabled );
		userTextField.setEnabled( userPasswordEnabled );
		passwordTextField.setEnabled( userPasswordEnabled );
		excludesTextField.setEnabled( otherFieldsEnabled );
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
		return values;
	}

	public void setFormValues( Settings settings )
	{
		getForm().setValues( getValues( settings ) );
		if( !settings.getBoolean( ProxySettings.ENABLE_PROXY ) )
		{
			none.setSelected( true );
			setManualProxyTextFieldsEnabled( false, false );
		}
		else if( settings.getBoolean( ProxySettings.AUTO_PROXY ) )
		{
			automatic.setSelected( true );
			autoProxy = true;
			setManualProxyTextFieldsEnabled( true, false );
		}
		else
		{
			manual.setSelected( true );
			autoProxy = false;
			setManualProxyTextFieldsEnabled( true, true );
		}
		autoProxy = settings.getBoolean( ProxySettings.AUTO_PROXY );
	}

	public void storeValues( StringToStringMap values, Settings settings )
	{
		String proxyHost = values.get( HOST );
		String proxyPort = values.get( PORT );
		settings.setString( ProxySettings.HOST, proxyHost );
		settings.setString( ProxySettings.PORT, proxyPort );
		settings.setString( ProxySettings.USERNAME, values.get( USERNAME ) );
		settings.setString( ProxySettings.PASSWORD, values.get( PASSWORD ) );
		settings.setString( ProxySettings.EXCLUDES, values.get( EXCLUDES ) );
		boolean enableProxy = !none.isSelected();
		if( !autoProxy && ( StringUtils.isNullOrEmpty( proxyHost ) || StringUtils.isNullOrEmpty( proxyPort ) ) )
		{
			enableProxy = false;
		}
		settings.setBoolean( ProxySettings.ENABLE_PROXY, enableProxy );
		settings.setBoolean( ProxySettings.AUTO_PROXY, autoProxy );
		SoapUI.updateProxyFromSettings();
	}

}
