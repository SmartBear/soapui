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

package com.eviware.soapui.impl.wsdl.teststeps.assertions.recent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings( "serial" )
public class BoundedQueue
{
	private final int MAX_SIZE = 5;

	private LinkedList<String> queue;

	public BoundedQueue()
	{
		this.queue = new LinkedList<String>();
	}

	public void remove( String e )
	{
		this.queue.remove( e );
	}

	public void add( String e )
	{
		if( this.queue.contains( e ) )
		{
			return;
		}

		this.queue.addLast( e );

		if( this.queue.size() > MAX_SIZE )
		{
			this.queue.removeFirst();
		}
	}

	public List<String> getByAlphabeticalOrder()
	{
		List<String> list = new ArrayList<String>( this.queue );
		Collections.sort( list );
		return list;
	}

	public List<String> getByInsertionOrder()
	{
		return new LinkedList<String>( this.queue );
	}
}
