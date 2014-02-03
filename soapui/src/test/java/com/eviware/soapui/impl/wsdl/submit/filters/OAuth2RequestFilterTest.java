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
	private String expiredAccessToken = "EXPIREDXLA#EXPIREDX";
	private String accessToken = "ACDFECDSFKJFK#SDFSD8df";
	private ExtendedPostMethod httpRequest;
	private OAuth2Profile oAuth2Profile;

	@Before
	public void setUp() throws SoapUIException, URISyntaxException
	{
		oAuth2RequestFilter = new OAuth2RequestFilter();

		restRequest = ModelItemFactory.makeRestRequest();
		restRequest.setAuthType( O_AUTH_2.toString());
		WsdlProject project = restRequest.getOperation().getInterface().getProject();
		OAuth2ProfileContainer oAuth2ProfileContainer = project.getOAuth2ProfileContainer();
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

	@Test
	public void automaticallyRefreshAccessTokenIfExpired()
	{
		// Sätt en utgången access token på en profil
		oAuth2Profile.setAccessToken( expiredAccessToken );
		oAuth2Profile.setAccessTokenIssuedTime( 0 );			//issued 43 years ago
		oAuth2Profile.setAccessTokenExpirationTime( 1 );  	//and expired one second later

		// Skicka en request med den utgångna tokenen


		// Kolla så att vårt filter upptäckte detta och refreshade den
		String actualAccessToken = httpRequest.getHeaders( ( OAuth.HeaderType.AUTHORIZATION ) )[0].getValue();
		assertThat( actualAccessToken, is( not( expiredAccessToken ) ) );
	}
}
