package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.oauth.OltuAuth2ClientFacade;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.OAuth2RequestFacade;
import com.eviware.soapui.model.iface.SubmitContext;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

public class OAuth2RequestFilter extends AbstractRequestFilter
{
	@Override
	public void filterRestRequest( SubmitContext context, RestRequestInterface request )
	{
		WsdlProject project = request.getResource().getService().getProject();
		OAuth2Profile profile = project.getOAuth2ProfileContainer().getOAuth2ProfileList().get( 0 );

		OAuth2RequestFacade oAuth2Request = new OltuAuth2ClientFacade();
		oAuth2Request.applyAccessToken( profile, request );

	}

}
