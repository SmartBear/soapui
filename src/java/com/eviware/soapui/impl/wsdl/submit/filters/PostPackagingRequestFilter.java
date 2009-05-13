package com.eviware.soapui.impl.wsdl.submit.filters;

import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;

public class PostPackagingRequestFilter extends AbstractRequestFilter
{

	@Override
	public void filterAbstractHttpRequest( SubmitContext context, AbstractHttpRequest<?> request )
	{
		ExtendedHttpMethod httpMethod = ( ExtendedHttpMethod )context.getProperty( BaseHttpRequestTransport.HTTP_METHOD );
		Settings settings = request.getSettings();
		
		// chunking?
		if(httpMethod.getParams().getVersion().equals( HttpVersion.HTTP_1_1 ) && httpMethod instanceof EntityEnclosingMethod) {
			EntityEnclosingMethod entityEnclosingMethod = ( ( EntityEnclosingMethod )httpMethod );
			long limit = settings.getLong( HttpSettings.CHUNKING_THRESHOLD, -1 );
			entityEnclosingMethod.setContentChunked( limit >= 0 ? entityEnclosingMethod.getRequestEntity()
					.getContentLength() > limit : false );
		}
	}

}
