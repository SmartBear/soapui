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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineFactory;
import com.eviware.soapui.support.scripting.SoapUIScriptGenerator;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XPathData;

/**
 * Factory for creating Groovy ScriptEngines
 * 
 * @author ole.matzura
 */

public class GroovyScriptEngineFactory implements SoapUIScriptEngineFactory, SoapUIScriptGenerator
{
	public static final String ID = "Groovy";

	public SoapUIScriptEngine createScriptEngine( ModelItem modelItem )
	{
		return new SoapUIGroovyScriptEngine( SoapUI.getSoapUICore().getExtensionClassLoader() );
	}

	public SoapUIScriptGenerator createCodeGenerator( ModelItem modelItem )
	{
		return this;
	}

	public String createContextExpansion( String name, PropertyExpansion expansion )
	{
		String exp = expansion.toString();
		StringBuffer buf = new StringBuffer();

		for( int c = 0; c < exp.length(); c++ )
		{
			char ch = exp.charAt( c );

			switch( ch )
			{
			case '\'' :
			case '\\' :
				buf.append( '\\' );
			default :
				buf.append( ch );
			}
		}

		return "def " + name + " = context.expand( '" + buf.toString() + "' )\n";
	}

	public String createScriptAssertionForExists( XPathData xpathData )
	{
		String script = "import com.eviware.soapui.support.XmlHolder\n\n"
				+ "def holder = new XmlHolder( messageExchange.responseContentAsXml )\n";

		StringToStringMap nsMap = xpathData.getNamespaceMap();
		for( String ns : nsMap.keySet() )
		{
			script += "holder.namespaces[\"" + nsMap.get( ns ) + "\"] = \"" + ns + "\"\n";
		}

		script += "def node = holder.getDomNode( \"" + xpathData.getPath() + "\" )\n";
		script += "\nassert node != null\n";

		return script;
	}
}
