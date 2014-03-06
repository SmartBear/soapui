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

package com.eviware.soapui.support.swing;

import com.eviware.soapui.support.UIUtils;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Lars Höidahl
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

	@Override
	public void invokeAndWaitIfNotInEDT( Runnable runnable )
	{
		if( SwingUtilities.isEventDispatchThread() )
		{
			runnable.run();
		}
		else
		{
			try
			{
				SwingUtilities.invokeAndWait( runnable );
			}
			catch( InterruptedException e )
			{
				throw new RuntimeException( e );
			}
			catch( InvocationTargetException e )
			{
				throw new RuntimeException( e );
			}
		}
	}
}
