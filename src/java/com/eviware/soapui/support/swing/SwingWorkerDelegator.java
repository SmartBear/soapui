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

import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;

/**
 * 
 * @author Lars Høidahl
 */
public class SwingWorkerDelegator extends SwingWorker
{
	private XProgressMonitor monitor;
	private Worker delegate;
	private XProgressDialog dialog;

	/**
	 * Start a thread that will call <code>delegate.construct</code> and then
	 * exit.
	 */
	public SwingWorkerDelegator( XProgressMonitor monitor, XProgressDialog dialog, Worker delegate )
	{
		this.monitor = monitor;
		this.dialog = dialog;
		this.delegate = delegate;
	}

	/**
	 * Compute the value to be returned by the <code>get</code> method.
	 */

	public Object construct()
	{
		return delegate.construct( monitor );
	}

	/**
	 * Called on the event dispatching thread (not on the worker thread) after
	 * the <code>construct</code> method has returned.
	 */
	public void finished()
	{
		delegate.finished();
		if( dialog != null )
			dialog.setVisible( false );
		delegate = null;
		monitor = null;
		dialog = null;
	}
}
