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
package com.eviware.soapui.impl.wsdl.actions.project;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

public class StartHermesJMS extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "StarHermesJMS";

	public StartHermesJMS()
	{
		super("Start HermesJMS", "Start HermesJMS application");
	}

	public void perform(WsdlProject project, Object param)
	{

		String hermesConfigPath = UISupport.prompt("Specify path to hermes-config.xml file", "Hermes configuration",
				PropertyExpander.expandProperties(project, project.getHermesConfig()));
		if (hermesConfigPath == null)
			return;

		String hermesHome = SoapUI.getSettings().getString(ToolsSettings.HERMES_1_13, "");
		if ("".equals(hermesHome))
		{
			UISupport.showErrorMessage("Please set Hermes 1.13 path in Preferences->Tools ! ");
			return;
		}
		String extension = UISupport.isWindows() ? ".bat" : ".sh";
		String hermesBatPath = hermesHome + File.separator + "bin" + File.separator + "hermes" + extension;
		try
		{
			ProcessBuilder pb = new ProcessBuilder(hermesBatPath);
			Map<String, String> env = pb.environment();
			env.put("HERMES_CONFIG", hermesConfigPath);
			pb.start();
		}
		catch (IOException e)
		{
			SoapUI.logError(e);
		}
	}


}
