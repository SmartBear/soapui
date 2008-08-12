/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.actions.monitor;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.panels.monitor.SoapMonitorDesktopPanel;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.types.StringList;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

public class SoapMonitorAction extends AbstractSoapUIAction<WsdlProject>
{
	private XFormDialog dialog;

	public SoapMonitorAction()
	{
		super("Launch SOAP Monitor", "Launches a SOAP traffic monitor for this project");
	}

	public void perform(WsdlProject target, Object param)
	{
		if (target.getInterfaceCount() == 0)
		{
			UISupport.showErrorMessage("Missing interfaces to monitor");
			return;
		}

		if (dialog == null)
		{
			dialog = ADialogBuilder.buildDialog(LaunchForm.class);
		}

		Settings settings = target.getSettings();

		StringList endpoints = new StringList();
		endpoints.add(null);

		for (Interface iface : target.getInterfaceList())
		{
			if (iface.getInterfaceType().equals(WsdlInterfaceFactory.WSDL_TYPE))
				endpoints.addAll(iface.getEndpoints());
		}

		dialog.setIntValue(LaunchForm.PORT, (int) settings.getLong(LaunchForm.PORT, 8081));
		dialog.setOptions(LaunchForm.REQUEST_WSS, StringUtils.merge(target.getWssContainer().getIncomingWssNames(),
				"<none>"));
		dialog.setOptions(LaunchForm.RESPONSE_WSS, StringUtils.merge(target.getWssContainer().getIncomingWssNames(),
				"<none>"));

		if (dialog.show())
		{
			int listenPort = dialog.getIntValue(LaunchForm.PORT, 8080);
			settings.setLong(LaunchForm.PORT, listenPort);

			openSoapMonitor(target, listenPort, dialog.getValue(LaunchForm.REQUEST_WSS), dialog
					.getValue(LaunchForm.RESPONSE_WSS));
		}
	}

	protected void openSoapMonitor(WsdlProject target, int listenPort, String incomingRequestWss,
			String incomingResponseWss)
	{
		UISupport.showDesktopPanel(new SoapMonitorDesktopPanel(target, listenPort, incomingRequestWss,
				incomingResponseWss));
	}

	@AForm(description = "Specify SOAP Monitor settings", name = "Launch SOAP Monitor")
	private interface LaunchForm
	{
		@AField(description = "The local port to listen on", name = "Port", type = AFieldType.INT)
		public final static String PORT = "Port";

		@AField(description = "The Incoming WSS configuration to use for processing requests", name = "Incoming Request WSS", type = AFieldType.ENUMERATION)
		public final static String REQUEST_WSS = "Incoming Request WSS";

		@AField(description = "The Outgoing WSS configuration to use for processing responses", name = "Incoming Response WSS", type = AFieldType.ENUMERATION)
		public final static String RESPONSE_WSS = "Incoming Response WSS";
	}
}
