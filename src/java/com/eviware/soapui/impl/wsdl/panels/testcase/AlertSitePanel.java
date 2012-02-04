/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.panels.testcase;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.testondemand.TestOnDemandCaller;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.NativeBrowserComponent;
import com.teamdev.jxbrowser.DefaultWebPolicyDelegate;
import com.teamdev.jxbrowser.events.NavigationEvent;

/**
 * 
 * Panel for displaying a AlertSite report
 * 
 */
public class AlertSitePanel extends JPanel
{

	private JComboBox locations;
	private CustomNativeBrowserComponent browser;
	private Action runAction;
	private boolean useSystemBrowser;

	// FIXME This should not be hardcoded, but fetched from the location resource in the AlertSite Rest API
	private static final String TEST_LOCATION_CODE = "10|ash.regression.alertsite.com";

	private static String[] locationsCache = getListOfLocations();
	private final WsdlTestCase testCase;

	public AlertSitePanel( WsdlTestCase testCase )
	{
		super( new BorderLayout() );
		this.testCase = testCase;
		setBackground( Color.WHITE );
		setOpaque( true );

		add( buildToolbar(), BorderLayout.NORTH );

		if( !SoapUI.isJXBrowserDisabled( true ) )
		{
			browser = new CustomNativeBrowserComponent( true, false );
			add( browser.getComponent(), BorderLayout.CENTER );
			browser.setWebPolicy();
		}
		else
		{
			JEditorPane jxbrowserDisabledPanel = new JEditorPane();
			jxbrowserDisabledPanel.setText( "browser component disabled or not available on this platform" );
			add( jxbrowserDisabledPanel, BorderLayout.CENTER );
		}

		if( runAction != null )
		{
			if( locationsCache.length == 0 )
			{
				runAction.setEnabled( false );
			}
		}
	}

	public void release()
	{
		if( browser != null )
		{
			browser.release();
		}
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		runAction = new RunAction();
		toolbar.addFixed( UISupport.createToolbarButton( runAction ) );
		toolbar.addRelatedGap();
		locations = buildLocationsComboBox();
		toolbar.addFixed( locations );
		toolbar.addGlue();
		toolbar.addFixed( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.ALERT_SITE_HELP_URL ) ) );

		return toolbar;
	}

	private JComboBox buildLocationsComboBox()
	{
		JComboBox cb = new JComboBox( locationsCache );
		return cb;
	}

	private static String[] getListOfLocations()
	{
		String[] arr = { new Location( TEST_LOCATION_CODE, TEST_LOCATION_CODE ).getName() };
		return arr;
	}

	private static class Location
	{
		private String code;
		private String name;

		public Location( String code, String name )
		{
			this.code = code;
			this.name = name;
		}

		public String getCode()
		{
			return this.code;
		}

		public String getName()
		{
			return this.name;
		}
	}

	private class RunAction extends AbstractAction
	{
		public RunAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/run.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Run Test On Demand report" );
		}

		public void actionPerformed( ActionEvent arg0 )
		{
			if( locations != null )
			{
				String name = ( String )locations.getSelectedItem();

				TestOnDemandCaller caller = new TestOnDemandCaller();
				String redirectUrl;

				// FIXME Add better error handling
				try
				{
					redirectUrl = caller.sendProject( testCase, name );

					if( SoapUI.isJXBrowserDisabled( true ) )
					{
						Tools.openURL( redirectUrl );
					}
					else
					{
						if( browser != null )
						{
							browser.navigate( redirectUrl, null );
						}
					}
					useSystemBrowser = false;
				}
				catch( Exception e )
				{
					SoapUI.logError( e );
				}
			}
		}
	}

	private class CustomNativeBrowserComponent extends NativeBrowserComponent
	{

		public CustomNativeBrowserComponent( boolean addToolbar, boolean addStatusBar )
		{
			super( addToolbar, addStatusBar );
		}

		public void setWebPolicy()
		{
			if( getBrowser() != null )
			{
				getBrowser().getServices().setWebPolicyDelegate( new DefaultWebPolicyDelegate()
				{
					@Override
					public boolean allowNavigation( NavigationEvent event )
					{
						if( !useSystemBrowser )
						{
							useSystemBrowser = true;
							return true;
						}
						// return false to cancel navigation
						Tools.openURL( event.getUrl() );
						return false;
					}
				} );
			}
		}
	}

}
