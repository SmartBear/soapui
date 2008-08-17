package com.eviware.soapui.impl.wsdl.panels.teststeps;

import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

public class RestTestRequestDesktopPanel extends ModelItemDesktopPanel<RestTestRequestStep>
{

	public RestTestRequestDesktopPanel(RestTestRequestStep modelItem)
	{
		super(modelItem);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean dependsOn(ModelItem modelItem)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onClose(boolean canCancel)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
