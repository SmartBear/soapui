package com.eviware.soapui.impl.rest.actions.mock;

import com.eviware.soapui.impl.rest.HttpMethod;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class AddEmptyRestMockResourceAction extends AbstractSoapUIAction<RestMockService>
{
	public static final String SOAPUI_ACTION_ID = "AddEmptyRestMockResourceAction";

	public AddEmptyRestMockResourceAction()
	{
		super( "Add new mock resource", "Add a new REST mock resource to this mock service" );
	}


	@Override
	public void perform( RestMockService mockService, Object param )
	{
		String resourceName = UISupport.prompt( "Enter name for new REST mock resource", "New REST mock resource", "" );
		mockService.addEmptyMockAction( resourceName, HttpMethod.GET, "/" );
	}
}
