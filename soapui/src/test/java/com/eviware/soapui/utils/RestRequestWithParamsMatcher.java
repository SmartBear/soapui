package com.eviware.soapui.utils;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

/**
* Created with IntelliJ IDEA.
* User: manne
* Date: 9/12/13
* Time: 10:03 PM
* To change this template use File | Settings | File Templates.
*/
public class RestRequestWithParamsMatcher extends TypeSafeMatcher<RestRequest>
{

	private String parameterName;
	private String parameterValue;

	RestRequestWithParamsMatcher( String parameterName )
	{
		this.parameterName = parameterName;
	}

	public RestRequestWithParamsMatcher withValue(String value)
	{
		RestRequestWithParamsMatcher matcherToReturn = new RestRequestWithParamsMatcher( parameterName );
		matcherToReturn.parameterValue = value;
		return matcherToReturn;
	}

	@Override
	public boolean matchesSafely( RestRequest restRequest )
	{
		RestParamProperty property = restRequest.getParams().getProperty( parameterName );
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
