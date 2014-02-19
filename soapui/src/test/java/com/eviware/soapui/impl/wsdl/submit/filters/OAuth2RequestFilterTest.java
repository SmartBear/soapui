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
import org.apache.log4j.Logger;
import org.apache.oltu.oauth2.common.OAuth;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import static com.eviware.soapui.config.CredentialsConfig.AuthType.O_AUTH_2;
import static com.eviware.soapui.config.CredentialsConfig.AuthType.PREEMPTIVE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OAuth2RequestFilterTest
{

	private static final String ACCESS_TOKEN = "ACDFECDSFKJFK#SDFSD8df#ACCESS-TOKEN";

	private OAuth2RequestFilter oAuth2RequestFilter;
	private RestRequest restRequest;
	private ExtendedPostMethod httpRequest;
	private OAuth2ProfileContainer oAuth2ProfileContainer;
	@Mock
	private SubmitContext mockContext;
	@Mock
	private Logger mockLogger;
	private Logger realLogger;

	@Before
	public void setUp() throws SoapUIException, URISyntaxException
	{
		MockitoAnnotations.initMocks( this );

		oAuth2RequestFilter = new OAuth2RequestFilter();

		setupModelItems();
		setupRequest();
		replaceLogger();
	}

	@After
	public void restoreLogger() throws Exception
	{
		 OAuth2RequestFilter.setLog(realLogger);
	}

	@Test
	public void appliesAccessToken() throws URISyntaxException
	{
		String expectedAccessTokenValue = "Bearer " + ACCESS_TOKEN;
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
	public void automaticallyRefreshAccessTokenIfExpired() throws Exception
	{
		setupProfileWithRefreshToken();

		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );

		String actualAccessTokenHeader = httpRequest.getHeaders( ( OAuth.HeaderType.AUTHORIZATION ) )[0].getValue();
		assertThat( actualAccessTokenHeader, is( "Bearer " + OAuth2TestUtils.ACCESS_TOKEN ) );
	}

	@Test
	public void automaticallyReloadsAccessTokenWhenProfileHasAutomationScripts() throws Exception
	{
		setupProfileWithAutomationScripts();

		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );

		String actualAccessTokenHeader = httpRequest.getHeaders( ( OAuth.HeaderType.AUTHORIZATION ) )[0].getValue();
		assertThat( actualAccessTokenHeader, is( "Bearer " + OAuth2TestUtils.ACCESS_TOKEN ) );
	}

	@Test
	public void addsLogStatementsWhenRefreshingAccessToken() throws Exception
	{
		setupProfileWithRefreshToken();

		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );

		verify( mockLogger, times(2) ).info( any( String.class ) );
	}

	@Test
	public void addsLogStatementsWhenReloadingAccessToken() throws Exception
	{
		setupProfileWithAutomationScripts();

		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );

		verify( mockLogger, times(2) ).info( any( String.class ) );
	}

	@Test
	public void logsWarningWhenAutomationScriptsAreMissing() throws Exception
	{
		final OAuth2Profile profileWithoutAutomationScripts = OAuth2TestUtils.getOAuthProfileWithDefaultValues();
		setExpiredAccessToken( profileWithoutAutomationScripts );
		injectProfile( profileWithoutAutomationScripts );

		oAuth2RequestFilter.filterRestRequest( mockContext, restRequest );

		verify( mockLogger, times(1) ).warn( any( String.class ) );
	}


	/*
	Setup helpers.
	 */


	private void setupRequest() throws URISyntaxException
	{
		httpRequest = new ExtendedPostMethod();
		httpRequest.setURI( new URI( "endpoint/path" ) );
		when( mockContext.getProperty( BaseHttpRequestTransport.HTTP_METHOD ) ).thenReturn( httpRequest );
	}

	private void setupModelItems() throws SoapUIException
	{
		restRequest = ModelItemFactory.makeRestRequest();
		restRequest.setAuthType( O_AUTH_2.toString());
		WsdlProject project = restRequest.getOperation().getInterface().getProject();
		oAuth2ProfileContainer = project.getOAuth2ProfileContainer();
		OAuth2Profile oAuth2Profile = oAuth2ProfileContainer.getOAuth2ProfileList().get( 0 );
		oAuth2Profile.setAccessToken( ACCESS_TOKEN );
	}

	private void replaceLogger()
	{
		realLogger = OAuth2RequestFilter.getLog();
		OAuth2RequestFilter.setLog( mockLogger );
	}

	private void setupProfileWithRefreshToken() throws SoapUIException
	{
		final OAuth2Profile profileWithRefreshToken = OAuth2TestUtils.getOAuthProfileWithRefreshToken();
		setExpiredAccessToken( profileWithRefreshToken );
		injectProfile( profileWithRefreshToken );
	}

	private void setupProfileWithAutomationScripts() throws SoapUIException
	{
		final OAuth2Profile profileWithAutomationScripts = makeProfileWithAutomationScripts();
		setExpiredAccessToken( profileWithAutomationScripts );
		injectProfile( profileWithAutomationScripts );
	}

	private void injectProfile( final OAuth2Profile profileWithAutomationScripts )
	{
		oAuth2ProfileContainer.getOAuth2ProfileList().set( 0, profileWithAutomationScripts );
		oAuth2RequestFilter = new OAuth2RequestFilter(){
			@Override
			protected OAuth2ClientFacade getOAuth2ClientFacade()
			{
				return OAuth2TestUtils.getOltuOAuth2ClientFacadeWithMockedTokenExtractor( profileWithAutomationScripts );
			}
		};
	}

	private OAuth2Profile makeProfileWithAutomationScripts() throws SoapUIException
	{
		final OAuth2Profile profileWithAutomationScripts = OAuth2TestUtils.getOAuthProfileWithDefaultValues();
		profileWithAutomationScripts.setAutomationJavaScripts( Arrays.asList( "doLoginAndConsent()" ) );
		return profileWithAutomationScripts;
	}

	private void setExpiredAccessToken( OAuth2Profile profileWithRefreshToken )
	{
		profileWithRefreshToken.setAccessToken( "EXPIRED#TOKEN" );
		profileWithRefreshToken.setAccessTokenIssuedTime( 1 );			//Token was issued Jan 1 1970
		profileWithRefreshToken.setAccessTokenExpirationTime( 10 );		//and expired 10 seconds later.
	}
}
