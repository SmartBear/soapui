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
package com.eviware.soapui.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.UISupport;
/**
 *  this class represents toolbar button for starting HermesJMS
 * @author nebojsa.tasic
 *
 */
public class StartHermesJMSButtonAction extends AbstractAction
{
	public StartHermesJMSButtonAction()
	{
		putValue(Action.SMALL_ICON, UISupport.createImageIcon("/hermes-16x16.gif"));
		putValue(Action.SHORT_DESCRIPTION, "Start HermesJMS application");
	}

	public void actionPerformed(ActionEvent e)
	{
		try
		{
			String hermesHome =SoapUI.getSettings().getString(ToolsSettings.HERMES_1_13, "");
			if("".equals(hermesHome)){
				UISupport.showErrorMessage("Please set Hermes 1.13 path in Preferences->Tools ! ");
				return;
			}
			String hermesBatPath =hermesHome  + File.separator + "bin"+ File.separator + "hermes.bat";
			Runtime.getRuntime().exec(hermesBatPath);
		}
		catch (Throwable t)
		{
			SoapUI.logError(t);
		}
	}
}