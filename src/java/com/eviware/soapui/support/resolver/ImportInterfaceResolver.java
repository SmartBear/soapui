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
package com.eviware.soapui.support.resolver;

import java.io.File;

import javax.swing.JOptionPane;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.propertyexpansion.resolvers.providers.ProjectDirProvider;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

public abstract class ImportInterfaceResolver implements Resolver
{
	private boolean resolved = false;
	private WsdlTestRequestStep item;

	public ImportInterfaceResolver(WsdlTestRequestStep item)
	{
		this.item = item;
	}

	public String getResolvedPath()
	{
		return "";
	}

	public boolean isResolved()
	{
		return resolved;
	}

	public boolean resolve()
	{
		String[] options = { "File", "Url", "Cancel" };
		int choosed = JOptionPane
				.showOptionDialog(UISupport.getMainFrame(), "Choose source for new interface from ...",
						"New interface source", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
						options, null);
		switch (choosed)
		{
		case 0:
		{
			loadWsdlFromFile();
		}
			break;
		case 1:
		{
			loadWsdlFromUrl();
		}
			break;
		case 2:
			break;
		}
		resolved = update();
//		ResolveContext context = new ResolveContext(item.getTestCase().getTestSuite());
//		item.getTestCase().getTestSuite().resolve(context);
//		
//		if (context.isEmpty())
//		{
//			resolved = true;
//		}
//		else
//		{
//			resolved = false;
//		}
		return resolved;
	}
	
	protected abstract boolean update();

	private void loadWsdlFromUrl()
	{
		WsdlProject project = item.getTestCase().getTestSuite().getProject();
		String url = UISupport.prompt("Enter WSDL URL", "Add WSDL from URL", "");
		if (url == null)
			return;

		try
		{
			Boolean createRequests = UISupport
					.confirmOrCancel("Create default requests for all operations", "Import WSDL");
			if (createRequests == null)
				return;

			Interface[] ifaces = WsdlInterfaceFactory.importWsdl(project, url, createRequests);
			if (ifaces != null && ifaces.length > 0)
				UISupport.select(ifaces[0]);
		}
		catch (Exception ex)
		{
			UISupport.showErrorMessage(ex.getMessage() + ":" + ex.getCause());
		}
	}

	private void loadWsdlFromFile()
	{

		WsdlProject project = item.getTestCase().getTestSuite().getProject();
		File file = UISupport.getFileDialogs().open(this, "Select WSDL file", ".wsdl", "WSDL Files (*.wsdl)",
				ProjectDirProvider.getProjectFolder(project));
		if (file == null)
			return;

		String path = file.getAbsolutePath();
		if (path == null)
			return;

		try
		{
			Boolean createRequests = UISupport
					.confirmOrCancel("Create default requests for all operations", "Import WSDL");
			if (createRequests == null)
				return;

			Interface[] ifaces = WsdlInterfaceFactory.importWsdl(project, file.toURI().toURL().toString(), createRequests);
			if (ifaces.length > 0)
				UISupport.select(ifaces[0]);
		}
		catch (Exception ex)
		{
			UISupport.showErrorMessage(ex.getMessage() + ":" + ex.getCause());
		}
	}

	public String getDescription()
	{
		return "Resolve: Import inteface";
	}

	@Override
	public String toString()
	{
		return getDescription();
	}
}
