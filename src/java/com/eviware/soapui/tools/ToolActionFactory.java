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
package com.eviware.soapui.tools;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.eviware.soapui.impl.wsdl.actions.iface.tools.axis1.Axis1XWSDL2JavaAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.axis2.Axis2WSDL2CodeAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.cxf.CXFAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.dotnet.DotNetWsdlAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.gsoap.GSoapAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.jaxb.JaxbXjcAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.jbossws.JBossWSConsumeAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.jbossws.WSToolsWsdl2JavaAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.oracle.OracleWsaGenProxyAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.wscompile.WSCompileAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.wsi.WSIAnalyzeAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.wsimport.WSImportAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.xfire.XFireAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.xmlbeans.XmlBeans2Action;
import com.eviware.soapui.model.iface.Interface;

/**
 * Factory class used to create a tool action instances based on action's
 * logical human-readable name.
 * 
 * @author <a href="mailto:nenadn@eviware.com">Nenad V. Nikolic</a>
 */
@SuppressWarnings( "unchecked" )
public class ToolActionFactory
{

	protected static final Logger log = Logger.getLogger( ToolActionFactory.class );
	private static Hashtable<String, Class> toolActionTypeMap;

	static
	{
		toolActionTypeMap = new Hashtable<String, Class>();
		toolActionTypeMap.put( "axis1", Axis1XWSDL2JavaAction.class );
		toolActionTypeMap.put( "axis2", Axis2WSDL2CodeAction.class );
		toolActionTypeMap.put( "dotnet", DotNetWsdlAction.class );
		toolActionTypeMap.put( "gsoap", GSoapAction.class );
		toolActionTypeMap.put( "jaxb", JaxbXjcAction.class );
		toolActionTypeMap.put( "wstools", WSToolsWsdl2JavaAction.class );
		toolActionTypeMap.put( "wscompile", WSCompileAction.class );
		toolActionTypeMap.put( "wsimport", WSImportAction.class );
		toolActionTypeMap.put( "wsconsume", JBossWSConsumeAction.class );
		toolActionTypeMap.put( "xfire", XFireAction.class );
		toolActionTypeMap.put( "cxf", CXFAction.class );
		toolActionTypeMap.put( "xmlbeans", XmlBeans2Action.class );
		toolActionTypeMap.put( "ora", OracleWsaGenProxyAction.class );
		toolActionTypeMap.put( "wsi", WSIAnalyzeAction.class );
	}

	public static AbstractToolsAction<Interface> createToolAction( String toolName )
	{

		Class toolActionType = toolActionTypeMap.get( toolName );
		AbstractToolsAction<Interface> toolActionObject = null;

		if( toolActionType == null )
		{
			return null;
		}
		try
		{
			toolActionObject = ( AbstractToolsAction<Interface> )toolActionType.newInstance();
		}
		catch( IllegalAccessException e )
		{
			log.error( "Constructor is not accessible." );
			log.error( "Check your source code." );
		}
		catch( InstantiationException ie )
		{
			log.error( "Could not instantiate " + toolActionType + " for some reason." );
			log.error( "Check your source code." );
		}
		catch( Exception e )
		{
			log.error( "Some error while instantiating " + toolActionType + " occurred." );
			log.error( "Check your source code." );
		}
		return toolActionObject;
	}
}
