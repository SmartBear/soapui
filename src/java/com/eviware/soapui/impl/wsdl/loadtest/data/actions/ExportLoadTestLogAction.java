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

package com.eviware.soapui.impl.wsdl.loadtest.data.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.loadtest.log.LoadTestLog;
import com.eviware.soapui.impl.wsdl.loadtest.log.LoadTestLogEntry;
import com.eviware.soapui.support.UISupport;

/**
 * Simple loadtest log exporter, creates a comma-separated file containing a
 * header row and values for each log entry
 * 
 * @author Ole.Matzura
 */

public class ExportLoadTestLogAction extends AbstractAction
{
	private final LoadTestLog loadTestLog;
	private final JXTable logTable;

	public ExportLoadTestLogAction( LoadTestLog loadTestLog, JXTable logTable )
	{
		this.loadTestLog = loadTestLog;
		this.logTable = logTable;
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/export.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, "Export current loadtest log to a file" );
	}

	public void actionPerformed( ActionEvent e )
	{
		try
		{
			if( loadTestLog.getSize() == 0 || ( logTable != null && logTable.getRowCount() == 0 ) )
			{
				UISupport.showErrorMessage( "No data to export!" );
				return;
			}

			File file = UISupport.getFileDialogs().saveAs( this, "Select file for log export" );
			if( file == null )
				return;

			int cnt = exportToFile( file );

			UISupport.showInfoMessage( "Saved " + cnt + " log entries to file [" + file.getName() + "]" );
		}
		catch( IOException e1 )
		{
			SoapUI.logError( e1 );
		}
	}

	public int exportToFile( File file ) throws IOException
	{
		PrintWriter writer = new PrintWriter( file );
		writeHeader( writer );
		int cnt = writeLog( writer );
		writer.flush();
		writer.close();
		return cnt;
	}

	private int writeLog( PrintWriter writer )
	{
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

		int cnt = 0;
		for( int c = 0; c < loadTestLog.getSize(); c++ )
		{
			if( logTable != null )
			{
				int index = logTable.getFilters().convertRowIndexToView( c );
				if( index == -1 )
					continue;
			}

			LoadTestLogEntry logEntry = ( LoadTestLogEntry )loadTestLog.getElementAt( c );
			writer.write( sdf.format( new Date( logEntry.getTimeStamp() ) ) );
			writer.write( ',' );
			writer.write( logEntry.getType() );
			writer.write( ',' );
			String targetStepName = logEntry.getTargetStepName();
			writer.write( targetStepName == null ? "" : targetStepName );
			writer.write( ",\"" );
			writer.write( logEntry.getMessage() );
			writer.write( '"' );
			writer.println();
			cnt++ ;
		}

		return cnt;
	}

	private void writeHeader( PrintWriter writer )
	{
		writer.println( "time,type,step,message" );
	}
}