package com.eviware.soapui.impl.rest.actions.mock;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class AddRestRequestToMockServiceAction extends AbstractSoapUIAction<RestRequest>
{

	public static final String SOAPUI_ACTION_ID = "AddRestRequestToMockServiceAction";

	private static final MessageSupport messages = MessageSupport.getMessages( AddRestRequestToMockServiceAction.class );


	public AddRestRequestToMockServiceAction( )
	{
		super( messages.get( "Title" ), messages.get( "Description" ) );
	}

	@Override
	public void perform( RestRequest target, Object param )
	{

	}
}
