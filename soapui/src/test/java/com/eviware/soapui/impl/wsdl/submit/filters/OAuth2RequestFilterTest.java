package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.oltu.oauth2.common.OAuth;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class OAuth2RequestFilterTest
{

	private OAuth2RequestFilter oAuth2RequestFilter;
	private SubmitContext mockContext;
	private RestRequest restRequest;
	private String accessToken = "ACDFECDSFKJFK#SDFSD8df";
	private ExtendedPostMethod httpRequest;

	@Before
	public void setUp() throws SoapUIException, URISyntaxException
	{
		oAuth2RequestFilter = new OAuth2RequestFilter();

		restRequest = ModelItemFactory.makeRestRequest();
		WsdlProject project = restRequest.getOperation().getInterface().getProject();
		OAuth2ProfileContainer oAuth2ProfileContainer = project.getOAuth2ProfileContainer();
		OAuth2Profile oAuth2Profile = oAuth2ProfileContainer.addNewOAuth2Profile();
		oAuth2Profile.setAccessToken( accessToken );


		httpRequest = new ExtendedPostMethod();
		httpRequest.setURI(  new URI( "endpoint/path" ) );
		mockContext = Mockito.mock( SubmitContext.class );
		Mockito.when( mockContext.getProperty( BaseHttpRequestTransport.HTTP_METHOD )).thenReturn( httpRequest );
	}

	@Test
	public void appliesAccessToken() throws URISyntaxException
	{
		String expectedAccessTokenValue = "Bearer "+ accessToken;
		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );

		assertThat( httpRequest.getHeaders(OAuth.HeaderType.AUTHORIZATION )[0].getValue(), is( expectedAccessTokenValue ) ) ;
	}

	@Test
	public void doNotApplyNullAccessTokenToHeader() throws Exception
	{
		restRequest.getOperation().getInterface().getProject().getOAuth2ProfileContainer().getOAuth2ProfileList().get( 0 ).setAccessToken( null );

		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );

		assertThat( httpRequest.getHeaders( OAuth.HeaderType.AUTHORIZATION ).length, is( 0 ) ) ;
	}

}
