package com.eviware.soapui.utils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.net.URL;
import java.util.Collection;

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

	public static Matcher<Collection> aCollectionWithSize( final int size )
	{
		return new TypeSafeMatcher<Collection>()
		{
			@Override
			public boolean matchesSafely( Collection collection )
			{
				return collection != null && collection.size() == size;
			}

			@Override
			public void describeTo( Description description )
			{
				description.appendText( "a collection with " + size + " elements" );
			}
		};
	}

	public static Matcher<Node> compliantWithSchema(final String schemaPath)
	{
		return new TypeSafeMatcher<Node>()
		{
			@Override
			public boolean matchesSafely( Node node )
			{
				URL schemaURL = CommonMatchers.class.getResource( schemaPath );
				if (schemaURL == null)
				{
					throw new IllegalArgumentException( "No schema found at " + schemaPath );
				}
				SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
				Schema schema = null;
				try
				{
					schema = sf.newSchema( schemaURL );
				}
				catch( SAXException e )
				{
					throw new IllegalArgumentException( "The file at " + schemaURL + " does not contain a valid XML schema", e);
				}
				try
				{
					schema.newValidator().validate( new DOMSource( node ) );
					return true;
				}
				catch( SAXException e )
				{
					return false;
				}
				catch( Exception e )
				{
					throw new RuntimeException( "Unexpected exception", e );
				}
			}

			@Override
			public void describeTo( Description description )
			{
				description.appendText( "an XML node compliant with the XML schema at " + schemaPath );
			}
		};
	}

}
