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

import java.util.HashMap;
import java.util.Map;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.scripting.groovy.GroovyScriptEngineFactory;
import com.eviware.soapui.support.scripting.js.JsScriptEngineFactory;
import com.eviware.soapui.support.types.StringList;

/**
 * Registry of available script engines
 * 
 * @author ole.matzura
 */

public class SoapUIScriptEngineRegistry
{
	public static final String DEFAULT_SCRIPT_ENGINE_ID = GroovyScriptEngineFactory.ID;

	private static Map<String, SoapUIScriptEngineFactory> factories = new HashMap<String, SoapUIScriptEngineFactory>();

	public static void registerScriptEngine( String id, SoapUIScriptEngineFactory factory )
	{
		factories.put( id, factory );
	}

	public static SoapUIScriptEngineFactory getFactory( String id )
	{
		return factories.get( id );
	}

	public static SoapUIScriptEngine create( ModelItem modelItem )
	{
		WsdlProject project = ( WsdlProject )ModelSupport.getModelItemProject( modelItem );

		String scriptEngineId = null;
		if( project == null )
			SoapUI.log.warn( "Project is null" );
		else
			scriptEngineId = project.getDefaultScriptLanguage();

		if( StringUtils.isNullOrEmpty( scriptEngineId ) )
			scriptEngineId = DEFAULT_SCRIPT_ENGINE_ID;

		return factories.get( scriptEngineId ).createScriptEngine( modelItem );
	}

	public static SoapUIScriptGenerator createScriptGenerator( ModelItem modelItem )
	{
		WsdlProject project = ( WsdlProject )ModelSupport.getModelItemProject( modelItem );

		String scriptEngineId = project.getDefaultScriptLanguage();
		if( StringUtils.isNullOrEmpty( scriptEngineId ) )
			scriptEngineId = DEFAULT_SCRIPT_ENGINE_ID;

		return factories.get( scriptEngineId ).createCodeGenerator( modelItem );
	}

	static
	{
		registerScriptEngine( GroovyScriptEngineFactory.ID, new GroovyScriptEngineFactory() );
		registerScriptEngine( JsScriptEngineFactory.ID, new JsScriptEngineFactory() );
	}

	public static String[] getAvailableEngineIds()
	{
		return new StringList( factories.keySet() ).toStringArray();
	}
}
