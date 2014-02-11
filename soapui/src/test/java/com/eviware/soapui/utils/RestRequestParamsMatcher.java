package com.eviware.soapui.utils;

import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

/**
 * @author manne
 */
public class RestRequestParamsMatcher extends TypeSafeMatcher<RestParamsPropertyHolder>
{

	private String parameterName;
	private String parameterValue;

	RestRequestParamsMatcher( String parameterName )
	{
		this.parameterName = parameterName;
	}

	public RestRequestParamsMatcher withValue(String value)
	{
		RestRequestParamsMatcher matcherToReturn = new RestRequestParamsMatcher( parameterName );
		matcherToReturn.parameterValue = value;
		return matcherToReturn;
	}

	@Override
	public boolean matchesSafely( RestParamsPropertyHolder restParameters )
	{
		RestParamProperty property = restParameters.getProperty( parameterName );
		return property != null && ( parameterValue == null || parameterValue.equals( parameterValue ) );
	}

	@Override
	public void describeTo( Description description )
	{
		description.appendText( "a REST requests having a parameter named '" + parameterName + "'" );
		if (parameterValue != null)
		{
			description.appendText( " with the value '" + parameterValue + "'");
		}
	}
}

