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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.concurrent.Future;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.UISupport;

/**
 * Component for displaying log entries
 * 
 * @author Ole.Matzura
 */

public class JLogList extends JPanel
{
	private long maxRows = 1000;
	private JList logList;
	private SimpleAttributeSet requestAttributes;
	private SimpleAttributeSet responseAttributes;
	private LogListModel model;
	private List<Logger> loggers = new ArrayList<Logger>();
	private InternalLogAppender internalLogAppender = new InternalLogAppender();
	private boolean tailing = true;
	private Stack<Object> linesToAdd = new Stack<Object>();
	private EnableAction enableAction;
	private JCheckBoxMenuItem enableMenuItem;
	private final String title;
	private boolean released;
	private Future<?> future;

	public JLogList( String title )
	{
		super( new BorderLayout() );
		this.title = title;

		model = new LogListModel();
		logList = new JList( model );
		logList.setToolTipText( title );
		logList.setCellRenderer( new LogAreaCellRenderer() );
		logList.setPrototypeCellValue( "Testing 123" );
		logList.setFixedCellWidth( -1 );

		JPopupMenu listPopup = new JPopupMenu();
		listPopup.add( new ClearAction() );
		enableAction = new EnableAction();
		enableMenuItem = new JCheckBoxMenuItem( enableAction );
		enableMenuItem.setSelected( true );
		listPopup.add( enableMenuItem );
		listPopup.addSeparator();
		listPopup.add( new CopyAction() );
		listPopup.add( new SetMaxRowsAction() );
		listPopup.addSeparator();
		listPopup.add( new ExportToFileAction() );

		logList.setComponentPopupMenu( listPopup );

		setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		JScrollPane scrollPane = new JScrollPane( logList );
		UISupport.addPreviewCorner( scrollPane, true );
		add( scrollPane, BorderLayout.CENTER );

		requestAttributes = new SimpleAttributeSet();
		StyleConstants.setForeground( requestAttributes, Color.BLUE );

		responseAttributes = new SimpleAttributeSet();
		StyleConstants.setForeground( responseAttributes, Color.GREEN );

		try
		{
			maxRows = Long.parseLong( SoapUI.getSettings().getString( "JLogList#" + title, "1000" ) );
		}
		catch( NumberFormatException e )
		{
		}
	}

	public void clear()
	{
		model.clear();
	}

	public JList getLogList()
	{
		return logList;
	}

	public long getMaxRows()
	{
		return maxRows;
	}

	public void setMaxRows( long maxRows )
	{
		this.maxRows = maxRows;
	}

	public synchronized void addLine( Object line )
	{
		if( !isEnabled() )
			return;

		if( line instanceof LoggingEvent )
		{
			LoggingEvent ev = ( LoggingEvent )line;
			linesToAdd.push( new LoggingEventWrapper( ev ) );

			if( ev.getThrowableInformation() != null )
			{
				Throwable t = ev.getThrowableInformation().getThrowable();
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter( sw );
				t.printStackTrace( pw );
				StringTokenizer st = new StringTokenizer( sw.toString(), "\r\n" );
				while( st.hasMoreElements() )
					linesToAdd.push( "   " + st.nextElement() );
			}
		}
		else
		{
			linesToAdd.push( line );
		}

		if( future == null )
		{
			released = false;
			future = SoapUI.getThreadPool().submit( model );
		}
	}

	public void setEnabled( boolean enabled )
	{
		super.setEnabled( enabled );
		logList.setEnabled( enabled );
		enableMenuItem.setSelected( enabled );
	}

	private static class LogAreaCellRenderer extends DefaultListCellRenderer
	{
		private Map<Level, Color> levelColors = new HashMap<Level, Color>();

		private LogAreaCellRenderer()
		{
			levelColors.put( Level.ERROR, new Color( 192, 0, 0 ) );
			levelColors.put( Level.INFO, new Color( 0, 92, 0 ) );
			levelColors.put( Level.WARN, Color.ORANGE.darker().darker() );
			levelColors.put( Level.DEBUG, new Color( 0, 0, 128 ) );
		}

		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus )
		{
			JLabel component = ( JLabel )super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

			if( value instanceof LoggingEventWrapper )
			{
				LoggingEventWrapper eventWrapper = ( LoggingEventWrapper )value;

				if( levelColors.containsKey( eventWrapper.getLevel() ) )
					component.setForeground( levelColors.get( eventWrapper.getLevel() ) );
			}

			// Limit the length of the tool tip, to prevent long delays.
			String toolTip = component.getText();
			if( toolTip != null && toolTip.length() > 1000 )
				toolTip = toolTip.substring( 0, 1000 );
			component.setToolTipText( toolTip );

			return component;
		}
	}

	private final static class LoggingEventWrapper
	{
		private final LoggingEvent loggingEvent;
		private String str;

		public LoggingEventWrapper( LoggingEvent loggingEvent )
		{
			this.loggingEvent = loggingEvent;
		}

		public Level getLevel()
		{
			return loggingEvent.getLevel();
		}

		public String toString()
		{
			if( str == null )
			{
				StringBuilder builder = new StringBuilder();
				builder.append( new Date( loggingEvent.timeStamp ) );
				builder.append( ':' ).append( loggingEvent.getLevel() ).append( ':' ).append( loggingEvent.getMessage() );
				str = builder.toString();
			}

			return str;
		}
	}

	public void addLogger( String loggerName, boolean addAppender )
	{
		Logger logger = Logger.getLogger( loggerName );
		if( addAppender )
			logger.addAppender( internalLogAppender );

		loggers.add( logger );
	}

	public Logger[] getLoggers()
	{
		return loggers.toArray( new Logger[loggers.size()] );
	}

	public void setLevel( Level level )
	{
		for( Logger logger : loggers )
		{
			logger.setLevel( level );
		}
	}

	public Logger getLogger( String loggerName )
	{
		for( Logger logger : loggers )
		{
			if( logger.getName().equals( loggerName ) )
				return logger;
		}

		return null;
	}

	private class InternalLogAppender extends AppenderSkeleton
	{
		protected void append( LoggingEvent event )
		{
			addLine( event );
		}

		public void close()
		{
		}

		public boolean requiresLayout()
		{
			return false;
		}
	}

	public boolean monitors( String loggerName )
	{
		for( Logger logger : loggers )
		{
			if( loggerName.startsWith( logger.getName() ) )
				return true;
		}

		return false;
	}

	public void removeLogger( String loggerName )
	{
		for( Logger logger : loggers )
		{
			if( loggerName.equals( logger.getName() ) )
			{
				logger.removeAppender( internalLogAppender );
			}
		}
	}

	public boolean isTailing()
	{
		return tailing;
	}

	public void setTailing( boolean tail )
	{
		this.tailing = tail;
	}

	private class ClearAction extends AbstractAction
	{
		public ClearAction()
		{
			super( "Clear" );
		}

		public void actionPerformed( ActionEvent e )
		{
			model.clear();
		}
	}

	private class SetMaxRowsAction extends AbstractAction
	{
		public SetMaxRowsAction()
		{
			super( "Set Max Rows" );
		}

		public void actionPerformed( ActionEvent e )
		{
			String val = UISupport.prompt( "Set maximum number of log rows to keep", "Set Max Rows", String
					.valueOf( maxRows ) );
			if( val != null )
			{
				try
				{
					maxRows = Long.parseLong( val );
					SoapUI.getSettings().setString( "JLogList#" + title, val );
				}
				catch( NumberFormatException e1 )
				{
					UISupport.beep();
				}
			}
		}
	}

	private class ExportToFileAction extends AbstractAction
	{
		public ExportToFileAction()
		{
			super( "Export to File" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( model.getSize() == 0 )
			{
				UISupport.showErrorMessage( "Log is empty; nothing to export" );
				return;
			}

			File file = UISupport.getFileDialogs().saveAs( JLogList.this, "Save Log [] to File", "*.log", "*.log", null );
			if( file != null )
				saveToFile( file );
		}
	}

	private class CopyAction extends AbstractAction
	{
		public CopyAction()
		{
			super( "Copy to clipboard" );
		}

		public void actionPerformed( ActionEvent e )
		{
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

			StringBuffer buf = new StringBuffer();
			int[] selectedIndices = logList.getSelectedIndices();
			if( selectedIndices.length == 0 )
			{
				for( int c = 0; c < logList.getModel().getSize(); c++ )
				{
					buf.append( logList.getModel().getElementAt( c ).toString() );
					buf.append( "\r\n" );
				}
			}
			else
			{
				for( int c = 0; c < selectedIndices.length; c++ )
				{
					buf.append( logList.getModel().getElementAt( selectedIndices[c] ).toString() );
					buf.append( "\r\n" );
				}
			}

			StringSelection selection = new StringSelection( buf.toString() );
			clipboard.setContents( selection, selection );
		}
	}

	private class EnableAction extends AbstractAction
	{
		public EnableAction()
		{
			super( "Enable" );
		}

		public void actionPerformed( ActionEvent e )
		{
			JLogList.this.setEnabled( enableMenuItem.isSelected() );
		}
	}

	/**
	 * Internal list model that for optimized storage and notifications
	 * 
	 * @author Ole.Matzura
	 */

	private final class LogListModel extends AbstractListModel implements Runnable
	{
		private List<Object> lines = new LinkedList<Object>();

		public int getSize()
		{
			return lines.size();
		}

		public Object getElementAt( int index )
		{
			return lines.get( index );
		}

		public void clear()
		{
			int sz = lines.size();
			if( sz == 0 )
				return;

			lines.clear();
			fireIntervalRemoved( this, 0, sz - 1 );
		}

		public void run()
		{
			Thread.currentThread().setName( "LogList Updater for " + title );

			try
			{
				while( !released && !linesToAdd.isEmpty() )
				{
					try
					{
						if( !linesToAdd.isEmpty() )
						{
							SwingUtilities.invokeAndWait( new Runnable()
							{
								public void run()
								{
									try
									{
										while( !linesToAdd.isEmpty() )
										{
											int sz = lines.size();
											lines.addAll( linesToAdd );
											linesToAdd.clear();
											fireIntervalAdded( LogListModel.this, sz, lines.size() - sz );
										}

										int cnt = 0;
										while( lines.size() > maxRows )
										{
											lines.remove( 0 );
											cnt++ ;
										}

										if( cnt > 0 )
											fireIntervalRemoved( LogListModel.this, 0, cnt - 1 );

										if( tailing )
										{
											logList.ensureIndexIsVisible( lines.size() - 1 );
										}
									}
									catch( Throwable e )
									{
										SoapUI.logError( e );
									}
								}
							} );
						}

						Thread.sleep( 500 );
					}
					catch( Throwable e )
					{
						SoapUI.logError( e );
					}
				}
			}
			finally
			{
				future = null;
			}
		}
	}

	public void release()
	{
		released = true;
	}

	public void saveToFile( File file )
	{
		try
		{
			PrintWriter writer = new PrintWriter( file );
			for( int c = 0; c < model.getSize(); c++ )
			{
				writer.println( model.getElementAt( c ) );
			}

			writer.close();
		}
		catch( Exception e )
		{
			UISupport.showErrorMessage( e );
		}
	}
}
