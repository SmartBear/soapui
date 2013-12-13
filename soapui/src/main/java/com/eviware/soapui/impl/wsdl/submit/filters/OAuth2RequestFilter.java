package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2ClientFacade;
import com.eviware.soapui.impl.rest.actions.oauth.OltuOAuth2ClientFacade;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.iface.SubmitContext;

public class OAuth2RequestFilter extends AbstractRequestFilter
{
	@Override
	public void filterRestRequest( SubmitContext context, RestRequestInterface request )
	{
		WsdlProject project = request.getResource().getService().getProject();
		OAuth2ProfileContainer profileContainer = project.getOAuth2ProfileContainer();

		if( !profileContainer.getOAuth2ProfileList().isEmpty() )
		{
			OAuth2Profile profile = profileContainer.getOAuth2ProfileList().get( 0 );
			OAuth2ClientFacade oAuth2Client = new OltuOAuth2ClientFacade();
			oAuth2Client.applyAccessToken( profile, request );
		}



	}

}
