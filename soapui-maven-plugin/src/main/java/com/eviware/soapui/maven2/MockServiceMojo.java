package com.eviware.soapui.maven2;

/*
 * Copyright 2004-2022 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
*/

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.tools.SoapUIMockServiceRunner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

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
    * @parameter property="soapui.projectFile"    
    * 	default-value="${project.artifactId}-soapui-project.xml"
    */
	
   private String projectFile;
   
   /**
    * The mockservice to run 
    *
    * @parameter property="soapui.mockService"    
    */
	
   private String mockService;
   
   /**
    * The path to listen on
    *
    * @parameter property="soapui.path"    
    */
	
   private String path;
   
   /**
    * The port to listen on
    *
    * @parameter property="soapui.port"    
    */
	
   private String port;
   
   /**
    * Specifies SoapUI settings file to use
    *
    * @parameter property="soapui.settingsFile"    
    */
	
   private String settingsFile;
   
   /**
    * To not wait for input
    *
    * @parameter property="soapui.noBlock"    
    */
	
   private boolean noBlock;
   
   /**
    * Tells Test Runner to skip tests.
    */
   
   private boolean skip;
   
   /**
    * Specifies password for encrypted SoapUI project file
    * 
    * @parameter property="soapui.project.password"
    */
	private String projectPassword;
	
	/**
	 * Specifies password for encrypted soapui-settings file
	 * 
	 * @parameter property="soapui.settingsFile.password"
	 */
	private String settingsPassword;
	
	/**
	 * Specified global property values
	 * 
	 * @parameter property="soapui.globalProperties"
	 */
	
	private String [] globalProperties;

	/**
	 * Specified project property values
	 * 
	 * @parameter property="soapui.projectProperties"
	 */
	
	private String [] projectProperties;
	
	/**
	 * Saves project file after running tests
	 * 
	 * @parameter property="soapui.saveAfterRun"
	 */
	
	private boolean saveAfterRun;
	
	/**
	 * SoapUI Properties.
	 * 
	 * @parameter property="soapuiProperties"
	 */
	private Properties soapuiProperties;

}
