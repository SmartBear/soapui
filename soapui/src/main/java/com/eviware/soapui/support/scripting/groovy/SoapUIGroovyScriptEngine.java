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

package com.eviware.soapui.support.scripting.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import org.codehaus.groovy.control.CompilerConfiguration;

import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader.SoapUIClassLoaderState;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;

/**
 * A Groovy ScriptEngine
 * 
 * @author ole.matzura
 */

public class SoapUIGroovyScriptEngine implements SoapUIScriptEngine
{
	private GroovyClassLoader classLoader;
	private GroovyShell shell;
	private Binding binding;
	private Script script;
	private String scriptText;
	protected ScriptSaver saver = new ScriptSaver();

	public SoapUIGroovyScriptEngine( ClassLoader parentClassLoader )
	{
		classLoader = new GroovyClassLoader( parentClassLoader );
		binding = new Binding();
		CompilerConfiguration config = new CompilerConfiguration();
		config.setDebug( true );
		config.setVerbose( true );
		shell = new GroovyShell( classLoader, binding, config );
	}

	protected class ScriptSaver
	{
		private String text = null;
		private boolean locked = false;

		public synchronized void save( String scriptText )
		{
			if( locked )
				text = scriptText;
			else
				synchronizedSetScript( scriptText );
		}

		public synchronized void lockSave()
		{
			locked = true;
		}

		public synchronized void unlockSave()
		{
			if( text != null )
			{
				synchronizedSetScript( text );
				text = null;
			}
			locked = false;
		}
	}

	public synchronized Object run() throws Exception
	{
		saver.lockSave();
		SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
		try
		{

			if( StringUtils.isNullOrEmpty( scriptText ) )
				return null;

			if( script == null )
			{
				compile();
			}

			Object result = script.run();

			return result;
		}
		finally
		{
			state.restore();
			saver.unlockSave();
		}
	}

	protected synchronized void synchronizedSetScript( String scriptText )
	{
		if( scriptText != null && scriptText.equals( this.scriptText ) )
			return;

		if( script != null )
		{
			script.setBinding( null );
			script = null;

			if( shell != null )
				shell.resetLoadedClasses();

			classLoader.clearCache();
		}

		this.scriptText = scriptText;
	}

	public synchronized void setScript( String scriptText )
	{
		if( scriptText != null && !scriptText.equals( this.scriptText ) )
			saver.save( scriptText );
	}

	protected synchronized void reset()
	{
		saver.lockSave();

		script = null;

		saver.unlockSave();
	}

	public synchronized void compile() throws Exception
	{
		if( script == null )
		{
			SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
			try
			{
				script = shell.parse( scriptText );
				script.setBinding( binding );
			}
			finally
			{
				state.restore();
			}
		}
	}

	public synchronized void setVariable( String name, Object value )
	{
		binding.setVariable( name, value );
	}

	public synchronized void clearVariables()
	{
		if( binding != null )
			binding.getVariables().clear();
	}

	public synchronized void release()
	{
		script = null;

		if( binding != null )
		{
			binding.getVariables().clear();
			binding = null;
		}

		if( shell != null )
		{
			shell.resetLoadedClasses();
			shell = null;
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
