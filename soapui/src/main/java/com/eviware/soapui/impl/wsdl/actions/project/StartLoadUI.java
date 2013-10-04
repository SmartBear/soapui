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

package com.eviware.soapui.impl.wsdl.actions.project;

import java.io.File;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.actions.SoapUIPreferencesAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.integration.impl.CajoClient;
import com.eviware.soapui.settings.LoadUISettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class StartLoadUI extends AbstractSoapUIAction<WsdlProject>
{
	private static final String SH = ".sh";
	private static final String BAT = ".bat";
	private static final String COMMAND = ".command";
	private static final String LOADUI_LAUNCH_EXTENSION = ( UISupport.isWindows() ? BAT : UISupport.isMac() ? COMMAND
			: SH );
	private static final String LOADUI = "loadUI";
	public static final String SOAPUI_ACTION_ID = "StartLoadUI";
	public static final String LOADUI_LAUNCH_TITLE = "Launch loadUI";
	public static final String LOADUI_LAUNCH_QUESTION = "For this action you have to launch loadUI. Launch it now?";

	public StartLoadUI()
	{
		super( "Start loadUI", "Start loadUI application" );
	}

	public void perform( WsdlProject project, Object param )
	{
		String loadUIBatPath = getLoadUIPath();
		startLoadUI( loadUIBatPath );
	}

	public static Process launchLoadUI()
	{
		String loadUILaunchPath = getLoadUIPath();
		return startLoadUI( loadUILaunchPath );
	}

	private static Process startLoadUI( String loadUILaunchPath )
	{
		if( CajoClient.getInstance().testConnection() )
		{
			try
			{
				CajoClient.getInstance().invoke( "bringToFront", null );
				return null;
			}
			catch( Exception e )
			{
				SoapUI.log.error( "Error while invoke cajo server in loadui ", e );
			}
		}

		try
		{
			while( !( new File( loadUILaunchPath ) ).exists() )
			{
				UISupport.showInfoMessage( "No loadUI" + LOADUI_LAUNCH_EXTENSION + " in path:\"" + loadUILaunchPath + "\"" );
				if( UISupport.getMainFrame() != null )
				{
					if( SoapUIPreferencesAction.getInstance().show( SoapUIPreferencesAction.LOADUI_SETTINGS ) )
					{
						loadUILaunchPath = getLoadUIPath();
					}
					else
					{
						return null;
					}
				}
			}
			String[] commandsWin = new String[] { "cmd.exe", "/c", LOADUI + LOADUI_LAUNCH_EXTENSION };
			String[] commandsLinux = new String[] { "sh", LOADUI + LOADUI_LAUNCH_EXTENSION };
			String[] commandsMac = new String[] { "sh", LOADUI + LOADUI_LAUNCH_EXTENSION };

			ProcessBuilder pb = new ProcessBuilder( UISupport.isWindows() ? commandsWin : UISupport.isMac() ? commandsMac
					: commandsLinux );
			pb.directory( new File( SoapUI.getSettings().getString( LoadUISettings.LOADUI_PATH, "" ) ) );
			Process p = pb.start();
			return p;
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		return null;
	}

	private static String getLoadUIPath()
	{
		return SoapUI.getSettings().getString( LoadUISettings.LOADUI_PATH, "" ) + File.separator + LOADUI
				+ LOADUI_LAUNCH_EXTENSION;
	}

	public static boolean testCajoConnection()
	{
		return CajoClient.getInstance().testConnection();
	}

}
