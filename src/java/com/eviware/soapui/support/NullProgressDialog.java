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

package com.eviware.soapui.support;

import com.eviware.soapui.support.swing.SwingWorkerDelegator;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;

public class NullProgressDialog implements XProgressDialog
{
	public void run( Worker worker ) throws Exception
	{
		SwingWorkerDelegator swingWorker = new SwingWorkerDelegator( new NullProgressMonitor(), this, worker );
		swingWorker.start();
		swingWorker.get();
	}

	public void setVisible( boolean visible )
	{
	}

	public void setCancelLabel( String label )
	{
	}

	private final static class NullProgressMonitor implements XProgressMonitor
	{
		public void setProgress( int value, String string )
		{
			System.out.println( "Progress: " + value + " - " + string );
		}
	}
}