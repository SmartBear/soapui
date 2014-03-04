package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.impl.support.AbstractMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestMockRequest extends AbstractMockRequest
{
	public RestMockRequest( HttpServletRequest request, HttpServletResponse response, WsdlMockRunContext context )
	{
		super( request, response, context );
	}

	@Override
	public XmlObject getContentElement() throws XmlException
	{
		return null;
	}
}
