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

package com.eviware.soapui;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.ui.desktop.DesktopRegistry;
import com.eviware.soapui.ui.desktop.standalone.StandaloneDesktopFactory;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyBluer;

public class StandaloneSoapUICore extends SwingSoapUICore
{

	public StandaloneSoapUICore( boolean init )
	{
		super();

		if( init )
			init( DEFAULT_SETTINGS_FILE );
	}

	public StandaloneSoapUICore( String settingsFile )
	{
		super( null, settingsFile );
	}

	public StandaloneSoapUICore( boolean init, boolean settingPassword, String soapUISettingsPassword )
	{
		super( true, soapUISettingsPassword );

		if( init )
			init( DEFAULT_SETTINGS_FILE );
	}

	public void prepareUI()
	{
		super.prepareUI();

		initSoapUILookAndFeel();
		DesktopRegistry.getInstance().addDesktop( SoapUI.DEFAULT_DESKTOP, new StandaloneDesktopFactory() );
	}

	public void initSoapUILookAndFeel()
	{
		try
		{
			if( getSettings().getBoolean( UISettings.NATIVE_LAF ) )
			{
				javax.swing.UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			}
			else
			{
				SoapUITheme theme = new SoapUITheme();

				PlasticXPLookAndFeel.setCurrentTheme( theme );
				PlasticXPLookAndFeel.setTabStyle( "Metal" );

				UIManager.setLookAndFeel( new PlasticXPLookAndFeel() );
				UIManager.put( "TabbedPane.tabAreaInsets", new Insets( 3, 2, 0, 0 ) );
				UIManager.put( "TabbedPane.unselectedBackground", new Color( 220, 220, 220 ) );
				UIManager.put( "TabbedPane.selected", new Color( 240, 240, 240 ) );

				PlasticXPLookAndFeel.setPlasticTheme( theme );
			}
		}
		catch( Throwable e )
		{
			System.err.println( "Error initializing PlasticXPLookAndFeel; " + e.getMessage() );
		}
	}

	/**
	 * Adapted theme for soapUI Look and Feel
	 * 
	 * @author ole.matzura
	 */

	public static class SoapUITheme extends SkyBluer
	{
		public static final Color BACKGROUND_COLOR = new Color( 240, 240, 240 );

		public ColorUIResource getControl()
		{
			return new ColorUIResource( BACKGROUND_COLOR );
		}

		public ColorUIResource getMenuBackground()
		{
			return getControl();
		}

		public ColorUIResource getMenuItemBackground()
		{
			return new ColorUIResource( new Color( 248, 248, 248 ) );
		}
	}
}
