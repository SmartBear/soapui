/*
 * Copyright 2004-2016 SmartBear Software
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

package com.eviware.soapui.maven2;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.tools.SoapUILoadTestRunner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.Properties;

/**
 * Runs SoapUI loadtests
 * 
 * @goal loadtest
 */

public class LoadTestMojo extends AbstractMojo
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
		
		SoapUILoadTestRunner runner = new SoapUILoadTestRunner( 
					"SoapUI " + SoapUI.SOAPUI_VERSION + " Maven2 LoadTest Runner");
		runner.setProjectFile( projectFile );
		
		if( endpoint != null )
			runner.setEndpoint( endpoint );
		
		if( testSuite != null )
			runner.setTestSuite( testSuite );
		
		if( testCase != null )
			runner.setTestCase( testCase );
		
		if( loadTest != null )
			runner.setLoadTest( loadTest );
		
		if( username != null )
			runner.setUsername( username );
		
		if( password != null )
			runner.setPassword( password );
		
		if( wssPasswordType != null )
			runner.setWssPasswordType( wssPasswordType );
		
		if( domain != null )
			runner.setDomain( domain );
		
		if( limit != null )
			runner.setLimit( limit.intValue() );
		
		if( threadCount != null )
			runner.setThreadCount( threadCount.intValue() );
		
		if( host != null )
			runner.setHost( host );
		
		if( outputFolder != null )
			runner.setOutputFolder( outputFolder );
		
		runner.setPrintReport( printReport );
		runner.setSaveAfterRun( saveAfterRun );

		if( settingsFile != null )
			runner.setSettingsFile( settingsFile );
		
		if ( projectPassword != null )
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
		catch (Throwable e)
		{
			getLog().error( e.toString() );
			throw new MojoFailureException( this, "SoapUI LoadTest(s) failed", e.getMessage() ); 
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
    * The TestSuite to run 
    *
    * @parameter expression="${soapui.testsuite}"    
    */
	
   private String testSuite;
   
   /**
    * The TestCase to run 
    *
    * @parameter expression="${soapui.testcase}"    
    */
	
   private String testCase;
   
   /**
    * The LoadTest to run 
    *
    * @parameter expression="${soapui.loadtest}"    
    */
	
   private String loadTest;
   
   /**
    * The username to use for authentication challenges
    *
    * @parameter expression="${soapui.username}"    
    */
	
   private String username;
   
   /**
    * The password to use for authentication challenges
    *
    * @parameter expression="${soapui.password}"    
    */
	
   private String password;
   
   /**
	 * The WSS password-type to use for any authentications. Setting this will result
	 * in the addition of WS-Security UsernamePassword tokens to any outgoing request containing
	 * the specified username and password. Set to either 'Text' or 'Digest'
	 *  
    * @parameter expression="${soapui.wssPasswordType}"    
	 */
	
   private String wssPasswordType;
   
   /**
    * The domain to use for authentication challenges
    *
    * @parameter expression="${soapui.domain}"    
    */
	
   private String domain;
   
   /**
    * The host to use for requests
    *
    * @parameter expression="${soapui.host}"    
    */
	
   private String host;
   
   /**
    * Overrides the endpoint to use for requests
    *
    * @parameter expression="${soapui.endpoint}"    
    */
	
   private String endpoint;
   
   /**
    * Overrides the LoadTest limit
    *
    * @parameter expression="${soapui.limit}"    
    */
   
   private Integer limit;
   
   /**
    * Overrides the LoadTest threadCount
    *
    * @parameter expression="${soapui.threadcount}"    
    */
   
   private Integer threadCount;
   
   /**
    * Sets the output folder for reports
    *
    * @parameter expression="${soapui.outputFolder}"    
    */
	
   private String outputFolder;
   
   /**
    * Turns on printing of reports
    *
    * @parameter 
    */
	
   private boolean printReport;
   
   /**
    * Specifies SoapUI settings file to use
    *
    * @parameter expression="${soapui.settingsFile}"    
    * */
	
   private String settingsFile;
   
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
