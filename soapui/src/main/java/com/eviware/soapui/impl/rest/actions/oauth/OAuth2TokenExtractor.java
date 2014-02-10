package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.support.StringUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.GitHubTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.oltu.oauth2.httpclient4.HttpClient4;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public class OAuth2TokenExtractor
{

	public static final String CODE = "code";
	public static final String TITLE = "<TITLE>";
	public static final String TOKEN = "token";
	public static final String ACCESS_TOKEN = "access_token";

	UserBrowserFacade browserFacade = new WebViewUserBrowserFacade();

	void extractAccessTokenForAuthorizationCodeGrantFlow( final OAuth2Parameters parameters ) throws URISyntaxException,
			MalformedURLException, OAuthSystemException
	{
		{
			browserFacade.addBrowserStateListener( new BrowserStateChangeListener()
			{
				@Override
				public void locationChanged( String newLocation )
				{
					getAccessTokenAndSaveToProfile( parameters, extractAuthorizationCodeFromForm( extractFormData( newLocation ), CODE ) );
				}

				@Override
				public void contentChanged( String newContent )
				{
					int titlePosition = newContent.indexOf( TITLE );
					if( titlePosition != -1 )
					{
						String title = newContent.substring( titlePosition + TITLE.length(), newContent.indexOf( "</TITLE>" ) );
						getAccessTokenAndSaveToProfile( parameters, extractAuthorizationCodeFromTitle( title ) );
					}
				}

			} );
			parameters.startAccessTokenFlow();
			browserFacade.open( new URI( createAuthorizationURL( parameters, CODE ) ).toURL() );
			parameters.waitingForAuthorization();
		}

	}

	void extractAccessTokenForImplicitGrantFlow(final OAuth2Parameters parameters) throws OAuthSystemException,
			URISyntaxException, MalformedURLException
	{
		{
			browserFacade.addBrowserStateListener( new BrowserStateChangeListener()
			{
				@Override
				public void locationChanged( String newLocation )
				{
					String accessToken = extractAuthorizationCodeFromForm( extractFormData( newLocation ), ACCESS_TOKEN );
					if( !StringUtils.isNullOrEmpty( accessToken ))
					{
						parameters.setAccessTokenInProfile( accessToken );
						browserFacade.close();
					}
				}

				@Override
				public void contentChanged( String newContent )
				{
				}

			} );
			parameters.startAccessTokenFlow();
			browserFacade.open( new URI( createAuthorizationURL( parameters, TOKEN ) ).toURL() );
			parameters.waitingForAuthorization();
		}
	}

	void refreshAccessToken( OAuth2Parameters parameters ) throws OAuthProblemException, OAuthSystemException
	{
		OAuthClientRequest accessTokenRequest = OAuthClientRequest
				.tokenLocation( parameters.accessTokenUri )
				.setGrantType( GrantType.REFRESH_TOKEN )
				.setClientId( parameters.clientId )
				.setClientSecret( parameters.clientSecret )
				.setRefreshToken( parameters.refreshToken )
				.buildBodyMessage();

		OAuthClient oAuthClient = getOAuthClient();

		OAuthToken oAuthToken = oAuthClient.accessToken( accessTokenRequest, OAuthJSONAccessTokenResponse.class ).getOAuthToken();
		parameters.applyRetrievedAccessToken( oAuthToken.getAccessToken() );
	}

	protected OAuthClient getOAuthClient()
	{
		return new OAuthClient( new HttpClient4( HttpClientSupport.getHttpClient() ) );
	}

	private String createAuthorizationURL( OAuth2Parameters parameters, String responseType )
			throws OAuthSystemException
	{
		return OAuthClientRequest
				.authorizationLocation( parameters.authorizationUri )
				.setClientId( parameters.clientId )
				.setResponseType( responseType )
				.setScope( parameters.scope )
				.setRedirectURI( parameters.redirectUri )
				.buildQueryMessage().getLocationUri();
	}

	private String extractFormData( String url )
	{
		int questionMarkIndex = url.indexOf( '?' );
		if( questionMarkIndex != -1 )
		{
			return url.substring( questionMarkIndex + 1 );
		}

		int hashIndex = url.indexOf( "#" );
		if(hashIndex!=-1)
		{
			return url.substring( hashIndex+1 );
		}
		return "";
	}

	private String extractAuthorizationCodeFromTitle( String title )
	{
		if( title.contains( "code=" ) )
		{
			return title.substring( title.indexOf( "code=" ) + 5 );
		}
		return null;
	}

	private String extractAuthorizationCodeFromForm( String formData, String parameterName )
	{
		return ( String )OAuthUtils.decodeForm( formData ).get( parameterName );
	}

	private void getAccessTokenAndSaveToProfile( OAuth2Parameters parameters, String authorizationCode )
	{
		if( authorizationCode != null )
		{
			try
			{
				parameters.receivedAuthorizationCode();
				OAuthClientRequest accessTokenRequest = OAuthClientRequest
						.tokenLocation( parameters.accessTokenUri )
						.setGrantType( GrantType.AUTHORIZATION_CODE )
						.setClientId( parameters.clientId )
						.setClientSecret( parameters.clientSecret )
						.setRedirectURI( parameters.redirectUri )
						.setCode( authorizationCode )
						.buildBodyMessage();
				OAuthToken token = null;
				switch( parameters.accessTokenRetrievalLocation )
				{
					case BODY_URL_ENCODED_FORM:
						token = getOAuthClient().accessToken( accessTokenRequest, GitHubTokenResponse.class ).getOAuthToken();
						break;
					case BODY_JSON:
					default:
						token = getOAuthClient().accessToken( accessTokenRequest, OAuthJSONAccessTokenResponse.class )
								.getOAuthToken();
						break;
				}
				if( token != null && token.getAccessToken() != null )
				{
					parameters.setAccessTokenInProfile( token.getAccessToken() );
					parameters.setRefreshTokenInProfile( token.getRefreshToken() );
					browserFacade.close();
				}
			}
			catch( OAuthSystemException e )
			{
				SoapUI.logError( e );
			}
			catch( OAuthProblemException e )
			{
				SoapUI.logError( e );
			}
		}
	}
}
