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

package com.eviware.soapui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.HermesUtils;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.UISupport;

/**
 * this class represents toolbar button for starting HermesJMS
 * 
 * @author nebojsa.tasic
 * 
 */
public class StartHermesJMSButtonAction extends AbstractAction
{
	public StartHermesJMSButtonAction()
	{
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/hermes-16x16.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, "Start HermesJMS application" );
		putValue( Action.NAME, "HermesJMS" );
	}

	public void actionPerformed( ActionEvent e )
	{
		try
		{
			String hermesHome = SoapUI.getSettings().getString( ToolsSettings.HERMES_JMS,
					HermesUtils.defaultHermesJMSPath() );
			if( !isHermesHomeValid( hermesHome ) )
			{
				UISupport.showErrorMessage( "Please set Hermes JMS path in Preferences->Tools ! " );
				if( UISupport.getMainFrame() != null )
				{
					if( SoapUIPreferencesAction.getInstance().show( SoapUIPreferencesAction.INTEGRATED_TOOLS ) )
					{
						hermesHome = SoapUI.getSettings().getString( ToolsSettings.HERMES_JMS,
								HermesUtils.defaultHermesJMSPath() );
					}
				}

			}
			if( !isHermesHomeValid( hermesHome ) )
			{
				return;
			}
			String extension = UISupport.isWindows() ? ".bat" : ".sh";
			String hermesBatPath = hermesHome + File.separator + "bin" + File.separator + "hermes" + extension;
			ProcessBuilder pb = new ProcessBuilder( hermesBatPath );
			Map<String, String> env = pb.environment();
			env.put( "JAVA_HOME", System.getProperty( "java.home" ) );
			pb.start();
		}
		catch( Throwable t )
		{
			SoapUI.logError( t );
		}
	}

	private boolean isHermesHomeValid( String hermesHome )
	{
		File file = new File( hermesHome + File.separator + "bin" + File.separator + "hermes.bat" );
		if( file.exists() )
		{
			return true;
		}
		return false;
	}
}
