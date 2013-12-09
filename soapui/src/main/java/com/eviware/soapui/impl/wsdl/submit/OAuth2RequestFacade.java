package com.eviware.soapui.impl.wsdl.submit;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;

public interface OAuth2RequestFacade
{
	public void applyAccessToken( OAuth2Profile profile, HttpRequestInterface request );
}
