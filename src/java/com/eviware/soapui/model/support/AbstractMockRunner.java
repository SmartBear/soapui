package com.eviware.soapui.model.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunner;

public abstract class AbstractMockRunner implements MockRunner
{

	public MockResult dispatchGetRequest(HttpServletRequest request, HttpServletResponse response) throws DispatchException
	{
		throw new DispatchException( "Unsupported HTTP Method: GET" );
	}

	public MockResult dispatchPostRequest(HttpServletRequest request, HttpServletResponse response) throws DispatchException
	{
		throw new DispatchException( "Unsupported HTTP Method: POST" );
	}
	
	public MockResult dispatchHeadRequest(HttpServletRequest request, HttpServletResponse response) throws DispatchException
	{
		throw new DispatchException( "Unsupported HTTP Method: HEAD" );
	}
	
	public MockResult dispatchPutRequest(HttpServletRequest request, HttpServletResponse response) throws DispatchException
	{
		throw new DispatchException( "Unsupported HTTP Method: PUT" );
	}
	
	public MockResult dispatchDeleteRequest(HttpServletRequest request, HttpServletResponse response) throws DispatchException
	{
		throw new DispatchException( "Unsupported HTTP Method: DELETE" );
	}
	
	public MockResult dispatchRequest(HttpServletRequest request, HttpServletResponse response)
			throws DispatchException
	{
		String method = request.getMethod();
		
		if( method.equals( "POST"))
			return dispatchPostRequest(request, response);
		else if( method.equals( "GET"))
			return dispatchGetRequest(request, response);
		else if( method.equals( "HEAD"))
			return dispatchHeadRequest(request, response);
		else if( method.equals( "PUT"))
			return dispatchPutRequest(request, response);
		else if( method.equals( "DELETE"))
			return dispatchDeleteRequest(request, response);
		
		throw new DispatchException( "Unsupported HTTP Method: " + method );
	}
}
