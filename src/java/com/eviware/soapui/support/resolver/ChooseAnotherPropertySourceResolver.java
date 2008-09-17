/*
 * soapUI, copyright (C) 2004-2008 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfer;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ChooseAnotherPropertySourceResolver implements Resolver
{
	private boolean resolved;

	public ChooseAnotherPropertySourceResolver(PropertyTransfer propertyTransfer, PropertyTransfersTestStep parent)
	{
		// TODO Auto-generated constructor stub
	}

	public String getDescription()
	{
		return "Add new source property";
	}

	public String getResolvedPath()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isResolved()
	{
		return resolved;
	}

	public boolean resolve()
	{
		// TODO Auto-generated method stub
		return false;
	}

	private class ChoosePropertyDialog
	{

		void init()
		{
			FormLayout layout = new FormLayout("p,3dlu,p", "p,3dlu,p,3dlu,p");
			CellConstraints cc = new CellConstraints();
			PanelBuilder panel = new PanelBuilder(layout);
			panel.addLabel("Source:", cc.xy(1, 1));
		}
	}

}
