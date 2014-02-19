package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2ClientFacade;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2TestUtils;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.MalformedURLException;
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

	public static final String EXPIRED_TOKEN = "EXPIRED#TOKEN";
	private OAuth2RequestFilter oAuth2RequestFilter;
	private SubmitContext mockContext;
	private RestRequest restRequest;
	private ExtendedPostMethod httpRequest;
	private OAuth2ProfileContainer oAuth2ProfileContainer;
	private OAuth2Profile oAuth2Profile;

	private final String accessToken = "ACDFECDSFKJFK#SDFSD8df#ACCESS-TOKEN";

	@Before
	public void setUp() throws SoapUIException, URISyntaxException
	{
		oAuth2RequestFilter = new OAuth2RequestFilter();

		restRequest = ModelItemFactory.makeRestRequest();
		restRequest.setAuthType( O_AUTH_2.toString() );
		WsdlProject project = restRequest.getOperation().getInterface().getProject();
		oAuth2ProfileContainer = project.getOAuth2ProfileContainer();
		oAuth2Profile = oAuth2ProfileContainer.addNewOAuth2Profile("profile");
		oAuth2Profile.setAccessToken( accessToken );


		httpRequest = new ExtendedPostMethod();
		httpRequest.setURI( new URI( "endpoint/path" ) );
		mockContext = Mockito.mock( SubmitContext.class );
		Mockito.when( mockContext.getProperty( BaseHttpRequestTransport.HTTP_METHOD ) ).thenReturn( httpRequest );
	}

	@Test
	public void appliesAccessToken() throws URISyntaxException
	{
		String expectedAccessTokenValue = "Bearer " + accessToken;
		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );
		assertThat( httpRequest.getHeaders( OAuth.HeaderType.AUTHORIZATION )[0].getValue(), is( expectedAccessTokenValue ) );
	}

	@Test
	public void doNotApplyNullAccessTokenToHeader() throws Exception
	{
		oAuth2Profile.setAccessToken( null );
		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );
		assertThat( httpRequest.getHeaders( OAuth.HeaderType.AUTHORIZATION ).length, is( 0 ) );
	}

	@Test
	public void doesNotApplyAccessTokenIfOAuthTypeIsNotOAuth2()
	{
		restRequest.setAuthType( PREEMPTIVE.toString() );
		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );
		assertThat( httpRequest.getHeaders( OAuth.HeaderType.AUTHORIZATION ).length, is( 0 ) );
	}

	@Test
	public void automaticallyRefreshAccessTokenIfExpired() throws Exception
	{
		OAuth2Profile profileWithRefreshToken = setProfileWithRefreshTokenAndExpiredAccessToken();
		oAuth2FilterWithMockOAuth2ClientFacade( profileWithRefreshToken );
		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );

		String actualAccessTokenHeader = httpRequest.getHeaders( ( OAuth.HeaderType.AUTHORIZATION ) )[0].getValue();
		assertThat( actualAccessTokenHeader, is( "Bearer " + OAuth2TestUtils.ACCESS_TOKEN ) );
	}

	@Test
	public void doesNotRefreshAccessTokenWhenRefreshMethodIsManual() throws SoapUIException
	{
		OAuth2Profile profileWithRefreshToken = setProfileWithRefreshTokenAndExpiredAccessToken();
		profileWithRefreshToken.setRefreshAccessTokenMethod( OAuth2Profile.RefreshAccessTokenMethods.MANUAL );
		oAuth2FilterWithMockOAuth2ClientFacade( profileWithRefreshToken );
		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );

		assertThat( profileWithRefreshToken.getAccessToken(), is( EXPIRED_TOKEN ) );
	}

	private OAuth2Profile setProfileWithRefreshTokenAndExpiredAccessToken() throws SoapUIException
	{
		final OAuth2Profile profileWithRefreshToken = OAuth2TestUtils.getOAuthProfileWithRefreshToken();
		setExpiredAccessToken( profileWithRefreshToken );

		oAuth2ProfileContainer.getOAuth2ProfileList().set( 0, profileWithRefreshToken );
		return profileWithRefreshToken;
	}

	private void oAuth2FilterWithMockOAuth2ClientFacade( final OAuth2Profile profileWithRefreshToken )
	{
		oAuth2RequestFilter = new OAuth2RequestFilter()
		{
			@Override
			protected OAuth2ClientFacade getOAuth2ClientFacade()
			{
				return OAuth2TestUtils.getOltuOAuth2ClientFacadeWithMockedTokenExtractor( profileWithRefreshToken );
			}
		};
	}

	private void setExpiredAccessToken( OAuth2Profile profileWithRefreshToken )
	{
		profileWithRefreshToken.setAccessToken( EXPIRED_TOKEN );
		profileWithRefreshToken.setAccessTokenIssuedTime( 1 );         //Token was issued Jan 1 1970
		profileWithRefreshToken.setAccessTokenExpirationTime( 10 );      //and expired 10 seconds later.
	}
}
