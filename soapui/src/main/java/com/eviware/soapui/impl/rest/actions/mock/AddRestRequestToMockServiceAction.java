package com.eviware.soapui.impl.rest.actions.mock;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
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
      String title = getName();
      WsdlProject project = target.getRestMethod().getInterface().getProject();

		String defaultName = "MockService " + (project.getMockServiceCount() + 1);
      String mockServiceName = UISupport.prompt("Enter name of new MockService", title, defaultName);

      project.addNewRestMockService(mockServiceName);
   }
}
