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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.table.TableModel;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.panels.loadtest.JStatisticsHistoryGraph;
import com.eviware.soapui.support.UISupport;

/**
 * Simple samplesmodel exporter, creates a comma-separated file containing a
 * header row and values for each test step
 * 
 * @author Ole.Matzura
 */

public class ExportSamplesHistoryAction extends AbstractAction
{
	private final JStatisticsHistoryGraph graph;

	public ExportSamplesHistoryAction( JStatisticsHistoryGraph historyGraph )
	{
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/export.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, "Export samples history to a file" );

		this.graph = historyGraph;
	}

	public void actionPerformed( ActionEvent e )
	{
		try
		{
			TableModel model = graph.getModel();
			if( model.getRowCount() == 0 )
			{
				UISupport.showErrorMessage( "No data to export!" );
				return;
			}

			File file = UISupport.getFileDialogs().saveAs( this, "Select file for export" );
			if( file == null )
				return;

			int cnt = exportToFile( file, model );

			UISupport.showInfoMessage( "Saved " + cnt + " rows to file [" + file.getName() + "]" );
		}
		catch( IOException e1 )
		{
			SoapUI.logError( e1 );
		}
	}

	private int exportToFile( File file, TableModel model ) throws IOException
	{
		PrintWriter writer = new PrintWriter( file );
		writerHeader( writer, model );
		int cnt = writeData( writer, model );
		writer.flush();
		writer.close();
		return cnt;
	}

	private int writeData( PrintWriter writer, TableModel model )
	{
		int c = 0;
		for( ; c < model.getRowCount(); c++ )
		{
			for( int i = 0; i < model.getColumnCount(); i++ )
			{
				if( i > 0 )
					writer.print( ',' );

				writer.print( model.getValueAt( c, i ) );
			}

			writer.println();
		}

		return c;
	}

	private void writerHeader( PrintWriter writer, TableModel model )
	{
		for( int i = 0; i < model.getColumnCount(); i++ )
		{
			if( i > 0 )
				writer.print( ',' );

			writer.print( model.getColumnName( i ) );
		}

		writer.println();
	}
}