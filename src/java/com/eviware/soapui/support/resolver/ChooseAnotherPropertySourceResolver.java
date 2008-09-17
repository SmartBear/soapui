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

	@Override
	public String getDescription()
	{
		return "Add new source property";
	}

	@Override
	public String getResolvedPath()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isResolved()
	{
		return resolved;
	}

	@Override
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
