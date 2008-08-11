/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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

import org.apache.log4j.spi.LoggingEvent;

import com.eviware.soapui.support.components.Inspector;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;

/**
 * JTabbedPane that displays Log4J output in different tabs
 * 
 * @author Ole.Matzura
 */

public class InspectorLog4JMonitor extends JInspectorPanel implements Log4JMonitor
{
	private JLogList defaultLogArea;

	public InspectorLog4JMonitor( JComponent content )
	{
		super( content );
		
		setResizeWeight( 0.9F );
	}
	
	public JLogList addLogArea( String title, String loggerName, boolean isDefault )
	{
		JLogList logArea = new JLogList( title );
		logArea.addLogger( loggerName, !isDefault );
		JComponentInspector<JLogList> inspector = new JComponentInspector<JLogList>(logArea,  title, null, true);
		addInspector( inspector);
		
		if( isDefault )
		{
			defaultLogArea = logArea;
			activate( inspector );
			setDividerLocation( 500 );
		}
		
		return logArea;
	}

	public void logEvent(Object msg)
	{
		if( msg instanceof LoggingEvent )
		{
			LoggingEvent event = (LoggingEvent) msg;
			String loggerName = event.getLoggerName();
			
			for( Inspector inspector : getInspectors() )
			{
				Component tabComponent = inspector.getComponent();
				if( tabComponent instanceof JLogList )
				{
					JLogList logArea = (JLogList) tabComponent;
					if( logArea.monitors( loggerName ))
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
		Inspector inspector = getInspectorByTitle( title );
		return ( JLogList ) ( title == null ? null : inspector.getComponent() );
	}

	public boolean hasLogArea(String loggerName)
	{
		for( Inspector inspector : getInspectors() )
		{
			Component tabComponent = inspector.getComponent();
			if( tabComponent instanceof JLogList )
			{
				JLogList logArea = (JLogList) tabComponent;
				if( logArea.monitors( loggerName ))
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
	
	public void setCurrentLog( JLogList lastLog )
	{
		for( Inspector inspector : getInspectors() )
		{
			Component tabComponent = inspector.getComponent();
			if( tabComponent == lastLog )
			{
				activate( inspector );
				return;
			}
		}
		
		deactivate();
	}

	public JLogList getCurrentLog()
	{
		return ( JLogList ) ( getCurrentInspector() == null ? null : getCurrentInspector().getComponent() );
	}

	public boolean removeLogArea( String loggerName )
	{
		for( Inspector inspector : getInspectors())
		{
			JLogList logList = ((JLogList)((JComponentInspector<?>)inspector).getComponent());
			if( logList.getLogger( loggerName ) != null )
			{
				logList.removeLogger( loggerName );
				removeInspector( inspector );
				
				return true;
			}
		}
		
		return false;
	}
}
