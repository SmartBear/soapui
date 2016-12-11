package com.eviware.soapui.maven2;

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

//import java.io.File;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.tools.SoapUIToolRunner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.Properties;

/**
 * Runs SoapUI tools
 * 
 * @goal tool
 */

public class ToolMojo extends AbstractMojo
{
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		if (projectFile == null)
		{
			throw new MojoExecutionException("soapui-project-file setting is required");
		}

//		if (!new File(projectFile).exists())
//		{
//			throw new MojoExecutionException("soapui-project-file [" + projectFile + "] is not found");
//		}

		SoapUIToolRunner runner = new SoapUIToolRunner("SoapUI " + SoapUI.SOAPUI_VERSION + " Maven2 Tool Runner");
		runner.setProjectFile(projectFile);

		if (iface != null)
			runner.setInterface(iface);

		if (tool != null)
			runner.setTool(tool);

		if (settingsFile != null)
			runner.setSettingsFile(settingsFile);

		if (projectPassword != null)
			runner.setProjectPassword(projectPassword);

		if (settingsPassword != null)
			runner.setSoapUISettingsPassword(settingsPassword);

		if (outputFolder != null)
			runner.setOutputFolder(outputFolder);

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
			getLog().error(e.toString());
			throw new MojoFailureException(this, "SoapUI Tool(s) failed", e.getMessage());
		}
	}

	/**
	 * The SoapUI project file to test with
	 * 
	 * @parameter expression="${soapui.projectFile}"
	 *            default-value="${project.artifactId}-soapui-project.xml"
	 */

	private String projectFile;

	/**
	 * The tool to run
	 * 
	 * @parameter expression="${soapui.tool}"
	 */

	private String tool;

	/**
	 * The interface to run for
	 * 
	 * @parameter expression="${soapui.iface}"
	 */

	private String iface;

	/**
	 * Specifies SoapUI settings file to use
	 * 
	 * @parameter expression="${soapui.settingsFile}"
	 */

	private String settingsFile;

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
	 * Specifies output forder for report created by runned tool
	 * 
	 * @parameter expression="${soapui.outputFolder}"
	 */
	private String outputFolder;
	
	/**
	 * SoapUI Properties.
	 * 
	 * @parameter expression="${soapuiProperties}"
	 */
	private Properties soapuiProperties;
}
