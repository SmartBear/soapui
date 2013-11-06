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

package com.eviware.soapui.monitor;

import java.util.concurrent.TimeUnit;

import org.mortbay.thread.ThreadPool;

import com.eviware.soapui.SoapUI;

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

public final class SoapUIJettyThreadPool implements ThreadPool
{
	@Override
	public boolean dispatch( Runnable arg0 )
	{
		SoapUI.getThreadPool().execute( arg0 );
		return true;
	}

	@Override
	public int getIdleThreads()
	{
		return 0;
	}

	@Override
	public int getThreads()
	{
		return SoapUI.getThreadPool().getActiveCount();
	}

	@Override
	public boolean isLowOnThreads()
	{
		return false;
	}

	@Override
	public void join() throws InterruptedException
	{
		SoapUI.getThreadPool().awaitTermination( 30, TimeUnit.SECONDS );
	}
}
