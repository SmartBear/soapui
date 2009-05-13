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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineFactory;

/**
 * Factory for creating Groovy ScriptEngines
 * 
 * @author ole.matzura
 */

public class GroovyScriptEngineFactory implements SoapUIScriptEngineFactory
{
	public static final String ID = "groovy";

	public SoapUIScriptEngine createScriptEngine( ModelItem modelItem )
	{
		return new SoapUIGroovyScriptEngine(SoapUI.class.getClassLoader() );
	}
}
