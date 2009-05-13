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
package com.eviware.soapui.support.swing;

import javax.swing.SwingUtilities;

import com.eviware.soapui.support.UIUtils;

/**
 * 
 * @author Lars Høidahl
 */
public class SwingUtils implements UIUtils
{
	public void invokeLater( Runnable runnable )
	{
		SwingUtilities.invokeLater( runnable );
	}

	public void invokeAndWait( Runnable runnable ) throws Exception
	{
		SwingUtilities.invokeAndWait( runnable );
	}

	// TODO Change this to run in the UI thread on Swing too, and then rename the
	// function to "runInUIThread".
	public void runInUIThreadIfSWT( Runnable runnable )
	{
		runnable.run();
	}
}
