package com.eviware.soapui.utils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.awt.Component;

/**
 * Contains factory methods for Hamcrest matchers to be applied on Swing and AWT components.
 */
public class SwingMatchers
{

	public static Matcher<Component> enabled() {
		 return new TypeSafeMatcher<Component>()
		 {
			 @Override
			 public boolean matchesSafely( Component component )
			 {
				 return component.isEnabled();
			 }

			 @Override
			 public void describeTo( Description description )
			 {
				 description.appendText( "an enabled component");
			 }
		 };
	}
}
