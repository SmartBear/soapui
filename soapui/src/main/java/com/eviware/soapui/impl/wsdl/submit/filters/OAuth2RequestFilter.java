package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2ClientFacade;
import com.eviware.soapui.impl.rest.actions.oauth.OltuOAuth2ClientFacade;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.TimeUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.log4j.Logger;

import static com.eviware.soapui.config.CredentialsConfig.AuthType.O_AUTH_2;

public class OAuth2RequestFilter extends AbstractRequestFilter
{
	// intentionally left non-final to facilitate testing, but should not be modified in production!
	private static Logger log = Logger.getLogger( OAuth2RequestFilter.class );


	/* setLog() and getLog() should only be used for testing */

	static void setLog(Logger newLog)
	{
		log = newLog;
	}

	static Logger getLog()
	{
		return log;
	}

	@Override
	public void filterRestRequest( SubmitContext context, RestRequestInterface request )
	{

		HttpRequestBase httpMethod = ( HttpRequestBase )context.getProperty( BaseHttpRequestTransport.HTTP_METHOD );

		OAuth2ProfileContainer profileContainer = request.getResource().getService().getProject()
				.getOAuth2ProfileContainer();

		if( !profileContainer.getOAuth2ProfileList().isEmpty() && O_AUTH_2.toString().equals( request.getAuthType() ) )
		{
			OAuth2Profile profile = profileContainer.getOAuth2ProfileList().get( 0 );
			if( StringUtils.isNullOrEmpty( profile.getAccessToken() ) )
			{
				return;
			}
			OAuth2ClientFacade oAuth2Client = getOAuth2ClientFacade();

			if( accessTokenIsExpired( profile ) )
			{
				reloadAccessToken( profile, oAuth2Client );
			}
			oAuth2Client.applyAccessToken( profile, httpMethod, request.getRequestContent() );
		}
	}

	protected OAuth2ClientFacade getOAuth2ClientFacade()
	{
		return new OltuOAuth2ClientFacade();
	}

	private boolean accessTokenIsExpired( OAuth2Profile profile )
	{
		long currentTime = TimeUtils.getCurrentTimeInSeconds();
		long issuedTime = profile.getAccessTokenIssuedTime();
		long expirationTime = profile.getAccessTokenExpirationTime();

		return !( issuedTime <= 0 || expirationTime <= 0 ) && expirationTime < currentTime - issuedTime;

	}

	private void reloadAccessToken( OAuth2Profile profile, OAuth2ClientFacade oAuth2Client )
	{
		try
		{
			if( profile.getRefreshToken() != null )
			{
				log.info( "The access token has expired, trying to refresh it." );
				oAuth2Client.refreshAccessToken( profile );
				log.info( "The access token has been refreshed successfully." );
			}
			else
			{
				if( profile.hasAutomationJavaScripts() )
				{
					log.info( "The access token has expired, trying to retrieve a new one with JavaScript automation." );
					oAuth2Client.requestAccessToken( profile );
					log.info( "A new access token has been retrieved successfully." );
				}
				else
				{
					log.warn( "No automation JavaScripts added to OAuth2 profile – cannot retrieve new access token" );
				}
			}
		}
		catch( Exception e )
		{
			//Propagate it up so that it is shown as a failure message in test case log
			throw new RuntimeException( "Unable to refresh expired access token.", e );
		}
	}
}
