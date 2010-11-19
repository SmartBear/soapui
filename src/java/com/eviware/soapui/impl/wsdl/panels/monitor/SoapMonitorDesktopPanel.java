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

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitor;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.monitor.MonitorSecurityTest;
import com.eviware.soapui.security.panels.SecurityTestsMonitorDesktopPanel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

public class SoapMonitorDesktopPanel extends DefaultDesktopPanel
{
	private SoapMonitor soapMonitor;
	private final WsdlProject project;
	private SecurityTestsMonitorDesktopPanel securityTab;
	JSecurityTestRunLog securityTestRunLog;
	JInspectorPanel inspectorPanel;
	JTabbedPane tabs;

	public SoapMonitorDesktopPanel( WsdlProject project, int sourcePort, String incomingRequestWss,
			String incomingResponseWss, boolean setAsProxy, String sslEndpoint )
	{
		super( "HTTP Monitor [" + project.getName() + "]", null, new JPanel( new BorderLayout() ) );
		this.project = project;

		JPanel p = ( JPanel )getComponent();
		tabs = new JTabbedPane();

		JXToolBar toolbar = UISupport.createToolbar();
		soapMonitor = new SoapMonitor( project, sourcePort, incomingRequestWss, incomingResponseWss, toolbar, setAsProxy,
				sslEndpoint );
		JComponent securityTestRunLog = buildRunLog( soapMonitor );
		MonitorSecurityTest securityTest = new MonitorSecurityTest( ( JSecurityTestRunLog )securityTestRunLog );
		soapMonitor.setMonitorSecurityTest( securityTest );

		tabs.add( soapMonitor, "Traffic Log" );
		securityTab = new SecurityTestsMonitorDesktopPanel( securityTest );
		tabs.add( securityTab, "Security Tests" );

		toolbar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.SOAPMONITOR_HELP_URL ) ) );

		JPanel innerPanel = new JPanel( new BorderLayout() );
		innerPanel.add( toolbar, BorderLayout.NORTH );
		innerPanel.add( UISupport.createTabPanel( tabs, true ), BorderLayout.CENTER );

		inspectorPanel = JInspectorPanelFactory.build( innerPanel );
		JComponentInspector<JComponent> logInspector = new JComponentInspector<JComponent>( securityTestRunLog,
				"SecurityChecks Log", "Log of applied SecurityChecks", true );
		inspectorPanel.addInspector( logInspector );
		p.add( inspectorPanel.getComponent() );
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

	private JComponent buildRunLog( SoapMonitor soapMonitor )
	{
		securityTestRunLog = new JSecurityTestRunLog( soapMonitor, tabs );
		return securityTestRunLog;
	}

}