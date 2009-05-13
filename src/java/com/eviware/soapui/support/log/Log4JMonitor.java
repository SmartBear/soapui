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

package com.eviware.soapui.support.log;

import javax.swing.JComponent;

/**
 * JTabbedPane that displays Log4J output in different tabs
 * 
 * @author Ole.Matzura
 */

public interface Log4JMonitor
{
	public JLogList addLogArea( String title, String loggerName, boolean isDefault );

	public void logEvent( Object msg );

	public JLogList getLogArea( String title );

	public boolean hasLogArea( String loggerName );

	public JComponent getComponent();

	public JLogList getCurrentLog();

	public void setCurrentLog( JLogList lastLog );

	public boolean removeLogArea( String loggerName );
}
