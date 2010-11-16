/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.panels.monitor;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitor;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.security.monitor.MonitorSecurityTest;
import com.eviware.soapui.security.panels.SecurityTestsMonitorDesktopPanel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

public class SoapMonitorDesktopPanel extends DefaultDesktopPanel
{
	private SoapMonitor soapMonitor;
	private final WsdlProject project;
	private SecurityTestsMonitorDesktopPanel securityTab;

	public SoapMonitorDesktopPanel( WsdlProject project, int sourcePort, String incomingRequestWss,
			String incomingResponseWss, boolean setAsProxy, String sslEndpoint )
	{
		super( "HTTP Monitor [" + project.getName() + "]", null, new JPanel( new BorderLayout() ) );
		this.project = project;

		JPanel p = ( JPanel )getComponent();
		JTabbedPane tabs = new JTabbedPane();

		JXToolBar toolbar = UISupport.createToolbar();
		MonitorSecurityTest securityTest = new MonitorSecurityTest();
		soapMonitor = new SoapMonitor( project, sourcePort, incomingRequestWss, incomingResponseWss, toolbar, setAsProxy,
				sslEndpoint, securityTest );

		tabs.add( soapMonitor, "Traffic Log" );
		securityTab = new SecurityTestsMonitorDesktopPanel( new MonitorSecurityTest() );
		tabs.add( securityTab, "Security Tests" );

		toolbar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.SOAPMONITOR_HELP_URL ) ) );

		p.add( toolbar, BorderLayout.NORTH );
		p.add( UISupport.createTabPanel( tabs, true ), BorderLayout.CENTER );

		p.setPreferredSize( new Dimension( 700, 600 ) );
	}

	@Override
	public boolean onClose( boolean canCancel )
	{
		if( soapMonitor.isRunning() && canCancel )
		{
			if( !UISupport.confirm( "Close and stop HTTP Monitor", "Close HTTP Monitor" ) )
			{
				return false;
			}
		}

		soapMonitor.stop();
		soapMonitor.release();
		return true;
	}

	@Override
	public boolean dependsOn( ModelItem modelItem )
	{
		return modelItem == project;
	}

	public WsdlProject getProject()
	{
		return project;
	}
}