/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.eviware.soapui.support.types.StringList;

public class StringUtils
{
	public static final String NEWLINE = System.getProperty( "line.separator" );

	public static String unquote( String str )
	{
		int length = str == null ? -1 : str.length();
		if( str == null || length == 0 )
			return str;

		if( length > 1 && str.charAt( 0 ) == '\"' && str.charAt( length - 1 ) == '\"' )
		{
			str = str.substring( 1, length - 1 );
		}

		return str;
	}

	public static boolean isNullOrEmpty( String str )
	{
		return str == null || str.length() == 0 || str.trim().length() == 0;
	}

	public static int parseInt( String str, int defaultValue )
	{
		if( isNullOrEmpty( str ) )
			return defaultValue;

		try
		{
			return Integer.parseInt( str );
		}
		catch( NumberFormatException e )
		{
			return defaultValue;
		}
	}

	public static List<String> splitLines( String string )
	{
		try
		{
			ArrayList<String> list = new ArrayList<String>();

			LineNumberReader reader = new LineNumberReader( new StringReader( string ) );
			String s;
			while( ( s = reader.readLine() ) != null )
			{
				list.add( s );
			}
			return list;
		}
		catch( IOException e )
		{
			// I don't think this can really happen with a StringReader.
			throw new RuntimeException( e );
		}
	}

	public static String normalizeSpace( String str )
	{
		if( !isNullOrEmpty( str ) )
		{
			StringTokenizer st = new StringTokenizer( str );
			if( st.hasMoreTokens() )
			{

				StringBuffer sb = new StringBuffer( str.length() );
				while( true )
				{
					sb.append( st.nextToken() );
					if( st.hasMoreTokens() )
					{
						sb.append( ' ' );
					}
					else
					{
						break;
					}
				}
				return sb.toString();
			}
			else
			{
				return "";
			}
		}
		else
		{
			return str;
		}
	}

	public static boolean hasContent( String str )
	{
		return str != null && str.trim().length() > 0;
	}

	public static String stripStartAndEnd( String s, String start, String end )
	{
		if( s.startsWith( start ) && s.endsWith( end ) )
			return s.substring( start.length(), s.length() - end.length() );
		else
			return s;
	}

	public static Writer createSeparatedRow( Writer writer, StringList values, char separator, char quote )
			throws IOException
	{
		for( int c = 0; c < values.size(); c++ )
		{
			String value = values.get( c );

			if( c > 0 )
				writer.append( separator );

			if( quote > 0 )
			{
				writer.append( quote );

				if( value != null )
				{
					for( int i = 0; i < value.length(); i++ )
					{
						char ch = value.charAt( i );

						if( ch == quote )
							writer.append( '\\' );
						else if( ch == '\\' )
							writer.append( '\\' );

						writer.append( ch );
					}
				}

				writer.append( quote );
			}
			else if( value != null )
			{
				writer.append( value );
			}
		}

		return writer;
	}

	public static StringList readSeparatedRow( String row, char separator, char quote )
	{
		StringList result = new StringList();

		while( row != null && row.length() > 0 )
		{
			if( row.startsWith( String.valueOf( quote ) ) )
			{
				StringBuffer buf = new StringBuffer();
				char last = row.charAt( 0 );
				int ix = 1;
				while( ix < row.length() )
				{
					char ch = row.charAt( ix );
					if( ( quote == 0 || ch == quote ) && last != '\\' )
					{
						result.add( buf.toString() );
						row = row.length() > ix + 1 ? row.substring( ix + 1 ) : null;
						if( row != null && row.length() > 1 && row.charAt( 0 ) == separator )
						{
							row = row.substring( 1 );
							ix = -1;
						}
						break;
					}
					else if( ch != '\\' || last == '\\' )
					{
						buf.append( ch );
					}

					last = ch;
					ix++ ;
				}

				if( row != null && ix == row.length() )
				{
					result.add( row );
					row = null;
				}
			}
			else
			{
				int ix = row.indexOf( separator );
				if( ix == -1 )
				{
					result.add( row );
					row = null;
				}
				else
				{
					result.add( row.substring( 0, ix ) );
					row = row.substring( ix + 1 );
				}
			}
		}

		return result;
	}

	public static String createFileName( String str, char whitespaceChar )
	{
		StringBuffer result = new StringBuffer();

		for( int c = 0; c < str.length(); c++ )
		{
			char ch = str.charAt( c );

			if( Character.isWhitespace( ch ) && whitespaceChar != 0 )
				result.append( whitespaceChar );
			else if( Character.isLetterOrDigit( ch ) )
				result.append( ch );
		}

		return result.toString();
	}

	public static String[] merge( String[] incomingNames, String string )
	{
		StringList result = new StringList( incomingNames );
		result.add( string );
		return result.toStringArray();
	}

	public static String quote( String str )
	{
		if( str == null )
			return str;

		if( str.length() < 2 || !str.startsWith( "\"" ) || !str.endsWith( "\"" ) )
			str = "\"" + str + "\"";

		return str;
	}

	public static String join( String[] array, String separator )
	{
		StringBuffer buf = new StringBuffer();
		for( int i = 0; i < array.length; i++ )
		{
			if( i > 0 )
				buf.append( separator );
			buf.append( array[i] );
		}
		return buf.toString();
	}

	public static String toHtml( String string )
	{
		if( StringUtils.isNullOrEmpty( string ) )
			return "<html><body></body></html>";

		BufferedReader st = new BufferedReader( new StringReader( string ) );
		StringBuffer buf = new StringBuffer( "<html><body>" );

		try
		{
			String str = st.readLine();

			while( str != null )
			{
				if( str.equalsIgnoreCase( "<br/>" ) )
				{
					str = "<br>";
				}

				buf.append( str );

				if( !str.equalsIgnoreCase( "<br>" ) )
				{
					buf.append( "<br>" );
				}

				str = st.readLine();
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		buf.append( "</body></html>" );
		string = buf.toString();
		return string;
	}

	public static String replace( String data, String from, String to )
	{
		StringBuffer buf = new StringBuffer( data.length() );
		int pos = -1;
		int i = 0;
		while( ( pos = data.indexOf( from, i ) ) != -1 )
		{
			buf.append( data.substring( i, pos ) ).append( to );
			i = pos + from.length();
		}
		buf.append( data.substring( i ) );
		return buf.toString();
	}

	public static String fixLineSeparator( String xml ) throws UnsupportedEncodingException
	{
		if( "\r\n".equals( System.getProperty( "line.separator" ) ) )
		{
			xml = xml.replaceAll( "\r[^\n]", System.getProperty( "line.separator" ) );
		}
		else
		{
			xml = xml.replaceAll( "\r\n", System.getProperty( "line.separator" ) );
		}

		return xml;
	}

}
