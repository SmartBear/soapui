package com.eviware.soapui.utils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

/**
 * Hamcrest matchers for common data types.
 */
public class CommonMatchers
{

	public static Matcher<String> endsWith(final String suffix)
	{
		return new TypeSafeMatcher<String>()
		{
			@Override
			public boolean matchesSafely( String s )
			{
				return s.endsWith( suffix );
			}

			@Override
			public void describeTo( Description description )
			{
				description.appendText( "a string ending with " + suffix );
			}
		};
	}

	public static Matcher<String> startsWith(final String prefix)
	{
		return new TypeSafeMatcher<String>()
		{
			@Override
			public boolean matchesSafely( String s )
			{
				return s.startsWith( prefix );
			}

			@Override
			public void describeTo( Description description )
			{
				description.appendText( "a string starting with " + prefix );
			}
		};
	}

	public static Matcher<Object[]> anEmptyArray()
	{
		return new TypeSafeMatcher<Object[]>()
		{
			@Override
			public boolean matchesSafely( Object[] objects )
			{
				return objects != null && objects.length == 0;
			}

			@Override
			public void describeTo( Description description )
			{
				description.appendText( "an empty array" );
			}
		};
	}

}
