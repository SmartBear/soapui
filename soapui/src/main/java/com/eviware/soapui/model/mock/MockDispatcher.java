package com.eviware.soapui.model.mock;

import com.eviware.soapui.impl.wsdl.mock.DispatchException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface MockDispatcher
{

	public MockResult dispatchRequest( HttpServletRequest request, HttpServletResponse response )
			throws DispatchException;

	public int getMockResultCount();

	public MockResult getMockResultAt( int index );

	public void setLogEnabled( boolean logEnabled );

	public void clearResults();

	public void setMaxResults( long maxNumberOfResults );
}
