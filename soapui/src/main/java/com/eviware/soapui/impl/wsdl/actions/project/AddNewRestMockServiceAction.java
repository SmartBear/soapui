package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class AddNewRestMockServiceAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "AddNewRestMockServiceAction";

	public AddNewRestMockServiceAction()
	{
		super( "New REST MockService", "Creates a new REST MockService in this project" );
	}

	public void perform( WsdlProject target, Object param )
	{
		createRestMockService( target );
	}

	public MockService createRestMockService( WsdlProject project )
	{
		String name = UISupport.prompt( "Specify name of MockService", "New MockService",
				"REST MockService " + ( project.getRestMockServiceCount() + 1 ) );
		if( name == null )
			return null;
		while( project.getMockServiceByName( name.trim() ) != null )
		{
			name = UISupport.prompt( "Specify unique name of REST MockService", "Rename MockService", name );
		}

		MockService mockService = project.addNewRestMockService( name.trim() );
		UISupport.select( mockService );

		return mockService;
	}
}
