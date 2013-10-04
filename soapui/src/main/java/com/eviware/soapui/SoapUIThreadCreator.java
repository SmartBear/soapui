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

package com.eviware.soapui;

import java.util.concurrent.ThreadFactory;

/**
 * Creates threads for SoapUI internal usage that have the extensionClassLoader
 * as parent
 * 
 * @author Ole
 */

public class SoapUIThreadCreator implements ThreadFactory
{
	public Thread newThread( Runnable r )
	{
		Thread thread = new Thread( r );
		thread.setContextClassLoader( SoapUI.getSoapUICore().getExtensionClassLoader() );
		return thread;
	}
}
