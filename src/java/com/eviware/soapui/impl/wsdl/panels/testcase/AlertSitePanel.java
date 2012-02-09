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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.NativeBrowserComponent;
import com.eviware.soapui.testondemand.DependencyValidator;
import com.eviware.soapui.testondemand.Location;
import com.eviware.soapui.testondemand.TestOnDemandCaller;
import com.google.common.base.Strings;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * 
 * Panel for displaying a AlertSite report
 * 
 */
public class AlertSitePanel extends JPanel
{
	@NonNull
	private JComboBox locationsComboBox;

	@NonNull
	private CustomNativeBrowserComponent browser;

	@NonNull
	private Action runAction;
	private boolean useSystemBrowser;

	private final WsdlTestCase testCase;
	private static List<Location> locationsCache;

	private TestOnDemandCaller caller = new TestOnDemandCaller();

	private DependencyValidator validator = new DependencyValidator();

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
		}
		else
		{
			JEditorPane jxbrowserDisabledPanel = new JEditorPane();
			jxbrowserDisabledPanel.setText( "browser component disabled or not available on this platform" );
			add( jxbrowserDisabledPanel, BorderLayout.CENTER );
		}

		if( locationsCache.isEmpty() )
		{
			runAction.setEnabled( false );
			locationsComboBox.setEnabled( false );
			if( !SoapUI.isJXBrowserDisabled( true ) )
			{
				browser.navigate( SoapUI.PUSH_PAGE_ERROR_URL, null );
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
		locationsComboBox = buildLocationsComboBox();
		toolbar.addFixed( locationsComboBox );
		toolbar.addGlue();
		toolbar.addFixed( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.ALERT_SITE_HELP_URL ) ) );

		return toolbar;
	}

	private JComboBox buildLocationsComboBox()
	{
		locationsCache = SoapUI.getSoapUICore().getTestOnDemandLocations();
		JComboBox cb = new JComboBox( locationsCache.toArray() );
		return cb;
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

			if( validator != null && !validator.isValid( testCase ) )
			{
				UISupport.showErrorMessage( "Your project contains external dependencies that "
						+ "are not supported by the Test-On-Demand functionality at this point." );
				return;
			}

			if( locationsComboBox != null )
			{
				Location selectedLocation = ( Location )locationsComboBox.getSelectedItem();

				String redirectUrl;

				// FIXME Add better error handling
				try
				{
					redirectUrl = caller.sendProject( testCase, selectedLocation );

					if( !Strings.isNullOrEmpty( redirectUrl ) )
					{
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

		// TODO check if clicking a link opens a new window
	}
}
