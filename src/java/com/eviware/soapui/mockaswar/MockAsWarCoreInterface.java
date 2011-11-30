package com.eviware.soapui.mockaswar;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eviware.soapui.impl.wsdl.mock.DispatchException;

interface MockAsWarCoreInterface
{

	public void dispatchRequest( HttpServletRequest request, HttpServletResponse response ) throws DispatchException,
			IOException;

	public void stop();

}