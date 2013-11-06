/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Matcher;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.GroovyUtils;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;

public class JdbcUtils
{

	public static final String PASS_TEMPLATE = "PASS_VALUE";

	public static Connection initConnection( PropertyExpansionContext context, String driver, String connectionString,
			String password ) throws SQLException, SoapUIException
	{
		if( JdbcUtils.missingConnSettings( driver, connectionString, password ) )
		{
			throw new SoapUIException( "Some connections settings are missing" );
		}
		String drvr = PropertyExpander.expandProperties( context, driver ).trim();
		String connStr = PropertyExpander.expandProperties( context, connectionString ).trim();
		String pass = StringUtils.hasContent( password ) ? PropertyExpander.expandProperties( context, password ).trim()
				: "";
		String masskedPass = connStr.replace( PASS_TEMPLATE, "#####" );
		if( connStr.contains( PASS_TEMPLATE ) )
		{
			pass = Matcher.quoteReplacement( pass );
			connStr = connStr.replaceFirst( PASS_TEMPLATE, pass );
		}
		try
		{
			GroovyUtils.registerJdbcDriver( drvr );
			DriverManager.getDriver( connStr );
		}
		catch( SQLException e )
		{
			// SoapUI.logError( e );
			try
			{
				Class.forName( drvr ).newInstance();
			}
			catch( Exception e1 )
			{
				SoapUI.logError( e );
				throw new SoapUIException( "Failed to init connection for drvr [" + drvr + "], connectionString ["
						+ masskedPass + "]" );
			}
		}
		return DriverManager.getConnection( connStr );

	}

	public static boolean hasMasskedPass( String connStr )
	{
		return !StringUtils.isNullOrEmpty( connStr ) ? connStr.contains( PASS_TEMPLATE ) : false;
	}

	public static boolean missingConnSettings( String driver, String connectionString, String password )
	{
		return StringUtils.isNullOrEmpty( driver ) || StringUtils.isNullOrEmpty( connectionString )
				|| ( connectionString.contains( PASS_TEMPLATE ) && StringUtils.isNullOrEmpty( password ) );
	}

}
