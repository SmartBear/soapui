package com.eviware.soapui.maven2;

/*
 *  SoapUI, copyright (C) 2004-2011 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of the GNU Lesser General Public License as published by the Free Software Foundation; 
 *  either version 2.1 of the License, or (at your option) any later version.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.tools.SoapUIMockServiceRunner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.Properties;

/**
 * Runs SoapUI mockservice
 * 
 * @goal mock
 */

public class MockServiceMojo extends AbstractMojo
{
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		if( skip || System.getProperty( "maven.test.skip", "false" ).equals( "true") )
			return;
		
		if( projectFile == null )
		{
			throw new MojoExecutionException("soapui-project-file setting is required" );
		}

//		if( !new File(projectFile).exists() )
//		{
//			throw new MojoExecutionException("soapui-project-file [" + projectFile + "] is not found" );
//		}
		
		SoapUIMockServiceRunner runner = new SoapUIMockServiceRunner(
					"SoapUI " + SoapUI.SOAPUI_VERSION + " Maven2 MockService Runner");
		runner.setProjectFile( projectFile );
		
		
		if( mockService != null )
			runner.setMockService( mockService );
		
		if( path != null )
			runner.setPath( path );
		
		if( port != null )
			runner.setPort( port );
		
		if( settingsFile != null )
			runner.setSettingsFile( settingsFile );
		
		runner.setBlock( !noBlock );
		runner.setSaveAfterRun( saveAfterRun );

		if( projectPassword != null )
			runner.setProjectPassword(projectPassword);
		
		if ( settingsPassword != null ) 
			runner.setSoapUISettingsPassword(settingsPassword);
		
		if( globalProperties != null )
			runner.setGlobalProperties(globalProperties);
		
		if( projectProperties != null )
			runner.setProjectProperties(projectProperties);
		
		if( soapuiProperties != null && soapuiProperties.size() > 0 )
			for( Object key : soapuiProperties.keySet() )
			{
				System.out.println( "Setting " + ( String )key + " value " + soapuiProperties.getProperty( ( String )key ) );
				System.setProperty( ( String )key, soapuiProperties.getProperty( ( String )key ) );
			}
			
		try
		{
			runner.run();
		}
		catch (Exception e)
		{
			getLog().error( e.toString() );
			throw new MojoFailureException( this, "SoapUI MockService(s) failed", e.getMessage() ); 
		}		
	}
	
	/**
    * The SoapUI project file to test with
    *
    * @parameter expression="${soapui.projectFile}"    
    * 	default-value="${project.artifactId}-soapui-project.xml"
    */
	
   private String projectFile;
   
   /**
    * The mockservice to run 
    *
    * @parameter expression="${soapui.mockService}"    
    */
	
   private String mockService;
   
   /**
    * The path to listen on
    *
    * @parameter expression="${soapui.path}"    
    */
	
   private String path;
   
   /**
    * The port to listen on
    *
    * @parameter expression="${soapui.port}"    
    */
	
   private String port;
   
   /**
    * Specifies SoapUI settings file to use
    *
    * @parameter expression="${soapui.settingsFile}"    
    */
	
   private String settingsFile;
   
   /**
    * To not wait for input
    *
    * @parameter expression="${soapui.noBlock}"    
    */
	
   private boolean noBlock;
   
   /**
    * Tells Test Runner to skip tests.
    */
   
   private boolean skip;
   
   /**
    * Specifies password for encrypted SoapUI project file
    * 
    * @parameter expression="${soapui.project.password}"
    */
	private String projectPassword;
	
	/**
	 * Specifies password for encrypted soapui-settings file
	 * 
	 * @parameter expression="${soapui.settingsFile.password}"
	 */
	private String settingsPassword;
	
	/**
	 * Specified global property values
	 * 
	 * @parameter expression="${soapui.globalProperties}"
	 */
	
	private String [] globalProperties;

	/**
	 * Specified project property values
	 * 
	 * @parameter expression="${soapui.projectProperties}"
	 */
	
	private String [] projectProperties;
	
	/**
	 * Saves project file after running tests
	 * 
	 * @parameter expression="${soapui.saveAfterRun}"
	 */
	
	private boolean saveAfterRun;
	
	/**
	 * SoapUI Properties.
	 * 
	 * @parameter expression="${soapuiProperties}"
	 */
	private Properties soapuiProperties;

}
