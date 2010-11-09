/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.log;

import javax.swing.AbstractListModel;

import com.eviware.soapui.security.SecurityTest;

/**
 * SecurityTestLog
 * 
 * @author soapUI team
 */
public class SecurityTestLog extends AbstractListModel implements Runnable
{
	public SecurityTestLog( SecurityTest securityTest )
	{

	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object getElementAt( int arg0 )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSize()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public void addEntry( SecurityTestLogMessageEntry securityTestLogMessageEntry )
	{
		// TODO Auto-generated method stub
		
	}

}
