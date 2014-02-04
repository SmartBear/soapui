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

package com.eviware.soapui.impl.wsdl.panels.monitor;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitor;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class SoapMonitorDesktopPanel extends DefaultDesktopPanel implements SoapMonitorContainer
{
	private SoapMonitor soapMonitor;
	private final WsdlProject project;

	public SoapMonitorDesktopPanel( WsdlProject project, int sourcePort, String incomingRequestWss,
			String incomingResponseWss, boolean setAsProxy, String sslEndpoint )
	{
		super( "HTTP Monitor [" + project.getName() + "]", null, new JPanel( new BorderLayout() ) );
		this.project = project;

		JPanel p = ( JPanel )getComponent();
		JTabbedPane tabs = new JTabbedPane();

		JXToolBar toolbar = UISupport.createToolbar();
		soapMonitor = new SoapMonitor( project, sourcePort, incomingRequestWss, incomingResponseWss, toolbar, setAsProxy,
				sslEndpoint );

		tabs.add( soapMonitor, "Traffic Log" );

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

	public SoapMonitor getSoapMonitor()
	{
		return soapMonitor;
	}
}