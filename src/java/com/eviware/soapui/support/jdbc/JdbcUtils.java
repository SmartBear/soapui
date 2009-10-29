package com.eviware.soapui.support.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;

public class JdbcUtils
{

	public static final String PASS_TEMPLATE = "PASS_VALUE";	
	public static Connection testConnection( ModelItem modelItem, String driver, String connectionString, String password )
			throws Exception, SQLException
	{
		PropertyExpansionContext context = new DefaultPropertyExpansionContext( modelItem );
	
		String drvr = PropertyExpander.expandProperties(context, driver).trim();
		String connStr = PropertyExpander.expandProperties(context, connectionString).trim();
		String pass = PropertyExpander.expandProperties(context, password).trim();
		connStr = connStr.replaceFirst(PASS_TEMPLATE, pass);
		try
		{
			DriverManager.getDriver( connStr );
		}
		catch( SQLException e )
		{
			try
			{
				Class.forName( drvr ).newInstance();
			}
			catch( Exception e1 )
			{
				throw new Exception( "Failed to init connection for drvr [" + drvr + "], connectionString ["
						+ connectionString + "]" );
			}
		}
		return DriverManager.getConnection( connStr );
	
	}

}
