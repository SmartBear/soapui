package com.eviware.soapui.utils;

import com.eviware.soapui.impl.rest.RestRequest;
import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

/**
 * A matcher for REST requests with parameters.
 */
public class RestRequestWithParamsMatcher extends TypeSafeMatcher<RestRequest>
{

	private RestRequestParamsMatcher parametersMatcher;
	private String parameterName;


	RestRequestWithParamsMatcher( String parameterName )
	{
		this.parameterName = parameterName;
		this.parametersMatcher = new RestRequestParamsMatcher( parameterName );
	}

	public RestRequestWithParamsMatcher withValue(String value)
	{
		RestRequestWithParamsMatcher matcherToReturn = new RestRequestWithParamsMatcher( parameterName );
		matcherToReturn.parametersMatcher = new RestRequestParamsMatcher( parameterName ).withValue( value );
		return matcherToReturn;
	}

	@Override
	public boolean matchesSafely( RestRequest restRequest )
	{
		return parametersMatcher.matchesSafely( restRequest.getParams() );
	}

	@Override
	public void describeTo( Description description )
	{
		description.appendText( "a REST request with " );
		parametersMatcher.describeTo( description );
	}
}
