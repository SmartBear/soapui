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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.support;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Utility for build commandline arguments from dialog input
 * 
 * @author ole.matzura
 */

public class ArgumentBuilder
{
	private static final String SHADOW = "XXXXXX";
	private final StringToStringMap values;
	private List<String> args = new ArrayList<String>();
	/**
	 * List of arguments that needs to be shadowed.
	 */
	private List<String> argsToShadow = new ArrayList<String>();
	private boolean isUnix;

	public ArgumentBuilder( StringToStringMap values )
	{
		this.values = values;
	}

	public List<String> getArgs()
	{
		if( isUnix )
		{
			// sh -c requires all args in one string..
			StringBuffer buf = new StringBuffer();
			for( int c = 2; c < args.size(); c++ )
			{
				if( c > 2 )
					buf.append( ' ' );
				buf.append( escapeUnixArg( args.get( c ) ) );
			}

			ArrayList<String> result = new ArrayList<String>();
			result.add( args.get( 0 ) );
			result.add( args.get( 1 ) );
			result.add( buf.toString() );

			return result;
		}
		else
		{
			return new ArrayList<String>( args );
		}
	}

	private String escapeUnixArg( String str )
	{
		if( str == null )
			return "";

		StringBuffer buf = new StringBuffer();

		for( int c = 0; c < str.length(); c++ )
		{
			char ch = str.charAt( c );
			switch( ch )
			{
			case ' ' :
				buf.append( "%20" );
				break;
			default :
				buf.append( ch );
			}
		}

		return buf.toString();
	}

	public boolean addString( String name, String arg )
	{
		if( !values.containsKey( name ) )
			return false;

		String value = values.get( name ).toString();
		if( value == null || value.length() == 0 )
			return false;

		if( arg != null )
			args.add( arg );

		args.add( value );

		return true;
	}

	public boolean addStringShadow( String name, String arg )
	{
		if( !values.containsKey( name ) )
			return false;

		String value = values.get( name ).toString();
		if( value == null || value.length() == 0 )
		{
			return false;
		}

		if( arg != null )
		{
			args.add( arg );
		}

		args.add( value );
		argsToShadow.add( value );

		return true;
	}

	public ArgumentBuilder addArgs( String... args )
	{
		for( int c = 0; c < args.length; c++ )
			this.args.add( args[c] );

		return this;
	}

	public boolean addBoolean( String name, String arg )
	{
		if( values.containsKey( name ) && Boolean.valueOf( values.get( name ).toString() ) )
		{
			args.add( arg );
			return true;
		}

		return false;
	}

	/**
	 * Arguments that are added
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		for( int cnt = 0; cnt < args.size(); cnt++ )
		{
			if( cnt > 0 )
			{
				buf.append( ' ' );
			}

			String value = args.get( cnt );
			for( String argToShadow : argsToShadow )
			{
				if( value.equals( argToShadow ) )
				{
					value = SHADOW;
					break;
				}
				if( value.startsWith( argToShadow ) )
				{
					value = argToShadow + SHADOW;
					break;
				}
			}
			buf.append( value );
		}

		return buf.toString();
	}

	public ArgumentBuilder startScript( String script )
	{
		return startScript( script, ".bat", ".sh" );
	}

	public boolean addString( String name, String arg, String separator )
	{
		if( !values.containsKey( name ) )
			return false;

		String value = values.get( name ).toString();
		if( value == null || value.length() == 0 )
			return false;

		args.add( arg + separator + value );

		return true;
	}

	/**
	 * Just remember how argument starts, assumes that value can change.
	 */
	public boolean addStringShadow( String name, String arg, String separator )
	{

		if( !values.containsKey( name ) )
			return false;

		String value = values.get( name ).toString();
		if( value == null || value.length() == 0 )
			return false;

		args.add( arg + separator + value );
		argsToShadow.add( arg + separator );

		return true;
	}

	public boolean addBoolean( String name, String arg, String trueValue, String falseValue )
	{
		if( !values.containsKey( name ) )
			return false;

		args.add( arg );

		if( Boolean.valueOf( values.get( name ).toString() ) )
		{
			args.add( trueValue );
			return true;
		}
		else
		{
			args.add( falseValue );
			return false;
		}
	}

	public ArgumentBuilder startScript( String script, String windowsExt, String unixExt )
	{
		if( UISupport.isWindows() && windowsExt != null )
		{
			addArgs( "cmd.exe", "/C", script + windowsExt );
		}
		else
		{
			isUnix = true;

			if( !script.startsWith( "." ) && !script.startsWith( File.separator ) )
				script = "./" + script;

			addArgs( "sh", "-c", script + unixExt );
		}

		return this;
	}

	public String[] getStringArgs()
	{
		return args.toArray( new String[args.size()] );
	}

}