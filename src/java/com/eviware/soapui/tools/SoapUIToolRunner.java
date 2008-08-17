/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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

import java.io.File;

import org.apache.commons.cli.CommandLine;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
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
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.RunnerContext;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.wscompile.WSCompileAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.wsi.WSIAnalyzeAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.wsimport.WSImportAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.xfire.XFireAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.xmlbeans.XmlBeans2Action;
import com.eviware.soapui.model.iface.Interface;

/**
 * Standalone tool-runner used from maven-plugin, can also be used from command-line (see xdocs) or
 * directly from other classes.
 * <p>
 * For standalone usage, set the project file (with setProjectFile) and other desired properties before
 * calling run</p> 
 * 
 * @author Ole.Matzura
 */

public class SoapUIToolRunner extends AbstractSoapUIRunner implements ToolHost, RunnerContext 
{
	private String iface;
	private String tool;

	private RunnerStatus status;
	private String projectPassword;
	public String getProjectPassword() {
		return projectPassword;
	}

	public static String TITLE = "soapUI " + SoapUI.SOAPUI_VERSION + " Tool Runner";
	
	/**
	 * Runs the specified tool in the specified soapUI project file, see soapUI xdocs for details.
	 * 
	 * @param args
	 * @throws Exception
	 */

	public static void main( String [] args) throws Exception
	{
		new SoapUIToolRunner().runFromCommandLine( args );
	}

	/**
	 * Sets the tool(s) to run, can be a comma-seperated list
	 * 
	 * @param tool the tools to run
	 */

	public void setTool(String tool)
	{
		this.tool = tool;
	}

	public void setInterface(String iface)
	{
		this.iface = iface;
	}

	public SoapUIToolRunner()
	{
		super( TITLE );
	}
	
	public SoapUIToolRunner( String title )
	{
		super( title );
	}
	
	public boolean runRunner() throws Exception
	{
		String projectFile = getProjectFile();
		
		if( !new File( projectFile ).exists() )
			throw new Exception( "soapUI project file [" + projectFile + "] not found" );
		
		WsdlProject project = new WsdlProject( projectFile, getProjectPassword() );
		log.info( "Running tools [" + tool + "] for interface [" + iface + "] in project [" + project.getName() + "]" );

		long startTime = System.nanoTime();
		
		for( int c = 0; c < project.getInterfaceCount(); c++ )
		{
			Interface i = project.getInterfaceAt( c );
			if( iface == null || i.getName().equals( iface ))
			{
				runTool( i );
			}
		}
		
		long timeTaken = (System.nanoTime()-startTime)/1000000;
		log.info( "time taken: " + timeTaken + "ms" );
		
		return true;
	}
	
	/**
	 * Runs the configured tool(s) for the specified interface.. needs to be refactored to use
	 * some kind of registry/factory pattern for tools
	 * 
	 * @param iface
	 */
	
	public void runTool( Interface iface )
	{
		AbstractToolsAction<Interface> action = null;
		
		String [] tools = tool.split( "," );
		for( String tool : tools )
		{
			if( tool == null || tool.trim().length() == 0 )
				continue;
			
			if( tool.equals( "axis1" ))
			{
				action = new Axis1XWSDL2JavaAction();
			}
			else if( tool.equals( "axis2" ))
			{
				action = new Axis2WSDL2CodeAction();
			}
			else if( tool.equals( "dotnet" ))
			{
				action = new DotNetWsdlAction();
			}
			else if( tool.equals( "gsoap" ))
			{
				action = new GSoapAction();
			}
			else if( tool.equals( "jaxb" ))
			{
				action = new JaxbXjcAction();
			}
			else if( tool.equals( "wstools" ))
			{
				action = new WSToolsWsdl2JavaAction();
			}
			else if( tool.equals( "wscompile" ))
			{
				action = new WSCompileAction();
			}
			else if( tool.equals( "wsimport" ))
			{
				action = new WSImportAction();
			}
			else if( tool.equals( "wsconsume" ))
			{
				action = new JBossWSConsumeAction();
			}
			else if( tool.equals( "xfire" ))
			{
				action = new XFireAction();
			}
			else if( tool.equals( "cxf" ))
			{
				action = new CXFAction();
			}
			else if( tool.equals( "xmlbeans" ))
			{
				action = new XmlBeans2Action();
			}
			else if( tool.equals( "ora" ))
			{
				action = new OracleWsaGenProxyAction();
			}
			else if( tool.equals( "wsi" ))
			{
				action = new WSIAnalyzeAction();
			}
			
			try
			{
				log.info( "Running tool [" + tool + 
							"] for Interface [" + iface.getName() + "]" );
				action.perform( iface, null );
			}
			catch (Exception e)
			{
				SoapUI.logError( e );
			}
		}
	}

	public void run(ToolRunner runner) throws Exception
	{
		status = RunnerStatus.RUNNING;
		runner.setContext( this );
		runner.run();
	}

	public RunnerStatus getStatus()
	{
		return status;
	}

	public String getTitle()
	{
		return getClass().getSimpleName();
	}

	public void log(String msg)
	{
		System.out.print( msg );
	}

   public void logError( String msg )
   {
      System.err.println( msg );
   }

	public void setStatus(RunnerStatus status)
	{
		this.status = status;
	}

   public void disposeContext()
   {
   }

	@Override
	protected SoapUIOptions initCommandLineOptions()
	{
		SoapUIOptions options = new SoapUIOptions( "toolrunner" );
		options.addOption( "i", true, "Sets the interface" );
		options.addOption( "t", true, "Sets the tool to run" );
		options.addOption( "s", true, "Sets the soapui-settings.xml file to use" );
		options.addOption( "x", true, "Sets project password for decryption if project is encrypted" );
		options.addOption( "v", true, "Sets password for soapui-settings.xml file");
		return options;
	}

	@Override
	protected boolean processCommandLine( CommandLine cmd )
	{
		setTool( cmd.getOptionValue( "t") );
		
		if( cmd.hasOption( "i"))
			setInterface( cmd.getOptionValue( "i" ) );
		
		if( cmd.hasOption( "s"))
			setSettingsFile( getCommandLineOptionSubstSpace( cmd, "s" ));
		
		if( cmd.hasOption( "x" ) ) {
			setProjectPassword( cmd.getOptionValue("x"));
		}
		
		if( cmd.hasOption( "v" ) ) {
			setSoapUISettingsPassword( cmd.getOptionValue("v"));
		}
		
		return true;

	}

	public void setProjectPassword(String projectPassword) {
		this.projectPassword = projectPassword;
	}
}