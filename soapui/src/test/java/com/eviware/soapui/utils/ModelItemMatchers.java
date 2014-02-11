package com.eviware.soapui.utils;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

/**
 * Hamcrest matchers for different SoapUI model items.
 */
public class ModelItemMatchers
{

	public static Matcher<ModelItem> belongsTo(final Project project) {
		return new TypeSafeMatcher<ModelItem>()
		{
			@Override
			public boolean matchesSafely( ModelItem modelItem )
			{
				ModelItem parent;
				while ((parent = modelItem.getParent()) != null) {
					if (parent.equals(project)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public void describeTo( Description description )
			{
				description.appendText("an item in the project " + project);
			}
		};
	}

	public static Matcher<WsdlTestSuite> hasATestCaseNamed( final String testCaseName )
	{
		return new TypeSafeMatcher<WsdlTestSuite>()
		{
			@Override
			public boolean matchesSafely( WsdlTestSuite wsdlTestSuite )
			{
				return wsdlTestSuite.getTestCases().containsKey( testCaseName );
			}

			@Override
			public void describeTo( Description description )
			{
				description.appendText( "a TestSuite with a test case named " + testCaseName );
			}
		};
	}

	public static RestRequestWithParamsMatcher hasARestParameterNamed(String name)
	{
		return new RestRequestWithParamsMatcher( name );
	}

	public static RestRequestParamsMatcher hasParameter( final String parameterName )
	{
		return new RestRequestParamsMatcher( parameterName );
	}
}
