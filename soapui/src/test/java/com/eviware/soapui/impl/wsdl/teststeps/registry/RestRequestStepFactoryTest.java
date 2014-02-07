package com.eviware.soapui.impl.wsdl.teststeps.registry;

import com.eviware.soapui.config.RestRequestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.rest.HttpMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class RestRequestStepFactoryTest
{
	@Test
	public void copiesRESTRequestBody() throws Exception
	{
		RestRequest restRequest = ModelItemFactory.makeRestRequest();
		restRequest.setMethod( HttpMethod.POST );
		String requestBody = "Some meaningful data";
		restRequest.setRequestContent( requestBody );

		TestStepConfig testStepConfig = RestRequestStepFactory.createConfig( restRequest, "Rest Request" );
		RestRequestStepConfig config = ( RestRequestStepConfig )  testStepConfig.getConfig();

		assertThat( config.getRestRequest().getRequest().getStringValue(), is( requestBody ) );
	}
}
