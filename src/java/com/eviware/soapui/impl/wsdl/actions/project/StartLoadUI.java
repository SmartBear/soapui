/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
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
	private static final String LOADUI = "loadUI";
	public static final String SOAPUI_ACTION_ID = "StartLoadUI";
	public static final String LOADUI_LAUNCH_TITLE = "Launch loadUI";
	public static final String LOADUI_LAUNCH_QUESTION = "For this action you have to launch loadUI. Launch it now?";

	public StartLoadUI()
	{
		super( "Start LoadUI", "Start LoadUI application" );
	}

	public void perform( WsdlProject project, Object param )
	{
		String loadUIBatPath = SoapUI.getSettings().getString( LoadUISettings.LOADUI_PATH, "" ) + File.separator + LOADUI
				+ ( UISupport.isWindows() ? BAT : SH );
		startLoadUI( loadUIBatPath );
	}

	public static void launchLoadUI()
	{
		String loadUIBatPath = SoapUI.getSettings().getString( LoadUISettings.LOADUI_PATH, "" ) + File.separator + LOADUI
				+ ( UISupport.isWindows() ? BAT : SH );
		startLoadUI( loadUIBatPath );
	}

	private static void startLoadUI( String loadUIbatPath )
	{
		if( CajoClient.getInstance().testConnection() )
		{
			try
			{
				CajoClient.getInstance().invoke( "bringToFront", null );
				return;
			}
			catch( Exception e )
			{
				SoapUI.log.error( "Error while invoke cajo server in loadui ", e );
			}
		}

		String extension = ( UISupport.isWindows() ? BAT : SH );
		try
		{
			while( !( new File( loadUIbatPath ) ).exists() )
			{
				UISupport.showInfoMessage( "No loadUI"+extension+" in this path!" );
				if( UISupport.getMainFrame() != null )
				{
					if( SoapUIPreferencesAction.getInstance().show( SoapUIPreferencesAction.LOADUI_SETTINGS ) )
					{
						loadUIbatPath = SoapUI.getSettings().getString( LoadUISettings.LOADUI_PATH, "" ) + File.separator
								+ LOADUI + ( UISupport.isWindows() ? BAT : SH );
					}
					else
					{
						return;
					}
				}
			}
			String[] commandsWin = new String[] { "cmd.exe", "/c", LOADUI + extension };
			String[] commandsLinux = new String[] { "sh", LOADUI + extension };

			ProcessBuilder pb = new ProcessBuilder( UISupport.isWindows() ? commandsWin : commandsLinux );
			pb.directory( new File( SoapUI.getSettings().getString( LoadUISettings.LOADUI_PATH, "" ) ) );
			pb.start();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	public static boolean testCajoConnection()
	{
		return CajoClient.getInstance().testConnection();
	}

}