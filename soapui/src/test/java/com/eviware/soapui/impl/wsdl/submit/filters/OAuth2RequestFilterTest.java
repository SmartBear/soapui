package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2TestUtils;
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

import static com.eviware.soapui.config.CredentialsConfig.AuthType.O_AUTH_2;
import static com.eviware.soapui.config.CredentialsConfig.AuthType.PREEMPTIVE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class OAuth2RequestFilterTest
{

	private OAuth2RequestFilter oAuth2RequestFilter;
	private SubmitContext mockContext;
	private RestRequest restRequest;
	private ExtendedPostMethod httpRequest;
	private OAuth2ProfileContainer oAuth2ProfileContainer;
	private OAuth2Profile oAuth2Profile;

	private final String accessToken = "ACDFECDSFKJFK#SDFSD8df#ACCESS-TOKEN";
	private final String refreshToken = "ACDFECDSFKJFK#SDFSD8df#REFRESH-TOKEN";


	@Before
	public void setUp() throws SoapUIException, URISyntaxException
	{
		oAuth2RequestFilter = new OAuth2RequestFilter();

		restRequest = ModelItemFactory.makeRestRequest();
		restRequest.setAuthType( O_AUTH_2.toString());
		WsdlProject project = restRequest.getOperation().getInterface().getProject();
		oAuth2ProfileContainer = project.getOAuth2ProfileContainer();
		oAuth2Profile = oAuth2ProfileContainer.getOAuth2ProfileList().get( 0 );
		oAuth2Profile.setAccessToken( accessToken );


		httpRequest = new ExtendedPostMethod();
		httpRequest.setURI(  new URI( "endpoint/path" ) );
		mockContext = Mockito.mock( SubmitContext.class );
		Mockito.when( mockContext.getProperty( BaseHttpRequestTransport.HTTP_METHOD )).thenReturn( httpRequest );
	}

	@Test
	public void appliesAccessToken() throws URISyntaxException
	{
		String expectedAccessTokenValue = "Bearer " + accessToken;
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

	@Test
	public void doesNotApplyAccessTokenIfOAuthTypeIsNotOAuth2()
	{
		restRequest.setAuthType( PREEMPTIVE.toString() );
		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );
		assertThat( httpRequest.getHeaders( OAuth.HeaderType.AUTHORIZATION ).length, is( 0 ) ) ;
	}

	@Ignore
	@Test
	public void automaticallyRefreshAccessTokenIfExpired() throws SoapUIException
	{
		// Set expired token on profile
		String expiredAccessToken = "EXPIREDXLA#EXPIREDX";

		OAuth2Profile profile = OAuth2TestUtils.getOAuthProfileWithRefreshToken();
		oAuth2ProfileContainer.getOAuth2ProfileList().set( 0, profile );

		// Put the request through our filter
		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );

		// Verify that the filter has changed our expired access token
		String actualAccessTokenHeader = httpRequest.getHeaders( ( OAuth.HeaderType.AUTHORIZATION ) )[0].getValue();
		assertThat( actualAccessTokenHeader, is( not( "Bearer " + expiredAccessToken ) ) );
	}
}
