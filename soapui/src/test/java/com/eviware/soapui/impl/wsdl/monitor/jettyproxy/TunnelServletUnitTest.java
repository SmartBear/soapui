/*
 *  soapUI, copyright (C) 2004-2013 smartbear.com
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.monitor.jettyproxy;

import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitor;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import org.apache.http.HttpVersion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TunnelServletUnitTest
{
	private SoapMonitor monitor;

	@Before
	public void setUp()
	{
		monitor = Mockito.mock( SoapMonitor.class );
		WsdlProject project = Mockito.mock( WsdlProject.class );
		XmlBeansSettingsImpl settings = Mockito.mock( XmlBeansSettingsImpl.class );
		Mockito.when( project.getSettings() ).thenReturn( settings );
		Mockito.when( monitor.getProject() ).thenReturn( project );
	}

	@Test
	public void shouldSetHttpProtocolVersion_1_1_ToRequest(){
		ExtendedPostMethod extendedPostMethod = new ExtendedPostMethod(  );
		String sslEndPoint = "Dummy End point";
		TunnelServlet tunnelServlet =  new TunnelServlet( monitor, sslEndPoint );
		tunnelServlet.setProtocolversion( extendedPostMethod, HttpVersion.HTTP_1_1.toString() );
		Assert.assertEquals( "The copied version doesn't match with the provided version string",
				HttpVersion.HTTP_1_1, extendedPostMethod.getProtocolVersion() );
	}

	@Test
	public void shouldSetHttpProtocolVersion_1_0_ToRequest(){
		ExtendedPostMethod extendedPostMethod = new ExtendedPostMethod(  );
		String sslEndPoint = "Dummy End point";
		TunnelServlet tunnelServlet =  new TunnelServlet( monitor, sslEndPoint );
		tunnelServlet.setProtocolversion( extendedPostMethod, HttpVersion.HTTP_1_0.toString() );
		Assert.assertEquals( "The copied version doesn't match with the provided version string",
				HttpVersion.HTTP_1_0, extendedPostMethod.getProtocolVersion() );
	}

}
