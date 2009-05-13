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

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.apache.log4j.spi.LoggingEvent;

/**
 * JTabbedPane that displays Log4J output in different tabs
 * 
 * @author Ole.Matzura
 */

public class TabbedLog4JMonitor extends JTabbedPane implements Log4JMonitor
{
	private JLogList defaultLogArea;

	public TabbedLog4JMonitor()
	{
		super( JTabbedPane.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT );
	}

	public JLogList addLogArea( String title, String loggerName, boolean isDefault )
	{
		JLogList logArea = new JLogList( title );
		logArea.addLogger( loggerName, !isDefault );
		addTab( title, logArea );

		if( isDefault )
			defaultLogArea = logArea;

		return logArea;
	}

	public void logEvent( Object msg )
	{
		if( msg instanceof LoggingEvent )
		{
			LoggingEvent event = ( LoggingEvent )msg;
			String loggerName = event.getLoggerName();

			for( int c = 0; c < getTabCount(); c++ )
			{
				Component tabComponent = getComponentAt( c );
				if( tabComponent instanceof JLogList )
				{
					JLogList logArea = ( JLogList )tabComponent;
					if( logArea.monitors( loggerName ) )
					{
						logArea.addLine( msg );
					}
				}
			}
		}
		else if( defaultLogArea != null )
		{
			defaultLogArea.addLine( msg );
		}
	}

	public JLogList getLogArea( String title )
	{
		int ix = indexOfTab( title );
		return ( JLogList )( ix == -1 ? null : getComponentAt( ix ) );
	}

	public boolean hasLogArea( String loggerName )
	{
		for( int c = 0; c < getTabCount(); c++ )
		{
			Component tabComponent = getComponentAt( c );
			if( tabComponent instanceof JLogList )
			{
				JLogList logArea = ( JLogList )tabComponent;
				if( logArea.monitors( loggerName ) )
				{
					return true;
				}
			}
		}

		return false;
	}

	public JComponent getComponent()
	{
		return this;
	}

	public JLogList getCurrentLog()
	{
		int ix = getSelectedIndex();
		return ix == -1 ? null : getLogArea( getTitleAt( ix ) );
	}

	public void setCurrentLog( JLogList lastLog )
	{
		for( int c = 0; c < getTabCount(); c++ )
		{
			Component tabComponent = getComponentAt( c );
			if( tabComponent == lastLog )
			{
				setSelectedComponent( tabComponent );
			}
		}
	}

	public boolean removeLogArea( String loggerName )
	{
		for( int c = 0; c < getTabCount(); c++ )
		{
			JLogList tabComponent = ( JLogList )getComponentAt( c );
			if( tabComponent.getLogger( loggerName ) != null )
			{
				removeTabAt( c );
				return true;
			}
		}

		return false;
	}
}
