package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.config.CredentialsConfig;
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

import static com.eviware.soapui.config.CredentialsConfig.AuthType.O_AUTH_2;

public class OAuth2RequestFilter extends AbstractRequestFilter
{
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
			OAuth2ClientFacade oAuth2Client = new OltuOAuth2ClientFacade();

			if( accessTokenIsExpired( profile ) && hasRefreshToken( profile ) )
			{
				refreshAccessToken( profile, oAuth2Client );
			}
			oAuth2Client.applyAccessToken( profile, httpMethod, request.getRequestContent() );
		}
	}

	private boolean accessTokenIsExpired( OAuth2Profile profile )
	{
		//TODO: Null checks

		long currentTime = TimeUtils.getCurrentTimeInSeconds();
		long issuedTime = profile.getAccessTokenIssuedTime();
		long expirationTime = profile.getAccessTokenExpirationTime();

		return expirationTime < currentTime - issuedTime;
	}

	private boolean hasRefreshToken( OAuth2Profile profile )
	{
		return profile.getRefreshToken() != null;
	}

	private void refreshAccessToken( OAuth2Profile profile, OAuth2ClientFacade oAuth2Client )
	{
		try
		{
			oAuth2Client.refreshAccessToken( profile );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			//TODO: Can we do anything here other than just throw?
		}
	}
}
