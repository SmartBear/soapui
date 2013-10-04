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

package com.eviware.soapui.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

public class WeakPropertyChangeListener implements PropertyChangeListener
{
	WeakReference<?> listenerRef;
	Object src;

	@SuppressWarnings( "unchecked" )
	public WeakPropertyChangeListener( PropertyChangeListener listener, Object src )
	{
		listenerRef = new WeakReference( listener );
		this.src = src;
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		PropertyChangeListener listener = ( PropertyChangeListener )listenerRef.get();
		if( listener == null )
		{
			removeListener();
		}
		else
			listener.propertyChange( evt );
	}

	private void removeListener()
	{
		try
		{
			Method method = src.getClass().getMethod( "removePropertyChangeListener",
					new Class[] { PropertyChangeListener.class } );
			method.invoke( src, new Object[] { this } );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
