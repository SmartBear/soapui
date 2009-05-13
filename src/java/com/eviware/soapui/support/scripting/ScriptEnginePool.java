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

package com.eviware.soapui.support.scripting;

import java.util.Stack;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;

/**
 * A pool of script engines
 * 
 * @author ole.matzura
 */

public class ScriptEnginePool
{
	private Stack<SoapUIScriptEngine> scriptEngines = new Stack<SoapUIScriptEngine>();
	private String script;
	private ModelItem modelItem;
	private int borrowed;

	public ScriptEnginePool( ModelItem modelItem )
	{
		this.modelItem = modelItem;
	}

	public void setScript( String script )
	{
		this.script = script;
	}

	public void returnScriptEngine( SoapUIScriptEngine scriptEngine )
	{
		synchronized( this )
		{
			scriptEngines.push( scriptEngine );
			borrowed-- ;
		}
	}

	public SoapUIScriptEngine getScriptEngine()
	{
		synchronized( this )
		{
			if( scriptEngines.isEmpty() )
				scriptEngines.push( SoapUIScriptEngineRegistry.create( SoapUIScriptEngineRegistry.GROOVY_ID, modelItem ) );

			SoapUIScriptEngine result = scriptEngines.pop();
			if( script != null )
				result.setScript( script );

			borrowed++ ;

			return result;
		}
	}

	public void release()
	{
		int waitcount = 10;

		while( borrowed > 0 && waitcount-- > 0 )
		{
			try
			{
				System.out.println( "Waiting for " + borrowed + " script engines" );
				Thread.sleep( 1000 );
			}
			catch( InterruptedException e )
			{
				SoapUI.logError( e );
			}
		}

		for( SoapUIScriptEngine scriptEngine : scriptEngines )
		{
			scriptEngine.release();
		}

		scriptEngines.clear();

		if( borrowed > 0 )
			System.out.println( "Failed to release " + borrowed + " script engines" );
	}
}