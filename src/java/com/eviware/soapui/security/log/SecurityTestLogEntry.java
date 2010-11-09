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

import java.io.IOException;

import javax.swing.ImageIcon;

/**
 * SecurityTestLogEntry
 * 
 * @author soapUI team
 */

public interface SecurityTestLogEntry
{
	public String getMessage();

	public long getTimeStamp();

	public String getType();

	public String getTargetStepName();

//	public ActionList getActions();

	public ImageIcon getIcon();

	public boolean isError();

	public void discard();

	public boolean isDiscarded();

	public void exportToFile( String fileName ) throws IOException;
}
