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
import javax.swing.JPanel;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.BrowserComponent;
import com.eviware.soapui.support.components.JXToolBar;

/**
 * 
 * Panel for displaying a AlertSite report
 * 
 */
public class AlertSitePanel extends JPanel
{

	private JComboBox locations;
	private BrowserComponent browser;

	private static String[] locationsCache = getListOfLocations();

	public AlertSitePanel()
	{
		super( new BorderLayout() );
		setBackground( Color.WHITE );
		setOpaque( true );

		add( buildToolbar(), BorderLayout.NORTH );
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		toolbar.addFixed( UISupport.createToolbarButton( new RunAction() ) );
		toolbar.addFixed( buildLocationsComboBox() );
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
		String[] arr = { new Location( "1", "name1" ).getName(), new Location( "2", "name2" ).getName() };
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
		}
	}

}
