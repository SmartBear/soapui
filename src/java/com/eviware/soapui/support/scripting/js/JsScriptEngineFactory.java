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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineFactory;
import com.eviware.soapui.support.scripting.SoapUIScriptGenerator;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XPathData;

/**
 * Factory for creating Javascript ScriptEngines
 * 
 * @author ole.matzura
 */

public class JsScriptEngineFactory implements SoapUIScriptEngineFactory, SoapUIScriptGenerator
{
	public static final String ID = "Javascript";

	public SoapUIScriptEngine createScriptEngine( ModelItem modelItem )
	{
		return new JsScriptEngine( SoapUI.class.getClassLoader() );
	}

	public SoapUIScriptGenerator createCodeGenerator( ModelItem modelItem )
	{
		return this;
	}

	public String createContextExpansion( String name, PropertyExpansion expansion )
	{
		return "var " + name + " = context.expand( \"" + expansion + "\" );\n";
	}

	public String createScriptAssertionForExists( XPathData xpathData )
	{
		String script = "var holder = new com.eviware.soapui.support.XmlHolder( messageExchange.responseContentAsXml );\n";

		StringToStringMap nsMap = xpathData.getNamespaceMap();
		for( String ns : nsMap.keySet() )
		{
			script += "holder.namespaces.put(\"" + nsMap.get( ns ) + "\", \"" + ns + "\" );\n";
		}

		script += "var node = holder.getDomNode( \"" + xpathData.getPath() + "\" );\n";
		script += "if( node == null )\n   throw new java.lang.Exception( \"Missing node\" );\n";

		return script;
	}
}
