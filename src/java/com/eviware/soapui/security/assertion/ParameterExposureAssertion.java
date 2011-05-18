/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.assertion;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.config.ParameterExposureCheckConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.security.scan.ParameterExposureCheck;
import com.eviware.soapui.support.SecurityScanUtil;

 class ParameterExposureAssertion extends WsdlMessageAssertion implements ResponseAssertion
{
//	public static final String ID = "Parameter Exposure";
//	public static final String LABEL = "Cross Site Scripting Detection";

	public ParameterExposureAssertion( TestAssertionConfig assertionConfig, Assertable assertable )
	{
		super( assertionConfig, assertable, false, true, false, true );
	}

	@Override
	protected String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		ParameterExposureCheckConfig parameterExposureCheckConfig = ( ParameterExposureCheckConfig )context
				.getProperty( ParameterExposureCheck.PARAMETER_EXPOSURE_CHECK_CONFIG );
		boolean throwException = false;
		List<AssertionError> assertionErrorList = new ArrayList<AssertionError>();
		throwException = assertImediateResponse( messageExchange, context, parameterExposureCheckConfig, throwException,
				assertionErrorList );

		if( throwException )
		{
			throw new AssertionException( assertionErrorList.toArray( new AssertionError[assertionErrorList.size()] ) );
		}

		return "OK";
	}

	private boolean assertImediateResponse( MessageExchange messageExchange, SubmitContext context,
			ParameterExposureCheckConfig parameterExposureCheckConfig, boolean throwException,
			List<AssertionError> assertionErrorList )
	{
		for( String value : parameterExposureCheckConfig.getParameterExposureStringsList() )
		{
			value = context.expand( value );// property expansion support
			String match = SecurityScanUtil.contains( context, new String( messageExchange.getRawResponseData() ), value,
					false );
			if( match != null )
			{
				String message = "Content that is sent in request '" + value
						+ "' is exposed in response. Possibility for XSS script attack in: "
						+ messageExchange.getModelItem().getName();
				assertionErrorList.add( new AssertionError( message ) );
				throwException = true;
			}
		}
		return throwException;
	}

//	public static class Factory extends AbstractTestAssertionFactory
//	{
//		public Factory()
//		{
////			super( ParameterExposureAssertion.ID, ParameterExposureAssertion.LABEL, ParameterExposureAssertion.class,
////					ParameterExposureCheck.class );
//
//		}
//
//		@Override
//		public Class<? extends WsdlMessageAssertion> getAssertionClassType()
//		{
//			return ParameterExposureAssertion.class;
//		}
//	}

	@Override
	protected String internalAssertRequest( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		return null;
	}

}
