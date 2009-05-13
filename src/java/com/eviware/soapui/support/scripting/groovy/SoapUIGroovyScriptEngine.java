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

package com.eviware.soapui.support.scripting.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import org.codehaus.groovy.control.CompilerConfiguration;

import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;

/**
 * A Groovy ScriptEngine
 * 
 * @author ole.matzura
 */

public class SoapUIGroovyScriptEngine implements SoapUIScriptEngine
{
	private static GroovyClassLoader classLoader;
	private static GroovyShell shell;
	private Binding binding;
	private Script script;
	private String scriptText;
	
	public static void init( ClassLoader parentClassLoader )
	{
		classLoader = new GroovyClassLoader( parentClassLoader );
		CompilerConfiguration config = new CompilerConfiguration();
		config.setDebug( true );
		config.setVerbose( true );
		shell = new GroovyShell( classLoader, new Binding(), config );
	}

	public SoapUIGroovyScriptEngine()
	{
		binding = new Binding();
	}

	public synchronized Object run() throws Exception
	{
		if( StringUtils.isNullOrEmpty( scriptText ) )
			return null;

		if( script == null )
		{
			compile();
		}

		return script.run();
	}

	public synchronized void setScript( String scriptText )
	{
		if( scriptText != null && scriptText.equals( this.scriptText ) )
			return;

		if( script != null )
		{
			script.setBinding( null );
			script = null;
		}

		this.scriptText = scriptText;
	}

	public void compile() throws Exception
	{
		if( script == null )
		{
			script = shell.parse( scriptText );
			script.setBinding( binding );
		}
	}

	public void setVariable( String name, Object value )
	{
		binding.setVariable( name, value );
	}

	public void clearVariables()
	{
		if( binding != null )
			binding.getVariables().clear();
	}

	public void release()
	{
		if( script != null )
		{
			script = null;
			shell.resetLoadedClasses();
			classLoader.clearCache();
		}

		if( binding != null )
		{
			binding.getVariables().clear();
			binding = null;
		}
	}

	protected Binding getBinding()
	{
		return binding;
	}

	protected GroovyClassLoader getClassLoader()
	{
		return classLoader;
	}

	protected synchronized void reset()
	{
		script = null;
	}

	protected Script getScript()
	{
		return script;
	}

	protected String getScriptText()
	{
		return scriptText;
	}

	protected GroovyShell getShell()
	{
		return shell;
	}
}
