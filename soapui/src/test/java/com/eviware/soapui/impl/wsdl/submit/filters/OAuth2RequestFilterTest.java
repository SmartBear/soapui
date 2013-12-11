package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.oltu.oauth2.common.OAuth;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OAuth2RequestFilterTest
{

	private OAuth2RequestFilter oAuth2RequestFilter;
	private SubmitContext mockContext;
	private RestRequest restRequest;
	private String accessToken = "ACDFECDSFKJFK#SDFSD8df";

	@Before
	public void setUp() throws SoapUIException
	{
		oAuth2RequestFilter = new OAuth2RequestFilter();

		restRequest = ModelItemFactory.makeRestRequest();
		WsdlProject project = restRequest.getOperation().getInterface().getProject();
		OAuth2ProfileContainer oAuth2ProfileContainer = project.getOAuth2ProfileContainer();
		OAuth2Profile oAuth2Profile = oAuth2ProfileContainer.addNewOAuth2Profile();
		oAuth2Profile.setAccessToken( accessToken );

		mockContext = Mockito.mock( SubmitContext.class );
	}

	@Test
	public void appliesAccessToken() throws URISyntaxException
	{
		String expectedAccessTokenValue = "Bearer "+ accessToken;
		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );

		assertThat( restRequest.getRequestHeaders().get( OAuth.HeaderType.AUTHORIZATION ).get( 0 ), is( expectedAccessTokenValue ) ) ;

	}
}
