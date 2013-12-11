package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2ClientFacade;
import com.eviware.soapui.impl.rest.actions.oauth.OltuAuth2ClientFacade;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.iface.SubmitContext;

public class OAuth2RequestFilter extends AbstractRequestFilter
{
	@Override
	public void filterRestRequest( SubmitContext context, RestRequestInterface request )
	{
		WsdlProject project = request.getResource().getService().getProject();
		OAuth2Profile profile = project.getOAuth2ProfileContainer().getOAuth2ProfileList().get( 0 );

		OAuth2ClientFacade oAuth2Client = new OltuAuth2ClientFacade();
		oAuth2Client.applyAccessToken( profile, request );

	}

}
