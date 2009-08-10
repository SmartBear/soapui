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

package com.eviware.soapui.support.scripting.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * A Groovy ScriptEngine
 * 
 * @author ole.matzura
 */

public class JsScriptEngine implements SoapUIScriptEngine
{
	private String scriptText;
	private StringToObjectMap properties = new StringToObjectMap();
	private final ClassLoader parentClassLoader;

	public JsScriptEngine( ClassLoader parentClassLoader )
	{
		this.parentClassLoader = parentClassLoader;
	}

	public Object run() throws Exception
	{
		if( StringUtils.isNullOrEmpty( scriptText ) )
			return null;

		Context context = ContextFactory.getGlobal().enterContext();
		context.setApplicationClassLoader( parentClassLoader );

		ScriptableObject scope = context.initStandardObjects();

		try
		{
			for( String name : properties.keySet() )
				ScriptableObject.putProperty( scope, name, Context.javaToJS( properties.get( name ), scope ) );

			Script script = context.compileString( scriptText, "Script", 0, null );

			return script.exec( context, scope );
		}
		finally
		{
			for( String name : properties.keySet() )
				scope.delete( name );

			Context.exit();
		}
	}

	public synchronized void setScript( String scriptText )
	{
		if( scriptText != null && scriptText.equals( this.scriptText ) )
			return;

		this.scriptText = scriptText;
	}

	public void compile() throws Exception
	{
	}

	public void setVariable( String name, Object value )
	{
		properties.put( name, value );
	}

	public void clearVariables()
	{
		properties.clear();
	}

	public void release()
	{
		clearVariables();
	}
}
