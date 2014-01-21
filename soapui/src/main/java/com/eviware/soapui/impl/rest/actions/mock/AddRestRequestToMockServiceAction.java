package com.eviware.soapui.impl.rest.actions.mock;

import com.eviware.soapui.config.RESTMockResponseConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
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
	public void perform( RestRequest restRequest, Object param )
	{
      String title = getName();
      WsdlProject project = restRequest.getRestMethod().getInterface().getProject();

		String defaultName = "MockService " + (project.getMockServiceCount() + 1);
      String mockServiceName = UISupport.prompt("Enter name of new MockService", title, defaultName);

      RestMockService mockService = project.addNewRestMockService( mockServiceName );

		addNewMockAction( restRequest, mockService );
   }

	private void addNewMockAction( RestRequest restRequest, RestMockService mockService )
	{
		RestMockAction restMockAction = mockService.addNewMockAction( restRequest );
		RESTMockResponseConfig responseConfig = restMockAction.getConfig().addNewRestMockResponse();
		RestMockResponse mockResponse = restMockAction.addNewMockResponse( responseConfig );

		// add expected response if available
		if( restRequest != null && restRequest.getResponse() != null )
		{
			HttpResponse response = restRequest.getResponse();
			mockResponse.setResponseContent( response.getContentAsString() );

		}
	}

}
