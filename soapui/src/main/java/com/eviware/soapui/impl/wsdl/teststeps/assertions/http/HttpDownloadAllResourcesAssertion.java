/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.assertions.http;

import java.util.List;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.support.http.HttpRequest;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HTMLPageSourceDownloader;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;

public class HttpDownloadAllResourcesAssertion extends WsdlMessageAssertion implements ResponseAssertion,
		RequestAssertion
{
	public static final String ID = "HTTP Download all resources";
	public static final String LABEL = "HTTP Download all resources";
	public static final String DESCRIPTION = "Downloads all resources referred to be an HTML document (images, scripts, etc.) and validates that they are all available. Applicable to any property containing HTML.";

	public HttpDownloadAllResourcesAssertion( TestAssertionConfig assertionConfig, Assertable assertable )
	{
		super( assertionConfig, assertable, false, false, false, true );
	}

	@Override
	protected String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{

		List<String> missingResourcesList = ( List<String> )context
				.getProperty( HTMLPageSourceDownloader.MISSING_RESOURCES_LIST );
		if( missingResourcesList != null && !missingResourcesList.isEmpty() )
		{
			StringBuilder sb = new StringBuilder( "Missing resources: \n" );
			for( String url : missingResourcesList )
			{
				sb.append( url + "  ;\n" );
			}
			throw new AssertionException( new AssertionError( sb.toString() ) );
		}

		return "HTTP Download all resources OK";
	}

	@Override
	protected String internalAssertProperty( TestPropertyHolder source, String propertyName,
			MessageExchange messageExchange, SubmitContext context ) throws AssertionException
	{
		return "OK";
	}

	@Override
	protected String internalAssertRequest( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		return "HTTP Download all resources OK";
	}

	public static class Factory extends AbstractTestAssertionFactory
	{
		public Factory()
		{
			super( HttpDownloadAllResourcesAssertion.ID, HttpDownloadAllResourcesAssertion.LABEL,
					HttpDownloadAllResourcesAssertion.class, HttpRequest.class );
		}

		@Override
		public Class<? extends WsdlMessageAssertion> getAssertionClassType()
		{
			return HttpDownloadAllResourcesAssertion.class;
		}

		@Override
		public String getCategory()
		{
			//TODO change the category when decided where it really goes
			return AssertionCategoryMapping.STATUS_CATEGORY;
		}

		@Override
		public AssertionListEntry getAssertionListEntry()
		{
			return new AssertionListEntry( HttpDownloadAllResourcesAssertion.ID, HttpDownloadAllResourcesAssertion.LABEL,
					HttpDownloadAllResourcesAssertion.DESCRIPTION );
		}
	}
}
